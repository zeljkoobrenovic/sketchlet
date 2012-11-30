package net.sf.sketchlet.designer.editor.media;

import net.sf.sketchlet.designer.Workspace;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * @author zobrenovic
 */
public class ImageOperations {

    public static BufferedImage rotateClockwise(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage img2 = Workspace.createCompatibleImage(h, w);
        Graphics2D g2 = img2.createGraphics();
        g2.rotate(Math.PI / 2, 0, 0);
        g2.translate(0, -h);
        g2.drawImage(img, 0, 0, null);
        g2.dispose();

        return img2;
    }

    public static BufferedImage rotateAntiClockwise(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage img2 = Workspace.createCompatibleImage(h, w);
        Graphics2D g2 = img2.createGraphics();
        g2.rotate(-Math.PI / 2, 0, 0);
        g2.translate(-w, 0);
        g2.drawImage(img, 0, 0, null);
        g2.dispose();

        return img2;
    }

    public static BufferedImage flipHorizontal(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage img2 = Workspace.createCompatibleImage(w, h);
        Graphics2D g2 = img2.createGraphics();
        AffineTransform reflectTransform = AffineTransform.getScaleInstance(-1.0, 1.0);
        reflectTransform.translate(-w, 0);
        g2.drawImage(img, reflectTransform, null);
        g2.dispose();

        return img2;
    }

    public static BufferedImage flipVertical(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage img2 = Workspace.createCompatibleImage(w, h);
        Graphics2D g2 = img2.createGraphics();
        AffineTransform reflectTransform = AffineTransform.getScaleInstance(1.0, -1.0);
        reflectTransform.translate(0, -h);
        g2.drawImage(img, reflectTransform, null);
        g2.dispose();

        return img2;
    }
}
 