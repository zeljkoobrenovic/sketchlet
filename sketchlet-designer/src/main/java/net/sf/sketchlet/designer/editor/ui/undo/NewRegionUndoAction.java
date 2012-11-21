/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.ui.undo;

import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.model.ActiveRegion;

/**
 * @author zobrenovic
 */
public class NewRegionUndoAction extends UndoAction {

    ActiveRegion region;

    public NewRegionUndoAction(ActiveRegion action) {
        this.region = action;
    }

    public void restore() {
        if (SketchletEditor.getInstance().getCurrentPage() != this.region.getSketch()) {
            SketchletEditor.getInstance().openSketchAndWait(this.region.getSketch());
        }
        if (SketchletEditor.getInstance().getCurrentPage().getRegions().getSelectedRegions() != null) {
            SketchletEditor.getInstance().getCurrentPage().getRegions().getSelectedRegions().removeAllElements();
        }
        SketchletEditor.getInstance().deleteRegion(region);
    }

    public boolean shouldUndo() {
        return region.getSketch().getRegions().getRegions().contains(region);
    }
}
