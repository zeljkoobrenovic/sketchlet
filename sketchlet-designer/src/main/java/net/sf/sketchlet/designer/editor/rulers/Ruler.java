package net.sf.sketchlet.designer.editor.rulers;

import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.util.RefreshTime;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Vector;

public class Ruler extends JComponent {

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    public static final int SIZE = 20;

    private int orientation;
    private int increment;
    private MouseHandler mouseHandler = new MouseHandler();
    private int l_x1, l_y1, l_x2, l_y2;
    private Polygon p1, p2, p3, p4;
    private int selectedLimit = -1;
    private ActiveRegion selectedRegion = null;

    public Ruler(int o, boolean m) {
        setOrientation(o);
        setIncrementAndUnits();
        if (o == HORIZONTAL) {
            this.setPreferredWidth(5000);
        } else {
            this.setPreferredHeight(5000);
        }
        this.addMouseListener(mouseHandler);
        this.addMouseMotionListener(mouseHandler);
    }

    private void setIncrementAndUnits() {
        increment = 20;
    }

    public void setPreferredHeight(int ph) {
        setPreferredSize(new Dimension(SIZE, ph));
    }

    public void setPreferredWidth(int pw) {
        setPreferredSize(new Dimension(pw, SIZE));
    }

    public void calculateLimits(ActiveRegion region) {
        String strX1 = (String) region.limits[0][1];
        String strX2 = (String) region.limits[0][2];
        String strY1 = (String) region.limits[1][1];
        String strY2 = (String) region.limits[1][2];
        int _x1 = 0;
        int _y1 = 0;
        BufferedImage img = SketchletEditor.getInstance().getCurrentPage().getImages()[0];
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

        l_x1 = (int) (Math.min(_x1, _x2) * SketchletEditor.getInstance().getScale());
        l_y1 = (int) (Math.min(_y1, _y2) * SketchletEditor.getInstance().getScale());
        l_x2 = (int) (Math.max(_x1, _x2) * SketchletEditor.getInstance().getScale());
        l_y2 = (int) (Math.max(_y1, _y2) * SketchletEditor.getInstance().getScale());

        int mx = (int) (SketchletEditor.getInstance().getMarginX() * SketchletEditor.getInstance().getScale());
        int my = (int) (SketchletEditor.getInstance().getMarginY() * SketchletEditor.getInstance().getScale());

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
        if (SketchletEditor.getInstance().getCurrentPage() == null) {
            return;
        }
        int w2 = SketchletEditor.getInstance().getRenderer().getPanelRenderer().getImage(0) != null ? SketchletEditor.getInstance().getRenderer().getPanelRenderer().getImage(0).getWidth() : (int) InteractionSpace.getSketchWidth();
        int h2 = SketchletEditor.getInstance().getRenderer().getPanelRenderer().getImage(0) != null ? SketchletEditor.getInstance().getRenderer().getPanelRenderer().getImage(0).getHeight() : (int) InteractionSpace.getSketchHeight();

        Rectangle drawHere = new Rectangle(0, 0, w2, h2);//g.getClipBounds();

        g.setFont(new Font("SansSerif", Font.PLAIN, 8));
        g.setColor(Color.black);

        int end = 0;
        int start = 0;
        int tickLength = 0;
        String text = null;

        if (getOrientation() == HORIZONTAL) {
            start = (drawHere.x / increment) * increment;
            end = (((drawHere.x + drawHere.width) / increment) + 1) * increment;
        } else {
            start = (drawHere.y / increment) * increment;
            end = (((drawHere.y + drawHere.height) / increment) + 1) * increment;
        }

        if (start == 0) {
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

            if (getOrientation() == HORIZONTAL) {
                int ii = (int) ((SketchletEditor.getInstance().getMarginX() + i) * SketchletEditor.getInstance().getScale());
                g.drawLine(ii, SIZE - 1, ii, SIZE - tickLength - 1);
                if (text != null) {
                    g.drawString(text, ii - 7, 11);
                }
            } else {
                int ii = (int) ((SketchletEditor.getInstance().getMarginY() + i) * SketchletEditor.getInstance().getScale());
                g.drawLine(SIZE - 1, ii, SIZE - tickLength - 1, ii);
                if (text != null) {
                    g.drawString(text, 1, ii + 3);
                }
            }
        }
        if (SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() == 1) {
            ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().firstElement();
            calculateLimits(region);

            g.setColor(Color.WHITE);
            if (getOrientation() == HORIZONTAL) {
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
            if (getOrientation() == HORIZONTAL) {
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

    public void mousePressed(int x, int y) {
        selectedLimit = -1;
        if (SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() == 1) {
            selectedRegion = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().firstElement();
            if (this.getOrientation() == HORIZONTAL) {
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

        } else {
            selectedRegion = null;
        }
    }

    public void mouseReleased(int x, int y) {
    }

    public void mouseDragged(int x, int y) {
        if (selectedRegion == null) {
            return;
        }

        x -= SketchletEditor.getInstance().getMarginX() * SketchletEditor.getInstance().getScale();
        y -= SketchletEditor.getInstance().getMarginY() * SketchletEditor.getInstance().getScale();

        int w = SketchletEditor.getInstance().getCurrentPage().getImages() != null && SketchletEditor.getInstance().getCurrentPage().getImages()[0] != null ? SketchletEditor.getInstance().getCurrentPage().getImages()[0].getWidth() : 2000;
        int h = SketchletEditor.getInstance().getCurrentPage().getImages() != null && SketchletEditor.getInstance().getCurrentPage().getImages()[0] != null ? SketchletEditor.getInstance().getCurrentPage().getImages()[0].getHeight() : 2000;

        Rectangle rect;

        if (SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() == 1) {
            ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().firstElement();
            rect = region.getBounds(false);
            y = Math.max(y, 0);
            y = Math.min(y, h);
        } else {
            return;
        }

        x = (int) (x / SketchletEditor.getInstance().getScale());
        y = (int) (y / SketchletEditor.getInstance().getScale());
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
        SketchletEditor.getInstance().repaint();
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    class MouseHandler extends MouseInputAdapter {

        public void mouseMoved(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
            SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().setSelectedRegions(new Vector<ActiveRegion>());
            SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().defocusAllRegions();
            int x = e.getX();
            int y = e.getY();
            Ruler.this.mousePressed(x, y);
        }

        public void mouseReleased(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            Ruler.this.mouseReleased(x, y);
        }

        public void mouseDragged(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            Ruler.this.mouseDragged(x, y);
        }
    }

    ;
}
