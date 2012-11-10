/*
 * ConfigModule.java
 *
 * Created on March 21, 2008, 9:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.sketchlet.common.config;

import java.util.Enumeration;
import java.util.Vector;
import net.sf.sketchlet.common.EscapeChars;


/**
 *
 * @author cuypers
 */
public class ConfigModule extends ConfigItem {
    public Vector<ConfigItem> moduleItems = new Vector<ConfigItem>();
    
    public String toString() {
        String str = super.toString() + " (";
        
        Enumeration<ConfigItem> items = moduleItems.elements();
        
        while (items.hasMoreElements()) {
            ConfigItem item = items.nextElement();
            str += item.name + "=" + item.value + ";";
        }
        
        str += ")";
        
        return str;
    }
    
    public String toXML() {
        String str = "<" + this.name.toLowerCase() + " id=\"" + EscapeChars.prepareForXML( value )  + "\">";
        
        Enumeration<ConfigItem> items = moduleItems.elements();
        
        while (items.hasMoreElements()) {
            ConfigItem item = items.nextElement();
            str += item.toXML();
        }
        
        str += "</" + this.name.toLowerCase() + ">";
        
        return str;
    }
}
