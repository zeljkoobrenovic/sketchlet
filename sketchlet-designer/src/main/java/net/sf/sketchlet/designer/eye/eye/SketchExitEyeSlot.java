/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.eye.eye;

import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.page.PageDetailsPanel;
import net.sf.sketchlet.model.Page;
import net.sf.sketchlet.model.programming.macros.Macro;

import java.awt.*;

/**
 * @author zobrenovic
 */
public class SketchExitEyeSlot extends EyeSlot {

    Macro onExitMacro;
    Page page;

    public SketchExitEyeSlot(Page page, EyeData parent) {
        super(parent);
        this.page = page;
        this.onExitMacro = page.getOnExitMacro();
        this.name = "on exit";
        this.backgroundColor = Color.BLACK;
    }

    public String getLongName() {
        return "on exit of sketch '" + page.getTitle() + "'";
    }

    public void addRelatedSlot(EyeSlot relatedSlot) {
        if (relatedSlot instanceof TimerEyeSlot) {
            TimerEyeSlot s = (TimerEyeSlot) relatedSlot;
            this.checkAndAdd(relatedSlot, onExitMacro.getActions(), "Start timer", relatedSlot.name, 0, 1,
                    "started on exit of sketch '" + page.getTitle() + "'",
                    "starts timer '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, onExitMacro.getActions(), "Pause timer", relatedSlot.name, 0, 1,
                    "paused on exit of sketch '" + page.getTitle() + "'",
                    "pauses timer '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, onExitMacro.getActions(), "Stop timer", relatedSlot.name, 0, 1,
                    "stopped on exit of sketch '" + page.getTitle() + "'",
                    "stops timer '" + relatedSlot.name + "'");
        } else if (relatedSlot instanceof MacroEyeSlot) {
            this.checkAndAdd(relatedSlot, onExitMacro.getActions(), "Start action", relatedSlot.name, 0, 1,
                    "started on exit of sketch '" + page.getTitle() + "'",
                    "starts action '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, onExitMacro.getActions(), "Stop action", relatedSlot.name, 0, 1,
                    "stopped on exit of sketch '" + page.getTitle() + "'",
                    "stops action '" + relatedSlot.name + "'");
        } else if (relatedSlot instanceof ScriptEyeSlot) {
            this.checkAndAdd(relatedSlot, onExitMacro.getActions(), "Start action", "Script:" + relatedSlot.name, 0, 1,
                    "started on exit of sketch '" + page.getTitle() + "'",
                    "starts script '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, onExitMacro.getActions(), "Stop action", "Script:" + relatedSlot.name, 0, 1,
                    "stopped on exit of sketch '" + page.getTitle() + "'",
                    "stops script '" + relatedSlot.name + "'");
        } else if (relatedSlot instanceof ScreenActionEyeSlot) {
            this.checkAndAdd(relatedSlot, onExitMacro.getActions(), "Start action", "Screen:" + relatedSlot.name, 0, 1,
                    "started on exit of sketch '" + page.getTitle() + "'",
                    "starts screen action '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, onExitMacro.getActions(), "Stop action", "Screen:" + relatedSlot.name, 0, 1,
                    "stopped on exit of sketch '" + page.getTitle() + "'",
                    "stops screen action '" + relatedSlot.name + "'");
        }
    }

    public void openItem() {
        SketchletEditor.getInstance().showStatePanel(PageDetailsPanel.actionsTabIndex, PageDetailsPanel.actionsOnExitSubtabIndex);
    }
}
