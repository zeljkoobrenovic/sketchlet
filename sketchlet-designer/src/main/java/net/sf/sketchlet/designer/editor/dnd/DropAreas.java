package net.sf.sketchlet.designer.editor.dnd;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zeljko
 */
public class DropAreas {
    private static final Logger log = Logger.getLogger(DropAreas.class);

    public enum Orientation {VERTICAL, HORIZONTAL}

    private List<DropArea> dropAreaList = new ArrayList<DropArea>();
    private int padding = 5;
    private int offsetX = 0;
    private int offsetY = 0;

    private Orientation orientation = Orientation.HORIZONTAL;

    public DropAreas(Orientation orientation) {
        this.setOrientation(orientation);
    }

    public DropAreas(Orientation orientation, int offsetX, int offsetY) {
        this.setOrientation(orientation);
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public DropArea getDropArea(int x, int y) {
        int _x = getPadding() + offsetX;
        int _y = getPadding() + offsetY;
        for (DropArea dropArea : getActiveDropAreas()) {
            if (dropArea.isActive()) {
                if (x >= _x && x <= _x + dropArea.getWidth() && y >= _y && y <= _y + dropArea.getHeight()) {
                    return dropArea;
                }
                if (getOrientation() == Orientation.HORIZONTAL) {
                    _x += dropArea.getWidth() + getPadding();
                } else {
                    _y += dropArea.getHeight() + getPadding();
                }
            }
        }

        return null;
    }

    public int getWidth() {
        int width = getPadding();
        for (DropArea dropArea : getActiveDropAreas()) {
            width += dropArea.getWidth() + getPadding();
        }

        return width;
    }

    public int getHeight() {
        int height = getPadding();
        for (DropArea dropArea : getActiveDropAreas()) {
            height += dropArea.getHeight() + getPadding();
        }

        return height;
    }

    public void addDropArea(DropArea dropArea) {
        dropAreaList.add(dropArea);
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public void setOffset(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public List<DropArea> getActiveDropAreas() {
        List<DropArea> activeDropAreaList = new ArrayList<DropArea>();
        for (DropArea dropArea : this.dropAreaList) {
            if (dropArea.isActive()) {
                activeDropAreaList.add(dropArea);
            }
        }
        return activeDropAreaList;
    }

    public int getPadding() {
        return padding;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

}
