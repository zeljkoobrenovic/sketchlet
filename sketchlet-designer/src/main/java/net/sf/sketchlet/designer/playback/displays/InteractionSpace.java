package net.sf.sketchlet.designer.playback.displays;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.designer.playback.ui.InteractionSpaceFrame;
import net.sf.sketchlet.loaders.InteractionSpaceSaxLoader;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class InteractionSpace {

    private static double sketchWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    private static double sketchHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    private static double left = 0.0;
    private static double right = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    private static double top = 0.0;
    private static double bottom = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    private static double angleStart = 0.0;
    private static double angleEnd = 360;
    private static int gridSpacing = 30;
    private static Vector<ScreenMapping> displays = new Vector<ScreenMapping>();

    public static void createDisplaySpace() {
        getDisplays().removeAllElements();
        getDisplays().add(new ScreenMapping());
        getDisplays().add(new ScreenMapping(false));
        getDisplays().add(new ScreenMapping(false));
        getDisplays().add(new ScreenMapping(false));
        getDisplays().add(new ScreenMapping(false));
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
        if (getAngleStart() < getAngleEnd()) {
            while (physicalAngle < getAngleStart()) {
                physicalAngle += Math.abs(getAngleStart() - getAngleEnd());
            }
            while (physicalAngle > getAngleEnd()) {
                physicalAngle -= Math.abs(getAngleStart() - getAngleEnd());
            }
        } else {
            while (physicalAngle > getAngleStart()) {
                physicalAngle -= Math.abs(getAngleStart() - getAngleEnd());
            }
            while (physicalAngle < getAngleEnd()) {
                physicalAngle += Math.abs(getAngleStart() - getAngleEnd());
            }
        }
        return physicalAngle;
    }

    public static double toRadians(double physicalAngle) {
        double value;
        if (getAngleStart() < getAngleEnd()) {
            while (physicalAngle < getAngleStart()) {
                physicalAngle += Math.abs(getAngleStart() - getAngleEnd());
            }
            while (physicalAngle > getAngleEnd()) {
                physicalAngle -= Math.abs(getAngleStart() - getAngleEnd());
            }
            value = 0 + (physicalAngle - getAngleStart()) * Math.PI * 2 / Math.abs(getAngleStart() - getAngleEnd());
        } else {
            while (physicalAngle > getAngleStart()) {
                physicalAngle -= Math.abs(getAngleStart() - getAngleEnd());
            }
            while (physicalAngle < getAngleEnd()) {
                physicalAngle += Math.abs(getAngleStart() - getAngleEnd());
            }
            value = 0 + (getAngleStart() - physicalAngle) * Math.PI * 2 / Math.abs(getAngleStart() - getAngleEnd());
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
        if (getAngleStart() < getAngleEnd()) {
            value = getAngleStart() + radians * Math.abs(getAngleStart() - getAngleEnd()) / (Math.PI * 2);
        } else {
            value = getAngleStart() - radians * Math.abs(getAngleStart() - getAngleEnd()) / (Math.PI * 2);
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

            out.println("   <sketch-width>" + getSketchWidth() + "</sketch-width>\r\n");
            out.println("   <sketch-height>" + getSketchHeight() + "</sketch-height>\r\n");
            out.println("   <sketch-left>" + getLeft() + "</sketch-left>\r\n");
            out.println("   <sketch-top>" + getTop() + "</sketch-top>\r\n");
            out.println("   <sketch-right>" + getRight() + "</sketch-right>\r\n");
            out.println("   <sketch-bottom>" + getBottom() + "</sketch-bottom>\r\n");
            out.println("   <start-angle>" + getAngleStart() + "</start-angle>\r\n");
            out.println("   <end-angle>" + getAngleEnd() + "</end-angle>\r\n");
            out.println("   <grid-spacing>" + getGridSpacing() + "</grid-spacing>\r\n");

            for (ScreenMapping dm : getDisplays()) {
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
                InteractionSpaceSaxLoader.getScreens(file.getPath(), getDisplays());
                for (int i = 0; i < 5 - getDisplays().size(); i++) {
                    ScreenMapping dm = new ScreenMapping();
                    dm.enable.setSelected(false);
                    getDisplays().add(dm);
                }
            } else {
                setSketchWidth(Toolkit.getDefaultToolkit().getScreenSize().getWidth());
                setSketchHeight(Toolkit.getDefaultToolkit().getScreenSize().getHeight());
                setLeft(0.0);
                setRight(Toolkit.getDefaultToolkit().getScreenSize().getWidth());
                setTop(0.0);
                setBottom(Toolkit.getDefaultToolkit().getScreenSize().getHeight());
                setAngleStart(0.0);
                setAngleEnd(360);
                setDisplays(new Vector<ScreenMapping>());

                getDisplays().add(new ScreenMapping());
                getDisplays().add(new ScreenMapping(false));
                getDisplays().add(new ScreenMapping(false));
                getDisplays().add(new ScreenMapping(false));
                getDisplays().add(new ScreenMapping(false));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double getSketchWidth() {
        return sketchWidth;
    }

    public static void setSketchWidth(double sketchWidth) {
        InteractionSpace.sketchWidth = sketchWidth;
    }

    public static double getSketchHeight() {
        return sketchHeight;
    }

    public static void setSketchHeight(double sketchHeight) {
        InteractionSpace.sketchHeight = sketchHeight;
    }

    public static double getLeft() {
        return left;
    }

    public static void setLeft(double left) {
        InteractionSpace.left = left;
    }

    public static double getRight() {
        return right;
    }

    public static void setRight(double right) {
        InteractionSpace.right = right;
    }

    public static double getTop() {
        return top;
    }

    public static void setTop(double top) {
        InteractionSpace.top = top;
    }

    public static double getBottom() {
        return bottom;
    }

    public static void setBottom(double bottom) {
        InteractionSpace.bottom = bottom;
    }

    public static double getAngleStart() {
        return angleStart;
    }

    public static void setAngleStart(double angleStart) {
        InteractionSpace.angleStart = angleStart;
    }

    public static double getAngleEnd() {
        return angleEnd;
    }

    public static void setAngleEnd(double angleEnd) {
        InteractionSpace.angleEnd = angleEnd;
    }

    public static int getGridSpacing() {
        return gridSpacing;
    }

    public static void setGridSpacing(int gridSpacing) {
        InteractionSpace.gridSpacing = gridSpacing;
    }

    public static Vector<ScreenMapping> getDisplays() {
        return displays;
    }

    public static void setDisplays(Vector<ScreenMapping> displays) {
        InteractionSpace.displays = displays;
    }
}
