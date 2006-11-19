package no.knubo.mud;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import no.knubo.mud.inventory.Armour;
import no.knubo.mud.inventory.Container;
import no.knubo.mud.inventory.Item;
import no.knubo.mud.inventory.Weapon;

public class Inventory extends JFrame
		implements
			TreeModel,
			TreeExpansionListener {

	private List inventory = new ArrayList(255);

	private List listeners = new LinkedList();

	private TreeRoot treeRoot = new TreeRoot();

	private JTree tree;

	private static final String ESC = String.valueOf((char) 27);

	private HashSet bagsOpen = new HashSet();

	Inventory() {
		tree = new JTree();
		tree.setModel(this);
		tree.setEditable(false);
		tree.setDoubleBuffered(true);
		tree.setRootVisible(false);
		tree.addTreeExpansionListener(this);
		getContentPane().add(tree);
	}

	public String readInventory(InputStream vikingIn, PrintStream vikingOut)
			throws IOException {

		/* Don't poll if not window is visible. */
		if (!isVisible()) {
			return "";
		}

		vikingOut.println("!ainvent");

		StringBuilder read = new StringBuilder();
		readData(vikingIn, read);

		int start = read.indexOf(ESC + "AINV");
		int end = read.indexOf(ESC + "END");

		parseInventory(read.substring(start + 5, end));

		notifyListeners();

		System.out.println("Updated inventory");

		return read.substring(0, start) + read.substring(end + 4);
	}

	private void notifyListeners() {

		for (Iterator i = listeners.iterator(); i.hasNext();) {
			TreeModelListener listener = (TreeModelListener) i.next();

			listener.treeStructureChanged(new TreeModelEvent(this,
					new Object[]{treeRoot}));
		}

		/* restore the blody selection afterwards. */
		for (Iterator i = bagsOpen.iterator(); i.hasNext();) {
			Item item = (Item) i.next();

			Object[] objs = new Object[]{treeRoot, item};
			TreePath path = new TreePath(objs);
			tree.expandPath(path);

		}
	}

	private void parseInventory(String string) {
		String[] lines = string.split("\r\n");

		inventory.clear();

		Container container = null;
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];

			if (line.length() == 0) {
				continue;
			}

			boolean inContainer = line.startsWith(">");
			if (inContainer) {
				line = line.substring(1);
			}

			String data[] = line.split(ESC);

			Item item = createItem(data);
			if (data[data.length - 1].startsWith("#")) {
				int count = Integer
						.parseInt(data[data.length - 1].substring(1));
				item.setCount(count);
			}

			if (item instanceof Container) {
				container = (Container) item;
			}

			if (inContainer) {
				if (container != null) {
					container.add(item);
				}
			} else {
				inventory.add(item);
			}
		}
	}
	private Item createItem(String[] data) {
		int id = Integer.parseInt(data[0]);
		boolean tagged = data[1].charAt(0) == '1';
		String shortdesc = data[1].substring(1);
		char type = data[2].charAt(0);
		int wornOut = Integer.parseInt(data[3]);
		boolean worn = false;
		char wield = 0;
		
		String armourType = null;

		switch (type) {
			case 'W' :
				if (data.length >= 5) {
					wield = data[4].charAt(0);
				}
				return new Weapon(id, tagged, shortdesc, wornOut, type, wield);
			case 'A' :
				worn = data[4].charAt(0) == '*';

				if (worn) {
					armourType = data[4].substring(1);
				} else {
					armourType = data[4];
				}

				return new Armour(id, tagged, shortdesc, wornOut, type, worn,
						armourType);
			case 'C' :
				return new Container(id, tagged, shortdesc, wornOut, type);
			default :
				return new Item(id, tagged, shortdesc, wornOut, type);

		}

	}
	private void readData(InputStream vikingIn, StringBuilder read)
			throws IOException {

		/* Read until ESC+END */
		while (read.indexOf(ESC + "END") == -1) {
			int available = vikingIn.available();
			byte[] bytes = new byte[available];
			int bc = vikingIn.read(bytes);

			read.append(new String(bytes, 0, bc));
		}
	}

	public void addTreeModelListener(TreeModelListener l) {
		listeners.add(l);
	}

	public Object getChild(Object parent, int index) {
		if (parent == treeRoot) {
			return inventory.get(index);
		}

		if (parent instanceof Container) {
			Container cont = (Container) parent;

			return cont.getItem(index);
		}

		return null;
	}

	public int getChildCount(Object parent) {
		if (parent == treeRoot) {
			System.out.println("Child count:" + inventory.size());
			return inventory.size();
		}

		if (parent instanceof Container) {
			Container cont = (Container) parent;

			return cont.getItemCount();
		}

		return 0;
	}

	public int getIndexOfChild(Object parent, Object child) {
		if (parent == treeRoot) {
			return inventory.indexOf(child);
		}
		if (parent instanceof Container) {
			Container cont = (Container) parent;

			return cont.getIndex(child);
		}
		return 0;
	}

	public Object getRoot() {
		return treeRoot;
	}

	public boolean isLeaf(Object node) {
		return node != treeRoot && !(node instanceof Container);
	}

	public void removeTreeModelListener(TreeModelListener l) {
		listeners.remove(l);
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
		System.out.println("HUM");
	}

	public String toString() {
		return "[Inventory]";
	}

	public void treeCollapsed(TreeExpansionEvent event) {
		TreePath path = event.getPath();

		Object[] objs = path.getPath();

		if (objs.length == 2) {
			bagsOpen.remove(objs[1]);
		}
	}

	public void treeExpanded(TreeExpansionEvent event) {
		TreePath path = event.getPath();

		Object[] objs = path.getPath();

		if (objs.length == 2) {
			bagsOpen.add(objs[1]);
		}

	}


}
