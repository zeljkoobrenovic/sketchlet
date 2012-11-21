/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.ui.macros;

import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.help.HelpUtils;
import net.sf.sketchlet.model.programming.macros.Macro;
import net.sf.sketchlet.model.programming.macros.Macros;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author zobrenovic
 */
public class MacrosFrame extends JDialog {

    JTabbedPane tabs = new JTabbedPane();
    JButton newMacro = new JButton("New Macro", Workspace.createImageIcon("resources/macros.png", ""));
    JButton save = new JButton("Save", Workspace.createImageIcon("resources/ok.png", ""));
    JButton deleteMacroBtn = new JButton("Delete", Workspace.createImageIcon("resources/user-trash.png"));

    public MacrosFrame() {
        setTitle("Action Lists");
        this.setIconImage(Workspace.createImageIcon("resources/macros.png").getImage());
        JPanel btnPanel = new JPanel();
        JPanel btnPanelDown = new JPanel(new BorderLayout());
        btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(newMacro);
        btnPanelDown.add(save, BorderLayout.WEST);
        btnPanel.add(deleteMacroBtn);

        final JButton alwaysOnTopBtn = new JButton(Workspace.createImageIcon("resources/pin_up.png", ""));
        alwaysOnTopBtn.setActionCommand("pin");
        alwaysOnTopBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (alwaysOnTopBtn.getActionCommand().equals("pin")) {
                    alwaysOnTopBtn.setActionCommand("unpin");
                    alwaysOnTopBtn.setIcon(Workspace.createImageIcon("resources/pin_down.png", ""));
                    setAlwaysOnTop(true);
                } else {
                    alwaysOnTopBtn.setActionCommand("pin");
                    alwaysOnTopBtn.setIcon(Workspace.createImageIcon("resources/pin_up.png", ""));
                    setAlwaysOnTop(false);
                }
            }
        });
        JToolBar toolbarPin = new JToolBar();
        toolbarPin.setFloatable(false);
        toolbarPin.add(alwaysOnTopBtn);
        final JButton help = new JButton("", Workspace.createImageIcon("images/help-browser.png", ""));
        help.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                HelpUtils.openHelpFile("Action Lists", "macros");
            }
        });
        help.setToolTipText("What are macros?");
        toolbarPin.add(help);
        btnPanelDown.add(toolbarPin, BorderLayout.EAST);
        getContentPane().add(btnPanel, BorderLayout.NORTH);
        getContentPane().add(tabs, BorderLayout.CENTER);
        getContentPane().add(btnPanelDown, BorderLayout.SOUTH);
        newMacro.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                newMacro();
            }
        });
        this.deleteMacroBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                deleteMacro(tabs.getSelectedIndex());
            }
        });

        save.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                for (int i = 0; i < tabs.getTabCount(); i++) {
                    MacroPanel p = (MacroPanel) tabs.getComponentAt(i);
                    if (p.macroThread != null) {
                        p.macroThread.stop();
                    }
                }
                save();
                hideMacros();
            }
        });
        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                for (int i = 0; i < tabs.getTabCount(); i++) {
                    MacroPanel p = (MacroPanel) tabs.getComponentAt(i);
                    if (p.macroThread != null) {
                        p.macroThread.stop();
                    }
                }
                Macros.globalMacros.save();
                frame = null;
            }
        });
        frame = this;
        load();
        setSize(500, 400);
    }

    public void load() {
        tabs.removeAll();
        int i = 0;
        for (Macro m : Macros.globalMacros.macros) {
            MacroPanel p = new MacroPanel(m, true, true, i);
            tabs.add(m.getName(), p);
            i++;
        }
    }

    public void save() {
        Macros.globalMacros.save();
        int i = 0;
        for (Macro m : Macros.globalMacros.macros) {
            tabs.setTitleAt(i++, m.getName());
        }
    }

    public Macro newMacro() {
        Macro m = Macros.globalMacros.addNewMacro();
        tabs.add(m.getName(), new MacroPanel(m, true, true, Macros.globalMacros.macros.size() - 1));
        tabs.setSelectedIndex(tabs.getTabCount() - 1);

        return m;
    }

    public static MacrosFrame frame;

    public static void showMacros(boolean bModal) {
        if (frame == null) {
            frame = new MacrosFrame();
        }
        frame.setLocationRelativeTo(SketchletEditor.editorFrame);
        frame.setModal(bModal);
        frame.setVisible(true);
    }

    public static void showMacros(boolean bModal, int index) {
        if (frame == null) {
            frame = new MacrosFrame();
        }
        frame.setLocationRelativeTo(SketchletEditor.editorFrame);
        frame.setModal(bModal);
        frame.load();
        if (index >= 0 && index < frame.tabs.getTabCount()) {
            frame.tabs.setSelectedIndex(index);
        }
        frame.setVisible(true);
    }

    public static void showMacros(String strName, boolean bModal) {
        if (frame == null) {
            frame = new MacrosFrame();
        }
        frame.setLocationRelativeTo(SketchletEditor.editorFrame);
        for (int i = 0; i < Macros.globalMacros.macros.size(); i++) {
            Macro m = Macros.globalMacros.macros.elementAt(i);
            if (m.getName().equalsIgnoreCase(strName)) {
                frame.tabs.setSelectedIndex(i);
                break;
            }
        }
        frame.setModal(bModal);
        frame.setVisible(true);
    }

    public void deleteMacro(int index) {
        if (index >= 0 && index < Macros.globalMacros.macros.size()) {
            Macros.globalMacros.macros.remove(index);
            load();
        }
    }

    public static void hideMacros() {
        if (frame != null && frame.isVisible()) {
            frame.save();
            frame.setVisible(false);
        }
        frame = null;
    }
}
