/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui.undo;

import net.sf.sketchlet.designer.programming.timers.Timer;

/**
 * @author zobrenovic
 */
public class TimerChangedUndoAction extends UndoAction {

    Timer timer = null;
    Timer restoreTimer = null;

    public TimerChangedUndoAction(Timer timer) {
        this.timer = timer;
        if (timer != null) {
            this.restoreTimer = timer.getUndoCopy();
        }
    }

    public void restore() {
        if (timer != null && this.restoreTimer != null) {
            timer.restore(this.restoreTimer);
            if (timer.panel != null) {
                timer.panel.refreshComponents();
            }
        }
    }

    public boolean shouldUndo() {
        return true;
    }
}
