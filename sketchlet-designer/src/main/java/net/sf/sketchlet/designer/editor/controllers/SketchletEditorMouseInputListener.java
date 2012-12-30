package net.sf.sketchlet.designer.editor.controllers;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.SketchletEditorMode;
import net.sf.sketchlet.designer.editor.tool.ActiveRegionSelectTool;
import net.sf.sketchlet.designer.editor.tool.ActiveRegionTool;
import net.sf.sketchlet.designer.editor.tool.PostNoteTool;
import net.sf.sketchlet.designer.editor.tool.TrajectoryPointsTool;
import net.sf.sketchlet.framework.controller.ActiveRegionMouseController;
import net.sf.sketchlet.framework.model.log.ActivityLog;
import net.sf.sketchlet.designer.editor.ui.extraeditor.ActiveRegionsExtraPanel;
import net.sf.sketchlet.designer.editor.ui.profiles.Profiles;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionPanel;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionsFrame;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.util.RefreshTime;

import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class SketchletEditorMouseInputListener extends MouseInputAdapter {

    public SketchletEditorMouseInputListener() {
    }

    public void mouseMoved(MouseEvent e) {
        if (SketchletEditor.getInstance() == null) {
            return;
        }
        int x = (int) ((e.getPoint().x) / SketchletEditor.getInstance().getScale()) - SketchletEditor.getInstance().getMarginX();
        int y = (int) ((e.getPoint().y) / SketchletEditor.getInstance().getScale()) - SketchletEditor.getInstance().getMarginY();
        if (SketchletEditor.getInstance().getMode() == SketchletEditorMode.EDITING_REGIONS) {
            if (SketchletEditor.getInstance().getTool() != null) {
                SketchletEditor.getInstance().getTool().mouseMoved(e, x, y);
            }
        } else if (SketchletEditor.getInstance().getMode() == SketchletEditorMode.SKETCHING || SketchletEditor.getInstance().getCurrentPage() == null || SketchletEditor.getInstance().isDragging() || SketchletEditor.getInstance().isPasting()) {
            if (x < 0 || y < 0) {
                SketchletEditor.getInstance().setCursor(Cursor.getDefaultCursor());
            } else {
                SketchletEditor.getInstance().setCursor();
            }
            if (SketchletEditor.getInstance().getTool() != null && SketchletEditor.getInstance().getCurrentPage().isLayerActive(SketchletEditor.getInstance().getLayer())) {
                SketchletEditor.getInstance().getTool().mouseMoved(x, y, e.getModifiers());
            }
        }

        showStatus(e);
    }

    public void showStatus(final MouseEvent e) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                int x = (int) ((e.getPoint().x) / SketchletEditor.getInstance().getScale()) - SketchletEditor.getInstance().getMarginX();
                int y = (int) ((e.getPoint().y) / SketchletEditor.getInstance().getScale()) - SketchletEditor.getInstance().getMarginY();
                SketchletEditor.getInstance().setStatusX(x);
                SketchletEditor.getInstance().setStatusY(y);
                if (/*SketchletEditor.editorPanel.mode == SketchletEditorMode.SKETCHING || */SketchletEditor.getInstance().isDragging() || SketchletEditor.getInstance().getCurrentPage() == null) {
                    return;
                }
                String strMessage = "";
                if (SketchletEditor.getInstance().getMode() == SketchletEditorMode.SKETCHING) {
                    strMessage = Language.translate("Sketching mode") + "  |  " + Language.translate("layer") + " " + (SketchletEditor.getInstance().getLayer() + 1) + "  |  x=" + x + ", y=" + y + "";
                } else if (SketchletEditor.getInstance().getMode() == SketchletEditorMode.PREVIEW) {
                    strMessage = Language.translate("Preview mode");
                } else {
                    strMessage = Language.translate("Active regions mode") + "  |  x=" + x + ", y=" + y + "";
                }

                if (SketchletEditor.getInstance().getMode() == SketchletEditorMode.EDITING_REGIONS && SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
                    ActiveRegion a = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                    strMessage += "  " + Language.translate("selected region") + ": x=" + a.getX1Value();
                    strMessage += ", y=" + a.getY1Value();
                    strMessage += ", " + Language.translate("width=") + (a.getX2Value() - a.getX1Value());
                    strMessage += ", " + Language.translate("height=") + (a.getY2Value() - a.getY1Value());
                }
                SketchletEditor.getInstance().getStatusBar().setText(strMessage);
            }
        });
    }

    public void mousePressed(MouseEvent e) {
        SketchletEditor.getInstance().setDragging(true);
        int x = (int) ((e.getPoint().x) / SketchletEditor.getInstance().getScale()) - SketchletEditor.getInstance().getMarginX();
        int y = (int) ((e.getPoint().y) / SketchletEditor.getInstance().getScale()) - SketchletEditor.getInstance().getMarginY();

        ActivityLog.log("mousePressed", "" + e.getButton() + " " + x + " " + y + " " + SketchletEditor.getInstance().getTool().getName());
        if (SketchletEditor.getInstance().getMode() == SketchletEditorMode.SKETCHING || SketchletEditor.getInstance().getTool() instanceof PostNoteTool) {
            if (SketchletEditor.getInstance().getSelectedModesTabIndex() == 0) {
                SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().mousePressed(e, SketchletEditor.getInstance().getScale(), SketchletEditor.getInstance().editorFrame, false, false);
            }
            if (SketchletEditor.getInstance().getCurrentPage().isLayerActive(SketchletEditor.getInstance().getLayer()) || SketchletEditor.getInstance().getTool() instanceof TrajectoryPointsTool) {
                SketchletEditor.getInstance().getTool().mousePressed(x, y, e.getModifiers());
            }
            if (!(SketchletEditor.getInstance().getTool() instanceof PostNoteTool)) {
                SketchletEditor.getInstance().requestFocus();
            }
            return;
        } else if (SketchletEditor.getInstance().getCurrentPage() != null) {
            if (x < 0 || y < 0) {
                SketchletEditor.getInstance().requestFocus();
                RefreshTime.update();
                return;
            } else {
                SketchletEditor.getInstance().getTool().mousePressed(e, x, y);
            }
        }
        showStatus(e);
        SketchletEditor.getInstance().requestFocus();
        SketchletEditor.getInstance().repaint();
        RefreshTime.update();
    }

    public void mouseDragged(MouseEvent e) {
        int x = (int) ((e.getPoint().x) / SketchletEditor.getInstance().getScale()) - SketchletEditor.getInstance().getMarginX();
        int y = (int) ((e.getPoint().y) / SketchletEditor.getInstance().getScale()) - SketchletEditor.getInstance().getMarginY();

        if (SketchletEditor.getInstance().getMode() == SketchletEditorMode.SKETCHING) {
            if (SketchletEditor.getInstance().getCurrentPage().isLayerActive(SketchletEditor.getInstance().getLayer())) {
                SketchletEditor.getInstance().getTool().mouseDragged(x, y, e.getModifiers());
            }
            RefreshTime.update();
            return;
        } else if (SketchletEditor.getInstance().getCurrentPage() != null) {
            SketchletEditor.getInstance().getTool().mouseDragged(e, x, y);
        }
        SketchletEditor.getInstance().repaintIfNeeded();
        RefreshTime.update();
    }

    public void mouseReleased(MouseEvent e) {
        SketchletEditor.getInstance().setDragging(false);
        int x = (int) ((e.getPoint().x) / SketchletEditor.getInstance().getScale()) - SketchletEditor.getInstance().getMarginX();
        int y = (int) ((e.getPoint().y) / SketchletEditor.getInstance().getScale()) - SketchletEditor.getInstance().getMarginY();
        ActivityLog.log("mouseReleased", "" + e.getButton() + " " + x + " " + y + " " + SketchletEditor.getInstance().getTool().getName());

        if (SketchletEditor.getInstance().getMode() == SketchletEditorMode.SKETCHING) {
            if (SketchletEditor.getInstance().getCurrentPage().isLayerActive(SketchletEditor.getInstance().getLayer())) {
                SketchletEditor.getInstance().getTool().mouseReleased(x, y, e.getModifiers());
            }
            RefreshTime.update();
            return;
        } else if (SketchletEditor.getInstance().getMode() == SketchletEditorMode.EDITING_REGIONS && SketchletEditor.getInstance().getCurrentPage() != null) {
            SketchletEditor.getInstance().getTool().mouseReleased(e, x, y);
            SketchletEditor.getInstance().getFormulaToolbar().refresh();
        }

        SketchletEditor.getInstance().enableControls();
        SketchletEditor.getInstance().requestFocus();
        RefreshTime.update();
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            boolean mp = false;

            if (SketchletEditor.getInstance().getCurrentPage() != null && Profiles.isActive("active_regions_layer")) {
                int x = (int) ((e.getPoint().x) / SketchletEditor.getInstance().getScale()) - SketchletEditor.getInstance().getMarginX();
                int y = (int) ((e.getPoint().y) / SketchletEditor.getInstance().getScale()) - SketchletEditor.getInstance().getMarginY();

                if (SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null) {
                    ActiveRegionsExtraPanel.showRegionsAndActions();
                    ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();

                    Point2D ip = ActiveRegionMouseController.inversePoint(region, x, y, false);
                    x = (int) ip.getX();
                    y = (int) ip.getY();

                    if (SketchletEditor.getInstance().getTool() instanceof ActiveRegionTool || SketchletEditor.getInstance().getTool() instanceof ActiveRegionSelectTool) {
                        if (region.getWidget().isEmpty()) {
                            if (region.getText().trim().isEmpty()) {
                                ActiveRegionsFrame.refresh(region, ActiveRegionPanel.getIndexGraphics());
                            } else {
                                ActiveRegionsFrame.refresh(region, ActiveRegionPanel.getIndexGraphics(), 4);
                            }
                        } else {
                            ActiveRegionsFrame.refresh(region, ActiveRegionPanel.getIndexWidget());
                        }
                        //}
                    } else {
                        ActiveRegionsFrame.refresh(region, ActiveRegionPanel.getIndexGraphics());
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
