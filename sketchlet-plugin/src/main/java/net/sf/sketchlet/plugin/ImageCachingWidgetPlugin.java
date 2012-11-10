/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugin;

import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.context.VariablesBlackboardContext;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import net.sf.sketchlet.context.SketchletGraphicsContext;

/**
 *
 * @author zobrenovic
 */
public abstract class ImageCachingWidgetPlugin extends WidgetPlugin {

    private BufferedImage image = null;

    public ImageCachingWidgetPlugin(ActiveRegionContext region) {
        super(region);
    }

    @Override
    public void dispose() {
        if (image != null) {
            image.flush();
            image = null;
        }
    }

    public BufferedImage getCachedImage() {
        return this.image;
    }

    /**
     * This method is called by the Sketchlet Designer renderer when the page needs to repainted.
     * 
     * This default implementation uses auxilary methods paintImage and is isRegionChaned
     * and field image, to cache painted  image and paint cached image unless 
     * data used to create image have changed. You can define the criteria for 
     * image cache change by overiding the isRegionChanged() method.
     * 
     * @param g2 the graphic context
     */
    @Override
    public void paint(Graphics2D g2) {

        if (image == null || this.isRegionChanged()) {
            int w = getActiveRegionContext().getWidth();
            int h = getActiveRegionContext().getHeight();
            image = SketchletGraphicsContext.getInstance().createCompatibleImage(w, h, image);
            if (image != null) {
                this.paintImage(image.createGraphics());
            }
        }

        if (image != null) {
            int x = 0;
            int y = 0;

            g2.drawImage(image, x, y, null);
        }
    }

    protected abstract void paintImage(Graphics2D g2);

    protected boolean isRegionChanged() {
        if (bForceChange) {
            bForceChange = false;
            return true;
        }
        boolean changed = false;

        if (!prevLineStyle.equals(getActiveRegionContext().getProperty("line style"))) {
            prevLineStyle = getActiveRegionContext().getProperty("line style");
            changed = true;
        } else if (!prevLineThickness.equals(getActiveRegionContext().getProperty("line thickness"))) {
            prevLineThickness = getActiveRegionContext().getProperty("line thickness");
            changed = true;
        } else if (!prevLineColor.equals(getActiveRegionContext().getProperty("line color"))) {
            prevLineColor = getActiveRegionContext().getProperty("line color");
            changed = true;
        } else if (!prevTextColor.equals(getActiveRegionContext().getProperty("text color"))) {
            prevTextColor = getActiveRegionContext().getProperty("text color");
            changed = true;
        } else if (!prevFont.equals(getActiveRegionContext().getProperty("font name"))) {
            prevFont = getActiveRegionContext().getProperty("font name");
            changed = true;
        } else if (!prevFontSize.equals(getActiveRegionContext().getProperty("font size"))) {
            prevFontSize = getActiveRegionContext().getProperty("font size");
            changed = true;
        } else if (!prevFontStyle.equals(getActiveRegionContext().getProperty("font style"))) {
            prevFontStyle = getActiveRegionContext().getProperty("font style");
            changed = true;
        }

        String strItems = getActiveRegionContext().getWidgetItemText();

        if (!prevItems.equals(strItems)) {
            prevItems = strItems;
            changed = true;
        }
        String strProperties = getActiveRegionContext().getWidgetPropertiesString(true);

        if (!prevProperties.equals(strProperties)) {
            prevProperties = strProperties;
            changed = true;
        }

        changed = changed || prevWidth != getActiveRegionContext().getWidth() || prevHeight != getActiveRegionContext().getHeight();

        prevWidth = getActiveRegionContext().getWidth();
        prevHeight = getActiveRegionContext().getHeight();

        return changed;
    }

    protected void updateVariable(String variable, String value) {
        if (!variable.isEmpty() && VariablesBlackboardContext.getInstance() != null) {
            VariablesBlackboardContext.getInstance().updateVariable(variable, value);
        }
    }
    private int prevWidth;
    int prevHeight;
    String prevValue = "";
    String prevItems = "";
    String prevProperties = "";
    String prevLineStyle = "";
    String prevLineThickness = "";
    String prevLineColor = "";
    String prevTextColor = "";
    String prevFont = "";
    String prevFontSize = "";
    String prevFontStyle = "";
    boolean bForceChange = false;

    @Override
    protected void repaint() {
        this.forceChange();
        super.repaint();
    }

    private void forceChange() {
        bForceChange = true;
    }
}
