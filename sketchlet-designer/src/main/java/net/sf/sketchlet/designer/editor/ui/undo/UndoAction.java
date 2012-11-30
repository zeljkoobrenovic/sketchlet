package net.sf.sketchlet.designer.editor.ui.undo;

/**
 * @author zobrenovic
 */
public abstract class UndoAction {

    public abstract void restore();

    public boolean shouldUndo() {
        return true;
    }
}
