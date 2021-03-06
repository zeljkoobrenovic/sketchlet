package net.sf.sketchlet.loaders.pluginloader;

/**
 *
 * @author zeljko
 */
public class PluginData {
    private String name = "";
    private String className = "";
    private String type = "";
    private String description = "";

    public PluginData(String name, String className, String type, String description) {
        this.name = name;
        this.className = className;
        this.type = type;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
