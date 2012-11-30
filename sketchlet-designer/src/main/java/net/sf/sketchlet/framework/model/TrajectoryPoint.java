package net.sf.sketchlet.framework.model;

import java.awt.*;

/**
 * @author zobrenovic
 */
public class TrajectoryPoint {

    private int x;
    private int y;
    private int time;

    public TrajectoryPoint(int x, int y, int time) {
        this.setX(x);
        this.setY(y);
        this.setTime(time);
    }

    public Point getPoint() {
        return new Point(getX(), getY());
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

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
