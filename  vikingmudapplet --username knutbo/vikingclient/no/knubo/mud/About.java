package no.knubo.mud;

class About {

	static String getText() {
		return mainText()
				+ "\n\nUse edit field in the bottom to send commands to the mud. "
				+ "Use up and down arrows to access history of the commands that "
				+ "are sent.\n"
				+ "This client is written in Java. "
				+ "It is released under the Gnu Public Lisence version 2 as described by "
				+ "http://www.gnu.org/licenses/gpl.txt." + ""
				+ " The code is hosted by Google Code at "
				+ "http://code.google.com/p/vikingmudapplet/." + "\n";
	}

	public static String mainText() {
		return "=================================\n"
				+ "Viking Mud Online Client 0.9.\n"
				+ "Choose action from menu.\n"
				+ "Created by knutbo@ifi.uio.no\n"
				+ "=================================\n";
	}
}
