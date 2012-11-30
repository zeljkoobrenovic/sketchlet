package net.sf.sketchlet.designer.editor.ui.wizard;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.editor.ui.extraeditor.TimersExtraPanel;
import net.sf.sketchlet.framework.model.programming.timers.Timer;
import net.sf.sketchlet.framework.model.programming.timers.Timers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author zobrenovic
 */
public class TimerPanel extends JPanel {

    JComboBox timerCombo;
    ActionParamPage paramPage;
    JButton editTimer = new JButton(Language.translate("edit..."));

    public TimerPanel(ActionParamPage paramPage) {
        this.paramPage = paramPage;
        setLayout(new BorderLayout());
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timerCombo = new JComboBox();
        timerCombo.setEditable(true);
        timerCombo.addItem("");

        for (Timer t : Timers.getGlobalTimers().getTimers()) {
            timerCombo.addItem(t.getName());
        }
        timerCombo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                enableControls();
            }
        });


        add(new JLabel("Select the timer"), BorderLayout.NORTH);
        panel.add(new JLabel("Timer: "));
        panel.add(timerCombo);
        JButton addTimer = new JButton("new timer...");
        addTimer.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                Timer t = Timers.getGlobalTimers().addNewTimer();
                timerCombo.addItem(t.getName());
                timerCombo.setSelectedItem(t.getName());
                enableControls();
            }
        });
        panel.add(addTimer);
        editTimer.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                String strTimer = (String) timerCombo.getSelectedItem();
                int index = timerCombo.getSelectedIndex() - 1;
                if (strTimer != null) {
                    TimersExtraPanel.showTimers(strTimer);
                    if (index >= 0) {
                        timerCombo.removeAllItems();
                        timerCombo.addItem("");

                        for (Timer t : Timers.getGlobalTimers().getTimers()) {
                            timerCombo.addItem(t.getName());
                        }
                        timerCombo.setSelectedItem(Timers.getGlobalTimers().getTimers().elementAt(index).getName());
                    }
                }
            }
        });
        panel.add(editTimer);
        add(panel, BorderLayout.CENTER);
        enableControls();
    }

    public void enableControls() {
        editTimer.setEnabled(timerCombo.getSelectedIndex() > 0);
    }

    protected String validateContents(Component comp, Object o) {
        if (timerCombo.getSelectedItem() == null || timerCombo.getSelectedItem().toString().isEmpty()) {
            return "Select timer";
        } else {
            paramPage.param1 = timerCombo.getSelectedItem().toString();
            paramPage.param2 = "";
            return null;
        }
    }
}
