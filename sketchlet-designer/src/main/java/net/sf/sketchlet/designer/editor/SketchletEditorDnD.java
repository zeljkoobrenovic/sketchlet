/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor;

import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.editor.dnd.SelectDropAction;
import net.sf.sketchlet.designer.editor.dnd.SelectDropProperty;
import net.sf.sketchlet.designer.events.region.ActiveRegionMouseHandler;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.ui.page.SketchStatePanel;
import net.sf.sketchlet.designer.ui.region.ActiveRegionPanel;
import net.sf.sketchlet.designer.ui.region.ActiveRegionsFrame;
import net.sf.sketchlet.util.RefreshTime;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * @author zobrenovic
 */
public class SketchletEditorDnD {

    SketchletEditor editor;

    public SketchletEditorDnD(SketchletEditor editor) {
        this.editor = editor;
    }

    public static void processDroppedString(Point p, String strText) {
        int x = (int) ((p.getX()) / SketchletEditor.editorPanel.scale - SketchletEditor.marginX);
        int y = (int) ((p.getY()) / SketchletEditor.editorPanel.scale - SketchletEditor.marginY);
        FileDrop.bDragging = true;
        ActiveRegion region = SketchletEditor.editorPanel.currentPage.regions.selectRegion(x, y, false);
        FileDrop.bDragging = false;

        Point ptOnScreen = new Point(SketchletEditor.editorPanel.getLocationOnScreen().x + p.x, SketchletEditor.editorPanel.getLocationOnScreen().y + p.y);

        if (region != null) {
            Point2D ip = ActiveRegionMouseHandler.inversePoint(region, x, y, false);
            x = (int) ip.getX();
            y = (int) ip.getY();
            SketchletEditor.editorPanel.saveRegionUndo(region);
            SketchletEditor.editorPanel.skipUndo = true;

            if (strText.startsWith("=") && region.isInMouseIconArea(x, y)) {
                ActiveRegionsFrame.showRegionsAndActions();
                ActiveRegionsFrame.refresh(region, ActiveRegionPanel.indexEvents, ActiveRegionPanel.indexMouseEvents);
                TutorialPanel.addLine("cmd", "Drag the variable, and drop it on the region mouse icon", "arrow_cursor.png", Workspace.mainPanel.sketchletPanel.globalVariablesPanel.table, ptOnScreen);
                ActiveRegionPanel.currentActiveRegionPanel.mouseEventPanel.addNewEventMacro("Variable update", strText.substring(1), "");
            } else if (strText.startsWith("=") && region.isInRegionsIconArea(x, y)) {
                ActiveRegionsFrame.showRegionsAndActions();
                ActiveRegionsFrame.refresh(region, ActiveRegionPanel.indexEvents, ActiveRegionPanel.indexMouseEvents);
                TutorialPanel.addLine("cmd", "Drag the variable, and drop it on the region mouse icon", "arrow_cursor.png", Workspace.mainPanel.sketchletPanel.globalVariablesPanel.table, ptOnScreen);
                ActiveRegionPanel.currentActiveRegionPanel.regionOverlapEventsPanel.addNewRegionOverlapMacro("Variable update", strText.substring(1), "");
            } else if (strText.startsWith("=") && region.isInMappingIconArea(x, y)) {
                ActiveRegionsFrame.showRegionsAndActions();
                ActiveRegionPanel ap = ActiveRegionsFrame.refresh(region, ActiveRegionPanel.indexEvents, ActiveRegionPanel.indexMotion);
                int row = ap.getFreeMappingRow();
                region.updateTransformations[row][1] = strText.substring(1);
                TutorialPanel.addLine("cmd", "Drag the variable, and drop it on the region move & rotate icon", "arrow_cursor.png", Workspace.mainPanel.sketchletPanel.globalVariablesPanel.table, ptOnScreen);
                ap.editUpdateTransformationsEvent(row);
            } else if (strText.startsWith("@timer ") && region.isInMouseIconArea(x, y)) {
                TutorialPanel.addLine("cmd", "Drag the timer, and drop it on the region mouse icon", "arrow_cursor.png", SketchletEditor.editorPanel.timersTablePanel.table, ptOnScreen);
                ActiveRegionPanel.currentActiveRegionPanel.mouseEventPanel.addNewEventMacro("Start timer", strText.substring(7), "");
            } else if (strText.startsWith("@timer ") && region.isInRegionsIconArea(x, y)) {
                ActiveRegionsFrame.showRegionsAndActions();
                ActiveRegionsFrame.refresh(region, ActiveRegionPanel.indexEvents, ActiveRegionPanel.indexMouseEvents);
                TutorialPanel.addLine("cmd", "Drag the variable, and drop it on the region mouse icon", "arrow_cursor.png", Workspace.mainPanel.sketchletPanel.globalVariablesPanel.table, ptOnScreen);
                ActiveRegionPanel.currentActiveRegionPanel.regionOverlapEventsPanel.addNewRegionOverlapMacro("Start timer", strText.substring(7), "");
            } else if (strText.startsWith("@sketch ") && region.isInMouseIconArea(x, y)) {
                ActiveRegionsFrame.showRegionsAndActions();
                ActiveRegionsFrame.refresh(region, ActiveRegionPanel.indexEvents, ActiveRegionPanel.indexMouseEvents);
                TutorialPanel.addLine("cmd", "Drag the variable, and drop it on the region mouse icon", "arrow_cursor.png", Workspace.mainPanel.sketchletPanel.globalVariablesPanel.table, ptOnScreen);
                ActiveRegionPanel.currentActiveRegionPanel.regionOverlapEventsPanel.addNewRegionOverlapMacro("Go to page", strText.substring(8), "");
            } else if (strText.startsWith("@sketch ") && region.isInRegionsIconArea(x, y)) {
                TutorialPanel.addLine("cmd", "Drag the page, and drop it on the overlap & touch icon", "arrow_cursor.png", SketchletEditor.editorPanel.sketchListPanel.table, ptOnScreen);
                ActiveRegionPanel.currentActiveRegionPanel.mouseEventPanel.addNewEventMacro("Go to page", strText.substring(8), "");
            } else if (strText.startsWith("@macro ") && region.isInMouseIconArea(x, y)) {
                TutorialPanel.addLine("cmd", "Drag the macro, and drop it on the region mouse icon", "arrow_cursor.png", SketchletEditor.editorPanel.macrosTablePanel.table, ptOnScreen);
                ActiveRegionPanel.currentActiveRegionPanel.mouseEventPanel.addNewEventMacro("Start action", strText.substring(7), "");
            } else if (strText.startsWith("@macro ") && region.isInRegionsIconArea(x, y)) {
                ActiveRegionsFrame.showRegionsAndActions();
                ActiveRegionsFrame.refresh(region, ActiveRegionPanel.indexEvents, ActiveRegionPanel.indexMouseEvents);
                TutorialPanel.addLine("cmd", "Drag the variable, and drop it on the region mouse icon", "arrow_cursor.png", Workspace.mainPanel.sketchletPanel.globalVariablesPanel.table, ptOnScreen);
                ActiveRegionPanel.currentActiveRegionPanel.regionOverlapEventsPanel.addNewRegionOverlapMacro("Start macro", strText.substring(7), "");
            } else if (strText.startsWith("=")) {
                TutorialPanel.addLine("cmd", "Drag the variable, and drop it on the region parameters icon", "arrow_cursor.png", Workspace.mainPanel.sketchletPanel.globalVariablesPanel.table, ptOnScreen);
                new SelectDropAction(SketchletEditor.editorPanel.editorFrame, strText, region);
            }
            SketchletEditor.editorPanel.skipUndo = false;
            RefreshTime.update();
            SketchletEditor.editorPanel.repaint();
        } else {
            SketchletEditor.editorPanel.saveSketchUndo();
            SketchletEditor.editorPanel.skipUndo = true;
            if (strText.startsWith("=") && SketchletEditor.editorPanel.isInEntryArea(x, y)) {
                SketchStatePanel actions = SketchStatePanel.showStateProperties(SketchStatePanel.actionsTabIndex, SketchStatePanel.actionsOnEntrySubtabIndex);
                int row = actions.getFreeEntryRow();
                actions.pOnEntry.model.setValueAt("Variable update", row, 0);
                actions.pOnEntry.model.setValueAt(strText.substring(1), row, 1);
                TutorialPanel.addLine("cmd", "Drag the variable, and drop it on the entry events icon", "arrow_cursor.png", Workspace.mainPanel.sketchletPanel.globalVariablesPanel.table, ptOnScreen);
                actions.pOnEntry.editMacroActions(row);
            } else if (strText.startsWith("=") && SketchletEditor.editorPanel.isInExitArea(x, y)) {
                SketchStatePanel actions = SketchStatePanel.showStateProperties(SketchStatePanel.actionsTabIndex, SketchStatePanel.actionsOnExitSubtabIndex);
                int row = actions.getFreeExitRow();
                actions.pOnExit.model.setValueAt("Variable update", row, 0);
                actions.pOnExit.model.setValueAt(strText.substring(1), row, 1);
                TutorialPanel.addLine("cmd", "Drag the variable, and drop it on the exit events icon", "arrow_cursor.png", Workspace.mainPanel.sketchletPanel.globalVariablesPanel.table, ptOnScreen);
                actions.pOnExit.editMacroActions(row);
            } else if (strText.startsWith("=") && SketchletEditor.editorPanel.isInVariableInArea(x, y)) {
                SketchStatePanel actions = SketchStatePanel.showStateProperties(SketchStatePanel.actionsTabIndex, SketchStatePanel.actionsKeyboardSubtabIndex);
                actions.variableUpdateEventsPanel.addNewEventMacro("Update variable", strText.substring(1), "");
                TutorialPanel.addLine("cmd", "Drag the variable, and drop it on the variables updates event icon", "arrow_cursor.png", Workspace.mainPanel.sketchletPanel.globalVariablesPanel.table, ptOnScreen);
            } else if (strText.startsWith("=") && SketchletEditor.editorPanel.isInKeyboardArea(x, y)) {
                SketchStatePanel actions = SketchStatePanel.showStateProperties(SketchStatePanel.actionsTabIndex, SketchStatePanel.actionsKeyboardSubtabIndex);
                actions.keyboardEventsPanel.addNewEventMacro("Update variable", strText.substring(1), "");
                TutorialPanel.addLine("cmd", "Drag the variable, and drop it on the keyboard events icon", "arrow_cursor.png", Workspace.mainPanel.sketchletPanel.globalVariablesPanel.table, ptOnScreen);
            } else if (strText.startsWith("=") && SketchletEditor.editorPanel.isInPropertiesArea(x, y)) {
                TutorialPanel.addLine("cmd", "Drag the variable, and drop it on the page parameters icon", "arrow_cursor.png", Workspace.mainPanel.sketchletPanel.globalVariablesPanel.table, ptOnScreen);
                new SelectDropProperty(SketchletEditor.editorPanel.editorFrame, strText, SketchletEditor.editorPanel.currentPage);
            } else if (strText.startsWith("@timer ") && SketchletEditor.editorPanel.isInEntryArea(x, y)) {
                SketchStatePanel actions = SketchStatePanel.showStateProperties(SketchStatePanel.actionsTabIndex, SketchStatePanel.actionsOnEntrySubtabIndex);
                int row = actions.getFreeEntryRow();
                actions.pOnEntry.model.setValueAt("Start timer", row, 0);
                actions.pOnEntry.model.setValueAt(strText.substring(7), row, 1);
                TutorialPanel.addLine("cmd", "Drag the timer, and drop it on the entry events icon", "arrow_cursor.png", SketchletEditor.editorPanel.timersTablePanel.table, ptOnScreen);
                actions.pOnEntry.editMacroActions(row);
            } else if (strText.startsWith("@timer ") && SketchletEditor.editorPanel.isInExitArea(x, y)) {
                SketchStatePanel actions = SketchStatePanel.showStateProperties(SketchStatePanel.actionsTabIndex, SketchStatePanel.actionsOnExitSubtabIndex);
                int row = actions.getFreeExitRow();
                actions.pOnExit.model.setValueAt("Start timer", row, 0);
                actions.pOnExit.model.setValueAt(strText.substring(7), row, 1);
                TutorialPanel.addLine("cmd", "Drag the timer, and drop it on the exit events icon", "arrow_cursor.png", SketchletEditor.editorPanel.timersTablePanel.table, ptOnScreen);
                actions.pOnExit.editMacroActions(row);
            } else if (strText.startsWith("@timer ") && (SketchletEditor.editorPanel.isInVariableInArea(x, y) /*|| isInVariableOutArea(x, y)*/)) {
                SketchStatePanel actions = SketchStatePanel.showStateProperties(SketchStatePanel.actionsTabIndex, SketchStatePanel.actionsKeyboardSubtabIndex);
                actions.variableUpdateEventsPanel.addNewEventMacro("Start timer", strText.substring(7), "");
                TutorialPanel.addLine("cmd", "Drag the timer, and drop it on the varuable updates events icon", "arrow_cursor.png", SketchletEditor.editorPanel.timersTablePanel.table, ptOnScreen);
            } else if (strText.startsWith("@timer ") && SketchletEditor.editorPanel.isInKeyboardArea(x, y)) {
                SketchStatePanel actions = SketchStatePanel.showStateProperties(SketchStatePanel.actionsTabIndex, SketchStatePanel.actionsKeyboardSubtabIndex);
                actions.keyboardEventsPanel.addNewEventMacro("Start timer", strText.substring(7), "");
                TutorialPanel.addLine("cmd", "Drag the timer, and drop it on the keyboard events icon", "arrow_cursor.png", SketchletEditor.editorPanel.timersTablePanel.table, ptOnScreen);
            } else if (strText.startsWith("@sketch ") && SketchletEditor.editorPanel.isInEntryArea(x, y)) {
                SketchStatePanel actions = SketchStatePanel.showStateProperties(SketchStatePanel.actionsTabIndex, SketchStatePanel.actionsOnEntrySubtabIndex);
                int row = actions.getFreeEntryRow();
                actions.pOnEntry.model.setValueAt("Go to page", row, 0);
                actions.pOnEntry.model.setValueAt(strText.substring(8), row, 1);
                TutorialPanel.addLine("cmd", "Drag the page, and drop it on the entry event icon", "arrow_cursor.png", SketchletEditor.editorPanel.sketchListPanel.table, ptOnScreen);
                actions.pOnEntry.editMacroActions(row);
            } else if (strText.startsWith("@sketch ") && SketchletEditor.editorPanel.isInExitArea(x, y)) {
                SketchStatePanel actions = SketchStatePanel.showStateProperties(SketchStatePanel.actionsTabIndex, SketchStatePanel.actionsOnExitSubtabIndex);
                int row = actions.getFreeExitRow();
                actions.pOnExit.model.setValueAt("Go to page", row, 0);
                actions.pOnExit.model.setValueAt(strText.substring(8), row, 1);
                TutorialPanel.addLine("cmd", "Drag the page, and drop it on the exit event icon", "arrow_cursor.png", SketchletEditor.editorPanel.sketchListPanel.table, ptOnScreen);
                actions.pOnExit.editMacroActions(row);
            } else if (strText.startsWith("@sketch ") && (SketchletEditor.editorPanel.isInVariableInArea(x, y) /*|| isInVariableOutArea(x, y)*/)) {
                SketchStatePanel actions = SketchStatePanel.showStateProperties(SketchStatePanel.actionsTabIndex, SketchStatePanel.actionsKeyboardSubtabIndex);
                actions.variableUpdateEventsPanel.addNewEventMacro("Go to page", strText.substring(8), "");
                TutorialPanel.addLine("cmd", "Drag the page, and drop it on the variable supdates event icon", "arrow_cursor.png", SketchletEditor.editorPanel.sketchListPanel.table, ptOnScreen);
            } else if (strText.startsWith("@sketch ") && SketchletEditor.editorPanel.isInKeyboardArea(x, y)) {
                SketchStatePanel actions = SketchStatePanel.showStateProperties(SketchStatePanel.actionsTabIndex, SketchStatePanel.actionsKeyboardSubtabIndex);
                actions.keyboardEventsPanel.addNewEventMacro("Go to page", strText.substring(8), "");
                TutorialPanel.addLine("cmd", "Drag the page, and drop it on the keyboard events icon", "arrow_cursor.png", SketchletEditor.editorPanel.sketchListPanel.table, ptOnScreen);
            } else if (strText.startsWith("@macro ") && SketchletEditor.editorPanel.isInEntryArea(x, y)) {
                SketchStatePanel actions = SketchStatePanel.showStateProperties(SketchStatePanel.actionsTabIndex, SketchStatePanel.actionsOnEntrySubtabIndex);
                int row = actions.getFreeEntryRow();
                actions.pOnEntry.model.setValueAt("Start Action", row, 0);
                actions.pOnEntry.model.setValueAt(strText.substring(7), row, 1);
                TutorialPanel.addLine("cmd", "Drag the macro, and drop it on the entry events icon", "arrow_cursor.png", SketchletEditor.editorPanel.macrosTablePanel.table, ptOnScreen);
                actions.pOnEntry.editMacroActions(row);
            } else if (strText.startsWith("@macro ") && SketchletEditor.editorPanel.isInExitArea(x, y)) {
                SketchStatePanel actions = SketchStatePanel.showStateProperties(SketchStatePanel.actionsTabIndex, SketchStatePanel.actionsOnExitSubtabIndex);
                int row = actions.getFreeExitRow();
                actions.pOnExit.model.setValueAt("Start Action", row, 0);
                actions.pOnExit.model.setValueAt(strText.substring(7), row, 1);
                TutorialPanel.addLine("cmd", "Drag the macro, and drop it on the exit events icon", "arrow_cursor.png", SketchletEditor.editorPanel.macrosTablePanel.table, ptOnScreen);
                actions.pOnExit.editMacroActions(row);
            } else if (strText.startsWith("@macro ") && (SketchletEditor.editorPanel.isInVariableInArea(x, y) /*|| isInVariableOutArea(x, y)*/)) {
                SketchStatePanel actions = SketchStatePanel.showStateProperties(SketchStatePanel.actionsTabIndex, SketchStatePanel.actionsKeyboardSubtabIndex);
                actions.variableUpdateEventsPanel.addNewEventMacro("Start action", strText.substring(7), "");
                TutorialPanel.addLine("cmd", "Drag the macro, and drop it on the variables updates events icon", "arrow_cursor.png", SketchletEditor.editorPanel.macrosTablePanel.table, ptOnScreen);
            } else if (strText.startsWith("@macro ") && SketchletEditor.editorPanel.isInKeyboardArea(x, y)) {
                SketchStatePanel actions = SketchStatePanel.showStateProperties(SketchStatePanel.actionsTabIndex, SketchStatePanel.actionsKeyboardSubtabIndex);
                actions.keyboardEventsPanel.addNewEventMacro("Start action", strText.substring(7), "");
                TutorialPanel.addLine("cmd", "Drag the macro, and drop it on the keyboard events icon", "arrow_cursor.png", SketchletEditor.editorPanel.macrosTablePanel.table, ptOnScreen);
            }
            SketchletEditor.editorPanel.skipUndo = false;
            SketchletEditor.editorPanel.forceRepaint();
        }
    }
}
