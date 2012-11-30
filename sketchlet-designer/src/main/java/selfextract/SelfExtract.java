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

    private static int totalNumberOfFiles = 1;
    private static int currentFileIndex = 0;

    public static void unzipArchive(File archive, File outputDir) throws Exception {
        ZipFile zipfile = new ZipFile(archive);
        setTotalNumberOfFiles(zipfile.size());
        setCurrentFileIndex(0);
        MessageFrame.getMessageFrame().refresh();
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

        setCurrentFileIndex(getCurrentFileIndex() + 1);
        MessageFrame.getMessageFrame().refresh();
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

            MessageFrame.showMessage(null, "Preparing files...");
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

            MessageFrame.getMessageFrame().setMessage(Language.translate("Please wait..."));

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

    public static int getTotalNumberOfFiles() {
        return totalNumberOfFiles;
    }

    public static void setTotalNumberOfFiles(int totalNumberOfFiles) {
        SelfExtract.totalNumberOfFiles = totalNumberOfFiles;
    }

    public static int getCurrentFileIndex() {
        return currentFileIndex;
    }

    public static void setCurrentFileIndex(int currentFileIndex) {
        SelfExtract.currentFileIndex = currentFileIndex;
    }
}

class StreamGobbler extends Thread {
    private static final Logger log = Logger.getLogger(StreamGobbler.class);

    private InputStream is;
    private String type;

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
                MessageFrame.closeMessage();
            }
        } catch (IOException ioe) {
            log.error(ioe);
        }
    }
}

class MessageFrame extends JDialog implements Runnable {

    private boolean stopped = false;
    private JTextField message = new JTextField(20);
    private JProgressBar progressBar = new JProgressBar();
    private JFrame frame;
    private Cursor originalCursor;
    private Thread t = new Thread(this);
    private static MessageFrame messageFrame;

    private MessageFrame(JFrame frame, String strMessage, final long timeout) {
        this(frame, strMessage);
        new Thread(new Runnable() {

            public void run() {
                try {
                    Thread.sleep(timeout);
                    getMessageFrame().close();
                } catch (Exception e) {
                }
            }
        }).start();
    }

    public static void showMessage(JFrame frame, String strMessage) {
        MessageFrame.closeMessage();
        messageFrame = new MessageFrame(frame, strMessage);
    }

    public static void showMessage(JFrame frame, String strMessage, long timeout) {
        MessageFrame.closeMessage();
        messageFrame = new MessageFrame(frame, strMessage, timeout);
    }

    private MessageFrame(JFrame frame, String strMessage) {
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

    public static MessageFrame getMessageFrame() {
        return messageFrame;
    }

    public void refresh() {
        this.progressBar.setMaximum(SelfExtract.getTotalNumberOfFiles());
        this.progressBar.setValue(SelfExtract.getCurrentFileIndex());
    }

    public void run() {
        pack();
        this.setLocationRelativeTo(frame);
        setVisible(true);
    }

    public static void closeMessage() {
        if (getMessageFrame() != null) {
            getMessageFrame().close();
        }
    }

    public void close() {
        if (getMessageFrame() != null) {
            stopped = true;
            setVisible(false);
            messageFrame = null;
        }
        if (frame != null) {
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public void setMessage(String strMessage) {
        message.setText(strMessage);
    }

    public static void main(String args[]) {
        new MessageFrame(null, "test");
    }
}
