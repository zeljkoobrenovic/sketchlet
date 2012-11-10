/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui.playback.displays;

import net.sf.sketchlet.common.filter.Filters;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.designer.playback.displays.ScreenMapping;
import net.sf.sketchlet.designer.programming.screenscripts.AWTRobotUtil;
import net.sf.sketchlet.designer.ui.UIUtils;
import net.sf.sketchlet.designer.ui.macros.ImageAreaSelect;
import net.sf.sketchlet.designer.ui.region.ActiveRegionPanel;
import net.sf.sketchlet.help.HelpUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

/**
 * @author zobrenovic
 */
public class InteractionSpaceFrame extends JFrame {

    JButton save = new JButton(Language.translate("Save"), Workspace.createImageIcon("resources/ok.png", ""));
    JButton rename = new JButton(Language.translate("Rename"), Workspace.createImageIcon("resources/font.gif", ""));
    JButton newDisplay = new JButton(Language.translate("Add Display"), Workspace.createImageIcon("resources/screen.png", ""));
    String[] columnNamesCutFromSketch = {Language.translate("Dimension"), Language.translate("Value or Variable"), ""};
    String[] columnNamesTransformations = {Language.translate("Filter")};
    JTabbedPane targets;

    public InteractionSpaceFrame() {
        setTitle(Language.translate("Mapping Design Space to Display Space"));
        this.setIconImage(Workspace.createImageIcon("resources/interaction_space.png").getImage());

        String[] resolutionStrings = {"", "800x600", "1024x768", "1152x864", "1280x800", "1280x1024", "1440x800"};

        JComboBox resoultionList = new JComboBox(resolutionStrings);
        resoultionList.setSelectedIndex(0);
        resoultionList.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
            }
        });

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // logicalSpace.add(new JLabel("Unit:"));
        String[] unitsStrings = {"", "pixel", "mm", "cm", "m", "inch", "foot"};

        JComboBox unitsList = new JComboBox(unitsStrings);
        unitsList.setSelectedIndex(0);
        // logicalSpace.add(unitsList);
        // logicalSpace.add( new JLabel(" ") );
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        String[] screens = new String[gs.length];
        for (int j = 0; j < gs.length; j++) {
            screens[j] = "" + (j + 1);
        }

        targets = new JTabbedPane();

        int nScreen = 1;
        final InteractionSpaceFrame parent = this;
        for (final ScreenMapping display : InteractionSpace.displays) {
            JPanel controls = new JPanel(new BorderLayout());
            JPanel mapping = new JPanel(new BorderLayout());

            JComboBox screenList = new JComboBox(screens);

            screenList.setSelectedItem(display.showOnDisplay);

            JPanel controlsTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlsTop.add(display.enable);
            ItemListener itemListeners[] = display.enable.getItemListeners();
            for (int i = 0; i < itemListeners.length; i++) {
                display.enable.removeItemListener(itemListeners[i]);
            }
            final int screenIndex = nScreen - 1;
            display.enable.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    if (display.enable.isSelected()) {
                        targets.setIconAt(screenIndex, Workspace.createImageIcon("resources/screen.png"));
                    } else {
                        targets.setIconAt(screenIndex, Workspace.createImageIcon("resources/screen_disabled.png"));
                    }
                }
            });
            // controls.add(screenList);
            controlsTop.add(display.showToolbar);
            controlsTop.add(display.showDecoration);
            controlsTop.add(display.alwaysOnTop);
            JPanel controlsBottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
            controlsBottom.add(display.showMaximized);
            controlsBottom.add(new JLabel(" on Screen "));
            controlsBottom.add(display.screenIndex);
            controlsBottom.add(display.fitToScreen);
            JButton displayArea = new JButton("Screen Position & Size", Workspace.createImageIcon("resources/position_screen.png", ""));
            JTabbedPane tabs = new JTabbedPane();

            final AbstractTableModel model1 = new AbstractTableModel() {

                public int getColumnCount() {
                    return columnNamesCutFromSketch.length;
                }

                public String getColumnName(int col) {
                    return columnNamesCutFromSketch[col];
                }

                public int getRowCount() {
                    return display.cutFromSketch.length;
                }

                public Object getValueAt(int row, int col) {
                    return display.cutFromSketch[row][col];
                }

                public void setValueAt(Object value, int row, int col) {
                    display.cutFromSketch[row][col] = value;
                }

                public boolean isCellEditable(int row, int col) {
                    return col == 1;
                }
            };

            displayArea.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    selectDisplayArea(display);
                    model1.fireTableDataChanged();
                }
            });

            controls.add(controlsTop, BorderLayout.CENTER);
            controls.add(controlsBottom, BorderLayout.SOUTH);

            final AbstractTableModel model2 = new AbstractTableModel() {

                public int getColumnCount() {
                    return columnNamesTransformations.length;
                }

                public String getColumnName(int col) {
                    return columnNamesTransformations[col];
                }

                public int getRowCount() {
                    return display.transformations.length;
                }

                public Object getValueAt(int row, int col) {
                    return display.transformations[row][col];
                }

                public void setValueAt(Object value, int row, int col) {
                    display.transformations[row][col] = value;
                }

                public boolean isCellEditable(int row, int col) {
                    return true;
                }
            };
            final JTable tableCutFromSketch = new JTable(model1);

            tableCutFromSketch.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
            tableCutFromSketch.setFillsViewportHeight(true);
            JScrollPane scrollPaneCutFromSketch = new JScrollPane(tableCutFromSketch);
            scrollPaneCutFromSketch.setPreferredSize(new Dimension(100, 160));

            final JTable tableTransformations = new JTable(model2);
            tableTransformations.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
            tableTransformations.setFillsViewportHeight(true);
            JScrollPane scrollPaneTransformations = new JScrollPane(tableTransformations);
            scrollPaneTransformations.setPreferredSize(new Dimension(100, 80));

            setVariablesComboWithEquals(tableCutFromSketch, tableTransformations);
            setTransformations(tableTransformations);

            JButton clearClip = new JButton(Language.translate("Clear Clip"));
            JButton clearPosSize = new JButton(Language.translate("Clear Position and Size"));
            JButton clearPerspective = new JButton(Language.translate("Clear Calibration"));
            JButton equalSize = new JButton(Language.translate("Display=Clip"));

            JPanel clips = new JPanel(new BorderLayout());
            clips.setBorder(BorderFactory.createEtchedBorder());
            JPanel clipPanel = new JPanel(new BorderLayout());

            JButton sketchArea = new JButton(Language.translate("Visible Area"), Workspace.createImageIcon("resources/clip.png", ""));
            JButton perspective = new JButton(Language.translate("Calibration"), Workspace.createImageIcon("resources/perspective.png", ""));
            sketchArea.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    selectSketchArea(display);
                    model1.fireTableDataChanged();
                }
            });
            perspective.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    perspective(display, screenIndex);
                    model1.fireTableDataChanged();
                }
            });

            JPanel btnPanel = new JPanel();
            btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            btnPanel.add(sketchArea);
            btnPanel.add(displayArea);
            btnPanel.add(perspective);

            JPanel btnPanel2 = new JPanel();
            btnPanel2.setLayout(new FlowLayout(FlowLayout.LEFT));
            btnPanel2.add(clearClip);
            btnPanel2.add(clearPosSize);
            btnPanel2.add(clearPerspective);
            btnPanel2.add(equalSize);

            mapping.add(controls, BorderLayout.NORTH);
            clipPanel.add(btnPanel, BorderLayout.NORTH);
            clipPanel.add(scrollPaneCutFromSketch, BorderLayout.CENTER);


            clearPosSize.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    for (int i = 0; i < 4; i++) {
                        display.cutFromSketch[i][1] = "";
                    }
                    model1.fireTableDataChanged();
                }
            });
            clearClip.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    for (int i = 4; i < 8; i++) {
                        display.cutFromSketch[i][1] = "";
                    }
                    model1.fireTableDataChanged();
                }
            });
            clearPerspective.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    for (int i = 8; i < 16; i++) {
                        display.cutFromSketch[i][1] = "";
                    }
                    model1.fireTableDataChanged();
                }
            });
            equalSize.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    for (int i = 0; i < 4; i++) {
                        display.cutFromSketch[i][1] = display.cutFromSketch[4 + i][1];
                    }
                    model1.fireTableDataChanged();
                }
            });

            JPanel btnPanels = new JPanel(new BorderLayout());
            btnPanels.add(btnPanel2, BorderLayout.SOUTH);

            JPanel downPanel = new JPanel(new BorderLayout());
            downPanel.add(btnPanels, BorderLayout.NORTH);

            JPanel panelTransform = new JPanel(new BorderLayout());
            panelTransform.add(scrollPaneTransformations, BorderLayout.CENTER);

            JPanel panelFilterButtons = new JPanel();
            JButton deleteFilter = new JButton(Language.translate("Remove Filter"));
            panelFilterButtons.add(deleteFilter);
            panelTransform.add(panelFilterButtons, BorderLayout.EAST);

            deleteFilter.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    int row = tableTransformations.getSelectedRow();

                    if (row >= 0) {
                        for (int r = row; r < display.transformations.length - 1; r++) {
                            display.transformations[r][0] = display.transformations[r + 1][0];
                            display.transformations[r][1] = display.transformations[r + 1][1];
                        }
                        int r = display.transformations.length - 1;
                        display.transformations[r][0] = "";
                        display.transformations[r][1] = "";
                    }
                    model2.fireTableDataChanged();
                }
            });


            // downPanel.add(tabs, BorderLayout.CENTER);

            JPanel panel1 = new JPanel(new BorderLayout());

            JPanel panel2 = new JPanel();

            UIUtils.populateVariablesCombo(display.exportFileVariableCombo, false);
            UIUtils.populateVariablesCombo(display.exportBase64VariableCombo, false);
            display.exportFileVariableCombo.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    display.exportStrFileVariableCombo = display.exportFileVariableCombo.getSelectedItem() + "";
                }
            });
            display.exportBase64VariableCombo.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    display.exportStrBase64VariableCombo = display.exportBase64VariableCombo.getSelectedItem() + "";
                }
            });
            display.exportFileVariableCombo.setSelectedItem(display.exportStrFileVariableCombo);
            display.exportBase64VariableCombo.setSelectedItem(display.exportStrBase64VariableCombo);

            display.exportFileButton = new JButton("...");
            ActionListener listeners[] = display.exportFileButton.getActionListeners();
            for (int ll = 0; ll < listeners.length; ll++) {
                display.exportFileButton.removeActionListener(listeners[ll]);
            }
            display.exportFileButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    int returnVal = ActiveRegionPanel.getFileChooser().showOpenDialog(parent);

                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        display.exportFileField.setText(ActiveRegionPanel.getFileChooser().getSelectedFile().getAbsolutePath());
                    }
                }
            });
            panel2.add(display.exportLabel3);
            panel2.add(display.exportFileVariableCombo);
            panel2.add(display.exportLabel4);
            JPanel panel4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
            panel4.add(display.exportLabel5);
            panel4.add(display.exportBase64VariableCombo);
            panel4.add(display.exportLabel6);
            //panel2.add(display.exportFileField);
            //panel2.add(display.exportFileButton);

            JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
            panel3.add(display.exportDisplay);
            display.exportOn.removeAllItems();
            display.exportOn.addItem(Language.translate("on entry"));
            display.exportOn.addItem(Language.translate("periodically"));
            display.exportOn.setSelectedItem(display.exportStrOn);
            display.exportOn.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    display.exportStrOn = display.exportOn.getSelectedItem() + "";
                    display.enableControls();
                }
            });
            panel3.add(display.exportOn);
            panel3.add(display.exportLabel1);
            panel3.add(display.exportFrequency);
            panel3.add(display.exportLabel2);

            display.exportDisplay.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    display.enableControls();
                }
            });

            display.enableControls();

            panel1.add(panel3, BorderLayout.NORTH);
            JPanel panel5 = new JPanel(new BorderLayout());
            panel5.add(panel2, BorderLayout.NORTH);
            panel5.add(panel4, BorderLayout.CENTER);
            panel1.add(panel5, BorderLayout.CENTER);
            // downPanel.add(panel1, BorderLayout.SOUTH);

            clipPanel.add(downPanel, BorderLayout.SOUTH);

            clips.add(clipPanel);
            tabs.add(clips, Language.translate("Dimensions"));
            tabs.add(panelTransform, Language.translate("Image Filters"));
            tabs.add(panel1, Language.translate("Save to Image File"));
            mapping.add(tabs);

            targets.add(mapping, Language.translate("Screen") + " " + nScreen);
            if (display.enable.isSelected()) {
                targets.setIconAt(nScreen - 1, Workspace.createImageIcon("resources/screen.png"));
            } else {
                targets.setIconAt(nScreen - 1, Workspace.createImageIcon("resources/screen_disabled.png"));
            }

            nScreen++;
        }

        JPanel panelSouth = new JPanel(new BorderLayout());
        panelSouth.add(targets, BorderLayout.CENTER);
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(save);


        final JButton alwaysOnTopBtn = new JButton(Workspace.createImageIcon("resources/pin_up.png"));
        alwaysOnTopBtn.setActionCommand("pin");
        alwaysOnTopBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (alwaysOnTopBtn.getActionCommand().equals("pin")) {
                    alwaysOnTopBtn.setActionCommand("unpin");
                    alwaysOnTopBtn.setIcon(Workspace.createImageIcon("resources/pin_down.png"));
                    setAlwaysOnTop(true);
                } else {
                    alwaysOnTopBtn.setActionCommand("pin");
                    alwaysOnTopBtn.setIcon(Workspace.createImageIcon("resources/pin_up.png"));
                    setAlwaysOnTop(false);
                }
            }
        });
        final JButton help = new JButton(Workspace.createImageIcon("resources/help-browser.png", ""));
        help.setToolTipText(Language.translate("What are page events?"));
        help.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                HelpUtils.openHelpFile("Mapping Design Space to Display Space", "display_mapping");
            }
        });
        JToolBar toolbarPin = new JToolBar();
        toolbarPin.setFloatable(false);
        toolbarPin.add(alwaysOnTopBtn);
        toolbarPin.add(help);
        JPanel panelPinHelp = new JPanel(new BorderLayout());
        panelPinHelp.add(btnPanel);
        panelPinHelp.add(toolbarPin, BorderLayout.EAST);

        panelSouth.add(panelPinHelp, BorderLayout.SOUTH);

        getContentPane().add(panelSouth, BorderLayout.SOUTH);
        save.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                save();
            }
        });
        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                save();
            }
        });
        frame = this;

        load();
        pack();
        //setSize(500, 650);

    }

    JComboBox comboBox = new JComboBox();
    JComboBox comboBoxTransformations = new JComboBox();

    public void save() {
        try {
            InteractionSpace.save();
        } catch (Exception e) {
        }

        setState(Frame.ICONIFIED);
    }

    public void selectSketchArea(ScreenMapping display) {
        BufferedImage image;
        if (SketchletEditor.editorPanel != null) {
            int tw = SketchletEditor.editorPanel.getWidth();
            int th = SketchletEditor.editorPanel.getHeight();

            image = SketchletEditor.editorPanel.renderer.paintImage(0, 0, tw, th);
        } else {
            image = Workspace.createCompatibleImage(2000, 2000);
        }
        int x = 0;
        int y = 0;
        int w = 100;
        int h = 100;
        try {
            w = Integer.parseInt(display.cutFromSketch[6][1].toString());
        } catch (Exception eNum) {
        }
        try {
            h = Integer.parseInt(display.cutFromSketch[7][1].toString());
        } catch (Exception eNum) {
        }
        try {
            x = Integer.parseInt(display.cutFromSketch[4][1].toString());
        } catch (Exception eNum) {
        }
        try {
            y = Integer.parseInt(display.cutFromSketch[5][1].toString());
        } catch (Exception eNum) {
        }

        ImageAreaSelect.createAndShowGUI(this, image, x, y, w, h, false);
        if (ImageAreaSelect.bSaved) {
            String params[] = ImageAreaSelect.strArea.split(" ");
            if (params.length >= 4) {
                display.cutFromSketch[4][1] = params[0];
                display.cutFromSketch[5][1] = params[1];
                display.cutFromSketch[6][1] = params[2];
                display.cutFromSketch[7][1] = params[3];

                //if (display.cutFromSketch[2][1].toString().isEmpty()) {
                display.cutFromSketch[2][1] = params[2];
                //}
                //if (display.cutFromSketch[3][1].toString().isEmpty()) {
                display.cutFromSketch[3][1] = params[3];
                //}
            }
        }
    }

    public void perspective(ScreenMapping display, int screenIndex) {
        CalibrationFrame.createAndShowGUI(this, display, screenIndex);
    }

    public void selectDisplayArea(ScreenMapping display) {
        Dimension dimScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle rectScreenSize = new Rectangle(dimScreenSize);
        BufferedImage captureedImage = AWTRobotUtil.robot.createScreenCapture(rectScreenSize);
        int x = 0;
        int y = 0;
        int w = 100;
        int h = 100;
        try {
            x = Integer.parseInt(display.cutFromSketch[0][1].toString());
        } catch (Exception eNum) {
        }
        try {
            y = Integer.parseInt(display.cutFromSketch[1][1].toString());
        } catch (Exception eNum) {
        }
        try {
            w = Integer.parseInt(display.cutFromSketch[2][1].toString());
        } catch (Exception eNum) {
        }
        try {
            h = Integer.parseInt(display.cutFromSketch[3][1].toString());
        } catch (Exception eNum) {
        }
        ImageAreaSelect.createAndShowGUI(this, captureedImage, x, y, w, h, false);
        if (ImageAreaSelect.bSaved) {
            display.showMaximized.setSelected(false);
            String params[] = ImageAreaSelect.strArea.split(" ");
            if (params.length >= 4) {
                display.cutFromSketch[0][1] = params[0];
                display.cutFromSketch[1][1] = params[1];
                display.cutFromSketch[2][1] = params[2];
                display.cutFromSketch[3][1] = params[3];
                if (display.cutFromSketch[6][1].toString().length() == 0) {
                    display.cutFromSketch[6][1] = params[2];
                }
                if (display.cutFromSketch[7][1].toString().length() == 0) {
                    display.cutFromSketch[7][1] = params[3];
                }
            }
        }
    }

    public void setVariablesComboWithEquals(JTable tableCutFromSketch, JTable tableTransformations) {
        comboBox.removeAllItems();
        comboBox.setEditable(true);
        comboBox.addItem("");

        for (String strVar : DataServer.variablesServer.variablesVector) {
            comboBox.addItem("=" + strVar);
        }

        tableCutFromSketch.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(comboBox));
//        tableTransformations.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(comboBox));
    }

    public void setTransformations(JTable tableTransformations) {
        comboBoxTransformations.removeAllItems();
        comboBoxTransformations.setEditable(true);
        comboBoxTransformations.addItem("");

        /*
        comboBoxTransformations.addItem("translate x");
        comboBoxTransformations.addItem("translate y");
        comboBoxTransformations.addItem("scale x");
        comboBoxTransformations.addItem("scale y");
        comboBoxTransformations.addItem("rotate");
        comboBoxTransformations.addItem("shear x");
        comboBoxTransformations.addItem("shear y");
        comboBoxTransformations.addItem("- - - - -");
         */

        for (String strImageFilter : Filters.imageFiltersVector) {
            comboBoxTransformations.addItem(strImageFilter);
        }

        tableTransformations.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(comboBoxTransformations));
    }

    public void load() {
    }

    public static InteractionSpaceFrame frame;

    public static void showFrame(int tabIndex) {
        showFrame();
        frame.targets.setSelectedIndex(tabIndex);
    }

    public static void showFrame() {
        if (frame == null) {
            frame = new InteractionSpaceFrame();
        }
        frame.setState(Frame.NORMAL);
        frame.setLocationRelativeTo(SketchletEditor.editorFrame);
        frame.setVisible(true);
        //frame.toFront();
    }

    public static void closeFrame() {
        if (frame != null && frame.isVisible()) {
            frame.save();
            frame.setVisible(false);
        }
        frame = null;
    }
}
