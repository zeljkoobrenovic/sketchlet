package net.sf.sketchlet.common.ui.fisheye;

/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 *
 * @author Ben Bederson
 */

import net.sf.sketchlet.util.UtilContext;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.FontRenderContext;
import java.net.URL;
import java.util.Vector;

public class ArrowMenu extends JMenu implements MenuListener {
    ArrowWindow arrowWindow;

    public ArrowMenu(String title) {
        super(title);
        addMenuListener(this);

        arrowWindow = new ArrowWindow(this);
    }

    public JMenuItem add(JMenuItem item) {
        arrowWindow.add(item);
        return item;
    }

    public void menuSelected(MenuEvent e) {
        Point location = getLocationOnScreen();
        arrowWindow.setLocation(location.x, location.y + getSize().height);
        arrowWindow.setVisible(true);
        arrowWindow.requestFocus();
        arrowWindow.repaint();
    }

    public void menuCanceled(MenuEvent e) {
        arrowWindow.setVisible(false);
    }

    public void menuDeselected(MenuEvent e) {
        arrowWindow.setVisible(false);
    }

    protected void fireMenuCanceled() {
        super.fireMenuCanceled();
    }
}

class AItem {
    JMenuItem menuItem;
    String label;

    public AItem(JMenuItem menuItem) {
        this.menuItem = menuItem;
        this.label = menuItem.getText();
    }

    public String toString() {
        return label;
    }
}

class ArrowWindow extends JWindow implements MouseListener, ActionListener {
    ArrowMenu menu;
    JList list;
    JViewport viewport;
    ;
    Vector items;
    Font font;
    int fontSize = 12;
    JButton up = null;
    JButton down = null;
    int index;
    javax.swing.Timer timer = null;
    int initialDelay = 300;
    int repeatDelay = 50;
    int increment = 0;
    Image image;
    URL resource;

    public ArrowWindow(ArrowMenu menu) {
        super();
        this.menu = menu;

        items = new Vector();

        viewport = new JViewport();
        list = new JList();
        font = new Font(null, Font.PLAIN, fontSize);
        list.setFont(font);
        list.setBackground(new Color(207, 207, 207));
        list.setForeground(Color.black);
        list.setSelectionBackground(new Color(144, 151, 207));
        list.setSelectionForeground(Color.black);
        list.addMouseListener(this);
        viewport.setView(list);

        resource = FishEyeMenu.class.getResource("arrow-up.gif");
        up = new JButton(UtilContext.getInstance().getImageIconFromResources("resources/arrow-up.gif"));
        up.setBackground(new Color(207, 207, 207));
        up.setBorder(null);
        up.addMouseListener(this);

        resource = FishEyeMenu.class.getResource("arrow-down.gif");
        down = new JButton(UtilContext.getInstance().getImageIconFromResources("resources/arrow-down.gif"));
        down.setBackground(new Color(207, 207, 207));
        down.setBorder(null);
        down.addMouseListener(this);

        Container pane = getContentPane();

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        panel.setLayout(new BorderLayout());
        panel.add(up, BorderLayout.NORTH);
        panel.add(viewport, BorderLayout.CENTER);
        panel.add(down, BorderLayout.SOUTH);

        pane.add(panel);

        timer = new javax.swing.Timer(repeatDelay, this);
        timer.setInitialDelay(initialDelay);
    }

    public void add(JMenuItem menuItem) {
        AItem item = new AItem(menuItem);
        items.addElement(item);
        list.setListData(items);
    }

    public void setVisible(boolean visible) {
        int width = 0;
        int height = 0;
        // Calculate width based on content
        FontRenderContext frc = new FontRenderContext(null, false, false);
        float stringWidth;
        for (int i = 0; i < items.size(); i++) {
            stringWidth = (float) font.getStringBounds(((AItem) items.elementAt(i)).label, frc).getWidth();
            if (stringWidth > width) {
                width = (int) stringWidth;
            }
        }

        // Calculate height based on available screen space
        Point location = getLocation();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        height = screenSize.height - location.y - 30;       // Leave space for Start Menu Bar

        index = 0;
        viewport.setViewPosition(list.indexToLocation(0));
        list.setSelectedIndex(0);

        setSize(width, height);
        super.setVisible(visible);
    }

    public void mousePressed(MouseEvent e) {
        increment = 0;
        if (e.getSource() == up) {
            if (index > 0) {
                increment = -1;
                timer.start();
            }
        } else if (e.getSource() == down) {
            if (list.getLastVisibleIndex() < items.size() - 1) {
                increment = 1;
                timer.start();
            }
        }
        index += increment;
        viewport.setViewPosition(list.indexToLocation(index));
    }

    public void mouseReleased(MouseEvent e) {
        if (timer != null) {
            timer.stop();
        }
        if (e.getSource() == list) {
            ((AItem) items.elementAt(list.getSelectedIndex())).menuItem.doClick();
            menu.fireMenuCanceled();
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void actionPerformed(ActionEvent e) {
        if (increment == 1) {
            if (list.getLastVisibleIndex() < items.size() - 1) {
                index++;
            }
        } else if (increment == -1) {
            if (index > 0) {
                index--;
            }
        }
        viewport.setViewPosition(list.indexToLocation(index));
    }
}
