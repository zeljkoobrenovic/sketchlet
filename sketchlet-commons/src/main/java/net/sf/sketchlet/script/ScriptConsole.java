/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.script;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 *
 * @author cuypers
 */
public class ScriptConsole extends JFrame {

    public JTextArea textArea = new JTextArea(4, 80);
    public static ScriptConsole console = new ScriptConsole();
    JButton clearButton;

    public ScriptConsole() {
        super("Script Console");
        //setIconImage(Utils.createImageIcon("images/history.gif", "").getImage());
        this.clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                textArea.setText("");
            }
        });
        JPanel buttons = new JPanel();
        buttons.add(this.clearButton);

        Font font = new Font("Verdana", Font.PLAIN, 9);
        textArea.setFont(font);
        textArea.setForeground(Color.RED);

        textArea.setLineWrap(true);

        //JScrollPane scrollPane = new JScrollPane(this.textArea);

        //this.getContentPane().add(scrollPane, BorderLayout.CENTER);
        this.getContentPane().add(buttons, BorderLayout.SOUTH);
    }

    public static void showConsole() {
        if (ScriptConsole.console == null) {
            ScriptConsole.console = new ScriptConsole();
        }

        ScriptConsole.console.pack();
        ScriptConsole.console.setVisible(!ScriptConsole.console.isVisible());
    }

    public static void main(String args[]) {
        ScriptConsole.showConsole();
        ;
    }

    public static void addLine(String line) {
        if (ScriptConsole.console == null) {
            ScriptConsole.console = new ScriptConsole();
        }

        ScriptConsole.console.textArea.append(line + "\n");
        ScriptConsole.console.textArea.setSelectionStart(ScriptConsole.console.textArea.getText().length() - line.length());
        ScriptConsole.console.textArea.setSelectionEnd(ScriptConsole.console.textArea.getText().length());
    }
}
