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

	private List actionLists;
	private Socket vikingSocket;

	private PrintStream vikingOut;

	private InputStream vikingIn;

	private final ColorPane textPane;

	private Color currentColor;

	/**
	 * Stuff that was left when text was parsed. Is kept to the next time text
	 * comes from the mud.
	 */
	private String leftovers;

	private HashMap formatCode;

	private boolean currentBold;

	private boolean currentUnderline;

	private boolean currentRevVid;

	private final History history;

	CommunicationThread(ColorPane textPane, History history) {
		this.textPane = textPane;
		this.history = history;
		currentColor = Color.WHITE;
		setupColorCodes();
		actionLists = new LinkedList();
	}

	class RevVid {
		RevVid(Color x) {
			color = x;
		}

		Color color;
	}

	private void setupColorCodes() {
		formatCode = new HashMap();

		formatCode.put("30", Color.DARK_GRAY); /* Really black. */
		formatCode.put("31", Color.RED);
		formatCode.put("32", Color.GREEN);
		formatCode.put("33", Color.YELLOW);
		formatCode.put("34", Color.BLUE);
		formatCode.put("35", Color.MAGENTA);
		formatCode.put("36", Color.cyan);
		formatCode.put("37", Color.WHITE);
		formatCode.put("40", new RevVid(Color.DARK_GRAY)); /* Really black */
		formatCode.put("41", Color.RED);
		formatCode.put("42", Color.GREEN);
		formatCode.put("43", new RevVid(Color.YELLOW));
		formatCode.put("44", new RevVid(Color.BLUE));
		formatCode.put("45", new RevVid(Color.MAGENTA));
		formatCode.put("46", new RevVid(Color.CYAN));
		formatCode.put("", "");

		formatCode.put("1", "bold");
		formatCode.put("5", Color.WHITE); /* Really blink */
		formatCode.put("4", "underline");
		formatCode.put("7", new RevVid(Color.WHITE)); /* Inverse */
	}

	public void run() {
		try {
			vikingSocket = new Socket("connect.vikingmud.org", 2001);
			vikingOut = new PrintStream(vikingSocket.getOutputStream(), true);
			vikingIn = vikingSocket.getInputStream();
			

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
		System.out.println("Socket loop ended");
	}

	private void visTekst(String fromServerInput) {
		String fromServer = fromServerInput;
		
		if (leftovers != null) {
			fromServer = leftovers + fromServer;
			leftovers = null;
		}

		int mpos = -1;
		int posBrac = 0;

		while (posBrac >= 0 && mpos == -1) {

			posBrac = fromServer.indexOf('[', posBrac);

			if (posBrac == -1) {
				addText(fromServer);
				return;
			}

			mpos = fromServer.indexOf('m', posBrac);

			if ((fromServer.length() - 1) > posBrac) {
				int nextBrac = fromServer.indexOf('[', posBrac + 1);
				if (nextBrac != -1 && nextBrac < mpos) {
					posBrac = nextBrac;
					mpos = -1;
					continue;
				}
			}

			/* Maybe a [ has sneaked itself in like [[...m */

			if (mpos == -1 || mpos > (posBrac + 5)) {

				/*
				 * Maybe just a [ without a m? Then we look for next [ by
				 * looping again.
				 */
				if ((fromServer.length() - posBrac) > 5) {
					posBrac++;
					mpos = -1;
				} else {
					leftovers = fromServer;
					return;
				}
			}
		}

		if (posBrac == -1) {
			addText(fromServer);
			return;
		}

		/* Add text before change */
		if (posBrac != 0) {
			/* There's a trash letter before the brachet */
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

		Object nextAction = formatCode.get(color);

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

			if (actionLists.size() > 0) {
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
		for (Iterator i = actionLists.iterator(); i.hasNext();) {
			String action = (String) i.next();
			vikingOut.print(action + "\n");
		}
		actionLists.clear();

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

		String raw = textfield.getText().replaceAll("\n", "");
		String toSend = raw + "\n";

		switch (arg0.getKeyChar()) {
			case '\n' :
				history.addHistroy(raw);
				vikingOut.print(toSend);
				textfield.setText("");
				textPane.appendPlain(toSend, Color.white);
				return;
		}

	}

	public void doAction(String action) {
		textPane.appendPlain(action+"\n", Color.white);
		vikingOut.println(action);
	}

	public void loginGuest() {
		actionLists.add("guest");

	}


}
