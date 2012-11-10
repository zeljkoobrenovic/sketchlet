/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.pluginloader;

import net.sf.sketchlet.plugin.CodeGenPlugin;

import java.util.Vector;

/**
 * @author zobrenovic
 */
public class CodeGenPluginFactory {

    public static Vector<SketchletPluginHandler> codegenPlugins = new Vector<SketchletPluginHandler>();
    public static String platform = "html";

    public static CodeGenPlugin getCodeGenPluginInstance() {
        for (SketchletPluginHandler _plugin : codegenPlugins) {
            if (_plugin instanceof CodeGenPluginHandler) {
                CodeGenPluginHandler plugin = (CodeGenPluginHandler) _plugin;
                if (plugin.name.equalsIgnoreCase(platform)) {
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(PluginLoader.classLoader);
                    CodeGenPlugin codegen = (CodeGenPlugin) plugin.getInstance().getInstance();
                    codegen.prepare();
                    Thread.currentThread().setContextClassLoader(cl);
                    return codegen;
                }
            }
        }

        return null;
    }

    public static Vector<String> getPlatforms() {
        Vector<String> platforms = new Vector<String>();

        for (SketchletPluginHandler cg : codegenPlugins) {
            platforms.add(cg.name);
        }

        return platforms;
    }
}
