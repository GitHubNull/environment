package oxff.org.ui;

import oxff.org.model.ArgTableModel;

import javax.swing.*;
import java.awt.*;

public class EnvironmentTab extends JPanel {
    private JPanel northPanel;
    private JScrollPane centerPanel;
    private JPanel southPanel;

    private JButton addButton;
    private JButton removeButton;
    private JButton clearButton;
    private JButton editButton;
    private JButton queryButton;
    private JButton moveUpButton;
    private JButton moveDownButton;

    private JTable argTable;
    private ArgTableModel argTableModel;

    private  JLabel statusLabel;

    public EnvironmentTab(){
        setLayout(new BorderLayout());

        northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = new JButton("Add");
        removeButton = new JButton("Remove");
        clearButton = new JButton("Clear");
        editButton = new JButton("Edit");
        queryButton = new JButton("Query");
        moveUpButton = new JButton("Move Up");
        moveDownButton = new JButton("Move Down");

        northPanel.add(addButton);
        northPanel.add(removeButton);
        northPanel.add(clearButton);
        northPanel.add(editButton);
        northPanel.add(queryButton);
        northPanel.add(moveUpButton);
        northPanel.add(moveDownButton);
        add(northPanel, BorderLayout.NORTH);

        argTableModel = new ArgTableModel();
        argTable = new JTable(argTableModel);
        centerPanel = new JScrollPane(argTable);

        add(centerPanel, BorderLayout.CENTER);

        southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        statusLabel = new JLabel("Ready");
        southPanel.add(statusLabel);

        add(southPanel, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("EnvironmentTab");
        frame.setContentPane(new EnvironmentTab());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

}
