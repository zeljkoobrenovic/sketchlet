/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.ui.page.spreadsheet;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import net.sf.sketchlet.designer.Workspace;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * @author zobrenovic
 */
public class SpreadsheetCellRenderer extends DefaultTableCellRenderer {

    Color color = this.getBackground();
    Font font = this.getFont();
    SpreadsheetPanel panel;

    public SpreadsheetCellRenderer(SpreadsheetPanel panel) {
        this.panel = panel;
        setOpaque(true); //MUST do this for background to show up.
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (column == 0) {
            setForeground(Color.BLACK);
            setBackground(Color.LIGHT_GRAY);
            setFont(font);
        } else if (Workspace.getPage().getSpreadsheetCellValue(row, column).startsWith("=") && !isSelected) {
            setForeground(Color.BLACK);
            setBackground(Color.YELLOW);
            setFont(font);
        } else {
            if (Workspace.getPage().isMasterCell(row, column)) {
                setForeground(Color.GRAY);
            } else {
                setForeground(Color.BLACK);
            }
            setBackground(this.color);
            setFont(font);
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
        return this;
    }
}
