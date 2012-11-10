/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui.desktop;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.GlobalProperties;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.SketchletEditorFrame;
import net.sf.sketchlet.designer.editor.tool.stroke.WobbleStroke;
import net.sf.sketchlet.designer.help.TutorialPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Hashtable;

public class DesktopPanel extends JPanel {

    static DesktopPanel diagram;
    public static Page selectedPage = null;
    public static int selectedIndex = -1;
    static int posX = 0;
    static int posY = 0;
    // Object[][] sketchInfo;
    public static final int MAX_ICON_WIDTH = 100;
    public int iconWidth = MAX_ICON_WIDTH;
    public static final int MANUAL = 0;
    public static final int AUTO = 1;
    int type = MANUAL;
    Component component;
    boolean singleClickChange = false;
    JFrame frame;
    int panelWidth;

    public DesktopPanel(int type, Component component) {
        frame = Workspace.mainFrame;
        this.component = component;
        this.type = type;
        panelWidth = component.getWidth();
        // sketchInfo = Sketches.getSketchInfoFromDir(ICON_WIDTH, 600);
        MouseAdapter l;
        if (type == MANUAL) {
            l = new DesktopPanelMouseListener();
        } else {
            l = new DesktopPanelAutoMouseListener();

        }
        this.addMouseListener(l);
        this.addMouseMotionListener(l);
        this.setTransferHandler(new DesktopPanelTransferHandler(this));
        loadThumbnails();
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    public void ensureVisible(Page s) {
        Rectangle r = this.getSketchRect(s);
        if (r != null) {
            this.scrollRectToVisible(r);
        }
    }

    public void refresh() {
        // sketchInfo = Sketches.getSketchInfoFromDir(ICON_WIDTH, 600);
        loadThumbnails();
        try {
            selectedIndex = Integer.parseInt(GlobalProperties.get("last-sketch-index"));
            if (selectedIndex < SketchletEditor.pages.pages.size()) {
                selectedPage = SketchletEditor.pages.pages.elementAt(selectedIndex);
            } else {
                selectedPage = null;
            }
        } catch (Throwable e) {
            //e.printStackTrace();
            selectedPage = null;
        }
        repaint();
    }

    public void save() {
        for (Page s : touchedSketches.values()) {
            s.save(false);
        }
    }

    public void enableSingleClickChange(boolean enable) {
        this.singleClickChange = enable;
    }

    public Dimension getPreferredSize() {
        return new Dimension(5000, 5000);
    }

    static Hashtable<Page, Page> touchedSketches;
    static BufferedImage images[];

    public void loadThumbnails() {
        if (SketchletEditor.pages == null) {
            return;
        }
        if (images != null) {
            for (int i = 0; i < images.length; i++) {
                if (images[i] != null) {
                    images[i].flush();
                    images[i] = null;
                }
            }
        }
        images = new BufferedImage[SketchletEditor.pages.pages.size()];
        for (int i = 0; i < SketchletEditor.pages.pages.size(); i++) {
            Page s = SketchletEditor.pages.pages.elementAt(i);
            if (s != null) {
                try {
                    images[i] = Workspace.createCompatibleImage(iconWidth, iconWidth, images[i]);
                    File thumbFile = new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "Sketch_" + s.getId() + "_thumbnail.png");
                    if (thumbFile.exists()) {
                        BufferedImage image = ImageIO.read(thumbFile);
                        Graphics2D g2i = images[i].createGraphics();
                        g2i.drawImage(image, 0, 0, null, null);
                        g2i.dispose();
                        images[i].flush();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        repaint();
    }

    public void paintComponent(Graphics g) {
        try {
            super.paintComponent(g);
            if (SketchletEditor.pages == null) {
                return;
            }

            if (images == null || images.length != SketchletEditor.pages.pages.size()) {
                this.loadThumbnails();
            }

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            touchedSketches = new Hashtable<Page, Page>();

            final Hashtable hash = new Hashtable();

            int c = 20;

            Font font = g2.getFont();

            int panelWidth = component.getWidth();
            iconWidth = Math.min(MAX_ICON_WIDTH, panelWidth - 35);

            if (type == MANUAL) {
                g2.setStroke(new WobbleStroke(2, 2, 1));
                g2.setColor(Color.GRAY);
                for (int i = 0; i < SketchletEditor.pages.pages.size(); i++) {
                    Page s = SketchletEditor.pages.pages.elementAt(i);
                    for (int j = 0; j < i; j++) {
                        Page s2 = SketchletEditor.pages.pages.elementAt(j);
                        boolean connected1 = s.isConnectedTo(s2);
                        boolean connected2 = s2.isConnectedTo(s);
                        if (connected1 || connected2) {
                            int _x1 = (int) s.stateDiagramX + iconWidth / 2;
                            int _y1 = (int) s.stateDiagramY + iconWidth / 2;
                            int _x2 = (int) s2.stateDiagramX + iconWidth / 2;
                            int _y2 = (int) s2.stateDiagramY + iconWidth / 2;
                            g2.drawLine(_x1, _y1, _x2, _y2);
                        }
                    }
                }
            }

            for (int i = 0; i < SketchletEditor.pages.pages.size(); i++) {
                Page s = SketchletEditor.pages.pages.elementAt(i);
                if (s == null) {
                    continue;
                }
                double x = s.stateDiagramX;
                double y = s.stateDiagramY;

                if (type == MANUAL) {
                    x = s.stateDiagramX;
                    y = s.stateDiagramY;

                    if (Double.isNaN(x) || x <= 0) {
                        x = 20 + (int) (150 * Math.random());
                        y += (int) (150 * Math.random());
                    }
                    s.stateDiagramX = x;
                    s.stateDiagramY = y;
                    /*
                    
                    if (x == 20) {
                    y = c;
                    c += 140;
                    }
                    
                    s.stateDiagramX = x;
                    s.stateDiagramY = y;
                     */
                } else {
                    int row_space = Math.max(1, panelWidth / (iconWidth + 15));

                    int row = i / row_space;
                    int col = i % row_space;

                    y = 15 + (row * (iconWidth + 15));
                    x = 15 + (col * (iconWidth + 15));
                }

                BufferedImage img = images[i];

                int w = iconWidth;
                int h = iconWidth;
                g2.setColor(new Color(100, 100, 100, 80));
                g2.fillRect((int) x + 2, (int) y + 2, w, h);
                if (selectedPage == s) {
                    g2.setFont(font.deriveFont(Font.BOLD));
                    g2.setColor(new Color(100, 100, 100, 80));
                    g2.fillRect((int) x + 8, (int) y + 8, w, h);
                    g2.setStroke(new WobbleStroke(2, 2, 1));
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setFont(font);
                    g2.setStroke(new WobbleStroke(1, 1, 1));
                    g2.setColor(new Color(250, 250, 250));
                }
                g2.fillRect((int) x, (int) y, w, h);
                g2.setColor(Color.GRAY);
                g2.drawRect((int) x, (int) y, w, h);
                g2.setColor(Color.DARK_GRAY);
                g2.drawString(s.title, (int) x, (int) y - 3);

                if (img != null) {
                    g2.drawImage(img, (int) x, (int) y, iconWidth, iconWidth, null);
                }

                hash.put(s.title, s);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public Rectangle getSketchRect(Page page) {
        for (int i = 0; i < SketchletEditor.pages.pages.size(); i++) {
            Page s = SketchletEditor.pages.pages.elementAt(i);
            double x = s.stateDiagramX;
            double y = s.stateDiagramY;

            if (s == page) {
                if (type == MANUAL) {
                    x = s.stateDiagramX;
                    y = s.stateDiagramY;

                    if (Double.isNaN(x) || x <= 0) {
                        x = 20 + (int) (150 * Math.random());
                        y += (int) (150 * Math.random());
                    }

                    return new Rectangle((int) x, (int) y - 15, iconWidth, iconWidth);
                } else {
                    panelWidth = component.getWidth();
                    int row_space = Math.max(1, panelWidth / (iconWidth + 15));

                    int row = i / row_space;
                    int col = i % row_space;

                    y = 15 + (row * (iconWidth + 15));
                    x = 15 + (col * (iconWidth + 15));
                    return new Rectangle((int) x, (int) y - 15, iconWidth, iconWidth);
                }
            }
        }
        return null;
    }

    class DesktopPanelMouseListener extends MouseInputAdapter {

        Point start;

        public DesktopPanelMouseListener() {
        }

        public void mousePressed(MouseEvent e) {
            start = e.getPoint();
            int mx = e.getPoint().x;
            int my = e.getPoint().y;
            selectedPage = null;
            selectedIndex = -1;
            int c = 20;

            for (int i = SketchletEditor.pages.pages.size() - 1; i >= 0; i--) {
                Page s = SketchletEditor.pages.pages.elementAt(i);
                double x = s.stateDiagramX;
                double y = s.stateDiagramY;
                if (Double.isNaN(x) || x <= 0) {
                    x = 20;
                }

                if (x == 20) {
                    y = c;
                    c += 140;
                }

                s.stateDiagramX = x;
                s.stateDiagramY = y;
                Rectangle r = new Rectangle((int) x, (int) y, iconWidth, iconWidth);

                if (r.contains(mx, my)) {
                    selectedPage = s;
                    selectedIndex = i;
                    break;
                }
            }
            if (selectedPage != null && ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)) {
                showPopupMenu(selectedPage, selectedIndex, e.getX(), e.getY());
            }

            repaint();
        }

        public void mouseDragged(MouseEvent e) {
            if (start != null && selectedPage != null) {
                Point p = e.getPoint();
                double dx = p.getX() - start.getX();
                double dy = p.getY() - start.getY();
                if (selectedPage.stateDiagramX + dx > 0 && selectedPage.stateDiagramY + dy > 0) {
                    selectedPage.stateDiagramX += dx;
                    selectedPage.stateDiagramY += dy;
                }
                start = p;
                repaint();
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (selectedPage != null) {
                selectedPage.save(false);
            }
        }

        public void mouseClicked(MouseEvent e) {
            if (selectedPage != null && e.getClickCount() >= 2) {
                Workspace.mainPanel.openSketches(false);
            }
        }
    }

    class DesktopPanelAutoMouseListener extends MouseInputAdapter {

        Point start;

        public void mousePressed(MouseEvent e) {
            if (SketchletEditor.pages == null) {
                return;
            }
            start = e.getPoint();
            int mx = e.getPoint().x;
            int my = e.getPoint().y;
            selectedPage = null;
            selectedIndex = -1;

            panelWidth = component.getWidth();
            for (int i = SketchletEditor.pages.pages.size() - 1; i >= 0; i--) {
                Page s = SketchletEditor.pages.pages.elementAt(i);
                int row_space = Math.max(1, panelWidth / (iconWidth + 15));

                int row = i / row_space;
                int col = i % row_space;

                double y = 15 + (row * (iconWidth + 15));
                double x = 15 + (col * (iconWidth + 15));
                Rectangle r = new Rectangle((int) x, (int) y, iconWidth, iconWidth);

                if (r.contains(mx, my)) {
                    selectedPage = s;
                    selectedIndex = i;
                    break;
                }
            }

            if (selectedPage != null && ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)) {
                showPopupMenu(selectedPage, selectedIndex, e.getX(), e.getY());
            }

            repaint();
        }

        public void mouseDragged(MouseEvent e) {
            TransferHandler handler = getTransferHandler();
            handler.exportAsDrag(DesktopPanel.this, e, TransferHandler.COPY);
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseClicked(MouseEvent e) {
            if (selectedPage != null && singleClickChange) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                SketchletEditorFrame.createAndShowGui(selectedIndex, false);
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            } else if (selectedPage != null && e.getClickCount() >= 2) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                SketchletEditorFrame.createAndShowGui(selectedIndex, false);
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

    public void showPopupMenu(final Page s, final int selectedIndex, int x, int y) {
        final JPopupMenu popupMenu = new JPopupMenu();
        TutorialPanel.prepare(popupMenu, true);

        JMenuItem sketchMenuItem = new JMenuItem(Language.translate("Open..."));
        sketchMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                SketchletEditorFrame.createAndShowGui(selectedIndex, false);
            }
        });
        popupMenu.add(sketchMenuItem);
        JMenuItem renameMenuItem = new JMenuItem(Language.translate("Rename..."));
        renameMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (selectedPage != null) {
                    String newName = JOptionPane.showInputDialog(component, Language.translate("New name"), selectedPage.title);
                    if (newName != null) {
                        String oldName = selectedPage.title;
                        selectedPage.title = newName;
                        if (SketchletEditor.editorFrame != null) {
                            SketchletEditor.editorFrame.setTitle(newName);
                        }
                        selectedPage.title = newName;
                        selectedPage.save(false);
                        SketchletEditor.pages.replaceReferencesSketches(oldName, newName);

                        repaint();
                    }
                }
            }
        });
        popupMenu.add(renameMenuItem);
        sketchMenuItem = new JMenuItem("Delete");
        sketchMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (SketchletEditor.editorPanel != null) {
                    SketchletEditor.editorPanel.openSketchByIndex(selectedIndex);
                    SketchletEditor.editorPanel.delete(frame);
                } else {
                    Object[] options = {Language.translate("Delete"), Language.translate("Cancel")};
                    int n = JOptionPane.showOptionDialog(frame,
                            Language.translate("You are about to delete") + " '" + s.title + "'",
                            Language.translate("Delete Sketch Confirmation"),
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            options,
                            options[0]);

                    if (n != 0) {
                        return;
                    }

                    int i = SketchletEditor.pages.pages.indexOf(s);
                    s.delete();

                    SketchletEditor.pages.pages.remove(s);
                }

                refresh();
            }
        });
        popupMenu.add(sketchMenuItem);

        popupMenu.show(this, x, y);

    }
}
