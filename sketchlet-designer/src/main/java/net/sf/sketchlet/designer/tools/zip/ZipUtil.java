package net.sf.sketchlet.designer.tools.zip;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.StringTokenizer;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author cuypers
 */
public class ZipUtil {
    private static final Logger log = Logger.getLogger(ZipUtil.class);

    private ZipProgressFeedback feedback;

    /**
     * Creates a new instance of ZipUtil
     */
    public ZipUtil(ZipProgressFeedback feedback) {
        this.feedback = feedback;
    }

    public static void main(String args[]) {
        try {
            //create a ZipOutputStream to zip the data to
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream("C:\\Documents and Settings\\cuypers\\Desktop\\scr.zip"));

            // zipDir("C:\\Documents and Settings\\cuypers\\Desktop\\scr", zos);
            //close the stream
            zos.close();
        } catch (Exception e) {
            e.printStackTrace();
            //handle exception
        }
    }

    public void zipFiles(String strFiles[], String strZipFile, String strExclude) {
        try {
            new File(strZipFile).getParentFile().mkdirs();
            new File(strZipFile).createNewFile();

            boolean bError = false;
            for (int r = 0; r < 5; r++) {
                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(strZipFile));

                for (int i = 0; i < strFiles.length; i++) {
                    File file = new File(strFiles[i]);
                    if (file.isDirectory()) {
                        zipDir(strFiles[i], zos, strExclude);
                    } else {
                        zipFile(strFiles[i], zos, strExclude);
                    }

                    this.feedback.setProgress(i + 1, strFiles.length, false, false);
                }

                zos.flush();
                zos.close();

                if (!UnZip.checkZip(new File(strZipFile))) {
                    log.error("Problem with reading ZIP file");
                    bError = true;
                } else {
                    bError = false;
                    break;
                }
            }

            this.feedback.setProgress(strFiles.length, strFiles.length, true, bError);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void zipFile(String strFile, ZipOutputStream zos, String strExclude) {
        if (this.shouldExclude(strFile, strExclude)) {
            return;
        }
        try {
            File f = new File(strFile);
            byte[] readBuffer = new byte[2156];
            int bytesIn = 0;

            FileInputStream fis = new FileInputStream(f);

            String zipPath = new File(SketchletContextUtils.getCurrentProjectDir()).toURI().relativize(new File(f.getPath()).toURI()).getPath();

            ZipEntry anEntry = new ZipEntry(zipPath);
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
    public void zipDir(String dir2zip, ZipOutputStream zos, String strExclude) {

        try {
            File zipDir = new File(dir2zip);

            if (zipDir.getName().equals("history") && zipDir.getParentFile().getName().equals(SketchletContextUtils.sketchletDataDir())) {
                return;
            }

            String[] dirList = zipDir.list();
            byte[] readBuffer = new byte[2156];
            int bytesIn = 0;

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

                ZipEntry anEntry = new ZipEntry(zipPath);
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
