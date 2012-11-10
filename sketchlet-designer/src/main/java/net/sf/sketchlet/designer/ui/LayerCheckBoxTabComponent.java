/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui;

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
        button = new JCheckBox("", SketchletEditor.editorPanel.currentPage.isLayerActive(layer));
        button.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        button.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (SketchletEditor.editorPanel.tool != null) {
                    SketchletEditor.editorPanel.tool.deactivate();
                }
                SketchletEditor.editorPanel.currentPage.layerActive[layer] = button.isSelected();
                if (SketchletEditor.editorPanel.currentPage.layerActive[layer]) {
                    if (SketchletEditor.editorPanel.currentPage.images[layer] == null) {
                        int w = Toolkit.getDefaultToolkit().getScreenSize().width;
                        int h = Toolkit.getDefaultToolkit().getScreenSize().height;
                        if (SketchletEditor.editorPanel.currentPage.images[0] != null) {
                            w = SketchletEditor.editorPanel.currentPage.images[0].getWidth();
                            h = SketchletEditor.editorPanel.currentPage.images[0].getHeight();
                        }
                        SketchletEditor.editorPanel.currentPage.images[layer] = Workspace.createCompatibleImage(w, h, SketchletEditor.editorPanel.currentPage.images[layer]);
                    }
                }
                setAppearance();
                // tabs.setSelectedIndex(layer);
                SketchletEditor.editorPanel.repaintEverything();
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
        boolean bExists = SketchletEditor.editorPanel.currentPage.getLayerImageFile(layer).exists();

        if (!button.isSelected() && !bExists) {
            label.setEnabled(false);
        } else {
            label.setEnabled(true);
        }

    }
}
