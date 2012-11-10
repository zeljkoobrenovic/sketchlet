/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.util;

import javax.swing.ImageIcon;

/**
 *
 * @author zobrenovic
 */
public abstract class UtilContext {

    private static UtilContext context;

    public static void setInstance(UtilContext context) {
        UtilContext.context = context;
    }

    public static UtilContext getInstance() {
        return UtilContext.context;
    }

    public abstract void skipUndo(boolean bSkip);

    public abstract ImageIcon getImageIconFromResources(String resource);
    
    public abstract void refreshScriptTable();
}
