/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.tool;

import net.sf.sketchlet.common.geom.DistancePointSegment;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.data.TrajectoryPoint;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.tools.log.ActivityLog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class TrajectoryMoveTool extends TrajectoryPointsTool {

    Cursor cursor;
    Vector<TrajectoryPoint> selectedTrajectory;

    public TrajectoryMoveTool(SketchletEditor freeHand) {
        super(freeHand);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Point hotSpot = new Point(10, 4);
        Image cursorImage = Workspace.createImageIcon("resources/cursor_move_hand.png").getImage();
        cursor = toolkit.createCustomCursor(cursorImage, hotSpot, "Move Trajectories");
        super.bDrawBorders = true;
    }

    public String getName() {
        return Language.translate("Trajectory Move Tool");
    }

    public ImageIcon getIcon() {
        return Workspace.createImageIcon("resources/select.png");
    }

    public String getIconFileName() {
        return "select.png";
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void draw(Graphics2D g2) {
        super.bDraw1 = selectedTrajectory == this.tps1;
        super.bDraw2 = selectedTrajectory == this.tps2;
        super.draw(g2);
    }

    public void mouseMoved(int x, int y, int modifiers) {
    }

    public void mouseReleased(int x, int y, int modifiers) {
        if ((modifiers & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
            rotateTrajectory(selectedTrajectory, angle);
            saveTrajectory();
            toolInterface.repaintImage();
            angle = 0.0;
        }
        ActivityLog.log("toolResult", "Move the trajectory", "select.png", toolInterface.getPanel());
        TutorialPanel.addLine("cmd", "Move the trajectory", "", toolInterface.getPanel());
    }

    int startX = -1;
    int startY = -1;

    public void mousePressed(int x, int y, int modifiers) {
        selectedTrajectory = null;
        startX = x;
        startY = y;
        ActiveRegion reg = this.getRegion();
        if (reg != null && tps1 != null && tps2 != null) {
            SketchletEditor.editorPanel.saveRegionUndo();
            if (tps1 == null) {
                tps1 = reg.createTrajectoryVector();
            }
            if (tps2 == null) {
                tps2 = reg.createTrajectory2Vector();
            }
            TrajectoryPoint points[] = new TrajectoryPoint[tps1.size()];
            points = tps1.toArray(points);
            TrajectoryPoint prevtp = null;
            for (int i = 0; i < points.length; i++) {
                TrajectoryPoint tp = points[i];
                if (prevtp == null) {
                    prevtp = tp;
                    continue;
                }
                if (DistancePointSegment.distanceToSegment(x, y, prevtp.x, prevtp.y, tp.x, tp.y) <= 12) {
                    selectedTrajectory = tps1;
                    return;
                }
            }

            points = new TrajectoryPoint[tps2.size()];
            points = tps2.toArray(points);
            prevtp = null;
            for (int i = 0; i < points.length; i++) {
                TrajectoryPoint tp = points[i];
                if (prevtp == null) {
                    prevtp = tp;
                    continue;
                }
                if (DistancePointSegment.distanceToSegment(x, y, prevtp.x, prevtp.y, tp.x, tp.y) <= 12) {
                    selectedTrajectory = tps2;
                    return;
                }
            }
        }

        toolInterface.repaintImage();
    }

    public void mouseDragged(int x, int y, int modifiers) {
        if (selectedTrajectory != null) {
            if ((modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                int dx = x - startX;
                int dy = y - startY;

                if (!toolInterface.isInCtrlMode()) {
                    moveTrajectory(selectedTrajectory, dx, dy);
                } else {
                    moveTrajectory(this.tps1, dx, dy);
                    moveTrajectory(this.tps2, dx, dy);
                }

                startX = x;
                startY = y;

                saveTrajectory();
                toolInterface.repaintImage();
            } else if (selectedTrajectory != null && (modifiers & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                Rectangle r = this.getRectangle(selectedTrajectory);
                angle = -Math.atan2(startY - r.getCenterY(), startX - r.getCenterX()) + Math.atan2(y - r.getCenterY(), x - r.getCenterX());
            }
        }
    }

    public void moveTrajectory(Vector<TrajectoryPoint> trajectory, int dx, int dy) {
        for (TrajectoryPoint tp : trajectory) {
            tp.x += dx;
            tp.y += dy;
        }
    }

    public void rotateTrajectory(Vector<TrajectoryPoint> trajectory, double angle) {
        if (trajectory != null) {
            Rectangle r = this.getRectangle(trajectory);
            for (TrajectoryPoint tp : trajectory) {
                AffineTransform affine = new AffineTransform();
                affine.rotate(angle, r.getCenterX(), r.getCenterY());
                Point2D ptDest = new Point2D.Double();
                affine.transform(tp.getPoint(), ptDest);
                tp.x = (int) ptDest.getX();
                tp.y = (int) ptDest.getY();
            }
        }
    }

    public void keyPressed(KeyEvent e) {
    }
}
