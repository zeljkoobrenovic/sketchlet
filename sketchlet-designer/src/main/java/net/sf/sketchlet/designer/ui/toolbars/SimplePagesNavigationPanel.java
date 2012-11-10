/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui.toolbars;

import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.ui.playback.displays.PropertiesFrame;
import net.sf.sketchlet.help.HelpUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author zobrenovic
 */
public class SimplePagesNavigationPanel extends JPanel {

    JButton home = new JButton(Workspace.createImageIcon("resources/home.gif", ""));
    JButton help = new JButton(Workspace.createImageIcon("resources/help-browser.png", ""));
    JButton properties = new JButton(Workspace.createImageIcon("resources/form-properties.png"));
    JButton importSketches = new JButton(Workspace.createImageIcon("resources/import.gif", ""));
    public JButton goBack = new JButton(Workspace.createImageIcon("resources/go-previous.png"));

    public SimplePagesNavigationPanel() {
        createControlPanel();
    }

    private void createControlPanel() {
        properties.setToolTipText("Properties");
        importSketches.setToolTipText("Import sketches from other projects");


        importSketches.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.importSketches();
            }
        });


        home.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Workspace.mainFrame.setVisible(true);
                Workspace.mainPanel.tabs.setSelectedIndex(0);
            }
        });

        properties.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                PropertiesFrame.showFrame();
            }
        });

        this.setLayout(new BorderLayout());

        SketchletEditor.editorPanel.editorFrame.setTitle(SketchletEditor.editorPanel.currentPage.title);
        SketchletEditor.editorPanel.textArea.setText(SketchletEditor.editorPanel.currentPage.strTextAnnotation);
        if (SketchletEditor.editorPanel.currentPage != null) {
            SketchletEditor.editorPanel.currentPage.activate(false);
        }

        JToolBar fileControls = new JToolBar();
        fileControls.setFloatable(false);
        fileControls.setBorder(BorderFactory.createEmptyBorder());
        JToolBar fileControlsHelp = new JToolBar();
        fileControlsHelp.setFloatable(false);
        fileControlsHelp.setBorder(BorderFactory.createEmptyBorder());

        fileControls.add(new JLabel("   "));
        fileControls.add(new JLabel("   "));
        fileControls.add(new JLabel("   "));
        fileControlsHelp.add(help);
        help.setToolTipText("How can I \"sketchify\" here?");
        help.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                HelpUtils.openHelpFile("Sketcifying", "sketchifying");
            }
        });


        JPanel top = new JPanel();
        top.setLayout(new BorderLayout());
        JPanel tabPanel = new JPanel(new BorderLayout());
        final JButton tabsButton = new JButton(Workspace.createImageIcon("resources/down.png"));
        tabsButton.setMargin(new Insets(0, 0, 0, 0));
        tabsButton.putClientProperty("JComponent.sizeVariant", "mini");

        JToolBar tabsButtonTB = new JToolBar();
        tabsButtonTB.setFloatable(false);
        tabsButtonTB.setBorder(BorderFactory.createEmptyBorder());
        tabsButton.setBorder(BorderFactory.createEmptyBorder());
        tabsButtonTB.add(tabsButton);
        tabPanel.add(tabsButtonTB, BorderLayout.EAST);
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(fileControls, BorderLayout.CENTER);
        northPanel.add(fileControlsHelp, BorderLayout.EAST);
        this.add(top, BorderLayout.NORTH);

        final JButton help = new JButton(Workspace.createImageIcon("resources/help-browser.png"));
        help.setToolTipText("What is this toolbar?");
        help.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                HelpUtils.openHelpFile("Sketchlet Toolbar", "sketchify_editor_toolbar");
            }
        });

        JToolBar toolbarHelp = new JToolBar();
        toolbarHelp.setFloatable(false);
        toolbarHelp.setBorder(BorderFactory.createEmptyBorder());
        BorderLayout bl = new BorderLayout();
        bl.setVgap(0);
        bl.setHgap(0);
        final JButton sketchesButton = new JButton(Workspace.createImageIcon("resources/down.png"));
        sketchesButton.setToolTipText("Shows a menu with a list of sketches");
        sketchesButton.setMargin(new Insets(0, 0, 0, 0));
        sketchesButton.putClientProperty("JComponent.sizeVariant", "mini");
        sketchesButton.setMargin(new Insets(0, 0, 0, 0));
        goBack.setEnabled(false);
        goBack.setToolTipText("Go to a previous sketch");
        goBack.setMargin(new Insets(0, 0, 0, 0));
        goBack.putClientProperty("JComponent.sizeVariant", "mini");
        goBack.setMargin(new Insets(0, 0, 0, 0));

        sketchesButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                final JPopupMenu popupMenu = new JPopupMenu();
                TutorialPanel.prepare(popupMenu);

                int i = 0;
                for (Page s : SketchletEditor.editorPanel.pages.pages) {
                    final int index = i;
                    JMenuItem sketchMenuItem = new JMenuItem((i + 1) + ". " + s.title);
                    sketchMenuItem.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent ae) {
                            SketchletEditor.editorPanel.openSketchByIndex(index);
                        }
                    });
                    popupMenu.add(sketchMenuItem);
                    i++;
                }

                popupMenu.show(sketchesButton.getParent(), sketchesButton.getX(), sketchesButton.getY() + sketchesButton.getHeight());
            }
        });
        goBack.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (SketchletEditor.editorPanel.navigationHistory.size() > 0) {
                    Page s = SketchletEditor.editorPanel.navigationHistory.lastElement();
                    SketchletEditor.editorPanel.navigationHistory.remove(s);
                    SketchletEditor.editorPanel.selectSketch(s);
                    s = SketchletEditor.editorPanel.navigationHistory.lastElement();
                    SketchletEditor.editorPanel.navigationHistory.remove(s);
                    goBack.setEnabled(SketchletEditor.editorPanel.navigationHistory.size() > 0);
                }
            }
        });
        toolbarHelp.add(goBack);
        toolbarHelp.add(sketchesButton);
        toolbarHelp.add(help);
        JPanel panelNorth = new JPanel(bl);
        panelNorth.add(SketchletEditor.editorPanel.sketchToolbar);
        panelNorth.add(toolbarHelp, BorderLayout.EAST);

        this.add(panelNorth, BorderLayout.CENTER);

        tabsButton.setMargin(new Insets(0, 0, 0, 0));

        tabsButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                final JPopupMenu popupMenu = new JPopupMenu();
                TutorialPanel.prepare(popupMenu);

                int i = 0;
                for (Page s : SketchletEditor.editorPanel.pages.pages) {
                    final int index = i;
                    JMenuItem sketchMenuItem = new JMenuItem((i + 1) + ". " + s.title);
                    sketchMenuItem.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent ae) {
                            SketchletEditor.editorPanel.openSketchByIndex(index);
                        }
                    });
                    popupMenu.add(sketchMenuItem);
                    i++;
                }

                popupMenu.show(tabsButton.getParent(), tabsButton.getX(), tabsButton.getY() + tabsButton.getHeight());
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        this.add(bottomPanel, BorderLayout.SOUTH);
    }
}
