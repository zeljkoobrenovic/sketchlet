package net.sf.sketchlet.designer.editor.ui.wizard;

import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.framework.model.Page;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author zobrenovic
 */
public class SketchPanel extends JPanel {

    JComboBox sketchesCombo;
    ActionParamPage paramPage;

    public SketchPanel(final ActionParamPage paramPage) {
        this.paramPage = paramPage;

        setLayout(new BorderLayout());
        JPanel panel = new JPanel();

        sketchesCombo = new JComboBox();
        sketchesCombo.setEditable(false);
        sketchesCombo.addItem("");

        for (Page s : SketchletEditor.getInstance().getProject().getPages()) {
            sketchesCombo.addItem(s.getTitle());
        }

        panel.add(new JLabel("Sketch: "));
        panel.add(sketchesCombo);

        sketchesCombo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                paramPage.action = "Go to page";
                paramPage.param1 = sketchesCombo.getSelectedItem().toString();
                paramPage.param2 = "";
            }
        });

        add(new JLabel("  Select the sketch you would like to go to."), BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);
    }

    protected String validateContents(Component comp, Object o) {
        if (sketchesCombo.getSelectedItem().toString().length() == 0) {
            return "Select sketch";
        } else {
            return null;
        }
    }
}
