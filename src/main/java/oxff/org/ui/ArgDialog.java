package oxff.org.ui;

import burp.api.montoya.logging.Logging;
import groovy.lang.Script;
import oxff.org.Environment;
import oxff.org.model.Arg;
import oxff.org.model.ArgDialogOpType;
import oxff.org.model.ArgType;
import oxff.org.model.AutoUpdateType;
import oxff.org.utils.GroovyUtils;
import oxff.org.utils.Tools;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.Objects;

public class ArgDialog extends JDialog {
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

        initUI();
        initData();
        initUIStatusByArgDialogOpType();
        initActionListeners();
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
            case EDIT:
                enableAllFields(true);
                enabledCheckBox.setSelected(true);
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
        argTypeComboBox.addItem("NUMBER");
        argTypeComboBox.addItem("TEXT");
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
        });

        autoUpdateTypeComboBox.addActionListener(e -> autoUpdateTypeComboBoxActionListenerInit());

        argCodePathTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
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
        if (Objects.requireNonNull(autoUpdateTypeComboBox.getSelectedItem()).toString()
                .equals(AutoUpdateType.Groovy_CODE.toString())) {
            argCodePathLabel.setEnabled(true);
            codePathPanel.setEnabled(true);
            argCodePathTextField.setEnabled(true);
            codePathButtonChooseFile.setEnabled(true);

            argLengthLabel.setEnabled(false);
            argLengthTextField.setEnabled(false);

        } else {
            argCodePathLabel.setEnabled(false);
            codePathPanel.setEnabled(false);
            argCodePathTextField.setEnabled(false);
            codePathButtonChooseFile.setEnabled(false);

            if (autoUpdateTypeComboBox.getSelectedItem().toString().equals(AutoUpdateType.UUID.toString()) ||
                    autoUpdateTypeComboBox.getSelectedItem().toString()
                            .equals(AutoUpdateType.TIMESTAMP.toString()) ||
                    autoUpdateTypeComboBox.getSelectedItem().toString()
                            .equals(AutoUpdateType.SHA1_OF_TIMESTAMP.toString())) {
                argLengthLabel.setEnabled(false);
                argLengthTextField.setEnabled(false);
            } else {
                argLengthLabel.setEnabled(true);
                argLengthTextField.setEnabled(true);
            }
        }


        SwingUtilities.invokeLater(() -> {
            logger.logToOutput("Selected auto update type: " + autoUpdateTypeComboBox.getSelectedItem().toString());

            int length = 8;
            try {
                length = Integer.parseInt(argLengthTextField.getText().trim());
            } catch (Exception e1) {
                logger.logToError(e1);
                argLengthTextField.setText("8");
            }

            switch (AutoUpdateType.getAutoUpdateType(autoUpdateTypeComboBox.getSelectedItem().toString())) {
                case UUID:
                    autoUpdateTypeExampleLabel.setText("Example: " + Tools.getRandomUUID());
                    logger.logToOutput("UUID: " + Tools.getRandomUUID());
                    break;
                case TIMESTAMP:
                    autoUpdateTypeExampleLabel.setText("Example: " + Tools.getTimestamp());
                    logger.logToOutput("Timestamp: " + Tools.getTimestamp());
                    break;
                case SHA1_OF_TIMESTAMP:
                    autoUpdateTypeExampleLabel.setText("Example: " + Tools.sha1(Tools.getTimestamp()));
                    logger.logToOutput("SHA1: " + Tools.sha1(Tools.getTimestamp()));
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
                    String tmpValue = argDefaultValueTextField.getText().strip().trim().isEmpty() ? "1" :
                            argDefaultValueTextField.getText().strip().trim();
                    if (argDefaultValueTextField.getText().strip().trim().isEmpty()) {
                        argDefaultValueTextField.setText("1");
                    }
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
        });


        if (checkArgFields()) {
            okButton.setEnabled(true);
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
                arg.setScript(script);
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

            if (Tools.needParams(autoUpdateType)) {
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
        String name = argNameTextField.getText();
        if (null == name || name.isEmpty() || name.isBlank()) {
            return false;
        }
        ArgType argType = ArgType.getArgType(Objects.requireNonNull(argTypeComboBox.getSelectedItem()).toString());
        if (null == argType) {
            return false;
        }
        AutoUpdateType autoUpdateType = AutoUpdateType.getAutoUpdateType(
                Objects.requireNonNull(autoUpdateTypeComboBox.getSelectedItem()).toString());
        if (AutoUpdateType.NONE == autoUpdateType) {
            return false;
        }

        if (autoUpdateType.equals(AutoUpdateType.UUID) || autoUpdateType.equals(AutoUpdateType.TIMESTAMP) ||
                autoUpdateType.equals(AutoUpdateType.SHA1_OF_TIMESTAMP)) {
            return true;
        } else {
            String defaultValue = argDefaultValueTextField.getText();
            if (null == defaultValue || defaultValue.isEmpty() || defaultValue.isBlank()) {
                return false;
            }

            if (AutoUpdateType.Groovy_CODE == autoUpdateType) {
                String codePath = argCodePathTextField.getText();
                return (null != codePath && !codePath.isEmpty() && !codePath.isBlank());
            }

            int length;
            try {
                length = Integer.parseInt(argLengthTextField.getText());
            } catch (NumberFormatException e) {
                logger.logToError(e);
                return false;
            }
            return length >= 0;
        }

    }

}
