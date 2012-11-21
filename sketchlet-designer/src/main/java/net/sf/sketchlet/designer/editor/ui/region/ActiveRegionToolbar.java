/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.ui.region;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.tool.Tool;

import javax.swing.*;
import java.awt.*;

/**
 * @author zobrenovic
 */
public class ActiveRegionToolbar extends JToolBar {

    public JLabel toolLabel = new JLabel(Language.translate("Tool: "));
    JToolBar leftPanel = new JToolBar();
    // JButton activeRegionsShapes = new JButton(Workspace.createImageIcon("resources/active_region2.png", ""));
    // JButton activeRegionsLibrary = new JButton(Workspace.createImageIcon("resources/active_region3.png", ""));
    final static String shapes[][] = {
            {"Rectangle", "resources/rectangle.png", "Rectangle"},
            {"Oval", "resources/oval.png", "Oval"},
            {"Rounded Rectangle", "resources/rounded_rectangle.png", "Rounded Rectangle"},
            {"Triangle", "resources/triangle_1.png", "Triangle 1"},
            {"Triangle", "resources/triangle_2.png", "Triangle 2"},
            {"Line", "resources/line_1.png", "Line 1"},
            {"Line", "resources/line_2.png", "Line 2"},
            {"Horizontal Line", "resources/line_3.png", "Horizontal Line"},
            {"Vertical Line", "resources/line_4.png", "Vertical Line"},};

    public ActiveRegionToolbar() {
        /*activeRegionsShapes.setToolTipText(Language.translate("Create a new region with predefined shapes or controles"));
        activeRegionsShapes.addActionListener(new ActionListener()     {

            public void actionPerformed(ActionEvent e) {

                final JPopupMenu popupMenu = new JPopupMenu();
                TutorialPanel.prepare(popupMenu);

                for (int i = 0; i < shapes.length; i++) {
                    JMenuItem mi = new JMenuItem(Workspace.createImageIcon(shapes[i][1]));
                    mi.setToolTipText(shapes[i][0]);
                    final int ii = i;
                    mi.addActionListener(new ActionListener()     {

                        public void actionPerformed(ActionEvent event) {
                            SketchletEditor.initProperties = new String[][]{{"shape", shapes[ii][2]}};
                            SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("tool_active_region_new");
                            SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.activeRegionTool, null);
                        }
                    });

                    mi.setMargin(new Insets(0, 0, 0, 0));
                    popupMenu.add(mi);
                }

                popupMenu.show(activeRegionsShapes.getParent(), activeRegionsShapes.getX(), activeRegionsShapes.getY() + activeRegionsShapes.getHeight());
            }
        });

        activeRegionsLibrary.setToolTipText(Language.translate("Create a new region from library"));
        activeRegionsLibrary.addActionListener(new ActionListener()     {

            public void actionPerformed(ActionEvent e) {

                final JPopupMenu popupMenu = new JPopupMenu();
                TutorialPanel.prepare(popupMenu);
                final String strDir = SketchletContextUtils.getLibraryLocation() + "regions";

                final String list[] = new File(strDir).list();
                if (list != null && list.length > 0) {
                    for (int i = 0; i < list.length; i++) {
                        JMenuItem mi = new JMenuItem(list[i].replace(".xml", ""));
                        final int ii = i;
                        mi.addActionListener(new ActionListener()     {

                            public void actionPerformed(ActionEvent event) {
                                try {
                                    Properties p = new Properties();
                                    p.loadFromXML(new FileInputStream(new File(strDir + "/" + list[ii])));
                                    SketchletEditor.initProperties = new String[p.size()][2];
                                    int ip = 0;
                                    for (String strName : p.stringPropertyNames()) {
                                        SketchletEditor.initProperties[ip][0] = strName;
                                        SketchletEditor.initProperties[ip][1] = p.getProperty(strName);
                                        ip++;
                                    }
                                    SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("tool_active_region_new");
                                    SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.activeRegionTool, null);
                                } catch (Exception e) {
                                }
                            }
                        });

                        mi.setMargin(new Insets(0, 0, 0, 0));
                        popupMenu.add(mi);
                    }
                }

                popupMenu.show(activeRegionsLibrary.getParent(), activeRegionsLibrary.getX(), activeRegionsLibrary.getY() + activeRegionsLibrary.getHeight());
            }
        });*/

        setFloatable(false);
        FlowLayout flowLayout1 = new FlowLayout(FlowLayout.LEFT);
        flowLayout1.setVgap(0);
        flowLayout1.setHgap(0);

        leftPanel.setLayout(flowLayout1);
        leftPanel.setFloatable(false);
        leftPanel.setBorder(BorderFactory.createEmptyBorder());

        toolLabel.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(toolLabel);

        setBorder(BorderFactory.createEmptyBorder());
        setLayout(new BorderLayout());

        leftPanel.add(toolLabel);
        toolLabel.setHorizontalTextPosition(JLabel.LEFT);

        /*leftPanel.add(activeRegionsShapes);
        leftPanel.add(activeRegionsLibrary);*/

        add(leftPanel, BorderLayout.WEST);
        add(SketchletEditor.getInstance().getFormulaToolbar(), BorderLayout.CENTER);
    }

    public void toolChanged(Tool tool) {
        ImageIcon icon = tool.getIcon();
        this.toolLabel.setIcon(icon);
        this.toolLabel.setToolTipText(tool.getName());
        /*if (tool instanceof ActiveRegionTool) {
            activeRegionsShapes.setEnabled(true);
            activeRegionsLibrary.setEnabled(true);
        } else {
            activeRegionsShapes.setEnabled(false);
            activeRegionsLibrary.setEnabled(false);
        }*/
    }
}
