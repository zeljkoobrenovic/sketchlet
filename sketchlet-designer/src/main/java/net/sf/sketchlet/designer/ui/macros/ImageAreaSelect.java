/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui.macros;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.programming.screenscripts.AWTRobotUtil;
import net.sf.sketchlet.designer.ui.UIUtils;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

/**
 * Grabs a frame from a Webcam, overlays the current date and time, and saves the frame as a PNG to c:\webcam.png
 *
 * @author David
 * @version 1.0, 16/01/2004
 */
public class ImageAreaSelect extends JPanel {
    private static final Logger log = Logger.getLogger(CommandPanel.class);

    private BufferedImage img;
    private Robot robot;
    static Toolkit toolkit = Toolkit.getDefaultToolkit();
    int width = (int) toolkit.getScreenSize().getWidth();
    int height = (int) toolkit.getScreenSize().getHeight();
    int x = 0;
    int y = 0;
    int w = width;
    int h = height;
    int x1 = x, y1 = y, x2 = x1 + w, y2 = y1 + h;
    public static String strArea = "";
    public static String strVariable = "image-path";
    public static String strFile = "";
    public static boolean bSaved = false;
    boolean extraControls = false;
    double scale = 1.0;
    public static boolean active = false;

    public ImageAreaSelect(BufferedImage img, boolean extraControls) {
        this.img = img;
        this.extraControls = extraControls;
        ImageAreaSelectMotionListener listener = new ImageAreaSelectMotionListener();
        this.addMouseMotionListener(listener);
        this.addMouseListener(listener);
    }

    class ImageAreaSelectMotionListener extends MouseAdapter implements MouseMotionListener {

        final static int SELECT = 0;
        final static int DRAG = 1;
        final static int DRAG_1 = 2;
        final static int DRAG_2 = 3;
        final static int DRAG_3 = 4;
        final static int DRAG_4 = 5;
        int mode = SELECT;
        int px;
        int py;

        public void mousePressed(MouseEvent e) {
            px = e.getX();
            py = e.getY();

            AffineTransform affine = new AffineTransform();
            affine.scale(scale, scale);
            try {
                Point2D np = affine.inverseTransform(e.getPoint(), null);
                px = (int) np.getX();
                py = (int) np.getY();
            } catch (Exception ep) {
            }

            int w = 9;
            int w2 = w / 2;

            if (new Rectangle(x1 - w2, y1 - w2, w, w).contains(px, py)) {
                mode = DRAG_1;
            } else if (new Rectangle(x1 - w2, y2 - w2, w, w).contains(px, py)) {
                mode = DRAG_2;
            } else if (new Rectangle(x2 - w2, y1 - w2, w, w).contains(px, py)) {
                mode = DRAG_3;
            } else if (new Rectangle(x2 - w2, y2 - w2, w, w).contains(px, py)) {
                mode = DRAG_4;
            } else if (px >= x1 && px <= x2 && py >= y1 && py <= y2) {
                mode = DRAG;
            } else {
                mode = SELECT;
                x1 = px;
                y1 = py;
                x2 = x1;
                y2 = y1;
            }

            repaint();
        }

        public void mouseReleased(MouseEvent e) {
            int npx = e.getX();
            int npy = e.getY();

            AffineTransform affine = new AffineTransform();
            affine.scale(scale, scale);
            try {
                Point2D np = affine.inverseTransform(e.getPoint(), null);
                npx = (int) np.getX();
                npy = (int) np.getY();
            } catch (Exception ep) {
            }
            if (mode == SELECT) {
                x2 = Math.min(width, Math.max(0, npx));
                y2 = Math.min(height, Math.max(0, npy));
            }

            if (x1 > x2) {
                int temp = x1;
                x1 = x2;
                x2 = temp;
            }

            if (y1 > y2) {
                int temp = y1;
                y1 = y2;
                y2 = temp;
            }

            x = x1;
            y = y1;
            w = x2 - x1;
            h = y2 - y1;

            repaint();
        }

        public void mouseDragged(MouseEvent e) {
            int npx = e.getX();
            int npy = e.getY();

            AffineTransform affine = new AffineTransform();
            affine.scale(scale, scale);
            try {
                Point2D np = affine.inverseTransform(e.getPoint(), null);
                npx = (int) np.getX();
                npy = (int) np.getY();
            } catch (Exception ep) {
            }
            if (mode == SELECT) {
                x2 = Math.min(width, Math.max(0, npx));
                y2 = Math.min(height, Math.max(0, npy));
            } else {
                int dx = npx - px;
                int dy = npy - py;

                if (mode == DRAG) {
                    x1 += dx;
                    x2 += dx;
                    y1 += dy;
                    y2 += dy;
                } else if (mode == DRAG_1) {
                    x1 += dx;
                    y1 += dy;
                } else if (mode == DRAG_2) {
                    x1 += dx;
                    y2 += dy;
                } else if (mode == DRAG_3) {
                    x2 += dx;
                    y1 += dy;
                } else if (mode == DRAG_4) {
                    x2 += dx;
                    y2 += dy;
                }

                px = npx;
                py = npy;
            }

            repaint();
        }

        public void mouseMoved(MouseEvent e) {
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(width + 1, height + 1);
    }

    public void paint(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;
        g2.scale(scale, scale);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, Math.max(2000, width), Math.max(2000, height));
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        if (img != null) {
            g2.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null, null);
        }

        g2.setColor(new Color(0, 0, 255, 100));
        g2.fillRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));

        g2.setColor(Color.BLUE);
        g2.drawRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));

        int w = 9;
        int w2 = w / 2;

        g2.fillRect(x1 - w2, y1 - w2, w, w);
        g2.fillRect(x2 - w2, y1 - w2, w, w);
        g2.fillRect(x1 - w2, y2 - w2, w, w);
        g2.fillRect(x2 - w2, y2 - w2, w, w);
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    public static JDialog screenSelecterFrame = null;

    public static void createAndShowGUI(JFrame frame, BufferedImage img, int x, int y, int w, int h, boolean extraControls) {

        //Create and set up the window.
        screenSelecterFrame = new JDialog(frame, true);
        screenSelecterFrame.setUndecorated(false);
        screenSelecterFrame.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
        // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        ImageAreaSelect selecter = new ImageAreaSelect(img, extraControls);
        selecter.x = x;
        selecter.y = y;
        selecter.w = w;
        selecter.h = h;
        selecter.x1 = x;
        selecter.y1 = y;
        selecter.x2 = selecter.x1 + w;
        selecter.y2 = selecter.y1 + h;
        selecter.setOpaque(true); //content panes must be opaque
        screenSelecterFrame.getContentPane().add(new CommandPanel(screenSelecterFrame, selecter), BorderLayout.NORTH);
        screenSelecterFrame.getContentPane().add(new JScrollPane(selecter), BorderLayout.CENTER);

        //Display the window.
        //screenSelecterFrame.pack();
        int screenW = (int) (Toolkit.getDefaultToolkit().getScreenSize().width * 0.9);
        int screenH = (int) (Toolkit.getDefaultToolkit().getScreenSize().height * 0.9);
        screenSelecterFrame.setSize(screenW, screenH);
        active = true;
        screenSelecterFrame.setLocationRelativeTo(SketchletEditor.editorFrame);
        screenSelecterFrame.setVisible(true);
    }

    public static void main(String[] args) {

        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                createAndShowGUI(null, Workspace.createCompatibleImage(100, 100), 0, 0, 100, 100, false);
            }
        });
    }

    static class CommandPanel extends JPanel {

        JButton saveButton;
        JButton selectAllButton;
        JButton printScreenButton;
        JButton pasteImageButton;
        JButton cancelButton;
        JButton zoomInButton;
        JButton zoomOutButton;
        JComboBox variableCombo = new JComboBox();
        JTextField fileField = new JTextField(15);
        JButton fileButton;
        JDialog frame;
        ImageAreaSelect selecter;

        public CommandPanel(JDialog frame, final ImageAreaSelect selecter) {
            this.selecter = selecter;
            this.frame = frame;

            setLayout(new BorderLayout());

            final CommandPanel parent = this;

            saveButton = new JButton(Language.translate("Select"), Workspace.createImageIcon("resources/ok.png"));
            saveButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    saveImage(false);
                }
            });

            selectAllButton = new JButton(Language.translate("Select Whole Screen"), Workspace.createImageIcon("resources/image-window.gif"));
            selectAllButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    selectAllImage();
                }
            });

            printScreenButton = new JButton(Language.translate("Print Screen(s)"), Workspace.createImageIcon("resources/screen.png"));
            printScreenButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    final Point p = screenSelecterFrame.getLocation();
                    screenSelecterFrame.setLocation(-5000, -5000);
                    new Thread(new Runnable() {

                        public void run() {
                            try {
                                Thread.sleep(100);
                                AWTRobotUtil.pressKey(KeyEvent.VK_PRINTSCREEN);
                                Thread.sleep(100);
                                AWTRobotUtil.releaseKey(KeyEvent.VK_PRINTSCREEN);
                                Thread.sleep(100);
                                selecter.pasteImage();
                                screenSelecterFrame.setLocation(p);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            });

            pasteImageButton = new JButton(Language.translate("Paste Image"), Workspace.createImageIcon("resources/edit-paste.png"));
            pasteImageButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    selecter.pasteImage();
                }
            });

            cancelButton = new JButton(Language.translate("Cancel"), Workspace.createImageIcon("resources/edit-delete.png"));
            cancelButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    cancel();
                }
            });

            zoomInButton = new JButton(Workspace.createImageIcon("resources/zoomin.gif"));
            zoomInButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    selecter.scale += 0.1;
                    selecter.repaint();
                }
            });

            zoomOutButton = new JButton(Workspace.createImageIcon("resources/zoomout.gif"));
            zoomOutButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    selecter.scale -= 0.1;
                    if (selecter.scale < 0.1) {
                        selecter.scale = 0.1;
                    }
                    selecter.repaint();
                }
            });

            JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout(FlowLayout.LEFT));

            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout(FlowLayout.LEFT));

            panel1.add(saveButton);
            panel1.add(cancelButton);
            panel1.add(new JLabel("      "));
            panel1.add(selectAllButton);
            panel1.add(printScreenButton);
            panel1.add(pasteImageButton);
            panel1.add(new JLabel("      "));
            panel1.add(zoomInButton);
            panel1.add(zoomOutButton);
            if (selecter.extraControls) {
                UIUtils.populateVariablesCombo(variableCombo, false);
                variableCombo.setSelectedItem(selecter.strVariable);
                panel1.add(new JLabel(Language.translate("   Update variable")));
                panel1.add(variableCombo);
            }
            this.add(panel1, BorderLayout.CENTER);
            this.add(new JLabel(Language.translate("   Select the area of the screen you want to save, or click \"Select Whole Screen\" if you want to save the whole screen.")), BorderLayout.SOUTH);
        }

        public void saveImage(boolean bClose) {

            if (this.selecter.extraControls) {
                selecter.strVariable = this.variableCombo.getSelectedItem().toString();
                selecter.strFile = this.fileField.getText();

                if (selecter.strVariable == null || selecter.strVariable.length() == 0) {
                    JOptionPane.showMessageDialog(frame, Language.translate("You have to specify the variable!"));
                    return;
                }
            }
            int x = Math.min(this.selecter.x1, this.selecter.x2);
            int y = Math.min(this.selecter.y1, this.selecter.y2);
            int w = Math.abs(this.selecter.x2 - this.selecter.x1);
            int h = Math.abs(this.selecter.y2 - this.selecter.y1);

            selecter.strArea = x + " " + y + " " + w + " " + h;

            selecter.bSaved = true;

            active = false;
            this.frame.setVisible(false);
        }

        public void selectAllImage() {
            this.selecter.x1 = 0;
            this.selecter.y1 = 0;
            this.selecter.x2 = (int) toolkit.getScreenSize().getWidth();
            this.selecter.y2 = (int) toolkit.getScreenSize().getHeight();
            selecter.x = selecter.x1;
            selecter.y = selecter.y1;
            selecter.w = selecter.x2 - selecter.x1;
            selecter.h = selecter.y2 - selecter.y1;
            this.selecter.repaint();
        }

        public void cancel() {
            selecter.bSaved = false;
            active = false;
            this.frame.setVisible(false);
        }
    }

    public void pasteImage() {
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = clip.getContents(null);
        RenderedImage imgPaste = null;

        if (transferable.isDataFlavorSupported(new DataFlavor("image/x-java-image; class=java.awt.Image", "Image"))) {
            try {
                imgPaste = (RenderedImage) transferable.getTransferData(new DataFlavor("image/x-java-image; class=java.awt.Image", "Image"));
                width = imgPaste.getWidth();
                height = imgPaste.getHeight();
                img = Workspace.createCompatibleImage(width, height);
                Graphics2D g2 = img.createGraphics();
                g2.drawRenderedImage(imgPaste, null);
                g2.dispose();
                revalidate();
                repaint();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            log.info("Not an image!");
        }
    }
}
