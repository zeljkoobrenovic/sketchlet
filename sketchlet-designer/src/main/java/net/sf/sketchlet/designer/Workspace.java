/*
 * Workspace.java
 *
 * Created on April 18, 2006, 10:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.communicator.ConfigurationData;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.context.SketchletGraphicsContext;
import net.sf.sketchlet.context.VariablesBlackboardContext;
import net.sf.sketchlet.designer.context.SketchletContextImpl;
import net.sf.sketchlet.designer.context.SketchletGraphicsContextImpl;
import net.sf.sketchlet.designer.context.UtilContextImpl;
import net.sf.sketchlet.designer.context.VariablesBlackboardContextImpl;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.designer.editor.ui.DidYouKnow;
import net.sf.sketchlet.designer.editor.ui.SketchletDesignerMainPanel;
import net.sf.sketchlet.designer.editor.ui.desktop.SketchletDesignerSplashScreen;
import net.sf.sketchlet.designer.playback.ui.PlaybackFrame;
import net.sf.sketchlet.designer.playback.ui.PlaybackPanel;
import net.sf.sketchlet.designer.editor.ui.profiles.Profiles;
import net.sf.sketchlet.ioservices.IoServicesHandler;
import net.sf.sketchlet.loaders.pluginloader.PluginInstance;
import net.sf.sketchlet.loaders.pluginloader.PluginLoader;
import net.sf.sketchlet.model.Page;
import net.sf.sketchlet.model.varspaces.VariableSpacesHandler;
import net.sf.sketchlet.util.UtilContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author cuypers
 */
public class Workspace {
    private static final Logger log = Logger.getLogger(Workspace.class);
    private static JFrame mainFrame;
    private static JFrame referenceFrame;
    private static GraphicsConfiguration graphics;
    private static SketchletDesignerMainPanel mainPanel;
    private static JFrame ioServicesFrame;
    private static boolean showGUI = true;
    private static Workspace processRunner;
    private static Workspace consolePane;
    private static String filePath;
    private static Color sketchBackground = new Color(255, 255, 255);
    private static List<PluginInstance> variableSpaces = new Vector<PluginInstance>();
    private static List<String> variableSourcesNames = new Vector<String>();
    private static CountDownLatch pluginsReadyCountDownLatch = new CountDownLatch(1);
    private static CountDownLatch derivedVariablesReadyCountDownLatch = new CountDownLatch(1);
    private static CountDownLatch applicationReadyCountDownLatch = new CountDownLatch(1);
    private static CountDownLatch readyCountDownLatch = new CountDownLatch(1);

    private IoServicesHandler ioServicesHandler = new IoServicesHandler();
    private VariableSpacesHandler variableSpacesHandler = new VariableSpacesHandler();
    private static SketchletDesignerSplashScreen splashScreen;

    public Workspace(String configFileUrl) throws Exception {
        this.setProcessRunner(this);
        URL configURL;
        if (configFileUrl == null) {
            configURL = null;
        } else {
            configFileUrl = this.replaceSystemVariables(configFileUrl);
            configURL = new URL(configFileUrl);
        }

        getIoServicesHandler().loadProcesses(configURL);
    }

    public static CountDownLatch getPluginsReadyCountDownLatch() {
        return pluginsReadyCountDownLatch;
    }

    public static CountDownLatch getDerivedVariablesReadyCountDownLatch() {
        return derivedVariablesReadyCountDownLatch;
    }

    public static CountDownLatch getApplicationReadyCountDownLatch() {
        return applicationReadyCountDownLatch;
    }

    public static CountDownLatch getReadyCountDownLatch() {
        return readyCountDownLatch;
    }

    public static SketchletDesignerSplashScreen getSplashScreen() {
        return splashScreen;
    }

    public static JFrame getMainFrame() {
        return mainFrame;
    }

    public static void setMainFrame(JFrame mainFrame) {
        Workspace.mainFrame = mainFrame;
    }

    public static JFrame getReferenceFrame() {
        return referenceFrame;
    }

    public static void setReferenceFrame(JFrame referenceFrame) {
        Workspace.referenceFrame = referenceFrame;
    }

    public static GraphicsConfiguration getGraphics() {
        return graphics;
    }

    public static void setGraphics(GraphicsConfiguration graphics) {
        Workspace.graphics = graphics;
    }

    public static SketchletDesignerMainPanel getMainPanel() {
        return mainPanel;
    }

    public static void setMainPanel(SketchletDesignerMainPanel mainPanel) {
        Workspace.mainPanel = mainPanel;
    }

    public static JFrame getIoServicesFrame() {
        return ioServicesFrame;
    }

    public static void setIoServicesFrame(JFrame ioServicesFrame) {
        Workspace.ioServicesFrame = ioServicesFrame;
    }

    public static boolean isShowGUI() {
        return showGUI;
    }

    public static void setShowGUI(boolean showGUI) {
        Workspace.showGUI = showGUI;
    }

    public static Workspace getProcessRunner() {
        return processRunner;
    }

    public static void setProcessRunner(Workspace processRunner) {
        Workspace.processRunner = processRunner;
    }

    public static Workspace getConsolePane() {
        return consolePane;
    }

    public static void setConsolePane(Workspace consolePane) {
        Workspace.consolePane = consolePane;
    }

    public static String getFilePath() {
        return filePath;
    }

    public static void setFilePath(String filePath) {
        Workspace.filePath = filePath;
    }

    public static Color getSketchBackground() {
        return sketchBackground;
    }

    public static void setSketchBackground(Color sketchBackground) {
        Workspace.sketchBackground = sketchBackground;
    }

    public static List<PluginInstance> getVariableSpaces() {
        return variableSpaces;
    }

    public static void setVariableSpaces(List<PluginInstance> variableSpaces) {
        Workspace.variableSpaces = variableSpaces;
    }

    public static List<String> getVariableSourcesNames() {
        return variableSourcesNames;
    }

    public static void setVariableSourcesNames(List<String> variableSourcesNames) {
        Workspace.variableSourcesNames = variableSourcesNames;
    }

    protected void finalize() throws Throwable {
        try {
            getIoServicesHandler().killProcesses();
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

    private static void createAndShowGUI(String configFileUrl) throws Exception {
        JFrame.setDefaultLookAndFeelDecorated(true);

        setMainFrame(new JFrame("Sketchlet"));

        getMainFrame().setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        getMainFrame().setIconImage(Workspace.createImageIcon("resources/sketcify24x24.png", "").getImage());

        setIoServicesFrame(new JFrame("I/O Services"));
        getIoServicesFrame().setIconImage(Workspace.createImageIcon("resources/sketcify24x24.png", "").getImage());
        getIoServicesFrame().setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        getMainFrame().addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                if (SketchletEditor.getInstance() != null) {
                    int n = JOptionPane.showConfirmDialog(getMainFrame(),
                            Language.translate("Are you sure you want to exit Sketchlet") + "\n" + Language.translate("and close all windows?"),
                            Language.translate("Exit"),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);

                    if (n != JOptionPane.YES_OPTION) {
                        return;
                    }
                    if (!SketchletEditor.getInstance().close()) {
                        return;
                    }
                }

                Workspace.getMainPanel().saveConfiguration();
                getConsolePane().getIoServicesHandler().killProcesses();

                int w = getMainFrame().getWidth();
                int h = getMainFrame().getHeight();

                GlobalProperties.set("main-window-size", w + "," + h);
                GlobalProperties.save();

                ApplicationLifecycleCentre.beforeApplicationEnd();

                int attempt = 0;
                while (SketchletEditor.getInstance() != null && attempt++ < 50) {
                    try {
                        Thread.sleep(100);
                    } catch (Exception te) {
                    }
                }
                System.exit(0);
            }
        });

        setConsolePane(new Workspace(null));
        getConsolePane().getIoServicesHandler().setOpaque(true); //content panes must be opaque
        getIoServicesFrame().getContentPane().add(getConsolePane().getIoServicesHandler(), BorderLayout.SOUTH);

        getIoServicesFrame().addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                getMainPanel().consoleButton.setText(Language.translate("Show Details and Console..."));
            }
        });

        setMainPanel(new SketchletDesignerMainPanel(getIoServicesFrame(), getConsolePane()));

        getMainFrame().getContentPane().add(getMainPanel(), BorderLayout.CENTER);

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
            getMainFrame().setSize((int) (sd.getWidth() * 0.6), (int) (sd.getHeight() * 0.6));
            getMainFrame().setExtendedState(getMainFrame().getExtendedState() | Frame.MAXIMIZED_BOTH);
        } else {
            getMainFrame().setSize(windowWidth, windowHeight);
        }

        Workspace.setReferenceFrame(getMainFrame());

        if (configFileUrl == null) {
            RecentFilesManager.loadRecentFiles();
            RecentFilesManager.loadLastProject();
            if (Workspace.isShowGUI()) {
                RecentFilesManager.populateMenu();
                getMainFrame().setVisible(true);
            }
        } else {
            try {
                bCloseOnPlaybackEnd = true;
                getConsolePane().getIoServicesHandler().loadProcesses(new URL(configFileUrl));
                getMainPanel().openSketches(true);
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

    public static BufferedImage createCompatibleImage(int w, int h) {
        return getGraphicsEnvironemnt().createCompatibleImage(w, h, Transparency.TRANSLUCENT);
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

    public static VolatileImage createCompatibleVolatileImage(int w, int h) {
        return getGraphicsEnvironemnt().createCompatibleVolatileImage(w, h, Transparency.TRANSLUCENT);
    }

    public static void graphicsConfiguration() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        setGraphics(ge.getDefaultScreenDevice().getDefaultConfiguration());
    }

    public static GraphicsConfiguration getGraphicsEnvironemnt() {
        if (getGraphics() == null) {
            graphicsConfiguration();
        }
        return getGraphics();
    }

    public static void testPorts() {
        ConfigurationData.loadFromURL(null);
        int port = ConfigurationData.getTcpPort();
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

    public static void closeSplashScreen() {
        if (getSplashScreen() != null) {
            getSplashScreen().setVisible(false);
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
                    Workspace.getSplashScreen().setMessage("Loading plugins...");
                    PluginLoader.loadPlugins();
                    Workspace.getSplashScreen().setMessage("");

                    InteractionSpace.createDisplaySpace();
                    RecentFilesManager.loadRecentFiles();
                    RecentFilesManager.loadLastProject();

                    String configFileUrl = null;
                    if (args.length > 0) {
                        configFileUrl = args[0];
                    }
                    createAndShowGUI(configFileUrl);
                    SketchletDesignerMainPanel.projectSelectorPanel.populate();
                    getMainPanel().sketchletPanel.globalVariablesPanel.enableControls();
                } catch (Exception e) {
                    log.error(e);
                } finally {
                    getApplicationReadyCountDownLatch().countDown();
                }
                if (!Workspace.bCloseOnPlaybackEnd) {
                    Workspace.closeSplashScreen();
                    if (GlobalProperties.get("help-start-screen") == null || !GlobalProperties.get("help-start-screen").equalsIgnoreCase("false")) {
                        DidYouKnow.showFrame(Workspace.getMainFrame());
                    }
                }
            }
        });
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(new Runnable() {

            public void run() {
                try {
                    getApplicationReadyCountDownLatch().await();
                    ApplicationLifecycleCentre.afterApplicationStart();
                    ApplicationLifecycleCentre.afterProjectOpening();
                } catch (InterruptedException ie) {
                    log.error(ie);
                } finally {
                    Workspace.getDerivedVariablesReadyCountDownLatch().countDown();
                    Workspace.getReadyCountDownLatch().countDown();
                }
            }
        });
    }

    public static void openProject(String strPath, boolean append) {
        try {
            DataServer.setPaused(false);
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

            getProcessRunner().getIoServicesHandler().loadProcesses(new File(strPath).toURL(), append);
        } catch (Exception e) {
            log.error(e);
        }
    }

    public static Page getPage() {
        Page page;
        if ((PlaybackFrame.playbackFrame != null || SketchletEditor.getInstance().getInternalPlaybackPanel() != null) && PlaybackPanel.currentPage != null) {
            page = PlaybackPanel.currentPage;
        } else {
            page = SketchletEditor.getInstance().getCurrentPage();
        }
        return page;
    }

    public static Page getMasterSketch() {
        return SketchletEditor.getInstance() != null ? SketchletEditor.getInstance().getMasterPage() : null;
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

    public IoServicesHandler getIoServicesHandler() {
        return ioServicesHandler;
    }

    public VariableSpacesHandler getVariableSpacesHandler() {
        return variableSpacesHandler;
    }

}
