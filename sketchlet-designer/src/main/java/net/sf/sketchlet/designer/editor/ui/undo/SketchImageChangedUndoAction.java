package net.sf.sketchlet.designer.editor.ui.undo;

import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.framework.model.Page;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author zobrenovic
 */
public class SketchImageChangedUndoAction extends UndoAction {

    //    static Vector<BufferedImage> undoImages = new Vector<BufferedImage>();
    File tempImageFile;
    int layer;
    BufferedImage newImg;
    Page page;

    public SketchImageChangedUndoAction(Page page, BufferedImage img, int layer) {
        this.page = page;
        this.layer = layer;
        try {
            newImg = Workspace.createCompatibleImage(img.getWidth(), img.getHeight());
            Graphics2D g2 = newImg.createGraphics();
            g2.drawImage(img, 0, 0, null);
            g2.dispose();
            newImg.flush();
            /*
            undoImages.add(newImg);
            if (undoImages.size() > 5) {
            undoImages.remove(0);
            }
             */
            Thread t = new Thread(new Runnable() {

                public void run() {
                    try {
                        while (SketchletEditor.getInstance().isDragging()) {
                            Thread.sleep(10);
                        }
                        tempImageFile = File.createTempFile("undo", ".png");
                        tempImageFile.deleteOnExit();
                        ImageIO.write(newImg, "png", tempImageFile);
                        newImg.flush();
                        newImg = null;
                    } catch (Exception e) {
                    }
                }
            });
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();
        } catch (Exception e) {
        }
    }

    public void restore() {
        if (tempImageFile != null) {
            if (SketchletEditor.getInstance().getCurrentPage() != page) {
                SketchletEditor.getInstance().openSketchAndWait(page);
            }
            try {
                BufferedImage img = newImg;
                if (img == null) {
                    img = ImageIO.read(tempImageFile);
                }
                int w = img.getWidth();
                int h = img.getHeight();
                BufferedImage bi = Workspace.createCompatibleImage(w, h);
                Graphics2D g2 = bi.createGraphics();
                g2.drawImage(img, 0, 0, null);
                g2.dispose();
                SketchletEditor.getInstance().updateImage(layer, img);
                //FreeHand.editorPanel.repaint();
                //FreeHand.editorPanel.enableControls();
                tempImageFile.delete();
                tempImageFile = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /*
        if (undoImages.size() > 0) {
        SketchletEditor.editorPanel.bahabahaImages[SketchletEditor.editorPanel.layer] = undoImages.remove(undoImages.size() - 1);
        SketchletEditor.editorPanel.bahabahaimageUpdated[SketchletEditor.editorPanel.layer] = true;
        SketchletEditor.editorPanel.repaint();
        SketchletEditor.editorPanel.enableControls();
        }
         */
    }
}
