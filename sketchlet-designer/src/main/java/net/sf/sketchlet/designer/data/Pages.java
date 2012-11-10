/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.data;

import net.sf.sketchlet.common.XPathEvaluator;
import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.programming.macros.Macros;
import net.sf.sketchlet.designer.programming.timers.Timers;
import net.sf.sketchlet.designer.programming.timers.curves.Curves;
import net.sf.sketchlet.designer.ui.MessageFrame;
import org.apache.log4j.Logger;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * @author cuypers
 */
public class Pages {
    private static final Logger log = Logger.getLogger(Pages.class);

    public Vector<Page> pages = new Vector<Page>();
    public String projectDirectory;

    public Pages() {
        // load();
        loadSax();
    }

    public void saveSort() {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "sort.txt"));
            for (Page s : this.pages) {
                out.println("sketch_" + s.getId() + ".xml");
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            log.error(e);
        }
    }

    public String getNewName() {
        int i = pages.size() + 1;
        while (true) {
            String name = "Page " + i++;
            boolean nameExists = false;
            for (Page s : this.pages) {
                if (s.title.equals(name)) {
                    nameExists = true;
                }
            }

            if (!nameExists) {
                return name;
            }
        }
    }

    public boolean sketchNameExists(String name, Page page) {
        boolean nameExists = false;
        for (Page s : this.pages) {
            if (s != page && s.title.equals(name)) {
                nameExists = true;
            }
        }

        return nameExists;
    }

    public Page addNewSketch() {

        Page s = new Page("", "");
        s.title = this.getNewName();

        /*s.imageFile[0] = new File(WorkspaceUtils.getCurrentProjectSkecthletsDir() + "Sketch_" + s.getId() + ".png");
        
        for (int i = 1; i < 9; i++) {
        s.imageFile[i] = new File(WorkspaceUtils.getCurrentProjectSkecthletsDir() + "Sketch_" + s.getId() + "_" + (i + 1) + ".png");
        }*/

        s.regions = new ActiveRegions(s);
        s.isChanged();

        pages.add(s);

        s.initCache();

        return s;
    }

    public void replaceReferencesSketches(String oldSketchName, String newSketchName) {
        replaceReferences("Go to page", oldSketchName, newSketchName);
        Macros.globalMacros.replaceReferences("Go to page", oldSketchName, oldSketchName);
    }

    public void replaceReferencesMacros(String oldMacroName, String newMacroName) {
        replaceReferences("Call Macro", oldMacroName, newMacroName);
        replaceReferences("Start Action", oldMacroName, newMacroName);
        replaceReferences("Stop Action", oldMacroName, newMacroName);
        Macros.globalMacros.replaceReferences("Call Macro", oldMacroName, newMacroName);
        Macros.globalMacros.replaceReferences("Start Action", oldMacroName, newMacroName);
        Macros.globalMacros.replaceReferences("Stop Action", oldMacroName, newMacroName);
    }

    public void replaceReferencesTimers(String oldTimerName, String newTimerName) {
        replaceReferences("Start Timer", oldTimerName, newTimerName);
        Macros.globalMacros.replaceReferences("Start Timer", oldTimerName, newTimerName);
    }

    public void replaceReferences(String _action, String oldName, String newName) {
        for (Page s : this.pages) {
            boolean bSave = false;
            for (ActiveRegion region : s.regions.regions) {
                for (EventMacro eventMacro : region.mouseProcessor.mouseEventMacros) {
                    for (int i = 0; i < eventMacro.getMacro().actions.length; i++) {
                        String action = (String) eventMacro.getMacro().actions[i][0];
                        String param = (String) eventMacro.getMacro().actions[i][1];
                        if (action.equalsIgnoreCase(_action) && param.equals(oldName)) {
                            bSave = true;
                            eventMacro.getMacro().actions[i][1] = newName;
                        }
                    }
                }
                for (EventMacro eventMacro : region.regionOverlapEventMacros) {
                    for (int i = 0; i < eventMacro.getMacro().actions.length; i++) {
                        String action = (String) eventMacro.getMacro().actions[i][0];
                        String param = (String) eventMacro.getMacro().actions[i][1];
                        if (action.equalsIgnoreCase(_action) && param.equals(oldName)) {
                            bSave = true;
                            eventMacro.getMacro().actions[i][1] = newName;
                        }
                    }
                }
            }
            bSave = bSave || s.onEntryMacro.replaceReferences(_action, oldName, newName);
            bSave = bSave || s.onExitMacro.replaceReferences(_action, oldName, newName);

            if (bSave) {
                s.save(false);
            }
        }
    }

    public void dispose() {
        if (pages != null) {
            for (Page s : this.pages) {
                if (s != null) {
                    s.dispose();
                }
            }
        }
    }

    public Page getSketch(String strSketchName) {
        strSketchName = strSketchName.trim();
        for (Page s : this.pages) {
            if (s.title.equalsIgnoreCase(strSketchName)) {
                return s;
            }
        }

        return null;
    }

    public synchronized static Object[][] getSketchInfo() {
        return getSketchInfo(300);
    }

    public synchronized static Object[][] getSketchInfo(int size) {
        // MessageFrame msgFrame = new MessageFrame("Loading sketches...", "...");

        Object[][] data = null;
        try {
            File file = new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "sketches.xml");

            if (file.exists()) {
                XPathEvaluator xp = new XPathEvaluator();
                xp.createDocumentFromFile(file);

                NodeList sketcheNodes = xp.getNodes("/sketches/sketch");

                if (sketcheNodes != null) {
                    data = new Object[sketcheNodes.getLength()][2];
                    for (int i = 0; i < sketcheNodes.getLength(); i++) {
                        data[i][0] = xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/title");

                        try {
                            BufferedImage img = Workspace.createCompatibleImage(size, size);
                            data[i][1] = img;

                            Graphics2D g2 = (Graphics2D) img.createGraphics();

                            File imageFile = new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/image-file"));
                            if (imageFile.exists()) {
                                g2.drawImage(ImageIO.read(imageFile), 0, 0, size, size, 0, 0, size * 4, size * 4, null);
                            }

                            File annotationFile = new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/annotation-file"));
                            if (annotationFile.exists()) {
                                g2.drawImage(ImageIO.read(annotationFile), 0, 0, size, size, 0, 0, size * 4, size * 4, null);
                            }

                            NodeList actionNodes = xp.getNodes("/sketches/sketch[position()=" + (i + 1) + "]/action");
                            if (actionNodes != null) {
                                for (int j = 0; j < actionNodes.getLength(); j++) {
                                    int x1 = xp.getInteger("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/region/@x1");
                                    int y1 = xp.getInteger("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/region/@y1");
                                    int x2 = xp.getInteger("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/region/@x2");
                                    int y2 = xp.getInteger("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/region/@y2");

                                    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
                                    x1 = (int) ((300.0 / 1200) * x1);
                                    y1 = (int) ((300.0 / 1200) * y1);
                                    x2 = (int) ((300.0 / 1200) * x2);
                                    y2 = (int) ((300.0 / 1200) * y2);

                                    g2.setColor(new Color(255, 0, 0, 50));
                                    g2.fillRect(x1, y1, x2 - x1, y2 - y1);
                                }
                            }
                            g2.dispose();
                        } catch (Exception e) {
                        }

                    }
                }
            } else {
                return getSketchInfoFromDir();
            }
        } catch (Exception e) {
            log.error(e);
        }

        // msgFrame.stop();

        return data;
    }

    public synchronized static Object[][] getSketchInfoFromDir() {
        return getSketchInfoFromDir(300, 1200);
    }

    public synchronized static Object[][] getSketchInfoFromDir(int size, int imageBounds) {
        Object[][] data = null;
        try {
            Vector<String> sketchFiles = SketchletsSaxLoader.getSketchFiles(SketchletContextUtils.getCurrentProjectSkecthletsDir());
            data = new Object[sketchFiles.size()][2];
            int i = 0;
            for (String strFile : sketchFiles) {
                File file = new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + strFile);

                String strThumbFile = SketchletContextUtils.getCurrentProjectSkecthletsDir() + strFile.replace("sketch_", "Sketch_").replace(".xml", "_thumbnail.png");
                File thumbFile = new File(strThumbFile);

                XPathEvaluator xp = new XPathEvaluator();
                xp.createDocumentFromFile(file);

                data[i][0] = xp.getString("/sketch/title");

                if (thumbFile.exists()) {
                    try {
                        BufferedImage img = Workspace.createCompatibleImage(size, size);
                        BufferedImage image = ImageIO.read(thumbFile);
                        Graphics2D g2 = img.createGraphics();
                        g2.drawImage(image, 0, 0, null, null);
                        g2.dispose();
                        data[i][1] = img;
                    } catch (Exception e) {
                        log.error(e);
                    }
                } else {
                    File drawImageFile = null;

                    try {
                        BufferedImage img = Workspace.createCompatibleImage(size, size);
                        data[i][1] = img;

                        Graphics2D g2 = (Graphics2D) img.createGraphics();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        File imageFile = new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + xp.getString("/sketch/image-file"));
                        if (imageFile.exists() && imageFile.getName().endsWith(".png")) {
                            g2.drawImage(ImageIO.read(imageFile), 0, 0, size, size, 0, 0, imageBounds, imageBounds, null);
                        }
                        File annotationFile = new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + xp.getString("/sketch/annotation-file"));
                        if (annotationFile.exists() && annotationFile.getName().endsWith(".png")) {
                            g2.drawImage(ImageIO.read(annotationFile), 0, 0, size, size, 0, 0, imageBounds, imageBounds, null);
                        }
                        NodeList actionNodes = xp.getNodes("/sketch/action");
                        if (actionNodes != null) {
                            for (int j = 0; j < actionNodes.getLength(); j++) {
                                int x1 = xp.getInteger("/sketch/action[position()=" + (j + 1) + "]/region/@x1");
                                int y1 = xp.getInteger("/sketch/action[position()=" + (j + 1) + "]/region/@y1");
                                int x2 = xp.getInteger("/sketch/action[position()=" + (j + 1) + "]/region/@x2");
                                int y2 = xp.getInteger("/sketch/action[position()=" + (j + 1) + "]/region/@y2");

                                String strDrawImage = xp.getString("/sketch/action[position()=" + (j + 1) + "]/image-draw");

                                Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
                                double ratio = ((double) size / imageBounds);
                                x1 = (int) (ratio * x1);
                                y1 = (int) (ratio * y1);
                                x2 = (int) (ratio * x2);
                                y2 = (int) (ratio * y2);

                                drawImageFile = new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + strDrawImage);

                                if (drawImageFile.exists() && !strDrawImage.equals("")) {
                                    g2.drawImage(ImageIO.read(drawImageFile), x1, y1, x2 - x1, y2 - y1, null);
                                }

                                g2.setColor(new Color(255, 0, 0, 50));
                                g2.fillRect(x1, y1, x2 - x1, y2 - y1);

                            }
                        }
                        g2.dispose();
                    } catch (Exception e) {
                        if (drawImageFile != null) {
                        }
                        log.error(e);
                    }
                }
                i++;
            }
        } catch (Exception e) {
            log.error(e);
        }

        return data;
    }

    public static MessageFrame msgFrame;

    public void loadSax() {
        Timers.globalTimers = new Timers();
        Macros.globalMacros = new Macros();
        Curves.globalCurves = new Curves();

        this.projectDirectory = SketchletContextUtils.getCurrentProjectSkecthletsDir();
        File file = new File(this.projectDirectory + "sketches.xml");

        if (file.exists()) {
            this.pages = SketchletsSaxLoader.getSketches(this.projectDirectory + "sketches.xml");
        } else {
            this.pages = SketchletsSaxLoader.getSketchesFromDir(projectDirectory);
        }

        if (this.pages == null || this.pages.size() == 0) {
            this.addNewSketch();
        }
    }

    /*    public void load() {
    Cursor originalCursor = Workspace.mainFrame.getCursor();
    Workspace.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    try {
    Thread.sleep(10);
    Timers.globalTimers = new Timers();
    Macros.globalMacros = new Macros();
    
    this.projectDirectory = WorkspaceUtils.getCurrentProjectSkecthletsDir();
    File file = new File(this.projectDirectory + "sketches.xml");
    
    if (file.exists()) {
    XPathEvaluator xp = new XPathEvaluator();
    xp.createDocumentFromFile(file);
    
    NodeList sketcheNodes = xp.getNodes("/sketches/sketch");
    
    if (sketcheNodes != null) {
    for (int i = 0; i < sketcheNodes.getLength(); i++) {
    if (msgFrame != null) {
    msgFrame.setMessage("Loading sketch " + (i + 1) + "/" + sketcheNodes.getLength());
    }
    Sketch s = new Sketch("", "");
    
    s.title = xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/title");
    s.setId(xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/id"));
    s.stateDiagramX = xp.getDouble("/sketches/sketch[position()=" + (i + 1) + "]/state-diagram-x");
    s.stateDiagramY = xp.getDouble("/sketches/sketch[position()=" + (i + 1) + "]/state-diagram-y");
    s.imageFile[0] = new File(WorkspaceUtils.getCurrentProjectSkecthletsDir() + xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/image-file"));
    s.imageFile[1] = new File(WorkspaceUtils.getCurrentProjectSkecthletsDir() + xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/annotation-file"));
    s.actions = new ActiveRegions(freeHand, s);
    
    s.onEntryMacro = new Macro();
    s.onEntryMacro.name = xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/on-entry/name");
    
    for (int j = 0; j < s.onEntryMacro.actions.length; j++) {
    s.onEntryMacro.actions[j][0] = xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/on-entry/action[position()=" + (j + 1) + "]/type");
    s.onEntryMacro.actions[j][1] = xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/on-entry/action[position()=" + (j + 1) + "]/param1");
    s.onEntryMacro.actions[j][2] = xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/on-entry/action[position()=" + (j + 1) + "]/param2");
    }
    
    s.onExitMacro = new Macro();
    s.onExitMacro.name = xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/on-entry/name");
    
    for (int j = 0; j < s.onExitMacro.actions.length; j++) {
    s.onExitMacro.actions[j][0] = xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/on-exit/action[position()=" + (j + 1) + "]/type");
    s.onExitMacro.actions[j][1] = xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/on-exit/action[position()=" + (j + 1) + "]/param1");
    s.onExitMacro.actions[j][2] = xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/on-exit/action[position()=" + (j + 1) + "]/param2");
    }
    
    s.eventHandler = new StateEventHandler(s);
    
    for (int j = 0; j < s.eventHandler.actions.length; j++) {
    s.eventHandler.actions[j][0] = xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/state-event-handler/when[position()=" + (j + 1) + "]/variable");
    s.eventHandler.actions[j][1] = xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/state-event-handler/when[position()=" + (j + 1) + "]/equals");
    s.eventHandler.actions[j][2] = xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/state-event-handler/when[position()=" + (j + 1) + "]/do");
    s.eventHandler.actions[j][3] = xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/state-event-handler/when[position()=" + (j + 1) + "]/param1");
    s.eventHandler.actions[j][4] = xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/state-event-handler/when[position()=" + (j + 1) + "]/param2");
    }
    
    NodeList actionNodes = xp.getNodes("/sketches/sketch[position()=" + (i + 1) + "]/action");
    if (actionNodes != null) {
    for (int j = 0; j < actionNodes.getLength(); j++) {
    ActiveRegion a = new ActiveRegion(freeHand, s.actions);
    Node actionNode = actionNodes.item(j);
    a.textField.setSelectedItem(xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/show-text"));
    a.imageUrlField.setSelectedItem(xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/show-image"));
    a.setImageFile(xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/image-draw"), 0);
    
    a.transformations[0][1] = (xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/transform/x"));
    a.transformations[1][1] = (xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/transform/y"));
    a.transformations[2][1] = (xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/transform/width"));
    a.transformations[3][1] = (xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/transform/height"));
    a.transformations[4][1] = (xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/transform/rotation"));
    a.transformations[5][1] = (xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/transform/shearX"));
    a.transformations[6][1] = (xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/transform/shearY"));
    a.transformations[7][1] = (xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/transform/windowX"));
    a.transformations[8][1] = (xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/transform/windowY"));
    a.transformations[9][1] = (xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/transform/windowWidth"));
    a.transformations[10][1] = (xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/transform/windowHeight"));
    a.transformations[11][1] = (xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/transform/transparency"));
    a.transformations[12][1] = (xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/transform/speed"));
    a.transformations[13][1] = (xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/transform/rotationSpeed"));
    a.transformations[14][1] = (xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/transform/pen"));
    
    a.fitToBox.setSelected((xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/alignment/fit-to-box").equals("true")));
    a.horizontalAlign.setSelectedItem(xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/alignment/horizontal-alignment"));
    a.verticalAlign.setSelectedItem(xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/alignment/vertical-alignment"));
    
    a.canMove.setSelected(xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/movable/move").equals("true"));
    a.canRotate.setSelected(xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/movable/rotate").equals("true"));
    a.canResize.setSelected(xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/movable/resize").equals("true"));
    
    for (int ut = 0; ut < a.updateTransformations.length; ut++) {
    a.updateTransformations[ut][1] = (xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/movable/update/dimension[position()=" + (ut + 1) + "]/@variable"));
    a.updateTransformations[ut][2] = (xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/movable/update/dimension[position()=" + (ut + 1) + "]/@min"));
    a.updateTransformations[ut][3] = (xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/movable/update/dimension[position()=" + (ut + 1) + "]/@max"));
    a.updateTransformations[ut][4] = (xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/movable/update/dimension[position()=" + (ut + 1) + "]/@start"));
    a.updateTransformations[ut][5] = (xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/movable/update/dimension[position()=" + (ut + 1) + "]/@end"));
    }
    
    a.x1 = xp.getInteger("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/region/@x1");
    a.y1 = xp.getInteger("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/region/@y1");
    a.x2 = xp.getInteger("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/region/@x2");
    a.y2 = xp.getInteger("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/region/@y2");
    a.rotation = xp.getDouble("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/region/@rotation");
    s.actions.regions.add(a);
    
    NodeList eventNodes = xp.getNodes("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/event");
    if (eventNodes != null) {
    for (int k = 0; k < eventNodes.getLength(); k++) {
    if (k > a.events.length) {
    break;
    }
    a.events[k][0] = xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/event[position()=" + (k + 1) + "]/@type");
    
    String goToSketch = xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/event[position()=" + (k + 1) + "]/goto-sketch");
    String variable = xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/event[position()=" + (k + 1) + "]/update/@variable");
    String operation = xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/event[position()=" + (k + 1) + "]/update/@operation");
    String update = xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/event[position()=" + (k + 1) + "]/update");
    
    if (!goToSketch.equals("") || !variable.equals("")) {
    if (!goToSketch.equals("")) {
    a.events[k][1] = "Go to page";
    a.events[k][2] = goToSketch;
    a.events[k][3] = "";
    } else {
    a.events[k][1] = "Variable Update";
    a.events[k][2] = variable;
    a.events[k][3] = update;
    }
    } else {
    a.events[k][1] = xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/event[position()=" + (k + 1) + "]/action");
    a.events[k][2] = xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/event[position()=" + (k + 1) + "]/param1");
    a.events[k][3] = xp.getString("/sketches/sketch[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/event[position()=" + (k + 1) + "]/param2");
    }
    }
    }
    }
    }
    
    sketches.add(s);
    }
    }
    } else {
    this.addNewSketch();
    }
    } catch (Exception e) {
    log.error(e);
    }
    
    Workspace.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
     */
    /*
    public void save() {
    try {
    PrintWriter out = new PrintWriter(new FileWriter(projectDirectory + "sketches.xml"));
    out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    out.println("<sketches>");
    for (Sketch s : this.sketches) {
    s.save(out);
    }
    out.println("</sketches>");
    
    out.flush();
    out.close();
    
    Workspace.mainPanel.refreshSketches();
    } catch (Exception e) {
    log.error(e);
    }
    }*/
}
