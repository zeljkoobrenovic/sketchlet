package net.sf.sketchlet.framework.renderer.batch;

import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.renderer.regions.ActiveRegionRenderer;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author zeljko
 */

/**
 * <region>
 * <parameters>
 * <property name="width">1500</property>
 * <property name="height">500</property>
 * </parameters>
 * <widget type="PlantUML">
 * <parameters>
 * <property name="items"><![CDATA[
 * Class A
 * ]]></property>
 * <property name="resize region">true</property>
 * </parameters>
 * </widget>
 * </region>
 */
public class BatchRenderer {
    private static final Logger log = Logger.getLogger(BatchRenderer.class);

    public static void main(String args[]) {
        Workspace.initForBatchProcessing();
        ActiveRegion region = ActiveRegion.getInstanceForBatchProcessing();
        region.setProperty("width", "1500");
        region.setProperty("height", "500");

        region.setWidget("PlantUML");
        region.setWidgetItems("Class A");
        region.setWidgetProperties(new Properties());
        region.getWidgetProperties().put("resize region", "true");

        region.initPlayback();
        region.activate(true);

        int w = region.getWidthValue();
        int h = region.getHeightValue();
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        ActiveRegionRenderer renderer = new ActiveRegionRenderer(region);
        renderer.draw(g);

        int widthAfterRendering = region.getWidthValue();
        int heightAfterRendering = region.getHeightValue();

        if (w != widthAfterRendering || h != heightAfterRendering) {
            BufferedImage imageAfterRendering = new BufferedImage(widthAfterRendering, heightAfterRendering, BufferedImage.TYPE_INT_ARGB);
            imageAfterRendering.getGraphics().drawImage(image, 0, 0, null);
            image = imageAfterRendering;
        }

        try {
            ImageIO.write(image, "png", new File("/temp/image.png"));
        } catch (IOException e) {
            log.error(e);
        } finally {
            g.dispose();
        }
    }
}
