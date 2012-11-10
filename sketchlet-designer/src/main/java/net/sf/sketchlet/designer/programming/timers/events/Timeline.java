/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.programming.timers.events;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.designer.data.Evaluator;
import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.programming.macros.Commands;
import net.sf.sketchlet.designer.programming.macros.MacroThread;
import net.sf.sketchlet.designer.programming.timers.Timer;
import net.sf.sketchlet.designer.programming.timers.curves.Curve;
import net.sf.sketchlet.designer.programming.timers.curves.Curves;
import net.sf.sketchlet.designer.ui.macros.MacroPanel;
import net.sf.sketchlet.designer.ui.playback.PlaybackFrame;
import net.sf.sketchlet.designer.ui.playback.PlaybackPanel;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class Timeline {

    public Vector<TimelineEvent> events = new Vector<TimelineEvent>();
    int pointWidth = 10;
    Timer timer;
    public double displayPosition = 0.0;
    TimelinePanel panel;
    public String strParams = "";

    public Timeline(Timer timer, TimelinePanel panel) {
        this.timer = timer;
        this.panel = panel;
        events.add(new TimelineEvent(0.0, "begin"));
        events.add(new TimelineEvent(1.0, "end"));
    }

    public Timeline getUndoCopy() {
        Timeline tl = new Timeline(this.timer, this.panel);
        tl.events.removeAllElements();
        for (TimelineEvent e : this.events) {
            TimelineEvent tle = tl.addEvent(e.relTime);
            tle.bExecuted = e.bExecuted;
            tle.label = e.label;
            tle.macro = e.macro;
            tle.relTime = e.relTime;
        }
        return tl;
    }

    public void restore(Timeline tl) {
        this.events.removeAllElements();
        for (TimelineEvent e : tl.events) {
            TimelineEvent tle = this.addEvent(e.relTime);
            tle.bExecuted = e.bExecuted;
            tle.label = e.label;
            tle.macro = e.macro;
            tle.relTime = e.relTime;
        }
    }

    double prevRelTime = 0.0;

    public void onTimer(double relTime) {
        double _relTime = relTime;
        boolean bUp = true;
        if (this.timer.pulsar) {
            if (relTime <= 0.5) {
                relTime *= 2;
            } else {
                relTime = (1 - relTime) * 2;
                if (prevRelTime <= 0.5) {
                    for (TimelineEvent tle : events) {
                        tle.bExecuted = false;
                    }
                }
                bUp = false;
            }
        }
        prevRelTime = _relTime;

        for (TimelineEvent tle : events) {
            if (!tle.bExecuted && ((bUp && relTime >= tle.relTime) || (!bUp && relTime <= tle.relTime))) {
                tle.bExecuted = true;
                Page page = null;
                if (PlaybackFrame.playbackFrame != null || (SketchletEditor.editorPanel != null && SketchletEditor.editorPanel.internalPlaybackPanel != null)) {
                    page = PlaybackPanel.currentPage;
                } else {
                    page = SketchletEditor.editorPanel.currentPage;
                }

                try {
                    Thread.sleep(1);
                    MacroThread mt = new MacroThread(tle.macro, "", "", "");
                    page.activeMacros.add(mt);
                } catch (Throwable e) {
                }

                break;
            }
        }
    }

    public void onStart() {
        for (TimelineEvent tle : events) {
            tle.bExecuted = false;
        }
    }

    public void onEnd() {
        this.onTimer(1.0);
        for (TimelineEvent tle : events) {
            tle.bExecuted = true;
        }
    }

    public TimelineEvent addEvent(double time) {
        TimelineEvent tle = new TimelineEvent(time);
        events.add(tle);
        return tle;
    }

    public TimelineEvent selectEvent(int x, int y, TimelinePanel panel) {
        int w = panel.getWidth() - panel.marginH * 2;
        int h = panel.getHeight() - panel.marginV * 2;
        int mw = panel.marginH;
        int mh = panel.marginV;
        for (TimelineEvent tle : events) {
            int _x = (int) (mw + tle.getTime() * w - pointWidth / 2);
            int _y = (int) (mh + h / 2 - pointWidth / 2);

            if (x >= _x && x <= _x + pointWidth) {
                return tle;
            }
        }
        return null;
    }

    public void draw(Graphics2D g2d, TimelinePanel panel) {
        int w = panel.getWidth() - panel.marginH * 2;
        int h = panel.getHeight() - panel.marginV * 2;
        int mw = panel.marginH;
        int mh = panel.marginV;

        g2d.setFont(g2d.getFont().deriveFont(9.0f));

        for (TimelineEvent tle : events) {
            int x = (int) (mw + tle.getTime() * w - pointWidth / 2);
            int y = (int) (mh + h / 2 - pointWidth / 2);
            if (panel.selectedEvent == tle) {
                g2d.setColor(new Color(200, 0, 0, 100));
            } else {
                g2d.setColor(Color.WHITE);
            }
            g2d.fillOval(x, y, pointWidth, pointWidth);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(x, y, pointWidth, pointWidth);

            g2d.setColor(Color.GRAY);
            if (tle.label.isEmpty() && timer != null) {
                double duration = 1.0;

                try {
                    String strDur = Evaluator.processText(timer.strDurationInSec, "", "");
                    duration = Double.parseDouble(strDur);
                    g2d.drawString(Language.translate(tle.getTimeString(duration)), x + pointWidth / 2, y + 21);
                } catch (Exception e) {
                }
            } else {
                g2d.drawString(tle.label, x + pointWidth / 2, y + 21);
            }
            g2d.drawString(tle.getTimeString(), x + pointWidth / 2, y - 5);
        }

        if (displayPosition > 0) {
            g2d.setStroke(new BasicStroke(2.5f));
            g2d.setColor(Color.GREEN.darker());
            g2d.drawRect((int) (mw + w * displayPosition), mh + h / 2 - 5, 2, 10);
        }
    }

    public void sort() {
        Collections.sort(events, new Comparator() {

            public int compare(Object o1, Object o2) {
                if (o1 instanceof TimelineEvent && o2 instanceof TimelineEvent) {
                    TimelineEvent seg1 = (TimelineEvent) o1;
                    TimelineEvent seg2 = (TimelineEvent) o2;

                    if (seg1.relTime < seg2.relTime) {
                        return -1;
                    } else if (seg1.relTime > seg2.relTime) {
                        return 1;
                    } else {
                        return 0;
                    }

                }
                return 0;
            }

            public boolean equals(Object obj) {
                return false;
            }
        });
    }

    boolean bLoading = false;

    public boolean isLoading() {
        return this.bLoading;
    }

    public void loadTabs(JTabbedPane tabs, TimelinePanel panel) {
        this.bLoading = true;
        tabs.removeAll();
        sort();

        for (final TimelineEvent tle : events) {
            MacroPanel mp = new MacroPanel(tle.macro, false, true, 0, true);
            mp.setSaveUndoAction(new Runnable() {

                public void run() {
                    SketchletEditor.editorPanel.saveMacroUndo(tle.macro);
                }
            });
            tabs.add(Language.translate("At " + tle.getTitle()), mp);
        }
        this.selectTab(tabs, panel);
        this.bLoading = false;
    }

    public void deleteEvent(TimelineEvent tle) {
        events.remove(tle);
    }

    public void selectTab(JTabbedPane tabs, TimelinePanel panel) {
        if (panel.selectedEvent != null) {
            int index = events.indexOf(panel.selectedEvent);
            if (index >= 0) {
                tabs.setSelectedIndex(index);
            }
        }
    }

    public void save(PrintWriter out) {
        out.println("<timeline>");

        for (TimelineEvent tle : events) {
            out.println("<timeline-event relTime='" + tle.relTime + "'>");
            tle.macro.save(out, "tle-macro");
            out.println("</timeline-event>");
        }

        out.println("</timeline>");
    }

    public void updateVariables(double rel) {
        if (!this.panel.eventsPanel.updateVariables.isSelected()) {
            return;
        }
        for (int i = 0; i < timer.variables.length; i++) {
            String strVariable = Evaluator.processText(timer.variables[i][0].toString(), "", "");
            String strStart = Evaluator.processText(timer.variables[i][1].toString(), "", "");
            String strEnd = Evaluator.processText(timer.variables[i][2].toString(), "", "");
            String strFormat = Evaluator.processText(timer.variables[i][3].toString(), "", "");
            String strCurve = Evaluator.processText(timer.variables[i][4].toString(), "", "");
            Curve curve = Curves.globalCurves.getCurve(strCurve);
            if (curve == null) {
                curve = Curves.globalCurves.getCurve(timer.defaultCurve);
            }
            double r;
            double durationInSec = 2.0;
            try {
                if (!timer.strDurationInSec.isEmpty()) {
                    durationInSec = Double.parseDouble(Evaluator.processText(timer.strDurationInSec, "", ""));
                }
            } catch (Exception e) {
            }
            if (curve != null) {
                r = curve.getRelativeValue(durationInSec, rel);
            } else {
                r = rel;
            }
            if (!Double.isNaN(r)) {
                try {
                    if (!strStart.isEmpty() && !strEnd.isEmpty()) {
                        double start = Double.parseDouble(strStart);
                        double end = Double.parseDouble(strEnd);
                        double value = start + (end - start) * r;

                        if (!Double.isNaN(value)) {
                            String strValue = "";
                            if (strFormat.isEmpty()) {
                                strValue += value;
                            } else {
                                DecimalFormat df = new DecimalFormat(strFormat, new DecimalFormatSymbols(Locale.US));
                                strValue += df.format(value);
                            }

                            this.updateVariable(strVariable, strValue);
                        }
                    }

                } catch (Exception e) {
                }
            }
        }
    }

    public void updateVariable(String variable, String value) {
        if (variable.isEmpty()) {
            return;
        }

        if (DataServer.variablesServer != null) {
            Commands.updateVariableOrProperty(this, variable, value, Commands.ACTION_VARIABLE_UPDATE);
        }
    }
}
