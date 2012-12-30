package net.sf.sketchlet.designer.editor.ui.localvars;

import net.sf.sketchlet.common.dnd.StringTransferHandler;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;

public class LocalVariableTransferHandler extends StringTransferHandler {
    public LocalVariableTransferHandler() {
    }

    protected String exportString(JComponent c) {
        JTable table = (JTable) c;

        int col = 0;
        int row = table.getSelectedRow();

        String reference = "=" + table.getValueAt(row, col);

        return reference;
    }

    protected void importString(JComponent c, String str) {
    }

    protected void cleanup(JComponent c, boolean remove) {
        JTable table = (JTable) c;
        AbstractTableModel model = (AbstractTableModel) table.getModel();
        model.fireTableDataChanged();
    }
}
