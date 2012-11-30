package net.sf.sketchlet.designer.editor.ui;

import net.sf.sketchlet.framework.model.Pages;
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
                    Pages.getMessageFrame().close();
                } catch (Exception e) {
                }
            }
        }).start();
    }

    public static void showMessage(JFrame frame, String strMessage, Component relativeComp) {
        MessageFrame.closeMessage();
        if (frame != null && frame.getState() != JFrame.ICONIFIED) {
            try {
                Pages.setMessageFrame(new MessageFrame(frame, strMessage, relativeComp));
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
                Pages.setMessageFrame(new MessageFrame(frame, strMessage, relativeComp, timeout));
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
        if (Pages.getMessageFrame() != null) {
            Pages.getMessageFrame().close();
        }
    }

    public static boolean isOpen() {
        return Pages.getMessageFrame() != null;
    }

    public void close() {
        if (Pages.getMessageFrame() != null) {
            stopped = true;
            setVisible(false);
            Pages.setMessageFrame(null);
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
