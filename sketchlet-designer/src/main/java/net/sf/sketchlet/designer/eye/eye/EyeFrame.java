package net.sf.sketchlet.designer.eye.eye;

import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;

public class EyeFrame extends JPanel {

    SelectorPanel selectorPanel = new SelectorPanel(this);
    EyeData eyeData = new EyeData(selectorPanel);

    public EyeFrame() {
        this.addMouseListener(new EyeMouseListener());
        this.addMouseMotionListener(new EyeMouseListener());
    }

    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        eyeData.draw(g2, w, h, angle);
    }

    double angle = 0;
    double tempA = 0;
    double startAngle = 0;
    double prevAngle = 0;

    class EyeMouseListener extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            int w = getWidth();
            int h = getHeight();
            prevAngle = (-Math.atan2(w / 2 - e.getX(), h / 2 - e.getY())) - Math.PI / 2;
            while (prevAngle < 0) {
                prevAngle += Math.PI * 2;
            }
            while (prevAngle > Math.PI * 2) {
                prevAngle -= Math.PI * 2;
            }
            startAngle = angle;
        }

        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() >= 2) {
                if (eyeData.selectedSlot != null) {
                    eyeData.selectedSlot.openItem();
                    eyeData.load();
                    repaint();
                }
            }
        }

        public void mouseDragged(MouseEvent e) {
            int w = getWidth();
            int h = getHeight();
            double a = (-Math.atan2(w / 2 - e.getX(), h / 2 - e.getY())) - Math.PI / 2;
            while (a < 0) {
                a += Math.PI * 2;
            }
            while (a > Math.PI * 2) {
                a -= Math.PI * 2;
            }

            double da = a - prevAngle;
            angle = startAngle + da;

            while (angle < 0) {
                angle += Math.PI * 2;
            }
            while (angle > Math.PI * 2) {
                angle -= Math.PI * 2;
            }

            repaint();
        }

        public void mouseMoved(MouseEvent e) {
            int w = getWidth();
            int h = getHeight();
            double a = (-Math.atan2(w / 2 - e.getX(), h / 2 - e.getY())) - Math.PI / 2;
            while (a < 0) {
                a += Math.PI * 2;
            }
            while (a > Math.PI * 2) {
                a -= Math.PI * 2;
            }

            tempA = a;
            repaint();
        }
    }

    public boolean intersects(Area a1, Area a2) {
        Area _a1 = new Area(a1);
        Area _a2 = new Area(a2);
        _a1.intersect(a2);
        _a2.intersect(a1);
        return !_a1.isEmpty() || !_a2.isEmpty();
    }

    public boolean contains(Area a1, Area a2) {
        a1.intersect(a2);
        return a1.equals(a2);
    }

    static JFrame frame = null;
    static EyeFrame eyeFrame = null;

    public static void showFrame() {
        if (frame == null) {
            frame = new JFrame("Sketchlet Eye");
            frame.setIconImage(Workspace.createImageIcon("resources/eye.gif").getImage());
            eyeFrame = new EyeFrame();
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, eyeFrame.selectorPanel, eyeFrame);
            frame.getContentPane().add(splitPane, BorderLayout.CENTER);
            frame.setSize(new Dimension(900, 640));
            frame.setLocationRelativeTo(SketchletEditor.editorFrame);
        } else {
            eyeFrame.eyeData.load();
            eyeFrame.repaint();
        }

        frame.setVisible(true);
    }

    public Component getGUI() {
        EyeFrame eyeFrame = new EyeFrame();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, eyeFrame.selectorPanel, eyeFrame);
        return splitPane;
    }

    public static void hideFrame() {
        if (frame != null) {
            frame.setVisible(false);
            frame = null;
        }
    }
}
