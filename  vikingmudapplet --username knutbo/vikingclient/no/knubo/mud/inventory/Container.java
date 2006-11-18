package no.knubo.mud.inventory;

import java.util.LinkedList;
import java.util.List;

public class Container extends Item implements Cloneable {

	List contains = new LinkedList();

	public Container(boolean tagged, String shortdesc, int wornOut, char type ) {
		super(tagged, shortdesc, wornOut, type);
	}

	public Item copy() {
		try {
			return (Item) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public void add(Item item) {
		contains.add(item);
	}
	
	public Object getItem(int index) {
		return contains.get(index);
	}

	public int getItemCount() {
		return contains.size();
	}

	public int getIndex(Object child) {
		return contains.indexOf(child);
	}

}
