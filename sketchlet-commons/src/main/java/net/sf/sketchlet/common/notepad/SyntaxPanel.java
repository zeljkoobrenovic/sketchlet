/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.common.notepad;

import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.SyntaxTester;

import javax.swing.*;
import javax.swing.text.EditorKit;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SyntaxPanel extends javax.swing.JPanel {

    /** Creates new form Tester */
    public SyntaxPanel() {
        initComponents();
        jCmbLangs.setModel(new DefaultComboBoxModel(DefaultSyntaxKit.getContentTypes()));
        // jEditor.setContentType(jCmbLangs.getItemAt(0).toString());
        jCmbLangs.setSelectedItem("text/xml");
        //new CaretMonitor(jEditor, lblCaretPos);

    }

    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditor = new javax.swing.JEditorPane();
        jCmbLangs = new javax.swing.JComboBox();
        jToolBar1 = new javax.swing.JToolBar();

        jEditor.setContentType("");
        jEditor.setFont(new java.awt.Font("Monospaced", 0, 13));
        jEditor.setCaretColor(new java.awt.Color(153, 204, 255));
        jScrollPane1.setViewportView(jEditor);

        jCmbLangs.setMaximumRowCount(20);
        jCmbLangs.setFocusable(false);
        jCmbLangs.addItemListener(new java.awt.event.ItemListener()   {

            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCmbLangsItemStateChanged(evt);
            }
        });

        jToolBar1.setRollover(true);
        jToolBar1.setFocusable(false);

        setLayout(new BorderLayout());
        add(jToolBar1, BorderLayout.NORTH);
        add(jScrollPane1, BorderLayout.CENTER);
    }

    public String getText() {
        return this.jEditor.getText();
    }

    public void setText(String strText) {
        this.jEditor.setText(strText);
    }

    private void jCmbLangsItemStateChanged(java.awt.event.ItemEvent evt) {
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            String lang = jCmbLangs.getSelectedItem().toString();

            // save the state of the current JEditorPane, as it's Document is about
            // to be replaced.
            String oldText = jEditor.getText();

            // install a new DefaultSyntaxKit on the JEditorPane for the requested language.
            jEditor.setContentType(lang);
            // Recreate the Toolbar
            jToolBar1.removeAll();
            EditorKit kit = jEditor.getEditorKit();
            if (kit instanceof DefaultSyntaxKit) {
                DefaultSyntaxKit defaultSyntaxKit = (DefaultSyntaxKit) kit;
                defaultSyntaxKit.addToolBarActions(jEditor, jToolBar1);
            }
            jToolBar1.validate();
            try {
                // setText should not be called (read the JavaDocs).  Better use the read
                // method and create a new document.
                jEditor.read(new StringReader(oldText), lang);
            } catch (IOException ex) {
                Logger.getLogger(SyntaxTester.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        jEditor.requestFocusInWindow();
    }
    public javax.swing.JComboBox jCmbLangs;
    public javax.swing.JEditorPane jEditor;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    // private javax.swing.JLabel lblCaretPos;
    // private javax.swing.JLabel lblToken;
}
