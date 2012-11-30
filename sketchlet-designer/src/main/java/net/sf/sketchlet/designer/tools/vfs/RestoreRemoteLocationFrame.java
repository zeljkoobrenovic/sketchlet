package net.sf.sketchlet.designer.tools.vfs;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.ui.desktop.Notepad;

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
public class RestoreRemoteLocationFrame extends JFrame implements KeyListener, RemoteBackupProgressFeedback {

    private JLabel label1 = new JLabel(Language.translate("Remote location:  "));
    private JLabel label2 = new JLabel(Language.translate("Subdirectory:        "));
    private JLabel label3 = new JLabel(Language.translate("Remote folder:      "));
    private JComboBox remoteDirectoryCombobox = new JComboBox();
    private JTextField remoteSubdirectoryField = new JTextField(30);
    private JTextField remoteFolderField = new JTextField(30);
    private JProgressBar progressBar = new JProgressBar();
    private JButton createButton;
    private RestoreBackupThread thread;

    public RestoreRemoteLocationFrame() {
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
        RestoreRemoteLocationFrame frame = new RestoreRemoteLocationFrame();

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

        this.setTitle(Language.translate("Restore of Remote Backup of Project Files"));
        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));

        JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel1.add(label1);

        remoteDirectoryCombobox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                int sel = remoteDirectoryCombobox.getSelectedIndex();

                if (sel >= 0 && sel < locations.size()) {
                    RemoteBackup.userInfo.setUsername(locations.elementAt(sel).getUsername());
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

        final RestoreRemoteLocationFrame parent = this;

        JPanel panel4 = new JPanel();
        panel4.setLayout(new FlowLayout(FlowLayout.LEFT));

        createButton = new JButton(Language.translate("Restore Files"));
        createButton.setAlignmentX(JButton.CENTER_ALIGNMENT);

        createButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                statusTextArea.setText(Language.translate("  Counting files..."));
                thread = new RestoreBackupThread(remoteFolderField.getText(), SketchletContextUtils.getCurrentProjectDir(), parent, parent);
                createButton.setEnabled(false);
            }
        });
        panel4.add(createButton);

        JButton cancelButton = new JButton(Language.translate("Cancel"));
        cancelButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                if (thread != null) {
                    thread.remoteBackup.setStopped(true);
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

        // this.add(createFilesPanel());
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
                        location = Workspace.replaceSystemVariables(l2);
                    } else if (l1.equalsIgnoreCase("UserName")) {
                        username = l2;
                    }
                }

                if (!locName.trim().equals("") && !location.trim().equals("")) {
                    locations.add(new RemoteLocationInfo(locName, location, username, ""));
                }

                this.remoteDirectoryCombobox.removeAllItems();

                for (RemoteLocationInfo info : locations) {
                    this.remoteDirectoryCombobox.addItem(info.getName());
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

        String root = locations.elementAt(sel).getUri();

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
        this.statusTextArea.setText("  " + currentFile);
        this.progressBar.setMaximum(totalNumberOfFiles);
        this.progressBar.setValue(numberOfCopiedFiles);

        if (finished) {
            JOptionPane.showMessageDialog(this, Language.translate("Copying finished."), Language.translate("Copying"), JOptionPane.INFORMATION_MESSAGE);
            this.setVisible(false);
        }
    }
}


