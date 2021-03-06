package net.sf.sketchlet.designer.editor.ui.timers;

import net.sf.sketchlet.common.dnd.GenericTableTransferHandler;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.timers.curve.CurvesFrame;
import net.sf.sketchlet.help.HelpUtils;
import net.sf.sketchlet.framework.model.programming.timers.Timer;
import net.sf.sketchlet.framework.model.programming.timers.Timers;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author zobrenovic
 */
public class TimersTablePanel extends JPanel {

    public JTable table;
    public AbstractTableModel model = new MyTableModel();
    JButton btnNew = new JButton(Workspace.createImageIcon("resources/add.gif"));
    JButton btnEdit = new JButton(Workspace.createImageIcon("resources/open.gif"));
    JButton btnDelete = new JButton(Workspace.createImageIcon("resources/remove.gif"));
    JButton btnCurves = new JButton(Workspace.createImageIcon("resources/curve.png"));
    JButton help = new JButton(Workspace.createImageIcon("resources/help-browser.png", ""));

    public TimersTablePanel() {
        createGUI();
    }

    public void createGUI() {
        setLayout(new BorderLayout());
        JToolBar toolbar = new JToolBar();
        JToolBar toolbarHelp = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder());
        toolbarHelp.setFloatable(false);
        toolbarHelp.setBorder(BorderFactory.createEmptyBorder());
        toolbar.add(btnNew);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        toolbar.add(btnCurves);

        btnNew.setToolTipText(Language.translate("Create a new timer"));
        btnEdit.setToolTipText(Language.translate("Edit the selected timer"));
        btnDelete.setToolTipText(Language.translate("Delete the selected timer"));
        btnCurves.setToolTipText(Language.translate("Open the timer curves dialog"));

        btnNew.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                Timers.getGlobalTimers().addNewTimer();
                model.fireTableDataChanged();
                SketchletEditor.getInstance().getExtraEditorPanel().timersExtraPanel.showTimers(Timers.getGlobalTimers().getTimers().size() - 1);
            }
        });
        btnEdit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    SketchletEditor.getInstance().getExtraEditorPanel().timersExtraPanel.showTimers(row);
                }
            }
        });
        btnDelete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    // TimersFrame.hideTimers();
                    Timers.getGlobalTimers().getTimers().remove(row);
                    model.fireTableDataChanged();
                    SketchletEditor.getInstance().getExtraEditorPanel().timersExtraPanel.load();
                }
            }
        });
        btnCurves.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                CurvesFrame.showFrame();
            }
        });
        toolbarHelp.add(help);
        help.setToolTipText("What are Timers?");
        help.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                HelpUtils.openHelpFile("Timers", "timers");
            }
        });

        JPanel panelNorth = new JPanel(new BorderLayout());
        panelNorth.add(toolbar);
        panelNorth.add(toolbarHelp, BorderLayout.EAST);
        add(panelNorth, BorderLayout.NORTH);

        this.putClientProperty("JComponent.sizeVariant", "mini");

        table = new JTable(model);
        table.setTableHeader(null);
        table.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(table);
        table.setDragEnabled(true);
        table.setTransferHandler(new GenericTableTransferHandler("@timer ", 0));

        ListSelectionModel rowSM = table.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) {
                    return;
                }

                enableControls();
            }
        });

        table.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        SketchletEditor.getInstance().getExtraEditorPanel().timersExtraPanel.showTimers(Timers.getGlobalTimers().getTimers().elementAt(row).getName());
                    }
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(table);
        add(tableScroll, BorderLayout.CENTER);

        enableControls();
    }

    public void enableControls() {
        int row = table.getSelectedRow();
        boolean bEnable = row >= 0;

        btnEdit.setEnabled(bEnable);
        btnDelete.setEnabled(bEnable);
    }

    class MyTableModel extends AbstractTableModel {

        private String[] columnNames = {
                "Timer"
        };

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return Timers.getGlobalTimers().getTimers().size();
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            Timer timer = Timers.getGlobalTimers().getTimers().elementAt(row);
            return timer.getName();
        }
    }
}
