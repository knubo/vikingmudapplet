package no.knubo.mud.inventory;

import javax.swing.Icon;

import no.knubo.mud.ImageFactory;


public class Armour extends Item implements Cloneable {

	private final boolean worn;
	private final String armourType;

	public Armour(int id, boolean tagged, String shortdesc, int wornOut, char type,
			boolean worn, String armourType) {
		super(id, tagged, shortdesc, wornOut, type);
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
	
	public Icon getImageIcon() {
		if (armourType.equals("armour")) {
			return ImageFactory.getImageIcon("body.gif");
		} else if (armourType.equals("amulet")) {
			return ImageFactory.getImageIcon("amulet.gif");
		} else if (armourType.equals("helmet")) {
			return ImageFactory.getImageIcon("helmet.gif");
		} else if (armourType.equals("glove")) {
			return ImageFactory.getImageIcon("hands.gif");
		} else if (armourType.equals("cloak")) {
			return ImageFactory.getImageIcon("cloak.gif");
		} else if (armourType.equals("ring")) {
			return ImageFactory.getImageIcon("ring.gif");
		} else if (armourType.equals("boot")) {
			return ImageFactory.getImageIcon("boots.gif");
		} else if (armourType.equals("shield")) {
			return ImageFactory.getImageIcon("shield.gif");
		}
		return null;
	}
}
