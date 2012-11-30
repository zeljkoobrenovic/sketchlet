/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.context;

import java.awt.Component;

/**
 *
 * @author zobrenovic
 */
public class SketchletPlaybackContext {

    public static SketchletPlaybackContext context;
    public Component currentPanel = null;
    public double scale = 1.0;
    public int marginX = 0;
    public int marginY = 0;

    public static SketchletPlaybackContext getInstance() {
        if (context == null) {
            context = new SketchletPlaybackContext();
        }
        return context;
    }

    public static void setInstance(SketchletPlaybackContext context) {
        SketchletPlaybackContext.context = context;
    }

    public Component getCurrentPanel() {
        return this.currentPanel;
    }

    public void setCurrentPanel(Component panel) {
        this.currentPanel = panel;
    }

    public void setScale(double s) {
        this.scale = s;
    }

    public double getScale() {
        return scale;
    }

    public void setMargin(int mx, int my) {
        this.marginX = mx;
        this.marginY = my;
    }

    public int getRegionX1OnPanel(ActiveRegionContext region) {
        return marginX + (int) (region.getX1() * scale);
    }

    public int getRegionY1OnPanel(ActiveRegionContext region) {
        return marginY + (int) (region.getY1() * scale);
    }

    public int getRegionX2OnPanel(ActiveRegionContext region) {
        return marginX + (int) (region.getX2() * scale);
    }

    public int getRegionY2OnPanel(ActiveRegionContext region) {
        return marginY + (int) (region.getY1() * scale);
    }
}
