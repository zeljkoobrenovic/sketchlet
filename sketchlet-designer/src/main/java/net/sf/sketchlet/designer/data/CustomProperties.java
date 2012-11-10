/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.data;

import java.util.Properties;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class CustomProperties {

    public static Properties regionCustomProperties = new Properties();
    public static Vector<String> regionCustomPropertyNames = new Vector<String>();

    public static void addCustomProperty(String name, String defaultValue) {
        CustomProperties.regionCustomProperties.setProperty(name, defaultValue);
        CustomProperties.regionCustomPropertyNames.add(name);
    }

    public Vector<String> getCustomPropertyNames() {
        return CustomProperties.regionCustomPropertyNames;
    }
}
