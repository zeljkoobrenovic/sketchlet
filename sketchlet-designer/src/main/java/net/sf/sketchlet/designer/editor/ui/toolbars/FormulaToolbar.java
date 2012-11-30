package net.sf.sketchlet.designer.editor.ui.toolbars;

import net.sf.sketchlet.codegen.CodeGenUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.GlobalProperties;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.tool.stroke.StrokeCombo;
import net.sf.sketchlet.designer.editor.ui.extraeditor.ActiveRegionsExtraPanel;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionPanel;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionsFrame;
import net.sf.sketchlet.designer.editor.ui.region.ShapePanel;
import net.sf.sketchlet.help.HelpUtils;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.util.Colors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

/**
 * @author zobrenovic
 */
public class FormulaToolbar extends JPanel {

    //public JComboBox regions = new JComboBox();
    public JComboBox properties1 = new JComboBox();
    public JTextField value1 = new JTextField();
    public JComboBox properties2 = new JComboBox();
    public JTextField value2 = new JTextField();
    public JComboBox properties3 = new JComboBox();
    public JTextField value3 = new JTextField();
    JButton formula = new JButton(Workspace.createImageIcon("resources/formula_small.png"));
    JButton ok = new JButton(Workspace.createImageIcon("resources/formula_ok.png"));
    JButton cancel = new JButton(Workspace.createImageIcon("resources/formula_cancel.png"));
    JButton appearance = new JButton(Workspace.createImageIcon("resources/color-wheel.gif"));
    JButton image = new JButton(Workspace.createImageIcon("resources/image_small.png"));
    JButton widget = new JButton(Workspace.createImageIcon("resources/checked_checkbox.png"));
    JButton transform = new JButton(Workspace.createImageIcon("resources/properties_small.gif"));
    JButton move = new JButton(Workspace.createImageIcon("resources/move_small.png"));
    JButton mouse = new JButton(Workspace.createImageIcon("resources/mouse_small.png"));
    JButton overlap = new JButton(Workspace.createImageIcon("resources/overlap_small.png"));
    JButton more = new JButton("...");
    JButton menu1 = new JButton(Workspace.createImageIcon("resources/menu.png"));
    JButton menu2 = new JButton(Workspace.createImageIcon("resources/menu.png"));
    JButton menu3 = new JButton(Workspace.createImageIcon("resources/menu.png"));
    JButton help = new JButton(Workspace.createImageIcon("resources/help-browser.png"));
    JButton colorFill = new JButton(Workspace.createImageIcon("resources/palette_fill.png"));
    JButton colorLine = new JButton(Workspace.createImageIcon("resources/palette_line.png"));
    JButton colorText = new JButton(Workspace.createImageIcon("resources/color_text.gif"));
    JButton shapes = new JButton(Workspace.createImageIcon("resources/shapes.png"));
    JButton align = new JButton(Workspace.createImageIcon("resources/center.gif"));

    public FormulaToolbar() {
        BorderLayout bl = new BorderLayout();
        bl.setVgap(0);
        this.setLayout(bl);

        JToolBar panelLeft = new JToolBar();
        panelLeft.setFloatable(false);
        JLabel label = new JLabel(Language.translate(" selected region: "));
        label.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(label);
        label = new JLabel(Language.translate(" set region property: "));
        label.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(label);
        properties1.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(properties1);
        properties2.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(properties2);
        properties3.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(properties3);
        // panelLeft.add(label);
        /*panelLeft.add(formula);
        panelLeft.add(cancel);
        panelLeft.add(ok);*/
        add(panelLeft, BorderLayout.WEST);
        JToolBar filedsTb = new JToolBar();
        filedsTb.setFloatable(false);
        filedsTb.setLayout(new GridLayout(1, 0));
        JLabel labelP = new JLabel(Language.translate("Property: "));
        labelP.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(labelP);
        JToolBar pan1 = new JToolBar();
        pan1.setFloatable(false);
        pan1.add(labelP);
        pan1.add(properties1);
        //filedsTb.add(panP);
        //pan1.setLayout(new BorderLayout());
        value1.setSize(new Dimension(120, 25));
        value1.setPreferredSize(new Dimension(120, 25));
        pan1.add(value1);
        pan1.add(menu1);
        filedsTb.add(pan1);
        //filedsTb.add(properties2);
        JToolBar pan2 = new JToolBar();
        pan2.setLayout(new BorderLayout());
        pan2.add(value2, BorderLayout.CENTER);
        pan2.add(menu2, BorderLayout.EAST);
        //filedsTb.add(pan2);
        //filedsTb.add(properties3);
        JToolBar pan3 = new JToolBar();
        pan3.setLayout(new BorderLayout());
        pan3.add(value3, BorderLayout.CENTER);
        pan3.add(menu3, BorderLayout.EAST);
        //filedsTb.add(pan3);

        colorFill.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getLastSelectedRegion();
                if (region != null || SketchletEditor.getInstance().getCurrentPage().getSelectedConnector() != null) {
                    JPopupMenu menu = PopupMenus.loadFillColorMenu();
                    menu.show(null, 0, 0);
                    int h = menu.getHeight();
                    menu.setVisible(false);
                    menu.show(colorFill, 0, -h);
                }
            }
        });
        colorLine.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getLastSelectedRegion();
                if (region != null || SketchletEditor.getInstance().getCurrentPage().getSelectedConnector() != null) {
                    JPopupMenu menu = PopupMenus.loadLineColorMenu();
                    menu.show(null, 0, 0);
                    int h = menu.getHeight();
                    menu.setVisible(false);
                    menu.show(colorLine, 0, -h);
                }
            }
        });
        colorText.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getLastSelectedRegion();
                if (region != null || SketchletEditor.getInstance().getCurrentPage().getSelectedConnector() != null) {
                    JPopupMenu menu = PopupMenus.loadTextColorMenu();
                    menu.show(null, 0, 0);
                    int h = menu.getHeight();
                    menu.setVisible(false);
                    menu.show(colorText, 0, -h);
                }
            }
        });

        align.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getLastSelectedRegion();
                if (region != null) {
                    JPopupMenu menu = PopupMenus.loadAlignmentMenu();
                    menu.show(null, 0, 0);
                    int h = menu.getHeight();
                    menu.setVisible(false);
                    menu.show(align, 0, -h);
                }
            }
        });

        shapes.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getLastSelectedRegion();
                if (region != null) {
                    JPopupMenu menu = PopupMenus.loadShapeMenu();
                    menu.show(null, 0, 0);
                    int h = menu.getHeight();
                    menu.setVisible(false);
                    menu.show(shapes, 0, -h);
                }
            }
        });

        add(filedsTb, BorderLayout.CENTER);
        //filedsTb.add(menu);

        JToolBar panelRight = new JToolBar();
        panelRight.setFloatable(false);
        panelRight.add(shapes);
        panelRight.add(colorFill);
        panelRight.add(colorLine);
        panelRight.addSeparator();
        panelRight.add(colorText);
        panelRight.add(align);
        panelRight.addSeparator();
        panelRight.add(help);
        appearance.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                JPopupMenu lineMenu = (JPopupMenu) ModeToolbar.createAppearanceMenu(true);
                lineMenu.show(appearance.getParent(), appearance.getX(), appearance.getY() + appearance.getHeight());
            }
        });
        widget.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                ActiveRegionsExtraPanel.showRegionsAndActions();
                final ActiveRegionPanel ap = ActiveRegionsFrame.refresh(region, ActiveRegionPanel.getIndexWidget());
                if (ap != null) {
                }
            }
        });
        image.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                ActiveRegionsExtraPanel.showRegionsAndActions();
                final ActiveRegionPanel ap = ActiveRegionsFrame.refresh(region, ActiveRegionPanel.getIndexGraphics());
                if (ap != null) {
                }
                /*
                JPopupMenu popup = new JPopupMenu();
                JMenuItem menuItem;
                
                menuItem = new JMenuItem("draw");
                menuItem.addActionListener(new ActionListener() {
                
                public void actionPerformed(ActionEvent e) {
                ActiveRegionsExtraPanel.showRegionsAndActions();
                final ActiveRegionPanel ap = ActiveRegionsFrame.refresh(region, ActiveRegionsFrame.imageTabIndex);
                if (ap != null) {
                ap.tabsImage.setSelectedIndex(0);
                }
                }
                });
                popup.add(menuItem);
                menuItem = new JMenuItem("import from a file");
                menuItem.addActionListener(new ActionListener() {
                
                public void actionPerformed(ActionEvent e) {
                ActiveRegionsExtraPanel.showRegionsAndActions(); 
                final ActiveRegionPanel ap = ActiveRegionsFrame.refresh(region, ActiveRegionsFrame.imageTabIndex);
                if (ap != null) {
                ap.imageEditor.fromFile();
                }
                }
                });
                popup.add(menuItem);
                menuItem = new JMenuItem("paste from clipboard");
                menuItem.addActionListener(new ActionListener() {
                
                public void actionPerformed(ActionEvent e) {
                ActiveRegionsExtraPanel.showRegionsAndActions(); 
                final ActiveRegionPanel ap = ActiveRegionsFrame.refresh(region, ActiveRegionsFrame.imageTabIndex);
                if (ap != null) {
                }
                }
                });
                popup.add(menuItem);
                popup.addSeparator();
                menuItem = new JMenuItem("extract from main sketch");
                menuItem.addActionListener(new ActionListener() {
                
                public void actionPerformed(ActionEvent e) {
                ActiveRegionsExtraPanel.showRegionsAndActions(); 
                final ActiveRegionPanel ap = ActiveRegionsFrame.refresh(region, ActiveRegionsFrame.imageTabIndex);
                if (ap != null) {
                SketchletEditor.editorPanel.extract(0);
                }
                }
                });
                popup.add(menuItem);
                menuItem = new JMenuItem("extract in new frame");
                menuItem.addActionListener(new ActionListener() {
                
                public void actionPerformed(ActionEvent e) {
                ActiveRegionsExtraPanel.showRegionsAndActions(); 
                final ActiveRegionPanel ap = ActiveRegionsFrame.refresh(region, ActiveRegionsFrame.imageTabIndex);
                if (ap != null) {
                SketchletEditor.editorPanel.extract(-1);
                }
                }
                });
                popup.add(menuItem);
                popup.addSeparator();
                if (Profiles.isActive("active_region_screen_capture")) {
                menuItem = new JMenuItem("screen capture");
                menuItem.addActionListener(new ActionListener() {
                
                public void actionPerformed(ActionEvent e) {
                ActiveRegionsExtraPanel.showRegionsAndActions(); 
                final ActiveRegionPanel ap = ActiveRegionsFrame.refresh(region, ActiveRegionsFrame.imageTabIndex);
                if (ap != null) {
                ap.tabsImage.setSelectedIndex(2);
                }
                }
                });
                popup.add(menuItem);
                popup.addSeparator();
                }
                if (Profiles.isActive("active_region_url")) {
                menuItem = new JMenuItem("specify URL/file path");
                menuItem.addActionListener(new ActionListener() {
                
                public void actionPerformed(ActionEvent e) {
                ActiveRegionsExtraPanel.showRegionsAndActions(); 
                final ActiveRegionPanel ap = ActiveRegionsFrame.refresh(region, ActiveRegionsFrame.imageTabIndex);
                if (ap != null) {
                ap.tabsImage.setSelectedIndex(1);
                }
                }
                });
                popup.add(menuItem);
                popup.addSeparator();
                }
                if (Profiles.isActive("active_region_screen_text")) {
                menuItem = new JMenuItem("text");
                menuItem.addActionListener(new ActionListener() {
                
                public void actionPerformed(ActionEvent e) {
                ActiveRegionsExtraPanel.showRegionsAndActions(); 
                final ActiveRegionPanel ap = ActiveRegionsFrame.refresh(region, ActiveRegionsFrame.imageTabIndex);
                if (ap != null) {
                ap.tabsImage.setSelectedIndex(4);
                }
                }
                });
                popup.add(menuItem);
                }
                if (Profiles.isActive("active_region_screen_charts")) {
                menuItem = new JMenuItem("charts");
                menuItem.addActionListener(new ActionListener() {
                
                public void actionPerformed(ActionEvent e) {
                ActiveRegionsExtraPanel.showRegionsAndActions(); 
                final ActiveRegionPanel ap = ActiveRegionsFrame.refresh(region, ActiveRegionsFrame.imageTabIndex);
                if (ap != null) {
                ap.tabsImage.setSelectedIndex(5);
                }
                }
                });
                popup.add(menuItem);
                }
                if (Profiles.isActive("active_region_html")) {
                menuItem = new JMenuItem("HTML");
                menuItem.addActionListener(new ActionListener() {
                
                public void actionPerformed(ActionEvent e) {
                ActiveRegionsExtraPanel.showRegionsAndActions(); 
                final ActiveRegionPanel ap = ActiveRegionsFrame.refresh(region, ActiveRegionsFrame.imageTabIndex);
                if (ap != null) {
                ap.tabsImage.setSelectedIndex(6);
                }
                }
                });
                popup.add(menuItem);
                }
                if (Profiles.isActive("active_region_svg")) {
                menuItem = new JMenuItem("SVG");
                menuItem.addActionListener(new ActionListener() {
                
                public void actionPerformed(ActionEvent e) {
                ActiveRegionsExtraPanel.showRegionsAndActions(); 
                final ActiveRegionPanel ap = ActiveRegionsFrame.refresh(region, ActiveRegionsFrame.imageTabIndex);
                if (ap != null) {
                ap.tabsImage.setSelectedIndex(7);
                }
                }
                });
                popup.add(menuItem);
                popup.addSeparator();
                }
                if (Profiles.isActive("active_region_shape")) {
                menuItem = new JMenuItem("define shape");
                menuItem.addActionListener(new ActionListener() {
                
                public void actionPerformed(ActionEvent e) {
                ActiveRegionsExtraPanel.showRegionsAndActions(); 
                final ActiveRegionPanel ap = ActiveRegionsFrame.refresh(region, ActiveRegionsFrame.imageTabIndex);
                if (ap != null) {
                ap.tabsImage.setSelectedIndex(3);
                }
                }
                });
                popup.add(menuItem);
                }
                if (Profiles.isActive("active_region_screen_align")) {
                menuItem = new JMenuItem("align");
                menuItem.addActionListener(new ActionListener() {
                
                public void actionPerformed(ActionEvent e) {
                ActiveRegionsExtraPanel.showRegionsAndActions(); 
                final ActiveRegionPanel ap = ActiveRegionsFrame.refresh(region, ActiveRegionsFrame.imageTabIndex);
                if (ap != null) {
                ap.tabsImage.setSelectedIndex(8);
                }
                }
                });
                popup.add(menuItem);
                }
                menuItem = new JMenuItem("define visible area");
                menuItem.addActionListener(new ActionListener() {
                
                public void actionPerformed(ActionEvent e) {
                ActiveRegionsExtraPanel.showRegionsAndActions(); 
                final ActiveRegionPanel ap = ActiveRegionsFrame.refresh(region, ActiveRegionsFrame.imageTabIndex);
                if (ap != null) {
                SketchletEditor.editorPanel.defineClip();
                }
                }
                });
                popup.add(menuItem);
                if (Profiles.isActive("active_region_all_image_properties")) {
                popup.addSeparator();
                menuItem = new JMenuItem("all images properties1");
                menuItem.addActionListener(new ActionListener() {
                
                public void actionPerformed(ActionEvent e) {
                ActiveRegionsExtraPanel.showRegionsAndActions(); 
                final ActiveRegionPanel ap = ActiveRegionsFrame.refresh(region, ActiveRegionsFrame.imageTabIndex);
                if (ap != null) {
                ap.tabsImage.setSelectedIndex(9);
                }
                }
                });
                popup.add(menuItem);
                }
                popup.show(images, 0, images.getHeight());*/
            }
        });
        move.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                ActiveRegionsExtraPanel.showRegionsAndActions();
                ActiveRegionPanel ap = ActiveRegionsFrame.refresh(region, ActiveRegionPanel.getIndexEvents(), ActiveRegionPanel.getIndexMotion());
            }
        });
        mouse.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                ActiveRegionsExtraPanel.showRegionsAndActions();
                ActiveRegionPanel ap = ActiveRegionsFrame.refresh(region, ActiveRegionPanel.getIndexEvents(), ActiveRegionPanel.getIndexMouseEvents());
            }
        });
        overlap.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                ActiveRegionsExtraPanel.showRegionsAndActions();
                ActiveRegionPanel ap = ActiveRegionsFrame.refresh(region, ActiveRegionPanel.getIndexEvents(), ActiveRegionPanel.getIndexOverlap());
            }
        });
        more.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                ActiveRegionsExtraPanel.showRegionsAndActions();
                final ActiveRegionPanel ap = ActiveRegionsFrame.refresh(region, ActiveRegionPanel.getIndexGeneral());
            }
        });

        transform.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                ActiveRegionsExtraPanel.showRegionsAndActions();
                final ActiveRegionPanel ap = ActiveRegionsFrame.refresh(region, ActiveRegionPanel.getIndexTransform());
            }
        });
        menu1.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                loadMenu(menu1, properties1, value1);
            }
        });
        menu2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                loadMenu(menu2, properties2, value2);
            }
        });
        menu3.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                loadMenu(menu3, properties3, value3);
            }
        });
        help.setToolTipText(Language.translate("What is this parameters bar?"));
        help.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                HelpUtils.openHelpFile("", "active_region_image_properties_bar");
            }
        });
        add(panelRight, BorderLayout.EAST);
        //add(value1, BorderLayout.CENTER);
        //regions.setMaximumRowCount(30);
        properties1.setMaximumRowCount(30);
        properties2.setMaximumRowCount(30);
        properties3.setMaximumRowCount(30);
        formula.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                FormulasListDialog dlg = new FormulasListDialog(SketchletEditor.editorFrame, value1);
            }
        });
        /*ok.addActionListener(new ActionListener() {
        
        public void actionPerformed(ActionEvent ae) {
        setValues();
        }
        });
        cancel.addActionListener(new ActionListener() {
        
        public void actionPerformed(ActionEvent ae) {
        loadValues();
        }
        });*/
        properties1.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int index = properties1.getSelectedIndex();
                String strProp = (String) properties1.getSelectedItem();
                String type = getPropertyType(properties1);
                menu1.setEnabled(type != null && !type.isEmpty());
                if (strProp != null) {
                    if (!strProp.isEmpty()) {
                        loadValue(properties1, value1);
                        GlobalProperties.set("region-quick-property-1", strProp);
                        GlobalProperties.save();
                    } else {
                        // properties1.setSelectedIndex(index + 1);
                    }
                }
            }
        });
        value1.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                setValue(properties1, value1);
                value1.selectAll();
            }
        });
        properties2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int index = properties2.getSelectedIndex();
                String strProp = (String) properties2.getSelectedItem();
                String type = getPropertyType(properties2);
                menu2.setEnabled(type != null && !type.isEmpty());
                if (strProp != null) {
                    if (!strProp.isEmpty()) {
                        loadValue(properties2, value2);
                        GlobalProperties.set("region-quick-property-2", strProp);
                        GlobalProperties.save();
                    } else {
                        // properties1.setSelectedIndex(index + 1);
                    }
                }
            }
        });
        value2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                setValue(properties2, value2);
                value2.selectAll();
            }
        });
        properties3.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int index = properties3.getSelectedIndex();
                String strProp = (String) properties3.getSelectedItem();
                String type = getPropertyType(properties3);
                menu3.setEnabled(type != null && !type.isEmpty());
                if (strProp != null) {
                    if (!strProp.isEmpty()) {
                        loadValue(properties3, value3);
                        GlobalProperties.set("region-quick-property-3", strProp);
                        GlobalProperties.save();
                    } else {
                        // properties1.setSelectedIndex(index + 1);
                    }
                }
            }
        });
        value3.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                setValue(properties3, value3);
                value3.selectAll();
            }
        });
        menu1.setToolTipText(Language.translate("predefined property values"));
        menu2.setToolTipText(Language.translate("predefined property values"));
        menu3.setToolTipText(Language.translate("predefined property values"));
        loadProperties();

        this.enableControls();
    }

    public void reload() {
        bLoading = true;
        //reloadRegions();
        bLoading = false;
        refresh();
        loadValues();
    }

    public void refresh() {
        ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getLastSelectedRegion();
        if (bLoading) {
            return;
        }
        refreshRegions(region);
        loadValues();
    }

    public void loadProperties() {
        loadProperties(properties1, "region-quick-property-1");
        loadProperties(properties2, "region-quick-property-2");
        loadProperties(properties3, "region-quick-property-3");
    }

    public void loadProperties(JComboBox combo, String strPropertyId) {
        String oldValue = GlobalProperties.get(strPropertyId);
        combo.removeAllItems();
        for (int i = 0; i < this.regionProperties.length; i++) {
            combo.addItem(this.regionProperties[i][0]);
        }

        if (oldValue != null && !oldValue.isEmpty()) {
            combo.setSelectedItem(oldValue);
        }
    }

    /*public void reloadRegions() {
    int selIndex = 0;
    regions.removeAllItems();
    regions.addItem("");
    for (int i = SketchletEditor.editorPanel.currentSketch.regions.regions.size() - 1; i >= 0; i--) {
    ActiveRegion r = SketchletEditor.editorPanel.currentSketch.regions.regions.elementAt(i);
    if (r.isSelected()) {
    region = r;
    selIndex = i;
    }
    regions.addItem(r.getNumber());
    }
    
    if (selIndex >= 0) {
    regions.setSelectedIndex(selIndex);
    }
    }*/
    ActiveRegion region = null;

    public void setValues() {
        setValue(properties1, value1);
        setValue(properties2, value2);
        setValue(properties3, value3);
        SketchletEditor.getInstance().forceRepaint();
        ActiveRegionsFrame.reload(region);
    }

    public void setValue(JComboBox combo, JTextField value) {
        bLoading = true;
        //String strRegion = (String) this.regions.getSelectedItem();
        String strProperty = (String) combo.getSelectedItem();
        if (strProperty != null) {
            try {
                //int n = Integer.parseInt(strRegion);
                //region = SketchletEditor.editorPanel.currentSketch.regions.getRegionByNumber(n);
                region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getLastSelectedRegion();//SketchletEditor.editorPanel.currentSketch.regions.getRegionByNumber(n);

                if (region != null) {
                    region.setProperty(strProperty, value.getText());
                    SketchletEditor.getInstance().repaint();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        bLoading = false;
    }

    boolean bLoading = false;

    public void enableControls() {
        this.enableControls(SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getLastSelectedRegion() != null, SketchletEditor.getInstance().getCurrentPage().getSelectedConnector() != null);
    }

    public void enableControls(boolean bEnableRegion, boolean bEnableConnector) {
        this.properties1.setEnabled(bEnableRegion);
        this.value1.setEnabled(bEnableRegion);
        this.properties2.setEnabled(bEnableRegion);
        this.value2.setEnabled(bEnableRegion);
        this.properties3.setEnabled(bEnableRegion);
        this.value3.setEnabled(bEnableRegion);
        this.ok.setEnabled(bEnableRegion);
        this.cancel.setEnabled(bEnableRegion);
        this.appearance.setEnabled(bEnableRegion);
        this.image.setEnabled(bEnableRegion);
        this.widget.setEnabled(bEnableRegion);
        this.mouse.setEnabled(bEnableRegion);
        this.move.setEnabled(bEnableRegion);
        this.overlap.setEnabled(bEnableRegion);
        this.more.setEnabled(bEnableRegion);
        this.formula.setEnabled(bEnableRegion);
        this.shapes.setEnabled(bEnableRegion);
        this.colorFill.setEnabled(bEnableRegion || bEnableConnector);
        this.colorLine.setEnabled(bEnableRegion || bEnableConnector);
        this.colorText.setEnabled(bEnableRegion || bEnableConnector);
        this.align.setEnabled(bEnableRegion);
    }

    public String getPropertyType(JComboBox combo) {
        String strProperty = (String) combo.getSelectedItem();
        String type = "";
        if (strProperty != null && !strProperty.isEmpty()) {
            for (int i = 0; i < regionProperties.length; i++) {
                if (strProperty.equalsIgnoreCase(regionProperties[i][0])) {
                    type = regionProperties[i][1];
                    break;
                }
            }
        }

        return type;
    }

    public void loadMenu(JButton btnMenu, JComboBox combo, JTextField value) {
        String type = getPropertyType(combo);
        if (type != null && !type.isEmpty()) {
            if (type.equalsIgnoreCase("colors")) {
                JPopupMenu popup = loadColorMenu(value);
                popup.show(btnMenu, 0, btnMenu.getHeight());
            } else if (type.equalsIgnoreCase("relative")) {
                JPopupMenu popup = loadRelativeMenu(value);
                popup.show(btnMenu, 0, btnMenu.getHeight());
            } else if (type.equalsIgnoreCase("horizontal alignment")) {
                JPopupMenu popup = this.loadHorizontalAlignmentMenu(value);
                popup.show(btnMenu, 0, btnMenu.getHeight());
            } else if (type.equalsIgnoreCase("vertical alignment")) {
                JPopupMenu popup = this.loadVerticalAlignmentMenu(value);
                popup.show(btnMenu, 0, btnMenu.getHeight());
            } else if (type.equalsIgnoreCase("transparency")) {
                JPopupMenu popup = loadTransparencyMenu(value);
                popup.show(btnMenu, 0, btnMenu.getHeight());
            } else if (type.equalsIgnoreCase("pixels")) {
                JPopupMenu popup = loadPixelsMenu(value);
                popup.show(btnMenu, 0, btnMenu.getHeight());
            } else if (type.equalsIgnoreCase("rotation")) {
                JPopupMenu popup = loadRotationMenu(value);
                popup.show(btnMenu, 0, btnMenu.getHeight());
            } else if (type.equalsIgnoreCase("thickness")) {
                JPopupMenu popup = loadThicknessMenu(value);
                popup.show(btnMenu, 0, btnMenu.getHeight());
            } else if (type.equalsIgnoreCase("line style")) {
                JPopupMenu popup = loadLineStyleMenu(value);
                popup.show(btnMenu, 0, btnMenu.getHeight());
            } else if (type.equalsIgnoreCase("font style")) {
                JPopupMenu popup = loadMenu(value, new String[]{"bold", "italic", "bold italic"});
                popup.show(btnMenu, 0, btnMenu.getHeight());
            } else if (type.equalsIgnoreCase("auto perspective")) {
                JPopupMenu popup = loadMenu(value, new String[]{"front", "left", "right", "bottom", "top"});
                popup.show(btnMenu, 0, btnMenu.getHeight());
            } else if (type.equalsIgnoreCase("shape")) {
                JPopupMenu popup = loadShapeMenu(value);
                popup.show(btnMenu, 0, btnMenu.getHeight());
            } else if (type.equalsIgnoreCase("font")) {
                String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
                JPopupMenu popup = loadMenu(value, fonts);
                popup.show(btnMenu, 0, btnMenu.getHeight());
            } else if (type.equalsIgnoreCase("font size")) {
                String size[] = new String[30];
                for (int i = 0; i < size.length; i++) {
                    size[i] = "" + (8 + i * 2);
                }
                JPopupMenu popup = loadMenu(value, size);
                popup.show(btnMenu, 0, btnMenu.getHeight());
            } else if (type.equalsIgnoreCase("type")) {
                JPopupMenu popup = loadMenu(value, CodeGenUtils.getControlTypeIDs(), CodeGenUtils.getExtraControls());
                popup.show(btnMenu, 0, btnMenu.getHeight());
            }
        }
    }

    public JPopupMenu loadColorMenu(final JTextField value) {
        final JPopupMenu colorsMenu = new JPopupMenu();

        Color colors[] = Colors.getStandardColors();
        String colorNames[] = Colors.getStandardColorNames();
        for (int i = 0; i < colors.length; i++) {
            final String colorName = colorNames[i];
            JMenuItem menuItem = new JMenuItem(colorName, ModeToolbar.createImageIcon(colors[i], 70, 20));
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    value.setText(colorName);
                    setValues();
                }
            });
            colorsMenu.add(menuItem);
        }
        colorsMenu.addSeparator();
        JMenuItem _menuItem = new JMenuItem(Language.translate("more colors"), ModeToolbar.createImageIconMore(70, 20));
        _menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                Color newColor = JColorChooser.showDialog(
                        SketchletEditor.getInstance().getInstance(),
                        "Choose Color",
                        SketchletEditor.getInstance().getColor());

                if (newColor != null) {
                    value.setText(newColor.getRed() + " " + newColor.getGreen() + " " + newColor.getBlue());
                    setValues();
                }
            }
        });
        colorsMenu.add(_menuItem);

        return colorsMenu;
    }

    public JPopupMenu loadShapeMenu(final JTextField value) {
        final JPopupMenu shapeMenu = new JPopupMenu();

        JMenuItem shapeRect = new JMenuItem(Language.translate("Rectangle"), Workspace.createImageIcon("resources/rectangle.png"));
        JMenuItem shapeOval = new JMenuItem(Language.translate("Oval"), Workspace.createImageIcon("resources/oval.png"));
        JMenuItem shapeRoundRect = new JMenuItem(Language.translate("Rounded Rectangle"), Workspace.createImageIcon("resources/rounded_rectangle.png"));
        JMenuItem shapeTriangle1 = new JMenuItem(Language.translate("Triangle"), Workspace.createImageIcon("resources/triangle_1.png"));
        JMenuItem shapeTriangle2 = new JMenuItem(Language.translate("Triangle"), Workspace.createImageIcon("resources/triangle_2.png"));
        JMenuItem shapeLine1 = new JMenuItem(Language.translate("Line"), Workspace.createImageIcon("resources/line_1.png"));
        JMenuItem shapeLine2 = new JMenuItem(Language.translate("Line"), Workspace.createImageIcon("resources/line_2.png"));
        JMenuItem shapeLine3 = new JMenuItem(Language.translate("Horizontal Line"), Workspace.createImageIcon("resources/line_3.png"));
        JMenuItem shapeLine4 = new JMenuItem(Language.translate("Vertical Line"), Workspace.createImageIcon("resources/line_4.png"));
        JMenuItem pieSlice = new JMenuItem(Language.translate("Pie Slice"), Workspace.createImageIcon("resources/pie_slice.png"));
        shapeRect.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setShape(value, "Rectangle");
            }
        });
        shapeOval.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setShape(value, "Oval");
            }
        });
        shapeRoundRect.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setShape(value, "Rounded Rectangle");
            }
        });
        shapeTriangle1.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setShape(value, "Triangle 1");
            }
        });
        shapeTriangle2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setShape(value, "Triangle 2");
            }
        });
        shapeLine1.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setShape(value, "Line 1");
            }
        });
        shapeLine2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setShape(value, "Line 2");
            }
        });
        shapeLine3.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setShape(value, "Horizontal Line");
            }
        });
        shapeLine4.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setShape(value, "Vertical Line");
            }
        });
        pieSlice.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setShape(value, "Pie Slice");
            }
        });
        shapeMenu.add(shapeRect);
        shapeMenu.add(shapeOval);
        shapeMenu.add(shapeRoundRect);
        shapeMenu.addSeparator();
        shapeMenu.add(shapeTriangle1);
        shapeMenu.add(shapeTriangle2);
        shapeMenu.addSeparator();
        shapeMenu.add(shapeLine1);
        shapeMenu.add(shapeLine2);
        shapeMenu.add(shapeLine3);
        shapeMenu.add(shapeLine4);
        shapeMenu.addSeparator();
        JMenu polygons = new JMenu(Language.translate("Regular Polygons"));
        polygons.setIcon(ShapePanel.createRegularShapeIcon(5));
        for (int mi = 10; mi < 15; mi++) {
            JMenuItem regPol = new JMenuItem(ShapePanel.getShapeStrings()[mi], ShapePanel.shapeIcons[mi]);
            final String strID = ShapePanel.shapeIDs[mi];
            final int _mi = mi;
            regPol.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    setShape(value, strID);
                }
            });
            polygons.add(regPol);
        }
        JMenu stars = new JMenu(Language.translate("Stars"));
        stars.setIcon(ShapePanel.createStarShapeIcon(5));
        for (int mi = 15; mi < 22; mi++) {
            JMenuItem regPol = new JMenuItem(ShapePanel.getShapeStrings()[mi], ShapePanel.shapeIcons[mi]);
            final String strID = ShapePanel.shapeIDs[mi];
            final int _mi = mi;
            regPol.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    setShape(value, strID);
                }
            });
            stars.add(regPol);
        }
        shapeMenu.add(polygons);
        shapeMenu.add(stars);
        shapeMenu.addSeparator();
        shapeMenu.add(pieSlice);

        return shapeMenu;
    }

    public void setShape(final JTextField value, String shape) {
        if (SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null) {
            value.setText(shape);
            for (ActiveRegion r : SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                r.setProperty("shape", shape);

                String strArgs = "";
                String args[] = SketchletEditor.getShapeArgs(shape);
                if (args != null) {
                    String arg = JOptionPane.showInputDialog(SketchletEditor.editorFrame, args[0], args[1]);
                    if (arg != null) {
                        strArgs = arg;
                    }
                }
                r.shapeArguments = strArgs;
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement());
            SketchletEditor.getInstance().forceRepaint();
        }
    }

    public JPopupMenu loadThicknessMenu(final JTextField value) {
        final JPopupMenu popupMenu = new JPopupMenu();

        for (int i = 1; i < 10; i++) {
            final String strThickness = "" + i;
            JMenuItem menuItem = new JMenuItem(strThickness, ModeToolbar.createImageIcon(i + 1, 70, 20));
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    value.setText(strThickness);
                    setValues();
                }
            });
            popupMenu.add(menuItem);
        }


        return popupMenu;
    }

    public JPopupMenu loadTransparencyMenu(final JTextField value) {
        final JPopupMenu popupMenu = new JPopupMenu();

        for (int i = 0; i <= 10; i++) {
            final double t = i / 10.0;
            final String strTransparency = "" + t;
            JMenuItem menuItem = new JMenuItem(strTransparency, ModeToolbar.createImageIcon(new Color(0.0f, 0.0f, 0.0f, (float) t), 70, 20));
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    value.setText("" + (float) t);
                    setValues();
                }
            });
            popupMenu.add(menuItem);
        }

        return popupMenu;
    }

    public JPopupMenu loadHorizontalAlignmentMenu(final JTextField value) {
        JMenuItem alignLeft = new JMenuItem(Language.translate("Left"), Workspace.createImageIcon("resources/left.gif"));
        alignLeft.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                value.setText("left");
                setValues();
            }
        });
        JMenuItem alignCenter = new JMenuItem(Language.translate("Center"), Workspace.createImageIcon("resources/center.gif"));
        alignCenter.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                value.setText("center");
                setValues();
            }
        });
        JMenuItem alignRight = new JMenuItem(Language.translate("Right"), Workspace.createImageIcon("resources/right.gif"));
        alignRight.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                value.setText("right");
                setValues();
            }
        });

        final JPopupMenu popupMenu = new JPopupMenu();

        popupMenu.add(alignLeft);
        popupMenu.add(alignCenter);
        popupMenu.add(alignRight);
        return popupMenu;
    }

    public JPopupMenu loadVerticalAlignmentMenu(final JTextField value) {
        JMenuItem alignTop = new JMenuItem(Language.translate("Top"), Workspace.createImageIcon("resources/align-top.png"));
        alignTop.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                value.setText("top");
                setValues();
            }
        });
        JMenuItem alignMiddle = new JMenuItem(Language.translate("Middle"), Workspace.createImageIcon("resources/align-centered.png"));
        alignMiddle.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                value.setText("center");
                setValues();
            }
        });
        JMenuItem alignBottom = new JMenuItem(Language.translate("Bottom"), Workspace.createImageIcon("resources/align-bottom.png"));
        alignBottom.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                value.setText("bottom");
                setValues();
            }
        });

        final JPopupMenu popupMenu = new JPopupMenu();

        popupMenu.add(alignTop);
        popupMenu.add(alignMiddle);
        popupMenu.add(alignBottom);
        return popupMenu;
    }

    public JPopupMenu loadRelativeMenu(final JTextField value) {
        final JPopupMenu popup = new JPopupMenu();

        for (int i = 0; i <= 10; i++) {
            final String strValue = "" + (i / 10.0);
            JMenuItem menuItem = new JMenuItem(strValue);
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    value.setText(strValue);
                    setValues();
                }
            });
            popup.add(menuItem);
        }

        return popup;
    }

    public JPopupMenu loadPixelsMenu(final JTextField value) {
        final JPopupMenu popup = new JPopupMenu();

        for (int i = 0; i <= 1000; i += 100) {
            final String strValue = "" + (i);
            JMenuItem menuItem = new JMenuItem(strValue);
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    value.setText(strValue);
                    setValues();
                }
            });
            popup.add(menuItem);
        }

        return popup;
    }

    public JPopupMenu loadRotationMenu(final JTextField value) {
        final JPopupMenu popup = new JPopupMenu();

        for (int i = 0; i <= 360; i += 45) {
            final String strValue = "" + (i);
            JMenuItem menuItem = new JMenuItem(strValue + "\u00b0");
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    value.setText(strValue);
                    setValues();
                }
            });
            popup.add(menuItem);
        }

        return popup;
    }

    public JPopupMenu loadLineStyleMenu(final JTextField value) {
        final JPopupMenu popup = new JPopupMenu();

        for (int i = 0; i < StrokeCombo.strokeIDs.length; i++) {
            final String strStroke = StrokeCombo.strokeIDs[i];
            JMenuItem menuItem = new JMenuItem(StrokeCombo.createImageIcon(ColorToolbar.getStroke(strStroke, 3.0f), 70, 20));
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    value.setText(strStroke);
                    setValues();
                }
            });
            popup.add(menuItem);
        }
        return popup;
    }

    public JPopupMenu loadMenu(final JTextField value, String items[]) {
        final JPopupMenu popup = new JPopupMenu();

        for (int i = 0; i < items.length; i++) {
            final String strValue = items[i];
            JMenuItem menuItem = new JMenuItem(strValue);
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    value.setText(strValue);
                    setValues();
                }
            });
            popup.add(menuItem);
        }

        return popup;
    }

    public JPopupMenu loadMenu(final JTextField value, String items[], Hashtable<String, String[]> subMenus) {
        final JPopupMenu popup = new JPopupMenu();

        for (int i = 0; i < items.length; i++) {
            final String strValue = items[i];
            JMenuItem menuItem = new JMenuItem(strValue);
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    value.setText(strValue);
                    setValues();
                }
            });
            popup.add(menuItem);
        }
        if (subMenus != null) {
            for (String strSubMenu : subMenus.keySet()) {
                JMenu menu = new JMenu(strSubMenu);
                String subItems[] = subMenus.get(strSubMenu);
                for (int i = 0; i < subItems.length; i++) {
                    final String strValue = subItems[i];
                    JMenuItem menuItem = new JMenuItem(strValue);
                    menuItem.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent event) {
                            value.setText(strValue);
                            setValues();
                        }
                    });
                    menu.add(menuItem);
                }
                popup.add(menu);
            }
        }

        return popup;
    }

    public void loadValues() {
        loadValue(properties1, value1);
        loadValue(properties2, value2);
        loadValue(properties3, value3);
    }

    public void loadValue(JComboBox combo, JTextField value) {
        //String strRegion = (String) this.regions.getSelectedItem();
        String strProperty = (String) combo.getSelectedItem();
        String strValue = "";
        if (strProperty != null) {
            try {
                //int n = Integer.parseInt(strRegion);
                region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getLastSelectedRegion();//SketchletEditor.editorPanel.currentSketch.regions.getRegionByNumber(n);
                if (region != null) {
                    strValue = region.getProperty(strProperty);
                    enableControls(true, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            enableControls(false, false);
        }

        value.setText(strValue);
    }

    public void refreshRegions(ActiveRegion region) {
        this.region = region;
    }

    static String[][] regionProperties = new String[][]{
            {"Name", ""},
            {"Type", "type"},
            {"", ""},
            {"Text", ""},
            {"", ""},
            {"Image URL", ""},
            {"Image frame", ""},
            {"", ""},
            {"Position x", "pixels"},
            {"Position y", "pixels"},
            {"Relative x", "relative"},
            {"Relative y", "relative"},
            {"Trajectory position", "relative"},
            {"", ""},
            {"Width", "pixels"},
            {"Height", "pixels"},
            {"Rotation", "rotation"},
            {"", ""},
            {"Transparency", "transparency"},
            {"", ""},
            {"Shape", "shape"},
            {"", ""},
            {"Fill Color", "colors"},
            {"", ""},
            {"Line Style", "line style"},
            {"Line Thickness", "thickness"},
            {"Line Color", "colors"},
            {"", ""},
            {"Font Name", "font"},
            {"Font Style", "font style"},
            {"Font Size", "font size"},
            {"Text Color", "colors"},
            {"", ""},
            {"Horizontal Alignment", "horizontal alignment"},
            {"Vertical Alignment", "vertical alignment"},
            {"", ""},
            {"Visible area x", "pixels"},
            {"Visible area y", "pixels"},
            {"Visible area width", "pixels"},
            {"Visible area height", "pixels"},
            {"", ""},
            {"Speed", "pixels"},
            {"Direction", "rotation"},
            {"", ""},
            {"Pen Thickness", "thickness"},
            {"", ""},
            {"Horizontal 3d Rotation", "rotation"},
            {"Vertical 3d Rotation", "rotation"},
            {"", ""},
            {"Shear x", "relative"},
            {"Shear y", "relative"},
            {"", ""},
            {"Perspective x1", "relative"},
            {"Perspective y1", "relative"},
            {"Perspective x2", "relative"},
            {"Perspective y2", "relative"},
            {"Perspective x3", "relative"},
            {"Perspective y3", "relative"},
            {"Perspective x4", "relative"},
            {"Perspective y4", "relative"},
            {"Automatic perspective", "auto perspective"},
            {"Perspective depth", "relative"},};
}
