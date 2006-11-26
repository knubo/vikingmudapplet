package no.knubo.mud;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.JScrollPane;
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

	private JLabel moneyAmount;

	private JLabel bankAmount;

	private JLabel headAmount;

	private JLabel neckAmount;

	private JLabel backAmount;

	private JLabel bodyAmount;

	private JLabel handsAmount;

	private JLabel fingerAmount;

	private JLabel feetAmount;

	private JLabel shieldAmount;

	private JLabel leftAmount;

	private JLabel rightAmount;

	private JLabel playerIconLabel;

	Inventory() {
		tree = new JTree();
		tree.setModel(this);
		tree.setEditable(false);
		tree.setDoubleBuffered(true);
		tree.setRootVisible(false);
		tree.setCellRenderer(new InventoryTreeRenderer());

		nameLabel = new JLabel("   ");
		nameLabel.setFont(new Font("Lucida", Font.BOLD, 18));
		nameLabel.setForeground(Color.yellow);
		JLabel inventoryLabel = new JLabel("Inventory");
		JLabel statsLabel = new JLabel("Stats");
		JLabel equipLabel = new JLabel("Equipment");
		statsLabel.setForeground(Color.green);
		inventoryLabel.setForeground(Color.green);
		equipLabel.setForeground(Color.green);

		Font statFont = new Font("Lucida", Font.BOLD, 14);
		Font otherFont = new Font("Lucida", Font.BOLD, 12);
		statsLabel.setFont(statFont);
		inventoryLabel.setFont(statFont);
		equipLabel.setFont(statFont);

		lvlAmount = newLabel(otherFont);
		expAmount = newLabel(otherFont);
		strAmount = newLabel(otherFont);
		dexAmount = newLabel(otherFont);
		intAmount = newLabel(otherFont);
		conAmount = newLabel(otherFont);
		moneyAmount = newLabel(otherFont);
		bankAmount = newLabel(otherFont);
		zeroEQDescs(otherFont);

		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints displayConstraints = new GridBagConstraints();

		displayConstraints.fill = GridBagConstraints.NONE;
		displayConstraints.gridx = 0;
		displayConstraints.gridy = 0;
		displayConstraints.anchor = GridBagConstraints.CENTER;
		displayConstraints.gridwidth = 4;
		gbl.setConstraints(nameLabel, displayConstraints);
		displayConstraints.insets = new Insets(0, 0, 15, 0);
		getContentPane().add(nameLabel);
		displayConstraints.insets = new Insets(0, 0, 0, 0);

		displayConstraints.anchor = GridBagConstraints.NORTHWEST;
		displayConstraints.gridwidth = 1;

		addLabel(equipLabel, gbl, displayConstraints, 2, 10);

		displayConstraints.insets = new Insets(0, 10, 0, 20);
		addLabel(inventoryLabel, gbl, displayConstraints, 0, 10);
		addLabel(statsLabel, gbl, displayConstraints, 0, 1);
		addLabel(newLabel("Level", otherFont), gbl, displayConstraints, 0, 2);
		addLabel(newLabel("Experience", otherFont), gbl, displayConstraints, 0,
				3);
		addLabel(newLabel("Money", otherFont), gbl, displayConstraints, 0, 4);
		addLabel(newLabel("Bank", otherFont), gbl, displayConstraints, 0, 5);
		addLabel(newLabel("Strength", otherFont), gbl, displayConstraints, 0, 6);
		addLabel(newLabel("Dexterity", otherFont), gbl, displayConstraints, 0,
				7);
		addLabel(newLabel("Constitution", otherFont), gbl, displayConstraints,
				0, 8);
		addLabel(newLabel("Intelligence", otherFont), gbl, displayConstraints,
				0, 9);

		displayConstraints.insets = new Insets(0, 0, 0, 0);
		addLabel(newLabel("Head", otherFont), gbl, displayConstraints, 2, 11);
		addLabel(newLabel("Neck", otherFont), gbl, displayConstraints, 2, 12);
		addLabel(newLabel("Back", otherFont), gbl, displayConstraints, 2, 13);
		addLabel(newLabel("Body", otherFont), gbl, displayConstraints, 2, 14);
		addLabel(newLabel("Hands", otherFont), gbl, displayConstraints, 2, 15);
		addLabel(newLabel("Finger", otherFont), gbl, displayConstraints, 2, 16);
		addLabel(newLabel("Feet", otherFont), gbl, displayConstraints, 2, 17);
		addLabel(newLabel("Shield", otherFont), gbl, displayConstraints, 2, 18);
		addLabel(newLabel("Right arm", otherFont), gbl, displayConstraints, 2,
				19);
		addLabel(newLabel("Left arm", otherFont), gbl, displayConstraints, 2,
				20);

		addLabel(lvlAmount, gbl, displayConstraints, 1, 2);
		addLabel(expAmount, gbl, displayConstraints, 1, 3);
		addLabel(moneyAmount, gbl, displayConstraints, 1, 4);
		addLabel(bankAmount, gbl, displayConstraints, 1, 5);
		addLabel(strAmount, gbl, displayConstraints, 1, 6);
		addLabel(dexAmount, gbl, displayConstraints, 1, 7);
		addLabel(conAmount, gbl, displayConstraints, 1, 8);
		addLabel(intAmount, gbl, displayConstraints, 1, 9);

		displayConstraints.insets = new Insets(0, 0, 0, 10);
		addLabel(headAmount, gbl, displayConstraints, 3, 11);
		addLabel(neckAmount, gbl, displayConstraints, 3, 12);
		addLabel(backAmount, gbl, displayConstraints, 3, 13);
		addLabel(bodyAmount, gbl, displayConstraints, 3, 14);
		addLabel(handsAmount, gbl, displayConstraints, 3, 15);
		addLabel(fingerAmount, gbl, displayConstraints, 3, 16);
		addLabel(feetAmount, gbl, displayConstraints, 3, 17);
		addLabel(shieldAmount, gbl, displayConstraints, 3, 18);
		addLabel(rightAmount, gbl, displayConstraints, 3, 19);
		addLabel(leftAmount, gbl, displayConstraints, 3, 20);

		displayConstraints.gridheight = 9;
		displayConstraints.gridwidth = 3;
		displayConstraints.anchor = GridBagConstraints.NORTHEAST;
		playerIconLabel = new JLabel();
		displayConstraints.insets = new Insets(0, 0, 0, 10);
		addLabel(playerIconLabel, gbl, displayConstraints, 2, 1);

		displayConstraints.gridheight = 1;

		displayConstraints.gridx = 0;
		displayConstraints.gridy = 11;
		displayConstraints.gridwidth = 2;
		displayConstraints.gridheight = 12;
		displayConstraints.anchor = GridBagConstraints.NORTHWEST;
		displayConstraints.fill = GridBagConstraints.BOTH;
		displayConstraints.weightx = 1;
		displayConstraints.weighty = 1;

		JScrollPane scrollPane = new JScrollPane(tree);
		gbl.setConstraints(scrollPane, displayConstraints);
		getContentPane().add(scrollPane);

		setSize(400, 400);
	}

	private JLabel newLabel(String string, Font otherFont) {
		JLabel label = new JLabel(string);
		label.setFont(otherFont);
		return label;
	}

	private JLabel newLabel(Font otherFont) {
		JLabel label = new JLabel("  ");
		label.setFont(otherFont);
		return label;
	}

	private void zeroEQDescs(Font otherFont) {
		headAmount = newLabel(otherFont);
		neckAmount = newLabel(otherFont);
		backAmount = newLabel(otherFont);
		bodyAmount = newLabel(otherFont);
		handsAmount = newLabel(otherFont);
		fingerAmount = newLabel(otherFont);
		feetAmount = newLabel(otherFont);
		shieldAmount = newLabel(otherFont);
		leftAmount = newLabel(otherFont);
		rightAmount = newLabel(otherFont);
	}

	private void addLabel(JLabel label, GridBagLayout gbl,
			GridBagConstraints displayConstraints, int x, int y) {
		displayConstraints.gridx = x;
		displayConstraints.gridy = y;
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
		// zeroEQDescs();

		boolean pickedFirst = false;

		Container container = null;
		for (int i = 1; i < lines.length; i++) {
			String line = lines[i];

			if (line.length() == 0) {
				continue;
			}

			if (!pickedFirst) {
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
				showAsEQ(item);
				inventory.add(item);
			}
		}
	}
	private void showAsEQ(Item item) {
		if (item instanceof Weapon) {
			Weapon wep = (Weapon) item;

			switch (wep.getWield()) {
				case 'L' :
					leftAmount.setText(limit20(wep.getShortdesc()));
					break;
				case 'B' :
					/* Fallthrough */
				case 'R' :
					rightAmount.setText(limit20(wep.getShortdesc()));
					break;
			}
		}

		if (item instanceof Armour) {
			Armour arm = (Armour) item;

			if (!arm.isWorn()) {
				return;
			}

			String shortdesc = arm.getShortdesc();

			shortdesc = limit20(shortdesc);
			String armourType = arm.getArmourType();

			if (armourType.equals("armour")) {
				bodyAmount.setText(shortdesc);
			} else if (armourType.equals("amulet")) {
				neckAmount.setText(shortdesc);
			} else if (armourType.equals("helmet")) {
				headAmount.setText(shortdesc);
			} else if (armourType.equals("glove")) {
				handsAmount.setText(shortdesc);
			} else if (armourType.equals("cloak")) {
				backAmount.setText(shortdesc);
			} else if (armourType.equals("ring")) {
				fingerAmount.setText(shortdesc);
			} else if (armourType.equals("boot")) {
				feetAmount.setText(shortdesc);
			} else if (armourType.equals("shield")) {
				shieldAmount.setText(shortdesc);
			}
		}
	}

	private String limit20(String shortdesc) {
		if (shortdesc.length() > 20) {
			return shortdesc.substring(0, 17) + "...";
		}
		return shortdesc;
	}

	private void setStats(String string) {
		String[] stats = string.split(ESC);

		nameLabel.setText(stats[0]);
		lvlAmount.setText(stats[1]);
		expAmount.setText(stats[2]);
		moneyAmount.setText(stats[3]);
		bankAmount.setText(stats[4]);
		strAmount.setText(Integer.parseInt(stats[5])
				+ Integer.parseInt(stats[6]) + "(" + stats[5] + ")");
		dexAmount.setText(Integer.parseInt(stats[7])
				+ Integer.parseInt(stats[8]) + "(" + stats[7] + ")");
		intAmount.setText(Integer.parseInt(stats[9])
				+ Integer.parseInt(stats[10]) + "(" + stats[9] + ")");
		conAmount.setText(Integer.parseInt(stats[11])
				+ Integer.parseInt(stats[12]) + "(" + stats[11] + ")");

		playerIconLabel.setIcon(ImageFactory.getImageIcon(stats[13]));
		playerIconLabel.repaint();
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

	public static void main(String[] args) {
		UIStuff.setupUI();
		Inventory f = new Inventory();
		f.setVisible(true);

		f.nameLabel.setText("Knubo");
		f.lvlAmount.setText("40");
		f.dexAmount.setText("29");
		f.intAmount.setText("29");
		f.conAmount.setText("29");
		f.strAmount.setText("29");
		f.expAmount.setText("290101");
		f.moneyAmount.setText("424242");
		f.bankAmount.setText("424242");
		f.addWindowListener(new WindowAdapter() {

			public void windowClosed(WindowEvent e) {
				System.exit(0);
			}

		});
	}

}
