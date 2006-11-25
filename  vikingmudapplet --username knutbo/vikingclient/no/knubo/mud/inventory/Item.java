package no.knubo.mud.inventory;

import javax.swing.Icon;

public class Item implements Cloneable {

	private final boolean tagged;
	private final String shortdesc;
	private final int wornOut;
	private final char type;
	private int count;
	private final int id;

	public Item(int id, boolean tagged, String shortdesc, int wornOut, char type) {
		this.id = id;
		this.tagged = tagged;
		this.shortdesc = shortdesc;
		this.wornOut = wornOut;
		this.type = type;
	}

	public Item copy() {
		try {
			return (Item) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public String toString() {
		return (tagged ? "*" : "") + shortdesc
				+ (count > 1 ? "[" + count + "]" : "");
	}
	public String getShortdesc() {
		return shortdesc;
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
		return ((Item)obj).id == id;
	}

	public int hashCode() {
		return id * 41;
	}

	public Icon getImageIcon() {
		return null;
	}

}
