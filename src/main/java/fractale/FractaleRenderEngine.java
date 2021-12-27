package fractale;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Function;

public class FractaleRenderEngine {

	static class PixelRowComputation {
		int j;
		PixelRowComputation(int j) { this.j = j; }
	}

	static class PixelComputation {
		int i,j; // in
		int iterations; // out
		public PixelComputation(int i, int j) {
			this.i = i; this.j = j;
		}
	}

	public static int divergenceIndex (FractaleRenderConfig cfg, JolieFonction f, Complex c) { 
		double RADIUS=2.;
		int ite = 0; 
		Complex zn = c;
		// sortie de boucle si divergence
		while (ite < cfg.maxIterations-1 && zn.getModule() <= RADIUS) { 
			zn = f.getFunction().apply(zn);
			ite++;
		}
		return ite;
	}

	public BufferedImage generateFractaleImage(FractaleRenderConfig config, JolieFonction f, BiFunction<FractaleRenderConfig, Integer, Color> c) {
		//		int outputWidth = 1001;
		//		int outputHeight = 1001;
		//		double minReal = -1;
		//		double maxReal = 1;
		//		double minImaginary = -1;
		//		double maxImaginary = 1;
		//		double xStep = (maxReal - minReal) / (outputWidth-1);
		//		double yStep = (maxImaginary - minImaginary) / (outputHeight-1);
		//        int xStepCount;
		//        int yStepCount;
		long start = System.currentTimeMillis();
		System.out.println("generating fractal image " + config.outputWidth + "x" + config.outputHeight);
		BufferedImage img = new BufferedImage(config.outputWidth, config.outputHeight, BufferedImage.TYPE_INT_RGB);
	
		//		for (int i=0; i< config.outputWidth; i++) { 
		//			for (int j = 0; j< config.outputHeight; j++) { 
		//				int ite = divergenceIndex(f, new Complex(config.minReal + i * config.xStep, config.minImaginary + j * config.yStep));
		////				int r = 64; int g = 224; int b = 208; //turquoise
		////				int col = ite * 317; //(r << 16) | (g << 8) | b;
		////				Color color = Color.RED;
		////				Color color = c0(ite);
		//				Color color = c.apply(ite);
		//				int col = color.getRGB();
		//				img.setRGB(i, j, col);
		//			}
		//		}
		//		List<PixelComputation> comps = new ArrayList<>(config.outputWidth * config.outputHeight);
		//		for (int i=0; i< config.outputWidth; i++) 
		//			for (int j = 0; j< config.outputHeight; j++) 
		//				comps.add(new PixelComputation(i,j));
		//		comps.parallelStream()
		//			.forEach(pc -> {
		//				int ite = divergenceIndex(f, new Complex(config.minReal + pc.i * config.xStep, config.minImaginary + pc.j * config.yStep));
		//				Color color = c.apply(ite);
		//				int col = color.getRGB();
		//				img.setRGB(pc.i, pc.j, col);
		//			});
	
		List<FractaleRenderEngine.PixelRowComputation> comps = new ArrayList<>(config.outputHeight);
		for (int j = 0; j< config.outputHeight; j++) 
			comps.add(new FractaleRenderEngine.PixelRowComputation(j));
		comps.stream() // parallelStream()
		.forEach(pc -> {
			for (int i=0; i< config.outputWidth; i++) {
				int ite = divergenceIndex(config, f , new Complex(config.minReal + i * config.xStep, config.minImaginary + pc.j * config.yStep));
				Color color = c.apply(config, ite);
				int col = color.getRGB();
				img.setRGB(i, pc.j, col);
			}
		});
	
		//		Stack<PixelComputation> pcs = new Stack<>(); //config.outputWidth * config.outputHeight);
		//		for (int i=0; i< config.outputWidth; i++) 
		//			for (int j = 0; j< config.outputHeight; j++) 
		//				pcs.push(new PixelComputation(i,j));
		//
		//		List<Thread> threads = new ArrayList<>();
		//		for (int i=0; i<16; i++) {
		//			Thread t = new Thread(() -> {
		//				PixelComputation pc;
		//				while (true) {
		//					synchronized (pcs) {
		//						if (pcs.isEmpty())
		//							return;
		//						pc = pcs.pop();
		//					}
		//					int ite = divergenceIndex(f, new Complex(config.minReal + pc.i * config.xStep, config.minImaginary + pc.j * config.yStep));
		//					Color color = c.apply(ite);
		//					int col = color.getRGB();
		//					img.setRGB(pc.i, pc.j, col);
		//				}
		//			});
		//			t.start();
		//			threads.add(t);
		//		}
		//		System.out.println("-- thread setup : " + (System.currentTimeMillis() - start) + "ms");
		//
		//		for (Thread t : threads) {
		//			try {
		//				t.join();
		//			} catch (InterruptedException e) {
		//				e.printStackTrace();
		//			}
		//		}
		
//		Stack<Integer> pcs = new Stack<>(); //config.outputWidth * config.outputHeight);
//		for (int j = 0; j< config.outputHeight; j++) 
//			pcs.push(j);
//		
//		List<Thread> threads = new ArrayList<>();
//		for (int i=0; i<16; i++) {
//					Thread t = new Thread(() -> {
//						PixelComputation pc;
//						while (true) {
//							synchronized (pcs) {
//								if (pcs.isEmpty())
//									return;
//								pc = pcs.pop();
//							}
//							int ite = divergenceIndex(f, new Complex(config.minReal + pc.i * config.xStep, config.minImaginary + pc.j * config.yStep));
//							Color color = c.apply(ite);
//							int col = color.getRGB();
//							img.setRGB(pc.i, pc.j, col);
//						}
//					});
//					t.start();
//					threads.add(t);
//				}
//				System.out.println("-- thread setup : " + (System.currentTimeMillis() - start) + "ms");
//		
//				for (Thread t : threads) {
//					try {
//						t.join();
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
	
		//		System.out.println("stack " + pcs.size());
		//		BufferedImage dbi = null;
		//		dbi = new BufferedImage(1001, 1001, img.getType());
		//	    Graphics2D g = dbi.createGraphics();
		////	    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		//	    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		//	    g.setRenderingHint(RenderingHints.KEY_RENDERING	, RenderingHints.VALUE_RENDER_QUALITY);
		//	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,	RenderingHints.VALUE_ANTIALIAS_ON); 
		//	    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		////	    AffineTransform at = AffineTransform.getScaleInstance(1/3.0, 1/3.0);
		////	    g.drawRenderedImage(img, at);
		//	    g.drawImage(img, 0, 0, 1001, 1001, null);
		//        g.dispose();
		//
		//	    img = dbi;
	    Graphics2D g = img.createGraphics();
//	    information(g, config, f);
	    g.dispose();
		System.out.println("-- generated fractal image " + config.outputWidth + "x" + config.outputHeight + " : " + (System.currentTimeMillis() - start) + "ms");
		return img;
	}

	//	MAX_ITER=1000; RADIUS=2.;
	//	int divergenceIndex(Complex z0) {
	//	int ite = 0; Complex zn = z0;
	//	// sortie de boucle si divergence
	//	while (ite < MAX_ITER-1 && |zn| <= RADIUS)
	//	zn = f(zn); ite++;
	//	return ite;
	//	}
//	public static int divergenceIndex (Complex c) { 
//		int MAX_ITER=1000;
//		double RADIUS=2.;
//		int ite = 0; 
//		Complex zn = c;
//		// sortie de boucle si divergence
//		while (ite < MAX_ITER-1 && zn.getModule() <= RADIUS) { 
//			zn = App.f0(zn);
//			ite++;
//		}
//		return ite;
//	}
	public static void information(Graphics2D g, FractaleRenderConfig config, JolieFonction f) { 
	    String text = "";
	    if (f.getName() == null) 
	    	text = "f(z) = " + f.getDefinition();
	    else 
	    	text = f.getName() + " (f(z) = " + f.getDefinition() + ")";
//	    text += "  xmin : " + config.minReal + " xmax : " + config.maxReal + " ymin : " + config.minImaginary +" y : " + config.maxImaginary;   
	    text += String.format("   x [%.2f .. %.2f]  y [%.2f .. %.2f]", 
	    		 config.minReal,
	    		 config.maxReal,
	    		 config.minImaginary,
	    		 config.maxImaginary);
	    text +=  String.format("  pixels : %dx%d ", config.outputWidth, config.outputHeight);
	    g.drawString(text , 20, 20);

	}
	
}
