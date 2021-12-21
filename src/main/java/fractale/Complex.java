package fractale;

public class Complex {
	
	private final double real;
	private final double imaginary;

	public Complex (double real, double imaginary) { 
		this.real = real;
		this.imaginary = imaginary;
	}
	
	public double getModule () { 
		return Math.sqrt(this.imaginary * this.imaginary + this.real * this.real);	
	}
	
	public double getReal() {
		return real;
	}

	public double getImaginary() {
		return imaginary;
	}

	public Complex add(Complex other) {
		return new Complex (this.real + other.real, this.imaginary + other.imaginary);
	}
	
	public Complex sub(Complex other) {
		return new Complex (this.real - other.real, this.imaginary - other.imaginary);
	}
	
	public Complex mul (Complex other) { 
		return new Complex (
				this.getReal() * other.getReal() - this.getImaginary() * other.getImaginary(), 
				this.getReal() * other.getImaginary() + this.getImaginary() * other.getReal());
	}
	
	public Complex div (Complex other) { 
		double a = this.real;
		double b = this.imaginary;
		double c = other.real;
		double d = other.imaginary;
		return new Complex (
				(a*c + b*d)/(c*c + d*d) , 
				(b*c-a*d)/(c*c+d*d));
	}
	public String toString() { 
		if (this.imaginary < 0 )
			return this.real + "" + this.imaginary + "i";
		return this.real + "+" + this.imaginary + "i";
	}

	
}
