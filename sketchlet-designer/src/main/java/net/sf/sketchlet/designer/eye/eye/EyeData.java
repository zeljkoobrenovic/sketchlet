package net.sf.sketchlet.designer.eye.eye;

import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;
import net.sf.sketchlet.framework.blackboard.Variable;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.model.Page;
import net.sf.sketchlet.framework.model.programming.macros.Macro;
import net.sf.sketchlet.framework.model.programming.macros.Macros;
import net.sf.sketchlet.framework.model.programming.screenscripts.ScreenScript;
import net.sf.sketchlet.framework.model.programming.screenscripts.ScreenScripts;
import net.sf.sketchlet.framework.model.programming.timers.Timer;
import net.sf.sketchlet.framework.model.programming.timers.Timers;
import net.sf.sketchlet.script.ScriptPluginProxy;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class EyeData {

    Vector<EyeSlot> slots = new Vector<EyeSlot>();
    EyeSlot selectedSlot = null;
    SelectorPanel selector;

    public EyeData(SelectorPanel selector) {
        this.selector = selector;
        load();
    }

    public void load() {
        slots.removeAllElements();
        int n = 0;
        if (selector.showVariables.isSelected()) {
            for (String strVar : VariablesBlackboard.getInstance().getVariablesList()) {
                Variable v = VariablesBlackboard.getInstance().getVariable(strVar);
                if (v != null && !v.getName().isEmpty()) {
                    VariableEyeSlot slot = new VariableEyeSlot(v, this);
                    slots.add(slot);
                    n++;
                }
            }
        }
        if (n > 0) {
            slots.add(new EmptyEyeSlot(this));
            n = 0;
        }

        if (selector.showTimers.isSelected()) {
            for (Timer t : Timers.getGlobalTimers().getTimers()) {
                TimerEyeSlot slot = new TimerEyeSlot(t, this);
                slots.add(slot);
                n++;
            }
        }
        if (n > 0) {
            slots.add(new EmptyEyeSlot(this));
            n = 0;
        }

        if (selector.showMacros.isSelected()) {
            for (Macro m : Macros.globalMacros.macros) {
                MacroEyeSlot slot = new MacroEyeSlot(m, this);
                slots.add(slot);
                n++;
            }
        }
        if (n > 0) {
            slots.add(new EmptyEyeSlot(this));
            n = 0;
        }
        if (selector.showScreenActions.isSelected()) {
            for (ScreenScript sc : ScreenScripts.getPublicScriptRunner().getScripts()) {
                ScreenActionEyeSlot slot = new ScreenActionEyeSlot(sc, this);
                slots.add(slot);
                n++;
            }
        }
        if (n > 0) {
            slots.add(new EmptyEyeSlot(this));
            n = 0;
        }
        if (selector.showScripts.isSelected()) {
            for (ScriptPluginProxy sc : VariablesBlackboard.getScripts()) {
                ScriptEyeSlot slot = new ScriptEyeSlot(sc, this);
                slots.add(slot);
                n++;
            }
        }
        if (n > 0) {
            slots.add(new EmptyEyeSlot(this));
            n = 0;
        }

        if (selector.showSketches.isSelected()) {
            for (Page s : SketchletEditor.getPages().getPages()) {
                EyeSlot slot = new SketchEyeSlot(s, this);
                if (s == SketchletEditor.getInstance().getCurrentPage() && n > 0) {
                    slots.add(new EmptyEyeSlot(this));
                }
                slots.add(slot);
                if (s == SketchletEditor.getInstance().getCurrentPage()) {
                    addCurrentSketch();
                    slots.add(new EmptyEyeSlot(this));
                    n = 0;
                } else {
                    n++;
                }
            }
        } else {
            addCurrentSketch();
            n++;
        }
        if (n > 0) {
            slots.add(new EmptyEyeSlot(this));
        }

        for (int i = 0; i < slots.size(); i++) {
            for (int j = 0; j < slots.size(); j++) {
                slots.elementAt(i).addRelatedSlot(slots.elementAt(j));
            }
        }

        for (int i = 0; i < slots.size(); i++) {
            Collections.sort(slots.elementAt(i).relatedSlotsInfo, new Comparator() {

                public int compare(Object obj1, Object obj2) {
                    if (obj1 instanceof EyeSlotRelation && obj2 instanceof EyeSlotRelation) {
                        return ((EyeSlotRelation) obj1).description.compareTo(((EyeSlotRelation) obj2).description);
                    }
                    return 0;
                }
            });
        }
    }

    public void addCurrentSketch() {
        if (selector.showRegions.isSelected()) {
            Page page = SketchletEditor.getInstance().getCurrentPage();
            SketchEntryEyeSlot mslot = new SketchEntryEyeSlot(page, this);
            slots.add(mslot);
            SketchExitEyeSlot mslot2 = new SketchExitEyeSlot(page, this);
            slots.add(mslot2);
            KeyboardActionsEyeSlot kslot = new KeyboardActionsEyeSlot(page, this);
            slots.add(kslot);
            VariablesEventsEyeSlot vslot = new VariablesEventsEyeSlot(page, this);
            slots.add(vslot);
            for (ActiveRegion region : page.getRegions().getRegions()) {
                RegionEyeSlot rs = new RegionEyeSlot(region, this);
                slots.add(rs);
            }
        }
    }

    double prevAngle = 0.0;

    public EyeSlot selectSlot(double angle) {
        selectedSlot = null;
        int n = this.slots.size();
        for (int i = 0; i < slots.size(); i++) {
            EyeSlot slot = slots.elementAt(i);
            if (slot.isSelected(angle)) {
                selectedSlot = slot;
                break;
            }
        }

        if (selectedSlot != null) {
            this.selector.describeRelation(selectedSlot);
        }

        return selectedSlot;
    }

    public double getAngle(double angle) {
        int n = this.slots.size();
        double rotStep = Math.PI * 2 / n;
        for (int i = 0; i < slots.size(); i++) {
            EyeSlot slot = slots.elementAt(i);
            if (slot.isSelected(angle)) {
                angle = -rotStep * i - rotStep / 2;
                break;
            }
        }
        return angle;
    }

    public void draw(Graphics2D g2, int w, int h, double angle) {
        int n = this.slots.size();
        double rotStep = Math.PI * 2 / n;
        int cx = w / 2;
        int cy = h / 2;

        int slotW = 10;
        int slotH = 12;

        // angle = getAngle(angle);

        int _w = Math.min(w, h);
        int _h = _w;
        int marginX = Math.max(0, (w - _w) / 2);
        int marginY = Math.max(0, (h - _h) / 2);
        g2.setPaint(Color.gray);
        BasicStroke stroke = new BasicStroke(2.0f);
        g2.setStroke(stroke);
        //Ellipse2D e1 = new Ellipse2D.Double(marginX, marginY, _w , _h);
        //g2.draw(e1);
        // g2.rotate(angle, cx, cy);

        g2.translate(-marginX, -marginY);
        // g2.drawOval(marginX, marginY, _w, _h);

        AffineTransform affine = g2.getTransform();
        for (int i = 0; i < slots.size(); i++) {
            EyeSlot slot = slots.elementAt(i);
            double a = angle + rotStep * i;
            while (a < 0) {
                a += Math.PI * 2;
            }
            while (a > Math.PI * 2) {
                a -= Math.PI * 2;
            }
            slot.angle = a;
            g2.translate(marginX, marginY);
            g2.rotate(a, cx, cy);
            g2.translate(marginX + _w, marginY + _h / 2);
            slot.draw(g2, slotW, slotH);
            Point2D pt = g2.getTransform().transform(new Point(-EyeSlot.offset, slotH / 2), null);
            slot.x = (int) pt.getX();
            slot.y = (int) pt.getY();
            g2.setTransform(affine);
        }

        g2.translate(marginX, marginY);

        stroke = new BasicStroke(1.0f);
        g2.setStroke(stroke);
        for (int i = 0; i < slots.size(); i++) {
            EyeSlot slot = slots.elementAt(i);

            for (EyeSlot rs : slot.relatedSlots) {
                if (selectedSlot != null && (slot == selectedSlot || rs == selectedSlot)) {
                    g2.setColor(selectedSlot.backgroundColor);
                    g2.setStroke(new BasicStroke(1.5f));
                } else {
                    g2.setColor(new Color(100, 100, 100, 100));
                    g2.setStroke(new BasicStroke(1.0f));
                }
                QuadCurve2D shape = new QuadCurve2D.Float(slot.x, slot.y, cx, cy, rs.x, rs.y);
                g2.draw(shape);
            }
        }
    }
}
