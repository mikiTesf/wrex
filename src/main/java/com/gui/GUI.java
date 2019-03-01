package com.gui;

import com.excel.ExcelFileGenerator;
import com.extraction.EPUBContentExtractor;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import java.io.File;

import java.awt.Dimension;
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

    private File[] publicationFolders = null;

    public GUI() {
        setContentPane(mainPanel);
        setSize(new Dimension(370, 400));
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
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Publication(s)");
        publicationTable.setModel(tableModel);

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
                generateButton.setEnabled(publicationFolders.length != 0);
            }
        });

        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (publicationFolders == null) return;

                try {
                    new EPUBContentExtractor().unzip(publicationFolders, Charset.defaultCharset());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                new ExcelFileGenerator().makeExcel();
                generateButton.setEnabled(false);
            }
        });

        setVisible(true);
    }
}
