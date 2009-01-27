package p2psip.tools;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public class Estilo {

	private static String fuente = "Verdana";
	public static Color naranja = new Color(null, 255, 128, 0);
	public static Color negro = new Color(null, 0, 0, 0);
	public static Color verde = new Color(null, 0, 100, 0);
	public static Color verdeClaro = new Color(null, 0, 128, 64);
	public static Color blanco = new Color(null, 255, 255, 255);
	public static Color gris = new Color(null, 190, 190, 190);
	public static Color grana = new Color(null, 150, 0, 0);
	public static Color amarillo = new Color(null, 255, 255, 0);
	public static Color rojo = new Color(null, 255, 0, 0);
	public static Color amarilloSuave = new Color(null, 255, 250, 120);
	public static Color azulMarino = new Color(null, 0, 0, 128);
	public static Color azulCeleste = new Color(null, 0, 128, 255);
	public static Color azulFondo = new Color(null, 35, 105, 111);

	public static Image maderaDeRoble = new Image(null, ClassLoader
			.getSystemResourceAsStream("rsc/cards/madera2.png"));

	public static Font letra14 = new Font(null, fuente, 14, SWT.NONE);
	public static Font letra12 = new Font(null, fuente, 12, SWT.NONE);
	public static Font letra22 = new Font(null, fuente, 22, SWT.NONE);
	public static Font letra18 = new Font(null, fuente, 18, SWT.NONE);
	public static Font letra10normal = new Font(null, fuente, 10, SWT.NONE);
	public static Font letra16Tapete = new Font(null, "rsc/fonts/akbar.TTF", 16,SWT.NONE);

	private static StyleRange style = null;

	public static Color getColor(int pos) {
		Color[] color = { naranja, azulMarino, verdeClaro, rojo, blanco,
				azulCeleste, gris, verde, grana, amarillo };
		if (pos >= color.length) {
			pos = 0;
		}
		return color[pos];
	}
}
