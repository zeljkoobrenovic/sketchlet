/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.regions.renderer;

import net.sf.sketchlet.designer.data.ActiveRegion;

import java.awt.*;

/**
 * @author zobrenovic
 */
public abstract class DrawingLayer {

    protected ActiveRegion region;

    public DrawingLayer(ActiveRegion region) {
        this.region = region;
    }

    public abstract void draw(Graphics2D g2, Component component, boolean bPlayback);
}
