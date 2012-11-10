/*
 * FileManager.java
 *
 * Created on April 3, 2008, 12:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.sketchlet.designer.tools.vfs;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileUtil;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.sftp.SftpFileSystemConfigBuilder;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * A simple command-line shell for performing file operations.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @author Gary D. Gregory
 * @version $Id:Shell.java 232419 2005-08-13 07:23:40 +0200 (Sa, 13 Aug 2005) imario $
 */
public class Shell {
    private static final Logger log = Logger.getLogger(Shell.class);
    private static final String CVS_ID = "$Id:Shell.java 232419 2005-08-13 07:23:40 +0200 (Sa, 13 Aug 2005) imario $";
    private final FileSystemManager mgr;
    private FileObject cwd;
    private BufferedReader reader;

    public static void main(final String[] args) {
        try {
            FileSystemManager fsm = VFS.getManager();
            FileSystemOptions fso = new FileSystemOptions();
            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(fso, "no");

            FileObject sourceFileObject = fsm.resolveFile("file:C:/Program Files/amico0.3.0/README.txt", fso);
            String sourceFilePath = sourceFileObject.getName().getPath();

            // StaticUserAuthenticator auth = new StaticUserAuthenticator("", "obrenovic", "");
            StaticUserAuthenticator auth = new StaticUserAuthenticator("", "obrenovi", "");
            FileSystemOptions opts = new FileSystemOptions();
            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);

            FileObject targetFileObject = fsm.resolveFile("sftp://mekong.ins.cwi.nl/ufs/obrenovi/Desktop/README.txt", opts);

            FileObject tfo = targetFileObject;
            if (tfo.getType() == FileType.FOLDER) {
                tfo = targetFileObject.resolveFile(sourceFileObject.getName().getBaseName());
            }

            copyFile(sourceFileObject, tfo);

            (new Shell()).go();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }


    private static void copyFile(FileObject sourceFileObject, FileObject targetFileObject) {
        try {
            targetFileObject.copyFrom(sourceFileObject, Selectors.SELECT_ALL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Shell() throws FileSystemException {
        mgr = VFS.getManager();
        cwd = mgr.resolveFile(System.getProperty("user.dir"));
        reader = new BufferedReader(new InputStreamReader(System.in));
    }

    private void go() throws Exception {
        log.info("VFS Shell [" + CVS_ID + "]");
        while (true) {
            final String[] cmd = nextCommand();
            if (cmd == null) {
                return;
            }
            if (cmd.length == 0) {
                continue;
            }
            final String cmdName = cmd[0];
            if (cmdName.equalsIgnoreCase("exit") || cmdName.equalsIgnoreCase("quit")) {
                return;
            }
            try {
                handleCommand(cmd);
            } catch (final Exception e) {
                System.err.println("Command failed:");
                e.printStackTrace(System.err);
            }
        }
    }

    /**
     * Handles a command.
     */
    private void handleCommand(final String[] cmd) throws Exception {
        final String cmdName = cmd[0];
        if (cmdName.equalsIgnoreCase("cat")) {
            cat(cmd);
        } else if (cmdName.equalsIgnoreCase("cd")) {
            cd(cmd);
        } else if (cmdName.equalsIgnoreCase("cp")) {
            cp(cmd);
        } else if (cmdName.equalsIgnoreCase("help")) {
            help();
        } else if (cmdName.equalsIgnoreCase("ls")) {
            ls(cmd);
        } else if (cmdName.equalsIgnoreCase("pwd")) {
            pwd();
        } else if (cmdName.equalsIgnoreCase("rm")) {
            rm(cmd);
        } else if (cmdName.equalsIgnoreCase("touch")) {
            touch(cmd);
        } else {
            log.error("Unknown command \"" + cmdName + "\".");
        }
    }

    /**
     * Does a 'help' command.
     */
    private void help() {
        log.info("Commands:");
        log.info("cat <file>         Displays the contents of a file.");
        log.info("cd [folder]        Changes current folder.");
        log.info("cp <src> <dest>    Copies a file or folder.");
        log.info("help               Shows this message.");
        log.info("ls [-R] [path]     Lists contents of a file or folder.");
        log.info("pwd                Displays current folder.");
        log.info("rm <path>          Deletes a file or folder.");
        log.info("touch <path>       Sets the last-modified time of a file.");
        log.info("exit       Exits this program.");
        log.info("quit       Exits this program.");
    }

    /**
     * Does an 'rm' command.
     */
    private void rm(final String[] cmd) throws Exception {
        if (cmd.length < 2) {
            throw new Exception("USAGE: rm <path>");
        }

        final FileObject file = mgr.resolveFile(cwd, cmd[1]);
        file.delete(Selectors.SELECT_SELF);
    }

    /**
     * Does a 'cp' command.
     */
    private void cp(final String[] cmd) throws Exception {
        if (cmd.length < 3) {
            throw new Exception("USAGE: cp <src> <dest>");
        }

        final FileObject src = mgr.resolveFile(cwd, cmd[1]);
        FileObject dest = mgr.resolveFile(cwd, cmd[2]);
        if (dest.exists() && dest.getType() == FileType.FOLDER) {
            dest = dest.resolveFile(src.getName().getBaseName());
        }

        dest.copyFrom(src, Selectors.SELECT_ALL);
    }

    /**
     * Does a 'cat' command.
     */
    private void cat(final String[] cmd) throws Exception {
        if (cmd.length < 2) {
            throw new Exception("USAGE: cat <path>");
        }

        // Locate the file
        final FileObject file = mgr.resolveFile(cwd, cmd[1]);

        // Dump the contents to System.out
        FileUtil.writeContent(file, System.out);
    }

    /**
     * Does a 'pwd' command.
     */
    private void pwd() {
        log.info("Current folder is " + cwd.getName());
    }

    /**
     * Does a 'cd' command.
     * If the taget directory does not exist, a message is printed to <code>System.err</code>.
     */
    private void cd(final String[] cmd) throws Exception {
        final String path;
        if (cmd.length > 1) {
            path = cmd[1];
        } else {
            path = System.getProperty("user.home");
        }

        // Locate and validate the folder
        FileObject tmp = mgr.resolveFile(cwd, path);
        if (tmp.exists()) {
            cwd = tmp;
        } else {
            log.info("Folder does not exist: " + tmp.getName());
        }
        log.info("Current folder is " + cwd.getName());
    }

    /**
     * Does an 'ls' command.
     */
    private void ls(final String[] cmd) throws FileSystemException {
        int pos = 1;
        final boolean recursive;
        if (cmd.length > pos && cmd[pos].equals("-R")) {
            recursive = true;
            pos++;
        } else {
            recursive = false;
        }

        final FileObject file;
        if (cmd.length > pos) {
            file = mgr.resolveFile(cwd, cmd[pos]);
        } else {
            file = cwd;
        }

        if (file.getType() == FileType.FOLDER) {
            // List the contents
            log.info("Contents of " + file.getName());
            listChildren(file, recursive, "");
        } else {
            // Stat the file
            log.info(file.getName());
            final FileContent content = file.getContent();
            log.info("Size: " + content.getSize() + " bytes.");
            final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
            final String lastMod = dateFormat.format(new Date(content.getLastModifiedTime()));
            log.info("Last modified: " + lastMod);
        }
    }

    /**
     * Does a 'touch' command.
     */
    private void touch(final String[] cmd) throws Exception {
        if (cmd.length < 2) {
            throw new Exception("USAGE: touch <path>");
        }
        final FileObject file = mgr.resolveFile(cwd, cmd[1]);
        if (!file.exists()) {
            file.createFile();
        }
        file.getContent().setLastModifiedTime(System.currentTimeMillis());
    }

    /**
     * Lists the children of a folder.
     */
    private void listChildren(final FileObject dir,
                              final boolean recursive,
                              final String prefix)
            throws FileSystemException {
        final FileObject[] children = dir.getChildren();
        for (int i = 0; i < children.length; i++) {
            final FileObject child = children[i];
            System.out.print(prefix);
            System.out.print(child.getName().getBaseName());
            if (child.getType() == FileType.FOLDER) {
                log.info("/");
                if (recursive) {
                    listChildren(child, recursive, prefix + "    ");
                }
            }
        }
    }

    /**
     * Returns the next command, split into tokens.
     */
    private String[] nextCommand() throws IOException {
        System.out.print("> ");
        final String line = reader.readLine();
        if (line == null) {
            return null;
        }
        final ArrayList cmd = new ArrayList();
        final StringTokenizer tokens = new StringTokenizer(line);
        while (tokens.hasMoreTokens()) {
            cmd.add(tokens.nextToken());
        }
        return (String[]) cmd.toArray(new String[cmd.size()]);
    }
}
