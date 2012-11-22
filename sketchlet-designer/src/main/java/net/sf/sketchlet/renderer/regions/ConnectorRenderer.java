/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.renderer.regions;

import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.toolbars.ColorToolbar;
import net.sf.sketchlet.blackboard.evaluator.Evaluator;
import net.sf.sketchlet.model.Connector;
import net.sf.sketchlet.util.Colors;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * @author zobrenovic
 */
public class ConnectorRenderer {

    private Connector connector;
    private int mouseX;
    private int mouseY;
    private Rectangle2D textRect = null;

    public ConnectorRenderer(Connector connector) {
        this.setConnector(connector);
    }

    public boolean isSelected() {
        return SketchletEditor.getInstance().getCurrentPage().getSelectedConnector() == this.getConnector();
    }

    public void draw(Graphics2D g2, boolean bPlayback) {
        if (this.getConnector().getRegion1() != null) {
            int x1 = this.getConnector().getRegion1().getCenterX(bPlayback);
            int y1 = this.getConnector().getRegion1().getCenterY(bPlayback);
            int x2;
            int y2;
            if (this.getConnector().getRegion2() != null) {
                x2 = this.getConnector().getRegion2().getCenterX(bPlayback);
                y2 = this.getConnector().getRegion2().getCenterY(bPlayback);
            } else {
                x2 = getMouseX();
                y2 = getMouseY();
            }
            int cx = (x1 + x2) / 2;
            int cy = (y1 + y2) / 2;

            g2.setColor(Colors.getColor(this.getConnector().getLineColor(), Color.BLACK));

            float thickness = 1.0f;
            try {
                thickness = (float) Double.parseDouble(this.getConnector().getLineThickness());
            } catch (Exception e) {
            }

            Stroke stroke = ColorToolbar.getStroke(this.getConnector().getLineStyle(), thickness);
            if (stroke != null) {
                g2.setStroke(stroke);
                g2.drawLine(x1, y1, x2, y2);
            }

            if (!this.getConnector().getCaption().isEmpty()) {
                String text = Evaluator.processText(this.getConnector().getCaption(), "", "");

                Font oldFont = g2.getFont();
                String fontName = this.getConnector().getFontName();
                String fontStyle = this.getConnector().getFontStyle();
                float th = 10;
                if (!this.getConnector().getFontSize().isEmpty()) {
                    try {
                        th = (float) Double.parseDouble(this.getConnector().getFontSize());
                    } catch (Exception e) {
                    }
                }
                Font font;
                if (!fontName.isEmpty()) {
                    font = TextDrawingLayer.getFont(fontName, fontStyle, th);
                } else {
                    font = g2.getFont().deriveFont(th);
                }
                g2.setFont(font);
                FontRenderContext frc = g2.getFontRenderContext();
                LineMetrics metrics = font.getLineMetrics(text, frc);

                Rectangle2D rect = font.getStringBounds(text, frc);

                int tx = cx - (int) rect.getWidth() / 2;
                int ty = cy - (int) rect.getHeight() / 2 + 2;
                int tw = (int) rect.getWidth();
                this.setTextRect(new Rectangle2D.Double(tx - 2, ty - 2, tw + 4, (int) th + 4));
                g2.setColor(new Color(255, 255, 255, 124));
                if (!this.getConnector().getFillColor().isEmpty()) {
                    g2.setColor(Colors.getColor(this.getConnector().getFillColor(), new Color(0, 0, 0, 0)));
                    g2.fillRect(tx - 2, ty - 2, tw + 4, (int) th + 4);
                }

                g2.setColor(Color.BLACK);
                g2.setColor(Colors.getColor(this.getConnector().getTextColor(), Color.BLACK));

                g2.drawString(text, tx, ty + metrics.getAscent() - 3);

                g2.setFont(oldFont);
            }
            if (this.isSelected()) {
                g2.setColor(new Color(50, 50, 50, 150));
                g2.setStroke(new BasicStroke(1));

                g2.draw(new Ellipse2D.Double(cx - 9, cy - 9, 19, 19));
                g2.setColor(Colors.getColor(this.getConnector().getLineColor(), Color.BLACK));
            }
        }
    }

    public Rectangle2D getTextRect() {
        return textRect;
    }

    public void setTextRect(Rectangle2D textRect) {
        this.textRect = textRect;
    }

    public Connector getConnector() {
        return connector;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    public int getMouseX() {
        return mouseX;
    }

    public void setMouseX(int mouseX) {
        this.mouseX = mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    public void setMouseY(int mouseY) {
        this.mouseY = mouseY;
    }
}
