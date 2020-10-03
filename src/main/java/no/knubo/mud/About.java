package no.knubo.mud;

class About {
	final static String LATEST = "1.0";
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

		return "\nChange history:\n"+
				"1.0 Converted application to Java stand alone client.";
	}
}
