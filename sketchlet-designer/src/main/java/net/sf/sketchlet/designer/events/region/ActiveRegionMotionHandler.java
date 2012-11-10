/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.events.region;

import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.designer.programming.macros.Commands;
import org.apache.log4j.Logger;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class ActiveRegionMotionHandler {
    private static final Logger log = Logger.getLogger(ActiveRegionMotionHandler.class);

    ActiveRegion region;
    public boolean updatingVariables = false;
    String updateVariableName = "";
    Hashtable<String, String> variablesInProgress = new Hashtable<String, String>();
    public boolean bUpdating = false;

    public ActiveRegionMotionHandler(ActiveRegion region) {
        this.region = region;
    }

    public void dispose() {
        region = null;
    }

    public double[] getLimits(String dimension, double offset1, double offset2) {
        int limitRow = -1;

        for (int i = 0; i < region.limits.length; i++) {
            String dim = (String) region.limits[i][0];

            if (dim.equalsIgnoreCase(dimension)) {
                limitRow = i;
                break;
            }
        }

        if (limitRow == -1) {
            return null;
        }

        String strRowMin = (String) region.limits[limitRow][1];
        String strRowMax = (String) region.limits[limitRow][2];

        if (dimension.equals("position x")) {
            if (strRowMin.equals("")) {
                strRowMin = "" + InteractionSpace.left;
            }
            if (strRowMax.equals("")) {
                strRowMax = "" + InteractionSpace.right;
            }
            offset1 = InteractionSpace.getPhysicalWidth(offset1);
            offset2 = InteractionSpace.getPhysicalWidth(offset2);
        } else if (dimension.equals("position y")) {
            if (strRowMin.equals("")) {
                strRowMin = "" + InteractionSpace.top;
            }
            if (strRowMax.equals("")) {
                strRowMax = "" + InteractionSpace.bottom;
            }
            offset1 = InteractionSpace.getPhysicalHeight(offset1);
            offset2 = InteractionSpace.getPhysicalHeight(offset2);
        } else if (dimension.equals("rotation")) {
            if (strRowMin.equals("")) {
                strRowMin = "" + InteractionSpace.angleStart;
            }
            if (strRowMax.equals("")) {
                strRowMax = "" + InteractionSpace.angleEnd;
            }
        }

        double min = Double.NaN;
        double max = Double.NaN;
        String strMin = region.processText(strRowMin);
        String strMax = region.processText(strRowMax);
        if (!strMin.equals("")) {
            try {
                min = Double.parseDouble(strMin) + offset1;
            } catch (Exception en) {
            }
        }

        if (!strMax.equals("")) {
            try {
                max = Double.parseDouble(strMax) - offset2;
            } catch (Exception en) {
            }
        }

        if (Double.isNaN(min) || Double.isNaN(max)) {
            return null;
        }

        return new double[]{min, max};
    }

    public double processLimits(String dimension, double value, double offset1, double offset2, boolean updateVar) {
        return processLimits(dimension, value, offset1, offset2, updateVar, "");
    }

    public double processLimits(String dimension, double value, double offset1, double offset2, boolean updateVar, String ignoreVar) {
        double original = value;
        if (dimension.equals("position x")) {
            value = InteractionSpace.getPhysicalX(value);
            offset1 = InteractionSpace.getPhysicalWidth(offset1);
            offset2 = InteractionSpace.getPhysicalWidth(offset2);
        } else if (dimension.equals("position y")) {
            value = InteractionSpace.getPhysicalY(value);
            offset1 = InteractionSpace.getPhysicalHeight(offset1);
            offset2 = InteractionSpace.getPhysicalHeight(offset2);
        }

        int limitRow = -1;
        for (int i = 0; i < region.limits.length; i++) {
            String dim = (String) region.limits[i][0];

            if (dim.equalsIgnoreCase(dimension)) {
                limitRow = i;
                break;
            }
        }

        if (limitRow >= 0 && limitRow < region.limits.length) {
            double min = Double.NaN;
            double max = Double.NaN;
            String strMin = region.processText((String) region.limits[limitRow][1]);
            String strMax = region.processText((String) region.limits[limitRow][2]);
            String _strMin = strMin;
            String _strMax = strMax;

            if (strMin.equals("")) {
                if (dimension.equals("position x")) {
                    strMin = InteractionSpace.left + "";
                } else if (dimension.equals("position y")) {
                    strMin = InteractionSpace.top + "";
                } else if (dimension.equals("rotation")) {
                    strMin = InteractionSpace.angleStart + "";
                }
            }

            if (!strMin.equals("")) {
                try {
                    min = Double.parseDouble(strMin) + offset1;
                    if (value < min) {
                        value = min;
                    }
                } catch (Exception en) {
                }
            }

            if (strMax.equals("")) {
                if (dimension.equals("position x")) {
                    strMax = InteractionSpace.right + "";
                } else if (dimension.equals("position y")) {
                    strMax = InteractionSpace.bottom + "";
                } else if (dimension.equals("rotation")) {
                    strMax = InteractionSpace.angleEnd + "";
                }
            }

            if (!strMax.equals("")) {
                try {
                    max = Double.parseDouble(strMax) - offset2;
                    if (value > max) {
                        value = max;
                    }
                } catch (Exception en) {
                }
            }

            double result = processLimits(dimension, value, min, max, offset1, offset2, updateVar, ignoreVar);

            if (dimension.equals("position x")) {
                if (!region.bWalkThrough && _strMin.isEmpty() && _strMax.isEmpty()) {
                    return original;
                } else {
                    result = InteractionSpace.getSketchX(result);
                }
            } else if (dimension.equals("position y")) {
                if (!region.bWalkThrough && _strMin.isEmpty() && _strMax.isEmpty()) {
                    return original;
                } else {
                    result = InteractionSpace.getSketchY(result);
                }
            }
            return result;
        } else {
            return value;
        }

    }

    public double processLimits(String dimension, double value, double min, double max, double offset1, double offset2, boolean updateVar) {
        return processLimits(dimension, value, min, max, offset1, offset2, updateVar, "");
    }

    public double processLimits(String dimension, double value, double min, double max, double offset1, double offset2, boolean updateVar, String ignoreVar) {
        double _value = value;

        for (int i = 0; i < region.updateTransformations.length; i++) {
            String dim = (String) region.updateTransformations[i][0];
            String var = (String) region.updateTransformations[i][1];

            if (!var.equals("") && dim.equalsIgnoreCase(dimension) && !var.equalsIgnoreCase(ignoreVar.trim())) {
                _value = processLimits(i, _value, min, max, offset1, offset2, updateVar);
            }
        }
        return _value;
    }

    int nnnn = 0;
    public Vector<Integer> ignoreRows = new Vector<Integer>();

    public void updateAssociatedVariables(String dimension, double value) {
        value = checkForLimits(dimension, value);

        for (int i = 0; i < region.updateTransformations.length; i++) {
            String strDim = region.processText((String) region.updateTransformations[i][0]);

            if (dimension.equalsIgnoreCase(strDim)) {
                String strVariable = region.processText((String) region.updateTransformations[i][1]);
                String strStart = region.processText((String) region.updateTransformations[i][2]);
                String strEnd = region.processText((String) region.updateTransformations[i][3]);
                String strFormat = region.processText((String) region.updateTransformations[i][4]);

                double limits[] = getLimits(dimension);

                double start = getDouble(strStart);
                double end = getDouble(strEnd);

                if (limits != null) {
                    if (start != Double.NaN && end != Double.NaN) {
                        value = ((value - limits[0]) / (limits[1] - limits[0])) * (end - start);
                    } else {
                        if (!Double.isNaN(start)) {
                            value = Math.min(value, start);
                        }
                        if (!Double.isNaN(end)) {
                            value = Math.max(value, end);
                        }
                    }
                } else {
                    if (!Double.isNaN(start)) {
                        value = Math.min(value, start);
                    }
                    if (!Double.isNaN(end)) {
                        value = Math.max(value, end);
                    }
                }
                updateVariableFormated(strVariable, value, strFormat);
            }
        }
    }

    public void updateVariableFormated(String strVar, double value, String strFormat) {
        String strNumber = "";
        try {
            if (strFormat.length() > 0) {
                DecimalFormat df = new DecimalFormat(strFormat, new DecimalFormatSymbols(Locale.US));
                strNumber = df.format(value);
            } else {
                strNumber = "" + value;
            }
        } catch (Exception e) {
            log.error(e);
        }

        Commands.updateVariableOrProperty(this.region, strVar, strNumber, Commands.ACTION_VARIABLE_UPDATE, true);
    }

    public double getDouble(String strValue) {
        try {
            return Double.parseDouble(strValue);
        } catch (Exception e) {
        }

        return Double.NaN;
    }

    public double[] getLimits(String dimension) {
        double[] limits = new double[]{Double.NaN, Double.NaN};

        for (int i = 0; i < region.limits.length; i++) {
            String dim = (String) region.limits[i][0];

            if (dim.equalsIgnoreCase(dimension)) {
                String min = region.processText((String) region.limits[i][1]);
                String max = region.processText((String) region.limits[i][2]);
                try {
                    limits[0] = Double.parseDouble(min);
                } catch (Exception e) {
                    return null;
                }
                try {
                    limits[1] = Double.parseDouble(max);
                } catch (Exception e) {
                    return null;
                }

                return limits;
            }
        }

        return null;
    }

    public double checkForLimits(String dimension, double value) {
        int limitRow = -1;
        for (int i = 0; i < region.limits.length; i++) {
            String dim = (String) region.limits[i][0];

            if (dim.equalsIgnoreCase(dimension)) {
                limitRow = i;
                break;
            }
        }

        if (limitRow >= 0 && limitRow < region.limits.length) {
            double min = Double.NaN;
            double max = Double.NaN;
            String strMin = region.processText((String) region.limits[limitRow][1]);
            String strMax = region.processText((String) region.limits[limitRow][2]);

            if (strMin.isEmpty() && strMax.isEmpty()) {
                return value;
            }

            double offset1 = 0.0;
            double offset2 = 0.0;
            if (dimension.equalsIgnoreCase("position x")) {
                if (region.strHAlign.equalsIgnoreCase("left")) {
                    offset1 = 0.0;
                    offset2 = region.getWidth();
                } else if (region.strHAlign.equalsIgnoreCase("center")) {
                    offset1 = -region.getWidth() / 2;
                    offset2 = region.getWidth() / 2;
                } else if (region.strHAlign.equalsIgnoreCase("right")) {
                    offset1 = -region.getWidth();
                    offset2 = 0.0;
                }
            } else if (dimension.equalsIgnoreCase("position y")) {
                if (region.strVAlign.equalsIgnoreCase("top")) {
                    offset1 = 0.0;
                    offset2 = region.getHeight();
                } else if (region.strVAlign.equalsIgnoreCase("center")) {
                    offset1 = -region.getHeight() / 2;
                    offset2 = region.getHeight() / 2;
                } else if (region.strVAlign.equalsIgnoreCase("bottom")) {
                    offset1 = -region.getHeight();
                    offset2 = 0.0;
                }
            }

            if (!strMin.isEmpty()) {
                try {
                    min = Double.parseDouble(strMin);
                    if (value + offset1 < min) {
                        value = min - offset1;
                    }
                } catch (Exception e) {
                }
            }

            if (!strMax.isEmpty()) {
                try {
                    max = Double.parseDouble(strMax);
                    if (value + offset2 > max) {
                        value = max + offset2;
                    }
                } catch (Exception e) {
                }
            }
        }

        return value;
    }

    public double processLimits(int row, double value, double min, double max, double offset1, double offset2, boolean updateVar) {
        String updateVariable = region.processText((String) region.updateTransformations[row][1]);

        if (updateVariable.equals("")) {
            return value;
        }

        double start = Double.NaN;
        double end = Double.NaN;

        String strStart = region.processText((String) region.updateTransformations[row][2]);
        String strEnd = region.processText((String) region.updateTransformations[row][3]);
        String strFormat = region.processText((String) region.updateTransformations[row][4]);

        double _value = value;

        if (!strStart.equals("")) {
            try {
                start = Double.parseDouble(strStart);

                if (!strEnd.equals("")) {
                    end = Double.parseDouble(strEnd);
                    if (!Double.isNaN(min) && !Double.isNaN(max)) {
                        if (max >= min) {
                            if (end >= start) {
                                _value = start + (value - min) * (Math.abs(end - start) / Math.abs(max - min));
                            } else {
                                _value = start - (value - min) * (Math.abs(end - start) / Math.abs(max - min));
                            }
                        } else {
                            if (end >= start) {
                                _value = start + (min - value) * (Math.abs(end - start) / Math.abs(max - min));
                            } else {
                                _value = start - (min - value) * (Math.abs(end - start) / Math.abs(max - min));
                            }
                        }
                    } else {
                        if (value < Math.min(start, end)) {
                            _value = start;
                        }
                        if (value > Math.max(start, end)) {
                            _value = end;
                        }
                    }
                } else {
                    if (max >= min) {
                        _value = start + value - min;
                    } else {
                        _value = start + (min - value);
                    }
                }
            } catch (Exception en) {
            }
        } else if (!Double.isNaN(min) && !Double.isNaN(max)) {
            if (value < Math.min(min, max)) {
                _value = min;
            }
            if (value > Math.max(min, max)) {
                _value = max;
            }
        } else {
        }

        if (updateVar && !updateVariable.equals("")) {
            String strNumber;

            try {
                if (strFormat.length() > 0) {
                    DecimalFormat df = new DecimalFormat(strFormat, new DecimalFormatSymbols(Locale.US));
                    strNumber = df.format(_value);
                } else {
                    strNumber = "" + _value;
                }

                if (!updateVariable.equals("")) {
                    Integer intValue = new Integer(row);
                    ignoreRows.add(intValue);
                    protectVariablesByName.add(updateVariable);
                    Commands.updateVariableOrProperty(this.region, region.getVarPrefix() + updateVariable + region.getVarPostfix(), strNumber, Commands.ACTION_VARIABLE_UPDATE, true);
                    protectVariablesByName.remove(updateVariable);
                    ignoreRows.add(intValue);
                }
            } catch (Exception e) {
                log.error(e);
            }
        }

        return value;
    }

    public Vector<Integer> protectVariables = new Vector<Integer>();
    public Vector<String> protectVariablesByName = new Vector<String>();

    public boolean isInProcessing(String strVar) {
        return this.protectVariablesByName.contains(strVar);
    }
}
