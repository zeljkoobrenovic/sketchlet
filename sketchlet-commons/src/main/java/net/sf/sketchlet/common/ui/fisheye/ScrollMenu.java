package net.sf.sketchlet.common.ui.fisheye;

/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 *
 * @author Ben Bederson
 */

import java.awt.*;
import java.awt.font.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class ScrollMenu extends JMenu implements MenuListener {
    ScrollWindow scrollWindow;

    public ScrollMenu(String title) {
	super(title);
	addMenuListener(this);

	scrollWindow = new ScrollWindow(this);
    }

    public JMenuItem add(JMenuItem item) {
	scrollWindow.add(item);
	return item;
    }

    public void menuSelected(MenuEvent e) {
	Point location = getLocationOnScreen();
	scrollWindow.setLocation(location.x, location.y + getSize().height);
	scrollWindow.setVisible(true);
	scrollWindow.requestFocus();
	scrollWindow.repaint();
    }

    public void menuCanceled(MenuEvent e) {
	scrollWindow.setVisible(false);
    }

    public void menuDeselected(MenuEvent e) {
	scrollWindow.setVisible(false);
    }

    protected void fireMenuCanceled() {
	super.fireMenuCanceled();
    }
}

class SWItem {
    JMenuItem menuItem;
    String label;

    public SWItem(JMenuItem menuItem) {
	this.menuItem = menuItem;
	this.label = menuItem.getText();
    }

    public String toString() {
	return label;
    }
}

class ScrollWindow extends JWindow implements MouseListener {
    ScrollMenu menu;
    JList list;
    JScrollPane scrollPane;
    Vector items;
    Font font;
    int fontSize = 12;

    public ScrollWindow(ScrollMenu menu) {
	super();
	this.menu = menu;

	items = new Vector();

	scrollPane = new JScrollPane();
	list = new JList();
	font = new Font(null, Font.PLAIN, fontSize);
	list.setFont(font);
	list.setBackground(new Color(207, 207, 207));
	list.setForeground(Color.black);
	list.setSelectionBackground(new Color(144, 151, 207));
	list.setSelectionForeground(Color.black);
	list.addMouseListener(this);
	scrollPane.getViewport().setView(list);
	getContentPane().add(scrollPane);
    }

    public void add(JMenuItem menuItem) {
	SWItem item = new SWItem(menuItem);
	items.addElement(item);
	list.setListData(items);
    }

    public void setVisible(boolean visible) {
	int width = 0;
	int height = 0;
				// Calculate width based on content
	FontRenderContext frc = new FontRenderContext(null, false, false);
	float stringWidth;
	for (int i=0; i<items.size(); i++) {
	    stringWidth = (float)font.getStringBounds(((SWItem)items.elementAt(i)).label, frc).getWidth();
	    if (stringWidth > width) {
		width = (int)stringWidth + 25;   // Leave room for vertical scrollbar
	    }
	}

				// Calculate height based on available screen space
	Point location = getLocation();
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	height = screenSize.height - location.y - 30;       // Leave space for Start Menu Bar

	scrollPane.getViewport().setViewPosition(list.indexToLocation(0));
	list.setSelectedIndex(0);

	setSize(width, height);
	super.setVisible(visible);
    }

    public void mouseReleased(MouseEvent e) {
	((SWItem)items.elementAt(list.getSelectedIndex())).menuItem.doClick();
	menu.fireMenuCanceled();
    }
    public void mousePressed(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}
