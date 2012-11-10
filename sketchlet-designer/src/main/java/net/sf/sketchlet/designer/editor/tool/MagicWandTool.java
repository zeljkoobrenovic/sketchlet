/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.tool;

/**
 *
 * @author zobrenovic
 */
// MagicWandTool.java
/**
 * Demonstrates how to extract a shape from a BufferedImage.
 **/

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.tools.log.ActivityLog;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Vector;

public class MagicWandTool extends Tool {
    private static final Logger log = Logger.getLogger(MagicWandTool.class);

    public MagicWandTool(ToolInterface toolInterface) {
        super(toolInterface);
    }

    public void draw(Graphics2D g2) {
        g2.setColor(Color.BLACK);

        if (image != null) {
            g2.drawImage(image, 0, 0, null);
        }
    }

    public void mouseReleased(int x, int y, int modifiers) {
        super.mouseReleased(x, y, modifiers);
        ActivityLog.log("toolResult", "Draw a line in " + toolInterface.getName(), "line_1.png", toolInterface.getPanel());
        TutorialPanel.addLine("cmd", "Select a region by freeform dragging in " + toolInterface.getName(), "", toolInterface.getPanel());
    }

    public void mousePressed(int x, int y, int modifiers) {
        Polygon polygon = new Polygon();
        Vector<int[]> pixels = new Vector<int[]>();
        BufferedImage image;
        polygon = new Polygon();
        pixels = new Vector<int[]>();
        toolInterface.saveImageUndo();
        try {
            if (!this.floodFill(polygon, pixels, toolInterface.getImage(), toolInterface.getColor(), new Point(x, y))) {
                return;
            }
            toolInterface.setImageUpdated(true);
            toolInterface.repaintImage();
        } catch (Throwable e) {
            return;
        }

        int x1 = 5000;
        int x2 = 0;
        int y1 = 5000;
        int y2 = 0;

        for (int i = 0; i < polygon.npoints; i++) {
            x1 = Math.min(polygon.xpoints[i], x1);
            x2 = Math.max(polygon.xpoints[i], x2);
            y1 = Math.min(polygon.ypoints[i], y1);
            y2 = Math.max(polygon.ypoints[i], y2);
        }

        int w = x2 - x1 + 1;
        int h = y2 - y1 + 1;

        image = Workspace.createCompatibleImage(w, h);
        Graphics2D g2 = image.createGraphics();

        int i = 0;
        try {
            for (i = 0; i < polygon.npoints; i++) {
                int[] pixel = pixels.elementAt(i);
                g2.setColor(new Color(pixel[0], pixel[1], pixel[2], pixel[3]));
                g2.fillRect(polygon.xpoints[i] - x1, polygon.ypoints[i] - y1, 1, 1);
            }
        } catch (Exception e) {
            log.error(x1 + " " + y1 + " " + w + " " + " " + h + " " + (polygon.xpoints[i] - x1) + " " + (polygon.ypoints[i] - y1), e);
        }

        g2.dispose();
        super.mousePressed(x, y, modifiers);

        toolInterface.setTool(toolInterface.getSelectTool(), null);
        toolInterface.getSelectTool().setClip(image, x1, y1);
    }

    Image cursorImage = Workspace.createImageIcon("resources/cursor_magicwand.gif").getImage();

    public Cursor getCursor() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Point hotSpot = new Point(9, 9);

        return toolkit.createCustomCursor(cursorImage, hotSpot, "Magic Wand");
    }

    public static boolean floodFill(Polygon polygon, Vector<int[]> pixels, BufferedImage img, Color fillColor, Point loc) {
        if (loc.x < 0 || loc.x >= img.getWidth() || loc.y < 0 || loc.y >= img.getHeight()) {
            throw new IllegalArgumentException();
        }

        WritableRaster raster = img.getRaster();
        int[] fill = new int[]{0, 0, 0, 0};
        int[] old = raster.getPixel(loc.x, loc.y, new int[4]);

        // Checks trivial case where loc is of the fill color
        if (isEqualRgba2(fill, old)) {
            return false;
        }
        // do not do it for the transparent color
        if (old[0] == 0 && old[1] == 0 && old[2] == 0 && old[3] == 0) {
            return false;
        }

        floodLoop(polygon, pixels, raster, loc.x, loc.y, fill, old);

        return true;
    }

    // Recursively fills surrounding pixels of the old color
    private static void floodLoop(Polygon polygon, Vector<int[]> pixels, WritableRaster raster, int x, int y, int[] fill, int[] old) {
        Rectangle bounds = raster.getBounds();
        int[] aux = {255, 255, 255, 255};

        // finds the left side, filling along the way
        int fillL = x;
        do {
            int[] pixel = {255, 255, 255, 255};
            pixel = raster.getPixel(fillL, y, pixel);
            polygon.addPoint(fillL, y);
            pixels.add(pixel);

            raster.setPixel(fillL, y, fill);
            fillL--;
        } while (fillL >= 0 && isEqualRgba(raster.getPixel(fillL, y, aux), old));
        fillL++;

        // find the right right side, filling along the way
        int fillR = x;
        do {
            int[] pixel = {255, 255, 255, 255};
            pixel = raster.getPixel(fillR, y, pixel);
            polygon.addPoint(fillR, y);
            pixels.add(pixel);

            raster.setPixel(fillR, y, fill);
            fillR++;
        } while (fillR < bounds.width - 1 && isEqualRgba(raster.getPixel(fillR, y, aux), old));
        fillR--;

        // checks if applicable up or down
        for (int i = fillL; i <= fillR; i++) {
            if (y > 0 && isEqualRgba(raster.getPixel(i, y - 1, aux), old)) {
                floodLoop(polygon, pixels, raster, i, y - 1, fill, old);
            }
            if (y < bounds.height - 1 && isEqualRgba(raster.getPixel(i, y + 1, aux), old)) {
                floodLoop(polygon, pixels, raster, i, y + 1, fill, old);
            }
        }
    }

    private static boolean isEqualRgba2(int[] pix1, int[] pix2) {
        return pix1[0] == pix2[0] && pix1[1] == pix2[1] && pix1[2] == pix2[2] && pix1[3] == pix2[3];
    }

    private static boolean isEqualRgba(int[] pix1, int[] pix2) {
        return Math.abs(pix1[0] - pix2[0]) < 25 && Math.abs(pix1[1] - pix2[1]) < 25 && Math.abs(pix1[2] - pix2[2]) < 25 && Math.abs(pix1[3] - pix2[3]) < 250;
    }

    public ImageIcon getIcon() {
        return Workspace.createImageIcon("resources/magicwand.gif");
    }

    public String getIconFileName() {
        return "magicwand.gif";
    }

    public String getName() {
        return Language.translate("Magic Wand");
    }
}
