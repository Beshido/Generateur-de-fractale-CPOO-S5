package fractale;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Consumer;

class AsynchFractalEngine {
	
	private FractaleRenderEngine fre;
	private Thread thread;
	
	private static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	public AsynchFractalEngine() {
		fre = new FractaleRenderEngine(executorService);		
	}
	
	public void runAndDo(FractaleRenderConfig config, JolieFonction f, BiFunction<FractaleRenderConfig, Integer, Color> c, Consumer<BufferedImage> then) {
		thread = new Thread(() -> {
			BufferedImage img = fre.generateFractaleImage(config, f, c);
			if (img != null)
				then.accept(img);
		});
		thread.start();
	}
	
	public void interrupt() {
		fre.interrupted = true;
	}
	
}

//public class FractaleRenderEngine {
//	
//	public static ExecutorService executorServiceInstance = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//	
//	public static int divergenceIndex (FractaleRenderConfig cfg, JolieFonction f, Complex c) { 
//		double RADIUS=2.;
//		int ite = 0; 
//		Complex zn = c;
//		// sortie de boucle si divergence
//		while (ite < cfg.maxIterations-1 && zn.getModule() <= RADIUS) { 
//			zn = f.getFunction().apply(zn);
//			ite++;
//		}
//		return ite;
//	}
//
//	boolean interrupted = false;
//	private ExecutorService executorService;
//	FractaleRenderEngine(ExecutorService executorService) {
//		this.executorService = executorService;
//	}
//	
//	public BufferedImage generateFractaleImage(FractaleRenderConfig config, JolieFonction f, BiFunction<FractaleRenderConfig, Integer, Color> c) {
//		long start = System.currentTimeMillis();
//		System.out.println("generating fractal image " + config.outputWidth + "x" + config.outputHeight);
//		BufferedImage img = BufferedImagePool.instance.get(config.outputWidth, config.outputHeight);
//		List<Callable<Boolean>> lineJobs = new ArrayList<>();
//		final int[] a = ( (DataBufferInt) img.getRaster().getDataBuffer() ).getData();
//		for (int j = 0; j< config.outputHeight; j++) {
//			int y = j;
//			lineJobs.add(() -> {
//				for (int i=0; i< config.outputWidth && ! interrupted; i++) {
//					int ite = divergenceIndex(config, f , new Complex(config.minReal + i * config.xStep, config.minImaginary + y * config.yStep));
//					Color color = c.apply(config, ite);
//					int col = color.getRGB();
////					img.setRGB(i, y, col);
//					a[i + y * config.outputWidth] = col;
//				}
//				return interrupted;
//			});
//		}
//		try {
//			List<Future<Boolean>> completeLineJobs = executorService.invokeAll(lineJobs);
//			for (Future<Boolean> x : completeLineJobs)
//				x.get();
//		} catch (InterruptedException|ExecutionException e) {
//			BufferedImagePool.instance.free(img);
//			return null;
//		}
//		if (interrupted) {
//			BufferedImagePool.instance.free(img);
//			return null;
//		}
//		System.out.println("-- generated fractal image " + config.outputWidth + "x" + config.outputHeight + " : " + (System.currentTimeMillis() - start) + "ms");
//		return img;
//	}
//
//	public static void information(Graphics2D g, FractaleRenderConfig config, JolieFonction f) { 
//	    String text = "";
//	    if (f.getName() == null) 
//	    	text = "f(z) = " + f.getDefinition();
//	    else 
//	    	text = f.getName() + " (f(z) = " + f.getDefinition() + ")";
////	    text += "  xmin : " + config.minReal + " xmax : " + config.maxReal + " ymin : " + config.minImaginary +" y : " + config.maxImaginary;   
//	    text += String.format("   x [%.2f .. %.2f]  y [%.2f .. %.2f]", 
//	    		 config.minReal,
//	    		 config.maxReal,
//	    		 config.minImaginary,
//	    		 config.maxImaginary);
//	    text +=  String.format("  pixels : %dx%d ", config.outputWidth, config.outputHeight);
//	    g.drawString(text , 20, 20);
//
//	}
//	
//}

public class FractaleRenderEngine {
	
	public static ExecutorService executorServiceInstance = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
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

	boolean interrupted = false;
	private ExecutorService executorService;
	FractaleRenderEngine(ExecutorService executorService) {
		this.executorService = executorService;
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
//		BufferedImage img = new BufferedImage(config.outputWidth, config.outputHeight, BufferedImage.TYPE_INT_RGB);
		BufferedImage img = BufferedImagePool.instance.get(config.outputWidth, config.outputHeight);
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
	
//		List<FractaleRenderEngine.PixelRowComputation> comps = new ArrayList<>(config.outputHeight);
//		for (int j = 0; j< config.outputHeight; j++) 
//			comps.add(new FractaleRenderEngine.PixelRowComputation(j));
//		comps.stream() // parallelStream()
//		.forEach(pc -> {
//			for (int i=0; i< config.outputWidth; i++) {
//				int ite = divergenceIndex(config, f , new Complex(config.minReal + i * config.xStep, config.minImaginary + pc.j * config.yStep));
//				Color color = c.apply(config, ite);
//				int col = color.getRGB();
//				img.setRGB(i, pc.j, col);
//			}
//		});
	
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
		
		List<Callable<Boolean>> lineJobs = new ArrayList<>();
		for (int j = 0; j< config.outputHeight; j++) {
			int y = j;
			lineJobs.add(() -> {
				for (int i=0; i< config.outputWidth && ! interrupted; i++) {
					int ite = divergenceIndex(config, f , new Complex(config.minReal + i * config.xStep, config.minImaginary + y * config.yStep));
					Color color = c.apply(config, ite);
					int col = color.getRGB();
					img.setRGB(i, y, col);
				}
				return interrupted;
			});
		}
		try {
			List<Future<Boolean>> completeLineJobs = executorService.invokeAll(lineJobs);
			for (Future<Boolean> x : completeLineJobs) 
				x.get();
		} catch (InterruptedException|ExecutionException e) {
			BufferedImagePool.instance.free(img);
			return null;
		}
//		Stack<Integer> pcs = new Stack<>();
//		for (int j = 0; j< config.outputHeight; j++) 
//			pcs.push(j);
//		
//		int threadCount = Runtime.getRuntime().availableProcessors();
//		List<Thread> threads = new ArrayList<>();
//		for (int ti=0; ti<threadCount; ti++) {
//			Thread t = new Thread(() -> {
//				Integer pc;
//				while (! interrupted) {
//					synchronized (pcs) {
//						if (pcs.isEmpty())
//							return;
//						pc = pcs.pop();
//					}
//					for (int i=0; i< config.outputWidth; i++) {
//						int ite = divergenceIndex(config, f , new Complex(config.minReal + i * config.xStep, config.minImaginary + pc * config.yStep));
//						Color color = c.apply(config, ite);
//						int col = color.getRGB();
//						img.setRGB(i, pc, col);
//					}
//				}
//			});
//			t.start();
//			threads.add(t);
//		}
//		System.out.println("-- thread setup : " + (System.currentTimeMillis() - start) + "ms, using " + threadCount + " threads");
//
//		for (Thread t : threads) {
//			try {
//				t.join();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}

		if (interrupted) {
			BufferedImagePool.instance.free(img);
			return null;
		}
//	    Graphics2D g = img.createGraphics();
////	    information(g, config, f);
//	    g.dispose();
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
