/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.ui.page.perspective;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.UIUtils;
import net.sf.sketchlet.model.ActiveRegion;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class PerspectivePanel extends JPanel {

    JComboBox perspectiveType = new JComboBox();
    public JCheckBox showPerspectiveGrid = new JCheckBox(Language.translate("Always show perspective grid"), false);
    JButton perspectiveFront = new JButton(Language.translate("front"), Workspace.createImageIcon("resources/perspective_lines.png"));
    JButton perspectiveLeft = new JButton(Language.translate("left"), Workspace.createImageIcon("resources/perspective_left.png"));
    JButton perspectiveRight = new JButton(Language.translate("right"), Workspace.createImageIcon("resources/perspective_right.png"));
    JButton perspectiveBottom = new JButton(Language.translate("bottom"), Workspace.createImageIcon("resources/perspective_bottom.png"));
    JButton perspectiveTop = new JButton(Language.translate("top"), Workspace.createImageIcon("resources/perspective_top.png"));
    Perspective3D perspectivePanel;

    public PerspectivePanel() {
        perspectivePanel = new Perspective3D(this);
        setLayout(new BorderLayout());
        createGUI();
        enableControls();
    }

    public void refresh() {
        UIUtils.refreshComboBox(this.perspectiveType, SketchletEditor.getInstance().getCurrentPage().getProperty("perspective type"));
        this.perspectivePanel.repaint();
        enableControls();
    }

    public void reload() {
        this.removeAll();
        createGUI();
        revalidate();
        repaint();
        enableControls();
    }

    public void enableControls() {
        boolean bEnable = SketchletEditor.getInstance().getCurrentPage().getRegions().getSelectedRegions() != null && SketchletEditor.getInstance().getCurrentPage().getRegions().getSelectedRegions().size() > 0;
        perspectiveFront.setEnabled(bEnable);
        perspectiveLeft.setEnabled(bEnable);
        perspectiveRight.setEnabled(bEnable);
        perspectiveBottom.setEnabled(bEnable);
        perspectiveTop.setEnabled(bEnable);
    }

    public void createGUI() {
        perspectiveType = new JComboBox();
        perspectiveType.addItem("");
        perspectiveType.addItem("1 point");
        perspectiveType.addItem("2 points");

        perspectiveType.setSelectedItem(SketchletEditor.getInstance().getCurrentPage().getProperty("perspective type"));

        perspectiveType.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.getInstance().saveSketchUndo();
                SketchletEditor.getInstance().getCurrentPage().setProperty("perspective type", perspectiveType.getSelectedItem().toString());
                SketchletEditor.getInstance().repaint();
            }
        });


        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.PAGE_AXIS));

        JPanel header1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header1.add(new JLabel(), Language.translate("Perspective Type: "));
        header1.add(this.perspectiveType);

        header.add(header1);
        JPanel header2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header2.add(this.showPerspectiveGrid);
        header.add(header2);

        JPanel autoPerspective = new JPanel(new GridLayout(3, 3));
        perspectiveTop.setMargin(new Insets(0, 2, 0, 2));
        perspectiveTop.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().setAutomaticPerspective("top");
            }
        });
        autoPerspective.add(new JLabel(""));
        autoPerspective.add(perspectiveTop);
        autoPerspective.add(new JLabel(""));
        perspectiveLeft.setMargin(new Insets(0, 2, 0, 2));
        perspectiveLeft.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().setAutomaticPerspective("left");
            }
        });
        autoPerspective.add(perspectiveLeft);
        perspectiveFront.setMargin(new Insets(0, 2, 0, 2));
        perspectiveFront.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().setAutomaticPerspective("front");
            }
        });
        autoPerspective.add(perspectiveFront);
        perspectiveRight.setMargin(new Insets(0, 2, 0, 2));
        perspectiveRight.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().setAutomaticPerspective("right");
            }
        });
        autoPerspective.add(perspectiveRight);
        perspectiveBottom.setMargin(new Insets(0, 2, 0, 2));
        perspectiveBottom.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().setAutomaticPerspective("bottom");
            }
        });
        autoPerspective.add(new JLabel(""));
        autoPerspective.add(perspectiveBottom);
        autoPerspective.add(new JLabel(""));


        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(header, BorderLayout.NORTH);
        rightPanel.add(autoPerspective, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.WEST);
        add(perspectivePanel, BorderLayout.CENTER);

        /*JButton btnClose = new JButton(Workspace.createImageIcon("resources/close_small.png"));
        btnClose.setToolTipText("Close");
        btnClose.addActionListener(new ActionListener() {
        
        public void actionPerformed(ActionEvent ae) {
        SketchletEditor.showPerspectivePanel();
        }
        });*/

        /*JToolBar tbRight = new JToolBar();
        tbRight.setFloatable(false);
        tbRight.setBorder(BorderFactory.createEmptyBorder());
        tbRight.setOrientation(JToolBar.VERTICAL);
        tbRight.add(btnClose);
        final JButton help = new JButton("", MainFrame.createImageIcon(this, "resources/help-browser.png", ""));
        help.addActionListener(new ActionListener() {
        
        public void actionPerformed(ActionEvent event) {
        HelpUtils.openHelpFile("Perspective", "perspective");
        }
        });
        help.setToolTipText("What is this?");
        help.setMargin(new Insets(0, 0, 0, 0));
        tbRight.add(help);
        
        add(tbRight, BorderLayout.EAST);*/
    }

    public Dimension getPreferredSize() {
        return new Dimension(1000, 170);
    }
}

class Perspective3D extends JPanel {

    PerspectivePanel parent;
    Perspective3D thisPanel = this;

    public Perspective3D(PerspectivePanel parent) {
        this.parent = parent;
        DepthMouseListener l = new DepthMouseListener();
        addMouseListener(l);
        addMouseMotionListener(l);
//        createGUI();
    }

    public Dimension getPreferredSize() {
        return new Dimension(1000, 120);
    }

    public void paintComponent(Graphics g) {
        Vector<ActiveRegion> regions = SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions();
        Graphics2D g2 = (Graphics2D) g;

        int w = getWidth();
        int h = getHeight();
        int sw = SketchletEditor.getInstance().getWidth();
        int sh = SketchletEditor.getInstance().getHeight();

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h);
        g2.setColor(Color.BLACK);
        g2.drawRect(0, 0, w, h);

        int mw = 10;
        int mh = 10;
        int rh = 8;

        g2.setColor(new Color(100, 100, 100, 100));
        g2.drawLine(mw, mh, w - 2 * mw, mh);
        g2.drawLine(mw, h - mh, w - 2 * mw, h - mh);

        int i = 0;
        for (ActiveRegion region : regions) {
            double depth = 0.0;
            if (!region.strPerspectiveDepth.isEmpty()) {
                try {
                    depth = Double.parseDouble(region.strPerspectiveDepth);
                } catch (Exception e) {
                }
            }

            int x = (int) (region.x1 * w / (double) sw);
            int y = (int) (h - (h - 2 * mh - rh) * depth) - mh - rh;
            int _w = (int) (region.getWidth() * w / (double) sw);

            g2.setColor(new Color(100, 100, 100, 100));
            g2.drawLine(x, h - mh, x + _w / 2, mh);
            g2.drawLine(x + _w, h - mh, x + _w / 2, mh);

            if (SketchletEditor.getInstance().getCurrentPage().getRegions().getSelectedRegions() != null && SketchletEditor.getInstance().getCurrentPage().getRegions().getSelectedRegions().contains(region)) {
                g2.setColor(new Color(200, 100, 100, 80));
            } else {
                g2.setColor(new Color(100, 100, 100, 80));
            }

            g2.fillRect(x, y, _w, rh);
            g2.setColor(Color.BLACK);
            g2.drawRect(x, y, _w, rh);
            String strText = "" + (regions.size() - regions.indexOf(region));
            g2.drawString(strText, x + 3, y + rh);
            i++;
        }
    }

    public ActiveRegion getSelectedRegion(int mx, int my) {
        Vector<ActiveRegion> regions = SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions();
        int w = getWidth();
        int h = getHeight();
        int sw = SketchletEditor.getInstance().getWidth();
        int sh = SketchletEditor.getInstance().getHeight();
        int mw = 10;
        int mh = 10;
        int rh = 8;

        int i = 0;
        for (ActiveRegion region : regions) {
            double depth = 0.0;
            if (!region.strPerspectiveDepth.isEmpty()) {
                try {
                    depth = Double.parseDouble(region.strPerspectiveDepth);
                } catch (Exception e) {
                }
            }

            int x = (int) (region.x1 * w / (double) sw);
            int y = (int) (h - (h - 2 * mh - rh) * depth) - mh - rh;
            int _w = (int) (region.getWidth() * w / (double) sw);

            if (new Rectangle(x, y - 3, _w, rh + 6).contains(mx, my)) {
                return region;
            }

            i++;
        }

        return null;
    }

    public void createGUI() {
        setBorder(BorderFactory.createTitledBorder("Depth"));
        Vector<ActiveRegion> _regions = SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions();
        ActiveRegion regions[] = new ActiveRegion[_regions.size()];

        int i = 0;
        for (ActiveRegion region : _regions) {
            regions[i] = region;
            i++;
        }

        for (i = 0; i < regions.length; i++) {
            for (int j = i + 1; j < regions.length; j++) {
                if (i != j) {
                    ActiveRegion r1 = regions[i];
                    ActiveRegion r2 = regions[j];
                    if (r1.x1 > r2.x1) {
                        regions[i] = r2;
                        regions[j] = r1;
                    }
                }
            }
        }
        setLayout(new GridLayout(0, regions.length));

        for (i = 0; i < regions.length; i++) {
            final ActiveRegion region = regions[i];
            int init = 0;
            if (!region.strPerspectiveDepth.isEmpty()) {
                try {
                    init = (int) Double.parseDouble(region.strPerspectiveDepth) * 100;
                } catch (Exception e) {
                }
            }
            final JSlider slider = new JSlider(JSlider.VERTICAL, 0, 100, init);
            slider.setValue(init);
            slider.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    double depth = slider.getValue() / 100.0;
                    region.strPerspectiveDepth = "" + depth;
                    SketchletEditor.getInstance().getCurrentPage().getRegions().setSelectedRegions(new Vector<ActiveRegion>());
                    SketchletEditor.getInstance().getCurrentPage().getRegions().getSelectedRegions().add(region);
                    parent.enableControls();
                    SketchletEditor.getInstance().repaint();
                }
            });

            JPanel panelSlider = new JPanel();
            panelSlider.setLayout(new BoxLayout(panelSlider, BoxLayout.PAGE_AXIS));

            panelSlider.add(new JLabel("Region " + (_regions.size() - _regions.indexOf(region))));
            slider.setMajorTickSpacing(10);
            slider.setPaintTicks(true);

            panelSlider.add(slider);

            add(panelSlider);
        }
    }

    class DepthMouseListener extends MouseInputAdapter {

        ActiveRegion selectedRegion = null;

        @Override
        public void mousePressed(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            selectedRegion = getSelectedRegion(x, y);
            if (selectedRegion != null) {
                SketchletEditor.getInstance().saveRegionUndo();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            if (getSelectedRegion(x, y) != null) {
                setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
            } else {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (selectedRegion != null) {
                int y = e.getY();
                int h = getHeight();
                int mh = 10;

                double depth = Math.max(0.0, ((double) y - mh) / (h - 2 * mh));
                depth = Math.min(1.0, depth);
                depth = 1 - depth;
                selectedRegion.strPerspectiveDepth = "" + depth;
                SketchletEditor.getInstance().getCurrentPage().getRegions().setSelectedRegions(new Vector<ActiveRegion>());
                SketchletEditor.getInstance().getCurrentPage().getRegions().getSelectedRegions().add(selectedRegion);
                parent.enableControls();
                repaint();
                SketchletEditor.getInstance().forceRepaint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }
    }
}
