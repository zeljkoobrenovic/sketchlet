package net.sf.sketchlet.framework.model.events.variable;

import net.sf.sketchlet.framework.model.events.EventMacro;

/**
 * @author zeljko
 */
public class VariableUpdateEventMacro extends EventMacro {
    public static final String PARAMETER_VARIABLE = "variable";
    public static final String PARAMETER_OPERATOR = "operator";
    public static final String PARAMETER_VALUE = "value";

    public VariableUpdateEventMacro(String eventName) {
        super(eventName);
        parameters().put(PARAMETER_VARIABLE, "");
        parameters().put(PARAMETER_OPERATOR, "");
        parameters().put(PARAMETER_VALUE, "");
    }

    public VariableUpdateEventMacro(EventMacro eventMacro) {
        super(eventMacro);
    }

    public String getVariable() {
        return parameters().get(PARAMETER_VARIABLE);
    }

    public void setVariable(String variable) {
        parameters().put(PARAMETER_VARIABLE, variable);
    }

    public String getOperator() {
        return parameters().get(PARAMETER_OPERATOR);
    }

    public void setOperator(String operator) {
        parameters().put(PARAMETER_OPERATOR, operator);
    }

    public String getValue() {
        return parameters().get(PARAMETER_VALUE);
    }

    public void setValue(String value) {
        parameters().put(PARAMETER_VALUE, value);
    }
}
