/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.ui;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.designer.GlobalProperties;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.help.HelpViewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * @author zobrenovic
 */
public class DidYouKnow extends JPanel {

    HelpViewer helpViewer = new HelpViewer("profiling", false);
    HelpViewer helpViewerAll = new HelpViewer("index", true);
    HelpViewer helpViewerVideos = new HelpViewer("videos", false);
    HelpViewer helpViewerLectures = new HelpViewer("tutorials", false);
    JButton close = new JButton("Close", Workspace.createImageIcon("resources/ok.png"));
    JButton next = new JButton("Next Random Topic", Workspace.createImageIcon("resources/go-next.png"));
    JCheckBox nextTime = new JCheckBox("Show Next Time", GlobalProperties.get("help-start-screen") == null || !GlobalProperties.get("help-start-screen").equalsIgnoreCase("false"));
    JTabbedPane tabs = new JTabbedPane();
    static JDialog frame = null;

    public DidYouKnow(JDialog _frame) {
        this.frame = _frame;
        frame.setIconImage(Workspace.createImageIcon("resources/help-browser2.png").getImage());
        setLayout(new BorderLayout());
        JToolBar buttonPanel = new JToolBar();
        buttonPanel.setFloatable(false);
        buttonPanel.add(close);
        buttonPanel.add(next);
        buttonPanel.add(nextTime);
        add(buttonPanel, BorderLayout.SOUTH);

        helpViewer.setPreferredSize(new Dimension(500, 300));
        helpViewer.showAutoHelpByID("profiling");
        helpViewerAll.setPreferredSize(new Dimension(300, 300));
        helpViewerAll.showAutoHelpByID("index");
        helpViewerVideos.setPreferredSize(new Dimension(300, 300));
        helpViewerVideos.showAutoHelpByID("videos");
        helpViewerLectures.setPreferredSize(new Dimension(300, 300));
        helpViewerLectures.showAutoHelpByID("tutorials");
        tabs.addTab("Random Topic", Workspace.createImageIcon("resources/idea.png"), helpViewer);
        tabs.addTab("Videos", Workspace.createImageIcon("resources/slideshow.gif"), helpViewerVideos);
        tabs.addTab("Lecture Notes", Workspace.createImageIcon("resources/history.gif"), helpViewerLectures);
        tabs.addTab("All Topics", Workspace.createImageIcon("resources/listbullet.gif"), helpViewerAll);

        next.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                randomTopic();
            }
        });

        close.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (frame != null) {
                    GlobalProperties.set("help-start-screen", "" + nextTime.isSelected());
                    GlobalProperties.save();
                    frame.setVisible(false);
                    frame = null;
                }
            }
        });
        frame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                frame = null;
            }
        });

        randomTopic();

        frame.getRootPane().setDefaultButton(close);

        add(tabs);
    }

    public void randomTopic() {
        String[] files = new File(SketchletContextUtils.getSketchletDesignerHelpDir()).list();

        String strTopic = null;
        while (strTopic == null) {
            int n = (int) (files.length * Math.random());
            String strFile = files[n];
            if (strFile.endsWith(".html")) {
                strTopic = strFile.replace(".html", "");
                break;
            }
        }

        tabs.setSelectedIndex(0);
        helpViewer.showAutoHelpByID(strTopic);
    }

    public static void showFrame(JFrame parent) {
        if (frame != null) {
            frame.setVisible(true);
            return;
        }
        JDialog frame = new JDialog(parent);
        frame.setTitle("Did You Know?");
        frame.setModal(true);
        frame.add(new DidYouKnow(frame));
        frame.pack();
        if (parent != null) {
            frame.setLocationRelativeTo(parent);
        }
        frame.setVisible(true);
    }

    public static void main(String args[]) {
        showFrame(null);
    }
}
