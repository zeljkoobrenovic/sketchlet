/*
 * ZipProgressFeedback.java
 *
 * Created on April 22, 2008, 3:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.sketchlet.designer.tools.zip;

interface ZipProgressFeedback {
    public void setProgress(int numberOfCopiedFiles, int totalNumberOfFiles, boolean bFinished, boolean bError);

    public void error(String message);
}