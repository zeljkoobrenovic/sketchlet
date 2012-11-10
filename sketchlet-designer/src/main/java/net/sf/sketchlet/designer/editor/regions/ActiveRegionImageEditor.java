/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.regions;

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
import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.editor.ImageOperations;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.resize.ResizeDialog;
import net.sf.sketchlet.designer.editor.resize.ResizeInterface;
import net.sf.sketchlet.designer.editor.tool.*;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.tools.imagecache.ImageCache;
import net.sf.sketchlet.designer.ui.BufferedImageClipboardObject;
import net.sf.sketchlet.designer.ui.playback.PlaybackFrame;
import net.sf.sketchlet.designer.ui.region.ActiveRegionPanel;
import net.sf.sketchlet.designer.ui.region.ActiveRegionsFrame;
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

    static Color color;
    static Stroke stroke;
    static float strokeValue;
    static boolean bErase = false;
    public boolean imageUpdated = false;
    ActiveRegion region;
    boolean selectTransparentColor;
    JButton newImage = new JButton(Workspace.createImageIcon("resources/image_bitmap_new.png"));
    JButton deleteImage = new JButton(Workspace.createImageIcon("resources/remove.gif"));
    public ActiveRegionPanel parent;
    public JTabbedPane drawingPanels = new JTabbedPane();
    public int index = 0;
    public static double scale = 1.0;
    public int zoomOptions[] = {3200, 2400, 1600, 1200, 800, 700, 600, 500, 400, 300, 200, 150, 140, 130, 120, 110, 100, 90, 80, 70, 66, 50, 33, 25, 16, 12, 8, 7, 6, 5, 4, 3, 2, 1};
    public JComboBox zoomBox = new JComboBox();
    JButton zoomIn = new JButton(Workspace.createImageIcon("resources/zoomin.gif", ""));
    JButton zoomOut = new JButton(Workspace.createImageIcon("resources/zoomout.gif", ""));
    public PenTool penTool = new PenTool(this);
    public ColorPickerTool colorPickerTool = new ColorPickerTool(this);
    public BucketTool bucketTool = new BucketTool(this);
    public TransparentColorTool transparentColorTool = new TransparentColorTool(this);
    public MagicWandTool magicWandTool = new MagicWandTool(this);
    public LineTool lineTool = new LineTool(this);
    public RectTool rectTool = new RectTool(this);
    public OvalTool ovalTool = new OvalTool(this);
    public EraserTool eraserTool = new EraserTool(this);
    public SelectTool selectTool = new SelectTool(this);
    public FreeFormSelectTool freeFormSelectTool = new FreeFormSelectTool(this);
    public static Tool currentTool;
    public JScrollPane scrollPane;
    //StrokeCombo strokeType;
    JButton moveFirst = new JButton(Workspace.createImageIcon("resources/go-first.png"));
    JButton moveLeft = new JButton(Workspace.createImageIcon("resources/go-previous.png"));
    JButton moveRight = new JButton(Workspace.createImageIcon("resources/go-next.png"));
    JButton moveLast = new JButton(Workspace.createImageIcon("resources/go-last.png"));
    static BufferedImage transparentPattern = null;
    JCheckBoxMenuItem drawSketch = new JCheckBoxMenuItem(Language.translate("show main sketch image in background"), Workspace.createImageIcon("resources/background.png"));
    JCheckBoxMenuItem drawPrev = new JCheckBoxMenuItem(Language.translate("show previous frame in background"), Workspace.createImageIcon("resources/background.png"));
    JCheckBoxMenuItem drawShape = new JCheckBoxMenuItem(Language.translate("show shape outline"), Workspace.createImageIcon("resources/oval.png"));
    JCheckBoxMenuItem drawPoints = new JCheckBoxMenuItem(Language.translate("show rotation and trajectory point"), Workspace.createImageIcon("resources/points.png"));

    public ActiveRegionImageEditor(final ActiveRegion region, ActiveRegionPanel parent) {
        this.parent = parent;

        if (currentTool == null) {
            setTool(penTool, null);
        } else {
            currentTool.deactivate();
            currentTool.toolInterface = this;
            currentTool.activate();
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

        /*
         * strokeType = StrokeCombo.getInstance();
         * strokeType.addActionListener(new ActionListener() {
         *
         * public void actionPerformed(ActionEvent e) { setStroke(); } });
        strokeType.setSelectedIndex(0);
         */

        moveFirst.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (index > 0) {
                    BufferedImage img1 = region.getDrawImage(index);

                    for (int i = index; i >= 1; i--) {
                        region.setDrawImage(i, region.getDrawImage(i - 1));
                        region.setDrawImageChanged(i, true);
                    }

                    region.setDrawImage(0, img1);
                    region.setDrawImageChanged(0, true);
                    drawingPanels.setSelectedIndex(0);
                    TutorialPanel.addLine("cmd", "Move the current active region image as the first in the list of frames", "go-first.png", moveFirst);
                }
            }
        });
        moveLeft.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (index > 0) {
                    BufferedImage img1 = region.getDrawImage(index);
                    BufferedImage img2 = region.getDrawImage(index - 1);

                    region.setDrawImage(index - 1, img1);
                    region.setDrawImageChanged(index - 1, true);
                    region.setDrawImage(index, img2);
                    region.setDrawImageChanged(index, true);
                    drawingPanels.setSelectedIndex(index - 1);
                    TutorialPanel.addLine("cmd", "Move the current active region image to the left in the list of frames", "go-previous.png", moveLeft);
                }
            }
        });
        moveRight.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (index < region.getImageCount() - 1) {
                    BufferedImage img1 = region.getDrawImage(index);
                    BufferedImage img2 = region.getDrawImage(index + 1);

                    region.setDrawImage(index + 1, img1);
                    region.setDrawImageChanged(index + 1, true);
                    region.setDrawImage(index, img2);
                    region.setDrawImageChanged(index, true);
                    drawingPanels.setSelectedIndex(index + 1);
                    TutorialPanel.addLine("cmd", "Move the current active region image to the right in the list of frames", "go-next.png", moveRight);
                }
            }
        });
        moveLast.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (index < region.getImageCount() - 1) {
                    BufferedImage img1 = region.getDrawImage(index);

                    for (int i = index; i < region.getImageCount() - 1; i++) {
                        region.setDrawImage(i, region.getDrawImage(i + 1));
                        region.setDrawImageChanged(i, true);
                    }


                    region.setDrawImage(region.getImageCount() - 1, img1);
                    region.setDrawImageChanged(region.getImageCount() - 1, true);
                    drawingPanels.setSelectedIndex(region.getImageCount() - 1);
                    TutorialPanel.addLine("cmd", "Move the current active region image as the last in the list of frames", "go-last.png", moveLast);
                }
            }
        });

        prepareZoomBox();

        drawingPanels.setFont(drawingPanels.getFont().deriveFont(9.0f));
        drawingPanels.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(drawingPanels);
        drawingPanels.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (scrollPane != null && drawingPanels.getSelectedIndex() >= 0) {
                    if (currentTool != null) {
                        currentTool.deactivate();
                    }
                    index = drawingPanels.getSelectedIndex();
                    enableControls();
                    // drawingPanels.remove(scrollPane);

                    for (int i = 0; i < drawingPanels.getTabCount(); i++) {
                        drawingPanels.setComponentAt(i, new JPanel());
                    }

                    if (index >= 0 && index < drawingPanels.getTabCount()) {
                        drawingPanels.setComponentAt(index, scrollPane);
                    }
                    if (currentTool != null) {
                        currentTool.activate();
                    }
                    setStroke();

                    createGraphics();

                    revalidateAll();
                    repaintAll();
                }
            }
        });
        TutorialPanel.prepare(drawingPanels);

        deleteImage.setEnabled(false);
        deleteImage.setToolTipText(Language.translate("Delete image"));
        deleteImage.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                deleteImage();
                TutorialPanel.addLine("cmd", Language.translate("Remove the active region image"), "remove.gif", deleteImage);
            }
        });
        newImage.setToolTipText(Language.translate("Adds new image to the active region"));

        /*
         * btnSketching.setToolTipText("Pencil, Shortcut Key: P");
         * btnSketching.addActionListener(new ActionListener() {
         *
         * public void actionPerformed(ActionEvent e) { setTool(penTool); } });
         * btnColorPicker.setToolTipText("Color Picker, Shortcut Key: C");
         * btnColorPicker.addActionListener(new ActionListener() {
         *
         * public void actionPerformed(ActionEvent e) {
         * setTool(colorPickerTool); } }); btnBucket.setToolTipText("Bucket,
         * Shortcut Key: B"); btnBucket.addActionListener(new ActionListener() {
         *
         * public void actionPerformed(ActionEvent e) { setTool(bucketTool); }
         * });
         *
         * btnMagicWand.setToolTipText("Magic Wand, Shortcut Key: M");
         * btnMagicWand.addActionListener(new ActionListener() {
         *
         * public void actionPerformed(ActionEvent e) { setTool(magicWandTool);
         * } });
         *
         * btnLine.setToolTipText("Line, Shortcut Key: L");
         * btnLine.addActionListener(new ActionListener() {
         *
         * public void actionPerformed(ActionEvent e) { setTool(lineTool); } });
         * btnRect.setToolTipText("Rectangle, Shortcut Key: R");
         * btnRect.addActionListener(new ActionListener() {
         *
         * public void actionPerformed(ActionEvent e) { setTool(rectTool); } });
         * btnOval.setToolTipText("Oval, Shortcut Key: O");
         * btnOval.addActionListener(new ActionListener() {
         *
         * public void actionPerformed(ActionEvent e) { setTool(ovalTool); } });
         * btnSelect.setToolTipText("Select, Shortcut Key: S");
         * btnSelect.addActionListener(new ActionListener() {
         *
         * public void actionPerformed(ActionEvent e) { setTool(selectTool); }
         * }); btnFreeFormSelect.setToolTipText("Free-Form Select, Shortcut Key:
         * F"); btnFreeFormSelect.addActionListener(new ActionListener() {
         *
         * public void actionPerformed(ActionEvent e) {
         * setTool(freeFormSelectTool); } });
         *
         * buttonEraser.setToolTipText("Eraser, Shortcut Key: E");
         * buttonEraser.addActionListener(new ActionListener() {
         *
         * public void actionPerformed(ActionEvent e) { setTool(eraserTool); }
        });
         */

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
        this.region = region;
        if (color == null) {
            color = Color.BLACK;
        }

        strokeValue = 2f;

        setFocusable(true);
        this.addKeyListener(this);
        enableControls();
    }

    public void enableControls() {
        this.deleteImage.setEnabled(region.additionalDrawImages.size() > 0);

        if (region.additionalDrawImages.size() > 0) {
            moveLeft.setEnabled(index > 0);
            moveFirst.setEnabled(index > 0);
            moveRight.setEnabled(index < region.additionalDrawImages.size());
            moveLast.setEnabled(index < region.additionalDrawImages.size());
        } else {
            moveLeft.setEnabled(false);
            moveRight.setEnabled(false);
            moveFirst.setEnabled(false);
            moveLast.setEnabled(false);
        }
    }

    public synchronized void deleteImage() {
        if (drawingPanels.getTabCount() > 1) {
            int selectedTabIndex = drawingPanels.getSelectedIndex();
            region.deleteDrawImage(selectedTabIndex);
            /*
             * for (int i = selectedTabIndex + 1; i < inTabs.size(); i++) {
             * drawingPanels.setTitleAt(i, "" + i); inTabs.elementAt(i).index--;
             * }
            inTabs.remove(selectedTabIndex);
             */

            parent.loadDrawingTabs();

            if (selectedTabIndex - 1 >= 0 && selectedTabIndex - 1 < drawingPanels.getTabCount()) {
                index = selectedTabIndex - 1;
                drawingPanels.setSelectedIndex(selectedTabIndex - 1);
            } else {
                index = 0;
                drawingPanels.setSelectedIndex(0);
            }

            enableControls();
            createGraphics();
            repaint();
        }
    }

    public Dimension getDrawImageDimension() {
        int w = region.getDrawImage(index).getWidth();
        int h = region.getDrawImage(index).getHeight();

        if (w == 1 && h == 1) {
            if (index > 0) {
                w = region.getDrawImage(0).getWidth();
                h = region.getDrawImage(0).getHeight();
            }
            if (w == 1 && h == 1) {
                w = region.getWidth();
                h = region.getHeight();
            }
        }

        return new Dimension(w, h);
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        super.paintComponent(g);

        if (scale > 0) {
            g2.scale(scale, scale);
        }

        int indexPrev = index - 1;

        if (region.getDrawImage(index) == null) {
            region.initImage(index);
        }

        if (region.getDrawImage(index) == null) {
            return;
        }

        Dimension d = getDrawImageDimension();
        int w = d.width;
        int h = d.height;

        g2.setPaint(new TexturePaint(transparentPattern, new Rectangle(16, 16)));
        g2.fillRect(0, 0, w, h);
        g2.setPaint(Workspace.sketchBackground);
        if (this.drawPrev.isSelected() && indexPrev >= 0 && region.getDrawImage(indexPrev) != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g.drawImage(region.getDrawImage(indexPrev), 0, 0, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        if (this.drawSketch.isSelected()) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            BufferedImage img = SketchletEditor.editorPanel.getImage().getSubimage(region.getX1(), region.getY1(), region.getWidth(), region.getHeight());
            g.drawImage(img, 0, 0, w, h, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        if (this.drawShape.isSelected() && !region.shape.isEmpty()) {
            AffineTransform affine = g2.getTransform();
            g2.setColor(new Color(0, 0, 0, 100));
            Area a = region.getArea(false);
            float dash1[] = {10.0f};
            float thick = 2.0f;
            BasicStroke dashed = new BasicStroke(thick,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER,
                    10.0f, dash1, 0.0f);

            Stroke oldStroke = g2.getStroke();
            g2.setStroke(dashed);
            if (region.bFitToBox) {
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
            if (region.bFitToBox) {
                p1x = (int) (region.center_rotation_x * w);
                p1y = (int) (region.center_rotation_y * h);
                p2x = (int) (region.trajectory2_x * w);
                p2y = (int) (region.trajectory2_y * h);
            } else {
                int _w = region.getWidth();
                int _h = region.getWidth();
                p1x = (int) (region.center_rotation_x * _w);
                p1y = (int) (region.center_rotation_y * _h);
                p2x = (int) (region.trajectory2_x * _w);
                p2y = (int) (region.trajectory2_y * _h);
            }
            g2.setColor(new Color(200, 0, 0, 100));
            g2.fillOval(p1x - 3, p1y - 3, 7, 7);
            g2.setColor(new Color(0, 0, 200, 100));
            g2.fillOval(p2x - 3, p2y - 3, 7, 7);
            g2.setTransform(affine);
        }

        if (region.getDrawImage(index).getWidth() > 1) {
            g.drawImage(region.getDrawImage(index), 0, 0, this);
        }

        g2.setColor(Color.BLACK);
        g2.drawRect(0, 0, w, h);

        if (currentTool != null) {
            currentTool.draw(g2);
        }
    }

    public void draw(Point start, Point end) {
        imageUpdated = true;
        region.setDrawImageChanged(index, true);

        Graphics2D g2 = region.getDrawImage(index).createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (bErase) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
            g2.setStroke(new BasicStroke(20, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        } else {
            g2.setPaint(color);
            g2.setStroke(stroke);
        }
        g2.draw(new Line2D.Double(start, end));
        g2.dispose();
        repaintAll();
    }

    public void save() {
        if (imageUpdated) {
            try {
                File file = new File(region.getDrawImagePath(index));
                ImageCache.write(region.getDrawImage(index), file);
                SketchletEditor.editorPanel.repaint();
            } catch (IOException e) {
                log.error(e);
            }
        }
    }

    public void editor() {
        this.imageUpdated = true;
        save();
        SketchletContextUtils.editImages("\"" + region.getDrawImagePath(index) + "\"", this, index);
    }

    public void keyReleased(KeyEvent e) {
        bInShiftMode = false;
        bInCtrlMode = false;

        if (currentTool != null) {
            this.currentTool.keyReleased(e);
        }
    }

    public void keyTyped(KeyEvent e) {

        if (currentTool != null) {
            this.currentTool.keyTyped(e);
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
                SketchletEditor.editorPanel.modeToolbar.btnSketching.doClick();
            } else if (key == KeyEvent.VK_L) {
                SketchletEditor.editorPanel.modeToolbar.btnLine.doClick();
            } else if (key == KeyEvent.VK_R) {
                SketchletEditor.editorPanel.modeToolbar.btnRect.doClick();
            } else if (key == KeyEvent.VK_O) {
                SketchletEditor.editorPanel.modeToolbar.btnOval.doClick();
            } else if (key == KeyEvent.VK_S) {
                SketchletEditor.editorPanel.modeToolbar.btnSelect.doClick();
            } else if (key == KeyEvent.VK_F) {
                SketchletEditor.editorPanel.modeToolbar.btnFreeFormSelect.doClick();
            } else if (key == KeyEvent.VK_P) {
                SketchletEditor.editorPanel.modeToolbar.btnColorPicker.doClick();
            } else if (key == KeyEvent.VK_M) {
                SketchletEditor.editorPanel.modeToolbar.btnMagicWand.doClick();
            } else if (key == KeyEvent.VK_B) {
                SketchletEditor.editorPanel.modeToolbar.btnBucket.doClick();
            } else if (key == KeyEvent.VK_E) {
                SketchletEditor.editorPanel.modeToolbar.btnEraser.doClick();
            } else if (key == KeyEvent.VK_PLUS || key == KeyEvent.VK_EQUALS || key == 107) {
                int n = SketchletEditor.editorPanel.colorToolbar.slider.getValue();
                SketchletEditor.editorPanel.colorToolbar.slider.setValue(n + 1);
            } else if (key == KeyEvent.VK_MINUS || key == 109) {
                int n = SketchletEditor.editorPanel.colorToolbar.slider.getValue();
                SketchletEditor.editorPanel.colorToolbar.slider.setValue(n - 1);
            }
        }
        if ((modifiers & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
            switch (key) {
                /*
                 * case KeyEvent.VK_E: editorPanel();
                break;
                 */
                /*
                 * case KeyEvent.VK_H: history();
                break;
                 */
                /*
                 * case KeyEvent.VK_S: save();
                break;
                 */
                case KeyEvent.VK_C:
                    SketchletEditor.editorPanel.skipKey = true;
                    this.copy();
                    break;
                /*
                 * case KeyEvent.VK_X: this.copy(); this.clearImage();
                break;
                 */
                case KeyEvent.VK_V:
                    SketchletEditor.editorPanel.skipKey = true;
                    this.pasteImage();
                    break;
                /*
                 * case KeyEvent.VK_R: this.refresh(); break; case
                 * KeyEvent.VK_I: this.fromFile();
                break;
                 */
            }
        }

        if (currentTool != null) {
            this.currentTool.keyPressed(e);
        }

    }

    public void copy() {
        BufferedImageClipboardObject tr = new BufferedImageClipboardObject(this.region.getDrawImage(index), DataFlavor.imageFlavor);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(tr, null);
    }

    public void saveUndo() {
        BufferedImage img = region.getDrawImage(index);
        SketchletEditor.editorPanel.saveRegionImageUndo(img, region, index);
        /*
         * if (img != null) { BufferedImage newImg = new
         * BufferedImage(img.getWidth(), img.getHeight(),
         * BufferedImage.TYPE_INT_ARGB); Graphics2D g2 =
         * newImg.createGraphics(); g2.drawImage(img, 0, 0, null); g2.dispose();
         *
         * undoImages.add(newImg); undo.setEnabled(true); if (undoImages.size()
         * > 5) { undoImages.remove(0); }
        }
         */
    }

    public JPanel getColorPanel() {
        Color[] colors = {
                Color.BLACK, Color.WHITE, Color.red, Color.green.darker(),
                Color.blue, Color.orange
        };
        JPanel colorPanel = new JPanel(new BorderLayout());
        ActionListener l = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                currentTool.deactivate();
                bErase = false;
                JButton button = (JButton) e.getSource();
                color = button.getBackground();
                currentTool.activate();
            }
        };
        /*
         * JToolBar panel = new JToolBar(); panel.setFloatable(false);
         * FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT);
         * flowLayout.setHgap(0); flowLayout.setVgap(0);
         * panel.setLayout(flowLayout); slider = new JSlider(JSlider.HORIZONTAL,
         * 1, 20, (int) strokeValue); slider.setValue(2);
         * slider.addChangeListener(new ChangeListener() {
         *
         * public void stateChanged(ChangeEvent e) { setStroke(); } });
         * slider.setPreferredSize(new Dimension(60, 20));
         *
         * sliderWatering = new JSlider(JSlider.HORIZONTAL, 0, 10, 0);
         * sliderWatering.setPreferredSize(new Dimension(20, 20));
         * sliderWatering.addChangeListener(new ChangeListener() {
         *
         * public void stateChanged(ChangeEvent e) { setWatering(); } });
         *
         * slider.setToolTipText("stroke width, shortcut key: (bigger +, smaller
         * -)"); sliderWatering.setToolTipText("watering");
         *
         * JPanel strokePanel = new JPanel(); strokePanel.add(strokeType);
         * strokePanel.add(slider); strokePanel.add(sliderWatering);
         * colorPanel.add(strokePanel, BorderLayout.NORTH); for (int j = 0; j <
         * colors.length; j++) { JButton button = new JButton(" ") {
         *
         * public void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D)
         * g; super.paintComponent(g);
         *
         * int w = getWidth(); int h = getHeight();
         * g2.setPaint(getBackground()); g2.fillRect(3, 3, w - 6, h - 6); } };
         * button.setFocusPainted(false); button.setBackground(colors[j]);
         * button.addActionListener(l); panel.add(button); } JButton moreColors
         * = new JButton(Workspace.createImageIcon("resources/palette.png"));
         * final FreeHandSimple freeHandSimple = this;
         * moreColors.addActionListener(new ActionListener() {
         *
         * public void actionPerformed(ActionEvent ae) { Color newColor =
         * JColorChooser.showDialog( freeHandSimple, "Choose Color",
         * freeHandSimple.color);
         *
         * if (newColor != null) { currentTool.deactivate();
         * freeHandSimple.color = newColor; currentTool.activate(); } } });
         *
         * panel.add(moreColors);
         *
         * panel.setPreferredSize(new Dimension(130, 100));
         *
         * colorPanel.add(panel);
         */

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
                region.initImage(_index, true);
                repaintAll();
                setStroke();
            }
        });
    }

    public void refresh() {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                saveUndo();
                region.initImage(index, true);
                repaintAll();
                setStroke();
            }
        });
    }

    public void setStroke() {
        //if (slider != null && strokeType != null) {
        currentTool.deactivate();
        /*
         * String strStroke = strokeType.getStroke(); float value = ((Integer)
         * slider.getValue()).floatValue();
         *
         * Stroke s = ColorToolbar.getStroke(strStroke, value); if (s != null) {
         * stroke = s;
         */
        createGraphics();
        //}
        currentTool.activate();
        //}
    }

    public JPanel getControlPanel() {
        final JButton clear = new JButton(Workspace.createImageIcon("resources/edit-clear.png"));
        final JButton copy = new JButton(Workspace.createImageIcon("resources/edit-copy.png"));
        final JButton clipboard = new JButton(Workspace.createImageIcon("resources/edit-paste.png"));
        // final JButton transparent = new JButton(Workspace.createImageIcon("resources/image_transparent_color.png"));
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

        // resize.setText("Resize images");

        editor.setToolTipText(Language.translate("External image editor"));
        refresh.setToolTipText(Language.translate("Refresh image"));

        extract.setToolTipText(Language.translate("Extract image from main sketch (Alt X)"));
        stamp.setToolTipText(Language.translate("Draws (stamp) image over main skatch (Alt S)"));
        // transparent.setToolTipText("Select transparent color");
        final JMenuItem defineClip = new JMenuItem(Language.translate("define visible area"), Workspace.createImageIcon("resources/clip.png"));
        defineClip.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.defineClip();
            }
        });
        final ActiveRegionImageEditor fhs = this;
        resize.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                Dimension d = getDrawImageDimension();
                int w = d.width;
                int h = d.height;

                new ResizeDialog(SketchletEditor.editorFrame, Language.translate("Resize Image"), fhs, w, h);
                TutorialPanel.addLine("cmd", "Resize the active region image (" + w + "x" + h + ")", "resize.png", resize);
            }
        });

        flipHorizontal.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                TutorialPanel.addLine("cmd", "Flip the region image horizontal", "image_flip_horizontal.png", flipHorizontal);
                flipHorizontal();
            }
        });
        flipVertical.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                TutorialPanel.addLine("cmd", "Flip the region image vertical", "image_flip_vertical.png", flipVertical);
                flipVertical();
            }
        });
        rotateClockwise.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                TutorialPanel.addLine("cmd", "Rotate the region image clockwise", "image_rotate_clockwise.png", rotateClockwise);
                rotateClockwise();
            }
        });
        rotateAntiClockwise.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                TutorialPanel.addLine("cmd", "Rotate the region image anticlockwise", "image_rotate_anticlockwise.png", rotateAntiClockwise);
                rotateAntiClockwise();
            }
        });


        newImage.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                createNewImage();
                requestFocus();
                TutorialPanel.addLine("cmd", "Add a new image to the active region", "image_bitmap_new.png", newImage);
            }
        });

        clear.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                clearImage();
                repaint();
                requestFocus();
                TutorialPanel.addLine("cmd", "Clear the active region image " + (index + 1), "edit-clear.png", clear);
            }
        });

        clipboard.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                pasteImage();
                repaint();
                requestFocus();
                TutorialPanel.addLine("cmd", "Paste an image in the active region", "edit-paste.png", clipboard);
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
                SketchletEditor.editorPanel.extract(drawingPanels.getSelectedIndex());
                region.initImage(index);
                repaint();
                requestFocus();
                TutorialPanel.addLine("cmd", "Extract an image from the main page in the active region", "edit-cut.png", extract);
            }
        });

        stamp.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.editorPanel.stamp();
                requestFocus();
                TutorialPanel.addLine("cmd", "Stamp the active region image onto the main page", "stamp.png", stamp);
            }
        });

        /*
         * transparent.addActionListener(new ActionListener() {
         *
         * public void actionPerformed(ActionEvent e) { makeTransparent();
         * repaint(); requestFocus(); }
        });
         */
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
                TutorialPanel.addLine("cmd", "Save the active region image to a file", "export.gif", saveImage);
            }
        });
        saveAnimatedGif.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                saveAsAnimatedGif();
                TutorialPanel.addLine("cmd", "Save the active region image as an animated gif", "export.gif", saveAnimatedGif);
            }
        });

        copy.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                copy();
                requestFocus();
                TutorialPanel.addLine("cmd", "Copy the active region image to a clipboard", "edit-copy.png", copy);
            }
        });

        editor.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                editor();
                requestFocus();
                TutorialPanel.addLine("cmd", "Open the active region image in an external editor", "imageeditor.gif", editor);
            }
        });

        refresh.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                refresh();
                requestFocus();
                TutorialPanel.addLine("cmd", "Refresh the active region image", "view-refresh.png", refresh);
            }
        });

        JPanel p = new JPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(220, 300));

        JToolBar panel = new JToolBar("Toolbar", JToolBar.HORIZONTAL);
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setFloatable(false);
        // panel.add(save);
        panel.add(newImage);
        panel.add(fromFile);
        panel.add(deleteImage);
        panel.add(copy);
        panel.add(clipboard);
        panel.add(clear);
        // panel.add(transparent);
        panel.add(resize);

        panel.add(moveFirst);
        panel.add(moveLeft);
        panel.add(moveRight);
        panel.add(moveLast);

        JToolBar panel2 = new JToolBar("Toolbar", JToolBar.HORIZONTAL);
        panel2.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel2.setFloatable(false);
        /*
         * panel2.add(btnSketching); panel2.add(btnLine); panel2.add(btnRect);
         * panel2.add(btnOval); panel2.add(btnBucket);
         * panel2.add(btnColorPicker); panel2.add(buttonEraser);
         * panel2.add(btnMagicWand); panel2.add(btnSelect);
        panel2.add(btnFreeFormSelect);
         */

        /*
         * panel.add(fromFile); panel.add(extract); panel.add(stamp);
         * panel.add(editorPanel); panel.add(refresh); panel.add(transparent);
         * panel.add(versions);
         */

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
                TutorialPanel.prepare(popupMenu);

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
                //popupMenu.add(fromFile);
                //popupMenu.addSeparator();
                //popupMenu.add(versions);
                //popupMenu.addSeparator();
                //popupMenu.add(resize);
                //popupMenu.addSeparator();
                popupMenu.add(imageEqRegion);
                popupMenu.add(regionEqImage);
                popupMenu.addSeparator();
                popupMenu.add(saveImage);
                popupMenu.add(saveAnimatedGif);

                saveAnimatedGif.setEnabled(region.additionalImageFile.size() > 0);

                popupMenu.show(more.getParent(), more.getX(), more.getY() + more.getHeight());

                TutorialPanel.addLine("cmd", "Click on the 'more...' button", "preferences-system.png", more);
            }
        });

        panel2.setPreferredSize(new Dimension(215, 100));
        p.add(panel, BorderLayout.CENTER);
        // p.add(panel2, BorderLayout.SOUTH);

        return p;
    }

    static JFileChooser fc = new JFileChooser();

    public void saveAsImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setApproveButtonText(Language.translate("Save Image"));
        chooser.setDialogTitle(Language.translate("Save Frame as Image"));
        //chooser.setSelectedFile(new File(chooser.getCurrentDirectory(), currentSketch.title + ".png"));

        ImageFileFilter filter = new ImageFileFilter();
        chooser.setFileFilter(filter);

        //In response to a button click:
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
        if (currentTool != null) {
            currentTool.deactivate();
        }
        this.saveUndo();
        BufferedImage img = region.getDrawImage(index);
        BufferedImage img2 = ImageOperations.rotateClockwise(img);
        region.setDrawImage(index, img2);
        region.setDrawImageChanged(index, true);
        revalidate();
        repaint();
        if (currentTool != null) {
            currentTool.activate();
            createGraphics();
        }
    }

    public void rotateAntiClockwise() {
        if (currentTool != null) {
            currentTool.deactivate();
        }
        this.saveUndo();
        BufferedImage img = region.getDrawImage(index);
        BufferedImage img2 = ImageOperations.rotateAntiClockwise(img);
        region.setDrawImage(index, img2);
        region.setDrawImageChanged(index, true);
        revalidate();
        repaint();
        if (currentTool != null) {
            currentTool.activate();
            createGraphics();
        }
    }

    public void flipHorizontal() {
        if (currentTool != null) {
            currentTool.deactivate();
        }
        this.saveUndo();
        BufferedImage img = region.getDrawImage(index);
        BufferedImage img2 = ImageOperations.flipHorizontal(img);
        region.setDrawImage(index, img2);
        region.setDrawImageChanged(index, true);
        revalidate();
        repaint();
        if (currentTool != null) {
            currentTool.activate();
            createGraphics();
        }
    }

    public void flipVertical() {
        if (currentTool != null) {
            currentTool.deactivate();
        }
        this.saveUndo();
        BufferedImage img = region.getDrawImage(index);
        BufferedImage img2 = ImageOperations.flipVertical(img);
        region.setDrawImage(index, img2);
        region.setDrawImageChanged(index, true);
        revalidate();
        repaint();
        if (currentTool != null) {
            currentTool.activate();
            createGraphics();
        }
    }

    public void saveAsAnimatedGif() {
        AnimateGifEncoder anim = new AnimateGifEncoder();

        int delay = 1000;
        String strDelay = JOptionPane.showInputDialog(Language.translate("Enter delay between frames (in ms):"), "" + delay);

        fc.setApproveButtonText(Language.translate("Save Animated GIF"));
        fc.setDialogTitle(Language.translate("Save Frames as Animated GIF"));
        //In response to a button click:
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (strDelay != null) {
                try {
                    delay = Integer.parseInt(strDelay);
                } catch (Throwable e) {
                }
            }

            // anim.setTransparent(Color.WHITE);
            String strPath = file.getPath();
            if (!strPath.toLowerCase().endsWith(".gif")) {
                strPath += ".gif";
            }
            anim.start(strPath);
            anim.setDelay(delay);
            anim.setRepeat(0);
            anim.setTransparent(Color.PINK);
            for (int i = 0; i <= region.additionalImageFile.size(); i++) {
                BufferedImage img = region.getDrawImage(i);

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
        /*
         * this.region.getDrawImagePath(index); new
         * ImageHistoryDialog(this.editorFrame,
         * this.region.getDrawImageFileName(index).replace(".png", ""),
         * "kdsjfl32", this);
        requestFocus();
         */
    }

    public void resizeImage() {
        int w = region.getWidth();
        int h = region.getHeight();

        TutorialPanel.addLine("cmd", "Resize the active region image (" + w + "x" + h + ")");

        new ResizeDialog(SketchletEditor.editorFrame, Language.translate("Resize Image"), this, w, h);

        /*
         * BufferedImage img = Workspace.createCompatibleImageCopy(w, h);
         * Graphics2D g2 = img.createGraphics();
         * g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
         * RenderingHints.VALUE_ANTIALIAS_ON);
         *
         * g2.drawImage(action.getDrawImage(index), 0, 0, w, h, null);
         * action.setDrawImage(index, img); action.setDrawImageChanged(index,
         * true);
         *
         * this.save();
         *
         * revalidateAll(); repaintAll();
         *
         * setStroke();
         *
         * g2.dispose();
         */
    }

    public void revalidateAll() {
        drawingPanels.revalidate();
        this.revalidate();
    }

    public void repaintAll() {
        this.repaint();
    }

    public void resizeRegion() {
        int w = region.getDrawImage(index).getWidth();
        int h = region.getDrawImage(index).getHeight();

        region.x2 = region.x1 + w;
        region.y2 = region.y1 + h;

        TutorialPanel.addLine("cmd", "Set the region size equal to the region image size (" + w + "x" + h + ")");

        SketchletEditor.editorPanel.repaint();
    }

    public void createNewImage() {
        // FreeHandSimple drawingPanel = new FreeHandSimple(this.mainDrawingPanel, action, parent, drawingPanels.getTabCount());
        // JScrollPane scrollPane = new JScrollPane(drawingPanel);
        //scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        //scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        currentTool.deactivate();

        region.additionalImageFile.add("");
        region.additionalDrawImages.add(null);
        region.additionalImageChanged.add(new Boolean(false));

        drawingPanels.add("" + (drawingPanels.getTabCount() + 1), new JPanel());
        drawingPanels.setSelectedIndex(drawingPanels.getTabCount() - 1);

        region.clearImage(index);

        createGraphics();
        currentTool.activate();
    }

    public void clearImage() {
        saveUndo();
        region.clearImage11(this.drawingPanels.getSelectedIndex());
        region.saveImage();
        repaint();
    }

    public void pasteImage() {
        saveUndo();
        fromClipboard(index);
        region.setDrawImageChanged(index, true);
        region.saveImage();
        revalidate();
        this.createGraphics();
        repaint();
    }

    public void fromClipboard(int index) {
        saveImageUndo();

        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = clip.getContents(null);
        RenderedImage img = null;

        if (transferable.isDataFlavorSupported(new DataFlavor("image/x-java-image; class=java.awt.Image", "Image"))) {
            try {
                img = (RenderedImage) transferable.getTransferData(new DataFlavor("image/x-java-image; class=java.awt.Image", "Image"));
                int w = Math.max(region.getDrawImage(index).getWidth(), img.getWidth());
                int h = Math.max(region.getDrawImage(index).getHeight(), img.getHeight());
                BufferedImage oldImage = region.getDrawImage(index);
                BufferedImage pImg = Workspace.createCompatibleImage(img.getWidth(), img.getHeight());

                region.setDrawImage(index, Workspace.createCompatibleImage(w, h));

                Graphics2D g2 = region.getDrawImage(index).createGraphics();
                g2.drawImage(oldImage, 0, 0, null);
                g2.dispose();
                g2 = pImg.createGraphics();
                g2.drawRenderedImage(img, null);

                this.setTool(this.selectTool, null);
                this.selectTool.setClip(pImg, 0, 0);

                region.setDrawImageChanged(index, true);
                region.saveImage();

                g2.dispose();
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        } else {
            log.error("Object on clipboard is not an image!");
        }

        SketchletEditor.editorPanel.repaint();
    }

    public Dimension getPreferredSize() {
        if (this.region.getDrawImage(index) == null) {
            return new Dimension((int) (220 * scale), (int) (220 * scale));
        } else {
            return new Dimension((int) (region.getDrawImage(index).getWidth() * scale), (int) (region.getDrawImage(index).getHeight() * scale));
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

        freeHand.scrollPane = new JScrollPane(freeHand);
        freeHand.scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        freeHand.scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        freeHand.drawingPanels.addTab("1", freeHand.scrollPane);

        freeHandFrame.getContentPane().add(freeHand.drawingPanels, BorderLayout.CENTER);
        // editorFrame.getContentPane().add(editorPanel.getControlPanel(), BorderLayout.WEST);
        freeHandFrame.pack();
        freeHandFrame.setVisible(true);
    }

    public void makeTransparent() {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.selectTransparentColor = true;
    }

    public void makeTransparent(int color) {
        if (region.getDrawImage(index) != null) {
            for (int i = 0; i < region.getDrawImage(index).getWidth(); i++) {
                for (int j = 0; j < region.getDrawImage(index).getHeight(); j++) {
                    if (region.getDrawImage(index).getRGB(i, j) == color) {
                        region.getDrawImage(index).setRGB(i, j, new Color(0, 0, 0, 0).getRGB());
                    }
                }
            }

            this.repaint();

            this.region.setDrawImageChanged(index, true);
            this.region.saveImage();
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

        TutorialPanel.addLine("cmd", "Import the image file into the active region (" + options[n].toString().toLowerCase() + ")", "import.gif", c);

        SketchletEditor.editorPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (file.getPath().toLowerCase().endsWith(".gif")) {
            fromGifFile(file);
        } else if (file.getPath().toLowerCase().endsWith(".pdf")) {
            fromPdfFile(file);
        } else {
            try {
                saveUndo();
                BufferedImage newImage = Workspace.createCompatibleImageCopy(ImageIO.read(file));
                this.region.setDrawImage(index, newImage);

                this.region.setSize(newImage.getWidth(), newImage.getHeight());

                this.revalidateAll();
                this.repaint();
                this.region.setDrawImageChanged(index, true);
                region.saveImage();

            } catch (Throwable e) {
                log.error(e);
            }
        }
        SketchletEditor.editorPanel.repaint();
        SketchletEditor.editorPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
                this.region.setDrawImage(index, newImage);
                this.region.setDrawImageChanged(index, true);
                this.region.setSize(img.getWidth(), img.getHeight());
            }
            region.saveImage();

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
            PDFFile pdffile = new PDFFile(buf);

            int n = pdffile.getNumPages();

            for (int i = 0; i < n; i++) {
                PDFPage page = pdffile.getPage(i + 1);

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
                    w = h;
                    h = t;
                }
                BufferedImage img = Workspace.createCompatibleImageCopy(image);
                if (i > 0) {
                    this.createNewImage();
                }
                this.region.setDrawImage(index, img);
                this.region.setDrawImageChanged(index, true);
                this.region.setSize(img.getWidth(), img.getHeight());
            }
            region.saveImage();

            this.revalidateAll();
            this.repaint();

        } catch (Throwable e) {
            log.error(e);
        }
    }

    /*
     * public void refreshSlider() { this.slider.setValue((int)
     * this.strokeValue);
    }
     */
    public static void main(String[] args) {
        createAndShowGui();
    }

    public void restoreImage(File imageFile, Component c) {
        fromFile(imageFile, c);
    }

    public void previewImage(File imageFile) {
        try {
            this.region.setDrawImage(index, ImageIO.read(imageFile));
            this.repaint();
        } catch (Throwable e) {
            log.error(e);
        }
    }

    public BufferedImage getImage() {
        return region.getDrawImage(index);
    }

    public void saveImageUndo() {
        this.saveUndo();
    }

    public void setImageUpdated(boolean bUpdated) {
        region.setDrawImageChanged(index, bUpdated);
    }

    public void setImage(BufferedImage img) {
        saveUndo();
        region.setDrawImage(index, img);
        region.setDrawImageChanged(index, true);
        region.saveImage();
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
        if (region != null && region.getDrawImage(index) != null) {
            graphics2D = region.getDrawImage(index).createGraphics();
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
        this.color = c;
    }

    public Color getColor() {
        return SketchletEditor.editorPanel.color;
    }

    public Stroke getStroke() {
        return SketchletEditor.editorPanel.stroke;
    }

    public int getStrokeWidth() {
        return (SketchletEditor.editorPanel.colorToolbar == null || SketchletEditor.editorPanel.colorToolbar.slider == null) ? 0 : SketchletEditor.editorPanel.colorToolbar.slider.getValue();
    }

    public int getImageWidth() {
        return region.getDrawImage(index).getWidth();
    }

    public int getImageHeight() {
        return region.getDrawImage(index).getHeight();
    }

    public BufferedImage extractImage(int x1, int y1, int w, int sh) {
        return SketchletEditor.extractImage(x1, y1, w, sh, this);
    }

    public BufferedImage extractImage(Polygon polygon) {
        return SketchletEditor.extractImage(polygon, this);
    }

    public void setTool(Tool tool, Component c) {
        if (this.currentTool != null) {
            this.currentTool.deactivate();
        }
        this.currentTool = tool;
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
        return SketchletEditor.editorPanel.watering;
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
            g2.drawImage(region.getDrawImage(index), 0, 0, w, h, null);
            g2.dispose();

            region.setDrawImage(index, img);

            region.setDrawImageChanged(index, true);
            region.saveImage();
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
            g2.drawImage(region.getDrawImage(index), 0, 0, null);
            g2.dispose();

            region.setDrawImage(index, img);

            region.setDrawImageChanged(index, true);
            region.saveImage();
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
                    scale = zoom / 100.0;
                    revalidateAll();
                    repaintAll();
                    setStroke();
                } catch (Throwable e) {
                }
            }
        });

        zoomBox.setSelectedItem("100%");
        /*
         * try { String strZoom =
         * FileUtils.getFileText(WorkspaceUtils.getCurrentProjectSkecthletsDir()
         * + "last_zoom.txt").trim(); if (!strZoom.equals("")) {
         * zoomBox.setSelectedItem(strZoom); } else {
         * zoomBox.setSelectedItem("100%"); } } catch (Throwable e) {
         * zoomBox.setSelectedItem("100%");
        }
         */


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
        return this.scrollPane;
    }

    public String getName() {
        return "the region image area";
    }
}

class DrawingListenerSimple extends MouseInputAdapter {

    ActiveRegionImageEditor freeHandSimple;
    Point start;

    public DrawingListenerSimple(ActiveRegionImageEditor fh) {
        this.freeHandSimple = fh;
    }

    public void mousePressed(MouseEvent e) {
        BufferedImage img = freeHandSimple.region.getDrawImage(freeHandSimple.index);
        if (img == null) {
            freeHandSimple.region.initImage(freeHandSimple.index);
            img = freeHandSimple.region.getDrawImage(freeHandSimple.index);
        }

        if (img == null) {
            return;
        }
        int w = img.getWidth();
        int h = img.getHeight();

        if (w == 1 && h == 1) {
            Dimension d = freeHandSimple.getDrawImageDimension();
            w = d.width;
            h = d.height;
            freeHandSimple.region.setDrawImage(freeHandSimple.index, Workspace.createCompatibleImage(w, h, img));
        }

        if (freeHandSimple.selectTransparentColor) {
            freeHandSimple.makeTransparent(freeHandSimple.getImage().getRGB((int) (e.getPoint().x / freeHandSimple.scale), (int) (e.getPoint().y / freeHandSimple.scale)));
            start = null;
            freeHandSimple.selectTransparentColor = false;
            freeHandSimple.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        } else {
            // freeHandSimple.saveUndo();
            // start = e.getPoint();
            freeHandSimple.currentTool.mousePressed((int) (e.getX() / freeHandSimple.scale), (int) (e.getY() / freeHandSimple.scale), e.getModifiers());
            freeHandSimple.repaint();
        }
    }

    public void mouseDragged(MouseEvent e) {
        freeHandSimple.currentTool.mouseDragged((int) (e.getX() / freeHandSimple.scale), (int) (e.getY() / freeHandSimple.scale), e.getModifiers());
        freeHandSimple.repaint();
        //if (start != null) {
//            Point p = e.getPoint();
//            editorPanel.draw(start, p);
//            start = p;
        //}
    }

    public void mouseReleased(MouseEvent e) {
        freeHandSimple.currentTool.mouseReleased((int) (e.getX() / freeHandSimple.scale), (int) (e.getY() / freeHandSimple.scale), e.getModifiers());

        RefreshTime.update();
        freeHandSimple.repaint();
        SketchletEditor.editorPanel.repaint();
        freeHandSimple.requestFocus();
    }

    public void mouseMoved(MouseEvent e) {
        freeHandSimple.setCursor(freeHandSimple.currentTool.getCursor());
        freeHandSimple.currentTool.mouseMoved((int) (e.getX() / freeHandSimple.scale), (int) (e.getY() / freeHandSimple.scale), e.getModifiers());
    }
}
