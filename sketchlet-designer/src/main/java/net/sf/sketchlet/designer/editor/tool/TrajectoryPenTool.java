/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.tool;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.tools.log.ActivityLog;
import net.sf.sketchlet.model.ActiveRegion;
import net.sf.sketchlet.model.TrajectoryPoint;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * @author zobrenovic
 */
public class TrajectoryPenTool extends Tool {
    private Cursor cursor;
    private int trajectory;
    private long startTime = 0;
    private int mouseX = -1;
    private int mouseY = -1;

    private final int squareSize = 7;
    private List<TrajectoryPoint> tps = null;

    public TrajectoryPenTool(SketchletEditor editor, int trajectory) {
        super(editor);
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

    public void mouseMoved(int x, int y, int modifiers) {
        mouseX = x;
        mouseY = y;
        toolInterface.repaintImage();
    }

    public void mousePressed(int x, int y, int modifiers) {
        startTime = System.currentTimeMillis();
        SketchletEditor.getInstance().saveRegionUndo();
    }

    public void mouseReleased(int x, int y, int modifiers) {
        if (mouseX == x && mouseY == y) {
            addPoint(x, y);
        }
        ActivityLog.log("toolResult", "Add a trajectory point", "select.png", toolInterface.getPanel());
    }

    public void mouseDragged(int x, int y, int modifiers) {
        mouseX = -1;
        mouseY = -1;
        addPoint(x, y);
    }

    private void addPoint(int x, int y) {
        ActiveRegion reg = this.getRegion();
        if (reg != null) {
            if (trajectory == 1) {
                reg.trajectory1 += x + " " + y + " " + (System.currentTimeMillis() - startTime) + "\n";
                tps = reg.createTrajectoryVector();
            } else {
                reg.trajectory2 += x + " " + y + " " + (System.currentTimeMillis() - startTime) + "\n";
                tps = reg.createTrajectory2Vector();
            }
            toolInterface.repaintImage();
        }
    }

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
                g2.fillOval(tp.getX() - squareSize / 2, tp.getY() - squareSize / 2, squareSize, squareSize);
            }
            if (tps.size() > 0) {
                TrajectoryPoint tp = tps.get(tps.size() - 1);
                g2.fillOval(tp.getX() - squareSize, tp.getY() - squareSize, squareSize * 2, squareSize * 2);

                if (mouseX >= 0) {
                    g2.drawLine(mouseX, mouseY, tp.getX(), tp.getY());
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

    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
    }

    private ActiveRegion getRegion() {
        if (editor.getCurrentPage().getRegions().getSelectedRegions() != null) {
            if (editor.getCurrentPage().getRegions().getSelectedRegions().size() > 0) {
                return editor.getCurrentPage().getRegions().getSelectedRegions().lastElement();
            }
        }
        return null;
    }
}
