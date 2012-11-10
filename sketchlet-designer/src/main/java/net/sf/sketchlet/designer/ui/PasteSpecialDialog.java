/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author zobrenovic
 */
public class PasteSpecialDialog extends JDialog {

    public JTextField numCopies = new JTextField("1");
    public JTextField xOffset = new JTextField("10");
    public JTextField yOffset = new JTextField("10");

    JButton okButton = new JButton("Paste");
    JButton cancelButton = new JButton("Cancel");

    public boolean bCopy = false;

    public PasteSpecialDialog(JFrame frame) {
        super(frame, true);
        setTitle("Paste Region(s)");

        setLayout(new GridLayout(4, 2));

        this.getRootPane().setDefaultButton(okButton);

        add(new JLabel("  Number of copies:"));
        add(this.numCopies);
        add(new JLabel("  X offset:"));
        add(this.xOffset);
        add(new JLabel("  Y offset:"));
        add(this.yOffset);
        add(this.okButton);
        add(this.cancelButton);

        pack();
        this.setLocationRelativeTo(frame);
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                bCopy = true;
                setVisible(false);
            }
        });
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                bCopy = false;
                setVisible(false);
            }
        });

        setVisible(true);
    }

    public static void main(String args[]) {
        new PasteSpecialDialog(null);
    }
}
