package net.sf.sketchlet.framework.model.programming.timers;

import net.sf.sketchlet.common.XPathEvaluator;
import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.framework.model.programming.timers.events.TimelineEvent;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class Timers {

    private Vector<Timer> timers = new Vector<Timer>();
    private static Timers globalTimers = new Timers();

    public Timers() {
        load();
    }

    public static Timers getGlobalTimers() {
        return globalTimers;
    }

    public static void setGlobalTimers(Timers globalTimers) {
        Timers.globalTimers = globalTimers;
    }

    public void load() {
        try {
            getTimers().removeAllElements();

            File file = new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "timers.xml");

            if (file.exists()) {
                XPathEvaluator xp = new XPathEvaluator();
                xp.createDocumentFromFile(file);

                NodeList sketcheNodes = xp.getNodes("/timers/timer");

                if (sketcheNodes != null) {
                    for (int i = 0; i < sketcheNodes.getLength(); i++) {
                        Timer t = new Timer();

                        t.setName(xp.getString("/timers/timer[position()=" + (i + 1) + "]/name"));

                        t.setStrDurationInSec(xp.getString("/timers/timer[position()=" + (i + 1) + "]/duration"));
                        t.setStrPauseBefore(xp.getString("/timers/timer[position()=" + (i + 1) + "]/pause-before"));
                        t.setStrPauseAfter(xp.getString("/timers/timer[position()=" + (i + 1) + "]/pause-after"));

                        t.setLoop(xp.getString("/timers/timer[position()=" + (i + 1) + "]/loop").equals("true"));
                        t.setPulsar(xp.getString("/timers/timer[position()=" + (i + 1) + "]/pulsar").equals("true"));
                        t.setDefaultCurve(xp.getString("/timers/timer[position()=" + (i + 1) + "]/default-curve"));
                        t.setbResetAtEnd(xp.getString("/timers/timer[position()=" + (i + 1) + "]/reset-at-end").equals("true"));
                        t.setTabIndex(xp.getInteger("/timers/timer[position()=" + (i + 1) + "]/tab-index"));

                        int timerCount = xp.getInteger("count(/timers/timer[position()=" + (i + 1) + "]/variable)");
                        for (int j = 0; j < t.getVariables().length && j < timerCount; j++) {
                            t.getVariables()[j][0] = xp.getString("/timers/timer[position()=" + (i + 1) + "]/variable[position()=" + (j + 1) + "]/name");
                            t.getVariables()[j][1] = xp.getString("/timers/timer[position()=" + (i + 1) + "]/variable[position()=" + (j + 1) + "]/start");
                            t.getVariables()[j][2] = xp.getString("/timers/timer[position()=" + (i + 1) + "]/variable[position()=" + (j + 1) + "]/end");
                            t.getVariables()[j][3] = xp.getString("/timers/timer[position()=" + (i + 1) + "]/variable[position()=" + (j + 1) + "]/format");
                            t.getVariables()[j][4] = xp.getString("/timers/timer[position()=" + (i + 1) + "]/variable[position()=" + (j + 1) + "]/curve");
                        }

                        int timelineEventsCounter = xp.getInteger("count(/timers/timer[position()=" + (i + 1) + "]/timeline/timeline-event)");
                        if (timelineEventsCounter >= 2) {
                            t.getTimeline().getEvents().removeAllElements();
                            for (int j = 0; j < timelineEventsCounter; j++) {
                                double time = xp.getDouble("/timers/timer[position()=" + (i + 1) + "]/timeline/timeline-event[position()=" + (j + 1) + "]/@relTime");
                                TimelineEvent tle = t.getTimeline().addEvent(time);
                                if (time == 0) {
                                    tle.setLabel("begin");
                                } else if (time == 1) {
                                    tle.setLabel("end");
                                }

                                int macroLines = xp.getInteger("count(/timers/timer[position()=" + (i + 1) + "]/timeline/timeline-event[position()=" + (j + 1) + "]/tle-macro/action)");
                                for (int k = 0; k < macroLines; k++) {
                                    String type = xp.getString("/timers/timer[position()=" + (i + 1) + "]/timeline/timeline-event[position()=" + (j + 1) + "]/tle-macro/action[position()=" + (k + 1) + "]/type");
                                    String param1 = xp.getString("/timers/timer[position()=" + (i + 1) + "]/timeline/timeline-event[position()=" + (j + 1) + "]/tle-macro/action[position()=" + (k + 1) + "]/param1");
                                    String param2 = xp.getString("/timers/timer[position()=" + (i + 1) + "]/timeline/timeline-event[position()=" + (j + 1) + "]/tle-macro/action[position()=" + (k + 1) + "]/param2");
                                    tle.getMacro().getActions()[k][0] = type;
                                    tle.getMacro().getActions()[k][1] = param1;
                                    tle.getMacro().getActions()[k][2] = param2;
                                }
                            }
                        }

                        getTimers().add(t);
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
        int i = getTimers().size() + 1;
        while (true) {
            String name = "Timer " + i++;
            boolean nameExists = false;
            for (Timer t : this.getTimers()) {
                if (t.getName().equals(name)) {
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
        for (Timer t : getTimers()) {
            if (t.getName().equalsIgnoreCase(strTimer)) {
                return t;
            }

        }

        return null;
    }

    public Timer addNewTimer() {
        Timer t = new Timer();
        t.setName(this.getNewName());
        getTimers().add(t);

        save();

        return t;
    }

    public void replaceCurveReferences(String oldName, String newName) {
        boolean bRefresh = false;
        for (Timer t : getTimers()) {
            if (t.getDefaultCurve().equalsIgnoreCase(oldName)) {
                t.setDefaultCurve("" + newName);
                bRefresh =
                        true;
            }

            for (int i = 0; i
                    < t.getVariables().length; i++) {
                if (((String) t.getVariables()[i][4]).equalsIgnoreCase(oldName)) {
                    t.getVariables()[i][4] = "" + newName;
                    bRefresh =
                            true;
                }

            }
        }

        int selTab = SketchletEditor.getInstance().getExtraEditorPanel().timersExtraPanel.tabs.getSelectedIndex();
        SketchletEditor.getInstance().getExtraEditorPanel().timersExtraPanel.showTimers(selTab);
    }

    public void save() {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "timers.xml"));
            out.println("<timers>");
            for (Timer t : this.getTimers()) {
                t.save(out);
            }

            out.println("</timers>");

            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public TimerThread startTimerThread(String strTimerName, String strParams, List<TimerThread> activeTimers) {
        TimerThread tt = null;

        for (Timer t : getTimers()) {
            if (t.getName().trim().equals(strTimerName.trim())) {
                return new TimerThread(t, strParams, activeTimers, true);
            }

        }

        return tt;
    }

    public Vector<Timer> getTimers() {
        return timers;
    }

    public void setTimers(Vector<Timer> timers) {
        this.timers = timers;
    }
}
