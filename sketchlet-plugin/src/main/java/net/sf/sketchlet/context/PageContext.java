/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.context;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * This class provides an interface towards Sketchlet Designer pages. Pages in
 * Sketchlet Designer consist of background images and zero or more active regions.
 * This is an abstract class, and you can get instances of its implementation
 * using <tt>SketchletContext.getCurrentPage()</tt>, or <tt>SketchletContext.getPages()</tt>.
 *
 * @author Zeljko Obrenovic
 */
public abstract class PageContext {

    /**
     * Returns the Sketchlet Context to which this page belongs. It is the same
     * as SketcheltContext.getContext().
     *
     * @return the sketchlet context
     * @see SketchletContext
     */
    public abstract SketchletContext getSketchletContext();

    /**
     * Returns the title of this page.
     *
     * @return the title of the page
     */
    public abstract String getTitle();

    /**
     * Returns the list of active regions in the page.
     *
     * @return the list of active regions; if there are no active regions in the page, it returns the lsit with size 0
     */
    public abstract List<ActiveRegionContext> getActiveRegions();

    /**
     * Returns an active region with a given name.
     *
     * @param name the name of the region
     * @return the active region, or <tt>null</tt> if the region with the given name does not exist
     */
    public abstract ActiveRegionContext getActiveRegion(String name);

    /**
     * Returns an active region with a given number. Each region is given a number
     * that reflects it depth on page. This number is NOT a region identifier, and
     * it may change if you delete regions or move the region to front or back.
     * If you want a more reliable way of identifying the region, use <tt>getActiveRegion(String name)</tt>
     *
     * @param number the name of the region
     * @return the active region, or <tt>null</tt> if the region with the given number does not exist
     */
    public abstract ActiveRegionContext getActiveRegion(int number);

    /**
     * Returns the value of the page property with a given name.
     *
     * @param name the name of the property
     * @return the value of the property or <tt>null</tt> if the property with the given name does not exist
     */
    public abstract String getProperty(String name);

    /**
     * Sets the value of the page property with a given name. If the property with
     * the given name does not exist, nothing happens.
     *
     * @param name  the name of the property
     * @param value the value of the property
     */
    public abstract void setProperty(String name, String value);

    /**
     * Returns the list with names of all page properties.
     *
     * @return the list of page properties
     */
    public abstract List<String> getPropertyNames();

    /**
     * Return the page background color.
     *
     * @return the background color of the page
     */
    public abstract Color getBackgroundColor();

    /**
     * Returns the image of the page within the layer with a given number. Numbers
     *
     * @param layer a zero indexed layer number
     * @return the image in the given layer, or <tt>null</tt> if the image layer
     *         is parameters is out of bounds; returns <tt>null</tt> also if the image is empty
     */
    public abstract BufferedImage getImage(int layer);

    /**
     * Returns the number of background image layers in the page.
     *
     * @return the number of image layers
     */
    public abstract int getImageLayersCount();

    /**
     * Returns the page width in pixels.
     *
     * @return the page width in pixels
     */
    public abstract int getWidth();

    /**
     * Returns the page height in pixels.
     *
     * @return the page height in pixels
     */
    public abstract int getHeight();

    public abstract int getPageVariableCount();

    public abstract List<String> getPageVariableNames();

    public abstract void addPageVariable(String name, String value);

    public abstract void setPageVariableValue(String name, String value);

    public abstract String getPageVariableValue(String name);

    public abstract void deletePageVariable(String name);

    public abstract String getPageVariableFormat(String name);

    public abstract void setPageVariableFormat(String name, String value);
}
