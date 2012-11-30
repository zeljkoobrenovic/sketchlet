package net.sf.sketchlet.framework.model.programming.timers.curves;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.editor.ui.timers.curve.CurveFrame;
import net.sf.sketchlet.designer.editor.ui.timers.curve.StiffnessPanel;

import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class StiffnessCurve {

    private Vector<StiffnessSegment> segments = new Vector<StiffnessSegment>();
    private String[] columnNames = {Language.translate("Segment"), Language.translate("Start Time"), Language.translate("End Time"), Language.translate("Min Duration"), Language.translate("Max Duration")};
    private String name = "New Curve";
    private Curve curve;
    private StiffnessPanel panel;

    public StiffnessCurve(Curve curve) {
        getSegments().add(new StiffnessSegment(0.333, "", ""));
        getSegments().add(new StiffnessSegment(0.667, "", ""));
        getSegments().add(new StiffnessSegment(1.0, "", ""));
        this.setCurve(curve);
    }

    public void addSegment(double endTime, double value) {
        StiffnessSegment cs = new StiffnessSegment(endTime, "", "");
        getSegments().add(cs);
    }

    public double getMaxDuration() {
        double max = 0.0;
        for (StiffnessSegment seg : getSegments()) {
            try {
                double value = Double.parseDouble(seg.getMaxDuration());
                max = Math.max(value, max);
                value = Double.parseDouble(seg.getMinDuration());
                max = Math.max(value, max);
            } catch (Exception e) {
            }
        }
        return max;
    }

    public double getTotalMinDuration() {
        double min = 0.0;
        for (StiffnessSegment seg : getSegments()) {
            try {
                if (!seg.getMinDuration().isEmpty()) {
                    double value = Double.parseDouble(seg.getMinDuration());
                    min += value;
                }
            } catch (Exception e) {
            }
        }
        return min;
    }

    public double getTotalMaxDuration() {
        double max = 0.0;
        for (StiffnessSegment seg : getSegments()) {
            try {
                if (!seg.getMaxDuration().isEmpty()) {
                    double value = Double.parseDouble(seg.getMaxDuration());
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
        double array_x[] = new double[getSegments().size()];
        double array_y[] = new double[getSegments().size()];
        for (StiffnessSegment segment : getSegments()) {
            array_x[i] = time;
            int x1 = x + (int) (time * w);
            int y1 = y - (int) (0.5 * h);
            time = segment.getEndTime();
            int x2 = x + (int) (time * w);
            int y2 = y - (int) (0.5 * h);

            g2.setColor(Color.DARK_GRAY);
            g2.drawLine(x1, y1, x2, y2);

            g2.setColor(new Color(0, 99, 0, 100));

            double start = 0.0;
            double end = 2.0;

            if (!segment.getMinDuration().isEmpty()) {
                try {
                    start = Double.parseDouble(segment.getMinDuration());
                    start /= max;
                } catch (Exception e) {
                }
            }
            if (!segment.getMaxDuration().isEmpty()) {
                try {
                    end = Double.parseDouble(segment.getMaxDuration());
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
                return getColumnNames().length;
            }

            public String getColumnName(int col) {
                return getColumnNames()[col];
            }

            public int getRowCount() {
                return getSegments().size();
            }

            public Object getValueAt(int row, int col) {
                StiffnessSegment seg = getSegments().elementAt(row);
                StiffnessSegment prevSeg = (row > 0) ? getSegments().elementAt(row - 1) : null;
                switch (col) {
                    case 0:
                        return (row + 1) + "";
                    case 1:
                        return prevSeg == null ? "0.0" : (prevSeg.getEndTime() + "00000").substring(0, 5);
                    case 2:
                        return (seg.getEndTime() + "00000").substring(0, 5);
                    case 3:
                        return seg.getMinDuration();
                    case 4:
                        return seg.getMaxDuration();
                }
                return "";
            }

            public void setValueAt(Object value, int row, int col) {
                StiffnessSegment seg = getSegments().elementAt(row);
                switch (col) {
                    case 3:
                        seg.setMinDuration((String) value);
                        break;
                    case 4:
                        seg.setMaxDuration((String) value);
                        break;
                }

                if (getPanel() != null) {
                    getPanel().repaint();
                }
            }

            public boolean isCellEditable(int row, int col) {
                return col >= 2;
            }
        };
    }

    public void sort() {
        Collections.sort(getSegments(), new Comparator() {

            public int compare(Object o1, Object o2) {
                if (o1 instanceof StiffnessSegment && o2 instanceof StiffnessSegment) {
                    StiffnessSegment seg1 = (StiffnessSegment) o1;
                    StiffnessSegment seg2 = (StiffnessSegment) o2;

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

    public static void main(String args[]) {
        CurveFrame.main(args);
    }

    public Vector<StiffnessSegment> getSegments() {
        return segments;
    }

    public void setSegments(Vector<StiffnessSegment> segments) {
        this.segments = segments;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Curve getCurve() {
        return curve;
    }

    public void setCurve(Curve curve) {
        this.curve = curve;
    }

    public StiffnessPanel getPanel() {
        return panel;
    }

    public void setPanel(StiffnessPanel panel) {
        this.panel = panel;
    }
}
