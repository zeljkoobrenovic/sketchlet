/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.regions.renderer;

import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.editor.EditorMode;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.designer.ui.playback.PlaybackFrame;
import net.sf.sketchlet.util.Colors;

import java.awt.*;
import java.awt.geom.Line2D;

/**
 * @author zobrenovic
 */
public class AuxiliaryDrawingLayer extends DrawingLayer {

    public AuxiliaryDrawingLayer(ActiveRegion region) {
        super(region);
    }

    public void dispose() {
        region = null;
    }

    public void draw(Graphics2D g2, Component component, boolean bPlayback) {
    }

    public void drawLimits(Graphics2D g2, Component component, EditorMode mode) {
        if (mode == EditorMode.ACTIONS) {
            String strX1 = (String) region.limits[0][1];
            String strX2 = (String) region.limits[0][2];
            String strY1 = (String) region.limits[1][1];
            String strY2 = (String) region.limits[1][2];
            g2.setColor(new Color(255, 0, 0, 70));
            float dash1[] = {10.0f};
            float thick = region.parent.selectedRegions != null && region.parent.selectedRegions.contains(region) ? 3.0f : 1.0f;
            BasicStroke dashed = new BasicStroke(thick,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER,
                    10.0f, dash1, 0.0f);

            g2.setStroke(dashed);

            if (strX1.length() > 0 || strY1.length() > 0 || strX2.length() > 0 || strY2.length() > 0) {
                int _x1 = 0;
                int _y1 = 0;
                int _x2 = 2000;
                int _y2 = 2000;
                try {
                    if (strX1.length() > 0) {
                        _x1 = (int) Double.parseDouble(strX1);
                    }
                } catch (Exception e) {
                }
                try {
                    if (strY1.length() > 0) {
                        _y1 = (int) Double.parseDouble(strY1);
                    }
                } catch (Exception e) {
                }
                try {
                    if (strX2.length() > 0) {
                        _x2 = (int) Double.parseDouble(strX2);
                    } else {
                        _x2 = SketchletEditor.editorPanel.getSketchWidth();
                    }
                } catch (Exception e) {
                }
                try {
                    if (strY2.length() > 0) {
                        _y2 = (int) Double.parseDouble(strY2);
                    } else {
                        _y2 = SketchletEditor.editorPanel.getSketchHeight();
                    }
                } catch (Exception e) {
                }

                _x1 = (int) InteractionSpace.getSketchX(_x1);
                _y1 = (int) InteractionSpace.getSketchY(_y1);
                _x2 = (int) InteractionSpace.getSketchX(_x2);
                _y2 = (int) InteractionSpace.getSketchY(_y2);

                g2.drawRect(Math.min(_x1, _x2), Math.min(_y1, _y2), Math.abs(_x2 - _x1), Math.abs(_y2 - _y1));
            }
        }
    }

    public void drawPen() {
        try {
            String strHAlign = region.processText(region.strHAlign);
            String strVAlign = region.processText(region.strVAlign);
            int x, y;

            int w = region.playback_x2 - region.playback_x1;
            int h = region.playback_y2 - region.playback_y1;

            x = region.playback_x1 + (int) (w * region.center_rotation_x);
            y = region.playback_y1 + (int) (h * region.center_rotation_y);

            String strPen = region.processText((region.strPen).trim());
            if (strPen.equals("")) {
                return;
            }

            float thickness = (float) Double.parseDouble(strPen);
            if (thickness > 0) {
                if (region.pen_x > 0 && region.pen_y > 0) {
                    Color lineColor = Colors.getColor(region.strLineColor);
                    if (lineColor == null) {
                        lineColor = Color.BLACK;
                    }

                    if (PlaybackFrame.playbackFrame != null && PlaybackFrame.playbackFrame.length > 0) {
                        for (int i = 0; i < PlaybackFrame.playbackFrame.length; i++) {
                            Graphics2D g2 = (Graphics2D) PlaybackFrame.playbackFrame[i].playbackPanel.currentPage.images[0].createGraphics();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setPaint(lineColor);
                            BasicStroke stroke = new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
                            g2.setStroke(stroke);
                            g2.draw(new Line2D.Double(new Point(region.pen_x, region.pen_y), new Point(x, y)));
                            g2.dispose();
                            PlaybackFrame.playbackFrame[i].playbackPanel.repaint();
                        }
                    } else if (SketchletEditor.editorPanel.internalPlaybackPanel != null) {
                        Graphics2D g2 = (Graphics2D) SketchletEditor.editorPanel.internalPlaybackPanel.currentPage.images[0].createGraphics();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setPaint(lineColor);
                        BasicStroke stroke = new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
                        g2.setStroke(stroke);
                        g2.draw(new Line2D.Double(new Point(region.pen_x, region.pen_y), new Point(x, y)));
                        g2.dispose();
                        SketchletEditor.editorPanel.internalPlaybackPanel.repaint();
                    }
                }

            }
            region.pen_x = x;
            region.pen_y = y;
        } catch (Exception e) {
        }
    }

    public void drawHighlight(Graphics2D g2) {
        g2.setColor(new Color(255, 255, 0, 100));
        int w, h;

        w = region.playback_x2 - region.playback_x1;
        h = region.playback_y2 - region.playback_y1;

        g2.fillRect(region.playback_x1, region.playback_y1, w, h);
        g2.setColor(new Color(255, 255, 0, 255));
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(region.playback_x1, region.playback_y1, w, h);
    }
}
