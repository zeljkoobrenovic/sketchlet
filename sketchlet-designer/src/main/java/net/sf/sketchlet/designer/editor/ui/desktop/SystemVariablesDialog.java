/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.ui.desktop;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.blackboard.VariablesBlackboard;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.util.SpringUtilities;
import net.sf.sketchlet.util.XMLHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Calendar;

/**
 * @author zobrenovic
 */
public class SystemVariablesDialog extends JDialog {

    JTextArea textArea = new JTextArea(2, 12);
    JButton okButton = new JButton(Language.translate("OK"), Workspace.createImageIcon("resources/ok.png"));
    JButton cancelButton = new JButton(Language.translate("Cancel"), Workspace.createImageIcon("resources/cancel.png"));
    JCheckBox checkMouseX = new JCheckBox(Language.translate("Mouse Position X"), false);
    JCheckBox checkMouseY = new JCheckBox(Language.translate("Mouse Position Y"), false);
    JCheckBox checkMouseEvent = new JCheckBox(Language.translate("Mouse Event"), false);
    JCheckBox checkKey = new JCheckBox(Language.translate("Keyboard Key"), false);
    JCheckBox checkKeyCode = new JCheckBox(Language.translate("Keyboard Code"), false);
    JCheckBox checkKeyboardEvent = new JCheckBox(Language.translate("Keyboard Event"), false);
    JCheckBox checkTimeHour = new JCheckBox(Language.translate("Time / Hours"), false);
    JCheckBox checkTimeMinute = new JCheckBox(Language.translate("Time / Minutes"), false);
    JCheckBox checkTimeSecond = new JCheckBox(Language.translate("Time / Seconds"), false);
    JCheckBox checkTimestamp = new JCheckBox(Language.translate("Timestamp"), false);
    JTextField fieldVarX = new JTextField("mouse_x");
    JTextField fieldVarY = new JTextField("mouse_x");
    JTextField fieldVarEvent = new JTextField("mouse_event");
    JTextField fieldVarKey = new JTextField("key");
    JTextField fieldVarCode = new JTextField("keycode");
    JTextField fieldVarKeyboardEvent = new JTextField("keyboard_event");
    JTextField fieldVarTimeHour = new JTextField("hours");
    JTextField fieldVarTimeMinute = new JTextField("minutes");
    JTextField fieldVarTimeSecond = new JTextField("seconds");
    JTextField fieldVarTimestamp = new JTextField("timestamp");
    public static Object data[][] = {
            {"false", "mouse_x", "mouse_x"},
            {"false", "mouse_y", "mouse_y"},
            {"false", "mouse_event", "mouse_event"},
            {"false", "key", "key"},
            {"false", "keyboard_event", "keyboard_event"},
            {"false", "time_hour", "time_hour"},
            {"false", "time_minute", "time_minute"},
            {"false", "time_second", "time_second"},
            {"false", "timestamp", "timestamp"},
            {"false", "keycode", "keycode"},};

    public SystemVariablesDialog(JFrame frame) {
        super(frame);
        setModal(true);
        setTitle(Language.translate("System Variables"));

        load();

        if (frame != null) {
            this.setLocationRelativeTo(frame);
        }


        JTabbedPane tabs = new JTabbedPane();

        JPanel panel = new JPanel(new SpringLayout());

        panel.add(new JLabel("        Event"));
        panel.add(new JLabel("Update Variable"));
        panel.add(checkMouseX);
        panel.add(fieldVarX);
        panel.add(checkMouseY);
        panel.add(fieldVarY);
        panel.add(checkMouseEvent);
        panel.add(fieldVarEvent);
        panel.add(new JLabel(" "));
        panel.add(new JLabel(" "));
        SpringUtilities.makeCompactGrid(panel,
                5, 2, //rows, cols
                5, 5, //initialX, initialY
                10, 10);//xPad, yPad
        tabs.add(panel, Language.translate("Mouse Events"));
        panel = new JPanel(new SpringLayout());
        panel.add(new JLabel(Language.translate("        Event")));
        panel.add(new JLabel(Language.translate("Update Variable")));
        panel.add(checkKey);
        panel.add(fieldVarKey);
        panel.add(checkKeyCode);
        panel.add(fieldVarCode);
        panel.add(checkKeyboardEvent);
        panel.add(fieldVarKeyboardEvent);
        panel.add(new JLabel(" "));
        panel.add(new JLabel(" "));
        SpringUtilities.makeCompactGrid(panel,
                5, 2, //rows, cols
                5, 5, //initialX, initialY
                15, 15);//xPad, yPad
        tabs.add(panel, Language.translate("Keyboard Events"));
        panel = new JPanel(new SpringLayout());
        panel.add(new JLabel(Language.translate("        Event")));
        panel.add(new JLabel(Language.translate("Update Variable")));
        panel.add(checkTimeHour);
        panel.add(fieldVarTimeHour);
        panel.add(checkTimeMinute);
        panel.add(fieldVarTimeMinute);
        panel.add(checkTimeSecond);
        panel.add(fieldVarTimeSecond);
        panel.add(checkTimestamp);
        panel.add(fieldVarTimestamp);
        SpringUtilities.makeCompactGrid(panel,
                5, 2, //rows, cols
                5, 5, //initialX, initialY
                5, 5);//xPad, yPad
        tabs.add(panel, Language.translate("System Time"));

        add(tabs);

        JPanel buttons = new JPanel();
        buttons.add(okButton);
        buttons.add(cancelButton);

        this.getRootPane().setDefaultButton(okButton);

        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                save();
                setVisible(false);
            }
        });
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                setVisible(false);
            }
        });

        add(buttons, BorderLayout.SOUTH);

        setSize(350, 270);
        if (frame != null) {
            this.setLocationRelativeTo(frame);
        }
        setVisible(true);
    }

    public void save() {
        data[0][0] = "" + checkMouseX.isSelected();
        data[1][0] = "" + checkMouseY.isSelected();
        data[2][0] = "" + checkMouseEvent.isSelected();
        data[3][0] = "" + checkKey.isSelected();
        data[4][0] = "" + checkKeyboardEvent.isSelected();
        data[5][0] = "" + checkTimeHour.isSelected();
        data[6][0] = "" + checkTimeMinute.isSelected();
        data[7][0] = "" + checkTimeSecond.isSelected();
        data[8][0] = "" + checkTimestamp.isSelected();
        data[9][0] = "" + checkKeyCode.isSelected();

        data[0][1] = fieldVarX.getText();
        data[1][1] = fieldVarY.getText();
        data[2][1] = fieldVarEvent.getText();
        data[3][1] = fieldVarKey.getText();
        data[9][1] = fieldVarCode.getText();
        data[4][1] = fieldVarKeyboardEvent.getText();
        data[5][1] = fieldVarTimeHour.getText();
        data[6][1] = fieldVarTimeMinute.getText();
        data[7][1] = fieldVarTimeSecond.getText();
        data[8][1] = fieldVarTimestamp.getText();

        XMLHelper.save("system_variables.xml", "system_variables", data);
        startThread();
    }

    public static SystemVarThread timeThread;

    public void load() {
        XMLHelper.load("system_variables.xml", "system_variables", data);

        checkMouseX.setSelected(((String) data[0][0]).equalsIgnoreCase("true"));
        checkMouseY.setSelected(((String) data[1][0]).equalsIgnoreCase("true"));
        checkMouseEvent.setSelected(((String) data[2][0]).equalsIgnoreCase("true"));
        checkKey.setSelected(((String) data[3][0]).equalsIgnoreCase("true"));
        checkKeyCode.setSelected(((String) data[9][0]).equalsIgnoreCase("true"));
        checkKeyboardEvent.setSelected(((String) data[4][0]).equalsIgnoreCase("true"));
        checkTimeHour.setSelected(((String) data[5][0]).equalsIgnoreCase("true"));
        checkTimeMinute.setSelected(((String) data[6][0]).equalsIgnoreCase("true"));
        checkTimeSecond.setSelected(((String) data[7][0]).equalsIgnoreCase("true"));
        checkTimestamp.setSelected(((String) data[8][0]).equalsIgnoreCase("true"));
        fieldVarX.setText((String) data[0][1]);
        fieldVarY.setText((String) data[1][1]);
        fieldVarEvent.setText((String) data[2][1]);
        fieldVarKey.setText((String) data[3][1]);
        fieldVarCode.setText((String) data[9][1]);
        fieldVarKeyboardEvent.setText((String) data[4][1]);
        fieldVarTimeHour.setText((String) data[5][1]);
        fieldVarTimeMinute.setText((String) data[6][1]);
        fieldVarTimeSecond.setText((String) data[7][1]);
        fieldVarTimestamp.setText((String) data[8][1]);

    }

    public static void startThread() {
        if (timeThread != null) {
            timeThread.stopped = true;
            timeThread = null;
        }

        timeThread = new SystemVarThread();

    }

    public static void processMouseEvent(int x, int y, String event) {
        if (((String) data[0][0]).equalsIgnoreCase("true")) {
            VariablesBlackboard.getInstance().updateVariableIfDifferent((String) data[0][1], "" + x);
        }
        if (((String) data[1][0]).equalsIgnoreCase("true")) {
            VariablesBlackboard.getInstance().updateVariableIfDifferent((String) data[1][1], "" + y);
        }
        if (((String) data[2][0]).equalsIgnoreCase("true")) {
            VariablesBlackboard.getInstance().updateVariable((String) data[2][1], event);
        }
    }

    public static void processKeyboardEvent(KeyEvent e, String key, String event) {
        if (((String) data[3][0]).equalsIgnoreCase("true")) {
            VariablesBlackboard.getInstance().updateVariableIfDifferent((String) data[3][1], key);
        }
        if (((String) data[9][0]).equalsIgnoreCase("true")) {
            VariablesBlackboard.getInstance().updateVariableIfDifferent((String) data[9][1], e.getKeyCode() + "");
        }
        if (((String) data[4][0]).equalsIgnoreCase("true")) {
            VariablesBlackboard.getInstance().updateVariable((String) data[4][1], event);
        }
    }

    public static void stop() {
        if (timeThread != null) {
            timeThread.stopped = true;
            timeThread = null;
        }
    }

    static class SystemVarThread implements Runnable {

        Thread t;
        boolean stopped = false;

        public SystemVarThread() {
            t = new Thread(this);
            t.start();
        }

        public void run() {
            while (!stopped) {
                String strH = data[5][1].toString();
                String strM = data[6][1].toString();
                String strS = data[7][1].toString();
                String strT = data[8][1].toString();
                boolean bH = data[5][0].toString().equalsIgnoreCase("true") && !strH.isEmpty();
                boolean bM = data[6][0].toString().equalsIgnoreCase("true") && !strM.isEmpty();
                boolean bS = data[7][0].toString().equalsIgnoreCase("true") && !strS.isEmpty();
                boolean bT = data[8][0].toString().equalsIgnoreCase("true") && !strT.isEmpty();

                if (bH || bM || bS || bT) {
                    if (!VariablesBlackboard.getInstance().isPaused()) {
                        Calendar cal = Calendar.getInstance();
                        if (bH) {
                            int hod = cal.get(Calendar.HOUR_OF_DAY);
                            VariablesBlackboard.getInstance().updateVariableIfDifferent(strH, "" + (hod > 9 ? hod : "0" + hod));
                        }
                        if (bM) {
                            int min = cal.get(Calendar.MINUTE);
                            VariablesBlackboard.getInstance().updateVariableIfDifferent(strM, "" + (min > 9 ? min : "0" + min));
                        }
                        if (bS) {
                            int sec = cal.get(Calendar.SECOND);
                            VariablesBlackboard.getInstance().updateVariableIfDifferent(strS, "" + (sec > 9 ? sec : "0" + sec));
                        }
                        if (bT) {
                            VariablesBlackboard.getInstance().updateVariableIfDifferent(strT, "" + cal.getTimeInMillis());
                        }
                    }
                } else {
                    break;
                }

                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }
            stopped = true;
        }
    }

    public static void main(String args[]) {
        new SystemVariablesDialog(null);
    }
}
