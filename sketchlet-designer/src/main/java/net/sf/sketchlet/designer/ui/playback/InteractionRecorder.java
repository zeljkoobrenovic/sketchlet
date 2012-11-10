/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui.playback;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.ui.TextTransfer;

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
public class InteractionRecorder extends JFrame {

    final JButton save = new JButton(Language.translate("Save..."));
    final JButton copy = new JButton(Language.translate("Copy to Clipboard"));
    JTable table;
    static AbstractTableModel model;
    JScrollPane scrollPane;
    static long startTime = System.currentTimeMillis();
    Vector<String> variables;
    Vector<String> variablesTriggers;
    String columns[] = new String[]{"Time", "Event", "Param1", "Param2", "Param3"};
    public static Vector<String[]> events = null;

    public InteractionRecorder(JFrame parent) {
        setTitle(Language.translate("Interaction Recorder"));

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Recorded Events"));

        events = new Vector<String[]>();

        model = getTableModel();
        table = new JTable(model);
        scrollPane = new JScrollPane(table);
        tablePanel.add(scrollPane);

        add(tablePanel);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));

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

    public static void addEvent(String strEvent, String param1) {
        addEvent(strEvent, param1, "", "");
    }

    public static void addEvent(String strEvent, String param1, String param2) {
        addEvent(strEvent, param1, param2, "");
    }

    public static void addEvent(String strEvent, String param1, String param2, String param3) {
        if (events != null && !strEvent.isEmpty()) {
            String strTimestamp = (System.currentTimeMillis() - startTime) + "";
            events.add(new String[]{strTimestamp, strEvent, param1, param2, param3});
            if (model != null) {
                model.fireTableDataChanged();
            }
        }
    }

    public void close() {
        events = null;
        model = null;
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
                for (String[] vars : events) {
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
        for (String[] vars : events) {
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

    public AbstractTableModel getTableModel() {
        return new AbstractTableModel() {

            public int getColumnCount() {
                return columns.length;
            }

            public String getColumnName(int col) {
                return columns[col];
            }

            public int getRowCount() {
                if (events != null) {
                    return events.size();
                } else {
                    return 0;
                }
            }

            public Object getValueAt(int row, int col) {
                if (events != null) {
                    return events.elementAt(row)[col];
                } else {
                    return "";
                }
            }

            public void setValueAt(Object value, int row, int col) {
                //data.elementAt(row).setElementAt(value.toString(), col);
            }

            public boolean isCellEditable(int row, int col) {
                return true;
            }
        };

    }
}
