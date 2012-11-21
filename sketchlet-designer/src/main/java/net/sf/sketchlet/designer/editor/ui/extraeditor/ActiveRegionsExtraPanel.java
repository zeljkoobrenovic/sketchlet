/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.ui.extraeditor;

import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.tools.log.ActivityLog;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionPanel;
import net.sf.sketchlet.model.ActiveRegion;
import net.sf.sketchlet.model.Page;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.Vector;

/**
 * @author cuypers
 */
public class ActiveRegionsExtraPanel extends JPanel {

    public JTabbedPane tabs;
    public ActiveRegionPanel previousPanel;
    static int subTabIndex = 0;
    static int subTabIndexProperties = 0;
    public static ActiveRegionsExtraPanel regionsAndActions = null;
    boolean initializing = false;

    public ActiveRegionsExtraPanel() {
        regionsAndActions = this;
        tabs = new JTabbedPane();
        tabs.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        tabs.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(tabs);

        setLayout(new BorderLayout());

        add(tabs);

        tabs.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (initializing) {
                    return;
                }
                if (previousTabIndex >= 0 && previousTabIndex < tabs.getTabCount()) {
                    tabs.setComponentAt(previousTabIndex, new JPanel());
                }
                for (int i = 0; i < tabs.getTabCount(); i++) {
                    String strID = (tabs.getTabCount() - i) + "";
                    JLabel label = new JLabel(strID);
                    label.putClientProperty("JComponent.sizeVariant", "small");
                    SwingUtilities.updateComponentTreeUI(label);
                    tabs.setTabComponentAt(i, label);
                }
                int selectedTabIndex = tabs.getSelectedIndex();
                ActivityLog.log("selectTabRegion", selectedTabIndex + "");
                if (selectedTabIndex >= 0 && selectedTabIndex < SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().size() && !initializing) {
                    ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().elementAt(selectedTabIndex);
                    int t = 0;
                    int timage = 0;
                    int tevent = 0;
                    int ttransform = 0;
                    if (previousPanel != null) {
                        t = previousPanel.tabs.getSelectedIndex();
                        timage = previousPanel.tabsImage.getSelectedIndex();
                        tevent = previousPanel.tabsRegionEvents.getSelectedIndex();
                        ttransform = previousPanel.tabsTransform.getSelectedIndex();
                    }
                    ActiveRegionPanel p = new ActiveRegionPanel(SketchletEditor.getPages(), region, t);
                    tabs.setComponentAt(selectedTabIndex, p);
                    if (SketchletEditor.getInstance().getCurrentPage().getRegions().getSelectedRegions() == null) {
                        SketchletEditor.getInstance().getCurrentPage().getRegions().setSelectedRegions(new Vector<ActiveRegion>());
                    }
                    if (!SketchletEditor.getInstance().getCurrentPage().getRegions().getSelectedRegions().contains(region)) {
                        if (!(SketchletEditor.getInstance().getCurrentPage().getRegions().getSelectedRegions().size() > 1)) {
                            SketchletEditor.getInstance().getCurrentPage().getRegions().setSelectedRegions(new Vector<ActiveRegion>());
                            SketchletEditor.getInstance().getCurrentPage().getRegions().addToSelection(region);
                        }
                    }
                    SketchletEditor.getInstance().repaint();

                    if (previousPanel != null) {
                        subTabIndex = previousPanel.tabs.getSelectedIndex();
                        subTabIndexProperties = previousPanel.tabsTransform.getSelectedIndex();
                    }

                    if (subTabIndex >= 0 && subTabIndex < p.tabs.getTabCount()) {
                        p.tabs.setSelectedIndex(subTabIndex);
                    }
                    if (timage >= 0 && timage < p.tabsImage.getTabCount()) {
                        p.tabsImage.setSelectedIndex(timage);
                    }
                    if (tevent >= 0 && tevent < p.tabsRegionEvents.getTabCount()) {
                        p.tabsRegionEvents.setSelectedIndex(tevent);
                    }
                    if (ttransform >= 0 && ttransform < p.tabsTransform.getTabCount()) {
                        p.tabsTransform.setSelectedIndex(ttransform);
                    }

                    p.refresh();

                    previousPanel = p;

                    tabs.setTabComponentAt(selectedTabIndex, new CheckButtonTabComponent(region));
                }
            }
        });
    }

    public int previousTabIndex = -1;

    public void save() {
        updateUIControls();
        SketchletEditor.getInstance().getCurrentPage().getRegions().createNewVariables();
        SketchletEditor.getInstance().repaint();
    }

    public static void updateUIControls() {
        if (regionsAndActions != null && SketchletEditor.getInstance().getCurrentPage() != null && SketchletEditor.getPages() != null) {
            ActiveRegionPanel p = (ActiveRegionPanel) regionsAndActions.tabs.getSelectedComponent();

            if (p != null) {
                JTable tables[] = {p.tableLimits, p.tableUpdateTransformations};

                for (int it = 0; it < tables.length; it++) {
                    TableCellEditor tce = tables[it].getCellEditor();

                    if (tce != null) {
                        tce.stopCellEditing();
                    }
                }
            }
            if (p != null && p.region != null) {
                p.imageUrlField.setSelectedItem(p.imageUrlField.getEditor().getItem());
                p.updateUIControls();
                p.imageIndex.setSelectedItem(p.imageIndex.getEditor().getItem());
                p.animationMs.setSelectedItem(p.animationMs.getEditor().getItem());
            }
        }
    }

    public static void showRegionsAndActions(final boolean bShow) {
        if (bShow) {
            SketchletEditor.getInstance().showExtraEditorPanel(ExtraEditorPanel.indexActiveRegion);
            reload();
        }
    }

    public static void showRegionsAndActions() {
        showRegionsAndActions(-1);
    }

    public static void showRegionsAndActionsImage(int tabIndex) {
        if (tabIndex >= 0) {
            ActiveRegionPanel p = (ActiveRegionPanel) regionsAndActions.tabs.getSelectedComponent();
            p.tabs.setSelectedIndex(0);
            p.tabsImage.setSelectedIndex(tabIndex);
        }
        SketchletEditor.getInstance().showExtraEditorPanel(ExtraEditorPanel.indexActiveRegion);
    }

    public static void showRegionsAndActions(int tabIndex) {
        if (tabIndex >= 0) {
            ActiveRegionPanel p = (ActiveRegionPanel) regionsAndActions.tabs.getSelectedComponent();
            p.tabs.setSelectedIndex(tabIndex);
        }
        SketchletEditor.getInstance().showExtraEditorPanel(ExtraEditorPanel.indexActiveRegion);
    }

    public static void reload() {
        if (regionsAndActions != null && SketchletEditor.getInstance().getCurrentPage() != null && SketchletEditor.getPages() != null) {
            regionsAndActions.init();
            regionsAndActions.tabs.setSelectedIndex(regionsAndActions.tabs.getTabCount() - 1);
        }
    }

    public static void reload(ActiveRegion action) {
        if (action != null && regionsAndActions != null && SketchletEditor.getInstance().getCurrentPage() != null && SketchletEditor.getPages() != null) {
            ActiveRegionPanel p = (ActiveRegionPanel) regionsAndActions.tabs.getSelectedComponent();
            int s = 0;
            int ss = 0;
            JTabbedPane subTab = null;
            if (p != null) {
                s = p.tabs.getSelectedIndex();
                if (s >= 0) {
                    String title = p.tabs.getTitleAt(s).trim().toLowerCase();
                    if (title.contains("graphic")) {
                        subTab = p.tabsImage;
                    } else if (title.contains("transform")) {
                        subTab = p.tabsTransform;
                    } else if (title.contains("move")) {
                        subTab = p.tabsMove;
                    } else {
                        subTab = null;
                    }
                }
                if (subTab != null) {
                    ss = subTab.getSelectedIndex();
                }
            }

            regionsAndActions.init();
            regionsAndActions.tabs.setSelectedIndex(SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().indexOf(action));
            p = (ActiveRegionPanel) regionsAndActions.tabs.getSelectedComponent();

            if (p != null) {
                p.tabs.setSelectedIndex(s);

                if (s >= 0 && p.tabs.getTabCount() > s) {
                    String title = p.tabs.getTitleAt(s);
                    if (title != null) {
                        title = title.trim().toLowerCase();
                        if (title.contains("graphic")) {
                            subTab = p.tabsImage;
                        } else if (title.contains("transform")) {
                            subTab = p.tabsTransform;
                        } else if (title.contains("move")) {
                            subTab = p.tabsMove;
                        } else {
                            subTab = null;
                        }
                    }
                }
                if (subTab != null && ss >= 0 && subTab.getTabCount() > ss) {
                    subTab.setSelectedIndex(ss);
                }
            }

        }
    }

    public static void refresh(final ActiveRegion action) {
        java.awt.EventQueue.invokeLater(
                new Runnable() {

                    public void run() {
                        refresh(action, -1);
                    }
                });
    }

    public static ActiveRegionPanel refresh(ActiveRegion action, int tabIndex) {
        ActiveRegionPanel p = null;

        if (action != null && regionsAndActions != null && SketchletEditor.getInstance().getCurrentPage() != null && SketchletEditor.getPages() != null) {
            if (regionsAndActions.tabs.getTabCount() != action.parent.getRegions().size()) {
                regionsAndActions.init();
            }
            if (regionsAndActions.tabs.getSelectedComponent() instanceof ActiveRegionPanel) {
                p = (ActiveRegionPanel) regionsAndActions.tabs.getSelectedComponent();
            }
            int s = tabIndex;
            int timage = 0;
            int tevent = 0;
            int ttransform = 0;
            if (p != null) {
                if (s < 0) {
                    s = p.tabs.getSelectedIndex();
                }
                timage = p.tabsImage.getSelectedIndex();
                tevent = p.tabsRegionEvents.getSelectedIndex();
                ttransform = p.tabsTransform.getSelectedIndex();
            }
            int index = SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().indexOf(action);
            if (index < regionsAndActions.tabs.getTabCount()) {
                regionsAndActions.tabs.setSelectedIndex(index);
                if (regionsAndActions.tabs.getSelectedComponent() instanceof ActiveRegionPanel) {
                    p = (ActiveRegionPanel) regionsAndActions.tabs.getSelectedComponent();
                }
                if (p != null) {
                    p.tabs.setSelectedIndex(s);
                    p.tabsImage.setSelectedIndex(timage);
                    p.tabsRegionEvents.setSelectedIndex(tevent);
                    p.tabsTransform.setSelectedIndex(ttransform);
                }
            }
        }

        if (p != null) {
            p.installPluginAutoCompletion();
        }

        return p;
    }

    public static ActiveRegionPanel refresh(ActiveRegion action, int tabIndex, int subIndex) {
        ActiveRegionPanel p = null;

        if (action != null && regionsAndActions != null && SketchletEditor.getInstance().getCurrentPage() != null && SketchletEditor.getPages() != null) {
            if (regionsAndActions.tabs.getTabCount() != action.parent.getRegions().size()) {
                regionsAndActions.init();
            }
            if (regionsAndActions.tabs.getSelectedComponent() instanceof ActiveRegionPanel) {
                p = (ActiveRegionPanel) regionsAndActions.tabs.getSelectedComponent();
            }
            int s = tabIndex;
            if (p != null) {
                if (s < 0) {
                    s = p.tabs.getSelectedIndex();
                }
            }
            int index = SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().indexOf(action);
            if (index < regionsAndActions.tabs.getTabCount()) {
                regionsAndActions.tabs.setSelectedIndex(index);
                if (regionsAndActions.tabs.getSelectedComponent() instanceof ActiveRegionPanel) {
                    p = (ActiveRegionPanel) regionsAndActions.tabs.getSelectedComponent();
                }
                if (p != null) {
                    p.tabs.setSelectedIndex(s);

                    if (tabIndex == ActiveRegionPanel.indexGraphics) {
                        p.tabsImage.setSelectedIndex(subIndex);
                    }
                    if (tabIndex == ActiveRegionPanel.indexEvents) {
                        p.tabsRegionEvents.setSelectedIndex(subIndex);
                    }
                    if (tabIndex == ActiveRegionPanel.indexTransform) {
                        p.tabsTransform.setSelectedIndex(subIndex);
                    }
                }
            }
        }

        return p;
    }

    public void init() {
        initializing = true;
        Page page = SketchletEditor.getInstance().getCurrentPage();
        int s = 0;
        ActiveRegionPanel p;
        if (tabs.getSelectedComponent() != null && tabs.getSelectedComponent() instanceof ActiveRegionPanel) {
            p = (ActiveRegionPanel) tabs.getSelectedComponent();
            if (p != null) {
                s = p.tabs.getSelectedIndex();
            }
        }
        tabs.removeAll();

        int i = page.getRegions().getRegions().size();

        for (ActiveRegion a : page.getRegions().getRegions()) {
            try {
                tabs.add(new JPanel(), i-- + "");
                if (a.isSelected()) {
                    tabs.setTabComponentAt(tabs.getTabCount() - 1, new CheckButtonTabComponent(a));
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        if (page.getRegions().getSelectedRegions() != null && page.getRegions().getSelectedRegions().size() > 0) {
            int index = page.getRegions().getRegions().indexOf(page.getRegions().getSelectedRegions().lastElement());
            tabs.setSelectedIndex(index);
            int t = 0;
            if (previousPanel != null) {
                t = previousPanel.tabs.getSelectedIndex();
            }
            p = new ActiveRegionPanel(SketchletEditor.getPages(), page.getRegions().getRegions().elementAt(index), t);
            tabs.setComponentAt(index, p);
        } else if (tabs.getTabCount() > 0) {
            tabs.setSelectedIndex(0);
            p = new ActiveRegionPanel(SketchletEditor.getPages(), page.getRegions().getRegions().elementAt(0), 0);
            tabs.setComponentAt(0, p);
        }

        ActiveRegionPanel.refreshRegionComboBox(page.getRegions().getRegions().size());
        initializing = false;
    }

    public static void main(String[] args) {
        ActiveRegionsExtraPanel f = new ActiveRegionsExtraPanel();
    }
}
