/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui.wizard;

import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.data.MouseProcessor;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.util.SpringUtilities;
import org.netbeans.spi.wizard.WizardPage;

import javax.swing.*;
import java.awt.*;

public class EventPage extends WizardPage {

    JComboBox regionsCombo;
    JComboBox interactionEvent;
    JComboBox mouseEvent;
    JPanel panelInteraction;
    JPanel panelMouse;
    int activePanel = 0;

    public EventPage() {
        setLayout(new FlowLayout());
        createInteractionEventPanel();
        createMouseEventPanel();
    }

    public void setPanel(int active) {
        removeAll();
        activePanel = active;
        if (active == 0) {
            add(panelMouse);
        } else {
            add(panelInteraction);
        }
        revalidate();
    }

    public void createInteractionEventPanel() {
        panelInteraction = new JPanel(new BorderLayout());
        JPanel panel = new JPanel(new SpringLayout());

        interactionEvent = new JComboBox();
        interactionEvent.setEditable(true);
        interactionEvent.addItem("");
        interactionEvent.addItem("touches");
        interactionEvent.addItem("inside");
        interactionEvent.addItem("outside");
        interactionEvent.addItem("completely outside");

        panel.add(new JLabel("  Region"));
        regionsCombo = this.createRegionCombo(true, SketchletEditor.editorPanel.currentPage.regions.regions.size());
        regionsCombo.setName("Region");
        panel.add(regionsCombo);
        panel.add(new JLabel(""));

        panel.add(new JLabel("  Overlap type"));
        interactionEvent.setName("Overlap type");
        panel.add(interactionEvent);
        panel.add(new JLabel(""));

        SpringUtilities.makeCompactGrid(panel,
                2, 3, //rows, cols
                0, 15, //initialX, initialY
                5, 5);//xPad, yPad

        panelInteraction.add(new JLabel("Select the region and type of overlap (touches, inside, outsize...)"), BorderLayout.NORTH);
        panelInteraction.add(panel, BorderLayout.CENTER);
        panelInteraction.add(new JLabel(Workspace.createImageIcon("resources/interaction.png")), BorderLayout.WEST);
    }

    public void createMouseEventPanel() {
        panelMouse = new JPanel(new BorderLayout());
        JPanel panel = new JPanel(new SpringLayout());
        mouseEvent = new JComboBox();
        mouseEvent.setName("Mouse event");
        mouseEvent.setEditable(false);

        mouseEvent.addItem("");

        for (int i = 0; i < MouseProcessor.MOUSE_EVENT_TYPES.length; i++) {
            mouseEvent.addItem(MouseProcessor.MOUSE_EVENT_TYPES[i]);
        }

        panel.add(new JLabel("  Mouse event"));
        panel.add(mouseEvent);
        panel.add(new JLabel(""));

        SpringUtilities.makeCompactGrid(panel,
                1, 3, //rows, cols
                0, 15, //initialX, initialY
                5, 5);//xPad, yPad


        panelMouse.add(new JLabel("Select mouse event (click, press, release...)"), BorderLayout.NORTH);
        panelMouse.add(panel, BorderLayout.CENTER);
        panelMouse.add(new JLabel(Workspace.createImageIcon("resources/mouse.png")), BorderLayout.WEST);
    }

    public static final String getDescription() {
        return "Select Event";
    }

    protected String validateContents(Component comp, Object o) {
        if (activePanel == 0) {
            if (mouseEvent.getSelectedItem().toString().length() == 0) {
                return "Select mouse event";
            } else {
                return null;
            }
        } else {
            if (regionsCombo.getSelectedItem() != null && regionsCombo.getSelectedItem().toString().length() == 0) {
                return "Select the region";
            } else if (interactionEvent.getSelectedItem() != null && interactionEvent.getSelectedItem().toString().length() == 0) {
                return "Select event";
            } else {
                return null;
            }
        }
    }

    public static JComboBox createRegionCombo(boolean addAny, int n) {
        JComboBox regionComboBox = new JComboBox();
        regionComboBox.setEditable(true);

        regionComboBox.removeAllItems();

        regionComboBox.addItem("");
        if (addAny) {
            regionComboBox.addItem("Any region");
        }

        for (int i = 0; i < n; i++) {
            regionComboBox.addItem("" + (i + 1));
        }

        return regionComboBox;
    }
}