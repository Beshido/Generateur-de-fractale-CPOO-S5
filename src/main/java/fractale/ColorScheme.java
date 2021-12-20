package fractale;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class ColorScheme {
	String name;
	Function<Integer, Color> f;

	public ColorScheme (String name, Function<Integer, Color> f) { 
		this.name = name;
		this.f    = f ;
	}

	public static List<ColorScheme> getAllSchemes() { 
		return Arrays.asList(
				new ColorScheme("ColorScheme BLEU", ColorScheme::colorScheme0),
				new ColorScheme("ColorScheme GREEN", ColorScheme::colorScheme1),
				new ColorScheme("ColorScheme RED", ColorScheme::colorScheme2),
				new ColorScheme("ColorScheme FLAMBOYANT", ColorScheme::colorScheme3));
	}

	public static Color colorScheme0(int iterations) {
		//		return new Color(0, 0, Math.min(255, iterations));
		//		return new Color(((iterations / 256) % 256) * 64, 0, iterations % 256);
		return new Color(0, 0, (255*iterations)/App.MAX_ITER);
	}
	public static Color colorScheme1(int iterations) {
		//		return new Color(0, 0, Math.min(255, iterations));
		//		return new Color(((iterations / 256) % 256) * 64, 0, iterations % 256);
		return new Color(0,(255*iterations)/App.MAX_ITER,0);
	}
	public static Color colorScheme2(int iterations) {
		//		return new Color(0, 0, Math.min(255, iterations));
		//		return new Color(((iterations / 256) % 256) * 64, 0, iterations % 256);
		return new Color((255*iterations)/App.MAX_ITER, 0, 0);
	}
	public static Color colorScheme3(int iterations) {
		//		return new Color(0, 0, Math.min(255, iterations));
		//		return new Color(((iterations / 256) % 256) * 64, 0, iterations % 256);
		return new Color(Color.HSBtoRGB((float)iterations/App.MAX_ITER, 0.7f, 0.7f));
	}

	public String toString () { 
		return name;
	}

}
