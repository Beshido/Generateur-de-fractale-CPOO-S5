package fractale;



public class FractaleRenderConfig {
	public static class Builder {
	
//	int outputWidth;
//	int outputHeight;
//	double minReal;
//	double maxReal;
//	double minImaginary;
//	double maxImaginary;
//	double xStep;
//	double yStep;
//	int maxIterations = 1000;

		private FractaleRenderConfig c = new FractaleRenderConfig();
		
		Builder() {
			c = new FractaleRenderConfig();
		}

		public Builder outputWidth(int value) {
			c.outputWidth = value;
			return this;
		}

		public Builder outputHeight(int value) {
			c.outputHeight = value;
			return this;
		}
		public Builder realRange (double minr, double maxr) { 
			c.minReal = minr;
			c.maxReal = maxr;
			return this;
		}
		public Builder imgRange (double mini, double maxi) { 
			c.minImaginary = mini;
			c.maxImaginary = maxi;
			return this;
		}
		public FractaleRenderConfig build() {
			c.setOutputSize(c.outputWidth, c.outputHeight);
			return c;
		}

	}
	
	int outputWidth;
	int outputHeight;
	double minReal;
	double maxReal;
	double minImaginary;
	double maxImaginary;
	double xStep;
	double yStep;
	int maxIterations = 1000;
	
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

	public void ranges(double minR, double maxR, double minI, double maxI) {
		this.minReal = minR;
		this.maxReal = maxR;
		this.minImaginary = minI;
		this.maxImaginary = maxI;
		setOutputSize(outputWidth, outputHeight);
	}
	
	
}
