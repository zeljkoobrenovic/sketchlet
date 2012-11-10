/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.programming.timers.curves;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.ui.timers.curve.CurveFrame;
import net.sf.sketchlet.designer.ui.timers.curve.StiffnessPanel;

import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class StiffnessCurve {

    public Vector<StiffnessSegment> segments = new Vector<StiffnessSegment>();
    public String columnNames[] = {Language.translate("Segment"), Language.translate("Start Time"), Language.translate("End Time"), Language.translate("Min Duration"), Language.translate("Max Duration")};
    public String name = "New Curve";
    public Curve curve;
    public StiffnessPanel panel;

    public StiffnessCurve(Curve curve) {
        segments.add(new StiffnessSegment(0.333, "", ""));
        segments.add(new StiffnessSegment(0.667, "", ""));
        segments.add(new StiffnessSegment(1.0, "", ""));
        this.curve = curve;
    }

    public void addSegment(double endTime, double value) {
        StiffnessSegment cs = new StiffnessSegment(endTime, "", "");
        segments.add(cs);
    }

    public double getMaxDuration() {
        double max = 0.0;
        for (StiffnessSegment seg : segments) {
            try {
                double value = Double.parseDouble(seg.maxDuration);
                max = Math.max(value, max);
                value = Double.parseDouble(seg.minDuration);
                max = Math.max(value, max);
            } catch (Exception e) {
            }
        }
        return max;
    }

    public double getTotalMinDuration() {
        double min = 0.0;
        for (StiffnessSegment seg : segments) {
            try {
                if (!seg.minDuration.isEmpty()) {
                    double value = Double.parseDouble(seg.minDuration);
                    min += value;
                }
            } catch (Exception e) {
            }
        }
        return min;
    }

    public double getTotalMaxDuration() {
        double max = 0.0;
        for (StiffnessSegment seg : segments) {
            try {
                if (!seg.maxDuration.isEmpty()) {
                    double value = Double.parseDouble(seg.maxDuration);
                    max += value;
                } else {
                    return Double.MAX_VALUE;
                }
            } catch (Exception e) {
            }
        }
        return max <= 0.0 ? Double.MAX_VALUE : max;
    }

    public void drawCurve(Graphics2D g2, int x, int y, int w, int h) {

        double time = 0.0;

        double max = getMaxDuration();

        int i = 0;
        double array_x[] = new double[segments.size()];
        double array_y[] = new double[segments.size()];
        for (StiffnessSegment segment : segments) {
            array_x[i] = time;
            int x1 = x + (int) (time * w);
            int y1 = y - (int) (0.5 * h);
            time = segment.endTime;
            int x2 = x + (int) (time * w);
            int y2 = y - (int) (0.5 * h);

            g2.setColor(Color.DARK_GRAY);
            g2.drawLine(x1, y1, x2, y2);

            g2.setColor(new Color(0, 99, 0, 100));

            double start = 0.0;
            double end = 2.0;

            if (!segment.minDuration.isEmpty()) {
                try {
                    start = Double.parseDouble(segment.minDuration);
                    start /= max;
                } catch (Exception e) {
                }
            }
            if (!segment.maxDuration.isEmpty()) {
                try {
                    end = Double.parseDouble(segment.maxDuration);
                    end /= max;
                } catch (Exception e) {
                }
            }

            int _y1 = 20 + (int) ((1 - end) * h);
            int _h = (int) ((Math.max(start, end) - Math.min(start, end)) * h);
            g2.fillRect(x1, _y1, x2 - x1, _h);

            g2.setColor(Color.GREEN.darker());
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(x1, _y1, x2, _y1);
            g2.drawLine(x1, _y1 + _h, x2, _y1 + _h);
            g2.setStroke(new BasicStroke(1));

            g2.setColor(Color.DARK_GRAY);

            if (time < 1.0) {
                g2.fillRect(x2 - 4, y2 - 4, 9, 9);

                g2.setColor(new Color(100, 100, 100, 80));
                //g2.drawLine(x, y2, x + w, y2);
                g2.drawLine(x2, y, x2, y - h);
            }
            i++;
        }
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
                StiffnessSegment seg = segments.elementAt(row);
                StiffnessSegment prevSeg = (row > 0) ? segments.elementAt(row - 1) : null;
                switch (col) {
                    case 0:
                        return (row + 1) + "";
                    case 1:
                        return prevSeg == null ? "0.0" : (prevSeg.endTime + "00000").substring(0, 5);
                    case 2:
                        return (seg.endTime + "00000").substring(0, 5);
                    case 3:
                        return seg.minDuration;
                    case 4:
                        return seg.maxDuration;
                }
                return "";
            }

            public void setValueAt(Object value, int row, int col) {
                StiffnessSegment seg = segments.elementAt(row);
                switch (col) {
                    case 3:
                        seg.minDuration = (String) value;
                        break;
                    case 4:
                        seg.maxDuration = (String) value;
                        break;
                }

                if (panel != null) {
                    panel.repaint();
                }
            }

            public boolean isCellEditable(int row, int col) {
                return col >= 2;
            }
        };
    }

    public void sort() {
        Collections.sort(segments, new Comparator() {

            public int compare(Object o1, Object o2) {
                if (o1 instanceof StiffnessSegment && o2 instanceof StiffnessSegment) {
                    StiffnessSegment seg1 = (StiffnessSegment) o1;
                    StiffnessSegment seg2 = (StiffnessSegment) o2;

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
