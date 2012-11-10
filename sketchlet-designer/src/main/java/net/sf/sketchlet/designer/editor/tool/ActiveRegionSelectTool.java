/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.tool;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.regions.connector.Connector;
import net.sf.sketchlet.designer.editor.regions.connector.ConnectorPanel;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.ui.playback.PlaybackFrame;

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

    int prevX, prevY;
    int endX, endY;
    long prevTimestamp;
    double speeds[] = new double[5];
    int currentDistanceIndex = 0;
    double speed;
    boolean inSelectMode = false;
    int lastX = -1000, lastY = -1000;

    public ActiveRegionSelectTool(SketchletEditor freeHand) {
        super(freeHand);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Point hotSpot = new Point(5, 4);
    }

    public ImageIcon getIcon() {
        return Workspace.createImageIcon("resources/arrow_cursor.png");
    }

    public String getIconFileName() {
        return "arrow_cursor.png";
    }

    public void mouseMoved(MouseEvent e, int x, int y) {
        ActiveRegion a = freeHand.currentPage.regions.selectRegion(x, y, false);
        freeHand.currentPage.regions.defocusAllRegions();

        if (x < 0 || y < 0) {
            freeHand.setCursor(Cursor.getDefaultCursor());
        } else if (a != null) {
            a.mouseHandler.mouseMoved(e, freeHand.scale, SketchletEditor.editorFrame, false);
            a.bInFocus = true;
        } else {
            freeHand.setCursor();
        }
        lastX = x;
        lastY = y;
    }

    boolean bDraggingPerspectivePoint1 = false;
    boolean bDraggingPerspectivePoint2 = false;
    private boolean bDragged = false;

    public void mousePressed(MouseEvent e, int x, int y) {
        bDragged = false;
        freeHand.currentPage.selectedConnector = null;
        if (new Rectangle((int) freeHand.currentPage.perspective_horizont_x1 - 5, (int) freeHand.currentPage.perspective_horizont_y - 5, 11, 11).contains(x, y)) {
            bDraggingPerspectivePoint1 = true;
        } else if (new Rectangle((int) freeHand.currentPage.perspective_horizont_x2 - 5, (int) freeHand.currentPage.perspective_horizont_y - 5, 11, 11).contains(x, y)) {
            bDraggingPerspectivePoint2 = true;
        } else {
            freeHand.currentPage.selectConnector(e.getX(), e.getY());
            freeHand.currentPage.regions.mousePressed(e, freeHand.scale, freeHand.editorFrame, false, false);
            freeHand.saveRegionUndo();

            prevX = x;
            prevY = y;
            prevTimestamp = e.getWhen();

            for (int i = 0; i < speeds.length; i++) {
                speeds[i] = 0.0;
            }

            if (freeHand.currentPage.regions.selectedRegions == null) {
                Connector c = freeHand.currentPage.selectConnector(e.getX(), e.getY());
                if (c != null) {
                    if (e.getClickCount() >= 2) {
                        ConnectorPanel.createAndShowGUI(c);
                    }
                    freeHand.repaintEverything();
                } else {
                    endX = x;
                    endY = y;
                    inSelectMode = true;
                }
            }
        }
    }

    public void mouseReleased(MouseEvent e, int x, int y) {
        freeHand.currentPage.regions.mouseReleased(e, freeHand.scale, freeHand.editorFrame, false);
        if (!bDragged) {
            if (freeHand.undoRegionActions.size() > 0) {
                freeHand.undoRegionActions.remove(freeHand.undoRegionActions.size() - 1);
            }
        }
        if (inSelectMode) {
            freeHand.currentPage.regions.selectedRegions = getSelectedRegions();
            prevX = 0;
            prevY = 0;
            endX = 0;
            endY = 0;
            inSelectMode = false;
            freeHand.repaint();
        }
        bDraggingPerspectivePoint1 = false;
        bDraggingPerspectivePoint2 = false;
        freeHand.perspectivePanel.reload();
    }

    public void mouseDragged(MouseEvent e, int x, int y) {
        bDragged = true;
        if (bDraggingPerspectivePoint2) {
            freeHand.currentPage.perspective_horizont_x2 = x;
            freeHand.currentPage.perspective_horizont_y = y;
            freeHand.currentPage.setProperty("perspective x2", "" + x);
            freeHand.currentPage.setProperty("perspective y", "" + y);
            freeHand.repaint();
        } else if (bDraggingPerspectivePoint1) {
            freeHand.currentPage.perspective_horizont_x1 = x;
            freeHand.currentPage.perspective_horizont_y = y;
            freeHand.currentPage.setProperty("perspective x1", "" + x);
            freeHand.currentPage.setProperty("perspective y", "" + y);
            freeHand.repaint();
        } else if (!inSelectMode) {
            freeHand.currentPage.regions.mouseDragged(e, freeHand.scale, freeHand.editorFrame, false);
            if (PlaybackFrame.playbackFrame != null && freeHand.currentPage.regions.selectedRegions != null) {
                freeHand.currentPage.regions.selectedRegions.lastElement().play();
            }

            if (freeHand.currentPage.regions.selectedRegions != null && freeHand.currentPage.regions.selectedRegions.size() > 0 && ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK || (e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)) {
                if (e.getWhen() != prevTimestamp) {
                    ActiveRegion region = freeHand.currentPage.regions.selectedRegions.lastElement();
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
                    double _speed = region.limitsHandler.processLimits("speed", speed, 0.0, 0.0, true);

                    /*
                    if (_speed != speed) {
                    x = (int) (prevX + dx * _speed / speed);
                    y = (int) (prevY + dy * _speed / speed);
                    }
                     */

                    if (dx != 0 || dy != 0) {
                        if (!region.regionGrouping.equals("")) {
                            for (ActiveRegion as : region.parent.regions) {
                                if (as != region && as.regionGrouping.equals(region.regionGrouping)) {
                                    as.limitsHandler.processLimits("position x", as.x1, 0, 0, true);
                                    as.limitsHandler.processLimits("position y", as.y1, 0, 0, true);
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
            freeHand.currentPage.regions.selectedRegions = getSelectedRegions();
            endX = x;
            endY = y;
            freeHand.repaint();
        }
    }

    public void mouseMoved(int x, int y, int modifiers) {
    }

    public void mousePressed(int x, int y, int modifiers) {
    }

    public void mouseReleased(int x, int y, int modifiers) {
    }

    public void mouseDragged(int x, int y, int modifiers) {
    }

    public Cursor getCursor() {
        if (new Rectangle((int) freeHand.currentPage.perspective_horizont_x1 - 5, (int) freeHand.currentPage.perspective_horizont_y - 5, 11, 11).contains(lastX, lastY)) {
            return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
        } else if (new Rectangle((int) freeHand.currentPage.perspective_horizont_x2 - 5, (int) freeHand.currentPage.perspective_horizont_y - 5, 11, 11).contains(lastX, lastY)) {
            return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
        } else {
            return Cursor.getDefaultCursor();
        }
    }

    public void draw(Graphics2D g2) {
        if (inSelectMode) {
            g2.setColor(new Color(100, 100, 100, 100));
            g2.setStroke(new BasicStroke(1));
            g2.drawRect(Math.min(prevX, endX), Math.min(prevY, endY), Math.abs(prevX - endX), Math.abs(prevY - endY));
        }
    }

    public void activate() {
    }

    public void deactivate() {
    }

    public void onUndo() {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            if (freeHand.currentPage.selectedConnector != null) {
                freeHand.currentPage.removeConnector(freeHand.currentPage.selectedConnector);
                freeHand.currentPage.selectedConnector = null;
            } else {
                SketchletEditor.editorPanel.deleteSelectedRegion();
            }
        } else {
            if (freeHand.currentPage.selectedConnector != null) {
                Connector conn = freeHand.currentPage.selectedConnector;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_BACK_SPACE:
                        SketchletEditor.editorPanel.saveRegionUndo();
                        String strText = conn.caption;
                        if (strText.length() > 0) {
                            conn.caption = strText.substring(0, strText.length() - 1);
                        }

                        freeHand.repaintEverything();
                        break;

                    default:
                        String keyText = e.getKeyText(e.getKeyCode());
                        if (!conn.caption.startsWith("=")) {
                            if (keyText.equalsIgnoreCase("ENTER")) {
                                SketchletEditor.editorPanel.saveRegionUndo();
                                conn.caption += "\n";
                                freeHand.repaintEverything();
                            } else if (!e.isControlDown() && !e.isAltGraphDown() && !e.isMetaDown() && !e.isAltDown() && !e.isActionKey() && e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
                                SketchletEditor.editorPanel.saveRegionUndo();
                                conn.caption += "" + e.getKeyChar();
                                freeHand.repaintEverything();
                                TutorialPanel.addLine("cmd", "Type the connection caption", "keyboard.png", SketchletEditor.editorPanel.scrollPane);
                            }
                        }
                        break;
                }
            } else {
                ActiveRegion region = freeHand.currentPage.regions.getLastSelectedRegion();
                if (region != null) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_BACK_SPACE:
                            SketchletEditor.editorPanel.saveRegionUndo();
                            String strText = region.strText;
                            if (strText.length() > 0) {
                                region.strText = strText.substring(0, strText.length() - 1);
                            }

                            freeHand.repaintEverything();
                            break;

                        default:
                            String keyText = e.getKeyText(e.getKeyCode());
                            if (!region.strText.startsWith("=")) {
                                if (keyText.equalsIgnoreCase("ENTER")) {
                                    SketchletEditor.editorPanel.saveRegionUndo();
                                    region.strText += "\n";
                                    freeHand.repaintEverything();
                                } else if (!e.isControlDown() && !e.isAltGraphDown() && !e.isMetaDown() && !e.isAltDown() && !e.isActionKey() && e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
                                    SketchletEditor.editorPanel.saveRegionUndo();
                                    region.strText += "" + e.getKeyChar();
                                    freeHand.repaintEverything();
                                    TutorialPanel.addLine("cmd", "Type the region text", "keyboard.png", SketchletEditor.editorPanel.scrollPane);
                                }
                            }
                            break;
                    }
                }
            }
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public Vector<ActiveRegion> getSelectedRegions() {
        Vector<ActiveRegion> regions = new Vector<ActiveRegion>();

        for (ActiveRegion region : freeHand.currentPage.regions.regions) {
            if (region.getArea(false).intersects(Math.min(prevX, endX), Math.min(prevY, endY), Math.abs(prevX - endX), Math.abs(prevY - endY))) {
                regions.add(region);
            }
        }

        return regions.size() == 0 ? null : regions;
    }

    public String getName() {
        return Language.translate("Active Region Select Tool");
    }
}
