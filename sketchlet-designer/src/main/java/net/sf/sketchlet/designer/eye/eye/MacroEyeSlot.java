package net.sf.sketchlet.designer.eye.eye;

import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.framework.model.programming.macros.Macro;
import net.sf.sketchlet.framework.model.programming.macros.Macros;

import java.awt.*;

/**
 * @author zobrenovic
 */
public class MacroEyeSlot extends EyeSlot {

    Macro macro;

    public MacroEyeSlot(Macro macro, EyeData parent) {
        super(parent);
        this.macro = macro;
        this.name = macro.getName();
        this.backgroundColor = Color.BLUE;
    }

    public String getLongName() {
        return "Macro " + this.name;
    }

    public void addRelatedSlot(EyeSlot relatedSlot) {
        if (relatedSlot instanceof TimerEyeSlot) {
            TimerEyeSlot s = (TimerEyeSlot) relatedSlot;
            this.checkAndAdd(relatedSlot, macro.getActions(), "Start timer", relatedSlot.name, 0, 1,
                    "started by macro '" + this.name + "'",
                    "starts timer '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, macro.getActions(), "Pause timer", relatedSlot.name, 0, 1,
                    "paused by macro '" + this.name + "'",
                    "pauses timer '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, macro.getActions(), "Stop timer", relatedSlot.name, 0, 1,
                    "stopped by macro '" + this.name + "'",
                    "stops timer '" + relatedSlot.name + "'");
        } else if (relatedSlot instanceof MacroEyeSlot) {
            this.checkAndAdd(relatedSlot, macro.getActions(), "Start action", relatedSlot.name, 0, 1,
                    "stopped by macro '" + this.name + "'",
                    "stops macro '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, macro.getActions(), "Stop action", relatedSlot.name, 0, 1,
                    "stopped by macro '" + this.name + "'",
                    "stops macro '" + relatedSlot.name + "'");
        } else if (relatedSlot instanceof ScriptEyeSlot) {
            this.checkAndAdd(relatedSlot, macro.getActions(), "Start action", "Script:" + relatedSlot.name, 0, 1,
                    "stopped by macro '" + this.name + "'",
                    "stops script '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, macro.getActions(), "Stop action", "Script:" + relatedSlot.name, 0, 1,
                    "stopped by macro '" + this.name + "'",
                    "stops script '" + relatedSlot.name + "'");
        } else if (relatedSlot instanceof ScreenActionEyeSlot) {
            this.checkAndAdd(relatedSlot, macro.getActions(), "Start action", "Screen:" + relatedSlot.name, 0, 1,
                    "stopped by macro '" + this.name + "'",
                    "stops screen action '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, macro.getActions(), "Stop action", "Screen:" + relatedSlot.name, 0, 1,
                    "stopped by macro '" + this.name + "'",
                    "stops screen action '" + relatedSlot.name + "'");
        }
    }

    public void openItem() {
        int row = Macros.globalMacros.macros.indexOf(macro);
        SketchletEditor.getInstance().getExtraEditorPanel().macrosExtraPanel.showMacros(row);
    }
}
