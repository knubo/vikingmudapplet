package no.knubo.mud.inventory;

import java.awt.Color;

import javax.swing.Icon;

import no.knubo.mud.ImageFactory;

public class Item implements Cloneable {

	private final boolean tagged;
	private final String shortdesc;
	private final int wornOut;
	private final char type;
	private int count;
	private final int id;

	final static Color[] wornOutCols = {Color.WHITE,
			new Color(0xCC, 0xCC, 0xCC),
			new Color(0xAA, 0xCC, 0xAA),
			new Color(0x80, 0x80, 0x80),
			new Color(0x60, 0x60, 0x60),
			new Color(0x30, 0x30, 0x30),
			Color.BLACK,
			};
	private final String image;

	public Item(int id, boolean tagged, String shortdesc, int wornOut, char type, String image) {
		this.id = id;
		this.tagged = tagged;
		this.shortdesc = shortdesc;
		this.wornOut = wornOut;
		this.type = type;
		this.image = image;
	}

	public Item copy() {
		try {
			return (Item) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public String toString() {
		return getShortdesc() + (count > 1 ? "[" + count + "]" : "");
	}

	public String getShortdesc() {
		if (shortdesc.length() < 2) {
			return shortdesc.toUpperCase();
		}
		return shortdesc.substring(0, 1).toUpperCase() + shortdesc.substring(1);
	}

	public boolean isTagged() {
		return tagged;
	}

	public char getType() {
		return type;
	}

	public int getWornOut() {
		return wornOut;
	}

	public void setCount(int c) {
		this.count = c;
	}

	public int getCount() {
		return count;
	}

	public boolean hasSameShort(Item n) {
		return n.getType() == getType() && n.isTagged() == isTagged()
				&& n.getShortdesc().equals(getShortdesc());
	}

	public boolean equals(Object obj) {
		return ((Item) obj).id == id;
	}

	public int hashCode() {
		return id * 41;
	}

	public Icon getImageIcon() {
		return ImageFactory.getImageIcon(image, isTagged());
	}

	public Color colorWornOut() {

		return wornOutCols[wornOut];
	}

}
