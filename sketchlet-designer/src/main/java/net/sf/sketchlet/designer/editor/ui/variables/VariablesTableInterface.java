package net.sf.sketchlet.designer.editor.ui.variables;

import java.awt.*;

/**
 * @author zobrenovic
 */
public interface VariablesTableInterface {
    public void variableTableUpdate(String var, String value, Component c);

    public void variableDialogAdded(String var, String value, Component c);
}
