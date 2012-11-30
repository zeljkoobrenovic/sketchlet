/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.framework.blackboard.evaluator;

import net.sf.sketchlet.context.VariablesBlackboardContext;

/**
 * @author zobrenovic
 */
public class CellReferenceResolver {

    public String getValue(String strReference) {
        return VariablesBlackboardContext.getInstance().getVariableValue(strReference);
    }
}
