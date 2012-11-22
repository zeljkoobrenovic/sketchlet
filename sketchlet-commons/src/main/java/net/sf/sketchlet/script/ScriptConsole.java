/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.script;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author cuypers
 */
public class ScriptConsole extends JFrame {

    private JTextArea textArea = new JTextArea(4, 80);
    private static ScriptConsole console = new ScriptConsole();
    private JButton clearButton;

    public ScriptConsole() {
        super("Script Console");
        //setIconImage(Utils.createImageIcon("images/history.gif", "").getImage());
        this.clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                getTextArea().setText("");
            }
        });
        JPanel buttons = new JPanel();
        buttons.add(this.clearButton);

        Font font = new Font("Verdana", Font.PLAIN, 9);
        getTextArea().setFont(font);
        getTextArea().setForeground(Color.RED);

        getTextArea().setLineWrap(true);

        //JScrollPane scrollPane = new JScrollPane(this.textArea);

        //this.getContentPane().add(scrollPane, BorderLayout.CENTER);
        this.getContentPane().add(buttons, BorderLayout.SOUTH);
    }

    public static void showConsole() {
        if (ScriptConsole.getConsole() == null) {
            ScriptConsole.setConsole(new ScriptConsole());
        }

        ScriptConsole.getConsole().pack();
        ScriptConsole.getConsole().setVisible(!ScriptConsole.getConsole().isVisible());
    }

    public static void main(String args[]) {
        ScriptConsole.showConsole();
        ;
    }

    public static void addLine(String line) {
        if (ScriptConsole.getConsole() == null) {
            ScriptConsole.setConsole(new ScriptConsole());
        }

        ScriptConsole.getConsole().getTextArea().append(line + "\n");
        ScriptConsole.getConsole().getTextArea().setSelectionStart(ScriptConsole.getConsole().getTextArea().getText().length() - line.length());
        ScriptConsole.getConsole().getTextArea().setSelectionEnd(ScriptConsole.getConsole().getTextArea().getText().length());
    }

    public static ScriptConsole getConsole() {
        return console;
    }

    public static void setConsole(ScriptConsole console) {
        ScriptConsole.console = console;
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    public void setTextArea(JTextArea textArea) {
        this.textArea = textArea;
    }
}
