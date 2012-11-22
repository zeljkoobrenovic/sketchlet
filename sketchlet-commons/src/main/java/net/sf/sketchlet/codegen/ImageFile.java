/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.codegen;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 *
 * @author zobrenovic
 */
public class ImageFile {

    private String fileName = "";
    private String subDir = "files";
    private String imageType = "png";
    private BufferedImage image = null;

    public ImageFile(String fileName, BufferedImage image) {
        this.setFileName(fileName);
        this.setImage(image);
    }

    public ImageFile(String fileName, String subDir, BufferedImage image) {
        this.setFileName(fileName);
        this.setSubDir(subDir);
        this.setImage(image);
    }
    
    public void dispose() {
        this.setImage(null);
    }

    public void setType(String type) {
        this.setImageType(type);
    }

    public void exportImage(File dir) {
        try {
            if (getImage() != null && !getFileName().isEmpty()) {
                File file = new File(dir, getSubDir() + "/" + getFileName());
                file.getParentFile().mkdirs();
                ImageIO.write(getImage(), getImageType(), file);
            }
        } catch (Exception e) {
        }
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSubDir() {
        return subDir;
    }

    public void setSubDir(String subDir) {
        this.subDir = subDir;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }
}
