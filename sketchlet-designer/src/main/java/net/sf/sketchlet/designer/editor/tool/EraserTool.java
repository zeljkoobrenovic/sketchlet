package net.sf.sketchlet.designer.editor.tool;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.framework.model.log.ActivityLog;

import javax.swing.*;
import java.awt.*;

/**
 * @author zobrenovic
 */
public class EraserTool extends PointPenTool {
    private Cursor cursor;
    private int mouseX;
    private int mouseY;

    public EraserTool(ToolInterface toolInterface) {
        super(toolInterface, new String[]{"stroke width"});
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Point hotSpot = new Point(7, 28);
        Image cursorImage = Workspace.createImageIcon("resources/cursor_eraser.gif").getImage();
        cursor = toolkit.createCustomCursor(cursorImage, hotSpot, "Eraser");
    }

    @Override
    public void setStroke() {
        toolInterface.getImageGraphics().setStroke(new BasicStroke(toolInterface.getStrokeWidth() * 4));
        toolInterface.getImageGraphics().setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
    }

    @Override
    public Cursor getCursor() {
        return cursor;
    }

    @Override
    public void mouseMoved(int x, int y, int modifiers) {
        mouseX = x;
        mouseY = y;
        toolInterface.repaintImage();
    }

    @Override
    public void mouseDragged(int x, int y, int modifiers) {
        this.mouseMoved(x, y, modifiers);
        super.mouseDragged(x, y, modifiers);
    }

    @Override
    public void draw(Graphics2D g2) {
        int w = toolInterface.getStrokeWidth() * 4;
        Color c = g2.getColor();
        g2.setColor(Color.GRAY);
        g2.drawRect(mouseX - w / 2, mouseY - w / 2, w, w);
        g2.setColor(c);
    }

    @Override
    public ImageIcon getIcon() {
        return Workspace.createImageIcon("resources/eraser.png");
    }

    @Override
    public String getIconFileName() {
        return "eraser.png";
    }

    @Override
    public String getName() {
        return Language.translate("Eraser");
    }

    @Override
    public void mouseReleased(int x, int y, int modifiers) {
        ActivityLog.log("toolResult", "Erase a part of the image in " + toolInterface.getName(), "eraser.png", toolInterface.getPanel());
    }
}
