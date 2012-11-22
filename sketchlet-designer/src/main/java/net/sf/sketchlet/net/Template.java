/*
 * Template.java
 *
 * Created on April 21, 2008, 2:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.sketchlet.net;

import net.sf.sketchlet.blackboard.VariablesBlackboard;

/**
 * @author cuypers
 */
public class Template {

    private String template;
    private String variable;
    private String test = null;

    public Template() {
    }

    public Template(String template, String variable, String test) {
        this.setTemplate(template);
        this.setVariable(variable);
        this.setTest(test);
    }

    public boolean test() {
        if (getTest() == null) {
            return true;
        }

        String value = VariablesBlackboard.getInstance().getVariableValue(this.getVariable(), this.getTest());
        return !value.trim().equals("");
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }
}
