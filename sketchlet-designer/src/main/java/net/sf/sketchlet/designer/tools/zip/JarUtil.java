package net.sf.sketchlet.designer.tools.zip;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;

/**
 * @author cuypers
 */
public class JarUtil {
    private static final Logger log = Logger.getLogger(JarUtil.class);
    private ZipProgressFeedback feedback;

    /**
     * Creates a new instance of JarUtil
     */
    public JarUtil(ZipProgressFeedback feedback) {
        this.feedback = feedback;
    }

    public static void main(String args[]) {
        try {
            //create a JarOutputStream to zip the data to
            JarOutputStream zos = new JarOutputStream(new FileOutputStream("C:\\Documents and Settings\\cuypers\\Desktop\\scr.zip"));
            zos.close();
        } catch (Exception e) {
            log.error(e);
        }
    }

    public void zipFiles(String strFiles[], String strJarFile, String strExclude) {
        try {
            new File(strJarFile).getParentFile().mkdirs();

            boolean bError = false;
            JarOutputStream zos = new JarOutputStream(new FileOutputStream(strJarFile));

            for (int i = 0; i < strFiles.length; i++) {
                File file = new File(strFiles[i]);
                if (file.isDirectory()) {
                    zipDir(strFiles[i], zos, strExclude);
                } else {
                    zipFile(strFiles[i], zos, strExclude);
                }

                this.feedback.setProgress(i, strFiles.length, false, false);
            }

            zos.flush();
            zos.close();

            this.feedback.setProgress(strFiles.length, strFiles.length, true, bError);
        } catch (Exception e) {
            log.error(e);
        }
    }

    public void zipFile(String strFile, JarOutputStream zos, String strExclude) {
        if (this.shouldExclude(strFile, strExclude)) {
            return;
        }
        try {
            File f = new File(strFile);
            byte[] readBuffer = new byte[2156];
            int bytesIn;

            FileInputStream fis = new FileInputStream(f);

            String zipPath = new File(SketchletContextUtils.getCurrentProjectDir()).toURI().relativize(new File(f.getPath()).toURI()).getPath();

            JarEntry anEntry = new JarEntry(zipPath);
            zos.putNextEntry(anEntry);

            CRC32 crc = new CRC32();
            crc.reset();

            while ((bytesIn = fis.read(readBuffer)) != -1) {
                crc.update(readBuffer, 0, bytesIn);
                zos.write(readBuffer, 0, bytesIn);
                zos.flush();
            }

            anEntry.setCrc(crc.getValue());
            zos.closeEntry();

            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    //here is the code for the method
    public void zipDir(String dir2zip, JarOutputStream zos, String strExclude) {

        try {
            File zipDir = new File(dir2zip);

            if (zipDir.getName().equals("history") && zipDir.getParentFile().getName().equals(SketchletContextUtils.sketchletDataDir())) {
                return;
            }

            String[] dirList = zipDir.list();
            byte[] readBuffer = new byte[2156];
            int bytesIn;

            for (int i = 0; i < dirList.length; i++) {
                File f = new File(zipDir, dirList[i]);
                if (f.isDirectory()) {
                    String filePath = f.getPath();
                    zipDir(filePath, zos, strExclude);

                    continue;
                } else {
                    if (this.shouldExclude(dirList[i], strExclude)) {
                        continue;
                    }
                }


                FileInputStream fis = new FileInputStream(f);

                String zipPath = new File(SketchletContextUtils.getCurrentProjectDir()).toURI().relativize(new File(f.getPath()).toURI()).getPath();

                JarEntry anEntry = new JarEntry(zipPath);
                zos.putNextEntry(anEntry);

                CRC32 crc = new CRC32();
                crc.reset();

                while ((bytesIn = fis.read(readBuffer)) != -1) {
                    crc.update(readBuffer, 0, bytesIn);
                    zos.write(readBuffer, 0, bytesIn);
                    zos.flush();
                }

                anEntry.setCrc(crc.getValue());
                zos.closeEntry();
                fis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
