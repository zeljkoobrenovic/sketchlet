/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor;

import net.sf.sketchlet.context.SketchletGraphicsContext;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.editor.tool.SelectTool;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.ui.BufferedImageClipboardObject;
import net.sf.sketchlet.designer.ui.PasteSpecialDialog;
import net.sf.sketchlet.designer.ui.region.ActiveRegionsFrame;
import net.sf.sketchlet.util.RefreshTime;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class SketchletEditorClipboard {
    private static final Logger log = Logger.getLogger(SketchletEditorClipboard.class);
    SketchletEditor editor;

    public SketchletEditorClipboard(SketchletEditor editor) {
        this.editor = editor;
    }

    public void pasteSpecial() {
        editor.bPasting = true;
        if (editor.copiedActions != null && editor.copiedActions.size() > 0) {
            PasteSpecialDialog dlg = new PasteSpecialDialog(editor.editorFrame);
            if (!dlg.bCopy) {
                return;
            }
            int n = 1;
            int ox = 0;
            int oy = 0;
            try {
                n = Integer.parseInt(dlg.numCopies.getText());
                ox = Integer.parseInt(dlg.xOffset.getText());
                oy = Integer.parseInt(dlg.yOffset.getText());
            } catch (Throwable e) {
            }
            Vector<ActiveRegion> selectedActions = new Vector<ActiveRegion>();
            Hashtable<String, String> groups = new Hashtable<String, String>();
            ActiveRegion lastAction = null;
            for (int i = 0; i < n; i++) {
                for (ActiveRegion a : editor.copiedActions) {
                    lastAction = a;
                    ActiveRegion a2 = new ActiveRegion(a, true);
                    a2.parent = editor.currentPage.regions;
                    if (!a2.regionGrouping.isEmpty()) {
                        String newGroup = groups.get(a2.regionGrouping);
                        if (newGroup == null) {
                            newGroup = "" + System.currentTimeMillis();
                            groups.put(a2.regionGrouping, newGroup);
                        }

                        a2.regionGrouping = newGroup;
                    }
                    a2.x1 += (i + 1) * ox;
                    a2.y1 += (i + 1) * oy;
                    a2.x2 += (i + 1) * ox;
                    a2.y2 += (i + 1) * oy;

                    editor.currentPage.regions.regions.insertElementAt(a2, 0);
                    editor.currentPage.regions.selectedRegions = new Vector<ActiveRegion>();
                    editor.currentPage.regions.addToSelection(a2);
                }

                SketchletEditor.editorPanel.inCtrlMode = false;
                SketchletEditor.editorPanel.inShiftMode = false;
            }
            if (lastAction != null) {
                ActiveRegionsFrame.reload(lastAction);
            }
        } else {
        }
        editor.bPasting = false;
        RefreshTime.update();
    }

    public void fromClipboard() {
        int x = (int) (editor.scrollPane.getViewport().getViewPosition().getX() / editor.scale);
        int y = (int) (editor.scrollPane.getViewport().getViewPosition().getY() / editor.scale);
        pasteImageFromClipboard(false, x, y);
        RefreshTime.update();

        editor.repaint();
    }

    public void pasteImageFromClipboard(boolean bClear, int x, int y) {
        editor.saveImageUndo();

        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = clip.getContents(null);
        RenderedImage img = null;
        DataFlavor[] dataFlavors;

        dataFlavors = transferable.getTransferDataFlavors();

        if (transferable.isDataFlavorSupported(new DataFlavor("image/x-java-image; class=java.awt.Image", "Image"))) {
            try {
                img = (RenderedImage) transferable.getTransferData(new DataFlavor("image/x-java-image; class=java.awt.Image", "Image"));

                if (editor.currentPage.images[editor.layer] == null) {
                    editor.updateImage(editor.layer, editor.sketchletImages.createEmptyImage());
                }

                int w = Math.max(editor.currentPage.images[editor.layer].getWidth(), img.getWidth());
                int h = Math.max(editor.currentPage.images[editor.layer].getHeight(), img.getHeight());


                BufferedImage newImage = Workspace.createCompatibleImage(img.getWidth(), img.getHeight());

                if (bClear || w > editor.currentPage.images[editor.layer].getWidth() || h > editor.currentPage.images[editor.layer].getHeight()) {
                    BufferedImage tempImage = Workspace.createCompatibleImage(w, h);
                    if (!bClear) {
                        Graphics2D tempg2 = tempImage.createGraphics();
                        tempg2.drawImage(editor.currentPage.images[editor.layer], 0, 0, null);
                        tempg2.dispose();
                    }

                    editor.updateImage(editor.layer, tempImage);
                    tempImage.flush();
                    tempImage = null;
                }
                Graphics2D g2a = newImage.createGraphics();
                g2a.drawRenderedImage(img, null);
                g2a.dispose();
                editor.setTool(editor.selectTool, null);
                editor.selectTool.setClip(newImage, x, y);
                RefreshTime.update();
                editor.repaint();
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(SketchletEditor.editorFrame, "The object on the clipboard\nis not an image");
        }
        editor.createGraphics();
    }

    public void pasteImageAsRegion(int x, int y) {
        ActiveRegion a = new ActiveRegion(editor.currentPage.regions);

        a.setDrawImageChanged(0, true);

        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = clip.getContents(null);
        RenderedImage img = null;

        if (transferable.isDataFlavorSupported(new DataFlavor("image/x-java-image; class=java.awt.Image", "Image"))) {
            try {
                img = (RenderedImage) transferable.getTransferData(new DataFlavor("image/x-java-image; class=java.awt.Image", "Image"));

                a.setDrawImage(0, Workspace.createCompatibleImage(img.getWidth(), img.getHeight(), a.getDrawImage(0)));
                a.x1 = x;
                a.y1 = y;
                a.x2 = x + img.getWidth();
                a.y2 = y + img.getHeight();

                Graphics2D g2a = a.getDrawImage(0).createGraphics();
                g2a.drawRenderedImage(img, null);
                g2a.dispose();

                RefreshTime.update();
                editor.repaint();
                editor.currentPage.regions.regions.insertElementAt(a, 0);
                editor.currentPage.regions.selectedRegions = new Vector<ActiveRegion>();
                editor.currentPage.regions.addToSelection(a);
                editor.setMode(EditorMode.ACTIONS);
                ActiveRegionsFrame.reload(a);
                editor.editorFrame.requestFocus();
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        } else {
            log.info("Not an image!");
        }

    }

    public void pasteRegion(int x, int y) {
        for (ActiveRegion a : editor.copiedActions) {
            ActiveRegion a2 = new ActiveRegion(a, true);
            a2.parent = editor.currentPage.regions;
            a2.regionGrouping = "";
            int w = a2.x2 - a2.x1;
            int h = a2.y2 - a2.y1;
            a2.x1 = x;
            a2.y1 = y;
            a2.x2 = a2.x1 + w;
            a2.y2 = a2.y1 + h;
            editor.currentPage.regions.regions.insertElementAt(a2, 0);
        }

        RefreshTime.update();
        editor.repaint();
    }

    public void copy() {
        if (editor.skipKey) {
            editor.skipKey = false;
            return;
        }

        if (editor.mode == EditorMode.ACTIONS) {
            copySelectedAction();
        } else {
            if (editor.tool != null && editor.tool instanceof SelectTool && ((SelectTool) editor.tool).x1 != ((SelectTool) editor.tool).x2) {
                ((SelectTool) editor.tool).copy();
            } else if (editor.currentPage.images != null) {
                BufferedImageClipboardObject tr = new BufferedImageClipboardObject(editor.currentPage.images[editor.layer], DataFlavor.imageFlavor);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(tr, null);
            }
        }

        editor.enableControls();
    }

    public void copySelectedAction() {
        editor.copiedActions = new Vector<ActiveRegion>();
        if (editor.currentPage.regions.selectedRegions != null) {
            for (ActiveRegion a : editor.currentPage.regions.selectedRegions) {
                editor.copiedActions.add(new ActiveRegion(a, true));
            }
        }
        editor.sketchToolbar.pasteSpecial.setEnabled(true);
    }

    public void printRegionImageToClipboard() {
        if (editor.mode == EditorMode.ACTIONS && editor.currentPage.regions.selectedRegions != null) {
            ActiveRegion region = editor.currentPage.regions.selectedRegions.lastElement();
            int w = region.getWidth();
            int h = region.getHeight();
            BufferedImage image = SketchletGraphicsContext.getInstance().createCompatibleImage(w, h);
            Graphics2D g2 = image.createGraphics();
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, w, h);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.translate(-region.x1, -region.y1);

            region.renderer.drawActive(g2, editor.editorPanel, false, 1.0f);

            g2.dispose();

            BufferedImagePrinter printer = new BufferedImagePrinter(image);
            printer.print();
        }
    }

    public void copyRegionImageToClipboard() {
        if (editor.mode == EditorMode.ACTIONS && editor.currentPage.regions.selectedRegions != null) {
            ActiveRegion region = editor.currentPage.regions.selectedRegions.lastElement();
            int w = region.getWidth();
            int h = region.getHeight();
            BufferedImage image = SketchletGraphicsContext.getInstance().createCompatibleImage(w, h);
            Graphics2D g2 = image.createGraphics();
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, w, h);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.translate(-region.x1, -region.y1);

            region.renderer.drawActive(g2, editor.editorPanel, false, 1.0f);

            g2.dispose();

            BufferedImageClipboardObject tr = new BufferedImageClipboardObject(image, DataFlavor.imageFlavor);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(tr, null);
        }
    }

    public void saveRegionImageToFile() {
        if (editor.mode == EditorMode.ACTIONS && editor.currentPage.regions.selectedRegions != null) {
            File file = SketchletEditor.selectImageFile("");

            if (file != null) {
                ActiveRegion region = editor.currentPage.regions.selectedRegions.lastElement();
                int w = region.getWidth();
                int h = region.getHeight();
                // BufferedImage image = SketchletGraphicsContext.getInstance().createCompatibleImage(w, h);
                BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = image.createGraphics();
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, w, h);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.translate(-region.x1, -region.y1);

                region.renderer.drawActive(g2, editor.editorPanel, false, 1.0f);

                g2.dispose();

                String fileName = file.getName();
                int n = fileName.indexOf(".");
                if (n > 0) {
                    String extension = fileName.substring(n + 1);
                    try {
                        ImageIO.write(image, extension, file);
                        JOptionPane.showMessageDialog(SketchletEditor.editorFrame, "The image has been saved.");
                    } catch (Exception e) {
                        log.error(e);
                    }
                }
            }
        }
    }

    public void fromClipboardCurrentAction(int index) {
        if (editor.editorPanel.currentPage.regions.selectedRegions != null && editor.editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            ActiveRegion action = editor.editorPanel.currentPage.regions.selectedRegions.lastElement();
            editor.saveImageUndo();

            Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable transferable = clip.getContents(null);
            RenderedImage img = null;
            DataFlavor[] dataFlavors;

            dataFlavors =
                    transferable.getTransferDataFlavors();

            if (transferable.isDataFlavorSupported(new DataFlavor("image/x-java-image; class=java.awt.Image", "Image"))) {
                try {
                    img = (RenderedImage) transferable.getTransferData(new DataFlavor("image/x-java-image; class=java.awt.Image", "Image"));
                    action.setDrawImage(index, Workspace.createCompatibleImage(img.getWidth(), img.getHeight()));
                    Graphics2D g2 = action.getDrawImage(index).createGraphics();
                    g2.drawRenderedImage(img, null);

                    action.setDrawImageChanged(index, true);
                    action.saveImage();

                    g2.dispose();

                    TutorialPanel.addLine("cmd", "Paste image in the region");
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }

            } else {
                log.info("Not an image!");
            }
            RefreshTime.update();

            editor.repaint();
        }

    }
}
