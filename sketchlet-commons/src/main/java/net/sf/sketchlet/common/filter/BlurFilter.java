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
public class BlurFilter implements ImageFilter {
    public String getName() {
        return "Blur Filter";
    }

    public BufferedImage processImage(BufferedImage image) {
        float[] blurMatrix = {1.0f / 9.0f, 1.0f / 9.0f, 1.0f / 9.0f, 1.0f / 9.0f, 1.0f / 9.0f,
            1.0f / 9.0f, 1.0f / 9.0f, 1.0f / 9.0f, 1.0f / 9.0f};
        BufferedImageOp blurFilter = new ConvolveOp(new Kernel(3, 3, blurMatrix),
                ConvolveOp.EDGE_NO_OP, null);
        return blurFilter.filter(image, null);
    }
}