package net.sf.sketchlet.framework.model.programming.timers.curves;

/**
 * @author zobrenovic
 */
public class StiffnessSegment {

    private double endTime = 1.0;
    private String minDuration = "";
    private String maxDuration = ""; // -1 means unlimited

    public StiffnessSegment() {
    }

    public StiffnessSegment(double pos, String minDuration, String maxDuration) {
        this.setEndTime(pos);
        this.setMinDuration(minDuration);
        this.setMaxDuration(maxDuration);
    }

    public double getValue(double value) {
        if (!getMinDuration().isEmpty()) {
            try {
                double min = Double.parseDouble(getMinDuration());
                if (value < min) {
                    value = min;
                }
            } catch (Exception e) {
            }
        }
        if (!getMaxDuration().isEmpty()) {
            try {
                double max = Double.parseDouble(getMaxDuration());
                if (value > max) {
                    value = max;
                }
            } catch (Exception e) {
            }
        }
        return value;
    }

    public double getEndTime() {
        return endTime;
    }

    public void setEndTime(double endTime) {
        this.endTime = endTime;
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
}
