/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.eye.eye;

import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.designer.data.Pages;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import org.apache.log4j.Logger;

import java.awt.*;

/**
 * @author zobrenovic
 */
public class SketchEyeSlot extends EyeSlot {
    private static final Logger log = Logger.getLogger(SketchEyeSlot.class);
    Page page;

    public SketchEyeSlot(Page page, EyeData parent) {
        super(parent);
        this.page = page;
        this.name = page.title;
        this.backgroundColor = Color.GREEN;
    }

    public String getLongName() {
        return "Page " + this.name;
    }

    public void addRelatedSlot(EyeSlot relatedSlot) {
        String sname = "page '" + page.title + "'";
        if (relatedSlot instanceof VariableEyeSlot) {
            String vname = "variable '" + ((VariableEyeSlot) relatedSlot).name + "'";
            String value;

            value = this.getValue(page.onEntryMacro.actions, "Variable update", relatedSlot.name, 0, 1);
            checkAndAdd(relatedSlot, page.onEntryMacro.actions, "Variable update", relatedSlot.name, 0, 1,
                    "updated on entry of " + sname + " to '" + value + "'",
                    "on entry updates " + vname + " to '" + value + "'");

            /*value = getValue(sketch.variablesMappingHandler.data, relatedSlot.name, 3, 0);
            checkAndAdd(relatedSlot, sketch.variablesMappingHandler.data, relatedSlot.name, 3,
                    "connected to property '" + value + "' of " + sname,
                    "property '" + value + "' connected to variable '" + vname + "'");*/

            value = this.getValue(page.onEntryMacro.actions, "Variable append", relatedSlot.name, 0, 1);
            checkAndAdd(relatedSlot, page.onEntryMacro.actions, "Variable append", relatedSlot.name, 0, 1,
                    "appended on entry of " + sname + " with '" + value + "'",
                    "on entry appends " + vname + " with '" + value + "'");

            value = this.getValue(page.onEntryMacro.actions, "Variable increment", relatedSlot.name, 0, 1);
            checkAndAdd(relatedSlot, page.onEntryMacro.actions, "Variable increment", relatedSlot.name, 0, 1,
                    "incremented on entry of " + sname + " by " + value + "",
                    "on entry increments " + vname + " by " + value + "");

            value = this.getValue(page.onExitMacro.actions, "Variable update", relatedSlot.name, 0, 1);
            checkAndAdd(relatedSlot, page.onExitMacro.actions, "Variable update", relatedSlot.name, 0, 1,
                    "updated on exit of " + sname + " to '" + value + "'",
                    "on exit updates " + vname + " to '" + value + "'");

            value = this.getValue(page.onExitMacro.actions, "Variable append", relatedSlot.name, 0, 1);
            checkAndAdd(relatedSlot, page.onExitMacro.actions, "Variable append", relatedSlot.name, 0, 1,
                    "appended on exit of " + sname + " with '" + value + "'",
                    "on exit appends " + vname + " with '" + value + "'");

            value = this.getValue(page.onExitMacro.actions, "Variable increment", relatedSlot.name, 0, 1);
            checkAndAdd(relatedSlot, page.onExitMacro.actions, "Variable increment", relatedSlot.name, 0, 1,
                    "incremented on exit of " + sname + " by " + value + "",
                    "on exit increments " + vname + " by " + value + "");


            /*if (this.sketch.isConnectedTo("Variable update", relatedSlot.name)) {
            addRelationToSlot(relatedSlot, "update", "update");
            }
            if (this.sketch.isConnectedTo("Variable increment", relatedSlot.name)) {
            addRelationToSlot(relatedSlot, "increment", "increment");
            }
            if (this.sketch.isConnectedTo("Variable appent", relatedSlot.name)) {
            addRelationToSlot(relatedSlot, "append", "append");
            }*/
        } else if (relatedSlot instanceof TimerEyeSlot) {
//            checkAndAdd(relatedSlot, sketch.onEntryMacro.actions, "Start Timer", relatedSlot.name, 0, 1);
            //checkAndAdd(relatedSlot, sketch.onEntryMacro.actions, "Stop Timer", relatedSlot.name, 0, 1);
//            checkAndAdd(relatedSlot, sketch.onExitMacro.actions, "Start Timer", relatedSlot.name, 0, 1);
//            checkAndAdd(relatedSlot, sketch.onExitMacro.actions, "Stop Timer", relatedSlot.name, 0, 1);
            checkAndAdd(relatedSlot, page.onEntryMacro.actions, "Start Timer", relatedSlot.name, 0, 1,
                    "started on entry of sketch '" + page.title + "'",
                    "starts timer '" + relatedSlot.name + "'");
            checkAndAdd(relatedSlot, page.onEntryMacro.actions, "Pause Timer", relatedSlot.name, 0, 1,
                    "paused on entry of sketch '" + page.title + "'",
                    "pauses timer '" + relatedSlot.name + "'");
            checkAndAdd(relatedSlot, page.onEntryMacro.actions, "Stop Timer", relatedSlot.name, 0, 1,
                    "stopped on entry of sketch '" + page.title + "'",
                    "stops timer '" + relatedSlot.name + "'");
            checkAndAdd(relatedSlot, page.onExitMacro.actions, "Start Timer", relatedSlot.name, 0, 1,
                    "started on exit of sketch '" + page.title + "'",
                    "starts timer '" + relatedSlot.name + "'");
            checkAndAdd(relatedSlot, page.onExitMacro.actions, "Pause Timer", relatedSlot.name, 0, 1,
                    "paused on exit of sketch '" + page.title + "'",
                    "pauses timer '" + relatedSlot.name + "'");
            checkAndAdd(relatedSlot, page.onExitMacro.actions, "Stop Timer", relatedSlot.name, 0, 1,
                    "stopped on exit of sketch '" + page.title + "'",
                    "stops timer '" + relatedSlot.name + "'");
            //checkAndAdd(relatedSlot, sketch.keyboardHandler.actions, "Start Timer", relatedSlot.name, 5, 6);
            //checkAndAdd(relatedSlot, sketch.keyboardHandler.actions, "Stop Timer", relatedSlot.name, 5, 6);

            //checkAndAdd(relatedSlot, sketch.eventHandler.actions, "Start Timer", relatedSlot.name, 3, 4);
            //checkAndAdd(relatedSlot, sketch.eventHandler.actions, "Stop Timer", relatedSlot.name, 3, 4);
            /*if (this.sketch.isConnectedTo("Start timer", relatedSlot.name)) {
            addRelationToSlot(relatedSlot, "start timer", "start timer");
            }
            if (this.sketch.isConnectedTo("Stop timer", relatedSlot.name)) {
            addRelationToSlot(relatedSlot, "stop timer", "stop timer");
            }*/
        } else if (relatedSlot instanceof MacroEyeSlot) {
            /*checkAndAdd(relatedSlot, sketch.onEntryMacro.actions, "Start macro", relatedSlot.name, 0, 1);
            checkAndAdd(relatedSlot, sketch.onEntryMacro.actions, "Stop macro", relatedSlot.name, 0, 1);
            checkAndAdd(relatedSlot, sketch.onExitMacro.actions, "Start macro", relatedSlot.name, 0, 1);
            checkAndAdd(relatedSlot, sketch.onExitMacro.actions, "Stop macro", relatedSlot.name, 0, 1);
            checkAndAdd(relatedSlot, sketch.keyboardHandler.actions, "Start macro", relatedSlot.name, 5, 6);
            checkAndAdd(relatedSlot, sketch.keyboardHandler.actions, "Stop macro", relatedSlot.name, 5, 6);
            checkAndAdd(relatedSlot, sketch.eventHandler.actions, "Start macro", relatedSlot.name, 3, 4);
            checkAndAdd(relatedSlot, sketch.eventHandler.actions, "Stop macro", relatedSlot.name, 3, 4);
            if (this.sketch.isConnectedTo("Start macro", relatedSlot.name)) {
            addRelationToSlot(relatedSlot, "start macro", "start macro");
            }
            if (this.sketch.isConnectedTo("Stop macro", relatedSlot.name)) {
            addRelationToSlot(relatedSlot, "stop macro", "stop macro");
            }*/
        } else if (relatedSlot instanceof ScriptEyeSlot) {
            /*checkAndAdd(relatedSlot, sketch.onEntryMacro.actions, "Start macro", "Script:" + relatedSlot.name, 0, 1);
            checkAndAdd(relatedSlot, sketch.onEntryMacro.actions, "Stop macro", "Script:" + relatedSlot.name, 0, 1);
            checkAndAdd(relatedSlot, sketch.onExitMacro.actions, "Start macro", "Script:" + relatedSlot.name, 0, 1);
            checkAndAdd(relatedSlot, sketch.onExitMacro.actions, "Stop macro", "Script:" + relatedSlot.name, 0, 1);
            checkAndAdd(relatedSlot, sketch.keyboardHandler.actions, "Start macro", "Script:" + relatedSlot.name, 5, 6);
            checkAndAdd(relatedSlot, sketch.keyboardHandler.actions, "Stop macro", "Script:" + relatedSlot.name, 5, 6);
            checkAndAdd(relatedSlot, sketch.eventHandler.actions, "Start macro", "Script:" + relatedSlot.name, 3, 4);
            checkAndAdd(relatedSlot, sketch.eventHandler.actions, "Stop macro", "Script:" + relatedSlot.name, 3, 4);
            if (this.sketch.isConnectedTo("Start macro", "Script:" + relatedSlot.name)) {
            addRelationToSlot(relatedSlot, "start macro", "start macro");
            }
            if (this.sketch.isConnectedTo("Stop macro", "Script:" + relatedSlot.name)) {
            addRelationToSlot(relatedSlot, "stop macro", "stop macro");
            }*/
        } else if (relatedSlot instanceof ScreenActionEyeSlot) {
            checkAndAdd(relatedSlot, page.onEntryMacro.actions, "Start action", "Screen:" + relatedSlot.name, 0, 1);
            checkAndAdd(relatedSlot, page.onEntryMacro.actions, "Stop action", "Screen:" + relatedSlot.name, 0, 1);
            checkAndAdd(relatedSlot, page.onExitMacro.actions, "Start action", "Screen:" + relatedSlot.name, 0, 1);
            checkAndAdd(relatedSlot, page.onExitMacro.actions, "Stop action", "Screen:" + relatedSlot.name, 0, 1);
        } else if (relatedSlot instanceof SketchEyeSlot) {
            checkAndAdd(relatedSlot, page.onEntryMacro.actions, "Go to page", relatedSlot.name, 0, 1);
            checkAndAdd(relatedSlot, page.onExitMacro.actions, "Go to page", relatedSlot.name, 0, 1);

            /*if (this.sketch.isConnectedTo(((SketchEyeSlot) relatedSlot).sketch)) {
            addRelationToSlot(relatedSlot, "go to", "go to");
            }*/
        }
    }

    public void openItem() {
        SketchletEditor.editorPanel.selectSketch(page);

        try {
            while (Pages.msgFrame != null) {
                Thread.sleep(10);
            }
        } catch (Exception e) {
            log.error(e);
        }
    }
}
