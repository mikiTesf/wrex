package com.gui;

import com.excel.ExcelFileGenerator;
import com.extraction.EPUBContentExtractor;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JFileChooser;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.TableView;

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

                new Thread() {
                    @Override
                    public void run() {
                        fileChooser.resetChoosableFileFilters();
                        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
                        fileChooser.setDialogTitle("Save...");
                        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        fileChooser.setMultiSelectionEnabled(false);
                        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

                        fileChooser.showDialog(thisFrame, "Save");

                        generateButton.setEnabled(false);
                        openButton.setEnabled(false);
                        statusLabel.setText("Generating...");

                        try {
                            new EPUBContentExtractor().unzip(EPUBFiles, Charset.defaultCharset());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }

                        int status = new ExcelFileGenerator(fileChooser.getSelectedFile()).makeExcel();

                        generateButton.setEnabled(true);
                        openButton.setEnabled(true);

                        switch (status) {
                            case NO_PUBLICATIONS:
                                statusLabel.setText("...");
                                JOptionPane.showMessageDialog
                                        (thisFrame, "You didn't select any publications",
                                                "Problem", JOptionPane.ERROR_MESSAGE);
                                break;
                            case COULD_NOT_SAVE_FILE:
                                statusLabel.setText("...");
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
                                statusLabel.setText("...");
                                JOptionPane.showMessageDialog
                                        (thisFrame, "An unknown problem has occurred",
                                                "Problem", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }.start();
            }
        });

        statusLabel.setText("...");

        setVisible(true);
    }
}
