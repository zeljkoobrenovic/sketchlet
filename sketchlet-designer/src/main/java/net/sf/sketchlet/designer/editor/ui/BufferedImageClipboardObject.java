/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.sketchlet.designer.editor.ui;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author zobrenovic
 */
public class BufferedImageClipboardObject implements Transferable, ClipboardOwner {

    private BufferedImage data;
    private DataFlavor flavor;

    public BufferedImageClipboardObject(BufferedImage data, DataFlavor flavor) {
        this.data = data;
        this.flavor = flavor;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{flavor};
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return this.flavor.equals(flavor);
    }

    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        if (this.flavor.equals(flavor)) {
            return data;
        }
        return null;
    }
}

