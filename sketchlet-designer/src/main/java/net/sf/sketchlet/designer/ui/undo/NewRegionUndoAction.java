/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui.undo;

import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.editor.SketchletEditor;

/**
 * @author zobrenovic
 */
public class NewRegionUndoAction extends UndoAction {

    ActiveRegion region;

    public NewRegionUndoAction(ActiveRegion action) {
        this.region = action;
    }

    public void restore() {
        if (SketchletEditor.editorPanel.currentPage != this.region.getSketch()) {
            SketchletEditor.editorPanel.openSketchAndWait(this.region.getSketch());
        }
        if (SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null) {
            SketchletEditor.editorPanel.currentPage.regions.selectedRegions.removeAllElements();
        }
        SketchletEditor.editorPanel.deleteRegion(region);
    }

    public boolean shouldUndo() {
        return region.getSketch().regions.regions.contains(region);
    }
}
