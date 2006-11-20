package no.knubo.mud;

import java.awt.Color;
import java.awt.Font;

import javax.swing.UIManager;

public class UIStuff {

	public static void setupUI() {
		Font menuFont = new Font("Verdana", Font.PLAIN, 12);
		UIManager.put("Menu.font", menuFont);
		UIManager.put("MenuItem.font", menuFont);
		UIManager.put("CheckBoxMenuItem.font", menuFont);

		// Color offwhite = new Color(0xEE,0xEE,0xEE);

		Color bgPanel = new Color(0xCC, 0x33, 0x00);
		Color bgColor = new Color(0x99, 0x00, 0x00);
		UIManager.put("Button.background", bgColor);
		UIManager.put("Panel.background", bgColor);

		UIManager.put("TextPane.background", bgPanel);
		UIManager.put("TextPane.foreground", new Color(0xFF, 0xFF, 0xCC));

		UIManager.put("Label.foreground", Color.white);

		UIManager.put("Tree.background", bgColor);
		UIManager.put("Tree.textBackground", bgColor);
		UIManager.put("Tree.textForeground", Color.white);

		UIManager.put("Table.background", bgPanel);
		UIManager.put("Table.foreground", Color.white);
		UIManager.put("Viewport.background",bgPanel);
		
//		UIManager.put("TableHeader.background", bgColor);
//		UIManager.put("TableHeader.foreground", Color.white);
		
		UIManager.put("MenuBar.background", bgColor);
		UIManager.put("Menu.background", bgColor);
		UIManager.put("MenuItem.background", bgColor);
		UIManager.put("CheckBoxMenuItem.background", bgColor);
		UIManager.put("Menu.foreground", Color.white);
		UIManager.put("MenuItem.foreground", Color.white);
		UIManager.put("CheckBoxMenuItem.foreground", Color.white);
		
		
	}

}
