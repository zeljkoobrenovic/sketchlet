/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.pluginloader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * @author zobrenovic
 */
public class SketchletPluginHandler {

    public String type = "";
    public String name = "";
    public String strClass = "";
    public String filePath = "";
    public String strDescription = "";
    public String strGroup = "";
    int position = Integer.MAX_VALUE;
    public Properties properties = new Properties();

    public SketchletPluginHandler(String name, String type, String strClass, String strDescription, String filePath, String group, int position) {
        this.name = name;
        this.type = type;
        this.strClass = strClass;
        this.strDescription = strDescription;
        this.filePath = filePath;
        this.position = position;
        this.strGroup = group;
    }

    public void addParam(String name, String value) {
        this.properties.setProperty(name, value);
    }

    public void addParam(String nameValue) {
        String nameValueArray[] = nameValue.split("=");
        if (nameValueArray.length == 2) {
            addParam(nameValueArray[0], nameValueArray[1]);
        }
    }

    public String getParam(String name) {
        return this.properties.getProperty(name);
    }

    public String getParam(String name, String defaultValue) {
        return this.properties.getProperty(name, defaultValue);
    }

    public Object getObject() {
        return null;
    }

    public void prepare() {
    }

    public File getJarDirectory() {
        return new File(this.filePath).getParentFile();
    }

    public PluginInstance getInstance() {
        try {
            Class c = Class.forName(this.strClass, false, PluginLoader.classLoader);
            return new PluginInstance(this, c.getConstructor().newInstance());
        } catch (ClassNotFoundException e1) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.filePath + "':\n");
            PluginConsole.appendToConsole("    Class '" + this.strClass + " not found.'\n");
            e1.printStackTrace();
        } catch (NoSuchMethodException e2) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.filePath + "':\n");
            PluginConsole.appendToConsole("    Constructor not found. Script plugin has to have a constructor with two arguments: (SketchletContext context, File script).'\n");
            e2.printStackTrace();
        } catch (InstantiationException e3) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.filePath + "':\n");
            PluginConsole.appendToConsole("    Error message: " + e3.getMessage() + "\n");
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.filePath + "':\n");
            PluginConsole.appendToConsole("    Error message: " + e4.getMessage() + "\n");
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.filePath + "':\n");
            PluginConsole.appendToConsole("    Error message: " + e5.getMessage() + "\n");
            e5.printStackTrace();
        } catch (Throwable th) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.filePath + "':\n");
            PluginConsole.appendToConsole("    Error message: " + th.getMessage() + "\n");
            th.printStackTrace();
        }
        return null;
    }

    public PluginInstance getInstance(Class<?> parameterType, Object initarg) {
        try {
            Class c = Class.forName(this.strClass, false, PluginLoader.classLoader);
            return new PluginInstance(this, c.getConstructor(parameterType).newInstance(initarg));
        } catch (ClassNotFoundException e1) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.filePath + "':\n");
            PluginConsole.appendToConsole("    Class '" + this.strClass + " not found.'\n");
            e1.printStackTrace();
        } catch (NoSuchMethodException e2) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.filePath + "':\n");
            PluginConsole.appendToConsole("    Constructor not found. Script plugin has to have a constructor with two arguments: (SketchletContext context, File script).'\n");
            e2.printStackTrace();
        } catch (InstantiationException e3) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.filePath + "':\n");
            PluginConsole.appendToConsole("    Error message: " + e3.getMessage() + "\n");
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.filePath + "':\n");
            PluginConsole.appendToConsole("    Error message: " + e4.getMessage() + "\n");
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.filePath + "':\n");
            PluginConsole.appendToConsole("    Error message: " + e5.getMessage() + "\n");
            e5.printStackTrace();
        } catch (Throwable th) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.filePath + "':\n");
            PluginConsole.appendToConsole("    Error message: " + th.getMessage() + "\n");
            th.printStackTrace();
        }
        return null;
    }

    public PluginInstance getInstance(Class<?> parameterTypes[], Object initargs[]) {
        try {
            Class c = Class.forName(this.strClass, false, PluginLoader.classLoader);
            return new PluginInstance(this, c.getConstructor(parameterTypes).newInstance(initargs));
        } catch (ClassNotFoundException e1) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.filePath + "':\n");
            PluginConsole.appendToConsole("    Class '" + this.strClass + " not found.'\n");
            e1.printStackTrace();
        } catch (NoSuchMethodException e2) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.filePath + "':\n");
            PluginConsole.appendToConsole("    Constructor not found. Script plugin has to have a constructor with two arguments: (SketchletContext context, File script).'\n");
            e2.printStackTrace();
        } catch (InstantiationException e3) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.filePath + "':\n");
            PluginConsole.appendToConsole("    Error message: " + e3.getMessage() + "\n");
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.filePath + "':\n");
            PluginConsole.appendToConsole("    Error message: " + e4.getMessage() + "\n");
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.filePath + "':\n");
            PluginConsole.appendToConsole("    Error message: " + e5.getMessage() + "\n");
            e5.printStackTrace();
        } catch (Throwable th) {
            PluginConsole.appendToConsole("ERROR in plugin file '" + this.filePath + "':\n");
            PluginConsole.appendToConsole("    Error message: " + th.getMessage() + "\n");
            th.printStackTrace();
        }
        return null;
    }
}
