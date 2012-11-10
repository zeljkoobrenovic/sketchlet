package net.sf.sketchlet.designer.programming.screenscripts;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author zobrenovic
 */
public class ActionFactory {

    public static RobotAction createAction(String name) {
        if (name.equalsIgnoreCase(RobotAction.CLICK_LEFT_MOUSE_BUTTON_ACTION)) {
            return new MouseLeftButtonClickAction("");
        } else if (name.equalsIgnoreCase(RobotAction.CLICK_MIDDLE_MOUSE_BUTTON_ACTION)) {
            return new MouseMiddleButtonClickAction("");
        } else if (name.equalsIgnoreCase(RobotAction.CLICK_RIGHT_MOUSE_BUTTON_ACTION)) {
            return new MouseRightButtonClickAction("");
        } else if (name.equalsIgnoreCase(RobotAction.DOUBLE_CLICK_MOUSE_ACTION)) {
        } else if (name.equalsIgnoreCase(RobotAction.KEYS_ACTION)) {
            return new TypeKeysAction("");
        } else if (name.equalsIgnoreCase(RobotAction.CAPTURE_SCREEN_ACTION)) {
            return new CaptureScreenAction("");
        } else if (name.equalsIgnoreCase(RobotAction.UPDATE_VARIABLE_ACTION)) {
            return new UpdateVariableAction("");
        } else if (name.equalsIgnoreCase(RobotAction.MOUSE_MOVE_ACTION)) {
            return new MouseMoveAction("");
        } else if (name.equalsIgnoreCase(RobotAction.PASTE_TEXT_ACTION)) {
            return new PasteTextAction("");
        } else if (name.equalsIgnoreCase(RobotAction.PAUSE_ACTION)) {
            return new PauseAction("");
        } else if (name.equalsIgnoreCase(RobotAction.PRESS_LEFT_MOUSE_BUTTON_ACTION)) {
            return new MouseLeftButtonPressAction("");
        } else if (name.equalsIgnoreCase(RobotAction.RESTORE_MOUSE_POSITION)) {
            return new MouseRestoreAction();
        } else if (name.equalsIgnoreCase(RobotAction.PRESS_MIDDLE_MOUSE_BUTTON_ACTION)) {
            return new MouseMiddleButtonPressAction("");
        } else if (name.equalsIgnoreCase(RobotAction.PRESS_RIGHT_MOUSE_BUTTON_ACTION)) {
            return new MouseRightButtonPressAction("");
        } else if (name.equalsIgnoreCase(RobotAction.RELEASE_LEFT_MOUSE_BUTTON_ACTION)) {
            return new MouseLeftButtonReleaseAction("");
        } else if (name.equalsIgnoreCase(RobotAction.RELEASE_MIDDLE_MOUSE_BUTTON_ACTION)) {
            return new MouseMiddleButtonReleaseAction("");
        } else if (name.equalsIgnoreCase(RobotAction.RELEASE_RIGHT_MOUSE_BUTTON_ACTION)) {
            return new MouseRightButtonReleaseAction("");
        }

        return null;
    }
}
