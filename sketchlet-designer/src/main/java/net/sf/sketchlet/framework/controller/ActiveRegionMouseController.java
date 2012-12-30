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
import net.sf.sketchlet.designer.editor.dnd.DropArea;
import net.sf.sketchlet.designer.editor.dnd.DropAreas;
import net.sf.sketchlet.designer.editor.dnd.InternalDroppedString;
import net.sf.sketchlet.designer.editor.dnd.InternallyDroppedRunnable;
import net.sf.sketchlet.designer.editor.dnd.SelectDropAction;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionPanel;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionsFrame;
import net.sf.sketchlet.designer.editor.ui.timers.curve.CurvesFrame;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.designer.playback.ui.PlaybackFrame;
import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.model.TrajectoryPoint;
import net.sf.sketchlet.framework.model.events.mouse.MouseEventsProcessor;
import net.sf.sketchlet.framework.model.programming.screenscripts.AWTRobotUtil;
import net.sf.sketchlet.framework.model.programming.timers.Timer;
import net.sf.sketchlet.framework.model.programming.timers.curves.Curve;
import net.sf.sketchlet.framework.model.programming.timers.curves.Curves;
import net.sf.sketchlet.framework.renderer.DropAreasRenderer;
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
    private DropAreas dropAreas;
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
        if (!region.getParent().getPage().isRegionsLayer()) {
            return;
        }
        int marginX = bPlayback ? region.getParent().getOffsetX() : SketchletEditor.getInstance().getMarginX();
        int marginY = bPlayback ? region.getParent().getOffsetY() : SketchletEditor.getInstance().getMarginY();
        int x = (int) ((e.getPoint().x) / scale) - marginX;
        int y = (int) ((e.getPoint().y) / scale) - marginY;
        mouseMoved(x, y, scale, e, frame, bPlayback);
    }

    public void mouseMoved(int x, int y, double scale, MouseEvent e, JFrame frame, boolean bPlayback) {
        if (!region.getParent().getPage().isRegionsLayer()) {
            return;
        }
        Point ip = inversePoint(x, y, bPlayback);
        x = ip.x;
        y = ip.y;
        if (!bPlayback) {
            int w = region.getX2Value() - region.getX1Value();
            int h = region.getY2Value() - region.getY1Value();
            int cx = region.getX1Value() + w / 2;
            int cy = region.getY1Value() + h / 2;

            Cursor cursor;

            int cs2 = (int) (ActiveRegionRenderer.CORNER_SIZE / SketchletEditor.getInstance().getScale() / 2) + 1;

            if (x >= region.getX1Value() - cs2 && x <= region.getX1Value() + cs2 && y >= region.getY1Value() - cs2 && y <= region.getY1Value() + cs2) {
                cursor = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
            } else if (x >= region.getX2Value() - cs2 && x <= region.getX2Value() + cs2 && y >= region.getY1Value() - cs2 && y <= region.getY1Value() + cs2) {
                cursor = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
            } else if (x >= region.getX2Value() - cs2 && x <= region.getX2Value() + cs2 && y >= region.getY2Value() - cs2 && y <= region.getY2Value() + cs2) {
                cursor = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
            } else if (x >= region.getX1Value() - cs2 && x <= region.getX1Value() + cs2 && y >= region.getY2Value() - cs2 && y <= region.getY2Value() + cs2) {
                cursor = Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
            } else if (x >= region.getX1Value() - cs2 && x <= region.getX1Value() + cs2 && y >= cy - cs2 && y <= cy + cs2) {
                cursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
            } else if (x >= region.getX2Value() - cs2 && x <= region.getX2Value() + cs2 && y >= cy - cs2 && y <= cy + cs2) {
                cursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
            } else if (x >= cx - cs2 && x <= cx + cs2 && y >= region.getY1Value() - cs2 && y <= region.getY1Value() + cs2) {
                cursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
            } else if (x >= cx - cs2 && x <= cx + cs2 && y >= region.getY2Value() - cs2 && y <= region.getY2Value() + cs2) {
                cursor = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
            } else if (x >= cx - cs2 && x <= cx + cs2 && y >= region.getY1Value() - 35 - cs2 && y <= region.getY1Value() - 35 + cs2) {
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
        if (!region.getParent().getPage().isRegionsLayer()) {
            return;
        }
    }

    public void mousePressed(MouseEvent e, double scale, JFrame frame, boolean bPlayback) {
        if (!region.getParent().getPage().isRegionsLayer()) {
            return;
        }
        int marginX = bPlayback ? region.getParent().getOffsetX() : SketchletEditor.getInstance().getMarginX();
        int marginY = bPlayback ? region.getParent().getOffsetY() : SketchletEditor.getInstance().getMarginY();
        int x = (int) ((e.getPoint().x) / scale) - marginX;
        int y = (int) ((e.getPoint().y) / scale) - marginY;

        mousePressed(x, y, e.getModifiers(), e.getWhen(), e, frame, bPlayback);
    }

    public void mousePressed(int x, int y, int modifiers, long when, MouseEvent e, JFrame frame, boolean bPlayback) {
        if (!region.getParent().getPage().isRegionsLayer()) {
            return;
        }
        setPressedX(x);
        setPressedY(y);
        setStartX(x);
        setStartY(y);
        if (region.isInTrajectoryMode() && region.getTrajectory1().trim().isEmpty()) {
            int xCenter = region.getX1Value() + (int) ((region.getX2Value() - region.getX1Value()) * region.getCenterOfRotationX());
            int yCenter = region.getY1Value() + (int) ((region.getY2Value() - region.getY1Value()) * region.getCenterOfRotationY());
            this.startMouseXCenter = xCenter;
            this.startMouseYCenter = yCenter;
            region.setTrajectory1(region.getTrajectory1() + xCenter + " " + yCenter + " " + 0 + "\n");
        }
        if (region.isInTrajectoryMode2() && region.getTrajectory2().trim().isEmpty()) {
            int xCenter = region.getX1Value() + (int) ((region.getX2Value() - region.getX1Value()) * region.getTrajectory2X());
            int yCenter = region.getY1Value() + (int) ((region.getY2Value() - region.getY1Value()) * region.getTrajectory2Y());
            Point2D p = rotate(xCenter, yCenter);
            this.startMouseXCenter2 = (int) p.getX();
            this.startMouseYCenter2 = (int) p.getY();
            region.setTrajectory2(region.getTrajectory2() + (int) p.getX() + " " + (int) p.getY() + " " + 0 + "\n");
        }
        startTime = when;
        Point ip = inversePoint(x, y, bPlayback);
        x = ip.x;
        y = ip.y;
        if (bPlayback) {
            if ((modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                setSelectedCorner(MIDDLE);
            } else if ((modifiers & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                setSelectedCorner(ROTATE);
                startRotate(getStartX(), getStartY());
            }

            if (region.isScreenCapturingMouseMappingEnabled()) {
                try {
                    int sx = (int) Double.parseDouble(region.getCaptureScreenX());
                    int sy = (int) Double.parseDouble(region.getCaptureScreenY());
                    sx += (int) (Double.parseDouble(region.getCaptureScreenWidth()) * ((double) x - region.getX1Value()) / region.getWidthValue());
                    sy += (int) (Double.parseDouble(region.getCaptureScreenHeight()) * ((double) y - region.getY1Value()) / region.getHeightValue());
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
            aspectRatio = (double) (region.getY2Value() - region.getY1Value()) / (region.getX2Value() - region.getX1Value());
            if ((modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                if (!SketchletEditor.getInstance().isInCtrlMode()) {
                    int w = region.getX2Value() - region.getX1Value();
                    int h = region.getY2Value() - region.getY1Value();
                    int cx = region.getX1Value() + w / 2;
                    int cy = region.getY1Value() + h / 2;

                    int cs2 = (int) (ActiveRegionRenderer.CORNER_SIZE / SketchletEditor.getInstance().getScale() / 2) + 1;

                    if (x >= region.getX1Value() - cs2 && x <= region.getX1Value() + cs2 && y >= region.getY1Value() - cs2 && y <= region.getY1Value() + cs2) {
                        setSelectedCorner(UPPER_LEFT);
                    } else if (x >= region.getX2Value() - cs2 && x <= region.getX2Value() + cs2 && y >= region.getY1Value() - cs2 && y <= region.getY1Value() + cs2) {
                        setSelectedCorner(UPPER_RIGHT);
                    } else if (x >= region.getX2Value() - cs2 && x <= region.getX2Value() + cs2 && y >= region.getY2Value() - cs2 && y <= region.getY2Value() + cs2) {
                        setSelectedCorner(BOTTOM_RIGHT);
                    } else if (x >= region.getX1Value() - cs2 && x <= region.getX1Value() + cs2 && y >= region.getY2Value() - cs2 && y <= region.getY2Value() + cs2) {
                        setSelectedCorner(BOTTOM_LEFT);
                    } else if (x >= region.getX1Value() - cs2 && x <= region.getX1Value() + cs2 && y >= cy - cs2 && y <= cy + cs2) {
                        setSelectedCorner(LEFT);
                    } else if (x >= region.getX2Value() - cs2 && x <= region.getX2Value() + cs2 && y >= cy - cs2 && y <= cy + cs2) {
                        setSelectedCorner(RIGHT);
                    } else if (x >= cx - cs2 && x <= cx + cs2 && y >= region.getY1Value() - cs2 && y <= region.getY1Value() + cs2) {
                        setSelectedCorner(UPPER);
                    } else if (x >= cx - cs2 && x <= cx + cs2 && y >= region.getY2Value() - cs2 && y <= region.getY2Value() + cs2) {
                        setSelectedCorner(BOTTOM);
                    } else if (x >= cx - cs2 && x <= cx + cs2 && y >= region.getY1Value() - 35 - cs2 && y <= region.getY1Value() - 35 + cs2) {
                        setSelectedCorner(ROTATE);
                        startRotate(getStartX(), getStartY());
                    } else {
                        setSelectedCorner(MIDDLE);
                    }
                } else {
                    int w = region.getX2Value() - region.getX1Value();
                    int h = region.getY2Value() - region.getY1Value();

                    int _x0 = region.getX1Value() + (int) (w * region.getP_x0());
                    int _y0 = region.getY1Value() + (int) (h * region.getP_y0());
                    int _x1 = region.getX1Value() + (int) (w * region.getP_x1());
                    int _y1 = region.getY1Value() + (int) (h * region.getP_y1());
                    int _x2 = region.getX1Value() + (int) (w * region.getP_x2());
                    int _y2 = region.getY1Value() + (int) (h * region.getP_y2());
                    int _x3 = region.getX1Value() + (int) (w * region.getP_x3());
                    int _y3 = region.getY1Value() + (int) (h * region.getP_y3());

                    int c_x = region.getX1Value() + (int) (w * region.getCenterOfRotationX());
                    int c_y = region.getY1Value() + (int) (h * region.getCenterOfRotationY());

                    int t2_x = region.getX1Value() + (int) (w * region.getTrajectory2X());
                    int t2_y = region.getY1Value() + (int) (h * region.getTrajectory2Y());

                    int cs2 = (int) (ActiveRegionRenderer.CORNER_SIZE / SketchletEditor.getInstance().getScale() / 2) + 1;

                    if (!SketchletEditor.getInstance().isInShiftMode() && x >= c_x - cs2 && x <= c_x + cs2 && y >= c_y - cs2 && y <= c_y + cs2) {
                        setSelectedCorner(CENTER_ROTATION);
                        setCenterRotationX(region.getCenterOfRotationX());
                        setCenterRotationY(region.getCenterOfRotationY());
                    } else if (SketchletEditor.getInstance().isInShiftMode() && x >= t2_x - cs2 && x <= t2_x + cs2 && y >= t2_y - cs2 && y <= t2_y + cs2) {
                        setSelectedCorner(TRAJECTORY2_POINT);
                        this.setTrajectory2X(region.getTrajectory2X());
                        this.setTrajectory2Y(region.getTrajectory2Y());
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
        if (!region.getParent().getPage().isRegionsLayer()) {
            return;
        }
        setRotating(true);
        int xCenter = region.getX1Value() + (int) ((region.getX2Value() - region.getX1Value()) * region.getCenterOfRotationX());
        int yCenter = region.getY1Value() + (int) ((region.getY2Value() - region.getY1Value()) * region.getCenterOfRotationY());

        if (x == xCenter) {
            setStartAngle(y > yCenter ? Math.PI / 2 : -Math.PI / 2);
        } else {
            setStartAngle(Math.atan((double) (y - yCenter) / (x - xCenter)));
        }

        setStartAngle(getStartAngle() - region.getRotationValue());

        if (x < xCenter) {
            setStartAngle(getStartAngle() + Math.PI);
        }

        if (getStartAngle() < 0) {
            setStartAngle(getStartAngle() + 2 * Math.PI);
        }

    }

    public boolean mousePressedEmbedded(int x, int y, int modifiers, long when, MouseEvent e, JFrame frame, boolean bPlayback) {
        if (!region.getParent().getPage().isRegionsLayer()) {
            return false;
        }

        return false;
    }

    public void mouseReleased(MouseEvent e, double scale, JFrame frame, boolean bPlayback) {
        region.setAdjusting(false);
        if (!region.getParent().getPage().isRegionsLayer()) {
            return;
        }
        int marginX = bPlayback ? region.getParent().getOffsetX() : SketchletEditor.getInstance().getMarginX();
        int marginY = bPlayback ? region.getParent().getOffsetY() : SketchletEditor.getInstance().getMarginY();
        int x = (int) ((e.getPoint().x) / scale) - marginX;
        int y = (int) ((e.getPoint().y) / scale) - marginY;
        mouseReleased(x, y, e.getModifiers(), e.getWhen(), e, frame, bPlayback);
    }

    public void mouseReleased(int x, int y, int modifiers, long when, MouseEvent e, JFrame frame, boolean bPlayback) {
        region.setAdjusting(false);
        if (this.getSelectedCorner() == CENTER_ROTATION) {
            region.setCenterOfRotationX(getCenterRotationX());
            region.setCenterOfRotationY(getCenterRotationY());
            return;
        } else if (this.getSelectedCorner() == TRAJECTORY2_POINT) {
            region.setTrajectory2X(getTrajectory2X());
            region.setTrajectory2Y(getTrajectory2Y());

            setSelectedCorner(-1);
            return;
        }
        if (!region.getParent().getPage().isRegionsLayer()) {
            return;
        }
        if (isRotating()) {
            region.setRotationValue(Math.toRadians((int) Math.toDegrees(region.getRotationValue())));
            setRotating(false);
        }
        Point ip = inversePoint(x, y, bPlayback);
        x = ip.x;
        y = ip.y;
        if ((modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {

            int _x1 = Math.min(region.getX1Value(), region.getX2Value());
            int _x2 = Math.max(region.getX1Value(), region.getX2Value());
            int _y1 = Math.min(region.getY1Value(), region.getY2Value());
            int _y2 = Math.max(region.getY1Value(), region.getY2Value());

            if (!bPlayback && SketchletEditor.isSnapToGrid()) {
                int s = InteractionSpace.getGridSpacing();
                _x1 = ((_x1 + s / 2) / s) * s;
                _x2 = ((_x2 + s / 2) / s) * s;
                _y1 = ((_y1 + s / 2) / s) * s;
                _y2 = ((_y2 + s / 2) / s) * s;
            }

            region.setX1Value(_x1);
            region.setY1Value(_y1);
            region.setX2Value(_x2);
            region.setY2Value(_y2);

            SketchletEditor.getInstance().repaint();
        } else if ((modifiers & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
            SketchletEditor.getInstance().repaint();
        }
        region.getMotionController().processLimits("speed", 0.0, 0.0, 0.0, true);
        if (bPlayback) {
            region.getMouseEventsProcessor().processAction(e, frame, new int[]{MouseEventsProcessor.MOUSE_LEFT_BUTTON_RELEASE, MouseEventsProcessor.MOUSE_MIDDLE_BUTTON_RELEASE, MouseEventsProcessor.MOUSE_RIGHT_BUTTON_RELEASE});
        }

        if (region.isInTrajectoryMode()) {
            if (region.getTrajectoryType() < 2) {
                processTrajectory();
                region.setInTrajectoryMode(false);
            } else if (region.getTrajectoryType() == 2) {
                int xCenter = region.getX1Value() + (int) ((region.getX2Value() - region.getX1Value()) * region.getCenterOfRotationX());
                int yCenter = region.getY1Value() + (int) ((region.getY2Value() - region.getY1Value()) * region.getCenterOfRotationY());
                region.setTrajectory1(region.getTrajectory1() + xCenter + " " + yCenter + " " + 0 + "\n");
            } else if (region.getTrajectoryType() == 3) {
                processTrajectory();
                region.setInTrajectoryMode(false);
            }
            region.setTrajectory1(simplifyTrajectory(region.getTrajectory1()));
        } else if (region.isInTrajectoryMode2()) {
            if (region.getTrajectoryType() < 2) {
                processTrajectory2();
                region.setInTrajectoryMode2(false);
            } else if (region.getTrajectoryType() == 2) {
                int xCenter = region.getX1Value() + (int) ((region.getX2Value() - region.getX1Value()) * region.getTrajectory2X());
                int yCenter = region.getY1Value() + (int) ((region.getY2Value() - region.getY1Value()) * region.getTrajectory2Y());
                Point2D p = rotate(xCenter, yCenter);
                region.setTrajectory2(region.getTrajectory2() + (int) p.getX() + " " + (int) p.getY() + " " + 0 + "\n");
            } else if (region.getTrajectoryType() == 3) {
                region.setInTrajectoryMode2(false);
            }
            region.setTrajectory2(simplifyTrajectory(region.getTrajectory2()));
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
        if (!region.getParent().getPage().isRegionsLayer()) {
            return;
        }
        JPanel panel = new JPanel(new SpringLayout());
        JLabel label1 = new JLabel(Language.translate("Do you also want to create a new timer"));
        JLabel label2 = new JLabel(Language.translate("using the trajectory timing data?"));
        JCheckBox curveCheck = new JCheckBox(Language.translate("Create timer curve with geture timing"), true);
        JCheckBox timerCheck = new JCheckBox(Language.translate("Create timer"), true);
        JCheckBox controlOrientation = new JCheckBox(Language.translate("Control orientation"), region.isChangingOrientationOnTrajectoryEnabled());
        JCheckBox restartCheck = new JCheckBox(Language.translate("Restart variables on sketch entry and exit"), true);
        panel.add(label1);
        if (region.getTrajectoryType() == 0) {
            panel.add(label2);
        } else {
            label1.setText(label1.getText() + "?");
        }
        panel.add(timerCheck);
        panel.add(restartCheck);
        panel.add(controlOrientation);
        if (region.getTrajectoryType() == 0) {
            panel.add(curveCheck);
        } else {
            curveCheck.setSelected(false);
        }
        if (region.getTrajectoryType() == 0) {
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
                    if (region.getTrajectoryType() == 0) {
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

                region.setChangingOrientationOnTrajectoryEnabled(controlOrientation.isSelected());

                region.setTrajectoryPosition("=" + newVariable);
                if (t != null && region.getTrajectoryType() == 0) {
                    t.setStrDurationInSec("" + (totalDuration / 1000.0));
                }
            }
        }
        region.setInTrajectoryMode(false);
    }

    public void processTrajectory2() {
        region.setInTrajectoryMode2(false);
    }

    public boolean mouseReleasedEmbedded(int x, int y, int modifiers, long when, MouseEvent e, JFrame frame, boolean bPlayback) {
        if (!region.getParent().getPage().isRegionsLayer()) {
            return false;
        }

        return false;
    }

    public void mouseDragged(MouseEvent e, double scale, JFrame frame, boolean bPlayback) {
        if (!region.getParent().getPage().isRegionsLayer()) {
            return;
        }
        int marginX = bPlayback ? region.getParent().getOffsetX() : SketchletEditor.getInstance().getMarginX();
        int marginY = bPlayback ? region.getParent().getOffsetY() : SketchletEditor.getInstance().getMarginY();
        int x = (int) ((e.getPoint().x) / scale) - marginX;
        int y = (int) ((e.getPoint().y) / scale) - marginY;
        mouseDragged(x, y, e.getModifiers(), e.getWhen(), scale, e, frame, bPlayback);
    }

    public synchronized void mouseDragged(int x, int y, int modifiers, long when, double scale, MouseEvent e, JFrame frame, boolean playbackMode) {
        if (!region.getParent().getPage().isRegionsLayer()) {
            return;
        }
        if (!playbackMode && region.isPinned()) {
            return;
        }
        region.setAdjusting(true);
        int prevX1 = region.getX1Value();
        int prevY1 = region.getY1Value();
        int prevX2 = region.getX2Value();
        int prevY2 = region.getY2Value();

        int _x = x, _y = y;

        boolean moved = true;

        if (playbackMode && (!region.getWidget().isEmpty())) {
            moved = false;
            region.getRenderer().getWidgetImageLayer().mouseDragged(e, x, y);
        }
        if (!e.isConsumed()) {
            if ((!playbackMode || region.isMovable()) && (modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                if (getSelectedCorner() != MIDDLE) {
                    if (playbackMode) {
                        return;
                    }
                    Point ip = inversePoint(x, y, playbackMode);
                    x = ip.x;
                    y = ip.y;
                }
                int w = region.getX2Value() - region.getX1Value();
                int h = region.getY2Value() - region.getY1Value();
                switch (getSelectedCorner()) {
                    case MIDDLE:
                        int dx = x - getStartX();
                        int dy = y - getStartY();
                        region.setX1Value(region.getX1Value() + dx);
                        region.setY1Value(region.getY1Value() + dy);
                        region.setX2Value(region.getX2Value() + dx);
                        region.setY2Value(region.getY2Value() + dy);

                        break;
                    case UPPER_LEFT:
                        if (!SketchletEditor.getInstance().isInShiftMode()) {
                            dx = x - region.getX1Value();
                            dy = y - region.getY1Value();
                            if (!region.getWidth().startsWith("=")) {
                                region.setX1Value(x);
                                if (region.getRotationValue() != 0) {
                                    region.setX2Value(region.getX2Value() - dx);
                                }
                            }
                            if (!region.getHeight().startsWith("=")) {
                                region.setY1Value(y);
                                if (region.getRotationValue() != 0) {
                                    region.setY2Value(region.getY2Value() - dy);
                                }
                            }
                        } else {
                            dx = x - region.getX1Value();
                            if (!region.getWidth().startsWith("=")) {
                                region.setX1Value(x);
                                if (region.getRotationValue() != 0) {
                                    region.setX2Value(region.getX2Value() - dx);
                                }
                            }
                            dy = region.getY2Value() - (int) ((region.getX2Value() - region.getX1Value()) * aspectRatio) - region.getY2Value();
                            if (!region.getHeight().startsWith("=")) {
                                region.setY1Value(region.getY2Value() - (int) ((region.getX2Value() - region.getX1Value()) * aspectRatio));
                                if (region.getRotationValue() != 0) {
                                    region.setY2Value(region.getY2Value() - dy);
                                }
                            }
                        }
                        break;
                    case UPPER_RIGHT:
                        if (!SketchletEditor.getInstance().isInShiftMode()) {
                            dx = x - region.getX2Value();
                            dy = y - region.getY1Value();
                            if (!region.getWidth().startsWith("=")) {
                                region.setX2Value(x);
                                if (region.getRotationValue() != 0) {
                                    region.setX1Value(region.getX1Value() - dx);
                                }
                            }
                            if (!region.getHeight().startsWith("=")) {
                                region.setY1Value(y);
                                if (region.getRotationValue() != 0) {
                                    region.setY2Value(region.getY2Value() - dy);
                                }
                            }
                        } else {
                            dx = x - region.getX1Value();
                            if (!region.getWidth().startsWith("=")) {
                                region.setX2Value(x);
                                if (region.getRotationValue() != 0) {
                                    region.setX1Value(region.getX1Value() - dx);
                                }
                            }
                            dy = region.getY2Value() - (int) ((region.getX2Value() - region.getX1Value()) * aspectRatio) - region.getY1Value();
                            if (!region.getHeight().startsWith("=")) {
                                region.setY1Value(region.getY2Value() - (int) ((region.getX2Value() - region.getX1Value()) * aspectRatio));
                                if (region.getRotationValue() != 0) {
                                    region.setY2Value(region.getY2Value() - dy);
                                }
                            }
                        }
                        break;
                    case BOTTOM_LEFT:
                        if (!SketchletEditor.getInstance().isInShiftMode()) {
                            dx = x - region.getX1Value();
                            dy = y - region.getY2Value();
                            if (!region.getWidth().startsWith("=")) {
                                region.setX1Value(x);
                                if (region.getRotationValue() != 0) {
                                    region.setX2Value(region.getX2Value() - dx);
                                }
                            }
                            if (!region.getHeight().startsWith("=")) {
                                region.setY2Value(y);
                                if (region.getRotationValue() != 0) {
                                    region.setY1Value(region.getY1Value() - dy);
                                }
                            }
                        } else {
                            dx = x - region.getX1Value();
                            if (!region.getWidth().startsWith("=")) {
                                region.setX1Value(x);
                                if (region.getRotationValue() != 0) {
                                    region.setX2Value(region.getX2Value() - dx);
                                }
                            }
                            dy = region.getY1Value() + (int) ((region.getX2Value() - region.getX1Value()) * aspectRatio) - region.getY2Value();
                            if (!region.getHeight().startsWith("=")) {
                                region.setY2Value(region.getY1Value() + (int) ((region.getX2Value() - region.getX1Value()) * aspectRatio));
                                if (region.getRotationValue() != 0) {
                                    region.setY1Value(region.getY1Value() - dy);
                                }
                            }
                        }
                        break;
                    case BOTTOM_RIGHT:
                        if (!SketchletEditor.getInstance().isInShiftMode()) {
                            dx = x - region.getX2Value();
                            dy = y - region.getY2Value();
                            if (!region.getWidth().startsWith("=")) {
                                region.setX2Value(x);
                                if (region.getRotationValue() != 0) {
                                    region.setX1Value(region.getX1Value() - dx);
                                }
                            }
                            if (!region.getHeight().startsWith("=")) {
                                region.setY2Value(y);
                                if (region.getRotationValue() != 0) {
                                    region.setY1Value(region.getY1Value() - dy);
                                }
                            }
                        } else {
                            dx = x - region.getX2Value();
                            if (!region.getWidth().startsWith("=")) {
                                region.setX2Value(x);
                                if (region.getRotationValue() != 0) {
                                    region.setX1Value(region.getX1Value() - dx);
                                }
                            }
                            dy = region.getY1Value() + (int) ((region.getX2Value() - region.getX1Value()) * aspectRatio) - region.getY2Value();
                            if (!region.getHeight().startsWith("=")) {
                                region.setY2Value(region.getY1Value() + (int) ((region.getX2Value() - region.getX1Value()) * aspectRatio));
                                if (region.getRotationValue() != 0) {
                                    region.setY1Value(region.getY1Value() - dy);
                                }
                            }
                        }
                        break;
                    case UPPER:
                        if (!region.getHeight().startsWith("=")) {
                            dy = y - region.getY1Value();
                            region.setY1Value(y);
                            if (region.getRotationValue() != 0) {
                                region.setY2Value(region.getY2Value() - dy);
                            }
                        }
                        break;
                    case BOTTOM:
                        if (!region.getHeight().startsWith("=")) {
                            dy = y - region.getY2Value();
                            region.setY2Value(y);
                            if (region.getRotationValue() != 0) {
                                region.setY1Value(region.getY1Value() - dy);
                            }
                        }
                        break;
                    case LEFT:
                        if (!region.getWidth().startsWith("=")) {
                            dx = x - region.getX1Value();
                            region.setX1Value(x);
                            if (region.getRotationValue() != 0) {
                                region.setX2Value(region.getX2Value() - dx);
                            }
                        }
                        break;
                    case RIGHT:
                        if (!region.getWidth().startsWith("=")) {
                            dx = x - region.getX2Value();
                            region.setX2Value(x);
                            if (region.getRotationValue() != 0) {
                                region.setX1Value(region.getX1Value() - dx);
                            }
                        }
                        break;
                    case PERSPECTIVE_UPPER_LEFT:
                        region.setP_x0((double) (x - region.getX1Value()) / w);
                        region.setP_y0((double) (y - region.getY1Value()) / h);

                        region.setP_x0(Math.max(0.0, region.getP_x0()));
                        region.setP_x0(Math.min(1.0, region.getP_x0()));
                        region.setP_y0(Math.max(0.0, region.getP_y0()));
                        region.setP_y0(Math.min(1.0, region.getP_y0()));
                        break;
                    case CENTER_ROTATION:
                        setCenterRotationX((double) (x - region.getX1Value()) / w);
                        setCenterRotationY((double) (y - region.getY1Value()) / h);

                        setCenterRotationX(Math.max(0.0, getCenterRotationX()));
                        setCenterRotationX(Math.min(1.0, getCenterRotationX()));
                        setCenterRotationY(Math.max(0.0, getCenterRotationY()));
                        setCenterRotationY(Math.min(1.0, getCenterRotationY()));
                        break;
                    case TRAJECTORY2_POINT:
                        setTrajectory2X((double) (x - region.getX1Value()) / w);
                        setTrajectory2Y((double) (y - region.getY1Value()) / h);

                        setTrajectory2X(Math.max(0.0, getTrajectory2X()));
                        setTrajectory2X(Math.min(1.0, getTrajectory2X()));
                        setTrajectory2Y(Math.max(0.0, getTrajectory2Y()));
                        setTrajectory2Y(Math.min(1.0, getTrajectory2Y()));

                        break;
                    case PERSPECTIVE_UPPER_RIGHT:
                        region.setP_x1((double) (x - region.getX1Value()) / w);
                        region.setP_y1((double) (y - region.getY1Value()) / h);
                        region.setP_x1(Math.max(0.0, region.getP_x1()));
                        region.setP_x1(Math.min(1.0, region.getP_x1()));
                        region.setP_y1(Math.max(0.0, region.getP_y1()));
                        region.setP_y1(Math.min(1.0, region.getP_y1()));
                        break;
                    case PERSPECTIVE_BOTTOM_RIGHT:
                        region.setP_x2((double) (x - region.getX1Value()) / w);
                        region.setP_y2((double) (y - region.getY1Value()) / h);
                        region.setP_x2(Math.max(0.0, region.getP_x2()));
                        region.setP_x2(Math.min(1.0, region.getP_x2()));
                        region.setP_y2(Math.max(0.0, region.getP_y2()));
                        region.setP_y2(Math.min(1.0, region.getP_y2()));
                        break;
                    case PERSPECTIVE_BOTTOM_LEFT:
                        region.setP_x3((double) (x - region.getX1Value()) / w);
                        region.setP_y3((double) (y - region.getY1Value()) / h);
                        region.setP_x3(Math.max(0.0, region.getP_x3()));
                        region.setP_x3(Math.min(1.0, region.getP_x3()));
                        region.setP_y3(Math.max(0.0, region.getP_y3()));
                        region.setP_y3(Math.min(1.0, region.getP_y3()));
                        break;
                    case ROTATE:
                        rotate(_x, _y, playbackMode);
                        break;
                }

                if (getSelectedCorner() != ROTATE && getSelectedCorner() != TRAJECTORY2_POINT && getSelectedCorner() != CENTER_ROTATION) {
                    setStartX(x);
                    setStartY(y);

                    region.getInteractionController().processInteractionEvents(playbackMode, region.getParent().getPage().getActiveTimers(), region.getParent().getPage().getActiveMacros());
                    region.getSketch().updateConnectors(region, playbackMode);

                    if (!region.isWithinLimits(false) || region.getInteractionController().intersectsWithSolids(playbackMode)) {
                        region.getParent().getOverlapHelper().findNonOverlappingLocation(region);
                        return;
                    }

                    if (!playbackMode) {
                        if (region.getParent() != null && region.getParent().getMouseHelper().getSelectedRegions() != null) {
                            for (ActiveRegion as : region.getParent().getMouseHelper().getSelectedRegions()) {
                                if (as != region) {
                                    as.setX1Value(as.getX1Value() + region.getX1Value() - prevX1);
                                    as.setY1Value(as.getY1Value() + region.getY1Value() - prevY1);
                                    as.setX2Value(as.getX2Value() + region.getX2Value() - prevX2);
                                    as.setY2Value(as.getY2Value() + region.getY2Value() - prevY2);
                                }
                            }
                        }
                    }
                    if (region.getHorizontalAlignment().equalsIgnoreCase("center")) {
                        region.getMotionController().processLimits("position x", region.getX1Value() + w / 2, w / 2, w / 2, true);
                    } else if (region.getHorizontalAlignment().equalsIgnoreCase("right")) {
                        region.getMotionController().processLimits("position x", region.getX1Value() + w, w, 0, true);
                    } else {
                        region.getMotionController().processLimits("position x", region.getX1Value(), 0, w, true);
                    }
                    if (region.getVerticalAlignment().equalsIgnoreCase("center")) {
                        region.getMotionController().processLimits("position y", region.getY1Value() + h / 2, h / 2, h / 2, true);
                    } else if (region.getVerticalAlignment().equalsIgnoreCase("right")) {
                        region.getMotionController().processLimits("position y", region.getY1Value() + h, h, 0, true);
                    } else {
                        region.getMotionController().processLimits("position y", region.getY1Value(), 0, h, true);
                    }

                    if (region.isInTrajectoryMode()) {
                        int xCenter = region.getX1Value() + (int) ((region.getX2Value() - region.getX1Value()) * region.getCenterOfRotationX());
                        int yCenter = region.getY1Value() + (int) ((region.getY2Value() - region.getY1Value()) * region.getCenterOfRotationY());
                        if (region.getTrajectoryType() == 0) {
                            region.setTrajectory1(region.getTrajectory1() + xCenter + " " + yCenter + " " + (when - startTime) + "\n");
                        } else if (region.getTrajectoryType() == 1) {
                            region.setTrajectory1(startMouseXCenter + " " + startMouseYCenter + " " + 0 + "\n");
                            region.setTrajectory1(region.getTrajectory1() + xCenter + " " + yCenter + " " + (when - startTime) + "\n");
                        } else if (region.getTrajectoryType() == 2) {
                        } else if (region.getTrajectoryType() == 3) {
                            int xEnd = region.getX1Value() + (int) ((region.getX2Value() - region.getX1Value()) * region.getCenterOfRotationX());
                            int yEnd = region.getY1Value() + (int) ((region.getY2Value() - region.getY1Value()) * region.getCenterOfRotationY());

                            int __x1 = Math.min(getPressedX(), xEnd);
                            int __x2 = Math.max(getPressedX(), xEnd);
                            int __y1 = Math.min(getPressedY(), yEnd);
                            int __y2 = Math.max(getPressedY(), yEnd);

                            int _w = __x2 - __x1;
                            int _h = __y2 - __y1;
                            int _cx = __x1 + _w / 2;
                            int _cy = __y1 + _h / 2;

                            region.setTrajectory1("");
                            for (int i = 0; i < 360; i += 10) {
                                double __x = _cx + Math.cos(Math.toRadians(i)) * _w / 2;
                                double __y;
                                if (SketchletEditor.getInstance().isInShiftMode()) {
                                    __y = _cy + Math.sin(Math.toRadians(i)) * _w / 2;
                                } else {
                                    __y = _cy + Math.sin(Math.toRadians(i)) * _h / 2;
                                }
                                region.setTrajectory1(region.getTrajectory1() + (int) __x + " " + (int) __y + " " + 0 + "\n");
                            }
                        }
                    } else if (region.isInTrajectoryMode2()) {
                        int xCenter2 = region.getX1Value() + (int) ((region.getX2Value() - region.getX1Value()) * region.getTrajectory2X());
                        int yCenter2 = region.getY1Value() + (int) ((region.getY2Value() - region.getY1Value()) * region.getTrajectory2Y());
                        Point2D p = rotate(xCenter2, yCenter2);
                        xCenter2 = (int) p.getX();
                        yCenter2 = (int) p.getY();
                        if (region.getTrajectoryType() == 0) {
                            region.setTrajectory2(region.getTrajectory2() + xCenter2 + " " + yCenter2 + " " + (when - startTime) + "\n");
                        } else if (region.getTrajectoryType() == 1) {
                            region.setTrajectory2(startMouseXCenter2 + " " + startMouseYCenter2 + " " + 0 + "\n");
                            region.setTrajectory2(region.getTrajectory2() + xCenter2 + " " + yCenter2 + " " + (when - startTime) + "\n");
                        } else if (region.getTrajectoryType() == 2) {
                        } else if (region.getTrajectoryType() == 3) {
                            int xEnd = region.getX1Value() + (int) ((region.getX2Value() - region.getX1Value()) * region.getCenterOfRotationX());
                            int yEnd = region.getY1Value() + (int) ((region.getY2Value() - region.getY1Value()) * region.getCenterOfRotationY());

                            int __x1 = Math.min(getPressedX(), xEnd);
                            int __x2 = Math.max(getPressedX(), xEnd);
                            int __y1 = Math.min(getPressedY(), yEnd);
                            int __y2 = Math.max(getPressedY(), yEnd);

                            int _w = __x2 - __x1;
                            int _h = __y2 - __y1;
                            int _cx = __x1 + _w / 2;
                            int _cy = __y1 + _h / 2;

                            region.setTrajectory2("");
                            for (int i = 0; i < 360; i += 10) {
                                double __x = _cx + Math.cos(Math.toRadians(i)) * _w / 2;
                                double __y;
                                if (SketchletEditor.getInstance().isInShiftMode()) {
                                    __y = _cy + Math.sin(Math.toRadians(i)) * _w / 2;
                                } else {
                                    __y = _cy + Math.sin(Math.toRadians(i)) * _h / 2;
                                }
                                region.setTrajectory2(region.getTrajectory2() + (int) __x + " " + (int) __y + " " + 0 + "\n");
                            }
                        }
                    } else {
                        if (!region.getTrajectory1().trim().equals("")) {
                            if (region.isStickToTrajectoryEnabled()) {
                                Point p = region.getRenderer().getTrajectoryDrawingLayer().getClosestTrajectoryPoint(new Point(x, y));

                                region.setX1Value((int) (p.x - region.getCenterOfRotationX() * w));
                                region.setY1Value((int) (p.y - region.getCenterOfRotationY() * h));
                                region.setX2Value(region.getX1Value() + w);
                                region.setY2Value(region.getY1Value() + h);

                                region.getMotionController().processLimits("trajectory position", region.getRenderer().getTrajectoryDrawingLayer().trajectoryPositionFromPoint, 0.0, 1.0, 0.0, 0.0, true);
                                region.getMotionController().processLimits("trajectory position 2", region.getRenderer().getTrajectoryDrawingLayer().trajectoryPositionFromPoint2, 0.0, 1.0, 0.0, 0.0, true);
                            }

                            if (!region.isWithinLimits(false) || region.getInteractionController().intersectsWithSolids(playbackMode)) {
                                region.getParent().getOverlapHelper().findNonOverlappingLocation(region);
                                return;
                            }

                            if (region.isChangingOrientationOnTrajectoryEnabled()) {
                                region.setRotationValue(region.getRenderer().getTrajectoryDrawingLayer().trajectoryOrientationFromPoint);
                            }
                        }
                    }
                }
            } else if ((!playbackMode || region.isRotatable()) && (modifiers & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                int marginX = playbackMode ? region.getParent().getOffsetX() : SketchletEditor.getInstance().getMarginX();
                int marginY = playbackMode ? region.getParent().getOffsetY() : SketchletEditor.getInstance().getMarginY();
                int __x = (int) ((e.getPoint().x) / scale) - marginX;
                int __y = (int) ((e.getPoint().y) / scale) - marginY;
                rotate(__x, __y, playbackMode);
                moved = false;
            } else {
                Point ip = inversePoint(_x, _y, playbackMode);
                _x = ip.x;
                _y = ip.y;
                mouseDragEmbedded(_x, _y, modifiers, when, e, frame, playbackMode);
                moved = false;
            }
        }

        if (moved && playbackMode) {
            int dx = region.getX1Value() - prevX1;
            int dy = region.getY1Value() - prevY1;
            if (dx != 0 || dy != 0) {
                if (!region.getRegionGrouping().equals("")) {
                    for (ActiveRegion as : region.getParent().getRegions()) {
                        if (as != region && as.getRegionGrouping().equals(region.getRegionGrouping())) {
                            as.setX1Value(as.getX1Value() + dx);
                            as.setY1Value(as.getY1Value() + dy);
                            as.setX2Value(as.getX2Value() + dx);
                            as.setY2Value(as.getY2Value() + dy);
                            as.getMotionController().processLimits("position x", as.getX1Value(), 0, 0, true);
                            as.getMotionController().processLimits("position y", as.getY1Value(), 0, 0, true);
                        }
                    }
                }
            }
        }

        if (playbackMode) {
            PlaybackFrame.repaintAllFrames();
        } else {
            SketchletEditor.getInstance().repaint();
        }
    }

    public Point2D rotate(int x, int y) {
        int xCenter = region.getX1Value() + (int) ((region.getX2Value() - region.getX1Value()) * region.getCenterOfRotationX());
        int yCenter = region.getY1Value() + (int) ((region.getY2Value() - region.getY1Value()) * region.getCenterOfRotationY());
        AffineTransform transformer = AffineTransform.getRotateInstance(region.getRotationValue(), xCenter, yCenter);
        Point2D before = new Point2D.Double(x, y);
        Point2D after = new Point2D.Double();
        after = transformer.transform(before, after);
        return after;
    }

    public boolean correctPosition(int prevX1, int prevY1, int prevX2, int prevY2) {
        int _x1 = region.getX1Value();
        int _x2 = region.getX2Value();

        region.setX1Value(prevX1);
        region.setX2Value(prevX2);
        if (region.isWithinLimits(false)) {
            return true;
        }
        region.setX1Value(_x1);
        region.setX2Value(_x2);
        region.setY1Value(prevY1);
        region.setY2Value(prevY2);
        if (region.isWithinLimits(false)) {
            return true;
        }

        return false;
    }

    private boolean rotating = false;

    public void rotate(int x, int y, boolean playbackMode) {
        double prevRotation = region.getRotationValue();
        int xCenter = region.getX1Value() + (int) ((region.getX2Value() - region.getX1Value()) * region.getCenterOfRotationX());
        int yCenter = region.getY1Value() + (int) ((region.getY2Value() - region.getY1Value()) * region.getCenterOfRotationY());

        if (x == xCenter) {
            region.setRotationValue(y > yCenter ? Math.PI / 2 : -Math.PI / 2);
        } else {
            region.setRotationValue(Math.atan((double) (y - yCenter) / (x - xCenter)));
        }

        if (x < xCenter) {
            region.setRotationValue(region.getRotationValue() + Math.PI);
        }

        region.setRotationValue(region.getRotationValue() - getStartAngle());
        region.setRotationValue(InteractionSpace.toRadians(region.getMotionController().processLimits("rotation", InteractionSpace.toPhysicalAngle(region.getRotationValue()), 0.0, 0, true)));
        region.getInteractionController().processInteractionEvents(playbackMode, region.getParent().getPage().getActiveTimers(), region.getParent().getPage().getActiveMacros());
        region.getSketch().updateConnectors(region, playbackMode);

        if (!region.isWithinLimits(false) || region.getInteractionController().intersectsWithSolids(playbackMode)) {
            region.setRotationValue(prevRotation);
            region.getParent().getOverlapHelper().findNonOverlappingLocation(region);
            return;
        }

    }

    public static Rectangle getResizedRect(ActiveRegion region, double angle, int x1, int y1, int x2, int y2, int new_x1, int new_y1, int new_x2, int new_y2) {
        double oldCenterX = x1 + (x2 - x1) * region.getCenterOfRotationX();
        double oldCenterY = y1 + (y2 - y1) * region.getCenterOfRotationY();
        double newCenterX = new_x1 + (new_x2 - new_x1) * region.getCenterOfRotationX();
        double newCenterY = new_y1 + (new_y2 - new_y1) * region.getCenterOfRotationY();

        AffineTransform aft1 = new AffineTransform();
        aft1.rotate(angle, oldCenterX, oldCenterY);
        aft1.rotate(-angle, newCenterX, newCenterY);

        Point2D pxy1 = aft1.transform(new Point(new_x1, new_y1), null);
        Point2D pxy2 = aft1.transform(new Point(new_x2, new_y2), null);

        return new Rectangle((int) pxy1.getX(), (int) pxy1.getY(), (int) (pxy2.getX() - pxy1.getX()), (int) (pxy2.getY() - pxy1.getY()));
    }

    public boolean mouseDragEmbedded(int x, int y, int modifiers, long when, MouseEvent e, JFrame frame, boolean bPlayback) {
        if (!region.getParent().getPage().isRegionsLayer()) {
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
            int w = Math.abs(region.getX2Value() - region.getX1Value());
            int h = Math.abs(region.getY2Value() - region.getY1Value());
            p.addPoint(region.getX1Value() + (int) (region.getP_x0() * w), region.getY1Value() + (int) (region.getP_y0() * h));
            p.addPoint(region.getX1Value() + (int) (region.getP_x1() * w), region.getY1Value() + (int) (region.getP_y1() * h));
            p.addPoint(region.getX1Value() + (int) (region.getP_x2() * w), region.getY1Value() + (int) (region.getP_y2() * h));
            p.addPoint(region.getX1Value() + (int) (region.getP_x3() * w), region.getY1Value() + (int) (region.getP_y3() * h));
            return region.getArea(true).contains(x, y);
        } else {
            if (SketchletEditor.getInstance() != null) {
                int cs2 = (int) (ActiveRegionRenderer.CORNER_SIZE / SketchletEditor.getInstance().getScale() / 2) + 1;

                boolean bInRect = x >= region.getX1Value() - cs2 && x <= region.getX2Value() + cs2 / 2 && y >= region.getY1Value() - cs2 / 2 && y <= region.getY2Value() + cs2 / 2;
                boolean inRotateZone = x >= region.getX1Value() + (region.getX2Value() - region.getX1Value()) * region.getCenterOfRotationX() - cs2 / 2 && x <= region.getX2Value() + (region.getX2Value() - region.getX1Value()) * region.getCenterOfRotationY() + cs2 / 2 && y >= region.getY1Value() - 35 - cs2 / 2 && y <= region.getY1Value() - 35 + cs2 / 2;
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
            af.shear(region.getShearXValue(), region.getShearYValue());
            af.rotate(region.getRotationValue(),
                    region.getX1Value() + (region.getX2Value() - region.getX1Value()) * region.getCenterOfRotationX(),
                    region.getY1Value() + (region.getY2Value() - region.getY1Value()) * region.getCenterOfRotationY());
        } else {
            af.shear(region.getShearXValue(), region.getShearYValue());
            af.rotate(region.getRotationValue(),
                    region.getX1Value() + (region.getX2Value() - region.getX1Value()) * region.getCenterOfRotationX(),
                    region.getY1Value() + (region.getY2Value() - region.getY1Value()) * region.getCenterOfRotationY());
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
            af.shear(region.getShearXValue(), region.getShearYValue());
            af.rotate(region.getRotationValue(),
                    region.getX1Value() + (region.getX2Value() - region.getX1Value()) * region.getCenterOfRotationX(),
                    region.getY1Value() + (region.getY2Value() - region.getY1Value()) * region.getCenterOfRotationY());
        } else {
            af.shear(region.getShearXValue(), region.getShearYValue());
            af.rotate(region.getRotationValue(),
                    region.getX1Value() + (region.getX2Value() - region.getX1Value()) * region.getCenterOfRotationX(),
                    region.getY1Value() + (region.getY2Value() - region.getY1Value()) * region.getCenterOfRotationY());
        }
        try {
            Point2D ip = af.inverseTransform(new Point(x, y), null);
            x = (int) ip.getX();
            y = (int) ip.getY();
            if (bPlayback) {
                PerspectiveFilter perspectiveFilter = new PerspectiveFilter();
                int w = region.getWidthValue();
                int h = region.getHeightValue();
                perspectiveFilter.setCorners((float) region.getP_x0() * w, (float) region.getP_y0() * h,
                        (float) region.getP_x1() * w, (float) region.getP_y1() * h,
                        (float) region.getP_x2() * w, (float) region.getP_y2() * h,
                        (float) region.getP_x3() * w, (float) region.getP_y3() * h);
                perspectiveFilter.originalSpace = new Rectangle(0, 0, w, h);
                perspectiveFilter.transformedSpace = new Rectangle(0, 0, w, h);
                perspectiveFilter.transformSpace(perspectiveFilter.transformedSpace);
                float points[] = new float[2];
                perspectiveFilter.transformInverse(x - region.getX1Value(), y - region.getY1Value(), points);
                x = region.getX1Value() + (int) points[0];
                y = region.getY1Value() + (int) points[1];
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

    public boolean isRotating() {
        return rotating;
    }

    public void setRotating(boolean rotating) {
        this.rotating = rotating;
    }

    public DropAreas getDropAreas() {
        if (dropAreas == null) {
            dropAreas = new DropAreas(DropAreas.Orientation.HORIZONTAL);
            dropAreas.addDropArea(getNewMouseEventDropArea());
            dropAreas.addDropArea(getNewKeyboardEventDropArea());
            dropAreas.addDropArea(getNewOverlapEventDropArea());
            dropAreas.addDropArea(getNewMoveAndRotateEventDropArea());
            dropAreas.addDropArea(getNewPropertiesDropArea());
        }
        return dropAreas;
    }

    private DropArea getNewMouseEventDropArea() {
        InternallyDroppedRunnable runnable = new InternallyDroppedRunnable() {
            @Override
            public void run(InternalDroppedString info) {
                ActiveRegionsFrame.showRegionsAndActions();
                ActiveRegionsFrame.refresh(region, ActiveRegionPanel.getIndexEvents(), ActiveRegionPanel.getIndexMouseEvents());
                ActiveRegionPanel.getCurrentActiveRegionPanel().getMouseEventPanel().addNewEventMacro(info.getAction(), info.getParam1(), info.getParam2());
            }
        };
        return new DropArea(DropAreasRenderer.MOUSE_ICON, "on mouse events (click, press, release, double click...)", 24, 24, "active_region_mouse", runnable);
    }

    private DropArea getNewKeyboardEventDropArea() {
        InternallyDroppedRunnable runnable = new InternallyDroppedRunnable() {
            @Override
            public void run(InternalDroppedString info) {
                ActiveRegionsFrame.showRegionsAndActions();
                ActiveRegionsFrame.refresh(region, ActiveRegionPanel.getIndexEvents(), ActiveRegionPanel.getIndexKeyboardEvents());
                ActiveRegionPanel.getCurrentActiveRegionPanel().getKeyboardEventsPanel().addNewEventMacro(info.getAction(), info.getParam1(), info.getParam2());
            }
        };
        return new DropArea(DropAreasRenderer.KEYBOARD_ICON, "on keyboard event", 24, 24, "active_region_keyboard", runnable);
    }

    private DropArea getNewOverlapEventDropArea() {
        InternallyDroppedRunnable runnable = new InternallyDroppedRunnable() {
            @Override
            public void run(InternalDroppedString info) {
                ActiveRegionsFrame.showRegionsAndActions();
                ActiveRegionsFrame.refresh(region, ActiveRegionPanel.getIndexEvents(), ActiveRegionPanel.getIndexOverlap());
                ActiveRegionPanel.getCurrentActiveRegionPanel().getRegionOverlapEventsPanel().addNewRegionOverlapMacro(info.getAction(), info.getParam1(), info.getParam2());
            }
        };
        return new DropArea(DropAreasRenderer.REGION_OVERLAP_ICON, "on region overlap event", 24, 24, "active_region_overlap", runnable);
    }

    private DropArea getNewMoveAndRotateEventDropArea() {
        InternallyDroppedRunnable
                runnable = new InternallyDroppedRunnable() {
            @Override
            public void run(InternalDroppedString info) {
                ActiveRegionsFrame.showRegionsAndActions();
                ActiveRegionPanel ap = ActiveRegionsFrame.refresh(region, ActiveRegionPanel.getIndexEvents(), ActiveRegionPanel.getIndexMotion());
                int row = ap.getFreeMappingRow();
                region.getMotionAndRotationVariablesMapping()[row][1] = info.getPastedText().substring(1);
                ap.editUpdateTransformationsEvent(row);
            }
        };
        DropArea dropArea = new DropArea(DropAreasRenderer.MOVE_ROTATE_ICON, "on move or rotate", 24, 24, "active_region_move", runnable);
        dropArea.setAcceptMacrosEnabled(false);
        dropArea.setAcceptTimersEnabled(false);
        dropArea.setAcceptPagesEnabled(false);

        return dropArea;
    }

    private DropArea getNewPropertiesDropArea() {
        InternallyDroppedRunnable
                runnable = new InternallyDroppedRunnable() {
            @Override
            public void run(InternalDroppedString info) {
                new SelectDropAction(SketchletEditor.getInstance().editorFrame, info.getPastedText(), region);
            }
        };
        DropArea dropArea = new DropArea(DropAreasRenderer.PROPERTIES_ICON, "set property", 24, 24, "active_region_general", runnable);
        dropArea.setAcceptMacrosEnabled(false);
        dropArea.setAcceptTimersEnabled(false);
        dropArea.setAcceptPagesEnabled(false);
        return dropArea;
    }
}
