package net.sf.sketchlet.designer.editor.controllers;

import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.dnd.DropArea;
import net.sf.sketchlet.designer.editor.dnd.DropAreas;
import net.sf.sketchlet.designer.editor.dnd.InternalDroppedString;
import net.sf.sketchlet.designer.editor.dnd.InternallyDroppedRunnable;
import net.sf.sketchlet.designer.editor.dnd.SelectDropProperty;
import net.sf.sketchlet.designer.editor.ui.page.PageDetailsPanel;
import net.sf.sketchlet.framework.controller.ActiveRegionMouseController;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.renderer.DropAreasRenderer;

import java.awt.*;

/**
 * @author zobrenovic
 */
public class SketchletEditorDragAndDropController {

    private SketchletEditor editor;
    private DropAreas pageDropAreas = new DropAreas(DropAreas.Orientation.VERTICAL, 5, 5);

    public SketchletEditorDragAndDropController(SketchletEditor editor) {
        this.editor = editor;

        initPageDropArea();
    }

    private void initPageDropArea() {
        InternallyDroppedRunnable runnable = new InternallyDroppedRunnable() {
            @Override
            public void run(InternalDroppedString info) {
                PageDetailsPanel pageDetailsPanel = SketchletEditor.getInstance().getPageDetailsPanel();
                SketchletEditor.getInstance().showStatePanel(PageDetailsPanel.actionsTabIndex, PageDetailsPanel.actionsOnEntrySubtabIndex);
                int row = pageDetailsPanel.pOnEntry.getMacro().getLastNonEmptyRow() + 1;
                pageDetailsPanel.pOnEntry.model.setValueAt(info.getAction(), row, 0);
                pageDetailsPanel.pOnEntry.model.setValueAt(info.getParam1(), row, 1);
                pageDetailsPanel.pOnEntry.model.setValueAt(info.getParam2(), row, 2);
                pageDetailsPanel.pOnEntry.editMacroActions(row);
            }
        };
        pageDropAreas.addDropArea(new DropArea(DropAreasRenderer.ENTRY_ICON, "on page entry", 24, 24, "page_actions", runnable));

        runnable = new InternallyDroppedRunnable() {
            @Override
            public void run(InternalDroppedString info) {
                PageDetailsPanel pageDetailsPanel = SketchletEditor.getInstance().getPageDetailsPanel();
                SketchletEditor.getInstance().showStatePanel(PageDetailsPanel.actionsTabIndex, PageDetailsPanel.actionsOnExitSubtabIndex);
                int row = pageDetailsPanel.pOnExit.getMacro().getLastNonEmptyRow() + 1;
                pageDetailsPanel.pOnExit.model.setValueAt(info.getAction(), row, 0);
                pageDetailsPanel.pOnExit.model.setValueAt(info.getParam1(), row, 1);
                pageDetailsPanel.pOnExit.model.setValueAt(info.getParam2(), row, 2);
                pageDetailsPanel.pOnExit.editMacroActions(row);
            }
        };
        pageDropAreas.addDropArea(new DropArea(DropAreasRenderer.EXIT_ICON, "on page exit", 24, 24, "page_actions", runnable));

        runnable = new InternallyDroppedRunnable() {
            @Override
            public void run(InternalDroppedString info) {
                PageDetailsPanel pageDetailsPanel = SketchletEditor.getInstance().getPageDetailsPanel();
                SketchletEditor.getInstance().showStatePanel(PageDetailsPanel.actionsTabIndex, PageDetailsPanel.actionsKeyboardSubtabIndex);
                pageDetailsPanel.keyboardEventsPanel.addNewEventMacro(info.getAction(), info.getParam1(), info.getParam2());
            }
        };
        pageDropAreas.addDropArea(new DropArea(DropAreasRenderer.KEYBOARD_ICON, "on page keyboard events", 24, 24, "page_actions", runnable));

        runnable = new InternallyDroppedRunnable() {
            @Override
            public void run(InternalDroppedString info) {
                PageDetailsPanel pageDetailsPanel = SketchletEditor.getInstance().getPageDetailsPanel();
                SketchletEditor.getInstance().showStatePanel(PageDetailsPanel.actionsTabIndex, PageDetailsPanel.actionsMouseSubtabIndex);
                pageDetailsPanel.mouseEventsPanel.addNewEventMacro(info.getAction(), info.getParam1(), info.getParam2());
            }
        };
        pageDropAreas.addDropArea(new DropArea(DropAreasRenderer.MOUSE_ICON, "on page mouse events", 24, 24, "page_actions", runnable));

        runnable = new InternallyDroppedRunnable() {
            @Override
            public void run(InternalDroppedString info) {
                PageDetailsPanel pageDetailsPanel = PageDetailsPanel.showStateProperties(PageDetailsPanel.actionsTabIndex, PageDetailsPanel.actionsKeyboardSubtabIndex);
                SketchletEditor.getInstance().showStatePanel(PageDetailsPanel.actionsTabIndex, PageDetailsPanel.actionsVariablesSubtabIndex);
                pageDetailsPanel.variableUpdateEventsPanel.addNewEventMacro("Update variable", info.getPastedText().substring(1), "");
            }
        };
        pageDropAreas.addDropArea(new DropArea(DropAreasRenderer.VARIABLE_ICON_IN, "on variable update events", 24, 24, "page_actions,variables", runnable));

        runnable = new InternallyDroppedRunnable() {
            @Override
            public void run(InternalDroppedString info) {
                SketchletEditor.getInstance().showStatePanel(PageDetailsPanel.propertiesTabIndex, 0);
                new SelectDropProperty(SketchletEditor.getInstance().editorFrame, info.getPastedText(), SketchletEditor.getInstance().getCurrentPage());
            }
        };
        DropArea dropAreaPageProperties = new DropArea(DropAreasRenderer.PROPERTIES_ICON, "page properties", 24, 24, "page_properties", runnable);
        dropAreaPageProperties.setAcceptMacrosEnabled(false);
        dropAreaPageProperties.setAcceptTimersEnabled(false);
        dropAreaPageProperties.setAcceptPagesEnabled(false);
        pageDropAreas.addDropArea(dropAreaPageProperties);
    }

    public void processDroppedString(Point p, String text) {
        Point ptOnScreen = new Point(SketchletEditor.getInstance().getLocationOnScreen().x + p.x, SketchletEditor.getInstance().getLocationOnScreen().y + p.y);

        DropArea dropArea = pageDropAreas.getDropArea(p.x, p.y);
        if (dropArea == null) {
            dropArea = getActiveRegionDropArea(p.x, p.y);
        }
        if (dropArea != null) {
            SketchletEditor.getInstance().saveSketchUndo();
            SketchletEditor.getInstance().skipUndo = true;
            dropArea.getRunnable().run(DropArea.processInternallyDroppedString(ptOnScreen, text));
            SketchletEditor.getInstance().skipUndo = false;
            SketchletEditor.getInstance().forceRepaint();
        }
    }

    private DropArea getActiveRegionDropArea(int x, int y) {
        for (ActiveRegion region : this.editor.getCurrentPage().getRegions().getRegions()) {
            ActiveRegionMouseController mouseController = region.getMouseController();
            if (mouseController != null) {
                DropAreas dropAreas = mouseController.getDropAreas();
                if (dropAreas != null) {
                    DropArea dropArea = dropAreas.getDropArea(x, y);
                    if (dropArea != null) {
                        return dropArea;
                    }
                }
            }
        }

        return null;
    }

    public DropAreas getPageDropAreas() {
        return pageDropAreas;
    }
}
