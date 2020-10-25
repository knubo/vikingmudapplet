package no.knubo.mud;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextArea;

class CommunicationThread implements Runnable, KeyListener {

	/** The place for pre actions. Not good for sending after startup. */
	private List startupLists;
	private Socket vikingSocket;
	private PrintStream vikingOut;
	private InputStream vikingIn;
	/**
	 * Stuff that was left when text was parsed. Is kept to the next time text
	 * comes from the mud.
	 */
	private String leftovers;

	/**
	 * Keeps color codes and formatting for ansi text.
	 */
	private HashMap formatCodes;

	private Color currentColor;
	/**
	 * Keeps track of bold for text to be written.
	 */
	private boolean currentBold;
	private boolean currentUnderline;
	private boolean currentRevVid;

	private HashMap<String, String> aliases;
	private HashMap<String, String> triggers;
	private HashMap<String, Pattern> triggerPatterns = new HashMap<>();
	private final ColorPane textPane;
	private ColorPane chatPane;
	private final History history;
	private final JTextArea textInput;

	private long lastPoll;
	private final long timeBetweenPoll = 10 * 1000;
	private final Inventory inventory;

	/**
	 * Try to track if login is complete.
	 */
	private boolean loginComplete = false;
	private boolean passwordInput = false;
	private long lastAction;
	private StringBuilder textFieldSearchMode;
	private boolean startSearchMode;
	private TabCompletion tabCompletion;

	CommunicationThread(HashMap<String, String> aliases, HashMap<String, String> triggers,
						ColorPane textPane, ColorPane chatPane, History history,
						Inventory inventory, JTextArea textInput) {
		this.aliases = aliases;
		this.triggers = triggers;
		this.textPane = textPane;
		this.chatPane = chatPane;
		this.history = history;
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
				showText(fromServer);
			}
		} catch (IOException e) {
			textPane.setText(e.getMessage());
		}
		loginComplete = false;
		textPane.appendPlain("Connection to mud closed.\n", Color.WHITE);
		System.out.println("Socket loop ended");
	}

	private void doTriggers(String fromServer) {
		Set<Map.Entry<String, String>> ents = triggers.entrySet();

		for (Map.Entry<String, String> ent : ents) {
			String pattern = ent.getKey();

			Pattern p = triggerPatterns.get(pattern);
			if (p == null) {
				p = Pattern.compile(pattern, Pattern.DOTALL);
				triggerPatterns.put(pattern, p);
			}
			Matcher match = p.matcher(fromServer);

			if (match.lookingAt()) {
				String cmd = ent.getValue();
				int c = match.groupCount();

				for (int i = 1; i <= c; i++) {
					cmd = cmd.replace("$" + i, match.group(i));
				}
				doAction(cmd);
			}
		}

	}

	private void showText(String fromServerInput) {
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
			String t = fromServer.substring(0, posBrac - 1);
			addText(t);
		}

		posBrac++;

		if (posBrac == mpos) {
			currentColor = Color.WHITE;
			currentBold = false;
			currentUnderline = false;
			currentRevVid = false;
			showText(fromServer.substring(mpos + 1));
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

		showText(fromServer.substring(mpos + 1));
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

	final Pattern regex = Pattern.compile(".*\\[.*\\]:.*", Pattern.DOTALL);

	private void addText(String text) {

		if (regex.matcher(text).find() || text.contains("tells") || text.contains("You tell") || text.contains("shouts")) {
			chatPane.setEditable(true);
			chatPane.append(currentColor, text + "\n", currentBold, currentUnderline,
					currentRevVid);
			chatPane.setEditable(false);
		}
		if (lastAction + 60000 > System.currentTimeMillis()) {
			doTriggers(text);
		}
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
						.currentTimeMillis() &&
						lastAction + 60000 > System.currentTimeMillis()
				) {
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
		boolean replace = false;

		for (int i = 0; i < read; i++) {
			byte b = bytes[i];

			if (b < 27 && b != 10 && b != 9 && b != 13) {

				read--;

				/* -1 -5 1 */
				if (b == -5) {
					loginComplete = false;
					passwordInput = true;
					System.out.println("Password input");
				} else if (b == -4) {
					loginComplete = true;
					passwordInput = false;
					System.out.println("Login complete");
				}
				bytes[i] = 7;
				replace = true;
			}
		}
		if(replace) {
			return new String(bytes,0,read).replaceAll(String.valueOf(((char)7)), "");
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

		JTextArea textField = (JTextArea) arg0.getComponent();

		int keyCode = arg0.getKeyCode();

		if (arg0.getKeyCode() == KeyEvent.VK_TAB && textInput.getCaretPosition() > 0) {
			if (tabCompletion == null) {
				String text = textField.getText();
				int start = textInput.getCaretPosition() - 1;
				int end = textInput.getCaretPosition() - 1;
				while (start > 0 && (Character.isLetterOrDigit(text.charAt(start - 1)) || text.charAt(start - 1) == '/')) {
					start--;
				}
				while (end < text.length() - 1 && (Character.isLetterOrDigit(text.charAt(end + 1)) || text.charAt(start - 1) == '/')) {
					end++;
				}

				tabCompletion = new TabCompletion(textPane.getText(), text.substring(start, end + 1), textField.getText(), start, end);
			}
			if (arg0.isShiftDown()) {
				textField.setText(tabCompletion.getPreviousSuggestion());
				textField.setCaretPosition(tabCompletion.getCaretPositon());
			} else {
				textField.setText(tabCompletion.getNextSuggestion());
				textField.setCaretPosition(tabCompletion.getCaretPositon());
			}
			arg0.consume();
			return;
		}
		if (arg0.getKeyCode() == KeyEvent.VK_SHIFT) {
			return;
		}
		tabCompletion = null;


		if (arg0.getKeyCode() == KeyEvent.VK_R && arg0.isControlDown() &&
				textFieldSearchMode == null) {
			startSearchMode = true;
			arg0.consume();
			return;
		}
		if (keyCode == KeyEvent.VK_UP) {
			textField.setText(history.previous());
			stopSearchMode();
			return;
		}
		if (keyCode == KeyEvent.VK_DOWN) {
			textField.setText(history.next());
			stopSearchMode();
			return;
		}
		if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_BACK_SPACE) {
			stopSearchMode();
			return;
		}

		/* ctrl - d */
		if (arg0.isControlDown() && arg0.getKeyCode() == KeyEvent.VK_D) {
			String text = textField.getText();
			int caretPosition = textField.getCaretPosition();

			if(text.length() > caretPosition) {
				textField.setText(text.substring(0,
						caretPosition) + text.substring(textField.getCaretPosition() + 1));
				textField.setCaretPosition(caretPosition);
			}
			stopSearchMode();
			arg0.consume();
			return;
		}


		if (arg0.isControlDown() || arg0.isMetaDown()) {
			stopSearchMode();
			return;
		}

	}

	public void keyReleased(KeyEvent arg0) {
		JTextArea textField = (JTextArea) arg0.getComponent();

		/* ctrl -e */
		if ((arg0.isControlDown() || arg0.isMetaDown())
				&& arg0.getKeyCode() == KeyEvent.VK_E) {
			textField.setCaretPosition(textField.getText().length());
			stopSearchMode();
			arg0.consume();
			return;
		}

		/* ctrl -a */
		if ((arg0.isControlDown())
				&& arg0.getKeyCode() == KeyEvent.VK_A) {
			textField.setCaretPosition(0);
			stopSearchMode();
			arg0.consume();
			return;
		}

		/* ctrl - k */
		if (arg0.isControlDown() && arg0.getKeyCode() == KeyEvent.VK_K) {
			textField.setText(textField.getText().substring(0,
					textField.getCaretPosition()));
			stopSearchMode();
			arg0.consume();
			return;
		}


		/* ctrl-p */
		if (arg0.isControlDown() && arg0.getKeyCode() == KeyEvent.VK_P) {
			textField.setText(history.previous());
			stopSearchMode();
			arg0.consume();
			return;
		}

		/* ctrl-n */
		if (arg0.isControlDown() && arg0.getKeyCode() == KeyEvent.VK_N) {
			textField.setText(history.next());
			stopSearchMode();
			arg0.consume();
			return;
		}
	}

	private void stopSearchMode() {
		if (textFieldSearchMode != null) {
			textFieldSearchMode = null;
			textPane.appendPlain("Stopping search\n", Color.WHITE);
		}
	}

	private boolean updateChoiceWithHistorySearch(JTextArea textfield) {
		if (textFieldSearchMode == null || textFieldSearchMode.length() == 0) {
			return false;
		}
		String search = history.search(textFieldSearchMode);
		if (search != null) {
			textfield.setText(search);
			return true;
		}
		stopSearchMode();
		return false;
	}

	public void keyTyped(KeyEvent arg0) {

		if (startSearchMode) {
			textPane.appendPlain("Starting search.\n", Color.WHITE);

			textFieldSearchMode = new StringBuilder();
			arg0.consume();
			startSearchMode = false;
			return;
		}

		if (textFieldSearchMode != null) {
			if (!arg0.isControlDown() && !arg0.isAltDown() && (Character.isAlphabetic(arg0.getKeyChar()) || arg0.getKeyChar() == ' ')) {
				textFieldSearchMode.append(arg0.getKeyChar());
			}

			if (updateChoiceWithHistorySearch((JTextArea) arg0.getComponent())) {
				arg0.consume();
			}

		}

		JTextArea textfield = (JTextArea) arg0.getComponent();

		String raw = textfield.getText().replaceAll("\n", "");
		String toSend = raw + "\n";

		switch (arg0.getKeyChar()) {
			case '\n':
			case '\r':
				stopSearchMode();
				if (doClientAction(raw)) {
					textfield.setText("");
					return;
				}

				if (!passwordInput) {
					history.addHistroy(raw);
					textPane.appendPlain(toSend, Color.white);
				} else {
					textPane.appendPlain("<hidden>\n", Color.white);
				}

				/* alias replacement */

				String[] actions = null;
				if (raw.startsWith("$")) {
					actions = raw.substring(1).split(";");
				} else {
					String doit = aliases.get(raw);
					if(doit != null) {
						actions = doit.split(",");
					} else {
						actions = new String[] {raw};
					}
				}

				for (int k = 0; k < actions.length; k++) {
					String action = actions[k];

					String[] reps = calcReps(action);

					for (int i = Integer.parseInt(reps[1]); i-- > 0; ) {
						vikingOut.print(reps[0] + "\n");
						textfield.setText("");
						lastAction = System.currentTimeMillis();

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

	public void doActionNoEcho(String action) {
		vikingOut.println(action);
	}

	public void loginGuest() {
		startupLists.add("guest");
		loginComplete = true;
	}

	public void loginUser(String[] userAndPassword) {
		startupLists.add(userAndPassword[0]);
		startupLists.add(userAndPassword[1]);
		loginComplete = true;
	}


	public boolean isLoginComplete() {
		return loginComplete;
	}

	public void quit() {
		try {
			vikingSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
