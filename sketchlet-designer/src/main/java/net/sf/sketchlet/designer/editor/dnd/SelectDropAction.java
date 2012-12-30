package net.sf.sketchlet.designer.editor.dnd;

import net.sf.sketchlet.designer.Workspace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author zobrenovic
 */
public class SelectDropAction extends JDialog {

    private JTextField textArea = new JTextField(15);
    private String[] transformations = new String[]{
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
    private JButton okButton = new JButton("OK", Workspace.createImageIcon("resources/ok.png"));
    private JButton cancelButton = new JButton("Cancel", Workspace.createImageIcon("resources/cancel.png"));

    public SelectDropAction(JFrame frame, String strText, final net.sf.sketchlet.framework.model.ActiveRegion region) {
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
                    int index = combo.getSelectedIndex();
                    if (index >= 0) {
                        if (index == 0) {
                            region.setText(strText);
                        } else if (index == 1) {
                            region.setImageUrlField(strText);
                        } else if (index == 2) {
                            region.setImageIndex(strText);
                        } else if (index < _n) {
                            String strItem = (String) combo.getSelectedItem();
                            region.setProperty(strItem, strText);
                        } else {
                            String strItem = (String) combo.getSelectedItem();
                            if (strItem.equalsIgnoreCase("Line Style")) {
                                region.setLineStyle(strText);
                            }
                            if (strItem.equalsIgnoreCase("Line Thickness")) {
                                region.setLineThickness(strText);
                            }
                            if (strItem.equalsIgnoreCase("Line Color")) {
                                region.setLineColor(strText);
                            }
                            if (strItem.equalsIgnoreCase("Fill Color")) {
                                region.setFillColor(strText);
                            }
                            if (strItem.equalsIgnoreCase("Font Name")) {
                                region.setFontName(strText);
                            }
                            if (strItem.equalsIgnoreCase("Font Style")) {
                                region.setFontStyle(strText);
                            }
                            if (strItem.equalsIgnoreCase("Font Size")) {
                                region.setFontSize(strText);
                            }
                            if (strItem.equalsIgnoreCase("Text Color")) {
                                region.setFontColor(strText);
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
