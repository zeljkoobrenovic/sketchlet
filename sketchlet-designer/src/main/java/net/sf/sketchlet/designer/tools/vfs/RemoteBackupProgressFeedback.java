/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.sketchlet.designer.tools.vfs;

public interface RemoteBackupProgressFeedback {

    public void setProgress(int numberOfCopiedFiles, int totalNumberOfFiles, String currentFile, boolean finished);

    public void error(String message);
}