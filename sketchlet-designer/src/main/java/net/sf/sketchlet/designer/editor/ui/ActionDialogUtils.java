package net.sf.sketchlet.designer.editor.ui;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.region.AddActionRunnable;
import net.sf.sketchlet.framework.model.programming.macros.Macro;
import net.sf.sketchlet.util.SpringUtilities;
import net.sf.sketchlet.util.UtilContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author zeljko
 */
public class ActionDialogUtils {

    private ActionDialogUtils() {
    }

    public static void openAddActionDialog(String title, Component[] components, final AddActionRunnable onOk, String action, String param1, String param2) {
        final JComboBox commandComboBox = new JComboBox();
        commandComboBox.setEditable(true);

        for (String command : Macro.commands) {
            commandComboBox.addItem(command);
        }

        final JComboBox param1Combo = new JComboBox();
        Macro.loadParam1Combo(param1Combo, null, action);

        commandComboBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                String selItem = (String) param1Combo.getSelectedItem();
                String command = (String) commandComboBox.getSelectedItem();
                Macro.loadParam1Combo(param1Combo, selItem, command);
            }
        });

        final JDialog dlg = new JDialog(SketchletEditor.editorFrame, true);
        dlg.setTitle(Language.translate(title));
        dlg.setLayout(new BorderLayout());
        JPanel centerPane = new JPanel();
        centerPane.setLayout(new SpringLayout());

        final JTextField param2Field = new JTextField();

        for (Component component : components) {
            centerPane.add(component);
        }

        centerPane.add(new JLabel(Language.translate("Action: ")));
        centerPane.add(commandComboBox);
        commandComboBox.setSelectedItem(action);
        centerPane.add(new JLabel(Language.translate("Param1: ")));
        centerPane.add(param1Combo);
        param1Combo.setSelectedItem(param1);
        centerPane.add(new JLabel(Language.translate("Param2: ")));
        centerPane.add(param2Field);
        param2Field.setText(param2);

        SpringUtilities.makeCompactGrid(centerPane,
                3 + components.length / 2, 2, //rows, cols
                5, 5, //initialX, initialY
                5, 5);//xPad, yPad    }
        JPanel buttons = new JPanel();
        JButton btnOk = new JButton(Language.translate("OK"), UtilContext.getInstance().getImageIconFromResources("resources/ok.png"));
        dlg.getRootPane().setDefaultButton(btnOk);

        JButton btnCancel = new JButton(Language.translate("Cancel"), UtilContext.getInstance().getImageIconFromResources("resources/cancel.png"));
        btnOk.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                String action = (String) commandComboBox.getSelectedItem();
                String param1 = (String) param1Combo.getSelectedItem();
                String param2 = param2Field.getText();

                onOk.addAction(action, param1, param2);
                dlg.setVisible(false);
            }
        });
        btnCancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                dlg.setVisible(false);
            }
        });
        buttons.add(btnOk);
        buttons.add(btnCancel);

        dlg.add(centerPane, BorderLayout.CENTER);
        dlg.add(buttons, BorderLayout.SOUTH);

        dlg.pack();
        dlg.setLocationRelativeTo(SketchletEditor.editorFrame);
        dlg.setVisible(true);
    }

    public static void openAddActionDialog(String title, Component[] components, final Runnable onOk) {
        final JDialog dlg = new JDialog(SketchletEditor.editorFrame, true);
        dlg.setTitle(Language.translate(title));
        dlg.setLayout(new BorderLayout());
        JPanel centerPane = new JPanel();
        centerPane.setLayout(new SpringLayout());

        for (Component component : components) {
            centerPane.add(component);
        }

        SpringUtilities.makeCompactGrid(centerPane,
                components.length / 2, 2, //rows, cols
                5, 5, //initialX, initialY
                5, 5);//xPad, yPad    }
        JPanel buttons = new JPanel();
        JButton btnOk = new JButton(Language.translate("OK"), UtilContext.getInstance().getImageIconFromResources("resources/ok.png"));
        dlg.getRootPane().setDefaultButton(btnOk);

        JButton btnCancel = new JButton(Language.translate("Cancel"), UtilContext.getInstance().getImageIconFromResources("resources/cancel.png"));
        btnOk.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                onOk.run();
                dlg.setVisible(false);
            }
        });
        btnCancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                dlg.setVisible(false);
            }
        });
        buttons.add(btnOk);
        buttons.add(btnCancel);

        dlg.add(centerPane, BorderLayout.CENTER);
        dlg.add(buttons, BorderLayout.SOUTH);

        dlg.pack();
        dlg.setLocationRelativeTo(SketchletEditor.editorFrame);
        dlg.setVisible(true);
    }
}
