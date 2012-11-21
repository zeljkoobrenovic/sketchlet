/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.loaders.pluginloader;

import javax.swing.*;

/**
 * @author zobrenovic
 */
public class PluginConsole {

    private static JTextArea console = new JTextArea();

    public static void appendToConsole(String strText) {
        PluginConsole.console.append(strText);
        int n = PluginConsole.console.getText().length() - 1;
        if (n >= 0) {
            PluginConsole.console.select(n, n);
        }
    }
}
