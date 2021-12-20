package fractale;

public class FractaleRenderConfig {
	int outputWidth;
	int outputHeight;
	double minReal;
	double maxReal;
	double minImaginary;
	double maxImaginary;
	double xStep;
	double yStep;
	
	private FractaleRenderConfig() {
		
	}
	
	public static FractaleRenderConfig createSimple(int outputSize, double min, double max) {
		FractaleRenderConfig c = new FractaleRenderConfig();
	    c.outputWidth =	c.outputHeight = outputSize;
	    c.minReal = c.minImaginary = min;
	    c.maxReal = c.maxImaginary = max;
		c.xStep = (c.maxReal - c.minReal) / (c.outputWidth-1);
		c.yStep = (c.maxImaginary - c.minImaginary) / (c.outputHeight-1);
		return c;
	}

	public void setOutputSize(int w, int h) {
		outputWidth = w;
		outputHeight = h;
		xStep = (maxReal - minReal) / (outputWidth-1);
		yStep = (maxImaginary - minImaginary) / (outputHeight-1);
	}
	
}
