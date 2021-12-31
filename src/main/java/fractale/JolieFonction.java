package fractale;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Class de stockage des focntions notables. 
 * <p> La defintion de la fonction peut �tre rentr�e sous forme de String grace au : {@link PolyParser}. <p>
 */
public class JolieFonction {
	private final String name;
	private final String def;
	private final Function<Complex, Complex> function;
	
	public JolieFonction(String name, String def) {
		this.name = name;
		this.def  = def;
		this.function = new PolyParser().parse2(def);
	}
	public JolieFonction(String def) { 
		this(null, def);
	}
	public Function<Complex, Complex> getFunction() {
		return function;
	}
	public String toString() {
		return name + " (f(z) = " + def + ")";
	}
	public String getDefinition() { 
		return def;
	}
	public String getName() { 
		return name;
	}
	/**
	 * Fonction revoyant les fonctions sauvegard�es. 
	 * @return La liste des fonctions sauvegard�es. 
	 */
	public static List<JolieFonction> getJoliesFonctions () { 
	return Arrays.asList(
			new JolieFonction("julia", "z * z -0.7269 + 0.1889i"),
			new JolieFonction("saut interstellaire", "z*z -1.401155"),
			new JolieFonction("Newton degre 5 (25 iterations) ", "z * z * z * z -1")
			);
	}
			
}