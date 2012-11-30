package net.sf.sketchlet.designer.editor.ui.desktop;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;
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

    private JTextArea textArea = new JTextArea(2, 12);
    private JButton okButton = new JButton(Language.translate("OK"), Workspace.createImageIcon("resources/ok.png"));
    private JButton cancelButton = new JButton(Language.translate("Cancel"), Workspace.createImageIcon("resources/cancel.png"));
    private JCheckBox checkMouseX = new JCheckBox(Language.translate("Mouse Position X"), false);
    private JCheckBox checkMouseY = new JCheckBox(Language.translate("Mouse Position Y"), false);
    private JCheckBox checkMouseEvent = new JCheckBox(Language.translate("Mouse Event"), false);
    private JCheckBox checkKey = new JCheckBox(Language.translate("Keyboard Key"), false);
    private JCheckBox checkKeyCode = new JCheckBox(Language.translate("Keyboard Code"), false);
    private JCheckBox checkKeyboardEvent = new JCheckBox(Language.translate("Keyboard Event"), false);
    private JCheckBox checkTimeHour = new JCheckBox(Language.translate("Time / Hours"), false);
    private JCheckBox checkTimeMinute = new JCheckBox(Language.translate("Time / Minutes"), false);
    private JCheckBox checkTimeSecond = new JCheckBox(Language.translate("Time / Seconds"), false);
    private JCheckBox checkTimestamp = new JCheckBox(Language.translate("Timestamp"), false);
    private JTextField fieldVarX = new JTextField("mouse_x");
    private JTextField fieldVarY = new JTextField("mouse_x");
    private JTextField fieldVarEvent = new JTextField("mouse_event");
    private JTextField fieldVarKey = new JTextField("key");
    private JTextField fieldVarCode = new JTextField("keycode");
    private JTextField fieldVarKeyboardEvent = new JTextField("keyboard_event");
    private JTextField fieldVarTimeHour = new JTextField("hours");
    private JTextField fieldVarTimeMinute = new JTextField("minutes");
    private JTextField fieldVarTimeSecond = new JTextField("seconds");
    private JTextField fieldVarTimestamp = new JTextField("timestamp");
    private static Object[][] data = {
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

    public static Object[][] getData() {
        return data;
    }

    public static void setData(Object[][] data) {
        SystemVariablesDialog.data = data;
    }

    public void save() {
        getData()[0][0] = "" + checkMouseX.isSelected();
        getData()[1][0] = "" + checkMouseY.isSelected();
        getData()[2][0] = "" + checkMouseEvent.isSelected();
        getData()[3][0] = "" + checkKey.isSelected();
        getData()[4][0] = "" + checkKeyboardEvent.isSelected();
        getData()[5][0] = "" + checkTimeHour.isSelected();
        getData()[6][0] = "" + checkTimeMinute.isSelected();
        getData()[7][0] = "" + checkTimeSecond.isSelected();
        getData()[8][0] = "" + checkTimestamp.isSelected();
        getData()[9][0] = "" + checkKeyCode.isSelected();

        getData()[0][1] = fieldVarX.getText();
        getData()[1][1] = fieldVarY.getText();
        getData()[2][1] = fieldVarEvent.getText();
        getData()[3][1] = fieldVarKey.getText();
        getData()[9][1] = fieldVarCode.getText();
        getData()[4][1] = fieldVarKeyboardEvent.getText();
        getData()[5][1] = fieldVarTimeHour.getText();
        getData()[6][1] = fieldVarTimeMinute.getText();
        getData()[7][1] = fieldVarTimeSecond.getText();
        getData()[8][1] = fieldVarTimestamp.getText();

        XMLHelper.save("system_variables.xml", "system_variables", getData());
        startThread();
    }

    public static SystemVarThread timeThread;

    public void load() {
        XMLHelper.load("system_variables.xml", "system_variables", getData());

        checkMouseX.setSelected(((String) getData()[0][0]).equalsIgnoreCase("true"));
        checkMouseY.setSelected(((String) getData()[1][0]).equalsIgnoreCase("true"));
        checkMouseEvent.setSelected(((String) getData()[2][0]).equalsIgnoreCase("true"));
        checkKey.setSelected(((String) getData()[3][0]).equalsIgnoreCase("true"));
        checkKeyCode.setSelected(((String) getData()[9][0]).equalsIgnoreCase("true"));
        checkKeyboardEvent.setSelected(((String) getData()[4][0]).equalsIgnoreCase("true"));
        checkTimeHour.setSelected(((String) getData()[5][0]).equalsIgnoreCase("true"));
        checkTimeMinute.setSelected(((String) getData()[6][0]).equalsIgnoreCase("true"));
        checkTimeSecond.setSelected(((String) getData()[7][0]).equalsIgnoreCase("true"));
        checkTimestamp.setSelected(((String) getData()[8][0]).equalsIgnoreCase("true"));
        fieldVarX.setText((String) getData()[0][1]);
        fieldVarY.setText((String) getData()[1][1]);
        fieldVarEvent.setText((String) getData()[2][1]);
        fieldVarKey.setText((String) getData()[3][1]);
        fieldVarCode.setText((String) getData()[9][1]);
        fieldVarKeyboardEvent.setText((String) getData()[4][1]);
        fieldVarTimeHour.setText((String) getData()[5][1]);
        fieldVarTimeMinute.setText((String) getData()[6][1]);
        fieldVarTimeSecond.setText((String) getData()[7][1]);
        fieldVarTimestamp.setText((String) getData()[8][1]);

    }

    public static void startThread() {
        if (timeThread != null) {
            timeThread.stopped = true;
            timeThread = null;
        }

        timeThread = new SystemVarThread();

    }

    public static void processMouseEvent(int x, int y, String event) {
        if (((String) getData()[0][0]).equalsIgnoreCase("true")) {
            VariablesBlackboard.getInstance().updateVariableIfDifferent((String) getData()[0][1], "" + x);
        }
        if (((String) getData()[1][0]).equalsIgnoreCase("true")) {
            VariablesBlackboard.getInstance().updateVariableIfDifferent((String) getData()[1][1], "" + y);
        }
        if (((String) getData()[2][0]).equalsIgnoreCase("true")) {
            VariablesBlackboard.getInstance().updateVariable((String) getData()[2][1], event);
        }
    }

    public static void processKeyboardEvent(KeyEvent e, String key, String event) {
        if (((String) getData()[3][0]).equalsIgnoreCase("true")) {
            VariablesBlackboard.getInstance().updateVariableIfDifferent((String) getData()[3][1], key);
        }
        if (((String) getData()[9][0]).equalsIgnoreCase("true")) {
            VariablesBlackboard.getInstance().updateVariableIfDifferent((String) getData()[9][1], e.getKeyCode() + "");
        }
        if (((String) getData()[4][0]).equalsIgnoreCase("true")) {
            VariablesBlackboard.getInstance().updateVariable((String) getData()[4][1], event);
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
                String strH = getData()[5][1].toString();
                String strM = getData()[6][1].toString();
                String strS = getData()[7][1].toString();
                String strT = getData()[8][1].toString();
                boolean bH = getData()[5][0].toString().equalsIgnoreCase("true") && !strH.isEmpty();
                boolean bM = getData()[6][0].toString().equalsIgnoreCase("true") && !strM.isEmpty();
                boolean bS = getData()[7][0].toString().equalsIgnoreCase("true") && !strS.isEmpty();
                boolean bT = getData()[8][0].toString().equalsIgnoreCase("true") && !strT.isEmpty();

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
}
