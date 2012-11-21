/*
 * ZipVersion.java
 *
 * Created on April 10, 2008, 12:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.tools.zip;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.GlobalProperties;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.CheckboxList;
import net.sf.sketchlet.model.Page;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/* ZipVersion.java requires no other files. */
public class ZipVersion extends JFrame implements KeyListener, ZipProgressFeedback {

    private JRadioButton zipBtn = new JRadioButton(Language.translate("Create simple ZIP archive"));
    private JRadioButton jarBtn = new JRadioButton(Language.translate("Create self-executable JAR file"));
    private JLabel label2 = new JLabel(Language.translate("Archive name:      "));
    private JLabel label3 = new JLabel(Language.translate(""));
    private JLabel label4 = new JLabel(Language.translate("Exclude extensions:      "));
    private JTextField remoteSubdirectoryField = new JTextField(20);
    private JTextField zipFileField = new JTextField(40);
    private JTextField excludeField = new JTextField(27);
    private JProgressBar progressBar = new JProgressBar();
    private CheckboxList fileList;
    private JButton createButton;
    private ZipThread thread;
    private static final JFileChooser fc = new JFileChooser();
    private JFrame frame = this;
    private String strRoot = "";

    public ZipVersion(String initName) {
        super(Language.translate("Archive Project"));

        if (GlobalProperties.get("archive-root") != null) {
            strRoot = GlobalProperties.get("archive-root");
        }

        this.setIconImage(Workspace.createImageIcon("resources/zip.gif").getImage());
        this.init(initName);
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    public static void createAndShowGUI(String initName) {
        //Create and set up the window.
        ZipVersion frame = new ZipVersion(initName);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                createAndShowGUI("");
            }
        });
    }

    private void init(String initName) {

        this.setTitle(Language.translate("Archive Project"));
        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));

        JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout(FlowLayout.LEFT));

        String remoteDirectory = SketchletContextUtils.getSketchletDesignerHome();
        zipBtn.setMnemonic(KeyEvent.VK_Z);
        jarBtn.setMnemonic(KeyEvent.VK_J);

        //Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(zipBtn);
        group.add(jarBtn);

        JPanel zipPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        zipPanel.add(jarBtn);
        zipPanel.add(zipBtn);

        jarBtn.setSelected(true);

        zipBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setFolder();
            }
        });

        jarBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setFolder();
            }
        });

        JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel2.add(label2);
        remoteSubdirectoryField.setText(initName);
        panel2.add(remoteSubdirectoryField);
        remoteSubdirectoryField.addKeyListener(this);

        JButton selectArchiveDir = new JButton(Language.translate("Change Folder..."));
        selectArchiveDir.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                fc.setDialogTitle(Language.translate("Select Archive Folder"));
                fc.setCurrentDirectory(new File(strRoot));
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = fc.showDialog(frame, Language.translate("Select Folder"));
                if (returnVal == fc.APPROVE_OPTION) {
                    strRoot = fc.getSelectedFile().getPath() + File.separator;
                    GlobalProperties.set("archive-root", strRoot);
                    GlobalProperties.save();
                    setFolder();
                }
            }
        });


        JButton uniqueName = new JButton(Language.translate("unique name"));
        uniqueName.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                Calendar date = Calendar.getInstance();
                String strName = "Version " + date.get(Calendar.YEAR) + "_" + (date.get(Calendar.MONTH) < Calendar.OCTOBER ? "0" : "") + (date.get(Calendar.MONTH) + 1) + "_" + (date.get(Calendar.DAY_OF_MONTH) < 10 ? "0" : "") + date.get(Calendar.DAY_OF_MONTH) + " at " + (date.get(Calendar.HOUR_OF_DAY) < 10 ? "0" : "") + date.get(Calendar.HOUR_OF_DAY) + "." + (date.get(Calendar.MINUTE) < 10 ? "0" : "") + date.get(Calendar.MINUTE) + "." + (date.get(Calendar.SECOND) < 10 ? "0" : "") + date.get(Calendar.SECOND);
                remoteSubdirectoryField.setText(strName);
                setFolder();
            }
        });

        // panel2.add(uniqueName);

        JPanel panel3 = new JPanel();
        panel3.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel3.add(label3);
        panel3.add(zipFileField);
        panel2.add(selectArchiveDir);
        zipFileField.setEditable(false);

        JPanel panel3b = new JPanel();
        panel3b.add(label4);
        excludeField.setText(FileUtils.getFileText(SketchletContextUtils.getCurrentProjectConfDir() + "archive_exclude.txt").trim());
        panel3b.add(this.excludeField);

        JPanel panel4 = new JPanel();
        panel4.setLayout(new FlowLayout(FlowLayout.LEFT));

        createButton = new JButton(Language.translate("Create Archive"));
        createButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        final ZipVersion parent = this;
        createButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                FileUtils.saveFileText(SketchletContextUtils.getCurrentProjectConfDir() + "archive_exclude.txt", excludeField.getText().trim());

                setFolder();

                List<String> list = new ArrayList<String>();
                final List<File> deleteFiles = new ArrayList<File>();
                if (jarBtn.isSelected()) {
                    selectStartSketch();
                    File files[] = new File(SketchletContextUtils.getSketchletDesignerConfDir() + "selfextract").listFiles();
                    FileUtils.restore(SketchletContextUtils.getSketchletDesignerConfDir() + "selfextract", SketchletContextUtils.getCurrentProjectDir());
                    for (File file : files) {
                        String strFile = SketchletContextUtils.getCurrentProjectDir() + file.getName();
                        list.add(strFile);
                        deleteFiles.add(new File(strFile));
                    }
                }
                Object values[] = fileList.getSelectedValues();
                for (Object fileName : values) {
                    String strFile = SketchletContextUtils.getCurrentProjectDir() + (fileName.toString()).replace("[", "").replace("]", "");
                    list.add(strFile);
                }

                thread = new ZipThread(zipFileField.getText(), SketchletContextUtils.getCurrentProjectDir(), list.toArray(new String[list.size()]), parent, parent, excludeField.getText());
                createButton.setEnabled(false);
                fileList.setEnabled(false);

                new Thread(new Runnable() {

                    public void run() {
                        thread.await();
                        for (File file : deleteFiles) {
                            FileUtils.deleteDir(file);
                        }
                    }
                }).start();
            }
        });
        panel4.add(createButton);

        JButton cancelButton = new JButton(Language.translate("Cancel"));
        cancelButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                if (thread != null) {
                    // thread..stopped = true;
                }
                setVisible(false);
            }
        });
        panel4.add(cancelButton);

        this.add(panel1);
        this.add(panel2);
        this.add(panel3);
        this.add(panel3b);
        this.add(createFilesPanel());

        this.progressBar.setStringPainted(true);
        this.progressBar.setPreferredSize(new Dimension(270, 25));
        panel4.add(this.progressBar);
        this.add(zipPanel);
        this.add(panel4);

        setFolder();

        this.getRootPane().setDefaultButton(createButton);
    }

    public void selectStartSketch() {
        String sketches[] = new String[SketchletEditor.getPages().getPages().size()];
        int i = 0;
        for (Page sk : SketchletEditor.getPages().getPages()) {
            sketches[i++] = sk.getTitle();
        }

        if (sketches.length == 0) {
            return;
        }

        String s = (String) JOptionPane.showInputDialog(
                frame,
                Language.translate("Select the start page (it will be shown") + "\n" + Language.translate("first when the file is executed)."),
                Language.translate("Start Sketch"),
                JOptionPane.PLAIN_MESSAGE,
                Workspace.createImageIcon("resources/start.gif"),
                sketches,
                sketches[0]);

        if (s != null) {
            for (i = 0; i < sketches.length; i++) {
                if (sketches[i].equalsIgnoreCase(s)) {
                    GlobalProperties.setAndSave("last-sketch-index", "" + i);
                    return;
                }
            }
        }
    }

    public void error(String message) {
        JOptionPane.showMessageDialog(this, message, Language.translate("Error"), JOptionPane.ERROR_MESSAGE);
        createButton.setEnabled(true);
        fileList.setEnabled(true);
    }

    public JPanel createFilesPanel() {
        JPanel panel = new JPanel();

        JButton selectAll = new JButton(Language.translate("Select All"));
        JButton deselectAll = new JButton(Language.translate("Deselect All"));
        JPanel panelButtons = new JPanel();
        panelButtons.setLayout(new GridLayout(0, 1));

        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder(Language.translate("Select files")));
        try {
            String files[] = new File(SketchletContextUtils.getCurrentProjectDir()).list();
            boolean selected[] = new boolean[files.length];

            for (int i = 0; i < files.length; i++) {
                selected[i] = !files[i].endsWith(".zip") && !files[i].equalsIgnoreCase(".original");

                if (new File(SketchletContextUtils.getCurrentProjectDir() + files[i]).isDirectory()) {
                    files[i] = "[" + files[i] + "]";
                }
            }

            this.fileList = new CheckboxList(files, selected);

            panel.add(new JScrollPane(fileList));
        } catch (Exception e) {
            e.printStackTrace();
        }

        selectAll.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                fileList.selectAll();
            }
        });

        deselectAll.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                fileList.deselectAll();
            }
        });

        panelButtons.add(selectAll);
        panelButtons.add(deselectAll);
        panel.add(panelButtons);

        return panel;
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
        this.setFolder();
    }

    public void setFolder() {
        String root = strRoot;

        String name = this.remoteSubdirectoryField.getText();
        if (!name.equals("") && !name.endsWith("/") && !name.endsWith("\\")) {
            name += "";
        }
        String suffix = zipBtn.isSelected() ? ".zip" : ".jar";
        this.zipFileField.setText(root + name + suffix);
    }

    public void keyTyped(KeyEvent e) {
    }

    public static boolean openDialog(Frame owner) {
        return true;
    }

    public void setProgress(int numberOfCopiedFiles, int totalNumberOfFiles, boolean bFinished, boolean bError) {
        this.progressBar.setMaximum(totalNumberOfFiles);
        this.progressBar.setValue(numberOfCopiedFiles);

        if (bFinished && bError) {
            JOptionPane.showMessageDialog(this, Language.translate("Error occured during saving.") + "\n" + Language.translate("Try to create archive again."), Language.translate("Save Archive"), JOptionPane.ERROR_MESSAGE);
            this.setVisible(false);
        } else if (bFinished) {
            if (jarBtn.isSelected()) {
                int n = JOptionPane.showConfirmDialog(this, Language.translate("Self-executable JAR file is saved.") + "\n" + Language.translate("Do you want to test it?"), Language.translate("Save Archive"), JOptionPane.YES_NO_OPTION);
                if (n == JOptionPane.YES_OPTION) {
                    try {
                        Process p = Runtime.getRuntime().exec(Workspace.replaceSystemVariables("%JAVA_HOME%/bin/java -jar \"" + zipFileField.getText() + "\" test"));
                        StreamGobbler es = new StreamGobbler(p.getErrorStream(), "");
                        StreamGobbler is = new StreamGobbler(p.getInputStream(), "");
                        es.start();
                        is.start();
                        int err = p.waitFor();
                        if (err != 0) {
                            JOptionPane.showMessageDialog(this, Language.translate("The self-executable JAR file seems to be corrupt.") + "\n" + Language.translate("Try to generate it again."), Language.translate("Test Result"), JOptionPane.ERROR_MESSAGE);
                            this.createButton.setEnabled(true);
                        } else {
                            JOptionPane.showMessageDialog(this, Language.translate("The self-executable JAR file is OK."), Language.translate("Test Result"), JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception e) {
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, Language.translate("Archive saved."), ("Save Archive"), JOptionPane.INFORMATION_MESSAGE);
            }
            this.setVisible(false);
        }
    }
}

class RemoteLocationInfo {

    public RemoteLocationInfo(String name, String uri, String username, String password) {
        this.setName(name);
        this.setUri(uri);
        this.setUsername(username);
        this.setPassword(password);
    }

    private String name = "";
    private String uri = "";
    private String username = "";
    private String password = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

class StreamGobbler extends Thread {

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
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
