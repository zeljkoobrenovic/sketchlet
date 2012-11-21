/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.ui.variables;

import net.sf.sketchlet.communicator.server.Variable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * @author zobrenovic
 */
public class VariablesTableRenderer extends DefaultTableCellRenderer {

    Color color = this.getBackground();
    Font font = this.getFont();

    public VariablesTableRenderer() {
        setOpaque(true); //MUST do this for background to show up.
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (row >= VariablesTableModel.variableRows.size()) {
            setBackground(this.color);
            setFont(font);
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
        Variable v = VariablesTableModel.variableRows.elementAt(row);
        if (v == null || v.getValue() == null) {
            setBackground(Color.LIGHT_GRAY);
            setFont(font.deriveFont(Font.BOLD));
            this.setBorder(null);
            return this;
        } else {
            setBackground(this.color);
            setFont(font);
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
}
