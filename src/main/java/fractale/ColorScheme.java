package fractale;

import java.awt.Color;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;


/**
 * Generation de couleur en utilisation la courbe de Bezier quadratique.
 * <p>Courbe de Bezier :  <a href="https://fr.wikipedia.org/wiki/Courbe_de_B%C3%A9zier">Wiki courbe de Beziers</a></p>.
 */
class ColorBezierQuad {
	
	private final Color c0, c1, c2;
	
	ColorBezierQuad(Color c0, Color c1, Color c2) {
		this.c0 = c0;
		this.c1 = c1;
		this.c2 = c2;
	}
	
	public static double get(double v0, double v1, double v2, double t) {
		double t1 = 1 - t;
		return t1 * t1 * v0 + 2 * t1 * t * v1 + t * t * v2;
	}
	
	public Color getColor(double t) {
		return new Color(
				(int)get(c0.getRed(), c1.getRed(), c2.getRed(), t),
				(int)get(c0.getGreen(), c1.getGreen(), c2.getGreen(), t),
				(int)get(c0.getBlue(), c1.getBlue(), c2.getBlue(), t));
	}
	
}


/**
 * Generation de couleur en utilisation la courbe de Bezier cubique.
 * <p> Courbe de Bezier :  <a href="https://fr.wikipedia.org/wiki/Courbe_de_B%C3%A9zier">Wiki courbe de Beziers</a> </p>.
 */
class ColorBezierCube {
	private final Color c0, c1, c2, c3;
	
	ColorBezierCube(Color c0, Color c1, Color c2, Color c3) {
		this.c0 = c0;
		this.c1 = c1;
		this.c2 = c2;
		this.c3 = c3;
	}
	
	public static double get(double v0, double v1, double v2, double v3, double t) {
		double t1 = 1 - t;
		return t1 * t1 * t1 * v0 + 3 * t1 * t1 * t * v1 + 3 * t1 * t * t * v2 + t * t * t * v3;
	}
	
	public Color getColor(double t) {
		return new Color(
				(int)get(c0.getRed(), c1.getRed(), c2.getRed(), c3.getRed(), t),
				(int)get(c0.getGreen(), c1.getGreen(), c2.getGreen(), c3.getGreen(),t),
				(int)get(c0.getBlue(), c1.getBlue(), c2.getBlue(), c3.getBlue(), t));
	}

}


/**
 * Les diff�rents th�mes de couleur.
 */
public class ColorScheme {
	String name;
	BiFunction<FractaleRenderConfig, Integer, Color> f;

	public ColorScheme (String name, BiFunction<FractaleRenderConfig, Integer, Color> f) { 
		this.name = name;
		this.f    = f ;
	}

	/**
	 * Renvoie la listes des differents themes de couleur.
	 * @return list des ColorSchemes.
	 */
	public static List<ColorScheme> getAllSchemes() { 
		return Arrays.asList(
				new ColorScheme("BLEU", ColorScheme::colorScheme0),
				new ColorScheme("GREEN", ColorScheme::colorScheme1),
				new ColorScheme("RED", ColorScheme::colorScheme2),
				new ColorScheme("FLAMBOYANT", ColorScheme::colorScheme3),
				quadBzScheme("BGR", Color.BLUE, Color.GREEN, Color.RED),
				quadBzScheme("KGR", Color.BLACK, Color.GREEN, Color.RED),
				quadBzScheme("KRY", Color.BLACK, Color.RED, Color.YELLOW),
				cubicBzScheme("KBGR", Color.BLACK, Color.BLUE, Color.GREEN, Color.RED)
				);
	}
	//Les differents exemples fournit dans le pdf. 
	//	return new Color(0, 0, Math.min(255, iterations));
	//	return new Color(((iterations / 256) % 256) * 64, 0, iterations % 256);
	
	public static Color colorScheme0(FractaleRenderConfig cfg, int iterations) {
		return new Color(0, 0, (255*iterations)/cfg.maxIterations);
	}
	public static Color colorScheme1(FractaleRenderConfig cfg, int iterations) {
		return new Color(0,(255*iterations)/cfg.maxIterations,0);
	}
	public static Color colorScheme2(FractaleRenderConfig cfg, int iterations) {
		return new Color((255*iterations)/cfg.maxIterations, 0, 0);
	}
	public static Color colorScheme3(FractaleRenderConfig cfg, int iterations) {
		return new Color(Color.HSBtoRGB((float)iterations/cfg.maxIterations, 0.7f, 0.7f));
	}
	
	public static ColorScheme quadBzScheme(String name, Color c0, Color c1, Color c2) {
		ColorBezierQuad cbzq = new ColorBezierQuad(c0, c1, c2);
		return new ColorScheme(name, (cfg,its) -> cbzq.getColor(((double)its)/cfg.maxIterations));
	}
	
	public static ColorScheme cubicBzScheme(String name, Color c0, Color c1, Color c2, Color c3) {
		ColorBezierCube cbzq = new ColorBezierCube(c0, c1, c2, c3);
		return new ColorScheme(name, (cfg,its) -> cbzq.getColor(((double)its)/cfg.maxIterations));
	}
	
	public String toString () { 
		return name;
	}

}
