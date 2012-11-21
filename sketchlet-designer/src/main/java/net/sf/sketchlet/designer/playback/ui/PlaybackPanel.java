/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.playback.ui;

import net.sf.sketchlet.common.base64.Base64Coder;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.communicator.server.Variable;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.context.SketchletPainter;
import net.sf.sketchlet.context.VariableUpdateListener;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.SketchletEditorMode;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.designer.playback.displays.ScreenMapping;
import net.sf.sketchlet.designer.tools.imagecache.ImageCache;
import net.sf.sketchlet.designer.editor.ui.desktop.SystemVariablesDialog;
import net.sf.sketchlet.model.evaluator.Evaluator;
import net.sf.sketchlet.model.ActiveRegion;
import net.sf.sketchlet.model.MouseProcessor;
import net.sf.sketchlet.model.Page;
import net.sf.sketchlet.model.Pages;
import net.sf.sketchlet.model.programming.macros.Commands;
import net.sf.sketchlet.plugin.WidgetPlugin;
import net.sf.sketchlet.renderer.page.PageRenderer;
import net.sf.sketchlet.renderer.page.PanelRenderer;
import net.sf.sketchlet.renderer.page.VariablesRelationsRenderer;
import net.sf.sketchlet.util.Colors;
import net.sf.sketchlet.util.RefreshTime;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

/**
 * @author cuypers
 */
public class PlaybackPanel extends JPanel implements VariableUpdateListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, PanelRenderer, SketchletPainter {
    private static final Logger log = Logger.getLogger(PlaybackPanel.class);

    public static Pages pages;
    public static Page currentPage;
    public static ActiveRegion selectedRegion = null;
    public static ActiveRegion mouseOverRegion = null;
    PlaybackFrame frame;
    public static Vector<Page> history = new Vector<Page>();
    JCheckBoxMenuItem showAnnotation = new JCheckBoxMenuItem("Show Annotation", Workspace.createImageIcon("resources/pencil_annotate.gif"), false);
    JCheckBoxMenuItem highlightRegions = new JCheckBoxMenuItem("Highlight Active Regions", Workspace.createImageIcon("resources/active_region.png"), false);
    static BufferedImage masterImage;
    static public Page masterPage;
    public static double scale = 1.0;
    public double perspective_scale = 1.0;
    public double translate_zoom_x = 0.0;
    public double translate_zoom_y = 0.0;
    AffineTransform affine;
    Image drawnImage = null;
    ScreenMapping display;
    DisplayExportThread displayThread;
    public static int mouseScreenX = 0;
    public static int mouseScreenY = 0;
    PageRenderer renderer = new PageRenderer(this);

    public PlaybackPanel(ScreenMapping display, Pages pages, PlaybackFrame frame) {
        this.display = display;
        this.pages = pages;
        this.frame = frame;
        this.scale = SketchletEditor.getInstance().getScale();
        WidgetPlugin.setActiveWidget(null);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);
        this.setFocusable(true);
        showAnnotation.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                repaint();
            }
        });
        highlightRegions.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                repaint();
            }
        });

        this.addKeyListener(this);

        if (display != null) {
            displayThread = new DisplayExportThread();
        }
    }

    boolean inCtrlMode = false;
    boolean inAltMode = false;
    boolean inShiftMode = false;
    boolean bDisposed = false;

    public boolean isActive() {
        return !bDisposed;
    }

    public void repaint() {
        if (RefreshTime.shouldRefresh()) {
            bUpdated = true;
        }
    }

    public boolean bUpdated = false;
    public static int repaintCounter = 0;

    public void repaintIfNeeded() {
        if (bUpdated) {
            bUpdated = false;
            super.repaint();
            repaintCounter++;
        }
    }

    public void dispose() {
        bDisposed = true;
        if (currentPage != null && currentPage.getImages() != null) {
            for (int i = 0; i < currentPage.getImages().length; i++) {
                BufferedImage img = currentPage.getImages()[i];
                if (img != null) {
                    img.flush();
                    currentPage.getImages()[i] = null;
                }
            }
        }

        display = null;
        pages = null;
        currentPage = null;
        selectedRegion = null;
        frame = null;
        history.removeAllElements();
        showAnnotation = null;
        highlightRegions = null;
        if (masterImage != null) {
            masterImage.flush();
            masterImage = null;
        }
        affine = null;
        if (drawnImage != null) {
            drawnImage.flush();
            drawnImage = null;
        }
        if (renderer != null) {
            renderer.dispose();
            renderer = null;
        }
    }

    int currentKey = -1;

    public void keyPressed(KeyEvent e) {
        if (currentKey == e.getKeyCode()) {
            return; // we process just one press
        }
        currentKey = e.getKeyCode();
        int modifiers = e.getModifiers();
        inShiftMode = (modifiers & KeyEvent.SHIFT_MASK) != 0;
        inCtrlMode = (modifiers & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0;
        inAltMode = (modifiers & KeyEvent.ALT_MASK) != 0;
        SystemVariablesDialog.processKeyboardEvent(e, e.getKeyText(e.getKeyCode()), "pressed");
        boolean keyProcessed = false;
        if (currentPage != null) {
            if (selectedRegion != null) {
                keyProcessed = selectedRegion.keyboardProcessor.processKey(e, "pressed");
            }
            if (!keyProcessed) {
                keyProcessed = currentPage.getKeyboardProcessor().processKey(e, "pressed");
                if (!keyProcessed && masterPage != null) {
                    keyProcessed = masterPage.getKeyboardProcessor().processKey(e, "pressed");
                }
            }
        }
        if (!keyProcessed) {
            if (currentPage != null) {
                keyProcessed = currentPage.getKeyboardProcessor().processKey(e, "");
                if (!keyProcessed && masterPage != null) {
                    keyProcessed = masterPage.getKeyboardProcessor().processKey(e, "");
                }
            }
        }
        if (!keyProcessed) {
            int index;
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (WidgetPlugin.getActiveWidget() == null) {
                        index = pages.getPages().indexOf(currentPage);
                        if (index > 0) {
                            Commands.execute(PlaybackPanel.currentPage, "Go to page", "previous", "", currentPage.getActiveTimers(), currentPage.getActiveMacros(), "", "", frame);
                        }
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (WidgetPlugin.getActiveWidget() == null) {
                        index = pages.getPages().indexOf(currentPage);
                        if (index < pages.getPages().size() - 1) {
                            Commands.execute(PlaybackPanel.currentPage, "Go to page", "next", "", currentPage.getActiveTimers(), currentPage.getActiveMacros(), "", "", frame);
                        }
                    }
                    break;
                case KeyEvent.VK_PLUS:
                case KeyEvent.VK_EQUALS:
                case 107:
                    if ((e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
                        scale += 0.1;
                        repaint();
                    }
                    break;
                case KeyEvent.VK_MINUS:
                case 109:
                    if ((e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
                        scale -= 0.1;
                        repaint();
                    }
                    break;
                case KeyEvent.VK_0:
                    if ((e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
                        scale = 1.0;
                        repaint();
                    }
                    break;
                case KeyEvent.VK_ESCAPE:
                    PlaybackFrame.closeWindow();
                    break;
            }
        }

        if (WidgetPlugin.getActiveWidget() != null) {
            WidgetPlugin.getActiveWidget().keyPressed(e);
        }

        e.consume();
        RefreshTime.update();
    }

    public void keyTyped(KeyEvent e) {
        e.consume();
        RefreshTime.update();
    }

    public void keyReleased(KeyEvent e) {
        currentKey = -1;
        int modifiers = e.getModifiers();
        inShiftMode = (modifiers & KeyEvent.SHIFT_MASK) != 0;
        inCtrlMode = (e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0;
        inAltMode = (modifiers & KeyEvent.ALT_MASK) != 0;

        boolean keyProcessed = false;
        if (currentPage != null) {
            if (selectedRegion != null) {
                keyProcessed = selectedRegion.keyboardProcessor.processKey(e, "pressed");
            }
            if (!keyProcessed) {
                keyProcessed = currentPage.getKeyboardProcessor().processKey(e, "released");
                if (!keyProcessed && masterPage != null) {
                    keyProcessed = masterPage.getKeyboardProcessor().processKey(e, "released");
                }
            }
        }
        SystemVariablesDialog.processKeyboardEvent(e, e.getKeyText(e.getKeyCode()), "released");

        if (WidgetPlugin.getActiveWidget() != null) {
            WidgetPlugin.getActiveWidget().keyReleased(e);
        }
        e.consume();
        RefreshTime.update();
    }

    public static void showSketch(Page page) {
        if (currentPage != null) {
            currentPage.deactivate(true);
            history.add(currentPage);
        }
        if (masterPage != null) {
            masterPage.deactivate(true);
        }
        currentPage = page;
        // initImage();
        masterPage = pages.getSketch("Master");
        if (masterPage != null) {
            initMasterImage();
            masterPage.activate(true);
        }
        currentPage.activate(true);
    }

    public void goBack() {
        if (history.size() > 0) {
            Page page = history.lastElement();
            history.removeElementAt(history.lastIndexOf(page));
            if (frame != null) {
                frame.enableControls();
            }
            if (this.currentPage != null) {
                this.currentPage.deactivate(true);
            }
            if (this.masterPage != null) {
                this.masterPage.deactivate(true);
            }
            this.currentPage = page;
            masterPage = this.pages.getSketch("Master");
            if (this.masterPage != null) {
                initMasterImage();
                this.masterPage.activate(true);
            }
            //initImage();
            this.currentPage.activate(true);
            if (frame != null) {
                this.frame.setTitle(currentPage.getTitle());
            }
            repaint();

            if (frame != null) {
                PlaybackFrame.refreshAllFrames(currentPage);
            }
        }
    }

    public Dimension getPreferredSize2() {
        double _scale = scale; // > 1.0 ? scale : 1.0;
        int w = 0;
        int h = 0;
        if (currentPage != null && this.currentPage.getImages() != null && this.currentPage.getImages()[0] != null) {
            w = currentPage.getImages()[0].getWidth();
            h = currentPage.getImages()[0].getHeight();
        } else if (currentPage != null && currentPage.getPageWidth() > 0 && currentPage.getPageHeight() > 0) {
            w = currentPage.getPageWidth();
            h = currentPage.getPageHeight();
        } else {
            w = (int) InteractionSpace.getSketchWidth();
            h = (int) InteractionSpace.getSketchHeight();
        }

        return new Dimension((int) (w * _scale), (int) (h * _scale));
    }

    public void drawSketchClip(Graphics2D g2) {
        int imageOffset[] = currentPage.getBackgroundOffset(true);
        g2.scale(getScale(), getScale());
        g2.translate(translate_zoom_x, translate_zoom_y);

        int bw = getWidth();
        int bh = getHeight();
        Color bgColor = Colors.getColor(currentPage.getPropertyValue("background color"));
        if (bgColor == null) {
            bgColor = Workspace.getSketchBackground();
        }
        g2.setColor(bgColor);
        g2.fillRect(0, 0, bw, bh);

        if (masterPage != null && masterPage != currentPage) {
            if (masterImage == null) {
                initMasterImage();
            }
            g2.drawImage(masterImage, imageOffset[0], imageOffset[1], this);
            for (int i = masterPage.getRegions().getRegions().size() - 1; i >= 0; i--) {
                ActiveRegion a = masterPage.getRegions().getRegions().elementAt(i);
                a.getRenderer().draw(g2, this, SketchletEditorMode.SKETCHING, true, this.highlightRegions.isSelected(), 1.0f);
                if (highlightRegions.isSelected()) {
                    a.getRenderer().getAuxiliaryDrawingLayer().drawHighlight(g2);
                }
            }
        }

        g2.drawImage(currentPage.getImages()[0], imageOffset[0], imageOffset[1], this);
        if (currentPage != null) {
            for (int i = currentPage.getRegions().getRegions().size() - 1; i >= 0; i--) {
                if (currentPage.getRegions().getRegions().size() > i) {
                    ActiveRegion a = currentPage.getRegions().getRegions().elementAt(i);
                    a.getRenderer().draw(g2, this, SketchletEditorMode.SKETCHING, true, this.highlightRegions.isSelected(), 1.0f);
                    if (highlightRegions.isSelected()) {
                        a.getRenderer().getAuxiliaryDrawingLayer().drawHighlight(g2);
                    }
                }
            }
        }
        if (showAnnotation.isSelected() && currentPage.getImages()[1] != null) {
            g2.drawImage(currentPage.getImages()[1], imageOffset[0], imageOffset[1], this);
        }
        if (SketchletEditor.getInstance().getSketchToolbar().bVisualizeVariables) {
            VariablesRelationsRenderer.drawVariables(currentPage.getRegions(), g2, true);
        }
    }

    public BufferedImage getSketchClip(double dClip[]) {
        BufferedImage img = null;
        if (currentPage == null) {
            return img;
        }
        synchronized (currentPage) {
            while (bImageInitializing || bMasterImageInitializing) {
                try {
                    Thread.sleep(10);
                } catch (Throwable e) {
                }
            }

            int w = (int) (dClip[2] * getScale());
            int h = (int) (dClip[3] * getScale());

            if (w == 0) {
                w = getWidth();
            }
            if (h == 0) {
                h = getHeight();
            }
            w = (int) (w * getScale());
            h = (int) (h * getScale());

            img = Workspace.createCompatibleImage(w, h);

            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(-dClip[0], -dClip[1]);
            drawSketchClip(g2);
            g2.dispose();
        }

        return img;
    }

    boolean bInPaint = false;

    public void paintGraphics(Graphics2D g) {
        paint(g);
    }

    public boolean isPainting() {
        return bInPaint;
    }

    public int getPaintWidth() {
        return getWidth();
    }

    public int getPaintHeigth() {
        return getHeight();
    }

    public Container getContainer() {
        return this;
    }

    public void preparePerspective() {
        translate_zoom_x = 0;
        translate_zoom_y = 0;

        if (currentPage.getPropertyValue("zoom").isEmpty()) {
            perspective_scale = scale;
            return;
        }

        currentPage.calculateHorizonPoint();

        double x = currentPage.getZoomCenterX();
        double y = currentPage.getZoomCenterY();

        this.perspective_scale = currentPage.getZoom() * scale;

        translate_zoom_x += -(perspective_scale * x - x) / perspective_scale;
        translate_zoom_y += -(perspective_scale * y - y) / perspective_scale;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (bInPaint) {
            return;
        }
        bInPaint = true;
        while (bImageInitializing || bMasterImageInitializing) {
            try {
                Thread.sleep(10);
            } catch (Throwable e) {
            }
        }
        if (currentPage == null) {
            return;
        }
        if (currentPage.getImages() == null || currentPage.getImages()[0] == null) {
            //initImage();
        }
        affine = null;
        preparePerspective();
        double t_x = 0;
        double t_y = 0;
        double s_x = getScale();
        double s_y = getScale();
        int w, h;
        if (currentPage.getImages() == null || currentPage.getImages()[0] == null) {
            w = (int) InteractionSpace.getSketchWidth();
            h = (int) InteractionSpace.getSketchHeight();
        } else {
            w = currentPage.getImages()[0].getWidth();
            h = currentPage.getImages()[0].getHeight();
        }
        double[] dClip = null;
        if (display != null) {
            dClip = display.getClipRect(null);
            if (dClip == null || (dClip[2] == 0 && dClip[3] == 0)) {
                dClip = new double[]{0, 0, w, h};
            }
            t_x = -dClip[0];
            t_y = -dClip[1];
            if (dClip[2] > 0 && dClip[3] > 0) {
                w = (int) dClip[2];
                h = (int) dClip[3];

                double fitFactor = 1.0;
                if (display.fitToScreen.isSelected()) {
                    double fitFactor1 = getWidth() / dClip[2];
                    double fitFactor2 = getHeight() / dClip[3];
                    fitFactor = Math.min(fitFactor1, fitFactor2);
                    s_x *= fitFactor;
                    s_y *= fitFactor;
                }
                double _tx = (int) ((getWidth() - w * scale * fitFactor) / 2) / (scale * fitFactor);
                double _ty = (int) ((getHeight() - h * scale * fitFactor) / 2) / (scale * fitFactor);
                if (_tx > 0) {
                    t_x += _tx;
                }
                if (_ty > 0) {
                    t_y += _ty;
                }

                double x = currentPage.getZoomCenterX() - dClip[0];
                double y = currentPage.getZoomCenterY() - dClip[1];

                this.perspective_scale = currentPage.getZoom() * scale;

                translate_zoom_x = -(s_x * x - x) / s_x;
                translate_zoom_y = -(s_y * y - y) / s_y;
            }

        }
        Graphics2D g2 = null;
        BufferedImage buffer = null;
        if (display == null || !display.shouldProcessImage()) {
            g2 = (Graphics2D) g;
        } else {
            buffer = Workspace.createCompatibleImage(w, h);
            g2 = buffer.createGraphics();
        }
        if (affine == null) {
            affine = new AffineTransform();
            affine.scale(s_x, s_y);
            int offset[] = currentPage.getRegionsOffset(true);
            affine.translate(offset[0] + t_x + translate_zoom_x, offset[1] + t_y + translate_zoom_y);
            g2.scale(s_x, s_y);
            g2.translate(t_x + translate_zoom_x, t_y + translate_zoom_y);
        }
        Color bgColor = Colors.getColor(currentPage.getPropertyValue("background color"));
        if (bgColor == null) {
            bgColor = Workspace.getSketchBackground();
        }
        // SketchletEditor.editorPanel.draw(g2, true, currentSketch, this.images, false);

        g2.setColor(bgColor);
        g2.fillRect(-2000, -2000, Math.max(w, 4000), Math.max(h, 4000));
        if (dClip != null && display != null && !display.shouldProcessImage()) {
            g2.setClip((int) dClip[0], (int) dClip[1], w, h);
        }
        renderer.draw(g2, true, false, this.highlightRegions.isSelected());

        if (buffer == null) {
            bInPaint = false;
            if (g2 != null) {
                g2.dispose();
            }
            return;
        } else {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (display != null) {
                buffer = this.display.filter(buffer);
                buffer = display.doPerspective(buffer, w, h);

                if (display.inPerspective) {
                    g.drawImage(buffer, (int) (display.p_x), (int) (display.p_y), this);
                } else {
                    g.drawImage(buffer, 0, 0, this);
                }
            }
        }
        if (g2 != null) {
            g2.dispose();
        }

        bInPaint = false;
    }

    public synchronized BufferedImage paintImage(int x, int y, int w, int h) {
        while (bImageInitializing || bMasterImageInitializing) {
            try {
                Thread.sleep(10);
            } catch (Throwable e) {
            }
        }
        BufferedImage newImage = Workspace.createCompatibleImage(2000, 2000);
        Graphics2D g2 = newImage.createGraphics();

        paintComponent(g2);

        g2.dispose();

        w = Math.min(w, newImage.getWidth());
        h = Math.min(h, newImage.getHeight());

        return newImage.getSubimage(x, y, w, h);
    }

    public void exportDisplay() {
        if (display != null && display.exportDisplay.isSelected()) {
            if (display.exportStrBase64VariableCombo.trim().length() > 0) {
                try {
                    String strVar = display.exportStrBase64VariableCombo;
                    int x = 0;
                    int y = 0;
                    int w = getWidth();
                    int h = getHeight();
                    if (w > 0 && h > 0) {
                        BufferedImage image = paintImage(x, y, w, h);
                        if (image != null) {
                            String strValue = new String(Base64Coder.encode(Base64Coder.getCompressedImageBytes(image, w, h)));
                            DataServer.getInstance().updateVariable(strVar, strValue);
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            if (!display.exportStrFileVariableCombo.isEmpty()) {
                String strVar = display.exportStrFileVariableCombo;
                String strFile = display.exportFileField.getText();
                try {
                    int x = 0;
                    int y = 0;
                    int w = getWidth();
                    int h = getHeight();
                    if (w > 0 && h > 0) {
                        BufferedImage image = paintImage(x, y, w, h);

                        if (image != null && strFile == null || strFile.equals("")) {
                            File file = File.createTempFile("capture_image_temp", ".png");
                            ImageIO.write(image, "PNG", file);
                            DataServer.getInstance().updateVariable(strVar, file.getAbsolutePath());
                            file.deleteOnExit();
                        } else if (image != null) {
                            ImageIO.write(image, "PNG", new File(strFile));
                            DataServer.getInstance().updateVariable(strVar, strFile);
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean waitForImage(Image image, Component c) {
        MediaTracker tracker = new MediaTracker(c);
        tracker.addImage(image, 0);
        try {
            tracker.waitForAll();
        } catch (InterruptedException ie) {
        }
        return (!tracker.isErrorAny());
    }

    static boolean bImageInitializing = false;
    static boolean bMasterImageInitializing = false;

    public static synchronized void initImage() {
        bImageInitializing = true;
        if (currentPage == null) {
            bImageInitializing = false;
            return;
        }
        try {
            int w = (int) InteractionSpace.getSketchWidth();
            int h = (int) InteractionSpace.getSketchHeight();
            for (int i = 0; i < currentPage.getImages().length; i++) {
                boolean bCreateEmpty = false;
                File file = currentPage.getLayerImageFile(i);
                if (file.exists()) {
                    try {
                        currentPage.getImages()[i] = ImageCache.read(file, currentPage.getImages()[i]);
                    } catch (Exception e) {
                        bCreateEmpty = true;
                    }
                } else {
                    bCreateEmpty = true;
                }

                if (bCreateEmpty) {
                    currentPage.getImages()[i] = null; // new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        bImageInitializing = false;
    }

    private static synchronized void initMasterImage() {
        bMasterImageInitializing = true;
        if (masterImage != null || masterPage == null || masterPage == currentPage) {
            bMasterImageInitializing = false;
            return;
        }

        try {
            int w = Toolkit.getDefaultToolkit().getScreenSize().width;
            int h = Toolkit.getDefaultToolkit().getScreenSize().height;
            masterImage = Workspace.createCompatibleImage(w, h);
            Graphics2D g2 = masterImage.createGraphics();
            g2.setPaint(Workspace.getSketchBackground());
            BufferedImage tempImage = null;
            for (int i = 0; i < Page.NUMBER_OF_LAYERS; i++) {
                File file = masterPage.getLayerImageFile(i);
                if (file.exists()) {
                    tempImage = ImageCache.read(file, tempImage);
                    g2.drawImage(tempImage, 0, 0, null);
                }
            }
            g2.dispose();
        } catch (IOException e) {
            e.printStackTrace();
            int w = Toolkit.getDefaultToolkit().getScreenSize().width;
            int h = Toolkit.getDefaultToolkit().getScreenSize().height;
            masterImage = Workspace.createCompatibleImage(w, h);
        }

        bMasterImageInitializing = false;
    }


    protected void clearImageMemory() {
        if (ImageCache.getImages() != null) {
            return;
        }
        if (currentPage.getImages() != null) {
            for (int i = 0; i < currentPage.getImages().length; i++) {
                BufferedImage img = currentPage.getImages()[i];
                if (img != null) {
                    img.flush();
                    currentPage.getImages()[i] = null;
                }
            }
        }
        if (currentPage != null) {
            currentPage.flush();
        }
    }

    public void processAction(Object source, String strGoTo, String strVariable, String strOperation, String strValue) {
        if (strVariable != null && !strVariable.equals("")) {
            if (strValue.equals("?")) {
                Variable v = DataServer.getInstance().getVariable(strVariable);
                String strDescription = "Enter value: ";
                if (v != null && !v.getDescription().trim().equals("")) {
                    strDescription = "Enter value (" + v.getDescription() + "): ";
                }
                strValue = JOptionPane.showInputDialog(strDescription);
                if (strValue == null) {
                    return;
                }
            }
            if (strOperation.equalsIgnoreCase("append") && strValue != null && !strValue.equals("")) {
                Commands.updateVariableOrProperty(source, strVariable, strValue, Commands.ACTION_VARIABLE_APPEND);
            } else if (strOperation != null && strOperation.equalsIgnoreCase("backspace")) {
                Commands.updateVariableOrProperty(source, strVariable, "1", Commands.ACTION_VARIABLE_CUT_RIGHT);
            } else if (strValue != null && !strValue.equals("") && strOperation != null && strOperation.equalsIgnoreCase("add")) {
                Commands.updateVariableOrProperty(source, strVariable, "1", Commands.ACTION_VARIABLE_INCREMENT);
            } else if (strValue != null && !strValue.equals("")) {
                Commands.updateVariableOrProperty(source, strVariable, "1", Commands.ACTION_VARIABLE_UPDATE, true);
            }
            repaint();
        }
        if (strGoTo != null && !strGoTo.equalsIgnoreCase("")) {
            Page s = this.pages.getSketch(strGoTo);
            if (currentPage != s) {
                if (currentPage != null) {
                    affine = null;
                    currentPage.deactivate(true);
                    clearImageMemory();
                    history.add(currentPage);
                    if (frame != null) {
                        frame.enableControls();
                    }
                }
                if (masterPage != null) {
                    masterPage.deactivate(true);
                }

                if (s != null || strGoTo.equalsIgnoreCase("next") || strGoTo.equalsIgnoreCase("previous")) {
                    if (s == null) {
                        int i = pages.getPages().indexOf(currentPage);
                        if (strGoTo.equalsIgnoreCase("next") && i < pages.getPages().size() - 1) {
                            currentPage = pages.getPages().elementAt(i + 1);
                        } else if (strGoTo.equalsIgnoreCase("previous") && i > 0) {
                            currentPage = pages.getPages().elementAt(i - 1);
                        } else {
                            // this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                            return;
                        }
                    } else {
                        currentPage = s;
                    }
                    masterPage = this.pages.getSketch("Master");
                    if (masterPage != null) {
                        initMasterImage();
                        masterPage.activate(true);
                    }
                    currentPage.activate(true);
                    if (frame != null) {
                        this.frame.setTitle(currentPage.getTitle());
                    }
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    if (frame != null) {
                        PlaybackFrame.refreshAllFrames(currentPage);
                    }
                }
            }
        }
    }

    public Point inversePerspective(Point p) {
        if (display != null && display.inPerspective) {
            int x = (int) p.getX();
            int y = (int) p.getY();
            float points[] = new float[2];
            display.perspectiveFilter.transformInverse(x, y, points);
            return new Point((int) points[0], (int) points[1]);
        }

        return p;
    }

    public double getScale() {
        return perspective_scale;
    }

    public void mouseClicked(MouseEvent e) {
        int x = (int) (e.getPoint().x);
        int y = (int) (e.getPoint().y);

        mouseClicked(x, y, e.getButton(), e.getClickCount());
        RefreshTime.update();
    }

    public void mouseClicked(int x, int y, int button, int clickCount) {

        if (currentPage == null || currentPage.getRegions() == null) {
            return;
        }
        if (affine != null) {
            try {
                Point2D ip = new Point();
                Point p = inversePerspective(new Point(x, y));
                ip = affine.inverseTransform(p, ip);
                x = (int) (ip.getX());
                y = (int) (ip.getY());
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }

        selectedRegion = currentPage.getRegions().selectRegion(x, y, true);

        if (selectedRegion == null && masterPage != null) {
            selectedRegion = masterPage.getRegions().selectRegion(x, y, true);
        }

        MouseProcessor mouseProcessor;
        if (selectedRegion != null) {
            mouseProcessor = selectedRegion.mouseProcessor;
        } else {
            mouseProcessor = currentPage.getMouseProcessor();
        }

        if (mouseProcessor != null) {
            mouseProcessor.processAction(button, frame, new int[]{MouseProcessor.MOUSE_LEFT_BUTTON_CLICK, MouseProcessor.MOUSE_MIDDLE_BUTTON_CLICK, MouseProcessor.MOUSE_RIGHT_BUTTON_CLICK});

            if (clickCount == 2) {
                mouseProcessor.processAction(frame, MouseProcessor.MOUSE_DOUBLE_CLICK);
            }

            if (clickCount == 1) {
                InteractionRecorder.addEvent("Mouse click", selectedRegion != null ? "region " + selectedRegion.getNumber() : "page", "button " + button);
            } else if (clickCount == 2) {
                InteractionRecorder.addEvent("Mouse double click", selectedRegion != null ? "region " + selectedRegion.getNumber() : "page", "button " + button);
            }
        }
    }

    boolean bProcessing = false;

    public void variableUpdated(final String name, final String value) {
        if (name.trim().isEmpty() || currentPage == null) {
            return;
        }

        if (currentPage == null || currentPage.getRegions() == null || currentPage.getVariableUpdateEventMacros().size() == 0) {
            DataServer.getInstance().removeVariablesUpdateListener(this);
            return;
        }

        currentPage.getRegions().changePerformed(name, value, true);
        boolean bProcessEvenetsMaster = false;
        if (currentPage != null && currentPage.getVariableUpdatePageHandler() != null) {
            bProcessEvenetsMaster = currentPage.getVariableUpdatePageHandler().process(name, value);
        }

        if (masterPage != null && currentPage != masterPage) {
            masterPage.getRegions().changePerformed(name, value, true);
            if (masterPage.getVariableUpdatePageHandler() != null && !bProcessEvenetsMaster) {
                masterPage.getVariableUpdatePageHandler().process(name, value);
            }
        }
        repaint();
    }

    public void processPositionAndSize() {
        if (frame == null || display == null) {
            return;
        }
        try {
            String strX = Evaluator.processText((String) display.pageClip[0][1], "", "");
            String strY = Evaluator.processText((String) display.pageClip[1][1], "", "");
            String strWidth = Evaluator.processText((String) display.pageClip[2][1], "", "");
            String strHeight = Evaluator.processText((String) display.pageClip[3][1], "", "");

            int x = frame.getX();
            int y = frame.getY();
            int w = frame.getSize().width;
            int h = frame.getSize().height;

            if (strX.length() > 0) {
                x = Integer.parseInt(strX);
            }
            if (strY.length() > 0) {
                y = Integer.parseInt(strY);
            }
            if (strWidth.length() > 0) {
                w = Integer.parseInt(strWidth);
            }
            if (strHeight.length() > 0) {
                h = Integer.parseInt(strHeight);
            }

            if (x != frame.getX() || y != frame.getY()) {
                frame.setLocation(x, y);
            }

            if (w != frame.getWidth() || h != frame.getHeight()) {
                frame.setSize(w, h);
            }
        } catch (Throwable e) {
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        currentPage.getMouseProcessor().processAction(e, frame, MouseProcessor.MOUSE_ENTRY);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        currentPage.getMouseProcessor().processAction(e, frame, MouseProcessor.MOUSE_EXIT);
    }

    int prevX, prevY;
    double speeds[] = new double[5];
    int currentDistanceIndex = 0;
    double speed;
    long prevTimestamp;

    public void mousePressed(MouseEvent e) {
        this.requestFocus();

        int x = (int) (e.getPoint().x);
        int y = (int) (e.getPoint().y);
        if (affine != null) {
            try {
                Point2D ip = new Point();
                Point p = inversePerspective(e.getPoint());
                ip = affine.inverseTransform(p, ip);
                x = (int) (ip.getX());
                y = (int) (ip.getY());
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        SystemVariablesDialog.processMouseEvent(x, y, "mouse pressed");

        prevX = x;
        prevY = y;
        prevTimestamp = e.getWhen();

        for (int i = 0; i < speeds.length; i++) {
            speeds[i] = 0.0;
        }
        selectedRegion = currentPage.getRegions().selectRegion(x, y, true);

        if (selectedRegion == null && masterPage != null) {
            selectedRegion = masterPage.getRegions().selectRegion(x, y, true);
        }

        MouseProcessor mouseProcessor;
        if (selectedRegion != null) {
            selectedRegion.getMouseHandler().mousePressed(x, y, e.getModifiers(), e.getWhen(), e, frame, true);
            repaint();
            mouseProcessor = selectedRegion.mouseProcessor;
        } else {
            WidgetPlugin.setActiveWidget(null);
            mouseProcessor = currentPage.getMouseProcessor();
        }

        if (mouseProcessor != null) {
            mouseProcessor.processAction(e, frame, new int[]{MouseProcessor.MOUSE_LEFT_BUTTON_PRESS, MouseProcessor.MOUSE_MIDDLE_BUTTON_PRESS, MouseProcessor.MOUSE_RIGHT_BUTTON_PRESS});
            InteractionRecorder.addEvent("Mouse press", selectedRegion != null ? "region " + selectedRegion.getNumber() : "page", "button " + e.getButton());
        }
        SketchletContext.getInstance().repaint();
    }

    public void mouseReleased(MouseEvent e) {
        int x = (int) (e.getPoint().x);
        int y = (int) (e.getPoint().y);
        if (affine != null) {
            try {
                Point2D ip = new Point();
                Point p = inversePerspective(e.getPoint());
                ip = affine.inverseTransform(p, ip);
                x = (int) (ip.getX());
                y = (int) (ip.getY());
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        SystemVariablesDialog.processMouseEvent(x, y, "mouse released");
        MouseProcessor mouseProcessor;
        if (selectedRegion != null) {
            selectedRegion.getMouseHandler().mouseReleased(x, y, e.getModifiers(), e.getWhen(), e, frame, true);
            repaint();
            mouseProcessor = selectedRegion.mouseProcessor;
        } else {
            mouseProcessor = currentPage.getMouseProcessor();
        }

        if (mouseProcessor != null) {
            mouseProcessor.processAction(e, frame, new int[]{MouseProcessor.MOUSE_LEFT_BUTTON_RELEASE, MouseProcessor.MOUSE_MIDDLE_BUTTON_RELEASE, MouseProcessor.MOUSE_RIGHT_BUTTON_RELEASE});
            InteractionRecorder.addEvent("Mouse release", selectedRegion != null ? "region " + selectedRegion.getNumber() : "page", "button " + e.getButton());
        }
        RefreshTime.update();
    }

    public void mouseDragged(MouseEvent e) {
        int x = (int) (e.getPoint().x);
        int y = (int) (e.getPoint().y);
        if (affine != null) {
            try {
                Point2D ip = new Point();
                Point p = inversePerspective(e.getPoint());
                ip = affine.inverseTransform(p, ip);
                x = (int) (ip.getX());
                y = (int) (ip.getY());
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }

        SystemVariablesDialog.processMouseEvent(x, y, "mouse dragged");
        if (this.selectedRegion != null) {
            if ((selectedRegion.movable || selectedRegion.rotatable) && e.getWhen() != prevTimestamp) {
                int dx = x - prevX;
                int dy = y - prevY;
                prevX = x;
                prevY = y;

                double dt = (e.getWhen() - prevTimestamp) / 100.0;

                speed = Math.sqrt(dx * dx + dy * dy) / dt;

                speeds[currentDistanceIndex++] = speed;
                currentDistanceIndex = currentDistanceIndex % speeds.length;

                double _s = 0.0;
                for (int i = 0; i < speeds.length; i++) {
                    _s += speeds[i];
                }

                speed = _s / speeds.length;
                double _speed = selectedRegion.getMotionHandler().processLimits("speed", speed, 0.0, 0.0, true);

                prevTimestamp = e.getWhen();
            }
            selectedRegion.getMouseHandler().mouseDragged(x, y, e.getModifiers(), e.getWhen(), this.getScale(), e, frame, true);
            repaint();
        }
        RefreshTime.update();
    }

    public void mouseMoved(MouseEvent e) {
        if (currentPage == null) {
            return;
        }
        mouseScreenX = e.getXOnScreen();
        mouseScreenY = e.getYOnScreen();
        int x = (int) (e.getPoint().x);
        int y = (int) (e.getPoint().y);
        if (affine != null) {
            try {
                Point2D ip = new Point();
                Point p = inversePerspective(e.getPoint());
                ip = affine.inverseTransform(p, ip);
                x = (int) (ip.getX());
                y = (int) (ip.getY());
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }

        if (inCtrlMode && inAltMode) {
            currentPage.setPerspective_horizont_x2(x);
            currentPage.setPerspective_horizont_y(y);
            repaint();
            return;
        }

        if (inCtrlMode) {
            currentPage.setPerspective_horizont_x1(x);
            currentPage.setPerspective_horizont_y(y);
            repaint();
            return;
        }

        SystemVariablesDialog.processMouseEvent(x, y, "mouse moved");

        e.getPoint().x = x;
        e.getPoint().y = y;

        ActiveRegion region = currentPage.getRegions().selectRegion(x, y, true);

        if (region == null && masterPage != null) {
            region = masterPage.getRegions().selectRegion(x, y, true);
        }

        if (region != null && region.isMouseActive()) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            this.setCursor(Cursor.getDefaultCursor());
        }

        if (region != mouseOverRegion) {
            if (mouseOverRegion != null) {
                mouseOverRegion.mouseProcessor.processAction(e, frame, MouseProcessor.MOUSE_EXIT);
            }
            mouseOverRegion = region;
            if (region != null) {
                region.mouseProcessor.processAction(e, frame, MouseProcessor.MOUSE_ENTRY);
            }
        }

        if (region != null) {
            region.getMouseHandler().mouseMoved(x, y, getScale(), e, frame, true);
        }
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        int x = (int) (e.getPoint().x);
        int y = (int) (e.getPoint().y);

        if (affine != null) {
            try {
                Point2D ip = new Point();
                Point p = inversePerspective(e.getPoint());
                ip = affine.inverseTransform(p, ip);
                x = (int) (ip.getX());
                y = (int) (ip.getY());
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }

        if (inShiftMode) {
            currentPage.setZoomCenterX(x);
            currentPage.setZoomCenterY(y);
            if (e.getWheelRotation() > 0) {
                currentPage.setZoom(currentPage.getZoom() + 0.1);
            } else {
                currentPage.setZoom(currentPage.getZoom() - 0.1);
            }
            repaint();
            return;
        }

        selectedRegion = currentPage.getRegions().selectRegion(x, y, true);

        if (selectedRegion == null) {
            currentPage.getMouseProcessor().processAction(e, frame, MouseProcessor.MOUSE_WHEEL_UP);
            return;
        }
        int notches = e.getWheelRotation();
        if (notches < 0) {
            for (int i = 0; i < Math.abs(notches); i++) {
                selectedRegion.mouseProcessor.processAction(e, frame, MouseProcessor.MOUSE_WHEEL_UP);
            }
        } else {
            for (int i = 0; i < notches; i++) {
                selectedRegion.mouseProcessor.processAction(e, frame, MouseProcessor.MOUSE_WHEEL_DOWN);
            }
        }
    }

    public static RefreshScreenCaptureThread refreshCaptureThread = null;

    class DisplayExportThread implements Runnable {

        Thread t = new Thread(this);

        public DisplayExportThread() {
            t.start();
        }

        public void run() {
            if (display != null && display.exportDisplay.isSelected()) {
                try {
                    String strType = display.exportStrOn;
                    if (strType != null && strType.equalsIgnoreCase("periodically")) {
                        String strTime = Evaluator.processText(display.exportFrequency.getText(), "", "");
                        int nPause = (int) (1000 * Double.parseDouble(strTime));
                        while (PlaybackFrame.playbackFrame != null) {
                            exportDisplay();
                            Thread.sleep(nPause);
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            displayThread = null;
        }
    }

    public Page getSketch() {
        return this.currentPage;
    }

    public Page getMasterPage() {
        return this.masterPage;
    }

    public BufferedImage getImage(int layer) {
        if (this.currentPage == null) {
            return null;
        }

        if (this.currentPage.getImages() == null) {
            this.currentPage.initImages();
        }
        return this.currentPage.getImages()[layer];
    }

    public Component getComponent() {
        return this;
    }

    public void setImage(int layer, BufferedImage img) {
        this.currentPage.getImages()[layer] = img;
    }

    public void setLayerImage(int layer, BufferedImage image) {
    }

    public BufferedImage getLayerImage(int layer) {
        return null;
    }

    public int getImageCount() {
        return currentPage.getImages().length;
    }

    public BufferedImage getMasterImage() {
        return this.masterImage;
    }

    public void setMasterImage(BufferedImage image) {
        this.masterImage = image;
    }

    public int getLayer() {
        return 0;
    }
    // public int getWidth();
    // public int getHeight();

    public SketchletEditorMode getMode() {
        return SketchletEditorMode.UNDEFINED;
    }

    public int getMarginX() {
        return 0;
    }

    public int getMarginY() {
        return 0;
    }

    public double getScaleX() {
        return getScale();
    }

    public double getScaleY() {
        return getScale();
    }

    public void extraDraw(Graphics2D g2) {
    }

    public void parentPaintComponent(Graphics2D g2) {
        super.paintComponent(g2);
    }

    public void enableControls() {
    }
    //public void revalidate();

    public void save() {
    }

    Graphics2D prevGraphics = null;

    public Graphics2D createGraphics() {
        if (prevGraphics != null) {
            prevGraphics.dispose();
        }
        if (currentPage.getImages() != null && currentPage.getImages()[0] != null) {
            prevGraphics = currentPage.getImages()[0].createGraphics();
        }
        return prevGraphics;
    }
    //public void setCursor(Cursor cursor);
}
