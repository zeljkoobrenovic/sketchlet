/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.context;

import java.util.List;
import javax.swing.JComboBox;

/**
 * A collection of methods to work with the Sketchlet Designer variables. The variables blackboard 
 * contains a list of variables, untyped data objects encoded as strings. 
 * Sketchlet Designer modules use variables to communicate by updating, reading, 
 * or receiving notifications about updates of variables. 
 * 
 * <p>This is an abstract class, whose full implementation is provided by Sketchlet Designer. 
 * To work with this calls, use the <tt>getInstance</tt> method. Usage example:</p>
 * <pre>
 *     VariablesBlackboardContext context = VariablesBlackboardContext.getInstance();
 *     context.updateVariable("test", "123");
 *     System.out.println(context.getVariableValue("test"));
 * </pre>
 * @author Zeljko Obrenovic
 */
public abstract class VariablesBlackboardContext {

    private static VariablesBlackboardContext blackboard;

    /** 
     * Returns the implementation object of this abstract class. This is a singleton method,
     * and all modules will get the same reference.
     * 
     * @return the reference to the implementation object
     */
    public static VariablesBlackboardContext getInstance() {
        return blackboard;
    }

    /** 
     * Sets the implementation object of this abstract class. This is done by Sketchlet Designer.
     * 
     * @param contextImpl the reference to the implementation object
     */
    public static void setInstance(VariablesBlackboardContext contextImpl) {
        blackboard = contextImpl;
    }

    /** 
     * Returns the list with the names of all variables.
     * 
     * @return the list with the names of variables; if there are no variables, it return the list with size 0
     */
    public abstract List<String> getVariableNames();

    /** 
     * Adds a new variable. If the variable with the same name exists, nothing happens. 
     * 
     * @param variableName the name of the variable
     * @param group the group in which variable belongs; this is a descriptive parameter, and it can be empty string
     * @param description the description of the variables to be shown to the user in the variables table; it can be an empty string
     */
    public abstract void addVariable(String variableName, String group, String description);

    /** 
     * Deletes the variable. If variable does not exist, nothing happens. 
     * 
     * @param variableName the name of the variable
     */
    public abstract void deleteVariable(String variableName);

    /** 
     * Deletes several variables, based on the name template. 
     * The name template may contain "*". For example, if you call this method with
     * the "var-*" template, you will delete all variables that have the name that starts with "var-". 
     * With "*-var" you will delete all variables with names that end with "-var".
     * If there are no variables that match the template, nothing happens.
     * 
     * @param namePattern the name pattern of the variables
     */
    public abstract void deleteVariables(String namePattern);

    /** 
     * Updates the variables with the new value. If the variables does not exist,
     * it will be automatically created.
     * 
     * @param variableName the name of the variable
     * @param value the new value of the variable
     */
    public abstract void updateVariable(String variableName, String value);

    /** 
     * Updates the variables with the new value if the current value of the variable is an empty string. 
     * If the variables does not exist, it will be automatically created, and updated to the new value.
     * 
     * @param variableName the name of the variable
     * @param value the new value of the variable
     */
    public abstract void updateVariableIfEmpty(String variableName, String value);

    /** 
     * Updates the variables with the new value if the current value of the variable is different from the new value. 
     * If the variables does not exist, it will be automatically created, and updated to the new value.
     * Use this method if you are updating variables frequently. This may improve performances as each variable 
     * update is propagated all other elements of Sketchlet Designer.
     * 
     * @param variableName the name of the variable
     * @param value the new value of the variable
     */
    public abstract void updateVariableIfDifferent(String variableName, String value);

    /** 
     * Appends the text to the current value of the variable. For example, if the current value of the variable is "abc", 
     * and you call this method with value parameter "123", the new value of the variable will be "abc123".
     * If the variables does not exist, it will be automatically created, and updated to the given value.
     * 
     * @param variableName the name of the variable
     * @param value the text to be appended
     */
    public abstract void appendTextToVariable(String variableName, String value);

    /** 
     * Increments the current value of the variable with the given value. 
     * If the variables does not exist, it will be automatically created, and updated to the new value.
     * If the current value of the variable is not a number, nothing happens.
     * 
     * @param variableName the name of the variable
     * @param value string encoded numeric increment; can be negative
     */
    public abstract void incrementVariable(String variableName, double value);

    /** 
     * Returns the value of the variable
     * 
     * @param variableName the name of the variable
     * @return the current variable value
     */
    public abstract String getVariableValue(String variableName);

    /** 
     * Returns the group of the variable. This is a descriptive property of the variable.
     * 
     * @param variableName the name of the variable
     * @return the variable group; can be an empty string, but not null
     */
    public abstract String getVariableGroup(String variableName);

    /** 
     * Returns the description of the variable. This is a descriptive property of the variable.
     * 
     * @param variableName the name of the variable
     * @return the variable description; can be an empty string, but not null
     */
    public abstract String getVariableDescription(String variableName);

    /** 
     * Returns the number of variable updates since the start of the application.
     * 
     * @param variableName the name of the variable
     * @return the variable update count
     */
    public abstract int getVariableUpdateCount(String variableName);

    /** 
     * The time of the last variable update.
     * 
     * @param variableName the name of the variable
     * @return the time in milliseconds
     * @see java.lang.System#currentTimeMillis()
     */
    public abstract long getVariableTimestampMillis(String variableName);

    /** 
     * Populates the variable template string. A variable template string is the
     * string where. Sketchlet For example, if we give the template "Value is ${val}"
     * the part "${val}" will be replaced with the actual value of the variable with the name val.
     * If the variable does not exist, the "${val}" will be replaced with the empty string.
     * You can also use simple mathematical formulas in your templates. For example: 
     * "${a} + ${b} = ${a+b}"
     * 
     * @param template the variable template
     * @return the template populated with actual variable values
     */
    public abstract String populateTemplate(String template);

    /**
     * Returns the list of variable groups.
     * 
     * @return the list of variable groups; if there are no variables, returns a list with size 0
     */
    public abstract List<String> getVariableGroups();

    /**
     * Adds a variable update listener. With variables update listener, you can 
     * register to receive notifications about variables updates.
     * 
     * @param listener the instance of VariableUpdateListener interface
     * @see VariableUpdateListener
     */
    public abstract void addVariableUpdateListener(VariableUpdateListener listener);

    /**
     * Removes a variable update listener.
     * 
     * @param listener the instance of VariableUpdateListener interface
     * @see VariableUpdateListener
     */
    public abstract void removeVariableUpdateListener(VariableUpdateListener listener);

    /**
     * Returns the pause status of the variables blackboard. The user may disable 
     * variable updates, and with this method you may see if that is true.
     * 
     * @return true if the user has disables updates of variables
     */
    public abstract boolean isPaused();

    /**
     * Populates a combo box with variable names.
     * 
     * @param combo an instance of a combo box to be populated with variable names
     * @param addEqual if true, variable names in the list will have "=" as a prefix
     */
    public abstract void populateVariablesCombo(JComboBox combo, boolean addEqual);

    /**
     * Returns the list with variable names define in a template.
     * 
     * @param template the variable template
     * @return the list with variables in the template; if there are no variables in the template, it return the list with size 0
     */
    public abstract List<String> getVariablesInTemplate(String template);
}
