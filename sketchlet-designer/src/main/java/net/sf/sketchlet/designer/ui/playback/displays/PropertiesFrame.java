/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui.playback.displays;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author zobrenovic
 */
public class PropertiesFrame extends JFrame {

    JButton save = new JButton(Language.translate("Save"), Workspace.createImageIcon("resources/ok.png", ""));
    JTextField width = new JTextField();
    JTextField height = new JTextField();
    JTextField left = new JTextField("" + InteractionSpace.left);
    JTextField top = new JTextField("" + InteractionSpace.top);
    JTextField right = new JTextField("" + InteractionSpace.right);
    JTextField bottom = new JTextField("" + InteractionSpace.bottom);
    JTextField angleStart = new JTextField("" + InteractionSpace.angleStart);
    JTextField angleEnd = new JTextField("" + InteractionSpace.angleEnd);
    JTextField gridSize = new JTextField("" + InteractionSpace.gridSpacing);

    public PropertiesFrame() {
        setTitle(Language.translate("Properties"));
        this.setIconImage(Workspace.createImageIcon("resources/form-properties.png").getImage());

        JPanel settings = new JPanel();
        JPanel logicalSpace = new JPanel();
        JPanel gridPanel = new JPanel();
        settings.setLayout(new GridLayout(0, 2));
        logicalSpace.setLayout(new GridLayout(0, 3));
        gridPanel.setLayout(new GridLayout(0, 2));

        settings.setBorder(BorderFactory.createTitledBorder(Language.translate("Design Space")));
        settings.add(new JLabel(Language.translate("Default sketch width (in pixels):")));
        width = new JTextField("" + InteractionSpace.sketchWidth);
        height = new JTextField("" + InteractionSpace.sketchHeight);
        settings.add(width);
        settings.add(new JLabel(Language.translate("Default sketch height (in pixels):")));
        settings.add(height);

        logicalSpace.setBorder(BorderFactory.createTitledBorder(Language.translate("Units")));
        left = new JTextField("" + InteractionSpace.left);
        top = new JTextField("" + InteractionSpace.top);
        right = new JTextField("" + InteractionSpace.right);
        bottom = new JTextField("" + InteractionSpace.bottom);
        angleStart = new JTextField("" + InteractionSpace.angleStart);
        angleEnd = new JTextField("" + InteractionSpace.angleEnd);
        gridSize = new JTextField("" + InteractionSpace.gridSpacing);

        logicalSpace.add(new JLabel(Language.translate("Left, Right: ")));
        logicalSpace.add(left);
        logicalSpace.add(right);
        logicalSpace.add(new JLabel(Language.translate("Top, Bottom: ")));
        logicalSpace.add(top);
        logicalSpace.add(bottom);
        logicalSpace.add(new JLabel(Language.translate("Angle (start, end): ")));
        logicalSpace.add(angleStart);
        logicalSpace.add(angleEnd);

        gridPanel.setBorder(BorderFactory.createTitledBorder(Language.translate("Grid")));
        gridPanel.add(new JLabel(Language.translate("Grid Spacing: ")));
        gridPanel.add(gridSize);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        String[] screens = new String[gs.length];
        for (int j = 0; j < gs.length; j++) {
            screens[j] = "" + (j + 1);
        }

        JPanel panelSouth = new JPanel(new BorderLayout());
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(save);
        panelSouth.add(gridPanel, BorderLayout.NORTH);
        panelSouth.add(btnPanel, BorderLayout.SOUTH);

        getContentPane().add(settings, BorderLayout.NORTH);
        // getContentPane().add(logicalSpace, BorderLayout.CENTER);
        getContentPane().add(panelSouth, BorderLayout.SOUTH);
        save.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                save();
            }
        });
        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                save();
            }
        });

        pack();
    }

    public void save() {
        try {
            InteractionSpace.sketchWidth = Double.parseDouble(width.getText());
            InteractionSpace.sketchHeight = Double.parseDouble(height.getText());
            InteractionSpace.top = Double.parseDouble(top.getText());
            InteractionSpace.left = Double.parseDouble(left.getText());
            InteractionSpace.top = Double.parseDouble(top.getText());
            InteractionSpace.right = Double.parseDouble(right.getText());
            InteractionSpace.bottom = Double.parseDouble(bottom.getText());
            InteractionSpace.angleStart = Double.parseDouble(angleStart.getText());
            InteractionSpace.angleEnd = Double.parseDouble(angleEnd.getText());

            InteractionSpace.gridSpacing = (int) Double.parseDouble(gridSize.getText());
            if (InteractionSpace.gridSpacing <= 0) {
                InteractionSpace.gridSpacing = 30;
            }

            InteractionSpace.save();
        } catch (Exception e) {
        }

        setVisible(false);
    }

    public static PropertiesFrame frame;

    public static void showFrame() {
        if (frame == null) {
            frame = new PropertiesFrame();
        }
        frame.setState(Frame.NORMAL);
        frame.setLocationRelativeTo(SketchletEditor.editorFrame);
        frame.setVisible(true);
        //frame.toFront();
    }

    public static void closeFrame() {
        if (frame != null && frame.isVisible()) {
            frame.save();
            frame.setVisible(false);
        }
        frame = null;
    }
}
