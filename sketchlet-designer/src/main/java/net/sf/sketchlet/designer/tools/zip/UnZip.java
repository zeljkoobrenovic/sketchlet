/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.tools.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UnZip {

    public static void unzipArchive(File archive, File outputDir) throws Exception {
        ZipFile zipfile = new ZipFile(archive);
        for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
            ZipEntry entry = (ZipEntry) e.nextElement();
            unzipEntry(zipfile, entry, outputDir);
        }
    }

    public static boolean checkZip(File archive) {
        try {
            JarFile jarfile = new JarFile(archive);
            for (Enumeration e = jarfile.entries(); e.hasMoreElements(); ) {
                JarEntry entry = (JarEntry) e.nextElement();
                InputStream inputStream = jarfile.getInputStream(entry);
                int c;
                while ((c = inputStream.read()) != -1) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static void unzipEntry(ZipFile zipfile, ZipEntry entry, File outputDir) throws IOException {

        if (entry.isDirectory()) {
            createDir(new File(outputDir, entry.getName()));
            return;
        }

        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }

        BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try {
            copy(inputStream, outputStream);
        } finally {
            outputStream.close();
            inputStream.close();
        }
    }

    private static void createDir(File dir) {
        if (!dir.mkdirs()) {
        }
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        if (in == null) {
            throw new NullPointerException("InputStream is null!");
        }
        if (out == null) {
            throw new NullPointerException("OutputStream is null");
        }

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
}
