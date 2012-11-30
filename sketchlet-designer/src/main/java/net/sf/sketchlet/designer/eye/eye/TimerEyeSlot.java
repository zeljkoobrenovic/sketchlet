package net.sf.sketchlet.designer.eye.eye;

import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.framework.model.programming.timers.Timer;
import net.sf.sketchlet.framework.model.programming.timers.Timers;

import java.awt.*;

/**
 * @author zobrenovic
 */
public class TimerEyeSlot extends EyeSlot {

    Timer timer;

    public TimerEyeSlot(Timer timer, EyeData parent) {
        super(parent);
        this.timer = timer;
        this.name = timer.getName();
        this.backgroundColor = Color.MAGENTA;
    }


    public String getLongName() {
        return "Timer " + this.name;
    }

    public void openItem() {
        int row = Timers.getGlobalTimers().getTimers().indexOf(timer);
        SketchletEditor.getInstance().getExtraEditorPanel().timersExtraPanel.showTimers(row);
    }
}
