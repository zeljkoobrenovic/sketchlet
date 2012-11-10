package net.sf.sketchlet.designer.programming.screenscripts;

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

    public static Robot robot;
    public static TextTransfer clipboard = new TextTransfer();

    static {
        try {
            robot = new Robot();
            robot.setAutoDelay(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendKey(int keyCode) {
        robot.keyPress(keyCode);
        robot.keyRelease(keyCode);
    }

    public static void sendKey(int keyCode, int pause) {
        robot.keyPress(keyCode);
        robot.keyRelease(keyCode);
        robot.delay(pause);   // for you to see the keystroke

    }

    public static void pressKey(int keyCode) {
        robot.keyPress(keyCode);
    }

    public static void releaseKey(int keyCode) {
        robot.keyRelease(keyCode);
    }

    public static void sendMouseClick(int x, int y) {
        sendMouseClick(x, y, InputEvent.BUTTON1_MASK);
    }

    public static void sendMouseClick(int x, int y, int button) {
        robot.mouseMove(x, y);
        robot.mousePress(button);
        robot.mouseRelease(button);
    }

    public static void sendMouseClick(int button) {
        robot.mousePress(button);
        robot.mouseRelease(button);
    }

    public static void moveMouse(int x, int y) {
        robot.mouseMove(x, y);
    }

    public static void pressMouse(int button) {
        robot.mousePress(button);
    }

    public static void releaseMouse(int button) {
        robot.mouseRelease(button);
    }

    public static void pause(int pauseInMs) {
        robot.delay(pauseInMs);
    }

    public static void pasteToApp(String sendString) {
        clipboard.setClipboardContents(sendString);
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
}

