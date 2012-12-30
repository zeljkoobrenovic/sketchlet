package net.sf.sketchlet.designer.editor.ui;

import net.sf.sketchlet.framework.model.Project;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

/**
 * @author zobrenovic
 */
public class PreviewList extends JPanel {

    JTable tableSketches;
    public SketchesTableModel tableModelSketches;
    String[] columnNamesSketches = {"Sketch"};

    public PreviewList() {
        this.tableModelSketches = new SketchesTableModel();
        // this.tableModelSketches.refreshData();
        tableSketches = new JTable(this.tableModelSketches);
        tableSketches.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        tableSketches = new JTable();
        tableSketches.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        tableSketches.setRowHeight(100);
        tableSketches.getColumnModel().getColumn(1).setCellRenderer(new SketchCellRenderer(true));
        tableSketches.getSelectionModel().addListSelectionListener(new SketchRowListener());

        JScrollPane scrollPaneSketches = new JScrollPane(tableSketches);

        add(scrollPaneSketches);
    }

    class SketchesTableModel extends AbstractTableModel {

        Object data[][];

        public void refreshData() {
            data = Project.getSketchInfoFromDir();
            this.fireTableDataChanged();
        }

        public String getColumnName(int col) {
            return columnNamesSketches[col].toString();
        }

        public int getRowCount() {
            if (data == null) {
                return 0;
            } else {
                return data.length;
            }
        }

        public int getColumnCount() {
            return columnNamesSketches.length;
        }

        public Object getValueAt(int row, int col) {
            return data[row][1];
        }

        public boolean isCellEditable(int row, int col) {
            return false;
        }

        public void setValueAt(Object value, int row, int col) {
        }
    }

    int sketchRow = -1;

    private class SketchRowListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent event) {
            if (event.getValueIsAdjusting()) {
                return;
            }
            sketchRow = tableSketches.getSelectedRow();

            if (sketchRow >= 0) {
            }
        }
    }


}
