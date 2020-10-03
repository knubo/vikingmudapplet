package no.knubo.mud;

import javax.swing.*;
import java.awt.*;

public class ChatMessages extends JFrame {

    public ColorPane textPane;

    @SuppressWarnings("DuplicatedCode")
    public void init(MainWindow mainWindow) {
        textPane = new ColorPane();
        textPane.setMargin(new Insets(5, 5, 5, 5));
        textPane.setFont(new Font(mainWindow.getFontName(), Font.PLAIN, mainWindow.getFontSize()));

        textPane.setBackground(Color.BLACK);
        textPane.setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(textPane,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        int width = textPane.getFontMetrics(textPane.getFont()).charWidth('X') * 90;
        scrollPane.setMinimumSize(new Dimension(width, 200));

        GridBagConstraints displayConstraints = new GridBagConstraints();
        displayConstraints.gridx = 0;
        displayConstraints.gridy = 0;
        displayConstraints.gridwidth = 1;
        displayConstraints.gridheight = 1;
        displayConstraints.anchor = GridBagConstraints.NORTHWEST;
        displayConstraints.fill = GridBagConstraints.BOTH;
        displayConstraints.weightx = 1;
        displayConstraints.weighty = 1;

        GridBagLayout gbl = new GridBagLayout();

        setLayout(gbl);

        gbl.setConstraints(scrollPane, displayConstraints);
        add(scrollPane);

        setSize(640, 400);
    }



}
