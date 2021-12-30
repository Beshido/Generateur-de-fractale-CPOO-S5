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
	
	class EventHandlerFlicking implements ImagePanelEventHandler {
		
		int lastX;
		int lastY;
		
		@Override
		public void mouseDragged(MouseEvent e) {
			int wdx = e.getX() - lastX;
			int wdy = e.getY() - lastY;
			System.out.println("Je suis dragué  " + wdx + " x " + wdy);
//			double wfdx = config.xStep * wdx;
//			double wfdy = config.yStep * wdy;
//			config.ranges(config.minReal - wfdx, config.maxReal - wfdx, config.minImaginary - wfdy, config.maxImaginary - wfdy);
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
//				if (zoomFactor < 1) {  
//					int srcWidth  = (int)(img.getWidth()  * zoomFactor);
//					int srcHeight = (int)(img.getHeight() * zoomFactor);
//					int srcDx = (img.getWidth() - srcWidth) / 2 + 1;
//					int srcDy = (img.getHeight() - srcHeight) / 2 + 1;
//					System.out.println("z " + srcWidth + " x " + srcHeight + " o " + srcDx + " " + srcDy);
//					g.drawImage(img, 
//							0,     0,     config.outputWidth-1, config.outputHeight-1, 
//							srcDx, srcDy, srcDx + srcWidth,     srcDy + srcHeight, 
//							null);
//				} else {
//					int srcWidth  = (int)(img.getWidth()  / zoomFactor);
//					int srcHeight = (int)(img.getHeight() / zoomFactor);
//					int srcDx = (img.getWidth() - srcWidth) / 2 + 1;
//					int srcDy = (img.getHeight() - srcHeight) / 2 + 1;
//					g.setColor(Color.BLACK);
//					g.fillRect(0, 0, config.outputWidth, config.outputHeight);
//					g.drawImage(img, 
//							srcDx, srcDy, srcDx + srcWidth, srcDy + srcHeight, 
//							0,     0,     img.getWidth()-1, img.getHeight()-1, null);
//				}
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
	
	class EventHandlerLowRes implements ImagePanelEventHandler {
		
		int lastX;
		int lastY;
		int resolution = 200;
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
//			final double zoomAmount = 1.1;
//			double zoomFactor = zoomAmount * e.getWheelRotation();
//			if (e.getWheelRotation() < 0)
//				zoomFactor = - 1 / zoomFactor;
//			zoom *= zoomFactor;
//			double zv = zoom; // 1 - zoom;
//			System.out.println("wheel : " + e.getWheelRotation() + " current zoom : " + zoom + " zf " + zoomFactor);
//			double fcx = (config.maxReal + config.minReal)/2;
//			double fcy = (config.maxImaginary + config.minImaginary)/2;
//			config.ranges(fcx - zv, fcx + zv, fcy - zv, fcy + zv);
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
//			double wfdx = config.xStep * wdx;
//			double wfdy = config.yStep * wdy;
//			
//			config.maxReal -= wfdx;
//			config.minReal -= wfdx;
//			config.maxImaginary -= wfdy;
//			config.minImaginary -= wfdy;
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
//			g.drawImage(img, wdx, wdy, null);
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
	class EventHandlerLowResComposite implements ImagePanelEventHandler {
		
		int lastX;
		int lastY;
		int resolution = 200;
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
//			final double zoomAmount = 1.1;
//			double zoomFactor = zoomAmount * e.getWheelRotation();
//			if (e.getWheelRotation() < 0)
//				zoomFactor = - 1 / zoomFactor;
//			zoom *= zoomFactor;
//			double zv = zoom; // 1 - zoom;
//			System.out.println("wheel : " + e.getWheelRotation() + " current zoom : " + zoom + " zf " + zoomFactor);
//			double fcx = (config.maxReal + config.minReal)/2;
//			double fcy = (config.maxImaginary + config.minImaginary)/2;
//			config .minReal      = fcx - zv;
//			config .maxReal      = fcx + zv;
//			config .minImaginary = fcy - zv;
//			config .maxImaginary = fcy + zv;
//			config.ranges(fcx - zv, fcx + zv, fcy - zv, fcy + zv);
			double zoomFactor = scaleFromMouseWheel(e.getWheelRotation());
			config.scale(zoomFactor);
			// BufferedImage img3 = new BufferedImage(config.outputWidth, config.outputHeight, BufferedImage.TYPE_INT_RGB);
			BufferedImage img3 = BufferedImagePool.instance.get(config.outputWidth, config.outputHeight);
			Graphics2D g = img3.createGraphics();
//			if (zoomFactor < 1) {  
//				int srcWidth  = (int)(img.getWidth()  * zoomFactor);
//				int srcHeight = (int)(img.getHeight() * zoomFactor);
//				int srcDx = (img.getWidth() - srcWidth) / 2;
//					int srcDy = (img.getHeight() - srcHeight) / 2;
//				System.out.println("z " + srcWidth + " x " + srcHeight + " o " + srcDx + " " + srcDy);
//				g.drawImage(img, 
//						0,     0, config.outputWidth-1, config.outputHeight-1, 
//						srcDx, srcDy, srcDx + srcWidth,             srcDy + srcHeight, 
//						null);
//			} else {
//				FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
//						.outputWidth (resolution)
//						.realRange   (config.minReal,      config.maxReal)
//						.outputHeight(resolution)
//						.imgRange    (config.minImaginary, config.maxImaginary)
//						.build();
//				BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
//				g.drawImage(img2, 
//						0,0, config.outputWidth-1, config.outputHeight-1, 
//						0, 0, img2.getWidth()-1, img2.getHeight()-1, null);
//				int srcWidth  = (int)(img.getWidth()  / zoomFactor);
//				int srcHeight = (int)(img.getHeight() / zoomFactor);
//				int srcDx = (img.getWidth() - srcWidth) / 2;
//					int srcDy = (img.getHeight() - srcHeight) / 2;
//				g.drawImage(img, 
//						srcDx, srcDy, srcDx + srcWidth,             srcDy + srcHeight, 
//						0, 0, img.getWidth()-1, img.getHeight()-1, null);
//				BufferedImagePool.instance.free(img2);
//			}
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
//			System.out.println("Je suis dragué  " + wdx + " x " + wdy);
//			double wfdx = config.xStep * wdx;
//			double wfdy = config.yStep * wdy;
//			
//			config.maxReal -= wfdx;
//			config.minReal -= wfdx;
//			config.maxImaginary -= wfdy;
//			config.minImaginary -= wfdy;
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
	
	class EventHandlerFillMissing implements ImagePanelEventHandler {
		
		int lastX;
		int lastY;
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			List<BufferedImage> imgs = new ArrayList<>();
//			final double zoomAmount = 1.1;
//			double zoomFactor = zoomAmount * e.getWheelRotation();
//			if (e.getWheelRotation() < 0)
//				zoomFactor = - 1 / zoomFactor;
//			zoom *= zoomFactor;
//			double zv = zoom; // 1 - zoom;
//			System.out.println("wheel : " + e.getWheelRotation() + " current zoom : " + zoom + " zf " + zoomFactor);
//			double fcx = (config.maxReal + config.minReal)/2;
//			double fcy = (config.maxImaginary + config.minImaginary)/2;
//			config .minReal      = fcx - zv; // += (0.1 * e.getWheelRotation());
//			config .maxReal      = fcx + zv; //(0.1 * e.getWheelRotation());
//			config .minImaginary = fcy - zv; //(0.1 * e.getWheelRotation());
//			config .maxImaginary = fcy + zv; // (0.1 * e.getWheelRotation());
//			config.ranges(fcx - zv, fcx + zv, fcy - zv, fcy + zv);
			double zoomFactor = scaleFromMouseWheel(e.getWheelRotation());
			config.scale(zoomFactor);
			// BufferedImage img3 = new BufferedImage(config.outputWidth, config.outputHeight, BufferedImage.TYPE_INT_RGB);
			BufferedImage img3 = BufferedImagePool.instance.get(config.outputWidth, config.outputHeight);
			Graphics2D g = img3.createGraphics();
//			if (zoomFactor < 1) {  
//				int srcWidth  = (int)(img.getWidth()  * zoomFactor);
//				int srcHeight = (int)(img.getHeight() * zoomFactor);
//				int srcDx = (img.getWidth() - srcWidth) / 2;
//				int srcDy = (img.getHeight() - srcHeight) / 2;
//				System.out.println("** z " + srcWidth + " x " + srcHeight + " o " + srcDx + " " + srcDy);
//				int sz = 200;     
//				int sx = (config.outputWidth - 200) / 2;
//				int sy = (config.outputHeight - 200) / 2;
//				System.out.println("zoom in " + sx + "x" + sy);
//				FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
//						.outputWidth (sz)
//						.realRange   (config.minReal + sx * config.xStep,      config.minReal + (sx + sz) * config.xStep)
//						.outputHeight(sz)
//						.imgRange    (config.minImaginary + sy * config.yStep, config.minImaginary + (sy + sz) * config.yStep)
//						.build();
//				BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
//				imgs.add(img2);
////				g.setColor(Color.RED);
////				g.fillRect(0, 0, sx, sy);
//				
//				g.drawImage(img, 
//						0,     0, config.outputWidth-1, config.outputHeight-1, 
//						srcDx, srcDy, srcDx + srcWidth,             srcDy + srcHeight, 
////						0,     0, config.outputWidth-1, config.outputHeight-1, 
//						null);
//				g.drawImage(img2, sx, sy, null);
////				g.drawImage(img, 0, 0, null);
////				BufferedImagePool.instance.free(img2);
//			} else {
////				FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
////						.outputWidth (200)
////						.realRange   (config.minReal,      config.maxReal)
////						.outputHeight(200)
////						.imgRange    (config.minImaginary, config.maxImaginary)
////						.build();
////				BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
////				imgs.add(img2);
////				//					g.setColor(Color.RED);
////				//					g.fillRect(0, 0, config.outputWidth, config.outputHeight);
////				g.drawImage(img2, 
////						0,0, config.outputWidth-1, config.outputHeight-1, 
////						0, 0, img2.getWidth()-1, img2.getHeight()-1, null);
//				int srcWidth  = (int)(img.getWidth()  / zoomFactor);
//				int srcHeight = (int)(img.getHeight() / zoomFactor);
//				int srcDx = (img.getWidth() - srcWidth) / 2;
//				int srcDy = (img.getHeight() - srcHeight) / 2;
//				g.drawImage(img, 
//						srcDx, srcDy, srcDx + srcWidth,             srcDy + srcHeight, 
//						0, 0, img.getWidth()-1, img.getHeight()-1, null);
//				
//				// Generate missing 4 borders
//				{
//					FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
//							.outputWidth (srcDx)
//							.realRange   (config.minReal,      config.minReal + srcDx * config.xStep)
//							.outputHeight(config.outputHeight)
//							.imgRange    (config.minImaginary, config.maxImaginary)
//							.build();
//					BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
//					imgs.add(img2);
//					g.drawImage(img2, 0, 0, null);
//				}
//				{
//					FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
//							.outputWidth (srcDx)
//							.realRange   (config.maxReal - srcDx * config.xStep,      config.maxReal)
//							.outputHeight(config.outputHeight)
//							.imgRange    (config.minImaginary, config.maxImaginary)
//							.build();
//					BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
//					imgs.add(img2);
//					g.drawImage(img2, config.outputWidth - srcDx, 0, null);
//				}
//				{
//					FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
//							.outputWidth (config.outputWidth)
//							.realRange   (config.minReal,      config.maxReal)
//							.outputHeight(srcDy)
//							.imgRange    (config.minImaginary, config.minImaginary + srcDy * config.yStep)
//							.build();
//					BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
//					imgs.add(img2);
//					g.drawImage(img2, 0, 0, null);
//				}
//				{
//					FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
//							.outputWidth (config.outputWidth)
//							.realRange   (config.minReal,      config.maxReal)
//							.outputHeight(srcDy)
//							.imgRange    (config.maxImaginary - srcDy * config.yStep, config.maxImaginary)
//							.build();
//					BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
//					imgs.add(img2);
//					g.drawImage(img2, 0, config.outputHeight - srcDy, null);
//				}
////				g.setColor(Color.RED);
////				g.drawRect(0, 0, srcDx, config.outputHeight);
////				g.drawRect(config.outputWidth - srcDx, 0, srcDx, config.outputHeight);
////				g.drawRect(0, 0, config.outputWidth, srcDy);
////				g.drawRect(0, config.outputHeight - srcDy, config.outputWidth, srcDy);
//				
////				BufferedImagePool.instance.free(img2);
////				g.drawImage(img, 0, 0, null);
//			}
			if (zoomFactor < 1) {  
//				int srcWidth  = (int)(img.getWidth()  * zoomFactor);
//				int srcHeight = (int)(img.getHeight() * zoomFactor);
//				int srcDx = (img.getWidth() - srcWidth) / 2;
//				int srcDy = (img.getHeight() - srcHeight) / 2;
//				System.out.println("** z " + srcWidth + " x " + srcHeight + " o " + srcDx + " " + srcDy);
				int sz = 200;     
				int sx = (config.outputWidth - 200) / 2;
				int sy = (config.outputHeight - 200) / 2;
//				System.out.println("zoom in " + sx + "x" + sy);
				FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
						.outputWidth (sz)
						.realRange   (config.minReal + sx * config.xStep,      config.minReal + (sx + sz -1) * config.xStep)
						.outputHeight(sz)
						.imgRange    (config.minImaginary + sy * config.yStep, config.minImaginary + (sy + sz -1) * config.yStep)
						.build();
				BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
				imgs.add(img2);
//				g.setColor(Color.RED);
//				g.fillRect(0, 0, sx, sy);
				
//				g.drawImage(img, 
//						0,     0, config.outputWidth-1, config.outputHeight-1, 
//						srcDx, srcDy, srcDx + srcWidth,             srcDy + srcHeight, 
////						0,     0, config.outputWidth-1, config.outputHeight-1, 
//						null);
				renderCenteredScaled(g, img, 1 / zoomFactor);
				g.drawImage(img2, sx, sy, null);
//				g.drawImage(img, 0, 0, null);
//				BufferedImagePool.instance.free(img2);
			} else {
//				FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
//						.outputWidth (200)
//						.realRange   (config.minReal,      config.maxReal)
//						.outputHeight(200)
//						.imgRange    (config.minImaginary, config.maxImaginary)
//						.build();
//				BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
//				imgs.add(img2);
//				//					g.setColor(Color.RED);
//				//					g.fillRect(0, 0, config.outputWidth, config.outputHeight);
//				g.drawImage(img2, 
//						0,0, config.outputWidth-1, config.outputHeight-1, 
//						0, 0, img2.getWidth()-1, img2.getHeight()-1, null);
				int srcWidth  = (int)(img.getWidth()  / zoomFactor);
				int srcHeight = (int)(img.getHeight() / zoomFactor);
				int srcDx = (img.getWidth() - srcWidth) / 2;
				int srcDy = (img.getHeight() - srcHeight) / 2;
//				g.drawImage(img, 
//						srcDx, srcDy, srcDx + srcWidth,             srcDy + srcHeight, 
//						0, 0, img.getWidth()-1, img.getHeight()-1, null);
				renderCenteredScaled(g, img, 1 / zoomFactor);
				// Generation des 4 bords manquants
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
//			System.out.println("Je suis dragué  " + wdx + " x " + wdy);
//			// update drawing
			double wfdx = config.xStep * wdx;
			double wfdy = config.yStep * wdy;
//			// redraw();
//			
//			
//			
//			config.maxReal -= wfdx;
//			config.minReal -= wfdx;
//			config.maxImaginary -= wfdy;
//			config.minImaginary -= wfdy;
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
	
//	FractaleRenderConfig refConfig;
	WindowFitMode wfm;
	
	double zoom;
	
	EventHandlerDelegator ehd;
	
//	double zoomSpeed = 1.1;
	double zoomSpeed = 1.03;

	public ImagePanel(FractaleRenderConfig config, JolieFonction  f, BiFunction<FractaleRenderConfig, Integer, Color> c ) { 
		this.img = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(config, f, c);
		
		this.imgX = this.imgY = 0;
		this.calculEnCours = null;
		
		this.wfm = WindowFitMode.FILL;
		
//		this.refConfig = config;
//		this.config = getCurrentConfig();
		this.config = config;
				
		this.f = f;
		this.c = c;
		this.zoom = 1;
//		this.tMode = TransitionMode.FLICKING;
//		this.tMode = TransitionMode.LOW_DEF;
		
		this.ehd = new EventHandlerDelegator();
		this.ehd.setDelegate(new EventHandlerFlicking());
//		this.ehd.setDelegate(new EventHandlerLowRes());
		addMouseMotionListener(this.ehd);
		addMouseWheelListener(this.ehd);
		
		setOpaque(true);
		setBackground(Color.BLACK);
//		setBackground(Color.GREEN);
		
		addComponentListener(new ComponentAdapter() {
	        public void componentResized(ComponentEvent e) {
	        	System.out.println("resize " + getWidth() + "x" + getHeight());
	        	config.setOutputSize(getWidth(),getHeight());
	        	setWindowFitMode(wfm);
	        	asynchUpdate();
	        }
		});

		
//		addMouseWheelListener(e -> {
//			if (true) return;
//			if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
//				final double zoomAmount = 1.1;
//				double zoomFactor = zoomAmount * e.getWheelRotation();
//				if (e.getWheelRotation() < 0)
//					zoomFactor = - 1 / zoomFactor;
//				zoom *= zoomFactor;
//				double zv = zoom; // 1 - zoom;
//				System.out.println("wheel : " + e.getWheelRotation() + " current zoom : " + zoom + " zf " + zoomFactor);
//				double fcx = (config.maxReal + config.minReal)/2;
//				double fcy = (config.maxImaginary + config.minImaginary)/2;
//				this.config .minReal      = fcx - zv; // += (0.1 * e.getWheelRotation());
//				this.config .maxReal      = fcx + zv; //(0.1 * e.getWheelRotation());
//				this.config .minImaginary = fcy - zv; //(0.1 * e.getWheelRotation());
//				this.config .maxImaginary = fcy + zv; // (0.1 * e.getWheelRotation());
//				this.config.ranges(fcx - zv, fcx + zv, fcy - zv, fcy + zv);
//				
//				switch (tMode) {
//				case FLICKING: {
//					BufferedImage img3 = BufferedImagePool.instance.get(config.outputWidth, config.outputHeight);
//					Graphics2D g = img3.createGraphics();
//					if (zoomFactor < 1) {  
//						int srcWidth  = (int)(img.getWidth()  * zoomFactor);
//						int srcHeight = (int)(img.getHeight() * zoomFactor);
//						int srcDx = (img.getWidth() - srcWidth) / 2;
// 						int srcDy = (img.getHeight() - srcHeight) / 2;
//						System.out.println("z " + srcWidth + " x " + srcHeight + " o " + srcDx + " " + srcDy);
//						g.drawImage(img, 
//								0,     0, config.outputWidth-1, config.outputHeight-1, 
//								srcDx, srcDy, srcDx + srcWidth,             srcDy + srcHeight, 
//								null);
//					} else {
//						FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
//								.outputWidth (200)
//								.realRange   (config.minReal,      config.maxReal)
//								.outputHeight(200)
//								.imgRange    (config.minImaginary, config.maxImaginary)
//								.build();
//						int srcWidth  = (int)(img.getWidth()  / zoomFactor);
//						int srcHeight = (int)(img.getHeight() / zoomFactor);
//						int srcDx = (img.getWidth() - srcWidth) / 2;
// 						int srcDy = (img.getHeight() - srcHeight) / 2;
// 						g.setColor(Color.BLACK);
// 						g.fillRect(0, 0, config.outputWidth, config.outputHeight);
//						g.drawImage(img, 
//								srcDx, srcDy, srcDx + srcWidth,             srcDy + srcHeight, 
//								0, 0, img.getWidth()-1, img.getHeight()-1, null);
//					}
//				    g.dispose();
//				    imgX = 0;
//					imgY = 0;
//					BufferedImagePool.instance.free(img);
//				    img = img3;
//				    break;
//				}
//				case LOW_DEF: {
//					// BufferedImage img3 = new BufferedImage(config.outputWidth, config.outputHeight, BufferedImage.TYPE_INT_RGB);
//					BufferedImage img3 = BufferedImagePool.instance.get(config.outputWidth, config.outputHeight);
//					Graphics2D g = img3.createGraphics();
//					if (zoomFactor < 1) {  
//						int srcWidth  = (int)(img.getWidth()  * zoomFactor);
//						int srcHeight = (int)(img.getHeight() * zoomFactor);
//						int srcDx = (img.getWidth() - srcWidth) / 2;
// 						int srcDy = (img.getHeight() - srcHeight) / 2;
//						System.out.println("z " + srcWidth + " x " + srcHeight + " o " + srcDx + " " + srcDy);
//						g.drawImage(img, 
//								0,     0, config.outputWidth-1, config.outputHeight-1, 
//								srcDx, srcDy, srcDx + srcWidth,             srcDy + srcHeight, 
//								null);
//					} else {
//						FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
//								.outputWidth (200)
//								.realRange   (config.minReal,      config.maxReal)
//								.outputHeight(200)
//								.imgRange    (config.minImaginary, config.maxImaginary)
//								.build();
//						BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
//						g.drawImage(img2, 
//								0,0, config.outputWidth-1, config.outputHeight-1, 
//								0, 0, img2.getWidth()-1, img2.getHeight()-1, null);
//						int srcWidth  = (int)(img.getWidth()  / zoomFactor);
//						int srcHeight = (int)(img.getHeight() / zoomFactor);
//						int srcDx = (img.getWidth() - srcWidth) / 2;
// 						int srcDy = (img.getHeight() - srcHeight) / 2;
//						g.drawImage(img, 
//								srcDx, srcDy, srcDx + srcWidth,             srcDy + srcHeight, 
//								0, 0, img.getWidth()-1, img.getHeight()-1, null);
//						BufferedImagePool.instance.free(img2);
//					}
//				    g.dispose();
//				    imgX = 0;
//					imgY = 0;
//					BufferedImagePool.instance.free(img);
//				    img = img3;
//				    break;
//				}				
//				case FILL_MISSING: {
//					// BufferedImage img3 = new BufferedImage(config.outputWidth, config.outputHeight, BufferedImage.TYPE_INT_RGB);
//					BufferedImage img3 = BufferedImagePool.instance.get(config.outputWidth, config.outputHeight);
//					Graphics2D g = img3.createGraphics();
//					if (zoomFactor < 1) {  
//						int srcWidth  = (int)(img.getWidth()  * zoomFactor);
//						int srcHeight = (int)(img.getHeight() * zoomFactor);
//						int srcDx = (img.getWidth() - srcWidth) / 2;
// 						int srcDy = (img.getHeight() - srcHeight) / 2;
//						System.out.println("z " + srcWidth + " x " + srcHeight + " o " + srcDx + " " + srcDy);
////						int    sz   = 200;     
////						double rR   = config.xStep * sz;
////						double minR = 
////						FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
////								.outputWidth (200)
////								.realRange   (config.minReal,      config.maxReal)
////								.outputHeight(200)
////								.imgRange    (config.minImaginary, config.maxImaginary)
////								.build();
////						BufferedImage img2 = new FractaleRenderEngine().generateFractaleImage(nc, f, c);
////						//					g.setColor(Color.RED);
////						//					g.fillRect(0, 0, config.outputWidth, config.outputHeight);
////						g.drawImage(img2, 
////								0,0, config.outputWidth-1, config.outputHeight-1, 
////								0, 0, img2.getWidth()-1, img2.getHeight()-1, null);
//						g.drawImage(img, 
//								0,     0, config.outputWidth-1, config.outputHeight-1, 
//								srcDx, srcDy, srcDx + srcWidth,             srcDy + srcHeight, 
////								0,     0, config.outputWidth-1, config.outputHeight-1, 
//								null);
////						g.drawImage(img, 0, 0, null);
//					} else {
//						FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
//								.outputWidth (200)
//								.realRange   (config.minReal,      config.maxReal)
//								.outputHeight(200)
//								.imgRange    (config.minImaginary, config.maxImaginary)
//								.build();
//						BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
//						//					g.setColor(Color.RED);
//						//					g.fillRect(0, 0, config.outputWidth, config.outputHeight);
//						g.drawImage(img2, 
//								0,0, config.outputWidth-1, config.outputHeight-1, 
//								0, 0, img2.getWidth()-1, img2.getHeight()-1, null);
//						int srcWidth  = (int)(img.getWidth()  / zoomFactor);
//						int srcHeight = (int)(img.getHeight() / zoomFactor);
//						int srcDx = (img.getWidth() - srcWidth) / 2;
// 						int srcDy = (img.getHeight() - srcHeight) / 2;
//						g.drawImage(img, 
//								srcDx, srcDy, srcDx + srcWidth,             srcDy + srcHeight, 
//								0, 0, img.getWidth()-1, img.getHeight()-1, null);
//						BufferedImagePool.instance.free(img2);
////						g.drawImage(img, 0, 0, null);
//					}
//				    g.dispose();
//				    imgX = 0;
//					imgY = 0;
//					BufferedImagePool.instance.free(img);
//				    img = img3;
//				}
//
//				}
//				
//				asynchUpdate();
////				if (e.getWheelRotation() > 0)
////					zoom *= zoomAmount * e.getWheelRotation();
////				else
////					zoom /= zoomAmount * -e.getWheelRotation();
//				
//				
//				
//				repaint();
//				
////				redraw();
//			}
//		});
//		
//		addMouseMotionListener(
//				new MouseMotionListener() {
//					int lastX;
//					int lastY;
//					
//					@Override
//					public void mouseDragged(MouseEvent arg0) {
//						if (true) return;
//						int wdx = arg0.getX() - lastX;
//						int wdy = arg0.getY() - lastY;
//						System.out.println("Je suis dragué  " + wdx + " x " + wdy);
//						// update drawing
//						double wfdx = config.xStep * wdx;
//						double wfdy = config.yStep * wdy;
//						// redraw();
//						
//						
//						
//						config.maxReal -= wfdx;
//						config.minReal -= wfdx;
//						config.maxImaginary -= wfdy;
//						config.minImaginary -= wfdy;
//						switch (tMode) {
//						case FLICKING: {
//							imgX += wdx;
//							imgY += wdy;
//						    break;
//						}
//						case LOW_DEF:					{
//							FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
//									.outputWidth (200)
//									.realRange   (config.minReal,      config.maxReal)
//									.outputHeight(200)
//									.imgRange    (config.minImaginary, config.maxImaginary)
//									.build();
//							BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
//							BufferedImage img3 = BufferedImagePool.instance.get(config.outputWidth, config.outputHeight);
//							Graphics2D g = img3.createGraphics();
//							g.drawImage(img2, 0,0, config.outputWidth, config.outputHeight, 0, 0, img2.getWidth(), img2.getHeight(), null);
////							g.drawImage(img, wdx, wdy, null);
//							g.dispose();
//							imgX = 0;
//							imgY = 0;
//							img = img3;
//							BufferedImagePool.instance.free(img2);
//							BufferedImagePool.instance.free(img);
//							break;
//						}
//						case FILL_MISSING: {
//							BufferedImage img3 = BufferedImagePool.instance.get(config.outputWidth, config.outputHeight);
//							
//							Graphics2D g = img3.createGraphics();
//							g.setColor(Color.RED);
//							g.fillRect(0, 0, config.outputWidth, config.outputHeight);
//							
//							List<BufferedImage> allocImages = new ArrayList<>();
//							
//							if (wdy > 0) {
//								FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
//										.outputWidth (config.outputWidth)
//										.realRange   (config.minReal,      config.maxReal)
//										.outputHeight(wdy+1)
//										.imgRange    (config.minImaginary, config.minImaginary + wfdy)
//										.build();
//								BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
//								allocImages.add(img2);
//								g.drawImage(img2, 0, 0, null);
//							} else if (wdy < 0) {
//								FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
//										.outputWidth (config.outputWidth)
//										.realRange   (config.minReal,      config.maxReal)
//										.outputHeight(-wdy+1)
//										.imgRange    (config.maxImaginary + wfdy, config.maxImaginary)
//										.build();
//								BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
//								allocImages.add(img2);								
//								g.drawImage(img2, 0, config.outputHeight + wdy , null);
//							}
//							if (wdx > 0) {
//								FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
//										.outputHeight(config.outputHeight)
//										.imgRange    (config.minImaginary, config.maxImaginary)
//										.outputWidth (wdx+1)
//										.realRange   (config.minReal,      config.minReal + wfdx)
//										.build();
//								BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
//								allocImages.add(img2);								
//								g.drawImage(img2, 0, 0, null);
//							} else if (wdx < 0) {
//								FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
//										.outputHeight(config.outputHeight)
//										.imgRange    (config.minImaginary, config.maxImaginary)
//										.outputWidth (-wdx+1)
//										.realRange   (config.maxReal + wfdx,      config.maxReal)
//										.build();
//								BufferedImage img2 = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(nc, f, c);
//								allocImages.add(img2);								
//								g.drawImage(img2, config.outputWidth + wdx, 0, null);
//							}
//							
//							imgY = 0;
//							imgX = 0;
//							g.drawImage(img,  wdx, wdy, null);
//							g.dispose();
//							for (BufferedImage i : allocImages)
//								BufferedImagePool.instance.free(i);
//							BufferedImagePool.instance.free(img);
//							img = img3;
//							break;
//						}
//						}
//						
//						repaint();
//						asynchUpdate();
//						lastX = arg0.getX();
//						lastY = arg0.getY();
//					}
//					@Override
//					public void mouseMoved(MouseEvent arg0) {
//						lastX=arg0.getX();
//						lastY=arg0.getY();
//					} 
//				}
//		);
	}
	
	private void asynchUpdate() {
		if (calculEnCours != null) {
//			calculEnCours.stop();
			calculEnCours.interrupt();
			calculEnCours = null;
		}
//		calculEnCours = new Thread(() -> {
//			BufferedImage img2 = new FractaleRenderEngine().generateFractaleImage(config, f, c);
//			imgX = 0;
//			imgY = 0;
//			img = img2;
//			repaint();
//		});
//		calculEnCours.start();
		calculEnCours = new AsynchFractalEngine();
		calculEnCours.runAndDo(config, f, c, img2 -> {
			imgX = 0;
			imgY = 0;
			BufferedImagePool.instance.free(img);
			img  = img2;
			repaint();
		});
	}

	public void paintComponent(Graphics g) {
		System.out.println("paint");
		int w = img.getWidth();
		int h = img.getHeight();
		int fw = w;
		int fh = h;
//		if (this.squareRendering) { 
		switch (wfm) { 
		case MINFIT :
			fw = fh = Math.min(w, h);
			break;
		case MAXFIT : 
			fw = fh = Math.max(w, h);
//			fw = w;
//			fh = h;
			break;
		case FILL : 
			fw = w;
			fh = h;
			break;
		}
		
//		}
//			if (w > h) { 
//				config.setOutputSize(h, h);
//				g.clearRect(0, 0, w, h);
//				this.img = App.generateFractaleImage(config, f, c);
//			} else { 
//				config.setOutputSize(w, w);
//				g.clearRect(0, 0, w, h);
//				this.img = App.generateFractaleImage(config, f, c);
//			}
//			if (w > h) {
//				config.setOutputSize(h, h);
//				g.clearRect(0, 0, w, h);
//				this.img = App.generateFractaleImage(config, f, c);
//			} else if (h > w) { 
//				config.setOutputSize(w, w);
//				g.clearRect(0, 0, w, h);
//				this.img = App.generateFractaleImage(config, f, c);
//			}
//		} else { 

//			if (img == null || fw != img.getWidth() || fh != img.getHeight()) {
//				config.setOutputSize(fw, fh);
//				this.img = new FractaleRenderEngine(FractaleRenderEngine.executorServiceInstance).generateFractaleImage(config, f, c);
//			}
//		}
		
		Graphics2D g_ = (Graphics2D)g;
		g_.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		//	    g_.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g_.setRenderingHint(RenderingHints.KEY_RENDERING	, RenderingHints.VALUE_RENDER_QUALITY);
		g_.setRenderingHint(RenderingHints.KEY_ANTIALIASING,	RenderingHints.VALUE_ANTIALIAS_ON); 
		g_.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		//g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
//		g.drawImage(img, (getWidth() - fw)/2, (getHeight() - fh)/2, null);
//		g.clearRect(0, 0, w, h);
		super.paintComponent(g);
		int idx = imgX + (getWidth() - fw)/2;
		int idy = imgY + (getHeight() - fh)/2;
		System.out.println("repaint " + idx + " " + idy + " " + img);
		g.drawImage(img, idx, idy, null);
		g.setColor(Color.WHITE);
		FractaleRenderEngine.information(g_, config, f);	
		//g.drawString("FONCTION ZOOM COORDONNEES", 100,100);
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
//		img = null;
//		repaint();	
		asynchUpdate();
	}
	
	public void setTransitionMode(TransitionMode tm) {
		// tMode = tm;
		switch (tm) {
		case FLICKING: ehd.setDelegate(new EventHandlerFlicking()); break;
		case LOW_RES: ehd.setDelegate(new EventHandlerLowRes()); break;
		case LOW_RES_COMPOSITE:  ehd.setDelegate(new EventHandlerLowResComposite()); break;
		case FILL_MISSING:  ehd.setDelegate(new EventHandlerFillMissing()); break;
		}
		redraw();
	}

//	public void setSquareRendering(boolean selected) {
//		System.out.println("Square rendering : " + selected);
//		this.squareRendering = selected;
//	}


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
//			config.setOutputSize(w, h);
//			if (w > h)
//				config.scale(1, ((double)h) / w);
//			else
//				config.scale(1, ((double)w) / h);
			break;
		case FILL : 
			config.setOutputSize(w, h);
			break;
		}
		redraw();
	}
	
//	private FractaleRenderConfig getCurrentConfig() {
//		switch (wfm) { 
//		case MINFIT :
//			return new FractaleRenderConfig.Builder()
//					.maxIterations(this.refConfig.maxIterations)
//					.outputWidth  (Math.min(this.refConfig.outputWidth, this.refConfig.outputHeight))
//					.outputHeight (Math.min(this.refConfig.outputWidth, this.refConfig.outputHeight))
//					.realRange    (this.refConfig.minReal,      this.refConfig.maxReal)
//					.imgRange     (this.refConfig.minImaginary, this.refConfig.maxImaginary)
//					.build();
//		case MAXFIT :
//			return new FractaleRenderConfig.Builder()
//					.maxIterations(this.refConfig.maxIterations)
//					.outputWidth  (Math.max(this.refConfig.outputWidth, this.refConfig.outputHeight))
//					.outputHeight (Math.max(this.refConfig.outputWidth, this.refConfig.outputHeight))
//					.realRange    (this.refConfig.minReal,      this.refConfig.maxReal)
//					.imgRange     (this.refConfig.minImaginary, this.refConfig.maxImaginary)
//					.build();
//		case FILL : 
//			return new FractaleRenderConfig.Builder()
//					.maxIterations(this.refConfig.maxIterations)
//					.outputWidth  (this.refConfig.outputWidth)
//					.outputHeight (this.refConfig.outputHeight)
//					.realRange    (this.refConfig.minReal,      this.refConfig.maxReal)
//					.imgRange     (this.refConfig.minImaginary, this.refConfig.maxImaginary)
//					.build();
//		default: throw new RuntimeException("unknown fill mode " + wfm);
//		}
//	}
	public double scaleFromMouseWheel(int wheelRotations) {
		return Math.pow(this.zoomSpeed, wheelRotations);
	}
	

}