/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.tool;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.tool.stroke.WobbleStroke;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.tools.log.ActivityLog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;

public class PenTool extends ShapeTool {

    Polygon polygon = new Polygon();

    public PenTool(ToolInterface toolInterface) {
        super(toolInterface, new String[]{"stroke type", "stroke width", "fill patterns"});
    }

    public void fill(Graphics2D g2, int x, int y, int w, int h) {
    }

    public void deactivate() {
        polygon = new Polygon();
        super.deactivate();
    }

    public ImageIcon getIcon() {
        return Workspace.createImageIcon("resources/pencil_annotate.png");
    }

    public String getIconFileName() {
        return "pencil_annotate.png";
    }

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

    public void mousePressed(int x, int y, int modifiers) {
        polygon = new Polygon();
        polygon.addPoint(x, y);
        super.mousePressed(x, y, modifiers);
    }

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

    Image cursorImage = Workspace.createImageIcon("resources/cursor_pencil_annotate.gif").getImage();
    Cursor cursor;

    public Cursor getCursor() {
        if (cursor == null) {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Point hotSpot = new Point(4, 23);
            cursor = toolkit.createCustomCursor(cursorImage, hotSpot, "Pencil");
        }

        return cursor;
    }

    public String getName() {
        return Language.translate("Pen");
    }

    public void mouseReleased(int x, int y, int modifiers) {
        super.mouseReleased(x, y, modifiers);
        ActivityLog.log("toolResult", "Draw with a pen in " + toolInterface.getName(), "pencil_annotate.png", toolInterface.getPanel());
        TutorialPanel.addLine("cmd", "Draw with a pen by dragging in " + toolInterface.getName(), "", toolInterface.getPanel());
    }
}
/**
 *
 * @author zobrenovic
 */
/*public class PenTool extends Tool {

Point start;

public PenTool(ToolInterface toolInterface) {
super(toolInterface);
}
int mouseX;
int mouseY;

public void mouseMoved(int x, int y, int modifiers) {
mouseX = x;
mouseY = y;
toolInterface.repaintImage();
}

public void mousePressed(int x, int y, int modifiers) {
toolInterface.saveImageUndo();
toolInterface.createGraphics();
start = new Point(x, y);
}

public void setStroke() {
toolInterface.getImageGraphics().setPaint(toolInterface.getColor());
toolInterface.getImageGraphics().setStroke(toolInterface.getStroke());
}
Image cursorImage = Workspace.createImageIcon("resources/cursor_pencil_annotate.gif").getImage();
Cursor cursor;
// Image cursorImageAnnotate = Workspace.createImageIcon("resources/cursor_pencil_annotate.gif").getImage();

public Cursor getCursor() {
if (cursor == null) {
Toolkit toolkit = Toolkit.getDefaultToolkit();
Point hotSpot = new Point(4, 23);
cursor = toolkit.createCustomCursor(cursorImage, hotSpot, "Pencil");
}

return cursor;
}

public void mouseReleased(int x, int y, int modifiers) {
}

public void mouseDragged(int x, int y, int modifiers) {
this.mouseMoved(x, y, modifiers);
if ((modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
Point p = new Point(x, y);
draw(start, p);
start = p;
}
}

public void draw(Graphics2D g2) {
super.draw(g2);
int w = toolInterface.getStrokeWidth() + 2;
Color c = g2.getColor();
g2.setColor(Color.GRAY);
g2.drawOval(mouseX - w / 2, mouseY - w / 2, w, w);
g2.setColor(c);
}

public void draw(Point start, Point end) {
setStroke();
toolInterface.getImageGraphics().setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, toolInterface.getWatering()));
toolInterface.getImageGraphics().draw(new Line2D.Double(start, end));
toolInterface.setImageUpdated(true);

if (FreeHand.freeHand != null && PlaybackFrame.playbackFrame != null) {
for (int i = 0; i < PlaybackFrame.playbackFrame.length; i++) {
Graphics2D g2 = PlaybackFrame.playbackFrame[i].playbackPanel.image[FreeHand.freeHand.layer].createGraphics();
g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
g2.setPaint(toolInterface.getColor());
g2.setStroke(toolInterface.getStroke());
g2.draw(new Line2D.Double(start, end));
g2.dispose();
PlaybackFrame.playbackFrame[i].playbackPanel.repaint();
}
}
}
}
 */
