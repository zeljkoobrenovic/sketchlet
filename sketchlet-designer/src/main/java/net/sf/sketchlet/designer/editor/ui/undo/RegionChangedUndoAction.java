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
public class RegionChangedUndoAction extends UndoAction {

    ActiveRegion region = null;
    ActiveRegion restoreRegion = null;

    public RegionChangedUndoAction(ActiveRegion region) {
        this.region = region;
        if (region != null) {
            this.restoreRegion = new ActiveRegion(region, false);
            this.restoreRegion.image = null;
            this.restoreRegion.additionalDrawImages.removeAllElements();
            this.restoreRegion.setRenderer(null);
        }
    }

    public void restore() {
        if (region != null) {
            if (SketchletEditor.getInstance().getCurrentPage() != this.region.getSketch()) {
                SketchletEditor.getInstance().openSketchAndWait(this.region.getSketch());
            }
            this.region.setPropertiesFromRegion(this.restoreRegion);
        }
    }

    public boolean shouldUndo() {
        return region.getSketch().getRegions().getRegions().contains(region);
    }
}
