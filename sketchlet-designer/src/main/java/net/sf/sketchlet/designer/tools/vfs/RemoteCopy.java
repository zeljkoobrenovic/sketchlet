/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.tools.vfs;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.Selectors;

import javax.swing.*;

/**
 * @author cuypers
 */
public class RemoteCopy {
    private boolean stopped = false;

    public static final int OVEWRITE_ASK = 0;
    public static final int OVEWRITE_ALL = 1;
    public static final int OVEWRITE_ALL_OLDER = 2;
    public static final int SKIP_ALL = 3;
    public static final int CANCEL = 4;
    private int overwriteAction = OVEWRITE_ASK;

    void copyFile(FileObject sourceFileObject, FileObject targetFileObject, JFrame frame) throws Exception {
        if (this.isStopped()) {
            return;
        }

        try {
            boolean bCopy = false;

            if (targetFileObject.exists()) {
                if (getOverwriteAction() == OVEWRITE_ASK) {
                    Object[] options = {"Overwrite",
                            "Overwrite all", "Overwrite all older",
                            "Skip", "Skip all",
                            "Cancel"
                    };
                    int answer = JOptionPane.showOptionDialog(frame,
                            "File '" + targetFileObject.getName().getBaseName() + "' already exists. " + "Do you want to overwrite it?",
                            "Overwrite",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[2]);

                    switch (answer) {
                        case 0:
                            bCopy = true;
                            setOverwriteAction(OVEWRITE_ASK);
                            break;
                        case 1:
                            bCopy = true;
                            setOverwriteAction(OVEWRITE_ALL);
                            break;
                        case 2:
                            bCopy = targetFileObject.getContent().getLastModifiedTime() < sourceFileObject.getContent().getLastModifiedTime();
                            setOverwriteAction(OVEWRITE_ALL_OLDER);
                            break;
                        case 3: // skip now, ask next time
                            bCopy = false;
                            setOverwriteAction(OVEWRITE_ASK);
                            break;
                        case 4:
                            bCopy = false;
                            setOverwriteAction(SKIP_ALL);
                            break;
                        case 5:
                            bCopy = false;
                            setOverwriteAction(CANCEL);
                            setStopped(true);
                            break;
                    }
                } else {
                    switch (getOverwriteAction()) {
                        case OVEWRITE_ASK:
                        case OVEWRITE_ALL:
                            bCopy = true;
                            break;
                        case OVEWRITE_ALL_OLDER:
                            bCopy = targetFileObject.getContent().getLastModifiedTime() < sourceFileObject.getContent().getLastModifiedTime();
                            break;
                        case SKIP_ALL:
                        case CANCEL:
                            bCopy = false;
                            break;
                    }
                }
            } else {
                bCopy = getOverwriteAction() != CANCEL;
            }

            if (bCopy) {
                targetFileObject.copyFrom(sourceFileObject, Selectors.SELECT_ALL);
            }

        } catch (Exception e) {
            throw e;
        }
    }

    public int getOverwriteAction() {
        return overwriteAction;
    }

    public void setOverwriteAction(int overwriteAction) {
        this.overwriteAction = overwriteAction;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
}
