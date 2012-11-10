/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.eye.eye;

import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.ui.page.SketchStatePanel;

import java.awt.*;

/**
 * @author zobrenovic
 */
public class KeyboardActionsEyeSlot extends EyeSlot {

    Page page;

    public KeyboardActionsEyeSlot(Page page, EyeData parent) {
        super(parent);
        this.page = page;
        this.name = "on keyboard events";
        this.backgroundColor = Color.BLACK;
    }

    public String getLongName() {
        return "on keyboard events in sketch '" + page.title + "'";
    }

    public void addRelatedSlot(EyeSlot relatedSlot) {
    }

    public void openItem() {
        SketchletEditor.editorPanel.showStatePanel(SketchStatePanel.actionsTabIndex, SketchStatePanel.actionsKeyboardSubtabIndex);
    }
}
