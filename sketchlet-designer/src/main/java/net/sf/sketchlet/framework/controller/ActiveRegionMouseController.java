/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.framework.controller;

import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.common.filter.PerspectiveFilter;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.animation.AnimationTimerDialog;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.timers.curve.CurvesFrame;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.designer.playback.ui.PlaybackFrame;
import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.model.TrajectoryPoint;
import net.sf.sketchlet.framework.model.events.mouse.MouseProcessor;
import net.sf.sketchlet.framework.model.programming.screenscripts.AWTRobotUtil;
import net.sf.sketchlet.framework.model.programming.timers.Timer;
import net.sf.sketchlet.framework.model.programming.timers.curves.Curve;
import net.sf.sketchlet.framework.model.programming.timers.curves.Curves;
import net.sf.sketchlet.framework.renderer.regions.ActiveRegionRenderer;
import net.sf.sketchlet.util.SpringUtilities;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class ActiveRegionMouseController {
    private static final Logger log = Logger.getLogger(ActiveRegionMouseController.class);
    public static final int MIDDLE = -1;
    public static final int UPPER_LEFT = 0;
    public static final int BOTTOM_LEFT = 1;
    public static final int UPPER_RIGHT = 2;
    public static final int BOTTOM_RIGHT = 3;
    public static final int LEFT = 4;
    public static final int BOTTOM = 5;
    public static final int RIGHT = 6;
    public static final int UPPER = 7;
    public static final int PERSPECTIVE_UPPER_LEFT = 8;
    public static final int PERSPECTIVE_BOTTOM_LEFT = 9;
    public static final int PERSPECTIVE_UPPER_RIGHT = 10;
    public static final int PERSPECTIVE_BOTTOM_RIGHT = 11;
    public static final int ROTATE = 12;
    public static final int CENTER_ROTATION = 13;
    public static final int TRAJECTORY2_POINT = 14;

    private ActiveRegion region;
    private int selectedCorner = MIDDLE;
    private int startX;
    private int startY;
    private int pressedX;
    private int pressedY;
    private double startAngle = 0.0;
    private long startTime;
    private int startMouseXCenter = 0;
    private int startMouseYCenter = 0;
    private int startMouseXCenter2 = 0;
    private int startMouseYCenter2 = 0;
    private double centerRotationX = 0;
    private double centerRotationY = 0;
    private double trajectory2X = 0;
    private double trajectory2Y = 0;
    private double aspectRatio = 1.0;

    public ActiveRegionMouseController(ActiveRegion region) {
        this.region = region;
    }

    public void dispose() {
        region = null;
    }

    public void mouseMoved(MouseEvent e, double scale, JFrame frame, boolean bPlayback) {
        if (!region.parent.getPage().isRegionsLayer()) {
            return;
        }
        int marginX = bPlayback ? region.parent.getOffsetX() : SketchletEditor.getInstance().getMarginX();
        int marginY = bPlayback ? region.parent.getOffsetY() : SketchletEditor.getInstance().getMarginY();
        int x = (int) ((e.getPoint().x) / scale) - marginX;
        int y = (int) ((e.getPoint().y) / scale) - marginY;
        mouseMoved(x, y, scale, e, frame, bPlayback);
    }

    public void mouseMoved(int x, int y, double scale, MouseEvent e, JFrame frame, boolean bPlayback) {
        if (!region.parent.getPage().isRegionsLayer()) {
            return;
        }
        Point ip = inversePoint(x, y, bPlayback);
        x = ip.x;
        y = ip.y;
        if (!bPlayback) {
            int w = region.x2 - region.x1;
            int h = region.y2 - region.y1;
            int cx = region.x1 + w / 2;
            int cy = region.y1 + h / 2;

            Cursor cursor;

            int cs2 = (int) (ActiveRegionRenderer.CORNER_SIZE / SketchletEditor.getInstance().getScale() / 2) + 1;

            if (x >= region.x1 - cs2 && x <= region.x1 + cs2 && y >= region.y1 - cs2 && y <= region.y1 + cs2) {
                cursor = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
            } else if (x >= region.x2 - cs2 && x <= region.x2 + cs2 && y >= region.y1 - cs2 && y <= region.y1 + cs2) {
                cursor = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
            } else if (x >= region.x2 - cs2 && x <= region.x2 + cs2 && y >= region.y2 - cs2 && y <= region.y2 + cs2) {
                cursor = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
            } else if (x >= region.x1 - cs2 && x <= region.x1 + cs2 && y >= region.y2 - cs2 && y <= region.y2 + cs2) {
                cursor = Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
            } else if (x >= region.x1 - cs2 && x <= region.x1 + cs2 && y >= cy - cs2 && y <= cy + cs2) {
                cursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
            } else if (x >= region.x2 - cs2 && x <= region.x2 + cs2 && y >= cy - cs2 && y <= cy + cs2) {
                cursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
            } else if (x >= cx - cs2 && x <= cx + cs2 && y >= region.y1 - cs2 && y <= region.y1 + cs2) {
                cursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
            } else if (x >= cx - cs2 && x <= cx + cs2 && y >= region.y2 - cs2 && y <= region.y2 + cs2) {
                cursor = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
            } else if (x >= cx - cs2 && x <= cx + cs2 && y >= region.y1 - 35 - cs2 && y <= region.y1 - 35 + cs2) {
                cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
            } else {
                cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            }

            if (cursor != null && SketchletEditor.getInstance().editorFrame != null) {
                SketchletEditor.getInstance().setCursor(cursor);
            }
        } else {
            if (bPlayback) {
                region.getRenderer().getWidgetImageLayer().mouseMoved(e, x, y);
            }
        }
        mouseMovedEmbedded(x, y, System.currentTimeMillis(), e, frame, bPlayback);
    }

    public void mouseMovedEmbedded(int x, int y, long when, MouseEvent e, JFrame frame, boolean bPlayback) {
        if (!region.parent.getPage().isRegionsLayer()) {
            return;
        }
    }

    public void mousePressed(MouseEvent e, double scale, JFrame frame, boolean bPlayback) {
        if (!region.parent.getPage().isRegionsLayer()) {
            return;
        }
        int marginX = bPlayback ? region.parent.getOffsetX() : SketchletEditor.getInstance().getMarginX();
        int marginY = bPlayback ? region.parent.getOffsetY() : SketchletEditor.getInstance().getMarginY();
        int x = (int) ((e.getPoint().x) / scale) - marginX;
        int y = (int) ((e.getPoint().y) / scale) - marginY;

        mousePressed(x, y, e.getModifiers(), e.getWhen(), e, frame, bPlayback);
    }

    public void mousePressed(int x, int y, int modifiers, long when, MouseEvent e, JFrame frame, boolean bPlayback) {
        if (!region.parent.getPage().isRegionsLayer()) {
            return;
        }
        setPressedX(x);
        setPressedY(y);
            setStartX(x);
            setStartY(y);
        if (region.inTrajectoryMode && region.trajectory1.trim().isEmpty()) {
            int xCenter = region.x1 + (int) ((region.x2 - region.x1) * region.center_rotation_x);
            int yCenter = region.y1 + (int) ((region.y2 - region.y1) * region.center_rotation_y);
            this.startMouseXCenter = xCenter;
            this.startMouseYCenter = yCenter;
            region.trajectory1 += xCenter + " " + yCenter + " " + 0 + "\n";
        }
        if (region.inTrajectoryMode2 && region.trajectory2.trim().isEmpty()) {
            int xCenter = region.x1 + (int) ((region.x2 - region.x1) * region.trajectory2_x);
            int yCenter = region.y1 + (int) ((region.y2 - region.y1) * region.trajectory2_y);
            Point2D p = rotate(xCenter, yCenter);
            this.startMouseXCenter2 = (int) p.getX();
            this.startMouseYCenter2 = (int) p.getY();
            region.trajectory2 += (int) p.getX() + " " + (int) p.getY() + " " + 0 + "\n";
        }
        startTime = when;
        Point ip = inversePoint(x, y, bPlayback);
        x = ip.x;
        y = ip.y;
        if (bPlayback) {
            if ((modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                setSelectedCorner(MIDDLE);
            } else if ((modifiers & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                setSelectedCorner(MIDDLE);
                int xCenter = region.x1 + (int) ((region.x2 - region.x1) * region.center_rotation_x);
                int yCenter = region.y1 + (int) ((region.y2 - region.y1) * region.center_rotation_y);

                if (x == xCenter) {
                    setStartAngle(y > yCenter ? Math.PI / 2 : -Math.PI / 2);
                } else {
                    setStartAngle(Math.atan((double) (y - yCenter) / (x - xCenter)));
                }

                setStartAngle(getStartAngle() - region.rotation);

                if (x < xCenter) {
                    setStartAngle(getStartAngle() + Math.PI);
                }

                if (getStartAngle() < 0) {
                    setStartAngle(getStartAngle() + 2 * Math.PI);
                }
            }

            if (region.screenCapturingMouseMappingEnabled) {
                try {
                    int sx = (int) Double.parseDouble(region.captureScreenX);
                    int sy = (int) Double.parseDouble(region.captureScreenY);
                    sx += (int) (Double.parseDouble(region.captureScreenWidth) * ((double) x - region.x1) / region.getWidth());
                    sy += (int) (Double.parseDouble(region.captureScreenHeight) * ((double) y - region.y1) / region.getHeight());
                    try {
                        AWTRobotUtil.releaseMouse(InputEvent.BUTTON1_MASK);
                        AWTRobotUtil.releaseMouse(InputEvent.BUTTON2_MASK);
                        AWTRobotUtil.releaseMouse(InputEvent.BUTTON3_MASK);
                        if ((modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                            AWTRobotUtil.sendMouseClick(sx, sy, InputEvent.BUTTON1_MASK);
                        } else if ((modifiers & InputEvent.BUTTON2_MASK) == InputEvent.BUTTON2_MASK) {
                            AWTRobotUtil.sendMouseClick(sx, sy, InputEvent.BUTTON2_MASK);
                        } else if ((modifiers & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                            AWTRobotUtil.sendMouseClick(sx, sy, InputEvent.BUTTON3_MASK);
                        }
                        Thread.sleep(200);
                        AWTRobotUtil.moveMouse(MouseController.getMouseScreenX(), MouseController.getMouseScreenY());
                    } catch (Throwable re) {
                        log.error(e);
                    }

                } catch (Throwable sce) {
                }
            }
        } else {
            aspectRatio = (double) (region.y2 - region.y1) / (region.x2 - region.x1);
            if ((modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                if (!SketchletEditor.getInstance().isInCtrlMode()) {
                    int w = region.x2 - region.x1;
                    int h = region.y2 - region.y1;
                    int cx = region.x1 + w / 2;
                    int cy = region.y1 + h / 2;

                    int cs2 = (int) (ActiveRegionRenderer.CORNER_SIZE / SketchletEditor.getInstance().getScale() / 2) + 1;

                    if (x >= region.x1 - cs2 && x <= region.x1 + cs2 && y >= region.y1 - cs2 && y <= region.y1 + cs2) {
                        setSelectedCorner(UPPER_LEFT);
                    } else if (x >= region.x2 - cs2 && x <= region.x2 + cs2 && y >= region.y1 - cs2 && y <= region.y1 + cs2) {
                        setSelectedCorner(UPPER_RIGHT);
                    } else if (x >= region.x2 - cs2 && x <= region.x2 + cs2 && y >= region.y2 - cs2 && y <= region.y2 + cs2) {
                        setSelectedCorner(BOTTOM_RIGHT);
                    } else if (x >= region.x1 - cs2 && x <= region.x1 + cs2 && y >= region.y2 - cs2 && y <= region.y2 + cs2) {
                        setSelectedCorner(BOTTOM_LEFT);
                    } else if (x >= region.x1 - cs2 && x <= region.x1 + cs2 && y >= cy - cs2 && y <= cy + cs2) {
                        setSelectedCorner(LEFT);
                    } else if (x >= region.x2 - cs2 && x <= region.x2 + cs2 && y >= cy - cs2 && y <= cy + cs2) {
                        setSelectedCorner(RIGHT);
                    } else if (x >= cx - cs2 && x <= cx + cs2 && y >= region.y1 - cs2 && y <= region.y1 + cs2) {
                        setSelectedCorner(UPPER);
                    } else if (x >= cx - cs2 && x <= cx + cs2 && y >= region.y2 - cs2 && y <= region.y2 + cs2) {
                        setSelectedCorner(BOTTOM);
                    } else if (x >= cx - cs2 && x <= cx + cs2 && y >= region.y1 - 35 - cs2 && y <= region.y1 - 35 + cs2) {
                        setSelectedCorner(ROTATE);
                        startRotate(getStartX(), getStartY());
                    } else {
                        setSelectedCorner(MIDDLE);
                    }
                } else {
                    int w = region.x2 - region.x1;
                    int h = region.y2 - region.y1;

                    int _x0 = region.x1 + (int) (w * region.p_x0);
                    int _y0 = region.y1 + (int) (h * region.p_y0);
                    int _x1 = region.x1 + (int) (w * region.p_x1);
                    int _y1 = region.y1 + (int) (h * region.p_y1);
                    int _x2 = region.x1 + (int) (w * region.p_x2);
                    int _y2 = region.y1 + (int) (h * region.p_y2);
                    int _x3 = region.x1 + (int) (w * region.p_x3);
                    int _y3 = region.y1 + (int) (h * region.p_y3);

                    int c_x = region.x1 + (int) (w * region.center_rotation_x);
                    int c_y = region.y1 + (int) (h * region.center_rotation_y);

                    int t2_x = region.x1 + (int) (w * region.trajectory2_x);
                    int t2_y = region.y1 + (int) (h * region.trajectory2_y);

                    int cs2 = (int) (ActiveRegionRenderer.CORNER_SIZE / SketchletEditor.getInstance().getScale() / 2) + 1;

                    if (!SketchletEditor.getInstance().isInShiftMode() && x >= c_x - cs2 && x <= c_x + cs2 && y >= c_y - cs2 && y <= c_y + cs2) {
                        setSelectedCorner(CENTER_ROTATION);
                        setCenterRotationX(region.center_rotation_x);
                        setCenterRotationY(region.center_rotation_y);
                    } else if (SketchletEditor.getInstance().isInShiftMode() && x >= t2_x - cs2 && x <= t2_x + cs2 && y >= t2_y - cs2 && y <= t2_y + cs2) {
                        setSelectedCorner(TRAJECTORY2_POINT);
                        this.setTrajectory2X(region.trajectory2_x);
                        this.setTrajectory2Y(region.trajectory2_y);
                    } else if (x >= _x0 - cs2 && x <= _x0 + cs2 && y >= _y0 - cs2 && y <= _y0 + cs2) {
                        setSelectedCorner(PERSPECTIVE_UPPER_LEFT);
                    } else if (x >= _x1 - cs2 && x <= _x1 + cs2 && y >= _y1 - cs2 && y <= _y1 + cs2) {
                        setSelectedCorner(PERSPECTIVE_UPPER_RIGHT);
                    } else if (x >= _x2 - cs2 && x <= _x2 + cs2 && y >= _y2 - cs2 && y <= _y2 + cs2) {
                        setSelectedCorner(PERSPECTIVE_BOTTOM_RIGHT);
                    } else if (x >= _x3 - cs2 && x <= _x3 + cs2 && y >= _y3 - cs2 && y <= _y3 + cs2) {
                        setSelectedCorner(PERSPECTIVE_BOTTOM_LEFT);
                    }
                }
            } else if ((modifiers & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                startRotate(getStartX(), getStartY());
            }
        }
        if (bPlayback) {
            region.getRenderer().getWidgetImageLayer().mousePressed(e, x, y);
        }
        mousePressedEmbedded(x, y, modifiers, when, e, frame, bPlayback);
    }

    public void startRotate(int x, int y) {
        if (!region.parent.getPage().isRegionsLayer()) {
            return;
        }
        setbRotating(true);
        int xCenter = region.x1 + (int) ((region.x2 - region.x1) * region.center_rotation_x);
        int yCenter = region.y1 + (int) ((region.y2 - region.y1) * region.center_rotation_y);

        if (x == xCenter) {
            setStartAngle(y > yCenter ? Math.PI / 2 : -Math.PI / 2);
        } else {
            setStartAngle(Math.atan((double) (y - yCenter) / (x - xCenter)));
        }

        setStartAngle(getStartAngle() - region.rotation);

        if (x < xCenter) {
            setStartAngle(getStartAngle() + Math.PI);
        }

        if (getStartAngle() < 0) {
            setStartAngle(getStartAngle() + 2 * Math.PI);
        }

    }

    public boolean mousePressedEmbedded(int x, int y, int modifiers, long when, MouseEvent e, JFrame frame, boolean bPlayback) {
        if (!region.parent.getPage().isRegionsLayer()) {
            return false;
        }

        return false;
    }

    public void mouseReleased(MouseEvent e, double scale, JFrame frame, boolean bPlayback) {
        region.bAdjusting = false;
        if (!region.parent.getPage().isRegionsLayer()) {
            return;
        }
        int marginX = bPlayback ? region.parent.getOffsetX() : SketchletEditor.getInstance().getMarginX();
        int marginY = bPlayback ? region.parent.getOffsetY() : SketchletEditor.getInstance().getMarginY();
        int x = (int) ((e.getPoint().x) / scale) - marginX;
        int y = (int) ((e.getPoint().y) / scale) - marginY;
        mouseReleased(x, y, e.getModifiers(), e.getWhen(), e, frame, bPlayback);
    }

    public void mouseReleased(int x, int y, int modifiers, long when, MouseEvent e, JFrame frame, boolean bPlayback) {
        region.bAdjusting = false;
        if (this.getSelectedCorner() == CENTER_ROTATION) {
            region.center_rotation_x = getCenterRotationX();
            region.center_rotation_y = getCenterRotationY();
            return;
        } else if (this.getSelectedCorner() == TRAJECTORY2_POINT) {
            region.trajectory2_x = getTrajectory2X();
            region.trajectory2_y = getTrajectory2Y();

            setSelectedCorner(-1);
            return;
        }
        if (!region.parent.getPage().isRegionsLayer()) {
            return;
        }
        if (isbRotating()) {
            region.rotation = Math.toRadians((int) Math.toDegrees(region.rotation));
            setbRotating(false);
        }
        Point ip = inversePoint(x, y, bPlayback);
        x = ip.x;
        y = ip.y;
        if ((modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {

            int _x1 = Math.min(region.x1, region.x2);
            int _x2 = Math.max(region.x1, region.x2);
            int _y1 = Math.min(region.y1, region.y2);
            int _y2 = Math.max(region.y1, region.y2);

            if (!bPlayback && SketchletEditor.isSnapToGrid()) {
                int s = InteractionSpace.getGridSpacing();
                _x1 = ((_x1 + s / 2) / s) * s;
                _x2 = ((_x2 + s / 2) / s) * s;
                _y1 = ((_y1 + s / 2) / s) * s;
                _y2 = ((_y2 + s / 2) / s) * s;
            }

            region.x1 = _x1;
            region.y1 = _y1;
            region.x2 = _x2;
            region.y2 = _y2;

            SketchletEditor.getInstance().repaint();
        } else if ((modifiers & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
            SketchletEditor.getInstance().repaint();
        }
        region.getMotionController().processLimits("speed", 0.0, 0.0, 0.0, true);
        if (bPlayback) {
            region.mouseProcessor.processAction(e, frame, new int[]{MouseProcessor.MOUSE_LEFT_BUTTON_RELEASE, MouseProcessor.MOUSE_MIDDLE_BUTTON_RELEASE, MouseProcessor.MOUSE_RIGHT_BUTTON_RELEASE});
        }

        if (region.inTrajectoryMode) {
            if (region.trajectoryType < 2) {
                processTrajectory();
                region.inTrajectoryMode = false;
            } else if (region.trajectoryType == 2) {
                int xCenter = region.x1 + (int) ((region.x2 - region.x1) * region.center_rotation_x);
                int yCenter = region.y1 + (int) ((region.y2 - region.y1) * region.center_rotation_y);
                region.trajectory1 += xCenter + " " + yCenter + " " + 0 + "\n";
            } else if (region.trajectoryType == 3) {
                processTrajectory();
                region.inTrajectoryMode = false;
            }
            region.trajectory1 = simplifyTrajectory(region.trajectory1);
        } else if (region.inTrajectoryMode2) {
            if (region.trajectoryType < 2) {
                processTrajectory2();
                region.inTrajectoryMode2 = false;
            } else if (region.trajectoryType == 2) {
                int xCenter = region.x1 + (int) ((region.x2 - region.x1) * region.trajectory2_x);
                int yCenter = region.y1 + (int) ((region.y2 - region.y1) * region.trajectory2_y);
                Point2D p = rotate(xCenter, yCenter);
                region.trajectory2 += (int) p.getX() + " " + (int) p.getY() + " " + 0 + "\n";
            } else if (region.trajectoryType == 3) {
                region.inTrajectoryMode2 = false;
            }
            region.trajectory2 = simplifyTrajectory(region.trajectory2);
        }
        if (bPlayback) {
            region.getRenderer().getWidgetImageLayer().mouseReleased(e, x, y);
        }
    }

    public String simplifyTrajectory(String text) {
        try {
            Vector<TrajectoryPoint> tps = region.createTrajectoryVector(text);
            int dist = 6;
            TrajectoryPoint points[] = new TrajectoryPoint[tps.size()];
            points = tps.toArray(points);
            TrajectoryPoint prevtp = null;
            SketchletEditor.getInstance().saveRegionUndo();
            for (int i = 0; i < points.length; i++) {
                TrajectoryPoint tp = points[i];
                if (prevtp == null) {
                    prevtp = tp;
                    continue;
                }

                if (Math.sqrt((tp.getX() - prevtp.getX()) * (tp.getX() - prevtp.getX()) + (tp.getY() - prevtp.getY()) * (tp.getY() - prevtp.getY())) < dist) {
                    tps.remove(tp);
                } else {
                    prevtp = tp;
                }
            }

            String strTrajectory = "";
            for (TrajectoryPoint tp : tps) {
                strTrajectory += tp.getX() + " " + tp.getY() + " " + tp.getTime() + "\n";
            }
            return strTrajectory;
        } catch (Exception e) {
        }

        return text;
    }

    public void processTrajectory() {
        if (!region.parent.getPage().isRegionsLayer()) {
            return;
        }
        JPanel panel = new JPanel(new SpringLayout());
        JLabel label1 = new JLabel(Language.translate("Do you also want to create a new timer"));
        JLabel label2 = new JLabel(Language.translate("using the trajectory timing data?"));
        JCheckBox curveCheck = new JCheckBox(Language.translate("Create timer curve with geture timing"), true);
        JCheckBox timerCheck = new JCheckBox(Language.translate("Create timer"), true);
        JCheckBox controlOrientation = new JCheckBox(Language.translate("Control orientation"), region.changingOrientationOnTrajectoryEnabled);
        JCheckBox restartCheck = new JCheckBox(Language.translate("Restart variables on sketch entry and exit"), true);
        panel.add(label1);
        if (region.trajectoryType == 0) {
            panel.add(label2);
        } else {
            label1.setText(label1.getText() + "?");
        }
        panel.add(timerCheck);
        panel.add(restartCheck);
        panel.add(controlOrientation);
        if (region.trajectoryType == 0) {
            panel.add(curveCheck);
        } else {
            curveCheck.setSelected(false);
        }
        if (region.trajectoryType == 0) {
            SpringUtilities.makeCompactGrid(panel,
                    6, 1, //rows, cols
                    5, 5, //initialX, initialY
                    5, 5);//xPad, yPad
        } else {
            SpringUtilities.makeCompactGrid(panel,
                    4, 1, //rows, cols
                    5, 5, //initialX, initialY
                    5, 5);//xPad, yPad
        }
        Object[] options = {"Save", "Skip"};
        int n = JOptionPane.showOptionDialog(SketchletEditor.editorFrame,
                panel,
                "Gesture Data",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (n == 0) {
            Vector<TrajectoryPoint> tps = region.createTrajectoryVector();

            if (tps.size() > 1) {

                double totalDuration = tps.lastElement().getTime();

                Timer t = null;
                if (timerCheck.isSelected()) {
                    t = SketchletEditor.getInstance().getExtraEditorPanel().timersExtraPanel.newTimer();
                    if (region.trajectoryType == 0) {
                        t.setStrDurationInSec("" + (totalDuration / 1000.0));
                        if (t.getPanel() != null) {
                            t.getPanel().fieldDuration.setText(t.getStrDurationInSec() + "");
                        }
                    }
                }
                // t.defaultCurve = c.name;
                String newVariable = VariablesBlackboard.getInstance().getUniqueVariableName("trajectory");
                if (curveCheck.isSelected()) {
                    Curve c = Curves.getGlobalCurves().addNewCurve();

                    int i = 1;
                    double count = tps.size();
                    for (TrajectoryPoint tp : tps) {
                        if (tp.getTime() / totalDuration < 1.0) {
                            c.addSegment(tp.getTime() / totalDuration, i / count);
                        }
                        i++;
                    }
                    c.sort();
                    Curves.getGlobalCurves().save();
                    if (CurvesFrame.frame != null) {
                        CurvesFrame.frame.load();
                        CurvesFrame.frame.tabs.setSelectedIndex(CurvesFrame.frame.tabs.getTabCount() - 1);
                    }
                    // CurvesFrame.frame.toFront();
                    if (t != null) {
                        t.getVariables()[0][4] = c.getName();
                    }
                }

                if (restartCheck.isSelected()) {
                    Object[][] transformations = {
                            {new JCheckBox("", true), new JTextField("0.0"), new JTextField("1.0"), new JTextField(newVariable)}
                    };
                    AnimationTimerDialog.initTimerVariables(region, transformations, t);
                } else {
                    t.getVariables()[0][0] = newVariable;
                    t.getVariables()[0][1] = "0.0";
                    t.getVariables()[0][2] = "1.0";
                }

                region.changingOrientationOnTrajectoryEnabled = controlOrientation.isSelected();

                region.strTrajectoryPosition = "=" + newVariable;
                if (t != null && region.trajectoryType == 0) {
                    t.setStrDurationInSec("" + (totalDuration / 1000.0));
                }
            }
        }
        region.inTrajectoryMode = false;
    }

    public void processTrajectory2() {
        region.inTrajectoryMode2 = false;
    }

    public boolean mouseReleasedEmbedded(int x, int y, int modifiers, long when, MouseEvent e, JFrame frame, boolean bPlayback) {
        if (!region.parent.getPage().isRegionsLayer()) {
            return false;
        }

        return false;
    }

    public void mouseDragged(MouseEvent e, double scale, JFrame frame, boolean bPlayback) {
        if (!region.parent.getPage().isRegionsLayer()) {
            return;
        }
        int marginX = bPlayback ? region.parent.getOffsetX() : SketchletEditor.getInstance().getMarginX();
        int marginY = bPlayback ? region.parent.getOffsetY() : SketchletEditor.getInstance().getMarginY();
        int x = (int) ((e.getPoint().x) / scale) - marginX;
        int y = (int) ((e.getPoint().y) / scale) - marginY;
        mouseDragged(x, y, e.getModifiers(), e.getWhen(), scale, e, frame, bPlayback);
    }

    public synchronized void mouseDragged(int x, int y, int modifiers, long when, double scale, MouseEvent e, JFrame frame, boolean bPlayback) {
        if (!region.parent.getPage().isRegionsLayer()) {
            return;
        }
        if (!bPlayback && region.pinned) {
            return;
        }
        region.bAdjusting = true;
        int prevX1 = region.x1;
        int prevY1 = region.y1;
        int prevX2 = region.x2;
        int prevY2 = region.y2;

        int _x = x, _y = y;

        boolean bMoved = true;

        if (bPlayback && (!region.widget.isEmpty())) {
            Point ip = inversePoint(_x, _y, bPlayback);
            bMoved = false;
            region.getRenderer().getWidgetImageLayer().mouseDragged(e, x, y);
        } else if ((!bPlayback || region.movable) && (modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
            if (getSelectedCorner() != MIDDLE) {
                if (bPlayback) {
                    return;
                }
                Point ip = inversePoint(x, y, bPlayback);
                x = ip.x;
                y = ip.y;
            }
            int w = region.x2 - region.x1;
            int h = region.y2 - region.y1;
            switch (getSelectedCorner()) {
                case MIDDLE:
                    int dx = x - getStartX();
                    int dy = y - getStartY();
                    region.x1 += dx;
                    region.y1 += dy;
                    region.x2 += dx;
                    region.y2 += dy;

                    break;
                case UPPER_LEFT:
                    if (!SketchletEditor.getInstance().isInShiftMode()) {
                        dx = x - region.x1;
                        dy = y - region.y1;
                        if (!region.strWidth.startsWith("=")) {
                            region.x1 = x;
                            if (region.rotation != 0) {
                                region.x2 -= dx;
                            }
                        }
                        if (!region.strHeight.startsWith("=")) {
                            region.y1 = y;
                            if (region.rotation != 0) {
                                region.y2 -= dy;
                            }
                        }
                    } else {
                        dx = x - region.x1;
                        if (!region.strWidth.startsWith("=")) {
                            region.x1 = x;
                            if (region.rotation != 0) {
                                region.x2 -= dx;
                            }
                        }
                        dy = region.y2 - (int) ((region.x2 - region.x1) * aspectRatio) - region.y2;
                        if (!region.strHeight.startsWith("=")) {
                            region.y1 = region.y2 - (int) ((region.x2 - region.x1) * aspectRatio);
                            if (region.rotation != 0) {
                                region.y2 -= dy;
                            }
                        }
                    }
                    break;
                case UPPER_RIGHT:
                    if (!SketchletEditor.getInstance().isInShiftMode()) {
                        dx = x - region.x2;
                        dy = y - region.y1;
                        if (!region.strWidth.startsWith("=")) {
                            region.x2 = x;
                            if (region.rotation != 0) {
                                region.x1 -= dx;
                            }
                        }
                        if (!region.strHeight.startsWith("=")) {
                            region.y1 = y;
                            if (region.rotation != 0) {
                                region.y2 -= dy;
                            }
                        }
                    } else {
                        dx = x - region.x1;
                        if (!region.strWidth.startsWith("=")) {
                            region.x2 = x;
                            if (region.rotation != 0) {
                                region.x1 -= dx;
                            }
                        }
                        dy = region.y2 - (int) ((region.x2 - region.x1) * aspectRatio) - region.y1;
                        if (!region.strHeight.startsWith("=")) {
                            region.y1 = region.y2 - (int) ((region.x2 - region.x1) * aspectRatio);
                            if (region.rotation != 0) {
                                region.y2 -= dy;
                            }
                        }
                    }
                    break;
                case BOTTOM_LEFT:
                    if (!SketchletEditor.getInstance().isInShiftMode()) {
                        dx = x - region.x1;
                        dy = y - region.y2;
                        if (!region.strWidth.startsWith("=")) {
                            region.x1 = x;
                            if (region.rotation != 0) {
                                region.x2 -= dx;
                            }
                        }
                        if (!region.strHeight.startsWith("=")) {
                            region.y2 = y;
                            if (region.rotation != 0) {
                                region.y1 -= dy;
                            }
                        }
                    } else {
                        dx = x - region.x1;
                        if (!region.strWidth.startsWith("=")) {
                            region.x1 = x;
                            if (region.rotation != 0) {
                                region.x2 -= dx;
                            }
                        }
                        dy = region.y1 + (int) ((region.x2 - region.x1) * aspectRatio) - region.y2;
                        if (!region.strHeight.startsWith("=")) {
                            region.y2 = region.y1 + (int) ((region.x2 - region.x1) * aspectRatio);
                            if (region.rotation != 0) {
                                region.y1 -= dy;
                            }
                        }
                    }
                    break;
                case BOTTOM_RIGHT:
                    if (!SketchletEditor.getInstance().isInShiftMode()) {
                        dx = x - region.x2;
                        dy = y - region.y2;
                        if (!region.strWidth.startsWith("=")) {
                            region.x2 = x;
                            if (region.rotation != 0) {
                                region.x1 -= dx;
                            }
                        }
                        if (!region.strHeight.startsWith("=")) {
                            region.y2 = y;
                            if (region.rotation != 0) {
                                region.y1 -= dy;
                            }
                        }
                    } else {
                        dx = x - region.x2;
                        if (!region.strWidth.startsWith("=")) {
                            region.x2 = x;
                            if (region.rotation != 0) {
                                region.x1 -= dx;
                            }
                        }
                        dy = region.y1 + (int) ((region.x2 - region.x1) * aspectRatio) - region.y2;
                        if (!region.strHeight.startsWith("=")) {
                            region.y2 = region.y1 + (int) ((region.x2 - region.x1) * aspectRatio);
                            if (region.rotation != 0) {
                                region.y1 -= dy;
                            }
                        }
                    }
                    break;
                case UPPER:
                    if (!region.strHeight.startsWith("=")) {
                        dy = y - region.y1;
                        region.y1 = y;
                        if (region.rotation != 0) {
                            region.y2 -= dy;
                        }
                    }
                    break;
                case BOTTOM:
                    if (!region.strHeight.startsWith("=")) {
                        dy = y - region.y2;
                        region.y2 = y;
                        if (region.rotation != 0) {
                            region.y1 -= dy;
                        }
                    }
                    break;
                case LEFT:
                    if (!region.strWidth.startsWith("=")) {
                        dx = x - region.x1;
                        region.x1 = x;
                        if (region.rotation != 0) {
                            region.x2 -= dx;
                        }
                    }
                    break;
                case RIGHT:
                    if (!region.strWidth.startsWith("=")) {
                        dx = x - region.x2;
                        region.x2 = x;
                        if (region.rotation != 0) {
                            region.x1 -= dx;
                        }
                    }
                    break;
                case PERSPECTIVE_UPPER_LEFT:
                    region.p_x0 = (double) (x - region.x1) / w;
                    region.p_y0 = (double) (y - region.y1) / h;

                    region.p_x0 = Math.max(0.0, region.p_x0);
                    region.p_x0 = Math.min(1.0, region.p_x0);
                    region.p_y0 = Math.max(0.0, region.p_y0);
                    region.p_y0 = Math.min(1.0, region.p_y0);
                    break;
                case CENTER_ROTATION:
                    setCenterRotationX((double) (x - region.x1) / w);
                    setCenterRotationY((double) (y - region.y1) / h);

                    setCenterRotationX(Math.max(0.0, getCenterRotationX()));
                    setCenterRotationX(Math.min(1.0, getCenterRotationX()));
                    setCenterRotationY(Math.max(0.0, getCenterRotationY()));
                    setCenterRotationY(Math.min(1.0, getCenterRotationY()));
                    break;
                case TRAJECTORY2_POINT:
                    setTrajectory2X((double) (x - region.x1) / w);
                    setTrajectory2Y((double) (y - region.y1) / h);

                    setTrajectory2X(Math.max(0.0, getTrajectory2X()));
                    setTrajectory2X(Math.min(1.0, getTrajectory2X()));
                    setTrajectory2Y(Math.max(0.0, getTrajectory2Y()));
                    setTrajectory2Y(Math.min(1.0, getTrajectory2Y()));

                    break;
                case PERSPECTIVE_UPPER_RIGHT:
                    region.p_x1 = (double) (x - region.x1) / w;
                    region.p_y1 = (double) (y - region.y1) / h;
                    region.p_x1 = Math.max(0.0, region.p_x1);
                    region.p_x1 = Math.min(1.0, region.p_x1);
                    region.p_y1 = Math.max(0.0, region.p_y1);
                    region.p_y1 = Math.min(1.0, region.p_y1);
                    break;
                case PERSPECTIVE_BOTTOM_RIGHT:
                    region.p_x2 = (double) (x - region.x1) / w;
                    region.p_y2 = (double) (y - region.y1) / h;
                    region.p_x2 = Math.max(0.0, region.p_x2);
                    region.p_x2 = Math.min(1.0, region.p_x2);
                    region.p_y2 = Math.max(0.0, region.p_y2);
                    region.p_y2 = Math.min(1.0, region.p_y2);
                    break;
                case PERSPECTIVE_BOTTOM_LEFT:
                    region.p_x3 = (double) (x - region.x1) / w;
                    region.p_y3 = (double) (y - region.y1) / h;
                    region.p_x3 = Math.max(0.0, region.p_x3);
                    region.p_x3 = Math.min(1.0, region.p_x3);
                    region.p_y3 = Math.max(0.0, region.p_y3);
                    region.p_y3 = Math.min(1.0, region.p_y3);
                    break;
                case ROTATE:
                    rotate(_x, _y, bPlayback);
                    break;
            }

            if (getSelectedCorner() != ROTATE && getSelectedCorner() != TRAJECTORY2_POINT && getSelectedCorner() != CENTER_ROTATION) {
                setStartX(x);
                setStartY(y);

                region.getInteractionController().processInteractionEvents(bPlayback, region.parent.getPage().getActiveTimers(), region.parent.getPage().getActiveMacros());
                region.getSketch().updateConnectors(region, bPlayback);

                if (!region.isWithinLimits(false) || region.getInteractionController().intersectsWithSolids(bPlayback)) {
                    region.parent.getOverlapHelper().findNonOverlappingLocation(region);
                    return;
                }

                if (!bPlayback) {
                    if (region.parent != null && region.parent.getMouseHelper().getSelectedRegions() != null) {
                        for (ActiveRegion as : region.parent.getMouseHelper().getSelectedRegions()) {
                            if (as != region) {
                                as.x1 += region.x1 - prevX1;
                                as.y1 += region.y1 - prevY1;
                                as.x2 += region.x2 - prevX2;
                                as.y2 += region.y2 - prevY2;
                            }
                        }
                    }
                }
                if (region.horizontalAlignment.equalsIgnoreCase("center")) {
                    region.getMotionController().processLimits("position x", region.x1 + w / 2, w / 2, w / 2, true);
                } else if (region.horizontalAlignment.equalsIgnoreCase("right")) {
                    region.getMotionController().processLimits("position x", region.x1 + w, w, 0, true);
                } else {
                    region.getMotionController().processLimits("position x", region.x1, 0, w, true);
                }
                if (region.verticalAlignment.equalsIgnoreCase("center")) {
                    region.getMotionController().processLimits("position y", region.y1 + h / 2, h / 2, h / 2, true);
                } else if (region.verticalAlignment.equalsIgnoreCase("right")) {
                    region.getMotionController().processLimits("position y", region.y1 + h, h, 0, true);
                } else {
                    region.getMotionController().processLimits("position y", region.y1, 0, h, true);
                }

                if (region.inTrajectoryMode) {
                    int xCenter = region.x1 + (int) ((region.x2 - region.x1) * region.center_rotation_x);
                    int yCenter = region.y1 + (int) ((region.y2 - region.y1) * region.center_rotation_y);
                    if (region.trajectoryType == 0) {
                        region.trajectory1 += xCenter + " " + yCenter + " " + (when - startTime) + "\n";
                    } else if (region.trajectoryType == 1) {
                        region.trajectory1 = startMouseXCenter + " " + startMouseYCenter + " " + 0 + "\n";
                        region.trajectory1 += xCenter + " " + yCenter + " " + (when - startTime) + "\n";
                    } else if (region.trajectoryType == 2) {
                    } else if (region.trajectoryType == 3) {
                        int xEnd = region.x1 + (int) ((region.x2 - region.x1) * region.center_rotation_x);
                        int yEnd = region.y1 + (int) ((region.y2 - region.y1) * region.center_rotation_y);

                        int __x1 = Math.min(getPressedX(), xEnd);
                        int __x2 = Math.max(getPressedX(), xEnd);
                        int __y1 = Math.min(getPressedY(), yEnd);
                        int __y2 = Math.max(getPressedY(), yEnd);

                        int _w = __x2 - __x1;
                        int _h = __y2 - __y1;
                        int _cx = __x1 + _w / 2;
                        int _cy = __y1 + _h / 2;

                        region.trajectory1 = "";
                        for (int i = 0; i < 360; i += 10) {
                            double __x = _cx + Math.cos(Math.toRadians(i)) * _w / 2;
                            double __y;
                            if (SketchletEditor.getInstance().isInShiftMode()) {
                                __y = _cy + Math.sin(Math.toRadians(i)) * _w / 2;
                            } else {
                                __y = _cy + Math.sin(Math.toRadians(i)) * _h / 2;
                            }
                            region.trajectory1 += (int) __x + " " + (int) __y + " " + 0 + "\n";
                        }
                    }
                } else if (region.inTrajectoryMode2) {
                    int xCenter2 = region.x1 + (int) ((region.x2 - region.x1) * region.trajectory2_x);
                    int yCenter2 = region.y1 + (int) ((region.y2 - region.y1) * region.trajectory2_y);
                    Point2D p = rotate(xCenter2, yCenter2);
                    xCenter2 = (int) p.getX();
                    yCenter2 = (int) p.getY();
                    if (region.trajectoryType == 0) {
                        region.trajectory2 += xCenter2 + " " + yCenter2 + " " + (when - startTime) + "\n";
                    } else if (region.trajectoryType == 1) {
                        region.trajectory2 = startMouseXCenter2 + " " + startMouseYCenter2 + " " + 0 + "\n";
                        region.trajectory2 += xCenter2 + " " + yCenter2 + " " + (when - startTime) + "\n";
                    } else if (region.trajectoryType == 2) {
                    } else if (region.trajectoryType == 3) {
                        int xEnd = region.x1 + (int) ((region.x2 - region.x1) * region.center_rotation_x);
                        int yEnd = region.y1 + (int) ((region.y2 - region.y1) * region.center_rotation_y);

                        int __x1 = Math.min(getPressedX(), xEnd);
                        int __x2 = Math.max(getPressedX(), xEnd);
                        int __y1 = Math.min(getPressedY(), yEnd);
                        int __y2 = Math.max(getPressedY(), yEnd);

                        int _w = __x2 - __x1;
                        int _h = __y2 - __y1;
                        int _cx = __x1 + _w / 2;
                        int _cy = __y1 + _h / 2;

                        region.trajectory2 = "";
                        for (int i = 0; i < 360; i += 10) {
                            double __x = _cx + Math.cos(Math.toRadians(i)) * _w / 2;
                            double __y;
                            if (SketchletEditor.getInstance().isInShiftMode()) {
                                __y = _cy + Math.sin(Math.toRadians(i)) * _w / 2;
                            } else {
                                __y = _cy + Math.sin(Math.toRadians(i)) * _h / 2;
                            }
                            region.trajectory2 += (int) __x + " " + (int) __y + " " + 0 + "\n";
                        }
                    }
                } else {
                    if (!region.trajectory1.trim().equals("")) {
                        if (region.stickToTrajectoryEnabled) {
                            Point p = region.getRenderer().getTrajectoryDrawingLayer().getClosestTrajectoryPoint(new Point(x, y));

                            region.x1 = (int) (p.x - region.center_rotation_x * w);
                            region.y1 = (int) (p.y - region.center_rotation_y * h);
                            region.x2 = region.x1 + w;
                            region.y2 = region.y1 + h;

                            region.getMotionController().processLimits("trajectory position", region.getRenderer().getTrajectoryDrawingLayer().trajectoryPositionFromPoint, 0.0, 1.0, 0.0, 0.0, true);
                            region.getMotionController().processLimits("trajectory position 2", region.getRenderer().getTrajectoryDrawingLayer().trajectoryPositionFromPoint2, 0.0, 1.0, 0.0, 0.0, true);
                        }

                        if (!region.isWithinLimits(false) || region.getInteractionController().intersectsWithSolids(bPlayback)) {
                            region.parent.getOverlapHelper().findNonOverlappingLocation(region);
                            return;
                        }

                        if (region.changingOrientationOnTrajectoryEnabled) {
                            region.rotation = region.getRenderer().getTrajectoryDrawingLayer().trajectoryOrientationFromPoint;
                        }
                    }
                }
            }
        } else if ((!bPlayback || region.rotatable) && (modifiers & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
            int marginX = bPlayback ? region.parent.getOffsetX() : SketchletEditor.getInstance().getMarginX();
            int marginY = bPlayback ? region.parent.getOffsetY() : SketchletEditor.getInstance().getMarginY();
            int __x = (int) ((e.getPoint().x) / scale) - marginX;
            int __y = (int) ((e.getPoint().y) / scale) - marginY;
            rotate(__x, __y, bPlayback);
            bMoved = false;
        } else {
            Point ip = inversePoint(_x, _y, bPlayback);
            _x = ip.x;
            _y = ip.y;
            mouseDragEmbedded(_x, _y, modifiers, when, e, frame, bPlayback);
            bMoved = false;
        }

        if (bMoved && bPlayback) {
            int dx = region.x1 - prevX1;
            int dy = region.y1 - prevY1;
            if (dx != 0 || dy != 0) {
                if (!region.regionGrouping.equals("")) {
                    for (ActiveRegion as : region.parent.getRegions()) {
                        if (as != region && as.regionGrouping.equals(region.regionGrouping)) {
                            as.x1 += dx;
                            as.y1 += dy;
                            as.x2 += dx;
                            as.y2 += dy;
                            as.getMotionController().processLimits("position x", as.x1, 0, 0, true);
                            as.getMotionController().processLimits("position y", as.y1, 0, 0, true);
                        }
                    }
                }
            }
        }

        if (bPlayback) {
            PlaybackFrame.repaintAllFrames();
        } else {
            SketchletEditor.getInstance().repaint();
        }
    }

    public Point2D rotate(int x, int y) {
        int xCenter = region.x1 + (int) ((region.x2 - region.x1) * region.center_rotation_x);
        int yCenter = region.y1 + (int) ((region.y2 - region.y1) * region.center_rotation_y);
        AffineTransform transformer = AffineTransform.getRotateInstance(region.rotation, xCenter, yCenter);
        Point2D before = new Point2D.Double(x, y);
        Point2D after = new Point2D.Double();
        after = transformer.transform(before, after);
        return after;
    }

    public boolean correctPosition(int prevX1, int prevY1, int prevX2, int prevY2) {
        int _x1 = region.x1;
        int _x2 = region.x2;

        region.x1 = prevX1;
        region.x2 = prevX2;
        if (region.isWithinLimits(false)) {
            return true;
        }
        region.x1 = _x1;
        region.x2 = _x2;
        region.y1 = prevY1;
        region.y2 = prevY2;
        if (region.isWithinLimits(false)) {
            return true;
        }

        return false;
    }

    private boolean bRotating = false;

    public void rotate(int x, int y, boolean bPlayback) {
        double prevRotation = region.rotation;
        int xCenter = region.x1 + (int) ((region.x2 - region.x1) * region.center_rotation_x);
        int yCenter = region.y1 + (int) ((region.y2 - region.y1) * region.center_rotation_y);

        if (x == xCenter) {
            region.rotation = y > yCenter ? Math.PI / 2 : -Math.PI / 2;
        } else {
            region.rotation = Math.atan((double) (y - yCenter) / (x - xCenter));
        }

        if (x < xCenter) {
            region.rotation += Math.PI;
        }

        region.rotation = region.rotation - (bPlayback ? 0 : getStartAngle());
        region.rotation = InteractionSpace.toRadians(region.getMotionController().processLimits("rotation", InteractionSpace.toPhysicalAngle(region.rotation), 0.0, 0, true));
        region.getInteractionController().processInteractionEvents(bPlayback, region.parent.getPage().getActiveTimers(), region.parent.getPage().getActiveMacros());
        region.getSketch().updateConnectors(region, bPlayback);

        if (!region.isWithinLimits(false) || region.getInteractionController().intersectsWithSolids(bPlayback)) {
            region.rotation = prevRotation;
            region.parent.getOverlapHelper().findNonOverlappingLocation(region);
            return;
        }

    }

    public static Rectangle getResizedRect(ActiveRegion region, double angle, int x1, int y1, int x2, int y2, int new_x1, int new_y1, int new_x2, int new_y2) {
        double oldCenterX = x1 + (x2 - x1) * region.center_rotation_x;
        double oldCenterY = y1 + (y2 - y1) * region.center_rotation_y;
        double newCenterX = new_x1 + (new_x2 - new_x1) * region.center_rotation_x;
        double newCenterY = new_y1 + (new_y2 - new_y1) * region.center_rotation_y;

        AffineTransform aft1 = new AffineTransform();
        aft1.rotate(angle, oldCenterX, oldCenterY);
        aft1.rotate(-angle, newCenterX, newCenterY);

        Point2D pxy1 = aft1.transform(new Point(new_x1, new_y1), null);
        Point2D pxy2 = aft1.transform(new Point(new_x2, new_y2), null);

        return new Rectangle((int) pxy1.getX(), (int) pxy1.getY(), (int) (pxy2.getX() - pxy1.getX()), (int) (pxy2.getY() - pxy1.getY()));
    }

    public boolean mouseDragEmbedded(int x, int y, int modifiers, long when, MouseEvent e, JFrame frame, boolean bPlayback) {
        if (!region.parent.getPage().isRegionsLayer()) {
            return false;
        }

        return false;
    }

    public boolean inRect(int x, int y, boolean bPlayback) {
        Point ip = inversePoint(x, y, bPlayback);
        x = ip.x;
        y = ip.y;
        if (bPlayback) {
            Polygon p = new Polygon();
            int w = Math.abs(region.x2 - region.x1);
            int h = Math.abs(region.y2 - region.y1);
            p.addPoint(region.x1 + (int) (region.p_x0 * w), region.y1 + (int) (region.p_y0 * h));
            p.addPoint(region.x1 + (int) (region.p_x1 * w), region.y1 + (int) (region.p_y1 * h));
            p.addPoint(region.x1 + (int) (region.p_x2 * w), region.y1 + (int) (region.p_y2 * h));
            p.addPoint(region.x1 + (int) (region.p_x3 * w), region.y1 + (int) (region.p_y3 * h));
            return region.getArea(true).contains(x, y);
        } else {
            if (SketchletEditor.getInstance() != null) {
                int cs2 = (int) (ActiveRegionRenderer.CORNER_SIZE / SketchletEditor.getInstance().getScale() / 2) + 1;

                boolean bInRect = x >= region.x1 - cs2 && x <= region.x2 + cs2 / 2 && y >= region.y1 - cs2 / 2 && y <= region.y2 + cs2 / 2;
                boolean inRotateZone = x >= region.x1 + (region.x2 - region.x1) * region.center_rotation_x - cs2 / 2 && x <= region.x2 + (region.x2 - region.x1) * region.center_rotation_y + cs2 / 2 && y >= region.y1 - 35 - cs2 / 2 && y <= region.y1 - 35 + cs2 / 2;
                boolean inDropZone = region.isInRegionsPropertiesArea(x, y) || region.isInMappingIconArea(x, y) || region.isInMouseIconArea(x, y) || region.isInRegionsIconArea(x, y);
                return bInRect || inRotateZone || (inDropZone && (FileDrop.isDragging() || SketchletEditor.getInstance().isInCtrlMode()));
            } else {
                return false;
            }
        }
    }

    public Point transformPoint(int x, int y, boolean bPlayback) {
        AffineTransform af = new AffineTransform();
        if (bPlayback) {
            af.shear(region.shearX, region.shearY);
            af.rotate(region.rotation,
                    region.x1 + (region.x2 - region.x1) * region.center_rotation_x,
                    region.y1 + (region.y2 - region.y1) * region.center_rotation_y);
        } else {
            af.shear(region.shearX, region.shearY);
            af.rotate(region.rotation,
                    region.x1 + (region.x2 - region.x1) * region.center_rotation_x,
                    region.y1 + (region.y2 - region.y1) * region.center_rotation_y);
        }
        try {
            Point2D ip = af.transform(new Point(x, y), null);
            x = (int) ip.getX();
            y = (int) ip.getY();
        } catch (Throwable e) {
        }
        return new Point(x, y);
    }

    public static Point inversePoint(ActiveRegion region, int x, int y, boolean bPlayback) {
        AffineTransform af = new AffineTransform();
        if (bPlayback) {
            af.shear(region.shearX, region.shearY);
            af.rotate(region.rotation,
                    region.x1 + (region.x2 - region.x1) * region.center_rotation_x,
                    region.y1 + (region.y2 - region.y1) * region.center_rotation_y);
        } else {
            af.shear(region.shearX, region.shearY);
            af.rotate(region.rotation,
                    region.x1 + (region.x2 - region.x1) * region.center_rotation_x,
                    region.y1 + (region.y2 - region.y1) * region.center_rotation_y);
        }
        try {
            Point2D ip = af.inverseTransform(new Point(x, y), null);
            x = (int) ip.getX();
            y = (int) ip.getY();
            if (bPlayback) {
                PerspectiveFilter perspectiveFilter = new PerspectiveFilter();
                int w = region.getWidth();
                int h = region.getHeight();
                perspectiveFilter.setCorners((float) region.p_x0 * w, (float) region.p_y0 * h,
                        (float) region.p_x1 * w, (float) region.p_y1 * h,
                        (float) region.p_x2 * w, (float) region.p_y2 * h,
                        (float) region.p_x3 * w, (float) region.p_y3 * h);
                perspectiveFilter.originalSpace = new Rectangle(0, 0, w, h);
                perspectiveFilter.transformedSpace = new Rectangle(0, 0, w, h);
                perspectiveFilter.transformSpace(perspectiveFilter.transformedSpace);
                float points[] = new float[2];
                perspectiveFilter.transformInverse(x - region.x1, y - region.y1, points);
                x = region.x1 + (int) points[0];
                y = region.y1 + (int) points[1];
            }
        } catch (Throwable e) {
            log.error(e);
        }

        return new Point(x, y);
    }

    public Point inversePoint(int x, int y, boolean bPlayback) {
        return inversePoint(this.region, x, y, bPlayback);
    }

    public int getSelectedCorner() {
        return selectedCorner;
    }

    public void setSelectedCorner(int selectedCorner) {
        this.selectedCorner = selectedCorner;
    }

    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public int getStartY() {
        return startY;
    }

    public void setStartY(int startY) {
        this.startY = startY;
    }

    public int getPressedX() {
        return pressedX;
    }

    public void setPressedX(int pressedX) {
        this.pressedX = pressedX;
    }

    public int getPressedY() {
        return pressedY;
    }

    public void setPressedY(int pressedY) {
        this.pressedY = pressedY;
    }

    public double getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(double startAngle) {
        this.startAngle = startAngle;
    }

    public double getCenterRotationX() {
        return centerRotationX;
    }

    public void setCenterRotationX(double centerRotationX) {
        this.centerRotationX = centerRotationX;
    }

    public double getCenterRotationY() {
        return centerRotationY;
    }

    public void setCenterRotationY(double centerRotationY) {
        this.centerRotationY = centerRotationY;
    }

    public double getTrajectory2X() {
        return trajectory2X;
    }

    public void setTrajectory2X(double trajectory2X) {
        this.trajectory2X = trajectory2X;
    }

    public double getTrajectory2Y() {
        return trajectory2Y;
    }

    public void setTrajectory2Y(double trajectory2Y) {
        this.trajectory2Y = trajectory2Y;
    }

    public boolean isbRotating() {
        return bRotating;
    }

    public void setbRotating(boolean bRotating) {
        this.bRotating = bRotating;
    }
}
