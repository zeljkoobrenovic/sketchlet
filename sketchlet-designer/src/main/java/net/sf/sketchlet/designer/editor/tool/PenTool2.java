/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.tool;

import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.tools.log.ActivityLog;
import net.sf.sketchlet.designer.ui.playback.PlaybackFrame;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.geom.Line2D;

/**
 * @author zobrenovic
 */
public class PenTool2 extends Tool {

    Point start;

    public PenTool2(ToolInterface toolInterface) {
        super(toolInterface);
    }

    public PenTool2(ToolInterface toolInterface, String settings[]) {
        super(toolInterface, settings);
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
        ActivityLog.log("toolResult", "Draw with a pen in " + toolInterface.getName(), "pencil_annotate.png", toolInterface.getPanel());
        TutorialPanel.addLine("cmd", "Draw with a pen by dragging in " + toolInterface.getName(), "", toolInterface.getPanel());
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

        if (SketchletEditor.editorPanel != null && PlaybackFrame.playbackFrame != null) {
            for (int i = 0; i < PlaybackFrame.playbackFrame.length; i++) {
                Graphics2D g2 = PlaybackFrame.playbackFrame[i].playbackPanel.currentPage.images[SketchletEditor.editorPanel.layer].createGraphics();
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
