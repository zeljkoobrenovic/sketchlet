package net.sf.sketchlet.model.programming.screenscripts;

import net.sf.sketchlet.common.template.TemplateMarkers;
import net.sf.sketchlet.blackboard.VariablesBlackboard;
import net.sf.sketchlet.designer.editor.ui.page.VariableUpdatePageHandler;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author cuypers
 */
public class Condition {

    private String variable = "variable";
    private String operator = "=";
    private String value = "";

    public boolean conditionSatisfied() {
        String var = getVariable();
        if (TemplateMarkers.containsStartMarker(var)) {
            var = VariablesBlackboard.populateTemplate(var);
        }

        return conditionSatisfied(var, VariablesBlackboard.getInstance().getVariableValue(var));
    }

    public boolean conditionSatisfied(String variable, String value) {
        if (variable.equals(this.getVariable())) {
            try {
                if (getOperator().equals("updated")) {
                    return true;
                } else if (getOperator().equals("equals") || getOperator().equals("=")) {
                    return value.equals(this.getValue());
                } else if (getOperator().equals("<>") || getOperator().equals("!=")) {
                    return !value.equals(this.getValue());
                } else if (getOperator().equals(">")) {
                    double value1 = Double.parseDouble(value);
                    double value2 = Double.parseDouble(this.getValue());

                    return value1 > value2;
                } else if (getOperator().equals(">=")) {
                    double value1 = Double.parseDouble(value);
                    double value2 = Double.parseDouble(this.getValue());

                    return value1 >= value2;
                } else if (getOperator().equals("<")) {
                    double value1 = Double.parseDouble(value);
                    double value2 = Double.parseDouble(this.getValue());

                    return value1 < value2;
                } else if (getOperator().equals("<=")) {
                    double value1 = Double.parseDouble(value);
                    double value2 = Double.parseDouble(this.getValue());

                    return value1 <= value2;

                } else if (getOperator().equalsIgnoreCase("in")) {
                    return VariableUpdatePageHandler.isIn(this.getValue(), value);
                } else if (getOperator().equalsIgnoreCase("not in")) {
                    return !VariableUpdatePageHandler.isIn(this.getValue(), value);
                }
            } catch (Exception e) {
                // e.printStackTrace();
            }

            return false;
        } else {
            return true;
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}
