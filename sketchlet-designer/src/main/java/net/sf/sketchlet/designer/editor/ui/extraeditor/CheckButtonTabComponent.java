package net.sf.sketchlet.designer.editor.ui.extraeditor;

import com.lowagie.text.Font;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.util.RefreshTime;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * @author zobrenovic
 */
public class CheckButtonTabComponent extends JPanel {

    ActiveRegion region;

    public CheckButtonTabComponent(final ActiveRegion region) {
        //unset default FlowLayout' gaps
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.region = region;
        setOpaque(false);

        final JToggleButton pin = new JToggleButton(Workspace.createImageIcon("resources/pin_small.png"));

        //tab button
        final JCheckBox button = new JCheckBox("", region.visible);
        button.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        button.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                region.visible = button.isSelected();
                SketchletEditor.getInstance().repaint();
                if (SketchletEditor.getInstance().getInternalPlaybackPanel() != null) {
                    SketchletEditor.getInstance().getInternalPlaybackPanel().repaint();
                }
                RefreshTime.update();
                SketchletEditor.getInstance().repaint();
                RefreshTime.update();
            }
        });
        // String strID = region.isBackgroundRegion() ? Language.translate("background region") : region.getNumber();
        String strID = region.getNumber();
        JLabel label = new JLabel(" " + strID + " ");
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        label.putClientProperty("JComponent.sizeVariant", "small");
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        SwingUtilities.updateComponentTreeUI(label);
        add(label);
        button.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(label);
        add(button);
        pin.setBorder(BorderFactory.createEmptyBorder(4, 5, 4, 5));
        add(pin);
        button.setToolTipText(Language.translate("Show/hide the region"));
        pin.setToolTipText(Language.translate("Pin the region"));
        JButton btnClose = new JButton(Workspace.createImageIcon("resources/close_small.png"));
        btnClose.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 4));
        btnClose.setToolTipText(Language.translate("Delete the region"));
        btnClose.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int n = JOptionPane.showConfirmDialog(SketchletEditor.editorFrame,
                        Language.translate("Are you sure you want to delete this region?"),
                        Language.translate("Delete Region"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (n == JOptionPane.YES_OPTION) {
                    SketchletEditor.getInstance().deleteSelectedRegion();
                }
            }
        });
        add(btnClose);


        pin.setSelected(region.pinned);
        if (region.pinned) {
            pin.setIcon(Workspace.createImageIcon("resources/pin_small_down.png"));
        } else {
            pin.setIcon(Workspace.createImageIcon("resources/pin_small.png"));
        }
        pin.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (pin.isSelected()) {
                    pin.setIcon(Workspace.createImageIcon("resources/pin_small_down.png"));
                } else {
                    pin.setIcon(Workspace.createImageIcon("resources/pin_small.png"));
                }
                region.pinned = pin.isSelected();
            }
        });

        //add more space to the top of the component
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    }
}
