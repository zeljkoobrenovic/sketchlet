/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.ui.undo;

/**
 * @author zobrenovic
 */
public abstract class UndoAction {

    public abstract void restore();

    public boolean shouldUndo() {
        return true;
    }
}
