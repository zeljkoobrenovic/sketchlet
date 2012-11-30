package net.sf.sketchlet.designer.eye.eye;

/**
 * @author zobrenovic
 */
public class EyeSlotRelation {

    EyeSlot slot1;
    EyeSlot slot2;
    String description = "";

    public EyeSlotRelation(EyeSlot slot1, EyeSlot slot2, String description) {
        this.slot1 = slot1;
        this.slot2 = slot2;
        this.description = description;
    }
}
