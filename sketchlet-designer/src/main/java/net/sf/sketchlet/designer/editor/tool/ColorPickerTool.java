package net.sf.sketchlet.designer.editor.tool;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.framework.model.log.ActivityLog;

import javax.swing.*;
import java.awt.*;

/**
 * @author zobrenovic
 */
public class ColorPickerTool extends Tool {
    private Image cursorImage = Workspace.createImageIcon("resources/cursor_color_picker.gif").getImage();

    public ColorPickerTool(ToolInterface toolInterface) {
        super(toolInterface);
    }

    @Override
    public void mousePressed(int x, int y, int modifiers) {
        Color c = new Color(toolInterface.getImage().getRGB(x, y));
        toolInterface.setColor(c);
    }

    @Override
    public Cursor getCursor() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Point hotSpot = new Point(4, 23);

        return toolkit.createCustomCursor(cursorImage, hotSpot, "Color Picker");
    }

    @Override
    public void mouseReleased(int x, int y, int modifiers) {
        ActivityLog.log("toolResult", "Pick the color from the image in " + toolInterface.getName(), "color-picker.png", toolInterface.getPanel());
    }

    @Override
    public ImageIcon getIcon() {
        return Workspace.createImageIcon("resources/color-picker.png");
    }

    @Override
    public String getIconFileName() {
        return "color-picker.png";
    }

    @Override
    public String getName() {
        return Language.translate("Color Picker");
    }
}
