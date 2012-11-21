/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.ui.variables.recorder;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.context.VariableUpdateListener;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.TextTransfer;
import net.sf.sketchlet.designer.editor.ui.variables.SelectVariables;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class VariablesRecorder extends JFrame implements VariableUpdateListener, Runnable {

    JTextArea varArea = new JTextArea(3, 20);
    JCheckBox checkTimestamp = new JCheckBox(Language.translate("Add Timestamp"));
    final JButton record = new JButton(Language.translate("Start Recording"));
    final JButton save = new JButton(Language.translate("Save..."));
    final JButton copy = new JButton(Language.translate("Copy to Clipboard"));
    JTable table;
    AbstractTableModel model;
    JScrollPane scrollPane;
    long startTime = System.currentTimeMillis();
    JRadioButton onAnyUpdate = new JRadioButton(Language.translate("On Every Update of Any Selected Variable"));
    JRadioButton onUpdate = new JRadioButton(Language.translate("On Update of Variables"));
    JTextField triggers = new JTextField(15);
    JRadioButton periodically = new JRadioButton(Language.translate("Periodically every "));
    JTextField recPause = new JTextField("1000");
    Thread t;

    public VariablesRecorder(JFrame parent) {
        setTitle(Language.translate("Variables Recorder"));

        ButtonGroup group = new ButtonGroup();
        group.add(onAnyUpdate);
        group.add(onUpdate);
        group.add(periodically);

        onAnyUpdate.setSelected(true);

        JPanel selectedVariables = new JPanel(new BorderLayout());
        selectedVariables.setBorder(BorderFactory.createTitledBorder(Language.translate("Selected Variables")));
        JButton select = new JButton(Language.translate("Select Variables..."));
        select.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                SelectVariables dlg = new SelectVariables(SketchletEditor.editorFrame, getVarsVector());
                if (dlg.result != null) {
                    varArea.setText(dlg.result);
                }
            }
        });
        JButton selectTriggers = new JButton("...");
        selectTriggers.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                SelectVariables dlg = new SelectVariables(SketchletEditor.editorFrame, getVarsVector(triggers.getText()));
                if (dlg.result != null) {
                    onUpdate.setSelected(true);
                    triggers.setText(dlg.result);
                }
            }
        });

        selectedVariables.add(select, BorderLayout.NORTH);

        varArea.setFont(varArea.getFont().deriveFont(varArea.getFont().getSize() - 1f));
        varArea.setWrapStyleWord(false);
        varArea.setLineWrap(true);
        selectedVariables.add(new JScrollPane(varArea));

        GridLayout gridLayout = new GridLayout(3, 1);
        gridLayout.setVgap(0);
        JPanel recordType = new JPanel(gridLayout);
        recordType.setBorder(BorderFactory.createTitledBorder(Language.translate("Record")));
        JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel1.add(this.onAnyUpdate);
        JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel2.add(this.onUpdate);
        panel2.add(this.triggers);
        panel2.add(selectTriggers);
        JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel3.add(this.periodically);
        panel3.add(this.recPause);
        panel3.add(new JLabel(Language.translate(" ms")));


        recordType.add(panel1);
        recordType.add(panel2);
        recordType.add(panel3);

        selectedVariables.add(recordType, BorderLayout.SOUTH);

        JPanel timeStampPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        timeStampPanel.add(checkTimestamp);

        add(selectedVariables, BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder(Language.translate("Recorded Values")));

        model = getTableModel();
        table = new JTable(model);
        scrollPane = new JScrollPane(table);
        tablePanel.add(scrollPane);

        add(tablePanel);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));

        enableControls();

        final VariablesRecorder frame = this;

        record.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                DataServer.getInstance().removeVariablesUpdateListener(frame);
                if (!bRecording) {

                    if (!checkParams()) {
                        return;
                    }

                    data.removeAllElements();
                    bRecording = true;
                    record.setText(Language.translate("Stop Recording"));
                    variables = getVarsVector();
                    variablesTriggers = getVarsVector(triggers.getText());
                    model = getTableModel();
                    table.setModel(model);
                    startTime = System.currentTimeMillis();

                    if (onUpdate.isSelected() || onAnyUpdate.isSelected()) {
                        DataServer.getInstance().addVariablesUpdateListener(frame);
                    } else {
                        stopped = false;
                        t = new Thread(frame);
                        t.start();
                    }
                } else {
                    stopped = true;
                    bRecording = false;
                    record.setText(Language.translate("Start Recording"));
                }
                enableControls();
            }
        });
        save.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                saveToFile();
            }
        });
        copy.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                copyToClipboard();
            }
        });

        buttons.add(record);
        buttons.add(save);
        buttons.add(copy);

        add(buttons, BorderLayout.SOUTH);

        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent weeee) {
                close();
            }
        });

        pack();
        this.setLocationRelativeTo(parent);
        setVisible(true);
    }

    @Override
    public void variableUpdated(String name, String value) {
        if (this.onAnyUpdate.isSelected()) {
            if (variables.contains(name)) {
                addSample();
            }
        } else if (this.onUpdate.isSelected()) {
            if (variablesTriggers.contains(name)) {
                addSample();
            }
        }
    }

    public void addSample() {
        Vector<String> vars = new Vector<String>();
        vars.add((System.currentTimeMillis() - startTime) + "");
        for (int i = 1; i < variables.size(); i++) {
            String strVar = variables.elementAt(i);
            vars.add(DataServer.getInstance().getVariableValue(strVar));
        }

        data.add(vars);
        model.fireTableDataChanged();
        Rectangle r = table.getCellRect(table.getRowCount() - 1, 0, true);
        scrollPane.scrollRectToVisible(r);
    }

    public boolean checkParams() {
        if (this.varArea.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, Language.translate("You have to select variables\nhat you want to record."));
            return false;
        }
        if (this.onUpdate.isSelected()) {
            if (this.triggers.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, Language.translate("You have to specify trigger variables."));
                return false;
            }
        }
        if (this.periodically.isSelected()) {
            if (this.recPause.getText().isEmpty() || !containsOnlyNumbers(recPause.getText())) {
                JOptionPane.showMessageDialog(this, Language.translate("You have to specify the pause."));
                return false;
            }
        }
        return true;
    }

    public boolean containsOnlyNumbers(String str) {

        //It can't contain only numbers if it's null or empty...
        if (str == null || str.length() == 0) {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {

            //If we find a non-digit character we return false.
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public void close() {
        stopped = true;
        DataServer.getInstance().removeVariablesUpdateListener(this);
    }

    public void saveToFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnVal = fileChooser.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().contains(".")) {
                    file = new File(file.getPath() + ".txt");
                }
                PrintWriter out = new PrintWriter(new FileWriter(file));
                for (Vector<String> vars : data) {
                    String strLine = "";
                    for (String str : vars) {
                        if (!strLine.isEmpty()) {
                            strLine += "\t";
                        }
                        strLine += str;
                    }

                    out.println(strLine);
                }

                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static TextTransfer clipboard = new TextTransfer();

    public void copyToClipboard() {
        String strContent = "";
        for (Vector<String> vars : data) {
            String strLine = "";
            for (String str : vars) {
                if (!strLine.isEmpty()) {
                    strLine += "\t";
                }
                strLine += str;
            }
            strContent += strLine.trim() + "\r\n";
        }

        clipboard.setClipboardContents(strContent);
    }

    public void enableControls() {
        save.setEnabled(!bRecording && (variables != null && variables.size() > 0));
        copy.setEnabled(!bRecording && (variables != null && variables.size() > 0));
    }

    boolean bRecording = false;
    Vector<Vector<String>> data = new Vector<Vector<String>>();
    Vector<String> variables;
    Vector<String> variablesTriggers;

    public AbstractTableModel getTableModel() {
        return new AbstractTableModel() {

            public int getColumnCount() {
                return (variables == null) ? 0 : variables.size();
            }

            public String getColumnName(int col) {
                return (variables == null) ? "" : variables.elementAt(col);
            }

            public int getRowCount() {
                return data.size();
            }

            public Object getValueAt(int row, int col) {
                return data.elementAt(row).elementAt(col);
            }

            public void setValueAt(Object value, int row, int col) {
                //data.elementAt(row).setElementAt(value.toString(), col);
            }

            public boolean isCellEditable(int row, int col) {
                return true;
            }
        };

    }

    public Vector<String> getVarsVector() {
        return getVarsVector(varArea.getText().replace(" ", ""));
    }

    public Vector<String> getVarsVector(String strVars) {
        strVars = strVars.replace(" ", "");
        String vars[] = strVars.split(",");
        Vector<String> vectorVars = new Vector<String>();
        vectorVars.add("timestamp (ms)");
        for (int i = 0; i < vars.length; i++) {
            vectorVars.add(vars[i]);
        }

        return vectorVars;
    }

    boolean stopped = false;

    public void run() {
        try {
            int pause = Integer.parseInt(this.recPause.getText());
            while (!stopped && pause > 0) {
                addSample();
                Thread.sleep(pause);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
