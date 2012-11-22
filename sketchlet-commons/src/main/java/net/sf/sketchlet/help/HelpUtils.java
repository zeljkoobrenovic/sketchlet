/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.help;

import net.sf.sketchlet.common.QuotedStringTokenizer;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.context.SketchletContext;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Map;

/**
 * @author zobrenovic
 */
public class HelpUtils {
    private static final Logger log = Logger.getLogger(HelpUtils.class);
    private static HelpInterface helpInterface;

    public static void openHelpFile(String strID) {
        openHelpFile("Help", strID);
    }

    public static void openHelpFile(String title, String strID) {
        if (getHelpInterface() != null) {
            getHelpInterface().openByID(strID);
            return;
        }
        String strPath = HelpUtils.getAMICOHelpDir() + strID + ".html";
        String strTemplate = HelpUtils.getAMICOConfDir() + "help/note.html";

        try {
            File file = new File(strPath);
            File fileTemplate = new File(strTemplate);
            if (!file.exists()) {
                if (fileTemplate.exists()) {
                    FileUtils.copyFile(fileTemplate, file);
                } else {
                    FileUtils.saveFileText(strPath, "");
                }
            }

            String command = "\"$JAVA_HOME/bin/java\" -jar \"$AMICO_HOME/bin/tools/htmleditor/ekit.jar\" -jar com.hexidec.ekit.Ekit";
            command += " -t \"-f" + strPath + "\"";
            // command += " \"-c" + strStyle + "\" ";
            command = HelpUtils.replaceSystemVariables(command);

            String args[] = QuotedStringTokenizer.parseArgs(command);

            log.info("Executing external process: " + command);

            ProcessBuilder processBuilder = new ProcessBuilder(args);
            final Process process = processBuilder.start();

            new Thread(new Runnable() {

                public void run() {
                    try {
                        process.waitFor();
                    } catch (Exception e) {
                    }
                }
            }).start();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public static String replaceSystemVariables(String commandLine) {
        java.util.Map<String, String> env = System.getenv();

        for (Map.Entry variable : env.entrySet()) {
            String name = (String) variable.getKey();
            String value = (String) variable.getValue();

            commandLine = commandLine.replace("%" + name + "%", value);
            commandLine = commandLine.replace("$" + name, value);
        }

        commandLine = commandLine.replace("%USER_HOME%", System.getProperty("user.home"));
        commandLine = commandLine.replace("$USER_HOME", System.getProperty("user.home"));

        commandLine = commandLine.replace("%JAVA_HOME%", System.getProperty("java.home"));
        commandLine = commandLine.replace("$JAVA_HOME", System.getProperty("java.home"));

        commandLine = commandLine.replace("%USER_NAME%", System.getProperty("user.name"));
        commandLine = commandLine.replace("$USER_NAME", System.getProperty("user.name"));

        commandLine = commandLine.replace("\\/", "/");


        return commandLine;
    }

    public static void openFile(File file) {
        try {
            String strPath = file.getPath();
            String command = "\"$JAVA_HOME/bin/java\" -jar \"$AMICO_HOME/bin/tools/htmleditor/ekit.jar\" -jar com.hexidec.ekit.Ekit";
            command += " -t \"-f" + strPath + "\"";
            // command += " \"-c" + strStyle + "\" ";
            command = replaceSystemVariables(command);

            String args[] = QuotedStringTokenizer.parseArgs(command);

            log.info("Executing external process: " + command);

            ProcessBuilder processBuilder = new ProcessBuilder(args);
            final Process process = processBuilder.start();

            new Thread(new Runnable() {

                public void run() {
                    try {
                        process.waitFor();
                    } catch (Exception e) {
                    }
                }
            }).start();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public static String getAMICOHome() {
        return SketchletContext.getInstance().getApplicationHomeDir();
    }

    public static String getAMICOHelpDir() {
        return getAMICOHome() + "help" + File.separator;
    }

    public static String getAMICOConfDir() {
        return getAMICOHome() + "conf" + File.separator;
    }

    public static HelpInterface getHelpInterface() {
        return helpInterface;
    }

    public static void setHelpInterface(HelpInterface helpInterface) {
        HelpUtils.helpInterface = helpInterface;
    }
}
