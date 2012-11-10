/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.sketchlet.designer.ui.macros;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class MacroTableCellRenderer extends JComboBox implements TableCellRenderer {
    public MacroTableCellRenderer(String[] items) {
        super(items);
    }

    public MacroTableCellRenderer() {
        super();
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());
        } else {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }

        // Select the current value
        setSelectedItem(value);
        return this;
    }
}
