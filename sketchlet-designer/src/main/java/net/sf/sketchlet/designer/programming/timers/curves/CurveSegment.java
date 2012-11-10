/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.programming.timers.curves;

/**
 * @author zobrenovic
 */
public class CurveSegment {
    public double endTime = 1.0;
    public double relativeValue = 0.0;
    public String minDuration = "";
    public String maxDuration = ""; // -1 means unlimited
    public String startAfter = "";
    public String finishBefore = ""; // -1 means unlimited

    public CurveSegment() {
    }

    public CurveSegment(double pos, double value, String minDuration, String maxDuration, String startAfter, String endBefore) {
        this.endTime = pos;
        this.relativeValue = value;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
        this.startAfter = startAfter;
        this.finishBefore = finishBefore;
    }

    public double getDistance(CurveSegment prevSegment, double time, double value) {
        double x1;
        double y1;
        if (prevSegment == null) {
            x1 = 0.0;
            y1 = 0.0;
        } else {
            x1 = prevSegment.endTime;
            y1 = prevSegment.relativeValue;
        }
        double x2 = this.endTime;
        double y2 = this.relativeValue;
        double x3 = time;
        double y3 = value;

        double u = ((x3 - x1) * (x2 - x1) + (y3 - y1) * (y2 - y1)) / ((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));

        double x = x1 + u * (x2 - x1);
        double y = y1 + u * (y2 - y1);

        return Math.sqrt((x - x3) * (x - x3) + (y - y3) * (y - y3));
    }
}
