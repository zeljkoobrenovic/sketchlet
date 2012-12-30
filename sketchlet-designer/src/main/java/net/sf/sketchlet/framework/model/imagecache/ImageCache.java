package net.sf.sketchlet.framework.model.imagecache;

import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.MessageFrame;
import net.sf.sketchlet.designer.playback.ui.PlaybackFrame;
import net.sf.sketchlet.framework.model.Page;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

public class ImageCache {

    private static Hashtable<File, BufferedImage> images = null;

    public static void clear() {
        if (getImages() != null) {
            if (SketchletEditor.editorFrame != null) {
                SketchletEditor.editorFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }
            flush();
            getImages().clear();
            images = null;
            if (SketchletEditor.editorFrame != null) {
                SketchletEditor.editorFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

    public static void flush() {
        if (getImages() != null) {
            for (BufferedImage img : getImages().values()) {
                img.flush();
            }
        }
    }

    public static void load() {
        final boolean bHide = !MessageFrame.isOpen();
        if (bHide) {
            MessageFrame.showMessage(SketchletEditor.editorFrame, "Caching images...", SketchletEditor.editorFrame);
        }
        new Thread(new Runnable() {

            public void run() {
                try {
                    if (getImages() != null) {
                        flush();
                        getImages().clear();
                    }
                    images = new Hashtable<File, BufferedImage>();
                    for (Page s : SketchletEditor.getProject().getPages()) {
                        Vector<File> imageFiles = s.getImageFiles();
                        for (File file : imageFiles) {
                            if (file.exists() && file.getName().endsWith(".png")) {
                                BufferedImage img = read(file);
                                getImages().put(file, img);
                            }
                        }
                    }
                } catch (OutOfMemoryError oome) {
                    flush();
                    if (getImages() != null) {
                        getImages().clear();
                    }
                    images = null;
                    System.gc();
                    if (PlaybackFrame.playbackFrame == null) {
                        JOptionPane.showMessageDialog(SketchletEditor.editorFrame, "No enough memory to load images!");
                    }
                } catch (Throwable e) {
                    flush();
                    if (getImages() != null) {
                        getImages().clear();
                    }
                    images = null;
                    System.gc();
                } finally {
                    if (bHide) {
                        MessageFrame.closeMessage();
                    }
                }
            }
        }).start();
    }

    public static BufferedImage read(File file) throws IOException {
        return read(file, null);
    }

    public static BufferedImage read(File file, BufferedImage oldImage) throws IOException {
        return read(file, oldImage, false);
    }

    public static BufferedImage read(File file, BufferedImage oldImage, boolean bForceRead) throws IOException {
        if (getImages() != null) {
            if (!bForceRead) {
                BufferedImage img = getImages().get(file);
                if (img != null) {
                    return img;
                }
            }
        }
        BufferedImage tempImage = ImageIO.read(file);

        BufferedImage image = Workspace.createCompatibleImage(tempImage.getWidth(), tempImage.getHeight(), oldImage);
        Graphics2D g2 = image.createGraphics();

        g2.drawImage(tempImage, 0, 0, null);
        g2.dispose();
        tempImage.flush();

        if (getImages() != null) {
            getImages().put(file, image);
        }

        return image;
    }

    public static boolean write(BufferedImage image, File file) throws IOException {
        if (getImages() != null) {
            BufferedImage img = getImages().get(file);
            if (img != null) {
                getImages().remove(img);
                img.flush();
                getImages().put(file, image);
            }
        }
        return ImageIO.write(image, "png", file);
    }

    public static void remove(File file) {
        if (getImages() != null) {
            getImages().remove(file);
        }
        if (file.exists()) {
            file.delete();
        }
    }

    public static Hashtable<File, BufferedImage> getImages() {
        return images;
    }

}
