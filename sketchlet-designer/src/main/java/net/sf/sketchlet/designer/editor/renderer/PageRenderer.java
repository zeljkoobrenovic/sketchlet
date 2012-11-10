/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.renderer;

import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.context.SketchletPlaybackContext;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.context.SketchletContextImpl;
import net.sf.sketchlet.designer.context.SketchletGraphicsContextImpl;
import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.data.Evaluator;
import net.sf.sketchlet.designer.data.KeyboardEventMacro;
import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.designer.data.VariableUpdateEventMacro;
import net.sf.sketchlet.designer.editor.EditorMode;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.regions.connector.Connector;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.designer.tools.imagecache.ImageCache;
import net.sf.sketchlet.designer.ui.profiles.Profiles;
import net.sf.sketchlet.util.Colors;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author zobrenovic
 */
public class PageRenderer {
    private static final Logger log = Logger.getLogger(PageRenderer.class);

    public RendererInterface panel;
    public static Image entryIcon = Workspace.createImageIcon("resources/entry.gif").getImage();
    public static Image exitIcon = Workspace.createImageIcon("resources/exit.gif").getImage();
    public static Image variableIconIn = Workspace.createImageIcon("resources/variable_in.jpg").getImage();
    public static Image variableIconOut = Workspace.createImageIcon("resources/variable_out.jpg").getImage();
    public static Image keyboardIcon = Workspace.createImageIcon("resources/keyboard.png").getImage();
    public static Image propertiesIcon = Workspace.createImageIcon("resources/details_transparent.png").getImage();
    // public Rulers rulers = new Rulers();

    public PageRenderer(RendererInterface panel) {
        this.panel = panel;
    }

    public void dispose() {
        this.panel = null;
    }

    public void prepare(boolean bPlayback, boolean bProcessLimits) {
        for (ActiveRegion r : panel.getSketch().regions.regions) {
            r.renderer.prepare(bPlayback, bProcessLimits);
        }
    }

    public void draw(Graphics g, boolean bPlayback, boolean bDrawRect, boolean bHighlightRegions) {
        if (panel == null || !panel.isActive()) {
            return;
        }

        SketchletPlaybackContext.getInstance().setCurrentPanel(panel.getComponent());
        SketchletPlaybackContext.getInstance().setMargin(panel.getMarginX(), panel.getMarginY());
        SketchletPlaybackContext.getInstance().setScale(panel.getScaleX());
        /*for (int i = 0; i < panel.getImageCount(); i++) {
        if (panel.getImage(i) == null) {
        initImage(i);
        }
        }*/

        double s = Math.min(1, SketchletEditor.editorPanel.scale);
        int imageOffset[] = panel.getSketch().getBackgroundOffset(bPlayback);
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform oldTransformation = g2.getTransform();
        Stroke oldStroke = g2.getStroke();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color bgColor = Colors.getColor(panel.getSketch().getPropertyValue("background color"));
        if (bgColor == null) {
            bgColor = Workspace.sketchBackground;
        }
        g2.setPaint(bgColor);
        g2.fillRect(0, 0, 5000, 5000);
        float transparency = 1.0f;
        if (bPlayback) {
            String strTransparency = panel.getSketch().getPropertyValue("transparency");
            if (!strTransparency.isEmpty() && !strTransparency.equals("NaN")) {
                try {
                    transparency = (float) Double.parseDouble(strTransparency);
                } catch (Exception e) {
                }
            }
        }

        if (!bPlayback) {
            transparency = (float) (transparency * SketchletEditor.transparencyFactor);
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));

        int w = panel.getImage(0) != null ? panel.getImage(0).getWidth() : (int) InteractionSpace.sketchWidth;
        int h = panel.getImage(0) != null ? panel.getImage(0).getHeight() : (int) InteractionSpace.sketchHeight;

        if (!bPlayback) {
            g2.scale(panel.getScaleX(), panel.getScaleY());
            g2.translate(panel.getMarginX(), panel.getMarginY());
        }
        g2.setPaint(bgColor);
        g2.fillRect(0, 0, w, h);
        g2.setPaint(Color.BLACK);
        g2.setStroke(new BasicStroke(1));
        Font oldFont = g.getFont();
        g.setFont(oldFont.deriveFont((float) (oldFont.getSize() / s)));
        if (bDrawRect) {
            g2.drawRect(-1, -1, w + 2, h + 2);
            g2.setPaint(Color.GRAY);
            if (SketchletEditor.editorPanel.mode == EditorMode.SKETCHING) {
                g2.drawString("layer " + (this.panel.getLayer() + 1), 0, -3);
            }
        }

        if (panel.getMasterPage() != null && panel.getMasterPage() != panel.getSketch()) {
            if (panel.getMasterImage() == null) {
                initMasterImage();
            }
            g2.drawImage(panel.getMasterImage(), imageOffset[0], imageOffset[1], null);

            if (panel.getSketch().bLayerRegions && panel.getMasterPage() != null && panel.getMasterPage().regions != null) {
                panel.getMasterPage().regions.draw((Graphics2D) g, panel.getComponent(), EditorMode.SKETCHING, bPlayback, bHighlightRegions, transparency);
            }
        }

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f * transparency));

        for (int l = 0; l < Page.NUMBER_OF_LAYERS; l++) {
            boolean bDraw = this.panel.getSketch().isLayerActive(l, bPlayback);
            if (bDraw) {
                float t = 1.0f;
                try {
                    String strTransparency = this.panel.getSketch().getPropertyValue("transparency layer " + (l + 1));
                    if (strTransparency != null && !strTransparency.isEmpty()) {
                        strTransparency = Evaluator.processText(strTransparency, "", "");
                        t = (float) Double.parseDouble(strTransparency);
                    }
                } catch (Exception e) {
                    log.error(e);
                }
                if (t > 1) {
                    t = 1.0f;
                }
                if (t > 0.01f) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, t * transparency));
                    g2.drawImage(panel.getImage(l), imageOffset[0], imageOffset[1], null);
                }
            }

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f * transparency));
            if (l == 0 && !bPlayback) {
                drawGrid(g2, w, h);
            }

            // g.drawString("" + PlaybackPanel.repaintCounter, 10, 18);

            if (panel.getSketch() != null && panel.getSketch().bLayerRegions) {
                for (Connector c : panel.getSketch().connectors) {
                    c.renderer.draw(g2, bPlayback);
                }

                panel.getSketch().regions.draw(l, (Graphics2D) g, panel.getComponent(), panel.getMode(), bPlayback, bHighlightRegions, transparency);
            }

        }

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f * transparency));
        /*
        if (panel.getLayer() == 1) {
        g2.drawImage(panel.getImage(1), imageOffset[0], imageOffset[1], null);
        }*/

        if ((DataServer.scriptFiles != null && DataServer.scriptFiles.size() > 0) || DataServer.drawExternal) {
            g2.setColor(Color.BLACK);
            g2.drawImage(SketchletContextImpl.image, imageOffset[0], imageOffset[1], null);
        }

        panel.extraDraw(g2);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f * transparency));

        if (SketchletEditor.editorPanel.sketchToolbar.bVisualizeVariables) {
            DrawVariables.drawVariables(panel.getSketch().regions, g2, bPlayback);
        }

        if (!bPlayback && (panel.getMode() == EditorMode.ACTIONS || panel.getMode() == EditorMode.SKETCHING)) {
            if (FileDrop.bDragging || SketchletEditor.editorPanel.inCtrlMode) {
                if (Profiles.isActive("page_actions")) {
                    g2.drawImage(entryIcon, (int) (5 / s), (int) (5 / s), (int) (24 / s), (int) (24 / s), null);
                    g2.drawImage(exitIcon, (int) (5 / s), (int) (35 / s), (int) (24 / s), (int) (24 / s), null);
                    g2.drawImage(keyboardIcon, (int) (5 / s), (int) (95 / s), (int) (24 / s), (int) (24 / s), null);
                }
                if (Profiles.isActive("page_actions,variables")) {
                    g2.drawImage(variableIconIn, (int) (5 / s), (int) (65 / s), (int) (24 / s), (int) (24 / s), null);
                }                //g2.drawImage(variableIconOut, 5, 95, null);
                if (Profiles.isActive("page_properties")) {
                    g2.drawImage(propertiesIcon, (int) (5 / s), (int) (125 / s), (int) (24 / s), (int) (24 / s), null);
                }
            }

            int x = (int) (FileDrop.mouseX / SketchletEditor.editorPanel.scale);
            int y = (int) (FileDrop.mouseY / SketchletEditor.editorPanel.scale);
            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(2));
            if (SketchletEditor.editorPanel.isInEntryArea(x, y)) {
                g2.drawRoundRect((int) (2 / s), (int) (3 / s), (int) (28 / s), (int) (28 / s), (int) (12 / s), (int) (12 / s));
                g2.drawString("on sketch entry events", (int) (36 / s), (int) (11 / s));
            }
            if (SketchletEditor.editorPanel.isInExitArea(x, y)) {
                g2.drawRoundRect((int) (2 / s), (int) (33 / s), (int) (28 / s), (int) (28 / s), (int) (12 / s), (int) (12 / s));
                g2.drawString("on sketch exit events", (int) (36 / s), (int) (41 / s));
            }
            if (SketchletEditor.editorPanel.isInVariableInArea(x, y)) {
                g2.drawRoundRect((int) (2 / s), (int) (63 / s), (int) (28 / s), (int) (28 / s), (int) (12 / s), (int) (12 / s));
                g2.drawString("on variable updates events", (int) (36 / s), (int) (71 / s));
            }
            /*if (SketchletEditor.editorPanel.isInVariableOutArea(x, y)) {
            g2.drawRoundRect(2, 93, 28, 28, 12, 12);
            g2.drawString("on variable updates events", 36, 101);
            }*/
            if (SketchletEditor.editorPanel.isInKeyboardArea(x, y)) {
                g2.drawRoundRect((int) (2 / s), (int) (93 / s), (int) (28 / s), (int) (28 / s), (int) (12 / s), (int) (12 / s));
                g2.drawString("on keyboard events", (int) (36 / s), (int) (101 / s));
            }
            if (SketchletEditor.editorPanel.isInPropertiesArea(x, y) && FileDrop.currentString.startsWith("=")) {
                g2.drawRoundRect((int) (2 / s), (int) (123 / s), (int) (28 / s), (int) (28 / s), (int) (12 / s), (int) (12 / s));
                g2.drawString("page parameters", (int) (36 / s), (int) (131 / s));
            }
        }

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f * transparency));

        int y = panel.getMarginY() + 20;
        if (SketchletEditor.editorPanel.sketchToolbar.bVisualizeInfoSketch) {
            y = drawSketchInfo(g2);
        }
        if (SketchletEditor.editorPanel.sketchToolbar.bVisualizeInfoVariables) {
            drawVariablesInfo(g2, y);
        }

        if (panel.getSketch().remoteX >= 0 && panel.getSketch().remoteY >= 0) {
            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(1.0f));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            int _s = 11;
            g2.drawOval(panel.getSketch().remoteX - _s / 2, panel.getSketch().remoteY - _s / 2, _s, _s);
        }

        SketchletGraphicsContextImpl.paint(g2);

        if (oldTransformation != null) {
            g2.setTransform(oldTransformation);
        }
        if (oldStroke != null) {
            g2.setStroke(oldStroke);
        }
    }

    public int drawSketchInfo(Graphics2D g2) {
        int x = panel.getMarginX() + 35;
        int y = panel.getMarginY() + 20;
        int yStep = 15;

        drawTexWithBackground(g2, "page " + panel.getSketch().title, x, y, Color.DARK_GRAY);
        y += yStep;

        for (int i = 0; i < panel.getSketch().properties.length; i++) {
            if (panel.getSketch().properties[i][1] != null && !panel.getSketch().properties[i][1].toString().isEmpty()) {
                drawTexWithBackground(g2, panel.getSketch().properties[i][0].toString() + ": " + panel.getSketch().properties[i][1].toString(), x, y, Color.BLACK);
                y += yStep;
            }
        }
        int level = 0;
        int startLevel = 0;
        for (int i = 0; i < panel.getSketch().onEntryMacro.actions.length; i++) {
            if (!panel.getSketch().onEntryMacro.actions[i][0].toString().isEmpty()) {
                drawTexWithBackground(g2, "on page entry:", x, y, Color.BLUE);
                y += yStep;

                if (panel.getSketch().onEntryMacro.repeat == 0) {
                    drawTexWithBackground(g2, "    repeat forever", x, y, Color.BLACK);
                    y += yStep;
                    level++;
                    startLevel++;
                } else if (panel.getSketch().onEntryMacro.repeat > 1) {
                    drawTexWithBackground(g2, "    repeat " + panel.getSketch().onEntryMacro.repeat + " times", x, y, Color.BLACK);
                    y += yStep;
                    level++;
                    startLevel++;
                }

                break;
            }
        }
        for (int i = 0; i < panel.getSketch().onEntryMacro.actions.length; i++) {
            if (!panel.getSketch().onEntryMacro.actions[i][0].toString().isEmpty()) {
                String strAction = (String) panel.getSketch().onEntryMacro.actions[i][0];
                String strParam1 = (String) panel.getSketch().onEntryMacro.actions[i][1];
                if (!strParam1.isEmpty()) {
                    strParam1 = "'" + strParam1 + "'";
                }
                String strParam2 = (String) panel.getSketch().onEntryMacro.actions[i][2];
                if (!strParam2.isEmpty()) {
                    strParam2 = "'" + strParam2 + "'";
                }

                if (strAction.equalsIgnoreCase("end") || strAction.isEmpty()) {
                    if (level > startLevel) {
                        level--;
                    }
                }

                String strPrefix = "    ";
                for (int l = 0; l < level; l++) {
                    strPrefix += "    ";
                }

                drawTexWithBackground(g2, strPrefix + strAction.toLowerCase() + " " + strParam1.toLowerCase() + " " + strParam2.toLowerCase(), x, y, Color.BLACK);

                if (strAction.equalsIgnoreCase("if") || strAction.equalsIgnoreCase("repeat")) {
                    level++;
                }

                y += yStep;
            }
        }
        for (int i = 0; i < panel.getSketch().onExitMacro.actions.length; i++) {
            if (!panel.getSketch().onExitMacro.actions[i][0].toString().isEmpty()) {
                drawTexWithBackground(g2, "on page exit:", x, y, Color.BLUE);
                y += yStep;
                break;
            }
        }
        for (int i = 0; i < panel.getSketch().onExitMacro.actions.length; i++) {
            if (!panel.getSketch().onExitMacro.actions[i][0].toString().isEmpty()) {
                String strAction = (String) panel.getSketch().onExitMacro.actions[i][0];
                String strParam1 = "'" + (String) panel.getSketch().onExitMacro.actions[i][1] + "'";
                String strParam2 = (String) panel.getSketch().onExitMacro.actions[i][2];
                if (!strParam2.isEmpty()) {
                    strParam2 = "'" + strParam2 + "'";
                }
                drawTexWithBackground(g2, "     " + strAction.toLowerCase() + " " + strParam1.toLowerCase() + " " + strParam2.toLowerCase(), x, y, Color.BLACK);
                y += yStep;
            }
        }

        if (panel.getSketch().keyboardProcessor.keyboardEventMacros.size() > 0) {
            drawTexWithBackground(g2, "on keyboard events:", x, y, Color.BLUE);
            y += yStep;
            for (KeyboardEventMacro keyboardEventMacro : panel.getSketch().keyboardProcessor.keyboardEventMacros) {
                String eventInfo = (keyboardEventMacro.getModifiers() + " " + keyboardEventMacro.getKey()).trim() + " " + keyboardEventMacro.getEventName();
                drawTexWithBackground(g2, "    when " + eventInfo + " then", x, y, Color.BLUE);
                y += yStep;
                for (Object action[] : keyboardEventMacro.getMacro().actions) {
                    drawTexWithBackground(g2, "        " + action[0] + " " + action[1] + " " + action[2], x, y, Color.BLUE);
                    y += yStep;
                }
            }
        }

        if (panel.getSketch().variableUpdateEventMacros.size() > 0) {
            drawTexWithBackground(g2, "on keyboard events:", x, y, Color.BLUE);
            y += yStep;
            for (VariableUpdateEventMacro variableUpdateEventMacro : panel.getSketch().variableUpdateEventMacros) {
                String eventInfo = (variableUpdateEventMacro.getVariable() + " " + variableUpdateEventMacro.getOperator()).trim() + " " + variableUpdateEventMacro.getValue() + " " + variableUpdateEventMacro.getEventName();
                drawTexWithBackground(g2, "    when " + eventInfo + " then", x, y, Color.BLUE);
                y += yStep;
                for (Object action[] : variableUpdateEventMacro.getMacro().actions) {
                    drawTexWithBackground(g2, "        " + action[0] + " " + action[1] + " " + action[2], x, y, Color.BLUE);
                    y += yStep;
                }
            }
        }

        for (int i = 0; i < panel.getSketch().propertiesAnimation.length; i++) {
            if (panel.getSketch().propertiesAnimation[i][1] != null && !panel.getSketch().propertiesAnimation[i][1].toString().isEmpty()) {
                drawTexWithBackground(g2, "animation", x, y, Color.BLUE);
                y += yStep;
                break;
            }
        }
        for (int i = 0; i < panel.getSketch().propertiesAnimation.length; i++) {
            if (panel.getSketch().propertiesAnimation[i][1] == null) {
                continue;
            }
            String property = panel.getSketch().propertiesAnimation[i][0].toString();
            String type = panel.getSketch().propertiesAnimation[i][1].toString();
            String start = panel.getSketch().propertiesAnimation[i][2].toString();
            String end = panel.getSketch().propertiesAnimation[i][3].toString();
            String duration = panel.getSketch().propertiesAnimation[i][4].toString();
            String curve = panel.getSketch().propertiesAnimation[i][5].toString();

            if (!type.isEmpty() && !start.isEmpty() && !end.isEmpty() && !duration.isEmpty()) {
                String strCond = "    animate property '" + property + "'";
                strCond += " from " + start + " to " + end + " in " + duration + " seconds";
                if (!curve.isEmpty()) {
                    strCond += " using curve '" + curve + "'";
                }
                drawTexWithBackground(g2, strCond, x, y, Color.BLACK);
                y += yStep;
            }
        }
        return y;
    }

    public void drawVariablesInfo(Graphics2D g2, int y) {
        int x = panel.getMarginX() + 35;
        int yStep = 15;
        y += yStep;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    public void drawTexWithBackground(Graphics2D g2, String strText, int x, int y, Color color) {
        Font font = g2.getFont();
        FontRenderContext frc = g2.getFontRenderContext();
        LineMetrics metrics = font.getLineMetrics(strText, frc);
        metrics = font.getLineMetrics(strText, frc);
        int width = (int) font.getStringBounds(strText, frc).getWidth();
        int height = (int) metrics.getHeight();
        g2.setColor(new Color(255, 255, 255, 185));
        g2.fillRect(x, (int) (y - metrics.getAscent()), width, height);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2.setColor(color);
        g2.drawString(strText, x, y);
    }

    public void drawGrid(Graphics2D g2, int w, int h) {
        if (SketchletEditor.editorPanel.snapToGrid) {
            g2.setColor(Color.DARK_GRAY);
            float dash1[] = {4.0f};
            float thick = 0.5f;
            BasicStroke dashed = new BasicStroke(thick, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);

            g2.setStroke(dashed);
            for (int i = 1; i <= w / InteractionSpace.gridSpacing; i++) {
                int x = i * InteractionSpace.gridSpacing;
                g2.drawLine(x, 0, x, h);
            }
            for (int i = 1; i <= w / InteractionSpace.gridSpacing; i++) {
                int y = i * InteractionSpace.gridSpacing;
                g2.drawLine(0, y, w, y);
            }
        }
        panel.getSketch().calculateHorizonPoint();
        drawPerspectiveLines(g2, panel.getSketch().perspective_horizont_x1, panel.getSketch().perspective_horizont_y, w, h);
        if (panel.getSketch().getPropertyValue("perspective type").equalsIgnoreCase("2 points")) {
            drawPerspectiveLines(g2, panel.getSketch().perspective_horizont_x2, panel.getSketch().perspective_horizont_y, w, h);
        }
    }

    public void drawPerspectiveLines(Graphics2D g2, double h_x, double h_y, int w, int h) {
        if (SketchletEditor.editorPanel.showPerspectiveLines || SketchletEditor.editorPanel.perspectivePanel.showPerspectiveGrid.isSelected()) {
            g2.setColor(new Color(100, 100, 100, 200));
            g2.setStroke(new BasicStroke(1.0f));

            g2.drawLine(0, (int) h_y, w, (int) h_y);
            g2.drawLine((int) h_x, 0, (int) h_x, h);

            if (panel.getMode() == EditorMode.ACTIONS) {
                g2.fillOval((int) h_x - 4, (int) h_y - 4, 9, 9);
            }

            g2.setColor(new Color(120, 120, 120, 80));
            g2.setStroke(new BasicStroke(0.5f));
            int pp = 4;
            int y_down = (int) h_y;
            int y_up = (int) h_y;
            while (y_down < h) {
                y_down += pp;
                g2.drawLine(0, y_down, w, y_down);
                g2.drawLine(0, y_down, (int) h_x, (int) h_y);
                g2.drawLine(w, y_down, (int) h_x, (int) h_y);
                pp = (int) (pp * 2);
            }
            pp = 4;
            while (y_up > panel.getMarginY()) {
                y_up -= pp;
                g2.drawLine(0, y_up, w, y_up);
                g2.drawLine(0, y_up, (int) h_x, (int) h_y);
                g2.drawLine(w, y_up, (int) h_x, (int) h_y);
                pp = (int) (pp * 2);
            }
            for (int p = 1; p < w / 100; p++) {
                g2.drawLine(p * 100, 0, p * 100, h);
                g2.drawLine(p * 100, 0, (int) h_x, (int) h_y);
                g2.drawLine(p * 100, h, (int) h_x, (int) h_y);
            }
        }
    }

    public BufferedImage paintImage(int x, int y, int w, int h) {
        // BufferedImage newImage = new BufferedImage(2001, 2001, BufferedImage.TYPE_INT_ARGB);
        BufferedImage newImage = Workspace.createCompatibleImage(2001, 2001);

        Graphics2D g2 = newImage.createGraphics();

        panel.parentPaintComponent(g2);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        /*for (int i = 0; i < panel.getImageCount(); i++) {
        if (panel.getImage(i) == null) {
        initImage(i);
        }
        }*/

        g2.setPaint(Workspace.sketchBackground);
        g2.fillRect(0, 0, Math.max(w, 2000), Math.max(h, 2000));

        g2.setColor(Color.BLACK);
        g2.drawRect(0, 0, w - 1, h - 1);

        if (panel.getMasterPage() != null && panel.getMasterPage() != panel.getSketch()) {
            if (panel.getMasterImage() == null) {
                initMasterImage();
            }
            g2.drawImage(panel.getMasterImage(), 0, 0, null);
            panel.getMasterPage().regions.draw(g2, panel.getComponent(), EditorMode.SKETCHING, false, false, 1.0f);
        }

        g2.drawImage(panel.getImage(0), 0, 0, null);

        if (this.panel.getSketch() != null) {
            this.panel.getSketch().regions.draw(g2, panel.getComponent(), EditorMode.SKETCHING, false, false, 1.0f);
        }

        // if (modeToolbar.showAnnotation.isSelected()) {
        if (SketchletEditor.editorPanel.layer == 1) {
            g2.drawImage(panel.getImage(1), 0, 0, null);
        }

        if (x + w > newImage.getWidth()) {
            w = newImage.getWidth() - x;
        }

        if (y + h > newImage.getHeight()) {
            h = newImage.getHeight() - y;
        }

        g2.dispose();

        return newImage.getSubimage(x, y, w, h);
    }

    public void initMasterImage() {
        if (this.panel.getMasterPage() == null || panel.getMasterPage() == panel.getSketch()) {
            return;
        }

        panel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            int w = panel.getMasterPage().pageWidth;
            int h = panel.getMasterPage().pageHeight;
            if (w <= 0 || h <= 0) {
                w = (int) InteractionSpace.sketchWidth;
                h = (int) InteractionSpace.sketchHeight;
            }
            panel.setMasterImage(Workspace.createCompatibleImage(w, h, panel.getMasterImage()));
            Graphics2D g2 = panel.getMasterImage().createGraphics();
            BufferedImage tempImage = null;
            for (int i = 0; i < Page.NUMBER_OF_LAYERS; i++) {
                File file = panel.getMasterPage().getLayerImageFile(i);
                if (panel.getMasterPage().isLayerActive(i) && file.exists()) {
                    tempImage = ImageCache.read(file, tempImage);
                    g2.drawImage(tempImage, 0, 0, null);
                    tempImage.flush();
                }
            }
            g2.dispose();
        } catch (IOException e) {
            int w = panel.getWidth();
            int h = panel.getHeight();
            panel.setMasterImage(Workspace.createCompatibleImage(w, h, panel.getMasterImage()));
        }

        // System.gc();
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}
