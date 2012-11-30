package net.sf.sketchlet.designer.editor.tool.stroke;

import java.awt.*;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

public class WobbleStroke implements Stroke {

    private float detail = 2;
    private float amplitude = 2;
    private float flatness = 1;
    private int size = 10;

    public WobbleStroke(int size, float detail, float amplitude) {
        this.size = size;
        this.detail = detail;
        this.amplitude = amplitude;
    }

    public Shape createStrokedShape(Shape shape) {
        GeneralPath result = new GeneralPath();
        shape = new BasicStroke(size).createStrokedShape(shape);
        PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), flatness);
        float points[] = new float[6];
        float moveX = 0, moveY = 0;
        float lastX = 0, lastY = 0;
        float thisX = 0, thisY = 0;
        int type = 0;
        float next = 0;

        int index = 0;

        while (!it.isDone()) {
            type = it.currentSegment(points);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    moveX = lastX = randomize(index++, points[0]);
                    moveY = lastY = randomize(index++, points[1]);
                    result.moveTo(moveX, moveY);
                    next = 0;
                    break;

                case PathIterator.SEG_CLOSE:
                    points[0] = moveX;
                    points[1] = moveY;

                case PathIterator.SEG_LINETO:
                    thisX = randomize(index++, points[0]);
                    thisY = randomize(index++, points[1]);
                    float dx = thisX - lastX;
                    float dy = thisY - lastY;
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);
                    if (distance >= next) {
                        float r = 1.0f / distance;
                        float angle = (float) Math.atan2(dy, dx);
                        while (distance >= next) {
                            float x = lastX + next * dx * r;
                            float y = lastY + next * dy * r;
                            result.lineTo(randomize(index++, x), randomize(index++, y));
                            next += detail;
                        }
                    }
                    next -= distance;
                    lastX = thisX;
                    lastY = thisY;
                    break;
            }
            it.next();
        }

        return result;
    }

    private float randomize(int index, float x) {
        return x + (float) rand[index % rand.length] * amplitude * 2 - 1;
    }

    private static double rand[] = new double[2000];

    static {
        randomize();
    }

    private static void randomize() {
        for (int i = 0; i < rand.length; i++) {
            rand[i] = Math.random();
        }
    }
}
