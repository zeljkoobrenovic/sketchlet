package net.sf.sketchlet.designer.ui.region;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.context.ActiveRegionContextImpl;
import net.sf.sketchlet.designer.context.PageContextImpl;
import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.ui.UIUtils;
import net.sf.sketchlet.designer.ui.desktop.Notepad;
import net.sf.sketchlet.designer.ui.profiles.Profiles;
import net.sf.sketchlet.plugin.ScriptPluginAutoCompletion;
import net.sf.sketchlet.plugin.WidgetPlugin;
import net.sf.sketchlet.pluginloader.WidgetPluginFactory;
import net.sf.sketchlet.util.RefreshTime;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: zeljko
 * Date: 11-10-12
 * Time: 10:35
 * To change this template use File | Settings | File Templates.
 */
public class WidgetPanel extends JPanel {
    private KeyUpdateThread widgetItemsUpdateThread;
    // public JTextArea widgetItems = new JTextArea(4, 20);
    private RSyntaxTextArea widgetItems = Notepad.getInstance(RSyntaxTextArea.SYNTAX_STYLE_NONE);
    private JTabbedPane tabsWidget = new JTabbedPane();
    private WidgetEventsPanel widgetActionsPanel;
    private JTable tableWidgetProperties;
    private JComboBox widget;
    private static Notepad widgetNotepad;
    private java.util.Map<String, DefaultCompletionProvider> installedAutoCompletions = new HashMap<String, DefaultCompletionProvider>();
    private JButton btnImportItems = new JButton(Workspace.createImageIcon("resources/import.gif"));
    private JButton btnEditItems = new JButton(Workspace.createImageIcon("resources/edit.gif"));
    private JButton btnWidgetHelp = new JButton(Workspace.createImageIcon("resources/help-browser2.png"));

    private ActiveRegion region;

    public WidgetPanel(ActiveRegion region) {
        super(new BorderLayout());
        this.region = region;
        init();
    }

    public void reloadWidgetEvents() {
        this.widgetActionsPanel.refresh();
    }

    public void setTabsEnabledAt(int index, boolean bEnable) {
        if (tabsWidget != null && index >= 0 && index < tabsWidget.getTabCount()) {
            this.tabsWidget.setEnabledAt(index, bEnable);
        }
    }

    public void refreshComponents() {
        UIUtils.refreshComboBox(this.widget, region.strWidget);
        this.widgetItems.setText(region.strWidgetItems);
        widgetItems.setCaretPosition(0);
        UIUtils.refreshTable(tableWidgetProperties);
        widgetActionsPanel.refresh();
    }

    private void init() {
        widgetActionsPanel = new WidgetEventsPanel(this.region);
        this.widgetItems.setText(region.strWidgetItems);
        this.widgetItems.setCaretPosition(0);

        installPluginAutoCompletion();

        this.widgetItems.addKeyListener(new KeyAdapter() {

            public void keyReleased(KeyEvent e) {
                if (widgetNotepad != null) {
                    widgetNotepad.getTopLevelAncestor().setVisible(false);
                    widgetNotepad = null;
                }
                if (widgetItemsUpdateThread != null) {
                    widgetItemsUpdateThread.action = null;
                }
                widgetItemsUpdateThread = new KeyUpdateThread(new Runnable() {

                    public void run() {
                        if (!region.strWidgetItems.equals(widgetItems.getText())) {
                            region.strWidgetItems = widgetItems.getText();
                            installPluginAutoCompletion();
                            RefreshTime.update();
                            SketchletEditor.editorPanel.repaint();
                        }
                        widgetItemsUpdateThread = null;
                    }
                });
            }
        });
        widget = new JComboBox();
        populateControlsCombo(widget);
        UIUtils.removeActionListeners(widget);
        tableWidgetProperties = new JTable();
        tableWidgetProperties.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        tableWidgetProperties.setFillsViewportHeight(true);
        AbstractTableModel model = new AbstractTableModel() {

            String columns[] = new String[]{"Property", "Value", "Description"};

            public String getColumnName(int col) {
                return columns[col].toString();
            }

            public int getRowCount() {
                if (region == null) {
                    return 0;
                }
                String defaultProperties[][] = WidgetPluginFactory.getDefaultProperties(new ActiveRegionContextImpl(region, new PageContextImpl(region.parent.page)));
                return defaultProperties.length;
            }

            public int getColumnCount() {
                return columns.length;
            }

            public Object getValueAt(int row, int col) {
                String defaultProperties[][] = WidgetPluginFactory.getDefaultProperties(new ActiveRegionContextImpl(region, new PageContextImpl(region.parent.page)));
                if (row >= 0 && row < defaultProperties.length) {
                    switch (col) {
                        case 0:
                            return defaultProperties[row][0];
                        case 1:
                            return region.getWidgetProperty(defaultProperties[row][0], false);
                        case 2:
                            return defaultProperties[row][2];
                    }
                }
                return "";
            }

            public boolean isCellEditable(int row, int col) {
                return col == 1;
            }

            public void setValueAt(Object value, int row, int col) {
                if (col == 1) {
                    if (!getValueAt(row, col).toString().equals(value.toString())) {
                        SketchletEditor.editorPanel.saveRegionUndo();
                        region.setWidgetProperty(getValueAt(row, 0).toString(), value.toString());
                        RefreshTime.update();
                        SketchletEditor.editorPanel.repaint();
                    }
                }
            }

            public Class getColumnClass(int c) {
                return String.class;
            }
        };
        tableWidgetProperties.setModel(model);
        tableWidgetProperties.getColumnModel().getColumn(1).setCellEditor(new RegionWidgetPropertiesRowEditor(tableWidgetProperties, region));

        widget.setSelectedItem(region.strWidget);
        widget.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                region.widgetProperties = null;
                refreshComponents();
                if (widget.getSelectedItem() != null && !region.strWidget.equals(widget.getSelectedItem().toString())) {
                    region.widgetEventMacros.clear();
                    SketchletEditor.editorPanel.saveRegionUndo();
                    region.strWidget = (String) widget.getSelectedItem();
                    region.strWidgetProperties = WidgetPluginFactory.getDefaultPropertiesValue(new ActiveRegionContextImpl(region, new PageContextImpl(region.parent.page)));
                    region.widgetProperties = null;
                    TutorialPanel.addLine("cmd", "Set the region property: widget" + "=" + region.strWidget, "details.gif", widget);
                    if (region.strWidget.isEmpty()) {
                        widgetItems.setText("");
                        widgetItems.setEnabled(false);
                        btnWidgetHelp.setEnabled(false);
                        region.strWidgetItems = "";
                        btnImportItems.setEnabled(false);
                    } else {
                        ActiveRegionContextImpl regionContext = new ActiveRegionContextImpl(region, new PageContextImpl(region.parent.page));
                        region.strWidgetProperties = WidgetPluginFactory.getDefaultPropertiesValue(regionContext);
                        String strDefaultItems = WidgetPluginFactory.getDefaultItemsText(regionContext);
                        widgetItems.setText(strDefaultItems);
                        widgetItems.setCaretPosition(0);
                        region.strWidgetItems = strDefaultItems;
                        boolean bEnabled = WidgetPluginFactory.hasTextItems(regionContext);
                        widgetItems.setEnabled(bEnabled);
                        btnWidgetHelp.setEnabled(WidgetPluginFactory.hasLinks(regionContext));
                        btnImportItems.setEnabled(bEnabled);
                    }
                    widgetActionsPanel.refresh();

                    ((AbstractTableModel) tableWidgetProperties.getModel()).fireTableDataChanged();
                }
                RefreshTime.update();
                SketchletEditor.editorPanel.repaint();
            }
        });
        if (Profiles.isActive("active_region_widget")) {
            JPanel controls = new JPanel(new BorderLayout());
            controls.add(new JLabel(Language.translate("  Widget: ")), BorderLayout.WEST);
            controls.add(widget);

            TutorialPanel.prepare(widgetItems);
            JPanel controlsWrapper = new JPanel(new BorderLayout());
            controlsWrapper.add(controls, BorderLayout.NORTH);

            controlsWrapper.add(new JScrollPane(tableWidgetProperties), BorderLayout.CENTER);

            JSplitPane panelWidget;
            JPanel textItemsPanel = new JPanel(new BorderLayout());
            textItemsPanel.add(Notepad.getEditorPanel(widgetItems, false));
            btnImportItems.setToolTipText(Language.translate("Import text items from a file"));
            btnImportItems.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    ActiveRegionPanel.getFileChooser().setDialogTitle("Import Text Items");

                    ActiveRegionPanel.getFileChooser().setCurrentDirectory(new File(SketchletContextUtils.getSketchletDesignerTemplateFilesDir()));

                    int returnVal = ActiveRegionPanel.getFileChooser().showOpenDialog(SketchletEditor.editorFrame);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = ActiveRegionPanel.getFileChooser().getSelectedFile();
                        widgetItems.setText(FileUtils.getFileText(file.getPath()));
                        widgetItems.setCaretPosition(0);
                    }
                    RefreshTime.update();
                    SketchletEditor.editorPanel.repaint();
                }
            });
            btnEditItems.setToolTipText(Language.translate("Opens text items in a more advanced editor"));
            if (widgetNotepad != null) {
                if (widgetNotepad.onSave != null) {
                    widgetNotepad.onSave.run();
                }
                widgetNotepad.getTopLevelAncestor().setVisible(false);
                widgetNotepad = null;
            }
            btnEditItems.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    if (widgetNotepad != null) {
                        widgetNotepad.getTopLevelAncestor().setVisible(false);
                        widgetNotepad = null;
                    }
                    widgetNotepad = Notepad.openNotepadFromString(widgetItems.getText(), Language.translate("Widget Text/Items"), "text/java");
                    widgetNotepad.onSave = new Runnable() {

                        public void run() {
                            String strText = widgetNotepad.editor.getText();
                            if (!strText.equals(region.strWidgetItems)) {
                                SketchletEditor.editorPanel.saveRegionUndo();
                                widgetItems.setText(strText);
                                widgetItems.setCaretPosition(0);
                                region.strWidgetItems = strText;
                                RefreshTime.update();
                                SketchletEditor.editorPanel.repaint();
                            }
                        }
                    };
                }
            });
            btnWidgetHelp.setToolTipText(Language.translate("Open the widget help link"));
            btnWidgetHelp.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    String links[][] = WidgetPluginFactory.getLinks(new ActiveRegionContextImpl(region, new PageContextImpl(region.parent.page)));
                    if (links != null) {
                        for (String s[] : links) {
                            if (s.length > 1) {
                                SketchletContextUtils.openWebBrowser(s[1]);
                            }
                        }
                    }
                }
            });
            JPanel panelEast = new JPanel(new GridLayout(3, 0));
            panelEast.add(btnImportItems);
            panelEast.add(this.btnEditItems);
            panelEast.add(this.btnWidgetHelp);
            textItemsPanel.add(panelEast, BorderLayout.EAST);
            panelWidget = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, controlsWrapper, textItemsPanel);
            panelWidget.setOneTouchExpandable(true);
            panelWidget.setDividerLocation(350);
            boolean bEnabled = WidgetPluginFactory.hasTextItems(new ActiveRegionContextImpl(region, new PageContextImpl(region.parent.page)));
            widgetItems.setEnabled(bEnabled);
            btnWidgetHelp.setEnabled(WidgetPluginFactory.hasLinks(new ActiveRegionContextImpl(region, new PageContextImpl(region.parent.page))));
            btnImportItems.setEnabled(true);
            btnEditItems.setEnabled(bEnabled);

            tabsWidget.setFont(tabsWidget.getFont().deriveFont(9.0f));
            tabsWidget.putClientProperty("JComponent.sizeVariant", "small");
            tabsWidget.addTab("Settings", panelWidget);
            tabsWidget.addTab("Events", new JLabel());
            tabsWidget.setComponentAt(1, widgetActionsPanel);
        }

        add(tabsWidget);
    }

    public static void populateControlsCombo(JComboBox comboBox) {
        Object selectedItem = comboBox.getSelectedItem();

        comboBox.removeAllItems();
        comboBox.setEditable(true);
        comboBox.addItem("");

        if (SketchletEditor.editorPanel != null) {
            String ctrls[] = WidgetPluginFactory.getWidgetList();
            for (int i = 0; i < ctrls.length; i++) {
                comboBox.addItem(ctrls[i]);
            }
        }

        if (selectedItem != null) {
            comboBox.setSelectedItem(selectedItem);
        } else {
            comboBox.setSelectedIndex(0);
        }
    }

    public void installPluginAutoCompletion() {
        final WidgetPlugin widgetInstance = region.renderer.widgetImageLayer.widgetControl;
        if (widgetInstance != null && widgetInstance instanceof ScriptPluginAutoCompletion) {
            Map<String, java.util.List<String>> map = ((ScriptPluginAutoCompletion) widgetInstance).getAutoCompletionPairs();
            installPluginAutoCompletion(widgetItems, map);
        }
    }

    private synchronized void installPluginAutoCompletion(RSyntaxTextArea editor, Map<String, java.util.List<String>> pairs) {
        for (final String key : pairs.keySet()) {
            java.util.List<String> values = pairs.get(key);
            DefaultCompletionProvider provider = installedAutoCompletions.get(key);
            if (provider == null) {
                provider = new DefaultCompletionProvider() {
                    public boolean isAutoActivateOkay(JTextComponent tc) {
                        try {
                            Document doc = tc.getDocument();
                            String lastCharactersTyped = doc.getText(tc.getCaretPosition() - (key.length() - 1), key.length());
                            return (lastCharactersTyped.equals(key));
                        } catch (BadLocationException e) {
                        }
                        return false;
                    }
                };
                provider.setAutoActivationRules(false, null);
                provider.setAutoActivationRules(true, key);

                AutoCompletion ac = new AutoCompletion(provider);
                ac.setAutoActivationDelay(180);
                ac.setAutoActivationEnabled(true);
                ac.install(editor);
                installedAutoCompletions.put(key, provider);
            }

            provider.clear();
            for (String value : values) {
                if (value.startsWith(key)) {
                    value = value.substring(key.length());
                }
                provider.addCompletion(new ShorthandCompletion(provider, value, value));
            }
        }
    }
}
