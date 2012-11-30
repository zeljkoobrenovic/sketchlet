package net.sf.sketchlet.designer.tools.vfs;

import net.sf.sketchlet.common.mail.PasswordDialog;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.sftp.SftpFileSystemConfigBuilder;

import javax.swing.*;
import java.io.File;
import java.net.URLDecoder;


public class RestoreRemoteBackup extends RemoteCopy {

    private int numberOfCopiedFiles = 0;
    private int totalNumberOfFiles = 0;
    private RemoteBackupProgressFeedback feedback;

    public RestoreRemoteBackup(RemoteBackupProgressFeedback feedback) {
        this.feedback = feedback;
    }

    public void doRestore(String remoteFolder, String directory, JFrame frame) {
        numberOfCopiedFiles = 0;

        try {
            if (!PasswordDialog.showPasswordDialog(frame, RemoteBackup.userInfo)) {
                this.feedback.setProgress(0, 0, "Canceled.", true);
                return;
            }

            StaticUserAuthenticator auth = new StaticUserAuthenticator("", RemoteBackup.userInfo.getUsername(), RemoteBackup.userInfo.getPassword());
            FileSystemOptions opts = new FileSystemOptions();
            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);

            numberOfCopiedFiles = 0;
            totalNumberOfFiles = 0;
            this.feedback.setProgress(0, 0, "Counting files...", false);
            this.countFiles(remoteFolder, "", opts);

            doRestoreRemoteBackup(remoteFolder, directory, "", opts, frame);

            this.feedback.setProgress(0, 0, "Done.", true);
        } catch (Exception e) {
            e.printStackTrace();
            feedback.error("Could not copy files.\nTry again and check username and password.");
        }
    }

    public void doRestoreRemoteBackup(String remoteFolder, String localFolder, String relativePath, FileSystemOptions opts, JFrame frame) {
        if (this.isStopped()) {
            return;
        }
        try {
            FileSystemManager fsm = VFS.getManager();
            FileSystemOptions fso = new FileSystemOptions();
            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(fso, "no");

            if (!remoteFolder.endsWith("/") && !remoteFolder.endsWith("\\")) {
                remoteFolder += "/";
            }

            FileObject remoteFileObject = fsm.resolveFile(remoteFolder + relativePath.replace("\\", "/"), opts);

            if (remoteFileObject.getType() == FileType.FOLDER) {
                FileObject children[] = remoteFileObject.getChildren();
                for (int i = 0; i < children.length; i++) {
                    if (this.isStopped()) {
                        return;
                    }
                    doRestoreRemoteBackup(remoteFolder, localFolder, relativePath + "/" + children[i].getName().getBaseName(), opts, frame);
                }
            } else {
                numberOfCopiedFiles++;
                this.feedback.setProgress(numberOfCopiedFiles, totalNumberOfFiles, "Copying " + relativePath, false);
                String strLocalFile;

                if (localFolder.endsWith("/") || localFolder.endsWith("\\")) {
                    strLocalFile = localFolder + relativePath;
                } else {
                    strLocalFile = localFolder + File.separator + relativePath;
                }

                File file = new File(strLocalFile);

                FileObject localFileObject = fsm.resolveFile(URLDecoder.decode(file.toURL().toString(), "UTF8"), fso);

                copyFile(remoteFileObject, localFileObject, frame);
            }
        } catch (Exception e) {
            e.printStackTrace();
            feedback.error("Could not copy files.\nTry again and check username and password.");
        }
    }

    public void countFiles(String remoteFolder, String relativePath, FileSystemOptions opts) {
        if (this.isStopped()) {
            return;
        }

        try {
            FileSystemManager fsm = VFS.getManager();
            FileSystemOptions fso = new FileSystemOptions();
            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(fso, "no");

            if (!remoteFolder.endsWith("/") && !remoteFolder.endsWith("\\")) {
                remoteFolder += "/";
            }

            FileObject remoteFileObject = fsm.resolveFile(remoteFolder + relativePath.replace("\\", "/"), opts);

            if (remoteFileObject.getType() == FileType.FOLDER) {
                FileObject children[] = remoteFileObject.getChildren();
                for (int i = 0; i < children.length; i++) {
                    if (this.isStopped()) {
                        return;
                    }
                    countFiles(remoteFolder, relativePath + "/" + children[i].getName().getBaseName(), opts);
                }
            } else {
                totalNumberOfFiles++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            feedback.error("Could not copy files.\nTry again and check username and password.");
        }

        return;
    }
}

class RestoreBackupThread implements Runnable {

    Thread t = new Thread(this);
    String remoteDirectory;
    String directory;
    String files[];
    RestoreRemoteBackup remoteBackup;
    RemoteBackupProgressFeedback feedback;
    JFrame frame;

    public RestoreBackupThread(String remoteDirectory, String directory, RemoteBackupProgressFeedback feedback, JFrame frame) {
        this.feedback = feedback;
        this.remoteDirectory = remoteDirectory;
        this.directory = directory;
        this.files = null;
        this.frame = frame;

        t.start();
    }

    public void run() {
        this.remoteBackup = new RestoreRemoteBackup(this.feedback);
        this.remoteBackup.doRestore(this.remoteDirectory, this.directory, this.frame);
    }
}



