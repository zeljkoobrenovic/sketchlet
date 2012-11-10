/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.common.filter;

import java.awt.image.BufferedImage;

/**
 *
 * @author zobrenovic
 */
public class ColorBlindDeuteranopeFilter extends ColorBlindFilter {
    public String getName() {
        return "Disability / Color / Deuteranope";
    }
    public BufferedImage processImage(BufferedImage image) {
        return brettel(image, 'd', gammaRGB);
    }
}
