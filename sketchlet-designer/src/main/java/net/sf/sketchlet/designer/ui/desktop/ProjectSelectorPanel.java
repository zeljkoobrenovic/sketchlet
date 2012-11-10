/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui.desktop;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.ui.MessageFrame;
import net.sf.sketchlet.designer.ui.SketchletDesignerMainPanel;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

/**
 * Example tree built out of DefaultMutableTreeNodes. 1999 Marty Hall,
 * http://www.apl.jhu.edu/~hall/java/
 */

/**
 * JTree that reports selections by placing their string values in a JTextField.
 * 1999 Marty Hall, http://www.apl.jhu.edu/~hall/java/
 */
public class ProjectSelectorPanel extends JPanel implements TreeSelectionListener {
    private static final Logger log = Logger.getLogger(ProjectSelectorPanel.class);

    public static void main(String[] args) {
        // new ProjectSelectorPanel( "Test", true );
    }

    private JTree tree;
    private JTextArea currentSelectionField;
    DefaultMutableTreeNode myProjects;
    boolean append;

    public ProjectSelectorPanel() {
        setLayout(new BorderLayout());
        this.append = false;
        myProjects = new DefaultMutableTreeNode("My Projects");

        selectNode = null;
        // populate(myProjects, WorkspaceUtils.getDefaultProjectsRootLocation());

        tree = new JTree(myProjects);
        tree.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(tree);

        //Set the icon for leaf nodes.
        ImageIcon leafIcon = Workspace.createImageIcon("resources/desktop-icon-16.png", "");
        if (leafIcon != null) {
            CustomIconRenderer renderer = new CustomIconRenderer();
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
                    if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
                        if (node.getUserObject() != null && node.getUserObject() instanceof UserFileObject) {
                            importFiles();
                        }
                    } else {
                        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
                        if (node.getUserObject() != null) {
                            if (node.getUserObject() instanceof UserFileObject) {
                                final UserFileObject fo = (UserFileObject) node.getUserObject();
                                final JPopupMenu popupMenu = new JPopupMenu();
                                TutorialPanel.prepare(popupMenu, true);

                                JMenuItem btnDelete = new JMenuItem("Delete Project '" + fo.toString() + "'");
                                btnDelete.addActionListener(new ActionListener() {

                                    public void actionPerformed(ActionEvent ae) {
                                        popupMenu.setVisible(false);
                                        Object[] options = {"Delete", "Cancel"};
                                        int n = JOptionPane.showOptionDialog(Workspace.mainFrame,
                                                "You are about to delete the project '" + fo.toString() + "'. \nThis operation is not reversable.",
                                                "Delete Project Confirmation",
                                                JOptionPane.YES_NO_CANCEL_OPTION,
                                                JOptionPane.WARNING_MESSAGE,
                                                null,
                                                options,
                                                options[0]);

                                        if (n != 0) {
                                            return;
                                        }
                                        closeEverything();
                                        MessageFrame.showMessage(Workspace.mainFrame, "Deleting project...", Workspace.mainFrame);
                                        System.setProperty("user.dir", "/");
                                        SketchletContextUtils.projectFolder = null;
                                        java.awt.EventQueue.invokeLater(new Runnable() {

                                            public void run() {
                                                try {
                                                    for (int i = 0; i < 3; i++) {
                                                        if (FileUtils.deleteDir(new File(fo.getPath()).getParentFile().getParentFile())) {
                                                            break;
                                                        }
                                                        try {
                                                            Thread.sleep(300);
                                                        } catch (InterruptedException e) {
                                                        }
                                                    }
                                                    populate();
                                                    SketchletDesignerMainPanel.desktopPanelAuto.refresh();
                                                    SketchletDesignerMainPanel.desktopPanel.refresh();
                                                } finally {
                                                    MessageFrame.closeMessage();
                                                }
                                            }
                                        });
                                    }
                                });

                                popupMenu.add(btnDelete);
                                JMenuItem mi = new JMenuItem("Rename Project...");
                                mi.addActionListener(new ActionListener() {

                                    public void actionPerformed(ActionEvent ae) {
                                        closeEverything();
                                        File orgDir = new File(fo.getPath()).getParentFile().getParentFile();
                                        String dir = JOptionPane.showInputDialog("New Project Name: ", orgDir.getName());
                                        if (dir != null) {
                                            // if (SketchletContextUtils.getCurrentProjectFile() != null && new File(fo.getPath()).getPath().equalsIgnoreCase(new File(SketchletContextUtils.getCurrentProjectFile()).getPath())) {
                                            // }
                                            File newDir = new File(orgDir.getParentFile(), dir);
                                            try {
                                                System.setProperty("user.dir", "/");
                                                //Files.move(orgDir.toPath(), newDir.toPath());
                                                orgDir.renameTo(newDir);
                                                System.setProperty("user.dir", newDir.getPath());
                                            } catch (Exception e) {
                                                log.error(orgDir.getPath() + ", " + newDir.getPath(), e);
                                            }
                                            populate();
                                        }
                                    }
                                });
                                popupMenu.add(mi);
                                if (folders.size() > 0) {
                                    popupMenu.addSeparator();
                                    JMenu menuFolders = new JMenu("Move to Folder");

                                    populateFoldersMenu(folders.toArray(new File[folders.size()]), menuFolders, fo.getPath());
                                    popupMenu.add(menuFolders);
                                }
                                popupMenu.show(tree, e.getX(), e.getY());
                            } else {
                                final JPopupMenu popupMenu = new JPopupMenu();
                                TutorialPanel.prepare(popupMenu, true);

                                JMenuItem btnDir = new JMenuItem("Create New Folder");
                                btnDir.addActionListener(new ActionListener() {

                                    public void actionPerformed(ActionEvent ae) {
                                        String dir = JOptionPane.showInputDialog("Folder Name: ");
                                        if (dir != null) {
                                            File file;
                                            boolean bRoot = !(node.getUserObject() != null && node.getUserObject() instanceof UserFolderObject);
                                            if (!bRoot) {
                                                file = new File(((UserFolderObject) node.getUserObject()).getPath(), dir);
                                            } else {
                                                file = new File(SketchletContextUtils.getDefaultProjectsRootLocation() + dir);
                                            }
                                            if (file.exists()) {
                                                JOptionPane.showMessageDialog(SketchletContext.getInstance().getMainFrame(), "Directory/file '" + dir + "' already exists.");
                                            } else {
                                                file.mkdirs();
                                            }
                                            populate();
                                        }
                                    }
                                });

                                popupMenu.add(btnDir);
                                JMenuItem mi = new JMenuItem("Rename Folder...");
                                mi.addActionListener(new ActionListener() {

                                    public void actionPerformed(ActionEvent ae) {
                                        closeEverything();
                                        if (node.getUserObject() instanceof UserFolderObject) {
                                            final UserFolderObject fo = (UserFolderObject) node.getUserObject();
                                            File orgDir = new File(fo.getPath());
                                            String dir = JOptionPane.showInputDialog("New Folder Name: ", orgDir.getName());
                                            if (dir != null) {
                                                File newDir = new File(orgDir.getParentFile(), dir);
                                                try {
                                                    System.setProperty("user.dir", "/");
                                                    // Files.move(orgDir.toPath(), newDir.toPath());
                                                    orgDir.renameTo(newDir);
                                                    System.setProperty("user.dir", newDir.getPath());
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                populate();
                                            }
                                        }
                                    }
                                });
                                popupMenu.add(mi);
                                popupMenu.addSeparator();
                                JMenuItem btnDelete = new JMenuItem("Delete Folder");
                                btnDelete.addActionListener(new ActionListener() {

                                    public void actionPerformed(ActionEvent ae) {
                                        if (node.getUserObject() instanceof UserFolderObject) {
                                            final UserFolderObject fo = (UserFolderObject) node.getUserObject();
                                            popupMenu.setVisible(false);
                                            Object[] options = {"Delete", "Cancel"};
                                            int n = JOptionPane.showOptionDialog(Workspace.mainFrame,
                                                    "You are about to delete the folder '" + fo.toString() + "' and its subfolders. \nThis operation is not reversable.",
                                                    "Delete Folder Confirmation",
                                                    JOptionPane.YES_NO_CANCEL_OPTION,
                                                    JOptionPane.WARNING_MESSAGE,
                                                    null,
                                                    options,
                                                    options[0]);

                                            if (n != 0) {
                                                return;
                                            }
                                            closeEverything();
                                            MessageFrame.showMessage(Workspace.mainFrame, "Deleting folder...", Workspace.mainFrame);
                                            System.setProperty("user.dir", "/");
                                            SketchletContextUtils.projectFolder = null;
                                            java.awt.EventQueue.invokeLater(new Runnable() {

                                                public void run() {
                                                    for (int i = 0; i < 3; i++) {
                                                        if (FileUtils.deleteDir(new File(fo.getPath()))) {
                                                            break;
                                                        }
                                                        try {
                                                            Thread.sleep(300);
                                                        } catch (InterruptedException e) {
                                                        }
                                                    }
                                                    populate();
                                                    SketchletDesignerMainPanel.desktopPanelAuto.refresh();
                                                    SketchletDesignerMainPanel.desktopPanel.refresh();
                                                    MessageFrame.closeMessage();
                                                }
                                            });
                                        }
                                    }
                                });

                                popupMenu.add(btnDelete);
                                popupMenu.show(tree, e.getX(), e.getY());
                            }
                        } else {
                        }
                    }
                }
            }
        });
        add(new JScrollPane(tree), BorderLayout.CENTER);

        currentSelectionField = new JTextArea("Current Selection: NONE");
        currentSelectionField.setLineWrap(true);
        currentSelectionField.setEditable(false);
    }

    boolean bLoading = false;

    private void populateFoldersMenu(File folders[], JMenu parentMenu, final String dir) {
        for (final File file : folders) {
            boolean isLeaf = true;
            File children[] = file.listFiles();
            java.util.List<File> subDirs = new ArrayList<File>();
            for (File child : children) {
                if (child.isDirectory()) {
                    if (child.getName().startsWith(".") || new File(child, SketchletContextUtils.sketchletDataDir()).exists()) {
                        continue;
                    } else {
                        isLeaf = false;
                        subDirs.add(child);
                    }
                }
            }

            JMenuItem mi = new JMenuItem(file.getName());
            mi.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    closeEverything();
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                    }
                    File orgDir = new File(dir).getParentFile().getParentFile();
                    File newDir = new File(file.getPath() + "/" + orgDir.getName());

                    moveProject(orgDir, newDir);
                }
            });
            if (isLeaf) {
                parentMenu.add(mi);
            } else {
                JMenu menu = new JMenu(file.getName());
                mi.setText("/");
                menu.add(mi);
                populateFoldersMenu(subDirs.toArray(new File[subDirs.size()]), menu, dir);
                parentMenu.add(menu);
            }
        }
    }

    public void moveProject(final File orgDir, final File newDir) {
        MessageFrame.showMessage(Workspace.mainFrame, "Moving project...", Workspace.mainFrame);
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                try {
                    if (!newDir.exists()) {
                        FileUtils.restore(orgDir.getPath(), newDir.getPath());
                        for (int i = 0; i < 3; i++) {
                            if (FileUtils.deleteDir(orgDir)) {
                                break;
                            }
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                            }
                        }
                        populate();
                        SketchletDesignerMainPanel.desktopPanelAuto.refresh();
                        SketchletDesignerMainPanel.desktopPanel.refresh();
                    } else {
                        JOptionPane.showMessageDialog(SketchletContext.getInstance().getMainFrame(), "Directory already exists.\nProject has not been moved.");
                    }
                } finally {
                    MessageFrame.closeMessage();
                }
            }
        });
        populate();
    }

    public void closeEverything() {
        SketchletEditor.close(false);
        SketchletEditor.pages = null;
        SketchletContextUtils.projectFolder = null;
        Workspace.mainPanel.enableMenuItems();
        Workspace.mainPanel.enableToolbarItems();
    }

    public void importFiles() {
        if (bLoading) {
            return;
        }
        final String strPath = currentSelectionField.getText();

        if (strPath == null || strPath.isEmpty() || !(new File(strPath).exists())) {
            return;
        }
        if (!SketchletEditor.close()) {
            return;
        }
        bLoading = true;
        MessageFrame.showMessage(Workspace.mainFrame, "Loading project...", Workspace.mainFrame);
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                long start = System.currentTimeMillis();
                try {
                    if (append) {
                        String strDir = new File(strPath).getParent() + File.separator + "conf";
                        if (!new File(strDir).exists()) {
                            strDir = new File(strPath).getParent() + File.separator + SketchletContextUtils.sketchletDataDir() + "/conf";
                        }
                        FileUtils.restore(SketchletContextUtils.getCurrentProjectConfDir(), strDir, null);
                    }

                    Workspace.processRunner.loadProcesses(new File(strPath).toURL(), append);
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    MessageFrame.closeMessage();
                }
                Workspace.mainPanel.refreshData(false);
                bLoading = false;
            }
        });
    }

    DefaultMutableTreeNode selectNode = null;

    public void populate() {
        folders.clear();
        populate(myProjects, SketchletContextUtils.getDefaultProjectsRootLocation());
    }

    java.util.List<File> folders = new ArrayList<File>();

    public boolean populate(DefaultMutableTreeNode node, String directory) {
        if (!directory.endsWith("/") && !directory.endsWith("\\")) {
            directory += File.separator;
        }

        node.removeAllChildren();

        boolean bAddNode = false;

        File dir = new File(directory);
        String[] children = dir.list();

        if (children == null) {
            log.debug("Could not find anything in " + directory);
        } else {
            try {
                for (String filename : children) {
                    File file = new File(directory + filename);
                    if (file.isDirectory()) {
                        File fExec = new File(file.getAbsolutePath() + "/" + SketchletContextUtils.sketchletDataDir() + "/workspace.txt");
                        if (!fExec.exists()) {
                            if (file.getName().startsWith(".")) {
                                continue;
                            }
                            if (myProjects == node) {
                                folders.add(file);
                            }
                            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(filename);
                            newNode.setUserObject(new UserFolderObject(file.getPath()));

                            boolean bTemp = populate(newNode, directory + filename);
                            bAddNode = bAddNode || bTemp;

                            //if (bTemp) {
                            node.add(newNode);
                            //}
                        }
                    }
                }
                for (String filename : children) {
                    File file = new File(directory + filename);
                    if (file.getName().startsWith(".")) {
                        continue;
                    }
                    if (file.isDirectory()) {
                        File fExec = new File(file.getAbsolutePath() + "/" + SketchletContextUtils.sketchletDataDir() + "/workspace.txt");
                        if (fExec.exists()) {
                            bAddNode = true;
                            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(filename);
                            newNode.setUserObject(new UserFileObject(directory, filename, SketchletContextUtils.sketchletDataDir() + "/workspace.txt"));

                            node.add(newNode);

                            if (SketchletContextUtils.getCurrentProjectFile() != null) {
                                File file1 = new File(file.getAbsolutePath() + "/" + SketchletContextUtils.sketchletDataDir() + "/workspace.txt");
                                File file2 = new File(SketchletContextUtils.getCurrentProjectFile());

                                if (file1.getPath().equalsIgnoreCase(file2.getPath())) {
                                    selectNode = newNode;
                                }
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

        DefaultTreeModel treeModel = new DefaultTreeModel(node);
        tree.setModel(treeModel);
        treeModel.reload(node);

        if (selectNode != null) {
            TreePath treePath = new TreePath(selectNode.getPath());
            tree.setSelectionPath(treePath);
            tree.scrollPathToVisible(treePath);
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

    class UserFolderObject {

        String directory;

        public UserFolderObject(String directory) {
            this.directory = directory;
        }

        public String getPath() {
            return this.directory + File.separator;
        }

        @Override
        public String toString() {
            return new File(directory).getName();
        }
    }

    class CustomIconRenderer extends DefaultTreeCellRenderer {

        public CustomIconRenderer() {
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            if (node.getUserObject() != null && node.getUserObject() instanceof UserFileObject) {
                this.setIcon(this.getLeafIcon());
            } else {
                if (expanded) {
                    this.setIcon(this.getOpenIcon());
                } else {
                    this.setIcon(this.getClosedIcon());
                }
            }
            return this;
        }
    }
}
