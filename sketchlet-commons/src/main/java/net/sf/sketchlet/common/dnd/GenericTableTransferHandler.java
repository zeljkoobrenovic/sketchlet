/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.common.dnd;

import javax.swing.*;

public class GenericTableTransferHandler extends StringTransferHandler {

    private int[] rows = null;
    private String prefix = "";
    private int column = 0;

    public GenericTableTransferHandler(String prefix, int column) {
        this.setPrefix(prefix);
        this.setColumn(column);
    }

    protected String exportString(JComponent c) {
        JTable table = (JTable) c;
        rows = table.getSelectedRows();

        StringBuffer buff = new StringBuffer();

        for (int i = 0; i < rows.length; i++) {
            Object val;
            val = getPrefix() + table.getValueAt(rows[i], getColumn());
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

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }
}
