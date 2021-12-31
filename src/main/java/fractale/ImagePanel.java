package fractale;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import javax.swing.JPanel;

/**
 * Class repertoriant les bufferedimages ayant deja etait genere.
 */
class BufferedImagePool {

	private Map<Dimension, List<BufferedImage>> images;

	public static final BufferedImagePool instance = new BufferedImagePool(); 

	private BufferedImagePool() {
		images = new HashMap<>();
	}

	public BufferedImage get(int w, int h) {
		Dimension k = new Dimension(w , h);
		List<BufferedImage> imgs = images.computeIfAbsent(k, x -> new ArrayList<>());
		if (imgs.isEmpty()) {
			System.out.println("allocating new buffered image " + w + "x" + h);
			return new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		}
		return imgs.remove(imgs.size() - 1);
	}

	public void free(BufferedImage i) {
		//		if (i == null)
		//			return;
		//		Dimension k = new Dimension(i.getWidth(), i.getHeight());
		//		List<BufferedImage> imgs = images.computeIfAbsent(k, x -> new ArrayList<>());
		//		if (!imgs.contains(i))
		//			imgs.add(i);
	}

}

class ImagePanel extends JPanel {

	/**
	 * Les diff�rents mode de calculs lors du d�placement.
	 */
	public enum TransitionMode {
		FLICKING,
		LOW_RES,
		LOW_RES_COMPOSITE,
		FILL_MISSING
	}


	interface ImagePanelEventHandler extends MouseWheelListener, MouseMotionListener {
	}

	class EventHandlerDelegator implements ImagePanelEventHandler {
		ImagePanelEventHandler delegate;

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (delegate != null)
				delegate.mouseWheelMoved(e);
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (delegate != null)
				delegate.mouseDragged(e);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			if (delegate != null)
				delegate.mouseMoved(e);
		}

		public void setDelegate(ImagePanelEventHandler d) {
			delegate = d;
		}
	}

	void renderCenteredScaled(Graphics2D g, BufferedImage img, double scale) {
		if (scale > 1) {  
			int srcWidth  = (int)(img.getWidth()  / scale);
			int srcHeight = (int)(img.getHeight() / scale);
			int srcX = (img.getWidth()  - srcWidth) / 2;
			int srcY = (img.getHeight() - srcHeight) / 2;
			g.drawImage(img, 
					0,     0,     config.outputWidth-1, config.outputHeight-1, 
					srcX, srcY, srcX + srcWidth-1,     srcY + srcHeight-1, 
					null);
		} else {
			int dstWidth  = (int)(img.getWidth()  * scale);
			int dstHeight = (int)(img.getHeight() * scale);
			int dstX = (img.getWidth() - dstWidth) / 2;
			int dstY = (img.getHeight() - dstHeight) / 2;
			g.drawImage(img, 
					dstX, dstY, dstX + dstWidth - 1, dstY + dstHeight - 1, 
					0,     0,     img.getWidth()-1, img.getHeight()-1, null);
		}
	}

	/**
	 * Lors du deplacement on ne fait pas de calculs intermediaire.
	 * <p> On ne fait l'affichage que lorsque la fractale a etait genere</p>
	 */
	class EventHandlerFlicking implements ImagePanelEventHandler {

		int lastX;
		int lastY;

		@Override
		public void mouseDragged(MouseEvent e) {
			int wdx = e.getX() - lastX;
			int wdy = e.getY() - lastY;
			System.out.println("Je suis dragué  " + wdx + " x " + wdy);
			config.move(- config.xStep * wdx, - config.yStep * wdy);
			imgX += wdx;
			imgY += wdy;
			repaint();
			asynchUpdate();
			lastX = e.getX();
			lastY = e.getY();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			lastX = e.getX();
			lastY = e.getY();
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
				List<BufferedImage> imgs = new ArrayList<>();
				double zoomFactor = scaleFromMouseWheel(e.getWheelRotation());
				config.scale(zoomFactor);
				BufferedImage img3 = BufferedImagePool.instance.get(config.outputWidth, config.outputHeight);
				imgs.add(img3);
				Graphics2D g = img3.createGraphics();
				if (zoomFactor > 1) {
					g.setColor(Color.BLACK);
					g.fillRect(0, 0, config.outputWidth, config.outputHeight);
				}
				renderCenteredScaled(g, img, 1 / zoomFactor);
				g.dispose();
				imgX = 0;
				imgY = 0;
				imgs.add(img);
				img = img3;
				for (BufferedImage i : imgs)
					BufferedImagePool.instance.free(i);
				repaint();
				asynchUpdate();
			}
		}

	}

	/**
	 * Lors du d�placement on calcule une image en r�solution moindre (200 par 200). 
	 * <p> On affiche la fractale en r�solution r�duite (qui a �tait scale a la taille de fen�tre, 
	 * puis on affiche la fractale en bonne r�solution une fois le calcul termin�.</p>
	 */
	class EventHandlerLowRes implements ImagePanelEventHandler {

		int lastX;
		int lastY;
		int resolution = 200;

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			double zoomFactor = scaleFromMouseWheel(e.getWheelRotation());
			config.scale(zoomFactor);
			BufferedImage img3 = BufferedImagePool.instance.get(config.outputWidth, config.outputHeight);
			Graphics2D g = img3.createGraphics();
			FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
					.outputWidth (resolution)
					.realRange   (config.minReal,      config.maxReal)
					.outputHeight(resolution)
					.imgRange    (config.minImaginary, config.maxImaginary)
					.build();
			BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
			g.drawImage(img2, 
					0,0, config.outputWidth-1, config.outputHeight-1, 
					0, 0, img2.getWidth()-1, img2.getHeight()-1, null);
			g.dispose();
			imgX = 0;
			imgY = 0;
			BufferedImagePool.instance.free(img);
			img = img3;
			asynchUpdate();
			repaint();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			int wdx = e.getX() - lastX;
			int wdy = e.getY() - lastY;
			System.out.println("Je suis dragué  " + wdx + " x " + wdy);
			config.move(- config.xStep * wdx, - config.yStep * wdy);
			FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
					.outputWidth (resolution)
					.realRange   (config.minReal,      config.maxReal)
					.outputHeight(resolution)
					.imgRange    (config.minImaginary, config.maxImaginary)
					.build();
			BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
			BufferedImage img3 = BufferedImagePool.instance.get(config.outputWidth, config.outputHeight);
			Graphics2D g = img3.createGraphics();
			g.drawImage(img2, 0,0, config.outputWidth, config.outputHeight, 0, 0, img2.getWidth(), img2.getHeight(), null);
			g.dispose();
			imgX = 0;
			imgY = 0;
			BufferedImagePool.instance.free(img2);
			BufferedImagePool.instance.free(img);
			img = img3;

			asynchUpdate();
			repaint();
			lastX = e.getX();
			lastY = e.getY();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			lastX = e.getX();
			lastY = e.getY();
		}

	}

	/**
	 * Lors du d�placement on garde la partie de la fractale d�j� g�n�r�e en bonne qualit� au centre.
	 * <p> On affiche la fractale en bonne r�solution au centren puis lors du d�placement on on affiche 
	 * la fracatle basse r�solution l� o� la fractale bonne r�solution n'as pas encore �tait g�n�r�.</p>
	 */
	class EventHandlerLowResComposite implements ImagePanelEventHandler {

		int lastX;
		int lastY;
		int resolution = 200;

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {

			double zoomFactor = scaleFromMouseWheel(e.getWheelRotation());
			config.scale(zoomFactor);
			BufferedImage img3 = BufferedImagePool.instance.get(config.outputWidth, config.outputHeight);
			Graphics2D g = img3.createGraphics();

			if (zoomFactor > 1) {
				FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
						.outputWidth (resolution)
						.realRange   (config.minReal,      config.maxReal)
						.outputHeight(resolution)
						.imgRange    (config.minImaginary, config.maxImaginary)
						.build();
				BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
				g.drawImage(img2, 
						0,0, config.outputWidth-1, config.outputHeight-1, 
						0, 0, img2.getWidth()-1, img2.getHeight()-1, null);
			}
			renderCenteredScaled(g, img, 1 / zoomFactor);

			g.dispose();
			imgX = 0;
			imgY = 0;
			BufferedImagePool.instance.free(img);
			img = img3;
			asynchUpdate();
			repaint();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			int wdx = e.getX() - lastX;
			int wdy = e.getY() - lastY;
			config.move(- config.xStep * wdx, - config.yStep * wdy);
			FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
					.outputWidth (200)
					.realRange   (config.minReal,      config.maxReal)
					.outputHeight(200)
					.imgRange    (config.minImaginary, config.maxImaginary)
					.build();
			BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
			BufferedImage img3 = BufferedImagePool.instance.get(config.outputWidth, config.outputHeight);
			Graphics2D g = img3.createGraphics();
			g.drawImage(img2, 0,0, config.outputWidth, config.outputHeight, 0, 0, img2.getWidth(), img2.getHeight(), null);
			g.drawImage(img, wdx, wdy, null);
			g.dispose();
			imgX = 0;
			imgY = 0;
			BufferedImagePool.instance.free(img2);
			BufferedImagePool.instance.free(img);
			img = img3;

			asynchUpdate();
			repaint();
			lastX = e.getX();
			lastY = e.getY();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			lastX = e.getX();
			lastY = e.getY();
		}

	}


	/**
	 * Lors du deplacement on calcule les rectangle de fractale manquante, puis on les ajoute a l'image existante.
	 */
	class EventHandlerFillMissing implements ImagePanelEventHandler {

		int lastX;
		int lastY;

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			List<BufferedImage> imgs = new ArrayList<>();
			double zoomFactor = scaleFromMouseWheel(e.getWheelRotation());
			config.scale(zoomFactor);
			BufferedImage img3 = BufferedImagePool.instance.get(config.outputWidth, config.outputHeight);
			Graphics2D g = img3.createGraphics();
			if (zoomFactor < 1) {  
				int sz = 200;     
				int sx = (config.outputWidth - 200) / 2;
				int sy = (config.outputHeight - 200) / 2;
				FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
						.outputWidth (sz)
						.realRange   (config.minReal + sx * config.xStep,      config.minReal + (sx + sz -1) * config.xStep)
						.outputHeight(sz)
						.imgRange    (config.minImaginary + sy * config.yStep, config.minImaginary + (sy + sz -1) * config.yStep)
						.build();
				BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
				imgs.add(img2);
				renderCenteredScaled(g, img, 1 / zoomFactor);
				g.drawImage(img2, sx, sy, null);
			} else {
				int srcWidth  = (int)(img.getWidth()  / zoomFactor);
				int srcHeight = (int)(img.getHeight() / zoomFactor);
				int srcDx = (img.getWidth() - srcWidth) / 2;
				int srcDy = (img.getHeight() - srcHeight) / 2;
				renderCenteredScaled(g, img, 1 / zoomFactor);
				{
					FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
							.outputWidth (srcDx)
							.realRange   (config.minReal,      config.minReal + (srcDx - 1) * config.xStep)
							.outputHeight(config.outputHeight)
							.imgRange    (config.minImaginary, config.maxImaginary)
							.build();
					System.out.println("" + config.xStep + " " + nc.xStep);
					BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
					imgs.add(img2);
					g.drawImage(img2, 0, 0, null);
				}
				{
					FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
							.outputWidth (srcDx + 2)
							.realRange   (config.maxReal - (srcDx + 1) * config.xStep,      config.maxReal)
							.outputHeight(config.outputHeight)
							.imgRange    (config.minImaginary, config.maxImaginary)
							.build();
					System.out.println("" + config.xStep + " " + nc.xStep);
					BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
					imgs.add(img2);
					g.drawImage(img2, config.outputWidth - srcDx - 2, 0, null);
				}
				{
					FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
							.outputWidth (config.outputWidth)
							.realRange   (config.minReal,      config.maxReal)
							.outputHeight(srcDy)
							.imgRange    (config.minImaginary, config.minImaginary + (srcDy - 1) * config.yStep)
							.build();
					BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
					imgs.add(img2);
					g.drawImage(img2, 0, 0, null);
				}
				{
					FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
							.outputWidth (config.outputWidth)
							.realRange   (config.minReal,      config.maxReal)
							.outputHeight(srcDy + 2)
							.imgRange    (config.maxImaginary - (srcDy + 1) * config.yStep, config.maxImaginary)
							.build();
					BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
					imgs.add(img2);
					g.drawImage(img2, 0, config.outputHeight - srcDy - 2, null);
				}
			}
			g.dispose();
			imgX = 0;
			imgY = 0;
			imgs.add(img);
			img = img3;

			for (BufferedImage i : imgs)
				BufferedImagePool.instance.free(i);

			repaint();
			asynchUpdate();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			int wdx = e.getX() - lastX;
			int wdy = e.getY() - lastY;

			double wfdx = config.xStep * wdx;
			double wfdy = config.yStep * wdy;
			config.move(- wfdx, - wfdy);

			BufferedImage img3 = BufferedImagePool.instance.get(config.outputWidth, config.outputHeight);

			Graphics2D g = img3.createGraphics();
			g.setColor(Color.RED);
			g.fillRect(0, 0, config.outputWidth, config.outputHeight);

			List<BufferedImage> allocImages = new ArrayList<>();

			if (wdy > 0) {
				FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
						.outputWidth (config.outputWidth)
						.realRange   (config.minReal,      config.maxReal)
						.outputHeight(wdy+1)
						.imgRange    (config.minImaginary, config.minImaginary + wfdy)
						.build();
				BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
				allocImages.add(img2);
				g.drawImage(img2, 0, 0, null);
			} else if (wdy < 0) {
				FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
						.outputWidth (config.outputWidth)
						.realRange   (config.minReal,      config.maxReal)
						.outputHeight(-wdy+1)
						.imgRange    (config.maxImaginary + wfdy, config.maxImaginary)
						.build();
				BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
				allocImages.add(img2);								
				g.drawImage(img2, 0, config.outputHeight + wdy - 1 , null);
			}
			if (wdx > 0) {
				FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
						.outputHeight(config.outputHeight)
						.imgRange    (config.minImaginary, config.maxImaginary)
						.outputWidth (wdx+1)
						.realRange   (config.minReal,      config.minReal + wfdx)
						.build();
				BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
				allocImages.add(img2);								
				g.drawImage(img2, 0, 0, null);
			} else if (wdx < 0) {
				FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
						.outputHeight(config.outputHeight)
						.imgRange    (config.minImaginary, config.maxImaginary)
						.outputWidth (-wdx+1)
						.realRange   (config.maxReal + wfdx,      config.maxReal)
						.build();
				BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
				allocImages.add(img2);								
				g.drawImage(img2, config.outputWidth + wdx - 1, 0, null);
			}

			imgY = 0;
			imgX = 0;
			g.drawImage(img,  wdx, wdy, null);
			g.dispose();
			allocImages.add(img);
			img = img3;

			for (BufferedImage i : allocImages)
				BufferedImagePool.instance.free(i);
			repaint();
			asynchUpdate();
			lastX = e.getX();
			lastY = e.getY();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			lastX = e.getX();
			lastY = e.getY();
		}

	}


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int imgX;
	int imgY;
	AsynchFractalEngine calculEnCours;

	BufferedImage img;
	FractaleRenderConfig config;
	JolieFonction f;
	BiFunction<FractaleRenderConfig, Integer, Color> c;

	WindowFitMode wfm;

	double zoom;

	EventHandlerDelegator ehd;

	double zoomSpeed = 1.03;

	public ImagePanel(FractaleRenderConfig config, JolieFonction  f, BiFunction<FractaleRenderConfig, Integer, Color> c ) { 
		this.img = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(config, f, c);

		this.imgX = this.imgY = 0;
		this.calculEnCours = null;

		this.wfm = WindowFitMode.FILL;

		this.config = config;

		this.f = f;
		this.c = c;
		this.zoom = 1;


		this.ehd = new EventHandlerDelegator();
		this.ehd.setDelegate(new EventHandlerFlicking());
		addMouseMotionListener(this.ehd);
		addMouseWheelListener(this.ehd);

		setOpaque(true);
		setBackground(Color.BLACK);

		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				System.out.println("resize " + getWidth() + "x" + getHeight());
				config.setOutputSize(getWidth(),getHeight());
				setWindowFitMode(wfm);
				asynchUpdate();
			}
		});

	}


/**
 * On calcule que 1 image de fractale hd.
 * <p>
 * Si on veut en calculer une nouvelle on enleve le calcul de la derniere fractale.
 *</p>
 */
	private void asynchUpdate() {
		if (calculEnCours != null) {
			calculEnCours.interrupt();
			calculEnCours = null;
		}

		calculEnCours = new AsynchFractalEngine();
		calculEnCours.runAndDo(config, f, c, img2 -> {
			imgX = 0;
			imgY = 0;
			BufferedImagePool.instance.free(img);
			img  = img2;
			repaint();
		});
	}

	/**
	 * Fonction qui s'occupe de d'afficher l'img actuelle sur le graphics g
	 * @param g le graphics d'affichage de l'image.
	 */
	public void paintComponent(Graphics g) {
		System.out.println("paint");
		int w = img.getWidth();
		int h = img.getHeight();
		int fw = w;
		int fh = h;
		switch (wfm) { 
		case MINFIT :
			fw = fh = Math.min(w, h);
			break;
		case MAXFIT : 
			fw = fh = Math.max(w, h);
			break;
		case FILL : 
			fw = w;
			fh = h;
			break;
		}


		Graphics2D g_ = (Graphics2D)g;
		g_.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g_.setRenderingHint(RenderingHints.KEY_RENDERING	, RenderingHints.VALUE_RENDER_QUALITY);
		g_.setRenderingHint(RenderingHints.KEY_ANTIALIASING,	RenderingHints.VALUE_ANTIALIAS_ON); 
		g_.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);


		super.paintComponent(g);
		int idx = imgX + (getWidth() - fw)/2;
		int idy = imgY + (getHeight() - fh)/2;
		System.out.println("repaint " + idx + " " + idy + " " + img);
		g.drawImage(img, idx, idy, null);
		g.setColor(Color.WHITE);
		FractaleRenderEngine.information(g_, config, f);	
	}


	public void setColorScheme(ColorScheme colorScheme) {
		this.c = colorScheme.f;
		redraw();
	}


	public void setFunction(JolieFonction f2) {
		this.f = f2;
		redraw();
	}


	public void setIterations(int iterations) {
		this.config.maxIterations = iterations;
		redraw();
	}

	public void redraw() {	
		asynchUpdate();
	}

	/**
	 * Definition du mode de transition (lors du deplacement)
	 * @param tm mode de transition
	 */
	public void setTransitionMode(TransitionMode tm) {
		switch (tm) {
		case FLICKING: ehd.setDelegate(new EventHandlerFlicking()); break;
		case LOW_RES: ehd.setDelegate(new EventHandlerLowRes()); break;
		case LOW_RES_COMPOSITE:  ehd.setDelegate(new EventHandlerLowResComposite()); break;
		case FILL_MISSING:  ehd.setDelegate(new EventHandlerFillMissing()); break;
		}
		redraw();
	}

	/**
	 *  Definition du mode de fitting de l'image de la fractale
	 * @param wfm mode de fitting
	 */
	public void setWindowFitMode(WindowFitMode wfm) {
		this.wfm = wfm;
		int w = getWidth();
		int h = getHeight();
		switch (wfm) { 
		case MINFIT :
			if (w > h)
				config.setOutputSize(h, h);
			else 
				config.setOutputSize(w, w);
			break;
		case MAXFIT : 
			if (w > h)
				config.setOutputSize(w, w);
			else
				config.setOutputSize(h, h);
			break;
		case FILL : 
			config.setOutputSize(w, h);
			break;
		}
		redraw();
	}

	public double scaleFromMouseWheel(int wheelRotations) {
		return Math.pow(this.zoomSpeed, wheelRotations);
	}



	public void setMaxIteration(int value) {
		if (value < 1 )
			throw new IllegalArgumentException("La valeur n'est pas bonne");
		config.maxIterations = value;
		redraw();
	}


}