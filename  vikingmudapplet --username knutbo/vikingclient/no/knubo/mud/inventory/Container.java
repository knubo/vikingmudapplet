package no.knubo.mud.inventory;

public class Container extends Item implements Cloneable {

	public Container(boolean tagged, String shortdesc, int wornOut, char type) {
		super(tagged, shortdesc, wornOut, type);

	}

	public Item copy() {
		try {
			return (Item) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
