/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui.playback;

import net.sf.sketchlet.common.awt.AWTUtilitiesWrapper;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.designer.data.Pages;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.designer.playback.displays.ScreenMapping;
import net.sf.sketchlet.designer.programming.macros.Commands;
import net.sf.sketchlet.designer.programming.screenscripts.ScreenScripts;
import net.sf.sketchlet.designer.tools.imagecache.ImageCache;
import net.sf.sketchlet.designer.ui.pagetransition.StateDiagram;
import net.sf.sketchlet.designer.ui.playback.displays.InteractionSpaceFrame;
import net.sf.sketchlet.util.RefreshTime;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Vector;

/**
 * @author cuypers
 */
public class PlaybackFrame extends JFrame {

    public static PlaybackFrame playbackFrame[];
    public PlaybackPanel playbackPanel;
    JButton backButton = new JButton(Workspace.createImageIcon("resources/go-previous.png"));
    JButton forwardButton = new JButton(Workspace.createImageIcon("resources/go-next.png"));
    JButton tabsButton = new JButton(Workspace.createImageIcon("resources/down.png"));
    JMenuItem settingsButton = new JMenuItem("Screen Settings", Workspace.createImageIcon("resources/screen.png"));
    ScreenMapping display;
    JButton more = new JButton("more...", Workspace.createImageIcon("resources/preferences-system.png", ""));
    int displayIndex;

    private PlaybackFrame(final int displayIndex, ScreenMapping display, Pages pages) {
        this.displayIndex = displayIndex;
        this.display = display;
        playbackPanel = new PlaybackPanel(display, pages, this);

        final JButton more = new JButton(Workspace.createImageIcon("resources/preferences-system.png", ""));
        more.setToolTipText("More commands");
        more.setText("more...");
        more.setMnemonic(KeyEvent.VK_M);

        JToolBar toolbar = new JToolBar("Playback", JToolBar.HORIZONTAL);
        JButton reloadButton = new JButton(Workspace.createImageIcon("resources/view-refresh.png"));
        JButton clearButton = new JButton(Workspace.createImageIcon("resources/clean.png"));
        JButton statesButton = new JButton(Workspace.createImageIcon("resources/states.png"));
        JButton bigger = new JButton(Workspace.createImageIcon("resources/zoomin.gif"));
        JButton smaller = new JButton(Workspace.createImageIcon("resources/zoomout.gif"));
        more.setToolTipText("More commands");
        more.setMnemonic(KeyEvent.VK_M);
        toolbar.add(backButton);
        toolbar.add(forwardButton);
        toolbar.add(tabsButton);
        toolbar.addSeparator();
        toolbar.add(reloadButton);
        toolbar.add(clearButton);
        toolbar.addSeparator();
        toolbar.add(statesButton);
        toolbar.addSeparator();
        toolbar.add(smaller);
        toolbar.add(bigger);
        toolbar.add(more);

        if (this.display.showToolbar.isSelected()) {
            add(toolbar, BorderLayout.NORTH);
        }

        final PlaybackFrame parent = this;

        bigger.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                playbackPanel.scale += 0.1;
                playbackPanel.revalidate();
                RefreshTime.update();
                playbackPanel.repaint();
            }
        });
        smaller.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                playbackPanel.scale -= 0.1;
                if (playbackPanel.scale < 0.1) {
                    playbackPanel.scale = 0.1;
                }
                playbackPanel.revalidate();
                RefreshTime.update();
                playbackPanel.repaint();
            }
        });
        backButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                playbackPanel.goBack();
            }
        });
        forwardButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                Commands.execute(PlaybackPanel.currentPage, "Go to page", "next", "", playbackPanel.currentPage.activeTimers, playbackPanel.currentPage.activeMacros, "", "", parent);
            }
        });
        reloadButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                // playbackPanel.initImage();
                playbackPanel.currentPage.activate(true);
            }
        });
        clearButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                // playbackPanel.initImage();
            }
        });
        statesButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                StateDiagram.showDiagram(SketchletEditor.editorPanel.pages);
            }
        });
        settingsButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                InteractionSpaceFrame.showFrame(displayIndex);
            }
        });
        more.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JPopupMenu popupMenu = new JPopupMenu();

                popupMenu.add(playbackPanel.showAnnotation);
                popupMenu.add(playbackPanel.highlightRegions);
                popupMenu.addSeparator();
                popupMenu.add(settingsButton);

                popupMenu.show(more.getParent(), more.getX(), more.getY() + more.getHeight());
            }
        });
        tabsButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                JPopupMenu popupMenu = new JPopupMenu();

                int i = 0;
                for (final Page s : playbackPanel.pages.pages) {
                    JMenuItem sketchMenuItem = new JMenuItem((i + 1) + ". " + s.title);
                    sketchMenuItem.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent ae) {
                            showSketch(s);
                        }
                    });
                    popupMenu.add(sketchMenuItem);
                    i++;
                }

                popupMenu.show(tabsButton.getParent(), tabsButton.getX(), tabsButton.getY() + tabsButton.getHeight());
            }
        });
        this.setUndecorated(!this.display.showDecoration.isSelected());

        if (this.display.alwaysOnTop.isSelected()) {
            this.setAlwaysOnTop(true);
        }
        /*
        JScrollPane scrollPane = new JScrollPane(playbackPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         */
        add(playbackPanel);

        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                closeWindow();
            }
        });
        this.addKeyListener(this.playbackPanel);
        setFocusable(true);

        enableControls();

    }

    public static void showSketch(Page page) {
        PlaybackPanel.showSketch(page);
        refreshAllFrames(page);
    }

    public static void repaintAllFrames() {
        if (PlaybackFrame.playbackFrame != null) {
            for (int i = 0; i < PlaybackFrame.playbackFrame.length; i++) {
                PlaybackFrame frame = PlaybackFrame.playbackFrame[i];
                if (frame != null) {
                    frame.playbackPanel.repaint();
                }
            }
        }
    }

    public static void repaintAllFramesIfNeeded() {
        if (PlaybackFrame.playbackFrame != null) {
            for (int i = 0; i < PlaybackFrame.playbackFrame.length; i++) {
                PlaybackFrame frame = PlaybackFrame.playbackFrame[i];
                if (frame != null) {
                    frame.playbackPanel.repaintIfNeeded();
                }
            }
        }
    }

    public static void refreshAllFrames(Page page) {
        PlaybackFrame.playbackFrame[0].setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        for (int i = 0; i < PlaybackFrame.playbackFrame.length; i++) {
            PlaybackFrame frame = PlaybackFrame.playbackFrame[i];
            frame.setTitle(page.title);
            frame.enableControls();
            frame.playbackPanel.repaint();

            frame.playbackPanel.exportDisplay();
        }
        PlaybackFrame.playbackFrame[0].setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

    }

    public static boolean clearImagesOnClose = false;

    public static void closeWindow() {
        if (clearImagesOnClose) {
            ImageCache.clear();
            clearImagesOnClose = false;
        }
        PlaybackPanel.currentPage.deactivate(true);
        close();
        if (!SketchletEditor.inPlaybackMode) {
            if (SketchletEditor.editorFrame.isVisible()) {
                if (SketchletEditor.editorPanel.currentPage != null) {
                    SketchletEditor.editorPanel.currentPage.activate(false);
                }

                SketchletEditor.editorPanel.currentPage.regions.refreshFromVariables();
                SketchletEditor.editorPanel.repaint();
                SketchletEditor.editorFrame.setState(JFrame.NORMAL);
                //FreeHand.editorFrame.toFront();
                SketchletEditor.editorFrame.setVisible(true);
            }
        } else {
            if (Workspace.bCloseOnPlaybackEnd) {
                Workspace.processRunner.killProcesses();
                System.exit(0);
            }
            SketchletEditor.close(false);
        }
        playbackFrame = null;
        if (SketchletEditor.editorPanel != null) {
            SketchletEditor.editorPanel.repaintIfNeeded();
        }
        if (SketchletEditor.inPlaybackMode) {
            SketchletEditor.editorFrame = null;
            SketchletEditor.inPlaybackMode = false;
        }

        if (SketchletEditor.editorPanel != null) {
            SketchletEditor.editorPanel.tabsModes.setSelectedIndex(0);
            SketchletEditor.editorPanel.resetScale();
        }
    }

    public static void close() {
        ScreenScripts.closeScreenScripts();

        if (playbackFrame != null) {
            for (int i = 0; i < playbackFrame.length; i++) {
                if (playbackFrame[i] != null && playbackFrame[i].isVisible()) {
                    playbackFrame[i].setVisible(false);
                    DataServer.variablesServer.removeVariablesUpdateListener(playbackFrame[i].playbackPanel);
                    playbackFrame[i].playbackPanel.dispose();
                    playbackFrame[i].dispose();
                    playbackFrame[i] = null;
                }
            }
        }
        playbackFrame = null;
        // System.gc();
    }

    public static void play(Pages pages, Page startingPage) {
        ScreenScripts.closeScreenScripts();
        ScreenScripts.createScreenScripts(true);
        if (playbackFrame == null) {
            PlaybackPanel.history = new Vector<Page>();
            // PlaybackPanel.bahabahaImages = new BufferedImage[Sketch.NUMBER_OF_LAYERS];

            Vector<ScreenMapping> activeDisplays = new Vector<ScreenMapping>();

            for (ScreenMapping display : InteractionSpace.displays) {
                if (display.enable.isSelected()) {
                    activeDisplays.add(display);
                }
            }

            if (activeDisplays.size() == 0) {
                activeDisplays.add(new ScreenMapping());
            }

            playbackFrame = new PlaybackFrame[activeDisplays.size()];

            for (int i = 0; i < activeDisplays.size(); i++) {
                playbackFrame[i] = new PlaybackFrame(i, activeDisplays.elementAt(i), pages);
                playbackFrame[i].setIconImage(Workspace.createImageIcon("resources/start.gif", "").getImage());
                playbackFrame[i].setFrameSizeAndPosition();
                playbackFrame[i].setVisible(true);
            }

            showSketch(startingPage);
        } else {
            PlaybackPanel.pages = pages;
            for (int i = 0; i < playbackFrame.length; i++) {
                playbackFrame[i].playbackPanel.showSketch(startingPage);
            }
        }

        SketchletEditor.editorPanel.currentPage.regions.refreshVariables();
        for (int i = 0; i < playbackFrame.length; i++) {
            playbackFrame[i].setTitle(startingPage.title);
            // playbackFrame[i].toFront();
            playbackFrame[i].setVisible(true);
        }
    }

    public void setFrameSizeAndPosition() {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        double x = 0;
        double y = 0;
        double width = playbackPanel.getPreferredSize2().width;
        double height = playbackPanel.getPreferredSize2().height;

        if (!this.display.isEmpty("screen width")) {
            width = this.display.getValue("screen width");
        }
        if (!this.display.isEmpty("screen height")) {
            height = this.display.getValue("screen height");
        }
        if (!this.display.isEmpty("screen x")) {
            x = this.display.getValue("screen x");
        } else {
            x = d.getWidth() / 2 - width / 2;
        }
        if (!this.display.isEmpty("screen y")) {
            y = this.display.getValue("screen y");
        } else {
            y = d.getHeight() / 2 - height / 2;
        }

        if (!this.display.isEmpty("window transparency")) {
            //AWTUtilitiesWrapper.setWindowOpaque(this, false);
            AWTUtilitiesWrapper.setWindowOpacity(this, (float) this.display.getValue("window transparency"));
        }

        if (!this.display.isEmpty("window shape")) {
            String strShape = this.display.getString("window shape").toLowerCase();
            if (strShape.equals("oval") || strShape.equals("circle") || strShape.equals("ellipse")) {
                AWTUtilitiesWrapper.setWindowShape(this, new Ellipse2D.Double(0, 0, width, height));
            } else if (strShape.startsWith("round rectangle")) {
                double cx = 10;
                double cy = 10;

                String params[] = strShape.split(" ");
                if (params.length > 2) {
                    try {
                        cx = Double.parseDouble(params[2]);
                        cy = cx;
                    } catch (Exception e) {
                    }
                }
                if (params.length > 3) {
                    try {
                        cy = Double.parseDouble(params[3]);
                    } catch (Exception e) {
                    }
                }

                AWTUtilitiesWrapper.setWindowShape(this, new RoundRectangle2D.Double(0, 0, width, height, cx, cy));
            }
        }


        // this.setSize((int) width, (int) height);
        this.playbackPanel.setPreferredSize(new Dimension((int) width, (int) height));
        this.pack();
        this.setLocation((int) x, (int) y);

        if (this.display.showMaximized.isSelected()) {
            int n = this.display.screenIndex.getSelectedIndex();
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] allScreens = env.getScreenDevices();
            if (n < 1 || n - 1 > allScreens.length) {
                this.setExtendedState(JFrame.MAXIMIZED_BOTH);
            } else {
                allScreens[n - 1].setFullScreenWindow(this);
            }
        }
    }

    public void enableControls() {
        backButton.setEnabled(this.playbackPanel.history.size() > 0);
    }

    public static void main(String[] args) {
        PlaybackFrame f = new PlaybackFrame(0, null, null);
        f.setVisible(true);
    }
}
