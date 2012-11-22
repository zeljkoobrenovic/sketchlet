/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.playback.ui;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.playback.displays.ScreenMapping;
import net.sf.sketchlet.blackboard.evaluator.Evaluator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * Grabs a frame from a Webcam, overlays the current date and time, and saves the frame as a PNG to c:\webcam.png
 *
 * @author David
 * @version 1.0, 16/01/2004
 */
public class CalibrationFrame extends JPanel {

    private static boolean saved = false;
    private float x0 = 0.0f;
    private float y0 = 0.0f;
    private float x1 = 1.0f;
    private float y1 = 0.0f;
    private float x2 = 1.0f;
    private float y2 = 1.0f;
    private float x3 = 0.0f;
    private float y3 = 1.0f;
    private String strOriginalX0 = "";
    private String strOriginalY0 = "";
    private String strOriginalX1 = "";
    private String strOriginalY1 = "";
    private String strOriginalX2 = "";
    private String strOriginalY2 = "";
    private String strOriginalX3 = "";
    private String strOriginalY3 = "";
    private int selectedPoint = -1;
    private ScreenMapping display;
    private PlaybackFrame playbackFrame;

    public CalibrationFrame(ScreenMapping display, PlaybackFrame playbackFrame) {
        this.display = display;
        this.playbackFrame = playbackFrame;
        try {
            strOriginalX0 = display.pageClip[8][1].toString();
            setX0(Float.parseFloat(Evaluator.processText(display.pageClip[8][1].toString(), "", "")));
        } catch (Exception e) {
        }
        try {
            strOriginalY0 = display.pageClip[9][1].toString();
            setY0(Float.parseFloat(Evaluator.processText(display.pageClip[9][1].toString(), "", "")));
        } catch (Exception e) {
        }
        try {
            strOriginalX1 = display.pageClip[10][1].toString();
            setX1(Float.parseFloat(Evaluator.processText(display.pageClip[10][1].toString(), "", "")));
        } catch (Exception e) {
        }
        try {
            strOriginalY1 = display.pageClip[11][1].toString();
            setY1(Float.parseFloat(Evaluator.processText(display.pageClip[11][1].toString(), "", "")));
        } catch (Exception e) {
        }
        try {
            strOriginalX2 = display.pageClip[12][1].toString();
            setX2(Float.parseFloat(Evaluator.processText(display.pageClip[12][1].toString(), "", "")));
        } catch (Exception e) {
        }
        try {
            strOriginalY2 = display.pageClip[13][1].toString();
            setY2(Float.parseFloat(Evaluator.processText(display.pageClip[13][1].toString(), "", "")));
        } catch (Exception e) {
        }
        try {
            strOriginalX3 = display.pageClip[14][1].toString();
            setX3(Float.parseFloat(Evaluator.processText(display.pageClip[14][1].toString(), "", "")));
        } catch (Exception e) {
        }
        try {
            strOriginalY3 = display.pageClip[15][1].toString();
            setY3(Float.parseFloat(Evaluator.processText(display.pageClip[15][1].toString(), "", "")));
        } catch (Exception e) {
        }
        PerspectiveMotionListener listener = new PerspectiveMotionListener();
        this.addMouseMotionListener(listener);
        this.addMouseListener(listener);
    }

    public float getX0() {
        return x0;
    }

    public void setX0(float x0) {
        this.x0 = x0;
    }

    public float getY0() {
        return y0;
    }

    public void setY0(float y0) {
        this.y0 = y0;
    }

    public float getX1() {
        return x1;
    }

    public void setX1(float x1) {
        this.x1 = x1;
    }

    public float getY1() {
        return y1;
    }

    public void setY1(float y1) {
        this.y1 = y1;
    }

    public float getX2() {
        return x2;
    }

    public void setX2(float x2) {
        this.x2 = x2;
    }

    public float getY2() {
        return y2;
    }

    public void setY2(float y2) {
        this.y2 = y2;
    }

    public float getX3() {
        return x3;
    }

    public void setX3(float x3) {
        this.x3 = x3;
    }

    public float getY3() {
        return y3;
    }

    public void setY3(float y3) {
        this.y3 = y3;
    }

    class PerspectiveMotionListener extends MouseAdapter implements MouseMotionListener {

        final static int SELECT = 0;
        final static int DRAG = 1;
        int mode = SELECT;
        int px;
        int py;

        public void mousePressed(MouseEvent e) {
            px = e.getX();
            py = e.getY();

            if (isSelected(e.getPoint(), getX0(), getY0())) {
                selectedPoint = 0;
            } else if (isSelected(e.getPoint(), getX1(), getY1())) {
                selectedPoint = 1;
            } else if (isSelected(e.getPoint(), getX2(), getY2())) {
                selectedPoint = 2;
            } else if (isSelected(e.getPoint(), getX3(), getY3())) {
                selectedPoint = 3;
            } else {
                selectedPoint = -1;
            }

            repaint();
        }

        boolean isSelected(Point p, float _x, float _y) {
            int x = 10 + (int) (400 * _x);
            int y = 10 + (int) (400 * _y);
            return p.x >= x - 9 && p.x <= x + 9 && p.y >= y - 9 && p.y <= y + 9;
        }

        public void mouseReleased(MouseEvent e) {
            repaint();
        }

        public void mouseDragged(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            if (selectedPoint == 0) {
                setX0(Math.min(x / 400.f, 1.0f));
                setY0(Math.min(y / 400.f, 1.0f));
                setX0(Math.max(getX0(), 0.0f));
                setY0(Math.max(getY0(), 0.0f));
            } else if (selectedPoint == 1) {
                setX1(Math.min(x / 400.f, 1.0f));
                setY1(Math.min(y / 400.f, 1.0f));
                setX1(Math.max(getX1(), 0.0f));
                setY1(Math.max(getY1(), 0.0f));
            } else if (selectedPoint == 2) {
                setX2(Math.min(x / 400.f, 1.0f));
                setY2(Math.min(y / 400.f, 1.0f));
                setX2(Math.max(getX2(), 0.0f));
                setY2(Math.max(getY2(), 0.0f));
            } else if (selectedPoint == 3) {
                setX3(Math.min(x / 400.f, 1.0f));
                setY3(Math.min(y / 400.f, 1.0f));
                setX3(Math.max(getX3(), 0.0f));
                setY3(Math.max(getY3(), 0.0f));
            }

            updateData();

            repaint();
            PlaybackFrame.repaintAllFrames();
        }

        public void mouseMoved(MouseEvent e) {
        }
    }

    public void updateData() {
        display.pageClip[8][1] = "" + getX0();
        display.pageClip[9][1] = "" + getY0();
        display.pageClip[10][1] = "" + getX1();
        display.pageClip[11][1] = "" + getY1();
        display.pageClip[12][1] = "" + getX2();
        display.pageClip[13][1] = "" + getY2();
        display.pageClip[14][1] = "" + getX3();
        display.pageClip[15][1] = "" + getY3();

        if (this.playbackFrame != null) {
            this.playbackFrame.playbackPanel.repaint();
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(420, 420);
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h);
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRect(9, 9, 400, 400);

        drawPoint(g2, getX0(), getY0(), "1");
        drawPoint(g2, getX1(), getY1(), "2");
        drawPoint(g2, getX2(), getY2(), "3");
        drawPoint(g2, getX3(), getY3(), "4");
        drawArea(g2);
    }

    public void drawPoint(Graphics2D g2, float _x, float _y, String strText) {
        int x = 10 + (int) (400 * _x);
        int y = 10 + (int) (400 * _y);

        g2.setColor(Color.RED);
        g2.fillRect(x - 9, y - 9, 19, 19);
        g2.setColor(Color.WHITE);
        g2.drawString(strText, x - 3, y + 5);
    }

    public void drawArea(Graphics2D g2) {
        g2.setColor(new Color(255, 0, 0, 80));
        int xPoints[] = new int[4];
        int yPoints[] = new int[4];

        xPoints[0] = 10 + (int) (400 * getX0());
        xPoints[1] = 10 + (int) (400 * getX1());
        xPoints[2] = 10 + (int) (400 * getX2());
        xPoints[3] = 10 + (int) (400 * getX3());

        yPoints[0] = 10 + (int) (400 * getY0());
        yPoints[1] = 10 + (int) (400 * getY1());
        yPoints[2] = 10 + (int) (400 * getY2());
        yPoints[3] = 10 + (int) (400 * getY3());

        g2.fillPolygon(xPoints, yPoints, 4);
    }

    public static JDialog screenSelecterFrame = null;

    public static void createAndShowGUI(JFrame frame, ScreenMapping display, int displayIndex) {

        //Create and set up the window.
        screenSelecterFrame = new JDialog(frame, false);
        screenSelecterFrame.setTitle(Language.translate("Calibration"));

        PlaybackFrame playbackFrame = null;
        if (PlaybackFrame.playbackFrame != null && PlaybackFrame.playbackFrame.length > displayIndex) {
            playbackFrame = PlaybackFrame.playbackFrame[displayIndex];
        }

        //Create and set up the content pane.
        CalibrationFrame selecter = new CalibrationFrame(display, playbackFrame);
        selecter.setOpaque(true); //content panes must be opaque
        screenSelecterFrame.getContentPane().add(new JScrollPane(selecter), BorderLayout.CENTER);
        screenSelecterFrame.getContentPane().add(new CommandPanel(screenSelecterFrame, selecter), BorderLayout.SOUTH);

        screenSelecterFrame.pack();
        screenSelecterFrame.setVisible(true);
    }

    public static void main(String[] args) {

        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                createAndShowGUI(null, null, 0);
            }
        });
    }

    static class CommandPanel extends JPanel {

        private JButton saveButton;
        private JButton cancelButton;
        private JDialog frame;
        private CalibrationFrame selecter;

        public CommandPanel(JDialog frame, final CalibrationFrame selecter) {
            this.selecter = selecter;
            this.frame = frame;

            setLayout(new BorderLayout());

            final CommandPanel parent = this;

            saveButton = new JButton(Language.translate("Save"), Workspace.createImageIcon("resources/ok.png"));
            saveButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    savePerspective(false);
                }
            });

            cancelButton = new JButton(Language.translate("Cancel"), Workspace.createImageIcon("resources/edit-delete.png"));
            cancelButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    cancel();
                }
            });


            JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout(FlowLayout.LEFT));

            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout(FlowLayout.LEFT));

            panel1.add(new JLabel("   "));
            panel1.add(saveButton);
            panel1.add(cancelButton);
            this.add(panel1, BorderLayout.CENTER);
            this.add(panel2, BorderLayout.SOUTH);

            // this.add( cancelButton );
        }

        public void savePerspective(boolean bClose) {
            selecter.saved = true;
            selecter.display.pageClip[8][1] = "" + selecter.getX0();
            selecter.display.pageClip[9][1] = "" + selecter.getY0();
            selecter.display.pageClip[10][1] = "" + selecter.getX1();
            selecter.display.pageClip[11][1] = "" + selecter.getY1();
            selecter.display.pageClip[12][1] = "" + selecter.getX2();
            selecter.display.pageClip[13][1] = "" + selecter.getY2();
            selecter.display.pageClip[14][1] = "" + selecter.getX3();
            selecter.display.pageClip[15][1] = "" + selecter.getY3();
            this.frame.setVisible(false);
        }

        public void cancel() {
            selecter.saved = false;
            selecter.display.pageClip[8][1] = selecter.strOriginalX0;
            selecter.display.pageClip[9][1] = selecter.strOriginalY0;
            selecter.display.pageClip[10][1] = selecter.strOriginalX1;
            selecter.display.pageClip[11][1] = selecter.strOriginalY1;
            selecter.display.pageClip[12][1] = selecter.strOriginalX2;
            selecter.display.pageClip[13][1] = selecter.strOriginalY2;
            selecter.display.pageClip[14][1] = selecter.strOriginalX3;
            selecter.display.pageClip[15][1] = selecter.strOriginalY3;
            this.frame.setVisible(false);
        }
    }
}
