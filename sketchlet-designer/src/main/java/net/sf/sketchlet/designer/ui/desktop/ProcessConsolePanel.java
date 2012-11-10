/*
 * ProcessConsolePanel.java
 *
 * Created on November 11, 2006, 4:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui.desktop;

import net.sf.sketchlet.common.EscapeChars;
import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.ProcessHandler;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.script.RunInterface;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Vector;


/**
 * @author cuypers
 */
public class ProcessConsolePanel extends JPanel implements RunInterface {

    public JTextArea textArea = new JTextArea(8, 70);
    static boolean dataChanged = false;
    public JButton start = new JButton(Language.translate("Start"));
    public JButton stop = new JButton(Language.translate("Stop"));
    public JButton clear = new JButton(Language.translate("Clear Output"));
    public JButton remove = new JButton(Language.translate("Remove Process"));
    public JButton duplicate = new JButton(Language.translate("Duplicate Process"));
    public JTextField idField;
    public JTextField titleField;
    public JTextField descriptionField;
    public JCheckBox autoStartCheckBox;
    public JTextField timeOffsetField;
    public JTextField startOnField = new JTextField(10);
    public JTextField stopOnField = new JTextField(10);
    public JTextField outVariableField = new JTextField(10);
    public JTextField inVariableField = new JTextField(10);
    JLabel label1 = new JLabel(Language.translate("after:"));
    JLabel label2 = new JLabel(Language.translate("ms"));
    public ProcessHandler process;
    public String id;
    public String command;
    public String commandOriginal;
    public String workingDirectory;
    public String workingDirectoryOriginal;
    public String description;
    public String status;
    public String title;
    public String startCondition;
    public String stopCondition;
    public String outVariable;
    public String inVariable;
    public int offset;
    boolean showed = false;
    Vector processesVector;
    JTextField cmdTextFieldOriginal;
    JTextField cmdTextField;
    JTextField workingDirectoryField;
    JTabbedPane tabbedPane;
    KeyAdapter keyListener = new KeyAdapter() {

        public void keyTyped(KeyEvent e) {
            dataChanged = true;
        }
    };
    ChangeListener changeListener = new ChangeListener() {

        public void stateChanged(ChangeEvent e) {
            dataChanged = true;
        }
    };

    public ProcessConsolePanel(String identifier, String titleText, String description, String commandOriginal, String workingDirectory, Vector processes, int offset, boolean autoStart,
                               String startCondition, String stopCondition, String outVariable, String inVariable) {
        this.id = identifier;
        this.title = titleText;
        this.description = description;
        this.commandOriginal = commandOriginal;
        this.command = Workspace.replaceSystemVariables(commandOriginal);

        this.startCondition = startCondition;
        this.stopCondition = stopCondition;
        this.outVariable = outVariable;
        this.inVariable = inVariable;

        workingDirectoryField = new JTextField(49);
        workingDirectoryField.setText(workingDirectory != null ? workingDirectory : "");

        this.workingDirectoryOriginal = workingDirectory;
        if (workingDirectory.trim().equals("")) {
            this.workingDirectory = SketchletContextUtils.getCurrentProjectDir();
        } else {
            this.workingDirectory = Workspace.replaceSystemVariables(workingDirectory);
        }

        this.offset = offset;
        this.processesVector = processes;

        this.setLayout(new BorderLayout());
        cmdTextFieldOriginal = new JTextField(51);
        cmdTextField = new JTextField(64);
        cmdTextFieldOriginal.setText(commandOriginal);
        cmdTextField.setText(command);

        this.idField = new JTextField(id, 5);
        this.titleField = new JTextField(this.title, 20);
        this.descriptionField = new JTextField(description, 58);
        this.autoStartCheckBox = new JCheckBox(Language.translate("Start automatically"), autoStart);
        this.timeOffsetField = new JTextField(offset + "", 4);

        cmdTextFieldOriginal.addKeyListener(this.keyListener);
        this.idField.addKeyListener(this.keyListener);
        this.titleField.addKeyListener(this.keyListener);
        this.descriptionField.addKeyListener(this.keyListener);
        this.autoStartCheckBox.addChangeListener(this.changeListener);
        this.timeOffsetField.addKeyListener(this.keyListener);

        timeOffsetField.setEnabled(autoStart);
        label1.setEnabled(autoStart);
        label2.setEnabled(autoStart);

        autoStartCheckBox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                timeOffsetField.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
                label1.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
                label2.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
            }
        });

        JPanel panelData = new JPanel();
        panelData.setLayout(new BoxLayout(panelData, BoxLayout.Y_AXIS));

        JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel1.add(new JLabel(Language.translate("Module Name:   ")));
        panel1.add(this.titleField);
        panel1.add(new JLabel("   "));
        panel1.add(this.autoStartCheckBox);
        panel1.add(label1);
        panel1.add(this.timeOffsetField);
        panel1.add(label2);
        // panel1.add( new JLabel( "                       ID:" ) );
        // panel1.add( this.idField );
        panelData.add(panel1);

        JPanel panel3 = new JPanel();
        panel3.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel3.add(new JLabel(Language.translate("Command Line:")));
        panel3.add(cmdTextFieldOriginal);
        JButton selectFileButton = new JButton(Language.translate("Browse..."));
        final ProcessConsolePanel thisPanel = this;
        selectFileButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                fc.setCurrentDirectory(new File(workingDirectoryField.getText()));
                int returnVal = fc.showOpenDialog(thisPanel);

                if (returnVal == fc.APPROVE_OPTION) {
                    cmdTextFieldOriginal.replaceSelection(fc.getSelectedFile().getPath());
                }
            }
        });
        panel3.add(selectFileButton);
        panelData.add(panel3);

        // JPanel panel4 = new JPanel();
        // panel4.setLayout( new FlowLayout(FlowLayout.LEFT) );
        JLabel labelCmdLine = new JLabel(Language.translate("Command Line:"));
        labelCmdLine.setEnabled(false);
        // panel4.add( labelCmdLine );
        cmdTextField.setEditable(false);
        // panel4.add( cmdTextField );
        // panelData.add(panel4);

        JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel5.add(new JLabel(Language.translate("Working Directory:")));
        panel5.add(workingDirectoryField);
        JButton selectWorkingDirectoryButton = new JButton(Language.translate("Browse..."));
        selectWorkingDirectoryButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.setCurrentDirectory(new File(workingDirectoryField.getText()));
                int returnVal = fc.showOpenDialog(thisPanel);

                if (returnVal == fc.APPROVE_OPTION) {
                    workingDirectoryField.setText(fc.getSelectedFile().getPath());
                }
            }
        });
        panel5.add(selectWorkingDirectoryButton);
        panelData.add(panel5);

        JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel2.add(new JLabel(Language.translate("Description:             ")));
        panel2.add(this.descriptionField);
        panelData.add(panel2);


        JPanel panel7 = new JPanel();
        panel7.setBorder(BorderFactory.createTitledBorder("Advanced variable options"));
        panel7.setLayout(new GridLayout(0, 1));

        startOnField.setText(startCondition);
        stopOnField.setText(stopCondition);
        outVariableField.setText(outVariable);
        inVariableField.setText(inVariable);

        JPanel panel7a = new JPanel();
        panel7a.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel7a.add(new JLabel("Start condition:  "));
        panel7a.add(startOnField);
        JLabel label = new JLabel(" (for example, condition 'cmd=begin' starts process when variable 'cmd' equals 'begin')");
        label.setEnabled(false);
        panel7a.add(label);

        JPanel panel7b = new JPanel();
        panel7b.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel7b.add(new JLabel("Stop condition:   "));
        panel7b.add(stopOnField);
        label = new JLabel(" (for example, condition 'cmd=end' stops process when variable 'cmd' equals 'end')");
        label.setEnabled(false);
        panel7b.add(label);

        JPanel panel7c = new JPanel();
        panel7c.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel7c.add(new JLabel("Output variable: "));
        panel7c.add(outVariableField);
        label = new JLabel(" (updates this variable with process console output)");
        label.setEnabled(false);
        panel7c.add(label);

        JPanel panel7d = new JPanel();
        panel7d.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel7d.add(new JLabel("Input variable:    "));
        panel7d.add(inVariableField);
        label = new JLabel(" (sends content of this variable to process console input)");
        label.setEnabled(false);
        panel7d.add(label);

        panel7.add(panel7a);
        panel7.add(panel7b);
        panel7.add(panel7c);
        panel7.add(panel7d);

        // panelData.add(panel7);

        panelData.add(new JLabel(""));
        JPanel panel6 = new JPanel();
        panel6.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel labelConsoleOutput = new JLabel(Language.translate("Running: "));
        labelConsoleOutput.setEnabled(false);
        panel6.add(labelConsoleOutput);
        panel6.add(cmdTextField);
        panelData.add(panel6);

        add(panelData, BorderLayout.NORTH);

        if (autoStart) {
            this.process = new ProcessHandler(id, command, this.workingDirectory, offset, processes, this);
        }

        textArea.setBackground(Color.BLACK);
        textArea.setForeground(Color.GREEN);

        add(new JScrollPane(textArea), BorderLayout.CENTER);
        JPanel panel = new JPanel();

        start.setEnabled(!autoStart);
        stop.setEnabled(autoStart);

        stop.addActionListener(
                new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        stopProcess();
                    }
                });
        start.addActionListener(
                new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        startProcess();
                    }
                });
        clear.addActionListener(
                new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        textArea.setText("");
                    }
                });

        final JPanel container = this;

        remove.addActionListener(
                new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        removeProcess();
                    }
                });

        duplicate.addActionListener(
                new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        duplicateProcess();
                    }
                });

        panel.add(start);
        panel.add(stop);
        panel.add(new JLabel("  "));
        panel.add(clear);
        panel.add(new JLabel("                 "));
        panel.add(remove);
        panel.add(duplicate);

        add(panel, BorderLayout.SOUTH);
    }

    public void startProcess() {
        start.setEnabled(false);
        restart();
        stop.setEnabled(true);
    }

    /* public Dimension getPreferredSize() {
    return new Dimension( 800, 400 );
    }
     */
    public void stopProcess() {
        stop.setEnabled(false);
        kill();
        start.setEnabled(true);
    }

    public void removeProcess() {
        if (JOptionPane.showConfirmDialog(SketchletEditor.editorFrame, Language.translate("You are about to remove the process '") + title + "'.\n" + Language.translate("Do you want to continue?"), Language.translate("Removing I/O Service"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            kill();
            tabbedPane.remove(this);

            Workspace.processHandlers.remove(this);
            if (process != null && process.theProcess != null) {
                synchronized (Workspace.processes) {
                    Workspace.processes.remove(process.theProcess);
                }
            }
            Workspace.processHandlersIdMap.remove(id);

            Workspace.mainPanel.refreshData(true);
            Workspace.mainPanel.saveConfiguration();
        }
    }

    public void duplicateProcess() {
        Workspace.processRunner.addProcess(idField.getText(), cmdTextFieldOriginal.getText(), this.workingDirectoryOriginal, titleField.getText(),
                descriptionField.getText(), Integer.parseInt(timeOffsetField.getText()), autoStartCheckBox.isSelected(),
                this.startOnField.getText(), this.stopOnField.getText(),
                this.outVariableField.getText(), this.inVariableField.getText());
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
        Workspace.mainPanel.refreshData(true);
    }

    public String getXmlString() {
        this.tabbedPane.setTitleAt(this.tabbedPane.indexOfComponent(this), this.titleField.getText());

        String xmlString = "";
        xmlString += "<process id='" + EscapeChars.forHTMLTag(this.idField.getText()) + "'";
        xmlString += " title='" + EscapeChars.forHTMLTag(this.titleField.getText()) + "'";
        xmlString += " description='" + EscapeChars.forHTMLTag(this.descriptionField.getText()) + "'";
        xmlString += " auto-start='" + this.autoStartCheckBox.isSelected() + "'";
        xmlString += " working-directory='" + this.workingDirectoryField.getText() + "'";

        xmlString += " start-condition='" + this.startOnField.getText() + "'";
        xmlString += " stop-condition='" + this.stopOnField.getText() + "'";
        xmlString += " out-variable='" + this.outVariableField.getText() + "'";
        xmlString += " in-variable='" + this.inVariableField.getText() + "'";

        xmlString += " timeOffsetMs='" + this.timeOffsetField.getText() + "'>";


        xmlString += EscapeChars.forHTMLTag(this.cmdTextFieldOriginal.getText());
        xmlString += "</process>";

        return xmlString;
    }

    public String getTxtString() {
        this.tabbedPane.setTitleAt(this.tabbedPane.indexOfComponent(this), this.titleField.getText());

        String xmlString = "";
        xmlString += "AddProcess " + this.idField.getText() + "\r\n";
        xmlString += "ProcessTitle " + this.titleField.getText() + "\r\n";
        xmlString += "Description " + this.descriptionField.getText() + "\r\n";
        xmlString += "AutoStart " + this.autoStartCheckBox.isSelected() + "\r\n";
        xmlString += "TimeOffsetMs " + this.timeOffsetField.getText() + "\r\n";
        xmlString += "WorkingDirectory " + this.workingDirectoryField.getText() + "\r\n";
        xmlString += "CommandLine " + this.cmdTextFieldOriginal.getText() + "\r\n";
        xmlString += "StartCondition " + this.startOnField.getText() + "\r\n";
        xmlString += "StopCondition " + this.stopOnField.getText() + "\r\n";
        xmlString += "OutVariable " + this.outVariableField.getText() + "\r\n";
        xmlString += "InVariable " + this.inVariableField.getText() + "\r\n\r\n";

        return xmlString;
    }

    public void setParentTabbedPanel(JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
    }

    public void restart() {
        this.command = Workspace.replaceSystemVariables(this.cmdTextFieldOriginal.getText());
        if (this.workingDirectoryField.getText().trim().equals("")) {
            this.workingDirectory = SketchletContextUtils.getCurrentProjectDir();
        } else {
            this.workingDirectory = Workspace.replaceSystemVariables(this.workingDirectoryField.getText());
        }
        this.cmdTextField.setText(this.command);
        this.process = new ProcessHandler(this.id, this.command, this.workingDirectory, 0, this.processesVector, this);
    }

    public void kill() {
        if (process == null || process.theProcess == null) {
            return;
        }

        this.processesVector.remove(process.theProcess);
        this.process.theProcess.destroy();
        this.process.theProcess = null;

        this.textArea.append("Process killed.\n");
    }

    public void start() {
        if (this.start.isEnabled()) {
            this.startProcess();
        }
    }

    public void stop() {
        this.stopProcess();
    }

    public String getName() {
        if (this.titleField != null) {
            return "Service:" + this.titleField.getText();
        }
        return "";
    }
}
