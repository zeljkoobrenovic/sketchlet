/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.regions.renderer;

import net.sf.sketchlet.common.geom.DistancePointSegment;
import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.data.TrajectoryPoint;
import net.sf.sketchlet.designer.editor.EditorMode;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.events.region.ActiveRegionMouseHandler;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class TrajectoryDrawingLayer extends DrawingLayer {
    private static final Logger log = Logger.getLogger(TrajectoryDrawingLayer.class);

    public TrajectoryDrawingLayer(ActiveRegion region) {
        super(region);
    }

    public void dispose() {
        region = null;
    }

    boolean bedub = true;
    Ellipse2D ellipse = null;
    Point _p;
    Point _p2;

    public void draw(Graphics2D g2, Component component, boolean bPlayback) {
        if (region.strTrajectory1.isEmpty()) {
            return;
        }
        EditorMode mode = SketchletEditor.editorPanel.mode;
        String strTrajectory1 = region.processText(region.strTrajectory1);
        String strTrajectory2 = region.processText(region.strTrajectory2);
        String points[] = strTrajectory1.split("\n");
        String points2[] = strTrajectory2.split("\n");

        Polygon p = getPolygon(points);
        Polygon p2 = getPolygon(points2);
        if (p.npoints > 0) {
            if (!region.inTrajectoryMode) {
                g2.setColor(new Color(127, 127, 127, 70));
                g2.setStroke(new BasicStroke(1));
                if (_p != null) {
                    g2.fillOval(_p.x - 14, _p.y - 14, 29, 29);
                }
                g2.setColor(new Color(127, 0, 0, 70));
                if (_p2 != null) {
                    g2.fillOval(_p2.x - 14, _p2.y - 14, 29, 29);
                }
                if (ellipse != null) {
                    g2.draw(ellipse);
                }
            }
        }
        if (p.npoints > 0) {
            float dash1[] = {10.0f};
            float thick = region.parent.selectedRegions != null && region.parent.selectedRegions.contains(region) ? 3.0f : 1.0f;
            BasicStroke dashed = new BasicStroke(thick, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
            g2.setStroke(dashed);

            if (mode == EditorMode.ACTIONS) {
                g2.setColor(new Color(255, 0, 0, 70));
                g2.drawPolyline(p.xpoints, p.ypoints, p.npoints);
                g2.setColor(new Color(0, 0, 255, 70));
                g2.drawPolyline(p2.xpoints, p2.ypoints, p2.npoints);
            } else {
                g2.setColor(new Color(255, 0, 0, 50));
                g2.drawPolyline(p.xpoints, p.ypoints, p.npoints);
                g2.setColor(new Color(0, 0, 255, 50));
                g2.drawPolyline(p2.xpoints, p2.ypoints, p2.npoints);
            }

        }
    }

    public Polygon getPolygon(String points[]) {
        Polygon p = new Polygon();
        for (int i = 0; i < points.length; i++) {
            String strPoint = points[i].trim();
            strPoint.replace("\t", " ");

            String coord[] = strPoint.split(" ");
            if (coord.length >= 2) {
                try {
                    int x = Integer.parseInt(coord[0]);
                    int y = Integer.parseInt(coord[1]);

                    p.addPoint(x, y);
                } catch (Exception e) {
                    log.debug("Trajectory conversion error.");
                }
            }
        }
        return p;
    }

    public Point getTrajectoryPoint(double position) {
        Vector<TrajectoryPoint> tps = region.createTrajectoryVector();
        Vector<TrajectoryPoint> tps2 = region.createTrajectory2Vector();

        double totalDistance = 0.0;

        TrajectoryPoint prevTP = null;
        for (TrajectoryPoint tp : tps) {
            if (prevTP != null) {
                double dx = tp.x - prevTP.x;
                double dy = tp.y - prevTP.y;

                double d = Math.sqrt(dx * dx + dy * dy);
                totalDistance += d;
            }
            prevTP = tp;
        }

        double relDistance = totalDistance * position;

        if (tps.size() > 1) {

            if (position <= 10 / totalDistance) {
                Point p = new Point((tps.elementAt(0).x + tps.elementAt(1).x) / 2, (tps.elementAt(0).y + tps.elementAt(1).y) / 2);
                if (tps.size() > 0) {
                    calculateOrientation(tps, tps2, p, tps.elementAt(0), tps.elementAt(1));
                }
                return p;
            }

            if (position >= 1.0) {
                Point p = new Point(tps.lastElement().x, tps.lastElement().y);
                if (tps.size() > 0) {
                    calculateOrientation(tps, tps2, p, tps.elementAt(0), tps.elementAt(1));
                }
                return p;
            }


            TrajectoryPoint prevTP2 = null;
            double distance = 0.0;
            for (TrajectoryPoint tp2 : tps) {
                if (prevTP2 != null) {
                    double dx = tp2.x - prevTP2.x;
                    double dy = tp2.y - prevTP2.y;

                    double d = Math.sqrt(dx * dx + dy * dy);
                    if (relDistance >= distance && relDistance <= distance + d) {
                        double da = Math.atan2(-prevTP2.y + tp2.y, -prevTP2.x + tp2.x);
                        int x = (int) (prevTP2.x + (relDistance - distance) * Math.cos(da));
                        int y = (int) (prevTP2.y + (relDistance - distance) * Math.sin(da));
                        Point p = new Point(x, y);
                        calculateOrientation(tps, tps2, p, prevTP2, tp2);
                        return p;
                    }
                    distance += d;
                }
                prevTP2 = tp2;
            }
        }

        return null;
    }

    public Point getTrajectoryPoint2(double position) {
        Point p1 = getTrajectoryPoint(position);

        int w = region.x2 - region.x1;
        int h = region.y2 - region.y1;
        double p1_x = region.x1 + w * region.center_rotation_x;
        double p1_y = region.y1 + h * region.center_rotation_y;
        double p2_x = region.x1 + w * region.trajectory2_x;
        double p2_y = region.y1 + h * region.trajectory2_y;
        double dx = p1_x - p2_x;
        double dy = p1_y - p2_y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        double x = p1.x - distance * Math.cos(region.rotation);
        double y = p1.y - distance * Math.sin(region.rotation);

        return new Point((int) x, (int) y);
    }

    public void calculateOrientation(Vector<TrajectoryPoint> tps1, Vector<TrajectoryPoint> tps2, Point p, TrajectoryPoint prevTp, TrajectoryPoint tp) {
        if (region.mouseHandler.selectedCorner == ActiveRegionMouseHandler.TRAJECTORY2_POINT) {
            return;
        }
        if (region.center_rotation_x == region.trajectory2_x && region.center_rotation_y == region.trajectory2_y) {
            getTrajectoryOrientation(prevTp, tp);
        } else {
            if (tps2.size() > 1) {
                this.calculateOrientation2(tps2, p, prevTp, tp);
            } else {
                this.calculateOrientation1(tps1, p, prevTp, tp);
            }
        }
    }

    public void calculateOrientation1(Vector<TrajectoryPoint> tps1, Point p, TrajectoryPoint prevTp, TrajectoryPoint tp) {
        Vector<Point> points = new Vector<Point>();
        int n;

        for (n = tps1.size() - 1; n >= 0; n--) {
            if (tps1.elementAt(n) == prevTp) {
                break;
            }
        }
        int w = region.x2 - region.x1;
        int h = region.y2 - region.y1;
        double p1_x = region.x1 + w * region.center_rotation_x;
        double p1_y = region.y1 + h * region.center_rotation_y;
        double p2_x = region.x1 + w * region.trajectory2_x;
        double p2_y = region.y1 + h * region.trajectory2_y;
        double dx = p1_x - p2_x;
        double dy = p1_y - p2_y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        /*if (distance > relDistance * totalDistance) {
        trajectoryOrientationFromPoint = 0;
        return;
        }*/

        Point __p = p;
        for (int i = n; i >= 0; i--) {
            TrajectoryPoint _tp = tps1.elementAt(i);
            int x1 = __p.x;
            int y1 = __p.y;
            int x2, y2;
            x2 = _tp.x;
            y2 = _tp.y;
            lineBresenham(x1, y1, x2, y2, points);
            __p = _tp.getPoint();
        }

        ellipse = new Ellipse2D.Double(p1_x - distance, p1_y - distance, distance * 2, distance * 2);

        _p = findClosestPoint(points, ellipse);
        _p2 = _p;
        if (tps1.indexOf(prevTp) == 0) {
            _p2 = prevTp.getPoint();
        }
        if (_p != null) {
            dx = p1_x - _p2.x;
            dy = p1_y - _p2.y;
            double da = Math.atan2(p2_y - p1_y, p2_x - p1_x) + Math.PI;
            trajectoryOrientationFromPoint = Math.atan2(dy, dx) - da;
        } else {
            getTrajectoryOrientation(tp, prevTp);
        }
    }

    public void calculateOrientation2(Vector<TrajectoryPoint> tps2, Point p, TrajectoryPoint prevTp, TrajectoryPoint tp) {
        Vector<Point> points = new Vector<Point>();
        TrajectoryPoint _tp = tps2.firstElement();
        Point __p = new Point(_tp.x, _tp.y);
        for (int i = 1; i < tps2.size(); i++) {
            _tp = tps2.elementAt(i);
            int x1 = __p.x;
            int y1 = __p.y;
            int x2 = _tp.x;
            int y2 = _tp.y;
            lineBresenham(x1, y1, x2, y2, points);
            __p = new Point(_tp.x, _tp.y);
        }

        int w = region.x2 - region.x1;
        int h = region.y2 - region.y1;
        double p1_x = region.x1 + w * region.center_rotation_x;
        double p1_y = region.y1 + h * region.center_rotation_y;
        double p2_x = region.x1 + w * region.trajectory2_x;
        double p2_y = region.y1 + h * region.trajectory2_y;
        double dx = p1_x - p2_x;
        double dy = p1_y - p2_y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        ellipse = new Ellipse2D.Double(p1_x - distance, p1_y - distance, distance * 2, distance * 2);

        _p = findClosestPoint2(points, ellipse);
        if (_p != null) {
            dx = p.x - _p.x;
            dy = p.y - _p.y;
            double da = Math.atan2(p2_y - p1_y, p2_x - p1_x) + Math.PI;
            trajectoryOrientationFromPoint = Math.atan2(dy, dx) - da;
        } else {
            getTrajectoryOrientation(tp, prevTp);
        }
    }

    public double trajectoryPositionFromPoint = 0.0;
    public double trajectoryPositionFromPoint2 = 0.0;
    public double trajectoryOrientationFromPoint = 0.0;

    public double getTrajectoryOrientation(TrajectoryPoint tp1, TrajectoryPoint tp2) {
        int w = region.x2 - region.x1;
        int h = region.y2 - region.y1;
        double p1_x = region.x1 + w * region.center_rotation_x;
        double p1_y = region.y1 + h * region.center_rotation_y;
        double p2_x = region.x1 + w * region.trajectory2_x;
        double p2_y = region.y1 + h * region.trajectory2_y;
        double dx = p1_x - p2_x;
        double dy = p1_y - p2_y;
        double da = Math.atan2(p2_y - p1_y, p2_x - p1_x) + Math.PI;
        trajectoryOrientationFromPoint = Math.atan2((double) tp2.y - tp1.y, tp2.x - tp1.x) - da;

        return trajectoryOrientationFromPoint;
    }

    public void lineBresenham(int x0, int y0, int x1, int y1, Vector<Point> points) {
        int dy = y1 - y0;
        int dx = x1 - x0;
        int stepx, stepy;

        if (dy < 0) {
            dy = -dy;
            stepy = -1;
        } else {
            stepy = 1;
        }
        if (dx < 0) {
            dx = -dx;
            stepx = -1;
        } else {
            stepx = 1;
        }
        dy <<= 1;                                                  // dy is now 2*dy
        dx <<= 1;                                                  // dx is now 2*dx

        points.add(new Point(x0, y0));
        if (dx > dy) {
            int fraction = dy - (dx >> 1);                         // same as 2*dy - dx
            while (x0 != x1) {
                if (fraction >= 0) {
                    y0 += stepy;
                    fraction -= dx;                                // same as fraction -= 2*dx
                }
                x0 += stepx;
                fraction += dy;                                    // same as fraction -= 2*dy
                points.add(new Point(x0, y0));
            }
        } else {
            int fraction = dx - (dy >> 1);
            while (y0 != y1) {
                if (fraction >= 0) {
                    x0 += stepx;
                    fraction -= dy;
                }
                y0 += stepy;
                fraction += dx;
                points.add(new Point(x0, y0));
            }
        }
    }

    public static Point findClosestPoint(Vector<Point> points, Ellipse2D oval) {
        double pointToCenter, dy, dx, theta, centerToCurve, pointToCurve, min = Double.MAX_VALUE;
        Point minp = null;
        for (int j = 0; j < points.size(); j++) {
            Point p = points.get(j);                          // j2se 1.5
            pointToCenter = p.distance(oval.getCenterX(), oval.getCenterY());
            dy = p.y - oval.getCenterY();
            dx = p.x - oval.getCenterX();
            theta = Math.atan2(dy, dx);
            centerToCurve = distanceCenterToCurve(oval, theta);
            pointToCurve = Math.abs(pointToCenter - centerToCurve);
            if (Math.abs(pointToCurve) < 1) {
                return p;
            } else if (pointToCurve < min) {
                minp = p;
                min = pointToCurve;
            }
        }

        return minp == null && points.size() > 0 ? points.firstElement() : minp;
    }

    public static Point findClosestPoint2(Vector<Point> points, Ellipse2D oval) {
        double pointToCenter, dy, dx, theta, centerToCurve, pointToCurve, min = Double.MAX_VALUE;
        Point minp = null;
        for (int j = 0; j < points.size(); j++) {
            Point p = points.get(j);                          // j2se 1.5
            pointToCenter = p.distance(oval.getCenterX(), oval.getCenterY());
            dy = p.y - oval.getCenterY();
            dx = p.x - oval.getCenterX();
            theta = Math.atan2(dy, dx);
            centerToCurve = distanceCenterToCurve(oval, theta);
            pointToCurve = Math.abs(pointToCenter - centerToCurve);
            if (Math.abs(pointToCurve) < 1) {
                return p;
            } else if (pointToCurve < min) {
                minp = p;
                min = pointToCurve;
            }
        }

        return minp;
    }

    private static double distanceCenterToCurve(Ellipse2D oval, double theta) {
        Rectangle r = oval.getBounds();
        double e = Math.sqrt(1.0 - (double) (r.height * r.height) / (r.width * r.width)); // 22
        double divisor = 1.0 - e * e * Math.cos(theta) * Math.cos(theta);
        double radial = (r.width / 2) * Math.sqrt((1.0 - e * e) / divisor);            // 27
        return radial;
    }

    public Point getClosestTrajectoryPoint(Point point) {
        Point p = point;
        String strTrajectory1 = region.processText(region.strTrajectory1);
        String points[] = strTrajectory1.split("\n");

        double distance = 2000.0;
        trajectoryPositionFromPoint = 0.0;

        Vector<TrajectoryPoint> tps = region.createTrajectoryVector();
        Vector<TrajectoryPoint> tps2 = region.createTrajectory2Vector();
        int i = 0;

        int index = -1;

        TrajectoryPoint prevTP = tps.size() > 0 ? tps.firstElement() : null;
        double totalDistance = 0.0;
        for (int j = 1; j < tps.size(); j++) {
            TrajectoryPoint tp = tps.elementAt(j);
            totalDistance += Math.sqrt((tp.x - prevTP.x) * (tp.x - prevTP.x) + (tp.y - prevTP.y) * (tp.y - prevTP.y));
            prevTP = tp;
        }
        double pointDistance = 0.0;
        prevTP = tps.size() > 0 ? tps.firstElement() : null;
        for (int j = 1; j < tps.size(); j++) {
            TrajectoryPoint tp = tps.elementAt(j);
            if (tp.x == prevTP.x && tp.y == prevTP.y) {
                continue;
            }

            double d = DistancePointSegment.distanceToSegment(new Point(prevTP.x, prevTP.y), new Point(tp.x, tp.y), point);
            if (d < distance) {
                distance = d;

                Point2D p2d = DistancePointSegment.closestPoint(new Point(prevTP.x, prevTP.y), new Point(tp.x, tp.y), point);
                p = new Point((int) p2d.getX(), (int) p2d.getY());
                //trajectoryPositionFromPoint = (double) i / points.length;
                double _dx = Math.sqrt((p.getX() - prevTP.x) * (p.getX() - prevTP.x) + (p.getY() - prevTP.y) * (p.getY() - prevTP.y));
                trajectoryPositionFromPoint = (pointDistance + _dx) / totalDistance;
                if (i == points.length - 1) {
                    trajectoryPositionFromPoint = 1.0;
                }
                //getTrajectoryPoint((double) i * points.length);
                getTrajectoryPoint(trajectoryPositionFromPoint);
                index = i;
            }
            pointDistance += Math.sqrt((tp.x - prevTP.x) * (tp.x - prevTP.x) + (tp.y - prevTP.y) * (tp.y - prevTP.y));
            prevTP = tp;
            i++;
        }

        if (tps.size() >= 2) {
            if (index == tps.size() - 1) {
                calculateOrientation(tps, tps2, p, tps.elementAt(index - 1), tps.elementAt(index));
            } else {
                if (index <= 0) {
                    index = 0;
                }
                if (tps2.size() > 1) {
                    calculateOrientation(tps, tps2, p, tps.elementAt(index), tps.elementAt(index + 1));
                } else {
                    if (index <= 0) {
                        getTrajectoryOrientation(tps.elementAt(index), tps.elementAt(index + 1));
                    } else {
                        calculateOrientation(tps, tps2, p, tps.elementAt(index), tps.elementAt(index + 1));
                    }
                }
            }
        }
        calculateSecondPoint((tps2.size() > 1) ? tps2 : tps, p);
        return p;
    }

    public void calculateSecondPoint(Vector<TrajectoryPoint> tps, Point firstPoint) {
        TrajectoryPoint prevTP = tps.size() > 0 ? tps.firstElement() : null;
        double totalDistance = 0.0;
        for (int j = 1; j < tps.size(); j++) {
            TrajectoryPoint tp = tps.elementAt(j);
            totalDistance += Math.sqrt((tp.x - prevTP.x) * (tp.x - prevTP.x) + (tp.y - prevTP.y) * (tp.y - prevTP.y));
            prevTP = tp;
        }
        double pointDistance = 0.0;
        prevTP = tps.size() > 0 ? tps.firstElement() : null;
        int w = region.x2 - region.x1;
        int h = region.y2 - region.y1;
        double p1_x = region.x1 + w * region.center_rotation_x;
        double p1_y = region.y1 + h * region.center_rotation_y;
        double p2_x = region.x1 + w * region.trajectory2_x;
        double p2_y = region.y1 + h * region.trajectory2_y;
        double dx = p1_x - p2_x;
        double dy = p1_y - p2_y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        double x = firstPoint.x - distance * Math.cos(this.trajectoryOrientationFromPoint);
        double y = firstPoint.y - distance * Math.sin(this.trajectoryOrientationFromPoint);

        Point point2 = _p;
        _p2 = point2;
        trajectoryPositionFromPoint2 = 0.0;
        Point p = null;

        double mind = Double.MAX_VALUE;
        for (int j = 1; j < tps.size(); j++) {
            TrajectoryPoint tp = tps.elementAt(j);
            if (tp.x == prevTP.x && tp.y == prevTP.y) {
                continue;
            }

            if (point2 != null) {
                double d = DistancePointSegment.distanceToSegment(new Point(prevTP.x, prevTP.y), new Point(tp.x, tp.y), point2);
                if (d < mind) {
                    mind = d;

                    Point2D p2d = DistancePointSegment.closestPoint(new Point(prevTP.x, prevTP.y), new Point(tp.x, tp.y), point2);
                    p = new Point((int) p2d.getX(), (int) p2d.getY());
                    double _dx = Math.sqrt((p.getX() - prevTP.x) * (p.getX() - prevTP.x) + (p.getY() - prevTP.y) * (p.getY() - prevTP.y));
                    trajectoryPositionFromPoint2 = (pointDistance + _dx) / totalDistance;
                }
            }
            pointDistance += Math.sqrt((tp.x - prevTP.x) * (tp.x - prevTP.x) + (tp.y - prevTP.y) * (tp.y - prevTP.y));
            prevTP = tp;
        }
    }
}
