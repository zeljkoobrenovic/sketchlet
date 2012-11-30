package net.sf.sketchlet.designer.editor;

import net.sf.sketchlet.common.dnd.GenericTableTransferHandler;
import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.common.template.TemplateMarkers;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.GlobalProperties;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.dnd.SelectDropFile;
import net.sf.sketchlet.designer.help.HelpViewer;
import net.sf.sketchlet.framework.model.log.ActivityLog;
import net.sf.sketchlet.designer.editor.ui.MessageFrame;
import net.sf.sketchlet.designer.editor.ui.VerticalButton;
import net.sf.sketchlet.designer.editor.ui.desktop.SystemVariablesDialog;
import net.sf.sketchlet.designer.editor.ui.extraeditor.ExtraEditorPanel;
import net.sf.sketchlet.designer.editor.ui.macros.MacrosTablePanel;
import net.sf.sketchlet.designer.editor.ui.page.PageDetailsPanel;
import net.sf.sketchlet.designer.editor.ui.page.PageListPanel;
import net.sf.sketchlet.designer.editor.ui.page.perspective.PerspectivePanel;
import net.sf.sketchlet.designer.editor.ui.page.spreadsheet.SpreadsheetPanel;
import net.sf.sketchlet.designer.editor.ui.profiles.Profiles;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionToolbar;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionsFrame;
import net.sf.sketchlet.designer.editor.ui.timers.TimersTablePanel;
import net.sf.sketchlet.designer.editor.ui.toolbars.ColorToolbar;
import net.sf.sketchlet.designer.editor.ui.toolbars.FormulaToolbar;
import net.sf.sketchlet.designer.editor.ui.toolbars.ModeToolbar;
import net.sf.sketchlet.designer.editor.ui.toolbars.SimplePagesNavigationPanel;
import net.sf.sketchlet.help.HelpUtils;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.model.Page;
import net.sf.sketchlet.framework.model.programming.macros.Macro;
import net.sf.sketchlet.framework.model.programming.screenscripts.ScreenScripts;
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
import java.io.File;

/**
 * @author zobrenovic
 */
public class SketchletEditorFrame {
    private static final Logger log = Logger.getLogger(SketchletEditorFrame.class);

    public static void createAndShowGui(int showIndex, boolean play) {
        if (SketchletEditor.getInstance() == null) {
            SketchletEditor.setbGUIReady(false);
            SketchletEditor.editorFrame = new JFrame();
            SketchletEditor.editorFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            SketchletEditor.editorFrame.setFocusable(true);
            SketchletEditor.editorFrame.setIconImage(Workspace.createImageIcon("resources/editor.gif", "").getImage());
            SketchletEditor.editorFrame.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent e) {
                    MessageFrame.showMessage(Workspace.getMainFrame(), Language.translate("Please wait..."), Workspace.getMainFrame());
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
            Workspace.setReferenceFrame(SketchletEditor.editorFrame);

            populateFrame();

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
                windowWidth = (int) sd.getWidth();

                SketchletEditor.editorFrame.setSize((int) (sd.getWidth() * 0.8), (int) (sd.getHeight() * 0.8));
                SketchletEditor.editorFrame.setExtendedState(SketchletEditor.editorFrame.getExtendedState() | Frame.MAXIMIZED_BOTH);
            } else {
                SketchletEditor.editorFrame.setSize(windowWidth, windowHeight);
            }

            SketchletEditor.editorFrame.setLocationRelativeTo(Workspace.getMainFrame());
        }

        if (showIndex >= 0) {
            SketchletEditor.getInstance().openSketchByIndex(showIndex);
        }

        SketchletEditor.getInstance().loadLayersTab();

        SystemVariablesDialog.startThread();

        if (!play) {
            SketchletEditor.editorFrame.setVisible(true);
        }

        SketchletEditor.getInstance().enableControls();
        RefreshTime.update();
        SketchletEditor.getInstance().repaint();
        if (play) {
            try {
                Thread.sleep(500);
            } catch (Throwable e) {
            }
            SketchletEditor.getInstance().play();
        } else {
            Workspace.getMainPanel().getSketchletPanel().globalVariablesPanel.enableControls();
        }

        if (SketchletEditor.getInstance().getCurrentPage() != null) {
            int index = SketchletEditor.getInstance().getPages().getPages().indexOf(SketchletEditor.getInstance().getCurrentPage());
            SketchletEditor.getInstance().getPageListPanel().table.getSelectionModel().setSelectionInterval(index, index);
        }

        if (Profiles.isActive("active_regions_layer")) {
            SketchletEditor.getInstance().setTool(SketchletEditor.getInstance().getActiveRegionSelectTool(), null);
            SketchletEditor.getInstance().setEditorMode(SketchletEditorMode.ACTIONS);
        } else {
            SketchletEditor.getInstance().setTool(SketchletEditor.getInstance().getPenTool(), null);
            SketchletEditor.getInstance().setEditorMode(SketchletEditorMode.SKETCHING);
        }
        SketchletEditor.setbGUIReady(true);

        SketchletEditor.getInstance().getSpllitPane().setDividerLocation(SketchletEditor.editorFrame.getWidth() - 300);
    }

    public static void populateFrame() {
        Macro.prepareCommandList();

        SketchletEditor.setEditorPanel(new SketchletEditor());

        Workspace.getMainPanel().setMenuBar(Workspace.getMainPanel().createMenubar("menubar"));
        SketchletEditor.editorFrame.setJMenuBar(Workspace.getMainPanel().getMenuBar());

        SwingUtilities.updateComponentTreeUI(SketchletEditor.editorFrame);
        SketchletEditor.getStatusBar().setEditable(false);
        SketchletEditor.getStatusBar().putClientProperty("JComponent.sizeVariant", "mini");
        SwingUtilities.updateComponentTreeUI(SketchletEditor.getStatusBar());

        SketchletEditor.getInstance().scrollPane = new JScrollPane(SketchletEditor.getInstance());
        if (GlobalProperties.get("rulers", "false").equalsIgnoreCase("true")) {
            SketchletEditor.getInstance().scrollPane.setColumnHeaderView(SketchletEditor.getInstance().getRulerHorizontal());
            SketchletEditor.getInstance().scrollPane.setRowHeaderView(SketchletEditor.getInstance().getRulerVertical());
            GlobalProperties.set("rulers", "true");
        }

        SketchletEditor.getInstance().scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        SketchletEditor.getInstance().scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        SketchletEditor.getInstance().setColorToolbar(new ColorToolbar());
        SketchletEditor.getInstance().setFormulaToolbar(new FormulaToolbar());
        SketchletEditor.getInstance().setActiveRegionToolbar(new ActiveRegionToolbar());
        SketchletEditor.getInstance().setFileDrop(new FileDrop(System.out, SketchletEditor.getInstance(), new FileDrop.Listener() {

            public void filesDropped(Point p, File[] files) {
                if (files.length > 0) {
                    if (SketchletEditor.getInstance().getMode() == SketchletEditorMode.ACTIONS) {
                        int x = (int) ((p.getX()) / SketchletEditor.getInstance().getScale()) - SketchletEditor.getInstance().getMarginX();
                        int y = (int) ((p.getY()) / SketchletEditor.getInstance().getScale()) - SketchletEditor.getInstance().getMarginY();
                        ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().selectRegion(x, y, false);
                        if (region != null) {
                            new SelectDropFile(SketchletEditor.getInstance().editorFrame, files[0], region, x, y);
                            SketchletEditor.getInstance().repaint();
                        }

                    } else {
                        int x = (int) ((p.getX()) / SketchletEditor.getInstance().getScale()) - SketchletEditor.getInstance().getMarginX();
                        int y = (int) ((p.getY()) / SketchletEditor.getInstance().getScale()) - SketchletEditor.getInstance().getMarginY();

                        SketchletEditor.getInstance().fromFile(x, y, files[0]);
                    }
                    RefreshTime.update();
                }
            }

            public void dragOver(int x, int y) {
                FileDrop.setMouseX((int) (x - SketchletEditor.getInstance().getMarginX() * SketchletEditor.getInstance().getScale()));
                FileDrop.setMouseY((int) (y - SketchletEditor.getInstance().getMarginY() * SketchletEditor.getInstance().getScale()));
                RefreshTime.update();
                SketchletEditor.getInstance().repaint();
            }

            public void stringDropped(Point p, String strText) {
                DataRowFrame.emptyOnCancel = true;
                if (SketchletEditor.getInstance().getMode() == SketchletEditorMode.ACTIONS || SketchletEditor.getInstance().getMode() == SketchletEditorMode.SKETCHING) {
                    SketchletEditor.getInstance().getDragAndDropController().processDroppedString(p, strText);
                }

                DataRowFrame.emptyOnCancel = false;
            }
        }));


        SketchletEditor.getInstance().setModeToolbar(new ModeToolbar());
        SketchletEditor.getInstance().setPerspectivePanel(new PerspectivePanel());
        SketchletEditor.getInstance().setSpreadsheetPanel(new SpreadsheetPanel());

        SketchletEditor.getInstance().getSketchToolbar().loadButtons();

        final JButton helpMode = new JButton(Workspace.createImageIcon("resources/help-browser.png", ""));
        helpMode.setToolTipText("What is this toolbar?");
        helpMode.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                switch (SketchletEditor.getInstance().getSelectedModesTabIndex()) {
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
        SketchletEditor.getInstance().setPanelNorthMode(new JPanel(new BorderLayout()));

        SketchletEditor.getInstance().getTabsModes().setTabPlacement(JTabbedPane.LEFT);
        SketchletEditor.getInstance().getTabsModes().addTab("", Workspace.createImageIcon("resources/editor.gif"), new JPanel());
        SketchletEditor.getInstance().getTabsModes().setToolTipTextAt(0, Language.translate("Drawing mode (F4)"));

        SketchletEditor.getInstance().getTabsModes().addTab("", Workspace.createImageIcon("resources/start-preview.gif"), new JPanel());
        SketchletEditor.getInstance().getTabsModes().setToolTipTextAt(1, Language.translate("Preview (F5)"));
        SketchletEditor.getInstance().getTabsModes().setPreferredSize(new Dimension(48, 120));
        SketchletEditor.getInstance().getTabsRight().addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                int index = SketchletEditor.getInstance().getTabsRight().getSelectedIndex();
                if (index >= 0) {
                    ActivityLog.log("selectTabRigth", SketchletEditor.getInstance().getTabsRight().getTitleAt(index));
                }
            }
        });
        SketchletEditor.getInstance().loadLayersTab();

        SketchletEditor.getInstance().getTabsLayers().putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(SketchletEditor.getInstance().getTabsLayers());
        SketchletEditor.getInstance().getTabsLayers().setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        SketchletEditor.getInstance().getTabsLayers().setTabPlacement(JTabbedPane.RIGHT);
        SketchletEditor.getInstance().getTabsLayers().addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (SketchletEditor.getInstance().getTool() != null) {
                    SketchletEditor.getInstance().getTool().deactivate();
                }

                int l = SketchletEditor.getInstance().getTabsLayers().getSelectedIndex();
                if (l < Page.NUMBER_OF_LAYERS) {
                    String strProperty = "transparency layer " + (l + 1);
                    String strTransparency = SketchletEditor.getInstance().getCurrentPage().getProperty(strProperty);
                    if (strTransparency == null) {
                        return;
                    }
                    boolean bTransparency = !TemplateMarkers.containsStartMarker(strTransparency) && !strTransparency.startsWith("=");
                    SketchletEditor.getInstance().setLayer(l);

                    if (bTransparency) {
                        try {
                            if (strTransparency.isEmpty()) {
                                strTransparency = SketchletEditor.getInstance().getCurrentPage().getDefaultValue(strProperty);
                            }
                            float t = (float) Double.parseDouble(strTransparency);
                        } catch (Exception e2) {
                        }
                    }
                    SketchletEditor.getInstance().createGraphics();
                }
                ActivityLog.log("setLayer", l + "");
                RefreshTime.update();
                SketchletEditor.getInstance().repaint();

            }
        });

        SketchletEditor.getInstance().getTabsModes().addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                int nTab = SketchletEditor.getInstance().getTabsModes().getSelectedIndex();
                SketchletEditor.getInstance().getSketchToolbar().setEnabled(nTab == 0);
                switch (nTab) {
                    case 0:
                        SketchletEditor.getInstance().resetScale();
                        WidgetPlugin.setActiveWidget(null);
                        if (SketchletEditor.getInstance().getTabsModes().getTabCount() == 3) {
                            SketchletEditor.getInstance().getTabsModes().removeTabAt(2);
                            SketchletEditor.getInstance().setEditorMode(SketchletEditorMode.UNDEFINED);
                        }
                        if (Profiles.isActive("active_regions_layer")) {
                            SketchletEditor.getInstance().setEditorMode(SketchletEditorMode.ACTIONS, 0);
                        } else {
                            SketchletEditor.getInstance().setEditorMode(SketchletEditorMode.SKETCHING, 0);
                        }

                        SketchletEditor.getInstance().getHelpViewer().showAutoHelpByID("editor_mode_drawing");
                        break;
                    case 1:
                        if (SketchletEditor.getInstance().getTabsModes().getTabCount() == 3) {
                            SketchletEditor.getInstance().getTabsModes().removeTabAt(2);
                        }
                        MessageFrame.showMessage(SketchletEditor.editorFrame, "Preparing preview...", SketchletEditor.getInstance().getCentralPanel());
                        EventQueue.invokeLater(new Runnable() {

                            public void run() {
                                try {
                                    SketchletEditor.getInstance().setEditorMode(SketchletEditorMode.PREVIEW, 0);
                                    SketchletEditor.getInstance().getHelpViewer().showAutoHelpByID("editor_mode_preview");
                                    SketchletEditor.getInstance().getActiveRegionMenu().setEnabled(false);
                                    SketchletEditor.getInstance().getFormulaToolbar().enableControls(false, false);
                                    SketchletEditor.getInstance().getInternalPlaybackPanel().requestFocus();
                                } finally {
                                    MessageFrame.closeMessage();
                                }
                            }
                        });
                        break;
                    case 2:
                        SketchletEditor.getInstance().getHelpViewer().showAutoHelpByID("editor_mode_trajectory");
                        SketchletEditor.getInstance().getActiveRegionMenu().setEnabled(false);
                        SketchletEditor.getInstance().getFormulaToolbar().enableControls(false, false);
                        break;
                }

            }
        });
        SketchletEditor.getInstance().setPanelModes(new JPanel(new BorderLayout()));
        if (Profiles.isActive("active_regions_layer")) {
            SketchletEditor.getInstance().getPanelModes().add(SketchletEditor.getInstance().getTabsModes(), BorderLayout.NORTH);
        }
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(SketchletEditor.getInstance().getModeToolbar(), BorderLayout.NORTH);
        SketchletEditor.getInstance().getPanelModes().add(centerPanel, BorderLayout.CENTER);
        SketchletEditor.getInstance().getPanelModes().add(toolbarHelpMode, BorderLayout.SOUTH);

        SketchletEditor.getInstance().getCentralPanel().add(SketchletEditor.getInstance().getPanelModes(), BorderLayout.WEST);

        SketchletEditor.getInstance().getMainSketchPanel().add(SketchletEditor.getInstance().getPanelNorthMode(), BorderLayout.WEST);
        SketchletEditor.getInstance().getMainSketchPanel().add(SketchletEditor.getInstance().scrollPane);

        JPanel panelLayers = new JPanel();
        panelLayers.setLayout(new BoxLayout(panelLayers, BoxLayout.Y_AXIS));
        panelLayers.add(SketchletEditor.getInstance().getTabsLayers());
        SketchletEditor.getInstance().getTabsLayers().setSize(new Dimension(50, 300));
        SketchletEditor.getInstance().getTabsLayers().setPreferredSize(new Dimension(50, 300));

        SketchletEditor.getInstance().getDrawingPanel().add(panelLayers, BorderLayout.EAST);
        SketchletEditor.getInstance().getMainSketchPanel().add(SketchletEditor.getInstance().getActiveRegionToolbar(), BorderLayout.SOUTH);

        SketchletEditor.getInstance().getDrawingPanel().add(SketchletEditor.getInstance().getMainSketchPanel());
        SketchletEditor.getInstance().setPanelDrawingPanel(new JPanel(new BorderLayout()));
        JPanel panelDrawingPanelLeft = new JPanel(new BorderLayout());
        panelDrawingPanelLeft.add(SketchletEditor.getInstance().getDrawingPanel(), BorderLayout.CENTER);
        SketchletEditor.getInstance().getPanelDrawingPanel().add(panelDrawingPanelLeft, BorderLayout.CENTER);

        SketchletEditor.getInstance().getEditorPane().add(SketchletEditor.getInstance().getPanelDrawingPanel());

        SketchletEditor.getInstance().setPageListPanel(new PageListPanel());
        SketchletEditor.getInstance().getCentralPanel().add(SketchletEditor.getInstance().getEditorPane());
        SketchletEditor.getInstance().setControlPanel(new SimplePagesNavigationPanel());
        SketchletEditor.getInstance().getCentralPanel().add(SketchletEditor.getInstance().getControlPanel(), BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        BorderLayout bl = new BorderLayout();
        bl.setVgap(0);
        SketchletEditor.getInstance().setStatusPanel(new JPanel(bl));
        JToolBar profilePanel = new JToolBar();
        profilePanel.setFloatable(false);

        JMenuBar mbprofiles = new JMenuBar();
        SketchletEditor.getInstance().setProfilesMenu(Profiles.getMenu());
        mbprofiles.add(SketchletEditor.getInstance().getProfilesMenu());
        profilePanel.add(mbprofiles);

        SketchletEditor.getInstance().getStatusPanel().add(profilePanel, BorderLayout.WEST);
        SketchletEditor.getInstance().getStatusPanel().add(SketchletEditor.getStatusBar(), BorderLayout.CENTER);
        if (GlobalProperties.get("memory-monitor", "false").equalsIgnoreCase("true")) {
            SketchletEditor.getInstance().addMemoryPanel();
        }
        SketchletEditor.getInstance().setSnapToGrid(GlobalProperties.get("snap-to-grid", "false").equalsIgnoreCase("true"));

        bottomPanel.add(SketchletEditor.getInstance().getStatusPanel(), BorderLayout.SOUTH);

        final JTabbedPane tabsVariables = new JTabbedPane();
        tabsVariables.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                int index = tabsVariables.getSelectedIndex();
                if (index >= 0) {
                    ActivityLog.log("selectTabNavigator", tabsVariables.getTitleAt(index));
                }
            }
        });
        tabsVariables.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(tabsVariables);

        SketchletEditor.editorFrame.getContentPane().add(bottomPanel, "South");
        SketchletEditor.getInstance().getTabsRight().setPreferredSize(new Dimension(250, 450));

        JComponent rightComponent;
        if (Profiles.isActiveAny("variables,io_services,timers,macros,scripts,screen_scripts")) {
            SketchletEditor.getInstance().getTabsNavigator().addTab(Language.translate("Pages"), SketchletEditor.getInstance().getPageListPanel());
            SketchletEditor.getInstance().getTabsNavigator().addTab(Language.translate("Global Objects"), SketchletEditor.getInstance().getTabsRight());
            rightComponent = SketchletEditor.getInstance().getTabsNavigator();
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(SketchletEditor.getInstance().getTabsNavigator());
            rightComponent = panel;
        } else {
            rightComponent = SketchletEditor.getInstance().getPageListPanel();
        }

        SketchletEditor.getInstance().setTabsBrowser(new JTabbedPane());
        SketchletEditor.getInstance().getTabsBrowser().setPreferredSize(new Dimension(280, 200));
        SketchletEditor.getInstance().getTabsBrowser().putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(SketchletEditor.getInstance().getTabsBrowser());
        SketchletEditor.getInstance().getTabsBrowser().setTabPlacement(JTabbedPane.BOTTOM);

        SketchletEditor.getInstance().setHelpViewer(new HelpViewer());

        fillTabsBrowser(rightComponent);

        SketchletEditor.getInstance().getHelpViewer().showAutoHelpByID("editor_mode_drawing");

        SketchletEditor.getInstance().setSpllitPane(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, SketchletEditor.getInstance().getCentralPanel(), SketchletEditor.getInstance().getTabsBrowser()));

        SketchletEditor.getInstance().getSpllitPane().setResizeWeight(1);
        SketchletEditor.editorFrame.getContentPane().add(SketchletEditor.getInstance().getSpllitPane(), BorderLayout.CENTER);

        SketchletEditor.getInstance().getNavigationToolbar().setOrientation(JToolBar.VERTICAL);
        SketchletEditor.getInstance().getNavigationToolbar().setFloatable(false);

        JPanel panelEastBottom = new JPanel(new BorderLayout());

        panelEastBottom.add(SketchletEditor.getInstance().getNavigationToolbar(), BorderLayout.NORTH);

        SketchletEditor.editorFrame.getContentPane().add(panelEastBottom, BorderLayout.EAST);

        SketchletEditor.getInstance().setPageDetailsPanel(new PageDetailsPanel());

        SketchletEditor.getInstance().setExtraEditorPanel(new ExtraEditorPanel());

        Workspace.getMainPanel().createToolbar(Workspace.getMainPanel().getMainFrameToolbar(), "toolbarVariables");
        Workspace.getMainPanel().getMainFrameToolbar().setOrientation(JToolBar.VERTICAL);

        loadTabsRight();
        loadNavigationButtons();

        SketchletEditor.getInstance().getSketchToolbar().prepareZoomBox();
    }

    private static void loadNavigationButtons() {
        SketchletEditor.getInstance().getNavigationToolbar().removeAll();
        SketchletEditor.getInstance().getHideButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setRigthPanelVisible(!SketchletEditor.getInstance().isObjectPanelVisible());
            }
        });
        SketchletEditor.getInstance().getNavigationToolbar().add(SketchletEditor.getInstance().getHideButton());
        SketchletEditor.getInstance().getNavigationToolbar().addSeparator();

        VerticalButton ab;
        ab = new VerticalButton(Language.translate("Pages & Variables"), Workspace.createImageIcon("resources/icon_other_page.gif"));
        SketchletEditor.getInstance().getNavigationToolbar().add(ab);
        SketchletEditor.getInstance().getNavigationToolbar().addSeparator();
        ab.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectTabs(0);
            }
        });

        if (Profiles.isActive("io_services")) {
            ab = new VerticalButton(Language.translate("I/O Services"), Workspace.createImageIcon("resources/io_services.png"));
            SketchletEditor.getInstance().getNavigationToolbar().add(ab);
            SketchletEditor.getInstance().getNavigationToolbar().addSeparator();
            ab.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectTabs(1, SketchletEditor.getIoservicesTabIndex());
                }
            });
        }
        if (Profiles.isActive("timers")) {
            ab = new VerticalButton(Language.translate("Timers"), Workspace.createImageIcon("resources/timer_small.png"));
            SketchletEditor.getInstance().getNavigationToolbar().add(ab);
            SketchletEditor.getInstance().getNavigationToolbar().addSeparator();
            ab.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectTabs(1, SketchletEditor.getProgrammingTabIndex(), SketchletEditor.getTimersTabIndex());
                }
            });
        }
        if (Profiles.isActive("macros")) {
            ab = new VerticalButton(Language.translate("Actions"), Workspace.createImageIcon("resources/programming-object.png"));
            SketchletEditor.getInstance().getNavigationToolbar().add(ab);
            SketchletEditor.getInstance().getNavigationToolbar().addSeparator();
            ab.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectTabs(1, SketchletEditor.getProgrammingTabIndex(), SketchletEditor.getMacrosTabIndex());
                }
            });
        }
        if (Profiles.isActive("screen_scripts")) {
            ab = new VerticalButton(Language.translate("Screen Scripts"), Workspace.createImageIcon("resources/mouse.gif"));
            SketchletEditor.getInstance().getNavigationToolbar().add(ab);
            SketchletEditor.getInstance().getNavigationToolbar().addSeparator();
            ab.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectTabs(1, SketchletEditor.getProgrammingTabIndex(), SketchletEditor.getScreenpokingTabIndex());
                }
            });
        }
        if (Profiles.isActive("scripts")) {
            ab = new VerticalButton(Language.translate("Scripts"), Workspace.createImageIcon("resources/code.gif"));
            SketchletEditor.getInstance().getNavigationToolbar().add(ab);
            SketchletEditor.getInstance().getNavigationToolbar().addSeparator();
            ab.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectTabs(1, SketchletEditor.getProgrammingTabIndex(), SketchletEditor.getScriptsTabIndex());
                }
            });
        }
    }

    private static void selectTabs(int tabIndex, int tabSubIndex, int tabSubSubIndex) {
        selectTabs(tabIndex, tabSubIndex);
        SketchletEditor.getInstance().getTabsProgramming().setSelectedIndex(tabSubSubIndex);
    }

    private static void selectTabs(int tabIndex, int tabSubIndex) {
        selectTabs(tabIndex);
        SketchletEditor.getInstance().getTabsRight().setSelectedIndex(tabSubIndex);
    }

    private static void selectTabs(int tabIndex) {
        if (!SketchletEditor.getInstance().isTabsVisible()) {
            SketchletEditor.getInstance().getSketchToolbar().showHideProjectNavigator();
        }
        setRigthPanelVisible(true);
        SketchletEditor.getInstance().getTabsNavigator().setSelectedIndex(tabIndex);
        SketchletEditor.getInstance().getTabsBrowser().setSelectedIndex(0);
    }

    private static void setRigthPanelVisible(boolean bVisible) {
        SketchletEditor.getInstance().setObjectPanelVisible(bVisible);
        if (!bVisible) {
            SketchletEditor.getInstance().getHideButton().setIcon(Workspace.createImageIcon("resources/hide-left-icon.png"));
            SketchletEditor.getInstance().getSpllitPane().remove(SketchletEditor.getInstance().getTabsBrowser());
        } else {
            SketchletEditor.getInstance().getHideButton().setIcon(Workspace.createImageIcon("resources/hide-right-icon.png"));
            SketchletEditor.getInstance().getSpllitPane().setRightComponent(SketchletEditor.getInstance().getTabsBrowser());
        }
    }

    public static void loadTabsRight() {
        SketchletEditor.getInstance().getTabsRight().removeAll();
        SketchletEditor.getInstance().getTabsProgramming().removeAll();

        SketchletEditor.setPageTabIndex(SketchletEditor.getInstance().getTabsRight().getTabCount());
        if (Profiles.isActive("io_services")) {
            SketchletEditor.setIoservicesTabIndex(SketchletEditor.getInstance().getTabsRight().getTabCount());
            SketchletEditor.getInstance().getTabsRight().addTab("", Workspace.createImageIcon("resources/service.gif"), Workspace.getMainPanel().getPanelProcesses(), Language.translate("I/O Services"));
            Workspace.getMainPanel().getTableModules().setDragEnabled(true);
            Workspace.getMainPanel().getTableModules().setTransferHandler(new GenericTableTransferHandler("@macro Service:", 0));
        }

        SketchletEditor.getInstance().setTimersTablePanel(new TimersTablePanel());
        SketchletEditor.getInstance().setMacrosTablePanel(new MacrosTablePanel());
        if (Profiles.isActive("timers")) {
            SketchletEditor.setTimersTabIndex(SketchletEditor.getInstance().getTabsProgramming().getTabCount());
            SketchletEditor.getInstance().getTabsProgramming().addTab("", Workspace.createImageIcon("resources/timer.png"), SketchletEditor.getInstance().getTimersTablePanel(), Language.translate("Timers"));
        }
        if (Profiles.isActiveAny("timers,macros,screen_poking,scripts")) {
            SketchletEditor.getInstance().getTabsProgramming().putClientProperty("JComponent.sizeVariant", "small");
            SwingUtilities.updateComponentTreeUI(SketchletEditor.getInstance().getTabsProgramming());
            SketchletEditor.setProgrammingTabIndex(SketchletEditor.getInstance().getTabsRight().getTabCount());
            SketchletEditor.getInstance().getTabsRight().addTab("", Workspace.createImageIcon("resources/programming.png"), SketchletEditor.getInstance().getTabsProgramming(), Language.translate("Programming objects (timers, macros, screen poking, scripts"));
        }
        if (Profiles.isActive("macros")) {
            SketchletEditor.setMacrosTabIndex(SketchletEditor.getInstance().getTabsProgramming().getTabCount());
            SketchletEditor.getInstance().getTabsProgramming().addTab("", Workspace.createImageIcon("resources/macros.png"), SketchletEditor.getInstance().getMacrosTablePanel(), Language.translate("Action Lists"));
        }
        if (Profiles.isActive("screen_poking")) {
            SketchletEditor.setScreenpokingTabIndex(SketchletEditor.getInstance().getTabsProgramming().getTabCount());
            SketchletEditor.getInstance().getTabsProgramming().addTab("", Workspace.createImageIcon("resources/mouse.png"), ScreenScripts.createScreenScripts(false), Language.translate("Screen Poking"));
        }
        if (Workspace.getMainPanel().getSketchletPanel().panel2 != null) {
            Workspace.getMainPanel().getSketchletPanel().panel2.table.setDragEnabled(true);
            Workspace.getMainPanel().getSketchletPanel().panel2.table.setTransferHandler(new GenericTableTransferHandler("@macro Script:", 0));
            if (Profiles.isActive("scripts")) {
                SketchletEditor.setScriptsTabIndex(SketchletEditor.getInstance().getTabsProgramming().getTabCount());
                SketchletEditor.getInstance().getTabsProgramming().addTab("", Workspace.createImageIcon("resources/script.png"), Workspace.getMainPanel().getSketchletPanel().panel2, Language.translate("Scripts"));
            }
        }

        SketchletEditor.getInstance().getTabsRight().setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        SketchletEditor.getInstance().getTabsRight().setTabPlacement(JTabbedPane.TOP);
    }

    public static void fillTabsBrowser(Component rightComponent) {
        if (SketchletEditor.getInstance() != null && SketchletEditor.getInstance().getTabsBrowser() != null) {
            SketchletEditor.getInstance().getTabsBrowser().removeAll();
            SketchletEditor.getInstance().getTabsBrowser().addTab(Language.translate("Project Navigator"), rightComponent);
            SketchletEditor.getInstance().getTabsBrowser().addTab(Language.translate("Help"), SketchletEditor.getInstance().getHelpViewer());
        }
    }

    public static void onLanguageChange() {
        MessageFrame.showMessage(Workspace.getMainFrame(), Language.translate("Please wait..."), Workspace.getMainFrame());
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                try {
                    SketchletEditor.close();
                    Workspace.getMainPanel().getSketchletPanel().globalVariablesPanel.variablesTableModel.fireTableStructureChanged();
                    Workspace.getMainPanel().getSketchletPanel().panel2.scriptsTableModel.fireTableStructureChanged();
                    if (ScreenScripts.getScreenScriptsPanel() != null && ScreenScripts.getScreenScriptsPanel().getModel() != null) {
                        ScreenScripts.getScreenScriptsPanel().getModel().fireTableStructureChanged();
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
        Workspace.getMainPanel().setMenuBar(Workspace.getMainPanel().createMenubar("menubar"));
        SketchletEditor.editorFrame.setJMenuBar(Workspace.getMainPanel().getMenuBar());
        SketchletEditor.getInstance().getPanelModes().remove(SketchletEditor.getInstance().getTabsModes());
        if (Profiles.isActive("active_regions_layer")) {
            SketchletEditor.getInstance().getPanelModes().add(SketchletEditor.getInstance().getTabsModes(), BorderLayout.NORTH);
            SketchletEditor.getInstance().setEditorMode(SketchletEditorMode.ACTIONS, 0);
        } else {
            SketchletEditor.getInstance().getTabsModes().setSelectedIndex(0);
            SketchletEditor.getInstance().setEditorMode(SketchletEditorMode.SKETCHING, 0);
        }
        SketchletEditor.getInstance().getPanelModes().revalidate();
        SketchletEditor.getInstance().getSketchToolbar().loadButtons();
        Workspace.getMainPanel().createToolbar(Workspace.getMainPanel().getMainFrameToolbar(), "toolbarVariables");
        Workspace.getMainPanel().getMainFrameToolbar().revalidate();

        loadTabsRight();
        loadNavigationButtons();
        JComponent rightComponent;
        if (Profiles.isActiveAny("variables,io_services,timers,macros,scripts,screen_scripts")) {
            SketchletEditor.getInstance().getTabsNavigator().removeAll();
            SketchletEditor.getInstance().getTabsNavigator().addTab(Language.translate("Pages"), SketchletEditor.getInstance().getPageListPanel());
            SketchletEditor.getInstance().getTabsNavigator().addTab(Language.translate("Global Objects"), SketchletEditor.getInstance().getTabsRight());
            rightComponent = SketchletEditor.getInstance().getTabsNavigator();
        } else {
            rightComponent = SketchletEditor.getInstance().getPageListPanel();
        }

        SketchletEditor.getInstance().getTabsBrowser().setComponentAt(0, rightComponent);
        fillTabsBrowser(rightComponent);
        SketchletEditor.getInstance().getTabsBrowser().revalidate();

        if (SketchletEditor.getInstance().getExtraEditorPanel() != null) {
            SketchletEditor.getInstance().getExtraEditorPanel().createTabs();
        }

        SketchletEditor.getInstance().getPageListPanel().reloadToolbar();

        ActiveRegionsFrame.reload();
    }
}
