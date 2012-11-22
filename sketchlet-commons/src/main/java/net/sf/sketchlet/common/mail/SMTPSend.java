package net.sf.sketchlet.common.mail;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;

import javax.swing.*;
import java.io.File;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Vector;


/**
 * Demo app that shows how to construct and send an RFC822
 * (singlepart) message.
 * <p/>
 * XXX - allow more than one recipient on the command line
 * <p/>
 * This is just a variant of msgsend.java that demonstrates use of
 * some SMTP-specific features.
 *
 * @author Max Spivak
 * @author Bill Shannon
 */

public class SMTPSend {
    public static void main(String[] args) {
        SMTPSend obj = new SMTPSend();
        String server = "smtp.gmail.com";
        String userName = "amico.test.account";
        String fromAddres = "amico.test.account@gmail.com";
        String toAddres = "zeljko.obrenovic@cwi.nl";
        String cc = "";
        String bcc = "";
        boolean htmlFormat = false;
        boolean secure = true;
        String subject = "tema";
        String body = "prueba";

        obj.sendMail(server, userName, fromAddres, new String[]{toAddres}, new String[]{cc}, new String[]{bcc}, htmlFormat, secure, subject, body, null, null, null);

    }

    static String password = null;

    static UserInfo userInfo = new UserInfo("", "");

    public static boolean sendMail(String server, String userName, String fromAddress, String toAddress[], String cc[], String bcc[],
                                   boolean htmlFormat, boolean secure, String subject, String body,
                                   Vector<String> images, Vector<String> files, JFrame frame) {
        if (password == null) {
            userInfo.setUsername(userName);
            PasswordDialog.showPasswordDialog(frame, userInfo);
            userName = userInfo.getUsername();
            password = userInfo.getPassword();
        }

        try {

            // Create the email message
            HtmlEmail email = new HtmlEmail();
            email.setHostName(server);
            email.setAuthentication(userName, password);
            // email.setSSL( secure );
            email.setTLS(true);
            email.setSmtpPort(587);

            for (int i = 0; i < toAddress.length; i++) {
                String strAddress = toAddress[i].trim();
                if (strAddress.equals("")) {
                    continue;
                }
                email.addTo(strAddress, strAddress);
            }
            for (int i = 0; i < cc.length; i++) {
                String strAddress = cc[i].trim();
                if (strAddress.equals("")) {
                    continue;
                }
                email.addCc(strAddress, strAddress);
            }
            for (int i = 0; i < bcc.length; i++) {
                String strAddress = bcc[i].trim();
                if (strAddress.equals("")) {
                    continue;
                }
                email.addBcc(strAddress, strAddress);
            }

            email.setFrom(fromAddress, fromAddress);

            email.setSubject(subject);

            // email.setBoolHasAttachments( true );

            int i = 0;
            for (String image : images) {
                String cid = email.embed(new File(image));
                body = body.replace("cid:image" + i++, "cid:" + cid);
            }

            email.setHtmlMsg(body);

            for (String filename : files) {
                EmailAttachment attachment = new EmailAttachment();
                attachment.setPath(filename);
                attachment.setDisposition(EmailAttachment.ATTACHMENT);
                attachment.setDescription(new File(filename).getName());
                attachment.setName(new File(filename).getName());

                email.attach(attachment);
            }

            email.buildMimeMessage();

            email.send();

        } catch (Exception e) {
            password = null;
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame,
                    "Could not notify (send email message failed)." + ((e.getMessage() != null) ? "\nError message: " + e.getMessage() : ""),
                    "Notify error",
                    JOptionPane.ERROR_MESSAGE);

            return false;
        }


        return true;
    }
}

class MyPasswordAuthenticator extends Authenticator {
    String user;
    String pw;

    public MyPasswordAuthenticator(String username, String password) {
        super();
        this.user = username;
        this.pw = password;
    }

    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, pw.toCharArray());
    }
}

