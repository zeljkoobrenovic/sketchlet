package net.sf.sketchlet.designer.editor.ui.undo;

import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionPanel;
import net.sf.sketchlet.framework.model.ActiveRegion;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author zobrenovic
 */
public class RegionImageChangedUndoAction extends UndoAction {

    File tempImageFile;
    BufferedImage newImg;
    ActiveRegion region;
    int frame = 0;

    public RegionImageChangedUndoAction(BufferedImage img, ActiveRegion region, int frame) {
        this.frame = frame;
        this.region = region;
        try {
            newImg = Workspace.createCompatibleImage(img.getWidth(), img.getHeight());
            Graphics2D g2 = newImg.createGraphics();
            g2.drawImage(img, 0, 0, null);
            g2.dispose();
            newImg.flush();
            new Thread(new Runnable() {

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
            }).start();
        } catch (Exception e) {
        }
    }

    public void restore() {
        if (tempImageFile != null && region != null) {
            if (SketchletEditor.getInstance().getCurrentPage() != this.region.getSketch()) {
                SketchletEditor.getInstance().openSketchAndWait(this.region.getSketch());
            }
            try {
                BufferedImage img = newImg;
                if (img == null) {
                    img = ImageIO.read(tempImageFile);
                }
                int w = img.getWidth(null);
                int h = img.getHeight(null);
                BufferedImage bi = Workspace.createCompatibleImage(w, h);
                Graphics2D g2 = bi.createGraphics();
                g2.drawImage(img, 0, 0, null);
                g2.dispose();
                region.setDrawnImage(frame, img);
                //FreeHand.editorPanel.repaint();
                //FreeHand.editorPanel.enableControls();
                tempImageFile.delete();
                tempImageFile = null;

                if (ActiveRegionPanel.getCurrentActiveRegionPanel() != null && ActiveRegionPanel.getCurrentActiveRegionPanel().getImageEditor() != null) {
                    ActiveRegionPanel.getCurrentActiveRegionPanel().getImageEditor().repaint();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean shouldUndo() {
        return region.getSketch().getRegions().getRegions().contains(region) && region.getAdditionalDrawnImages().size() >= frame;
    }
}
