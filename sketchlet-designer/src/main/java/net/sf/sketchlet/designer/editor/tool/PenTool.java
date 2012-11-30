package net.sf.sketchlet.designer.editor.tool;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.tool.stroke.WobbleStroke;
import net.sf.sketchlet.framework.model.log.ActivityLog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;

public class PenTool extends ShapeTool {

    private Polygon polygon = new Polygon();
    private Image cursorImage = Workspace.createImageIcon("resources/cursor_pencil_annotate.gif").getImage();
    private Cursor cursor;

    public PenTool(ToolInterface toolInterface) {
        super(toolInterface, new String[]{"stroke type", "stroke width", "fill patterns"});
    }

    @Override
    public void deactivate() {
        polygon = new Polygon();
        super.deactivate();
    }

    @Override
    public ImageIcon getIcon() {
        return Workspace.createImageIcon("resources/pencil_annotate.png");
    }

    @Override
    public String getIconFileName() {
        return "pencil_annotate.png";
    }

    @Override
    public void draw(Graphics2D g2, int x, int y, int w, int h) {
        if (polygon.npoints > 1) {
            if (toolInterface.getStroke() instanceof WobbleStroke) {
                for (int i = 1; i < polygon.npoints; i++) {
                    int x1 = polygon.xpoints[i - 1];
                    int y1 = polygon.ypoints[i - 1];
                    int x2 = polygon.xpoints[i];
                    int y2 = polygon.ypoints[i];
                    g2.drawLine(x1, y1, x2, y2);
                }
            } else {
                g2.drawPolyline(polygon.xpoints, polygon.ypoints, polygon.npoints);
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        if (drawStrokeWidth) {
            int w = toolInterface.getStrokeWidth() + 2;
            Color c = g2.getColor();
            g2.setColor(Color.GRAY);
            g2.drawOval(mouseX - w / 2, mouseY - w / 2, w, w);
            g2.setColor(c);
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
        g2.setStroke(toolInterface.getStroke());
        g2.setColor(toolInterface.getColor());
        draw(g2, Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
    }

    @Override
    public void mousePressed(int x, int y, int modifiers) {
        polygon = new Polygon();
        polygon.addPoint(x, y);
        super.mousePressed(x, y, modifiers);
    }

    @Override
    public void mouseDragged(int x, int y, int modifiers) {
        this.mouseMoved(x, y, modifiers);
        if ((modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
            polygon.addPoint(x, y);
            toolInterface.repaintImage();
        }
        x1 = 100;
        y1 = 100;
        x2 = 200;
        y2 = 200;
    }

    @Override
    public Cursor getCursor() {
        if (cursor == null) {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Point hotSpot = new Point(4, 23);
            cursor = toolkit.createCustomCursor(cursorImage, hotSpot, "Pencil");
        }

        return cursor;
    }

    @Override
    public String getName() {
        return Language.translate("Pen");
    }

    @Override
    public void mouseReleased(int x, int y, int modifiers) {
        super.mouseReleased(x, y, modifiers);
        ActivityLog.log("toolResult", "Draw with a pen in " + toolInterface.getName(), "pencil_annotate.png", toolInterface.getPanel());
    }
}
