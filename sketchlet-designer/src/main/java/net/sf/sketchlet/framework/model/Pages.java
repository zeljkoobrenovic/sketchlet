package net.sf.sketchlet.framework.model;

import net.sf.sketchlet.common.XPathEvaluator;
import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.ui.MessageFrame;
import net.sf.sketchlet.loaders.SketchletsSaxLoader;
import net.sf.sketchlet.framework.model.events.EventMacro;
import net.sf.sketchlet.framework.model.programming.macros.Macros;
import net.sf.sketchlet.framework.model.programming.timers.Timers;
import net.sf.sketchlet.framework.model.programming.timers.curves.Curves;
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

    private Vector<Page> pages = new Vector<Page>();
    private String projectDirectory;
    private static MessageFrame messageFrame;

    public Pages() {
        loadSax();
    }

    public static MessageFrame getMessageFrame() {
        return messageFrame;
    }

    public static void setMessageFrame(MessageFrame messageFrame) {
        Pages.messageFrame = messageFrame;
    }

    public void savePageSorting() {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "sort.txt"));
            for (Page s : this.getPages()) {
                out.println("sketch_" + s.getId() + ".xml");
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            log.error(e);
        }
    }

    public String getNewName() {
        int i = getPages().size() + 1;
        while (true) {
            String name = "Page " + i++;
            boolean nameExists = false;
            for (Page s : this.getPages()) {
                if (s.getTitle().equals(name)) {
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
        for (Page s : this.getPages()) {
            if (s != page && s.getTitle().equals(name)) {
                nameExists = true;
            }
        }

        return nameExists;
    }

    public Page addNewSketch() {

        Page s = new Page("", "");
        s.setTitle(this.getNewName());

        s.setRegions(new ActiveRegions(s));
        s.isChanged();

        getPages().add(s);

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
        for (Page s : this.getPages()) {
            boolean bSave = false;
            for (ActiveRegion region : s.getRegions().getRegions()) {
                for (EventMacro eventMacro : region.mouseProcessor.getMouseEventMacros()) {
                    for (int i = 0; i < eventMacro.getMacro().getActions().length; i++) {
                        String action = (String) eventMacro.getMacro().getActions()[i][0];
                        String param = (String) eventMacro.getMacro().getActions()[i][1];
                        if (action.equalsIgnoreCase(_action) && param.equals(oldName)) {
                            bSave = true;
                            eventMacro.getMacro().getActions()[i][1] = newName;
                        }
                    }
                }
                for (EventMacro eventMacro : region.regionOverlapEventMacros) {
                    for (int i = 0; i < eventMacro.getMacro().getActions().length; i++) {
                        String action = (String) eventMacro.getMacro().getActions()[i][0];
                        String param = (String) eventMacro.getMacro().getActions()[i][1];
                        if (action.equalsIgnoreCase(_action) && param.equals(oldName)) {
                            bSave = true;
                            eventMacro.getMacro().getActions()[i][1] = newName;
                        }
                    }
                }
            }
            bSave = bSave || s.getOnEntryMacro().replaceReferences(_action, oldName, newName);
            bSave = bSave || s.getOnExitMacro().replaceReferences(_action, oldName, newName);

            if (bSave) {
                s.save(false);
            }
        }
    }

    public void dispose() {
        if (getPages() != null) {
            for (Page s : this.getPages()) {
                if (s != null) {
                    s.dispose();
                }
            }
        }
    }

    public Page getSketch(String strSketchName) {
        strSketchName = strSketchName.trim();
        for (Page s : this.getPages()) {
            if (s.getTitle().equalsIgnoreCase(strSketchName)) {
                return s;
            }
        }

        return null;
    }

    public synchronized static Object[][] getSketchInfo() {
        return getSketchInfo(300);
    }

    public synchronized static Object[][] getSketchInfo(int size) {
        // MessageFrame messageFrame = new MessageFrame("Loading sketches...", "...");

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

        // messageFrame.stop();

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

    public void loadSax() {
        Timers.setGlobalTimers(new Timers());
        Macros.globalMacros = new Macros();
        Curves.setGlobalCurves(new Curves());

        this.setProjectDirectory(SketchletContextUtils.getCurrentProjectSkecthletsDir());
        File file = new File(this.getProjectDirectory() + "sketches.xml");

        if (file.exists()) {
            this.setPages(SketchletsSaxLoader.getSketches(this.getProjectDirectory() + "sketches.xml"));
        } else {
            this.setPages(SketchletsSaxLoader.getSketchesFromDir(getProjectDirectory()));
        }

        if (this.getPages() == null || this.getPages().size() == 0) {
            this.addNewSketch();
        }
    }

    public Vector<Page> getPages() {
        return pages;
    }

    public void setPages(Vector<Page> pages) {
        this.pages = pages;
    }

    public String getProjectDirectory() {
        return projectDirectory;
    }

    public void setProjectDirectory(String projectDirectory) {
        this.projectDirectory = projectDirectory;
    }
}