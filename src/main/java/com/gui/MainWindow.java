package com.gui;

import com.excel.ExcelFileGenerator;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FileChooserUI;
import javax.swing.table.DefaultTableModel;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import java.lang.reflect.Field;

import java.nio.charset.StandardCharsets;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.zip.ZipException;

import com.extraction.Extractor;
import com.extraction.PubExtract;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import static com.gui.GenerationStatus.*;

public class MainWindow extends JFrame {
    private final JFrame THIS_FRAME = this;
    private JButton openButton;
    private JButton generateButton;
    private JPanel mainPanel;
    private JTable publicationTable;
    private JScrollPane scrollPane;
    private JLabel statusLabel;
    private JComboBox<String> languageComboBox;
    private JPanel controlsPanel;
    private final JFileChooser FILE_CHOOSER;

    private final Properties UI_TEXTS = new Properties();

    private File[] EPUBFiles;

    public MainWindow() {
        final Properties PROGRAM_META = new Properties();

        try {
            UI_TEXTS.load(getClass().getResourceAsStream("/UITexts.properties"));
            PROGRAM_META.load(getClass().getResourceAsStream("/wrexMeta.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        setContentPane(mainPanel);
        setSize(new Dimension(450, 350));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle(
                PROGRAM_META.getProperty("program.name") + " (" + PROGRAM_META.getProperty("program.version") + ")"
        );
        setIconImage(new ImageIcon(getClass().getResource("/icons/frameIcon.png")).getImage());
        insertMenuBarAndItems();
        // other initial setups
        generateButton.setEnabled(false);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("FileChooser.readOnly", Boolean.TRUE);
        } catch (ClassNotFoundException | InstantiationException |
                IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        FILE_CHOOSER = new JFileChooser();

        try {
            FileChooserUI fileChooserUI = FILE_CHOOSER.getUI();
            Field field = fileChooserUI.getClass().getDeclaredField("fileNameTextField");
            field.setAccessible(true);
            JTextField textField = (JTextField) field.get(fileChooserUI);
            textField.setEditable(false);
            textField.setEnabled(false);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            System.out.println(e.getMessage());
        }
    }

    private void insertMenuBarAndItems() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu(UI_TEXTS.getProperty("file.menu.text"));

        JMenuItem exitItem = new JMenuItem(UI_TEXTS.getProperty("exit.menu.item.text"));
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        JMenuItem presentersItem = new JMenuItem(UI_TEXTS.getProperty("presenters.menu.item.text"));
        presentersItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new PresenterDialog(THIS_FRAME).setVisible(true);
            }
        });

        JMenuItem settingsItem = new JMenuItem(UI_TEXTS.getProperty("settings.menu.item.text"));
        settingsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SettingsDialog(THIS_FRAME).setVisible(true);
            }
        });

        fileMenu.add(presentersItem);
        fileMenu.add(settingsItem);
        fileMenu.add(exitItem);

        JMenu helpMenu = new JMenu(UI_TEXTS.getProperty("help.menu.text"));

        JMenuItem aboutItem = new JMenuItem(UI_TEXTS.getProperty("about.menu.item.text"));
        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AboutDialog(THIS_FRAME).setVisible(true);
            }
        });

        JMenuItem howToItem = new JMenuItem(UI_TEXTS.getProperty("howto.menu.item.text"));
        howToItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new HowToDialog(THIS_FRAME).setVisible(true);
            }
        });

        helpMenu.add(aboutItem);
        helpMenu.add(howToItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    public void setupAndDrawUI() {
        // fill `languageComboBox` with the available language(s)
        File languageFolder = new File("languages" + File.separator);
        if (!languageFolder.exists()) {
            // noinspection ResultOfMethodCallIgnored
            languageFolder.mkdir();
        }
        File[] availableLanguages = languageFolder.listFiles();
        // noinspection ConstantConditions
        Arrays.sort(availableLanguages);
        for (File languagePack : availableLanguages) {
            String language = languagePack.getName().toLowerCase();
            language = language.replaceFirst
                    (language.charAt(0) + "", Character.toUpperCase(language.charAt(0)) + "");
            language = language.substring(0, language.indexOf(".lang"));
            languageComboBox.addItem(language);
        }
        // I got nothing to say about the next line of code
        FILE_CHOOSER.setDragEnabled(false);
        // setup table properties
        publicationTable.setFillsViewportHeight(true);
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableModel.addColumn(UI_TEXTS.getProperty("publications.column.title"));
        publicationTable.setModel(tableModel);

        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        statusLabel.setText("");

        FileNameExtensionFilter filter = new FileNameExtensionFilter
                (UI_TEXTS.getProperty("jfilechooser.publication.filter.description"), "epub");

        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FILE_CHOOSER.setFileSelectionMode(JFileChooser.FILES_ONLY);
                FILE_CHOOSER.setMultiSelectionEnabled(true);
                FILE_CHOOSER.setCurrentDirectory(new File(System.getProperty("user.home")));
                FILE_CHOOSER.setFileFilter(filter);
                FILE_CHOOSER.showOpenDialog(THIS_FRAME);
                final File[] SELECTED_FILES_TEST = FILE_CHOOSER.getSelectedFiles();

                if (SELECTED_FILES_TEST.length != 0) {
                    EPUBFiles = SELECTED_FILES_TEST;
                    tableModel.setRowCount(0);
                    generateButton.setEnabled(true);

                    for (File EPUBFile : EPUBFiles) {
                        // strip the ".epub" part of the name
                        String nameToDisplayInTable = EPUBFile.getName()
                                .replaceAll("\\.epub", "");
                        // insert a '/' between the year and month of its due date
                        nameToDisplayInTable = new StringBuilder(nameToDisplayInTable)
                                .insert(nameToDisplayInTable.length() - 2, '/')
                                .toString();
                        // enclose the language identifying letter of the publication
                        // with parenthesis (this also gets rid of the underscores)
                        nameToDisplayInTable = nameToDisplayInTable
                                .replaceFirst("_", " (")
                                // the following `replaceFirst(...)` only replaces the last underscore
                                // as there will only be one left after the above replacement is done
                                .replaceFirst("_", ") ");
                        tableModel.addRow(new String[]{nameToDisplayInTable});
                    }
                }
            }
        });

        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (EPUBFiles.length == 0) return;

                FILE_CHOOSER.resetChoosableFileFilters();
                FILE_CHOOSER.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                FILE_CHOOSER.setCurrentDirectory(new File(System.getProperty("user.home")));
                FILE_CHOOSER.setMultiSelectionEnabled(false);

                switch (FILE_CHOOSER.showSaveDialog(THIS_FRAME)) {
                    case JFileChooser.CANCEL_OPTION:
                    case JFileChooser.ERROR_OPTION:
                        return;
                }

                final File DESTINATION = FILE_CHOOSER.getSelectedFile();
                File[] files = DESTINATION.listFiles();
                final String GENERATED_DATE = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                final String SAVE_NAME = "WREX_" + GENERATED_DATE + ".xlsx";

                if (files != null && files.length > 0) {
                    // make sure the destination doesn't contain the same file
                    for (File file : files) {
                        if (file.getName().contains(SAVE_NAME)) {
                            int choice = JOptionPane.showConfirmDialog
                                    (THIS_FRAME, UI_TEXTS.getProperty("jfilechooser.overwrite.duplicate.file.message"),
                                            "", JOptionPane.YES_NO_OPTION);
                            if (choice == JOptionPane.YES_OPTION) break;
                            if (choice == JOptionPane.NO_OPTION) return;
                        }
                    }
                }

                final Properties LANGUAGE_PACK = new Properties();

                try {
                    @SuppressWarnings("ConstantConditions")
                    FileInputStream input = new FileInputStream
                            ("languages" + File.separator + languageComboBox.getSelectedItem().toString().toLowerCase() + ".lang");
                    LANGUAGE_PACK.load(new InputStreamReader(input, StandardCharsets.UTF_8));
                } catch (FileNotFoundException e1) {
                    JOptionPane.showMessageDialog(
                            THIS_FRAME,
                            UI_TEXTS.getProperty("language.pack.renamed.or.deleted.message"),
                            UI_TEXTS.getProperty("problem.message.dialogue.title"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (IOException e2) {
                    JOptionPane.showMessageDialog(
                            THIS_FRAME,
                            UI_TEXTS.getProperty("language.pack.unreadable.message"),
                            UI_TEXTS.getProperty("problem.message.dialogue.title"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                new UIController(DESTINATION, SAVE_NAME, LANGUAGE_PACK).execute();
            }
        });

        setVisible(true);
    }

    private class UIController extends SwingWorker<Void, Void> {

        private final File DESTINATION;
        private final String SAVE_NAME;
        // `GENERATION_STATUS` is only useful to choose the message to be shown to the user in case
        // of an error. The actual error/exception is properly handled using try/catch blocks. In fact,
        // the value for `GENERATION_STATUS` is set in the `catch` blocks of the corresponding error(s)
        private GenerationStatus GENERATION_STATUS;
        private final Properties LANGUAGE_PACK;
        private final Extractor EXTRACTOR;

        private UIController(File DESTINATION, String SAVE_NAME, Properties LANGUAGE_PACK) {
            this.DESTINATION = DESTINATION;
            this.SAVE_NAME = SAVE_NAME;
            this.LANGUAGE_PACK = LANGUAGE_PACK;
            EXTRACTOR = new Extractor(LANGUAGE_PACK.getProperty("filter_for_minute"));
        }

        @Override
        protected Void doInBackground() {
            toggleButtons();
            statusLabel.setText(UI_TEXTS.getProperty("status.label.generating.text"));

            final ArrayList<PubExtract> ALL_PUB_EXTRACTS;

            try {
                ALL_PUB_EXTRACTS = EXTRACTOR.getPublicationExtracts(EPUBFiles);
            } catch (ZipException e) {
                GENERATION_STATUS = ZIP_FORMAT_ERROR;
                return null;
            } catch (IOException e1) {
                GENERATION_STATUS = COULD_NOT_READ_FILE_ERROR;
                return null;
            }

            try {
                new ExcelFileGenerator(ALL_PUB_EXTRACTS, LANGUAGE_PACK, DESTINATION)
                        .makeExcel(SAVE_NAME);
                // If the above operation does not throw any Exceptions, then it can be confidently
                // concluded that the generation process went smoothly without any problems. Hence
                // the assignment of `SUCCESS` to `GENERATION_STATUS`.
                GENERATION_STATUS = SUCCESS;

            } catch (IOException e1) {
                GENERATION_STATUS = COULD_NOT_SAVE_FILE_ERROR;
                return null;
            }

            return null;
        }

        @Override
        protected void done() {
            toggleButtons();

            switch (GENERATION_STATUS) {
                case ZIP_FORMAT_ERROR:
                    JOptionPane.showMessageDialog(THIS_FRAME, UI_TEXTS.getProperty("file.format.error.message"),
                            UI_TEXTS.getProperty("problem.message.dialogue.title"), JOptionPane.ERROR_MESSAGE);
                    break;
                case COULD_NOT_READ_FILE_ERROR:
                    JOptionPane.showMessageDialog(THIS_FRAME, UI_TEXTS.getProperty("could.not.read.epub.file.message"),
                            UI_TEXTS.getProperty("problem.message.dialogue.title"), JOptionPane.ERROR_MESSAGE);
                    break;
                case COULD_NOT_SAVE_FILE_ERROR:
                    JOptionPane.showMessageDialog(THIS_FRAME, UI_TEXTS.getProperty("could.not.save.document.message"),
                            UI_TEXTS.getProperty("problem.message.dialogue.title"), JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("");
                    break;
                case SUCCESS:
                    statusLabel.setText(UI_TEXTS.getProperty("status.label.generation.finished.text"));
                    JOptionPane.showMessageDialog
                            (THIS_FRAME, UI_TEXTS.getProperty("generation.successful.message"),
                                    UI_TEXTS.getProperty("done.message.dialogue.title"), JOptionPane.INFORMATION_MESSAGE);
                    break;
                default:
                    JOptionPane.showMessageDialog(THIS_FRAME, UI_TEXTS.getProperty("unknown.problem.has.occurred.message"),
                            UI_TEXTS.getProperty("problem.message.dialogue.title"), JOptionPane.ERROR_MESSAGE);
            }

            statusLabel.setText("");
        }

        private void toggleButtons() {
            openButton.setEnabled(!openButton.isEnabled());
            generateButton.setEnabled(!generateButton.isEnabled());
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
        scrollPane = new JScrollPane();
        scrollPane.setBackground(new Color(-1));
        mainPanel.add(scrollPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        publicationTable = new JTable();
        publicationTable.setAutoCreateRowSorter(false);
        publicationTable.setAutoResizeMode(4);
        publicationTable.setEnabled(true);
        publicationTable.setFillsViewportHeight(true);
        Font publicationTableFont = this.$$$getFont$$$(null, -1, 14, publicationTable.getFont());
        if (publicationTableFont != null) publicationTable.setFont(publicationTableFont);
        publicationTable.setIntercellSpacing(new Dimension(1, 1));
        publicationTable.setRowHeight(25);
        publicationTable.setToolTipText("");
        scrollPane.setViewportView(publicationTable);
        controlsPanel = new JPanel();
        controlsPanel.setLayout(new GridLayoutManager(2, 5, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(controlsPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        openButton = new JButton();
        openButton.setIcon(new ImageIcon(getClass().getResource("/icons/openFile.png")));
        this.$$$loadButtonText$$$(openButton, ResourceBundle.getBundle("UITexts").getString("open.button.text"));
        controlsPanel.add(openButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        generateButton = new JButton();
        generateButton.setIcon(new ImageIcon(getClass().getResource("/icons/generateExcel.png")));
        this.$$$loadButtonText$$$(generateButton, ResourceBundle.getBundle("UITexts").getString("generate.button.text"));
        controlsPanel.add(generateButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        statusLabel = new JLabel();
        statusLabel.setEnabled(false);
        Font statusLabelFont = this.$$$getFont$$$(null, -1, 11, statusLabel.getFont());
        if (statusLabelFont != null) statusLabel.setFont(statusLabelFont);
        statusLabel.setHorizontalAlignment(0);
        statusLabel.setHorizontalTextPosition(0);
        statusLabel.setText("Label");
        controlsPanel.add(statusLabel, new GridConstraints(1, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        languageComboBox = new JComboBox();
        controlsPanel.add(languageComboBox, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        controlsPanel.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        controlsPanel.add(spacer2, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
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
