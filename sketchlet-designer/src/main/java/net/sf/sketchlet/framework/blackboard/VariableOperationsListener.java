package net.sf.sketchlet.framework.blackboard;

/**
 * @author Omnibook
 */
public interface VariableOperationsListener {
    public void variableAdded(String triggerVariable, String value);

    public void variableUpdated(String triggerVariable, String value);

    public void variableDeleted(String triggerVariable);
}
