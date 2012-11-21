/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.controllers;

import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.dnd.DropArea;
import net.sf.sketchlet.designer.editor.dnd.DropAreas;
import net.sf.sketchlet.designer.editor.dnd.InternalDroppedString;
import net.sf.sketchlet.designer.editor.dnd.InternallyDroppedRunnable;
import net.sf.sketchlet.designer.editor.dnd.SelectDropAction;
import net.sf.sketchlet.designer.editor.dnd.SelectDropProperty;
import net.sf.sketchlet.designer.editor.ui.page.PageDetailsPanel;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionPanel;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionsFrame;
import net.sf.sketchlet.model.ActiveRegion;
import net.sf.sketchlet.model.events.region.ActiveRegionMouseHandler;
import net.sf.sketchlet.renderer.page.PageRenderer;
import net.sf.sketchlet.util.RefreshTime;
import org.apache.commons.lang.StringUtils;

import java.awt.*;
import java.awt.geom.Point2D;

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
        pageDropAreas.addDropArea(new DropArea(PageRenderer.ENTRY_ICON, "on page entry", 24, 24, "page_actions", runnable));

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
        pageDropAreas.addDropArea(new DropArea(PageRenderer.EXIT_ICON, "on page exit", 24, 24, "page_actions", runnable));

        runnable = new InternallyDroppedRunnable() {
            @Override
            public void run(InternalDroppedString info) {
                PageDetailsPanel pageDetailsPanel = SketchletEditor.getInstance().getPageDetailsPanel();
                SketchletEditor.getInstance().showStatePanel(PageDetailsPanel.actionsTabIndex, PageDetailsPanel.actionsKeyboardSubtabIndex);
                pageDetailsPanel.keyboardEventsPanel.addNewEventMacro(info.getAction(), info.getParam1(), info.getParam2());
            }
        };
        pageDropAreas.addDropArea(new DropArea(PageRenderer.KEYBOARD_ICON, "on page keyboard events", 24, 24, "page_actions", runnable));

        runnable = new InternallyDroppedRunnable() {
            @Override
            public void run(InternalDroppedString info) {
                PageDetailsPanel pageDetailsPanel = SketchletEditor.getInstance().getPageDetailsPanel();
                SketchletEditor.getInstance().showStatePanel(PageDetailsPanel.actionsTabIndex, PageDetailsPanel.actionsMouseSubtabIndex);
                pageDetailsPanel.mouseEventsPanel.addNewEventMacro(info.getAction(), info.getParam1(), info.getParam2());
            }
        };
        pageDropAreas.addDropArea(new DropArea(PageRenderer.MOUSE_ICON, "on page mouse events", 24, 24, "page_actions", runnable));

        runnable = new InternallyDroppedRunnable() {
            @Override
            public void run(InternalDroppedString info) {
                PageDetailsPanel pageDetailsPanel = PageDetailsPanel.showStateProperties(PageDetailsPanel.actionsTabIndex, PageDetailsPanel.actionsKeyboardSubtabIndex);
                SketchletEditor.getInstance().showStatePanel(PageDetailsPanel.actionsTabIndex, PageDetailsPanel.actionsVariablesSubtabIndex);
                pageDetailsPanel.variableUpdateEventsPanel.addNewEventMacro("Update variable", info.getPastedText().substring(1), "");
            }
        };
        pageDropAreas.addDropArea(new DropArea(PageRenderer.VARIABLE_ICON_IN, "on variable update events", 24, 24, "page_actions,variables", runnable));

        runnable = new InternallyDroppedRunnable() {
            @Override
            public void run(InternalDroppedString info) {
                SketchletEditor.getInstance().showStatePanel(PageDetailsPanel.propertiesTabIndex, 0);
                new SelectDropProperty(SketchletEditor.getInstance().editorFrame, info.getPastedText(), SketchletEditor.getInstance().getCurrentPage());
            }
        };
        pageDropAreas.addDropArea(new DropArea(PageRenderer.PROPERTIES_ICON, "page properties", 24, 24, "page_properties", runnable));
    }

    public void processDroppedString(Point p, String text) {
        Point ptOnScreen = new Point(SketchletEditor.getInstance().getLocationOnScreen().x + p.x, SketchletEditor.getInstance().getLocationOnScreen().y + p.y);

        DropArea dropArea = pageDropAreas.getDropArea(p.x, p.y);
        if (dropArea != null) {
            SketchletEditor.getInstance().saveSketchUndo();
            SketchletEditor.getInstance().skipUndo = true;
            dropArea.getRunnable().run(DropArea.processInternallyDroppedString(ptOnScreen, text));
            SketchletEditor.getInstance().skipUndo = false;
            SketchletEditor.getInstance().forceRepaint();
            return;
        } else {
            int x = (int) ((p.getX()) / SketchletEditor.getInstance().getScale() - SketchletEditor.getInstance().getMarginX());
            int y = (int) ((p.getY()) / SketchletEditor.getInstance().getScale() - SketchletEditor.getInstance().getMarginY());
            FileDrop.setDragging(true);
            ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().selectRegion(x, y, false);
            FileDrop.setDragging(false);
            if (region != null) {
                String action = "", param1 = "", param2 = "";
                if (text.startsWith("=")) {
                    action = "Variable update";
                    param1 = text.substring(1);
                    param2 = "";
                } else if (text.startsWith("@timer ")) {
                    action = "Start timer";
                    param1 = text.substring(7);
                    param2 = "";
                } else if (text.startsWith("@sketch ")) {
                    action = "Go to page";
                    param1 = text.substring(8);
                    param2 = "";
                } else if (text.startsWith("@macro ")) {
                    action = "Start action";
                    param1 = text.substring(7);
                    param2 = "";
                }

                Point2D ip = ActiveRegionMouseHandler.inversePoint(region, x, y, false);
                x = (int) ip.getX();
                y = (int) ip.getY();
                SketchletEditor.getInstance().saveRegionUndo(region);
                SketchletEditor.getInstance().skipUndo = true;

                if (StringUtils.isNotBlank(action) && region.isInMouseIconArea(x, y)) {
                    ActiveRegionsFrame.showRegionsAndActions();
                    ActiveRegionsFrame.refresh(region, ActiveRegionPanel.indexEvents, ActiveRegionPanel.indexMouseEvents);
                    ActiveRegionPanel.currentActiveRegionPanel.mouseEventPanel.addNewEventMacro(action, param1, param2);
                } else if (StringUtils.isNotBlank(action) && region.isInRegionsIconArea(x, y)) {
                    ActiveRegionsFrame.showRegionsAndActions();
                    ActiveRegionsFrame.refresh(region, ActiveRegionPanel.indexEvents, ActiveRegionPanel.indexMouseEvents);
                    ActiveRegionPanel.currentActiveRegionPanel.regionOverlapEventsPanel.addNewRegionOverlapMacro(action, param1, param2);
                } else if (text.startsWith("=") && region.isInMappingIconArea(x, y)) {
                    ActiveRegionsFrame.showRegionsAndActions();
                    ActiveRegionPanel ap = ActiveRegionsFrame.refresh(region, ActiveRegionPanel.indexEvents, ActiveRegionPanel.indexMotion);
                    int row = ap.getFreeMappingRow();
                    region.updateTransformations[row][1] = text.substring(1);
                    ap.editUpdateTransformationsEvent(row);
                } else if (text.startsWith("=") && region.isInRegionsPropertiesArea(x, y)) {
                    new SelectDropAction(SketchletEditor.getInstance().editorFrame, text, region);
                }

                SketchletEditor.getInstance().skipUndo = false;
                RefreshTime.update();
                SketchletEditor.getInstance().repaint();
            }
        }
    }

    public DropAreas getPageDropAreas() {
        return pageDropAreas;
    }
}
