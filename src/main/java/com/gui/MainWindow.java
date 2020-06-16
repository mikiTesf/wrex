package com.gui;

//import com.domain.Settings;
import com.excel.ExcelFileGenerator;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
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

import static com.gui.MainWindow.GenerationStatus.*;


public class MainWindow extends JFrame {
    private final JFrame THIS_FRAME = this;
    private JButton openButton;
    private JButton generateButton;
    private JPanel mainPanel;
    private JTable publicationTable;
    private JScrollPane scrollPane;
    private JComboBox<String> languageComboBox;
    private JPanel controlsPanel;
    private JProgressBar progressBar;
    private final JFileChooser FILE_CHOOSER;
//    private Settings settings;

    private final Properties UI_TEXTS = new Properties();

    private File[] EPUBFiles;

    // These values are used in the GUI to identify what kind of message dialog should
    // be displayed when an exception is caught.
    enum GenerationStatus {
        SUCCESS,
        ZIP_FORMAT_ERROR,
        COULD_NOT_READ_FILE_ERROR,
        COULD_NOT_SAVE_FILE_ERROR
    }

    public MainWindow() {
        final Properties PROGRAM_META = new Properties();

        try {
            UI_TEXTS.load(getClass().getResourceAsStream("/UITexts.properties"));
            PROGRAM_META.load(getClass().getResourceAsStream("/wrexMeta.properties"));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    THIS_FRAME,
                    "An unknown problem has occurred.",
                    "Problem",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        setContentPane(mainPanel);
        setIconImage(new ImageIcon(getClass().getResource("/icons/frameIcon.png")).getImage());
        insertMenuBarAndItems();
        // Other initial setups
        generateButton.setEnabled(false);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.put("FileChooser.readOnly", Boolean.TRUE);
        } catch (ClassNotFoundException | InstantiationException |
                IllegalAccessException | UnsupportedLookAndFeelException ignore) { }

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

        setTitle(PROGRAM_META.getProperty("program.name") + " (" + PROGRAM_META.getProperty("program.version") + ")");

        setMinimumSize(new Dimension(450, 350));
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
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

        JMenuItem refreshLanguagesItem = new JMenuItem(UI_TEXTS.getProperty("refresh.languages.menu.item.text"));
        refreshLanguagesItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshLanguageComboBox();
            }
        });

        fileMenu.add(presentersItem);
        fileMenu.add(settingsItem);
        fileMenu.add(refreshLanguagesItem);
        fileMenu.add(exitItem);

        JMenu helpMenu = new JMenu(UI_TEXTS.getProperty("help.menu.text"));

        JMenuItem aboutItem = new JMenuItem(UI_TEXTS.getProperty("about.menu.item.text"));
        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AboutDialog(THIS_FRAME).setVisible(true);
            }
        });

        JMenuItem howToItem = new JMenuItem(UI_TEXTS.getProperty("howTo.menu.item.text"));
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
        refreshLanguageComboBox();
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
        tableModel.addColumn(UI_TEXTS.getProperty("publications.column.header"));
        publicationTable.setModel(tableModel);
        publicationTable.setToolTipText(null);

        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        FileNameExtensionFilter filter = new FileNameExtensionFilter
                (UI_TEXTS.getProperty("jfilechooser.publication.filter.description"), "epub");

        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FILE_CHOOSER.setFileSelectionMode(JFileChooser.FILES_ONLY);
                FILE_CHOOSER.setMultiSelectionEnabled(true);
                FILE_CHOOSER.setCurrentDirectory(new File(System.getProperty("user.home")));
                FILE_CHOOSER.setFileFilter(filter);

                if (FILE_CHOOSER.showOpenDialog(THIS_FRAME) == JFileChooser.APPROVE_OPTION) {
                    EPUBFiles = FILE_CHOOSER.getSelectedFiles();
                    tableModel.setRowCount(0);
                    generateButton.setEnabled(true);

                    for (File EPUBFile : EPUBFiles) {
                        // Strip the extension from the file's name
                        String nameToDisplayInTable = EPUBFile.getName()
                                .replaceAll("\\.[^.]+$", "");
                        // The following check helps avoid a `StringIndexOutOfBoundsException` on the line
                        // where a '/' is inserted between the publication's year and month. If, for example,
                        // the selected EPUB file was not an MWB and just had one character in its name, the
                        // if check below saves WREX from encountering the exception mentioned above.
                        if (nameToDisplayInTable.matches("^mwb_[A-Z]+_[0-9]{6}$")) {
                            // Insert a '/' between the year and month of its due date.
                            nameToDisplayInTable = new StringBuilder(nameToDisplayInTable)
                                    .insert(nameToDisplayInTable.length() - 2, '/')
                                    .toString();
                            // Enclose the language identifying letter of the publication
                            // with parenthesis (this also gets rid of the underscores).
                            nameToDisplayInTable = nameToDisplayInTable
                                    .replaceFirst("_", " (")
                                    // The following `replaceFirst(...)` only replaces the last underscore
                                    // as there will only be one left after the above replacement is done.
                                    .replaceFirst("_", ") ");
                        }
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

                    if (!languagePackIsValid(LANGUAGE_PACK)) {
                        JOptionPane.showMessageDialog(
                                THIS_FRAME,
                                UI_TEXTS.getProperty("invalid.language.pack.message"),
                                UI_TEXTS.getProperty("problem.message.dialogue.title"),
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
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

//                settings = Settings.getLastSavedSettings();
                new UIController(DESTINATION, SAVE_NAME, LANGUAGE_PACK).execute();
            }
        });

        setVisible(true);
    }

    private void refreshLanguageComboBox() {
        File languageFolder = new File("languages" + File.separator);
        if (!languageFolder.exists()) {
            // noinspection ResultOfMethodCallIgnored
            languageFolder.mkdir();
        }

        File[] availableLanguages = languageFolder.listFiles();

        if (availableLanguages == null || availableLanguages.length == 0) {
            JOptionPane.showMessageDialog(
                    THIS_FRAME,
                    UI_TEXTS.getProperty("no.language.files.found.message"),
                    "",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        languageComboBox.removeAllItems();

        for (File languagePack : availableLanguages) {
            String nameInSmallCase = languagePack.getName().toLowerCase();
            if (!languagePack.getName().equals(nameInSmallCase)) {
                // noinspection ResultOfMethodCallIgnored
                languagePack.renameTo(
                        new File(languagePack.getParent() + File.separator + nameInSmallCase));
            }
        }

        availableLanguages = languageFolder.listFiles();
        // The following line sorts the languages alphabetically
        Arrays.sort(availableLanguages);

        for (File languagePack : availableLanguages) {
            String language = languagePack.getName().toLowerCase();
            if (language.matches("^[a-zA-Z_]+( )?[a-zA-Z_0-9]+\\.lang$")) {
                language = language.replaceFirst
                        (language.charAt(0) + "", Character.toUpperCase(language.charAt(0)) + "");
                language = language.substring(0, language.indexOf(".lang"));
                languageComboBox.addItem(language);
            }
        }
    }

    private boolean languagePackIsValid(Properties languagePack) {
        Properties langPackTemplate = new Properties();
        try {
            langPackTemplate.load(getClass().getResourceAsStream("/langPackTemplate.properties"));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    UI_TEXTS.getProperty("unknown.problem.has.occurred.message"),
                    UI_TEXTS.getProperty("problem.message.dialogue.title"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (languagePack.keySet().size() != langPackTemplate.keySet().size()) {
            return false;
        }

        for (Object key : langPackTemplate.keySet()) {
            if (!languagePack.containsKey(key)) {
                return false;
            }
        }

        return true;
    }

    private class UIController extends SwingWorker<Void, Void> {

        private final File DESTINATION;
        private final String SAVE_NAME;
        // `GENERATION_STATUS` is only useful to choose the message to be shown to the user in case
        // of an error. The actual error/exception is properly handled using try/catch blocks. In fact,
        // the value for `GENERATION_STATUS` is set in the `catch` blocks of the corresponding error(s)
        private GenerationStatus GENERATION_STATUS;
        private final Properties LANGUAGE_PACK;
        private Extractor EXTRACTOR;

        private UIController(File DESTINATION, String SAVE_NAME, Properties LANGUAGE_PACK) {
            this.DESTINATION = DESTINATION;
            this.SAVE_NAME = SAVE_NAME;
            this.LANGUAGE_PACK = LANGUAGE_PACK;
            try {
                EXTRACTOR = new Extractor(LANGUAGE_PACK.getProperty("filter_for_minute"));
            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                        THIS_FRAME,
                        UI_TEXTS.getProperty("unknown.problem.has.occurred.message"),
                        UI_TEXTS.getProperty("problem.message.dialogue.title"),
                        JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        }

        @Override
        protected Void doInBackground() {
            toggleButtons();
            // Each publication has 2 operations associated with it. The first one is reading the
            // necessary entries and the second one is creating an excel sheet for each extract.
            // That is why the length of `EPUBFiles` needs to be multiplied by 2. The 1 added to
            // the product is for the final saving operation.
            final int UNIT_PROGRESS = progressBar.getMaximum() / (2 * EPUBFiles.length + 1);
            progressBar.setVisible(true);
            progressBar.setValue(progressBar.getMinimum());

            final ArrayList<PubExtract> ALL_PUB_EXTRACTS = new ArrayList<>();
            StringBuilder unparsedFileNames = new StringBuilder();

            for (File epubFile : EPUBFiles) {
                progressBar.setString(
                        UI_TEXTS.getProperty("reading.meeting.files.from") + " '" + epubFile.getName() + "'");
                try {
                    ALL_PUB_EXTRACTS.add(EXTRACTOR.getPublicationExtracts(epubFile));
                } catch (IllegalStateException e1) {
                    String fileName = epubFile.getName();
                    // 60 was chosen because the longest line in did.not.find.meeting.content.message
                    // is 65 characters. This would make the displayed name of a faulty file compact.
                    // If the file's name is any longer it would make the dialog displaying the message
                    // ugly and unorganized.
                    if (fileName.length() > 60) {
                        fileName = fileName.substring(0, 55) + "...";
                    }
                    unparsedFileNames.append("<li>").append(fileName).append("</li>");
                } catch (ZipException e2) {
                    GENERATION_STATUS = ZIP_FORMAT_ERROR;
                    return null;
                } catch (IOException e3) {
                    GENERATION_STATUS = COULD_NOT_READ_FILE_ERROR;
                    return null;
                }

                progressBar.setValue(progressBar.getValue() + UNIT_PROGRESS);
            }

            if (!unparsedFileNames.toString().equals("")) {
                JOptionPane.showMessageDialog(
                        THIS_FRAME,
                        String.format(
                                UI_TEXTS.getProperty("did.not.find.meeting.content.message"),
                                unparsedFileNames.toString()),
                        UI_TEXTS.getProperty("problem.message.dialogue.title"),
                        JOptionPane.ERROR_MESSAGE);
            }

//            if (settings.askToAssignPresenters()) {
//                new AssignmentDialog(THIS_FRAME, LANGUAGE_PACK, ALL_PUB_EXTRACTS).setVisible(true);
//                // TODO: The presenters assigned on the dialog must be returned in some way (a Map for example)
//            }

            ExcelFileGenerator excelFileGenerator = new ExcelFileGenerator(LANGUAGE_PACK, DESTINATION);
            for (PubExtract pubExtract : ALL_PUB_EXTRACTS) {
                progressBar.setString(
                        UI_TEXTS.getProperty("adding.an.Excel.sheet.for") + " '" + pubExtract.getPublicationName() + "'");
                excelFileGenerator.addPopulatedSheet
                        (pubExtract.getMeetings(), pubExtract.getPublicationName());
                progressBar.setValue(progressBar.getValue() + UNIT_PROGRESS);
            }

            progressBar.setString(UI_TEXTS.getProperty("saving.Excel.file"));

            try {
                excelFileGenerator.saveExcelDocument(SAVE_NAME);
                // If the above operation does not throw any Exceptions, then it can be confidently
                // concluded that the generation process went smoothly without any problems. Hence
                // the assignment of `SUCCESS` to `GENERATION_STATUS`.
                GENERATION_STATUS = SUCCESS;
            } catch (IOException e1) {
                GENERATION_STATUS = COULD_NOT_SAVE_FILE_ERROR;
                return null;
            }

            // `UNIT_PROGRESS` may be smaller than the required value to set the progress bar at
            // its maximum due to its type (`int` divisions are not exact). That would leave the
            // progress bar at an incomplete position while all operations are actually complete.
            // Therefore, after the last operation is complete, the progress bar will be set to its maximum.
            progressBar.setValue(progressBar.getMaximum());
            progressBar.setString(UI_TEXTS.getProperty("status.label.generation.finished.text"));

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
                    break;
                case SUCCESS:
                    JOptionPane.showMessageDialog
                            (THIS_FRAME, UI_TEXTS.getProperty("generation.successful.message"),
                                    UI_TEXTS.getProperty("done.message.dialogue.title"), JOptionPane.INFORMATION_MESSAGE);
                    break;
                default:
                    JOptionPane.showMessageDialog(THIS_FRAME, UI_TEXTS.getProperty("unknown.problem.has.occurred.message"),
                            UI_TEXTS.getProperty("problem.message.dialogue.title"), JOptionPane.ERROR_MESSAGE);
            }

            progressBar.setVisible(false);
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
        mainPanel.setLayout(new GridLayoutManager(2, 1, new Insets(10, 5, 10, 5), -1, -1));
        scrollPane = new JScrollPane();
        scrollPane.setBackground(new Color(-1));
        mainPanel.add(scrollPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        publicationTable = new JTable();
        publicationTable.setAutoCreateRowSorter(true);
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
        languageComboBox = new JComboBox();
        controlsPanel.add(languageComboBox, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(120, -1), null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        controlsPanel.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        controlsPanel.add(spacer2, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        progressBar = new JProgressBar();
        controlsPanel.add(progressBar, new GridConstraints(1, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
