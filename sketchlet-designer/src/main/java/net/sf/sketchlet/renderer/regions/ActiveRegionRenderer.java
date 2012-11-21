/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.renderer.regions;

import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.SketchletEditorMode;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.designer.playback.ui.PlaybackPanel;
import net.sf.sketchlet.designer.editor.ui.profiles.Profiles;
import net.sf.sketchlet.model.evaluator.Evaluator;
import net.sf.sketchlet.model.ActiveRegion;
import net.sf.sketchlet.model.ActiveRegions;
import net.sf.sketchlet.model.events.region.ActiveRegionMouseHandler;
import net.sf.sketchlet.renderer.page.VariablesRelationsRenderer;
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

    public static final Image MOUSE_ICON = Workspace.createImageIcon("resources/mouse.png").getImage();
    public static final Image REGIONS_ICON = Workspace.createImageIcon("resources/overlap.png").getImage();
    public static final Image MOVE_ROTATE_ICON = Workspace.createImageIcon("resources/move_rotate.png").getImage();
    public static final Image PROPERTIES_ICON = Workspace.createImageIcon("resources/details_transparent.png").getImage();

    public static final int CORNER_SIZE = 8;

    private ShapeLayer shapeImageLayer;
    private WidgetLayer widgetImageLayer;
    private TextDrawingLayer textImageLayer;
    private DrawnImageLayer drawnImageLayer;
    private TrajectoryDrawingLayer trajectoryDrawingLayer;
    private ImageDrawingLayer imageDrawingLayer;
    private AuxiliaryDrawingLayer auxiliaryDrawingLayer;
    private Perspective perspective;

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
    }

    public void activate(boolean bPlayback) {
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
            if (region.parent != null) {
                String strTransparency = region.getSketch().getPropertyValue("transparency layer " + (region.layer + 1));
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
        draw(g, null, SketchletEditorMode.ACTIONS, false, false, 1.0f);
    }

    public synchronized void draw(Graphics2D g2, Component component, SketchletEditorMode mode, boolean bPlayback, boolean bHighlightRegions, float transparency) {
        try {
            double scale = SketchletEditor.getInstance() != null ? SketchletEditor.getInstance().getScale() : 1.0;
            float fontSize = (float) (11 / scale);
            if (fontSize < 11) {
                fontSize = 11;
            }

            Font font = g2.getFont().deriveFont(fontSize);
            g2.setFont(font);
            AffineTransform oldTransform = g2.getTransform();
            prepare(bPlayback);

            transparency *= getLayerTransparency();

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));

            if (bPlayback) {
                if (region.parent != null) {
                    g2.translate(region.parent.getOffset_x(), region.parent.getOffset_y());
                }
                g2.shear(region.playback_shearX, region.playback_shearY);
                if (region.playback_rotation != 0.0) {
                    g2.rotate(region.playback_rotation,
                            region.playback_x1 + (region.playback_x2 - region.playback_x1) * region.center_rotation_x,
                            region.playback_y1 + (region.playback_y2 - region.playback_y1) * region.center_rotation_y);
                }
                if (region.parent != null) {
                    g2.translate(-region.parent.getOffset_x(), -region.parent.getOffset_y());
                }
            } else {
                g2.shear(region.shearX, region.shearY);
                if (region.rotation != 0.0) {
                    g2.rotate(region.rotation,
                            region.getX1() + (region.x2 - region.getX1()) * region.center_rotation_x,
                            region.y1 + (region.y2 - region.y1) * region.center_rotation_y);
                }

            }

            drawActive(g2, component, bPlayback, transparency);

            if (SketchletEditor.getInstance() != null && SketchletEditor.getInstance().getSketchToolbar() != null) {
                if (SketchletEditor.getInstance().getSketchToolbar().bVisualizeInfoRegions) {
                    this.drawInfo(g2, bPlayback, transparency);
                }
            }
            if (bPlayback && region == PlaybackPanel.selectedRegion) {
                Stroke stroke = g2.getStroke();
                g2.setStroke(new BasicStroke(1));
                g2.setColor(Color.ORANGE);
                g2.drawRect(region.playback_x1, region.playback_y1, region.getWidth(), region.getHeight());
                g2.setStroke(stroke);
            }

            if (region == null) {
                return;
            }

            if (!bPlayback) {
                int w = Math.abs(region.x2 - region.getX1());
                int h = Math.abs(region.y2 - region.y1);
                int _x1 = Math.min(region.getX1(), region.x2);
                int _x2 = Math.max(region.getX1(), region.x2);
                int _y1 = Math.min(region.y1, region.y2);
                int _y2 = Math.max(region.y1, region.y2);

                g2.setStroke(new BasicStroke(1));

                boolean selected = false;

                boolean bInDrag = false;
                if (SketchletEditor.getInstance() == null) {
                    return;
                }
                Point p = region.getInversePoint(false, (int) (FileDrop.getMouseX() / SketchletEditor.getInstance().getScale()), (int) (FileDrop.getMouseY() / SketchletEditor.getInstance().getScale()));
                bInDrag = region.getMouseHandler().inRect((int) (FileDrop.getMouseX() / SketchletEditor.getInstance().getScale()), (int) (FileDrop.getMouseY() / SketchletEditor.getInstance().getScale()), false);

                Rectangle r_properties = region.getPropertiesIconRectangle();
                Rectangle r_mouse = region.getMouseIconRectangle();
                Rectangle r_motion = region.getMappingIconRectangle();
                Rectangle r_overlap = region.getInRegionsIconRectangle();

                double sc = Math.min(1.0, SketchletEditor.getInstance().getScale());
                int iconSize = (int) (24 / sc);
                int iconOffset = (int) (4 / sc);
                int rectMargin = (int) (2 / sc);

                if ((mode == SketchletEditorMode.ACTIONS && (FileDrop.isDragging())) || bInDrag) {
                    if (this.PROPERTIES_ICON != null) {
                        g2.drawImage(PROPERTIES_ICON, r_properties.x + iconOffset + 1, r_properties.y + iconOffset + 1, iconSize, iconSize, null);
                    }
                    if (MOVE_ROTATE_ICON != null && Profiles.isActive("active_region_move")) {
                        g2.drawImage(MOVE_ROTATE_ICON, r_motion.x + iconOffset + 1, r_motion.y + iconOffset + 1, iconSize, iconSize, null);
                    }
                    if (MOUSE_ICON != null && Profiles.isActive("active_region_mouse")) {
                        g2.drawImage(MOUSE_ICON, r_mouse.x + iconOffset + 1, r_mouse.y + iconOffset + 1, iconSize, iconSize, null);
                    }
                    if (REGIONS_ICON != null && Profiles.isActive("active_region_overlap")) {
                        g2.drawImage(REGIONS_ICON, r_overlap.x + iconOffset + 1, r_overlap.y + iconOffset + 1, iconSize, iconSize, null);
                    }
                }

                if (mode == SketchletEditorMode.ACTIONS || bInDrag) {
                    selected = region.parent.getSelectedRegions() != null && region.parent.getSelectedRegions().contains(region);
                    if (bInDrag) {
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
                if (selected || (!bPlayback && region.bInFocus)) {
                    g2.fillRect(_x1, _y1, w, h);
                }
                if (bInDrag) {
                    g2.setStroke(new BasicStroke(1));
                    g2.setColor(new Color(255, 255, 255));
                    if (region.isInMappingIconArea(p.x, p.y) && FileDrop.getCurrentString().startsWith("=")) {
                        g2.fillRoundRect(r_motion.x + rectMargin, r_motion.y + rectMargin, r_motion.width - rectMargin, r_motion.height - rectMargin, rectMargin * 6, rectMargin * 6);
                        g2.drawImage(MOVE_ROTATE_ICON, r_motion.x + iconOffset + 1, r_motion.y + iconOffset + 1, iconSize, iconSize, null);
                        g2.setColor(new Color(100, 100, 100));
                        g2.drawRoundRect(r_motion.x + 2, r_motion.y + rectMargin, r_motion.width - rectMargin, r_motion.height - rectMargin, rectMargin * 6, rectMargin * 6);
                        g2.drawString("motion and rotation", r_motion.x + rectMargin, r_motion.y - 6);
                        g2.setColor(new Color(255, 255, 255));
                    } else if (region.isInMouseIconArea(p.x, p.y)) {
                        g2.fillRoundRect(r_mouse.x + rectMargin, r_mouse.y + rectMargin, r_mouse.width - rectMargin, r_mouse.height - rectMargin, rectMargin * 6, rectMargin * 6);
                        g2.drawImage(MOUSE_ICON, r_mouse.x + iconOffset + 1, r_mouse.y + iconOffset + 1, iconSize, iconSize, null);
                        g2.setColor(new Color(100, 100, 100));
                        g2.drawRoundRect(r_mouse.x + 2, r_mouse.y + rectMargin, r_mouse.width - rectMargin, r_mouse.height - rectMargin, rectMargin * 6, rectMargin * 6);
                        g2.drawString("mouse events (click, press, release, double ckick...)", r_mouse.x + rectMargin, r_mouse.y - 6);
                        g2.setColor(new Color(255, 255, 255));
                    } else if (region.isInRegionsIconArea(p.x, p.y)) {
                        g2.fillRoundRect(r_overlap.x + rectMargin, r_overlap.y + rectMargin, r_overlap.width - rectMargin, r_overlap.height - rectMargin, rectMargin * 6, rectMargin * 6);
                        g2.drawImage(REGIONS_ICON, r_overlap.x + iconOffset + 1, r_overlap.y + iconOffset + 1, iconSize, iconSize, null);
                        g2.setColor(new Color(100, 100, 100));
                        g2.drawRoundRect(r_overlap.x + 2, r_overlap.y + rectMargin, r_overlap.width - rectMargin, r_overlap.height - rectMargin, rectMargin * 6, rectMargin * 6);
                        g2.drawString("regions overlap events", r_overlap.x + rectMargin, r_overlap.y - 6);
                        g2.setColor(new Color(255, 255, 255));
                    } else if (FileDrop.getCurrentString().startsWith("=")) {
                        g2.fillRoundRect(r_properties.x + rectMargin, r_properties.y + rectMargin, r_properties.width - rectMargin, r_properties.height - rectMargin, rectMargin * 6, rectMargin * 6);
                        g2.drawImage(PROPERTIES_ICON, r_properties.x + iconOffset + 1, r_properties.y + iconOffset + 1, iconSize, iconSize, null);
                        g2.setColor(new Color(100, 100, 100));
                        g2.drawRoundRect(r_properties.x + 2, r_properties.y + rectMargin, r_properties.width - rectMargin, r_properties.height - rectMargin, rectMargin * 6, rectMargin * 6);
                        g2.drawString("region parameters", r_properties.x + rectMargin, r_properties.y - 6);
                        g2.setColor(new Color(255, 255, 255));
                    }
                }

                if (mode == SketchletEditorMode.ACTIONS) {
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.drawRect(_x1, _y1, w, h);
                    Stroke stroke = g2.getStroke();
                    g2.setStroke(new BasicStroke(1));

                    int _cornerSize = (int) (CORNER_SIZE / SketchletEditor.getInstance().getScale());

                    if (selected) {
                        if (SketchletEditor.getInstance().isDragging() && !this.region.getMouseHandler().isbRotating()) {
                            g2.setColor(new Color(255, 255, 255, 100));
                            g2.fillRect(region.getX1(), region.getY1() - 12, region.getWidth(), 12);
                            g2.setColor(Color.BLACK);
                            g2.drawString(Language.translate("Left") + ": " + region.getX1() + " " + Language.translate("Top") + ": " + region.getY1() + " " + Language.translate("Width") + ": " + region.getWidth() + " " + Language.translate("Height") + ": " + region.getHeight(), _x1, _y1 - 3);
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

                        if (!region.trajectory1.isEmpty()) {
                            g2.setColor(Color.WHITE);
                            g2.fillOval(_x1 - _cornerSize / 2 - 1 + (int) (w * region.trajectory2_x), _y1 - _cornerSize / 2 - 1 + (int) (h * region.trajectory2_y), _cornerSize + 2, _cornerSize + 2);
                            g2.setColor(new Color(0, 0, 255, 100));
                            g2.fillOval(_x1 - _cornerSize / 2 + (int) (w * region.trajectory2_x), _y1 - _cornerSize / 2 + (int) (h * region.trajectory2_y), _cornerSize, _cornerSize);
                        }

                        if (!SketchletEditor.getInstance().isInShiftMode()) {
                            g2.setColor(Color.WHITE);
                            g2.fillOval(_x1 - _cornerSize / 2 + (int) (w * region.center_rotation_x), _y1 - _cornerSize / 2 + (int) (h * region.center_rotation_y), _cornerSize, _cornerSize);
                            g2.setColor(Color.GRAY);
                            g2.drawOval(_x1 - _cornerSize / 2 - 1 + (int) (w * region.center_rotation_x), _y1 - _cornerSize / 2 - 1 + (int) (h * region.center_rotation_y), _cornerSize + 2, _cornerSize + 2);
                        }
                        if (!SketchletEditor.getInstance().isInShiftMode() && region.getMouseHandler().getSelectedCorner() == ActiveRegionMouseHandler.CENTER_ROTATION) {
                            int __x = _x1 - _cornerSize / 2 - 1 + (int) (w * region.getMouseHandler().getCenterRotationX());
                            int __y = _y1 - _cornerSize / 2 - 1 + (int) (h * region.getMouseHandler().getCenterRotationY());
                            g2.drawOval(__x, __y, _cornerSize + 2, _cornerSize + 2);
                            g2.drawString("rotation center", __x, __y + 20);
                        } else if (SketchletEditor.getInstance().isInCtrlMode() && SketchletEditor.getInstance().isInShiftMode()) {
                            if (region.getMouseHandler().getSelectedCorner() == ActiveRegionMouseHandler.TRAJECTORY2_POINT) {
                                int __x = _x1 - _cornerSize / 2 - 1 + (int) (w * region.getMouseHandler().getCenterRotationX());
                                int __y = _y1 - _cornerSize / 2 - 1 + (int) (h * region.getMouseHandler().getCenterRotationY());
                                g2.drawOval(__x, __y, _cornerSize + 2, _cornerSize + 2);
                                __x = _x1 - _cornerSize / 2 - 1 + (int) (w * region.getMouseHandler().getTrajectory2X());
                                __y = _y1 - _cornerSize / 2 - 1 + (int) (h * region.getMouseHandler().getTrajectory2Y());
                                g2.setColor(Color.WHITE);
                                g2.drawOval(__x, __y, _cornerSize + 2, _cornerSize + 2);
                                g2.setColor(Color.GRAY);
                                g2.drawString("trajectory point 2", __x, __y + 20);
                                g2.setColor(Color.WHITE);
                                int __x1 = _x1 + (int) (w * region.center_rotation_x);
                                int __y1 = _y1 + (int) (h * region.center_rotation_y);
                                int __x2 = _x1 + (int) (w * region.getMouseHandler().getTrajectory2X());
                                int __y2 = _y1 + (int) (h * region.getMouseHandler().getTrajectory2Y());
                                g2.drawLine(__x1, __y1, __x2, __y2);
                            }
                        }

                        if (this.region.getMouseHandler().isbRotating() && SketchletEditor.getInstance().isDragging()) {
                            double circelSize = 60 / SketchletEditor.getInstance().getScale();
                            g2.drawOval((int) (_x1 - circelSize / 2 - 1 + w * region.center_rotation_x), (int) (_y1 - circelSize / 2 - 1 + h * region.center_rotation_y), (int) (circelSize + 2), (int) (circelSize + 2));
                            g2.setColor(Color.GRAY);
                            for (int r = 0; r < 360; r += 15) {
                                double angle = Math.toRadians(r) - region.rotation;
                                double r_x1 = _x1 + w * region.center_rotation_x + circelSize / 2 * Math.cos(angle);
                                double r_y1 = _y1 + h * region.center_rotation_y + circelSize / 2 * Math.sin(angle);
                                double r_x2 = _x1 + w * region.center_rotation_x + (circelSize / 2 - 5) * Math.cos(angle);
                                double r_y2 = _y1 + h * region.center_rotation_y + (circelSize / 2 - 5) * Math.sin(angle);

                                g2.drawLine((int) r_x1, (int) r_y1, (int) r_x2, (int) r_y2);
                            }
                            g2.setColor(Color.RED);
                            double angle = -Math.PI / 2;
                            double r_x1 = _x1 + w * region.center_rotation_x + circelSize / 2 * Math.cos(angle);
                            double r_y1 = _y1 + h * region.center_rotation_y + circelSize / 2 * Math.sin(angle);
                            double r_x2 = _x1 + w * region.center_rotation_x + (circelSize / 2 - 10) * Math.cos(angle);
                            double r_y2 = _y1 + h * region.center_rotation_y + (circelSize / 2 - 10) * Math.sin(angle);
                            g2.drawLine((int) r_x1, (int) r_y1, (int) r_x2, (int) r_y2);

                            g2.setColor(Color.GRAY);
                            g2.drawString("  " + ((int) Math.toDegrees(region.rotation)), (int) r_x1 - 10, (int) r_y1 - 5);
                        }
                        g2.setColor(Color.YELLOW);
                        g2.fillOval(_x1 - _cornerSize / 2 + (int) (w * region.p_x0), _y1 - _cornerSize / 2 + (int) (h * region.p_y0), _cornerSize, _cornerSize);
                        g2.fillOval(_x1 - _cornerSize / 2 + (int) (w * region.p_x1), _y1 - _cornerSize / 2 + (int) (h * region.p_y1), _cornerSize, _cornerSize);
                        g2.fillOval(_x1 - _cornerSize / 2 + (int) (w * region.p_x2), _y1 - _cornerSize / 2 + (int) (h * region.p_y2), _cornerSize, _cornerSize);
                        g2.fillOval(_x1 - _cornerSize / 2 + (int) (w * region.p_x3), _y1 - _cornerSize / 2 + (int) (h * region.p_y3), _cornerSize, _cornerSize);
                        g2.drawLine(_x1 + (int) (w * region.p_x0), _y1 + (int) (h * region.p_y0),
                                _x1 + (int) (w * region.p_x1), _y1 + (int) (h * region.p_y1));
                        g2.drawLine(_x1 + (int) (w * region.p_x1), _y1 + (int) (h * region.p_y1),
                                _x1 + (int) (w * region.p_x2), _y1 + (int) (h * region.p_y2));
                        g2.drawLine(_x1 + (int) (w * region.p_x2), _y1 + (int) (h * region.p_y2),
                                _x1 + (int) (w * region.p_x3), _y1 + (int) (h * region.p_y3));
                        g2.drawLine(_x1 + (int) (w * region.p_x3), _y1 + (int) (h * region.p_y3),
                                _x1 + (int) (w * region.p_x0), _y1 + (int) (h * region.p_y0));

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
                    g2.setColor(Color.BLACK);
                    g2.drawString(region.getNumber(), _x1 + w / 2 - 2, _y1 + h / 2 + 5);
                    g2.drawString(region.name, _x1 + w / 2 - 2, _y1 + h / 2 + 17);
                }
            }

            g2.setTransform(oldTransform);

            if (!bPlayback) {
                this.getTrajectoryDrawingLayer().draw(g2, component, bPlayback);
                getAuxiliaryDrawingLayer().drawLimits(g2, component, mode);
            } else if (bHighlightRegions) {
                getAuxiliaryDrawingLayer().drawHighlight(g2);
            }
        } catch (Throwable e) {
            log.error(e);
        }
    }

    public synchronized void prepare(boolean bPlayback) {
        prepare(bPlayback, true);
    }

    public synchronized void prepare(boolean bPlayback, boolean bProcessLimits) {
        if (region == null) {
            return;
        }
        if (bPlayback) {
            region.setDataFromPlayback();
        }
        try {
            region.horizontalAlignment = region.processText(region.horizontalAlignment);
            region.verticalAlignment = region.processText(region.verticalAlignment);

            String strX = region.processText(region.strX).trim();
            String strY = region.processText(region.strY).trim();
            String strX1 = region.processText(region.strX1).trim();
            String strY1 = region.processText(region.strY1).trim();
            String strX2 = region.processText(region.strX2).trim();
            String strY2 = region.processText(region.strY2).trim();
            String strRelX1 = region.processText(region.strRelX).trim();
            String strRelY1 = region.processText(region.strRelY).trim();
            String strTrajectoryPosition = region.processText(region.strTrajectoryPosition).trim();
            String strWidth = region.processText(region.strWidth).trim();
            String strHeight = region.processText(region.strHeight).trim();
            String strRotate = region.processText(region.strRotate).trim();
            String strShearX = region.processText(region.strShearX).trim();
            String strShearY = region.processText(region.strShearY).trim();

            int prevX1 = region.x1;
            int prevY1 = region.y1;
            int prevX2 = region.x2;
            int prevY2 = region.y2;

            double prevRotation = region.rotation;
            double prevShearX = region.shearX;
            double prevShearY = region.shearY;

            int w = region.x2 - region.x1;
            int h = region.y2 - region.y1;

            if (!strX.isEmpty()) {
                try {
                    int x = (int) InteractionSpace.getSketchX(Double.parseDouble(strX));

                    if (region.horizontalAlignment.equalsIgnoreCase("center")) {
                        x -= w / 2;
                    } else if (region.horizontalAlignment.equalsIgnoreCase("right")) {
                        x -= w;
                    } else {
                    }

                    region.x1 = x;
                    region.x2 = region.x1 + w;

                    if (bProcessLimits) {
                        region.processLimitsX();
                    }
                } catch (Throwable e) {
                }
            } else if (!strRelX1.isEmpty()) {
                try {
                    double limitsX[] = region.getMotionHandler().getLimits("position x", 0, w);
                    int x = (int) InteractionSpace.getSketchX(limitsX[0] + (limitsX[1] - limitsX[0]) * Math.min(1.0, Double.parseDouble(strRelX1)));
                    if (region.horizontalAlignment.equalsIgnoreCase("center")) {
                        x -= w / 2;
                    } else if (region.horizontalAlignment.equalsIgnoreCase("right")) {
                        x -= w;
                    } else {
                    }

                    region.x2 += x - region.x1;
                    region.x1 = x;
                    if (bProcessLimits) {
                        region.processLimitsX();
                    }
                } catch (Throwable e) {
                }
            } else if (!strX1.isEmpty() || !strX2.isEmpty()) {
                if (!strX1.isEmpty()) {
                    try {
                        int x = (int) InteractionSpace.getSketchX(Double.parseDouble(strX1));
                        region.x1 = x;
                    } catch (Throwable e) {
                    }
                }
                if (!strX2.isEmpty()) {
                    try {
                        int x = (int) InteractionSpace.getSketchX(Double.parseDouble(strX2));
                        region.x2 = x;
                    } catch (Throwable e) {
                    }
                }
                if (bProcessLimits) {
                    region.processLimitsX();
                }
            }
            if (!strY.isEmpty()) {
                try {
                    int y = (int) InteractionSpace.getSketchY(Double.parseDouble(strY));
                    if (region.verticalAlignment.equalsIgnoreCase("center")) {
                        y -= h / 2;
                    } else if (region.verticalAlignment.equalsIgnoreCase("bottom")) {
                        y -= h;
                    } else {
                    }

                    region.y1 = y;
                    region.y2 = region.y1 + h;
                    if (bProcessLimits) {
                        region.processLimitsY();
                    }
                } catch (Throwable e) {
                }
            } else if (!strRelY1.isEmpty()) {
                try {
                    double limitsY[] = region.getMotionHandler().getLimits("position y", 0, h);
                    int y = (int) InteractionSpace.getSketchY(limitsY[0] + (limitsY[1] - limitsY[0]) * Math.min(1.0, Double.parseDouble(strRelY1)));
                    if (region.verticalAlignment.equalsIgnoreCase("center")) {
                        y -= h / 2;
                    } else if (region.verticalAlignment.equalsIgnoreCase("bottom")) {
                        y -= h;
                    } else {
                    }

                    region.y2 += y - region.y1;
                    region.y1 = y;
                    if (bProcessLimits) {
                        region.processLimitsY();
                    }
                } catch (Throwable e) {
                }
            } else if (!strY1.isEmpty() || !strY2.isEmpty()) {
                if (!strY1.isEmpty()) {
                    try {
                        int y = (int) InteractionSpace.getSketchX(Double.parseDouble(strY1));
                        region.y1 = y;
                    } catch (Throwable e) {
                    }
                }
                if (!strY2.isEmpty()) {
                    try {
                        int y = (int) InteractionSpace.getSketchX(Double.parseDouble(strY2));
                        region.y2 = y;
                    } catch (Throwable e) {
                    }
                }
                if (bProcessLimits) {
                    region.processLimitsY();
                }
            }
            if (!strWidth.isEmpty()) {
                try {
                    int oldW = region.x2 - region.x1;
                    int centerX = region.x1 + oldW / 2;
                    int newW = (int) InteractionSpace.getSketchWidth(Double.parseDouble(strWidth));
                    if (region.horizontalAlignment.equalsIgnoreCase("center")) {
                        region.x1 = centerX - newW / 2;
                        region.x2 = centerX + newW / 2;
                    } else if (region.horizontalAlignment.equalsIgnoreCase("right")) {
                        region.x1 = region.x2 - newW;
                    } else {
                        region.x2 = region.x1 + newW;
                    }
                    w = newW;
                } catch (Throwable e) {
                    //log.error(e);
                }
            }
            if (!strHeight.isEmpty()) {
                try {
                    int oldH = region.y2 - region.y1;
                    int centerY = region.y1 + oldH / 2;
                    int newH = (int) InteractionSpace.getSketchHeight(Double.parseDouble(strHeight));
                    if (region.verticalAlignment.equalsIgnoreCase("center")) {
                        region.y1 = centerY - newH / 2;
                        region.y2 = centerY + newH / 2;
                    } else if (region.verticalAlignment.equalsIgnoreCase("bottom")) {
                        region.y1 = region.y2 - newH;
                    } else {
                        region.y2 = region.y1 + newH;
                    }
                    h = newH;
                } catch (Throwable e) {
                    //log.error(e);
                }
            }

            if (!region.inTrajectoryMode && !region.inTrajectoryMode2 && !region.trajectory1.trim().isEmpty()) {
                if (!strTrajectoryPosition.isEmpty() && region.stickToTrajectoryEnabled) {
                    try {
                        double pos = Double.parseDouble(strTrajectoryPosition);
                        if (!Double.isNaN(pos)) {
                            Point p = getTrajectoryDrawingLayer().getTrajectoryPoint(pos);
                            if (p != null) {
                                region.x1 = (int) (p.x - w * region.center_rotation_x);
                                region.y1 = (int) (p.y - h * region.center_rotation_y);
                                region.x2 = region.x1 + w;
                                region.y2 = region.y1 + h;
                                if (bProcessLimits) {
                                    region.processLimitsX();
                                    region.processLimitsY();
                                    region.processLimitsTrajectory(p);
                                }
                                if (region.changingOrientationOnTrajectoryEnabled) {
                                    region.rotation = getTrajectoryDrawingLayer().trajectoryOrientationFromPoint;
                                }
                            }
                        }
                    } catch (Throwable e) {
                        //log.error(e);
                    }
                }
            }

            if (!strRotate.isEmpty()) {
                try {
                    double rotDeg = Double.parseDouble(strRotate);
                    rotDeg = InteractionSpace.wrapPhysicalAngle(rotDeg);

                    region.rotation = InteractionSpace.toRadians(rotDeg);
                } catch (Throwable e) {
                }
            }

            if (!strShearX.isEmpty()) {
                try {
                    region.shearX = Double.parseDouble(strShearX);
                } catch (Throwable e) {
                }
            } else {
                region.shearX = 0.0;
            }

            if (!strShearY.isEmpty()) {
                try {
                    region.shearY = Double.parseDouble(strShearY);
                } catch (Throwable e) {
                }
            } else {
                region.shearY = 0.0;
            }

            if (region.parent != null) {
                region.getInteractionHandler().processInteractionEvents(bPlayback, region.parent.getPage().getActiveTimers(), region.parent.getPage().getActiveMacros());
                region.getSketch().updateConnectors(region, bPlayback);
            }

            if (region != null && region.getInteractionHandler() != null) {
                if (!region.isWithinLimits(bPlayback) || region.getInteractionHandler().intersectsWithSolids(bPlayback)) {
                    ActiveRegions.findNonOverlapingLocation(region);
                    region.rotation = prevRotation;
                    region.shearX = prevShearX;
                    region.shearY = prevShearY;
                } else {
                    if (!region.regionGrouping.isEmpty()) {
                        for (ActiveRegion as : region.parent.getRegions()) {
                            if (as != region && as.regionGrouping.equals(region.regionGrouping)) {
                                if (bPlayback) {
                                    as.setDataFromPlayback();
                                    as.x1 += region.x1 - prevX1;
                                    as.y1 += region.y1 - prevY1;
                                    as.x2 += region.x2 - prevX2;
                                    as.y2 += region.y2 - prevY2;
                                    if (bProcessLimits) {
                                        as.getMotionHandler().processLimits("position x", as.x1, 0, 0, true);
                                        as.getMotionHandler().processLimits("position y", as.y1, 0, 0, true);
                                    }
                                    as.resetFromPlayback();
                                } else {
                                    if (region.x1 - prevX1 != 0 || region.y1 - prevY1 != 0) {
                                        as.x1 += region.x1 - prevX1;
                                        as.y1 += region.y1 - prevY1;
                                        as.x2 += region.x2 - prevX2;
                                        as.y2 += region.y2 - prevY2;
                                        if (bProcessLimits) {
                                            as.getMotionHandler().processLimits("position x", as.x1, 0, 0, true);
                                            as.getMotionHandler().processLimits("position y", as.y1, 0, 0, true);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable e) {
            log.error(e);
        }
        if (bPlayback) {
            region.resetFromPlayback();
        }
    }

    public boolean shouldRedraw(boolean bPlayback) {
        if (buffer == null) {
            return true;
        }

        for (int i = 0; i < region.getImageCount(); i++) {
            if (region.isDrawImageChanged(i)) {
                return true;
            }
        }

        if (region.screenCapturingEnabled) {
            return true;
        }

        if (bPlayback && !region.strPen.isEmpty()) {
            return true;
        }

        if (!region.strWidget.isEmpty()) {
            return true;
        }

        if (oldW != region.getWidth() || oldH != region.getHeight()) {
            oldW = region.getWidth();
            oldH = region.getHeight();
            return true;
        }

        if (old_p_x0 != region.p_x0 || old_p_y0 != region.p_y0
                || old_p_x1 != region.p_x1 || old_p_y1 != region.p_y1
                || old_p_x2 != region.p_x2 || old_p_y2 != region.p_y2
                || old_p_x3 != region.p_x3 || old_p_y3 != region.p_y3) {
            old_p_x0 = region.p_x0;
            old_p_y0 = region.p_y0;
            old_p_x1 = region.p_x1;
            old_p_y1 = region.p_y1;
            old_p_x2 = region.p_x2;
            old_p_y2 = region.p_y2;
            old_p_x3 = region.p_x3;
            old_p_y3 = region.p_y3;
            return true;
        }

        if (this.getTextImageLayer().isFontChanged()) {
            return true;
        }

        String strImageProperties[] = {
                region.strTransparency,
                region.strImageIndex,
                region.strImageUrlField,
                region.text,
                region.strTextField,
                //region.strTextFile,
                region.shape,
                region.lineColor,
                region.strFillColor,
                region.lineThickness,
                region.lineStyle,
                //region.chartSpecification.getText(),
                //region.svgSpecification.getText(),
                //region.htmlSpecification.getText(),
                region.strEmbeddedSketch,
                region.horizontalAlignment,
                region.verticalAlignment,
                region.strPerspectiveDepth,
                region.strRotation3DHorizontal,
                region.strRotation3DVertical,
                region.strShapeArgs,
                region.strShearX,
                region.strShearY,
                region.strWindowX,
                region.strWindowY,
                region.strWindowWidth,
                region.strWindowHeight,
                region.strZoom
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
            if (bPlayback && region.parent != null && region.playback_x2 > 0 && region.playback_y2 > 0) {
                region.setDataFromPlayback();
            }

            if (bPlayback) {
                region.animate();
            }

            if (region.x2 == region.x1 || region.y2 == region.y1) {
                return;
            }

            if (shouldRedraw(bPlayback)) {
                int w = Math.abs(region.x2 - region.x1);
                int h = Math.abs(region.y2 - region.y1);
                if (Math.abs(region.x2 - region.x1) > 10000 || Math.abs(region.y2 - region.y1) > 10000) {
                    region.x1 = 100;
                    region.y1 = 100;
                    region.x2 = 200;
                    region.y2 = 200;

                    buffer = Workspace.createCompatibleImage(100, 100);
                } else {
                    buffer = Workspace.createCompatibleImage(w, h, buffer);
                }

                Graphics2D g2 = buffer.createGraphics();

                g2.translate(-region.x1, -region.y1);

                Area clipShape = region.getArea(bPlayback);
                g2.setColor(new Color(0, 0, 0));

                g2.setClip(clipShape);

                AffineTransform oldTransform = g2.getTransform();
                Composite oldComposite = g2.getComposite();

                if (bPlayback) {
                    getAuxiliaryDrawingLayer().drawPen();
                }

                String strTransparency = region.strTransparency;

                if (!strTransparency.isEmpty()) {
                    strTransparency = region.processText(strTransparency);
                    try {
                        float alpha = (float) Double.parseDouble(strTransparency);
                        g2.setComposite(makeComposite(alpha * transparency));
                    } catch (Throwable e) {
                    }
                }

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                getShapeImageLayer().draw(g2, component, bPlayback, true, false);
                getDrawnImageLayer().init();
                getWidgetImageLayer().draw(g2, component, bPlayback);
                if (getWidgetImageLayer() != null && getWidgetImageLayer().getWidgetPlugin() == null && getImageDrawingLayer() != null && getDrawnImageLayer() != null) {
                    getImageDrawingLayer().draw(g2, component, bPlayback);
                    for (int di = 0; di < indexes.length; di++) {
                        getDrawnImageLayer().setIndex(indexes[di]);
                        getDrawnImageLayer().draw(g2, component, bPlayback);
                    }
                    getTextImageLayer().draw(g2, component, bPlayback);
                }
                getShapeImageLayer().draw(g2, component, bPlayback, false, true);
                g2.setComposite(oldComposite);
                g2.setTransform(oldTransform);
                g2.dispose();
            }

            getPerspective().doPerspectiveAndDrawImage(g, buffer);

            if (bPlayback && region != null && region.playback_x2 > 0 && region.playback_y2 > 0) {
                region.resetFromPlayback();
            }
        } catch (Throwable e) {
            log.error(e);
        }
    }

    public void drawImageWin(Graphics2D g2, BufferedImage image, int dx, int dy, int dw, int dh) {
        String strZoom = region.processText((region.strZoom).trim()).trim();
        String strWindowX = region.processText((region.strWindowX).trim()).trim();
        String strWindowY = region.processText((region.strWindowY).trim()).trim();
        String strWindowWidth = region.processText((region.strWindowWidth).trim()).trim();
        String strWindowHeight = region.processText((region.strWindowHeight).trim()).trim();

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

                    sx = (int) ((sw - __w) * region.center_rotation_x);
                    sy = (int) ((sh - __h) * region.center_rotation_y);

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
        String strIndex = region.processText(region.strImageIndex);

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
        boolean selected = bPlayback ? false : SketchletEditor.getInstance().getMode() == SketchletEditorMode.ACTIONS && region.parent.getSelectedRegions() != null && region.parent.getSelectedRegions().contains(region);
        Vector<String> info = VariablesRelationsRenderer.getRegionInfo(region, selected);

        int x = bPlayback ? region.playback_x1 : region.getX1();
        int y = bPlayback ? region.playback_y1 : region.getY1();

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

        y = bPlayback ? region.playback_y1 : region.getY1();
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
}
