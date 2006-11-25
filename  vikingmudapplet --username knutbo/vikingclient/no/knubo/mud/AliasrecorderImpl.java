package no.knubo.mud;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

class AliasrecorderImpl extends JFrame
		implements
			TableModel,
			ActionListener,
			Aliasrecorder {

	private JTable table;

	private ArrayList recordedLines = new ArrayList(255);

	private boolean isrecording = false;

	private JTextField aliasName;

	private LinkedList listeners = new LinkedList();

	private final Alias alias;

	AliasrecorderImpl(Alias alias) {
		this.alias = alias;
		setTitle("Alias recorder");

		table = new JTable(this);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setOpaque(true);
		JScrollPane scrollPane = new JScrollPane(table);

		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);

		GridBagConstraints displayConstraints = new GridBagConstraints();
		displayConstraints.gridx = 0;
		displayConstraints.gridy = 0;
		displayConstraints.gridwidth = 3;
		displayConstraints.gridheight = 1;
		displayConstraints.insets = new Insets(3, 3, 3, 3);
		displayConstraints.anchor = GridBagConstraints.NORTHWEST;
		displayConstraints.fill = GridBagConstraints.BOTH;
		displayConstraints.weightx = 1;
		displayConstraints.weighty = 1;
		gbl.setConstraints(scrollPane, displayConstraints);
		getContentPane().add(scrollPane);

		JButton start = new JButton("Start");
		start.addActionListener(this);
		displayConstraints.gridx = 0;
		displayConstraints.gridy = 1;
		displayConstraints.gridheight = 1;
		displayConstraints.gridwidth = 1;
		displayConstraints.weightx = 0;
		displayConstraints.weighty = 0;
		displayConstraints.fill = GridBagConstraints.NONE;
		gbl.setConstraints(start, displayConstraints);
		getContentPane().add(start);

		JButton stopp = new JButton("Stop");
		stopp.addActionListener(this);
		displayConstraints.gridx = 1;
		displayConstraints.gridy = 1;
		gbl.setConstraints(stopp, displayConstraints);
		getContentPane().add(stopp);

		JButton clear = new JButton("Clear");
		clear.addActionListener(this);
		displayConstraints.gridx = 2;
		displayConstraints.gridy = 1;
		gbl.setConstraints(clear, displayConstraints);
		getContentPane().add(clear);

		JButton insert = new JButton("Insert line");
		insert.addActionListener(this);
		displayConstraints.gridx = 0;
		displayConstraints.gridy = 2;
		gbl.setConstraints(insert, displayConstraints);
		getContentPane().add(insert);

		JButton delete = new JButton("Delete line");
		delete.addActionListener(this);
		displayConstraints.gridx = 1;
		displayConstraints.gridy = 2;
		gbl.setConstraints(delete, displayConstraints);
		getContentPane().add(delete);

		JButton reverse = new JButton("Add reversed");
		reverse.addActionListener(this);
		displayConstraints.gridx = 2;
		displayConstraints.gridy = 2;
		gbl.setConstraints(reverse, displayConstraints);
		getContentPane().add(reverse);

		JLabel label = new JLabel("Alias");
		displayConstraints.gridx = 0;
		displayConstraints.gridy = 3;
		displayConstraints.anchor = GridBagConstraints.EAST;
		gbl.setConstraints(label, displayConstraints);
		getContentPane().add(label);

		aliasName = new JTextField();
		displayConstraints.gridx = 1;
		displayConstraints.ipadx = 0;
		displayConstraints.gridy = 3;
		displayConstraints.anchor = GridBagConstraints.WEST;
		displayConstraints.fill = GridBagConstraints.HORIZONTAL;
		gbl.setConstraints(aliasName, displayConstraints);
		getContentPane().add(aliasName);

		JButton create = new JButton("Create alias");
		create.addActionListener(this);
		displayConstraints.gridx = 2;
		displayConstraints.gridwidth = 1;
		displayConstraints.gridy = 3;
		displayConstraints.fill = GridBagConstraints.NONE;
		gbl.setConstraints(create, displayConstraints);
		getContentPane().add(create);

		setSize(350, 400);
	}

	public void addCommand(String command) {
		if (isrecording) {

			recordedLines.add(command);
			notifyInsert(recordedLines.size() - 1);
		}
	}

	public void addTableModelListener(TableModelListener arg0) {
		listeners.add(arg0);
	}

	public Class getColumnClass(int arg0) {
		return String.class;
	}

	public int getColumnCount() {
		return 1;
	}

	public String getColumnName(int arg0) {
		return "Commands ";
	}

	public int getRowCount() {
		return recordedLines.size();
	}

	public Object getValueAt(int row, int col) {
		return recordedLines.get(row);
	}

	public boolean isCellEditable(int arg0, int arg1) {
		return true;
	}

	public void removeTableModelListener(TableModelListener arg0) {
		listeners.remove(arg0);
	}

	public void setValueAt(Object arg0, int row, int col) {
		recordedLines.set(row, arg0);
	}

	void notify_listeners(TableModelEvent event) {
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			TableModelListener tml = (TableModelListener) i.next();
			tml.tableChanged(event);
		}
	}

	public static void main(String[] args) {
		AliasrecorderImpl f = new AliasrecorderImpl(null);
		f.isrecording = true;
		f.addCommand("s");
		f.addCommand("n");
		f.addWindowListener(new WindowAdapter() {

			public void windowClosed(WindowEvent e) {
				System.exit(0);
			}

		});
		f.setVisible(true);
	}

	public void actionPerformed(ActionEvent arg0) {
		JButton button = (JButton) arg0.getSource();
		if (button.getText().equals("Start")) {
			isrecording = true;
			setTitle("Alias recorder (recording)");
		} else if (button.getText().equals("Stop")) {
			isrecording = false;
			setTitle("Alias recorder");
		} else if (button.getText().equals("Clear")) {
			clearAll();
		} else if (button.getText().equals("Insert line")) {
			int row = table.getSelectedRow();
			if (row == -1) {
				recordedLines.add("nop");
				notifyInsert(recordedLines.size() - 1);
			} else {
				recordedLines.add(row + 1, "nop");
				notifyInsert(row + 1);
			}
		} else if (button.getText().equals("Delete line")) {
			int selectedRow = table.getSelectedRow();

			if (selectedRow >= 0) {
				recordedLines.remove(selectedRow);
				notifyRemove(selectedRow);
			}
		} else if (button.getText().equals("Create alias")) {
			doCreateAlias();
		} else if (button.getText().equals("Add reversed")) {
			ArrayList al = new ArrayList(recordedLines);
			Collections.reverse(al);
			recordedLines.addAll(al);
			notify_listeners(new TableModelEvent(this));
		}

	}

	private void clearAll() {
		recordedLines.clear();
		notify_listeners(new TableModelEvent(this));
	}

	private void notifyInsert(int row) {

		notify_listeners(new TableModelEvent(this, row, row, 1,
				TableModelEvent.INSERT));
	}
	private void notifyRemove(int row) {
		notify_listeners(new TableModelEvent(this, row, row, 1,
				TableModelEvent.DELETE));
	}

	private void doCreateAlias() {
		List compressedList = recordedLines;

		String name = this.aliasName.getText();

		name = name.trim();

		if (name.length() == 0) {
			JOptionPane.showMessageDialog(this, "Please provide alias name.");
			return;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("$");
		for (Iterator i = compressedList.iterator(); i.hasNext();) {
			String one = (String) i.next();
			sb.append(one);
			if (i.hasNext()) {
				sb.append(";");
			}
		}
		if (!alias.addAlias(name, sb.toString(), true)) {
			JOptionPane
					.showMessageDialog(this,
							"Provided alias name is already used, please use some other one.");
			return;
		}
		clearAll();
		aliasName.setText("");
	}

}
