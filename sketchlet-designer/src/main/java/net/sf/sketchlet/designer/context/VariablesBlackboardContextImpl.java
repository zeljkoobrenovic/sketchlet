/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.context;

import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.communicator.server.Variable;
import net.sf.sketchlet.context.VariableUpdateListener;
import net.sf.sketchlet.context.VariablesBlackboardContext;

import javax.swing.*;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class VariablesBlackboardContextImpl extends VariablesBlackboardContext {

    @Override
    public void addVariable(String name, String group, String description) {
        DataServer.variablesServer.addVariable(name, group, description);
    }

    @Override
    public void deleteVariable(String name) {
        DataServer.variablesServer.removeVariable(name);
    }

    @Override
    public void deleteVariables(String name) {
        DataServer.variablesServer.removeVariables(name);
    }

    @Override
    public void updateVariable(String name, String value) {
        DataServer.variablesServer.updateVariable(name, value);
    }

    @Override
    public void updateVariableIfEmpty(String name, String value) {
        DataServer.variablesServer.updateVariableIfEmpty(name, value);
    }

    @Override
    public void updateVariableIfDifferent(String name, String value) {
        DataServer.variablesServer.updateVariableIfDifferent(name, value);
    }

    @Override
    public void appendTextToVariable(String name, String value) {
        DataServer.variablesServer.appendVariable(name, value);
    }

    @Override
    public void incrementVariable(String name, double value) {
        DataServer.variablesServer.incrementVariable(name, "" + value);
    }

    @Override
    public String getVariableValue(String name) {
        return DataServer.variablesServer.getVariableValue(name);
    }

    @Override
    public String getVariableGroup(String name) {
        Variable v = DataServer.variablesServer.getVariable(name);
        if (v != null) {
            return v.group;
        } else {
            return "";
        }
    }

    @Override
    public String getVariableDescription(String name) {
        Variable v = DataServer.variablesServer.getVariable(name);
        if (v != null) {
            return v.description;
        } else {
            return "";
        }
    }

    @Override
    public String populateTemplate(String text) {
        return DataServer.variablesServer.populateTemplate(text, false);
    }

    @Override
    public Vector<String> getVariableNames() {
        if (DataServer.variablesServer == null) {
            return new Vector<String>();
        } else {
            return DataServer.variablesServer.variablesVector;
        }
    }

    @Override
    public Vector<String> getVariableGroups() {
        return DataServer.variablesServer.getGroups();
    }

    @Override
    public void addVariableUpdateListener(VariableUpdateListener notifyChange) {
        DataServer.variablesServer.addVariablesUpdateListener(notifyChange);
    }

    @Override
    public void removeVariableUpdateListener(VariableUpdateListener notifyChange) {
        DataServer.variablesServer.removeVariablesUpdateListener(notifyChange);
    }

    @Override
    public boolean isPaused() {
        return DataServer.paused;
    }

    @Override
    public void populateVariablesCombo(JComboBox varCombo, boolean addEquals) {
        DataServer.populateVariablesCombo(varCombo, addEquals);
    }

    @Override
    public Vector<String> getVariablesInTemplate(String text) {
        return DataServer.getVariablesInTemplate(text);
    }

    @Override
    public long getVariableTimestampMillis(String variableName) {
        Variable v = DataServer.variablesServer.getVariable(variableName);
        if (v != null) {
            return v.timestamp;
        } else {
            return 0;
        }
    }

    @Override
    public int getVariableUpdateCount(String variableName) {
        Variable v = DataServer.variablesServer.getVariable(variableName);
        if (v != null) {
            return v.count;
        } else {
            return 0;
        }
    }
}
