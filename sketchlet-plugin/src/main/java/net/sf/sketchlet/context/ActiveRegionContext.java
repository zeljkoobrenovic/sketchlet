/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.context;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

/**
 * This class provides an interface towards active regions. 
 * Through this interface you can access region properties region images.
 * This is an abstract class, and you can get instances of its implementation
 * using one of the <tt>PageContext</tt> <tt>getActiveRegion()</tt> methods.
 * 
 * @author Zeljko Obrenovic
 */
public abstract class ActiveRegionContext {

    /**
     * Returns the page context of this region. Each region belongs to only one page.
     * @return the page context of this region
     */
    public abstract PageContext getPageContext();

    /**
     * Returns string encoded value of a region property. If the property with
     * the given name does not exist, empty string "" is returned.
     * 
     * @param name the name of the region property
     * @return the value of the region property
     */
    public abstract String getProperty(String name);

    /**
     * Sets the value of the region property. If the property with
     * the given name does not exist, nothing happens.
     * 
     * @param name the name of the region property
     * @param value the value of the region property
     */
    public abstract void setProperty(String name, String value);

    public abstract String getWidgetType();

    public abstract String getWidgetProperty(String strProperty);

    public abstract void setWidgetProperty(String strProperty, String strValue);

    public abstract String getWidgetItemText();

    public abstract BufferedImage getImage(int index);

    public abstract int getImageCount();

    public abstract int getX1();

    public abstract int getY1();

    public abstract int getX2();

    public abstract int getY2();

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract Stroke getStroke();

    public abstract Color getLineColor();

    public abstract Color getFillColor();

    public abstract Color getTextColor();

    public abstract int getLineThickness();

    public abstract Dimension getSize();

    public abstract Font getFont(float size);

    public abstract String getWidgetPropertiesString(boolean bProcess);

    public abstract String getName();

    public abstract String getNumber();

    public abstract Object[][] getMouseEvents();

    public abstract String getFirstMousePageLink();

    public abstract boolean fitToBox();

    public abstract boolean isAdjusting();

    public abstract void processEvent(String eventId, String... params);
}
