/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.tools.imagecache;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.ui.MessageFrame;
import net.sf.sketchlet.designer.ui.playback.PlaybackFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

public class ImageCache {

    public static Hashtable<File, BufferedImage> images = null;

    public static void clear() {
        if (images != null) {
            if (SketchletEditor.editorFrame != null) {
                SketchletEditor.editorFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }
            flush();
            images.clear();
            images = null;
            if (SketchletEditor.editorFrame != null) {
                SketchletEditor.editorFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

    public static void flush() {
        if (images != null) {
            for (BufferedImage img : images.values()) {
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
                    if (images != null) {
                        flush();
                        images.clear();
                    }
                    images = new Hashtable<File, BufferedImage>();
                    String strDir = SketchletContextUtils.getCurrentProjectSkecthletsDir();
                    //File dir = new File(strDir);
                    //File files[] = dir.listFiles();
                    //for (int i = 0; i < files.length; i++) {
                    for (Page s : SketchletEditor.pages.pages) {
                        Vector<File> imageFiles = s.getImageFiles();
                        for (File file : imageFiles) {
                            if (file.exists() && file.getName().endsWith(".png")) {
                                BufferedImage img = read(file);
                                images.put(file, img);
                            }
                        }
                    }
                } catch (OutOfMemoryError oome) {
                    flush();
                    if (images != null) {
                        images.clear();
                    }
                    images = null;
                    System.gc();
                    if (PlaybackFrame.playbackFrame == null) {
                        JOptionPane.showMessageDialog(SketchletEditor.editorFrame, "No enough memory to load images!");
                    }
                } catch (Throwable e) {
                    flush();
                    if (images != null) {
                        images.clear();
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
        if (images != null) {
            if (!bForceRead) {
                BufferedImage img = images.get(file);
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
        tempImage = null;

        if (images != null) {
            images.put(file, image);
        }

        return image;
    }

    public static boolean write(BufferedImage image, File file) throws IOException {
        if (images != null) {
            BufferedImage img = images.get(file);
            if (img != null) {
                images.remove(img);
                img.flush();
                img = null;
                images.put(file, image);
            }
        }
        return ImageIO.write(image, "png", file);
    }

    public static void remove(File file) {
        if (images != null) {
            images.remove(file);
        }
        if (file.exists()) {
            file.delete();
        }
    }

    public VolatileImage getVolatileImage(File file) throws IOException {
        BufferedImage bImage = read(file);
        VolatileImage vImage = Workspace.createCompatibleVolatileImage(bImage.getWidth(), bImage.getHeight());
        Graphics2D g2d = vImage.createGraphics();
        g2d.drawImage(bImage, 0, 0, null);
        g2d.dispose();
        bImage.flush();
        bImage = null;
        return vImage;
    }
}
