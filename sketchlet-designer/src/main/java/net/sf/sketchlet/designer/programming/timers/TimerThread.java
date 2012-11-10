/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.programming.timers;

import net.sf.sketchlet.common.QuotedStringTokenizer;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.designer.data.Evaluator;
import net.sf.sketchlet.designer.programming.macros.Commands;
import net.sf.sketchlet.designer.programming.macros.MacroThread;
import net.sf.sketchlet.designer.programming.timers.curves.Curve;
import net.sf.sketchlet.designer.programming.timers.curves.Curves;
import net.sf.sketchlet.designer.ui.ProgressMonitor;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class TimerThread implements Runnable {

    public Timer timer;
    Thread t;
    boolean stopped = false;
    boolean paused = false;
    long pausedTime = 0;
    double[][] currentValues;
    boolean shouldProcess[];
    // boolean isIntegerValue[];
    Vector<TimerThread> activeTimers;
    ProgressMonitor progressMonitor = null;
    Hashtable<String, String> lastUpdateVariable = new Hashtable<String, String>();
    public double progress = 0.0;

    public TimerThread(Timer timer, String strParams, Vector<TimerThread> activeTimers, boolean bStart) {
        this(timer, strParams, activeTimers, null, bStart);
    }

    public String strParams = "";
    String args[] = null;

    public TimerThread(Timer timer, String strParams, Vector<TimerThread> activeTimers, ProgressMonitor progressMonitor, boolean bStart) {
        this.timer = timer;
        this.strParams = strParams;
        this.activeTimers = activeTimers;
        this.progressMonitor = progressMonitor;
        this.activeTimers.add(this);

        if (progressMonitor != null) {
            progressMonitor.setMaximum(0);
            progressMonitor.setMaximum(1000);
            progressMonitor.setValue(0);

            progress = 0.0;
        }
        if (bStart) {
            // start();
        }

        timer.timeline.strParams = strParams;
        args = QuotedStringTokenizer.parseArgs(strParams.trim());
    }

    public void start() {
        t = new Thread(this);
        stopped = false;
        t.start();
    }

    public void stop() {
        stopped = true;
    }

    long startTime = 0;
    long pausedStartTime = 0;

    public void pause() {
        if (!isPaused()) {
            paused = true;
            this.pausedTime = System.currentTimeMillis();
            pausedStartTime = startTime;
            startTime = 0;
        }
    }

    public void continueExecution() {
        paused = false;
    }

    public boolean isPaused() {
        return this.paused;
    }

    public double tick(long time) {
        if (stopped || paused) {
            return 0.0;
        }
        double rel = 0.0;
        if (startTime == 0) {
            if (pausedStartTime == 0) {
                startTime = time;
                timer.timeline.onStart();
            } else {
                startTime = pausedStartTime + (System.currentTimeMillis() - this.pausedTime);
                paused = false;
                pausedStartTime = 0;
            }
        } else {
            double pauseBefore = 0.0;
            try {
                if (!timer.strPauseBefore.isEmpty()) {
                    pauseBefore = Double.parseDouble(Evaluator.processText(MacroThread.processForArgs(args, timer.strPauseBefore), "", ""));
                }
            } catch (Exception e) {
            }
            double pauseAfter = 0.0;
            try {
                if (!timer.strPauseAfter.isEmpty()) {
                    pauseAfter = Double.parseDouble(Evaluator.processText(MacroThread.processForArgs(args, timer.strPauseAfter), "", ""));
                }
            } catch (Exception e) {
            }
            double durationInSec = 2.0;
            try {
                if (!timer.strDurationInSec.isEmpty()) {
                    durationInSec = Double.parseDouble(Evaluator.processText(MacroThread.processForArgs(args, timer.strDurationInSec), "", ""));
                }
            } catch (Exception e) {
            }
            long t = (long) (time - startTime - pauseBefore * 1000);
            if (t < 0) {
                return 0.0;
            }
            long d = (long) (durationInSec * 1000);
            if (t < d) {
                rel = (double) t / d;
                updateVariables(rel);
                timer.timeline.onTimer(rel);
            } else if (timer.loop) {
                updateVariablesAtEnd();
                rel = 1.0;
                timer.timeline.onEnd();
                if (t > d + pauseAfter * 1000) {
                    startTime = time;
                    timer.timeline.onStart();
                }
            } else {
                updateVariablesAtEnd();
                rel = 1.0;
                timer.timeline.onEnd();
                if (t > d + pauseAfter * 1000) {
                    stop();
                    resetVariablesAtEnd();
                    finalizeTimer();
                }
            }
        }
        if (progressMonitor != null) {
            progressMonitor.setValue((int) (rel * 1000));
        }

        progress = rel;

        return rel;
    }

    private void updateVariablesAtEnd() {
        for (int j = 0; j < timer.variables.length; j++) {
            String variable = Evaluator.processText(MacroThread.processForArgs(args, (String) timer.variables[j][0]), "", "");
            String value = timer.pulsar ? MacroThread.processForArgs(args, timer.variables[j][1].toString()) : MacroThread.processForArgs(args, timer.variables[j][2].toString());
            String strVar = value;
            try {
                String strCurve = Evaluator.processText(MacroThread.processForArgs(args, timer.variables[j][4].toString()), "", "");
                Curve curve = Curves.globalCurves.getCurve(strCurve);
                if (curve == null) {
                    curve = Curves.globalCurves.getCurve(timer.defaultCurve);
                }
                if (curve != null) {
                    double start = Double.parseDouble(MacroThread.processForArgs(args, timer.variables[j][1].toString()));
                    double end = Double.parseDouble(MacroThread.processForArgs(args, timer.variables[j][2].toString()));
                    double range = end - start;
                    double rel;
                    if (timer.pulsar) {
                        rel = curve.getRelativeValue(0.0);
                    } else {
                        rel = curve.getRelativeValue(1.0);
                    }
                    double v = start + range * rel;
                    String strFormat = Evaluator.processText(MacroThread.processForArgs(args, timer.variables[j][3].toString()), "", "");
                    if (strFormat.isEmpty()) {
                        strVar = "" + v;
                    } else {
                        DecimalFormat df = new DecimalFormat(strFormat, new DecimalFormatSymbols(Locale.US));
                        strVar = df.format(v);
                    }

                }
            } catch (Exception e) {
            }
            updateVariable(variable, strVar);
            // timer.variables[j][5] = strVar;
            if (progressMonitor != null) {
                progressMonitor.variableUpdated(variable, strVar);
            }
        }
    }

    private void resetVariablesAtEnd() {
        if (timer.bResetAtEnd) {
            try {
                Thread.sleep(50);
            } catch (Exception e) {
            }
            for (int j = 0; j < timer.variables.length; j++) {
                String variable = Evaluator.processText(MacroThread.processForArgs(args, (String) timer.variables[j][0]), "", "");
                String value = MacroThread.processForArgs(args, timer.variables[j][1].toString());
                String strVar = value;
                String strCurve = Evaluator.processText(MacroThread.processForArgs(args, timer.variables[j][4].toString()), "", "");
                Curve curve = Curves.globalCurves.getCurve(strCurve);
                if (curve == null) {
                    curve = Curves.globalCurves.getCurve(timer.defaultCurve);
                }
                if (curve != null) {
                    try {
                        double start = Double.parseDouble(MacroThread.processForArgs(args, timer.variables[j][1].toString()));
                        double end = Double.parseDouble(MacroThread.processForArgs(args, timer.variables[j][2].toString()));
                        double range = end - start;
                        double rel;
                        rel = curve.getRelativeValue(0.0);
                        double v = start + range * rel;
                        String strFormat = Evaluator.processText(MacroThread.processForArgs(args, timer.variables[j][3].toString()), "", "");
                        if (strFormat.isEmpty()) {
                            strVar = "" + v;
                        } else {
                            DecimalFormat df = new DecimalFormat(strFormat, new DecimalFormatSymbols(Locale.US));
                            strVar = df.format(v);
                        }
                    } catch (Exception e) {
                    }
                }
                updateVariable(variable, strVar);
                if (progressMonitor != null) {
                    progressMonitor.variableUpdated(variable, strVar);
                }
            }
        }
    }

    private void updateVariables(double rel) {
        if (timer.pulsar) {
            if (rel <= 0.5) {
                rel *= 2;
            } else {
                rel = (1 - rel) * 2;
            }
        }
        for (int i = 0; i < timer.variables.length; i++) {
            String strVariable = Evaluator.processText(MacroThread.processForArgs(args, timer.variables[i][0].toString()), "", "");
            String strStart = Evaluator.processText(MacroThread.processForArgs(args, timer.variables[i][1].toString()), "", "");
            String strEnd = Evaluator.processText(MacroThread.processForArgs(args, timer.variables[i][2].toString()), "", "");

            if (strVariable.isEmpty() || strStart.isEmpty() || strEnd.isEmpty()) {
                continue;
            }

            String strFormat = Evaluator.processText(MacroThread.processForArgs(args, timer.variables[i][3].toString()), "", "");
            String strCurve = Evaluator.processText(MacroThread.processForArgs(args, timer.variables[i][4].toString()), "", "");
            Curve curve = Curves.globalCurves.getCurve(strCurve);
            if (curve == null) {
                curve = Curves.globalCurves.getCurve(timer.defaultCurve);
            }
            double r;
            double durationInSec = 2.0;
            try {
                if (!timer.strDurationInSec.isEmpty()) {
                    durationInSec = Double.parseDouble(Evaluator.processText(MacroThread.processForArgs(args, timer.strDurationInSec), "", ""));
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
                            if (progressMonitor != null) {
                                progressMonitor.variableUpdated(strVariable, strValue);
                            }
                        }
                    }

                } catch (Exception e) {
                }
            }
        }
    }

    public void run() {
        while (!stopped) {
            try {
                double r = this.tick(System.currentTimeMillis());
                Thread.sleep(50);
            } catch (Exception e) {
            }
        }

        finalizeTimer();
    }

    public void finalizeTimer() {
        if (activeTimers != null) {
            activeTimers.remove(this);
        }
        if (progressMonitor != null) {
            progressMonitor.onStop();
        }
        try {
            Thread.sleep(50);
        } catch (Exception e) {
        }
    }

    public void updateVariable(String variable, String value) {
        if (variable.isEmpty()) {
            return;
        }
        String oldValue = this.lastUpdateVariable.get(variable);
        if (oldValue == null || !oldValue.equals(value)) {
            if (DataServer.variablesServer != null) {
                Commands.updateVariableOrProperty(this, variable, value, Commands.ACTION_VARIABLE_UPDATE);
            }
        }
        this.lastUpdateVariable.put(variable, value);
    }
}
