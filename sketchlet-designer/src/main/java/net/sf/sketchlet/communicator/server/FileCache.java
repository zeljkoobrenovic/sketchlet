/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.communicator.server;

import net.sf.sketchlet.common.file.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.util.Hashtable;

/**
 * @author zobrenovic
 */
public class FileCache {
    private static final Logger log = Logger.getLogger(FileCache.class);
    static Hashtable<String, FileCacheEntry> hashtable = new Hashtable<String, FileCacheEntry>();

    public static String getContent(String strFileURL) {
        FileCacheEntry fce = hashtable.get(strFileURL);
        if (fce == null) {
            fce = new FileCacheEntry();
            fce.strFileURL = strFileURL;
            if (hashtable.size() < 1000) {
                hashtable.put(strFileURL, fce);
            }

            log.info("loading file " + strFileURL);
        }

        return fce.getContent();
    }
}

class FileCacheEntry {
    private static final Logger log = Logger.getLogger(FileCacheEntry.class);

    String strFileURL = "";
    long timestamp = 0l;
    String strContent = null;

    public FileCacheEntry() {
    }

    public void refresh() {
        this.timestamp = System.currentTimeMillis();
    }

    public String getContent() {
        File file = new File(strFileURL);
        if (strContent == null) {
            try {
                if (file.exists()) {
                    strContent = FileUtils.getFileText(strFileURL);
                    this.timestamp = file.lastModified();
                } else {
                    strContent = FileUtils.getStringFromInputStream(new URL(strFileURL).openStream());
                }
            } catch (Exception e) {
                log.info("Error while reading the file '" + strFileURL + "'");
                this.timestamp = file.lastModified() + 86400000;
                strContent = "Error while reading the file '" + strFileURL + "'";
                return "";
            }
        } else {
            try {
                if (file.exists() && file.lastModified() > this.timestamp) {
                    strContent = FileUtils.getFileText(strFileURL);
                    this.timestamp = file.lastModified();
                }
            } catch (Exception e) {
                this.timestamp = file.lastModified() + 86400000;
                strContent = "Error while reading the file '" + strFileURL + "'";
                return "";
            }
        }
        if (strContent != null && strContent.length() > 100000) {
            strContent = strContent.substring(0, 100000);
        }
        return strContent == null ? "" : strContent;
    }
}