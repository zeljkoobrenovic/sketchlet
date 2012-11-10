/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.programming.timers.curves;

/**
 * @author zobrenovic
 */
public class StiffnessSegment {

    public double endTime = 1.0;
    public String minDuration = "";
    public String maxDuration = ""; // -1 means unlimited

    public StiffnessSegment() {
    }

    public StiffnessSegment(double pos, String minDuration, String maxDuration) {
        this.endTime = pos;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
    }

    public double getValue(double value) {
        if (!minDuration.isEmpty()) {
            try {
                double min = Double.parseDouble(minDuration);
                if (value < min) {
                    value = min;
                }
            } catch (Exception e) {
            }
        }
        if (!maxDuration.isEmpty()) {
            try {
                double max = Double.parseDouble(maxDuration);
                if (value > max) {
                    value = max;
                }
            } catch (Exception e) {
            }
        }
        return value;
    }
}
