package oxff.org.ui;

import burp.api.montoya.logging.Logging;
import oxff.org.Environment;
import oxff.org.model.Arg;
import oxff.org.model.ArgDialogOpType;
import oxff.org.model.ArgTableModel;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.regex.PatternSyntaxException;

public class EnvironmentTab extends JPanel {
    private final ArgTableModel argTableModel;
    JTextField searchField;
    Logging logger;
    private JButton addButton;
    private JButton removeButton;
    private JButton clearButton;
    private JButton editButton;
    private JButton queryButton;
    private JButton moveUpButton;
    private JButton moveDownButton;
    private JTable argTable;
    private TableRowSorter<TableModel> sorter;

    public EnvironmentTab(Logging logger, ArgTableModel argTableModel) {
        this.logger = logger;
        this.argTableModel = argTableModel;
        initUI();
        initActionListener();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JPanel northPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

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
        sorter = new TableRowSorter<>(argTableModel);
        argTable.setRowSorter(sorter);
        JScrollPane centerPanel = new JScrollPane(argTable);

        // 启用多字段排序
        sorter.setSortsOnUpdates(true);

        add(centerPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel statusLabel = new JLabel("Ready");
        southPanel.add(statusLabel);

        add(southPanel, BorderLayout.SOUTH);
    }

    private void initActionListener() {

        addButton.addActionListener(e -> {
            int cnt = Environment.getArgListSize();
            int maxArgId = 0;
            if (cnt != 0) {
                // get max id value
                for (int i = 0; i < argTableModel.getArgList().size(); i++) {
                    Arg arg = argTableModel.getArg(i);
                    if (arg.getId() > maxArgId) {
                        maxArgId = arg.getId();
                    }
                }
            }
            ArgDialog argDialog = new ArgDialog(ArgDialogOpType.ADD, logger, maxArgId, EnvironmentTab.this);
            argDialog.setVisible(true);
        });

        removeButton.addActionListener(e -> {
            // check it again
            if (JOptionPane.showConfirmDialog(null, "Are you sure you want to delete selected arguments?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            logger.logToOutput("selectedRows:" + Arrays.toString(argTable.getSelectedRows()));
            int[] selectedRows = argTable.getSelectedRows();
            if (null == selectedRows || selectedRows.length == 0) {
                logger.logToError("Please select one or more rows to delete.");
                return;
            }
            for (int i = selectedRows.length - 1; i >= 0; i--) {
                int row = selectedRows[i];
                argTableModel.removeArg(row);
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
            if (selectedRows.length != 1) {
                logger.logToError("Please select one row to edit.");
                return;
            }

            ArgDialog argDialog = new ArgDialog(ArgDialogOpType.EDIT, logger, Environment.getArgListSize(), selectedRows[0]);
            argDialog.setVisible(true);
        });

        queryButton.addActionListener(e -> {
            if (searchField == null || sorter == null) {
                logger.logToError("Search field or sorter is null.");
                return;
            }

            String searchKey = searchField.getText().trim();
            if (searchKey.isBlank()) {
                logger.logToError("Please input search key.");
                sorter.setRowFilter(null);
            } else {
                try {
                    logger.logToOutput("searchKey:" + searchKey);
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchKey, 1));
                } catch (PatternSyntaxException ex) {
                    logger.logToError("Invalid regex pattern: " + ex.getMessage());
                    sorter.setRowFilter(null);
                }
            }
        });


        moveUpButton.addActionListener(e -> moveSelectedRow(-1));

        moveDownButton.addActionListener(e -> moveSelectedRow(1));

        argTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // 判断是否为双击事件
                    int row = argTable.rowAtPoint(e.getPoint()); // 获取被双击的行号
                    if (row < 0) { // 确保行号有效
                        return;
                    }
                    int modelRow = argTable.getRowSorter().convertRowIndexToModel(row);

                    ArgDialog argDialog = new ArgDialog(ArgDialogOpType.VIEW, logger, Environment.getArgListSize(), modelRow);
                    argDialog.setVisible(true);
                }
            }
        });
    }

    public ArgTableModel getArgTableModel() {
        return argTableModel;
    }

    private void moveSelectedRow(int direction) {
        int selectedRow = argTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a row to move.", "No Row Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = argTable.convertRowIndexToModel(selectedRow);
        int targetRow = modelRow + direction;

        if (targetRow < 0 || targetRow >= argTableModel.getRowCount()) {
            JOptionPane.showMessageDialog(this, "Cannot move row further in this direction.", "Boundary Reached", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 调用自定义TableModel的moveRow方法
        argTableModel.moveRow(modelRow, targetRow);

        // 更新选中行
        int viewRow = argTable.convertRowIndexToView(targetRow);
        argTable.setRowSelectionInterval(viewRow, viewRow);
        argTable.scrollRectToVisible(argTable.getCellRect(viewRow, 0, true));
    }
}
