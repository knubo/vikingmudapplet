package no.knubo.mud;

class About {
	final static String LATEST = "1.1";

	static String getClientHelp() {
		return "\nTo connect choose login as guest or just login from the game menu. The game does only allow for "
				+ "one guest user at at time so if there already is one present you can just log in and create "
				+ "a new character. Game commands are sent in the text field in the bottom.\n\n"
				+ "The input field support history accessible by the arrow keys. "
				+ "Command history is also available by sending the command #history. "
				+ "Repeats are supported by sending a "
				+ "command like '#3 smile', which will make you smile 3 times.\n";

	}
	public static String greetingText() {
		return "==========================================\n"
				+ "Viking Mud Online Client "
				+ LATEST
				+ "\n"
				+ "Created by vikingmud@knubo.no, alias Knubo.\n"
				+ "==========================================\n\n"
				+ "To play the game, select the game menu in the "
				+ "top and select 'Login as guest' or 'Just login'. " +
				" In the bottom there is a text line input where you write your commands." +
				" If you have never created a character before, " +
				"you may create a character. Just find an unused name while logging in."
				+ "\n\nSee menu Help -> Client for even more details.\n"
				+ "Be nice, have a good time, play one character at a time! (For more detailed rules, look at help rules).\n";
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

		return "\nChange history:\n" +
				"1.0 Converted application to Java stand alone client.\n" +
				"1.1 Added aliases support in settings.";
	}

	public static String getAliasesHelp() {
		return "\nAliases:\n" +
				"Viking mud have aliases support where you can create shortcuts\n" +
				"for commands. if you do alias gab get all from bag, you have\n" +
				"created an alias. Sending 'gab' to the mud, it will expand this to \n" +
				"get all from bag.\n\n" +
				"If you want to do several commands in sequence, there is no such\n" +
				"feature in the game. Though here the client\n" +
				"will help. The aliases you you add in the settings menu can do\n" +
				"several commands in sequence. If you add 'smile,smile,smile'\n" +
				"when typing #smile it will send the command smile 3 times to\n" +
				"the mud.";
	}
}
