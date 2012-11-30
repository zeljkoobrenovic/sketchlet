package net.sf.sketchlet.designer.editor.tool;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.framework.model.log.ActivityLog;

import javax.swing.*;
import java.awt.*;

/**
 * @author zobrenovic
 */
public class LineTool extends ShapeTool {

    public LineTool(ToolInterface toolInterface) {
        super(toolInterface, new String[]{"stroke type", "stroke width", "anitaliasing", "fill patterns"});
    }

    @Override
    public void draw(Graphics2D g2, int x, int y, int w, int h) {
        g2.drawLine(x1, y1, x2, y2);
    }

    @Override
    public ImageIcon getIcon() {
        return Workspace.createImageIcon("resources/line_1.png");
    }

    @Override
    public String getIconFileName() {
        return "line_1.png";
    }

    @Override
    public String getName() {
        return Language.translate("Line");
    }

    @Override
    public boolean checkDimensions() {
        return true;
    }

    @Override
    public void mouseReleased(int x, int y, int modifiers) {
        super.mouseReleased(x, y, modifiers);
        ActivityLog.log("toolResult", "Draw a line in " + toolInterface.getName(), "line_1.png", toolInterface.getPanel());
    }
}
