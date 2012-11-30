package net.sf.sketchlet.designer.editor.ui.undo;

import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionPanel;
import net.sf.sketchlet.framework.model.ActiveRegion;

import java.awt.*;

/**
 * @author zobrenovic
 */
public class RegionChangedUndoActionWithComponent extends RegionChangedUndoAction {

    public RegionChangedUndoActionWithComponent(ActiveRegion action, Component component, Object value) {
        super(action);
    }

    @Override
    public void restore() {
        if (region != null) {
            super.restore();
            if (ActiveRegionPanel.getCurrentActiveRegionPanel() != null) {
                ActiveRegionPanel.getCurrentActiveRegionPanel().refreshComponents();
            }
        }
    }
}
