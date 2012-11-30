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
        String strImage = region.imageUrlField;
        if (strImage == null) {
            strImage = "";
        } else {
            strImage = strImage.trim();
        }
        if (!strImage.isEmpty() || region.screenCapturingEnabled) {
            if (region.screenCapturingEnabled) {
                int areaX = 0;
                int areaY = 0;
                int areaWidth = 800;
                int areaHeight = 600;

                try {
                    areaX = (int) Double.parseDouble(region.processText(region.captureScreenX));
                    areaY = (int) Double.parseDouble(region.processText(region.captureScreenY));
                    areaWidth = (int) Double.parseDouble(region.processText(region.captureScreenWidth));
                    areaHeight = (int) Double.parseDouble(region.processText(region.captureScreenHeight));
                } catch (Exception e) {
                }

                RefreshScreenCaptureThread.start();

                Rectangle rectScreenSize = new Rectangle(areaX, areaY, areaWidth, areaHeight);
                region.image = AWTRobotUtil.getRobot().createScreenCapture(rectScreenSize);
            } else {
                strImage = region.processText(strImage);
                if (region.image == null || !strImage.equals(region.strPrevImage)) {
                    try {
                        if (strImage.contains("file:") || strImage.contains("http:") || strImage.contains("ftp:")) {   //file:, http:,...
                            region.image = ImageIO.read(new URL(strImage));
                        } else {
                            region.image = ImageIO.read(new File(strImage));
                        }

                    } catch (Exception e) {
                        region.image = Workspace.createCompatibleImage(10, 10);
                    }
                }
            }
            if (region.image != null) {
                if (region.fitToBoxEnabled) {
                    region.getRenderer().drawImageWin(g2, region.image, region.x1, region.y1, region.x2 - region.x1, region.y2 - region.y1);
                } else {
                    int xImage;
                    int yImage;
                    if (region.horizontalAlignment.equals("left") || region.horizontalAlignment.isEmpty()) {
                        xImage = region.x1;
                    } else if (region.horizontalAlignment.equals("center")) {
                        xImage = region.x1 + (region.x2 - region.x1) / 2 - region.image.getWidth() / 2;
                    } else {
                        xImage = region.x2 - region.image.getWidth();
                    }

                    if (region.verticalAlignment.equals("top") || region.verticalAlignment.isEmpty()) {
                        yImage = region.y1;
                    } else if (region.verticalAlignment.equals("center")) {
                        yImage = region.y1 + (region.y2 - region.y1) / 2 - region.image.getHeight() / 2;
                    } else {
                        yImage = region.y2 - region.image.getHeight();
                    }

                    region.getRenderer().drawImageWin(g2, region.image, xImage, yImage, region.image.getWidth(), region.image.getHeight());
                }
            }
        }

        region.strPrevImage = strImage;
    }
}
