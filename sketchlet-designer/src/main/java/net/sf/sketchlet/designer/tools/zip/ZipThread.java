/*
 * ZipThread.java
 *
 * Created on April 22, 2008, 3:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.tools.zip;

import javax.swing.*;
import java.util.concurrent.CountDownLatch;

class ZipThread implements Runnable {

    Thread t = new Thread(this);
    String zipFile;
    String directory;
    String files[];
    String strExclude;
    ZipProgressFeedback feedback;
    JFrame frame;
    CountDownLatch countDownLatch = new CountDownLatch(1);

    public ZipThread(String zipFile, String directory, ZipProgressFeedback feedback, JFrame frame, String strExclude) {
        this.feedback = feedback;
        this.zipFile = zipFile;
        this.directory = directory;
        this.files = null;
        this.frame = frame;
        this.strExclude = strExclude;

        t.start();
    }

    public ZipThread(String zipFile, String directory, String files[], ZipProgressFeedback feedback, JFrame frame, String strExclude) {
        this.feedback = feedback;
        this.zipFile = zipFile;
        this.directory = directory;
        this.files = files;
        this.frame = frame;
        this.strExclude = strExclude;

        t.start();
    }

    public void run() {
        if (this.files != null) {
            new JarUtil(this.feedback).zipFiles(this.files, this.zipFile, this.strExclude);
        } else {
            new JarUtil(this.feedback).zipFiles(new String[]{this.directory}, this.zipFile, this.strExclude);
        }

        countDownLatch.countDown();
    }

    public void await() {
        try {
            this.countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
