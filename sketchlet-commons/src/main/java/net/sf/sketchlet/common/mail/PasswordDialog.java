/*
 * PasswordDialog.java
 *
 * Created on April 11, 2008, 2:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.sketchlet.common.mail;

import net.sf.sketchlet.common.mail.UserInfo;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class PasswordDialog extends JDialog implements ActionListener {
    private JTextField username = new JTextField(10);
    private JPasswordField password= new JPasswordField(10);
    
    private boolean okPressed;
    
    private JButton okButton;
    private JButton cancelButton;
    
    public PasswordDialog(JFrame parent) {
        super(parent, "Authentication", true);
        Container contentPane = getContentPane();
        JPanel p1 = new JPanel();
        p1.setLayout( new BoxLayout( p1, BoxLayout.PAGE_AXIS ) );
        
        JPanel p1_1 = new JPanel();
        p1_1.setLayout( new FlowLayout( FlowLayout.LEFT ) );
        p1_1.add(new JLabel(" User name: "));
        p1_1.add(username);
        p1.add(p1_1);
        
        JPanel p1_2 = new JPanel();
        p1_2.setLayout( new FlowLayout( FlowLayout.LEFT ) );
        p1_2.add(new JLabel(" Password:  "));
        p1_2.add(password);
        p1.add(p1_2);
        
        contentPane.add(p1, BorderLayout.CENTER);
        
        Panel p2 = new Panel();
        okButton = addButton(p2, "OK");
        cancelButton = addButton(p2, "Cancel");
        contentPane.add(p2, BorderLayout.SOUTH);
        
        this.getRootPane().setDefaultButton( okButton );
        
        pack();
        
        this.setLocationRelativeTo( parent );
        
        password.requestFocus();
    }
    
    private JButton addButton(Container c, String name) {
        JButton button = new JButton(name);
        button.addActionListener(this);
        c.add(button);
        return button;
    }
    
    public void actionPerformed(ActionEvent evt) {
        Object source = evt.getSource();
        if (source == okButton) {
            okPressed = true;
            setVisible(false);
        } else if (source == cancelButton)
            setVisible(false);
    }
    
    public boolean showDialog(UserInfo transfer) {
        username.setText(transfer.username);
        password.setText(transfer.password);
        okPressed = false;
        
        setVisible( true );
        
        if (okPressed) {
            transfer.username = username.getText();
            transfer.password = new String(password.getPassword());
        }
        
        return okPressed;
    }
    
    public static boolean showPasswordDialog( JFrame parent, UserInfo info ) {
        PasswordDialog dlg = new PasswordDialog( parent );
        return dlg.showDialog( info );
    }
    
    public static void main(String args[]) {
        showPasswordDialog( null, new UserInfo( "a", "b" ) );
    }
}
