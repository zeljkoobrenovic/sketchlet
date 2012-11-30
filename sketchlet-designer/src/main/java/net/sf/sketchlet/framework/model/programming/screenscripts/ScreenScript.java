package net.sf.sketchlet.framework.model.programming.screenscripts;

import net.sf.sketchlet.framework.blackboard.evaluator.Evaluator;
import net.sf.sketchlet.framework.controller.MouseController;

import java.util.Vector;

public class ScreenScript {

    private ScreenScriptInfo info = new ScreenScriptInfo();
    private Vector<Condition> conditions = new Vector<Condition>();
    private Vector<RobotAction> actions = new Vector<RobotAction>();
    private ScreenScriptRunner parent;

    public ScreenScript(ScreenScriptRunner parent) {
        this.setParent(parent);
    }

    public void run() {
        MouseRestoreAction.setX(MouseController.getMouseScreenX());
        MouseRestoreAction.setY(MouseController.getMouseScreenY());
        for (RobotAction a : getActions()) {
            if (getParent().isStopped()) {
                return;
            }

            String strParams = a.getParameters();

            strParams = Evaluator.processText(strParams, "", "");

            a.doAction(strParams);
        }
    }

    public ScreenScriptInfo getInfo() {
        return info;
    }

    public void setInfo(ScreenScriptInfo info) {
        this.info = info;
    }

    public Vector<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(Vector<Condition> conditions) {
        this.conditions = conditions;
    }

    public Vector<RobotAction> getActions() {
        return actions;
    }

    public void setActions(Vector<RobotAction> actions) {
        this.actions = actions;
    }

    public ScreenScriptRunner getParent() {
        return parent;
    }

    public void setParent(ScreenScriptRunner parent) {
        this.parent = parent;
    }
}
