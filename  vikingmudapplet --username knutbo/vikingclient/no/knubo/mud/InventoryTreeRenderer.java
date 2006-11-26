package no.knubo.mud;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import no.knubo.mud.inventory.Item;

public class InventoryTreeRenderer extends DefaultTreeCellRenderer {

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);

		if (value instanceof Item) {
			Item item = (Item) value;
			setIcon(item.getImageIcon());
			
			setForeground(item.colorWornOut());
		 } else {
			 setIcon(null);
		}
		return this;
	}
}
