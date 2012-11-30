package net.sf.sketchlet.designer.editor.media;

import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.framework.model.imagecache.ImageCache;
import net.sf.sketchlet.util.RefreshTime;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author zobrenovic
 */
public class SketchletEditorImagesHandler {

    private SketchletEditor editor;

    public SketchletEditorImagesHandler(SketchletEditor editor) {
        this.editor = editor;
    }

    public BufferedImage createEmptyImage() {
        BufferedImage img = null;
        int w = Toolkit.getDefaultToolkit().getScreenSize().width;
        int h = Toolkit.getDefaultToolkit().getScreenSize().height;
        if (editor.getCurrentPage().getImages()[0] != null) {
            w = editor.getCurrentPage().getImages()[0].getWidth();
            h = editor.getCurrentPage().getImages()[0].getHeight();
        }

        img = Workspace.createCompatibleImage(w, h, img);
        return img;
    }

    public void clearImage(int _l, boolean bSaveUndo) {
        if (bSaveUndo) {
            editor.saveImageUndo();
        }

        editor.getTool().deactivate();

        int w, h;
        if (editor.getCurrentPage().getPageWidth() > 0 && editor.getCurrentPage().getPageHeight() > 0) {
            w = editor.getCurrentPage().getPageWidth();
            h = editor.getCurrentPage().getPageHeight();
        } else {
            w = (int) InteractionSpace.getSketchWidth();
            h = (int) InteractionSpace.getSketchHeight();
        }

        editor.getCurrentPage().setPageWidth(w);
        editor.getCurrentPage().setPageHeight(h);

        ImageCache.remove(editor.getCurrentPage().getLayerImageFile(_l));

        editor.getCurrentPage().getImages()[_l] = null;
        editor.getCurrentPage().getImageUpdated()[_l] = false;
        editor.createGraphics();
        editor.getTool().activate();

        editor.revalidate();
        RefreshTime.update();
        editor.repaint();
    }

    public void rotateClockwise() {
        if (editor.getTool() != null) {
            editor.getTool().deactivate();
        }
        editor.saveImageUndo();
        BufferedImage img = editor.getImage();
        BufferedImage img2 = ImageOperations.rotateClockwise(img);
        editor.setImage(img2);
        editor.revalidate();
        RefreshTime.update();

        editor.repaint();
        if (editor.getTool() != null) {
            editor.getTool().activate();
            editor.createGraphics();
        }
    }

    public void rotateAntiClockwise() {
        if (editor.getTool() != null) {
            editor.getTool().deactivate();
        }
        editor.saveImageUndo();
        BufferedImage img = editor.getImage();
        BufferedImage img2 = ImageOperations.rotateAntiClockwise(img);
        editor.setImage(img2);
        editor.revalidate();
        RefreshTime.update();

        editor.repaint();
        if (editor.getTool() != null) {
            editor.getTool().activate();
            editor.createGraphics();
        }
    }

    public void flipHorizontal() {
        if (editor.getTool() != null) {
            editor.getTool().deactivate();
        }
        editor.saveImageUndo();
        BufferedImage img = editor.getImage();
        BufferedImage img2 = ImageOperations.flipHorizontal(img);
        editor.setImage(img2);
        editor.revalidate();
        RefreshTime.update();

        editor.repaint();
        if (editor.getTool() != null) {
            editor.getTool().activate();
            editor.createGraphics();
        }
    }

    public void flipVertical() {
        if (editor.getTool() != null) {
            editor.getTool().deactivate();
        }
        editor.saveImageUndo();
        BufferedImage img = editor.getImage();
        BufferedImage img2 = ImageOperations.flipVertical(img);
        editor.setImage(img2);
        editor.revalidate();
        RefreshTime.update();

        editor.repaint();
        if (editor.getTool() != null) {
            editor.getTool().activate();
            editor.createGraphics();
        }
    }
}
