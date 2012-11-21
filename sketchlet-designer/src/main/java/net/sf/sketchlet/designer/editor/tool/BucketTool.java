/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.tool;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.tools.log.ActivityLog;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * @author zobrenovic
 */
public class BucketTool extends Tool {
    private static final Logger log = Logger.getLogger(BucketTool.class);
    private Image cursorImage = Workspace.createImageIcon("resources/cursor_bucket.gif").getImage();

    public BucketTool(ToolInterface toolInterface) {
        super(toolInterface);
    }

    @Override
    public void mouseReleased(int x, int y, int modifiers) {
        ActivityLog.log("toolResult", "Fill the color with the bucket in " + toolInterface.getName(), "bucket.gif", toolInterface.getPanel());
    }

    @Override
    public void mousePressed(final int x, final int y, int modifiers) {
        new Thread(new Runnable() {

            public void run() {
                toolInterface.saveImageUndo();

                try {
                    float transparency = toolInterface.getWatering();
                    Color c = toolInterface.getColor();
                    c = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (transparency * 255));
                    floodFill(toolInterface.getImage(), c, new Point(x, y));
                    toolInterface.setImageUpdated(true);
                    toolInterface.repaintImage();
                    deactivate();
                } catch (Throwable e) {
                    log.error(e);
                }
            }
        }).start();
    }

    @Override
    public Cursor getCursor() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Point hotSpot = new Point(2, 22);

        return toolkit.createCustomCursor(cursorImage, hotSpot, "Bucket");
    }

    @Override
    public ImageIcon getIcon() {
        return Workspace.createImageIcon("resources/bucket.gif");
    }

    @Override
    public String getIconFileName() {
        return "bucket.gif";
    }

    @Override
    public String getName() {
        return Language.translate("Bucket");
    }

    private static void floodFill(BufferedImage img, Color fillColor, Point loc) {
        if (loc.x < 0 || loc.x >= img.getWidth() || loc.y < 0 || loc.y >= img.getHeight()) {
            throw new IllegalArgumentException();
        }

        WritableRaster raster = img.getRaster();
        int[] fill = new int[]{fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), fillColor.getAlpha()};
        int[] old = raster.getPixel(loc.x, loc.y, new int[4]);

        // Checks trivial case where loc is of the fill color
        if (isEqualRgba(fill, old)) {
            return;
        }

        floodLoop(raster, loc.x, loc.y, fill, old);
    }

    // Recursively fills surrounding pixels of the old color
    private static void floodLoop(WritableRaster raster, int x, int y, int[] fill, int[] old) {
        Rectangle bounds = raster.getBounds();
        int[] aux = {255, 255, 255, 255};

        // finds the left side, filling along the way
        int fillL = x;
        do {
            raster.setPixel(fillL, y, fill);
            fillL--;
        } while (fillL >= 0 && isEqualRgba(raster.getPixel(fillL, y, aux), old));
        fillL++;

        // find the right right side, filling along the way
        int fillR = x;
        do {
            raster.setPixel(fillR, y, fill);
            fillR++;
        } while (fillR < bounds.width - 1 && isEqualRgba(raster.getPixel(fillR, y, aux), old));
        fillR--;

        // checks if applicable up or down
        for (int i = fillL; i <= fillR; i++) {
            if (y > 0 && isEqualRgba(raster.getPixel(i, y - 1, aux), old)) {
                floodLoop(raster, i, y - 1, fill, old);
            }
            if (y < bounds.height - 1 && isEqualRgba(raster.getPixel(i, y + 1, aux), old)) {
                floodLoop(raster, i, y + 1, fill, old);
            }
        }
    }

    /**
     * Returns true if RGBA arrays are equivalent, false otherwise
     * Could use Arrays.equals(int[], int[]), but this is probably a little faster...
     */
    private static boolean isEqualRgba2(int[] pix1, int[] pix2) {
        return pix1[0] == pix2[0] && pix1[1] == pix2[1] && pix1[2] == pix2[2] && pix1[3] == pix2[3];
    }

    private static boolean isEqualRgba(int[] pix1, int[] pix2) {
        return Math.abs(pix1[0] - pix2[0]) == 0 && Math.abs(pix1[1] - pix2[1]) == 0 && Math.abs(pix1[2] - pix2[2]) == 0 && Math.abs(pix1[3] - pix2[3]) == 0;
    }

    private static boolean isEqualRgba3(int[] pix1, int[] pix2) {
        float hsv1[] = Color.RGBtoHSB(pix1[0], pix1[1], pix1[2], null);
        float hsv2[] = Color.RGBtoHSB(pix2[0], pix2[1], pix2[2], null);

        return Math.abs(hsv1[0] - hsv2[0]) < 0.02 && Math.abs(hsv1[1] - hsv2[1]) < 0.5 && Math.abs(hsv1[2] - hsv2[2]) < 0.5;
    }
}
