package net.sf.sketchlet.designer.eye.eye;

import net.sf.sketchlet.util.SpringUtilities;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class SelectorPanel extends JPanel implements ItemListener {

    public JCheckBox showVariables = new JCheckBox("Variables", true);
    public JCheckBox showSketches = new JCheckBox("All Sketches", true);
    public JCheckBox showRegions = new JCheckBox("Current Sketch Objects   ", true);
    public JCheckBox showMacros = new JCheckBox("Action Lists", true);
    public JCheckBox showTimers = new JCheckBox("Timers", true);
    public JCheckBox showScripts = new JCheckBox("Scripts", true);
    public JCheckBox showScreenActions = new JCheckBox("Screen Actions", true);
    String descCols[] = {"Description"};
    Vector<String> relatedTo = new Vector<String>();
    Vector<String> description = new Vector<String>();
    JTable table = new JTable();
    AbstractTableModel model;
    // JTextField selectedItem = new JTextField();
    EyeFrame eyeFrame;

    public SelectorPanel(EyeFrame eyeFrame) {
        this.eyeFrame = eyeFrame;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Selector"));
        JPanel selection = new JPanel(new SpringLayout());
        selection.add(showVariables);
        selection.add(new ColorPanel(20, 20, Color.RED));
        selection.add(showSketches);
        selection.add(new ColorPanel(20, 20, Color.GREEN));
        selection.add(showRegions);
        selection.add(new ColorPanel(20, 20, Color.BLACK));
        selection.add(showMacros);
        selection.add(new ColorPanel(20, 20, Color.BLUE));
        selection.add(showTimers);
        selection.add(new ColorPanel(20, 20, Color.MAGENTA));
        selection.add(showScripts);
        selection.add(new ColorPanel(20, 20, Color.CYAN));
        selection.add(showScreenActions);
        selection.add(new ColorPanel(20, 20, Color.BLUE));

        SpringUtilities.makeCompactGrid(selection,
                7, 2, //rows, cols
                5, 5, //initialX, initialY
                5, 5);//xPad, yPad    }

        table.setTableHeader(null);

        showVariables.addItemListener(this);
        showSketches.addItemListener(this);
        showRegions.addItemListener(this);
        showMacros.addItemListener(this);
        showTimers.addItemListener(this);
        showScripts.addItemListener(this);
        showScreenActions.addItemListener(this);

        table.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(table);

        add(selection, BorderLayout.NORTH);

        JPanel descPanel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new BorderLayout());
        //selectedItem.setEditable(false);
        //selectedItem.setEnabled(false);
        //selectedItem.putClientProperty("JComponent.sizeVariant", "small");
        //SwingUtilities.updateComponentTreeUI(selectedItem);
        //top.add(selectedItem, BorderLayout.CENTER);

        descPanel.add(top, BorderLayout.NORTH);
        descPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        add(descPanel, BorderLayout.CENTER);

        model = new AbstractTableModel() {

            public String getColumnName(int col) {
                return descCols[col].toString();
            }

            public int getRowCount() {
                return description.size();
            }

            public int getColumnCount() {
                return descCols.length;
            }

            public Object getValueAt(int row, int col) {
                if (col == 1) {
                    return relatedTo.elementAt(row);
                } else {
                    return description.elementAt(row);
                }
            }

            public boolean isCellEditable(int row, int col) {
                return true;
            }

            public void setValueAt(Object value, int row, int col) {
            }

            public Class getColumnClass(int c) {
                return String.class;
            }
        };

        table.setModel(model);

    }

    public void describeRelation(EyeSlot slot) {
        this.relatedTo.removeAllElements();
        this.description.removeAllElements();

        relatedTo.add(slot.getLongName());

        description.add(slot.getLongName());

        for (int i = 0; i < slot.relatedSlots.size(); i++) {
            EyeSlotRelation rel = slot.relatedSlotsInfo.elementAt(i);

            relatedTo.add(rel.slot2.getLongName());
            description.add("      " + rel.description);
        }

        model.fireTableDataChanged();
    }

    public void itemStateChanged(ItemEvent e) {
        eyeFrame.eyeData.load();
        eyeFrame.repaint();
    }

    public Dimension getPreferredSize() {
        return new Dimension(250, 180);
    }
}

class ColorPanel extends JPanel {
    int w;
    int h;
    Color color;

    public ColorPanel(int w, int h, Color color) {
        this.w = w;
        this.h = h;
        this.color = color;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(color);
        g.fillRect(0, 0, w, h);
    }

    public Dimension getPreferredSize() {
        return new Dimension(w, h);
    }

    public Dimension getMinimalSize() {
        return getPreferredSize();
    }
}