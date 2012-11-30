package net.sf.sketchlet.designer.editor.tool;

import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.framework.model.log.ActivityLog;
import net.sf.sketchlet.designer.playback.ui.PlaybackFrame;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.geom.Line2D;

/**
 * @author zobrenovic
 */
public class PointPenTool extends Tool {
    private Point start;
    private int mouseX, mouseY;

    private Image cursorImage = Workspace.createImageIcon("resources/cursor_pencil_annotate.gif").getImage();
    private Cursor cursor;

    public PointPenTool(ToolInterface toolInterface, String settings[]) {
        super(toolInterface, settings);
    }

    @Override
    public void mouseMoved(int x, int y, int modifiers) {
        mouseX = x;
        mouseY = y;
        toolInterface.repaintImage();
    }

    @Override
    public void mousePressed(int x, int y, int modifiers) {
        toolInterface.saveImageUndo();
        toolInterface.createGraphics();
        start = new Point(x, y);
    }

    protected void setStroke() {
        toolInterface.getImageGraphics().setPaint(toolInterface.getColor());
        toolInterface.getImageGraphics().setStroke(toolInterface.getStroke());
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
    public void mouseReleased(int x, int y, int modifiers) {
        ActivityLog.log("toolResult", "Draw with a pen in " + toolInterface.getName(), "pencil_annotate.png", toolInterface.getPanel());
    }

    @Override
    public void mouseDragged(int x, int y, int modifiers) {
        this.mouseMoved(x, y, modifiers);
        if ((modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
            Point p = new Point(x, y);
            draw(start, p);
            start = p;
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        super.draw(g2);
        int w = toolInterface.getStrokeWidth() + 2;
        Color c = g2.getColor();
        g2.setColor(Color.GRAY);
        g2.drawOval(mouseX - w / 2, mouseY - w / 2, w, w);
        g2.setColor(c);
    }

    protected void draw(Point start, Point end) {
        setStroke();
        toolInterface.getImageGraphics().setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, toolInterface.getWatering()));
        toolInterface.getImageGraphics().draw(new Line2D.Double(start, end));
        toolInterface.setImageUpdated(true);

        if (SketchletEditor.getInstance() != null && PlaybackFrame.playbackFrame != null) {
            for (int i = 0; i < PlaybackFrame.playbackFrame.length; i++) {
                Graphics2D g2 = PlaybackFrame.playbackFrame[i].playbackPanel.getCurrentPage().getImages()[SketchletEditor.getInstance().getLayer()].createGraphics();
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
