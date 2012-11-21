/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.renderer.regions;

import net.sf.sketchlet.model.ActiveRegion;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author zobrenovic
 */
public class DrawnImageLayer extends DrawingLayer {

    private int index;

    public DrawnImageLayer(ActiveRegion region) {
        super(region);
    }

    public void dispose() {
        region = null;
    }

    public void setIndex(int i) {
        this.index = i;
        if (index > region.additionalDrawImages.size()) {
            index = region.additionalImageFile.size();
        }
        if (index < 0) {
            index = 0;
        }
    }

    public void init() {
        if (!region.getDrawImageFileName(0).isEmpty() && region.getDrawImage(0) == null) {
            region.initImage(0);
        }
        for (int aai = 0; aai < region.additionalDrawImages.size(); aai++) {
            String strAdditionalImage = region.additionalImageFile.elementAt(aai);
            BufferedImage additionalImage = region.additionalDrawImages.elementAt(aai);

            if (!strAdditionalImage.equals("") && additionalImage == null) {
                region.initImage(aai + 1);
            }
        }
    }

    public void draw(Graphics2D g2, Component component, boolean bPlayback) {
        init();
        if (region.getDrawImage(index) != null && region.getDrawImage(index).getWidth() > 1) {
            if (region.fitToBoxEnabled) {
                region.getRenderer().drawImageWin(g2, region.getDrawImage(index), region.x1, region.y1, region.x2 - region.x1, region.y2 - region.y1);
            } else {
                int xImage;
                int yImage;
                if (region.horizontalAlignment.equals("left") || region.horizontalAlignment.isEmpty()) {
                    xImage = region.x1;
                } else if (region.horizontalAlignment.equals("center")) {
                    xImage = region.x1 + (region.x2 - region.x1) / 2 - region.getDrawImage(index).getWidth() / 2;
                } else {
                    xImage = region.x2 - region.getDrawImage(index).getWidth();
                }

                if (region.verticalAlignment.equals("top") || region.verticalAlignment.isEmpty()) {
                    yImage = region.y1;
                } else if (region.verticalAlignment.equals("center")) {
                    yImage = region.y1 + (region.y2 - region.y1) / 2 - region.getDrawImage(index).getHeight() / 2;
                } else {
                    yImage = region.y2 - region.getDrawImage(index).getHeight();
                }

                region.getRenderer().drawImageWin(g2, region.getDrawImage(index), xImage, yImage, region.getDrawImage(index).getWidth(), region.getDrawImage(index).getHeight());
            }
        }
    }
}
