/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.programming.timers.curves;

import net.sf.sketchlet.common.geom.NaturalCubicSpline;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.programming.timers.Timers;
import net.sf.sketchlet.designer.ui.timers.curve.CurveFrame;
import net.sf.sketchlet.designer.ui.timers.curve.CurvePanel;

import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class Curve {

    public Vector<CurveSegment> segments = new Vector<CurveSegment>();
    public String columnNames[] = {Language.translate("Segment"), Language.translate("Start Time"), Language.translate("End Time"), Language.translate("Start Value"), Language.translate("End Value")};
    public CurvePanel panel;
    public String name = "New Curve";
    public StiffnessCurve stiffnessCurve = new StiffnessCurve(this);
    public double startValue = 0.0;

    public Curve() {
        segments.add(new CurveSegment(1.0, 1.0, "", "", "", ""));
    }

    public void addSegment(double endTime, double value) {
        CurveSegment cs = new CurveSegment(endTime, value, "", "", "", "");
        segments.add(cs);
    }

    public double[] getSpeed() {
        double[] speeds = new double[segments.size()];

        double prevTime = 0.0;
        double prevValue = startValue;
        int i = 0;
        double maxSpeed = 0.0;
        for (CurveSegment seg : segments) {
            double speed = Math.abs((seg.relativeValue - prevValue) / (seg.endTime - prevTime));
            speeds[i] = speed;
            if (speed > maxSpeed) {
                maxSpeed = speed;
            }
            prevTime = seg.endTime;
            prevValue = seg.relativeValue;
            i++;
        }

        for (int s = 0; s < speeds.length; s++) {
            speeds[s] /= maxSpeed;
        }

        return speeds;
    }

    public void save(PrintWriter out) {
        String oldName = name;
        if (panel != null) {
            this.name = panel.frame.nameField.getText();
        }

        if (!oldName.equalsIgnoreCase(name)) {
            Timers.globalTimers.replaceCurveReferences(oldName, name);
        }
        out.println("<curve>");
        out.println("<name>" + this.name + "</name>");
        out.println("<start-value>" + this.startValue + "</start-value>");
        out.println("<segments>");
        for (CurveSegment s : segments) {
            out.println("<segment>");
            out.println("<end-time>" + s.endTime + "</end-time>");
            out.println("<relative-value>" + s.relativeValue + "</relative-value>");
            /*
            if (!s.minDuration.isEmpty()) {
            out.println("<min-duration>" + s.minDuration + "</min-duration>");
            }
            if (!s.maxDuration.isEmpty()) {
            out.println("<max-duration>" + s.maxDuration + "</max-duration>");
            }
            if (!s.startAfter.isEmpty()) {
            out.println("<start-after>" + s.startAfter + "</start-after>");
            }
            if (!s.finishBefore.isEmpty()) {
            out.println("<finish-before>" + s.finishBefore + "</finish-before>");
            }
             */
            out.println("</segment>");
        }
        out.println("</segments>");
        out.println("<constraints>");
        for (StiffnessSegment cs : this.stiffnessCurve.segments) {
            out.println("<constraint-segment end-time='" + cs.endTime + "' min-duration='" + cs.minDuration + "' max-duration='" + cs.maxDuration + "'/>");
        }
        out.println("</constraints>");
        out.println("</curve>");
    }

    public double[][] getRelativeEndTimes() {
        double[][] result = new double[segments.size()][2];
        double prevTime = 0.0;
        int i = 0;
        for (CurveSegment seg : segments) {
            result[i][0] = seg.endTime;
            result[i][1] = seg.endTime - prevTime;
            prevTime = seg.endTime;
            i++;
        }
        return result;
    }

    public double getRelativeValue(double duration, double relIndex) {
        double absolute[][] = this.getAbsolute(duration);

        double prevTime = 0.0;
        double prevValue = startValue;
        for (int i = 0; i < absolute.length; i++) {
            if (relIndex >= prevTime && relIndex <= prevTime + absolute[i][0] && absolute[i][0] != 0) {
                return prevValue + (absolute[i][1] - prevValue) * (relIndex - prevTime) / (absolute[i][0]);
            }

            prevTime += absolute[i][0];
            prevValue = absolute[i][1];
        }

        return 0.0;
    }

    public double getRelativeValue(double relIndex) {
        double start = 0.0;
        double prevValue = startValue;
        for (CurveSegment seg : segments) {
            if (relIndex >= start && relIndex <= seg.endTime) {
                return prevValue + (seg.relativeValue - prevValue) * (relIndex - start) / (seg.endTime - start);
            }
            start = seg.endTime;
            prevValue = seg.relativeValue;
        }

        return relIndex;
    }

    boolean[] fixedTimes = null;

    public double[][] getAbsolute(double duration) {
        boolean fits = stiffnessCurve.getTotalMinDuration() < duration && stiffnessCurve.getTotalMaxDuration() > duration;

        Vector<double[]> points = new Vector<double[]>();
        double normalDuration[] = new double[stiffnessCurve.segments.size()];
        boolean constrained[] = new boolean[stiffnessCurve.segments.size()];
        double realDuration[] = new double[stiffnessCurve.segments.size()];
        double factors[] = new double[stiffnessCurve.segments.size()];
        if (fits) {
            int i = 0;
            double pt = 0.0;
            for (StiffnessSegment ss : stiffnessCurve.segments) {
                normalDuration[i] = (ss.endTime - pt) * duration;
                realDuration[i] = ss.getValue(normalDuration[i]);
                constrained[i] = normalDuration[i] != realDuration[i];
                pt = ss.endTime;
                i++;
            }

            double constrainedT = 0.0;
            double notConstrainedT = 0.0;
            for (i = 0; i < realDuration.length; i++) {
                if (!constrained[i]) {
                    notConstrainedT += realDuration[i];
                } else {
                    constrainedT += realDuration[i];
                }
            }

            double factor = notConstrainedT == 0 ? 1.0 : (duration - constrainedT) / notConstrainedT;
            for (i = 0; i < realDuration.length; i++) {
                if (!constrained[i]) {
                    realDuration[i] *= factor;
                    factors[i] = factor;
                } else {
                    factors[i] = normalDuration[i] == 0 ? 1.0 : (realDuration[i]) / (normalDuration[i]);
                }
            }

            double prevEndTime = 0.0;
            double prevRelValue = startValue;
            for (CurveSegment s : segments) {
                for (StiffnessSegment ss : stiffnessCurve.segments) {
                    if (prevEndTime < ss.endTime && s.endTime > ss.endTime && s.endTime - prevEndTime != 0) {
                        double data[] = new double[2];
                        data[0] = ss.endTime;
                        if (s.endTime - prevEndTime != 0) {
                            data[1] = prevRelValue + (s.relativeValue - prevRelValue) * (ss.endTime - prevEndTime) / (s.endTime - prevEndTime);
                            points.add(data);
                        }
                        prevEndTime = ss.endTime;
                        prevRelValue = data[1];
                    }
                }

                double data[] = new double[2];
                data[0] = s.endTime;
                data[1] = s.relativeValue;
                points.add(data);

                prevEndTime = s.endTime;
                prevRelValue = s.relativeValue;
            }

        } else {
            for (CurveSegment s : segments) {
                double data[] = new double[2];
                data[0] = s.endTime;
                data[1] = s.relativeValue;
                points.add(data);
            }
        }
        double result[][] = new double[points.size()][2];

        int i = 0;
        double prev = 0.0;

        for (double d[] : points) {
            result[i][0] = d[0] - prev;
            result[i][1] = d[1];

            if (fits) {
                int j = 0;
                double prevSegT = 0.0;
                for (StiffnessSegment ss : stiffnessCurve.segments) {
                    if (prev >= prevSegT) {
                        if (d[0] <= ss.endTime) {
                            double f = factors[j];
                            if (normalDuration[j] != 0) {
                                result[i][0] *= realDuration[j] / normalDuration[j];
                            }
                            break;
                        }
                    }
                    j++;
                    prevSegT = ss.endTime;
                }
            }

            prev = d[0];
            i++;
        }

        return result;
    }

    public double[][] getAbsolute2(double duration) {
        double relEndTimes[][] = getRelativeEndTimes();
        double[][] result = new double[segments.size()][2];
        double[][] resultRel = new double[segments.size()][2];
        fixedTimes = new boolean[segments.size()];

        double prevDuration = 0.0;
        int i = 0;
        for (CurveSegment seg : segments) {
            fixedTimes[i] = false;
            double absDur = (relEndTimes[i][0] - prevDuration) * duration;

            try {
                if (!seg.minDuration.isEmpty()) {
                    double minValue = Double.parseDouble(seg.minDuration);
                    if (absDur < minValue) {
                        fixedTimes[i] = true;
                        absDur = minValue;
                    }
                }
                if (!seg.maxDuration.isEmpty()) {
                    double maxValue = Double.parseDouble(seg.maxDuration);
                    if (absDur > maxValue) {
                        fixedTimes[i] = true;
                        absDur = maxValue;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            result[i][0] = absDur / duration;
            result[i][1] = seg.relativeValue;
            prevDuration = relEndTimes[i][0];
            i++;
        }

        double totalFixedTimes = 0.0;
        double totalUnfixedTimes = 0.0;
        for (int r = 0; r < result.length; r++) {
            if (fixedTimes[r]) {
                totalFixedTimes += result[r][0];
            } else {
                totalUnfixedTimes += result[r][0];
            }
        }
        if (totalFixedTimes < 1) {
            for (int r = 0; r < result.length; r++) {
                if (!fixedTimes[r]) {
                    result[r][0] = (1 - totalFixedTimes) * (result[r][0] / totalUnfixedTimes);
                }
            }
        } else {
            return resultRel;
        }

        return result;
    }

    public void drawAbsoluteCurve(double duration, Graphics2D g2, int x, int y, int w, int h) {
        double absolute[][] = this.getAbsolute(duration);

        int x1 = x;
        int y1 = y;
        double d = 0.0;
        for (int i = 0; i < absolute.length; i++) {
            int x2 = x1 + (int) (absolute[i][0] * w);
            int y2 = y - (int) (absolute[i][1] * h);

            g2.setColor(new Color(100, 100, 100, 80));
            d += absolute[i][0] * duration;
            g2.drawLine(x1, y, x1, y - h);
            g2.drawString(("" + d + "00000").substring(0, 4), x2 + 5, y - 5);

            g2.setColor(Color.BLACK);
            g2.drawLine(x1, y1, x2, y2);
            x1 = x2;
            y1 = y2;
        }
    }

    public void drawCurve(Graphics2D g2, int x, int y, int w, int h) {
        double value = startValue;
        double time = 0.0;

        int i = 0;
        double speeds[] = getSpeed();
        double array_x[] = new double[segments.size()];
        double array_y[] = new double[segments.size()];

        int x1 = x;
        int y1 = y - (int) (startValue * h);
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(x1 - 4, y1 - 4, 9, 9);

        NaturalCubicSpline ncs = new NaturalCubicSpline();
        ncs.addPoint(x1, y1);

        for (CurveSegment segment : segments) {
            array_x[i] = time;
            array_y[i] = value;
            x1 = x + (int) (time * w);
            y1 = y - (int) (value * h);
            time = segment.endTime;
            value = segment.relativeValue;
            int x2 = x + (int) (time * w);
            int y2 = y - (int) (value * h);

            if (panel.frame.checkSpeed.isSelected()) {
                g2.setColor(new Color(0, 100, 0, 40));
                double sH = speeds[i] * h;
                g2.fillRect(x1, y - (int) sH, x2 - x1, (int) sH);
            }
            if (panel != null && panel.selectedSegment == segment) {
                g2.setColor(Color.RED);
            } else {
                g2.setColor(Color.DARK_GRAY);
            }
            g2.drawLine(x1, y1, x2, y2);

            if (time <= 1.0) {
                g2.fillRect(x2 - 4, y2 - 4, 9, 9);

                g2.setColor(new Color(100, 100, 100, 80));
                g2.drawLine(x, y2, x + w, y2);
                g2.drawLine(x2, y, x2, y - h);
            }
            i++;

            ncs.addPoint(x2, y2);
        }

        ncs.paint(g2);
    }

    public AbstractTableModel getTableModel() {
        return new AbstractTableModel() {

            public int getColumnCount() {
                return columnNames.length;
            }

            public String getColumnName(int col) {
                return columnNames[col];
            }

            public int getRowCount() {
                return segments.size();
            }

            public Object getValueAt(int row, int col) {
                CurveSegment seg = segments.elementAt(row);
                CurveSegment prevSegment = (row <= 0) ? null : segments.elementAt(row - 1);
                switch (col) {
                    case 0:
                        return (row + 1) + "";
                    case 1:
                        if (row <= 0) {
                            return "0.0";
                        } else {
                            return (prevSegment.endTime + "00000").substring(0, 5);
                        }
                    case 2:
                        return (seg.endTime + "00000").substring(0, 5);
                    case 3:
                        if (row <= 0) {
                            return "" + startValue;
                        } else {
                            return (prevSegment.relativeValue + "00000").substring(0, 5);
                        }
                    case 4:
                        return (seg.relativeValue + "00000").substring(0, 5);
                    /*case 3:
                    return seg.minDuration;
                    case 4:
                    return seg.maxDuration;
                    case 5:
                    return seg.startAfter;
                    case 6:
                    return seg.finishBefore;*/
                }
                return "";
            }

            public void setValueAt(Object value, int row, int col) {
                CurveSegment seg = segments.elementAt(row);
                try {
                    switch (col) {
                        case 2:
                            if (seg.endTime < 1 && seg.endTime > 0) {
                                seg.endTime = Double.parseDouble(value.toString());
                                sort();
                                this.fireTableDataChanged();
                            }
                            break;
                        case 4:
                            double v = Double.parseDouble(value.toString());
                            if (v >= 0 && v <= 1) {
                                seg.relativeValue = v;
                            }
                            break;
                    }
                } catch (Exception e) {
                }

                panel.repaint();
                panel.frame.curvePreviewPanel.repaint();
            }

            public boolean isCellEditable(int row, int col) {
                return col == 2 || col == 4;
            }
        };
    }

    public void sort() {
        Collections.sort(segments, new Comparator() {

            public int compare(Object o1, Object o2) {
                if (o1 instanceof CurveSegment && o2 instanceof CurveSegment) {
                    CurveSegment seg1 = (CurveSegment) o1;
                    CurveSegment seg2 = (CurveSegment) o2;

                    if (seg1.endTime < seg2.endTime) {
                        return -1;
                    } else if (seg1.endTime > seg2.endTime) {
                        return 1;
                    } else {
                        return 0;
                    }

                }
                return 0;
            }

            public boolean equals(Object obj) {
                return false;
            }
        });

    }

    public static void main(String args[]) {
        CurveFrame.main(args);
    }
}
