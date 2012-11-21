package net.sf.sketchlet.model.programming.screenscripts;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorobot.
 */

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * @author cuypers
 */
public class AWTRobotUtil {

    private static Robot robot;
    private static TextTransfer clipboard = new TextTransfer();

    static {
        try {
            setRobot(new Robot());
            getRobot().setAutoDelay(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendKey(int keyCode) {
        getRobot().keyPress(keyCode);
        getRobot().keyRelease(keyCode);
    }

    public static void sendKey(int keyCode, int pause) {
        getRobot().keyPress(keyCode);
        getRobot().keyRelease(keyCode);
        getRobot().delay(pause);   // for you to see the keystroke

    }

    public static void pressKey(int keyCode) {
        getRobot().keyPress(keyCode);
    }

    public static void releaseKey(int keyCode) {
        getRobot().keyRelease(keyCode);
    }

    public static void sendMouseClick(int x, int y) {
        sendMouseClick(x, y, InputEvent.BUTTON1_MASK);
    }

    public static void sendMouseClick(int x, int y, int button) {
        getRobot().mouseMove(x, y);
        getRobot().mousePress(button);
        getRobot().mouseRelease(button);
    }

    public static void sendMouseClick(int button) {
        getRobot().mousePress(button);
        getRobot().mouseRelease(button);
    }

    public static void moveMouse(int x, int y) {
        getRobot().mouseMove(x, y);
    }

    public static void pressMouse(int button) {
        getRobot().mousePress(button);
    }

    public static void releaseMouse(int button) {
        getRobot().mouseRelease(button);
    }

    public static void pause(int pauseInMs) {
        getRobot().delay(pauseInMs);
    }

    public static void pasteToApp(String sendString) {
        getClipboard().setClipboardContents(sendString);
        pressKey(KeyEvent.VK_CONTROL);
        sendKey(KeyEvent.VK_V);
        releaseKey(KeyEvent.VK_CONTROL);
    }

    public static void sendKeys(String sendKeys) {
        String keys[] = sendKeys.replace("+", "").split(" ");
        try {
            for (int i = 0; i < keys.length; i++) {
                int key = Integer.parseInt(keys[i]);

                if (key > 0) {
                    pressKey(key);
                } else {
                    releaseKey(Math.abs(key));
                }
            }
        } catch (Exception e) {
        }
    }

    public static Robot getRobot() {
        return robot;
    }

    public static void setRobot(Robot robot) {
        AWTRobotUtil.robot = robot;
    }

    public static TextTransfer getClipboard() {
        return clipboard;
    }

    public static void setClipboard(TextTransfer clipboard) {
        AWTRobotUtil.clipboard = clipboard;
    }
}

