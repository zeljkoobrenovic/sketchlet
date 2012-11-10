/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor;

import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.designer.tools.imagecache.ImageCache;
import net.sf.sketchlet.util.RefreshTime;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author zobrenovic
 */
public class SketchletEditorImages {

    SketchletEditor editor;

    public SketchletEditorImages(SketchletEditor editor) {
        this.editor = editor;
    }

    public BufferedImage createEmptyImage() {
        BufferedImage img = null;
        int w = Toolkit.getDefaultToolkit().getScreenSize().width;
        int h = Toolkit.getDefaultToolkit().getScreenSize().height;
        if (editor.currentPage.images[0] != null) {
            w = editor.currentPage.images[0].getWidth();
            h = editor.currentPage.images[0].getHeight();
        }

        img = Workspace.createCompatibleImage(w, h, img);
        return img;
    }

    protected void clearImage(int _l, boolean bSaveUndo) {
        if (bSaveUndo) {
            editor.saveImageUndo();
        }

        editor.tool.deactivate();

        int w, h;
        if (editor.currentPage.pageWidth > 0 && editor.currentPage.pageHeight > 0) {
            w = editor.currentPage.pageWidth;
            h = editor.currentPage.pageHeight;
        } else {
            w = (int) InteractionSpace.sketchWidth;
            h = (int) InteractionSpace.sketchHeight;
        }

        editor.currentPage.pageWidth = w;
        editor.currentPage.pageHeight = h;

        ImageCache.remove(editor.currentPage.getLayerImageFile(_l));

        editor.currentPage.images[_l] = null;
        editor.currentPage.imageUpdated[_l] = false;
        editor.createGraphics();
        editor.tool.activate();

        editor.revalidate();
        RefreshTime.update();
        editor.repaint();
    }

    public void rotateClockwise() {
        if (editor.tool != null) {
            editor.tool.deactivate();
        }
        editor.saveImageUndo();
        BufferedImage img = editor.getImage();
        BufferedImage img2 = ImageOperations.rotateClockwise(img);
        editor.setImage(img2);
        editor.revalidate();
        RefreshTime.update();

        editor.repaint();
        if (editor.tool != null) {
            editor.tool.activate();
            editor.createGraphics();
        }
    }

    public void rotateAntiClockwise() {
        if (editor.tool != null) {
            editor.tool.deactivate();
        }
        editor.saveImageUndo();
        BufferedImage img = editor.getImage();
        BufferedImage img2 = ImageOperations.rotateAntiClockwise(img);
        editor.setImage(img2);
        editor.revalidate();
        RefreshTime.update();

        editor.repaint();
        if (editor.tool != null) {
            editor.tool.activate();
            editor.createGraphics();
        }
    }

    public void flipHorizontal() {
        if (editor.tool != null) {
            editor.tool.deactivate();
        }
        editor.saveImageUndo();
        BufferedImage img = editor.getImage();
        BufferedImage img2 = ImageOperations.flipHorizontal(img);
        editor.setImage(img2);
        editor.revalidate();
        RefreshTime.update();

        editor.repaint();
        if (editor.tool != null) {
            editor.tool.activate();
            editor.createGraphics();
        }
    }

    public void flipVertical() {
        if (editor.tool != null) {
            editor.tool.deactivate();
        }
        editor.saveImageUndo();
        BufferedImage img = editor.getImage();
        BufferedImage img2 = ImageOperations.flipVertical(img);
        editor.setImage(img2);
        editor.revalidate();
        RefreshTime.update();

        editor.repaint();
        if (editor.tool != null) {
            editor.tool.activate();
            editor.createGraphics();
        }
    }
}
