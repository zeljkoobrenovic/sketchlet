package net.sf.sketchlet.designer.editor.ui.undo;

import net.sf.sketchlet.framework.model.ActiveRegion;

import java.awt.*;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class RegionsChangedUndoActionWithComponent extends UndoAction {

    Vector<RegionChangedUndoAction> regions = new Vector<RegionChangedUndoAction>();

    public RegionsChangedUndoActionWithComponent(Vector<ActiveRegion> actions, Component c, Object value) {
        if (actions != null) {
            for (ActiveRegion a : actions) {
                regions.add(new RegionChangedUndoActionWithComponent(a, c, value));
            }
        }
    }

    public boolean isSame(Vector<ActiveRegion> actions) {
        if (actions == null || regions == null || actions.size() != regions.size()) {
            return false;
        }
        int i = 0;
        for (ActiveRegion a : actions) {
            ActiveRegion oldA = regions.elementAt(i).getRestoreRegion();
            String str1 = a.saveString();
            String str2 = oldA.saveString();
            if (!str1.equals(str2)) {
                return false;
            }
            i++;
        }


        return true;
    }

    public void restore() {
        for (RegionChangedUndoAction ua : regions) {
            if (ua.shouldUndo()) {
                ua.restore();
            }
        }
    }

    public boolean shouldRestore() {
        for (RegionChangedUndoAction ua : regions) {
            if (ua.shouldUndo()) {
                return true;
            }
        }

        return false;
    }
}
