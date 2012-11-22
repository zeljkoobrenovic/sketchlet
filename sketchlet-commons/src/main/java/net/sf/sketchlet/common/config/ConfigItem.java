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
    private String name;
    private String value;
    
    public String toString() {
        return getName() + "=" + getValue();
    }
    
    public String toXML() {
        return "<" + getName().toLowerCase() + ">" + EscapeChars.forHTMLTag(getValue()) + "</" + getName().toLowerCase() + ">";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
