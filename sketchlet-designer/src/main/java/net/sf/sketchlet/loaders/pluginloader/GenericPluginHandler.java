package net.sf.sketchlet.loaders.pluginloader;

/**
 * @author zobrenovic
 */
public class GenericPluginHandler extends SketchletPluginHandler {

    public GenericPluginHandler(String name, String type, String strClass, String strDescription, String filePath, String group, int position) {
        super(name, type, strClass, strDescription, filePath, group, position);
    }

    public void prepare() {
    }
}