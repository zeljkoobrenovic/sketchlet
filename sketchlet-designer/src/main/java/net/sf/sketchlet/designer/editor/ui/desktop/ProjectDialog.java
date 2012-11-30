package net.sf.sketchlet.designer.editor.ui.desktop;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.Vector;

public class ProjectDialog extends JDialog implements KeyListener {
    private static final Logger log = Logger.getLogger(ProjectDialog.class);

    JLabel label0 = new JLabel(Language.translate("Template:           "));
    JLabel label1 = new JLabel(Language.translate("Project location: "));
    JLabel label2 = new JLabel(Language.translate("Project name:     "));
    JLabel label3 = new JLabel(Language.translate("Project folder:     "));
    JComboBox projectTemplate = new JComboBox();
    JTextField projectDirectoryField = new JTextField(30);
    JTextField projectNameField = new JTextField(30);
    JTextField projectFolderField = new JTextField(30);
    public boolean projectCreated = false;
    public static String projectFolder;
    public static String projectTitle;
    Vector<String> templateFolders = new Vector<String>();
    public static String templateDir = null;

    public ProjectDialog(Frame owner, boolean modal, String title) {
        super(owner, modal);
        init(title);
    }

    public ProjectDialog(Frame owner, boolean modal, String title, String strName) {
        super(owner, modal);
        init(title);
        if (strName != null) {
            this.projectNameField.setText(strName);
            this.setFolder();
        }
    }

    private void init(String title) {
        this.setTitle(title);
        this.setLayout(new GridLayout(0, 1));

        fillTemplates();

        JPanel panel0 = new JPanel();
        panel0.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel0.add(label0);
        panel0.add(projectTemplate);

        JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel1.add(label1);
        panel1.add(projectDirectoryField);
        projectDirectoryField.addKeyListener(this);
        JButton selectDirectoryButton = new JButton(Language.translate("Browse..."));
        panel1.add(selectDirectoryButton);

        this.projectDirectoryField.setText(SketchletContextUtils.getDefaultProjectsRootLocation());

        JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel2.add(label2);
        panel2.add(projectNameField);
        //projectNameField.setText(SketchletContextUtils.getCurrentProjectDirName());
        projectNameField.addKeyListener(this);

        JPanel panel3 = new JPanel();
        panel3.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel3.add(label3);
        panel3.add(projectFolderField);
        projectFolderField.setEditable(false);

        JPanel panel4 = new JPanel();
        panel4.setLayout(new FlowLayout(FlowLayout.LEFT));

        JButton createButton = new JButton(Language.translate("Create Project"));
        createButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        createButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                if (createProject()) {
                    projectCreated = true;
                    if (projectTemplate.getSelectedIndex() > 0) {
                        templateDir = templateFolders.elementAt(projectTemplate.getSelectedIndex());
                    } else {
                        templateDir = null;
                    }

                    setVisible(false);
                } else {
                    templateDir = null;
                    projectCreated = false;
                }
            }
        });
        panel4.add(createButton);

        JButton cancelButton = new JButton(Language.translate("Cancel"));
        cancelButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                templateDir = null;
                projectCreated = false;
                setVisible(false);
            }
        });
        panel4.add(cancelButton);

        final JDialog dialog = this;

        selectDirectoryButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.setCurrentDirectory(new File(projectDirectoryField.getText()));
                int returnVal = fc.showOpenDialog(dialog);

                if (returnVal == fc.APPROVE_OPTION) {
                    projectDirectoryField.setText(fc.getSelectedFile().getPath());
                    setFolder();
                }
            }
        });

        this.add(panel2);
        this.add(panel0);
        this.add(new JLabel(" "));
        this.add(panel1);
        this.add(panel3);
        this.add(new JLabel("  "));
        this.add(panel4);

        setFolder();

        this.getRootPane().setDefaultButton(createButton);
    }

    public void fillTemplates() {
        projectTemplate.removeAllItems();
        projectTemplate.addItem(Language.translate("Empty project"));

        templateFolders.removeAllElements();
        templateFolders.add("");

        populate(projectTemplate, SketchletContextUtils.getSketchletDesignerProjectTemplatesDir(), "");
    }

    public boolean populate(JComboBox node, String directory, String strPrefix) {
        if (!directory.endsWith("/") && !directory.endsWith("\\")) {
            directory += File.separator;
        }

        boolean bAddNode = false;

        File dir = new File(directory);
        String[] children = dir.list();

        if (children == null) {
            log.debug(Language.translate("Could not find anything in ") + directory);
        } else {
            try {
                for (int i = 0; i < children.length; i++) {
                    String filename = children[i];

                    File file = new File(directory + filename);

                    if (file.isDirectory()) {
                        if (new File(file.getAbsolutePath() + "/" + SketchletContextUtils.sketchletDataDir() + "/workspace.txt").exists()) {
                            bAddNode = true;
                            node.addItem(strPrefix + filename);
                            templateFolders.add(file.getPath());
                        } else {
                            if (filename.equals("history") || filename.equals("index") || filename.equals("notepad")) {
                                continue;
                            } // we are not going to load history of projects

                            boolean bTemp = populate(node, directory + filename, strPrefix + filename + " / ");
                            bAddNode = bAddNode || bTemp;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return bAddNode;
    }

    public boolean createProject() {
        if (this.projectNameField.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, Language.translate("Project name cannot be empty."), "Error!", JOptionPane.ERROR_MESSAGE);
            return false;
        } else if (new File(this.projectFolderField.getText()).exists()) {
            JOptionPane.showMessageDialog(this, Language.translate("Project folder already exists.") + "\n\n" + Language.translate("Select anouther name for the project") + "\n" + Language.translate("or change the project location."), Language.translate("Error!"), JOptionPane.ERROR_MESSAGE);
            return false;
        } else if (!(new File(this.projectFolderField.getText()).mkdirs())) {
            JOptionPane.showMessageDialog(this, Language.translate("Project folder could not be created.") + "\n\n" + Language.translate("Check that project name does not contain special characters."), Language.translate("Error!"), JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    public void setFolder() {
        String root = this.projectDirectoryField.getText();
        if (!root.endsWith("/") && !root.endsWith("\\")) {
            root += File.separator;
        }

        String name = this.projectNameField.getText();
        if (!name.equals("") && !name.endsWith("/") && !name.endsWith("\\")) {
            name += File.separator;
        }

        this.projectFolderField.setText(root + name);
    }

    public static void main(String[] args) {
        openDialog(null, Language.translate("New Project"));
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
        this.setFolder();
    }

    public void keyTyped(KeyEvent e) {
    }

    public static boolean openDialog(Frame owner, String title) {
        return openDialog(owner, title, null);
    }

    public static boolean openDialog(Frame owner, String title, String strName) {
        ProjectDialog dialog = new ProjectDialog(owner, true, title, strName);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);

        if (dialog.projectCreated) {
            projectFolder = dialog.projectFolderField.getText();
            Workspace.getMainPanel().setProjectTitle(dialog.projectNameField.getText());
        } else {
            projectFolder = null;
        }

        return dialog.projectCreated;
    }
}
