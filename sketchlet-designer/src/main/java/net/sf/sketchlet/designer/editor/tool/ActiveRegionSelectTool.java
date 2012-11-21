/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.tool;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.model.Connector;
import net.sf.sketchlet.designer.editor.ui.region.ConnectorPanel;
import net.sf.sketchlet.designer.playback.ui.PlaybackFrame;
import net.sf.sketchlet.model.ActiveRegion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class ActiveRegionSelectTool extends Tool {

    private int prevX, prevY;
    private int endX, endY;
    private long prevTimestamp;
    private double speeds[] = new double[5];
    private int currentDistanceIndex = 0;
    private double speed;
    private boolean inSelectMode = false;
    private int lastX = -1000, lastY = -1000;

    private boolean bDraggingPerspectivePoint1 = false;
    private boolean bDraggingPerspectivePoint2 = false;
    private boolean bDragged = false;

    public ActiveRegionSelectTool(SketchletEditor freeHand) {
        super(freeHand);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Point hotSpot = new Point(5, 4);
    }

    @Override
    public ImageIcon getIcon() {
        return Workspace.createImageIcon("resources/arrow_cursor.png");
    }

    @Override
    public String getIconFileName() {
        return "arrow_cursor.png";
    }

    @Override
    public void mouseMoved(MouseEvent e, int x, int y) {
        ActiveRegion a = editor.getCurrentPage().getRegions().selectRegion(x, y, false);
        editor.getCurrentPage().getRegions().defocusAllRegions();

        if (x < 0 || y < 0) {
            editor.setCursor(Cursor.getDefaultCursor());
        } else if (a != null) {
            a.getMouseHandler().mouseMoved(e, editor.getScale(), SketchletEditor.editorFrame, false);
            a.bInFocus = true;
        } else {
            editor.setCursor();
        }
        lastX = x;
        lastY = y;
    }

    @Override
    public void mousePressed(MouseEvent e, int x, int y) {
        bDragged = false;
        editor.getCurrentPage().setSelectedConnector(null);
        if (new Rectangle((int) editor.getCurrentPage().getPerspective_horizont_x1() - 5, (int) editor.getCurrentPage().getPerspective_horizont_y() - 5, 11, 11).contains(x, y)) {
            bDraggingPerspectivePoint1 = true;
        } else if (new Rectangle((int) editor.getCurrentPage().getPerspective_horizont_x2() - 5, (int) editor.getCurrentPage().getPerspective_horizont_y() - 5, 11, 11).contains(x, y)) {
            bDraggingPerspectivePoint2 = true;
        } else {
            editor.getCurrentPage().selectConnector(e.getX(), e.getY());
            editor.getCurrentPage().getRegions().mousePressed(e, editor.getScale(), editor.editorFrame, false, false);
            editor.saveRegionUndo();

            prevX = x;
            prevY = y;
            prevTimestamp = e.getWhen();

            for (int i = 0; i < speeds.length; i++) {
                speeds[i] = 0.0;
            }

            if (editor.getCurrentPage().getRegions().getSelectedRegions() == null) {
                Connector c = editor.getCurrentPage().selectConnector(e.getX(), e.getY());
                if (c != null) {
                    if (e.getClickCount() >= 2) {
                        ConnectorPanel.createAndShowGUI(c);
                    }
                    editor.repaintEverything();
                } else {
                    endX = x;
                    endY = y;
                    inSelectMode = true;
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e, int x, int y) {
        editor.getCurrentPage().getRegions().mouseReleased(e, editor.getScale(), editor.editorFrame, false);
        if (!bDragged) {
            if (editor.getUndoRegionActions().size() > 0) {
                editor.getUndoRegionActions().remove(editor.getUndoRegionActions().size() - 1);
            }
        }
        if (inSelectMode) {
            editor.getCurrentPage().getRegions().setSelectedRegions(getSelectedRegions());
            prevX = 0;
            prevY = 0;
            endX = 0;
            endY = 0;
            inSelectMode = false;
            editor.repaint();
        }
        bDraggingPerspectivePoint1 = false;
        bDraggingPerspectivePoint2 = false;
        editor.getPerspectivePanel().reload();
    }

    @Override
    public void mouseDragged(MouseEvent e, int x, int y) {
        bDragged = true;
        if (bDraggingPerspectivePoint2) {
            editor.getCurrentPage().setPerspective_horizont_x2(x);
            editor.getCurrentPage().setPerspective_horizont_y(y);
            editor.getCurrentPage().setProperty("perspective x2", "" + x);
            editor.getCurrentPage().setProperty("perspective y", "" + y);
            editor.repaint();
        } else if (bDraggingPerspectivePoint1) {
            editor.getCurrentPage().setPerspective_horizont_x1(x);
            editor.getCurrentPage().setPerspective_horizont_y(y);
            editor.getCurrentPage().setProperty("perspective x1", "" + x);
            editor.getCurrentPage().setProperty("perspective y", "" + y);
            editor.repaint();
        } else if (!inSelectMode) {
            editor.getCurrentPage().getRegions().mouseDragged(e, editor.getScale(), editor.editorFrame, false);
            if (PlaybackFrame.playbackFrame != null && editor.getCurrentPage().getRegions().getSelectedRegions() != null) {
                editor.getCurrentPage().getRegions().getSelectedRegions().lastElement().play();
            }

            if (editor.getCurrentPage().getRegions().getSelectedRegions() != null && editor.getCurrentPage().getRegions().getSelectedRegions().size() > 0 && ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK || (e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)) {
                if (e.getWhen() != prevTimestamp) {
                    ActiveRegion region = editor.getCurrentPage().getRegions().getSelectedRegions().lastElement();
                    int dx = x - prevX;
                    int dy = y - prevY;
                    double dt = (e.getWhen() - prevTimestamp) / 100.0;

                    speed = Math.sqrt(dx * dx + dy * dy) / dt;

                    speeds[currentDistanceIndex++] = speed;
                    currentDistanceIndex = currentDistanceIndex % speeds.length;

                    double _s = 0.0;
                    for (int i = 0; i < speeds.length; i++) {
                        _s += speeds[i];
                    }

                    speed = _s / speeds.length;

                    if (dx != 0 || dy != 0) {
                        if (!region.regionGrouping.equals("")) {
                            for (ActiveRegion as : region.parent.getRegions()) {
                                if (as != region && as.regionGrouping.equals(region.regionGrouping)) {
                                    as.getMotionHandler().processLimits("position x", as.x1, 0, 0, true);
                                    as.getMotionHandler().processLimits("position y", as.y1, 0, 0, true);
                                }
                            }
                        }
                    }
                    prevX = x;
                    prevY = y;
                    prevTimestamp = e.getWhen();
                }
            }
        } else {
            editor.getCurrentPage().getRegions().setSelectedRegions(getSelectedRegions());
            endX = x;
            endY = y;
            editor.repaint();
        }
    }

    @Override
    public Cursor getCursor() {
        if (new Rectangle((int) editor.getCurrentPage().getPerspective_horizont_x1() - 5, (int) editor.getCurrentPage().getPerspective_horizont_y() - 5, 11, 11).contains(lastX, lastY)) {
            return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
        } else if (new Rectangle((int) editor.getCurrentPage().getPerspective_horizont_x2() - 5, (int) editor.getCurrentPage().getPerspective_horizont_y() - 5, 11, 11).contains(lastX, lastY)) {
            return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
        } else {
            return Cursor.getDefaultCursor();
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        if (inSelectMode) {
            g2.setColor(new Color(100, 100, 100, 100));
            g2.setStroke(new BasicStroke(1));
            g2.drawRect(Math.min(prevX, endX), Math.min(prevY, endY), Math.abs(prevX - endX), Math.abs(prevY - endY));
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            if (editor.getCurrentPage().getSelectedConnector() != null) {
                editor.getCurrentPage().removeConnector(editor.getCurrentPage().getSelectedConnector());
                editor.getCurrentPage().setSelectedConnector(null);
            } else {
                SketchletEditor.getInstance().deleteSelectedRegion();
            }
        } else {
            if (editor.getCurrentPage().getSelectedConnector() != null) {
                Connector conn = editor.getCurrentPage().getSelectedConnector();
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_BACK_SPACE:
                        SketchletEditor.getInstance().saveRegionUndo();
                        String strText = conn.getCaption();
                        if (strText.length() > 0) {
                            conn.setCaption(strText.substring(0, strText.length() - 1));
                        }

                        editor.repaintEverything();
                        break;

                    default:
                        String keyText = e.getKeyText(e.getKeyCode());
                        if (!conn.getCaption().startsWith("=")) {
                            if (keyText.equalsIgnoreCase("ENTER")) {
                                SketchletEditor.getInstance().saveRegionUndo();
                                conn.setCaption(conn.getCaption() + "\n");
                                editor.repaintEverything();
                            } else if (!e.isControlDown() && !e.isAltGraphDown() && !e.isMetaDown() && !e.isAltDown() && !e.isActionKey() && e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
                                SketchletEditor.getInstance().saveRegionUndo();
                                conn.setCaption(conn.getCaption() + "" + e.getKeyChar());
                                editor.repaintEverything();
                            }
                        }
                        break;
                }
            } else {
                ActiveRegion region = editor.getCurrentPage().getRegions().getLastSelectedRegion();
                if (region != null) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_BACK_SPACE:
                            SketchletEditor.getInstance().saveRegionUndo();
                            String strText = region.text;
                            if (strText.length() > 0) {
                                region.text = strText.substring(0, strText.length() - 1);
                            }

                            editor.repaintEverything();
                            break;

                        default:
                            String keyText = e.getKeyText(e.getKeyCode());
                            if (!region.text.startsWith("=")) {
                                if (keyText.equalsIgnoreCase("ENTER")) {
                                    SketchletEditor.getInstance().saveRegionUndo();
                                    region.text += "\n";
                                    editor.repaintEverything();
                                } else if (!e.isControlDown() && !e.isAltGraphDown() && !e.isMetaDown() && !e.isAltDown() && !e.isActionKey() && e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
                                    SketchletEditor.getInstance().saveRegionUndo();
                                    region.text += "" + e.getKeyChar();
                                    editor.repaintEverything();
                                }
                            }
                            break;
                    }
                }
            }
        }
    }

    private Vector<ActiveRegion> getSelectedRegions() {
        Vector<ActiveRegion> regions = new Vector<ActiveRegion>();

        for (ActiveRegion region : editor.getCurrentPage().getRegions().getRegions()) {
            if (region.getArea(false).intersects(Math.min(prevX, endX), Math.min(prevY, endY), Math.abs(prevX - endX), Math.abs(prevY - endY))) {
                regions.add(region);
            }
        }

        return regions.size() == 0 ? null : regions;
    }

    @Override
    public String getName() {
        return Language.translate("Active Region Select Tool");
    }
}
