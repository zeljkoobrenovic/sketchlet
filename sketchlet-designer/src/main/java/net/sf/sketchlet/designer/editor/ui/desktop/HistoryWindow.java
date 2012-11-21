/*
 * HistoryWindow.java
 *
 * Created on April 2, 2008, 12:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.sketchlet.designer.editor.ui.desktop;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.designer.Workspace;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class HistoryWindow extends JFrame {
    public static void main(String[] args) {
        new HistoryWindow();
    }

    private JList historyList;

    JButton restoreButton;
    JButton removeButton;
    JButton removeAllButton;

    String selectedDirectory;

    public HistoryWindow() {
        super("Workspace History");
        Container content = getContentPane();

        String[] entries = getHistoryDirectories();

        historyList = new JList(entries);
        historyList.setVisibleRowCount(10);
        historyList.addListSelectionListener(new ValueReporter());

        if (entries.length > 0) {
            historyList.setSelectedIndex(entries.length - 1);
            historyList.ensureIndexIsVisible(entries.length - 1);
        }
        JScrollPane listPane = new JScrollPane(historyList);

        JPanel listPanel = new JPanel();
        Border listPanelBorder = BorderFactory.createTitledBorder("History");
        listPanel.setBorder(listPanelBorder);
        listPanel.add(listPane);
        content.add(listPanel, BorderLayout.CENTER);

        JPanel valuePanel = new JPanel();
        valuePanel.setLayout(new BoxLayout(valuePanel, BoxLayout.Y_AXIS));
        Border valuePanelBorder = BorderFactory.createTitledBorder("Actions");
        valuePanel.setBorder(valuePanelBorder);

        restoreButton = new JButton("Restore");
        removeButton = new JButton("Remove");
        removeAllButton = new JButton("Remove All");

        restoreButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                restore();
            }
        });

        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                delete();
            }
        });

        removeAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                deleteAll();
            }
        });

        valuePanel.add(restoreButton);
        valuePanel.add(removeButton);
        valuePanel.add(new JLabel("---"));
        valuePanel.add(removeAllButton);

        enableItems();

        content.add(valuePanel, BorderLayout.EAST);
        pack();
        setVisible(true);
    }

    public void enableItems() {
        boolean enabled = this.historyList.getSelectedIndex() >= 0;

        restoreButton.setEnabled(enabled);
        removeButton.setEnabled(enabled);
    }

    int numOfRestoredFiles = 0;
    String message = "";

    public void restore() {
        numOfRestoredFiles = 0;
        message = "Restored <%=num-files%> file(s):\n";
        restore(SketchletContextUtils.getCurrentProjectDir(), this.selectedDirectory, null);

        message = message.replace("<%=num-files%>", "" + numOfRestoredFiles);

        Workspace.getProcessRunner().getIoServicesHandler().reloadProcesses();

        JOptionPane.showMessageDialog(this, message, "Restored Files", JOptionPane.INFORMATION_MESSAGE);
    }

    public void restore(String strDestination, String strDirectory, String subdirectory) {
        try {
            if (subdirectory != null) {
                new File(strDestination + File.separator + subdirectory).mkdirs();
            }

            String files[] = new File(strDirectory).list();

            for (int i = 0; i < files.length; i++) {
                String filename = files[i];
                File file = new File(strDirectory + File.separator + filename);

                if (file.isDirectory()) {
                    String dir;
                    if (subdirectory != null) {
                        dir = subdirectory + File.separator + filename;
                    } else {
                        dir = filename;
                    }
                    restore(strDestination, strDirectory + File.separator + filename, dir);
                } else {
                    String restoreFile = strDestination + File.separator + filename;
                    FileUtils.copyFile(file, new File(restoreFile));

                    if (numOfRestoredFiles < 15) {
                        message += "   " + (subdirectory == null ? "" : subdirectory + File.separator) + filename + "\n";
                    } else if (numOfRestoredFiles == 15) {
                        message += "   ...\n";
                    }

                    numOfRestoredFiles++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete() {
        if (FileUtils.deleteDir(new File(selectedDirectory))) {
            // JOptionPane.showMessageDialog( this, "History directory\n" + selectedDirectory + "\nsuccesfully removed.", "Directory Removed", JOptionPane.INFORMATION_MESSAGE );
        } else {
            JOptionPane.showMessageDialog(this, "Could not delete all files in\n" + selectedDirectory + "", "Error", JOptionPane.ERROR_MESSAGE);
        }

        historyList.setListData(this.getHistoryDirectories());
        enableItems();
    }


    public void deleteAll() {
        Object[] options = {"Continue with Remove All...", "Cancel"};
        int selectedValue = JOptionPane.showOptionDialog(this, "You are about to remove all items from history.\nThis operation is not reversable.", "Confirm Remove All",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);

        switch (selectedValue) {
            case 0:
                if (FileUtils.deleteDir(new File(selectedDirectory).getParentFile())) {
                    // JOptionPane.showMessageDialog( this, "History succesfully removed.", "Directory Removed", JOptionPane.INFORMATION_MESSAGE );
                } else {
                    JOptionPane.showMessageDialog(this, "Could not delete all files.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                break;
            case 1:
                return;
        }

        historyList.setListData(this.getHistoryDirectories());
        enableItems();
    }

    private class ValueReporter implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent event) {
            if (!event.getValueIsAdjusting()) {
                /*if (SketchletContextUtils.getCurrentProjectHistoryDir() != null && historyList.getSelectedValue() != null) {
                    selectedDirectory = SketchletContextUtils.getCurrentProjectHistoryDir() + historyList.getSelectedValue().toString();
                }*/
            }
        }
    }

    public static String[] getHistoryDirectories() {
        /*try {
            File historyDir = new File( SketchletContextUtils.getCurrentProjectHistoryDir() );
            
            String files[] = historyDir.list();
            
            if (files != null) {
                Arrays.sort( files );
                return files;
            } else {
                return new String[]{};
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        return new String[]{};
    }
}
