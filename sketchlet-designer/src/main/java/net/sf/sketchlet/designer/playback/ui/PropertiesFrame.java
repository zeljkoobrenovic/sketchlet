package net.sf.sketchlet.designer.playback.ui;

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
    JTextField left = new JTextField("" + InteractionSpace.getLeft());
    JTextField top = new JTextField("" + InteractionSpace.getTop());
    JTextField right = new JTextField("" + InteractionSpace.getRight());
    JTextField bottom = new JTextField("" + InteractionSpace.getBottom());
    JTextField angleStart = new JTextField("" + InteractionSpace.getAngleStart());
    JTextField angleEnd = new JTextField("" + InteractionSpace.getAngleEnd());
    JTextField gridSize = new JTextField("" + InteractionSpace.getGridSpacing());

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
        width = new JTextField("" + InteractionSpace.getSketchWidth());
        height = new JTextField("" + InteractionSpace.getSketchHeight());
        settings.add(width);
        settings.add(new JLabel(Language.translate("Default sketch height (in pixels):")));
        settings.add(height);

        logicalSpace.setBorder(BorderFactory.createTitledBorder(Language.translate("Units")));
        left = new JTextField("" + InteractionSpace.getLeft());
        top = new JTextField("" + InteractionSpace.getTop());
        right = new JTextField("" + InteractionSpace.getRight());
        bottom = new JTextField("" + InteractionSpace.getBottom());
        angleStart = new JTextField("" + InteractionSpace.getAngleStart());
        angleEnd = new JTextField("" + InteractionSpace.getAngleEnd());
        gridSize = new JTextField("" + InteractionSpace.getGridSpacing());

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
            InteractionSpace.setSketchWidth(Double.parseDouble(width.getText()));
            InteractionSpace.setSketchHeight(Double.parseDouble(height.getText()));
            InteractionSpace.setTop(Double.parseDouble(top.getText()));
            InteractionSpace.setLeft(Double.parseDouble(left.getText()));
            InteractionSpace.setTop(Double.parseDouble(top.getText()));
            InteractionSpace.setRight(Double.parseDouble(right.getText()));
            InteractionSpace.setBottom(Double.parseDouble(bottom.getText()));
            InteractionSpace.setAngleStart(Double.parseDouble(angleStart.getText()));
            InteractionSpace.setAngleEnd(Double.parseDouble(angleEnd.getText()));

            InteractionSpace.setGridSpacing((int) Double.parseDouble(gridSize.getText()));
            if (InteractionSpace.getGridSpacing() <= 0) {
                InteractionSpace.setGridSpacing(30);
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
