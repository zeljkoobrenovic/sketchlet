package net.sf.sketchlet.designer.programming.macros;

import net.sf.sketchlet.common.QuotedStringTokenizer;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.designer.data.Evaluator;
import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.ui.ProgressMonitor;
import net.sf.sketchlet.designer.ui.playback.PlaybackFrame;
import net.sf.sketchlet.designer.ui.playback.PlaybackPanel;
import net.sf.sketchlet.parser.JEParser;
import net.sf.sketchlet.script.RunInterface;

/**
 * @author zobrenovic
 */
public class MacroThread implements Runnable, RunInterface {

    Macro macro;
    boolean stopped = false;
    Thread t;
    String strVarPrefix;
    String strVarPostfix;
    ProgressMonitor progressMonitor;
    long id = System.currentTimeMillis();
    public static final String GOTO_SKETCH_COMMAND = "go to page";
    public static final String GOTO_SKETCH_COMMAND2 = "go to";
    public static final String GOTO_SKETCH_COMMAND3 = "goto";
    public static final String START_MACRO = "start action";
    public static final String STOP_MACRO = "stop action";
    public static final String START_TIMER = "start timer";
    public static final String PAUSE_TIMER = "pause timer";
    public static final String STOP_TIMER = "stop timer";
    public static final String PAUSE = "pause";
    public static final String PAUSE2 = "p";
    String strResult = null;

    public MacroThread(String strCommands, String strVarPrefix, String strVarPostfix) {
        String commands[] = strCommands.split(";");

        this.macro = new Macro();

        SketchletContext sc = SketchletContext.getInstance();

        int i = 0;

        for (String cmd : commands) {
            String action = "";
            String param1 = "";
            String param2 = "";
            cmd = Evaluator.processText(cmd, strVarPrefix, strVarPostfix).trim();
            if (cmd.toLowerCase().startsWith(GOTO_SKETCH_COMMAND + " ")) {
                String page = prepareParamString(cmd.substring(GOTO_SKETCH_COMMAND.length() + 1).trim(), strVarPrefix, strVarPostfix);
                if (!page.isEmpty()) {
                    action = "Go to page";
                    param1 = page;
                }
            } else if (cmd.toLowerCase().startsWith(GOTO_SKETCH_COMMAND2 + " ")) {
                String page = prepareParamString(cmd.substring(GOTO_SKETCH_COMMAND2.length() + 1).trim(), strVarPrefix, strVarPostfix);
                if (!page.isEmpty()) {
                    action = "Go to page";
                    param1 = page;
                }
            } else if (cmd.toLowerCase().startsWith(GOTO_SKETCH_COMMAND3 + " ")) {
                String page = prepareParamString(cmd.substring(GOTO_SKETCH_COMMAND3.length() + 1).trim(), strVarPrefix, strVarPostfix);
                if (!page.isEmpty()) {
                    action = "Go to page";
                    param1 = page;
                }
            } else if (cmd.toLowerCase().startsWith(START_MACRO + " ")) {
                String strMacro = prepareParamString(cmd.substring(START_MACRO.length() + 1).trim(), strVarPrefix, strVarPostfix);
                if (!strMacro.isEmpty()) {
                    action = "Start action";
                    param1 = strMacro;
                }
            } else if (cmd.toLowerCase().startsWith(STOP_MACRO + " ")) {
                String strMacro = prepareParamString(cmd.substring(STOP_MACRO.length() + 1).trim(), strVarPrefix, strVarPostfix);
                if (!strMacro.isEmpty()) {
                    action = "Stop action";
                    param1 = strMacro;
                }
            } else if (cmd.toLowerCase().startsWith(START_TIMER + " ")) {
                String strTimer = prepareParamString(cmd.substring(START_TIMER.length() + 1).trim(), strVarPrefix, strVarPostfix);
                if (!strTimer.isEmpty()) {
                    action = "Start timer";
                    param1 = strTimer;
                }
            } else if (cmd.toLowerCase().startsWith(PAUSE_TIMER + " ")) {
                String strTimer = prepareParamString(cmd.substring(PAUSE_TIMER.length() + 1).trim(), strVarPrefix, strVarPostfix);
                if (!strTimer.isEmpty()) {
                    action = "Pause timer";
                    param1 = strTimer;
                }
            } else if (cmd.toLowerCase().startsWith(STOP_TIMER + " ")) {
                String strTimer = prepareParamString(cmd.substring(STOP_TIMER.length() + 1).trim(), strVarPrefix, strVarPostfix);
                if (!strTimer.isEmpty()) {
                    action = "Stop timer";
                    param1 = strTimer;
                }
            } else if (cmd.toLowerCase().startsWith(PAUSE + " ")) {
                String strPause = prepareParamString(cmd.substring(PAUSE.length() + 1).trim(), strVarPrefix, strVarPostfix);
                if (!strPause.isEmpty()) {
                    action = "PAUSE";
                    param1 = strPause;
                }
            } else if (cmd.toLowerCase().startsWith(PAUSE2 + " ")) {
                String strPause = prepareParamString(cmd.substring(PAUSE2.length() + 1).trim(), strVarPrefix, strVarPostfix);
                if (!strPause.isEmpty()) {
                    try {
                        action = "PAUSE";
                        param1 = strPause;
                    } catch (Exception e) {
                    }
                }
            } else {
                cmd = prepareParamString(cmd, strVarPrefix, strVarPostfix);
                int n = cmd.indexOf("=");
                if (n > 0) {
                    String variable = cmd.substring(0, n);
                    String value = prepareParamString(cmd.substring(n + 1), strVarPrefix, strVarPostfix);

                    if (variable.endsWith("+")) {
                        action = "Variable increment";
                        param1 = variable.substring(0, n - 1);
                        param2 = value;
                    } else if (variable.endsWith("&")) {
                        action = "Variable append";
                        param1 = variable.substring(0, n - 1);
                        param2 = value;
                    } else {
                        action = "Variable update";
                        param1 = variable;
                        param2 = value;
                    }
                } else {
                    strResult = cmd;
                }
            }
            if (!action.isEmpty() && i < this.macro.actions.length) {
                this.macro.actions[i][0] = action;
                this.macro.actions[i][1] = param1;
                this.macro.actions[i][2] = param2;
                i++;
            }
        }

        this.strVarPrefix = strVarPrefix;
        this.strVarPostfix = strVarPostfix;
        start();
    }

    public static String prepareParamString(String param, String strVarPrefix, String strVarPostfix) {
        param = param.trim();
        if (param.startsWith("\"") && param.endsWith("\"")) {
            param = param.substring(1, param.length() - 2);
        }

        param = Evaluator.processText(param, strVarPrefix, strVarPostfix);

        return param;
    }

    public MacroThread(Macro macro, String strParams, String strVarPrefix, String strVarPostfix) {
        this(macro, strParams, strVarPrefix, strVarPostfix, null);
    }

    String args[] = null;
    public String strParams = "";

    public MacroThread(Macro macro, String strParams, String strVarPrefix, String strVarPostfix, ProgressMonitor progressMonitor) {
        this.macro = macro;
        this.strVarPrefix = strVarPrefix;
        this.strVarPostfix = strVarPostfix;
        this.progressMonitor = progressMonitor;
        this.strParams = strParams;

        args = QuotedStringTokenizer.parseArgs(strParams);

        start();
    }

    Object source = null;

    public MacroThread(Object source, Macro macro, String strParams, String strVarPrefix, String strVarPostfix, ProgressMonitor progressMonitor) {
        this.source = source;
        this.macro = macro;
        this.strVarPrefix = strVarPrefix;
        this.strVarPostfix = strVarPostfix;
        this.progressMonitor = progressMonitor;
        this.strParams = strParams;

        args = QuotedStringTokenizer.parseArgs(strParams);

        start();
    }

    public void start() {
        t = new Thread(this);
        t.start();
    }

    public void stop() {
        stopped = true;
    }

    public String getName() {
        return macro.name;
    }

    public static String processForArgs(String args[], String text) {
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i].trim();
                String template0 = "${$" + (i + 1) + "}";
                String template1 = "<%=$" + (i + 1) + "%>";
                String template2 = "=$" + (i + 1);
                text = text.replace(template0, arg);
                text = text.replace(template1, arg);
                text = text.replace(template2, arg);
            }
        }

        return text;
    }

    public int executeSequence(int startIndex) {
        Page page;
        if (PlaybackFrame.playbackFrame != null || (SketchletEditor.editorPanel != null && SketchletEditor.editorPanel.internalPlaybackPanel != null)) {
            page = PlaybackPanel.currentPage;
        } else {
            if (SketchletEditor.editorPanel == null) {
                stopped = true;
                return 0;
            }
            page = SketchletEditor.editorPanel.currentPage;
        }
        int i = 0;
        for (i = startIndex; i < macro.actions.length && !stopped; i++) {
            if (stopped) {
                return i;
            }
            if (progressMonitor != null) {
                progressMonitor.setValue(i + 1);
            }
            String action = processForArgs(args, (String) macro.actions[i][0]);
            String param1 = processForArgs(args, (String) macro.actions[i][1]);
            String param2 = processForArgs(args, (String) macro.actions[i][2]);

            if (action.equalsIgnoreCase("end") || action.equalsIgnoreCase("")) {
                return i;
            } else if (action.equalsIgnoreCase("if")) {
                param1 = DataServer.getTemplateFromApostrophes(param1);
                param1 = DataServer.populateTemplate(param1);
                Object result = JEParser.getValue(param1);
                if (result == null || !(result instanceof Double) || ((Double) result).doubleValue() == 0.0) {
                    int l = macro.levels[i];
                    while (!stopped && i < macro.actions.length) {
                        i++;
                        action = (String) macro.actions[i][0];

                        if (l == macro.levels[i] && (action.equalsIgnoreCase("end") || action.isEmpty())) {
                            break;
                        }
                    }
                } else {
                    i = executeSequence(i + 1);
                }
                continue;
            } else if (action.equalsIgnoreCase("repeat")) {
                try {
                    double repeatBlock;
                    if (param1.isEmpty() || param1.equalsIgnoreCase("forever")) {
                        repeatBlock = 0;
                    } else {
                        repeatBlock = Double.parseDouble(param1);
                    }

                    int __r = 0;
                    int __i = i;
                    while (!stopped && (repeatBlock == 0 || __r < repeatBlock)) {
                        __r++;
                        i = executeSequence(__i + 1);
                    }
                } catch (Exception e) {
                }
                if (stopped) {
                    return i;
                }
                continue;
            } else {
                boolean bContinue = page == null ? false : Commands.execute(source == null ? this : source, action, param1, param2, page.activeTimers, page.activeMacros, strVarPrefix, strVarPostfix, null);
                if (!bContinue) {
                    stopped = true;
                    break;
                }
            }
            try {
                Thread.sleep(1);
            } catch (Exception e) {
            }
        }

        return i;
    }

    public void run() {
        if (progressMonitor != null) {
            progressMonitor.onStart();
        }
        int nActive = 0;
        for (int i = 0; i < macro.actions.length && !stopped; i++) {
            String action = (String) macro.actions[i][0];
            if (!action.equals("")) {
                nActive++;
            }
        }
        if (progressMonitor != null) {
            progressMonitor.setMinimum(0);
            progressMonitor.setMaximum(nActive);
        }

        if ((PlaybackFrame.playbackFrame != null && progressMonitor != null)
                || (SketchletEditor.editorPanel != null && SketchletEditor.editorPanel.internalPlaybackPanel != null)) {
            for (int r = 0; !stopped && (r < macro.repeat || macro.repeat == 0); r++) {
                executeSequence(0);
            }
        } else {
            for (int r = 0; !stopped && (r < macro.repeat || macro.repeat == 0); r++) {
                executeSequence(0);
            }
        }

        stopped = true;
        if (progressMonitor != null) {
            progressMonitor.onStop();
        }
    }

    public boolean isStopped() {
        return this.stopped;
    }
}
