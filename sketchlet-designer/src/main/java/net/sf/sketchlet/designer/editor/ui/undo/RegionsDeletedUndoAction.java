package net.sf.sketchlet.designer.editor.ui.undo;

import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionsFrame;
import net.sf.sketchlet.framework.model.ActiveRegion;

import java.util.Vector;

/**
 * @author zobrenovic
 */
public class RegionsDeletedUndoAction extends UndoAction {

    Vector<ActiveRegion> regions = new Vector<ActiveRegion>();

    public RegionsDeletedUndoAction(Vector<ActiveRegion> actions) {
        if (actions != null) {
            for (ActiveRegion a : actions) {
                regions.add(a);
            }
        }
    }

    public void restore() {
        for (ActiveRegion reg : regions) {
            if (SketchletEditor.getInstance().getCurrentPage() != reg.getSketch()) {
                SketchletEditor.getInstance().openSketchAndWait(reg.getSketch());
            }
            SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().insertElementAt(reg, 0);
            SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().setSelectedRegions(new Vector<ActiveRegion>());
            SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().addToSelection(reg);
        }
        ActiveRegionsFrame.reload();
    }
}
