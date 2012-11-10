package net.sf.sketchlet.designer.programming.screenscripts;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 * @author cuypers
 */

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.data.Evaluator;
import org.apache.log4j.Logger;

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
 * TableRenderDemo is just like TableDemo, except that it explicitly initializes
 * column sizes and it uses a combo box as an editor for the Sport column.
 */
public class ActionListTable extends JPanel {
    private static final Logger log = Logger.getLogger(ActionListTable.class);
    private boolean DEBUG = false;
    Vector<RobotAction> actions = new Vector<RobotAction>();
    MyTableModel model;
    JPanel controlPanel = new JPanel();
    JButton btnRemove = new JButton(Language.translate("Remove"));
    JButton btnMoveUp = new JButton(Language.translate("Move Up"));
    JButton btnMoveDown = new JButton(Language.translate("Move Down"));
    JButton btnTest = new JButton(Language.translate("Test"));
    CaptureFrame captureFrame;
    JTable table;
    int currentRow;

    public ActionListTable(CaptureFrame captureFrame) {
        super(new BorderLayout());

        this.captureFrame = captureFrame;

        model = new MyTableModel();
        table = new JTable(model);
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
            for (RobotAction a : actions) {
                String strParams = Evaluator.processText(a.parameters, "", "");
                a.doAction(strParams);
            }
            this.getParent().setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void remove() {
        int index = this.table.getSelectedRow();
        this.actions.remove(index);
        this.model.fireTableDataChanged();
        this.captureFrame.imageArea.repaint();
    }

    public void moveUp() {
        int index = this.table.getSelectedRow();
        RobotAction action = this.actions.remove(index);
        actions.insertElementAt(action, index - 1);
        this.model.fireTableDataChanged();
        this.table.getSelectionModel().setSelectionInterval(index - 1, index - 1);
    }

    public void moveDown() {
        int index = this.table.getSelectedRow();
        RobotAction action = this.actions.remove(index);
        actions.insertElementAt(action, index + 1);
        this.model.fireTableDataChanged();
        this.table.getSelectionModel().setSelectionInterval(index + 1, index + 1);
    }

    /*
     * This method picks good column sizes. If all column heads are wider than
     * the column's cells' contents, then you can just use
     * column.sizeWidthToFit().
     */
    private void initColumnSizes(JTable table) {
        MyTableModel model = (MyTableModel) table.getModel();
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;

        Object[] longValues = model.longValues;

        TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();

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

            log.debug("Initializing width of column " + i + ". " + "headerWidth = " + headerWidth + "; cellWidth = " + cellWidth);

            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }

    public void setUpSportColumn(JTable table,
                                 TableColumn sportColumn) {
        //Set up the editor for the sport cells.
        JComboBox comboBox = new JComboBox();

        comboBox.addItem("Pause");
        comboBox.addItem("Update Variable");

        comboBox.addItem("Mouse - Move Cursor");
        comboBox.addItem("Mouse - Left Button Click");
        comboBox.addItem("Mouse - Left Button Double Click");
        comboBox.addItem("Mouse - Left Button Press");
        comboBox.addItem("Mouse - Left Button Release");
        comboBox.addItem("Mouse - Right Button Click");
        comboBox.addItem("Mouse - Right Button Double Click");
        comboBox.addItem("Mouse - Right Button Press");
        comboBox.addItem("Mouse - Right Button Release");

        comboBox.addItem("Keyboard - Paste Text");

        comboBox.addItem("Keyboard - SHIFT Pressed");
        comboBox.addItem("Keyboard - SHIFT Released");

        comboBox.addItem("Keyboard - CTRL Pressed");
        comboBox.addItem("Keyboard - CTRL Released");

        comboBox.addItem("Keyboard - ALT Pressed");
        comboBox.addItem("Keyboard - ALT Released");

        comboBox.addItem("Keyboard - ESC");
        comboBox.addItem("Keyboard - ESC Pressed");
        comboBox.addItem("Keyboard - ESC Released");

        comboBox.addItem("Keyboard - Home");
        comboBox.addItem("Keyboard - Home Pressed");
        comboBox.addItem("Keyboard - Home Released");

        comboBox.addItem("Keyboard - End");
        comboBox.addItem("Keyboard - End Pressed");
        comboBox.addItem("Keyboard - End Released");

        comboBox.addItem("Keyboard - PageUp");
        comboBox.addItem("Keyboard - PageUp Pressed");
        comboBox.addItem("Keyboard - PageUp Released");

        comboBox.addItem("Keyboard - PageDown");
        comboBox.addItem("Keyboard - PageDown Pressed");
        comboBox.addItem("Keyboard - PageDown Released");

        comboBox.addItem("Keyboard - Left Arrow");
        comboBox.addItem("Keyboard - Left Arrow Pressed");
        comboBox.addItem("Keyboard - Left Arrow Released");

        comboBox.addItem("Keyboard - Right Arrow");
        comboBox.addItem("Keyboard - Right Arrow Pressed");
        comboBox.addItem("Keyboard - Right Arrow Released");

        comboBox.addItem("Keyboard - Up Arrow");
        comboBox.addItem("Keyboard - Up Arrow Pressed");
        comboBox.addItem("Keyboard - Up Arrow Released");

        comboBox.addItem("Keyboard - Down Arrow");
        comboBox.addItem("Keyboard - Down Arrow Pressed");
        comboBox.addItem("Keyboard - Down Arrow Released");

        comboBox.addItem("Keyboard - F1");
        comboBox.addItem("Keyboard - F1 Pressed");
        comboBox.addItem("Keyboard - F1 Released");


        // sportColumn.setCellEditor(new DefaultCellEditor(comboBox));

        //Set up tool tips for the sport cells.
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setToolTipText("Click for combo box");
        sportColumn.setCellRenderer(renderer);
    }

    public void addRobotAction(RobotAction action) {
        actions.add(action);
        model.fireTableDataChanged();
    }

    class MyTableModel extends AbstractTableModel {

        private String[] columnNames = {Language.translate("Action"),
                Language.translate("Parameters"),
                Language.translate("Description")
        };
        public String[] longValues = {
                "Action name", "100, 100", "This is a description"
        };

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return actions.size();
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            RobotAction action = actions.elementAt(row);

            switch (col) {
                case 0:
                    return action.name;
                case 1:
                    return action.parameters;
                case 2:
                    return action.description;
            }

            return "";
        }

        /*
         * JTable uses this method to determine the default renderer/ editor for
         * each cell. If we didn't implement this method, then the last column
         * would contain text ("true"/"false"), rather than a check box.
         */
        public Class getColumnClass(int c) {
            return String.class;
        }

        /*
         * Don't need to implement this method unless your table's editable.
         */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            return col == 1;
        }

        /*
         * Don't need to implement this method unless your table's data can
         * change.
         */
        public void setValueAt(Object value, int row, int col) {
            log.debug("Setting value at " + row + "," + col + " to " + value + " (an instance of " + value.getClass() + ")");

            RobotAction action = actions.elementAt(row);

            switch (col) {
                case 0:
                    action.name = (String) value;
                    break;
                case 1:
                    action.parameters = (String) value;
                    break;
                case 2:
                    action.description = (String) value;
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
