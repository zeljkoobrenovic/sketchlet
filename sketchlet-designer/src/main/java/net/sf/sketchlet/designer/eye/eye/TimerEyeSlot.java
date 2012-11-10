/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.eye.eye;

import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.programming.timers.Timer;
import net.sf.sketchlet.designer.programming.timers.Timers;

import java.awt.*;

/**
 * @author zobrenovic
 */
public class TimerEyeSlot extends EyeSlot {

    Timer timer;

    public TimerEyeSlot(Timer timer, EyeData parent) {
        super(parent);
        this.timer = timer;
        this.name = timer.name;
        this.backgroundColor = Color.MAGENTA;
    }


    public String getLongName() {
        return "Timer " + this.name;
    }

    public void openItem() {
        int row = Timers.globalTimers.timers.indexOf(timer);
        SketchletEditor.editorPanel.extraEditorPanel.timersExtraPanel.showTimers(row);
    }
}
