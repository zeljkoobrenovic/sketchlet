/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui.undo;

import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.designer.editor.SketchletEditor;

/**
 * @author zobrenovic
 */
public class SketchChangedUndoAction extends UndoAction {

    Page page = null;
    Page restorePage = null;

    public SketchChangedUndoAction(Page page) {
        this.page = page;
        if (page != null) {
            this.restorePage = page.copyForUndo();
        }
    }

    public void restore() {
        if (page != null && this.restorePage != null) {
            if (SketchletEditor.editorPanel.currentPage != page) {
                SketchletEditor.editorPanel.openSketchAndWait(page);
            }
            page.setPropertiesFromSketch(this.restorePage);
            if (SketchletEditor.editorPanel.extraEditorPanel != null) {
                SketchletEditor.editorPanel.extraEditorPanel.refreshPageComponents();
            }
        }
    }

    public boolean shouldUndo() {
        return true;//SketchletEditor.editorPanel.currentSketch == this.sketch;
    }
}
