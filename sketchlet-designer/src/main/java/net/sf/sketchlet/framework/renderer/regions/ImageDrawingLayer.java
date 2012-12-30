package net.sf.sketchlet.framework.renderer.regions;

import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.playback.ui.RefreshScreenCaptureThread;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.model.programming.screenscripts.AWTRobotUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.net.URL;

/**
 * @author zobrenovic
 */
public class ImageDrawingLayer extends DrawingLayer {

    public ImageDrawingLayer(ActiveRegion region) {
        super(region);
    }

    public void dispose() {
        region = null;
    }

    public void draw(Graphics2D g2, Component component, boolean bPlayback) {
        ActiveRegion region = this.region;
        if (region == null) {
            return;
        }
        String strImage = region.getImageUrlField();
        if (strImage == null) {
            strImage = "";
        } else {
            strImage = strImage.trim();
        }
        if (!strImage.isEmpty() || region.isScreenCapturingEnabled()) {
            if (region.isScreenCapturingEnabled()) {
                int areaX = 0;
                int areaY = 0;
                int areaWidth = 800;
                int areaHeight = 600;

                try {
                    areaX = (int) Double.parseDouble(region.processText(region.getCaptureScreenX()));
                    areaY = (int) Double.parseDouble(region.processText(region.getCaptureScreenY()));
                    areaWidth = (int) Double.parseDouble(region.processText(region.getCaptureScreenWidth()));
                    areaHeight = (int) Double.parseDouble(region.processText(region.getCaptureScreenHeight()));
                } catch (Exception e) {
                }

                RefreshScreenCaptureThread.start();

                Rectangle rectScreenSize = new Rectangle(areaX, areaY, areaWidth, areaHeight);
                region.setImage(AWTRobotUtil.getRobot().createScreenCapture(rectScreenSize));
            } else {
                strImage = region.processText(strImage);
                if (region.getImage() == null || !strImage.equals(region.getPreviousImage())) {
                    try {
                        if (strImage.contains("file:") || strImage.contains("http:") || strImage.contains("ftp:")) {   //file:, http:,...
                            region.setImage(ImageIO.read(new URL(strImage)));
                        } else {
                            region.setImage(ImageIO.read(new File(strImage)));
                        }

                    } catch (Exception e) {
                        region.setImage(Workspace.createCompatibleImage(10, 10));
                    }
                }
            }
            if (region.getImage() != null) {
                if (region.isFitToBoxEnabled()) {
                    region.getRenderer().drawImageWin(g2, region.getImage(), region.getX1Value(), region.getY1Value(), region.getX2Value() - region.getX1Value(), region.getY2Value() - region.getY1Value());
                } else {
                    int xImage;
                    int yImage;
                    if (region.getHorizontalAlignment().equals("left") || region.getHorizontalAlignment().isEmpty()) {
                        xImage = region.getX1Value();
                    } else if (region.getHorizontalAlignment().equals("center")) {
                        xImage = region.getX1Value() + (region.getX2Value() - region.getX1Value()) / 2 - region.getImage().getWidth() / 2;
                    } else {
                        xImage = region.getX2Value() - region.getImage().getWidth();
                    }

                    if (region.getVerticalAlignment().equals("top") || region.getVerticalAlignment().isEmpty()) {
                        yImage = region.getY1Value();
                    } else if (region.getVerticalAlignment().equals("center")) {
                        yImage = region.getY1Value() + (region.getY2Value() - region.getY1Value()) / 2 - region.getImage().getHeight() / 2;
                    } else {
                        yImage = region.getY2Value() - region.getImage().getHeight();
                    }

                    region.getRenderer().drawImageWin(g2, region.getImage(), xImage, yImage, region.getImage().getWidth(), region.getImage().getHeight());
                }
            }
        }

        region.setPreviousImage(strImage);
    }
}
