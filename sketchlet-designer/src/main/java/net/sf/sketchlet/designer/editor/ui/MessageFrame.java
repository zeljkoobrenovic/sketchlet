package net.sf.sketchlet.designer.editor.ui;

import net.sf.sketchlet.framework.model.Project;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zobrenovic
 */
public class MessageFrame extends JDialog implements Runnable {
    private static final Logger log = Logger.getLogger(MessageFrame.class);

    private JTextField message = new JTextField(20);
    private JFrame frame;

    private Component relativeComp;

    private MessageFrame(JFrame frame, String strMessage, Component relativeComp, final long timeout) {
        this(frame, strMessage, relativeComp);
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(this);
        executorService.execute(new Runnable() {

            public void run() {
                try {
                    Thread.sleep(timeout);
                    Project.getMessageFrame().close();
                } catch (Exception e) {
                }
            }
        });
    }

    public static void showMessage(JFrame frame, String strMessage, Component relativeComp) {
        MessageFrame.closeMessage();
        if (frame != null && frame.getState() != JFrame.ICONIFIED) {
            try {
                Project.setMessageFrame(new MessageFrame(frame, strMessage, relativeComp));
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
                Project.setMessageFrame(new MessageFrame(frame, strMessage, relativeComp, timeout));
            } catch (Throwable e) {
                log.error("Exception in message frame.", e);
                closeMessage();
            }
        }
    }

    private MessageFrame(JFrame frame, String strMessage, Component relativeComp) {
        super(frame, false);
        this.relativeComp = relativeComp;
        this.frame = frame;
        this.setUndecorated(true);
        message.setEditable(false);
        message.setText(strMessage);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(message);
        this.getContentPane().add(panel);
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 6));
        if (frame != null) {
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }

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
        if (Project.getMessageFrame() != null) {
            Project.getMessageFrame().close();
        }
    }

    public static boolean isOpen() {
        return Project.getMessageFrame() != null;
    }

    public void close() {
        if (Project.getMessageFrame() != null) {
            setVisible(false);
            Project.setMessageFrame(null);
        }
        if (frame != null) {
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }
}
