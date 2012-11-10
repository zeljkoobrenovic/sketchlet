/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.ui.timers.curve;

import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.programming.timers.curves.Curve;
import net.sf.sketchlet.designer.programming.timers.curves.CurveSegment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author zobrenovic
 */
public class CurvePanel extends JPanel {

    public CurveFrame frame;
    public Curve curve;
    public MouseHandler mouseHandler = new MouseHandler();
    public CurveSegment selectedSegment = null;
    boolean selectedStartValue = false;

    public CurvePanel(final CurveFrame frame, final Curve curve) {
        this.frame = frame;
        this.curve = curve;
        curve.panel = this;
        this.addMouseListener(mouseHandler);
        this.addMouseMotionListener(mouseHandler);
        this.setFocusable(true);
        this.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    if (selectedSegment != null) {
                        curve.segments.remove(selectedSegment);
                        selectedSegment = null;
                        repaint();
                    }
                }
            }
        });
    }

    public Dimension getPreferredSize() {
        return new Dimension(400, 300);
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h);
        g2.fillRect(50, 20, w - 60, h - 60);
        g2.setColor(Color.BLACK);
        g2.drawRect(50, 20, w - 60, h - 60);
        g2.drawString("0.0", 50, h - 20);
        g2.drawString("1.0", w - 25, h - 20);
        g2.drawString("0.0", 26, h - 42);
        g2.drawString("1.0", 26, 30);
        g2.setColor(Color.GRAY);
        g2.drawString("relative time", w / 2, h - 20);
        g2.drawString("relative", 5, h / 2 - 10);
        g2.drawString(" value", 5, h / 2 + 5);

        curve.drawCurve(g2, 50, h - 40, w - 60, h - 60);
    }

    public static void main(String args[]) {
        CurveFrame.main(args);
    }

    public boolean mousePressed = false;

    class MouseHandler extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            if (mousePressed) {
                return;
            }
            mousePressed = true;
            int mouse_x = e.getX();
            int mouse_y = e.getY();
            int w = getWidth() - 60;
            int h = getHeight() - 60;

            int i = 0;

            int x = 50 + (int) (0 * w);
            int y = h + 20 - (int) (curve.startValue * h);

            if (Math.abs(x - mouse_x) < 10 && Math.abs(y - mouse_y) < 10) {
                selectedSegment = null;
                selectedStartValue = true;
                return;
            } else {
                selectedStartValue = false;
            }

            for (CurveSegment segment : curve.segments) {
                double value = segment.relativeValue;
                double time = segment.endTime;
                x = 50 + (int) (time * w);
                y = h + 20 - (int) (value * h);

                if (Math.abs(x - mouse_x) < 10 && Math.abs(y - mouse_y) < 10) {
                    selectedSegment = segment;
                    frame.table.getSelectionModel().setSelectionInterval(i, i);
                    repaint();
                    return;
                }
                i++;
            }
            i = 0;
            CurveSegment prevSegment = null;
            for (CurveSegment segment : curve.segments) {
                double value = segment.relativeValue;
                double time = segment.endTime;
                x = 50 + (int) (time * w);
                y = h + 20 - (int) (value * h);

                time = (mouse_x - x) / w;
                value = (y - mouse_y) / h;
                double d = segment.getDistance(prevSegment, time, value);
                if (d < 0.1) {
                    CurveSegment newSegment = new CurveSegment(time, value, "", "", "", "");
                    curve.segments.add(newSegment);
                    curve.sort();
                    repaint();
                    frame.model.fireTableDataChanged();
                    selectedSegment = newSegment;
                    frame.table.getSelectionModel().setSelectionInterval(i, i);
                    return;
                }
                prevSegment = segment;
                i++;
            }

            selectedSegment = null;
            frame.table.getSelectionModel().setSelectionInterval(-1, -1);
            repaint();
        }

        public void mouseDragged(MouseEvent e) {
            if (selectedSegment == null && !selectedStartValue) {
                return;
            }
            int mouse_x = e.getX();
            int mouse_y = e.getY();
            int w = getWidth() - 60;
            int h = getHeight() - 60;
            double time = (mouse_x - 50.0) / w;

            double value = (h + 20.0 - mouse_y) / h;
            if (!selectedStartValue) {
                if (time > 0 && time < 1 && selectedSegment.endTime < 1) {
                    selectedSegment.endTime = time;
                }
                if (value >= 0 && value <= 1) {
                    selectedSegment.relativeValue = value;
                }
            } else {
                if (value >= 0 && value <= 1) {
                    curve.startValue = value;
                }
            }
            curve.sort();
            frame.model.fireTableDataChanged();
            int i = curve.segments.indexOf(selectedSegment);
            frame.table.getSelectionModel().setSelectionInterval(i, i);
            repaint();
        }

        public void mouseReleased(MouseEvent e) {
            mousePressed = false;
            TutorialPanel.addLine("cmd", "Add or change the points of the curve by dragging", curve.panel);
        }
    }
}
