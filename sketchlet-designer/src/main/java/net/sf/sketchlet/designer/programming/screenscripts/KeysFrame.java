/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.programming.screenscripts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class KeysFrame extends JDialog implements KeyListener {

    TextField t1;
    TextField l1;
    JFrame frame;

    public KeysFrame(JFrame frame) {
        this.frame = frame;
        setTitle("Type Keys");
        this.setModal(true);
        setLayout(new BorderLayout());
        Panel p = new Panel();
        l1 = new TextField(35);
        l1.addKeyListener(this);
        p.add(l1);
        add(p);

        JButton btnClear = new JButton("Clear");
        JButton btnYes = new JButton("OK");
        JButton btnNo = new JButton("Cancel");

        btnClear.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                strText = "";
                l1.setText(strText);
                l1.requestFocus();
            }
        });
        btnYes.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setVisible(false);
            }
        });
        btnNo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                strText = null;
                setVisible(false);
            }
        });

        JPanel panel = new JPanel();
        panel.add(btnClear);
        panel.add(new JLabel("    "));
        panel.add(btnYes);
        panel.add(btnNo);

        add(new JLabel("Type keys here:"), BorderLayout.NORTH);
        add(panel, BorderLayout.SOUTH);

        addKeyListener(this);
        pack();
        this.setLocationRelativeTo(frame);
        setVisible(true);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });
    }

    String strText = "";

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        strText += "+" + e.getKeyCode() + " ";
        l1.setText(strText);
    }

    public void keyReleased(KeyEvent e) {
        strText += "-" + e.getKeyCode() + " ";
        l1.setText(strText);
    }

    public static void main(String[] args) {
        new KeysFrame(null);
    }
}