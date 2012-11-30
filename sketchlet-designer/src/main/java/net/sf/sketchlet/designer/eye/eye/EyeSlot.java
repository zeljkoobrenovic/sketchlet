package net.sf.sketchlet.designer.eye.eye;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class EyeSlot {

    String name = "";
    Vector<EyeSlot> relatedSlots = new Vector<EyeSlot>();
    Vector<EyeSlotRelation> relatedSlotsInfo = new Vector<EyeSlotRelation>();
    Color backgroundColor = Color.WHITE;
    Color foregroundColor = Color.BLACK;
    double angle = 0.0;
    int x;
    int y;
    public static final int offset = 60;
    EyeData parent;

    public EyeSlot(EyeData parent) {
        this.parent = parent;
    }

    public void draw(Graphics2D g2, int w, int h) {
        AffineTransform oldTransform = g2.getTransform();
        Font font = g2.getFont();
        if (parent.selectedSlot == this) {
            g2.setFont(font.deriveFont(Font.BOLD));
        } else {
            g2.setFont(font.deriveFont(Font.PLAIN));
        }
        font = g2.getFont();
        FontRenderContext frc = g2.getFontRenderContext();
        LineMetrics metrics = font.getLineMetrics(name, frc);
        int textWidth = (int) font.getStringBounds(name, frc).getWidth();
        int textHeight = (int) metrics.getHeight();

        g2.setColor(this.backgroundColor);
        g2.fillRoundRect(-offset, 0, w, h, 5, 5);
        g2.setColor(this.foregroundColor);
        if (angle > Math.PI / 2 && angle < 3 * Math.PI / 2) {
            g2.rotate(Math.PI, w + 2 - offset + textWidth / 2, textHeight / 2);
        }
        g2.setColor(this.foregroundColor);
        g2.drawString(name, w + 2 - offset, h - 3);
        g2.setTransform(oldTransform);
    }

    public boolean isSelected(double angle) {
        int n = parent.slots.size();
        double rotStep = Math.PI * 2 / n;

        return Math.abs(angle - this.angle) < rotStep / 2;
    }

    public String getLongName() {
        return this.name;
    }

    public void addRelatedSlot(EyeSlot relatedSlot) {
    }

    public void checkAndAdd(EyeSlot relatedSlot, Object data[][], String strName1, String strName2, int index1, int index2) {
        for (int i = 0; i < data.length; i++) {
            String strAction = (String) data[i][index1];
            String strParam1 = (String) data[i][index2];
            String strParam2 = (String) data[i][index2 + 1];
            if (!strAction.isEmpty() && !strParam1.isEmpty()) {
                if (strAction.equalsIgnoreCase(strName1) && strParam1.equalsIgnoreCase(strName2)) {
                    addRelationToSlot(relatedSlot, strAction + " " + strParam1 + " " + strParam2, strAction + " " + strParam1 + " " + strParam2);
                    break;
                }
            }
        }
    }

    public void checkAndAdd(EyeSlot relatedSlot, Object data[][], String strName1, String strName2, int index1, int index2, String desc1, String desc2) {
        for (int i = 0; i < data.length; i++) {
            String strAction = (String) data[i][index1];
            String strParam1 = (String) data[i][index2];
            if (!strAction.isEmpty() && !strParam1.isEmpty()) {
                if (strAction.equalsIgnoreCase(strName1) && strParam1.equalsIgnoreCase(strName2)) {
                    addRelationToSlot(relatedSlot, desc1, desc2);
                    break;
                }
            }
        }
    }

    public void checkAndAdd(EyeSlot relatedSlot, Object data[][], String strName1, int index1, String desc1, String desc2) {
        for (int i = 0; i < data.length; i++) {
            String strParam1 = (String) data[i][index1];
            if (!strParam1.isEmpty() && strParam1.equalsIgnoreCase(strName1)) {
                addRelationToSlot(relatedSlot, desc1, desc2);
                break;
            }
        }
    }

    public String getValue(Object data[][], String strName1, String strName2, int index1, int index2) {
        for (int i = 0; i < data.length; i++) {
            String strAction = (String) data[i][index1];
            String strParam1 = (String) data[i][index2];
            String strParam2 = (String) data[i][index2 + 1];
            if (!strAction.isEmpty() && !strParam1.isEmpty()) {
                if (strAction.equalsIgnoreCase(strName1) && strParam1.equalsIgnoreCase(strName2)) {
                    return strParam2;
                }
            }
        }

        return "";
    }

    public String getValue(Object data[][], String strName1, int index1, int index2) {
        for (int i = 0; i < data.length; i++) {
            String strParam1 = (String) data[i][index1];
            String strParam2 = (String) data[i][index2];
            if (strParam1.equalsIgnoreCase(strName1)) {
                return strParam2;
            }
        }

        return "";
    }

    public String getValue(Object data[][], String strNames[], int indexes[], int index2) {
        for (int i = 0; i < data.length; i++) {
            boolean b[] = new boolean[strNames.length];
            for (int n = 0; n < strNames.length; n++) {
                String strName = strNames[n];
                int index = indexes[n];
                String strParam = (String) data[i][index];
                if (strParam.equalsIgnoreCase(strName)) {
                    b[n] = true;
                }
            }
            boolean done = true;
            for (int n = 0; n < strNames.length; n++) {
                if (!b[n]) {
                    done = false;
                    break;
                }
            }

            if (done) {
                return (String) data[i][index2];
            }
        }


        return "";
    }

    public void addRelationToSlot(EyeSlot relatedSlot, String desc1, String desc2) {
        if (!relatedSlotsInfo.contains(relatedSlot)) {
            relatedSlots.add(relatedSlot);
            relatedSlotsInfo.add(new EyeSlotRelation(this, relatedSlot, desc2));
        }
        if (!relatedSlot.relatedSlotsInfo.contains(this)) {
            relatedSlot.relatedSlots.add(this);
            relatedSlot.relatedSlotsInfo.add(new EyeSlotRelation(relatedSlot, this, desc1));
        }
    }

    public void openItem() {
    }
}
