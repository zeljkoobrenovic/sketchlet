/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui.toolbars;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.data.ActiveRegions;
import net.sf.sketchlet.designer.editor.EditorMode;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.regions.ActiveRegionImageEditor;
import net.sf.sketchlet.designer.editor.tool.stroke.StrokeCombo;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.ui.profiles.Profiles;
import net.sf.sketchlet.designer.ui.region.ActiveRegionPanel;
import net.sf.sketchlet.designer.ui.region.ActiveRegionsFrame;
import net.sf.sketchlet.designer.ui.region.ShapePanel;
import net.sf.sketchlet.pluginloader.WidgetPluginFactory;
import net.sf.sketchlet.util.Colors;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zobrenovic
 */
public class ModeToolbar extends JToolBar {

    ImageIcon[] images;
    String[] modeStrings = {"sketching", "active regions"};
    String[] modeIcons = {"resources/sketching.png", "resources/active_region.png"};
    //JComboBox modeList;
    JLabel labelLayer = new JLabel(" layer:");
    public JButton btnText = new JButton(Workspace.createImageIcon("resources/text.gif"));
    public JButton btnSketching = new JButton(Workspace.createImageIcon("resources/sketching.png"));
    public JButton btnColorPicker = new JButton(Workspace.createImageIcon("resources/color-picker.png"));
    public JButton btnBucket = new JButton(Workspace.createImageIcon("resources/bucket.gif"));
    public JButton btnSelectColor = new JButton(Workspace.createImageIcon("resources/image_transparent_color.png"));
    public JButton btnMagicWand = new JButton(Workspace.createImageIcon("resources/magicwand.gif"));
    public JButton btnLine = new JButton(Workspace.createImageIcon("resources/line_1.png"));
    public JButton btnRect = new JButton(Workspace.createImageIcon("resources/rectangle.png"));
    public JButton btnOval = new JButton(Workspace.createImageIcon("resources/oval.png"));
    public JButton btnSelect = new JButton(Workspace.createImageIcon("resources/select.png"));
    public JButton btnFreeFormSelect = new JButton(Workspace.createImageIcon("resources/select_freeform.png"));
    public JButton btnEraser = new JButton(Workspace.createImageIcon("resources/eraser.png"));
    JButton selectTrajectory = new JButton(Workspace.createImageIcon("resources/hand.png", ""));
    JButton moveTrajectory = new JButton(Workspace.createImageIcon("resources/move_hand.png", ""));
    JButton penTrajectory1 = new JButton(Workspace.createImageIcon("resources/pen_trajectory1.gif", ""));
    JButton penTrajectory2 = new JButton(Workspace.createImageIcon("resources/pen_trajectory2.gif", ""));
    public JButton select = new JButton(Workspace.createImageIcon("resources/arrow_cursor.png", ""));
    public JButton activeRegions = new JButton(Workspace.createImageIcon("resources/active_region.png", ""));
    public JButton connector = new JButton(Workspace.createImageIcon("resources/connector.png", ""));
    JButton details = new JButton(Workspace.createImageIcon("resources/details.gif", ""));
    JButton extract = new JButton("extract", Workspace.createImageIcon("resources/edit-cut.png"));
    JButton stamp = new JButton("stamp", Workspace.createImageIcon("resources/stamp.png"));
    JButton delete = new JButton("", Workspace.createImageIcon("resources/user-trash.png"));
    JMenuItem group = new JMenuItem("group", Workspace.createImageIcon("resources/system-users.png"));
    JButton more = new JButton(Workspace.createImageIcon("resources/right_mouse.png"));
    JButton outline = new JButton(Workspace.createImageIcon("resources/draw-cube.png"));
    // JButton reset = new JButton(Workspace.createImageIcon("resources/no_limits.png", ""));
    public String[] layerStrings = {"sketch", "annotation"};
    public JComboBox layerList = new JComboBox(layerStrings);
    //public JCheckBox showAnnotation = new JCheckBox(" show annotation", true);
    JButton clear = new JButton(Workspace.createImageIcon("resources/edit-clear.png"));
    JButton fromFile = new JButton(Workspace.createImageIcon("resources/import.gif"));
    JButton editor = new JButton(Workspace.createImageIcon("resources/imageeditor.gif"));
    JButton refresh = new JButton(Workspace.createImageIcon("resources/view-refresh.png"));
    JButton refreshPlayback = new JButton(Workspace.createImageIcon("resources/view-refresh.png"));

    public ModeToolbar() {
        setFloatable(false);
        setBorder(BorderFactory.createEmptyBorder());
        GridLayout grid = new GridLayout(0, 1);
        grid.setHgap(0);
        grid.setVgap(0);
        setLayout(grid);

        //this.setPreferredSize(new Dimension(45, 360));

        this.setOrientation(JToolBar.VERTICAL);

        this.createModeCombo();
        this.createSketchingPanel();
        this.createRegionsPanel();
    }

    private void createSketchingPanel() {

        layerList.setBackground(getBackground());
        layerList.setSelectedIndex(0);
        layerList.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                SketchletEditor.editorPanel.layer = layerList.getSelectedIndex();
                SketchletEditor.editorPanel.createGraphics();
                SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.penTool, null);
                SketchletEditor.editorPanel.repaint();
            }
        });

        layerList.setPreferredSize(new Dimension(80, 28));
        btnSketching.setToolTipText(Language.translate("Drawing Pencil, Shortcut Key: D"));
        btnSketching.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.editorPanel.setMode(EditorMode.SKETCHING);
                SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.penTool, btnSketching);
                SketchletEditor.editorPanel.createGraphics();
                SketchletEditor.editorPanel.setCursor();

                if (ActiveRegionPanel.currentActiveRegionPanel != null) {
                    ActiveRegionImageEditor fhs = ActiveRegionPanel.currentActiveRegionPanel.imageEditor;
                    fhs.setTool(fhs.penTool, null);
                }

                SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("editor_mode_drawing");
            }
        });
        btnColorPicker.setToolTipText(Language.translate("Color Picker, Shortcut Key: P"));
        btnColorPicker.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.editorPanel.setMode(EditorMode.SKETCHING);
                SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.colorPickerTool, btnColorPicker);
                SketchletEditor.editorPanel.createGraphics();
                SketchletEditor.editorPanel.setCursor();
                if (ActiveRegionPanel.currentActiveRegionPanel != null) {
                    ActiveRegionImageEditor fhs = ActiveRegionPanel.currentActiveRegionPanel.imageEditor;
                    fhs.setTool(fhs.colorPickerTool, btnColorPicker);
                }
                SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("editor_mode_drawing");
            }
        });
        btnSelectColor.setToolTipText(Language.translate("Select Transparent Color"));
        btnSelectColor.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.editorPanel.setMode(EditorMode.SKETCHING);
                SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.transparentColorTool, btnSelectColor);
                SketchletEditor.editorPanel.createGraphics();
                SketchletEditor.editorPanel.setCursor();
                if (ActiveRegionPanel.currentActiveRegionPanel != null) {
                    ActiveRegionImageEditor fhs = ActiveRegionPanel.currentActiveRegionPanel.imageEditor;
                    fhs.setTool(fhs.transparentColorTool, btnSelectColor);
                }
                SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("editor_mode_drawing");
            }
        });
        btnBucket.setToolTipText(Language.translate("Bucket, Shortcut Key: B"));
        btnBucket.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.editorPanel.setMode(EditorMode.SKETCHING);
                SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.bucketTool, btnBucket);
                SketchletEditor.editorPanel.createGraphics();
                SketchletEditor.editorPanel.setCursor();
                if (ActiveRegionPanel.currentActiveRegionPanel != null) {
                    ActiveRegionImageEditor fhs = ActiveRegionPanel.currentActiveRegionPanel.imageEditor;
                    fhs.setTool(fhs.bucketTool, btnBucket);
                }
                SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("editor_mode_drawing");
            }
        });

        btnMagicWand.setToolTipText(Language.translate("Magic Wand, Shortcut Key: M"));
        btnMagicWand.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.editorPanel.setMode(EditorMode.SKETCHING);
                SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.magicWandTool, btnMagicWand);
                SketchletEditor.editorPanel.createGraphics();
                SketchletEditor.editorPanel.setCursor();
                if (ActiveRegionPanel.currentActiveRegionPanel != null) {
                    ActiveRegionImageEditor fhs = ActiveRegionPanel.currentActiveRegionPanel.imageEditor;
                    fhs.setTool(fhs.magicWandTool, btnMagicWand);
                }
                SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("editor_mode_drawing");
            }
        });

        btnLine.setToolTipText(Language.translate("Line, Shortcut Key: L"));
        btnLine.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.editorPanel.setMode(EditorMode.SKETCHING);
                SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.lineTool, btnLine);
                SketchletEditor.editorPanel.createGraphics();
                SketchletEditor.editorPanel.setCursor();
                if (ActiveRegionPanel.currentActiveRegionPanel != null) {
                    ActiveRegionImageEditor fhs = ActiveRegionPanel.currentActiveRegionPanel.imageEditor;
                    fhs.setTool(fhs.lineTool, btnLine);
                }
                SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("editor_mode_drawing");
            }
        });
        btnRect.setToolTipText(Language.translate("Rectangle, Shortcut Key: R"));
        btnRect.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.editorPanel.setMode(EditorMode.SKETCHING);
                SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.rectTool, btnRect);
                SketchletEditor.editorPanel.createGraphics();
                SketchletEditor.editorPanel.setCursor();
                if (ActiveRegionPanel.currentActiveRegionPanel != null) {
                    ActiveRegionImageEditor fhs = ActiveRegionPanel.currentActiveRegionPanel.imageEditor;
                    fhs.setTool(fhs.rectTool, btnRect);
                }
                SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("editor_mode_drawing");
            }
        });
        btnOval.setToolTipText(Language.translate("Oval, Shortcut Key: O"));
        btnOval.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.editorPanel.setMode(EditorMode.SKETCHING);
                SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.ovalTool, btnOval);
                SketchletEditor.editorPanel.createGraphics();
                SketchletEditor.editorPanel.setCursor();
                if (ActiveRegionPanel.currentActiveRegionPanel != null) {
                    ActiveRegionImageEditor fhs = ActiveRegionPanel.currentActiveRegionPanel.imageEditor;
                    fhs.setTool(fhs.ovalTool, btnOval);
                }
                SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("editor_mode_drawing");
            }
        });
        btnSelect.setToolTipText(Language.translate("Select, Shortcut Key: S"));
        btnSelect.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.editorPanel.setMode(EditorMode.SKETCHING);
                SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.selectTool, btnSelect);
                SketchletEditor.editorPanel.createGraphics();
                SketchletEditor.editorPanel.setCursor();
                if (ActiveRegionPanel.currentActiveRegionPanel != null) {
                    ActiveRegionImageEditor fhs = ActiveRegionPanel.currentActiveRegionPanel.imageEditor;
                    fhs.setTool(fhs.selectTool, btnSelect);
                }
                SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("editor_mode_drawing");
            }
        });
        btnFreeFormSelect.setToolTipText(Language.translate("Free-Form Select, Shortcut Key: F"));
        btnFreeFormSelect.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.editorPanel.setMode(EditorMode.SKETCHING);
                SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.freeFormSelectTool, btnFreeFormSelect);
                SketchletEditor.editorPanel.createGraphics();
                SketchletEditor.editorPanel.setCursor();
                if (ActiveRegionPanel.currentActiveRegionPanel != null) {
                    ActiveRegionImageEditor fhs = ActiveRegionPanel.currentActiveRegionPanel.imageEditor;
                    fhs.setTool(fhs.freeFormSelectTool, btnFreeFormSelect);
                }
                SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("editor_mode_drawing");
            }
        });

        btnEraser.setToolTipText(Language.translate("Eraser, Shortcut Key: E"));
        btnEraser.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JButton button = (JButton) e.getSource();
                SketchletEditor.editorPanel.setMode(EditorMode.SKETCHING);
                SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.eraserTool, btnEraser);
                SketchletEditor.editorPanel.createGraphics();
                SketchletEditor.editorPanel.setCursor();
                if (ActiveRegionPanel.currentActiveRegionPanel != null) {
                    ActiveRegionImageEditor fhs = ActiveRegionPanel.currentActiveRegionPanel.imageEditor;
                    fhs.setTool(fhs.eraserTool, btnEraser);
                }
                SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("editor_mode_drawing");
            }
        });
        editor.setToolTipText(Language.translate("External image editor"));
        refresh.setToolTipText(Language.translate("Refresh image"));
        refreshPlayback.setToolTipText(Language.translate("Restart"));

        editor.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.editorPanel.openExternalEditor();
            }
        });

        refresh.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.editorPanel.refresh();
            }
        });

        refreshPlayback.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.editorPanel.refreshPlayback();
            }
        });

        addComponents(SketchletEditor.editorPanel.mode);
    }

    public static JMenu getWidgetMenu() {
        JMenu widgetMenu = new JMenu(Language.translate("Widget"));
        widgetMenu.setIcon(Workspace.createImageIcon("resources/widget.png"));
        String ctrls[][] = WidgetPluginFactory.getWidgetListWithGroups();
        Map<String, JMenu> hash = new HashMap<String, JMenu>();
        for (int i = 0; i < ctrls.length; i++) {
            final String widgetGroup = ctrls[i][0];
            final String widgetName = ctrls[i][1];
            JMenu groupMenu = hash.get(widgetGroup);
            if (StringUtils.isNotBlank(widgetGroup) && groupMenu == null) {
                groupMenu = new JMenu(widgetGroup);
                hash.put(widgetGroup, groupMenu);
                widgetMenu.add(groupMenu);
            }
            JMenuItem menuItem = new JMenuItem(widgetName);
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    SketchletEditor.editorPanel.setWidget(widgetName);
                }
            });
            if (groupMenu == null) {
                widgetMenu.add(menuItem);
            } else {
                groupMenu.add(menuItem);
            }
        }


        return widgetMenu;
    }

    public static Object createAppearanceMenu(boolean bPopup) {
        JMenu colorsMenu = new JMenu(Language.translate("Line Color"));
        colorsMenu.setIcon(Workspace.createImageIcon("resources/palette_line.png"));
        JMenu colorsFill = new JMenu(Language.translate("Fill Color"));
        colorsFill.setIcon(Workspace.createImageIcon("resources/palette_fill.png"));
        JMenu colorsText = new JMenu(Language.translate("Text Color"));
        colorsText.setIcon(Workspace.createImageIcon("resources/color_text.gif"));
        JMenu transparency = new JMenu(Language.translate("Transparency"));
        transparency.setIcon(Workspace.createImageIcon("resources/transparency.png"));
        JMenu speed = new JMenu(Language.translate("Speed"));
        speed.setIcon(Workspace.createImageIcon("resources/speed.png"));
        //JMenu rotationSpeed = new JMenu("Rotation Speed");
        //rotationSpeed.setIcon(Workspace.createImageIcon("resources/restart.gif"));
        JMenu horizontal3DRotation = new JMenu(Language.translate("Horizontal 3D Rotation"));
        horizontal3DRotation.setIcon(Workspace.createImageIcon("resources/restart.gif"));
        JMenu vertical3DRotation = new JMenu(Language.translate("Vertical 3D Rotation"));
        vertical3DRotation.setIcon(Workspace.createImageIcon("resources/restart.gif"));
        JMenu weights = new JMenu(Language.translate("Line Weight"));
        weights.setIcon(Workspace.createImageIcon("resources/line-weight.png"));
        JMenu penWeights = new JMenu(Language.translate("Pen Thickness"));
        penWeights.setIcon(Workspace.createImageIcon("resources/line-weight.png"));
        JMenu dashes = new JMenu(Language.translate("Line Style"));
        dashes.setIcon(Workspace.createImageIcon("resources/line-style.png"));

        Color colors[] = Colors.getStandardColors();
        String colorNames[] = Colors.getStandardColorNames();
        for (int i = 0; i < colors.length; i++) {
            final String colorName = colorNames[i];
            JMenuItem menuItem = new JMenuItem(colorName, createImageIcon(colors[i], 70, 20));
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    SketchletEditor.editorPanel.setLineColor(colorName);
                }
            });
            colorsMenu.add(menuItem);
        }
        colorsMenu.addSeparator();
        JMenuItem _menuItem = new JMenuItem(Language.translate("more colors"), createImageIconMore(70, 20));
        _menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                Color newColor = JColorChooser.showDialog(
                        SketchletEditor.editorPanel.editorPanel,
                        Language.translate("Choose Color"),
                        SketchletEditor.editorPanel.color);

                if (newColor != null) {
                    SketchletEditor.editorPanel.setLineColor(newColor.getRed() + " " + newColor.getGreen() + " " + newColor.getBlue());
                }
            }
        });
        colorsMenu.add(_menuItem);
        _menuItem = new JMenuItem(Language.translate("none"));
        _menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setLineColor("");
            }
        });
        colorsMenu.add(_menuItem);

        for (int i = 0; i < colors.length; i++) {
            final String colorName = colorNames[i];
            JMenuItem menuItem = new JMenuItem(colorName, createImageIcon(colors[i], 70, 20));
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    SketchletEditor.editorPanel.setFillColor(colorName);
                }
            });
            colorsFill.add(menuItem);
        }
        _menuItem = new JMenuItem(Language.translate("more colors"), createImageIconMore(70, 20));
        _menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                Color newColor = JColorChooser.showDialog(
                        SketchletEditor.editorPanel.editorPanel,
                        Language.translate("Choose Color"),
                        SketchletEditor.editorPanel.color);

                if (newColor != null) {
                    SketchletEditor.editorPanel.setFillColor(newColor.getRed() + " " + newColor.getGreen() + " " + newColor.getBlue());
                }
            }
        });

        colorsFill.addSeparator();
        colorsFill.add(_menuItem);
        _menuItem = new JMenuItem(Language.translate("none"));
        _menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setFillColor("");
            }
        });
        colorsFill.add(_menuItem);

        for (int i = 0; i < colors.length; i++) {
            final String colorName = colorNames[i];
            JMenuItem menuItem = new JMenuItem(colorName, createImageIcon(colors[i], 70, 20));
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    SketchletEditor.editorPanel.setTextColor(colorName);
                }
            });
            colorsText.add(menuItem);
        }
        _menuItem = new JMenuItem(Language.translate("more colors"), createImageIconMore(70, 20));
        _menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                Color newColor = JColorChooser.showDialog(
                        SketchletEditor.editorPanel.editorPanel,
                        Language.translate("Choose Color"),
                        SketchletEditor.editorPanel.color);

                if (newColor != null) {
                    SketchletEditor.editorPanel.setTextColor(newColor.getRed() + " " + newColor.getGreen() + " " + newColor.getBlue());
                }
            }
        });
        colorsText.addSeparator();
        colorsText.add(_menuItem);
        _menuItem = new JMenuItem(Language.translate("none"));
        _menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setTextColor("");
            }
        });
        colorsText.add(_menuItem);

        for (float i = 0.0f; i < 1.1; i += 0.1) {
            final String strTransparency = "" + i;
            final double t = i;
            JMenuItem menuItem = new JMenuItem(strTransparency.substring(0, 3), createImageIcon(new Color(0.0f, 0.0f, 0.0f, i), 70, 20));
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    SketchletEditor.editorPanel.setTransparency((float) t);
                }
            });
            transparency.add(menuItem);
        }

        for (int i = 0; i <= 100; i += 10) {
            final String strSpeed = "" + i;
            final int s = i;
            JMenuItem menuItem = new JMenuItem(strSpeed);
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    SketchletEditor.editorPanel.setSpeed(s);
                }
            });
            speed.add(menuItem);
        }
        /*
        for (int i = 0; i <= 360; i += 30) {
        final String strRotSpeed = "" + i;
        final int s = i;
        JMenuItem menuItem = new JMenuItem(strRotSpeed);
        menuItem.addActionListener(new ActionListener() {
        
        public void actionPerformed(ActionEvent event) {
        SketchletEditor.editorPanel.setRotationSpeed(s);
        }
        });
        rotationSpeed.add(menuItem);
        }
         */
        for (int i = 0; i <= 360; i += 30) {
            final String strRot = "" + i;
            final int s = i;
            JMenuItem menuItem = new JMenuItem(strRot);
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    SketchletEditor.editorPanel.setHorizontal3DRotation(s);
                }
            });
            horizontal3DRotation.add(menuItem);
        }

        for (int i = 0; i <= 360; i += 30) {
            final String strRot = "" + i;
            final int s = i;
            JMenuItem menuItem = new JMenuItem(strRot);
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    SketchletEditor.editorPanel.setVertical3DRotation(s);
                }
            });
            vertical3DRotation.add(menuItem);
        }

        for (int i = 0; i <= 10; i++) {
            final String strThickness = "" + (i == 0 ? "None" : i);
            JMenuItem menuItem = new JMenuItem(strThickness, createImageIcon(i, 70, 20));
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    SketchletEditor.editorPanel.setLineThickness(strThickness);
                }
            });
            weights.add(menuItem);
        }

        for (int i = 0; i < 10; i++) {
            final String strThickness = "" + (i == 0 ? "None" : i);
            JMenuItem menuItem = new JMenuItem(strThickness, createImageIcon(i + 1, 70, 20));
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    SketchletEditor.editorPanel.setPenLineThickness(strThickness);
                }
            });
            penWeights.add(menuItem);
        }

        for (int i = 0; i < StrokeCombo.strokeIDs.length; i++) {
            final String strStroke = StrokeCombo.strokeIDs[i];
            JMenuItem menuItem = new JMenuItem(StrokeCombo.createImageIcon(ColorToolbar.getStroke(strStroke, 3.0f), 70, 20));
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    SketchletEditor.editorPanel.setLineStyle(strStroke);
                }
            });
            dashes.add(menuItem);
        }
        JMenu shapeMenu = getShapeMenu();

        JMenu align = new JMenu(Language.translate("Alignment"));
        align.setIcon(Workspace.createImageIcon("resources/center.gif"));
        JMenuItem alignLeft = new JMenuItem(Language.translate("Left"), Workspace.createImageIcon("resources/left.gif"));
        alignLeft.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setHorizontalAlignment("left");
            }
        });
        JMenuItem alignCenter = new JMenuItem(Language.translate("Center"), Workspace.createImageIcon("resources/center.gif"));
        alignCenter.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setHorizontalAlignment("center");
            }
        });
        JMenuItem alignRight = new JMenuItem(Language.translate("Right"), Workspace.createImageIcon("resources/right.gif"));
        alignRight.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setHorizontalAlignment("right");
            }
        });
        JMenuItem alignTop = new JMenuItem(Language.translate("Top"), Workspace.createImageIcon("resources/align-top.png"));
        alignTop.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setVerticalAlignment("top");
            }
        });
        JMenuItem alignMiddle = new JMenuItem(Language.translate("Middle"), Workspace.createImageIcon("resources/align-centered.png"));
        alignMiddle.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setVerticalAlignment("center");
            }
        });
        JMenuItem alignBottom = new JMenuItem(Language.translate("Bottom"), Workspace.createImageIcon("resources/align-bottom.png"));
        alignBottom.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setVerticalAlignment("bottom");
            }
        });

        align.add(alignLeft);
        align.add(alignCenter);
        align.add(alignRight);
        align.addSeparator();
        align.add(alignTop);
        align.add(alignMiddle);
        align.add(alignBottom);

        if (SketchletEditor.editorPanel.editorPanel != null && SketchletEditor.editorPanel.currentPage != null && SketchletEditor.editorPanel.currentPage.regions != null && SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null && SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            ActiveRegion action = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
            if (action.shape.isEmpty() || action.shape.equalsIgnoreCase("none")) {
                colorsFill.setEnabled(false);
                // colorsMenu.setEnabled(false);
                //weights.setEnabled(false);
                //dashes.setEnabled(false);
            }
        }

        JMenu autoPerspective = new JMenu(Language.translate("Automatic Perspective"));
        autoPerspective.setIcon(Workspace.createImageIcon("resources/perspective_lines.png"));
        JMenuItem perspectiveFront = new JMenuItem("Front", Workspace.createImageIcon("resources/perspective_lines.png"));
        perspectiveFront.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setAutomaticPerspective("front");
            }
        });
        autoPerspective.add(perspectiveFront);
        JMenuItem perspectiveLeft = new JMenuItem(Language.translate("Left"), Workspace.createImageIcon("resources/perspective_left.png"));
        perspectiveLeft.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setAutomaticPerspective("left");
            }
        });
        autoPerspective.add(perspectiveLeft);
        JMenuItem perspectiveRight = new JMenuItem(Language.translate("Right"), Workspace.createImageIcon("resources/perspective_right.png"));
        perspectiveRight.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setAutomaticPerspective("right");
            }
        });
        autoPerspective.add(perspectiveRight);
        JMenuItem perspectiveBottom = new JMenuItem(Language.translate("Bottom"), Workspace.createImageIcon("resources/perspective_bottom.png"));
        perspectiveBottom.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setAutomaticPerspective("bottom");
            }
        });
        autoPerspective.add(perspectiveBottom);
        JMenuItem perspectiveTop = new JMenuItem(Language.translate("Top"), Workspace.createImageIcon("resources/perspective_top.png"));
        perspectiveTop.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setAutomaticPerspective("top");
            }
        });
        autoPerspective.add(perspectiveTop);

        JMenu perspectiveDepth = new JMenu(Language.translate("Perspective Depth"));
        perspectiveDepth.setIcon(Workspace.createImageIcon("resources/perspective_lines.png"));

        for (double di = 0.0; di <= 1.0; di += 0.1) {
            final String strDepth = ("" + di).substring(0, 3);
            JMenuItem menuItem = new JMenuItem(strDepth, createImageIcon(new Color(0.0f, 0.0f, 0.0f, 0.5f), (int) ((1 - di) * 25 + 2), (int) ((1 - di) * 25 + 2)));
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    SketchletEditor.editorPanel.setPerspectiveDepth(strDepth);
                }
            });
            perspectiveDepth.add(menuItem);
        }

        if (bPopup) {
            final JPopupMenu lineMenu = new JPopupMenu();
            TutorialPanel.prepare(lineMenu);

            lineMenu.add(shapeMenu);
            lineMenu.addSeparator();
            lineMenu.add(colorsFill);
            lineMenu.add(colorsMenu);
            lineMenu.add(colorsText);
            lineMenu.addSeparator();
            lineMenu.add(transparency);
            lineMenu.addSeparator();
            lineMenu.add(weights);
            lineMenu.add(dashes);
            lineMenu.addSeparator();
            lineMenu.add(penWeights);
            lineMenu.addSeparator();
            lineMenu.add(horizontal3DRotation);
            lineMenu.add(vertical3DRotation);
            lineMenu.addSeparator();
            lineMenu.add(speed);
            //lineMenu.add(rotationSpeed);
            lineMenu.addSeparator();
            lineMenu.add(align);
            lineMenu.addSeparator();
            lineMenu.add(autoPerspective);
            lineMenu.add(perspectiveDepth);
            return lineMenu;
        } else {
            JMenu menu = new JMenu(Language.translate("Appearance"));
            menu.setIcon(Workspace.createImageIcon("resources/draw-cube.png"));
            menu.add(shapeMenu);
            menu.addSeparator();
            menu.add(colorsFill);
            menu.add(colorsMenu);
            menu.add(colorsText);
            menu.addSeparator();
            menu.add(transparency);
            menu.addSeparator();
            menu.add(weights);
            menu.add(dashes);
            menu.addSeparator();
            menu.add(penWeights);
            menu.addSeparator();
            menu.add(horizontal3DRotation);
            menu.add(vertical3DRotation);
            menu.addSeparator();
            menu.add(speed);
            //menu.add(rotationSpeed);
            menu.addSeparator();
            menu.add(align);
            menu.addSeparator();
            menu.add(autoPerspective);
            menu.add(perspectiveDepth);
            return menu;
        }
    }

    public static JMenu getShapeMenu() {
        JMenu shapeMenu = new JMenu(Language.translate("Shape"));
        shapeMenu.setIcon(Workspace.createImageIcon("resources/shapes.png"));
        JMenuItem shapeNone = new JMenuItem(Language.translate("No Shape"), Workspace.createImageIcon("resources/no_shape.png"));
        JMenuItem shapeRect = new JMenuItem(Language.translate("Rectangle"), Workspace.createImageIcon("resources/rectangle.png"));
        JMenuItem shapeOval = new JMenuItem("Oval", Workspace.createImageIcon("resources/oval.png"));
        JMenuItem shapeRoundRect = new JMenuItem(Language.translate("Rounded Rectangle"), Workspace.createImageIcon("resources/rounded_rectangle.png"));
        JMenuItem shapeTriangle1 = new JMenuItem(Language.translate("Triangle"), Workspace.createImageIcon("resources/triangle_1.png"));
        JMenuItem shapeTriangle2 = new JMenuItem(Language.translate("Triangle"), Workspace.createImageIcon("resources/triangle_2.png"));
        JMenuItem shapeLine1 = new JMenuItem(Language.translate("Line"), Workspace.createImageIcon("resources/line_1.png"));
        JMenuItem shapeLine2 = new JMenuItem(Language.translate("Line"), Workspace.createImageIcon("resources/line_2.png"));
        JMenuItem shapeLine3 = new JMenuItem(Language.translate("Horizontal Line"), Workspace.createImageIcon("resources/line_3.png"));
        JMenuItem shapeLine4 = new JMenuItem(Language.translate("Vertical Line"), Workspace.createImageIcon("resources/line_4.png"));
        JMenuItem pieSlice = new JMenuItem(Language.translate("Pie Slice"), Workspace.createImageIcon("resources/pie_slice.png"));
        shapeNone.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setShape("None", 0);
            }
        });
        shapeRect.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setShape("Rectangle", 1);
            }
        });
        shapeOval.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setShape("Oval", 2);
            }
        });
        shapeRoundRect.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setShape("Rounded Rectangle", 3);
            }
        });
        shapeTriangle1.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setShape("Triangle 1", 4);
            }
        });
        shapeTriangle2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setShape("Triangle 2", 5);
            }
        });
        shapeLine1.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setShape("Line 1", 6);
            }
        });
        shapeLine2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setShape("Line 2", 7);
            }
        });
        shapeLine3.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setShape("Horizontal Line", 8);
            }
        });
        shapeLine4.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setShape("Vertical Line", 9);
            }
        });
        pieSlice.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setShape("Pie Slice", 22);
            }
        });
        shapeMenu.add(shapeNone);
        shapeMenu.addSeparator();
        shapeMenu.add(shapeRect);
        shapeMenu.add(shapeOval);
        shapeMenu.add(shapeRoundRect);
        shapeMenu.addSeparator();
        shapeMenu.add(shapeTriangle1);
        shapeMenu.add(shapeTriangle2);
        shapeMenu.addSeparator();
        shapeMenu.add(shapeLine1);
        shapeMenu.add(shapeLine2);
        shapeMenu.add(shapeLine3);
        shapeMenu.add(shapeLine4);
        shapeMenu.addSeparator();
        JMenu polygons = new JMenu(Language.translate("Regular Polygons"));
        polygons.setIcon(ShapePanel.createRegularShapeIcon(5));
        for (int mi = 10; mi < 15; mi++) {
            JMenuItem regPol = new JMenuItem(ShapePanel.getShapeStrings()[mi], ShapePanel.shapeIcons[mi]);
            final String strID = ShapePanel.shapeIDs[mi];
            final int _mi = mi;
            regPol.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    SketchletEditor.editorPanel.setShape(strID, _mi);
                }
            });
            polygons.add(regPol);
        }
        JMenu stars = new JMenu(Language.translate("Stars"));
        stars.setIcon(ShapePanel.createStarShapeIcon(5));
        for (int mi = 15; mi < 22; mi++) {
            JMenuItem regPol = new JMenuItem(ShapePanel.getShapeStrings()[mi], ShapePanel.shapeIcons[mi]);
            final String strID = ShapePanel.shapeIDs[mi];
            final int _mi = mi;
            regPol.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    SketchletEditor.editorPanel.setShape(strID, _mi);
                }
            });
            stars.add(regPol);
        }
        shapeMenu.add(polygons);
        shapeMenu.add(stars);
        shapeMenu.addSeparator();
        shapeMenu.add(pieSlice);

        return shapeMenu;
    }

    public void createRegionsPanel() {
        select.setToolTipText(Language.translate("Select Regions Tool, Shortcut Key: ESC"));
        select.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.editorPanel.setMode(EditorMode.ACTIONS);
                SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("tool_active_region_select");
                SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.activeRegionSelectTool, select);
            }
        });
        activeRegions.setToolTipText(Language.translate("New Active Regions Tool, Shortcut Key: A"));
        activeRegions.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.initProperties = null;
                SketchletEditor.editorPanel.setMode(EditorMode.ACTIONS);
                SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("tool_active_region_new");
                SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.activeRegionTool, activeRegions);
            }
        });
        connector.setToolTipText(Language.translate("Active Regions Connector Tool, Shortcut Key: C"));
        connector.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.initProperties = null;
                SketchletEditor.editorPanel.setMode(EditorMode.ACTIONS);
                SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("tool_active_region_connector");
                SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.activeRegionConnectorTool, connector);
            }
        });

        selectTrajectory.setToolTipText(Language.translate("Edit trajectory points"));
        selectTrajectory.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.trajectoryPointsTool, selectTrajectory);
            }
        });

        moveTrajectory.setToolTipText(Language.translate("Move trajectory (press CTRL to move both trajectories)"));
        moveTrajectory.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.trajectoryMoveTool, moveTrajectory);
            }
        });

        penTrajectory1.setToolTipText(Language.translate("Add new points to the primary trajectory"));
        penTrajectory1.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.trajectory1PointsTool, penTrajectory1);
            }
        });

        penTrajectory2.setToolTipText(Language.translate("Add new points to the secondary trajectory"));
        penTrajectory2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.trajectory2PointsTool, penTrajectory2);
            }
        });

        details.setToolTipText(Language.translate("Properties of the selected region"));
        details.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ActiveRegionsFrame.showRegionsAndActions();
                SketchletEditor.editorPanel.modeToolbar.enableControls();
            }
        });

        extract.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.extract(0);
                enableControls();
            }
        });

        stamp.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.stamp();
                enableControls();
            }
        });

        delete.setToolTipText(Language.translate("Delete the active region"));
        delete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.deleteSelectedRegion();
                enableControls();
            }
        });
        group.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.groupSelectedRegion();
                enableControls();
            }
        });

        clear.setToolTipText(Language.translate("Clear image"));
        clear.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.editorPanel.clearAll();
            }
        });

        fromFile.setToolTipText(Language.translate("import an image from a file (JPG, GIF, PNG, PDF)"));
        fromFile.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.editorPanel.fromFile();
            }
        });

        more.setToolTipText(Language.translate("More commands (also available on right mouse click on the region)"));
        more.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JPopupMenu popupMenu = SketchletEditor.editorPanel.regionPopupListener.getPopupMenu(false);

                popupMenu.show(more.getParent(), more.getX(), more.getY() + more.getHeight());
                enableControls();
            }
        });

        btnText.setToolTipText(Language.translate("Edit the text in the region"));
        btnText.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ActiveRegionsFrame.showRegionsAndActionsImage(4);
            }
        });

        outline.setToolTipText(Language.translate("Set the appearance (shape, color...) of the selected active region"));
        outline.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JPopupMenu lineMenu = (JPopupMenu) ModeToolbar.createAppearanceMenu(true);
                lineMenu.show(outline.getParent(), outline.getX(), outline.getY() + outline.getHeight());
                enableControls();
            }
        });

        /*reset.setToolTipText("Reset the regions settings (position limits...)");
        reset.addActionListener(new ActionListener() {
        
        public void actionPerformed(ActionEvent e) {
        for (ActiveRegion a : SketchletEditor.editorPanel.currentSketch.actions.selectedActions) {
        a.limits[0][1] = "";
        a.limits[0][2] = "";
        a.limits[1][1] = "";
        a.limits[1][2] = "";
        a.rotation = 0.0;
        a.trajectory1.setText("");
        a.shearX = 0.0;
        a.shearY = 0.0;
        a.p_x0 = 0.0;
        a.p_y0 = 0.0;
        a.p_x1 = 1.0;
        a.p_y1 = 0.0;
        a.p_x2 = 1.0;
        a.p_y2 = 1.0;
        a.p_x3 = 0.0;
        a.p_y3 = 1.0;
        a.center_rotation_x = 0.5;
        a.center_rotation_y = 0.5;
        
        a.imageIndex.setSelectedItem("");
        a.animationMs.setSelectedItem("");
        
        a.resetProperties();
        for (int i = 0; i < a.mouseEvents.length; i++) {
        for (int j = 0; j < a.mouseEvents[i].length; j++) {
        a.mouseEvents[i][j] = "";
        }
        }
        for (int i = 0; i < a.interactionEvents.length; i++) {
        for (int j = 0; j < a.interactionEvents[i].length; j++) {
        a.interactionEvents[i][j] = "";
        }
        }
        
        SketchletEditor.editorPanel.repaint();
        }
        }
        });*/
    }

    public static ImageIcon createImageIcon(int weight, int w, int h) {
        BufferedImage img = Workspace.createCompatibleImage(w, h);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(weight));
        g2.setColor(new Color(255, 255, 255, 120));
        g2.fillRect(0, 0, w, h);
        g2.setColor(Color.BLACK);
        g2.drawLine(0, h / 2, w, h / 2);
        g2.dispose();
        return new ImageIcon(img);
    }

    public static ImageIcon createImageIcon(Color color, int w, int h) {
        BufferedImage img = Workspace.createCompatibleImage(w, h);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fillRect(0, 0, w, h);
        g2.dispose();
        return new ImageIcon(img);
    }

    public static ImageIcon createImageIconMore(int w, int h) {
        BufferedImage img = Workspace.createCompatibleImage(w, h);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.BLACK);
        g2.drawString("...", 15, h - 5);
        g2.dispose();
        return new ImageIcon(img);
    }

    public void addComponents(EditorMode mode) {
        this.removeAll();
        if (mode == EditorMode.SKETCHING || mode == EditorMode.ACTIONS) {
            if (Profiles.isActive("active_regions_layer")) {
                add(select);
                add(activeRegions);
                add(connector);
                addSeparator();
            }
            // add(labelLayer);
            // add(layerList);
            // add(showAnnotation);
            //if (SketchletEditor.editorPanel.layer == 0) {
            //   btnSketching.setIcon(Workspace.createImageIcon("resources/sketching.png"));
            //} else {
            btnSketching.setIcon(Workspace.createImageIcon("resources/pencil_annotate.png"));
            //}
            add(btnSketching);
            add(btnLine);
            add(btnRect);
            add(btnOval);
            add(btnBucket);
            add(btnColorPicker);
            add(btnSelectColor);
            // add(fromFile);
            //addSeparator();
            //addSeparator();
            //addSeparator();
            add(btnEraser);
            add(btnMagicWand);
            add(btnSelect);
            add(btnFreeFormSelect);
            /*addSeparator();
            add(editorPanel);
            add(refresh);
            addSeparator();
            add(clear);*/
        } else if (mode == EditorMode.PREVIEW) {
            add(refreshPlayback);
        } else if (mode == EditorMode.TRAJECTORY) {
            add(this.selectTrajectory);
            add(this.moveTrajectory);
            add(penTrajectory1);
            add(penTrajectory2);
        }

        TutorialPanel.prepare((Container) this);

        revalidate();
        repaint();
    }

    public void createModeCombo() {
        images = new ImageIcon[modeStrings.length];
        Integer[] intArray = new Integer[modeStrings.length];
        for (int i = 0; i < modeStrings.length; i++) {
            intArray[i] = new Integer(i);
            images[i] = Workspace.createImageIcon(modeIcons[i]);
            if (images[i] != null) {
                images[i].setDescription("  " + modeStrings[i]);
            }
        }

        //Create the combo box.
        // modeList = new JComboBox(intArray);
        // modeList.setBackground(this.getBackground());

        /*
        ComboBoxRenderer renderer = new ComboBoxRenderer();
        renderer.setPreferredSize(new Dimension(95, 25));
        modeList.setRenderer(renderer);
        modeList.setMaximumRowCount(2);
         */
        // add(new JLabel(" mode:"));
        // add(modeList);
    }

    public void enableControls() {
        ActiveRegions actions = SketchletEditor.editorPanel.currentPage.regions;
        boolean bEnable = actions.selectedRegions != null && actions.selectedRegions.size() > 0;
        details.setEnabled(bEnable);
        extract.setEnabled(bEnable);
        stamp.setEnabled(bEnable);
        delete.setEnabled(bEnable);
        more.setEnabled(bEnable);
        outline.setEnabled(bEnable);
        // reset.setEnabled(bEnable);
        btnText.setEnabled(bEnable);

        if (SketchletEditor.editorPanel.currentPage == null || SketchletEditor.editorPanel.currentPage.regions.selectedRegions == null) {
            group.setEnabled(bEnable);
        } else {
            boolean bGroup = false;
            for (ActiveRegion as : SketchletEditor.editorPanel.currentPage.regions.selectedRegions) {
                if (as.regionGrouping.equals("") || !as.regionGrouping.equals(SketchletEditor.editorPanel.currentPage.regions.selectedRegions.firstElement().regionGrouping)) {
                    bGroup = true;
                    break;
                }
            }
            if (bGroup) {
                group.setText("group");
            } else {
                group.setText("ungroup");
            }

            if (bGroup) {
                group.setEnabled(actions.selectedRegions != null && actions.selectedRegions.size() > 1);
            } else {
                group.setEnabled(actions.selectedRegions != null && actions.selectedRegions.size() > 0);
            }
        }
    }

    class ComboBoxRenderer extends JLabel
            implements ListCellRenderer {

        private Font uhOhFont;

        public ComboBoxRenderer() {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
        }

        /*
         * This method finds the images and text corresponding
         * to the selected value1 and returns the label, set up
         * to display the text and images.
         */
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            //Get the selected index. (The index param isn't
            //always valid, so just use the value1.)
            int selectedIndex = ((Integer) value).intValue();

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            //Set the icon and text.  If icon was null, say so.
            ImageIcon icon = images[selectedIndex];
            String pet = modeStrings[selectedIndex];
            setIcon(icon);
            if (icon != null) {
                setText(pet);
                setFont(list.getFont());
            } else {
                setUhOhText(pet + " (no image available)",
                        list.getFont());
            }

            return this;
        }

        //Set the font and text when no images was found.
        protected void setUhOhText(String uhOhText, Font normalFont) {
            if (uhOhFont == null) { //lazily create this font
                uhOhFont = normalFont.deriveFont(Font.ITALIC);
            }
            setFont(uhOhFont);
            setText(uhOhText);
        }
    }
}
