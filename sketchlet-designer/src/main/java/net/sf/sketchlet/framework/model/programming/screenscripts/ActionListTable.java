package net.sf.sketchlet.framework.model.programming.screenscripts;

import net.sf.sketchlet.framework.blackboard.evaluator.Evaluator;
import net.sf.sketchlet.common.translation.Language;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

public class ActionListTable extends JPanel {
    private static final Logger log = Logger.getLogger(ActionListTable.class);
    private java.util.List<RobotAction> actions = new Vector<RobotAction>();
    private ActionListTableModel tableModel;
    private JPanel controlPanel = new JPanel();
    private JButton btnRemove = new JButton(Language.translate("Remove"));
    private JButton btnMoveUp = new JButton(Language.translate("Move Up"));
    private JButton btnMoveDown = new JButton(Language.translate("Move Down"));
    private JButton btnTest = new JButton(Language.translate("Test"));
    private CaptureFrame captureFrame;
    private JTable table;
    private int currentRow;

    public ActionListTable(CaptureFrame captureFrame) {
        super(new BorderLayout());

        this.captureFrame = captureFrame;

        setTableModel(new ActionListTableModel());
        table = new JTable(getTableModel());
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        table.setPreferredScrollableViewportSize(new Dimension(500, 60));
        table.setFillsViewportHeight(true);

        this.setBorder(javax.swing.BorderFactory.createTitledBorder(Language.translate("Mouse and Keyboard Actions")));

        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);

        //Set up column sizes.
//        initColumnSizes(table);

        //Fiddle with the Sport column's cell editors/renderers.
//        setUpSportColumn(table, table.getColumnModel().getColumn(0));

        //Add the scroll pane to this panel.
        add(scrollPane);

        btnRemove.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                remove();
            }
        });
        btnMoveUp.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                moveUp();
            }
        });
        btnMoveDown.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                moveDown();
            }
        });

        btnTest.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                test();
            }
        });

        controlPanel.add(btnRemove);
        controlPanel.add(new JLabel("  "));
        controlPanel.add(btnMoveUp);
        controlPanel.add(btnMoveDown);
        controlPanel.add(new JLabel("  "));
        controlPanel.add(btnTest);

        ListSelectionModel rowSM = table.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) {
                    return;
                }

                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (lsm.isSelectionEmpty()) {
                } else {
                    int selectedRow = lsm.getMinSelectionIndex();
                    currentRow = selectedRow;
                }

                enableControls();
            }
        });

        enableControls();

        add(controlPanel, BorderLayout.SOUTH);
    }

    public void enableControls() {
        int index = this.table.getSelectedRow();

        this.btnRemove.setEnabled(index >= 0);
        this.btnMoveUp.setEnabled(index > 0);
        this.btnMoveDown.setEnabled(index >= 0 && index < this.table.getRowCount() - 1);
    }

    public void test() {
        try {
            this.getParent().setVisible(false);
            Thread.sleep(500);
            for (RobotAction a : getActions()) {
                String strParams = Evaluator.processText(a.getParameters(), "", "");
                a.doAction(strParams);
            }
            this.getParent().setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void remove() {
        int index = this.table.getSelectedRow();
        this.getActions().remove(index);
        this.getTableModel().fireTableDataChanged();
        this.captureFrame.getImageArea().repaint();
    }

    public void moveUp() {
        int index = this.table.getSelectedRow();
        RobotAction action = this.getActions().remove(index);
        getActions().set(index - 1, action);
        this.getTableModel().fireTableDataChanged();
        this.table.getSelectionModel().setSelectionInterval(index - 1, index - 1);
    }

    public void moveDown() {
        int index = this.table.getSelectedRow();
        RobotAction action = this.getActions().remove(index);
        getActions().set(index + 1, action);
        this.getTableModel().fireTableDataChanged();
        this.table.getSelectionModel().setSelectionInterval(index + 1, index + 1);
    }

    public void addRobotAction(RobotAction action) {
        getActions().add(action);
        getTableModel().fireTableDataChanged();
    }

    public java.util.List<RobotAction> getActions() {
        return actions;
    }

    public ActionListTableModel getTableModel() {
        return tableModel;
    }

    public void setTableModel(ActionListTableModel tableModel) {
        this.tableModel = tableModel;
    }

    class ActionListTableModel extends AbstractTableModel {

        private String[] columnNames = {Language.translate("Action"),
                Language.translate("Parameters"),
                Language.translate("Description")
        };

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public int getRowCount() {
            return getActions().size();
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            RobotAction action = getActions().get(row);

            switch (col) {
                case 0:
                    return action.getName();
                case 1:
                    return action.getParameters();
                case 2:
                    return action.getDescription();
            }

            return "";
        }

        @Override
        public Class getColumnClass(int c) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 1;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            log.debug("Setting value at " + row + "," + col + " to " + value + " (an instance of " + value.getClass() + ")");

            RobotAction action = getActions().get(row);

            switch (col) {
                case 0:
                    action.setName((String) value);
                    break;
                case 1:
                    action.setParameters((String) value);
                    break;
                case 2:
                    action.setDescription((String) value);
                    break;
            }

            fireTableCellUpdated(row, col);

            log.debug("New value of data:");
            printDebugData();
        }

        private void printDebugData() {
        }
    }
}
