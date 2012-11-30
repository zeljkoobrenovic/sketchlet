package net.sf.sketchlet.designer.editor.ui.timers.curve;

import net.sf.sketchlet.framework.model.programming.timers.curves.StiffnessCurve;
import net.sf.sketchlet.framework.model.programming.timers.curves.StiffnessSegment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author zobrenovic
 */
public class StiffnessPanel extends JPanel {

    CurveFrame frame;
    StiffnessCurve curveStiffness;
    MouseHandler mouseHandler = new MouseHandler();
    StiffnessSegment selectedSegment = null;

    public StiffnessPanel(final CurveFrame frame, final StiffnessCurve curve) {
        curve.setPanel(this);
        this.frame = frame;
        this.curveStiffness = curve;
        this.addMouseListener(mouseHandler);
        this.addMouseMotionListener(mouseHandler);
        this.setFocusable(true);
        this.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    if (selectedSegment != null) {
                        curve.getSegments().remove(selectedSegment);
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
        g2.drawString("0 s", 26, h - 42);
        if (curveStiffness.getMaxDuration() > 0) {
            g2.drawString((int) curveStiffness.getMaxDuration() + " s", 22, 30);
        }
        g2.setColor(Color.GRAY);
        g2.drawString("relative time", w / 2, h - 20);
        g2.drawString("min/max", 5, h / 2 - 10);
        g2.drawString("duration", 5, h / 2 + 5);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
        curveStiffness.getCurve().drawCurve(g2, 50, h - 40, w - 60, h - 60);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        curveStiffness.drawCurve(g2, 50, h - 40, w - 60, h - 60);
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

            for (StiffnessSegment segment : curveStiffness.getSegments()) {
                double time = segment.getEndTime();
                int x = 50 + (int) (time * w);

                if (Math.abs(x - mouse_x) < 10) {
                    selectedSegment = segment;
                    frame.tableStiffness.getSelectionModel().setSelectionInterval(i, i);
                    repaint();
                    return;
                }
                i++;
            }
            i = 0;
            StiffnessSegment prevSegment = null;
            for (StiffnessSegment segment : curveStiffness.getSegments()) {
                double time = segment.getEndTime();
                int x = 50 + (int) (time * w);

                time = (mouse_x - x) / w;
                double d = Math.abs(time - segment.getEndTime());
                if (d > 11) {
                    StiffnessSegment newSegment = new StiffnessSegment(time, "", "");
                    curveStiffness.getSegments().add(newSegment);
                    curveStiffness.sort();
                    repaint();
                    frame.modelStiffness.fireTableDataChanged();
                    selectedSegment = newSegment;
                    frame.tableStiffness.getSelectionModel().setSelectionInterval(i, i);
                    return;
                }
                prevSegment = segment;
                i++;
            }

            selectedSegment = null;
            frame.tableStiffness.getSelectionModel().setSelectionInterval(-1, -1);
            repaint();
        }

        public void mouseDragged(MouseEvent e) {
            if (selectedSegment == null) {
                return;
            }
            int mouse_x = e.getX();
            int mouse_y = e.getY();
            int w = getWidth() - 60;
            int h = getHeight() - 60;
            double time = (mouse_x - 50.0) / w;
            if (time > 0 && time < 1) {
                selectedSegment.setEndTime(time);
            }
            double value = (h + 20.0 - mouse_y) / h;

            curveStiffness.sort();
            frame.modelStiffness.fireTableDataChanged();
            int i = curveStiffness.getSegments().indexOf(selectedSegment);
            frame.tableStiffness.getSelectionModel().setSelectionInterval(i, i);

            repaint();
        }

        public void mouseReleased(MouseEvent e) {
            mousePressed = false;
        }
    }

    public static void main(String args[]) {
        JFrame frame = new JFrame();
        StiffnessCurve c = new StiffnessCurve(null);
        StiffnessPanel p = new StiffnessPanel(null, c);
        frame.add(p);
        frame.pack();
        frame.setVisible(true);
    }
}
