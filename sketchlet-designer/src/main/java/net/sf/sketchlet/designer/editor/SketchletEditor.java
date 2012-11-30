package net.sf.sketchlet.designer.editor;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import net.sf.sketchlet.designer.editor.controllers.SketchletEditorMouseInputListener;
import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;
import net.sf.sketchlet.common.Refresh;
import net.sf.sketchlet.common.awt.AWTUtilitiesWrapper;
import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.context.SketchletPainter;
import net.sf.sketchlet.context.VariableUpdateListener;
import net.sf.sketchlet.designer.GlobalProperties;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.context.ActiveRegionContextImpl;
import net.sf.sketchlet.designer.context.PageContextImpl;
import net.sf.sketchlet.designer.editor.controllers.SketchletEditorClipboardController;
import net.sf.sketchlet.designer.editor.controllers.SketchletEditorDragAndDropController;
import net.sf.sketchlet.designer.editor.media.ImportMediaWatcher;
import net.sf.sketchlet.designer.editor.media.SketchletEditorImagesHandler;
import net.sf.sketchlet.designer.editor.printing.BufferedImagePrinter;
import net.sf.sketchlet.designer.editor.resize.ResizeDialog;
import net.sf.sketchlet.designer.editor.resize.ResizeInterface;
import net.sf.sketchlet.designer.editor.rulers.Ruler;
import net.sf.sketchlet.designer.editor.tool.*;
import net.sf.sketchlet.designer.editor.tool.notes.NoteDialog;
import net.sf.sketchlet.designer.editor.tool.stroke.WobbleStroke;
import net.sf.sketchlet.designer.editor.ui.LayerCheckBoxTabComponent;
import net.sf.sketchlet.designer.editor.ui.MemoryPanel;
import net.sf.sketchlet.designer.editor.ui.MessageFrame;
import net.sf.sketchlet.designer.editor.ui.desktop.DesktopPanel;
import net.sf.sketchlet.designer.editor.ui.desktop.SystemVariablesDialog;
import net.sf.sketchlet.designer.editor.ui.extraeditor.ActiveRegionsExtraPanel;
import net.sf.sketchlet.designer.editor.ui.extraeditor.ExtraEditorPanel;
import net.sf.sketchlet.designer.editor.ui.localvars.PageVariablesPanel;
import net.sf.sketchlet.designer.editor.ui.macros.ImageAreaSelect;
import net.sf.sketchlet.designer.editor.ui.macros.MacrosFrame;
import net.sf.sketchlet.designer.editor.ui.macros.MacrosTablePanel;
import net.sf.sketchlet.designer.editor.ui.page.PageDetailsPanel;
import net.sf.sketchlet.designer.editor.ui.page.PageListPanel;
import net.sf.sketchlet.designer.editor.ui.page.perspective.PerspectivePanel;
import net.sf.sketchlet.designer.editor.ui.page.spreadsheet.SpreadsheetPanel;
import net.sf.sketchlet.designer.editor.ui.pagetransition.StateDiagram;
import net.sf.sketchlet.designer.editor.ui.profiles.Profiles;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionImageEditor;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionPanel;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionToolbar;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionsFrame;
import net.sf.sketchlet.designer.editor.ui.region.menus.ActiveRegionMenu;
import net.sf.sketchlet.designer.editor.ui.region.menus.ActiveRegionPopupListener;
import net.sf.sketchlet.designer.editor.ui.timers.TimersTablePanel;
import net.sf.sketchlet.designer.editor.ui.timers.curve.CurvesFrame;
import net.sf.sketchlet.designer.editor.ui.toolbars.ColorToolbar;
import net.sf.sketchlet.designer.editor.ui.toolbars.FormulaToolbar;
import net.sf.sketchlet.designer.editor.ui.toolbars.ModeToolbar;
import net.sf.sketchlet.designer.editor.ui.toolbars.SimplePagesNavigationPanel;
import net.sf.sketchlet.designer.editor.ui.toolbars.SketchToolbar;
import net.sf.sketchlet.designer.editor.ui.undo.*;
import net.sf.sketchlet.designer.editor.ui.varspace.VariableSpacesFrame;
import net.sf.sketchlet.designer.eye.eye.EyeFrame;
import net.sf.sketchlet.designer.help.HelpViewer;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.designer.playback.ui.InteractionRecorder;
import net.sf.sketchlet.designer.playback.ui.InteractionSpaceFrame;
import net.sf.sketchlet.designer.playback.ui.PlaybackFrame;
import net.sf.sketchlet.designer.playback.ui.PlaybackPanel;
import net.sf.sketchlet.framework.model.imagecache.ImageCache;
import net.sf.sketchlet.framework.model.log.ActivityLog;
import net.sf.sketchlet.help.HelpInterface;
import net.sf.sketchlet.help.HelpUtils;
import net.sf.sketchlet.loaders.SketchletsSaxLoader;
import net.sf.sketchlet.loaders.pluginloader.GenericPluginFactory;
import net.sf.sketchlet.loaders.pluginloader.PluginInstance;
import net.sf.sketchlet.loaders.pluginloader.WidgetPluginFactory;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.model.ActiveRegions;
import net.sf.sketchlet.framework.model.events.EventMacro;
import net.sf.sketchlet.framework.model.Page;
import net.sf.sketchlet.framework.model.Pages;
import net.sf.sketchlet.framework.model.events.variable.VariableUpdateEventMacro;
import net.sf.sketchlet.framework.model.events.keyboard.KeyboardEventMacro;
import net.sf.sketchlet.framework.model.programming.macros.Macro;
import net.sf.sketchlet.framework.model.programming.macros.Macros;
import net.sf.sketchlet.framework.model.programming.timers.Timer;
import net.sf.sketchlet.framework.model.programming.timers.Timers;
import net.sf.sketchlet.plugin.SketchletPluginGUI;
import net.sf.sketchlet.framework.renderer.page.PageRenderer;
import net.sf.sketchlet.framework.renderer.page.PanelRenderer;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class SketchletEditor extends JDesktopPane implements KeyListener, VariableUpdateListener, Refresh, ToolInterface, ResizeInterface, PanelRenderer, HelpInterface, SketchletPainter {
    private static final Logger log = Logger.getLogger(SketchletEditor.class);
    private PageRenderer renderer = new PageRenderer(this);
    private FileDrop fileDrop;
    private static Pages pages;
    private Page currentPage;
    private Page masterPage;
    private SketchletEditorMode mode = SketchletEditorMode.SKETCHING;
    private JTabbedPane tabsModes = new JTabbedPane();
    private JTabbedPane tabsLayers = new JTabbedPane();
    private JTextArea textArea = new JTextArea();
    private static SketchletEditor editorPanel;
    private JToolBar navigationToolbar = new JToolBar();
    private static double transparencyFactor = 1.0;
    private int layer = 0;
    private static JFileChooser fc = new JFileChooser();
    private boolean dragging = false;
    private Graphics2D g2SketchletEditor = null;
    private ActiveRegionMenu activeRegionMenu = new ActiveRegionMenu();
    private boolean inShiftMode = false;
    private boolean inCtrlMode = false;
    private SimplePagesNavigationPanel controlPanel;
    private JPanel centralPanel = new JPanel(new BorderLayout());
    private PenTool penTool = new PenTool(this);
    private ColorPickerTool colorPickerTool = new ColorPickerTool(this);
    private BucketTool bucketTool = new BucketTool(this);
    private TransparentColorTool transparentColorTool = new TransparentColorTool(this);
    private MagicWandTool magicWandTool = new MagicWandTool(this);
    private LineTool lineTool = new LineTool(this);
    private RectTool rectTool = new RectTool(this);
    private OvalTool ovalTool = new OvalTool(this);
    private EraserTool eraserTool = new EraserTool(this);
    private SelectTool selectTool = new SelectTool(this);
    private FreeFormSelectTool freeFormSelectTool = new FreeFormSelectTool(this);
    private ActiveRegionTool activeRegionTool = new ActiveRegionTool(this);
    private ActiveRegionConnectorTool activeRegionConnectorTool = new ActiveRegionConnectorTool(this);
    private TrajectoryPointsTool trajectoryPointsTool = new TrajectoryPointsTool(this);
    private TrajectoryPenTool trajectory1PointsTool = new TrajectoryPenTool(this, 1);
    private TrajectoryPenTool trajectory2PointsTool = new TrajectoryPenTool(this, 2);
    private TrajectoryMoveTool trajectoryMoveTool = new TrajectoryMoveTool(this);
    private ActiveRegionSelectTool activeRegionSelectTool = new ActiveRegionSelectTool(this);
    private PostNoteTool postNoteTool = new PostNoteTool(this);
    private Tool tool = getActiveRegionSelectTool();
    private ActiveRegionPopupListener regionPopupListener = new ActiveRegionPopupListener();
    private ModeToolbar modeToolbar;
    private PerspectivePanel perspectivePanel;
    private PageVariablesPanel pageVariablesPanel;
    private SpreadsheetPanel spreadsheetPanel;
    private PageDetailsPanel pageDetailsPanel;
    private ExtraEditorPanel extraEditorPanel;
    private JPanel panelNorthMode;
    private ColorToolbar colorToolbar;
    private ActiveRegionToolbar activeRegionToolbar;
    private FormulaToolbar formulaToolbar;
    private SketchToolbar sketchToolbar = new SketchToolbar();
    private JTabbedPane tabsRight = new JTabbedPane();
    private JTabbedPane tabsProgramming = new JTabbedPane();
    private TimersTablePanel timersTablePanel;
    private MacrosTablePanel macrosTablePanel;
    private JTabbedPane tabsBrowser;
    private JTabbedPane tabsNavigator = new JTabbedPane();
    private boolean tabsVisible = true;
    private static boolean snapToGrid = false;
    private static boolean showPerspectiveLines = false;
    private static boolean pagePanelShown = false;
    private BufferedImage masterImage;
    private double scale = 1.0;
    private double oldScale = 0.0;
    private static int margin = 0;
    private static int marginX = 0;
    private static int marginY = 0;
    private Color color;
    private Stroke stroke;
    private int outlineType = 0;
    private float watering = 1.0f;
    private int statusX = 0;
    private int statusY = 0;
    private JPanel panelDrawingPanel;
    private JPanel editorPane = new JPanel(new BorderLayout());
    private JSplitPane splitPane;
    private PageListPanel pageListPanel;
    private JPanel drawingPanel = new JPanel(new BorderLayout());
    private JPanel mainSketchPanel = new JPanel(new BorderLayout());
    private PlaybackPanel internalPlaybackPanel;
    private JScrollPane internalPlaybackPanelScrollPane;
    private boolean extraEditorPanelVisible = false;
    private Ruler rulerHorizontal = new Ruler(Ruler.HORIZONTAL, true);
    private Ruler rulerVertical = new Ruler(Ruler.VERTICAL, true);
    private JPanel statusPanel;
    private HelpViewer helpViewer;
    private JSplitPane spllitPane;
    private static int pageTabIndex = 0;
    private static int ioservicesTabIndex = 1;
    private static int programmingTabIndex = 2;
    private static int timersTabIndex = 0;
    private static int macrosTabIndex = 1;
    private static int screenpokingTabIndex = 2;
    private static int scriptsTabIndex = 3;
    private JPanel panelModes;
    private static String[][] initProperties = null;
    private JMenu profilesMenu;
    private boolean bCloseMsg = false;
    private boolean loading = false;
    private Vector<Page> navigationHistory = new Vector<Page>();
    private static boolean bStatePanelUndocked = false;
    private static JFrame undockStateFrame = null;
    private boolean bRepaint = false;
    private long lastRepaintTime = 0;
    private static boolean bGUIReady = false;
    private SketchletEditorClipboardController editorClipboardController = new SketchletEditorClipboardController(this);
    private SketchletEditorImagesHandler sketchletImagesHandler = new SketchletEditorImagesHandler(this);
    private SketchletEditorDragAndDropController dragAndDropController = new SketchletEditorDragAndDropController(this);
    private List<UndoAction> undoRegionActions;
    private JButton hideButton = new JButton(Workspace.createImageIcon("resources/hide-right-icon.png"));
    private boolean objectPanelVisible = true;
    private boolean pasting = false;

    private ImportMediaWatcher mediaWatcher;
    private static JTextField statusBar = new JTextField(60);
    private MemoryPanel memoryPanel;

    public SketchletEditor() {
        ActivityLog.resetTime();
        this.setUndoRegionActions(new Vector());
        new Thread(new Runnable() {

            public void run() {
                try {
                    while (!closing) {
                        repaintIfNeeded();
                        Thread.sleep(50);
                    }
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }).start();
        setPagePanelShown(false);
        initSketches();

        HelpUtils.setHelpInterface(this);

        setColor(Color.BLACK);
        setStroke(new WobbleStroke(3, 1, 1));

        InteractionSpace.load();

        SketchletEditorMouseInputListener listener = new SketchletEditorMouseInputListener();
        this.addMouseListener(listener);
        this.addMouseMotionListener(listener);

        this.addMouseListener(getRegionPopupListener());

        this.addKeyListener(this);
        setFocusable(true);

        SketchletEditor.getStatusBar().setText("Sketching mode");

        VariablesBlackboard.getInstance().addVariablesUpdateListener(this);
        setCursor();

        this.setDesktopManager(new NoteDesktopManager());
        UIManager.put("InternalFrame.titlePaneHeight", new Integer(16));
        UIManager.put("InternalFrame.titleButtonHeight", new Integer(16));
        UIManager.put("InternalFrame.closeButtonToolTip", "Delete note");

        this.putClientProperty("JComponent.sizeVariant", "small");
        this.getTabsRight().putClientProperty("JComponent.sizeVariant", "small");

        SwingUtilities.updateComponentTreeUI(this);
        Workspace.getProcessRunner().getVariableSpacesHandler().prepareAdditionalVariables();

        mediaWatcher = new ImportMediaWatcher();
    }

    public static SketchletEditor getInstance() {
        return editorPanel;
    }

    public static void setEditorPanel(SketchletEditor editorPanel) {
        SketchletEditor.editorPanel = editorPanel;
    }

    public static Pages getPages() {
        return pages;
    }

    public static void setPages(Pages pages) {
        SketchletEditor.pages = pages;
    }

    public static double getTransparencyFactor() {
        return transparencyFactor;
    }

    public static void setTransparencyFactor(double transparencyFactor) {
        SketchletEditor.transparencyFactor = transparencyFactor;
    }

    public static boolean isSnapToGrid() {
        return snapToGrid;
    }

    public static void setSnapToGrid(boolean snapToGrid) {
        SketchletEditor.snapToGrid = snapToGrid;
    }

    public static boolean isShowPerspectiveLines() {
        return showPerspectiveLines;
    }

    public static void setShowPerspectiveLines(boolean showPerspectiveLines) {
        SketchletEditor.showPerspectiveLines = showPerspectiveLines;
    }

    public static boolean isPagePanelShown() {
        return pagePanelShown;
    }

    public static void setPagePanelShown(boolean pagePanelShown) {
        SketchletEditor.pagePanelShown = pagePanelShown;
    }

    public static int getMargin() {
        return margin;
    }

    public static void setMargin(int margin) {
        SketchletEditor.margin = margin;
    }

    public static int getPageTabIndex() {
        return pageTabIndex;
    }

    public static void setPageTabIndex(int pageTabIndex) {
        SketchletEditor.pageTabIndex = pageTabIndex;
    }

    public static int getIoservicesTabIndex() {
        return ioservicesTabIndex;
    }

    public static void setIoservicesTabIndex(int ioservicesTabIndex) {
        SketchletEditor.ioservicesTabIndex = ioservicesTabIndex;
    }

    public static int getProgrammingTabIndex() {
        return programmingTabIndex;
    }

    public static void setProgrammingTabIndex(int programmingTabIndex) {
        SketchletEditor.programmingTabIndex = programmingTabIndex;
    }

    public static int getTimersTabIndex() {
        return timersTabIndex;
    }

    public static void setTimersTabIndex(int timersTabIndex) {
        SketchletEditor.timersTabIndex = timersTabIndex;
    }

    public static int getMacrosTabIndex() {
        return macrosTabIndex;
    }

    public static void setMacrosTabIndex(int macrosTabIndex) {
        SketchletEditor.macrosTabIndex = macrosTabIndex;
    }

    public static int getScreenpokingTabIndex() {
        return screenpokingTabIndex;
    }

    public static void setScreenpokingTabIndex(int screenpokingTabIndex) {
        SketchletEditor.screenpokingTabIndex = screenpokingTabIndex;
    }

    public static int getScriptsTabIndex() {
        return scriptsTabIndex;
    }

    public static void setScriptsTabIndex(int scriptsTabIndex) {
        SketchletEditor.scriptsTabIndex = scriptsTabIndex;
    }

    public static String[][] getInitProperties() {
        return initProperties;
    }

    public static void setInitProperties(String[][] initProperties) {
        SketchletEditor.initProperties = initProperties;
    }

    public static boolean isbStatePanelUndocked() {
        return bStatePanelUndocked;
    }

    public static void setbStatePanelUndocked(boolean bStatePanelUndocked) {
        SketchletEditor.bStatePanelUndocked = bStatePanelUndocked;
    }

    public static JFrame getUndockStateFrame() {
        return undockStateFrame;
    }

    public static void setUndockStateFrame(JFrame undockStateFrame) {
        SketchletEditor.undockStateFrame = undockStateFrame;
    }

    public static void setMarginX(int marginX) {
        SketchletEditor.marginX = marginX;
    }

    public static void setMarginY(int marginY) {
        SketchletEditor.marginY = marginY;
    }

    public static boolean isbGUIReady() {
        return bGUIReady;
    }

    public static void setbGUIReady(boolean bGUIReady) {
        SketchletEditor.bGUIReady = bGUIReady;
    }

    public static JTextField getStatusBar() {
        return statusBar;
    }

    public void update(Graphics g) {
        if (getRenderer() != null) {
            getRenderer().prepare(false, true);
        }
        if (System.currentTimeMillis() - lastRepaintTime > 50) {
            super.update(g);
            lastRepaintTime = System.currentTimeMillis();
        }
    }

    public void repaint() {
        if (RefreshTime.shouldRefresh()) {
            setbRepaint(true);
        }
    }

    public void repaintIfNeeded() {
        if (PlaybackFrame.playbackFrame != null) {
            PlaybackFrame.repaintAllFramesIfNeeded();
            setbRepaint(true);
        } else if (this.getInternalPlaybackPanel() != null) {
            this.getInternalPlaybackPanel().repaintIfNeeded();
            refreshTables();
        } else {
            if (isbRepaint()) {
                setbRepaint(false);
                super.repaint();

                if (getRulerHorizontal() != null) {
                    getRulerHorizontal().repaint();
                }
                if (getRulerVertical() != null) {
                    getRulerVertical().repaint();
                }
            }
            refreshTables();
        }
    }

    public void refreshTables() {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                if (Workspace.getMainPanel().getSketchletPanel().globalVariablesPanel.bTableDirty) {
                    Workspace.getMainPanel().getSketchletPanel().globalVariablesPanel.bTableDirty = false;
                    Workspace.getMainPanel().getSketchletPanel().globalVariablesPanel.variablesTableModel.fireTableDataChanged();
                }
                if (Workspace.getMainPanel().getSketchletPanel().globalVariablesPanel.bTableUpdated) {
                    Workspace.getMainPanel().getSketchletPanel().globalVariablesPanel.table.repaint();
                    Workspace.getMainPanel().getSketchletPanel().globalVariablesPanel.bTableUpdated = false;
                }
            }
        });
    }

    public void addNote(int x, int y) {
        NoteDialog note = new NoteDialog(getInstance().getMarginX() + x - 8, getInstance().getMarginY() + y - 33);
        getCurrentPage().getNotes().add(note);
        addAndShowNote(note);
    }

    public void addAndShowNote(NoteDialog note) {
        NoteDialog _note = new NoteDialog(note.getX(), note.getY());
        _note.setBounds(note.getX(), note.getY(), note.getWidth(), note.getHeight());
        _note.setNoteText(note.getNoteText());
        getCurrentPage().getNotes().remove(note);
        getCurrentPage().getNotes().add(_note);
        remove(note);
        add(_note, JDesktopPane.PALETTE_LAYER);
        _note.setVisible(true);
        if (note.isMinimized()) {
            _note.setMinimized(true);

            this.getDesktopManager().iconifyFrame(_note);
            try {
                _note.setIcon(true);
            } catch (Throwable e) {
                log.error(e);
            }
            _note.setOriginalWidth(note.getOriginalWidth());
            _note.setOriginalHeight(note.getOriginalHeight());
        }
    }

    public void removeNote(NoteDialog note) {
        note.setVisible(false);
        this.remove(note);
    }

    public void loadLayersTab() {
        getInstance().getTabsLayers().removeAll();

        int i = 0;
        for (i = 0; i < Page.NUMBER_OF_LAYERS; i++) {
            getInstance().getTabsLayers().add("" + (i + 1), new JPanel());
            getInstance().getTabsLayers().setTabComponentAt(i, new LayerCheckBoxTabComponent(i, getInstance().getTabsLayers()));
        }
        getInstance().getTabsLayers().setSelectedIndex(0);
    }

    public static void showPerspectivePanel() {
        showStatePanel(PageDetailsPanel.perspectiveTabIndex, 0);
    }

    public static void showSpreadsheetPanel() {
        showStatePanel(PageDetailsPanel.spreadsheetTabIndex, 0);
    }

    public static void showStatePanel(int tab, int subTab) {
        showExtraEditorPanel(ExtraEditorPanel.indexPage);

        getInstance().getPageDetailsPanel().tabs.setSelectedIndex(tab);
        if (tab == 0) {
            getInstance().getPageDetailsPanel().tabs1.setSelectedIndex(subTab);
        } else if (tab == 1) {
            getInstance().getPageDetailsPanel().tabs2.setSelectedIndex(subTab);
        } else {
        }
    }

    public static void undockStatePanel() {
        showStatePanel();
        setUndockStateFrame(new JFrame());
        getUndockStateFrame().getRootPane().putClientProperty("Window.style", "small");
        getUndockStateFrame().addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                setbStatePanelUndocked(false);
                getInstance().setPagePanelShown(false);
            }

            public void windowActivated(WindowEvent e) {
                AWTUtilitiesWrapper.setWindowOpacity(getUndockStateFrame(), 1.0f);
            }

            public void windowDeactivated(WindowEvent e) {
                AWTUtilitiesWrapper.setWindowOpacity(getUndockStateFrame(), 0.5f);
            }
        });

        getUndockStateFrame().setAlwaysOnTop(true);
        getUndockStateFrame().setTitle("Page Events and Propertites");
        getUndockStateFrame().add(getInstance().getPageDetailsPanel());
        Dimension d = getInstance().getPageDetailsPanel().getSize();
        getUndockStateFrame().setSize(new Dimension((int) d.getWidth(), (int) d.getHeight() + 60));
        getUndockStateFrame().setVisible(true);
        getUndockStateFrame().setIconImage(Workspace.createImageIcon("resources/editor.gif", "").getImage());
        bUndocked = true;
    }

    public static void dockStatePanel() {
        setbStatePanelUndocked(false);
        if (getUndockStateFrame() != null) {
            getUndockStateFrame().setVisible(false);
            getInstance().setPagePanelShown(false);
            showStatePanel();
        }
    }

    public static void showStatePanel() {
    }

    public Page getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Page currentPage) {
        this.currentPage = currentPage;
    }

    public void setMasterPage(Page masterPage) {
        this.masterPage = masterPage;
    }

    public PenTool getPenTool() {
        return penTool;
    }

    public void setPenTool(PenTool penTool) {
        this.penTool = penTool;
    }

    public ColorPickerTool getColorPickerTool() {
        return colorPickerTool;
    }

    public void setColorPickerTool(ColorPickerTool colorPickerTool) {
        this.colorPickerTool = colorPickerTool;
    }

    public BucketTool getBucketTool() {
        return bucketTool;
    }

    public void setBucketTool(BucketTool bucketTool) {
        this.bucketTool = bucketTool;
    }

    public TransparentColorTool getTransparentColorTool() {
        return transparentColorTool;
    }

    public void setTransparentColorTool(TransparentColorTool transparentColorTool) {
        this.transparentColorTool = transparentColorTool;
    }

    public MagicWandTool getMagicWandTool() {
        return magicWandTool;
    }

    public void setMagicWandTool(MagicWandTool magicWandTool) {
        this.magicWandTool = magicWandTool;
    }

    public LineTool getLineTool() {
        return lineTool;
    }

    public void setLineTool(LineTool lineTool) {
        this.lineTool = lineTool;
    }

    public RectTool getRectTool() {
        return rectTool;
    }

    public void setRectTool(RectTool rectTool) {
        this.rectTool = rectTool;
    }

    public OvalTool getOvalTool() {
        return ovalTool;
    }

    public void setOvalTool(OvalTool ovalTool) {
        this.ovalTool = ovalTool;
    }

    public EraserTool getEraserTool() {
        return eraserTool;
    }

    public void setEraserTool(EraserTool eraserTool) {
        this.eraserTool = eraserTool;
    }

    public void setSelectTool(SelectTool selectTool) {
        this.selectTool = selectTool;
    }

    public FreeFormSelectTool getFreeFormSelectTool() {
        return freeFormSelectTool;
    }

    public void setFreeFormSelectTool(FreeFormSelectTool freeFormSelectTool) {
        this.freeFormSelectTool = freeFormSelectTool;
    }

    public ActiveRegionTool getActiveRegionTool() {
        return activeRegionTool;
    }

    public void setActiveRegionTool(ActiveRegionTool activeRegionTool) {
        this.activeRegionTool = activeRegionTool;
    }

    public ActiveRegionConnectorTool getActiveRegionConnectorTool() {
        return activeRegionConnectorTool;
    }

    public void setActiveRegionConnectorTool(ActiveRegionConnectorTool activeRegionConnectorTool) {
        this.activeRegionConnectorTool = activeRegionConnectorTool;
    }

    public TrajectoryPointsTool getTrajectoryPointsTool() {
        return trajectoryPointsTool;
    }

    public void setTrajectoryPointsTool(TrajectoryPointsTool trajectoryPointsTool) {
        this.trajectoryPointsTool = trajectoryPointsTool;
    }

    public TrajectoryPenTool getTrajectory1PointsTool() {
        return trajectory1PointsTool;
    }

    public void setTrajectory1PointsTool(TrajectoryPenTool trajectory1PointsTool) {
        this.trajectory1PointsTool = trajectory1PointsTool;
    }

    public TrajectoryPenTool getTrajectory2PointsTool() {
        return trajectory2PointsTool;
    }

    public void setTrajectory2PointsTool(TrajectoryPenTool trajectory2PointsTool) {
        this.trajectory2PointsTool = trajectory2PointsTool;
    }

    public TrajectoryMoveTool getTrajectoryMoveTool() {
        return trajectoryMoveTool;
    }

    public void setTrajectoryMoveTool(TrajectoryMoveTool trajectoryMoveTool) {
        this.trajectoryMoveTool = trajectoryMoveTool;
    }

    public ActiveRegionSelectTool getActiveRegionSelectTool() {
        return activeRegionSelectTool;
    }

    public void setActiveRegionSelectTool(ActiveRegionSelectTool activeRegionSelectTool) {
        this.activeRegionSelectTool = activeRegionSelectTool;
    }

    public PostNoteTool getPostNoteTool() {
        return postNoteTool;
    }

    public void setPostNoteTool(PostNoteTool postNoteTool) {
        this.postNoteTool = postNoteTool;
    }

    public Tool getTool() {
        return tool;
    }

    public void setTool(Tool tool) {
        this.tool = tool;
    }

    public ActiveRegionPopupListener getRegionPopupListener() {
        return regionPopupListener;
    }

    public void setRegionPopupListener(ActiveRegionPopupListener regionPopupListener) {
        this.regionPopupListener = regionPopupListener;
    }

    public ActiveRegionMenu getActiveRegionMenu() {
        return activeRegionMenu;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public JPanel getCentralPanel() {
        return centralPanel;
    }

    public FileDrop getFileDrop() {
        return fileDrop;
    }

    public void setFileDrop(FileDrop fileDrop) {
        this.fileDrop = fileDrop;
    }

    public void setInShiftMode(boolean inShiftMode) {
        this.inShiftMode = inShiftMode;
    }

    public void setInCtrlMode(boolean inCtrlMode) {
        this.inCtrlMode = inCtrlMode;
    }

    public JTabbedPane getTabsModes() {
        return tabsModes;
    }

    public JTabbedPane getTabsLayers() {
        return tabsLayers;
    }

    public SimplePagesNavigationPanel getControlPanel() {
        return controlPanel;
    }

    public void setControlPanel(SimplePagesNavigationPanel controlPanel) {
        this.controlPanel = controlPanel;
    }

    public JToolBar getNavigationToolbar() {
        return navigationToolbar;
    }

    public void setNavigationToolbar(JToolBar navigationToolbar) {
        this.navigationToolbar = navigationToolbar;
    }

    public PageRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(PageRenderer renderer) {
        this.renderer = renderer;
    }

    public ModeToolbar getModeToolbar() {
        return modeToolbar;
    }

    public void setModeToolbar(ModeToolbar modeToolbar) {
        this.modeToolbar = modeToolbar;
    }

    public PerspectivePanel getPerspectivePanel() {
        return perspectivePanel;
    }

    public void setPerspectivePanel(PerspectivePanel perspectivePanel) {
        this.perspectivePanel = perspectivePanel;
    }

    public PageVariablesPanel getPageVariablesPanel() {
        return pageVariablesPanel;
    }

    public void setPageVariablesPanel(PageVariablesPanel pageVariablesPanel) {
        this.pageVariablesPanel = pageVariablesPanel;
    }

    public SpreadsheetPanel getSpreadsheetPanel() {
        return spreadsheetPanel;
    }

    public void setSpreadsheetPanel(SpreadsheetPanel spreadsheetPanel) {
        this.spreadsheetPanel = spreadsheetPanel;
    }

    public PageDetailsPanel getPageDetailsPanel() {
        return pageDetailsPanel;
    }

    public void setPageDetailsPanel(PageDetailsPanel pageDetailsPanel) {
        this.pageDetailsPanel = pageDetailsPanel;
    }

    public ExtraEditorPanel getExtraEditorPanel() {
        return extraEditorPanel;
    }

    public void setExtraEditorPanel(ExtraEditorPanel extraEditorPanel) {
        this.extraEditorPanel = extraEditorPanel;
    }

    public JPanel getPanelNorthMode() {
        return panelNorthMode;
    }

    public void setPanelNorthMode(JPanel panelNorthMode) {
        this.panelNorthMode = panelNorthMode;
    }

    public ColorToolbar getColorToolbar() {
        return colorToolbar;
    }

    public void setColorToolbar(ColorToolbar colorToolbar) {
        this.colorToolbar = colorToolbar;
    }

    public ActiveRegionToolbar getActiveRegionToolbar() {
        return activeRegionToolbar;
    }

    public void setActiveRegionToolbar(ActiveRegionToolbar activeRegionToolbar) {
        this.activeRegionToolbar = activeRegionToolbar;
    }

    public FormulaToolbar getFormulaToolbar() {
        return formulaToolbar;
    }

    public void setFormulaToolbar(FormulaToolbar formulaToolbar) {
        this.formulaToolbar = formulaToolbar;
    }

    public SketchToolbar getSketchToolbar() {
        return sketchToolbar;
    }

    public void setSketchToolbar(SketchToolbar sketchToolbar) {
        this.sketchToolbar = sketchToolbar;
    }

    public JTabbedPane getTabsRight() {
        return tabsRight;
    }

    public void setTabsRight(JTabbedPane tabsRight) {
        this.tabsRight = tabsRight;
    }

    public JTabbedPane getTabsProgramming() {
        return tabsProgramming;
    }

    public void setTabsProgramming(JTabbedPane tabsProgramming) {
        this.tabsProgramming = tabsProgramming;
    }

    public TimersTablePanel getTimersTablePanel() {
        return timersTablePanel;
    }

    public void setTimersTablePanel(TimersTablePanel timersTablePanel) {
        this.timersTablePanel = timersTablePanel;
    }

    public MacrosTablePanel getMacrosTablePanel() {
        return macrosTablePanel;
    }

    public void setMacrosTablePanel(MacrosTablePanel macrosTablePanel) {
        this.macrosTablePanel = macrosTablePanel;
    }

    public JTabbedPane getTabsBrowser() {
        return tabsBrowser;
    }

    public void setTabsBrowser(JTabbedPane tabsBrowser) {
        this.tabsBrowser = tabsBrowser;
    }

    public JTabbedPane getTabsNavigator() {
        return tabsNavigator;
    }

    public void setTabsNavigator(JTabbedPane tabsNavigator) {
        this.tabsNavigator = tabsNavigator;
    }

    public boolean isTabsVisible() {
        return tabsVisible;
    }

    public void setTabsVisible(boolean tabsVisible) {
        this.tabsVisible = tabsVisible;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double getOldScale() {
        return oldScale;
    }

    public void setOldScale(double oldScale) {
        this.oldScale = oldScale;
    }

    public int getOutlineType() {
        return outlineType;
    }

    public void setOutlineType(int outlineType) {
        this.outlineType = outlineType;
    }

    public JPanel getEditorPane() {
        return editorPane;
    }

    public void setEditorPane(JPanel editorPane) {
        this.editorPane = editorPane;
    }

    public JSplitPane getSplitPane() {
        return splitPane;
    }

    public void setSplitPane(JSplitPane splitPane) {
        this.splitPane = splitPane;
    }

    public PageListPanel getPageListPanel() {
        return pageListPanel;
    }

    public void setPageListPanel(PageListPanel pageListPanel) {
        this.pageListPanel = pageListPanel;
    }

    public JPanel getDrawingPanel() {
        return drawingPanel;
    }

    public void setDrawingPanel(JPanel drawingPanel) {
        this.drawingPanel = drawingPanel;
    }

    public JPanel getMainSketchPanel() {
        return mainSketchPanel;
    }

    public void setMainSketchPanel(JPanel mainSketchPanel) {
        this.mainSketchPanel = mainSketchPanel;
    }

    public PlaybackPanel getInternalPlaybackPanel() {
        return internalPlaybackPanel;
    }

    public void setInternalPlaybackPanel(PlaybackPanel internalPlaybackPanel) {
        this.internalPlaybackPanel = internalPlaybackPanel;
    }

    public JScrollPane getInternalPlaybackPanelScrollPane() {
        return internalPlaybackPanelScrollPane;
    }

    public void setInternalPlaybackPanelScrollPane(JScrollPane internalPlaybackPanelScrollPane) {
        this.internalPlaybackPanelScrollPane = internalPlaybackPanelScrollPane;
    }

    public boolean isExtraEditorPanelVisible() {
        return extraEditorPanelVisible;
    }

    public void setExtraEditorPanelVisible(boolean extraEditorPanelVisible) {
        this.extraEditorPanelVisible = extraEditorPanelVisible;
    }

    public Ruler getRulerHorizontal() {
        return rulerHorizontal;
    }

    public void setRulerHorizontal(Ruler rulerHorizontal) {
        this.rulerHorizontal = rulerHorizontal;
    }

    public Ruler getRulerVertical() {
        return rulerVertical;
    }

    public void setRulerVertical(Ruler rulerVertical) {
        this.rulerVertical = rulerVertical;
    }

    public JPanel getStatusPanel() {
        return statusPanel;
    }

    public void setStatusPanel(JPanel statusPanel) {
        this.statusPanel = statusPanel;
    }

    public HelpViewer getHelpViewer() {
        return helpViewer;
    }

    public void setHelpViewer(HelpViewer helpViewer) {
        this.helpViewer = helpViewer;
    }

    public JMenu getProfilesMenu() {
        return profilesMenu;
    }

    public void setProfilesMenu(JMenu profilesMenu) {
        this.profilesMenu = profilesMenu;
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public Vector<Page> getNavigationHistory() {
        return navigationHistory;
    }

    public void setNavigationHistory(Vector<Page> navigationHistory) {
        this.navigationHistory = navigationHistory;
    }

    public boolean isbRepaint() {
        return bRepaint;
    }

    public void setbRepaint(boolean bRepaint) {
        this.bRepaint = bRepaint;
    }

    public void setWatering(float watering) {
        this.watering = watering;
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    public JSplitPane getSpllitPane() {
        return spllitPane;
    }

    public void setSpllitPane(JSplitPane spllitPane) {
        this.spllitPane = spllitPane;
    }

    public JPanel getPanelModes() {
        return panelModes;
    }

    public void setPanelModes(JPanel panelModes) {
        this.panelModes = panelModes;
    }

    public List<UndoAction> getUndoRegionActions() {
        return undoRegionActions;
    }

    public void setUndoRegionActions(List<UndoAction> undoRegionActions) {
        this.undoRegionActions = undoRegionActions;
    }

    public SketchletEditorImagesHandler getSketchletImagesHandler() {
        return sketchletImagesHandler;
    }

    public void setSketchletImagesHandler(SketchletEditorImagesHandler sketchletImagesHandler) {
        this.sketchletImagesHandler = sketchletImagesHandler;
    }

    public SketchletEditorClipboardController getEditorClipboardController() {
        return editorClipboardController;
    }

    public void setEditorClipboardController(SketchletEditorClipboardController editorClipboardController) {
        this.editorClipboardController = editorClipboardController;
    }

    public int getStatusX() {
        return statusX;
    }

    public void setStatusX(int statusX) {
        this.statusX = statusX;
    }

    public int getStatusY() {
        return statusY;
    }

    public void setStatusY(int statusY) {
        this.statusY = statusY;
    }

    public JPanel getPanelDrawingPanel() {
        return panelDrawingPanel;
    }

    public void setPanelDrawingPanel(JPanel panelDrawingPanel) {
        this.panelDrawingPanel = panelDrawingPanel;
    }

    public boolean isObjectPanelVisible() {
        return objectPanelVisible;
    }

    public void setObjectPanelVisible(boolean objectPanelVisible) {
        this.objectPanelVisible = objectPanelVisible;
    }

    public JButton getHideButton() {
        return hideButton;
    }

    public void setHideButton(JButton hideButton) {
        this.hideButton = hideButton;
    }

    public boolean isPasting() {
        return pasting;
    }

    public void setPasting(boolean pasting) {
        this.pasting = pasting;
    }

    public SketchletEditorDragAndDropController getDragAndDropController() {
        return dragAndDropController;
    }

    public void setDragAndDropController(SketchletEditorDragAndDropController dragAndDropController) {
        this.dragAndDropController = dragAndDropController;
    }

    class NoteDesktopManager extends DefaultDesktopManager {

        public void iconifyFrame(JInternalFrame f) {
            int x = f.getX();
            int y = f.getY();
            if (f instanceof NoteDialog) {
                ((NoteDialog) f).setOriginalWidth(f.getWidth());
                ((NoteDialog) f).setOriginalHeight(f.getHeight());
                ((NoteDialog) f).setMinimized(true);
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
                ((NoteDialog) f).setMinimized(false);
                f.setBounds(x, y, ((NoteDialog) f).getOriginalWidth(), ((NoteDialog) f).getOriginalHeight());
            }
        }
    }

    public void reloadPlay() {
        if (PlaybackFrame.playbackFrame != null) {
            PlaybackFrame.play(this.getPages(), getCurrentPage());
        }
        if (getInstance() != null && getInstance().getCurrentPage() != null && getInstance().getInternalPlaybackPanel() != null) {
            updateTables();
            save();

            getInstance().getInternalPlaybackPanel().showSketch(getInstance().getCurrentPage());
            getInstance().getInternalPlaybackPanel().revalidate();
            RefreshTime.update();
            getInstance().getInternalPlaybackPanel().repaint();
            getInstance().getInternalPlaybackPanelScrollPane().revalidate();
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
        if (this.getTool() != null) {
            this.getTool().deactivate();
        }

        ActivityLog.log("setTool", tool.getName(), tool.getIconFileName(), source);

        getInstance().setTool(tool);
        getInstance().setCursor();
        getInstance().requestFocus();

        if (tool instanceof ActiveRegionSelectTool || tool instanceof ActiveRegionTool || tool instanceof ActiveRegionConnectorTool
                || tool instanceof TrajectoryMoveTool || tool instanceof TrajectoryPenTool || tool instanceof TrajectoryPointsTool) {
            getInstance().getActiveRegionToolbar().toolChanged(tool);
        } else {
            getInstance().getColorToolbar().toolChanged(tool);
            getInstance().getCurrentPage().getRegions().getMouseHelper().deselectRegions();
        }
    }

    public int getSketchWidth() {
        if (getCurrentPage().getImages() != null && getCurrentPage().getImages()[0] != null) {
            return getCurrentPage().getImages()[0].getWidth();
        } else {
            return getWidth();
        }
    }

    public int getSketchHeight() {
        if (getCurrentPage().getImages() != null && getCurrentPage().getImages()[0] != null) {
            return getCurrentPage().getImages()[0].getHeight();
        } else {
            return getHeight();
        }
    }

    public void initSketches() {
        if (this.getPages() == null) {
            this.setPages(new Pages());
        }

        if (this.getPages().getPages().size() > 0) {
            setMasterPage(this.getPages().getSketch("Master"));
            this.setMasterPage(this.getPages().getSketch("Master"));
            int index = DesktopPanel.selectedPageIndex;
            if (index < 0 || index >= getPages().getPages().size()) {
                index = 0;
            }
            this.setCurrentPage(this.getPages().getPages().elementAt(index));
            editorFrame.setTitle(getCurrentPage().getTitle());
            textArea.setText(getCurrentPage().getTextAnnotation());

            if (getPageDetailsPanel() != null) {
                getPageDetailsPanel().load();
            }

            reloadPlay();
        }
    }

    long lastRepaintTime2;

    public synchronized void paintComponent(Graphics g) {
        long t = System.currentTimeMillis();
        super.paintComponent(g);

        if (getRenderer() != null) {
            calculateMargins();
            getRenderer().draw(g, false, true, false);
            if (getMarginX() > 0 || getMarginY() > 0) {
                int w = SketchletEditor.getInstance().scrollPane.getViewport().getWidth();
                int h = SketchletEditor.getInstance().scrollPane.getViewport().getHeight();

                g.setColor(new Color(192, 192, 192));

                int x1 = (int) (getMarginX() * SketchletEditor.getInstance().getScale());
                int y1 = (int) (getMarginY() * SketchletEditor.getInstance().getScale());
                int x2 = (int) (w - getMarginX() * SketchletEditor.getInstance().getScale());
                int y2 = (int) (h - getMarginY() * SketchletEditor.getInstance().getScale());

                g.fillRect(0, 0, x1, h);
                g.fillRect(x1, 0, x2 - x1, y1);
                g.fillRect(x2, 0, getMarginX(), h);
                g.fillRect(x1, y2, x2 - x1, getMarginY());

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
        final int w = SketchletEditor.getInstance().scrollPane.getViewport().getWidth();
        final int h = SketchletEditor.getInstance().scrollPane.getViewport().getHeight();

        int w2 = getRenderer().getPanelRenderer().getImage(0) != null ? getRenderer().getPanelRenderer().getImage(0).getWidth() : (int) InteractionSpace.getSketchWidth();
        int h2 = getRenderer().getPanelRenderer().getImage(0) != null ? getRenderer().getPanelRenderer().getImage(0).getHeight() : (int) InteractionSpace.getSketchHeight();

        if (w < w2 * SketchletEditor.getInstance().getScale()) {
            SketchletEditor.setMarginX(0);
        } else {
            SketchletEditor.setMarginX((int) ((w / SketchletEditor.getInstance().getScale() - w2) / 2));
        }

        if (h < h2 * SketchletEditor.getInstance().getScale()) {
            SketchletEditor.setMarginY(0);
        } else {
            SketchletEditor.setMarginY((int) ((h / SketchletEditor.getInstance().getScale() - h2) / 2));
        }
    }

    public void extraDraw(Graphics2D g2) {
        if (getTool() != null) {
            getTool().draw(g2);
        }
    }

    public Graphics2D createGraphics() {
        if (g2SketchletEditor != null) {
            g2SketchletEditor.dispose();
        }
        if (getCurrentPage().getImages() != null && layer >= 0) {
            if (getCurrentPage().getImages()[layer] == null && getCurrentPage().getLayerActive()[layer]) {
                int w = Toolkit.getDefaultToolkit().getScreenSize().width;
                int h = Toolkit.getDefaultToolkit().getScreenSize().height;
                if (getCurrentPage().getImages()[0] != null) {
                    w = getCurrentPage().getImages()[0].getWidth();
                    h = getCurrentPage().getImages()[0].getHeight();
                }
                //renderer.initImage(layer);
                if (getCurrentPage().getImages()[layer] == null) {
                    this.updateImage(layer, Workspace.createCompatibleImage(w, h, getCurrentPage().getImages()[layer]));
                }
            } else if (getCurrentPage().getImages()[layer] == null) {
                return null;
            }
            g2SketchletEditor = getCurrentPage().getImages()[layer].createGraphics();
            g2SketchletEditor.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            g2SketchletEditor = null;
        }

        return g2SketchletEditor;
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
            if (getTool() != null) {
                getTool().onUndo();
            }

            while (this.getUndoRegionActions().size() > 0) {
                UndoAction ua = (UndoAction) this.getUndoRegionActions().remove(this.getUndoRegionActions().size() - 1);

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
            if (ActiveRegionPanel.getCurrentActiveRegionPanel() != null) {
                ActiveRegionPanel.getCurrentActiveRegionPanel().refreshComponents();
            }
            if (SketchletEditor.getInstance().getPerspectivePanel() != null) {
                SketchletEditor.getInstance().getPerspectivePanel().refresh();
            }
            this.getFormulaToolbar().refresh();
            enableControls();
            this.createGraphics();

            if (getTool() != null) {
                getTool().activate();
            }
        } finally {
            undoing = false;
        }
    }

    protected void clearImageMemory() {
        if (this.getCurrentPage() == null) {
            return;
        }
        if (getCurrentPage().getImages() != null) {
            for (int i = 0; i < getCurrentPage().getImages().length; i++) {
                BufferedImage img = getCurrentPage().getImages()[i];
                if (img != null) {
                    img.flush();
                    getCurrentPage().getImages()[i] = null;
                }
            }
        }
        getCurrentPage().flush();
    }

    protected void flush() {
        if (this.getCurrentPage() == null) {
            return;
        }
        getCurrentPage().flush();
    }

    protected void initImages() {
        if (this.getCurrentPage() == null) {
            return;
        }
        getCurrentPage().initRegionImages();
    }

    public void clearAll() {
        ComplexUndoAction ua = new ComplexUndoAction();
        for (int i = 0; i < getCurrentPage().getImages().length; i++) {
            BufferedImage img = getCurrentPage().getImages()[i];
            if (img != null) {
                ua.add(new SketchImageChangedUndoAction(this.getCurrentPage(), img, i));
                // currentSketch.redoRegionActions[i].removeAllElements();
                this.getSketchletImagesHandler().clearImage(i, false);
            }
        }
        ua.add(new RegionsDeletedUndoAction(getCurrentPage().getRegions().getRegions()));
        this.getUndoRegionActions().add(ua);
        checkUndo();
        this.getCurrentPage().getRegions().getMouseHelper().setSelectedRegions(null);
        this.getCurrentPage().getRegions().getRegions().removeAllElements();

        enableControls();
        RefreshTime.update();
        SketchletEditor.getInstance().repaint();
    }

    public void clearAllImage() {
        saveImageUndo();
        this.clearImage();
        RefreshTime.update();
        SketchletEditor.getInstance().repaint();
    }

    protected void clearImage() {
        this.getSketchletImagesHandler().clearImage(layer, true);
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
        if (bInPreviewMode || bSaving || !isbGUIReady()) {
            synchronized (lockSave) {
                lockSave.notifyAll();
            }
            return;
        }
        if (getCurrentPage() == null || getPages().getPages().indexOf(getCurrentPage()) < 0) {
            synchronized (lockSave) {
                lockSave.notifyAll();
            }
            return;
        }

        if (this.getTool() != null) {
            this.getTool().deactivate();
        }
        if (ActiveRegionImageEditor.getCurrentTool() != null) {
            ActiveRegionImageEditor.getCurrentTool().deactivate();
        }

        bSaving = true;

        new Thread(new Runnable() {

            public void run() {
                try {
                    enableControls();

                    if (editorFrame != null) {
                        editorFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    }

                    getCurrentPage().setTextAnnotation(textArea.getText());
                    getCurrentPage().save((layer >= 0 && getUndoRegionActions().size() > 0));

                    getPages().savePageSorting();
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
        if (this.getCurrentPage() == null) {
            return;
        }
        try {
            for (int i = 0; i < Page.NUMBER_OF_LAYERS; i++) {
                if (this.getCurrentPage().getImageUpdated()[i]) {
                    File file = this.getCurrentPage().getLayerImageFile(i);
                    if (file == null) {
                        if (i == 0) {
                            file = new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "Sketch_" + getCurrentPage().getId() + ".png");
                        } else {
                            file = new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "Sketch_" + getCurrentPage().getId() + "_" + (i + 1) + ".png");
                        }
                    }
                    ImageCache.write(getCurrentPage().getImages()[i], file);
                    this.getCurrentPage().getImageUpdated()[i] = false;
                }
            }
        } catch (IOException e) {
            log.error(e);
        }
    }

    public void openExternalEditor() {
        if (this.getCurrentPage() == null) {
            return;
        }

        this.getTool().deactivate();
        this.saveImageUndo();

        this.getCurrentPage().getImageUpdated()[layer] = true;
        save();

        String strFile = getCurrentPage().getLayerImageFile(layer).getPath();
        SketchletContextUtils.editImages("\"" + strFile + "\"", this, layer);
    }

    public void newRegionFromImage(BufferedImage image, int x, int y) {
        ActiveRegion a = new ActiveRegion(this.getCurrentPage().getRegions());

        a.setDrawImageChanged(0, true);

        a.setDrawImage(0, image);
        a.x1 = x;
        a.y1 = y;
        a.x2 = x + image.getWidth();
        a.y2 = y + image.getHeight();

        RefreshTime.update();
        repaint();
        getCurrentPage().getRegions().getRegions().insertElementAt(a, 0);
        this.getCurrentPage().getRegions().getMouseHelper().setSelectedRegions(new Vector<ActiveRegion>());
        getCurrentPage().getRegions().getMouseHelper().addToSelection(a);
        if (this.getTabsModes() != null && getTabsModes().getTabCount() > 0) {
            this.getTabsModes().setSelectedIndex(0);
        }
        ActiveRegionsFrame.reload(a);
        editorFrame.requestFocus();
    }

    public void selectSketch(Page page) {
        if (page == null) {
            return;
        }
        int i = getPages().getPages().indexOf(page);

        if (i >= 0) {
            this.openSketchByIndex(i);
        }
    }

    public void selectSketch(String strSketch) {
        selectSketch(getPages().getSketch(strSketch));
    }

    public static boolean bInPreviewMode = false;

    public void preview(Page s) {
        ActiveRegionsFrame.closeRegionsAndActions();
        setEditorMode(SketchletEditorMode.SKETCHING);
        setMasterPage(getPages().getSketch("Master"));
        setCurrentPage(s);
        getPageDetailsPanel().load();
        getCurrentPage().activate(false);
        editorFrame.setTitle(getCurrentPage().getTitle());
        this.textArea.setText(getCurrentPage().getTextAnnotation());
        RefreshTime.update();
        repaint();
        reloadPlay();
    }

    public void refreshImage(int index) {
        getCurrentPage().initImage(index, true);
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
        if (this.getCurrentPage() == null) {
            return;
        }

        getRenderer().initMasterImage();
        ActiveRegionsFrame.showRegionsAndActions((getMode() == SketchletEditorMode.ACTIONS || getMode() == SketchletEditorMode.PREVIEW) && isExtraEditorPanelVisible());
        editorFrame.setTitle(getCurrentPage().getTitle());
        this.textArea.setText(getCurrentPage().getTextAnnotation());
        getPageDetailsPanel().load();

        revalidate();
        RefreshTime.update();
        repaint();

        if (this.getPageListPanel() != null && getCurrentPage() != null) {
            this.getPageListPanel().model.fireTableDataChanged();
            int index = getInstance().getPages().getPages().indexOf(getInstance().getCurrentPage());
            getInstance().getPageListPanel().table.getSelectionModel().setSelectionInterval(index, index);
        }

        loadLayersTab();
        enableControls();
    }

    public void refreshPlayback() {
        if (this.getCurrentPage() != null && this.getInternalPlaybackPanel() != null) {
            getInternalPlaybackPanel().getCurrentPage().deactivate(true);
            getInternalPlaybackPanel().getCurrentPage().activate(true);
            return;
        }
    }

    public void duplicate() {
        if (getCurrentPage() == null) {
            return;
        }
        saveAndWait();
        if (editorFrame != null) {
            editorFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        if (getCurrentPage() != null && getControlPanel() != null) {
            getNavigationHistory().add(getCurrentPage());
            this.getControlPanel().goBack.setEnabled(true);
        }
        if (getInstance().getTool() != null) {
            getInstance().getTool().deactivate();
        }
        BufferedImage tempImages[] = new BufferedImage[getCurrentPage().getImages().length];
        for (int i = 0; i < getCurrentPage().getImages().length; i++) {
            tempImages[i] = getCurrentPage().getImages()[i];
            this.getCurrentPage().getImageUpdated()[i] = getCurrentPage().getLayerImageFile(i).exists();
        }
        Page s = getPages().addNewSketch();
        s.setRegions(new ActiveRegions(getCurrentPage().getRegions(), s));
        // s.title = "Duplicate: " + currentSketch.title;
        s.setTextAnnotation(getCurrentPage().getTextAnnotation());

        for (EventMacro eventMacro : getCurrentPage().getKeyboardProcessor().getKeyboardEventMacros()) {
            s.getKeyboardProcessor().getKeyboardEventMacros().add(new KeyboardEventMacro(eventMacro));
        }
        for (EventMacro eventMacro : getCurrentPage().getVariableUpdateEventMacros()) {
            s.getVariableUpdateEventMacros().add(new VariableUpdateEventMacro(eventMacro));
        }

        s.setOnEntryMacro(new Macro(getCurrentPage().getOnEntryMacro()));
        s.setOnExitMacro(new Macro(getCurrentPage().getOnExitMacro()));
        for (int i = 0; i < s.getProperties().length; i++) {
            s.getProperties()[i][1] = getCurrentPage().getProperties()[i][1];
        }
        for (int i = 0; i < s.getPropertiesAnimation().length; i++) {
            for (int j = 0; j < s.getPropertiesAnimation()[i].length; j++) {
                s.getPropertiesAnimation()[i][j] = getCurrentPage().getPropertiesAnimation()[i][j];
            }
        }
        for (int i = 0; i < s.getSpreadsheetData().length; i++) {
            for (int j = 0; j < s.getSpreadsheetData()[i].length; j++) {
                s.updateSpreadsheetCell(i, j, getCurrentPage().getSpreadsheetCellValue(i, j));
            }
        }
        s.setStrSpreadsheetColumnWidths(getCurrentPage().getStrSpreadsheetColumnWidths());
        for (int i = 0; i < s.getLayerActive().length; i++) {
            s.getLayerActive()[i] = getCurrentPage().getLayerActive()[i];
        }

        getCurrentPage().deactivate(false);

        setCurrentPage(s);

        getCurrentPage().activate(false);
        for (int i = 0; i < getCurrentPage().getImages().length; i++) {
            getCurrentPage().getImages()[i] = tempImages[i];
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
        if (getTool() != null) {
            getTool().deactivate();
        }
        saveAndWait();
        Page s = getPages().addNewSketch();

        String name = "";
        name = (String) JOptionPane.showInputDialog(SketchletEditor.editorFrame,
                Language.translate("Page name:"), Language.translate("New Page"),
                JOptionPane.QUESTION_MESSAGE, null, null, s.getTitle());
        while (name != null && getPages().sketchNameExists(name, s)) {
            JOptionPane.showMessageDialog(SketchletEditor.editorFrame, Language.translate("Page with name \"" + name + "\" already exists.\nEach page has to have a unique name."), Language.translate("Name Conflict"), JOptionPane.WARNING_MESSAGE);
            name = (String) JOptionPane.showInputDialog(SketchletEditor.editorFrame,
                    Language.translate("Page name:"), Language.translate("New Page"),
                    JOptionPane.QUESTION_MESSAGE, null, null, s.getTitle());
        }

        if (name == null) {
            getPages().getPages().remove(s);
            s.dispose();
            return null;
        } else {
            s.setTitle(name);
            editorFrame.setTitle(s.getTitle());
            this.textArea.setText(s.getTextAnnotation());

            if (getCurrentPage() != null) {
                getCurrentPage().deactivate(false);
            }
            setMasterPage(getPages().getSketch("Master"));
            setCurrentPage(s);
            this.initImages();
            if (getCurrentPage() != null) {
                getCurrentPage().activate(false);
            }

            GlobalProperties.setAndSave("last-sketch-index", "" + (getPages().getPages().size() - 1));

            saveAndWait();
            refresh();
            reloadPlay();

            return s;
        }

    }

    public void resize() {
        if (getCurrentPage() == null) {
            return;
        }
        try {
            new ResizeDialog(this.editorFrame, "Resize Page", getInstance());
        } catch (Throwable e) {
            log.error(e);
        }
    }

    public void delete() {
        delete(this.editorFrame);
    }

    public void delete(Component component) {
        if (getCurrentPage() == null) {
            return;
        }
        try {
            Object[] options = {"Delete", "Cancel"};
            int n = JOptionPane.showOptionDialog(component,
                    "You are about to delete '" + getCurrentPage().getTitle() + "'",
                    "Delete Sketch Confirmation",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (n != 0) {
                return;
            }

            int i = this.getPages().getPages().indexOf(getCurrentPage());
            getCurrentPage().delete();

            this.getPages().getPages().remove(getCurrentPage());
            //tabs.remove(i);

            setMasterPage(getPages().getSketch("Master"));
            if (this.getPages().getPages().size() == 0) {
                setCurrentPage(null);
            } else {
                int index = i + 1 > this.getPages().getPages().size() ? this.getPages().getPages().size() - 1 : i;
                setMasterPage(this.getPages().getSketch("Master"));
                setCurrentPage(this.getPages().getPages().elementAt(index));
                this.initImages();
                getCurrentPage().activate(false);
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
        if (SketchletEditor.getInstance().getTool() != null) {
            SketchletEditor.getInstance().getTool().deactivate();
        }
        SketchletEditor.getInstance().setOldScale(SketchletEditor.getInstance().getScale());
        SketchToolbar.animateZoom = false;
        SketchletEditor.getInstance().getSketchToolbar().zoomBox.setSelectedItem("100%");
        SketchToolbar.animateZoom = true;
        SketchletEditor.getInstance().setScale(scale);

        getInstance().clearImageMemory();
        if (ImageCache.getImages() == null) {
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

        PlaybackFrame.play(this.getPages(), getCurrentPage());
        if (PlaybackFrame.playbackFrame.length > 0 && PlaybackFrame.playbackFrame[0] != null) {
            PlaybackFrame.playbackFrame[0].playbackPanel.requestFocus();
        }
    }

    public JScrollPane playInternal() {
        if (SketchletEditor.getInstance().getTool() != null) {
            SketchletEditor.getInstance().getTool().deactivate();
        }
        SketchletEditor.getInstance().setOldScale(SketchletEditor.getInstance().getScale());
        SketchToolbar.animateZoom = false;
        SketchletEditor.getInstance().getSketchToolbar().zoomBox.setSelectedItem("100%");
        SketchToolbar.animateZoom = true;
        SketchletEditor.getInstance().setScale(scale);

        updateTables();
        getExtraEditorPanel().save();
        saveAndWait();

        if (InteractionSpaceFrame.frame != null) {
            InteractionSpaceFrame.frame.save();
            InteractionSpaceFrame.frame.setState(Frame.ICONIFIED);
        }

        setInternalPlaybackPanel(new PlaybackPanel(null, getPages(), null));
        setInternalPlaybackPanelScrollPane(new JScrollPane(getInternalPlaybackPanel()));
        getInternalPlaybackPanelScrollPane().setFocusable(false);
        if (getCurrentPage().getImages() != null && getCurrentPage().getImages()[0] != null) {
            getInternalPlaybackPanel().setPreferredSize(new Dimension(getCurrentPage().getImages()[0].getWidth(), getCurrentPage().getImages()[0].getHeight()));
        } else {
            int w = (int) InteractionSpace.getSketchWidth();
            int h = (int) InteractionSpace.getSketchHeight();
            getInternalPlaybackPanel().setPreferredSize(new Dimension(w, h));
        }

        this.getCurrentPage().deactivate(false);
        if (getMasterPage() != null) {
            getMasterPage().deactivate(false);
        }
        getInternalPlaybackPanel().showSketch(this.getCurrentPage());

        return getInternalPlaybackPanelScrollPane();
    }
    // JLabel label = new JLabel(Workspace.createImageIcon("resources/sketching.png"));

    public void zoomToWindow() {
        try {
            final int w = SketchletEditor.getInstance().scrollPane.getViewport().getWidth();
            final int h = SketchletEditor.getInstance().scrollPane.getViewport().getHeight();
            int w2 = 0;
            int h2 = 0;
            if (getCurrentPage().getImages()[0] != null) {
                w2 = getCurrentPage().getImages()[0].getWidth();
                h2 = getCurrentPage().getImages()[0].getHeight();
            } else {
                w2 = (int) InteractionSpace.getSketchWidth();
                h2 = (int) InteractionSpace.getSketchHeight();
            }
            double _zoom = Math.min((double) w / w2, (double) h / h2) - 0.02;
            SketchletEditor.getInstance().getSketchToolbar().zoomBox.setSelectedItem("" + (int) (_zoom * 100) + "%");
        } catch (Exception e) {
        }
    }

    public void setEditorMode(SketchletEditorMode m) {
        setEditorMode(m, layer);
    }

    public void setMode(SketchletEditorMode m) {
        mode = m;
    }

    public void setEditorMode(SketchletEditorMode m, int _layer) {
        if (getInstance().getTool() != null) {
            getInstance().getTool().deactivate();
        }
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        boolean shouldActivate = false;
        if (getInstance().getCurrentPage() != null && getInstance().getInternalPlaybackPanel() != null) {
            shouldActivate = true;
            getInstance().getCurrentPage().deactivate(true);
            getInstance().getDrawingPanel().remove(getInstance().getInternalPlaybackPanelScrollPane());
            VariablesBlackboard.getInstance().removeVariablesUpdateListener(getInstance().getInternalPlaybackPanel());
            if (getInstance().getInternalPlaybackPanel() != null) {
                getInstance().getInternalPlaybackPanel().dispose();
                getInstance().setMasterPage(getInstance().getPages().getSketch("Master"));
                getInstance().setInternalPlaybackPanel(null);
            }
            setMode(SketchletEditorMode.UNDEFINED);
        }

        if (getMode() != m) {
            ActivityLog.log("setMode", "" + m);
        }

        if (m == SketchletEditorMode.SKETCHING) {
            if (getMode() != SketchletEditorMode.SKETCHING || _layer != layer) {
                if (shouldActivate) {
                    getInstance().getCurrentPage().activate(false);
                }

                setMode(SketchletEditorMode.SKETCHING);
                SketchletEditor.getStatusBar().setText("Sketching mode");
                ActiveRegionsFrame.showRegionsAndActions(false);
                setTool(getPenTool(), null);
                setCursor();
                enableControls();
                createGraphics();

                getInstance().getDrawingPanel().add(getInstance().getMainSketchPanel());
                SketchletEditor.getInstance().getFormulaToolbar().refresh();
                getInstance().getMainSketchPanel().remove(getActiveRegionToolbar());
                getInstance().getMainSketchPanel().add(getColorToolbar(), BorderLayout.SOUTH);
                getInstance().getDrawingPanel().revalidate();
                editorFrame.repaint();
            }
        } else if (m == SketchletEditorMode.ACTIONS) {
            if (getMode() != SketchletEditorMode.ACTIONS || _layer != layer) {
                if (shouldActivate) {
                    getInstance().getCurrentPage().activate(false);
                }
                setMode(SketchletEditorMode.ACTIONS);
                getCurrentPage().getRegions().getVariablesHelper().refreshFromVariables();

                if (Profiles.isActive("active_regions_layer")) {
                    setTool(getActiveRegionSelectTool(), null);
                } else {
                    setTool(getPenTool(), null);
                }
                requestFocus();
                setCursor();
                enableControls();
                getInstance().getDrawingPanel().add(getInstance().getMainSketchPanel());
                SketchletEditor.getInstance().getFormulaToolbar().refresh();
                getInstance().getMainSketchPanel().remove(getColorToolbar());
                getInstance().getMainSketchPanel().add(getActiveRegionToolbar(), BorderLayout.SOUTH);
                getInstance().getDrawingPanel().revalidate();
                editorFrame.repaint();
            }
        } else if (m == SketchletEditorMode.PREVIEW) {
            saveAndWait();
            getInstance().getDrawingPanel().remove(getInstance().getMainSketchPanel());
            getInstance().clearImageMemory();
            JScrollPane sp = getInstance().playInternal();
            getInstance().getDrawingPanel().add(sp);
            getInstance().getDrawingPanel().revalidate();
            editorFrame.repaint();
        }

        getModeToolbar().addComponents(m);
        getInstance().revalidate();
        RefreshTime.update();

        getInstance().repaint();
        getInstance().enableControls();
        editorFrame.setVisible(true);
        requestFocus();
        this.setCursor();
    }

    private static Cursor currentCursor = null;

    public void setCursor() {
        if (getTool() != null && getCurrentPage().isLayerActive(layer)) {
            currentCursor = getTool().getCursor();
            setCursor(currentCursor);
        } else {
            setCursor(null);
        }
    }

    public void resetScale() {
        if (SketchletEditor.getInstance().getOldScale() != 0.0) {
            SketchletEditor.getInstance().getSketchToolbar().zoomBox.setSelectedItem((int) (SketchletEditor.getInstance().getOldScale() * 100) + "%");
        }
    }

    public void history() {
    }

    public void setAsMaster() {
        setTool(getActiveRegionSelectTool(), null);

        int i = 0;
        for (Page s : SketchletEditor.getInstance().getPages().getPages()) {
            if (s.getTitle().equalsIgnoreCase("master")) {
                s.setTitle(s.getTitle() + " (old)");
                s.save(false);
            }
            i++;
        }
        this.getCurrentPage().setTitle("master");
        editorFrame.setTitle("master");
        this.save();
    }

    public void printPage() {
        final BufferedImagePrinter printer = new BufferedImagePrinter();
        printer.setActionBeforePrinting(new Runnable() {

            public void run() {
                BufferedImage img = Workspace.createCompatibleImage(getCurrentPage().getImages()[0].getWidth(), getCurrentPage().getImages()[0].getHeight());
                Graphics2D g2i = img.createGraphics();
                getRenderer().draw(g2i, SketchletEditor.getInstance().getTabsModes().getSelectedIndex() != 0, true, false);
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
        File file = selectImageFile(this.getCurrentPage().getTitle());
        if (file != null) {
            try {
                if (!file.getName().contains(".")) {
                    file = new File(file.getPath() + ".png");
                }
                int n = file.getName().indexOf(".");
                String extension = file.getName().substring(n + 1);
                int w = getCurrentPage().getImages()[0].getWidth();
                int h = getCurrentPage().getImages()[0].getHeight();
                BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2i = img.createGraphics();
                g2i.setColor(Color.WHITE);
                g2i.fillRect(0, 0, w, h);
                getRenderer().draw(g2i, SketchletEditor.getInstance().getTabsModes().getSelectedIndex() != 0, true, false);
                g2i.dispose();
                ImageIO.write(img, extension, file);
                JOptionPane.showMessageDialog(this, "The image has been saved.");
            } catch (Throwable e) {
                log.error(e);
            }
        }
    }

    public void selectAll() {
        if (this.getTabsModes().getSelectedIndex() == 0) {
            if (getTool() instanceof ActiveRegionSelectTool || getTool() instanceof ActiveRegionTool) {
                selectAllRegions();
            } else {
                setTool(this.getSelectTool(), null);
                this.getSelectTool().setX1(0);
                this.getSelectTool().setY1(0);
                this.getSelectTool().setX2(this.getWidth());
                this.getSelectTool().setY2(this.getHeight());
            }
        }

        RefreshTime.update();
        repaint();
    }

    public void importSketches() {
        setTool(getActiveRegionSelectTool(), null);
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
                data[i][1] = importedPages.elementAt(i).getTitle();
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
                            getPages().getPages().add(s);
                            JPanel pane = new JPanel();
                            pane.setPreferredSize(new Dimension(10, 10));
                            //tabs.add(pane, s.title);

                            FileUtils.restore(SketchletContextUtils.getCurrentProjectSkecthletsDir(), strDir, null, false, s.getId().substring(1));
                            for (ActiveRegion a : s.getRegions().getRegions()) {
                                a.getDrawImagePath(0);
                                FileUtils.copyFile(new File(strDir + a.getDrawImagePath(0)), new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + a.getDrawImagePath(0)));

                                for (int aai = 0; aai < a.additionalImageFile.size(); aai++) {
                                    a.getDrawImagePath(aai + 1);
                                    FileUtils.copyFile(new File(strDir + a.getDrawImagePath(aai + 1)), new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + a.getDrawImagePath(aai + 1)));
                                }
                            }
                        }
                    }
                    SketchletEditor.getInstance().refresh();
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
        if (getSketchToolbar() != null) {
            getSketchToolbar().enableControls();
        }
        if (getModeToolbar() != null) {
            getModeToolbar().enableControls();
        }
    }

    public void moveLeft() {
        setTool(getActiveRegionSelectTool(), null);
        int index = getPages().getPages().indexOf(getCurrentPage());

        if (index > 0) {
            //String title1 = tabs.getTitleAt(index);
            //String title2 = tabs.getTitleAt(index - 1);
            //tabs.setTitleAt(index, title2);
            //tabs.setTitleAt(index - 1, title1);

            Page s = getPages().getPages().remove(index);
            getPages().getPages().insertElementAt(s, index - 1);
            SketchletEditor.getInstance().openSketchByIndex(index - 1);
            getInstance().refresh();
            //tabs.setSelectedIndex(index - 1);
        }
    }

    public void moveRight() {
        setTool(getActiveRegionSelectTool(), null);
        int index = getPages().getPages().indexOf(getCurrentPage());

        if (index >= 0 && index < getPages().getPages().size() - 1) {
            Page s = getPages().getPages().remove(index);
            getPages().getPages().insertElementAt(s, index + 1);

            SketchletEditor.getInstance().openSketchByIndex(index + 1);
            getInstance().refresh();
        }
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public Dimension getPreferredSize() {
        double _scale = getScale(); // > 1.0 ? scale : 1.0;
        int w = 0;
        int h = 0;
        if (getCurrentPage().getImages() != null && getCurrentPage().getImages()[0] != null) {
            w = getCurrentPage().getImages()[0].getWidth();
            h = getCurrentPage().getImages()[0].getHeight();
        } else if (getCurrentPage().getPageWidth() > 0 && getCurrentPage().getPageHeight() > 0) {
            w = getCurrentPage().getPageWidth();
            h = getCurrentPage().getPageHeight();
        } else {
            w = (int) InteractionSpace.getSketchWidth();
            h = (int) InteractionSpace.getSketchHeight();
        }

        getCurrentPage().setPageWidth(w);
        getCurrentPage().setPageHeight(h);

        w += getMarginX() * 2;
        h += getMarginY() * 2;

        return new Dimension((int) (w * _scale), (int) (h * _scale));
    }

    public static JFrame editorFrame;
    public JScrollPane scrollPane;
    public boolean bDisposed = false;

    public boolean isActive() {
        return !bDisposed;
    }

    public boolean isLayerActive(int l) {
        return SketchletEditor.getInstance().getCurrentPage().getLayerActive()[l];
    }

    boolean closing = false;

    public static boolean close() {
        return close(true);
    }

    public static boolean close(boolean bMsg) {
        if (getInstance() == null || getInstance().closing) {
            return true;
        }
        getInstance().closing = true;
        if (getInstance().mediaWatcher != null) {
            getInstance().mediaWatcher.stop();
        }
        if (getUndockStateFrame() != null) {
            getUndockStateFrame().setVisible(false);
            setbStatePanelUndocked(false);
            getInstance().setPagePanelShown(false);
        }

        if (undockFrame != null) {
            undockFrame.setVisible(false);
            bUndocked = false;
            getInstance().setExtraEditorPanelVisible(false);
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
            if (getInstance() != null) {
                ActivityLog.save();
                HelpUtils.setHelpInterface(null);
                getInstance().bDisposed = true;
                if (nOption == 0) {
                    getInstance().saveAndWait();
                    Timers.getGlobalTimers().save();
                    Macros.globalMacros.save();
                    Workspace.getMainPanel().saveConfiguration();
                }

                ImageCache.clear();
                deleteUnusedFiles();

                if (getInstance().getCurrentPage() != null) {
                    getInstance().getCurrentPage().deactivate(false);
                }
                getInstance().hideExtraEditorPanel();
                getInstance().getPageDetailsPanel().hideProperties();
                VariableSpacesFrame.hideFrame();

                int w = getInstance().editorFrame.getWidth();
                int h = getInstance().editorFrame.getHeight();
                GlobalProperties.set("editor-window-size", w + "," + h);
                GlobalProperties.save();
//                FileUtils.saveFileText(SketchletContextUtils.getDefaultProjectsRootLocation() + "editor_window_size.txt", w + "," + h);
                getInstance().editorFrame.remove(getInstance());
                if (getInstance().getInternalPlaybackPanel() != null) {
                    VariablesBlackboard.getInstance().removeVariablesUpdateListener(getInstance().getInternalPlaybackPanel());
                    if (getInstance().getInternalPlaybackPanel().getCurrentPage() != null) {
                        getInstance().getInternalPlaybackPanel().getCurrentPage().deactivate(true);
                    }
                    getInstance().getInternalPlaybackPanel().dispose();
                    getInstance().setInternalPlaybackPanel(null);
                }
            }
            if (getInstance().getUndoRegionActions() != null) {
                getInstance().getUndoRegionActions().clear();
            }
            SystemVariablesDialog.stop();
            ActiveRegionsFrame.closeRegionsAndActions();
            PlaybackFrame.close();
            Workspace.setReferenceFrame(null);
            CurvesFrame.hideFrame();
            StateDiagram.hideDiagram();
            InteractionSpaceFrame.closeFrame();
            EyeFrame.hideFrame();

            PlaybackPanel.setCurrentPage(null);
            PlaybackPanel.setMasterPage(null);
            PlaybackPanel.setPages(null);
            PlaybackPanel.getHistory().removeAllElements();

            if (getInstance() != null) {
                getInstance().setFileDrop(null);
                getInstance().removeKeyListener(getInstance());
                getInstance().getRenderer().dispose();
                getInstance().setRenderer(null);
                getInstance().getNavigationHistory().removeAllElements();
            }
            if (getInstance() != null && getInstance().getCurrentPage().getImages() != null) {
                for (int i = 0; i < getInstance().getCurrentPage().getImages().length; i++) {
                    BufferedImage img = getInstance().getCurrentPage().getImages()[i];
                    if (img != null) {
                        img.flush();
                        getInstance().getCurrentPage().getImages()[i] = null;
                    }
                }
            }
            getInstance().getCurrentPage().setImages(null);
            getInstance().getPenTool().dispose();
            getInstance().getBucketTool().dispose();
            getInstance().getTransparentColorTool().dispose();
            getInstance().getMagicWandTool().dispose();
            getInstance().getLineTool().dispose();
            getInstance().getRectTool().dispose();
            getInstance().getOvalTool().dispose();
            getInstance().getEraserTool().dispose();
            getInstance().getSelectTool().dispose();
            getInstance().getFreeFormSelectTool().dispose();
            getInstance().getActiveRegionTool().dispose();
            getInstance().getActiveRegionSelectTool().dispose();
            getInstance().getPostNoteTool().dispose();
            getInstance().getTool().dispose();

            if (getInstance().g2SketchletEditor != null) {
                getInstance().g2SketchletEditor.dispose();
                getInstance().g2SketchletEditor = null;
            }

            VariablesBlackboard.getInstance().removeVariablesUpdateListener(getInstance());
        } catch (Throwable e) {
            log.error(e);
        }
        if (SketchletEditor.copiedActions != null) {
            SketchletEditor.copiedActions.removeAllElements();
        }

        if (SketchletEditor.getPages() != null) {
            SketchletEditor.getPages().dispose();
            SketchletEditor.setPages(null);
        }

        if (nOption == 1) {
            Workspace.getMainPanel().restoreOriginal();
        } else {
            FileUtils.deleteDir(new File(SketchletContextUtils.getCurrentProjectOriginalDir()));
        }

        if (getInstance() != null) {
            editorFrame.setVisible(false);
            editorFrame.dispose();
        }
        editorFrame.dispose();
        setEditorPanel(null);
        editorFrame = null;
        Workspace.getMainFrame().toFront();
        final int _nOption = nOption;
        if (!Workspace.bCloseOnPlaybackEnd) {
            if (SketchletEditor.getPages() != null) {
                SketchletEditor.getPages().dispose();
            }
            if (_nOption == 1 || _nOption == -1) {
                try {
                    Workspace.getProcessRunner().getIoServicesHandler().loadProcesses(new File(Workspace.getFilePath()).toURL(), false);
                } catch (Exception e) {
                    log.error(e);
                }
            }
            SketchletEditor.setPages(new Pages());
            Workspace.getMainPanel().refreshSketches();
        }

        return true;
    }

    private static void deleteUnusedFiles() {
        // We first collect the list of all used image files
        List<File> usedImages = new ArrayList<File>();
        for (Page page : SketchletEditor.getPages().getPages()) {
            for (File file : page.getImageFiles()) {
                usedImages.add(file);
                usedImages.add(new File(file.getParent(), file.getName().replace(".png", "_thumbnail.png")));
            }
            for (ActiveRegion region : page.getRegions().getRegions()) {
                usedImages.add(region.getImageFile(0));
                for (File file : region.getImageFiles()) {
                    usedImages.add(file);
                }
            }
        }

        // here we look at all image files, and sof delete the ones that are not in the list of used images
        File files[] = new File(SketchletContextUtils.getCurrentProjectSkecthletsDir()).listFiles();
        for (File file : files) {
            String fileName = file.getName();
            if (fileName.toLowerCase().endsWith(".png")) {
                if (!usedImages.contains(file)) {
                    log.info("Soft deleting " + file.getPath());
                    file.renameTo(new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "deleted", file.getName()));
                }
            }
        }

        // delete all soft deleted files older than 10 days
        File softDeletedFiles[] = new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "deleted").listFiles();
        for (File file : softDeletedFiles) {
            if (System.currentTimeMillis() - file.lastModified() > 10 * 24 * 60 * 60 * 1000L) {
                log.info("Hard deleting " + file.getPath());
                file.delete();
            }
        }
    }

    public void addMemoryPanel() {
        if (memoryPanel != null) {
            getInstance().getStatusPanel().remove(memoryPanel);
            memoryPanel.stop();
            memoryPanel = null;
            GlobalProperties.set("memory-monitor", "false");
        } else {
            memoryPanel = new MemoryPanel();
            getInstance().getStatusPanel().add(memoryPanel, BorderLayout.EAST);
            GlobalProperties.set("memory-monitor", "true");
        }
        getInstance().getStatusPanel().revalidate();
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
                        SketchletEditor.getInstance().getExtraEditorPanel().derivedVariablesExtraPanel.tabs.setSelectedIndex(index);
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
        if (!getInstance().isExtraEditorPanelVisible() && !bUndocked) {
            getInstance().setExtraEditorPanelVisible(true);
            int h = SketchletEditor.getInstance().getPanelDrawingPanel().getHeight();
            getInstance().getEditorPane().remove(SketchletEditor.getInstance().getPanelDrawingPanel());
            // editorPanel.editorPane.add(editorPanel.extraEditorPanel, BorderLayout.SOUTH);
            getInstance().setSplitPane(new JSplitPane(JSplitPane.VERTICAL_SPLIT, SketchletEditor.getInstance().getPanelDrawingPanel(), getInstance().getExtraEditorPanel()));
            getInstance().getSplitPane().setResizeWeight(1);
            getInstance().getSplitPane().setDividerLocation(h - 195);
            getInstance().getEditorPane().add(getInstance().getSplitPane());
            getInstance().getEditorPane().revalidate();
        }

        if (nTab >= 0 && nTab < getInstance().getExtraEditorPanel().tabs.getTabCount()) {
            getInstance().getExtraEditorPanel().tabs.setSelectedIndex(nTab);
        }
    }

    public static void showExtraEditorPanelBig(int nTab) {
        if (!getInstance().isExtraEditorPanelVisible() && !bUndocked) {
            getInstance().setExtraEditorPanelVisible(true);
            getInstance().getExtraEditorPanel().setBiggerSize();
            getInstance().getEditorPane().add(getInstance().getExtraEditorPanel(), BorderLayout.SOUTH);
            getInstance().getEditorPane().revalidate();
        }

        getInstance().getExtraEditorPanel().tabs.setSelectedIndex(nTab);
    }

    public static boolean bUndocked = false;
    public static JFrame undockFrame = null;

    public static void undockExtraEditorPanel() {
        hideExtraEditorPanel();
        undockFrame = new JFrame();
        undockFrame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                bUndocked = false;
                getInstance().setExtraEditorPanelVisible(false);
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
        undockFrame.add(getInstance().getExtraEditorPanel());
        undockFrame.pack();
        undockFrame.setIconImage(Workspace.createImageIcon("resources/editor.gif", "").getImage());
        undockFrame.setVisible(true);
        bUndocked = true;
    }

    public static void dockExtraEditorPanel() {
        if (undockFrame != null) {
            undockFrame.setVisible(false);
            getInstance().setExtraEditorPanelVisible(false);
            bUndocked = false;
            showExtraEditorPanel(-1);
        }
    }

    public static void hideExtraEditorPanel() {
        if (getInstance().isExtraEditorPanelVisible()) {
            int index = SketchletEditor.getInstance().getTabsRight().getSelectedIndex();

            getInstance().getExtraEditorPanel().onHide();
            getInstance().setExtraEditorPanelVisible(false);
            getInstance().getEditorPane().remove(SketchletEditor.getInstance().getSplitPane());
            getInstance().getEditorPane().add(SketchletEditor.getInstance().getPanelDrawingPanel());
            // editorPanel.editorPane.remove(editorPanel.extraEditorPanel);
            getInstance().getEditorPane().revalidate();
            SketchletEditor.getInstance().getTimersTablePanel().model.fireTableDataChanged();
            SketchletEditor.getInstance().getExtraEditorPanel().timersExtraPanel.load();
            SketchletEditor.getInstance().getMacrosTablePanel().model.fireTableDataChanged();
            SketchletEditor.getInstance().getExtraEditorPanel().macrosExtraPanel.load();

            SketchletEditor.getInstance().getTabsRight().setSelectedIndex(index);
        }
    }

    public void keyTyped(KeyEvent e) {
        if (getTool() != null) {
            getTool().keyTyped(e);
        }

    }

    public void selectAllRegions() {
        getCurrentPage().getRegions().getMouseHelper().setSelectedRegions(new Vector<ActiveRegion>());

        for (ActiveRegion a : this.getCurrentPage().getRegions().getRegions()) {
            getCurrentPage().getRegions().getMouseHelper().addToSelection(a);
        }
        RefreshTime.update();

        repaint();
    }

    public void deleteSelectedRegion() {
        if (skipKey) {
            skipKey = false;
            return;
        }
        if (getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null) {
            this.getUndoRegionActions().add(new RegionsDeletedUndoAction(getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()));
            checkUndo();
            save();

            for (ActiveRegion region : getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                region.deactivate(false);
                getCurrentPage().getRegions().getRegions().remove(region);
            }

            getCurrentPage().getRegions().getMouseHelper().setSelectedRegions(null);
            ActiveRegionsFrame.reload();
            getCurrentPage().getRegions().getMouseHelper().setSelectedRegions(null);
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
        getCurrentPage().getRegions().getRegions().remove(region);
        getCurrentPage().getRegions().getMouseHelper().setSelectedRegions(null);
        ActiveRegionsFrame.reload();
        getCurrentPage().getRegions().getMouseHelper().setSelectedRegions(null);
        RefreshTime.update();
        repaint();

    }

    public void groupSelectedRegion() {
        if (getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null) {
            String strGroupId = "" + System.currentTimeMillis();

            boolean bGroup = false;
            for (ActiveRegion as : getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                if (as.regionGrouping.equals("") || !as.regionGrouping.equals(getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().firstElement().regionGrouping)) {
                    bGroup = true;
                    break;
                }
            }

            for (ActiveRegion as : getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
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
            setInCtrlMode(true);
        } else {
            setInCtrlMode(false);
        }

        if ((modifiers & KeyEvent.SHIFT_MASK) != 0) {
            setInShiftMode(true);
        } else {
            setInShiftMode(false);
        }

        boolean useShortcut = getMode() == SketchletEditorMode.SKETCHING || getMode() == SketchletEditorMode.ACTIONS && (getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() == null || getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() == 0);
        if ((useShortcut) && (modifiers & KeyEvent.ALT_MASK) == 0 && (modifiers & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) == 0) {
            if (key == KeyEvent.VK_P) {
                SketchletEditor.getInstance().getModeToolbar().btnSketching.doClick();
            } else if (key == KeyEvent.VK_L) {
                SketchletEditor.getInstance().getModeToolbar().btnLine.doClick();
            } else if (key == KeyEvent.VK_A) {
                SketchletEditor.getInstance().getModeToolbar().activeRegions.doClick();
            } else if (key == KeyEvent.VK_C) {
                SketchletEditor.getInstance().getModeToolbar().connector.doClick();
            } else if (key == KeyEvent.VK_ESCAPE) {
                SketchletEditor.getInstance().getModeToolbar().select.doClick();
            } else if (key == KeyEvent.VK_R) {
                SketchletEditor.getInstance().getModeToolbar().btnRect.doClick();
            } else if (key == KeyEvent.VK_O) {
                SketchletEditor.getInstance().getModeToolbar().btnOval.doClick();
            } else if (key == KeyEvent.VK_S) {
                SketchletEditor.getInstance().getModeToolbar().btnSelect.doClick();
            } else if (key == KeyEvent.VK_F) {
                SketchletEditor.getInstance().getModeToolbar().btnFreeFormSelect.doClick();
            } else if (key == KeyEvent.VK_P) {
                SketchletEditor.getInstance().getModeToolbar().btnColorPicker.doClick();
            } else if (key == KeyEvent.VK_M) {
                SketchletEditor.getInstance().getModeToolbar().btnMagicWand.doClick();
            } else if (key == KeyEvent.VK_B) {
                SketchletEditor.getInstance().getModeToolbar().btnBucket.doClick();
            } else if (key == KeyEvent.VK_E) {
                SketchletEditor.getInstance().getModeToolbar().btnEraser.doClick();
            } else if (key == KeyEvent.VK_PLUS || key == KeyEvent.VK_EQUALS || key == 107) {
                int n = SketchletEditor.getInstance().getColorToolbar().slider.getValue();
                SketchletEditor.getInstance().getColorToolbar().slider.setValue(n + 1);
            } else if (key == KeyEvent.VK_MINUS || key == 109) {
                int n = SketchletEditor.getInstance().getColorToolbar().slider.getValue();
                SketchletEditor.getInstance().getColorToolbar().slider.setValue(n - 1);
            }
        } else if (getMode() == SketchletEditorMode.ACTIONS) {
            if (key == KeyEvent.VK_ESCAPE) {
                if (getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null) {
                    ActiveRegion as = getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                    if (as.inTrajectoryMode && as.trajectoryType == 2) {
                        as.getMouseController().processTrajectory();
                        as.inTrajectoryMode = false;
                    } else if (as.inTrajectoryMode2 && as.trajectoryType == 2) {
                        as.getMouseController().processTrajectory2();
                        as.inTrajectoryMode2 = false;
                    }
                }
            } else if (key == KeyEvent.VK_F2) {
                if (getMode() == SketchletEditorMode.SKETCHING) {
                    setEditorMode(SketchletEditorMode.ACTIONS);
                } else {
                    setEditorMode(SketchletEditorMode.SKETCHING);
                }

            } else if (key == KeyEvent.VK_F3) {
                setEditorMode(SketchletEditorMode.SKETCHING);
                if (getModeToolbar().layerList.getSelectedIndex() == 0) {
                    getModeToolbar().layerList.setSelectedIndex(1);
                } else {
                    getModeToolbar().layerList.setSelectedIndex(0);
                }
            } else if ((modifiers & KeyEvent.ALT_MASK) != 0) {
            } else if ((modifiers & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
                switch (key) {
                    case KeyEvent.VK_B:
                        if (getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null) {
                            for (ActiveRegion region : getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
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
                        if (getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null) {
                            for (ActiveRegion region : getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
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
                     * PageDetailsPanel.showStateProperties(currentSketch);
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
            } else if (getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null) {
                if (getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null) {
                    for (ActiveRegion region : getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
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
        if (getTool() != null) {
            getTool().keyPressed(e);
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

        if (getMode() == SketchletEditorMode.ACTIONS && copiedActions != null) {
            this.getEditorClipboardController().pasteSpecial();
        } else {
            this.getEditorClipboardController().fromClipboard();
        }

    }

    public void defineClip() {
        if (getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() == null || getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() == 0) {
            return;
        }

        try {
            ActiveRegion _a = this.getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().firstElement();
            ActiveRegion a = new ActiveRegion(_a, _a.parent, false);
            a.fitToBoxEnabled = false;
            a.horizontalAlignment = "";
            a.verticalAlignment = "";
            a.rotation = 0.0;
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
            int x = 1;
            int y = 1;
            int w = _a.x2 - _a.x1;
            int h = _a.y2 - _a.y1;
            try {
                x = (int) InteractionSpace.getSketchX(Double.parseDouble(a.processText(a.windowX)));
            } catch (Throwable e) {
            }
            try {
                y = (int) InteractionSpace.getSketchY(Double.parseDouble(a.processText(a.windowY)));
            } catch (Throwable e) {
            }
            try {
                w = (int) InteractionSpace.getSketchWidth(Double.parseDouble(a.processText(a.windowWidth)));
            } catch (Throwable e) {
            }
            try {
                h = (int) InteractionSpace.getSketchHeight(Double.parseDouble(a.processText(a.windowHeight)));
            } catch (Throwable e) {
            }
            a.resetAllProperties();

            a.activate(true);
            a.text = "";

            try {
                BufferedImage img = Workspace.createCompatibleImage(2000, 2000);
                Graphics2D g2 = img.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                a.getRenderer().draw(g2, getInstance(), SketchletEditorMode.SKETCHING, true, false, 1.0f);
                g2.dispose();
                ImageAreaSelect.createAndShowGUI(SketchletEditor.editorFrame, img, x, y, w, h, false);
                if (ImageAreaSelect.bSaved) {
                    String params[] = ImageAreaSelect.strArea.split(" ");
                    if (params.length >= 4) {
                        _a.windowX = "" + InteractionSpace.getPhysicalX(Double.parseDouble(params[0]));
                        _a.windowY = "" + InteractionSpace.getPhysicalY(Double.parseDouble(params[1]));
                        _a.windowWidth = "" + InteractionSpace.getPhysicalWidth(Double.parseDouble(params[2]));
                        _a.windowHeight = "" + InteractionSpace.getPhysicalHeight(Double.parseDouble(params[3]));
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
        if (getMode() == SketchletEditorMode.ACTIONS && this.getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null) {
            BufferedImage img = SketchletEditor.getInstance().getCurrentPage().getImages()[SketchletEditor.getInstance().layer];
            ActiveRegion a = getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
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
        return extractImage(x, y, w, h, SketchletEditor.getInstance());
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
     * currentSketch.varPrefix, currentSketch.varPostfix)); stamp(index -
     * 1); } catch (Throwable e) { stamp(0); }
     *
     * repaint(); } }
     */
    public void stamp() {
        if (getMode() == SketchletEditorMode.ACTIONS && this.getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null) {
            saveImageUndo();
            ActiveRegion region = this.getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
            Graphics2D g2 = getCurrentPage().getImages()[layer].createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.rotate(region.rotation, region.x1 + (region.x2 - region.x1) * region.center_rotation_x, region.y1 + (region.y2 - region.y1) * region.center_rotation_y);
            region.getRenderer().drawActive(g2, getInstance(), false, 1.0f);

            g2.dispose();
            RefreshTime.update();
            SketchletEditor.getInstance().repaint();

            this.getCurrentPage().getImageUpdated()[layer] = true;
        }

    }

    public void keyReleased(KeyEvent e) {
        setInShiftMode(false);
        setInCtrlMode(false);

        if (getTool() != null) {
            getTool().keyReleased(e);
        }

        RefreshTime.update();
        repaint();
    }

    public static String lastVariable = "";
    long count = 0;

    public void repaintEverything() {
        RefreshTime.update();
        this.repaint();
        if (this.getInternalPlaybackPanel() != null) {
            this.getInternalPlaybackPanel().repaint();
        }

        PlaybackFrame.repaintAllFrames();
        RefreshTime.update();
    }

    int countt = 0;

    public void variableUpdated(String name, String varValue) {
        if (this.getCurrentPage() == null || name.trim().equals("")) {
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

        if (this.getInternalPlaybackPanel() != null) {
            this.getInternalPlaybackPanel().variableUpdated(name, varValue);
            return;
        }


        lastVariable = name;

        if (this.getSpreadsheetPanel() != null) {
            this.getSpreadsheetPanel().table.repaint();
        }

        this.getCurrentPage().getRegions().getVariablesHelper().changePerformed(name, varValue, false);
        if (!getCurrentPage().getVariableUpdatePageHandler().process(name, varValue)) {
            if (getMasterPage() != null && getCurrentPage() != getMasterPage()) {
                getMasterPage().getVariableUpdatePageHandler().process(name, varValue);
            }
        }

        this.repaint();
    }

    public void saveSpreadsheetColumWidths() {
        String strWidth = "";
        if (SketchletEditor.getInstance() != null && SketchletEditor.getInstance().getSpreadsheetPanel() != null) {
            for (int ic = 0; ic < SketchletEditor.getInstance().getSpreadsheetPanel().table.getColumnCount(); ic++) {
                if (!strWidth.isEmpty()) {
                    strWidth += ",";
                }
                strWidth += SketchletEditor.getInstance().getSpreadsheetPanel().table.getColumnModel().getColumn(ic).getWidth();
            }
        }

        this.getCurrentPage().setStrSpreadsheetColumnWidths(strWidth);
    }

    public void fromFile() {
        int returnVal = ActiveRegionPanel.getFileChooser().showOpenDialog(ActiveRegionsFrame.reagionsAndActions);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            fromFile(ActiveRegionPanel.getFileChooser().getSelectedFile());
        }

    }

    public static boolean isInPlaybackMode() {
        return SketchletEditor.getInstance() != null && (SketchletEditor.getInstance().getInternalPlaybackPanel() != null || (PlaybackFrame.playbackFrame != null && PlaybackFrame.playbackFrame[0] != null));
    }

    public void fromFile(File file) {
        fromFile(0, 0, file);
    }

    public void fromFile(int x, int y, File file) {
        setTool(getActiveRegionSelectTool(), null);
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

            this.getCurrentPage().getImageUpdated()[layer] = true;
            setTool(this.getSelectTool(), null);
            getSelectTool().setClip(newImage, x, y);
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
            getCurrentPage().getImages()[layer] = ImageIO.read(file);
            RefreshTime.update();
            this.repaint();
        } catch (Throwable e) {
            log.error(e);
        }

        createGraphics();
        setTool(getActiveRegionSelectTool(), null);
    }

    public boolean skipUndo = false;

    public void saveRegionUndo() {
        if (!undoing && !skipUndo && getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            final UndoAction ua = new RegionsChangedUndoAction(getCurrentPage().getRegions().getMouseHelper().getSelectedRegions());
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
            final UndoAction ua = new SketchChangedUndoAction(getCurrentPage());
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
        this.getUndoRegionActions().add(ua);
        checkUndo();
        enableControls();
    }

    public void saveRegionUndo(final UndoAction ua) {
        if (!undoing) {
            if (getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null) {
                java.awt.EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        if (getUndoRegionActions().size() > 0) {
                            UndoAction ua = (UndoAction) getUndoRegionActions().get(getUndoRegionActions().size() - 1);
                            if (ua instanceof RegionsChangedUndoAction) {
                                if (((RegionsChangedUndoAction) ua).isSame(getCurrentPage().getRegions().getMouseHelper().getSelectedRegions())) {
                                    return;
                                }
                            }
                        }

                        getUndoRegionActions().add(ua);
                        checkUndo();
                        enableControls();
                    }
                });
            }
        }
    }

    public static final int UNDO_BUFFER_SIZE = 100;

    public void checkUndo() {
        if (getUndoRegionActions() != null) {
            if (getUndoRegionActions().size() > SketchletEditor.UNDO_BUFFER_SIZE) {
                getUndoRegionActions().remove(0);
            }
        }
    }

    public void saveRegionImageUndo(BufferedImage img, ActiveRegion region, int frame) {
        getUndoRegionActions().add(new RegionImageChangedUndoAction(img, region, frame));
        checkUndo();
        enableControls();
    }

    public void saveNewRegionUndo() {
        if (getMode() == SketchletEditorMode.ACTIONS) {
            if (getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null) {
                getUndoRegionActions().add(new NewRegionUndoAction(getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().firstElement()));
                checkUndo();
                enableControls();
            }
        }
    }

    public void saveImageUndo() {
        BufferedImage img = getCurrentPage().getImages()[layer];
        if (img != null) {
            getUndoRegionActions().add(new SketchImageChangedUndoAction(this.getCurrentPage(), img, layer));
            checkUndo();
            enableControls();
        }
    }

    public void saveImageRedo() {
    }

    public void moveCurrentActionUpwards() {
        if (getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            for (ActiveRegion action : getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                int index = action.parent.getRegions().indexOf(action);
                int newIndex = index - 1;

                if (newIndex >= 0) {
                    action.parent.getRegions().remove(action);
                    action.parent.getRegions().insertElementAt(action, newIndex);
                    ActiveRegionsFrame.reload();
                    ActiveRegionsFrame.refresh(action);
                }

            }

            RefreshTime.update();

            repaint();
        }

    }

    public void moveCurrentActionToFront() {
        if (getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            for (ActiveRegion action : getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                int index = action.parent.getRegions().indexOf(action);

                if (index > 0) {
                    int newIndex = 0;

                    action.parent.getRegions().remove(action);
                    action.parent.getRegions().insertElementAt(action, newIndex);
                    ActiveRegionsFrame.reload();
                    ActiveRegionsFrame.refresh(action);
                }

            }
            RefreshTime.update();

            repaint();
        }

    }

    public void moveCurrentActionToBack() {
        if (getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            for (ActiveRegion action : getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                int index = action.parent.getRegions().indexOf(action);
                int newIndex = action.parent.getRegions().size() - 1;

                if (index >= 0 && index < newIndex) {

                    action.parent.getRegions().remove(action);
                    action.parent.getRegions().insertElementAt(action, newIndex);
                    ActiveRegionsFrame.reload();
                    ActiveRegionsFrame.refresh(action);
                }
            }
        }
    }

    public void moveCurrentActionToBackground() {
        if (getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            for (ActiveRegion action : getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                action.strX = "=0";
                action.strY = "=0";
                action.strWidth = Integer.toString(SketchletEditor.getInstance().getSketchWidth());
                action.strHeight = Integer.toString(SketchletEditor.getInstance().getSketchHeight());
            }
            this.moveCurrentActionToBack();
            SketchletEditor.getInstance().forceRepaint();
        }
    }

    public void moveCurrentActionBackwards() {
        if (getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            for (ActiveRegion action : getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                int index = action.parent.getRegions().indexOf(action);
                int newIndex = index + 1;

                if (newIndex < action.parent.getRegions().size()) {
                    action.parent.getRegions().remove(action);
                    action.parent.getRegions().insertElementAt(action, newIndex);
                    ActiveRegionsFrame.reload();
                    ActiveRegionsFrame.refresh(action);
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
        SketchletEditor.getInstance().saveRegionUndo();
        if (getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            for (ActiveRegion action : getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                String strArgs = "";
                String args[] = getShapeArgs(shape);
                if (args != null) {
                    String arg = JOptionPane.showInputDialog(SketchletEditor.editorFrame, args[0], args[1]);
                    if (arg != null) {
                        strArgs = arg;
                    }
                }
                action.shape = shape;
                action.shapeArguments = strArgs;
                /*
                 * if (action.shapeList != null) {
                 * action.shapeList.setSelectedIndex(index); }
                 */
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement());
            this.forceRepaint();
        }

    }

    public void setHorizontalAlignment(String strAlign) {
        if (getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            SketchletEditor.getInstance().saveRegionUndo();
            for (ActiveRegion action : getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                //action.horizontalAlign.setSelectedItem(strAlign);
                action.horizontalAlignment = strAlign;
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement());
            this.forceRepaint();
        }

    }

    public void setAutomaticPerspective(String strPerspective) {
        if (getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            SketchletEditor.getInstance().saveRegionUndo();
            for (ActiveRegion action : getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                action.strAutomaticPerspective = strPerspective;
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement());
            this.forceRepaint();
        }

    }

    public void setPerspectiveDepth(String strDepth) {
        if (getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            SketchletEditor.getInstance().saveRegionUndo();
            for (ActiveRegion action : getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                action.strPerspectiveDepth = strDepth;
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement());
            this.forceRepaint();
        }

    }

    public void setVerticalAlignment(String strAlign) {
        if (getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            SketchletEditor.getInstance().saveRegionUndo();
            for (ActiveRegion action : getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                // action.verticalAlign.setSelectedItem(strAlign);
                action.verticalAlignment = "";
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement());
            this.forceRepaint();
        }

    }

    public void setLineColor(String color) {
        if (getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            SketchletEditor.getInstance().saveRegionUndo();
            for (ActiveRegion action : getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                action.lineColor = color;
                ActiveRegionsExtraPanel.reload(getCurrentPage().getRegions().getMouseHelper().getLastSelectedRegion());
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement());
            this.forceRepaint();
        }

    }

    public void setWidget(String strWidget) {
        if (getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            SketchletEditor.getInstance().saveRegionUndo();
            for (ActiveRegion region : getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                region.widget = strWidget;
                region.widgetItems = WidgetPluginFactory.getDefaultItemsText(new ActiveRegionContextImpl(region, new PageContextImpl(region.parent.getPage())));
                region.widgetPropertiesString = WidgetPluginFactory.getDefaultPropertiesValue(new ActiveRegionContextImpl(region, new PageContextImpl(region.parent.getPage())));
                region.widgetProperties = null;
                region.widgetEventMacros.clear();

                ActiveRegionsExtraPanel.reload(getCurrentPage().getRegions().getMouseHelper().getLastSelectedRegion());
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement());
            this.forceRepaint();
        }

    }

    public void setTextColor(String color) {
        if (getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            SketchletEditor.getInstance().saveRegionUndo();
            for (ActiveRegion action : getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                action.fontColor = color;
                ActiveRegionsExtraPanel.reload(getCurrentPage().getRegions().getMouseHelper().getLastSelectedRegion());
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement());
            this.forceRepaint();
        }

    }

    public void setTextStyle(String style) {
        if (getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            SketchletEditor.getInstance().saveRegionUndo();
            for (ActiveRegion action : getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                action.fontStyle = style;
                ActiveRegionsExtraPanel.reload(getCurrentPage().getRegions().getMouseHelper().getLastSelectedRegion());
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement());
            this.forceRepaint();
        }

    }

    public void setFillColor(String color) {
        if (getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            SketchletEditor.getInstance().saveRegionUndo();
            for (ActiveRegion action : getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                action.strFillColor = color;
                ActiveRegionsExtraPanel.reload(getCurrentPage().getRegions().getMouseHelper().getLastSelectedRegion());
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement());
            this.forceRepaint();
        }

    }

    public void setTransparency(float t) {
        if (getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            SketchletEditor.getInstance().saveRegionUndo();
            for (ActiveRegion action : getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                action.transparency = (t == 0) ? "" : "" + t;
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement());
            this.forceRepaint();
        }

    }

    public void setSpeed(int s) {
        if (getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            SketchletEditor.getInstance().saveRegionUndo();
            for (ActiveRegion action : getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                action.strSpeed = (s == 0) ? "" : "" + s;
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement());
            this.forceRepaint();
        }

    }

    public void setRotationSpeed(int s) {
        if (getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            SketchletEditor.getInstance().saveRegionUndo();
            for (ActiveRegion action : getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                action.strRotationSpeed = (s == 0) ? "" : "" + s;
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement());
            this.forceRepaint();
        }

    }

    public void setHorizontal3DRotation(int rot) {
        if (getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            SketchletEditor.getInstance().saveRegionUndo();
            for (ActiveRegion action : getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                action.strRotation3DHorizontal = (rot == 0) ? "" : "" + rot;
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement());
            this.forceRepaint();
        }

    }

    public void setVertical3DRotation(int rot) {
        if (getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            SketchletEditor.getInstance().saveRegionUndo();
            for (ActiveRegion action : getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                action.strRotation3DVertical = (rot == 0) ? "" : "" + rot;
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement());
            this.forceRepaint();
        }

    }

    public void setLineStyle(String style) {
        if (getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            SketchletEditor.getInstance().saveRegionUndo();
            for (ActiveRegion action : getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                action.lineStyle = style;
                ActiveRegionsExtraPanel.reload(getCurrentPage().getRegions().getMouseHelper().getLastSelectedRegion());
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement());
            this.forceRepaint();
        }

    }

    public void setLineThickness(String thickness) {
        if (getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            SketchletEditor.getInstance().saveRegionUndo();
            for (ActiveRegion action : getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                action.lineThickness = thickness;
                ActiveRegionsExtraPanel.reload(getCurrentPage().getRegions().getMouseHelper().getLastSelectedRegion());
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement());
            this.forceRepaint();
        }

    }

    public void setPenLineThickness(String thickness) {
        if (getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            SketchletEditor.getInstance().saveRegionUndo();
            for (ActiveRegion action : getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                action.strPen = thickness;
                ActiveRegionsExtraPanel.reload(getCurrentPage().getRegions().getMouseHelper().getLastSelectedRegion());
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement());
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
        for (Page s : getPages().getPages()) {
            if (s.getTitle().equalsIgnoreCase(sketch)) {
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
    }

    public void openSketchByIndex(final int tabIndex) {
        if (getCurrentPage() != null && tabIndex == getPages().getPages().indexOf(getCurrentPage())) {
            return;
        }

        while (isLoading()) {
            log.info("Page " + getCurrentPage().getTitle() + " is loading.");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                log.error(e);
            }
        }

        if (getCurrentPage() != null && getControlPanel() != null) {
            getNavigationHistory().add(getCurrentPage());
            this.getControlPanel().goBack.setEnabled(true);
        }
        if (getInstance().getTool() != null) {
            getInstance().getTool().deactivate();
        }
        if (getInstance().getTabsModes().getSelectedIndex() == 2) {
            getInstance().getTabsModes().setSelectedIndex(0);
        }

        setLoading(true);
        if (!Workspace.bCloseOnPlaybackEnd) {
            if (Pages.getMessageFrame() == null) {
                if (PlaybackFrame.playbackFrame != null && PlaybackFrame.playbackFrame[0] != null) {
                    MessageFrame.showMessage(PlaybackFrame.playbackFrame[0], Language.translate("Opening pages..."), PlaybackFrame.playbackFrame[0].playbackPanel);
                } else {
                    MessageFrame.showMessage(SketchletEditor.editorFrame, Language.translate("Opening page..."), SketchletEditor.getInstance().getCentralPanel());
                }

                bCloseMsg = true;
            }
        }
        getInstance().save();

        try {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {

                    try {
                        SketchletEditor.editorFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        getInstance().flush();

                        getInstance().enableControls();
                        getInstance().getCurrentPage().getRegions().getMouseHelper().setSelectedRegions(null);

                        if (tabIndex >= 0 && tabIndex < getInstance().getPages().getPages().size()) {
                            GlobalProperties.setAndSave("last-sketch-index", "" + tabIndex);
                            if (getInstance().getCurrentPage() != null) {
                                getInstance().getCurrentPage().deactivate(false);
                            }

                            getInstance().setMasterPage(getInstance().getPages().getSketch("Master"));
                            getInstance().setCurrentPage(getInstance().getPages().getPages().elementAt(tabIndex));
                            getInstance().editorFrame.setTitle(getInstance().getCurrentPage().getTitle());
                            getInstance().getPageListPanel().setPageTitle(getInstance().getCurrentPage().getTitle());
                            getInstance().textArea.setText(getInstance().getCurrentPage().getTextAnnotation());
                            if (bCloseMsg) {
                                getInstance().refresh();
                            }

                            if (getInstance().getCurrentPage() != null) {
                                getInstance().getCurrentPage().activate(false);
                                getInstance().reloadPlay();
                            }
                        }

                        SketchletEditor.getInstance().getPerspectivePanel().repaint();
                        SketchletEditor.getInstance().scrollPane.getViewport().setViewPosition(new Point(0, 0));

                        while (bSaving) {
                            Thread.sleep(10);
                        }

                        getInstance().initImages();

                        if (Workspace.bCloseOnPlaybackEnd) {
                            Workspace.closeSplashScreen();
                        }
                        ActiveRegionsFrame.reload();
                        getFormulaToolbar().reload();

                        editorFrame.setVisible(true);
                        SketchletEditor.editorFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        ActivityLog.log("openSketch", "" + SketchletEditor.getInstance().getCurrentPage().getTitle());

                        SketchletEditor.getInstance().getFormulaToolbar().reload();
                    } catch (Throwable e) {
                        log.error(e);
                    } finally {
                        if (bCloseMsg && Pages.getMessageFrame() != null) {
                            bCloseMsg = false;
                            setLoading(false);
                            MessageFrame.closeMessage();
                            getInstance().repaint();
                        }
                        RefreshTime.update();
                        SketchletEditor.setTransparencyFactor(1.0);
                        SketchletEditor.getInstance().repaint();
                        SketchletEditor.getInstance().getPageVariablesPanel().refreshComponents();
                    }

                    setLoading(false);
                }
            });
        } catch (Throwable e2) {
            e2.printStackTrace();
        }

    }

    public BufferedImage getImage() {
        return getCurrentPage().getImages()[this.layer];
    }

    public void setImage(BufferedImage image) {
        getCurrentPage().getImages()[this.layer] = image;
        this.getCurrentPage().getImageUpdated()[this.layer] = true;
        File file = this.getCurrentPage().getLayerImageFile(this.layer);
        if (ImageCache.getImages() != null && ImageCache.getImages().get(file) != null && image != null) {
            ImageCache.getImages().put(file, image);
        }
        revalidate();

    }

    public void setImageUpdated(boolean bUpdated) {
        this.getCurrentPage().getImageUpdated()[this.layer] = bUpdated;
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
        return (this.getColorToolbar() == null || this.getColorToolbar().slider == null) ? 0 : this.getColorToolbar().slider.getValue();
    }

    public Graphics2D getImageGraphics() {
        return this.g2SketchletEditor;
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
        for (int i = 0; i < SketchletEditor.getInstance().getCurrentPage().getImages().length; i++) {
            try {
                getCurrentPage().setPageWidth(w);
                getCurrentPage().setPageHeight(h);
                if (SketchletEditor.getInstance().getCurrentPage().getImages()[i] != null) {
                    BufferedImage img = Workspace.createCompatibleImage(w, h);
                    Graphics2D g2 = img.createGraphics();
                    g2.drawImage(SketchletEditor.getInstance().getCurrentPage().getImages()[i], 0, 0, w, h, null);
                    g2.dispose();
                    SketchletEditor.getInstance().updateImage(i, img);
                }
            } catch (Throwable e) {
            }
            SketchletEditor.getInstance().createGraphics();
            SketchletEditor.getInstance().revalidate();
            RefreshTime.update();

            SketchletEditor.getInstance().repaint();
        }

    }

    public void resizeCanvas(int w, int h) {
        this.saveImageUndo();
        for (int i = 0; i < SketchletEditor.getInstance().getCurrentPage().getImages().length; i++) {
            try {
                getCurrentPage().setPageWidth(w);
                getCurrentPage().setPageHeight(h);
                if (SketchletEditor.getInstance().getCurrentPage().getImages()[i] != null) {
                    BufferedImage img = Workspace.createCompatibleImage(w, h);
                    Graphics2D g2 = img.createGraphics();
                    g2.drawImage(SketchletEditor.getInstance().getCurrentPage().getImages()[i], 0, 0, null);
                    g2.dispose();
                    SketchletEditor.getInstance().updateImage(i, img);
                }
            } catch (Throwable e) {
            }
            SketchletEditor.getInstance().createGraphics();
            SketchletEditor.getInstance().revalidate();
            RefreshTime.update();

            SketchletEditor.getInstance().repaint();
        }

    }

    public Page getPage() {
        return this.getCurrentPage();
    }

    public Page getMasterPage() {
        return this.masterPage;
    }

    public void setSelectedModesTabIndex(int tabIndex) {
        this.getTabsModes().setSelectedIndex(tabIndex);
    }

    public int getSelectedModesTabIndex() {
        return this.getTabsModes().getSelectedIndex();
    }

    public BufferedImage getImage(int layer) {
        return getCurrentPage().getImages()[layer];
    }

    public void setImage(int layer, BufferedImage img) {
        if (getCurrentPage().getImages()[layer] != null) {
            getCurrentPage().getImages()[layer].flush();
        }
        getCurrentPage().getImages()[layer] = img;
        File file = this.getCurrentPage().getLayerImageFile(layer);
        if (ImageCache.getImages() != null && ImageCache.getImages().get(file) != null) {
            ImageCache.getImages().get(file).flush();
            if (img != null) {
                ImageCache.getImages().put(file, img);
            }
        }
    }

    public void updateImage(int layer, BufferedImage img) {
        if (getCurrentPage().getImages()[layer] != null) {
            getCurrentPage().getImages()[layer].flush();
        }
        getCurrentPage().getImages()[layer] = img;
        this.getCurrentPage().getImageUpdated()[layer] = true;

        File file = this.getCurrentPage().getLayerImageFile(layer);
        if (ImageCache.getImages() != null && ImageCache.getImages().get(file) != null && img != null) {
            ImageCache.getImages().get(file).flush();
            ImageCache.getImages().put(file, img);
        }
    }

    public int getImageCount() {
        return getCurrentPage().getImages().length;
    }

    public BufferedImage getMasterImage() {
        return this.masterImage;
    }

    public void setMasterImage(BufferedImage image) {
        this.masterImage = image;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public int getLayer() {
        return this.layer;
    }

    public SketchletEditorMode getMode() {
        return this.mode;
    }

    public int getMarginX() {
        return this.marginX;
    }

    public int getMarginY() {
        return this.marginY;
    }

    public double getScaleX() {
        return this.getScale();
    }

    public double getScaleY() {
        return this.getScale();
    }

    public void parentPaintComponent(Graphics2D g2) {
        super.paintComponent(g2);
    }

    public void repaintInternalPlaybackPanel() {
        if (this.getInternalPlaybackPanel() != null) {
            this.getInternalPlaybackPanel().repaint();
        }
    }

    public float getWatering() {
        return watering;
    }

    public boolean shouldShapeFill() {
        return getOutlineType() == 1 || getOutlineType() == 2;
    }

    public boolean shouldShapeOutline() {
        return getOutlineType() == 0 || getOutlineType() == -1;
    }

    public void goToTrajectoryMode() {
        if (getTabsModes().getTabCount() == 3) {
            getTabsModes().removeTabAt(2);
        }

        getTabsModes().addTab("", Workspace.createImageIcon("resources/trajectory.png"), new JPanel());
        getTabsModes().setSelectedIndex(2);
        this.setEditorMode(SketchletEditorMode.SKETCHING);
        this.getModeToolbar().addComponents(SketchletEditorMode.TRAJECTORY);
        SketchletEditor.getInstance().setTool(SketchletEditor.getInstance().getTrajectoryPointsTool(), null);
        SketchletEditor.getInstance().createGraphics();
        SketchletEditor.getInstance().setCursor();
        SketchletEditor.getInstance().revalidate();
        RefreshTime.update();

        SketchletEditor.getInstance().repaint();
    }

    public void openByID(String strID) {
        this.getHelpViewer().showHelpByID(strID);
    }

    public Component getPanel() {
        return SketchletEditor.getInstance().scrollPane;
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
