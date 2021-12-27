package fractale;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.swing.JPanel;

// 



class ImagePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	BufferedImage img;
	FractaleRenderConfig config;
	JolieFonction f;
	BiFunction<FractaleRenderConfig, Integer, Color> c;
	WindowFitMode wfm;
	double zoom;
	
	public ImagePanel(FractaleRenderConfig config, JolieFonction  f, BiFunction<FractaleRenderConfig, Integer, Color> c ) { 
		this.img = FractaleRenderEngine.generateFractaleImage(config, f, c);
		this.config = config;
		this.f = f;
		this.c = c;
		this.zoom = 1;
		addMouseWheelListener(e -> {
			if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
				final double zoomAmount = 1.1;
				if (e.getWheelRotation() > 0)
					zoom *= zoomAmount * e.getWheelRotation();
				else
					zoom /= zoomAmount * -e.getWheelRotation();
				double zv = zoom; // 1 - zoom;
				System.out.println("wheel : " + e.getWheelRotation() + " current zoom : " + zoom);
				this.config .minReal  = - zv; // += (0.1 * e.getWheelRotation());
				this.config .maxReal  =   zv; //(0.1 * e.getWheelRotation());
				this.config .minImaginary = - zv; //(0.1 * e.getWheelRotation());
				this.config .maxImaginary = zv; // (0.1 * e.getWheelRotation());
				redraw();
			}
		});
	}
	


	public void paintComponent(Graphics g) {
		int w = getWidth(); // * 3;
		int h = getHeight(); // * 3;
		int fw = w;
		int fh = h;
//		if (this.squareRendering) { 
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

			if (img == null || fw != img.getWidth() || fh != img.getHeight()) {
				config.setOutputSize(fw, fh);
				g.clearRect(0, 0, w, h);
				this.img = FractaleRenderEngine.generateFractaleImage(config, f, c);
			}
//		}
		
		Graphics2D g_ = (Graphics2D)g;
		g_.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		//	    g_.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g_.setRenderingHint(RenderingHints.KEY_RENDERING	, RenderingHints.VALUE_RENDER_QUALITY);
		g_.setRenderingHint(RenderingHints.KEY_ANTIALIASING,	RenderingHints.VALUE_ANTIALIAS_ON); 
		g_.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		//g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
		g.drawImage(img, (getWidth() - fw)/2, (getHeight() - fh)/2, null);

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
		img = null;
		repaint();		
	}
	

//	public void setSquareRendering(boolean selected) {
//		System.out.println("Square rendering : " + selected);
//		this.squareRendering = selected;
//	}


	public void setWindowFitMode(WindowFitMode wfm) {
		this.wfm = wfm;
		img = null;
		repaint();
	}

}