package net.sf.sketchlet.designer.editor.ui.wizard;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.framework.model.events.keyboard.KeyEvents;
import net.sf.sketchlet.util.SpringUtilities;
import org.netbeans.spi.wizard.WizardPage;

import javax.swing.*;
import java.awt.*;

/**
 * @author zobrenovic
 */
public class KeyboardEventPage extends WizardPage {

    JCheckBox ctrlBox = new JCheckBox(Language.translate("ctrl"));
    JCheckBox altBox = new JCheckBox(Language.translate("alt"));
    JCheckBox shiftBox = new JCheckBox(Language.translate("shift"));
    JComboBox variableKeys;
    JComboBox variableEvents;
    String events[] = {"pressed", "released", "hold 1", "hold 1*"};

    public KeyboardEventPage() {
        setLayout(new BorderLayout());
        JPanel panel = new JPanel(new SpringLayout());
        JPanel panelMod = new JPanel();
        panelMod.add(shiftBox);
        panelMod.add(ctrlBox);
        panelMod.add(altBox);

        variableKeys = KeyEvents.getComboBox();
        variableEvents = new JComboBox(events);
        variableEvents.setSelectedIndex(0);

        panel.add(panelMod);
        panel.add(new JLabel(" + key "));
        panel.add(variableKeys);
        panel.add(variableEvents);

        SpringUtilities.makeCompactGrid(panel,
                1, 4, //rows, cols
                0, 15, //initialX, initialY
                5, 5);//xPad, yPad

        JPanel panelVars = new JPanel();
        panelVars.add(panel);

        add(new JLabel(Language.translate("Select the key event")), BorderLayout.NORTH);
        add(panelVars, BorderLayout.CENTER);
    }

    public static final String getDescription() {
        return Language.translate("Define the condition");
    }

    protected String validateContents(Component comp, Object o) {
        if (variableKeys.getSelectedItem().toString().length() == 0) {
            return Language.translate("Select the key");
        } else {
            return null;
        }
    }
}

