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
				+ "Viking Mud Online Client 0.9.1\n"
				+ "Created by knutbo@ifi.uio.no, alias Knubo.\n"
				+ "==========================================\n";
	}

	public static String aboutInfo() {
		return greetingText()
				+ "This code is released under the Gnu Public Lisence version 2 as described by "
				+ "http://www.gnu.org/licenses/gpl.txt.\n"
				+ "The code is hosted by Google Code at "
				+ "http://code.google.com/p/vikingmudapplet/ for your pleasure."
				+ "\n";
	}
}
