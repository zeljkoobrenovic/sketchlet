/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.ui.codegenerator;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.designer.GlobalProperties;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.plugin.CodeGenPluginSetting;
import net.sf.sketchlet.pluginloader.CodeGenPluginFactory;
import net.sf.sketchlet.util.SpringUtilities;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.List;

/**
 * @author zobrenovic
 */
public class CodeGeneratorUIUtils {

    public static JDialog getSettingsDialog(final CodeGeneratorPanel codegenPanel, List<CodeGenPluginSetting> settings) {
        if (settings == null) {
            return null;
        }
        final JDialog dlg = new JDialog();
        dlg.setModal(true);

        JPanel buttons = new JPanel();
        JButton btnOk = new JButton(Language.translate("OK"), Workspace.createImageIcon("resources/ok.png", ""));

        JButton btnCancel = new JButton(Language.translate("Cancel"), Workspace.createImageIcon("resources/cancel.png", ""));
        btnOk.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                codegenPanel.reload();
                dlg.setVisible(false);
            }
        });
        btnCancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                dlg.setVisible(false);
            }
        });
        dlg.getRootPane().setDefaultButton(btnOk);
        buttons.add(btnOk);
        buttons.add(btnCancel);

        dlg.add(getComponent(settings), BorderLayout.CENTER);
        dlg.add(buttons, BorderLayout.SOUTH);

        dlg.setTitle(CodeGenPluginFactory.platform + " Settings");
        dlg.setLocationRelativeTo(codegenPanel);
        dlg.pack();

        return dlg;
    }

    public static Component getComponent(List<CodeGenPluginSetting> settings) {
        JPanel panel = new JPanel(new SpringLayout());

        for (final CodeGenPluginSetting s : settings) {
            String oldValue = GlobalProperties.get(s.propName);
            if (oldValue == null) {
                oldValue = s.defaultValue;
            }
            if (s.possibleValues != null) {
                if (s.label != null && !s.label.isEmpty()) {
                    panel.add(new JLabel(s.label));
                } else {
                    panel.add(new JLabel(""));
                }
                final JComboBox combo = new JComboBox(s.possibleValues);
                combo.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        String strValue = (String) combo.getSelectedItem();
                        if (strValue != null) {
                            GlobalProperties.setAndSave(s.propName, strValue);
                        }
                    }
                });
                panel.add(combo);
                if (oldValue != null) {
                    combo.setSelectedItem(oldValue);
                }
            } else {
                if (s.type.equalsIgnoreCase("string")) {
                    if (s.label != null && !s.label.isEmpty()) {
                        panel.add(new JLabel(s.label));
                    } else {
                        panel.add(new JLabel(""));
                    }
                    final JTextField field = new JTextField();
                    if (oldValue != null) {
                        field.setText(oldValue);
                    }
                    field.addKeyListener(new KeyListener() {

                        public void keyTyped(KeyEvent e) {
                        }

                        public void keyPressed(KeyEvent e) {
                        }

                        public void keyReleased(KeyEvent e) {
                            save();
                        }

                        public void save() {
                            String strValue = (String) field.getText();
                            if (strValue != null) {
                                GlobalProperties.setAndSave(s.propName, strValue);
                            }
                        }
                    });

                    panel.add(field);
                } else if (s.type.equalsIgnoreCase("boolean")) {
                    final JCheckBox checkBox = new JCheckBox();
                    panel.add(new JLabel(""));
                    if (s.label != null && !s.label.isEmpty()) {
                        checkBox.setText(s.label);
                    }
                    if (oldValue != null) {
                        checkBox.setSelected(oldValue.equalsIgnoreCase("true"));
                    } else if (s.defaultValue != null) {
                        checkBox.setSelected(s.defaultValue.equalsIgnoreCase("true"));
                    }
                    checkBox.addChangeListener(new ChangeListener() {

                        public void stateChanged(ChangeEvent e) {
                            GlobalProperties.setAndSave(s.propName, checkBox.isSelected() + "");
                        }
                    });
                    panel.add(checkBox);
                } else if (s.type.equalsIgnoreCase("file") || s.type.equalsIgnoreCase("directory")) {
                    final JTextField fileField = new JTextField(15);
                    fileField.setEditable(false);
                    JButton btn = new JButton("...");
                    btn.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            final JFileChooser fc = new JFileChooser();
                            //In response to a button click:
                            int returnVal = fc.showOpenDialog(SketchletContext.getInstance().getEditorFrame());
                            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                            if (returnVal == JFileChooser.APPROVE_OPTION) {
                                final File file = fc.getSelectedFile();
                                fileField.setText(file.getAbsolutePath());
                                GlobalProperties.setAndSave(s.propName, file.getAbsolutePath());
                            }
                        }
                    });
                    panel.add(new JLabel(s.label));
                    JPanel panel2 = new JPanel();
                    panel2.add(fileField);
                    panel2.add(btn);
                    panel.add(panel2);
                } else {
                    panel.add(new JLabel(""));
                    panel.add(new JLabel(""));
                }
            }
        }

        SpringUtilities.makeCompactGrid(panel,
                settings.size(), 2, //rows, cols
                5, 5, //initialX, initialY
                5, 5);//xPad, yPad    }


        return panel;
    }
}
