package no.knubo.mud;

import java.awt.Color;
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
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

/**
 * Main code for the applet window. Sets up window stuff.
 */
public class MainWindow extends JApplet implements MenuTopics {

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

	/**
	 * Setup stuff.
	 */
	public MainWindow() {
		history = new History();

		textPane = new ColorPane();
		textPane.setMargin(new Insets(5, 5, 5, 5));
		textPane.setFont(new Font("Courier", Font.PLAIN, 14));

		textPane.append(Color.YELLOW, About.greetingText(),

		true, false, false);
		textPane.setBackground(Color.BLACK);
		textPane.setForeground(Color.WHITE);

		JScrollPane scrollPane = new JScrollPane(textPane,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		scrollPane.setAutoscrolls(true);

		int width = textPane.getFontMetrics(textPane.getFont()).charWidth('X') * 90;
		scrollPane.setMinimumSize(new Dimension(width, 200));
		setLocation(50, 50);

		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);

		GridBagConstraints displayConstraints = new GridBagConstraints();
		displayConstraints.gridx = 0;
		displayConstraints.gridy = 0;
		displayConstraints.gridwidth = 1;
		displayConstraints.gridheight = 1;
		displayConstraints.fill = GridBagConstraints.BOTH;
		displayConstraints.weighty = 1;
		gbl.setConstraints(scrollPane, displayConstraints);
		add(scrollPane);

		textInput = new JTextArea(80, 4);
		textInput.setBackground(Color.BLACK);
		textInput.setForeground(Color.WHITE);

		JScrollPane textPanel = new JScrollPane(textInput);
		int inputHeight = (int) ((textInput.getFontMetrics(textInput.getFont())
				.getHeight()) * 2.5);
		textPanel
				.setMinimumSize(new Dimension(textPane.getWidth(), inputHeight));

		textInput.setLineWrap(true);
		textInput.setCaretColor(Color.WHITE);
		displayConstraints.gridy = 1;
		displayConstraints.weighty = 0;

		gbl.setConstraints(textPanel, displayConstraints);
		add(textPanel);

		// Set up the menu bar.
		JMenuBar mb = new JMenuBar();
		mb.add(createGameMenu());
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
