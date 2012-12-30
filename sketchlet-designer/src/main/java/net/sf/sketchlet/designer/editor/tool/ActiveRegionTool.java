package net.sf.sketchlet.designer.editor.tool;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.framework.model.log.ActivityLog;
import net.sf.sketchlet.designer.playback.ui.PlaybackFrame;
import net.sf.sketchlet.framework.model.ActiveRegion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

/**
 * @author zobrenovic
 */
public class ActiveRegionTool extends Tool {
    private int prevX, prevY;
    private long prevTimestamp;
    private double speeds[] = new double[5];
    private int currentDistanceIndex = 0;
    private double speed;
    private Cursor cursor;
    private boolean bDragged = false;

    public ActiveRegionTool(SketchletEditor freeHand) {
        super(freeHand);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Point hotSpot = new Point(5, 4);
        Image cursorImage = Workspace.createImageIcon("resources/cursor_active_region.png").getImage();
        cursor = toolkit.createCustomCursor(cursorImage, hotSpot, "Active Region");
    }

    @Override
    public String getName() {
        return Language.translate("Active Region Create Tool");
    }

    @Override
    public ImageIcon getIcon() {
        return Workspace.createImageIcon("resources/active_region.png");
    }

    @Override
    public String getIconFileName() {
        return "active_region.png";
    }

    @Override
    public void mouseMoved(MouseEvent e, int x, int y) {
        if (x < 0 || y < 0) {
            editor.setCursor(Cursor.getDefaultCursor());
        } else {
            editor.setCursor();
        }
    }

    @Override
    public void mousePressed(MouseEvent e, int x, int y) {
        bDragged = false;
        editor.getCurrentPage().getRegions().getMouseHelper().mousePressed(e, editor.getScale(), editor.editorFrame, false, true);
        if (editor.getCurrentPage().getRegions().getMouseHelper().isNewRegion()) {
            editor.saveNewRegionUndo();
        } else {
            editor.saveRegionUndo();
        }
        prevX = x;
        prevY = y;
        prevTimestamp = e.getWhen();

        for (int i = 0; i < speeds.length; i++) {
            speeds[i] = 0.0;
        }

    }

    @Override
    public void mouseReleased(MouseEvent e, int x, int y) {
        boolean newRegion = editor.getCurrentPage().getRegions().getMouseHelper().isNewRegion();
        editor.getCurrentPage().getRegions().getMouseHelper().mouseReleased(e, editor.getScale(), editor.editorFrame, false);

        if (editor.getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() == null || editor.getCurrentPage().getRegions().getMouseHelper().isWithoutEffect() || !bDragged) {
            if (editor.getUndoRegionActions().size() > 0) {
                editor.getUndoRegionActions().remove(editor.getUndoRegionActions().size() - 1);
            }
        }
        if (newRegion && editor.getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null) {
            SketchletEditor.getInstance().setTool(SketchletEditor.getInstance().getActiveRegionSelectTool(), SketchletEditor.getInstance());
        }

        editor.getPerspectivePanel().reload();
        editor.setCursor();
    }

    @Override
    public void mouseDragged(MouseEvent e, int x, int y) {
        bDragged = true;
        editor.getCurrentPage().getRegions().getMouseHelper().mouseDragged(e, editor.getScale(), editor.editorFrame, false);
        if (PlaybackFrame.playbackFrame != null && editor.getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null) {
            editor.getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement().play();
        }

        if (editor.getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && editor.getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0 && ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK || (e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)) {
            if (e.getWhen() != prevTimestamp) {
                ActiveRegion a = editor.getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
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
                    if (!a.getRegionGrouping().equals("")) {
                        for (ActiveRegion as : a.getParent().getRegions()) {
                            if (as != a && as.getRegionGrouping().equals(a.getRegionGrouping())) {
                                as.getMotionController().processLimits("position x", as.getX1Value(), 0, 0, true);
                                as.getMotionController().processLimits("position y", as.getY1Value(), 0, 0, true);
                            }
                        }
                    }
                }
                prevX = x;
                prevY = y;
                prevTimestamp = e.getWhen();
            }
        }
    }

    @Override
    public void mouseReleased(int x, int y, int modifiers) {
        ActivityLog.log("toolResult", "Create a new region");
    }

    @Override
    public Cursor getCursor() {
        return cursor;
    }
}
