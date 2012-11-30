package net.sf.sketchlet.designer;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * @author zobrenovic
 */
public class SketchletDesignerProperties {
    private static final Logger log = Logger.getLogger(SketchletDesignerProperties.class);
    private static Properties properties;

    static {
        properties = new Properties();
        try {
            getProperties().loadFromXML(new FileInputStream(new File(SketchletContextUtils.getSketchletDesignerConfDir() + "parameters.xml")));
        } catch (Exception e) {
        }
    }

    public static void getProperty(String strProperty) {
    }

    public static void set(String key, String value) {
        getProperties().setProperty(key, value);
    }

    public static String get(String key) {
        return getProperties().getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        return getProperties().getProperty(key, defaultValue);
    }

    public static int get(String key, int defaultValue) {
        return (int) get(key, (double) defaultValue);
    }

    public static double get(String key, double defaultValue) {
        String str = get(key);
        if (str != null) {
            try {
                return Double.parseDouble(str);
            } catch (Exception e) {
                log.error(e);
            }
        }
        return defaultValue;
    }

    public static void save() {
        try {
            getProperties().storeToXML(new FileOutputStream(new File(SketchletContextUtils.getSketchletDesignerConfDir() + "parameters.xml")), "sketchlet");
        } catch (Exception e) {
        }
    }

    public static Properties getProperties() {
        return properties;
    }
}
