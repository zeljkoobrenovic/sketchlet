package net.sf.sketchlet.framework.model.log;

import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;
import net.sf.sketchlet.framework.blackboard.Variable;

import java.awt.*;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class ActivityLog {

    private static long startTime = System.currentTimeMillis();
    private static Vector<LogItem> logItems = new Vector<LogItem>();
    private static Vector<String> startObjects = new Vector<String>();
    private static Vector<String> endObjects = new Vector<String>();

    public static void resetTime() {
        startTime = System.currentTimeMillis();
        logItems.removeAllElements();
        startObjects = getObjects();
    }

    public static Vector<String> getObjects() {
        Vector<String> objects = new Vector<String>();

        for (String variableName : VariablesBlackboard.getInstance().getVariablesList()) {
            Variable v = VariablesBlackboard.getInstance().getVariable(variableName);
            String strObject = "variable " + v.getName() + "," + v.getCount() + "," + v.getValue();
            objects.add(strObject);
        }

        return objects;
    }

    public static void log(String action, String parameters) {
        /*RefreshTime.update();*
        log(action, parameters, null, null);*/
    }

    public static void log(String action, String parameters, String strIcon, Component source) {
        /*long time = System.currentTimeMillis() - startTime;
        LogItem item = new LogItem(time, action, parameters);
        logItems.add(item);*/

        // TutorialPanel.addLine(action, parameters, strIcon, source);
    }

    public static void save() {
        /*try {
            endObjects = getObjects();

            new File(SketchletContextUtils.getCurrentProjectLogDir()).mkdirs();
            PrintWriter out = new PrintWriter(new FileWriter(SketchletContextUtils.getCurrentProjectLogDir() + "session_" + startTime + ".txt"));
            for (LogItem item : logItems) {
                out.println(item.toString());
            }
            out.println();
            out.println("Start State:");
            for (String strObject : startObjects) {
                out.println(strObject);
            }
            out.println();
            out.println("End State:");
            for (String strObject : endObjects) {
                out.println(strObject);
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
}
