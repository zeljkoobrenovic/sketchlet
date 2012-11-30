package net.sf.sketchlet.designer.editor.ui.undo;

import net.sf.sketchlet.framework.model.programming.macros.Macro;

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
