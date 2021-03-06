package net.sf.sketchlet.designer.editor.ui.region;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.framework.model.Connector;
import net.sf.sketchlet.util.Colors;
import net.sf.sketchlet.util.SpringUtilities;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * @author zobrenovic
 */
public class ConnectorPanel extends JPanel {

    JComboBox lineColor = new JComboBox();
    JComboBox lineThickness = new JComboBox();
    JComboBox lineStyle = new JComboBox();
    JComboBox fillColor = new JComboBox();
    JComboBox textColor = new JComboBox();
    JComboBox fontName = new JComboBox();
    JComboBox fontStyle = new JComboBox();
    JComboBox fontSize = new JComboBox();
    JTable tableMapping;
    JButton deleteMapping = new JButton(Language.translate("Delete"));
    JButton moveUpMapping = new JButton(Language.translate("Move Up"));
    JButton moveDownMapping = new JButton(Language.translate("Move Down"));
    AbstractTableModel modelMapping;
    Connector connector;
    public static String[] columnsProperties = new String[]{Language.translate("Property"), Language.translate("Value"), Language.translate("Description")};
    public static String[] columnsMapping = new String[]{Language.translate("Variable"), Language.translate("a (offset)"), Language.translate("b (factor)"), Language.translate("Format")};
    String strOldCaption;
    String strOldLineThickness;
    String strOldLineStyle;
    String strOldLineColor;
    String strOlTextColor;
    String strOldFontName;
    String strOldFontStyle;
    String strOldFontSize;
    String oldMapping[][];

    public ConnectorPanel(Connector connector) {
        this.connector = connector;

        this.strOldCaption = connector.getCaption();
        this.strOldLineThickness = connector.getLineThickness();
        this.strOldLineStyle = connector.getLineStyle();
        this.strOldLineColor = connector.getLineColor();
        this.strOlTextColor = connector.getTextColor();
        this.strOldFontName = connector.getFontName();
        this.strOldFontStyle = connector.getFontStyle();
        this.strOldFontSize = connector.getFontSize();

        oldMapping = new String[connector.getVariablesMapping().length][4];

        for (int i = 0; i < connector.getVariablesMapping().length; i++) {
            for (int j = 0; j < connector.getVariablesMapping()[i].length; j++) {
                oldMapping[i][j] = connector.getVariablesMapping()[i][j];
            }
        }

        this.setup();
    }

    public void setup() {
        this.setLayout(new BorderLayout());

        JPanel panelProperties = new JPanel(new SpringLayout());
        panelProperties.add(new JLabel(Language.translate("Line Style")));
        panelProperties.add(lineStyle);
        panelProperties.add(new JLabel(""));
        panelProperties.add(new JLabel(Language.translate("Line Thickness")));
        panelProperties.add(lineThickness);
        panelProperties.add(new JLabel(""));
        panelProperties.add(new JLabel(Language.translate("Line Color")));
        panelProperties.add(lineColor);
        JButton btnLineColor = new JButton("...");
        btnLineColor.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                Color newColor = JColorChooser.showDialog(
                        SketchletEditor.getInstance(),
                        Language.translate("Choose Color"),
                        Color.BLACK);

                if (newColor != null) {
                    String strColor = newColor.getRed() + " " + newColor.getGreen() + " " + newColor.getBlue();
                    lineColor.setSelectedItem(strColor);
                }
            }
        });
        panelProperties.add(btnLineColor);
        panelProperties.add(new JLabel(Language.translate("Fill Color")));
        panelProperties.add(fillColor);
        JButton btnFillColor = new JButton("...");
        btnFillColor.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                Color newColor = JColorChooser.showDialog(
                        SketchletEditor.getInstance(),
                        Language.translate("Choose Color"),
                        Color.BLACK);

                if (newColor != null) {
                    String strColor = newColor.getRed() + " " + newColor.getGreen() + " " + newColor.getBlue();
                    fillColor.setSelectedItem(strColor);
                }
            }
        });
        panelProperties.add(btnFillColor);

        panelProperties.add(new JLabel(Language.translate("Text Color")));
        panelProperties.add(textColor);
        JButton btnTextColor = new JButton("...");
        btnTextColor.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                Color newColor = JColorChooser.showDialog(
                        SketchletEditor.getInstance(),
                        Language.translate("Choose Color"),
                        Color.BLACK);

                if (newColor != null) {
                    String strColor = newColor.getRed() + " " + newColor.getGreen() + " " + newColor.getBlue();
                    textColor.setSelectedItem(strColor);
                }
            }
        });
        panelProperties.add(btnTextColor);

        panelProperties.add(new JLabel(Language.translate("Font")));
        panelProperties.add(fontName);
        panelProperties.add(new JLabel(""));

        panelProperties.add(new JLabel(Language.translate("Font Style")));
        panelProperties.add(fontStyle);
        panelProperties.add(new JLabel(""));

        panelProperties.add(new JLabel(Language.translate("Font Size")));
        panelProperties.add(fontSize);
        panelProperties.add(new JLabel(""));


        lineColor.setEditable(true);
        fillColor.setEditable(true);
        textColor.setEditable(true);
        fontName.setEditable(true);
        fontStyle.setEditable(true);
        fontSize.setEditable(true);

        lineThickness.removeAllItems();
        lineThickness.setEditable(true);
        lineThickness.addItem("");
        lineStyle.removeAllItems();
        lineStyle.setEditable(true);
        lineStyle.addItem("");
        lineStyle.addItem("no outline");
        lineStyle.addItem("------");
        lineStyle.addItem("regular");
        lineStyle.addItem("wobble");
        lineStyle.addItem("brush");
        lineStyle.addItem("empty");
        lineStyle.addItem("dashed 1");
        lineStyle.addItem("dashed 2");
        lineStyle.addItem("------");

        lineColor.removeAllItems();
        lineColor.setEditable(true);
        lineColor.addItem("");
        Colors.addColorNamesToCombo(lineColor);
        lineColor.addItem("------");

        fillColor.removeAllItems();
        fillColor.addItem("");
        fillColor.setEditable(true);
        Colors.addColorNamesToCombo(fillColor);
        fillColor.addItem("------");

        textColor.removeAllItems();
        textColor.addItem("");
        textColor.setEditable(true);
        Colors.addColorNamesToCombo(textColor);
        textColor.addItem("------");

        fontName.removeAllItems();
        fontName.addItem("");
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        fontName.addItem("----------");
        for (int i = 0; i < fonts.length; i++) {
            fontName.addItem(fonts[i]);
        }

        fontStyle.removeAllItems();
        fontStyle.addItem("");
        String[] styles = {"Regular", "Italic", "Bold", "Bold Italic"};
        fontStyle.addItem("----------");
        for (int i = 0; i < styles.length; i++) {
            fontStyle.addItem(styles[i]);
        }

        fontSize.removeAllItems();
        fontSize.addItem("");
        fontSize.addItem("----------");
        for (int i = 8; i <= 72; i += 2) {
            fontSize.addItem(i + "");
        }

        for (int i = 1; i <= 10; i++) {
            lineThickness.addItem("" + i);
        }
        lineThickness.addItem("------");

        if (VariablesBlackboard.getInstance() != null) {
            for (String strVar : VariablesBlackboard.getInstance().getVariablesList()) {
                lineColor.addItem("=" + strVar);
                lineThickness.addItem("=" + strVar);
                lineStyle.addItem("=" + strVar);
                fillColor.addItem("=" + strVar);
                textColor.addItem("=" + strVar);
                fontName.addItem("=" + strVar);
                fontStyle.addItem("=" + strVar);
                fontSize.addItem("=" + strVar);
            }
        }

        lineColor.setSelectedItem(connector.getLineColor());
        lineThickness.setSelectedItem(connector.getLineThickness());
        lineStyle.setSelectedItem(connector.getLineStyle());
        fillColor.setSelectedItem(connector.getFillColor());
        textColor.setSelectedItem(connector.getTextColor());
        fontName.setSelectedItem(connector.getFontName());
        fontStyle.setSelectedItem(connector.getFontStyle());
        fontSize.setSelectedItem(connector.getFontSize());

        setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        lineColor.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (lineColor.getSelectedItem() != null) {
                    connector.setLineColor((String) lineColor.getSelectedItem());
                    refresh();
                }
            }
        });
        lineThickness.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (lineThickness.getSelectedItem() != null) {
                    connector.setLineThickness((String) lineThickness.getSelectedItem());
                    refresh();
                }
            }
        });
        lineStyle.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (lineStyle.getSelectedItem() != null) {
                    connector.setLineStyle((String) lineStyle.getSelectedItem());
                    refresh();
                }
            }
        });
        fillColor.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (fillColor.getSelectedItem() != null) {
                    connector.setFillColor((String) fillColor.getSelectedItem());
                    refresh();
                }
            }
        });
        textColor.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (textColor.getSelectedItem() != null) {
                    connector.setTextColor((String) textColor.getSelectedItem());
                    refresh();
                }
            }
        });
        fontName.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (fontName.getSelectedItem() != null) {
                    connector.setFontName((String) fontName.getSelectedItem());
                    refresh();
                }
            }
        });
        fontStyle.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (fontStyle.getSelectedItem() != null) {
                    connector.setFontStyle((String) fontStyle.getSelectedItem());
                    refresh();
                }
            }
        });
        fontSize.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (fontSize.getSelectedItem() != null) {
                    connector.setFontSize((String) fontSize.getSelectedItem());
                    refresh();
                }
            }
        });
        this.modelMapping = new AbstractTableModel() {

            public String getColumnName(int col) {
                return columnsMapping[col];
            }

            public int getColumnCount() {
                return columnsMapping.length;
            }

            public int getRowCount() {
                return connector.getVariablesMapping().length;
            }

            public Object getValueAt(int row, int col) {
                return connector.getVariablesMapping()[row][col];
            }

            public boolean isCellEditable(int row, int col) {
                return true;
            }

            public void setValueAt(Object value, int row, int col) {
                connector.getVariablesMapping()[row][col] = value.toString();
            }

            public Class getColumnClass(int c) {
                return String.class;
            }
        };

        this.tableMapping = new JTable(modelMapping);
        JComboBox comboBoxFormat = new JComboBox();
        comboBoxFormat.setEditable(true);
        comboBoxFormat.addItem("");

        comboBoxFormat.addItem("0");
        comboBoxFormat.addItem("00");
        comboBoxFormat.addItem("000");
        comboBoxFormat.addItem("0.00");

        TableColumn formatColumn = this.tableMapping.getColumnModel().getColumn(3);
        formatColumn.setCellEditor(new DefaultCellEditor(comboBoxFormat));

        this.tableMapping.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                enableControls();
            }
        });

        this.deleteMapping.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableMapping.getSelectedRow();
                if (row > 0) {
                    for (int i = row; i < connector.getVariablesMapping().length - 1; i++) {
                        connector.getVariablesMapping()[i] = connector.getVariablesMapping()[i + 1];
                    }
                    connector.getVariablesMapping()[connector.getVariablesMapping().length - 1] = new String[]{"", "", "", ""};
                    modelMapping.fireTableDataChanged();
                }
            }
        });

        this.moveUpMapping.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableMapping.getSelectedRow();
                if (row > 0) {
                    String row1[] = connector.getVariablesMapping()[row - 1];
                    String row2[] = connector.getVariablesMapping()[row];
                    connector.getVariablesMapping()[row - 1] = row2;
                    connector.getVariablesMapping()[row] = row1;
                    modelMapping.fireTableDataChanged();
                }
            }
        });

        this.moveDownMapping.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableMapping.getSelectedRow();
                if (row < connector.getVariablesMapping().length - 1) {
                    String row1[] = connector.getVariablesMapping()[row];
                    String row2[] = connector.getVariablesMapping()[row + 1];
                    connector.getVariablesMapping()[row] = row2;
                    connector.getVariablesMapping()[row + 1] = row1;
                    modelMapping.fireTableDataChanged();
                }
            }
        });


        JScrollPane tableMappingScrollPane = new JScrollPane(tableMapping);
        tableMappingScrollPane.setPreferredSize(new Dimension(300, 100));
        tableMappingScrollPane.setBorder(BorderFactory.createTitledBorder(Language.translate("Variable Mapping")));

        JPanel panelButtonsMapping = new JPanel();
        panelButtonsMapping.add(deleteMapping);
        panelButtonsMapping.add(moveUpMapping);
        panelButtonsMapping.add(moveDownMapping);
        JPanel mappingPanel = new JPanel(new BorderLayout());
        mappingPanel.add(tableMappingScrollPane);
        mappingPanel.add(panelButtonsMapping, BorderLayout.SOUTH);
        this.add(mappingPanel, BorderLayout.EAST);

        SpringUtilities.makeCompactGrid(panelProperties,
                8, 3, //rows, cols
                5, 5, //initialX, initialY
                5, 5);//xPad, yPad
        JScrollPane scrollPanelProperties = new JScrollPane(panelProperties);
        scrollPanelProperties.setBorder(BorderFactory.createTitledBorder(Language.translate("Visual Properties")));
        this.add(scrollPanelProperties, BorderLayout.CENTER);

        JPanel panelTitle = new JPanel(new SpringLayout());
        panelTitle.add(new JLabel(Language.translate("Caption: ")));
        final JTextField captionField = new JTextField();
        captionField.setText(connector.getCaption());
        panelTitle.add(captionField);
        SpringUtilities.makeCompactGrid(panelTitle,
                1, 2, //rows, cols
                5, 5, //initialX, initialY
                5, 5);//xPad, yPad

        panelTitle.setBorder(BorderFactory.createTitledBorder(Language.translate("Connects region ") + this.connector.getRegion1().getName() + Language.translate(" to region " + this.connector.getRegion2().getName())));
        this.add(panelTitle, BorderLayout.NORTH);
        captionField.addKeyListener(new KeyAdapter() {

            public void keyReleased(KeyEvent e) {
                connector.setCaption(captionField.getText());
                SketchletEditor.getInstance().repaintEverything();
            }
        });

        enableControls();
    }

    public void save() {
    }

    public void cancel() {
        connector.setCaption(this.strOldCaption);
        connector.setLineThickness(this.strOldLineThickness);
        connector.setLineStyle(this.strOldLineStyle);
        connector.setLineColor(this.strOldLineColor);
        connector.setTextColor(this.strOlTextColor);
        connector.setFontName(this.strOldFontName);
        connector.setFontStyle(this.strOldFontStyle);
        connector.setFontSize(this.strOldFontSize);
        for (int i = 0; i < oldMapping.length; i++) {
            for (int j = 0; j < oldMapping[i].length; j++) {
                connector.getVariablesMapping()[i][j] = oldMapping[i][j];
            }
        }
    }

    public void enableControls() {
        int row = this.tableMapping.getSelectedRow();

        this.deleteMapping.setEnabled(row >= 0);
        this.moveUpMapping.setEnabled(row > 0);
        this.moveDownMapping.setEnabled(row >= 0 && row < connector.getVariablesMapping().length - 1);
    }

    public static void createAndShowGUI(Connector connector) {
        final JDialog frame = new JDialog(SketchletEditor.editorFrame);
        frame.setModal(true);
        frame.setTitle(Language.translate("Connector"));
        final ConnectorPanel panel = new ConnectorPanel(connector);
        frame.add(panel);

        JButton saveButton = new JButton(Language.translate("Save"), Workspace.createImageIcon("resources/ok.png"));
        saveButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                frame.setVisible(false);
            }
        });

        JButton cancelButton = new JButton(Language.translate("Cancel"), Workspace.createImageIcon("resources/edit-delete.png"));
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                panel.cancel();
                frame.setVisible(false);
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(saveButton);
        buttons.add(cancelButton);

        frame.add(buttons, BorderLayout.SOUTH);

        frame.getRootPane().setDefaultButton(saveButton);

        frame.pack();
        frame.setLocationRelativeTo(SketchletEditor.editorFrame);
        frame.setVisible(true);
    }

    public void refresh() {
        SketchletEditor.getInstance().repaintEverything();
    }
}
