/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.regions.renderer;

import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.data.ActiveRegions;
import net.sf.sketchlet.designer.data.Evaluator;
import net.sf.sketchlet.designer.editor.EditorMode;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.renderer.DrawVariables;
import net.sf.sketchlet.designer.events.region.ActiveRegionMouseHandler;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.designer.ui.playback.PlaybackPanel;
import net.sf.sketchlet.designer.ui.profiles.Profiles;
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

    public static Image mouseIcon = Workspace.createImageIcon("resources/mouse.png").getImage();
    public static Image regionsIcon = Workspace.createImageIcon("resources/overlap.png").getImage();
    public static Image moveRotateIcon = Workspace.createImageIcon("resources/move_rotate.png").getImage();
    public static Image propertiesIcon = Workspace.createImageIcon("resources/details_transparent.png").getImage();
    ActiveRegion region;
    public final static int cornerSize = 8;
    public ShapeLayer shapeImageLayer;
    public WidgetLayer widgetImageLayer;
    public TextDrawingLayer textImageLayer;
    public DrawnImageLayer drawnImageLayer;
    public TrajectoryDrawingLayer trajectoryDrawingLayer;
    public ImageDrawingLayer imageDrawingLayer;
    public AuxiliaryDrawingLayer auxiliaryDrawingLayer;
    //public WidgetCharts chartDrawingLayer;
    //public WidgetHTML htmlDrawingLayer;
    //public WidgetSVG svgDrawingLayer;
    public Perspective perspective;
    BufferedImage buffer;
    String strPropertiesCache = "";
    int oldW = -1;
    int oldH = -1;
    double old_p_x0 = -1;
    double old_p_y0 = -1;
    double old_p_x1 = -1;
    double old_p_y1 = -1;
    double old_p_x2 = -1;
    double old_p_y2 = -1;
    double old_p_x3 = -1;
    double old_p_y3 = -1;

    public ActiveRegionRenderer(ActiveRegion region) {
        this.region = region;
        perspective = new Perspective(region);
        shapeImageLayer = new ShapeLayer(region);
        widgetImageLayer = new WidgetLayer(region);
        textImageLayer = new TextDrawingLayer(region);
        drawnImageLayer = new DrawnImageLayer(region);
        trajectoryDrawingLayer = new TrajectoryDrawingLayer(region);
        imageDrawingLayer = new ImageDrawingLayer(region);
        auxiliaryDrawingLayer = new AuxiliaryDrawingLayer(region);
    }

    public void activate(boolean bPlayback) {
    }

    public void deactivate(boolean bPlayback) {
        if (widgetImageLayer.widgetControl != null) {
            widgetImageLayer.widgetControl.deactivate(bPlayback);
        }
    }

    public void flush() {
        if (textImageLayer != null) {
            textImageLayer.flush();
        }
    }

    public void dispose() {
        region = null;

        strPropertiesCache = "";

        if (perspective != null) {
            perspective.dispose();
        }
        if (shapeImageLayer != null) {
            shapeImageLayer.dispose();
        }
        if (widgetImageLayer != null) {
            widgetImageLayer.dispose();
        }
        if (textImageLayer != null) {
            textImageLayer.dispose();
        }
        if (drawnImageLayer != null) {
            drawnImageLayer.dispose();
        }
        if (trajectoryDrawingLayer != null) {
            trajectoryDrawingLayer.dispose();
        }
        if (imageDrawingLayer != null) {
            imageDrawingLayer.dispose();
        }
        if (auxiliaryDrawingLayer != null) {
            auxiliaryDrawingLayer.dispose();
        }

        perspective = null;
        shapeImageLayer = null;
        widgetImageLayer = null;
        textImageLayer = null;
        drawnImageLayer = null;
        trajectoryDrawingLayer = null;
        imageDrawingLayer = null;
        auxiliaryDrawingLayer = null;
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
        draw(g, null, EditorMode.ACTIONS, false, false, 1.0f);
    }

    public synchronized void draw(Graphics2D g2, Component component, EditorMode mode, boolean bPlayback, boolean bHighlightRegions, float transparency) {
        try {
            double scale = SketchletEditor.editorPanel != null ? SketchletEditor.editorPanel.scale : 1.0;
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
                    g2.translate(region.parent.offset_x, region.parent.offset_y);
                }
                g2.shear(region.playback_shearX, region.playback_shearY);
                if (region.playback_rotation != 0.0) {
                    g2.rotate(region.playback_rotation,
                            region.playback_x1 + (region.playback_x2 - region.playback_x1) * region.center_rotation_x,
                            region.playback_y1 + (region.playback_y2 - region.playback_y1) * region.center_rotation_y);
                }
                if (region.parent != null) {
                    g2.translate(-region.parent.offset_x, -region.parent.offset_y);
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

            if (SketchletEditor.editorPanel != null && SketchletEditor.editorPanel.sketchToolbar != null) {
                if (SketchletEditor.editorPanel.sketchToolbar.bVisualizeInfoRegions) {
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
                if (SketchletEditor.editorPanel == null) {
                    return;
                }
                Point p = region.getInversePoint(false, (int) (FileDrop.mouseX / SketchletEditor.editorPanel.scale), (int) (FileDrop.mouseY / SketchletEditor.editorPanel.scale));
                bInDrag = region.mouseHandler.inRect((int) (FileDrop.mouseX / SketchletEditor.editorPanel.scale), (int) (FileDrop.mouseY / SketchletEditor.editorPanel.scale), false);

                Rectangle r_properties = region.getPropertiesIconRectangle();
                Rectangle r_mouse = region.getMouseIconRectangle();
                Rectangle r_motion = region.getMappingIconRectangle();
                Rectangle r_overlap = region.getInRegionsIconRectangle();

                double sc = Math.min(1.0, SketchletEditor.editorPanel.scale);
                int iconSize = (int) (24 / sc);
                int iconOffset = (int) (4 / sc);
                int rectMargin = (int) (2 / sc);

                if ((mode == EditorMode.ACTIONS && (FileDrop.bDragging)) || bInDrag) {
                    if (this.propertiesIcon != null) {
                        g2.drawImage(propertiesIcon, r_properties.x + iconOffset + 1, r_properties.y + iconOffset + 1, iconSize, iconSize, null);
                    }
                    if (moveRotateIcon != null && Profiles.isActive("active_region_move")) {
                        g2.drawImage(moveRotateIcon, r_motion.x + iconOffset + 1, r_motion.y + iconOffset + 1, iconSize, iconSize, null);
                    }
                    if (mouseIcon != null && Profiles.isActive("active_region_mouse")) {
                        g2.drawImage(mouseIcon, r_mouse.x + iconOffset + 1, r_mouse.y + iconOffset + 1, iconSize, iconSize, null);
                    }
                    if (regionsIcon != null && Profiles.isActive("active_region_overlap")) {
                        g2.drawImage(regionsIcon, r_overlap.x + iconOffset + 1, r_overlap.y + iconOffset + 1, iconSize, iconSize, null);
                    }
                }

                if (mode == EditorMode.ACTIONS || bInDrag) {
                    selected = region.parent.selectedRegions != null && region.parent.selectedRegions.contains(region);
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
                    if (region.isInMappingIconArea(p.x, p.y) && FileDrop.currentString.startsWith("=")) {
                        g2.fillRoundRect(r_motion.x + rectMargin, r_motion.y + rectMargin, r_motion.width - rectMargin, r_motion.height - rectMargin, rectMargin * 6, rectMargin * 6);
                        g2.drawImage(moveRotateIcon, r_motion.x + iconOffset + 1, r_motion.y + iconOffset + 1, iconSize, iconSize, null);
                        g2.setColor(new Color(100, 100, 100));
                        g2.drawRoundRect(r_motion.x + 2, r_motion.y + rectMargin, r_motion.width - rectMargin, r_motion.height - rectMargin, rectMargin * 6, rectMargin * 6);
                        g2.drawString("motion and rotation", r_motion.x + rectMargin, r_motion.y - 6);
                        g2.setColor(new Color(255, 255, 255));
                    } else if (region.isInMouseIconArea(p.x, p.y)) {
                        g2.fillRoundRect(r_mouse.x + rectMargin, r_mouse.y + rectMargin, r_mouse.width - rectMargin, r_mouse.height - rectMargin, rectMargin * 6, rectMargin * 6);
                        g2.drawImage(mouseIcon, r_mouse.x + iconOffset + 1, r_mouse.y + iconOffset + 1, iconSize, iconSize, null);
                        g2.setColor(new Color(100, 100, 100));
                        g2.drawRoundRect(r_mouse.x + 2, r_mouse.y + rectMargin, r_mouse.width - rectMargin, r_mouse.height - rectMargin, rectMargin * 6, rectMargin * 6);
                        g2.drawString("mouse events (click, press, release, double ckick...)", r_mouse.x + rectMargin, r_mouse.y - 6);
                        g2.setColor(new Color(255, 255, 255));
                    } else if (region.isInRegionsIconArea(p.x, p.y)) {
                        g2.fillRoundRect(r_overlap.x + rectMargin, r_overlap.y + rectMargin, r_overlap.width - rectMargin, r_overlap.height - rectMargin, rectMargin * 6, rectMargin * 6);
                        g2.drawImage(regionsIcon, r_overlap.x + iconOffset + 1, r_overlap.y + iconOffset + 1, iconSize, iconSize, null);
                        g2.setColor(new Color(100, 100, 100));
                        g2.drawRoundRect(r_overlap.x + 2, r_overlap.y + rectMargin, r_overlap.width - rectMargin, r_overlap.height - rectMargin, rectMargin * 6, rectMargin * 6);
                        g2.drawString("regions overlap events", r_overlap.x + rectMargin, r_overlap.y - 6);
                        g2.setColor(new Color(255, 255, 255));
                    } else if (FileDrop.currentString.startsWith("=")) {
                        g2.fillRoundRect(r_properties.x + rectMargin, r_properties.y + rectMargin, r_properties.width - rectMargin, r_properties.height - rectMargin, rectMargin * 6, rectMargin * 6);
                        g2.drawImage(propertiesIcon, r_properties.x + iconOffset + 1, r_properties.y + iconOffset + 1, iconSize, iconSize, null);
                        g2.setColor(new Color(100, 100, 100));
                        g2.drawRoundRect(r_properties.x + 2, r_properties.y + rectMargin, r_properties.width - rectMargin, r_properties.height - rectMargin, rectMargin * 6, rectMargin * 6);
                        g2.drawString("region parameters", r_properties.x + rectMargin, r_properties.y - 6);
                        g2.setColor(new Color(255, 255, 255));
                    }
                }

                if (mode == EditorMode.ACTIONS) {
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.drawRect(_x1, _y1, w, h);
                    Stroke stroke = g2.getStroke();
                    g2.setStroke(new BasicStroke(1));

                    int _cornerSize = (int) (cornerSize / SketchletEditor.editorPanel.scale);

                    if (selected) {
                        if (SketchletEditor.editorPanel.bDragging && !this.region.mouseHandler.bRotating) {
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

                        if (!region.strTrajectory1.isEmpty()) {
                            g2.setColor(Color.WHITE);
                            g2.fillOval(_x1 - _cornerSize / 2 - 1 + (int) (w * region.trajectory2_x), _y1 - _cornerSize / 2 - 1 + (int) (h * region.trajectory2_y), _cornerSize + 2, _cornerSize + 2);
                            g2.setColor(new Color(0, 0, 255, 100));
                            g2.fillOval(_x1 - _cornerSize / 2 + (int) (w * region.trajectory2_x), _y1 - _cornerSize / 2 + (int) (h * region.trajectory2_y), _cornerSize, _cornerSize);
                        }

                        if (!SketchletEditor.editorPanel.inShiftMode) {
                            g2.setColor(Color.WHITE);
                            g2.fillOval(_x1 - _cornerSize / 2 + (int) (w * region.center_rotation_x), _y1 - _cornerSize / 2 + (int) (h * region.center_rotation_y), _cornerSize, _cornerSize);
                            g2.setColor(Color.GRAY);
                            g2.drawOval(_x1 - _cornerSize / 2 - 1 + (int) (w * region.center_rotation_x), _y1 - _cornerSize / 2 - 1 + (int) (h * region.center_rotation_y), _cornerSize + 2, _cornerSize + 2);
                        }
                        if (!SketchletEditor.editorPanel.inShiftMode && region.mouseHandler.selectedCorner == ActiveRegionMouseHandler.CENTER_ROTATION) {
                            int __x = _x1 - _cornerSize / 2 - 1 + (int) (w * region.mouseHandler.centerRotationX);
                            int __y = _y1 - _cornerSize / 2 - 1 + (int) (h * region.mouseHandler.centerRotationY);
                            g2.drawOval(__x, __y, _cornerSize + 2, _cornerSize + 2);
                            g2.drawString("rotation center", __x, __y + 20);
                        } else if (SketchletEditor.editorPanel.inCtrlMode && SketchletEditor.editorPanel.inShiftMode) {
                            if (region.mouseHandler.selectedCorner == ActiveRegionMouseHandler.TRAJECTORY2_POINT) {
                                int __x = _x1 - _cornerSize / 2 - 1 + (int) (w * region.mouseHandler.centerRotationX);
                                int __y = _y1 - _cornerSize / 2 - 1 + (int) (h * region.mouseHandler.centerRotationY);
                                g2.drawOval(__x, __y, _cornerSize + 2, _cornerSize + 2);
                                __x = _x1 - _cornerSize / 2 - 1 + (int) (w * region.mouseHandler.trajectory2X);
                                __y = _y1 - _cornerSize / 2 - 1 + (int) (h * region.mouseHandler.trajectory2Y);
                                g2.setColor(Color.WHITE);
                                g2.drawOval(__x, __y, _cornerSize + 2, _cornerSize + 2);
                                g2.setColor(Color.GRAY);
                                g2.drawString("trajectory point 2", __x, __y + 20);
                                g2.setColor(Color.WHITE);
                                int __x1 = _x1 + (int) (w * region.center_rotation_x);
                                int __y1 = _y1 + (int) (h * region.center_rotation_y);
                                int __x2 = _x1 + (int) (w * region.mouseHandler.trajectory2X);
                                int __y2 = _y1 + (int) (h * region.mouseHandler.trajectory2Y);
                                g2.drawLine(__x1, __y1, __x2, __y2);
                            }
                        }

                        if (this.region.mouseHandler.bRotating && SketchletEditor.editorPanel.bDragging) {
                            double circelSize = 60 / SketchletEditor.editorPanel.scale;
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
                    g2.drawString(region.strName, _x1 + w / 2 - 2, _y1 + h / 2 + 17);
                }
            }

            g2.setTransform(oldTransform);

            if (!bPlayback) {
                this.trajectoryDrawingLayer.draw(g2, component, bPlayback);
                auxiliaryDrawingLayer.drawLimits(g2, component, mode);
            } else if (bHighlightRegions) {
                auxiliaryDrawingLayer.drawHighlight(g2);
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
            region.setFromPlayback();
        }
        try {
            region.strHAlign = region.processText(region.strHAlign);
            region.strVAlign = region.processText(region.strVAlign);

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

                    if (region.strHAlign.equalsIgnoreCase("center")) {
                        x -= w / 2;
                    } else if (region.strHAlign.equalsIgnoreCase("right")) {
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
                    double limitsX[] = region.limitsHandler.getLimits("position x", 0, w);
                    int x = (int) InteractionSpace.getSketchX(limitsX[0] + (limitsX[1] - limitsX[0]) * Math.min(1.0, Double.parseDouble(strRelX1)));
                    if (region.strHAlign.equalsIgnoreCase("center")) {
                        x -= w / 2;
                    } else if (region.strHAlign.equalsIgnoreCase("right")) {
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
                    if (region.strVAlign.equalsIgnoreCase("center")) {
                        y -= h / 2;
                    } else if (region.strVAlign.equalsIgnoreCase("bottom")) {
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
                    double limitsY[] = region.limitsHandler.getLimits("position y", 0, h);
                    int y = (int) InteractionSpace.getSketchY(limitsY[0] + (limitsY[1] - limitsY[0]) * Math.min(1.0, Double.parseDouble(strRelY1)));
                    if (region.strVAlign.equalsIgnoreCase("center")) {
                        y -= h / 2;
                    } else if (region.strVAlign.equalsIgnoreCase("bottom")) {
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
                    if (region.strHAlign.equalsIgnoreCase("center")) {
                        region.x1 = centerX - newW / 2;
                        region.x2 = centerX + newW / 2;
                    } else if (region.strHAlign.equalsIgnoreCase("right")) {
                        region.x1 = region.x2 - newW;
                    } else {
                        region.x2 = region.x1 + newW;
                    }
                    w = newW;
                    //region.processLimitsX();
                } catch (Throwable e) {
                    //log.error(e);
                }
            }
            if (!strHeight.isEmpty()) {
                try {
                    int oldH = region.y2 - region.y1;
                    int centerY = region.y1 + oldH / 2;
                    int newH = (int) InteractionSpace.getSketchHeight(Double.parseDouble(strHeight));
                    if (region.strVAlign.equalsIgnoreCase("center")) {
                        region.y1 = centerY - newH / 2;
                        region.y2 = centerY + newH / 2;
                    } else if (region.strVAlign.equalsIgnoreCase("bottom")) {
                        region.y1 = region.y2 - newH;
                    } else {
                        region.y2 = region.y1 + newH;
                    }
                    h = newH;
                    //region.processLimitsY();
                } catch (Throwable e) {
                    //log.error(e);
                }
            }

            if (!region.inTrajectoryMode && !region.inTrajectoryMode2 && !region.strTrajectory1.trim().isEmpty()) {
                if (!strTrajectoryPosition.isEmpty() && region.bStickToTrajectory) {
                    try {
                        double pos = Double.parseDouble(strTrajectoryPosition);
                        if (!Double.isNaN(pos)) {
                            Point p = trajectoryDrawingLayer.getTrajectoryPoint(pos);
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
                                if (region.bOrientationTrajectory) {
                                    region.rotation = trajectoryDrawingLayer.trajectoryOrientationFromPoint;
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
                region.interactionHandler.processInteractionEvents(bPlayback, region.parent.page.activeTimers, region.parent.page.activeMacros);
                region.getSketch().updateConnectors(region, bPlayback);
            }

            if (region != null && region.interactionHandler != null) {
                if (!region.isWithinLimits(bPlayback) || region.interactionHandler.intersectsWithSolids(bPlayback)) {
                    ActiveRegions.findNonOverlapingLocation(region);
                    region.rotation = prevRotation;
                    region.shearX = prevShearX;
                    region.shearY = prevShearY;
                } else {
                    if (!region.regionGrouping.isEmpty()) {
                        for (ActiveRegion as : region.parent.regions) {
                            if (as != region && as.regionGrouping.equals(region.regionGrouping)) {
                                if (bPlayback) {
                                    as.setFromPlayback();
                                    as.x1 += region.x1 - prevX1;
                                    as.y1 += region.y1 - prevY1;
                                    as.x2 += region.x2 - prevX2;
                                    as.y2 += region.y2 - prevY2;
                                    if (bProcessLimits) {
                                        as.limitsHandler.processLimits("position x", as.x1, 0, 0, true);
                                        as.limitsHandler.processLimits("position y", as.y1, 0, 0, true);
                                    }
                                    as.resetFromPlayback();
                                } else {
                                    if (region.x1 - prevX1 != 0 || region.y1 - prevY1 != 0) {
                                        as.x1 += region.x1 - prevX1;
                                        as.y1 += region.y1 - prevY1;
                                        as.x2 += region.x2 - prevX2;
                                        as.y2 += region.y2 - prevY2;
                                        if (bProcessLimits) {
                                            as.limitsHandler.processLimits("position x", as.x1, 0, 0, true);
                                            as.limitsHandler.processLimits("position y", as.y1, 0, 0, true);
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

        if (region.bCaptureScreen) {
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

        if (this.textImageLayer.isFontChanged()) {
            return true;
        }

        String strImageProperties[] = {
                region.strTransparency,
                region.strImageIndex,
                region.strImageUrlField,
                region.strText,
                region.strTextField,
                //region.strTextFile,
                region.shape,
                region.strLineColor,
                region.strFillColor,
                region.strLineThickness,
                region.strLineStyle,
                //region.chartSpecification.getText(),
                //region.svgSpecification.getText(),
                //region.htmlSpecification.getText(),
                region.strEmbeddedSketch,
                region.strHAlign,
                region.strVAlign,
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
                region.setFromPlayback();
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
                    auxiliaryDrawingLayer.drawPen();
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

                shapeImageLayer.draw(g2, component, bPlayback, true, false);
                drawnImageLayer.init();
                widgetImageLayer.draw(g2, component, bPlayback);
                if (widgetImageLayer != null && widgetImageLayer.widgetControl == null && imageDrawingLayer != null && drawnImageLayer != null) {
                    imageDrawingLayer.draw(g2, component, bPlayback);
                    for (int di = 0; di < indexes.length; di++) {
                        drawnImageLayer.setIndex(indexes[di]);
                        drawnImageLayer.draw(g2, component, bPlayback);
                    }
                    textImageLayer.draw(g2, component, bPlayback);
                }
                shapeImageLayer.draw(g2, component, bPlayback, false, true);
                g2.setComposite(oldComposite);
                g2.setTransform(oldTransform);
                g2.dispose();
            }

            perspective.doPerspectiveAndDrawImage(g, buffer);

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
        boolean selected = bPlayback ? false : SketchletEditor.editorPanel.mode == EditorMode.ACTIONS && region.parent.selectedRegions != null && region.parent.selectedRegions.contains(region);
        Vector<String> info = DrawVariables.getRegionInfo(region, selected);

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
}
