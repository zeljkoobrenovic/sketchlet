package net.sf.sketchlet.framework.controller;

import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.model.Page;
import net.sf.sketchlet.framework.model.events.keyboard.KeyboardEventMacro;
import net.sf.sketchlet.framework.model.events.mouse.MouseEventMacro;
import net.sf.sketchlet.framework.model.events.overlap.RegionOverlapEventMacro;
import net.sf.sketchlet.loaders.pluginloader.WidgetPluginHandler;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.Vector;

/**
 * @author zeljko
 */
public class ActiveRegionsVariablesHelper {
    private static final Logger log = Logger.getLogger(ActiveRegionsVariablesHelper.class);
    private Page page;

    public ActiveRegionsVariablesHelper(Page page) {
        this.page = page;
    }

    public void variableUpdated(final String variableName, String variableValue, boolean playbackMode) {
        if (variableName.isEmpty()) {
            return;
        }
        if (SketchletEditor.isInPlaybackMode() && !playbackMode) {
            return;
        }
        for (ActiveRegion region : getPage().getRegions().getRegions()) {
            if (region.getMotionController().isInProcessing(variableName)) {
                continue;
            }
            int prevX1 = region.getX1Value();
            int prevY1 = region.getY1Value();
            int prevX2 = region.getX2Value();
            int prevY2 = region.getY2Value();
            double prevRotation = region.getRotationValue();
            double prevShearX = region.getShearXValue();
            double prevShearY = region.getShearYValue();

            if (region.getRenderer().getWidgetImageLayer().getWidgetPlugin() != null) {
                WidgetPluginHandler.injectWidgetPropertiesValues(region.getRenderer().getWidgetImageLayer().getWidgetPlugin());
                region.getRenderer().getWidgetImageLayer().getWidgetPlugin().variableUpdated(variableName, variableValue);
            }

            for (int i = 0; i < region.getMotionAndRotationVariablesMapping().length; i++) {
                Object ivars[] = region.getMotionController().getIgnoreRows().toArray();
                for (int iii = 0; iii < ivars.length; iii++) {
                    Integer ignoreRow = (Integer) ivars[iii];
                    if (ignoreRow.intValue() == i) {
                        continue;
                    }
                }
                String strVar = ((String) region.getMotionAndRotationVariablesMapping()[i][1]).trim();

                strVar = region.getVarPrefix() + strVar + region.getVarPostfix();

                if (!strVar.equals("") && strVar.equals(variableName)) {
                    String strDim = region.processText((String) region.getMotionAndRotationVariablesMapping()[i][0]);

                    double value;

                    try {
                        value = Double.parseDouble(variableValue);
                    } catch (Throwable e) {
                        continue;
                    }
                    double dimValue = value;
                    double dimValueRelative = value;
                    double min;
                    double max;

                    String strStart = region.processText((String) region.getMotionAndRotationVariablesMapping()[i][2]);
                    String strEnd = region.processText((String) region.getMotionAndRotationVariablesMapping()[i][3]);

                    double varLimits[];

                    double offset1;
                    double offset2;

                    String strHAlign = region.processText(region.getHorizontalAlignment());
                    String strVAlign = region.processText(region.getVerticalAlignment());
                    if (strDim.equals("position x")) {
                        int w = region.getX2Value() - region.getX1Value();

                        if (strHAlign.equalsIgnoreCase("center")) {
                            offset1 = w / 2;
                            offset2 = w / 2;
                        } else if (strHAlign.equalsIgnoreCase("right")) {
                            offset1 = w;
                            offset2 = 0;
                        } else {
                            offset1 = 0;
                            offset2 = w;
                        }
                        varLimits = region.getMotionController().getLimits(strDim, offset1, offset2);
                    } else if (strDim.equals("position y")) {
                        int h = region.getY2Value() - region.getY1Value();

                        if (strHAlign.equalsIgnoreCase("center")) {
                            offset1 = h / 2;
                            offset2 = h / 2;
                        } else if (strHAlign.equalsIgnoreCase("bottom")) {
                            offset1 = h;
                            offset2 = 0;
                        } else {
                            offset1 = 0;
                            offset2 = h;
                        }
                        varLimits = region.getMotionController().getLimits(strDim, offset1, offset2);
                    } else if (strDim.startsWith("trajectory position")) {
                        varLimits = new double[]{0.0, 1.0};
                        offset1 = 0;
                        offset2 = 0;
                    } else {
                        offset1 = 0;
                        offset2 = 0;
                        varLimits = region.getMotionController().getLimits(strDim, offset1, offset2);
                    }


                    if (varLimits == null) {
                        try {
                            double start = Double.parseDouble(strStart);
                            double end = Double.parseDouble(strEnd);

                            if (start > end) {
                                if (value > start) {
                                    value = start;
                                } else if (value < end) {
                                    value = end;
                                }
                            } else {
                                if (value < start) {
                                    value = start;
                                } else if (value > end) {
                                    value = end;
                                }
                            }

                            dimValue = value;
                            dimValueRelative = value;
                        } catch (Throwable e) {
                            dimValue = value;
                            dimValueRelative = value;
                        }
                    } else {
                        min = varLimits[0];
                        max = varLimits[1];

                        if (!Double.isNaN(min) && min > max) {
                            double temp = min;
                            min = max;
                            max = temp;
                        }

                        double start = Double.NaN, end = Double.NaN;
                        try {
                            start = Double.parseDouble(strStart);
                            end = Double.parseDouble(strEnd);
                        } catch (Throwable e) {
                        }

                        if (Double.isNaN(start) && Double.isNaN(end)) {
                            dimValue = value;
                            dimValueRelative = value;
                        } else if (!Double.isNaN(start) && Double.isNaN(end)) {
                            if (!Double.isNaN(min)) {
                                dimValue = min + value - start;
                            } else {
                                dimValue = value + start;
                            }
                            dimValueRelative = value;
                        } else if (Double.isNaN(start) && !Double.isNaN(end)) {
                            if (!Double.isNaN(max)) {
                                dimValue = Math.min(max, value);
                            } else {
                                dimValue = value;
                            }
                            dimValueRelative = value;
                        } else if (end > start) {
                            if (value < start) {
                                value = start;
                            } else if (value > end) {
                                value = end;
                            }
                            dimValue = min + (max - min) * (value - start) / (end - start);

                            dimValueRelative = (value - start) / (end - start);
                        } else if (start > end) {
                            if (value < end) {
                                value = end;
                            } else if (value > start) {
                                value = start;
                            }

                            dimValueRelative = (start - value) / (start - end);

                            if (Double.isNaN(min)) {
                                dimValue = max * dimValueRelative;
                            } else if (Double.isNaN(max)) {
                                dimValue = min - min * dimValueRelative;
                            } else {
                                dimValue = min + (max - min) * dimValueRelative;
                            }
                        }

                        if (dimValueRelative > 1) {
                            dimValueRelative = 1;
                        } else if (dimValueRelative < 0) {
                            dimValueRelative = 0;
                        }

                        if (dimValue < min) {
                            dimValue = min;
                        } else if (dimValue > max) {
                            dimValue = max;
                        }
                    }
                    if (strDim.equals("position x")) {
                        int w = region.getX2Value() - region.getX1Value();
                        region.setX1Value((int) InteractionSpace.getSketchX(dimValue));
                        if (strHAlign.equalsIgnoreCase("center")) {
                            region.setX1Value(region.getX1Value() - w / 2);
                        } else if (strHAlign.equalsIgnoreCase("right")) {
                            region.setX1Value(region.getX1Value() - w);
                        } else {
                        }
                        region.setX2Value(region.getX1Value() + w);
                        if (!region.getRegionGrouping().equals("")) {
                            for (ActiveRegion as : region.getParent().getRegions()) {
                                if (as != region && as.getRegionGrouping().equals(region.getRegionGrouping())) {
                                    as.setX1Value(as.getX1Value() + region.getX1Value() - prevX1);
                                    as.setX2Value(as.getX2Value() + region.getX2Value() - prevX2);
                                }
                            }
                        }
                    } else if (strDim.equals("position y")) {
                        int h = region.getY2Value() - region.getY1Value();
                        region.setY1Value((int) InteractionSpace.getSketchY(dimValue));
                        if (strVAlign.equalsIgnoreCase("center")) {
                            region.setY1Value(region.getY1Value() - h / 2);
                        } else if (strHAlign.equalsIgnoreCase("bottom")) {
                            region.setY1Value(region.getY1Value() - h);
                        } else {
                        }
                        region.setY2Value(region.getY1Value() + h);
                        if (!region.getRegionGrouping().equals("")) {
                            for (ActiveRegion as : region.getParent().getRegions()) {
                                if (as != region && as.getRegionGrouping().equals(region.getRegionGrouping())) {
                                    as.setY1Value(as.getY1Value() + region.getY1Value() - prevY1);
                                    as.setY2Value(as.getY2Value() + region.getY2Value() - prevY2);
                                }
                            }
                        }
                    } else if (strDim.equals("rotation")) {
                        region.setRotationValue(InteractionSpace.toRadians(dimValue));
                    } else if (strDim.equalsIgnoreCase("trajectory position")) {
                        Point p = region.getRenderer().getTrajectoryDrawingLayer().getTrajectoryPoint(dimValueRelative);
                        if (p != null) {
                            int h = region.getY2Value() - region.getY1Value();
                            int w = region.getX2Value() - region.getX1Value();
                            region.setX1Value((int) (p.x - w * region.getCenterOfRotationX()));
                            region.setY1Value((int) (p.y - h * region.getCenterOfRotationY()));
                            region.setX2Value(region.getX1Value() + w);
                            region.setY2Value(region.getY1Value() + h);
                            if (region.isChangingOrientationOnTrajectoryEnabled()) {
                                region.setRotationValue(region.getRenderer().getTrajectoryDrawingLayer().trajectoryOrientationFromPoint);
                            }
                        }
                    } else if (strDim.equalsIgnoreCase("trajectory position 2")) {
                    }

                    if (strDim.startsWith("trajectory position")) {
                        region.getMotionController().processLimits("trajectory position", dimValue, 0.0, 1.0, 0.0, 0.0, true, variableName);
                        region.getRenderer().getTrajectoryDrawingLayer().getClosestTrajectoryPoint(region.getRenderer().getTrajectoryDrawingLayer().getTrajectoryPoint(dimValue));
                        region.getMotionController().processLimits("trajectory position 2", region.getRenderer().getTrajectoryDrawingLayer().trajectoryPositionFromPoint2, 0.0, 1.0, 0.0, 0.0, true, variableName);
                    } else {
                        double _dimValue = dimValue;
                        if (strDim.equals("position x")) {
                            _dimValue = InteractionSpace.getSketchX(_dimValue);
                        } else if (strDim.equals("position y")) {
                            _dimValue = InteractionSpace.getSketchY(_dimValue);
                        }
                        region.getMotionController().processLimits(strDim, _dimValue, offset1, offset2, true, variableName);
                    }
                }
            }

            if (!region.isWithinLimits(playbackMode) || region.getInteractionController().intersectsWithSolids(playbackMode)) {
                getPage().getRegions().getOverlapHelper().findNonOverlappingLocationPlayback(region);

                region.setRotationValue(prevRotation);
                region.setShearXValue(prevShearX);
                region.setShearYValue(prevShearY);
            }
        }
    }

    public void refreshVariablesFromRegionDimensions() {
        for (ActiveRegion a : getPage().getRegions().getRegions()) {
            int w = a.getX2Value() - a.getX1Value();

            String strHAlign = a.processText(a.getHorizontalAlignment());
            String strVAlign = a.processText(a.getVerticalAlignment());

            if (strHAlign.equalsIgnoreCase("center")) {
                a.getMotionController().processLimits("position x", a.getX1Value() + w / 2, w / 2, w / 2, true);
            } else if (strHAlign.equalsIgnoreCase("right")) {
                a.getMotionController().processLimits("position x", a.getX1Value() + w, w, 0, true);
            } else {
                a.getMotionController().processLimits("position x", a.getX1Value(), 0, w, true);
            }

            if (a.getX2Value() - a.getX1Value() < w) {
                a.setX1Value(a.getX2Value() - w);
            }

            int h = a.getY2Value() - a.getY1Value();

            if (strVAlign.equalsIgnoreCase("center")) {
                a.getMotionController().processLimits("position y", a.getY1Value() + h / 2, h / 2, h / 2, true);
            } else if (strVAlign.equalsIgnoreCase("bottom")) {
                a.getMotionController().processLimits("position y", a.getY1Value() + h, h, 0, true);
            } else {
                a.getMotionController().processLimits("position y", a.getY1Value(), 0, h, true);
            }

            if (a.getY2Value() - a.getY1Value() < h) {
                a.setY1Value(a.getY2Value() - h);
            }

            a.getMotionController().processLimits("rotation", InteractionSpace.toPhysicalAngle(a.getRotationValue()), 0.0, 0.0, true);
        }
    }

    public void refreshRegionDimensionsFromVariables() {
        for (ActiveRegion a : getPage().getRegions().getRegions()) {
            for (int i = 0; i
                    < a.getMotionAndRotationVariablesMapping().length; i++) {
                String strVar = ((String) a.getMotionAndRotationVariablesMapping()[i][1]).trim();

                strVar = a.getVarPrefix() + strVar + a.getVarPostfix();

                if (!strVar.equals("")) {
                    String varValue = VariablesBlackboard.getInstance().getVariableValue(strVar);

                    if (varValue == null || varValue.equals("")) {
                        continue;
                    }

                    variableUpdated(strVar, varValue, false);
                }

            }
        }
    }

    public Vector<String> getVariables() {
        Vector<String> variables = new Vector<String>();
        for (ActiveRegion region : getPage().getRegions().getRegions()) {
            String strValue;
            for (int i = 0; i < region.getMotionAndRotationVariablesMapping().length; i++) {
                strValue = (String) region.getMotionAndRotationVariablesMapping()[i][1];

                if (strValue.length() > 0) {
                    variables.add(strValue);
                }
            }
            for (MouseEventMacro mouseEventMacro : region.getMouseEventsProcessor().getMouseEventMacros()) {
                for (int i = 0; i < mouseEventMacro.getMacro().getActions().length; i++) {
                    String strAction = (String) mouseEventMacro.getMacro().getActions()[i][0];
                    strValue = (String) mouseEventMacro.getMacro().getActions()[i][1];
                    String strContent = (String) mouseEventMacro.getMacro().getActions()[i][2];
                    if (strAction.toLowerCase().startsWith("variable") && strContent.length() > 0) {
                        variables.add(strValue);
                    }
                }
            }
            for (KeyboardEventMacro keyboardEventMacro : region.getKeyboardEventsProcessor().getKeyboardEventMacros()) {
                for (int i = 0; i < keyboardEventMacro.getMacro().getActions().length; i++) {
                    String strAction = (String) keyboardEventMacro.getMacro().getActions()[i][0];
                    strValue = (String) keyboardEventMacro.getMacro().getActions()[i][1];
                    String strContent = (String) keyboardEventMacro.getMacro().getActions()[i][2];
                    if (strAction.toLowerCase().startsWith("variable") && strContent.length() > 0) {
                        variables.add(strValue);
                    }
                }
            }
            for (RegionOverlapEventMacro regionOverlapEventMacro : region.getRegionOverlapEventMacros()) {
                for (int i = 0; i < regionOverlapEventMacro.getMacro().getActions().length; i++) {
                    String strAction = (String) regionOverlapEventMacro.getMacro().getActions()[i][0];
                    strValue = (String) regionOverlapEventMacro.getMacro().getActions()[i][1];
                    String strContent = (String) regionOverlapEventMacro.getMacro().getActions()[i][2];
                    if (strAction.toLowerCase().startsWith("variable") && strContent.length() > 0) {
                        variables.add(strValue);
                    }
                }
            }
        }
        return variables;
    }

    public void createNewVariables() {
        Vector<String> variables = getVariables();

        for (String strVar : variables) {
            if (strVar != null) {
                if (!mayBeFormula(strVar) && VariablesBlackboard.getInstance().getVariable(strVar) == null && !strVar.startsWith("[")) {
                    VariablesBlackboard.getInstance().updateVariable(strVar, " ");
                }
            }
        }
    }

    public boolean mayBeFormula(String strVar) {
        if (strVar == null) {
            return false;
        }
        strVar = strVar.trim();
        return strVar.contains(" ") || strVar.contains("-") || strVar.contains("+") || strVar.contains("^") || strVar.contains("/") || strVar.contains("*") || strVar.contains("(") || strVar.contains(")") || strVar.contains("&&") || strVar.contains("||") || strVar.contains("%");
    }

    public Page getPage() {
        return page;
    }
}

