/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.ui;

import net.sf.sketchlet.designer.data.Pages;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;

/**
 * @author zobrenovic
 */
public class MessageFrame extends JDialog implements Runnable {
    private static final Logger log = Logger.getLogger(MessageFrame.class);

    boolean stopped = false;
    public JTextField message = new JTextField(20);
    JProgressBar progressBar = new JProgressBar();
    boolean finished = false;
    JFrame frame;
    Cursor originalCursor;
    Thread t = new Thread(this);

    private MessageFrame(JFrame frame, String strMessage, Component relativeComp, final long timeout) {
        this(frame, strMessage, relativeComp);
        new Thread(new Runnable() {

            public void run() {
                try {
                    Thread.sleep(timeout);
                    Pages.msgFrame.close();
                } catch (Exception e) {
                }
            }
        }).start();
    }

    public static void showMessage(JFrame frame, String strMessage, Component relativeComp) {
        MessageFrame.closeMessage();
        if (frame != null && frame.getState() != JFrame.ICONIFIED) {
            try {
                Pages.msgFrame = new MessageFrame(frame, strMessage, relativeComp);
            } catch (Throwable e) {
                log.error("Exception in message frame.", e);
                closeMessage();
            }
        }
    }

    public static void showMessage(JFrame frame, String strMessage, Component relativeComp, long timeout) {
        MessageFrame.closeMessage();
        if (frame.getState() != JFrame.ICONIFIED) {
            try {
                Pages.msgFrame = new MessageFrame(frame, strMessage, relativeComp, timeout);
            } catch (Throwable e) {
                log.error("Exception in message frame.", e);
                closeMessage();
            }
        }
    }

    Component relativeComp;

    private MessageFrame(JFrame frame, String strMessage, Component relativeComp) {
        super(frame, false);
        this.relativeComp = relativeComp;
        this.frame = frame;
        this.setUndecorated(true);
        // this.setAlwaysOnTop(true);
        message.setEditable(false);
        message.setText(strMessage);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(message);
        this.getContentPane().add(panel);
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 6));
        panel.add(progressBar, BorderLayout.SOUTH);
        progressBar.setIndeterminate(true);
        // progressBar.setPreferredSize(new Dimension(100, 8));
        if (frame != null) {
            originalCursor = frame.getCursor();
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        // SwingUtilities.invokeLater(this);//t.start;();

        run();
    }

    public void run() {
        pack();
        if (relativeComp != null) {
            this.setLocationRelativeTo(relativeComp);
        } else {
            this.setLocationRelativeTo(frame);
        }
        setVisible(true);
    }

    public static void closeMessage() {
        if (Pages.msgFrame != null) {
            Pages.msgFrame.close();
        }
    }

    public static boolean isOpen() {
        return Pages.msgFrame != null;
    }

    public void close() {
        if (Pages.msgFrame != null) {
            stopped = true;
            setVisible(false);
            Pages.msgFrame = null;
        }
        if (frame != null) {
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public void setMessage(String strMessage) {
        message.setText(strMessage);
    }

    public static void main(String args[]) {
        new MessageFrame(null, "test", null);
    }
}

class RotateProgressPanel extends JPanel implements Runnable {

    Thread t = new Thread(this);
    boolean stopped = false;

    public RotateProgressPanel() {
        t.start();
    }

    public void stop() {
        stopped = true;
    }

    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.BLACK);
        g.drawOval(0, 0, 22, 22);
        g.drawString("" + System.currentTimeMillis(), 30, 30);
    }

    public Dimension getPreferredSize() {
        return new Dimension(100, 40);
    }

    public void run() {
        try {
            while (!stopped) {
                repaint();
                Thread.sleep(50);
            }
        } catch (Exception e) {
        }
    }
}