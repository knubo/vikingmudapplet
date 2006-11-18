package no.knubo.mud;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

class Inventory extends JFrame {

	private List inventory = new ArrayList(255);

	private static final String ESC = String.valueOf((char) 27);

	public String readInventory(InputStream vikingIn, PrintStream vikingOut)
			throws IOException {

		/* Don't poll if not window is visible. */
		if (!isVisible()) {
			return "";
		}

		vikingOut.println("!ainvent");

		StringBuilder read = new StringBuilder();
		readData(vikingIn, read);

		int start = read.indexOf(ESC + "AINV");
		int end = read.indexOf(ESC + "END");

		parseInventory(read.substring(start + 5, end));

		return read.substring(0, start) + read.substring(end + 4);
	}

	private void parseInventory(String string) {
		String[] lines = string.split("\n");

		inventory.clear();

		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];

			String data[] = line.split(ESC);

			Item item = createItem(data);
			inventory.add(item);
		}

	}

	private Item createItem(String[] data) {
		boolean tagged = data[0].charAt(0) == '1';
		String shortdesc = data[0].substring(1);
		char type = data[1].charAt(0);
		int wornOut = Integer.parseInt(data[2]);
		boolean worn = false;
		char wield = 0;
		String armourType = null;
		
		switch (type) {
			case 'W' :
				if (data[3].length() > 0) {
					wield = data[3].charAt(0);
				}
				break;
			case 'A' :
				worn = data[3].charAt(0) == '*';

				if (worn) {
					armourType = data[3].substring(1);
				} else {
					armourType = data[3];
				}
				break;
		}
		return null;
	}

	private void readData(InputStream vikingIn, StringBuilder read)
			throws IOException {

		/* Read until ESC+END */
		while (read.indexOf(ESC + "END") == -1) {
			int available = vikingIn.available();
			byte[] bytes = new byte[available];
			int bc = vikingIn.read(bytes);

			read.append(new String(bytes, 0, bc));
		}
	}
}
