package net.sf.sketchlet.designer.eye.eye;

import net.sf.sketchlet.framework.blackboard.Variable;
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
        this.name = variable.getName();
        this.backgroundColor = Color.RED;
    }

    public String getLongName() {
        return "Variable " + this.name;
    }

    public void addRelatedSlot(EyeSlot relatedSlot) {
        if (relatedSlot instanceof TimerEyeSlot) {
            TimerEyeSlot s = (TimerEyeSlot) relatedSlot;
            for (int i = 0; i < s.timer.getVariables().length; i++) {
                String strVar = (String) s.timer.getVariables()[i][0];
                if (strVar.equalsIgnoreCase(variable.getName())) {
                    addRelationToSlot(relatedSlot,
                            "updates variable '" + this.name + "' from " + s.timer.getVariables()[i][1] + " to " + s.timer.getVariables()[i][2],
                            "updated by timer '" + s.timer.getName() + "' from " + s.timer.getVariables()[i][1] + " to " + s.timer.getVariables()[i][2]);
                    break;
                }
            }
        } else if (relatedSlot instanceof MacroEyeSlot) {
            MacroEyeSlot m = (MacroEyeSlot) relatedSlot;
            for (int i = 0; i < m.macro.getActions().length; i++) {
                String strAction = (String) m.macro.getActions()[i][0];
                String strVar = (String) m.macro.getActions()[i][1];
                if (strAction.equalsIgnoreCase("variable update") && strVar.equalsIgnoreCase(variable.getName())) {
                    addRelationToSlot(relatedSlot,
                            "updates variable '" + this.name + "' to '" + m.macro.getActions()[i][2] + "'",
                            "updated by macro '" + m.name + "' to '" + m.macro.getActions()[i][2] + "'");
                } else if (strAction.equalsIgnoreCase("variable increment") && strVar.equalsIgnoreCase(variable.getName())) {
                    addRelationToSlot(relatedSlot,
                            "increments variable '" + this.name + "' by " + m.macro.getActions()[i][2],
                            "incremented by macro '" + m.name + "' by " + m.macro.getActions()[i][2]);
                } else if (strAction.equalsIgnoreCase("variable append") && strVar.equalsIgnoreCase(variable.getName())) {
                    addRelationToSlot(relatedSlot,
                            "appends variable '" + this.name + "' by " + m.macro.getActions()[i][2],
                            "appended by macro '" + m.name + "' by " + m.macro.getActions()[i][2]);
                }
            }
        }
    }

    public void openItem() {
        SketchletEditor.getInstance().getSketchToolbar().showNavigator(true);
        int row = Workspace.getMainPanel().getSketchletPanel().globalVariablesPanel.variablesTableModel.variableRows.indexOf(variable);
        Workspace.getMainPanel().getSketchletPanel().globalVariablesPanel.table.getSelectionModel().setSelectionInterval(row, row);
        Rectangle rect = Workspace.getMainPanel().getSketchletPanel().globalVariablesPanel.table.getCellRect(row, 0, true);
        Workspace.getMainPanel().getSketchletPanel().globalVariablesPanel.table.scrollRectToVisible(rect);
    }
}
