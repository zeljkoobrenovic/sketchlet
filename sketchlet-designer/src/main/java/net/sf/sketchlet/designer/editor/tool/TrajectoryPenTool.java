/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.tool;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.data.TrajectoryPoint;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.tools.log.ActivityLog;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class TrajectoryPenTool extends Tool {

    Cursor cursor;
    int trajectory;

    public TrajectoryPenTool(SketchletEditor freeHand, int trajectory) {
        super(freeHand);
        this.trajectory = trajectory;
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Point hotSpot = new Point(4, 23);
        Image cursorImage = Workspace.createImageIcon("resources/cursor_pen_trajectory" + trajectory + ".gif").getImage();
        cursor = toolkit.createCustomCursor(cursorImage, hotSpot, "Add Trajectory Points");
    }

    public String getName() {
        return Language.translate("Trajectory Pen Tool");
    }

    public Cursor getCursor() {
        return cursor;
    }

    public int mouseX = -1;
    public int mouseY = -1;

    public void mouseMoved(int x, int y, int modifiers) {
        mouseX = x;
        mouseY = y;
        toolInterface.repaintImage();
    }

    long startTime = 0;

    public void mousePressed(int x, int y, int modifiers) {
        startTime = System.currentTimeMillis();
        SketchletEditor.editorPanel.saveRegionUndo();
    }

    public void mouseReleased(int x, int y, int modifiers) {
        if (mouseX == x && mouseY == y) {
            addPoint(x, y);
        }
        ActivityLog.log("toolResult", "Add a trajectory point", "select.png", toolInterface.getPanel());
        TutorialPanel.addLine("cmd", "Add a trajectory point", "", toolInterface.getPanel());
    }

    public void mouseDragged(int x, int y, int modifiers) {
        mouseX = -1;
        mouseY = -1;
        addPoint(x, y);
    }

    public void addPoint(int x, int y) {
        ActiveRegion reg = this.getRegion();
        if (reg != null) {
            if (trajectory == 1) {
                reg.strTrajectory1 += x + " " + y + " " + (System.currentTimeMillis() - startTime) + "\n";
                tps = reg.createTrajectoryVector();
            } else {
                reg.strTrajectory2 += x + " " + y + " " + (System.currentTimeMillis() - startTime) + "\n";
                tps = reg.createTrajectory2Vector();
            }
            toolInterface.repaintImage();
        }
    }

    final int squareSize = 7;
    Vector<TrajectoryPoint> tps = null;

    public void draw(Graphics2D g2) {
        ActiveRegion reg = this.getRegion();
        if (reg != null) {
            if (tps == null) {
                if (trajectory == 1) {
                    tps = reg.createTrajectoryVector();
                } else {
                    tps = reg.createTrajectory2Vector();
                }
            }
            if (trajectory == 1) {
                g2.setColor(new Color(200, 0, 0, 220));
            } else {
                g2.setColor(new Color(0, 0, 255));
            }
            for (TrajectoryPoint tp : tps) {
                g2.fillOval(tp.x - squareSize / 2, tp.y - squareSize / 2, squareSize, squareSize);
            }
            if (tps.size() > 0) {
                TrajectoryPoint tp = tps.lastElement();
                g2.fillOval(tp.x - squareSize, tp.y - squareSize, squareSize * 2, squareSize * 2);

                if (mouseX >= 0) {
                    g2.drawLine(mouseX, mouseY, tp.x, tp.y);
                }
            }
        }
    }

    public void activate() {
        ActiveRegion reg = this.getRegion();
        if (reg != null) {
            if (trajectory == 1) {
                tps = reg.createTrajectoryVector();
            } else {
                tps = reg.createTrajectory2Vector();
            }
        }

        toolInterface.repaintImage();
    }

    public void deactivate() {
        tps = null;
    }

    public void onUndo() {
        activate();
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
    }

    public void keyReleased(KeyEvent e) {
    }

    public ActiveRegion getRegion() {
        if (freeHand.currentPage.regions.selectedRegions != null) {
            if (freeHand.currentPage.regions.selectedRegions.size() > 0) {
                return freeHand.currentPage.regions.selectedRegions.lastElement();
            }
        }
        return null;
    }
}
