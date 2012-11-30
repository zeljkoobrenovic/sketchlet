package net.sf.sketchlet.designer.editor.ui.properties;

import net.sf.sketchlet.common.dnd.StringTransferHandler;
import net.sf.sketchlet.framework.model.PropertiesInterface;

import javax.swing.*;

public class PropertiesTransferHandler extends StringTransferHandler {

    public String prefix = "=[";
    public String postfix = "]";
    public int column = 0;
    PropertiesInterface properties;

    public PropertiesTransferHandler(PropertiesInterface properties) {
        this.properties = properties;
    }

    protected String exportString(JComponent c) {
        JTable table = (JTable) c;
        int row = table.getSelectedRow();

        String strProperty = table.getValueAt(row, 0).toString();

        String transferString = properties.getTransferString(strProperty);

        if (transferString == null || transferString.isEmpty()) {
            return "";
        } else {
            return prefix + transferString + postfix;
        }
    }

    protected void importString(JComponent c, String str) {
    }

    protected void cleanup(JComponent c, boolean remove) {
    }
}
