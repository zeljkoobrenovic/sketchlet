/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.programming.timers.curves;

import net.sf.sketchlet.common.context.SketchletContextUtils;

import javax.swing.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class Curves {

    public Vector<Curve> curves = new Vector<Curve>();
    public static Curves globalCurves = new Curves();

    public Curves() {
        load();
    }

    public void load() {
        try {
            CurvesSaxLoader.getCurves(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "curves.xml", curves);
            /*
            File file = new File(WorkspaceUtils.getCurrentProjectSkecthletsDir() + "curves.xml");

            if (file.exists()) {
            XPathEvaluator xp = new XPathEvaluator();
            xp.createDocumentFromFile(file);

            NodeList curveNodes = xp.getNodes("/curves/curve");

            if (curveNodes != null) {
            for (int i = 0; i < curveNodes.getLength(); i++) {
            Curve c = new Curve();

            c.name = xp.getString("/curves/curve[position()=" + (i + 1) + "]/name");
            NodeList segmentNodes = xp.getNodes("/curves/curve[position()=" + (i + 1) + "]/segments/segment");

            c.segments.removeAllElements();

            for (int j = 0; j < segmentNodes.getLength(); j++) {
            CurveSegment cs = new CurveSegment();
            cs.endTime = xp.getDouble("/curves/curve[position()=" + (i + 1) + "]/segments/segment[position()=" + (j + 1) + "]/end-time");
            cs.relativeValue = xp.getDouble("/curves/curve[position()=" + (i + 1) + "]/segments/segment[position()=" + (j + 1) + "]/relative-value");
            cs.minDuration = xp.getString("/curves/curve[position()=" + (i + 1) + "]/segments/segment[position()=" + (j + 1) + "]/min-duration");
            cs.maxDuration = xp.getString("/curves/curve[position()=" + (i + 1) + "]/segments/segment[position()=" + (j + 1) + "]/max-duration");
            cs.startAfter = xp.getString("/curves/curve[position()=" + (i + 1) + "]/segments/segment[position()=" + (j + 1) + "]/start-after");
            cs.finishBefore = xp.getString("/curves/curve[position()=" + (i + 1) + "]/segments/segment[position()=" + (j + 1) + "]/finish-before");

            c.segments.add(cs);
            }

            curves.add(c);
            }
            }
            } else {
            this.addNewCurve();
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final String standardCurves[] = {"linear", "accelerate", "decelerate", "accelerate + decelerate"};

    public JComboBox getComboBox() {
        JComboBox combo = new JComboBox();
        combo.setEditable(true);

        combo.addItem("");

        for (int i = 0; i < standardCurves.length; i++) {
            combo.addItem(standardCurves[i]);
        }
        for (Curve c : curves) {
            combo.addItem(c.name);
        }

        return combo;
    }

    public String getNewName() {
        int i = curves.size() + 1;
        while (true) {
            String name = "Curve " + i++;
            boolean nameExists = false;
            for (Curve c : this.curves) {
                if (c.name.equals(name)) {
                    nameExists = true;
                }
            }

            if (!nameExists) {
                return name;
            }
        }
    }

    public boolean isStandardCurve(String strCurve) {
        for (int i = 0; i < this.standardCurves.length; i++) {
            if (standardCurves[i].equalsIgnoreCase(strCurve)) {
                return true;
            }
        }
        return false;
    }

    public Curve getCurve(String strCurve) {
        if (isStandardCurve(strCurve)) {
            return new StandardCurve(strCurve);
        }
        for (Curve c : curves) {
            if (c.name.equalsIgnoreCase(strCurve)) {
                return c;
            }
        }

        return null;
    }

    public Curve addNewCurve() {
        Curve c = new Curve();
        c.name = this.getNewName();
        curves.add(c);

        save();

        return c;
    }

    public void save() {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "curves.xml"));
            out.println("<curves>");
            for (Curve c : this.curves) {
                c.save(out);
            }
            out.println("</curves>");

            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
