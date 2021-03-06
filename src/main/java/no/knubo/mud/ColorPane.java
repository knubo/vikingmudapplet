package no.knubo.mud;

import org.languagetool.JLanguageTool;
import org.languagetool.language.BritishEnglish;
import org.languagetool.rules.RuleMatch;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.io.IOException;
import java.util.List;

class ColorPane extends JTextPane {
	JLanguageTool langTool = new JLanguageTool(new BritishEnglish());
	private boolean spellchecking;


	public ColorPane() {
		new Thread(() -> {
			int lastPos = 0;

			while(true) {
				try {
					if(!spellchecking) {
						Thread.sleep(3000);
						continue;
					}

					int nextLen;

					String t = getText();
					nextLen = t.length();

					if(nextLen != lastPos) {

						String check = t.substring(lastPos);

						List<RuleMatch> result = langTool.check(check);

						for (RuleMatch ruleMatch : result) {
							int from = ruleMatch.getFromPos();
							int to = ruleMatch.getToPos();

							StyleContext sc = StyleContext.getDefaultStyleContext();
							synchronized (langTool) {
								AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY,
										StyleConstants.Foreground, Color.RED);
								aset = sc.addAttribute(aset, StyleConstants.Underline, true);

								setCaretPosition(lastPos+from);
								setSelectionEnd(lastPos+to	);
								setCharacterAttributes(aset, false);
								setCaretPosition(getDocument().getLength());
							}
						}

						lastPos = nextLen;
					}

					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}).start();
	}

	public void appendPlain(String s, Color color) {
		append(color, s, false, false, false);
	}

	public void append(Color cInput, String s, boolean bold, boolean underline,
					   boolean revVid) {
		Color c = cInput;

		Color bgcolor = null;

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

		/* same value as getText().length(); */
		synchronized (langTool) {
			int len = getDocument().getLength();

			setCaretPosition(len); // place caret at the end (with no selection)
			setCharacterAttributes(aset, false);
			replaceSelection(s);

			/* Force cursor to end of document to make it scroll */
			len = getDocument().getLength();
			setCaretPosition(len);
		}		// there is no selection, so inserts at caret
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
		if (n < 2) {
			return false;
		}
		double max = Math.sqrt(n);
		for (int j = 2; j <= max; j += 1) {
			if (n % j == 0) {
				return false; // j is a factor
			}
		}
		return true;
	}

	public static boolean isPerfectSquare(int n) {
		int j = 1;
		while (j * j < n && j * j > 0) {
			j += 1;
		}
		return j * j == n;
	}

	public void toggleSpelling() {
		spellchecking = !spellchecking;
	}
}
