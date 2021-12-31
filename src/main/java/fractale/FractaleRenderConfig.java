package fractale;



public class FractaleRenderConfig {
	
	/**
	 * Builder de FractaleRednerConfig. 
	 * <p>Permet de cr�er des config de g�n�ration de fractale.</p>
	 */
	public static class Builder {

		private FractaleRenderConfig c = new FractaleRenderConfig();
		
		Builder() {
			c = new FractaleRenderConfig();
		}

		//Les fonctions permettant de set up les diff�rents param�tres 
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

		public Builder maxIterations(int maxIterations) {
			c.maxIterations = maxIterations;
			return this;
		}

	}
	
	// Les param�tres d'une config de g�n�ration de fractale.
	int outputWidth;
	int outputHeight;
	double minReal;
	double maxReal;
	double minImaginary;
	double maxImaginary;
	double xStep;
	double yStep;
	int maxIterations;
	
	private FractaleRenderConfig() {
		
	}
	
	/**
	 * Fonction permettant de cr�er une config en ne rentrant que 3 param�tres. 
	 * @param outputSize la largeur et la hauteur de la config. 
	 * @param min le minimum de la partie r�el et imaginaire des nombres complexes. 
	 * @param max le maximum de la partie r�el et imaginaire des nombres complexes. 
	 * @return
	 */
	public static FractaleRenderConfig createSimple(int outputSize, double min, double max) {
		FractaleRenderConfig c = new FractaleRenderConfig();
	    c.outputWidth =	c.outputHeight = outputSize;
	    c.minReal = c.minImaginary = min;
	    c.maxReal = c.maxImaginary = max;
		c.xStep = (c.maxReal - c.minReal) / (c.outputWidth-1);
		c.yStep = (c.maxImaginary - c.minImaginary) / (c.outputHeight-1);
		c.maxIterations = 1000;
		return c;
	}

	/**
	 * Fonction de set up de la largeur et hauteur.
	 * @param w la largeur.
	 * @param h la hauteur.
	 */
	public void setOutputSize(int w, int h) {
		outputWidth = w;
		outputHeight = h;
		updateSteps();
	}

	/**
	 * Fonction de set up du max et min des nombres complexes.
	 * @param minR minimum de la partie r�el.
	 * @param maxR maximum de la partie r�el.
	 * @param minI minimum de la partie imaginaire.
	 * @param maxI maximum de la partie imaginaire.
	 */
	public void ranges(double minR, double maxR, double minI, double maxI) {
		this.minReal = minR;
		this.maxReal = maxR;
		this.minImaginary = minI;
		this.maxImaginary = maxI;
		updateSteps();
	}
	
	/**
	 * Fonction qui recalcule le pas a utilis� en fonction de la hauteur/largeur et 
	 * du min/max des parties r�el et imaginaire entr� en param�tres.
	 */
	public void updateSteps() {
		xStep = (maxReal - minReal) / (outputWidth-1);
		yStep = (maxImaginary - minImaginary) / (outputHeight-1);
	}
	
	/**
	 * Fonction qui re set up les valeurs de minReal, maxReal, minImaginary, maxImaginary lors d'un d�placement.
	 * @param dReal       d�placement sur l'axe des r�els.
	 * @param dImaginary  d�placement sur l'axe des imaginaires.
	 */
	public void move(double dReal, double dImaginary) {
		this.minReal      += dReal;
		this.maxReal      += dReal;
		this.minImaginary += dImaginary;
		this.maxImaginary += dImaginary;
	}
	
	/**
	 * Mise a l'echelle autour du centre courrant.
	 * @param value
	 */
	public void scale(double value) {
		scale(value, value);
	}
	
	/**
	 * Fonction de rescalling lors du zoom/dezoom.
	 * @param realScale
	 * @param imaginaryScale
	 */
	public void scale(double realScale, double imaginaryScale) {
		double cReal      = (maxReal + minReal) / 2;
		double cImaginary = (maxImaginary + minImaginary) / 2;
		double dReal      = (maxReal - cReal) * realScale;
		double dImaginary = (maxImaginary - cImaginary) * imaginaryScale;
		minReal      = cReal - dReal;
		maxReal      = cReal + dReal;
		minImaginary = cImaginary - dImaginary;
		maxImaginary = cImaginary + dImaginary;
		updateSteps();
	}
	
}
