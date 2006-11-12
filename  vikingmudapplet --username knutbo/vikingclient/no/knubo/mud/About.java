package no.knubo.mud;

class About {

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
				+ "Viking Mud Online Client 0.9\n"
				+ "Created by knutbo@ifi.uio.no, alias Knubo.\n"
				+ "==========================================\n";
	}

	public static String aboutInfo() {
		return greetingText()
				+ "\nThis code is released under the Gnu Public Lisence version 2 as described by "
				+ "http://www.gnu.org/licenses/gpl.txt.\n"
				+ "The code is hosted by Google Code at "
				+ "http://code.google.com/p/vikingmudapplet/ for your pleasure."
				+ "\n" + changes();
	}

	public static String changes() {
		return "\nChange history:\n"
				+ "0.10 Added showing of current font in menu (also for other fonts if you have done so).\n"
				+ "0.9 Added applet parameter FONT_NAME and FONT_SIZE. Font size must be an integer.\n"
				+ "0.8 Changed font to Courier New on request.\n"
				+ "0.7 Added discard of first two commands to history when connecting. Renumbered version numbers\n"
				+ "0.6 Started tracking changes in about.\n"
				+ "0.5 Added some more documentation.\n"
				+ "0.4 Added font size change.\n"
				+ "0.3 Added command repeats.\n"
				+ "0.2 Added history. Removed telnet noise upon login.\n"
				+ "0.1 Created applet.";
	}
}
