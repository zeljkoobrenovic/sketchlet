package net.sf.sketchlet.designer.editor;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;
import net.sf.sketchlet.designer.GlobalProperties;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.framework.model.ActiveRegion;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class SketchletEditorUtils {

    public static JMenu getLanguageMenu() {
        Vector<String> languages = FileUtils.getFileLines(SketchletContextUtils.getSketchletDesignerConfDir() + "lang/languages.txt");
        final JMenu menu = new JMenu(Language.translate("Language"));
        menu.setIcon(Workspace.createImageIcon("resources/menu.png"));
        menu.setAlignmentX(JMenu.RIGHT_ALIGNMENT);
        menu.setMnemonic('F');
        String activeLang = GlobalProperties.get("gui-lang");

        JCheckBoxMenuItem item = new JCheckBoxMenuItem("Default (English)");
        if (activeLang == null || activeLang.isEmpty()) {
            menu.setText("Default (English)");
            item.setSelected(true);
            Language.loadTranslation(null);
        }

        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                //combo.setSelectedIndex(0);
                menu.setText("Default (English)");
                GlobalProperties.set("gui-lang", "");
                GlobalProperties.set("gui-lang-file", "");
                GlobalProperties.save();
                Language.loadTranslation(null);
                if (SketchletEditor.editorFrame != null && SketchletEditor.editorFrame.isVisible()) {
                    SketchletEditorFrame.onLanguageChange();
                }
                for (int i = 0; i < menu.getItemCount(); i++) {
                    JMenuItem mi = menu.getItem(i);
                    if (mi != null) {
                        if (mi != ae.getSource()) {
                            mi.setSelected(false);
                        }
                    }
                }
            }
        });

        menu.add(item);
        menu.addSeparator();
        for (String strLang : languages) {
            final String strLangData[] = strLang.split(";");
            if (strLangData.length < 2) {
                continue;
            }
            item = new JCheckBoxMenuItem(strLangData[0]);
            item.setSelected(strLang.equalsIgnoreCase(strLangData[0]));
            if (strLangData[0].equalsIgnoreCase(activeLang)) {
                menu.setText(strLangData[0]);
                Language.loadTranslation(SketchletContextUtils.getSketchletDesignerConfDir() + "lang/" + strLangData[1].trim());
                if (SketchletEditor.editorFrame != null && SketchletEditor.editorFrame.isVisible()) {
                    SketchletEditorFrame.onLanguageChange();
                }
            }
            item.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    menu.setText(strLangData[0]);
                    GlobalProperties.set("gui-lang", strLangData[0]);
                    GlobalProperties.set("gui-lang-file", strLangData[1].trim());
                    GlobalProperties.save();
                    Language.loadTranslation(SketchletContextUtils.getSketchletDesignerConfDir() + "lang/" + strLangData[1].trim());
                    if (SketchletEditor.editorFrame != null && SketchletEditor.editorFrame.isVisible()) {
                        SketchletEditorFrame.onLanguageChange();
                    }
                    for (int i = 0; i < menu.getItemCount(); i++) {
                        JMenuItem mi = menu.getItem(i);
                        if (mi != null) {
                            if (mi != ae.getSource()) {
                                mi.setSelected(false);
                            }
                        }
                    }
                }
            });
            menu.add(item);
        }
        menu.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(menu);

        return menu;
    }

    public static void createAnimatedImageWindow() {
        if (SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null) {
            for (ActiveRegion region : SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                String strXVar = "x";
                String strYVar = "y";
                String _strXVar = "x";
                String _strYVar = "y";
                int temp = 2;
                while (VariablesBlackboard.getInstance().getVariable(_strXVar) != null) {
                    _strXVar = strXVar + "_" + (temp++);
                }
                temp = 2;
                while (VariablesBlackboard.getInstance().getVariable(_strYVar) != null) {
                    _strYVar = strYVar + "_" + (temp++);
                }

                strXVar = _strXVar;
                strYVar = _strYVar;

                int posX = -1;
                int posY = -1;

                for (int i = 0; i < region.updateTransformations.length; i++) {
                    String strDim = (String) region.updateTransformations[i][0];
                    String strValue = (String) region.updateTransformations[i][1];

                    if (strDim.equalsIgnoreCase("position x")) {
                        posX = i;
                        if (!strValue.isEmpty()) {
                            strXVar = strValue;
                            break;
                        }
                    } else if (strDim.isEmpty()) {
                        if (posX == -1) {
                            posX = i;
                        }
                    }
                }
                for (int i = 0; i < region.updateTransformations.length; i++) {
                    String strDim = (String) region.updateTransformations[i][0];
                    String strValue = (String) region.updateTransformations[i][1];

                    if (strDim.equalsIgnoreCase("position y")) {
                        posY = i;
                        if (!strValue.isEmpty()) {
                            strYVar = strValue;
                            break;
                        }
                    } else if (strDim.isEmpty()) {
                        if (posY == -1 && posX != i) {
                            posY = i;
                        }
                    }
                }

                if (posX == -1) {
                    posX = 0;
                }
                region.updateTransformations[posX][0] = "position x";
                region.updateTransformations[posX][1] = strXVar;

                if (posY == -1) {
                    posY = 1;
                }
                region.updateTransformations[posY][0] = "position y";
                region.updateTransformations[posY][1] = strYVar;

                region.windowX = "=" + strXVar;
                region.windowY = "=" + strYVar;
                region.windowWidth = "" + region.getWidth();
                region.windowHeight = "" + region.getHeight();
                SketchletEditor.getInstance().forceRepaint();
            }
        }
    }
}
