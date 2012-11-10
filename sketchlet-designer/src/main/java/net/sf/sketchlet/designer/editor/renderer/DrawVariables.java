/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.renderer;

import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.communicator.server.Variable;
import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.data.ActiveRegions;
import net.sf.sketchlet.designer.data.EventMacro;
import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.designer.data.RegionOverlapEventMacro;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.programming.timers.Timer;
import net.sf.sketchlet.designer.programming.timers.TimerThread;
import net.sf.sketchlet.designer.programming.timers.Timers;
import net.sf.sketchlet.designer.ui.playback.PlaybackPanel;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class DrawVariables {
    private static final Logger log = Logger.getLogger(DrawVariables.class);

    public static Vector<String> getRegionInfo(ActiveRegion region, boolean bShowPosition) {
        Vector<String> regionInfo = new Vector<String>();

        String strValue = "region " + (region.parent.regions.size() - region.parent.regions.indexOf(region));
        regionInfo.add(strValue);

        if (region.bPinned) {
            regionInfo.add("PINNED");
        }
        strValue = region.strTextField.trim();
        if (strValue != null && !strValue.isEmpty()) {
            regionInfo.add("text: '" + strValue + "'");
        }
        String lines[] = region.strText.split("\n");
        strValue = "";
        for (int i = 0; i < lines.length; i++) {
            strValue += lines[i] + " ";
        }
        strValue = strValue.trim();
        if (strValue != null && !strValue.isEmpty()) {
            regionInfo.add("text: '" + strValue + "'");
        }

        strValue = region.shape;
        if (strValue != null && !strValue.isEmpty() && !strValue.equalsIgnoreCase("None")) {
            regionInfo.add("shape: " + strValue);
        }
        strValue = region.strLineColor;
        if (strValue != null && !strValue.isEmpty()) {
            regionInfo.add("line color: " + strValue);
        }
        strValue = region.strLineThickness;
        if (strValue != null && !strValue.isEmpty()) {
            regionInfo.add("line thickness: " + strValue);
        }
        strValue = region.strLineStyle;
        if (strValue != null && !strValue.isEmpty()) {
            regionInfo.add("line style: " + strValue);
        }
        strValue = region.strFillColor;
        if (strValue != null && !strValue.isEmpty()) {
            regionInfo.add("fill color: " + strValue);
        }
        strValue = region.strCaptureScreenX;
        if (strValue != null && !strValue.isEmpty()) {
            regionInfo.add("capture screen x (left): " + strValue);
        }
        strValue = region.strCaptureScreenY;
        if (strValue != null && !strValue.isEmpty()) {
            regionInfo.add("capture screen x (top): " + strValue);
        }
        strValue = region.strCaptureScreenWidth;
        if (strValue != null && !strValue.isEmpty()) {
            regionInfo.add("capture screen width: " + strValue);
        }
        strValue = region.strCaptureScreenHeight;
        if (strValue != null && !strValue.isEmpty()) {
            regionInfo.add("capture screen height: " + strValue);
        }
        strValue = region.strHAlign;
        if (strValue != null && !strValue.isEmpty() && !strValue.equalsIgnoreCase("null") && !strValue.equalsIgnoreCase("left")) {
            regionInfo.add("horizontal alignment: " + strValue);
        }
        strValue = region.strVAlign;
        if (strValue != null && !strValue.isEmpty() && !strValue.equalsIgnoreCase("null") && !strValue.equalsIgnoreCase("top")) {
            regionInfo.add("vertical alignment: " + strValue);
        }

        for (int i = 0; i < ActiveRegion.propertiesInfo.length; i++) {
            String property = ActiveRegion.propertiesInfo[i][0];
            strValue = region.getProperty(property);
            String strDefault = region.getDefaultValue(property);
            if (strValue != null && !strValue.isEmpty() && !strValue.equalsIgnoreCase(strDefault)) {
                regionInfo.add(property + ": " + strValue);
            }
        }

        for (int i = 0; i < region.updateTransformations.length; i++) {
            strValue = (String) region.updateTransformations[i][1];

            if (strValue != null && !strValue.isEmpty()) {
                regionInfo.add("connect dimension '" + region.updateTransformations[i][0].toString().toLowerCase() + "' to variable '" + strValue + "'");
            }
        }

        for (int i = 0; i < region.propertiesAnimation.length; i++) {
            String strType = region.propertiesAnimation[i][1];
            String strStart = region.propertiesAnimation[i][2];
            String strEnd = region.propertiesAnimation[i][3];
            String strDuration = region.propertiesAnimation[i][4];
            String strCurve = region.propertiesAnimation[i][5];

            if (strType != null && !strType.isEmpty()) {
                regionInfo.add("animate property '" + region.propertiesAnimation[i][0].toString().toLowerCase() + "'" + " from " + strStart + " to " + strEnd + " in " + strDuration + " seconds" + (strCurve.isEmpty() ? "" : " using curve '" + strCurve + "'"));
            }
        }

        for (EventMacro eventMacro : region.mouseProcessor.mouseEventMacros) {
            for (int i = 0; i < eventMacro.getMacro().actions.length; i++) {
                String strEvent = eventMacro.getEventName();
                String strAction = (String) eventMacro.getMacro().actions[i][0];
                strValue = (String) eventMacro.getMacro().actions[i][1];
                String strContent = (String) eventMacro.getMacro().actions[i][2];
                if (strValue != null && !strValue.isEmpty()) {
                    regionInfo.add("on " + strEvent.toLowerCase() + ": " + strAction.toLowerCase() + " '" + strValue.toLowerCase() + "'" + (strContent.isEmpty() ? "" : " '" + strContent + "'"));
                }
            }
        }

        for (EventMacro eventMacro : region.regionOverlapEventMacros) {
            for (int i = 0; i < eventMacro.getMacro().actions.length; i++) {
                String strEvent = "region '" + (region.parent.regions.size() - region.parent.getActionIndex(eventMacro.getMacro().getParameters().get(RegionOverlapEventMacro.PARAMETER_REGION_ID))) + "' " + eventMacro.getEventName() + " this region";
                String strAction = (String) eventMacro.getMacro().actions[i][0];
                strValue = (String) eventMacro.getMacro().actions[i][1];
                String strContent = (String) eventMacro.getMacro().actions[i][2];
                if (strValue != null && !strValue.isEmpty()) {
                    regionInfo.add("when " + strEvent.toLowerCase() + "  then  " + strAction.toLowerCase() + " '" + strValue.toLowerCase() + "'" + (strContent.isEmpty() ? "" : " '" + strContent + "'"));
                }
            }
        }

        strValue = region.strEmbeddedSketch;
        if (strValue != null && !strValue.isEmpty()) {
            regionInfo.add("embedded sketch: " + strValue);
        }
        strValue = region.strEmbeddedSketchVarPrefix;
        if (strValue != null && !strValue.isEmpty()) {
            regionInfo.add("embedded sketch variable prefix: " + strValue);
        }
        strValue = region.strEmbeddedSketchVarPostfix;
        if (strValue != null && !strValue.isEmpty()) {
            regionInfo.add("embedded sketch variable postfix: " + strValue);
        }

        return regionInfo;
    }

    public static Hashtable<String, DrawVariableInfo> prepareVariables(ActiveRegions regions) {
        Hashtable<String, DrawVariableInfo> variables = new Hashtable<String, DrawVariableInfo>();
        for (ActiveRegion region : regions.regions) {
            String strValue = region.strTextField;
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = DataServer.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] text");
            }

            String lines[] = region.strText.split("\n");
            for (int i = 0; i < lines.length; i++) {
                strValue = lines[i];
                if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                    String strVar = DataServer.populateTemplate(strValue.substring(1));
                    processString(variables, region, strVar, "[in] text");
                }
            }
            strValue = region.strHAlign;

            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = DataServer.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] horizontal alignment");
            }
            strValue = region.strVAlign;
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = DataServer.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] vertical alignment");
            }
            strValue = region.shape;
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = DataServer.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] shape");
            }
            strValue = region.strLineColor;
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = DataServer.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] line color");
            }
            strValue = region.strLineThickness;
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = DataServer.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] line thickness");
            }
            strValue = region.strLineStyle;
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = DataServer.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] line style");
            }
            strValue = region.strFillColor;
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = DataServer.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] fill color");
            }
            strValue = region.strCaptureScreenX;
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = DataServer.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] capture screen x (left)");
            }
            strValue = region.strCaptureScreenY;
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = DataServer.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] capture screen x (top)");
            }
            strValue = region.strCaptureScreenWidth;
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = DataServer.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] capture screen width");
            }
            strValue = region.strCaptureScreenHeight;
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = DataServer.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] capture screen height");
            }

            strValue = region.strEmbeddedSketch;
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = DataServer.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] embedded sketch");
            }
            strValue = region.strEmbeddedSketchVarPrefix;
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = DataServer.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] embedded sketch variable prefix");
            }
            strValue = region.strEmbeddedSketchVarPostfix;
            if (strValue != null && strValue.startsWith("=") && strValue.length() > 1) {
                String strVar = DataServer.populateTemplate(strValue.substring(1));
                processString(variables, region, strVar, "[in] embedded sketch variable postfix");
            }

            for (int i = 0; i < ActiveRegion.propertiesInfo.length; i++) {
                String property = ActiveRegion.propertiesInfo[i][0];
                strValue = region.getProperty(property);
                if (strValue.startsWith("=") && strValue.length() > 1) {
                    String strVar = DataServer.populateTemplate(strValue.substring(1));
                    processString(variables, region, strVar, "[in] " + property.toLowerCase());
                }
            }
            for (int i = 0; i < region.updateTransformations.length; i++) {
                strValue = (String) region.updateTransformations[i][1];

                if (strValue.length() > 0) {
                    processString(variables, region, strValue, "[in][out] " + region.updateTransformations[i][0].toString().toLowerCase());
                }
            }
            /*for (int i = 0; i < region.variablesMappingHandler.data.length; i++) {
                strValue = (String) region.variablesMappingHandler.data[i][3];

                if (strValue.length() > 0) {
                    processString(variables, region, strValue, "[in] " + region.variablesMappingHandler.data[i][0].toString().toLowerCase());
                }
            }*/
            for (EventMacro eventMacro : region.mouseProcessor.mouseEventMacros) {
                for (int i = 0; i < eventMacro.getMacro().actions.length; i++) {
                    String strEvent = eventMacro.getEventName();
                    String strAction = (String) eventMacro.getMacro().actions[i][0];
                    strValue = (String) eventMacro.getMacro().actions[i][1];
                    String strContent = (String) eventMacro.getMacro().actions[i][2];
                    if (strAction.toLowerCase().startsWith("variable") && strContent.length() > 0) {
                        processString(variables, region, strValue, "[out] " + strEvent.toLowerCase() + ", " + strAction.toLowerCase().replace("variable", "").trim() + " with '" + strContent + "'");
                    }
                }
            }
            for (EventMacro eventMacro : region.regionOverlapEventMacros) {
                for (int i = 0; i < eventMacro.getMacro().actions.length; i++) {
                    String strEvent = "region " + (region.parent.regions.size() - region.parent.getActionIndex(eventMacro.getMacro().getParameters().get(RegionOverlapEventMacro.PARAMETER_REGION_ID))) + " " + eventMacro.getEventName();
                    String strAction = (String) eventMacro.getMacro().actions[i][0];
                    strValue = (String) eventMacro.getMacro().actions[i][1];
                    String strContent = (String) eventMacro.getMacro().actions[i][2];
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

        for (int i = 0; i < page.onEntryMacro.actions.length; i++) {
            String strAction = (String) page.onEntryMacro.actions[i][0];
            String strParam1 = (String) page.onEntryMacro.actions[i][1];

            if (strAction.length() > 0) {
                if (strAction.toLowerCase().contains("timer") && strParam1.length() > 0 && !timers.contains(strParam1)) {
                    timers.add(strParam1);
                }
            }
        }

        for (ActiveRegion region : regions.regions) {
            String strValue;
            for (EventMacro eventMacro : region.mouseProcessor.mouseEventMacros) {
                for (int i = 0; i < eventMacro.getMacro().actions.length; i++) {
                    String strAction = (String) eventMacro.getMacro().actions[i][0];
                    strValue = (String) eventMacro.getMacro().actions[i][1];
                    if (strAction.toLowerCase().contains("timer") && strValue.length() > 0 && !timers.contains(strValue)) {
                        timers.add(strValue);
                    }
                }
            }
            for (EventMacro eventMacro : region.regionOverlapEventMacros) {
                for (int i = 0; i < eventMacro.getMacro().actions.length; i++) {
                    String strAction = (String) eventMacro.getMacro().actions[i][0];
                    strValue = (String) eventMacro.getMacro().actions[i][1];
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
            vi.name = strVar;
            variables.put(strVar.toLowerCase(), vi);
        }

        String strText = vi.regions.get(region);
        if (strText == null) {
            strText = strDescription;
        } else {
            strText += " / " + strDescription;
        }
        vi.regions.put(region, strText);

    }

    public static void drawVariables(ActiveRegions regions, Graphics2D g2, boolean bPlayback) {
        Hashtable<String, DrawVariableInfo> variables = prepareVariables(regions);
        Vector<String> strVariables = new Vector<String>();
        Page page = bPlayback ? PlaybackPanel.currentPage : SketchletEditor.editorPanel.currentPage;
        Vector<String> timers = prepareTimers(page, regions);
        Vector<String> infoTexts = new Vector<String>();

        int y = 50;
        int stepY = 25;
        int x = 150;
        int stepX = 110;

        Enumeration<DrawVariableInfo> info = variables.elements();
        while (info.hasMoreElements()) {
            DrawVariableInfo vi = info.nextElement();
            Variable var = DataServer.variablesServer.getVariable(vi.name);

            String strVarValue = DataServer.variablesServer.getVariableValue(vi.name);
            String strVarText = " " + vi.name + " : " + strVarValue + " >";

            infoTexts.add(strVarText);

            Font font = g2.getFont();
            FontRenderContext frc = g2.getFontRenderContext();

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g2.setStroke(new BasicStroke(1));
            g2.setColor(Color.WHITE);
            Rectangle r1 = new Rectangle(53, y + 6, (int) font.getStringBounds(strVarText, frc).getWidth() + 4, (int) font.getStringBounds(strVarText, frc).getHeight() + 6);

            vi.textWidth = (int) font.getStringBounds(strVarText, frc).getWidth() + 4;

            g2.fill(r1);
            g2.setColor(Color.BLACK);
            g2.draw(r1);
            if (var != null && System.currentTimeMillis() - var.timestamp < 1500) {
                g2.setColor(Color.RED);
                g2.setStroke(new BasicStroke(2));
            } else {
                Enumeration<ActiveRegion> r = vi.regions.keys();
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

            strVariables.add(vi.name.toLowerCase());

            /*
            float dash1[] = {4.0f};
            float thick = 0.5f;
            BasicStroke dashed = new BasicStroke(thick,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            10.0f, dash1, 0.0f);

            g2.setStroke(dashed);
             */
            Enumeration<ActiveRegion> r = vi.regions.keys();
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
                String strText = vi.regions.get(reg);
                Rectangle r2 = (bPlayback) ? new Rectangle(reg.playback_x1, reg.playback_y1, reg.getPlaybackWidth(), reg.getHeight()) : new Rectangle(reg.x1, reg.y1, reg.getWidth(), reg.getPlaybackHeight());

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
                timer = getTimerThread(PlaybackPanel.currentPage.activeTimers, strTimer);
                /*if (timer == null && PlaybackPanel.currentSketch.onEntryMacroThread != null) {
                    timer = getTimerThread(PlaybackPanel.currentSketch.onEntryMacroThread.activeMacroInternalTimers, strTimer);
                }
                if (timer == null && PlaybackPanel.currentSketch.onExitMacroThread != null) {
                    timer = getTimerThread(PlaybackPanel.currentSketch.onExitMacroThread.activeMacroInternalTimers, strTimer);
                }*/
            } else {
                timer = getTimerThread(SketchletEditor.editorPanel.currentPage.activeTimers, strTimer);
                /*if (timer == null && SketchletEditor.editorPanel.currentSketch.onEntryMacroThread != null) {
                    timer = getTimerThread(SketchletEditor.editorPanel.currentSketch.onEntryMacroThread.activeMacroInternalTimers, strTimer);
                }
                if (timer == null && SketchletEditor.editorPanel.currentSketch.onExitMacroThread != null) {
                    timer = getTimerThread(SketchletEditor.editorPanel.currentSketch.onExitMacroThread.activeMacroInternalTimers, strTimer);
                }*/
            }
            if (timer != null) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
                g2.setColor(Color.GREEN.darker());
                g2.fillRect(x + 5, 27, (int) (80 * Math.min(1.0, timer.progress)), 8);
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

            Timer _timer = Timers.globalTimers.getTimer(strTimer);
            if (timer != null) {
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2));
            }
            for (int i = 0; i < _timer.variables.length; i++) {
                String strVar = (String) _timer.variables[i][0];
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

    public static TimerThread getTimerThread(Vector<TimerThread> timers, String strTimer) {

        if (timers != null) {
            for (TimerThread tt : timers) {
                if (tt.timer.name.equalsIgnoreCase(strTimer)) {
                    return tt;
                }
            }
        }

        return null;
    }

    static int barb = 10;
    static double phi = Math.toRadians(20);

    private static GeneralPath getPath2(Rectangle r1, Rectangle r2, boolean arrow1, boolean arrow2) {
        double x1 = r1.getCenterX();
        double y1 = r1.getCenterY();
        double x2 = r2.getCenterX();
        double y2 = r2.getCenterY();
        double theta = Math.atan2(y2 - y1, x2 - x1);
        Point2D.Double p1 = new Point.Double(r1.getMaxX(), y1);
        Point2D.Double p2 = new Point.Double(x2, y2);
        GeneralPath path = new GeneralPath(new Line2D.Float(p1, p2));
        // Add an arrow head at p2.
        if (arrow1) {
            double x = p1.x + barb * Math.cos(theta - phi);
            double y = p1.y + barb * Math.sin(theta - phi);
            path.moveTo((float) x, (float) y);
            path.lineTo((float) p1.x, (float) p1.y);
            x = p1.x + barb * Math.cos(theta + phi);
            y = p1.y + barb * Math.sin(theta + phi);
            path.lineTo((float) x, (float) y);
        }
        if (arrow2) {
            double x = p2.x + barb * Math.cos(theta + Math.PI - phi);
            double y = p2.y + barb * Math.sin(theta + Math.PI - phi);
            path.moveTo((float) x, (float) y);
            path.lineTo((float) p2.x, (float) p2.y);
            x = p2.x + barb * Math.cos(theta + Math.PI + phi);
            y = p2.y + barb * Math.sin(theta + Math.PI + phi);
            path.lineTo((float) x, (float) y);
        }
        return path;
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
        /*        if (arrow1) {
        double x = p3.x + barb * Math.cos(theta - phi);
        double y = p3.y + barb * Math.sin(theta - phi);
        path.moveTo((float) x, (float) y);
        path.lineTo((float) p1.x, (float) p1.y);
        x = p1.x + barb * Math.cos(theta + phi);
        y = p1.y + barb * Math.sin(theta + phi);
        path.lineTo((float) x, (float) y);
        }
        if (arrow2) {
        double x = p2.x + barb * Math.cos(theta + Math.PI - phi);
        double y = p2.y + barb * Math.sin(theta + Math.PI - phi);
        path.moveTo((float) x, (float) y);
        path.lineTo((float) p2.x, (float) p2.y);
        x = p2.x + barb * Math.cos(theta + Math.PI + phi);
        y = p2.y + barb * Math.sin(theta + Math.PI + phi);
        path.lineTo((float) x, (float) y);
        }*/
        return path;
    }

    private static Point2D.Double getPoint(double theta, Rectangle r) {
        double cx = r.getCenterX();
        double cy = r.getCenterY();
        double w = r.width / 2;
        double h = r.height / 2;
        double d = Point2D.distance(cx, cy, cx + w, cy + h);
        double x = cx + d * Math.cos(theta);
        double y = cy + d * Math.sin(theta);
        Point2D.Double p = new Point2D.Double();
        int outcode = r.outcode(x, y);
        switch (outcode) {
            case Rectangle.OUT_TOP:
                p.x = cx - h * ((x - cx) / (y - cy));
                p.y = cy - h;
                break;
            case Rectangle.OUT_LEFT:
                p.x = cx - w;
                p.y = cy - w * ((y - cy) / (x - cx));
                break;
            case Rectangle.OUT_BOTTOM:
                p.x = cx + h * ((x - cx) / (y - cy));
                p.y = cy + h;
                break;
            case Rectangle.OUT_RIGHT:
                p.x = cx + w;
                p.y = cy + w * ((y - cy) / (x - cx));
                break;
            default:
                log.error("Non-cardinal outcode: " + outcode);
        }
        return p;
    }
}

class DrawVariableInfo {

    String name;
    Hashtable<ActiveRegion, String> regions = new Hashtable<ActiveRegion, String>();
    int textWidth = 0;
}
