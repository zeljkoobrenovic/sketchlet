/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugin;

/**
 *
 * @author zobrenovic
 */
public interface VariableSpacePlugin {

    public void update(String id, String value);

    public String evaluate(String expressionOrId);

    public void delete(String id);
}
