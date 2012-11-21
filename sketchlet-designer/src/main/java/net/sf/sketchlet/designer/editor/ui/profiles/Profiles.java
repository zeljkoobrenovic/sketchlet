/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.ui.profiles;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.GlobalProperties;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.SketchletEditorFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class Profiles {

    private static Vector<Profile> standardProfiles = new Vector<Profile>();
    private static Vector<Profile> profiles = new Vector<Profile>();
    public static Profile activeProfile = null;
    public static JComboBox combo = new JComboBox();

    public static Vector<Profile> getProfiles() {
        String strProfile = FileUtils.getFileText(SketchletContextUtils.getApplicationSettingsDir() + "profiles.txt");
        String[] strProfiles = strProfile.split(";");

        Vector profs = new Vector<Profile>();

        for (int i = 0; i < strProfiles.length; i++) {
            if (!strProfiles[i].trim().isEmpty()) {
                profs.add(new Profile(strProfiles[i]));
            }
        }
        return profs;
    }

    public static Vector<Profile> getStandardProfiles() {
        String strProfile = FileUtils.getFileText(SketchletContextUtils.getSketchletDesignerConfDir() + "profiles/standard_profiles.txt");
        String[] strProfiles = strProfile.split(";");

        Vector profs = new Vector<Profile>();

        for (int i = 0; i < strProfiles.length; i++) {
            if (!strProfiles[i].trim().isEmpty()) {
                profs.add(new Profile(strProfiles[i]));
            }
        }

        return profs;
    }

    public static void reloadComboBox() {
        int n = combo.getSelectedIndex();
        combo.removeAllItems();
        combo.addItem("Advanced Profile (All Features)");

        for (Profile p : Profiles.getProfiles()) {
            combo.addItem(p.name);
        }
        if (n > combo.getItemCount()) {
            n = 0;
        }
        if (n >= 0) {
            combo.setSelectedIndex(n);
        }
    }

    public static JMenu getMenu() {
        final JMenu menu = new JMenu(Language.translate("Profiles"));
        menu.setIcon(Workspace.createImageIcon("resources/menu.png"));
        return loadMenu(menu);
    }

    public static JMenu loadMenu(final JMenu menu) {
        if (menu == null) {
            return null;
        }
        menu.removeAll();
        int n = GlobalProperties.get("active-profile", -1);

        if (n == -1) {
            menu.setText(Language.translate("Advanced Profile (All Features)"));
        }
        for (int i = 0; i < Profiles.getStandardProfiles().size(); i++) {
            final Profile p = Profiles.getStandardProfiles().elementAt(i);
            final int index = i;
            if (!p.name.isEmpty()) {
                JCheckBoxMenuItem item = new JCheckBoxMenuItem(p.name);
                item.setSelected(n == 10000 + index);
                if (n == 10000 + index) {
                    menu.setText(p.name);
                }
                item.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        //combo.setSelectedIndex(index);
                        Profile p = Profiles.getStandardProfiles().elementAt(index);
                        menu.setText(p.name);
                        activeProfile = p;
                        GlobalProperties.set("active-profile", (10000 + index) + "");
                        GlobalProperties.save();
                        if (SketchletEditor.editorFrame != null && SketchletEditor.editorFrame.isVisible()) {
                            SketchletEditorFrame.onProfileChange();
                        }
                        for (int i = 0; i < menu.getItemCount(); i++) {
                            JMenuItem mi = menu.getItem(i);
                            if (mi != null) {
                                if (mi != ae.getSource()) {
                                    mi.setSelected(false);
                                } else {
                                    mi.setSelected(true);
                                }
                            }
                        }
                        loadMenu(SketchletEditor.getInstance().getProfilesMenu());
                    }
                });
                /*if (index < 10) {
                item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0 + index + 1, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | Event.ALT_MASK));
                }*/
                menu.add(item);
            }
        }
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(Language.translate("Advanced Profile (All Features)"));
        //item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | Event.ALT_MASK));
        item.setSelected(n == -1);
        item.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                //combo.setSelectedIndex(0);
                menu.setText(Language.translate("Advanced Profile (All Features)"));
                activeProfile = null;
                GlobalProperties.set("active-profile", -1 + "");
                GlobalProperties.save();
                if (SketchletEditor.editorFrame != null && SketchletEditor.editorFrame.isVisible()) {
                    SketchletEditorFrame.onProfileChange();
                }
                for (int i = 0; i < menu.getItemCount(); i++) {
                    JMenuItem mi = menu.getItem(i);
                    if (mi != null) {
                        if (mi != ae.getSource()) {
                            mi.setSelected(false);
                        }
                    }
                }
                loadMenu(SketchletEditor.getInstance().getProfilesMenu());
            }
        });

        menu.add(item);
        if (Profiles.getProfiles().size() > 0) {
            menu.addSeparator();
            for (int i = 0; i < Profiles.getProfiles().size(); i++) {
                final Profile p = Profiles.getProfiles().elementAt(i);
                final int index = i;
                if (!p.name.isEmpty()) {
                    item = new JCheckBoxMenuItem(p.name);
                    item.setSelected(n == index);
                    if (n == index) {
                        menu.setText(p.name);
                    }
                    item.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent ae) {
                            //combo.setSelectedIndex(index);
                            Profile p = Profiles.getProfiles().elementAt(index);
                            menu.setText(p.name);
                            activeProfile = p;
                            GlobalProperties.set("active-profile", (index) + "");
                            GlobalProperties.save();
                            if (SketchletEditor.editorFrame != null && SketchletEditor.editorFrame.isVisible()) {
                                SketchletEditorFrame.onProfileChange();
                            }
                            for (int i = 0; i < menu.getItemCount(); i++) {
                                JMenuItem mi = menu.getItem(i);
                                if (mi != null) {
                                    if (mi != ae.getSource()) {
                                        mi.setSelected(false);
                                    } else {
                                        mi.setSelected(true);
                                    }
                                }
                            }
                            loadMenu(SketchletEditor.getInstance().getProfilesMenu());
                        }
                    });
                    /*if (index < 10) {
                    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0 + index + 1, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | Event.ALT_MASK));
                    }*/
                    menu.add(item);
                }
            }
        }
        JMenuItem item2 = new JMenuItem(Language.translate("Edit User Profiles..."));
        item2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int n = Profiles.combo.getSelectedIndex();
                new ProfileManager(SketchletEditor.editorFrame, n - 1);
                if (n > 0) {
                    SketchletEditorFrame.onProfileChange();
                }
            }
        });
        menu.addSeparator();
        menu.add(item2);
        menu.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(menu);

        return menu;
    }

    public static JComboBox getComboBox() {
        int n = GlobalProperties.get("active-profile", -1);
        combo.setPreferredSize(new Dimension(150, 25));
        combo.setMaximumSize(new Dimension(150, 25));
        combo.setMaximumRowCount(30);
        reloadComboBox();
        combo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int n = combo.getSelectedIndex();

                if (n < 0 || n > Profiles.getProfiles().size()) {
                    return;
                }
                if (n == 0) {
                    activeProfile = null;
                } else {
                    activeProfile = Profiles.getProfiles().elementAt(n - 1);
                }
                GlobalProperties.set("active-profile", n + "");
                GlobalProperties.save();
                if (SketchletEditor.editorFrame != null && SketchletEditor.editorFrame.isVisible()) {
                    SketchletEditorFrame.onProfileChange();
                }
            }
        });
        combo.setSelectedIndex(n);

        return combo;
    }

    public static boolean isActive(String strItem) {
        if (activeProfile == null) {
            return true;
        } else {
            String strItems[] = strItem.split(",");
            for (int i = 0; i < strItems.length; i++) {
                if (!activeProfile.isActive(strItems[i].trim())) {
                    return false;
                }

            }
            return true;
        }
    }

    public static boolean isActiveAny(String strItem) {
        if (activeProfile == null) {
            return true;
        } else {
            String strItems[] = strItem.split(",");
            for (int i = 0; i < strItems.length; i++) {
                if (activeProfile.isActive(strItems[i])) {
                    return true;
                }
            }
            return false;
        }
    }
}
