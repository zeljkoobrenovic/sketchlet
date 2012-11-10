/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.data;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.designer.editor.regions.connector.Connector;
import net.sf.sketchlet.designer.editor.tool.notes.NoteDialog;
import net.sf.sketchlet.designer.programming.macros.Macro;
import net.sf.sketchlet.designer.programming.macros.MacroSaxParserUtil;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

public class SketchletsSaxLoader extends DefaultHandler {
    private static final Logger log = Logger.getLogger(SketchletsSaxLoader.class);

    public SketchletsSaxLoader() {
    }

    public static Vector<String> getSketchFiles(String strDirectory) {
        String[] files = new File(strDirectory).list();
        Vector<String> sketchFiles = new Vector<String>();
        Vector<String> sortedFiles = new Vector<String>();

        if (files != null) {
            File sortFile = new File(strDirectory + "sort.txt");
            if (sortFile.exists()) {
                try {
                    BufferedReader in = new BufferedReader(new FileReader(sortFile));
                    String line;
                    while ((line = in.readLine()) != null) {
                        if (new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + line).exists()) {
                            sortedFiles.add(line);
                        }
                    }

                    in.close();
                } catch (Throwable e) {
                    log.error(e);
                }
            }
            for (int i = 0; i < files.length; i++) {
                String fileName = files[i];

                if (fileName.startsWith("sketch_") && fileName.endsWith(".xml") && sortedFiles.indexOf(fileName) < 0) {
                    sketchFiles.add(fileName);
                }
            }
            Collections.sort(sketchFiles);
            sortedFiles.addAll(sketchFiles);
        }

        return sortedFiles;
    }

    public static Vector<Page> getSketchesFromDir(String strDirectory) {
        try {
            Vector<String> sketchFiles = getSketchFiles(strDirectory);
            Vector<Page> vectorPages = new Vector<Page>();

            for (String strFile : sketchFiles) {
                long t = System.currentTimeMillis();
                if (new File(strDirectory + strFile).exists()) {
                    Page s = getSketch(strDirectory + strFile);
                    if (s != null) {
                        vectorPages.add(s);
                    }
                }
            }

            return vectorPages;
        } catch (Throwable e) {
            log.error(e);
        }
        return null;
    }

    public static Vector<Page> getSketches(String strFile) {
        SketchletsSaxLoader handler = new SketchletsSaxLoader();
        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);

            FileInputStream r = new FileInputStream(strFile);
            xr.parse(new InputSource(r));
            r.close();
        } catch (Throwable e) {
            log.error("ERROR in the XML file '" + strFile + "'", e);
        }
        return handler.pages;
    }

    public static Page getSketch(String strFile) {
        try {
            Vector<Page> ss = getSketches(strFile);
            if (ss != null) {
                return ss.elementAt(0);
            }
        } catch (Throwable e) {
            log.error(e);
        }
        return null;
    }

    private Vector<Page> pages = new Vector<Page>();
    private Page currentPage;
    private ActiveRegion currentRegion;
    private Connector currentConnector;
    private String currentConnectorProperty = "";
    private String currentElement;
    private Macro onEntry;
    private Macro onExit;
    private int onEntryActionIndex = -1;
    private int onExitActionIndex = -1;
    private int interactionEventIndex = -1;
    private int updateIndex = -1;
    private int limitIndex = -1;
    private boolean withinEvent = false;
    private String lastSketchProperty = "";
    private String lastSketchVariable = "";
    private String lastSketchFormat = "";
    private int layerIndex = -1;

    private MacroSaxParserUtil currentMacroSaxUtil = null;


    public void startDocument() {
        pages = new Vector<Page>();
    }

    public void endDocument() {
        for (Page s : pages) {
            s.initCache();
        }
    }

    java.util.List<String> pathElements = new ArrayList<String>();

    public String path() {
        String path = "";

        for (String pathElement : pathElements) {
            path += "/" + pathElement;
        }

        return path;
    }

    public void startElement(String uri, String name, String qName, Attributes atts) {
        String strElem = "";
        if ("".equals(uri)) {
            strElem = qName;
        } else {
            strElem = name;
        }

        pathElements.add(strElem);

        currentElement = strElem;
        strCharacters = "";
        if (path().equalsIgnoreCase("/sketch/action/region-widget-event-actions/widget-event-action")) {
            currentRegion.widgetEventMacros.add(new WidgetEventMacro(""));
            indexOfWidgetAction = -1;
            return;
        } else if (path().equalsIgnoreCase("/sketch/action/region-widget-event-actions/widget-event-action/action")) {
            indexOfWidgetAction++;
            return;
        } else if (path().equalsIgnoreCase("/sketch/action/region-mouse-event-actions/mouse-event-action")) {
            return;
        } else if (path().equalsIgnoreCase("/sketch/action/region-overlap-event-actions/region-overlap-event-action")) {
            return;
        } else if (currentMacroSaxUtil != null && currentMacroSaxUtil.startElement(path(), atts)) {
            return;
        }

        if (strElem.equalsIgnoreCase("sketch")) {
            currentPage = new Page("", "");
            currentPage.title = "";
            currentPage.regions = new ActiveRegions(currentPage);
            pages.add(currentPage);
        } else if (strElem.equalsIgnoreCase("on-entry") && currentPage != null) {
            currentPage.onEntryMacro = new Macro();
            onEntry = currentPage.onEntryMacro;
            onEntryActionIndex = -1;

            for (int i = 0; i < onEntry.actions.length; i++) {
                onEntry.actions[i][0] = "";
                onEntry.actions[i][1] = "";
                onEntry.actions[i][2] = "";
            }
        } else if (strElem.equalsIgnoreCase("on-exit") && currentPage != null) {
            currentPage.onExitMacro = new Macro();
            onExit = currentPage.onExitMacro;
            onExitActionIndex = -1;
            for (int i = 0; i < onExit.actions.length; i++) {
                onExit.actions[i][0] = "";
                onExit.actions[i][1] = "";
                onExit.actions[i][2] = "";
            }
        } else if (strElem.equalsIgnoreCase("repeat")) {
        } else if (strElem.equalsIgnoreCase("sketch-property")) {
            lastSketchProperty = atts.getValue("name");
        } else if (strElem.equalsIgnoreCase("page-variable")) {
            lastSketchVariable = atts.getValue("name");
            lastSketchFormat = atts.getValue("format");
        } else if (strElem.equalsIgnoreCase("sketch-layer")) {
            layerIndex++;
            try {
                currentPage.layerActive[layerIndex] = atts.getValue("active").equalsIgnoreCase("true");
            } catch (Exception e) {
                log.error(e);
            }
        } else if (strElem.equalsIgnoreCase("animate-sketch-property") && currentPage != null) {
            try {
                String property = atts.getValue("name");
                String type = atts.getValue("type");
                String start = atts.getValue("start");
                String end = atts.getValue("end");
                String duration = atts.getValue("duration");
                String curve = atts.getValue("curve");

                currentPage.setAnimateProperty(property, type, start, end, duration, curve);
            } catch (Throwable e) {
                log.error(e);
            }
        } else if (strElem.equalsIgnoreCase("post-note")) {
            try {
                int x = Integer.parseInt(atts.getValue("x"));
                int y = Integer.parseInt(atts.getValue("y"));
                int w = Integer.parseInt(atts.getValue("w"));
                int h = Integer.parseInt(atts.getValue("h"));
                String state = atts.getValue("state");
                NoteDialog note = new NoteDialog(x, y);
                note.setBounds(x, y, w, h);
                note.original_w = w;
                note.original_h = h;
                note.isMinimized = state.equalsIgnoreCase("1");
                currentPage.notes.add(note);
            } catch (Throwable e) {
                log.error(e);
            }
        } else if (strElem.equalsIgnoreCase("connector")) {
            try {
                String strRegion1 = atts.getValue("region1");
                String strRegion2 = atts.getValue("region2");

                ActiveRegion region1 = this.currentPage.regions.getRegionByImageFileName(strRegion1);
                ActiveRegion region2 = this.currentPage.regions.getRegionByImageFileName(strRegion2);

                if (region1 != null && region2 != null) {
                    this.currentConnector = new Connector(region1, region2);
                    currentPage.addConnector(this.currentConnector);
                }
            } catch (Throwable e) {
                log.error(e);
            }
        } else if (strElem.equalsIgnoreCase("connector-mapping")) {
            try {
                String strVar = atts.getValue("variable");
                String strMin = atts.getValue("min");
                String strMax = atts.getValue("max");
                String strFormat = atts.getValue("format");

                for (int i = 0; i < this.currentConnector.variablesMapping.length; i++) {
                    boolean bEmpty = true;
                    for (int j = 0; j < this.currentConnector.variablesMapping[i].length; j++) {
                        if (!this.currentConnector.variablesMapping[i][j].isEmpty()) {
                            bEmpty = false;
                            break;
                        }
                    }
                    if (bEmpty) {
                        if (strVar != null) {
                            this.currentConnector.variablesMapping[i][0] = strVar;
                        }
                        if (strMin != null) {
                            this.currentConnector.variablesMapping[i][1] = strMin;
                        }
                        if (strMax != null) {
                            this.currentConnector.variablesMapping[i][2] = strMax;
                        }
                        if (strFormat != null) {
                            this.currentConnector.variablesMapping[i][3] = strFormat;
                        }
                        break;
                    }
                }

            } catch (Throwable e) {
                log.error(e);
            }
        } else if (strElem.equalsIgnoreCase("connector-property")) {
            this.currentConnectorProperty = atts.getValue("name");
        } else if (strElem.equalsIgnoreCase("action")) {
            if (onEntry != null) {
                onEntryActionIndex++;
            } else if (onExit != null) {
                onExitActionIndex++;
            } else if (!this.withinEvent) {
                currentRegion = new ActiveRegion(currentPage.regions);
                currentPage.regions.regions.add(currentRegion);
            }
        } else if (strElem.equalsIgnoreCase("region") && currentRegion != null) {
            try {
                String regionName = atts.getValue("name");
                if (regionName != null) {
                    currentRegion.strName = regionName;
                }
                currentRegion.x1 = Integer.parseInt(atts.getValue("x1"));
                currentRegion.y1 = Integer.parseInt(atts.getValue("y1"));
                currentRegion.x2 = Integer.parseInt(atts.getValue("x2"));
                currentRegion.y2 = Integer.parseInt(atts.getValue("y2"));

                if (currentRegion.x2 < currentRegion.x1) {
                    int tempX = currentRegion.x2;
                    currentRegion.x2 = currentRegion.x1;
                    currentRegion.x1 = tempX;
                }

                if (currentRegion.y2 < currentRegion.y1) {
                    int tempY = currentRegion.x2;
                    currentRegion.y2 = currentRegion.y1;
                    currentRegion.y1 = tempY;
                }

                currentRegion.shearX = Double.parseDouble(atts.getValue("shearX"));
                currentRegion.shearY = Double.parseDouble(atts.getValue("shearY"));

                currentRegion.rotation = Double.parseDouble(atts.getValue("rotation"));
            } catch (Throwable e) {
                log.error(e);
            }
        } else if (strElem.equalsIgnoreCase("perspective") && currentRegion != null) {
            try {
                currentRegion.p_x0 = Double.parseDouble(atts.getValue("p_x0"));
                currentRegion.p_y0 = Double.parseDouble(atts.getValue("p_y0"));
                currentRegion.p_x1 = Double.parseDouble(atts.getValue("p_x1"));
                currentRegion.p_y1 = Double.parseDouble(atts.getValue("p_y1"));
                currentRegion.p_x2 = Double.parseDouble(atts.getValue("p_x2"));
                currentRegion.p_y2 = Double.parseDouble(atts.getValue("p_y2"));
                currentRegion.p_x3 = Double.parseDouble(atts.getValue("p_x3"));
                currentRegion.p_y3 = Double.parseDouble(atts.getValue("p_y3"));
            } catch (Throwable e) {
                log.error(e);
            }
        } else if (strElem.equalsIgnoreCase("animate-property") && currentRegion != null) {
            try {
                String property = atts.getValue("name");
                String type = atts.getValue("type");
                String start = atts.getValue("start");
                String end = atts.getValue("end");
                String duration = atts.getValue("duration");
                String curve = atts.getValue("curve");

                currentRegion.setAnimateProperty(property, type, start, end, duration, curve);
            } catch (Throwable e) {
                log.error(e);
            }
        } else if (strElem.equalsIgnoreCase("widget-action") && currentRegion != null) {
            try {
                String property = atts.getValue("name");
                String type = atts.getValue("type");
                String start = atts.getValue("start");
                String end = atts.getValue("end");
                String duration = atts.getValue("duration");
                String curve = atts.getValue("curve");

                currentRegion.setAnimateProperty(property, type, start, end, duration, curve);
            } catch (Throwable e) {
                log.error(e);
            }
        } else if (strElem.equalsIgnoreCase("rotation_center") && currentRegion != null) {
            try {
                currentRegion.center_rotation_x = Double.parseDouble(atts.getValue("x"));
                currentRegion.center_rotation_y = Double.parseDouble(atts.getValue("y"));
            } catch (Throwable e) {
                log.error(e);
            }
        } else if (strElem.equalsIgnoreCase("trajectory_point") && currentRegion != null) {
            try {
                currentRegion.trajectory2_x = Double.parseDouble(atts.getValue("x"));
                currentRegion.trajectory2_y = Double.parseDouble(atts.getValue("y"));
            } catch (Throwable e) {
                log.error(e);
            }
        } else if (strElem.equalsIgnoreCase("event") && currentRegion != null) {
            String type = atts.getValue("type");
            MouseEventMacro currentMouseEventMacro = currentRegion.mouseProcessor.getMouseEventMacro(type);
            if (currentMouseEventMacro == null) {
                currentMouseEventMacro = new MouseEventMacro(type);
                currentRegion.mouseProcessor.mouseEventMacros.add(currentMouseEventMacro);
            }
            for (int i = 0; i < currentMouseEventMacro.getMacro().actions.length; i++) {
                Object row[] = currentMouseEventMacro.getMacro().actions[i];
                if (row[0].toString().isEmpty()) {
                    break;
                }
            }
            withinEvent = true;
        } else if (strElem.equalsIgnoreCase("interaction-event") && currentRegion != null) {
            this.interactionEventIndex++;
        } else if (strElem.equalsIgnoreCase("dimension") && currentRegion != null) {
            updateIndex++;
            String dimName = atts.getValue("name");
            String dimVariable = atts.getValue("variable");
            String dimStart = atts.getValue("start");
            String dimEnd = atts.getValue("end");
            String dimFormat = atts.getValue("format");

            currentRegion.updateTransformations[updateIndex][0] = dimName;
            currentRegion.updateTransformations[updateIndex][1] = dimVariable;
            currentRegion.updateTransformations[updateIndex][2] = dimStart;
            currentRegion.updateTransformations[updateIndex][3] = dimEnd;
            currentRegion.updateTransformations[updateIndex][4] = dimFormat == null ? "" : dimFormat;
        } else if (strElem.equalsIgnoreCase("spreadsheet-cell")) {
            spreadsheet_x = -1;
            spreadsheet_y = -1;
            try {
                spreadsheet_x = Integer.parseInt(atts.getValue("x"));
                spreadsheet_y = Integer.parseInt(atts.getValue("y"));
            } catch (Exception e) {
                log.error(e);
            }
        } else if (strElem.equalsIgnoreCase("limit-motion") && currentRegion != null) {
            limitIndex++;
            if (limitIndex < currentRegion.limits.length) {
                String dimName = atts.getValue("dimension");
                String dimMin = atts.getValue("min");
                String dimMax = atts.getValue("max");

                if (dimMax.startsWith("6.28")) {
                    dimMax = "360";
                }

                currentRegion.limits[limitIndex][0] = dimName;
                currentRegion.limits[limitIndex][1] = dimMin;
                currentRegion.limits[limitIndex][2] = dimMax;
            }
        }
    }

    int spreadsheet_x = -1;
    int spreadsheet_y = -1;

    public void endElement(String uri, String name, String qName) {
        try {
            String strElem = "";
            if ("".equals(uri)) {
                strElem = qName;
            } else {
                strElem = name;
            }

            this.processCharacters();

            currentElement = null;

            if (path().equalsIgnoreCase("/sketch/action/region-widget-event-actions/widget-action")) {
                return;
            } else if (path().equalsIgnoreCase("/sketch/action/region-widget-event-actions/widget-action/action")) {
                return;
            } else if (path().equalsIgnoreCase("/sketch/action/region-mouse-event-actions/mouse-event-action/action")) {
                return;
            } else if (path().equalsIgnoreCase("/sketch/action/region-overlap-event-actions/mouse-overlap-action/action")) {
                return;
            } else if (path().equalsIgnoreCase("/sketch/keyboard-event-actions/keyboard-event-action/action")) {
                return;
            } else if (path().equalsIgnoreCase("/sketch/mouse-event-actions/mouse-event-action/action")) {
                return;
            } else if (path().equalsIgnoreCase("/sketch/action/region-keyboard-event-actions/region-keyboard-event-action/action")) {
                return;
            } else if (path().equalsIgnoreCase("/sketch/variable-update-event-actions/variable-update-event-action/action")) {
                return;
            } else if (currentMacroSaxUtil != null && currentMacroSaxUtil.endElement(path())) {
                return;
            }

            if (strElem.equalsIgnoreCase("sketch")) {
                currentPage.isChanged();
                currentPage = null;
            } else if (strElem.equalsIgnoreCase("on-entry")) {
                onEntry = null;
                onEntryActionIndex = -1;
            } else if (strElem.equalsIgnoreCase("on-exit")) {
                onExit = null;
                onExitActionIndex = -1;
            } else if (strElem.equalsIgnoreCase("post-note")) {
                if (currentPage.notes.size() > 0) {
                    currentPage.notes.lastElement().noteTextArea.setText(strCharacters);
                }
            } else if (strElem.equalsIgnoreCase("sketch-property")) {
                currentPage.setProperty(lastSketchProperty, strCharacters);
            } else if (strElem.equalsIgnoreCase("page-variable")) {
                currentPage.localVariables.add(new LocalVariable(lastSketchVariable, strCharacters, lastSketchFormat));
                lastSketchFormat = "";
            } else if (strElem.equalsIgnoreCase("spreadsheet-cell")) {
                if (spreadsheet_x >= 0 && spreadsheet_x < currentPage.getSpreadsheetData().length && spreadsheet_y >= 0 && spreadsheet_y < currentPage.getSpreadsheetData()[0].length) {
                    currentPage.updateSpreadsheetCell(spreadsheet_x, spreadsheet_y, strCharacters);
                }
            } else if (strElem.equalsIgnoreCase("connector")) {
                this.currentConnector = null;
            } else if (strElem.equalsIgnoreCase("action")) {
                if (onEntry != null) {
                } else if (onExit != null) {
                }
                if (!this.withinEvent) {
                    currentRegion = null;
                    interactionEventIndex = -1;
                    updateIndex = -1;
                    limitIndex = -1;
                }
            } else if (strElem.equalsIgnoreCase("dimension")) {
            } else if (strElem.equalsIgnoreCase("event") && currentRegion != null) {
                withinEvent = false;
            } else if (strElem.equals("text-annotation")) {
                currentPage.strTextAnnotation = strCharacters;
            }

        } finally {
            if (pathElements.size() > 0) {
                pathElements.remove(pathElements.size() - 1);
            }
        }
    }

    String strCharacters = "";

    public void characters(char ch[], int start, int length) {
        if (currentElement != null) {
            String strValue = new String(ch, start, length);
            strCharacters += strValue;
        }
    }

    private int indexOfWidgetAction = -1;

    public void processCharacters() {
        strCharacters = strCharacters.replace("&lt;", "<");
        strCharacters = strCharacters.replace("&gt;", ">");
        strCharacters = strCharacters.replace("&amp;", "&");
        if (currentElement == null) {
            return;
        }

        if (path().equalsIgnoreCase("/sketch/action/region-widget-event-actions/widget-action/name")) {
            currentRegion.widgetEventMacros.get(currentRegion.widgetEventMacros.size() - 1).setEventName(strCharacters);
            return;
        } else if (path().equalsIgnoreCase("/sketch/action/region-widget-event-actions/widget-action/repeat")) {
            try {
                currentRegion.widgetEventMacros.get(currentRegion.widgetEventMacros.size() - 1).getMacro().repeat = (int) Double.parseDouble(strCharacters);
            } catch (NumberFormatException e) {
                log.error(e);
            }
            return;
        } else if (path().equalsIgnoreCase("/sketch/action/region-widget-event-actions/widget-action/action/type")) {
            Macro macro = currentRegion.widgetEventMacros.get(currentRegion.widgetEventMacros.size() - 1).getMacro();
            macro.actions[indexOfWidgetAction][0] = strCharacters;
            return;
        } else if (path().equalsIgnoreCase("/sketch/action/region-widget-event-actions/widget-action/action/param1")) {
            Macro macro = currentRegion.widgetEventMacros.get(currentRegion.widgetEventMacros.size() - 1).getMacro();
            macro.actions[indexOfWidgetAction][1] = strCharacters;
            return;
        } else if (path().equalsIgnoreCase("/sketch/action/region-widget-event-actions/widget-action/action/param2")) {
            Macro macro = currentRegion.widgetEventMacros.get(currentRegion.widgetEventMacros.size() - 1).getMacro();
            macro.actions[indexOfWidgetAction][2] = strCharacters;
            return;
        } else if (path().equalsIgnoreCase("/sketch/action/region-mouse-event-actions/mouse-event-action/name")) {
            MouseEventMacro mouseEventMacro = currentRegion.mouseProcessor.getMouseEventMacro(strCharacters);
            if (mouseEventMacro == null) {
                mouseEventMacro = new MouseEventMacro(strCharacters);
                currentRegion.mouseProcessor.mouseEventMacros.add(mouseEventMacro);
            }
            mouseEventMacro.setEventName(strCharacters);
            currentMacroSaxUtil = new MacroSaxParserUtil(mouseEventMacro.getMacro(), "/sketch/action/region-mouse-event-actions/mouse-event-action");
            currentMacroSaxUtil.processCharacters(path(), strCharacters);
        } else if (path().equalsIgnoreCase("/sketch/action/region-overlap-event-actions/region-overlap-event-action/name")) {
            RegionOverlapEventMacro regionOverlapEventMacro = new RegionOverlapEventMacro(strCharacters);
            currentRegion.regionOverlapEventMacros.add(regionOverlapEventMacro);
            regionOverlapEventMacro.setEventName(strCharacters);
            currentMacroSaxUtil = new MacroSaxParserUtil(regionOverlapEventMacro.getMacro(), "/sketch/action/region-overlap-event-actions/region-overlap-event-action");
            currentMacroSaxUtil.processCharacters(path(), strCharacters);
        } else if (path().equalsIgnoreCase("/sketch/keyboard-event-actions/keyboard-event-action/name")) {
            KeyboardEventMacro keyboardEventMacro = new KeyboardEventMacro(strCharacters);
            currentPage.keyboardProcessor.keyboardEventMacros.add(keyboardEventMacro);
            keyboardEventMacro.setEventName(strCharacters);
            currentMacroSaxUtil = new MacroSaxParserUtil(keyboardEventMacro.getMacro(), "/sketch/keyboard-event-actions/keyboard-event-action");
            currentMacroSaxUtil.processCharacters(path(), strCharacters);
        } else if (path().equalsIgnoreCase("/sketch/mouse-event-actions/mouse-event-action/name")) {
            MouseEventMacro mouseEventMacro = new MouseEventMacro(strCharacters);
            currentPage.mouseProcessor.mouseEventMacros.add(mouseEventMacro);
            mouseEventMacro.setEventName(strCharacters);
            currentMacroSaxUtil = new MacroSaxParserUtil(mouseEventMacro.getMacro(), "/sketch/mouse-event-actions/mouse-event-action");
            currentMacroSaxUtil.processCharacters(path(), strCharacters);
        } else if (path().equalsIgnoreCase("/sketch/action/region-keyboard-event-actions/region-keyboard-event-action/name")) {
            KeyboardEventMacro keyboardEventMacro = new KeyboardEventMacro(strCharacters);
            currentRegion.keyboardProcessor.keyboardEventMacros.add(keyboardEventMacro);
            keyboardEventMacro.setEventName(strCharacters);
            currentMacroSaxUtil = new MacroSaxParserUtil(keyboardEventMacro.getMacro(), "/sketch/action/region-keyboard-event-actions/region-keyboard-event-action");
            currentMacroSaxUtil.processCharacters(path(), strCharacters);
        } else if (path().equalsIgnoreCase("/sketch/variable-update-event-actions/variable-update-event-action/name")) {
            VariableUpdateEventMacro variableUpdateEventMacro = new VariableUpdateEventMacro(strCharacters);
            currentPage.variableUpdateEventMacros.add(variableUpdateEventMacro);
            variableUpdateEventMacro.setEventName(strCharacters);
            currentMacroSaxUtil = new MacroSaxParserUtil(variableUpdateEventMacro.getMacro(), "/sketch/variable-update-event-actions/variable-update-event-action");
            currentMacroSaxUtil.processCharacters(path(), strCharacters);
        } else if (currentMacroSaxUtil != null && currentMacroSaxUtil.processCharacters(path(), strCharacters)) {
            return;
        }

        if (currentPage != null) {
            if (currentElement.equalsIgnoreCase("title")) {
                currentPage.title = strCharacters;
            }
            if (currentElement.equalsIgnoreCase("id")) {
                currentPage.setId(strCharacters);
            }
            if (currentElement.equalsIgnoreCase("state-diagram-x")) {
                try {
                    currentPage.stateDiagramX = Double.parseDouble(strCharacters);
                } catch (Throwable e) {
                    log.error(e);
                }
            }
            if (currentElement.equalsIgnoreCase("state-diagram-y")) {
                try {
                    currentPage.stateDiagramY = Double.parseDouble(strCharacters);
                } catch (Throwable e) {
                    log.error(e);
                }
            }
            if (currentElement.equalsIgnoreCase("spreadsheet-column-widths")) {
                currentPage.strSpreadsheetColumnWidths = strCharacters;
            }
            if (currentElement.equalsIgnoreCase("page-width")) {
                try {
                    currentPage.pageWidth = (int) Double.parseDouble(strCharacters);
                } catch (Throwable e) {
                    log.error(e);
                }
            }
            if (currentElement.equalsIgnoreCase("page-height")) {
                try {
                    currentPage.pageHeight = (int) Double.parseDouble(strCharacters);
                } catch (Throwable e) {
                    log.error(e);
                }
            }
        }
        if (currentElement.equalsIgnoreCase("name")) {
            if (onEntry != null) {
                onEntry.name = strCharacters;
            } else if (onExit != null) {
                onExit.name = strCharacters;
            }
        }
        if (this.currentConnector != null) {
            if (currentElement.equalsIgnoreCase("connector-caption")) {
                this.currentConnector.caption = strCharacters;
            } else if (currentElement.equalsIgnoreCase("connector-property")) {
                String key = this.currentConnectorProperty;
                if (key != null && !key.isEmpty()) {
                    this.currentConnector.setProperty(key, strCharacters);
                }
            }
        }

        if (currentElement.equalsIgnoreCase("repeat")) {
            if (onEntry != null) {
                try {
                    onEntry.repeat = Integer.parseInt(strCharacters);
                } catch (Throwable e) {
                    onEntry.repeat = 1;
                }
            }
        }

        if (currentElement.equalsIgnoreCase("type")) {
            if (onEntryActionIndex >= 0) {
                strCharacters = Macro.checkOldCommand(strCharacters);
                onEntry.actions[onEntryActionIndex][0] = strCharacters;
            } else if (onExitActionIndex >= 0) {
                strCharacters = Macro.checkOldCommand(strCharacters);
                onExit.actions[onExitActionIndex][0] = strCharacters;
            }
        }
        if (currentElement.equalsIgnoreCase("param1")) {
            if (onEntryActionIndex >= 0) {
                onEntry.actions[onEntryActionIndex][1] = strCharacters;
            } else if (onExitActionIndex >= 0) {
                onExit.actions[onExitActionIndex][1] = strCharacters;
            }
        }
        if (currentElement.equalsIgnoreCase("param2")) {
            if (onEntryActionIndex >= 0) {
                onEntry.actions[onEntryActionIndex][2] = strCharacters;
            } else if (onExitActionIndex >= 0) {
                onExit.actions[onExitActionIndex][2] = strCharacters;
            }
        }
        if (currentElement.equalsIgnoreCase("show-text") && currentRegion != null) {
            currentRegion.strTextField = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("embedded-sketch") && currentRegion != null) {
            currentRegion.strEmbeddedSketch = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("embedded-sketch-var-prefix") && currentRegion != null) {
            currentRegion.strEmbeddedSketchVarPrefix = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("embedded-sketch-var-postfix") && currentRegion != null) {
            currentRegion.strEmbeddedSketchVarPostfix = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("capture-screen-x") && currentRegion != null) {
            currentRegion.strCaptureScreenX = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("capture-screen") && currentRegion != null) {
            currentRegion.bCaptureScreen = strCharacters.equals("true");
        }
        if (currentElement.equalsIgnoreCase("capture-screen-mouse-map") && currentRegion != null) {
            currentRegion.bCaptureScreenMouseMap = strCharacters.equals("true");
        }
        if (currentElement.equalsIgnoreCase("capture-screen-y") && currentRegion != null) {
            currentRegion.strCaptureScreenY = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("capture-screen-width") && currentRegion != null) {
            currentRegion.strCaptureScreenWidth = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("capture-screen-height") && currentRegion != null) {
            currentRegion.strCaptureScreenHeight = strCharacters;
        }

        if (currentElement.equalsIgnoreCase("basic-shape") && currentRegion != null) {
            currentRegion.shape = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("basic-shape-args") && currentRegion != null) {
            currentRegion.strShapeArgs = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("group") && currentRegion != null) {
            currentRegion.regionGrouping = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("show-image") && currentRegion != null) {
            currentRegion.strImageUrlField = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("image-index") && currentRegion != null) {
            currentRegion.strImageIndex = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("is-active") && currentRegion != null) {
            currentRegion.strActive = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("type") && currentRegion != null) {
            currentRegion.strType = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("control") && currentRegion != null) {
            if (strCharacters.equals("UMLGraph / Class Diagram / Simple DSL")) {
                strCharacters = "Cascading UML";
            }
            if (strCharacters.equals("UMLGraph / Class Diagram / Cascading UML")) {
                strCharacters = "Cascading UML";
            }
            currentRegion.strWidget = strCharacters;
        }

        if (currentElement.equalsIgnoreCase("control-parameters") && currentRegion != null) {
            currentRegion.strWidgetProperties = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("image-animation-ms") && currentRegion != null) {
            currentRegion.strAnimationMs = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("text-area") && currentRegion != null) {
            currentRegion.strText = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("wrap-text") && currentRegion != null) {
            currentRegion.bWrapText = strCharacters.equals("true");
        }
        if (currentElement.equalsIgnoreCase("trim-text") && currentRegion != null) {
            currentRegion.bTrimText = strCharacters.equals("true");
        }
        if (currentElement.equalsIgnoreCase("is-solid") && currentRegion != null) {
            currentRegion.bWalkThrough = strCharacters.equals("true");
        }
        if (currentElement.equalsIgnoreCase("characters-per-line") && currentRegion != null) {
            currentRegion.strCharactersPerLine = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("max-lines") && currentRegion != null) {
            currentRegion.strMaxNumLines = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("trajectory") && currentRegion != null) {
            currentRegion.strTrajectory1 = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("control-items") && currentRegion != null) {
            currentRegion.strWidgetItems = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("trajectory2") && currentRegion != null) {
            currentRegion.strTrajectory2 = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("image-draw") && currentRegion != null) {
            currentRegion.setImageFile(strCharacters, 0);
        }
        if (currentElement.equalsIgnoreCase("additional-image-draw") && currentRegion != null) {
            currentRegion.additionalImageFile.add(strCharacters);
            currentRegion.additionalDrawImages.add(null);
            currentRegion.additionalImageChanged.add(new Boolean(false));
        }
        if (currentElement.equalsIgnoreCase("x") && currentRegion != null) {
            currentRegion.strX = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("visible") && currentRegion != null) {
            currentRegion.bVisible = strCharacters.equalsIgnoreCase("true");
        }
        if (currentElement.equalsIgnoreCase("layer") && currentRegion != null) {
            currentRegion.layer = 0;
            try {
                currentRegion.layer = (int) Double.parseDouble(strCharacters);
            } catch (Exception e) {
            }
        }
        if (currentElement.equalsIgnoreCase("y") && currentRegion != null) {
            currentRegion.strY = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("direct-x1") && currentRegion != null) {
            currentRegion.strX1 = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("direct-y2") && currentRegion != null) {
            currentRegion.strY2 = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("direct-x2") && currentRegion != null) {
            currentRegion.strX2 = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("direct-y2") && currentRegion != null) {
            currentRegion.strY2 = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("perspective-x1") && currentRegion != null) {
            currentRegion.strPerspectiveX1 = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("perspective-y1") && currentRegion != null) {
            currentRegion.strPerspectiveY1 = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("perspective-x2") && currentRegion != null) {
            currentRegion.strPerspectiveX2 = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("perspective-y2") && currentRegion != null) {
            currentRegion.strPerspectiveY2 = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("perspective-x3") && currentRegion != null) {
            currentRegion.strPerspectiveX3 = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("perspective-y3") && currentRegion != null) {
            currentRegion.strPerspectiveY3 = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("perspective-x4") && currentRegion != null) {
            currentRegion.strPerspectiveX4 = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("perspective-y4") && currentRegion != null) {
            currentRegion.strPerspectiveY4 = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("rotate-3d-horizontal") && currentRegion != null) {
            currentRegion.strRotation3DHorizontal = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("rotate-3d-vertical") && currentRegion != null) {
            currentRegion.strRotation3DVertical = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("automatic-perspective") && currentRegion != null) {
            currentRegion.strAutomaticPerspective = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("perspective-depth") && currentRegion != null) {
            currentRegion.strPerspectiveDepth = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("relative-x") && currentRegion != null) {
            currentRegion.strRelX = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("region-zoom") && currentRegion != null) {
            currentRegion.strZoom = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("relative-y") && currentRegion != null) {
            currentRegion.strRelY = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("trajectory-position") && currentRegion != null) {
            currentRegion.strTrajectoryPosition = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("width") && currentRegion != null) {
            currentRegion.strWidth = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("height") && currentRegion != null) {
            currentRegion.strHeight = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("rotation") && currentRegion != null) {
            currentRegion.strRotate = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("shearX") && currentRegion != null) {
            currentRegion.strShearX = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("shearY") && currentRegion != null) {
            currentRegion.strShearY = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("windowX") && currentRegion != null) {
            currentRegion.strWindowX = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("windowY") && currentRegion != null) {
            currentRegion.strWindowY = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("windowWidth") && currentRegion != null) {
            currentRegion.strWindowWidth = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("windowHeight") && currentRegion != null) {
            currentRegion.strWindowHeight = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("transparency") && currentRegion != null) {
            currentRegion.strTransparency = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("speed") && currentRegion != null) {
            currentRegion.strSpeed = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("motionDirection") && currentRegion != null) {
            currentRegion.strSpeedDirection = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("rotationSpeed") && currentRegion != null) {
            currentRegion.strRotationSpeed = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("pen") && currentRegion != null) {
            currentRegion.strPen = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("move") && currentRegion != null) {
            currentRegion.bCanMove = strCharacters.equals("true");
        }
        if (currentElement.equalsIgnoreCase("rotate") && currentRegion != null) {
            currentRegion.bCanRotate = strCharacters.equals("true");
        }
        if (currentElement.equalsIgnoreCase("resize") && currentRegion != null) {
            currentRegion.bCanResize = strCharacters.equals("true");
        }
        if (currentElement.equalsIgnoreCase("fit-to-box") && currentRegion != null) {
            currentRegion.bFitToBox = strCharacters.equals("true");
        }
        if (currentElement.equalsIgnoreCase("stick-to-trajectory") && currentRegion != null) {
            currentRegion.bStickToTrajectory = strCharacters.equals("true");
        }
        if (currentElement.equalsIgnoreCase("orientation-trajectory") && currentRegion != null) {
            currentRegion.bOrientationTrajectory = strCharacters.equals("true");
        }
        if (currentElement.equalsIgnoreCase("font-name") && currentRegion != null) {
            currentRegion.fontName = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("font-style") && currentRegion != null) {
            currentRegion.fontStyle = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("font-size") && currentRegion != null) {
            currentRegion.fontSize = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("font-color") && currentRegion != null) {
            currentRegion.fontColor = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("fill-color") && currentRegion != null) {
            currentRegion.strFillColor = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("line-color") && currentRegion != null) {
            currentRegion.strLineColor = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("line-style") && currentRegion != null) {
            currentRegion.strLineStyle = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("line-thickness") && currentRegion != null) {
            currentRegion.strLineThickness = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("horizontal-alignment") && currentRegion != null) {
            currentRegion.strHAlign = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("vertical-alignment") && currentRegion != null) {
            currentRegion.strVAlign = strCharacters;
        }
        if (currentElement.startsWith("interaction-event-") && currentRegion != null) {
            if (currentElement.equalsIgnoreCase("interaction-event-region") && currentRegion != null) {
                RegionOverlapEventMacro regionOverlapEventMacro = new RegionOverlapEventMacro("");
                currentRegion.regionOverlapEventMacros.add(regionOverlapEventMacro);
                if (this.interactionEventIndex >= 0) {
                    regionOverlapEventMacro.getMacro().getParameters().put(RegionOverlapEventMacro.PARAMETER_REGION_ID, strCharacters);
                    // currentRegion.interactionEvents[interactionEventIndex][0] = strCharacters;
                }
                return;
            } else if (currentElement.equalsIgnoreCase("interaction-event-type") && currentRegion != null) {
                RegionOverlapEventMacro regionOverlapEventMacro = currentRegion.regionOverlapEventMacros.get(currentRegion.regionOverlapEventMacros.size() - 1);
                if (this.interactionEventIndex >= 0) {
                    regionOverlapEventMacro.setEventName(strCharacters);
                }
                return;
            }
            RegionOverlapEventMacro regionOverlapEventMacro = currentRegion.regionOverlapEventMacros.get(currentRegion.regionOverlapEventMacros.size() - 1);
            if (currentElement.equalsIgnoreCase("interaction-event-action") && currentRegion != null) {
                if (this.interactionEventIndex >= 0) {
                    strCharacters = Macro.checkOldCommand(strCharacters);
                    regionOverlapEventMacro.getMacro().addLine(strCharacters, "", "");
                }
                return;
            } else if (currentElement.equalsIgnoreCase("interaction-event-param1") && currentRegion != null) {
                if (this.interactionEventIndex >= 0) {
                    regionOverlapEventMacro.getMacro().setLastLineValue(1, strCharacters);
                }
                return;
            } else if (currentElement.equalsIgnoreCase("interaction-event-param2") && currentRegion != null) {
                if (this.interactionEventIndex >= 0) {
                    regionOverlapEventMacro.getMacro().setLastLineValue(2, strCharacters);
                }
                return;
            }
        }
    }
}

