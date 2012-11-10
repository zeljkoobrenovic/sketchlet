/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.common.filter;

import java.awt.image.*;

/**
 *
 * @author zobrenovic
 */
public class ColorFilter implements ImageFilter {
    public String getName() {
        return "Color Filter";
    }

    public BufferedImage processImage(BufferedImage image) {
        float[][] colorMatrix = {{1f, 0f, 0f}, {0.5f, 1.0f, 0.5f}, {0.2f, 0.4f, 0.6f}};
        BandCombineOp changeColors = new BandCombineOp(colorMatrix, null);
        Raster sourceRaster = image.getRaster();
        WritableRaster displayRaster = sourceRaster.createCompatibleWritableRaster();
        changeColors.filter(sourceRaster, displayRaster);
        return new BufferedImage(image.getColorModel(), displayRaster, true, null);

    }
}
