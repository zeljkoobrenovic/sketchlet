/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.pluginloader;

/**
 * @author zobrenovic
 */
public class PluginInstance {

    private Object instance;
    SketchletPluginHandler pluginHandler;

    public PluginInstance(SketchletPluginHandler handler, Object instance) {
        this.instance = instance;
        this.pluginHandler = handler;
    }

    public Object getInstance() {
        return instance;
    }

    public String getName() {
        if (pluginHandler.name != null && !pluginHandler.name.isEmpty()) {
            return pluginHandler.name;
        } else {
            String name = instance.getClass().getName();
            int n = name.lastIndexOf(".");
            if (n > 0) {
                return name.substring(n + 1);
            } else {
                return name;
            }
        }
    }

    public String getType() {
        return pluginHandler.type;
    }
}
