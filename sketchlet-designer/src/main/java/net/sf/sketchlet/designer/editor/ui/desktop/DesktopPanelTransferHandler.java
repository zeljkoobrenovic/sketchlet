package net.sf.sketchlet.designer.editor.ui.desktop;

import net.sf.sketchlet.common.dnd.StringTransferHandler;
import javax.swing.*;

public class DesktopPanelTransferHandler extends StringTransferHandler {

    private int[] rows = null;
    public String prefix = "@sketch ";
    DesktopPanel desktopPanel;

    public DesktopPanelTransferHandler(DesktopPanel desktopPanel) {
        this.desktopPanel = desktopPanel;
    }

    protected String exportString(JComponent c) {
        String result = "";

        if (this.desktopPanel.selectedPage != null) {
            result = this.desktopPanel.selectedPage.getTitle();
        }

        return prefix + result;
    }

    protected void importString(JComponent c, String str) {
    }

    protected void cleanup(JComponent c, boolean remove) {
    }
}
