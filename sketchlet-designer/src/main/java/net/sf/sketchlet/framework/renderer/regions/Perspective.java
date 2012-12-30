package net.sf.sketchlet.framework.renderer.regions;

import net.sf.sketchlet.common.filter.PerspectiveFilter;
import net.sf.sketchlet.common.geom.GraphicsTools;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.framework.model.ActiveRegion;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author zobrenovic
 */
public class Perspective {

    private PerspectiveFilter perspectiveFilter = new PerspectiveFilter();
    private ActiveRegion region;
    private int minX, minY;

    Perspective(ActiveRegion region) {
        this.region = region;
    }

    public void dispose() {
        perspectiveFilter = null;
        region = null;
    }

    BufferedImage buffer2;

    public void doPerspectiveAndDrawImage(Graphics2D g, BufferedImage buffer) {
        ActiveRegion region = this.region;
        if (region == null) {
            return;
        }
        minX = 0;
        minY = 0;
        double p_x0 = region.getP_x0();
        double p_y0 = region.getP_y0();
        double p_x1 = region.getP_x1();
        double p_y1 = region.getP_y1();
        double p_x2 = region.getP_x2();
        double p_y2 = region.getP_y2();
        double p_x3 = region.getP_x3();
        double p_y3 = region.getP_y3();

        int w = region.getWidthValue();
        int h = region.getHeightValue();
        double h_x1;
        double h_x2;
        double h_y1;
        double h_y2;

        if (region.getParent() != null) {
            region.getParent().getPage().calculateHorizonPoint();
            h_x1 = region.getParent().getPage().getPerspective_horizont_x1();
            h_x2 = region.getParent().getPage().getPerspective_horizont_x2();
            h_y1 = region.getParent().getPage().getPerspective_horizont_y();
            h_y2 = region.getParent().getPage().getPerspective_horizont_y();
        } else {
            h_x1 = 500;
            h_x2 = 1000;
            h_y1 = 500.0;
            h_y2 = 500.0;
        }

        Point pt1 = region.getInversePoint(false, (int) h_x1, (int) h_y1);
        Point pt2 = region.getInversePoint(false, (int) h_x2, (int) h_y2);

        h_x1 = pt1.getX();
        h_x2 = pt2.getX();
        h_y1 = pt1.getY();
        h_y2 = pt2.getY();

        double h_x = h_x1;
        double h_y = h_y1;

        if (!region.processText(region.getRotation3DHorizontal()).isEmpty()) {
            try {
                double r = Math.toRadians(Double.parseDouble(region.processText(region.getRotation3DHorizontal())));
                double k = 0.2;
                double rel_x = 0;

                while (r < 0) {
                    r += Math.PI * 2;
                }
                while (r > Math.PI * 2) {
                    r -= Math.PI * 2;
                }

                if (r <= Math.PI / 2) {
                    rel_x = Math.sin(r) / 2;
                } else if (r <= Math.PI) {
                    rel_x = 1 - Math.sin(r) / 2;
                } else if (r <= 3 * Math.PI / 2) {
                    rel_x = 1 + Math.sin(r) / 2;
                } else if (r < 2 * Math.PI) {
                    rel_x = -Math.sin(r) / 2;
                }

                double s = Math.sin(r) * k;

                p_x0 = rel_x;
                p_y0 = -s;

                p_x1 = 1 - rel_x;
                p_y1 = s;

                p_x2 = 1 - rel_x;
                p_y2 = 1 - s;

                p_x3 = rel_x;
                p_y3 = 1 + s;

            } catch (Exception e) {
            }
        } else if (!region.processText(region.getRotation3DVertical()).isEmpty()) {
            try {
                double r = Math.toRadians(Double.parseDouble(region.processText(region.getRotation3DVertical())));
                double k = 0.2;
                double rel_y = 0;
                while (r < 0) {
                    r += Math.PI * 2;
                }
                while (r > Math.PI * 2) {
                    r -= Math.PI * 2;
                }
                if (r <= Math.PI / 2) {
                    rel_y = Math.sin(r) / 2;
                } else if (r <= Math.PI) {
                    rel_y = 1 - Math.sin(r) / 2;
                } else if (r <= 3 * Math.PI / 2) {
                    rel_y = 1 + Math.sin(r) / 2;
                } else if (r < 2 * Math.PI) {
                    rel_y = -Math.sin(r) / 2;
                }
                double s = Math.sin(r) * k;

                p_x0 = -s;
                p_y0 = rel_y;

                p_x1 = 1 + s;
                p_y1 = rel_y;

                p_x2 = 1 - s;
                p_y2 = 1 - rel_y;

                p_x3 = s;
                p_y3 = 1 - rel_y;

            } catch (Exception e) {
            }
        } else if (!region.getAutomaticPerspective().isEmpty()) {
            String type = region.getParent().getPage().getPropertyValue("perspective type");

            g.setColor(new Color(100, 100, 100, 100));
            g.setStroke(new BasicStroke(1));

            if (region.getAutomaticPerspective().equalsIgnoreCase("front")) {
                if (type.equalsIgnoreCase("2 points")) {
                    h_x = h_x1;
                    h_y = h_y1;
                } else {
                    h_x = h_x1;
                    h_y = h_y1;
                }

                if (SketchletEditor.getInstance().isShowPerspectiveLines() || SketchletEditor.getInstance().getPerspectivePanel().showPerspectiveGrid.isSelected()) {
                    g.drawLine(region.getX1Value(), region.getY1Value(), (int) h_x, (int) h_y);
                    g.drawLine(region.getX1Value(), region.getY2Value(), (int) h_x, (int) h_y);
                    g.drawLine(region.getX2Value(), region.getY1Value(), (int) h_x, (int) h_y);
                    g.drawLine(region.getX2Value(), region.getY2Value(), (int) h_x, (int) h_y);
                }

            } else if (region.getAutomaticPerspective().equalsIgnoreCase("left")) {
                if (type.equalsIgnoreCase("2 points")) {
                    h_x = h_x2;
                    h_y = h_y2;
                } else {
                    h_x = h_x1;
                    h_y = h_y1;
                }
                double rel_x = Math.tanh((h_x - region.getX1Value()) / 1000);
                double x2 = region.getX1Value() + w * rel_x;

                double p1[] = GraphicsTools.findIntersection(region.getX1Value(), region.getY1Value(), h_x, h_y, x2, 0, x2, 5000);
                double p2[] = GraphicsTools.findIntersection(region.getX1Value(), region.getY2Value(), h_x, h_y, x2, 0, x2, 5000);

                if (p1 != null && p2 != null) {
                    p_x1 = rel_x;
                    p_y1 = (p1[1] - region.getY1Value()) / region.getHeightValue();
                    p_x2 = rel_x;
                    p_y2 = (p2[1] - region.getY1Value()) / region.getHeightValue();

                    if (p_y1 > p_y2) {
                        double t = p_y2;
                        p_y2 = p_y1;
                        p_y1 = t;
                    }
                } else {
                    return;
                }

                if (SketchletEditor.getInstance().isShowPerspectiveLines() || SketchletEditor.getInstance().getPerspectivePanel().showPerspectiveGrid.isSelected()) {
                    g.drawLine(region.getX1Value(), region.getY1Value(), (int) h_x, (int) h_y);
                    g.drawLine(region.getX1Value(), region.getY2Value(), (int) h_x, (int) h_y);
                }
            } else if (region.getAutomaticPerspective().equalsIgnoreCase("right")) {
                if (type.equalsIgnoreCase("2 points")) {
                    h_x = h_x1;
                    h_y = h_y1;
                } else {
                    h_x = h_x1;
                    h_y = h_y1;
                }
                double rel_x = Math.tanh((region.getX2Value() - h_x) / 1000);
                double x1 = region.getX2Value() - w * rel_x;

                double p1[] = GraphicsTools.findIntersection(region.getX1Value() + w * p_x1, region.getY1Value() + h * p_y1, h_x, h_y, x1, 0, x1, 5000);
                double p2[] = GraphicsTools.findIntersection(region.getX1Value() + w * p_x2, region.getY1Value() + h * p_y2, h_x, h_y, x1, 0, x1, 5000);

                if (p1 != null && p2 != null) {
                    p_x0 = 1 - rel_x;
                    p_y0 = (p1[1] - region.getY1Value()) / region.getHeightValue();
                    p_x3 = 1 - rel_x;
                    p_y3 = (p2[1] - region.getY1Value()) / region.getHeightValue();
                } else {
                    return;
                }
                if (SketchletEditor.getInstance().isShowPerspectiveLines() || SketchletEditor.getInstance().getPerspectivePanel().showPerspectiveGrid.isSelected()) {
                    g.drawLine(region.getX2Value(), region.getY1Value(), (int) h_x, (int) h_y);
                    g.drawLine(region.getX2Value(), region.getY2Value(), (int) h_x, (int) h_y);
                }
            } else if (region.getAutomaticPerspective().equalsIgnoreCase("top")) {
                if (type.equalsIgnoreCase("2 points")) {
                    h_x = h_x1;
                    h_y = h_y1;
                    double rel_y = Math.tanh((region.getY2Value() - h_y) / 1000);

                    double p1[] = GraphicsTools.findIntersection(region.getX1Value(), region.getY1Value(), h_x1, h_y1, region.getX2Value(), region.getY1Value(), h_x2, h_y2);
                    double p2[] = GraphicsTools.findIntersection(region.getX1Value(), region.getY1Value(), h_x2, h_y2, region.getX2Value(), region.getY1Value(), h_x1, h_y1);

                    if (p1 != null && p2 != null) {
                        p_x0 = 0;
                        p_y0 = 0;
                        p_x2 = 1;
                        p_y2 = 0;
                        p_x1 = (p1[0] - region.getX1Value()) / region.getWidthValue();
                        p_y1 = (p1[1] - region.getY1Value()) / region.getHeightValue();
                        p_x3 = (p2[0] - region.getX1Value()) / region.getWidthValue();
                        p_y3 = (p2[1] - region.getY1Value()) / region.getHeightValue();
                    } else {
                        return;
                    }
                    if (SketchletEditor.getInstance().isShowPerspectiveLines() || SketchletEditor.getInstance().getPerspectivePanel().showPerspectiveGrid.isSelected()) {
                        g.drawLine(region.getX1Value(), region.getY1Value(), (int) h_x1, (int) h_y1);
                        g.drawLine(region.getX2Value(), region.getY1Value(), (int) h_x1, (int) h_y1);
                        g.drawLine(region.getX1Value(), region.getY1Value(), (int) h_x2, (int) h_y2);
                        g.drawLine(region.getX2Value(), region.getY1Value(), (int) h_x2, (int) h_y2);
                    }
                } else {
                    h_x = h_x1;
                    h_y = h_y1;
                    double rel_y = Math.tanh((h_y - region.getY1Value()) / 1000);
                    double y2 = region.getY1Value() + h * rel_y;

                    double p1[] = GraphicsTools.findIntersection(region.getX1Value(), region.getY1Value(), h_x, h_y, 0, y2, 5000, y2);
                    double p2[] = GraphicsTools.findIntersection(region.getX2Value(), region.getY1Value(), h_x, h_y, 0, y2, 5000, y2);

                    if (p1 != null && p2 != null) {
                        p_x2 = (p2[0] - region.getX1Value()) / region.getWidthValue();
                        p_y2 = rel_y;
                        p_x3 = (p1[0] - region.getX1Value()) / region.getWidthValue();
                        p_y3 = rel_y;
                    } else {
                        return;
                    }
                    if (SketchletEditor.getInstance().isShowPerspectiveLines() || SketchletEditor.getInstance().getPerspectivePanel().showPerspectiveGrid.isSelected()) {
                        g.drawLine(region.getX1Value(), region.getY1Value(), (int) h_x, (int) h_y);
                        g.drawLine(region.getX2Value(), region.getY1Value(), (int) h_x, (int) h_y);
                    }
                }
            } else if (region.getAutomaticPerspective().equalsIgnoreCase("bottom")) {
                if (type.equalsIgnoreCase("2 points")) {
                    h_x = h_x1;
                    h_y = h_y1;
                    double rel_y = Math.tanh((region.getY2Value() - h_y) / 1000);
                    double y1 = region.getY2Value() - h * rel_y;

                    double p1[] = GraphicsTools.findIntersection(region.getX1Value(), region.getY2Value(), h_x1, h_y1, region.getX2Value(), region.getY2Value(), h_x2, h_y2);
                    double p2[] = GraphicsTools.findIntersection(region.getX1Value(), region.getY2Value(), h_x2, h_y2, region.getX2Value(), region.getY2Value(), h_x1, h_y1);

                    if (p1 != null && p2 != null) {
                        p_x0 = 0;
                        p_y0 = 1;
                        p_x2 = 1;
                        p_y2 = 1;
                        p_x1 = (p1[0] - region.getX1Value()) / region.getWidthValue();
                        p_y1 = (p1[1] - region.getY1Value()) / region.getHeightValue();
                        p_x3 = (p2[0] - region.getX1Value()) / region.getWidthValue();
                        p_y3 = (p2[1] - region.getY1Value()) / region.getHeightValue();
                    } else {
                        return;
                    }
                    if (SketchletEditor.getInstance().isShowPerspectiveLines() || SketchletEditor.getInstance().getPerspectivePanel().showPerspectiveGrid.isSelected()) {
                        g.drawLine(region.getX1Value(), region.getY2Value(), (int) h_x1, (int) h_y1);
                        g.drawLine(region.getX2Value(), region.getY2Value(), (int) h_x1, (int) h_y1);
                        g.drawLine(region.getX1Value(), region.getY2Value(), (int) h_x2, (int) h_y2);
                        g.drawLine(region.getX2Value(), region.getY2Value(), (int) h_x2, (int) h_y2);
                    }
                } else {
                    h_x = h_x1;
                    h_y = h_y1;
                    double rel_y = Math.tanh((region.getY2Value() - h_y) / 1000);
                    double y1 = region.getY2Value() - h * rel_y;

                    double p1[] = GraphicsTools.findIntersection(region.getX1Value(), region.getY2Value(), h_x, h_y, 0, y1, 5000, y1);
                    double p2[] = GraphicsTools.findIntersection(region.getX2Value(), region.getY2Value(), h_x, h_y, 0, y1, 5000, y1);

                    if (p1 != null && p2 != null) {
                        p_x0 = (p1[0] - region.getX1Value()) / region.getWidthValue();
                        p_y0 = 1 - rel_y;
                        p_x1 = (p2[0] - region.getX1Value()) / region.getWidthValue();
                        p_y1 = 1 - rel_y;
                    } else {
                        return;
                    }
                    if (SketchletEditor.getInstance().isShowPerspectiveLines() || SketchletEditor.getInstance().getPerspectivePanel().showPerspectiveGrid.isSelected()) {
                        g.drawLine(region.getX1Value(), region.getY2Value(), (int) h_x, (int) h_y);
                        g.drawLine(region.getX2Value(), region.getY2Value(), (int) h_x, (int) h_y);
                    }
                }
            }
        } else {
            if (!region.getPerspectiveX1().isEmpty()) {
                try {
                    p_x0 = Double.parseDouble(region.processText(region.getPerspectiveX1()));
                } catch (Exception e) {
                }
            }
            if (!region.getPerspectiveY1().isEmpty()) {
                try {
                    p_y0 = Double.parseDouble(region.processText(region.getPerspectiveY1()));
                } catch (Exception e) {
                }
            }
            if (!region.getPerspectiveX2().isEmpty()) {
                try {
                    p_x1 = Double.parseDouble(region.processText(region.getPerspectiveX2()));
                } catch (Exception e) {
                }
            }
            if (!region.getPerspectiveY2().isEmpty()) {
                try {
                    p_y1 = Double.parseDouble(region.processText(region.getPerspectiveY2()));
                } catch (Exception e) {
                }
            }
            if (!region.getPerspectiveX3().isEmpty()) {
                try {
                    p_x2 = Double.parseDouble(region.processText(region.getPerspectiveX3()));
                } catch (Exception e) {
                }
            }
            if (!region.getPerspectiveY3().isEmpty()) {
                try {
                    p_y2 = Double.parseDouble(region.processText(region.getPerspectiveY3()));
                } catch (Exception e) {
                }
            }
            if (!region.getPerspectiveX4().isEmpty()) {
                try {
                    p_x3 = Double.parseDouble(region.processText(region.getPerspectiveX4()));
                } catch (Exception e) {
                }
            }
            if (!region.getPerspectiveY4().isEmpty()) {
                try {
                    p_y3 = Double.parseDouble(region.processText(region.getPerspectiveY4()));
                } catch (Exception e) {
                }
            }
        }
        double depth = 0.0;
        try {
            if (!region.getPerspectiveDepth().isEmpty()) {
                depth = Double.parseDouble(region.processText(region.getPerspectiveDepth()));
            }
        } catch (Exception e) {
        }

        double __x0 = region.getX1Value() + w * p_x0;
        double __y0 = region.getY1Value() + h * p_y0;
        double __x1 = region.getX1Value() + w * p_x1;
        double __y1 = region.getY1Value() + h * p_y1;
        double __x2 = region.getX1Value() + w * p_x2;
        double __y2 = region.getY1Value() + h * p_y2;
        double __x3 = region.getX1Value() + w * p_x3;
        double __y3 = region.getY1Value() + h * p_y3;

        double dp0 = Math.sqrt((h_x - __x0) * (h_x - __x0) + (h_y - __y0) * (h_y - __y0));
        double angle0 = Math.atan2(h_y - __y0, h_x - __x0);
        double dp1 = Math.sqrt((h_x - __x1) * (h_x - __x1) + (h_y - __y1) * (h_y - __y1));
        double angle1 = Math.atan2(h_y - __y1, h_x - __x1);
        double dp2 = Math.sqrt((h_x - __x2) * (h_x - __x2) + (h_y - __y2) * (h_y - __y2));
        double angle2 = Math.atan2(h_y - __y2, h_x - __x2);
        double dp3 = Math.sqrt((h_x - __x3) * (h_x - __x3) + (h_y - __y3) * (h_y - __y3));
        double angle3 = Math.atan2(h_y - __y3, h_x - __x3);

        if (depth > 0.0) {
            double _x0 = __x0 + dp0 * Math.cos(angle0) * depth;
            double _y0 = __y0 + dp0 * Math.sin(angle0) * depth;
            p_x0 = (_x0 - region.getX1Value()) / w;
            p_y0 = (_y0 - region.getY1Value()) / h;

            double _x1 = __x1 + dp1 * Math.cos(angle1) * depth;
            double _y1 = __y1 + dp1 * Math.sin(angle1) * depth;
            p_x1 = (_x1 - region.getX1Value()) / w;
            p_y1 = (_y1 - region.getY1Value()) / h;

            double _x2 = __x2 + dp2 * Math.cos(angle2) * depth;
            double _y2 = __y2 + dp2 * Math.sin(angle2) * depth;
            p_x2 = (_x2 - region.getX1Value()) / w;
            p_y2 = (_y2 - region.getY1Value()) / h;

            double _x3 = __x3 + dp3 * Math.cos(angle3) * depth;
            double _y3 = __y3 + dp3 * Math.sin(angle3) * depth;
            p_x3 = (_x3 - region.getX1Value()) / w;
            p_y3 = (_y3 - region.getY1Value()) / h;
        }
        if (p_x0 != 0.0 || p_y0 != 0.0 || p_x1 != 1.0 || p_y1 != 0.0 || p_x2 != 1.0 || p_y2 != 1.0 || p_x3 != 0.0 || p_y3 != 1.0) {
            if (w == 0 || h == 0) {
                return;
            }

            int max_x = (int) Math.max(p_x0 * w, p_x1 * w);
            max_x = (int) Math.max(max_x, p_x2 * w);
            max_x = (int) Math.max(max_x, p_x3 * w);

            int max_y = (int) Math.max(p_y0 * h, p_y1 * h);
            max_y = (int) Math.max(max_y, p_y2 * h);
            max_y = (int) Math.max(max_y, p_y3 * h);

            minX = (int) Math.min(p_x0 * w, p_x1 * w);
            minX = (int) Math.min(minX, p_x2 * w);
            minX = (int) Math.min(minX, p_x3 * w);

            minY = (int) Math.min(p_y0 * h, p_y1 * h);
            minY = (int) Math.min(minY, p_y2 * h);
            minY = (int) Math.min(minY, p_y3 * h);


            if (max_x - minX == 0 || max_y - minY == 0) {
                return;
            }
            if (p_x0 == p_x1 || p_x2 == p_x3 || p_y0 == p_y3 || p_y1 == p_y2) {
                return;
            }
            buffer2 = Workspace.createCompatibleImage(Math.abs(max_x - minX), Math.abs(max_y - minY), buffer2);

            this.perspectiveFilter.setCorners(
                    (float) (p_x0 * w), (float) (p_y0 * h),
                    (float) (p_x1 * w), (float) (p_y1 * h),
                    (float) (p_x2 * w), (float) (p_y2 * h),
                    (float) (p_x3 * w), (float) (p_y3 * h));

            buffer = this.perspectiveFilter.filter(buffer, buffer2);

            g.drawImage(buffer, region.getX1Value() + minX, region.getY1Value() + minY, null);
        } else {
            g.drawImage(buffer, region.getX1Value(), region.getY1Value(), null);
        }
    }
}
