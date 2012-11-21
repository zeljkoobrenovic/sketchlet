/*
 * Template.java
 *
 * Created on April 21, 2008, 2:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.sketchlet.communicator.server;

/**
 * @author cuypers
 */
public class Template {

    public String template;
    public String variable;
    public String test = null;

    public Template() {
    }

    public Template(String template, String variable, String test) {
        this.template = template;
        this.variable = variable;
        this.test = test;
    }

    public boolean test() {
        // if test is null, this is unconditional
        if (test == null) {
            return true;
        }

        String value = DataServer.getInstance().getVariableValue(this.variable, this.test);
        return !value.trim().equals("");
    }
}
