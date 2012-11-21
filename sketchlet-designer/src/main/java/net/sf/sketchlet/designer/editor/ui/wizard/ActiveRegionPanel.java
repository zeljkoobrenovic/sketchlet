/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.ui.wizard;

import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.communicator.server.Variable;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.model.ActiveRegion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * @author zobrenovic
 */
public class ActiveRegionPanel extends JPanel {

    public Object[][] transformations;
    ActiveRegion region;
    JComboBox regionCombo;
    ActionParamPage paramPage;

    public ActiveRegionPanel(ActionParamPage paramPage) {
        this.paramPage = paramPage;
        prepareControls();
        setLayout(new BorderLayout());

        regionCombo = EventPage.createRegionCombo(false, SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().size());

        regionCombo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                try {
                    if (scrollCenterPanel != null) {
                        remove(scrollCenterPanel);
                        revalidate();
                    }
                    if (regionCombo.getSelectedItem() != null && !((String) regionCombo.getSelectedItem()).equals("")) {
                        int n = SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().size() - Integer.parseInt((String) regionCombo.getSelectedItem());

                        if (n >= 0 && n < SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().size()) {
                            region = SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().elementAt(n);
                            prepareControls();
                            addPropertiesPanel();
                            revalidate();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        JPanel regionPanel = new JPanel(new BorderLayout());
        regionPanel.add(new JLabel("Select the region and set its parameters."), BorderLayout.NORTH);
        JPanel comboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        comboPanel.add(new JLabel("Change the parameters of the region: "));
        comboPanel.add(regionCombo);
        regionPanel.add(comboPanel, BorderLayout.CENTER);

        add(regionPanel, BorderLayout.NORTH);
    }

    JPanel centerPanel;
    JScrollPane scrollCenterPanel;

    public void addPropertiesPanel() {
        if (centerPanel != null) {
            remove(centerPanel);
        }
        if (scrollCenterPanel != null) {
            remove(scrollCenterPanel);
        }
        centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(transformations.length + 2, 2));

        centerPanel.add(new JLabel("   Property"));
        centerPanel.add(new JLabel("Value"));
        centerPanel.add(new JLabel("Variable"));

        for (int i = 0; i < transformations.length; i++) {
            final JCheckBox checkBox = (JCheckBox) transformations[i][0];
            final JTextField field = (JTextField) transformations[i][2];
            final JTextField fieldStart = (JTextField) transformations[i][1];

            checkBox.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    field.setEnabled(checkBox.isSelected());
                    fieldStart.setEnabled(checkBox.isSelected());
                }
            });

            field.setEnabled(false);
            fieldStart.setEnabled(false);

            centerPanel.add(checkBox);
            centerPanel.add(fieldStart);
            centerPanel.add(field);
        }
        scrollCenterPanel = new JScrollPane(centerPanel);
        add(scrollCenterPanel);
    }

    public void prepareControls() {
        Object[][] transformations = {
                {new JCheckBox("image frame"), new JTextField("1"), new JTextField(getVariableName("image frame"))},
                {new JCheckBox("image URL"), new JTextField(""), new JTextField(getVariableName("image url"))},
                {new JCheckBox("position x"), new JTextField("0"), new JTextField(getVariableName("position x"))},
                {new JCheckBox("position y"), new JTextField("0"), new JTextField(getVariableName("position y"))},
                {new JCheckBox("relative x"), new JTextField("0.0"), new JTextField(getVariableName("relative x"))},
                {new JCheckBox("relative y"), new JTextField("0.0"), new JTextField(getVariableName("relative y"))},
                {new JCheckBox("trajectory position"), new JTextField("0.0"), new JTextField(getVariableName("trajectory position"))},
                {new JCheckBox("width"), new JTextField("0"), new JTextField(getVariableName("width"))},
                {new JCheckBox("height"), new JTextField("0"), new JTextField(getVariableName("height"))},
                {new JCheckBox("rotation"), new JTextField("" + InteractionSpace.getAngleStart()), new JTextField(getVariableName("rotation"))},
                {new JCheckBox("shear x"), new JTextField("0.0"), new JTextField(getVariableName("shear x"))},
                {new JCheckBox("shear y"), new JTextField("0.0"), new JTextField(getVariableName("shear y"))},
                {new JCheckBox("visible area x"), new JTextField("0"), new JTextField(getVariableName("visible area x"))},
                {new JCheckBox("visible area y"), new JTextField("0"), new JTextField(getVariableName("visible area y"))},
                {new JCheckBox("visible area width"), new JTextField("0"), new JTextField(getVariableName("visible area width"))},
                {new JCheckBox("visible area height"), new JTextField("0"), new JTextField(getVariableName("visible area height"))},
                {new JCheckBox("transparency"), new JTextField("0.0"), new JTextField(getVariableName("transparency"))},
                {new JCheckBox("speed"), new JTextField("0"), new JTextField(getVariableName("speed"))},
                {new JCheckBox("direction"), new JTextField("" + InteractionSpace.getAngleStart()), new JTextField(getVariableName("direction"))},
                //{new JCheckBox("rotation speed"), new JTextField("0"), new JTextField(getVariableName("rotation speed"))},
                {new JCheckBox("pen thickness"), new JTextField("0"), new JTextField(getVariableName("pen thickness"))},};
        this.transformations = transformations;
    }

    public String getVariableName(String strName) {
        if (region != null) {
            String exisitingVariable = region.getProperty(strName).trim();
            if (exisitingVariable != null && exisitingVariable.startsWith("=") && exisitingVariable.length() > 1) {
                return DataServer.populateTemplate(exisitingVariable);
            }
        }
        strName = strName.replace(' ', '_');

        if (DataServer.getInstance() != null) {
            Variable variable = DataServer.getInstance().getVariable(strName);

            String strPrefix = strName;

            int i = 2;
            while (variable != null) {
                strName = strPrefix + "_" + i;
                i++;
                variable = DataServer.getInstance().getVariable(strName);
            }
        }

        return strName;
    }

    protected String validateContents(Component comp, Object o) {
        if (regionCombo.getSelectedItem().toString().length() == 0) {
            return "Select a region";
        }
        for (int i = 0; i < this.transformations.length; i++) {
            if (((JCheckBox) this.transformations[i][0]).isSelected()) {
                return null;
            }
        }
        return "Select a region property";
    }
}
