/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.data;

import java.awt.*;

/**
 * @author zobrenovic
 */
public class TrajectoryPoint {

    public int x;
    public int y;
    public int time;

    public TrajectoryPoint(int x, int y, int time) {
        this.x = x;
        this.y = y;
        this.time = time;
    }

    public Point getPoint() {
        return new Point(x, y);
    }
}
