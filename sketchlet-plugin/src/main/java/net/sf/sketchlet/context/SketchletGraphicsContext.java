/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.context;

import java.awt.image.BufferedImage;

/**
 *
 * @author zobrenovic
 */
public abstract class SketchletGraphicsContext {

    private static SketchletGraphicsContext context;

    public static SketchletGraphicsContext getInstance() {
        return context;
    }

    public static void setInstance(SketchletGraphicsContext c) {
        context = c;
    }

    public abstract void clearCanvas();

    public abstract void setColor(int r, int g, int b);

    public abstract void setColor(int r, int g, int b, int transparency);

    public abstract void setTransparency(float transparency);

    public abstract void setLineWidth(double width);

    public abstract void setFont(String name, String style, int size);

    public abstract void drawText(String text, int x, int y);

    public abstract void drawLine(int x1, int y1, int x2, int y2);

    public abstract void drawRect(int x, int y, int w, int h);

    public abstract void drawEllipse(int x, int y, int w, int h);

    public abstract void drawCircle(int center_x, int center_y, int r);

    public abstract void fillRect(int x, int y, int w, int h);

    public abstract void fillEllipse(int x, int y, int w, int h);

    public abstract void fillCircle(int center_x, int center_y, int r);

    public abstract void drawImage(String strPathOrURL, int x, int y);

    public abstract void drawImage(String strPathOrURL, int x, int y, int w, int h);

    public abstract void repaint();

    public abstract int getTextWidth(String text);

    public abstract int getTextHeight(String text);

    public abstract void translate(int x, int y);

    public abstract void rotate(double angle, int x, int y);

    public abstract void scale(double x, double y);

    public abstract void shear(double x, double y);

    public abstract BufferedImage createCompatibleImage(int w, int h);

    public abstract BufferedImage createCompatibleImage(int w, int h, BufferedImage image);

    public abstract SketchletPainter getSketchletPainter();
}
