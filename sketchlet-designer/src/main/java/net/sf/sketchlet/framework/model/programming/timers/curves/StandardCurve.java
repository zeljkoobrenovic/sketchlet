package net.sf.sketchlet.framework.model.programming.timers.curves;

/**
 * @author zobrenovic
 */
public class StandardCurve extends Curve {

    private String strFunction = "";

    public StandardCurve(String strFunction) {
        this.strFunction = strFunction;
    }

    public double getRelativeValue(double relIndex) {
        return getRelativeValue(0, relIndex);
    }

    public double getRelativeValue(double duration, double relIndex) {

        if (strFunction.equalsIgnoreCase("linear")) {
            return relIndex;
        } else if (strFunction.equalsIgnoreCase("accelerate")) {
            double x = relIndex * 6 - 6;
            return 2 / (1 + Math.exp(-x));
        } else if (strFunction.equalsIgnoreCase("decelerate")) {
            double x = relIndex * 6;
            return 2 * (1 / (1 + Math.exp(-x)) - 0.5);
        } else if (strFunction.equalsIgnoreCase("accelerate + decelerate")) {
            double x = relIndex * 12 - 6;
            return 1 / (1 + Math.exp(-x));
        }

        return relIndex;
    }
}
