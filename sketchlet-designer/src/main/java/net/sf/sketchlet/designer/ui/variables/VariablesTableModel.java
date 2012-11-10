/*
 * VariablesTableModel.java
 *
 * Created on April 21, 2008, 2:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.ui.variables;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.communicator.server.Variable;
import net.sf.sketchlet.communicator.server.VariableGroupComparator;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.Collections;
import java.util.Vector;

public class VariablesTableModel extends AbstractTableModel {

    public int columnWidths[] = {120, 105, 200, 100, 50, 50, 50, 50, 100, 100, 120};
    VariablesPanel mainFrame;
    public static String strFilter = "";
    public static String strSort = "time";
    public static Vector<Variable> variableRows = new Vector<Variable>();
    public static VariablesTableModel model = null;

    public VariablesTableModel(VariablesPanel mainFrame) {
        this.mainFrame = mainFrame;
        model = this;
    }

    public String[] getColumnNames() {
        String[] columnNames = {Language.translate("Global Variable"), Language.translate("Value"), Language.translate("Description"), Language.translate("Module"), Language.translate("Format"), Language.translate("Min"), Language.translate("Max"), Language.translate("Count"), Language.translate("Count filter"), Language.translate("Time filter (ms)"), Language.translate("Timestamp")};
        return columnNames;
    }

    public int getVariableRow(String strVar) {
        int row = 0;
        synchronized (variableRows) {
            for (Variable v : variableRows) {
                if (v.name.equalsIgnoreCase(strVar)) {
                    return row;
                }
                row++;
            }
        }
        return -1;
    }

    public boolean bSorting = false;

    public void sortAndFilter() {
        if (bSorting) {
            return;
        }
        bSorting = true;
        boolean paused = DataServer.paused;
        DataServer.paused = true;
        variableRows.removeAllElements();

        if (DataServer.variablesServer == null || DataServer.variablesServer.variablesVector == null) {
            DataServer.paused = paused;
            bSorting = false;
            return;
        }

        Vector<Variable> _variables = new Vector<Variable>();
        synchronized (DataServer.variablesServer.variablesVector) {
            if (DataServer.variablesServer != null) {
                String prevGroup = "";
                for (String strVar : DataServer.variablesServer.variablesVector) {
                    if (strFilter.length() == 0 || strVar.contains(strFilter)) {
                        Variable v = DataServer.variablesServer.getVariable(strVar);
                        _variables.add(v);
                    }
                }
            }
        }

        if (strSort.equals("name")) {
            Collections.sort(_variables, new Variable());
            for (Variable v : _variables) {
                variableRows.add(v);
            }
        } else if (strSort.equals("group")) {
            Collections.sort(_variables, new VariableGroupComparator());

            Vector<Variable> groupedVariables = new Vector<Variable>();
            String prevGroup = "";
            for (Variable v : _variables) {
                if (!v.group.trim().equalsIgnoreCase(prevGroup)) {
                    prevGroup = v.group;
                    Variable separator = new Variable();
                    separator.name = v.group;
                    separator.value = null;
                    groupedVariables.add(separator);
                }
                groupedVariables.add(v);
            }
            for (Variable v : groupedVariables) {
                variableRows.add(v);
            }
        } else {
            for (Variable v : _variables) {
                variableRows.add(v);
            }
        }

        DataServer.paused = paused;
        bSorting = false;
    }

    public int getColumnCount() {
        return getColumnNames().length;
    }

    public int getRowCount() {
        this.sortAndFilter();
        return this.variableRows.size() + 1;
    }

    public String getColumnName(int col) {
        return getColumnNames()[col];
    }

    public Object getValueAt(int row, int col) {
        Variable v = null;
        synchronized (variableRows) {
            this.sortAndFilter();
            if (row >= this.variableRows.size() || this.variableRows.size() == 0) {
                return "";
            }

            v = this.variableRows.elementAt(row);
        }

        if (v != null) {
            switch (col) {
                case 0:
                    return v.name;
                case 1:
                    if (v.value == null) {
                        return "";
                    } else if (v.value.length() > 256) {
                        return v.value.substring(0, 50) + "...";
                    } else {
                        return v.value;
                    }
                case 2:
                    return v.description;
                case 3:
                    return v.group;
                case 4:
                    return v.format + "";
                case 5:
                    return v.min + "";
                case 6:
                    return v.max + "";
                case 7:
                    return v.count + "";
                case 8:
                    return v.countFilter + "";
                case 9:
                    return v.timeFilterMs + "";
                case 10:
                    return v.timestamp + "";
            }
        }

        return "";
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
        this.sortAndFilter();
        if (row == this.variableRows.size()) {
            return col == 0;
        } else if (col >= 1) {
            if (row >= this.variableRows.size()) {
                return false;
            }

            Variable v = this.variableRows.elementAt(row);

            if (v.value == null || v.value.length() > 500) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public void setValueAt(Object value, int row, int col) {
        this.sortAndFilter();
        if (row == this.variableRows.size()) {
            if (col == 0) {
                for (Variable v : variableRows) {
                    if (v.name.equalsIgnoreCase(value.toString())) {
                        JOptionPane.showMessageDialog(mainFrame, "The variable '" + value.toString() + "' already exists.");
                        return;
                    }
                }
                DataServer.variablesServer.addVariable(value.toString(), "", "");
            }
        } else {
            Variable v = this.variableRows.elementAt(row);
            DataServer.variablesServer.updateVariable(v, col, value.toString());
            if (VariablesTablePanel.variablesTableInterface != null) {
                VariablesTablePanel.variablesTableInterface.variableTableUpdate(v.name, value.toString(), mainFrame.globalVariablesPanel.scrollPane);
            }
        }
    }

    public DataServer getDataServer() {
        return DataServer.variablesServer;
    }
}
