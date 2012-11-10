/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.common.dnd;

import net.sf.sketchlet.common.dnd.StringTransferHandler;
import javax.swing.*;
import javax.swing.table.*;

public class GenericTableTransferHandler extends StringTransferHandler {

    private int[] rows = null;
    public String prefix = "";
    public int column = 0;

    public GenericTableTransferHandler(String prefix, int column) {
        this.prefix = prefix;
        this.column = column;
    }

    protected String exportString(JComponent c) {
        JTable table = (JTable) c;
        rows = table.getSelectedRows();

        StringBuffer buff = new StringBuffer();

        for (int i = 0; i < rows.length; i++) {
            Object val;
            val = prefix + table.getValueAt(rows[i], column);
            buff.append(val == null ? "" : val.toString());
            if (i != rows.length - 1) {
                buff.append("\n");
            }
        }

        String result = buff.toString();
        try {
            result = new String(result.getBytes("UTF-8"), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    protected void importString(JComponent c, String str) {
    }

    protected void cleanup(JComponent c, boolean remove) {
        rows = null;
    }
}
