/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.codegen;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 *
 * @author zobrenovic
 */
public class ImageFile {

    String fileName = "";
    String subDir = "files";
    String imageType = "png";
    BufferedImage image = null;

    public ImageFile(String fileName, BufferedImage image) {
        this.fileName = fileName;
        this.image = image;
    }

    public ImageFile(String fileName, String subDir, BufferedImage image) {
        this.fileName = fileName;
        this.subDir = subDir;
        this.image = image;
    }
    
    public void dispose() {
        this.image = null;
    }

    public void setType(String type) {
        this.imageType = type;
    }

    public void exportImage(File dir) {
        try {
            if (image != null && !fileName.isEmpty()) {
                File file = new File(dir, subDir + "/" + fileName);
                file.getParentFile().mkdirs();
                ImageIO.write(image, imageType, file);
            }
        } catch (Exception e) {
        }
    }
}
