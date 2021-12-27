/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package fractale;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
enum WindowFitMode { 
	MINFIT, 
	MAXFIT,
	FILL;
}

public class App {

	//	static final int MAX_ITER = 1000;


	public String getGreeting() {
		return "Hello world.";
	}

	// f (z) = z^2 + c avec c = −0.7269 + 0.1889i
	public static Complex f0(Complex c) {
		return c.mul(c).add(new Complex(-0.7269, 0.1889));
	}

	public static void main(String[] args) throws IOException {
		System.out.println(new App().getGreeting());
		FractaleRenderConfig c = FractaleRenderConfig.createSimple(1001, -1, 1);
		//		BufferedImage img = generateFractaleImage(c, App::f0, App::colorScheme1);
		//		ImageIO.write(img, "PNG", new File("MyFile.png"));
		main_(args);
	}
	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		//Create and set up the window.
		JFrame frame = new JFrame("Fractale rendering");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		FractaleRenderConfig c = FractaleRenderConfig.createSimple(1001, -1, 1);
		//        BufferedImage img = generateFractaleImage(c, App::f0, ColorScheme::colorScheme1);
		ImagePanel imagePanel = new ImagePanel(c, JolieFonction.getJoliesFonctions().get(0), ColorScheme::colorScheme0);
		imagePanel.setPreferredSize(new Dimension(1001, 1001));
		frame.getContentPane().add(imagePanel, BorderLayout.CENTER);
		imagePanel.setBackground(Color.GREEN);
		JFrame frame2 = new JFrame("Fractale controls");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new GridLayout(0, 1));
		frame2.getContentPane().add(leftPanel, BorderLayout.WEST);
		JComboBox<ColorScheme> colorScheme = new JComboBox<>(ColorScheme.getAllSchemes().toArray(new ColorScheme[2]));
		colorScheme.addActionListener(e ->  {
			System.out.println("selected : " + colorScheme.getSelectedItem());
			imagePanel.setColorScheme((ColorScheme)colorScheme.getSelectedItem());
		});
		leftPanel.add(colorScheme);
//		JTextField realNumber = new JTextField();
//		JTextField imaginaryNumber = new JTextField();  
//		JButton generator = new JButton("Générer fractale");
//		generator.addActionListener(e -> {
//			final double real = Double.parseDouble(realNumber.getText());
//			final double imaginary = Double.parseDouble(imaginaryNumber.getText());
//			Function <Complex, Complex> f = (Complex x) -> x.mul(x).add(new Complex(real, imaginary));
//			imagePanel.setFunction(f);
//		});
//		leftPanel.add(realNumber);
//		leftPanel.add(imaginaryNumber);
//		leftPanel.add(generator);
//		JComboBox<Integer> iterations = new JComboBox<>(new Integer[] { 100, 500, 1000 });
//		leftPanel.add(iterations);
//		iterations.addActionListener(e -> {
//			imagePanel.setIterations((Integer)iterations.getSelectedItem());
//		});

		JComboBox<WindowFitMode> squareRender = new JComboBox<>(WindowFitMode.values());
		
		leftPanel.add(squareRender);
		squareRender.addActionListener(e -> {
			// imagePanel.setSquareRendering(squareRender.isSelected());
			imagePanel.setWindowFitMode((WindowFitMode)squareRender.getSelectedItem());
		});
		squareRender.setSelectedItem(WindowFitMode.FILL);
//		squareRender.setSelected(false);
		JTextField function = new JTextField();
		leftPanel.add(function);
		JButton generatorFun = new JButton("Générer fonction");
		generatorFun.addActionListener(e -> {
			imagePanel.setFunction(new JolieFonction(function.getText()));
		});
		leftPanel.add(generatorFun);
		
		JComboBox<JolieFonction> joliesFonction = new JComboBox<>(JolieFonction.getJoliesFonctions().toArray(new JolieFonction[0]));
		joliesFonction.addActionListener(e ->  {
			JolieFonction f = (JolieFonction)joliesFonction.getSelectedItem();
			System.out.println("selected : " + f);
			imagePanel.setFunction(f);
			function.setText(f.getDefinition());
		});
		leftPanel.add(joliesFonction);

		//Display the window.
		frame.pack();
		frame.setVisible(true);
		frame2.pack();
		frame2.setVisible(true);
	}

	public static void main_(String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}
