/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.ui.playback.displays;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.data.Evaluator;
import net.sf.sketchlet.designer.playback.displays.ScreenMapping;
import net.sf.sketchlet.designer.ui.playback.PlaybackFrame;

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

    public static boolean bSaved = false;
    float x0 = 0.0f;
    float y0 = 0.0f;
    float x1 = 1.0f;
    float y1 = 0.0f;
    float x2 = 1.0f;
    float y2 = 1.0f;
    float x3 = 0.0f;
    float y3 = 1.0f;
    String strOriginalX0 = "";
    String strOriginalY0 = "";
    String strOriginalX1 = "";
    String strOriginalY1 = "";
    String strOriginalX2 = "";
    String strOriginalY2 = "";
    String strOriginalX3 = "";
    String strOriginalY3 = "";
    int selectedPoint = -1;
    ScreenMapping display;
    PlaybackFrame playbackFrame;

    public CalibrationFrame(ScreenMapping display, PlaybackFrame playbackFrame) {
        this.display = display;
        this.playbackFrame = playbackFrame;
        try {
            strOriginalX0 = display.cutFromSketch[8][1].toString();
            x0 = Float.parseFloat(Evaluator.processText(display.cutFromSketch[8][1].toString(), "", ""));
        } catch (Exception e) {
        }
        try {
            strOriginalY0 = display.cutFromSketch[9][1].toString();
            y0 = Float.parseFloat(Evaluator.processText(display.cutFromSketch[9][1].toString(), "", ""));
        } catch (Exception e) {
        }
        try {
            strOriginalX1 = display.cutFromSketch[10][1].toString();
            x1 = Float.parseFloat(Evaluator.processText(display.cutFromSketch[10][1].toString(), "", ""));
        } catch (Exception e) {
        }
        try {
            strOriginalY1 = display.cutFromSketch[11][1].toString();
            y1 = Float.parseFloat(Evaluator.processText(display.cutFromSketch[11][1].toString(), "", ""));
        } catch (Exception e) {
        }
        try {
            strOriginalX2 = display.cutFromSketch[12][1].toString();
            x2 = Float.parseFloat(Evaluator.processText(display.cutFromSketch[12][1].toString(), "", ""));
        } catch (Exception e) {
        }
        try {
            strOriginalY2 = display.cutFromSketch[13][1].toString();
            y2 = Float.parseFloat(Evaluator.processText(display.cutFromSketch[13][1].toString(), "", ""));
        } catch (Exception e) {
        }
        try {
            strOriginalX3 = display.cutFromSketch[14][1].toString();
            x3 = Float.parseFloat(Evaluator.processText(display.cutFromSketch[14][1].toString(), "", ""));
        } catch (Exception e) {
        }
        try {
            strOriginalY3 = display.cutFromSketch[15][1].toString();
            y3 = Float.parseFloat(Evaluator.processText(display.cutFromSketch[15][1].toString(), "", ""));
        } catch (Exception e) {
        }
        PerspectiveMotionListener listener = new PerspectiveMotionListener();
        this.addMouseMotionListener(listener);
        this.addMouseListener(listener);
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

            if (isSelected(e.getPoint(), x0, y0)) {
                selectedPoint = 0;
            } else if (isSelected(e.getPoint(), x1, y1)) {
                selectedPoint = 1;
            } else if (isSelected(e.getPoint(), x2, y2)) {
                selectedPoint = 2;
            } else if (isSelected(e.getPoint(), x3, y3)) {
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
                x0 = Math.min(x / 400.f, 1.0f);
                y0 = Math.min(y / 400.f, 1.0f);
                x0 = Math.max(x0, 0.0f);
                y0 = Math.max(y0, 0.0f);
            } else if (selectedPoint == 1) {
                x1 = Math.min(x / 400.f, 1.0f);
                y1 = Math.min(y / 400.f, 1.0f);
                x1 = Math.max(x1, 0.0f);
                y1 = Math.max(y1, 0.0f);
            } else if (selectedPoint == 2) {
                x2 = Math.min(x / 400.f, 1.0f);
                y2 = Math.min(y / 400.f, 1.0f);
                x2 = Math.max(x2, 0.0f);
                y2 = Math.max(y2, 0.0f);
            } else if (selectedPoint == 3) {
                x3 = Math.min(x / 400.f, 1.0f);
                y3 = Math.min(y / 400.f, 1.0f);
                x3 = Math.max(x3, 0.0f);
                y3 = Math.max(y3, 0.0f);
            }

            updateData();

            repaint();
            PlaybackFrame.repaintAllFrames();
        }

        public void mouseMoved(MouseEvent e) {
        }
    }

    public void updateData() {
        display.cutFromSketch[8][1] = "" + x0;
        display.cutFromSketch[9][1] = "" + y0;
        display.cutFromSketch[10][1] = "" + x1;
        display.cutFromSketch[11][1] = "" + y1;
        display.cutFromSketch[12][1] = "" + x2;
        display.cutFromSketch[13][1] = "" + y2;
        display.cutFromSketch[14][1] = "" + x3;
        display.cutFromSketch[15][1] = "" + y3;

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

        drawPoint(g2, x0, y0, "1");
        drawPoint(g2, x1, y1, "2");
        drawPoint(g2, x2, y2, "3");
        drawPoint(g2, x3, y3, "4");
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

        xPoints[0] = 10 + (int) (400 * x0);
        xPoints[1] = 10 + (int) (400 * x1);
        xPoints[2] = 10 + (int) (400 * x2);
        xPoints[3] = 10 + (int) (400 * x3);

        yPoints[0] = 10 + (int) (400 * y0);
        yPoints[1] = 10 + (int) (400 * y1);
        yPoints[2] = 10 + (int) (400 * y2);
        yPoints[3] = 10 + (int) (400 * y3);

        g2.fillPolygon(xPoints, yPoints, 4);
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    public static JDialog screenSelecterFrame = null;

    public static void createAndShowGUI(JFrame frame, ScreenMapping display, int displayIndex) {

        //Create and set up the window.
        screenSelecterFrame = new JDialog(frame, false);
        screenSelecterFrame.setTitle(Language.translate("Calibration"));

        // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        PlaybackFrame playbackFrame = null;
        if (PlaybackFrame.playbackFrame != null && PlaybackFrame.playbackFrame.length > displayIndex) {
            playbackFrame = PlaybackFrame.playbackFrame[displayIndex];
        }

        //Create and set up the content pane.
        CalibrationFrame selecter = new CalibrationFrame(display, playbackFrame);
        selecter.setOpaque(true); //content panes must be opaque
        screenSelecterFrame.getContentPane().add(new JScrollPane(selecter), BorderLayout.CENTER);
        screenSelecterFrame.getContentPane().add(new CommandPanel(screenSelecterFrame, selecter), BorderLayout.SOUTH);

        //Display the window.
        //screenSelecterFrame.pack();
        screenSelecterFrame.pack();
        screenSelecterFrame.setVisible(true);
    }

    public static void main(String[] args) {

        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                createAndShowGUI(null, null, 0);
            }
        });
    }

    static class CommandPanel extends JPanel {

        JButton saveButton;
        JButton cancelButton;
        JDialog frame;
        CalibrationFrame selecter;

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
            selecter.bSaved = true;
            selecter.display.cutFromSketch[8][1] = "" + selecter.x0;
            selecter.display.cutFromSketch[9][1] = "" + selecter.y0;
            selecter.display.cutFromSketch[10][1] = "" + selecter.x1;
            selecter.display.cutFromSketch[11][1] = "" + selecter.y1;
            selecter.display.cutFromSketch[12][1] = "" + selecter.x2;
            selecter.display.cutFromSketch[13][1] = "" + selecter.y2;
            selecter.display.cutFromSketch[14][1] = "" + selecter.x3;
            selecter.display.cutFromSketch[15][1] = "" + selecter.y3;
            this.frame.setVisible(false);
        }

        public void cancel() {
            selecter.bSaved = false;
            selecter.display.cutFromSketch[8][1] = selecter.strOriginalX0;
            selecter.display.cutFromSketch[9][1] = selecter.strOriginalY0;
            selecter.display.cutFromSketch[10][1] = selecter.strOriginalX1;
            selecter.display.cutFromSketch[11][1] = selecter.strOriginalY1;
            selecter.display.cutFromSketch[12][1] = selecter.strOriginalX2;
            selecter.display.cutFromSketch[13][1] = selecter.strOriginalY2;
            selecter.display.cutFromSketch[14][1] = selecter.strOriginalX3;
            selecter.display.cutFromSketch[15][1] = selecter.strOriginalY3;
            this.frame.setVisible(false);
        }
    }
}
