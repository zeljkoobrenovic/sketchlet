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
import net.sf.sketchlet.plugin.CodeGenPlugin;
import net.sf.sketchlet.plugin.CodeGenPluginFile;
import net.sf.sketchlet.plugin.CodeGenPluginSetting;
import net.sf.sketchlet.pluginloader.CodeGenPluginFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

/**
 * @author zobrenovic
 */
public class CodeGeneratorPanel extends JPanel {

    JTabbedPane tabsCode = new JTabbedPane();
    JButton reload = new JButton(Language.translate("reload"), Workspace.createImageIcon("resources/view-refresh.png"));
    JButton export = new JButton(Language.translate("export files..."), Workspace.createImageIcon("resources/export.gif"));
    JButton exportData = new JButton(Language.translate("export files..."), Workspace.createImageIcon("resources/export.gif"));
    JButton btnSettings = new JButton(Language.translate("settings..."));
    JTabbedPane tabs = new JTabbedPane();
    //JTabbedPane tabsData = new JTabbedPane();
    JComboBox platformsCombo = new JComboBox(CodeGenPluginFactory.getPlatforms());
    // JComboBox dataPlatformsCombo = new JComboBox(CodeGeneratorFactory.DATA_PLATFORMS);
    CodeGenPlugin currentCodeGenerator;
    public static CodeGeneratorPanel codeGeneratorPanel;
    static JFileChooser fc = new JFileChooser();
    JProgressBar progress = new JProgressBar();
    JProgressBar progressData = new JProgressBar();
    // DataCodeGenerator dataGenerator;
    //JTabbedPane appTabs = new JTabbedPane();
    //JSplitPane splitPanel;
    List<CodeGenPluginSetting> settings;

    public CodeGeneratorPanel(final JDialog _frame) {
        codeGeneratorPanel = this;

        JPanel panelView = new JPanel(new BorderLayout());

        tabs.setTabPlacement(JTabbedPane.TOP);
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        setLayout(new BorderLayout());
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new JLabel(Language.translate("Platform: ")));
        toolbar.add(platformsCombo);
        toolbar.add(btnSettings);
        toolbar.add(reload);
        toolbar.add(export);
        toolbar.add(progress);

        //this.dataGenerator = CodeGeneratorFactory.getDataGeneratorInstance();

        JPanel panelNorth = new JPanel(new BorderLayout());

        panelNorth.add(toolbar, BorderLayout.NORTH);

        panelView.add(panelNorth, BorderLayout.NORTH);
        panelView.add(tabs);

        add(panelView);

        reload();

        platformsCombo.setSelectedItem(CodeGenPluginFactory.platform);
        platformsCombo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                String platform = (String) platformsCombo.getSelectedItem();
                if (platform != null) {
                    CodeGenPluginFactory.platform = platform;
                    GlobalProperties.set("platform", platform);
                    GlobalProperties.save();
                    reload();
                    btnSettings.setEnabled(settings != null && settings.size() > 0);
                }
            }
        });

        btnSettings.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (settings != null) {
                    JDialog dlg = CodeGeneratorUIUtils.getSettingsDialog(codeGeneratorPanel, settings);
                    dlg.setVisible(true);
                }
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

                fc.showOpenDialog(codeGeneratorPanel);

                final File dir = fc.getSelectedFile();
                if (dir != null) {
                    new Thread(new Runnable() {

                        public void run() {
                            if (dir != null) {
                                GlobalProperties.setAndSave("codegen-last-export-dir", dir.getPath());
                                if (dir.list() != null && dir.list().length > 0) {
                                    int n = JOptionPane.showConfirmDialog(codeGeneratorPanel, "The directory '" + dir.getName() + "' is not empty.\nDo you want to clean it?", "Clean Export Directory", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                                    if (n == JOptionPane.CANCEL_OPTION) {
                                        return;
                                    }
                                    progress.setIndeterminate(true);
                                    if (n == JOptionPane.YES_OPTION) {
                                        FileUtils.emptyDir(dir);
                                    }
                                }
                                progress.setIndeterminate(true);
                                // currentCodeGenerator = CodeGenPluginFactory.getCodeGenPluginInstance();
                                for (CodeGenPluginFile file : currentCodeGenerator.getFiles()) {
                                    file.exportFile(dir);
                                }
                            }
                            progress.setIndeterminate(false);
                            JOptionPane.showMessageDialog(codeGeneratorPanel, "Export completed.");
                        }
                    }).start();
                }
            }
        });

    }

    public void dispose() {
        if (this.currentCodeGenerator != null) {
            this.currentCodeGenerator.dispose();
            this.currentCodeGenerator = null;
        }
        codeGeneratorPanel = null;
        this.tabs.removeAll();
        this.tabsCode.removeAll();
        this.removeAll();
    }

    public void reload() {
        if (this.currentCodeGenerator != null) {
            this.currentCodeGenerator.dispose();
        }
        this.currentCodeGenerator = CodeGenPluginFactory.getCodeGenPluginInstance();
        int stab = tabs.getSelectedIndex();

        tabs.removeAll();

        if (currentCodeGenerator != null) {
            for (CodeGenPluginFile pg : currentCodeGenerator.getFiles()) {
                JPanel panel = new JPanel(new BorderLayout());

                SyntaxPanel markupCodePanel = new SyntaxPanel();
                markupCodePanel.setText(pg.getPreviewText());
                markupCodePanel.jCmbLangs.setSelectedItem(pg.getFileMimeType());
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
        }
        settings = currentCodeGenerator == null ? null : this.currentCodeGenerator.getSettings();
        btnSettings.setEnabled(settings != null && settings.size() > 0);

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

        if (tabs.getTabCount() == 0) {
            tabs.addTab("", new JLabel("    Code generator plugins are not installed.    "));
        }
        SketchletEditor.editorFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}
