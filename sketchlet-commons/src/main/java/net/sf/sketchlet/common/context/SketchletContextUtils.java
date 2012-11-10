/*
 * Utils.java
 *
 * Created on April 9, 2008, 5:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.sketchlet.common.context;

import net.sf.sketchlet.common.QuotedStringTokenizer;
import net.sf.sketchlet.common.Refresh;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.common.system.PlatformManager;
import net.sf.sketchlet.context.SketchletContext;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author cuypers
 */
public class SketchletContextUtils {
    private static final Logger log = Logger.getLogger(SketchletContextUtils.class);
    public static String projectFolder;
    public static int httpBlogPort = 8090;
    public static int httpProjectPort = 8091;
    private static String sketchletDir = ".amico";

    /**
     * Creates a new instance of Utils
     */
    public SketchletContextUtils() {
    }

    public static String sketchletDataDir() {
        return sketchletDir;
    }

    public static String getSketchletDesignerHome() {
        return SketchletContext.getInstance() != null ? SketchletContext.getInstance().getApplicationHomeDir() : System.getenv("SKETCHLET_HOME") + "/";
    }

    public static boolean isInteger(String strInteger) {
        try {
            Integer.parseInt(strInteger);
        } catch (NumberFormatException nfe) {
            return false;
        }

        return true;
    }

    public static final void copyInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) >= 0)
            out.write(buffer, 0, len);
        in.close();
        out.close();
    }

    public static final void unpackDirectoryFromJar(String jarFile, String dirName, File outDir) {
        Enumeration entries;
        ZipFile zipFile;
        try {
            zipFile = new ZipFile(jarFile);
            entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if (!entry.getName().equals(dirName) && !entry.getName().startsWith(dirName + "/")) {
                    continue;
                }
                if (entry.isDirectory()) {
                    File dir = new File(outDir, entry.getName());
                    dir.mkdir();
                    dir.deleteOnExit();
                    continue;
                }
                File file = new File(outDir, entry.getName());
                file.deleteOnExit();
                copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(file)));
            }
            zipFile.close();
        } catch (IOException ioe) {
            System.err.println("Unhandled exception:");
            ioe.printStackTrace();
            return;
        }
    }


    private static String confDirPath = null;
    private static String helpDirPath = null;
    private static String modulesDirPath = null;

    public static String getTemporaryRuntimeDirectoryPath() {
        return modulesDirPath;
    }

    public static String getSketchletDesignerConfDir() {
        if (confDirPath != null) {
            return confDirPath;
        }
        confDirPath = getSketchletDesignerHome() + "conf" + File.separator;
        File dir = new File(confDirPath);
        if (!dir.exists()) {
            try {
                dir = File.createTempFile("sketchletConf", "");
                dir.delete();
                dir.mkdirs();
                dir.deleteOnExit();

                System.out.println("Unpacking configuration files in the " + dir.getPath());

                File jar = new File(SketchletContextUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath());
                unpackDirectoryFromJar(jar.getPath(), "conf", dir);

                confDirPath = dir.getPath() + "/conf" + File.separator;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return confDirPath;
    }


    public static String getSketchletDesignerHelpDir() {
        if (helpDirPath != null) {
            return helpDirPath;
        }
        helpDirPath = getSketchletDesignerHome() + "help" + File.separator;
        File dir = new File(helpDirPath);
        if (!dir.exists()) {
            try {
                dir = File.createTempFile("sketchletHelp", "");
                dir.delete();
                dir.mkdirs();
                dir.deleteOnExit();

                System.out.println("Unpacking help files in the " + dir.getPath());

                File jar = new File(SketchletContextUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath());
                unpackDirectoryFromJar(jar.getPath(), "help", dir);

                helpDirPath = dir.getPath() + "/help" + File.separator;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return helpDirPath;
    }

    public static String getSketchletDesignerTutorialsDir() {
        return getSketchletDesignerHelpDir() + "tutorials" + File.separator;
    }

    public static String getSketchletDesignerHTMLTutorialsDir() {
        return getSketchletDesignerTutorialsDir() + "html" + File.separator;
    }

    public static String getSketchletDesignerProjectTemplatesDir() {
        return getSketchletDesignerConfDir() + "templates" + File.separator + "Projects" + File.separator;
    }

    public static String getSketchletDesignerCodeSnippetsDir(String subDir) {
        return getSketchletDesignerConfDir() + "templates" + File.separator + "code_snippets" + File.separator + subDir + File.separator;
    }

    public static String getSketchletDesignerModulesDir() {
        if (modulesDirPath != null) {
            return modulesDirPath;
        }
        modulesDirPath = getSketchletDesignerHome() + "modules" + File.separator;
        File dir = new File(modulesDirPath);
        if (!dir.exists()) {
            try {
                dir = File.createTempFile("sketchletModules", "");
                dir.delete();
                dir.mkdirs();
                dir.deleteOnExit();

                System.out.println("Unpacking modules files in the " + dir.getPath());

                File jar = new File(SketchletContextUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath());
                unpackDirectoryFromJar(jar.getPath(), "modules", dir);
                unpackDirectoryFromJar(jar.getPath(), "bin", dir);

                modulesDirPath = dir.getPath() + "/modules" + File.separator;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return modulesDirPath;
    }

    public static String getSketchletDesignerScriptTemplatesDir() {
        return getSketchletDesignerConfDir() + "templates" + File.separator + "scripts" + File.separator;
    }

    public static String getCurrentProjectDir() {
        return projectFolder;
    }

    public static String getCurrentProjectDirName() {
        if (projectFolder == null) {
            return "";
        }
        return new File(projectFolder).getName();
    }

    public static String getCurrentProjectFile() {
        if (projectFolder == null) {
            return null;
        } else {
            return getCurrentProjectDir() + sketchletDataDir() + File.separator + "workspace.txt";
        }
    }

    public static String getCurrentProjectConfDir() {
        if (projectFolder == null) {
            return "";
        }

        return projectFolder + sketchletDataDir() + File.separator + "conf" + File.separator;
    }

    public static String getCurrentProjectScriptsDir() {
        if (projectFolder == null) {
            return "";
        }

        return projectFolder + sketchletDataDir() + File.separator + "scripts" + File.separator;
    }

    public static String getCurrentProjectLogDir() {
        if (projectFolder == null) {
            return "";
        }

        return projectFolder + sketchletDataDir() + File.separator + "sessions" + File.separator;
    }

    public static String getCurrentProjectTeamDir() {
        if (projectFolder == null) {
            return "";
        }

        return projectFolder + sketchletDataDir() + File.separator + "team" + File.separator;
    }

    public static String getCurrentProjectNotebookDir() {
        if (projectFolder == null) {
            return "";
        }

        String legacyDir = projectFolder + sketchletDataDir() + File.separator + "sketchbook" + File.separator;
        if (new File(legacyDir).exists()) {
            return legacyDir;
        }

        String strDir = projectFolder + sketchletDataDir() + File.separator + "blog" + File.separator;

        if (!new File(strDir).exists()) {
            new File(strDir).mkdirs();
        }

        return strDir;
    }

    public static String getCurrentProjectMouseAndKeyboardActionsDir() {
        if (projectFolder == null) {
            return "";
        }

        return projectFolder + sketchletDataDir() + File.separator + "mouse_keyboard" + File.separator;
    }

    /*
    public static String getCurrentProjectHistoryDir() {
        if (projectFolder == null) {
            return "";
        }

        String projectName = "unknown";
        try {
            File pf = new File(projectFolder);
            projectName = pf.getName();
        } catch (Exception e) {
        }
        // String strDir = WorkspaceUtils.getDefaultProjectsRootLocation() + ".archive" + File.separator + projectName + File.separator;
        String strDir = SketchletContextUtils.getDefaultProjectsRootLocation() + ".archive" + File.separator;
        try {
            File hd = new File(strDir);
            if (!hd.exists()) {
                hd.mkdirs();
            }
        } catch (Exception e) {
        }
        return strDir;
    }*/

    public static String getCurrentProjectOriginalDir() {
        if (projectFolder == null) {
            return "";
        }
        String strDir = getCurrentProjectDir() + ".original" + File.separator;
        if (!new File(strDir).exists()) {
            new File(strDir).mkdirs();
        }

        return strDir;
    }

    public static String getCurrentProjectTempDir() {
        if (projectFolder == null) {
            return "";
        }
        String strDir = getCurrentProjectDir() + ".temp" + File.separator;
        if (!new File(strDir).exists()) {
            new File(strDir).mkdirs();
        }

        return strDir;
    }

    public static String getCurrentProjectOriginalDir2() {
        if (projectFolder == null) {
            return "";
        }
        String strDir = getCurrentProjectDir() + ".original" + File.separator;
        if (!new File(strDir).exists()) {
            new File(strDir).mkdirs();
        }

        return strDir;
    }

    public static String getCurrentProjectSkecthletsDir() {
        if (projectFolder == null) {
            return "";
        }

        String strDir = projectFolder + sketchletDataDir() + File.separator + "sketches" + File.separator;

        if (!new File(strDir).exists()) {
            new File(strDir).mkdirs();
        }

        return strDir;
    }

    /*public static String getCurrentProjectHistoryDir(String sketchId) {
        String projectName = "unknown";
        try {
            File pf = new File(projectFolder);
            projectName = pf.getName();
        } catch (Exception e) {
        }
        String strDir = SketchletContextUtils.getDefaultProjectsRootLocation() + ".history" + File.separator + projectName + File.separator;
        try {
            File hd = new File(strDir);
            if (!hd.exists()) {
                hd.mkdirs();
            }
        } catch (Exception e) {
        }
        return strDir;
    }*/

    public static String getSketchletDesignerTemplateFilesDir() {
        return getSketchletDesignerConfDir() + "templates" + File.separator;
    }

    public static String getCommandFromFile(String filename) {
        String configurationFile = getSketchletDesignerConfDir() + "" + File.separator + filename;

        String command = "";

        try {
            BufferedReader in = new BufferedReader(new FileReader(configurationFile));

            String line;

            while ((line = in.readLine()) != null) {
                line = line.trim();

                if (!line.startsWith("#")) {
                    command += line;
                }
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        command = replaceSystemVariables(command);

        return command;
    }

    public static void openWebBrowser(URL url) {
        openWebBrowser(url.toString());
    }

    public static void openWebBrowser(String strURL) {
        String strOpener = PlatformManager.getDefaultFileOpenerCommand();

        strOpener = strOpener.replace("$f", strURL);

        try {
            Runtime.getRuntime().exec(strOpener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * public static void openWebBrowser2(String strURL) { try { String command
     * = getCommandFromFile("web_browser.txt");
     *
     * command += " \"" + strURL + "\"";
     *
     * String args[] = QuotedStringTokenizer.parseArgs(command);
     *
     * ProcessBuilder processBuilder = new ProcessBuilder(args); Process process
     * = processBuilder.start(); } catch (Exception e) { e.printStackTrace(); }
    }
     */
    public static String getDefaultProjectsRootLocation() {
        String strDefaultLocation = FileUtils.getFileText(SketchletContextUtils.getSketchletDesignerConfDir() + "" + File.separator + "default_project_location.txt");
        strDefaultLocation = replaceSystemVariables(strDefaultLocation.trim());
        if (!strDefaultLocation.endsWith("/") && !strDefaultLocation.endsWith("\\")) {
            strDefaultLocation += File.separator;
        }

        File fileLoc = new File(strDefaultLocation);
        try {
            fileLoc.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!fileLoc.exists()) {
            log.error("Could not open '" + strDefaultLocation + "'");
            strDefaultLocation = replaceSystemVariables("%USER_HOME%/SketchletProjects");
        }

        return strDefaultLocation;
    }

    public static String getApplicationSettingsDir() {
        return replaceSystemVariables("%USER_HOME%/.sketchlet/");
    }

    public static String getUserLibraryLocation() {
        return replaceSystemVariables("%USER_HOME%/.sketchlet/.library/");
    }

    public static String editImages(String strImageFiles, final Refresh refresher, final int index) {
        String imageEditorCommandLine = SketchletContextUtils.getCommandFromFile("image_editor.txt");

        try {
            if (imageEditorCommandLine == null || imageEditorCommandLine.trim().equals("")) {
                imageEditorCommandLine = "\"$JAVA_HOME/bin/java\" -jar \"$SKETCHLET_HOME/bin/tools/imageeditor/ImageEditor.jar\"";
            }

            imageEditorCommandLine += " " + strImageFiles;

            imageEditorCommandLine = replaceSystemVariables(imageEditorCommandLine);

            String args[] = QuotedStringTokenizer.parseArgs(imageEditorCommandLine);

            log.info("Executing external process: " + imageEditorCommandLine);

            ProcessBuilder processBuilder = new ProcessBuilder(args);
            final Process process = processBuilder.start();

            if (refresher != null) {
                new Thread(new Runnable() {

                    public void run() {
                        try {
                            process.waitFor();
                            refresher.refreshImage(index);
                        } catch (Exception e) {
                        }
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error starting Image Editor.";
        }

        return "";

    }

    static Font font = null;

    public static Font getDefaultSketchFont() {
        if (font == null) {
            try {
                font = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(SketchletContextUtils.getSketchletDesignerConfDir() + "fonts" + File.separator + "sketch.ttf"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (font == null) {
                // font = new Font("Comic Sans MS", Font.PLAIN, 12);
                // font = font.deriveFont(Font.BOLD);
            }
        }

        return font;
    }

    public static String replaceSystemVariables(String commandLine) {
        java.util.Map<String, String> env = System.getenv();

        commandLine = commandLine.replace("%USER_HOME%", System.getProperty("user.home"));
        commandLine = commandLine.replace("$USER_HOME", System.getProperty("user.home"));

        commandLine = commandLine.replace("%JAVA_HOME%", System.getProperty("java.home"));
        commandLine = commandLine.replace("$JAVA_HOME", System.getProperty("java.home"));

        for (Map.Entry variable : env.entrySet()) {
            String name = (String) variable.getKey();
            String value = (String) variable.getValue();

            commandLine = commandLine.replace("%" + name + "%", value);
            commandLine = commandLine.replace("$" + name, value);
        }

        commandLine = commandLine.replace("\\/", "/");

        return commandLine;
    }
}
