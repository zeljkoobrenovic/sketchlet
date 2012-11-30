package net.sf.sketchlet.designer.editor.ui.desktop;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class SelectionTree extends JFrame implements TreeSelectionListener {
    private static final Logger log = Logger.getLogger(SelectionTree.class);

    public static void main(String[] args) {
        // new SelectionTree( "Test", true );
    }

    private JTree tree;
    private JTextArea currentSelectionField;
    DefaultMutableTreeNode examples;
    DefaultMutableTreeNode modules;
    DefaultMutableTreeNode basicModules;
    DefaultMutableTreeNode myProjects;
    DefaultMutableTreeNode tools;
    JButton importButton;
    JButton editButton;
    boolean append;

    public SelectionTree(String title, final String buttonTitle, String strIcon, boolean appendProcesses) {
        super(Language.translate(title));

        this.append = appendProcesses;
        // WindowUtilities.setNativeLookAndFeel();
        // addWindowListener(new ExitListener());
        Container content = getContentPane();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(Language.translate("I/O Services and Projects"));

        // basicModules = new DefaultMutableTreeNode("Basic AMICO Modules");
        // root.add(basicModules);

        modules = new DefaultMutableTreeNode(Language.translate("I/O Services"));
        root.add(modules);

        tools = new DefaultMutableTreeNode(Language.translate("Tools"));
        root.add(tools);

        examples = new DefaultMutableTreeNode(Language.translate("Examples"));
        root.add(examples);

        myProjects = new DefaultMutableTreeNode(Language.translate("My Projects"));
        root.add(myProjects);

        String amicoHome = SketchletContextUtils.getSketchletDesignerHome();

//        populate(basicModules, amicoHome + "bin/import-processes");
        selectNode = null;
        populate(examples, amicoHome + "examples");

        populate(modules, SketchletContextUtils.getSketchletDesignerModulesDir());
        populate(myProjects, SketchletContextUtils.getDefaultProjectsRootLocation());
        populate(tools, amicoHome + "conf/tools");


        tree = new JTree(root);

        //Set the icon for leaf nodes.
        ImageIcon leafIcon = Workspace.createImageIcon("resources/process_small.gif", "");
        if (leafIcon != null) {
            DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
            renderer.setLeafIcon(leafIcon);
            tree.setCellRenderer(renderer);
        } else {
            System.err.println("Leaf icon missing; using default.");
        }

        tree.addTreeSelectionListener(this);
        tree.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                if (selRow != -1) {
                    if (e.getClickCount() == 1) {
                    } else if (e.getClickCount() == 2) {
                        importFiles();
                    }
                }
            }
        });
        content.add(new JScrollPane(tree), BorderLayout.NORTH);

        currentSelectionField = new JTextArea("Current Selection: NONE");
        currentSelectionField.setLineWrap(true);
        currentSelectionField.setEditable(false);
        content.add(currentSelectionField, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        importButton = new JButton(Language.translate(buttonTitle));
        importButton.setIcon(Workspace.createImageIcon(strIcon, ""));

        final JFrame thisFrame = this;

        importButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                importFiles();
            }
        });

        JButton selectAnotherButton = new JButton(Language.translate("Select Other..."));
        selectAnotherButton.setIcon(Workspace.createImageIcon("resources/open.gif", ""));
        selectAnotherButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle(Language.translate("Select Project Folder"));
                fc.setCurrentDirectory(new File(SketchletContextUtils.getDefaultProjectsRootLocation()));
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = fc.showDialog(thisFrame, Language.translate("Open Project"));

                if (returnVal == fc.APPROVE_OPTION) {
                    String strPath = fc.getSelectedFile().getPath();
                    currentSelectionField.setText(strPath);

                    Workspace.openProject(strPath, append);

                    if (!append) {
                        setVisible(false);
                    }
                }
            }
        });

        editButton = new JButton(Language.translate("Edit..."));
        editButton.setIcon(Workspace.createImageIcon("resources/edit.gif", ""));

        editButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                Notepad.openNotepad(currentSelectionField.getText(), null);
            }
        });

        importButton.setEnabled(false);
        editButton.setEnabled(false);

        panel.add(importButton);
        panel.add(editButton);
        panel.add(new JLabel("    "));
        panel.add(selectAnotherButton);

        content.add(panel, BorderLayout.SOUTH);

        if (!append) {
            tree.expandRow(4);
            tree.setSelectionRow(4);
            if (selectNode != null) {
                TreePath treePath = new TreePath(selectNode.getPath());
                tree.setSelectionPath(treePath);
                tree.scrollPathToVisible(treePath);
            }
        } else {
            tree.expandRow(1);
            tree.setSelectionRow(1);
        }

        setSize(400, 500);
        if (SketchletEditor.editorFrame != null) {
            setLocationRelativeTo(SketchletEditor.editorFrame);
        } else {
            setLocationRelativeTo(Workspace.getMainFrame());
        }
        setVisible(true);
    }

    public void importFiles() {
        String strPath = currentSelectionField.getText();

        try {
            if (append) {
                String strDir = new File(strPath).getParent() + File.separator + "conf";
                if (!new File(strDir).exists()) {
                    strDir = new File(strPath).getParent() + File.separator + SketchletContextUtils.sketchletDataDir() + "/conf";
                }
                FileUtils.restore(SketchletContextUtils.getCurrentProjectConfDir(), strDir, null);
            }

            Workspace.getProcessRunner().getIoServicesHandler().loadProcesses(new File(strPath).toURL(), append);

            setVisible(false);

            int index = Workspace.getMainPanel().getTableModules().getRowCount() - 1;
            if (index >= 0) {
                Workspace.getMainPanel().getTableModules().getSelectionModel().setSelectionInterval(index, index);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    DefaultMutableTreeNode selectNode = null;

    public boolean populate(DefaultMutableTreeNode node, String directory) {
        if (!directory.endsWith("/") && !directory.endsWith("\\")) {
            directory += File.separator;
        }

        boolean bAddNode = false;

        File dir = new File(directory);
        String[] children = dir.list();

        if (children == null) {
            log.debug("Could not find anything in " + directory);
        } else {
            try {
                for (int i = 0; i < children.length; i++) {
                    String filename = children[i];

                    File file = new File(directory + filename);
                    if (file.isDirectory()) {
                        if (new File(file.getAbsolutePath() + "/" + SketchletContextUtils.sketchletDataDir() + "/workspace.txt").exists()) {
                            bAddNode = true;
                            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(filename);
                            newNode.setUserObject(new UserFileObject(directory, filename, SketchletContextUtils.sketchletDataDir() + "/workspace.txt"));

                            node.add(newNode);

                            File file1 = new File(file.getAbsolutePath() + "/" + SketchletContextUtils.sketchletDataDir() + "/workspace.txt");
                            File file2 = new File(SketchletContextUtils.getCurrentProjectFile());

                            if (file1.getPath().equalsIgnoreCase(file2.getPath())) {
                                selectNode = newNode;
                            }
                        } else {
                            if (filename.equals("history") || filename.equals("index") || filename.equals("notepad")) {
                                continue;
                            } // we are not going to load history of projects

                            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(filename);
                            newNode.setUserObject(new UserFileObject(directory, filename));

                            boolean bTemp = populate(newNode, directory + filename);
                            bAddNode = bAddNode || bTemp;

                            if (bTemp) {
                                node.add(newNode);
                            }
                        }
                    } else if (filename.toLowerCase().equals("process-runner.xml") || filename.toLowerCase().equals("process-starter.xml") || filename.toLowerCase().equals("run.xml") || filename.toLowerCase().equals("process-starter.txt") || filename.toLowerCase().equals("process_starter.txt") || filename.toLowerCase().equals("process-runner.txt") || filename.toLowerCase().equals("run.txt") || filename.toLowerCase().equals("workspace.txt")) {
                        if (node.getUserObject() != null && node.getUserObject() instanceof UserFileObject) {
                            ((UserFileObject) node.getUserObject()).configFile = filename;
                        }
                        bAddNode = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return bAddNode;
    }

    public void valueChanged(TreeSelectionEvent event) {
        if (currentSelectionField != null && tree != null && tree.getLastSelectedPathComponent() != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node.getUserObject() != null) {
                if (node.getUserObject() instanceof UserFileObject) {
                    String strPath = ((UserFileObject) node.getUserObject()).getPath();
                    currentSelectionField.setText(strPath);

                    File file = new File(strPath);

                    if (file.exists() && !file.isDirectory()) {
                        importButton.setEnabled(true);
                        editButton.setEnabled(true);
                    } else {
                        importButton.setEnabled(false);
                        editButton.setEnabled(false);
                    }

                } else {
                    currentSelectionField.setText(node.getUserObject().toString());
                    importButton.setEnabled(false);
                    editButton.setEnabled(false);
                }
            }
        }
    }

    class UserFileObject {

        String directory;
        String file;
        String configFile = "";

        public UserFileObject(String directory, String file) {
            this.directory = directory;
            this.file = file;
        }

        public UserFileObject(String directory, String file, String configFile) {
            this.directory = directory;
            this.file = file;
            this.configFile = configFile;
        }

        public String getPath() {
            return this.directory + this.file + File.separator + this.configFile;
        }

        public String toString() {
            return file;
        }
    }
}
