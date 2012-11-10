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
public class OvalTool extends ShapeTool {

    public OvalTool(ToolInterface toolInterface) {
        super(toolInterface);
    }

    public void fill(Graphics2D g2, int x, int y, int w, int h) {
        g2.fillOval(x, y, w, h);
    }

    public void draw(Graphics2D g2, int x, int y, int w, int h) {
        g2.drawOval(x, y, w, h);
    }

    public ImageIcon getIcon() {
        return Workspace.createImageIcon("resources/oval.png");
    }

    public String getIconFileName() {
        return "oval.png";
    }

    public String getName() {
        return Language.translate("Oval");
    }

    public void mouseReleased(int x, int y, int modifiers) {
        super.mouseReleased(x, y, modifiers);
        ActivityLog.log("toolResult", "Draw an oval in " + toolInterface.getName(), "oval.png", toolInterface.getPanel());
        TutorialPanel.addLine("cmd", "Draw an oval by dragging in " + toolInterface.getName(), "", toolInterface.getPanel());
    }
}
