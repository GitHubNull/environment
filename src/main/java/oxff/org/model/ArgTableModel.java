package oxff.org.model;

import groovy.lang.Script;

import javax.swing.table.AbstractTableModel;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ArgTableModel extends AbstractTableModel {
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
        return switch (columnIndex) {
            case 0 -> arg.getId();
            case 1 -> arg.getName();
            case 2 -> arg.getType().toString();
            case 3 -> arg.getAutoUpdateType().toString();
            case 4 -> 0 == arg.getLength() ? "" : arg.getLength();
            case 5 -> null == arg.getDefaultValue() ? "" : arg.getDefaultValue();
            case 6 -> null == arg.getValue() ? "" : arg.getDefaultValue();
            case 7 -> null == arg.getMethod() ? "" : arg.getMethod().toString();
            case 8 -> null == arg.getScript() ? "" : arg.getScript().toString();
            case 9 -> null == arg.getCodePath() ? "" : arg.getCodePath();
            case 10 -> arg.isEnabled();
            case 11 -> null == arg.getDescription() ? "" : arg.getDescription();
            default -> null;
        };
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
        return switch (columnIndex) {
            case 0, 4 -> Integer.class;
            case 1, 5, 6, 7, 8, 9, 11 -> String.class;
            case 2 -> ArgType.class;
            case 3 -> AutoUpdateType.class;
            case 10 -> Boolean.class;
            default -> null;
        };
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
            case 5:
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

    public void setArgList(List<Arg> filteredArgs) {
        argList.clear();
        argList.addAll(filteredArgs);
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

    public Arg getArgByName(String argName) {
        if (null == argList || argList.isEmpty() || null == argName || argName.isEmpty() || argName.isBlank()){
            return null;
        }
        for (Arg arg : argList) {
            if (arg.getName().equals(argName.strip())) {
                return arg;
            }
        }
        return null;
    }

    public void updateArgByRow(int row, Arg arg) {
        argList.set(row, arg);
        fireTableRowsUpdated(row, row);
    }

    public void updateArgById(int id, Arg arg) {
        for (int i = 0; i < argList.size(); i++) {
            if (argList.get(i).getId() == id) {
                argList.set(i, arg);
                fireTableRowsUpdated(i, i);
                return;
            }
        }
    }

    // 添加行移动逻辑
    public void moveRow(int oldIndex, int newIndex) {
        if (oldIndex < 0 || oldIndex >= argList.size() || newIndex < 0 || newIndex >= argList.size()) {
            throw new IllegalArgumentException("Invalid row index");
        }

        Arg arg = argList.remove(oldIndex); // 移除旧位置的行
        argList.add(newIndex, arg); // 插入到新位置
        fireTableRowsUpdated(Math.min(oldIndex, newIndex), Math.max(oldIndex, newIndex)); // 通知表格更新
    }
}
