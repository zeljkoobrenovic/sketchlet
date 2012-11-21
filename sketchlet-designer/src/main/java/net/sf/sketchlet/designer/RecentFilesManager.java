/*
 * RecentFilesManager.java
 *
 * Created on March 25, 2008, 6:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the openExternalEditor.
 */
package net.sf.sketchlet.designer;

import net.sf.sketchlet.common.SimpleProperties;
import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.model.Page;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Vector;

/**
 * @author cuypers
 */
public class RecentFilesManager {
    private static final Logger log = Logger.getLogger(RecentFilesManager.class);
    public static final int NUMBER_OF_ITEMS = 21;
    static Vector<String> recentFiles = new Vector<String>();

    /**
     * Creates a new instance of RecentFilesManager
     */
    public RecentFilesManager() {
    }

    public static Vector<String> loadRecentFiles() {
        String fileName = "";
        try {
            fileName = SketchletContextUtils.getApplicationSettingsDir() + "recent_projects.txt";

            BufferedReader inStream = new BufferedReader(new FileReader(fileName));

            recentFiles.removeAllElements();

            String line;
            while ((line = inStream.readLine()) != null) {
                try {
                    String line2 = URLDecoder.decode(new File(line.trim()).toURL().toString(), "UTF8");
                    String line3 = line2.replace("file:", "");

                    String line4 = "file:" + line;
                    try {
                        line4 = new java.net.URL("file:" + line).getPath();
                        line4 = new File(line4).getPath();
                    } catch (Exception e) {
                    }

                    if (recentFiles.contains(line.trim())) {
                        recentFiles.remove(line);
                    }

                    if (recentFiles.contains(line2)) {
                        recentFiles.remove(line2);
                    }


                    if (recentFiles.contains(line3)) {
                        recentFiles.remove(line3);
                    }

                    if (line4 != null && recentFiles.contains(line4)) {
                        recentFiles.remove(line4);
                    }

                } catch (Exception e) {
                    log.error(e);
                }

                File file = new File(line.trim());
                if (file.exists()) {
                    recentFiles.add(file.getPath());
                }
            }
        } catch (Exception e) {
            log.error("Could not open " + fileName + ". Recent file list is empty.", e);
        }

        return recentFiles;
    }

    public static void removeRecentFile(String file) {
        recentFiles.remove(file);
    }

    public static void addRecentFile(String file) {
        recentFiles.add(new File(file).getPath());

        saveRecentFiles();
        loadRecentFiles();

        populateMenu();
    }

    public static void saveRecentFiles() {
        try {
            String fileName = SketchletContextUtils.getApplicationSettingsDir() + "recent_projects.txt";

            PrintWriter out = new PrintWriter(new FileWriter(fileName));

            int startIndex = 0;

            if (recentFiles.size() > NUMBER_OF_ITEMS) {
                startIndex = recentFiles.size() - NUMBER_OF_ITEMS;
            }

            int index = 0;

            for (String file : recentFiles) {
                if (index++ >= startIndex) {
                    out.println(file);
                }
            }

            out.flush();
            out.close();
        } catch (Exception e) {
            log.error(e);
        }
    }

    public static void populateMenu() {
        if (Workspace.getMainPanel() == null || Workspace.getMainPanel().recentProjectsMenu == null) {
            return;
        }

        JMenu menu = Workspace.getMainPanel().recentProjectsMenu;
        menu.removeAll();

        int startIndex = 0;

        if (recentFiles.size() > NUMBER_OF_ITEMS) {
            startIndex = recentFiles.size() - NUMBER_OF_ITEMS;
        }

        int index = 0;

        for (final String file : recentFiles) {
            if (!new File(file).exists()) {
                continue;
            }

            if (index++ >= startIndex) {
                SimpleProperties props = new SimpleProperties();
                props.loadData(file);
                String strParent;

                if (new File(file).getParentFile().getName().equals(SketchletContextUtils.sketchletDataDir())) {
                    strParent = new File(file).getParentFile().getParent();
                } else {
                    strParent = new File(file).getParent();
                }

                String strTitle = props.getString("title") + " (" + strParent + ")";

                JMenuItem menuItem = new JMenuItem(strTitle);
                menu.add(menuItem, 0);

                menuItem.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent event) {
                        try {
                            if (!net.sf.sketchlet.designer.editor.SketchletEditor.close()) {
                                return;
                            }
                            Workspace.getMainPanel().saveConfiguration();
                            Workspace.getProcessRunner().getIoServicesHandler().loadProcesses(new File(file).toURL(), false);
                        } catch (Exception e) {
                            log.error(e);
                        }
                    }
                });
            }
        }

        for (int i = 0; i < menu.getItemCount(); i++) {
            JMenuItem menuItem = menu.getItem(i);
            menuItem.setText((i + 1) + " : " + menuItem.getText());
            if (i < Page.NUMBER_OF_LAYERS) {
                menuItem.setMnemonic('1' + i);
            }
        }

        int mc = menu.getItemCount();
        for (int i = 4, j = 0; i < mc; i += 4, j++) {
            menu.insertSeparator(i + j);
        }

        menu.setEnabled(menu.getItemCount() > 0);
    }

    public static void loadLastProject() {
        if (recentFiles == null || recentFiles.size() == 0 || Workspace.getProcessRunner() == null) {
            return;
        }

        String file = recentFiles.lastElement();
        try {
            if (new File(file).exists()) {
                Workspace.getProcessRunner().getIoServicesHandler().loadProcesses(new File(file).toURL(), false);
            }
        } catch (Exception e) {
            log.error(e);
        }
    }
}
