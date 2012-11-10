/*
 * ScriptPluginProxy.java
 *
 * Created on April 21, 2008, 2:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.sketchlet.script;

import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.context.SketchletGraphicsContext;
import net.sf.sketchlet.context.VariablesBlackboardContext;
import net.sf.sketchlet.util.UtilContext;

import javax.script.ScriptEngine;
import javax.swing.*;
import javax.swing.table.TableColumn;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * @author cuypers
 */
public class ScriptPluginProxy implements RunInterface {

    public String status = "";
    public String strFile = "";
    public String scriptFilePath = null;
    //public boolean updating = false;
    public boolean error = false;
    public File scriptFile;
    //public boolean stopped = false;
    // public static SketchletContext apiFactory;
    SketchletContext api;
    SketchletGraphicsContext graphicsApi;
    protected ScriptEngine engine;
    protected List<String> contextVariableTypes = new Vector<String>();
    protected List<String> contextVariables = new Vector<String>();

    /**
     * Creates a new instance of ScriptPluginProxy
     */
    public ScriptPluginProxy(File scriptFile) {
        this.init(scriptFile);
    }

    public void init(File scriptFile) {
        this.scriptFile = scriptFile;
        if (SketchletContext.getInstance() != null) {
            api = SketchletContext.getInstance();
        }
        if (SketchletGraphicsContext.getInstance() != null) {
            graphicsApi = SketchletGraphicsContext.getInstance();
        }
        if (SketchletContext.getInstance().isApplicationReady()) {
            this.scriptFilePath = scriptFile.getPath();
            this.strFile = scriptFile.getName();
        }
    }

    @Override
    public void start() {
        this.status = "running";

        if (scriptFile != null) {
            load(scriptFile);
        }
        this.status = "done";
    }

    @Override
    public void stop() {
        // api.stopped = true;
        status = "stopped";
    }

    public String getName() {
        if (this.scriptFile != null) {
            return "Script:" + scriptFile.getName();
        }
        return "";
    }

    public void addVariableToContextDescription(String type, String name) {
        contextVariables.add(name);
        contextVariableTypes.add(type);
    }

    public void showContext(JFrame frame) {
        setContext(null);
        Object[][] data = new Object[contextVariables.size()][2];
        Object[] columnNames = new String[]{"Type", "Variable"};

        for (int i = 0; i < data.length; i++) {
            data[i][0] = contextVariableTypes.get(i);
            data[i][1] = contextVariables.get(i);
        }

        JTable table = new JTable(data, columnNames);
        TableColumn col = table.getColumnModel().getColumn(0);
        col.setPreferredWidth(100);
        col.setMaxWidth(100);

        JOptionPane.showMessageDialog(frame, new JScrollPane(table));
    }

    private static String[] getMethod(Method method, String prefix) {
        String type = method.getReturnType().getSimpleName();
        String name = method.getName();
        String params = "";
        for (Class param : method.getParameterTypes()) {
            if (!params.isEmpty()) {
                params += ",";
            }
            params += param.getSimpleName();
        }

        return new String[]{type, prefix + name, params};
    }

    public static String[][] getMethods() {
        List<String[]> list = new ArrayList<String[]>();
        for (Method method : SketchletContext.class.getDeclaredMethods()) {
            list.add(getMethod(method, "sketchlet."));
        }
        for (Method method : SketchletGraphicsContext.class.getDeclaredMethods()) {
            list.add(getMethod(method, "graphics."));
        }

        return list.toArray(new String[list.size()][]);
    }

    public void showExtensions(JFrame frame) {
        String[][] data = getMethods();

        Object[] columnNames = new String[]{"Returns", "Method", "Parameters"};
        JTable table = new JTable(data, columnNames);
        TableColumn col = table.getColumnModel().getColumn(0);
        col.setPreferredWidth(750);
        col.setMaxWidth(100);
        col = table.getColumnModel().getColumn(1);
        col.setPreferredWidth(195);
        col.setMaxWidth(195);

        JOptionPane.showMessageDialog(frame, new JScrollPane(table));
    }

    public void updateContext(String variable, String strValue) {
        if (engine != null) {
            variable = getUniqueName(variable);
            updateAutoType(variable, strValue, engine);
        }
    }

    public void setContext(ScriptEngine engine) {
        contextVariables.clear();
        contextVariableTypes.clear();
        if (engine != null) {
            engine.put("amico", api);
            engine.put("sketchify", api);
            engine.put("sketchlet", api);
            engine.put("graphics", graphicsApi);
        }
        for (String strVar : VariablesBlackboardContext.getInstance().getVariableNames()) {
            try {
                strVar = getUniqueName(strVar);
                String strValue = SketchletContext.getInstance().getVariableValue(strVar);

                updateAutoType(strVar, strValue, engine);
            } catch (Exception e) {
            }
        }
    }

    public void updateAutoType(String strVar, String strValue, ScriptEngine engine) {
        try {
            Integer intValue = new Integer(strValue);
            if (engine != null) {
                engine.put(strVar, intValue);
            }
            addVariableToContextDescription("Integer", strVar);
        } catch (Exception e1) {
            try {
                Double doubleValue = new Double(strValue);
                if (engine != null) {
                    engine.put(strVar, doubleValue);
                }
                addVariableToContextDescription("Double", strVar);
            } catch (Exception e2) {
                if (engine != null) {
                    engine.put(strVar, strValue);
                }
                addVariableToContextDescription("String", strVar);
            }
        }
    }

    public String getUniqueName(String strVar) {
        StringBuffer strVarBuff = new StringBuffer(strVar);
        for (int i = 0; i < strVarBuff.length(); i++) {
            char c = strVarBuff.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_')) {
                strVarBuff.setCharAt(i, '_');
            }
        }
        strVar = strVarBuff.toString();

        if (strVar.charAt(0) >= '0' && strVar.charAt(0) <= '9') {
            strVar = "_" + strVar;
        }
        /*        while (contextVariables.contains(strVar)) {
        strVar = "_" + strVar;
        }*/

        return strVar;
    }

    public void changePerformed(Object sender, String triggerVariable, String value, String oldValue) {
    }

    public void callScript(Object sender, String triggerVariable, String value, String oldValue) throws Exception {
    }

    public void load(File file) {
        FileReader in = null;
        try {
            in = new FileReader(file);
            int n = strFile.lastIndexOf("/");
            if (n >= 0) {
                strFile = strFile.substring(n + 1);
            }

            loadScript(in);
            // new LoadScriptThread( this, in );

            error = false;
        } catch (Exception ex) {
            error = true;

            ex.printStackTrace();
            ScriptConsole.addLine("");
            ScriptConsole.addLine("* ERROR in " + strFile);
            ScriptConsole.addLine("    " + ex.getMessage() + " " + ex.getLocalizedMessage());
            ScriptConsole.addLine("    Script '" + strFile + "' stopped. To restart the script, edit '" + strFile + "' and fix the error.");

            this.status = "ERROR (see console)";
            UtilContext.getInstance().refreshScriptTable();
        }

        try {
            if (in != null) {
                in.close();
            }
        } catch (Exception e) {
        }
    }

    public void loadScript(FileReader file) throws Exception {
    }

    public void update(String variable, int value) {
        api.updateVariable(variable, value + "");
    }

    public void update(String variable, double value) {
        api.updateVariable(variable, value + "");
    }

    public void pause(double seconds) {
        api.pause(seconds);
    }

    public void waitForUpdate(final String variable) {
        api.waitForVariableUpdate(variable);
    }

    public void update(String variable, String value) {
        api.updateVariable(variable, value);
    }

    public String askFile(String title) {
        return api.askFile(title);
    }

    public String askFolder(String title) {
        return api.askFolder(title);
    }

    public String ask(String question) {
        return api.ask(question);
    }

    public String askString(String question) {
        return ask(question);
    }

    public int askInteger(String question) {
        return api.askInteger(question);
    }

    public double askDouble(String question) {
        return api.askDouble(question);
    }

    public String get(String variable) {
        return api.getVariableValue(variable);
    }

    public void goToSketch(String name) {
        api.goToPage(name);
    }

    public void startMacro(String name) {
        api.startMacro(name);
    }

    public void stopMacro(String name) {
        api.stopMacro(name);
    }

    public void startTimer(String name) {
        api.startTimer(name);
    }

    public void stopTimer(String name) {
        api.stopTimer(name);
    }
}

class LoadScriptThread implements Runnable {

    ScriptPluginProxy script;
    FileReader file;
    Thread t = new Thread(this);

    public LoadScriptThread(ScriptPluginProxy script, FileReader file) {
        this.script = script;
        this.file = file;

        this.t.start();
    }

    public void run() {
        try {
            this.script.loadScript(file);
        } catch (Exception ex) {
            this.script.error = true;

            ex.printStackTrace();
            ScriptConsole.addLine("");
            ScriptConsole.addLine("* ERROR in " + this.script.strFile);
            ScriptConsole.addLine("    " + ex.getMessage());
            ScriptConsole.addLine("    Script '" + this.script.strFile + "' stopped. To restart the script, edit '" + this.script.strFile + "' and fix the error.");

            this.script.status = "ERROR (see console)";
            UtilContext.getInstance().refreshScriptTable();
        }
    }
}
