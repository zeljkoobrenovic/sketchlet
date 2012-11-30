package net.sf.sketchlet.designer.eye.eye;

import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.page.PageDetailsPanel;
import net.sf.sketchlet.framework.model.Page;

import java.awt.*;

/**
 * @author zobrenovic
 */
public class VariablesEventsEyeSlot extends EyeSlot {

    Page page;

    public VariablesEventsEyeSlot(Page page, EyeData parent) {
        super(parent);
        this.page = page;
        this.name = "on variable updates";
        this.backgroundColor = Color.BLACK;
    }

    public String getLongName() {
        return "on variable updates in sketch '" + page.getTitle() + "'";
    }

    public void addRelatedSlot(EyeSlot relatedSlot) {
        /*if (relatedSlot instanceof VariableEyeSlot) {
            this.checkAndAdd(relatedSlot, stateEventHandler.actions, "Variable update", relatedSlot.name, 3, 4, "updated by", "updates");
            this.checkAndAdd(relatedSlot, stateEventHandler.actions, "Variable increment", relatedSlot.name, 3, 4, "incremented by", "increments");
            this.checkAndAdd(relatedSlot, stateEventHandler.actions, "Variable append", relatedSlot.name, 3, 4, "appended by", "appends");
        } else if (relatedSlot instanceof TimerEyeSlot) {
            this.checkAndAdd(relatedSlot, stateEventHandler.actions, "Start Timer", relatedSlot.name, 3, 4);
            this.checkAndAdd(relatedSlot, stateEventHandler.actions, "Pause Timer", relatedSlot.name, 3, 4);
            this.checkAndAdd(relatedSlot, stateEventHandler.actions, "Stop Timer", relatedSlot.name, 3, 4);
        } else if (relatedSlot instanceof MacroEyeSlot) {
            this.checkAndAdd(relatedSlot, stateEventHandler.actions, "Start Action", relatedSlot.name, 3, 4);
            this.checkAndAdd(relatedSlot, stateEventHandler.actions, "Stop Action", relatedSlot.name, 3, 4);
        } else if (relatedSlot instanceof ScriptEyeSlot) {
            this.checkAndAdd(relatedSlot, stateEventHandler.actions, "Start Action", "Script:" + relatedSlot.name, 3, 4);
            this.checkAndAdd(relatedSlot, stateEventHandler.actions, "Stop Action", "Script:" + relatedSlot.name, 3, 4);
        } else if (relatedSlot instanceof ScreenActionEyeSlot) {
            this.checkAndAdd(relatedSlot, stateEventHandler.actions, "Start Action", "Screen:" + relatedSlot.name, 3, 4);
            this.checkAndAdd(relatedSlot, stateEventHandler.actions, "Stop Action", "Screen:" + relatedSlot.name, 3, 4);
        }
                  */
    }

    public void openItem() {
        SketchletEditor.getInstance().showStatePanel(PageDetailsPanel.actionsTabIndex, PageDetailsPanel.actionsVariablesSubtabIndex);
    }
}
