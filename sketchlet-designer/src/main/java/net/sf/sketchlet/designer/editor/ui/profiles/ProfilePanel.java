package net.sf.sketchlet.designer.editor.ui.profiles;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.GlobalProperties;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.SketchletEditorFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

/**
 * @author zobrenovic
 */
public class ProfilePanel extends JPanel {

    String items[][] = {
            {Language.translate("Main Objects"), ""},
            {Language.translate("Active Regions"), "active_regions_layer", "active_region.png", "editor_mode_active_regions"},
            {Language.translate("Variables"), "variables", "variable.png", "variables"},
            {Language.translate("I/O Services"), "io_services", "actions.png", "services"},
            {Language.translate("Timers"), "timers", "timer.png", "timers"},
            {Language.translate("Action Lists"), "macros", "macros.png", "macros"},
            {Language.translate("Screen Poking"), "screen_poking", "mouse.png", "screen_poking"},
            {Language.translate("Scripts"), "scripts", "script.png", "scripts"},
            {Language.translate("Table Variables"), "derived_variables", "tables.gif", ""},
            {Language.translate("Derived Variables"), "derived_variables", "formula.png", ""},
            {Language.translate("Page Objects"), ""},
            {Language.translate("Page Events"), "page_actions", "actions.png", "sketchlet_actions"},
            {Language.translate("Page Properties"), "page_properties", "details.gif", "sketchlet_properties"},
            {Language.translate("Page Perspective"), "page_perspective", "perspective_lines.png", "sketchlet_perspective"},
            {Language.translate("Page Spreadsheet"), "page_spreadsheet", "spreadsheet-icon.png", "page_spreadsheet"},
            {Language.translate("Active Region Settings"), ""},
            {Language.translate("Widget"), "active_region_widget", "listbullet.gif", "active_region_widget"},
            {Language.translate("Transformations"), "active_region_transformations", "transformation.png", "active_region_transform"},
            {Language.translate("Move & Rotate"), "active_region_move", "move_rotate.png", "active_region_move"},
            {Language.translate("Mouse Events"), "active_region_mouse", "mouse.png", "active_region_mouse"},
            {Language.translate("Keyboard Events"), "active_region_keyboard", "keyboard.png", "active_region_keyboard"},
            {Language.translate("Overlap & Touch"), "active_region_overlap", "overlap.png", "active_region_interaction"},
            {Language.translate("General"), "active_region_general", "", "active_region_general"},
            {Language.translate("Active Region Image"), ""},
            {Language.translate("URL/file path"), "active_region_url", "url.gif", "active_region_image_url"},
            {Language.translate("Screen Capture"), "active_region_screen_capture", "interaction_space.png", "active_region_image_screen_capture"},
            {Language.translate("Shape"), "active_region_shape", "shapes.png", "active_region_image_shape"},
            {Language.translate("Text"), "active_region_text", "text.png", "active_region_image_text"},
            {Language.translate("HTML"), "active_region_html", "text-html.gif", "active_region_image_html"},
            {Language.translate("SVG"), "active_region_svg", "svg_logo.png", "active_region_image_svg"},
            {Language.translate("VNC"), "active_region_vnc", "screen.png", "active_region_image_svn"},
            {Language.translate("Active Region Popup Menu"), "tool_active_region_context_menu"},
            {Language.translate("Appearance"), "active_region_popup_appearance", "draw-cube.png"},
            {Language.translate("Appearance"), "active_region_popup_widget", "listbullet.gif"},
            {Language.translate("Copy Region"), "active_region_popup_copy", "edit-copy.png"},
            {Language.translate("Paste Text in Region"), "active_region_popup_paste_text", "edit-paste.png"},
            {Language.translate("Paste Image in Region"), "active_region_popup_paste_image", "edit-paste.png"},
            {Language.translate("Fix Values"), "active_region_popup_fix", "lock.png"},
            {Language.translate("Reset Values"), "active_region_popup_reset", "zero.png"},
            {Language.translate("Wizards"), "active_region_popup_wizards", "wizard.png"},
            {Language.translate("Define Clip"), "active_region_popup_define_clip", "clip.png"},
            {Language.translate("Define Trajectory"), "active_region_popup_trajectory", "trajectory.png"},
            {Language.translate("Group Regions"), "active_region_popup_group", "system-users.png"},
            {Language.translate("Align Regions"), "active_region_popup_align", "align.gif"},
            {Language.translate("Bring to Front"), "active_region_popup_bring_to_front", "bring-forward.png"},
            {Language.translate("Send to Back"), "active_region_popup_send_to_back", "send-backwards.png"},
            {Language.translate("Extract Image From Sketch"), "active_region_popup_extract", "edit-cut.png"},
            {Language.translate("Extract in New Frame"), "active_region_popup_extract_new", "edit-cut.png"},
            {Language.translate("Stamp Image On Sketch"), "active_region_popup_stamp", "stamp.png"},
            {Language.translate("Toolbar Icons"), "sketchify_editor_toolbar"},
            {Language.translate("New Page"), "new_page", "new.gif"},
            {Language.translate("Save Page"), "save_page", "save.gif"},
            {Language.translate("Execute Page"), "execute_page", "start.gif"},
            {Language.translate("Define Presentation Space"), "presentation_space", "screen.png"},
            {Language.translate("Undo"), "undo", "edit-undo.png"},
            {Language.translate("Copy"), "copy", "edit-copy.png"},
            {Language.translate("Paste"), "paste", "edit-paste.png"},
            {Language.translate("Show Page Events"), "toolbar_page_actions", "actions.png"},
            {Language.translate("Show Page Properties"), "toolbar_page_properties", "details.gif"},
            {Language.translate("Show Page Perspective"), "toolbar_page_perspective", "perspective_lines.png"},
            {Language.translate("Show Page Spreadsheet"), "toolbar_page_spreadsheet", "spreadsheet-icon.png"},
            {Language.translate("Zoom in/out"), "zoom_inout", "zoomin.gif"},
            {Language.translate("Zoom combo box"), "zoom_combo", ""},
            {Language.translate("Grid"), "grid", "snap-grid.png"},
            {Language.translate("Rulers"), "rulers", "rulers.png"},
            {Language.translate("Used Variables"), "toolbar_variables", "variable.png"},
            {Language.translate("Textual info"), "toolbar_textinfo", "info.gif"},
            {Language.translate("Page Wizards"), "toolbar_wizards", "wizard.png"},
            {Language.translate("PostIt Note"), "toolbar_postit", "postit.png"},
            {Language.translate("Show/hide Project Navigator"), "toolbar_right_panel", "tab.png"},
            {Language.translate("Help"), "toolbar_help", "help-browser2.png"},
            {Language.translate("Pages Toolbar"), "pages_toolbar"},
            {Language.translate("Move Page Up"), "pages_move_up", "go-up.png"},
            {Language.translate("Move Page Down"), "pages_move_down", "go-down.png"},
            {Language.translate("Page Transition Diagram"), "pages_diagram", "states.png"},
            {Language.translate("Drag Checkbox"), "pages_drag", "checked_checkbox.png"},
            {Language.translate("Variables Toolbar"), "variables_toolbar"},
            {Language.translate("Network Connectors"), "variables_network_connectors", "network-receive.png"},
            {Language.translate("Aggregate Variables"), "variables_aggregate", "formula.png"},
            {Language.translate("System Variables"), "variables_system", "system_variables.png"},
            {Language.translate("Pause Variable Updates"), "variables_pause", "stop.gif"},};
    Profile profile;
    Hashtable<String, JCheckBox> selectedItems = new Hashtable<String, JCheckBox>();
    ProfileManager profileManager;

    public ProfilePanel(Profile profile, ProfileManager profileManager) {
        this.profileManager = profileManager;
        this.profile = profile;
        setLayout(new BorderLayout());
        populateInterface();
    }

    public void selectAll(boolean bSelect) {
        for (JCheckBox check : selectedItems.values()) {
            check.setSelected(bSelect);
        }
    }

    JTextField nameField = new JTextField(15);

    public void populateInterface() {
        JToolBar nameTB = new JToolBar();
        nameTB.setFloatable(false);
        nameTB.add(new JLabel(Language.translate("Profile Name: ")));
        nameField.setText(profile.name);
        nameTB.add(nameField);
        add(nameTB, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridLayout(0, 1));

        for (int i = 0; i < items.length; i++) {
            final String row[] = items[i];
            String text = items[i][0];
            if (row.length <= 2) {//group
                JToolBar groupTB = new JToolBar();
                groupTB.setFloatable(false);
                JLabel check = new JLabel(" " + text);
                check.setFont(check.getFont().deriveFont(Font.BOLD));
                groupTB.add(check);
                if (!row[1].isEmpty()) {
                    JButton help = new JButton(Workspace.createImageIcon("resources/help-browser.png"));
                    help.setToolTipText("Describe '" + text + "'");
                    help.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent ae) {
                            profileManager.helpViewer.showAutoHelpByID(row[1]);
                        }
                    });
                    groupTB.add(help);
                } else {
                    groupTB.add(new JLabel());
                }
                panel.add(groupTB);
            } else if (row.length >= 3) {//selectible group
                JToolBar groupTB = new JToolBar();
                groupTB.setFloatable(false);
                JCheckBox check = new JCheckBox(text);
                String id = row[1];
                selectedItems.put(id, check);
                check.setSelected(profile.isActive(id));
                JLabel label = new JLabel(Workspace.createImageIcon("resources/" + row[2]));
                label.setText("   ");
                label.setHorizontalTextPosition(JLabel.LEFT);
                label.setPreferredSize(new Dimension(40, 24));
                groupTB.add(label);
                groupTB.add(check);

                if (row.length >= 4) {
                    JButton help = new JButton(Workspace.createImageIcon("resources/help-browser.png"));
                    help.setToolTipText("Describe '" + text + "'");
                    help.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent ae) {
                            profileManager.helpViewer.showAutoHelpByID(row[3]);
                        }
                    });
                    groupTB.add(help);
                }
                panel.add(groupTB);
            }
        }

        add(new JScrollPane(panel), BorderLayout.CENTER);

        JToolBar southTB = new JToolBar();
        southTB.setFloatable(false);

        JButton selectAll = new JButton(Language.translate("Select All"));
        selectAll.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                selectAll(true);
            }
        });
        JButton deselectAll = new JButton(Language.translate("Deselect All"));
        deselectAll.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                selectAll(false);
            }
        });
        JButton btnSave = new JButton(Language.translate("Save"), Workspace.createImageIcon("resources/ok.png"));
        btnSave.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                profileManager.saveProfiles();
                int n = GlobalProperties.get("active-profile", -1);

                if (n == -1) {
                    Profiles.activeProfile = null;
                } else if (n >= 10000) {
                    Profiles.activeProfile = Profiles.getStandardProfiles().elementAt(n - 10000);
                } else {
                    Profiles.activeProfile = Profiles.getProfiles().elementAt(n);
                }
                Profiles.loadMenu(SketchletEditor.getInstance().getProfilesMenu());
                SketchletEditorFrame.onProfileChange();
                profileManager.frame.setVisible(false);
            }
        });
        JButton btnCancel = new JButton(Language.translate("Cancel"), Workspace.createImageIcon("resources/cancel.png"));
        btnCancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                profileManager.frame.setVisible(false);
            }
        });

        southTB.add(selectAll);
        southTB.add(deselectAll);
        southTB.add(btnSave);
        southTB.add(btnCancel);

        add(southTB, BorderLayout.SOUTH);
    }

    public void saveToProfile() {
        profile.name = nameField.getText();
        profile.activeItems.removeAllElements();

        for (JCheckBox check : this.selectedItems.values()) {
            if (check.isSelected()) {
                for (int i = 0; i < items.length; i++) {
                    if (check.getText().equalsIgnoreCase(items[i][0])) {
                        profile.activeItems.add(items[i][1]);
                    }
                }
            }
        }
    }

    public static void main(String args[]) throws Exception {
        ProfileManager.main(args);
    }
}
