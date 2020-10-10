package no.knubo.mud;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

/**
 * Main code for the applet window. Sets up window stuff.
 */
public class MainWindow extends JFrame implements MenuTopics {

	private static final String GAME_LOGG_OFF = "Logg off";

	private static final String COURIER_NEW = "Courier New";

	private static final int DEFALT_FONT_SIZE = 14;

	/**
	 * The place where the text is drawn.
	 */
	protected ColorPane textPane;

	JTextArea textInput;

	private Thread theThread;

	CommunicationThread communicationThread;

	History history;

	int fontSize;

	String chosenFont;

	HashMap<String, String> aliases = new HashMap<>();
	HashMap<String, String> triggers = new HashMap<>();

	Inventory inventoryFrame;
	ChatMessages chatFrame;

	SettingsFrame settingsFrame;

	/**
	 * Setup stuff.
	 */
	@SuppressWarnings("DuplicatedCode")
	public void init() {

		UIStuff.setupUI();

		history = new History();

		inventoryFrame = new Inventory();
		chatFrame = new ChatMessages();
		chatFrame.init(this);

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

		int width = textPane.getFontMetrics(textPane.getFont()).charWidth('X') * 90;
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

		setupTextInput();
		displayConstraints.gridy = 1;
		displayConstraints.weightx = 0;
		displayConstraints.weighty = 0;

		gbl.setConstraints(textInput, displayConstraints);
		add(textInput);

		makeMenu();

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

		setBounds(0, 0, 1024, 768);
		// put it all together and show it.

		setVisible(true);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/yggdrasil-normal.jpg"));
		setIconImage(image);

		//this is new since JDK 9
		final Taskbar taskbar = Taskbar.getTaskbar();

		try {
			//set icon for mac os (and other systems which do support this method)
			taskbar.setIconImage(image);
		} catch (final UnsupportedOperationException e) {
			System.out.println("The os does not support: 'taskbar.setIconImage'");
		} catch (final SecurityException e) {
			System.out.println("There was a security exception for: 'taskbar.setIconImage'");
		}

	}

	public void makeMenu() {
		JMenuBar mb = new JMenuBar();
		// Set up the menu bar.

		mb.add(createGameMenu());
		mb.add(createCommandMenu());
		mb.add(createAliasMenu());
		mb.add(createFontMenu());
		mb.add(createColorMenu());
		mb.add(createHistoryMenu());
		mb.add(createHelpMenu());


		setJMenuBar(mb);
		pack();
		setBounds(0, 0, 1024, 768);

	}

	private JMenu createAliasMenu() {
		JMenu menu = new JMenu("Aliases");
		String home = System.getProperty("user.home");

		File file = new File(home + "/.vikingmud");

		if(!file.canRead()) {
			return menu;
		}

		try (BufferedReader br = new BufferedReader(new FileReader(file))){
			String line = br.readLine();
			while(line != null) {
				if (line.startsWith("T:")) {
					int part = line.indexOf("=");
					triggers.put(line.substring(2, part), line.substring(part + 1));
				}
				if (line.startsWith("A:")) {
					int part = line.indexOf("=");
					String alias = line.substring(2, part);
					String action = line.substring(part + 1);
					aliases.put(alias, action);

					menu.add(menuitem(alias, l -> {
						if (communicationThread != null) {
							String[] cmds = action.split(",");
							for (String cmd : cmds) {
								communicationThread.doActionNoEcho(cmd);
							}
						}
					}));
				}
				line = br.readLine();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}



		return menu;
	}


	private void setupTextInput() {
		textInput = new JTextArea(80, 4);
		textInput.setBackground(Color.BLACK);
		textInput.setForeground(Color.WHITE);

		int inputHeight = (int) (textInput.getFontMetrics(textInput.getFont())
				.getHeight() * 2.5);
		textInput
				.setMinimumSize(new Dimension(textPane.getWidth(), inputHeight));

		textInput.setLineWrap(true);
		textInput.setFont(textPane.getFont());
		textInput.setCaretColor(Color.WHITE);

		textInput.setMargin(new Insets(5, 5, 5, 5));


	}
	String getFontName() {
		try {
			if (chosenFont != null) {
				return chosenFont;
			}
			String value = null; //getParameter("FONT_NAME");

			if (value != null) {
				chosenFont = value;
				return value;
			}
			chosenFont = COURIER_NEW;
			return COURIER_NEW;
		} catch (Exception e) {
			textPane.appendPlain(e.getMessage(), Color.RED);
			e.printStackTrace();
			chosenFont = COURIER_NEW;
			return COURIER_NEW;
		}
	}
	int getFontSize() {
		try {
			String value = null; //getParameter("FONT_SIZE");

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
			communicationThread = new CommunicationThread(this.aliases, this.triggers, this.textPane, this.chatFrame.textPane,
					history,
					this.inventoryFrame, this.textInput);

			this.textInput.addKeyListener(communicationThread);
		}
		theThread = new Thread(communicationThread);
		theThread.start();
		this.textInput.requestFocus();
		return true;
	}

	private JMenu createHistoryMenu() {
		ActionListener actionListener = e -> {
			JMenuItem item = (JMenuItem) e.getSource();

			final String choice = item.getText();

			switch (choice) {
				case HISTORY_NEXT_COMMAND:
					textInput.setText(history.next());
					break;
				case HISTORY_PREVIOUS_COMMAND:
					textInput.setText(history.previous());
					break;
				case HISTORY_SHOW_ALL:
					textPane.appendPlain(history.allHistory(), Color.WHITE);
					break;
			}
		};

		JMenu menu = new JMenu("History");
		menu.setMnemonic('h');

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

		for (Component menuComponent : menuComponents) {
			JMenuItem component = (JMenuItem) menuComponent;
			component.setSelected(false);
		}
	}

	private JMenu createFontMenu() {
		final JMenu menu = new JMenu("Font");
		menu.setMnemonic('f');
		final JMenu familymenu = new JMenu("Family");
		familymenu.setOpaque(true);
		final JMenu sizemenu = new JMenu("Size");
		sizemenu.setOpaque(true);
		menu.add(familymenu);
		menu.add(sizemenu);

		setupSizeMenu(sizemenu);
		setupFamilyMenu(familymenu);

		return menu;

	}
	private void setupFamilyMenu(final JMenu familymenu) {
		ActionListener actionListener = e -> {
			JMenuItem item = (JMenuItem) e.getSource();

			clearSelections(familymenu);

			item.setSelected(true);
			chosenFont = item.getText();
			textPane.setFont(new Font(getFontName(), Font.PLAIN, fontSize));
			textInput.setFont(textPane.getFont());
		};

		Font[] allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getAllFonts();

		LinkedList names = new LinkedList();
		for (int i = 0; i < allFonts.length; i++) {
			Font f = allFonts[i];

			f = f.deriveFont(15);

			/* Test if font is monospaced. */
			FontMetrics fontMetrics = getFontMetrics(f);

			int s1 = fontMetrics.stringWidth("/\\XX");
			int s2 = fontMetrics.stringWidth("  ii");
			if (s1 == 0 || s2 == 0 || s1 != s2) {
				continue;
			}

			names.add(f.getName());
		}
		names.add("Verdana");

		for (Iterator i = names.iterator(); i.hasNext();) {
			String name = (String) i.next();

			JCheckBoxMenuItem item = new JCheckBoxMenuItem(name);
			item.addActionListener(actionListener);
			if (name.equals(chosenFont)) {
				item.setSelected(true);
			}
			familymenu.add(item);
		}
		/* On request */

	}

	private void setupSizeMenu(final JMenu sizemenu) {
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JMenuItem item = (JMenuItem) e.getSource();

				clearSelections(sizemenu);

				item.setSelected(true);
				String size = item.getText().substring(0, 2).trim();
				fontSize = Integer.parseInt(size);

				textPane.setFont(new Font(getFontName(), Font.PLAIN, fontSize));
				textInput.setFont(textPane.getFont());
			}
		};

		String[] choices = {" 8pt", " 9pt", "10 pt", "11 pt", "12 pt", "14 pt",
				"16 pt", "18 pt"};

		for (int i = 0; i < choices.length; i++) {
			String size = choices[i];

			JMenuItem item = new JCheckBoxMenuItem(size);
			item.addActionListener(actionListener);
			if (i == 2) {
				item.setSelected(true);
			}
			sizemenu.add(item);
		}

		if (fontSize != DEFALT_FONT_SIZE) {
			String point;
			if (fontSize < 10) {
				point = " " + fontSize;
			} else {
				point = String.valueOf(fontSize);
			}
			clearSelections(sizemenu);
			JMenuItem item = new JCheckBoxMenuItem(point + "pt (custom)");
			item.addActionListener(actionListener);
			item.setSelected(true);
			sizemenu.add(item);
		}
	}

	boolean loginCheck() {
		if (communicationThread == null
				|| !communicationThread.isLoginComplete()) {
			textPane.appendPlain("You need to connect first.\n", Color.YELLOW);
			return false;
		}
		return true;
	}

	private JMenu createCommandMenu() {
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JMenuItem item = (JMenuItem) e.getSource();

				if (!loginCheck()) {
					return;
				}

				communicationThread.doAction("!" + item.getText());
			}

		};

		JMenu menu = new JMenu("Commands");
		menu.setMnemonic('c');

		menu.add(menuitem("bags", actionListener));
		menu.add(menuitem("eq", actionListener));
		menu.add(menuitem("inventory", actionListener));
		menu.add(menuitem("look", actionListener));
		menu.add(menuitem("score", actionListener));
		menu.add(menuitem("wear all", actionListener));
		menu.add(menuitem("wield all", actionListener));
		menu.add(menuitem("who", actionListener));
		menu.add(new JSeparator());
		menu.add(menuitem("west", actionListener));
		menu.add(menuitem("north", actionListener));
		menu.add(menuitem("east", actionListener));
		menu.add(menuitem("south", actionListener));
		return menu;
	}
	private JMenu createColorMenu() {
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JMenuItem item = (JMenuItem) e.getSource();

				if (!loginCheck()) {
					return;
				}
				if (item.getText().equals(TURN_ON_COLOUR_SUPPORT)) {
					communicationThread.doAction("!screen term colxterm");
				} else if (item.getText().equals(TURN_OFF_COLOUR_SUPPORT)) {
					communicationThread.doAction("!screen term dumb");
				} else if (item.getText().equals(SHOW_COLORS)) {
					communicationThread.doAction("!color");
				} else if (item.getText().equals(SUGGEST_COLOURS)) {
					String[] acts = {"colour youtell l_blue",
							"colour tells l_red", "colour prompt l_yellow",
							"colour exits b_blue", "colour channels l_green",
							"colour youhit l_green", "colour hityou cyan",
							"colour youmiss green", "colour missyou green"};
					for (int i = 0; i < acts.length; i++) {
						communicationThread.doAction(acts[i]);
					}
				}
			}
		};

		JMenu menu = new JMenu("Colour");
		menu.setMnemonic('o');

		menu.add(menuitem(TURN_ON_COLOUR_SUPPORT, actionListener));
		menu.add(menuitem(TURN_OFF_COLOUR_SUPPORT, actionListener));
		menu.add(menuitem(SHOW_COLORS, actionListener));
		menu.add(menuitem(SUGGEST_COLOURS, actionListener));

		return menu;
	}

	/**
	 * Create the style menu.
	 * 
	 * @return the style menu.
	 */
	private JMenu createGameMenu() {
		ActionListener actionListener = e -> {
			JMenuItem item = (JMenuItem) e.getSource();

			if (item.getText().equals(GAME_LOGIN_AS_GUEST)) {
				if (login()) {
					communicationThread.loginGuest();
				}
			} else if (item.getText().equals(GAME_JUST_LOGIN)) {
				login();
			} else if (item.getText().equals(GAME_LOGG_OFF)) {
				if (communicationThread != null) {
					communicationThread.doAction("!quit");
					communicationThread.quit();
				}
			} else if (item.getText().equals(GAME_CLEAR_WINDOW)) {
				textPane.setText("");
			} else if (item.getText().equals(GAME_SETTINGS_WINDOW)) {
				if(settingsFrame != null) {
					settingsFrame.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
				}
				settingsFrame = new SettingsFrame(this);
				settingsFrame.setVisible(true);
			} else if (item.getText().equals(GAME_INVENTORY)) {
				inventoryFrame.setVisible(true);
			} else if (item.getText().equals(GAME_CHAT)) {
				chatFrame.setVisible(true);
			}
		};

		JMenu menu = new JMenu("Game");

		menu.setMnemonic('g');
		menu.add(menuitem(GAME_LOGIN_AS_GUEST, actionListener));
		menu.add(menuitem(GAME_JUST_LOGIN, actionListener));

		addDynamicLogins(menu);

		menu.add(new JSeparator());
		menu.add(menuitem(GAME_INVENTORY, actionListener));
		menu.add(menuitem(GAME_CHAT, actionListener));
		menu.add(new JSeparator());

		menu.add(menuitem(GAME_CLEAR_WINDOW, actionListener));
		menu.add(new JSeparator());
		menu.add(menuitem(GAME_SETTINGS_WINDOW, actionListener));

		menu.add(new JSeparator());

		menu.add(menuitem(GAME_LOGG_OFF, actionListener));

		return menu;
	}

	private void addDynamicLogins(JMenu menu) {
		String home = System.getProperty("user.home");

		File file = new File(home + "/.vikingmud");

		if(!file.canRead()) {
			return;
		}

		try (BufferedReader br = new BufferedReader(new FileReader(file))){
			String line = br.readLine();
			while(line != null) {
				if(line.startsWith("C:")) {
					String[] userAndPassword = line.substring(2).split("=");
					menu.add(menuitem("Login " + userAndPassword[0], l -> {
						if (login()) {
							communicationThread.loginUser(userAndPassword);
						}
					}));
				}
				line = br.readLine();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private JMenuItem menuitem(String text, ActionListener actionListener) {
		JMenuItem menuItem = new JMenuItem(text);
		menuItem.addActionListener(actionListener);
		return menuItem;
	}

	private JMenu createHelpMenu() {
		JMenu menu = new JMenu("Help");
		menu.setMnemonic('h');

		final Map<String, String> helpMap = new HashMap<>(); //getHelpMap();

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JMenuItem item = (JMenuItem) e.getSource();

				textPane.appendPlain("\n", Color.white);

				switch (item.getText()) {
					case HELP_ABOUT:
						UIStuff.setupUI();
						textPane.appendPlain(About.aboutInfo(), Color.YELLOW);
						break;
					case HELP_GETTING_STARTED:
						if (!loginCheck()) {
							return;
						}
						communicationThread.doAction("!help");
						break;
					case HELP_TOPICS:
						if (!loginCheck()) {
							return;
						}
						communicationThread.doAction("!help mortal");
						break;
					case HELP_CLIENT:
						textPane.appendPlain(About.getClientHelp(), Color.YELLOW);
						break;
					case HELP_ALIASES:
						textPane.appendPlain(About.getAliasesHelp(), Color.YELLOW);
						break;

					case HELP_CHANGES:
						textPane.appendPlain(About.changes(), Color.YELLOW);
						break;
					default:
						if (!loginCheck()) {
							return;
						}

						String topic = helpMap.get(item.getText());
						communicationThread.doAction("!" + topic);
						break;
				}
			}
		};

		menu.add(menuitem(HELP_CLIENT, actionListener));
		menu.add(menuitem(HELP_GETTING_STARTED, actionListener));
		menu.add(menuitem(HELP_ALIASES, actionListener));
		menu.add(menuitem(HELP_TOPICS, actionListener));
		menu.add(new JSeparator());

		ArrayList<String> topics = new ArrayList<>(helpMap.keySet());
		Collections.sort(topics);

		for (String topic : topics) {
			menu.add(menuitem(topic, actionListener));
		}

		menu.add(new JSeparator());
		menu.add(menuitem(HELP_CHANGES, actionListener));
		menu.add(menuitem(HELP_ABOUT, actionListener));
		return menu;
	}



	/*
	private Map getHelpMap() {
		int i = 1;

		HashMap data = new HashMap();

		while (true) {
			String helpTopic = getParameter("h_topic_" + i);

			if (helpTopic == null) {
				return data;
			}
			String helpcmd = getParameter("h_cmd_" + i);

			if (helpcmd == null) {
				return data;
			}

			data.put(helpTopic, helpcmd);

			i++;
		}
	}
	 */
}
