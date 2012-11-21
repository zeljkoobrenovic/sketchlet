/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.loaders.pluginloader;

import java.io.File;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class ScriptPluginFactory {

    private static Vector<SketchletPluginHandler> scriptPlugins = new Vector<SketchletPluginHandler>();

    public static PluginInstance getScriptPluginInstance(File scriptFile) {
        String strPath = scriptFile.getPath().toLowerCase();

        int n = strPath.lastIndexOf(".");
        if (n > 0 && n < strPath.length() - 1) {
            String strExtension = strPath.substring(n + 1);
            for (SketchletPluginHandler _plugin : getScriptPlugins()) {
                if (_plugin instanceof ScriptPlugin) {
                    ScriptPlugin plugin = (ScriptPlugin) _plugin;
                    String strParam = plugin.getParam("extension");
                    if (strParam != null && strParam.equalsIgnoreCase(strExtension)) {
                        ClassLoader cl = Thread.currentThread().getContextClassLoader();
                        Thread.currentThread().setContextClassLoader(PluginLoader.getClassLoader());
                        PluginInstance sc = plugin.getInstance(File.class, scriptFile);
                        Thread.currentThread().setContextClassLoader(cl);
                        return sc;
                    }
                }
            }
        }

        return null;
    }

    public static Vector<String> getScriptExtensions() {
        Vector<String> extensions = new Vector<String>();
        for (SketchletPluginHandler _plugin : getScriptPlugins()) {
            if (_plugin instanceof ScriptPlugin) {
                ScriptPlugin plugin = (ScriptPlugin) _plugin;
                String strParam = plugin.getParam("extension");
                if (strParam != null) {
                    extensions.add(strParam);
                }
            }
        }
        return extensions;
    }

    public static String[] getScriptTitles() {
        Vector<String[]> pluginInfos = new Vector<String[]>();
        for (SketchletPluginHandler _plugin : getScriptPlugins()) {
            if (_plugin instanceof ScriptPlugin) {
                ScriptPlugin plugin = (ScriptPlugin) _plugin;
                String strParam = plugin.getParam("extension");
                if (strParam != null) {
                    pluginInfos.add(new String[]{plugin.getName(), strParam});
                }
            }
        }
        String titles[] = new String[pluginInfos.size()];

        for (int i = 0; i < titles.length; i++) {
            String info[] = pluginInfos.elementAt(i);
            titles[i] = info[0] + " (." + info[1] + ")";
        }

        return titles;
    }

    public static String[] getScriptDefaultFiles() {
        Vector<String> extensions = getScriptExtensions();
        String defaultFiles[] = new String[extensions.size()];

        for (int i = 0; i < defaultFiles.length; i++) {
            defaultFiles[i] = "script." + extensions.elementAt(i);
        }

        return defaultFiles;
    }

    public static void sort() {
    }

    public static Vector<SketchletPluginHandler> getScriptPlugins() {
        return scriptPlugins;
    }
}
