/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.regions.renderer;

import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.programming.screenscripts.AWTRobotUtil;
import net.sf.sketchlet.designer.ui.playback.RefreshScreenCaptureThread;

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
        String strImage = region.strImageUrlField;
        if (strImage == null) {
            strImage = "";
        } else {
            strImage = strImage.trim();
        }
        if (!strImage.isEmpty() || region.bCaptureScreen) {
            if (region.bCaptureScreen) {
                Dimension dimScreenSize = Toolkit.getDefaultToolkit().getScreenSize();

                int areaX = 0;
                int areaY = 0;
                int areaWidth = 800;
                int areaHeight = 600;

                try {
                    areaX = (int) Double.parseDouble(region.processText(region.strCaptureScreenX));
                    areaY = (int) Double.parseDouble(region.processText(region.strCaptureScreenY));
                    areaWidth = (int) Double.parseDouble(region.processText(region.strCaptureScreenWidth));
                    areaHeight = (int) Double.parseDouble(region.processText(region.strCaptureScreenHeight));
                } catch (Exception e) {
                }

                RefreshScreenCaptureThread.start();

                Rectangle rectScreenSize = new Rectangle(areaX, areaY, areaWidth, areaHeight);
                region.image = AWTRobotUtil.robot.createScreenCapture(rectScreenSize);
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
                if (region.bFitToBox) {
                    region.renderer.drawImageWin(g2, region.image, region.x1, region.y1, region.x2 - region.x1, region.y2 - region.y1);
                } else {
                    int xImage;
                    int yImage;
                    if (region.strHAlign.equals("left") || region.strHAlign.isEmpty()) {
                        xImage = region.x1;
                    } else if (region.strHAlign.equals("center")) {
                        xImage = region.x1 + (region.x2 - region.x1) / 2 - region.image.getWidth() / 2;
                    } else {
                        xImage = region.x2 - region.image.getWidth();
                    }

                    if (region.strVAlign.equals("top") || region.strVAlign.isEmpty()) {
                        yImage = region.y1;
                    } else if (region.strVAlign.equals("center")) {
                        yImage = region.y1 + (region.y2 - region.y1) / 2 - region.image.getHeight() / 2;
                    } else {
                        yImage = region.y2 - region.image.getHeight();
                    }

                    region.renderer.drawImageWin(g2, region.image, xImage, yImage, region.image.getWidth(), region.image.getHeight());
                }
            }
        }

        region.strPrevImage = strImage;
    }
}
