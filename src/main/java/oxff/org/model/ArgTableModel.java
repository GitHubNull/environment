package oxff.org.model;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ArgTableModel extends AbstractTableModel {
    private final static String[] columnNames = {
            "ID", "Name", "Type", "Auto Update Type", "Default Value", "Value", "Description"
    };
    private List<Arg> argList;

    public ArgTableModel() {
        argList = new ArrayList<>();
    }

    public ArgTableModel(List<Arg> argList) {
        this.argList = argList;
    }

    @Override
    public int getRowCount() {
        return argList.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Arg arg = argList.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return arg.getId();
            case 1:
                return arg.getName();
            case 2:
                return arg.getType();
            case 3:
                return arg.getAutoUpdateType();
            case 4:
                return arg.getDefaultValue();
            case 5:
                return arg.getValue();
            case 6:
                return arg.getDescription();
            default:
                return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Integer.class;
            case 2:
                return ArgType.class;
            case 3:
                return AutoUpdateType.class;
            default:
                return String.class;
        }
    }

}
