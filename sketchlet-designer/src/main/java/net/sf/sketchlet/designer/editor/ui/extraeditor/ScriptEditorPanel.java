/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.ui.extraeditor;

import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.desktop.Notepad;
import net.sf.sketchlet.script.ScriptPluginProxy;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class ScriptEditorPanel extends JPanel {

    private RSyntaxTextArea editor = Notepad.getInstance();
    File file = null;
    JButton btnRun = new JButton(Language.translate("Run"), Workspace.createImageIcon("resources/start.gif"));
    JButton btnStop = new JButton(Language.translate("Stop"), Workspace.createImageIcon("resources/stop2.gif"));
    JButton btnDecl = new JButton(Language.translate("Declarations"));
    JButton btnExt = new JButton(Language.translate("Extensions"));
    JButton btnReload = new JButton(Language.translate("Reload"));

    ScriptEditorPanel() {
        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new BorderLayout());

        // create the embedded JTextComponent
        btnRun.setMargin(new Insets(0, 0, 0, 0));
        btnStop.setMargin(new Insets(0, 0, 0, 0));
        btnDecl.setMargin(new Insets(0, 0, 0, 0));
        btnExt.setMargin(new Insets(0, 0, 0, 0));
        btnReload.setMargin(new Insets(0, 0, 0, 0));

        DefaultCompletionProvider provider = new DefaultCompletionProvider() {
            public boolean isAutoActivateOkay(JTextComponent tc) {
                try {
                    Document doc = tc.getDocument();
                    String lastTwoCharactersTyped = doc.getText(tc.getCaretPosition() - 9, 10);
                    return (lastTwoCharactersTyped.equals("sketchlet."));
                } catch (BadLocationException e) {
                }
                return false;
            }
        };
        provider.setAutoActivationRules(false, null);
        provider.setAutoActivationRules(true, "sketchlet.");
        for (String varName[] : ScriptPluginProxy.getMethods()) {
            String inputText = varName[1];
            if (inputText.startsWith("sketchlet.")) {
                String name = varName[1].replace("sketchlet.", "");
                String replacementText = name + "(" + replaceTypesForValue(varName[2]) + ")";
                provider.addCompletion(new ShorthandCompletion(provider, replacementText, replacementText));
            }
        }

        AutoCompletion ac = new AutoCompletion(provider);
        ac.setAutoActivationDelay(180);
        ac.setAutoActivationEnabled(true);
        ac.install(editor);

        provider = new DefaultCompletionProvider() {
            public boolean isAutoActivateOkay(JTextComponent tc) {
                try {
                    Document doc = tc.getDocument();
                    String lastTwoCharactersTyped = doc.getText(tc.getCaretPosition() - 8, 9);
                    return (lastTwoCharactersTyped.equals("graphics."));
                } catch (BadLocationException e) {
                }
                return false;
            }
        };
        provider.setAutoActivationRules(false, null);
        provider.setAutoActivationRules(true, "graphics.");
        for (String varName[] : ScriptPluginProxy.getMethods()) {
            String inputText = varName[1];
            if (inputText.startsWith("graphics.")) {
                String name = varName[1].replace("graphics.", "");
                String replacementText = name + "(" + replaceTypesForValue(varName[2]) + ")";
                provider.addCompletion(new ShorthandCompletion(provider, replacementText, replacementText));
            }
        }

        ac = new AutoCompletion(provider);
        ac.setAutoActivationDelay(180);
        ac.setAutoActivationEnabled(true);
        ac.install(editor);


        setAutoComplete(editor, "f", new String[][]{{"for (i = 0; i < 10; i++)", "for (i = 0; i < 10; i++) {\n}"}, {"for (;;)", "for (;;) {\n}"}});
        setAutoComplete(editor, "for", new String[][]{{"for (i = 0; i < 10; i++)", "for (i = 0; i < 10; i++) {\n}"}, {"for (;;)", "for (;;) {\n}"}});
        setAutoComplete(editor, "i", new String[][]{{"if ()", "if () {\n}"}, {"if () else", "if (true) {\n} else {\n}"}});
        setAutoComplete(editor, "if", new String[][]{{"if ()", "if () {\n}"}, {"if () else", "if (true) {\n} else {\n}"}});
        setAutoComplete(editor, "w", new String[][]{{"while ()", "while(true) {\n}"}, {"w", "w"}});
        setAutoComplete(editor, "while", new String[][]{{"while ()", "while(true) {\n}"}, {"w", "w"}});
        setAutoComplete(editor, "s", new String[][]{{"switch ()", "switch(choice) {\n\tcase 0:\n\t\t break;\n}"}, {"switch () default", "switch(choice) {\n\tcase 0:\n\t\t break;\n" +
                "\tdefault:\n" +
                "\t\t break;\n}"}});
        setAutoComplete(editor, "switch", new String[][]{{"switch ()", "switch(choice) {\n\tcase 0:\n\t\t break;\n}"}, {"switch () default", "switch(choice) {\n\tcase 0:\n\t\t break;\n" +
                "\tdefault:\n" +
                "\t\t break;\n}"}});

        setAutoComplete(editor, "s", new String[][]{{"sketchlet", "sketchlet"}, {"s", "s"}});
        setAutoComplete(editor, "g", new String[][]{{"graphics", "graphics"}, {"g", "g"}});

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add("Center", new RTextScrollPane(editor));
        add("Center", panel);

        btnRun.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                saveDocument();
                int index = SketchletEditor.getInstance().getExtraEditorPanel().scriptEditorExtraPanel.tabs.getSelectedIndex();
                Workspace.getMainPanel().sketchletPanel.panel2.table.getSelectionModel().setSelectionInterval(index, index);
                Workspace.getMainPanel().sketchletPanel.panel2.startScript();
            }
        });
        btnStop.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                saveDocument();
                int index = SketchletEditor.getInstance().getExtraEditorPanel().scriptEditorExtraPanel.tabs.getSelectedIndex();
                Workspace.getMainPanel().sketchletPanel.panel2.table.getSelectionModel().setSelectionInterval(index, index);
                Workspace.getMainPanel().sketchletPanel.panel2.stopScript();
            }
        });
        btnDecl.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int index = SketchletEditor.getInstance().getExtraEditorPanel().scriptEditorExtraPanel.tabs.getSelectedIndex();
                ScriptPluginProxy s = DataServer.scripts.get(index);
                s.showContext(SketchletEditor.editorFrame);
            }
        });
        btnExt.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int index = SketchletEditor.getInstance().getExtraEditorPanel().scriptEditorExtraPanel.tabs.getSelectedIndex();
                ScriptPluginProxy s = DataServer.scripts.get(index);
                s.showExtensions(SketchletEditor.editorFrame);
            }
        });
        btnReload.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int index = SketchletEditor.getInstance().getExtraEditorPanel().scriptEditorExtraPanel.tabs.getSelectedIndex();
                ScriptPluginProxy s = DataServer.scripts.get(index);
                openFile(s.getScriptFile());
            }
        });

        GridLayout gridLayout = new GridLayout(0, 1);
        gridLayout.setVgap(0);
        JPanel buttons = new JPanel(gridLayout);
        buttons.add(btnRun);
        buttons.add(btnStop);
        buttons.add(btnDecl);
        buttons.add(btnExt);
        buttons.add(btnReload);
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(buttons, BorderLayout.NORTH);
        JPanel panelEast = new JPanel();
        panelEast.add(buttons);
        add("East", panelEast);
    }

    private String replaceTypesForValue(String params) {
        params = params.replace("String", "\"\"");
        params = params.replace("int", "0");
        params = params.replace("double", "0.0");
        params = params.replace("float", "0.0");

        return params;
    }

    public void openFile(File file) {
        openFile(file.getPath());
    }

    public void openFile(String strPath) {
        this.file = new File(strPath);
        editor.setText(FileUtils.getFileText(file.getPath()));

        if (strPath.endsWith("js")) {
            editor.setSyntaxEditingStyle("text/javascript");
        } else if (strPath.endsWith("java")) {
            editor.setSyntaxEditingStyle("text/java");
        } else if (strPath.endsWith("py")) {
            editor.setSyntaxEditingStyle("text/python");
        } else if (strPath.endsWith(".xsl") || strPath.endsWith(".xslt")) {
            editor.setSyntaxEditingStyle("text/xml");
        } else if (strPath.endsWith(".bsh")) {
            editor.setSyntaxEditingStyle("text/java");
        } else if (strPath.endsWith(".groovy")) {
            editor.setSyntaxEditingStyle("text/groovy");
        } else if (strPath.endsWith(".c")) {
            editor.setSyntaxEditingStyle("text/c");
        } else if (strPath.endsWith(".cpp")) {
            editor.setSyntaxEditingStyle("text/cpp");
        } else if (strPath.endsWith(".cs")) {
            editor.setSyntaxEditingStyle("text/cpp");
        } else if (strPath.endsWith(".h")) {
            editor.setSyntaxEditingStyle("text/cpp");
        } else if (strPath.endsWith(".tcl")) {
        } else if (strPath.endsWith(".rb")) {
            editor.setSyntaxEditingStyle("text/ruby");
        } else if (strPath.endsWith(".sl")) {
        } else if (strPath.endsWith(".jsl")) {
        } else if (strPath.endsWith(".pl")) {
        } else if (strPath.endsWith(".awk")) {
        } else if (strPath.endsWith(".ams")) {
        } else if (strPath.endsWith(".html")) {
            editor.setSyntaxEditingStyle("text/xhtml");
        } else if (strPath.endsWith(".htm")) {
            editor.setSyntaxEditingStyle("text/xhtml");
        } else if (strPath.endsWith(".jsp")) {
            editor.setSyntaxEditingStyle("text/xhtml");
        } else if (strPath.endsWith(".asp")) {
            editor.setSyntaxEditingStyle("text/xhtml");
        } else {
            editor.setSyntaxEditingStyle("text/xml");
        }
    }

    public void saveDocument() {
        if (file == null) {
            JFileChooser chooser = new JFileChooser();
            int ret = chooser.showSaveDialog(SketchletEditor.editorFrame);

            if (ret != JFileChooser.APPROVE_OPTION) {
                return;
            }

            file = chooser.getSelectedFile();
        }

        FileUtils.saveFileText(file.getPath(), editor.getText());
    }

    public static DefaultCompletionProvider setAutoComplete(RSyntaxTextArea editor, final String strActivate, String[][] shorthand) {
        DefaultCompletionProvider provider = new DefaultCompletionProvider() {
            public boolean isAutoActivateOkay(JTextComponent tc) {
                try {
                    Document doc = tc.getDocument();
                    String lastCharactersTyped = doc.getText(tc.getCaretPosition() - (strActivate.length() - 1), strActivate.length());
                    return (lastCharactersTyped.equals(strActivate));
                } catch (BadLocationException e) {
                }
                return false;
            }
        };
        provider.setAutoActivationRules(false, null);
        provider.setAutoActivationRules(true, strActivate);
        for (String str[] : shorthand) {
            provider.addCompletion(new ShorthandCompletion(provider, str[0], str[1]));
        }

        AutoCompletion ac = new AutoCompletion(provider);
        ac.setAutoActivationDelay(180);
        ac.setAutoActivationEnabled(true);
        ac.install(editor);

        return provider;
    }

}
