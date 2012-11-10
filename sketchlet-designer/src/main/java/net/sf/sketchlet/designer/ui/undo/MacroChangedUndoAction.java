/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui.undo;

import net.sf.sketchlet.designer.programming.macros.Macro;

/**
 * @author zobrenovic
 */
public class MacroChangedUndoAction extends UndoAction {

    Macro macro = null;
    Macro restoreMacro = null;

    public MacroChangedUndoAction(Macro macro) {
        this.macro = macro;
        if (macro != null) {
            this.restoreMacro = macro.getUndoCopy();
        }
    }

    public void restore() {
        if (macro != null && this.restoreMacro != null) {
            macro.restore(this.restoreMacro);
            if (macro.panel != null) {
                macro.panel.refreshComponents();
            }
        }
    }

    public boolean shouldUndo() {
        return true;
    }
}
