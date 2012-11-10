/*
 * ConfigItem.java
 *
 * Created on March 21, 2008, 9:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.sketchlet.common.config;

import net.sf.sketchlet.common.EscapeChars;

/**
 *
 * @author cuypers
 */
public class ConfigItem {
    public String name;
    public String value;
    
    public String toString() {
        return name + "=" + value;
    }
    
    public String toXML() {
        return "<" + name.toLowerCase() + ">" + EscapeChars.forHTMLTag( value ) + "</" + name.toLowerCase() + ">";
    }
}
