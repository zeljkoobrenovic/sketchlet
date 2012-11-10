package net.sf.sketchlet.designer.programming.screenscripts;

import net.sf.sketchlet.common.template.TemplateMarkers;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.designer.ui.page.VariableUpdatePageHandler;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author cuypers
 */
public class Condition {

    String variable = "variable";
    String operator = "=";
    String value = "";

    public boolean conditionSatisfied() {
        String var = variable;
        if (TemplateMarkers.containsStartMarker(var)) {
            var = DataServer.populateTemplate(var);
        }

        return conditionSatisfied(var, DataServer.variablesServer.getVariableValue(var));
    }

    public boolean conditionSatisfied(String variable, String value) {
        if (variable.equals(this.variable)) {
            try {
                if (operator.equals("updated")) {
                    return true;
                } else if (operator.equals("equals") || operator.equals("=")) {
                    return value.equals(this.value);
                } else if (operator.equals("<>") || operator.equals("!=")) {
                    return !value.equals(this.value);
                } else if (operator.equals(">")) {
                    double value1 = Double.parseDouble(value);
                    double value2 = Double.parseDouble(this.value);

                    return value1 > value2;
                } else if (operator.equals(">=")) {
                    double value1 = Double.parseDouble(value);
                    double value2 = Double.parseDouble(this.value);

                    return value1 >= value2;
                } else if (operator.equals("<")) {
                    double value1 = Double.parseDouble(value);
                    double value2 = Double.parseDouble(this.value);

                    return value1 < value2;
                } else if (operator.equals("<=")) {
                    double value1 = Double.parseDouble(value);
                    double value2 = Double.parseDouble(this.value);

                    return value1 <= value2;

                } else if (operator.equalsIgnoreCase("in")) {
                    return VariableUpdatePageHandler.isIn(this.value, value);
                } else if (operator.equalsIgnoreCase("not in")) {
                    return !VariableUpdatePageHandler.isIn(this.value, value);
                }
            } catch (Exception e) {
                // e.printStackTrace();
            }

            return false;
        } else {
            return true;
        }
    }
}
