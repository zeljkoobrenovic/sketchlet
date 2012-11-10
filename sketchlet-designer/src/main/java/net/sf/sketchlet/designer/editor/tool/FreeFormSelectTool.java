/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.tool;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.tools.log.ActivityLog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * @author zobrenovic
 */
public class FreeFormSelectTool extends SelectTool {

    Polygon polygon = new Polygon();

    public FreeFormSelectTool(ToolInterface toolInterface) {
        super(toolInterface);
    }

    public void mousePressed(int x, int y, int modifiers) {
        if (!selected) {
            polygon = new Polygon();
        }
        super.mousePressed(x, y, modifiers);
    }

    public void deactivate() {
        super.deactivate();
        polygon = new Polygon();
    }

    public void mouseReleased(int x, int y, int modifiers) {
        super.mouseReleased(x, y, modifiers);
        ActivityLog.log("toolResult", (selected ? "Move the selected image area in " : "Select an image area in ") + toolInterface.getName(), "select_freeform.png", toolInterface.getPanel());
        TutorialPanel.addLine("cmd", (selected ? "Move the selected image area in " : "Select an image area by dragging in ") + toolInterface.getName(), "", toolInterface.getPanel());
    }

    public void mouseDragged(int x, int y, int modifiers) {
        if (!selected) {
            polygon.addPoint(x, y);
            Rectangle rect = polygon.getBounds();

            if (rect != null) {
                x1 = (int) rect.getMinX();
                y1 = (int) rect.getMinY();
                x2 = (int) rect.getMaxX();
                y2 = (int) rect.getMaxY();
            }
            toolInterface.repaintImage();
        } else {
            int dx = x - prev_x;
            int dy = y - prev_y;
            polygon.translate(dx, dy);
            super.mouseDragged(x, y, modifiers);
        }
    }

    public void draw(Graphics2D g2) {
        if (selectedClip == null) {
            bDrawRect = false;
        } else {
            bDrawRect = x2 - x1 != selectedClip.getWidth() || y2 - y1 != selectedClip.getHeight();
        }
        super.draw(g2);
        if (polygon.npoints > 2 && !bDrawRect) {
            g2.drawPolygon(polygon);
        }
    }

    public void onUndo() {
        polygon = new Polygon();
        super.onUndo();
    }

    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            polygon = new Polygon();
        }
    }

    public void getClip() {
        if (selectedClip == null) {
            selectedClip = toolInterface.extractImage(polygon);
        }
    }

    public ImageIcon getIcon() {
        return Workspace.createImageIcon("resources/select_freeform.png");
    }

    public String getIconFileName() {
        return "select_freeform.png";
    }

    public String getName() {
        return Language.translate("Free Form Select");
    }
}
