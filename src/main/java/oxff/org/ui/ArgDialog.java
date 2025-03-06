package oxff.org.ui;

import burp.api.montoya.logging.Logging;
import groovy.lang.Script;
import oxff.org.Environment;
import oxff.org.model.Arg;
import oxff.org.model.ArgDialogOpType;
import oxff.org.model.ArgType;
import oxff.org.model.AutoUpdateType;
import oxff.org.utils.ArgTool;
import oxff.org.utils.GroovyUtils;
import oxff.org.utils.StringTool;
import oxff.org.utils.Tools;
import oxff.org.utils.sec.sha.StringShaTools;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ArgDialog extends JDialog {
    private static final String DEFAULT_LENGTH = "8";
    private static final String DEFAULT_INCREMENT_VALUE = "1";
    private final static Set<String> NUMBER_TYPE_SET = new HashSet<>();
    private final static Set<String> TEXT_TYPE_SET = new HashSet<>();
    //    private final static Set<String> GROOVY_CODE_TYPE_SET = new HashSet<>();
    private final static Set<String> ALL_TYPE_SET = new HashSet<>();
    ArgDialogOpType argDialogOpType;
    JPanel northPanel;
    //    JScrollPane centPanel;
    JPanel southPanel;
    Logging logger;
    int maxArgId;
    EnvironmentTab enviTab;
    int argListSize;
    int selectedRow;
    private JTextField argNameTextField;
    private JComboBox<String> argTypeComboBox;
    private JComboBox<String> autoUpdateTypeComboBox;
    private JLabel autoUpdateTypeExampleLabel;
    private JLabel argLengthLabel;
    private JTextField argLengthTextField;
    private JTextField argDefaultValueTextField;
    private JTextField argValueTextField;
    private JLabel argCodePathLabel;
    private JPanel codePathPanel;
    private JTextField argCodePathTextField;
    private JButton codePathButtonChooseFile;
    private JTextField argDescriptionTextField;
    private JCheckBox enabledCheckBox;
    private JButton okButton;
    private JButton cancelButton;


    public ArgDialog(ArgDialogOpType argDialogOpType, Logging logger, int maxArgId, EnvironmentTab enviTab) {
        this.argDialogOpType = argDialogOpType;
        this.logger = logger;
        this.maxArgId = maxArgId;
        this.enviTab = enviTab;

        initArgTypeSet();

        initUI();
        initData();
        initUIStatusByArgDialogOpType();
        initActionListeners();
    }

    public ArgDialog(ArgDialogOpType argDialogOpType, Logging logger, int argListSize, int selectedRow) {
        this.argDialogOpType = argDialogOpType;
        this.logger = logger;
        this.argListSize = argListSize;
        this.selectedRow = selectedRow;

        initArgTypeSet();

        initUI();
        initData();
        initUIStatusByArgDialogOpType();
        initActionListeners();
    }

    private void initNumberTypeSet() {
        NUMBER_TYPE_SET.clear();
        NUMBER_TYPE_SET.add(AutoUpdateType.TIMESTAMP.toString());
        NUMBER_TYPE_SET.add(AutoUpdateType.RANDOM_NUMBER.toString());
        NUMBER_TYPE_SET.add(AutoUpdateType.INCREMENT_NUMBER.toString());
        NUMBER_TYPE_SET.add(AutoUpdateType.Groovy_CODE.toString());
    }

    private void initTextTypeSet() {
        TEXT_TYPE_SET.clear();
        TEXT_TYPE_SET.add(AutoUpdateType.UUID.toString());
        TEXT_TYPE_SET.add(AutoUpdateType.SHA1_OF_TIMESTAMP.toString());
        TEXT_TYPE_SET.add(AutoUpdateType.RANDOM_TEXT.toString());
        TEXT_TYPE_SET.add(AutoUpdateType.Groovy_CODE.toString());
    }

    private void initAllArgTypeSet() {
        ALL_TYPE_SET.clear();
        ALL_TYPE_SET.add(AutoUpdateType.NONE.toString());
        ALL_TYPE_SET.add(AutoUpdateType.UUID.toString());
        ALL_TYPE_SET.add(AutoUpdateType.TIMESTAMP.toString());
        ALL_TYPE_SET.add(AutoUpdateType.SHA1_OF_TIMESTAMP.toString());
        ALL_TYPE_SET.add(AutoUpdateType.RANDOM_NUMBER.toString());
        ALL_TYPE_SET.add(AutoUpdateType.RANDOM_TEXT.toString());
        ALL_TYPE_SET.add(AutoUpdateType.INCREMENT_NUMBER.toString());
        ALL_TYPE_SET.add(AutoUpdateType.Groovy_CODE.toString());
    }

    private void initArgTypeSet() {
        initNumberTypeSet();
        initTextTypeSet();
        initAllArgTypeSet();
    }

    private void initData() {
        if (ArgDialogOpType.VIEW.equals(argDialogOpType) || ArgDialogOpType.EDIT.equals(argDialogOpType) ||
                ArgDialogOpType.DELETE.equals(argDialogOpType)) {
            logger.logToOutput("view arg");
            initArgFiled();
        } else if (ArgDialogOpType.ADD.equals(argDialogOpType)) {
            logger.logToOutput("add arg");
        } else {
            logger.logToOutput("error");
        }
    }

    private void initArgFiled() {
        Arg arg = Environment.argTableModel.getArg(selectedRow);
        argNameTextField.setText(arg.getName());
        argTypeComboBox.setSelectedItem(arg.getType().toString());
        autoUpdateTypeComboBox.setSelectedItem(arg.getAutoUpdateType().toString());
        argLengthTextField.setText(String.valueOf(arg.getLength()));
        argDefaultValueTextField.setText(arg.getDefaultValue());
        argValueTextField.setText(arg.getValue());
        argCodePathTextField.setText(arg.getCodePath());
        argDescriptionTextField.setText(arg.getDescription());
        enabledCheckBox.setSelected(arg.isEnabled());
    }

    private void initUIStatusByArgDialogOpType() {
        switch (argDialogOpType) {
            case ADD:
                enableAllFields(true);
                break;
            case EDIT:
                enableAllFields(true);
                Arg arg = Environment.argTableModel.getArg(selectedRow);
                enabledCheckBox.setSelected(arg.isEnabled());
                break;
            case VIEW:
            case DELETE:
                disableAllFields();
                break;
            default:
                // 处理未知的操作类型
                throw new IllegalArgumentException("Unknown dialog operation type: " + argDialogOpType);
        }
    }

    private void enableAllFields(boolean enable) {
        argNameTextField.setEnabled(enable);
        argTypeComboBox.setEnabled(enable);
        autoUpdateTypeComboBox.setEnabled(enable);
        argLengthTextField.setEnabled(enable);
        argDefaultValueTextField.setEnabled(enable);
        argValueTextField.setEnabled(enable);
        argCodePathTextField.setEnabled(enable);
        argDescriptionTextField.setEnabled(enable);
        enabledCheckBox.setEnabled(enable);
    }

    private void disableAllFields() {
        enableAllFields(false);
    }


    private void initUI() {
        setTitle(argDialogOpType.toString());

        setLayout(new BorderLayout());

        northPanel = new JPanel();
        northPanel.setLayout(new GridLayout(9, 3));

        JLabel argNameLabel = new JLabel("arg name: ");
        argNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        argNameTextField = new JTextField("argName_" + maxArgId);
        northPanel.add(argNameLabel);
        northPanel.add(argNameTextField);
        northPanel.add(new JLabel());

        JLabel argTypeLabel = new JLabel("arg type: ");
        argTypeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        argTypeComboBox = new JComboBox<>();
        argTypeComboBox.addItem(ArgType.TEXT.toString());
        argTypeComboBox.addItem(ArgType.NUMBER.toString());
        argTypeComboBox.addItem(ArgType.ALL.toString());
        argTypeComboBox.setSelectedItem(ArgType.ALL.toString());

        northPanel.add(argTypeLabel);
        northPanel.add(argTypeComboBox);
        northPanel.add(new JLabel());

        JLabel autoUpdateTypeLabel = new JLabel("auto update type: ");
        autoUpdateTypeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        autoUpdateTypeComboBox = new JComboBox<>();
        autoUpdateTypeComboBox.addItem(AutoUpdateType.NONE.toString());
        autoUpdateTypeComboBox.addItem(AutoUpdateType.UUID.toString());
        autoUpdateTypeComboBox.addItem(AutoUpdateType.TIMESTAMP.toString());
        autoUpdateTypeComboBox.addItem(AutoUpdateType.SHA1_OF_TIMESTAMP.toString());
        autoUpdateTypeComboBox.addItem(AutoUpdateType.RANDOM_NUMBER.toString());
        autoUpdateTypeComboBox.addItem(AutoUpdateType.RANDOM_TEXT.toString());
        autoUpdateTypeComboBox.addItem(AutoUpdateType.INCREMENT_NUMBER.toString());
        autoUpdateTypeComboBox.addItem(AutoUpdateType.Groovy_CODE.toString());

        autoUpdateTypeExampleLabel = new JLabel("Example:");

        northPanel.add(autoUpdateTypeLabel);
        northPanel.add(autoUpdateTypeComboBox);
        northPanel.add(autoUpdateTypeExampleLabel);

        argLengthLabel = new JLabel("arg length: ");
        argLengthLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        argLengthLabel.setEnabled(false);
        argLengthTextField = new JTextField("16");
        argLengthTextField.setEnabled(false);
        northPanel.add(argLengthLabel);
        northPanel.add(argLengthTextField);
        northPanel.add(new JLabel());

        JLabel argDefaultValueLabel = new JLabel("defaultValue: ");
        argDefaultValueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        argDefaultValueTextField = new JTextField("1");
        northPanel.add(argDefaultValueLabel);
        northPanel.add(argDefaultValueTextField);
        northPanel.add(new JLabel());

        JLabel argValueLabel = new JLabel("arg value: ");
        argValueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        argValueTextField = new JTextField("value...");
        northPanel.add(argValueLabel);
        northPanel.add(argValueTextField);
        northPanel.add(new JLabel());

        argCodePathLabel = new JLabel("code path: ");
        argCodePathLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        codePathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        argCodePathTextField = new JTextField("codePath...", 32);
        argCodePathTextField.setEnabled(false);
        codePathButtonChooseFile = new JButton("...");
        codePathButtonChooseFile.setEnabled(false);
        codePathPanel.add(argCodePathTextField);
        codePathPanel.add(codePathButtonChooseFile);

        codePathPanel.setEnabled(false);
        northPanel.add(argCodePathLabel);
        northPanel.add(codePathPanel);
        northPanel.add(new JLabel());

        //    JTextArea argCodeTextArea;
        JLabel argDescriptionLabel = new JLabel("description: ");
        argDescriptionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        argDescriptionTextField = new JTextField("description...");
        northPanel.add(argDescriptionLabel);
        northPanel.add(argDescriptionTextField);
        northPanel.add(new JLabel());

        JLabel enabledLabel = new JLabel("enabled: ");
        enabledLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        enabledCheckBox = new JCheckBox("enable");
        enabledCheckBox.setEnabled(true);
        enabledCheckBox.setSelected(true);

        northPanel.add(enabledLabel);
        northPanel.add(enabledCheckBox);
        northPanel.add(new JLabel());

        add(northPanel, BorderLayout.NORTH);


        southPanel = new JPanel();
        southPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        okButton = new JButton("OK");
        okButton.setEnabled(false);

        cancelButton = new JButton("Cancel");
        cancelButton.setEnabled(true);

        southPanel.add(okButton);
        southPanel.add(cancelButton);

        add(southPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    }

    private void initActionListeners() {

        argNameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if (checkArgFields()) {
                    okButton.setEnabled(true);
                }
            }
        });

        argTypeComboBox.addActionListener(e -> {
            if (checkArgFields()) {
                okButton.setEnabled(true);
            }
            Object selectedItem = argTypeComboBox.getSelectedItem();
            if (selectedItem == null) {
                logger.logToError("Selected item is null.");
                updateAutoUpdateTypeComboBoxItems(ArgType.ALL);
                return;
            }
            String selectedItemString = selectedItem.toString();
            if (null == selectedItemString || selectedItemString.isEmpty()){
                logger.logToError("Selected item is empty.");
                updateAutoUpdateTypeComboBoxItems(ArgType.ALL);
                return;
            }
            ArgType argType = ArgType.getArgType(selectedItemString);
            if (null == argType){
                logger.logToError("Selected item is not a valid ArgType.");
                updateAutoUpdateTypeComboBoxItems(ArgType.ALL);
                return;
            }
            SwingUtilities.invokeLater(() -> {
                updateAutoUpdateTypeComboBoxItems(argType);
            });
        });

        autoUpdateTypeComboBox.addActionListener(e -> autoUpdateTypeComboBoxActionListenerInit());

        argCodePathTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if (checkArgFields()) {
                    okButton.setEnabled(true);
                }
            }
        });

        codePathButtonChooseFile.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            fileChooser.setDialogTitle("Choose a file");

            // groovy file only
            fileChooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isFile() && f.getName().endsWith(".groovy");
                }

                @Override
                public String getDescription() {
                    return "";
                }
            });

            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                argCodePathTextField.setText(filePath);
                if (checkArgFields()) {
                    okButton.setEnabled(true);
                }
            }
        });

        okButton.addActionListener(e -> {
            if (argDialogOpType == ArgDialogOpType.ADD) {
                addArgProcess();
            } else if (argDialogOpType == ArgDialogOpType.EDIT) {
                editArgProcess();
            } else if (argDialogOpType == ArgDialogOpType.DELETE) {
                deleteArgProcess();
            } else {
                logger.logToError("unknown argDialogOpType: " + argDialogOpType);
            }
            dispose();
        });

        cancelButton.addActionListener(e -> dispose());
    }

    private void updateAutoUpdateTypeComboBoxItems(ArgType argType) {
        switch (argType) {
            case NUMBER:
                setAutoUpdateTypeItems(NUMBER_TYPE_SET);
                break;
            case TEXT:
                setAutoUpdateTypeItems(TEXT_TYPE_SET);
                break;
            default:
                setAutoUpdateTypeItems(ALL_TYPE_SET);
                break;
        }
    }

    private void setAutoUpdateTypeItems(Set<String> autoUpdateTypeItems) {
        autoUpdateTypeComboBox.removeAllItems();
        if (autoUpdateTypeItems == null || autoUpdateTypeItems.isEmpty()) {
            for (String item : ALL_TYPE_SET) {
                autoUpdateTypeComboBox.addItem(item);
            }
            return;
        }
        for (String item : autoUpdateTypeItems) {
            autoUpdateTypeComboBox.addItem(item);
        }
    }

    private void deleteArgProcess() {
    }

    private void editArgProcess() {
        if (!checkArgFields()) {
            logger.logToError("arg fields are not valid");
            return;
        }
        try {
            Arg arg = new Arg();
            arg.setId(Environment.argTableModel.getArg(selectedRow).getId());
            arg.setName(argNameTextField.getText());
            arg.setType(ArgType.getArgType(Objects.requireNonNull(argTypeComboBox.getSelectedItem()).toString()));
            arg.setAutoUpdateType(AutoUpdateType.getAutoUpdateType(Objects.requireNonNull(autoUpdateTypeComboBox.getSelectedItem()).toString()));
            arg.setLength(Integer.parseInt(argLengthTextField.getText()));
            arg.setDefaultValue(argDefaultValueTextField.getText());
            arg.setValue(argValueTextField.getText());
            arg.setCodePath(argCodePathTextField.getText());
            arg.setDescription(argDescriptionTextField.getText());
            arg.setEnabled(enabledCheckBox.isSelected());
            Environment.argTableModel.updateArgByRow(selectedRow, arg);
            logger.logToOutput("arg edited: " + arg.getName());
        } catch (Exception e) {
            logger.logToError("edit arg error: " + e.getMessage());
        }
    }

    private void autoUpdateTypeComboBoxActionListenerInit() {
        Object selectedItem = autoUpdateTypeComboBox.getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        String selectedType = selectedItem.toString();
        boolean isGroovyCode = AutoUpdateType.Groovy_CODE.toString().equals(selectedType);
        boolean isUUIDorTimestampOrSHA1 = Arrays.asList(
                AutoUpdateType.UUID.toString(),
                AutoUpdateType.TIMESTAMP.toString(),
                AutoUpdateType.SHA1_OF_TIMESTAMP.toString()
        ).contains(selectedType);

        updateComponentEnabledStatus(isGroovyCode, isUUIDorTimestampOrSHA1);

        SwingUtilities.invokeLater(() -> {
            logger.logToOutput("Selected auto update type: " + selectedType);

            int length = parseLengthFromTextField();

            AutoUpdateType autoUpdateType = AutoUpdateType.getAutoUpdateType(selectedType);
            updateExampleLabelAndLog(autoUpdateType, length);

            if (checkArgFields()) {
                okButton.setEnabled(true);
            }
        });
    }

    private void updateComponentEnabledStatus(boolean isGroovyCode, boolean isUUIDorTimestampOrSHA1) {
        argCodePathLabel.setEnabled(isGroovyCode);
        codePathPanel.setEnabled(isGroovyCode);
        argCodePathTextField.setEnabled(isGroovyCode);
        codePathButtonChooseFile.setEnabled(isGroovyCode);

        argLengthLabel.setEnabled(!isGroovyCode && !isUUIDorTimestampOrSHA1);
        argLengthTextField.setEnabled(!isGroovyCode && !isUUIDorTimestampOrSHA1);
    }

    private int parseLengthFromTextField() {
        String argLengthText = argLengthTextField.getText().trim();
        if (argLengthText.isEmpty() || argLengthText.isBlank()) {
            argLengthTextField.setText(DEFAULT_LENGTH);
            return Integer.parseInt(DEFAULT_LENGTH);
        }
        try {
            return Integer.parseInt(argLengthText.trim());
        } catch (NumberFormatException e) {
            logger.logToError(e);
            argLengthTextField.setText(DEFAULT_LENGTH);
            return Integer.parseInt(DEFAULT_LENGTH);
        }
    }

    private void updateExampleLabelAndLog(AutoUpdateType autoUpdateType, int length) {
        switch (autoUpdateType) {
            case UUID:
                autoUpdateTypeExampleLabel.setText("Example: " + Tools.getRandomUUID());
                logger.logToOutput("UUID: " + Tools.getRandomUUID());
                break;
            case TIMESTAMP:
                autoUpdateTypeExampleLabel.setText("Example: " + Tools.getTimestamp());
                logger.logToOutput("Timestamp: " + Tools.getTimestamp());
                break;
            case SHA1_OF_TIMESTAMP:
                autoUpdateTypeExampleLabel.setText("Example: " + StringShaTools.sha1(Tools.getTimestamp()));
                logger.logToOutput("SHA1: " + StringShaTools.sha1(Tools.getTimestamp()));
                break;
            case RANDOM_NUMBER:
                autoUpdateTypeExampleLabel.setText("Example: " + Tools.getRandomNumber(length));
                logger.logToOutput("Random Number: " + Tools.getRandomNumber(length));
                break;
            case RANDOM_TEXT:
                autoUpdateTypeExampleLabel.setText("Example: " + Tools.getRandomText(length));
                logger.logToOutput("Random Text: " + Tools.getRandomText(length));
                break;
            case INCREMENT_NUMBER:
                String tmpValue = argDefaultValueTextField.getText().strip().trim().isEmpty() ? DEFAULT_INCREMENT_VALUE :
                        argDefaultValueTextField.getText().strip().trim();
                autoUpdateTypeExampleLabel.setText("Example: " + Tools.getRandomIncrementNumber(tmpValue));
                logger.logToOutput("Increment Number: " + Tools.getRandomIncrementNumber(tmpValue));
                break;
            case Groovy_CODE:
                autoUpdateTypeExampleLabel.setText("Groovy Code Example: ");
                logger.logToOutput("Groovy Code Example: ");
                break;
            default:
                autoUpdateTypeExampleLabel.setText("default: ");
                logger.logToError("default");
        }
    }

    private void addArgProcess() {
        Arg arg = new Arg();
        arg.setId(maxArgId + 1);

        String name = argNameTextField.getText();
        if (null == name || name.isEmpty() || name.isBlank()) {
            JOptionPane.showMessageDialog(null, "arg name is empty");
            return;
        }
        arg.setName(name);

        ArgType argType = ArgType.getArgType(Objects.requireNonNull(argTypeComboBox.getSelectedItem()).toString());
        if (null == argType) {
            JOptionPane.showMessageDialog(null, "arg type is empty");
            return;
        }
        arg.setType(argType);

        AutoUpdateType autoUpdateType = AutoUpdateType.getAutoUpdateType(
                Objects.requireNonNull(autoUpdateTypeComboBox.getSelectedItem()).toString());
        arg.setAutoUpdateType(autoUpdateType);

        arg.setMethod(Environment.autoUpdateMethods.get(autoUpdateType));

        String defaultValue = argDefaultValueTextField.getText();
        if (null == defaultValue || defaultValue.isEmpty()) {
            arg.setDefaultValue("");
        } else {
            arg.setDefaultValue(defaultValue);
        }

        String value = argValueTextField.getText();
        if (null == value || value.isEmpty()) {
            arg.setValue("");
        } else {
            arg.setValue(value);
        }


        if (autoUpdateType.equals(AutoUpdateType.Groovy_CODE)) {
            String codePath = argCodePathTextField.getText();
            if (null == codePath || codePath.isEmpty()) {
                JOptionPane.showMessageDialog(null, "code path is empty");
                return;
            }
            Script script;
            try {
                script = GroovyUtils.getScript(codePath);
//                arg.setScript(script);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "code path is invalid");
                return;
            }
            arg.setCodePath(codePath);
            arg.setScript(script);
            Environment.groovyScripts.put(name, script);

        } else {
            Method method;
            switch (AutoUpdateType.getAutoUpdateType(autoUpdateTypeComboBox.getSelectedItem().toString())) {
                case UUID:
                    method = Environment.autoUpdateMethods.get(AutoUpdateType.UUID);
                    break;
                case TIMESTAMP:
                    method = Environment.autoUpdateMethods.get(AutoUpdateType.TIMESTAMP);
                    arg.setMethod(method);
                    break;
                case SHA1_OF_TIMESTAMP:
                    method = Environment.autoUpdateMethods.get(AutoUpdateType.SHA1_OF_TIMESTAMP);
                    break;
                case RANDOM_NUMBER:
                    method = Environment.autoUpdateMethods.get(AutoUpdateType.RANDOM_NUMBER);
                    arg.setMethod(method);
                    break;
                case RANDOM_TEXT:
                    method = Environment.autoUpdateMethods.get(AutoUpdateType.RANDOM_TEXT);
                    break;
                case INCREMENT_NUMBER:
                    method = Environment.autoUpdateMethods.get(AutoUpdateType.INCREMENT_NUMBER);
                    break;
                default:
                    method = null;
            }
            arg.setMethod(method);

            if (ArgTool.needParams(autoUpdateType)) {
                String lengthStr = argLengthTextField.getText();
                if (null == lengthStr || lengthStr.isEmpty() || lengthStr.isBlank()) {
                    JOptionPane.showMessageDialog(null, "arg length is empty");
                    return;
                }
                int length;
                try {
                    length = Integer.parseInt(lengthStr);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "arg length is invalid");
                    logger.logToError(e);
                    return;
                }
                if (length < 1) {
                    JOptionPane.showMessageDialog(null, "arg length is invalid");
                    return;
                }
                arg.setLength(length);
            }
        }
        arg.setEnabled(enabledCheckBox.isSelected());
        enviTab.getArgTableModel().addArg(arg);
//        Environment.argsMap.put(name, arg);

        dispose();
    }

    private boolean checkArgFields() {
        if (!checkArgName() || !checkArgAutoUpdateType()) {
            return false;
        }
        AutoUpdateType autoUpdateType = AutoUpdateType.getAutoUpdateType(
                Objects.requireNonNull(autoUpdateTypeComboBox.getSelectedItem()).toString());
        return switch (autoUpdateType) {
            case UUID, TIMESTAMP, SHA1_OF_TIMESTAMP -> true;
            case RANDOM_NUMBER, RANDOM_TEXT -> checkArgLength();
            case INCREMENT_NUMBER -> checkArgDefaultValue();
            case Groovy_CODE -> checkArgCodePath();
            default -> false;
        };
    }

    private boolean checkArgName() {
        String name = argNameTextField.getText();
        if (null == name || name.isEmpty() || name.isBlank()) {
            return false;
        }

        // 只允许出现大小写字母和下划线，并且必须以字母或者下划线开始
        if (!name.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            return false;
        }
        return true;
    }

    private boolean checkArgAutoUpdateType() {
        AutoUpdateType autoUpdateType = AutoUpdateType.getAutoUpdateType(
                Objects.requireNonNull(autoUpdateTypeComboBox.getSelectedItem()).toString());
        if (AutoUpdateType.NONE == autoUpdateType) {
            return false;
        }
        return true;
    }

    private boolean checkArgLength() {
        String lengthStr = argLengthTextField.getText();
        if (null == lengthStr || lengthStr.isEmpty() || lengthStr.isBlank()) {
            return false;
        }
        int length;
        try {
            length = Integer.parseInt(lengthStr);
        } catch (NumberFormatException e) {
            logger.logToError(e);
            return false;
        }
        return length > 0;
    }

    private boolean checkArgDefaultValue() {
        String defaultValue = argDefaultValueTextField.getText();
        if (null == defaultValue || defaultValue.isEmpty() || defaultValue.isBlank()) {
            return false;
        }
        return true;
    }

    private boolean checkArgValue() {
        String value = argValueTextField.getText();
        if (null == value || value.isEmpty() || value.isBlank()) {
            return false;
        }
        if (AutoUpdateType.INCREMENT_NUMBER.equals(autoUpdateTypeComboBox.getSelectedItem())) {
            return StringTool.isPositiveInteger(value);
        }

        return true;
    }

    private boolean checkArgCodePath() {
        String codePath = argCodePathTextField.getText();
        if (null == codePath || codePath.isEmpty() || codePath.isBlank()) {
            return false;
        }
        if (!new File(codePath).exists()) {
            return false;
        }
        return true;
    }

    private boolean checkArgDescription() {
        String description = argDescriptionTextField.getText();
        if (null == description || description.isEmpty() || description.isBlank()) {
            return false;
        }
        return true;
    }

}
