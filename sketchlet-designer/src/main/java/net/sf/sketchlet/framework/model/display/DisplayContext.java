package net.sf.sketchlet.framework.model.display;

import net.sf.sketchlet.designer.playback.displays.PageClip;

import java.awt.*;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class DisplayContext {
    private int x;
    private int y;
    private int width = Toolkit.getDefaultToolkit().getScreenSize().width;
    private int height = Toolkit.getDefaultToolkit().getScreenSize().height;

    private Vector<PageClip> clips = new Vector<PageClip>();

    public DisplayContext() {
        getClips().add(new PageClip());
        getClips().add(new PageClip());
        getClips().add(new PageClip());
        getClips().add(new PageClip());
        getClips().add(new PageClip());
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Vector<PageClip> getClips() {
        return clips;
    }

    public void setClips(Vector<PageClip> clips) {
        this.clips = clips;
    }
}
