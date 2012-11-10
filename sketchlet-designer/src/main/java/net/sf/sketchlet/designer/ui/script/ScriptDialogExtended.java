/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.ui.script;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.ui.variables.SelectVariables;
import net.sf.sketchlet.pluginloader.ScriptPluginFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * @author zobrenovic
 */
public class ScriptDialogExtended extends JDialog implements ActionListener {

    private JPanel scriptPanel = null;
    private JPanel varPanel = null;
    private JPanel controlPanel = null;
    JTextField fileNameField = null;
    private JButton okButton = null;
    private JButton cancelButton = null;
    private JComboBox scriptTypeList = null;
    boolean bOK = false;
    String[] fileNames;
    ScriptsTablePanel panel;
    JTextField triggerVariables;
    JTextArea scriptBody = new JTextArea(15, 50);
    String strFileName;
    String strTemplateDir;
    String replace[][];

    public ScriptDialogExtended(ScriptsTablePanel panel, final JFrame frame, String strTitle,
                                String strFileName, String triggerVars,
                                String[] typeStrings, String[] fileNames, String replace[][],
                                String strTemplateDir, boolean modal) {
        super(frame, modal);
        this.strFileName = strFileName;
        this.strTemplateDir = strTemplateDir;
        this.panel = panel;
        this.fileNames = fileNames;
        this.replace = replace;

        this.setTitle(Language.translate("New Script") + " - " + strTitle);

        scriptPanel = new JPanel();
        varPanel = new JPanel();
        varPanel.setLayout(new BorderLayout());

        getContentPane().add(scriptPanel, BorderLayout.NORTH);
        //getContentPane().add(varPanel, BorderLayout.CENTER);

        scriptPanel.add(new JLabel(Language.translate(" Language: ")));
        scriptTypeList = new JComboBox(typeStrings);
        scriptTypeList.setSelectedIndex(0);
        scriptTypeList.addActionListener(this);
        scriptPanel.add(scriptTypeList);

        scriptPanel.add(new JLabel(Language.translate("   File name: ")));
        fileNameField = new JTextField(12);
        scriptPanel.add(fileNameField);

        // varPanel.add(new JLabel("  Trigger variables (comma separated list, use '*' for all):"), BorderLayout.NORTH);

        JButton selectVars = new JButton("...");
        selectVars.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                SelectVariables dlg = new SelectVariables(frame);
                if (dlg.result != null) {
                    triggerVariables.setText(dlg.result);
                }
            }
        });

        JPanel variablesPanel = new JPanel();

        triggerVariables = new JTextField(triggerVars, 30);

        JPanel centerPane = new JPanel();
        centerPane.setLayout(new BorderLayout());

        variablesPanel.add(triggerVariables);
        variablesPanel.add(selectVars);

        centerPane.add(variablesPanel, BorderLayout.NORTH);
        // centerPane.add(new JScrollPane(scriptBody), BorderLayout.CENTER);
        varPanel.add(centerPane, BorderLayout.CENTER);

        varPanel.add(new JLabel("  "), BorderLayout.WEST);
        varPanel.add(new JLabel("  "), BorderLayout.EAST);

        controlPanel = new JPanel();
        getContentPane().add(controlPanel, BorderLayout.SOUTH);

        okButton = new JButton(Language.translate("Create New Script"));
        okButton.addActionListener(this);
        controlPanel.add(okButton);

        cancelButton = new JButton(Language.translate("Cancel"));
        cancelButton.addActionListener(this);
        controlPanel.add(cancelButton);

        pack();

        this.fileNameField.setText(fileNames[scriptTypeList.getSelectedIndex()].replace("script.", strFileName + "."));

        final ScriptDialogExtended dlg = this;

        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
            }
        });

        setLocationRelativeTo(frame);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (okButton == e.getSource()) {
            File templateFile = new File(strTemplateDir + fileNames[scriptTypeList.getSelectedIndex()]);

            try {
                BufferedReader in = new BufferedReader(new FileReader(templateFile));
                File tempFile = File.createTempFile("" + System.currentTimeMillis(), "" + System.currentTimeMillis());
                PrintWriter out = new PrintWriter(new FileWriter(tempFile));
                String line;

                while ((line = in.readLine()) != null) {
                    for (int i = 0; i < replace.length; i++) {
                        line = line.replace(replace[i][0], replace[i][1]);
                    }

                    out.println(line);
                }

                out.flush();
                out.close();

                File file = panel.createNewScript(this.fileNameField.getText(), tempFile, triggerVariables.getText(), true);
                if (file != null) {
                    bOK = true;
                    setVisible(false);
                    ScriptsTablePanel.editScriptExternal(file);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (cancelButton == e.getSource()) {
            bOK = false;
            setVisible(false);
        } else if (scriptTypeList == e.getSource()) {
            this.fileNameField.setText(fileNames[scriptTypeList.getSelectedIndex()].replace("script.", strFileName + "."));
        }
    }

    public static void main(String args[]) {
        String[] typeStrings = ScriptPluginFactory.getScriptTitles();/*{"Javascript (.js)", "Python (.py)",
            "BeanShell (.bsh)", "Groovy (.groovy)", "Ruby Script (.rb)"
        // "Prolog Script (.pl)", "TCL Script (.tcl)", "Sleep Script (.sl)", "Jaskell Script (.jsl)",
        // "JAWK Script (.awk)",
        // "Amico Script (.ams)"
        }; // , "XSLT Script (.xsl)" };*/

        String fileNames[] = ScriptPluginFactory.getScriptDefaultFiles();/*new String[]{"script.js", "script.py",
            "script.bsh", "script.groovy", "script.rb"
        // "script.pl", "script.tcl", "script.sl", "script.jsl",
        // "script.awk)",
        // "script.ams"
        }; // , "script.xsl" };*/
        ScriptDialogExtended dlg = new ScriptDialogExtended(null, null,
                "Serialize",
                "serialize-var-1",
                "var-1", typeStrings, fileNames, null,
                SketchletContextUtils.getSketchletDesignerConfDir() + "scripts/templates-serialize/", true);
    }
}