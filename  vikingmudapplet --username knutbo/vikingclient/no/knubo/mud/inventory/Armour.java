package no.knubo.mud.inventory;

public class Armour extends Item implements Cloneable {

	public Item copy() {
		try {
			return (Item) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
