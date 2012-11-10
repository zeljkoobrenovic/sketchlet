/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.eye.eye;

import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.script.ScriptPluginProxy;

import java.awt.*;

/**
 * @author zobrenovic
 */
public class ScriptEyeSlot extends EyeSlot {

    ScriptPluginProxy script;

    public ScriptEyeSlot(ScriptPluginProxy script, EyeData parent) {
        super(parent);
        this.script = script;
        this.name = script.scriptFile.getName();
        this.backgroundColor = Color.CYAN;
    }

    public String getLongName() {
        return "Script " + this.name;
    }

    public void openItem() {
        SketchletEditor.editorPanel.extraEditorPanel.scriptEditorExtraPanel.openScript(script.scriptFile);
    }
}
