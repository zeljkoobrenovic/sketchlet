/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.common.notepad;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.text.EditorKit;
import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.SyntaxTester;

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

        //lblCaretPos = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditor = new javax.swing.JEditorPane();
        //lblToken = new javax.swing.JLabel();
        jCmbLangs = new javax.swing.JComboBox();
        jToolBar1 = new javax.swing.JToolBar();

        //lblCaretPos.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        //lblCaretPos.setText("Caret Position");

        jEditor.setContentType("");
        jEditor.setFont(new java.awt.Font("Monospaced", 0, 13));
        jEditor.setCaretColor(new java.awt.Color(153, 204, 255));
        jScrollPane1.setViewportView(jEditor);

        //lblToken.setFont(new java.awt.Font("Courier New", 0, 12));
        //lblToken.setText("Token under cursor");

        jCmbLangs.setMaximumRowCount(20);
        jCmbLangs.setFocusable(false);
        jCmbLangs.addItemListener(new java.awt.event.ItemListener()   {

            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCmbLangsItemStateChanged(evt);
            }
        });

        jToolBar1.setRollover(true);
        jToolBar1.setFocusable(false);

        /*
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 848, Short.MAX_VALUE).addComponent(jScrollPane1).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap().addComponent(jCmbLangs, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 574, Short.MAX_VALUE).addContainerGap()).addGroup(layout.createSequentialGroup().addContainerGap().addGap(484, 484, 484)));
        layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(0, 0, 0).addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 387, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jCmbLangs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addContainerGap()));
         */

        setLayout(new BorderLayout());
        add(jToolBar1, BorderLayout.NORTH);
        add(jScrollPane1, BorderLayout.CENTER);
        // add(jCmbLangs, BorderLayout.SOUTH);
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
