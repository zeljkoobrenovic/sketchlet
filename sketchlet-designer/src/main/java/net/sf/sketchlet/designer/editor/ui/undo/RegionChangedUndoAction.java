package net.sf.sketchlet.designer.editor.ui.undo;

import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.framework.model.ActiveRegion;

/**
 * @author zobrenovic
 */
public class RegionChangedUndoAction extends UndoAction {

    private ActiveRegion region = null;
    private ActiveRegion restoreRegion = null;

    public RegionChangedUndoAction(ActiveRegion region) {
        this.setRegion(region);
        if (region != null) {
            this.setRestoreRegion(new ActiveRegion(region, false));
            this.getRestoreRegion().setImage(null);
            this.getRestoreRegion().getAdditionalDrawnImages().clear();
            this.getRestoreRegion().setRenderer(null);
        }
    }

    public void restore() {
        if (getRegion() != null) {
            if (SketchletEditor.getInstance().getCurrentPage() != this.getRegion().getSketch()) {
                SketchletEditor.getInstance().openSketchAndWait(this.getRegion().getSketch());
            }
            this.getRegion().setPropertiesFromRegion(this.getRestoreRegion());
        }
    }

    public boolean shouldUndo() {
        return getRegion().getSketch().getRegions().getRegions().contains(getRegion());
    }

    public ActiveRegion getRegion() {
        return region;
    }

    public void setRegion(ActiveRegion region) {
        this.region = region;
    }

    public ActiveRegion getRestoreRegion() {
        return restoreRegion;
    }

    public void setRestoreRegion(ActiveRegion restoreRegion) {
        this.restoreRegion = restoreRegion;
    }
}
