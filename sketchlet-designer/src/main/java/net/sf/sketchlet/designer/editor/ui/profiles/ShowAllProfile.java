package net.sf.sketchlet.designer.editor.ui.profiles;

/**
 * @author zobrenovic
 */
public class ShowAllProfile extends Profile {

    public ShowAllProfile() {
        name = "Show All Options";
    }

    public boolean isActive(String item) {
        return true;
    }
}
