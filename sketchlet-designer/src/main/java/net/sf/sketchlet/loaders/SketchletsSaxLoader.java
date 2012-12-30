package net.sf.sketchlet.loaders;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.framework.model.Connector;
import net.sf.sketchlet.designer.editor.tool.notes.NoteDialog;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.model.ActiveRegions;
import net.sf.sketchlet.framework.model.events.keyboard.KeyboardEventMacro;
import net.sf.sketchlet.framework.model.PageVariable;
import net.sf.sketchlet.framework.model.events.mouse.MouseEventMacro;
import net.sf.sketchlet.framework.model.Page;
import net.sf.sketchlet.framework.model.events.overlap.RegionOverlapEventMacro;
import net.sf.sketchlet.framework.model.events.variable.VariableUpdateEventMacro;
import net.sf.sketchlet.framework.model.events.widget.WidgetEventMacro;
import net.sf.sketchlet.framework.model.programming.macros.Macro;
import net.sf.sketchlet.framework.model.programming.macros.MacroSaxParserUtil;
import org.apache.commons.lang.StringUtils;
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
    private java.util.List<String> pathElements = new ArrayList<String>();
    private int spreadsheet_x = -1;
    private int spreadsheet_y = -1;
    private String strCharacters = "";

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

    public void startDocument() {
        pages = new Vector<Page>();
    }

    public void endDocument() {
        for (Page s : pages) {
            s.initCache();
        }
    }

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
        if (path().equalsIgnoreCase("/page/active-regions/active-region/region-widget-event-actions/widget-event-action")) {
            currentRegion.getWidgetEventMacros().add(new WidgetEventMacro(""));
            indexOfWidgetAction = -1;
            return;
        } else if (path().equalsIgnoreCase("/page/active-regions/active-region/region-widget-event-actions/widget-event-action/action")) {
            indexOfWidgetAction++;
            return;
        } else if (path().equalsIgnoreCase("/page/active-regions/active-region/region-mouse-event-actions/mouse-event-action")) {
            return;
        } else if (path().equalsIgnoreCase("/page/active-regions/active-region/region-overlap-event-actions/region-overlap-event-action")) {
            return;
        } else if (currentMacroSaxUtil != null && currentMacroSaxUtil.startElement(path(), atts)) {
            return;
        }

        if (strElem.equalsIgnoreCase("page") || strElem.equalsIgnoreCase("sketch")) {
            currentPage = new Page("", "");
            currentPage.setTitle("");
            currentPage.setRegions(new ActiveRegions(currentPage));
            pages.add(currentPage);
        } else if (strElem.equalsIgnoreCase("on-entry") && currentPage != null) {
            currentPage.setOnEntryMacro(new Macro());
            onEntry = currentPage.getOnEntryMacro();
            onEntryActionIndex = -1;

            for (int i = 0; i < onEntry.getActions().length; i++) {
                onEntry.getActions()[i][0] = "";
                onEntry.getActions()[i][1] = "";
                onEntry.getActions()[i][2] = "";
            }
        } else if (strElem.equalsIgnoreCase("on-exit") && currentPage != null) {
            currentPage.setOnExitMacro(new Macro());
            onExit = currentPage.getOnExitMacro();
            onExitActionIndex = -1;
            for (int i = 0; i < onExit.getActions().length; i++) {
                onExit.getActions()[i][0] = "";
                onExit.getActions()[i][1] = "";
                onExit.getActions()[i][2] = "";
            }
        } else if (strElem.equalsIgnoreCase("repeat")) {
        } else if (strElem.equalsIgnoreCase("page-property") || strElem.equalsIgnoreCase("sketch-property")) {
            lastSketchProperty = atts.getValue("name");
        } else if (strElem.equalsIgnoreCase("page-variable")) {
            lastSketchVariable = atts.getValue("name");
            lastSketchFormat = atts.getValue("format");
        } else if (strElem.equalsIgnoreCase("page-layer") || strElem.equalsIgnoreCase("sketch-layer")) {
            layerIndex++;
            if (StringUtils.isNotBlank(atts.getValue("index"))) {
                try {
                    layerIndex = (int) Double.parseDouble(atts.getValue("index"));
                } catch (NumberFormatException e) {
                    log.error(e);
                }
            }
            try {
                currentPage.getLayerActive()[layerIndex] = atts.getValue("active").equalsIgnoreCase("true");
            } catch (Exception e) {
                log.error(e);
            }
        } else if ((strElem.equalsIgnoreCase("animate-page-property") || strElem.equalsIgnoreCase("animate-sketch-property")) && currentPage != null) {
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
                note.setOriginalWidth(w);
                note.setOriginalHeight(h);
                note.setMinimized(state.equalsIgnoreCase("1"));
                currentPage.getNotes().add(note);
            } catch (Throwable e) {
                log.error(e);
            }
        } else if (strElem.equalsIgnoreCase("connector")) {
            try {
                String strRegion1 = atts.getValue("region1");
                String strRegion2 = atts.getValue("region2");

                ActiveRegion region1 = this.currentPage.getRegions().getRegionByImageFileName(strRegion1);
                ActiveRegion region2 = this.currentPage.getRegions().getRegionByImageFileName(strRegion2);

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

                for (int i = 0; i < this.currentConnector.getVariablesMapping().length; i++) {
                    boolean bEmpty = true;
                    for (int j = 0; j < this.currentConnector.getVariablesMapping()[i].length; j++) {
                        if (!this.currentConnector.getVariablesMapping()[i][j].isEmpty()) {
                            bEmpty = false;
                            break;
                        }
                    }
                    if (bEmpty) {
                        if (strVar != null) {
                            this.currentConnector.getVariablesMapping()[i][0] = strVar;
                        }
                        if (strMin != null) {
                            this.currentConnector.getVariablesMapping()[i][1] = strMin;
                        }
                        if (strMax != null) {
                            this.currentConnector.getVariablesMapping()[i][2] = strMax;
                        }
                        if (strFormat != null) {
                            this.currentConnector.getVariablesMapping()[i][3] = strFormat;
                        }
                        break;
                    }
                }

            } catch (Throwable e) {
                log.error(e);
            }
        } else if (strElem.equalsIgnoreCase("connector-property")) {
            this.currentConnectorProperty = atts.getValue("name");
        } else if (strElem.equalsIgnoreCase("active-region")) {
            currentRegion = new ActiveRegion(currentPage.getRegions());
            currentPage.getRegions().getRegions().add(currentRegion);
        } else if (strElem.equalsIgnoreCase("action")) {
            if (onEntry != null) {
                onEntryActionIndex++;
            } else if (onExit != null) {
                onExitActionIndex++;
            } else if (!this.withinEvent) {
                currentRegion = new ActiveRegion(currentPage.getRegions());
                currentPage.getRegions().getRegions().add(currentRegion);
            }
        } else if (strElem.equalsIgnoreCase("region") && currentRegion != null) {
            try {
                String regionName = atts.getValue("name");
                if (regionName != null) {
                    currentRegion.setName(regionName);
                }
                currentRegion.setX1Value(Integer.parseInt(atts.getValue("x1")));
                currentRegion.setY1Value(Integer.parseInt(atts.getValue("y1")));
                currentRegion.setX2Value(Integer.parseInt(atts.getValue("x2")));
                currentRegion.setY2Value(Integer.parseInt(atts.getValue("y2")));

                if (currentRegion.getX2Value() < currentRegion.getX1Value()) {
                    int tempX = currentRegion.getX2Value();
                    currentRegion.setX2Value(currentRegion.getX1Value());
                    currentRegion.setX1Value(tempX);
                }

                if (currentRegion.getY2Value() < currentRegion.getY1Value()) {
                    int tempY = currentRegion.getX2Value();
                    currentRegion.setY2Value(currentRegion.getY1Value());
                    currentRegion.setY1Value(tempY);
                }

                currentRegion.setShearXValue(Double.parseDouble(atts.getValue("shearX")));
                currentRegion.setShearYValue(Double.parseDouble(atts.getValue("shearY")));

                currentRegion.setRotationValue(Double.parseDouble(atts.getValue("rotation")));
            } catch (Throwable e) {
                log.error(e);
            }
        } else if (strElem.equalsIgnoreCase("perspective") && currentRegion != null) {
            try {
                currentRegion.setP_x0(Double.parseDouble(atts.getValue("p_x0")));
                currentRegion.setP_y0(Double.parseDouble(atts.getValue("p_y0")));
                currentRegion.setP_x1(Double.parseDouble(atts.getValue("p_x1")));
                currentRegion.setP_y1(Double.parseDouble(atts.getValue("p_y1")));
                currentRegion.setP_x2(Double.parseDouble(atts.getValue("p_x2")));
                currentRegion.setP_y2(Double.parseDouble(atts.getValue("p_y2")));
                currentRegion.setP_x3(Double.parseDouble(atts.getValue("p_x3")));
                currentRegion.setP_y3(Double.parseDouble(atts.getValue("p_y3")));
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
                currentRegion.setCenterOfRotationX(Double.parseDouble(atts.getValue("x")));
                currentRegion.setCenterOfRotationY(Double.parseDouble(atts.getValue("y")));
            } catch (Throwable e) {
                log.error(e);
            }
        } else if (strElem.equalsIgnoreCase("trajectory_point") && currentRegion != null) {
            try {
                currentRegion.setTrajectory2X(Double.parseDouble(atts.getValue("x")));
                currentRegion.setTrajectory2Y(Double.parseDouble(atts.getValue("y")));
            } catch (Throwable e) {
                log.error(e);
            }
        } else if (strElem.equalsIgnoreCase("event") && currentRegion != null) {
            String type = atts.getValue("type");
            MouseEventMacro currentMouseEventMacro = currentRegion.getMouseEventsProcessor().getMouseEventMacro(type);
            if (currentMouseEventMacro == null) {
                currentMouseEventMacro = new MouseEventMacro(type);
                currentRegion.getMouseEventsProcessor().getMouseEventMacros().add(currentMouseEventMacro);
            }
            for (int i = 0; i < currentMouseEventMacro.getMacro().getActions().length; i++) {
                Object row[] = currentMouseEventMacro.getMacro().getActions()[i];
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

            currentRegion.getMotionAndRotationVariablesMapping()[updateIndex][0] = dimName;
            currentRegion.getMotionAndRotationVariablesMapping()[updateIndex][1] = dimVariable;
            currentRegion.getMotionAndRotationVariablesMapping()[updateIndex][2] = dimStart;
            currentRegion.getMotionAndRotationVariablesMapping()[updateIndex][3] = dimEnd;
            currentRegion.getMotionAndRotationVariablesMapping()[updateIndex][4] = dimFormat == null ? "" : dimFormat;
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
            if (limitIndex < currentRegion.getMotionAndRotationLimits().length) {
                String dimName = atts.getValue("dimension");
                String dimMin = atts.getValue("min");
                String dimMax = atts.getValue("max");

                if (dimMax.startsWith("6.28")) {
                    dimMax = "360";
                }

                currentRegion.getMotionAndRotationLimits()[limitIndex][0] = dimName;
                currentRegion.getMotionAndRotationLimits()[limitIndex][1] = dimMin;
                currentRegion.getMotionAndRotationLimits()[limitIndex][2] = dimMax;
            }
        }
    }

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

            if (path().equalsIgnoreCase("/page/active-regions/active-region/region-widget-event-actions/widget-action")) {
                return;
            } else if (path().equalsIgnoreCase("/page/active-regions/active-region/region-widget-event-actions/widget-action/action")) {
                return;
            } else if (path().equalsIgnoreCase("/page/active-regions/active-region/region-mouse-event-actions/mouse-event-action/action")) {
                return;
            } else if (path().equalsIgnoreCase("/page/active-regions/active-region/region-overlap-event-actions/mouse-overlap-action/action")) {
                return;
            } else if (path().equalsIgnoreCase("/page/keyboard-event-actions/keyboard-event-action/action")) {
                return;
            } else if (path().equalsIgnoreCase("/page/mouse-event-actions/mouse-event-action/action")) {
                return;
            } else if (path().equalsIgnoreCase("/page/active-regions/active-region/region-keyboard-event-actions/region-keyboard-event-action/action")) {
                return;
            } else if (path().equalsIgnoreCase("/page/variable-update-event-actions/variable-update-event-action/action")) {
                return;
            } else if (currentMacroSaxUtil != null && currentMacroSaxUtil.endElement(path())) {
                return;
            }

            if (strElem.equalsIgnoreCase("page") || strElem.equalsIgnoreCase("sketch")) {
                currentPage.isChanged();
                currentPage = null;
            } else if (strElem.equalsIgnoreCase("on-entry")) {
                onEntry = null;
                onEntryActionIndex = -1;
            } else if (strElem.equalsIgnoreCase("on-exit")) {
                onExit = null;
                onExitActionIndex = -1;
            } else if (strElem.equalsIgnoreCase("post-note")) {
                if (currentPage.getNotes().size() > 0) {
                    currentPage.getNotes().lastElement().setNoteText(strCharacters);
                }
            } else if (strElem.equalsIgnoreCase("page-property") || strElem.equalsIgnoreCase("sketch-property")) {
                currentPage.setProperty(lastSketchProperty, strCharacters);
            } else if (strElem.equalsIgnoreCase("page-variable")) {
                currentPage.getPageVariables().add(new PageVariable(lastSketchVariable, strCharacters, lastSketchFormat));
                lastSketchFormat = "";
            } else if (strElem.equalsIgnoreCase("spreadsheet-cell")) {
                if (spreadsheet_x >= 0 && spreadsheet_x < currentPage.getSpreadsheetData().length && spreadsheet_y >= 0 && spreadsheet_y < currentPage.getSpreadsheetData()[0].length) {
                    currentPage.updateSpreadsheetCell(spreadsheet_x, spreadsheet_y, strCharacters);
                }
            } else if (strElem.equalsIgnoreCase("connector")) {
                this.currentConnector = null;
            } else if (strElem.equalsIgnoreCase("active-region")) {
                currentRegion = null;
                interactionEventIndex = -1;
                updateIndex = -1;
                limitIndex = -1;
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
                currentPage.setTextAnnotation(strCharacters);
            }

        } finally {
            if (pathElements.size() > 0) {
                pathElements.remove(pathElements.size() - 1);
            }
        }
    }

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

        if (path().equalsIgnoreCase("/page/active-regions/active-region/region-widget-event-actions/widget-action/name")) {
            currentRegion.getWidgetEventMacros().get(currentRegion.getWidgetEventMacros().size() - 1).setEventName(strCharacters);
            return;
        } else if (path().equalsIgnoreCase("/page/active-regions/active-region/region-widget-event-actions/widget-action/repeat")) {
            try {
                currentRegion.getWidgetEventMacros().get(currentRegion.getWidgetEventMacros().size() - 1).getMacro().setRepeat((int) Double.parseDouble(strCharacters));
            } catch (NumberFormatException e) {
                log.error(e);
            }
            return;
        } else if (path().equalsIgnoreCase("/page/active-regions/active-region/region-widget-event-actions/widget-action/action/type")) {
            Macro macro = currentRegion.getWidgetEventMacros().get(currentRegion.getWidgetEventMacros().size() - 1).getMacro();
            macro.getActions()[indexOfWidgetAction][0] = strCharacters;
            return;
        } else if (path().equalsIgnoreCase("/page/active-regions/active-region/region-widget-event-actions/widget-action/action/param1")) {
            Macro macro = currentRegion.getWidgetEventMacros().get(currentRegion.getWidgetEventMacros().size() - 1).getMacro();
            macro.getActions()[indexOfWidgetAction][1] = strCharacters;
            return;
        } else if (path().equalsIgnoreCase("/page/active-regions/active-region/region-widget-event-actions/widget-action/action/param2")) {
            Macro macro = currentRegion.getWidgetEventMacros().get(currentRegion.getWidgetEventMacros().size() - 1).getMacro();
            macro.getActions()[indexOfWidgetAction][2] = strCharacters;
            return;
        } else if (path().equalsIgnoreCase("/page/active-regions/active-region/region-mouse-event-actions/mouse-event-action/name")) {
            MouseEventMacro mouseEventMacro = currentRegion.getMouseEventsProcessor().getMouseEventMacro(strCharacters);
            if (mouseEventMacro == null) {
                mouseEventMacro = new MouseEventMacro(strCharacters);
                currentRegion.getMouseEventsProcessor().getMouseEventMacros().add(mouseEventMacro);
            }
            mouseEventMacro.setEventName(strCharacters);
            currentMacroSaxUtil = new MacroSaxParserUtil(mouseEventMacro.getMacro(), "/page/active-regions/active-region/region-mouse-event-actions/mouse-event-action");
            currentMacroSaxUtil.processCharacters(path(), strCharacters);
        } else if (path().equalsIgnoreCase("/page/active-regions/active-region/region-overlap-event-actions/region-overlap-event-action/name")) {
            RegionOverlapEventMacro regionOverlapEventMacro = new RegionOverlapEventMacro(strCharacters);
            currentRegion.getRegionOverlapEventMacros().add(regionOverlapEventMacro);
            regionOverlapEventMacro.setEventName(strCharacters);
            currentMacroSaxUtil = new MacroSaxParserUtil(regionOverlapEventMacro.getMacro(), "/page/active-regions/active-region/region-overlap-event-actions/region-overlap-event-action");
            currentMacroSaxUtil.processCharacters(path(), strCharacters);
        } else if (path().equalsIgnoreCase("/page/keyboard-event-actions/keyboard-event-action/name")) {
            KeyboardEventMacro keyboardEventMacro = new KeyboardEventMacro(strCharacters);
            currentPage.getKeyboardEventsProcessor().getKeyboardEventMacros().add(keyboardEventMacro);
            keyboardEventMacro.setEventName(strCharacters);
            currentMacroSaxUtil = new MacroSaxParserUtil(keyboardEventMacro.getMacro(), "/page/keyboard-event-actions/keyboard-event-action");
            currentMacroSaxUtil.processCharacters(path(), strCharacters);
        } else if (path().equalsIgnoreCase("/page/mouse-event-actions/mouse-event-action/name")) {
            MouseEventMacro mouseEventMacro = new MouseEventMacro(strCharacters);
            currentPage.getMouseEventsProcessor().getMouseEventMacros().add(mouseEventMacro);
            mouseEventMacro.setEventName(strCharacters);
            currentMacroSaxUtil = new MacroSaxParserUtil(mouseEventMacro.getMacro(), "/page/mouse-event-actions/mouse-event-action");
            currentMacroSaxUtil.processCharacters(path(), strCharacters);
        } else if (path().equalsIgnoreCase("/page/active-regions/active-region/region-keyboard-event-actions/region-keyboard-event-action/name")) {
            KeyboardEventMacro keyboardEventMacro = new KeyboardEventMacro(strCharacters);
            currentRegion.getKeyboardEventsProcessor().getKeyboardEventMacros().add(keyboardEventMacro);
            keyboardEventMacro.setEventName(strCharacters);
            currentMacroSaxUtil = new MacroSaxParserUtil(keyboardEventMacro.getMacro(), "/page/active-regions/active-region/region-keyboard-event-actions/region-keyboard-event-action");
            currentMacroSaxUtil.processCharacters(path(), strCharacters);
        } else if (path().equalsIgnoreCase("/page/variable-update-event-actions/variable-update-event-action/name")) {
            VariableUpdateEventMacro variableUpdateEventMacro = new VariableUpdateEventMacro(strCharacters);
            currentPage.getVariableUpdateEventMacros().add(variableUpdateEventMacro);
            variableUpdateEventMacro.setEventName(strCharacters);
            currentMacroSaxUtil = new MacroSaxParserUtil(variableUpdateEventMacro.getMacro(), "/page/variable-update-event-actions/variable-update-event-action");
            currentMacroSaxUtil.processCharacters(path(), strCharacters);
        } else if (currentMacroSaxUtil != null && currentMacroSaxUtil.processCharacters(path(), strCharacters)) {
            return;
        }

        if (currentPage != null) {
            if (currentElement.equalsIgnoreCase("title")) {
                currentPage.setTitle(strCharacters);
            }
            if (currentElement.equalsIgnoreCase("id")) {
                currentPage.setId(strCharacters);
            }
            if (currentElement.equalsIgnoreCase("state-diagram-x")) {
                try {
                    currentPage.setStateDiagramX(Double.parseDouble(strCharacters));
                } catch (Throwable e) {
                    log.error(e);
                }
            }
            if (currentElement.equalsIgnoreCase("state-diagram-y")) {
                try {
                    currentPage.setStateDiagramY(Double.parseDouble(strCharacters));
                } catch (Throwable e) {
                    log.error(e);
                }
            }
            if (currentElement.equalsIgnoreCase("spreadsheet-column-widths")) {
                currentPage.setStrSpreadsheetColumnWidths(strCharacters);
            }
            if (currentElement.equalsIgnoreCase("page-width")) {
                try {
                    currentPage.setPageWidth((int) Double.parseDouble(strCharacters));
                } catch (Throwable e) {
                    log.error(e);
                }
            }
            if (currentElement.equalsIgnoreCase("page-height")) {
                try {
                    currentPage.setPageHeight((int) Double.parseDouble(strCharacters));
                } catch (Throwable e) {
                    log.error(e);
                }
            }
        }
        if (currentElement.equalsIgnoreCase("name")) {
            if (onEntry != null) {
                onEntry.setName(strCharacters);
            } else if (onExit != null) {
                onExit.setName(strCharacters);
            }
        }
        if (this.currentConnector != null) {
            if (currentElement.equalsIgnoreCase("connector-caption")) {
                this.currentConnector.setCaption(strCharacters);
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
                    onEntry.setRepeat(Integer.parseInt(strCharacters));
                } catch (Throwable e) {
                    onEntry.setRepeat(1);
                }
            }
        }

        if (currentElement.equalsIgnoreCase("type")) {
            if (onEntryActionIndex >= 0) {
                strCharacters = Macro.checkOldCommand(strCharacters);
                onEntry.getActions()[onEntryActionIndex][0] = strCharacters;
            } else if (onExitActionIndex >= 0) {
                strCharacters = Macro.checkOldCommand(strCharacters);
                onExit.getActions()[onExitActionIndex][0] = strCharacters;
            }
        }
        if (currentElement.equalsIgnoreCase("param1")) {
            if (onEntryActionIndex >= 0) {
                onEntry.getActions()[onEntryActionIndex][1] = strCharacters;
            } else if (onExitActionIndex >= 0) {
                onExit.getActions()[onExitActionIndex][1] = strCharacters;
            }
        }
        if (currentElement.equalsIgnoreCase("param2")) {
            if (onEntryActionIndex >= 0) {
                onEntry.getActions()[onEntryActionIndex][2] = strCharacters;
            } else if (onExitActionIndex >= 0) {
                onExit.getActions()[onExitActionIndex][2] = strCharacters;
            }
        }
        if (currentElement.equalsIgnoreCase("show-text") && currentRegion != null) {
            currentRegion.setTextField(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("capture-screen-x") && currentRegion != null) {
            currentRegion.setCaptureScreenX(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("capture-screen") && currentRegion != null) {
            currentRegion.setScreenCapturingEnabled(strCharacters.equals("true"));
        }
        if (currentElement.equalsIgnoreCase("capture-screen-mouse-map") && currentRegion != null) {
            currentRegion.setScreenCapturingMouseMappingEnabled(strCharacters.equals("true"));
        }
        if (currentElement.equalsIgnoreCase("capture-screen-y") && currentRegion != null) {
            currentRegion.setCaptureScreenY(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("capture-screen-width") && currentRegion != null) {
            currentRegion.setCaptureScreenWidth(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("capture-screen-height") && currentRegion != null) {
            currentRegion.setCaptureScreenHeight(strCharacters);
        }

        if (currentElement.equalsIgnoreCase("basic-shape") && currentRegion != null) {
            currentRegion.setShape(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("basic-shape-args") && currentRegion != null) {
            currentRegion.setShapeArguments(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("group") && currentRegion != null) {
            currentRegion.setRegionGrouping(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("show-image") && currentRegion != null) {
            currentRegion.setImageUrlField(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("image-index") && currentRegion != null) {
            currentRegion.setImageIndex(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("is-active") && currentRegion != null) {
            currentRegion.setActive(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("type") && currentRegion != null) {
            currentRegion.setType(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("control") && currentRegion != null) {
            if (strCharacters.equals("UMLGraph / Class Diagram / Simple DSL")) {
                strCharacters = "Cascading UML";
            }
            if (strCharacters.equals("UMLGraph / Class Diagram / Cascading UML")) {
                strCharacters = "Cascading UML";
            }
            currentRegion.setWidget(strCharacters);
        }

        if (currentElement.equalsIgnoreCase("control-parameters") && currentRegion != null) {
            currentRegion.setWidgetPropertiesString(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("image-animation-ms") && currentRegion != null) {
            currentRegion.setAnimationFrameRateMs(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("text-area") && currentRegion != null) {
            currentRegion.setText(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("wrap-text") && currentRegion != null) {
            currentRegion.setTextWrapped(strCharacters.equals("true"));
        }
        if (currentElement.equalsIgnoreCase("trim-text") && currentRegion != null) {
            currentRegion.setTextTrimmed(strCharacters.equals("true"));
        }
        if (currentElement.equalsIgnoreCase("is-solid") && currentRegion != null) {
            currentRegion.setWalkThroughEnabled(strCharacters.equals("true"));
        }
        if (currentElement.equalsIgnoreCase("characters-per-line") && currentRegion != null) {
            currentRegion.setCharactersPerLine(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("max-lines") && currentRegion != null) {
            currentRegion.setMaxNumLines(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("trajectory") && currentRegion != null) {
            currentRegion.setTrajectory1(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("control-items") && currentRegion != null) {
            currentRegion.setWidgetItems(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("trajectory2") && currentRegion != null) {
            currentRegion.setTrajectory2(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("image-draw") && currentRegion != null) {
            currentRegion.setImageFile(strCharacters, 0);
        }
        if (currentElement.equalsIgnoreCase("additional-image-draw") && currentRegion != null) {
            currentRegion.getAdditionalImageFileNames().add(strCharacters);
            currentRegion.getAdditionalDrawnImages().add(null);
            currentRegion.getAdditionalDrawnImagesChanged().add(new Boolean(false));
        }
        if (currentElement.equalsIgnoreCase("x") && currentRegion != null) {
            currentRegion.setX(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("visible") && currentRegion != null) {
            currentRegion.setVisible(strCharacters.equalsIgnoreCase("true"));
        }
        if (currentElement.equalsIgnoreCase("layer") && currentRegion != null) {
            currentRegion.setLayer(0);
            try {
                currentRegion.setLayer((int) Double.parseDouble(strCharacters));
            } catch (Exception e) {
            }
        }
        if (currentElement.equalsIgnoreCase("y") && currentRegion != null) {
            currentRegion.setY(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("direct-x1") && currentRegion != null) {
            currentRegion.setX1(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("direct-y2") && currentRegion != null) {
            currentRegion.setY2(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("direct-x2") && currentRegion != null) {
            currentRegion.setX2(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("direct-y2") && currentRegion != null) {
            currentRegion.setY2(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("perspective-x1") && currentRegion != null) {
            currentRegion.setPerspectiveX1(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("perspective-y1") && currentRegion != null) {
            currentRegion.setPerspectiveY1(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("perspective-x2") && currentRegion != null) {
            currentRegion.setPerspectiveX2(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("perspective-y2") && currentRegion != null) {
            currentRegion.setPerspectiveY2(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("perspective-x3") && currentRegion != null) {
            currentRegion.setPerspectiveX3(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("perspective-y3") && currentRegion != null) {
            currentRegion.setPerspectiveY3(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("perspective-x4") && currentRegion != null) {
            currentRegion.setPerspectiveX4(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("perspective-y4") && currentRegion != null) {
            currentRegion.setPerspectiveY4(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("rotate-3d-horizontal") && currentRegion != null) {
            currentRegion.setRotation3DHorizontal(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("rotate-3d-vertical") && currentRegion != null) {
            currentRegion.setRotation3DVertical(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("automatic-perspective") && currentRegion != null) {
            currentRegion.setAutomaticPerspective(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("perspective-depth") && currentRegion != null) {
            currentRegion.setPerspectiveDepth(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("relative-x") && currentRegion != null) {
            currentRegion.setRelativeX(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("region-zoom") && currentRegion != null) {
            currentRegion.setZoom(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("relative-y") && currentRegion != null) {
            currentRegion.setRelativeY(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("trajectory-position") && currentRegion != null) {
            currentRegion.setTrajectoryPosition(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("width") && currentRegion != null) {
            currentRegion.setWidth(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("height") && currentRegion != null) {
            currentRegion.setHeight(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("rotation") && currentRegion != null) {
            currentRegion.setRotation(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("shearX") && currentRegion != null) {
            currentRegion.setShearX(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("shearY") && currentRegion != null) {
            currentRegion.setShearY(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("windowX") && currentRegion != null) {
            currentRegion.setWindowX(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("windowY") && currentRegion != null) {
            currentRegion.setWindowY(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("windowWidth") && currentRegion != null) {
            currentRegion.setWindowWidth(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("windowHeight") && currentRegion != null) {
            currentRegion.setWindowHeight(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("transparency") && currentRegion != null) {
            currentRegion.setTransparency(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("speed") && currentRegion != null) {
            currentRegion.setSpeed(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("motionDirection") && currentRegion != null) {
            currentRegion.setSpeedDirection(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("rotationSpeed") && currentRegion != null) {
            currentRegion.setRotationSpeed(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("pen") && currentRegion != null) {
            currentRegion.setPenWidth(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("move") && currentRegion != null) {
            currentRegion.setMovable(strCharacters.equals("true"));
        }
        if (currentElement.equalsIgnoreCase("rotate") && currentRegion != null) {
            currentRegion.setRotatable(strCharacters.equals("true"));
        }
        if (currentElement.equalsIgnoreCase("resize") && currentRegion != null) {
            currentRegion.setResizable(strCharacters.equals("true"));
        }
        if (currentElement.equalsIgnoreCase("fit-to-box") && currentRegion != null) {
            currentRegion.setFitToBoxEnabled(strCharacters.equals("true"));
        }
        if (currentElement.equalsIgnoreCase("stick-to-trajectory") && currentRegion != null) {
            currentRegion.setStickToTrajectoryEnabled(strCharacters.equals("true"));
        }
        if (currentElement.equalsIgnoreCase("orientation-trajectory") && currentRegion != null) {
            currentRegion.setChangingOrientationOnTrajectoryEnabled(strCharacters.equals("true"));
        }
        if (currentElement.equalsIgnoreCase("font-name") && currentRegion != null) {
            currentRegion.setFontName(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("font-style") && currentRegion != null) {
            currentRegion.setFontStyle(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("font-size") && currentRegion != null) {
            currentRegion.setFontSize(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("font-color") && currentRegion != null) {
            currentRegion.setFontColor(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("fill-color") && currentRegion != null) {
            currentRegion.setFillColor(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("line-color") && currentRegion != null) {
            currentRegion.setLineColor(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("line-style") && currentRegion != null) {
            currentRegion.setLineStyle(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("line-thickness") && currentRegion != null) {
            currentRegion.setLineThickness(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("horizontal-alignment") && currentRegion != null) {
            currentRegion.setHorizontalAlignment(strCharacters);
        }
        if (currentElement.equalsIgnoreCase("vertical-alignment") && currentRegion != null) {
            currentRegion.setVerticalAlignment(strCharacters);
        }
        if (currentElement.startsWith("interaction-event-") && currentRegion != null) {
            if (currentElement.equalsIgnoreCase("interaction-event-region") && currentRegion != null) {
                RegionOverlapEventMacro regionOverlapEventMacro = new RegionOverlapEventMacro("");
                currentRegion.getRegionOverlapEventMacros().add(regionOverlapEventMacro);
                if (this.interactionEventIndex >= 0) {
                    regionOverlapEventMacro.getMacro().getParameters().put(RegionOverlapEventMacro.PARAMETER_REGION_ID, strCharacters);
                    // currentRegion.interactionEvents[interactionEventIndex][0] = strCharacters;
                }
                return;
            } else if (currentElement.equalsIgnoreCase("interaction-event-type") && currentRegion != null) {
                RegionOverlapEventMacro regionOverlapEventMacro = currentRegion.getRegionOverlapEventMacros().get(currentRegion.getRegionOverlapEventMacros().size() - 1);
                if (this.interactionEventIndex >= 0) {
                    regionOverlapEventMacro.setEventName(strCharacters);
                }
                return;
            }
            RegionOverlapEventMacro regionOverlapEventMacro = currentRegion.getRegionOverlapEventMacros().get(currentRegion.getRegionOverlapEventMacros().size() - 1);
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

