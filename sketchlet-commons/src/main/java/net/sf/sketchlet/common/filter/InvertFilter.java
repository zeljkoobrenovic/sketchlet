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
public class InvertFilter implements ImageFilter {

    public String getName() {
        return "Invert Filter";
    }

    public BufferedImage processImage(BufferedImage image) {
        byte[] invertArray = new byte[256];

        for (int counter = 0; counter < 256; counter++) {
            invertArray[counter] = (byte) (255 - counter);
        }

        BufferedImageOp invertFilter = new LookupOp(new ByteLookupTable(0, invertArray), null);
        return invertFilter.filter(image, null);

    }
}