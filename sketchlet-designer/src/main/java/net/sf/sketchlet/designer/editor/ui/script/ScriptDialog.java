package net.sf.sketchlet.designer.editor.ui.script;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.editor.ui.variables.SelectVariables;
import net.sf.sketchlet.loaders.pluginloader.ScriptPluginFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * @author zobrenovic
 */
public class ScriptDialog extends JDialog implements ActionListener {

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

    public ScriptDialog(ScriptsTablePanel panel, final JFrame frame, boolean modal) {
        super(frame, modal);

        this.panel = panel;

        this.setTitle(Language.translate("New Script"));

        scriptPanel = new JPanel();
        varPanel = new JPanel();
        varPanel.setLayout(new BorderLayout());

        getContentPane().add(scriptPanel, BorderLayout.NORTH);
        //getContentPane().add(varPanel, BorderLayout.CENTER);

        scriptPanel.add(new JLabel(Language.translate(" Language: ")));
        String[] typeStrings = ScriptPluginFactory.getScriptTitles();/*{"Javascript (.js)", "Python (.py)",
        "BeanShell (.bsh)", "Groovy (.groovy)", "Ruby Script (.rb)",
        "Prolog Script (.pl)", "TCL Script (.tcl)", "Sleep Script (.sl)", "Jaskell Script (.jsl)",
        // "JAWK Script (.awk)",
        "Amico Script (.ams)"
        }; // , "XSLT Script (.xsl)" };*/

        fileNames = ScriptPluginFactory.getScriptDefaultFiles();/*new String[]{"script.js", "script.py",
        "script.bsh", "script.groovy", "script.rb",
        "script.pl", "script.tcl", "script.sl", "script.jsl",
        // "script.awk)",
        "script.ams"
        }; // , "script.xsl" };*/
        scriptTypeList = new JComboBox(typeStrings);
        if (typeStrings.length > 0) {
            scriptTypeList.setSelectedIndex(0);
        }
        scriptTypeList.addActionListener(this);
        scriptPanel.add(scriptTypeList);

        scriptPanel.add(new JLabel(Language.translate("   File name: ")));
        fileNameField = new JTextField(12);
        scriptPanel.add(fileNameField);

        // varPanel.add(new JLabel("  Trigger variables (comma separated list, use '*' for all):"), BorderLayout.NORTH);

        triggerVariables = new JTextField("*", 30);
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

        JPanel centerPane = new JPanel();
        variablesPanel.add(triggerVariables);
        variablesPanel.add(selectVars);

        centerPane.add(variablesPanel, BorderLayout.NORTH);
        //varPanel.add(variablesPanel, BorderLayout.CENTER);

        //varPanel.add(new JLabel("  "), BorderLayout.WEST);
        //varPanel.add(new JLabel("  "), BorderLayout.EAST);

        controlPanel = new JPanel();
        getContentPane().add(controlPanel, BorderLayout.SOUTH);

        okButton = new JButton(Language.translate("Create New Script"));
        okButton.addActionListener(this);
        controlPanel.add(okButton);

        this.getRootPane().setDefaultButton(okButton);

        cancelButton = new JButton(Language.translate("Cancel"));
        cancelButton.addActionListener(this);
        controlPanel.add(cancelButton);

        pack();

        if (scriptTypeList.getSelectedIndex() >= 0) {
            this.fileNameField.setText(fileNames[scriptTypeList.getSelectedIndex()]);
        }

        final ScriptDialog dlg = this;

        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
            }
        });

        setLocationRelativeTo(panel);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (okButton == e.getSource()) {
            File templateFile = new File(SketchletContextUtils.getSketchletDesignerConfDir() + "scripts/templates/" + fileNames[scriptTypeList.getSelectedIndex()]);
            File file = panel.createNewScript(this.fileNameField.getText(), templateFile, triggerVariables.getText(), true);
            if (file != null) {
                bOK = true;
                setVisible(false);
                ScriptsTablePanel.editScript(file);
            }
        } else if (cancelButton == e.getSource()) {
            bOK = false;
            setVisible(false);
        } else if (scriptTypeList == e.getSource()) {
            this.fileNameField.setText(fileNames[scriptTypeList.getSelectedIndex()]);
        }
    }

    public static void main(String args[]) {
        ScriptDialog dlg = new ScriptDialog(null, null, true);
    }
}
