package net.sf.sketchlet.designer.eye.eye;

import java.awt.*;

/**
 * @author zobrenovic
 */
public class EmptyEyeSlot extends EyeSlot {

    public EmptyEyeSlot(EyeData parent) {
        super(parent);
        backgroundColor = new Color(255, 255, 255, 0);
    }
}
