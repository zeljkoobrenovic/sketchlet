/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.common.geom;

/**
 *
 * @author zobrenovic
 */
public class GraphicsTools {

    public static double[] findIntersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        double pt[] = new double[2];

        pt[0] = det(det(x1, y1, x2, y2), x1 - x2,
                det(x3, y3, x4, y4), x3 - x4) /
                det(x1 - x2, y1 - y2, x3 - x4, y3 - y4);
        pt[1] = det(det(x1, y1, x2, y2), y1 - y2,
                det(x3, y3, x4, y4), y3 - y4) /
                det(x1 - x2, y1 - y2, x3 - x4, y3 - y4);

        // return the valid intersection
        return pt;
    }

    static double det(double a, double b, double c, double d) {
        return a * d - b * c;
    }

    static class Pt {

        double x, y;

        Pt(double x, double y) {
            this.x = x;
            this.y = y;
        }

        void set(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public static void main(String args[]) {
        double[] p = GraphicsTools.findIntersection(100, 100, 500, 500, 123, 190, 700, 200);
        System.out.println(p[0] + " kkl " + p[1]);
    }
}
