package no.knubo.mud.inventory;

import javax.swing.Icon;

import no.knubo.mud.ImageFactory;


public class Weapon extends Item implements Cloneable {

	private final char wield;

	public Weapon(int id, boolean tagged, String shortdesc, int wornOut, char type,
			char wield) {
		super(id, tagged, shortdesc, wornOut, type);
		this.wield = wield;

	}

	public Item copy() {
		try {
			return (Item) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public char getWield() {
		return wield;
	}
	
	public Icon getImageIcon() {
		return ImageFactory.getImageIcon("sword.gif");
	}
}
