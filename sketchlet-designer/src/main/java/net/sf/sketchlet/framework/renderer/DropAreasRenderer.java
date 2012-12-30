package net.sf.sketchlet.framework.renderer;

import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.dnd.DropArea;
import net.sf.sketchlet.designer.editor.dnd.DropAreas;

import java.awt.*;

/**
 * @author zeljko
 */
public class DropAreasRenderer {
    private DropAreas dropAreas;

    public static final Image ENTRY_ICON = Workspace.createImageIcon("resources/entry.gif").getImage();
    public static final Image EXIT_ICON = Workspace.createImageIcon("resources/exit.gif").getImage();
    public static final Image VARIABLE_ICON_IN = Workspace.createImageIcon("resources/variable_in.jpg").getImage();
    public static final Image KEYBOARD_ICON = Workspace.createImageIcon("resources/keyboard.png").getImage();
    public static final Image MOUSE_ICON = Workspace.createImageIcon("resources/mouse.png").getImage();
    public static final Image PROPERTIES_ICON = Workspace.createImageIcon("resources/details_transparent.png").getImage();
    public static final Image REGION_OVERLAP_ICON = Workspace.createImageIcon("resources/overlap.png").getImage();
    public static final Image MOVE_ROTATE_ICON = Workspace.createImageIcon("resources/move_rotate.png").getImage();

    public DropAreasRenderer(DropAreas dropAreas) {
        this.dropAreas = dropAreas;
    }

    public void dispose() {
    }

    public void draw(Graphics2D g2, double scale) {
        if (dropAreas != null) {
            int _x = dropAreas.getPadding();
            int _y = dropAreas.getPadding();
            int dragX = (int) (FileDrop.getMouseX() / scale);
            int dragY = (int) (FileDrop.getMouseY() / scale);

            DropArea highlightedArea = dropAreas.getDropArea(dragX, dragY);

            for (DropArea dropArea : dropAreas.getActiveDropAreas()) {
                int w = dropArea.getWidth();
                int h = dropArea.getHeight();

                g2.drawImage(dropArea.getIcon(), (int) ((dropAreas.getOffsetX() + _x) / scale), (int) ((dropAreas.getOffsetY() + _y) / scale), null);

                g2.setColor(Color.DARK_GRAY);
                g2.setStroke(new BasicStroke(2));

                if (dropArea == highlightedArea) {
                    g2.drawRoundRect((int) ((dropAreas.getOffsetX() + _x - 2) / scale), (int) ((dropAreas.getOffsetY() + _y - 2) / scale), (int) ((w + 5) / scale), (int) ((h + 5) / scale), (int) (12 / scale), (int) (12 / scale));
                    if (dropAreas.getOrientation() == DropAreas.Orientation.VERTICAL) {
                        g2.drawString(dropArea.getText(), (int) ((dropAreas.getOffsetX() + _x + w + 10) / scale), (int) ((dropAreas.getOffsetY() + _y + 8) / scale));
                    } else {
                        g2.drawString(dropArea.getText(), (int) ((dropAreas.getOffsetX() + _x) / scale), (int) ((dropAreas.getOffsetY() + _y - 8) / scale));
                    }
                }

                if (dropAreas.getOrientation() == DropAreas.Orientation.HORIZONTAL) {
                    _x += dropArea.getWidth() + dropAreas.getPadding();
                } else {
                    _y += dropArea.getHeight() + dropAreas.getPadding();
                }
            }
        }
    }

    public DropAreas getDropAreas() {
        return dropAreas;
    }
}
