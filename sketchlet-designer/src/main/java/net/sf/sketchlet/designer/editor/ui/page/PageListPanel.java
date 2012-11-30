package net.sf.sketchlet.designer.editor.ui.page;

import net.sf.sketchlet.common.dnd.GenericTableTransferHandler;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.framework.model.log.ActivityLog;
import net.sf.sketchlet.designer.editor.ui.localvars.PageVariablesPanel;
import net.sf.sketchlet.designer.editor.ui.pagetransition.StateDiagram;
import net.sf.sketchlet.designer.editor.ui.profiles.Profiles;
import net.sf.sketchlet.designer.editor.ui.wizard.WizActiveRegionEvent;
import net.sf.sketchlet.help.HelpUtils;
import net.sf.sketchlet.framework.model.Page;
import net.sf.sketchlet.framework.model.Pages;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author zobrenovic
 */
public class PageListPanel extends JPanel {

    public JTable table;
    JScrollPane scrollPane;
    public AbstractTableModel model;
    //JButton newSketch = new JButton(Workspace.createImageIcon("resources/new.gif", ""));
    JButton newSketch = new JButton(Workspace.createImageIcon("resources/new.gif", ""));
    JButton up = new JButton(Workspace.createImageIcon("resources/go-up.png", ""));
    JButton down = new JButton(Workspace.createImageIcon("resources/go-down.png", ""));
    JButton delete = new JButton(Workspace.createImageIcon("resources/user-trash.png", ""));
    JButton states;
    JCheckBox drag = new JCheckBox(Language.translate("drag    "), false);
    JButton help = new JButton(Workspace.createImageIcon("resources/help-browser.png"));
    public JToolBar pageToolbar;
    public JToolBar pageOptionsToolbar;
    JPanel pageOptionsPanel = new JPanel(new BorderLayout());
    public JButton showActions = new JButton(Workspace.createImageIcon("resources/actions_menu.png"));
    public JButton showProperties = new JButton(Workspace.createImageIcon("resources/details.gif"));
    public JButton showSpreadsheet = new JButton(Workspace.createImageIcon("resources/spreadsheet-icon.png"));
    JMenuItem entryActions = new JMenuItem(Language.translate("On Page Entry"), Workspace.createImageIcon("resources/import.gif"));
    JMenuItem exitActions = new JMenuItem(Language.translate("On Page Exit"), Workspace.createImageIcon("resources/export.gif"));
    JMenuItem variablesActions = new JMenuItem(Language.translate("On Variable Update"), Workspace.createImageIcon("resources/variables.png"));
    JMenuItem keyboardActions = new JMenuItem(Language.translate("On Keyboard Events"), Workspace.createImageIcon("resources/keyboard.png"));
    JMenuItem mouseActions = new JMenuItem(Language.translate("On Mouse Events"), Workspace.createImageIcon("resources/mouse.png"));
    public JButton wizard = new JButton("", Workspace.createImageIcon("resources/wizard.png"));
    public JMenuItem sketchEntry = new JMenuItem(Language.translate("on page entry wizard"), Workspace.createImageIcon("resources/actions.png"));
    public JMenuItem sketchExit = new JMenuItem(Language.translate("on page exit wizard"), Workspace.createImageIcon("resources/actions.png"));
    public JMenuItem sketchVariableUpdate = new JMenuItem(Language.translate("on variable update wizard"), Workspace.createImageIcon("resources/variable.png"));
    public JMenuItem sketchKeyboard = new JMenuItem(Language.translate("on keyboard event wizard"), Workspace.createImageIcon("resources/keyboard.gif"));
    public JMenuItem sketchMouse = new JMenuItem(Language.translate("on mouse event wizard"), Workspace.createImageIcon("resources/mouse.gif"));
    JMenu menuSketch;

    public PageListPanel() {
        setLayout(new BorderLayout());
        model = getTableModel();
        table = new JTable(model);
        table.putClientProperty("JComponent.sizeVariant", "small");
        table.setTransferHandler(new GenericTableTransferHandler("@sketch ", 1));
        table.setDragEnabled(true);
        table.setTableHeader(null);

        // Disable auto resizing
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        TableColumn col = table.getColumnModel().getColumn(0);
        int width = 20;
        col.setWidth(width);
        col.setMaxWidth(width);
        col.setPreferredWidth(width);

        newSketch.setToolTipText(Language.translate("Create a new page (Ctrl+N)"));
        newSketch.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.getInstance().newSketch();
                ActivityLog.log("newSketch", "", "new.gif", newSketch);
            }
        });
        up.setToolTipText(Language.translate("Moves the page up in the list"));
        up.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.getInstance().moveLeft();
                int s = table.getSelectedRow() - 1;
                refreshDesktop();
                model.fireTableDataChanged();
                table.getSelectionModel().setSelectionInterval(s, s);
            }
        });
        down.setToolTipText(Language.translate("Moves the page down in the list"));
        down.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.getInstance().moveRight();
                int s = table.getSelectedRow() + 1;
                refreshDesktop();
                model.fireTableDataChanged();
                table.getSelectionModel().setSelectionInterval(s, s);
            }
        });
        delete.setToolTipText(Language.translate("Delete the page"));
        delete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.getInstance().delete();
                refreshDesktop();
            }
        });

        showSpreadsheet.setText("");
        showSpreadsheet.setToolTipText(Language.translate("Show page data spreadsheets"));
        //showPerspective.setMnemonic(KeyEvent.VK_P);
        showSpreadsheet.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.getInstance().showSpreadsheetPanel();
                ActivityLog.log("showSpreadsheet", SketchletEditor.getInstance().isPagePanelShown() + "", "spreadsheet-icon.png", showSpreadsheet);
            }
        });

        showActions.setText("");
        showActions.setToolTipText(Language.translate("Show page events"));
        this.wizard.setToolTipText(Language.translate("Wizards"));
        //showActions.setMnemonic(KeyEvent.VK_P);
        showActions.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //FreeHand.editorPanel.showStatePanel(PageDetailsPanel.actionsTabIndex, PageDetailsPanel.actionsOnEntrySubtabIndex);
                JPopupMenu popup = new JPopupMenu();
                popup.add(entryActions);
                popup.add(exitActions);
                popup.addSeparator();
                popup.add(variablesActions);
                popup.addSeparator();
                popup.add(keyboardActions);
                popup.add(mouseActions);
                popup.show(showActions, 0, showActions.getHeight());
                ActivityLog.log("showActions", SketchletEditor.getInstance().isPagePanelShown() + "", "actions.png", showActions);
            }
        });
        entryActions.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().showStatePanel(PageDetailsPanel.actionsTabIndex, PageDetailsPanel.actionsOnEntrySubtabIndex);
            }
        });
        exitActions.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().showStatePanel(PageDetailsPanel.actionsTabIndex, PageDetailsPanel.actionsOnExitSubtabIndex);
            }
        });
        variablesActions.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().showStatePanel(PageDetailsPanel.actionsTabIndex, PageDetailsPanel.actionsVariablesSubtabIndex);
            }
        });
        keyboardActions.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().showStatePanel(PageDetailsPanel.actionsTabIndex, PageDetailsPanel.actionsKeyboardSubtabIndex);
            }
        });
        mouseActions.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().showStatePanel(PageDetailsPanel.actionsTabIndex, PageDetailsPanel.actionsMouseSubtabIndex);
            }
        });
        wizard.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().getHelpViewer().showAutoHelpByID("sketchify_editor_toolbar");
                JPopupMenu popupMenu = new JPopupMenu();

                menuSketch = new JMenu(Language.translate("wizards for sketch events"));

                popupMenu.add(sketchEntry);
                popupMenu.add(sketchExit);
                popupMenu.add(sketchVariableUpdate);
                popupMenu.add(sketchKeyboard);
                popupMenu.add(sketchMouse);

                popupMenu.show(wizard.getParent(), wizard.getX(), wizard.getY() + wizard.getHeight());
            }
        });

        sketchEntry.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                WizActiveRegionEvent.showWizard(3, Language.translate("Page Entry Action Wizard"));
            }
        });

        sketchExit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                WizActiveRegionEvent.showWizard(4, Language.translate("Page Exit Action Wizard"));
            }
        });

        sketchVariableUpdate.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                WizActiveRegionEvent.showWizard(5, Language.translate("Page Variable Update Action Wizard"));
            }
        });

        sketchKeyboard.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                WizActiveRegionEvent.showWizard(6, Language.translate("Page Keyboard Action Wizard"));
            }
        });
        sketchMouse.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                WizActiveRegionEvent.showWizard(6, Language.translate("Page Mouse Action Wizard"));
            }
        });


        showProperties.setText("");
        showProperties.setToolTipText(Language.translate("Show page parameters"));
        //showProperties.setMnemonic(KeyEvent.VK_P);
        showProperties.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.getInstance().showStatePanel(PageDetailsPanel.propertiesTabIndex, 0);
                ActivityLog.log("showPageProperties", SketchletEditor.getInstance().isPagePanelShown() + "", "details.gif", showProperties);
            }
        });

        setPageTitle(SketchletEditor.getInstance().getCurrentPage().getTitle());

        JToolBar fileControlsTB = new JToolBar();
        fileControlsTB.setFloatable(false);
        fileControlsTB.setLayout(new BorderLayout());
        pageToolbar = new JToolBar();
        FlowLayout fl = new FlowLayout(FlowLayout.LEFT, 0, 0);
        pageToolbar.setLayout(fl);
        fileControlsTB.add(pageToolbar);
        fileControlsTB.add(help, BorderLayout.EAST);
        pageToolbar.setFloatable(false);
        pageToolbar.setBorder(BorderFactory.createEmptyBorder());

        pageOptionsToolbar = new JToolBar();
        FlowLayout fl2 = new FlowLayout(FlowLayout.LEADING, 0, 0);
        // pageOptionsToolbar.setPreferredSize(new Dimension(30, 300));
        pageOptionsToolbar.setLayout(fl2);
        pageOptionsToolbar.setFloatable(false);
        pageOptionsToolbar.setOrientation(JToolBar.VERTICAL);

        // pageToolbar.add(newSketch);
        final JButton sketchProperties = new JButton("", Workspace.createImageIcon("resources/details.gif"));
        sketchProperties.setMnemonic(KeyEvent.VK_P);
        sketchProperties.setToolTipText(Language.translate("Page parameters"));
        sketchProperties.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                PageDetailsPanel.showStateProperties(PageDetailsPanel.propertiesTabIndex, 0);
            }
        });
        sketchProperties.setMargin(new Insets(2, 2, 2, 2));
        states = new JButton(Workspace.createImageIcon("resources/states.png", ""));
        states.setToolTipText(Language.translate("Shows state transition diagram (transitions among sketches)"));
        states.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                StateDiagram.showDiagram(SketchletEditor.getInstance().getPages());
            }
        });

        this.reloadToolbar();

        help.setToolTipText(Language.translate("What is this parameters bar?"));
        help.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                HelpUtils.openHelpFile("", "sketches");
            }
        });

        SwingUtilities.updateComponentTreeUI(table);
        scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setTabPlacement(JTabbedPane.BOTTOM);

        tabs.add("Table", scrollPane);
        tabs.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(tabs);

        ListSelectionModel rowSM = table.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() || drag.isSelected()) {
                    return;
                }
                SketchletEditor.getInstance().save();
                int row = table.getSelectedRow();
                if (row >= 0) {
                    SketchletEditor.setTransparencyFactor(0.25);
                    SketchletEditor.getInstance().repaint();

                    try {
                        Thread.sleep(100);
                    } catch (Exception e6) {
                    }

                    SketchletEditor.getInstance().openSketchByIndex(row);

                    new Thread(new Runnable() {

                        public void run() {
                            try {
                                while (Pages.getMessageFrame() != null) {
                                    Thread.sleep(1);
                                }
                                for (double t = 0.1; t < 1.0; t += 0.1) {
                                    SketchletEditor.setTransparencyFactor(t);
                                    SketchletEditor.getInstance().repaint();
                                    Thread.sleep(10);
                                }
                                SketchletEditor.setTransparencyFactor(1.0);
                                SketchletEditor.getInstance().repaint();
                            } catch (Exception e65) {
                            }
                        }
                    }).start();
                    //desktopPanel.selectedPageIndex = row;
                    //desktopPanel.selectedSketch = SketchletEditor.editorPanel.currentSketch;
                }
            }
        });

        drag.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                //desktopPanel.enableSingleClickChange(!drag.isSelected());
            }
        });
        table.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent me) {
                int row = table.getSelectedRow();
                if (me.getClickCount() == 2 && drag.isSelected()) {
                    if (row >= 0) {
                        SketchletEditor.getInstance().openSketchByIndex(row);
                        //desktopPanel.selectedPageIndex = row;
                        //desktopPanel.selectedSketch = SketchletEditor.editorPanel.currentSketch;
                    }
                }

                if ((me.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                    if (row >= 0 && !drag.isSelected()) {
                        SketchletEditor.getInstance().openSketchByIndex(row);
                        //desktopPanel.selectedPageIndex = row;
                        // desktopPanel.selectedSketch = SketchletEditor.editorPanel.currentSketch;
                    }
                    showPopupMenu(SketchletEditor.getInstance().getCurrentPage(), row, me.getX(), me.getY() + 30);
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout());

        panel.add(scrollPane);
        panel.add(fileControlsTB, BorderLayout.NORTH);

        pageOptionsPanel.add(pageOptionsToolbar);
        panel.add(pageOptionsPanel, BorderLayout.SOUTH);

        SketchletEditor.getInstance().setPageVariablesPanel(new PageVariablesPanel());
        //add(SketchletEditor.editorPanel.pageVariablesPanel, BorderLayout.SOUTH);
        if (Profiles.isActive("variables")) {
            JSplitPane splitPanelVariables = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, SketchletEditor.getInstance().getPageVariablesPanel(), Workspace.getMainPanel().getSketchletPanel().globalVariablesPanel);
            splitPanelVariables.setDividerLocation(150);
            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, panel, splitPanelVariables);
            splitPane.setDividerLocation(400);
            add(splitPane);
        } else {
            add(panel);
        }
    }

    ImageIcon icon = Workspace.createImageIcon("resources/desktop-icon-16.png");

    public void setPageTitle(String title) {
        pageOptionsPanel.setBorder(BorderFactory.createTitledBorder(title));
    }

    public void reloadToolbar() {
        pageToolbar.removeAll();
        pageOptionsToolbar.removeAll();
        pageToolbar.add(newSketch);
        if (Profiles.isActive("page_properties,toolbar_page_properties")) {
            pageOptionsToolbar.add(showProperties);
        }
        if (Profiles.isActive("page_actions,toolbar_page_actions")) {
            pageOptionsToolbar.add(showActions);
        }
        if (Profiles.isActive("page_spreadsheet,toolbar_page_spreadsheet")) {
            pageOptionsToolbar.add(showSpreadsheet);
        }
        if (Profiles.isActive("pages_move_up")) {
            pageToolbar.add(up);
        }
        if (Profiles.isActive("pages_move_down")) {
            pageToolbar.add(down);
        }
        pageToolbar.add(delete);
        if (Profiles.isActive("active_regions_layer")) {
            pageToolbar.add(drag);
        }
        if (Profiles.isActive("pages_diagram") && Profiles.isActive("active_regions_layer")) {
            pageOptionsToolbar.add(states);
        }
        if (Profiles.isActive("toolbar_wizards")) {
            // pageOptionsToolbar.add(wizard);
        }

        pageToolbar.revalidate();
    }

    public void refreshDesktop() {
        /*desktopPanel.refresh();
        desktopPanel.selectedSketch = SketchletEditor.editorPanel.currentSketch;
        desktopPanel.selectedPageIndex = SketchletEditor.sketches.sketches.indexOf(SketchletEditor.editorPanel.currentSketch);
        desktopPanel.repaint();*/
    }

    public AbstractTableModel getTableModel() {
        return new AbstractTableModel() {

            public int getColumnCount() {
                return 2;
            }

            public String getColumnName(int col) {
                return "";
            }

            public int getRowCount() {
                return SketchletEditor.getPages() == null ? 0 : SketchletEditor.getPages().getPages().size();
            }

            public Object getValueAt(int row, int col) {
                Page s = SketchletEditor.getPages().getPages().elementAt(row);
                switch (col) {
                    case 0:
                        return icon;
                    case 1:
                        return s.getTitle();
                }
                return "";
            }

            public void setValueAt(Object value, int row, int col) {
                Page s = SketchletEditor.getPages().getPages().elementAt(row);
                switch (col) {
                    case 1:
                        String oldName = s.getTitle();
                        String newName = (String) value;
                        s.setTitle(newName);
                        //FreeHand.editorPanel.sketchToolbar.title.setText(newName);
                        SketchletEditor.editorFrame.setTitle(newName);
                        SketchletEditor.getInstance().getPageListPanel().setPageTitle(SketchletEditor.getInstance().getCurrentPage().getTitle());
                        SketchletEditor.getPages().replaceReferencesSketches(oldName, newName);

                        s.save(false);
                        break;
                }

            }

            public Class getColumnClass(int col) {
                return col == 0 ? ImageIcon.class : String.class;
            }

            public boolean isCellEditable(int row, int col) {
                return !drag.isSelected() && col == 1;
            }
        };
    }

    public void showPopupMenu(final Page s, final int selectedIndex, int x, int y) {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem renameMenuItem = new JMenuItem(Language.translate("Rename..."));
        renameMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (SketchletEditor.getInstance().getCurrentPage() != null) {
                    String newName = JOptionPane.showInputDialog(SketchletEditor.editorFrame, Language.translate("New name"), SketchletEditor.getInstance().getCurrentPage().getTitle());
                    if (newName != null) {
                        String oldName = SketchletEditor.getInstance().getCurrentPage().getTitle();
                        SketchletEditor.getInstance().getCurrentPage().setTitle(newName);
                        SketchletEditor.editorFrame.setTitle(newName);
                        SketchletEditor.getPages().replaceReferencesSketches(oldName, newName);

                        SketchletEditor.getInstance().getCurrentPage().save(false);
                        repaint();
                    }
                }
            }
        });
        popupMenu.add(renameMenuItem);
        JMenuItem sketchMenuItem = new JMenuItem("Delete");
        sketchMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (SketchletEditor.getInstance() != null) {
                    SketchletEditor.getInstance().openSketchByIndex(selectedIndex);
                    SketchletEditor.getInstance().delete(SketchletEditor.editorFrame);
                } else {
                    Object[] options = {Language.translate("Delete"), Language.translate("Cancel")};
                    int n = JOptionPane.showOptionDialog(SketchletEditor.editorFrame,
                            Language.translate("You are about to delete '") + s.getTitle() + "'",
                            Language.translate("Delete Sketch Confirmation"),
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            options,
                            options[0]);

                    if (n != 0) {
                        return;
                    }

                    int i = SketchletEditor.getPages().getPages().indexOf(s);
                    s.delete();

                    SketchletEditor.getPages().getPages().remove(s);
                }
            }
        });
        popupMenu.add(sketchMenuItem);

        popupMenu.show(this, x, y);
    }
}
