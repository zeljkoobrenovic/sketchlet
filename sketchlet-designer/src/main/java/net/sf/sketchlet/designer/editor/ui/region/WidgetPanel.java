package net.sf.sketchlet.designer.editor.ui.region;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.context.ActiveRegionContextImpl;
import net.sf.sketchlet.designer.context.PageContextImpl;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.SyntaxEditorWrapper;
import net.sf.sketchlet.designer.editor.ui.UIUtils;
import net.sf.sketchlet.designer.editor.ui.desktop.Notepad;
import net.sf.sketchlet.designer.editor.ui.profiles.Profiles;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.loaders.pluginloader.WidgetPluginFactory;
import net.sf.sketchlet.plugin.ScriptPluginAutoCompletion;
import net.sf.sketchlet.plugin.WidgetPlugin;
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
 * @author zeljko
 */
public class WidgetPanel extends JPanel {
    private KeyUpdateThread widgetItemsUpdateThread;
    private SyntaxEditorWrapper widgetItems = Notepad.getInstance(RSyntaxTextArea.SYNTAX_STYLE_NONE);
    private JTabbedPane tabsWidget = new JTabbedPane();
    private WidgetEventsPanel widgetActionsPanel;
    private JTable tableWidgetProperties;
    private JComboBox widget;
    private static Notepad widgetNotepad;
    private java.util.Map<String, DefaultCompletionProvider> installedAutoCompletions = new HashMap<String, DefaultCompletionProvider>();
    private JButton btnImportItems = new JButton(Workspace.createImageIcon("resources/import.gif"));
    private JButton btnEditItems = new JButton(Workspace.createImageIcon("resources/edit-undocked.gif"));
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

    public void refreshComponents() {
        UIUtils.refreshComboBox(this.widget, region.getWidget());
        this.widgetItems.getSyntaxTextArea().setText(region.getWidgetItems());
        widgetItems.getSyntaxTextArea().setCaretPosition(0);
        UIUtils.refreshTable(tableWidgetProperties);
        widgetActionsPanel.refresh();
    }

    private void init() {
        widgetActionsPanel = new WidgetEventsPanel(this.region);
        this.widgetItems.getSyntaxTextArea().setText(region.getWidgetItems());
        this.widgetItems.getSyntaxTextArea().setCaretPosition(0);

        installPluginAutoCompletion();

        this.widgetItems.getSyntaxTextArea().addKeyListener(new KeyAdapter() {

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
                        if (!region.getWidgetItems().equals(widgetItems.getSyntaxTextArea().getText())) {
                            refreshAfterTextChange();
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

            String columns[] = new String[]{Language.translate("Property"), Language.translate("Value"), Language.translate("Description")};

            public String getColumnName(int col) {
                return columns[col].toString();
            }

            public int getRowCount() {
                if (region == null) {
                    return 0;
                }
                String defaultProperties[][] = WidgetPluginFactory.getDefaultProperties(new ActiveRegionContextImpl(region, new PageContextImpl(region.getParent().getPage())));
                return defaultProperties.length;
            }

            public int getColumnCount() {
                return columns.length;
            }

            public Object getValueAt(int row, int col) {
                String defaultProperties[][] = WidgetPluginFactory.getDefaultProperties(new ActiveRegionContextImpl(region, new PageContextImpl(region.getParent().getPage())));
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
                        SketchletEditor.getInstance().saveRegionUndo();
                        region.setWidgetProperty(getValueAt(row, 0).toString(), value.toString());
                        RefreshTime.update();
                        SketchletEditor.getInstance().repaint();
                    }
                }
            }

            public Class getColumnClass(int c) {
                return String.class;
            }
        };
        tableWidgetProperties.setModel(model);
        tableWidgetProperties.getColumnModel().getColumn(1).setCellEditor(new RegionWidgetPropertiesRowEditor(tableWidgetProperties, region));

        widget.setSelectedItem(region.getWidget());
        widget.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                region.setWidgetProperties(null);
                if (widget.getSelectedItem() != null && !region.getWidget().equals(widget.getSelectedItem().toString())) {
                    region.getWidgetEventMacros().clear();
                    SketchletEditor.getInstance().saveRegionUndo();
                    region.setWidget((String) widget.getSelectedItem());
                    region.setWidgetPropertiesString(WidgetPluginFactory.getDefaultPropertiesValue(new ActiveRegionContextImpl(region, new PageContextImpl(region.getParent().getPage()))));
                    region.setWidgetProperties(null);
                    if (region.getWidget().isEmpty()) {
                        widgetItems.getSyntaxTextArea().setText("");
                        widgetItems.getSyntaxTextArea().setEnabled(false);
                        btnWidgetHelp.setEnabled(false);
                        region.setWidgetItems("");
                        btnImportItems.setEnabled(false);
                    } else {
                        ActiveRegionContextImpl regionContext = new ActiveRegionContextImpl(region, new PageContextImpl(region.getParent().getPage()));
                        region.setWidgetPropertiesString(WidgetPluginFactory.getDefaultPropertiesValue(regionContext));
                        String strDefaultItems = WidgetPluginFactory.getDefaultItemsText(regionContext);
                        widgetItems.getSyntaxTextArea().setText(strDefaultItems);
                        widgetItems.getSyntaxTextArea().setCaretPosition(0);
                        region.setWidgetItems(strDefaultItems);
                        boolean bEnabled = WidgetPluginFactory.hasTextItems(regionContext);
                        widgetItems.getSyntaxTextArea().setEnabled(bEnabled);
                        btnWidgetHelp.setEnabled(WidgetPluginFactory.hasLinks(regionContext));
                        btnImportItems.setEnabled(bEnabled);
                    }
                    widgetActionsPanel.refresh();

                    ((AbstractTableModel) tableWidgetProperties.getModel()).fireTableDataChanged();
                }
                RefreshTime.update();
                SketchletEditor.getInstance().repaint();
            }
        });
        if (Profiles.isActive("active_region_widget")) {
            JPanel controls = new JPanel(new BorderLayout());
            controls.add(new JLabel(Language.translate("  Widget: ")), BorderLayout.WEST);
            controls.add(widget);

            JPanel controlsWrapper = new JPanel(new BorderLayout());
            controlsWrapper.add(controls, BorderLayout.NORTH);

            controlsWrapper.add(new JScrollPane(tableWidgetProperties), BorderLayout.CENTER);

            JSplitPane panelWidget;
            JPanel textItemsPanel = new JPanel(new BorderLayout());
            textItemsPanel.add(Notepad.getEditorPanel(widgetItems.getSyntaxTextArea(), new Runnable() {
                @Override
                public void run() {
                    refreshAfterTextChange();
                }
            }, false));
            btnImportItems.setToolTipText(Language.translate("Import text items from a file"));
            btnImportItems.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    ActiveRegionPanel.getFileChooser().setDialogTitle("Import Text Items");

                    ActiveRegionPanel.getFileChooser().setCurrentDirectory(new File(SketchletContextUtils.getSketchletDesignerTemplateFilesDir()));

                    int returnVal = ActiveRegionPanel.getFileChooser().showOpenDialog(SketchletEditor.editorFrame);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = ActiveRegionPanel.getFileChooser().getSelectedFile();
                        widgetItems.getSyntaxTextArea().setText(FileUtils.getFileText(file.getPath()));
                        widgetItems.getSyntaxTextArea().setCaretPosition(0);
                    }
                    RefreshTime.update();
                    SketchletEditor.getInstance().repaint();
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
                    widgetNotepad = Notepad.openNotepadFromString(widgetItems.getSyntaxTextArea().getText(), Language.translate("Widget Text/Items"), "text/java");
                    widgetNotepad.onSave = new Runnable() {

                        public void run() {
                            String strText = widgetNotepad.editor.getText();
                            if (!strText.equals(region.getWidgetItems())) {
                                SketchletEditor.getInstance().saveRegionUndo();
                                widgetItems.getSyntaxTextArea().setText(strText);
                                widgetItems.getSyntaxTextArea().setCaretPosition(0);
                                region.setWidgetItems(strText);
                                RefreshTime.update();
                                SketchletEditor.getInstance().repaint();
                            }
                        }
                    };
                }
            });
            btnWidgetHelp.setToolTipText(Language.translate("Open the widget help link"));
            btnWidgetHelp.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    String links[][] = WidgetPluginFactory.getLinks(new ActiveRegionContextImpl(region, new PageContextImpl(region.getParent().getPage())));
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
            boolean bEnabled = WidgetPluginFactory.hasTextItems(new ActiveRegionContextImpl(region, new PageContextImpl(region.getParent().getPage())));
            widgetItems.getSyntaxTextArea().setEnabled(bEnabled);
            btnWidgetHelp.setEnabled(WidgetPluginFactory.hasLinks(new ActiveRegionContextImpl(region, new PageContextImpl(region.getParent().getPage()))));
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

    private void refreshAfterTextChange() {
        region.setWidgetItems(widgetItems.getSyntaxTextArea().getText());
        installPluginAutoCompletion();
        RefreshTime.update();
        SketchletEditor.getInstance().repaint();
    }

    private static void populateControlsCombo(JComboBox comboBox) {
        Object selectedItem = comboBox.getSelectedItem();

        comboBox.removeAllItems();
        comboBox.setEditable(true);
        comboBox.addItem("");

        if (SketchletEditor.getInstance() != null) {
            String widgets[] = WidgetPluginFactory.getWidgetList();
            for (int i = 0; i < widgets.length; i++) {
                comboBox.addItem(widgets[i]);
            }
        }

        if (selectedItem != null) {
            comboBox.setSelectedItem(selectedItem);
        } else {
            comboBox.setSelectedIndex(0);
        }
    }

    public void installPluginAutoCompletion() {
        final WidgetPlugin widgetInstance = region.getRenderer().getWidgetImageLayer().getWidgetPlugin();
        if (widgetInstance != null && widgetInstance instanceof ScriptPluginAutoCompletion) {
            Map<String, java.util.List<String>> map = ((ScriptPluginAutoCompletion) widgetInstance).getAutoCompletionPairs();
            installPluginAutoCompletion(widgetItems.getSyntaxTextArea(), map);
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
