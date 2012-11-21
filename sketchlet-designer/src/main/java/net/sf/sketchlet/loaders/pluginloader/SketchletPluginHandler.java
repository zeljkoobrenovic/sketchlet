/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.loaders.pluginloader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * @author zobrenovic
 */
public class SketchletPluginHandler {

    private String type = "";
    private String name = "";
    private String className = "";
    private String filePath = "";
    private String strDescription = "";
    private String strGroup = "";
    private int position = Integer.MAX_VALUE;
    private Properties properties = new Properties();

    public SketchletPluginHandler(String name, String type, String className, String strDescription, String filePath, String group, int position) {
        this.setName(name);
        this.setType(type);
        this.setClassName(className);
        this.setStrDescription(strDescription);
        this.setFilePath(filePath);
        this.setPosition(position);
        this.setStrGroup(group);
    }

    public void addParam(String name, String value) {
        this.getProperties().setProperty(name, value);
    }

    public void addParam(String nameValue) {
        String nameValueArray[] = nameValue.split("=");
        if (nameValueArray.length == 2) {
            addParam(nameValueArray[0], nameValueArray[1]);
        }
    }

    public String getParam(String name) {
        return this.getProperties().getProperty(name);
    }

    public String getParam(String name, String defaultValue) {
        return this.getProperties().getProperty(name, defaultValue);
    }

    public Object getObject() {
        return null;
    }

    public void prepare() {
    }

    public File getJarDirectory() {
        return new File(this.getFilePath()).getParentFile();
    }

    public PluginInstance getInstance() {
        try {
            Class c = Class.forName(this.getClassName(), false, PluginLoader.getClassLoader());
            return new PluginInstance(this, c.getConstructor().newInstance());
        } catch (ClassNotFoundException e1) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.getFilePath() + "':\n");
            PluginConsole.appendToConsole("    Class '" + this.getClassName() + " not found.'\n");
            e1.printStackTrace();
        } catch (NoSuchMethodException e2) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.getFilePath() + "':\n");
            PluginConsole.appendToConsole("    Constructor not found. Script plugin has to have a constructor with two arguments: (SketchletContext context, File script).'\n");
            e2.printStackTrace();
        } catch (InstantiationException e3) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.getFilePath() + "':\n");
            PluginConsole.appendToConsole("    Error message: " + e3.getMessage() + "\n");
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.getFilePath() + "':\n");
            PluginConsole.appendToConsole("    Error message: " + e4.getMessage() + "\n");
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.getFilePath() + "':\n");
            PluginConsole.appendToConsole("    Error message: " + e5.getMessage() + "\n");
            e5.printStackTrace();
        } catch (Throwable th) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.getFilePath() + "':\n");
            PluginConsole.appendToConsole("    Error message: " + th.getMessage() + "\n");
            th.printStackTrace();
        }
        return null;
    }

    public PluginInstance getInstance(Class<?> parameterType, Object initarg) {
        try {
            Class c = Class.forName(this.getClassName(), false, PluginLoader.getClassLoader());
            return new PluginInstance(this, c.getConstructor(parameterType).newInstance(initarg));
        } catch (ClassNotFoundException e1) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.getFilePath() + "':\n");
            PluginConsole.appendToConsole("    Class '" + this.getClassName() + " not found.'\n");
            e1.printStackTrace();
        } catch (NoSuchMethodException e2) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.getFilePath() + "':\n");
            PluginConsole.appendToConsole("    Constructor not found. Script plugin has to have a constructor with two arguments: (SketchletContext context, File script).'\n");
            e2.printStackTrace();
        } catch (InstantiationException e3) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.getFilePath() + "':\n");
            PluginConsole.appendToConsole("    Error message: " + e3.getMessage() + "\n");
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.getFilePath() + "':\n");
            PluginConsole.appendToConsole("    Error message: " + e4.getMessage() + "\n");
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.getFilePath() + "':\n");
            PluginConsole.appendToConsole("    Error message: " + e5.getMessage() + "\n");
            e5.printStackTrace();
        } catch (Throwable th) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.getFilePath() + "':\n");
            PluginConsole.appendToConsole("    Error message: " + th.getMessage() + "\n");
            th.printStackTrace();
        }
        return null;
    }

    public PluginInstance getInstance(Class<?> parameterTypes[], Object initargs[]) {
        try {
            Class c = Class.forName(this.getClassName(), false, PluginLoader.getClassLoader());
            return new PluginInstance(this, c.getConstructor(parameterTypes).newInstance(initargs));
        } catch (ClassNotFoundException e1) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.getFilePath() + "':\n");
            PluginConsole.appendToConsole("    Class '" + this.getClassName() + " not found.'\n");
            e1.printStackTrace();
        } catch (NoSuchMethodException e2) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.getFilePath() + "':\n");
            PluginConsole.appendToConsole("    Constructor not found. Script plugin has to have a constructor with two arguments: (SketchletContext context, File script).'\n");
            e2.printStackTrace();
        } catch (InstantiationException e3) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.getFilePath() + "':\n");
            PluginConsole.appendToConsole("    Error message: " + e3.getMessage() + "\n");
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.getFilePath() + "':\n");
            PluginConsole.appendToConsole("    Error message: " + e4.getMessage() + "\n");
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.getFilePath() + "':\n");
            PluginConsole.appendToConsole("    Error message: " + e5.getMessage() + "\n");
            e5.printStackTrace();
        } catch (Throwable th) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.getFilePath() + "':\n");
            PluginConsole.appendToConsole("    Error message: " + th.getMessage() + "\n");
            th.printStackTrace();
        }
        return null;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getStrDescription() {
        return strDescription;
    }

    public void setStrDescription(String strDescription) {
        this.strDescription = strDescription;
    }

    public String getStrGroup() {
        return strGroup;
    }

    public void setStrGroup(String strGroup) {
        this.strGroup = strGroup;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Properties getProperties() {
        return properties;
    }
}
