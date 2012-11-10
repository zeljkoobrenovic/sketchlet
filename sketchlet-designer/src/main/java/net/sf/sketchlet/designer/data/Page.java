/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.data;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.common.geom.DistancePointSegment;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.ApplicationLifecycleCentre;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.animation.AnimatePropertiesThread;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.regions.connector.Connector;
import net.sf.sketchlet.designer.editor.tool.notes.NoteDialog;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.programming.macros.Macro;
import net.sf.sketchlet.designer.programming.macros.MacroThread;
import net.sf.sketchlet.designer.programming.macros.Macros;
import net.sf.sketchlet.designer.programming.timers.TimerThread;
import net.sf.sketchlet.designer.tools.imagecache.ImageCache;
import net.sf.sketchlet.designer.ui.page.VariableUpdatePageHandler;
import net.sf.sketchlet.designer.ui.playback.RefreshScreenCaptureThread;
import net.sf.sketchlet.script.RunInterface;
import net.sf.sketchlet.util.RefreshTime;
import net.sf.sketchlet.util.XMLUtils;
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
    public String title = "New Sketch";
    public boolean layerActive[];
    public ActiveRegions regions;
    private String id = null;
    public double stateDiagramX = 21.0;
    public double stateDiagramY = 21.0;
    public JGraph stateDiagramGraph = null;
    public DefaultGraphCell stateDiagramCell = null;
    public Macro onEntryMacro = new Macro();
    public Macro onExitMacro = new Macro();
    public MacroThread onEntryMacroThread;
    public MacroThread onExitMacroThread;
    public Vector<TimerThread> activeTimers = new Vector<TimerThread>();
    public Vector<RunInterface> activeMacros = new Vector<RunInterface>();
    public String strTextAnnotation = "";
    public String strSourceDirectory = null;
    public String strVarPrefix = "", strVarPostfix = "";
    public Vector<NoteDialog> notes = new Vector<NoteDialog>();
    public List<LocalVariable> localVariables = new ArrayList<LocalVariable>();
    public List<VariableUpdateEventMacro> variableUpdateEventMacros = new ArrayList<VariableUpdateEventMacro>();
    public static String allProperties[][] = {
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
    public String properties[][] = {
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
    public String propertiesLimits[][] = {
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
    public String propertiesAnimation[][] = {
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
    AnimatePropertiesThread animatePropertiesThread;
    public double perspective_horizont_x1 = 500.0;
    public double perspective_horizont_x2 = 1000.0;
    public double perspective_horizont_y = 500.0;
    public double zoom = 1.0;
    public double zoomCenterX = 0.0;
    public double zoomCenterY = 0.0;
    public VariableUpdatePageHandler variableUpdatePageHandler = new VariableUpdatePageHandler();
    public int pageWidth = 0;
    public int pageHeight = 0;
    public int remoteX = -1;
    public int remoteY = -1;
    private String spreadsheetData[][] = createEmptyData(99, 27);
    private String prevSpreadsheetData[][] = createEmptyData(99, 27);
    public String strSpreadsheetColumnWidths = "";
    public BufferedImage images[] = null;
    public boolean imageUpdated[] = new boolean[Page.NUMBER_OF_LAYERS];
    public List<Connector> connectors = new Vector<Connector>();
    public Connector selectedConnector = null;

    public MouseProcessor mouseProcessor = new MouseProcessor();
    public KeyboardProcessor keyboardProcessor = new KeyboardProcessor();

    public Page(String strVarPrefix, String strVarPostfix) {
        this.strVarPrefix = strVarPrefix;
        this.strVarPostfix = strVarPostfix;

        this.layerActive = new boolean[Page.NUMBER_OF_LAYERS];
        for (int i = 0; i < Page.NUMBER_OF_LAYERS; i++) {
            this.layerActive[i] = i == 0;
        }
    }

    public Page copyForUndo() {
        Page us = new Page(this.strVarPrefix, this.strVarPostfix);
        us.title = this.title;
        us.strSpreadsheetColumnWidths = this.strSpreadsheetColumnWidths;

        copyArray(this.properties, us.properties);
        copyArray(this.propertiesAnimation, us.propertiesAnimation);
        copyArray(this.spreadsheetData, us.spreadsheetData);
        copyArray(this.prevSpreadsheetData, us.prevSpreadsheetData);
        us.onEntryMacro.repeat = this.onEntryMacro.repeat;
        us.onExitMacro.repeat = this.onExitMacro.repeat;
        copyArray(this.onEntryMacro.actions, us.onEntryMacro.actions);
        copyArray(this.onExitMacro.actions, us.onExitMacro.actions);

        for (EventMacro eventMacro : keyboardProcessor.keyboardEventMacros) {
            us.keyboardProcessor.keyboardEventMacros.add(new KeyboardEventMacro(eventMacro));
        }
        for (EventMacro eventMacro : variableUpdateEventMacros) {
            us.variableUpdateEventMacros.add(new VariableUpdateEventMacro(eventMacro));
        }

        return us;
    }

    private void copyEventMacro(EventMacro eventMacro1, EventMacro eventMacro2) {
        eventMacro2.setEventName(eventMacro1.getEventName());
        copyArray(eventMacro1.getMacro().actions, eventMacro2.getMacro().actions);
    }

    public String[][] getSpreadsheetsData() {
        return this.spreadsheetData;
    }

    public void setPropertiesFromSketch(Page s) {
        this.title = s.title;
        this.strSpreadsheetColumnWidths = s.strSpreadsheetColumnWidths;

        copyArray(s.properties, this.properties);
        copyArray(s.propertiesAnimation, this.propertiesAnimation);
        copyArray(s.spreadsheetData, this.spreadsheetData);
        copyArray(s.prevSpreadsheetData, this.prevSpreadsheetData);
        for (EventMacro eventMacro : keyboardProcessor.keyboardEventMacros) {
            s.keyboardProcessor.keyboardEventMacros.add(new KeyboardEventMacro(eventMacro));
        }

        for (EventMacro eventMacro : variableUpdateEventMacros) {
            s.variableUpdateEventMacros.add(new VariableUpdateEventMacro(eventMacro));
        }
        this.onEntryMacro.repeat = s.onEntryMacro.repeat;
        this.onExitMacro.repeat = s.onExitMacro.repeat;
        copyArray(s.onEntryMacro.actions, this.onEntryMacro.actions);
        copyArray(s.onExitMacro.actions, this.onExitMacro.actions);
    }

    public static <T> void copyArray(T source[][], T target[][]) {
        for (int i = 0; i < source.length; i++) {
            for (int j = 0; j < source[i].length; j++) {
                target[i][j] = source[i][j];
            }
        }
    }

    public Connector selectConnector(int x, int y) {
        this.selectedConnector = null;
        for (Connector c : this.connectors) {
            int x1 = c.getRegion1().getCenterX(false);
            int y1 = c.getRegion1().getCenterY(false);
            int x2 = c.getRegion2().getCenterX(false);
            int y2 = c.getRegion2().getCenterY(false);
            double d = DistancePointSegment.distanceToSegment(x, y, x1, y1, x2, y2);
            if (d < 6) {
                this.selectedConnector = c;
                break;
            } else if (c.renderer != null && c.renderer.textRect != null && c.renderer.textRect.contains(x, y)) {
                this.selectedConnector = c;
                break;
            }
        }

        return this.selectedConnector;
    }

    public void addConnector(Connector connector) {
        this.connectors.add(connector);
    }

    public void updateConnectors(boolean bPlayback) {
        for (Connector c : this.connectors) {
            c.updateVariables(bPlayback);
        }
    }

    public void updateConnectors(ActiveRegion r, boolean bPlayback) {
        for (Connector c : this.connectors) {
            if (c.getRegion1() == r || c.getRegion2() == r) {
                c.updateVariables(bPlayback);
            }
        }
    }

    public void removeConnector(Connector connector) {
        this.connectors.remove(connector);
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
        return this.spreadsheetData[row][col].isEmpty();
    }

    public String getSpreadsheetCellValue(int row, int col) {
        if (row >= 0 && row < this.spreadsheetData.length && col >= 0 && col < this.spreadsheetData[row].length) {
            String value = this.getSpreadsheetData()[row][col];
            return value == null ? "" : value;
        }

        return "";
    }

    public void updateSpreadsheetCell(int row, int col, String value) {
        if (row >= 0 && row < this.spreadsheetData.length && col >= 0 && col < this.spreadsheetData[row].length) {
            this.spreadsheetData[row][col] = value;
        }
    }

    public Page cloneEmbedded(String strVarPrefix, String strVarPostfix) {
        Page s = new Page(strVarPrefix, strVarPostfix);
        s.strSourceDirectory = this.strSourceDirectory;
        s.strVarPrefix = strVarPrefix;
        s.strVarPostfix = strVarPostfix;
        s.regions = this.regions.cloneEmbedded(s);

        s.title = title;
        s.strTextAnnotation = this.strTextAnnotation;
        s.onEntryMacro = new Macro(this.onEntryMacro);
        s.onExitMacro = new Macro(this.onExitMacro);
        s.initCache();

        return s;
    }

    public Page(Page s, String strSourceDirectory, String strVarPrefix, String strVarPostfix) {
        this(strVarPrefix, strVarPostfix);
        this.strSourceDirectory = strSourceDirectory;
        this.regions = new ActiveRegions(s.regions, s);
        this.title = "" + s.title;
        this.strTextAnnotation = "" + s.strTextAnnotation;
        this.strTextAnnotation = s.strTextAnnotation;
        for (EventMacro eventMacro : s.keyboardProcessor.keyboardEventMacros) {
            this.keyboardProcessor.keyboardEventMacros.add(new KeyboardEventMacro(eventMacro));
        }
        for (EventMacro eventMacro : s.variableUpdateEventMacros) {
            this.variableUpdateEventMacros.add(new VariableUpdateEventMacro(eventMacro));
        }
        this.onEntryMacro = new Macro(s.onEntryMacro);
        this.onExitMacro = new Macro(s.onExitMacro);
        for (int i = 0; i < Page.NUMBER_OF_LAYERS; i++) {
            String strFileName;
            if (i == 0) {
                strFileName = "Sketch_" + this.getId() + ".png";
            } else {
                strFileName = "Sketch_" + this.getId() + "_" + (i + 1) + ".png";
            }
            File file = new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + strFileName);
            if (file.exists()) {
                File newFile = new File(strSourceDirectory + strFileName);
                FileUtils.copyFile(file, newFile);
            }
        }
    }

    public void flush() {
        for (ActiveRegion a : this.regions.regions) {
            a.flush();
        }
    }

    public void initRegionImages() {
        for (ActiveRegion a : this.regions.regions) {
            a.initImages();
        }
    }

    public void dispose() {
        id = null;
        stateDiagramGraph = null;
        stateDiagramCell = null;
        onEntryMacro = null;
        onExitMacro = null;
        onEntryMacroThread = null;
        onExitMacroThread = null;
        if (keyboardProcessor != null) {
            keyboardProcessor.dispose();
            keyboardProcessor = null;
        }
        if (variableUpdateEventMacros != null) {
            for (EventMacro eventMacro : variableUpdateEventMacros) {
                eventMacro.dispose();
            }
            variableUpdateEventMacros = null;
        }
        activeTimers = null;
        activeMacros = null;
        strTextAnnotation = null;
        strSourceDirectory = null;
        strVarPrefix = null;
        strVarPostfix = null;
        notes = null;
        //variablesMappingHandler = null;
        properties = null;
        propertiesLimits = null;
        propertiesAnimation = null;
        animatePropertiesThread = null;
        this.strCache = null;

        if (regions != null && regions.regions != null) {
            for (ActiveRegion a : this.regions.regions) {
                a.dispose();
            }
            this.regions.regions.removeAllElements();
        }

        regions = null;
    }

    public Vector<File> getImageFiles() {
        Vector<File> files = new Vector<File>();
        for (int i = 0; i < Page.NUMBER_OF_LAYERS; i++) {
            File file = this.getLayerImageFile(i);
            if (file.exists()) {
                files.add(file);
            }
        }
        for (ActiveRegion a : this.regions.regions) {
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
        return layer >= 0 && this.layerActive[layer];
    }

    public boolean bLayerRegions = true;

    public boolean isRegionsLayerActive() {
        return this.bLayerRegions;
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
        perspective_horizont_x1 = 500.0;
        perspective_horizont_x2 = 1000.0;
        perspective_horizont_y = 500.0;
        zoom = 1.0;
        zoomCenterX = 0.0;
        zoomCenterY = 0.0;
        try {
            if (!getPropertyValue("perspective x1").isEmpty()) {
                this.perspective_horizont_x1 = Double.parseDouble(getPropertyValue("perspective x1"));
            }
        } catch (Throwable e) {
        }
        try {
            if (!getPropertyValue("perspective x2").isEmpty()) {
                this.perspective_horizont_x2 = Double.parseDouble(getPropertyValue("perspective x2"));
            }
        } catch (Throwable e) {
        }
        try {
            if (!getPropertyValue("perspective y").isEmpty()) {
                this.perspective_horizont_y = Double.parseDouble(getPropertyValue("perspective y"));
            }
        } catch (Throwable e) {
        }
        try {
            if (!getPropertyValue("zoom").isEmpty()) {
                this.zoom = Double.parseDouble(getPropertyValue("zoom"));
            }
        } catch (Throwable e) {
        }
        try {
            if (!getPropertyValue("zoom center x").isEmpty()) {
                this.zoomCenterX = Double.parseDouble(getPropertyValue("zoom center x"));
            }
        } catch (Throwable e) {
        }
        try {
            if (!getPropertyValue("zoom center y").isEmpty()) {
                this.zoomCenterY = Double.parseDouble(getPropertyValue("zoom center y"));
            }
        } catch (Throwable e) {
        }
    }

    Vector<String> updatingProperties = new Vector<String>();

    public void logPropertyUpdate(String name, String value, Component source) {
        TutorialPanel.addLine("cmd", "Set the page property: " + name + "=" + value, "details.gif", source);
    }

    public void setProperty(String name, String value) {
        if (updatingProperties.contains(name)) {
            return;
        }
        updatingProperties.add(name);
        for (int i = 0; i < this.properties.length; i++) {
            if (properties[i][1] != null && ((String) properties[i][0]).equalsIgnoreCase(name)) {
                properties[i][1] = value;
                //this.variablesMappingHandler.variableUpdated("@property:" + name, value);
                updatingProperties.remove(name);
                return;
            }
        }
        updatingProperties.remove(name);
    }

    public String getProperty(String name) {
        for (int i = 0; i < this.properties.length; i++) {
            if (((String) properties[i][0]).equalsIgnoreCase(name) && properties[i][1] != null) {
                String strValue = properties[i][1];
                if (strValue.isEmpty() && name.equalsIgnoreCase("zoom")) {
                    strValue = SketchletEditor.editorPanel.scale + "";
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
                strValue = Evaluator.processText(strValue, strVarPrefix, strVarPostfix);
            }
        }
        return strValue;
    }

    public JComboBox getPropertiesCombo() {
        JComboBox combo = new JComboBox();
        combo.setEditable(true);
        combo.addItem("");

        for (int i = 2; i < this.properties.length; i++) {
            if (properties[i][1] != null) {
                combo.addItem(properties[i][0]);
            }
        }

        return combo;
    }

    public int getPropertiesCount() {
        return this.properties.length;
    }

    public String[][] getData() {
        return this.properties;
    }

    public String getProperty(int index) {
        return Evaluator.processText((String) properties[index][1], "", "");
    }

    public int getPropertyRow(String strProperty) {
        for (int i = 0; i < properties.length; i++) {
            if (properties[i][1] != null && properties[i][0].equalsIgnoreCase(strProperty)) {
                return i;
            }
        }
        return -1;
    }

    public String getPropertyDescription(String property) {
        for (int i = 0; i < properties.length; i++) {
            if (properties[i][1] != null && properties[i][0].equalsIgnoreCase(property)) {
                return properties[i][2];
            }
        }
        return "";
    }

    public void repaintProperties() {
        if (SketchletEditor.editorPanel != null) {
            SketchletEditor.editorPanel.repaint();
        }
        if (SketchletEditor.editorPanel.internalPlaybackPanel != null) {
            SketchletEditor.editorPanel.internalPlaybackPanel.repaint();
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

    String previousVersion = "";
    boolean deleted = false;

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

            for (Connector connector : this.connectors) {
                connector.dispose();
            }
            this.connectors.clear();

            for (ActiveRegion region : this.regions.regions) {
                for (int i = 0; i < region.getImageCount(); i++) {
                    ImageCache.remove(new File(region.getDrawImagePath(i)));
                }

                region.dispose();
            }
        } catch (Throwable e) {
            log.error(e);
        }
    }

    public static boolean bSaveToHistory = false;

    String strCache = "";

    public void initCache() {
        strCache = getSketchFileString();
    }

    boolean bSaving = false;

    public boolean saveSketchFile(String strFile, boolean bSaveImageThumb) {
        try {
            bSaving = true;
            if (images != null) {
                for (int i = 0; images != null && i < images.length; i++) {
                    if (imageUpdated[i] || bSaveImageThumb) {
                        try {
                            if (imageUpdated[i]) {
                                File file = getLayerImageFile(i);
                                if (images[i] != null) {
                                    ImageCache.write(images[i], file);
                                }
                            }
                            if (i == 0 && images != null && images[0] != null) {
                                BufferedImage thumb = Workspace.createCompatibleImage(images[0].getWidth() / 6, images[0].getHeight() / 6);
                                Graphics2D g2 = thumb.createGraphics();
                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                g2.scale(1.0 / 6, 1.0 / 6);
                                g2.drawImage(images[0], 0, 0, null);
                                regions.draw(g2, SketchletEditor.editorPanel, SketchletEditor.editorPanel.mode, false, false, 1.0f);
                                g2.dispose();
                                File fileThumb = new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "Sketch_" + getId() + "_thumbnail.png");
                                ImageIO.write(thumb, "png", fileThumb);
                            }

                            imageUpdated[i] = false;
                        } catch (IOException e) {
                            log.error(e);
                        }
                    }
                }
            }
            FileWriter fw = new FileWriter(strFile);
            PrintWriter outSketch = new PrintWriter(fw);
            saveSketchFile(outSketch);
            fw.close();

            bSaving = false;
            return true;
        } catch (Throwable e) {
            log.error(e);
        }

        bSaving = false;
        return false;
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

    public void saveSketchFile(PrintWriter outSketch) {
        try {
            outSketch.println("<?xml version='1.0' encoding='UTF-8'?>");
            outSketch.println("<page>");
            outSketch.println("<title>" + this.title + "</title>");
            outSketch.println("<id>" + this.getId() + "</id>");
            outSketch.println("<state-diagram-x>" + this.stateDiagramX + "</state-diagram-x>");
            outSketch.println("<state-diagram-y>" + this.stateDiagramY + "</state-diagram-y>");
            outSketch.println("<page-width>" + this.pageWidth + "</page-width>");
            outSketch.println("<page-height>" + this.pageHeight + "</page-height>");
            outSketch.println("<page-layers>");

            for (int i = 0; i < this.layerActive.length; i++) {
                outSketch.println("    <page-layer active='" + layerActive[i] + "'/>");
            }

            outSketch.println("</page-layers>");

            outSketch.println("<page-variables>");

            for (LocalVariable localVariable : this.localVariables) {
                outSketch.println("    <page-variable name='" + localVariable.getName() + "' format='" + localVariable.getFormat() + "'><![CDATA[" + localVariable.getValue() + "]]></page-variable>");
            }

            outSketch.println("</page-variables>");

            outSketch.println("<page-parameters>");

            if (properties != null) {
                for (int i = 0; i < this.properties.length; i++) {
                    if (properties[i][1] == null || properties[i][1].toString().isEmpty()) {
                        continue;
                    }

                    outSketch.println("    <page-property name='" + properties[i][0] + "'><![CDATA[" + properties[i][1] + "]]></page-property>");
                }
            }
            outSketch.println("</page-parameters>");

            outSketch.println("<page-spreadsheet>");
            if (!strSpreadsheetColumnWidths.isEmpty()) {
                outSketch.println("    <spreadsheet-column-widths>" + XMLUtils.prepareForXML(strSpreadsheetColumnWidths) + "</spreadsheet-column-widths>");
            }
            if (spreadsheetData != null) {
                for (int i = 0; i < this.spreadsheetData.length; i++) {
                    for (int j = 0; j < this.spreadsheetData[i].length; j++) {
                        String strCell = spreadsheetData[i][j];
                        if (!strCell.isEmpty()) {
                            outSketch.println("    <spreadsheet-cell x='" + i + "' y='" + j + "'>" + XMLUtils.prepareForXML(strCell) + "</spreadsheet-cell>");
                        }
                    }
                }
            }
            outSketch.println("</page-spreadsheet>");

            outSketch.println("<animate-page-parameters>");

            for (int i = 0; i < this.propertiesAnimation.length; i++) {
                if (propertiesAnimation[i][1] == null) {
                    continue;
                }
                boolean bAdd = false;
                for (int j = 1; j < propertiesAnimation[i].length; j++) {
                    if (!propertiesAnimation[i][j].isEmpty()) {
                        bAdd = true;
                        break;
                    }
                }
                if (!bAdd) {
                    continue;
                }
                outSketch.println("    <animate-page-property name='" + propertiesAnimation[i][0] + "' type='" + propertiesAnimation[i][1] + "' start = '" + propertiesAnimation[i][2] + "' end = '" + propertiesAnimation[i][3] + "' duration='" + propertiesAnimation[i][4] + "' curve='" + propertiesAnimation[i][5] + "'/>");
            }

            outSketch.println("</animate-page-parameters>");

            outSketch.println("<text-annotation>" + XMLUtils.prepareForXML(strTextAnnotation) + "</text-annotation>");

            regions.save(outSketch);

            this.onEntryMacro.save(outSketch, "on-entry");
            this.onExitMacro.save(outSketch, "on-exit");
            outSketch.println("<keyboard-event-actions>");
            for (KeyboardEventMacro keyboardEventMacro : keyboardProcessor.keyboardEventMacros) {
                keyboardEventMacro.getMacro().name = keyboardEventMacro.getEventName();
                keyboardEventMacro.getMacro().saveSimple(outSketch, "keyboard-event-action", "    ");
            }
            outSketch.println("</keyboard-event-actions>");
            outSketch.println("<mouse-event-actions>");
            for (MouseEventMacro mouseEventMacro : mouseProcessor.mouseEventMacros) {
                mouseEventMacro.getMacro().name = mouseEventMacro.getEventName();
                mouseEventMacro.getMacro().saveSimple(outSketch, "mouse-event-action", "    ");
            }
            outSketch.println("</mouse-event-actions>");
            outSketch.println("<variable-update-event-actions>");
            for (VariableUpdateEventMacro variableUpdateEventMacro : variableUpdateEventMacros) {
                variableUpdateEventMacro.getMacro().name = variableUpdateEventMacro.getEventName();
                variableUpdateEventMacro.getMacro().saveSimple(outSketch, "variable-update-event-action", "    ");
            }
            outSketch.println("</variable-update-event-actions>");

            outSketch.println("<connectors>");
            for (Connector connector : this.connectors) {
                outSketch.println(connector.toXML("    "));
            }
            outSketch.println("</connectors>");

            outSketch.println("<post-notes>");
            for (NoteDialog note : notes) {
                int x = note.getX();
                int y = note.getY();
                int s = note.isMinimized ? 1 : 0;

                int w = note.isMinimized ? note.original_w : note.getWidth();
                int h = note.isMinimized ? note.original_h : note.getHeight();

                outSketch.print("    <post-note x='" + x + "' y='" + y + "' w='" + w + "' h='" + h + "' state='" + s + "'>");
                outSketch.print(XMLUtils.prepareForXML(note.noteTextArea.getText()));
                outSketch.println("</post-note>");
            }
            outSketch.println("</post-notes>");

            outSketch.println("</page>");
            outSketch.flush();
            outSketch.close();
        } catch (Throwable e) {
            log.error(e);
        }
    }

    public boolean isConnectedTo(Page s) {
        for (ActiveRegion region : regions.regions) {
            for (MouseEventMacro mouseEventMacro : region.mouseProcessor.mouseEventMacros) {
                for (int i = 0; i < mouseEventMacro.getMacro().actions.length; i++) {
                    String action = (String) mouseEventMacro.getMacro().actions[i][0];
                    String param = (String) mouseEventMacro.getMacro().actions[i][1];
                    if (action.equalsIgnoreCase("Go to page") && param.equalsIgnoreCase(s.title)) {
                        return true;
                    } else if ((action.equalsIgnoreCase("Call Macro") || action.equalsIgnoreCase("Start Action")) && Macros.globalMacros.isConnected(param, s.title)) {
                        return true;
                    }
                }
            }
        }
        for (ActiveRegion a : regions.regions) {
            for (RegionOverlapEventMacro regionOverlapEventMacro : a.regionOverlapEventMacros) {
                for (int i = 0; i < regionOverlapEventMacro.getMacro().actions.length; i++) {
                    String action = (String) regionOverlapEventMacro.getMacro().actions[i][0];
                    String param = (String) regionOverlapEventMacro.getMacro().actions[i][1];
                    if (action.equalsIgnoreCase("Go to page") && param.equalsIgnoreCase(s.title)) {
                        return true;
                    } else if ((action.equalsIgnoreCase("Call Macro") || action.equalsIgnoreCase("Start Action")) && Macros.globalMacros.isConnected(param, s.title)) {
                        return true;
                    }
                }
            }
        }

        if (isMacroConnected(onEntryMacro, s)) {
            return true;
        }

        if (isMacroConnected(onExitMacro, s)) {
            return true;
        }
        for (KeyboardEventMacro keyboardEventMacro : keyboardProcessor.keyboardEventMacros) {
            if (isMacroConnected(keyboardEventMacro.getMacro(), s)) {
                return true;
            }
        }

        return false;
    }

    private boolean isMacroConnected(Macro macro, Page page) {
        for (int i = 0; i < macro.actions.length; i++) {
            String action = (String) macro.actions[i][0];
            String param = (String) macro.actions[i][1];
            if (action.equalsIgnoreCase("Go to page") && param.equalsIgnoreCase(page.title)) {
                return true;
            } else if ((action.equalsIgnoreCase("Call Macro") || action.equalsIgnoreCase("Start Action")) && Macros.globalMacros.isConnected(param, page.title)) {
                return true;
            }
        }

        return false;
    }

    public Set<String> getConnections(Page s) {
        Set<String> connections = new HashSet<String>();
        for (ActiveRegion region : regions.regions) {
            for (MouseEventMacro mouseEventMacro : region.mouseProcessor.mouseEventMacros) {
                for (int i = 0; i < mouseEventMacro.getMacro().actions.length; i++) {
                    String event = "on region '" + region.getName() + "' " + (String) mouseEventMacro.getEventName() + "";
                    String action = (String) mouseEventMacro.getMacro().actions[i][0];
                    String param = (String) mouseEventMacro.getMacro().actions[i][1];
                    if (action.equalsIgnoreCase("Go to page") && param.equalsIgnoreCase(s.title)) {
                        connections.add(event);
                    } else if ((action.equalsIgnoreCase("Call Macro") || action.equalsIgnoreCase("Start Action")) && Macros.globalMacros.isConnected(param, s.title)) {
                        connections.add(event + ", macro(" + param + ")");
                    }
                }
            }

            for (RegionOverlapEventMacro regionOverlapEventMacro : region.regionOverlapEventMacros) {
                for (int i = 0; i < regionOverlapEventMacro.getMacro().actions.length; i++) {
                    String event = "overlap event";
                    String action = (String) regionOverlapEventMacro.getMacro().actions[i][0];
                    String param = (String) regionOverlapEventMacro.getMacro().actions[i][1];
                    if (action.equalsIgnoreCase("Go to page") && param.equalsIgnoreCase(s.title)) {
                        connections.add(event);
                    } else if ((action.equalsIgnoreCase("Call Macro") || action.equalsIgnoreCase("Start Action")) && Macros.globalMacros.isConnected(param, s.title)) {
                        connections.add(event + ", macro(" + param + ")");
                    }
                }
            }
        }

        for (int i = 0; i < this.onEntryMacro.actions.length; i++) {
            String action = (String) this.onEntryMacro.actions[i][0];
            String param = (String) this.onEntryMacro.actions[i][1];
            if (action.equalsIgnoreCase("Go to page") && param.equalsIgnoreCase(s.title)) {
                connections.add("on page entry");
            } else if ((action.equalsIgnoreCase("Call Macro") || action.equalsIgnoreCase("Start Action")) && Macros.globalMacros.isConnected(param, s.title)) {
                connections.add("on page entry, macro(" + param + ")");
            }
        }

        for (int i = 0; i < this.onExitMacro.actions.length; i++) {
            String action = (String) this.onExitMacro.actions[i][0];
            String param = (String) this.onExitMacro.actions[i][1];
            if (action.equalsIgnoreCase("Go to page") && param.equalsIgnoreCase(s.title)) {
                connections.add("on page exit");
            } else if ((action.equalsIgnoreCase("Call Macro") || action.equalsIgnoreCase("Start Action")) && Macros.globalMacros.isConnected(param, s.title)) {
                connections.add("on page exit, macro(" + param + ")");
            }
        }

        for (KeyboardEventMacro keyboardEventMacro : keyboardProcessor.keyboardEventMacros) {
            String key = keyboardEventMacro.getKey();
            Object actions[][] = keyboardEventMacro.getMacro().actions;
            for (int i = 0; i < actions.length; i++) {
                String action = (String) actions[i][0];
                String param = (String) actions[i][1];
                if (action.equalsIgnoreCase("Go to page") && param.equalsIgnoreCase(s.title)) {
                    connections.add("when " + keyboardEventMacro.getModifiers() + key + " " + action);
                } else if ((action.equalsIgnoreCase("Call Macro") || action.equalsIgnoreCase("Start Action")) && Macros.globalMacros.isConnected(param, s.title)) {
                    connections.add("when " + keyboardEventMacro.getModifiers() + key + " " + action + ", macro(" + param + ")");
                }
            }
        }

        for (VariableUpdateEventMacro variableUpdateEventMacro : variableUpdateEventMacros) {
            String var = variableUpdateEventMacro.getVariable();
            String op = variableUpdateEventMacro.getOperator();
            String value = variableUpdateEventMacro.getValue();
            Object actions[][] = variableUpdateEventMacro.getMacro().actions;
            for (int i = 0; i < actions.length; i++) {
                String action = (String) actions[i][0];
                String param = (String) actions[i][1];
                if (action.equalsIgnoreCase("Go to page") && param.equalsIgnoreCase(s.title)) {
                    connections.add("when " + var + " " + op + " " + value);
                } else if ((action.equalsIgnoreCase("Call Macro") || action.equalsIgnoreCase("Start Action")) && Macros.globalMacros.isConnected(param, s.title)) {
                    connections.add("when " + var + " " + op + " " + value + ", macro(" + param + ")");
                }
            }
        }

        return connections;
    }

    public void deactivate(boolean bPlayback) {
        try {
            remoteX = -1;
            remoteY = -1;

            //DataServer.variablesServer.removeNotifyChangeClient(variablesMappingHandler);

            RefreshScreenCaptureThread.stop();

            if (this.animatePropertiesThread != null) {
                this.animatePropertiesThread.stopped = true;
                this.animatePropertiesThread = null;
            }
            if (onEntryMacroThread != null) {
                onEntryMacroThread.stop();
            }
            if (bPlayback && onExitMacro != null) {
                onExitMacroThread = onExitMacro.startThread("", strVarPrefix, strVarPostfix);
                try {
                    while (!onExitMacroThread.isStopped()) {
                        Thread.sleep(10);
                    }
                } catch (Throwable e) {
                }
            } else if (onExitMacroThread != null) {
                onExitMacroThread.stop();
            }

            if (activeTimers != null) {
                Object activeTimersArray[] = activeTimers.toArray();
                for (int i = 0; i < activeTimersArray.length; i++) {
                    TimerThread tt = (TimerThread) activeTimersArray[i];
                    if (tt != null) {
                        tt.stop();
                    }
                }
                activeTimers.removeAllElements();
            }

            if (activeMacros != null) {
                Object activeMacrosArray[] = activeMacros.toArray();
                for (int i = 0; i < activeMacrosArray.length; i++) {
                    RunInterface mt = (RunInterface) activeMacrosArray[i];
                    if (mt != null) {
                        mt.stop();
                    }
                }
                activeMacros.removeAllElements();
            }

            if (regions != null && regions.regions != null) {
                for (ActiveRegion a : regions.regions) {
                    a.deactivate(bPlayback);
                }
            }

            while (bSaving) {
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                }
            }

            if (this.images != null) {
                synchronized (this.images) {
                    for (int i = 0; i < this.images.length; i++) {
                        if (this.images[i] != null) {
                            this.images[i].flush();
                            this.images[i] = null;
                        }
                    }
                    this.images = null;
                }
            }

            // SketchStateFrame.hideProperties();

            if (!bPlayback && notes != null) {
                for (NoteDialog note : notes) {
                    SketchletEditor.editorPanel.removeNote(note);
                }
            }

        } catch (Throwable e) {
        }
        ApplicationLifecycleCentre.beforePageExit(this);
    }

    public void play() {
        for (ActiveRegion a : regions.regions) {
            a.play();
        }
    }

    private boolean bActivating = false;

    public void activate(boolean bPlayback) {
        if (bActivating) {
            return;
        }

        bActivating = true;
        this.initImages();
        //this.variablesMappingHandler.refreshPropertiesFromVariables();
        if (bPlayback) {
            play();
            if (onEntryMacro != null) {
                onEntryMacroThread = onEntryMacro.startThread("", strVarPrefix, strVarPostfix);
            }
            if (this.animatePropertiesThread == null) {
                this.animatePropertiesThread = new AnimatePropertiesThread(this);
            }
        } else {
            regions.refreshFromVariables();
        }
        if (stateDiagramGraph != null && stateDiagramCell != null) {
            stateDiagramGraph.setSelectionCell(stateDiagramCell);
        }

        this.calculateHorizonPoint();

        for (ActiveRegion a : regions.regions) {
            a.activate(bPlayback);
        }

        if (!bPlayback) {
            Object notesArray[] = notes.toArray();
            for (int i = 0; i < notesArray.length; i++) {
                NoteDialog note = (NoteDialog) notesArray[i];
                SketchletEditor.editorPanel.addAndShowNote(note);
            }
        }

        bActivating = false;
        RefreshTime.update();
        ApplicationLifecycleCentre.afterPageEntry(this);
    }


    public void setAnimateProperty(String property, String type, String start, String end, String duration, String curve) {
        for (int i = 0; i < this.propertiesAnimation.length; i++) {
            if (propertiesAnimation[i][1] != null && propertiesAnimation[i][0].equals(property)) {
                propertiesAnimation[i][1] = type;
                propertiesAnimation[i][2] = start;
                propertiesAnimation[i][3] = end;
                propertiesAnimation[i][4] = duration;
                propertiesAnimation[i][5] = curve;
                return;
            }
        }
    }

    public String getDefaultValue(String strProperty) {
        for (int i = 0; i < this.propertiesLimits.length; i++) {
            if (this.propertiesLimits[i][0].equalsIgnoreCase(strProperty)) {
                return propertiesLimits[i][3];
            }
        }

        return null;
    }

    public String getMinValue(String strProperty) {
        for (int i = 0; i < this.propertiesLimits.length; i++) {
            if (this.propertiesLimits[i][0].equalsIgnoreCase(strProperty)) {
                return propertiesLimits[i][1];
            }
        }

        return null;
    }

    public String getMaxValue(String strProperty) {
        for (int i = 0; i < this.propertiesLimits.length; i++) {
            if (this.propertiesLimits[i][0].equalsIgnoreCase(strProperty)) {
                return propertiesLimits[i][2];
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
                    return this.layerActive[layerIndex];
                } else {
                    return !strLayer.equalsIgnoreCase("false");
                }
            } else {
                String strLayer = this.getPropertyValue("layer " + (layerIndex + 1));
                strActiveLayers = "," + strActiveLayers.replace(" ", "") + ",";
                return strActiveLayers.contains("," + (layerIndex + 1) + ",") || strLayer.equalsIgnoreCase("true");
            }
        } else {
            bLayerActive = this.layerActive[layerIndex];
        }
        return bLayerActive;
    }

    public String getTitle() {
        return this.title;
    }

    public void initImages() {
        this.images = new BufferedImage[Page.NUMBER_OF_LAYERS];
        for (int i = 0; i < images.length; i++) {
            initImage(i);
        }
    }

    public void initImage(int _layer) {
        initImage(_layer, false);
    }

    public void initImage(int _layer, boolean bForceRead) {
        this.imageUpdated[_layer] = false;
        try {
            File file = this.getLayerImageFile(_layer);
            if (file != null && file.exists()) {
                BufferedImage image = ImageCache.read(file, images[_layer], bForceRead);
                images[_layer] = image;
            } else {
                images[_layer] = null;
            }
        } catch (Throwable e) {
            images[_layer] = null;
            log.error(e);
        }
    }
}
