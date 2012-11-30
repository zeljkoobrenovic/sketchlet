package net.sf.sketchlet.designer.tools.vfs;

import net.sf.sketchlet.common.mail.PasswordDialog;
import net.sf.sketchlet.common.mail.UserInfo;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.sftp.SftpFileSystemConfigBuilder;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.net.URLDecoder;
import java.util.StringTokenizer;

public class RemoteBackup extends RemoteCopy {
    private static final Logger log = Logger.getLogger(RemoteBackup.class);
    int numberOfCopiedFiles = 0;
    int totalNumberOfFiles = 0;
    RemoteBackupProgressFeedback feedback;
    public static UserInfo userInfo = new UserInfo("", "");

    public RemoteBackup(RemoteBackupProgressFeedback feedback) {
        this.feedback = feedback;
    }

    public void doBackup(String remoteFolder, String directory, JFrame frame, String strExclude) {
        numberOfCopiedFiles = 0;
        totalNumberOfFiles = getNumberOfFiles(directory);

        try {
            if (!userInfo.getUsername().trim().equals("")) {
                if (!PasswordDialog.showPasswordDialog(frame, userInfo)) {
                    this.feedback.setProgress(0, 0, "Canceled", true);
                    return;
                }
            }

            StaticUserAuthenticator auth = new StaticUserAuthenticator("", userInfo.getUsername(), userInfo.getPassword());
            FileSystemOptions opts = new FileSystemOptions();
            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);
            doRemoteBackup(remoteFolder, directory, "", opts, frame, strExclude);

            this.feedback.setProgress(1, 1, "Done.", true);
        } catch (Exception e) {
            e.printStackTrace();
            setStopped(true);
            feedback.error("Could not copy files.\nTry again and check username and password.");
        }
    }

    public void doBackup(String remoteFolder, String directory, String files[], JFrame frame, String strExclude) {
        numberOfCopiedFiles = 0;
        totalNumberOfFiles = getNumberOfFiles(directory, files);

        try {
            if (!userInfo.getUsername().trim().equals("")) {
                if (!PasswordDialog.showPasswordDialog(frame, userInfo)) {
                    this.feedback.setProgress(0, 0, "Canceled", true);
                    return;
                }
            }

            StaticUserAuthenticator auth = new StaticUserAuthenticator("", userInfo.getUsername(), userInfo.getPassword());
            FileSystemOptions opts = new FileSystemOptions();
            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);

            for (int i = 0; i < files.length; i++) {
                if (!isStopped()) {
                    doRemoteBackup(remoteFolder, directory, files[i].replace("[", "").replace("]", ""), opts, frame, strExclude);
                }
            }

            this.feedback.setProgress(1, 1, "Finished", true);
        } catch (Exception e) {
            e.printStackTrace();
            setStopped(true);
            feedback.error("Could not copy files.\nTry again and check username and password.");
        }
    }

    public int getNumberOfFiles(String directory) {
        int numberOfFiles = 0;
        File file = new File(directory);

        if (file.isDirectory()) {
            String filenames[] = file.list();

            for (int i = 0; i < filenames.length; i++) {
                if (!filenames[i].equalsIgnoreCase("index")) {
                    numberOfFiles += getNumberOfFiles(directory + File.separator + filenames[i]);
                }
            }
        } else {
            numberOfFiles = 1;
        }

        return numberOfFiles;
    }

    public int getNumberOfFiles(String directory, String files[]) {
        int numberOfFiles = 0;

        if (!directory.endsWith("/") && !directory.endsWith("\\")) {
            directory += File.separator;
        }

        for (int f = 0; f < files.length; f++) {
            String strFile = directory + files[f].replace("[", "").replace("]", "");
            File file = new File(strFile);

            if (file.isDirectory()) {
                numberOfFiles += getNumberOfFiles(strFile);
            } else {
                numberOfFiles += 1;
            }
        }

        return numberOfFiles;
    }

    public boolean shouldExclude(String strPath, String strExclude) {
        StringTokenizer t = new StringTokenizer(strExclude, ", ;\t");
        while (t.hasMoreTokens()) {
            if (strPath.endsWith("." + t.nextToken())) {
                return true;
            }
        }

        return false;
    }

    public void doRemoteBackup(String remoteFolder, String root, String relativePath, FileSystemOptions opts, JFrame frame, String strExclude) {
        String strSourceFile;

        if (root.endsWith("/") || root.endsWith("\\")) {
            strSourceFile = root + relativePath;
        } else {
            strSourceFile = root + File.separator + relativePath;
        }

        File file = new File(strSourceFile);

        if (file.isDirectory()) {
            String filenames[] = file.list();

            for (int i = 0; i < filenames.length; i++) {
                if (isStopped()) {
                    return;
                }
                if (!filenames[i].equalsIgnoreCase("index")) {
                    doRemoteBackup(remoteFolder, root, relativePath + File.separator + filenames[i], opts, frame, strExclude);
                }
            }
        } else {
            try {

                FileSystemManager fsm = VFS.getManager();
                FileSystemOptions fso = new FileSystemOptions();
                SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(fso, "no");

                FileObject sourceFileObject = fsm.resolveFile(URLDecoder.decode(file.toURL().toString(), "UTF8"), fso);

                if (!remoteFolder.endsWith("/") && !remoteFolder.endsWith("\\")) {
                    remoteFolder += "/";
                }

                FileObject targetFileObject = fsm.resolveFile(remoteFolder + relativePath.replace("\\", "/"), opts);

                FileObject tfo = targetFileObject;
                if (tfo.getType() == FileType.FOLDER) {
                    tfo = targetFileObject.resolveFile(sourceFileObject.getName().getBaseName());
                }

                if (this.feedback != null) {
                    this.feedback.setProgress(numberOfCopiedFiles - 1, totalNumberOfFiles, "Copying '" + relativePath + "'", false);
                }

                if (strExclude.trim().equals("") || (!shouldExclude(relativePath, strExclude))) {
                    copyFile(sourceFileObject, tfo, frame);
                }

                numberOfCopiedFiles++;

                if (this.feedback != null) {
                    this.feedback.setProgress(numberOfCopiedFiles, totalNumberOfFiles, "Copying '" + relativePath + "'", false);
                } else {
                    log.info(numberOfCopiedFiles + " / " + totalNumberOfFiles);
                    log.info((double) numberOfCopiedFiles / totalNumberOfFiles * 100);
                }

            } catch (Exception e) {
                e.printStackTrace();
                setStopped(true);
                feedback.error("Could not copy files.\nTry again and check username and password.");
            }
        }
    }

    private void copyFileOld(FileObject sourceFileObject, FileObject targetFileObject) {
        try {
            if (!targetFileObject.exists() || targetFileObject.getContent().getLastModifiedTime() < sourceFileObject.getContent().getLastModifiedTime()) {
                targetFileObject.copyFrom(sourceFileObject, Selectors.SELECT_ALL);
            }
        } catch (Exception e) {
            e.printStackTrace();
            setStopped(true);
            feedback.error("Could not copy files.\nTry again and check username and password.");
        }
    }
}

class RemoteBackupThread implements Runnable {

    Thread t = new Thread(this);
    String remoteDirectory;
    String directory;
    String files[];
    String strExclude;
    RemoteBackup remoteBackup;
    RemoteBackupProgressFeedback feedback;
    JFrame frame;

    public RemoteBackupThread(String remoteDirectory, String directory, RemoteBackupProgressFeedback feedback, JFrame frame, String strExclude) {
        this.feedback = feedback;
        this.remoteDirectory = remoteDirectory;
        this.directory = directory;
        this.files = null;
        this.frame = frame;
        this.strExclude = strExclude;

        t.start();
    }

    public RemoteBackupThread(String remoteDirectory, String directory, String files[], RemoteBackupProgressFeedback feedback, JFrame frame, String strExclude) {
        this.feedback = feedback;
        this.remoteDirectory = remoteDirectory;
        this.directory = directory;
        this.files = files;
        this.frame = frame;
        this.strExclude = strExclude;

        t.start();
    }

    public void run() {
        this.remoteBackup = new RemoteBackup(this.feedback);

        if (this.files == null) {
            this.remoteBackup.doBackup(this.remoteDirectory, this.directory, this.frame, strExclude);
        } else {
            this.remoteBackup.doBackup(this.remoteDirectory, this.directory, this.files, this.frame, strExclude);
        }
    }
}


