/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.model.programming.screenscripts;

import net.sf.sketchlet.designer.playback.ui.PlaybackPanel;
import net.sf.sketchlet.blackboard.evaluator.Evaluator;

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
        MouseRestoreAction.setX(PlaybackPanel.mouseScreenX);
        MouseRestoreAction.setY(PlaybackPanel.mouseScreenY);
        for (RobotAction a : getActions()) {
            if (getParent().isStopped()) {
                return;
            }

            String strParams = a.parameters;

            strParams = Evaluator.processText(strParams, "", "");

            a.doAction(strParams);
        }
        try {
            // Thread.sleep(200);
            /*AWTRobotUtil.releaseMouse(InputEvent.BUTTON1_MASK);
            AWTRobotUtil.releaseMouse(InputEvent.BUTTON2_MASK);
            AWTRobotUtil.releaseMouse(InputEvent.BUTTON3_MASK);
            AWTRobotUtil.moveMouse(PlaybackPanel.mouseScreenX, PlaybackPanel.mouseScreenY);*/
        } catch (Exception e) {
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
