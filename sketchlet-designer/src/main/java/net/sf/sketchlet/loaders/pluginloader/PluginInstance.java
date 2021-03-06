package net.sf.sketchlet.loaders.pluginloader;

/**
 * @author zobrenovic
 */
public class PluginInstance {

    private Object instance;
    private SketchletPluginHandler pluginHandler;

    public PluginInstance(SketchletPluginHandler handler, Object instance) {
        this.instance = instance;
        this.pluginHandler = handler;
    }

    public Object getInstance() {
        return instance;
    }

    public String getName() {
        if (pluginHandler.getName() != null && !pluginHandler.getName().isEmpty()) {
            return pluginHandler.getName();
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
        return pluginHandler.getType();
    }
}
