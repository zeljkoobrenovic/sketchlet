package net.sf.sketchlet.loaders.pluginloader;

import net.sf.sketchlet.plugin.CodeGenPlugin;

import java.util.Vector;

/**
 * @author zobrenovic
 */
public class CodeGenPluginFactory {

    private static Vector<SketchletPluginHandler> codeGeneratorPlugins = new Vector<SketchletPluginHandler>();
    private static String platform = "html";

    public static CodeGenPlugin getCodeGenPluginInstance() {
        for (SketchletPluginHandler _plugin : getCodeGeneratorPlugins()) {
            if (_plugin instanceof CodeGenPluginHandler) {
                CodeGenPluginHandler plugin = (CodeGenPluginHandler) _plugin;
                if (plugin.getName().equalsIgnoreCase(getPlatform())) {
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(PluginLoader.getClassLoader());
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

        for (SketchletPluginHandler cg : getCodeGeneratorPlugins()) {
            platforms.add(cg.getName());
        }

        return platforms;
    }

    public static Vector<SketchletPluginHandler> getCodeGeneratorPlugins() {
        return codeGeneratorPlugins;
    }

    public static void setCodeGeneratorPlugins(Vector<SketchletPluginHandler> codeGeneratorPlugins) {
        CodeGenPluginFactory.codeGeneratorPlugins = codeGeneratorPlugins;
    }

    public static String getPlatform() {
        return platform;
    }

    public static void setPlatform(String platform) {
        CodeGenPluginFactory.platform = platform;
    }
}
