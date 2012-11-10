/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.programming.macros;

import net.sf.sketchlet.common.QuotedStringTokenizer;
import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.system.PlatformManager;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.communicator.server.Variable;
import net.sf.sketchlet.context.VariableUpdateListener;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.data.Evaluator;
import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.designer.data.PropertiesInterface;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.programming.screenscripts.ScreenScripts;
import net.sf.sketchlet.designer.programming.timers.TimerThread;
import net.sf.sketchlet.designer.programming.timers.Timers;
import net.sf.sketchlet.designer.ui.desktop.ProcessConsolePanel;
import net.sf.sketchlet.designer.ui.playback.InteractionRecorder;
import net.sf.sketchlet.designer.ui.playback.PlaybackFrame;
import net.sf.sketchlet.designer.ui.playback.PlaybackPanel;
import net.sf.sketchlet.parser.JEParser;
import net.sf.sketchlet.pluginloader.PluginInstance;
import net.sf.sketchlet.pluginloader.ScriptPluginFactory;
import net.sf.sketchlet.script.RunInterface;
import net.sf.sketchlet.script.ScriptPluginProxy;
import net.sf.sketchlet.util.RefreshTime;

import javax.swing.*;
import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class Commands {

    static Hashtable<String, GlideThread> gliders = new Hashtable<String, GlideThread>();

    public static boolean execute(final Object source, final String action, String param1, String param2, Vector<TimerThread> activeTimers, Vector<RunInterface> activeMacros, String strVarPrefix, String strVarPostfix, JFrame frame) {
        boolean bContinue = true;
        param1 = Evaluator.processText(param1, strVarPrefix, strVarPostfix);
        param2 = Evaluator.processText(param2, strVarPrefix, strVarPostfix);
        RefreshTime.update();

        InteractionRecorder.addEvent(action, param1, param2);

        if (action.equalsIgnoreCase("STOP")) {
            return false;
        } else {
            if (param1.isEmpty()) {
                return true;
            }
        }

        if (action.startsWith("Variable") && param2.equals("?")) {
            Variable v = DataServer.variablesServer.getVariable(param1);
            String strDescription = "Enter value: ";
            if (v != null && !v.description.trim().equals("")) {
                strDescription = "Enter value (" + v.description + "): ";
            }

            if (PlaybackFrame.playbackFrame != null || SketchletEditor.editorPanel.internalPlaybackPanel != null) {
                if (frame == null) {
                    frame = SketchletEditor.editorFrame;
                }
                param2 = JOptionPane.showInputDialog(frame, strDescription);
            }
        }


        if (action.equalsIgnoreCase("Pause") || action.equalsIgnoreCase("Pause (seconds)")) {
            try {
                Thread.sleep((int) (Double.parseDouble(param1) * 1000));
            } catch (Exception e) {
            }
        } else if (action.equalsIgnoreCase("Variable update")) {
            if (!param1.equals("")) {
                param1 = strVarPrefix + param1 + strVarPostfix;

                Commands.updateVariableOrProperty(source, param1, param2, Commands.ACTION_VARIABLE_UPDATE);
            }
        } else if (action.equalsIgnoreCase("Variable glide")) {
            if (!param1.equals("")) {
                param1 = strVarPrefix + param1 + strVarPostfix;
                GlideThread t = gliders.get(param1);

                if (t != null) {
                    t.stop();
                }

                gliders.put(param1, new GlideThread(source, param1, param2));
            }
        } else if (action.equalsIgnoreCase("Wait Until")) {
            if (!param1.equals("")) {
                try {
                    while (true) {
                        param1 = DataServer.getTemplateFromApostrophes(param1);
                        param1 = DataServer.populateTemplate(param1);
                        Object result = JEParser.getValue(param1);
                        if (result == null || !(result instanceof Double) || ((Double) result).doubleValue() == 0.0) {
                            Thread.sleep(100);
                        } else {
                            break;
                        }
                    }

                } catch (Exception e) {
                }
            }
        } else if (action.equalsIgnoreCase("Wait For Update")) {
            final WaitingInfo waiting = new WaitingInfo();
            waiting.waiting = true;
            final String variable = param1;
            VariableUpdateListener ch = new VariableUpdateListener() {

                public void variableUpdated(String triggerVariable, String value) {
                    if (triggerVariable.equalsIgnoreCase(variable)) {
                        waiting.waiting = false;
                    }
                }
            };
            DataServer.variablesServer.addVariablesUpdateListener(ch);
            try {
                while (waiting.waiting) {
                    Thread.sleep(20);
                }
            } catch (Exception e) {
            }
            DataServer.variablesServer.removeVariablesUpdateListener(ch);

        } else if (action.equalsIgnoreCase("Variable append")) {
            if (!param1.equals("")) {
                param1 = strVarPrefix + param1 + strVarPostfix;
                Commands.updateVariableOrProperty(source, param1, param2, Commands.ACTION_VARIABLE_APPEND);
            }
        } else if (action.equalsIgnoreCase("Variable increment")) {
            if (!param1.equals("")) {
                param1 = strVarPrefix + param1 + strVarPostfix;
                Commands.updateVariableOrProperty(source, param1, param2, Commands.ACTION_VARIABLE_INCREMENT);
            }
        } else if (action.equalsIgnoreCase("Start timer")) {
            synchronized (activeTimers) {
                execute(source, "Stop timer 2", param1, param2, activeTimers, activeMacros, strVarPrefix, strVarPostfix, frame);
                boolean bContinued = false;
                for (int i = activeTimers.size() - 1; i >= 0; i--) {
                    TimerThread tt = activeTimers.elementAt(i);
                    if ((param2.trim().isEmpty() || tt.strParams.trim().equals(param2.trim())) && tt.timer.name.trim().equalsIgnoreCase(param1.trim())) {
                        tt.continueExecution();
                        bContinued = true;
                    }
                }
                if (!bContinued) {
                    if (Timers.globalTimers != null) {
                        TimerThread tt = Timers.globalTimers.startTimerThread(param1, param2, activeTimers);
                    }
                }
            }
        } else if (action.equalsIgnoreCase("Stop timer")) {
            synchronized (activeTimers) {
                for (int i = activeTimers.size() - 1; i >= 0; i--) {
                    TimerThread tt = activeTimers.elementAt(i);
                    if ((param2.trim().isEmpty() || tt.strParams.trim().equals(param2.trim())) && tt.timer.name.trim().equalsIgnoreCase(param1.trim())) {
                        tt.stop();
                        activeTimers.remove(i);
                    }
                }
            }
        } else if (action.equalsIgnoreCase("Stop timer 2")) {
            synchronized (activeTimers) {
                for (int i = activeTimers.size() - 1; i >= 0; i--) {
                    TimerThread tt = activeTimers.elementAt(i);
                    if (tt.strParams.trim().equals(param2.trim()) && tt.timer.name.trim().equalsIgnoreCase(param1.trim()) && !tt.isPaused()) {
                        tt.stop();
                        activeTimers.remove(i);
                    }
                }
            }
        } else if (action.equalsIgnoreCase("Pause timer")) {
            synchronized (activeTimers) {
                for (int i = activeTimers.size() - 1; i >= 0; i--) {
                    TimerThread tt = activeTimers.elementAt(i);
                    if ((param2.trim().isEmpty() || tt.strParams.trim().equals(param2.trim())) && tt.timer.name.trim().equalsIgnoreCase(param1.trim())) {
                        tt.pause();
                    }
                }
            }
        } else if (action.equalsIgnoreCase("Go to page") || action.equalsIgnoreCase("Go to sketch")) {
            if (PlaybackFrame.playbackFrame != null && PlaybackFrame.playbackFrame[0] != null && PlaybackFrame.playbackFrame[0].isVisible()) {
                PlaybackFrame.playbackFrame[0].playbackPanel.processAction(source, param1, "", "", "");
                for (int i = 0; i < PlaybackFrame.playbackFrame.length; i++) {
                    PlaybackFrame.playbackFrame[i].setTitle(PlaybackPanel.currentPage.title);
                    PlaybackFrame.playbackFrame[i].enableControls();
                    PlaybackFrame.playbackFrame[i].playbackPanel.repaint();
                }
                bContinue = param1.trim().equals("");
            } else if (!param1.trim().isEmpty()) {
                SketchletEditor.editorPanel.selectSketch(param1.trim());
                bContinue = param1.trim().equals("");
            }
        } else if ((action.equalsIgnoreCase("Call Macro") || action.equalsIgnoreCase("Start Action") || action.equalsIgnoreCase("Start Macro"))) {
            if (Macros.globalMacros != null) {
                if (param1.trim().startsWith("{") && param2.trim().endsWith("}")) {
                    param1 = param1.substring(1).trim();
                    param1.substring(0, param1.length() - 1);
                    param2 = param2.trim();
                    if (param2.startsWith("{")) {
                        param2.substring(1);
                    }
                    if (param2.endsWith("}")) {
                        param2.substring(0, param2.length() - 1);
                    }
                    MacroThread mt = Macros.globalMacros.startMacroThreadFromString(param1 + ";" + param2, strVarPrefix, strVarPostfix);
                    if (activeMacros != null) {
                        activeMacros.add(mt);
                    }
                } else if (!Macros.globalMacros.macroExists(param1) && param1.toLowerCase().startsWith("screen:")) {
                    if (ScreenScripts.publicScriptRunner != null) {
                        ScreenScripts.publicScriptRunner.executeScreenAction(param1.substring(7));
                    }
                } else if (!Macros.globalMacros.macroExists(param1) && param1.toLowerCase().startsWith("script:")) {
                    try {
                        // final ScriptPluginProxy script = ScriptFactory.getScriptFromFile(SketchletContext.getInstance(), new File(SketchletContextUtils.getCurrentProjectDir() + SketchletContextUtils.sketchletDataDir() + "/scripts/" + param1.substring(7)));
                        final PluginInstance script = ScriptPluginFactory.getScriptPluginInstance(new File(SketchletContextUtils.getCurrentProjectDir() + SketchletContextUtils.sketchletDataDir() + "/scripts/" + param1.substring(7)));
                        if (script != null) {
                            new Thread(new Runnable() {

                                public void run() {
                                    ((ScriptPluginProxy) script.getInstance()).start();
                                }
                            }).start();
                            if (activeMacros != null) {
                                activeMacros.add((ScriptPluginProxy) script.getInstance());
                            }
                        }
                    } catch (Exception e) {
                    }
                } else if (!Macros.globalMacros.macroExists(param1) && param1.toLowerCase().startsWith("service:")) {
                    try {
                        ProcessConsolePanel service = null;
                        for (int i = 0; i < Workspace.consolePane.tabbedPane.getTabCount(); i++) {
                            service = (ProcessConsolePanel) Workspace.consolePane.tabbedPane.getComponentAt(i);
                            if (service.titleField.getText().trim().equalsIgnoreCase(param1.substring(8).trim())) {
                                break;
                            }
                            service = null;
                        }
                        if (service != null) {
                            if (activeMacros != null) {
                                service.start();
                                activeMacros.add(service);
                            }
                        }
                    } catch (Exception e) {
                    }
                } else {
                    MacroThread mt = Macros.globalMacros.startMacroThread(param1, param2, strVarPrefix, strVarPostfix);
                    if (activeMacros != null) {
                        activeMacros.add(mt);
                    }
                }
            }
        } else if (action.equalsIgnoreCase("Start Sequence")) {
            if (Macros.globalMacros != null) {
                MacroThread mt = Macros.globalMacros.startMacroThreadFromString(param1 + ";" + param2, strVarPrefix, strVarPostfix);
                if (activeMacros != null) {
                    activeMacros.add(mt);
                }
            }
        } else if ((action.equalsIgnoreCase("Stop Action"))) {
            // MacroThread macroThread = null;
            if (Macros.globalMacros != null && activeMacros != null) {
                for (int i = activeMacros.size() - 1; i >= 0; i--) {
                    /*                    RunInterface ri = activeMacros.elementAt(i);
                    if (ri instanceof MacroThread) {
                    MacroThread mt = (MacroThread) ri;
                    if (mt.macro.name.equalsIgnoreCase(param1)) {
                    mt.stop();
                    macroThread = mt;
                    activeMacros.remove(i);
                    }
                    }*/
                    RunInterface ri = activeMacros.elementAt(i);
                    if (ri instanceof MacroThread) {
                        MacroThread mri = (MacroThread) ri;
                        if ((param2.trim().isEmpty() || mri.strParams.trim().equals(param2.trim())) && ri.getName().trim().equalsIgnoreCase(param1.trim())) {
                            ri.stop();
                            activeMacros.remove(i);
                            break;
                        }
                    } else if (ri.getName().trim().equalsIgnoreCase(param1.trim())) {
                        ri.stop();
                        activeMacros.remove(i);
                        break;
                    }
                }
            }
        } else if (action.equalsIgnoreCase("Open program/file")) {
            if (!(param1.contains("/") || param1.contains("\\"))) {
                param1 = SketchletContextUtils.getCurrentProjectDir() + param1;
            }

            String command = PlatformManager.getDefaultFileOpenerCommand().replace("$f", param1);
            try {
                if (param2.isEmpty()) {
                    Runtime.getRuntime().exec(command);
                } else {
                    if (param1.contains(" ") && !param1.contains("\"")) {
                        param1 = "\"" + param1 + "\"";
                    }
                    String args[] = QuotedStringTokenizer.parseArgs(param1 + " " + param2);
                    ProcessBuilder processBuilder = new ProcessBuilder(args);
                    processBuilder.redirectErrorStream(true);
                    Process theProcess = processBuilder.start();
                }
            } catch (Exception e) {
            }
        }

        return bContinue;
    }

    public final static int ACTION_VARIABLE_UPDATE = 0;
    public final static int ACTION_VARIABLE_APPEND = 1;
    public final static int ACTION_VARIABLE_INCREMENT = 2;
    public final static int ACTION_VARIABLE_CUT_RIGHT = 3;
    public final static int ACTION_VARIABLE_GLIDE = 4;
    public static Vector<String> glidingVariables = new Vector<String>();

    public static void updateVariableOrProperty(Object source, String param1, String param2, int actionType) {
        updateVariableOrProperty(source, param1, param2, actionType, false);
    }

    static int count = 0;

    public static void updateVariableOrProperty(Object source, String param1, String param2, int actionType, boolean bDifferent) {
        if (SketchletEditor.editorPanel == null || SketchletEditor.editorPanel.currentPage == null) {
            return;
        }

        Page page = SketchletEditor.editorPanel.currentPage;
        if (PlaybackFrame.playbackFrame != null && PlaybackFrame.playbackFrame[0] != null && PlaybackFrame.playbackFrame[0].isVisible()) {
            page = PlaybackPanel.currentPage;
        }

        param1 = param1.trim();
        if (param1.startsWith("[") && param1.endsWith("]")) {
            param1 = param1.substring(1, param1.length() - 1).trim();
            int n2 = param1.indexOf(".");

            try {
                String strID;
                String strProperty;
                if (param1.contains(".")) {
                    strID = param1.substring(0, n2).trim();
                    strProperty = param1.substring(n2 + 1).trim();
                } else {
                    strID = "this";
                    strProperty = param1.trim();
                }

                PropertiesInterface properties;
                if (strID.equalsIgnoreCase("sketch") || strID.equalsIgnoreCase("page")) {
                    properties = page;
                } else if (strID.equalsIgnoreCase("this") && source instanceof PropertiesInterface) {
                    properties = (PropertiesInterface) source;
                } else {
                    properties = page.regions.getRegionByName(strID);
                }
                if (properties != null) {
                    if (actionType == Commands.ACTION_VARIABLE_APPEND && (param2.equals("<-") || param2.equals("<--"))) {
                        actionType = Commands.ACTION_VARIABLE_CUT_RIGHT;
                        param2 = "1";
                    }
                    if (actionType == Commands.ACTION_VARIABLE_UPDATE) {
                    } else if (actionType == Commands.ACTION_VARIABLE_APPEND) {
                        param2 = properties.getProperty(strProperty) + param2;
                    } else if (actionType == Commands.ACTION_VARIABLE_CUT_RIGHT) {
                        int len = Integer.parseInt(param2);
                        param2 = properties.getProperty(strProperty);
                        if (param2.length() >= len) {
                            param2 = param2.substring(0, param2.length() - len);
                        }
                    } else if (actionType == Commands.ACTION_VARIABLE_INCREMENT) {
                        double inc = 1;
                        if (!param2.isEmpty()) {
                            inc = Double.parseDouble(param2);
                            double result = Double.parseDouble(properties.getProperty(strProperty)) + inc;
                            param2 = "" + result;
                        }
                    }
                    if (!bDifferent) {
                        properties.setProperty(strProperty, param2);
                        DataServer.variablesServer.notifyChange(param1, param2, "");
                    } else {
                        String strCurrentValue = properties.getProperty(strProperty);
                        if (!strCurrentValue.equals(param2)) {
                            properties.setProperty(strProperty, param2);
                            DataServer.variablesServer.notifyChange(param1, param2, "");
                        }
                    }
                }
            } catch (Exception e) {
                // e.printStackTrace();
            }
        } else {
            try {
                Thread.sleep(1);
            } catch (Exception e) {
            }
            switch (actionType) {
                case Commands.ACTION_VARIABLE_UPDATE:
                    if (!bDifferent) {
                        DataServer.variablesServer.updateVariable(param1, param2);
                    } else {
                        DataServer.variablesServer.updateVariableIfDifferent(param1, param2);
                    }
                    break;
                case Commands.ACTION_VARIABLE_APPEND:
                    DataServer.variablesServer.appendVariable(param1, param2);
                    break;
                case Commands.ACTION_VARIABLE_INCREMENT:
                    if (param2.trim().equals("")) {
                        param2 = "1";
                    }
                    DataServer.variablesServer.incrementVariable(param1, param2);
                    break;
                case Commands.ACTION_VARIABLE_CUT_RIGHT:
                    try {
                        DataServer.variablesServer.cutVariableRight(param1, Integer.parseInt(param2));
                    } catch (Exception e) {
                    }
                    break;
            }

            // amico.communicator.MainFrame.mainFrame.panel1.bTableDirty = true;
            // find this
        }
    }

    static class WaitingInfo {

        boolean waiting = false;
    }

    static class GlideThread implements Runnable {

        Thread t = new Thread(this);
        String param1 = "";
        String param2 = "";
        Object source = null;
        boolean stopped = false;

        public GlideThread(Object Source, String param1, String param2) {
            this.source = source;
            this.param1 = param1;
            this.param2 = param2;
            t.start();
        }

        public void run() {
            try {
                String param = param2.replace(" ", ",").trim();
                String params[] = param.split(",");

                double start = 0.0;
                double end = 1.0;
                double dur = 2.0;

                if (params.length == 2) {
                    start = Double.parseDouble(DataServer.variablesServer.getVariableValue(param1));
                    end = Double.parseDouble(params[0]);
                    dur = Double.parseDouble(params[1]);
                } else if (params.length == 3) {
                    start = Double.parseDouble(params[0]);
                    end = Double.parseDouble(params[1]);
                    dur = Double.parseDouble(params[2]);
                } else {
                    return;
                }

                long startTime = System.currentTimeMillis();
                double durMs = dur * 1000;
                double currMs;
                while (!stopped && ((currMs = (System.currentTimeMillis() - startTime)) <= durMs)) {
                    double rel = currMs / durMs;
                    double val = start + (end - start) * rel;
                    Commands.updateVariableOrProperty(source, param1, "" + val, Commands.ACTION_VARIABLE_UPDATE);
                    Thread.sleep(50);
                }
                Commands.updateVariableOrProperty(source, param1, "" + end, Commands.ACTION_VARIABLE_UPDATE);

            } catch (Exception e) {
                e.printStackTrace();
            }

            gliders.remove(this);
        }

        public void stop() {
            stopped = true;
        }
    }
}
