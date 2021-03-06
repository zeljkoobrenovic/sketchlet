package net.sf.sketchlet.designer.editor.ui.undo;

import java.util.Vector;

/**
 * @author zobrenovic
 */
public class ComplexUndoAction extends UndoAction {

    Vector<UndoAction> actions = new Vector<UndoAction>();

    public ComplexUndoAction() {
    }

    public void add(UndoAction a) {
        actions.add(a);
    }

    @Override
    public void restore() {
        for (UndoAction a : actions) {
            a.restore();
        }
    }

    @Override
    public boolean shouldUndo() {
        return actions.size() > 0;
    }
}