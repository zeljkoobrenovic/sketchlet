/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.util;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 *
 * @author zobrenovic
 */
public class ImageUtils {

    public static boolean isImageEmpty(BufferedImage image) {
        if (image != null) {
            WritableRaster raster = image.getRaster();
            int w = raster.getWidth();
            int h = raster.getHeight();
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    double d[] = raster.getPixel(x, y, new double[4]);
                    for (int i = 0; i < d.length; i++) {
                        if (d[i] != 0.0) {
                            raster = null;
                            image.flush();
                            return false;
                        }
                    }
                }
            }
            raster = null;
            image.flush();
        }
        return true;
    }
}
