/*
 * @(#)GeneralPathWriter.java
 *
 */
package net.sf.sketchlet.designer.editor.stroke.geom;

import org.apache.log4j.Logger;

import java.awt.geom.GeneralPath;

/**
 * This writes path data to an underlying <code>GeneralPath</code>.
 * <P>This also omits redundant path information, such as two consecutive
 * calls to lineTo() that go to the same point.
 * <P>Also this is safe to make several consecutive calls to <code>closePath()</code>
 * (the GeneralPath will only be closed once, unless data has been written in the meantime.)
 */
public class GeneralPathWriter extends PathWriter {
    private static final Logger log = Logger.getLogger(GeneralPathWriter.class);
    GeneralPath p;
    float lastX, lastY;
    boolean dataWritten = false;

    public GeneralPathWriter(GeneralPath p) {
        this.p = p;
    }

    public void flush() {
    }

    /**
     * This resets the underlying <code>GeneralPath</code>.
     */
    public void reset() {
        log.debug("reset()");
        p.reset();
        dataWritten = false;
    }

    public void curveTo(float cx1, float cy1, float cx2, float cy2, float x,
                        float y) {
        log.debug("curveTo( " + cx1 + ", " + cy1 + ", " + cx2 + ", " + cy2 + ", " + x + ", " + y + ")");

        p.curveTo(cx1, cy1, cx2, cy2, x, y);
        lastX = x;
        lastY = y;
        dataWritten = true;
    }

    public void lineTo(float x, float y) {
        if (equals(lastX, x) && equals(lastY, y)) {
            return;
        }
        log.debug("lineTo( " + x + ", " + y + ")");
        p.lineTo(x, y);
        lastX = x;
        lastY = y;
        dataWritten = true;
    }

    public void moveTo(float x, float y) {
        p.moveTo(x, y);
        log.debug("moveTo( " + x + ", " + y + ")");
        lastX = x;
        lastY = y;
        dataWritten = true;
    }

    public void quadTo(float cx, float cy, float x, float y) {
        p.quadTo(cx, cy, x, y);
        log.debug("quadTo( " + cx + ", " + cy + ", " + x + ", " + y + ")");
        lastX = x;
        lastY = y;
        dataWritten = true;
    }

    public void closePath() {
        if (dataWritten) {
            p.closePath();
            log.debug("closePath()");
            dataWritten = false;
        }
    }

    private static boolean equals(float z1, float z2) {
        float d = z2 - z1;
        if (d < 0) {
            d = -d;
        }
        if (d < .001f) {
            return true;
        }
        return false;
    }
}
