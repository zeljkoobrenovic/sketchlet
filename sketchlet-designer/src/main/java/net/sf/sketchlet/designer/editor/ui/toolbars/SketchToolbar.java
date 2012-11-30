package net.sf.sketchlet.designer.editor.ui.toolbars;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.GlobalProperties;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.SketchletEditorMode;
import net.sf.sketchlet.designer.eye.eye.EyeFrame;
import net.sf.sketchlet.framework.model.log.ActivityLog;
import net.sf.sketchlet.designer.playback.ui.InteractionSpaceFrame;
import net.sf.sketchlet.designer.editor.ui.profiles.Profiles;
import net.sf.sketchlet.framework.model.programming.macros.Macros;
import net.sf.sketchlet.framework.model.programming.timers.Timers;
import net.sf.sketchlet.util.RefreshTime;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author zobrenovic
 */
public class SketchToolbar extends JToolBar {
    private static final Logger log = Logger.getLogger(SketchToolbar.class);
    public JButton newSketch = new JButton(Workspace.createImageIcon("resources/new.gif", ""));
    public JButton save = new JButton(Workspace.createImageIcon("resources/save.gif", ""));
    public JButton undo = new JButton(Workspace.createImageIcon("resources/edit-undo.png"));
    // JButton redo = new JButton(Workspace.createImageIcon("resources/edit-redo.png"));
    public JButton clipboard = new JButton(Workspace.createImageIcon("resources/edit-paste.png"));
    public JButton pasteSpecial = new JButton("paste region(s)", Workspace.createImageIcon("resources/edit-paste.png"));
    public JButton play = new JButton(Workspace.createImageIcon("resources/start.gif", ""));
    public JButton space = new JButton(Workspace.createImageIcon("resources/interaction_space.png", ""));
    public JButton navigator = new JButton(Workspace.createImageIcon("resources/tab.png", ""));
    //public JMenuItem moveLeft = new JMenuItem(Workspace.createImageIcon("resources/go-previous.png"));
    //public JMenuItem moveRight = new JMenuItem(Workspace.createImageIcon("resources/go-next.png"));
    // public JButton spreadsheet = new JButton(Workspace.createImageIcon("resources/spreadsheet.png", ""));
    public JButton snapGrid = new JButton(Workspace.createImageIcon("resources/snap-grid.png"));
    public JButton showRulers = new JButton(Workspace.createImageIcon("resources/rulers.png"));
    // public JMenuItem mouseWizard = new JMenuItem(Language.translate("region mouse event wizard"), Workspace.createImageIcon("resources/mouse.png"));
    //public JMenuItem interactionWizard = new JMenuItem(Language.translate("region interaction wizard"), Workspace.createImageIcon("resources/interaction.png"));
    //public JMenuItem animationWizard = new JMenuItem(Language.translate("region animation wizard"), Workspace.createImageIcon("resources/timer.png"));
    public JToggleButton visualizeVariables = new JToggleButton(Workspace.createImageIcon("resources/variable.png"));
    public JToggleButton visualizeInfo = new JToggleButton(Workspace.createImageIcon("resources/info.gif"));
    public JToggleButton visualizeInfoPage = new JToggleButton(Workspace.createImageIcon("resources/info-page.gif"));
    public JButton visualizeEye = new JButton(Workspace.createImageIcon("resources/eye.gif"));
    public JButton zoomWin = new JButton(Workspace.createImageIcon("resources/zoom_window.png"));
    public JButton showPerspective = new JButton(Workspace.createImageIcon("resources/perspective_lines.png"));
    public JButton bigger = new JButton(Workspace.createImageIcon("resources/zoomin.gif"));
    public JButton smaller = new JButton(Workspace.createImageIcon("resources/zoomout.gif"));
    public JButton postIt = new JButton(Workspace.createImageIcon("resources/postit.png"));
    public JButton help = new JButton(Workspace.createImageIcon("resources/help-browser2.png"));
    public int zoomOptions[] = {3200, 2400, 1600, 1200, 800, 700, 600, 500, 400, 300, 200, 150, 140, 130, 120, 110, 100, 90, 80, 70, 66, 50, 33, 25, 16, 12, 8, 7, 6, 5, 4, 3, 2, 1};
    public JComboBox zoomBox = new JComboBox();
    public static boolean animateZoom = true;
    // public JTextField title = new JTextField(10);
    //JMenu menuRegion;
    public static boolean bVisualizeVariables = false;
    public static boolean bVisualizeInfoSketch = false;
    public static boolean bVisualizeInfoRegions = false;
    public static boolean bVisualizeInfoVariables = false;

    public SketchToolbar() {
        createButtons();
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (int i = 0; i < this.getComponentCount(); i++) {
            Component c = this.getComponent(i);
            if (c != this.zoomBox && c != this.bigger && c != this.smaller && c != this.navigator && c != this.help
                    && c != this.visualizeInfo && c != this.visualizeInfoPage && c != this.visualizeVariables) {
                c.setEnabled(enabled);
            }
        }
    }

    public void createButtons() {
        undo.setEnabled(false);
        // redo.setEnabled(false);
        final JButton copy = new JButton(Workspace.createImageIcon("resources/edit-copy.png"));
        //pasteSpecial.setMnemonic(KeyEvent.VK_R);
        // pasteSpecial.setEnabled(false);
        final JButton more = new JButton(Workspace.createImageIcon("resources/preferences-system.png", ""));
        more.setToolTipText("More commands");
        more.setText("more...");
        //more.setMnemonic(KeyEvent.VK_M);

        this.visualizeVariables.setToolTipText(Language.translate("Graphically shows relations among variables regions, timers and amcros"));
        this.visualizeInfo.setToolTipText(Language.translate("Show detailed textual information about regions"));
        this.visualizeInfoPage.setToolTipText(Language.translate("Show detailed textual information about the page"));
        this.visualizeEye.setToolTipText(Language.translate("Show relations among variables, sketches and regions, timers and macros"));
        this.zoomWin.setToolTipText(Language.translate("Zoom to window"));
        showPerspective.setText("");
        showPerspective.setToolTipText(Language.translate("Show page perspective"));
        this.bigger.setToolTipText(Language.translate("Zoom in"));
        this.smaller.setToolTipText(Language.translate("Zoom out"));
        this.postIt.setToolTipText(Language.translate("Put a note on the page"));

        final JMenuItem duplicate = new JMenuItem(Language.translate("duplicate sketch"), Workspace.createImageIcon("resources/edit-copy.png"));
        //duplicate.setMnemonic(KeyEvent.VK_D);
        //duplicate.setAccelerator(KeyStroke.getKeyStroke(
//                KeyEvent.VK_D, ActionEvent.CTRL_MASK));
        final JMenuItem delete = new JMenuItem(Workspace.createImageIcon("resources/user-trash.png"));
        final JMenuItem resize = new JMenuItem(Workspace.createImageIcon("resources/resize.png"));
        final JMenuItem history = new JMenuItem("history", Workspace.createImageIcon("resources/history.gif"));
        //history.setMnemonic(KeyEvent.VK_H);
        /*history.setAccelerator(KeyStroke.getKeyStroke(
        KeyEvent.VK_H, ActionEvent.CTRL_MASK));*/

        final JMenuItem setAsMaster = new JMenuItem(Language.translate("set as master sketch"), Workspace.createImageIcon("resources/master.png"));
        //setAsMaster.setMnemonic(KeyEvent.VK_S);

        undo.setToolTipText(Language.translate("Undo changes (Ctrl+Z)"));
        // redo.setToolTipText("Redo changes");
        copy.setToolTipText(Language.translate("Copy image/regions to clipboard (Ctrl+C)"));
        clipboard.setToolTipText(Language.translate("Paste active regions or an image from clipboard as background (Ctrl+V)"));

        duplicate.setToolTipText(Language.translate("Duplicate Sketch"));
        delete.setToolTipText(Language.translate("Delete Page"));
        resize.setToolTipText(Language.translate("Resize"));
        history.setToolTipText(Language.translate("History of Sketch Changes"));
        setAsMaster.setToolTipText(Language.translate("Saves this sketch as the master sketch"));
        /*moveLeft.setToolTipText(Language.translate("Move sketch left (Ctrl+<-)"));
        moveRight.setToolTipText(Language.translate("Move sketch right (Ctrl+->)"));
        
        moveLeft.setText(Language.translate("move sketch to left"));
        moveRight.setText(Language.translate("move sketch to right"));
        moveLeft.setAccelerator(KeyStroke.getKeyStroke(
        KeyEvent.VK_LEFT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        moveRight.setAccelerator(KeyStroke.getKeyStroke(
        KeyEvent.VK_RIGHT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));*/

        delete.setText(Language.translate("delete page"));
        resize.setText(Language.translate("resize page"));

        // clear.setText("clear");
        clipboard.setText("");
        //if (screenWidth > 800) {
        undo.setText("");
        // redo.setText("");
        copy.setText("");
        //}
        //delete.setMnemonic(KeyEvent.VK_T);
        //resize.setMnemonic(KeyEvent.VK_R);
        //undo.setMnemonic(KeyEvent.VK_U);
        //redo.setMnemonic(KeyEvent.VK_R);
        snapGrid.setText("");
        snapGrid.setToolTipText(Language.translate("Show/hide grid lines"));
        //snapGrid.setMnemonic(KeyEvent.VK_G);
        snapGrid.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.getInstance().setSnapToGrid(!SketchletEditor.getInstance().isSnapToGrid());
                GlobalProperties.set("snap-to-grid", "" + SketchletEditor.getInstance().isSnapToGrid());
                RefreshTime.update();
                SketchletEditor.getInstance().repaint();
                SketchletEditor.getInstance().getHelpViewer().showAutoHelpByID("editor_grid");
                ActivityLog.log("snapToGrid", snapGrid.isSelected() + "");
            }
        });
        showRulers.setText("");
        showRulers.setToolTipText(Language.translate("Show/hide rulers"));
        //showRulers.setMnemonic(KeyEvent.VK_G);
        showRulers.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (SketchletEditor.getInstance().scrollPane.getColumnHeader() == null || SketchletEditor.getInstance().scrollPane.getColumnHeader().getView() == null) {
                    SketchletEditor.getInstance().scrollPane.setColumnHeaderView(SketchletEditor.getInstance().getRulerHorizontal());
                    SketchletEditor.getInstance().scrollPane.setRowHeaderView(SketchletEditor.getInstance().getRulerVertical());
                    GlobalProperties.set("rulers", "true");
                } else {
                    SketchletEditor.getInstance().scrollPane.setColumnHeaderView(null);
                    SketchletEditor.getInstance().scrollPane.setRowHeaderView(null);
                    GlobalProperties.set("rulers", "false");
                }
                SketchletEditor.getInstance().getHelpViewer().showAutoHelpByID("editor_ruler");
                SketchletEditor.getInstance().repaint();
                ActivityLog.log("showRulers", showRulers.isSelected() + "");
                RefreshTime.update();
            }
        });

        help.setText("");
        help.setToolTipText(Language.translate("Help"));
        //showActions.setMnemonic(KeyEvent.VK_P);
        help.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.getInstance().getSketchToolbar().showNavigator(true);
                SketchletEditor.getInstance().getTabsBrowser().setSelectedIndex(SketchletEditor.getInstance().getTabsBrowser().getTabCount() - 1);
                ActivityLog.log("showHelp", "");
            }
        });


        clipboard.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (SketchletEditor.getInstance().getMode() == SketchletEditorMode.ACTIONS && SketchletEditor.getInstance().copiedActions != null) {
                    SketchletEditor.getInstance().getEditorClipboardController().pasteSpecial();
                    ActivityLog.log("paste", "regions");
                } else {
                    SketchletEditor.getInstance().getEditorClipboardController().fromClipboard();
                    ActivityLog.log("paste", "image");
                }
            }
        });

        pasteSpecial.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.getInstance().getEditorClipboardController().pasteSpecial();
            }
        });

        copy.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.getInstance().getEditorClipboardController().copy();
                ActivityLog.log("copy", "");
            }
        });

        duplicate.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.getInstance().duplicate();
                ActivityLog.log("duplicateSketch", "");
            }
        });

        undo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.getInstance().undo();
                ActivityLog.log("undo", "");
            }
        });


        delete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.getInstance().delete();
                ActivityLog.log("delete", "");
            }
        });

        resize.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.getInstance().resize();
                ActivityLog.log("resizePage", "");
            }
        });

        history.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.getInstance().history();
                ActivityLog.log("history", "");
            }
        });

        visualizeVariables.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                bVisualizeVariables = !bVisualizeVariables;
                RefreshTime.update();
                if (SketchletEditor.getInstance().getInternalPlaybackPanel() != null) {
                    SketchletEditor.getInstance().getInternalPlaybackPanel().repaint();
                }
                visualizeVariables.setSelected(bVisualizeVariables);
                SketchletEditor.getInstance().repaint();
                SketchletEditor.getInstance().getHelpViewer().showAutoHelpByID("sketchify_editor_toolbar");
                ActivityLog.log("showVariables", "");
                RefreshTime.update();
            }
        });
        visualizeEye.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                EyeFrame.showFrame();
                SketchletEditor.getInstance().getHelpViewer().showAutoHelpByID("sketchify_editor_toolbar");
                ActivityLog.log("showEye", "");
            }
        });

        visualizeInfo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                bVisualizeInfoRegions = !bVisualizeInfoRegions;
                RefreshTime.update();
                if (SketchletEditor.getInstance().getInternalPlaybackPanel() != null) {
                    SketchletEditor.getInstance().getInternalPlaybackPanel().repaint();
                }

                visualizeInfo.setSelected(bVisualizeInfoRegions);

                SketchletEditor.getInstance().repaint();
                if (SketchletEditor.getInstance().getInternalPlaybackPanel() != null) {
                    SketchletEditor.getInstance().getInternalPlaybackPanel().repaint();
                }
                SketchletEditor.getInstance().getHelpViewer().showAutoHelpByID("sketchify_editor_toolbar");
                ActivityLog.log("showRegionsTextInfo", "");
            }
        });
        visualizeInfoPage.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                bVisualizeInfoSketch = !bVisualizeInfoSketch;
                RefreshTime.update();
                if (SketchletEditor.getInstance().getInternalPlaybackPanel() != null) {
                    SketchletEditor.getInstance().getInternalPlaybackPanel().repaint();
                }

                visualizeInfoPage.setSelected(bVisualizeInfoSketch);

                SketchletEditor.getInstance().repaint();
                if (SketchletEditor.getInstance().getInternalPlaybackPanel() != null) {
                    SketchletEditor.getInstance().getInternalPlaybackPanel().repaint();
                }
                SketchletEditor.getInstance().getHelpViewer().showAutoHelpByID("sketchify_editor_toolbar");
                ActivityLog.log("showPageTextInfo", "");
            }
        });

        setAsMaster.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.getInstance().setAsMaster();
                ActivityLog.log("setAsMaster", "");
            }
        });
        postIt.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().getPostNoteTool().setPreviousTool(SketchletEditor.getInstance().getTool());
                SketchletEditor.getInstance().setTool(SketchletEditor.getInstance().getPostNoteTool(), postIt);
                SketchletEditor.getInstance().getHelpViewer().showAutoHelpByID("sketchify_editor_toolbar");
            }
        });
        more.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JPopupMenu popupMenu = new JPopupMenu();

                popupMenu.add(duplicate);
                popupMenu.add(delete);
                popupMenu.addSeparator();
                popupMenu.add(resize);
                popupMenu.addSeparator();
                popupMenu.addSeparator();
                popupMenu.add(setAsMaster);

                popupMenu.show(more.getParent(), more.getX(), more.getY() + more.getHeight());
            }
        });

        /*moveLeft.addActionListener(new ActionListener() {
        
        public void actionPerformed(ActionEvent ae) {
        SketchletEditor.editorPanel.moveLeft();
        ActivityLog.log("moveSketchLeft", "");
        }
        });
        moveRight.addActionListener(new ActionListener() {
        
        public void actionPerformed(ActionEvent ae) {
        SketchletEditor.editorPanel.moveRight();
        ActivityLog.log("moveSketchRight", "");
        }
        });*/

        setFloatable(false);
        setBorder(BorderFactory.createEmptyBorder());
        FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT);
        flowLayout.setHgap(0);
        flowLayout.setVgap(0);
        setLayout(flowLayout);
        setAlignmentX(JToolBar.LEFT_ALIGNMENT);
        // add(new JLabel(" title:"));
        //title.setPreferredSize(new Dimension(70, 25));


        newSketch.setToolTipText(Language.translate("Create a new page (Ctrl+N)"));
        save.setToolTipText(Language.translate("Saves current changes (Ctrl+S)"));
        newSketch.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.getInstance().newSketch();
                ActivityLog.log("newSketch", "", "new.gif", newSketch);
            }
        });

        save.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                GlobalProperties.save();
                SketchletEditor.getInstance().saveAndWait();
                Timers.getGlobalTimers().save();
                Macros.globalMacros.save();
                Workspace.getMainPanel().saveConfiguration();
                Workspace.getMainPanel().saveOriginal();
                while (Workspace.getMainPanel().isSavingOriginalInProgress()) {
                    try {
                        Thread.sleep(10);
                    } catch (Exception ex) {
                    }
                }
                ActivityLog.log("saveSketch", "");
            }
        });

        play.setToolTipText(Language.translate("Executes current sketch in a new window (Ctrl+F5)"));
        //spreadsheet.setToolTipText(Language.translate("Open the default spreadsheet in OpenOffice.org CALC"));
        navigator.setToolTipText(Language.translate("Show/hide the project navigator"));
        space.setToolTipText(Language.translate("Define interaction space and screens"));
        /*spreadsheet.addActionListener(new ActionListener() {
        
        public void actionPerformed(ActionEvent e) {
        Workspace.mainPanel.openDefaultSpreadsheet();
        ActivityLog.log("externalSpreadsheet", "", "spreadsheet.png", spreadsheet);
        }
        });*/

        navigator.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                showHideProjectNavigator();
                ActivityLog.log("showNavigator", SketchletEditor.getInstance().isTabsVisible() + "");
//                Workspace.mainFrame.setVisible(true);
                //              Workspace.mainPanel.tabs.setSelectedIndex(1);
            }
        });
        space.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                InteractionSpaceFrame.showFrame();
                showHideProjectNavigator();
                ActivityLog.log("interactionSpaceSettings", "", "interaction_space.png", space);
            }
        });


        play.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // MessageFrame.showMessage(SketchletEditor.editorFrame, "Preparing playback...", SketchletEditor.editorPanel.centralPanel);
                java.awt.EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        SketchletEditor.getInstance().play();
                        // MessageFrame.closeMessage();
                        ActivityLog.log("play", "", "start.gif", play);
                    }
                });
            }
        });

        //add(new JLabel("  "));
        //add(title);

        //add(spreadsheet);

        // add(moveLeft);
        // add(moveRight);

        // add(duplicate);

        // add(delete);
        // add(history);

        //add(sketchProperties);
//        add(stateTransition);


        // add(details);

        // JToolBar panel = new JToolBar();
        // panel.setFloatable(true);

        flowLayout = new FlowLayout(FlowLayout.LEFT);
        flowLayout.setHgap(0);
        flowLayout.setVgap(0);
        setLayout(flowLayout);
        undo.setMargin(new Insets(2, 2, 2, 2));
        //redo.setMargin(new Insets(2, 2, 2, 2));
        // add(clear);
        copy.setMargin(new Insets(2, 2, 2, 2));
        add(copy);
        clipboard.setMargin(new Insets(2, 2, 2, 2));
        add(clipboard);
        addSeparator();
        // add(pasteSpecial);
        smaller.setMargin(new Insets(2, 2, 2, 2));
        zoomWin.setMargin(new Insets(2, 2, 2, 2));
        showPerspective.setMargin(new Insets(2, 2, 2, 2));
        bigger.setMargin(new Insets(2, 2, 2, 2));
        snapGrid.setMargin(new Insets(2, 2, 2, 2));
        showRulers.setMargin(new Insets(2, 2, 2, 2));
        //wizard.setMargin(new Insets(2, 2, 2, 2));
        postIt.setMargin(new Insets(2, 2, 2, 2));
        more.setMargin(new Insets(2, 2, 2, 2));
        help.setMargin(new Insets(2, 2, 2, 2));
    }

    public void loadButtons() {
        this.removeAll();
        if (Profiles.isActive("new_page")) {
            add(newSketch);
        }
        if (Profiles.isActive("save_page")) {
            add(save);
        }
        if (Profiles.isActive("new_page,save_page")) {
            addSeparator();
        }
        if (Profiles.isActive("execute_page")) {
            add(play);
        }
        if (Profiles.isActive("presentation_space")) {
            add(space);
        }
        if (Profiles.isActive("execute_page,presentation_space")) {
            addSeparator();
        }
        if (Profiles.isActive("undo")) {
            add(undo);
            //add(redo);
            addSeparator();
        }
        if (Profiles.isActive("zoom_inout")) {
            add(smaller);
        }
        if (Profiles.isActive("zoom_combo")) {
            add(zoomBox);
        }
        if (Profiles.isActive("zoom_inout")) {
            add(bigger);
            add(zoomWin);
            addSeparator();
        }
        if (Profiles.isActive("page_perspective,toolbar_page_perspective")) {
            add(showPerspective);
            addSeparator();
        }
        if (Profiles.isActive("grid")) {
            add(snapGrid);
        }
        if (Profiles.isActive("rulers")) {
            add(showRulers);
            addSeparator();
        }
        if (Profiles.isActive("variables,toolbar_variables")) {
            add(visualizeVariables);
        }
        if (Profiles.isActive("variables,toolbar_textinfo")) {
            add(visualizeInfoPage);
            add(visualizeInfo);
        }
        if (Profiles.isActive("eye")) {
            //add(visualizeEye);
            //addSeparator();
        }
        if (Profiles.isActive("toolbar_postit")) {
            addSeparator();
            add(postIt);
            addSeparator();
        }
        if (Profiles.isActive("toolbar_help")) {
            add(help);
            //addSeparator();
        }
        if (Profiles.isActive("toolbar_right_panel")) {
            //add(navigator);
        }
    }

    public void showHideProjectNavigator() {
        if (SketchletEditor.getInstance().isTabsVisible()) {
            if (SketchletEditor.getInstance().getTabsBrowser().getSelectedIndex() == 0) {
                SketchletEditor.getInstance().editorFrame.getContentPane().remove(SketchletEditor.getInstance().getTabsBrowser());
                ((JPanel) SketchletEditor.getInstance().editorFrame.getContentPane()).revalidate();
                SketchletEditor.getInstance().setTabsVisible(false);
            } else {
                SketchletEditor.getInstance().getTabsBrowser().setSelectedIndex(0);
            }
        } else {
            SketchletEditor.getInstance().editorFrame.getContentPane().add(SketchletEditor.getInstance().getTabsBrowser(), BorderLayout.EAST);
            ((JPanel) SketchletEditor.getInstance().editorFrame.getContentPane()).revalidate();
            SketchletEditor.getInstance().setTabsVisible(true);
            SketchletEditor.getInstance().getTabsBrowser().setSelectedIndex(0);
        }
    }

    public void showNavigator(boolean bShow) {
        if (SketchletEditor.getInstance().isTabsVisible() && !bShow) {
            SketchletEditor.getInstance().editorFrame.getContentPane().remove(SketchletEditor.getInstance().getTabsBrowser());
            ((JPanel) SketchletEditor.getInstance().editorFrame.getContentPane()).revalidate();
            SketchletEditor.getInstance().setTabsVisible(false);
        } else if (!SketchletEditor.getInstance().isTabsVisible() && bShow) {
            SketchletEditor.getInstance().editorFrame.getContentPane().add(SketchletEditor.getInstance().getTabsBrowser(), BorderLayout.EAST);
            ((JPanel) SketchletEditor.getInstance().editorFrame.getContentPane()).revalidate();
            SketchletEditor.getInstance().setTabsVisible(true);
        }
    }

    public void enableControls() {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {

                if (SketchletEditor.getInstance() == null || SketchletEditor.getInstance().getPages() == null) {
                    return;
                }
                if (SketchletEditor.getInstance().getMode() == SketchletEditorMode.SKETCHING) {
                    try {
                        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
                        Transferable transferable = clip.getContents(null);
                        DataFlavor[] dataFlavors = transferable.getTransferDataFlavors();
                        clipboard.setEnabled(true);
                    } catch (Exception e) {
                    }
                } else {
                    clipboard.setEnabled(SketchletEditor.getInstance().copiedActions != null && SketchletEditor.getInstance().copiedActions.size() > 0);
                }
                undo.setEnabled(SketchletEditor.getInstance() != null && SketchletEditor.getInstance().getLayer() >= 0 && SketchletEditor.getInstance().getUndoRegionActions() != null && SketchletEditor.getInstance().getUndoRegionActions().size() > 0);
            }
        });
    }

    public void prepareZoomBox() {
        zoomBox.setEditable(true);

        zoomBox.setPreferredSize(new Dimension(70, 25));
        for (int i = 0; i < zoomOptions.length; i++) {
            zoomBox.addItem(zoomOptions[i] + "%");
        }

        zoomBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                String strZoom = (String) zoomBox.getSelectedItem();

                try {
                    final double oldZoom = SketchletEditor.getInstance().getScale();

                    double _zoom = 1.0;
                    strZoom = strZoom.replace("%", "").trim();
                    _zoom = Double.parseDouble(strZoom) / 100.0;

                    if (!SketchToolbar.animateZoom) {
                        SketchletEditor.getInstance().setScale(_zoom);
                        return;
                    }

                    final double zoom = _zoom;

                    final double endZoom = zoom;
                    final double startZoom = SketchletEditor.getInstance().getScale();

                    final String _strZoom = strZoom;

                    new Thread(new Runnable() {

                        public void run() {
                            double step = (endZoom - startZoom) / 20;
                            for (int zi = 0; zi < 20; zi++) {
                                SketchletEditor.getInstance().setScale(SketchletEditor.getInstance().getScale() + step);
                                SketchletEditor.getInstance().repaint();
                                try {
                                    Thread.sleep(10);
                                } catch (Exception e) {
                                }
                            }
                            double factor = (zoom + oldZoom) / 2;

                            SketchletEditor.getInstance().revalidate();
                            SketchletEditor.getInstance().getCentralPanel().revalidate();
                            SketchletEditor.getInstance().scrollPane.revalidate();
                            SketchletEditor.getInstance().editorFrame.repaint();
                            SketchletEditor.getInstance().requestFocus();
                            GlobalProperties.set("editor-zoom", (String) zoomBox.getSelectedItem());
                            GlobalProperties.save();
                            if (SketchletEditor.getInstance().getInternalPlaybackPanel() != null) {
                                SketchletEditor.getInstance().getInternalPlaybackPanel().setScale(SketchletEditor.getInstance().getScale());
                                SketchletEditor.getInstance().getInternalPlaybackPanel().repaint();
                            }
                            SketchletEditor.getInstance().getRulerHorizontal().repaint();
                            RefreshTime.update();
                            SketchletEditor.getInstance().getRulerVertical().repaint();
                            ActivityLog.log("setZoom", _strZoom);
                        }
                    }).start();

                } catch (Exception e) {
                }
            }
        });

        try {
            String strZoom = FileUtils.getFileText(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "last_zoom.txt").trim();
            if (!strZoom.isEmpty()) {
                zoomBox.setSelectedItem(strZoom);
            } else {
                strZoom = GlobalProperties.get("editor-zoom");
                if (strZoom != null && !strZoom.isEmpty()) {
                    zoomBox.setSelectedItem(strZoom);
                } else {
                    zoomBox.setSelectedItem("100%");
                }
            }
        } catch (Exception e) {
            zoomBox.setSelectedItem("100%");
        }


        zoomWin.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                SketchletEditor.getInstance().zoomToWindow();
            }
        });

        showPerspective.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.getInstance().showPerspectivePanel();
                ActivityLog.log("showPerspective", SketchletEditor.getInstance().isPagePanelShown() + "", "perspective_lines.png", showPerspective);
                RefreshTime.update();
            }
        });
        bigger.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                String strZoom = (String) zoomBox.getSelectedItem();

                try {
                    strZoom = strZoom.replace("%", "").trim();

                    double zoom = Double.parseDouble(strZoom);

                    for (int i = zoomOptions.length - 1; i >= 0; i--) {
                        final int index = i;
                        if (zoomOptions[i] > zoom) {
                            zoomBox.setSelectedItem(zoomOptions[i] + "%");
                            break;
                        }
                    }
                } catch (Exception e) {
                }
            }
        });
        smaller.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                String strZoom = (String) zoomBox.getSelectedItem();

                try {
                    strZoom = strZoom.replace("%", "").trim();

                    double zoom = Double.parseDouble(strZoom);

                    for (int i = 0; i < zoomOptions.length; i++) {
                        if (zoomOptions[i] < zoom) {
                            zoomBox.setSelectedItem(zoomOptions[i] + "%");
                            break;
                        }
                    }
                } catch (Exception e) {
                }
            }
        });
    }
}
