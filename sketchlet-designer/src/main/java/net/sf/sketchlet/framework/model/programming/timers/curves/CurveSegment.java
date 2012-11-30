package net.sf.sketchlet.framework.model.programming.timers.curves;

/**
 * @author zobrenovic
 */
public class CurveSegment {
    private double endTime = 1.0;
    private double relativeValue = 0.0;
    private String minDuration = "";
    private String maxDuration = ""; // -1 means unlimited
    private String startAfter = "";
    private String finishBefore = ""; // -1 means unlimited

    public CurveSegment() {
    }

    public CurveSegment(double pos, double value, String minDuration, String maxDuration, String startAfter, String endBefore) {
        this.setEndTime(pos);
        this.setRelativeValue(value);
        this.setMinDuration(minDuration);
        this.setMaxDuration(maxDuration);
        this.setStartAfter(startAfter);
        this.setFinishBefore(getFinishBefore());
    }

    public double getDistance(CurveSegment prevSegment, double time, double value) {
        double x1;
        double y1;
        if (prevSegment == null) {
            x1 = 0.0;
            y1 = 0.0;
        } else {
            x1 = prevSegment.getEndTime();
            y1 = prevSegment.getRelativeValue();
        }
        double x2 = this.getEndTime();
        double y2 = this.getRelativeValue();
        double x3 = time;
        double y3 = value;

        double u = ((x3 - x1) * (x2 - x1) + (y3 - y1) * (y2 - y1)) / ((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));

        double x = x1 + u * (x2 - x1);
        double y = y1 + u * (y2 - y1);

        return Math.sqrt((x - x3) * (x - x3) + (y - y3) * (y - y3));
    }

    public double getEndTime() {
        return endTime;
    }

    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    public double getRelativeValue() {
        return relativeValue;
    }

    public void setRelativeValue(double relativeValue) {
        this.relativeValue = relativeValue;
    }

    public String getMinDuration() {
        return minDuration;
    }

    public void setMinDuration(String minDuration) {
        this.minDuration = minDuration;
    }

    public String getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(String maxDuration) {
        this.maxDuration = maxDuration;
    }

    public String getStartAfter() {
        return startAfter;
    }

    public void setStartAfter(String startAfter) {
        this.startAfter = startAfter;
    }

    public String getFinishBefore() {
        return finishBefore;
    }

    public void setFinishBefore(String finishBefore) {
        this.finishBefore = finishBefore;
    }
}
