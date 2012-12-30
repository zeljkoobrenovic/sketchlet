package net.sf.sketchlet.framework.renderer.regions;

import net.sf.sketchlet.framework.model.ActiveRegion;

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
        if (index > region.getAdditionalDrawnImages().size()) {
            index = region.getAdditionalImageFileNames().size();
        }
        if (index < 0) {
            index = 0;
        }
    }

    public void init() {
        ActiveRegion region = this.region;
        if (region == null) {
            return;
        }
        if (!region.getDrawnImageFileName(0).isEmpty() && region.getDrawnImage(0) == null) {
            region.initImage(0);
        }
        for (int aai = 0; aai < region.getAdditionalDrawnImages().size(); aai++) {
            String strAdditionalImage = region.getAdditionalImageFileNames().get(aai);
            BufferedImage additionalImage = region.getAdditionalDrawnImages().get(aai);

            if (!strAdditionalImage.equals("") && additionalImage == null) {
                region.initImage(aai + 1);
            }
        }
    }

    public void draw(Graphics2D g2, Component component, boolean bPlayback) {
        ActiveRegion region = this.region;
        if (region == null) {
            return;
        }
        init();
        if (region.getDrawnImage(index) != null && region.getDrawnImage(index).getWidth() > 1) {
            if (region.isFitToBoxEnabled()) {
                region.getRenderer().drawImageWin(g2, region.getDrawnImage(index), region.getX1Value(), region.getY1Value(), region.getX2Value() - region.getX1Value(), region.getY2Value() - region.getY1Value());
            } else {
                int xImage;
                int yImage;
                if (region.getHorizontalAlignment().equals("left") || region.getHorizontalAlignment().isEmpty()) {
                    xImage = region.getX1Value();
                } else if (region.getHorizontalAlignment().equals("center")) {
                    xImage = region.getX1Value() + (region.getX2Value() - region.getX1Value()) / 2 - region.getDrawnImage(index).getWidth() / 2;
                } else {
                    xImage = region.getX2Value() - region.getDrawnImage(index).getWidth();
                }

                if (region.getVerticalAlignment().equals("top") || region.getVerticalAlignment().isEmpty()) {
                    yImage = region.getY1Value();
                } else if (region.getVerticalAlignment().equals("center")) {
                    yImage = region.getY1Value() + (region.getY2Value() - region.getY1Value()) / 2 - region.getDrawnImage(index).getHeight() / 2;
                } else {
                    yImage = region.getY2Value() - region.getDrawnImage(index).getHeight();
                }

                region.getRenderer().drawImageWin(g2, region.getDrawnImage(index), xImage, yImage, region.getDrawnImage(index).getWidth(), region.getDrawnImage(index).getHeight());
            }
        }
    }
}
