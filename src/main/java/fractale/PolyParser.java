package fractale;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

class Element { 
	String contenu;
	static class EVar extends Element {

		String name;

		public EVar (String name) { 
			this.name = name;
		}

	}
	static class EConstant extends Element {

		double valueReal;
		double valueImaginary;

		public EConstant(double value, double imaginary) {
			this.valueReal = value;
			this.valueImaginary = imaginary;
		}
		public String toString() { 
			return "(" + valueReal + "," + valueImaginary + ")"; 
		}
	}

	//	static class EParen extends Element {
	//		
	//	}
	static class OpenParen extends Element { 

	}
	static class CloseParen extends Element { 

	}
	static class EPlus extends Element { 

	}
	static class EMinus extends Element { 

	}

	static class EMul extends Element { 

	}
	static class EDiv extends Element { 

	}
}

class Expression { 
	static  class Constant extends Expression {
		public double valueRe;
		public double valueIm;
		Constant(double value, double valueImaginary) { this.valueRe = value; this.valueIm = valueImaginary ;}
	}
	static class Add extends Expression { 
		public Expression exp1;
		public Expression exp2;
		Add(Expression exp1, Expression exp2) {
			this.exp1 = exp1;
			this.exp2 = exp2;
		}
	}
	static class Mul extends Expression { 
		public Expression exp1;
		public Expression exp2;
		Mul(Expression exp1, Expression exp2) {
			this.exp1 = exp1;
			this.exp2 = exp2;
		}
	} 
	static class Div extends Expression { 
		public Expression exp1;
		public Expression exp2;
		Div(Expression exp1, Expression exp2) {
			this.exp1 = exp1;
			this.exp2 = exp2;
		}
	}
	static class Variable extends Expression { 
		public String name;
		Variable(String name) { this.name = name; }
	}
	static class Sub extends Expression { 
		public Expression exp1;
		public Expression exp2;
		Sub(Expression exp1, Expression exp2) {
			this.exp1 = exp1;
			this.exp2 = exp2;
		}
	}
}

public class PolyParser {

	// parse apres "f(z) = " z*z+1-i
	// z*(z+1)
	// syntaxe:
	//   z
	//   nombre
	//   (...)
	//   * /
	//   + -
	public Function<Complex, Complex> parse(String text) {
		List<Element> es = textDestructor(text);
		this.es = es;
		Expression e = parseExpr();
		return z -> evalExpr(e,z);
		// throw new RuntimeException();
	}
	public Function<Complex, Complex> parse2(String text) {
		List<Element> es = textDestructor(text);
		this.es = es;
		Expression e = parseExpr();
		return mkFun(e);
		// throw new RuntimeException();
	}
	public List<Element> textDestructor(String text) { 
		List<Element> es = new ArrayList<>();
		int index = 0;
		while (index < text.length()) {
			char c = text.charAt(index);
			if (c == '(') {
				es.add(new Element.OpenParen());
				index ++;
			} else if (c == ')') {
				es.add(new Element.CloseParen());
				index++;
			} else if (c == '+') {
				es.add(new Element.EPlus());
				index++;			
			} else if (c == '-') {
				es.add(new Element.EMinus());
				index++;
			} else if (c == '*') {
				es.add(new Element.EMul());
				index++;
			} else if (c == 'i') {
				es.add(new Element.EConstant(0, 1));
				index++;
			} else if (Character.isDigit(c)) {
				int i = index;
				while (i < text.length() && (text.charAt(i) == '.' || Character.isDigit(text.charAt(i))))
					i++;
				if (i < text.length() && (text.charAt(i) == 'i')) {
					es.add(new Element.EConstant(0, Double.parseDouble(text.substring(index, i))));
					index = i+1;
				} else {
					es.add(new Element.EConstant(Double.parseDouble(text.substring(index, i)), 0));
					index = i;
				}
			} else if (Character.isAlphabetic(c)) {
				int i = index;
				while (i < text.length() && (Character.isAlphabetic(text.charAt(i))))
					i++;
				es.add(new Element.EVar(text.substring(index, i)));
				index = i;
			} else if (c == ' ') {
				index++;
			} else {
				throw new RuntimeException("parse failed '" + text.substring(index) + "'");
			}
		}
		System.out.println(es);
		return es;		
	}

	List<Element> es;

	public Expression parseExpr() {
		Expression e = parsePlusMinus();
		if (!es.isEmpty())
			throw new RuntimeException("parse failed : " + es);
		return e;
	}
	
	public Expression parsePlusMinus() {
		Expression e = parseMulDiv();
		while (!es.isEmpty()) {
			Element o = es.get(0);
			if (o instanceof Element.EPlus) {
				es.remove(0);
				Expression f = parseMulDiv();
				e = new Expression.Add(e, f);
			} else if (o instanceof Element.EMinus) {
				es.remove(0);
				Expression f = parseMulDiv();
				e = new Expression.Sub(e, f);
			} else {
//				throw new RuntimeException("parsePlusMinus : " + es);
				return e;
			}	
		}
		return e;
	}
	
	public Expression parseMulDiv() {
//		System.out.println("parseMulDiv " + es);
		Expression e = parseNumOrVarOrParen();
		while (!es.isEmpty()) {
			Element o = es.get(0);
			if (o instanceof Element.EMul) {
				es.remove(0);
				Expression f = parseNumOrVarOrParen();
				e = new Expression.Mul(e, f);
			} else if (o instanceof Element.EDiv) {
				es.remove(0);
				Expression f = parseNumOrVarOrParen();
				e = new Expression.Div(e, f);
			} else {
				// throw new RuntimeException("parseMulDiv " + o + " / " + es);
				return e;
			}	
		}
		return e;
	}

	// Atom
	public Expression parseNumOrVarOrParen() {
		Element e = es.remove(0);
		if (e instanceof Element.EConstant) {
			Element.EConstant ec = (Element.EConstant)e;
			return new Expression.Constant(ec.valueReal, ec.valueImaginary);
		} else if (e instanceof Element.EVar) {
			return new Expression.Variable(((Element.EVar)e).name);
		} else if (e instanceof Element.OpenParen) {
			Expression ex = parsePlusMinus();
			Element e2 = es.remove(0);
			if (e2 instanceof Element.CloseParen) 
				return ex;
			throw new RuntimeException("expected close paren");
		} else {
			throw new RuntimeException("expected var/num/paren");
		}
	}
	
	public Complex evalExpr(Expression e, Complex z) {
		if (e instanceof Expression.Constant) {
			Expression.Constant c = (Expression.Constant)e;
			return new Complex(c.valueRe, c.valueIm);
		} else if (e instanceof Expression.Variable) {
			return z;
		} else if (e instanceof Expression.Add) {
			Expression.Add p = (Expression.Add)e;
			return evalExpr(p.exp1, z).add(evalExpr(p.exp2, z));
		} else if (e instanceof Expression.Sub) {
			Expression.Sub o = (Expression.Sub)e;
			return evalExpr(o.exp1, z).sub(evalExpr(o.exp2, z));
		}  else if (e instanceof Expression.Mul) {
			Expression.Mul o = (Expression.Mul)e;
			return evalExpr(o.exp1, z).mul(evalExpr(o.exp2, z));
		} else if (e instanceof Expression.Div) {
			Expression.Div o = (Expression.Div)e;
			return evalExpr(o.exp1, z).div(evalExpr(o.exp2, z));
		} else {
			throw new RuntimeException("evalExpr " + e);
		}
	}
	
	public Function<Complex, Complex> mkFun(Expression e) {
		if (e instanceof Expression.Constant) { 
			Expression.Constant c = (Expression.Constant)e;
			Complex k = new Complex (c.valueRe, c.valueIm);
			return z -> k;
		} else if (e instanceof Expression.Add) {
			Expression.Add c = (Expression.Add)e;
			Function<Complex, Complex> f = mkFun(c.exp1);
			Function<Complex, Complex> g = mkFun(c.exp2);
			return z -> f.apply(z).add(g.apply(z));
		} else if (e instanceof Expression.Sub) {
			Expression.Sub c = (Expression.Sub)e;
			Function<Complex, Complex> f = mkFun(c.exp1);
			Function<Complex, Complex> g = mkFun(c.exp2);
			return z -> f.apply(z).sub(g.apply(z));
		} else if (e instanceof Expression.Mul) {
			Expression.Mul c = (Expression.Mul)e;
			Function<Complex, Complex> f = mkFun(c.exp1);
			Function<Complex, Complex> g = mkFun(c.exp2);
			return z -> f.apply(z).mul(g.apply(z));
		} else if (e instanceof Expression.Div) {
			Expression.Div c = (Expression.Div)e;
			Function<Complex, Complex> f = mkFun(c.exp1);
			Function<Complex, Complex> g = mkFun(c.exp2);
			return z -> f.apply(z).div(g.apply(z));
		} else if (e instanceof Expression.Variable) {
			return z -> z;
		} else {
			throw new RuntimeException("mkfun " + e);
		}
	}
	
	public static void main(String[] args) {
		String text = args[0];
		System.out.println("expression en entrée : '" + text + "'");
		Function<Complex,Complex> f = new PolyParser().parse(text);
		Complex c = new Complex(2,0);
		System.out.println("f " + c + " = " + f.apply(c));
		Function<Complex,Complex> g = new PolyParser().parse2(text);
		System.out.println("g " + c + " = " + g.apply(c));
	}

}
