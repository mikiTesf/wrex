package com.gui;

import com.domain.Presenter;
import com.domain.Privilege;
import com.extraction.PubExtract;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.meeting.Meeting;
import com.meeting.MeetingSection;
import com.meeting.Part;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.gui.CommonUIResources.UI_TEXTS;

public class AssignmentDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTabbedPane tabbedPane;
    // MS = Ministerial Servant 😅
    private final ArrayList<String> ELDER_MS_NAMES = new ArrayList<>();
    private final ArrayList<String> PUBLISHER_NAMES = new ArrayList<>();

    AssignmentDialog(JFrame parentFrame, ArrayList<PubExtract> pubExtracts) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        try {
            initializeNameArrayLists();
        } catch (SQLException e) {
            // could not fetch presenters message, then bye bye
            JOptionPane.showMessageDialog(
                    this,
                    UI_TEXTS.getProperty("could.not.fetch.presenter.details.message"),
                    UI_TEXTS.getProperty("problem.message.dialogue.title"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        createAssignmentTables(pubExtracts);

        setTitle(UI_TEXTS.getProperty("assignments.dialog.title"));
        setMinimumSize(new Dimension(650, 650));
        setLocationRelativeTo(parentFrame);
    }

    private void initializeNameArrayLists() throws SQLException {
        List<Presenter> allPresenters = Presenter.presenterDao.queryForAll();
        for (Presenter presenter : allPresenters) {
            if (
                presenter.getPrivilege() == Privilege.ELDER ||
                presenter.getPrivilege() == Privilege.MINISTERIAL_SERVANT)
            {
                ELDER_MS_NAMES.add(presenter.getFullName());
            } else {
                PUBLISHER_NAMES.add(presenter.getFullName());
            }
        }
    }

    private void createAssignmentTables(ArrayList<PubExtract> pubExtracts) {
        for (PubExtract pubExtract : pubExtracts) {
            JPanel panel = new JPanel(new GridLayout(pubExtract.getMeetings().size(), 1));

            for (Meeting meeting : pubExtract.getMeetings()) {
                JPanel assignmentPanel = new JPanel(new GridLayout());
                assignmentPanel.setBorder(BorderFactory.createTitledBorder(
                    new EtchedBorder(), meeting.getWEEK_SPAN(), TitledBorder.LEFT, TitledBorder.TOP));

                JTable assignmentTable = getAssignmentTable();
                DefaultTableModel defaultTableModel = (DefaultTableModel) assignmentTable.getModel();
                insertPresenterSelectors(meeting.getTREASURES(), defaultTableModel);
                insertPresenterSelectors(meeting.getIMPROVE_IN_MINISTRY(), defaultTableModel);
                insertPresenterSelectors(meeting.getLIVING_AS_CHRISTIANS(), defaultTableModel);

                assignmentTable.setModel(defaultTableModel);
                assignmentPanel.add(assignmentTable);
                panel.add(assignmentPanel);
            }

            JScrollPane scrollPane = new JScrollPane(
                    panel,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            tabbedPane.addTab(pubExtract.getPublicationName(), scrollPane);
        }
    }

    private JTable getAssignmentTable() {
        JTable assignmentTable = new JTable() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        };
        assignmentTable.setRowHeight(25);
        assignmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        DefaultTableModel defaultTableModel = new DefaultTableModel();
        defaultTableModel.addColumn(UI_TEXTS.getProperty("part.name.table.column.header"));
        defaultTableModel.addColumn(UI_TEXTS.getProperty("presenter.name.table.column.header"));
        assignmentTable.setModel(defaultTableModel);

        return assignmentTable;
    }

    private void insertPresenterSelectors(MeetingSection meetingSection, DefaultTableModel defaultTableModel) {
        for (Part part : meetingSection.getParts()) {
//            JComboBox<String> presentersComboBox;
//
//            switch (meetingSection.getSECTION_KIND()) {
//                case TREASURES:
//                case LIVING_AS_CHRISTIANS:
//                    presentersComboBox = new JComboBox(ELDER_MS_NAMES.toArray(new String[]{}));
//                    break;
//                // Here, it is assumed that every publisher can be a candidate for the parts under
//                // Improve In Ministry
//                default: // IMPROVE_IN_MINISTRY
//                    presentersComboBox = new JComboBox(PUBLISHER_NAMES.toArray(new String[]{}));
//                    break;
//            }

            defaultTableModel.addRow(new Object[]{part.getPartTitle(), part.getPartTitle()});
        }
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
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
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabPlacement(1);
        contentPane.add(tabbedPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
