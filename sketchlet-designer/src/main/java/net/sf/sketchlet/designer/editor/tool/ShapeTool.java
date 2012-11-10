/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.tool;

import net.sf.sketchlet.designer.Workspace;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

/**
 * @author zobrenovic
 */
public class ShapeTool extends Tool {

    int x1, y1, x2, y2;
    boolean bDrawLine = true;
    boolean bFill = false;

    public ShapeTool(ToolInterface toolInterface) {
        super(toolInterface, new String[]{"stroke type", "stroke width", "anitaliasing", "outline/filling", "fill patterns"});
    }

    public ShapeTool(ToolInterface toolInterface, String settings[]) {
        super(toolInterface, settings);
    }

    int mouseX;
    int mouseY;

    public void mouseMoved(int x, int y, int modifiers) {
        mouseX = x;
        mouseY = y;
        /*toolInterface.repaintImage();
        toolInterface.setImageCursor(getCursor());*/
    }

    int prev_x, prev_y;

    public boolean shouldFill() {
        return bFill;
    }

    public void mousePressed(int x, int y, int modifiers) {
        bFill = toolInterface.shouldShapeFill();
        x1 = x;
        y1 = y;
        x2 = x;
        y2 = y;
        transparency = 1.0f;
        toolInterface.repaintImage();
    }

    boolean drawStrokeWidth = true;
    float transparency = 1.0f;

    public Color getColor() {
        Color c = this.toolInterface.getColor();
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (transparency * 255));
    }

    public void mouseReleased(int x, int y, int modifiers) {
        drawStrokeWidth = false;
        new Thread(new Runnable() {

            public void run() {
                transparency = 1.0f;
                if (toolInterface.getWatering() < 1.0f) {
                    try {
                        float d = (1 - toolInterface.getWatering()) / 10;
                        for (int i = 0; i < 10; i++) {
                            transparency -= d;
                            toolInterface.repaintImage();
                            Thread.sleep(15);
                        }
                    } catch (Exception e) {
                    }
                }
                if (!(x1 == x2 && y1 == y2)) {
                    toolInterface.saveImageUndo();
                    draw(toolInterface.createGraphics());
                    toolInterface.setImageUpdated(true);
                }
                deactivate();
                drawStrokeWidth = true;
            }
        }).start();
    }

    public void mouseDragged(int x, int y, int modifiers) {
        this.mouseMoved(x, y, modifiers);
        if (!toolInterface.isInShiftMode()) {
            x2 = x;
            y2 = y;
        } else {
            x2 = x;
            y2 = y1 + (x2 - x1);
        }

        toolInterface.repaintImage();
    }

    public Cursor getCursor() {
        return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    }

    public void deactivate() {
        x1 = x2;
        y1 = y2;
        toolInterface.repaintImage();
    }

    public void fill(Graphics2D g2, int x, int y, int w, int h) {
    }

    public void draw(Graphics2D g2, int x, int y, int w, int h) {
    }

    public boolean checkDimensions() {
        return Math.abs(x2 - x1) > 0 && Math.abs(y2 - y1) > 0;
    }

    public void draw(Graphics2D g2) {
        if (drawStrokeWidth) {
            int w = toolInterface.getStrokeWidth() + 2;
            Color c = g2.getColor();
            g2.setColor(Color.GRAY);
            g2.drawOval(mouseX - w / 2, mouseY - w / 2, w, w);
            g2.setColor(c);
        }
        if (!(x1 == x2 && y1 == y2) && checkDimensions()) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
            if (shouldFill()) {
                BufferedImage img = Workspace.createCompatibleImage(Math.abs(x2 - x1) + 40, Math.abs(y2 - y1) + 40);
                Graphics2D g2i = img.createGraphics();
                g2i.setStroke(toolInterface.getStroke());
                g2i.setColor(toolInterface.getColor());

                fill(g2i, 20, 20, Math.abs(x2 - x1), Math.abs(y2 - y1));
                draw(g2i, 20, 20, Math.abs(x2 - x1), Math.abs(y2 - y1));

                g2.drawImage(img, Math.min(x1, x2) - 20, Math.min(y1, y2) - 20, null);

                g2i.dispose();
                img.flush();
                img = null;
            } else {
                g2.setStroke(toolInterface.getStroke());
                g2.setColor(toolInterface.getColor());
                draw(g2, Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
            }
        }
    }

    public void onUndo() {
        x1 = x2;
        y1 = y2;
        toolInterface.repaintImage();
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            x1 = x2;
            y1 = y2;
            toolInterface.setCursor();
            toolInterface.repaintImage();
        }
    }
}
