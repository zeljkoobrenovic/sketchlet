/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.ui.page.spreadsheet;

import net.sf.sketchlet.common.dnd.StringTransferHandler;

import javax.swing.*;

public class SpreadsheetTransferHandler extends StringTransferHandler {
    public SpreadsheetTransferHandler() {
    }

    protected String exportString(JComponent c) {
        JTable table = (JTable) c;

        int col = table.getSelectedColumn();
        int row = table.getSelectedRow();

        String reference = "=";
        reference += "" + (char) ('A' + (col - 1));
        reference += row + 1;

        return reference;
    }

    protected void importString(JComponent c, String str) {
    }

    protected void cleanup(JComponent c, boolean remove) {
    }
}
