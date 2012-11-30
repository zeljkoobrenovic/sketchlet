/*
 * VariableUpdateListener.java
 *
 * Created on 24 February 2006, 13:21
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package net.sf.sketchlet.context;

/**
 * The listener interface for receiving variable update events. 
 * 
 * The class that is interested in processing variables update events, 
 * should implement this interface, and register it using the <tt>VariablesBlackboardContext#addVariableUpdateListener()</tt> method.
 * An example:
 * <pre>
 * VariablesBlackboardContext context = VariablesBlackboardContext.getInstance();
 * context.addVariableUpdateListener(new VariableUpdateListener() {
 *     public void variableUpdated(String name, String value) {
 *         System.out.prinltn("Variable '" + name "' is updated with the value '" + value +"'" );
 *     }
 * });
 * </pre>
 * 
 * @author Zeljko Obrenovic
 */
public interface VariableUpdateListener {

    /**
     * This method is called by the variables blackboard each time when any variable is updated.
     *      
     * @param name the name of the variable
     * @param value the value of the variable
     */
    public void variableUpdated(String name, String value);
}
