/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.ui.dnd;

import net.sf.sketchlet.common.dnd.StringTransferHandler;
import net.sf.sketchlet.designer.ui.variables.CopyExpression;

import javax.swing.*;

public class TableTransferHandler extends StringTransferHandler {

    private int[] rows = null;

    protected String exportString(JComponent c) {
        JTable table = (JTable) c;
        rows = table.getSelectedRows();
        int colCount = table.getColumnCount();

        StringBuffer buff = new StringBuffer();

        for (int i = 0; i < rows.length; i++) {
            Object val;
            if (CopyExpression.currentExpression == null) {
                val = "=" + table.getValueAt(rows[i], 0);
                buff.append(val == null ? "" : val.toString());
                if (i != rows.length - 1) {
                    buff.append("\n");
                }
            } else {
                val = CopyExpression.currentExpression.copyExpressions();
                buff.append(val == null ? "" : val.toString());
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
