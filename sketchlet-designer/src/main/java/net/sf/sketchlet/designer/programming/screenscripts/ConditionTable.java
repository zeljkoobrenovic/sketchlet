package net.sf.sketchlet.designer.programming.screenscripts;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author cuypers
 */

import net.sf.sketchlet.designer.ui.UIUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * TableRenderDemo is just like TableDemo, except that it
 * explicitly initializes column sizes and it uses a combo box
 * as an editor for the Sport column.
 */
public class ConditionTable extends JPanel {

    Vector<Condition> conditions = new Vector<Condition>();
    JPanel controlPanel = new JPanel();
    JButton btnAdd = new JButton("Add");
    JButton btnDelete = new JButton("Remove");
    CaptureFrame captureFrame;
    MyTableModel model;
    JTable table;
    JRadioButton allRadio = new JRadioButton("When ALL conditions are true", true);
    JRadioButton anyRadio = new JRadioButton("When ANY condition is true", false);

    int currentRow;

    public ConditionTable(CaptureFrame captureFrame) {
        super(new BorderLayout());

        this.captureFrame = captureFrame;

        model = new MyTableModel();
        table = new JTable(model);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);


        this.setBorder(javax.swing.BorderFactory.createTitledBorder("Condition (when to execute this)"));
        table.setPreferredScrollableViewportSize(new Dimension(500, 50));
        table.setFillsViewportHeight(true);

        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);

        //Set up column sizes.
//        initColumnSizes(table);


        setOperatorColumn(table, table.getColumnModel().getColumn(1));

        final JComboBox varCombo = new JComboBox();
        UIUtils.populateVariablesCombo(varCombo, false);
        table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(varCombo));

        //Add the scroll pane to this panel.
        add(scrollPane);

        btnAdd.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                addCondition(new Condition());
            }
        });

        btnDelete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int sel = table.getSelectedRow();

                if (sel >= 0) {
                    conditions.removeElementAt(sel);
                    model.fireTableDataChanged();
                }
            }
        });

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

        controlPanel.setLayout(new BorderLayout());

        JPanel controlCenter = new JPanel();
        controlCenter.add(btnAdd);
        controlCenter.add(btnDelete);

        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new GridLayout(0, 1));

        ButtonGroup radioGroup = new ButtonGroup();
        radioGroup.add(this.allRadio);
        radioGroup.add(this.anyRadio);

        radioPanel.add(this.allRadio);
        radioPanel.add(this.anyRadio);

        controlPanel.add(controlCenter, BorderLayout.CENTER);
        controlPanel.add(radioPanel, BorderLayout.SOUTH);

        add(controlPanel, BorderLayout.EAST);
    }

    public void enableControls() {
        btnDelete.setEnabled(table.getSelectedRow() >= 0);
    }

    public void setOperatorColumn(JTable table,
                                  TableColumn operatorColumn) {
        //Set up the editor for the sport cells.
        JComboBox comboBox = new JComboBox();
        comboBox.addItem("=");
        comboBox.addItem("<>");
        comboBox.addItem(">");
        comboBox.addItem(">=");
        comboBox.addItem("<");
        comboBox.addItem("<=");
        comboBox.addItem("in");
        comboBox.addItem("not in");
        comboBox.addItem("updated");

        operatorColumn.setCellEditor(new DefaultCellEditor(comboBox));

        //Set up tool tips for the sport cells.
        DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer();
        renderer.setToolTipText("Click for combo box");
        operatorColumn.setCellRenderer(renderer);
    }

    /*
     * This method picks good column sizes.
     * If all column heads are wider than the column's cells'
     * contents, then you can just use column.sizeWidthToFit().
     */
    private void initColumnSizes(JTable table) {
        MyTableModel model = (MyTableModel) table.getModel();
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;

        Object[] longValues = model.longValues;

        TableCellRenderer headerRenderer =
                table.getTableHeader().getDefaultRenderer();

        for (int i = 0; i < 3; i++) {
            column = table.getColumnModel().getColumn(i);

            comp = headerRenderer.getTableCellRendererComponent(
                    null, column.getHeaderValue(),
                    false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;

            comp = table.getDefaultRenderer(model.getColumnClass(i)).
                    getTableCellRendererComponent(
                            table, longValues[i],
                            false, false, 0, i);
            cellWidth = comp.getPreferredSize().width;

            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }

    public void addCondition(Condition action) {
        conditions.add(action);
        model.fireTableDataChanged();
    }

    class MyTableModel extends AbstractTableModel {

        private String[] columnNames = {"Variable",
                "Operator",
                "Value"
        };
        public String[] longValues = {
                "Action name", "100, 100", "This is a description"
        };

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return conditions.size();
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            Condition action = conditions.elementAt(row);

            switch (col) {
                case 0:
                    return action.variable;
                case 1:
                    return action.operator;
                case 2:
                    return action.value;
            }

            return "";
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public Class getColumnClass(int c) {
            return String.class;
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            return true;
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        public void setValueAt(Object value, int row, int col) {
            Condition action = conditions.elementAt(row);

            switch (col) {
                case 0:
                    action.variable = (String) value;
                    break;
                case 1:
                    action.operator = (String) value;
                    break;
                case 2:
                    action.value = (String) value;
                    break;
            }

            fireTableCellUpdated(row, col);
        }
    }
}

