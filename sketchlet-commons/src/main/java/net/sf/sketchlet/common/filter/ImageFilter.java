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
public interface ImageFilter {
    public abstract String getName();
    public abstract BufferedImage processImage(BufferedImage image);
}
