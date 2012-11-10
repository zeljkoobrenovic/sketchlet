/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.data;

import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.designer.editor.EditorMode;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.events.region.ActiveRegionMouseHandler;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.designer.ui.region.ActiveRegionsFrame;
import net.sf.sketchlet.pluginloader.WidgetPluginHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.util.Vector;

public class ActiveRegions {

    public Vector<ActiveRegion> regions = new Vector<ActiveRegion>();
    public Vector<ActiveRegion> selectedRegions = null;
    public Page page;
    public Point mousePressedPoint;
    public boolean newRegion = false;

    public ActiveRegions(Page page) {
        this.page = page;
    }

    public ActiveRegions(ActiveRegions ars, Page page) {
        this(page);
        long lastTime = System.currentTimeMillis();
        for (ActiveRegion reg : ars.regions) {
            while (lastTime == System.currentTimeMillis()) {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                }
            }
            this.regions.add(new ActiveRegion(reg, this, true));
            lastTime = System.currentTimeMillis();
        }
    }

    public ActiveRegion getLastSelectedRegion() {
        if (this.selectedRegions != null && this.selectedRegions.size() > 0) {
            return selectedRegions.lastElement();
        }
        return null;
    }

    public ActiveRegions cloneEmbedded(Page page) {
        ActiveRegions _actions = new ActiveRegions(page);
        for (ActiveRegion reg : regions) {
            _actions.regions.add(new ActiveRegion(reg, _actions, false));
        }

        return _actions;
    }

    public ActiveRegion getRegionByNumber(int num) {
        if (num <= this.regions.size()) {
            return regions.elementAt(regions.size() - num);
        }
        return null;
    }

    public ActiveRegion getRegionByName(String name) {
        for (ActiveRegion region : regions) {
            if (region.getName().equalsIgnoreCase(name)) {
                return region;
            }
        }
        for (ActiveRegion region : regions) {
            if (region.getNumber().equalsIgnoreCase(name)) {
                return region;
            }
        }
        return null;
    }

    public ActiveRegion getRegionByImageFileName(String name) {
        for (ActiveRegion region : regions) {
            if (region.getDrawImageFileName().equalsIgnoreCase(name)) {
                return region;
            }
        }
        return null;
    }

    public ActiveRegion getRegionById(String id) {
        for (ActiveRegion region : regions) {
            if (region.getId().equalsIgnoreCase(id)) {
                return region;
            }
        }
        return null;
    }

    public int getActionIndex(String imageFile) {
        int i = 0;
        for (ActiveRegion a : regions) {
            if (a.getDrawImageFileName(0).equals(imageFile)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public int offset_x = 0;
    public int offset_y = 0;

    public void draw(Graphics2D g2, Component component, EditorMode mode, boolean bPlayback, boolean bHighlightRegions, float transparency) {
        int offset[] = page.getRegionsOffset(bPlayback);
        offset_x = offset[0];
        offset_y = offset[1];
        for (int i = regions.size() - 1; i >= 0; i--) {
            ActiveRegion reg = regions.elementAt(i);
            if (reg.isActive(bPlayback)) {
                reg.renderer.draw(g2, component, mode, bPlayback, bHighlightRegions, transparency);
            }
        }
    }

    public void draw(int layer, Graphics2D g2, Component component, EditorMode mode, boolean bPlayback, boolean bHighlightRegions, float transparency) {
        int offset[] = page.getRegionsOffset(bPlayback);
        offset_x = offset[0];
        offset_y = offset[1];
        for (int i = regions.size() - 1; i >= 0; i--) {
            ActiveRegion reg = regions.elementAt(i);
            if (reg.isActive(bPlayback) && reg.layer == layer) {
                reg.renderer.draw(g2, component, mode, bPlayback, bHighlightRegions, transparency);
            }
        }
    }

    public void removeFromSelection(ActiveRegion region) {
        if (selectedRegions != null && region != null && selectedRegions.contains(region)) {
            selectedRegions.remove(region);
        }
    }

    public void addToSelection(ActiveRegion region) {
        if (region != null) {
            if (selectedRegions == null) {
                selectedRegions = new Vector<ActiveRegion>();
            }
            String strGroup = region.regionGrouping.trim();
            if (!strGroup.equals("")) {
                for (ActiveRegion reg : regions) {
                    if (reg.regionGrouping.equals(strGroup) && reg != region && !selectedRegions.contains(reg)) {
                        selectedRegions.add(reg);
                    }
                }
            }
            if (selectedRegions != null && region != null && selectedRegions.contains(region)) {
                selectedRegions.remove(region);
            }
            selectedRegions.add(region);
        }
    }

    public void mousePressed(MouseEvent e, double scale, JFrame frame, boolean bPlayback, boolean bAddNew) {
        int marginX = bPlayback ? 0 : SketchletEditor.marginX;
        int marginY = bPlayback ? 0 : SketchletEditor.marginY;
        int x = (int) ((e.getPoint().x) / scale) - marginX;
        int y = (int) ((e.getPoint().y) / scale) - marginY;

        mousePressed(x, y, e.getModifiers(), e.getWhen(), scale, e, frame, bPlayback, bAddNew);
    }

    public void mousePressed(int x, int y, int modifiers, long when, double scale, MouseEvent e, JFrame frame, boolean bPlayback, boolean bAddNew) {
        ActiveRegion reg = selectRegion(x, y, bPlayback);
        mousePressedPoint = new Point(x, y);

        if (reg == null || bAddNew) {
            if (bAddNew) {
                selectedRegions = new Vector<ActiveRegion>();
                if ((modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                    newRegion = true;
                    reg = new ActiveRegion(this, x, y, x + 50, y + 20);
                    reg.layer = SketchletEditor.editorPanel.layer;
                    if (SketchletEditor.initProperties != null) {
                        for (int i = 0; i < SketchletEditor.initProperties.length; i++) {
                            String strProp = SketchletEditor.initProperties[i][0];
                            if (!strProp.equalsIgnoreCase("x1") && !strProp.equalsIgnoreCase("y1") && !strProp.equalsIgnoreCase("x2") && !strProp.equalsIgnoreCase("y2") && !strProp.equalsIgnoreCase("position x") && !strProp.equalsIgnoreCase("position y")) {
                                reg.setProperty(strProp, SketchletEditor.initProperties[i][1]);
                            }
                        }
                    }
                    addToSelection(reg);
                    reg.mouseHandler.selectedCorner = ActiveRegionMouseHandler.BOTTOM_RIGHT;
                    regions.insertElementAt(reg, 0);
                    reg.activate(bPlayback);
                }
            } else {
                selectedRegions = null;
            }
        } else {
            if (selectedRegions == null) {
                selectedRegions = new Vector<ActiveRegion>();
                addToSelection(reg);
            } else if (SketchletEditor.editorPanel.inShiftMode && !selectedRegions.contains(reg)) {
                addToSelection(reg);
            } else if (SketchletEditor.editorPanel.inShiftMode && selectedRegions.contains(reg)) {
                removeFromSelection(reg);
            } else if (!SketchletEditor.editorPanel.inShiftMode && !selectedRegions.contains(reg)) {
                selectedRegions = new Vector<ActiveRegion>();
                addToSelection(reg);
            } else {
                selectedRegions.remove(reg);
                addToSelection(reg);
            }

            reg.mouseHandler.mousePressed(x, y, modifiers, when, e, frame, bPlayback);
            ActiveRegionsFrame.refresh(reg);
            SketchletEditor.editorPanel.repaint();
        }

        SketchletEditor.editorPanel.activeRegionMenu.setEnabled(isRegionSelected());
        SketchletEditor.editorPanel.formulaToolbar.enableControls(isRegionSelected(), this.isConnectorSelected());
    }

    public void deselectRegions() {
        selectedRegions = null;
        SketchletEditor.editorPanel.activeRegionMenu.setEnabled(isRegionSelected());
        SketchletEditor.editorPanel.formulaToolbar.enableControls(isRegionSelected(), this.isConnectorSelected());
        SketchletEditor.editorPanel.formulaToolbar.refresh();
    }

    public boolean isRegionSelected() {
        return selectedRegions != null && selectedRegions.size() > 0;
    }

    public boolean isConnectorSelected() {
        return SketchletEditor.editorPanel != null && SketchletEditor.editorPanel.currentPage.selectedConnector != null;
    }

    public void mouseDragged(MouseEvent e, double scale, JFrame frame, boolean bPlayback) {
        if (selectedRegions != null && selectedRegions.size() > 0) {
            // for (Action a : selectedActions) {
            selectedRegions.lastElement().mouseHandler.mouseDragged(e, scale, frame, bPlayback);
            //}
        }
    }

    public void mouseDragged(int x, int y, int modifiers, long when, double scale, MouseEvent e, JFrame frame, boolean bPlayback) {
        if (selectedRegions != null && selectedRegions.size() > 0) {
            // for (Action a : selectedActions) {
            selectedRegions.lastElement().mouseHandler.mouseDragged(x, y, modifiers, when, scale, e, frame, bPlayback);
            //}
        }
    }

    public void mouseReleased(MouseEvent e, double scale, JFrame frame, boolean bPlayback) {
        int marginX = bPlayback ? 0 : SketchletEditor.marginX;
        int marginY = bPlayback ? 0 : SketchletEditor.marginY;
        int x = (int) ((e.getPoint().x) / scale) - marginX;
        int y = (int) ((e.getPoint().y) / scale) - marginY;

        mouseReleased(x, y, e.getModifiers(), e.getWhen(), scale, e, frame, bPlayback);
    }

    public boolean bNoEffect = false;

    public void mouseReleased(int x, int y, int modifiers, long when, double scale, MouseEvent e, JFrame frame, boolean bPlayback) {
        bNoEffect = false;
        if (selectedRegions != null && selectedRegions.size() > 0) {
            if (selectedRegions.lastElement().getWidth() > 0 || (mousePressedPoint != null && !(Math.abs(mousePressedPoint.x - x) <= 0 && Math.abs(mousePressedPoint.y - y) <= 0))) {
                if (newRegion) {
                    ActiveRegionsFrame.reload(selectedRegions.lastElement());
                }

                selectedRegions.lastElement().mouseHandler.mouseReleased(x, y, modifiers, when, e, frame, bPlayback);
            } else {
                if (newRegion) {
                    for (ActiveRegion as : selectedRegions) {
                        as.deactivate(bPlayback);
                        regions.remove(as);
                    }
                    selectedRegions = null;
                }

                bNoEffect = true;
            }

            SketchletEditor.editorPanel.repaint();

            mousePressedPoint = null;
        }
        newRegion = false;
    }

    public ActiveRegion selectRegion(int x, int y, boolean bPlayback) {
        if (!this.page.bLayerRegions) {
            return null;
        }
        for (ActiveRegion a : regions) {
            if (a.isActive(bPlayback) && a.mouseHandler.inRect(x, y, bPlayback)) {
                /*if (bPlayback && a.embeddedSketchHandler.embeddedSketch != null) {
                Point ip = a.mouseHandler.inversePointEmbedded(x, y);
                a.embeddedSketchHandler.embeddedSelectedRegion = a.embeddedSketchHandler.embeddedSketch.regions.selectRegion(ip.x, ip.y, bPlayback);
                }*/
                return a;
            }
        }

        return null;
    }

    public boolean isAffected(String strVariable) {
        for (ActiveRegion a : regions) {
            if (a.isAffected(strVariable)) {
                return true;
            }
        }

        return false;
    }

    public void save(PrintWriter out) {
        out.println("<active-regions>");
        for (ActiveRegion a : regions) {
            a.save(out);
        }
        out.println("</active-regions>");
    }

    public void changePerformed(final String triggerVariable, String varValue, boolean bPlayback) {
        if (triggerVariable.isEmpty()) {
            return;
        }
        if (SketchletEditor.isInPlaybackMode() && !bPlayback) {
            return;
        }
        //DataServer.protectVariable(triggerVariable);
        int ia = 0;

        for (ActiveRegion a : regions) {
            if (a.limitsHandler.isInProcessing(triggerVariable)) {
                continue;
            }
            int prevX1 = (bPlayback) ? a.playback_x1 : a.x1;
            int prevY1 = (bPlayback) ? a.playback_y1 : a.y1;
            int prevX2 = (bPlayback) ? a.playback_x2 : a.x2;
            int prevY2 = (bPlayback) ? a.playback_y2 : a.y2;
            double prevRotation = (bPlayback) ? a.playback_rotation : a.rotation;
            double prevShearX = (bPlayback) ? a.playback_shearX : a.shearX;
            double prevShearY = (bPlayback) ? a.playback_shearY : a.shearY;

            if (a.renderer.widgetImageLayer.widgetControl != null) {
                WidgetPluginHandler.injectWidgetPropertiesValues(a.renderer.widgetImageLayer.widgetControl);
                a.renderer.widgetImageLayer.widgetControl.variableUpdated(triggerVariable, varValue);
            }

            ia++;
            for (int i = 0; i < a.updateTransformations.length; i++) {
                Object ivars[] = a.limitsHandler.ignoreRows.toArray();
                for (int iii = 0; iii < ivars.length; iii++) {
                    Integer ignoreRow = (Integer) ivars[iii];
                    if (ignoreRow.intValue() == i) {
                        continue;
                    }
                }
                String strVar = ((String) a.updateTransformations[i][1]).trim();

                strVar = a.getVarPrefix() + strVar + a.getVarPostfix();

                if (!strVar.equals("") && strVar.equals(triggerVariable)) {
                    String strDim = a.processText((String) a.updateTransformations[i][0]);

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

                    String strStart = a.processText((String) a.updateTransformations[i][2]);
                    String strEnd = a.processText((String) a.updateTransformations[i][3]);

                    double varLimits[] = null;

                    double offset1 = 0.0;
                    double offset2 = 0.0;

                    String strHAlign = a.processText(a.strHAlign);
                    String strVAlign = a.processText(a.strVAlign);
                    if (strDim.equals("position x")) {
                        int w = bPlayback ? a.playback_x2 - a.playback_x1 : a.x2 - a.x1;

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
                        varLimits = a.limitsHandler.getLimits(strDim, offset1, offset2);
                    } else if (strDim.equals("position y")) {
                        int h = bPlayback ? a.playback_y2 - a.playback_y1 : a.y2 - a.y1;

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
                        varLimits = a.limitsHandler.getLimits(strDim, offset1, offset2);
                    } else if (strDim.startsWith("trajectory position")) {
                        varLimits = new double[]{0.0, 1.0};
                        offset1 = 0;
                        offset2 = 0;
                    } else {
                        offset1 = 0;
                        offset2 = 0;
                        varLimits = a.limitsHandler.getLimits(strDim, offset1, offset2);
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
                            int w = a.playback_x2 - a.playback_x1;
                            a.playback_x1 = (int) InteractionSpace.getSketchX(dimValue);
                            if (strHAlign.equalsIgnoreCase("center")) {
                                a.playback_x1 -= w / 2;
                            } else if (strHAlign.equalsIgnoreCase("right")) {
                                a.playback_x1 -= w;
                            } else {
                            }
                            a.playback_x2 = a.playback_x1 + w;
                        } //else {
                        int w = a.x2 - a.x1;
                        a.x1 = (int) InteractionSpace.getSketchX(dimValue);
                        if (strHAlign.equalsIgnoreCase("center")) {
                            a.x1 -= w / 2;
                        } else if (strHAlign.equalsIgnoreCase("right")) {
                            a.x1 -= w;
                        } else {
                        }
                        a.x2 = a.x1 + w;
                        //}
                        if (!a.regionGrouping.equals("")) {
                            for (ActiveRegion as : a.parent.regions) {
                                if (as != a && as.regionGrouping.equals(a.regionGrouping)) {
                                    if (bPlayback) {
                                        as.playback_x1 += a.playback_x1 - prevX1;
                                        as.playback_x2 += a.playback_x2 - prevX2;
                                    } //else {
                                    as.x1 += a.x1 - prevX1;
                                    as.x2 += a.x2 - prevX2;
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
                            int h = a.playback_y2 - a.playback_y1;
                            a.playback_y1 = (int) InteractionSpace.getSketchY(dimValue);
                            if (strVAlign.equalsIgnoreCase("center")) {
                                a.playback_y1 -= h / 2;
                            } else if (strHAlign.equalsIgnoreCase("bottom")) {
                                a.playback_y1 -= h;
                            } else {
                            }

                            a.playback_y2 = a.playback_y1 + h;
                        } //else {
                        int h = a.y2 - a.y1;
                        a.y1 = (int) InteractionSpace.getSketchY(dimValue);
                        if (strVAlign.equalsIgnoreCase("center")) {
                            a.y1 -= h / 2;
                        } else if (strHAlign.equalsIgnoreCase("bottom")) {
                            a.y1 -= h;
                        } else {
                        }
                        a.y2 = a.y1 + h;
                        //}
                        if (!a.regionGrouping.equals("")) {
                            for (ActiveRegion as : a.parent.regions) {
                                if (as != a && as.regionGrouping.equals(a.regionGrouping)) {
                                    if (bPlayback) {
                                        as.playback_y1 += a.playback_y1 - prevY1;
                                        as.playback_y2 += a.playback_y2 - prevY2;
                                    } //else {
                                    as.y1 += a.y1 - prevY1;
                                    as.y2 += a.y2 - prevY2;
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
                            a.playback_rotation = InteractionSpace.toRadians(dimValue);
                        } //else {
                        a.rotation = InteractionSpace.toRadians(dimValue);
                        //}
                    } else if (strDim.equalsIgnoreCase("trajectory position")) {
                        Point p = a.renderer.trajectoryDrawingLayer.getTrajectoryPoint(dimValueRelative);
                        if (p != null) {
                            if (bPlayback) {
                                int h = a.playback_y2 - a.playback_y1;
                                int w = a.playback_x2 - a.playback_x1;
                                a.playback_x1 = (int) (p.x - w * a.center_rotation_x);
                                a.playback_y1 = (int) (p.y - h * a.center_rotation_y);
                                a.playback_x2 = a.playback_x1 + w;
                                a.playback_y2 = a.playback_y1 + h;
                                if (a.bOrientationTrajectory) {
                                    a.playback_rotation = a.renderer.trajectoryDrawingLayer.trajectoryOrientationFromPoint;
                                }
                            } //else {
                            int h = a.y2 - a.y1;
                            int w = a.x2 - a.x1;
                            a.x1 = (int) (p.x - w * a.center_rotation_x);
                            a.y1 = (int) (p.y - h * a.center_rotation_y);
                            a.x2 = a.x1 + w;
                            a.y2 = a.y1 + h;
                            if (a.bOrientationTrajectory) {
                                a.rotation = a.renderer.trajectoryDrawingLayer.trajectoryOrientationFromPoint;
                            }
                            //}
                        }
                    } else if (strDim.equalsIgnoreCase("trajectory position 2")) {
                    }

                    if (strDim.startsWith("trajectory position")) {
                        a.limitsHandler.processLimits("trajectory position", dimValue, 0.0, 1.0, 0.0, 0.0, true, triggerVariable);
                        a.renderer.trajectoryDrawingLayer.getClosestTrajectoryPoint(a.renderer.trajectoryDrawingLayer.getTrajectoryPoint(dimValue));
                        a.limitsHandler.processLimits("trajectory position 2", a.renderer.trajectoryDrawingLayer.trajectoryPositionFromPoint2, 0.0, 1.0, 0.0, 0.0, true, triggerVariable);
                    } else {
                        double _dimValue = dimValue;
                        if (strDim.equals("position x")) {
                            _dimValue = InteractionSpace.getSketchX(_dimValue);
                        } else if (strDim.equals("position y")) {
                            _dimValue = InteractionSpace.getSketchY(_dimValue);
                        }
                        a.limitsHandler.processLimits(strDim, _dimValue, offset1, offset2, true, triggerVariable);
                    }
                }
            }

            if (!a.isWithinLimits(bPlayback) || a.interactionHandler.intersectsWithSolids(bPlayback)) {
                if (!bPlayback) {
                    findNonOverlapingLocation(a);
                    a.rotation = prevRotation;
                    a.shearX = prevShearX;
                    a.shearY = prevShearY;
                } else {
                    findNonOverlapingLocationPlayback(a);

                    a.playback_rotation = prevRotation;
                    a.playback_shearX = prevShearX;
                    a.playback_shearY = prevShearY;
                    a.x1 = a.playback_x1;
                    a.y1 = a.playback_y1;
                    a.x2 = a.playback_x2;
                    a.y2 = a.playback_y2;
                    a.rotation = a.playback_rotation;
                    a.shearX = a.playback_shearX;
                    a.shearY = a.playback_shearY;
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
            if (a.isWithinLimits(true) && !a.interactionHandler.intersectsWithSolids(true)) {
                break;
            } else {
                a.playback_x1 = ox1;
                a.playback_x2 = ox2;
            }
            a.playback_y1 = oy1 + i;
            a.playback_y2 = oy2 + i;
            if (a.isWithinLimits(true) && !a.interactionHandler.intersectsWithSolids(true)) {
                break;
            } else {
                a.playback_y1 = oy1;
                a.playback_y2 = oy2;
            }
            a.playback_x1 = ox1 - i;
            a.playback_x2 = ox2 - i;
            if (a.isWithinLimits(true) && !a.interactionHandler.intersectsWithSolids(true)) {
                break;
            } else {
                a.playback_x1 = ox1;
                a.playback_x2 = ox2;
            }
            a.playback_y1 = oy1 - i;
            a.playback_y2 = oy2 - i;
            if (a.isWithinLimits(true) && !a.interactionHandler.intersectsWithSolids(true)) {
                break;
            } else {
                a.playback_y1 = oy1;
                a.playback_y2 = oy2;
            }

            a.playback_x1 = ox1 + i;
            a.playback_x2 = ox2 + i;
            a.playback_y1 = oy1 + i;
            a.playback_y2 = oy2 + i;
            if (a.isWithinLimits(true) && !a.interactionHandler.intersectsWithSolids(true)) {
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
            if (a.isWithinLimits(true) && !a.interactionHandler.intersectsWithSolids(true)) {
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
            if (a.isWithinLimits(true) && !a.interactionHandler.intersectsWithSolids(true)) {
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
            if (a.isWithinLimits(true) && !a.interactionHandler.intersectsWithSolids(true)) {
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
            if (a.isWithinLimits(false) && !a.interactionHandler.intersectsWithSolids(false)) {
                break;
            } else {
                a.x1 = ox1;
                a.x2 = ox2;
            }
            a.y1 = oy1 + i;
            a.y2 = oy2 + i;
            if (a.isWithinLimits(false) && !a.interactionHandler.intersectsWithSolids(false)) {
                break;
            } else {
                a.y1 = oy1;
                a.y2 = oy2;
            }
            a.x1 = ox1 - i;
            a.x2 = ox2 - i;
            if (a.isWithinLimits(false) && !a.interactionHandler.intersectsWithSolids(false)) {
                break;
            } else {
                a.x1 = ox1;
                a.x2 = ox2;
            }
            a.y1 = oy1 - i;
            a.y2 = oy2 - i;
            if (a.isWithinLimits(false) && !a.interactionHandler.intersectsWithSolids(false)) {
                break;
            } else {
                a.y1 = oy1;
                a.y2 = oy2;
            }

            a.x1 = ox1 + i;
            a.x2 = ox2 + i;
            a.y1 = oy1 + i;
            a.y2 = oy2 + i;
            if (a.isWithinLimits(false) && !a.interactionHandler.intersectsWithSolids(false)) {
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
            if (a.isWithinLimits(false) && !a.interactionHandler.intersectsWithSolids(false)) {
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
            if (a.isWithinLimits(false) && !a.interactionHandler.intersectsWithSolids(false)) {
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
            if (a.isWithinLimits(false) && !a.interactionHandler.intersectsWithSolids(false)) {
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
        for (ActiveRegion a : regions) {
            int w = a.x2 - a.x1;

            String strHAlign = a.processText(a.strHAlign);
            String strVAlign = a.processText(a.strVAlign);

            if (strHAlign.equalsIgnoreCase("center")) {
                a.limitsHandler.processLimits("position x", a.x1 + w / 2, w / 2, w / 2, true);
            } else if (strHAlign.equalsIgnoreCase("right")) {
                a.limitsHandler.processLimits("position x", a.x1 + w, w, 0, true);
            } else {
                a.limitsHandler.processLimits("position x", a.x1, 0, w, true);
            }

            if (a.x2 - a.x1 < w) {
                a.x1 = a.x2 - w;
            }

            int h = a.y2 - a.y1;

            if (strVAlign.equalsIgnoreCase("center")) {
                a.limitsHandler.processLimits("position y", a.y1 + h / 2, h / 2, h / 2, true);
            } else if (strVAlign.equalsIgnoreCase("bottom")) {
                a.limitsHandler.processLimits("position y", a.y1 + h, h, 0, true);
            } else {
                a.limitsHandler.processLimits("position y", a.y1, 0, h, true);
            }

            if (a.y2 - a.y1 < h) {
                a.y1 = a.y2 - h;
            }

            a.limitsHandler.processLimits("rotation", InteractionSpace.toPhysicalAngle(a.rotation), 0.0, 0.0, true);
        }
    }

    public void refreshFromVariables() {
        for (ActiveRegion a : regions) {
            for (int i = 0; i
                    < a.updateTransformations.length; i++) {
                String strVar = ((String) a.updateTransformations[i][1]).trim();

                strVar = a.getVarPrefix() + strVar + a.getVarPostfix();

                if (!strVar.equals("")) {
                    String strDim = a.processText((String) a.updateTransformations[i][0]);
                    //String strPrefix = a.processText((String) a.updateTransformations[i][4]);
                    //String strPostfix = a.processText((String) a.updateTransformations[i][5]);

                    String varValue = DataServer.variablesServer.getVariableValue(strVar);

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
        for (ActiveRegion region : regions) {
            String strValue;
            for (int i = 0; i < region.updateTransformations.length; i++) {
                strValue = (String) region.updateTransformations[i][1];

                if (strValue.length() > 0) {
                    variables.add(strValue);
                }
            }
            for (MouseEventMacro mouseEventMacro : region.mouseProcessor.mouseEventMacros) {
                for (int i = 0; i < mouseEventMacro.getMacro().actions.length; i++) {
                    String strAction = (String) mouseEventMacro.getMacro().actions[i][0];
                    strValue = (String) mouseEventMacro.getMacro().actions[i][1];
                    String strContent = (String) mouseEventMacro.getMacro().actions[i][2];
                    if (strAction.toLowerCase().startsWith("variable") && strContent.length() > 0) {
                        variables.add(strValue);
                    }
                }
            }
            for (RegionOverlapEventMacro regionOverlapEventMacro : region.regionOverlapEventMacros) {
                for (int i = 0; i < regionOverlapEventMacro.getMacro().actions.length; i++) {
                    String strAction = (String) regionOverlapEventMacro.getMacro().actions[i][0];
                    strValue = (String) regionOverlapEventMacro.getMacro().actions[i][1];
                    String strContent = (String) regionOverlapEventMacro.getMacro().actions[i][2];
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
                if (!mayBeFormula(strVar) && DataServer.variablesServer.getVariable(strVar) == null && !strVar.startsWith("[")) {
                    DataServer.variablesServer.updateVariable(strVar, " ");
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
        for (ActiveRegion ar : regions) {
            ar.bInFocus = false;
        }
        SketchletContext.getInstance().repaint();
    }
}
