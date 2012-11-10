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
public class ColorPickerTool extends Tool {

    public ColorPickerTool(ToolInterface toolInterface) {
        super(toolInterface);
    }

    public void mouseMoved(int x, int y, int modifiers) {
    }

    public void mousePressed(int x, int y, int modifiers) {
        Color c = new Color(toolInterface.getImage().getRGB(x, y));
        toolInterface.setColor(c);
    }

    Image cursorImage = Workspace.createImageIcon("resources/cursor_color_picker.gif").getImage();
    // Image cursorImageAnnotate = Workspace.createImageIcon("resources/cursor_pencil_annotate.gif").getImage();

    public Cursor getCursor() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Point hotSpot = new Point(4, 23);

        return toolkit.createCustomCursor(cursorImage, hotSpot, "Color Picker");
    }

    public void mouseReleased(int x, int y, int modifiers) {
        ActivityLog.log("toolResult", "Pick the color from the image in " + toolInterface.getName(), "color-picker.png", toolInterface.getPanel());
        TutorialPanel.addLine("cmd", "Pick the color from the image in " + toolInterface.getName(), "", toolInterface.getPanel());
    }

    public void mouseDragged(int x, int y, int modifiers) {
    }

    public void draw(Point start, Point end) {
    }

    public ImageIcon getIcon() {
        return Workspace.createImageIcon("resources/color-picker.png");
    }

    public String getIconFileName() {
        return "color-picker.png";
    }

    public String getName() {
        return Language.translate("Color Picker");
    }
}
