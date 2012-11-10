/*
 * To change this template, choose Tools | Templates
 * and open the template in the openExternalEditor.
 */
package net.sf.sketchlet.designer.editor;

import net.sf.sketchlet.common.dnd.GenericTableTransferHandler;
import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.common.template.TemplateMarkers;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.GlobalProperties;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.designer.editor.dnd.SelectDropFile;
import net.sf.sketchlet.designer.help.HelpViewer;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.programming.macros.Macro;
import net.sf.sketchlet.designer.programming.screenscripts.ScreenScripts;
import net.sf.sketchlet.designer.tools.log.ActivityLog;
import net.sf.sketchlet.designer.ui.MessageFrame;
import net.sf.sketchlet.designer.ui.VerticalButton;
import net.sf.sketchlet.designer.ui.desktop.SystemVariablesDialog;
import net.sf.sketchlet.designer.ui.extraeditor.ExtraEditorPanel;
import net.sf.sketchlet.designer.ui.macros.MacrosTablePanel;
import net.sf.sketchlet.designer.ui.page.SketchListPanel;
import net.sf.sketchlet.designer.ui.page.SketchStatePanel;
import net.sf.sketchlet.designer.ui.page.perspective.PerspectivePanel;
import net.sf.sketchlet.designer.ui.page.spreadsheet.SpreadsheetPanel;
import net.sf.sketchlet.designer.ui.profiles.Profiles;
import net.sf.sketchlet.designer.ui.region.ActiveRegionToolbar;
import net.sf.sketchlet.designer.ui.region.ActiveRegionsFrame;
import net.sf.sketchlet.designer.ui.timers.TimersTablePanel;
import net.sf.sketchlet.designer.ui.toolbars.ColorToolbar;
import net.sf.sketchlet.designer.ui.toolbars.FormulaToolbar;
import net.sf.sketchlet.designer.ui.toolbars.ModeToolbar;
import net.sf.sketchlet.designer.ui.toolbars.SimplePagesNavigationPanel;
import net.sf.sketchlet.help.HelpUtils;
import net.sf.sketchlet.plugin.WidgetPlugin;
import net.sf.sketchlet.util.RefreshTime;
import net.sf.sketchlet.util.ui.DataRowFrame;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author zobrenovic
 */
public class SketchletEditorFrame {
    private static final Logger log = Logger.getLogger(SketchletEditorFrame.class);

    public static void createAndShowGui(int showIndex, boolean play) {
        SketchletEditor.inPlaybackMode = play;
        if (SketchletEditor.editorPanel == null) {
            SketchletEditor.bGUIReady = false;
            SketchletEditor.editorFrame = new JFrame();
            SketchletEditor.editorFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            SketchletEditor.editorFrame.setFocusable(true);
            SketchletEditor.editorFrame.setIconImage(Workspace.createImageIcon("resources/editor.gif", "").getImage());
            SketchletEditor.editorFrame.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent e) {
                    MessageFrame.showMessage(Workspace.mainFrame, Language.translate("Please wait..."), Workspace.mainFrame);
                    java.awt.EventQueue.invokeLater(new Runnable() {

                        public void run() {
                            try {
                                SketchletEditor.close();
                            } finally {
                                MessageFrame.closeMessage();
                            }
                        }
                    });
                }

                @Override
                public void windowActivated(WindowEvent e) {
                }

                @Override
                public void windowDeactivated(WindowEvent e) {
                }
            });
            SketchletEditor.editorFrame.setTitle(Language.translate("Sketchlet Editor"));
            Workspace.referenceFrame = SketchletEditor.editorFrame;

            fillFrame();

            Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
            int windowWidth = (int) (sd.getWidth() * 0.9);
            int windowHeight = (int) (sd.getHeight() * 0.9);
            try {
                String strXY = GlobalProperties.get("editor-window-size", "");
                String xy[] = strXY.split(",");
                if (xy.length == 2) {
                    windowWidth = (int) Double.parseDouble(xy[0]);
                    windowHeight = (int) Double.parseDouble(xy[1]);
                }
            } catch (Throwable e) {
                log.error(e);
            }

            if (sd.getWidth() <= windowWidth || sd.getHeight() <= windowHeight) {
                SketchletEditor.editorFrame.setSize((int) (sd.getWidth() * 0.8), (int) (sd.getHeight() * 0.8));
                SketchletEditor.editorFrame.setExtendedState(SketchletEditor.editorFrame.getExtendedState() | Frame.MAXIMIZED_BOTH);
            } else {
                SketchletEditor.editorFrame.setSize(windowWidth, windowHeight);
            }

            SketchletEditor.editorFrame.setLocationRelativeTo(Workspace.mainFrame);
            SketchletEditor.editorPanel.spllitPane.setDividerLocation(windowWidth - 300);
        }

        if (showIndex >= 0) {
            SketchletEditor.editorPanel.openSketchByIndex(showIndex);
        }

        SketchletEditor.editorPanel.loadLayersTab();

        SystemVariablesDialog.startThread();

        if (!play) {
            SketchletEditor.editorFrame.setVisible(true);
        }

        SketchletEditor.editorPanel.enableControls();
        RefreshTime.update();
        SketchletEditor.editorPanel.repaint();
        if (play) {
            try {
                Thread.sleep(500);
            } catch (Throwable e) {
            }
            SketchletEditor.editorPanel.play();
        } else {
            Workspace.mainPanel.sketchletPanel.globalVariablesPanel.enableControls();
        }

        if (SketchletEditor.editorPanel.currentPage != null) {
            int index = SketchletEditor.editorPanel.pages.pages.indexOf(SketchletEditor.editorPanel.currentPage);
            SketchletEditor.editorPanel.sketchListPanel.table.getSelectionModel().setSelectionInterval(index, index);
        }

        if (Profiles.isActive("active_regions_layer")) {
            SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.activeRegionSelectTool, null);
            SketchletEditor.editorPanel.setMode(EditorMode.ACTIONS);
        } else {
            SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.penTool, null);
            SketchletEditor.editorPanel.setMode(EditorMode.SKETCHING);
        }
        SketchletEditor.bGUIReady = true;
    }

    public static void fillFrame() {
        Macro.prepareCommandList();

        SketchletEditor.editorPanel = new SketchletEditor();

        Workspace.mainPanel.menubar = Workspace.mainPanel.createMenubar("menubar");
        SketchletEditor.editorFrame.setJMenuBar(Workspace.mainPanel.menubar);

        SwingUtilities.updateComponentTreeUI(SketchletEditor.editorFrame);
        SketchletEditor.statusBar.setEditable(false);
        SketchletEditor.statusBar.putClientProperty("JComponent.sizeVariant", "mini");
        SwingUtilities.updateComponentTreeUI(SketchletEditor.statusBar);

        SketchletEditor.editorPanel.scrollPane = new JScrollPane(SketchletEditor.editorPanel);
        if (GlobalProperties.get("rulers", "false").equalsIgnoreCase("true")) {
            SketchletEditor.editorPanel.scrollPane.setColumnHeaderView(SketchletEditor.editorPanel.rulerHorizontal);
            SketchletEditor.editorPanel.scrollPane.setRowHeaderView(SketchletEditor.editorPanel.rulerVertical);
            GlobalProperties.set("rulers", "true");
        }

        SketchletEditor.editorPanel.scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        SketchletEditor.editorPanel.scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        SketchletEditor.editorPanel.colorToolbar = new ColorToolbar();
        SketchletEditor.editorPanel.formulaToolbar = new FormulaToolbar();
        SketchletEditor.editorPanel.activeRegionToolbar = new ActiveRegionToolbar();
        SketchletEditor.editorPanel.fileDrop = new FileDrop(System.out, SketchletEditor.editorPanel, new FileDrop.Listener() {

            public void filesDropped(Point p, java.io.File[] files) {
                if (files.length > 0) {
                    if (SketchletEditor.editorPanel.mode == EditorMode.ACTIONS) {
                        int x = (int) ((p.getX()) / SketchletEditor.editorPanel.scale) - SketchletEditor.marginX;
                        int y = (int) ((p.getY()) / SketchletEditor.editorPanel.scale) - SketchletEditor.marginY;
                        ActiveRegion region = SketchletEditor.editorPanel.currentPage.regions.selectRegion(x, y, false);
                        if (region != null) {
                            new SelectDropFile(SketchletEditor.editorPanel.editorFrame, files[0], region, x, y);
                            SketchletEditor.editorPanel.repaint();
                        }

                    } else {
                        int x = (int) ((p.getX()) / SketchletEditor.editorPanel.scale) - SketchletEditor.marginX;
                        int y = (int) ((p.getY()) / SketchletEditor.editorPanel.scale) - SketchletEditor.marginY;

                        SketchletEditor.editorPanel.fromFile(x, y, files[0]);
                    }
                    RefreshTime.update();
                }
            }

            public void dragOver(int x, int y) {
                FileDrop.mouseX = (int) (x - SketchletEditor.marginX * SketchletEditor.editorPanel.scale);
                FileDrop.mouseY = (int) (y - SketchletEditor.marginY * SketchletEditor.editorPanel.scale);
                RefreshTime.update();
                SketchletEditor.editorPanel.repaint();
            }

            public void stringDropped(Point p, String strText) {
                DataRowFrame.emptyOnCancel = true;
                if (SketchletEditor.editorPanel.mode == EditorMode.ACTIONS || SketchletEditor.editorPanel.mode == EditorMode.SKETCHING) {
                    SketchletEditorDnD.processDroppedString(p, strText);
                }

                DataRowFrame.emptyOnCancel = false;
            }
        });


        SketchletEditor.editorPanel.modeToolbar = new ModeToolbar();
        SketchletEditor.editorPanel.perspectivePanel = new PerspectivePanel();
        SketchletEditor.editorPanel.spreadsheetPanel = new SpreadsheetPanel();

        SketchletEditor.editorPanel.sketchToolbar.loadButtons();

        final JButton helpMode = new JButton(Workspace.createImageIcon("resources/help-browser.png", ""));
        helpMode.setToolTipText("What is this toolbar?");
        helpMode.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                switch (SketchletEditor.editorPanel.tabsModes.getSelectedIndex()) {
                    case 0:
                        HelpUtils.openHelpFile("Drawing Mode", "editor_mode_drawing");
                        break;

                    case 1:
                        HelpUtils.openHelpFile("Drawing Mode", "editor_mode_preview");
                        break;

                    case 2:
                        HelpUtils.openHelpFile("Drawing Mode", "editor_mode_trajectory");
                        break;
                }
            }
        });

        JToolBar toolbarHelpMode = new JToolBar();
        toolbarHelpMode.setFloatable(false);
        toolbarHelpMode.setBorder(BorderFactory.createEmptyBorder());
        toolbarHelpMode.add(helpMode);
        SketchletEditor.editorPanel.panelNorthMode = new JPanel(new BorderLayout());

        SketchletEditor.editorPanel.tabsModes.setTabPlacement(JTabbedPane.LEFT);
        SketchletEditor.editorPanel.tabsModes.addTab("", Workspace.createImageIcon("resources/editor.gif"), new JPanel());
        SketchletEditor.editorPanel.tabsModes.setToolTipTextAt(0, Language.translate("Drawing mode (F4)"));

        SketchletEditor.editorPanel.tabsModes.addTab("", Workspace.createImageIcon("resources/start-preview.gif"), new JPanel());
        SketchletEditor.editorPanel.tabsModes.setToolTipTextAt(1, Language.translate("Preview (F5)"));
        SketchletEditor.editorPanel.tabsModes.setPreferredSize(new Dimension(48, 120));
        SketchletEditor.editorPanel.tabsRight.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                int index = SketchletEditor.editorPanel.tabsRight.getSelectedIndex();
                if (index >= 0) {
                    ActivityLog.log("selectTabRigth", SketchletEditor.editorPanel.tabsRight.getTitleAt(index));
                }
            }
        });
        TutorialPanel.prepare(SketchletEditor.editorPanel.tabsRight);
        SketchletEditor.editorPanel.loadLayersTab();

        SketchletEditor.editorPanel.tabsLayers.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(SketchletEditor.editorPanel.tabsLayers);
        SketchletEditor.editorPanel.tabsLayers.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        SketchletEditor.editorPanel.tabsLayers.setTabPlacement(JTabbedPane.RIGHT);
        SketchletEditor.editorPanel.tabsLayers.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (SketchletEditor.editorPanel.tool != null) {
                    SketchletEditor.editorPanel.tool.deactivate();
                }

                int l = SketchletEditor.editorPanel.tabsLayers.getSelectedIndex();
                if (l < Page.NUMBER_OF_LAYERS) {
                    String strProperty = "transparency layer " + (l + 1);
                    String strTransparency = SketchletEditor.editorPanel.currentPage.getProperty(strProperty);
                    if (strTransparency == null) {
                        return;
                    }
                    boolean bTransparency = !TemplateMarkers.containsStartMarker(strTransparency) && !strTransparency.startsWith("=");
                    SketchletEditor.editorPanel.sliderLayerTransparency.setEnabled(bTransparency);
                    SketchletEditor.editorPanel.layer = l;

                    if (bTransparency) {
                        try {
                            if (strTransparency.isEmpty()) {
                                strTransparency = SketchletEditor.editorPanel.currentPage.getDefaultValue(strProperty);
                            }
                            float t = (float) Double.parseDouble(strTransparency);
                            SketchletEditor.editorPanel.sliderLayerTransparency.setValue((int) (t * SketchletEditor.editorPanel.sliderLayerTransparency.getMaximum()));
                        } catch (Exception e2) {
                        }
                    }
                    SketchletEditor.editorPanel.createGraphics();

                } else {
                    SketchletEditor.editorPanel.sliderLayerTransparency.setEnabled(false);
                }
                ActivityLog.log("setLayer", l + "");
                RefreshTime.update();
                SketchletEditor.editorPanel.repaint();

            }
        });
        TutorialPanel.prepare(SketchletEditor.editorPanel.tabsLayers);

        SketchletEditor.editorPanel.tabsModes.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                int nTab = SketchletEditor.editorPanel.tabsModes.getSelectedIndex();
                SketchletEditor.editorPanel.sketchToolbar.setEnabled(nTab == 0);
                switch (nTab) {
                    case 0:
                        SketchletEditor.editorPanel.resetScale();
                        WidgetPlugin.setActiveWidget(null);
                        if (SketchletEditor.editorPanel.tabsModes.getTabCount() == 3) {
                            SketchletEditor.editorPanel.tabsModes.removeTabAt(2);
                            SketchletEditor.editorPanel.mode = EditorMode.UNDEFINED;
                        }
                        if (Profiles.isActive("active_regions_layer")) {
                            SketchletEditor.editorPanel.setMode(EditorMode.ACTIONS, 0);
                        } else {
                            SketchletEditor.editorPanel.setMode(EditorMode.SKETCHING, 0);
                        }

                        SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("editor_mode_drawing");
                        break;
                    case 1:
                        if (SketchletEditor.editorPanel.tabsModes.getTabCount() == 3) {
                            SketchletEditor.editorPanel.tabsModes.removeTabAt(2);
                        }
                        MessageFrame.showMessage(SketchletEditor.editorFrame, "Preparing preview...", SketchletEditor.editorPanel.centralPanel);
                        java.awt.EventQueue.invokeLater(new Runnable() {

                            public void run() {
                                try {
                                    SketchletEditor.editorPanel.setMode(EditorMode.PREVIEW, 0);
                                    SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("editor_mode_preview");
                                    SketchletEditor.editorPanel.activeRegionMenu.setEnabled(false);
                                    SketchletEditor.editorPanel.formulaToolbar.enableControls(false, false);
                                    SketchletEditor.editorPanel.internalPlaybackPanel.requestFocus();
                                } finally {
                                    MessageFrame.closeMessage();
                                }
                            }
                        });
                        break;
                    case 2:
                        SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("editor_mode_trajectory");
                        SketchletEditor.editorPanel.activeRegionMenu.setEnabled(false);
                        SketchletEditor.editorPanel.formulaToolbar.enableControls(false, false);
                        break;
                }

            }
        });
        TutorialPanel.prepare(SketchletEditor.editorPanel.tabsModes);
        SketchletEditor.editorPanel.panelModes = new JPanel(new BorderLayout());
        if (Profiles.isActive("active_regions_layer")) {
            SketchletEditor.editorPanel.panelModes.add(SketchletEditor.editorPanel.tabsModes, BorderLayout.NORTH);
        }
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(SketchletEditor.editorPanel.modeToolbar, BorderLayout.NORTH);
        SketchletEditor.editorPanel.panelModes.add(centerPanel, BorderLayout.CENTER);
        SketchletEditor.editorPanel.panelModes.add(toolbarHelpMode, BorderLayout.SOUTH);

        SketchletEditor.editorPanel.centralPanel.add(SketchletEditor.editorPanel.panelModes, BorderLayout.WEST);

        SketchletEditor.editorPanel.mainSketchPanel.add(SketchletEditor.editorPanel.panelNorthMode, BorderLayout.WEST);
        SketchletEditor.editorPanel.mainSketchPanel.add(SketchletEditor.editorPanel.scrollPane);

        JPanel panelLayers = new JPanel();
        panelLayers.setLayout(new BoxLayout(panelLayers, BoxLayout.Y_AXIS));
        panelLayers.add(SketchletEditor.editorPanel.tabsLayers);
        SketchletEditor.editorPanel.tabsLayers.setSize(new Dimension(50, 300));
        SketchletEditor.editorPanel.tabsLayers.setPreferredSize(new Dimension(50, 300));
        SketchletEditor.editorPanel.sliderLayerTransparency = new JSlider(JSlider.VERTICAL, 0, 50, 50);
        SketchletEditor.editorPanel.sliderLayerTransparency.setToolTipText("Layer transparency");
        SketchletEditor.editorPanel.sliderLayerTransparency.setPreferredSize(new Dimension(50, 100));
        SketchletEditor.editorPanel.sliderLayerTransparency.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                int value = SketchletEditor.editorPanel.sliderLayerTransparency.getValue();
                float transparency = (float) value / SketchletEditor.editorPanel.sliderLayerTransparency.getMaximum();

                SketchletEditor.editorPanel.currentPage.setProperty("transparency layer " + (SketchletEditor.editorPanel.layer + 1), "" + transparency);
                SketchletEditor.editorPanel.repaintEverything();
            }
        });
        TutorialPanel.prepare(SketchletEditor.editorPanel.sliderLayerTransparency);
        SketchletEditor.editorPanel.drawingPanel.add(panelLayers, BorderLayout.EAST);
        SketchletEditor.editorPanel.mainSketchPanel.add(SketchletEditor.editorPanel.activeRegionToolbar, BorderLayout.SOUTH);

        SketchletEditor.editorPanel.drawingPanel.add(SketchletEditor.editorPanel.mainSketchPanel);
        SketchletEditor.editorPanel.panelDrawingPanel = new JPanel(new BorderLayout());
        JPanel panelDrawingPanelLeft = new JPanel(new BorderLayout());
        panelDrawingPanelLeft.add(SketchletEditor.editorPanel.drawingPanel, BorderLayout.CENTER);
        SketchletEditor.editorPanel.panelDrawingPanel.add(panelDrawingPanelLeft, BorderLayout.CENTER);

        SketchletEditor.editorPanel.editorPane.add(SketchletEditor.editorPanel.panelDrawingPanel);

        SketchletEditor.editorPanel.sketchListPanel = new SketchListPanel();
        SketchletEditor.editorPanel.centralPanel.add(SketchletEditor.editorPanel.editorPane);
        SketchletEditor.editorPanel.controlPanel = new SimplePagesNavigationPanel();
        SketchletEditor.editorPanel.centralPanel.add(SketchletEditor.editorPanel.controlPanel, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        BorderLayout bl = new BorderLayout();
        bl.setVgap(0);
        SketchletEditor.editorPanel.statusPanel = new JPanel(bl);
        JToolBar profilePanel = new JToolBar();
        profilePanel.setFloatable(false);

        JMenuBar mbprofiles = new JMenuBar();
        SketchletEditor.editorPanel.profilesMenu = Profiles.getMenu();
        mbprofiles.add(SketchletEditor.editorPanel.profilesMenu);
        profilePanel.add(mbprofiles);

        SketchletEditor.editorPanel.statusPanel.add(profilePanel, BorderLayout.WEST);
        SketchletEditor.editorPanel.statusPanel.add(SketchletEditor.statusBar, BorderLayout.CENTER);
        if (GlobalProperties.get("memory-monitor", "false").equalsIgnoreCase("true")) {
            SketchletEditor.editorPanel.addMemoryPanel();
        }
        SketchletEditor.editorPanel.snapToGrid = GlobalProperties.get("snap-to-grid", "false").equalsIgnoreCase("true");

        bottomPanel.add(SketchletEditor.editorPanel.statusPanel, BorderLayout.SOUTH);

        final JTabbedPane tabsVariables = new JTabbedPane();
        tabsVariables.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                int index = tabsVariables.getSelectedIndex();
                if (index >= 0) {
                    ActivityLog.log("selectTabNavigator", tabsVariables.getTitleAt(index));
                }
            }
        });
        TutorialPanel.prepare(tabsVariables);
        tabsVariables.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(tabsVariables);

        SketchletEditor.editorFrame.getContentPane().add(bottomPanel, "South");
        SketchletEditor.editorPanel.tabsRight.setPreferredSize(new Dimension(250, 450));

        JComponent rightComponent;
        if (Profiles.isActiveAny("variables,io_services,timers,macros,scripts,screen_scripts")) {
            SketchletEditor.editorPanel.tabsNavigator.addTab(Language.translate("Pages"), SketchletEditor.editorPanel.sketchListPanel);
            SketchletEditor.editorPanel.tabsNavigator.addTab(Language.translate("Global Objects"), SketchletEditor.editorPanel.tabsRight);
            rightComponent = SketchletEditor.editorPanel.tabsNavigator;
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(SketchletEditor.editorPanel.tabsNavigator);
            rightComponent = panel;
        } else {
            rightComponent = SketchletEditor.editorPanel.sketchListPanel;
        }

        SketchletEditor.editorPanel.tabsBrowser = new JTabbedPane();
        SketchletEditor.editorPanel.tabsBrowser.setPreferredSize(new Dimension(280, 200));
        SketchletEditor.editorPanel.tabsBrowser.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(SketchletEditor.editorPanel.tabsBrowser);
        SketchletEditor.editorPanel.tabsBrowser.setTabPlacement(JTabbedPane.BOTTOM);

        SketchletEditor.editorPanel.helpViewer = new HelpViewer();

        fillTabsBrowser(rightComponent);

        SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("editor_mode_drawing");

        SketchletEditor.editorPanel.spllitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, SketchletEditor.editorPanel.centralPanel, SketchletEditor.editorPanel.tabsBrowser);

        SketchletEditor.editorPanel.spllitPane.setResizeWeight(1);
        SketchletEditor.editorFrame.getContentPane().add(SketchletEditor.editorPanel.spllitPane, BorderLayout.CENTER);

        SketchletEditor.editorPanel.navigationToolbar.setOrientation(JToolBar.VERTICAL);
        SketchletEditor.editorPanel.navigationToolbar.setFloatable(false);

        JPanel panelEastBottom = new JPanel(new BorderLayout());

        panelEastBottom.add(SketchletEditor.editorPanel.navigationToolbar, BorderLayout.NORTH);

        SketchletEditor.editorFrame.getContentPane().add(panelEastBottom, BorderLayout.EAST);

        SketchletEditor.editorPanel.statePanel = new SketchStatePanel();

        SketchletEditor.editorPanel.extraEditorPanel = new ExtraEditorPanel();

        Workspace.mainPanel.createToolbar(Workspace.mainPanel.mainFrameToolbar, "toolbarVariables");
        Workspace.mainPanel.mainFrameToolbar.setOrientation(JToolBar.VERTICAL);
        TutorialPanel.prepare(Workspace.mainPanel.mainFrameToolbar);

        loadTabsRight();
        loadNavigationButtons();

        SketchletEditor.editorPanel.sketchToolbar.prepareZoomBox();
    }

    private static void loadNavigationButtons() {
        SketchletEditor.editorPanel.navigationToolbar.removeAll();
        SketchletEditor.editorPanel.hide.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setRigthPanelVisible(!SketchletEditor.editorPanel.objectPanelVisible);
            }
        });
        SketchletEditor.editorPanel.navigationToolbar.add(SketchletEditor.editorPanel.hide);
        SketchletEditor.editorPanel.navigationToolbar.addSeparator();

        VerticalButton ab;
        ab = new VerticalButton(Language.translate("Pages & Variables"), Workspace.createImageIcon("resources/icon_other_page.gif"));
        SketchletEditor.editorPanel.navigationToolbar.add(ab);
        SketchletEditor.editorPanel.navigationToolbar.addSeparator();
        ab.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectTabs(0);
            }
        });

        if (Profiles.isActive("io_services")) {
            ab = new VerticalButton(Language.translate("I/O Services"), Workspace.createImageIcon("resources/io_services.png"));
            SketchletEditor.editorPanel.navigationToolbar.add(ab);
            SketchletEditor.editorPanel.navigationToolbar.addSeparator();
            ab.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectTabs(1, SketchletEditor.ioservicesTabIndex);
                }
            });
        }
        if (Profiles.isActive("timers")) {
            ab = new VerticalButton(Language.translate("Timers"), Workspace.createImageIcon("resources/timer_small.png"));
            SketchletEditor.editorPanel.navigationToolbar.add(ab);
            SketchletEditor.editorPanel.navigationToolbar.addSeparator();
            ab.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectTabs(1, SketchletEditor.programmingTabIndex, SketchletEditor.timersTabIndex);
                }
            });
        }
        if (Profiles.isActive("macros")) {
            ab = new VerticalButton(Language.translate("Macros"), Workspace.createImageIcon("resources/programming-object.png"));
            SketchletEditor.editorPanel.navigationToolbar.add(ab);
            SketchletEditor.editorPanel.navigationToolbar.addSeparator();
            ab.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectTabs(1, SketchletEditor.programmingTabIndex, SketchletEditor.macrosTabIndex);
                }
            });
        }
        if (Profiles.isActive("screen_scripts")) {
            ab = new VerticalButton(Language.translate("Screen Scripts"), Workspace.createImageIcon("resources/mouse.gif"));
            SketchletEditor.editorPanel.navigationToolbar.add(ab);
            SketchletEditor.editorPanel.navigationToolbar.addSeparator();
            ab.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectTabs(1, SketchletEditor.programmingTabIndex, SketchletEditor.screenpokingTabIndex);
                }
            });
        }
        if (Profiles.isActive("scripts")) {
            ab = new VerticalButton(Language.translate("Scripts"), Workspace.createImageIcon("resources/code.gif"));
            SketchletEditor.editorPanel.navigationToolbar.add(ab);
            SketchletEditor.editorPanel.navigationToolbar.addSeparator();
            ab.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectTabs(1, SketchletEditor.programmingTabIndex, SketchletEditor.scriptsTabIndex);
                }
            });
        }
    }

    private static void selectTabs(int tabIndex, int tabSubIndex, int tabSubSubIndex) {
        selectTabs(tabIndex, tabSubIndex);
        SketchletEditor.editorPanel.tabsProgramming.setSelectedIndex(tabSubSubIndex);
    }

    private static void selectTabs(int tabIndex, int tabSubIndex) {
        selectTabs(tabIndex);
        SketchletEditor.editorPanel.tabsRight.setSelectedIndex(tabSubIndex);
    }

    private static void selectTabs(int tabIndex) {
        if (!SketchletEditor.editorPanel.tabsVisible) {
            SketchletEditor.editorPanel.sketchToolbar.showHideProjectNavigator();
        }
        setRigthPanelVisible(true);
        SketchletEditor.editorPanel.tabsNavigator.setSelectedIndex(tabIndex);
    }

    private static void setRigthPanelVisible(boolean bVisible) {
        SketchletEditor.editorPanel.objectPanelVisible = bVisible;
        if (!bVisible) {
            SketchletEditor.editorPanel.hide.setIcon(Workspace.createImageIcon("resources/hide-left-icon.png"));
            SketchletEditor.editorPanel.spllitPane.remove(SketchletEditor.editorPanel.tabsBrowser);
        } else {
            SketchletEditor.editorPanel.hide.setIcon(Workspace.createImageIcon("resources/hide-right-icon.png"));
            SketchletEditor.editorPanel.spllitPane.setRightComponent(SketchletEditor.editorPanel.tabsBrowser);
        }
    }

    public static void loadTabsRight() {
        SketchletEditor.editorPanel.tabsRight.removeAll();
        SketchletEditor.editorPanel.tabsProgramming.removeAll();

        SketchletEditor.pageTabIndex = SketchletEditor.editorPanel.tabsRight.getTabCount();
        if (Profiles.isActive("io_services")) {
            SketchletEditor.ioservicesTabIndex = SketchletEditor.editorPanel.tabsRight.getTabCount();
            SketchletEditor.editorPanel.tabsRight.addTab("", Workspace.createImageIcon("resources/service.gif"), Workspace.mainPanel.panelProcesses, Language.translate("I/O Services"));
            Workspace.mainPanel.tableModules.setDragEnabled(true);
            Workspace.mainPanel.tableModules.setTransferHandler(new GenericTableTransferHandler("@macro Service:", 0));
        }

        SketchletEditor.editorPanel.timersTablePanel = new TimersTablePanel();
        SketchletEditor.editorPanel.macrosTablePanel = new MacrosTablePanel();
        if (Profiles.isActive("timers")) {
            SketchletEditor.timersTabIndex = SketchletEditor.editorPanel.tabsProgramming.getTabCount();
            SketchletEditor.editorPanel.tabsProgramming.addTab("", Workspace.createImageIcon("resources/timer.png"), SketchletEditor.editorPanel.timersTablePanel, Language.translate("Timers"));
        }
        if (Profiles.isActiveAny("timers,macros,screen_poking,scripts")) {
            SketchletEditor.editorPanel.tabsProgramming.putClientProperty("JComponent.sizeVariant", "small");
            SwingUtilities.updateComponentTreeUI(SketchletEditor.editorPanel.tabsProgramming);
            SketchletEditor.programmingTabIndex = SketchletEditor.editorPanel.tabsRight.getTabCount();
            SketchletEditor.editorPanel.tabsRight.addTab("", Workspace.createImageIcon("resources/programming.png"), SketchletEditor.editorPanel.tabsProgramming, Language.translate("Programming objects (timers, macros, screen poking, scripts"));
        }
        if (Profiles.isActive("macros")) {
            SketchletEditor.macrosTabIndex = SketchletEditor.editorPanel.tabsProgramming.getTabCount();
            SketchletEditor.editorPanel.tabsProgramming.addTab("", Workspace.createImageIcon("resources/macros.png"), SketchletEditor.editorPanel.macrosTablePanel, Language.translate("Action Lists"));
        }
        if (Profiles.isActive("screen_poking")) {
            SketchletEditor.screenpokingTabIndex = SketchletEditor.editorPanel.tabsProgramming.getTabCount();
            SketchletEditor.editorPanel.tabsProgramming.addTab("", Workspace.createImageIcon("resources/mouse.png"), ScreenScripts.createScreenScripts(false), Language.translate("Screen Poking"));
        }
        if (Workspace.mainPanel.sketchletPanel.panel2 != null) {
            Workspace.mainPanel.sketchletPanel.panel2.table.setDragEnabled(true);
            Workspace.mainPanel.sketchletPanel.panel2.table.setTransferHandler(new GenericTableTransferHandler("@macro Script:", 0));
            if (Profiles.isActive("scripts")) {
                SketchletEditor.scriptsTabIndex = SketchletEditor.editorPanel.tabsProgramming.getTabCount();
                SketchletEditor.editorPanel.tabsProgramming.addTab("", Workspace.createImageIcon("resources/script.png"), Workspace.mainPanel.sketchletPanel.panel2, Language.translate("Scripts"));
                TutorialPanel.prepare(Workspace.mainPanel.sketchletPanel.panel2);
                TutorialPanel.prepare(Workspace.mainPanel.sketchletPanel.panel2.table);
            }
        }

        SketchletEditor.editorPanel.tabsRight.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        SketchletEditor.editorPanel.tabsRight.setTabPlacement(JTabbedPane.TOP);
    }

    public static void fillTabsBrowser(Component rightComponent) {
        if (SketchletEditor.editorPanel != null && SketchletEditor.editorPanel.tabsBrowser != null) {
            SketchletEditor.editorPanel.tabsBrowser.removeAll();
            SketchletEditor.editorPanel.tabsBrowser.addTab(Language.translate("Project Navigator"), rightComponent);
            SketchletEditor.editorPanel.tabsBrowser.addTab(Language.translate("Help"), SketchletEditor.editorPanel.helpViewer);
        }
    }

    public static void onLanguageChange() {
        MessageFrame.showMessage(Workspace.mainFrame, Language.translate("Please wait..."), Workspace.mainFrame);
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                try {
                    SketchletEditor.close();
                    Workspace.mainPanel.sketchletPanel.globalVariablesPanel.variablesTableModel.fireTableStructureChanged();
                    Workspace.mainPanel.sketchletPanel.panel2.scriptsTableModel.fireTableStructureChanged();
                    if (ScreenScripts.screenScriptsPanel != null && ScreenScripts.screenScriptsPanel.model != null) {
                        ScreenScripts.screenScriptsPanel.model.fireTableStructureChanged();
                    }
                    SketchletEditorFrame.createAndShowGui(-1, false);
                } catch (Exception e) {
                    log.error(e);
                } finally {
                    MessageFrame.closeMessage();
                }
            }
        });
    }

    public static void onProfileChange() {
        Macro.prepareCommandList();
        Workspace.mainPanel.menubar = Workspace.mainPanel.createMenubar("menubar");
        SketchletEditor.editorFrame.setJMenuBar(Workspace.mainPanel.menubar);
        SketchletEditor.editorPanel.panelModes.remove(SketchletEditor.editorPanel.tabsModes);
        if (Profiles.isActive("active_regions_layer")) {
            SketchletEditor.editorPanel.panelModes.add(SketchletEditor.editorPanel.tabsModes, BorderLayout.NORTH);
            SketchletEditor.editorPanel.setMode(EditorMode.ACTIONS, 0);
        } else {
            SketchletEditor.editorPanel.tabsModes.setSelectedIndex(0);
            SketchletEditor.editorPanel.setMode(EditorMode.SKETCHING, 0);
        }
        SketchletEditor.editorPanel.panelModes.revalidate();
        SketchletEditor.editorPanel.sketchToolbar.loadButtons();
        Workspace.mainPanel.createToolbar(Workspace.mainPanel.mainFrameToolbar, "toolbarVariables");
        Workspace.mainPanel.mainFrameToolbar.revalidate();

        loadTabsRight();
        loadNavigationButtons();
        JComponent rightComponent;
        if (Profiles.isActiveAny("variables,io_services,timers,macros,scripts,screen_scripts")) {
            SketchletEditor.editorPanel.tabsNavigator.removeAll();
            SketchletEditor.editorPanel.tabsNavigator.addTab(Language.translate("Pages"), SketchletEditor.editorPanel.sketchListPanel);
            SketchletEditor.editorPanel.tabsNavigator.addTab(Language.translate("Global Objects"), SketchletEditor.editorPanel.tabsRight);
            rightComponent = SketchletEditor.editorPanel.tabsNavigator;
        } else {
            rightComponent = SketchletEditor.editorPanel.sketchListPanel;
        }

        SketchletEditor.editorPanel.tabsBrowser.setComponentAt(0, rightComponent);
        fillTabsBrowser(rightComponent);
        SketchletEditor.editorPanel.tabsBrowser.revalidate();

        TutorialPanel.prepare(Workspace.mainPanel.mainFrameToolbar);
        if (SketchletEditor.editorPanel.extraEditorPanel != null) {
            SketchletEditor.editorPanel.extraEditorPanel.createTabs();
        }

        SketchletEditor.editorPanel.sketchListPanel.reloadToolbar();

        ActiveRegionsFrame.reload();
    }
}
