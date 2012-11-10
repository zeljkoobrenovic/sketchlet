/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor;

import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.util.RefreshTime;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Vector;

/* Rule.java is used by ScrollDemo.java. */
public class Rule extends JComponent {

    public static final int INCH = Toolkit.getDefaultToolkit().getScreenResolution();
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    public static final int SIZE = 20;
    public int orientation;
    public boolean isMetric;
    private int increment;
    private int units;
    MouseHandler mouseHandler = new MouseHandler();

    public Rule(int o, boolean m) {
        orientation = o;
        isMetric = m;
        setIncrementAndUnits();
        if (o == HORIZONTAL) {
            this.setPreferredWidth(5000);
        } else {
            this.setPreferredHeight(5000);
        }
        this.addMouseListener(mouseHandler);
        this.addMouseMotionListener(mouseHandler);
    }

    public void setIsMetric(boolean isMetric) {
        this.isMetric = isMetric;
        setIncrementAndUnits();
        repaint();
    }

    private void setIncrementAndUnits() {
        if (isMetric) {
            //units = (int) ((double) INCH / (double) 2.54); // dots per centimeter
            //increment = units;
        } else {
            //units = INCH;
            //increment = units / 2;
        }
        increment = 20;
    }

    public boolean isMetric() {
        return this.isMetric;
    }

    public int getIncrement() {
        return increment;
    }

    public void setPreferredHeight(int ph) {
        setPreferredSize(new Dimension(SIZE, ph));
    }

    public void setPreferredWidth(int pw) {
        setPreferredSize(new Dimension(pw, SIZE));
    }

    int l_x1, l_y1, l_x2, l_y2;
    Polygon p1, p2, p3, p4;

    public void calculateLimits(ActiveRegion region) {
        String strX1 = (String) region.limits[0][1];
        String strX2 = (String) region.limits[0][2];
        String strY1 = (String) region.limits[1][1];
        String strY2 = (String) region.limits[1][2];
        int _x1 = 0;
        int _y1 = 0;
        BufferedImage img = SketchletEditor.editorPanel.currentPage.images[0];
        int _x2 = img != null ? img.getWidth() : 2000;
        int _y2 = img != null ? img.getHeight() : 2000;
        try {
            if (strX1.length() > 0) {
                _x1 = (int) Double.parseDouble(strX1);
            }
        } catch (Exception e) {
        }
        try {
            if (strY1.length() > 0) {
                _y1 = (int) Double.parseDouble(strY1);
            }
        } catch (Exception e) {
        }
        try {
            if (strX2.length() > 0) {
                _x2 = (int) Double.parseDouble(strX2);
            }
        } catch (Exception e) {
        }
        try {
            if (strY2.length() > 0) {
                _y2 = (int) Double.parseDouble(strY2);
            }
        } catch (Exception e) {
        }

        _x1 = (int) InteractionSpace.getSketchX(_x1);
        _y1 = (int) InteractionSpace.getSketchY(_y1);
        _x2 = (int) InteractionSpace.getSketchX(_x2);
        _y2 = (int) InteractionSpace.getSketchY(_y2);

        l_x1 = (int) (Math.min(_x1, _x2) * SketchletEditor.editorPanel.scale);
        l_y1 = (int) (Math.min(_y1, _y2) * SketchletEditor.editorPanel.scale);
        l_x2 = (int) (Math.max(_x1, _x2) * SketchletEditor.editorPanel.scale);
        l_y2 = (int) (Math.max(_y1, _y2) * SketchletEditor.editorPanel.scale);

        int mx = (int) (SketchletEditor.editorPanel.marginX * SketchletEditor.editorPanel.scale);
        int my = (int) (SketchletEditor.editorPanel.marginY * SketchletEditor.editorPanel.scale);

        p1 = new Polygon();
        p1.addPoint(mx + l_x1 - 5, 0);
        p1.addPoint(mx + l_x1 + 5, 0);
        p1.addPoint(mx + l_x1 + 5, 5);
        p1.addPoint(mx + l_x1, 10);
        p1.addPoint(mx + l_x1 - 5, 5);

        p2 = new Polygon();
        p2.addPoint(mx + l_x2 - 5, 0);
        p2.addPoint(mx + l_x2 + 5, 0);
        p2.addPoint(mx + l_x2 + 5, 5);
        p2.addPoint(mx + l_x2, 10);
        p2.addPoint(mx + l_x2 - 5, 5);

        p3 = new Polygon();
        p3.addPoint(0, my + l_y1 - 5);
        p3.addPoint(0, my + l_y1 + 5);
        p3.addPoint(5, my + l_y1 + 5);
        p3.addPoint(10, my + l_y1);
        p3.addPoint(5, my + l_y1 - 5);

        p4 = new Polygon();
        p4.addPoint(0, my + l_y2 - 5);
        p4.addPoint(0, my + l_y2 + 5);
        p4.addPoint(5, my + l_y2 + 5);
        p4.addPoint(10, my + l_y2);
        p4.addPoint(5, my + l_y2 - 5);
    }

    protected void paintComponent(Graphics g) {
        if (SketchletEditor.editorPanel.currentPage == null) {
            return;
        }
        int w2 = SketchletEditor.editorPanel.renderer.panel.getImage(0) != null ? SketchletEditor.editorPanel.renderer.panel.getImage(0).getWidth() : (int) InteractionSpace.sketchWidth;
        int h2 = SketchletEditor.editorPanel.renderer.panel.getImage(0) != null ? SketchletEditor.editorPanel.renderer.panel.getImage(0).getHeight() : (int) InteractionSpace.sketchHeight;

        Rectangle drawHere = new Rectangle(0, 0, w2, h2);//g.getClipBounds();

        // Fill clipping area with dirty brown/orange.
        //g.setColor(new Color(230, 163, 4));
        //g.fillRect(drawHere.x, drawHere.y, drawHere.width, drawHere.height);

        // Do the ruler labels in a small font that's black.
        g.setFont(new Font("SansSerif", Font.PLAIN, 8));
        g.setColor(Color.black);

        // Some vars we need.
        int end = 0;
        int start = 0;
        int tickLength = 0;
        String text = null;

        // Use clipping bounds to calculate first and last tick locations.
        if (orientation == HORIZONTAL) {
            start = (drawHere.x / increment) * increment;
            end = (((drawHere.x + drawHere.width) / increment) + 1) * increment;
        } else {
            start = (drawHere.y / increment) * increment;
            end = (((drawHere.y + drawHere.height) / increment) + 1) * increment;
        }

        // Make a special case of 0 to display the number
        // within the rule and draw a units label.
        if (start == 0) {
            text = Integer.toString(0) + (isMetric ? " cm" : " in");
            tickLength = 5;
            text = null;
            start = increment;
        }

        for (int i = start; i < end; i += increment) {
            if (i % 100 == 0) {
                tickLength = 3;
                text = Integer.toString(i);
            } else {
                tickLength = 7;
                text = null;
            }

            if (orientation == HORIZONTAL) {
                int ii = (int) ((SketchletEditor.marginX + i) * SketchletEditor.editorPanel.scale);
                g.drawLine(ii, SIZE - 1, ii, SIZE - tickLength - 1);
                if (text != null) {
                    g.drawString(text, ii - 7, 11);
                }
            } else {
                int ii = (int) ((SketchletEditor.marginY + i) * SketchletEditor.editorPanel.scale);
                g.drawLine(SIZE - 1, ii, SIZE - tickLength - 1, ii);
                if (text != null) {
                    g.drawString(text, 1, ii + 3);
                }
            }
        }
        if (SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null && SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size() == 1) {
            ActiveRegion region = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.firstElement();
            calculateLimits(region);

            g.setColor(Color.WHITE);
            if (orientation == HORIZONTAL) {
                if (p1 != null) {
                    g.fillPolygon(p1);
                }
                if (p2 != null) {
                    g.fillPolygon(p2);
                }
            } else {
                if (p3 != null) {
                    g.fillPolygon(p3);
                }
                if (p4 != null) {
                    g.fillPolygon(p4);
                }
            }
            g.setColor(Color.BLACK);
            if (orientation == HORIZONTAL) {
                if (p1 != null) {
                    g.drawPolygon(p1);
                }
                if (p2 != null) {
                    g.drawPolygon(p2);
                }
            } else {
                if (p3 != null) {
                    g.drawPolygon(p3);
                }
                if (p4 != null) {
                    g.drawPolygon(p4);
                }
            }
        }
    }

    int startX, startY;
    int selectedLimit = -1;
    ActiveRegion selectedRegion = null;

    public void mousePressed(int x, int y) {
        selectedLimit = -1;
        if (SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null && SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size() == 1) {
            selectedRegion = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.firstElement();
            if (this.orientation == HORIZONTAL) {
                if (p1.contains(x, y)) {
                    selectedLimit = 1;
                } else if (p2.contains(x, y)) {
                    selectedLimit = 2;
                }
            } else {
                if (p3.contains(x, y)) {
                    selectedLimit = 3;
                } else if (p4.contains(x, y)) {
                    selectedLimit = 4;
                }
            }

            this.bDragging = true;
        } else {
            this.bDragging = false;
            selectedRegion = null;
        }
    }

    boolean bDragging = false;

    public void mouseReleased(int x, int y) {
        this.bDragging = false;
    }

    public void mouseDragged(int x, int y) {
        if (selectedRegion == null) {
            return;
        }

        x -= SketchletEditor.editorPanel.marginX * SketchletEditor.editorPanel.scale;
        y -= SketchletEditor.editorPanel.marginY * SketchletEditor.editorPanel.scale;

        int w = SketchletEditor.editorPanel.currentPage.images != null && SketchletEditor.editorPanel.currentPage.images[0] != null ? SketchletEditor.editorPanel.currentPage.images[0].getWidth() : 2000;
        int h = SketchletEditor.editorPanel.currentPage.images != null && SketchletEditor.editorPanel.currentPage.images[0] != null ? SketchletEditor.editorPanel.currentPage.images[0].getHeight() : 2000;

        Rectangle rect;

        if (SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null && SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size() == 1) {
            ActiveRegion region = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.firstElement();
            rect = region.getBounds(false);
            y = Math.max(y, 0);
            y = Math.min(y, h);
        } else {
            return;
        }

        x = (int) (x / SketchletEditor.editorPanel.scale);
        y = (int) (y / SketchletEditor.editorPanel.scale);
        switch (selectedLimit) {
            case 1:
                x = Math.min(x, (int) rect.getX());
                x = Math.max(x, 0);
                selectedRegion.limits[0][1] = "" + InteractionSpace.getPhysicalX(x);
                break;
            case 2:
                x = Math.max(x, (int) (rect.getX() + rect.getWidth()));
                x = Math.min(x, w);
                selectedRegion.limits[0][2] = "" + InteractionSpace.getPhysicalX(x);
                break;
            case 3:
                y = Math.min(y, (int) rect.getY());
                y = Math.max(y, 0);
                selectedRegion.limits[1][1] = "" + InteractionSpace.getPhysicalY(y);
                break;
            case 4:
                y = Math.max(y, (int) (rect.getY() + rect.getHeight()));
                y = Math.min(y, h);
                selectedRegion.limits[1][2] = "" + InteractionSpace.getPhysicalY(y);
                break;
        }

        RefreshTime.update();
        SketchletEditor.editorPanel.repaint();
    }

    class MouseHandler extends MouseInputAdapter {

        public void mouseMoved(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
            SketchletEditor.editorPanel.currentPage.regions.selectedRegions = new Vector<ActiveRegion>();
            SketchletEditor.editorPanel.currentPage.regions.defocusAllRegions();
            int x = e.getX();
            int y = e.getY();
            Rule.this.mousePressed(x, y);
        }

        public void mouseReleased(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            Rule.this.mouseReleased(x, y);
        }

        public void mouseDragged(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            Rule.this.mouseDragged(x, y);
        }
    }

    ;
}
