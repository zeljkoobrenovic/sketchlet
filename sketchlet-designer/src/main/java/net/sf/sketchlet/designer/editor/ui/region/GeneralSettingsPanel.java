package net.sf.sketchlet.designer.editor.ui.region;

import net.sf.sketchlet.codegen.CodeGenUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.UIUtils;
import net.sf.sketchlet.designer.editor.ui.profiles.Profiles;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.util.RefreshTime;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * @author zeljko
 */
public class GeneralSettingsPanel extends JPanel {
    private ActiveRegion region;
    private JTextField name = new JTextField(15);
    private JTextField type = new JTextField();
    private JComboBox layerCombo = new JComboBox(new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"});
    private JComboBox active = new JComboBox();
    private JComboBox horizontalAlign = new JComboBox();
    private JComboBox verticalAlign = new JComboBox();
    private JCheckBox fitToBox = new JCheckBox(Language.translate(" Fit to box"), true);

    public GeneralSettingsPanel(ActiveRegion region) {
        this.region = region;
        init();
    }

    private void init() {
        if (Profiles.isActive("active_region_general")) {
            this.name.setText(region.getTrajectory1());
            this.name.addKeyListener(new KeyAdapter() {

                public void keyReleased(KeyEvent e) {
                    if (!region.getName().equals(name.getText())) {
                        SketchletEditor.getInstance().saveRegionUndo();
                        region.setName(name.getText());
                        RefreshTime.update();
                        SketchletEditor.getInstance().repaint();
                    }
                }
            });
            this.type.setText(region.getType());
            this.type.addKeyListener(new KeyAdapter() {

                public void keyReleased(KeyEvent e) {
                    if (!region.getType().equals(type.getText())) {
                        SketchletEditor.getInstance().saveRegionUndo();
                        region.setType(type.getText());
                        RefreshTime.update();
                        SketchletEditor.getInstance().repaint();
                    }
                }
            });
            fitToBox.setSelected(region.isFitToBoxEnabled());
            fitToBox.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    SketchletEditor.getInstance().saveRegionUndo();
                    region.setFitToBoxEnabled(fitToBox.isSelected());
                }
            });
            UIUtils.removeActionListeners(this.active);
            this.active.setSelectedItem(region.getActive());
            this.active.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    if (active.getSelectedItem() != null) {
                        if (!region.getActive().equals(active.getSelectedItem().toString())) {
                            SketchletEditor.getInstance().saveRegionUndo();
                            region.setActive((String) active.getSelectedItem());
                            RefreshTime.update();
                            SketchletEditor.getInstance().repaint();
                        }
                    }
                }
            });
            UIUtils.removeActionListeners(type);
            type.setText(region.getType());
            type.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    if (!region.getType().equals(type.getText())) {
                        SketchletEditor.getInstance().saveRegionUndo();
                        region.setType(type.getText());
                        RefreshTime.update();
                        SketchletEditor.getInstance().repaint();
                    }
                }
            });
            UIUtils.populateVariablesCombo(this.active, true, new String[]{"true", "false"});
            this.horizontalAlign.setSelectedItem(region.getHorizontalAlignment());
            this.horizontalAlign.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    if (horizontalAlign.getSelectedItem() != null && !region.getHorizontalAlignment().equals(horizontalAlign.getSelectedItem().toString())) {
                        SketchletEditor.getInstance().saveRegionUndo();
                        region.setHorizontalAlignment((String) horizontalAlign.getSelectedItem());
                        RefreshTime.update();
                        SketchletEditor.getInstance().repaint();
                    }
                }
            });
            this.verticalAlign.setSelectedItem(region.getVerticalAlignment());
            this.verticalAlign.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    if (verticalAlign.getSelectedItem() != null && !region.getVerticalAlignment().equals(verticalAlign.getSelectedItem().toString())) {
                        SketchletEditor.getInstance().saveRegionUndo();
                        region.setVerticalAlignment((String) verticalAlign.getSelectedItem());
                        RefreshTime.update();
                        SketchletEditor.getInstance().repaint();
                    }
                }
            });


            JPanel general1 = new JPanel(new FlowLayout());
            JPanel general2 = new JPanel(new FlowLayout());
            general1.add(new JLabel(Language.translate("Region Name: ")));
            general1.add(name);
            general1.add(new JLabel(Language.translate("Is Active: ")));
            general1.add(this.active);
            general1.add(new JLabel(Language.translate("  Type: ")));
            general1.add(type);
            final JButton btnType = new JButton(Workspace.createImageIcon("resources/menu.png"));
            btnType.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    JPopupMenu popup = UIUtils.loadMenu(type, CodeGenUtils.getControlTypeIDs(), CodeGenUtils.getExtraControls());
                    popup.show(btnType, 0, btnType.getHeight());
                    RefreshTime.update();
                    SketchletEditor.getInstance().repaint();
                }
            });
            general1.add(btnType);
            general2.add(new JLabel(Language.translate("Layer: ")));
            layerCombo.setSelectedIndex(region.getLayer());
            layerCombo.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    int index = layerCombo.getSelectedIndex();
                    if (index < 0 || index > 9) {
                        index = 0;
                    }
                    region.setLayer(index);
                    RefreshTime.update();
                    SketchletEditor.getInstance().repaint();
                }
            });
            general2.add(layerCombo);
            general2.add(new JLabel(Language.translate("    Horizontal alignment: "), JLabel.RIGHT));
            general2.add(this.horizontalAlign);

            general2.add(new JLabel(Language.translate("    Vertical alignment: "), JLabel.RIGHT));
            general2.add(this.verticalAlign);

            general2.add(new JLabel(""));
            general2.add(this.fitToBox);

            JPanel general = new JPanel(new BorderLayout());
            general.add(general1, BorderLayout.CENTER);
            general.add(general2, BorderLayout.SOUTH);
            JPanel generalWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
            generalWrapper.add(general);
            add(generalWrapper);
        }
    }

    public void refreshComponents() {
        UIUtils.refreshComboBox(this.active, region.getActive());
        UIUtils.refreshComboBox(this.horizontalAlign, region.getHorizontalAlignment());
        UIUtils.refreshComboBox(this.verticalAlign, region.getVerticalAlignment());
        this.name.setText(region.getTrajectory1());
        this.type.setText(region.getType());
        UIUtils.refreshCheckBox(fitToBox, region.isFitToBoxEnabled());
    }

    public void populateComboBoxes() {
        UIUtils.populateVariablesCombo(this.active, true, new String[]{"true", "false"});
        this.active.setPreferredSize(new Dimension(280, 30));
        type.setPreferredSize(new Dimension(100, 30));
        this.horizontalAlign.addItem("left");
        this.horizontalAlign.addItem("center");
        this.horizontalAlign.addItem("right");
        this.horizontalAlign.setSelectedIndex(0);

        this.verticalAlign.addItem("top");
        this.verticalAlign.addItem("center");
        this.verticalAlign.addItem("bottom");
        this.verticalAlign.setSelectedIndex(0);
    }

    public void updateUIControls() {
        active.setSelectedItem(active.getEditor().getItem());
    }
}
