package no.knubo.mud.inventory;

public class Weapon extends Item implements Cloneable {

	private final char wield;

	public Weapon(int id, boolean tagged, String shortdesc, int wornOut, char type,
			char wield, String image) {
		super(id, tagged, shortdesc, wornOut, type, image);
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
	
}
