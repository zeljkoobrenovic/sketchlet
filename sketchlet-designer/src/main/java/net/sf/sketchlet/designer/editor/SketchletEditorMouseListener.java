/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.editor.tool.ActiveRegionSelectTool;
import net.sf.sketchlet.designer.editor.tool.ActiveRegionTool;
import net.sf.sketchlet.designer.editor.tool.PostNoteTool;
import net.sf.sketchlet.designer.editor.tool.TrajectoryPointsTool;
import net.sf.sketchlet.designer.events.region.ActiveRegionMouseHandler;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.tools.log.ActivityLog;
import net.sf.sketchlet.designer.ui.extraeditor.ActiveRegionsExtraPanel;
import net.sf.sketchlet.designer.ui.page.SketchStatePanel;
import net.sf.sketchlet.designer.ui.profiles.Profiles;
import net.sf.sketchlet.designer.ui.region.ActiveRegionPanel;
import net.sf.sketchlet.designer.ui.region.ActiveRegionsFrame;
import net.sf.sketchlet.util.RefreshTime;

import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

class SketchletEditorMouseListener extends MouseInputAdapter {

    public SketchletEditorMouseListener() {
    }

    public void mouseMoved(MouseEvent e) {
        if (SketchletEditor.editorPanel == null) {
            return;
        }
        int x = (int) ((e.getPoint().x) / SketchletEditor.editorPanel.scale) - SketchletEditor.editorPanel.marginX;
        int y = (int) ((e.getPoint().y) / SketchletEditor.editorPanel.scale) - SketchletEditor.editorPanel.marginY;
        if (SketchletEditor.editorPanel.mode == EditorMode.ACTIONS) {
            if (SketchletEditor.editorPanel.tool != null) {
                SketchletEditor.editorPanel.tool.mouseMoved(e, x, y);
            }
            /*
            ActiveRegion a = SketchletEditor.editorPanel.currentSketch.actions.selectAction(x, y, false);
            if (a != null) {
            a.mouseMoved(e, SketchletEditor.editorPanel.scale, false);
            }*/
        } else if (SketchletEditor.editorPanel.mode == EditorMode.SKETCHING || SketchletEditor.editorPanel.currentPage == null || SketchletEditor.editorPanel.bDragging || SketchletEditor.editorPanel.bPasting) {
            if (x < 0 || y < 0) {
                SketchletEditor.editorPanel.setCursor(Cursor.getDefaultCursor());
            } else {
                SketchletEditor.editorPanel.setCursor();
            }
            if (SketchletEditor.editorPanel.tool != null && SketchletEditor.editorPanel.currentPage.isLayerActive(SketchletEditor.editorPanel.layer)) {
                SketchletEditor.editorPanel.tool.mouseMoved(x, y, e.getModifiers());
            }
            //        return;
        }

        showStatus(e);
    }

    public void showStatus(final MouseEvent e) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                int x = (int) ((e.getPoint().x) / SketchletEditor.editorPanel.scale) - SketchletEditor.editorPanel.marginX;
                int y = (int) ((e.getPoint().y) / SketchletEditor.editorPanel.scale) - SketchletEditor.editorPanel.marginY;
                SketchletEditor.editorPanel.statusX = x;
                SketchletEditor.editorPanel.statusY = y;
                //  SketchletEditor.editorPanel.repaint();
                if (/*SketchletEditor.editorPanel.mode == EditorMode.SKETCHING || */SketchletEditor.editorPanel.bDragging || SketchletEditor.editorPanel.currentPage == null) {
                    return;
                }
                String strMessage = "";
                if (SketchletEditor.editorPanel.mode == EditorMode.SKETCHING) {
                    strMessage = Language.translate("Sketching mode") + "  |  " + Language.translate("layer") + " " + (SketchletEditor.editorPanel.layer + 1) + "  |  x=" + x + ", y=" + y + "";
                } else if (SketchletEditor.editorPanel.mode == EditorMode.PREVIEW) {
                    strMessage = Language.translate("Preview mode");
                } else {
                    strMessage = Language.translate("Active regions mode") + "  |  x=" + x + ", y=" + y + "";
                }

                if (SketchletEditor.editorPanel.mode == EditorMode.ACTIONS && SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null && SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size() > 0) {
                    ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                    strMessage += "  " + Language.translate("selected region") + ": x=" + a.x1;
                    strMessage += ", y=" + a.y1;
                    strMessage += ", " + Language.translate("width=") + (a.x2 - a.x1);
                    strMessage += ", " + Language.translate("height=") + (a.y2 - a.y1);
                }
                // strMessage += "        Memory: " + Runtime.getRuntime().totalMemory() / (1024*1024) + "M / " + Runtime.getRuntime().maxMemory() / (1024*1024) + "M";
                SketchletEditor.editorPanel.statusBar.setText(strMessage);
            }
        });
    }

    public void mousePressed(MouseEvent e) {
        /*if (SketchletEditor.editorPanel.extraEditorPanel.height > 180) {
        SketchletEditor.editorPanel.extraEditorPanel.minimize.doClick();
        }*/

        SketchletEditor.editorPanel.bDragging = true;
        int x = (int) ((e.getPoint().x) / SketchletEditor.editorPanel.scale) - SketchletEditor.editorPanel.marginX;
        int y = (int) ((e.getPoint().y) / SketchletEditor.editorPanel.scale) - SketchletEditor.editorPanel.marginY;

        ActivityLog.log("mousePressed", "" + e.getButton() + " " + x + " " + y + " " + SketchletEditor.editorPanel.tool.getName());
        if (SketchletEditor.editorPanel.mode == EditorMode.SKETCHING || SketchletEditor.editorPanel.tool instanceof PostNoteTool) {
            if (SketchletEditor.editorPanel.tabsModes.getSelectedIndex() == 0) {
                SketchletEditor.editorPanel.currentPage.regions.mousePressed(e, SketchletEditor.editorPanel.scale, SketchletEditor.editorPanel.editorFrame, false, false);
            }
            if (SketchletEditor.editorPanel.currentPage.isLayerActive(SketchletEditor.editorPanel.layer) || SketchletEditor.editorPanel.tool instanceof TrajectoryPointsTool) {
                SketchletEditor.editorPanel.tool.mousePressed(x, y, e.getModifiers());
            }
            if (!(SketchletEditor.editorPanel.tool instanceof PostNoteTool)) {
                SketchletEditor.editorPanel.requestFocus();
            }
            return;
        } else if (SketchletEditor.editorPanel.currentPage != null) {
            if (x < 0 || y < 0) {
                // SketchletEditor.editorPanel.renderer.rulers.mousePressed((int) ((e.getPoint().x) / SketchletEditor.editorPanel.scale), (int) ((e.getPoint().y) / SketchletEditor.editorPanel.scale));
                SketchletEditor.editorPanel.requestFocus();
                RefreshTime.update();
                return;
            } else {
                SketchletEditor.editorPanel.tool.mousePressed(e, x, y);
            }
        }
        showStatus(e);
        SketchletEditor.editorPanel.requestFocus();
        SketchletEditor.editorPanel.repaint();
        RefreshTime.update();
    }

    public void mouseDragged(MouseEvent e) {
        int x = (int) ((e.getPoint().x) / SketchletEditor.editorPanel.scale) - SketchletEditor.editorPanel.marginX;
        int y = (int) ((e.getPoint().y) / SketchletEditor.editorPanel.scale) - SketchletEditor.editorPanel.marginY;

        if (SketchletEditor.editorPanel.mode == EditorMode.SKETCHING) {
            if (SketchletEditor.editorPanel.currentPage.isLayerActive(SketchletEditor.editorPanel.layer)) {
                SketchletEditor.editorPanel.tool.mouseDragged(x, y, e.getModifiers());
            }
            RefreshTime.update();
            return;
        } else if (SketchletEditor.editorPanel.currentPage != null) {
            //if (SketchletEditor.editorPanel.renderer.rulers.isDragging()) {
            //FreeHand.editorPanel.renderer.rulers.mouseDragged((int) ((e.getPoint().x) / SketchletEditor.editorPanel.scale), (int) ((e.getPoint().y) / SketchletEditor.editorPanel.scale));
            //return;
            //} else {
            SketchletEditor.editorPanel.tool.mouseDragged(e, x, y);
            //}
        }
        SketchletEditor.editorPanel.repaintIfNeeded();
        RefreshTime.update();
    }

    public void mouseReleased(MouseEvent e) {
        SketchletEditor.editorPanel.bDragging = false;
        int x = (int) ((e.getPoint().x) / SketchletEditor.editorPanel.scale) - SketchletEditor.editorPanel.marginX;
        int y = (int) ((e.getPoint().y) / SketchletEditor.editorPanel.scale) - SketchletEditor.editorPanel.marginY;
        ActivityLog.log("mouseReleased", "" + e.getButton() + " " + x + " " + y + " " + SketchletEditor.editorPanel.tool.getName());

        if (SketchletEditor.editorPanel.mode == EditorMode.SKETCHING) {
            if (SketchletEditor.editorPanel.currentPage.isLayerActive(SketchletEditor.editorPanel.layer)) {
                SketchletEditor.editorPanel.tool.mouseReleased(x, y, e.getModifiers());
            }
            RefreshTime.update();
            return;
        } else if (SketchletEditor.editorPanel.mode == EditorMode.ACTIONS && SketchletEditor.editorPanel.currentPage != null) {
            /*if (SketchletEditor.editorPanel.renderer.rulers.isDragging()) {
            SketchletEditor.editorPanel.renderer.rulers.mouseReleased((int) ((e.getPoint().x) / SketchletEditor.editorPanel.scale), (int) ((e.getPoint().y) / SketchletEditor.editorPanel.scale));
            return;
            }*/
            SketchletEditor.editorPanel.tool.mouseReleased(e, x, y);
            SketchletEditor.editorPanel.formulaToolbar.refresh();
        }

        SketchletEditor.editorPanel.enableControls();
        SketchletEditor.editorPanel.requestFocus();
        RefreshTime.update();
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            boolean mp = false;
            /*if (SketchletEditor.editorPanel.mode != EditorMode.ACTIONS && SketchletEditor.editorPanel.mode != EditorMode.SKETCHING) {
            return;
            //FreeHand.editorPanel.tabsModes.setSelectedIndex(1);
            //mousePressed(e);
            //mp = true;
            }*/

            if (SketchletEditor.editorPanel.currentPage != null && Profiles.isActive("active_regions_layer")) {
                int x = (int) ((e.getPoint().x) / SketchletEditor.editorPanel.scale) - SketchletEditor.editorPanel.marginX;
                int y = (int) ((e.getPoint().y) / SketchletEditor.editorPanel.scale) - SketchletEditor.editorPanel.marginY;

                if (SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null) {
                    ActiveRegionsExtraPanel.showRegionsAndActions();
                    TutorialPanel.addLine("cmd", Language.translate("Double-click on the active region to open settings"), "arrow_cursor.png", SketchletEditor.editorPanel.extraEditorPanel, 1000);
                    ActiveRegion region = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();

                    Point2D ip = ActiveRegionMouseHandler.inversePoint(region, x, y, false);
                    x = (int) ip.getX();
                    y = (int) ip.getY();

                    if (SketchletEditor.editorPanel.tool instanceof ActiveRegionTool || SketchletEditor.editorPanel.tool instanceof ActiveRegionSelectTool) {
                        /*if (region.isInMouseIconArea(x, y)) {
                        ActiveRegionsFrame.refresh(region, ActiveRegionPanel.indexEvents, ActiveRegionPanel.indexMouseEvents);
                        } else if (region.isInRegionsIconArea(x, y)) {
                        ActiveRegionsFrame.refresh(region, ActiveRegionPanel.indexEvents, ActiveRegionPanel.indexOverlap);
                        } else if (region.isInMappingIconArea(x, y)) {
                        ActiveRegionsFrame.refresh(region, ActiveRegionPanel.indexEvents, ActiveRegionPanel.indexMotion);
                        } else if (region.isInRegionsPropertiesArea(x, y)) {
                        ActiveRegionsFrame.refresh(region, ActiveRegionPanel.indexTransform);
                        } else {*/
                        if (region.strWidget.isEmpty()) {
                            if (region.strText.trim().isEmpty()) {
                                ActiveRegionsFrame.refresh(region, ActiveRegionPanel.indexGraphics);
                            } else {
                                ActiveRegionsFrame.refresh(region, ActiveRegionPanel.indexGraphics, 4);
                            }
                        } else {
                            ActiveRegionsFrame.refresh(region, ActiveRegionPanel.indexWidget);
                        }
                        //}
                    } else {
                        ActiveRegionsFrame.refresh(region, ActiveRegionPanel.indexGraphics);
                    }
                } else {
                    if (SketchletEditor.editorPanel.isInEntryArea(x, y)) {
                        SketchStatePanel.showStateProperties(SketchStatePanel.actionsTabIndex, SketchStatePanel.actionsOnEntrySubtabIndex);
                    } else if (SketchletEditor.editorPanel.isInExitArea(x, y)) {
                        SketchStatePanel.showStateProperties(SketchStatePanel.actionsTabIndex, SketchStatePanel.actionsOnExitSubtabIndex);
                    } else if (SketchletEditor.editorPanel.isInVariableInArea(x, y) /*|| SketchletEditor.editorPanel.isInVariableOutArea(x, y)*/) {
                        SketchStatePanel.showStateProperties(SketchStatePanel.actionsTabIndex, SketchStatePanel.actionsVariablesSubtabIndex);
                    } else if (SketchletEditor.editorPanel.isInKeyboardArea(x, y)) {
                        SketchStatePanel.showStateProperties(SketchStatePanel.actionsTabIndex, SketchStatePanel.actionsKeyboardSubtabIndex);
                    } else if (SketchletEditor.editorPanel.isInPropertiesArea(x, y)) {
                        SketchStatePanel.showStateProperties(SketchStatePanel.propertiesTabIndex, 0);
//                        SketchPropertiesFrame.createAndShowGUI(SketchletEditor.editorPanel.currentSketch);
                    }
                }
            }
            if (mp) {
                mouseReleased(e);
            }
        }
        RefreshTime.update();
    }
}
