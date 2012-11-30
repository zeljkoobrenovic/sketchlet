package net.sf.sketchlet.designer.editor.ui.desktop;

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

    public SyntaxPanel() {
        initComponents();
        jCmbLangs.setModel(new DefaultComboBoxModel(DefaultSyntaxKit.getContentTypes()));
        jCmbLangs.setSelectedItem("text/xml");

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
        jCmbLangs.addItemListener(new java.awt.event.ItemListener() {

            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCmbLangsItemStateChanged(evt);
            }
        });

        jToolBar1.setRollover(true);
        jToolBar1.setFocusable(false);

        setLayout(new BorderLayout());
        add(jToolBar1, BorderLayout.NORTH);
        add(jScrollPane1, BorderLayout.CENTER);
        add(jCmbLangs, BorderLayout.SOUTH);
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

            String oldText = jEditor.getText();

            jEditor.setContentType(lang);
            jToolBar1.removeAll();
            EditorKit kit = jEditor.getEditorKit();
            if (kit instanceof DefaultSyntaxKit) {
                DefaultSyntaxKit defaultSyntaxKit = (DefaultSyntaxKit) kit;
                defaultSyntaxKit.addToolBarActions(jEditor, jToolBar1);
            }
            jToolBar1.validate();
            try {
                jEditor.read(new StringReader(oldText), lang);
            } catch (IOException ex) {
                Logger.getLogger(SyntaxTester.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        jEditor.requestFocusInWindow();
    }

    public void setContentType(String type) {
        jCmbLangs.setSelectedItem(type);
    }

    private javax.swing.JComboBox jCmbLangs;
    public javax.swing.JEditorPane jEditor;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
}
