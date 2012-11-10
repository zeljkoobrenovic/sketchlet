package net.sf.sketchlet.designer.data;

/**
 * Created with IntelliJ IDEA.
 * User: zeljko
 * Date: 26-10-12
 * Time: 21:01
 * To change this template use File | Settings | File Templates.
 */
public class VariableUpdateEventMacro extends EventMacro {
    public static final String PARAMETER_VARIABLE = "variable";
    public static final String PARAMETER_OPERATOR = "operator";
    public static final String PARAMETER_VALUE = "value";

    private String variable, operator, value;

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
