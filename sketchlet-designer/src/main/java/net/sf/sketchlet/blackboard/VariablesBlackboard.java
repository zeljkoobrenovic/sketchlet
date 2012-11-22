/*
 * DataServer.java
 *
 * Created on February 21, 2006, 1:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.sketchlet.blackboard;

import net.sf.sketchlet.common.EscapeChars;
import net.sf.sketchlet.common.XPathEvaluator;
import net.sf.sketchlet.common.template.TemplateMarkers;
import net.sf.sketchlet.net.NetUtils;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.context.VariableUpdateListener;
import net.sf.sketchlet.loaders.pluginloader.PluginInstance;
import net.sf.sketchlet.loaders.pluginloader.ScriptPluginFactory;
import net.sf.sketchlet.blackboard.evaluator.JEParser;
import net.sf.sketchlet.script.ScriptConsole;
import net.sf.sketchlet.script.ScriptPluginProxy;
import net.sf.sketchlet.util.RefreshTime;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

/**
 * @author Zeljko
 */
public class VariablesBlackboard {
    private static final Logger log = Logger.getLogger(VariablesBlackboard.class);

    private Map<String, Variable> variablesMap = new HashMap<String, Variable>();
    private List<String> variablesList = new Vector<String>();
    private static List<ScriptPluginProxy> scripts = new Vector<ScriptPluginProxy>();
    private static List scriptFiles = new Vector();
    private static boolean paused = false;
    private static boolean imageDrawnByExternalProcess = false;
    private static VariablesBlackboard instance = null;
    private static long startingTime = System.currentTimeMillis();
    private List changeClients = new Vector();
    private List<VariableOperationsListener> operationsListeners = new Vector<VariableOperationsListener>();
    private List<AdditionalVariables> additionalVariables = new Vector<AdditionalVariables>();


    enum FileImportMarkers {

        IMPORT("$import{", "}");

        private FileImportMarkers(String start, String end) {
            this.start = start;
            this.end = end;
        }

        private String start = "";
        private String end = "";

        public String start() {
            return start;
        }

        public String end() {
            return end;
        }
    }

    public VariablesBlackboard() {
        this.loadVariables();
    }

    public static Vector<String> variablesInProcessing = new Vector<String>();

    public static void protectVariable(String strVariable) {
        variablesInProcessing.add(strVariable.toLowerCase());
    }

    public static void unprotectVariable(String strVariable) {
        variablesInProcessing.remove(strVariable.toLowerCase());
    }

    public static void unprotectAllVariables() {
        variablesInProcessing.removeAllElements();
    }

    public static boolean isInProcessing(String strVariable) {
        return strVariable != null && variablesInProcessing.contains(strVariable.toLowerCase());
    }

    /**
     *
     */
    public void addVariablesUpdateListener(VariableUpdateListener client) {
        if (!this.changeClients.contains(client)) {
            this.changeClients.add(client);
        }
    }

    public void removeVariablesUpdateListener(VariableUpdateListener client) {
        if (client == null) {
            return;
        }

        this.changeClients.remove(client);
    }

    public void removeVariablesUpdateListener() {
        this.changeClients.clear();
    }

    public void removeVariablesUpdateListenerByVlass(String strPrefix) {
        final Object array[] = changeClients.toArray();
        for (int i = 0; i < array.length; i++) {
            String strClassName = array[i].getClass().getName();
            if (strClassName.startsWith(strPrefix)) {
                this.changeClients.remove(array[i]);
            }
        }
    }

    public void addVariableOperationsListener(VariableOperationsListener client) {
        if (!this.operationsListeners.contains(client)) {
            this.operationsListeners.add(client);
        }
    }

    public void removeVariablesUpdateListener(VariableOperationsListener client) {
        if (client == null) {
            return;
        }

        this.operationsListeners.remove(client);
    }

    public String getUniqueVariableName(String strName) {
        strName = strName.replace(' ', '_');

        Variable variable = getVariable(strName);

        String strPrefix = strName;

        int i = 2;
        while (variable != null) {
            strName = strPrefix + "_" + i;
            i++;
            variable = VariablesBlackboard.getInstance().getVariable(strName);
        }

        return strName;
    }

    public static boolean isInStringFunction(String exp, int n) {

        if (n > exp.length()) {
            exp = exp.substring(0, n).trim().toLowerCase();
            if (exp.endsWith("substring(")) {
                return true;
            }
            if (exp.endsWith("mid(")) {
                return true;
            }
            if (exp.endsWith("left(")) {
                return true;
            }
            if (exp.endsWith("right(")) {
                return true;
            }
        }

        return false;
    }

    public static String getTemplateFromApostrophes(String expression) {
        int _n = 0;
        String extra = "";
        while (expression.contains("'")) {
            if (_n % 2 == 0) {
                extra = "";
                if (isInStringFunction(expression, _n)) {
                    extra = "\"";
                }
                int n1 = expression.indexOf("'");
                if (n1 >= 0) {
                    int n2 = expression.indexOf("'", n1 + 1);
                    if (n2 > n1) {
                        String str = VariablesBlackboard.getInstance().getVariableValue(expression.substring(n1 + 1, n2));
                        boolean bNum = true;
                        try {
                            double d = Double.parseDouble(str);
                        } catch (Exception e) {
                            bNum = false;
                        }
                        if (!bNum) {
                            extra = "\"";
                        }
                    }
                }
                expression = expression.replaceFirst("'", extra + "<%=");
            } else {
                expression = expression.replaceFirst("'", "%>" + extra);
                extra = "";
            }
            _n++;
        }

        return expression;
    }

    public static String populateTemplate(String template) {
        return populateTemplate(template, true);
    }

    public static String populateTemplateSimple(String template, boolean encode) {
        if (VariablesBlackboard.getInstance() == null || template == null) {
            return template;
        }

        for (TemplateMarkers m : TemplateMarkers.values()) {
            while (true) {
                int pos1 = template.lastIndexOf(m.start());
                int pos2 = template.indexOf(m.end(), pos1);
                try {
                    if (pos1 >= 0 && pos2 > pos1) {
                        String variable = template.substring(pos1, pos2 + m.end().length());
                        String variableName = template.substring(pos1 + m.start().length(), pos2).trim();
                        String value = VariablesBlackboard.processForFormulas("=" + variableName);// VariablesBlackboard.variablesServer.getVariableValue(variableName);

                        if (encode) {
                            value = URLEncoder.encode(value, "UTF-8");
                        }

                        template = template.replace(variable, value);
                    } else {
                        break;
                    }
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
        while (true) {
            int pos1 = template.indexOf(FileImportMarkers.IMPORT.start());
            int pos2 = template.indexOf(FileImportMarkers.IMPORT.end(), pos1 + 1);
            try {
                if (pos1 >= 0 && pos2 > pos1) {
                    String importStatement = template.substring(pos1, pos2 + FileImportMarkers.IMPORT.end().length());
                    String strFileUrl = template.substring(pos1 + FileImportMarkers.IMPORT.start().length(), pos2).trim().trim();
                    strFileUrl = strFileUrl.replace("\"", "");
                    strFileUrl = strFileUrl.replace("'", "");
                    strFileUrl = populateTemplateSimple(strFileUrl, encode);// VariablesBlackboard.variablesServer.getVariableValue(variableName);
                    String content = FileCache.getContent(strFileUrl);
                    if (encode) {
                        content = URLEncoder.encode(content, "UTF-8");
                    }

                    template = template.replace(importStatement, content);
                } else {
                    break;
                }
            } catch (Exception e) {
                log.error(e);
            }
        }

        template = populateTemplateVelocity(template);

        return template;
    }

    public static String populateTemplateVelocity(String template) {
        if (VariablesBlackboard.getInstance() == null || template == null) {
            return template;
        }

        String start = "$velocity{";
        String end = "}";
        while (true) {
            int pos1 = template.indexOf(start);
            int pos2 = template.lastIndexOf(end);
            try {
                if (pos1 >= 0 && pos2 > pos1) {
                    String velocityTemplate = template.substring(pos1 + start.length(), pos2);
                    Velocity.init();

                    VelocityContext context = new VelocityContext();

                    context.put("name", new String("Velocity"));
                    context.put("sketchlet", SketchletContext.getInstance());

                    StringWriter sw = new StringWriter();
                    Velocity.evaluate(context, sw, "sketchlet", velocityTemplate);
                    template = template.substring(0, pos1) + sw.toString() + template.substring(pos2 + end.length());
                } else {
                    break;
                }
            } catch (Exception e) {
                break;
            }
        }

        return template;
    }

    public static String populateTemplate(String template, boolean encode) {
        if (VariablesBlackboard.getInstance() == null || template == null) {
            return template;
        }

        template = populateTemplateSimple(template, encode);

        if (template.startsWith("=")) {
            template = processForFormulas(template);
        }

        return template;
    }

    public static String processForFormulas(String template) {
        String varName = template.substring(1);
        if (varName.startsWith("{") || VariablesBlackboard.getInstance().getVariable(varName) != null) {
            template = VariablesBlackboard.getInstance().getVariableValue(varName);
        } else if (varName.startsWith("sequence{") || varName.startsWith("seq{") || varName.startsWith("sequence {") || varName.startsWith("seq {")) {
            int n = varName.indexOf("{");
            try {
                String commands = varName.substring(n + 1).trim();
                if (commands.endsWith("}")) {
                    commands = commands.substring(0, commands.length() - 1);
                }

                SketchletContext.getInstance().startCommandSequence(commands);

                int n2 = commands.lastIndexOf(";");
                if (n2 > 0) {
                    return VariablesBlackboard.populateTemplate(commands.substring(n2 + 1));
                } else {
                    return VariablesBlackboard.populateTemplate(commands);
                }
            } catch (Throwable e) {
            }

            return "";
        } else {
            varName = VariablesBlackboard.getTemplateFromApostrophes(varName);
            varName = VariablesBlackboard.populateTemplate(varName);
            Object result = JEParser.getValue(varName);

            if (result == null) {
                template = "";
            } else {
                if (result instanceof Double && varName.length() > 0) {
                    double d = ((Double) result).doubleValue();

                    String strFormat = "0";
                    if (d - (int) d == 0.0) {
                        strFormat = "0";
                    } else {
                        strFormat = "0.00";
                    }
                    DecimalFormat df = new DecimalFormat(strFormat, new DecimalFormatSymbols(Locale.US));
                    template = df.format(result);

                } else {
                    template = result.toString();
                }
            }
        }

        return template;
    }

    public static Vector<String> getVariablesInTemplate(String template) {
        Vector<String> vars = new Vector<String>();
        if (VariablesBlackboard.getInstance() == null || template == null) {
            return vars;
        }

        if (template.startsWith("=") && template.length() > 1) {
            vars.add(template.substring(1));
            return vars;
        }

        for (TemplateMarkers m : TemplateMarkers.values()) {
            while (true) {
                int pos1 = template.indexOf(m.start());
                int pos2 = template.indexOf(m.end());
                try {
                    if (pos1 >= 0 && pos2 > pos1) {
                        String variable = template.substring(pos1, pos2 + m.end().length());
                        String variableName = template.substring(pos1 + m.start().length(), pos2).trim();
                        vars.add(variableName);
                        template = template.replace(variable, "");
                    } else {
                        break;
                    }
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }

        return vars;
    }

    public void notifyVariableAdded(String variable, String value) {
        for (VariableOperationsListener l : operationsListeners) {
            l.variableAdded(variable, value);
        }
    }

    public void notifyVariableUpdated(String variable, String value) {
        for (VariableOperationsListener l : operationsListeners) {
            l.variableUpdated(variable, value);
        }
    }

    public void notifyVariableDeleted(String variable) {
        for (VariableOperationsListener l : operationsListeners) {
            l.variableDeleted(variable);
        }
    }

    public void notifyChange(final String triggerVariable, final String value, final String oldValue) {
        notifyChange(triggerVariable, value, oldValue, false);
    }

    public void notifyChange(final String triggerVariable, final String value, final String oldValue, final boolean newThread) {

        final Object array[] = changeClients.toArray();
        final int n = array.length;
        if (newThread) {
            if (VariablesBlackboard.isInProcessing(triggerVariable)) {
                return;
            }
            protectVariable(triggerVariable);
            new Thread(new Runnable() {

                public void run() {
                    notifyNetClients(triggerVariable, value, oldValue);
                    for (int i = 0; i < n; i++) {
                        final VariableUpdateListener client = (VariableUpdateListener) array[i];
                        client.variableUpdated(triggerVariable, value);
                    }
                    unprotectVariable(triggerVariable);
                }
            }).start();
        } else {
            if (VariablesBlackboard.isInProcessing(triggerVariable)) {
                return;
            }
            protectVariable(triggerVariable);
            this.notifyNetClients(triggerVariable, value, oldValue);

            for (int i = 0; i < n; i++) {
                final VariableUpdateListener client = (VariableUpdateListener) array[i];
                client.variableUpdated(triggerVariable, value);
            }
            unprotectVariable(triggerVariable);
        }
    }

    public void printInfo() {

        final Object array[] = changeClients.toArray();
        final int n = array.length;
        for (int i = 0; i < n; i++) {
            final VariableUpdateListener client = (VariableUpdateListener) array[i];
            log.info(client.getClass().getName());
        }
    }

    private void notifyNetClients(String variableName, String value, String oldValue) {
        String changedVariables[];
        String differentVariables[];

        changedVariables = new String[]{variableName};

        if (oldValue != null && oldValue.equals(value)) {
            differentVariables = new String[]{};
        } else {
            differentVariables = new String[]{variableName};
        }

        if (NetUtils.getServerTCP() != null) {
            NetUtils.getServerTCP().processTemplates(changedVariables, differentVariables);
        }
        if (NetUtils.getServerUDP() != null) {
            NetUtils.getServerUDP().processUDPTemplates(changedVariables, differentVariables);
        }

    }

    public static boolean addScript(String scriptFile) {
        boolean success = true;
        try {
            PluginInstance script = ScriptPluginFactory.getScriptPluginInstance(new File(scriptFile));
            if (script != null) {
                getScripts().add((ScriptPluginProxy) script.getInstance());
            } else {
                success = false;
                log.info("Cannot load script '" + scriptFile + "'");
            }

            VariablesBlackboard.getInstance().notifyChange("sajfh87435987", "", "");

            log.info("Communicator: added script '" + scriptFile + "'");
        } catch (Exception e) {
            log.info("Cannot load script '" + scriptFile + "'");
            // log.error(e);
        }

        return success;
    }

    public static void createScripts() {
        VariablesBlackboard.getScripts().clear();
        VariablesBlackboard.getScriptFiles().clear();

        for (int i = 0; i < ConfigurationData.getScriptFiles().size(); i++) {
            String scriptURL = "";
            try {
                scriptURL = (String) ConfigurationData.getScriptFiles().get(i);
                PluginInstance script = ScriptPluginFactory.getScriptPluginInstance(new File(scriptURL));
                if (script != null) {
                    VariablesBlackboard.getScriptFiles().add(scriptURL);
                    VariablesBlackboard.getScripts().add((ScriptPluginProxy) script.getInstance());
                }
            } catch (Exception e) {
                ScriptConsole.addLine("");
                log.info("ERROR in " + scriptURL);
                log.error(e);
            }
        }
    }

    public static ScriptPluginProxy createScript(int index, File scriptFile) {

        try {
            PluginInstance script = ScriptPluginFactory.getScriptPluginInstance(scriptFile);
            if (script != null) {
                VariablesBlackboard.getScripts().set(index, (ScriptPluginProxy) script.getInstance());
            }

            return (ScriptPluginProxy) script.getInstance();
        } catch (Exception e) {
            ScriptConsole.addLine("");
            log.info("ERROR in " + scriptFile.getName());
            log.error(e);
        }

        return null;
    }

    public void loadVariables() {
        String strMessage = "";
        try {
            long time = System.currentTimeMillis() - VariablesBlackboard.startingTime;

            Document documents[] = new Document[ConfigurationData.getInitialVariablesURLs().size()];

            Iterator iterator = ConfigurationData.getInitialVariablesURLs().iterator();

            this.getVariablesMap().clear();
            this.getVariablesList().clear();

            while (iterator.hasNext()) {
                String initVariablesFileURL = (String) iterator.next();
                strMessage = initVariablesFileURL;

                XPathEvaluator xpath = new XPathEvaluator();
                xpath.createDocumentFromInputStream(new URL(initVariablesFileURL).openStream());

                NodeList varNodes = xpath.getNodes("/variables/variable");
                XPath xp = XPathFactory.newInstance().newXPath();

                for (int i = 0; i < varNodes.getLength(); i++) {
                    Variable v = new Variable();

                    Node varNode = varNodes.item(i);
                    v.setValue(varNode.getTextContent());
                    v.setCount(0);
                    v.setName((String) xp.evaluate("@name", varNode, XPathConstants.STRING));
                    v.setGroup((String) xp.evaluate("@group", varNode, XPathConstants.STRING));
                    v.setCountFilter((int) ((Double) xp.evaluate("@count-filter", varNode, XPathConstants.NUMBER)).doubleValue());
                    v.setTimeFilterMs((int) ((Double) xp.evaluate("@time-filter", varNode, XPathConstants.NUMBER)).doubleValue());
                    v.setDescription((String) xp.evaluate("@description", varNode, XPathConstants.STRING));
                    v.setFormat((String) xp.evaluate("@format", varNode, XPathConstants.STRING));
                    v.setMin((String) xp.evaluate("@min", varNode, XPathConstants.STRING));
                    v.setMax((String) xp.evaluate("@max", varNode, XPathConstants.STRING));

                    this.getVariablesMap().put(v.getName(), v);
                    this.getVariablesList().add(v.getName());
                }

            }

            this.notifyChange("", "", "");
        } catch (Exception e) {
            log.info("Cannot open " + strMessage);
        }
    }

    public void updateVariableIfDifferent(String variableName, String value) {
        RefreshTime.update();
        if (variableName.length() == 0) {
            return;
        }
        Variable v = this.getVariable(variableName);
        if (v == null || !value.equals(v.getValue())) {
            updateVariable(variableName, value);
        }
        RefreshTime.update();
    }

    public void updateVariableIfEmpty(String variableName, String value) {
        RefreshTime.update();
        if (variableName.length() == 0) {
            return;
        }
        Variable v = this.getVariable(variableName);
        if (v == null || value.equals("")) {
            updateVariable(variableName, value);
        }
        RefreshTime.update();
    }

    public void updateVariable(String variableName, String value) {
        RefreshTime.update();
        updateVariable(variableName, value, false);
        RefreshTime.update();
    }

    public void updateVariable(String variableName, String value, boolean newThread) {
        RefreshTime.update();
        if (variableName.isEmpty()) {
            return;
        }

        if (isInProcessing(variableName)) {
            return;
        }

        Variable v = this.getVariable(variableName);

        if (v == null) {
            this.updateVariable(variableName, value, "", "");
            this.notifyVariableAdded(variableName, value);
        } else {
            v.setCount(v.getCount() + 1);

            long timeDiff = System.currentTimeMillis() - v.getTimestamp();

            if ((v.getCountFilter() <= 0 || v.getCount() % v.getCountFilter() == 0) && (timeDiff > v.getTimeFilterMs())) {
                String oldValue = v.getValue();
                v.setValue(value);
                v.boundValue();

                v.setTimestamp(System.currentTimeMillis());

                v.save();

                this.notifyChange(variableName, value, oldValue, newThread);
                this.notifyVariableUpdated(variableName, value);
            }
        }

        RefreshTime.update();
    }

    public void appendVariable(String variableName, String value) {
        RefreshTime.update();
        Variable v = this.getVariable(variableName);

        if (value.equals("<-") || value.equals("<--")) {
            cutVariableRight(variableName, 1);
        } else {
            if (v == null) {
                this.updateVariable(variableName, value);
            } else {
                this.updateVariable(variableName, v.getValue() + value);
            }
        }
        RefreshTime.update();
    }

    public void cutVariableRight(String variableName, int numOfCharacters) {
        RefreshTime.update();
        Variable v = this.getVariable(variableName);

        if (v != null && v.getValue().length() >= numOfCharacters) {
            this.updateVariable(variableName, v.getValue().substring(0, v.getValue().length() - numOfCharacters));
        }
        RefreshTime.update();
    }

    public void incrementVariable(String variableName, String strValue) {
        RefreshTime.update();
        try {
            Variable v = this.getVariable(variableName);

            if (v == null) {
                updateVariable(variableName, strValue);
                return;
            }

            try {
                int nValue = Integer.parseInt(v.getValue());
                int nIncrement = Integer.parseInt(strValue);
                this.updateVariable(variableName, "" + (nValue + nIncrement));
            } catch (NumberFormatException e) {
                try {
                    double nValue = Double.parseDouble(v.getValue());
                    double nIncrement = Double.parseDouble(strValue);
                    this.updateVariable(variableName, "" + (nValue + nIncrement));
                } catch (NumberFormatException e2) {
                    this.updateVariable(variableName, strValue);
                }
            }
        } catch (Exception e) {
        }
        RefreshTime.update();
    }

    public void updateVariable(int index, int col, String value) {
        RefreshTime.update();
        Variable v = this.getVariable(index);
        updateVariable(v, col, value);
        RefreshTime.update();
    }

    public void updateVariable(Variable v, int col, String value) {
        if (isInProcessing(v.getName())) {
            return;
        }
        RefreshTime.update();
        try {
            switch (col) {
                case 0:
                    v.setName(value);
                    break;
                case 1:
                    v.setCount(v.getCount() + 1);
                    String oldValue = v.getValue();
                    v.setValue(value);
                    v.boundValue();
                    v.save();

                    this.notifyChange(v.getName(), value, oldValue);
                    this.notifyVariableUpdated(v.getName(), value);

                    break;
                case 2:
                    v.setDescription(value);
                    break;
                case 3:
                    v.setGroup(value);
                    break;
                case 4:
                    v.setFormat(value);
                    break;
                case 5:
                    v.setMin(value);
                    break;
                case 6:
                    v.setMax(value);
                    break;
                case 7:
                    v.setCount(value.isEmpty() ? 0 : Integer.parseInt(value));
                    break;
                case 8:
                    v.setCountFilter(value.isEmpty() ? 0 : Integer.parseInt(value));
                    break;
                case 9:
                    v.setTimeFilterMs(value.isEmpty() ? 0 : Integer.parseInt(value));
                    break;
                case 10:
                    break;
            }
        } catch (Throwable e) {
            log.error(e);
        }

        RefreshTime.update();
    }

    public void setGroup(String variableName, String group) {
        Variable v = this.getVariable(variableName);
        if (v == null) {
            this.updateVariable(variableName, "", group, "");
        } else {
            v.setGroup(group);
        }
        RefreshTime.update();
    }

    public void setDesc(String variableName, String description) {
        Variable v = this.getVariable(variableName);
        if (v == null) {
            this.updateVariable(variableName, "", "", description);
        } else {
            v.setDescription(description);
        }
        RefreshTime.update();
    }

    public void updateVariable(String variableName, String value, String group, String description) {
        updateVariable(variableName, value, group, description, false);
        RefreshTime.update();
    }

    public void updateVariable(String variableName, String value, String group, String description, boolean newThread) {
        if (variableName.isEmpty()) {
            return;
        }

        if (isInProcessing(variableName)) {
            return;
        }

        try {
            Variable oldVariable = this.getVariable(variableName);
            String oldValue = oldVariable == null ? null : oldVariable.getValue();

            if (oldVariable != null) {
                oldVariable.setCount(oldVariable.getCount() + 1);
            }

            long timeDiff = oldVariable == null ? 0 : System.currentTimeMillis() - oldVariable.getTimestamp();

            if (oldVariable == null || ((oldVariable.getCountFilter() == 0 || (oldVariable.getCount() % oldVariable.getCountFilter() == 0)) && (timeDiff > oldVariable.getTimeFilterMs()))) {
                Variable v = new Variable();
                v.setName(variableName);
                v.setValue(value);
                v.boundValue();

                if (!group.isEmpty()) {
                    v.setGroup(group);
                }
                v.setCount(oldVariable == null ? 1 : oldVariable.getCount());
                v.setCountFilter(oldVariable == null ? 1 : oldVariable.getCountFilter());
                v.setTimeFilterMs(oldVariable == null ? 0 : oldVariable.getTimeFilterMs());
                if (!description.isEmpty()) {
                    v.setDescription(description);
                }
                v.setTimestamp(System.currentTimeMillis());

                this.getVariablesMap().put(v.getName(), v);
                if (!this.getVariablesList().contains(v.getName())) {
                    this.getVariablesList().add(v.getName());
                }

                this.notifyChange(variableName, value, oldValue, newThread);

                v.save();

                if (oldVariable == null) {
                    this.notifyVariableAdded(variableName, value);
                } else {
                    this.notifyVariableUpdated(variableName, value);
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
        RefreshTime.update();
    }

    public void addVariable(String variableName, String group, String description) {
        if (variableName.isEmpty()) {
            return;
        }

        if (isInProcessing(variableName)) {
            return;
        }

        try {
            Variable oldVariable = this.getVariable(variableName);

            if (oldVariable == null) {
                Variable v = new Variable();
                v.setName(variableName);
                if (!group.isEmpty()) {
                    v.setGroup(group);
                }
                v.setCount(oldVariable == null ? 1 : oldVariable.getCount());
                v.setCountFilter(oldVariable == null ? 1 : oldVariable.getCountFilter());
                v.setTimeFilterMs(oldVariable == null ? 0 : oldVariable.getTimeFilterMs());
                if (!description.isEmpty()) {
                    v.setDescription(description);
                }
                v.setTimestamp(System.currentTimeMillis());

                this.getVariablesMap().put(v.getName(), v);
                if (!this.getVariablesList().contains(v.getName())) {
                    this.getVariablesList().add(v.getName());
                }

                if (oldVariable == null) {
                    this.notifyVariableAdded(variableName, "");
                }
            } else {
                oldVariable.setGroup(group);
                oldVariable.setDescription(description);
            }
        } catch (Exception e) {
            log.error(e);
        }
        RefreshTime.update();
    }

    public void removeAll() {
        try {
            this.getVariablesMap().clear();
            this.getVariablesList().clear();

            this.notifyChange("", "", "");
            this.notifyVariableDeleted("");

            ConfigurationData.getInitialVariablesURLs().clear();
            ConfigurationData.getScriptFiles().clear();

            VariablesBlackboard.getScriptFiles().clear();
            VariablesBlackboard.getScripts().clear();
        } catch (Exception e) {
            log.error(e);
        }
        RefreshTime.update();
    }

    public void removeVariable(String variableName) {
        try {
            this.getVariablesMap().remove(variableName);
            this.getVariablesList().remove(variableName);

            this.notifyChange(variableName, "", "");
            this.notifyVariableDeleted(variableName);
        } catch (Exception e) {
            log.error(e);
        }
        RefreshTime.update();
    }

    public void removeVariables(String variableNamePattern) {
        try {
            String stringFunction;

            if (variableNamePattern.startsWith("*") && variableNamePattern.endsWith("*")) {
                stringFunction = "contains";
            } else if (variableNamePattern.endsWith("*")) {
                stringFunction = "starts-with";
            } else if (variableNamePattern.startsWith("*")) {
                stringFunction = "ends-with";
            } else {
                return;
            }

            String strVariableNameFragment = variableNamePattern.replace("*", "");

            String variables[] = new String[this.getVariablesList().size()];

            int i = 0;
            for (String variableName : this.getVariablesList()) {
                variables[i++] = variableName;
            }

            for (i = 0; i < variables.length; i++) {
                String variableName = variables[i];

                if (stringFunction.equals("contains") && variableName.contains(strVariableNameFragment)) {
                    this.removeVariable(variableName);
                } else if (stringFunction.equals("ends-with") && variableName.endsWith(strVariableNameFragment)) {
                    this.removeVariable(variableName);
                } else if (stringFunction.equals("starts-with") && variableName.startsWith(strVariableNameFragment)) {
                    this.removeVariable(variableName);
                }
            }

        } catch (Exception e) {
            log.error(e);
        }
        RefreshTime.update();
    }

    public int getNumberOfVariables() {
        return this.getVariablesMap().size();
    }

    public String getVariableValues(String listOfVariables) {
        return this.getVariableValues(listOfVariables, false);
    }

    public String getVariableValues(String listOfVariables, boolean asXML) {
        StringTokenizer tokenizer = new StringTokenizer(listOfVariables, ", \t\n");
        String values = "";
        XPath xpath = XPathFactory.newInstance().newXPath();

        while (tokenizer.hasMoreTokens()) {
            try {
                String variableName = tokenizer.nextToken().trim();

                Variable v = this.getVariable(variableName);

                String valueString = v == null ? "" : v.getValue();

                if (asXML) {
                    valueString = "<variable name='" + variableName + "'>" + EscapeChars.forHTMLTag(valueString) + "</variable>";
                }

                values += valueString;
                if (tokenizer.hasMoreTokens() && !asXML) {
                    values += ",";
                }
            } catch (Exception e) {
                log.error(e);
            }
        }

        return values;
    }

    public String getAllVariableValuesXml() {
        String values = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

        values += "<variables>";

        for (String variableName : this.getVariablesList()) {
            Variable v = this.getVariable(variableName);

            values += "    <variable name='" + v.getName() + "'";
            values += " group='" + v.getGroup() + "'";
            values += " description='" + v.getDescription() + "'>";
            values += v.getValue();
            values += "</variable>";
        }
        values += "</variables>";

        return values;
    }

    public String getAllVariableValues() {
        String values = "";

        for (String variableName : this.getVariablesList()) {
            Variable v = this.getVariable(variableName);

            values += "    <variable name='" + v.getName() + "'";
            values += " group='" + v.getGroup() + "'";
            values += " count-filter='" + v.getCountFilter() + "'";
            values += " time-filter='" + v.getTimeFilterMs() + "'";
            values += " description='" + v.getDescription() + "'>";
            values += v.getValue();
            values += "</variable>";
        }

        return values;
    }

    public String getVariableValue(String name) {
        if (name.startsWith("{")) {
            return this.getVariableValuesList(name);
        } else {
            Variable v = this.getVariable(name);

            String strValue = (v == null || v.getValue() == null) ? "" : v.getValue();

            for (int i = 0; i < 10; i++) {
                if (strValue.contains(TemplateMarkers.VELOCITY.start()) || strValue.contains(TemplateMarkers.JSP.start())) {
                    strValue = this.populateTemplate(strValue);
                } else if (strValue.startsWith("=")) {
                    strValue = this.processForFormulas(strValue);
                } else {
                    break;
                }
            }

            return strValue;
        }
    }

    public String getVariableValuesList(String name) {
        List<Variable> variables = this.getVariables(name);
        Collections.sort(variables, new Comparator<Variable>() {

            public int compare(Variable v1, Variable v2) {
                return v1.getName().compareTo(v2.getName());
            }
        });
        StringBuffer str = new StringBuffer();

        for (Variable v : variables) {
            String strValue = (v == null || v.getValue() == null) ? "" : v.getValue();
            for (int i = 0; i < 10; i++) {
                if (strValue.contains(TemplateMarkers.VELOCITY.start()) || strValue.contains(TemplateMarkers.JSP.start())) {
                    strValue = this.populateTemplate(strValue);
                } else if (strValue.startsWith("=")) {
                    strValue = this.processForFormulas(strValue);
                } else {
                    break;
                }
            }
            str.append(strValue + "\n");
        }

        return str.toString();
    }

    public Variable getVariable(int index) {
        return this.getVariable(this.getVariablesList().get(index));
    }

    public Variable getVariable(String name) {
        Variable var = this.getVariablesMap().get(name);
        if (var == null && additionalVariables != null) {
            for (AdditionalVariables vars : additionalVariables) {
                var = vars.getVariable(name);
                if (var != null) {
                    break;
                }
            }
        }

        return var;
    }

    public Vector<Variable> getVariables(String prefix) {
        int n1 = prefix.indexOf("{");
        int n2 = prefix.indexOf("}");
        Vector<Variable> variables = new Vector<Variable>();

        if (n1 >= 0 && n2 > n1) {
            prefix = prefix.substring(n1 + 1, n2);
            for (String name : this.getVariablesList()) {
                if (name.startsWith(prefix)) {
                    Variable var = this.getVariablesMap().get(name);
                    if (var == null && additionalVariables != null) {
                        for (AdditionalVariables vars : additionalVariables) {
                            var = vars.getVariable(name);
                            if (var != null) {
                                break;
                            }
                        }
                    }
                    variables.add(var);
                }
            }
        }
        return variables;
    }

    public boolean isAdditionalVariable(String name) {
        if (additionalVariables != null) {
            for (AdditionalVariables vars : additionalVariables) {
                Variable var = vars.getVariable(name);
                if (var != null) {
                    return true;
                }
            }
        }

        return false;
    }

    public void addAdditionalVariables(AdditionalVariables additionalVariables) {
        this.additionalVariables.add(additionalVariables);
    }

    public boolean variableExists(String name) {
        return this.getVariable(name) != null;
    }

    public String getVariableValue(String name, String test) {
        return getVariableValue(name);
    }

    public void printAll() {
        for (String variableName : this.getVariablesList()) {
            Variable v = this.getVariable(variableName);

            String values = "";
            values += "    <variable name='" + v.getName() + "'";
            values += " group='" + v.getGroup() + "'";
            values += " count-filter='" + v.getCountFilter() + "'";
            values += " time-filter='" + v.getTimeFilterMs() + "'";
            values += " description='" + v.getDescription() + "'>";
            values += v.getValue();
            values += "</variable>";

            log.info(values);
        }
    }

    public static String processText(String strText) {
        if (strText == null) {
            return "";
        }

        if (strText.startsWith("=")) {
            strText = strText.trim();

            if (strText.length() > 1) {
                String strVariable = strText.substring(1);
                strText = VariablesBlackboard.getInstance().getVariableValue(strVariable);
            }

        } else {
            strText = VariablesBlackboard.populateTemplate(strText, false);
        }

        return strText;
    }

    public List<String> getGroups() {
        List<String> groups = new ArrayList<String>();
        groups.add("");

        for (String strVariable : this.getVariablesList()) {
            Variable v = this.getVariable(strVariable);
            if (!groups.contains(v.getGroup())) {
                groups.add(v.getGroup());
            }
        }

        return groups;
    }

    public static boolean isPaused() {
        return paused;
    }

    public static void setPaused(boolean paused) {
        VariablesBlackboard.paused = paused;
    }

    public static VariablesBlackboard getInstance() {
        return instance;
    }

    public static void setInstance(VariablesBlackboard instance) {
        VariablesBlackboard.instance = instance;
    }

    public static List<ScriptPluginProxy> getScripts() {
        return scripts;
    }

    public static void setScripts(List<ScriptPluginProxy> scripts) {
        VariablesBlackboard.scripts = scripts;
    }

    public static List getScriptFiles() {
        return scriptFiles;
    }

    public static void setScriptFiles(List scriptFiles) {
        VariablesBlackboard.scriptFiles = scriptFiles;
    }

    public static boolean isImageDrawnByExternalProcess() {
        return imageDrawnByExternalProcess;
    }

    public static void setImageDrawnByExternalProcess(boolean imageDrawnByExternalProcess) {
        VariablesBlackboard.imageDrawnByExternalProcess = imageDrawnByExternalProcess;
    }

    public Map<String, Variable> getVariablesMap() {
        return variablesMap;
    }

    public void setVariablesMap(Hashtable<String, Variable> variablesMap) {
        this.variablesMap = variablesMap;
    }

    public List<String> getVariablesList() {
        return variablesList;
    }

    public void setVariablesList(List<String> variablesList) {
        this.variablesList = variablesList;
    }
}
