/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.eye.eye;

import net.sf.sketchlet.communicator.server.Variable;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;

import java.awt.*;

/**
 * @author zobrenovic
 */
public class VariableEyeSlot extends EyeSlot {

    Variable variable;

    public VariableEyeSlot(Variable variable, EyeData parent) {
        super(parent);
        this.variable = variable;
        this.name = variable.name;
        this.backgroundColor = Color.RED;
    }

    public String getLongName() {
        return "Variable " + this.name;
    }

    public void addRelatedSlot(EyeSlot relatedSlot) {
        if (relatedSlot instanceof TimerEyeSlot) {
            TimerEyeSlot s = (TimerEyeSlot) relatedSlot;
            for (int i = 0; i < s.timer.variables.length; i++) {
                String strVar = (String) s.timer.variables[i][0];
                if (strVar.equalsIgnoreCase(variable.name)) {
                    addRelationToSlot(relatedSlot,
                            "updates variable '" + this.name + "' from " + s.timer.variables[i][1] + " to " + s.timer.variables[i][2],
                            "updated by timer '" + s.timer.name + "' from " + s.timer.variables[i][1] + " to " + s.timer.variables[i][2]);
                    break;
                }
            }
        } else if (relatedSlot instanceof MacroEyeSlot) {
            MacroEyeSlot m = (MacroEyeSlot) relatedSlot;
            for (int i = 0; i < m.macro.actions.length; i++) {
                String strAction = (String) m.macro.actions[i][0];
                String strVar = (String) m.macro.actions[i][1];
                if (strAction.equalsIgnoreCase("variable update") && strVar.equalsIgnoreCase(variable.name)) {
                    addRelationToSlot(relatedSlot,
                            "updates variable '" + this.name + "' to '" + m.macro.actions[i][2] + "'",
                            "updated by macro '" + m.name + "' to '" + m.macro.actions[i][2] + "'");
                } else if (strAction.equalsIgnoreCase("variable increment") && strVar.equalsIgnoreCase(variable.name)) {
                    addRelationToSlot(relatedSlot,
                            "increments variable '" + this.name + "' by " + m.macro.actions[i][2],
                            "incremented by macro '" + m.name + "' by " + m.macro.actions[i][2]);
                } else if (strAction.equalsIgnoreCase("variable append") && strVar.equalsIgnoreCase(variable.name)) {
                    addRelationToSlot(relatedSlot,
                            "appends variable '" + this.name + "' by " + m.macro.actions[i][2],
                            "appended by macro '" + m.name + "' by " + m.macro.actions[i][2]);
                }
            }
        }
    }

    public void openItem() {
        SketchletEditor.editorPanel.sketchToolbar.showNavigator(true);
        int row = Workspace.mainPanel.sketchletPanel.globalVariablesPanel.variablesTableModel.variableRows.indexOf(variable);
        Workspace.mainPanel.sketchletPanel.globalVariablesPanel.table.getSelectionModel().setSelectionInterval(row, row);
        Rectangle rect = Workspace.mainPanel.sketchletPanel.globalVariablesPanel.table.getCellRect(row, 0, true);
        Workspace.mainPanel.sketchletPanel.globalVariablesPanel.table.scrollRectToVisible(rect);
    }
}
