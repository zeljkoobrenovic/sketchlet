package net.sf.sketchlet.framework.renderer.regions;

import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.SketchletEditorMode;
import net.sf.sketchlet.framework.blackboard.evaluator.Evaluator;
import net.sf.sketchlet.framework.controller.ActiveRegionMouseController;
import net.sf.sketchlet.framework.controller.InteractionContext;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.renderer.DropAreasRenderer;
import net.sf.sketchlet.framework.renderer.page.VariablesRelationsRenderer;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class ActiveRegionRenderer {
    private static final Logger log = Logger.getLogger(ActiveRegionRenderer.class);

    public static final int CORNER_SIZE = 8;

    private ShapeLayer shapeImageLayer;
    private WidgetLayer widgetImageLayer;
    private TextDrawingLayer textImageLayer;
    private DrawnImageLayer drawnImageLayer;
    private TrajectoryDrawingLayer trajectoryDrawingLayer;
    private ImageDrawingLayer imageDrawingLayer;
    private AuxiliaryDrawingLayer auxiliaryDrawingLayer;
    private Perspective perspective;
    private DropAreasRenderer dropAreasRenderer;
    private final ActiveRegionRendererDataPreparation activeRegionRendererDataPreparation;

    private ActiveRegion region;
    private BufferedImage buffer;
    private String strPropertiesCache = "";
    private int oldW = -1;
    private int oldH = -1;
    private double old_p_x0 = -1;
    private double old_p_y0 = -1;
    private double old_p_x1 = -1;
    private double old_p_y1 = -1;
    private double old_p_x2 = -1;
    private double old_p_y2 = -1;
    private double old_p_x3 = -1;
    private double old_p_y3 = -1;

    public ActiveRegionRenderer(ActiveRegion region) {
        this.region = region;
        setPerspective(new Perspective(region));
        setShapeImageLayer(new ShapeLayer(region));
        setWidgetImageLayer(new WidgetLayer(region));
        setTextImageLayer(new TextDrawingLayer(region));
        setDrawnImageLayer(new DrawnImageLayer(region));
        setTrajectoryDrawingLayer(new TrajectoryDrawingLayer(region));
        setImageDrawingLayer(new ImageDrawingLayer(region));
        setAuxiliaryDrawingLayer(new AuxiliaryDrawingLayer(region));
        activeRegionRendererDataPreparation = new ActiveRegionRendererDataPreparation(this);
    }

    public void activate(boolean inPlaybackMode) {
    }

    public void deactivate(boolean bPlayback) {
        if (getWidgetImageLayer().getWidgetPlugin() != null) {
            getWidgetImageLayer().getWidgetPlugin().deactivate(bPlayback);
        }
    }

    public void flush() {
        if (getTextImageLayer() != null) {
            getTextImageLayer().flush();
        }
    }

    public void dispose() {
        region = null;

        strPropertiesCache = "";

        if (getPerspective() != null) {
            getPerspective().dispose();
        }
        if (getShapeImageLayer() != null) {
            getShapeImageLayer().dispose();
        }
        if (getWidgetImageLayer() != null) {
            getWidgetImageLayer().dispose();
        }
        if (getTextImageLayer() != null) {
            getTextImageLayer().dispose();
        }
        if (getDrawnImageLayer() != null) {
            getDrawnImageLayer().dispose();
        }
        if (getTrajectoryDrawingLayer() != null) {
            getTrajectoryDrawingLayer().dispose();
        }
        if (getImageDrawingLayer() != null) {
            getImageDrawingLayer().dispose();
        }
        if (getAuxiliaryDrawingLayer() != null) {
            getAuxiliaryDrawingLayer().dispose();
        }

        if (dropAreasRenderer != null) {
            dropAreasRenderer.dispose();
            dropAreasRenderer = null;
        }

        setPerspective(null);
        setShapeImageLayer(null);
        setWidgetImageLayer(null);
        setTextImageLayer(null);
        setDrawnImageLayer(null);
        setTrajectoryDrawingLayer(null);
        setImageDrawingLayer(null);
        setAuxiliaryDrawingLayer(null);
    }

    public float getLayerTransparency() {
        float t = 1.0f;
        try {
            if (region.getParent() != null) {
                String strTransparency = region.getSketch().getPropertyValue("transparency layer " + (region.getLayer() + 1));
                if (strTransparency != null && !strTransparency.isEmpty()) {
                    strTransparency = Evaluator.processText(strTransparency, "", "");
                    t = (float) Double.parseDouble(strTransparency);
                }
            }
        } catch (Exception e) {
            // log.error(e);
        }
        return t;
    }

    public void draw(Graphics2D g) {
        draw(g, null, SketchletEditorMode.EDITING_REGIONS, false, false, 1.0f);
    }

    public synchronized void draw(Graphics2D g2, Component component, SketchletEditorMode mode, boolean inPlaybackMode, boolean bHighlightRegions, float transparency) {
        try {
            double scale = SketchletEditor.getInstance() != null ? SketchletEditor.getInstance().getScale() : 1.0;
            float fontSize = (float) (11 / scale);
            if (fontSize < 11) {
                fontSize = 11;
            }

            Font font = g2.getFont().deriveFont(fontSize);
            g2.setFont(font);
            AffineTransform oldTransform = g2.getTransform();
            activeRegionRendererDataPreparation.prepare(inPlaybackMode, true);

            transparency *= getLayerTransparency();

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));

            if (inPlaybackMode) {
                if (region.getParent() != null) {
                    g2.translate(region.getParent().getOffsetX(), region.getParent().getOffsetY());
                }
            }
            g2.shear(region.getShearXValue(), region.getShearYValue());
            if (region.getRotationValue() != 0.0) {
                g2.rotate(region.getRotationValue(),
                        region.getX1Value() + (region.getX2Value() - region.getX1Value()) * region.getCenterOfRotationX(),
                        region.getY1Value() + (region.getY2Value() - region.getY1Value()) * region.getCenterOfRotationY());
            }

            if (inPlaybackMode) {
                if (region.getParent() != null) {
                    g2.translate(-region.getParent().getOffsetX(), -region.getParent().getOffsetY());
                }
            }

            drawActive(g2, component, inPlaybackMode, transparency);

            if (SketchletEditor.getInstance() != null && SketchletEditor.getInstance().getSketchToolbar() != null) {
                if (SketchletEditor.getInstance().getSketchToolbar().bVisualizeInfoRegions) {
                    this.drawInfo(g2, inPlaybackMode, transparency);
                }
            }
            if (inPlaybackMode && region == InteractionContext.getSelectedRegion()) {
                Stroke stroke = g2.getStroke();
                g2.setStroke(new BasicStroke(1));
                g2.setColor(Color.ORANGE);
                g2.drawRect(region.getX1Value(), region.getY1Value(), region.getWidthValue(), region.getHeightValue());
                g2.setStroke(stroke);
            }

            if (region == null) {
                return;
            }

            if (!inPlaybackMode) {
                int w = Math.abs(region.getX2Value() - region.getX1Value());
                int h = Math.abs(region.getY2Value() - region.getY1Value());
                int _x1 = Math.min(region.getX1Value(), region.getX2Value());
                int _x2 = Math.max(region.getX1Value(), region.getX2Value());
                int _y1 = Math.min(region.getY1Value(), region.getY2Value());
                int _y2 = Math.max(region.getY1Value(), region.getY2Value());

                g2.setStroke(new BasicStroke(1));

                boolean selected = false;

                if (SketchletEditor.getInstance() == null) {
                    return;
                }
                boolean dragging = region.getMouseController().inRect((int) (FileDrop.getMouseX() / SketchletEditor.getInstance().getScale()), (int) (FileDrop.getMouseY() / SketchletEditor.getInstance().getScale()), false);

                if (FileDrop.isDragging() || dragging) {
                    DropAreasRenderer dropAreasRenderer = getDropAreasRenderer();
                    if (dropAreasRenderer != null) {
                        dropAreasRenderer.getDropAreas().setOffset(_x1 + w - dropAreasRenderer.getDropAreas().getWidth(), _y1);
                        dropAreasRenderer.draw(g2, scale);
                    }
                }

                if (mode == SketchletEditorMode.EDITING_REGIONS || dragging) {
                    selected = region.getParent().getMouseHelper().getSelectedRegions() != null && region.getParent().getMouseHelper().getSelectedRegions().contains(region);
                    if (dragging) {
                        g2.setColor(new Color(120, 120, 120, 100));
                        g2.setStroke(new BasicStroke(1));
                    } else if (selected) {
                        g2.setColor(new Color(120, 120, 120, 70));
                        g2.setStroke(new BasicStroke(1));
                    } else {
                        g2.setColor(new Color(210, 210, 210, 50));
                    }
                } else {
                    g2.setColor(new Color(120, 120, 120, 20));
                }
                if (selected || (!inPlaybackMode && region.isInFocus())) {
                    g2.fillRect(_x1, _y1, w, h);
                }
                if (dragging) {
                    g2.setStroke(new BasicStroke(1));
                    g2.setColor(new Color(255, 255, 255));
                }
                if (mode == SketchletEditorMode.EDITING_REGIONS) {
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.drawRect(_x1, _y1, w, h);
                    Stroke stroke = g2.getStroke();
                    g2.setStroke(new BasicStroke(1));

                    int _cornerSize = (int) (CORNER_SIZE / SketchletEditor.getInstance().getScale());

                    if (selected) {
                        if (SketchletEditor.getInstance().isDragging() && !this.region.getMouseController().isRotating()) {
                            g2.setColor(new Color(255, 255, 255, 100));
                            g2.fillRect(region.getX1Value(), region.getY1Value() - 12, region.getWidthValue(), 12);
                            g2.setColor(Color.BLACK);
                            g2.drawString(Language.translate("Left") + ": " + region.getX1Value() + " " + Language.translate("Top") + ": " + region.getY1Value() + " " + Language.translate("Width") + ": " + region.getWidthValue() + " " + Language.translate("Height") + ": " + region.getHeightValue(), _x1, _y1 - 3);
                        }

                        g2.drawLine(_x1 + (_x2 - _x1) / 2, _y1, _x1 + (_x2 - _x1) / 2, _y1 - 35);
                        g2.setStroke(stroke);
                        g2.setColor(Color.WHITE);
                        g2.fillOval(_x1 - _cornerSize / 2, _y1 - _cornerSize / 2, _cornerSize, _cornerSize);
                        g2.fillOval(_x1 - _cornerSize / 2, _y2 - _cornerSize / 2, _cornerSize, _cornerSize);
                        g2.fillOval(_x2 - _cornerSize / 2, _y1 - _cornerSize / 2, _cornerSize, _cornerSize);
                        g2.fillOval(_x2 - _cornerSize / 2, _y2 - _cornerSize / 2, _cornerSize, _cornerSize);
                        g2.fillOval(_x1 + (_x2 - _x1) / 2 - _cornerSize / 2, _y1 - 35 - _cornerSize / 2, _cornerSize, _cornerSize);

                        g2.setStroke(new BasicStroke(1));

                        drawTrajectoryElements(g2, _x1, _y1, w, h, _cornerSize);

                        if (this.region.getMouseController().isRotating() && SketchletEditor.getInstance().isDragging()) {
                            drawRotatingElements(g2, _x1, _y1, w, h);
                        }

                        drawSelectionElement(g2, _x1, _y1, _x2, _y2, w, h, _cornerSize);
                    }
                    g2.setColor(Color.BLACK);
                    g2.drawString(region.getNumber(), _x1 + w / 2 - 2, _y1 + h / 2 + 5);
                    if (!region.getName().equalsIgnoreCase(region.getName())) {
                        g2.drawString(region.getName(), _x1 + w / 2 - 2, _y1 + h / 2 + 17);
                    }
                }
            }

            g2.setTransform(oldTransform);

            if (!inPlaybackMode) {
                this.getTrajectoryDrawingLayer().draw(g2, component, inPlaybackMode);
                getAuxiliaryDrawingLayer().drawLimits(g2, component, mode);
            } else if (bHighlightRegions) {
                getAuxiliaryDrawingLayer().drawHighlight(g2);
            }
        } catch (Throwable e) {
            log.error(e);
            e.printStackTrace();
        }
    }

    private void drawRotatingElements(Graphics2D g2, int _x1, int _y1, int w, int h) {
        double circleSize = 60 / SketchletEditor.getInstance().getScale();
        g2.drawOval((int) (_x1 - circleSize / 2 - 1 + w * region.getCenterOfRotationX()), (int) (_y1 - circleSize / 2 - 1 + h * region.getCenterOfRotationY()), (int) (circleSize + 2), (int) (circleSize + 2));
        g2.setColor(Color.GRAY);
        for (int r = 0; r < 360; r += 15) {
            double angle = Math.toRadians(r) - region.getRotationValue();
            double r_x1 = _x1 + w * region.getCenterOfRotationX() + circleSize / 2 * Math.cos(angle);
            double r_y1 = _y1 + h * region.getCenterOfRotationY() + circleSize / 2 * Math.sin(angle);
            double r_x2 = _x1 + w * region.getCenterOfRotationX() + (circleSize / 2 - 5) * Math.cos(angle);
            double r_y2 = _y1 + h * region.getCenterOfRotationY() + (circleSize / 2 - 5) * Math.sin(angle);

            g2.drawLine((int) r_x1, (int) r_y1, (int) r_x2, (int) r_y2);
        }
        g2.setColor(Color.RED);
        double angle = -Math.PI / 2;
        double r_x1 = _x1 + w * region.getCenterOfRotationX() + circleSize / 2 * Math.cos(angle);
        double r_y1 = _y1 + h * region.getCenterOfRotationY() + circleSize / 2 * Math.sin(angle);
        double r_x2 = _x1 + w * region.getCenterOfRotationX() + (circleSize / 2 - 10) * Math.cos(angle);
        double r_y2 = _y1 + h * region.getCenterOfRotationY() + (circleSize / 2 - 10) * Math.sin(angle);
        g2.drawLine((int) r_x1, (int) r_y1, (int) r_x2, (int) r_y2);

        g2.setColor(Color.GRAY);
        g2.drawString("  " + ((int) Math.toDegrees(region.getRotationValue())), (int) r_x1 - 10, (int) r_y1 - 5);
    }

    private void drawSelectionElement(Graphics2D g2, int _x1, int _y1, int _x2, int _y2, int w, int h, int _cornerSize) {
        g2.setColor(Color.YELLOW);
        g2.fillOval(_x1 - _cornerSize / 2 + (int) (w * region.getP_x0()), _y1 - _cornerSize / 2 + (int) (h * region.getP_y0()), _cornerSize, _cornerSize);
        g2.fillOval(_x1 - _cornerSize / 2 + (int) (w * region.getP_x1()), _y1 - _cornerSize / 2 + (int) (h * region.getP_y1()), _cornerSize, _cornerSize);
        g2.fillOval(_x1 - _cornerSize / 2 + (int) (w * region.getP_x2()), _y1 - _cornerSize / 2 + (int) (h * region.getP_y2()), _cornerSize, _cornerSize);
        g2.fillOval(_x1 - _cornerSize / 2 + (int) (w * region.getP_x3()), _y1 - _cornerSize / 2 + (int) (h * region.getP_y3()), _cornerSize, _cornerSize);
        g2.drawLine(_x1 + (int) (w * region.getP_x0()), _y1 + (int) (h * region.getP_y0()), _x1 + (int) (w * region.getP_x1()), _y1 + (int) (h * region.getP_y1()));
        g2.drawLine(_x1 + (int) (w * region.getP_x1()), _y1 + (int) (h * region.getP_y1()), _x1 + (int) (w * region.getP_x2()), _y1 + (int) (h * region.getP_y2()));
        g2.drawLine(_x1 + (int) (w * region.getP_x2()), _y1 + (int) (h * region.getP_y2()), _x1 + (int) (w * region.getP_x3()), _y1 + (int) (h * region.getP_y3()));
        g2.drawLine(_x1 + (int) (w * region.getP_x3()), _y1 + (int) (h * region.getP_y3()), _x1 + (int) (w * region.getP_x0()), _y1 + (int) (h * region.getP_y0()));
        g2.setColor(new Color(170, 170, 170, 200));
        g2.drawRect(_x1, _y1, w, h);
        g2.setColor(new Color(170, 170, 170, 200));
        g2.fillOval(_x1 - _cornerSize / 2, _y1 - _cornerSize / 2, _cornerSize, _cornerSize);
        g2.fillOval(_x1 - _cornerSize / 2, _y2 - _cornerSize / 2, _cornerSize, _cornerSize);
        g2.fillOval(_x2 - _cornerSize / 2, _y1 - _cornerSize / 2, _cornerSize, _cornerSize);
        g2.fillOval(_x2 - _cornerSize / 2, _y2 - _cornerSize / 2, _cornerSize, _cornerSize);
        g2.fillOval(_x1 + (_x2 - _x1) / 2 - _cornerSize / 2, _y1 - 35 - _cornerSize / 2, _cornerSize, _cornerSize);
        g2.setStroke(new BasicStroke(1));
        g2.setColor(new Color(170, 170, 170, 200));
        g2.drawOval(_x1 - _cornerSize / 2, _y1 - _cornerSize / 2, _cornerSize, _cornerSize);
        g2.drawOval(_x1 - _cornerSize / 2, _y2 - _cornerSize / 2, _cornerSize, _cornerSize);
        g2.drawOval(_x2 - _cornerSize / 2, _y1 - _cornerSize / 2, _cornerSize, _cornerSize);
        g2.drawOval(_x2 - _cornerSize / 2, _y2 - _cornerSize / 2, _cornerSize, _cornerSize);
        g2.drawOval(_x1 + (_x2 - _x1) / 2 - _cornerSize / 2, _y1 - 35 - _cornerSize / 2, _cornerSize, _cornerSize);
        g2.setStroke(new BasicStroke(1));
        g2.setColor(new Color(170, 170, 170, 200));
        g2.fillRect(_x1 + w / 2 - _cornerSize / 2, _y1 - _cornerSize / 2, _cornerSize, _cornerSize);
        g2.fillRect(_x1 + w / 2 - _cornerSize / 2, _y2 - _cornerSize / 2, _cornerSize, _cornerSize);
        g2.fillRect(_x1 - _cornerSize / 2, _y1 + h / 2 - _cornerSize / 2, _cornerSize, _cornerSize);
        g2.fillRect(_x2 - _cornerSize / 2, _y1 + h / 2 - _cornerSize / 2, _cornerSize, _cornerSize);
    }

    private void drawTrajectoryElements(Graphics2D g2, int _x1, int _y1, int w, int h, int _cornerSize) {
        if (!region.getTrajectory1().isEmpty()) {
            g2.setColor(Color.WHITE);
            g2.fillOval(_x1 - _cornerSize / 2 - 1 + (int) (w * region.getTrajectory2X()), _y1 - _cornerSize / 2 - 1 + (int) (h * region.getTrajectory2Y()), _cornerSize + 2, _cornerSize + 2);
            g2.setColor(new Color(0, 0, 255, 100));
            g2.fillOval(_x1 - _cornerSize / 2 + (int) (w * region.getTrajectory2X()), _y1 - _cornerSize / 2 + (int) (h * region.getTrajectory2Y()), _cornerSize, _cornerSize);
        }

        if (!SketchletEditor.getInstance().isInShiftMode()) {
            g2.setColor(Color.WHITE);
            g2.fillOval(_x1 - _cornerSize / 2 + (int) (w * region.getCenterOfRotationX()), _y1 - _cornerSize / 2 + (int) (h * region.getCenterOfRotationY()), _cornerSize, _cornerSize);
            g2.setColor(Color.GRAY);
            g2.drawOval(_x1 - _cornerSize / 2 - 1 + (int) (w * region.getCenterOfRotationX()), _y1 - _cornerSize / 2 - 1 + (int) (h * region.getCenterOfRotationY()), _cornerSize + 2, _cornerSize + 2);
        }
        if (!SketchletEditor.getInstance().isInShiftMode() && region.getMouseController().getSelectedCorner() == ActiveRegionMouseController.CENTER_ROTATION) {
            int __x = _x1 - _cornerSize / 2 - 1 + (int) (w * region.getMouseController().getCenterRotationX());
            int __y = _y1 - _cornerSize / 2 - 1 + (int) (h * region.getMouseController().getCenterRotationY());
            g2.drawOval(__x, __y, _cornerSize + 2, _cornerSize + 2);
            g2.drawString("rotation center", __x, __y + 20);
        } else if (SketchletEditor.getInstance().isInCtrlMode() && SketchletEditor.getInstance().isInShiftMode()) {
            if (region.getMouseController().getSelectedCorner() == ActiveRegionMouseController.TRAJECTORY2_POINT) {
                int __x = _x1 - _cornerSize / 2 - 1 + (int) (w * region.getMouseController().getCenterRotationX());
                int __y = _y1 - _cornerSize / 2 - 1 + (int) (h * region.getMouseController().getCenterRotationY());
                g2.drawOval(__x, __y, _cornerSize + 2, _cornerSize + 2);
                __x = _x1 - _cornerSize / 2 - 1 + (int) (w * region.getMouseController().getTrajectory2X());
                __y = _y1 - _cornerSize / 2 - 1 + (int) (h * region.getMouseController().getTrajectory2Y());
                g2.setColor(Color.WHITE);
                g2.drawOval(__x, __y, _cornerSize + 2, _cornerSize + 2);
                g2.setColor(Color.GRAY);
                g2.drawString("trajectory point 2", __x, __y + 20);
                g2.setColor(Color.WHITE);
                int __x1 = _x1 + (int) (w * region.getCenterOfRotationX());
                int __y1 = _y1 + (int) (h * region.getCenterOfRotationY());
                int __x2 = _x1 + (int) (w * region.getMouseController().getTrajectory2X());
                int __y2 = _y1 + (int) (h * region.getMouseController().getTrajectory2Y());
                g2.drawLine(__x1, __y1, __x2, __y2);
            }
        }
    }

    public synchronized void prepare(boolean inPlaybackMode, boolean processLimitsEnabled) {
        activeRegionRendererDataPreparation.prepare(inPlaybackMode, processLimitsEnabled);
    }

    public boolean shouldRedraw(boolean bPlayback) {
        if (buffer == null) {
            return true;
        }

        for (int i = 0; i < region.getImageCount(); i++) {
            if (region.isDrawnImageChanged(i)) {
                return true;
            }
        }

        if (region.isScreenCapturingEnabled()) {
            return true;
        }

        if (bPlayback && !region.getPenWidth().isEmpty()) {
            return true;
        }

        if (!region.getWidget().isEmpty()) {
            return true;
        }

        if (oldW != region.getWidthValue() || oldH != region.getHeightValue()) {
            oldW = region.getWidthValue();
            oldH = region.getHeightValue();
            return true;
        }

        if (old_p_x0 != region.getP_x0() || old_p_y0 != region.getP_y0()
                || old_p_x1 != region.getP_x1() || old_p_y1 != region.getP_y1()
                || old_p_x2 != region.getP_x2() || old_p_y2 != region.getP_y2()
                || old_p_x3 != region.getP_x3() || old_p_y3 != region.getP_y3()) {
            old_p_x0 = region.getP_x0();
            old_p_y0 = region.getP_y0();
            old_p_x1 = region.getP_x1();
            old_p_y1 = region.getP_y1();
            old_p_x2 = region.getP_x2();
            old_p_y2 = region.getP_y2();
            old_p_x3 = region.getP_x3();
            old_p_y3 = region.getP_y3();
            return true;
        }

        if (this.getTextImageLayer().isFontChanged()) {
            return true;
        }

        String strImageProperties[] = {
                region.getTransparency(),
                region.getImageIndex(),
                region.getImageUrlField(),
                region.getText(),
                region.getTextField(),
                region.getShape(),
                region.getLineColor(),
                region.getFillColor(),
                region.getLineThickness(),
                region.getLineStyle(),
                region.getEmbeddedSketch(),
                region.getHorizontalAlignment(),
                region.getVerticalAlignment(),
                region.getPerspectiveDepth(),
                region.getRotation3DHorizontal(),
                region.getRotation3DVertical(),
                region.getShapeArguments(),
                region.getShearX(),
                region.getShearY(),
                region.getWindowX(),
                region.getWindowY(),
                region.getWindowWidth(),
                region.getWindowHeight(),
                region.getZoom()
        };
        String strCache = "" + bPlayback + ";";
        for (int i = 0; i < strImageProperties.length; i++) {
            strCache += region.processText(strImageProperties[i]) + ";";
        }

        if (strCache.equals(strPropertiesCache)) {
            return false;
        }
        strPropertiesCache = strCache;
        return true;
    }

    public void drawActive(Component component, int indexes[], Graphics2D g, boolean bPlayback, float transparency) {
        try {
            ActiveRegion region = this.region;
            if (region == null) {
                return;
            }

            if (bPlayback) {
                region.animate();
            }

            if (region.getX2Value() == region.getX1Value() || region.getY2Value() == region.getY1Value()) {
                return;
            }

            if (shouldRedraw(bPlayback)) {
                int w = Math.abs(region.getX2Value() - region.getX1Value());
                int h = Math.abs(region.getY2Value() - region.getY1Value());
                if (Math.abs(region.getX2Value() - region.getX1Value()) > 10000 || Math.abs(region.getY2Value() - region.getY1Value()) > 10000) {
                    region.setX1Value(100);
                    region.setY1Value(100);
                    region.setX2Value(200);
                    region.setY2Value(200);

                    buffer = Workspace.createCompatibleImage(100, 100);
                } else {
                    buffer = Workspace.createCompatibleImage(w, h, buffer);
                }

                Graphics2D g2 = buffer.createGraphics();

                g2.translate(-region.getX1Value(), -region.getY1Value());

                Area clipShape = region.getArea(bPlayback);
                g2.setColor(new Color(0, 0, 0));

                g2.setClip(clipShape);

                AffineTransform oldTransform = g2.getTransform();
                Composite oldComposite = g2.getComposite();

                if (bPlayback) {
                    getAuxiliaryDrawingLayer().drawPen();
                }

                String strTransparency = region.getTransparency();

                if (!strTransparency.isEmpty()) {
                    strTransparency = region.processText(strTransparency);
                    try {
                        float alpha = (float) Double.parseDouble(strTransparency);
                        g2.setComposite(makeComposite(alpha * transparency));
                    } catch (Throwable e) {
                    }
                }

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                ShapeLayer shapeImageLayer = getShapeImageLayer();
                shapeImageLayer.draw(g2, component, bPlayback, true, false);
                DrawnImageLayer drawnImageLayer = getDrawnImageLayer();
                drawnImageLayer.init();
                WidgetLayer widgetImageLayer = getWidgetImageLayer();
                if (widgetImageLayer != null) {
                    widgetImageLayer.draw(g2, component, bPlayback);
                    if (widgetImageLayer != null && widgetImageLayer.getWidgetPlugin() == null && getImageDrawingLayer() != null && drawnImageLayer != null) {
                        getImageDrawingLayer().draw(g2, component, bPlayback);
                        for (int di = 0; di < indexes.length; di++) {
                            drawnImageLayer.setIndex(indexes[di]);
                            drawnImageLayer.draw(g2, component, bPlayback);
                        }
                        getTextImageLayer().draw(g2, component, bPlayback);
                    }
                }
                shapeImageLayer.draw(g2, component, bPlayback, false, true);
                g2.setComposite(oldComposite);
                g2.setTransform(oldTransform);
                g2.dispose();
            }

            getPerspective().doPerspectiveAndDrawImage(g, buffer);

        } catch (Throwable e) {
            log.error(e);
            e.printStackTrace();
        }
    }

    public void drawImageWin(Graphics2D g2, BufferedImage image, int dx, int dy, int dw, int dh) {
        String strZoom = region.processText((region.getZoom()).trim()).trim();
        String strWindowX = region.processText((region.getWindowX()).trim()).trim();
        String strWindowY = region.processText((region.getWindowY()).trim()).trim();
        String strWindowWidth = region.processText((region.getWindowWidth()).trim()).trim();
        String strWindowHeight = region.processText((region.getWindowHeight()).trim()).trim();

        int sx = 0;
        int sy = 0;
        int sw = image.getWidth();
        int sh = image.getHeight();
        boolean changed = false;

        if (!strZoom.isEmpty()) {
            try {
                double dz = Double.parseDouble(strZoom);
                if (dz > 0.0) {
                    double __w = sw / dz;
                    double __h = sh / dz;

                    sx = (int) ((sw - __w) * region.getCenterOfRotationX());
                    sy = (int) ((sh - __h) * region.getCenterOfRotationY());

                    sw = (int) (__w);
                    sh = (int) (__h);
                    changed = true;
                }
            } catch (Exception e) {
            }
        } else {
            if (!strWindowX.isEmpty()) {
                try {
                    sx = (int) Double.parseDouble(strWindowX);
                    changed = true;
                } catch (Throwable e) {
                }
            }
            if (!strWindowY.isEmpty()) {
                try {
                    sy = (int) Double.parseDouble(strWindowY);
                    changed = true;
                } catch (Throwable e) {
                }
            }
            if (!strWindowWidth.isEmpty()) {
                try {
                    sw = (int) Double.parseDouble(strWindowWidth);
                    changed = true;
                } catch (Throwable e) {
                }
            }
            if (!strWindowHeight.isEmpty()) {
                try {
                    sh = (int) Double.parseDouble(strWindowHeight);
                    changed = true;
                } catch (Throwable e) {
                }
            }
        }

        if (changed) {
            g2.drawImage(image, dx, dy, dx + dw, dy + dh, sx, sy, sx + sw, sy + sh, null);
        } else {
            g2.drawImage(image, dx, dy, dw, dh, null);
        }

    }

    public void drawActive(Graphics2D g2, Component component, boolean bPlayback, float transparency) {
        String strIndex = region.processText(region.getImageIndex());

        if (strIndex == null) {
            strIndex = "";
        } else {
            strIndex = strIndex.trim();
        }

        int indexes[] = {0};

        if (!strIndex.isEmpty()) {
            strIndex = strIndex.replace(" ", ",");
            String strIndexes[] = strIndex.split(",");

            indexes = new int[strIndexes.length];

            for (int i = 0; i < strIndexes.length; i++) {
                try {
                    indexes[i] = (int) Double.parseDouble(region.processText(strIndexes[i]));
                    indexes[i]--;
                } catch (Throwable e) {
                }
            }
        }
        drawActive(component, indexes, g2, bPlayback, transparency);
    }

    public void drawInfo(Graphics2D g2, boolean bPlayback, float transparency) {
        ActiveRegion region = this.region;
        if (region == null) {
            return;
        }
        boolean selected = bPlayback ? false : SketchletEditor.getInstance().getMode() == SketchletEditorMode.EDITING_REGIONS && region.getParent().getMouseHelper().getSelectedRegions() != null && region.getParent().getMouseHelper().getSelectedRegions().contains(region);
        Vector<String> info = VariablesRelationsRenderer.getRegionInfo(region, selected);

        int x = region.getX1Value();
        int y = region.getY1Value();

        FontRenderContext frc = g2.getFontRenderContext();
        Font font = g2.getFont();

        LineMetrics metrics = font.getLineMetrics(" ", frc);
        int yStep = (int) metrics.getHeight();
        x += 5;
        y += yStep;

        Color textColor = Color.BLACK;

        for (String strInfo : info) {
            metrics = font.getLineMetrics(strInfo, frc);
            g2.setColor(Color.WHITE);
            g2.setComposite(makeComposite(0.7f * transparency));
            g2.fillRect(x - 3, (int) (y - metrics.getAscent()), (int) font.getStringBounds(strInfo, frc).getWidth() + 6, (int) metrics.getHeight() + 4);
            y += yStep;
        }

        y = bPlayback ? region.getY1Value() : region.getY1Value();
        y += yStep;
        for (String strInfo : info) {
            if (strInfo.startsWith("on ") || strInfo.startsWith("when ") || strInfo.startsWith("connect ") || strInfo.startsWith("animate ")) {
                g2.setColor(Color.BLUE);
            } else {
                g2.setColor(textColor);
            }
            g2.setComposite(makeComposite(1.0f * transparency));
            g2.drawString(strInfo, x, y);
            y += yStep;
            if (strInfo.isEmpty()) {
                textColor = Color.DARK_GRAY;
            }
        }
    }

    private AlphaComposite makeComposite(float alpha) {
        int type = AlphaComposite.SRC_OVER;
        return (AlphaComposite.getInstance(type, alpha));
    }

    public ShapeLayer getShapeImageLayer() {
        return shapeImageLayer;
    }

    public void setShapeImageLayer(ShapeLayer shapeImageLayer) {
        this.shapeImageLayer = shapeImageLayer;
    }

    public WidgetLayer getWidgetImageLayer() {
        return widgetImageLayer;
    }

    public void setWidgetImageLayer(WidgetLayer widgetImageLayer) {
        this.widgetImageLayer = widgetImageLayer;
    }

    public TextDrawingLayer getTextImageLayer() {
        return textImageLayer;
    }

    public void setTextImageLayer(TextDrawingLayer textImageLayer) {
        this.textImageLayer = textImageLayer;
    }

    public DrawnImageLayer getDrawnImageLayer() {
        return drawnImageLayer;
    }

    public void setDrawnImageLayer(DrawnImageLayer drawnImageLayer) {
        this.drawnImageLayer = drawnImageLayer;
    }

    public TrajectoryDrawingLayer getTrajectoryDrawingLayer() {
        return trajectoryDrawingLayer;
    }

    public void setTrajectoryDrawingLayer(TrajectoryDrawingLayer trajectoryDrawingLayer) {
        this.trajectoryDrawingLayer = trajectoryDrawingLayer;
    }

    public ImageDrawingLayer getImageDrawingLayer() {
        return imageDrawingLayer;
    }

    public void setImageDrawingLayer(ImageDrawingLayer imageDrawingLayer) {
        this.imageDrawingLayer = imageDrawingLayer;
    }

    public AuxiliaryDrawingLayer getAuxiliaryDrawingLayer() {
        return auxiliaryDrawingLayer;
    }

    public void setAuxiliaryDrawingLayer(AuxiliaryDrawingLayer auxiliaryDrawingLayer) {
        this.auxiliaryDrawingLayer = auxiliaryDrawingLayer;
    }

    public Perspective getPerspective() {
        return perspective;
    }

    public void setPerspective(Perspective perspective) {
        this.perspective = perspective;
    }

    public DropAreasRenderer getDropAreasRenderer() {
        if (dropAreasRenderer == null && region.getMouseController() != null) {
            dropAreasRenderer = new DropAreasRenderer(region.getMouseController().getDropAreas());
        }

        return dropAreasRenderer;
    }

    public ActiveRegion getRegion() {
        return region;
    }
}
