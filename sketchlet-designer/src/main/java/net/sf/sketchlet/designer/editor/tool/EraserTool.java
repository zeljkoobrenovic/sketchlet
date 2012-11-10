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

/**
 * @author zobrenovic
 */
public class EraserTool extends PenTool2 {

    Cursor cursor;

    public EraserTool(ToolInterface toolInterface) {
        super(toolInterface, new String[]{"stroke width"});
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Point hotSpot = new Point(7, 28);
        Image cursorImage = Workspace.createImageIcon("resources/cursor_eraser.gif").getImage();
        cursor = toolkit.createCustomCursor(cursorImage, hotSpot, "Eraser");
    }

    public void setStroke() {
        toolInterface.getImageGraphics().setStroke(new BasicStroke(toolInterface.getStrokeWidth() * 4));
        toolInterface.getImageGraphics().setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
    }

    public Cursor getCursor() {
        return cursor;
    }

    int mouseX;
    int mouseY;

    public void mouseMoved(int x, int y, int modifiers) {
        mouseX = x;
        mouseY = y;
        toolInterface.repaintImage();
    }

    public void mouseDragged(int x, int y, int modifiers) {
        this.mouseMoved(x, y, modifiers);
        super.mouseDragged(x, y, modifiers);
    }

    public void draw(Graphics2D g2) {
        int w = toolInterface.getStrokeWidth() * 4;
        Color c = g2.getColor();
        g2.setColor(Color.GRAY);
        g2.drawRect(mouseX - w / 2, mouseY - w / 2, w, w);
        g2.setColor(c);
    }

    public ImageIcon getIcon() {
        return Workspace.createImageIcon("resources/eraser.png");
    }

    public String getIconFileName() {
        return "eraser.png";
    }

    public String getName() {
        return Language.translate("Eraser");
    }

    public void mouseReleased(int x, int y, int modifiers) {
        ActivityLog.log("toolResult", "Erase a part of the image in " + toolInterface.getName(), "eraser.png", toolInterface.getPanel());
        TutorialPanel.addLine("cmd", "Erase a part of the image by dragging a mouse in " + toolInterface.getName(), "", toolInterface.getPanel());
    }
}
