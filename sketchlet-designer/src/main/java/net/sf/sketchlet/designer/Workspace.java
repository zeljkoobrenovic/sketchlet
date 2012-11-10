/*
 * Workspace.java
 *
 * Created on April 18, 2006, 10:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer;

import net.sf.sketchlet.common.SimpleProperties;
import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.communicator.ConfigurationData;
import net.sf.sketchlet.communicator.server.AdditionalVariables;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.communicator.server.Variable;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.context.SketchletGraphicsContext;
import net.sf.sketchlet.context.VariablesBlackboardContext;
import net.sf.sketchlet.designer.context.SketchletContextImpl;
import net.sf.sketchlet.designer.context.SketchletGraphicsContextImpl;
import net.sf.sketchlet.designer.context.UtilContextImpl;
import net.sf.sketchlet.designer.context.VariablesBlackboardContextImpl;
import net.sf.sketchlet.designer.data.ActiveRegions;
import net.sf.sketchlet.designer.data.Evaluator;
import net.sf.sketchlet.designer.data.LocalVariable;
import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.designer.programming.macros.Commands;
import net.sf.sketchlet.designer.ui.DidYouKnow;
import net.sf.sketchlet.designer.ui.SketchletDesignerMainPanel;
import net.sf.sketchlet.designer.ui.desktop.ProcessConsolePanel;
import net.sf.sketchlet.designer.ui.desktop.SketchletDesignerSplashScreen;
import net.sf.sketchlet.designer.ui.playback.PlaybackFrame;
import net.sf.sketchlet.designer.ui.playback.PlaybackPanel;
import net.sf.sketchlet.designer.ui.profiles.Profiles;
import net.sf.sketchlet.parser.CellReferenceResolver;
import net.sf.sketchlet.parser.JEParser;
import net.sf.sketchlet.plugin.VariableSpacePlugin;
import net.sf.sketchlet.pluginloader.PluginInstance;
import net.sf.sketchlet.pluginloader.PluginLoader;
import net.sf.sketchlet.util.UtilContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author cuypers
 */
public class Workspace extends JPanel {
    private static final Logger log = Logger.getLogger(Workspace.class);
    public static JFrame mainFrame;
    public static JFrame referenceFrame;
    public static GraphicsConfiguration graphics;
    public static SketchletDesignerMainPanel mainPanel;
    public static JFrame frame;
    public static boolean showGUI = true;
    public static boolean programEnding = false;
    public static Workspace processRunner;
    public static Workspace consolePane;
    public static String filePath;
    public JTabbedPane tabbedPane = new JTabbedPane();
    public static Vector processes = new Vector();
    public static Vector processHandlers = new Vector();
    public static Hashtable processHandlersIdMap = new Hashtable();
    public static Color sketchBackground = new Color(255, 255, 255);
    public static List<PluginInstance> variableSpaces = new Vector<PluginInstance>();
    public static List<String> variableSourcesNames = new Vector<String>();
    public static CountDownLatch pluginsReady = new CountDownLatch(1);
    public static CountDownLatch derivedVariablesReady = new CountDownLatch(1);
    public static CountDownLatch applicationReady = new CountDownLatch(1);
    public static CountDownLatch ready = new CountDownLatch(1);

    public Workspace(String configFileUrl) throws Exception {
        this.processRunner = this;
        URL configURL;
        if (configFileUrl == null) {
            configURL = null;
        } else {
            configFileUrl = this.replaceSystemVariables(configFileUrl);
            configURL = new URL(configFileUrl);
        }

        this.loadProcesses(configURL);

        add(tabbedPane);
    }

    public void addProcess(String id, String command, String workingDirectory, String title, String description, int offset, boolean autoStart,
                           String startCondition, String stopCondition, String outVariable, String inVariable) {
        ProcessConsolePanel panel = new ProcessConsolePanel(id, title, description, command, workingDirectory,
                processes, offset, autoStart,
                startCondition, stopCondition, outVariable, inVariable);
        panel.setParentTabbedPanel(tabbedPane);
        this.processHandlers.add(panel);
        this.processHandlersIdMap.put(id, panel);
        tabbedPane.addTab(title, createImageIcon("resources/middle.gif", ""), panel, description);
    }

    protected void finalize() throws Throwable {
        try {
            this.killProcesses();
        } finally {
            super.finalize();
        }
    }

    public static ImageIcon createImageIcon(String path) {
        return createImageIcon(path, "");
    }

    public static ImageIcon createImageIcon(String path, String description) {
        java.net.URL imgURL = Workspace.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    public void killProcesses() {
        if (programEnding) {
            return;
        }  // already called

        synchronized (Workspace.processes) {
            Workspace.programEnding = true;
            Iterator iterator = this.processes.iterator();

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

            processRunner.tabbedPane.removeAll();

            this.processHandlers.removeAllElements();
            this.processHandlersIdMap.clear();
            this.processes.removeAllElements();
        }

        Workspace.programEnding = false;
    }

    private static void createAndShowGUI(String configFileUrl) throws Exception {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the main window.
        mainFrame = new JFrame("Sketchlet");

        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.setIconImage(Workspace.createImageIcon("resources/sketcify24x24.png", "").getImage());
        //Create and set up the window.
        frame = new JFrame("Sketchlet");
        frame.setIconImage(Workspace.createImageIcon("resources/sketcify24x24.png", "").getImage());
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        //frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        mainFrame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                if (SketchletEditor.editorPanel != null) {
                    int n = JOptionPane.showConfirmDialog(frame,
                            Language.translate("Are you sure you want to exit Sketchlet") + "\n" + Language.translate("and close all windows?"),
                            Language.translate("Exit"),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);

                    if (n != JOptionPane.YES_OPTION) {
                        return;
                    }
                    if (!SketchletEditor.editorPanel.close()) {
                        return;
                    }
                }

                Workspace.mainPanel.saveConfiguration();
                consolePane.killProcesses();

                int w = mainFrame.getWidth();
                int h = mainFrame.getHeight();

                GlobalProperties.set("main-window-size", w + "," + h);
                GlobalProperties.save();

                ApplicationLifecycleCentre.beforeApplicationEnd();

                int attempt = 0;
                while (SketchletEditor.editorPanel != null && attempt++ < 50) {
                    try {
                        Thread.sleep(100);
                    } catch (Exception te) {
                    }
                }
                System.exit(0);
            }
        });

        consolePane = new Workspace(null);
        consolePane.setOpaque(true); //content panes must be opaque
        frame.getContentPane().add(consolePane, BorderLayout.SOUTH);
        TutorialPanel.prepare(frame);

        frame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                mainPanel.consoleButton.setText(Language.translate("Show Details and Console..."));
            }
        });

        mainPanel = new SketchletDesignerMainPanel(frame, consolePane);

        mainFrame.getContentPane().add(mainPanel, BorderLayout.CENTER);

        Dimension sd = Toolkit.getDefaultToolkit().getScreenSize();
        int windowWidth = (int) (sd.getWidth() * 0.6);
        int windowHeight = (int) (sd.getHeight() * 0.6);

        try {
            String strXY = GlobalProperties.get("main-window-size", "");
            String xy[] = strXY.split(",");
            if (xy.length == 2) {
                windowWidth = Integer.parseInt(xy[0]);
                windowHeight = Integer.parseInt(xy[1]);
            }
        } catch (Exception e) {
            log.error(e);
        }

        if (sd.getWidth() <= windowWidth || sd.getHeight() <= windowHeight) {
            mainFrame.setSize((int) (sd.getWidth() * 0.6), (int) (sd.getHeight() * 0.6));
            mainFrame.setExtendedState(mainFrame.getExtendedState() | Frame.MAXIMIZED_BOTH);
        } else {
            mainFrame.setSize(windowWidth, windowHeight);
        }

        Workspace.referenceFrame = mainFrame;

        if (configFileUrl == null) {
            RecentFilesManager.loadRecentFiles();
            RecentFilesManager.loadLastProject();
            if (Workspace.showGUI) {
                RecentFilesManager.populateMenu();
                mainFrame.setVisible(true);
            }
        } else {
            try {
                bCloseOnPlaybackEnd = true;
                consolePane.loadProcesses(new URL(configFileUrl));
                mainPanel.openSketches(true);
            } catch (Exception e) {
                Workspace.closeSplashScreen();
                JOptionPane.showMessageDialog(null, Language.translate("Could nor open the project") + "\n" + configFileUrl, Language.translate("Sketchlet Error"), JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
                log.error(e);
            }
        }
    }

    public static boolean bCloseOnPlaybackEnd = false;

    public static String getJavaHomeDir() {
        String strJavaHome = System.getenv("JAVA_HOME");
        if (strJavaHome == null || strJavaHome.trim().isEmpty() || !(new File(strJavaHome).exists())) {
            strJavaHome = System.getProperty("java.home");
        }

        if (!strJavaHome.endsWith("/") && !strJavaHome.endsWith("\\")) {
            strJavaHome += File.separator;
        }
        return strJavaHome;
    }

    public static String replaceSystemVariables(String commandLine) {
        java.util.Map<String, String> env = System.getenv();

        commandLine = commandLine.replace("%USER_HOME%", System.getProperty("user.home"));
        commandLine = commandLine.replace("$USER_HOME", System.getProperty("user.home"));

        commandLine = commandLine.replace("%JAVA_HOME%", Workspace.getJavaHomeDir());
        commandLine = commandLine.replace("$JAVA_HOME", Workspace.getJavaHomeDir());

        commandLine = commandLine.replace("%USER_NAME%", System.getProperty("user.name"));
        commandLine = commandLine.replace("$USER_NAME", System.getProperty("user.name"));

        String appHome = SketchletContext.getInstance().getApplicationHomeDir();
        String modulesHome = SketchletContextUtils.getTemporaryRuntimeDirectoryPath();
        if (StringUtils.isBlank(modulesHome)) {
            modulesHome = appHome + "modules/";
        }

        commandLine = commandLine.replace("%AMICO_HOME%", appHome);
        commandLine = commandLine.replace("$AMICO_HOME", appHome);

        commandLine = commandLine.replace("%SKETCHIFY_HOME%", appHome);
        commandLine = commandLine.replace("$SKETCHIFY_HOME", appHome);

        commandLine = commandLine.replace("%SKETCHLET_MODULES_HOME%", modulesHome);
        commandLine = commandLine.replace("$SKETCHLET_MODULES_HOME", modulesHome);

        commandLine = commandLine.replace("%SKETCHLET_HOME%", appHome);
        commandLine = commandLine.replace("$SKETCHLET_HOME", appHome);

        for (Map.Entry variable : env.entrySet()) {
            String name = (String) variable.getKey();
            String value = (String) variable.getValue();

            commandLine = commandLine.replace("%" + name + "%", value);
            commandLine = commandLine.replace("$" + name, value);
        }

        return commandLine;
    }

    public static Vector<String> getVariablesWindows(String str) {
        Vector<String> variables = new Vector<String>();

        int n;

        try {
            while ((n = str.indexOf("%")) != -1) {
                int n2 = str.indexOf("%", n + 1);

                if (n2 == -1) {
                    break;
                }

                String var = str.substring(n + 1, n2);
                if (!var.startsWith("=")) {
                    variables.add(var);
                }

                str = str.substring(n2 + 1);
            }
        } catch (Exception e) {
            log.error(e);
        }

        return variables;
    }

    public static Vector<String> getVariablesUnix(String str) {
        Vector<String> variables = new Vector<String>();

        int n = 0;

        try {
            while ((n = str.indexOf("$", n)) != -1) {
                n++;
                if (str.length() > n && (str.charAt(n + 1) == '\\' || str.charAt(n + 1) == '/')) {
                    continue;
                }
                int n2 = str.indexOf(" ", n + 1);

                if (n2 == -1) {
                    String var = str.substring(n + 1);
                    variables.add(var);

                    break;
                }

                String var = str.substring(n + 1, n2);
                variables.add(var);

                str = str.substring(n2 + 1);
            }
        } catch (Exception e) {
            log.error(e);
        }

        return variables;
    }

    static int imageCounter = 0;

    public static BufferedImage createCompatibleImage(int w, int h) {
        return (BufferedImage) getGraphicsEnvironemnt().createCompatibleImage(w, h, Transparency.TRANSLUCENT);
    }

    public static BufferedImage createCompatibleImageCopy(Image img) {
        BufferedImage newImage = createCompatibleImage((int) img.getWidth(null), (int) img.getHeight(null));
        Graphics2D g2 = newImage.createGraphics();
        g2.drawImage(img, 0, 0, null);
        g2.dispose();

        return newImage;
    }

    public static BufferedImage createCompatibleImage(int w, int h, BufferedImage oldImage) {
        if (oldImage == null || oldImage.getWidth() != w || oldImage.getHeight() != h) {
            if (oldImage != null) {
                oldImage.flush();
            }
            return Workspace.createCompatibleImage(w, h);
        } else {
            Workspace.clearImage(oldImage);
            return oldImage;
        }
    }

    public static void clearImage(BufferedImage image) {
        Graphics2D g2 = image.createGraphics();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
        g2.fill(new Rectangle(0, 0, image.getWidth(), image.getHeight()));
        g2.dispose();
    }

    public static BufferedImage createCompatibleImageOpaque(int w, int h) {
        return new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    }

    public static BufferedImage createCompatibleImageOpaque(int w, int h, BufferedImage oldImage) {
        if (oldImage == null || oldImage.getWidth() != w || oldImage.getHeight() != h) {
            if (oldImage != null) {
                oldImage.flush();
            }
            return Workspace.createCompatibleImageOpaque(w, h);
        } else {
            Workspace.clearImage(oldImage);
            return oldImage;
        }
    }

    public static void clearImageOpaque(BufferedImage image) {
        Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fill(new Rectangle(0, 0, image.getWidth(), image.getHeight()));
        g2.dispose();
    }

    public static VolatileImage createCompatibleVolatileImage(int w, int h) {
        return (VolatileImage) getGraphicsEnvironemnt().createCompatibleVolatileImage(w, h, Transparency.TRANSLUCENT);
    }

    public static void graphicsConfiguration() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        graphics = ge.getDefaultScreenDevice().getDefaultConfiguration();
    }

    public static GraphicsConfiguration getGraphicsEnvironemnt() {
        if (graphics == null) {
            graphicsConfiguration();
        }
        return graphics;
    }

    public static void testPorts() {
        ConfigurationData.loadFromURL(null);
        int port = ConfigurationData.tcpPort;
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            serverSocket.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "The port " + port + " is already in use.\n"
                    + "Another Sketchlet instance may be running.\n"
                    + "Only one instance of Sketchlet can run at a time.\n");
            System.exit(-1);
        }
    }

    public static SketchletDesignerSplashScreen splashScreen;

    public static void closeSplashScreen() {
        if (splashScreen != null) {
            splashScreen.setVisible(false);
        }
    }

    public static void main(final String args[]) throws Exception {
        try {
            BasicConfigurator.configure();
            Workspace.graphicsConfiguration();
            VariablesBlackboardContext.setInstance(new VariablesBlackboardContextImpl());
            SketchletContext.setInstance(new SketchletContextImpl());
            SketchletGraphicsContext.setInstance(new SketchletGraphicsContextImpl());
            UtilContext.setInstance(new UtilContextImpl());
            SketchletContextUtils.getSketchletDesignerModulesDir();

            log.info("Application Directory: " + SketchletContext.getInstance().getApplicationHomeDir());
            log.info("Java Home Directory: " + Workspace.getJavaHomeDir());

            GlobalProperties.load();

            int n = GlobalProperties.get("active-profile", -1);
            if (n == -1) {
                Profiles.activeProfile = null;
            } else if (n >= 10000) {
                Profiles.activeProfile = Profiles.getStandardProfiles().elementAt(n - 10000);
            } else {
                Profiles.activeProfile = Profiles.getProfiles().elementAt(n);
            }

            String strLaF = GlobalProperties.get("look-and-feel", "");
            if (strLaF.equalsIgnoreCase("System")) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } else if (strLaF.isEmpty()) {
                boolean bLaf = false;
                for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        bLaf = true;
                        break;
                    }
                }
                if (!bLaf) {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
            } else {
                for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if (info.getName().contains(strLaF)) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error(e);
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }

        Workspace.testPorts();

        Workspace.splashScreen = new SketchletDesignerSplashScreen();

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                try {
                    Workspace.splashScreen.setMessage("Loading plugins...");
                    PluginLoader.loadPlugins();
                    Workspace.splashScreen.setMessage("");

                    InteractionSpace.createDisplaySpace();
                    RecentFilesManager.loadRecentFiles();
                    RecentFilesManager.loadLastProject();

                    String configFileUrl = null;
                    if (args.length > 0) {
                        configFileUrl = args[0];
                    }
                    createAndShowGUI(configFileUrl);
                    SketchletDesignerMainPanel.projectSelectorPanel.populate();
                    mainPanel.sketchletPanel.globalVariablesPanel.enableControls();
                } catch (Exception e) {
                    log.error(e);
                } finally {
                    applicationReady.countDown();
                }
                if (!Workspace.bCloseOnPlaybackEnd) {
                    Workspace.closeSplashScreen();
                    if (GlobalProperties.get("help-start-screen") == null || !GlobalProperties.get("help-start-screen").equalsIgnoreCase("false")) {
                        DidYouKnow.showFrame(Workspace.mainFrame);
                    }
                }
            }
        });
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(new Runnable() {

            public void run() {
                try {
                    applicationReady.await();
                    ApplicationLifecycleCentre.afterApplicationStart();
                    ApplicationLifecycleCentre.afterProjectOpening();
                } catch (InterruptedException ie) {
                    log.error(ie);
                } finally {
                    Workspace.derivedVariablesReady.countDown();
                    Workspace.ready.countDown();
                }
            }
        });
    }

    public static void prepareAdditionalVariables() {

        DataServer.variablesServer.addAdditionalVariables(new AdditionalVariables() {
            public Variable getVariable(String variableName) {
                Page page = Workspace.getPage();
                for (LocalVariable localVariable : page.localVariables) {
                    if (localVariable.getName().equalsIgnoreCase(variableName)) {
                        Variable v = new Variable() {
                            @Override
                            public void save() {
                                savePageVariable(name, value);
                            }
                        };
                        v.name = localVariable.getName();
                        v.value = localVariable.getValue();
                        v.format = localVariable.getFormat();
                        v.timestamp--;
                        return v;
                    }
                }

                return null;
            }


            public void updateVariable(String variableName, String value) {
                savePageVariable(variableName, value);
            }
        });
        DataServer.variablesServer.addAdditionalVariables(new AdditionalVariables() {

            @Override
            public Variable getVariable(String name) {
                if (!name.isEmpty()) {
                    String strCol = name.substring(0, 1);
                    if (strCol.charAt(0) >= 'A' && strCol.charAt(0) <= 'Z') {
                        try {
                            String strRow = name.substring(1);
                            final int col = strCol.charAt(0) - 'A' + 1;
                            final int row = Integer.parseInt(strRow) - 1;
                            Variable v = new Variable() {

                                @Override
                                public void save() {
                                    if (SketchletEditor.editorPanel != null && SketchletEditor.editorPanel.spreadsheetPanel != null) {
                                        SketchletEditor.editorPanel.spreadsheetPanel.model.setValueAt(this.value, row, col);
                                    }
                                }
                            };
                            v.timestamp--;
                            v.name = name;
                            String strValue = getPage().getSpreadsheetCellValue(row, col);
                            if (strValue.startsWith("=")) {
                                strValue = Evaluator.processText(strValue, "", "");
                                while (strValue.startsWith("=")) {
                                    strValue = Evaluator.processText(strValue, "", "");
                                }
                            }
                            v.value = strValue;
                            return v;
                        } catch (Exception e) {
                            //log.error(e);
                        }
                    }
                }
                return null;
            }
        });


        DataServer.variablesServer.addAdditionalVariables(new AdditionalVariables() {

            @Override
            public Variable getVariable(String id) {
                if (!id.isEmpty()) {
                    for (int i = 0; i < Workspace.variableSourcesNames.size(); i++) {
                        String dsName = Workspace.variableSourcesNames.get(i);
                        if (id.startsWith(dsName)) {
                            final PluginInstance ds = Workspace.variableSpaces.get(i);
                            if (ds.getInstance() instanceof VariableSpacePlugin) {
                                id = id.substring(dsName.length() + 1);
                                try {
                                    Variable v = new Variable() {

                                        @Override
                                        public void save() {
                                            ((VariableSpacePlugin) ds.getInstance()).update(name, value);
                                        }
                                    };
                                    v.timestamp--;
                                    v.name = id;
                                    String strValue = ((VariableSpacePlugin) ds.getInstance()).evaluate(id);
                                    if (strValue.startsWith("=")) {
                                        strValue = Evaluator.processText(strValue, "", "");
                                        while (strValue.startsWith("=")) {
                                            strValue = Evaluator.processText(strValue, "", "");
                                        }
                                    }
                                    v.value = strValue;
                                    return v;
                                } catch (Exception e) {
                                }
                            }
                        }
                    }
                }
                return null;
            }
        });

        DataServer.variablesServer.addAdditionalVariables(new AdditionalVariables() {

            @Override
            public Variable getVariable(String name) {
                name = name.trim();
                if (name.startsWith("[") && name.endsWith("]")) {
                    try {
                        Variable v = new Variable() {

                            @Override
                            public void save() {
                                Commands.updateVariableOrProperty(this, name, value, Commands.ACTION_VARIABLE_UPDATE);
                            }
                        };
                        v.timestamp--;
                        v.name = name;
                        ActiveRegions regions = Workspace.getPage().regions;
                        v.value = Evaluator.processRegionReferences(regions, name);
                        return v;
                    } catch (Exception e) {
                        log.error(e);
                    }

                }
                return null;
            }
        });

        JEParser.setResolver(new CellReferenceResolver() {

            @Override
            public String getValue(String strReference) {
                if (!strReference.isEmpty() && SketchletEditor.editorPanel != null) {
                    ActiveRegions regions = Workspace.getPage().regions;

                    strReference = DataServer.populateTemplateSimple(strReference, false);
                    strReference = Evaluator.processRegionReferences(regions, strReference);
                    try {
                        String expression = "";
                        int col = 0;
                        int row = 0;
                        if (DataServer.variablesServer.variableExists(strReference)) {
                            return DataServer.variablesServer.getVariableValue(strReference);
                        }
                        String prevValue = getPage().getSpreadsheetCellValue(row, col);
                        if (!expression.equals(prevValue)) {
                            if (SketchletEditor.editorPanel != null && SketchletEditor.editorPanel.spreadsheetPanel != null) {
                                SketchletEditor.editorPanel.spreadsheetPanel.model.fireTableCellUpdated(row, col);
                            }
                        }
                        return expression;
                    } catch (Exception e) {
                    }
                }
                return "";
            }
        });
    }

    public URL selectFile() {

        Object[] options = {"Create New Configuration", "Open Existing Configuration...", "Exit"};
        int selectedValue = JOptionPane.showOptionDialog(frame, "Sketchlet: ", "Sketchlet",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (selectedValue == 0) {
            return null;
        } else if (selectedValue == 1) {
            return openFile();
        } else {
            System.exit(0);
            return null;
        }
    }

    static JFileChooser fc = new JFileChooser();

    public URL openFile() {
        try {
            //Create a file chooser
            fc.setApproveButtonText("Open Configuration");
            fc.setDialogTitle("Select Sketchlet Workspace Configuration");
            fc.setCurrentDirectory(new File(SketchletContextUtils.sketchletDataDir() + "/conf/processrunner"));
            //In response to a button click:
            int returnVal = fc.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                return file.toURL();
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error(e);
        }
        return null;
    }

    public static void main2(String args[]) throws Exception {
        for (String strCommand : args) {
            Thread.sleep(2000);
        }
    }

    public void reloadProcesses() {
        try {
            this.loadProcesses(new File(Workspace.filePath).toURL(), false);
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

        Workspace.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (!append) {
            Workspace.processRunner.killProcesses();
            filePath = configURL.getFile();

            if (new File(filePath).getParentFile().getName().equals(SketchletContextUtils.sketchletDataDir())) {
                SketchletContextUtils.projectFolder = new File(filePath).getParentFile().getParent() + File.separator;
            } else {
                SketchletContextUtils.projectFolder = new File(filePath).getParent() + File.separator;
            }

            System.setProperty("user.dir", SketchletContextUtils.projectFolder);

            if (!Workspace.bCloseOnPlaybackEnd) {
                RecentFilesManager.addRecentFile(Workspace.filePath);
                RecentFilesManager.saveRecentFiles();
            }
        }

        if (SketchletContextUtils.getCurrentProjectDir() != null && Workspace.mainPanel != null) {
            Workspace.mainPanel.populateSettingsMenu(Workspace.mainPanel.projectSettingsMenu, new File(SketchletContextUtils.getCurrentProjectConfDir()));
        }

        if (configURL.toString().toLowerCase().endsWith(".xml")) {
            loadProcessesXML(configURL, append);
        } else {
            loadProcessesTxT(configURL, append);
        }

        if (!append) {
            try {
                net.sf.sketchlet.communicator.Global.workingDirectory = SketchletContextUtils.getCurrentProjectDir();
                mainPanel.sketchletPanel.restart(URLDecoder.decode(new File(SketchletContextUtils.getCurrentProjectConfDir() + "communicator/config.xml").toURL().toString(), "UTF8"));
                ApplicationLifecycleCentre.afterProjectOpening();
            } catch (Exception e) {
                log.error(e);
            }
        }

        if (Workspace.mainPanel != null) {
            this.mainPanel.enableMenuItems();
            this.mainPanel.enableToolbarItems();

            this.mainPanel.refreshData(append);
            Workspace.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public void loadProcessesXML(URL configURL, boolean append) {
        boolean installFiles = true;
        try {
            DocumentBuilderFactory factory;
            DocumentBuilder builder;
            TransformerFactory tFactory;
            Transformer transformers[];

            try {
                factory = DocumentBuilderFactory.newInstance();
                builder = factory.newDocumentBuilder();
                tFactory = TransformerFactory.newInstance();
            } catch (Exception e) {
                log.error(e);
                return;
            }

            Document docConfig = builder.parse(configURL.openStream());

            XPath xpath = XPathFactory.newInstance().newXPath();

            String expression;
            String str;
            double number;

            if (!append) {
                expression = "/process-runner/@title";
                str = (String) xpath.evaluate(expression, docConfig, XPathConstants.STRING);

                if (this.mainFrame != null) {
                    this.mainFrame.setTitle("Sketchlet: " + str + " (" + SketchletContextUtils.projectFolder + ")");
                }
                if (this.frame != null) {
                    this.frame.setTitle("Control Panel: " + str);
                    this.mainPanel.projectTitle = str;
                }
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

                                // Transfer bytes from in to out
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

        boolean installFiles = true;

        try {
            SimpleProperties props = new SimpleProperties();

            props.loadData(configURL.toExternalForm(), "addprocess");

            if (!append) {
                String title = props.getString("title");

                if (this.mainFrame != null) {
                    this.mainFrame.setTitle("Sketchlet: " + title + " (" + SketchletContextUtils.projectFolder + ")");
                }
                if (this.frame != null) {
                    this.frame.setTitle("Control Panel: " + title);
                    this.mainPanel.projectTitle = title;
                }
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

    public static void openProject(String strPath, boolean append) {
        try {
            DataServer.paused = false;
            if (!strPath.endsWith("\\") && !strPath.endsWith("/")) {
                strPath += File.separator;
            }
            if (!(new File(strPath).exists())) {
                strPath = SketchletContextUtils.getDefaultProjectsRootLocation() + "new project" + File.separator;
            }

            String legacyProjectFileNames[] = {"workspace.txt", "process-runner.xml", "process-starter.xml", "run.xml",
                    "process-starter.txt", "process_starter.txt", "process-runner.txt", "run.txt"
            };

            boolean foundProjectFile = false;
            for (int i = 0; i < legacyProjectFileNames.length; i++) {
                if (new File(strPath + File.separator + "workspace.txt").exists()) {
                    strPath += "workspace.txt";
                    foundProjectFile = true;
                    break;
                }
            }

            if (!foundProjectFile) {
                String strTitle = new File(strPath).getName();
                strPath += SketchletContextUtils.sketchletDataDir() + File.separator + "workspace.txt";
                FileUtils.saveFileText(strPath, "Title " + strTitle);
            }

            processRunner.loadProcesses(new File(strPath).toURL(), append);
        } catch (Exception e) {
            log.error(e);
        }
    }

    private static void savePageVariable(String variableName, String value) {
        Page page = Workspace.getPage();
        for (LocalVariable localVariable : page.localVariables) {
            if (localVariable.getName().equalsIgnoreCase(variableName)) {
                localVariable.setValue(value);
                SketchletEditor.editorPanel.pageVariablesPanel.refreshComponents();
                break;
            }
        }
    }


    public static Page getPage() {
        Page page;
        if ((PlaybackFrame.playbackFrame != null || SketchletEditor.editorPanel.internalPlaybackPanel != null) && PlaybackPanel.currentPage != null) {
            page = PlaybackPanel.currentPage;
        } else {
            page = SketchletEditor.editorPanel.currentPage;
        }
        return page;
    }

    public static Page getMasterSketch() {
        return SketchletEditor.editorPanel != null ? SketchletEditor.editorPanel.getMasterPage() : null;
    }

    public static void initForBatchProcessing() {
        Workspace.graphicsConfiguration();
        VariablesBlackboardContext.setInstance(new VariablesBlackboardContextImpl());
        SketchletContext.setInstance(new SketchletContextImpl());
        SketchletContext.getInstance().setBatchMode(true);
        SketchletGraphicsContext.setInstance(new SketchletGraphicsContextImpl());
        UtilContext.setInstance(new UtilContextImpl());
        PluginLoader.loadPlugins();
    }
}
