/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.sketchlet.common.filter;

import java.awt.image.BufferedImage;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author zobrenovic
 */
public class Filters {
    public static Hashtable<String,ImageFilter> imageFilters = new Hashtable<String,ImageFilter>();
    public static Vector<String> imageFiltersVector = new Vector<String>();

    static {
        // addFilter("Color Filter", new ColorFilter());
        addFilter("Grayscale Filter", new GrayscaleFilter());
        addFilter("Invert Filter", new InvertFilter());
        addFilter("Blur Filter", new BlurFilter());
        addFilter("Sharpen Filter", new SharpenFilter());
        addFilter("Emboss Filter", new EmbossFilter());
        imageFiltersVector.add("- - - -");
        addFilter("Glass Distrosion Filter", new DisplaceFilter());
        addFilter("Under Water Filter", new SwimFilter());
        addFilter("Water Wave Filter", new WaterFilter());
        addFilter("Crystallize Filter", new CrystallizeFilter());
        addFilter("Halftone Filter", new HalftoneFilter());
        addFilter("Pointillize Filter", new PointillizeFilter());
        addFilter("Mirror Filter", new MirrorFilter());
        addFilter("Treshold Filter", new ThresholdFilter());

        // imageFilters.put("Disability / Deuteranope", new ColorBlindDeuteranopeFilter());
    }

    public static void addFilter(String strName, ImageFilter filter) {
        imageFilters.put(strName, filter);
        imageFiltersVector.add(strName);
    }

    public static String[] getFilterNames() {
        String strFilters[] = new String[imageFilters.size()];
        
        int i = 0;
        for (String strName : imageFilters.keySet()) {
            strFilters[i] = strName;
            i++;
        }
        return strFilters;
    }

    public static BufferedImage filter( String strFilter, String strParams, BufferedImage image ) {
        ImageFilter filter = imageFilters.get(strFilter);

        if (filter != null) {
            image = filter.processImage(image);
        }

        return image;
    }
}
