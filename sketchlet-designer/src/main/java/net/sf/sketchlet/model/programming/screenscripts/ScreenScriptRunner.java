/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.model.programming.screenscripts;

import net.sf.sketchlet.common.template.TemplateMarkers;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.context.VariableUpdateListener;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class ScreenScriptRunner implements VariableUpdateListener {

    private String scriptDir;
    private Vector<ScreenScript> scripts = new Vector<ScreenScript>();
    private boolean stopped = false;

    public ScreenScriptRunner(String scriptDir) {
        this.scriptDir = scriptDir;
        loadScripts();
    }

    public void variableUpdated(String triggerVariable, String value) {
        if (triggerVariable.trim().equals("")) {
            return;
        }
        //DataServer.protectVariable(triggerVariable);
        if (TemplateMarkers.containsStartMarker(triggerVariable)) {
            triggerVariable = DataServer.populateTemplate(triggerVariable);
        }

        for (ScreenScript ss : getScripts()) {
            boolean bRun = false;

            if (ss.getInfo().isWhenAllConditions()) {
                bRun = false;
                for (Condition c : ss.getConditions()) {
                    if (!c.conditionSatisfied()) {
                        bRun = false;
                        break;
                    } else {
                        if (c.getVariable().equals(triggerVariable)) {
                            bRun = true;
                        }
                    }
                }
            } else {
                bRun = false;
                for (Condition c : ss.getConditions()) {
                    if (c.getVariable().equals(triggerVariable) && c.conditionSatisfied()) {
                        bRun = true;
                        break;
                    }
                }
            }
            if (bRun) {
                ss.run();
            }
        }
        //DataServer.unprotectVariable(triggerVariable);
    }

    public void executeScreenAction(String strAction) {
        for (ScreenScript s : this.getScripts()) {
            if (s.getInfo().getName().equalsIgnoreCase(strAction)) {
                s.run();
                break;
            }
        }
    }

    public void setCombos(JComboBox comboBox) {
        for (ScreenScript s : ScreenScripts.getPublicScriptRunner().getScripts()) {
            comboBox.addItem("Screen:" + s.getInfo().getName());
        }
    }

    public void loadScripts() {
        getScripts().removeAllElements();

        new File(scriptDir).mkdirs();

        File dir = new File(this.scriptDir);
        File scriptFiles[] = dir.listFiles();

        for (int i = 0; i < scriptFiles.length; i++) {
            File file = scriptFiles[i];

            if (!file.getName().endsWith(".txt")) {
                continue;
            }

            ScreenScript ss = new ScreenScript(this);

            loadScreenScript(ss.getInfo(), file, ss.getConditions(), ss.getActions());

            getScripts().add(ss);
        }
    }

    public static ScreenScriptInfo loadScreenScript(ScreenScriptInfo s, File scriptFile, List<Condition> conditions, List<RobotAction> actions) {
        try {
            conditions.clear();
            actions.clear();

            FileReader fileReader = new FileReader(scriptFile);
            BufferedReader in = new BufferedReader(fileReader);

            String line;

            RobotAction currentAction = null;
            Condition currentCondition = null;

            String param = "";

            s.setName(scriptFile.getName().replace(".txt", ""));

            while ((line = in.readLine()) != null) {
                line = line.trim();

                int n = line.indexOf(" ");

                if (n > 0 && n < line.length() - 1) {
                    param = line.substring(n + 1).trim();
                } else {
                    param = "";
                }

                if (line.startsWith("WhenAllCondition")) {
                    s.setWhenAllConditions(param.equals("true"));
                } else if (line.startsWith("AddCondition")) {
                    if (currentAction != null) {
                        actions.add(currentAction);
                        currentAction = null;
                    }
                    if (currentCondition != null) {
                        conditions.add(currentCondition);
                    }
                    currentCondition = new Condition();
                } else if (line.startsWith("AddAction")) {
                    if (currentAction != null) {
                        actions.add(currentAction);
                    }
                    if (currentCondition != null) {
                        conditions.add(currentCondition);
                        currentCondition = null;
                    }
                    currentAction = ActionFactory.createAction(param);
                } else if (line.equals("")) {
                    if (currentAction != null) {
                        actions.add(currentAction);
                        currentAction = null;
                    }
                    if (currentCondition != null) {
                        conditions.add(currentCondition);
                        currentCondition = null;
                    }
                } else if (currentCondition != null && line.startsWith("Variable")) {
                    currentCondition.setVariable(param);
                } else if (currentCondition != null && line.startsWith("Operator")) {
                    currentCondition.setOperator(param);
                } else if (currentCondition != null && line.startsWith("Value")) {
                    currentCondition.setValue(param);
                } else if (currentAction != null && line.startsWith("Parameters")) {
                    currentAction.parameters = param;
                }
            }

            if (currentAction != null) {
                actions.add(currentAction);
                currentAction = null;
            }
            if (currentCondition != null) {
                conditions.add(currentCondition);
                currentCondition = null;
            }

            fileReader.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //if (conditions.size() == 0) {
        //    conditions.add(new Condition());
        //}

        return s;
    }

    public Vector<ScreenScript> getScripts() {
        return scripts;
    }

    public void setScripts(Vector<ScreenScript> scripts) {
        this.scripts = scripts;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
}
