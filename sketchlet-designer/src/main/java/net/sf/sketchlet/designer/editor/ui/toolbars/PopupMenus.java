/*
 * To change this template, choose Tools | Templates
 * and open the template in the openExternalEditor.
 */
package net.sf.sketchlet.designer.editor.ui.toolbars;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.common.ui.fisheye.FishEyeMenu;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.tool.stroke.StrokeCombo;
import net.sf.sketchlet.designer.editor.ui.extraeditor.ActiveRegionsExtraPanel;
import net.sf.sketchlet.designer.editor.ui.region.ShapePanel;
import net.sf.sketchlet.model.ActiveRegion;
import net.sf.sketchlet.util.Colors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author zobrenovic
 */
public class PopupMenus {

    public static void setRegionProperty(String name, String value) {
        if (SketchletEditor.getInstance().getCurrentPage().getRegions().getSelectedRegions() != null) {
            for (ActiveRegion r : SketchletEditor.getInstance().getCurrentPage().getRegions().getSelectedRegions()) {
                r.setProperty(name, value);

                if (name.equalsIgnoreCase("shape")) {
                    String strArgs = "";
                    String args[] = SketchletEditor.getShapeArgs(value);
                    if (args != null) {
                        String arg = JOptionPane.showInputDialog(SketchletEditor.editorFrame, args[0], args[1]);
                        if (arg != null) {
                            strArgs = arg;
                        }
                    }
                    r.strShapeArgs = strArgs;
                }
            }
            ActiveRegionsExtraPanel.reload(SketchletEditor.getInstance().getCurrentPage().getRegions().getSelectedRegions().lastElement());
            SketchletEditor.getInstance().forceRepaint();
        } else if (SketchletEditor.getInstance().getCurrentPage().getSelectedConnector() != null) {
            SketchletEditor.getInstance().getCurrentPage().getSelectedConnector().setProperty(name, value);
            SketchletEditor.getInstance().forceRepaint();
        }
    }

    public static JPopupMenu loadTextColorMenu() {
        final JPopupMenu colorsMenu = new JPopupMenu();
        String size[] = new String[30];
        for (int i = 0; i < size.length; i++) {
            size[i] = "" + (8 + i * 2);
        }

        colorsMenu.add(PopupMenus.loadFishEyeMenu("font", "font name", GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        colorsMenu.add(PopupMenus.loadMenu("font style", "font style", new String[]{"bold", "italic", "bold italic"}));
        colorsMenu.add(PopupMenus.loadMenu("text size", "font size", size));
        colorsMenu.addSeparator();
        Color colors[] = Colors.getStandardColors();
        String colorNames[] = Colors.getStandardColorNames();
        for (int i = 0; i < colors.length; i++) {
            final String colorName = colorNames[i];
            JMenuItem menuItem = new JMenuItem(colorName, ModeToolbar.createImageIcon(colors[i], 70, 20));
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    setRegionProperty("text color", colorName);
                }
            });
            colorsMenu.add(menuItem);
        }
        JMenuItem _menuItem = new JMenuItem(Language.translate("more colors..."), Workspace.createImageIcon("resources/palette.png"));
        _menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                Color newColor = JColorChooser.showDialog(
                        SketchletEditor.getInstance().getInstance(),
                        "Choose text Color",
                        SketchletEditor.getInstance().getColor());

                if (newColor != null) {
                    setRegionProperty("text color", newColor.getRed() + " " + newColor.getGreen() + " " + newColor.getBlue());
                }
            }
        });
        colorsMenu.add(_menuItem);
        colorsMenu.addSeparator();
        _menuItem = new JMenuItem(Language.translate("default"));
        _menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setRegionProperty("text color", "");
            }
        });
        colorsMenu.add(_menuItem);

        return colorsMenu;
    }

    public static JPopupMenu loadFillColorMenu() {
        final JPopupMenu colorsMenu = new JPopupMenu();

        colorsMenu.add(PopupMenus.loadTransparencyMenu());
        colorsMenu.addSeparator();
        Color colors[] = Colors.getStandardColors();
        String colorNames[] = Colors.getStandardColorNames();
        for (int i = 0; i < colors.length; i++) {
            final String colorName = colorNames[i];
            JMenuItem menuItem = new JMenuItem(colorName, ModeToolbar.createImageIcon(colors[i], 70, 20));
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    setRegionProperty("fill color", colorName);
                }
            });
            colorsMenu.add(menuItem);
        }
        JMenuItem _menuItem = new JMenuItem(Language.translate("more colors..."), Workspace.createImageIcon("resources/palette.png"));
        _menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                Color newColor = JColorChooser.showDialog(
                        SketchletEditor.getInstance().getInstance(),
                        "Choose Fill Color",
                        SketchletEditor.getInstance().getColor());

                if (newColor != null) {
                    setRegionProperty("fill color", newColor.getRed() + " " + newColor.getGreen() + " " + newColor.getBlue());
                }
            }
        });
        colorsMenu.add(_menuItem);
        colorsMenu.addSeparator();
        _menuItem = new JMenuItem(Language.translate("no fill"));
        _menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setRegionProperty("fill color", "");
            }
        });
        colorsMenu.add(_menuItem);

        return colorsMenu;
    }

    public static JPopupMenu loadLineColorMenu() {
        final JPopupMenu colorsMenu = new JPopupMenu();

        colorsMenu.add(PopupMenus.loadThicknessMenu());
        colorsMenu.add(PopupMenus.loadLineStyleMenu());
        colorsMenu.addSeparator();

        Color colors[] = Colors.getStandardColors();
        String colorNames[] = Colors.getStandardColorNames();
        for (int i = 0; i < colors.length; i++) {
            final String colorName = colorNames[i];
            JMenuItem menuItem = new JMenuItem(colorName, ModeToolbar.createImageIcon(colors[i], 70, 20));
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    setRegionProperty("line color", colorName);
                }
            });
            colorsMenu.add(menuItem);
        }
        JMenuItem _menuItem = new JMenuItem(Language.translate("more colors..."), Workspace.createImageIcon("resources/palette.png"));
        _menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                Color newColor = JColorChooser.showDialog(
                        SketchletEditor.getInstance().getInstance(),
                        "Choose Line Color",
                        SketchletEditor.getInstance().getColor());

                if (newColor != null) {
                    setRegionProperty("line color", newColor.getRed() + " " + newColor.getGreen() + " " + newColor.getBlue());
                }
            }
        });
        colorsMenu.add(_menuItem);
        colorsMenu.addSeparator();
        _menuItem = new JMenuItem(Language.translate("default"));
        _menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setRegionProperty("line color", "");
            }
        });
        colorsMenu.add(_menuItem);

        return colorsMenu;
    }

    public static JPopupMenu loadShapeMenu() {
        final JPopupMenu shapeMenu = new JPopupMenu();

        JMenuItem shapeRect = new JMenuItem(Language.translate("Rectangle"), Workspace.createImageIcon("resources/rectangle.png"));
        JMenuItem shapeOval = new JMenuItem(Language.translate("Oval"), Workspace.createImageIcon("resources/oval.png"));
        JMenuItem shapeRoundRect = new JMenuItem(Language.translate("Rounded Rectangle"), Workspace.createImageIcon("resources/rounded_rectangle.png"));
        JMenuItem shapeTriangle1 = new JMenuItem(Language.translate("Triangle"), Workspace.createImageIcon("resources/triangle_1.png"));
        JMenuItem shapeTriangle2 = new JMenuItem(Language.translate("Triangle"), Workspace.createImageIcon("resources/triangle_2.png"));
        JMenuItem shapeLine1 = new JMenuItem(Language.translate("Line"), Workspace.createImageIcon("resources/line_1.png"));
        JMenuItem shapeLine2 = new JMenuItem(Language.translate("Line"), Workspace.createImageIcon("resources/line_2.png"));
        JMenuItem shapeLine3 = new JMenuItem(Language.translate("Horizontal Line"), Workspace.createImageIcon("resources/line_3.png"));
        JMenuItem shapeLine4 = new JMenuItem(Language.translate("Vertical Line"), Workspace.createImageIcon("resources/line_4.png"));
        JMenuItem pieSlice = new JMenuItem(Language.translate("Pie Slice"), Workspace.createImageIcon("resources/pie_slice.png"));
        shapeRect.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setRegionProperty("shape", "Rectangle");
            }
        });
        shapeOval.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setRegionProperty("shape", "Oval");

            }
        });
        shapeRoundRect.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setRegionProperty("shape", "Rounded Rectangle");

            }
        });
        shapeTriangle1.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setRegionProperty("shape", "Triangle 1");

            }
        });
        shapeTriangle2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setRegionProperty("shape", "Triangle 2");

            }
        });
        shapeLine1.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setRegionProperty("shape", "Line 1");

            }
        });
        shapeLine2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setRegionProperty("shape", "Line 2");

            }
        });
        shapeLine3.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setRegionProperty("shape", "Horizontal Line");

            }
        });
        shapeLine4.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setRegionProperty("shape", "Vertical Line");

            }
        });
        pieSlice.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setRegionProperty("shape", "Pie Slice");

            }
        });
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
                    setRegionProperty("shape", strID);

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
                    setRegionProperty("shape", strID);

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

    public static JMenu loadThicknessMenu() {
        final JMenu menu = new JMenu(Language.translate("line thickness"));
        menu.setIcon(ModeToolbar.createImageIcon(5, 70, 20));

        for (int i = 1; i < 10; i++) {
            final String strThickness = "" + i;
            JMenuItem menuItem = new JMenuItem(strThickness, ModeToolbar.createImageIcon(i + 1, 70, 20));
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    setRegionProperty("line thickness", strThickness);
                }
            });
            menu.add(menuItem);
        }


        return menu;
    }

    public static JMenu loadLineStyleMenu() {
        final JMenu menu = new JMenu(Language.translate("line style"));
        menu.setIcon(StrokeCombo.createImageIcon(ColorToolbar.getStroke(StrokeCombo.strokeIDs[0], 3.0f), 70, 20));

        for (int i = 0; i < StrokeCombo.strokeIDs.length; i++) {
            final String strStroke = StrokeCombo.strokeIDs[i];
            JMenuItem menuItem = new JMenuItem(StrokeCombo.createImageIcon(ColorToolbar.getStroke(strStroke, 3.0f), 70, 20));
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    setRegionProperty("line style", strStroke);
                }
            });
            menu.add(menuItem);
        }
        return menu;
    }

    public static JMenu loadTransparencyMenu() {
        final JMenu menu = new JMenu(Language.translate("transparency"));
        menu.setIcon(Workspace.createImageIcon("resources/transparency_wide.png"));

        for (int i = 0; i <= 10; i++) {
            final double t = i / 10.0;
            final String strTransparency = "" + t;
            JMenuItem menuItem = new JMenuItem(strTransparency, ModeToolbar.createImageIcon(new Color(0.0f, 0.0f, 0.0f, (float) t), 70, 20));
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    setRegionProperty("transparency", "" + (float) t);

                }
            });
            menu.add(menuItem);
        }

        return menu;
    }

    public static JPopupMenu loadAlignmentMenu() {
        JMenuItem alignLeft = new JMenuItem(Language.translate("Left"), Workspace.createImageIcon("resources/left.gif"));
        alignLeft.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setRegionProperty("horizontal alignment", "left");
            }
        });
        JMenuItem alignCenter = new JMenuItem(Language.translate("Center"), Workspace.createImageIcon("resources/center.gif"));
        alignCenter.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setRegionProperty("horizontal alignment", "center");
            }
        });
        JMenuItem alignRight = new JMenuItem(Language.translate("Right"), Workspace.createImageIcon("resources/right.gif"));
        alignRight.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setRegionProperty("horizontal alignment", "right");
            }
        });

        JMenuItem alignTop = new JMenuItem(Language.translate("Top"), Workspace.createImageIcon("resources/align-top.png"));
        alignTop.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setRegionProperty("vertical alignment", "top");
            }
        });
        JMenuItem alignMiddle = new JMenuItem(Language.translate("Middle"), Workspace.createImageIcon("resources/align-centered.png"));
        alignMiddle.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setRegionProperty("vertical alignment", "center");
            }
        });
        JMenuItem alignBottom = new JMenuItem(Language.translate("Bottom"), Workspace.createImageIcon("resources/align-bottom.png"));
        alignBottom.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setRegionProperty("vertical alignment", "bottom");
            }
        });

        final JPopupMenu menu = new JPopupMenu();

        menu.add(alignLeft);
        menu.add(alignCenter);
        menu.add(alignRight);

        menu.addSeparator();

        menu.add(alignTop);
        menu.add(alignMiddle);
        menu.add(alignBottom);
        return menu;
    }

    public static JMenu loadMenu(String title, final String property, String items[]) {
        final JMenu menu = new JMenu(title);

        for (int i = 0; i < items.length; i++) {
            final String strValue = items[i];
            JMenuItem menuItem = new JMenuItem(strValue);
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    PopupMenus.setRegionProperty(property, strValue);
                }
            });
            menu.add(menuItem);
        }

        return menu;
    }

    public static FishEyeMenu loadFishEyeMenu(String title, final String property, String items[]) {
        final FishEyeMenu menu = new FishEyeMenu(title);

        for (int i = 0; i < items.length; i++) {
            final String strValue = items[i];
            JMenuItem menuItem = new JMenuItem(strValue);
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    PopupMenus.setRegionProperty(property, strValue);
                }
            });
            menu.add(menuItem);
        }

        return menu;
    }
}
