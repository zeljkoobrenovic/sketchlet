/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.programming.screenscripts;

import net.sf.sketchlet.designer.data.Evaluator;
import net.sf.sketchlet.designer.ui.playback.PlaybackPanel;

import java.util.Vector;

public class ScreenScript {

    public ScreenScriptInfo info = new ScreenScriptInfo();
    Vector<Condition> conditions = new Vector<Condition>();
    Vector<RobotAction> actions = new Vector<RobotAction>();
    ScreenScriptRunner parent;

    public ScreenScript(ScreenScriptRunner parent) {
        this.parent = parent;
    }

    public void run() {
        MouseRestoreAction.x = PlaybackPanel.mouseScreenX;
        MouseRestoreAction.y = PlaybackPanel.mouseScreenY;
        /*        try {
        AWTRobotUtil.releaseMouse(InputEvent.BUTTON1_MASK);
        AWTRobotUtil.releaseMouse(InputEvent.BUTTON2_MASK);
        AWTRobotUtil.releaseMouse(InputEvent.BUTTON3_MASK);
        } catch (Exception e) {
        }*/
        for (RobotAction a : actions) {
            if (parent.stopped) {
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
}
