package no.knubo.mud;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

class ColorPane extends JTextPane {

	public void appendPlain(String s, Color color) {
		append(color, s, false, false, false);
	}

	public void append(Color cInput, String s, boolean bold, boolean underline,
			boolean revVid) {
		Color c = cInput;
		
		Color bgcolor = null;

		// s = s.replace((char)27, '\n');

		if (revVid) {
			bgcolor = c;

			if (bgcolor == Color.WHITE) {
				c = Color.BLACK;
			} else {
				c = Color.WHITE;
			}
		} else {
			bgcolor = Color.BLACK;
		}

		StyleContext sc = StyleContext.getDefaultStyleContext();

		AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY,
				StyleConstants.Foreground, c);

		aset = sc
				.addAttribute(aset, StyleConstants.Bold, Boolean.valueOf(bold));

		aset = sc.addAttribute(aset, StyleConstants.Underline, Boolean
				.valueOf(underline));

		aset = sc.addAttribute(aset, StyleConstants.Background, bgcolor);

		int len = getDocument().getLength(); // same value as
		// getText().length();
		setCaretPosition(len); // place caret at the end (with no selection)
		setCharacterAttributes(aset, false);
		replaceSelection(s); // there is no selection, so inserts at caret
		// setEditable(false);
	}

	public static void main(String argv[]) {

		ColorPane pane = new ColorPane();
		pane.setFont(new Font("Monospaced", Font.PLAIN, 16));
		for (int n = 1; n <= 400; n += 1) {
			if (isPrime(n)) {
				pane.append(Color.red, String.valueOf(n) + ' ', true, true,
						false);
			} else if (isPerfectSquare(n)) {
				pane.append(Color.blue, String.valueOf(n) + ' ', false, false,
						false);
			} else {
				pane.append(Color.black, String.valueOf(n) + ' ', true, false,
						false);
			}
		}

		JFrame f = new JFrame("ColorPane example");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setContentPane(new JScrollPane(pane));
		f.setSize(600, 400);
		f.setVisible(true);
	}

	public static boolean isPrime(int n) {
		if (n < 2)
			return false;
		double max = Math.sqrt(n);
		for (int j = 2; j <= max; j += 1)
			if (n % j == 0)
				return false; // j is a factor
		return true;
	}

	public static boolean isPerfectSquare(int n) {
		int j = 1;
		while (j * j < n && j * j > 0)
			j += 1;
		return (j * j == n);
	}

}
