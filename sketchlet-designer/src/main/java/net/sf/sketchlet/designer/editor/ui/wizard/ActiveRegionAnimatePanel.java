/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.ui.wizard;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.communicator.server.Variable;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.model.ActiveRegion;
import net.sf.sketchlet.model.programming.timers.Timer;
import net.sf.sketchlet.model.programming.timers.Timers;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * @author zobrenovic
 */
public class ActiveRegionAnimatePanel extends JPanel {
    private static final Logger log = Logger.getLogger(ActiveRegionAnimatePanel.class);

    public Object[][] transformations;
    ActiveRegion region;
    JComboBox regionCombo;
    ActionParamPage paramPage;
    JComboBox timerCombo = new JComboBox();
    JButton editTimer = new JButton(Language.translate("edit..."));
    public Timer timer;
    public JCheckBox restartCheck = new JCheckBox("Restart variables on sketch entry and exit", true);

    public ActiveRegionAnimatePanel(ActionParamPage paramPage) {
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
                    log.error("regionCombo::actionPerformed()", e);
                }
            }
        });

        JPanel regionPanel = new JPanel(new BorderLayout());
        regionPanel.add(new JLabel("Select the region and set its parameters."), BorderLayout.NORTH);
        JPanel comboPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        comboPanel1.add(new JLabel("Animate the region: "));
        comboPanel1.add(regionCombo);
        JPanel comboPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timerCombo.setEditable(true);
        timerCombo.addItem("");

        for (Timer t : Timers.getGlobalTimers().getTimers()) {
            timerCombo.addItem(t.getName());
        }
        timerCombo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                enableControls();
                int index = timerCombo.getSelectedIndex() - 1;
                if (index >= 0) {
                    timer = Timers.getGlobalTimers().getTimers().elementAt(index);
                }
            }
        });
        comboPanel2.add(new JLabel("Using timer: "));
        comboPanel2.add(timerCombo);
        JButton addTimer = new JButton("new timer...");
        addTimer.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                timer = Timers.getGlobalTimers().addNewTimer();
                timerCombo.addItem(timer.getName());
                timerCombo.setSelectedItem(timer.getName());
                enableControls();
            }
        });
        comboPanel2.add(addTimer);
        editTimer.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                String strTimer = (String) timerCombo.getSelectedItem();
                int index = timerCombo.getSelectedIndex() - 1;
                if (strTimer != null) {
                    SketchletEditor.getInstance().getExtraEditorPanel().timersExtraPanel.showTimers(strTimer);
                    if (index >= 0) {
                        timerCombo.removeAllItems();
                        timerCombo.addItem("");

                        for (Timer t : Timers.getGlobalTimers().getTimers()) {
                            timerCombo.addItem(t.getName());
                        }
                        timerCombo.setSelectedItem(Timers.getGlobalTimers().getTimers().elementAt(index).getName());
                    }
                }
            }
        });
        comboPanel2.add(editTimer);

        JPanel comboPanel3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        comboPanel3.add(restartCheck);

        JPanel comboPanel = new JPanel(new BorderLayout());
        comboPanel.add(comboPanel1, BorderLayout.NORTH);
        comboPanel.add(comboPanel2, BorderLayout.CENTER);
        comboPanel.add(comboPanel3, BorderLayout.SOUTH);

        regionPanel.add(comboPanel, BorderLayout.CENTER);

        add(regionPanel, BorderLayout.NORTH);
        enableControls();
    }

    JPanel centerPanel;
    JScrollPane scrollCenterPanel;

    public void enableControls() {
        editTimer.setEnabled(timerCombo.getSelectedIndex() > 0);
    }

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
        centerPanel.add(new JLabel("Start"));
        centerPanel.add(new JLabel("End"));
        centerPanel.add(new JLabel("Variable"));

        for (int i = 0; i < transformations.length; i++) {
            final JCheckBox checkBox = (JCheckBox) transformations[i][0];
            final JTextField fieldStart = (JTextField) transformations[i][1];
            final JTextField fieldEnd = (JTextField) transformations[i][2];
            final JTextField field = (JTextField) transformations[i][3];

            checkBox.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    field.setEnabled(checkBox.isSelected());
                    fieldStart.setEnabled(checkBox.isSelected());
                    fieldEnd.setEnabled(checkBox.isSelected());
                }
            });

            field.setEnabled(false);
            fieldStart.setEnabled(false);
            fieldEnd.setEnabled(false);

            centerPanel.add(checkBox);
            centerPanel.add(fieldStart);
            centerPanel.add(fieldEnd);
            centerPanel.add(field);
        }
        scrollCenterPanel = new JScrollPane(centerPanel);
        add(scrollCenterPanel);
        scrollCenterPanel.setEnabled(false);
    }

    public void prepareControls() {
        int w = 0;
        int h = 0;
        int nAdd = 1;
        if (region != null) {
            w = region.x2 - region.x1;
            h = region.y2 - region.y1;
            nAdd = region.additionalImageFile.size();
        }
        transformations = new Object[][]{
                {new JCheckBox("image frame"), new JTextField("1"), new JTextField("" + (1 + (nAdd == 0 ? 0 : nAdd + 0.999))), new JTextField(getVariableName("image frame"))},
                {new JCheckBox("position x"), new JTextField("0"), new JTextField("500"), new JTextField(getVariableName("position x"))},
                {new JCheckBox("position y"), new JTextField("0"), new JTextField("500"), new JTextField(getVariableName("position y"))},
                {new JCheckBox("relative x"), new JTextField("0.0"), new JTextField("1.0"), new JTextField(getVariableName("relative x"))},
                {new JCheckBox("relative y"), new JTextField("0.0"), new JTextField("1.0"), new JTextField(getVariableName("relative y"))},
                {new JCheckBox("trajectory position"), new JTextField("0.0"), new JTextField("1.0"), new JTextField(getVariableName("trajectory position"))},
                {new JCheckBox("width"), new JTextField("0"), new JTextField("" + InteractionSpace.getPhysicalX(w)), new JTextField(getVariableName("width"))},
                {new JCheckBox("height"), new JTextField("0"), new JTextField("" + InteractionSpace.getPhysicalY(h)), new JTextField(getVariableName("height"))},
                {new JCheckBox("rotation"), new JTextField("" + InteractionSpace.getAngleStart()), new JTextField("" + InteractionSpace.getAngleEnd()), new JTextField(getVariableName("rotation"))},
                {new JCheckBox("shear x"), new JTextField("0.0"), new JTextField("1.0"), new JTextField(getVariableName("shear x"))},
                {new JCheckBox("shear y"), new JTextField("0.0"), new JTextField("1.0"), new JTextField(getVariableName("shear y"))},
                {new JCheckBox("visible area x"), new JTextField("0"), new JTextField("500"), new JTextField(getVariableName("visible area x"))},
                {new JCheckBox("visible area y"), new JTextField("0"), new JTextField("500"), new JTextField(getVariableName("visible area y"))},
                {new JCheckBox("visible area width"), new JTextField("0"), new JTextField("" + InteractionSpace.getPhysicalX(w)), new JTextField(getVariableName("visible area width"))},
                {new JCheckBox("visible area height"), new JTextField("0"), new JTextField("" + InteractionSpace.getPhysicalY(h)), new JTextField(getVariableName("visible area height"))},
                {new JCheckBox("transparency"), new JTextField("0.0"), new JTextField("1.0"), new JTextField(getVariableName("transparency"))},
                {new JCheckBox("speed"), new JTextField("0"), new JTextField("100"), new JTextField(getVariableName("speed"))},
                {new JCheckBox("direction"), new JTextField("" + InteractionSpace.getAngleStart()), new JTextField("" + InteractionSpace.getAngleEnd()), new JTextField(getVariableName("direction"))},
                //{new JCheckBox("rotation speed"), new JTextField("0"), new JTextField("" + InteractionSpace.angleEnd), new JTextField(getVariableName("rotation speed"))},
                {new JCheckBox("pen thickness"), new JTextField("0"), new JTextField("10"), new JTextField(getVariableName("pen thickness"))},};
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
        if (this.timerCombo.getSelectedItem().toString().length() == 0) {
            return "Select or create a timer";
        }
        for (int i = 0; i < this.transformations.length; i++) {
            if (((JCheckBox) this.transformations[i][0]).isSelected()) {
                return null;
            }
        }
        return "Select a region property";
    }
}
