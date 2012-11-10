/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.playback.displays;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.designer.ui.playback.displays.InteractionSpaceFrame;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class InteractionSpace {

    public static double sketchWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    public static double sketchHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    public static double left = 0.0;
    public static double right = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    public static double top = 0.0;
    public static double bottom = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    public static double angleStart = 0.0;
    public static double angleEnd = 360;
    public static int gridSpacing = 30;
    public static Vector<ScreenMapping> displays = new Vector<ScreenMapping>();

    public static void createDisplaySpace() {
        displays.removeAllElements();
        displays.add(new ScreenMapping());
        displays.add(new ScreenMapping(false));
        displays.add(new ScreenMapping(false));
        displays.add(new ScreenMapping(false));
        displays.add(new ScreenMapping(false));
    }

    public static double getPhysicalX(double sketchX) {
        return sketchX;
        /*
        double value;
        if (left < right) {
        value = left + sketchX * Math.abs(left - right) / SketchletEditor.freeHand.getSketchWidth();
        } else {
        value = left - sketchX * Math.abs(left - right) / SketchletEditor.freeHand.getSketchWidth();
        }


        return value;*/
    }

    public static double wrapPhysicalAngle(double physicalAngle) {
        if (angleStart < angleEnd) {
            while (physicalAngle < angleStart) {
                physicalAngle += Math.abs(angleStart - angleEnd);
            }
            while (physicalAngle > angleEnd) {
                physicalAngle -= Math.abs(angleStart - angleEnd);
            }
        } else {
            while (physicalAngle > angleStart) {
                physicalAngle -= Math.abs(angleStart - angleEnd);
            }
            while (physicalAngle < angleEnd) {
                physicalAngle += Math.abs(angleStart - angleEnd);
            }
        }
        return physicalAngle;
    }

    public static double toRadians(double physicalAngle) {
        double value;
        if (angleStart < angleEnd) {
            while (physicalAngle < angleStart) {
                physicalAngle += Math.abs(angleStart - angleEnd);
            }
            while (physicalAngle > angleEnd) {
                physicalAngle -= Math.abs(angleStart - angleEnd);
            }
            value = 0 + (physicalAngle - angleStart) * Math.PI * 2 / Math.abs(angleStart - angleEnd);
        } else {
            while (physicalAngle > angleStart) {
                physicalAngle -= Math.abs(angleStart - angleEnd);
            }
            while (physicalAngle < angleEnd) {
                physicalAngle += Math.abs(angleStart - angleEnd);
            }
            value = 0 + (angleStart - physicalAngle) * Math.PI * 2 / Math.abs(angleStart - angleEnd);
        }

        return value;
    }

    public static double toPhysicalAngle(double radians) {
        while (radians < 0) {
            radians += Math.PI * 2;
        }
        while (radians > Math.PI * 2) {
            radians -= Math.PI * 2;
        }
        double value;
        if (angleStart < angleEnd) {
            value = angleStart + radians * Math.abs(angleStart - angleEnd) / (Math.PI * 2);
        } else {
            value = angleStart - radians * Math.abs(angleStart - angleEnd) / (Math.PI * 2);
        }

        return value;
    }

    public static double getSketchX(double physicalX) {
        return physicalX;
        /*
        double value;
        if (left < right) {
        value = 0 + (physicalX - left) * SketchletEditor.freeHand.getSketchWidth() / Math.abs(left - right);
        } else {
        value = 0 + (left - physicalX) * SketchletEditor.freeHand.getSketchWidth() / Math.abs(left - right);
        }

        return value;*/
    }

    public static double getSketchWidth(double physicalWidth) {
        return physicalWidth;
        // return physicalWidth * SketchletEditor.freeHand.getSketchWidth() / Math.abs(left - right);
    }

    public static double getSketchHeight(double physicalHeight) {
        return physicalHeight;
        // return physicalHeight * SketchletEditor.freeHand.getSketchHeight() / Math.abs(top - bottom);
    }

    public static double getPhysicalWidth(double width) {
        return width;
        // return width / SketchletEditor.freeHand.getSketchWidth() * Math.abs(left - right);
    }

    public static double getPhysicalHeight(double height) {
        return height;
        // return height / SketchletEditor.freeHand.getSketchHeight() * Math.abs(top - bottom);
    }

    public static double getSketchY(double physicalY) {
        return physicalY;
    }

    public static double getPhysicalY(double sketchY) {
        return sketchY;
    }

    public static void save() {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "interaction_space.xml"));
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

            out.println("<interaction-space>");

            out.println("   <sketch-width>" + sketchWidth + "</sketch-width>\r\n");
            out.println("   <sketch-height>" + sketchHeight + "</sketch-height>\r\n");
            out.println("   <sketch-left>" + left + "</sketch-left>\r\n");
            out.println("   <sketch-top>" + top + "</sketch-top>\r\n");
            out.println("   <sketch-right>" + right + "</sketch-right>\r\n");
            out.println("   <sketch-bottom>" + bottom + "</sketch-bottom>\r\n");
            out.println("   <start-angle>" + angleStart + "</start-angle>\r\n");
            out.println("   <end-angle>" + angleEnd + "</end-angle>\r\n");
            out.println("   <grid-spacing>" + gridSpacing + "</grid-spacing>\r\n");

            for (ScreenMapping dm : displays) {
                dm.save(out);
            }

            out.println("</interaction-space>");

            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        try {
            InteractionSpaceFrame.closeFrame();
            File file = new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "interaction_space.xml");

            if (file.exists()) {
                InteractionSpaceSaxLoader.getScreens(file.getPath(), displays);
                for (int i = 0; i < 5 - displays.size(); i++) {
                    ScreenMapping dm = new ScreenMapping();
                    dm.enable.setSelected(false);
                    displays.add(dm);
                }
            } else {
                sketchWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
                sketchHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
                left = 0.0;
                right = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
                top = 0.0;
                bottom = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
                angleStart = 0.0;
                angleEnd = 360;
                displays = new Vector<ScreenMapping>();

                displays.add(new ScreenMapping());
                displays.add(new ScreenMapping(false));
                displays.add(new ScreenMapping(false));
                displays.add(new ScreenMapping(false));
                displays.add(new ScreenMapping(false));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
