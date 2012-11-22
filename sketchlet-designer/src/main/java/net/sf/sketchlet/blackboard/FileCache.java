/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.blackboard;

import net.sf.sketchlet.common.file.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zobrenovic
 */
public class FileCache {
    private static final Logger log = Logger.getLogger(FileCache.class);
    private static Map<String, FileCacheEntry> fileMap = new HashMap<String, FileCacheEntry>();

    public static String getContent(String strFileURL) {
        FileCacheEntry fce = fileMap.get(strFileURL);
        if (fce == null) {
            fce = new FileCacheEntry();
            fce.setFileURL(strFileURL);
            if (fileMap.size() < 1000) {
                fileMap.put(strFileURL, fce);
            }

            log.info("loading file " + strFileURL);
        }

        return fce.getContent();
    }
}

class FileCacheEntry {
    private static final Logger log = Logger.getLogger(FileCacheEntry.class);

    private String fileURL = "";
    private long timestamp = 0l;
    private String content = null;

    public FileCacheEntry() {
    }

    public void refresh() {
        this.timestamp = System.currentTimeMillis();
    }

    public String getContent() {
        File file = new File(getFileURL());
        if (content == null) {
            try {
                if (file.exists()) {
                    content = FileUtils.getFileText(getFileURL());
                    this.timestamp = file.lastModified();
                } else {
                    content = FileUtils.getStringFromInputStream(new URL(getFileURL()).openStream());
                }
            } catch (Exception e) {
                log.info("Error while reading the file '" + getFileURL() + "'");
                this.timestamp = file.lastModified() + 86400000;
                content = "Error while reading the file '" + getFileURL() + "'";
                return "";
            }
        } else {
            try {
                if (file.exists() && file.lastModified() > this.timestamp) {
                    content = FileUtils.getFileText(getFileURL());
                    this.timestamp = file.lastModified();
                }
            } catch (Exception e) {
                this.timestamp = file.lastModified() + 86400000;
                content = "Error while reading the file '" + getFileURL() + "'";
                return "";
            }
        }
        if (content != null && content.length() > 100000) {
            content = content.substring(0, 100000);
        }
        return content == null ? "" : content;
    }

    public String getFileURL() {
        return fileURL;
    }

    public void setFileURL(String fileURL) {
        this.fileURL = fileURL;
    }
}