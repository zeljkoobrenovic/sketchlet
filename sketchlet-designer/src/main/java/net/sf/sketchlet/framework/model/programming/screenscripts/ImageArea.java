package net.sf.sketchlet.framework.model.programming.screenscripts;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.ui.UIUtils;
import net.sf.sketchlet.designer.editor.ui.macros.ImageAreaSelect;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class ImageArea extends JPanel {

    private static Image image;
    private ActionListTable actionTable;
    private int mouseX = 0;
    private int mouseY = 0;
    private CaptureFrame frame;

    public ImageArea(final CaptureFrame frame, ActionListTable _actionTable) {
        this.frame = frame;
        this.actionTable = _actionTable;
        popup = new JPopupMenu();

        JMenu menu;
        JMenuItem menuItem;

        this.setBorder(BorderFactory.createTitledBorder(Language.translate("Screen Image")));

        menuItem = new JMenuItem(Language.translate("Move Mouse Cursor Here"));
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MouseMoveAction action = new MouseMoveAction(mouseX + "," + mouseY);
                actionTable.addRobotAction(action);
                repaint();
            }
        });
        popup.add(menuItem);

        menu = new JMenu(Language.translate("Click Mouse Button Here"));

        menuItem = new JMenuItem(Language.translate("Left Button"));
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MouseLeftButtonClickAction action = new MouseLeftButtonClickAction(mouseX + "," + mouseY);
                actionTable.addRobotAction(action);
                repaint();
            }
        });
        menu.add(menuItem);
        menuItem = new JMenuItem(Language.translate("Right Button"));
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MouseRightButtonClickAction action = new MouseRightButtonClickAction(mouseX + "," + mouseY);
                actionTable.addRobotAction(action);
                repaint();
            }
        });
        menu.add(menuItem);
        menuItem = new JMenuItem(Language.translate("Middle Button"));
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MouseMiddleButtonClickAction action = new MouseMiddleButtonClickAction(mouseX + "," + mouseY);
                actionTable.addRobotAction(action);
                repaint();
            }
        });
        menu.add(menuItem);
        // menuItem.addActionListener(this);
        popup.add(menu);

        popup.addSeparator();

        menu = new JMenu(Language.translate("Press Mouse Button Here"));
        menuItem = new JMenuItem(Language.translate("Left Button"));
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MouseLeftButtonPressAction action = new MouseLeftButtonPressAction(mouseX + "," + mouseY);
                actionTable.addRobotAction(action);
                repaint();
            }
        });
        menu.add(menuItem);
        menuItem = new JMenuItem(Language.translate("Right Button"));
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MouseRightButtonPressAction action = new MouseRightButtonPressAction(mouseX + "," + mouseY);
                actionTable.addRobotAction(action);
                repaint();
            }
        });
        menu.add(menuItem);
        menuItem = new JMenuItem(Language.translate("Middle Button"));
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MouseMiddleButtonPressAction action = new MouseMiddleButtonPressAction(mouseX + "," + mouseY);
                actionTable.addRobotAction(action);
                repaint();
            }
        });
        menu.add(menuItem);
        // menuItem.addActionListener(this);
        popup.add(menu);

        menu = new JMenu(Language.translate("Release Mouse Button Here"));
        menuItem = new JMenuItem(Language.translate("Left Button"));
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MouseLeftButtonReleaseAction action = new MouseLeftButtonReleaseAction(mouseX + "," + mouseY);
                actionTable.addRobotAction(action);
                repaint();
            }
        });
        menu.add(menuItem);
        menuItem = new JMenuItem(Language.translate("Right Button"));
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MouseRightButtonReleaseAction action = new MouseRightButtonReleaseAction(mouseX + "," + mouseY);
                actionTable.addRobotAction(action);
                repaint();
            }
        });
        menu.add(menuItem);
        menuItem = new JMenuItem(Language.translate("Middle Button"));
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MouseMiddleButtonReleaseAction action = new MouseMiddleButtonReleaseAction(mouseX + "," + mouseY);
                actionTable.addRobotAction(action);
                repaint();
            }
        });
        menu.add(menuItem);

        popup.add(menu);

        menuItem = new JMenuItem(Language.translate("Restore Mouse Original Position"));
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MouseRestoreAction action = new MouseRestoreAction();
                actionTable.addRobotAction(action);
                repaint();
            }
        });
        popup.add(menuItem);

        popup.addSeparator();

        menuItem = new JMenuItem(Language.translate("Paste Text..."));
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String strText = showVariableInputDialog(frame, Language.translate("Paste Text"), Language.translate("  Text to be pasted  "), "", Workspace.createImageIcon("resources/edit-paste.png"));

                if (strText != null && strText.length() > 0) {
                    PasteTextAction action = new PasteTextAction(strText);
                    actionTable.addRobotAction(action);
                    repaint();
                }
            }
        });
        popup.add(menuItem);

        menuItem = new JMenuItem(Language.translate("Press Key(s)..."));
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                KeysFrame keysFrame = new KeysFrame(frame);
                String strText = keysFrame.getText();

                if (strText != null && strText.length() > 0) {
                    TypeKeysAction action = new TypeKeysAction(strText);
                    actionTable.addRobotAction(action);
                    repaint();
                }
            }
        });
        popup.add(menuItem);

        popup.addSeparator();

        menuItem = new JMenuItem(Language.translate("Capture a part of the screen"));
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                BufferedImage img = null;

                if (ImageArea.image != null) {
                    try {
                        int w = ImageArea.image.getWidth(null);
                        int h = ImageArea.image.getHeight(null);
                        img = Workspace.createCompatibleImage(w, h);
                        Graphics2D g2 = img.createGraphics();
                        g2.drawImage(image, 0, 0, null);
                        g2.dispose();
                    } catch (Exception e2) {
                    }
                }
                ImageAreaSelect.createAndShowGUI(frame, img, 0, 0, 100, 100, true);

                if (ImageAreaSelect.bSaved) {
                    String strText = ImageAreaSelect.strVariable + " " + ImageAreaSelect.strArea;

                    if (strText != null && strText.length() > 0) {
                        CaptureScreenAction action = new CaptureScreenAction(strText);
                        actionTable.addRobotAction(action);
                        repaint();
                    }
                }
            }
        });
        popup.add(menuItem);

        popup.addSeparator();

        menuItem = new JMenuItem(Language.translate("Pause..."));
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                String strPause = showVariableInputDialog(frame, Language.translate("Pause"), Language.translate("  Pause (in ms)  "), "", Workspace.createImageIcon("resources/timer.png"));
                if (strPause != null) {
                    PauseAction action = new PauseAction(strPause);
                    actionTable.addRobotAction(action);
                    repaint();
                }
            }
        });
        popup.add(menuItem);

        menuItem = new JMenuItem(Language.translate("Update Variable..."));
        menuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String strVarValue = showVariableInputDialog2(frame, Language.translate("Update Variable"), Language.translate("   Enter variable and value"), "", Workspace.createImageIcon("resources/edit-paste.png"));
                ;
                if (strVarValue != null) {
                    UpdateVariableAction action = new UpdateVariableAction(strVarValue);
                    actionTable.addRobotAction(action);
                    repaint();
                }
            }
        });
        popup.add(menuItem);

        this.addMouseListener(new PopupListener());
        addMouseListener(new PopupListener());

        if (this.image != null) {
            this.setImage(image);
        }

    }

    JPopupMenu popup;
    static String strText = "";

    public static String showVariableInputDialog(JFrame frame, String strTitle, String strFirstLine, String strDefault, ImageIcon icon) {

        final JDialog dialog = new JDialog(frame);
        dialog.setIconImage(icon.getImage());
        dialog.setTitle(strTitle);
        dialog.setModal(true);

        final JComboBox cmb = new JComboBox();

        UIUtils.populateVariablesCombo(cmb, true);

        dialog.add(cmb);

        JButton btnYes = new JButton(Language.translate("OK"));
        JButton btnCancel = new JButton(Language.translate("Cancel"));

        JPanel pane = new JPanel();
        pane.add(btnYes);
        pane.add(btnCancel);

        dialog.getRootPane().setDefaultButton(btnYes);

        btnYes.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                strText = cmb.getSelectedItem().toString();
                dialog.setVisible(false);
            }
        });

        btnCancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                strText = null;
                dialog.setVisible(false);
            }
        });

        dialog.add(new JLabel(strFirstLine), BorderLayout.NORTH);
        dialog.add(new JLabel(icon), BorderLayout.WEST);
        dialog.add(new JLabel("  "), BorderLayout.EAST);
        dialog.add(pane, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);

        return strText;
    }

    public static String showVariableInputDialog2(JFrame frame, String strTitle, String strFirstLine, String strDefault, ImageIcon icon) {

        final JDialog dialog = new JDialog(frame);
        dialog.setIconImage(icon.getImage());
        dialog.setTitle(strTitle);
        dialog.setModal(true);

        final JComboBox cmb = new JComboBox();
        final JComboBox cmb2 = new JComboBox();

        UIUtils.populateVariablesCombo(cmb, false);
        UIUtils.populateVariablesCombo(cmb2, true);

        JPanel cmbPane = new JPanel();
        cmbPane.add(cmb);
        cmbPane.add(new JLabel("  =  "));
        cmbPane.add(cmb2);

        dialog.add(cmbPane);

        JButton btnYes = new JButton(Language.translate("OK"));
        JButton btnCancel = new JButton(Language.translate("Cancel"));

        JPanel pane = new JPanel();
        pane.add(btnYes);
        pane.add(btnCancel);

        btnYes.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                strText = cmb.getSelectedItem().toString() + "=" + cmb2.getSelectedItem().toString();
                dialog.setVisible(false);
            }
        });

        btnCancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                strText = null;
                dialog.setVisible(false);
            }
        });

        dialog.add(new JLabel(strFirstLine), BorderLayout.NORTH);
        dialog.add(new JLabel(icon), BorderLayout.WEST);
        dialog.add(new JLabel("   "), BorderLayout.EAST);
        dialog.add(pane, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);

        return strText;
    }

    class PopupListener extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            mouseX = e.getX();
            mouseY = e.getY();
            //maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            //if (e.isPopupTrigger()) {
            popup.show(e.getComponent(),
                    e.getX(), e.getY());
            //}
        }
    }

    public Image getImage() {
        return image;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (image != null) {
            g.drawImage(image, 0, 0, null);
        } else {
            g.drawString(Language.translate("In this area you can capture or paste screen (from the menu), and click on the image to generate eactions."),
                    50, 50);
        }

        int i = 0;
        for (RobotAction action : this.actionTable.getActions()) {
            action.paint(g, i++, action.getName());
        }
    }

    public void setImage(Image image) {
        this.image = image;
        setPreferredSize(new Dimension(image.getWidth(this), image.getHeight(this)));
        revalidate();

        repaint();
    }
}
