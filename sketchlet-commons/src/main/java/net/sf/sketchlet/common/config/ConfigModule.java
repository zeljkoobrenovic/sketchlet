/*
 * ConfigModule.java
 *
 * Created on March 21, 2008, 9:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.sketchlet.common.config;

import net.sf.sketchlet.common.EscapeChars;

import java.util.Enumeration;
import java.util.Vector;


/**
 *
 * @author cuypers
 */
public class ConfigModule extends ConfigItem {
    private Vector<ConfigItem> moduleItems = new Vector<ConfigItem>();
    
    public String toString() {
        String str = super.toString() + " (";
        
        Enumeration<ConfigItem> items = getModuleItems().elements();
        
        while (items.hasMoreElements()) {
            ConfigItem item = items.nextElement();
            str += item.getName() + "=" + item.getValue() + ";";
        }
        
        str += ")";
        
        return str;
    }
    
    public String toXML() {
        String str = "<" + this.getName().toLowerCase() + " id=\"" + EscapeChars.prepareForXML(getValue())  + "\">";
        
        Enumeration<ConfigItem> items = getModuleItems().elements();
        
        while (items.hasMoreElements()) {
            ConfigItem item = items.nextElement();
            str += item.toXML();
        }
        
        str += "</" + this.getName().toLowerCase() + ">";
        
        return str;
    }

    public Vector<ConfigItem> getModuleItems() {
        return moduleItems;
    }

    public void setModuleItems(Vector<ConfigItem> moduleItems) {
        this.moduleItems = moduleItems;
    }
}
