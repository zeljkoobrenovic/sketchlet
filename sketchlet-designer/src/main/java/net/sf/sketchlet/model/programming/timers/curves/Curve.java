/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.model.programming.timers.curves;

import net.sf.sketchlet.common.geom.NaturalCubicSpline;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.editor.ui.timers.curve.CurvePanel;
import net.sf.sketchlet.model.programming.timers.Timers;

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

    private Vector<CurveSegment> segments = new Vector<CurveSegment>();
    private String[] columnNames = {Language.translate("Segment"), Language.translate("Start Time"), Language.translate("End Time"), Language.translate("Start Value"), Language.translate("End Value")};
    private CurvePanel panel;
    private String name = "New Curve";
    private StiffnessCurve stiffnessCurve = new StiffnessCurve(this);
    private double startValue = 0.0;
    private boolean[] fixedTimes = null;

    public Curve() {
        getSegments().add(new CurveSegment(1.0, 1.0, "", "", "", ""));
    }

    public void addSegment(double endTime, double value) {
        CurveSegment cs = new CurveSegment(endTime, value, "", "", "", "");
        getSegments().add(cs);
    }

    public double[] getSpeed() {
        double[] speeds = new double[getSegments().size()];

        double prevTime = 0.0;
        double prevValue = getStartValue();
        int i = 0;
        double maxSpeed = 0.0;
        for (CurveSegment seg : getSegments()) {
            double speed = Math.abs((seg.getRelativeValue() - prevValue) / (seg.getEndTime() - prevTime));
            speeds[i] = speed;
            if (speed > maxSpeed) {
                maxSpeed = speed;
            }
            prevTime = seg.getEndTime();
            prevValue = seg.getRelativeValue();
            i++;
        }

        for (int s = 0; s < speeds.length; s++) {
            speeds[s] /= maxSpeed;
        }

        return speeds;
    }

    public void save(PrintWriter out) {
        String oldName = getName();
        if (getPanel() != null) {
            this.setName(getPanel().frame.nameField.getText());
        }

        if (!oldName.equalsIgnoreCase(getName())) {
            Timers.getGlobalTimers().replaceCurveReferences(oldName, getName());
        }
        out.println("<curve>");
        out.println("<name>" + this.getName() + "</name>");
        out.println("<start-value>" + this.getStartValue() + "</start-value>");
        out.println("<segments>");
        for (CurveSegment s : getSegments()) {
            out.println("<segment>");
            out.println("<end-time>" + s.getEndTime() + "</end-time>");
            out.println("<relative-value>" + s.getRelativeValue() + "</relative-value>");
            out.println("</segment>");
        }
        out.println("</segments>");
        out.println("<constraints>");
        for (StiffnessSegment cs : this.getStiffnessCurve().getSegments()) {
            out.println("<constraint-segment end-time='" + cs.getEndTime() + "' min-duration='" + cs.getMinDuration() + "' max-duration='" + cs.getMaxDuration() + "'/>");
        }
        out.println("</constraints>");
        out.println("</curve>");
    }

    public double[][] getRelativeEndTimes() {
        double[][] result = new double[getSegments().size()][2];
        double prevTime = 0.0;
        int i = 0;
        for (CurveSegment seg : getSegments()) {
            result[i][0] = seg.getEndTime();
            result[i][1] = seg.getEndTime() - prevTime;
            prevTime = seg.getEndTime();
            i++;
        }
        return result;
    }

    public double getRelativeValue(double duration, double relIndex) {
        double absolute[][] = this.getAbsolute(duration);

        double prevTime = 0.0;
        double prevValue = getStartValue();
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
        double prevValue = getStartValue();
        for (CurveSegment seg : getSegments()) {
            if (relIndex >= start && relIndex <= seg.getEndTime()) {
                return prevValue + (seg.getRelativeValue() - prevValue) * (relIndex - start) / (seg.getEndTime() - start);
            }
            start = seg.getEndTime();
            prevValue = seg.getRelativeValue();
        }

        return relIndex;
    }

    public double[][] getAbsolute(double duration) {
        boolean fits = getStiffnessCurve().getTotalMinDuration() < duration && getStiffnessCurve().getTotalMaxDuration() > duration;

        Vector<double[]> points = new Vector<double[]>();
        double normalDuration[] = new double[getStiffnessCurve().getSegments().size()];
        boolean constrained[] = new boolean[getStiffnessCurve().getSegments().size()];
        double realDuration[] = new double[getStiffnessCurve().getSegments().size()];
        double factors[] = new double[getStiffnessCurve().getSegments().size()];
        if (fits) {
            int i = 0;
            double pt = 0.0;
            for (StiffnessSegment ss : getStiffnessCurve().getSegments()) {
                normalDuration[i] = (ss.getEndTime() - pt) * duration;
                realDuration[i] = ss.getValue(normalDuration[i]);
                constrained[i] = normalDuration[i] != realDuration[i];
                pt = ss.getEndTime();
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
            double prevRelValue = getStartValue();
            for (CurveSegment s : getSegments()) {
                for (StiffnessSegment ss : getStiffnessCurve().getSegments()) {
                    if (prevEndTime < ss.getEndTime() && s.getEndTime() > ss.getEndTime() && s.getEndTime() - prevEndTime != 0) {
                        double data[] = new double[2];
                        data[0] = ss.getEndTime();
                        if (s.getEndTime() - prevEndTime != 0) {
                            data[1] = prevRelValue + (s.getRelativeValue() - prevRelValue) * (ss.getEndTime() - prevEndTime) / (s.getEndTime() - prevEndTime);
                            points.add(data);
                        }
                        prevEndTime = ss.getEndTime();
                        prevRelValue = data[1];
                    }
                }

                double data[] = new double[2];
                data[0] = s.getEndTime();
                data[1] = s.getRelativeValue();
                points.add(data);

                prevEndTime = s.getEndTime();
                prevRelValue = s.getRelativeValue();
            }

        } else {
            for (CurveSegment s : getSegments()) {
                double data[] = new double[2];
                data[0] = s.getEndTime();
                data[1] = s.getRelativeValue();
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
                for (StiffnessSegment ss : getStiffnessCurve().getSegments()) {
                    if (prev >= prevSegT) {
                        if (d[0] <= ss.getEndTime()) {
                            double f = factors[j];
                            if (normalDuration[j] != 0) {
                                result[i][0] *= realDuration[j] / normalDuration[j];
                            }
                            break;
                        }
                    }
                    j++;
                    prevSegT = ss.getEndTime();
                }
            }

            prev = d[0];
            i++;
        }

        return result;
    }

    public double[][] getAbsolute2(double duration) {
        double relEndTimes[][] = getRelativeEndTimes();
        double[][] result = new double[getSegments().size()][2];
        double[][] resultRel = new double[getSegments().size()][2];
        fixedTimes = new boolean[getSegments().size()];

        double prevDuration = 0.0;
        int i = 0;
        for (CurveSegment seg : getSegments()) {
            fixedTimes[i] = false;
            double absDur = (relEndTimes[i][0] - prevDuration) * duration;

            try {
                if (!seg.getMinDuration().isEmpty()) {
                    double minValue = Double.parseDouble(seg.getMinDuration());
                    if (absDur < minValue) {
                        fixedTimes[i] = true;
                        absDur = minValue;
                    }
                }
                if (!seg.getMaxDuration().isEmpty()) {
                    double maxValue = Double.parseDouble(seg.getMaxDuration());
                    if (absDur > maxValue) {
                        fixedTimes[i] = true;
                        absDur = maxValue;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            result[i][0] = absDur / duration;
            result[i][1] = seg.getRelativeValue();
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
        double value = getStartValue();
        double time = 0.0;

        int i = 0;
        double speeds[] = getSpeed();
        double array_x[] = new double[getSegments().size()];
        double array_y[] = new double[getSegments().size()];

        int x1 = x;
        int y1 = y - (int) (getStartValue() * h);
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(x1 - 4, y1 - 4, 9, 9);

        NaturalCubicSpline ncs = new NaturalCubicSpline();
        ncs.addPoint(x1, y1);

        for (CurveSegment segment : getSegments()) {
            array_x[i] = time;
            array_y[i] = value;
            x1 = x + (int) (time * w);
            y1 = y - (int) (value * h);
            time = segment.getEndTime();
            value = segment.getRelativeValue();
            int x2 = x + (int) (time * w);
            int y2 = y - (int) (value * h);

            if (getPanel().frame.checkSpeed.isSelected()) {
                g2.setColor(new Color(0, 100, 0, 40));
                double sH = speeds[i] * h;
                g2.fillRect(x1, y - (int) sH, x2 - x1, (int) sH);
            }
            if (getPanel() != null && getPanel().selectedSegment == segment) {
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
                return getColumnNames().length;
            }

            public String getColumnName(int col) {
                return getColumnNames()[col];
            }

            public int getRowCount() {
                return getSegments().size();
            }

            public Object getValueAt(int row, int col) {
                CurveSegment seg = getSegments().elementAt(row);
                CurveSegment prevSegment = (row <= 0) ? null : getSegments().elementAt(row - 1);
                switch (col) {
                    case 0:
                        return (row + 1) + "";
                    case 1:
                        if (row <= 0) {
                            return "0.0";
                        } else {
                            return (prevSegment.getEndTime() + "00000").substring(0, 5);
                        }
                    case 2:
                        return (seg.getEndTime() + "00000").substring(0, 5);
                    case 3:
                        if (row <= 0) {
                            return "" + getStartValue();
                        } else {
                            return (prevSegment.getRelativeValue() + "00000").substring(0, 5);
                        }
                    case 4:
                        return (seg.getRelativeValue() + "00000").substring(0, 5);
                }
                return "";
            }

            public void setValueAt(Object value, int row, int col) {
                CurveSegment seg = getSegments().elementAt(row);
                try {
                    switch (col) {
                        case 2:
                            if (seg.getEndTime() < 1 && seg.getEndTime() > 0) {
                                seg.setEndTime(Double.parseDouble(value.toString()));
                                sort();
                                this.fireTableDataChanged();
                            }
                            break;
                        case 4:
                            double v = Double.parseDouble(value.toString());
                            if (v >= 0 && v <= 1) {
                                seg.setRelativeValue(v);
                            }
                            break;
                    }
                } catch (Exception e) {
                }

                getPanel().repaint();
                getPanel().frame.curvePreviewPanel.repaint();
            }

            public boolean isCellEditable(int row, int col) {
                return col == 2 || col == 4;
            }
        };
    }

    public void sort() {
        Collections.sort(getSegments(), new Comparator() {

            public int compare(Object o1, Object o2) {
                if (o1 instanceof CurveSegment && o2 instanceof CurveSegment) {
                    CurveSegment seg1 = (CurveSegment) o1;
                    CurveSegment seg2 = (CurveSegment) o2;

                    if (seg1.getEndTime() < seg2.getEndTime()) {
                        return -1;
                    } else if (seg1.getEndTime() > seg2.getEndTime()) {
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

    public Vector<CurveSegment> getSegments() {
        return segments;
    }

    public void setSegments(Vector<CurveSegment> segments) {
        this.segments = segments;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }

    public CurvePanel getPanel() {
        return panel;
    }

    public void setPanel(CurvePanel panel) {
        this.panel = panel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StiffnessCurve getStiffnessCurve() {
        return stiffnessCurve;
    }

    public double getStartValue() {
        return startValue;
    }

    public void setStartValue(double startValue) {
        this.startValue = startValue;
    }
}
