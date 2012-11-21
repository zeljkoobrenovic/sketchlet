/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.context;

import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.script.ScriptsTableModel;
import net.sf.sketchlet.util.UtilContext;

import javax.swing.*;

/**
 * @author zobrenovic
 */
public class UtilContextImpl extends UtilContext {

    @Override
    public void refreshScriptTable() {
        ScriptsTableModel.refresh();
    }

    @Override
    public ImageIcon getImageIconFromResources(String path) {
        return Workspace.createImageIcon(path);
    }

    @Override
    public void skipUndo(boolean bSkip) {
        SketchletEditor.getInstance().skipUndo = bSkip;
    }
}
