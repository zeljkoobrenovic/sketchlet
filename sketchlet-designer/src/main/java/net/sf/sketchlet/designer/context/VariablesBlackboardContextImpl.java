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
import java.util.List;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class VariablesBlackboardContextImpl extends VariablesBlackboardContext {

    @Override
    public void addVariable(String name, String group, String description) {
        DataServer.getInstance().addVariable(name, group, description);
    }

    @Override
    public void deleteVariable(String name) {
        DataServer.getInstance().removeVariable(name);
    }

    @Override
    public void deleteVariables(String name) {
        DataServer.getInstance().removeVariables(name);
    }

    @Override
    public void updateVariable(String name, String value) {
        DataServer.getInstance().updateVariable(name, value);
    }

    @Override
    public void updateVariableIfEmpty(String name, String value) {
        DataServer.getInstance().updateVariableIfEmpty(name, value);
    }

    @Override
    public void updateVariableIfDifferent(String name, String value) {
        DataServer.getInstance().updateVariableIfDifferent(name, value);
    }

    @Override
    public void appendTextToVariable(String name, String value) {
        DataServer.getInstance().appendVariable(name, value);
    }

    @Override
    public void incrementVariable(String name, double value) {
        DataServer.getInstance().incrementVariable(name, "" + value);
    }

    @Override
    public String getVariableValue(String name) {
        return DataServer.getInstance().getVariableValue(name);
    }

    @Override
    public String getVariableGroup(String name) {
        Variable v = DataServer.getInstance().getVariable(name);
        if (v != null) {
            return v.getGroup();
        } else {
            return "";
        }
    }

    @Override
    public String getVariableDescription(String name) {
        Variable v = DataServer.getInstance().getVariable(name);
        if (v != null) {
            return v.getDescription();
        } else {
            return "";
        }
    }

    @Override
    public String populateTemplate(String text) {
        return DataServer.getInstance().populateTemplate(text, false);
    }

    @Override
    public List<String> getVariableNames() {
        if (DataServer.getInstance() == null) {
            return new Vector<String>();
        } else {
            return DataServer.getInstance().variablesVector;
        }
    }

    @Override
    public List<String> getVariableGroups() {
        return DataServer.getInstance().getGroups();
    }

    @Override
    public void addVariableUpdateListener(VariableUpdateListener notifyChange) {
        DataServer.getInstance().addVariablesUpdateListener(notifyChange);
    }

    @Override
    public void removeVariableUpdateListener(VariableUpdateListener notifyChange) {
        DataServer.getInstance().removeVariablesUpdateListener(notifyChange);
    }

    @Override
    public boolean isPaused() {
        return DataServer.isPaused();
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
        Variable v = DataServer.getInstance().getVariable(variableName);
        if (v != null) {
            return v.getTimestamp();
        } else {
            return 0;
        }
    }

    @Override
    public int getVariableUpdateCount(String variableName) {
        Variable v = DataServer.getInstance().getVariable(variableName);
        if (v != null) {
            return v.getCount();
        } else {
            return 0;
        }
    }
}
