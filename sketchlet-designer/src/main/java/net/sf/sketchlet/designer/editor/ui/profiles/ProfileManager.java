package net.sf.sketchlet.designer.editor.ui.profiles;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.GlobalProperties;
import net.sf.sketchlet.designer.SketchletDesignerProperties;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.help.HelpViewer;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class ProfileManager {

    HelpViewer helpViewer = new HelpViewer("profiling", false);
    JButton btnNew = new JButton(Workspace.createImageIcon("resources/new.gif"));
    JButton btnDelete = new JButton(Workspace.createImageIcon("resources/remove.gif"));
    JButton btnLeft = new JButton(Workspace.createImageIcon("resources/go-up.png"));
    JButton btnRight = new JButton(Workspace.createImageIcon("resources/go-down.png"));
    JButton btnImport = new JButton(Workspace.createImageIcon("resources/import.gif"));
    JButton btnExport = new JButton(Workspace.createImageIcon("resources/export.gif"));

    public ProfileManager(JFrame owner) {
        this(owner, 0);
    }

    JTabbedPane tabs = new JTabbedPane();

    public ProfileManager(JFrame owner, int n) {
        frame = new JDialog(owner, "Profile Manager", true);
        createAndShowGUI(n);
    }

    JDialog frame;
    Vector<ProfilePanel> panels = new Vector<ProfilePanel>();

    public void createAndShowGUI(int tabIndex) {
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.setTabPlacement(JTabbedPane.LEFT);

        loadTabs();
        frame.add(tabs);
        if (tabIndex < 0 || tabIndex >= tabs.getTabCount()) {
            tabIndex = 0;
        }
        if (tabs.getTabCount() > 0) {
            tabs.setSelectedIndex(tabIndex);
        }
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(Workspace.getMainFrame());

        helpViewer.setPreferredSize(new Dimension(220, 600));
        frame.add(helpViewer, BorderLayout.EAST);
        helpViewer.showAutoHelpByID("profiling");

        JToolBar tbCmd = new JToolBar();
        tbCmd.setFloatable(false);
        tbCmd.add(btnNew);
        tbCmd.add(btnDelete);
        tbCmd.add(btnLeft);
        tbCmd.add(btnRight);
        tbCmd.add(btnImport);
        tbCmd.add(btnExport);
        btnNew.setToolTipText("Create new profile");
        btnDelete.setToolTipText("Delete selected profile");
        btnLeft.setToolTipText("Move profile up in the list");
        btnRight.setToolTipText("Move profile down in the listy");
        btnImport.setToolTipText("Import profiles from a file");
        btnExport.setToolTipText("Export profiles to a file");
        btnNew.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                addNewProfile();
            }
        });
        btnDelete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int n = tabs.getSelectedIndex();
                if (n >= 0) {
                    tabs.removeTabAt(n);
                    panels.removeElementAt(n);
                }
            }
        });
        btnLeft.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int n = tabs.getSelectedIndex();
                if (n > 0) {
                    ProfilePanel pp = panels.remove(n);
                    panels.insertElementAt(pp, n - 1);
                    tabs.remove(n);
                    tabs.insertTab(pp.profile.name, null, pp, "", n - 1);
                    tabs.setSelectedIndex(n - 1);
                }
            }
        });
        btnRight.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int n = tabs.getSelectedIndex();
                if (n < tabs.getTabCount() - 1) {
                    ProfilePanel pp = panels.remove(n);
                    panels.insertElementAt(pp, n + 1);
                    tabs.remove(n);
                    tabs.insertTab(pp.profile.name, null, pp, "", n + 1);
                    tabs.setSelectedIndex(n + 1);
                }
            }
        });
        btnImport.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                importProfiles();
            }
        });
        btnExport.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                exportProfiles();
            }
        });
        frame.add(tbCmd, BorderLayout.NORTH);
        tabs.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (!tabs.isShowing()) {
                    return;
                }
                enableControls();
            }
        });
        enableControls();
        frame.setVisible(true);
    }

    public void loadTabs() {
        tabs.removeAll();
        for (Profile p : Profiles.getProfiles()) {
            ProfilePanel pp = new ProfilePanel(p, this);
            tabs.add(pp, p.name);
            panels.add(pp);
        }
        tabs.revalidate();
    }

    public void enableControls() {
        int n = tabs.getSelectedIndex();
        btnDelete.setEnabled(tabs.getTabCount() > 0);
        btnLeft.setEnabled(n > 0);
        btnRight.setEnabled(n < tabs.getTabCount() - 1);
    }

    public String getProfilesString() {
        String strProfiles = "";
        for (ProfilePanel pp : panels) {
            pp.saveToProfile();
            pp.profile.save();
            if (!pp.profile.name.trim().isEmpty()) {
                strProfiles += pp.profile.getProfileText() + ";";
            }
        }
        return strProfiles;
    }

    public void exportProfiles() {
        JFileChooser chooser = new JFileChooser();
        chooser.setApproveButtonText(Language.translate("Save"));
        chooser.setDialogTitle(Language.translate("Export Profiles"));
        chooser.setSelectedFile(new File(chooser.getCurrentDirectory(), "profiles.txt"));

        //In response to a button click:
        int returnVal = chooser.showSaveDialog(frame);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                String strProfiles = getProfilesString();
                FileUtils.saveFileText(chooser.getSelectedFile().getPath(), strProfiles);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public void importProfiles() {
        JFileChooser chooser = new JFileChooser();
        chooser.setApproveButtonText(Language.translate("Import"));
        chooser.setDialogTitle(Language.translate("Import Profiles"));
        chooser.setSelectedFile(new File(chooser.getCurrentDirectory(), "profiles.txt"));

        //In response to a button click:
        int returnVal = chooser.showOpenDialog(frame);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                String strProfiles = getProfilesString() + FileUtils.getFileText(chooser.getSelectedFile().getPath());
                FileUtils.saveFileText(SketchletContextUtils.getApplicationSettingsDir() + "profiles.txt", strProfiles);
                loadTabs();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public void addNewProfile() {
        Profile p = new Profile("New Profile");
        ProfilePanel pp = new ProfilePanel(p, this);
        panels.add(pp);
        tabs.add(pp, "New Profile");
        tabs.setSelectedIndex(tabs.getTabCount() - 1);
        tabs.scrollRectToVisible(tabs.getBoundsAt(tabs.getTabCount() - 1));
        tabs.revalidate();
    }

    public static void main(String args[]) throws Exception {
        String strLaF = "Nimbus";
        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if (info.getName().contains(strLaF)) {
                UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }
        new ProfileManager(null);
    }

    public void saveProfiles() {
        String strProfiles = getProfilesString();

        FileUtils.saveFileText(SketchletContextUtils.getApplicationSettingsDir() + "profiles.txt", strProfiles);

        GlobalProperties.save();
        SketchletDesignerProperties.save();


        //Profiles.reloadComboBox();
    }
}
