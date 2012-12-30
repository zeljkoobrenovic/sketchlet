package net.sf.sketchlet.framework.renderer.page;

import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;
import net.sf.sketchlet.framework.blackboard.evaluator.Evaluator;
import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.context.SketchletPlaybackContext;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.context.SketchletContextImpl;
import net.sf.sketchlet.designer.context.SketchletGraphicsContextImpl;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.SketchletEditorMode;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.framework.model.imagecache.ImageCache;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.model.Connector;
import net.sf.sketchlet.framework.model.Page;
import net.sf.sketchlet.framework.model.events.variable.VariableUpdateEventMacro;
import net.sf.sketchlet.framework.model.events.keyboard.KeyboardEventMacro;
import net.sf.sketchlet.framework.model.events.mouse.MouseEventMacro;
import net.sf.sketchlet.framework.renderer.DropAreasRenderer;
import net.sf.sketchlet.util.Colors;
import org.apache.commons.lang.StringUtils;
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

    private PanelRenderer panelRenderer;
    private DropAreasRenderer pageDropAreasRenderer;

    public PageRenderer(PanelRenderer panelRenderer) {
        this.setPanelRenderer(panelRenderer);
    }

    public void dispose() {
        this.setPanelRenderer(null);
    }

    public void prepare(boolean bPlayback, boolean processLimits) {
        for (ActiveRegion region : getPanelRenderer().getPage().getRegions().getRegions()) {
            region.getRenderer().prepare(bPlayback, processLimits);
        }
    }

    public void draw(Graphics g, boolean bPlayback, boolean bDrawRect, boolean bHighlightRegions) {
        if (getPanelRenderer() == null || !getPanelRenderer().isActive()) {
            return;
        }

        SketchletPlaybackContext sketchlet = SketchletPlaybackContext.getInstance();
        sketchlet.setCurrentPanel(getPanelRenderer().getComponent());
        sketchlet.setMargin(getPanelRenderer().getMarginX(), getPanelRenderer().getMarginY());
        sketchlet.setScale(getPanelRenderer().getScaleX());

        SketchletEditor sketchletEditor = SketchletEditor.getInstance();
        double s = Math.min(1, sketchletEditor.getScale());
        int imageOffset[] = getPanelRenderer().getPage().getBackgroundOffset(bPlayback);
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform oldTransformation = g2.getTransform();
        Stroke oldStroke = g2.getStroke();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color bgColor = Colors.getColor(getPanelRenderer().getPage().getPropertyValue("background color"));
        if (bgColor == null) {
            bgColor = Workspace.getSketchBackground();
        }
        g2.setPaint(bgColor);
        g2.fillRect(0, 0, 5000, 5000);
        float transparency = 1.0f;
        if (bPlayback) {
            String strTransparency = getPanelRenderer().getPage().getPropertyValue("transparency");
            if (!strTransparency.isEmpty() && !strTransparency.equals("NaN")) {
                try {
                    transparency = (float) Double.parseDouble(strTransparency);
                } catch (Exception e) {
                }
            }
        }

        if (!bPlayback) {
            transparency = (float) (transparency * SketchletEditor.getTransparencyFactor());
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));

        int w = getPanelRenderer().getImage(0) != null ? getPanelRenderer().getImage(0).getWidth() : (int) InteractionSpace.getSketchWidth();
        int h = getPanelRenderer().getImage(0) != null ? getPanelRenderer().getImage(0).getHeight() : (int) InteractionSpace.getSketchHeight();

        if (!bPlayback) {
            g2.scale(getPanelRenderer().getScaleX(), getPanelRenderer().getScaleY());
            g2.translate(getPanelRenderer().getMarginX(), getPanelRenderer().getMarginY());
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
            if (sketchletEditor.getMode() == SketchletEditorMode.SKETCHING) {
                g2.drawString("layer " + (this.getPanelRenderer().getLayer() + 1), 0, -3);
            }
        }

        if (getPanelRenderer().getMasterPage() != null && getPanelRenderer().getMasterPage() != getPanelRenderer().getPage()) {
            if (getPanelRenderer().getMasterImage() == null) {
                initMasterImage();
            }
            g2.drawImage(getPanelRenderer().getMasterImage(), imageOffset[0], imageOffset[1], null);

            if (getPanelRenderer().getPage().isRegionsLayer() && getPanelRenderer().getMasterPage() != null && getPanelRenderer().getMasterPage().getRegions() != null) {
                draw((Graphics2D) g, getPanelRenderer().getComponent(), SketchletEditorMode.SKETCHING, bPlayback, bHighlightRegions, transparency);
            }
        }

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f * transparency));

        for (int l = 0; l < Page.NUMBER_OF_LAYERS; l++) {
            boolean bDraw = this.getPanelRenderer().getPage().isLayerActive(l, bPlayback);
            if (bDraw) {
                float t = 1.0f;
                try {
                    String strTransparency = this.getPanelRenderer().getPage().getPropertyValue("transparency layer " + (l + 1));
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
                    g2.drawImage(getPanelRenderer().getImage(l), imageOffset[0], imageOffset[1], null);
                }
            }

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f * transparency));
            if (l == 0 && !bPlayback) {
                drawGrid(g2, w, h);
            }

            if (getPanelRenderer().getPage() != null && getPanelRenderer().getPage().isRegionsLayer()) {
                for (Connector c : getPanelRenderer().getPage().getConnectors()) {
                    c.getRenderer().draw(g2, bPlayback);
                }

                drawRegions(l, (Graphics2D) g, getPanelRenderer().getComponent(), getPanelRenderer().getMode(), bPlayback, bHighlightRegions, transparency);
            }

        }

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f * transparency));

        if ((VariablesBlackboard.getScriptFiles() != null && VariablesBlackboard.getScriptFiles().size() > 0) || VariablesBlackboard.isImageDrawnByExternalProcess()) {
            g2.setColor(Color.BLACK);
            g2.drawImage(SketchletContextImpl.getImage(), imageOffset[0], imageOffset[1], null);
        }

        getPanelRenderer().extraDraw(g2);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f * transparency));

        if (sketchletEditor.getSketchToolbar().bVisualizeVariables) {
            VariablesRelationsRenderer.drawVariables(getPanelRenderer().getPage().getRegions(), g2, bPlayback);
        }

        if (!bPlayback && (getPanelRenderer().getMode() == SketchletEditorMode.EDITING_REGIONS || getPanelRenderer().getMode() == SketchletEditorMode.SKETCHING)) {
            if (FileDrop.isDragging()) {
                getPageDropAreasRenderer().draw((Graphics2D) g, s);
            }

            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(2));
        }

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f * transparency));

        int y = getPanelRenderer().getMarginY() + 20;
        if (sketchletEditor.getSketchToolbar().visualizationInfoEnabled) {
            y = drawSketchInfo(g2);
        }
        if (sketchletEditor.getSketchToolbar().bVisualizeInfoVariables) {
            drawVariablesInfo(g2, y);
        }

        if (getPanelRenderer().getPage().getRemoteX() >= 0 && getPanelRenderer().getPage().getRemoteY() >= 0) {
            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(1.0f));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            int _s = 11;
            g2.drawOval(getPanelRenderer().getPage().getRemoteX() - _s / 2, getPanelRenderer().getPage().getRemoteY() - _s / 2, _s, _s);
        }

        SketchletGraphicsContextImpl.paint(g2);

        if (oldTransformation != null) {
            g2.setTransform(oldTransformation);
        }
        if (oldStroke != null) {
            g2.setStroke(oldStroke);
        }
    }

    private void draw(Graphics2D g2, Component component, SketchletEditorMode mode, boolean bPlayback, boolean bHighlightRegions, float transparency) {
        int offset[] = panelRenderer.getPage().getRegionsOffset(bPlayback);
        panelRenderer.getPage().getRegions().setOffsetX(offset[0]);
        panelRenderer.getPage().getRegions().setOffsetY(offset[1]);
        for (int i = panelRenderer.getPage().getRegions().getRegions().size() - 1; i >= 0; i--) {
            ActiveRegion reg = panelRenderer.getPage().getRegions().getRegions().elementAt(i);
            if (reg.isActive(bPlayback)) {
                reg.getRenderer().draw(g2, component, mode, bPlayback, bHighlightRegions, transparency);
            }
        }
    }

    private void drawRegions(int layer, Graphics2D g2, Component component, SketchletEditorMode mode, boolean bPlayback, boolean bHighlightRegions, float transparency) {
        int offset[] = panelRenderer.getPage().getRegionsOffset(bPlayback);
        panelRenderer.getPage().getRegions().setOffsetX(offset[0]);
        panelRenderer.getPage().getRegions().setOffsetY(offset[1]);
        for (int i = panelRenderer.getPage().getRegions().getRegions().size() - 1; i >= 0; i--) {
            ActiveRegion reg = panelRenderer.getPage().getRegions().getRegions().elementAt(i);
            if (reg.isActive(bPlayback) && reg.getLayer() == layer) {
                reg.getRenderer().draw(g2, component, mode, bPlayback, bHighlightRegions, transparency);
            }
        }
    }

    public int drawSketchInfo(Graphics2D g2) {
        int x = getPanelRenderer().getMarginX() + 35;
        int y = getPanelRenderer().getMarginY() + 20;
        int yStep = 15;

        drawTexWithBackground(g2, "page " + getPanelRenderer().getPage().getTitle(), x, y, Color.DARK_GRAY);
        y += yStep;

        for (int i = 0; i < getPanelRenderer().getPage().getProperties().length; i++) {
            if (getPanelRenderer().getPage().getProperties()[i][1] != null && !getPanelRenderer().getPage().getProperties()[i][1].toString().isEmpty()) {
                drawTexWithBackground(g2, getPanelRenderer().getPage().getProperties()[i][0].toString() + ": " + getPanelRenderer().getPage().getProperties()[i][1].toString(), x, y, Color.BLACK);
                y += yStep;
            }
        }
        int level = 0;
        int startLevel = 0;
        for (int i = 0; i < getPanelRenderer().getPage().getOnEntryMacro().getActions().length; i++) {
            if (!getPanelRenderer().getPage().getOnEntryMacro().getActions()[i][0].toString().isEmpty()) {
                drawTexWithBackground(g2, "on page entry:", x, y, Color.BLUE);
                y += yStep;

                if (getPanelRenderer().getPage().getOnEntryMacro().getRepeat() == 0) {
                    drawTexWithBackground(g2, "    repeat forever", x, y, Color.BLACK);
                    y += yStep;
                    level++;
                    startLevel++;
                } else if (getPanelRenderer().getPage().getOnEntryMacro().getRepeat() > 1) {
                    drawTexWithBackground(g2, "    repeat " + getPanelRenderer().getPage().getOnEntryMacro().getRepeat() + " times", x, y, Color.BLACK);
                    y += yStep;
                    level++;
                    startLevel++;
                }

                break;
            }
        }
        for (int i = 0; i < getPanelRenderer().getPage().getOnEntryMacro().getActions().length; i++) {
            if (!getPanelRenderer().getPage().getOnEntryMacro().getActions()[i][0].toString().isEmpty()) {
                String strAction = (String) getPanelRenderer().getPage().getOnEntryMacro().getActions()[i][0];
                String strParam1 = (String) getPanelRenderer().getPage().getOnEntryMacro().getActions()[i][1];
                if (!strParam1.isEmpty()) {
                    strParam1 = "'" + strParam1 + "'";
                }
                String strParam2 = (String) getPanelRenderer().getPage().getOnEntryMacro().getActions()[i][2];
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
        for (int i = 0; i < getPanelRenderer().getPage().getOnExitMacro().getActions().length; i++) {
            if (!getPanelRenderer().getPage().getOnExitMacro().getActions()[i][0].toString().isEmpty()) {
                drawTexWithBackground(g2, "on page exit:", x, y, Color.BLUE);
                y += yStep;
                break;
            }
        }
        for (int i = 0; i < getPanelRenderer().getPage().getOnExitMacro().getActions().length; i++) {
            if (!getPanelRenderer().getPage().getOnExitMacro().getActions()[i][0].toString().isEmpty()) {
                String strAction = (String) getPanelRenderer().getPage().getOnExitMacro().getActions()[i][0];
                String strParam1 = "'" + getPanelRenderer().getPage().getOnExitMacro().getActions()[i][1] + "'";
                String strParam2 = (String) getPanelRenderer().getPage().getOnExitMacro().getActions()[i][2];
                if (!strParam2.isEmpty()) {
                    strParam2 = "'" + strParam2 + "'";
                }
                drawTexWithBackground(g2, "     " + strAction.toLowerCase() + " " + strParam1.toLowerCase() + " " + strParam2.toLowerCase(), x, y, Color.BLACK);
                y += yStep;
            }
        }

        if (getPanelRenderer().getPage().getKeyboardEventsProcessor().getKeyboardEventMacros().size() > 0) {
            drawTexWithBackground(g2, "on keyboard events:", x, y, Color.BLUE);
            y += yStep;
            for (KeyboardEventMacro keyboardEventMacro : getPanelRenderer().getPage().getKeyboardEventsProcessor().getKeyboardEventMacros()) {
                String eventInfo = (keyboardEventMacro.getModifiers() + " " + keyboardEventMacro.getKey()).trim() + " " + keyboardEventMacro.getEventName();
                drawTexWithBackground(g2, "    when " + eventInfo + " then", x, y, Color.BLUE);
                y += yStep;
                for (Object action[] : keyboardEventMacro.getMacro().getActions()) {
                    if (StringUtils.isNotBlank(action[0].toString())) {
                        drawTexWithBackground(g2, "        " + action[0] + " " + action[1] + " " + action[2], x, y, Color.BLUE);
                        y += yStep;
                    }
                }
            }
        }

        if (getPanelRenderer().getPage().getMouseEventsProcessor().getMouseEventMacros().size() > 0) {
            drawTexWithBackground(g2, "on mouse events:", x, y, Color.BLUE);
            y += yStep;
            for (MouseEventMacro mouseEventMacro : getPanelRenderer().getPage().getMouseEventsProcessor().getMouseEventMacros()) {
                String eventInfo = mouseEventMacro.getEventName();
                drawTexWithBackground(g2, "    when " + eventInfo + " then", x, y, Color.BLUE);
                y += yStep;
                for (Object action[] : mouseEventMacro.getMacro().getActions()) {
                    if (StringUtils.isNotBlank(action[0].toString())) {
                        drawTexWithBackground(g2, "        " + action[0] + " " + action[1] + " " + action[2], x, y, Color.BLUE);
                        y += yStep;
                    }
                }
            }
        }

        if (getPanelRenderer().getPage().getVariableUpdateEventMacros().size() > 0) {
            drawTexWithBackground(g2, "on variable update events:", x, y, Color.BLUE);
            y += yStep;
            for (VariableUpdateEventMacro variableUpdateEventMacro : getPanelRenderer().getPage().getVariableUpdateEventMacros()) {
                String eventInfo = (variableUpdateEventMacro.getVariable() + " " + variableUpdateEventMacro.getOperator()).trim() + " " + variableUpdateEventMacro.getValue() + " " + variableUpdateEventMacro.getEventName();
                drawTexWithBackground(g2, "    when " + eventInfo + " then", x, y, Color.BLUE);
                y += yStep;
                for (Object action[] : variableUpdateEventMacro.getMacro().getActions()) {
                    if (StringUtils.isNotBlank(action[0].toString())) {
                        drawTexWithBackground(g2, "        " + action[0] + " " + action[1] + " " + action[2], x, y, Color.BLUE);
                        y += yStep;
                    }
                }
            }
        }

        for (int i = 0; i < getPanelRenderer().getPage().getPropertiesAnimation().length; i++) {
            if (getPanelRenderer().getPage().getPropertiesAnimation()[i][1] != null && !getPanelRenderer().getPage().getPropertiesAnimation()[i][1].toString().isEmpty()) {
                drawTexWithBackground(g2, "animation", x, y, Color.BLUE);
                y += yStep;
                break;
            }
        }
        for (int i = 0; i < getPanelRenderer().getPage().getPropertiesAnimation().length; i++) {
            if (getPanelRenderer().getPage().getPropertiesAnimation()[i][1] == null) {
                continue;
            }
            String property = getPanelRenderer().getPage().getPropertiesAnimation()[i][0].toString();
            String type = getPanelRenderer().getPage().getPropertiesAnimation()[i][1].toString();
            String start = getPanelRenderer().getPage().getPropertiesAnimation()[i][2].toString();
            String end = getPanelRenderer().getPage().getPropertiesAnimation()[i][3].toString();
            String duration = getPanelRenderer().getPage().getPropertiesAnimation()[i][4].toString();
            String curve = getPanelRenderer().getPage().getPropertiesAnimation()[i][5].toString();

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
        int x = getPanelRenderer().getMarginX() + 35;
        int yStep = 15;
        y += yStep;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    public void drawTexWithBackground(Graphics2D g2, String strText, int x, int y, Color color) {
        Font font = g2.getFont();
        FontRenderContext frc = g2.getFontRenderContext();
        LineMetrics metrics = font.getLineMetrics(strText, frc);
        int width = (int) font.getStringBounds(strText, frc).getWidth();
        int height = (int) metrics.getHeight();
        g2.setColor(new Color(255, 255, 255, 185));
        g2.fillRect(x, (int) (y - metrics.getAscent()), width, height);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2.setColor(color);
        g2.drawString(strText, x, y);
    }

    public void drawGrid(Graphics2D g2, int w, int h) {
        if (SketchletEditor.getInstance().isSnapToGrid()) {
            g2.setColor(Color.DARK_GRAY);
            float dash1[] = {4.0f};
            float thick = 0.5f;
            BasicStroke dashed = new BasicStroke(thick, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);

            g2.setStroke(dashed);
            for (int i = 1; i <= w / InteractionSpace.getGridSpacing(); i++) {
                int x = i * InteractionSpace.getGridSpacing();
                g2.drawLine(x, 0, x, h);
            }
            for (int i = 1; i <= w / InteractionSpace.getGridSpacing(); i++) {
                int y = i * InteractionSpace.getGridSpacing();
                g2.drawLine(0, y, w, y);
            }
        }
        getPanelRenderer().getPage().calculateHorizonPoint();
        drawPerspectiveLines(g2, getPanelRenderer().getPage().getPerspective_horizont_x1(), getPanelRenderer().getPage().getPerspective_horizont_y(), w, h);
        if (getPanelRenderer().getPage().getPropertyValue("perspective type").equalsIgnoreCase("2 points")) {
            drawPerspectiveLines(g2, getPanelRenderer().getPage().getPerspective_horizont_x2(), getPanelRenderer().getPage().getPerspective_horizont_y(), w, h);
        }
    }

    public void drawPerspectiveLines(Graphics2D g2, double h_x, double h_y, int w, int h) {
        if (SketchletEditor.getInstance().isShowPerspectiveLines() || SketchletEditor.getInstance().getPerspectivePanel().showPerspectiveGrid.isSelected()) {
            g2.setColor(new Color(100, 100, 100, 200));
            g2.setStroke(new BasicStroke(1.0f));

            g2.drawLine(0, (int) h_y, w, (int) h_y);
            g2.drawLine((int) h_x, 0, (int) h_x, h);

            if (getPanelRenderer().getMode() == SketchletEditorMode.EDITING_REGIONS) {
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
                pp = (pp * 2);
            }
            pp = 4;
            while (y_up > getPanelRenderer().getMarginY()) {
                y_up -= pp;
                g2.drawLine(0, y_up, w, y_up);
                g2.drawLine(0, y_up, (int) h_x, (int) h_y);
                g2.drawLine(w, y_up, (int) h_x, (int) h_y);
                pp = (pp * 2);
            }
            for (int p = 1; p < w / 100; p++) {
                g2.drawLine(p * 100, 0, p * 100, h);
                g2.drawLine(p * 100, 0, (int) h_x, (int) h_y);
                g2.drawLine(p * 100, h, (int) h_x, (int) h_y);
            }
        }
    }

    public BufferedImage paintImage(int x, int y, int w, int h) {
        BufferedImage newImage = Workspace.createCompatibleImage(2001, 2001);

        Graphics2D g2 = newImage.createGraphics();

        getPanelRenderer().parentPaintComponent(g2);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setPaint(Workspace.getSketchBackground());
        g2.fillRect(0, 0, Math.max(w, 2000), Math.max(h, 2000));

        g2.setColor(Color.BLACK);
        g2.drawRect(0, 0, w - 1, h - 1);

        if (getPanelRenderer().getMasterPage() != null && getPanelRenderer().getMasterPage() != getPanelRenderer().getPage()) {
            if (getPanelRenderer().getMasterImage() == null) {
                initMasterImage();
            }
            g2.drawImage(getPanelRenderer().getMasterImage(), 0, 0, null);
            draw(g2, getPanelRenderer().getComponent(), SketchletEditorMode.SKETCHING, false, false, 1.0f);
        }

        g2.drawImage(getPanelRenderer().getImage(0), 0, 0, null);

        if (this.getPanelRenderer().getPage() != null) {
            draw(g2, getPanelRenderer().getComponent(), SketchletEditorMode.SKETCHING, false, false, 1.0f);
        }

        if (SketchletEditor.getInstance().getLayer() == 1) {
            g2.drawImage(getPanelRenderer().getImage(1), 0, 0, null);
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
        if (this.getPanelRenderer().getMasterPage() == null || getPanelRenderer().getMasterPage() == getPanelRenderer().getPage()) {
            return;
        }

        getPanelRenderer().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            int w = getPanelRenderer().getMasterPage().getPageWidth();
            int h = getPanelRenderer().getMasterPage().getPageHeight();
            if (w <= 0 || h <= 0) {
                w = (int) InteractionSpace.getSketchWidth();
                h = (int) InteractionSpace.getSketchHeight();
            }
            getPanelRenderer().setMasterImage(Workspace.createCompatibleImage(w, h, getPanelRenderer().getMasterImage()));
            Graphics2D g2 = getPanelRenderer().getMasterImage().createGraphics();
            BufferedImage tempImage = null;
            for (int i = 0; i < Page.NUMBER_OF_LAYERS; i++) {
                File file = getPanelRenderer().getMasterPage().getLayerImageFile(i);
                if (getPanelRenderer().getMasterPage().isLayerActive(i) && file.exists()) {
                    tempImage = ImageCache.read(file, tempImage);
                    g2.drawImage(tempImage, 0, 0, null);
                    tempImage.flush();
                }
            }
            g2.dispose();
        } catch (IOException e) {
            int w = getPanelRenderer().getWidth();
            int h = getPanelRenderer().getHeight();
            getPanelRenderer().setMasterImage(Workspace.createCompatibleImage(w, h, getPanelRenderer().getMasterImage()));
        }

        getPanelRenderer().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public PanelRenderer getPanelRenderer() {
        return panelRenderer;
    }

    public void setPanelRenderer(PanelRenderer panelRenderer) {
        this.panelRenderer = panelRenderer;
    }

    private DropAreasRenderer getPageDropAreasRenderer() {
        if (pageDropAreasRenderer == null) {
            pageDropAreasRenderer = new DropAreasRenderer(SketchletEditor.getInstance().getDragAndDropController().getPageDropAreas());
        }

        return pageDropAreasRenderer;
    }
}
