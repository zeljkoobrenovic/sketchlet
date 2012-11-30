package net.sf.sketchlet.designer.eye.eye;

import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.page.PageDetailsPanel;
import net.sf.sketchlet.framework.model.Page;

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
        return "on keyboard events in sketch '" + page.getTitle() + "'";
    }

    public void addRelatedSlot(EyeSlot relatedSlot) {
    }

    public void openItem() {
        SketchletEditor.getInstance().showStatePanel(PageDetailsPanel.actionsTabIndex, PageDetailsPanel.actionsKeyboardSubtabIndex);
    }
}
