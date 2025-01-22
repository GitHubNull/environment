package oxff.org.ui;

import burp.api.montoya.logging.Logging;
import oxff.org.Environment;
import oxff.org.model.Arg;
import oxff.org.model.ArgDialogOpType;
import oxff.org.model.ArgTableModel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnvironmentTab extends JPanel {
    private JPanel northPanel;
    private JPanel buttonPanel;
    private JPanel searchPanel;

    private JScrollPane centerPanel;
    private JPanel southPanel;

    private JButton addButton;
    private JButton removeButton;
    private JButton clearButton;
    private JButton editButton;
    JTextField searchField;
    private JButton queryButton;
    private JButton moveUpButton;
    private JButton moveDownButton;

    private JTable argTable;
    private final ArgTableModel argTableModel;

    private  JLabel statusLabel;
    Logging logger;

    public EnvironmentTab(Logging logger, ArgTableModel argTableModel){
        this.logger = logger;
        this.argTableModel = argTableModel;
        initUI();
        initActionListener();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        northPanel = new JPanel(new BorderLayout());
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        addButton = new JButton("Add");
        removeButton = new JButton("Remove");
        clearButton = new JButton("Clear");
        editButton = new JButton("Edit");
        searchField = new JTextField("key word", 32);
        queryButton = new JButton("Query");
        moveUpButton = new JButton("Move Up");
        moveDownButton = new JButton("Move Down");

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(editButton);
        buttonPanel.add(moveUpButton);
        buttonPanel.add(moveDownButton);

        searchPanel.add(searchField);
        searchPanel.add(queryButton);

        northPanel.add(buttonPanel, BorderLayout.CENTER);
        northPanel.add(searchPanel, BorderLayout.SOUTH);

        add(northPanel, BorderLayout.NORTH);

        argTable = new JTable(argTableModel);
        centerPanel = new JScrollPane(argTable);

        add(centerPanel, BorderLayout.CENTER);

        southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        statusLabel = new JLabel("Ready");
        southPanel.add(statusLabel);

        add(southPanel, BorderLayout.SOUTH);
    }

    private void initActionListener() {

        addButton.addActionListener(e -> {
            int cnt = Environment.getArgListSize();
            int maxtArgId = 0;
            if (cnt != 0){
                // get max id value
                for (int i = 0; i < argTableModel.getArgList().size(); i++) {
                    Arg arg = argTableModel.getArg(i);
                    if (arg.getId() > maxtArgId){
                        maxtArgId = arg.getId();
                    }
                }
            }
            ArgDialog argDialog = new ArgDialog(ArgDialogOpType.ADD, logger, maxtArgId, EnvironmentTab.this);
            argDialog.setVisible(true);
        });

        removeButton.addActionListener(e -> {
            // check it again
            if (JOptionPane.showConfirmDialog(null, "Are you sure you want to delete selected arguments?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            logger.logToOutput("selectedRows:" + Arrays.toString(argTable.getSelectedRows()));
           int[] selectedRows = argTable.getSelectedRows();
           if (null == selectedRows || selectedRows.length == 0){
               logger.logToError("Please select one or more rows to delete.");
               return;
           }
           for (int i = selectedRows.length - 1; i >= 0; i--) {
               int row = selectedRows[i];
               argTableModel.removeArg(row);
//               int argId = (int) argTableModel.getValueAt(row, 0);
//               Environment.args.removeIf(arg -> arg.getId() == argId);
//               argTableModel.removeRow(row);
           }
        });

        clearButton.addActionListener(e -> {
            // check it again
            if (JOptionPane.showConfirmDialog(null, "Are you sure you want to clear all arguments?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            argTableModel.removeAll();
        });

        editButton.addActionListener(e -> {
            int[] selectedRows = argTable.getSelectedRows();
            if (selectedRows.length != 1){
                logger.logToError("Please select one row to edit.");
                return;
            }

            ArgDialog argDialog = new ArgDialog(ArgDialogOpType.EDIT, logger, Environment.getArgListSize(), selectedRows[0]);
        });

        queryButton.addActionListener(e -> {
            String searchKey = searchField.getText().trim();
            if (searchKey.isEmpty() || searchKey.isBlank()){
                logger.logToError("Please input search key.");
                // 恢复原始数据
                argTableModel.restoreArgs();
                return;
            }
            argTableModel.filterArgsByName(searchKey);
        });

        moveUpButton.addActionListener(e -> {
            int[] selectedRows = argTable.getSelectedRows();
            if (selectedRows.length != 1){
                logger.logToError("Please select one row to move up.");
                return;
            }
            int row = selectedRows[0];
            if (row == 0){
                logger.logToError("This is the first row, can not move up.");
                return;
            }
            argTableModel.moveUp(row);
            argTable.setRowSelectionInterval(row - 1, row - 1);
        });

        moveDownButton.addActionListener(e -> {
            int[] selectedRows = argTable.getSelectedRows();
            if (selectedRows.length != 1){
                logger.logToError("Please select one row to move down.");
                return;
            }
            int row = selectedRows[0];
            if (row == argTableModel.getRowCount() - 1){
                logger.logToError("This is the last row, can not move down.");
                return;
            }
            argTableModel.moveDown(row);
            argTable.setRowSelectionInterval(row + 1, row + 1);
        });
    }

    public ArgTableModel getArgTableModel() {
        return argTableModel;
    }

    private List<Arg> filterArgsByName(String keyword) {
        List<Arg> filteredArgs = new ArrayList<>();
        for (Arg arg : argTableModel.getArgList()) {
            if (arg.getName().contains(keyword) || arg.getName().equals(keyword)) {
                filteredArgs.add(arg);
            }
        }
        return filteredArgs;
    }
}
