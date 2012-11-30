package net.sf.sketchlet.designer.context;

import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;
import net.sf.sketchlet.framework.blackboard.Variable;
import net.sf.sketchlet.context.VariableUpdateListener;
import net.sf.sketchlet.context.VariablesBlackboardContext;
import net.sf.sketchlet.designer.editor.ui.UIUtils;

import javax.swing.*;
import java.util.List;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class VariablesBlackboardContextImpl extends VariablesBlackboardContext {

    @Override
    public void addVariable(String name, String group, String description) {
        VariablesBlackboard.getInstance().addVariable(name, group, description);
    }

    @Override
    public void deleteVariable(String name) {
        VariablesBlackboard.getInstance().removeVariable(name);
    }

    @Override
    public void deleteVariables(String name) {
        VariablesBlackboard.getInstance().removeVariables(name);
    }

    @Override
    public void updateVariable(String name, String value) {
        VariablesBlackboard.getInstance().updateVariable(name, value);
    }

    @Override
    public void updateVariableIfEmpty(String name, String value) {
        VariablesBlackboard.getInstance().updateVariableIfEmpty(name, value);
    }

    @Override
    public void updateVariableIfDifferent(String name, String value) {
        VariablesBlackboard.getInstance().updateVariableIfDifferent(name, value);
    }

    @Override
    public void appendTextToVariable(String name, String value) {
        VariablesBlackboard.getInstance().appendVariable(name, value);
    }

    @Override
    public void incrementVariable(String name, double value) {
        VariablesBlackboard.getInstance().incrementVariable(name, "" + value);
    }

    @Override
    public String getVariableValue(String name) {
        return VariablesBlackboard.getInstance().getVariableValue(name);
    }

    @Override
    public String getVariableGroup(String name) {
        Variable v = VariablesBlackboard.getInstance().getVariable(name);
        if (v != null) {
            return v.getGroup();
        } else {
            return "";
        }
    }

    @Override
    public String getVariableDescription(String name) {
        Variable v = VariablesBlackboard.getInstance().getVariable(name);
        if (v != null) {
            return v.getDescription();
        } else {
            return "";
        }
    }

    @Override
    public String populateTemplate(String text) {
        return VariablesBlackboard.getInstance().populateTemplate(text, false);
    }

    @Override
    public List<String> getVariableNames() {
        if (VariablesBlackboard.getInstance() == null) {
            return new Vector<String>();
        } else {
            return VariablesBlackboard.getInstance().getVariablesList();
        }
    }

    @Override
    public List<String> getVariableGroups() {
        return VariablesBlackboard.getInstance().getGroups();
    }

    @Override
    public void addVariableUpdateListener(VariableUpdateListener notifyChange) {
        VariablesBlackboard.getInstance().addVariablesUpdateListener(notifyChange);
    }

    @Override
    public void removeVariableUpdateListener(VariableUpdateListener notifyChange) {
        VariablesBlackboard.getInstance().removeVariablesUpdateListener(notifyChange);
    }

    @Override
    public boolean isPaused() {
        return VariablesBlackboard.isPaused();
    }

    @Override
    public void populateVariablesCombo(JComboBox varCombo, boolean addEquals) {
        UIUtils.populateVariablesCombo(varCombo, addEquals);
    }

    @Override
    public Vector<String> getVariablesInTemplate(String text) {
        return VariablesBlackboard.getVariablesInTemplate(text);
    }

    @Override
    public long getVariableTimestampMillis(String variableName) {
        Variable v = VariablesBlackboard.getInstance().getVariable(variableName);
        if (v != null) {
            return v.getTimestamp();
        } else {
            return 0;
        }
    }

    @Override
    public int getVariableUpdateCount(String variableName) {
        Variable v = VariablesBlackboard.getInstance().getVariable(variableName);
        if (v != null) {
            return v.getCount();
        } else {
            return 0;
        }
    }
}
