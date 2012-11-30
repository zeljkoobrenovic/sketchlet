package net.sf.sketchlet.designer.editor.ui.region;

import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.extraeditor.ActiveRegionsExtraPanel;
import net.sf.sketchlet.framework.model.ActiveRegion;

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
        SketchletEditor.getInstance().getFormulaToolbar().reload();
        SketchletEditor.getInstance().getActiveRegionMenu().fillMenu();
    }

    public static void reload(ActiveRegion action) {
        ActiveRegionsExtraPanel.reload(action);
        if (SketchletEditor.getInstance() != null && SketchletEditor.getInstance().getFormulaToolbar() != null) {
            SketchletEditor.getInstance().getFormulaToolbar().reload();
            SketchletEditor.getInstance().getActiveRegionMenu().fillMenu();
        }
    }

    public static void refresh(ActiveRegion action) {
        ActiveRegionsExtraPanel.refresh(action);
        SketchletEditor.getInstance().getFormulaToolbar().refresh();
        SketchletEditor.getInstance().getActiveRegionMenu().fillMenu();
    }

    public static ActiveRegionPanel refresh(ActiveRegion action, int tabIndex) {
        SketchletEditor.getInstance().getFormulaToolbar().refresh();
        SketchletEditor.getInstance().getActiveRegionMenu().fillMenu();
        return ActiveRegionsExtraPanel.refresh(action, tabIndex);
    }

    public static ActiveRegionPanel refresh(ActiveRegion action, int tabIndex, int subTab) {
        SketchletEditor.getInstance().getFormulaToolbar().refresh();
        SketchletEditor.getInstance().getActiveRegionMenu().fillMenu();
        return ActiveRegionsExtraPanel.refresh(action, tabIndex, subTab);
    }
}
