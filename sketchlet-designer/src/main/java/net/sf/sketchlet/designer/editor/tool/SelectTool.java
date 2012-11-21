/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.tool;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionImageEditor;
import net.sf.sketchlet.designer.tools.log.ActivityLog;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * @author zobrenovic
 */
public class SelectTool extends Tool {

    protected int x1;
    protected int y1;
    protected int x2;
    protected int y2;
    protected boolean selected = false;
    private int selectedArea = -1;
    protected BufferedImage selectedClip = null;
    protected boolean bDrawRect = true;
    private int pressedX;
    private int pressedY;

    private double rotation = 0.0;
    private double startAngle = 0.0;
    protected int prev_x, prev_y;

    public SelectTool(ToolInterface toolInterface) {
        super(toolInterface);
    }

    @Override
    public void mouseMoved(int x, int y, int modifiers) {
        int _x1 = Math.min(getX1(), getX2());
        int _y1 = Math.min(getY1(), getY2());
        int _x2 = Math.max(getX1(), getX2());
        int _y2 = Math.max(getY1(), getY2());
        int s = 9;

        if (new Rectangle(_x1 - s / 2, _y1 - s / 2, s, s).contains(x, y)) {
            selected = true;
            selectedArea = 1;
        } else if (new Rectangle(_x2 - s / 2, _y1 - s / 2, s, s).contains(x, y)) {
            selected = true;
            selectedArea = 2;
        } else if (new Rectangle(_x1 - s / 2, _y2 - s / 2, s, s).contains(x, y)) {
            selected = true;
            selectedArea = 3;
        } else if (new Rectangle(_x2 - s / 2, _y2 - s / 2, s, s).contains(x, y)) {
            selected = true;
            selectedArea = 4;
        } else if (x > Math.min(getX1(), getX2()) && y > Math.min(getY1(), getY2()) && x < Math.max(getX1(), getX2()) && y < Math.max(getY1(), getY2())) {
            selected = true;
            selectedArea = 5;
        } else {
            selected = false;
        }
        toolInterface.setImageCursor(getCursor());
    }

    @Override
    public void mousePressed(int x, int y, int modifiers) {
        pressedX = x;
        pressedY = y;
        if ((modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
            if (!selected) {
                saveClip();
                selectedClip = null;
                setX1(x);
                setY1(y);
                setX2(x);
                setY2(y);
                rotation = 0.0;
                toolInterface.repaintImage();
            } else {
                if ((modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                    prev_x = x;
                    prev_y = y;
                } else if ((modifiers & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                    startRotate(x, y);
                }
                getClip();
            }
        }
    }

    @Override
    public void mouseReleased(int x, int y, int modifiers) {
        if ((modifiers & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK && pressedX == x && pressedY == y && this.selected) {
            popupMenu(x, y);
        }
        ActivityLog.log("toolResult", (selected ? "Move the selected image area in " : "Select an image area in ") + toolInterface.getName(), "select.png", toolInterface.getPanel());
    }

    @Override
    public void mouseDragged(int x, int y, int modifiers) {
        if (!selected) {
            setX2(x);
            setY2(y);
        } else {
            if ((modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                int dx = x - prev_x;
                int dy = y - prev_y;

                if (selectedArea == 5) {
                    setX1(getX1() + dx);
                    setY1(getY1() + dy);
                    setX2(getX2() + dx);
                    setY2(getY2() + dy);
                } else if (selectedArea == 1) {
                    setX1(getX1() + dx);
                    setY1(getY1() + dy);
                } else if (selectedArea == 2) {
                    setX2(getX2() + dx);
                    setY1(getY1() + dy);
                } else if (selectedArea == 3) {
                    setX1(getX1() + dx);
                    setY2(getY2() + dy);
                } else if (selectedArea == 4) {
                    setX2(getX2() + dx);
                    setY2(getY2() + dy);
                }

                prev_x = x;
                prev_y = y;
            } else if ((modifiers & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                rotate(x, y);
            }
        }

        toolInterface.repaintImage();
    }

    @Override
    public Cursor getCursor() {
        if (selected) {
            switch (selectedArea) {
                case 1:
                    return Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
                case 2:
                    return Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
                case 3:
                    return Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
                case 4:
                    return Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
                case 5:
                    return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
            }
        }
        return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);

    }

    @Override
    public void deactivate() {
        saveClip();
        this.selectedClip = null;
        this.selected = false;
        setX1(getX2());
        setY1(getY2());
        rotation = 0.0;
        selected = false;
        toolInterface.repaintImage();
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (this.selectedClip != null) {
            int xCenter = getX1() + (getX2() - getX1()) / 2;
            int yCenter = getY1() + (getY2() - getY1()) / 2;
            AffineTransform affine = g2.getTransform();
            g2.rotate(rotation, xCenter, yCenter);
            g2.drawImage(this.selectedClip, getX1(), getY1(), getX2() - getX1(), getY2() - getY1(), null);
            g2.setTransform(affine);
        }
        float dash1[] = {10.0f};
        float thick = 1.0f;
        BasicStroke dashed = new BasicStroke(thick,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                10.0f, dash1, 0.0f);

        g2.setStroke(dashed);
        g2.setColor(Color.BLACK);
        int _x1 = Math.min(getX1(), getX2());
        int _y1 = Math.min(getY1(), getY2());
        int _x2 = Math.max(getX1(), getX2());
        int _y2 = Math.max(getY1(), getY2());
        int s = (int) (9 / SketchletEditor.getInstance().getScale());
        if (bDrawRect) {
            g2.drawRect(_x1, _y1, _x2 - _x1, _y2 - _y1);
        }
        if (getX1() != getX2() || getY1() != getY2()) {
            g2.fillRect(_x1 - s / 2, _y1 - s / 2, s, s);
            g2.fillRect(_x2 - s / 2, _y1 - s / 2, s, s);
            g2.fillRect(_x1 - s / 2, _y2 - s / 2, s, s);
            g2.fillRect(_x2 - s / 2, _y2 - s / 2, s, s);
        }
    }

    @Override
    public void onUndo() {
        this.selectedClip = null;
        this.selected = false;
        setX1(getX2());
        setY1(getY2());
        rotation = 0.0;
        toolInterface.repaintImage();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            delete();
        }
    }

    @Override
    public ImageIcon getIcon() {
        return Workspace.createImageIcon("resources/select.png");
    }

    @Override
    public String getIconFileName() {
        return "select.png";
    }

    @Override
    public String getName() {
        return Language.translate("Select");
    }

    public int getX1() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public int getY1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public int getX2() {
        return x2;
    }

    public void setX2(int x2) {
        this.x2 = x2;
    }

    public int getY2() {
        return y2;
    }

    public void setY2(int y2) {
        this.y2 = y2;
    }


    public void setClip(BufferedImage clip, int x, int y) {
        this.selectedClip = clip;
        setX1(x);
        setY1(y);
        setX2(getX1() + clip.getWidth());
        setY2(getY1() + clip.getHeight());
        rotation = 0.0;
        toolInterface.repaintImage();
    }

    private void saveClip() {
        if (this.selectedClip != null) {
            toolInterface.createGraphics();
            int xCenter = getX1() + (getX2() - getX1()) / 2;
            int yCenter = getY1() + (getY2() - getY1()) / 2;
            AffineTransform affine = toolInterface.getImageGraphics().getTransform();
            toolInterface.getImageGraphics().rotate(rotation, xCenter, yCenter);
            toolInterface.getImageGraphics().drawImage(this.selectedClip, getX1(), getY1(), getX2() - getX1(), getY2() - getY1(), null);
            toolInterface.getImageGraphics().setTransform(affine);
            toolInterface.setImageUpdated(true);
        }
    }

    private void cut() {
        this.getClip();
        net.sf.sketchlet.designer.editor.ui.BufferedImageClipboardObject tr = new net.sf.sketchlet.designer.editor.ui.BufferedImageClipboardObject(this.selectedClip, DataFlavor.imageFlavor);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(tr, null);

        this.selectedClip = null;
        this.selected = false;
        setX1(getX2());
        setY1(getY2());
        toolInterface.setCursor();
        toolInterface.repaintImage();
    }

    public void copy() {
        this.getClip();
        net.sf.sketchlet.designer.editor.ui.BufferedImageClipboardObject tr = new net.sf.sketchlet.designer.editor.ui.BufferedImageClipboardObject(this.selectedClip, DataFlavor.imageFlavor);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(tr, null);
    }

    private void intoActiveRegion(int x, int y) {
        if (SketchletEditor.getInstance() != null) {
            this.getClip();
            BufferedImage img = this.selectedClip;

            this.selectedClip = null;
            this.selected = false;
            setX1(getX2());
            setY1(getY2());
            SketchletEditor.getInstance().setCursor();
            SketchletEditor.getInstance().repaint();

            SketchletEditor.getInstance().newRegionFromImage(img, x, y);
        }
    }

    private void crop() {
        this.getClip();
        this.saveClip();
        this.toolInterface.setImage(this.selectedClip);
        delete();
    }

    private void delete() {
        this.getClip();
        this.selectedClip = null;
        this.selected = false;
        setX1(getX2());
        setY1(getY2());
        toolInterface.setCursor();
        toolInterface.repaintImage();
    }

    private void getClip() {
        if (selectedClip == null) {
            checkXY();

            int w = getX2() - getX1();
            int h = getY2() - getY1();

            if (w > 0 && h > 0) {
                selectedClip = toolInterface.extractImage(getX1(), getY1(), w, h);
            }
        }
    }

    private void rotate(int x, int y) {
        int xCenter = getX1() + (getX2() - getX1()) / 2;
        int yCenter = getY1() + (getY2() - getY1()) / 2;

        if (x == xCenter) {
            rotation = y > yCenter ? Math.PI / 2 : -Math.PI / 2;
        } else {
            rotation = Math.atan((double) (y - yCenter) / (x - xCenter));
        }

        if (x < xCenter) {
            rotation += Math.PI;
        }

        rotation = rotation + startAngle;
    }

    private void startRotate(int x, int y) {
        int xCenter = getX1() + (getX2() - getX1()) / 2;
        int yCenter = getY1() + (getY2() - getY1()) / 2;

        if (x == xCenter) {
            startAngle = y > yCenter ? Math.PI / 2 : -Math.PI / 2;
        } else {
            startAngle = Math.atan((double) (y - yCenter) / (x - xCenter));
        }

        startAngle -= rotation;

        if (x < xCenter) {
            startAngle += Math.PI;
        }

        if (startAngle < 0) {
            startAngle += 2 * Math.PI;
        }

    }


    private void popupMenu(final int x, final int y) {
        if (SketchletEditor.getInstance() != null && toolInterface instanceof SketchletEditor) {
            int _x = (int) (x * SketchletEditor.getInstance().getScale() + SketchletEditor.getInstance().getMarginX());
            int _y = (int) (y * SketchletEditor.getInstance().getScale() + SketchletEditor.getInstance().getMarginY());

            JPopupMenu popup = new JPopupMenu();

            JMenuItem delete = new JMenuItem("Delete", net.sf.sketchlet.designer.Workspace.createImageIcon("resources/user-trash.png"));
            JMenuItem cut = new JMenuItem("Cut", net.sf.sketchlet.designer.Workspace.createImageIcon("resources/edit-cut.png"));
            JMenuItem copy = new JMenuItem("Copy", net.sf.sketchlet.designer.Workspace.createImageIcon("resources/edit-copy.png"));
            JMenuItem activeRegion = new JMenuItem("Turn into active region", net.sf.sketchlet.designer.Workspace.createImageIcon("resources/active_region.png"));

            delete.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    delete();
                }
            });
            cut.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    cut();
                }
            });
            copy.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    copy();
                }
            });
            activeRegion.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    intoActiveRegion(getX1(), getY1());
                }
            });
            JMenuItem crop = new JMenuItem("Crop page to selection", net.sf.sketchlet.designer.Workspace.createImageIcon("resources/rectangle.png"));
            crop.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    crop();
                }
            });

            popup.add(copy);
            popup.addSeparator();
            popup.add(delete);
            popup.add(cut);
            popup.addSeparator();
            popup.add(crop);
            popup.addSeparator();
            popup.add(activeRegion);

            popup.show(SketchletEditor.getInstance(), _x, _y);
        } else if (toolInterface instanceof ActiveRegionImageEditor) {
            int _x = (int) (x * ActiveRegionImageEditor.getScale());
            int _y = (int) (y * ActiveRegionImageEditor.getScale());

            JPopupMenu popup = new JPopupMenu();

            JMenuItem delete = new JMenuItem("Delete", net.sf.sketchlet.designer.Workspace.createImageIcon("resources/user-trash.png"));
            JMenuItem cut = new JMenuItem("Cut", net.sf.sketchlet.designer.Workspace.createImageIcon("resources/edit-cut.png"));
            JMenuItem copy = new JMenuItem("Copy", net.sf.sketchlet.designer.Workspace.createImageIcon("resources/edit-copy.png"));
            JMenuItem crop = new JMenuItem("Crop to selection", net.sf.sketchlet.designer.Workspace.createImageIcon("resources/rectangle.png"));

            delete.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    delete();
                }
            });
            cut.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    cut();
                }
            });
            copy.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    copy();
                }
            });
            crop.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    crop();
                }
            });

            popup.add(copy);
            popup.addSeparator();
            popup.add(delete);
            popup.add(cut);
            popup.addSeparator();
            popup.add(crop);

            popup.show((ActiveRegionImageEditor) toolInterface, _x, _y);
        }
    }

    private void checkXY() {
        int _x1 = Math.min(getX1(), getX2());
        int _y1 = Math.min(getY1(), getY2());
        int _x2 = Math.max(getX1(), getX2());
        int _y2 = Math.max(getY1(), getY2());

        setX1(Math.max(0, _x1));
        setY1(Math.max(0, _y1));
        setX2(Math.min(toolInterface.getImageWidth(), _x2));
        setY2(Math.min(toolInterface.getImageHeight(), _y2));

    }
}
