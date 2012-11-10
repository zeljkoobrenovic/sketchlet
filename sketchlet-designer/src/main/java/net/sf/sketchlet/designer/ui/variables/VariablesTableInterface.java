/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.sketchlet.designer.ui.variables;

import java.awt.*;

/**
 * @author zobrenovic
 */
public interface VariablesTableInterface {
    public void variableTableUpdate(String var, String value, Component c);

    public void variableDialogAdded(String var, String value, Component c);
}
