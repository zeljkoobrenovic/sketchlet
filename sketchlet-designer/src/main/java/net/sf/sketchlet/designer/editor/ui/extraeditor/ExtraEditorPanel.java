/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.ui.extraeditor;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.page.PageDetailsPanel;
import net.sf.sketchlet.designer.editor.ui.profiles.Profiles;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author zobrenovic
 */
public class ExtraEditorPanel extends JPanel {

    public JTabbedPane tabs = new JTabbedPane();
    public TimersExtraPanel timersExtraPanel = new TimersExtraPanel();
    public MacrosExtraPanel macrosExtraPanel = new MacrosExtraPanel();
    public DerivedVariablesExtraPanel derivedVariablesExtraPanel = new DerivedVariablesExtraPanel();
    public ScriptEditorExtraPanel scriptEditorExtraPanel = new ScriptEditorExtraPanel();
    public ActiveRegionsExtraPanel activeRegionsExtraPanel = new ActiveRegionsExtraPanel();
    public int height = 180;
    public JButton maximize = new JButton(Workspace.createImageIcon("resources/maximize_16.png"));
    public JButton minimize = new JButton(Workspace.createImageIcon("resources/minimize_16.png"));
    JButton save = new JButton(Workspace.createImageIcon("resources/save_16.png"));

    public ExtraEditorPanel() {
        setLayout(new BorderLayout());
        createGUI();
    }

    public void onHide() {
        TimersExtraPanel.onHide();
        SketchletEditor.getInstance().getExtraEditorPanel().macrosExtraPanel.onHide();
        save();
    }

    public void save() {
        SketchletEditor.getInstance().getExtraEditorPanel().derivedVariablesExtraPanel.save();
        SketchletEditor.getInstance().getExtraEditorPanel().scriptEditorExtraPanel.save();
        SketchletEditor.getInstance().getExtraEditorPanel().activeRegionsExtraPanel.save();
    }

    public Dimension getPreferredSize() {
        return new Dimension(1000, height);
    }

    public Dimension getMinimumSize() {
        return new Dimension(0, 0);
    }

    public static int indexTimer = 0;
    public static int indexMacro = 1;
    public static int indexDerivedVariables = 2;
    public static int indexScript = 3;
    public static int indexActiveRegion = 4;
    public static int indexPage = 5;

    public void createTabs() {
        tabs.removeAll();
        tabs.setTabPlacement(JTabbedPane.LEFT);
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        if (Profiles.isActive("timers")) {
            indexTimer = tabs.getTabCount();
            tabs.add("", timersExtraPanel);
            tabs.setIconAt(indexTimer, Workspace.createImageIcon("resources/timer.png"));
            tabs.setToolTipTextAt(indexTimer, Language.translate("Timers"));
        }

        if (Profiles.isActive("macros")) {
            indexMacro = tabs.getTabCount();
            tabs.add("", macrosExtraPanel);
            tabs.setIconAt(indexMacro, Workspace.createImageIcon("resources/macros.png"));
            tabs.setToolTipTextAt(indexMacro, Language.translate("Action Lists"));
        }

        if (Profiles.isActive("derived_variables")) {
            indexDerivedVariables = tabs.getTabCount();
            tabs.add("", derivedVariablesExtraPanel);
            tabs.setIconAt(indexDerivedVariables, Workspace.createImageIcon("resources/formula.png"));
            tabs.setToolTipTextAt(indexDerivedVariables, Language.translate("Derived Variables (formulas, aggregate, serialize....)"));
        }

        if (Profiles.isActive("derived_variables")) {
            indexScript = tabs.getTabCount();
            tabs.add("", scriptEditorExtraPanel);
            tabs.setToolTipTextAt(indexScript, Language.translate("Scripts"));
            tabs.setIconAt(indexScript, Workspace.createImageIcon("resources/script.png"));
        }
        if (Profiles.isActive("active_regions_layer")) {
            indexActiveRegion = tabs.getTabCount();
            tabs.add("", activeRegionsExtraPanel);
            tabs.setIconAt(indexActiveRegion, Workspace.createImageIcon("resources/active_region.png"));
            tabs.setToolTipTextAt(indexActiveRegion, Language.translate("Active Regions"));
        }
        if (Profiles.isActiveAny("page_actions,page_properties,page_perspective,page_spreadsheet")) {
            indexPage = tabs.getTabCount();
            SketchletEditor.getInstance().getPageDetailsPanel().load();
            tabs.add("", SketchletEditor.getInstance().getPageDetailsPanel());
            tabs.setIconAt(indexPage, Workspace.createImageIcon("resources/page.png"));
            tabs.setToolTipTextAt(indexPage, Language.translate("Page Objects"));
        }
    }

    public void refreshPageComponents() {
        SketchletEditor.getInstance().getPageDetailsPanel().refreshComponenets();
        SketchletEditor.getInstance().getSpreadsheetPanel().refreshComponenets();
    }

    public void createGUI() {
        createTabs();
        JPanel rightPanel = new JPanel(new BorderLayout());
        JButton btnClose = new JButton(Workspace.createImageIcon("resources/close_small.png"));
        btnClose.setToolTipText(Language.translate("Close"));
        btnClose.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                SketchletEditor.getInstance().hideExtraEditorPanel();
            }
        });

        final JButton btnUndock = new JButton(Workspace.createImageIcon("resources/undock.png"));
        btnUndock.setToolTipText(Language.translate("Undock"));
        btnUndock.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (btnUndock.getToolTipText().equalsIgnoreCase("undock")) {
                    SketchletEditor.getInstance().undockExtraEditorPanel();
                    btnUndock.setToolTipText(Language.translate("dock"));
                    btnUndock.setIcon(Workspace.createImageIcon("resources/dock.png"));
                } else {
                    SketchletEditor.getInstance().dockExtraEditorPanel();
                    btnUndock.setToolTipText(Language.translate("undock"));
                    btnUndock.setIcon(Workspace.createImageIcon("resources/undock.png"));
                }
            }
        });

        tabs.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                openRelevantHelpPage(false);
            }
        });

        JToolBar tbRight = new JToolBar();
        tbRight.setFloatable(false);
        tbRight.setOrientation(JToolBar.VERTICAL);
        tbRight.add(btnClose);
        tbRight.add(btnUndock);
        rightPanel.add(tbRight, BorderLayout.NORTH);
        final JButton help = new JButton("", Workspace.createImageIcon("resources/help-browser.png", ""));

        help.addActionListener(
                new ActionListener() {

                    public void actionPerformed(ActionEvent event) {
                        openRelevantHelpPage(true);
                    }
                });
        help.setToolTipText(Language.translate("What is this?"));
        help.setMargin(new Insets(0, 0, 0, 0));
        tbRight.add(help);
        maximize.setToolTipText(Language.translate("Make panel bigger"));
        maximize.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                height += 120;
                minimize.setEnabled(true);
                revalidate();
                repaint();
            }
        });
        minimize.setToolTipText(Language.translate("Make panel small"));
        minimize.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                minimize.setEnabled(false);
                height = 180;
                revalidate();
                repaint();
            }
        });
        save.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                save();
            }
        });
        tbRight.addSeparator();
        add(rightPanel, BorderLayout.EAST);

        add(tabs);

        revalidate();
    }

    public void setBiggerSize() {
        height = 320;
        minimize.setEnabled(true);

    }

    public void openRelevantHelpPage(boolean bForceShow) {
        int selectedTabIndex = tabs.getSelectedIndex();
        if (selectedTabIndex < 0 || tabs.getToolTipTextAt(selectedTabIndex) == null) {
            return;
        }

        int index = tabs.getSelectedIndex();
        /*if (tip.contains("region")) {
        setBiggerSize();
        revalidate();
        }*/
        if (index == indexTimer) {
            SketchletEditor.getInstance().getHelpViewer().showHelpByID("timers", bForceShow);
        } else if (index == indexMacro) {
            SketchletEditor.getInstance().getHelpViewer().showHelpByID("macros", bForceShow);
        } else if (index == indexDerivedVariables) {
            /*int derIndex = derivedVariablesExtraPanel.tabs.getSelectedIndex();
            if (derIndex >= 0) {
            if (derIndex == 0) {
            SketchletEditor.editorPanel.helpViewer.showHelpByID("derived_variables_formulas", bForceShow);
            } else if (derIndex == 1) {
            SketchletEditor.editorPanel.helpViewer.showHelpByID("derived_variables_interpolator", bForceShow);
            } else if (derIndex == 3) {
            SketchletEditor.editorPanel.helpViewer.showHelpByID("derived_variables_aggregate", bForceShow);
            } else if (derIndex == 2) {
            SketchletEditor.editorPanel.helpViewer.showHelpByID("derived_variables_serialize", bForceShow);
            }
            }*/
            SketchletEditor.getInstance().getHelpViewer().showHelpByID("derived_variables", bForceShow);
        } else if (index == indexScript) {
            SketchletEditor.getInstance().getHelpViewer().showHelpByID("scripts", bForceShow);
        } else if (index == indexActiveRegion) {
            if (activeRegionsExtraPanel.tabs.getSelectedComponent() instanceof ActiveRegionPanel) {
                ActiveRegionPanel p = (ActiveRegionPanel) activeRegionsExtraPanel.tabs.getSelectedComponent();

                if (p != null) {
                    int n = p.tabs.getSelectedIndex();
                    if (n >= 0) {
                        if (n == ActiveRegionPanel.indexGraphics) {
                            SketchletEditor.getInstance().getHelpViewer().showHelpByID("active_region_image", bForceShow);
                        } else if (n == ActiveRegionPanel.indexTransform) {
                            SketchletEditor.getInstance().getHelpViewer().showHelpByID("active_region_transform", bForceShow);
                        } else if (n == ActiveRegionPanel.indexEvents) {
                            int n2 = p.tabsRegionEvents.getSelectedIndex();
                            if (n2 == ActiveRegionPanel.indexMotion) {
                                SketchletEditor.getInstance().getHelpViewer().showHelpByID("active_region_move", bForceShow);
                            } else if (n2 == ActiveRegionPanel.indexMouseEvents) {
                                SketchletEditor.getInstance().getHelpViewer().showHelpByID("active_region_mouse", bForceShow);
                            } else if (n2 == ActiveRegionPanel.indexKeyboardEvents) {
                                SketchletEditor.getInstance().getHelpViewer().showHelpByID("active_region_keyboard", bForceShow);
                            } else if (n2 == ActiveRegionPanel.indexOverlap) {
                                SketchletEditor.getInstance().getHelpViewer().showHelpByID("active_region_interaction", bForceShow);
                            }
                        } else if (n == ActiveRegionPanel.indexWidget) {
                            SketchletEditor.getInstance().getHelpViewer().showHelpByID("active_region_widget", bForceShow);
                        } else if (n == ActiveRegionPanel.indexGeneral) {
                            SketchletEditor.getInstance().getHelpViewer().showHelpByID("active_region_general", bForceShow);
                        }
                    }
                }
            }
        } else if (index == indexPage) {
            int pageIndex = SketchletEditor.getInstance().getPageDetailsPanel().tabs.getSelectedIndex();
            if (pageIndex > 0) {
                if (pageIndex == PageDetailsPanel.actionsTabIndex) {
                    SketchletEditor.getInstance().getHelpViewer().showHelpByID("sketchlet_actions", bForceShow);
                } else if (pageIndex == PageDetailsPanel.propertiesTabIndex) {
                    SketchletEditor.getInstance().getHelpViewer().showHelpByID("sketchlet_properties", bForceShow);
                } else if (pageIndex == PageDetailsPanel.perspectiveTabIndex) {
                    SketchletEditor.getInstance().getHelpViewer().showHelpByID("sketchlet_perspective", bForceShow);
                } else if (pageIndex == PageDetailsPanel.spreadsheetTabIndex) {
                    SketchletEditor.getInstance().getHelpViewer().showHelpByID("sketchlet_spreadsheet", bForceShow);
                }
            }
        }
    }
}