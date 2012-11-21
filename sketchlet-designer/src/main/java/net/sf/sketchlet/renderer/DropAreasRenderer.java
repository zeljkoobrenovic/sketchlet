package net.sf.sketchlet.renderer;

import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.designer.editor.dnd.DropArea;
import net.sf.sketchlet.designer.editor.dnd.DropAreas;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: zeljko
 * Date: 18-11-12
 * Time: 21:31
 * To change this template use File | Settings | File Templates.
 */
public class DropAreasRenderer {
    private DropAreas dropAreas;

    public DropAreasRenderer(DropAreas dropAreas) {
        this.dropAreas = dropAreas;
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
                        g2.drawString(dropArea.getText(), (int) ((dropAreas.getOffsetX() + _x - 15) / scale), (int) ((dropAreas.getOffsetY() + _y + h) / scale));
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
}
