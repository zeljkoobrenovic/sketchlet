/*
 * NotifyChange.java
 *
 * Created on 24 February 2006, 13:21
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package net.sf.sketchlet.blackboard;

/**
 * @author Omnibook
 */
public interface VariableOperationsListener {
    public void variableAdded(String triggerVariable, String value);

    public void variableUpdated(String triggerVariable, String value);

    public void variableDeleted(String triggerVariable);
}
