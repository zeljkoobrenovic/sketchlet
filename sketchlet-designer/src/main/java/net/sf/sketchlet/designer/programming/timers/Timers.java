/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.programming.timers;

import net.sf.sketchlet.common.XPathEvaluator;
import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.programming.timers.events.TimelineEvent;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class Timers {

    public Vector<Timer> timers = new Vector<Timer>();
    public static Timers globalTimers = new Timers();

    public Timers() {
        load();
    }

    public void load() {
        try {
            timers.removeAllElements();

            File file = new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "timers.xml");

            if (file.exists()) {
                XPathEvaluator xp = new XPathEvaluator();
                xp.createDocumentFromFile(file);

                NodeList sketcheNodes = xp.getNodes("/timers/timer");

                if (sketcheNodes != null) {
                    for (int i = 0; i < sketcheNodes.getLength(); i++) {
                        Timer t = new Timer();

                        t.name = xp.getString("/timers/timer[position()=" + (i + 1) + "]/name");
                        // t.durationInSec = xp.getDouble("/timers/timer[position()=" + (i + 1) + "]/duration");
                        // t.pauseBefore = xp.getDouble("/timers/timer[position()=" + (i + 1) + "]/pause-before");
                        // t.pauseAfter = xp.getDouble("/timers/timer[position()=" + (i + 1) + "]/pause-after");

                        t.strDurationInSec = xp.getString("/timers/timer[position()=" + (i + 1) + "]/duration");
                        t.strPauseBefore = xp.getString("/timers/timer[position()=" + (i + 1) + "]/pause-before");
                        t.strPauseAfter = xp.getString("/timers/timer[position()=" + (i + 1) + "]/pause-after");

                        /*if (Double.isNaN(t.pauseBefore)) {
                        t.pauseBefore = 0.0;
                        }
                        if (Double.isNaN(t.pauseAfter)) {
                        t.pauseAfter = 0.0;
                        }*/

                        t.loop = xp.getString("/timers/timer[position()=" + (i + 1) + "]/loop").equals("true");
                        t.pulsar = xp.getString("/timers/timer[position()=" + (i + 1) + "]/pulsar").equals("true");
                        t.defaultCurve = xp.getString("/timers/timer[position()=" + (i + 1) + "]/default-curve");
                        t.bResetAtEnd = xp.getString("/timers/timer[position()=" + (i + 1) + "]/reset-at-end").equals("true");
                        t.tabIndex = xp.getInteger("/timers/timer[position()=" + (i + 1) + "]/tab-index");

                        int timerCount = xp.getInteger("count(/timers/timer[position()=" + (i + 1) + "]/variable)");
                        for (int j = 0; j < t.variables.length && j < timerCount; j++) {
                            t.variables[j][0] = xp.getString("/timers/timer[position()=" + (i + 1) + "]/variable[position()=" + (j + 1) + "]/name");
                            t.variables[j][1] = xp.getString("/timers/timer[position()=" + (i + 1) + "]/variable[position()=" + (j + 1) + "]/start");
                            t.variables[j][2] = xp.getString("/timers/timer[position()=" + (i + 1) + "]/variable[position()=" + (j + 1) + "]/end");
                            //t.variables[j][3] = xp.getString("/timers/timer[position()=" + (i + 1) + "]/variable[position()=" + (j + 1) + "]/prefix");
                            //t.variables[j][4] = xp.getString("/timers/timer[position()=" + (i + 1) + "]/variable[position()=" + (j + 1) + "]/postfix");
                            t.variables[j][3] = xp.getString("/timers/timer[position()=" + (i + 1) + "]/variable[position()=" + (j + 1) + "]/format");
                            t.variables[j][4] = xp.getString("/timers/timer[position()=" + (i + 1) + "]/variable[position()=" + (j + 1) + "]/curve");
                        }

                        int timelineEventsCounter = xp.getInteger("count(/timers/timer[position()=" + (i + 1) + "]/timeline/timeline-event)");
                        if (timelineEventsCounter >= 2) {
                            t.timeline.events.removeAllElements();
                            for (int j = 0; j < timelineEventsCounter; j++) {
                                double time = xp.getDouble("/timers/timer[position()=" + (i + 1) + "]/timeline/timeline-event[position()=" + (j + 1) + "]/@relTime");
                                TimelineEvent tle = t.timeline.addEvent(time);
                                if (time == 0) {
                                    tle.label = "begin";
                                } else if (time == 1) {
                                    tle.label = "end";
                                }

                                int macroLines = xp.getInteger("count(/timers/timer[position()=" + (i + 1) + "]/timeline/timeline-event[position()=" + (j + 1) + "]/tle-macro/action)");
                                for (int k = 0; k < macroLines; k++) {
                                    String type = xp.getString("/timers/timer[position()=" + (i + 1) + "]/timeline/timeline-event[position()=" + (j + 1) + "]/tle-macro/action[position()=" + (k + 1) + "]/type");
                                    String param1 = xp.getString("/timers/timer[position()=" + (i + 1) + "]/timeline/timeline-event[position()=" + (j + 1) + "]/tle-macro/action[position()=" + (k + 1) + "]/param1");
                                    String param2 = xp.getString("/timers/timer[position()=" + (i + 1) + "]/timeline/timeline-event[position()=" + (j + 1) + "]/tle-macro/action[position()=" + (k + 1) + "]/param2");
                                    tle.macro.actions[k][0] = type;
                                    tle.macro.actions[k][1] = param1;
                                    tle.macro.actions[k][2] = param2;
                                }
                            }
                        }

                        timers.add(t);
                    }
                }
            } else {
                // this.addNewTimer();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getNewName() {
        int i = timers.size() + 1;
        while (true) {
            String name = "Timer " + i++;
            boolean nameExists = false;
            for (Timer t : this.timers) {
                if (t.name.equals(name)) {
                    nameExists = true;
                }

            }

            if (!nameExists) {
                return name;
            }

        }
    }

    public Timer getTimer(
            String strTimer) {
        for (Timer t : timers) {
            if (t.name.equalsIgnoreCase(strTimer)) {
                return t;
            }

        }

        return null;
    }

    public Timer addNewTimer() {
        Timer t = new Timer();
        t.name = this.getNewName();
        timers.add(t);

        save();

        return t;
    }

    public void replaceCurveReferences(String oldName, String newName) {
        boolean bRefresh = false;
        for (Timer t : timers) {
            if (t.defaultCurve.equalsIgnoreCase(oldName)) {
                t.defaultCurve = "" + newName;
                bRefresh =
                        true;
            }

            for (int i = 0; i
                    < t.variables.length; i++) {
                if (((String) t.variables[i][4]).equalsIgnoreCase(oldName)) {
                    t.variables[i][4] = "" + newName;
                    bRefresh =
                            true;
                }

            }
        }

        int selTab = SketchletEditor.editorPanel.extraEditorPanel.timersExtraPanel.tabs.getSelectedIndex();
        SketchletEditor.editorPanel.extraEditorPanel.timersExtraPanel.showTimers(selTab);
    }

    public void save() {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "timers.xml"));
            out.println("<timers>");
            for (Timer t : this.timers) {
                t.save(out);
            }

            out.println("</timers>");

            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public TimerThread startTimerThread(String strTimerName, String strParams, Vector<TimerThread> activeTimers) {
        TimerThread tt = null;

        for (Timer t : timers) {
            if (t.name.trim().equals(strTimerName.trim())) {
                return new TimerThread(t, strParams, activeTimers, true);
            }

        }

        return tt;
    }
}
