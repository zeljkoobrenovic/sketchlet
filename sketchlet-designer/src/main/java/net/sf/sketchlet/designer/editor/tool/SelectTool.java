/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.tool;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.regions.ActiveRegionImageEditor;
import net.sf.sketchlet.designer.help.TutorialPanel;
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

    public int x1, y1, x2, y2;
    boolean selected = false;
    int selectedArea = -1;
    public BufferedImage selectedClip = null;
    boolean bDrawRect = true;

    public SelectTool(ToolInterface toolInterface) {
        super(toolInterface);
    }

    public void mouseMoved(int x, int y, int modifiers) {
        int _x1 = Math.min(x1, x2);
        int _y1 = Math.min(y1, y2);
        int _x2 = Math.max(x1, x2);
        int _y2 = Math.max(y1, y2);
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
        } else if (x > Math.min(x1, x2) && y > Math.min(y1, y2) && x < Math.max(x1, x2) && y < Math.max(y1, y2)) {
            selected = true;
            selectedArea = 5;
        } else {
            selected = false;
        }
        toolInterface.setImageCursor(getCursor());
    }

    int prev_x, prev_y;

    public void setClip(BufferedImage clip, int x, int y) {
        this.selectedClip = clip;
        x1 = x;
        y1 = y;
        x2 = x1 + clip.getWidth();
        y2 = y1 + clip.getHeight();
        rotation = 0.0;
        toolInterface.repaintImage();
    }

    int pressedX;
    int pressedY;

    public void mousePressed(int x, int y, int modifiers) {
        pressedX = x;
        pressedY = y;
        if ((modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
            if (!selected) {
                saveClip();
                selectedClip = null;
                x1 = x;
                y1 = y;
                x2 = x;
                y2 = y;
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

    public void mouseReleased(int x, int y, int modifiers) {
        if ((modifiers & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK && pressedX == x && pressedY == y && this.selected) {
            popupMenu(x, y);
        }
        ActivityLog.log("toolResult", (selected ? "Move the selected image area in " : "Select an image area in ") + toolInterface.getName(), "select.png", toolInterface.getPanel());
        TutorialPanel.addLine("cmd", (selected ? "Move the selected image area in " : "Select an image area by dragging in ") + toolInterface.getName(), "", toolInterface.getPanel());
    }

    public void popupMenu(final int x, final int y) {
        if (SketchletEditor.editorPanel != null && toolInterface instanceof SketchletEditor) {
            int _x = (int) (x * SketchletEditor.editorPanel.scale + SketchletEditor.marginX);
            int _y = (int) (y * SketchletEditor.editorPanel.scale + SketchletEditor.marginY);

            JPopupMenu popup = new JPopupMenu();
            TutorialPanel.prepare(popup, true);

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
                    intoActiveRegion(x1, y1);
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

            popup.show(SketchletEditor.editorPanel, _x, _y);
        } else if (toolInterface instanceof ActiveRegionImageEditor) {
            int _x = (int) (x * ActiveRegionImageEditor.scale);
            int _y = (int) (y * ActiveRegionImageEditor.scale);

            JPopupMenu popup = new JPopupMenu();
            TutorialPanel.prepare(popup, true);

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

    void checkXY() {
        int _x1 = Math.min(x1, x2);
        int _y1 = Math.min(y1, y2);
        int _x2 = Math.max(x1, x2);
        int _y2 = Math.max(y1, y2);

        x1 = Math.max(0, _x1);
        y1 = Math.max(0, _y1);
        x2 = Math.min(toolInterface.getImageWidth(), _x2);
        y2 = Math.min(toolInterface.getImageHeight(), _y2);

    }

    public void mouseDragged(int x, int y, int modifiers) {
        if (!selected) {
            x2 = x;
            y2 = y;
        } else {
            if ((modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                int dx = x - prev_x;
                int dy = y - prev_y;

                if (selectedArea == 5) {
                    x1 += dx;
                    y1 += dy;
                    x2 += dx;
                    y2 += dy;
                } else if (selectedArea == 1) {
                    x1 += dx;
                    y1 += dy;
                } else if (selectedArea == 2) {
                    x2 += dx;
                    y1 += dy;
                } else if (selectedArea == 3) {
                    x1 += dx;
                    y2 += dy;
                } else if (selectedArea == 4) {
                    x2 += dx;
                    y2 += dy;
                }

                prev_x = x;
                prev_y = y;
            } else if ((modifiers & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                rotate(x, y);
            }
        }

        toolInterface.repaintImage();
    }

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

    public void deactivate() {
        saveClip();
        this.selectedClip = null;
        this.selected = false;
        x1 = x2;
        y1 = y2;
        rotation = 0.0;
        selected = false;
        toolInterface.repaintImage();
    }

    public void saveClip() {
        if (this.selectedClip != null) {
            toolInterface.createGraphics();
            int xCenter = x1 + (x2 - x1) / 2;
            int yCenter = y1 + (y2 - y1) / 2;
            AffineTransform affine = toolInterface.getImageGraphics().getTransform();
            toolInterface.getImageGraphics().rotate(rotation, xCenter, yCenter);
            toolInterface.getImageGraphics().drawImage(this.selectedClip, x1, y1, x2 - x1, y2 - y1, null);
            toolInterface.getImageGraphics().setTransform(affine);
            toolInterface.setImageUpdated(true);
        }
    }

    public void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (this.selectedClip != null) {
            int xCenter = x1 + (x2 - x1) / 2;
            int yCenter = y1 + (y2 - y1) / 2;
            AffineTransform affine = g2.getTransform();
            g2.rotate(rotation, xCenter, yCenter);
            g2.drawImage(this.selectedClip, x1, y1, x2 - x1, y2 - y1, null);
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
        int _x1 = Math.min(x1, x2);
        int _y1 = Math.min(y1, y2);
        int _x2 = Math.max(x1, x2);
        int _y2 = Math.max(y1, y2);
        int s = (int) (9 / SketchletEditor.editorPanel.scale);
        if (bDrawRect) {
            g2.drawRect(_x1, _y1, _x2 - _x1, _y2 - _y1);
        }
        if (x1 != x2 || y1 != y2) {
            g2.fillRect(_x1 - s / 2, _y1 - s / 2, s, s);
            g2.fillRect(_x2 - s / 2, _y1 - s / 2, s, s);
            g2.fillRect(_x1 - s / 2, _y2 - s / 2, s, s);
            g2.fillRect(_x2 - s / 2, _y2 - s / 2, s, s);
        }
    }

    public void onUndo() {
        this.selectedClip = null;
        this.selected = false;
        x1 = x2;
        y1 = y2;
        rotation = 0.0;
        toolInterface.repaintImage();
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            delete();
        }
    }

    public void cut() {
        this.getClip();
        net.sf.sketchlet.designer.ui.BufferedImageClipboardObject tr = new net.sf.sketchlet.designer.ui.BufferedImageClipboardObject(this.selectedClip, DataFlavor.imageFlavor);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(tr, null);

        this.selectedClip = null;
        this.selected = false;
        x1 = x2;
        y1 = y2;
        toolInterface.setCursor();
        toolInterface.repaintImage();
    }

    public void copy() {
        this.getClip();
        net.sf.sketchlet.designer.ui.BufferedImageClipboardObject tr = new net.sf.sketchlet.designer.ui.BufferedImageClipboardObject(this.selectedClip, DataFlavor.imageFlavor);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(tr, null);
    }

    public void intoActiveRegion(int x, int y) {
        if (SketchletEditor.editorPanel != null) {
            this.getClip();
            BufferedImage img = this.selectedClip;

            this.selectedClip = null;
            this.selected = false;
            x1 = x2;
            y1 = y2;
            SketchletEditor.editorPanel.setCursor();
            SketchletEditor.editorPanel.repaint();

            SketchletEditor.editorPanel.newRegionFromImage(img, x, y);
        }
    }

    public void crop() {
        this.getClip();
        this.saveClip();
        this.toolInterface.setImage(this.selectedClip);
        delete();
    }

    public void delete() {
        this.getClip();
        this.selectedClip = null;
        this.selected = false;
        x1 = x2;
        y1 = y2;
        toolInterface.setCursor();
        toolInterface.repaintImage();
    }

    public void getClip() {
        if (selectedClip == null) {
            checkXY();

            int w = x2 - x1;
            int h = y2 - y1;

            if (w > 0 && h > 0) {
                selectedClip = toolInterface.extractImage(x1, y1, w, h);
            }
        }
    }

    double rotation = 0.0;
    double startAngle = 0.0;

    public void rotate(int x, int y) {
        int xCenter = x1 + (x2 - x1) / 2;
        int yCenter = y1 + (y2 - y1) / 2;

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

    public void startRotate(int x, int y) {
        int xCenter = x1 + (x2 - x1) / 2;
        int yCenter = y1 + (y2 - y1) / 2;

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

    public ImageIcon getIcon() {
        return Workspace.createImageIcon("resources/select.png");
    }

    public String getIconFileName() {
        return "select.png";
    }

    public String getName() {
        return Language.translate("Select");
    }
}
