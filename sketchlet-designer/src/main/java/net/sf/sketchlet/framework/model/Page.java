package net.sf.sketchlet.framework.model;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.common.geom.DistancePointSegment;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.ApplicationLifecycleCentre;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.animation.AnimatePropertiesThread;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.SketchletEditorMode;
import net.sf.sketchlet.designer.editor.tool.notes.NoteDialog;
import net.sf.sketchlet.framework.model.events.EventMacro;
import net.sf.sketchlet.framework.model.events.keyboard.KeyboardProcessor;
import net.sf.sketchlet.framework.model.events.mouse.MouseProcessor;
import net.sf.sketchlet.framework.model.events.variable.VariableUpdateEventMacro;
import net.sf.sketchlet.framework.model.imagecache.ImageCache;
import net.sf.sketchlet.designer.editor.ui.page.VariableUpdatePageHandler;
import net.sf.sketchlet.designer.playback.ui.RefreshScreenCaptureThread;
import net.sf.sketchlet.framework.blackboard.evaluator.Evaluator;
import net.sf.sketchlet.framework.model.events.keyboard.KeyboardEventMacro;
import net.sf.sketchlet.framework.model.events.mouse.MouseEventMacro;
import net.sf.sketchlet.framework.model.events.overlap.RegionOverlapEventMacro;
import net.sf.sketchlet.framework.model.programming.macros.Macro;
import net.sf.sketchlet.framework.model.programming.macros.MacroThread;
import net.sf.sketchlet.framework.model.programming.macros.Macros;
import net.sf.sketchlet.framework.model.programming.timers.TimerThread;
import net.sf.sketchlet.script.RunInterface;
import net.sf.sketchlet.util.RefreshTime;
import net.sf.sketchlet.util.XMLUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * @author cuypers
 */
public class Page implements PropertiesInterface {
    private static final Logger log = Logger.getLogger(Page.class);

    public static final int NUMBER_OF_LAYERS = 10;
    private String title = "New Sketch";
    private boolean[] layerActive;
    private ActiveRegions regions;
    private String id = null;
    private double stateDiagramX = 21.0;
    private double stateDiagramY = 21.0;
    private JGraph stateDiagramGraph = null;
    private DefaultGraphCell stateDiagramCell = null;
    private Macro onEntryMacro = new Macro();
    private Macro onExitMacro = new Macro();
    private MacroThread onEntryMacroThread;
    private MacroThread onExitMacroThread;
    private List<TimerThread> activeTimers = new Vector<TimerThread>();
    private List<RunInterface> activeMacros = new Vector<RunInterface>();
    private String textAnnotation = "";
    private String sourceDirectory = null;
    private String varPrefix = "";
    private String varPostfix = "";
    private Vector<NoteDialog> notes = new Vector<NoteDialog>();
    private List<PageVariable> pageVariables = new ArrayList<PageVariable>();
    private List<VariableUpdateEventMacro> variableUpdateEventMacros = new ArrayList<VariableUpdateEventMacro>();
    private static String[][] allProperties = {
            {"background color", "", Language.translate("red, blue, green, gray, yellow....")},
            {"transparency", "", Language.translate("0.0 .. 1.0")},
            {"zoom", "", Language.translate("1.0 means 100%")},
            {"zoom center x", "", Language.translate("default is 0")},
            {"zoom center y", "", Language.translate("default is 0")},
            {"background offset x", "", Language.translate("")},
            {"background offset y", "", Language.translate("")},
            {"regions offset x", "", Language.translate("")},
            {"regions offset y", "", Language.translate("")},
            {"perspective type", "", Language.translate("1 point or two point")},
            {"perspective y", "", Language.translate("horizon")},
            {"perspective x1", "", Language.translate("point 1 on the horizon")},
            {"perspective x2", "", Language.translate("point 2 on the horizon")},
            {"active layers", "", Language.translate("comma separated list 1,2,..., empty for all")},
            {"layer 1", "", Language.translate("true or false")},
            {"layer 2", "", Language.translate("true or false")},
            {"layer 3", "", Language.translate("true or false")},
            {"layer 4", "", Language.translate("true or false")},
            {"layer 5", "", Language.translate("true or false")},
            {"layer 6", "", Language.translate("true or false")},
            {"layer 7", "", Language.translate("true or false")},
            {"layer 8", "", Language.translate("true or false")},
            {"layer 9", "", Language.translate("true or false")},
            {"layer 10", "", Language.translate("true or false")},
            {"transparency layer 1", "", Language.translate("0.0 .. 1.0")},
            {"transparency layer 2", "", Language.translate("0.0 .. 1.0")},
            {"transparency layer 3", "", Language.translate("0.0 .. 1.0")},
            {"transparency layer 4", "", Language.translate("0.0 .. 1.0")},
            {"transparency layer 5", "", Language.translate("0.0 .. 1.0")},
            {"transparency layer 6", "", Language.translate("0.0 .. 1.0")},
            {"transparency layer 7", "", Language.translate("0.0 .. 1.0")},
            {"transparency layer 8", "", Language.translate("0.0 .. 1.0")},
            {"transparency layer 9", "", Language.translate("0.0 .. 1.0")},
            {"transparency layer 10", "", Language.translate("0.0 .. 1.0")},};
    private String[][] properties = {
            {"Color", null, null},
            {"background color", "", Language.translate("red, blue, green, gray, yellow....")},
            {"transparency", "", Language.translate("0.0 .. 1.0")},
            {"Zoom", null, null},
            {"zoom", "", Language.translate("1.0 means 100%")},
            {"zoom center x", "", Language.translate("default is 0")},
            {"zoom center y", "", Language.translate("default is 0")},
            {"Offset", null, null},
            {"background offset x", "", Language.translate("")},
            {"background offset y", "", Language.translate("")},
            {"regions offset x", "", Language.translate("")},
            {"regions offset y", "", Language.translate("")},
            {"Perspective", null, null},
            {"perspective type", "", Language.translate("1 point or two point")},
            {"perspective y", "", Language.translate("horizon")},
            {"perspective x1", "", Language.translate("point 1 on the horizon")},
            {"perspective x2", "", Language.translate("point 2 on the horizon")},
            {"Layers Active", null, null},
            {"active layers", "", Language.translate("comma separated list 1,2,..., empty for all")},
            {"layer 1", "", Language.translate("true or false")},
            {"layer 2", "", Language.translate("true or false")},
            {"layer 3", "", Language.translate("true or false")},
            {"layer 4", "", Language.translate("true or false")},
            {"layer 5", "", Language.translate("true or false")},
            {"layer 6", "", Language.translate("true or false")},
            {"layer 7", "", Language.translate("true or false")},
            {"layer 8", "", Language.translate("true or false")},
            {"layer 9", "", Language.translate("true or false")},
            {"layer 10", "", Language.translate("true or false")},
            {"Layers Transparency", null, null},
            {"transparency layer 1", "", Language.translate("0.0 .. 1.0")},
            {"transparency layer 2", "", Language.translate("0.0 .. 1.0")},
            {"transparency layer 3", "", Language.translate("0.0 .. 1.0")},
            {"transparency layer 4", "", Language.translate("0.0 .. 1.0")},
            {"transparency layer 5", "", Language.translate("0.0 .. 1.0")},
            {"transparency layer 6", "", Language.translate("0.0 .. 1.0")},
            {"transparency layer 7", "", Language.translate("0.0 .. 1.0")},
            {"transparency layer 8", "", Language.translate("0.0 .. 1.0")},
            {"transparency layer 9", "", Language.translate("0.0 .. 1.0")},
            {"transparency layer 10", "", Language.translate("0.0 .. 1.0")},};
    private String[][] propertiesLimits = {
            {"transparency", "0.0", "1.0", "1.0"},
            {"transparency layer 1", "0.0", "1.0", "1.0"},
            {"transparency layer 2", "0.0", "1.0", "1.0"},
            {"transparency layer 3", "0.0", "1.0", "1.0"},
            {"transparency layer 4", "0.0", "1.0", "1.0"},
            {"transparency layer 5", "0.0", "1.0", "1.0"},
            {"transparency layer 6", "0.0", "1.0", "1.0"},
            {"transparency layer 7", "0.0", "1.0", "1.0"},
            {"transparency layer 8", "0.0", "1.0", "1.0"},
            {"transparency layer 9", "0.0", "1.0", "1.0"},
            {"transparency layer 10", "0.0", "1.0", "1.0"},
            {"zoom", "0.1", "2.0", "1.0"},
            {"zoom center x", "0", "" + Toolkit.getDefaultToolkit().getScreenSize().width, "0"},
            {"zoom center y", "0", "" + Toolkit.getDefaultToolkit().getScreenSize().height, "0"},
            {"background offset x", "0", "" + Toolkit.getDefaultToolkit().getScreenSize().width, "0"},
            {"background offset y", "0", "" + Toolkit.getDefaultToolkit().getScreenSize().height, "0"},
            {"regions offset x", "0", "" + Toolkit.getDefaultToolkit().getScreenSize().width, "0"},
            {"regions offset y", "0", "" + Toolkit.getDefaultToolkit().getScreenSize().height, "0"},
            {"perspective y", "0", "" + Toolkit.getDefaultToolkit().getScreenSize().height, "500"},
            {"perspective x1", "0", "" + Toolkit.getDefaultToolkit().getScreenSize().width, "500"},
            {"perspective x2", "0", "" + Toolkit.getDefaultToolkit().getScreenSize().height, "1000"}};
    private String[][] propertiesAnimation = {
            {"Color", null, null, null, null, null},
            {"transparency", "", "", "", "", ""},
            {"Layers Transparency", null, null, null, null, null},
            {"transparency layer 1", "", "", "", "", ""},
            {"transparency layer 2", "", "", "", "", ""},
            {"transparency layer 3", "", "", "", "", ""},
            {"transparency layer 4", "", "", "", "", ""},
            {"transparency layer 5", "", "", "", "", ""},
            {"transparency layer 6", "", "", "", "", ""},
            {"transparency layer 7", "", "", "", "", ""},
            {"transparency layer 8", "", "", "", "", ""},
            {"transparency layer 9", "", "", "", "", ""},
            {"transparency layer 10", "", "", "", "", ""},
            {"Zoom", null, null, null, null, null},
            {"zoom", "", "", "", "", ""},
            {"zoom center x", "", "", "", "", ""},
            {"zoom center y", "", "", "", "", ""},
            {"Offset", null, null, null, null, null},
            {"background offset x", "", "", "", "", ""},
            {"background offset y", "", "", "", "", ""},
            {"regions offset x", "", "", "", "", ""},
            {"regions offset y", "", "", "", "", ""},
            {"Perspective", null, null, null, null, null},
            {"perspective type", "", "", "", "", ""},
            {"perspective y", "", "", "", "", ""},
            {"perspective x1", "", "", "", "", ""},
            {"perspective x2", "", "", "", "", ""},};
    private AnimatePropertiesThread animatePropertiesThread;
    private double perspective_horizont_x1 = 500.0;
    private double perspective_horizont_x2 = 1000.0;
    private double perspective_horizont_y = 500.0;
    private double zoom = 1.0;
    private double zoomCenterX = 0.0;
    private double zoomCenterY = 0.0;
    private VariableUpdatePageHandler variableUpdatePageHandler = new VariableUpdatePageHandler();
    private int pageWidth = 0;
    private int pageHeight = 0;
    private int remoteX = -1;
    private int remoteY = -1;
    private String[][] spreadsheetData = createEmptyData(99, 27);
    private String[][] prevSpreadsheetData = createEmptyData(99, 27);
    private String strSpreadsheetColumnWidths = "";
    private BufferedImage[] images = null;
    private boolean[] imageUpdated = new boolean[Page.NUMBER_OF_LAYERS];
    private List<Connector> connectors = new Vector<Connector>();
    private Connector selectedConnector = null;

    private boolean activating = false;
    private boolean saving = false;
    private Vector<String> updatingProperties = new Vector<String>();
    private boolean deleted = false;

    private MouseProcessor mouseProcessor = new MouseProcessor();
    private KeyboardProcessor keyboardProcessor = new KeyboardProcessor();

    private static boolean bSaveToHistory = false;
    private String strCache = "";

    public Page(String varPrefix, String varPostfix) {
        this.setVarPrefix(varPrefix);
        this.setVarPostfix(varPostfix);

        this.setLayerActive(new boolean[Page.NUMBER_OF_LAYERS]);
        for (int i = 0; i < Page.NUMBER_OF_LAYERS; i++) {
            this.getLayerActive()[i] = i == 0;
        }
    }

    public static String[][] getAllProperties() {
        return allProperties;
    }

    public static void setAllProperties(String[][] allProperties) {
        Page.allProperties = allProperties;
    }

    public static boolean isbSaveToHistory() {
        return bSaveToHistory;
    }

    public static void setbSaveToHistory(boolean bSaveToHistory) {
        Page.bSaveToHistory = bSaveToHistory;
    }

    public Page copyForUndo() {
        Page us = new Page(this.getVarPrefix(), this.getVarPostfix());
        us.setTitle(this.getTitle());
        us.setStrSpreadsheetColumnWidths(this.getStrSpreadsheetColumnWidths());

        copyArray(this.getProperties(), us.getProperties());
        copyArray(this.getPropertiesAnimation(), us.getPropertiesAnimation());
        copyArray(this.getSpreadsheetData(), us.getSpreadsheetData());
        copyArray(this.getPrevSpreadsheetData(), us.getPrevSpreadsheetData());
        us.getOnEntryMacro().setRepeat(this.getOnEntryMacro().getRepeat());
        us.getOnExitMacro().setRepeat(this.getOnExitMacro().getRepeat());
        copyArray(this.getOnEntryMacro().getActions(), us.getOnEntryMacro().getActions());
        copyArray(this.getOnExitMacro().getActions(), us.getOnExitMacro().getActions());

        for (EventMacro eventMacro : getKeyboardProcessor().getKeyboardEventMacros()) {
            us.getKeyboardProcessor().getKeyboardEventMacros().add(new KeyboardEventMacro(eventMacro));
        }
        for (EventMacro eventMacro : getVariableUpdateEventMacros()) {
            us.getVariableUpdateEventMacros().add(new VariableUpdateEventMacro(eventMacro));
        }

        return us;
    }

    private void copyEventMacro(EventMacro eventMacro1, EventMacro eventMacro2) {
        eventMacro2.setEventName(eventMacro1.getEventName());
        copyArray(eventMacro1.getMacro().getActions(), eventMacro2.getMacro().getActions());
    }

    public String[][] getSpreadsheetsData() {
        return this.getSpreadsheetData();
    }

    public void setPropertiesFromSketch(Page s) {
        this.setTitle(s.getTitle());
        this.setStrSpreadsheetColumnWidths(s.getStrSpreadsheetColumnWidths());

        copyArray(s.getProperties(), this.getProperties());
        copyArray(s.getPropertiesAnimation(), this.getPropertiesAnimation());
        copyArray(s.getSpreadsheetData(), this.getSpreadsheetData());
        copyArray(s.getPrevSpreadsheetData(), this.getPrevSpreadsheetData());
        for (EventMacro eventMacro : getKeyboardProcessor().getKeyboardEventMacros()) {
            s.getKeyboardProcessor().getKeyboardEventMacros().add(new KeyboardEventMacro(eventMacro));
        }

        for (EventMacro eventMacro : getVariableUpdateEventMacros()) {
            s.getVariableUpdateEventMacros().add(new VariableUpdateEventMacro(eventMacro));
        }
        this.getOnEntryMacro().setRepeat(s.getOnEntryMacro().getRepeat());
        this.getOnExitMacro().setRepeat(s.getOnExitMacro().getRepeat());
        copyArray(s.getOnEntryMacro().getActions(), this.getOnEntryMacro().getActions());
        copyArray(s.getOnExitMacro().getActions(), this.getOnExitMacro().getActions());
    }

    public static <T> void copyArray(T source[][], T target[][]) {
        for (int i = 0; i < source.length; i++) {
            for (int j = 0; j < source[i].length; j++) {
                target[i][j] = source[i][j];
            }
        }
    }

    public Connector selectConnector(int x, int y) {
        this.setSelectedConnector(null);
        for (Connector c : this.getConnectors()) {
            int x1 = c.getRegion1().getCenterX(false);
            int y1 = c.getRegion1().getCenterY(false);
            int x2 = c.getRegion2().getCenterX(false);
            int y2 = c.getRegion2().getCenterY(false);
            double d = DistancePointSegment.distanceToSegment(x, y, x1, y1, x2, y2);
            if (d < 6) {
                this.setSelectedConnector(c);
                break;
            } else if (c.getRenderer() != null && c.getRenderer().getTextRect() != null && c.getRenderer().getTextRect().contains(x, y)) {
                this.setSelectedConnector(c);
                break;
            }
        }

        return this.getSelectedConnector();
    }

    public void addConnector(Connector connector) {
        this.getConnectors().add(connector);
    }

    public void updateConnectors(boolean bPlayback) {
        for (Connector c : this.getConnectors()) {
            c.updateVariables(bPlayback);
        }
    }

    public void updateConnectors(ActiveRegion r, boolean bPlayback) {
        for (Connector c : this.getConnectors()) {
            if (c.getRegion1() == r || c.getRegion2() == r) {
                c.updateVariables(bPlayback);
            }
        }
    }

    public void removeConnector(Connector connector) {
        this.getConnectors().remove(connector);
    }

    public String[][] createEmptyData(int rows, int cols) {
        String data[][] = new String[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[i][j] = "";
            }
        }

        return data;
    }

    public String[][] getSpreadsheetData() {
        Page masterPage = Workspace.getMasterSketch();
        String masterData[][] = masterPage != null ? masterPage.spreadsheetData : null;
        if (masterPage == null || this == masterPage) {
            return this.spreadsheetData;
        } else {
            int rows = spreadsheetData.length;
            int cols = spreadsheetData[0].length;
            String data[][] = new String[rows][cols];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    data[i][j] = spreadsheetData[i][j];
                    if (data[i][j].isEmpty() && !masterData[i][j].isEmpty()) {
                        data[i][j] = masterData[i][j];
                    }
                }
            }

            return data;
        }
    }

    public String[][] getPrevSpreadsheetData() {
        return this.prevSpreadsheetData;
    }

    public boolean isMasterCell(int row, int col) {
        return this.getSpreadsheetData()[row][col].isEmpty();
    }

    public String getSpreadsheetCellValue(int row, int col) {
        if (row >= 0 && row < this.getSpreadsheetData().length && col >= 0 && col < this.getSpreadsheetData()[row].length) {
            String value = this.getSpreadsheetData()[row][col];
            return value == null ? "" : value;
        }

        return "";
    }

    public void updateSpreadsheetCell(int row, int col, String value) {
        if (row >= 0 && row < this.getSpreadsheetData().length && col >= 0 && col < this.getSpreadsheetData()[row].length) {
            this.getSpreadsheetData()[row][col] = value;
        }
    }

    public Page cloneEmbedded(String strVarPrefix, String strVarPostfix) {
        Page s = new Page(strVarPrefix, strVarPostfix);
        s.setSourceDirectory(this.getSourceDirectory());
        s.setVarPrefix(strVarPrefix);
        s.setVarPostfix(strVarPostfix);
        s.setRegions(this.getRegions().cloneEmbedded(s));

        s.setTitle(getTitle());
        s.setTextAnnotation(this.getTextAnnotation());
        s.setOnEntryMacro(new Macro(this.getOnEntryMacro()));
        s.setOnExitMacro(new Macro(this.getOnExitMacro()));
        s.initCache();

        return s;
    }

    public Page(Page s, String sourceDirectory, String varPrefix, String varPostfix) {
        this(varPrefix, varPostfix);
        this.setSourceDirectory(sourceDirectory);
        this.setRegions(new ActiveRegions(s.getRegions(), s));
        this.setTitle("" + s.getTitle());
        this.setTextAnnotation("" + s.getTextAnnotation());
        this.setTextAnnotation(s.getTextAnnotation());
        for (EventMacro eventMacro : s.getKeyboardProcessor().getKeyboardEventMacros()) {
            this.getKeyboardProcessor().getKeyboardEventMacros().add(new KeyboardEventMacro(eventMacro));
        }
        for (EventMacro eventMacro : s.getVariableUpdateEventMacros()) {
            this.getVariableUpdateEventMacros().add(new VariableUpdateEventMacro(eventMacro));
        }
        this.setOnEntryMacro(new Macro(s.getOnEntryMacro()));
        this.setOnExitMacro(new Macro(s.getOnExitMacro()));
        for (int i = 0; i < Page.NUMBER_OF_LAYERS; i++) {
            String strFileName;
            if (i == 0) {
                strFileName = "Sketch_" + this.getId() + ".png";
            } else {
                strFileName = "Sketch_" + this.getId() + "_" + (i + 1) + ".png";
            }
            File file = new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + strFileName);
            if (file.exists()) {
                File newFile = new File(sourceDirectory + strFileName);
                FileUtils.copyFile(file, newFile);
            }
        }
    }

    public void flush() {
        for (ActiveRegion a : this.getRegions().getRegions()) {
            a.flush();
        }
    }

    public void initRegionImages() {
        for (ActiveRegion a : this.getRegions().getRegions()) {
            a.initImages();
        }
    }

    public void dispose() {
        setId(null);
        setStateDiagramGraph(null);
        setStateDiagramCell(null);
        setOnEntryMacro(null);
        setOnExitMacro(null);
        setOnEntryMacroThread(null);
        setOnExitMacroThread(null);
        if (getKeyboardProcessor() != null) {
            getKeyboardProcessor().dispose();
            setKeyboardProcessor(null);
        }
        if (getVariableUpdateEventMacros() != null) {
            for (EventMacro eventMacro : getVariableUpdateEventMacros()) {
                eventMacro.dispose();
            }
            setVariableUpdateEventMacros(null);
        }
        setActiveTimers(null);
        setActiveMacros(null);
        setTextAnnotation(null);
        setSourceDirectory(null);
        setVarPrefix(null);
        setVarPostfix(null);
        setNotes(null);
        setProperties(null);
        setPropertiesLimits(null);
        setPropertiesAnimation(null);
        setAnimatePropertiesThread(null);
        this.strCache = null;

        if (getRegions() != null && getRegions().getRegions() != null) {
            for (ActiveRegion a : this.getRegions().getRegions()) {
                a.dispose();
            }
            this.getRegions().getRegions().removeAllElements();
        }

        setRegions(null);
    }

    public Vector<File> getImageFiles() {
        Vector<File> files = new Vector<File>();
        for (int i = 0; i < Page.NUMBER_OF_LAYERS; i++) {
            File file = this.getLayerImageFile(i);
            if (file.exists()) {
                files.add(file);
            }
        }
        for (ActiveRegion a : this.getRegions().getRegions()) {
            Vector<File> regFiles = a.getImageFiles();
            files.addAll(regFiles);
        }
        return files;
    }

    public File getLayerImageFile(int layer) {
        String strFileName;

        if (layer == 0) {
            strFileName = "Sketch_" + this.getId() + ".png";
        } else {
            strFileName = "Sketch_" + this.getId() + "_" + (layer + 1) + ".png";
        }

        return new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + strFileName);
    }

    public boolean isLayerActive(int layer) {
        if (layer >= Page.NUMBER_OF_LAYERS) {
            return true;
        }
        return layer >= 0 && this.getLayerActive()[layer];
    }

    private boolean regionsLayer = true;

    public boolean isRegionsLayerActive() {
        return this.isRegionsLayer();
    }

    public void setId(String strId) {
        if (strId != null && strId.trim().length() > 0) {
            id = strId;
        }
    }

    public String getId() {
        if (id == null || id.equals("null")) {
            id = "" + System.currentTimeMillis();
        }

        return id;
    }

    public int[] getBackgroundOffset(boolean bPlayback) {
        int offset[] = new int[]{0, 0};
        if (!bPlayback) {
            return offset;
        }

        String strX = getPropertyValue("background offset x");
        String strY = getPropertyValue("background offset y");

        if (!strX.isEmpty()) {
            try {
                offset[0] = (int) Double.parseDouble(strX);
            } catch (Throwable e) {
            }
        }
        if (!strY.isEmpty()) {
            try {
                offset[1] = (int) Double.parseDouble(strY);
            } catch (Throwable e) {
            }
        }

        return offset;
    }

    public int[] getRegionsOffset(boolean bPlayback) {
        int offset[] = new int[]{0, 0};
        if (!bPlayback) {
            return offset;
        }

        String strX = getPropertyValue("regions offset x");
        String strY = getPropertyValue("regions offset y");

        if (!strX.isEmpty()) {
            try {
                offset[0] = (int) Double.parseDouble(strX);
            } catch (Throwable e) {
            }
        }
        if (!strY.isEmpty()) {
            try {
                offset[1] = (int) Double.parseDouble(strY);
            } catch (Throwable e) {
            }
        }

        return offset;
    }

    public void calculateHorizonPoint() {
        setPerspective_horizont_x1(500.0);
        setPerspective_horizont_x2(1000.0);
        setPerspective_horizont_y(500.0);
        setZoom(1.0);
        setZoomCenterX(0.0);
        setZoomCenterY(0.0);
        try {
            if (!getPropertyValue("perspective x1").isEmpty()) {
                this.setPerspective_horizont_x1(Double.parseDouble(getPropertyValue("perspective x1")));
            }
        } catch (Throwable e) {
        }
        try {
            if (!getPropertyValue("perspective x2").isEmpty()) {
                this.setPerspective_horizont_x2(Double.parseDouble(getPropertyValue("perspective x2")));
            }
        } catch (Throwable e) {
        }
        try {
            if (!getPropertyValue("perspective y").isEmpty()) {
                this.setPerspective_horizont_y(Double.parseDouble(getPropertyValue("perspective y")));
            }
        } catch (Throwable e) {
        }
        try {
            if (!getPropertyValue("zoom").isEmpty()) {
                this.setZoom(Double.parseDouble(getPropertyValue("zoom")));
            }
        } catch (Throwable e) {
        }
        try {
            if (!getPropertyValue("zoom center x").isEmpty()) {
                this.setZoomCenterX(Double.parseDouble(getPropertyValue("zoom center x")));
            }
        } catch (Throwable e) {
        }
        try {
            if (!getPropertyValue("zoom center y").isEmpty()) {
                this.setZoomCenterY(Double.parseDouble(getPropertyValue("zoom center y")));
            }
        } catch (Throwable e) {
        }
    }

    public void logPropertyUpdate(String name, String value, Component source) {
    }

    public void setProperty(String name, String value) {
        if (updatingProperties.contains(name)) {
            return;
        }
        updatingProperties.add(name);
        for (int i = 0; i < this.getProperties().length; i++) {
            if (getProperties()[i][1] != null && ((String) getProperties()[i][0]).equalsIgnoreCase(name)) {
                getProperties()[i][1] = value;
                updatingProperties.remove(name);
                return;
            }
        }
        updatingProperties.remove(name);
    }

    public String getProperty(String name) {
        for (int i = 0; i < this.getProperties().length; i++) {
            if (((String) getProperties()[i][0]).equalsIgnoreCase(name) && getProperties()[i][1] != null) {
                String strValue = getProperties()[i][1];
                if (strValue.isEmpty() && name.equalsIgnoreCase("zoom")) {
                    strValue = SketchletEditor.getInstance().getScale() + "";
                }
                return strValue;
            }
        }
        return null;
    }

    public String getPropertyValue(String name) {
        String strValue = this.getProperty(name);
        if (strValue == null) {
            return null;
        }
        for (int i = 0; i < 10; i++) {
            if (strValue.startsWith("=")) {
                strValue = Evaluator.processText(strValue, getVarPrefix(), getVarPostfix());
            }
        }
        return strValue;
    }

    public JComboBox getPropertiesCombo() {
        JComboBox combo = new JComboBox();
        combo.setEditable(true);
        combo.addItem("");

        for (int i = 2; i < this.getProperties().length; i++) {
            if (getProperties()[i][1] != null) {
                combo.addItem(getProperties()[i][0]);
            }
        }

        return combo;
    }

    public int getPropertiesCount() {
        return this.getProperties().length;
    }

    public String[][] getData() {
        return this.getProperties();
    }

    public String getProperty(int index) {
        return Evaluator.processText((String) getProperties()[index][1], "", "");
    }

    public int getPropertyRow(String strProperty) {
        for (int i = 0; i < getProperties().length; i++) {
            if (getProperties()[i][1] != null && getProperties()[i][0].equalsIgnoreCase(strProperty)) {
                return i;
            }
        }
        return -1;
    }

    public String getPropertyDescription(String property) {
        for (int i = 0; i < getProperties().length; i++) {
            if (getProperties()[i][1] != null && getProperties()[i][0].equalsIgnoreCase(property)) {
                return getProperties()[i][2];
            }
        }
        return "";
    }

    public void repaintProperties() {
        if (SketchletEditor.getInstance() != null) {
            SketchletEditor.getInstance().repaint();
        }
        if (SketchletEditor.getInstance().getInternalPlaybackPanel() != null) {
            SketchletEditor.getInstance().getInternalPlaybackPanel().repaint();
        }
    }


    public boolean save(boolean bSaveImageThumb) {
        if (!deleted && !SketchletEditor.bInPreviewMode) {
            return saveSketchFile(getFileName(), bSaveImageThumb);
        }

        return false;
    }

    public String getFileName() {
        return SketchletContextUtils.getCurrentProjectSkecthletsDir() + "sketch_" + this.getId() + ".xml";
    }

    public void delete() {
        try {
            this.deactivate(false);
            deleted = true;

            FileUtils.deleteSafe(new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "sketch_" + this.getId() + ".xml"));
            FileUtils.deleteSafe(new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "Sketch_" + this.getId() + "_annotation.png"));

            ImageCache.remove(new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "Sketch_" + this.getId() + ".png"));
            for (int i = 0; i < Page.NUMBER_OF_LAYERS; i++) {
                File file = new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "Sketch_" + this.getId() + "_" + (i + 1) + ".png");
                ImageCache.remove(file);
            }

            for (Connector connector : this.getConnectors()) {
                connector.dispose();
            }
            this.getConnectors().clear();

            for (ActiveRegion region : this.getRegions().getRegions()) {
                for (int i = 0; i < region.getImageCount(); i++) {
                    ImageCache.remove(new File(region.getDrawImagePath(i)));
                }

                region.dispose();
            }
        } catch (Throwable e) {
            log.error(e);
        }
    }

    public void initCache() {
        strCache = getSketchFileString();
    }

    public boolean saveSketchFile(String strFile, boolean bSaveImageThumb) {
        try {
            saving = true;
            FileWriter fw = new FileWriter(strFile);
            PrintWriter outSketch = new PrintWriter(fw);
            saveSketchFile(outSketch);
            fw.close();
            if (getImages() != null) {
                for (int i = 0; getImages() != null && i < getImages().length; i++) {
                    if (getImageUpdated()[i] || bSaveImageThumb) {
                        try {
                            if (getImageUpdated()[i]) {
                                File file = getLayerImageFile(i);
                                if (getImages()[i] != null) {
                                    ImageCache.write(getImages()[i], file);
                                }
                            }
                            if (i == 0 && getImages() != null && getImages()[0] != null) {
                                BufferedImage thumb = Workspace.createCompatibleImage(getImages()[0].getWidth() / 6, getImages()[0].getHeight() / 6);
                                Graphics2D g2 = thumb.createGraphics();
                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                g2.scale(1.0 / 6, 1.0 / 6);
                                g2.drawImage(getImages()[0], 0, 0, null);
                                draw(g2, SketchletEditor.getInstance(), SketchletEditor.getInstance().getMode(), false, false, 1.0f);
                                g2.dispose();
                                File fileThumb = new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "Sketch_" + getId() + "_thumbnail.png");
                                ImageIO.write(thumb, "png", fileThumb);
                            }

                            getImageUpdated()[i] = false;
                        } catch (IOException e) {
                            log.error(e);
                        }
                    }
                }
            }

            saving = false;
            return true;
        } catch (Throwable e) {
            log.error(e);
        }

        saving = false;
        return false;
    }

    private void draw(Graphics2D g2, Component component, SketchletEditorMode mode, boolean bPlayback, boolean bHighlightRegions, float transparency) {
        int offset[] = getRegionsOffset(bPlayback);
        getRegions().setOffsetX(offset[0]);
        getRegions().setOffsetY(offset[1]);
        for (int i = getRegions().getRegions().size() - 1; i >= 0; i--) {
            ActiveRegion reg = getRegions().getRegions().elementAt(i);
            if (reg.isActive(bPlayback)) {
                reg.getRenderer().draw(g2, component, mode, bPlayback, bHighlightRegions, transparency);
            }
        }
    }

    public String getSketchFileString() {
        String strBuffer = "";
        try {
            StringWriter sw = new StringWriter();
            PrintWriter outSketch = new PrintWriter(sw);
            saveSketchFile(outSketch);
            sw.close();

            strBuffer = sw.getBuffer().toString();
        } catch (Throwable e) {
            log.error(e);
        }

        return strBuffer;
    }

    public boolean isChanged() {
        try {
            String strBuffer = getSketchFileString();

            if (strCache.equals(strBuffer)) {
                return false;
            } else {
                strCache = strBuffer;
                return true;
            }
        } catch (Throwable e) {
            log.error(e);
        }

        return true;
    }

    private boolean isNotEmpty(Object[][] data) {
        return !isEmpty(data);
    }

    private boolean isEmpty(Object[][] data) {
        for (Object row[] : data) {
            if (row != null) {
                for (Object element : row) {
                    if (StringUtils.isNotBlank(element.toString())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean isNotEmpty(Object[][] data, int col) {
        return !isEmpty(data, col);
    }

    private boolean isEmpty(Object[][] data, int col) {
        for (Object row[] : data) {
            if (row != null) {
                if (StringUtils.isNotBlank((String) row[col])) {
                    return false;
                }
            }
        }
        return true;
    }

    public void saveSketchFile(PrintWriter outSketch) {
        try {
            outSketch.println("<?xml version='1.0' encoding='UTF-8'?>");
            outSketch.println("<page>");
            outSketch.println("<title>" + this.getTitle() + "</title>");
            outSketch.println("<id>" + this.getId() + "</id>");
            outSketch.println("<state-diagram-x>" + this.getStateDiagramX() + "</state-diagram-x>");
            outSketch.println("<state-diagram-y>" + this.getStateDiagramY() + "</state-diagram-y>");
            outSketch.println("<page-width>" + this.getPageWidth() + "</page-width>");
            outSketch.println("<page-height>" + this.getPageHeight() + "</page-height>");
            outSketch.println("<page-layers>");
            for (int i = 0; i < this.getLayerActive().length; i++) {
                if (getLayerActive()[i]) {
                    outSketch.println("    <page-layer index='" + i + "' active='" + getLayerActive()[i] + "'/>");
                }
            }
            outSketch.println("</page-layers>");

            if (getPageVariables().size() > 0) {
                outSketch.println("<page-variables>");
                for (PageVariable pageVariable : this.getPageVariables()) {
                    outSketch.println("    <page-variable name='" + pageVariable.getName() + "' format='" + pageVariable.getFormat() + "'><![CDATA[" + pageVariable.getValue() + "]]></page-variable>");
                }
                outSketch.println("</page-variables>");
            }


            if (getProperties() != null) {
                if (isNotEmpty(getProperties(), 1)) {
                    outSketch.println("<page-parameters>");
                    for (int i = 0; i < this.getProperties().length; i++) {
                        if (getProperties()[i][1] == null || getProperties()[i][1].toString().isEmpty()) {
                            continue;
                        }

                        outSketch.println("    <page-property name='" + getProperties()[i][0] + "'><![CDATA[" + getProperties()[i][1] + "]]></page-property>");
                    }
                    outSketch.println("</page-parameters>");
                }
            }

            if (getSpreadsheetData() != null) {
                if (isNotEmpty(getSpreadsheetData())) {
                    outSketch.println("<page-spreadsheet>");
                    if (!getStrSpreadsheetColumnWidths().isEmpty()) {
                        outSketch.println("    <spreadsheet-column-widths>" + XMLUtils.prepareForXML(getStrSpreadsheetColumnWidths()) + "</spreadsheet-column-widths>");
                    }
                    for (int i = 0; i < this.getSpreadsheetData().length; i++) {
                        for (int j = 0; j < this.getSpreadsheetData()[i].length; j++) {
                            String strCell = getSpreadsheetData()[i][j];
                            if (!strCell.isEmpty()) {
                                outSketch.println("    <spreadsheet-cell x='" + i + "' y='" + j + "'>" + XMLUtils.prepareForXML(strCell) + "</spreadsheet-cell>");
                            }
                        }
                    }
                    outSketch.println("</page-spreadsheet>");
                }
            }

            if (isNotEmpty(getPropertiesAnimation(), 1)) {
                outSketch.println("<animate-page-parameters>");
                for (int i = 0; i < this.getPropertiesAnimation().length; i++) {
                    if (getPropertiesAnimation()[i][1] == null) {
                        continue;
                    }
                    boolean bAdd = false;
                    for (int j = 1; j < getPropertiesAnimation()[i].length; j++) {
                        if (!getPropertiesAnimation()[i][j].isEmpty()) {
                            bAdd = true;
                            break;
                        }
                    }
                    if (!bAdd) {
                        continue;
                    }
                    outSketch.println("    <animate-page-property name='" + getPropertiesAnimation()[i][0] + "' type='" + getPropertiesAnimation()[i][1] + "' start = '" + getPropertiesAnimation()[i][2] + "' end = '" + getPropertiesAnimation()[i][3] + "' duration='" + getPropertiesAnimation()[i][4] + "' curve='" + getPropertiesAnimation()[i][5] + "'/>");
                }

                outSketch.println("</animate-page-parameters>");
            }

            if (StringUtils.isNotBlank(getTextAnnotation())) {
                outSketch.println("<text-annotation>" + XMLUtils.prepareForXML(getTextAnnotation()) + "</text-annotation>");
            }

            getRegions().save(outSketch);

            if (isNotEmpty(getOnEntryMacro().getActions())) {
                this.getOnEntryMacro().save(outSketch, "on-entry");
            }
            if (isNotEmpty(getOnExitMacro().getActions())) {
                this.getOnExitMacro().save(outSketch, "on-exit");
            }
            if (getKeyboardProcessor().getKeyboardEventMacros().size() > 0) {
                outSketch.println("<keyboard-event-actions>");
                for (KeyboardEventMacro keyboardEventMacro : getKeyboardProcessor().getKeyboardEventMacros()) {
                    keyboardEventMacro.getMacro().setName(keyboardEventMacro.getEventName());
                    keyboardEventMacro.getMacro().saveSimple(outSketch, "keyboard-event-action", "    ");
                }
                outSketch.println("</keyboard-event-actions>");
            }
            if (getMouseProcessor().getMouseEventMacros().size() > 0) {
                outSketch.println("<mouse-event-actions>");
                for (MouseEventMacro mouseEventMacro : getMouseProcessor().getMouseEventMacros()) {
                    mouseEventMacro.getMacro().setName(mouseEventMacro.getEventName());
                    mouseEventMacro.getMacro().saveSimple(outSketch, "mouse-event-action", "    ");
                }
                outSketch.println("</mouse-event-actions>");
            }
            if (getVariableUpdateEventMacros().size() > 0) {
                outSketch.println("<variable-update-event-actions>");
                for (VariableUpdateEventMacro variableUpdateEventMacro : getVariableUpdateEventMacros()) {
                    variableUpdateEventMacro.getMacro().setName(variableUpdateEventMacro.getEventName());
                    variableUpdateEventMacro.getMacro().saveSimple(outSketch, "variable-update-event-action", "    ");
                }
                outSketch.println("</variable-update-event-actions>");
            }

            if (getConnectors().size() > 0) {
                outSketch.println("<connectors>");
                for (Connector connector : this.getConnectors()) {
                    outSketch.println(connector.toXML("    "));
                }
                outSketch.println("</connectors>");
            }

            if (getNotes().size() > 0) {
                outSketch.println("<post-notes>");
                for (NoteDialog note : getNotes()) {
                    int x = note.getX();
                    int y = note.getY();
                    int s = note.isMinimized() ? 1 : 0;

                    int w = note.isMinimized() ? note.getOriginalWidth() : note.getWidth();
                    int h = note.isMinimized() ? note.getOriginalHeight() : note.getHeight();

                    outSketch.print("    <post-note x='" + x + "' y='" + y + "' w='" + w + "' h='" + h + "' state='" + s + "'>");
                    outSketch.print(XMLUtils.prepareForXML(note.getNoteText()));
                    outSketch.println("</post-note>");
                }
                outSketch.println("</post-notes>");
            }

            outSketch.println("</page>");
            outSketch.flush();
            outSketch.close();
        } catch (Throwable e) {
            log.error(e);
        }
    }

    public boolean isConnectedTo(Page s) {
        for (ActiveRegion region : getRegions().getRegions()) {
            for (MouseEventMacro mouseEventMacro : region.mouseProcessor.getMouseEventMacros()) {
                for (int i = 0; i < mouseEventMacro.getMacro().getActions().length; i++) {
                    String action = (String) mouseEventMacro.getMacro().getActions()[i][0];
                    String param = (String) mouseEventMacro.getMacro().getActions()[i][1];
                    if (action.equalsIgnoreCase("Go to page") && param.equalsIgnoreCase(s.getTitle())) {
                        return true;
                    } else if ((action.equalsIgnoreCase("Call Macro") || action.equalsIgnoreCase("Start Action")) && Macros.globalMacros.isConnected(param, s.getTitle())) {
                        return true;
                    }
                }
            }
        }
        for (ActiveRegion a : getRegions().getRegions()) {
            for (RegionOverlapEventMacro regionOverlapEventMacro : a.regionOverlapEventMacros) {
                for (int i = 0; i < regionOverlapEventMacro.getMacro().getActions().length; i++) {
                    String action = (String) regionOverlapEventMacro.getMacro().getActions()[i][0];
                    String param = (String) regionOverlapEventMacro.getMacro().getActions()[i][1];
                    if (action.equalsIgnoreCase("Go to page") && param.equalsIgnoreCase(s.getTitle())) {
                        return true;
                    } else if ((action.equalsIgnoreCase("Call Macro") || action.equalsIgnoreCase("Start Action")) && Macros.globalMacros.isConnected(param, s.getTitle())) {
                        return true;
                    }
                }
            }
        }

        if (isMacroConnected(getOnEntryMacro(), s)) {
            return true;
        }

        if (isMacroConnected(getOnExitMacro(), s)) {
            return true;
        }
        for (KeyboardEventMacro keyboardEventMacro : getKeyboardProcessor().getKeyboardEventMacros()) {
            if (isMacroConnected(keyboardEventMacro.getMacro(), s)) {
                return true;
            }
        }

        return false;
    }

    private boolean isMacroConnected(Macro macro, Page page) {
        for (int i = 0; i < macro.getActions().length; i++) {
            String action = (String) macro.getActions()[i][0];
            String param = (String) macro.getActions()[i][1];
            if (action.equalsIgnoreCase("Go to page") && param.equalsIgnoreCase(page.getTitle())) {
                return true;
            } else if ((action.equalsIgnoreCase("Call Macro") || action.equalsIgnoreCase("Start Action")) && Macros.globalMacros.isConnected(param, page.getTitle())) {
                return true;
            }
        }

        return false;
    }

    public Set<String> getConnections(Page s) {
        Set<String> connections = new HashSet<String>();
        for (ActiveRegion region : getRegions().getRegions()) {
            for (MouseEventMacro mouseEventMacro : region.mouseProcessor.getMouseEventMacros()) {
                for (int i = 0; i < mouseEventMacro.getMacro().getActions().length; i++) {
                    String event = "on region '" + region.getName() + "' " + (String) mouseEventMacro.getEventName() + "";
                    String action = (String) mouseEventMacro.getMacro().getActions()[i][0];
                    String param = (String) mouseEventMacro.getMacro().getActions()[i][1];
                    if (action.equalsIgnoreCase("Go to page") && param.equalsIgnoreCase(s.getTitle())) {
                        connections.add(event);
                    } else if ((action.equalsIgnoreCase("Call Macro") || action.equalsIgnoreCase("Start Action")) && Macros.globalMacros.isConnected(param, s.getTitle())) {
                        connections.add(event + ", macro(" + param + ")");
                    }
                }
            }

            for (RegionOverlapEventMacro regionOverlapEventMacro : region.regionOverlapEventMacros) {
                for (int i = 0; i < regionOverlapEventMacro.getMacro().getActions().length; i++) {
                    String event = "overlap event";
                    String action = (String) regionOverlapEventMacro.getMacro().getActions()[i][0];
                    String param = (String) regionOverlapEventMacro.getMacro().getActions()[i][1];
                    if (action.equalsIgnoreCase("Go to page") && param.equalsIgnoreCase(s.getTitle())) {
                        connections.add(event);
                    } else if ((action.equalsIgnoreCase("Call Macro") || action.equalsIgnoreCase("Start Action")) && Macros.globalMacros.isConnected(param, s.getTitle())) {
                        connections.add(event + ", macro(" + param + ")");
                    }
                }
            }
        }

        for (int i = 0; i < this.getOnEntryMacro().getActions().length; i++) {
            String action = (String) this.getOnEntryMacro().getActions()[i][0];
            String param = (String) this.getOnEntryMacro().getActions()[i][1];
            if (action.equalsIgnoreCase("Go to page") && param.equalsIgnoreCase(s.getTitle())) {
                connections.add("on page entry");
            } else if ((action.equalsIgnoreCase("Call Macro") || action.equalsIgnoreCase("Start Action")) && Macros.globalMacros.isConnected(param, s.getTitle())) {
                connections.add("on page entry, macro(" + param + ")");
            }
        }

        for (int i = 0; i < this.getOnExitMacro().getActions().length; i++) {
            String action = (String) this.getOnExitMacro().getActions()[i][0];
            String param = (String) this.getOnExitMacro().getActions()[i][1];
            if (action.equalsIgnoreCase("Go to page") && param.equalsIgnoreCase(s.getTitle())) {
                connections.add("on page exit");
            } else if ((action.equalsIgnoreCase("Call Macro") || action.equalsIgnoreCase("Start Action")) && Macros.globalMacros.isConnected(param, s.getTitle())) {
                connections.add("on page exit, macro(" + param + ")");
            }
        }

        for (KeyboardEventMacro keyboardEventMacro : getKeyboardProcessor().getKeyboardEventMacros()) {
            String key = keyboardEventMacro.getKey();
            Object actions[][] = keyboardEventMacro.getMacro().getActions();
            for (int i = 0; i < actions.length; i++) {
                String action = (String) actions[i][0];
                String param = (String) actions[i][1];
                if (action.equalsIgnoreCase("Go to page") && param.equalsIgnoreCase(s.getTitle())) {
                    connections.add("when " + keyboardEventMacro.getModifiers() + key + " " + action);
                } else if ((action.equalsIgnoreCase("Call Macro") || action.equalsIgnoreCase("Start Action")) && Macros.globalMacros.isConnected(param, s.getTitle())) {
                    connections.add("when " + keyboardEventMacro.getModifiers() + key + " " + action + ", macro(" + param + ")");
                }
            }
        }

        for (VariableUpdateEventMacro variableUpdateEventMacro : getVariableUpdateEventMacros()) {
            String var = variableUpdateEventMacro.getVariable();
            String op = variableUpdateEventMacro.getOperator();
            String value = variableUpdateEventMacro.getValue();
            Object actions[][] = variableUpdateEventMacro.getMacro().getActions();
            for (int i = 0; i < actions.length; i++) {
                String action = (String) actions[i][0];
                String param = (String) actions[i][1];
                if (action.equalsIgnoreCase("Go to page") && param.equalsIgnoreCase(s.getTitle())) {
                    connections.add("when " + var + " " + op + " " + value);
                } else if ((action.equalsIgnoreCase("Call Macro") || action.equalsIgnoreCase("Start Action")) && Macros.globalMacros.isConnected(param, s.getTitle())) {
                    connections.add("when " + var + " " + op + " " + value + ", macro(" + param + ")");
                }
            }
        }

        return connections;
    }

    public void deactivate(boolean inPlaybackMode) {
        try {
            setRemoteX(-1);
            setRemoteY(-1);

            RefreshScreenCaptureThread.stop();

            if (this.getAnimatePropertiesThread() != null) {
                this.getAnimatePropertiesThread().setStopped(true);
                this.setAnimatePropertiesThread(null);
            }
            if (getOnEntryMacroThread() != null) {
                getOnEntryMacroThread().stop();
            }
            if (inPlaybackMode && getOnExitMacro() != null) {
                setOnExitMacroThread(getOnExitMacro().startThread("", getVarPrefix(), getVarPostfix()));
                try {
                    while (!getOnExitMacroThread().isStopped()) {
                        Thread.sleep(10);
                    }
                } catch (Throwable e) {
                }
            } else if (getOnExitMacroThread() != null) {
                getOnExitMacroThread().stop();
            }

            if (getActiveTimers() != null) {
                Object activeTimersArray[] = getActiveTimers().toArray();
                for (int i = 0; i < activeTimersArray.length; i++) {
                    TimerThread tt = (TimerThread) activeTimersArray[i];
                    if (tt != null) {
                        tt.stop();
                    }
                }
                getActiveTimers().clear();
            }

            if (getActiveMacros() != null) {
                Object activeMacrosArray[] = getActiveMacros().toArray();
                for (int i = 0; i < activeMacrosArray.length; i++) {
                    RunInterface mt = (RunInterface) activeMacrosArray[i];
                    if (mt != null) {
                        mt.stop();
                    }
                }
                getActiveMacros().clear();
            }

            if (getRegions() != null && getRegions().getRegions() != null) {
                for (ActiveRegion a : getRegions().getRegions()) {
                    a.deactivate(inPlaybackMode);
                }
            }

            while (saving) {
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                }
            }

            if (this.getImages() != null) {
                synchronized (this.getImages()) {
                    for (int i = 0; i < this.getImages().length; i++) {
                        if (this.getImages()[i] != null) {
                            this.getImages()[i].flush();
                            this.getImages()[i] = null;
                        }
                    }
                    this.setImages(null);
                }
            }

            if (!inPlaybackMode && getNotes() != null) {
                for (NoteDialog note : getNotes()) {
                    SketchletEditor.getInstance().removeNote(note);
                }
            }

        } catch (Throwable e) {
        }
        ApplicationLifecycleCentre.beforePageExit(this);
    }

    public void play() {
        for (ActiveRegion a : getRegions().getRegions()) {
            a.play();
        }
    }

    public void activate(boolean bPlayback) {
        if (activating) {
            return;
        }

        activating = true;
        this.initImages();
        if (bPlayback) {
            play();
            if (getOnEntryMacro() != null) {
                setOnEntryMacroThread(getOnEntryMacro().startThread("", getVarPrefix(), getVarPostfix()));
            }
            if (this.getAnimatePropertiesThread() == null) {
                this.setAnimatePropertiesThread(new AnimatePropertiesThread(this));
            }
        } else {
            getRegions().getVariablesHelper().refreshFromVariables();
        }
        if (getStateDiagramGraph() != null && getStateDiagramCell() != null) {
            getStateDiagramGraph().setSelectionCell(getStateDiagramCell());
        }

        this.calculateHorizonPoint();

        for (ActiveRegion a : getRegions().getRegions()) {
            a.activate(bPlayback);
        }

        if (!bPlayback) {
            Object notesArray[] = getNotes().toArray();
            for (int i = 0; i < notesArray.length; i++) {
                NoteDialog note = (NoteDialog) notesArray[i];
                SketchletEditor.getInstance().addAndShowNote(note);
            }
        }

        activating = false;
        RefreshTime.update();
        ApplicationLifecycleCentre.afterPageEntry(this);
    }


    public void setAnimateProperty(String property, String type, String start, String end, String duration, String curve) {
        for (int i = 0; i < this.getPropertiesAnimation().length; i++) {
            if (getPropertiesAnimation()[i][1] != null && getPropertiesAnimation()[i][0].equals(property)) {
                getPropertiesAnimation()[i][1] = type;
                getPropertiesAnimation()[i][2] = start;
                getPropertiesAnimation()[i][3] = end;
                getPropertiesAnimation()[i][4] = duration;
                getPropertiesAnimation()[i][5] = curve;
                return;
            }
        }
    }

    public String getDefaultValue(String strProperty) {
        for (int i = 0; i < this.getPropertiesLimits().length; i++) {
            if (this.getPropertiesLimits()[i][0].equalsIgnoreCase(strProperty)) {
                return getPropertiesLimits()[i][3];
            }
        }

        return null;
    }

    public String getMinValue(String strProperty) {
        for (int i = 0; i < this.getPropertiesLimits().length; i++) {
            if (this.getPropertiesLimits()[i][0].equalsIgnoreCase(strProperty)) {
                return getPropertiesLimits()[i][1];
            }
        }

        return null;
    }

    public String getMaxValue(String strProperty) {
        for (int i = 0; i < this.getPropertiesLimits().length; i++) {
            if (this.getPropertiesLimits()[i][0].equalsIgnoreCase(strProperty)) {
                return getPropertiesLimits()[i][2];
            }
        }

        return null;
    }

    public String getTransferString(String strProperty) {
        return "sketch." + strProperty;
    }

    public boolean isLayerActive(int layerIndex, boolean bPlayback) {
        boolean bLayerActive = true;
        if (bPlayback) {
            String strActiveLayers = this.getPropertyValue("active layers").trim();
            if (strActiveLayers.isEmpty()) {
                String strLayer = this.getPropertyValue("layer " + (layerIndex + 1));
                if (strLayer.isEmpty()) {
                    return this.getLayerActive()[layerIndex];
                } else {
                    return !strLayer.equalsIgnoreCase("false");
                }
            } else {
                String strLayer = this.getPropertyValue("layer " + (layerIndex + 1));
                strActiveLayers = "," + strActiveLayers.replace(" ", "") + ",";
                return strActiveLayers.contains("," + (layerIndex + 1) + ",") || strLayer.equalsIgnoreCase("true");
            }
        } else {
            bLayerActive = this.getLayerActive()[layerIndex];
        }
        return bLayerActive;
    }

    public String getTitle() {
        return this.title;
    }

    public void initImages() {
        this.setImages(new BufferedImage[Page.NUMBER_OF_LAYERS]);
        for (int i = 0; i < getImages().length; i++) {
            initImage(i);
        }
    }

    public void initImage(int _layer) {
        initImage(_layer, false);
    }

    public void initImage(int _layer, boolean bForceRead) {
        this.getImageUpdated()[_layer] = false;
        try {
            File file = this.getLayerImageFile(_layer);
            if (file != null && file.exists()) {
                BufferedImage image = ImageCache.read(file, getImages()[_layer], bForceRead);
                getImages()[_layer] = image;
            } else {
                getImages()[_layer] = null;
            }
        } catch (Throwable e) {
            getImages()[_layer] = null;
            log.error(e);
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean[] getLayerActive() {
        return layerActive;
    }

    public void setLayerActive(boolean[] layerActive) {
        this.layerActive = layerActive;
    }

    public ActiveRegions getRegions() {
        return regions;
    }

    public void setRegions(ActiveRegions regions) {
        this.regions = regions;
    }

    public double getStateDiagramX() {
        return stateDiagramX;
    }

    public void setStateDiagramX(double stateDiagramX) {
        this.stateDiagramX = stateDiagramX;
    }

    public double getStateDiagramY() {
        return stateDiagramY;
    }

    public void setStateDiagramY(double stateDiagramY) {
        this.stateDiagramY = stateDiagramY;
    }

    public JGraph getStateDiagramGraph() {
        return stateDiagramGraph;
    }

    public void setStateDiagramGraph(JGraph stateDiagramGraph) {
        this.stateDiagramGraph = stateDiagramGraph;
    }

    public DefaultGraphCell getStateDiagramCell() {
        return stateDiagramCell;
    }

    public void setStateDiagramCell(DefaultGraphCell stateDiagramCell) {
        this.stateDiagramCell = stateDiagramCell;
    }

    public Macro getOnEntryMacro() {
        return onEntryMacro;
    }

    public void setOnEntryMacro(Macro onEntryMacro) {
        this.onEntryMacro = onEntryMacro;
    }

    public Macro getOnExitMacro() {
        return onExitMacro;
    }

    public void setOnExitMacro(Macro onExitMacro) {
        this.onExitMacro = onExitMacro;
    }

    public MacroThread getOnEntryMacroThread() {
        return onEntryMacroThread;
    }

    public void setOnEntryMacroThread(MacroThread onEntryMacroThread) {
        this.onEntryMacroThread = onEntryMacroThread;
    }

    public MacroThread getOnExitMacroThread() {
        return onExitMacroThread;
    }

    public void setOnExitMacroThread(MacroThread onExitMacroThread) {
        this.onExitMacroThread = onExitMacroThread;
    }

    public List<TimerThread> getActiveTimers() {
        return activeTimers;
    }

    public void setActiveTimers(List<TimerThread> activeTimers) {
        this.activeTimers = activeTimers;
    }

    public List<RunInterface> getActiveMacros() {
        return activeMacros;
    }

    public void setActiveMacros(List<RunInterface> activeMacros) {
        this.activeMacros = activeMacros;
    }

    public String getTextAnnotation() {
        return textAnnotation;
    }

    public void setTextAnnotation(String textAnnotation) {
        this.textAnnotation = textAnnotation;
    }

    public String getSourceDirectory() {
        return sourceDirectory;
    }

    public void setSourceDirectory(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public String getVarPrefix() {
        return varPrefix;
    }

    public void setVarPrefix(String varPrefix) {
        this.varPrefix = varPrefix;
    }

    public String getVarPostfix() {
        return varPostfix;
    }

    public void setVarPostfix(String varPostfix) {
        this.varPostfix = varPostfix;
    }

    public Vector<NoteDialog> getNotes() {
        return notes;
    }

    public void setNotes(Vector<NoteDialog> notes) {
        this.notes = notes;
    }

    public List<PageVariable> getPageVariables() {
        return pageVariables;
    }

    public void setPageVariables(List<PageVariable> pageVariables) {
        this.pageVariables = pageVariables;
    }

    public List<VariableUpdateEventMacro> getVariableUpdateEventMacros() {
        return variableUpdateEventMacros;
    }

    public void setVariableUpdateEventMacros(List<VariableUpdateEventMacro> variableUpdateEventMacros) {
        this.variableUpdateEventMacros = variableUpdateEventMacros;
    }

    public String[][] getProperties() {
        return properties;
    }

    public void setProperties(String[][] properties) {
        this.properties = properties;
    }

    public String[][] getPropertiesLimits() {
        return propertiesLimits;
    }

    public void setPropertiesLimits(String[][] propertiesLimits) {
        this.propertiesLimits = propertiesLimits;
    }

    public String[][] getPropertiesAnimation() {
        return propertiesAnimation;
    }

    public void setPropertiesAnimation(String[][] propertiesAnimation) {
        this.propertiesAnimation = propertiesAnimation;
    }

    public AnimatePropertiesThread getAnimatePropertiesThread() {
        return animatePropertiesThread;
    }

    public void setAnimatePropertiesThread(AnimatePropertiesThread animatePropertiesThread) {
        this.animatePropertiesThread = animatePropertiesThread;
    }

    public double getPerspective_horizont_x1() {
        return perspective_horizont_x1;
    }

    public void setPerspective_horizont_x1(double perspective_horizont_x1) {
        this.perspective_horizont_x1 = perspective_horizont_x1;
    }

    public double getPerspective_horizont_x2() {
        return perspective_horizont_x2;
    }

    public void setPerspective_horizont_x2(double perspective_horizont_x2) {
        this.perspective_horizont_x2 = perspective_horizont_x2;
    }

    public double getPerspective_horizont_y() {
        return perspective_horizont_y;
    }

    public void setPerspective_horizont_y(double perspective_horizont_y) {
        this.perspective_horizont_y = perspective_horizont_y;
    }

    public double getZoom() {
        return zoom;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    public double getZoomCenterX() {
        return zoomCenterX;
    }

    public void setZoomCenterX(double zoomCenterX) {
        this.zoomCenterX = zoomCenterX;
    }

    public double getZoomCenterY() {
        return zoomCenterY;
    }

    public void setZoomCenterY(double zoomCenterY) {
        this.zoomCenterY = zoomCenterY;
    }

    public VariableUpdatePageHandler getVariableUpdatePageHandler() {
        return variableUpdatePageHandler;
    }

    public void setVariableUpdatePageHandler(VariableUpdatePageHandler variableUpdatePageHandler) {
        this.variableUpdatePageHandler = variableUpdatePageHandler;
    }

    public int getPageWidth() {
        return pageWidth;
    }

    public void setPageWidth(int pageWidth) {
        this.pageWidth = pageWidth;
    }

    public int getPageHeight() {
        return pageHeight;
    }

    public void setPageHeight(int pageHeight) {
        this.pageHeight = pageHeight;
    }

    public int getRemoteX() {
        return remoteX;
    }

    public void setRemoteX(int remoteX) {
        this.remoteX = remoteX;
    }

    public int getRemoteY() {
        return remoteY;
    }

    public void setRemoteY(int remoteY) {
        this.remoteY = remoteY;
    }

    public void setSpreadsheetData(String[][] spreadsheetData) {
        this.spreadsheetData = spreadsheetData;
    }

    public void setPrevSpreadsheetData(String[][] prevSpreadsheetData) {
        this.prevSpreadsheetData = prevSpreadsheetData;
    }

    public String getStrSpreadsheetColumnWidths() {
        return strSpreadsheetColumnWidths;
    }

    public void setStrSpreadsheetColumnWidths(String strSpreadsheetColumnWidths) {
        this.strSpreadsheetColumnWidths = strSpreadsheetColumnWidths;
    }

    public BufferedImage[] getImages() {
        return images;
    }

    public void setImages(BufferedImage[] images) {
        this.images = images;
    }

    public boolean[] getImageUpdated() {
        return imageUpdated;
    }

    public void setImageUpdated(boolean[] imageUpdated) {
        this.imageUpdated = imageUpdated;
    }

    public List<Connector> getConnectors() {
        return connectors;
    }

    public void setConnectors(List<Connector> connectors) {
        this.connectors = connectors;
    }

    public Connector getSelectedConnector() {
        return selectedConnector;
    }

    public void setSelectedConnector(Connector selectedConnector) {
        this.selectedConnector = selectedConnector;
    }

    public MouseProcessor getMouseProcessor() {
        return mouseProcessor;
    }

    public void setMouseProcessor(MouseProcessor mouseProcessor) {
        this.mouseProcessor = mouseProcessor;
    }

    public KeyboardProcessor getKeyboardProcessor() {
        return keyboardProcessor;
    }

    public void setKeyboardProcessor(KeyboardProcessor keyboardProcessor) {
        this.keyboardProcessor = keyboardProcessor;
    }

    public boolean isRegionsLayer() {
        return regionsLayer;
    }

    public void setRegionsLayer(boolean regionsLayer) {
        this.regionsLayer = regionsLayer;
    }
}
