/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.ui.codegenerator;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author zobrenovic
 */
public class CodeGeneratorDialog {

    private static JDialog frame = null;

    public static void showFrame(JFrame parent) {
        SketchletEditor.editorFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (frame != null) {
            CodeGeneratorPanel.getCodeGeneratorPanel().reload();
            frame.setVisible(true);
            SketchletEditor.editorFrame.setCursor(Cursor.getDefaultCursor());
            return;
        }
        JTabbedPane tabs = new JTabbedPane();
        tabs.setTabPlacement(JTabbedPane.LEFT);
        JButton close = new JButton(Language.translate("Close"), Workspace.createImageIcon("resources/ok.png"));
        JToolBar buttonPanel = new JToolBar();
        buttonPanel.setFloatable(false);
        buttonPanel.add(close);
        close.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                CodeGeneratorDialog.frame.setVisible(false);
                CodeGeneratorPanel.getCodeGeneratorPanel().dispose();
                frame = null;
            }
        });
        frame = new JDialog(parent);
        frame.setIconImage(Workspace.createImageIcon("resources/help-browser2.png").getImage());
        frame.setTitle(Language.translate("Code Generator"));
        frame.setModal(false);

        tabs.addTab(Language.translate("Plugins"), new CodeGeneratorPanel(frame));

        frame.add(tabs);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setSize(800, 700);
        if (parent != null) {
            frame.setLocationRelativeTo(parent);
        }
        frame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                CodeGeneratorPanel.getCodeGeneratorPanel().dispose();
                frame = null;
            }
        });

        frame.getRootPane().setDefaultButton(close);
        frame.setVisible(true);
        SketchletEditor.editorFrame.setCursor(Cursor.getDefaultCursor());
    }

    public static void main(String args[]) {
        showFrame(null);
    }
}
