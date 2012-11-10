/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.eye.eye;

import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.programming.macros.Macro;
import net.sf.sketchlet.designer.ui.page.SketchStatePanel;

import java.awt.*;

/**
 * @author zobrenovic
 */
public class SketchEntryEyeSlot extends EyeSlot {

    Macro onEntryMacro;
    Page page;

    public SketchEntryEyeSlot(Page page, EyeData parent) {
        super(parent);
        this.page = page;
        this.onEntryMacro = page.onEntryMacro;
        this.name = "on entry";
        this.backgroundColor = Color.BLACK;
    }

    public String getLongName() {
        return "on entry of sketch '" + page.title + "'";
    }

    public void addRelatedSlot(EyeSlot relatedSlot) {
        if (relatedSlot instanceof TimerEyeSlot) {
            TimerEyeSlot s = (TimerEyeSlot) relatedSlot;
            this.checkAndAdd(relatedSlot, onEntryMacro.actions, "Start timer", relatedSlot.name, 0, 1,
                    "started on entry of sketch '" + page.title + "'",
                    "starts timer '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, onEntryMacro.actions, "Pause timer", relatedSlot.name, 0, 1,
                    "paused on entry of sketch '" + page.title + "'",
                    "pauses timer '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, onEntryMacro.actions, "Stop timer", relatedSlot.name, 0, 1,
                    "stopped on entry of sketch '" + page.title + "'",
                    "stops timer '" + relatedSlot.name + "'");
        } else if (relatedSlot instanceof MacroEyeSlot) {
            this.checkAndAdd(relatedSlot, onEntryMacro.actions, "Start action", relatedSlot.name, 0, 1,
                    "started on entry of sketch '" + page.title + "'",
                    "starts action '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, onEntryMacro.actions, "Stop action", relatedSlot.name, 0, 1,
                    "stopped on entry of sketch '" + page.title + "'",
                    "stops action '" + relatedSlot.name + "'");
        } else if (relatedSlot instanceof ScriptEyeSlot) {
            this.checkAndAdd(relatedSlot, onEntryMacro.actions, "Start action", "Script:" + relatedSlot.name, 0, 1,
                    "started on entry of sketch '" + page.title + "'",
                    "starts script '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, onEntryMacro.actions, "Stop action", "Script:" + relatedSlot.name, 0, 1,
                    "stopped on entry of sketch '" + page.title + "'",
                    "stops script '" + relatedSlot.name + "'");
        } else if (relatedSlot instanceof ScreenActionEyeSlot) {
            this.checkAndAdd(relatedSlot, onEntryMacro.actions, "Start action", "Screen:" + relatedSlot.name, 0, 1,
                    "started on entry of sketch '" + page.title + "'",
                    "starts screen action '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, onEntryMacro.actions, "Stop action", "Screen:" + relatedSlot.name, 0, 1,
                    "stopped on entry of sketch '" + page.title + "'",
                    "stops screen action '" + relatedSlot.name + "'");
        }
    }

    public void openItem() {
        SketchletEditor.editorPanel.showStatePanel(SketchStatePanel.actionsTabIndex, SketchStatePanel.actionsOnEntrySubtabIndex);
    }
}
