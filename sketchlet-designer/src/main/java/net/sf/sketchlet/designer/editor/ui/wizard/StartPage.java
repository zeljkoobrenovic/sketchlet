package net.sf.sketchlet.designer.editor.ui.wizard;

import net.sf.sketchlet.util.SpringUtilities;
import org.netbeans.spi.wizard.WizardPage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StartPage extends WizardPage {

    JRadioButton mouseEvent = new JRadioButton("User mouse event");
    JRadioButton interactionEvent = new JRadioButton("Overlap with other regions");
    ButtonGroup group = new ButtonGroup();
    EventPage eventPage;

    public StartPage(final EventPage eventPage) {
        this.eventPage = eventPage;
        setLayout(new SpringLayout());

        group.add(mouseEvent);
        group.add(interactionEvent);

        mouseEvent.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                eventPage.setPanel(0);
            }
        });
        interactionEvent.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                eventPage.setPanel(1);
            }
        });

        add(mouseEvent);
        add(interactionEvent);

        SpringUtilities.makeCompactGrid(this,
                2, 1, //rows, cols
                5, 5, //initialX, initialY
                5, 5);//xPad, yPad
    }

    public static final String getDescription() {
        return "Event type";
    }

    protected String validateContents(Component comp, Object o) {
        if (group.getSelection() == null) {
            return "Select event type";
        } else {
            return null;
        }
    }
}
