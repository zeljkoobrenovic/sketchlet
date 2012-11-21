/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.tool;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.tools.log.ActivityLog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * @author zobrenovic
 */
public class FreeFormSelectTool extends SelectTool {

    private Polygon polygon = new Polygon();

    public FreeFormSelectTool(ToolInterface toolInterface) {
        super(toolInterface);
    }

    @Override
    public void mousePressed(int x, int y, int modifiers) {
        if (!selected) {
            polygon = new Polygon();
        }
        super.mousePressed(x, y, modifiers);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        polygon = new Polygon();
    }

    @Override
    public void mouseReleased(int x, int y, int modifiers) {
        super.mouseReleased(x, y, modifiers);
        ActivityLog.log("toolResult", (selected ? "Move the selected image area in " : "Select an image area in ") + toolInterface.getName(), "select_freeform.png", toolInterface.getPanel());
    }

    @Override
    public void mouseDragged(int x, int y, int modifiers) {
        if (!selected) {
            polygon.addPoint(x, y);
            Rectangle rect = polygon.getBounds();

            if (rect != null) {
                setX1((int) rect.getMinX());
                setY1((int) rect.getMinY());
                setX2((int) rect.getMaxX());
                setY2((int) rect.getMaxY());
            }
            toolInterface.repaintImage();
        } else {
            int dx = x - prev_x;
            int dy = y - prev_y;
            polygon.translate(dx, dy);
            super.mouseDragged(x, y, modifiers);
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        if (selectedClip == null) {
            bDrawRect = false;
        } else {
            bDrawRect = getX2() - getX1() != selectedClip.getWidth() || getY2() - getY1() != selectedClip.getHeight();
        }
        super.draw(g2);
        if (polygon.npoints > 2 && !bDrawRect) {
            g2.drawPolygon(polygon);
        }
    }

    @Override
    public void onUndo() {
        polygon = new Polygon();
        super.onUndo();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            polygon = new Polygon();
        }
    }

    @Override
    public ImageIcon getIcon() {
        return Workspace.createImageIcon("resources/select_freeform.png");
    }

    @Override
    public String getIconFileName() {
        return "select_freeform.png";
    }

    @Override
    public String getName() {
        return Language.translate("Free Form Select");
    }
}
