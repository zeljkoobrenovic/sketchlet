package net.sf.sketchlet.designer;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.loaders.pluginloader.CodeGenPluginFactory;
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

    private static Properties properties;

    public static void load() {
        properties = new Properties();
        try {
            getProperties().loadFromXML(new FileInputStream(new File(SketchletContextUtils.getApplicationSettingsDir() + "parameters.xml")));
            String strLangFile = GlobalProperties.get("gui-lang-file");
            if (strLangFile != null && !strLangFile.isEmpty()) {
                Language.loadTranslation(SketchletContextUtils.getSketchletDesignerConfDir() + "lang/" + strLangFile.trim());
            }
            String strPlatform = GlobalProperties.get("platform");
            if (strPlatform != null && !strPlatform.isEmpty()) {
                CodeGenPluginFactory.setPlatform(strPlatform);
            }
        } catch (Exception e) {
        }
    }

    public static void getProperty(String strProperty) {
    }

    public static void set(String key, String value) {
        getProperties().setProperty(key, value);
    }

    public static void setAndSave(String key, String value) {
        set(key, value);
        save();
    }

    public static String get(String key) {
        return getProperties().getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        if (getProperties() == null || getProperties().size() == 0) {
            return defaultValue;
        }
        return getProperties().getProperty(key, defaultValue);
    }

    public static int get(String key, int defaultValue) {
        if (getProperties() == null || getProperties().size() == 0) {
            return defaultValue;
        }
        return (int) get(key, (double) defaultValue);
    }

    public static double get(String key, double defaultValue) {
        if (getProperties() == null || getProperties().size() == 0) {
            return defaultValue;
        }
        String str = get(key);
        if (str != null) {
            try {
                return Double.parseDouble(str);
            } catch (Exception e) {
                log.error("global properties error", e);
            }
        }
        return defaultValue;
    }

    public static void save() {
        try {
            getProperties().storeToXML(new FileOutputStream(new File(SketchletContextUtils.getApplicationSettingsDir() + "parameters.xml")), "sketchlet");
        } catch (Exception e) {
        }
    }

    public static Properties getProperties() {
        return properties;
    }
}
