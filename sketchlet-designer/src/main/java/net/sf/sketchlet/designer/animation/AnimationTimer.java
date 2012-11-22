/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.animation;

import net.sf.sketchlet.blackboard.VariablesBlackboard;
import net.sf.sketchlet.blackboard.Variable;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.model.ActiveRegion;
import net.sf.sketchlet.model.events.MouseEventMacro;
import net.sf.sketchlet.model.MouseProcessor;
import net.sf.sketchlet.model.programming.macros.Commands;
import net.sf.sketchlet.model.programming.timers.Timer;
import net.sf.sketchlet.model.programming.timers.Timers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * @author zobrenovic
 */
public class AnimationTimer extends JDialog {

    private JButton okButton = new JButton("Create/Select Timer...", Workspace.createImageIcon("resources/timer.png"));
    private JButton cancelButton = new JButton("Cancel", Workspace.createImageIcon("resources/process-stop.png"));
    private JButton btnStart = new JButton("from region");
    private JButton btnEnd = new JButton("from region");
    private Object[][] transformations;
    private ActiveRegion action;
    private JComboBox startTimer = new JComboBox();
    private JCheckBox initVars = new JCheckBox("Init variables", false);

    public AnimationTimer(final ActiveRegion action, JFrame frame) {
        super(frame, false);
        this.setAlwaysOnTop(true);
        this.setIconImage(Workspace.createImageIcon("resources/timer.png").getImage());
        this.action = action;
        setTitle("Define Animation Timer");

        prepareControls();

        setLayout(new BorderLayout());
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(transformations.length + 2, 2));

        centerPanel.add(new JLabel("  Dimension"));
        centerPanel.add(new JLabel("  Start Value"));
        centerPanel.add(new JLabel("  End Value"));
        centerPanel.add(new JLabel("  Variable"));
        centerPanel.add(new JLabel(" "));
        centerPanel.add(btnStart);
        centerPanel.add(btnEnd);
        centerPanel.add(new JLabel(" "));

        startTimer.addItem("");
        startTimer.addItem("Page Entry");

        for (int i = 0; i < MouseProcessor.MOUSE_EVENT_TYPES.length; i++) {
            startTimer.addItem(MouseProcessor.MOUSE_EVENT_TYPES[i]);
        }

        for (int i = 0; i < transformations.length; i++) {
            final JCheckBox checkBox = (JCheckBox) transformations[i][0];
            final JTextField field = (JTextField) transformations[i][3];
            final JTextField fieldStart = (JTextField) transformations[i][1];
            final JTextField fieldEnd = (JTextField) transformations[i][2];

            checkBox.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    enableControls();
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

        this.getRootPane().setDefaultButton(okButton);


        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel settingsPanel = new JPanel();
        settingsPanel.add(initVars);
        settingsPanel.add(new JLabel(" Start Timer On:"));
        settingsPanel.add(this.startTimer);
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(this.okButton);
        buttonsPanel.add(this.cancelButton);

        bottomPanel.add(new JLabel(""), BorderLayout.NORTH);
        bottomPanel.add(settingsPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonsPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        pack();

        final JDialog thisDialog = this;

        this.setLocationRelativeTo(frame);
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                final JPopupMenu popupMenu = new JPopupMenu();
                JMenuItem sketchMenuItem = new JMenuItem("Create New Timer...");
                sketchMenuItem.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        SketchletEditor.getInstance().showExtraEditorPanel();
                        Timer t = SketchletEditor.getInstance().getExtraEditorPanel().timersExtraPanel.newTimer();

                        addToTimer(t, transformations, action, initVars.isSelected());
                        connectVariables(t);

                        setVisible(false);
                    }
                });
                sketchMenuItem.setIcon(Workspace.createImageIcon("resources/timer.png"));
                popupMenu.add(sketchMenuItem);
                popupMenu.addSeparator();
                int i = 0;
                for (final Timer timer : Timers.getGlobalTimers().getTimers()) {
                    sketchMenuItem = new JMenuItem("Add to timer \"" + timer.getName() + "\"");
                    final int ti = i;
                    sketchMenuItem.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent ae) {
                            SketchletEditor.getInstance().showExtraEditorPanel();
                            SketchletEditor.getInstance().getExtraEditorPanel().timersExtraPanel.showTimers(ti);
                            addToTimer(timer, transformations, action, initVars.isSelected());
                            connectVariables(timer);
                            setVisible(false);
                        }
                    });
                    sketchMenuItem.setIcon(Workspace.createImageIcon("resources/timer.png"));
                    popupMenu.add(sketchMenuItem);
                    i++;
                }

                popupMenu.show(okButton, 0, okButton.getHeight());
            }
        });

        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                setVisible(false);
            }
        });

        btnStart.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                getFromRegion(1);
            }
        });

        btnEnd.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                getFromRegion(2);
            }
        });

        enableControls();

        setVisible(true);
    }

    public void getFromRegion(int index) {
        for (int i = 0; i < transformations.length; i++) {
            JCheckBox checkBox = (JCheckBox) transformations[i][0];
            JTextField field = (JTextField) transformations[i][index];
            int w = action.x2 - action.x1;
            int h = action.y2 - action.y1;

            if (checkBox.isSelected()) {
                if (i == 0) {
                } else if (i == 1) {
                    if (action.horizontalAlignment.equalsIgnoreCase("right")) {
                        field.setText("" + InteractionSpace.getPhysicalX(action.x2));
                    } else if (action.horizontalAlignment.equalsIgnoreCase("center")) {
                        field.setText("" + InteractionSpace.getPhysicalX(action.x1 + (action.x2 - action.x1) / 2));
                    } else {
                        field.setText("" + InteractionSpace.getPhysicalX(action.x1));
                    }
                } else if (i == 2) {
                    if (action.verticalAlignment.equalsIgnoreCase("bottom")) {
                        field.setText("" + InteractionSpace.getPhysicalY(action.y2));
                    } else if (action.verticalAlignment.equalsIgnoreCase("center")) {
                        field.setText("" + InteractionSpace.getPhysicalY(action.y1 + (action.y2 - action.y1) / 2));
                    } else {
                        field.setText("" + InteractionSpace.getPhysicalY(action.y1));
                    }
                } else if (i == 3) {
                    field.setText("" + ((index == 1) ? "0.0" : "1.0"));
                } else if (i == 4) {
                    field.setText("" + ((index == 1) ? "0.0" : "1.0"));
                } else if (i == 5) {
                    field.setText("" + ((index == 1) ? "0.0" : "1.0"));
                } else if (i == 6) {
                    field.setText("" + InteractionSpace.getPhysicalX(w));
                } else if (i == 7) {
                    field.setText("" + InteractionSpace.getPhysicalY(h));
                } else if (i == 8) {
                    field.setText("" + InteractionSpace.toPhysicalAngle(action.rotation));
                } else if (i == 9) {
                } else if (i == 10) {
                } else if (i == 11) {
                } else if (i == 12) {
                } else if (i == 13) {
                } else if (i == 14) {
                } else if (i == 15) {
                } else if (i == 16) {
                } else if (i == 17) {
                } else if (i == 18) {
                } else if (i == 19) {
                }
            }
        }
    }

    public void prepareControls() {
        int w = action.x2 - action.x1;
        int h = action.y2 - action.y1;
        int nAdd = action.additionalImageFile.size();
        Object[][] transformations = {
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

        this.transformations = transformations;
    }

    public void enableControls() {
        for (int i = 0; i < transformations.length; i++) {
            JCheckBox checkBox = (JCheckBox) transformations[i][0];

            if (checkBox.isSelected()) {
                this.okButton.setEnabled(true);
                return;
            }
        }
        this.okButton.setEnabled(false);
    }

    public String getVariableName(String strName) {
        String exisitingVariable = action.getProperty(strName).trim();
        if (exisitingVariable.startsWith("=") && exisitingVariable.length() > 1) {
            return VariablesBlackboard.populateTemplate(exisitingVariable.substring(1));
        }
        strName = strName.replace(' ', '_');

        Variable variable = VariablesBlackboard.getInstance().getVariable(strName);

        String strPrefix = strName;

        int i = 2;
        while (variable != null) {
            strName = strPrefix + "_" + i;
            i++;
            variable = VariablesBlackboard.getInstance().getVariable(strName);
        }

        return strName;
    }

    public static int addToTimer(Timer t, Object transformations[][], ActiveRegion region, boolean bInitVars) {

        int index = 0;

        for (int it = 0; it < t.getVariables().length; it++) {
            String strVar = (String) t.getVariables()[it][0];

            if (strVar.trim().length() > 0) {
                index++;
            } else {
                break;
            }
        }

        {
            JCheckBox checkBox = (JCheckBox) transformations[0][0];
            JTextField field = (JTextField) transformations[0][3];
            JTextField fieldStart = (JTextField) transformations[0][1];
            JTextField fieldEnd = (JTextField) transformations[0][2];

            if (checkBox.isSelected()) {
                t.getVariables()[index][0] = field.getText();
                t.getVariables()[index][1] = fieldStart.getText();
                t.getVariables()[index][2] = fieldEnd.getText();
                //region.imageIndex.setSelectedItem("=" + field.getText());
                region.strImageIndex = "=" + field.getText();
                index++;
            }
        }
        for (int i = 1; i < transformations.length; i++) {
            JCheckBox checkBox = (JCheckBox) transformations[i][0];
            JTextField field = (JTextField) transformations[i][3];
            JTextField fieldStart = (JTextField) transformations[i][1];
            JTextField fieldEnd = (JTextField) transformations[i][2];

            if (checkBox.isSelected() && index < t.getVariables().length) {
                t.getVariables()[index][0] = field.getText();
                t.getVariables()[index][1] = fieldStart.getText();
                t.getVariables()[index][2] = fieldEnd.getText();

                region.setProperty(checkBox.getText(), "=" + field.getText());

                index++;
            }
        }
        if (bInitVars) {
            initTimerVariables(region, transformations, t);
        }

        return index;
    }

    public static void initTimerVariables(ActiveRegion region, Object[][] transformations, Timer t) {
        SketchletEditor.getInstance().getPageDetailsPanel().pOnEntry.save();
        SketchletEditor.getInstance().getPageDetailsPanel().pOnExit.save();

        int macroStartIndex = 0;
        for (int ai = 0; ai < region.parent.getPage().getOnEntryMacro().getActions().length; ai++) {
            String strEvent = region.parent.getPage().getOnEntryMacro().getActions()[ai][0].toString();
            if (strEvent.trim().length() == 0) {
                macroStartIndex = ai;
                break;
            }
        }
        int macroStopIndex = 0;
        for (int ai = 0; ai < region.parent.getPage().getOnExitMacro().getActions().length; ai++) {
            String strEvent = region.parent.getPage().getOnExitMacro().getActions()[ai][0].toString();
            if (strEvent.trim().length() == 0) {
                macroStopIndex = ai;
                break;
            }
        }

        int index = 0;
        if (t != null) {
            for (int i = 0; i < transformations.length; i++) {
                JCheckBox checkBox = (JCheckBox) transformations[i][0];
                JTextField fieldStart = (JTextField) transformations[i][1];
                JTextField fieldEnd = (JTextField) transformations[i][2];
                JTextField field = (JTextField) transformations[i][3];

                if (checkBox.isSelected() && index < t.getVariables().length) {
                    t.getVariables()[index][0] = field.getText();
                    t.getVariables()[index][1] = fieldStart.getText();
                    t.getVariables()[index][2] = fieldEnd.getText();

                    region.parent.getPage().getOnEntryMacro().getActions()[macroStartIndex + index][0] = "Variable update";
                    region.parent.getPage().getOnEntryMacro().getActions()[macroStartIndex + index][1] = field.getText();
                    region.parent.getPage().getOnEntryMacro().getActions()[macroStartIndex + index][2] = fieldStart.getText();

                    region.parent.getPage().getOnExitMacro().getActions()[macroStopIndex + index][0] = "Variable update";
                    region.parent.getPage().getOnExitMacro().getActions()[macroStopIndex + index][1] = field.getText();
                    region.parent.getPage().getOnExitMacro().getActions()[macroStopIndex + index][2] = fieldStart.getText();

                    // VariablesBlackboard.variablesServer.updateVariable(field.getText(), fieldStart.getText());
                    Commands.updateVariableOrProperty(region, field.getText(), fieldStart.getText(), Commands.ACTION_VARIABLE_UPDATE);

                    index++;
                }
            }
        }

        SketchletEditor.getInstance().getPageDetailsPanel().pOnEntry.reload();
        SketchletEditor.getInstance().getPageDetailsPanel().pOnExit.reload();
    }

    public void connectVariables(Timer t) {
        int selStart = this.startTimer.getSelectedIndex();

        if (selStart == 0) {
            // Do nothing
        } else if (selStart == 1) {
            for (int ai = 0; ai < action.parent.getPage().getOnEntryMacro().getActions().length; ai++) {
                String strEvent = action.parent.getPage().getOnEntryMacro().getActions()[ai][0].toString();

                if (strEvent.trim().length() == 0) {
                    action.parent.getPage().getOnEntryMacro().getActions()[ai][0] = "Start Timer";
                    action.parent.getPage().getOnEntryMacro().getActions()[ai][1] = t.getName();

                    break;
                }
            }
        } else {
            String strMouse = (String) startTimer.getSelectedItem();
            for (MouseEventMacro mouseEventMacro : action.mouseProcessor.getMouseEventMacros()) {
                for (int ai = 0; ai < mouseEventMacro.getMacro().getActions().length; ai++) {
                    String strEvent = mouseEventMacro.getMacro().getActions()[ai][0].toString();

                    if (strEvent.trim().length() == 0) {
                        mouseEventMacro.getMacro().getActions()[ai][0] = strMouse;
                        mouseEventMacro.getMacro().getActions()[ai][1] = "Start Timer";
                        mouseEventMacro.getMacro().getActions()[ai][2] = t.getName();

                        break;
                    }
                }
            }
        }

    }
}
