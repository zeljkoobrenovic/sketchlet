/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.model.programming.timers;

import net.sf.sketchlet.common.QuotedStringTokenizer;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.designer.editor.ui.ProgressMonitor;
import net.sf.sketchlet.model.evaluator.Evaluator;
import net.sf.sketchlet.model.programming.macros.Commands;
import net.sf.sketchlet.model.programming.macros.MacroThread;
import net.sf.sketchlet.model.programming.timers.curves.Curve;
import net.sf.sketchlet.model.programming.timers.curves.Curves;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

/**
 * @author zobrenovic
 */
public class TimerThread implements Runnable {

    private Timer timer;
    private Thread t;
    private boolean stopped = false;
    private boolean paused = false;
    private long pausedTime = 0;
    private double[][] currentValues;
    private boolean shouldProcess[];
    private List<TimerThread> activeTimers;
    private ProgressMonitor progressMonitor = null;
    private Hashtable<String, String> lastUpdateVariable = new Hashtable<String, String>();
    private double progress = 0.0;

    private String parameters = "";
    private String args[] = null;

    public TimerThread(Timer timer, String parameters, List<TimerThread> activeTimers, boolean bStart) {
        this(timer, parameters, activeTimers, null, bStart);
    }

    public TimerThread(Timer timer, String parameters, List<TimerThread> activeTimers, ProgressMonitor progressMonitor, boolean bStart) {
        this.setTimer(timer);
        this.setParameters(parameters);
        this.activeTimers = activeTimers;
        this.progressMonitor = progressMonitor;
        this.activeTimers.add(this);

        if (progressMonitor != null) {
            progressMonitor.setMaximum(0);
            progressMonitor.setMaximum(1000);
            progressMonitor.setValue(0);

            setProgress(0.0);
        }
        if (bStart) {
            // start();
        }

        timer.getTimeline().setParameters(parameters);
        args = QuotedStringTokenizer.parseArgs(parameters.trim());
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
                getTimer().getTimeline().onStart();
            } else {
                startTime = pausedStartTime + (System.currentTimeMillis() - this.pausedTime);
                paused = false;
                pausedStartTime = 0;
            }
        } else {
            double pauseBefore = 0.0;
            try {
                if (!getTimer().getStrPauseBefore().isEmpty()) {
                    pauseBefore = Double.parseDouble(Evaluator.processText(MacroThread.processForArgs(args, getTimer().getStrPauseBefore()), "", ""));
                }
            } catch (Exception e) {
            }
            double pauseAfter = 0.0;
            try {
                if (!getTimer().getStrPauseAfter().isEmpty()) {
                    pauseAfter = Double.parseDouble(Evaluator.processText(MacroThread.processForArgs(args, getTimer().getStrPauseAfter()), "", ""));
                }
            } catch (Exception e) {
            }
            double durationInSec = 2.0;
            try {
                if (!getTimer().getStrDurationInSec().isEmpty()) {
                    durationInSec = Double.parseDouble(Evaluator.processText(MacroThread.processForArgs(args, getTimer().getStrDurationInSec()), "", ""));
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
                getTimer().getTimeline().onTimer(rel);
            } else if (getTimer().isLoop()) {
                updateVariablesAtEnd();
                rel = 1.0;
                getTimer().getTimeline().onEnd();
                if (t > d + pauseAfter * 1000) {
                    startTime = time;
                    getTimer().getTimeline().onStart();
                }
            } else {
                updateVariablesAtEnd();
                rel = 1.0;
                getTimer().getTimeline().onEnd();
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

        setProgress(rel);

        return rel;
    }

    private void updateVariablesAtEnd() {
        for (int j = 0; j < getTimer().getVariables().length; j++) {
            String variable = Evaluator.processText(MacroThread.processForArgs(args, (String) getTimer().getVariables()[j][0]), "", "");
            String value = getTimer().isPulsar() ? MacroThread.processForArgs(args, getTimer().getVariables()[j][1].toString()) : MacroThread.processForArgs(args, getTimer().getVariables()[j][2].toString());
            String strVar = value;
            try {
                String strCurve = Evaluator.processText(MacroThread.processForArgs(args, getTimer().getVariables()[j][4].toString()), "", "");
                Curve curve = Curves.getGlobalCurves().getCurve(strCurve);
                if (curve == null) {
                    curve = Curves.getGlobalCurves().getCurve(getTimer().getDefaultCurve());
                }
                if (curve != null) {
                    double start = Double.parseDouble(MacroThread.processForArgs(args, getTimer().getVariables()[j][1].toString()));
                    double end = Double.parseDouble(MacroThread.processForArgs(args, getTimer().getVariables()[j][2].toString()));
                    double range = end - start;
                    double rel;
                    if (getTimer().isPulsar()) {
                        rel = curve.getRelativeValue(0.0);
                    } else {
                        rel = curve.getRelativeValue(1.0);
                    }
                    double v = start + range * rel;
                    String strFormat = Evaluator.processText(MacroThread.processForArgs(args, getTimer().getVariables()[j][3].toString()), "", "");
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
            // timer.getVariables()[j][5] = strVar;
            if (progressMonitor != null) {
                progressMonitor.variableUpdated(variable, strVar);
            }
        }
    }

    private void resetVariablesAtEnd() {
        if (getTimer().isbResetAtEnd()) {
            try {
                Thread.sleep(50);
            } catch (Exception e) {
            }
            for (int j = 0; j < getTimer().getVariables().length; j++) {
                String variable = Evaluator.processText(MacroThread.processForArgs(args, (String) getTimer().getVariables()[j][0]), "", "");
                String value = MacroThread.processForArgs(args, getTimer().getVariables()[j][1].toString());
                String strVar = value;
                String strCurve = Evaluator.processText(MacroThread.processForArgs(args, getTimer().getVariables()[j][4].toString()), "", "");
                Curve curve = Curves.getGlobalCurves().getCurve(strCurve);
                if (curve == null) {
                    curve = Curves.getGlobalCurves().getCurve(getTimer().getDefaultCurve());
                }
                if (curve != null) {
                    try {
                        double start = Double.parseDouble(MacroThread.processForArgs(args, getTimer().getVariables()[j][1].toString()));
                        double end = Double.parseDouble(MacroThread.processForArgs(args, getTimer().getVariables()[j][2].toString()));
                        double range = end - start;
                        double rel;
                        rel = curve.getRelativeValue(0.0);
                        double v = start + range * rel;
                        String strFormat = Evaluator.processText(MacroThread.processForArgs(args, getTimer().getVariables()[j][3].toString()), "", "");
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
        if (getTimer().isPulsar()) {
            if (rel <= 0.5) {
                rel *= 2;
            } else {
                rel = (1 - rel) * 2;
            }
        }
        for (int i = 0; i < getTimer().getVariables().length; i++) {
            String strVariable = Evaluator.processText(MacroThread.processForArgs(args, getTimer().getVariables()[i][0].toString()), "", "");
            String strStart = Evaluator.processText(MacroThread.processForArgs(args, getTimer().getVariables()[i][1].toString()), "", "");
            String strEnd = Evaluator.processText(MacroThread.processForArgs(args, getTimer().getVariables()[i][2].toString()), "", "");

            if (strVariable.isEmpty() || strStart.isEmpty() || strEnd.isEmpty()) {
                continue;
            }

            String strFormat = Evaluator.processText(MacroThread.processForArgs(args, getTimer().getVariables()[i][3].toString()), "", "");
            String strCurve = Evaluator.processText(MacroThread.processForArgs(args, getTimer().getVariables()[i][4].toString()), "", "");
            Curve curve = Curves.getGlobalCurves().getCurve(strCurve);
            if (curve == null) {
                curve = Curves.getGlobalCurves().getCurve(getTimer().getDefaultCurve());
            }
            double r;
            double durationInSec = 2.0;
            try {
                if (!getTimer().getStrDurationInSec().isEmpty()) {
                    durationInSec = Double.parseDouble(Evaluator.processText(MacroThread.processForArgs(args, getTimer().getStrDurationInSec()), "", ""));
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
            if (DataServer.getInstance() != null) {
                Commands.updateVariableOrProperty(this, variable, value, Commands.ACTION_VARIABLE_UPDATE);
            }
        }
        this.lastUpdateVariable.put(variable, value);
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
}
