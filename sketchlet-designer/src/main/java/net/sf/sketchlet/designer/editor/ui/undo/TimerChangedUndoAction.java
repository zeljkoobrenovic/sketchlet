package net.sf.sketchlet.designer.editor.ui.undo;

import net.sf.sketchlet.framework.model.programming.timers.Timer;

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
            if (timer.getPanel() != null) {
                timer.getPanel().refreshComponents();
            }
        }
    }

    public boolean shouldUndo() {
        return true;
    }
}
