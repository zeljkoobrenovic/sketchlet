/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.tool;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.tools.log.ActivityLog;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author zobrenovic
 */
public class TransparentColorTool extends Tool {
    private static final Logger log = Logger.getLogger(TransparentColorTool.class);

    public TransparentColorTool(ToolInterface toolInterface) {
        super(toolInterface);
    }

    public void mouseMoved(int x, int y, int modifiers) {
    }

    public void mousePressed(final int x, final int y, int modifiers) {
        new Thread(new Runnable() {

            public void run() {
                toolInterface.saveImageUndo();

                try {
                    makeTransparent(x, y);
                    toolInterface.setImageUpdated(true);
                    toolInterface.repaintImage();
                    deactivate();
                } catch (Throwable e) {
                    log.error(e);
                }
            }
        }).start();
    }

    Image cursorImage = Workspace.createImageIcon("resources/cursor_transparent_color.gif").getImage();

    public Cursor getCursor() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Point hotSpot = new Point(2, 22);

        return toolkit.createCustomCursor(cursorImage, hotSpot, "Transparent Color");
    }

    public void mouseDragged(int x, int y, int modifiers) {
    }

    public void draw(Point start, Point end) {
    }

    public ImageIcon getIcon() {
        return Workspace.createImageIcon("resources/image_transparent_color.png");
    }

    public String getIconFileName() {
        return "image_transparent_color.png";
    }

    public String getName() {
        return Language.translate("Select Transparent Color");
    }

    public void makeTransparent(int x, int y) {
        BufferedImage img = toolInterface.getImage();
        if (img != null) {
            int color = img.getRGB(x, y);
            for (int i = 0; i < img.getWidth(); i++) {
                for (int j = 0; j < img.getHeight(); j++) {
                    if (img.getRGB(i, j) == color) {
                        img.setRGB(i, j, new Color(0, 0, 0, 0).getRGB());
                    }
                }
            }
        }
    }

    public void mouseReleased(int x, int y, int modifiers) {
        super.mouseReleased(x, y, modifiers);
        ActivityLog.log("toolResult", "Set transparent color in " + toolInterface.getName(), "image_transparent_color.png", toolInterface.getPanel());
        TutorialPanel.addLine("cmd", "Set transparent color in " + toolInterface.getName(), "", toolInterface.getPanel());
    }
}
