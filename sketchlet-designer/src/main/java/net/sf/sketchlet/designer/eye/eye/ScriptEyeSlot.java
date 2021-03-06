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
        this.name = script.getScriptFile().getName();
        this.backgroundColor = Color.CYAN;
    }

    public String getLongName() {
        return "Script " + this.name;
    }

    public void openItem() {
        SketchletEditor.getInstance().getExtraEditorPanel().scriptEditorExtraPanel.openScript(script.getScriptFile());
    }
}
