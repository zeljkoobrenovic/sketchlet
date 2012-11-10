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
public class RegionChangedUndoAction extends UndoAction {

    ActiveRegion region = null;
    ActiveRegion restoreRegion = null;

    public RegionChangedUndoAction(ActiveRegion region) {
        this.region = region;
        if (region != null) {
            this.restoreRegion = new ActiveRegion(region, false);
            this.restoreRegion.image = null;
            this.restoreRegion.additionalDrawImages.removeAllElements();
            this.restoreRegion.renderer = null;
        }
    }

    public void restore() {
        if (region != null) {
            if (SketchletEditor.editorPanel.currentPage != this.region.getSketch()) {
                SketchletEditor.editorPanel.openSketchAndWait(this.region.getSketch());
            }
            this.region.setPropertiesFromRegion(this.restoreRegion);
        }
    }

    public boolean shouldUndo() {
        return region.getSketch().regions.regions.contains(region);
    }
}
