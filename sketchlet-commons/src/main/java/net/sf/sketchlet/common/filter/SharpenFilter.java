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
public class SharpenFilter implements ImageFilter {

    public String getName() {
        return "Sharpen Filter";
    }

    public BufferedImage processImage(BufferedImage image) {
        float[] sharpenMatrix = {0.0f, -1.0f, 0.0f, -1.0f, 5.0f, -1.0f, 0.0f, -1.0f, 0.0f};
        BufferedImageOp sharpenFilter = new ConvolveOp(new Kernel(3, 3, sharpenMatrix),
                ConvolveOp.EDGE_NO_OP, null);
        return sharpenFilter.filter(image, null);
    }
}