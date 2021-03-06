package com.gui;

import com.domain.Settings;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.ToolTipManager;
import javax.swing.border.TitledBorder;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class SettingsDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JSpinner sheetTitleFontSizeSpinner;
    private JSpinner meetingSectionTitleFontSizeSpinner;
    private JSpinner partFontSizeSpinner;
    private JSpinner presenterNameFontSizeSpinner;
    private JSpinner labelsFontSizeSpinner;
    private JSpinner rowHeightSpinner;
    private JCheckBox askToAssignPresentersCheckBox;
    private JLabel sheetTitleFontSizeLabel;
    private JLabel meetingSectionTitleFontSizeLabel;
    private JLabel partFontSizeLabel;
    private JLabel presenterNameFontSizeLabel;
    private JLabel askToAssignPresentersLabel;
    private JLabel labelsFontSizeLabel;
    private JButton defaultSettingsButton;
    private JLabel rowHeightLabel;
    private JLabel addHallDivisionRowLabel;
    private JCheckBox addHallDivisionRowCheckbox;

    SettingsDialog(JFrame parentFrame) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        this.sheetTitleFontSizeSpinner.setModel(new SpinnerNumberModel(1, 1, 100, 1));
        this.meetingSectionTitleFontSizeSpinner.setModel(new SpinnerNumberModel(1, 1, 100, 1));
        this.partFontSizeSpinner.setModel(new SpinnerNumberModel(1, 1, 100, 1));
        this.presenterNameFontSizeSpinner.setModel(new SpinnerNumberModel(1, 1, 100, 1));
        this.labelsFontSizeSpinner.setModel(new SpinnerNumberModel(1, 1, 100, 1));
        this.rowHeightSpinner.setModel(
                new SpinnerNumberModel(1, 1, 999999, 1));

        final MouseAdapter mouseAdapter = new MouseAdapter() {
            public void mouseEntered(MouseEvent me) {
                ToolTipManager.sharedInstance().setDismissDelay(60000);
            }

            public void mouseExited(MouseEvent me) {
                ToolTipManager.sharedInstance().setDismissDelay(ToolTipManager.sharedInstance().getDismissDelay());
            }
        };

        askToAssignPresentersCheckBox.addMouseListener(mouseAdapter);
        addHallDivisionRowCheckbox.addMouseListener(mouseAdapter);


        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        defaultSettingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onDefault();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        this.setFieldsToSettingsDetails(Settings.getLastSavedSettings());

        pack();
        setResizable(false);
        setLocationRelativeTo(parentFrame);
    }

    private void onOK() {
        Settings settings = new Settings();
        settings.setId(1);
        settings.setSheetTitleFontSize((int) this.sheetTitleFontSizeSpinner.getValue());
        settings.setMeetingSectionTitleFontSize((int) this.meetingSectionTitleFontSizeSpinner.getValue());
        settings.setPartFontSize((int) this.partFontSizeSpinner.getValue());
        settings.setPresenterNameFontSize((int) this.presenterNameFontSizeSpinner.getValue());
        settings.setLabelsFontSize((int) this.labelsFontSizeSpinner.getValue());
        settings.setRowHeight((int) this.rowHeightSpinner.getValue() * 100);
        settings.setAskToAssignPresenters(this.askToAssignPresentersCheckBox.isSelected());
        settings.setHasHallDividers(this.addHallDivisionRowCheckbox.isSelected());

        try {
            Settings.settingsDao.update(settings);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    CommonUIResources.UI_TEXTS.getProperty("could.not.save.settings.message"),
                    CommonUIResources.UI_TEXTS.getProperty("problem.message.dialogue.title"),
                    JOptionPane.ERROR_MESSAGE);
        }

        dispose();
    }

    private void onDefault() {
        setFieldsToSettingsDetails(Settings.getDefaultSettings());
    }

    private void setFieldsToSettingsDetails(Settings settings) {
        this.sheetTitleFontSizeSpinner.setValue(settings.getSheetTitleFontSize());
        this.meetingSectionTitleFontSizeSpinner.setValue(settings.getMeetingSectionTitleFontSize());
        this.partFontSizeSpinner.setValue(settings.getPartFontSize());
        this.presenterNameFontSizeSpinner.setValue(settings.getPresenterNameFontSize());
        this.labelsFontSizeSpinner.setValue(settings.getLabelsFontSize());
        // The actual values for the row height that change the row height noticeably
        // are big numbers. Like, multiples of 100. So to make it easy for the user to
        // understand, I decided to divide the values by 100.
        this.rowHeightSpinner.setValue(settings.getRowHeight() / 100);
        this.askToAssignPresentersCheckBox.setSelected(settings.askToAssignPresenters());
        this.addHallDivisionRowCheckbox.setSelected(settings.hasHallDividers());
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
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(3, 1, new Insets(10, 5, 10, 5), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        defaultSettingsButton = new JButton();
        this.$$$loadButtonText$$$(defaultSettingsButton, this.$$$getMessageFromBundle$$$("UITexts", "default.settings.button.text"));
        panel1.add(defaultSettingsButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(6, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), this.$$$getMessageFromBundle$$$("UITexts", "font.settings.fields.border.title"), TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, null, null));
        sheetTitleFontSizeLabel = new JLabel();
        this.$$$loadLabelText$$$(sheetTitleFontSizeLabel, this.$$$getMessageFromBundle$$$("UITexts", "sheet.title.font.size.label"));
        panel3.add(sheetTitleFontSizeLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(0, 2, 6, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        sheetTitleFontSizeSpinner = new JSpinner();
        sheetTitleFontSizeSpinner.setToolTipText(this.$$$getMessageFromBundle$$$("UITexts", "tooltip.sheet.title.font.size.field"));
        panel3.add(sheetTitleFontSizeSpinner, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        meetingSectionTitleFontSizeLabel = new JLabel();
        this.$$$loadLabelText$$$(meetingSectionTitleFontSizeLabel, this.$$$getMessageFromBundle$$$("UITexts", "meeting.section.title.font.size.label"));
        panel3.add(meetingSectionTitleFontSizeLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        meetingSectionTitleFontSizeSpinner = new JSpinner();
        meetingSectionTitleFontSizeSpinner.setToolTipText(this.$$$getMessageFromBundle$$$("UITexts", "tooltip.meeting.section.title.font.size.field"));
        panel3.add(meetingSectionTitleFontSizeSpinner, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        partFontSizeLabel = new JLabel();
        this.$$$loadLabelText$$$(partFontSizeLabel, this.$$$getMessageFromBundle$$$("UITexts", "part.font.size.label"));
        partFontSizeLabel.setToolTipText("");
        panel3.add(partFontSizeLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        partFontSizeSpinner = new JSpinner();
        partFontSizeSpinner.setToolTipText(this.$$$getMessageFromBundle$$$("UITexts", "tooltip.part.font.size.field"));
        panel3.add(partFontSizeSpinner, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        presenterNameFontSizeLabel = new JLabel();
        this.$$$loadLabelText$$$(presenterNameFontSizeLabel, this.$$$getMessageFromBundle$$$("UITexts", "presenter.name.font.size.label"));
        presenterNameFontSizeLabel.setToolTipText("");
        panel3.add(presenterNameFontSizeLabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        presenterNameFontSizeSpinner = new JSpinner();
        presenterNameFontSizeSpinner.setToolTipText(this.$$$getMessageFromBundle$$$("UITexts", "tooltip.presenter.name.font.size.field"));
        panel3.add(presenterNameFontSizeSpinner, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelsFontSizeLabel = new JLabel();
        this.$$$loadLabelText$$$(labelsFontSizeLabel, this.$$$getMessageFromBundle$$$("UITexts", "labels.font.size.label"));
        labelsFontSizeLabel.setToolTipText("");
        panel3.add(labelsFontSizeLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelsFontSizeSpinner = new JSpinner();
        labelsFontSizeSpinner.setToolTipText(this.$$$getMessageFromBundle$$$("UITexts", "tooltip.labels.font.size.field"));
        panel3.add(labelsFontSizeSpinner, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rowHeightLabel = new JLabel();
        this.$$$loadLabelText$$$(rowHeightLabel, this.$$$getMessageFromBundle$$$("UITexts", "row.height.label"));
        rowHeightLabel.setToolTipText("");
        panel3.add(rowHeightLabel, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        rowHeightSpinner = new JSpinner();
        rowHeightSpinner.setToolTipText(this.$$$getMessageFromBundle$$$("UITexts", "tooltip.row.height.label.field"));
        panel3.add(rowHeightSpinner, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), this.$$$getMessageFromBundle$$$("UITexts", "other.settings.fields.border.title"), TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, null, null));
        final Spacer spacer3 = new Spacer();
        panel4.add(spacer3, new GridConstraints(0, 2, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        askToAssignPresentersLabel = new JLabel();
        this.$$$loadLabelText$$$(askToAssignPresentersLabel, this.$$$getMessageFromBundle$$$("UITexts", "ask.to.assign.presenter.label"));
        askToAssignPresentersLabel.setToolTipText("");
        panel4.add(askToAssignPresentersLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        askToAssignPresentersCheckBox = new JCheckBox();
        askToAssignPresentersCheckBox.setEnabled(false);
        askToAssignPresentersCheckBox.setText("");
        askToAssignPresentersCheckBox.setToolTipText(this.$$$getMessageFromBundle$$$("UITexts", "tooltip.ask.to.assign.presenter.field"));
        panel4.add(askToAssignPresentersCheckBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        addHallDivisionRowLabel = new JLabel();
        this.$$$loadLabelText$$$(addHallDivisionRowLabel, this.$$$getMessageFromBundle$$$("UITexts", "add.hall.division.row.label"));
        addHallDivisionRowLabel.setToolTipText("");
        panel4.add(addHallDivisionRowLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        addHallDivisionRowCheckbox = new JCheckBox();
        addHallDivisionRowCheckbox.setText("");
        addHallDivisionRowCheckbox.setToolTipText(this.$$$getMessageFromBundle$$$("UITexts", "tooltip.hall.division.row.field"));
        panel4.add(addHallDivisionRowCheckbox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    private static Method $$$cachedGetBundleMethod$$$ = null;

    private String $$$getMessageFromBundle$$$(String path, String key) {
        ResourceBundle bundle;
        try {
            Class<?> thisClass = this.getClass();
            if ($$$cachedGetBundleMethod$$$ == null) {
                Class<?> dynamicBundleClass = thisClass.getClassLoader().loadClass("com.intellij.DynamicBundle");
                $$$cachedGetBundleMethod$$$ = dynamicBundleClass.getMethod("getBundle", String.class, Class.class);
            }
            bundle = (ResourceBundle) $$$cachedGetBundleMethod$$$.invoke(null, path, thisClass);
        } catch (Exception e) {
            bundle = ResourceBundle.getBundle(path);
        }
        return bundle.getString(key);
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
        return contentPane;
    }

}
