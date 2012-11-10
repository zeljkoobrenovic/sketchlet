/*
 * RemoteLocationFrame.java
 *
 * Created on April 10, 2008, 12:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.tools.vfs;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.ui.CheckboxList;
import net.sf.sketchlet.designer.ui.desktop.Notepad;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Vector;

/* RemoteLocationFrame.java requires no other files. */
public class RemoteLocationFrame extends JFrame implements KeyListener, RemoteBackupProgressFeedback {

    JLabel label1 = new JLabel(Language.translate("Remote location:  "));
    JLabel label2 = new JLabel(Language.translate("Subdirectory:        "));
    JLabel label3 = new JLabel(Language.translate("Remote folder:      "));
    JLabel label4 = new JLabel(Language.translate("Exclude extensions:      "));
    JComboBox remoteDirectoryCombobox = new JComboBox();
    JTextField remoteSubdirectoryField = new JTextField(30);
    JTextField remoteFolderField = new JTextField(30);
    JTextField excludeField = new JTextField(27);
    JProgressBar progressBar = new JProgressBar();
    CheckboxList fileList;
    JButton createButton;
    RemoteBackupThread thread;

    public RemoteLocationFrame() {
        super(Language.translate("Remote Location"));
        this.setIconImage(Workspace.createImageIcon("resources/document-save-as.png").getImage());

        this.init();
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    public static void createAndShowGUI() {
        //Create and set up the window.
        RemoteLocationFrame frame = new RemoteLocationFrame();

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                createAndShowGUI();
            }
        });
    }

    JTextArea statusTextArea = new JTextArea();

    private void init() {

        this.setTitle(Language.translate("Remote Backup of Project Files"));
        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));

        JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel1.add(label1);

        remoteDirectoryCombobox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                int sel = remoteDirectoryCombobox.getSelectedIndex();

                if (sel >= 0 && sel < locations.size()) {
                    RemoteBackup.userInfo.username = locations.elementAt(sel).username;
                }

                setFolder();
            }
        });

        panel1.add(remoteDirectoryCombobox);
        remoteDirectoryCombobox.addKeyListener(this);
        JButton selectDirectoryButton = new JButton(Language.translate("Edit..."));
        panel1.add(selectDirectoryButton);

        String remoteDirectory = SketchletContextUtils.getSketchletDesignerHome();

        JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel2.add(label2);
        remoteSubdirectoryField.setText(SketchletContextUtils.getCurrentProjectDir() == null ? "" : new File(SketchletContextUtils.getCurrentProjectDir()).getName());
        panel2.add(remoteSubdirectoryField);
        remoteSubdirectoryField.addKeyListener(this);

        JPanel panel3 = new JPanel();
        panel3.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel3.add(label3);
        panel3.add(remoteFolderField);
        remoteFolderField.setEditable(false);

        final RemoteLocationFrame parent = this;

        JPanel panel4 = new JPanel();
        panel4.setLayout(new FlowLayout(FlowLayout.LEFT));

        createButton = new JButton(Language.translate("Copy Files"));
        createButton.setAlignmentX(JButton.CENTER_ALIGNMENT);

        createButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                FileUtils.saveFileText(SketchletContextUtils.getCurrentProjectConfDir() + "backup_exclude.txt", excludeField.getText().trim());
                statusTextArea.setText(Language.translate("  Counting files..."));
                Object values[] = fileList.getSelectedValues();
                String selectedFiles[] = new String[values.length];
                for (int i = 0; i < values.length; i++) {
                    selectedFiles[i] = (String) values[i];
                }
                thread = new RemoteBackupThread(remoteFolderField.getText(), SketchletContextUtils.getCurrentProjectDir(), selectedFiles, parent, parent, excludeField.getText());
                createButton.setEnabled(false);
                fileList.setEnabled(false);
            }
        });
        panel4.add(createButton);

        JButton cancelButton = new JButton(Language.translate("Cancel"));
        cancelButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                if (thread != null) {
                    thread.remoteBackup.stopped = true;
                }
                setVisible(false);
            }
        });
        panel4.add(cancelButton);

        selectDirectoryButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                try {

                    String strRemoteLocations = SketchletContextUtils.getSketchletDesignerConfDir() + "remote_locations.txt";
                    File file = new File(strRemoteLocations);
                    if (!file.exists()) {
                        file.createNewFile();
                    }

                    Notepad.openNotepad(strRemoteLocations, new WindowAdapter() {

                        public void windowClosing(WindowEvent e) {
                            loadRemoteLocations();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        this.add(panel1);
        this.add(panel2);
        this.add(panel3);
        JPanel paneEx = new JPanel();
        paneEx.add(label4);
        excludeField.setText(FileUtils.getFileText(SketchletContextUtils.getCurrentProjectConfDir() + "backup_exclude.txt").trim());
        paneEx.add(this.excludeField);
        this.add(paneEx);
        this.add(createFilesPanel());
        this.statusTextArea.setEditable(false);
        this.statusTextArea.setBackground(this.getBackground());
        this.add(statusTextArea);


        this.progressBar.setStringPainted(true);
        this.progressBar.setPreferredSize(new Dimension(270, 25));
        panel4.add(this.progressBar);
        this.add(panel4);


        loadRemoteLocations();

        setFolder();

        this.getRootPane().setDefaultButton(createButton);
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
            final String files[] = new File(SketchletContextUtils.getCurrentProjectDir()).list();
            final boolean selected[] = new boolean[files.length];

            for (int i = 0; i < files.length; i++) {
                selected[i] = !files[i].equalsIgnoreCase("history");

                if (new File(SketchletContextUtils.getCurrentProjectDir() + files[i]).isDirectory()) {
                    files[i] = "[" + files[i] + "]";
                }
            }

            this.fileList = new CheckboxList(files, selected);

            panel.add(new JScrollPane(fileList));

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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return panel;
    }

    Vector<RemoteLocationInfo> locations = new Vector<RemoteLocationInfo>();

    public void loadRemoteLocations() {
        locations.removeAllElements();

        try {
            String strRemoteLocations = SketchletContextUtils.getSketchletDesignerConfDir() + "remote_locations.txt";
            File file = new File(strRemoteLocations);
            if (file.exists()) {
                BufferedReader in = new java.io.BufferedReader(new FileReader(file));
                String line;

                String locName = "";
                String location = "";
                String username = "";

                while ((line = in.readLine()) != null) {
                    line = line.trim();

                    if (line.startsWith("#")) {
                        continue;
                    }

                    if (line.equals("")) {

                        if (!locName.trim().equals("") && !location.trim().equals("")) {
                            locations.add(new RemoteLocationInfo(locName, location, username, ""));
                        }

                        locName = "";
                        location = "";
                        username = "";

                        continue;
                    }

                    int n = line.indexOf(" ");

                    String l1;
                    String l2;

                    if (n > 0) {
                        l1 = line.substring(0, n);
                        l2 = line.substring(n + 1);
                    } else {
                        continue;
                    }

                    if (l1.equalsIgnoreCase("Location")) {
                        locName = l2;
                        location = "";
                        username = "";
                    } else if (l1.equalsIgnoreCase("URI")) {
                        location = l2;
                    } else if (l1.equalsIgnoreCase("UserName")) {
                        username = l2;
                    }
                }

                if (!locName.trim().equals("") && !location.trim().equals("")) {
                    locations.add(new RemoteLocationInfo(locName, location, username, ""));
                }

                this.remoteDirectoryCombobox.removeAllItems();

                for (RemoteLocationInfo info : locations) {
                    this.remoteDirectoryCombobox.addItem(info.name);
                }

            } else {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
        this.setFolder();
    }

    public void setFolder() {
        int sel = this.remoteDirectoryCombobox.getSelectedIndex();

        if (sel < 0 || sel > locations.size() - 1) {
            return;
        }

        String root = Workspace.replaceSystemVariables(locations.elementAt(sel).uri);

        if (root == null) {
            root = "";
        }

        if (!root.endsWith("/") && !root.endsWith("\\")) {
            root += "/";
        }

        String name = this.remoteSubdirectoryField.getText();
        if (!name.equals("") && !name.endsWith("/") && !name.endsWith("\\")) {
            name += "";
        }

        this.remoteFolderField.setText(root + name);
    }

    public void keyTyped(KeyEvent e) {
    }

    public static boolean openDialog(Frame owner) {
        return true;
    }

    public void setProgress(int numberOfCopiedFiles, int totalNumberOfFiles, String currentFile, boolean finished) {
        this.progressBar.setMaximum(totalNumberOfFiles);
        this.progressBar.setValue(numberOfCopiedFiles);

        this.statusTextArea.setText("  " + currentFile);

        if (finished) {
            JOptionPane.showMessageDialog(this, "Copying finished.", "Copying", JOptionPane.INFORMATION_MESSAGE);
            this.setVisible(false);
        }
    }
}

class RemoteLocationInfo {

    public RemoteLocationInfo(String name, String uri, String username, String password) {
        this.name = name;
        this.uri = uri;
        this.username = username;
        this.password = password;
    }

    String name = "";
    String uri = "";
    String username = "";
    String password = "";
}


