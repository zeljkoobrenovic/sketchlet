/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.ui.codegenerator;

import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.common.notepad.SyntaxPanel;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.GlobalProperties;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.jsp.JSPCodeGen;
import net.sf.sketchlet.designer.jsp.JSPGeneratedFile;
import net.sf.sketchlet.designer.jsp.JSPTemplate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class JSPCodeGeneratorPanel extends JPanel {

    JTabbedPane tabsCode = new JTabbedPane();
    JButton newTemplate = new JButton(Language.translate("new template"), Workspace.createImageIcon("resources/add.gif"));
    JButton save = new JButton(Language.translate("save"), Workspace.createImageIcon("resources/save.gif"));
    JButton reload = new JButton(Language.translate("reload"), Workspace.createImageIcon("resources/view-refresh.png"));
    JButton export = new JButton(Language.translate("export files..."), Workspace.createImageIcon("resources/export.gif"));
    JTabbedPane tabsTemplates = new JTabbedPane();
    JTabbedPane tabs = new JTabbedPane();
    // JComboBox dataPlatformsCombo = new JComboBox(CodeGeneratorFactory.DATA_PLATFORMS);
    JSPCodeGen jspGenerator = new JSPCodeGen();
    public static JSPCodeGeneratorPanel jspCodeGeneratorPanel;
    static JFileChooser fc = new JFileChooser();
    JProgressBar progress = new JProgressBar();
    JProgressBar progressData = new JProgressBar();
    // DataCodeGenerator dataGenerator;
    //JTabbedPane appTabs = new JTabbedPane();
    //JSplitPane splitPanel;

    public JSPCodeGeneratorPanel(final JDialog _frame) {
        jspCodeGeneratorPanel = this;

        JPanel panelView = new JPanel(new BorderLayout());

        tabs.setTabPlacement(JTabbedPane.TOP);
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        setLayout(new BorderLayout());
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(newTemplate);
        toolbar.add(save);
        toolbar.add(reload);
        toolbar.add(export);
        toolbar.add(progress);

        //this.dataGenerator = CodeGeneratorFactory.getDataGeneratorInstance();

        JPanel panelNorth = new JPanel(new BorderLayout());

        panelNorth.add(toolbar, BorderLayout.NORTH);

        panelView.add(panelNorth, BorderLayout.NORTH);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabsTemplates, tabs);
        splitPane.setDividerLocation(250);
        panelView.add(splitPane);

        add(panelView);

        reloadTemplates();

        newTemplate.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                addNewTemplate();
            }
        });

        save.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                save();
            }
        });

        reload.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                reload();
            }
        });
        export.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                progress.setMinimum(0);
                progress.setMaximum(SketchletEditor.pages.pages.size());
                fc.setApproveButtonText(Language.translate("Export"));
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                String strLastDir = GlobalProperties.get("codegen-last-export-dir");

                if (strLastDir != null && !strLastDir.isEmpty()) {
                    fc.setCurrentDirectory(new File(strLastDir));
                }

                fc.showOpenDialog(jspCodeGeneratorPanel);

                final File dir = fc.getSelectedFile();
                if (dir != null) {
                    new Thread(new Runnable() {

                        public void run() {
                            if (dir != null) {
                                GlobalProperties.setAndSave("codegen-last-export-dir", dir.getPath());
                                if (dir.list() != null && dir.list().length > 0) {
                                    int n = JOptionPane.showConfirmDialog(jspCodeGeneratorPanel, "The directory '" + dir.getName() + "' is not empty.\nDo you want to clean it?", "Clean Export Directory", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                                    if (n == JOptionPane.CANCEL_OPTION) {
                                        return;
                                    }
                                    progress.setIndeterminate(true);
                                    if (n == JOptionPane.YES_OPTION) {
                                        FileUtils.emptyDir(dir);
                                    }
                                }
                                progress.setIndeterminate(true);
                                // jspGenerator = CodeGenPluginFactory.getCodeGenPluginInstance();
                                jspGenerator.exportFiles(dir);
                            }
                            progress.setIndeterminate(false);
                            JOptionPane.showMessageDialog(jspCodeGeneratorPanel, "Export completed.");
                        }
                    }).start();
                }
            }
        });
    }

    public void dispose() {
        if (this.jspGenerator != null) {
            this.jspGenerator.dispose();
            this.jspGenerator = null;
        }
        jspCodeGeneratorPanel = null;
        this.tabs.removeAll();
        this.tabsCode.removeAll();
        this.removeAll();
    }

    public void save() {
        Vector<JSPTemplate> templates = jspGenerator.getTemplates();
        for (int i = 0; i < templates.size(); i++) {
            if (i < this.templateEditors.size()) {
                templates.elementAt(i).template = this.templateEditors.elementAt(i).getText();
                templates.elementAt(i).genFileName = this.nameFields.elementAt(i).getText();
            }
        }
        jspGenerator.saveTemplates();
    }

    public void addNewTemplate() {
        String name = JOptionPane.showInputDialog(this, Language.translate("File Name"));
        if (name != null) {
            this.jspGenerator.addTemplate(JSPTemplate.APPLICATION, name, "");
            this.jspGenerator.saveTemplates();
            reloadTemplates();
            if (this.tabsTemplates.getTabCount() > 0) {
                this.tabsTemplates.setSelectedIndex(this.tabsTemplates.getTabCount() - 1);
            }
        }
    }

    Vector<SyntaxPanel> templateEditors = new Vector<SyntaxPanel>();
    Vector<JTextField> nameFields = new Vector<JTextField>();

    public void reloadTemplates() {
        if (this.jspGenerator != null) {
            this.jspGenerator.dispose();
        }
        this.jspGenerator = new JSPCodeGen();
        int stab = tabsTemplates.getSelectedIndex();

        tabsTemplates.removeAll();
        templateEditors.removeAllElements();
        for (final JSPTemplate template : jspGenerator.getTemplates()) {
            JPanel panel = new JPanel(new BorderLayout());

            SyntaxPanel markupCodePanel = new SyntaxPanel();
            markupCodePanel.setText(template.getTemplateString());
            // markupCodePanel.jCmbLangs.setSelectedItem(pg.getFileMimeType());
            panel.add(markupCodePanel);
            templateEditors.add(markupCodePanel);

            JToolBar templateTB = new JToolBar();
            templateTB.setFloatable(false);
            panel.add(templateTB, BorderLayout.NORTH);

            templateTB.add(new JLabel(Language.translate("Type: ")));
            final JComboBox cmdType = new JComboBox(new String[]{"application", "one per page"});
            cmdType.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    if (cmdType.getSelectedIndex() == 1) {
                        template.type = JSPTemplate.PAGE;
                    } else {
                        template.type = JSPTemplate.APPLICATION;
                    }
                }
            });
            templateTB.add(cmdType);

            JTextField nameField = new JTextField(template.genFileName, 15);
            JLabel label = new JLabel(Language.translate("Generated file name: "));
            label.setToolTipText("You can use <%=page-name%> as a part of the string to embed the page name");
            templateTB.add(label);
            templateTB.add(nameField);
            nameFields.add(nameField);

            tabsTemplates.addTab(template.getFileName(), panel);
        }
        if (stab >= 0 && stab < tabs.getTabCount()) {
            tabsTemplates.setSelectedIndex(stab);
        }
        SketchletEditor.editorFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        reload();
    }

    public void reload() {
        this.save();
        int stab = tabs.getSelectedIndex();

        tabs.removeAll();

        for (JSPGeneratedFile pg : jspGenerator.getGeneratedFiles()) {
            JPanel panel = new JPanel(new BorderLayout());

            SyntaxPanel markupCodePanel = new SyntaxPanel();
            markupCodePanel.setText(pg.getText());
            // markupCodePanel.jCmbLangs.setSelectedItem(pg.getFileMimeType());
            panel.add(markupCodePanel);

            /*
            Vector<String> images = pg.getImageFilePaths();
            if (images != null && images.size() > 0) {
            JPanel filesPanel = new JPanel(new BorderLayout());
            JScrollPane list = new JScrollPane(new JList(pg.getImageFilePaths()));
            list.setSize(new Dimension(300, 100));
            list.setPreferredSize(new Dimension(300, 100));
            list.setMaximumSize(new Dimension(300, 100));
            filesPanel.add(list);
            filesPanel.setBorder(BorderFactory.createTitledBorder(Language.translate("Images")));
            panel.add(filesPanel, BorderLayout.SOUTH);
            }*/

            tabs.addTab(pg.getFileName(), panel);
        }

        /*tabsData.removeAll();
        
        for (CodeFile dcf : this.dataGenerator.generateCode()) {
        SyntaxPanel dc = new SyntaxPanel();
        dc.jCmbLangs.setSelectedItem(dcf.codeFormat);
        if (this.dataGenerator != null) {
        dc.setText(dcf.generatedCode);
        }
        tabsData.addTab(dcf.fileName, dc);
        }
        
         * */
        if (stab >= 0 && stab < tabs.getTabCount()) {
            tabs.setSelectedIndex(stab);
        }
        SketchletEditor.editorFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}
