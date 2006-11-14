package no.knubo.mud;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

class Aliases extends JFrame implements ActionListener, Alias {

	private JTextPane textPane;

	private HashMap aliases;

	Aliases() {
		textPane = new JTextPane();

		JScrollPane scrollPane = new JScrollPane(textPane);

		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);

		GridBagConstraints displayConstraints = new GridBagConstraints();
		displayConstraints.gridx = 0;
		displayConstraints.gridy = 0;
		displayConstraints.gridwidth = 1;
		displayConstraints.gridheight = 1;
		displayConstraints.anchor = GridBagConstraints.NORTHWEST;
		displayConstraints.fill = GridBagConstraints.BOTH;
		displayConstraints.weightx = 1;
		displayConstraints.weighty = 1;
		gbl.setConstraints(scrollPane, displayConstraints);
		getContentPane().add(scrollPane);

		displayConstraints.weightx = 0;
		displayConstraints.weighty = 0;
		displayConstraints.gridy = 1;
		displayConstraints.fill = GridBagConstraints.NONE;

		aliases = new HashMap();
		aliases.put("fun", "$#3smile;#3bounce");
		textPane.setText("fun=$#3smile;#3bounce");
		JButton okButton = new JButton("OK");

		gbl.setConstraints(okButton, displayConstraints);
		getContentPane().add(okButton);

		okButton.addActionListener(this);

		setTitle("Edit aliases");
		setSize(640, 400);

	}

	public void actionPerformed(ActionEvent arg0) {
		if (checkAliases()) {
			setVisible(false);
		}
	}
	
	/* (non-Javadoc)
	 * @see no.knubo.mud.Alias#getAlias(java.lang.String)
	 */
	public String getAlias(String action) {
		if(aliases == null) {
			return null;
		}
		return (String) aliases.get(action);
	}

	private boolean checkAliases() {
		String[] lines = textPane.getText().split("\n");
		if (lines == null) {
			return true;
		}
		aliases = new HashMap(lines.length);
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];

			int pos = line.indexOf('=');
			if (pos == -1) {
				JOptionPane.showMessageDialog(this,
						"Legal alias formats are alias=something. Line found was:\n"
								+ line);
				return false;
			}
			
			if(line.length() == pos+1) {
				JOptionPane.showMessageDialog(this,
						"Action missing for alias. Line found was:\n"
								+ line);
				return false;
			}
			
			aliases.put(line.substring(0, pos), line.substring(pos + 1));
		}

		return true;
	}
}
