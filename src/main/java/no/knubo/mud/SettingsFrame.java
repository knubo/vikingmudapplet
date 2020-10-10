package no.knubo.mud;

import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.*;

public class SettingsFrame extends JFrame {

    DefaultTableModel charsAndPassword = new DefaultTableModel(0, 2);
    DefaultTableModel aliases = new DefaultTableModel(0, 2);

    int gridY = 0;

    void loadSettings() {
        String home = System.getProperty("user.home");
        File file = new File(home + "/.vikingmud");

        if (!file.canRead()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            while (line != null) {
                if (line.startsWith("C:")) {
                    String[] data = line.substring(2).split("=");
                    charsAndPassword.insertRow(charsAndPassword.getRowCount(), data);
                }
                if(line.startsWith("A:")) {
                    String[] data = line.substring(2).split("=");
                    aliases.insertRow(aliases.getRowCount(), data);
                }
                line = br.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SettingsFrame(MainWindow mainWindow) {
        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);

        loadSettings();

        GridBagConstraints displayConstraints = new GridBagConstraints();
        displayConstraints.gridx = 0;
        displayConstraints.gridy = gridY++;
        displayConstraints.gridwidth = 1;
        displayConstraints.gridheight = 1;
        displayConstraints.anchor = GridBagConstraints.NORTHWEST;
        displayConstraints.fill = GridBagConstraints.BOTH;
        displayConstraints.weightx = 1;
        displayConstraints.weighty = 1;

        createCharTable(gbl, displayConstraints);
        createAliasTable(gbl, displayConstraints);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(c -> {
            saveFunction();
            mainWindow.makeMenu();
        });

        displayConstraints.gridy = gridY++;
        gbl.setConstraints(saveButton, displayConstraints);
        this.add(saveButton);

        this.setTitle("Settings");
        this.pack();
        setBounds(0, 0, 400, 400);

    }

    @SuppressWarnings("DuplicatedCode")
    private void createAliasTable(GridBagLayout gbl, GridBagConstraints displayConstraints) {
        DefaultTableColumnModel columns = new DefaultTableColumnModel();
        TableColumn column = new TableColumn(0);
        column.setHeaderValue("Alias");
        columns.addColumn(column);
        TableColumn column1 = new TableColumn(1);
        column1.setHeaderValue("Value");
        columns.addColumn(column1);

        JTable charsTable = new JTable(aliases, columns);

        JScrollPane pane = new JScrollPane(charsTable);

        displayConstraints.fill = GridBagConstraints.BOTH;
        displayConstraints.gridy = gridY++;
        displayConstraints.weightx = 1;
        displayConstraints.weighty = 1;

        gbl.setConstraints(pane, displayConstraints);

        this.add(pane);

        displayConstraints.weightx = 0;
        displayConstraints.weighty = 0;
        displayConstraints.fill = GridBagConstraints.NONE;

        JButton addButton = new JButton("New alias");
        addButton.addActionListener(c -> {
            aliases.insertRow(aliases.getRowCount(), new String[]{"", ""});
        });
        displayConstraints.gridy = gridY++;
        gbl.setConstraints(addButton, displayConstraints);

        this.add(addButton);
    }

    @SuppressWarnings("DuplicatedCode")
    private void createCharTable(GridBagLayout gbl, GridBagConstraints displayConstraints) {
        DefaultTableColumnModel columns = new DefaultTableColumnModel();
        TableColumn column = new TableColumn(0);
        column.setHeaderValue("Character");
        columns.addColumn(column);
        TableColumn column1 = new TableColumn(1);
        column1.setHeaderValue("Password");
        columns.addColumn(column1);

        JTable charsTable = new JTable(charsAndPassword, columns);

        JScrollPane pane = new JScrollPane(charsTable);
        gbl.setConstraints(pane, displayConstraints);

        this.add(pane);

        displayConstraints.weightx = 0;
        displayConstraints.weighty = 0;
        displayConstraints.fill = GridBagConstraints.NONE;

        JButton addButton = new JButton("Add character");
        addButton.addActionListener(c -> {
            charsAndPassword.insertRow(charsAndPassword.getRowCount(), new String[]{"", ""});
        });
        displayConstraints.gridy = gridY++;
        gbl.setConstraints(addButton, displayConstraints);

        this.add(addButton);
    }

    private void saveFunction() {
        String home = System.getProperty("user.home");
        File file = new File(home + "/.vikingmud");

        try (FileWriter writer = new FileWriter(file)) {
            storeSettings(writer, this.charsAndPassword, "C:");
            storeSettings(writer, this.aliases, "A:");


        } catch (IOException e) {
            e.printStackTrace();
        }


        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));


    }

    private void storeSettings(FileWriter writer, DefaultTableModel model, String prefix) throws IOException {
        int rows = model.getRowCount();

        for (int i = 0; i < rows; i++) {
            String column1 = (String) model.getValueAt(i, 0);
            String column2 = (String) model.getValueAt(i, 1);

            if (column1.length() > 0) {
                writer.append(prefix + column1 + "=" + column2 + "\n");
            }
        }
    }
}
