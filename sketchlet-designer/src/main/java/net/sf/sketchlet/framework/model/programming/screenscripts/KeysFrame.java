package net.sf.sketchlet.framework.model.programming.screenscripts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class KeysFrame extends JDialog implements KeyListener {

    private TextField textField;
    private String text = "";

    public KeysFrame(JFrame frame) {
        setTitle("Type Keys");
        this.setModal(true);
        setLayout(new BorderLayout());
        Panel p = new Panel();
        textField = new TextField(35);
        textField.addKeyListener(this);
        p.add(textField);
        add(p);

        JButton btnClear = new JButton("Clear");
        JButton btnYes = new JButton("OK");
        JButton btnNo = new JButton("Cancel");

        btnClear.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setText("");
                textField.setText(getText());
                textField.requestFocus();
            }
        });
        btnYes.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setVisible(false);
            }
        });
        btnNo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setText(null);
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

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        setText(getText() + "+" + e.getKeyCode() + " ");
        textField.setText(getText());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        setText(getText() + "-" + e.getKeyCode() + " ");
        textField.setText(getText());
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}