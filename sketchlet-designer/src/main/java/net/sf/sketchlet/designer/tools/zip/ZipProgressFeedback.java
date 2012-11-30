package net.sf.sketchlet.designer.tools.zip;

interface ZipProgressFeedback {
    public void setProgress(int numberOfCopiedFiles, int totalNumberOfFiles, boolean bFinished, boolean bError);

    public void error(String message);
}