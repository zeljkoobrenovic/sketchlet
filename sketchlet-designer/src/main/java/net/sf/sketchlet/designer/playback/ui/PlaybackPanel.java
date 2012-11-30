package net.sf.sketchlet.designer.playback.ui;

import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.context.SketchletPainter;
import net.sf.sketchlet.context.VariableUpdateListener;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.SketchletEditorMode;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.designer.playback.displays.ScreenMapping;
import net.sf.sketchlet.framework.blackboard.Variable;
import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;
import net.sf.sketchlet.framework.controller.InteractionContext;
import net.sf.sketchlet.framework.controller.KeyboardController;
import net.sf.sketchlet.framework.controller.MouseController;
import net.sf.sketchlet.framework.model.Page;
import net.sf.sketchlet.framework.model.Pages;
import net.sf.sketchlet.framework.model.imagecache.ImageCache;
import net.sf.sketchlet.framework.model.programming.macros.Commands;
import net.sf.sketchlet.framework.renderer.page.PageRenderer;
import net.sf.sketchlet.framework.renderer.page.PanelRenderer;
import net.sf.sketchlet.plugin.WidgetPlugin;
import net.sf.sketchlet.util.Colors;
import net.sf.sketchlet.util.RefreshTime;
import org.apache.log4j.Logger;

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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

/**
 * @author cuypers
 */
public class PlaybackPanel extends JPanel implements VariableUpdateListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, PanelRenderer, SketchletPainter {
    private static final Logger log = Logger.getLogger(PlaybackPanel.class);

    private static Pages pages;
    private static Page currentPage;
    private PlaybackFrame frame;
    private static Vector<Page> history = new Vector<Page>();
    private JCheckBoxMenuItem showAnnotation = new JCheckBoxMenuItem("Show Annotation", Workspace.createImageIcon("resources/pencil_annotate.gif"), false);
    private JCheckBoxMenuItem highlightRegions = new JCheckBoxMenuItem("Highlight Active Regions", Workspace.createImageIcon("resources/active_region.png"), false);
    static BufferedImage masterImage;
    private static Page masterPage;
    private static double scale = 1.0;
    private double perspective_scale = 1.0;
    private double translate_zoom_x = 0.0;
    private double translate_zoom_y = 0.0;
    private AffineTransform affine;
    private Image drawnImage = null;
    private ScreenMapping display;
    private DisplayExportThread displayThread;

    private PageRenderer renderer = new PageRenderer(this);

    private KeyboardController keyboardController = new KeyboardController();
    private MouseController mouseController = new MouseController();

    private static InteractionContext context;
    private Graphics2D prevGraphics = null;

    private boolean painting = false;

    private static boolean imageInitializing = false;
    private static boolean masterImageInitializing = false;

    private boolean disposed = false;

    private boolean updated = false;

    private static RefreshScreenCaptureThread refreshCaptureThread = null;


    public PlaybackPanel(ScreenMapping display, Pages pages, PlaybackFrame frame) {
        context = null;
        this.setDisplay(display);
        this.setPages(pages);
        this.frame = frame;
        this.setScale(SketchletEditor.getInstance().getScale());
        WidgetPlugin.setActiveWidget(null);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);
        this.setFocusable(true);
        getShowAnnotation().addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                repaint();
            }
        });
        getHighlightRegions().addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                repaint();
            }
        });

        this.addKeyListener(this);

        if (display != null) {
            setDisplayThread(new DisplayExportThread(this));
        }
    }

    public static RefreshScreenCaptureThread getRefreshCaptureThread() {
        return refreshCaptureThread;
    }

    public static void setRefreshCaptureThread(RefreshScreenCaptureThread refreshCaptureThread) {
        PlaybackPanel.refreshCaptureThread = refreshCaptureThread;
    }

    public static Pages getPages() {
        return pages;
    }

    public static void setPages(Pages pages) {
        PlaybackPanel.pages = pages;
    }

    public static Page getCurrentPage() {
        return currentPage;
    }

    public static void setCurrentPage(Page currentPage) {
        PlaybackPanel.currentPage = currentPage;
    }

    public static Vector<Page> getHistory() {
        return history;
    }

    public static void setHistory(Vector<Page> history) {
        PlaybackPanel.history = history;
    }

    public static void setMasterPage(Page masterPage) {
        PlaybackPanel.masterPage = masterPage;
    }

    public static void setScale(double scale) {
        PlaybackPanel.scale = scale;
    }

    public boolean isActive() {
        return !disposed;
    }

    public void repaint() {
        if (RefreshTime.shouldRefresh()) {
            updated = true;
        }
    }

    public void repaintIfNeeded() {
        if (updated) {
            updated = false;
            super.repaint();
        }
    }

    public void dispose() {
        disposed = true;
        if (getCurrentPage() != null && getCurrentPage().getImages() != null) {
            for (int i = 0; i < getCurrentPage().getImages().length; i++) {
                BufferedImage img = getCurrentPage().getImages()[i];
                if (img != null) {
                    img.flush();
                    getCurrentPage().getImages()[i] = null;
                }
            }
        }

        setDisplay(null);
        setDisplayThread(null);
        setPages(null);
        setCurrentPage(null);
        InteractionContext.setSelectedRegion(null);
        frame = null;
        getHistory().removeAllElements();
        setShowAnnotation(null);
        setHighlightRegions(null);
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

    private InteractionContext getInteractionContext() {
        if (context == null) {
            context = new InteractionContext() {
                @Override
                public void repaint() {
                    PlaybackPanel.this.repaint();
                }
            };
        }
        context.setCurrentPage(getCurrentPage());
        context.setFrame(frame != null ? frame : SketchletContext.getInstance().getEditorFrame());
        context.setMasterPage(getMasterPage());
        context.setPages(getPages());
        context.setScale(getScale());
        context.setSelectedRegion(InteractionContext.getSelectedRegion());
        context.setAffineTransform(affine);
        context.setKeyboardController(keyboardController);
        context.setMouseController(mouseController);
        context.setScreenMapping(display);

        return context;
    }

    public static void showSketch(Page page) {
        if (getCurrentPage() != null) {
            getCurrentPage().deactivate(true);
            getHistory().add(getCurrentPage());
        }
        if (masterPage != null) {
            masterPage.deactivate(true);
        }
        setCurrentPage(page);
        setMasterPage(getPages().getSketch("Master"));
        if (masterPage != null) {
            initMasterImage();
            masterPage.activate(true);
        }
        getCurrentPage().activate(true);
    }

    @Override
    public void paintGraphics(Graphics2D g) {
        paint(g);
    }

    void goBack() {
        if (getHistory().size() > 0) {
            Page page = getHistory().lastElement();
            getHistory().removeElementAt(getHistory().lastIndexOf(page));
            if (frame != null) {
                frame.enableControls();
            }
            if (this.getCurrentPage() != null) {
                this.getCurrentPage().deactivate(true);
            }
            if (this.getMasterPage() != null) {
                this.getMasterPage().deactivate(true);
            }
            this.setCurrentPage(page);
            setMasterPage(this.getPages().getSketch("Master"));
            if (this.getMasterPage() != null) {
                initMasterImage();
                this.getMasterPage().activate(true);
            }

            this.getCurrentPage().activate(true);
            if (frame != null) {
                this.frame.setTitle(getCurrentPage().getTitle());
            }

            repaint();

            if (frame != null) {
                PlaybackFrame.refreshAllFrames(getCurrentPage());
            }
        }
    }

    public Dimension getPreferredSize2() {
        double _scale = getScale();
        int w;
        int h;
        if (getCurrentPage() != null && this.getCurrentPage().getImages() != null && this.getCurrentPage().getImages()[0] != null) {
            w = getCurrentPage().getImages()[0].getWidth();
            h = getCurrentPage().getImages()[0].getHeight();
        } else if (getCurrentPage() != null && getCurrentPage().getPageWidth() > 0 && getCurrentPage().getPageHeight() > 0) {
            w = getCurrentPage().getPageWidth();
            h = getCurrentPage().getPageHeight();
        } else {
            w = (int) InteractionSpace.getSketchWidth();
            h = (int) InteractionSpace.getSketchHeight();
        }

        return new Dimension((int) (w * _scale), (int) (h * _scale));
    }

    @Override
    public boolean isPainting() {
        return painting;
    }

    @Override
    public int getPaintWidth() {
        return getWidth();
    }

    @Override
    public int getPaintHeigth() {
        return getHeight();
    }

    @Override
    public Container getContainer() {
        return this;
    }

    private void preparePerspective() {
        setTranslate_zoom_x(0);
        setTranslate_zoom_y(0);

        if (getCurrentPage().getPropertyValue("zoom").isEmpty()) {
            setPerspective_scale(getScale());
            return;
        }

        getCurrentPage().calculateHorizonPoint();

        double x = getCurrentPage().getZoomCenterX();
        double y = getCurrentPage().getZoomCenterY();

        this.setPerspective_scale(getCurrentPage().getZoom() * getScale());

        setTranslate_zoom_x(getTranslate_zoom_x() + -(getPerspective_scale() * x - x) / getPerspective_scale());
        setTranslate_zoom_y(getTranslate_zoom_y() + -(getPerspective_scale() * y - y) / getPerspective_scale());
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (painting) {
            return;
        }
        painting = true;
        while (imageInitializing || masterImageInitializing) {
            try {
                Thread.sleep(10);
            } catch (Throwable e) {
            }
        }
        if (getCurrentPage() == null) {
            return;
        }
        affine = null;
        preparePerspective();
        double t_x = 0;
        double t_y = 0;
        double s_x = getScale();
        double s_y = getScale();
        int w, h;
        if (getCurrentPage().getImages() == null || getCurrentPage().getImages()[0] == null) {
            w = (int) InteractionSpace.getSketchWidth();
            h = (int) InteractionSpace.getSketchHeight();
        } else {
            w = getCurrentPage().getImages()[0].getWidth();
            h = getCurrentPage().getImages()[0].getHeight();
        }
        double[] dClip = null;
        if (getDisplay() != null) {
            dClip = getDisplay().getClipRect(null);
            if (dClip == null || (dClip[2] == 0 && dClip[3] == 0)) {
                dClip = new double[]{0, 0, w, h};
            }
            t_x = -dClip[0];
            t_y = -dClip[1];
            if (dClip[2] > 0 && dClip[3] > 0) {
                w = (int) dClip[2];
                h = (int) dClip[3];

                double fitFactor = 1.0;
                if (getDisplay().fitToScreen.isSelected()) {
                    double fitFactor1 = getWidth() / dClip[2];
                    double fitFactor2 = getHeight() / dClip[3];
                    fitFactor = Math.min(fitFactor1, fitFactor2);
                    s_x *= fitFactor;
                    s_y *= fitFactor;
                }
                double _tx = (int) ((getWidth() - w * getScale() * fitFactor) / 2) / (getScale() * fitFactor);
                double _ty = (int) ((getHeight() - h * getScale() * fitFactor) / 2) / (getScale() * fitFactor);
                if (_tx > 0) {
                    t_x += _tx;
                }
                if (_ty > 0) {
                    t_y += _ty;
                }

                double x = getCurrentPage().getZoomCenterX() - dClip[0];
                double y = getCurrentPage().getZoomCenterY() - dClip[1];

                this.setPerspective_scale(getCurrentPage().getZoom() * getScale());

                setTranslate_zoom_x(-(s_x * x - x) / s_x);
                setTranslate_zoom_y(-(s_y * y - y) / s_y);
            }

        }
        Graphics2D g2;
        BufferedImage buffer = null;
        if (getDisplay() == null || !getDisplay().shouldProcessImage()) {
            g2 = (Graphics2D) g;
        } else {
            buffer = Workspace.createCompatibleImage(w, h);
            g2 = buffer.createGraphics();
        }
        if (affine == null) {
            affine = new AffineTransform();
            affine.scale(s_x, s_y);
            int offset[] = getCurrentPage().getRegionsOffset(true);
            affine.translate(offset[0] + t_x + getTranslate_zoom_x(), offset[1] + t_y + getTranslate_zoom_y());
            g2.scale(s_x, s_y);
            g2.translate(t_x + getTranslate_zoom_x(), t_y + getTranslate_zoom_y());
        }
        Color bgColor = Colors.getColor(getCurrentPage().getPropertyValue("background color"));
        if (bgColor == null) {
            bgColor = Workspace.getSketchBackground();
        }

        g2.setColor(bgColor);
        g2.fillRect(-2000, -2000, Math.max(w, 4000), Math.max(h, 4000));
        if (dClip != null && getDisplay() != null && !getDisplay().shouldProcessImage()) {
            g2.setClip((int) dClip[0], (int) dClip[1], w, h);
        }
        renderer.draw(g2, true, false, this.getHighlightRegions().isSelected());

        if (buffer == null) {
            painting = false;
            if (g2 != null) {
                g2.dispose();
            }
            return;
        } else {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (getDisplay() != null) {
                buffer = this.getDisplay().filter(buffer);
                buffer = getDisplay().doPerspective(buffer, w, h);

                if (getDisplay().inPerspective) {
                    g.drawImage(buffer, getDisplay().p_x, getDisplay().p_y, this);
                } else {
                    g.drawImage(buffer, 0, 0, this);
                }
            }
        }
        if (g2 != null) {
            g2.dispose();
        }

        painting = false;
    }

    synchronized BufferedImage paintImage(int x, int y, int w, int h) {
        while (imageInitializing || masterImageInitializing) {
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

    private static synchronized void initMasterImage() {
        masterImageInitializing = true;
        if (masterImage != null || masterPage == null || masterPage == getCurrentPage()) {
            masterImageInitializing = false;
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

        masterImageInitializing = false;
    }


    protected void clearImageMemory() {
        if (ImageCache.getImages() != null) {
            return;
        }
        if (getCurrentPage().getImages() != null) {
            for (int i = 0; i < getCurrentPage().getImages().length; i++) {
                BufferedImage img = getCurrentPage().getImages()[i];
                if (img != null) {
                    img.flush();
                    getCurrentPage().getImages()[i] = null;
                }
            }
        }
        if (getCurrentPage() != null) {
            getCurrentPage().flush();
        }
    }

    public void processAction(Object source, String strGoTo, String strVariable, String strOperation, String strValue) {
        if (strVariable != null && !strVariable.equals("")) {
            if (strValue.equals("?")) {
                Variable v = VariablesBlackboard.getInstance().getVariable(strVariable);
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
            Page s = this.getPages().getSketch(strGoTo);
            if (getCurrentPage() != s) {
                if (getCurrentPage() != null) {
                    affine = null;
                    getCurrentPage().deactivate(true);
                    clearImageMemory();
                    getHistory().add(getCurrentPage());
                    if (frame != null) {
                        frame.enableControls();
                    }
                }
                if (masterPage != null) {
                    masterPage.deactivate(true);
                }

                if (s != null || strGoTo.equalsIgnoreCase("next") || strGoTo.equalsIgnoreCase("previous")) {
                    if (s == null) {
                        int i = getPages().getPages().indexOf(getCurrentPage());
                        if (strGoTo.equalsIgnoreCase("next") && i < getPages().getPages().size() - 1) {
                            setCurrentPage(getPages().getPages().elementAt(i + 1));
                        } else if (strGoTo.equalsIgnoreCase("previous") && i > 0) {
                            setCurrentPage(getPages().getPages().elementAt(i - 1));
                        } else {
                            return;
                        }
                    } else {
                        setCurrentPage(s);
                    }
                    setMasterPage(this.getPages().getSketch("Master"));
                    if (masterPage != null) {
                        initMasterImage();
                        masterPage.activate(true);
                    }
                    getCurrentPage().activate(true);
                    if (frame != null) {
                        this.frame.setTitle(getCurrentPage().getTitle());
                    }
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    if (frame != null) {
                        PlaybackFrame.refreshAllFrames(getCurrentPage());
                    }
                }
            }
        }
    }

    public double getScale() {
        return scale;
    }

    public void variableUpdated(final String name, final String value) {
        if (name.trim().isEmpty() || getCurrentPage() == null) {
            return;
        }

        if (getCurrentPage() == null || getCurrentPage().getRegions() == null) {
            VariablesBlackboard.getInstance().removeVariablesUpdateListener(this);
            return;
        }

        getCurrentPage().getRegions().getVariablesHelper().changePerformed(name, value, true);
        boolean processEventsMaster = false;
        if (getCurrentPage() != null && getCurrentPage().getVariableUpdatePageHandler() != null) {
            processEventsMaster = getCurrentPage().getVariableUpdatePageHandler().process(name, value);
        }

        if (masterPage != null && getCurrentPage() != masterPage) {
            masterPage.getRegions().getVariablesHelper().changePerformed(name, value, true);
            if (masterPage.getVariableUpdatePageHandler() != null && !processEventsMaster) {
                masterPage.getVariableUpdatePageHandler().process(name, value);
            }
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keyboardController.keyPressed(getInteractionContext(), e);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        keyboardController.keyTyped(getInteractionContext(), e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keyboardController.keyReleased(getInteractionContext(), e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        mouseController.mouseClicked(getInteractionContext(), e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        mouseController.mouseEntered(getInteractionContext(), e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mouseController.mouseExited(getInteractionContext(), e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.requestFocus();
        mouseController.mousePressed(getInteractionContext(), e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseController.mouseReleased(getInteractionContext(), e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseController.mouseDragged(getInteractionContext(), e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseController.mouseMoved(getInteractionContext(), e);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        mouseController.mouseWheelMoved(getInteractionContext(), e);
    }

    public Page getPage() {
        return this.getCurrentPage();
    }

    public Page getMasterPage() {
        return this.masterPage;
    }

    public BufferedImage getImage(int layer) {
        if (this.getCurrentPage() == null) {
            return null;
        }

        if (this.getCurrentPage().getImages() == null) {
            this.getCurrentPage().initImages();
        }
        return this.getCurrentPage().getImages()[layer];
    }

    public Component getComponent() {
        return this;
    }

    public void setImage(int layer, BufferedImage img) {
        this.getCurrentPage().getImages()[layer] = img;
    }

    public int getImageCount() {
        return getCurrentPage().getImages().length;
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

    public void save() {
    }

    public Graphics2D createGraphics() {
        if (prevGraphics != null) {
            prevGraphics.dispose();
        }
        if (getCurrentPage().getImages() != null && getCurrentPage().getImages()[0] != null) {
            prevGraphics = getCurrentPage().getImages()[0].createGraphics();
        }
        return prevGraphics;
    }

    public ScreenMapping getDisplay() {
        return display;
    }

    public void setDisplay(ScreenMapping display) {
        this.display = display;
    }

    public double getPerspective_scale() {
        return perspective_scale;
    }

    public void setPerspective_scale(double perspective_scale) {
        this.perspective_scale = perspective_scale;
    }

    public double getTranslate_zoom_x() {
        return translate_zoom_x;
    }

    public void setTranslate_zoom_x(double translate_zoom_x) {
        this.translate_zoom_x = translate_zoom_x;
    }

    public double getTranslate_zoom_y() {
        return translate_zoom_y;
    }

    public void setTranslate_zoom_y(double translate_zoom_y) {
        this.translate_zoom_y = translate_zoom_y;
    }

    public DisplayExportThread getDisplayThread() {
        return displayThread;
    }

    public void setDisplayThread(DisplayExportThread displayThread) {
        this.displayThread = displayThread;
    }

    public JCheckBoxMenuItem getShowAnnotation() {
        return showAnnotation;
    }

    public void setShowAnnotation(JCheckBoxMenuItem showAnnotation) {
        this.showAnnotation = showAnnotation;
    }

    public JCheckBoxMenuItem getHighlightRegions() {
        return highlightRegions;
    }

    public void setHighlightRegions(JCheckBoxMenuItem highlightRegions) {
        this.highlightRegions = highlightRegions;
    }
}
