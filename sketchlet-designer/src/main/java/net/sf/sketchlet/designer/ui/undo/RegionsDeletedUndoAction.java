/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui.undo;

import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.ui.region.ActiveRegionsFrame;

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
            if (SketchletEditor.editorPanel.currentPage != reg.getSketch()) {
                SketchletEditor.editorPanel.openSketchAndWait(reg.getSketch());
            }
            SketchletEditor.editorPanel.currentPage.regions.regions.insertElementAt(reg, 0);
            SketchletEditor.editorPanel.currentPage.regions.selectedRegions = new Vector<ActiveRegion>();
            SketchletEditor.editorPanel.currentPage.regions.addToSelection(reg);
        }
        ActiveRegionsFrame.reload();
    }
}
