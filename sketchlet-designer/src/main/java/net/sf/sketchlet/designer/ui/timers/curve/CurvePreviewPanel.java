/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.ui.timers.curve;

import net.sf.sketchlet.designer.programming.timers.curves.Curve;
import net.sf.sketchlet.designer.programming.timers.curves.CurveSegment;

import javax.swing.*;
import java.awt.*;

/**
 * @author zobrenovic
 */
public class CurvePreviewPanel extends JPanel {

    CurveFrame frame;
    Curve curve;
    CurveSegment selectedSegment = null;
    double duration;

    public CurvePreviewPanel(final CurveFrame frame, final Curve curve, double duration) {
        this.duration = duration;
        this.frame = frame;
        this.curve = curve;
    }

    public Dimension getPreferredSize() {
        return new Dimension(400, 300);
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        g2.setColor(Color.GRAY);
        g2.fillRect(0, 0, w, h);
        g2.setColor(Color.WHITE);
        g2.fillRect(50, 20, w - 60, h - 60);
        g2.setColor(Color.BLACK);
        g2.drawRect(50, 20, w - 60, h - 60);
        g2.drawString("0.0", 50, h - 20);
        g2.drawString("" + duration, w - 30, h - 20);
        g2.drawString("0.0", 26, h - 42);
        g2.drawString("1.0", 26, 30);
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("absolute time (in seconds)", w / 2 - 30, h - 20);
        g2.drawString("relative", 5, h / 2 - 10);
        g2.drawString(" value", 5, h / 2 + 5);

        curve.drawAbsoluteCurve(duration, g2, 50, h - 40, w - 60, h - 60);
    }

    public static void main(String args[]) {
        CurveFrame.main(args);
    }
}
