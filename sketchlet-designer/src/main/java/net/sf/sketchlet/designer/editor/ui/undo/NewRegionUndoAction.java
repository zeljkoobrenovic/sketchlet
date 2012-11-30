package net.sf.sketchlet.designer.editor.ui.undo;

import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.framework.model.ActiveRegion;

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
        if (SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null) {
            SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().removeAllElements();
        }
        SketchletEditor.getInstance().deleteRegion(region);
    }

    public boolean shouldUndo() {
        return region.getSketch().getRegions().getRegions().contains(region);
    }
}
