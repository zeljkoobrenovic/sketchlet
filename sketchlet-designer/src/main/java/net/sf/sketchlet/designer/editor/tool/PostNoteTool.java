/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.tool;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.tools.log.ActivityLog;

import java.awt.*;

/**
 * @author zobrenovic
 */
public class PostNoteTool extends Tool {

    private Cursor cursor;
    private Tool previousTool;

    public PostNoteTool(SketchletEditor freeHand) {
        super(freeHand);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Point hotSpot = new Point(25, 25);
        Image cursorImage = Workspace.createImageIcon("resources/cursor_postit.png").getImage();
        cursor = toolkit.createCustomCursor(cursorImage, hotSpot, "Post Note");
    }

    @Override
    public String getName() {
        return Language.translate("PostIt Note Tool");
    }

    @Override
    public void mouseReleased(int x, int y, int modifiers) {
        super.mouseReleased(x, y, modifiers);
        ActivityLog.log("toolResult", "Put a post note on the page", "line_1.png", toolInterface.getPanel());
    }

    @Override
    public void mousePressed(int x, int y, int modifiers) {
        SketchletEditor.getInstance().addNote(x, y);
        if (getPreviousTool() != null) {
            SketchletEditor.getInstance().setTool(getPreviousTool(), toolInterface.getPanel());
        }
    }

    @Override
    public Cursor getCursor() {
        return cursor;
    }

    public Tool getPreviousTool() {
        return previousTool;
    }

    public void setPreviousTool(Tool previousTool) {
        this.previousTool = previousTool;
    }
}
