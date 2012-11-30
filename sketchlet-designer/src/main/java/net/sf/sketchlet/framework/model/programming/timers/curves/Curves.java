package net.sf.sketchlet.framework.model.programming.timers.curves;

import net.sf.sketchlet.common.context.SketchletContextUtils;

import javax.swing.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class Curves {

    private Vector<Curve> curves = new Vector<Curve>();
    private static Curves globalCurves = new Curves();

    public Curves() {
        load();
    }

    public static Curves getGlobalCurves() {
        return globalCurves;
    }

    public static void setGlobalCurves(Curves globalCurves) {
        Curves.globalCurves = globalCurves;
    }

    public static String[] getStandardCurves() {
        return standardCurves;
    }

    public void load() {
        try {
            CurvesSaxLoader.getCurves(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "curves.xml", getCurves());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final String[] standardCurves = {"linear", "accelerate", "decelerate", "accelerate + decelerate"};

    public JComboBox getComboBox() {
        JComboBox combo = new JComboBox();
        combo.setEditable(true);

        combo.addItem("");

        for (int i = 0; i < getStandardCurves().length; i++) {
            combo.addItem(getStandardCurves()[i]);
        }
        for (Curve c : getCurves()) {
            combo.addItem(c.getName());
        }

        return combo;
    }

    public String getNewName() {
        int i = getCurves().size() + 1;
        while (true) {
            String name = "Curve " + i++;
            boolean nameExists = false;
            for (Curve c : this.getCurves()) {
                if (c.getName().equals(name)) {
                    nameExists = true;
                }
            }

            if (!nameExists) {
                return name;
            }
        }
    }

    public boolean isStandardCurve(String strCurve) {
        for (int i = 0; i < this.getStandardCurves().length; i++) {
            if (getStandardCurves()[i].equalsIgnoreCase(strCurve)) {
                return true;
            }
        }
        return false;
    }

    public Curve getCurve(String strCurve) {
        if (isStandardCurve(strCurve)) {
            return new StandardCurve(strCurve);
        }
        for (Curve c : getCurves()) {
            if (c.getName().equalsIgnoreCase(strCurve)) {
                return c;
            }
        }

        return null;
    }

    public Curve addNewCurve() {
        Curve c = new Curve();
        c.setName(this.getNewName());
        getCurves().add(c);

        save();

        return c;
    }

    public void save() {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "curves.xml"));
            out.println("<curves>");
            for (Curve c : this.getCurves()) {
                c.save(out);
            }
            out.println("</curves>");

            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Vector<Curve> getCurves() {
        return curves;
    }
}
