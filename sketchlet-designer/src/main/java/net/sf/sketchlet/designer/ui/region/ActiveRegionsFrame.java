/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui.region;

import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.ui.extraeditor.ActiveRegionsExtraPanel;

import javax.swing.*;

/**
 * @author cuypers
 */
public class ActiveRegionsFrame extends JFrame {

    private ActiveRegionsFrame() {
    }

    public static ActiveRegionsFrame reagionsAndActions = null;

    public static void updateTables() {
    }

    public static void closeRegionsAndActions() {
    }

    static int positionX = 0;
    static int positionY = 0;

    public static void showRegionsAndActions(final boolean bShow) {
        ActiveRegionsExtraPanel.showRegionsAndActions(bShow);
    }

    public static void showRegionsAndActions() {
        ActiveRegionsExtraPanel.showRegionsAndActions();
    }

    public static void showRegionsAndActionsImage(int tabIndex) {
        ActiveRegionsExtraPanel.showRegionsAndActionsImage(tabIndex);
    }

    public static void showRegionsAndActions(int tabIndex) {
        ActiveRegionsExtraPanel.showRegionsAndActions(tabIndex);
    }

    public static void reload() {
        ActiveRegionsExtraPanel.reload();
        SketchletEditor.editorPanel.formulaToolbar.reload();
        SketchletEditor.editorPanel.activeRegionMenu.fillMenu();
    }

    public static void reload(ActiveRegion action) {
        ActiveRegionsExtraPanel.reload(action);
        if (SketchletEditor.editorPanel != null && SketchletEditor.editorPanel.formulaToolbar != null) {
            SketchletEditor.editorPanel.formulaToolbar.reload();
            SketchletEditor.editorPanel.activeRegionMenu.fillMenu();
        }
    }

    public static void refresh(ActiveRegion action) {
        ActiveRegionsExtraPanel.refresh(action);
        SketchletEditor.editorPanel.formulaToolbar.refresh();
        SketchletEditor.editorPanel.activeRegionMenu.fillMenu();
    }

    public static ActiveRegionPanel refresh(ActiveRegion action, int tabIndex) {
        SketchletEditor.editorPanel.formulaToolbar.refresh();
        SketchletEditor.editorPanel.activeRegionMenu.fillMenu();
        // ActiveRegionsExtraPanel.refresh(action);
        return ActiveRegionsExtraPanel.refresh(action, tabIndex);
    }

    public static ActiveRegionPanel refresh(ActiveRegion action, int tabIndex, int subTab) {
        SketchletEditor.editorPanel.formulaToolbar.refresh();
        SketchletEditor.editorPanel.activeRegionMenu.fillMenu();
        // ActiveRegionsExtraPanel.refresh(action);
        return ActiveRegionsExtraPanel.refresh(action, tabIndex, subTab);
    }
}
