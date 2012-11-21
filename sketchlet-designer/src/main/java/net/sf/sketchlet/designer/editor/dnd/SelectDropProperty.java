/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.dnd;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.model.Page;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author zobrenovic
 */
public class SelectDropProperty extends JDialog {

    private JTextField textField = new JTextField(12);
    private JButton okButton = new JButton("OK", Workspace.createImageIcon("resources/ok.png"));
    private JButton cancelButton = new JButton("Cancel", Workspace.createImageIcon("resources/cancel.png"));

    public SelectDropProperty(JFrame frame, final String strText, final Page page) {
        super(frame);
        setModal(true);
        setTitle("Set Page Property");

        final JComboBox combo = new JComboBox();
        for (int i = 0; i < page.getProperties().length; i++) {
            if (page.getProperties()[i][1] != null) {
                combo.addItem(page.getProperties()[i][0]);
            }
        }

        JPanel buttons = new JPanel();
        buttons.add(okButton);
        buttons.add(cancelButton);

        this.getRootPane().setDefaultButton(okButton);

        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                page.setProperty((String) combo.getSelectedItem(), textField.getText());
                setVisible(false);
            }
        });
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                setVisible(false);
            }
        });

        add(new JScrollPane(textField), BorderLayout.SOUTH);

        JPanel panelSet = new JPanel(new FlowLayout(FlowLayout.LEFT));
        textField.setText(strText);

        panelSet.add(new JLabel(Language.translate("Set ")));
        panelSet.add(combo);
        panelSet.add(new JLabel(Language.translate(" to ")));
        panelSet.add(textField);

        add(panelSet, BorderLayout.NORTH);

        add(buttons, BorderLayout.CENTER);

        pack();
        if (frame != null) {
            this.setLocationRelativeTo(frame);
        }
        setVisible(true);
    }

    public static void main(String args[]) {
        new SelectDropProperty(null, "=test", null);
    }
}
