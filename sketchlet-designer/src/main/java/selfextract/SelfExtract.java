/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package selfextract;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.designer.Workspace;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SelfExtract {
    private static final Logger log = Logger.getLogger(SelfExtract.class);

    public static int total = 1;
    public static int current = 0;

    public static void unzipArchive(File archive, File outputDir) throws Exception {
        ZipFile zipfile = new ZipFile(archive);
        total = zipfile.size();
        current = 0;
        MsgFrame.msgFrame.refresh();
        for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
            ZipEntry entry = (ZipEntry) e.nextElement();
            unzipEntry(zipfile, entry, outputDir);
        }
    }

    private static void unzipEntry(ZipFile zipfile, ZipEntry entry, File outputDir) throws IOException {

        if (entry.isDirectory()) {
            createDir(new File(outputDir, entry.getName()));
            return;
        }

        File outputFile = new File(outputDir, entry.getName());
        outputFile.deleteOnExit();

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

        current++;
        MsgFrame.msgFrame.refresh();
    }

    private static void createDir(File dir) {
        if (!dir.mkdirs()) {
        }

        dir.deleteOnExit();
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

    public static String getFileText(String fileName) {
        String text = "";

        if (new File(fileName).exists()) {
            try {
                BufferedReader in = new BufferedReader(new FileReader(fileName));

                String line;

                while ((line = in.readLine()) != null) {
                    text += line;
                }

                in.close();
            } catch (Exception e) {
                log.error("Could not open '" + fileName + "'. Returning empty string.", e);
            }
        }

        return text;
    }

    public static void main(String args[]) {
        try {
            if (args.length > 0 && args[0].equalsIgnoreCase("test")) {
                // JOptionPane.showMessageDialog(null, "The self-executable file is OK.");
                System.exit(0);
            }
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                log.error(e);
            }

            MsgFrame.showMessage(null, "Preparing files...");
            URL url = SelfExtract.class.getResource("SelfExtract.class");
            File file = null;
            String arch = "";
            try {
                arch = URLDecoder.decode(url.toURI().toString(), "UTF8").replace("jar:file:", "");
                int n = arch.indexOf("!/");
                if (n > 0) {
                    arch = arch.substring(1, n);
                    file = new File(arch);
                } else {
                    System.exit(0);
                }
            } catch (URISyntaxException e2) {
                arch = URLDecoder.decode(url.getPath().toString(), "UTF8").replace("jar:file:", "");
                int n = arch.indexOf("!/");
                if (n > 0) {
                    arch = arch.substring(1, n);
                    file = new File(arch);
                } else {
                    System.exit(0);
                }
            }

            File temp = File.createTempFile("sketchlet_", "");
            temp.delete();
            temp.mkdirs();
            temp.deleteOnExit();

            SelfExtract.unzipArchive(file, temp);

            MsgFrame.msgFrame.setMessage(Language.translate("Please wait..."));

            String prefix = SketchletContext.getInstance().getApplicationHomeDir();
            if (!prefix.endsWith("\\") || !prefix.endsWith("/")) {
                prefix += "/";
            }
            String cmd = getFileText(prefix + "conf/selfextract/selfextract/exec.txt");
            cmd = cmd.replace("${project-url}", "file:" + temp.getPath() + "\\" + SketchletContextUtils.sketchletDataDir() + "\\workspace.txt");
            cmd = cmd.replace("<%=project-url%>", "file:" + temp.getPath() + "\\" + SketchletContextUtils.sketchletDataDir() + "\\workspace.txt");
            cmd = Workspace.replaceSystemVariables(cmd);

            Process p = Runtime.getRuntime().exec(cmd);
            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
            StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");
            errorGobbler.start();
            outputGobbler.start();

            p.waitFor();

            System.exit(0);

        } catch (Exception e) {
            log.error(e);
        }
    }
}

class StreamGobbler extends Thread {
    private static final Logger log = Logger.getLogger(StreamGobbler.class);

    InputStream is;
    String type;

    StreamGobbler(InputStream is, String type) {
        this.is = is;
        this.type = type;
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                MsgFrame.closeMessage();
            }
        } catch (IOException ioe) {
            log.error(ioe);
        }
    }
}

class MsgFrame extends JDialog implements Runnable {

    boolean stopped = false;
    public JTextField message = new JTextField(20);
    JProgressBar progressBar = new JProgressBar();
    boolean finished = false;
    JFrame frame;
    Cursor originalCursor;
    Thread t = new Thread(this);
    static MsgFrame msgFrame;

    private MsgFrame(JFrame frame, String strMessage, final long timeout) {
        this(frame, strMessage);
        new Thread(new Runnable() {

            public void run() {
                try {
                    Thread.sleep(timeout);
                    msgFrame.close();
                } catch (Exception e) {
                }
            }
        }).start();
    }

    public static void showMessage(JFrame frame, String strMessage) {
        MsgFrame.closeMessage();
        msgFrame = new MsgFrame(frame, strMessage);
    }

    public static void showMessage(JFrame frame, String strMessage, long timeout) {
        MsgFrame.closeMessage();
        msgFrame = new MsgFrame(frame, strMessage, timeout);
    }

    private MsgFrame(JFrame frame, String strMessage) {
        super(frame, false);
        this.frame = frame;
        this.setUndecorated(true);
        this.setAlwaysOnTop(true);
        message.setEditable(false);
        message.setText(strMessage);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(message);
        this.getContentPane().add(panel);
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 6));
        panel.add(progressBar, BorderLayout.SOUTH);
        // progressBar.setIndeterminate(true);
        // progressBar.setPreferredSize(new Dimension(100, 8));
        if (frame != null) {
            originalCursor = frame.getCursor();
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
        // SwingUtilities.invokeLater(this);//t.start;();
        run();
    }

    public void refresh() {
        this.progressBar.setMaximum(SelfExtract.total);
        this.progressBar.setValue(SelfExtract.current);
    }

    public void run() {
        pack();
        this.setLocationRelativeTo(frame);
        setVisible(true);
    }

    public static void closeMessage() {
        if (msgFrame != null) {
            msgFrame.close();
        }
    }

    public void close() {
        if (msgFrame != null) {
            stopped = true;
            setVisible(false);
            msgFrame = null;
        }
        if (frame != null) {
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public void setMessage(String strMessage) {
        message.setText(strMessage);
    }

    public static void main(String args[]) {
        new MsgFrame(null, "test");
    }
}
