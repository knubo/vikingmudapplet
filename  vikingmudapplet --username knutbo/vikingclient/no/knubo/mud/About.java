package no.knubo.mud;

class About {
	final static String LATEST = "0.17";
	static String getClientHelp() {
		return "\nTo connect choose login as guest or just login from the game menu. The game does only allow for "
				+ "one guest user at at time so if there already is one present you can just log in and create "
				+ "a new character. Game commands are sent in the text field in the bottom.\n\n"
				+ "The input field support history accessible by the arrow keys. "
				+ "Command history is also available by sending the command #history. "
				+ "Repeats are supported by sending a "
				+ "command like '#3 smile', which will make you smile 3 times.";

	}
	public static String greetingText() {
		return "==========================================\n"
				+ "Viking Mud Online Client " + LATEST + "\n"
				+ "Created by knutbo@ifi.uio.no, alias Knubo.\n"
				+ "==========================================\n\n"
				+ "To play the game, select the game menu in the "
				+ "top and select 'Login as guest' or 'Just login'. "
				+ "See menu Help -> Client for even more details.\n";
	}

	public static String aboutInfo() {
		return greetingText()
				+ "\nThis code is released under the Gnu Public Lisence version 2 as described by "
				+ "http://www.gnu.org/licenses/gpl.txt.\n"
				+ "The code is hosted by Google Code at "
				+ "http://code.google.com/p/vikingmudapplet/ for your pleasure."
				+ "\n";
	}

	public static String changes() {

		return "\nChange history:\n"
				+ LATEST
				+ " Added colour menu to make it easier to get colours.\n"
				+ "0.16 Added alias support.\n"
				+ "0.15 Added splitting of commands by using the ; sign if the command starts with $.\n"
				+ "0.14 Font menu completed - picks out monospaced fonts from users computer.\n"
				+ "0.13 Added emacs like keybindings like - ctrl-a, ctrl-e and ctrl-k.\n"
				+ "0.12 Fixed colouring methods - simpler and correct now. Removed scrollbar for input fields and fixed size issues.\n"
				+ "0.11 Same font for input area as for textarea. Changed font for menu.\n"
				+ "0.10 Added showing of current font in menu (also for other fonts if you have done so).\n"
				+ "0.9 Added applet parameter FONT_NAME and FONT_SIZE. Font size must be an integer.\n"
				+ "0.8 Changed font to Courier New on request.\n"
				+ "0.7 Added discard of first two commands to history when connecting. Renumbered version numbers\n"
				+ "0.6 Started tracking changes in about.\n"
				+ "0.5 Added some more documentation.\n"
				+ "0.4 Added font size change.\n"
				+ "0.3 Added command repeats.\n"
				+ "0.2 Added history. Removed telnet noise upon login.\n"
				+ "0.1 Created applet.\n";
	}
}
