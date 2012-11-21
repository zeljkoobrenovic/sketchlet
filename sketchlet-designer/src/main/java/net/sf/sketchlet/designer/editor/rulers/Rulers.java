/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.rulers;

import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.SketchletEditorMode;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.model.ActiveRegion;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class Rulers {

    boolean bDragging = false;

    public Rulers() {
    }

    public boolean isDragging() {
        return this.bDragging;
    }

    public void drawRulers(Graphics2D g2) {
        if (SketchletEditor.getInstance().getMode() != SketchletEditorMode.ACTIONS) {
            return;
        }
        g2.setPaint(Color.LIGHT_GRAY);
        if (SketchletEditor.getInstance().getMarginX() > 0 || SketchletEditor.getInstance().getMarginY() > 0) {
            int w = SketchletEditor.getInstance().getWidth();
            int h = SketchletEditor.getInstance().getHeight();
            /*if (SketchletEditor.editorPanel.marginX > 0) {
                for (int i = SketchletEditor.editorPanel.marginX; i < w + SketchletEditor.editorPanel.marginX; i += 10) {
                    int x1 = i;
                    int x2 = x1;
                    int y1 = 0;
                    int y2 = 5;
                    if (i % 20 == 0) {
                        y2 += 3;
                    }

                    g2.drawLine(x1, y1, x2, y2);
                }
                g2.setColor(Color.YELLOW);
                g2.drawLine(SketchletEditor.editorPanel.statusX + SketchletEditor.editorPanel.marginX, 0, SketchletEditor.editorPanel.statusX + SketchletEditor.editorPanel.marginX, 8);
            }*/
            /*if (SketchletEditor.editorPanel.marginY > 0) {*/
            for (int i = SketchletEditor.getInstance().getMarginY(); i < h + SketchletEditor.getInstance().getMarginY(); i += 10) {
                int x1 = 0;
                int x2 = 5;
                if (i % 20 == 0) {
                    x2 += 3;
                }
                int y1 = i;
                int y2 = y1;

                g2.drawLine(x1, y1, x2, y2);
            }
            g2.setColor(Color.YELLOW);
            g2.drawLine(0, SketchletEditor.getInstance().getStatusY() + SketchletEditor.getInstance().getMarginY(), 8, SketchletEditor.getInstance().getStatusY() + SketchletEditor.getInstance().getMarginY());
            /*}*/
        }

        if (SketchletEditor.getInstance().getCurrentPage().getRegions().getSelectedRegions() != null && SketchletEditor.getInstance().getCurrentPage().getRegions().getSelectedRegions().size() == 1) {
            ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getSelectedRegions().firstElement();
            calculateLimits(region);

            g2.setColor(Color.WHITE);
            if (p1 != null) {
                g2.fillPolygon(p1);
            }
            if (p2 != null) {
                g2.fillPolygon(p2);
            }
            if (p3 != null) {
                g2.fillPolygon(p3);
            }
            if (p4 != null) {
                g2.fillPolygon(p4);
            }
            g2.setColor(Color.BLACK);
            if (p1 != null) {
                g2.drawPolygon(p1);
            }
            if (p2 != null) {
                g2.drawPolygon(p2);
            }
            if (p3 != null) {
                g2.drawPolygon(p3);
            }
            if (p4 != null) {
                g2.drawPolygon(p4);
            }
        }
    }

    int l_x1, l_y1, l_x2, l_y2;
    Polygon p1, p2, p3, p4;

    public void calculateLimits(ActiveRegion region) {
        String strX1 = (String) region.limits[0][1];
        String strX2 = (String) region.limits[0][2];
        String strY1 = (String) region.limits[1][1];
        String strY2 = (String) region.limits[1][2];
        int _x1 = 0;
        int _y1 = 0;
        BufferedImage img = SketchletEditor.getInstance().getCurrentPage().getImages()[0];
        int _x2 = img != null ? img.getWidth() : 2000;
        int _y2 = img != null ? img.getHeight() : 2000;
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
            }
        } catch (Exception e) {
        }
        try {
            if (strY2.length() > 0) {
                _y2 = (int) Double.parseDouble(strY2);
            }
        } catch (Exception e) {
        }

        _x1 = (int) InteractionSpace.getSketchX(_x1);
        _y1 = (int) InteractionSpace.getSketchY(_y1);
        _x2 = (int) InteractionSpace.getSketchX(_x2);
        _y2 = (int) InteractionSpace.getSketchY(_y2);

        l_x1 = Math.min(_x1, _x2);
        l_y1 = Math.min(_y1, _y2);
        l_x2 = Math.max(_x1, _x2);
        l_y2 = Math.max(_y1, _y2);

        int mx = SketchletEditor.getInstance().getMarginX();
        int my = SketchletEditor.getInstance().getMarginY();

        p1 = new Polygon();
        p1.addPoint(mx + l_x1 - 5, 0);
        p1.addPoint(mx + l_x1 + 5, 0);
        p1.addPoint(mx + l_x1 + 5, 5);
        p1.addPoint(mx + l_x1, 10);
        p1.addPoint(mx + l_x1 - 5, 5);

        p2 = new Polygon();
        p2.addPoint(mx + l_x2 - 5, 0);
        p2.addPoint(mx + l_x2 + 5, 0);
        p2.addPoint(mx + l_x2 + 5, 5);
        p2.addPoint(mx + l_x2, 10);
        p2.addPoint(mx + l_x2 - 5, 5);

        p3 = new Polygon();
        p3.addPoint(0, my + l_y1 - 5);
        p3.addPoint(0, my + l_y1 + 5);
        p3.addPoint(5, my + l_y1 + 5);
        p3.addPoint(10, my + l_y1);
        p3.addPoint(5, my + l_y1 - 5);

        p4 = new Polygon();
        p4.addPoint(0, my + l_y2 - 5);
        p4.addPoint(0, my + l_y2 + 5);
        p4.addPoint(5, my + l_y2 + 5);
        p4.addPoint(10, my + l_y2);
        p4.addPoint(5, my + l_y2 - 5);
    }

    public boolean isIn(int x, int y) {
        return x < SketchletEditor.getInstance().getMarginX() || y < SketchletEditor.getInstance().getMarginY();
    }

    int startX, startY;
    int selectedLimit = -1;
    ActiveRegion selectedRegion = null;

    public void mousePressed(int x, int y) {
        SketchletEditor.getInstance().getCurrentPage().getRegions().setSelectedRegions(new Vector<ActiveRegion>());
        SketchletEditor.getInstance().getCurrentPage().getRegions().defocusAllRegions();
        selectedLimit = -1;
        if (SketchletEditor.getInstance().getCurrentPage().getRegions().getSelectedRegions() != null && SketchletEditor.getInstance().getCurrentPage().getRegions().getSelectedRegions().size() == 1) {
            selectedRegion = SketchletEditor.getInstance().getCurrentPage().getRegions().getSelectedRegions().firstElement();
            if (p1.contains(x, y)) {
                selectedLimit = 1;
            } else if (p2.contains(x, y)) {
                selectedLimit = 2;
            } else if (p3.contains(x, y)) {
                selectedLimit = 3;
            } else if (p4.contains(x, y)) {
                selectedLimit = 4;
            }

            this.bDragging = true;
        } else {
            this.bDragging = false;
            selectedRegion = null;
        }
    }

    public void mouseReleased(int x, int y) {
        this.bDragging = false;
    }

    public void mouseDragged(int x, int y) {
        if (selectedRegion == null) {
            return;
        }

        x -= SketchletEditor.getInstance().getMarginX();
        y -= SketchletEditor.getInstance().getMarginY();

        int w = SketchletEditor.getInstance().getCurrentPage().getImages() != null ? SketchletEditor.getInstance().getCurrentPage().getImages()[0].getWidth() : 2000;
        int h = SketchletEditor.getInstance().getCurrentPage().getImages() != null ? SketchletEditor.getInstance().getCurrentPage().getImages()[0].getHeight() : 2000;

        Rectangle rect;

        if (SketchletEditor.getInstance().getCurrentPage().getRegions().getSelectedRegions() != null && SketchletEditor.getInstance().getCurrentPage().getRegions().getSelectedRegions().size() == 1) {
            ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getSelectedRegions().firstElement();
            rect = region.getBounds(false);
            y = Math.max(y, 0);
            y = Math.min(y, h);
        } else {
            return;
        }

        switch (selectedLimit) {
            case 1:
                x = Math.min(x, (int) rect.getX());
                x = Math.max(x, 0);
                selectedRegion.limits[0][1] = "" + InteractionSpace.getPhysicalX(x);
                break;
            case 2:
                x = Math.max(x, (int) (rect.getX() + rect.getWidth()));
                x = Math.min(x, w);
                selectedRegion.limits[0][2] = "" + InteractionSpace.getPhysicalX(x);
                break;
            case 3:
                y = Math.min(y, (int) rect.getY());
                y = Math.max(y, 0);
                selectedRegion.limits[1][1] = "" + InteractionSpace.getPhysicalY(y);
                break;
            case 4:
                y = Math.max(y, (int) (rect.getY() + rect.getHeight()));
                y = Math.min(y, h);
                selectedRegion.limits[1][2] = "" + InteractionSpace.getPhysicalY(y);
                break;
        }


        SketchletEditor.getInstance().repaint();
    }
}
