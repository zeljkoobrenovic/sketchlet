package net.sf.sketchlet.designer.editor.controllers;

import net.sf.sketchlet.context.SketchletGraphicsContext;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.SketchletEditorMode;
import net.sf.sketchlet.designer.editor.printing.BufferedImagePrinter;
import net.sf.sketchlet.designer.editor.tool.SelectTool;
import net.sf.sketchlet.designer.editor.ui.BufferedImageClipboardObject;
import net.sf.sketchlet.designer.editor.ui.PasteSpecialDialog;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionsFrame;
import net.sf.sketchlet.framework.model.ActiveRegion;
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
public class SketchletEditorClipboardController {
    private static final Logger log = Logger.getLogger(SketchletEditorClipboardController.class);
    private SketchletEditor editor;

    public SketchletEditorClipboardController(SketchletEditor editor) {
        this.editor = editor;
    }

    public void pasteSpecial() {
        editor.setPasting(true);
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
                    a2.setParent(editor.getCurrentPage().getRegions());
                    if (!a2.getRegionGrouping().isEmpty()) {
                        String newGroup = groups.get(a2.getRegionGrouping());
                        if (newGroup == null) {
                            newGroup = "" + System.currentTimeMillis();
                            groups.put(a2.getRegionGrouping(), newGroup);
                        }

                        a2.setRegionGrouping(newGroup);
                    }
                    a2.setX1Value(a2.getX1Value() + (i + 1) * ox);
                    a2.setY1Value(a2.getY1Value() + (i + 1) * oy);
                    a2.setX2Value(a2.getX2Value() + (i + 1) * ox);
                    a2.setY2Value(a2.getY2Value() + (i + 1) * oy);

                    editor.getCurrentPage().getRegions().getRegions().insertElementAt(a2, 0);
                    editor.getCurrentPage().getRegions().getMouseHelper().setSelectedRegions(new Vector<ActiveRegion>());
                    editor.getCurrentPage().getRegions().getMouseHelper().addToSelection(a2);
                }

                SketchletEditor.getInstance().setInCtrlMode(false);
                SketchletEditor.getInstance().setInShiftMode(false);
            }
            if (lastAction != null) {
                ActiveRegionsFrame.reload(lastAction);
            }
        } else {
        }
        editor.setPasting(false);
        RefreshTime.update();
    }

    public void fromClipboard() {
        int x = (int) (editor.scrollPane.getViewport().getViewPosition().getX() / editor.getScale());
        int y = (int) (editor.scrollPane.getViewport().getViewPosition().getY() / editor.getScale());
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

                if (editor.getCurrentPage().getImages()[editor.getLayer()] == null) {
                    editor.updateImage(editor.getLayer(), editor.getSketchletImagesHandler().createEmptyImage());
                }

                int w = Math.max(editor.getCurrentPage().getImages()[editor.getLayer()].getWidth(), img.getWidth());
                int h = Math.max(editor.getCurrentPage().getImages()[editor.getLayer()].getHeight(), img.getHeight());


                BufferedImage newImage = Workspace.createCompatibleImage(img.getWidth(), img.getHeight());

                if (bClear || w > editor.getCurrentPage().getImages()[editor.getLayer()].getWidth() || h > editor.getCurrentPage().getImages()[editor.getLayer()].getHeight()) {
                    BufferedImage tempImage = Workspace.createCompatibleImage(w, h);
                    if (!bClear) {
                        Graphics2D tempg2 = tempImage.createGraphics();
                        tempg2.drawImage(editor.getCurrentPage().getImages()[editor.getLayer()], 0, 0, null);
                        tempg2.dispose();
                    }

                    editor.updateImage(editor.getLayer(), tempImage);
                    tempImage.flush();
                    tempImage = null;
                }
                Graphics2D g2a = newImage.createGraphics();
                g2a.drawRenderedImage(img, null);
                g2a.dispose();
                editor.setTool(editor.getSelectTool(), null);
                editor.getSelectTool().setClip(newImage, x, y);
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
        ActiveRegion region = new ActiveRegion(editor.getCurrentPage().getRegions());

        region.setDrawnImageChanged(0, true);

        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = clip.getContents(null);
        RenderedImage img;

        if (transferable.isDataFlavorSupported(new DataFlavor("image/x-java-image; class=java.awt.Image", "Image"))) {
            try {
                img = (RenderedImage) transferable.getTransferData(new DataFlavor("image/x-java-image; class=java.awt.Image", "Image"));

                region.setDrawnImage(0, Workspace.createCompatibleImage(img.getWidth(), img.getHeight(), region.getDrawnImage(0)));
                region.setX1Value(x);
                region.setY1Value(y);
                region.setX2Value(x + img.getWidth());
                region.setY2Value(y + img.getHeight());

                Graphics2D g2a = region.getDrawnImage(0).createGraphics();
                g2a.drawRenderedImage(img, null);
                g2a.dispose();

                RefreshTime.update();
                editor.repaint();
                editor.getCurrentPage().getRegions().getRegions().insertElementAt(region, 0);
                editor.getCurrentPage().getRegions().getMouseHelper().setSelectedRegions(new Vector<ActiveRegion>());
                editor.getCurrentPage().getRegions().getMouseHelper().addToSelection(region);
                editor.setEditorMode(SketchletEditorMode.EDITING_REGIONS);
                ActiveRegionsFrame.reload(region);
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
            a2.setParent(editor.getCurrentPage().getRegions());
            a2.setRegionGrouping("");
            int w = a2.getX2Value() - a2.getX1Value();
            int h = a2.getY2Value() - a2.getY1Value();
            a2.setX1Value(x);
            a2.setY1Value(y);
            a2.setX2Value(a2.getX1Value() + w);
            a2.setY2Value(a2.getY1Value() + h);
            editor.getCurrentPage().getRegions().getRegions().insertElementAt(a2, 0);
        }

        RefreshTime.update();
        editor.repaint();
    }

    public void copy() {
        if (editor.skipKey) {
            editor.skipKey = false;
            return;
        }

        if (editor.getMode() == SketchletEditorMode.EDITING_REGIONS) {
            copySelectedAction();
        } else {
            if (editor.getTool() != null && editor.getTool() instanceof SelectTool && ((SelectTool) editor.getTool()).getX1() != ((SelectTool) editor.getTool()).getX2()) {
                ((SelectTool) editor.getTool()).copy();
            } else if (editor.getCurrentPage().getImages() != null) {
                BufferedImageClipboardObject tr = new BufferedImageClipboardObject(editor.getCurrentPage().getImages()[editor.getLayer()], DataFlavor.imageFlavor);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(tr, null);
            }
        }

        editor.enableControls();
    }

    public void copySelectedAction() {
        editor.copiedActions = new Vector<ActiveRegion>();
        if (editor.getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null) {
            for (ActiveRegion a : editor.getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                editor.copiedActions.add(new ActiveRegion(a, true));
            }
        }
        editor.getSketchToolbar().pasteSpecial.setEnabled(true);
        SketchletEditor.getInstance().getEditorClipboardController().copyRegionImageToClipboard();
    }

    public void printRegionImageToClipboard() {
        if (editor.getMode() == SketchletEditorMode.EDITING_REGIONS && editor.getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null) {
            ActiveRegion region = editor.getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
            int w = region.getWidthValue();
            int h = region.getHeightValue();
            BufferedImage image = SketchletGraphicsContext.getInstance().createCompatibleImage(w, h);
            Graphics2D g2 = image.createGraphics();
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, w, h);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.translate(-region.getX1Value(), -region.getY1Value());

            region.getRenderer().drawActive(g2, editor.getInstance(), false, 1.0f);

            g2.dispose();

            BufferedImagePrinter printer = new BufferedImagePrinter(image);
            printer.print();
        }
    }

    public void copyRegionImageToClipboard() {
        if (editor.getMode() == SketchletEditorMode.EDITING_REGIONS && editor.getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null) {
            ActiveRegion region = editor.getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
            int w = region.getWidthValue();
            int h = region.getHeightValue();
            BufferedImage image = SketchletGraphicsContext.getInstance().createCompatibleImage(w, h);
            Graphics2D g2 = image.createGraphics();
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, w, h);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.translate(-region.getX1Value(), -region.getY1Value());

            region.getRenderer().drawActive(g2, editor.getInstance(), false, 1.0f);

            g2.dispose();

            BufferedImageClipboardObject tr = new BufferedImageClipboardObject(image, DataFlavor.imageFlavor);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(tr, null);
        }
    }

    public void saveRegionImageToFile() {
        if (editor.getMode() == SketchletEditorMode.EDITING_REGIONS && editor.getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null) {
            File file = SketchletEditor.selectImageFile("");

            if (file != null) {
                ActiveRegion region = editor.getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                int w = region.getWidthValue();
                int h = region.getHeightValue();
                // BufferedImage image = SketchletGraphicsContext.getInstance().createCompatibleImage(w, h);
                BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = image.createGraphics();
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, w, h);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.translate(-region.getX1Value(), -region.getY1Value());

                region.getRenderer().drawActive(g2, editor.getInstance(), false, 1.0f);

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
        if (editor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && editor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            ActiveRegion action = editor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
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
                    action.setDrawnImage(index, Workspace.createCompatibleImage(img.getWidth(), img.getHeight()));
                    Graphics2D g2 = action.getDrawnImage(index).createGraphics();
                    g2.drawRenderedImage(img, null);

                    action.setDrawnImageChanged(index, true);
                    action.saveImage();

                    g2.dispose();
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
