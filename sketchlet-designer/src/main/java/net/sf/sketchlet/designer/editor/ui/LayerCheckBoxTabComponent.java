/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.ui;

import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author zobrenovic
 */
public class LayerCheckBoxTabComponent extends JPanel {

    int layer;
    JCheckBox button;
    JLabel label;

    public LayerCheckBoxTabComponent(final int layer, final JTabbedPane tabs) {
        //unset default FlowLayout' gaps
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.layer = layer;
        setOpaque(false);

        //tab button
        button = new JCheckBox("", SketchletEditor.getInstance().getCurrentPage().isLayerActive(layer));
        button.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        button.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (SketchletEditor.getInstance().getTool() != null) {
                    SketchletEditor.getInstance().getTool().deactivate();
                }
                SketchletEditor.getInstance().getCurrentPage().getLayerActive()[layer] = button.isSelected();
                if (SketchletEditor.getInstance().getCurrentPage().getLayerActive()[layer]) {
                    if (SketchletEditor.getInstance().getCurrentPage().getImages()[layer] == null) {
                        int w = Toolkit.getDefaultToolkit().getScreenSize().width;
                        int h = Toolkit.getDefaultToolkit().getScreenSize().height;
                        if (SketchletEditor.getInstance().getCurrentPage().getImages()[0] != null) {
                            w = SketchletEditor.getInstance().getCurrentPage().getImages()[0].getWidth();
                            h = SketchletEditor.getInstance().getCurrentPage().getImages()[0].getHeight();
                        }
                        SketchletEditor.getInstance().getCurrentPage().getImages()[layer] = Workspace.createCompatibleImage(w, h, SketchletEditor.getInstance().getCurrentPage().getImages()[layer]);
                    }
                }
                setAppearance();
                // tabs.setSelectedIndex(layer);
                SketchletEditor.getInstance().repaintEverything();
            }
        });
        label = new JLabel();//((layer >= 9) ? " " : "  ") + (layer + 1));
        label.setToolTipText("Drawing layer " + (layer + 1));
        label.setIcon(Workspace.createImageIcon("resources/pen_small.gif"));
        label.putClientProperty("JComponent.sizeVariant", "small");
        label.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                tabs.setSelectedIndex(layer);
            }
        });
        SwingUtilities.updateComponentTreeUI(label);
        button.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(label);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        add(button);
        add(new JLabel("  "));
        add(label);
        //add more space to the top of the component
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setAppearance();
    }

    public void setAppearance() {
        boolean bExists = SketchletEditor.getInstance().getCurrentPage().getLayerImageFile(layer).exists();

        if (!button.isSelected() && !bExists) {
            label.setEnabled(false);
        } else {
            label.setEnabled(true);
        }

    }
}
