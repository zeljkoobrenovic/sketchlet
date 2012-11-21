package net.sf.sketchlet.ioservices;

import net.sf.sketchlet.common.SimpleProperties;
import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.designer.ApplicationLifecycleCentre;
import net.sf.sketchlet.designer.RecentFilesManager;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.ui.desktop.ProcessConsolePanel;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: zeljko
 * Date: 13-11-12
 * Time: 16:07
 * To change this template use File | Settings | File Templates.
 */
public class IoServicesHandler extends JPanel {
    private static final Logger log = Logger.getLogger(IoServicesHandler.class);
    private JTabbedPane tabbedPane = new JTabbedPane();
    private static List processes = new Vector();
    private static List processHandlers = new Vector();
    private static Hashtable processHandlersIdMap = new Hashtable();
    private static boolean programEnding = false;

    public IoServicesHandler() {
        add(getTabbedPane());
    }

    public static List getProcesses() {
        return processes;
    }

    public static void setProcesses(List processes) {
        IoServicesHandler.processes = processes;
    }

    public static List getProcessHandlers() {
        return processHandlers;
    }

    public static void setProcessHandlers(List processHandlers) {
        IoServicesHandler.processHandlers = processHandlers;
    }

    public static Hashtable getProcessHandlersIdMap() {
        return processHandlersIdMap;
    }

    public static void setProcessHandlersIdMap(Hashtable processHandlersIdMap) {
        IoServicesHandler.processHandlersIdMap = processHandlersIdMap;
    }

    public static boolean isProgramEnding() {
        return programEnding;
    }

    public static void setProgramEnding(boolean programEnding) {
        IoServicesHandler.programEnding = programEnding;
    }

    public void addProcess(String id, String command, String workingDirectory, String title, String description, int offset, boolean autoStart,
                           String startCondition, String stopCondition, String outVariable, String inVariable) {
        ProcessConsolePanel panel = new ProcessConsolePanel(id, title, description, command, workingDirectory,
                getProcesses(), offset, autoStart,
                startCondition, stopCondition, outVariable, inVariable);
        panel.setParentTabbedPanel(getTabbedPane());
        this.getProcessHandlers().add(panel);
        this.getProcessHandlersIdMap().put(id, panel);
        getTabbedPane().addTab(title, Workspace.createImageIcon("resources/middle.gif", ""), panel, description);
    }

    public void reloadProcesses() {
        try {
            this.loadProcesses(new File(Workspace.getFilePath()).toURL(), false);
        } catch (Exception e) {
            log.error(e);
        }
    }

    public void loadProcesses(File file, boolean append) {
        try {
            this.loadProcesses(file.toURL(), append);
        } catch (Exception e) {
            log.error(e);
        }
    }

    public void loadProcesses(URL configURL) {
        this.loadProcesses(configURL, false);
    }

    public void loadProcesses(URL configURL, boolean append) {
        if (configURL == null) {
            return;
        }

        Workspace.getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (!append) {
            killProcesses();
            Workspace.setFilePath(configURL.getFile());

            if (new File(Workspace.getFilePath()).getParentFile().getName().equals(SketchletContextUtils.sketchletDataDir())) {
                SketchletContextUtils.setProjectFolder(new File(Workspace.getFilePath()).getParentFile().getParent() + File.separator);
            } else {
                SketchletContextUtils.setProjectFolder(new File(Workspace.getFilePath()).getParent() + File.separator);
            }

            System.setProperty("user.dir", SketchletContextUtils.getProjectFolder());

            if (!Workspace.bCloseOnPlaybackEnd) {
                RecentFilesManager.addRecentFile(Workspace.getFilePath());
                RecentFilesManager.saveRecentFiles();
            }
        }

        if (SketchletContextUtils.getCurrentProjectDir() != null && Workspace.getMainPanel() != null) {
            Workspace.getMainPanel().populateSettingsMenu(Workspace.getMainPanel().projectSettingsMenu, new File(SketchletContextUtils.getCurrentProjectConfDir()));
        }

        if (configURL.toString().toLowerCase().endsWith(".xml")) {
            loadProcessesXML(configURL, append);
        } else {
            loadProcessesTxT(configURL, append);
        }

        if (!append) {
            try {
                net.sf.sketchlet.communicator.Global.setWorkingDirectory(SketchletContextUtils.getCurrentProjectDir());
                Workspace.getMainPanel().sketchletPanel.restart(URLDecoder.decode(new File(SketchletContextUtils.getCurrentProjectConfDir() + "communicator/config.xml").toURL().toString(), "UTF8"));
                ApplicationLifecycleCentre.afterProjectOpening();
            } catch (Exception e) {
                log.error(e);
            }
        }

        if (Workspace.getMainPanel() != null) {
            Workspace.getMainPanel().enableMenuItems();
            Workspace.getMainPanel().enableToolbarItems();

            Workspace.getMainPanel().refreshData(append);
            Workspace.getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public void loadProcessesXML(URL configURL, boolean append) {
        boolean installFiles = true;
        try {
            DocumentBuilderFactory factory;
            DocumentBuilder builder;

            try {
                factory = DocumentBuilderFactory.newInstance();
                builder = factory.newDocumentBuilder();
            } catch (Exception e) {
                log.error(e);
                return;
            }

            Document docConfig = builder.parse(configURL.openStream());

            XPath xpath = XPathFactory.newInstance().newXPath();

            String expression;
            String str;

            if (!append) {
                expression = "/process-runner/@title";
                str = (String) xpath.evaluate(expression, docConfig, XPathConstants.STRING);

                if (Workspace.getMainFrame() != null) {
                    Workspace.getMainFrame().setTitle("Sketchlet: " + str + " (" + SketchletContextUtils.getProjectFolder() + ")");
                }
                Workspace.getMainPanel().projectTitle = str;
            }

            if (installFiles) {
                expression = "/process-runner/file-dependency/file";
                NodeList files = (NodeList) xpath.evaluate(expression, docConfig, XPathConstants.NODESET);

                for (int i = 0; i < files.getLength(); i++) {
                    Node file = files.item(i);

                    String copyFile = (String) xpath.evaluate("@copy-on-install", file, XPathConstants.STRING);

                    if (copyFile.equalsIgnoreCase("true") || copyFile.equalsIgnoreCase("yes")) {
                        String fileName = (String) xpath.evaluate("@name", file, XPathConstants.STRING);
                        String inputDirectory = (String) xpath.evaluate("@directory", file, XPathConstants.STRING);
                        String installDirectory = (String) xpath.evaluate("@install-directory", file, XPathConstants.STRING);

                        String inputPath = inputDirectory + "/" + fileName;
                        String installPath = installDirectory + "/" + fileName;

                        installDirectory = Workspace.replaceSystemVariables(installDirectory);
                        inputPath = Workspace.replaceSystemVariables(inputPath);
                        installPath = Workspace.replaceSystemVariables(installPath);

                        try {
                            File installDir = new File(installDirectory);
                            installDir.mkdirs();
                            File installFile = new File(installPath);
                            if (installFile.createNewFile()) {
                                InputStream in = new FileInputStream(inputPath);
                                OutputStream out = new FileOutputStream(installPath);

                                byte[] buf = new byte[1024];
                                int len;
                                while ((len = in.read(buf)) > 0) {
                                    out.write(buf, 0, len);
                                }
                                in.close();
                                out.close();
                            }
                        } catch (Exception e) {
                            log.error(e);
                        }
                    }
                }
            }

            expression = "/process-runner/process";
            NodeList processes = (NodeList) xpath.evaluate(expression, docConfig, XPathConstants.NODESET);

            for (int i = 0; i < processes.getLength(); i++) {
                Node process = processes.item(i);

                String title = (String) xpath.evaluate("@title", process, XPathConstants.STRING);
                String id = (String) xpath.evaluate("@id", process, XPathConstants.STRING);
                String workingDirectory = (String) xpath.evaluate("@working-directory", process, XPathConstants.STRING);

                String strAutoStart = (String) xpath.evaluate("@auto-start", process, XPathConstants.STRING);
                boolean autoStart = !(strAutoStart.equals("no") || strAutoStart.equals("false"));

                if (id.equals("")) {
                    id = "" + i;
                }

                String description = (String) xpath.evaluate("@description", process, XPathConstants.STRING);
                String command = (String) xpath.evaluate(".", process, XPathConstants.STRING);

                int offset = 0;
                String strOffset = (String) xpath.evaluate("@timeOffsetMs", process, XPathConstants.STRING);

                if (strOffset != null && !strOffset.equals("")) {
                    offset = Integer.parseInt(strOffset);
                }

                String startCondition = (String) xpath.evaluate("@start-condition", process, XPathConstants.STRING);
                String stopCondition = (String) xpath.evaluate("@stop-condition", process, XPathConstants.STRING);
                String outVariable = (String) xpath.evaluate("@out-variable", process, XPathConstants.STRING);
                String inVariable = (String) xpath.evaluate("@in-variable", process, XPathConstants.STRING);

                this.addProcess(id, command, workingDirectory, title, description, offset, autoStart,
                        startCondition, stopCondition, outVariable, inVariable);
            }

        } catch (XPathExpressionException xpee) {
            log.error(xpee);
        } catch (SAXException sxe) {
            log.error(sxe);
        } catch (IOException ioe) {
            log.error(ioe);
        } catch (Exception e) {
            log.error(e);
        }
    }

    public void loadProcessesTxT(URL configURL, boolean append) {
        if (configURL == null) {
            return;
        }

        try {
            SimpleProperties props = new SimpleProperties();

            props.loadData(configURL.toExternalForm(), "addprocess");

            if (!append) {
                String title = props.getString("title");

                if (Workspace.getMainFrame() != null) {
                    Workspace.getMainFrame().setTitle("Sketchlet: " + title + " (" + SketchletContextUtils.getProjectFolder() + ")");
                }
                Workspace.getMainPanel().projectTitle = title;
            }

            for (int i = 0; i < props.getCount("addprocess"); i++) {
                String processTitle = props.getString("AddProcess", i, "ProcessTitle");
                String id = props.getString("AddProcess", i);

                boolean autoStart = props.getBoolean("AddProcess", i, "AutoStart");

                if (id.equals("")) {
                    id = "" + i;
                }

                String description = props.getString("AddProcess", i, "Description");
                String command = props.getString("AddProcess", i, "CommandLine");
                String workingDirectory = props.getString("AddProcess", i, "WorkingDirectory");

                String startCondition = props.getString("AddProcess", i, "StartCondition");
                String stopCondition = props.getString("AddProcess", i, "StopCondition");
                String outVariable = props.getString("AddProcess", i, "OutVariable");
                String inVariable = props.getString("AddProcess", i, "InVariable");

                int offset = props.getInteger("AddProcess", i, "TimeOffsetMs");

                this.addProcess(id, command, workingDirectory, processTitle, description, offset, autoStart,
                        startCondition, stopCondition, outVariable, inVariable);
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    public void killProcesses() {
        if (isProgramEnding()) {
            return;
        }  // already called

        synchronized (getProcesses()) {
            setProgramEnding(true);
            Iterator iterator = this.getProcesses().iterator();

            while (iterator.hasNext()) {
                try {
                    Process process = (Process) iterator.next();
                    if (process != null) {
                        process.destroy();
                    }
                } catch (Exception e) {
                    log.error(e);
                }
            }

            getTabbedPane().removeAll();

            this.getProcessHandlers().clear();
            this.getProcessHandlersIdMap().clear();
            this.getProcesses().clear();
        }

        setProgramEnding(false);
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public void setTabbedPane(JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
    }
}
