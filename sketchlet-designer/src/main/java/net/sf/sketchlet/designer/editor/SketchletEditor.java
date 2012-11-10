/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import net.sf.sketchlet.common.Refresh;
import net.sf.sketchlet.common.awt.AWTUtilitiesWrapper;
import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.context.SketchletPainter;
import net.sf.sketchlet.context.VariableUpdateListener;
import net.sf.sketchlet.designer.GlobalProperties;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.context.ActiveRegionContextImpl;
import net.sf.sketchlet.designer.context.PageContextImpl;
import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.data.ActiveRegions;
import net.sf.sketchlet.designer.data.EventMacro;
import net.sf.sketchlet.designer.data.KeyboardEventMacro;
import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.designer.data.Pages;
import net.sf.sketchlet.designer.data.SketchletsSaxLoader;
import net.sf.sketchlet.designer.data.VariableUpdateEventMacro;
import net.sf.sketchlet.designer.editor.regions.ActiveRegionImageEditor;
import net.sf.sketchlet.designer.editor.regions.ActiveRegionMenu;
import net.sf.sketchlet.designer.editor.regions.ActiveRegionPopupListener;
import net.sf.sketchlet.designer.editor.renderer.PageRenderer;
import net.sf.sketchlet.designer.editor.renderer.RendererInterface;
import net.sf.sketchlet.designer.editor.resize.ResizeDialog;
import net.sf.sketchlet.designer.editor.resize.ResizeInterface;
import net.sf.sketchlet.designer.editor.tool.*;
import net.sf.sketchlet.designer.editor.tool.notes.NoteDialog;
import net.sf.sketchlet.designer.editor.tool.stroke.WobbleStroke;
import net.sf.sketchlet.designer.eye.eye.EyeFrame;
import net.sf.sketchlet.designer.help.HelpViewer;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.designer.programming.macros.Macro;
import net.sf.sketchlet.designer.programming.macros.Macros;
import net.sf.sketchlet.designer.programming.timers.Timer;
import net.sf.sketchlet.designer.programming.timers.Timers;
import net.sf.sketchlet.designer.tools.imagecache.ImageCache;
import net.sf.sketchlet.designer.tools.log.ActivityLog;
import net.sf.sketchlet.designer.ui.LayerCheckBoxTabComponent;
import net.sf.sketchlet.designer.ui.MemoryPanel;
import net.sf.sketchlet.designer.ui.MessageFrame;
import net.sf.sketchlet.designer.ui.datasource.VariableSpacesFrame;
import net.sf.sketchlet.designer.ui.desktop.DesktopPanel;
import net.sf.sketchlet.designer.ui.desktop.SystemVariablesDialog;
import net.sf.sketchlet.designer.ui.extraeditor.ActiveRegionsExtraPanel;
import net.sf.sketchlet.designer.ui.extraeditor.ExtraEditorPanel;
import net.sf.sketchlet.designer.ui.localvars.PageVariablesPanel;
import net.sf.sketchlet.designer.ui.macros.ImageAreaSelect;
import net.sf.sketchlet.designer.ui.macros.MacrosFrame;
import net.sf.sketchlet.designer.ui.macros.MacrosTablePanel;
import net.sf.sketchlet.designer.ui.page.SketchListPanel;
import net.sf.sketchlet.designer.ui.page.SketchStatePanel;
import net.sf.sketchlet.designer.ui.page.perspective.PerspectivePanel;
import net.sf.sketchlet.designer.ui.page.spreadsheet.SpreadsheetPanel;
import net.sf.sketchlet.designer.ui.pagetransition.StateDiagram;
import net.sf.sketchlet.designer.ui.playback.InteractionRecorder;
import net.sf.sketchlet.designer.ui.playback.PlaybackFrame;
import net.sf.sketchlet.designer.ui.playback.PlaybackPanel;
import net.sf.sketchlet.designer.ui.playback.displays.InteractionSpaceFrame;
import net.sf.sketchlet.designer.ui.profiles.Profiles;
import net.sf.sketchlet.designer.ui.region.ActiveRegionPanel;
import net.sf.sketchlet.designer.ui.region.ActiveRegionToolbar;
import net.sf.sketchlet.designer.ui.region.ActiveRegionsFrame;
import net.sf.sketchlet.designer.ui.timers.TimersTablePanel;
import net.sf.sketchlet.designer.ui.timers.curve.CurvesFrame;
import net.sf.sketchlet.designer.ui.toolbars.ColorToolbar;
import net.sf.sketchlet.designer.ui.toolbars.FormulaToolbar;
import net.sf.sketchlet.designer.ui.toolbars.ModeToolbar;
import net.sf.sketchlet.designer.ui.toolbars.SimplePagesNavigationPanel;
import net.sf.sketchlet.designer.ui.toolbars.SketchToolbar;
import net.sf.sketchlet.designer.ui.undo.*;
import net.sf.sketchlet.help.HelpInterface;
import net.sf.sketchlet.help.HelpUtils;
import net.sf.sketchlet.plugin.SketchletPluginGUI;
import net.sf.sketchlet.pluginloader.GenericPluginFactory;
import net.sf.sketchlet.pluginloader.PluginInstance;
import net.sf.sketchlet.pluginloader.WidgetPluginFactory;
import net.sf.sketchlet.util.RefreshTime;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Vector;

public class SketchletEditor extends JDesktopPane implements KeyListener, VariableUpdateListener, Refresh, ToolInterface, ResizeInterface, RendererInterface, HelpInterface, SketchletPainter {
    private static final Logger log = Logger.getLogger(SketchletEditor.class);
    public PageRenderer renderer = new PageRenderer(this);
    FileDrop fileDrop;
    public static Pages pages;
    public Page currentPage;
    public Page masterPage;
    public EditorMode mode = EditorMode.SKETCHING;
    public JTabbedPane tabsModes = new JTabbedPane();
    public JTabbedPane tabsLayers = new JTabbedPane();
    public JSlider sliderLayerTransparency;
    // public JTabbedPane tabs = new JTabbedPane();
    public JTextArea textArea = new JTextArea();
    public static SketchletEditor editorPanel;
    public JToolBar navigationToolbar = new JToolBar();
    public static double transparencyFactor = 1.0;
    public int layer = 0;
    public static JFileChooser fc = new JFileChooser();
    public boolean bDragging = false;
    public Graphics2D g2FreeHandDraw = null;
    public ActiveRegionMenu activeRegionMenu = new ActiveRegionMenu();
    public boolean inShiftMode = false;
    public boolean inCtrlMode = false;
    public SimplePagesNavigationPanel controlPanel;
    public JPanel centralPanel = new JPanel(new BorderLayout());
    public PenTool penTool = new PenTool(this);
    public ColorPickerTool colorPickerTool = new ColorPickerTool(this);
    public BucketTool bucketTool = new BucketTool(this);
    public TransparentColorTool transparentColorTool = new TransparentColorTool(this);
    public MagicWandTool magicWandTool = new MagicWandTool(this);
    public LineTool lineTool = new LineTool(this);
    public RectTool rectTool = new RectTool(this);
    public OvalTool ovalTool = new OvalTool(this);
    public EraserTool eraserTool = new EraserTool(this);
    public SelectTool selectTool = new SelectTool(this);
    public FreeFormSelectTool freeFormSelectTool = new FreeFormSelectTool(this);
    public ActiveRegionTool activeRegionTool = new ActiveRegionTool(this);
    public ActiveRegionConnectorTool activeRegionConnectorTool = new ActiveRegionConnectorTool(this);
    public TrajectoryPointsTool trajectoryPointsTool = new TrajectoryPointsTool(this);
    public TrajectoryPenTool trajectory1PointsTool = new TrajectoryPenTool(this, 1);
    public TrajectoryPenTool trajectory2PointsTool = new TrajectoryPenTool(this, 2);
    public TrajectoryMoveTool trajectoryMoveTool = new TrajectoryMoveTool(this);
    public ActiveRegionSelectTool activeRegionSelectTool = new ActiveRegionSelectTool(this);
    public PostNoteTool postNoteTool = new PostNoteTool(this);
    public Tool tool = activeRegionSelectTool;
    public ActiveRegionPopupListener regionPopupListener = new ActiveRegionPopupListener();
    public ModeToolbar modeToolbar;
    public PerspectivePanel perspectivePanel;
    public PageVariablesPanel pageVariablesPanel;
    public SpreadsheetPanel spreadsheetPanel;
    public SketchStatePanel statePanel;
    public ExtraEditorPanel extraEditorPanel;
    JPanel panelNorthMode;
    public ColorToolbar colorToolbar;
    public ActiveRegionToolbar activeRegionToolbar;
    public FormulaToolbar formulaToolbar;
    public SketchToolbar sketchToolbar = new SketchToolbar();
    public JTabbedPane tabsRight = new JTabbedPane();
    public JTabbedPane tabsProgramming = new JTabbedPane();
    public TimersTablePanel timersTablePanel;
    public MacrosTablePanel macrosTablePanel;
    JSplitPane splitRight;
    public JTabbedPane tabsBrowser;
    public JTabbedPane tabsNavigator = new JTabbedPane();
    public boolean tabsVisible = true;
    public static boolean snapToGrid = false;
    public static boolean showPerspectiveLines = false;
    public static boolean bShowStatePanel = false;
    public BufferedImage masterImage;
    public double scale = 1.0;
    public double oldScale = 0.0;
    public static int margin = 0;
    public static int marginX = 0;
    public static int marginY = 0;
    public Color color;
    public Stroke stroke;
    public int outlineType = 0;
    public float watering = 1.0f;
    int statusX = 0;
    int statusY = 0;
    JPanel panelDrawingPanel;
    public JPanel editorPane = new JPanel(new BorderLayout());
    public JSplitPane splitPane;
    public SketchListPanel sketchListPanel;
    // public JTabbedPane drawingTabs = new JTabbedPane();
    public JPanel drawingPanel = new JPanel(new BorderLayout());
    public JPanel mainSketchPanel = new JPanel(new BorderLayout());
    public PlaybackPanel internalPlaybackPanel;
    public JScrollPane internalPlaybackPanelScrollPane;
    public boolean bExtraEditorPanelVisible = false;
    public Rule rulerHorizontal = new Rule(Rule.HORIZONTAL, true);
    public Rule rulerVertical = new Rule(Rule.VERTICAL, true);
    JPanel statusPanel;
    //public ObjectBrowser objectBrowser;
    public HelpViewer helpViewer;
    JSplitPane spllitPane;
    public TutorialPanel tutorialPanel;
    public static int pageTabIndex = 0;
    public static int ioservicesTabIndex = 1;
    public static int programmingTabIndex = 2;
    public static int timersTabIndex = 0;
    public static int macrosTabIndex = 1;
    public static int screenpokingTabIndex = 2;
    public static int scriptsTabIndex = 3;
    JPanel panelModes;
    public static String[][] initProperties = null;
    public JMenu profilesMenu;
    boolean bCloseMsg = false;
    public boolean bLoading = false;
    public Vector<Page> navigationHistory = new Vector<Page>();
    public static boolean bStatePanelUndocked = false;
    public static JFrame undockStateFrame = null;
    public boolean bRepaint = false;
    long lastRepaintTime = 0;
    public static boolean bGUIReady = false;
    public SketchletEditorClipboard editorClipboard = new SketchletEditorClipboard(this);
    public SketchletEditorImages sketchletImages = new SketchletEditorImages(this);
    public List<UndoAction> undoRegionActions;
    public JButton hide = new JButton(Workspace.createImageIcon("resources/hide-right-icon.png"));
    public boolean objectPanelVisible = true;

    private ImportMediaWatcher mediaWatcher;

    public SketchletEditor() {
        ActivityLog.resetTime();
        this.undoRegionActions = new Vector();
        new Thread(new Runnable() {

            public void run() {
                try {
                    while (!bClosing) {
                        repaintIfNeeded();
                        Thread.sleep(50);
                    }
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }).start();
        bShowStatePanel = false;
        initSketches();

        HelpUtils.helpInterface = this;

        color = Color.BLACK;
        stroke = new WobbleStroke(3, 1, 1);

        InteractionSpace.load();

        SketchletEditorMouseListener listener = new SketchletEditorMouseListener();
        this.addMouseListener(listener);
        this.addMouseMotionListener(listener);

        this.addMouseListener(regionPopupListener);

        this.addKeyListener(this);
        setFocusable(true);

        SketchletEditor.statusBar.setText("Sketching mode");

        DataServer.variablesServer.addVariablesUpdateListener(this);
        setCursor();

        this.setDesktopManager(new NoteDesktopManager());
        UIManager.put("InternalFrame.titlePaneHeight", new Integer(16));
        UIManager.put("InternalFrame.titleButtonHeight", new Integer(16));
        UIManager.put("InternalFrame.closeButtonToolTip", "Delete note");

        this.putClientProperty("JComponent.sizeVariant", "small");
        this.tabsRight.putClientProperty("JComponent.sizeVariant", "small");

        TutorialPanel.prepare(tabsRight);

        SwingUtilities.updateComponentTreeUI(this);
        Workspace.prepareAdditionalVariables();

        mediaWatcher = new ImportMediaWatcher();
    }

    public void update(Graphics g) {
        if (renderer != null) {
            renderer.prepare(false, true);
        }
        if (System.currentTimeMillis() - lastRepaintTime > 50) {
            super.update(g);
            lastRepaintTime = System.currentTimeMillis();
        }
        PlaybackPanel.repaintCounter++;
    }

    public void repaint() {
        if (RefreshTime.shouldRefresh()) {
            bRepaint = true;
        }
    }

    public void repaintIfNeeded() {
        if (PlaybackFrame.playbackFrame != null) {
            PlaybackFrame.repaintAllFramesIfNeeded();
            bRepaint = true;
        } else if (this.internalPlaybackPanel != null) {
            this.internalPlaybackPanel.repaintIfNeeded();
            refreshTables();
        } else {
            if (bRepaint) {
                bRepaint = false;
                super.repaint();

                if (rulerHorizontal != null) {
                    rulerHorizontal.repaint();
                }
                if (rulerVertical != null) {
                    rulerVertical.repaint();
                }
                PlaybackPanel.repaintCounter++;
            }
            refreshTables();
        }
    }

    public void refreshTables() {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                if (Workspace.mainPanel.sketchletPanel.globalVariablesPanel.bTableDirty) {
                    Workspace.mainPanel.sketchletPanel.globalVariablesPanel.bTableDirty = false;
                    Workspace.mainPanel.sketchletPanel.globalVariablesPanel.variablesTableModel.fireTableDataChanged();
                    PlaybackPanel.repaintCounter++;
                }
                if (Workspace.mainPanel.sketchletPanel.globalVariablesPanel.bTableUpdated) {
                    Workspace.mainPanel.sketchletPanel.globalVariablesPanel.table.repaint();
                    Workspace.mainPanel.sketchletPanel.globalVariablesPanel.bTableUpdated = false;
                    PlaybackPanel.repaintCounter++;
                }
            }
        });
    }

    public void addNote(int x, int y) {
        NoteDialog note = new NoteDialog(editorPanel.marginX + x - 8, editorPanel.marginY + y - 33);
        currentPage.notes.add(note);
        addAndShowNote(note);
    }

    public void addAndShowNote(NoteDialog note) {
        NoteDialog _note = new NoteDialog(note.getX(), note.getY());
        _note.setBounds(note.getX(), note.getY(), note.getWidth(), note.getHeight());
        _note.noteTextArea.setText(note.noteTextArea.getText());
        currentPage.notes.remove(note);
        currentPage.notes.add(_note);
        remove(note);
        add(_note, JDesktopPane.PALETTE_LAYER);
        _note.setVisible(true);
        if (note.isMinimized) {
            _note.isMinimized = true;

            this.getDesktopManager().iconifyFrame(_note);
            try {
                _note.setIcon(true);
            } catch (Throwable e) {
                log.error(e);
            }
            _note.original_w = note.original_w;
            _note.original_h = note.original_h;
        }
    }

    public void removeNote(NoteDialog note) {
        note.setVisible(false);
        this.remove(note);
    }

    public void loadLayersTab() {
        editorPanel.tabsLayers.removeAll();

        int i = 0;
        for (i = 0; i < Page.NUMBER_OF_LAYERS; i++) {
            editorPanel.tabsLayers.add("" + (i + 1), new JPanel());
            editorPanel.tabsLayers.setTabComponentAt(i, new LayerCheckBoxTabComponent(i, editorPanel.tabsLayers));
        }
        editorPanel.tabsLayers.setSelectedIndex(0);
    }

    public static void showPerspectivePanel() {
        showStatePanel(SketchStatePanel.perspectiveTabIndex, 0);
    }

    public static void showSpreadsheetPanel() {
        showStatePanel(SketchStatePanel.spreadsheetTabIndex, 0);
    }

    public static void showStatePanel(int tab, int subTab) {
        showExtraEditorPanel(ExtraEditorPanel.indexPage);

        editorPanel.statePanel.tabs.setSelectedIndex(tab);
        if (tab == 0) {
            editorPanel.statePanel.tabs1.setSelectedIndex(subTab);
        } else if (tab == 1) {
            editorPanel.statePanel.tabs2.setSelectedIndex(subTab);
        } else {
        }
    }

    public static void undockStatePanel() {
        showStatePanel();
        undockStateFrame = new JFrame();
        undockStateFrame.getRootPane().putClientProperty("Window.style", "small");
        undockStateFrame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                bStatePanelUndocked = false;
                editorPanel.bShowStatePanel = false;
            }

            public void windowActivated(WindowEvent e) {
                AWTUtilitiesWrapper.setWindowOpacity(undockStateFrame, 1.0f);
            }

            public void windowDeactivated(WindowEvent e) {
                AWTUtilitiesWrapper.setWindowOpacity(undockStateFrame, 0.5f);
            }
        });

        undockStateFrame.setAlwaysOnTop(true);
        undockStateFrame.setTitle("Page Events and Propertites");
        undockStateFrame.add(editorPanel.statePanel);
        Dimension d = editorPanel.statePanel.getSize();
        undockStateFrame.setSize(new Dimension((int) d.getWidth(), (int) d.getHeight() + 60));
        undockStateFrame.setVisible(true);
        undockStateFrame.setIconImage(Workspace.createImageIcon("resources/editor.gif", "").getImage());
        bUndocked = true;
    }

    public static void dockStatePanel() {
        bStatePanelUndocked = false;
        if (undockStateFrame != null) {
            undockStateFrame.setVisible(false);
            editorPanel.bShowStatePanel = false;
            showStatePanel();
        }
    }

    public static void showStatePanel() {
    }

    class NoteDesktopManager extends DefaultDesktopManager {

        public void iconifyFrame(JInternalFrame f) {
            int x = f.getX();
            int y = f.getY();
            if (f instanceof NoteDialog) {
                ((NoteDialog) f).original_w = f.getWidth();
                ((NoteDialog) f).original_h = f.getHeight();
                ((NoteDialog) f).isMinimized = true;
            }
            f.setTitle("...");
            f.setBorder(BorderFactory.createCompoundBorder());
            f.setBounds(x, y, 75, 23);
        }

        public void deiconifyFrame(JInternalFrame f) {
            int x = f.getX();
            int y = f.getY();
            f.setTitle("");
            f.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
            if (f instanceof NoteDialog) {
                ((NoteDialog) f).isMinimized = false;
                f.setBounds(x, y, ((NoteDialog) f).original_w, ((NoteDialog) f).original_h);
            }
        }
    }

    public void reloadPlay() {
        if (PlaybackFrame.playbackFrame != null) {
            PlaybackFrame.play(this.pages, currentPage);
        }
        if (editorPanel != null && editorPanel.currentPage != null && editorPanel.internalPlaybackPanel != null) {
            updateTables();
            save();

            editorPanel.internalPlaybackPanel.showSketch(editorPanel.currentPage);
            editorPanel.internalPlaybackPanel.revalidate();
            RefreshTime.update();
            editorPanel.internalPlaybackPanel.repaint();
            editorPanel.internalPlaybackPanelScrollPane.revalidate();
        }
    }

    public void refreshPlay() {
        RefreshTime.update();
        if (PlaybackFrame.playbackFrame != null) {
            for (int i = 0; i < PlaybackFrame.playbackFrame.length; i++) {
                PlaybackFrame.playbackFrame[i].playbackPanel.repaint();
            }
        }
    }

    public void setTool(Tool tool, Component source) {
        if (this.tool != null) {
            this.tool.deactivate();
        }

        ActivityLog.log("setTool", tool.getName(), tool.getIconFileName(), source);

        editorPanel.tool = tool;
        editorPanel.setCursor();
        editorPanel.requestFocus();

        if (tool instanceof ActiveRegionSelectTool || tool instanceof ActiveRegionTool || tool instanceof ActiveRegionConnectorTool
                || tool instanceof TrajectoryMoveTool || tool instanceof TrajectoryPenTool || tool instanceof TrajectoryPointsTool) {
            editorPanel.activeRegionToolbar.toolChanged(tool);
        } else {
            editorPanel.colorToolbar.toolChanged(tool);
            editorPanel.currentPage.regions.deselectRegions();
        }
    }

    public int getSketchWidth() {
        if (currentPage.images != null && currentPage.images[0] != null) {
            return currentPage.images[0].getWidth();
        } else {
            return getWidth();
        }
    }

    public int getSketchHeight() {
        if (currentPage.images != null && currentPage.images[0] != null) {
            return currentPage.images[0].getHeight();
        } else {
            return getHeight();
        }
    }

    public void initSketches() {
        if (this.pages == null) {
            this.pages = new Pages();
        }

        if (this.pages.pages.size() > 0) {
            masterPage = this.pages.getSketch("Master");
            this.masterPage = this.pages.getSketch("Master");
            int index = DesktopPanel.selectedIndex;
            if (index < 0 || index >= pages.pages.size()) {
                index = 0;
            }
            this.currentPage = this.pages.pages.elementAt(index);
            editorFrame.setTitle(currentPage.title);
            textArea.setText(currentPage.strTextAnnotation);

            if (statePanel != null) {
                statePanel.load();
            }

            reloadPlay();
        }
    }

    long lastRepaintTime2;

    public synchronized void paintComponent(Graphics g) {
        long t = System.currentTimeMillis();
        super.paintComponent(g);

        if (renderer != null) {
            calculateMargins();
            renderer.draw(g, false, true, false);
            if (marginX > 0 || marginY > 0) {
                int w = SketchletEditor.editorPanel.scrollPane.getViewport().getWidth();
                int h = SketchletEditor.editorPanel.scrollPane.getViewport().getHeight();

                g.setColor(new Color(192, 192, 192));

                int x1 = (int) (marginX * SketchletEditor.editorPanel.scale);
                int y1 = (int) (marginY * SketchletEditor.editorPanel.scale);
                int x2 = (int) (w - marginX * SketchletEditor.editorPanel.scale);
                int y2 = (int) (h - marginY * SketchletEditor.editorPanel.scale);

                g.fillRect(0, 0, x1, h);
                g.fillRect(x1, 0, x2 - x1, y1);
                g.fillRect(x2, 0, marginX, h);
                g.fillRect(x1, y2, x2 - x1, marginY);

                int grays[] = new int[]{115, 146, 169, 184, 192};

                for (int i = 0; i < 4; i++) {
                    g.setColor(new Color(grays[i], grays[i], grays[i]));
                    g.drawLine(x1 + i, y1 - i, x2 - i * 2, y1 - i);
                    g.drawLine(x1 + i, y2 + i, x2 - i * 2, y2 + i);
                    g.drawLine(x1 - i, y1 + i, x1 - i, y2 - i * 2);
                    g.drawLine(x2 + i, y1 + i, x2 + i, y2 - i * 2);
                }
            }
            this.refreshPlay();
        }
    }

    public void calculateMargins() {
        final int w = SketchletEditor.editorPanel.scrollPane.getViewport().getWidth();
        final int h = SketchletEditor.editorPanel.scrollPane.getViewport().getHeight();

        int w2 = renderer.panel.getImage(0) != null ? renderer.panel.getImage(0).getWidth() : (int) InteractionSpace.sketchWidth;
        int h2 = renderer.panel.getImage(0) != null ? renderer.panel.getImage(0).getHeight() : (int) InteractionSpace.sketchHeight;

        if (w < w2 * SketchletEditor.editorPanel.scale) {
            SketchletEditor.marginX = 0;
        } else {
            SketchletEditor.marginX = (int) ((w / SketchletEditor.editorPanel.scale - w2) / 2);
        }

        if (h < h2 * SketchletEditor.editorPanel.scale) {
            SketchletEditor.marginY = 0;
        } else {
            SketchletEditor.marginY = (int) ((h / SketchletEditor.editorPanel.scale - h2) / 2);
        }
    }

    public void extraDraw(Graphics2D g2) {
        if (tool != null) {
            tool.draw(g2);
        }
    }

    public Graphics2D createGraphics() {
        if (g2FreeHandDraw != null) {
            g2FreeHandDraw.dispose();
        }
        if (currentPage.images != null && layer >= 0) {
            if (currentPage.images[layer] == null && currentPage.layerActive[layer]) {
                int w = Toolkit.getDefaultToolkit().getScreenSize().width;
                int h = Toolkit.getDefaultToolkit().getScreenSize().height;
                if (currentPage.images[0] != null) {
                    w = currentPage.images[0].getWidth();
                    h = currentPage.images[0].getHeight();
                }
                //renderer.initImage(layer);
                if (currentPage.images[layer] == null) {
                    this.updateImage(layer, Workspace.createCompatibleImage(w, h, currentPage.images[layer]));
                }
            } else if (currentPage.images[layer] == null) {
                return null;
            }
            g2FreeHandDraw = currentPage.images[layer].createGraphics();
            g2FreeHandDraw.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            g2FreeHandDraw = null;
        }

        return g2FreeHandDraw;
    }

    public boolean skipKey = false;
    public boolean undoing = false;

    public void undo() {
        try {
            undoing = true;
            if (skipKey) {
                skipKey = false;
                return;
            }
            if (tool != null) {
                tool.onUndo();
            }

            while (this.undoRegionActions.size() > 0) {
                UndoAction ua = (UndoAction) this.undoRegionActions.remove(this.undoRegionActions.size() - 1);

                if (ua instanceof SketchImageChangedUndoAction) {
                    this.saveImageRedo();
                }

                if (ua.shouldUndo()) {
                    ua.restore();
                    break;
                }
            }
            RefreshTime.update();
            repaint();
            if (ActiveRegionPanel.currentActiveRegionPanel != null) {
                ActiveRegionPanel.currentActiveRegionPanel.refreshComponents();
            }
            if (SketchletEditor.editorPanel.perspectivePanel != null) {
                SketchletEditor.editorPanel.perspectivePanel.refresh();
            }
            this.formulaToolbar.refresh();
            enableControls();
            this.createGraphics();

            if (tool != null) {
                tool.activate();
            }
        } finally {
            undoing = false;
        }
    }

    protected void clearImageMemory() {
        if (this.currentPage == null) {
            return;
        }
        if (currentPage.images != null) {
            for (int i = 0; i < currentPage.images.length; i++) {
                BufferedImage img = currentPage.images[i];
                if (img != null) {
                    img.flush();
                    currentPage.images[i] = null;
                }
            }
        }
        currentPage.flush();
    }

    protected void flush() {
        if (this.currentPage == null) {
            return;
        }
        currentPage.flush();
    }

    protected void initImages() {
        if (this.currentPage == null) {
            return;
        }
        currentPage.initRegionImages();
    }

    public void clearAll() {
        ComplexUndoAction ua = new ComplexUndoAction();
        for (int i = 0; i < currentPage.images.length; i++) {
            BufferedImage img = currentPage.images[i];
            if (img != null) {
                ua.add(new SketchImageChangedUndoAction(this.currentPage, img, i));
                // currentSketch.redoRegionActions[i].removeAllElements();
                this.sketchletImages.clearImage(i, false);
            }
        }
        ua.add(new RegionsDeletedUndoAction(currentPage.regions.regions));
        this.undoRegionActions.add(ua);
        checkUndo();
        this.currentPage.regions.selectedRegions = null;
        this.currentPage.regions.regions.removeAllElements();

        enableControls();
        RefreshTime.update();
        SketchletEditor.editorPanel.repaint();
    }

    public void clearAllImage() {
        saveImageUndo();
        this.clearImage();
        RefreshTime.update();
        SketchletEditor.editorPanel.repaint();
    }

    protected void clearImage() {
        this.sketchletImages.clearImage(layer, true);
    }

    public boolean bSaving = false;
    private Object lockSave = new Object();

    public void saveAndWait() {
        save();
        try {
            synchronized (lockSave) {
                while (bSaving) {
                    lockSave.wait();
                }
            }
        } catch (Exception e) {
            log.error(e);

        }
    }

    public void save() {
        if (bInPreviewMode || bSaving || !bGUIReady) {
            synchronized (lockSave) {
                lockSave.notifyAll();
            }
            return;
        }
        if (currentPage == null || pages.pages.indexOf(currentPage) < 0) {
            synchronized (lockSave) {
                lockSave.notifyAll();
            }
            return;
        }

        if (this.tool != null) {
            this.tool.deactivate();
        }
        if (ActiveRegionImageEditor.currentTool != null) {
            ActiveRegionImageEditor.currentTool.deactivate();
        }

        bSaving = true;

        new Thread(new Runnable() {

            public void run() {
                try {
                    enableControls();

                    if (editorFrame != null) {
                        editorFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    }

                    currentPage.strTextAnnotation = textArea.getText();
                    currentPage.save((layer >= 0 && undoRegionActions.size() > 0));

                    pages.saveSort();
                    enableControls();
                } catch (Throwable ex) {
                    ex.printStackTrace();
                } finally {
                    if (editorFrame != null) {
                        editorFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                    bSaving = false;
                    synchronized (lockSave) {
                        lockSave.notifyAll();
                    }
                }
            }
        }).start();
    }

    public void saveImage() {
        if (this.currentPage == null) {
            return;
        }
        try {
            for (int i = 0; i < Page.NUMBER_OF_LAYERS; i++) {
                if (this.currentPage.imageUpdated[i]) {
                    File file = this.currentPage.getLayerImageFile(i);
                    if (file == null) {
                        if (i == 0) {
                            file = new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "Sketch_" + currentPage.getId() + ".png");
                        } else {
                            file = new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "Sketch_" + currentPage.getId() + "_" + (i + 1) + ".png");
                        }
                    }
                    ImageCache.write(currentPage.images[i], file);
                    this.currentPage.imageUpdated[i] = false;
                }
            }
        } catch (IOException e) {
            log.error(e);
        }
    }

    public void openExternalEditor() {
        if (this.currentPage == null) {
            return;
        }

        this.tool.deactivate();
        this.saveImageUndo();

        this.currentPage.imageUpdated[layer] = true;
        save();

        String strFile = currentPage.getLayerImageFile(layer).getPath();
        SketchletContextUtils.editImages("\"" + strFile + "\"", this, layer);
    }

    boolean bPasting = false;

    public void newRegionFromImage(BufferedImage image, int x, int y) {
        ActiveRegion a = new ActiveRegion(this.currentPage.regions);

        a.setDrawImageChanged(0, true);

        a.setDrawImage(0, image);
        a.x1 = x;
        a.y1 = y;
        a.x2 = x + image.getWidth();
        a.y2 = y + image.getHeight();

        RefreshTime.update();
        repaint();
        currentPage.regions.regions.insertElementAt(a, 0);
        this.currentPage.regions.selectedRegions = new Vector<ActiveRegion>();
        currentPage.regions.addToSelection(a);
        if (this.tabsModes != null && tabsModes.getTabCount() > 0) {
            this.tabsModes.setSelectedIndex(0);
        }
        ActiveRegionsFrame.reload(a);
        editorFrame.requestFocus();
    }

    public void selectSketch(Page page) {
        if (page == null) {
            return;
        }
        int i = pages.pages.indexOf(page);

        if (i >= 0) {
            this.openSketchByIndex(i);
        }
    }

    public void selectSketch(String strSketch) {
        selectSketch(pages.getSketch(strSketch));
    }

    public static boolean bInPreviewMode = false;

    public void preview(Page s) {
        ActiveRegionsFrame.closeRegionsAndActions();
        setMode(EditorMode.SKETCHING);
        masterPage = pages.getSketch("Master");
        currentPage = s;
        statePanel.load();
        currentPage.activate(false);
        editorFrame.setTitle(currentPage.title);
        this.textArea.setText(currentPage.strTextAnnotation);
        RefreshTime.update();
        repaint();
        reloadPlay();
    }

    public void refreshImage(int index) {
        currentPage.initImage(index, true);
        if (this.getLayer() == index) {
            this.createGraphics();
        }

        this.repaintEverything();
    }

    public void forceRepaint() {
        RefreshTime.update();
        repaint();
    }

    public void refresh() {
        if (this.currentPage == null) {
            return;
        }

        renderer.initMasterImage();
        ActiveRegionsFrame.showRegionsAndActions((mode == EditorMode.ACTIONS || mode == EditorMode.PREVIEW) && bExtraEditorPanelVisible);
        editorFrame.setTitle(currentPage.title);
        this.textArea.setText(currentPage.strTextAnnotation);
        statePanel.load();

        revalidate();
        RefreshTime.update();
        repaint();

        if (this.sketchListPanel != null && currentPage != null) {
            this.sketchListPanel.model.fireTableDataChanged();
            int index = editorPanel.pages.pages.indexOf(editorPanel.currentPage);
            editorPanel.sketchListPanel.table.getSelectionModel().setSelectionInterval(index, index);
        }

        loadLayersTab();
        enableControls();
    }

    public void refreshPlayback() {
        if (this.currentPage != null && this.internalPlaybackPanel != null) {
            internalPlaybackPanel.currentPage.deactivate(true);
            internalPlaybackPanel.currentPage.activate(true);
            return;
        }
    }

    public void duplicate() {
        if (currentPage == null) {
            return;
        }
        saveAndWait();
        if (editorFrame != null) {
            editorFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        if (currentPage != null && controlPanel != null) {
            navigationHistory.add(currentPage);
            this.controlPanel.goBack.setEnabled(true);
        }
        if (editorPanel.tool != null) {
            editorPanel.tool.deactivate();
        }
        BufferedImage tempImages[] = new BufferedImage[currentPage.images.length];
        for (int i = 0; i < currentPage.images.length; i++) {
            tempImages[i] = currentPage.images[i];
            this.currentPage.imageUpdated[i] = currentPage.getLayerImageFile(i).exists();
        }
        Page s = pages.addNewSketch();
        s.regions = new ActiveRegions(currentPage.regions, s);
        // s.title = "Duplicate: " + currentSketch.title;
        s.strTextAnnotation = currentPage.strTextAnnotation;

        for (EventMacro eventMacro : currentPage.keyboardProcessor.keyboardEventMacros) {
            s.keyboardProcessor.keyboardEventMacros.add(new KeyboardEventMacro(eventMacro));
        }
        for (EventMacro eventMacro : currentPage.variableUpdateEventMacros) {
            s.variableUpdateEventMacros.add(new VariableUpdateEventMacro(eventMacro));
        }

        s.onEntryMacro = new Macro(currentPage.onEntryMacro);
        s.onExitMacro = new Macro(currentPage.onExitMacro);
        for (int i = 0; i < s.properties.length; i++) {
            s.properties[i][1] = currentPage.properties[i][1];
        }
        for (int i = 0; i < s.propertiesAnimation.length; i++) {
            for (int j = 0; j < s.propertiesAnimation[i].length; j++) {
                s.propertiesAnimation[i][j] = currentPage.propertiesAnimation[i][j];
            }
        }
        for (int i = 0; i < s.getSpreadsheetData().length; i++) {
            for (int j = 0; j < s.getSpreadsheetData()[i].length; j++) {
                s.updateSpreadsheetCell(i, j, currentPage.getSpreadsheetCellValue(i, j));
            }
        }
        s.strSpreadsheetColumnWidths = currentPage.strSpreadsheetColumnWidths;
        for (int i = 0; i < s.layerActive.length; i++) {
            s.layerActive[i] = currentPage.layerActive[i];
        }

        currentPage.deactivate(false);

        currentPage = s;

        currentPage.activate(false);
        for (int i = 0; i < currentPage.images.length; i++) {
            currentPage.images[i] = tempImages[i];
        }
        if (editorFrame != null) {
            editorFrame.setCursor(Cursor.getDefaultCursor());
        }
        saveAndWait();
        refresh();
        reloadPlay();
        createGraphics();
    }

    public Page newSketch() {
        if (tool != null) {
            tool.deactivate();
        }
        saveAndWait();
        Page s = pages.addNewSketch();

        String name = "";
        name = (String) JOptionPane.showInputDialog(SketchletEditor.editorFrame,
                Language.translate("Page name:"), Language.translate("New Page"),
                JOptionPane.QUESTION_MESSAGE, null, null, s.title);
        while (name != null && pages.sketchNameExists(name, s)) {
            JOptionPane.showMessageDialog(SketchletEditor.editorFrame, Language.translate("Page with name \"" + name + "\" already exists.\nEach page has to have a unique name."), Language.translate("Name Conflict"), JOptionPane.WARNING_MESSAGE);
            name = (String) JOptionPane.showInputDialog(SketchletEditor.editorFrame,
                    Language.translate("Page name:"), Language.translate("New Page"),
                    JOptionPane.QUESTION_MESSAGE, null, null, s.title);
        }

        if (name == null) {
            pages.pages.remove(s);
            s.dispose();
            return null;
        } else {
            s.title = name;
            editorFrame.setTitle(s.title);
            this.textArea.setText(s.strTextAnnotation);

            if (currentPage != null) {
                currentPage.deactivate(false);
            }
            masterPage = pages.getSketch("Master");
            currentPage = s;
            this.initImages();
            if (currentPage != null) {
                currentPage.activate(false);
            }

            GlobalProperties.setAndSave("last-sketch-index", "" + (pages.pages.size() - 1));

            saveAndWait();
            refresh();
            reloadPlay();

            return s;
        }

    }

    public void resize() {
        if (currentPage == null) {
            return;
        }
        try {
            TutorialPanel.addLine("cmd", "Resize the page image");
            new ResizeDialog(this.editorFrame, "Resize Page", editorPanel);
        } catch (Throwable e) {
            log.error(e);
        }
    }

    public void delete() {
        delete(this.editorFrame);
    }

    public void delete(Component component) {
        if (currentPage == null) {
            return;
        }
        try {
            Object[] options = {"Delete", "Cancel"};
            int n = JOptionPane.showOptionDialog(component,
                    "You are about to delete '" + currentPage.title + "'",
                    "Delete Sketch Confirmation",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (n != 0) {
                return;
            }

            int i = this.pages.pages.indexOf(currentPage);
            currentPage.delete();

            this.pages.pages.remove(currentPage);
            //tabs.remove(i);

            masterPage = pages.getSketch("Master");
            if (this.pages.pages.size() == 0) {
                currentPage = null;
            } else {
                int index = i + 1 > this.pages.pages.size() ? this.pages.pages.size() - 1 : i;
                masterPage = this.pages.getSketch("Master");
                currentPage = this.pages.pages.elementAt(index);
                this.initImages();
                currentPage.activate(false);
                reloadPlay();
            }
            refresh();
        } catch (Throwable e) {
            log.error(e);
        }
    }

    public static Vector<ActiveRegion> copiedActions = null;

    public void playAndRecord() {
        new InteractionRecorder(SketchletEditor.editorFrame);
        play();
    }

    public void play() {
        saveAndWait();
        if (SketchletEditor.editorPanel.tool != null) {
            SketchletEditor.editorPanel.tool.deactivate();
        }
        SketchletEditor.editorPanel.oldScale = SketchletEditor.editorPanel.scale;
        SketchToolbar.animateZoom = false;
        SketchletEditor.editorPanel.sketchToolbar.zoomBox.setSelectedItem("100%");
        SketchToolbar.animateZoom = true;
        SketchletEditor.editorPanel.scale = PlaybackPanel.scale;

        editorPanel.clearImageMemory();
        if (ImageCache.images == null) {
            PlaybackFrame.clearImagesOnClose = true;
            ImageCache.load();
        }
        updateTables();
        boolean bHideEditor = false;
        if (PlaybackFrame.playbackFrame == null) {
            bHideEditor = true;
            if (ActiveRegionsFrame.reagionsAndActions != null && ActiveRegionsFrame.reagionsAndActions.isVisible()) {
                ActiveRegionsFrame.reagionsAndActions.setState(Frame.ICONIFIED);
            }
            /*
             * if (TimersFrame.frame != null) { TimersFrame.frame.onSave();
             * TimersFrame.frame.setVisible(false); }
             */
            if (MacrosFrame.frame != null) {
                MacrosFrame.frame.save();
                MacrosFrame.frame.setVisible(false);
            }
            if (InteractionSpaceFrame.frame != null) {
                InteractionSpaceFrame.frame.save();
                InteractionSpaceFrame.frame.setState(Frame.ICONIFIED);
            }
        }

        if (bHideEditor) {
            this.editorFrame.setState(Frame.ICONIFIED);
        }

        PlaybackFrame.play(this.pages, currentPage);
        if (PlaybackFrame.playbackFrame.length > 0 && PlaybackFrame.playbackFrame[0] != null) {
            PlaybackFrame.playbackFrame[0].playbackPanel.requestFocus();
        }
    }

    public JScrollPane playInternal() {
        if (SketchletEditor.editorPanel.tool != null) {
            SketchletEditor.editorPanel.tool.deactivate();
        }
        SketchletEditor.editorPanel.oldScale = SketchletEditor.editorPanel.scale;
        SketchToolbar.animateZoom = false;
        SketchletEditor.editorPanel.sketchToolbar.zoomBox.setSelectedItem("100%");
        SketchToolbar.animateZoom = true;
        SketchletEditor.editorPanel.scale = PlaybackPanel.scale;

        updateTables();
        extraEditorPanel.save();
        saveAndWait();

        if (InteractionSpaceFrame.frame != null) {
            InteractionSpaceFrame.frame.save();
            InteractionSpaceFrame.frame.setState(Frame.ICONIFIED);
        }

        internalPlaybackPanel = new PlaybackPanel(null, pages, null);
        internalPlaybackPanelScrollPane = new JScrollPane(internalPlaybackPanel);
        internalPlaybackPanelScrollPane.setFocusable(false);
        if (currentPage.images != null && currentPage.images[0] != null) {
            internalPlaybackPanel.setPreferredSize(new Dimension(currentPage.images[0].getWidth(), currentPage.images[0].getHeight()));
        } else {
            int w = (int) InteractionSpace.sketchWidth;
            int h = (int) InteractionSpace.sketchHeight;
            internalPlaybackPanel.setPreferredSize(new Dimension(w, h));
        }

        this.currentPage.deactivate(false);
        if (masterPage != null) {
            masterPage.deactivate(false);
        }
        internalPlaybackPanel.showSketch(this.currentPage);

        return internalPlaybackPanelScrollPane;
    }
    // JLabel label = new JLabel(Workspace.createImageIcon("resources/sketching.png"));

    public void zoomToWindow() {
        try {
            final int w = SketchletEditor.editorPanel.scrollPane.getViewport().getWidth();
            final int h = SketchletEditor.editorPanel.scrollPane.getViewport().getHeight();
            int w2 = 0;
            int h2 = 0;
            if (currentPage.images[0] != null) {
                w2 = currentPage.images[0].getWidth();
                h2 = currentPage.images[0].getHeight();
            } else {
                w2 = (int) InteractionSpace.sketchWidth;
                h2 = (int) InteractionSpace.sketchHeight;
            }
            double _zoom = Math.min((double) w / w2, (double) h / h2) - 0.02;
            SketchletEditor.editorPanel.sketchToolbar.zoomBox.setSelectedItem("" + (int) (_zoom * 100) + "%");
        } catch (Exception e) {
        }
    }

    public void setMode(EditorMode m) {
        setMode(m, layer);
    }

    public void setMode(EditorMode m, int _layer) {
        if (editorPanel.tool != null) {
            editorPanel.tool.deactivate();
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        boolean shouldActivate = false;
        if (editorPanel.currentPage != null && editorPanel.internalPlaybackPanel != null) {
            shouldActivate = true;
            editorPanel.currentPage.deactivate(true);
            editorPanel.drawingPanel.remove(editorPanel.internalPlaybackPanelScrollPane);
            DataServer.variablesServer.removeVariablesUpdateListener(editorPanel.internalPlaybackPanel);
            if (editorPanel.internalPlaybackPanel != null) {
                editorPanel.internalPlaybackPanel.dispose();
                editorPanel.masterPage = editorPanel.pages.getSketch("Master");
                editorPanel.internalPlaybackPanel = null;
                //System.gc();
                //System.runFinalization();
            }
            mode = EditorMode.UNDEFINED;
        }

        if (mode != m) {
            ActivityLog.log("setMode", "" + m);
        }

        if (m == EditorMode.SKETCHING) {
            if (mode != EditorMode.SKETCHING || _layer != layer) {
                if (shouldActivate) {
                    editorPanel.currentPage.activate(false);
                }

                mode = EditorMode.SKETCHING;
                SketchletEditor.statusBar.setText("Sketching mode");
                ActiveRegionsFrame.showRegionsAndActions(false);
                setTool(penTool, null);
                // layer = _layer;
                setCursor();
                enableControls();
                createGraphics();

                editorPanel.drawingPanel.add(editorPanel.mainSketchPanel);
                SketchletEditor.editorPanel.formulaToolbar.refresh();
                editorPanel.mainSketchPanel.remove(activeRegionToolbar);
                editorPanel.mainSketchPanel.add(colorToolbar, BorderLayout.SOUTH);
                editorPanel.drawingPanel.revalidate();
                editorFrame.repaint();
            }
        } else if (m == EditorMode.ACTIONS) {
            if (mode != EditorMode.ACTIONS || _layer != layer) {
                if (shouldActivate) {
                    editorPanel.currentPage.activate(false);
                }
                mode = EditorMode.ACTIONS;
                currentPage.regions.refreshFromVariables();

                if (Profiles.isActive("active_regions_layer")) {
                    setTool(activeRegionSelectTool, null);
                } else {
                    setTool(penTool, null);
                }
                requestFocus();
                setCursor();
                enableControls();
                editorPanel.drawingPanel.add(editorPanel.mainSketchPanel);
                SketchletEditor.editorPanel.formulaToolbar.refresh();
                editorPanel.mainSketchPanel.remove(colorToolbar);
                editorPanel.mainSketchPanel.add(activeRegionToolbar, BorderLayout.SOUTH);
                editorPanel.drawingPanel.revalidate();
                editorFrame.repaint();
            }
        } else if (m == EditorMode.PREVIEW) {
            //layer = 0;
            saveAndWait();
            editorPanel.drawingPanel.remove(editorPanel.mainSketchPanel);
            editorPanel.clearImageMemory();
            JScrollPane sp = editorPanel.playInternal();
            //sp.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            editorPanel.drawingPanel.add(sp);
            editorPanel.drawingPanel.revalidate();
            editorFrame.repaint();
        }

        modeToolbar.addComponents(m);
        editorPanel.revalidate();
        RefreshTime.update();

        editorPanel.repaint();
        editorPanel.enableControls();
        //freeHandFrame.toFront();
        editorFrame.setVisible(true);
        requestFocus();
        this.setCursor();
    }

    static Cursor currentCursor = null;

    public void setCursor() {
        if (tool != null && currentPage.isLayerActive(layer)) {
            currentCursor = tool.getCursor();
            setCursor(currentCursor);
        } else {
            setCursor(null);
        }
    }

    public void resetScale() {
        if (SketchletEditor.editorPanel.oldScale != 0.0) {
            SketchletEditor.editorPanel.sketchToolbar.zoomBox.setSelectedItem((int) (SketchletEditor.editorPanel.oldScale * 100) + "%");
        }
    }

    public void history() {
    }

    public void setAsMaster() {
        setTool(activeRegionSelectTool, null);

        int i = 0;
        for (Page s : SketchletEditor.editorPanel.pages.pages) {
            if (s.title.equalsIgnoreCase("master")) {
                s.title += " (old)";
                s.save(false);
            }
            i++;
        }
        this.currentPage.title = "master";
        editorFrame.setTitle("master");
        this.save();
    }

    public void printPage() {
        final BufferedImagePrinter printer = new BufferedImagePrinter();
        printer.setActionBeforePrinting(new Runnable() {

            public void run() {
                BufferedImage img = Workspace.createCompatibleImage(currentPage.images[0].getWidth(), currentPage.images[0].getHeight());
                Graphics2D g2i = img.createGraphics();
                renderer.draw(g2i, SketchletEditor.editorPanel.tabsModes.getSelectedIndex() != 0, true, false);
                g2i.dispose();
                printer.setImages(img);
            }
        });
        printer.print();
    }

    private static JFileChooser chooser = new JFileChooser();

    public static File selectImageFile(String initFile) {
        if (initFile != null && !initFile.isEmpty()) {
            chooser.setSelectedFile(new File(initFile));
        }
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG file", "png"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("GIF file", "gif"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("JPEG file", "jpg", "jpeg"));
        chooser.setMultiSelectionEnabled(false);
        int returnVal = chooser.showSaveDialog(SketchletEditor.editorFrame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            if (file != null) {
                String extensions[] = {"png", "gif", "jpeg", "jpg"};
                boolean noExtension = true;
                for (String ex : extensions) {
                    if (ex.toLowerCase().endsWith("." + ex.toLowerCase())) {
                        noExtension = false;
                        break;
                    }
                }
                String fileName = file.getPath();
                if (noExtension) {
                    String desc = chooser.getFileFilter().getDescription();
                    for (String ex : extensions) {
                        if (desc.toLowerCase().contains(ex.toLowerCase())) {
                            fileName += "." + ex;
                            break;
                        }
                    }
                }

                int n = fileName.indexOf(".");
                if (n > 0) {
                    return new File(fileName);
                } else {
                    return new File(fileName + ".png");
                }
            }
        }

        return null;
    }

    public void saveSketchAsImage() {
        File file = selectImageFile(this.currentPage.getTitle());
        if (file != null) {
            try {
                if (!file.getName().contains(".")) {
                    file = new File(file.getPath() + ".png");
                }
                int n = file.getName().indexOf(".");
                String extension = file.getName().substring(n + 1);
                int w = currentPage.images[0].getWidth();
                int h = currentPage.images[0].getHeight();
                BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2i = img.createGraphics();
                g2i.setColor(Color.WHITE);
                g2i.fillRect(0, 0, w, h);
                renderer.draw(g2i, SketchletEditor.editorPanel.tabsModes.getSelectedIndex() != 0, true, false);
                g2i.dispose();
                ImageIO.write(img, extension, file);
                JOptionPane.showMessageDialog(this, "The image has been saved.");
            } catch (Throwable e) {
                log.error(e);
            }
        }
    }

    public void selectAll() {
        if (this.tabsModes.getSelectedIndex() == 0) {
            if (tool instanceof ActiveRegionSelectTool || tool instanceof ActiveRegionTool) {
                selectAllRegions();
            } else {
                setTool(this.selectTool, null);
                this.selectTool.x1 = 0;
                this.selectTool.y1 = 0;
                this.selectTool.x2 = this.getWidth();
                this.selectTool.y2 = this.getHeight();
            }
        }

        RefreshTime.update();
        repaint();
    }

    public void importSketches() {
        setTool(activeRegionSelectTool, null);
        fc.setDialogTitle("Select Project Folder");
        fc.setCurrentDirectory(new File(SketchletContextUtils.getDefaultProjectsRootLocation()));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showDialog(SketchletEditor.editorFrame, "Import Sketches");

        if (returnVal == fc.APPROVE_OPTION) {
            String strPath = fc.getSelectedFile().getPath();
            final String strDir = strPath + File.separator + SketchletContextUtils.sketchletDataDir() + File.separator + "sketches" + File.separator;
            final Vector<Page> importedPages = SketchletsSaxLoader.getSketchesFromDir(strDir);

            final Object[][] data = new Object[importedPages.size()][2];

            for (int i = 0; i < data.length; i++) {
                data[i][0] = new Boolean(true);
                data[i][1] = importedPages.elementAt(i).title;
            }

            final String columnNames[] = new String[]{"Import", "Page title"};
            final AbstractTableModel tableModel = new AbstractTableModel() {

                public String getColumnName(int col) {
                    return columnNames[col].toString();
                }

                public int getRowCount() {
                    return data.length;
                }

                public int getColumnCount() {
                    return columnNames.length;
                }

                public Object getValueAt(int row, int col) {
                    return data[row][col];
                }

                public boolean isCellEditable(int row, int col) {
                    return true;
                }

                public void setValueAt(Object value, int row, int col) {
                    data[row][col] = value;
                    fireTableCellUpdated(row, col);
                }

                public Class getColumnClass(int c) {
                    return getValueAt(0, c).getClass();
                }
            };
            final JTable table = new JTable(tableModel);
            table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.getColumnModel().getColumn(0).setPreferredWidth(30);
            table.getColumnModel().getColumn(1).setPreferredWidth(420);

            final JDialog dlg = new JDialog(SketchletEditor.editorFrame, true);
            dlg.setTitle("Select Sketches to Import");
            dlg.add(new JScrollPane(table));
            JPanel buttons = new JPanel();
            JButton importBtn = new JButton("Import");
            importBtn.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    int i = 0;
                    for (Page s : importedPages) {
                        if (((Boolean) data[i++][0]).booleanValue()) {
                            pages.pages.add(s);
                            JPanel pane = new JPanel();
                            pane.setPreferredSize(new Dimension(10, 10));
                            //tabs.add(pane, s.title);

                            FileUtils.restore(SketchletContextUtils.getCurrentProjectSkecthletsDir(), strDir, null, false, s.getId().substring(1));
                            for (ActiveRegion a : s.regions.regions) {
                                a.getDrawImagePath(0);
                                FileUtils.copyFile(new File(strDir + a.getDrawImagePath(0)), new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + a.getDrawImagePath(0)));

                                for (int aai = 0; aai < a.additionalImageFile.size(); aai++) {
                                    a.getDrawImagePath(aai + 1);
                                    FileUtils.copyFile(new File(strDir + a.getDrawImagePath(aai + 1)), new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + a.getDrawImagePath(aai + 1)));
                                }
                            }
                        }
                    }
                    SketchletEditor.editorPanel.refresh();
                    dlg.setVisible(false);
                }
            });
            JButton selectBtn = new JButton("Select All");
            selectBtn.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    for (int i = 0; i < data.length; i++) {
                        data[i][0] = new Boolean(true);
                    }
                    tableModel.fireTableDataChanged();
                }
            });
            JButton deselectBtn = new JButton("Deselect All");
            deselectBtn.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    for (int i = 0; i < data.length; i++) {
                        data[i][0] = new Boolean(false);
                    }
                    tableModel.fireTableDataChanged();
                }
            });
            buttons.add(importBtn);
            buttons.add(selectBtn);
            buttons.add(deselectBtn);
            dlg.add(buttons, BorderLayout.SOUTH);
            dlg.pack();
            dlg.setLocationRelativeTo(SketchletEditor.editorFrame);
            dlg.setVisible(true);

        }

    }

    public void enableControls() {
        if (sketchToolbar != null) {
            sketchToolbar.enableControls();
        }
        if (modeToolbar != null) {
            modeToolbar.enableControls();
        }
    }

    public void moveLeft() {
        setTool(activeRegionSelectTool, null);
        int index = pages.pages.indexOf(currentPage);

        if (index > 0) {
            //String title1 = tabs.getTitleAt(index);
            //String title2 = tabs.getTitleAt(index - 1);
            //tabs.setTitleAt(index, title2);
            //tabs.setTitleAt(index - 1, title1);

            Page s = pages.pages.remove(index);
            pages.pages.insertElementAt(s, index - 1);
            SketchletEditor.editorPanel.openSketchByIndex(index - 1);
            editorPanel.refresh();
            //tabs.setSelectedIndex(index - 1);
        }
    }

    public void moveRight() {
        setTool(activeRegionSelectTool, null);
        int index = pages.pages.indexOf(currentPage);

        if (index >= 0 && index < pages.pages.size() - 1) {
            Page s = pages.pages.remove(index);
            pages.pages.insertElementAt(s, index + 1);

            SketchletEditor.editorPanel.openSketchByIndex(index + 1);
            editorPanel.refresh();
        }
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public Dimension getPreferredSize() {
        double _scale = scale; // > 1.0 ? scale : 1.0;
        int w = 0;
        int h = 0;
        if (currentPage.images != null && currentPage.images[0] != null) {
            w = currentPage.images[0].getWidth();
            h = currentPage.images[0].getHeight();
        } else if (currentPage.pageWidth > 0 && currentPage.pageHeight > 0) {
            w = currentPage.pageWidth;
            h = currentPage.pageHeight;
        } else {
            w = (int) InteractionSpace.sketchWidth;
            h = (int) InteractionSpace.sketchHeight;
        }

        currentPage.pageWidth = w;
        currentPage.pageHeight = h;

        w += marginX * 2;
        h += marginY * 2;

        return new Dimension((int) (w * _scale), (int) (h * _scale));
    }

    public static JFrame editorFrame;
    public JScrollPane scrollPane;
    public boolean bDisposed = false;

    public boolean isActive() {
        return !bDisposed;
    }

    public boolean isLayerActive(int l) {
        return SketchletEditor.editorPanel.currentPage.layerActive[l];
    }

    boolean bClosing = false;

    public static boolean close() {
        return close(true);
    }

    public static boolean close(boolean bMsg) {
        if (editorPanel == null || editorPanel.bClosing) {
            return true;
        }
        editorPanel.bClosing = true;
        if (editorPanel.mediaWatcher != null) {
            editorPanel.mediaWatcher.stop();
        }
        if (undockStateFrame != null) {
            undockStateFrame.setVisible(false);
            bStatePanelUndocked = false;
            editorPanel.bShowStatePanel = false;
        }

        if (undockFrame != null) {
            undockFrame.setVisible(false);
            bUndocked = false;
            editorPanel.bExtraEditorPanelVisible = false;
        }

        int nOption = -1;
        if (bMsg) {
            Object[] options = {Language.translate("Save Changes"),
                    Language.translate("Don't Save"),
                    Language.translate("Cancel")};
            nOption = JOptionPane.showOptionDialog(editorFrame,
                    Language.translate("Do you want to save changes?"),
                    Language.translate("Unsaved Changes"),
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (nOption == 2) {
                return false;
            }
        } else {
            nOption = -1;
        }
        try {
            GlobalProperties.save();
            if (editorPanel != null) {
                ActivityLog.save();
                HelpUtils.helpInterface = null;
                editorPanel.bDisposed = true;
                if (nOption == 0) {
                    editorPanel.saveAndWait();
                    Timers.globalTimers.save();
                    Macros.globalMacros.save();
                    Workspace.mainPanel.saveConfiguration();
                }
                ImageCache.clear();
                if (editorPanel.currentPage != null) {
                    editorPanel.currentPage.deactivate(false);
                }
                editorPanel.hideExtraEditorPanel();
                editorPanel.statePanel.hideProperties();
                VariableSpacesFrame.hideFrame();

                int w = editorPanel.editorFrame.getWidth();
                int h = editorPanel.editorFrame.getHeight();
                GlobalProperties.set("editor-window-size", w + "," + h);
                GlobalProperties.save();
//                FileUtils.saveFileText(SketchletContextUtils.getDefaultProjectsRootLocation() + "editor_window_size.txt", w + "," + h);
                editorPanel.editorFrame.remove(editorPanel);
                if (editorPanel.internalPlaybackPanel != null) {
                    DataServer.variablesServer.removeVariablesUpdateListener(editorPanel.internalPlaybackPanel);
                    if (editorPanel.internalPlaybackPanel.currentPage != null) {
                        editorPanel.internalPlaybackPanel.currentPage.deactivate(true);
                    }
                    editorPanel.internalPlaybackPanel.dispose();
                    // editorPanel.masterSketch = editorPanel.sketches.getSketch("Master");
                    editorPanel.internalPlaybackPanel = null;
                }
            }
            if (editorPanel.undoRegionActions != null) {
                editorPanel.undoRegionActions.clear();
            }
            SystemVariablesDialog.stop();
            ActiveRegionsFrame.closeRegionsAndActions();
            PlaybackFrame.close();
            Workspace.referenceFrame = null;
            CurvesFrame.hideFrame();
            StateDiagram.hideDiagram();
            // SketchStatePanel.hideProperties();
            InteractionSpaceFrame.closeFrame();
            EyeFrame.hideFrame();

            PlaybackPanel.currentPage = null;
            PlaybackPanel.masterPage = null;
            PlaybackPanel.pages = null;
            PlaybackPanel.history.removeAllElements();

            if (editorPanel != null) {
                editorPanel.fileDrop = null;
                editorPanel.removeKeyListener(editorPanel);
                editorPanel.renderer.dispose();
                editorPanel.renderer = null;
                editorPanel.navigationHistory.removeAllElements();
            }
            if (editorPanel != null && editorPanel.currentPage.images != null) {
                for (int i = 0; i < editorPanel.currentPage.images.length; i++) {
                    BufferedImage img = editorPanel.currentPage.images[i];
                    if (img != null) {
                        img.flush();
                        editorPanel.currentPage.images[i] = null;
                    }
                }
            }
            editorPanel.currentPage.images = null;
            editorPanel.penTool.dispose();
            editorPanel.bucketTool.dispose();
            editorPanel.transparentColorTool.dispose();
            editorPanel.magicWandTool.dispose();
            editorPanel.lineTool.dispose();
            editorPanel.rectTool.dispose();
            editorPanel.ovalTool.dispose();
            editorPanel.eraserTool.dispose();
            editorPanel.selectTool.dispose();
            editorPanel.freeFormSelectTool.dispose();
            editorPanel.activeRegionTool.dispose();
            editorPanel.activeRegionSelectTool.dispose();
            editorPanel.postNoteTool.dispose();
            editorPanel.tool.dispose();

            if (editorPanel.g2FreeHandDraw != null) {
                editorPanel.g2FreeHandDraw.dispose();
                editorPanel.g2FreeHandDraw = null;
            }

            DataServer.variablesServer.removeVariablesUpdateListener(editorPanel);
            // DataServer.variablesServer.printInfo();
            /*
             * if (CaptureFrame.captureFrame != null) {
             * CaptureFrame.captureFrame.setVisible(false);
             * CaptureFrame.captureFrame.close(); }
             */
        } catch (Throwable e) {
            log.error(e);
        }
        if (SketchletEditor.copiedActions != null) {
            SketchletEditor.copiedActions.removeAllElements();
        }

        if (SketchletEditor.pages != null) {
            SketchletEditor.pages.dispose();
            SketchletEditor.pages = null;
        }

        if (nOption == 1) { // || nOption == -1) {
            Workspace.mainPanel.restoreOriginal();
        } else {
            FileUtils.deleteDir(new File(SketchletContextUtils.getCurrentProjectOriginalDir()));
        }

        if (editorPanel != null) {
            editorFrame.setVisible(false);
            editorFrame.dispose();
        }
        editorFrame.dispose();
        editorPanel = null;
        editorFrame = null;
        Workspace.mainFrame.toFront();
        final boolean _bMsg = bMsg;
        final int _nOption = nOption;
        if (!Workspace.bCloseOnPlaybackEnd) {
            if (SketchletEditor.pages != null) {
                SketchletEditor.pages.dispose();
            }
            if (_nOption == 1 || _nOption == -1) {
                try {
                    Workspace.processRunner.loadProcesses(new File(Workspace.filePath).toURL(), false);
                } catch (Exception e) {
                    log.error(e);
                }
            }
            SketchletEditor.pages = new Pages();
            Workspace.mainPanel.refreshSketches();
        }

        return true;
    }

    public static JTextField statusBar = new JTextField(60);
    public static boolean inPlaybackMode = false;
    MemoryPanel memoryPanel;

    public void addMemoryPanel() {
        if (memoryPanel != null) {
            editorPanel.statusPanel.remove(memoryPanel);
            memoryPanel.stop();
            memoryPanel = null;
            GlobalProperties.set("memory-monitor", "false");
        } else {
            memoryPanel = new MemoryPanel();
            editorPanel.statusPanel.add(memoryPanel, BorderLayout.EAST);
            GlobalProperties.set("memory-monitor", "true");
        }
        editorPanel.statusPanel.revalidate();
    }

    public static void showExtraEditorPanel() {
        showExtraEditorPanel(0);
    }

    public static JPanel getDrawingTabPanel(Color bgColor, String strImage) {
        JPanel panel = new JPanel();
        FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT);
        flowLayout.setHgap(0);
        flowLayout.setVgap(0);
        JLabel label = new JLabel(Workspace.createImageIcon(strImage));
        label.setIconTextGap(0);
        panel.setBackground(bgColor);
        panel.add(label);

        return panel;
    }

    public static void showTableVariables() {
        VariableSpacesFrame.showFrame();
    }

    public static void showDerivedVariablesPopupMenu(ActionEvent event) {
        if (event.getSource() instanceof Component) {
            JPopupMenu popup = new JPopupMenu();
            int n = 0;
            for (PluginInstance dv : GenericPluginFactory.getDerivedVariablesPlugins()) {
                ImageIcon icon;
                if (dv.getInstance() instanceof SketchletPluginGUI) {
                    icon = ((SketchletPluginGUI) dv.getInstance()).getIcon();
                } else {
                    icon = Workspace.createImageIcon("resources/plugin.png");
                }
                JMenuItem plugin = new JMenuItem(dv.getName(), icon);
                final int index = n;
                plugin.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent event) {
                        SketchletEditor.showExtraEditorPanel(ExtraEditorPanel.indexDerivedVariables);
                        SketchletEditor.editorPanel.extraEditorPanel.derivedVariablesExtraPanel.tabs.setSelectedIndex(index);
                    }
                });
                popup.add(plugin);
                n++;
            }
            if (n > 0) {
                popup.show((Component) event.getSource(), 0, ((Component) event.getSource()).getHeight());
            } else {
                JOptionPane.showMessageDialog(SketchletContext.getInstance().getEditorFrame(), "Plugins for derived variables are not installed.");
            }
        }
    }

    public static void showExtraEditorPanel(int nTab) {
        if (!editorPanel.bExtraEditorPanelVisible && !bUndocked) {
            editorPanel.bExtraEditorPanelVisible = true;
            int h = SketchletEditor.editorPanel.panelDrawingPanel.getHeight();
            editorPanel.editorPane.remove(SketchletEditor.editorPanel.panelDrawingPanel);
            // editorPanel.editorPane.add(editorPanel.extraEditorPanel, BorderLayout.SOUTH);
            editorPanel.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, SketchletEditor.editorPanel.panelDrawingPanel, editorPanel.extraEditorPanel);
            editorPanel.splitPane.setResizeWeight(1);
            editorPanel.splitPane.setDividerLocation(h - 195);
            editorPanel.editorPane.add(editorPanel.splitPane);
            editorPanel.editorPane.revalidate();
        }

        if (nTab >= 0 && nTab < editorPanel.extraEditorPanel.tabs.getTabCount()) {
            editorPanel.extraEditorPanel.tabs.setSelectedIndex(nTab);
        }
    }

    public static void showExtraEditorPanelBig(int nTab) {
        if (!editorPanel.bExtraEditorPanelVisible && !bUndocked) {
            editorPanel.bExtraEditorPanelVisible = true;
            editorPanel.extraEditorPanel.setBiggerSize();
            editorPanel.editorPane.add(editorPanel.extraEditorPanel, BorderLayout.SOUTH);
            editorPanel.editorPane.revalidate();
        }

        editorPanel.extraEditorPanel.tabs.setSelectedIndex(nTab);
    }

    public static boolean bUndocked = false;
    public static JFrame undockFrame = null;

    public static void undockExtraEditorPanel() {
        hideExtraEditorPanel();
        undockFrame = new JFrame();
        undockFrame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                bUndocked = false;
                editorPanel.bExtraEditorPanelVisible = false;
            }

            public void windowActivated(WindowEvent e) {
                //AWTUtilitiesWrapper.setWindowOpacity(undockFrame, 1.0f);
            }

            public void windowDeactivated(WindowEvent e) {
                //AWTUtilitiesWrapper.setWindowOpacity(undockFrame, 0.5f);
            }
        });
        undockFrame.setAlwaysOnTop(true);
        undockFrame.setTitle("Editor");
        undockFrame.add(editorPanel.extraEditorPanel);
        undockFrame.pack();
        undockFrame.setIconImage(Workspace.createImageIcon("resources/editor.gif", "").getImage());
        undockFrame.setVisible(true);
        bUndocked = true;
    }

    public static void dockExtraEditorPanel() {
        if (undockFrame != null) {
            undockFrame.setVisible(false);
            editorPanel.bExtraEditorPanelVisible = false;
            bUndocked = false;
            showExtraEditorPanel(-1);
        }
    }

    public static void hideExtraEditorPanel() {
        if (editorPanel.bExtraEditorPanelVisible) {
            int index = SketchletEditor.editorPanel.tabsRight.getSelectedIndex();

            editorPanel.extraEditorPanel.onHide();
            editorPanel.bExtraEditorPanelVisible = false;
            editorPanel.editorPane.remove(SketchletEditor.editorPanel.splitPane);
            editorPanel.editorPane.add(SketchletEditor.editorPanel.panelDrawingPanel);
            // editorPanel.editorPane.remove(editorPanel.extraEditorPanel);
            editorPanel.editorPane.revalidate();
            SketchletEditor.editorPanel.timersTablePanel.model.fireTableDataChanged();
            SketchletEditor.editorPanel.extraEditorPanel.timersExtraPanel.load();
            SketchletEditor.editorPanel.macrosTablePanel.model.fireTableDataChanged();
            SketchletEditor.editorPanel.extraEditorPanel.macrosExtraPanel.load();

            SketchletEditor.editorPanel.tabsRight.setSelectedIndex(index);
        }
    }

    public static boolean isInEntryArea(int x, int y) {
        return Profiles.isActive("page_actions") && getEntryIconRectangle().contains(x, y);
    }

    public static boolean isInExitArea(int x, int y) {
        return Profiles.isActive("page_actions") && getExitIconRectangle().contains(x, y);
    }

    public static boolean isInVariableInArea(int x, int y) {
        return Profiles.isActive("page_actions,variables") && getVariableIconRectangle().contains(x, y);
    }

    /*
     * public static boolean isInVariableOutArea(int x, int y) { return x > 0 &&
     * x < 35 && y > 95 && y < 125; }
     */
    public static boolean isInKeyboardArea(int x, int y) {
        return Profiles.isActive("page_actions") && getKeyboardIconRectangle().contains(x, y);
    }

    public static boolean isInPropertiesArea(int x, int y) {
        return Profiles.isActive("page_properties") && getPropertiesIconRectangle().contains(x, y);
    }

    public static Rectangle getEntryIconRectangle() {
        double s = Math.min(1, SketchletEditor.editorPanel.scale);
        return new Rectangle(0, 0, (int) (35 / s), (int) (30 / s));
    }

    public static Rectangle getExitIconRectangle() {
        double s = Math.min(1, SketchletEditor.editorPanel.scale);
        return new Rectangle(0, (int) (35 / s), (int) (30 / s), (int) (30 / s));
    }

    public static Rectangle getVariableIconRectangle() {
        double s = Math.min(1, SketchletEditor.editorPanel.scale);
        return new Rectangle(0, (int) (65 / s), (int) (30 / s), (int) (30 / s));
    }

    public static Rectangle getKeyboardIconRectangle() {
        double s = Math.min(1, SketchletEditor.editorPanel.scale);
        return new Rectangle(0, (int) (95 / s), (int) (30 / s), (int) (30 / s));
    }

    public static Rectangle getPropertiesIconRectangle() {
        double s = Math.min(1, SketchletEditor.editorPanel.scale);
        return new Rectangle(0, (int) (125 / s), (int) (30 / s), (int) (30 / s));
    }

    public void keyTyped(KeyEvent e) {
        if (tool != null) {
            tool.keyTyped(e);
        }

    }

    public void selectAllRegions() {
        currentPage.regions.selectedRegions = new Vector<ActiveRegion>();

        for (ActiveRegion a : this.currentPage.regions.regions) {
            currentPage.regions.addToSelection(a);
        }
        RefreshTime.update();

        repaint();
    }

    public void deleteSelectedRegion() {
        if (skipKey) {
            skipKey = false;
            return;
        }
        if (currentPage.regions.selectedRegions != null) {
            this.undoRegionActions.add(new RegionsDeletedUndoAction(currentPage.regions.selectedRegions));
            checkUndo();
            save();

            for (ActiveRegion as : currentPage.regions.selectedRegions) {
                as.deactivate(false);
                currentPage.regions.regions.remove(as);
            }

            currentPage.regions.selectedRegions = null;
            ActiveRegionsFrame.reload();
            currentPage.regions.selectedRegions = null;
            RefreshTime.update();
            repaint();

        }
    }

    public void deleteRegion(ActiveRegion region) {
        for (int i = 0; i < region.getImageCount(); i++) {
            ImageCache.remove(new File(region.getDrawImagePath(i)));
        }
        save();
        region.deactivate(false);
        currentPage.regions.regions.remove(region);
        currentPage.regions.selectedRegions = null;
        ActiveRegionsFrame.reload();
        currentPage.regions.selectedRegions = null;
        RefreshTime.update();
        repaint();

    }

    public void groupSelectedRegion() {
        if (currentPage.regions.selectedRegions != null) {
            String strGroupId = "" + System.currentTimeMillis();

            boolean bGroup = false;
            for (ActiveRegion as : currentPage.regions.selectedRegions) {
                if (as.regionGrouping.equals("") || !as.regionGrouping.equals(currentPage.regions.selectedRegions.firstElement().regionGrouping)) {
                    bGroup = true;
                    break;
                }
            }

            for (ActiveRegion as : currentPage.regions.selectedRegions) {
                as.regionGrouping = bGroup ? strGroupId : "";
            }
            RefreshTime.update();

            repaint();
        }

    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        String keyText = e.getKeyText(key);
        int modifiers = e.getModifiers();
        RefreshTime.update();

        if ((modifiers & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
            inCtrlMode = true;
        } else {
            inCtrlMode = false;
        }

        if ((modifiers & KeyEvent.SHIFT_MASK) != 0) {
            inShiftMode = true;
        } else {
            inShiftMode = false;
        }

        boolean useShortcut = mode == EditorMode.SKETCHING || mode == EditorMode.ACTIONS && (currentPage.regions.selectedRegions == null || currentPage.regions.selectedRegions.size() == 0);
        if ((useShortcut) && (modifiers & KeyEvent.ALT_MASK) == 0 && (modifiers & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) == 0) {
            if (key == KeyEvent.VK_P) {
                SketchletEditor.editorPanel.modeToolbar.btnSketching.doClick();
            } else if (key == KeyEvent.VK_L) {
                SketchletEditor.editorPanel.modeToolbar.btnLine.doClick();
            } else if (key == KeyEvent.VK_A) {
                SketchletEditor.editorPanel.modeToolbar.activeRegions.doClick();
            } else if (key == KeyEvent.VK_C) {
                SketchletEditor.editorPanel.modeToolbar.connector.doClick();
            } else if (key == KeyEvent.VK_ESCAPE) {
                SketchletEditor.editorPanel.modeToolbar.select.doClick();
            } else if (key == KeyEvent.VK_R) {
                SketchletEditor.editorPanel.modeToolbar.btnRect.doClick();
            } else if (key == KeyEvent.VK_O) {
                SketchletEditor.editorPanel.modeToolbar.btnOval.doClick();
            } else if (key == KeyEvent.VK_S) {
                SketchletEditor.editorPanel.modeToolbar.btnSelect.doClick();
            } else if (key == KeyEvent.VK_F) {
                SketchletEditor.editorPanel.modeToolbar.btnFreeFormSelect.doClick();
            } else if (key == KeyEvent.VK_P) {
                SketchletEditor.editorPanel.modeToolbar.btnColorPicker.doClick();
            } else if (key == KeyEvent.VK_M) {
                SketchletEditor.editorPanel.modeToolbar.btnMagicWand.doClick();
            } else if (key == KeyEvent.VK_B) {
                SketchletEditor.editorPanel.modeToolbar.btnBucket.doClick();
            } else if (key == KeyEvent.VK_E) {
                SketchletEditor.editorPanel.modeToolbar.btnEraser.doClick();
            } else if (key == KeyEvent.VK_PLUS || key == KeyEvent.VK_EQUALS || key == 107) {
                int n = SketchletEditor.editorPanel.colorToolbar.slider.getValue();
                SketchletEditor.editorPanel.colorToolbar.slider.setValue(n + 1);
            } else if (key == KeyEvent.VK_MINUS || key == 109) {
                int n = SketchletEditor.editorPanel.colorToolbar.slider.getValue();
                SketchletEditor.editorPanel.colorToolbar.slider.setValue(n - 1);
            }
        } else if (mode == EditorMode.ACTIONS) {
            if (key == KeyEvent.VK_ESCAPE) {
                if (currentPage.regions.selectedRegions != null) {
                    ActiveRegion as = currentPage.regions.selectedRegions.lastElement();
                    if (as.inTrajectoryMode && as.trajectoryType == 2) {
                        as.mouseHandler.processTrajectory();
                        as.inTrajectoryMode = false;
                    } else if (as.inTrajectoryMode2 && as.trajectoryType == 2) {
                        as.mouseHandler.processTrajectory2();
                        as.inTrajectoryMode2 = false;
                    }
                }
            } else if (key == KeyEvent.VK_F2) {
                if (mode == EditorMode.SKETCHING) {
                    setMode(EditorMode.ACTIONS);
                } else {
                    setMode(EditorMode.SKETCHING);
                }

            } else if (key == KeyEvent.VK_F3) {
                setMode(EditorMode.SKETCHING);
                if (modeToolbar.layerList.getSelectedIndex() == 0) {
                    modeToolbar.layerList.setSelectedIndex(1);
                } else {
                    modeToolbar.layerList.setSelectedIndex(0);
                }
            } else if ((modifiers & KeyEvent.ALT_MASK) != 0) {
            } else if ((modifiers & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
                switch (key) {
                    case KeyEvent.VK_B:
                        if (currentPage.regions.selectedRegions != null) {
                            for (ActiveRegion region : currentPage.regions.selectedRegions) {
                                if (region.fontStyle.equalsIgnoreCase("Bold Italic")) {
                                    this.setTextStyle("Italic");
                                } else if (region.fontStyle.equalsIgnoreCase("Bold")) {
                                    this.setTextStyle("");
                                } else {
                                    this.setTextStyle("Bold");
                                }

                            }
                        }
                        break;
                    case KeyEvent.VK_I:
                        if (currentPage.regions.selectedRegions != null) {
                            for (ActiveRegion region : currentPage.regions.selectedRegions) {
                                if (region.fontStyle.equalsIgnoreCase("Bold Italic")) {
                                    this.setTextStyle("Bold");
                                } else if (region.fontStyle.equalsIgnoreCase("Italic")) {
                                    this.setTextStyle("");
                                } else {
                                    this.setTextStyle("Italic");
                                }

                            }
                        }
                        break;
//                case KeyEvent.VK_I:
//                    this.fromFile();
//                    break;
                    /*
                    * case KeyEvent.VK_E: this.editorPanel(); break;
                    */
                    /*
                     * case KeyEvent.VK_A:
                     * SketchStatePanel.showStateProperties(currentSketch);
                     * break;
                     */
                    case KeyEvent.VK_LEFT:
                        this.moveLeft();
                        break;

                    case KeyEvent.VK_RIGHT:
                        this.moveRight();
                        break;

                    /*
                     * case KeyEvent.VK_D: this.duplicate(); break;
                     */
                }
            } else if (currentPage.regions.selectedRegions != null) {
                if (currentPage.regions.selectedRegions != null) {
                    for (ActiveRegion region : currentPage.regions.selectedRegions) {
                        switch (key) {
                            case KeyEvent.VK_LEFT:
                                region.x1 -= 10;
                                region.x2 -= 10;
                                repaint();

                                break;

                            case KeyEvent.VK_RIGHT:
                                region.x1 += 10;
                                region.x2 += 10;
                                repaint();

                                break;

                            case KeyEvent.VK_UP:
                                region.y1 -= 10;
                                region.y2 -= 10;
                                repaint();

                                break;

                            case KeyEvent.VK_DOWN:
                                region.y1 += 10;
                                region.y2 += 10;
                                repaint();

                                break;

                        }
                    }

                    e.consume();
                }
            }
        }
        if (tool != null) {
            tool.keyPressed(e);
        } else {
            log.info("Tool is null");
        }


        RefreshTime.update();
        repaint();
    }

    public void paste() {
        if (skipKey) {
            skipKey = false;
            return;
        }

        if (mode == EditorMode.ACTIONS && copiedActions != null) {
            this.editorClipboard.pasteSpecial();
        } else {
            this.editorClipboard.fromClipboard();
        }

    }

    public void defineClip() {
        if (currentPage.regions.selectedRegions == null || currentPage.regions.selectedRegions.size() == 0) {
            return;
        }

        try {
            ActiveRegion _a = this.currentPage.regions.selectedRegions.firstElement();
            ActiveRegion a = new ActiveRegion(_a, _a.parent, false);
            a.bFitToBox = false;
            a.strHAlign = "";
            a.strVAlign = "";
            a.rotation = 0.0;
            a.playback_rotation = 0.0;
            a.shearX = 0.0;
            a.shearY = 0.0;
            a.p_x0 = 0.0;
            a.p_y0 = 0.0;
            a.p_x1 = 1.0;
            a.p_y1 = 0.0;
            a.p_x2 = 1.0;
            a.p_y2 = 1.0;
            a.p_x3 = 0.0;
            a.p_y3 = 1.0;
            a.x1 = 0;
            a.y1 = 0;
            a.x2 = 2000;
            a.y2 = 2000;
            a.playback_x1 = 0;
            a.playback_y1 = 0;
            a.playback_x2 = 2000;
            a.playback_y2 = 2000;
            int x = 1;
            int y = 1;
            int w = _a.x2 - _a.x1;
            int h = _a.y2 - _a.y1;
            try {
                x = (int) InteractionSpace.getSketchX(Double.parseDouble(a.processText(a.strWindowX)));
            } catch (Throwable e) {
            }
            try {
                y = (int) InteractionSpace.getSketchY(Double.parseDouble(a.processText(a.strWindowY)));
            } catch (Throwable e) {
            }
            try {
                w = (int) InteractionSpace.getSketchWidth(Double.parseDouble(a.processText(a.strWindowWidth)));
            } catch (Throwable e) {
            }
            try {
                h = (int) InteractionSpace.getSketchHeight(Double.parseDouble(a.processText(a.strWindowHeight)));
            } catch (Throwable e) {
            }
            a.resetAllProperties();

            a.activate(true);
            a.strText = "";

            try {
                BufferedImage img = Workspace.createCompatibleImage(2000, 2000);
                Graphics2D g2 = img.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                a.renderer.draw(g2, editorPanel, EditorMode.SKETCHING, true, false, 1.0f);
                g2.dispose();
                ImageAreaSelect.createAndShowGUI(SketchletEditor.editorFrame, img, x, y, w, h, false);
                if (ImageAreaSelect.bSaved) {
                    String params[] = ImageAreaSelect.strArea.split(" ");
                    if (params.length >= 4) {
                        _a.strWindowX = "" + InteractionSpace.getPhysicalX(Double.parseDouble(params[0]));
                        _a.strWindowY = "" + InteractionSpace.getPhysicalY(Double.parseDouble(params[1]));
                        _a.strWindowWidth = "" + InteractionSpace.getPhysicalWidth(Double.parseDouble(params[2]));
                        _a.strWindowHeight = "" + InteractionSpace.getPhysicalHeight(Double.parseDouble(params[3]));
                    }

                }
                RefreshTime.update();

                repaint();
            } catch (Throwable e) {
                log.error(e);
            }

        } catch (Throwable e) {
            log.error(e);
        }

    }

    public void extract(int index) {
        if (mode == EditorMode.ACTIONS && this.currentPage.regions.selectedRegions != null) {
            BufferedImage img = SketchletEditor.editorPanel.currentPage.images[SketchletEditor.editorPanel.layer];
            ActiveRegion a = currentPage.regions.selectedRegions.lastElement();
            if (index == -1) {
                a.additionalImageFile.add("");
                a.additionalDrawImages.add(null);
                a.additionalImageChanged.add(new Boolean(false));
                index = a.additionalImageFile.size();
            }

            int x = a.x1;
            int y = a.y1;
            int w = a.x2 - a.x1;
            int h = a.y2 - a.y1;
            x = Math.max(x, 0);
            y = Math.max(y, 0);
            w = Math.min(w, img.getWidth());
            h = Math.min(h, img.getHeight());

            img = extractImage(x, y, w, h);

            a.setDrawImage(index, img);
            a.setDrawImageChanged(index, true);
            a.rotation = 0;
            a.saveImage();
            ActiveRegionsFrame.reload(a);
        }

    }

    public BufferedImage extractImage(int x, int y, int w, int h) {
        return extractImage(x, y, w, h, SketchletEditor.editorPanel);
    }

    public static BufferedImage extractImage(int x, int y, int w, int h, ToolInterface toolInterface) {
        BufferedImage subImg = null;

        try {
            if (w > 0 && h > 0) {
                toolInterface.saveImageUndo();
                BufferedImage img = toolInterface.getImage();
                img = img.getSubimage(x, y, w, h);

                subImg = Workspace.createCompatibleImage(w, h);
                Graphics2D g2 = subImg.createGraphics();
                g2.drawImage(img, 0, 0, null);
                g2.dispose();

                toolInterface.repaintImage();

                for (int i = 0; i < img.getWidth(); i++) {
                    for (int j = 0; j < img.getHeight(); j++) {
                        img.setRGB(i, j, new Color(0, 0, 0, 0).getRGB());
                    }

                }
                toolInterface.setImageUpdated(true);
            }

        } catch (Throwable e) {
            log.error(e);
        }

        return subImg;
    }

    public BufferedImage extractImage(Polygon polygon) {
        return extractImage(polygon, this);
    }

    public static BufferedImage extractImage(Polygon polygon, ToolInterface toolInterface) {
        toolInterface.saveImageUndo();
        BufferedImage img = toolInterface.getImage();
        Rectangle rect = polygon.getBounds();
        int x = (int) rect.getMinX();
        int y = (int) rect.getMinY();
        int w = (int) rect.getWidth();
        int h = (int) rect.getHeight();

        x = Math.max(0, x);
        y = Math.max(0, y);
        int x2 = Math.min(img.getWidth(), x + w);
        int y2 = Math.min(img.getHeight(), y + h);

        w = x2 - x;
        h = y2 - y;

        int i = 0, j = 0;

        RefreshTime.update();
        try {
            img = img.getSubimage(x, y, w, h);

            BufferedImage subImg = Workspace.createCompatibleImage(w, h);
            Graphics2D g2 = subImg.createGraphics();
            g2.drawImage(img, 0, 0, null);
            g2.dispose();

            toolInterface.repaintImage();

            for (i = 0; i < img.getWidth(); i++) {
                for (j = 0; j < img.getHeight(); j++) {
                    if (polygon.contains(i + x, j + y)) {
                        img.setRGB(i, j, new Color(0, 0, 0, 0).getRGB());
                    } else {
                        subImg.setRGB(i, j, new Color(0, 0, 0, 0).getRGB());
                    }

                }
            }
            toolInterface.setImageUpdated(true);
            return subImg;
        } catch (Throwable e) {
            log.error(e);
        }

        return null;
    }

    /*
     * public void stamp() { if (mode == ACTIONS &&
     * this.currentSketch.actions.selectedActions != null) { try { int index =
     * Integer.parseInt(ActiveRegion.processText((String)
     * this.currentSketch.actions.selectedActions.lastElement().strImageIndex,
     * currentSketch.strVarPrefix, currentSketch.strVarPostfix)); stamp(index -
     * 1); } catch (Throwable e) { stamp(0); }
     *
     * repaint(); } }
     */
    public void stamp() {
        if (mode == EditorMode.ACTIONS && this.currentPage.regions.selectedRegions != null) {
            saveImageUndo();
            ActiveRegion region = this.currentPage.regions.selectedRegions.lastElement();
            Graphics2D g2 = currentPage.images[layer].createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.rotate(region.rotation, region.x1 + (region.x2 - region.x1) * region.center_rotation_x, region.y1 + (region.y2 - region.y1) * region.center_rotation_y);
            region.renderer.drawActive(g2, editorPanel, false, 1.0f);

            g2.dispose();
            RefreshTime.update();
            SketchletEditor.editorPanel.repaint();

            this.currentPage.imageUpdated[layer] = true;
        }

    }

    public void keyReleased(KeyEvent e) {
        inShiftMode = false;
        inCtrlMode =
                false;

        if (tool != null) {
            tool.keyReleased(e);
        }

        RefreshTime.update();
        repaint();
    }

    public static String lastVariable = "";
    long count = 0;

    public void repaintEverything() {
        RefreshTime.update();
        this.repaint();
        if (this.internalPlaybackPanel != null) {
            this.internalPlaybackPanel.repaint();
        }

        PlaybackFrame.repaintAllFrames();
        RefreshTime.update();
    }

    int countt = 0;

    public void variableUpdated(String name, String varValue) {
        if (this.currentPage == null || name.trim().equals("")) {
            return;
        }
        if (PlaybackFrame.playbackFrame != null) {
            for (int i = 0; i < PlaybackFrame.playbackFrame.length; i++) {
                PlaybackFrame frame = PlaybackFrame.playbackFrame[i];
                if (frame != null) {
                    frame.playbackPanel.variableUpdated(name, varValue);
                }
            }
            return;
        }

        if (this.internalPlaybackPanel != null) {
            this.internalPlaybackPanel.variableUpdated(name, varValue);
            return;
        }


        lastVariable = name;

        if (this.spreadsheetPanel != null) {
            this.spreadsheetPanel.table.repaint();
        }

        this.currentPage.regions.changePerformed(name, varValue, false);
        if (!currentPage.variableUpdatePageHandler.process(name, varValue)) {
            if (masterPage != null && currentPage != masterPage) {
                masterPage.variableUpdatePageHandler.process(name, varValue);
            }
        }

        this.repaint();
    }

    public void saveSpreadsheetColumWidths() {
        String strWidth = "";
        if (SketchletEditor.editorPanel != null && SketchletEditor.editorPanel.spreadsheetPanel != null) {
            for (int ic = 0; ic < SketchletEditor.editorPanel.spreadsheetPanel.table.getColumnCount(); ic++) {
                if (!strWidth.isEmpty()) {
                    strWidth += ",";
                }
                strWidth += SketchletEditor.editorPanel.spreadsheetPanel.table.getColumnModel().getColumn(ic).getWidth();
            }
        }

        this.currentPage.strSpreadsheetColumnWidths = strWidth;
    }

    public void fromFile() {
        int returnVal = ActiveRegionPanel.getFileChooser().showOpenDialog(ActiveRegionsFrame.reagionsAndActions);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            fromFile(ActiveRegionPanel.getFileChooser().getSelectedFile());
        }

    }

    public static boolean isInPlaybackMode() {
        return SketchletEditor.editorPanel != null && (SketchletEditor.editorPanel.internalPlaybackPanel != null || (PlaybackFrame.playbackFrame != null && PlaybackFrame.playbackFrame[0] != null));
    }

    public void fromFile(File file) {
        fromFile(0, 0, file);
    }

    public void fromFile(int x, int y, File file) {
        setTool(activeRegionSelectTool, null);
        try {
            saveImageUndo();
            BufferedImage newImage;

            if (file.getName().toLowerCase().endsWith(".pdf")) {
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                FileChannel channel = raf.getChannel();
                ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
                PDFFile pdffile = new PDFFile(buf);

                int n = pdffile.getNumPages();

                PDFPage page = pdffile.getPage(0);

                Rectangle2D rect = page.getBBox();

                //generate the images
                Image image = page.getImage(
                        (int) rect.getWidth(), (int) rect.getHeight(), //width & height
                        rect, // clip rect
                        null, // null for the ImageObserver
                        false, // fill background with white
                        true // block until drawing is done
                );
                newImage = Workspace.createCompatibleImageCopy(image);
            } else {
                BufferedImage image = ImageIO.read(file);
                newImage = Workspace.createCompatibleImageCopy(image);
            }

            this.currentPage.imageUpdated[layer] = true;
            setTool(this.selectTool, null);
            selectTool.setClip(newImage, x, y);
            RefreshTime.update();

            this.repaint();
        } catch (Throwable e) {
            log.error(e);
        }
    }

    public void restoreImage(File file, Component c) {
        fromFile(file);
    }

    public void previewImage(File file) {
        try {
            currentPage.images[layer] = ImageIO.read(file);
            RefreshTime.update();
            this.repaint();
        } catch (Throwable e) {
            log.error(e);
        }

        createGraphics();
        setTool(activeRegionSelectTool, null);
    }

    public boolean skipUndo = false;

    public void saveRegionUndo() {
        if (!undoing && !skipUndo && currentPage.regions.selectedRegions != null && currentPage.regions.selectedRegions.size() > 0) {
            final UndoAction ua = new RegionsChangedUndoAction(currentPage.regions.selectedRegions);
            this.addUndoAction(ua);
        }
    }

    public void saveRegionUndo(ActiveRegion region) {
        if (!undoing && !skipUndo) {
            final UndoAction ua = new RegionChangedUndoAction(region);
            this.addUndoAction(ua);
        }
    }

    public void saveSketchUndo() {
        if (!undoing && !skipUndo) {
            final UndoAction ua = new SketchChangedUndoAction(currentPage);
            this.addUndoAction(ua);
        }
    }

    public void saveMacroUndo(Macro m) {
        if (!undoing && !skipUndo) {
            final UndoAction ua = new MacroChangedUndoAction(m);
            this.addUndoAction(ua);
        }

    }

    public void saveTimerUndo(Timer t) {
        if (!undoing && !skipUndo) {
            final UndoAction ua = new TimerChangedUndoAction(t);
            this.addUndoAction(ua);
        }
    }

    private void addUndoAction(UndoAction ua) {
        this.undoRegionActions.add(ua);
        checkUndo();
        enableControls();
    }

    public void saveRegionUndo(final UndoAction ua) {
        if (!undoing) {
            if (currentPage.regions.selectedRegions != null) {
                java.awt.EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        if (undoRegionActions.size() > 0) {
                            UndoAction ua = (UndoAction) undoRegionActions.get(undoRegionActions.size() - 1);
                            if (ua instanceof RegionsChangedUndoAction) {
                                if (((RegionsChangedUndoAction) ua).isSame(currentPage.regions.selectedRegions)) {
                                    return;
                                }
                            }
                        }

                        undoRegionActions.add(ua);
                        checkUndo();
                        enableControls();
                    }
                });
            }
        }
    }

    public static final int UNDO_BUFFER_SIZE = 100;

    public void checkUndo() {
        if (undoRegionActions != null) {
            if (undoRegionActions.size() > SketchletEditor.UNDO_BUFFER_SIZE) {
                undoRegionActions.remove(0);
            }
        }
    }

    public void saveRegionImageUndo(BufferedImage img, ActiveRegion region, int frame) {
        undoRegionActions.add(new RegionImageChangedUndoAction(img, region, frame));
        checkUndo();
        enableControls();
    }

    public void saveNewRegionUndo() {
        if (mode == EditorMode.ACTIONS) {
            if (currentPage.regions.selectedRegions != null) {
                undoRegionActions.add(new NewRegionUndoAction(currentPage.regions.selectedRegions.firstElement()));
                checkUndo();
                enableControls();
            }
        }
    }

    public void saveImageUndo() {
        BufferedImage img = currentPage.images[layer];
        if (img != null) {
            undoRegionActions.add(new SketchImageChangedUndoAction(this.currentPage, img, layer));
            checkUndo();
            enableControls();
        }
    }

    public void saveImageRedo() {
    }

    public void moveCurrentActionUpwards() {
        if (editorPanel.currentPage.regions.selectedRegions != null && editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            for (ActiveRegion action : editorPanel.currentPage.regions.selectedRegions) {
                int index = action.parent.regions.indexOf(action);
                int newIndex = index - 1;

                if (newIndex >= 0) {
                    action.parent.regions.remove(action);
                    action.parent.regions.insertElementAt(action, newIndex);
                    ActiveRegionsFrame.reload();
                    ActiveRegionsFrame.refresh(action);
                    TutorialPanel.addLine("cmd", "Move the region upwards");
                }

            }

            RefreshTime.update();

            repaint();
        }

    }

    public void moveCurrentActionToFront() {
        if (editorPanel.currentPage.regions.selectedRegions != null && editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            for (ActiveRegion action : editorPanel.currentPage.regions.selectedRegions) {
                int index = action.parent.regions.indexOf(action);

                if (index > 0) {
                    int newIndex = 0;

                    action.parent.regions.remove(action);
                    action.parent.regions.insertElementAt(action, newIndex);
                    ActiveRegionsFrame.reload();
                    ActiveRegionsFrame.refresh(action);
                    TutorialPanel.addLine("cmd", "Move the region to front");
                }

            }
            RefreshTime.update();

            repaint();
        }

    }

    public void moveCurrentActionToBack() {
        if (editorPanel.currentPage.regions.selectedRegions != null && editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            for (ActiveRegion action : editorPanel.currentPage.regions.selectedRegions) {
                int index = action.parent.regions.indexOf(action);
                int newIndex = action.parent.regions.size() - 1;

                if (index >= 0 && index < newIndex) {

                    action.parent.regions.remove(action);
                    action.parent.regions.insertElementAt(action, newIndex);
                    ActiveRegionsFrame.reload();
                    ActiveRegionsFrame.refresh(action);
                    TutorialPanel.addLine("cmd", "Move the region to back");
                }
            }
        }
    }

    public void moveCurrentActionToBackground() {
        if (editorPanel.currentPage.regions.selectedRegions != null && editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            for (ActiveRegion action : editorPanel.currentPage.regions.selectedRegions) {
                action.strX = "=0";
                action.strY = "=0";
                action.strWidth = Integer.toString(SketchletEditor.editorPanel.getSketchWidth());
                action.strHeight = Integer.toString(SketchletEditor.editorPanel.getSketchHeight());
            }
            this.moveCurrentActionToBack();
            SketchletEditor.editorPanel.forceRepaint();
        }
    }

    public void moveCurrentActionBackwards() {
        if (editorPanel.currentPage.regions.selectedRegions != null && editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            for (ActiveRegion action : editorPanel.currentPage.regions.selectedRegions) {
                int index = action.parent.regions.indexOf(action);
                int newIndex = index + 1;

                if (newIndex < action.parent.regions.size()) {
                    action.parent.regions.remove(action);
                    action.parent.regions.insertElementAt(action, newIndex);
                    ActiveRegionsFrame.reload();
                    ActiveRegionsFrame.refresh(action);
                    TutorialPanel.addLine("cmd", "Move the region backwards");
                }
            }
            RefreshTime.update();

            repaint();
        }

    }

    public static String[] getShapeArgs(String shape) {
        if (shape.toLowerCase().startsWith("starpolygon")) {
            return new String[]{"inner radius (0..1)", "0.5"};
        } else if (shape.toLowerCase().startsWith("pie")) {
            return new String[]{"start angle, extent, internal radius (0..1)", "0,45,0.0"};
        } else if (shape.toLowerCase().startsWith("rounded rectangle")) {
            return new String[]{"rounded corner radius", "10"};
        }

        return null;
    }

    public void setShape(String shape, int index) {
        SketchletEditor.editorPanel.saveRegionUndo();
        if (editorPanel.currentPage.regions.selectedRegions != null && editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            for (ActiveRegion action : editorPanel.currentPage.regions.selectedRegions) {
                String strArgs = "";
                String args[] = getShapeArgs(shape);
                if (args != null) {
                    String arg = JOptionPane.showInputDialog(SketchletEditor.editorFrame, args[0], args[1]);
                    if (arg != null) {
                        strArgs = arg;
                    }
                }
                action.shape = shape;
                action.strShapeArgs = strArgs;
                /*
                 * if (action.shapeList != null) {
                 * action.shapeList.setSelectedIndex(index); }
                 */
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement());
            this.forceRepaint();
        }

    }

    public void setHorizontalAlignment(String strAlign) {
        if (editorPanel.currentPage.regions.selectedRegions != null && editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            SketchletEditor.editorPanel.saveRegionUndo();
            for (ActiveRegion action : editorPanel.currentPage.regions.selectedRegions) {
                //action.horizontalAlign.setSelectedItem(strAlign);
                action.strHAlign = strAlign;
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement());
            this.forceRepaint();
        }

    }

    public void setAutomaticPerspective(String strPerspective) {
        if (editorPanel.currentPage.regions.selectedRegions != null && editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            SketchletEditor.editorPanel.saveRegionUndo();
            for (ActiveRegion action : editorPanel.currentPage.regions.selectedRegions) {
                action.strAutomaticPerspective = strPerspective;
                TutorialPanel.addLine("cmd", "Set the region automatic perspective: " + strPerspective);
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement());
            this.forceRepaint();
        }

    }

    public void setPerspectiveDepth(String strDepth) {
        if (editorPanel.currentPage.regions.selectedRegions != null && editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            SketchletEditor.editorPanel.saveRegionUndo();
            for (ActiveRegion action : editorPanel.currentPage.regions.selectedRegions) {
                action.strPerspectiveDepth = strDepth;
                TutorialPanel.addLine("cmd", "Set the region perspective depth: " + strDepth);
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement());
            this.forceRepaint();
        }

    }

    public void setVerticalAlignment(String strAlign) {
        if (editorPanel.currentPage.regions.selectedRegions != null && editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            SketchletEditor.editorPanel.saveRegionUndo();
            for (ActiveRegion action : editorPanel.currentPage.regions.selectedRegions) {
                // action.verticalAlign.setSelectedItem(strAlign);
                action.strVAlign = "";
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement());
            this.forceRepaint();
        }

    }

    public void setLineColor(String color) {
        if (editorPanel.currentPage.regions.selectedRegions != null && editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            SketchletEditor.editorPanel.saveRegionUndo();
            for (ActiveRegion action : editorPanel.currentPage.regions.selectedRegions) {
                // action.lineColor.setSelectedItem(color);
                action.strLineColor = color;
                ActiveRegionsExtraPanel.reload(currentPage.regions.getLastSelectedRegion());
                TutorialPanel.addLine("cmd", "Set the region line color: " + color);
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement());
            this.forceRepaint();
        }

    }

    public void setWidget(String strWidget) {
        if (editorPanel.currentPage.regions.selectedRegions != null && editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            SketchletEditor.editorPanel.saveRegionUndo();
            for (ActiveRegion region : editorPanel.currentPage.regions.selectedRegions) {
                region.strWidget = strWidget;
                region.strWidgetItems = WidgetPluginFactory.getDefaultItemsText(new ActiveRegionContextImpl(region, new PageContextImpl(region.parent.page)));
                region.strWidgetProperties = WidgetPluginFactory.getDefaultPropertiesValue(new ActiveRegionContextImpl(region, new PageContextImpl(region.parent.page)));
                region.widgetProperties = null;
                region.widgetEventMacros.clear();
                /*String actions[] = WidgetPluginFactory.getActions(new ActiveRegionContextImpl(region, new PageContextImpl(region.parent.sketch)));
                for (String action : actions) {
                    Macro macro = new Macro();
                    macro.name = action;
                    region.widgetEventMacros.add(new WidgetEventMacro(action));
                }      */
                ActiveRegionsExtraPanel.reload(currentPage.regions.getLastSelectedRegion());
                TutorialPanel.addLine("cmd", "Set the region widget: " + strWidget);
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement());
            this.forceRepaint();
        }

    }

    public void setTextColor(String color) {
        if (editorPanel.currentPage.regions.selectedRegions != null && editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            SketchletEditor.editorPanel.saveRegionUndo();
            for (ActiveRegion action : editorPanel.currentPage.regions.selectedRegions) {
                /*
                 * if (action.fontColorCombo != null) {
                 * action.fontColorCombo.setSelectedItem(color); }
                 */

                action.fontColor = color;
                ActiveRegionsExtraPanel.reload(currentPage.regions.getLastSelectedRegion());
                TutorialPanel.addLine("cmd", "Set the region text color: " + color);
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement());
            this.forceRepaint();
        }

    }

    public void setTextStyle(String style) {
        if (editorPanel.currentPage.regions.selectedRegions != null && editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            SketchletEditor.editorPanel.saveRegionUndo();
            for (ActiveRegion action : editorPanel.currentPage.regions.selectedRegions) {
                /*
                 * if (action.styleCombo != null) {
                 * action.styleCombo.setSelectedItem(style); }
                 */

                action.fontStyle = style;
                ActiveRegionsExtraPanel.reload(currentPage.regions.getLastSelectedRegion());
                TutorialPanel.addLine("cmd", "Set the region text style: " + style);
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement());
            this.forceRepaint();
        }

    }

    public void setFillColor(String color) {
        if (editorPanel.currentPage.regions.selectedRegions != null && editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            SketchletEditor.editorPanel.saveRegionUndo();
            for (ActiveRegion action : editorPanel.currentPage.regions.selectedRegions) {
                // action.fillColor.setSelectedItem(color);
                action.strFillColor = color;
                ActiveRegionsExtraPanel.reload(currentPage.regions.getLastSelectedRegion());
                TutorialPanel.addLine("cmd", "Set the region fill color: " + color);
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement());
            this.forceRepaint();
        }

    }

    public void setTransparency(float t) {
        if (editorPanel.currentPage.regions.selectedRegions != null && editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            SketchletEditor.editorPanel.saveRegionUndo();
            for (ActiveRegion action : editorPanel.currentPage.regions.selectedRegions) {
                action.strTransparency = (t == 0) ? "" : "" + t;
                TutorialPanel.addLine("cmd", "Set the region transparency: " + t);
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement());
            this.forceRepaint();
        }

    }

    public void setSpeed(int s) {
        if (editorPanel.currentPage.regions.selectedRegions != null && editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            SketchletEditor.editorPanel.saveRegionUndo();
            for (ActiveRegion action : editorPanel.currentPage.regions.selectedRegions) {
                action.strSpeed = (s == 0) ? "" : "" + s;
                TutorialPanel.addLine("cmd", "Set the region speed: " + s);
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement());
            this.forceRepaint();
        }

    }

    public void setRotationSpeed(int s) {
        if (editorPanel.currentPage.regions.selectedRegions != null && editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            SketchletEditor.editorPanel.saveRegionUndo();
            for (ActiveRegion action : editorPanel.currentPage.regions.selectedRegions) {
                action.strRotationSpeed = (s == 0) ? "" : "" + s;
                TutorialPanel.addLine("cmd", "Set the region rotation speed: " + s);
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement());
            this.forceRepaint();
        }

    }

    public void setHorizontal3DRotation(int rot) {
        if (editorPanel.currentPage.regions.selectedRegions != null && editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            SketchletEditor.editorPanel.saveRegionUndo();
            for (ActiveRegion action : editorPanel.currentPage.regions.selectedRegions) {
                action.strRotation3DHorizontal = (rot == 0) ? "" : "" + rot;
                TutorialPanel.addLine("cmd", "Set the region horizontal 3D rotation: " + rot);
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement());
            this.forceRepaint();
        }

    }

    public void setVertical3DRotation(int rot) {
        if (editorPanel.currentPage.regions.selectedRegions != null && editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            SketchletEditor.editorPanel.saveRegionUndo();
            for (ActiveRegion action : editorPanel.currentPage.regions.selectedRegions) {
                action.strRotation3DVertical = (rot == 0) ? "" : "" + rot;
                TutorialPanel.addLine("cmd", "Set the region vertical 3D rotation: " + rot);
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement());
            this.forceRepaint();
        }

    }

    public void setLineStyle(String style) {
        if (editorPanel.currentPage.regions.selectedRegions != null && editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            SketchletEditor.editorPanel.saveRegionUndo();
            for (ActiveRegion action : editorPanel.currentPage.regions.selectedRegions) {
                // action.lineStyle.setSelectedItem(style);
                action.strLineStyle = style;
                ActiveRegionsExtraPanel.reload(currentPage.regions.getLastSelectedRegion());
                TutorialPanel.addLine("cmd", "Set the region line style: " + style);
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement());
            this.forceRepaint();
        }

    }

    public void setLineThickness(String thickness) {
        if (editorPanel.currentPage.regions.selectedRegions != null && editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            SketchletEditor.editorPanel.saveRegionUndo();
            for (ActiveRegion action : editorPanel.currentPage.regions.selectedRegions) {
                // action.lineThickness.setSelectedItem(thickness);
                action.strLineThickness = thickness;
                ActiveRegionsExtraPanel.reload(currentPage.regions.getLastSelectedRegion());
                TutorialPanel.addLine("cmd", "Set the region line thickness: " + thickness);
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement());
            this.forceRepaint();
        }

    }

    public void setPenLineThickness(String thickness) {
        if (editorPanel.currentPage.regions.selectedRegions != null && editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            SketchletEditor.editorPanel.saveRegionUndo();
            for (ActiveRegion action : editorPanel.currentPage.regions.selectedRegions) {
                action.strPen = thickness;
                ActiveRegionsExtraPanel.reload(currentPage.regions.getLastSelectedRegion());
                TutorialPanel.addLine("cmd", "Set the region pen thickness: " + thickness);
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement());
            this.forceRepaint();
        }
    }

    public void updateTables() {
        ActiveRegionsFrame.updateTables();
        repaint();

    }

    public void openSketch(String sketch) {
        int n = -1;
        int i = 0;
        for (Page s : pages.pages) {
            if (s.title.equalsIgnoreCase(sketch)) {
                n = i;
                break;
            }
            i++;
        }

        if (n >= 0) {
            openSketchByIndex(n);
        }

    }

    public void openSketchAndWait(Page page) {
        this.selectSketch(page);
        /*
         * while (bLoading) { try { Thread.sleep(50); } catch (Exception e) { }
         * }
         */
    }

    public void openSketchByIndex(final int tabIndex) {
        if (currentPage != null && tabIndex == pages.pages.indexOf(currentPage)) {
            return;
        }

        if (bLoading) {
            return;
        }

        if (currentPage != null && controlPanel != null) {
            navigationHistory.add(currentPage);
            this.controlPanel.goBack.setEnabled(true);
        }
        if (editorPanel.tool != null) {
            editorPanel.tool.deactivate();
        }
        if (editorPanel.tabsModes.getSelectedIndex() == 2) {
            editorPanel.tabsModes.setSelectedIndex(0);
        }

        bLoading = true;
        if (!Workspace.bCloseOnPlaybackEnd) {
            if (Pages.msgFrame == null) {
                if (PlaybackFrame.playbackFrame != null && PlaybackFrame.playbackFrame[0] != null) {
                    MessageFrame.showMessage(PlaybackFrame.playbackFrame[0], Language.translate("Opening pages..."), PlaybackFrame.playbackFrame[0].playbackPanel);
                } else {
                    MessageFrame.showMessage(SketchletEditor.editorFrame, Language.translate("Opening page..."), SketchletEditor.editorPanel.centralPanel);
                    TutorialPanel.addLine("cmd", "Go to page: " + editorPanel.pages.pages.elementAt(tabIndex).title);
                }

                bCloseMsg = true;
            }
        }
        editorPanel.save();

        try {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {

                    try {
                        SketchletEditor.editorFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        editorPanel.flush();

                        editorPanel.enableControls();
                        editorPanel.currentPage.regions.selectedRegions = null;

                        if (tabIndex >= 0 && tabIndex < editorPanel.pages.pages.size()) {
                            GlobalProperties.setAndSave("last-sketch-index", "" + tabIndex);
                            if (editorPanel.currentPage != null) {
                                editorPanel.currentPage.deactivate(false);
                            }

                            editorPanel.masterPage = editorPanel.pages.getSketch("Master");
                            editorPanel.currentPage = editorPanel.pages.pages.elementAt(tabIndex);
                            editorPanel.editorFrame.setTitle(editorPanel.currentPage.title);
                            editorPanel.sketchListPanel.setPageTitle(editorPanel.currentPage.getTitle());
                            editorPanel.textArea.setText(editorPanel.currentPage.strTextAnnotation);
                            if (bCloseMsg) {
                                editorPanel.refresh();
                            }

                            if (editorPanel.currentPage != null) {
                                editorPanel.currentPage.activate(false);
                                editorPanel.reloadPlay();
                            }
                        }

                        SketchletEditor.editorPanel.perspectivePanel.repaint();
                        SketchletEditor.editorPanel.scrollPane.getViewport().setViewPosition(new Point(0, 0));

                        while (bSaving) {
                            Thread.sleep(10);
                        }

                        editorPanel.initImages();

                        if (Workspace.bCloseOnPlaybackEnd) {
                            Workspace.closeSplashScreen();
                        }
                        ActiveRegionsFrame.reload();
                        formulaToolbar.reload();

                        editorFrame.setVisible(true);
                        SketchletEditor.editorFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        ActivityLog.log("openSketch", "" + SketchletEditor.editorPanel.currentPage.title);

                        SketchletEditor.editorPanel.formulaToolbar.reload();
                    } catch (Throwable e) {
                        log.error(e);
                    } finally {
                        if (bCloseMsg && Pages.msgFrame != null) {
                            bCloseMsg = false;
                            bLoading = false;
                            MessageFrame.closeMessage();
                            editorPanel.repaint();
                        }
                        RefreshTime.update();
                        SketchletEditor.transparencyFactor = 1.0;
                        SketchletEditor.editorPanel.repaint();
                        SketchletEditor.editorPanel.pageVariablesPanel.refreshComponents();
                    }

                    bLoading = false;
                }
            });
        } catch (Throwable e2) {
            e2.printStackTrace();
        }

    }

    public BufferedImage getImage() {
        return currentPage.images[this.layer];
    }

    public void setImage(BufferedImage image) {
        currentPage.images[this.layer] = image;
        this.currentPage.imageUpdated[this.layer] = true;
        File file = this.currentPage.getLayerImageFile(this.layer);
        if (ImageCache.images != null && ImageCache.images.get(file) != null && image != null) {
            ImageCache.images.put(file, image);
        }
        revalidate();

    }

    public void setImageUpdated(boolean bUpdated) {
        this.currentPage.imageUpdated[this.layer] = bUpdated;
    }

    public boolean isInShiftMode() {
        return this.inShiftMode;
    }

    public boolean isInCtrlMode() {
        return this.inCtrlMode;
    }

    public void setColor(Color c) {
        this.color = c;
    }

    public Color getColor() {
        return this.color;
    }

    public Stroke getStroke() {
        return this.stroke;
    }

    public int getStrokeWidth() {
        return (this.colorToolbar == null || this.colorToolbar.slider == null) ? 0 : this.colorToolbar.slider.getValue();
    }

    public Graphics2D getImageGraphics() {
        return this.g2FreeHandDraw;
    }

    public SelectTool getSelectTool() {
        return this.selectTool;
    }

    public int getImageWidth() {
        return this.getSketchWidth();
    }

    public int getImageHeight() {
        return getSketchHeight();
    }

    public void setImageCursor(Cursor cursor) {
        this.setCursor(cursor);
    }

    public void repaintImage() {
        this.repaint();
    }

    public Component getComponent() {
        return this;
    }

    public void resizeImage(int w, int h) {
        this.saveImageUndo();
        for (int i = 0; i < SketchletEditor.editorPanel.currentPage.images.length; i++) {
            try {
                currentPage.pageWidth = w;
                currentPage.pageHeight = h;
                if (SketchletEditor.editorPanel.currentPage.images[i] != null) {
                    BufferedImage img = Workspace.createCompatibleImage(w, h);
                    Graphics2D g2 = img.createGraphics();
                    g2.drawImage(SketchletEditor.editorPanel.currentPage.images[i], 0, 0, w, h, null);
                    g2.dispose();
                    SketchletEditor.editorPanel.updateImage(i, img);
                }
            } catch (Throwable e) {
            }
            SketchletEditor.editorPanel.createGraphics();
            SketchletEditor.editorPanel.revalidate();
            RefreshTime.update();

            SketchletEditor.editorPanel.repaint();
        }

    }

    public void resizeCanvas(int w, int h) {
        this.saveImageUndo();
        for (int i = 0; i < SketchletEditor.editorPanel.currentPage.images.length; i++) {
            try {
                currentPage.pageWidth = w;
                currentPage.pageHeight = h;
                if (SketchletEditor.editorPanel.currentPage.images[i] != null) {
                    BufferedImage img = Workspace.createCompatibleImage(w, h);
                    Graphics2D g2 = img.createGraphics();
                    g2.drawImage(SketchletEditor.editorPanel.currentPage.images[i], 0, 0, null);
                    g2.dispose();
                    SketchletEditor.editorPanel.updateImage(i, img);
                }
            } catch (Throwable e) {
            }
            SketchletEditor.editorPanel.createGraphics();
            SketchletEditor.editorPanel.revalidate();
            RefreshTime.update();

            SketchletEditor.editorPanel.repaint();
        }

    }

    public Page getSketch() {
        return this.currentPage;
    }

    public Page getMasterPage() {
        return this.masterPage;
    }

    public BufferedImage getImage(int layer) {
        return currentPage.images[layer];
    }

    public void setImage(int layer, BufferedImage img) {
        if (currentPage.images[layer] != null) {
            currentPage.images[layer].flush();
        }
        currentPage.images[layer] = img;
        File file = this.currentPage.getLayerImageFile(layer);
        if (ImageCache.images != null && ImageCache.images.get(file) != null) {
            ImageCache.images.get(file).flush();
            if (img != null) {
                ImageCache.images.put(file, img);
            }
        }
    }

    public void updateImage(int layer, BufferedImage img) {
        if (currentPage.images[layer] != null) {
            currentPage.images[layer].flush();
        }
        currentPage.images[layer] = img;
        this.currentPage.imageUpdated[layer] = true;

        File file = this.currentPage.getLayerImageFile(layer);
        if (ImageCache.images != null && ImageCache.images.get(file) != null && img != null) {
            ImageCache.images.get(file).flush();
            ImageCache.images.put(file, img);
        }
    }

    public int getImageCount() {
        return currentPage.images.length;
    }

    public BufferedImage getMasterImage() {
        return this.masterImage;
    }

    public void setMasterImage(BufferedImage image) {
        this.masterImage = image;
    }

    public int getLayer() {
        return this.layer;
    }

    public EditorMode getMode() {
        return this.mode;
    }

    public int getMarginX() {
        return this.marginX;
    }

    public int getMarginY() {
        return this.marginY;
    }

    public double getScaleX() {
        return this.scale;
    }

    public double getScaleY() {
        return this.scale;
    }

    public void parentPaintComponent(Graphics2D g2) {
        super.paintComponent(g2);
    }

    public void repaintInternalPlaybackPanel() {
        if (this.internalPlaybackPanel != null) {
            this.internalPlaybackPanel.repaint();
        }
    }

    public float getWatering() {
        return watering;
    }

    public boolean shouldShapeFill() {
        return outlineType == 1 || outlineType == 2;
    }

    public boolean shouldShapeOutline() {
        return outlineType == 0 || outlineType == -1;
    }

    public void goToTrajectoryMode() {
        if (tabsModes.getTabCount() == 3) {
            tabsModes.removeTabAt(2);
        }

        tabsModes.addTab("", Workspace.createImageIcon("resources/trajectory.png"), new JPanel());
        tabsModes.setSelectedIndex(2);
        this.mode = EditorMode.SKETCHING;
        this.modeToolbar.addComponents(EditorMode.TRAJECTORY);
        SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.trajectoryPointsTool, null);
        SketchletEditor.editorPanel.createGraphics();
        SketchletEditor.editorPanel.setCursor();
        SketchletEditor.editorPanel.revalidate();
        RefreshTime.update();

        SketchletEditor.editorPanel.repaint();
    }

    public void openByID(String strID) {
        this.helpViewer.showHelpByID(strID);
    }

    public Component getPanel() {
        return SketchletEditor.editorPanel.scrollPane;
    }

    public String getName() {
        return "the main page area";
    }

    boolean bInPaint = false;

    public void paintGraphics(Graphics2D g) {
        paint(g);
    }

    public boolean isPainting() {
        return bInPaint;
    }

    public int getPaintWidth() {
        return getWidth();
    }

    public int getPaintHeigth() {
        return getHeight();
    }

    public Container getContainer() {
        return this;
    }
}
