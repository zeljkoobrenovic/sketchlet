/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugin;

/**
 *
 * @author zobrenovic
 */
public class CodeGenPluginSetting {
    
    public CodeGenPluginSetting(String label, String type, String propName, String defaultValue, String possibleValues[]) {
        this.label = label;
        this.type = type;
        this.propName = propName;
        this.defaultValue = defaultValue;
        this.possibleValues = possibleValues;
    }
    
    public String label = "";
    public String type = "";
    public String propName = "";
    public String defaultValue = null;
    public String possibleValues[] = null;
}
