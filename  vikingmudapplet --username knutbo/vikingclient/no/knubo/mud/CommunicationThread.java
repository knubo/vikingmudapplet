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
	private final Alias aliases;
	private final Aliasrecorder aliasRecorder;

	private long lastPoll;
	private final long timeBetweenPoll = 10 * 1000;
	private final Inventory inventory;

	private boolean loginComplete = false;
	private final JTextArea textInput;

	CommunicationThread(ColorPane textPane, History history, Alias aliases,
			Aliasrecorder aliasRecorder, Inventory inventory,
			JTextArea textInput) {
		this.textPane = textPane;
		this.history = history;
		this.aliases = aliases;
		this.aliasRecorder = aliasRecorder;
		this.inventory = inventory;
		this.textInput = textInput;
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
		formatCodes.put("41", new RevVid(Color.RED));
		formatCodes.put("42", new RevVid(Color.GREEN));
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
			leftovers = null;
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
		loginComplete = false;
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
		if (startCode == fromServer.length() - 1) {
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
		byte[] bytes = new byte[255];
		if (available > 255) {
			available = 255;
		}

		if (available < 1) {
			available = 1;

			if (startupLists.size() > 0) {
				doActions();
				return readSome();
			}

			try {
				if (loginComplete
						&& lastPoll + timeBetweenPoll < System
								.currentTimeMillis()) {
					lastPoll = System.currentTimeMillis();
					return inventory.readInventory(vikingIn, vikingOut);
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}

		}

		int read = vikingIn.read(bytes, 0, available);

		if (read == -1) {
			return null;
		}

		if (!loginComplete) {
			for (int i = 0; i < read; i++) {
				byte b = bytes[i];

				if (b < 31 && b != 10 && b != 9 && b != 13) {

					if (b == -4) {
						loginComplete = true;
						System.out.println("Login complete");
					}
					bytes[i] = 32;
				}
			}
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
		JTextArea textfield = (JTextArea) arg0.getComponent();

		/* ctrl -e */
		if ((arg0.isControlDown() || arg0.isMetaDown())
				&& arg0.getKeyCode() == KeyEvent.VK_E) {
			textfield.setCaretPosition(textfield.getText().length());
			arg0.consume();
			return;
		}

		/* ctrl -a */
		if ((arg0.isControlDown() || arg0.isMetaDown())
				&& arg0.getKeyCode() == KeyEvent.VK_A) {
			textfield.setCaretPosition(0);
			arg0.consume();
			return;
		}
		if (arg0.isControlDown() && arg0.getKeyCode() == KeyEvent.VK_K) {
			textfield.setText(textfield.getText().substring(0,
					textfield.getCaretPosition()));
		}

		/* ctrl-p */
		if (arg0.isControlDown() && arg0.getKeyCode() == KeyEvent.VK_P) {
			textfield.setText(history.previous());
			arg0.consume();
			return;
		}

		/* ctrl-n */

		if (arg0.isControlDown() && arg0.getKeyCode() == KeyEvent.VK_N) {
			textfield.setText(history.next());
			arg0.consume();
			return;
		}

	}

	public void keyTyped(KeyEvent arg0) {
		JTextArea textfield = (JTextArea) arg0.getComponent();

		String raw = textfield.getText().replaceAll("\n", "");
		String toSend = raw + "\n";

		switch (arg0.getKeyChar()) {
			case '\n' :
				if (doClientAction(raw)) {
					textfield.setText("");
					return;
				}

				history.addHistroy(raw);
				aliasRecorder.addCommand(raw);
				textPane.appendPlain(toSend, Color.white);

				/* alias replacement */
				String alias = aliases.getAlias(raw);
				if (alias != null) {
					raw = alias;
				}

				String[] actions = null;
				if (raw.startsWith("$")) {
					actions = raw.substring(1).split(";");
				} else {
					actions = new String[]{raw};
				}

				for (int k = 0; k < actions.length; k++) {
					String action = actions[k];

					String[] reps = calcReps(action);

					for (int i = Integer.parseInt(reps[1]); i-- > 0;) {
						vikingOut.print(reps[0] + "\n");
						textfield.setText("");
					}
				}

				return;
		}

	}
	private boolean doClientAction(String raw) {
		if (raw.equals("#history")) {
			textPane.appendPlain(history.allHistory(), Color.WHITE);
			return true;
		}

		if (raw.equals("#aliasedit")) {
			aliases.setVisible(!aliases.isVisible());
			return true;
		}

		if (raw.startsWith("#alias")) {
			if (raw.equals("#alias")) {
				textPane.appendPlain(aliases.toString(), Color.white);
			} else if (aliases.addAlias(raw)) {
				textPane.appendPlain("OK", Color.WHITE);
			} else {
				textPane.appendPlain("Alias sytax: #alias <alias> <whatever>.",
						Color.RED);
			}
			return true;
		}

		if (raw.equals("#inv")) {
			inventory.setVisible(!inventory.isVisible());
			textInput.requestFocus();
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

	public boolean isLoginComplete() {
		return loginComplete;
	}

}
