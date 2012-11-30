package net.sf.sketchlet.designer.editor.ui.region;

import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.util.Colors;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FontChooserPanel extends JPanel {
    private static final Logger log = Logger.getLogger(FontChooserPanel.class);
    // all variables, aside from the final public Font are simply for the UI of the class

    private JLabel preview;
    public Font selectedFont;
    ActiveRegionPanel parent;
    JComboBox fontSizeCombo = new JComboBox();
    JComboBox fontListCombo = new JComboBox();
    JComboBox fontColorCombo = new JComboBox();
    JComboBox styleCombo = new JComboBox();

    public FontChooserPanel(ActiveRegionPanel parent) {
        super();
        this.parent = parent;
        initialiseComponent();
    }

    private void initialiseComponent() {
        this.preview = new JLabel();

        fontSizeCombo.setEditable(true);
        fontListCombo.setEditable(true);
        fontColorCombo.setEditable(true);
        styleCombo.setEditable(true);

        this.preview.setText("ABC abc 123");
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        fontListCombo.addItem("");
        fontListCombo.addItem("----------");
        for (int i = 0; i < fonts.length; i++) {
            fontListCombo.addItem(fonts[i]);
        }
        fontColorCombo.addItem("");
        Colors.addColorNamesToCombo(fontColorCombo);
        fontSizeCombo.addItem("");
        fontSizeCombo.addItem("----------");
        for (int i = 8; i <= 72; i += 2) {
            fontSizeCombo.addItem(i + "");
        }

        String[] styles = {"Regular", "Italic", "Bold", "Bold Italic"};
        styleCombo.addItem("");
        styleCombo.addItem("----------");
        for (int i = 0; i < styles.length; i++) {
            styleCombo.addItem(styles[i]);
        }
        fontListCombo.addItem("----------");
        fontSizeCombo.addItem("----------");
        fontColorCombo.addItem("----------");
        styleCombo.addItem("----------");
        for (String strVar : VariablesBlackboard.getInstance().getVariablesList()) {
            fontListCombo.addItem("=" + strVar);
            fontSizeCombo.addItem("=" + strVar);
            fontColorCombo.addItem("=" + strVar);
            styleCombo.addItem("=" + strVar);
        }

        fontListCombo.setSelectedItem(parent.getRegion().fontName);
        fontSizeCombo.setSelectedItem(parent.getRegion().fontSize);
        fontColorCombo.setSelectedItem(parent.getRegion().fontColor);
        styleCombo.setSelectedItem(parent.getRegion().fontStyle);

        fontSizeCombo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                fontChanged();
            }
        });
        fontListCombo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                fontChanged();
            }
        });
        fontColorCombo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                fontChanged();
            }
        });

        styleCombo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                fontChanged();
            }
        });

        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        fontListCombo.setPreferredSize(new Dimension(100, 25));
        fontSizeCombo.setPreferredSize(new Dimension(90, 25));
        fontColorCombo.setPreferredSize(new Dimension(90, 25));
        styleCombo.setPreferredSize(new Dimension(90, 25));
        add(fontListCombo);
        add(styleCombo);
        JToolBar tbBoldItalic = new JToolBar();
        tbBoldItalic.setFloatable(false);
        JButton bold = new JButton(Workspace.createImageIcon("resources/bold.gif"));
        bold.setToolTipText("Align Left");
        bold.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                String strStyle = styleCombo.getSelectedItem().toString();

                if (strStyle.equalsIgnoreCase("bold")) {
                    styleCombo.setSelectedItem("");
                } else if (strStyle.equalsIgnoreCase("italic")) {
                    styleCombo.setSelectedItem("Bold Italic");
                } else if (strStyle.equalsIgnoreCase("bold italic")) {
                    styleCombo.setSelectedItem("Italic");
                } else {
                    styleCombo.setSelectedItem("Bold");
                }
                SketchletEditor.getInstance().repaintEverything();
            }
        });
        JButton italic = new JButton(Workspace.createImageIcon("resources/italic.gif"));
        italic.setToolTipText("Align Left");
        italic.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                String strStyle = styleCombo.getSelectedItem().toString();

                if (strStyle.equalsIgnoreCase("italic")) {
                    styleCombo.setSelectedItem("");
                } else if (strStyle.equalsIgnoreCase("bold")) {
                    styleCombo.setSelectedItem("Bold Italic");
                } else if (strStyle.equalsIgnoreCase("bold italic")) {
                    styleCombo.setSelectedItem("Bold");
                } else {
                    styleCombo.setSelectedItem("Italic");
                }
                SketchletEditor.getInstance().repaintEverything();
            }
        });
        tbBoldItalic.add(bold);
        tbBoldItalic.add(italic);
        add(tbBoldItalic);
        add(fontSizeCombo);
        add(fontColorCombo);

        this.setLocation(new Point(100, 100));
        this.setSize(new Dimension(360, 242));
    }

    private void fontChanged() {
        try {
            String name = fontListCombo.getSelectedItem().toString();
            String strSize = fontSizeCombo.getSelectedItem().toString();
            String strColor = fontColorCombo.getSelectedItem().toString();
            String strStyle = styleCombo.getSelectedItem().toString();

            parent.getRegion().fontName = name;
            parent.getRegion().fontSize = strSize;
            parent.getRegion().fontStyle = strStyle;
            parent.getRegion().fontColor = strColor;

            SketchletEditor.getInstance().repaint();

        } catch (Exception ex) {
            log.error(ex);
        }
    }
}
