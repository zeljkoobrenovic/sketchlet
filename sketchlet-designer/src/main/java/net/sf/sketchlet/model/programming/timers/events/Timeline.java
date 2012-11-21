/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.model.programming.timers.events;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.macros.MacroPanel;
import net.sf.sketchlet.designer.playback.ui.PlaybackFrame;
import net.sf.sketchlet.designer.playback.ui.PlaybackPanel;
import net.sf.sketchlet.model.evaluator.Evaluator;
import net.sf.sketchlet.model.Page;
import net.sf.sketchlet.model.programming.macros.Commands;
import net.sf.sketchlet.model.programming.macros.MacroThread;
import net.sf.sketchlet.model.programming.timers.Timer;
import net.sf.sketchlet.model.programming.timers.curves.Curve;
import net.sf.sketchlet.model.programming.timers.curves.Curves;

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

    private Vector<TimelineEvent> events = new Vector<TimelineEvent>();
    private int pointWidth = 10;
    private Timer timer;
    private double displayPosition = 0.0;
    private TimelinePanel panel;
    private String parameters = "";
    private double prevRelTime = 0.0;

    public Timeline(Timer timer, TimelinePanel panel) {
        this.setTimer(timer);
        this.setPanel(panel);
        getEvents().add(new TimelineEvent(0.0, "begin"));
        getEvents().add(new TimelineEvent(1.0, "end"));
    }

    public Timeline getUndoCopy() {
        Timeline tl = new Timeline(this.getTimer(), this.getPanel());
        tl.getEvents().removeAllElements();
        for (TimelineEvent e : this.getEvents()) {
            TimelineEvent tle = tl.addEvent(e.relativeTime);
            tle.setExecuted(e.isExecuted());
            tle.setLabel(e.getLabel());
            tle.setMacro(e.getMacro());
            tle.relativeTime = e.relativeTime;
        }
        return tl;
    }

    public void restore(Timeline tl) {
        this.getEvents().removeAllElements();
        for (TimelineEvent e : tl.getEvents()) {
            TimelineEvent tle = this.addEvent(e.relativeTime);
            tle.setExecuted(e.isExecuted());
            tle.setLabel(e.getLabel());
            tle.setMacro(e.getMacro());
            tle.relativeTime = e.relativeTime;
        }
    }

    public void onTimer(double relTime) {
        double _relTime = relTime;
        boolean bUp = true;
        if (this.getTimer().isPulsar()) {
            if (relTime <= 0.5) {
                relTime *= 2;
            } else {
                relTime = (1 - relTime) * 2;
                if (prevRelTime <= 0.5) {
                    for (TimelineEvent tle : getEvents()) {
                        tle.setExecuted(false);
                    }
                }
                bUp = false;
            }
        }
        prevRelTime = _relTime;

        for (TimelineEvent tle : getEvents()) {
            if (!tle.isExecuted() && ((bUp && relTime >= tle.relativeTime) || (!bUp && relTime <= tle.relativeTime))) {
                tle.setExecuted(true);
                Page page = null;
                if (PlaybackFrame.playbackFrame != null || (SketchletEditor.getInstance() != null && SketchletEditor.getInstance().getInternalPlaybackPanel() != null)) {
                    page = PlaybackPanel.currentPage;
                } else {
                    page = SketchletEditor.getInstance().getCurrentPage();
                }

                try {
                    Thread.sleep(1);
                    MacroThread mt = new MacroThread(tle.getMacro(), "", "", "");
                    page.getActiveMacros().add(mt);
                } catch (Throwable e) {
                }

                break;
            }
        }
    }

    public void onStart() {
        for (TimelineEvent tle : getEvents()) {
            tle.setExecuted(false);
        }
    }

    public void onEnd() {
        this.onTimer(1.0);
        for (TimelineEvent tle : getEvents()) {
            tle.setExecuted(true);
        }
    }

    public TimelineEvent addEvent(double time) {
        TimelineEvent tle = new TimelineEvent(time);
        getEvents().add(tle);
        return tle;
    }

    public TimelineEvent selectEvent(int x, int y, TimelinePanel panel) {
        int w = panel.getWidth() - panel.getHorizontalMargin() * 2;
        int h = panel.getHeight() - panel.getVerticalMargin() * 2;
        int mw = panel.getHorizontalMargin();
        int mh = panel.getVerticalMargin();
        for (TimelineEvent tle : getEvents()) {
            int _x = (int) (mw + tle.getTime() * w - pointWidth / 2);
            int _y = (int) (mh + h / 2 - pointWidth / 2);

            if (x >= _x && x <= _x + pointWidth) {
                return tle;
            }
        }
        return null;
    }

    public void draw(Graphics2D g2d, TimelinePanel panel) {
        int w = panel.getWidth() - panel.getHorizontalMargin() * 2;
        int h = panel.getHeight() - panel.getVerticalMargin() * 2;
        int mw = panel.getHorizontalMargin();
        int mh = panel.getVerticalMargin();

        g2d.setFont(g2d.getFont().deriveFont(9.0f));

        for (TimelineEvent tle : getEvents()) {
            int x = (int) (mw + tle.getTime() * w - pointWidth / 2);
            int y = (int) (mh + h / 2 - pointWidth / 2);
            if (panel.getSelectedEvent() == tle) {
                g2d.setColor(new Color(200, 0, 0, 100));
            } else {
                g2d.setColor(Color.WHITE);
            }
            g2d.fillOval(x, y, pointWidth, pointWidth);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(x, y, pointWidth, pointWidth);

            g2d.setColor(Color.GRAY);
            if (tle.getLabel().isEmpty() && getTimer() != null) {
                double duration = 1.0;

                try {
                    String strDur = Evaluator.processText(getTimer().getStrDurationInSec(), "", "");
                    duration = Double.parseDouble(strDur);
                    g2d.drawString(Language.translate(tle.getTimeString(duration)), x + pointWidth / 2, y + 21);
                } catch (Exception e) {
                }
            } else {
                g2d.drawString(tle.getLabel(), x + pointWidth / 2, y + 21);
            }
            g2d.drawString(tle.getTimeString(), x + pointWidth / 2, y - 5);
        }

        if (getDisplayPosition() > 0) {
            g2d.setStroke(new BasicStroke(2.5f));
            g2d.setColor(Color.GREEN.darker());
            g2d.drawRect((int) (mw + w * getDisplayPosition()), mh + h / 2 - 5, 2, 10);
        }
    }

    public void sort() {
        Collections.sort(getEvents(), new Comparator() {

            public int compare(Object o1, Object o2) {
                if (o1 instanceof TimelineEvent && o2 instanceof TimelineEvent) {
                    TimelineEvent seg1 = (TimelineEvent) o1;
                    TimelineEvent seg2 = (TimelineEvent) o2;

                    if (seg1.relativeTime < seg2.relativeTime) {
                        return -1;
                    } else if (seg1.relativeTime > seg2.relativeTime) {
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

        for (final TimelineEvent tle : getEvents()) {
            MacroPanel mp = new MacroPanel(tle.getMacro(), false, true, 0, true);
            mp.setSaveUndoAction(new Runnable() {

                public void run() {
                    SketchletEditor.getInstance().saveMacroUndo(tle.getMacro());
                }
            });
            tabs.add(Language.translate("At " + tle.getTitle()), mp);
        }
        this.selectTab(tabs, panel);
        this.bLoading = false;
    }

    public void deleteEvent(TimelineEvent tle) {
        getEvents().remove(tle);
    }

    public void selectTab(JTabbedPane tabs, TimelinePanel panel) {
        if (panel.getSelectedEvent() != null) {
            int index = getEvents().indexOf(panel.getSelectedEvent());
            if (index >= 0) {
                tabs.setSelectedIndex(index);
            }
        }
    }

    public void save(PrintWriter out) {
        out.println("<timeline>");

        for (TimelineEvent tle : getEvents()) {
            out.println("<timeline-event relTime='" + tle.relativeTime + "'>");
            tle.getMacro().save(out, "tle-macro");
            out.println("</timeline-event>");
        }

        out.println("</timeline>");
    }

    public void updateVariables(double rel) {
        if (!this.getPanel().getEventsPanel().getUpdateVariablesCheckbox().isSelected()) {
            return;
        }
        for (int i = 0; i < getTimer().getVariables().length; i++) {
            String strVariable = Evaluator.processText(getTimer().getVariables()[i][0].toString(), "", "");
            String strStart = Evaluator.processText(getTimer().getVariables()[i][1].toString(), "", "");
            String strEnd = Evaluator.processText(getTimer().getVariables()[i][2].toString(), "", "");
            String strFormat = Evaluator.processText(getTimer().getVariables()[i][3].toString(), "", "");
            String strCurve = Evaluator.processText(getTimer().getVariables()[i][4].toString(), "", "");
            Curve curve = Curves.getGlobalCurves().getCurve(strCurve);
            if (curve == null) {
                curve = Curves.getGlobalCurves().getCurve(getTimer().getDefaultCurve());
            }
            double r;
            double durationInSec = 2.0;
            try {
                if (!getTimer().getStrDurationInSec().isEmpty()) {
                    durationInSec = Double.parseDouble(Evaluator.processText(getTimer().getStrDurationInSec(), "", ""));
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

        if (DataServer.getInstance() != null) {
            Commands.updateVariableOrProperty(this, variable, value, Commands.ACTION_VARIABLE_UPDATE);
        }
    }

    public Vector<TimelineEvent> getEvents() {
        return events;
    }

    public void setEvents(Vector<TimelineEvent> events) {
        this.events = events;
    }

    public double getDisplayPosition() {
        return displayPosition;
    }

    public void setDisplayPosition(double displayPosition) {
        this.displayPosition = displayPosition;
    }

    public TimelinePanel getPanel() {
        return panel;
    }

    public void setPanel(TimelinePanel panel) {
        this.panel = panel;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }
}
