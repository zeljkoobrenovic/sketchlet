/*
 * ConfigItemHelper.java
 *
 * Created on March 21, 2008, 9:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.sketchlet.common.config;

import java.util.*;

/**
 *
 * @author cuypers
 */
public class ConfigItemHelper {
    Vector<ConfigItem> data;
    Hashtable itemsHashtable = new Hashtable();
    
    public ConfigItemHelper( Vector<ConfigItem> data ) {
        this.data = data;
        Enumeration<ConfigItem> elements = data.elements();
        
        int i = 0;
        while (elements.hasMoreElements()) {
            ConfigItem item = elements.nextElement();
            
            if (item instanceof ConfigModule) {
                itemsHashtable.put( item.name.toLowerCase() + "_" + i + "_value", item.value);
                itemsHashtable.put( item.name.toLowerCase() + "_" + i, new ConfigItemHelper(((ConfigModule) item).moduleItems ));
                i++;
            } else {
                itemsHashtable.put( item.name.toLowerCase(), item.value );
            }
        }
    }
    
    public String getString( String name ) {
        Object str = this.itemsHashtable.get( name.toLowerCase() );
        
        if (str != null && str instanceof String)
            return (String) str;
        
        return "";
    }
    
    public int getInteger( String name ) {
        int number = 0;
        
        try {
            number = Integer.parseInt( this.getString( name ) );
        } catch (Exception e) {
            
        }
         
        return number;
    }
 
    public int getInteger( String structName, int index, String name ) {
        int number = 0;
        
        try {
            number = Integer.parseInt( this.getString( structName, index, name ) );
        } catch (Exception e) {
            
        }
         
        return number;
    }
 
    public boolean getBoolean( String name ) {
        String str = this.getString( name ).trim().toLowerCase();
        
        return str.equals("true") || str.equals("yes");
    }
    
    public boolean getBoolean( String structName, int index, String name ) {
        String str = this.getString( structName, index, name ).trim().toLowerCase();
        
        return str.equals("true") || str.equals("yes");
    }
    
    public String getString( String name, int index ) {
        Object str = this.itemsHashtable.get( name.toLowerCase() + "_" + index + "_value" );
        
        if (str != null && str instanceof String)
            return (String) str;
        
        return "";
    }
    
    public String getString( String structName, int index, String name ) {
        Object o = this.itemsHashtable.get( structName.toLowerCase() + "_" + index );
        
        if (o != null && o instanceof ConfigItemHelper)
            return ((ConfigItemHelper) o).getString( name );
        
        return "";
    }
    
    public String getString( String structName, String name ) {
        return getString( structName, 0, name );
    }
}