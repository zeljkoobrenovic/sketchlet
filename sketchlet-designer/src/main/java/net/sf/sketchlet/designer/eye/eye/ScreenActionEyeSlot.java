package net.sf.sketchlet.designer.eye.eye;

import net.sf.sketchlet.framework.model.programming.screenscripts.ScreenScript;
import java.awt.*;

/**
 * @author zobrenovic
 */
public class ScreenActionEyeSlot extends EyeSlot {
    ScreenScript macro;

    public ScreenActionEyeSlot(ScreenScript macro, EyeData parent) {
        super(parent);
        this.macro = macro;
        this.name = macro.getInfo().getName();
        this.backgroundColor = Color.BLUE;
    }


    public String getLongName() {
        return "Screen Action " + this.name;
    }
}
