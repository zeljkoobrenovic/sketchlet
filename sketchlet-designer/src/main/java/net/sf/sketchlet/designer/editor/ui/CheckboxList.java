package net.sf.sketchlet.designer.editor.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Component containing a list of checkboxes.
 */
public class CheckboxList extends JList {

    private boolean[] selected;

    /**
     * Class constructor.
     *
     * @param items Items with which to populate the list.
     */
    public CheckboxList(Object[] items) {
        super(items);
        this.selected = new boolean[items.length];
        for (int i = 0; i < items.length; i++) {
            this.selected[i] = true;
        }
        this.setCellRenderer(new CheckListCellRenderer());
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        CheckListListener listener = new CheckListListener();
        this.addMouseListener(listener);
        this.addKeyListener(listener);
    }

    /**
     * Class constructor.
     *
     * @param items Items with which to populate the list.
     */
    public CheckboxList(Object[] items, boolean[] selected) {
        super(items);
        this.selected = selected;

        this.setCellRenderer(new CheckListCellRenderer());
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        CheckListListener listener = new CheckListListener();
        this.addMouseListener(listener);
        this.addKeyListener(listener);
    }

    public static void main(String args[]) {
        //Create and set up the window.
        JFrame frame = new JFrame("FrameDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel emptyLabel = new JLabel("");
        emptyLabel.setPreferredSize(new Dimension(175, 100));
        frame.getContentPane().add(new CheckboxList(new String[]{"1", "2", "3"}), BorderLayout.CENTER);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Returns an array of the objects that have been selected.
     * Overrides the JList method.
     */
    public Object[] getSelectedValues() {
        java.util.List list = new java.util.ArrayList(this.selected.length);
        for (int i = 0; i < this.selected.length; i++) {
            if (selected[i]) {
                list.add(this.getModel().getElementAt(i));
            }
        }

        return list.toArray();
    }

    //===================================================== PRIVATE

    private static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

    private class CheckListCellRenderer
            extends JCheckBox
            implements ListCellRenderer {

        public CheckListCellRenderer() {
            this.setOpaque(true);
            this.setBorder(noFocusBorder);
        }

        public Component getListCellRendererComponent(JList list,
                                                      Object value, int index, boolean isSelected, boolean cellHasFocus) {

            this.setText(value.toString());
            this.setSelected(selected[index]);
            this.setFont(list.getFont());

            if (isSelected) {
                this.setBackground(list.getSelectionBackground());
                this.setForeground(list.getSelectionForeground());
            } else {
                this.setBackground(list.getBackground());
                this.setForeground(list.getForeground());
            }

            if (cellHasFocus) {
                this.setBorder(UIManager.getBorder("List.focusCellHighlightBorder"));
            } else {
                this.setBorder(noFocusBorder);
            }

            return this;
        }
    }

    public void selectAll() {
        for (int i = 0; i < this.selected.length; i++) {
            this.selected[i] = true;
        }

        repaint();
    }

    public void deselectAll() {
        for (int i = 0; i < this.selected.length; i++) {
            this.selected[i] = false;
        }

        repaint();
    }

    private class CheckListListener implements MouseListener, KeyListener {

        public void mouseClicked(MouseEvent e) {
            if (e.getX() < 20) {
                // Heuristic that they clicked on the checkbox part
                doCheck();
            }
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            if (e.getKeyChar() == ' ') {
                doCheck();
            }
        }

        public void keyTyped(KeyEvent e) {
        }

        public void keyReleased(KeyEvent e) {
        }

        private void doCheck() {
            int index = getSelectedIndex();
            if (index >= 0) {
                selected[index] = !selected[index];
            }
            repaint();
        }
    }


}
