package net.sf.sketchlet.framework.renderer.page;

import net.sf.sketchlet.framework.blackboard.Variable;
import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.playback.ui.PlaybackPanel;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.model.ActiveRegions;
import net.sf.sketchlet.framework.model.events.EventMacro;
import net.sf.sketchlet.framework.model.Page;
import net.sf.sketchlet.framework.model.events.keyboard.KeyboardEventMacro;
import net.sf.sketchlet.framework.model.events.overlap.RegionOverlapEventMacro;
import net.sf.sketchlet.framework.model.programming.timers.Timer;
import net.sf.sketchlet.framework.model.programming.timers.TimerThread;
import net.sf.sketchlet.framework.model.programming.timers.Timers;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class VariablesRelationsRenderer {
    private static final Logger log = Logger.getLogger(VariablesRelationsRenderer.class);

    public static Vector<String> getRegionInfo(ActiveRegion region, boolean bShowPosition) {
        Vector<String> regionInfo = new Vector<String>();

        String strValue = "region " + (region.getParent().getRegions().size() - region.getParent().getRegions().indexOf(region));
        regionInfo.add(strValue);

        if (region.isPinned()) {
            regionInfo.add("PINNED");
        }
        strValue = region.getTextField().trim();
        if (StringUtils.isNotBlank(strValue)) {
            regionInfo.add("text: '" + strValue + "'");
        }
        String lines[] = region.getText().split("\n");
        strValue = "";
        for (int i = 0; i < lines.length; i++) {
            strValue += lines[i] + " ";
        }
        strValue = strValue.trim();
        if (StringUtils.isNotBlank(strValue)) {
            regionInfo.add("text: '" + strValue + "'");
        }

        strValue = region.getShape();
        if (StringUtils.isNotBlank(strValue) && !strValue.equalsIgnoreCase("None")) {
            regionInfo.add("shape: " + strValue);
        }
        strValue = region.getLineColor();
        if (StringUtils.isNotBlank(strValue)) {
            regionInfo.add("line color: " + strValue);
        }
        strValue = region.getLineThickness();
        if (StringUtils.isNotBlank(strValue)) {
            regionInfo.add("line thickness: " + strValue);
        }
        strValue = region.getLineStyle();
        if (StringUtils.isNotBlank(strValue)) {
            regionInfo.add("line style: " + strValue);
        }
        strValue = region.getFillColor();
        if (StringUtils.isNotBlank(strValue)) {
            regionInfo.add("fill color: " + strValue);
        }
        strValue = region.getCaptureScreenX();
        if (StringUtils.isNotBlank(strValue)) {
            regionInfo.add("capture screen x (left): " + strValue);
        }
        strValue = region.getCaptureScreenY();
        if (StringUtils.isNotBlank(strValue)) {
            regionInfo.add("capture screen x (top): " + strValue);
        }
        strValue = region.getCaptureScreenWidth();
        if (StringUtils.isNotBlank(strValue)) {
            regionInfo.add("capture screen width: " + strValue);
        }
        strValue = region.getCaptureScreenHeight();
        if (StringUtils.isNotBlank(strValue)) {
            regionInfo.add("capture screen height: " + strValue);
        }
        strValue = region.getHorizontalAlignment();
        if (StringUtils.isNotBlank(strValue) && !strValue.equalsIgnoreCase("null") && !strValue.equalsIgnoreCase("left")) {
            regionInfo.add("horizontal alignment: " + strValue);
        }
        strValue = region.getVerticalAlignment();
        if (StringUtils.isNotBlank(strValue) && !strValue.equalsIgnoreCase("null") && !strValue.equalsIgnoreCase("top")) {
            regionInfo.add("vertical alignment: " + strValue);
        }

        for (int i = 0; i < ActiveRegion.getPropertiesInfo().length; i++) {
            String property = ActiveRegion.getPropertiesInfo()[i][0];
            strValue = region.getProperty(property);
            String strDefault = region.getDefaultValue(property);
            if (StringUtils.isNotBlank(strValue) && !strValue.equalsIgnoreCase(strDefault)) {
                regionInfo.add(property + ": " + strValue);
            }
        }

        for (int i = 0; i < region.getMotionAndRotationVariablesMapping().length; i++) {
            strValue = (String) region.getMotionAndRotationVariablesMapping()[i][1];

            if (StringUtils.isNotBlank(strValue)) {
                regionInfo.add("connect dimension '" + region.getMotionAndRotationVariablesMapping()[i][0].toString().toLowerCase() + "' to variable '" + strValue + "'");
            }
        }

        for (int i = 0; i < region.getPropertiesAnimation().length; i++) {
            String strType = region.getPropertiesAnimation()[i][1];
            String strStart = region.getPropertiesAnimation()[i][2];
            String strEnd = region.getPropertiesAnimation()[i][3];
            String strDuration = region.getPropertiesAnimation()[i][4];
            String strCurve = region.getPropertiesAnimation()[i][5];

            if (strType != null && !strType.isEmpty()) {
                regionInfo.add("animate property '" + region.getPropertiesAnimation()[i][0].toString().toLowerCase() + "'" + " from " + strStart + " to " + strEnd + " in " + strDuration + " seconds" + (strCurve.isEmpty() ? "" : " using curve '" + strCurve + "'"));
            }
        }

        for (EventMacro eventMacro : region.getMouseEventsProcessor().getMouseEventMacros()) {
            String strEvent = eventMacro.getEventName();
            if (StringUtils.isNotBlank(strEvent)) {
                regionInfo.add("on " + strEvent.toLowerCase() + ": ");
                for (int i = 0; i < eventMacro.getMacro().getActions().length; i++) {
                    String strAction = (String) eventMacro.getMacro().getActions()[i][0];
                    strValue = (String) eventMacro.getMacro().getActions()[i][1];
                    String strContent = (String) eventMacro.getMacro().getActions()[i][2];
                    if (StringUtils.isNotBlank(strValue)) {
                        regionInfo.add("    " + strAction.toLowerCase() + " '" + strValue.toLowerCase() + "'" + (strContent.isEmpty() ? "" : " '" + strContent + "'"));
                    }
                }
            }
        }

        for (KeyboardEventMacro eventMacro : region.getKeyboardEventsProcessor().getKeyboardEventMacros()) {
            String strEvent = (eventMacro.getModifiers() + " " + eventMacro.getKey() + " " + eventMacro.getEventName()).trim();
            if (StringUtils.isNotBlank(strEvent)) {
                regionInfo.add("on " + strEvent.toLowerCase() + ": ");
                for (int i = 0; i < eventMacro.getMacro().getActions().length; i++) {
                    String strAction = (String) eventMacro.getMacro().getActions()[i][0];
                    strValue = (String) eventMacro.getMacro().getActions()[i][1];
                    String strContent = (String) eventMacro.getMacro().getActions()[i][2];
                    if (StringUtils.isNotBlank(strValue)) {
                        regionInfo.add("    " + strAction.toLowerCase() + " '" + strValue.toLowerCase() + "'" + (strContent.isEmpty() ? "" : " '" + strContent + "'"));
                    }
                }
            }
        }

        for (EventMacro eventMacro : region.getRegionOverlapEventMacros()) {
            String strEvent = "region '" + (region.getParent().getRegions().size() - region.getParent().getActionIndex(eventMacro.getMacro().getParameters().get(RegionOverlapEventMacro.PARAMETER_REGION_ID))) + "' " + eventMacro.getEventName() + " this region";
            if (StringUtils.isNotBlank(strEvent)) {
                regionInfo.add("when " + strEvent.toLowerCase() + "  then  ");
                for (int i = 0; i < eventMacro.getMacro().getActions().length; i++) {
                    String strAction = (String) eventMacro.getMacro().getActions()[i][0];
                    strValue = (String) eventMacro.getMacro().getActions()[i][1];
                    String strContent = (String) eventMacro.getMacro().getActions()[i][2];
                    if (StringUtils.isNotBlank(strValue)) {
                        regionInfo.add("    " + strAction.toLowerCase() + " '" + strValue.toLowerCase() + "'" + (strContent.isEmpty() ? "" : " '" + strContent + "'"));
                    }
                }
            }
        }

        strValue = region.getEmbeddedSketch();
        if (StringUtils.isNotBlank(strValue)) {
            regionInfo.add("embedded sketch: " + strValue);
        }
        strValue = region.getEmbeddedSketchVarPrefix();
        if (StringUtils.isNotBlank(strValue)) {
            regionInfo.add("embedded sketch variable prefix: " + strValue);
        }
        strValue = region.getEmbeddedSketchVarPostfix();
        if (StringUtils.isNotBlank(strValue)) {
            regionInfo.add("embedded sketch variable postfix: " + strValue);
        }

        return regionInfo;
    }

    public static Hashtable<String, DrawVariableInfo> prepareVariables(ActiveRegions regions) {
        Hashtable<String, DrawVariableInfo> variables = new Hashtable<String, DrawVariableInfo>();
        for (ActiveRegion region : regions.getRegions()) {
            String strValue = region.getTextField();
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = VariablesBlackboard.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] text");
            }

            String lines[] = region.getText().split("\n");
            for (int i = 0; i < lines.length; i++) {
                strValue = lines[i];
                if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                    String strVar = VariablesBlackboard.populateTemplate(strValue.substring(1));
                    processString(variables, region, strVar, "[in] text");
                }
            }
            strValue = region.getHorizontalAlignment();

            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = VariablesBlackboard.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] horizontal alignment");
            }
            strValue = region.getVerticalAlignment();
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = VariablesBlackboard.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] vertical alignment");
            }
            strValue = region.getShape();
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = VariablesBlackboard.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] shape");
            }
            strValue = region.getLineColor();
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = VariablesBlackboard.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] line color");
            }
            strValue = region.getLineThickness();
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = VariablesBlackboard.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] line thickness");
            }
            strValue = region.getLineStyle();
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = VariablesBlackboard.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] line style");
            }
            strValue = region.getFillColor();
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = VariablesBlackboard.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] fill color");
            }
            strValue = region.getCaptureScreenX();
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = VariablesBlackboard.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] capture screen x (left)");
            }
            strValue = region.getCaptureScreenY();
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = VariablesBlackboard.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] capture screen x (top)");
            }
            strValue = region.getCaptureScreenWidth();
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = VariablesBlackboard.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] capture screen width");
            }
            strValue = region.getCaptureScreenHeight();
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = VariablesBlackboard.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] capture screen height");
            }

            strValue = region.getEmbeddedSketch();
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = VariablesBlackboard.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] embedded sketch");
            }
            strValue = region.getEmbeddedSketchVarPrefix();
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = VariablesBlackboard.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] embedded sketch variable prefix");
            }
            strValue = region.getEmbeddedSketchVarPostfix();
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = VariablesBlackboard.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] embedded sketch variable postfix");
            }

            for (int i = 0; i < ActiveRegion.getPropertiesInfo().length; i++) {
                String property = ActiveRegion.getPropertiesInfo()[i][0];
                strValue = region.getProperty(property);
                if (strValue.startsWith("=") && strValue.length() > 1) {
                    String strVar = VariablesBlackboard.populateTemplate(strValue.substring(1));
                    processString(variables, region, strVar, "[in] " + property.toLowerCase());
                }
            }
            for (int i = 0; i < region.getMotionAndRotationVariablesMapping().length; i++) {
                strValue = (String) region.getMotionAndRotationVariablesMapping()[i][1];

                if (strValue.length() > 0) {
                    processString(variables, region, strValue, "[in][out] " + region.getMotionAndRotationVariablesMapping()[i][0].toString().toLowerCase());
                }
            }
            /*for (int i = 0; i < region.variablesMappingHandler.data.length; i++) {
                strValue = (String) region.variablesMappingHandler.data[i][3];

                if (strValue.length() > 0) {
                    processString(variables, region, strValue, "[in] " + region.variablesMappingHandler.data[i][0].toString().toLowerCase());
                }
            }*/
            for (EventMacro eventMacro : region.getMouseEventsProcessor().getMouseEventMacros()) {
                for (int i = 0; i < eventMacro.getMacro().getActions().length; i++) {
                    String strEvent = eventMacro.getEventName();
                    String strAction = (String) eventMacro.getMacro().getActions()[i][0];
                    strValue = (String) eventMacro.getMacro().getActions()[i][1];
                    String strContent = (String) eventMacro.getMacro().getActions()[i][2];
                    if (strAction.toLowerCase().startsWith("variable") && strContent.length() > 0) {
                        processString(variables, region, strValue, "[out] " + strEvent.toLowerCase() + ", " + strAction.toLowerCase().replace("variable", "").trim() + " with '" + strContent + "'");
                    }
                }
            }
            for (EventMacro eventMacro : region.getRegionOverlapEventMacros()) {
                for (int i = 0; i < eventMacro.getMacro().getActions().length; i++) {
                    String strEvent = "region " + (region.getParent().getRegions().size() - region.getParent().getActionIndex(eventMacro.getMacro().getParameters().get(RegionOverlapEventMacro.PARAMETER_REGION_ID))) + " " + eventMacro.getEventName();
                    String strAction = (String) eventMacro.getMacro().getActions()[i][0];
                    strValue = (String) eventMacro.getMacro().getActions()[i][1];
                    String strContent = (String) eventMacro.getMacro().getActions()[i][2];
                    if (strAction.toLowerCase().startsWith("variable") && strContent.length() > 0) {
                        processString(variables, region, strValue, "[out] " + strEvent.toLowerCase() + ", " + strAction.toLowerCase().replace("variable", "").trim() + " with '" + strContent + "'");
                    }
                }
            }
        }
        return variables;
    }

    public static Vector<String> prepareTimers(Page page, ActiveRegions regions) {
        Vector<String> timers = new Vector<String>();

        for (int i = 0; i < page.getOnEntryMacro().getActions().length; i++) {
            String strAction = (String) page.getOnEntryMacro().getActions()[i][0];
            String strParam1 = (String) page.getOnEntryMacro().getActions()[i][1];

            if (strAction.length() > 0) {
                if (strAction.toLowerCase().contains("timer") && strParam1.length() > 0 && !timers.contains(strParam1)) {
                    timers.add(strParam1);
                }
            }
        }

        for (ActiveRegion region : regions.getRegions()) {
            String strValue;
            for (EventMacro eventMacro : region.getMouseEventsProcessor().getMouseEventMacros()) {
                for (int i = 0; i < eventMacro.getMacro().getActions().length; i++) {
                    String strAction = (String) eventMacro.getMacro().getActions()[i][0];
                    strValue = (String) eventMacro.getMacro().getActions()[i][1];
                    if (strAction.toLowerCase().contains("timer") && strValue.length() > 0 && !timers.contains(strValue)) {
                        timers.add(strValue);
                    }
                }
            }
            for (EventMacro eventMacro : region.getRegionOverlapEventMacros()) {
                for (int i = 0; i < eventMacro.getMacro().getActions().length; i++) {
                    String strAction = (String) eventMacro.getMacro().getActions()[i][0];
                    strValue = (String) eventMacro.getMacro().getActions()[i][1];
                    if (strAction.toLowerCase().contains("timer") && strValue.length() > 0 && !timers.contains(strValue)) {
                        timers.add(strValue);
                    }
                }
            }
        }
        return timers;
    }

    public static void processString(Hashtable<String, DrawVariableInfo> variables, ActiveRegion region, String strVar, String strDescription) {
        DrawVariableInfo vi = variables.get(strVar.toLowerCase());
        if (vi == null) {
            vi = new DrawVariableInfo();
            vi.setName(strVar);
            variables.put(strVar.toLowerCase(), vi);
        }

        String strText = vi.getRegions().get(region);
        if (strText == null) {
            strText = strDescription;
        } else {
            strText += " / " + strDescription;
        }
        vi.getRegions().put(region, strText);

    }

    public static void drawVariables(ActiveRegions regions, Graphics2D g2, boolean bPlayback) {
        Hashtable<String, DrawVariableInfo> variables = prepareVariables(regions);
        Vector<String> strVariables = new Vector<String>();
        Page page = bPlayback ? PlaybackPanel.getCurrentPage() : SketchletEditor.getInstance().getCurrentPage();
        Vector<String> timers = prepareTimers(page, regions);
        Vector<String> infoTexts = new Vector<String>();

        int y = 50;
        int stepY = 25;
        int x = 150;
        int stepX = 110;

        Enumeration<DrawVariableInfo> info = variables.elements();
        while (info.hasMoreElements()) {
            DrawVariableInfo vi = info.nextElement();
            Variable var = VariablesBlackboard.getInstance().getVariable(vi.getName());

            String strVarValue = VariablesBlackboard.getInstance().getVariableValue(vi.getName());
            String strVarText = " " + vi.getName() + " : " + strVarValue + " >";

            infoTexts.add(strVarText);

            Font font = g2.getFont();
            FontRenderContext frc = g2.getFontRenderContext();

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g2.setStroke(new BasicStroke(1));
            g2.setColor(Color.WHITE);
            Rectangle r1 = new Rectangle(53, y + 6, (int) font.getStringBounds(strVarText, frc).getWidth() + 4, (int) font.getStringBounds(strVarText, frc).getHeight() + 6);

            g2.fill(r1);
            g2.setColor(Color.BLACK);
            g2.draw(r1);
            if (var != null && System.currentTimeMillis() - var.getTimestamp() < 1500) {
                g2.setColor(Color.RED);
                g2.setStroke(new BasicStroke(2));
            } else {
                Enumeration<ActiveRegion> r = vi.getRegions().keys();
                boolean colorSet = false;
                while (r.hasMoreElements()) {
                    ActiveRegion reg = r.nextElement();
                    if (reg.isSelected()) {
                        g2.setColor(Color.RED);
                        g2.setStroke(new BasicStroke(2));
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                        colorSet = true;
                        break;
                    }
                }
                if (!colorSet) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
                    g2.setColor(Color.BLACK);
                    g2.setStroke(new BasicStroke(1));
                }
            }
            g2.drawString(strVarText, 55, y + 20);

            strVariables.add(vi.getName().toLowerCase());

            Enumeration<ActiveRegion> r = vi.getRegions().keys();
            while (r.hasMoreElements()) {
                ActiveRegion reg = r.nextElement();
                if (reg.isSelected()) {
                    g2.setColor(Color.RED);
                    g2.setStroke(new BasicStroke(2));
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                } else {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
                    g2.setColor(Color.BLACK);
                    g2.setStroke(new BasicStroke(1));
                }
                String strText = vi.getRegions().get(reg);
                Rectangle r2 = new Rectangle(reg.getX1Value(), reg.getY1Value(), reg.getWidthValue(), reg.getHeightValue());

                GeneralPath path = getPath(r1, r2, strText.contains("[out]"), strText.contains("[in]"));
                Rectangle _r = path.getBounds();
                g2.draw(path);

                String lines[] = strText.split("\n");

                int __x = (int) r2.getCenterX();
                int __y = (int) _r.getCenterY();

                for (int i = 0; i < lines.length; i++) {
                    g2.drawString(lines[i], __x + 5, __y + 14);
                }
            }

            y += stepY;
        }

        int it = 0;
        x += 20;
        for (String strTimer : timers) {
            TimerThread timer = null;
            if (bPlayback) {
                timer = getTimerThread(PlaybackPanel.getCurrentPage().getActiveTimers(), strTimer);
            } else {
                timer = getTimerThread(SketchletEditor.getInstance().getCurrentPage().getActiveTimers(), strTimer);
            }
            if (timer != null) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
                g2.setColor(Color.GREEN.darker());
                g2.fillRect(x + 5, 27, (int) (80 * Math.min(1.0, timer.getProgress())), 8);
            } else {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
            }
            Rectangle r1 = new Rectangle(x, 10, 90, 30);
            Rectangle r2 = new Rectangle(x + 5, 27, 80, 8);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(1));
            g2.draw(r1);
            g2.setStroke(new BasicStroke(1));
            g2.setColor(Color.BLACK);
            g2.draw(r2);
            g2.drawString(strTimer, x + 5, 25);

            Timer _timer = Timers.getGlobalTimers().getTimer(strTimer);
            if (timer != null) {
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2));
            }
            for (int i = 0; i < _timer.getVariables().length; i++) {
                String strVar = (String) _timer.getVariables()[i][0];
                if (strVar.length() > 0) {
                    int index = strVariables.indexOf(strVar.toLowerCase());

                    if (index >= 0) {
                        Font font = g2.getFont();
                        FontRenderContext frc = g2.getFontRenderContext();
                        String strInfoText = infoTexts.elementAt(index);
                        g2.drawLine(x + 45, 40, x + 45, 60 + index * stepY + 0);
                        g2.drawLine(x + 45, 60 + index * stepY + 0, 56 + (int) font.getStringBounds(strInfoText, frc).getWidth(), 60 + index * stepY + 0);
                    }
                }
            }

            x += stepX;
            it++;
        }
    }

    public static TimerThread getTimerThread(java.util.List<TimerThread> timers, String strTimer) {

        if (timers != null) {
            for (TimerThread tt : timers) {
                if (tt.getTimer().getName().equalsIgnoreCase(strTimer)) {
                    return tt;
                }
            }
        }

        return null;
    }

    private static GeneralPath getPath(Rectangle r1, Rectangle r2, boolean arrow1, boolean arrow2) {
        double x1 = r1.getCenterX();
        double y1 = r1.getMaxY();
        double x2 = r2.getCenterX();
        double y2 = r2.getCenterY();
        double theta = Math.atan2(0, x2 - x1);
        Point2D.Double p1 = new Point.Double(r1.getMaxX(), y1);
        Point2D.Double p2 = new Point.Double(x2, y2);
        Point2D.Double p3 = new Point.Double(p1.x, p2.y);
        GeneralPath path = new GeneralPath();
        path.moveTo(r1.getMaxX(), y1);
        path.lineTo(x2, y1);
        path.lineTo(x2, y2);
        // Add an arrow head at p2.
        if (arrow1) {
            path.moveTo(x2, y1);
            path.lineTo(x2 - 5, y1 + 8);
            path.moveTo(x2, y1);
            path.lineTo(x2 + 5, y1 + 8);
        }
        if (arrow2) {
            path.moveTo(x2, y2);
            path.lineTo(x2 - 5, y2 - 8);
            path.moveTo(x2, y2);
            path.lineTo(x2 + 5, y2 - 8);
        }
        return path;
    }

}

class DrawVariableInfo {

    private String name;
    private Hashtable<ActiveRegion, String> regions = new Hashtable<ActiveRegion, String>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Hashtable<ActiveRegion, String> getRegions() {
        return regions;
    }
}
