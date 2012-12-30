package net.sf.sketchlet.designer.editor.ui.extraeditor;

import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.framework.model.log.ActivityLog;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionPanel;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.model.Page;

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
                        t = previousPanel.getTabs().getSelectedIndex();
                        timage = previousPanel.getTabsImage().getSelectedIndex();
                        tevent = previousPanel.getTabsRegionEvents().getSelectedIndex();
                        ttransform = previousPanel.getTabsTransform().getSelectedIndex();
                    }
                    ActiveRegionPanel p = new ActiveRegionPanel(SketchletEditor.getProject(), region, t);
                    tabs.setComponentAt(selectedTabIndex, p);
                    if (SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() == null) {
                        SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().setSelectedRegions(new Vector<ActiveRegion>());
                    }
                    if (!SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().contains(region)) {
                        if (!(SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 1)) {
                            SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().setSelectedRegions(new Vector<ActiveRegion>());
                            SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().addToSelection(region);
                        }
                    }
                    SketchletEditor.getInstance().repaint();

                    if (previousPanel != null) {
                        subTabIndex = previousPanel.getTabs().getSelectedIndex();
                        subTabIndexProperties = previousPanel.getTabsTransform().getSelectedIndex();
                    }

                    if (subTabIndex >= 0 && subTabIndex < p.getTabs().getTabCount()) {
                        p.getTabs().setSelectedIndex(subTabIndex);
                    }
                    if (timage >= 0 && timage < p.getTabsImage().getTabCount()) {
                        p.getTabsImage().setSelectedIndex(timage);
                    }
                    if (tevent >= 0 && tevent < p.getTabsRegionEvents().getTabCount()) {
                        p.getTabsRegionEvents().setSelectedIndex(tevent);
                    }
                    if (ttransform >= 0 && ttransform < p.getTabsTransform().getTabCount()) {
                        p.getTabsTransform().setSelectedIndex(ttransform);
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
        SketchletEditor.getInstance().getCurrentPage().getRegions().getVariablesHelper().createNewVariables();
        SketchletEditor.getInstance().repaint();
    }

    public static void updateUIControls() {
        if (regionsAndActions != null && SketchletEditor.getInstance().getCurrentPage() != null && SketchletEditor.getProject() != null) {
            ActiveRegionPanel p = (ActiveRegionPanel) regionsAndActions.tabs.getSelectedComponent();

            if (p != null) {
                JTable tables[] = {p.getTableLimits(), p.getTableUpdateTransformations()};

                for (int it = 0; it < tables.length; it++) {
                    TableCellEditor tce = tables[it].getCellEditor();

                    if (tce != null) {
                        tce.stopCellEditing();
                    }
                }
            }
            if (p != null && p.getRegion() != null) {
                p.getImageUrlField().setSelectedItem(p.getImageUrlField().getEditor().getItem());
                p.updateUIControls();
                p.getImageIndex().setSelectedItem(p.getImageIndex().getEditor().getItem());
                p.getAnimationMs().setSelectedItem(p.getAnimationMs().getEditor().getItem());
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
            p.getTabs().setSelectedIndex(0);
            p.getTabsImage().setSelectedIndex(tabIndex);
        }
        SketchletEditor.getInstance().showExtraEditorPanel(ExtraEditorPanel.indexActiveRegion);
    }

    public static void showRegionsAndActions(int tabIndex) {
        if (tabIndex >= 0) {
            ActiveRegionPanel p = (ActiveRegionPanel) regionsAndActions.tabs.getSelectedComponent();
            p.getTabs().setSelectedIndex(tabIndex);
        }
        SketchletEditor.getInstance().showExtraEditorPanel(ExtraEditorPanel.indexActiveRegion);
    }

    public static void reload() {
        if (regionsAndActions != null && SketchletEditor.getInstance().getCurrentPage() != null && SketchletEditor.getProject() != null) {
            regionsAndActions.init();
            regionsAndActions.tabs.setSelectedIndex(regionsAndActions.tabs.getTabCount() - 1);
        }
    }

    public static void reload(ActiveRegion action) {
        if (action != null && regionsAndActions != null && SketchletEditor.getInstance().getCurrentPage() != null && SketchletEditor.getProject() != null) {
            ActiveRegionPanel p = (ActiveRegionPanel) regionsAndActions.tabs.getSelectedComponent();
            int s = 0;
            int ss = 0;
            JTabbedPane subTab = null;
            if (p != null) {
                s = p.getTabs().getSelectedIndex();
                if (s >= 0) {
                    String title = p.getTabs().getTitleAt(s).trim().toLowerCase();
                    if (title.contains("graphic")) {
                        subTab = p.getTabsImage();
                    } else if (title.contains("transform")) {
                        subTab = p.getTabsTransform();
                    } else if (title.contains("move")) {
                        subTab = p.getTabsMove();
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
                p.getTabs().setSelectedIndex(s);

                if (s >= 0 && p.getTabs().getTabCount() > s) {
                    String title = p.getTabs().getTitleAt(s);
                    if (title != null) {
                        title = title.trim().toLowerCase();
                        if (title.contains("graphic")) {
                            subTab = p.getTabsImage();
                        } else if (title.contains("transform")) {
                            subTab = p.getTabsTransform();
                        } else if (title.contains("move")) {
                            subTab = p.getTabsMove();
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

        if (action != null && regionsAndActions != null && SketchletEditor.getInstance().getCurrentPage() != null && SketchletEditor.getProject() != null) {
            if (regionsAndActions.tabs.getTabCount() != action.getParent().getRegions().size()) {
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
                    s = p.getTabs().getSelectedIndex();
                }
                timage = p.getTabsImage().getSelectedIndex();
                tevent = p.getTabsRegionEvents().getSelectedIndex();
                ttransform = p.getTabsTransform().getSelectedIndex();
            }
            int index = SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().indexOf(action);
            if (index < regionsAndActions.tabs.getTabCount()) {
                regionsAndActions.tabs.setSelectedIndex(index);
                if (regionsAndActions.tabs.getSelectedComponent() instanceof ActiveRegionPanel) {
                    p = (ActiveRegionPanel) regionsAndActions.tabs.getSelectedComponent();
                }
                if (p != null) {
                    p.getTabs().setSelectedIndex(s);
                    p.getTabsImage().setSelectedIndex(timage);
                    p.getTabsRegionEvents().setSelectedIndex(tevent);
                    p.getTabsTransform().setSelectedIndex(ttransform);
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

        if (action != null && regionsAndActions != null && SketchletEditor.getInstance().getCurrentPage() != null && SketchletEditor.getProject() != null) {
            if (regionsAndActions.tabs.getTabCount() != action.getParent().getRegions().size()) {
                regionsAndActions.init();
            }
            if (regionsAndActions.tabs.getSelectedComponent() instanceof ActiveRegionPanel) {
                p = (ActiveRegionPanel) regionsAndActions.tabs.getSelectedComponent();
            }
            int s = tabIndex;
            if (p != null) {
                if (s < 0) {
                    s = p.getTabs().getSelectedIndex();
                }
            }
            int index = SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().indexOf(action);
            if (index < regionsAndActions.tabs.getTabCount()) {
                regionsAndActions.tabs.setSelectedIndex(index);
                if (regionsAndActions.tabs.getSelectedComponent() instanceof ActiveRegionPanel) {
                    p = (ActiveRegionPanel) regionsAndActions.tabs.getSelectedComponent();
                }
                if (p != null) {
                    p.getTabs().setSelectedIndex(s);

                    if (tabIndex == ActiveRegionPanel.getIndexGraphics()) {
                        p.getTabsImage().setSelectedIndex(subIndex);
                    }
                    if (tabIndex == ActiveRegionPanel.getIndexEvents()) {
                        p.getTabsRegionEvents().setSelectedIndex(subIndex);
                    }
                    if (tabIndex == ActiveRegionPanel.getIndexTransform()) {
                        p.getTabsTransform().setSelectedIndex(subIndex);
                    }
                }
            }
        }

        return p;
    }

    public void init() {
        initializing = true;
        Page page = SketchletEditor.getInstance().getCurrentPage();
        ActiveRegionPanel p;
        if (tabs.getSelectedComponent() != null && tabs.getSelectedComponent() instanceof ActiveRegionPanel) {
            p = (ActiveRegionPanel) tabs.getSelectedComponent();
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

        if (page.getRegions().getMouseHelper().getSelectedRegions() != null && page.getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            int index = page.getRegions().getRegions().indexOf(page.getRegions().getMouseHelper().getSelectedRegions().lastElement());
            tabs.setSelectedIndex(index);
            int t = 0;
            if (previousPanel != null) {
                t = previousPanel.getTabs().getSelectedIndex();
            }
            p = new ActiveRegionPanel(SketchletEditor.getProject(), page.getRegions().getRegions().elementAt(index), t);
            tabs.setComponentAt(index, p);
        } else if (tabs.getTabCount() > 0) {
            tabs.setSelectedIndex(0);
            p = new ActiveRegionPanel(SketchletEditor.getProject(), page.getRegions().getRegions().elementAt(0), 0);
            tabs.setComponentAt(0, p);
        }

        ActiveRegionPanel.refreshRegionComboBox(page.getRegions().getRegions().size());
        initializing = false;
    }

    public static void main(String[] args) {
        ActiveRegionsExtraPanel f = new ActiveRegionsExtraPanel();
    }
}
