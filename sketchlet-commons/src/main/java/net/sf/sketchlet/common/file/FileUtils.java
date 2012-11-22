/*
 * FileUtils.java
 *
 * Created on March 27, 2008, 2:53 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.sketchlet.common.file;

import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Vector;

public class FileUtils {
    private static final Logger log = Logger.getLogger(FileUtils.class);
    private static boolean stopped = false;

    public static void copyFile(File in, File out) {
        try {
            FileInputStream fis = new FileInputStream(in);
            FileOutputStream fos = new FileOutputStream(out);
            try {
                byte[] buf = new byte[1024];
                int i = 0;
                while ((i = fis.read(buf)) != -1) {
                    fos.write(buf, 0, i);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                if (fis != null) {
                    fis.close();
                }
                if (fos != null) {
                    fos.close();
                }
            }
        } catch (Exception e) {
            log.error("Could not copy file '" + in.getPath() + "' to file '" + out.getPath() + "'");
        }
    }

    public static void saveInputStreamToFile(File out, InputStream is) {
        try {
            FileOutputStream fos = new FileOutputStream(out);
            try {
                byte[] buf = new byte[1024];
                int i = 0;
                while ((i = is.read(buf)) != -1) {
                    fos.write(buf, 0, i);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }
        } catch (Exception e) {
            log.error("Could not save stream to file.");
        }
    }

    public static String getStringFromInputStream(InputStream input) {
        String response = new String("");
        try {
            InputStreamReader fis = new InputStreamReader(input);
            BufferedReader in = new BufferedReader(fis);

            String line;

            while ((line = in.readLine()) != null) {
                response += line + "\r\n";
            }

            in.close();
        } catch (Exception e) {
            log.error("Could not open the input stream.");
        }

        return response.toString();
    }

    public static void main(String args[]) throws Exception {
        FileUtils.copyFile(new File(args[0]), new File(args[1]));
    }

    public static boolean deleteDir(File dir) {
        if (FileUtils.emptyDir(dir)) {
            // The directory is now empty so delete it
            return dir.delete();
        } else {
            return false;
        }
    }

    public static boolean emptyDir(File dir) {
        if (dir.isDirectory()) {
            boolean bSuccess = true;
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    log.error("Could not delete " + new File(dir, children[i]).getPath());
                    bSuccess = false;
                }
            }

            return bSuccess;
        }

        return true;
    }

    public static String getFileText(String fileName) {
        return getFileText(new File(fileName));
    }

    public static String getFileText(File file) {
        String text = "";

        if (file.exists()) {
            try {
                BufferedReader in = new BufferedReader(new FileReader(file));

                String line;

                while ((line = in.readLine()) != null) {
                    text += line + "\r\n";
                }

                in.close();
            } catch (Exception e) {
                log.error("Could not open '" + file.getPath() + "'. Returning empty string.");
            }
        }

        return text;
    }

    public static void saveFileText(String fileName, String strText) {
        saveFileText(new File(fileName), strText);
    }

    public static void saveFileText(File file, String strText) {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(file));

            out.print(strText);

            out.flush();
            out.close();
        } catch (Exception e) {
            log.error("Could not save '" + file.getAbsolutePath() + "'.");
        }
    }

    public static void deleteSafe(File file) {
        try {
            if (file.exists()) {
                log.error(file.getPath());
                new File(file.getParent() + File.separator + "deleted").mkdirs();
                FileUtils.copyFile(file, new File(file.getParent() + "/deleted/" + file.getName()));
                if (!file.delete()) {
                    log.error("Could not delete " + new File(file.getParent() + "/deleted/" + file.getName()).getPath());
                }
            }
        } catch (Exception e) {
            log.error("Could not delete '" + file.getParent() + "'.");
        }
    }

    public static boolean copyURLToFile(URL url, File out, JTextField status, String strPrefix, String strPostfix) {
        try {
            InputStream fis = url.openStream();
            FileOutputStream fos = new FileOutputStream(out);
            int counter = 0;
            try {
                byte[] buf = new byte[1024];
                int i = 0;
                while ((i = fis.read(buf)) != -1 && !isStopped()) {
                    counter += i;
                    status.setText(strPrefix + " " + (counter / (1024 * 1024) + 1) + "m " + strPostfix);
                    fos.write(buf, 0, i);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                if (fis != null) {
                    fis.close();
                }
                if (fos != null) {
                    fos.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static Vector<String> getFileLines(String fileName) {
        Vector<String> lines = new Vector<String>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(fileName));

            String line;
            while ((line = in.readLine()) != null) {
                lines.add(line);
            }

            in.close();
        } catch (Exception e) {
            log.error("Could not open '" + fileName + "'");
        }

        return lines;
    }

    public static boolean copyURLToFile(URL url, File out) {
        try {
            InputStream fis = url.openStream();
            FileOutputStream fos = new FileOutputStream(out);
            try {
                byte[] buf = new byte[1024];
                int i = 0;
                while ((i = fis.read(buf)) != -1) {
                    fos.write(buf, 0, i);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                if (fis != null) {
                    fis.close();
                }
                if (fos != null) {
                    fos.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static void restore(String strDestination, String strDirectory, String subdirectory) {
        restore(strDestination, strDirectory, subdirectory, true, null);
    }

    public static void restore(boolean overwrite, String strDestination, String strDirectory, String subdirectory) {
        restore(overwrite, strDestination, strDirectory, subdirectory, true, null);
    }

    public static void restore(String strDestination, String strDirectory, String subdirectory, boolean bRecursive, String strContains) {
        restore(false, strDestination, strDirectory, subdirectory, bRecursive, strContains);
    }

    public static void restore(boolean overwrite, String strDestination, String strDirectory, String subdirectory, boolean bRecursive, String strContains) {
        try {
            if (subdirectory != null) {
                new File(strDestination + File.separator + subdirectory).mkdirs();
            }

            String files[];

            if (subdirectory == null) {
                files = new File(strDirectory).list();
            } else {
                files = new File(strDirectory + File.separator + subdirectory).list();
            }

            if (files != null) {

                for (int i = 0; i < files.length; i++) {
                    String filename = files[i];

                    if (strContains != null) {
                        if (!filename.toLowerCase().contains(strContains.toLowerCase())) {
                            continue;
                        }
                    }
                    File file;
                    if (subdirectory == null) {
                        file = new File(strDirectory + File.separator + filename);
                    } else {
                        file = new File(strDirectory + File.separator + subdirectory + File.separator + filename);
                    }

                    if (file.isDirectory() && bRecursive) {
                        String dir;
                        if (subdirectory != null) {
                            dir = subdirectory + File.separator + filename;
                        } else {
                            dir = filename;
                        }
                        restore(strDestination, strDirectory, dir);
                    } else {
                        String restoreFile;
                        if (subdirectory == null) {
                            restoreFile = strDestination + File.separator + filename;
                        } else {
                            restoreFile = strDestination + File.separator + subdirectory + File.separator + filename;
                        }

                        if (overwrite || !new File(restoreFile).exists()) {
                            FileUtils.copyFile(file, new File(restoreFile));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void restore(String strSource, String strDestination) {
        try {
            if (!strDestination.endsWith(File.separator) && !strDestination.endsWith("/")) {
                strDestination += File.separator;
            }
            String files[] = new File(strSource).list();

            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    String filename = files[i];

                    File srcFile = new File(strSource + File.separator + filename);

                    if (srcFile.isDirectory()) {
                        new File(strDestination + filename).mkdirs();
                        restore(srcFile.getPath(), strDestination + filename);
                    } else {
                        String restoreFile = strDestination + File.separator + filename;
                        FileUtils.copyFile(srcFile, new File(restoreFile));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isStopped() {
        return stopped;
    }

    public static void setStopped(boolean stopped) {
        FileUtils.stopped = stopped;
    }
}
