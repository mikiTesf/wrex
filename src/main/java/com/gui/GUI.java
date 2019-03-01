package com.gui;

import com.excel.ExcelFileGenerator;
import com.extraction.EPUBContentExtractor;

import javax.swing.*;

import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import java.awt.Dimension;
import java.awt.Color;
import java.io.File;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.charset.Charset;

public class GUI extends JFrame {
    private final JFrame thisFrame = this;
    private JButton openButton;
    private JButton generateButton;
    private JPanel mainPanel;
    private JTable publicationTable;
    private JScrollPane scrollPane;
    private JLabel statusLabel;

    private File[] publicationFolders = null;

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

        // setup table properties;
        publicationTable.setFillsViewportHeight(true);
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Publication(s)");
        publicationTable.setModel(tableModel);

        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        FileNameExtensionFilter filter = new FileNameExtensionFilter
                ("Meeting Workbook (EPUB)", "epub");

        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser
                        (System.getProperty("user.home"));
                fileChooser.setDragEnabled(false);
                fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
                fileChooser.setDialogTitle("Open...");
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setMultiSelectionEnabled(true);
                fileChooser.setFileFilter(filter);

                fileChooser.showDialog(thisFrame, "Open");
                publicationFolders = fileChooser.getSelectedFiles();

                for (int rowIndex = 0; rowIndex < tableModel.getColumnCount(); rowIndex++) {
                    if (tableModel.getRowCount() == 0) break;
                    tableModel.removeRow(rowIndex);
                }

                for (File publicationFolder : publicationFolders) {
                    tableModel.addRow(new Object[]{publicationFolder.getName()});
                }

                generateButton.setEnabled(publicationFolders.length != 0);
            }
        });

        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (publicationFolders == null) return;

                new Thread() {
                    @Override
                    public void run() {
                        generateButton.setEnabled(false);
                        openButton.setEnabled(false);
                        statusLabel.setText("Generating...");

                        try {
                            new EPUBContentExtractor().unzip(publicationFolders, Charset.defaultCharset());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        new ExcelFileGenerator().makeExcel();

                        statusLabel.setText("Done!");
                        generateButton.setEnabled(true);
                        openButton.setEnabled(true);
                    }
                }.start();
            }
        });

        statusLabel.setText("...");

        setVisible(true);
    }
}
