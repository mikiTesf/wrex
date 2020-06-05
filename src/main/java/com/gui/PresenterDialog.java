package com.gui;

import com.domain.Presenter;
import com.domain.Privilege;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;
import java.util.ResourceBundle;

public class PresenterDialog extends JDialog {
    private JTextField firstNameTextField;
    private JTextField middleNameTextField;
    private JTextField lastNameTextField;
    private JButton addPresenterButton;
    private JLabel firstNameLabel;
    private JLabel middleNameLabel;
    private JLabel lastNameLabel;
    private JTable presentersTable;
    private final DefaultTableModel presentersTableModel;
    private JButton editPresenterButton;
    private JButton deletePresenterButton;
    private JButton updateNamesButton;
    private JPanel mainPanel;
    private JPanel namesPanel;
    private JComboBox<Privilege> privilegeComboBox;
    private JLabel privilegeLabel;

    private final Properties UI_TEXTS = new Properties();
    private final HashMap<Integer, Integer> rowToIdMap = new HashMap<>();

    PresenterDialog(JFrame parentFrame) {
        try {
            UI_TEXTS.load(getClass().getResourceAsStream("/UITexts.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        setContentPane(mainPanel);
        setModal(true);

        presentersTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        presentersTable.setModel(presentersTableModel);
        presentersTable.setRowHeight(25);
        presentersTableModel.addColumn(UI_TEXTS.getProperty("full.name.column.header"));
        presentersTableModel.addColumn(UI_TEXTS.getProperty("privilege.column.header"));

        for (Privilege privilege : Privilege.values()) {
            privilegeComboBox.addItem(privilege);
        }

        this.addPresenterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAdd(false);
            }
        });

        this.editPresenterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onEdit();
            }
        });

        this.deletePresenterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onRemove();
            }
        });

        this.updateNamesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onUpdate();
            }
        });

        this.updateNamesButton.setVisible(false);

        final Dimension minimumSize = new Dimension(450, 500);
        setMinimumSize(minimumSize);
        setPreferredSize(minimumSize);
        setLocationRelativeTo(parentFrame);

        refreshPresentersTable();
    }

    // on `addPresenterButtonClicked`
    private void onAdd(boolean isUpdate) {
        String insertedFirstName = firstNameTextField.getText();
        String insertedMiddleName = middleNameTextField.getText();
        String insertedLastName = lastNameTextField.getText();

        if (!inputIsValid(insertedFirstName, insertedMiddleName, insertedLastName)) {
            JOptionPane.showMessageDialog(
                    this,
                    UI_TEXTS.getProperty("name.contains.special.characters"),
                    UI_TEXTS.getProperty("problem.message.dialogue.title"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Presenter presenter = new Presenter(
                    insertedFirstName,
                    insertedMiddleName,
                    insertedLastName,
                    (Privilege) privilegeComboBox.getSelectedItem());

            if (isUpdate) {
                presenter.setId(this.rowToIdMap.get(presentersTable.getSelectedRow()));
                Presenter.presenterDao.update(presenter);
            } else {
                Presenter.save(presenter);
            }
        } catch (SQLException e1) {
            JOptionPane.showMessageDialog(
                    this,
                    UI_TEXTS.getProperty("could.not.save.new.presenter"),
                    UI_TEXTS.getProperty("problem.message.dialogue.title"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        refreshPresentersTable();
        clearInputFields();
        JOptionPane.showMessageDialog(
                this,
                UI_TEXTS.getProperty("new.presenter.details.saved"),
                UI_TEXTS.getProperty("done.message.dialogue.title"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    // on `editPresenterButton` clicked
    private void onEdit() {
        int selectedRow = this.presentersTable.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        this.presentersTable.setEnabled(false);
        this.updateNamesButton.setVisible(true);
        this.addPresenterButton.setEnabled(false);
        this.deletePresenterButton.setEnabled(false);
        this.editPresenterButton.setEnabled(false);

        try {
            Presenter presenter = Presenter.presenterDao.queryForId(rowToIdMap.get(selectedRow));
            this.firstNameTextField.setText(presenter.getFirstName());
            this.middleNameTextField.setText(presenter.getMiddleName());
            this.lastNameTextField.setText(presenter.getLastName());
            this.privilegeComboBox.setSelectedItem(presenter.getPrivilege());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // on `removePresenterButton` clicked
    private void onRemove() {
        int selectedRow = this.presentersTable.getSelectedRow();

        if (selectedRow == -1) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                UI_TEXTS.getProperty("are.you.sure") + " \""
                        + presentersTable.getValueAt(selectedRow, 0) + "\"?",
                "",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.NO_OPTION) {
            return;
        }

        try {
            Presenter.presenterDao.deleteById(rowToIdMap.get(selectedRow));
        } catch (SQLException ignore) {
        }

        presentersTableModel.removeRow(selectedRow);
    }

    // on `updateNamesButton` clicked
    private void onUpdate() {
        this.presentersTable.setEnabled(true);
        this.updateNamesButton.setVisible(false);
        this.addPresenterButton.setEnabled(true);
        this.deletePresenterButton.setEnabled(true);
        this.editPresenterButton.setEnabled(true);

        onAdd(true);
    }

    private boolean inputIsValid(String insertedFirstName, String insertedMiddleName, String insertedLastName) {

        String specialCharactersPattern = "[/\\\\+\\-=\\[\\]#$%!^&*()_?@\"{};':|,.<> 0-9]*";

        // Last names can be empty. Only First and middle names are mandatory.
        if (insertedFirstName.equals("") || insertedMiddleName.equals("")) {
            return false;
        }

        return (insertedFirstName.length() == insertedFirstName.replaceAll(specialCharactersPattern, "").length()) &&
                (insertedMiddleName.length() == insertedMiddleName.replaceAll(specialCharactersPattern, "").length()) &&
                (insertedLastName.length() == insertedLastName.replaceAll(specialCharactersPattern, "").length());
    }

    private void clearInputFields() {
        this.firstNameTextField.setText("");
        this.middleNameTextField.setText("");
        this.lastNameTextField.setText("");
    }

    private void refreshPresentersTable() {
        presentersTableModel.setRowCount(0);
        rowToIdMap.clear();

        try {
            for (Presenter presenter : Presenter.presenterDao.queryForAll()) {
                this.presentersTableModel.addRow(new Object[]{
                        presenter.getFirstName() + " " + presenter.getMiddleName() + " " + presenter.getLastName(),
                        presenter.getPrivilege()
                });
                rowToIdMap.put(presentersTableModel.getRowCount() - 1, presenter.getId());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    UI_TEXTS.getProperty("could.not.fetch.presenter.details"),
                    UI_TEXTS.getProperty("problem.message.dialogue.title"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.setEnabled(true);
        namesPanel = new JPanel();
        namesPanel.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(namesPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        namesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), ResourceBundle.getBundle("UITexts").getString("name.fields.border.title"), TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION));
        firstNameLabel = new JLabel();
        this.$$$loadLabelText$$$(firstNameLabel, ResourceBundle.getBundle("UITexts").getString("first.name.label.text"));
        namesPanel.add(firstNameLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        firstNameTextField = new JTextField();
        namesPanel.add(firstNameTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        middleNameLabel = new JLabel();
        this.$$$loadLabelText$$$(middleNameLabel, ResourceBundle.getBundle("UITexts").getString("middle.name.label.text"));
        namesPanel.add(middleNameLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        middleNameTextField = new JTextField();
        namesPanel.add(middleNameTextField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        lastNameLabel = new JLabel();
        this.$$$loadLabelText$$$(lastNameLabel, ResourceBundle.getBundle("UITexts").getString("last.name.label.text"));
        namesPanel.add(lastNameLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lastNameTextField = new JTextField();
        namesPanel.add(lastNameTextField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        privilegeLabel = new JLabel();
        this.$$$loadLabelText$$$(privilegeLabel, ResourceBundle.getBundle("UITexts").getString("privilege.label.text"));
        namesPanel.add(privilegeLabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        privilegeComboBox = new JComboBox();
        namesPanel.add(privilegeComboBox, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setBackground(new Color(-1));
        panel1.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        presentersTable = new JTable();
        presentersTable.setAutoCreateRowSorter(true);
        presentersTable.setAutoResizeMode(4);
        presentersTable.setFillsViewportHeight(true);
        scrollPane1.setViewportView(presentersTable);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        editPresenterButton = new JButton();
        this.$$$loadButtonText$$$(editPresenterButton, ResourceBundle.getBundle("UITexts").getString("edit.presenter.button.text"));
        panel2.add(editPresenterButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        deletePresenterButton = new JButton();
        this.$$$loadButtonText$$$(deletePresenterButton, ResourceBundle.getBundle("UITexts").getString("remove.presenter.button.text"));
        panel2.add(deletePresenterButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        addPresenterButton = new JButton();
        this.$$$loadButtonText$$$(addPresenterButton, ResourceBundle.getBundle("UITexts").getString("add.presenter.button.text"));
        panel3.add(addPresenterButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        updateNamesButton = new JButton();
        this.$$$loadButtonText$$$(updateNamesButton, ResourceBundle.getBundle("UITexts").getString("update.names.button.text"));
        panel3.add(updateNamesButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadLabelText$$$(JLabel component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadButtonText$$$(AbstractButton component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
