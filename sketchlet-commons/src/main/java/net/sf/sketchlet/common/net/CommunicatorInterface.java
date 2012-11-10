/*
 * CommunicatorInterface.java
 *
 * Created on October 22, 2006, 3:37 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.sketchlet.common.net;

/**
 *
 * @author cuypers
 */
public interface CommunicatorInterface {
    void addTemplate(String triggerVariable);
    void addTemplate(String triggerVariable, String template);
    void addTemplate(String[] triggerVariables, String template);
    void register(String triggerVariable);
    void register(String triggerVariable, String template);
    void register(String[] triggerVariables, String template);

    String deleteVariable(String variableName);

    String loadTransformation(String transformationURL);

    String readLine();

    String removeTransformation(String transformationURL);

    void send(String strCommand);

    String sendAndReceive(String strCommand);

    String addVariable(String variableName, String strGroup, String strDescription);
    String updateVariable(String variableName, String variableValue);    
    String updateVariable(String variableName, String variableValue, String strGroup, String strDescription);
    
}
