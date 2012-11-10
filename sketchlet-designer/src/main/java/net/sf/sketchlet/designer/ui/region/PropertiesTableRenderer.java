/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.sketchlet.designer.ui.region;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * @author zobrenovic
 */
public class PropertiesTableRenderer extends DefaultTableCellRenderer {

    Object[][] data;
    Color color = this.getBackground();
    Font font = this.getFont();

    public PropertiesTableRenderer(Object[][] data) {
        this.data = data;
        setOpaque(true); //MUST do this for background to show up.
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (data[row][1] == null) {
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
