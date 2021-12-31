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

/** 
 *Generation de la fractale "gros calcul".
 */
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
		long start = System.currentTimeMillis();
		System.out.println("generating fractal image " + config.outputWidth + "x" + config.outputHeight);
		BufferedImage img = BufferedImagePool.instance.get(config.outputWidth, config.outputHeight);
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

		if (interrupted) {
			BufferedImagePool.instance.free(img);
			return null;
		}

		System.out.println("-- generated fractal image " + config.outputWidth + "x" + config.outputHeight + " : " + (System.currentTimeMillis() - start) + "ms");
		return img;
	}

	/**
	 * Affichage des informations de la fractale.
	 * @param g      le graphic ou sont affichees les informations.
	 * @param config la config de la fractale.
	 * @param f	     la fonction de la fractale.
	 */
	public static void information(Graphics2D g, FractaleRenderConfig config, JolieFonction f) { 
	    String text = "";
	    if (f.getName() == null) 
	    	text = "f(z) = " + f.getDefinition();
	    else 
	    	text = f.getName() + " (f(z) = " + f.getDefinition() + ")";
	    text += String.format("   x [%.2f .. %.2f]  y [%.2f .. %.2f]", 
	    		 config.minReal,
	    		 config.maxReal,
	    		 config.minImaginary,
	    		 config.maxImaginary);
	    text +=  String.format("  pixels : %dx%d ", config.outputWidth, config.outputHeight);
	    g.drawString(text , 20, 20);

	}
	
}
