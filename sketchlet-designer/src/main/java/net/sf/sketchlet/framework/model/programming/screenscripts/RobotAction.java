package net.sf.sketchlet.framework.model.programming.screenscripts;

import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.playback.ui.PlaybackFrame;
import net.sf.sketchlet.framework.blackboard.evaluator.Evaluator;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.StringTokenizer;

/**
 *
 * @author cuypers
 */
public abstract class RobotAction {

    public static final String MOUSE_MOVE_ACTION = "Move Mouse Cursor";
    public static final String CLICK_LEFT_MOUSE_BUTTON_ACTION = "Click Left Mouse Button";
    public static final String CLICK_RIGHT_MOUSE_BUTTON_ACTION = "Click Right Mouse Button";
    public static final String CLICK_MIDDLE_MOUSE_BUTTON_ACTION = "Click Middle Mouse Button";
    public static final String DOUBLE_CLICK_MOUSE_ACTION = "Double Click Left Mouse Button";
    public static final String PRESS_LEFT_MOUSE_BUTTON_ACTION = "Press Left Mouse Button";
    public static final String PRESS_RIGHT_MOUSE_BUTTON_ACTION = "Press Right Mouse Button";
    public static final String PRESS_MIDDLE_MOUSE_BUTTON_ACTION = "Press Middle Mouse Button";
    public static final String RELEASE_LEFT_MOUSE_BUTTON_ACTION = "Release Left Mouse Button";
    public static final String RELEASE_RIGHT_MOUSE_BUTTON_ACTION = "Release Right Mouse Button";
    public static final String RELEASE_MIDDLE_MOUSE_BUTTON_ACTION = "Release Middle Mouse Button";
    public static final String RESTORE_MOUSE_POSITION = "Restore Mouse Original Position";
    public static final String PAUSE_ACTION = "Pause Action";
    public static final String PASTE_TEXT_ACTION = "Paste Text";
    public static final String KEYS_ACTION = "Type Key(s)";
    public static final String CAPTURE_SCREEN_ACTION = "Capture a part of the screen";
    public static final String UPDATE_VARIABLE_ACTION = "Update Variable";

    private String name = "";
    private String parameters = "";
    private String description = "";

    public RobotAction() {
    }

    public RobotAction(String name, String initParams, String description) {
        this.setName(name);
        this.setParameters(initParams);
        this.setDescription(description);
    }

    public void doAction() {
        doAction(getParameters());
    }

    public abstract void doAction(String params);

    public void paint(Graphics g, int index, String text) {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

class MouseMoveAction extends RobotAction {

    int x = 0;
    int y = 0;

    public MouseMoveAction(String initParams) {
        setName(RobotAction.MOUSE_MOVE_ACTION);
        setParameters(initParams);
        setDescription("Move mouse cursor to given coordinates");
    }

    public void getXY(String parameters) {
        StringTokenizer t = new StringTokenizer(parameters, " ;,");

        try {
            if (t.countTokens() == 2) {
                x = Integer.parseInt(t.nextToken());
                y = Integer.parseInt(t.nextToken());
            }
        } catch (Exception e) {
        }

    }

    public void doAction(String strParams) {
        this.getXY(strParams);
        AWTRobotUtil.moveMouse(x, y);
    }

    public void paint(Graphics g, int index, String text) {
        getXY(getParameters());
        g.setColor(Color.RED);
        g.drawOval(x - 1, y - 1, 3, 3);
        g.drawString((index + 1) + ". " + text, x, y);
    }
}

class MouseRestoreAction extends RobotAction {

    private static int x;
    private static int y;

    public MouseRestoreAction() {
        setName(RobotAction.RESTORE_MOUSE_POSITION);
        setParameters("");
        setDescription("Restore original mouse position");
    }

    public static int getX() {
        return x;
    }

    public static void setX(int x) {
        MouseRestoreAction.x = x;
    }

    public static int getY() {
        return y;
    }

    public static void setY(int y) {
        MouseRestoreAction.y = y;
    }

    public void doAction(String strParams) {
        if (PlaybackFrame.playbackFrame != null) {
            for (int i = 0; i < PlaybackFrame.playbackFrame.length; i++) {
                if (PlaybackFrame.playbackFrame[i] != null) {
                    PlaybackFrame.playbackFrame[i].toFront();
                }
            }
        } else {
            SketchletEditor.editorFrame.toFront();
        }
        AWTRobotUtil.moveMouse(getX(), getY());
    }

    public void paint(Graphics g, int index, String text) {
    }
}

class MouseButtonClickAction extends RobotAction {

    private int button;

    public MouseButtonClickAction(String name, int button, String initParams) {
        this.setName(name);
        this.button = button;
        setParameters(initParams);
        setDescription("Clicks mouse button (optionally move mouse cursor to given coordinates)");
    }

    public void doAction(String strParams) {
        if (!strParams.trim().equals("")) {
            new MouseMoveAction(strParams).doAction();
        }

        AWTRobotUtil.sendMouseClick(this.button);
    }

    public void paint(Graphics g, int index, String text) {
        if (!getParameters().trim().equals("")) {
            new MouseMoveAction(getParameters()).paint(g, index, text);
        }
    }
}

class MouseLeftButtonClickAction extends MouseButtonClickAction {

    public MouseLeftButtonClickAction(String initParams) {
        super(RobotAction.CLICK_LEFT_MOUSE_BUTTON_ACTION, InputEvent.BUTTON1_MASK, initParams);
    }
}

class MouseMiddleButtonClickAction extends MouseButtonClickAction {

    public MouseMiddleButtonClickAction(String initParams) {
        super(RobotAction.CLICK_MIDDLE_MOUSE_BUTTON_ACTION, InputEvent.BUTTON2_MASK, initParams);
    }
}

class MouseRightButtonClickAction extends MouseButtonClickAction {

    public MouseRightButtonClickAction(String initParams) {
        super(RobotAction.CLICK_RIGHT_MOUSE_BUTTON_ACTION, InputEvent.BUTTON3_MASK, initParams);
    }
}

class MouseButtonPressAction extends RobotAction {

    int button;

    public MouseButtonPressAction(String name, int button, String initParams) {
        this.setName(name);
        this.button = button;
        setParameters(initParams);
        setDescription("Presses mouse button (optionally move mouse cursor to given coordinates)");
    }

    public void doAction(String strParams) {
        if (!strParams.trim().equals("")) {
            new MouseMoveAction(strParams).doAction();
        }

        AWTRobotUtil.pressMouse(this.button);
    }

    public void paint(Graphics g, int index, String text) {
        if (!getParameters().trim().equals("")) {
            new MouseMoveAction(getParameters()).paint(g, index, text);
        }
    }
}

class MouseLeftButtonPressAction extends MouseButtonPressAction {

    public MouseLeftButtonPressAction(String initParams) {
        super(RobotAction.PRESS_LEFT_MOUSE_BUTTON_ACTION, InputEvent.BUTTON1_MASK, initParams);
    }
}

class MouseMiddleButtonPressAction extends MouseButtonPressAction {

    public MouseMiddleButtonPressAction(String initParams) {
        super(RobotAction.PRESS_MIDDLE_MOUSE_BUTTON_ACTION, InputEvent.BUTTON2_MASK, initParams);
    }
}

class MouseRightButtonPressAction extends MouseButtonPressAction {

    public MouseRightButtonPressAction(String initParams) {
        super(RobotAction.PRESS_RIGHT_MOUSE_BUTTON_ACTION, InputEvent.BUTTON3_MASK, initParams);
    }
}

class MouseButtonReleaseAction extends RobotAction {

    int button;

    public MouseButtonReleaseAction(String name, int button, String initParams) {
        this.setName(name);
        this.button = button;
        setParameters(initParams);
        setDescription("Releases mouse button (optionally move mouse cursor to given coordinates)");
    }

    public void doAction(String strParams) {
        if (!strParams.trim().equals("")) {
            new MouseMoveAction(strParams).doAction();
        }

        AWTRobotUtil.releaseMouse(this.button);
    }

    public void paint(Graphics g, int index, String text) {
        if (!getParameters().trim().equals("")) {
            new MouseMoveAction(getParameters()).paint(g, index, text);
        }
    }
}

class MouseLeftButtonReleaseAction extends MouseButtonReleaseAction {

    public MouseLeftButtonReleaseAction(String initParams) {
        super(RobotAction.RELEASE_LEFT_MOUSE_BUTTON_ACTION, InputEvent.BUTTON1_MASK, initParams);
    }
}

class MouseMiddleButtonReleaseAction extends MouseButtonReleaseAction {

    public MouseMiddleButtonReleaseAction(String initParams) {
        super(RobotAction.RELEASE_MIDDLE_MOUSE_BUTTON_ACTION, InputEvent.BUTTON2_MASK, initParams);
    }
}

class MouseRightButtonReleaseAction extends MouseButtonReleaseAction {

    public MouseRightButtonReleaseAction(String initParams) {
        super(RobotAction.RELEASE_RIGHT_MOUSE_BUTTON_ACTION, InputEvent.BUTTON3_MASK, initParams);
    }
}

class PauseAction extends RobotAction {

    public PauseAction(String initParams) {
        super(RobotAction.PAUSE_ACTION, initParams, "Pause for given time (in ms)");
    }

    public void doAction(String strParams) {
        int pauseInMs = 0;
        try {
            pauseInMs = Integer.parseInt(strParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
        AWTRobotUtil.pause(pauseInMs);
    }
}

class PasteTextAction extends RobotAction {

    public PasteTextAction(String initParams) {
        super(RobotAction.PASTE_TEXT_ACTION, initParams, "Paste given text");
    }

    public void doAction(String strParams) {
        AWTRobotUtil.pasteToApp(strParams);
    }
}

class TypeKeysAction extends RobotAction {

    public TypeKeysAction(String initParams) {
        super(RobotAction.KEYS_ACTION, initParams, "Types keys");
    }

    public void doAction(String strParams) {
        AWTRobotUtil.sendKeys(strParams);
    }
}

class CaptureScreenAction extends RobotAction {

    public CaptureScreenAction(String initParams) {
        super(RobotAction.CAPTURE_SCREEN_ACTION, initParams, "Capture screen");
    }

    public void doAction(String strParams) {
        try {
            int areaX = 0;
            int areaY = 0;
            int areaWidth = 800;
            int areaHeight = 600;

            String strVar = "image-path";

            String params[] = strParams.split(" ");

            try {
                strVar = params[0];
                areaX = Integer.parseInt(params[1]);
                areaY = Integer.parseInt(params[2]);
                areaWidth = Integer.parseInt(params[3]);
                areaHeight = Integer.parseInt(params[4]);
            } catch (Exception e) {
            }

            Rectangle rectScreenSize = new Rectangle(areaX, areaY, areaWidth, areaHeight);
            BufferedImage image = AWTRobotUtil.getRobot().createScreenCapture(rectScreenSize);
            File file = File.createTempFile("capture_image_temp", ".png");
            ImageIO.write(image, "PNG", file);
            VariablesBlackboard.getInstance().updateVariable(strVar, file.getAbsolutePath());
            file.deleteOnExit();
        } catch (Exception e) {
        }
    }
}

class UpdateVariableAction extends RobotAction {

    public UpdateVariableAction(String initParams) {
        super(RobotAction.UPDATE_VARIABLE_ACTION, initParams, "Updates an Sketchlet variable (variable=value)");
    }

    public void doAction(String strParams) {
        int n = strParams.indexOf("=");
        if (n == -1) {
            n = strParams.indexOf(" ");
        }
        if (n > 0 && n < strParams.length() - 1) {
            String variable = Evaluator.processText(strParams.substring(0, n).trim(), "", "");
            String value = Evaluator.processText(strParams.substring(n + 1), "", "");
            VariablesBlackboard.getInstance().updateVariable(variable, value);
        }
    }
}
