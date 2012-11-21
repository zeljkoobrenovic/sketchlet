/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.ui.undo;

import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.model.Page;

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
            if (SketchletEditor.getInstance().getCurrentPage() != page) {
                SketchletEditor.getInstance().openSketchAndWait(page);
            }
            page.setPropertiesFromSketch(this.restorePage);
            if (SketchletEditor.getInstance().getExtraEditorPanel() != null) {
                SketchletEditor.getInstance().getExtraEditorPanel().refreshPageComponents();
            }
        }
    }

    public boolean shouldUndo() {
        return true;//SketchletEditor.editorPanel.currentSketch == this.sketch;
    }
}
