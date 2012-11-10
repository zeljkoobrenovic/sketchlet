/*
 * To change this template, choose Tools | Templates
 * and open the template in the openExternalEditor.
 */
package net.sf.sketchlet.designer.install;

import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.common.zip.UnZip;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;

/**
 * @author zobrenovic
 */
public class Installer extends JPanel {
    private static final Logger log = Logger.getLogger(Installer.class);

    JFrame frame = new JFrame();
    JProgressBar progress = new JProgressBar();
    JTextField status = new JTextField("  Downloading the latest update...");
    static boolean stopped = false;
    JButton stop = new JButton("Stop");

    public Installer() {
        setLayout(new BorderLayout());
        progress.setIndeterminate(true);
        JPanel panel = new JPanel();
        stop.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(stop);
        panel.add(progress);
        panel.add(stop);
        add(panel, BorderLayout.CENTER);
        status.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(status);
        add(status, BorderLayout.SOUTH);
        frame.add(this);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        stop.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (stop.getText().equalsIgnoreCase("stop")) {
                    stop();
                    progress.setIndeterminate(false);
                    stop.setText("Retry");
                    status.setText("");
                } else {
                    start();
                    progress.setIndeterminate(true);
                    stop.setText("Stop");
                    status.setText("   Downloading the latest update...");
                    download("http://update.zip");
                }
            }
        });

        SketchletEditor.close();

        this.download("http://update.zip");
    }

    public void stop() {
        FileUtils.stopped = true;
        UnZip.stopped = true;
        stopped = true;
    }

    public void start() {
        FileUtils.stopped = false;
        UnZip.stopped = false;
        stopped = false;
    }

    public static void main(String args[]) throws Exception {
        Installer m = new Installer();
        m.frame.setTitle("Sketchlet Updater");

        m.frame.pack();
        // Get the size of the screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        // Determine the new location of the window
        int w = m.frame.getSize().width;
        int h = m.frame.getSize().height;
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;

        // Move the window
        m.frame.setLocation(x, y);
        m.frame.setVisible(true);
    }

    public static boolean shouldDownload() {
        /*String strVersion = Version.getVersion();
        String strVersionOnSite = FileUtils.getURLText("", false);*/
        // return !strVersion.equalsIgnoreCase(strVersionOnSite);
        return false;
    }

    public static void open() {
        if (shouldDownload()) {
            Installer m = new Installer();
            m.frame.setTitle("Sketchlet Updater");

            m.frame.pack();
            m.frame.setLocationRelativeTo(Workspace.mainFrame);
            m.frame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(Workspace.mainFrame, "You already have the latest version.", "Sketchlet Update", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void download(final String strURL) {
        new Thread(new Runnable() {

            public void run() {
                try {
                    File tempZip = File.createTempFile("sketchlet_", "zip");
                    tempZip.deleteOnExit();
                    boolean bDown = FileUtils.copyURLToFile(new URL(strURL), tempZip, status, "   Downloading the latest update...", "");
                    if (!bDown) {
                        stop();
                        JOptionPane.showMessageDialog(frame, "Could not open the update URL.", "Error", JOptionPane.ERROR_MESSAGE);
                        frame.setVisible(false);
                        return;
                    }
                    if (!stopped) {
                        File dir = createTempDir();
                        status.setText("   Unpacking the update...");
                        UnZip.unzipArchive(tempZip, dir);
                        String skDir = SketchletContext.getInstance().getApplicationHomeDir();
                        status.setText("   Installing files...");
                        FileUtils.restore(dir.getPath(), new File(skDir).getPath());
                        JOptionPane.showMessageDialog(frame, "Done.\nPlease restart Sketchlet.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        frame.setVisible(false);
                        System.exit(0);
                    }
                } catch (Exception e) {
                    log.error(e);
                    stop();
                    JOptionPane.showMessageDialog(frame, "Could not open the update URL.", "Error", JOptionPane.ERROR_MESSAGE);
                    frame.setVisible(false);
                }
            }
        }).start();
    }

    public static File createTempDir() {
        final String baseTempPath = System.getProperty("java.io.tmpdir");

        File tempDir = new File(baseTempPath + File.separator + "sketchlet_install_" + System.currentTimeMillis());
        if (tempDir.exists() == false) {
            tempDir.mkdir();
        }

        tempDir.deleteOnExit();

        return tempDir;
    }
}
