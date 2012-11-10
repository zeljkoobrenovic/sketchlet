/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.regions.connector;

import net.sf.sketchlet.designer.data.Evaluator;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.regions.renderer.TextDrawingLayer;
import net.sf.sketchlet.designer.ui.toolbars.ColorToolbar;
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

    Connector connector;
    public int mouseX;
    public int mouseY;
    public Rectangle2D textRect = null;

    public ConnectorRenderer(Connector connector) {
        this.connector = connector;
    }

    public boolean isSelected() {
        return SketchletEditor.editorPanel.currentPage.selectedConnector == this.connector;
    }

    public void draw(Graphics2D g2, boolean bPlayback) {
        if (this.connector.region1 != null) {
            int x1 = this.connector.region1.getCenterX(bPlayback);
            int y1 = this.connector.region1.getCenterY(bPlayback);
            int x2;
            int y2;
            if (this.connector.region2 != null) {
                x2 = this.connector.region2.getCenterX(bPlayback);
                y2 = this.connector.region2.getCenterY(bPlayback);
            } else {
                x2 = mouseX;
                y2 = mouseY;
            }
            int cx = (x1 + x2) / 2;
            int cy = (y1 + y2) / 2;

            g2.setColor(Colors.getColor(this.connector.lineColor, Color.BLACK));

            float thickness = 1.0f;
            try {
                thickness = (float) Double.parseDouble(this.connector.lineThickness);
            } catch (Exception e) {
            }

            Stroke stroke = ColorToolbar.getStroke(this.connector.lineStyle, thickness);
            if (stroke != null) {
                g2.setStroke(stroke);
                g2.drawLine(x1, y1, x2, y2);
            }

            if (!this.connector.caption.isEmpty()) {
                String text = Evaluator.processText(this.connector.caption, "", "");

                Font oldFont = g2.getFont();
                String fontName = this.connector.fontName;
                String fontStyle = this.connector.fontStyle;
                float th = 10;
                if (!this.connector.fontSize.isEmpty()) {
                    try {
                        th = (float) Double.parseDouble(this.connector.fontSize);
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
                this.textRect = new Rectangle2D.Double(tx - 2, ty - 2, tw + 4, (int) th + 4);
                g2.setColor(new Color(255, 255, 255, 124));
                if (!this.connector.fillColor.isEmpty()) {
                    g2.setColor(Colors.getColor(this.connector.fillColor, new Color(0, 0, 0, 0)));
                    g2.fillRect(tx - 2, ty - 2, tw + 4, (int) th + 4);
                }

                g2.setColor(Color.BLACK);
                g2.setColor(Colors.getColor(this.connector.textColor, Color.BLACK));

                g2.drawString(text, tx, ty + metrics.getAscent() - 3);

                g2.setFont(oldFont);
            }
            if (this.isSelected()) {
                g2.setColor(new Color(50, 50, 50, 150));
                g2.setStroke(new BasicStroke(1));

                g2.draw(new Ellipse2D.Double(cx - 9, cy - 9, 19, 19));
                g2.setColor(Colors.getColor(this.connector.lineColor, Color.BLACK));
            }
        }
    }
}
