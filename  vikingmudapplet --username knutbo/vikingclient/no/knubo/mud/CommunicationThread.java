package no.knubo.mud;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTextArea;

class CommunicationThread implements Runnable, KeyListener {

	/** The place for pre actions. Not good for sending after startup. */
	private List startupLists;
	private Socket vikingSocket;

	private PrintStream vikingOut;
	private InputStream vikingIn;

	private final ColorPane textPane;

	/**
	 * Stuff that was left when text was parsed. Is kept to the next time text
	 * comes from the mud.
	 */
	private String leftovers;

	/** Keeps color codes and formatting for ansi text. */
	private HashMap formatCodes;

	private Color currentColor;
	/** Keeps track of bold for text to be written. */
	private boolean currentBold;
	private boolean currentUnderline;
	private boolean currentRevVid;

	private final History history;

	CommunicationThread(ColorPane textPane, History history) {
		this.textPane = textPane;
		this.history = history;
		currentColor = Color.WHITE;
		setupColorCodes();
		startupLists = new LinkedList();
	}

	class RevVid {
		RevVid(Color x) {
			color = x;
		}

		Color color;
	}

	private void setupColorCodes() {
		formatCodes = new HashMap();

		formatCodes.put("30", Color.DARK_GRAY); /* Really black. */
		formatCodes.put("31", Color.RED);
		formatCodes.put("32", Color.GREEN);
		formatCodes.put("33", Color.YELLOW);
		formatCodes.put("34", Color.BLUE);
		formatCodes.put("35", Color.MAGENTA);
		formatCodes.put("36", Color.cyan);
		formatCodes.put("37", Color.WHITE);
		formatCodes.put("40", new RevVid(Color.DARK_GRAY)); /* Really black */
		formatCodes.put("41", Color.RED);
		formatCodes.put("42", Color.GREEN);
		formatCodes.put("43", new RevVid(Color.YELLOW));
		formatCodes.put("44", new RevVid(Color.BLUE));
		formatCodes.put("45", new RevVid(Color.MAGENTA));
		formatCodes.put("46", new RevVid(Color.CYAN));
		formatCodes.put("", "");

		formatCodes.put("1", "bold");
		formatCodes.put("5", Color.WHITE); /* Really blink */
		formatCodes.put("4", "underline");
		formatCodes.put("7", new RevVid(Color.WHITE)); /* Inverse */
	}

	public void run() {
		try {
			vikingSocket = new Socket("connect.vikingmud.org", 2001);
			vikingOut = new PrintStream(vikingSocket.getOutputStream(), true);
			vikingIn = vikingSocket.getInputStream();

			/*
			 * Skip some init telnet noise.
			 */
			for (int i = 0; i < 6; i++) {
				vikingIn.read();
			}

		} catch (UnknownHostException e) {
			textPane.setText(e.getMessage());
		} catch (IOException e) {
			textPane.setText(e.getMessage());
		}

		String fromServer = null;

		try {
			while ((fromServer = readSome()) != null) {
				visTekst(fromServer);
			}
		} catch (IOException e) {
			textPane.setText(e.getMessage());
		}
		textPane.appendPlain("Connection to mud closed.\n", Color.WHITE);
		System.out.println("Socket loop ended");
	}

	private void visTekst(String fromServerInput) {
		String fromServer = fromServerInput;

		if (leftovers != null) {
			fromServer = leftovers + fromServer;
			leftovers = null;
		}

		int[] tmp = calcPosAndMposV2(fromServer);
		if (tmp == null) {
			return;
		}

		int posBrac = tmp[0];
		int mpos = tmp[1];

		if (posBrac == -1) {
			addText(fromServer);
			return;
		}

		/* Add text before change */
		if (posBrac != 0) {
			/* There's an esc sign before the brachet */
			textPane.append(currentColor, fromServer.substring(0, posBrac - 1),
					currentBold, currentUnderline, currentRevVid);
		}

		posBrac++;

		if (posBrac == mpos) {
			currentColor = Color.WHITE;
			currentBold = false;
			currentUnderline = false;
			currentRevVid = false;
			visTekst(fromServer.substring(mpos + 1));
			return;
		}

		String color = fromServer.substring(posBrac, mpos);

		int separator = color.indexOf(';');

		if (separator != -1) {
			color = color.substring(separator + 1);
			currentBold = true;
		}

		Object nextAction = formatCodes.get(color);

		if (nextAction instanceof RevVid) {
			RevVid revvid = (RevVid) nextAction;
			currentColor = revvid.color;
			currentRevVid = true;
		} else if (nextAction instanceof Color) {
			currentColor = (Color) nextAction;
			currentRevVid = false;
		} else if (nextAction instanceof String) {

			if (nextAction.toString().equals("bold")) {
				currentBold = true;
			} else if (nextAction.toString().equals("underline")) {
				currentUnderline = true;
			}

		} else {
			System.out.println("Missing! posbrac:" + posBrac + " mpos " + mpos
					+ "> " + color + " " + fromServer);
		}

		visTekst(fromServer.substring(mpos + 1));
		return;

	}

	private int[] calcPosAndMposV2(String fromServer) {

		if (fromServer.length() == 0) {
			return null;
		}
		int startCode = fromServer.indexOf(27);

		/* No esc start found */
		if (startCode == -1) {
			addText(fromServer);
			return null;
		}

		/*
		 * Was the last letter, the escape char - keep it for later let the
		 * regular parsing do it.
		 */
		if (startCode == (fromServer.length() - 1)) {
			leftovers = fromServer;
			return null;
		}

		/* Just an escape? Then we just print it and keep going. */
		if (fromServer.charAt(startCode + 1) != '[') {
			addText(fromServer.substring(0, startCode));
			calcPosAndMposV2(fromServer.substring(startCode + 1));
		}

		int mpos = fromServer.indexOf('m', startCode + 1);

		if (mpos != -1) {
			return new int[]{startCode + 1, mpos};
		}

		leftovers = fromServer;

		return null;
	}

	private void addText(String text) {
		textPane.append(currentColor, text, currentBold, currentUnderline,
				currentRevVid);
	}

	private String readSome() throws IOException {
		int available = vikingIn.available();
		byte[] bytes = new byte[80];
		if (available > 80) {
			available = 80;
		}

		if (available < 1) {
			available = 1;

			if (startupLists.size() > 0) {
				doActions();
				return readSome();
			}
		}

		int read = vikingIn.read(bytes, 0, available);

		if (read == -1) {
			return null;
		}

		return new String(bytes, 0, read);
	}

	private void doActions() {
		for (Iterator i = startupLists.iterator(); i.hasNext();) {
			String action = (String) i.next();
			vikingOut.print(action + "\n");
		}
		startupLists.clear();

	}

	public void keyPressed(KeyEvent arg0) {

		JTextArea textfield = (JTextArea) arg0.getComponent();

		switch (arg0.getKeyCode()) {
			case KeyEvent.VK_UP :
				textfield.setText(history.previous());
				return;
			case KeyEvent.VK_DOWN :
				textfield.setText(history.next());
				return;
		}
	}

	public void keyReleased(KeyEvent arg0) {
		/* Not used yet */

	}

	public void keyTyped(KeyEvent arg0) {
		JTextArea textfield = (JTextArea) arg0.getComponent();

		/* ctrl -e */
		if ((arg0.isControlDown() || arg0.isMetaDown())
				&& arg0.getKeyChar() == 'e') {
			textfield.setCaretPosition(textfield.getText().length());
			arg0.consume();
			return;
		}

		/* ctrl -a */
		if ((arg0.isControlDown() || arg0.isMetaDown())
				&& arg0.getKeyChar() == 'a') {
			textfield.setCaretPosition(0);
			arg0.consume();
			return;
		}

		/* ctrl-p */
		if (arg0.isControlDown() && arg0.getKeyChar() =='p') {
			textfield.setText(history.previous());
			arg0.consume();
			return;
		}

		/* ctrl-n */

		if (arg0.isControlDown() && arg0.getKeyChar() == 'n') {
			textfield.setText(history.next());
			arg0.consume();
			return;
		}

		String raw = textfield.getText().replaceAll("\n", "");
		String toSend = raw + "\n";

		switch (arg0.getKeyChar()) {
			case '\n' :
				if (doClientAction(raw)) {
					textfield.setText("");
					return;
				}

				history.addHistroy(raw);
				textPane.appendPlain(toSend, Color.white);

				String[] reps = calcReps(raw);

				for (int i = Integer.parseInt(reps[1]); i-- > 0;) {
					vikingOut.print(reps[0] + "\n");
					textfield.setText("");
				}

				return;
		}

	}
	private boolean doClientAction(String raw) {
		if (raw.equals("#history")) {
			textPane.appendPlain(history.allHistory(), Color.WHITE);
			return true;
		}
		return false;
	}
	private String[] calcReps(String raw) {
		if (!raw.startsWith("#") || raw.length() < 3) {
			return new String[]{raw, "1"};
		}

		char count = raw.charAt(1);

		if (!Character.isDigit(count)) {
			return new String[]{raw, "1"};
		}

		return new String[]{raw.substring(2).trim(), String.valueOf(count)};
	}

	public void doAction(String action) {
		textPane.appendPlain(action + "\n", Color.white);
		vikingOut.println(action);
	}

	public void loginGuest() {
		startupLists.add("guest");

	}

}
