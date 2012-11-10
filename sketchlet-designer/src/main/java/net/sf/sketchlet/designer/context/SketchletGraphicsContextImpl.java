/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.context;

import net.sf.sketchlet.context.SketchletGraphicsContext;
import net.sf.sketchlet.context.SketchletPainter;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.ui.playback.PlaybackFrame;
import net.sf.sketchlet.util.RefreshTime;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

/**
 * @author zobrenovic
 */
public class SketchletGraphicsContextImpl extends SketchletGraphicsContext {
    private static final Logger log = Logger.getLogger(SketchletGraphicsContext.class);

    public static BufferedImage image = null;
    Graphics2D g2;

    private Graphics2D getGraphics() {
        if (image == null) {
            image = Workspace.createCompatibleImage(2000, 2000);
        }
        if (g2 == null) {
            g2 = image.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.BLACK);
        }
        return g2;
    }

    public static void paint(Graphics2D g) {
        if (image != null) {
            g.drawImage(image, 0, 0, null);
        }
    }

    @Override
    public void clearCanvas() {
        image = null;
        g2 = null;
        repaint();
    }

    @Override
    public void repaint() {
        RefreshTime.update();
        if (SketchletEditor.editorPanel.internalPlaybackPanel != null) {
            SketchletEditor.editorPanel.internalPlaybackPanel.repaint();
        } else {
            SketchletEditor.editorPanel.repaint();
        }

        try {
            Thread.sleep(1);
        } catch (Exception e) {
            log.error(e);
        }
    }

    @Override
    public void setColor(int r, int g, int b) {
        Graphics2D g2 = getGraphics();
        g2.setColor(new Color(r, g, b));
    }

    @Override
    public void setColor(int r, int g, int b, int transparency) {
        Graphics2D g2 = getGraphics();
        g2.setColor(new Color(r, g, b, transparency));
    }

    @Override
    public void setTransparency(float transparency) {
        Graphics2D g2 = getGraphics();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
    }

    @Override
    public void setLineWidth(double width) {
        Graphics2D g2 = getGraphics();
        g2.setStroke(new BasicStroke((float) width));
    }

    @Override
    public void setFont(String name, String style, int size) {
        Graphics2D g2 = getGraphics();
        int nstyle = Font.PLAIN;
        if (style.equalsIgnoreCase("bold")) {
            nstyle = Font.BOLD;
        } else if (style.equalsIgnoreCase("italic")) {
            nstyle = Font.ITALIC;
        }
        g2.setFont(new Font(name, nstyle, size));
    }

    @Override
    public void drawText(String text, int x, int y) {
        Graphics2D g2 = getGraphics();
        g2.drawString(text, x, y);
        repaint();
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        Graphics2D g2 = getGraphics();
        g2.drawLine(x1, y1, x2, y2);
        repaint();
    }

    @Override
    public void drawRect(int x, int y, int w, int h) {
        Graphics2D g2 = getGraphics();
        g2.drawRect(x, y, w, h);
        repaint();
    }

    @Override
    public void drawEllipse(int x, int y, int w, int h) {
        Graphics2D g2 = getGraphics();
        g2.drawOval(x, y, w, h);
        repaint();
    }

    @Override
    public void drawCircle(int center_x, int center_y, int r) {
        Graphics2D g2 = getGraphics();
        g2.drawOval(center_x - r, center_y - r, 2 * r, 2 * r);
        repaint();
    }

    @Override
    public void fillRect(int x, int y, int w, int h) {
        Graphics2D g2 = getGraphics();
        g2.fillRect(x, y, w, h);
        repaint();
    }

    @Override
    public void fillEllipse(int x, int y, int w, int h) {
        Graphics2D g2 = getGraphics();
        g2.fillOval(x, y, w, h);
        repaint();
    }

    @Override
    public void fillCircle(int center_x, int center_y, int r) {
        Graphics2D g2 = getGraphics();
        g2.fillOval(center_x - r, center_y - r, 2 * r, 2 * r);
        repaint();
    }

    @Override
    public void drawImage(String strPathOrURL, int x, int y, int w, int h) {
        Graphics2D g2 = getGraphics();
        try {
            BufferedImage img = null;
            if (strPathOrURL.contains("file:") || strPathOrURL.contains("http:") || strPathOrURL.contains("ftp:")) {   //file:, http:,...
                img = ImageIO.read(new URL(strPathOrURL));
            } else {
                img = ImageIO.read(new File(strPathOrURL));
            }

            if (img != null) {
                g2.drawImage(img, x, y, w, h, null);
            }
            repaint();
        } catch (Exception e) {
            log.error(e);
        }
    }

    @Override
    public void drawImage(String strPathOrURL, int x, int y) {
        Graphics2D g2 = getGraphics();
        try {
            BufferedImage img = null;
            if (strPathOrURL.contains("file:") || strPathOrURL.contains("http:") || strPathOrURL.contains("ftp:")) {   //file:, http:,...
                img = ImageIO.read(new URL(strPathOrURL));
            } else {
                img = ImageIO.read(new File(strPathOrURL));
            }

            if (img != null) {
                g2.drawImage(img, x, y, null);
            }
            repaint();
        } catch (Exception e) {
            log.error(e);
        }
    }

    @Override
    public int getTextWidth(String text) {
        Graphics2D g2 = getGraphics();
        FontRenderContext frc = g2.getFontRenderContext();
        Font font = g2.getFont();
        LineMetrics metrics = font.getLineMetrics(text, frc);
        return (int) font.getStringBounds(text, frc).getMaxX();
    }

    @Override
    public int getTextHeight(String text) {
        Graphics2D g2 = getGraphics();
        FontRenderContext frc = g2.getFontRenderContext();
        Font font = g2.getFont();
        LineMetrics metrics = font.getLineMetrics(text, frc);
        return (int) metrics.getHeight();
    }

    @Override
    public void translate(int x, int y) {
        Graphics2D g2 = getGraphics();
        g2.translate(x, y);
    }

    @Override
    public void rotate(double angle, int x, int y) {
        Graphics2D g2 = getGraphics();
        g2.rotate(Math.toRadians(angle), x, y);
    }

    @Override
    public void scale(double x, double y) {
        g2.scale(x, y);
    }

    @Override
    public void shear(double x, double y) {
        g2.shear(x, y);
    }

    @Override
    public BufferedImage createCompatibleImage(int w, int h) {
        return Workspace.createCompatibleImage(w, h);
    }

    @Override
    public BufferedImage createCompatibleImage(int w, int h, BufferedImage image) {
        return Workspace.createCompatibleImage(w, h, image);
    }

    @Override
    public SketchletPainter getSketchletPainter() {
        if (PlaybackFrame.playbackFrame != null) {
            return PlaybackFrame.playbackFrame[0].playbackPanel;
        }
        if (SketchletEditor.editorPanel != null) {
            if (SketchletEditor.editorPanel.internalPlaybackPanel != null) {
                return SketchletEditor.editorPanel.internalPlaybackPanel;
            } else {
                return SketchletEditor.editorPanel;
            }
        }

        return null;
    }
}
