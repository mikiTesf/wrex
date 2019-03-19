package com.gui;

import com.excel.ExcelFileGenerator;
import com.extraction.EPUBContentExtractor;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.IOException;

import java.nio.charset.Charset;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

public class GUI extends JFrame {
    private final JFrame thisFrame = this;
    private JButton openButton;
    private JButton generateButton;
    private JPanel mainPanel;
    private JTable publicationTable;
    private JScrollPane scrollPane;
    private JLabel statusLabel;
    private final JFileChooser fileChooser = new JFileChooser();

    private final int NO_PUBLICATIONS = 1;
    private final int COULD_NOT_SAVE_FILE = 2;
    private final int SUCCESS = 0;

    private File[] EPUBFiles = null;

    public GUI() {
        setContentPane(mainPanel);
        setSize(new Dimension(300, 350));
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // other initial setups
        generateButton.setEnabled(false);
    }

    public void setupAndDrawUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        // I got nothing to say about the next line of code
        fileChooser.setDragEnabled(false);
        // setup table properties;
        publicationTable.setFillsViewportHeight(true);
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableModel.addColumn("Publication(s)");
        publicationTable.setModel(tableModel);

        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        statusLabel.setText("");

        FileNameExtensionFilter filter = new FileNameExtensionFilter
                ("Meeting Workbook (EPUB)", "epub");

        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
                fileChooser.setDialogTitle("Open...");
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setMultiSelectionEnabled(true);
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                fileChooser.setFileFilter(filter);

                fileChooser.showDialog(thisFrame, "Open");
                EPUBFiles = fileChooser.getSelectedFiles();

                tableModel.setRowCount(0);
                for (File EPUBFile : EPUBFiles) {
                    tableModel.addRow(new Object[]{EPUBFile.getName()});
                }

                generateButton.setEnabled(EPUBFiles.length != 0);
            }
        });

        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (EPUBFiles == null) return;

                Thread generateThread = new Thread() {
                    @Override
                    public void run() {
                        fileChooser.resetChoosableFileFilters();
                        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
                        fileChooser.setDialogTitle("Save...");
                        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        fileChooser.setMultiSelectionEnabled(false);
                        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

                        switch (fileChooser.showDialog(thisFrame, "Save")) {
                            case JFileChooser.CANCEL_OPTION:
                            case JFileChooser.ERROR_OPTION:
                                return;
                        }

                        final File DESTINATION = fileChooser.getSelectedFile();
                        final String FILE_NAME = "wrex.xlsx";
                        File[] files = DESTINATION.listFiles();

                        if (files != null && files.length > 0) {
                            // make sure the destination doesn't contain the same file
                            for (File file : files) {
                                if (file.getName().contains(FILE_NAME)) {
                                    int choice = JOptionPane.showConfirmDialog
                                            (thisFrame, "The file already exists.\n Overwrite?", "", JOptionPane.YES_NO_OPTION);
                                    if (choice == JOptionPane.YES_OPTION) break;
                                    if (choice == JOptionPane.NO_OPTION) return;
                                }
                            }
                        }

                        new Thread() {
                            @Override
                            public void run() {
                                generateButton.setEnabled(false);
                                openButton.setEnabled(false);
                                statusLabel.setText("Generating...");
                            }
                        }.start();

                        try {
                            new EPUBContentExtractor().unzip(EPUBFiles, Charset.defaultCharset());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }

                        final int STATUS = new ExcelFileGenerator(DESTINATION).makeExcel(FILE_NAME);

                        generateButton.setEnabled(true);
                        openButton.setEnabled(true);

                        switch (STATUS) {
                            case NO_PUBLICATIONS:
                                JOptionPane.showMessageDialog
                                        (thisFrame, "You didn't select any publications",
                                                "Problem", JOptionPane.ERROR_MESSAGE);
                                break;
                            case COULD_NOT_SAVE_FILE:
                                JOptionPane.showMessageDialog
                                        (thisFrame, "Could not save generated document",
                                                "Problem", JOptionPane.ERROR_MESSAGE);
                                break;
                            case SUCCESS:
                                statusLabel.setText("Done!");
                                JOptionPane.showMessageDialog
                                        (thisFrame, "Schedule generated", "Done",
                                                JOptionPane.INFORMATION_MESSAGE);
                                break;
                            default:
                                JOptionPane.showMessageDialog
                                        (thisFrame, "An unknown problem has occurred",
                                                "Problem", JOptionPane.ERROR_MESSAGE);
                        }
                        statusLabel.setText("");
                    }
                };

                SwingUtilities.invokeLater(generateThread);
            }
        });

        setVisible(true);
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
        mainPanel.setLayout(new GridLayoutManager(5, 5, new Insets(0, 0, 0, 0), -1, -1));
        openButton = new JButton();
        openButton.setIcon(new ImageIcon(getClass().getResource("/icons/openFile.png")));
        openButton.setText("Open");
        mainPanel.add(openButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        generateButton = new JButton();
        generateButton.setIcon(new ImageIcon(getClass().getResource("/icons/generateExcel.png")));
        generateButton.setText("Generate");
        mainPanel.add(generateButton, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        scrollPane = new JScrollPane();
        scrollPane.setBackground(new Color(-1));
        mainPanel.add(scrollPane, new GridConstraints(1, 1, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        publicationTable = new JTable();
        scrollPane.setViewportView(publicationTable);
        final JSeparator separator1 = new JSeparator();
        separator1.setEnabled(false);
        mainPanel.add(separator1, new GridConstraints(1, 4, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JSeparator separator2 = new JSeparator();
        separator2.setEnabled(false);
        mainPanel.add(separator2, new GridConstraints(1, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        statusLabel = new JLabel();
        Font statusLabelFont = this.$$$getFont$$$(null, -1, 11, statusLabel.getFont());
        if (statusLabelFont != null) statusLabel.setFont(statusLabelFont);
        statusLabel.setText("Label");
        mainPanel.add(statusLabel, new GridConstraints(3, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator3 = new JSeparator();
        separator3.setEnabled(false);
        mainPanel.add(separator3, new GridConstraints(4, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JSeparator separator4 = new JSeparator();
        separator4.setEnabled(false);
        mainPanel.add(separator4, new GridConstraints(0, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
