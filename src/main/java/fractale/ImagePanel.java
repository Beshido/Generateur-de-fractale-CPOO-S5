package fractale;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
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
	
	int imgX;
	int imgY;
	Thread calculEnCours;
	
	BufferedImage img;
	FractaleRenderConfig config;
	JolieFonction f;
	BiFunction<FractaleRenderConfig, Integer, Color> c;
	WindowFitMode wfm;
	double zoom;
	
	public ImagePanel(FractaleRenderConfig config, JolieFonction  f, BiFunction<FractaleRenderConfig, Integer, Color> c ) { 
		this.img = new FractaleRenderEngine().generateFractaleImage(config, f, c);
		
		this.imgX = this.imgY = 0;
		this.calculEnCours = null;
		
		this.config = config;
		this.f = f;
		this.c = c;
		this.zoom = 1;
		setOpaque(true);
		setBackground(Color.BLACK);
		
		addMouseWheelListener(e -> {
			if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
				final double zoomAmount = 1.1;
				double zoomFactor = zoomAmount * e.getWheelRotation();
				if (e.getWheelRotation() < 0)
					zoomFactor = - 1 / zoomFactor;
				zoom *= zoomFactor;
				double zv = zoom; // 1 - zoom;
				System.out.println("wheel : " + e.getWheelRotation() + " current zoom : " + zoom + " zf " + zoomFactor);
				double fcx = (config.maxReal + config.minReal)/2;
				double fcy = (config.maxImaginary + config.minImaginary)/2;
				this.config .minReal      = fcx - zv; // += (0.1 * e.getWheelRotation());
				this.config .maxReal      = fcx + zv; //(0.1 * e.getWheelRotation());
				this.config .minImaginary = fcy - zv; //(0.1 * e.getWheelRotation());
				this.config .maxImaginary = fcy + zv; // (0.1 * e.getWheelRotation());
				this.config.ranges(fcx - zv, fcx + zv, fcy - zv, fcy + zv);
				{
					BufferedImage img3 = new BufferedImage(config.outputWidth, config.outputHeight, BufferedImage.TYPE_INT_RGB);
					Graphics2D g = img3.createGraphics();
					if (zoomFactor < 1) {  
						int srcWidth  = (int)(img.getWidth()  * zoomFactor);
						int srcHeight = (int)(img.getHeight() * zoomFactor);
						int srcDx = (img.getWidth() - srcWidth) / 2;
 						int srcDy = (img.getHeight() - srcHeight) / 2;
						System.out.println("z " + srcWidth + " x " + srcHeight + " o " + srcDx + " " + srcDy);
//						FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
//								.outputWidth (200)
//								.realRange   (config.minReal,      config.maxReal)
//								.outputHeight(200)
//								.imgRange    (config.minImaginary, config.maxImaginary)
//								.build();
//						BufferedImage img2 = new FractaleRenderEngine().generateFractaleImage(nc, f, c);
//						//					g.setColor(Color.RED);
//						//					g.fillRect(0, 0, config.outputWidth, config.outputHeight);
//						g.drawImage(img2, 
//								0,0, config.outputWidth-1, config.outputHeight-1, 
//								0, 0, img2.getWidth()-1, img2.getHeight()-1, null);
						g.drawImage(img, 
								0,     0, config.outputWidth-1, config.outputHeight-1, 
								srcDx, srcDy, srcDx + srcWidth,             srcDy + srcHeight, 
//								0,     0, config.outputWidth-1, config.outputHeight-1, 
								null);
//						g.drawImage(img, 0, 0, null);
					} else {
						FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
								.outputWidth (200)
								.realRange   (config.minReal,      config.maxReal)
								.outputHeight(200)
								.imgRange    (config.minImaginary, config.maxImaginary)
								.build();
						BufferedImage img2 = new FractaleRenderEngine().generateFractaleImage(nc, f, c);
						//					g.setColor(Color.RED);
						//					g.fillRect(0, 0, config.outputWidth, config.outputHeight);
						g.drawImage(img2, 
								0,0, config.outputWidth-1, config.outputHeight-1, 
								0, 0, img2.getWidth()-1, img2.getHeight()-1, null);
						int srcWidth  = (int)(img.getWidth()  / zoomFactor);
						int srcHeight = (int)(img.getHeight() / zoomFactor);
						int srcDx = (img.getWidth() - srcWidth) / 2;
 						int srcDy = (img.getHeight() - srcHeight) / 2;
						g.drawImage(img, 
								srcDx, srcDy, srcDx + srcWidth,             srcDy + srcHeight, 
								0, 0, img.getWidth()-1, img.getHeight()-1, null);
//						g.drawImage(img, 0, 0, null);
					}
				    g.dispose();
				    imgX = 0;
					imgY = 0;
				    img = img3;
				}
				
				asynchUpdate();
//				if (e.getWheelRotation() > 0)
//					zoom *= zoomAmount * e.getWheelRotation();
//				else
//					zoom /= zoomAmount * -e.getWheelRotation();
				
				
				
				repaint();
				
//				redraw();
			}
		});
		
		addMouseMotionListener(
				new MouseMotionListener() {
					int lastX;
					int lastY;
					
					@Override
					public void mouseDragged(MouseEvent arg0) {
						int wdx = arg0.getX() - lastX;
						int wdy = arg0.getY() - lastY;
						System.out.println("Je suis dragué  " + wdx + " x " + wdy);
						// update drawing
						double wfdx = config.xStep * wdx;
						double wfdy = config.yStep * wdy;
						// redraw();
						
						imgX += wdx;
						imgY += wdy;
						
						config.maxReal -= wfdx;
						config.minReal -= wfdx;
						config.maxImaginary -= wfdy;
						config.minImaginary -= wfdy;

//						if (wdy > 0) {
//							FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
//									.outputWidth (getWidth())
//									.realRange   (config.minReal,      config.maxReal)
//									.outputHeight(wdy + 1)
//									.imgRange    (config.minImaginary - wfdy, config.minImaginary)
//									.build();
//							BufferedImage img2 = new FractaleRenderEngine().generateFractaleImage(nc, f, c);
////							{
////								Graphics2D g = img2.createGraphics();
////								g.setColor(Color.RED);
////								g.drawRect(0, 0, config.outputWidth, config.outputHeight);
////							}
//							// imgY -= wdy;
//							imgY = 0;
//							BufferedImage img3 = new BufferedImage(config.outputWidth, config.outputHeight, BufferedImage.TYPE_INT_RGB);
//							Graphics2D g = img3.createGraphics();
//							g.setColor(Color.RED);
//							g.fillRect(0, 0, config.outputWidth, config.outputHeight);
//							g.drawImage(img,  0, wdy, null);
//							g.drawImage(img2, 0, 0, null);
//						    g.dispose();
//						    img = img3;
//						}
						{
							FractaleRenderConfig nc = new FractaleRenderConfig.Builder()
									.outputWidth (200)
									.realRange   (config.minReal,      config.maxReal)
									.outputHeight(200)
									.imgRange    (config.minImaginary, config.maxImaginary)
									.build();
							BufferedImage img2 = new FractaleRenderEngine().generateFractaleImage(nc, f, c);
							BufferedImage img3 = new BufferedImage(config.outputWidth, config.outputHeight, BufferedImage.TYPE_INT_RGB);
							Graphics2D g = img3.createGraphics();
//							g.setColor(Color.RED);
//							g.fillRect(0, 0, config.outputWidth, config.outputHeight);
							g.drawImage(img2, 0,0, config.outputWidth-1, config.outputHeight-1, 0, 0, img2.getWidth()-1, img2.getHeight()-1, null);
							g.drawImage(img, wdx, wdy, null);
						    g.dispose();
						    imgX = 0;
							imgY = 0;
						    img = img3;
						}
						
						repaint();
						

//						if (calculEnCours != null) {
//							calculEnCours.stop();
//							calculEnCours = null;
//						}
//						calculEnCours = new Thread(() -> {
//							BufferedImage img2 = new FractaleRenderEngine().generateFractaleImage(config, f, c);
//							imgX = 0;
//							imgY = 0;
//							img = img2;
//							repaint();
//						});
//						calculEnCours.start();
						asynchUpdate();
						// end
						lastX = arg0.getX();
						lastY = arg0.getY();
					}
					@Override
					public void mouseMoved(MouseEvent arg0) {
						lastX=arg0.getX();
						lastY=arg0.getY();
					} 
				}
		);
	}
	
	private void asynchUpdate() {
		if (calculEnCours != null) {
			calculEnCours.stop();
			calculEnCours = null;
		}
		calculEnCours = new Thread(() -> {
			BufferedImage img2 = new FractaleRenderEngine().generateFractaleImage(config, f, c);
			imgX = 0;
			imgY = 0;
			img = img2;
			repaint();
		});
		calculEnCours.start();
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
				this.img = new FractaleRenderEngine().generateFractaleImage(config, f, c);
			}
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
		System.out.println("repaint " + idx + " " + idy);
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