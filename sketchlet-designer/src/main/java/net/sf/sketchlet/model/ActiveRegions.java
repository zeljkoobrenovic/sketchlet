/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.model;

import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.SketchletEditorMode;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionsFrame;
import net.sf.sketchlet.loaders.pluginloader.WidgetPluginHandler;
import net.sf.sketchlet.model.events.region.ActiveRegionMouseHandler;
import net.sf.sketchlet.model.events.MouseEventMacro;
import net.sf.sketchlet.model.events.RegionOverlapEventMacro;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.util.Vector;

public class ActiveRegions {

    private Vector<ActiveRegion> regions = new Vector<ActiveRegion>();
    private Vector<ActiveRegion> selectedRegions = null;
    private Page page;
    private Point mousePressedPoint;
    private boolean newRegion = false;

    public ActiveRegions(Page page) {
        this.setPage(page);
    }

    public ActiveRegions(ActiveRegions ars, Page page) {
        this(page);
        long lastTime = System.currentTimeMillis();
        for (ActiveRegion reg : ars.getRegions()) {
            while (lastTime == System.currentTimeMillis()) {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                }
            }
            this.getRegions().add(new ActiveRegion(reg, this, true));
            lastTime = System.currentTimeMillis();
        }
    }

    public ActiveRegion getLastSelectedRegion() {
        if (this.getSelectedRegions() != null && this.getSelectedRegions().size() > 0) {
            return getSelectedRegions().lastElement();
        }
        return null;
    }

    public ActiveRegions cloneEmbedded(Page page) {
        ActiveRegions _actions = new ActiveRegions(page);
        for (ActiveRegion reg : getRegions()) {
            _actions.getRegions().add(new ActiveRegion(reg, _actions, false));
        }

        return _actions;
    }

    public ActiveRegion getRegionByNumber(int num) {
        if (num <= this.getRegions().size()) {
            return getRegions().elementAt(getRegions().size() - num);
        }
        return null;
    }

    public ActiveRegion getRegionByName(String name) {
        for (ActiveRegion region : getRegions()) {
            if (region.getName().equalsIgnoreCase(name)) {
                return region;
            }
        }
        for (ActiveRegion region : getRegions()) {
            if (region.getNumber().equalsIgnoreCase(name)) {
                return region;
            }
        }
        return null;
    }

    public ActiveRegion getRegionByImageFileName(String name) {
        for (ActiveRegion region : getRegions()) {
            if (region.getDrawImageFileName().equalsIgnoreCase(name)) {
                return region;
            }
        }
        return null;
    }

    public ActiveRegion getRegionById(String id) {
        for (ActiveRegion region : getRegions()) {
            if (region.getId().equalsIgnoreCase(id)) {
                return region;
            }
        }
        return null;
    }

    public int getActionIndex(String imageFile) {
        int i = 0;
        for (ActiveRegion a : getRegions()) {
            if (a.getDrawImageFileName(0).equals(imageFile)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private int offset_x = 0;
    private int offset_y = 0;

    public void draw(Graphics2D g2, Component component, SketchletEditorMode mode, boolean bPlayback, boolean bHighlightRegions, float transparency) {
        int offset[] = getPage().getRegionsOffset(bPlayback);
        setOffset_x(offset[0]);
        setOffset_y(offset[1]);
        for (int i = getRegions().size() - 1; i >= 0; i--) {
            ActiveRegion reg = getRegions().elementAt(i);
            if (reg.isActive(bPlayback)) {
                reg.getRenderer().draw(g2, component, mode, bPlayback, bHighlightRegions, transparency);
            }
        }
    }

    public void draw(int layer, Graphics2D g2, Component component, SketchletEditorMode mode, boolean bPlayback, boolean bHighlightRegions, float transparency) {
        int offset[] = getPage().getRegionsOffset(bPlayback);
        setOffset_x(offset[0]);
        setOffset_y(offset[1]);
        for (int i = getRegions().size() - 1; i >= 0; i--) {
            ActiveRegion reg = getRegions().elementAt(i);
            if (reg.isActive(bPlayback) && reg.layer == layer) {
                reg.getRenderer().draw(g2, component, mode, bPlayback, bHighlightRegions, transparency);
            }
        }
    }

    public void removeFromSelection(ActiveRegion region) {
        if (getSelectedRegions() != null && region != null && getSelectedRegions().contains(region)) {
            getSelectedRegions().remove(region);
        }
    }

    public void addToSelection(ActiveRegion region) {
        if (region != null) {
            if (getSelectedRegions() == null) {
                setSelectedRegions(new Vector<ActiveRegion>());
            }
            String strGroup = region.regionGrouping.trim();
            if (!strGroup.equals("")) {
                for (ActiveRegion reg : getRegions()) {
                    if (reg.regionGrouping.equals(strGroup) && reg != region && !getSelectedRegions().contains(reg)) {
                        getSelectedRegions().add(reg);
                    }
                }
            }
            if (getSelectedRegions() != null && region != null && getSelectedRegions().contains(region)) {
                getSelectedRegions().remove(region);
            }
            getSelectedRegions().add(region);
        }
    }

    public void mousePressed(MouseEvent e, double scale, JFrame frame, boolean bPlayback, boolean bAddNew) {
        int marginX = bPlayback ? 0 : SketchletEditor.getInstance().getMarginX();
        int marginY = bPlayback ? 0 : SketchletEditor.getInstance().getMarginY();
        int x = (int) ((e.getPoint().x) / scale) - marginX;
        int y = (int) ((e.getPoint().y) / scale) - marginY;

        mousePressed(x, y, e.getModifiers(), e.getWhen(), scale, e, frame, bPlayback, bAddNew);
    }

    public void mousePressed(int x, int y, int modifiers, long when, double scale, MouseEvent e, JFrame frame, boolean bPlayback, boolean bAddNew) {
        ActiveRegion reg = selectRegion(x, y, bPlayback);
        setMousePressedPoint(new Point(x, y));

        if (reg == null || bAddNew) {
            if (bAddNew) {
                setSelectedRegions(new Vector<ActiveRegion>());
                if ((modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                    setNewRegion(true);
                    reg = new ActiveRegion(this, x, y, x + 50, y + 20);
                    reg.layer = SketchletEditor.getInstance().getLayer();
                    if (SketchletEditor.getInitProperties() != null) {
                        for (int i = 0; i < SketchletEditor.getInitProperties().length; i++) {
                            String strProp = SketchletEditor.getInitProperties()[i][0];
                            if (!strProp.equalsIgnoreCase("x1") && !strProp.equalsIgnoreCase("y1") && !strProp.equalsIgnoreCase("x2") && !strProp.equalsIgnoreCase("y2") && !strProp.equalsIgnoreCase("position x") && !strProp.equalsIgnoreCase("position y")) {
                                reg.setProperty(strProp, SketchletEditor.getInitProperties()[i][1]);
                            }
                        }
                    }
                    addToSelection(reg);
                    reg.getMouseHandler().setSelectedCorner(ActiveRegionMouseHandler.BOTTOM_RIGHT);
                    getRegions().insertElementAt(reg, 0);
                    reg.activate(bPlayback);
                }
            } else {
                setSelectedRegions(null);
            }
        } else {
            if (getSelectedRegions() == null) {
                setSelectedRegions(new Vector<ActiveRegion>());
                addToSelection(reg);
            } else if (SketchletEditor.getInstance().isInShiftMode() && !getSelectedRegions().contains(reg)) {
                addToSelection(reg);
            } else if (SketchletEditor.getInstance().isInShiftMode() && getSelectedRegions().contains(reg)) {
                removeFromSelection(reg);
            } else if (!SketchletEditor.getInstance().isInShiftMode() && !getSelectedRegions().contains(reg)) {
                setSelectedRegions(new Vector<ActiveRegion>());
                addToSelection(reg);
            } else {
                getSelectedRegions().remove(reg);
                addToSelection(reg);
            }

            reg.getMouseHandler().mousePressed(x, y, modifiers, when, e, frame, bPlayback);
            ActiveRegionsFrame.refresh(reg);
            SketchletEditor.getInstance().repaint();
        }

        SketchletEditor.getInstance().getActiveRegionMenu().setEnabled(isRegionSelected());
        SketchletEditor.getInstance().getFormulaToolbar().enableControls(isRegionSelected(), this.isConnectorSelected());
    }

    public void deselectRegions() {
        setSelectedRegions(null);
        SketchletEditor.getInstance().getActiveRegionMenu().setEnabled(isRegionSelected());
        SketchletEditor.getInstance().getFormulaToolbar().enableControls(isRegionSelected(), this.isConnectorSelected());
        SketchletEditor.getInstance().getFormulaToolbar().refresh();
    }

    public boolean isRegionSelected() {
        return getSelectedRegions() != null && getSelectedRegions().size() > 0;
    }

    public boolean isConnectorSelected() {
        return SketchletEditor.getInstance() != null && SketchletEditor.getInstance().getCurrentPage().getSelectedConnector() != null;
    }

    public void mouseDragged(MouseEvent e, double scale, JFrame frame, boolean bPlayback) {
        if (getSelectedRegions() != null && getSelectedRegions().size() > 0) {
            // for (Action a : selectedActions) {
            getSelectedRegions().lastElement().getMouseHandler().mouseDragged(e, scale, frame, bPlayback);
            //}
        }
    }

    public void mouseDragged(int x, int y, int modifiers, long when, double scale, MouseEvent e, JFrame frame, boolean bPlayback) {
        if (getSelectedRegions() != null && getSelectedRegions().size() > 0) {
            // for (Action a : selectedActions) {
            getSelectedRegions().lastElement().getMouseHandler().mouseDragged(x, y, modifiers, when, scale, e, frame, bPlayback);
            //}
        }
    }

    public void mouseReleased(MouseEvent e, double scale, JFrame frame, boolean bPlayback) {
        int marginX = bPlayback ? 0 : SketchletEditor.getInstance().getMarginX();
        int marginY = bPlayback ? 0 : SketchletEditor.getInstance().getMarginY();
        int x = (int) ((e.getPoint().x) / scale) - marginX;
        int y = (int) ((e.getPoint().y) / scale) - marginY;

        mouseReleased(x, y, e.getModifiers(), e.getWhen(), scale, e, frame, bPlayback);
    }

    private boolean bNoEffect = false;

    public void mouseReleased(int x, int y, int modifiers, long when, double scale, MouseEvent e, JFrame frame, boolean bPlayback) {
        setbNoEffect(false);
        if (getSelectedRegions() != null && getSelectedRegions().size() > 0) {
            if (getSelectedRegions().lastElement().getWidth() > 0 || (getMousePressedPoint() != null && !(Math.abs(getMousePressedPoint().x - x) <= 0 && Math.abs(getMousePressedPoint().y - y) <= 0))) {
                if (isNewRegion()) {
                    ActiveRegionsFrame.reload(getSelectedRegions().lastElement());
                }

                getSelectedRegions().lastElement().getMouseHandler().mouseReleased(x, y, modifiers, when, e, frame, bPlayback);
            } else {
                if (isNewRegion()) {
                    for (ActiveRegion as : getSelectedRegions()) {
                        as.deactivate(bPlayback);
                        getRegions().remove(as);
                    }
                    setSelectedRegions(null);
                }

                setbNoEffect(true);
            }

            SketchletEditor.getInstance().repaint();

            setMousePressedPoint(null);
        }
        setNewRegion(false);
    }

    public ActiveRegion selectRegion(int x, int y, boolean bPlayback) {
        if (!this.getPage().isbLayerRegions()) {
            return null;
        }
        for (ActiveRegion a : getRegions()) {
            if (a.isActive(bPlayback) && a.getMouseHandler().inRect(x, y, bPlayback)) {
                return a;
            }
        }

        return null;
    }

    public boolean isAffected(String strVariable) {
        for (ActiveRegion a : getRegions()) {
            if (a.isAffected(strVariable)) {
                return true;
            }
        }

        return false;
    }

    public void save(PrintWriter out) {
        if (getRegions().size() > 0) {
            out.println("<active-regions>");
            for (ActiveRegion a : getRegions()) {
                a.save(out);
            }
            out.println("</active-regions>");
        }
    }

    public void changePerformed(final String triggerVariable, String varValue, boolean bPlayback) {
        if (triggerVariable.isEmpty()) {
            return;
        }
        if (SketchletEditor.isInPlaybackMode() && !bPlayback) {
            return;
        }
        for (ActiveRegion region : getRegions()) {
            if (region.getMotionHandler().isInProcessing(triggerVariable)) {
                continue;
            }
            int prevX1 = (bPlayback) ? region.playback_x1 : region.x1;
            int prevY1 = (bPlayback) ? region.playback_y1 : region.y1;
            int prevX2 = (bPlayback) ? region.playback_x2 : region.x2;
            int prevY2 = (bPlayback) ? region.playback_y2 : region.y2;
            double prevRotation = (bPlayback) ? region.playback_rotation : region.rotation;
            double prevShearX = (bPlayback) ? region.playback_shearX : region.shearX;
            double prevShearY = (bPlayback) ? region.playback_shearY : region.shearY;

            if (region.getRenderer().getWidgetImageLayer().getWidgetPlugin() != null) {
                WidgetPluginHandler.injectWidgetPropertiesValues(region.getRenderer().getWidgetImageLayer().getWidgetPlugin());
                region.getRenderer().getWidgetImageLayer().getWidgetPlugin().variableUpdated(triggerVariable, varValue);
            }

            for (int i = 0; i < region.updateTransformations.length; i++) {
                Object ivars[] = region.getMotionHandler().getIgnoreRows().toArray();
                for (int iii = 0; iii < ivars.length; iii++) {
                    Integer ignoreRow = (Integer) ivars[iii];
                    if (ignoreRow.intValue() == i) {
                        continue;
                    }
                }
                String strVar = ((String) region.updateTransformations[i][1]).trim();

                strVar = region.getVarPrefix() + strVar + region.getVarPostfix();

                if (!strVar.equals("") && strVar.equals(triggerVariable)) {
                    String strDim = region.processText((String) region.updateTransformations[i][0]);

                    double value = 0.0;

                    try {
                        value = Double.parseDouble(varValue);
                    } catch (Throwable e) {
                        continue;
                    }
                    double dimValue = value;
                    double dimValueRelative = value;
                    double min;
                    double max;

                    String strStart = region.processText((String) region.updateTransformations[i][2]);
                    String strEnd = region.processText((String) region.updateTransformations[i][3]);

                    double varLimits[] = null;

                    double offset1 = 0.0;
                    double offset2 = 0.0;

                    String strHAlign = region.processText(region.horizontalAlignment);
                    String strVAlign = region.processText(region.verticalAlignment);
                    if (strDim.equals("position x")) {
                        int w = bPlayback ? region.playback_x2 - region.playback_x1 : region.x2 - region.x1;

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
                        varLimits = region.getMotionHandler().getLimits(strDim, offset1, offset2);
                    } else if (strDim.equals("position y")) {
                        int h = bPlayback ? region.playback_y2 - region.playback_y1 : region.y2 - region.y1;

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
                        varLimits = region.getMotionHandler().getLimits(strDim, offset1, offset2);
                    } else if (strDim.startsWith("trajectory position")) {
                        varLimits = new double[]{0.0, 1.0};
                        offset1 = 0;
                        offset2 = 0;
                    } else {
                        offset1 = 0;
                        offset2 = 0;
                        varLimits = region.getMotionHandler().getLimits(strDim, offset1, offset2);
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

                        /*
                        if (strDim.equals("rotation")) {
                        while (dimValue < 0) {
                        dimValue += 360;
                        }
                        while (dimValue > 360) {
                        dimValue -= 360;
                        }
                        }*/

                        if (dimValue < min) {
                            dimValue = min;
                        } else if (dimValue > max) {
                            dimValue = max;
                        }
                    }
                    if (strDim.equals("position x")) {
                        if (bPlayback) {
                            int w = region.playback_x2 - region.playback_x1;
                            region.playback_x1 = (int) InteractionSpace.getSketchX(dimValue);
                            if (strHAlign.equalsIgnoreCase("center")) {
                                region.playback_x1 -= w / 2;
                            } else if (strHAlign.equalsIgnoreCase("right")) {
                                region.playback_x1 -= w;
                            } else {
                            }
                            region.playback_x2 = region.playback_x1 + w;
                        } //else {
                        int w = region.x2 - region.x1;
                        region.x1 = (int) InteractionSpace.getSketchX(dimValue);
                        if (strHAlign.equalsIgnoreCase("center")) {
                            region.x1 -= w / 2;
                        } else if (strHAlign.equalsIgnoreCase("right")) {
                            region.x1 -= w;
                        } else {
                        }
                        region.x2 = region.x1 + w;
                        //}
                        if (!region.regionGrouping.equals("")) {
                            for (ActiveRegion as : region.parent.getRegions()) {
                                if (as != region && as.regionGrouping.equals(region.regionGrouping)) {
                                    if (bPlayback) {
                                        as.playback_x1 += region.playback_x1 - prevX1;
                                        as.playback_x2 += region.playback_x2 - prevX2;
                                    } //else {
                                    as.x1 += region.x1 - prevX1;
                                    as.x2 += region.x2 - prevX2;
                                    //}
                                }
                            }
                        }
                    } else if (strDim.equals("position y")) {
                        /*if (InteractionSpace.top < InteractionSpace.bottom) {
                        dimValue = dimValue * Toolkit.getDefaultToolkit().getScreenSize().getHeight() / Math.abs(InteractionSpace.top - InteractionSpace.bottom);
                        } else {
                        dimValue = dimValue * Toolkit.getDefaultToolkit().getScreenSize().getHeight() / Math.abs(InteractionSpace.top - InteractionSpace.bottom);
                        }*/
                        if (bPlayback) {
                            int h = region.playback_y2 - region.playback_y1;
                            region.playback_y1 = (int) InteractionSpace.getSketchY(dimValue);
                            if (strVAlign.equalsIgnoreCase("center")) {
                                region.playback_y1 -= h / 2;
                            } else if (strHAlign.equalsIgnoreCase("bottom")) {
                                region.playback_y1 -= h;
                            } else {
                            }

                            region.playback_y2 = region.playback_y1 + h;
                        } //else {
                        int h = region.y2 - region.y1;
                        region.y1 = (int) InteractionSpace.getSketchY(dimValue);
                        if (strVAlign.equalsIgnoreCase("center")) {
                            region.y1 -= h / 2;
                        } else if (strHAlign.equalsIgnoreCase("bottom")) {
                            region.y1 -= h;
                        } else {
                        }
                        region.y2 = region.y1 + h;
                        //}
                        if (!region.regionGrouping.equals("")) {
                            for (ActiveRegion as : region.parent.getRegions()) {
                                if (as != region && as.regionGrouping.equals(region.regionGrouping)) {
                                    if (bPlayback) {
                                        as.playback_y1 += region.playback_y1 - prevY1;
                                        as.playback_y2 += region.playback_y2 - prevY2;
                                    } //else {
                                    as.y1 += region.y1 - prevY1;
                                    as.y2 += region.y2 - prevY2;
                                    //}
                                }
                            }
                        }
                    } else if (strDim.equals("rotation")) {
                        /*while (dimValue < 0) {
                        dimValue += 360;
                        }
                        while (dimValue > 360) {
                        dimValue -= 360;
                        }*/
                        if (bPlayback) {
                            region.playback_rotation = InteractionSpace.toRadians(dimValue);
                        } //else {
                        region.rotation = InteractionSpace.toRadians(dimValue);
                        //}
                    } else if (strDim.equalsIgnoreCase("trajectory position")) {
                        Point p = region.getRenderer().getTrajectoryDrawingLayer().getTrajectoryPoint(dimValueRelative);
                        if (p != null) {
                            if (bPlayback) {
                                int h = region.playback_y2 - region.playback_y1;
                                int w = region.playback_x2 - region.playback_x1;
                                region.playback_x1 = (int) (p.x - w * region.center_rotation_x);
                                region.playback_y1 = (int) (p.y - h * region.center_rotation_y);
                                region.playback_x2 = region.playback_x1 + w;
                                region.playback_y2 = region.playback_y1 + h;
                                if (region.changingOrientationOnTrajectoryEnabled) {
                                    region.playback_rotation = region.getRenderer().getTrajectoryDrawingLayer().trajectoryOrientationFromPoint;
                                }
                            } //else {
                            int h = region.y2 - region.y1;
                            int w = region.x2 - region.x1;
                            region.x1 = (int) (p.x - w * region.center_rotation_x);
                            region.y1 = (int) (p.y - h * region.center_rotation_y);
                            region.x2 = region.x1 + w;
                            region.y2 = region.y1 + h;
                            if (region.changingOrientationOnTrajectoryEnabled) {
                                region.rotation = region.getRenderer().getTrajectoryDrawingLayer().trajectoryOrientationFromPoint;
                            }
                            //}
                        }
                    } else if (strDim.equalsIgnoreCase("trajectory position 2")) {
                    }

                    if (strDim.startsWith("trajectory position")) {
                        region.getMotionHandler().processLimits("trajectory position", dimValue, 0.0, 1.0, 0.0, 0.0, true, triggerVariable);
                        region.getRenderer().getTrajectoryDrawingLayer().getClosestTrajectoryPoint(region.getRenderer().getTrajectoryDrawingLayer().getTrajectoryPoint(dimValue));
                        region.getMotionHandler().processLimits("trajectory position 2", region.getRenderer().getTrajectoryDrawingLayer().trajectoryPositionFromPoint2, 0.0, 1.0, 0.0, 0.0, true, triggerVariable);
                    } else {
                        double _dimValue = dimValue;
                        if (strDim.equals("position x")) {
                            _dimValue = InteractionSpace.getSketchX(_dimValue);
                        } else if (strDim.equals("position y")) {
                            _dimValue = InteractionSpace.getSketchY(_dimValue);
                        }
                        region.getMotionHandler().processLimits(strDim, _dimValue, offset1, offset2, true, triggerVariable);
                    }
                }
            }

            if (!region.isWithinLimits(bPlayback) || region.getInteractionHandler().intersectsWithSolids(bPlayback)) {
                if (!bPlayback) {
                    findNonOverlapingLocation(region);
                    region.rotation = prevRotation;
                    region.shearX = prevShearX;
                    region.shearY = prevShearY;
                } else {
                    findNonOverlapingLocationPlayback(region);

                    region.playback_rotation = prevRotation;
                    region.playback_shearX = prevShearX;
                    region.playback_shearY = prevShearY;
                    region.x1 = region.playback_x1;
                    region.y1 = region.playback_y1;
                    region.x2 = region.playback_x2;
                    region.y2 = region.playback_y2;
                    region.rotation = region.playback_rotation;
                    region.shearX = region.playback_shearX;
                    region.shearY = region.playback_shearY;
                }
            }

            /*if (a.embeddedSketchHandler.embeddedSketch != null) {
            a.embeddedSketchHandler.embeddedSketch.regions.variableUpdated(triggerVariable, varValue, bPlayback);
            }
            processEmbeddedSketch(a, triggerVariable, varValue, bPlayback);*/
        }
        //DataServer.unprotectVariable(triggerVariable);
    }

    public static void findNonOverlapingLocationPlayback(ActiveRegion a) {
        int ox1 = a.playback_x1;
        int ox2 = a.playback_x2;
        int oy1 = a.playback_y1;
        int oy2 = a.playback_y2;
        for (int i = 1; i < 500; i++) {
            a.playback_x1 = ox1 + i;
            a.playback_x2 = ox2 + i;
            if (a.isWithinLimits(true) && !a.getInteractionHandler().intersectsWithSolids(true)) {
                break;
            } else {
                a.playback_x1 = ox1;
                a.playback_x2 = ox2;
            }
            a.playback_y1 = oy1 + i;
            a.playback_y2 = oy2 + i;
            if (a.isWithinLimits(true) && !a.getInteractionHandler().intersectsWithSolids(true)) {
                break;
            } else {
                a.playback_y1 = oy1;
                a.playback_y2 = oy2;
            }
            a.playback_x1 = ox1 - i;
            a.playback_x2 = ox2 - i;
            if (a.isWithinLimits(true) && !a.getInteractionHandler().intersectsWithSolids(true)) {
                break;
            } else {
                a.playback_x1 = ox1;
                a.playback_x2 = ox2;
            }
            a.playback_y1 = oy1 - i;
            a.playback_y2 = oy2 - i;
            if (a.isWithinLimits(true) && !a.getInteractionHandler().intersectsWithSolids(true)) {
                break;
            } else {
                a.playback_y1 = oy1;
                a.playback_y2 = oy2;
            }

            a.playback_x1 = ox1 + i;
            a.playback_x2 = ox2 + i;
            a.playback_y1 = oy1 + i;
            a.playback_y2 = oy2 + i;
            if (a.isWithinLimits(true) && !a.getInteractionHandler().intersectsWithSolids(true)) {
                break;
            } else {
                a.playback_x1 = ox1;
                a.playback_x2 = ox2;
                a.playback_y1 = oy1;
                a.playback_y2 = oy2;
            }

            a.playback_x1 = ox1 - i;
            a.playback_x2 = ox2 - i;
            a.playback_y1 = oy1 + i;
            a.playback_y2 = oy2 + i;
            if (a.isWithinLimits(true) && !a.getInteractionHandler().intersectsWithSolids(true)) {
                break;
            } else {
                a.playback_x1 = ox1;
                a.playback_x2 = ox2;
                a.playback_y1 = oy1;
                a.playback_y2 = oy2;
            }

            a.playback_x1 = ox1 + i;
            a.playback_x2 = ox2 + i;
            a.playback_y1 = oy1 - i;
            a.playback_y2 = oy2 - i;
            if (a.isWithinLimits(true) && !a.getInteractionHandler().intersectsWithSolids(true)) {
                break;
            } else {
                a.playback_x1 = ox1;
                a.playback_x2 = ox2;
                a.playback_y1 = oy1;
                a.playback_y2 = oy2;
            }

            a.playback_x1 = ox1 - i;
            a.playback_x2 = ox2 - i;
            a.playback_y1 = oy1 - i;
            a.playback_y2 = oy2 - i;
            if (a.isWithinLimits(true) && !a.getInteractionHandler().intersectsWithSolids(true)) {
                break;
            } else {
                a.playback_x1 = ox1;
                a.playback_x2 = ox2;
                a.playback_y1 = oy1;
                a.playback_y2 = oy2;
            }
        }
    }

    public static void findNonOverlapingLocation(ActiveRegion a) {
        int ox1 = a.x1;
        int oy1 = a.y1;
        int ox2 = a.x2;
        int oy2 = a.y2;
        for (int i = 1; i < 500; i++) {
            a.x1 = ox1 + i;
            a.x2 = ox2 + i;
            if (a.isWithinLimits(false) && !a.getInteractionHandler().intersectsWithSolids(false)) {
                break;
            } else {
                a.x1 = ox1;
                a.x2 = ox2;
            }
            a.y1 = oy1 + i;
            a.y2 = oy2 + i;
            if (a.isWithinLimits(false) && !a.getInteractionHandler().intersectsWithSolids(false)) {
                break;
            } else {
                a.y1 = oy1;
                a.y2 = oy2;
            }
            a.x1 = ox1 - i;
            a.x2 = ox2 - i;
            if (a.isWithinLimits(false) && !a.getInteractionHandler().intersectsWithSolids(false)) {
                break;
            } else {
                a.x1 = ox1;
                a.x2 = ox2;
            }
            a.y1 = oy1 - i;
            a.y2 = oy2 - i;
            if (a.isWithinLimits(false) && !a.getInteractionHandler().intersectsWithSolids(false)) {
                break;
            } else {
                a.y1 = oy1;
                a.y2 = oy2;
            }

            a.x1 = ox1 + i;
            a.x2 = ox2 + i;
            a.y1 = oy1 + i;
            a.y2 = oy2 + i;
            if (a.isWithinLimits(false) && !a.getInteractionHandler().intersectsWithSolids(false)) {
                break;
            } else {
                a.x1 = ox1;
                a.x2 = ox2;
                a.y1 = oy1;
                a.y2 = oy2;
            }

            a.x1 = ox1 - i;
            a.x2 = ox2 - i;
            a.y1 = oy1 + i;
            a.y2 = oy2 + i;
            if (a.isWithinLimits(false) && !a.getInteractionHandler().intersectsWithSolids(false)) {
                break;
            } else {
                a.x1 = ox1;
                a.x2 = ox2;
                a.y1 = oy1;
                a.y2 = oy2;
            }

            a.x1 = ox1 + i;
            a.x2 = ox2 + i;
            a.y1 = oy1 - i;
            a.y2 = oy2 - i;
            if (a.isWithinLimits(false) && !a.getInteractionHandler().intersectsWithSolids(false)) {
                break;
            } else {
                a.x1 = ox1;
                a.x2 = ox2;
                a.y1 = oy1;
                a.y2 = oy2;
            }

            a.x1 = ox1 - i;
            a.x2 = ox2 - i;
            a.y1 = oy1 - i;
            a.y2 = oy2 - i;
            if (a.isWithinLimits(false) && !a.getInteractionHandler().intersectsWithSolids(false)) {
                break;
            } else {
                a.x1 = ox1;
                a.x2 = ox2;
                a.y1 = oy1;
                a.y2 = oy2;
            }
        }
    }


    /*public void processEmbeddedSketch(ActiveRegion ea, String triggerVariable, String value, boolean bPlayback) {
    if (bPlayback && ea.embeddedSketchHandler.embeddedSketch != null && ea.embeddedSketchHandler.embeddedSketch.eventHandler != null) {
    ea.embeddedSketchHandler.embeddedSketch.eventHandler.process(triggerVariable, value);
    }
    }*/
    public void refreshVariables() {
        for (ActiveRegion a : getRegions()) {
            int w = a.x2 - a.x1;

            String strHAlign = a.processText(a.horizontalAlignment);
            String strVAlign = a.processText(a.verticalAlignment);

            if (strHAlign.equalsIgnoreCase("center")) {
                a.getMotionHandler().processLimits("position x", a.x1 + w / 2, w / 2, w / 2, true);
            } else if (strHAlign.equalsIgnoreCase("right")) {
                a.getMotionHandler().processLimits("position x", a.x1 + w, w, 0, true);
            } else {
                a.getMotionHandler().processLimits("position x", a.x1, 0, w, true);
            }

            if (a.x2 - a.x1 < w) {
                a.x1 = a.x2 - w;
            }

            int h = a.y2 - a.y1;

            if (strVAlign.equalsIgnoreCase("center")) {
                a.getMotionHandler().processLimits("position y", a.y1 + h / 2, h / 2, h / 2, true);
            } else if (strVAlign.equalsIgnoreCase("bottom")) {
                a.getMotionHandler().processLimits("position y", a.y1 + h, h, 0, true);
            } else {
                a.getMotionHandler().processLimits("position y", a.y1, 0, h, true);
            }

            if (a.y2 - a.y1 < h) {
                a.y1 = a.y2 - h;
            }

            a.getMotionHandler().processLimits("rotation", InteractionSpace.toPhysicalAngle(a.rotation), 0.0, 0.0, true);
        }
    }

    public void refreshFromVariables() {
        for (ActiveRegion a : getRegions()) {
            for (int i = 0; i
                    < a.updateTransformations.length; i++) {
                String strVar = ((String) a.updateTransformations[i][1]).trim();

                strVar = a.getVarPrefix() + strVar + a.getVarPostfix();

                if (!strVar.equals("")) {
                    String strDim = a.processText((String) a.updateTransformations[i][0]);
                    //String strPrefix = a.processText((String) a.updateTransformations[i][4]);
                    //String strPostfix = a.processText((String) a.updateTransformations[i][5]);

                    String varValue = DataServer.getInstance().getVariableValue(strVar);

                    if (varValue == null || varValue.equals("")) {
                        continue;
                    }

                    changePerformed(strVar, varValue, false);
                }

            }
            /*if (a.embeddedSketchHandler.embeddedSketch != null) {
            String strEPrefix = a.processText(a.strEmbeddedSketchVarPrefix);
            String strEPostfix = a.processText(a.strEmbeddedSketchVarPostfix);
            
            a.embeddedSketchHandler.embeddedSketch.regions.refreshFromVariables();
            }*/
        }
    }

    public Vector<String> getVariables() {
        Vector<String> variables = new Vector<String>();
        for (ActiveRegion region : getRegions()) {
            String strValue;
            for (int i = 0; i < region.updateTransformations.length; i++) {
                strValue = (String) region.updateTransformations[i][1];

                if (strValue.length() > 0) {
                    variables.add(strValue);
                }
            }
            for (MouseEventMacro mouseEventMacro : region.mouseProcessor.getMouseEventMacros()) {
                for (int i = 0; i < mouseEventMacro.getMacro().getActions().length; i++) {
                    String strAction = (String) mouseEventMacro.getMacro().getActions()[i][0];
                    strValue = (String) mouseEventMacro.getMacro().getActions()[i][1];
                    String strContent = (String) mouseEventMacro.getMacro().getActions()[i][2];
                    if (strAction.toLowerCase().startsWith("variable") && strContent.length() > 0) {
                        variables.add(strValue);
                    }
                }
            }
            for (RegionOverlapEventMacro regionOverlapEventMacro : region.regionOverlapEventMacros) {
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
                if (!mayBeFormula(strVar) && DataServer.getInstance().getVariable(strVar) == null && !strVar.startsWith("[")) {
                    DataServer.getInstance().updateVariable(strVar, " ");
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

    public void defocusAllRegions() {
        for (ActiveRegion ar : getRegions()) {
            ar.bInFocus = false;
        }
        SketchletContext.getInstance().repaint();
    }

    public Vector<ActiveRegion> getRegions() {
        return regions;
    }

    public void setRegions(Vector<ActiveRegion> regions) {
        this.regions = regions;
    }

    public Vector<ActiveRegion> getSelectedRegions() {
        return selectedRegions;
    }

    public void setSelectedRegions(Vector<ActiveRegion> selectedRegions) {
        this.selectedRegions = selectedRegions;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public Point getMousePressedPoint() {
        return mousePressedPoint;
    }

    public void setMousePressedPoint(Point mousePressedPoint) {
        this.mousePressedPoint = mousePressedPoint;
    }

    public boolean isNewRegion() {
        return newRegion;
    }

    public void setNewRegion(boolean newRegion) {
        this.newRegion = newRegion;
    }

    public int getOffset_x() {
        return offset_x;
    }

    public void setOffset_x(int offset_x) {
        this.offset_x = offset_x;
    }

    public int getOffset_y() {
        return offset_y;
    }

    public void setOffset_y(int offset_y) {
        this.offset_y = offset_y;
    }

    public boolean isbNoEffect() {
        return bNoEffect;
    }

    public void setbNoEffect(boolean bNoEffect) {
        this.bNoEffect = bNoEffect;
    }
}
