/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.tool;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.tools.log.ActivityLog;
import net.sf.sketchlet.designer.ui.playback.PlaybackFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * @author zobrenovic
 */
public class ActiveRegionTool extends Tool {

    int prevX, prevY;
    long prevTimestamp;
    double speeds[] = new double[5];
    int currentDistanceIndex = 0;
    double speed;
    Cursor cursor;

    public ActiveRegionTool(SketchletEditor freeHand) {
        super(freeHand);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Point hotSpot = new Point(5, 4);
        Image cursorImage = Workspace.createImageIcon("resources/cursor_active_region.png").getImage();
        cursor = toolkit.createCustomCursor(cursorImage, hotSpot, "Active Region");
    }

    public String getName() {
        return Language.translate("Active Region Create Tool");
    }

    public ImageIcon getIcon() {
        return Workspace.createImageIcon("resources/active_region.png");
    }

    public String getIconFileName() {
        return "active_region.png";
    }

    public void mouseMoved(MouseEvent e, int x, int y) {
        ActiveRegion a = freeHand.currentPage.regions.selectRegion(x, y, false);
        if (x < 0 || y < 0) {
            freeHand.setCursor(Cursor.getDefaultCursor());
            /*} else if (a != null) {
            a.mouseHandler.mouseMoved(e, editorPanel.scale, SketchletEditor.editorFrame, false);
             */
        } else {
            freeHand.setCursor();
        }
    }

    private boolean bDragged = false;

    public void mousePressed(MouseEvent e, int x, int y) {
        bDragged = false;
        freeHand.currentPage.regions.mousePressed(e, freeHand.scale, freeHand.editorFrame, false, true);
        if (freeHand.currentPage.regions.newRegion) {
            freeHand.saveNewRegionUndo();
        } else {
            freeHand.saveRegionUndo();
        }
        prevX = x;
        prevY = y;
        prevTimestamp = e.getWhen();

        for (int i = 0; i < speeds.length; i++) {
            speeds[i] = 0.0;
        }

    }

    public void mouseReleased(MouseEvent e, int x, int y) {
        //freeHand.setBusyCursor();
        boolean newRegion = freeHand.currentPage.regions.newRegion;
        freeHand.currentPage.regions.mouseReleased(e, freeHand.scale, freeHand.editorFrame, false);

        if (freeHand.currentPage.regions.selectedRegions == null || freeHand.currentPage.regions.bNoEffect || !bDragged) {
            if (freeHand.undoRegionActions.size() > 0) {
                freeHand.undoRegionActions.remove(freeHand.undoRegionActions.size() - 1);
            }
        }
        if (newRegion && freeHand.currentPage.regions.selectedRegions != null) {
            // ActivityLog.log("toolResult", "Create a new region", "active_region.png", toolInterface.getPanel());
            TutorialPanel.addLine("cmd", "Create a new region by dragging a rectangle area on the page", "active_region.png", toolInterface.getPanel());
            SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.activeRegionSelectTool, SketchletEditor.editorPanel);
        }

        freeHand.perspectivePanel.reload();
        freeHand.setCursor();
    }

    public void mouseDragged(MouseEvent e, int x, int y) {
        bDragged = true;
        freeHand.currentPage.regions.mouseDragged(e, freeHand.scale, freeHand.editorFrame, false);
        if (PlaybackFrame.playbackFrame != null && freeHand.currentPage.regions.selectedRegions != null) {
            freeHand.currentPage.regions.selectedRegions.lastElement().play();
        }

        if (freeHand.currentPage.regions.selectedRegions != null && freeHand.currentPage.regions.selectedRegions.size() > 0 && ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK || (e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)) {
            if (e.getWhen() != prevTimestamp) {
                ActiveRegion a = freeHand.currentPage.regions.selectedRegions.lastElement();
                int dx = x - prevX;
                int dy = y - prevY;
                double dt = (e.getWhen() - prevTimestamp) / 100.0;

                speed = Math.sqrt(dx * dx + dy * dy) / dt;

                speeds[currentDistanceIndex++] = speed;
                currentDistanceIndex = currentDistanceIndex % speeds.length;

                double _s = 0.0;
                for (int i = 0; i < speeds.length; i++) {
                    _s += speeds[i];
                }

                speed = _s / speeds.length;
                double _speed = a.limitsHandler.processLimits("speed", speed, 0.0, 0.0, true);

                /*
                if (_speed != speed) {
                x = (int) (prevX + dx * _speed / speed);
                y = (int) (prevY + dy * _speed / speed);
                }
                 */

                if (dx != 0 || dy != 0) {
                    if (!a.regionGrouping.equals("")) {
                        for (ActiveRegion as : a.parent.regions) {
                            if (as != a && as.regionGrouping.equals(a.regionGrouping)) {
                                as.limitsHandler.processLimits("position x", as.x1, 0, 0, true);
                                as.limitsHandler.processLimits("position y", as.y1, 0, 0, true);
                            }
                        }
                    }
                }
                prevX = x;
                prevY = y;
                prevTimestamp = e.getWhen();
            }
        }
    }

    public void mouseMoved(int x, int y, int modifiers) {
    }

    public void mousePressed(int x, int y, int modifiers) {
    }

    public void mouseReleased(int x, int y, int modifiers) {
        ActivityLog.log("toolResult", "Create a new region");
        TutorialPanel.addLine("cmd", "Create a new region by dragging a rectangle area on the page", "", toolInterface.getPanel());
    }

    public void mouseDragged(int x, int y, int modifiers) {
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void draw(Graphics2D g2) {
    }

    public void activate() {
    }

    public void deactivate() {
    }

    public void onUndo() {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }
}
