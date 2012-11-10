/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.dnd;

import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.help.TutorialPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author zobrenovic
 */
public class SelectDropAction extends JDialog {

    JTextField textArea = new JTextField(15);
    String[] transformations = new String[]{
            "Region text",
            "Image URL",
            "Image frame",
            "Position x",
            "Position y",
            "Relative x",
            "Relative y",
            "Trajectory position",
            "Width",
            "Height",
            "Rotation",
            "Shear x",
            "Shear y",
            "Visible area x",
            "Visible area y",
            "Visible area width",
            "Visible area height",
            "Transparency",
            "Speed",
            "Direction",
            //"Rotation speed",
            "Pen thickness",
            "Perspective x1",
            "Perspective y1",
            "Perspective x2",
            "Perspective y2",
            "Perspective x3",
            "Perspective y3",
            "Perspective x4",
            "Perspective y4",
            "Automatic perspective",
            "Perspective depth",
            "Horizontal 3d rotation",
            "Vertical 3d rotation"};
    JButton okButton = new JButton("OK", Workspace.createImageIcon("resources/ok.png"));
    JButton cancelButton = new JButton("Cancel", Workspace.createImageIcon("resources/cancel.png"));

    public SelectDropAction(JFrame frame, String strText, final net.sf.sketchlet.designer.data.ActiveRegion region) {
        super(frame);
        setModal(true);
        setTitle("Set Region Property");

        final JComboBox combo = new JComboBox();
        for (int i = 0; i < transformations.length; i++) {
            combo.addItem(transformations[i]);
        }

        final int _n = combo.getItemCount();

        combo.addItem("Line Style");
        combo.addItem("Line Thickness");
        combo.addItem("Line Color");
        combo.addItem("Fill Color");
        combo.addItem("Font Name");
        combo.addItem("Font Style");
        combo.addItem("Font Size");
        combo.addItem("Text Color");

        JPanel buttons = new JPanel();
        buttons.add(okButton);
        buttons.add(cancelButton);

        this.getRootPane().setDefaultButton(okButton);

        final JDialog thisDialog = this;

        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (region != null) {
                    String strText = textArea.getText();
                    TutorialPanel.addLine("cmd", "Set the region property '" + combo.getSelectedItem() + "' to '" + strText + "' and press OK", "arrow_cursor.png", thisDialog);
                    int index = combo.getSelectedIndex();
                    if (index >= 0) {
                        if (index == 0) {
                            region.strText = strText;
                            /*} else if (index == 1) {
                            region.htmlSpecification.setText(strText);*/
                        } else if (index == 1) {
                            //region.imageUrlField.setSelectedItem(strText);
                            region.strImageUrlField = strText;
                        } else if (index == 2) {
                            // region.imageIndex.setSelectedItem(strText);
                            region.strImageIndex = strText;
                        } else if (index < _n) {
                            String strItem = (String) combo.getSelectedItem();
                            region.setProperty(strItem, strText);
                        } else {
                            String strItem = (String) combo.getSelectedItem();
                            if (strItem.equalsIgnoreCase("Line Style")) {
                                /*if (region.lineStyle != null) {
                                region.lineStyle.setSelectedItem(strText);
                                }*/
                                region.strLineStyle = strText;
                            }
                            if (strItem.equalsIgnoreCase("Line Thickness")) {
                                /*if (region.lineThickness != null) {
                                region.lineThickness.setSelectedItem(strText);
                                }*/
                                region.strLineThickness = strText;
                            }
                            if (strItem.equalsIgnoreCase("Line Color")) {
                                /*if (region.lineColor != null) {
                                region.lineColor.setSelectedItem(strText);
                                }*/

                                region.strLineColor = strText;
                            }
                            if (strItem.equalsIgnoreCase("Fill Color")) {
                                /*if (region.fillColor != null) {
                                region.fillColor.setSelectedItem(strText);
                                }*/

                                region.strFillColor = strText;
                            }
                            if (strItem.equalsIgnoreCase("Font Name")) {
                                /*if (region.fontListCombo != null) {
                                region.fontListCombo.setSelectedItem(strText);
                                }*/
                                region.fontName = strText;
                            }
                            if (strItem.equalsIgnoreCase("Font Style")) {
                                /*if (region.styleCombo != null) {
                                region.styleCombo.setSelectedItem(strText);
                                }*/
                                region.fontStyle = strText;
                            }
                            if (strItem.equalsIgnoreCase("Font Size")) {
                                /*if (region.fontSizeCombo != null) {
                                region.fontSizeCombo.setSelectedItem(strText);
                                }*/
                                region.fontSize = strText;
                            }
                            if (strItem.equalsIgnoreCase("Text Color")) {
                                /*if (region.fontColorCombo != null) {
                                region.fontColorCombo.setSelectedItem(strText);
                                }*/
                                region.fontColor = strText;
                            }

                        }
                    }
                }
                setVisible(false);
            }
        });
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                setVisible(false);
            }
        });

        textArea.setText(strText);
        //textArea.setFont(new Font("Arial", Font.PLAIN, 9));
        // textArea.setEditable(false);
        //textArea.setLineWrap(true);
        //textArea.setWrapStyleWord(true);

        JPanel panel = new JPanel();
        panel.add(new JLabel("Set "));
        panel.add(combo, BorderLayout.NORTH);
        panel.add(new JLabel(" to "));
        panel.add(textArea);

        add(panel, BorderLayout.NORTH);
        add(buttons, BorderLayout.CENTER);

        pack();
        if (frame != null) {
            this.setLocationRelativeTo(frame);
        }
        setVisible(true);
    }

    public static void main(String args[]) {
        new SelectDropAction(null, "=test", null);
    }
}
