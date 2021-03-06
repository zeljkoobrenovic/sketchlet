package net.sf.sketchlet.designer.editor.ui.region;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import net.sf.sketchlet.common.Refresh;
import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.common.file.ImageFileFilter;
import net.sf.sketchlet.common.gif.AnimateGifDecoder;
import net.sf.sketchlet.common.gif.AnimateGifEncoder;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.media.ImageOperations;
import net.sf.sketchlet.designer.editor.resize.ResizeDialog;
import net.sf.sketchlet.designer.editor.resize.ResizeInterface;
import net.sf.sketchlet.designer.editor.tool.*;
import net.sf.sketchlet.designer.editor.ui.BufferedImageClipboardObject;
import net.sf.sketchlet.designer.playback.ui.PlaybackFrame;
import net.sf.sketchlet.framework.model.imagecache.ImageCache;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.util.RefreshTime;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ActiveRegionImageEditor extends JPanel implements KeyListener, Refresh, ToolInterface, ResizeInterface {
    private static final Logger log = Logger.getLogger(ActiveRegionImageEditor.class);

    private static float strokeValue;
    private static boolean bErase = false;
    private boolean imageUpdated = false;
    private ActiveRegion region;
    private boolean selectTransparentColor;
    private JButton newImage = new JButton(Workspace.createImageIcon("resources/image_bitmap_new.png"));
    private JButton deleteImage = new JButton(Workspace.createImageIcon("resources/remove.gif"));
    private ActiveRegionPanel parentActiveRegionPanel;
    private JTabbedPane drawingPanels = new JTabbedPane();
    private int index = 0;
    private static double scale = 1.0;
    private int zoomOptions[] = {3200, 2400, 1600, 1200, 800, 700, 600, 500, 400, 300, 200, 150, 140, 130, 120, 110, 100, 90, 80, 70, 66, 50, 33, 25, 16, 12, 8, 7, 6, 5, 4, 3, 2, 1};
    private JComboBox zoomBox = new JComboBox();
    private JButton zoomIn = new JButton(Workspace.createImageIcon("resources/zoomin.gif", ""));
    private JButton zoomOut = new JButton(Workspace.createImageIcon("resources/zoomout.gif", ""));
    private PenTool penTool = new PenTool(this);
    private ColorPickerTool colorPickerTool = new ColorPickerTool(this);
    private BucketTool bucketTool = new BucketTool(this);
    private TransparentColorTool transparentColorTool = new TransparentColorTool(this);
    private MagicWandTool magicWandTool = new MagicWandTool(this);
    private LineTool lineTool = new LineTool(this);
    private RectTool rectTool = new RectTool(this);
    private OvalTool ovalTool = new OvalTool(this);
    private EraserTool eraserTool = new EraserTool(this);
    private SelectTool selectTool = new SelectTool(this);
    private FreeFormSelectTool freeFormSelectTool = new FreeFormSelectTool(this);
    private static Tool currentTool;
    private JScrollPane scrollPane;

    private JButton moveFirst = new JButton(Workspace.createImageIcon("resources/go-first.png"));
    private JButton moveLeft = new JButton(Workspace.createImageIcon("resources/go-previous.png"));
    private JButton moveRight = new JButton(Workspace.createImageIcon("resources/go-next.png"));
    private JButton moveLast = new JButton(Workspace.createImageIcon("resources/go-last.png"));
    private static BufferedImage transparentPattern = null;
    private JCheckBoxMenuItem drawSketch = new JCheckBoxMenuItem(Language.translate("show main sketch image in background"), Workspace.createImageIcon("resources/background.png"));
    private JCheckBoxMenuItem drawPrev = new JCheckBoxMenuItem(Language.translate("show previous frame in background"), Workspace.createImageIcon("resources/background.png"));
    private JCheckBoxMenuItem drawShape = new JCheckBoxMenuItem(Language.translate("show shape outline"), Workspace.createImageIcon("resources/oval.png"));
    private JCheckBoxMenuItem drawPoints = new JCheckBoxMenuItem(Language.translate("show rotation and trajectory point"), Workspace.createImageIcon("resources/points.png"));

    public ActiveRegionImageEditor(final ActiveRegion region, ActiveRegionPanel parentActiveRegionPanel) {
        this.setParentActiveRegionPanel(parentActiveRegionPanel);

        if (getCurrentTool() == null) {
            setTool(getPenTool(), null);
        } else {
            getCurrentTool().deactivate();
            getCurrentTool().setToolInterface(this);
            getCurrentTool().activate();
        }

        if (transparentPattern == null) {
            try {
                Image img = Workspace.createImageIcon("resources/transparent_pattern.png").getImage();
                transparentPattern = Workspace.createCompatibleImage(img.getWidth(null), img.getHeight(null));
                Graphics2D g2 = transparentPattern.createGraphics();
                g2.drawImage(img, 0, 0, null);
                g2.dispose();
            } catch (Throwable e) {
                log.error("Active region image editor transparent pattern error", e);
            }
        }

        drawSketch.setSelected(false);
        drawPrev.setSelected(true);
        drawShape.setSelected(true);
        drawPoints.setSelected(false);

        DrawingListenerSimple listener = new DrawingListenerSimple(this);
        this.addMouseListener(listener);
        this.addMouseMotionListener(listener);

        moveFirst.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (getIndex() > 0) {
                    BufferedImage img1 = region.getDrawnImage(getIndex());

                    for (int i = getIndex(); i >= 1; i--) {
                        region.setDrawnImage(i, region.getDrawnImage(i - 1));
                        region.setDrawnImageChanged(i, true);
                    }

                    region.setDrawnImage(0, img1);
                    region.setDrawnImageChanged(0, true);
                    getDrawingPanels().setSelectedIndex(0);
                }
            }
        });
        moveLeft.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (getIndex() > 0) {
                    BufferedImage img1 = region.getDrawnImage(getIndex());
                    BufferedImage img2 = region.getDrawnImage(getIndex() - 1);

                    region.setDrawnImage(getIndex() - 1, img1);
                    region.setDrawnImageChanged(getIndex() - 1, true);
                    region.setDrawnImage(getIndex(), img2);
                    region.setDrawnImageChanged(getIndex(), true);
                    getDrawingPanels().setSelectedIndex(getIndex() - 1);
                }
            }
        });
        moveRight.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (getIndex() < region.getImageCount() - 1) {
                    BufferedImage img1 = region.getDrawnImage(getIndex());
                    BufferedImage img2 = region.getDrawnImage(getIndex() + 1);

                    region.setDrawnImage(getIndex() + 1, img1);
                    region.setDrawnImageChanged(getIndex() + 1, true);
                    region.setDrawnImage(getIndex(), img2);
                    region.setDrawnImageChanged(getIndex(), true);
                    getDrawingPanels().setSelectedIndex(getIndex() + 1);
                }
            }
        });
        moveLast.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (getIndex() < region.getImageCount() - 1) {
                    BufferedImage img1 = region.getDrawnImage(getIndex());

                    for (int i = getIndex(); i < region.getImageCount() - 1; i++) {
                        region.setDrawnImage(i, region.getDrawnImage(i + 1));
                        region.setDrawnImageChanged(i, true);
                    }


                    region.setDrawnImage(region.getImageCount() - 1, img1);
                    region.setDrawnImageChanged(region.getImageCount() - 1, true);
                    getDrawingPanels().setSelectedIndex(region.getImageCount() - 1);
                }
            }
        });

        prepareZoomBox();

        getDrawingPanels().setFont(getDrawingPanels().getFont().deriveFont(9.0f));
        getDrawingPanels().putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(getDrawingPanels());
        getDrawingPanels().addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (getScrollPane() != null && getDrawingPanels().getSelectedIndex() >= 0) {
                    if (getCurrentTool() != null) {
                        getCurrentTool().deactivate();
                    }
                    setIndex(getDrawingPanels().getSelectedIndex());
                    enableControls();

                    for (int i = 0; i < getDrawingPanels().getTabCount(); i++) {
                        getDrawingPanels().setComponentAt(i, new JPanel());
                    }

                    if (getIndex() >= 0 && getIndex() < getDrawingPanels().getTabCount()) {
                        getDrawingPanels().setComponentAt(getIndex(), getScrollPane());
                    }
                    if (getCurrentTool() != null) {
                        getCurrentTool().activate();
                    }
                    setStroke();

                    createGraphics();

                    revalidateAll();
                    repaintAll();
                }
            }
        });

        deleteImage.setEnabled(false);
        deleteImage.setToolTipText(Language.translate("Delete image"));
        deleteImage.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                deleteImage();
            }
        });
        newImage.setToolTipText(Language.translate("Adds new image to the active region"));

        final ActiveRegionImageEditor fhs = this;

        new FileDrop(System.out, this, new FileDrop.Listener() {

            public void filesDropped(Point p, java.io.File[] files) {
                if (files.length > 0) {
                    fromFile(files[0], fhs);
                }
            }

            public void dragOver(int x, int y) {
            }

            public void stringDropped(Point p, String strText) {
            }
        });
        this.setRegion(region);
        if (getColor() == null) {
            setColor(Color.BLACK);
        }

        setStrokeValue(2f);

        setFocusable(true);
        this.addKeyListener(this);
        enableControls();
    }

    public static void setStroke(Stroke stroke) {
    }

    public static float getStrokeValue() {
        return strokeValue;
    }

    public static void setStrokeValue(float strokeValue) {
        ActiveRegionImageEditor.strokeValue = strokeValue;
    }

    public static Tool getCurrentTool() {
        return currentTool;
    }

    public static void setCurrentTool(Tool currentTool) {
        ActiveRegionImageEditor.currentTool = currentTool;
    }

    public static double getScale() {
        return scale;
    }

    public static void setScale(double scale) {
        ActiveRegionImageEditor.scale = scale;
    }

    public void enableControls() {
        this.deleteImage.setEnabled(getRegion().getAdditionalDrawnImages().size() > 0);

        if (getRegion().getAdditionalDrawnImages().size() > 0) {
            moveLeft.setEnabled(getIndex() > 0);
            moveFirst.setEnabled(getIndex() > 0);
            moveRight.setEnabled(getIndex() < getRegion().getAdditionalDrawnImages().size());
            moveLast.setEnabled(getIndex() < getRegion().getAdditionalDrawnImages().size());
        } else {
            moveLeft.setEnabled(false);
            moveRight.setEnabled(false);
            moveFirst.setEnabled(false);
            moveLast.setEnabled(false);
        }
    }

    public synchronized void deleteImage() {
        if (getDrawingPanels().getTabCount() > 1) {
            int selectedTabIndex = getDrawingPanels().getSelectedIndex();
            getRegion().deleteDrawnImage(selectedTabIndex);

            getParentActiveRegionPanel().loadDrawingTabs();

            if (selectedTabIndex - 1 >= 0 && selectedTabIndex - 1 < getDrawingPanels().getTabCount()) {
                setIndex(selectedTabIndex - 1);
                getDrawingPanels().setSelectedIndex(selectedTabIndex - 1);
            } else {
                setIndex(0);
                getDrawingPanels().setSelectedIndex(0);
            }

            enableControls();
            createGraphics();
            repaint();
        }
    }

    public Dimension getDrawImageDimension() {
        int w = getRegion().getDrawnImage(getIndex()).getWidth();
        int h = getRegion().getDrawnImage(getIndex()).getHeight();

        if (w == 1 && h == 1) {
            if (getIndex() > 0) {
                w = getRegion().getDrawnImage(0).getWidth();
                h = getRegion().getDrawnImage(0).getHeight();
            }
            if (w == 1 && h == 1) {
                w = getRegion().getWidthValue();
                h = getRegion().getHeightValue();
            }
        }

        return new Dimension(w, h);
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        super.paintComponent(g);

        if (getScale() > 0) {
            g2.scale(getScale(), getScale());
        }

        int indexPrev = getIndex() - 1;

        if (getRegion().getDrawnImage(getIndex()) == null) {
            getRegion().initImage(getIndex());
        }

        if (getRegion().getDrawnImage(getIndex()) == null) {
            return;
        }

        Dimension d = getDrawImageDimension();
        int w = d.width;
        int h = d.height;

        g2.setPaint(new TexturePaint(transparentPattern, new Rectangle(16, 16)));
        g2.fillRect(0, 0, w, h);
        g2.setPaint(Workspace.getSketchBackground());
        if (this.drawPrev.isSelected() && indexPrev >= 0 && getRegion().getDrawnImage(indexPrev) != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g.drawImage(getRegion().getDrawnImage(indexPrev), 0, 0, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        if (this.drawSketch.isSelected()) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            BufferedImage img = SketchletEditor.getInstance().getImage().getSubimage(getRegion().getX1Value(), getRegion().getY1Value(), getRegion().getWidthValue(), getRegion().getHeightValue());
            g.drawImage(img, 0, 0, w, h, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        if (this.drawShape.isSelected() && !getRegion().getShape().isEmpty()) {
            AffineTransform affine = g2.getTransform();
            g2.setColor(new Color(0, 0, 0, 100));
            Area a = getRegion().getArea(false);
            float dash1[] = {10.0f};
            float thick = 2.0f;
            BasicStroke dashed = new BasicStroke(thick,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER,
                    10.0f, dash1, 0.0f);

            Stroke oldStroke = g2.getStroke();
            g2.setStroke(dashed);
            if (getRegion().isFitToBoxEnabled()) {
                g2.scale(w / a.getBounds().getWidth(), h / a.getBounds().getHeight());
            }
            g2.translate(-a.getBounds().x, -a.getBounds().y);
            g2.draw(a);
            g2.setStroke(oldStroke);
            g2.setTransform(affine);
        }
        if (this.drawShape.isSelected()) {
            AffineTransform affine = g2.getTransform();
            int p1x, p1y, p2x, p2y;
            if (getRegion().isFitToBoxEnabled()) {
                p1x = (int) (getRegion().getCenterOfRotationX() * w);
                p1y = (int) (getRegion().getCenterOfRotationY() * h);
                p2x = (int) (getRegion().getTrajectory2X() * w);
                p2y = (int) (getRegion().getTrajectory2Y() * h);
            } else {
                int _w = getRegion().getWidthValue();
                int _h = getRegion().getWidthValue();
                p1x = (int) (getRegion().getCenterOfRotationX() * _w);
                p1y = (int) (getRegion().getCenterOfRotationY() * _h);
                p2x = (int) (getRegion().getTrajectory2X() * _w);
                p2y = (int) (getRegion().getTrajectory2Y() * _h);
            }
            g2.setColor(new Color(200, 0, 0, 100));
            g2.fillOval(p1x - 3, p1y - 3, 7, 7);
            g2.setColor(new Color(0, 0, 200, 100));
            g2.fillOval(p2x - 3, p2y - 3, 7, 7);
            g2.setTransform(affine);
        }

        if (getRegion().getDrawnImage(getIndex()).getWidth() > 1) {
            g.drawImage(getRegion().getDrawnImage(getIndex()), 0, 0, this);
        }

        g2.setColor(Color.BLACK);
        g2.drawRect(0, 0, w, h);

        if (getCurrentTool() != null) {
            getCurrentTool().draw(g2);
        }
    }

    public void draw(Point start, Point end) {
        setImageUpdated(true);
        getRegion().setDrawnImageChanged(getIndex(), true);

        Graphics2D g2 = getRegion().getDrawnImage(getIndex()).createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (bErase) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
            g2.setStroke(new BasicStroke(20, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        } else {
            g2.setPaint(getColor());
            g2.setStroke(getStroke());
        }
        g2.draw(new Line2D.Double(start, end));
        g2.dispose();
        repaintAll();
    }

    public void save() {
        if (isImageUpdated()) {
            try {
                File file = new File(getRegion().getDrawnImagePath(getIndex()));
                ImageCache.write(getRegion().getDrawnImage(getIndex()), file);
                SketchletEditor.getInstance().repaint();
            } catch (IOException e) {
                log.error(e);
            }
        }
    }

    public void editor() {
        this.setImageUpdated(true);
        save();
        SketchletContextUtils.editImages("\"" + getRegion().getDrawnImagePath(getIndex()) + "\"", this, getIndex());
    }

    public void keyReleased(KeyEvent e) {
        bInShiftMode = false;
        bInCtrlMode = false;

        if (getCurrentTool() != null) {
            this.getCurrentTool().keyReleased(e);
        }
    }

    public void keyTyped(KeyEvent e) {

        if (getCurrentTool() != null) {
            this.getCurrentTool().keyTyped(e);
        }
    }

    boolean bInCtrlMode = false;
    boolean bInShiftMode = false;

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        int modifiers = e.getModifiers();

        bInShiftMode = (modifiers & KeyEvent.SHIFT_MASK) != 0;
        bInCtrlMode = (modifiers & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0;

        if ((modifiers & KeyEvent.ALT_MASK) == 0 && (modifiers & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) == 0) {
            if (key == KeyEvent.VK_D) {
                SketchletEditor.getInstance().getModeToolbar().btnSketching.doClick();
            } else if (key == KeyEvent.VK_L) {
                SketchletEditor.getInstance().getModeToolbar().btnLine.doClick();
            } else if (key == KeyEvent.VK_R) {
                SketchletEditor.getInstance().getModeToolbar().btnRect.doClick();
            } else if (key == KeyEvent.VK_O) {
                SketchletEditor.getInstance().getModeToolbar().btnOval.doClick();
            } else if (key == KeyEvent.VK_S) {
                SketchletEditor.getInstance().getModeToolbar().btnSelect.doClick();
            } else if (key == KeyEvent.VK_F) {
                SketchletEditor.getInstance().getModeToolbar().btnFreeFormSelect.doClick();
            } else if (key == KeyEvent.VK_P) {
                SketchletEditor.getInstance().getModeToolbar().btnColorPicker.doClick();
            } else if (key == KeyEvent.VK_M) {
                SketchletEditor.getInstance().getModeToolbar().btnMagicWand.doClick();
            } else if (key == KeyEvent.VK_B) {
                SketchletEditor.getInstance().getModeToolbar().btnBucket.doClick();
            } else if (key == KeyEvent.VK_E) {
                SketchletEditor.getInstance().getModeToolbar().btnEraser.doClick();
            } else if (key == KeyEvent.VK_PLUS || key == KeyEvent.VK_EQUALS || key == 107) {
                int n = SketchletEditor.getInstance().getColorToolbar().slider.getValue();
                SketchletEditor.getInstance().getColorToolbar().slider.setValue(n + 1);
            } else if (key == KeyEvent.VK_MINUS || key == 109) {
                int n = SketchletEditor.getInstance().getColorToolbar().slider.getValue();
                SketchletEditor.getInstance().getColorToolbar().slider.setValue(n - 1);
            }
        }
        if ((modifiers & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
            switch (key) {
                case KeyEvent.VK_C:
                    SketchletEditor.getInstance().skipKey = true;
                    this.copy();
                    break;
                case KeyEvent.VK_V:
                    SketchletEditor.getInstance().skipKey = true;
                    this.pasteImage();
                    break;
            }
        }

        if (getCurrentTool() != null) {
            this.getCurrentTool().keyPressed(e);
        }

    }

    public void copy() {
        BufferedImageClipboardObject tr = new BufferedImageClipboardObject(this.getRegion().getDrawnImage(getIndex()), DataFlavor.imageFlavor);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(tr, null);
    }

    public void saveUndo() {
        BufferedImage img = getRegion().getDrawnImage(getIndex());
        SketchletEditor.getInstance().saveRegionImageUndo(img, getRegion(), getIndex());
    }

    public JPanel getColorPanel() {
        JPanel colorPanel = new JPanel(new BorderLayout());

        JToolBar panel2 = new JToolBar();
        panel2.setFloatable(false);
        FlowLayout flowLayout2 = new FlowLayout(FlowLayout.CENTER);
        flowLayout2.setHgap(0);
        flowLayout2.setVgap(0);
        panel2.setLayout(flowLayout2);

        panel2.add(zoomOut);
        panel2.add(zoomBox);
        panel2.add(zoomIn);

        colorPanel.add(panel2, BorderLayout.SOUTH);

        setStroke();

        return colorPanel;
    }

    public void refreshImage(final int _index) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                saveUndo();
                getRegion().initImage(_index, true);
                repaintAll();
                setStroke();
            }
        });
    }

    public void refresh() {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                saveUndo();
                getRegion().initImage(getIndex(), true);
                repaintAll();
                setStroke();
            }
        });
    }

    public void setStroke() {
        getCurrentTool().deactivate();
        createGraphics();
        getCurrentTool().activate();
    }

    public JPanel getControlPanel() {
        final JButton clear = new JButton(Workspace.createImageIcon("resources/edit-clear.png"));
        final JButton copy = new JButton(Workspace.createImageIcon("resources/edit-copy.png"));
        final JButton clipboard = new JButton(Workspace.createImageIcon("resources/edit-paste.png"));
        final JButton fromFile = new JButton(Workspace.createImageIcon("resources/import.gif"));
        final JMenuItem extract = new JMenuItem(Language.translate("extract"), Workspace.createImageIcon("resources/edit-cut.png"));
        final JMenuItem stamp = new JMenuItem(Language.translate("stamp"), Workspace.createImageIcon("resources/stamp.png"));
        final JMenuItem versions = new JMenuItem(Workspace.createImageIcon("resources/history.gif"));
        final JButton resize = new JButton(Workspace.createImageIcon("resources/resize.png"));
        final JButton flipHorizontal = new JButton(Workspace.createImageIcon("resources/image_flip_horizontal.png"));
        final JButton flipVertical = new JButton(Workspace.createImageIcon("resources/image_flip_vertical.png"));
        final JButton rotateClockwise = new JButton(Workspace.createImageIcon("resources/image_rotate_clockwise.png"));
        final JButton rotateAntiClockwise = new JButton(Workspace.createImageIcon("resources/image_rotate_anticlockwise.png"));

        final JMenuItem imageEqRegion = new JMenuItem(Workspace.createImageIcon("resources/rectangle.png"));
        final JMenuItem regionEqImage = new JMenuItem(Workspace.createImageIcon("resources/rectangle.png"));
        final JMenuItem saveImage = new JMenuItem(Workspace.createImageIcon("resources/export.gif"));
        final JMenuItem saveAnimatedGif = new JMenuItem(Workspace.createImageIcon("resources/export.gif"));
        final JMenuItem editor = new JMenuItem(Language.translate("open image in external editor"), Workspace.createImageIcon("resources/imageeditor.gif"));
        final JMenuItem refresh = new JMenuItem(Workspace.createImageIcon("resources/view-refresh.png"));

        clear.setMnemonic(KeyEvent.VK_L);
        copy.setMnemonic(KeyEvent.VK_C);
        refresh.setMnemonic(KeyEvent.VK_R);
        refresh.setText(Language.translate("refresh"));

        clear.setToolTipText(Language.translate("Clear image"));
        copy.setToolTipText(Language.translate("Copy image to clipboard"));
        clipboard.setToolTipText(Language.translate("Paste image from clipboard"));
        fromFile.setToolTipText(Language.translate("Import image from file (JPG, GIF, PNG, PDF)"));
        resize.setToolTipText(Language.translate("Resize image"));
        flipHorizontal.setToolTipText(Language.translate("Flip horizontal"));
        flipVertical.setToolTipText(Language.translate("Flip vertical"));
        rotateClockwise.setToolTipText(Language.translate("Rotate clockwise"));
        rotateAntiClockwise.setToolTipText(Language.translate("Rotate anti-clockwise"));
        versions.setToolTipText(Language.translate("History of image changes"));
        versions.setText(Language.translate("history"));
        imageEqRegion.setToolTipText(Language.translate("Sets image size to equals region size"));
        imageEqRegion.setText(Language.translate("image size = region size"));
        regionEqImage.setToolTipText(Language.translate("Sets region size to equals image size"));
        regionEqImage.setText(Language.translate("region size = image size"));
        saveImage.setToolTipText(Language.translate("Export current frame as image"));
        saveImage.setText(Language.translate("export current frame as image"));
        saveAnimatedGif.setToolTipText(Language.translate("Export images as animated gif"));
        saveAnimatedGif.setText(Language.translate("export frames as animated GIF"));

        editor.setToolTipText(Language.translate("External image editor"));
        refresh.setToolTipText(Language.translate("Refresh image"));

        extract.setToolTipText(Language.translate("Extract image from main sketch (Alt X)"));
        stamp.setToolTipText(Language.translate("Draws (stamp) image over main skatch (Alt S)"));
        final JMenuItem defineClip = new JMenuItem(Language.translate("define visible area"), Workspace.createImageIcon("resources/clip.png"));
        defineClip.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().defineClip();
            }
        });
        final ActiveRegionImageEditor fhs = this;
        resize.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                Dimension d = getDrawImageDimension();
                int w = d.width;
                int h = d.height;

                new ResizeDialog(SketchletEditor.editorFrame, Language.translate("Resize Image"), fhs, w, h);
            }
        });

        flipHorizontal.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                flipHorizontal();
            }
        });
        flipVertical.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                flipVertical();
            }
        });
        rotateClockwise.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                rotateClockwise();
            }
        });
        rotateAntiClockwise.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                rotateAntiClockwise();
            }
        });


        newImage.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                createNewImage();
                requestFocus();
            }
        });

        clear.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                clearImage();
                repaint();
                requestFocus();
            }
        });

        clipboard.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                pasteImage();
                repaint();
                requestFocus();
            }
        });

        fromFile.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                fromFile(fromFile);
                requestFocus();
            }
        });

        extract.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.getInstance().extract(getDrawingPanels().getSelectedIndex());
                getRegion().initImage(getIndex());
                repaint();
                requestFocus();
            }
        });

        stamp.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.getInstance().stamp();
                requestFocus();
            }
        });

        versions.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                history();
            }
        });
        regionEqImage.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                resizeRegion();
            }
        });
        imageEqRegion.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                resizeImage();
            }
        });

        saveImage.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                saveAsImage();
            }
        });
        saveAnimatedGif.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                saveAsAnimatedGif();
            }
        });

        copy.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                copy();
                requestFocus();
            }
        });

        editor.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                editor();
                requestFocus();
            }
        });

        refresh.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                refresh();
                requestFocus();
            }
        });

        JPanel p = new JPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(220, 300));

        JToolBar panel = new JToolBar("Toolbar", JToolBar.HORIZONTAL);
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setFloatable(false);
        panel.add(newImage);
        panel.add(fromFile);
        panel.add(deleteImage);
        panel.add(copy);
        panel.add(clipboard);
        panel.add(clear);
        panel.add(resize);

        panel.add(moveFirst);
        panel.add(moveLeft);
        panel.add(moveRight);
        panel.add(moveLast);

        JToolBar panel2 = new JToolBar("Toolbar", JToolBar.HORIZONTAL);
        panel2.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel2.setFloatable(false);

        final JButton more = new JButton(Workspace.createImageIcon("resources/preferences-system.png", ""));
        more.setToolTipText(Language.translate("More commands"));
        more.setText(Language.translate("more..."));
        more.setMnemonic(KeyEvent.VK_M);

        panel.add(flipHorizontal);
        panel.add(flipVertical);
        panel.add(rotateClockwise);
        panel.add(rotateAntiClockwise);

        panel.add(more);
        panel.add(new JLabel(""));

        more.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                final JPopupMenu popupMenu = new JPopupMenu();

                popupMenu.add(drawSketch);
                popupMenu.add(drawPrev);
                popupMenu.addSeparator();
                popupMenu.add(drawShape);
                popupMenu.add(drawPoints);
                popupMenu.addSeparator();
                popupMenu.add(editor);
                popupMenu.add(refresh);
                popupMenu.addSeparator();
                popupMenu.add(extract);
                popupMenu.add(stamp);
                popupMenu.addSeparator();
                popupMenu.add(defineClip);
                popupMenu.addSeparator();
                popupMenu.add(imageEqRegion);
                popupMenu.add(regionEqImage);
                popupMenu.addSeparator();
                popupMenu.add(saveImage);
                popupMenu.add(saveAnimatedGif);

                saveAnimatedGif.setEnabled(getRegion().getAdditionalImageFileNames().size() > 0);

                popupMenu.show(more.getParent(), more.getX(), more.getY() + more.getHeight());
            }
        });

        panel2.setPreferredSize(new Dimension(215, 100));
        p.add(panel, BorderLayout.CENTER);

        return p;
    }

    static JFileChooser fc = new JFileChooser();

    public void saveAsImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setApproveButtonText(Language.translate("Save Image"));
        chooser.setDialogTitle(Language.translate("Save Frame as Image"));

        ImageFileFilter filter = new ImageFileFilter();
        chooser.setFileFilter(filter);

        int returnVal = chooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                if (!file.getName().contains(".")) {
                    file = new File(file.getPath() + ".png");
                }
                String extension = filter.getExtension(file);

                ImageIO.write(this.getImage(), extension, file);
            } catch (Throwable e) {
                log.error(e);
            }
        }
    }

    public void rotateClockwise() {
        if (getCurrentTool() != null) {
            getCurrentTool().deactivate();
        }
        this.saveUndo();
        BufferedImage img = getRegion().getDrawnImage(getIndex());
        BufferedImage img2 = ImageOperations.rotateClockwise(img);
        getRegion().setDrawnImage(getIndex(), img2);
        getRegion().setDrawnImageChanged(getIndex(), true);
        revalidate();
        repaint();
        if (getCurrentTool() != null) {
            getCurrentTool().activate();
            createGraphics();
        }
    }

    public void rotateAntiClockwise() {
        if (getCurrentTool() != null) {
            getCurrentTool().deactivate();
        }
        this.saveUndo();
        BufferedImage img = getRegion().getDrawnImage(getIndex());
        BufferedImage img2 = ImageOperations.rotateAntiClockwise(img);
        getRegion().setDrawnImage(getIndex(), img2);
        getRegion().setDrawnImageChanged(getIndex(), true);
        revalidate();
        repaint();
        if (getCurrentTool() != null) {
            getCurrentTool().activate();
            createGraphics();
        }
    }

    public void flipHorizontal() {
        if (getCurrentTool() != null) {
            getCurrentTool().deactivate();
        }
        this.saveUndo();
        BufferedImage img = getRegion().getDrawnImage(getIndex());
        BufferedImage img2 = ImageOperations.flipHorizontal(img);
        getRegion().setDrawnImage(getIndex(), img2);
        getRegion().setDrawnImageChanged(getIndex(), true);
        revalidate();
        repaint();
        if (getCurrentTool() != null) {
            getCurrentTool().activate();
            createGraphics();
        }
    }

    public void flipVertical() {
        if (getCurrentTool() != null) {
            getCurrentTool().deactivate();
        }
        this.saveUndo();
        BufferedImage img = getRegion().getDrawnImage(getIndex());
        BufferedImage img2 = ImageOperations.flipVertical(img);
        getRegion().setDrawnImage(getIndex(), img2);
        getRegion().setDrawnImageChanged(getIndex(), true);
        revalidate();
        repaint();
        if (getCurrentTool() != null) {
            getCurrentTool().activate();
            createGraphics();
        }
    }

    public void saveAsAnimatedGif() {
        AnimateGifEncoder anim = new AnimateGifEncoder();

        int delay = 1000;
        String strDelay = JOptionPane.showInputDialog(Language.translate("Enter delay between frames (in ms):"), "" + delay);

        fc.setApproveButtonText(Language.translate("Save Animated GIF"));
        fc.setDialogTitle(Language.translate("Save Frames as Animated GIF"));
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (strDelay != null) {
                try {
                    delay = Integer.parseInt(strDelay);
                } catch (Throwable e) {
                }
            }

            String strPath = file.getPath();
            if (!strPath.toLowerCase().endsWith(".gif")) {
                strPath += ".gif";
            }
            anim.start(strPath);
            anim.setDelay(delay);
            anim.setRepeat(0);
            anim.setTransparent(Color.PINK);
            for (int i = 0; i <= getRegion().getAdditionalImageFileNames().size(); i++) {
                BufferedImage img = getRegion().getDrawnImage(i);

                BufferedImage newImage = Workspace.createCompatibleImage(img.getWidth(), img.getHeight());
                Graphics2D g2 = newImage.createGraphics();
                g2.setColor(Color.PINK);
                g2.fillRect(0, 0, img.getWidth(), img.getHeight());
                g2.drawImage(img, 0, 0, null);
                g2.dispose();
                anim.addFrame(newImage);
            }

            anim.finish();

            JOptionPane.showMessageDialog(this, Language.translate("Animated GIF is saved."));
        }
    }

    public void history() {
    }

    public void resizeImage() {
        int w = getRegion().getWidthValue();
        int h = getRegion().getHeightValue();

        new ResizeDialog(SketchletEditor.editorFrame, Language.translate("Resize Image"), this, w, h);
    }

    public void revalidateAll() {
        getDrawingPanels().revalidate();
        this.revalidate();
    }

    public void repaintAll() {
        this.repaint();
    }

    public void resizeRegion() {
        int w = getRegion().getDrawnImage(getIndex()).getWidth();
        int h = getRegion().getDrawnImage(getIndex()).getHeight();

        getRegion().setX2Value(getRegion().getX1Value() + w);
        getRegion().setY2Value(getRegion().getY1Value() + h);

        SketchletEditor.getInstance().repaint();
    }

    public void createNewImage() {
        getCurrentTool().deactivate();

        getRegion().getAdditionalImageFileNames().add("");
        getRegion().getAdditionalDrawnImages().add(null);
        getRegion().getAdditionalDrawnImagesChanged().add(new Boolean(false));

        getDrawingPanels().add("" + (getDrawingPanels().getTabCount() + 1), new JPanel());
        getDrawingPanels().setSelectedIndex(getDrawingPanels().getTabCount() - 1);

        getRegion().clearImage(getIndex());

        createGraphics();
        getCurrentTool().activate();
    }

    public void clearImage() {
        saveUndo();
        getRegion().clearImage11(this.getDrawingPanels().getSelectedIndex());
        getRegion().saveImage();
        repaint();
    }

    public void pasteImage() {
        saveUndo();
        fromClipboard(getIndex());
        getRegion().setDrawnImageChanged(getIndex(), true);
        getRegion().saveImage();
        revalidate();
        this.createGraphics();
        repaint();
    }

    public void fromClipboard(int index) {
        saveImageUndo();

        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = clip.getContents(null);
        RenderedImage img;

        if (transferable.isDataFlavorSupported(new DataFlavor("image/x-java-image; class=java.awt.Image", "Image"))) {
            try {
                img = (RenderedImage) transferable.getTransferData(new DataFlavor("image/x-java-image; class=java.awt.Image", "Image"));
                int w = Math.max(getRegion().getDrawnImage(index).getWidth(), img.getWidth());
                int h = Math.max(getRegion().getDrawnImage(index).getHeight(), img.getHeight());
                BufferedImage oldImage = getRegion().getDrawnImage(index);
                BufferedImage pImg = Workspace.createCompatibleImage(img.getWidth(), img.getHeight());

                getRegion().setDrawnImage(index, Workspace.createCompatibleImage(w, h));

                Graphics2D g2 = getRegion().getDrawnImage(index).createGraphics();
                g2.drawImage(oldImage, 0, 0, null);
                g2.dispose();
                g2 = pImg.createGraphics();
                g2.drawRenderedImage(img, null);

                this.setTool(this.getSelectTool(), null);
                this.getSelectTool().setClip(pImg, 0, 0);

                getRegion().setDrawnImageChanged(index, true);
                getRegion().saveImage();

                g2.dispose();
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        } else {
            log.error("Object on clipboard is not an image!");
        }

        SketchletEditor.getInstance().repaint();
    }

    public Dimension getPreferredSize() {
        if (this.getRegion().getDrawnImage(getIndex()) == null) {
            return new Dimension((int) (220 * getScale()), (int) (220 * getScale()));
        } else {
            return new Dimension((int) (getRegion().getDrawnImage(getIndex()).getWidth() * getScale()), (int) (getRegion().getDrawnImage(getIndex()).getHeight() * getScale()));
        }
    }

    static JFrame freeHandFrame;

    public static void close() {
        if (freeHandFrame != null) {
            freeHandFrame.setVisible(false);
        }
    }

    public static void createAndShowGui() {
        final ActiveRegionImageEditor freeHand = new ActiveRegionImageEditor(null, null);
        DrawingListenerSimple listener = new DrawingListenerSimple(freeHand);
        freeHand.addMouseListener(listener);
        freeHand.addMouseMotionListener(listener);
        freeHandFrame = new JFrame();
        freeHandFrame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                freeHand.save();
                ActiveRegionsFrame.closeRegionsAndActions();
                PlaybackFrame.close();
            }
        });
        freeHandFrame.setTitle(Language.translate("Sketches"));

        freeHand.setScrollPane(new JScrollPane(freeHand));
        freeHand.getScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        freeHand.getScrollPane().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        freeHand.getDrawingPanels().addTab("1", freeHand.getScrollPane());

        freeHandFrame.getContentPane().add(freeHand.getDrawingPanels(), BorderLayout.CENTER);
        // editorFrame.getContentPane().add(editorPanel.getControlPanel(), BorderLayout.WEST);
        freeHandFrame.pack();
        freeHandFrame.setVisible(true);
    }

    public void makeTransparent() {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.setSelectTransparentColor(true);
    }

    public void makeTransparent(int color) {
        if (getRegion().getDrawnImage(getIndex()) != null) {
            for (int i = 0; i < getRegion().getDrawnImage(getIndex()).getWidth(); i++) {
                for (int j = 0; j < getRegion().getDrawnImage(getIndex()).getHeight(); j++) {
                    if (getRegion().getDrawnImage(getIndex()).getRGB(i, j) == color) {
                        getRegion().getDrawnImage(getIndex()).setRGB(i, j, new Color(0, 0, 0, 0).getRGB());
                    }
                }
            }

            this.repaint();

            this.getRegion().setDrawnImageChanged(getIndex(), true);
            this.getRegion().saveImage();
        }
    }

    public void fromFile(Component c) {
        int returnVal = ActiveRegionPanel.getFileChooser().showOpenDialog(SketchletEditor.editorFrame);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            fromFile(ActiveRegionPanel.getFileChooser().getSelectedFile(), c);
        }
    }

    public void fromFile(File file, Component c) {
        Object[] options = {Language.translate("Replace Current Image"),
                Language.translate("Add New Frame"),
                Language.translate("Cancel")};
        int n = JOptionPane.showOptionDialog(SketchletEditor.editorFrame,
                Language.translate("Do you want to replace current frame image ")
                        + "\n" + Language.translate("or add a new frame?"),
                Language.translate("Image Import"),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2]);

        if (n == 2 || n < 0) {
            return;
        } else if (n == 1) {
            this.createNewImage();
        }

        SketchletEditor.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (file.getPath().toLowerCase().endsWith(".gif")) {
            fromGifFile(file);
        } else if (file.getPath().toLowerCase().endsWith(".pdf")) {
            fromPdfFile(file);
        } else {
            try {
                saveUndo();
                BufferedImage newImage = Workspace.createCompatibleImageCopy(ImageIO.read(file));
                this.getRegion().setDrawnImage(getIndex(), newImage);

                this.getRegion().setSize(newImage.getWidth(), newImage.getHeight());

                this.revalidateAll();
                this.repaint();
                this.getRegion().setDrawnImageChanged(getIndex(), true);
                getRegion().saveImage();

            } catch (Throwable e) {
                log.error(e);
            }
        }
        SketchletEditor.getInstance().repaint();
        SketchletEditor.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public void fromGifFile(File file) {
        try {
            saveUndo();

            AnimateGifDecoder d = new AnimateGifDecoder();
            d.read(file.getPath());
            int n = d.getFrameCount();
            for (int i = 0; i < n; i++) {
                BufferedImage img = d.getFrame(i);  // frame i
                if (i > 0) {
                    this.createNewImage();
                }
                BufferedImage newImage = Workspace.createCompatibleImageCopy(img);
                this.getRegion().setDrawnImage(getIndex(), newImage);
                this.getRegion().setDrawnImageChanged(getIndex(), true);
                this.getRegion().setSize(img.getWidth(), img.getHeight());
            }
            getRegion().saveImage();

            this.revalidateAll();
            this.repaint();

        } catch (Throwable e) {
            log.error(e);
        }
    }

    public void fromPdfFile(File file) {
        try {
            saveUndo();

            RandomAccessFile raf = new RandomAccessFile(file, "r");
            FileChannel channel = raf.getChannel();
            ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            PDFFile pdfFile = new PDFFile(buf);

            int n = pdfFile.getNumPages();

            for (int i = 0; i < n; i++) {
                PDFPage page = pdfFile.getPage(i + 1);

                Rectangle2D rect = page.getBBox();

                int w = (int) rect.getWidth();
                int h = (int) rect.getHeight();

                Dimension d = page.getUnstretchedSize(w, h, rect); //generate the images
                w = (int) d.getWidth();
                h = (int) d.getHeight();
                Image image = page.getImage(
                        w, h, //width & height
                        rect, // clip rect
                        null, // null for the ImageObserver
                        false, // fill background with white
                        true // block until drawing is done
                );
                if (page.getRotation() == 90 || page.getRotation() == 270) {
                    int t = w;
                }
                BufferedImage img = Workspace.createCompatibleImageCopy(image);
                if (i > 0) {
                    this.createNewImage();
                }
                this.getRegion().setDrawnImage(getIndex(), img);
                this.getRegion().setDrawnImageChanged(getIndex(), true);
                this.getRegion().setSize(img.getWidth(), img.getHeight());
            }
            getRegion().saveImage();

            this.revalidateAll();
            this.repaint();

        } catch (Throwable e) {
            log.error(e);
        }
    }

    public static void main(String[] args) {
        createAndShowGui();
    }

    public void restoreImage(File imageFile, Component c) {
        fromFile(imageFile, c);
    }

    public void previewImage(File imageFile) {
        try {
            this.getRegion().setDrawnImage(getIndex(), ImageIO.read(imageFile));
            this.repaint();
        } catch (Throwable e) {
            log.error(e);
        }
    }

    public BufferedImage getImage() {
        return getRegion().getDrawnImage(getIndex());
    }

    public void saveImageUndo() {
        this.saveUndo();
    }

    public void setImageUpdated(boolean bUpdated) {
        getRegion().setDrawnImageChanged(getIndex(), bUpdated);
    }

    public void setImage(BufferedImage img) {
        saveUndo();
        getRegion().setDrawnImage(getIndex(), img);
        getRegion().setDrawnImageChanged(getIndex(), true);
        getRegion().saveImage();
        this.createGraphics();
        revalidate();
        repaint();
    }

    public void setCursor() {
        setCursor(Cursor.getDefaultCursor());
    }

    Graphics2D graphics2D = null;

    public Graphics2D getImageGraphics() {
        if (graphics2D == null) {
            return createGraphics();
        } else {
            return graphics2D;
        }
    }

    public Graphics2D createGraphics() {
        if (graphics2D != null) {
            graphics2D.dispose();
        }
        if (getRegion() != null && getRegion().getDrawnImage(getIndex()) != null) {
            graphics2D = getRegion().getDrawnImage(getIndex()).createGraphics();
            return graphics2D;
        } else {
            return null;
        }
    }

    public void setImageCursor(Cursor cursor) {
        this.setCursor(cursor);
    }

    public void repaintImage() {
        this.repaintAll();
    }

    public boolean isInShiftMode() {
        return this.bInShiftMode;
    }

    public boolean isInCtrlMode() {
        return this.bInCtrlMode;
    }

    public void setColor(Color c) {
    }

    public Color getColor() {
        return SketchletEditor.getInstance().getColor();
    }

    public Stroke getStroke() {
        return SketchletEditor.getInstance().getStroke();
    }

    public int getStrokeWidth() {
        return (SketchletEditor.getInstance().getColorToolbar() == null || SketchletEditor.getInstance().getColorToolbar().slider == null) ? 0 : SketchletEditor.getInstance().getColorToolbar().slider.getValue();
    }

    public int getImageWidth() {
        return getRegion().getDrawnImage(getIndex()).getWidth();
    }

    public int getImageHeight() {
        return getRegion().getDrawnImage(getIndex()).getHeight();
    }

    public BufferedImage extractImage(int x1, int y1, int w, int sh) {
        return SketchletEditor.extractImage(x1, y1, w, sh, this);
    }

    public BufferedImage extractImage(Polygon polygon) {
        return SketchletEditor.extractImage(polygon, this);
    }

    public void setTool(Tool tool, Component c) {
        if (this.getCurrentTool() != null) {
            this.getCurrentTool().deactivate();
        }
        this.setCurrentTool(tool);
        tool.activate();
    }

    public SelectTool getSelectTool() {
        return selectTool;
    }

    /*
     * public void setWatering() { float value = ((Integer)
     * sliderWatering.getValue()).floatValue();
     *
     * watering = 1 - value / sliderWatering.getMaximum(); createGraphics();
    }
     */
    public float getWatering() {
        return SketchletEditor.getInstance().getWatering();
    }

    public boolean shouldShapeFill() {
        return this.bInCtrlMode;
    }

    public boolean shouldShapeOutline() {
        return true;
    }

    public void resizeImage(int w, int h) {
        try {
            BufferedImage img = Workspace.createCompatibleImage(w, h);
            Graphics2D g2 = img.createGraphics();
            g2.drawImage(getRegion().getDrawnImage(getIndex()), 0, 0, w, h, null);
            g2.dispose();

            getRegion().setDrawnImage(getIndex(), img);

            getRegion().setDrawnImageChanged(getIndex(), true);
            getRegion().saveImage();
            revalidateAll();
            repaintAll();
            setStroke();
        } catch (Throwable e) {
        }
    }

    public void resizeCanvas(int w, int h) {
        try {
            BufferedImage img = Workspace.createCompatibleImage(w, h);
            Graphics2D g2 = img.createGraphics();
            g2.drawImage(getRegion().getDrawnImage(getIndex()), 0, 0, null);
            g2.dispose();

            getRegion().setDrawnImage(getIndex(), img);

            getRegion().setDrawnImageChanged(getIndex(), true);
            getRegion().saveImage();
            revalidateAll();
            repaintAll();
            setStroke();
        } catch (Throwable e) {
        }
    }

    void prepareZoomBox() {
        zoomBox.setEditable(true);

        zoomBox.setPreferredSize(new Dimension(70, 25));

        for (int i = 0; i < zoomOptions.length; i++) {
            zoomBox.addItem(zoomOptions[i] + "%");
        }

        zoomBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                String strZoom = (String) zoomBox.getSelectedItem();

                try {
                    strZoom = strZoom.replace("%", "").trim();

                    double zoom = Double.parseDouble(strZoom);
                    setScale(zoom / 100.0);
                    revalidateAll();
                    repaintAll();
                    setStroke();
                } catch (Throwable e) {
                }
            }
        });

        zoomBox.setSelectedItem("100%");
        zoomIn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                String strZoom = (String) zoomBox.getSelectedItem();

                try {
                    strZoom = strZoom.replace("%", "").trim();

                    double zoom = Double.parseDouble(strZoom);

                    for (int i = zoomOptions.length - 1; i >= 0; i--) {

                        if (zoomOptions[i] > zoom) {
                            zoomBox.setSelectedItem(zoomOptions[i] + "%");
                            break;
                        }
                    }
                } catch (Throwable e) {
                }
            }
        });
        zoomOut.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                String strZoom = (String) zoomBox.getSelectedItem();

                try {
                    strZoom = strZoom.replace("%", "").trim();

                    double zoom = Double.parseDouble(strZoom);

                    for (int i = 0; i < zoomOptions.length; i++) {
                        if (zoomOptions[i] < zoom) {
                            zoomBox.setSelectedItem(zoomOptions[i] + "%");
                            break;
                        }
                    }
                } catch (Throwable e) {
                }
            }
        });
    }

    public Component getPanel() {
        return this.getScrollPane();
    }

    public String getName() {
        return "the region image area";
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public void setScrollPane(JScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }

    public PenTool getPenTool() {
        return penTool;
    }

    public void setPenTool(PenTool penTool) {
        this.penTool = penTool;
    }

    public ColorPickerTool getColorPickerTool() {
        return colorPickerTool;
    }

    public void setColorPickerTool(ColorPickerTool colorPickerTool) {
        this.colorPickerTool = colorPickerTool;
    }

    public BucketTool getBucketTool() {
        return bucketTool;
    }

    public void setBucketTool(BucketTool bucketTool) {
        this.bucketTool = bucketTool;
    }

    public TransparentColorTool getTransparentColorTool() {
        return transparentColorTool;
    }

    public void setTransparentColorTool(TransparentColorTool transparentColorTool) {
        this.transparentColorTool = transparentColorTool;
    }

    public MagicWandTool getMagicWandTool() {
        return magicWandTool;
    }

    public void setMagicWandTool(MagicWandTool magicWandTool) {
        this.magicWandTool = magicWandTool;
    }

    public LineTool getLineTool() {
        return lineTool;
    }

    public void setLineTool(LineTool lineTool) {
        this.lineTool = lineTool;
    }

    public RectTool getRectTool() {
        return rectTool;
    }

    public void setRectTool(RectTool rectTool) {
        this.rectTool = rectTool;
    }

    public OvalTool getOvalTool() {
        return ovalTool;
    }

    public void setOvalTool(OvalTool ovalTool) {
        this.ovalTool = ovalTool;
    }

    public EraserTool getEraserTool() {
        return eraserTool;
    }

    public void setEraserTool(EraserTool eraserTool) {
        this.eraserTool = eraserTool;
    }

    public void setSelectTool(SelectTool selectTool) {
        this.selectTool = selectTool;
    }

    public FreeFormSelectTool getFreeFormSelectTool() {
        return freeFormSelectTool;
    }

    public void setFreeFormSelectTool(FreeFormSelectTool freeFormSelectTool) {
        this.freeFormSelectTool = freeFormSelectTool;
    }

    public boolean isImageUpdated() {
        return imageUpdated;
    }

    public JTabbedPane getDrawingPanels() {
        return drawingPanels;
    }

    public void setDrawingPanels(JTabbedPane drawingPanels) {
        this.drawingPanels = drawingPanels;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public ActiveRegionPanel getParentActiveRegionPanel() {
        return parentActiveRegionPanel;
    }

    public void setParentActiveRegionPanel(ActiveRegionPanel parentActiveRegionPanel) {
        this.parentActiveRegionPanel = parentActiveRegionPanel;
    }

    public ActiveRegion getRegion() {
        return region;
    }

    public void setRegion(ActiveRegion region) {
        this.region = region;
    }

    public boolean isSelectTransparentColor() {
        return selectTransparentColor;
    }

    public void setSelectTransparentColor(boolean selectTransparentColor) {
        this.selectTransparentColor = selectTransparentColor;
    }
}

class DrawingListenerSimple extends MouseInputAdapter {

    private ActiveRegionImageEditor editor;

    public DrawingListenerSimple(ActiveRegionImageEditor fh) {
        this.editor = fh;
    }

    public void mousePressed(MouseEvent e) {
        BufferedImage img = editor.getRegion().getDrawnImage(editor.getIndex());
        if (img == null) {
            editor.getRegion().initImage(editor.getIndex());
            img = editor.getRegion().getDrawnImage(editor.getIndex());
        }

        if (img == null) {
            return;
        }
        int w = img.getWidth();
        int h = img.getHeight();

        if (w == 1 && h == 1) {
            Dimension d = editor.getDrawImageDimension();
            w = d.width;
            h = d.height;
            editor.getRegion().setDrawnImage(editor.getIndex(), Workspace.createCompatibleImage(w, h, img));
        }

        if (editor.isSelectTransparentColor()) {
            editor.makeTransparent(editor.getImage().getRGB((int) (e.getPoint().x / editor.getScale()), (int) (e.getPoint().y / editor.getScale())));
            editor.setSelectTransparentColor(false);
            editor.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        } else {
            editor.getCurrentTool().mousePressed((int) (e.getX() / editor.getScale()), (int) (e.getY() / editor.getScale()), e.getModifiers());
            editor.repaint();
        }
    }

    public void mouseDragged(MouseEvent e) {
        editor.getCurrentTool().mouseDragged((int) (e.getX() / editor.getScale()), (int) (e.getY() / editor.getScale()), e.getModifiers());
        editor.repaint();
    }

    public void mouseReleased(MouseEvent e) {
        editor.getCurrentTool().mouseReleased((int) (e.getX() / editor.getScale()), (int) (e.getY() / editor.getScale()), e.getModifiers());

        RefreshTime.update();
        editor.repaint();
        SketchletEditor.getInstance().repaint();
        editor.requestFocus();
    }

    public void mouseMoved(MouseEvent e) {
        editor.setCursor(editor.getCurrentTool().getCursor());
        editor.getCurrentTool().mouseMoved((int) (e.getX() / editor.getScale()), (int) (e.getY() / editor.getScale()), e.getModifiers());
    }
}
