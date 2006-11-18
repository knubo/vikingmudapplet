package no.knubo.mud.inventory;

public class Item implements Cloneable {
	
	private final boolean tagged;
	private final String shortdesc;
	private final int wornOut;
	private final char type;

	public Item(boolean tagged, String shortdesc, int wornOut, char type) {
		this.tagged = tagged;
		this.shortdesc = shortdesc;
		this.wornOut = wornOut;
		this.type = type;
		
	}
	
	public Item copy()  {
		try {
			return (Item) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
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
}
