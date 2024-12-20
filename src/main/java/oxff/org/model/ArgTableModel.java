package oxff.org.model;

import groovy.lang.Script;

import javax.swing.table.AbstractTableModel;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ArgTableModel extends AbstractTableModel {
    //    int id;
//    String name;
//    ArgType type;
//    AutoUpdateType autoUpdateType;
//    int length;
//    String defaultValue;
//    String value;
//    Method method;
//    Script script;
//    String codePath;
//    boolean enabled = true;
//    String description;
    private final static String[] columnNames = {
            "ID", "Name", "Type", "AutoUpdateType", "Length", "DefaultValue", "Value", "Method", "Script", "CodePath",
            "Enable", "Description"
    };
    private final List<Arg> argList;

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
                return arg.getType().toString();
            case 3:
                return arg.getAutoUpdateType().toString();
            case 4:
                return 0 == arg.getLength() ? "" : arg.getLength();
            case 5:
                return null == arg.getDefaultValue() ? "" : arg.getDefaultValue();
            case 6:
                return null == arg.getValue() ? "" : arg.getDefaultValue();
            case 7:
                return null == arg.getMethod() ? "" : arg.getMethod().toString();
            case 8:
                return null == arg.getScript() ? "" : arg.getScript().toString();
            case 9:
                return null == arg.getCodePath() ? "" : arg.getCodePath();
            case 10:
                return arg.isEnabled();
            case 11:
                return null == arg.getDescription() ? "" : arg.getDescription();
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
        return 10 == columnIndex;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Integer.class;
            case 1:
                return String.class;
            case 2:
                return ArgType.class;
            case 3:
                return AutoUpdateType.class;
            case 4:
                return Integer.class;
            case 5:
                return String.class;
            case 6:
                return String.class;
            case 7:
                return String.class;
            case 8:
                return String.class;
            case 9:
                return String.class;
            case 10:
                return Boolean.class;
            case 11:
                return String.class;
            default:
                return null;
        }
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Arg arg = argList.get(rowIndex);
        switch (columnIndex) {
            case 0:
                arg.setId((int) aValue);
                break;
            case 1:
                arg.setName((String) aValue);
                break;
            case 2:
                arg.setType(ArgType.getArgType((String) aValue));
                break;
            case 3:
                arg.setAutoUpdateType(AutoUpdateType.getAutoUpdateType((String) aValue));
                break;
            case 4:
                arg.setLength((int) aValue);
                break;
            case 5:
                break;
            case 6:
                arg.setValue((String) aValue);
                break;
            case 7:
                arg.setMethod((Method) aValue);
                break;
            case 8:
                arg.setScript((Script) aValue);
                break;
            case 9:
                arg.setCodePath((String) aValue);
                break;
            case 10:
                arg.setEnabled((boolean) aValue);
                break;
            case 11:
                arg.setDescription((String) aValue);
                break;
            default:
                break;

        }
    }

    public void addArg(Arg arg) {
        argList.add(arg);
        fireTableRowsInserted(argList.size() - 1, argList.size() - 1);
    }

    public void removeArg(int row) {
        argList.remove(row);
        fireTableRowsDeleted(row, row);
    }

    public void removeAll() {
        int size = argList.size();
        argList.clear();
        fireTableRowsDeleted(0, size - 1);
    }

    public List<Arg> getArgList() {
        return argList;
    }

    public Arg getArg(int row) {
        return argList.get(row);
    }

    public int getArgIndex(String argName) {
        for (int i = 0; i < argList.size(); i++) {
            if (argList.get(i).getName().equals(argName)) {
                return i;
            }
        }
        return -1;
    }

    public int getArgIndex(Arg arg) {
        return argList.indexOf(arg);
    }

    public void updateArg(int row, Arg arg) {
        argList.set(row, arg);
        fireTableRowsUpdated(row, row);
    }

}
