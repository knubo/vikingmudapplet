package no.knubo.mud.inventory;


public class Armour extends Item implements Cloneable {

	private final boolean worn;
	private final String armourType;

	public Armour(int id, boolean tagged, String shortdesc, int wornOut,
			char type, boolean worn, String armourType, String image) {
		super(id, tagged, shortdesc, wornOut, type, image);
		this.worn = worn;
		this.armourType = armourType;
	}

	public Item copy() {
		try {
			return (Item) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public String getArmourType() {
		return armourType;
	}

	public boolean isWorn() {
		return worn;
	}
}
