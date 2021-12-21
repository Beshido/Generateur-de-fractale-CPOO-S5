package fractale;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
	Function<Complex,Complex> f;
	BiFunction<FractaleRenderConfig, Integer, Color> c;
	WindowFitMode wfm;

	public ImagePanel(FractaleRenderConfig config, Function<Complex,Complex> f, BiFunction<FractaleRenderConfig, Integer, Color> c ) { 
		this.img = App.generateFractaleImage(config, f, c);
		this.config = config;
		this.f = f;
		this.c = c;
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
				this.img = App.generateFractaleImage(config, f, c);
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


	}


	public void setColorScheme(ColorScheme colorScheme) {
		this.c = colorScheme.f;
		img = null;
		repaint();
	}


	public void setFunction(Function<Complex, Complex> f2) {
		this.f = f2;
		img = null;
		repaint();
	}


	public void setIterations(int iterations) {
		this.config.maxIterations = iterations;
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