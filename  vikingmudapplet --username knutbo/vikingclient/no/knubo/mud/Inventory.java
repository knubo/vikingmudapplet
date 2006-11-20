package no.knubo.mud;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
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

	private JLabel nameLabel;

	private JLabel lvlAmount;

	private JLabel expAmount;

	private JLabel strAmount;

	private JLabel dexAmount;

	private JLabel intAmount;

	private JLabel conAmount;

	Inventory() {
		tree = new JTree();
		tree.setModel(this);
		tree.setEditable(false);
		tree.setDoubleBuffered(true);
		tree.setRootVisible(false);
		tree.addTreeExpansionListener(this);

		nameLabel = new JLabel();
		nameLabel.setFont(new Font("Lucida",Font.BOLD, 18));
		JLabel inventoryLabel = new JLabel("Inventory");
		JLabel statsLabel = new JLabel("Stats");

		JLabel lvlLabel = new JLabel("Level");
		JLabel expLabel = new JLabel("EXP");
		JLabel strLabel = new JLabel("STR");
		JLabel dexLabel = new JLabel("DEX");
		JLabel conLabel = new JLabel("CON");
		JLabel intLabel = new JLabel("INT");

		lvlAmount = new JLabel();
		expAmount = new JLabel();
		strAmount = new JLabel();
		dexAmount = new JLabel();
		intAmount = new JLabel();
		conAmount = new JLabel();

		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints displayConstraints = new GridBagConstraints();

		displayConstraints.gridx = 0;
		displayConstraints.gridy = 0;
		displayConstraints.anchor = GridBagConstraints.CENTER;
		displayConstraints.gridwidth = 3;
		gbl.setConstraints(nameLabel, displayConstraints);
		getContentPane().add(nameLabel);

		addLabel(inventoryLabel, gbl, displayConstraints, 2, 1);
		displayConstraints.insets = new Insets(0,0,0,20);
		addLabel(statsLabel, gbl, displayConstraints, 0, 1);
		addLabel(lvlLabel, gbl, displayConstraints, 0, 2);
		addLabel(expLabel, gbl, displayConstraints, 0, 3);
		addLabel(strLabel, gbl, displayConstraints, 0, 4);
		addLabel(dexLabel, gbl, displayConstraints, 0, 5);
		addLabel(conLabel, gbl, displayConstraints, 0, 6);
		addLabel(intLabel, gbl, displayConstraints, 0, 7);

		addLabel(lvlAmount, gbl, displayConstraints, 1, 2);
		addLabel(expAmount, gbl, displayConstraints, 1, 3);
		addLabel(strAmount, gbl, displayConstraints, 1, 4);
		addLabel(dexAmount, gbl, displayConstraints, 1, 5);
		addLabel(conAmount, gbl, displayConstraints, 1, 6);
		addLabel(intAmount, gbl, displayConstraints, 1, 7);

		displayConstraints.gridx = 2;
		displayConstraints.gridy = 2;
		displayConstraints.gridwidth = 1;
		displayConstraints.gridheight = 6;
		displayConstraints.anchor = GridBagConstraints.NORTHWEST;
		displayConstraints.fill = GridBagConstraints.BOTH;
		displayConstraints.weightx = 1;
		displayConstraints.weighty = 1;
		gbl.setConstraints(tree, displayConstraints);
		getContentPane().add(tree);
		
		setSize(400,400);
	}

	private void addLabel(JLabel label, GridBagLayout gbl,
			GridBagConstraints displayConstraints, int x, int y) {
		displayConstraints.gridx = x;
		displayConstraints.gridy = y;
		displayConstraints.anchor = GridBagConstraints.NORTHWEST;
		displayConstraints.gridwidth = 1;
		gbl.setConstraints(label, displayConstraints);
		getContentPane().add(label);
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

		boolean pickedFirst = false;

		Container container = null;
		for (int i = 1; i < lines.length; i++) {
			String line = lines[i];

			if (line.length() == 0) {
				continue;
			}
			
			if(!pickedFirst) {
				pickedFirst = true;
				setStats(line);
				continue;
			}
			/* First line are player stats */

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
	private void setStats(String string) {
		String[] stats = string.split(ESC);

		nameLabel.setText(stats[0]);
		lvlAmount.setText(stats[1]);
		expAmount.setText(stats[2]);
		// money 3
		// bank 4
		strAmount.setText(Integer.parseInt(stats[5])
				+ Integer.parseInt(stats[6]) + "(" + stats[5] + ")");
		dexAmount.setText(Integer.parseInt(stats[7])
				+ Integer.parseInt(stats[8]) + "(" + stats[7] + ")");
		intAmount.setText(Integer.parseInt(stats[9])
				+ Integer.parseInt(stats[10]) + "(" + stats[9] + ")");
		conAmount.setText(Integer.parseInt(stats[11])
				+ Integer.parseInt(stats[12]) + "(" + stats[11] + ")");

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
