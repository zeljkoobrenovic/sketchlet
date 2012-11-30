package net.sf.sketchlet.designer.editor.ui.profiles;

import net.sf.sketchlet.designer.GlobalProperties;
import net.sf.sketchlet.designer.SketchletDesignerProperties;

import java.util.Vector;

/**
 * @author zobrenovic
 */
public class Profile {

    String name = "";
    String propertiesKey = "";
    Vector<String> activeItems = new Vector<String>();
    boolean userProfile = false;

    public Profile() {
    }

    /*public Profile(String name) {
        this.name = name;
    }*/

    public Profile(String strProfile) {
        String values[] = strProfile.split(",");
        if (values.length > 0) {
            this.name = values[0];
            for (int i = 1; i < values.length; i++) {
                activeItems.add(values[i]);
            }
        }
    }

    /*public Profile(String name, String propertiesKey, boolean userProfile) {
        this.name = name;
        this.propertiesKey = propertiesKey;
        this.userProfile = userProfile;
        String strProperty = "";
        if (userProfile) {
            strProperty = GlobalProperties.get(propertiesKey, name);
        } else {
            strProperty = SketchletDesignerProperties.get(propertiesKey, name);
        }
        String values[] = strProperty.split(",");
        if (values.length > 0) {
            this.name = values[0];
            for (int i = 1; i < values.length; i++) {
                activeItems.add(values[i]);
            }
        }
    }*/

    public boolean isActive(String item) {
        return activeItems.contains(item);
    }

    public void save() {
        String values = name;
        for (String item : activeItems) {
            values += "," + item;
        }
        if (userProfile) {
            GlobalProperties.set(this.propertiesKey, values);
        } else {
            SketchletDesignerProperties.set(this.propertiesKey, values);
        }
    }

    public String getProfileText() {
        String values = name;
        for (String item : activeItems) {
            values += "," + item;
        }
        return values;
    }
}
