package no.knubo.mud;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JApplet;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;

/**
 * Main code for the applet window. Sets up window stuff.
 */
public class MainWindow extends JApplet implements MenuTopics {

	private static final int DEFALT_FONT_SIZE = 12;

	/**
	 * The place where the text is drawn.
	 */
	protected ColorPane textPane;

	/**
	 * A lock for modifying the document, or for actions that depend on the
	 * document not being modified.
	 */
	Object doclock = new Object();

	JTextArea textInput;

	JMenuItem guestLogin;

	JMenuItem plainLogin;

	JMenuItem loggOff;

	private Thread theThread;

	CommunicationThread communicationThread;

	History history;

	JMenuItem clearWindow;

	private int fontSize;

	/**
	 * Setup stuff.
	 */
	public void init() {
		Font menuFont = new Font("Verdana", Font.PLAIN, 12);
		UIManager.put("Menu.font", menuFont);
		UIManager.put("MenuItem.font", menuFont);
		UIManager.put("CheckBoxMenuItem.font", menuFont);

		history = new History();

		textPane = new ColorPane();
		textPane.setMargin(new Insets(5, 5, 5, 5));
		fontSize = getFontSize();
		textPane.setFont(new Font(getFontName(), Font.PLAIN, fontSize));

		textPane.append(Color.YELLOW, About.greetingText(), true, false, false);
		textPane.setBackground(Color.BLACK);
		textPane.setForeground(Color.WHITE);

		JScrollPane scrollPane = new JScrollPane(textPane,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		scrollPane.setAutoscrolls(true);

		Font sizeFont = null;
		if (fontSize < DEFALT_FONT_SIZE) {
			sizeFont = new Font(getFontName(), Font.PLAIN, DEFALT_FONT_SIZE);
		} else {
			sizeFont = textPane.getFont();
		}

		int width = textPane.getFontMetrics(sizeFont).charWidth('X') * 90;
		scrollPane.setMinimumSize(new Dimension(width, 200));

		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);

		GridBagConstraints displayConstraints = new GridBagConstraints();
		displayConstraints.gridx = 0;
		displayConstraints.gridy = 0;
		displayConstraints.gridwidth = 1;
		displayConstraints.gridheight = 1;
		displayConstraints.anchor = GridBagConstraints.NORTHWEST;
		displayConstraints.fill = GridBagConstraints.BOTH;
		displayConstraints.weightx = 1;
		displayConstraints.weighty = 1;
		gbl.setConstraints(scrollPane, displayConstraints);
		add(scrollPane);

		textInput = new JTextArea(80, 4);
		textInput.setBackground(Color.BLACK);
		textInput.setForeground(Color.WHITE);

		int inputHeight = (int) ((textInput.getFontMetrics(textInput.getFont())
				.getHeight()) * 2.5);
		textInput
				.setMinimumSize(new Dimension(textPane.getWidth(), inputHeight));

		textInput.setLineWrap(true);
		textInput.setFont(textPane.getFont());
		textInput.setCaretColor(Color.WHITE);
		displayConstraints.gridy = 1;
		displayConstraints.weightx = 0;
		displayConstraints.weighty = 0;

		gbl.setConstraints(textInput, displayConstraints);
		add(textInput);

		// Set up the menu bar.
		JMenuBar mb = new JMenuBar();
		mb.add(createGameMenu());
		mb.add(createFontMenu());
		mb.add(createHistoryMenu());
		mb.add(createHelpMenu());
		setJMenuBar(mb);

		textPane.addKeyListener(new KeyAdapter() {

			public void keyTyped(KeyEvent e) {
				if (e.isActionKey()) {
					return;
				}

				if (e.isMetaDown() && e.getKeyChar() == 'c') {
					return;
				}

				textInput.requestFocus();
				textInput.append(String.valueOf(e.getKeyChar()));
				e.consume();
			}

		});

		setBounds(0, 0, textPane.getMinimumSize().width + 40, 540);
		// put it all together and show it.
		setVisible(true);
	}
	String getFontName() {
		try {
			String value = getParameter("FONT_NAME");

			if (value != null) {
				return value;
			}
			return "Courier New";
		} catch (Exception e) {
			textPane.appendPlain(e.getMessage(), Color.RED);
			e.printStackTrace();
			return "Courier New";
		}
	}
	private int getFontSize() {
		try {
			String value = getParameter("FONT_SIZE");

			if (value == null) {
				return DEFALT_FONT_SIZE;
			}
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				return DEFALT_FONT_SIZE;
			}
		} catch (Exception e) {
			textPane.appendPlain(e.getMessage(), Color.RED);
			e.printStackTrace();
			return DEFALT_FONT_SIZE;
		}
	}
	public boolean login() {

		if (theThread != null && theThread.isAlive()) {
			return false;
		}

		if (communicationThread == null) {
			communicationThread = new CommunicationThread(this.textPane,
					history);

			this.textInput.addKeyListener(communicationThread);
		}
		theThread = new Thread(communicationThread);
		theThread.start();
		this.textInput.requestFocus();
		return true;
	}

	private JMenu createHistoryMenu() {
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JMenuItem item = (JMenuItem) e.getSource();

				final String choice = item.getText();

				if (choice.equals(HISTORY_NEXT_COMMAND)) {
					textInput.setText(history.next());
				} else if (choice.equals(HISTORY_PREVIOUS_COMMAND)) {
					textInput.setText(history.previous());
				} else if (choice.equals(HISTORY_SHOW_ALL)) {
					textPane.appendPlain(history.allHistory(), Color.WHITE);
				}
			}
		};

		JMenu menu = new JMenu("History");

		JMenuItem item = new JMenuItem(HISTORY_PREVIOUS_COMMAND);
		item.addActionListener(actionListener);
		menu.add(item);

		item = new JMenuItem(HISTORY_NEXT_COMMAND);
		item.addActionListener(actionListener);
		menu.add(item);

		menu.add(new JSeparator());

		item = new JMenuItem(HISTORY_SHOW_ALL);
		item.addActionListener(actionListener);
		menu.add(item);

		return menu;
	}

	void clearSelections(final JMenu menu) {
		Component[] menuComponents = menu.getMenuComponents();

		for (int i = 0; i < menuComponents.length; i++) {
			JCheckBoxMenuItem component = (JCheckBoxMenuItem) menuComponents[i];
			component.setSelected(false);
		}
	}
	private JMenu createFontMenu() {
		final JMenu menu = new JMenu("Font");

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JMenuItem item = (JMenuItem) e.getSource();

				clearSelections(menu);

				item.setSelected(true);
				String size = item.getText().substring(0, 2).trim();
				int sizeInt = Integer.parseInt(size);

				textPane.setFont(new Font(getFontName(), Font.PLAIN, sizeInt));
				textInput.setFont(textPane.getFont());
			}

		};

		JMenuItem item = new JCheckBoxMenuItem(" 8 pt");
		item.addActionListener(actionListener);
		menu.add(item);

		item = new JCheckBoxMenuItem("10 pt");
		item.addActionListener(actionListener);
		menu.add(item);

		item = new JCheckBoxMenuItem("12 pt");
		item.addActionListener(actionListener);
		menu.add(item);

		item = new JCheckBoxMenuItem("14 pt");
		item.addActionListener(actionListener);
		item.setSelected(true);
		menu.add(item);

		item = new JCheckBoxMenuItem("16 pt");
		item.addActionListener(actionListener);
		menu.add(item);

		item = new JCheckBoxMenuItem("18 pt");
		item.addActionListener(actionListener);
		menu.add(item);

		if (fontSize != DEFALT_FONT_SIZE) {
			String point;
			if (fontSize < 10) {
				point = " " + fontSize;
			} else {
				point = String.valueOf(fontSize);
			}
			clearSelections(menu);
			item = new JCheckBoxMenuItem(point + "pt (custom)");
			item.addActionListener(actionListener);
			item.setSelected(true);
			menu.add(item);
		}

		return menu;
	}

	/**
	 * Create the style menu.
	 * 
	 * @return the style menu.
	 */
	private JMenu createGameMenu() {
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (e.getSource() == guestLogin) {
					if (login()) {
						communicationThread.loginGuest();
					}
				} else if (e.getSource() == plainLogin) {
					history.init_discard_count();
					login();
				} else if (e.getSource() == loggOff) {
					if (communicationThread != null) {
						communicationThread.doAction("!quit");
					}
				} else if (e.getSource() == clearWindow) {
					textPane.setText("");
				}
			}
		};

		JMenu menu = new JMenu("Game");
		guestLogin = new JMenuItem("Login as guest");
		guestLogin.addActionListener(actionListener);
		menu.add(guestLogin);
		plainLogin = new JMenuItem("Just login");
		plainLogin.addActionListener(actionListener);
		menu.add(plainLogin);

		menu.add(new JSeparator());

		clearWindow = new JMenuItem("Clear window");
		clearWindow.addActionListener(actionListener);
		menu.add(clearWindow);

		menu.add(new JSeparator());
		loggOff = new JMenuItem("Logg off");
		loggOff.addActionListener(actionListener);
		menu.add(loggOff);

		return menu;
	}
	private JMenu createHelpMenu() {
		JMenu menu = new JMenu("Help");

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JMenuItem item = (JMenuItem) e.getSource();

				textPane.appendPlain("\n", Color.white);

				if (item.getText().equals(HELP_ABOUT)) {
					textPane.appendPlain(About.aboutInfo(), Color.YELLOW);
				} else if (item.getText().equals(HELP_GETTING_STARTED)) {
					if (communicationThread != null) {
						communicationThread.doAction("!help");
					} else {
						textPane.appendPlain("You need to connect first.\n",
								Color.YELLOW);
					}
				} else if (item.getText().equals(HELP_TOPICS)) {
					if (communicationThread != null) {
						communicationThread.doAction("!help mortal");
					} else {
						textPane.appendPlain("You need to connect first.\n",
								Color.YELLOW);
					}
				} else if (item.getText().equals(HELP_CLIENT)) {
					textPane.appendPlain(About.getClientHelp(), Color.YELLOW);
				}
			}
		};

		JMenuItem item = new JMenuItem(HELP_CLIENT);
		item.addActionListener(actionListener);
		menu.add(item);

		item = new JMenuItem(HELP_GETTING_STARTED);
		item.addActionListener(actionListener);
		menu.add(item);

		item = new JMenuItem(HELP_TOPICS);
		item.addActionListener(actionListener);
		menu.add(item);

		item = new JMenuItem(HELP_ABOUT);
		item.addActionListener(actionListener);
		menu.add(item);
		return menu;
	}
}
