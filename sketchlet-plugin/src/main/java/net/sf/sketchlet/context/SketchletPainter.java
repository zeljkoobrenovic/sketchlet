/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.sketchlet.context;

import java.awt.Container;
import java.awt.Graphics2D;

/**
 *
 * @author zobrenovic
 */
public interface SketchletPainter {
    public void paintGraphics(Graphics2D g);
    public boolean isPainting();
    public int getPaintWidth();
    public int getPaintHeigth();
    public Container getContainer();
}
