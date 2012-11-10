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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class TrajectoryPointsTool extends Tool {

    Cursor cursor;

    public TrajectoryPointsTool(SketchletEditor freeHand) {
        super(freeHand);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Point hotSpot = new Point(9, 2);
        Image cursorImage = Workspace.createImageIcon("resources/cursor_hand.png").getImage();
        cursor = toolkit.createCustomCursor(cursorImage, hotSpot, "Move TrajectoryPoints");
    }

    public String getName() {
        return Language.translate("Trajectory Points Tool");
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void mouseMoved(int x, int y, int modifiers) {
    }

    public void mousePressed(int x, int y, int modifiers) {
        selectedPoint = null;
        ActiveRegion reg = this.getRegion();
        if (reg != null) {
            SketchletEditor.editorPanel.saveRegionUndo();
            if (tps1 == null) {
                tps1 = reg.createTrajectoryVector();
            }
            if (tps2 == null) {
                tps2 = reg.createTrajectory2Vector();
            }
            for (TrajectoryPoint tp : tps1) {
                Rectangle rect1 = new Rectangle(tp.x - squareSize / 2, tp.y - squareSize / 2, squareSize, squareSize);
                if (rect1.contains(new Point(x, y))) {
                    selectedPoint = tp;
                    return;
                }
            }
            for (TrajectoryPoint tp : tps2) {
                Rectangle rect2 = new Rectangle(tp.x - squareSize / 2, tp.y - squareSize / 2, squareSize, squareSize);
                if (rect2.contains(new Point(x, y))) {
                    selectedPoint = tp;
                    return;
                }
            }
            if (selectedPoint == null) {
                TrajectoryPoint points[] = new TrajectoryPoint[tps1.size()];
                points = tps1.toArray(points);
                TrajectoryPoint prevtp = null;
                for (int i = 0; i < points.length; i++) {
                    TrajectoryPoint tp = points[i];
                    if (prevtp == null) {
                        prevtp = tp;
                        continue;
                    }
                    if (DistancePointSegment.distanceToSegment(x, y, prevtp.x, prevtp.y, tp.x, tp.y) <= squareSize / 2) {
                        selectedPoint = new TrajectoryPoint(x, y, (prevtp.time + tp.time) / 2);
                        tps1.insertElementAt(selectedPoint, i);
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
                    if (DistancePointSegment.distanceToSegment(x, y, prevtp.x, prevtp.y, tp.x, tp.y) <= squareSize / 2) {
                        selectedPoint = new TrajectoryPoint(x, y, (prevtp.time + tp.time) / 2);
                        tps2.insertElementAt(selectedPoint, i);
                        return;
                    }
                }
            }
        }

        toolInterface.repaintImage();
    }

    public void mouseReleased(int x, int y, int modifiers) {
        saveTrajectory();
        if ((modifiers & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
            popupMenu(x, y);
        }
    }

    public void popupMenu(final int x, final int y) {
        int _x = (int) (x * SketchletEditor.editorPanel.scale + SketchletEditor.marginX);
        int _y = (int) (y * SketchletEditor.editorPanel.scale + SketchletEditor.marginY);

        JPopupMenu popup = new JPopupMenu();
        TutorialPanel.prepare(popup, true);

        JMenuItem delete = new JMenuItem("Delete Point", net.sf.sketchlet.designer.Workspace.createImageIcon("resources/user-trash.png"));
        JMenuItem cut1 = new JMenuItem("Simplify Primary Curve...", net.sf.sketchlet.designer.Workspace.createImageIcon("resources/edit-cut.png"));
        JMenuItem cut2 = new JMenuItem("Simplify Secondary Curve...", net.sf.sketchlet.designer.Workspace.createImageIcon("resources/edit-cut.png"));

        delete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                deletePoint();
            }
        });
        cut1.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                simplifyCurve(tps1);
            }
        });
        cut2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                simplifyCurve(tps2);
            }
        });

        if (selectedPoint != null) {
            popup.add(delete);
            popup.addSeparator();
        }
        if (tps1.size() > 2) {
            popup.add(cut1);
        }
        if (tps2.size() > 2) {
            popup.add(cut2);
        }

        popup.show(SketchletEditor.editorPanel, _x, _y);
    }

    public void simplifyCurve(Vector<TrajectoryPoint> tps) {
        String inputValue = JOptionPane.showInputDialog("Minimal distance between points (pixels):", "10");
        if (inputValue != null) {
            try {
                int dist = (int) Double.parseDouble(inputValue);
                if (dist >= 1) {
                    TrajectoryPoint points[] = new TrajectoryPoint[tps.size()];
                    points = tps.toArray(points);
                    TrajectoryPoint prevtp = null;
                    SketchletEditor.editorPanel.saveRegionUndo();
                    for (int i = 0; i < points.length; i++) {
                        TrajectoryPoint tp = points[i];
                        if (prevtp == null) {
                            prevtp = tp;
                            continue;
                        }

                        if (Math.sqrt((tp.x - prevtp.x) * (tp.x - prevtp.x) + (tp.y - prevtp.y) * (tp.y - prevtp.y)) < dist) {
                            tps.remove(tp);
                        } else {
                            prevtp = tp;
                        }
                    }
                    saveTrajectory();
                }
            } catch (Exception e) {
            }
        }

        toolInterface.repaintImage();
    }

    public void saveTrajectory() {
        ActiveRegion reg = this.getRegion();
        if (reg != null) {
            String strTrajectory1 = "";
            String strTrajectory2 = "";
            for (TrajectoryPoint tp : tps1) {
                strTrajectory1 += tp.x + " " + tp.y + " " + tp.time + "\n";
            }
            for (TrajectoryPoint tp : tps2) {
                strTrajectory2 += tp.x + " " + tp.y + " " + tp.time + "\n";
            }
            reg.strTrajectory1 = strTrajectory1;
            reg.strTrajectory2 = strTrajectory2;
            toolInterface.repaintImage();
        }

    }

    public void mouseDragged(int x, int y, int modifiers) {
        if (selectedPoint != null) {
            selectedPoint.x = x;
            selectedPoint.y = y;
            toolInterface.repaintImage();
        }
    }

    final int squareSize = 7;
    TrajectoryPoint selectedPoint = null;
    Vector<TrajectoryPoint> tps1 = null;
    Vector<TrajectoryPoint> tps2 = null;
    boolean bDrawBorders = false;
    boolean bDraw1 = false, bDraw2 = false;
    double angle = 0;

    public Rectangle getRectangle(Vector<TrajectoryPoint> trajectory) {
        int bx1 = Integer.MAX_VALUE, by1 = Integer.MAX_VALUE, bx2 = 0, by2 = 0;
        for (TrajectoryPoint tp : trajectory) {
            if (tp.x < bx1) {
                bx1 = tp.x;
            }
            if (tp.y < by1) {
                by1 = tp.y;
            }
            if (tp.x > bx2) {
                bx2 = tp.x;
            }
            if (tp.y > by2) {
                by2 = tp.y;
            }
        }

        return new Rectangle(bx1, by1, bx2 - bx1, by2 - by1);
    }

    public void draw(Graphics2D g2) {
        ActiveRegion reg = this.getRegion();
        if (reg != null) {
            if (tps1 == null) {
                tps1 = reg.createTrajectoryVector();
            }
            if (tps2 == null) {
                tps2 = reg.createTrajectory2Vector();
            }
            g2.setColor(new Color(0, 0, 0, 150));
            for (TrajectoryPoint tp : tps1) {
                if (tp == selectedPoint) {
                    g2.setColor(new Color(255, 0, 0));
                    g2.fillOval(tp.x - squareSize / 2 - 2, tp.y - squareSize / 2 - 2, squareSize + 4, squareSize + 4);
                } else {
                    g2.setColor(new Color(200, 0, 0, 220));
                    g2.fillOval(tp.x - squareSize / 2, tp.y - squareSize / 2, squareSize, squareSize);
                }
            }

            if (bDrawBorders && bDraw1) {
                Rectangle r = this.getRectangle(tps1);
                g2.setColor(Color.GRAY);
                g2.rotate(angle, r.getCenterX(), r.getCenterY());
                g2.drawRect(r.x, r.y, r.width, r.height);
                g2.rotate(-angle, r.getCenterX(), r.getCenterY());
            }
            for (TrajectoryPoint tp : tps2) {
                if (tp == selectedPoint) {
                    g2.setColor(new Color(0, 0, 255));
                    g2.fillOval(tp.x - squareSize / 2 - 2, tp.y - squareSize / 2 - 2, squareSize + 4, squareSize + 4);
                } else {
                    g2.setColor(new Color(0, 0, 200, 220));
                    g2.fillOval(tp.x - squareSize / 2, tp.y - squareSize / 2, squareSize, squareSize);
                }
            }
            if (bDrawBorders && bDraw2) {
                Rectangle r = this.getRectangle(tps1);
                g2.setColor(Color.GRAY);
                g2.rotate(angle, r.getCenterX(), r.getCenterY());
                g2.drawRect(r.x, r.y, r.width, r.height);
                g2.rotate(-angle, r.getCenterX(), r.getCenterY());
            }
        }
    }

    public void activate() {
        ActiveRegion reg = this.getRegion();
        selectedPoint = null;
        if (reg != null) {
            tps1 = reg.createTrajectoryVector();
            tps2 = reg.createTrajectory2Vector();
        }
        toolInterface.repaintImage();
    }

    public void deactivate() {
        tps1 = null;
        tps2 = null;
    }

    public void onUndo() {
        activate();
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            deletePoint();
        }
    }

    public void deletePoint() {
        if (selectedPoint != null) {
            tps1.remove(selectedPoint);
            tps2.remove(selectedPoint);
            saveTrajectory();
        }
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
