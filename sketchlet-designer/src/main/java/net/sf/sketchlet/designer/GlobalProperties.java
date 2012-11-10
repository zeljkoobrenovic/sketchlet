/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.pluginloader.CodeGenPluginFactory;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * @author zobrenovic
 */
public class GlobalProperties {
    private static final Logger log = Logger.getLogger(GlobalProperties.class);

    public static Properties properties;

    public static void load() {
        properties = new Properties();
        try {
            properties.loadFromXML(new FileInputStream(new File(SketchletContextUtils.getApplicationSettingsDir() + "parameters.xml")));
            String strLangFile = GlobalProperties.get("gui-lang-file");
            if (strLangFile != null && !strLangFile.isEmpty()) {
                Language.loadTranslation(SketchletContextUtils.getSketchletDesignerConfDir() + "lang/" + strLangFile.trim());
            }
            String strPlatform = GlobalProperties.get("platform");
            if (strPlatform != null && !strPlatform.isEmpty()) {
                CodeGenPluginFactory.platform = strPlatform;
            }
        } catch (Exception e) {
        }
    }

    public static void getProperty(String strProperty) {
    }

    public static void set(String key, String value) {
        properties.setProperty(key, value);
    }

    public static void setAndSave(String key, String value) {
        set(key, value);
        save();
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        if (properties == null || properties.size() == 0) {
            return defaultValue;
        }
        return properties.getProperty(key, defaultValue);
    }

    public static int get(String key, int defaultValue) {
        if (properties == null || properties.size() == 0) {
            return defaultValue;
        }
        return (int) get(key, (double) defaultValue);
    }

    public static double get(String key, double defaultValue) {
        if (properties == null || properties.size() == 0) {
            return defaultValue;
        }
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
            properties.storeToXML(new FileOutputStream(new File(SketchletContextUtils.getApplicationSettingsDir() + "parameters.xml")), "sketchlet");
        } catch (Exception e) {
        }
    }
}
