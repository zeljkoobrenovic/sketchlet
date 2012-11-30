package net.sf.sketchlet.loaders.pluginloader;

import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.plugin.SketchletApplicationAware;
import net.sf.sketchlet.plugin.SketchletProjectAware;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class GenericPluginFactory {

    private static List<PluginInstance> genericPlugins = new ArrayList<PluginInstance>();
    private static List<PluginInstance> derivedvars = new ArrayList<PluginInstance>();
    protected static List<SketchletPluginHandler> allPlugins = new Vector<SketchletPluginHandler>();

    public static void createPluginInstances() {
        genericPlugins = PluginLoader.getPluginInstances("", "generic", "connector", "netconnector");
        derivedvars = PluginLoader.getPluginInstances("derivedvars", "derivedvariables");
    }

    public static List<PluginInstance> getGenericPlugins() {
        return genericPlugins;
    }

    public static List<PluginInstance> getDerivedVariablesPlugins() {
        return derivedvars;
    }

    public static List<PluginInstance> getPlugins() {
        List<PluginInstance> list = new ArrayList<PluginInstance>();
        for (PluginInstance plugin : getGenericPlugins()) {
            list.add(plugin);
        }
        for (PluginInstance plugin : getDerivedVariablesPlugins()) {
            list.add(plugin);
        }
        return list;
    }

    public static void save() {
        for (PluginInstance plugin : GenericPluginFactory.getPlugins()) {
            if (plugin.getInstance() instanceof SketchletProjectAware) {
                ((SketchletProjectAware) plugin.getInstance()).onSave();
            }
        }
    }

    public static void afterApplicationStart() {
        for (PluginInstance plugin : GenericPluginFactory.getGenericPlugins()) {
            if (plugin.getInstance() instanceof SketchletApplicationAware) {
                ((SketchletApplicationAware) plugin.getInstance()).afterApplicationStart();
            }
        }
    }

    public static void beforeApplicationEnd() {
        for (PluginInstance plugin : GenericPluginFactory.getGenericPlugins()) {
            if (plugin.getInstance() instanceof SketchletApplicationAware) {
                ((SketchletApplicationAware) plugin.getInstance()).beforeApplicationEnd();
            }
        }
    }

    public static void afterProjectOpening() {
        if (SketchletContext.getInstance() == null || SketchletContext.getInstance().getCurrentProjectDirectory() == null) {
            return;
        }
        for (PluginInstance plugin : GenericPluginFactory.getPlugins()) {
            if (plugin.getInstance() instanceof SketchletProjectAware) {
                ((SketchletProjectAware) plugin.getInstance()).afterProjectOpening();
            }
        }
    }

    public static void beforeProjectClosing() {
        if (SketchletContext.getInstance() == null || SketchletContext.getInstance().getCurrentProjectDirectory() == null) {
            return;
        }
        for (PluginInstance plugin : GenericPluginFactory.getPlugins()) {
            if (plugin.getInstance() instanceof SketchletProjectAware) {
                ((SketchletProjectAware) plugin.getInstance()).beforeProjectClosing();
            }
        }
    }
}
