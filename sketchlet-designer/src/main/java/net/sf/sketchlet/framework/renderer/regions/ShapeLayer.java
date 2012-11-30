package net.sf.sketchlet.framework.renderer.regions;

import net.sf.sketchlet.designer.editor.tool.stroke.WobbleStroke;
import net.sf.sketchlet.designer.editor.ui.toolbars.ColorToolbar;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.util.Colors;

import java.awt.*;

/**
 * @author zobrenovic
 */
public class ShapeLayer extends DrawingLayer {
    private WobbleStroke wobble;
    private int thickness = 0;

    public ShapeLayer(ActiveRegion region) {
        super(region);
    }

    public void dispose() {
        region = null;
        wobble = null;
    }

    public void draw(Graphics2D g2, Component component, boolean bPlayback) {
        draw(g2, component, bPlayback, true, true);
    }

    public void draw(Graphics2D g2, Component component, boolean bPlayback, boolean bFill, boolean bOutline) {
        ActiveRegion region = this.region;
        if (region == null) {
            return;
        }
        if (region.shape.isEmpty()) {
            return;
        }
        String strShape = region.processText(region.shape);
        String strLineColor = region.processText(region.lineColor);
        String strFillColor = region.processText(region.strFillColor);
        String strLineThickness = region.processText(region.lineThickness);
        String strLineStyle = region.processText(region.lineStyle);

        Color lineColor = Colors.getColor(strLineColor);
        Color fillColor = Colors.getColor(strFillColor);
        if (lineColor == null) {
            lineColor = Color.BLACK;
        }
        int lineThickness = 2;
        try {
            lineThickness = Integer.parseInt(strLineThickness);
        } catch (Exception e) {
        }

        Stroke stroke = ColorToolbar.getStroke(strLineStyle, lineThickness);
        if (stroke instanceof WobbleStroke) {
            WobbleStroke _temp = (WobbleStroke) stroke;
            if (wobble != null && thickness == lineThickness) {
                stroke = wobble;
            }
            wobble = _temp;
            thickness = lineThickness;
        }

        if (stroke == null) {
            stroke = new WobbleStroke(3, 1, 1);
        }
        g2.setStroke(stroke);

        int w, h;

        w = region.x2 - region.x1;
        h = region.y2 - region.y1;

        int _center_x = region.x1 + w / 2;
        int _center_y = region.y1 + h / 2;

        if (bFill && fillColor != null) {
            g2.setColor(fillColor);
            if (strShape.equalsIgnoreCase("Rectangle")) {
                g2.fillRect(region.x1, region.y1, w, h);
            } else if (strShape.equalsIgnoreCase("Oval")) {
                g2.fillOval(region.x1 + 2, region.y1 + 2, w - 4, h - 4);
            } else if (strShape.equalsIgnoreCase("Line 1")) {
            } else if (strShape.equalsIgnoreCase("Line 2")) {
            } else if (strShape.equalsIgnoreCase("Horizontal Line")) {
            } else if (strShape.equalsIgnoreCase("Vertical Line")) {
            } else {
                // } else if (strShape.equalsIgnoreCase("Rounded Rectangle")) {
                // g2.fillRoundRect(region.x1, region.y1, w, h, h / 5, h / 5);
                if ((!strShape.isEmpty() && !strShape.equalsIgnoreCase("none")) || region.getRenderer().getWidgetImageLayer().getWidgetPlugin() == null) {
                    g2.fill(region.getArea(bPlayback));
                }
            }
        }
        if (bOutline && stroke != null) {
            g2.setColor(lineColor);
            if (strShape.equalsIgnoreCase("Rectangle")) {
                g2.drawRect(region.x1, region.y1, w, h);
            } else if (strShape.equalsIgnoreCase("Rounded Rectangle")) {
                //} else if (strShape.equalsIgnoreCase("Rounded Rectangle")) {
                //    g2.drawRoundRect(region.x1, region.y1, w, h, h / 5, h / 5);
                g2.draw(region.getArea(bPlayback));
            } else if (strShape.equalsIgnoreCase("Oval")) {
                g2.drawOval(region.x1 + 2, region.y1 + 2, w - 4, h - 4);
            } else if (strShape.equalsIgnoreCase("Line 1")) {
                g2.drawLine(region.x1, region.y1, region.x2, region.y2);
            } else if (strShape.equalsIgnoreCase("Line 2")) {
                g2.drawLine(region.x1, region.y2, region.x2, region.y1);
            } else if (strShape.equalsIgnoreCase("Horizontal Line")) {
                g2.drawLine(region.x1, _center_y, region.x2, _center_y);
            } else if (strShape.equalsIgnoreCase("Vertical Line")) {
                g2.drawLine(_center_x, region.y1, _center_x, region.y2);
            } else if (strShape.equalsIgnoreCase("Triangle 1")) {
                g2.draw(region.getArea(bPlayback));
            } else if (strShape.equalsIgnoreCase("Triangle 2")) {
                g2.draw(region.getArea(bPlayback));
            } else if (strShape.toLowerCase().startsWith("regularpolygon")) {
                g2.draw(region.getArea(bPlayback));
            } else if (strShape.toLowerCase().startsWith("starpolygon")) {
                g2.draw(region.getArea(bPlayback));
            } else if (strShape.toLowerCase().startsWith("pie slice")) {
                g2.draw(region.getArea(bPlayback));
            }
        }
    }
}
