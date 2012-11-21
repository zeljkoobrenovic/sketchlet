/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.ui.variables;

import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.communicator.server.Variable;
import net.sf.sketchlet.designer.Workspace;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class SelectVariables extends JDialog {

    String columnNames[] = new String[]{"", "Variable"};
    Boolean selectedVariables[] = new Boolean[DataServer.getInstance().getNumberOfVariables()];
    AbstractTableModel tableModel = new AbstractTableModel() {

        public String getColumnName(int col) {
            return columnNames[col].toString();
        }

        public int getRowCount() {
            return DataServer.getInstance().getNumberOfVariables();
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public Object getValueAt(int row, int col) {
            Variable v = DataServer.getInstance().getVariable(row);
            if (v != null) {
                switch (col) {
                    case 0:
                        return selectedVariables[row];
                    case 1:
                        return v.getName();
                }
            }
            return "";
        }

        public boolean isCellEditable(int row, int col) {
            return col == 0;
        }

        public void setValueAt(Object value, int row, int col) {
            if (col == 0) {
                selectedVariables[row] = (Boolean) value;
            }
        }

        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }
    };
    final JTable table = new JTable(tableModel);
    public String result = null;

    public SelectVariables(Frame frame) {
        this(frame, null);
    }

    public SelectVariables(Frame frame, Vector<String> variables) {
        setTitle("Select Variables");
        setModal(true);
        setLayout(new BorderLayout());

        for (int i = 0; i < selectedVariables.length; i++) {
            Variable v = DataServer.getInstance().getVariable(i);
            if (variables == null || !variables.contains(v.getName())) {
                selectedVariables[i] = new Boolean(false);
            } else {
                selectedVariables[i] = new Boolean(true);
            }
        }

        JPanel buttons = new JPanel();
        JButton btnOK = new JButton("OK", Workspace.createImageIcon("resources/ok.png", ""));
        JButton btnCancel = new JButton("Cancel", Workspace.createImageIcon("resources/cancel.png", ""));

        buttons.add(btnOK);
        buttons.add(btnCancel);

        btnOK.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                result = "";
                for (int i = selectedVariables.length - 1; i >= 0; i--) {
                    if (selectedVariables[i].booleanValue()) {
                        result = DataServer.getInstance().getVariable(i).getName() + (result.length() > 0 ? "," + result : "");
                    }
                }

                result = result.trim();
                setVisible(false);
            }
        });
        btnCancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                result = null;
                setVisible(false);
            }
        });

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setPreferredWidth(30);
        column = table.getColumnModel().getColumn(1);
        column.setPreferredWidth(400);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        pack();
        setVisible(true);
    }
}
