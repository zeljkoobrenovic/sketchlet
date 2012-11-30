package net.sf.sketchlet.designer.editor.ui.region;

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
public class LayerRegionsCheckBoxTabComponent extends JPanel {

    JCheckBox button;
    JLabel label;

    public LayerRegionsCheckBoxTabComponent(final JTabbedPane tabs) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setOpaque(false);

        button = new JCheckBox("", SketchletEditor.getInstance().getCurrentPage().isRegionsLayerActive());
        button.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        button.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (SketchletEditor.getInstance().getTool() != null) {
                    SketchletEditor.getInstance().getTool().deactivate();
                }
                SketchletEditor.getInstance().getCurrentPage().setRegionsLayer(button.isSelected());
                setAppearance();
                SketchletEditor.getInstance().repaintEverything();
            }
        });
        label = new JLabel();//((layer >= 9) ? " " : "  ") + (layer + 1));
        label.setToolTipText("Active Regions Layer");
        label.setIcon(Workspace.createImageIcon("resources/region_small.gif"));
        label.putClientProperty("JComponent.sizeVariant", "small");
        label.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                tabs.setSelectedIndex(tabs.getTabCount() - 1);
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
        label.setEnabled(true);
    }
}

