/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui.region;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.data.Pages;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.regions.ActiveRegionImageEditor;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.programming.screenscripts.AWTRobotUtil;
import net.sf.sketchlet.designer.programming.screenscripts.TextTransfer;
import net.sf.sketchlet.designer.programming.timers.curves.Curves;
import net.sf.sketchlet.designer.tools.log.ActivityLog;
import net.sf.sketchlet.designer.ui.UIUtils;
import net.sf.sketchlet.designer.ui.desktop.Notepad;
import net.sf.sketchlet.designer.ui.eventpanels.MouseEventsPanel;
import net.sf.sketchlet.designer.ui.macros.ImageAreaSelect;
import net.sf.sketchlet.designer.ui.eventpanels.KeyboardEventsPanel;
import net.sf.sketchlet.designer.ui.profiles.Profiles;
import net.sf.sketchlet.designer.ui.properties.PropertiesSetPanel;
import net.sf.sketchlet.util.RefreshTime;
import net.sf.sketchlet.util.SpringUtilities;
import net.sf.sketchlet.util.ui.DataRowFrame;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

public class ActiveRegionPanel extends JPanel {

    String[] columnInteractionNames = {Language.translate("Region"), Language.translate("Event"), Language.translate("Action"), Language.translate("Param1"), Language.translate("Param2")};
    String[] columnNamesAnimation = {Language.translate("Dimension"), Language.translate("Animation Type"), Language.translate("Start Value"), Language.translate("End Value"), Language.translate("Cycle Duration"), Language.translate("Curve")};
    String[] columnNamesUpdateTransformations = {Language.translate("Dimension"), Language.translate("Variable"), Language.translate("Start value"), Language.translate("End value"), Language.translate("Format")};
    String[] columnLimits = {Language.translate("Dimension"), Language.translate("Min"), Language.translate("Max")};
    ShapePanel shapePanel;
    PropertiesSetPanel setPanel;
    public AbstractTableModel modelAnimation;
    public AbstractTableModel modelUpdateTransformations;
    public JTable tableAnimation;
    public JScrollPane scrollAnimation;
    public JTable tableLimits;
    public AbstractTableModel modelLimits;
    public JScrollPane scrollLimits;
    JComboBox interactionEvent;
    public JTable tableUpdateTransformations;
    public JScrollPane scrollUpdateTransformations;
    public ActiveRegion region;
    Pages pages;
    public JTabbedPane tabs = new JTabbedPane();
    public JTabbedPane tabsImage = new JTabbedPane();
    public JTabbedPane tabsTransform = new JTabbedPane();
    public JTabbedPane tabsMove = new JTabbedPane();
    public JTabbedPane tabsRegionEvents = new JTabbedPane();
    public ActiveRegionImageEditor imageEditor;
    private WidgetPanel widgetPanel;
    private GeneralSettingsPanel generalSettingsPanel;
    JButton backwards = new JButton(Workspace.createImageIcon("resources/go-down.png"));
    JButton upwards = new JButton(Workspace.createImageIcon("resources/go-up.png"));
    JButton delete = new JButton(Workspace.createImageIcon("resources/user-trash.png"));
    JTextField statusBar = new JTextField(25);
    JComboBox paramComboBoxInteraction = new JComboBox();
    public static JFileChooser fileChoser;
    int eventRow = -1;
    int mappingRow = -1;
    static JComboBox regionComboBox = new JComboBox();
    JButton deleteMapping = new JButton(Language.translate("Delete"));
    JButton moveUpMapping = new JButton(Language.translate("Move Up"));
    JButton moveDownMapping = new JButton(Language.translate("Move Down"));
    JButton duplicateMapping = new JButton(Language.translate("Duplicate"));
    JButton editMapping = new JButton(Language.translate("Edit"));
    JComboBox comboBoxDim = new JComboBox();
    double _start = 0.0;
    double _end = 0.0;
    double _init = 0.0;
    boolean bCanUpdate = true;
    public static ActiveRegionPanel currentActiveRegionPanel = null;
    public static int indexGraphics = 0;
    public static int indexWidget = 1;
    public static int indexTransform = 2;
    public static int indexEvents = 3;
    public static int indexMouseEvents = 0;
    public static int indexKeyboardEvents = 0;
    public static int indexMotion = 1;
    public static int indexOverlap = 2;
    public static int indexGeneral = 4;
    public JComboBox imageUrlField = new JComboBox();
    public JComboBox imageIndex = new JComboBox();
    public JComboBox animationMs = new JComboBox();
    public JComboBox captureScreenX = new JComboBox();
    public JComboBox captureScreenY = new JComboBox();
    public JComboBox captureScreenWidth = new JComboBox();
    public JComboBox captureScreenHeight = new JComboBox();
    public RSyntaxTextArea textArea = Notepad.getInstance(RSyntaxTextArea.SYNTAX_STYLE_NONE);
    public JTextArea trajectory1 = new JTextArea();
    public JTextArea trajectory2 = new JTextArea();
    public JTextField charactersPerLine = new JTextField(5);
    public JTextField maxNumLines = new JTextField(5);
    public JCheckBox canMove = new JCheckBox(Language.translate("Enable moving by mouse"), true);
    public JCheckBox canRotate = new JCheckBox(Language.translate("Enable rotating by mouse"), true);
    public JCheckBox canResize = new JCheckBox(Language.translate("Enable resizing"));
    public JCheckBox wrapText = new JCheckBox(Language.translate("Wrap text"), false);
    public JCheckBox trimText = new JCheckBox(Language.translate("Trim"), false);
    public JCheckBox walkThrough = new JCheckBox(Language.translate("Make region solid (disable walk through)"));
    public JCheckBox stickToTrajectory = new JCheckBox(Language.translate("Stick to Trajectory"), true);
    public JCheckBox orientationTrajectory = new JCheckBox(Language.translate("Control Orientation"), true);
    public JCheckBox captureScreen = new JCheckBox(Language.translate("Capture Part of the Screen"), false);
    public JCheckBox captureScreenMouseMap = new JCheckBox(Language.translate("Map Mouse Clicks to Screen"), false);

    public KeyboardEventsPanel keyboardEventsPanel;
    public MouseEventsPanel mouseEventPanel;
    public RegionOverlapEventsPanel regionOverlapEventsPanel;

    public ActiveRegionPanel() {
        currentActiveRegionPanel = this;
    }

    public static JFileChooser getFileChooser() {
        if (ActiveRegionPanel.fileChoser == null) {
            ActiveRegionPanel.fileChoser = new JFileChooser();
            ActiveRegionPanel.fileChoser.setCurrentDirectory(new File(SketchletContextUtils.getSketchletDesignerTemplateFilesDir()));
        }
        return ActiveRegionPanel.fileChoser;
    }

    public ActiveRegionPanel(Pages pages, ActiveRegion _action, int tabIndex) {
        currentActiveRegionPanel = this;
        this.pages = pages;
        this.region = _action;
        this.populateComboBoxes();
        modelUpdateTransformations = new AbstractTableModel() {

            public String getColumnName(int col) {
                return columnNamesUpdateTransformations[col].toString();
            }

            public int getRowCount() {
                return region.updateTransformations.length;
            }

            public int getColumnCount() {
                return columnNamesUpdateTransformations.length;
            }

            public Object getValueAt(int row, int col) {
                return region.updateTransformations[row][col];
            }

            public boolean isCellEditable(int row, int col) {
                return true;
            }

            public void setValueAt(Object value, int row, int col) {
                if (!getValueAt(row, col).toString().equals(value.toString())) {
                    SketchletEditor.editorPanel.saveRegionUndo();
                }
                region.updateTransformations[row][col] = value;
                fireTableCellUpdated(row, col);
                ActivityLog.log("setRegionContinousMouseEvent", row + " " + col + " " + region.updateTransformations[row][0] + " " + " " + region.updateTransformations[row][1] + " " + " " + region.updateTransformations[row][2]);
                TutorialPanel.addLine("cmd", "Set a motion mapping " + region.updateTransformations[row][0], "details.gif", scrollUpdateTransformations);
            }

            public Class getColumnClass(int c) {
                return String.class;
            }
        };

        modelAnimation = new AbstractTableModel() {

            public String getColumnName(int col) {
                return columnNamesAnimation[col].toString();
            }

            public int getRowCount() {
                return region.propertiesAnimation.length;
            }

            public int getColumnCount() {
                return columnNamesAnimation.length;
            }

            public Object getValueAt(int row, int col) {
                return region.propertiesAnimation[row][col] == null ? "" : region.propertiesAnimation[row][col];
            }

            public boolean isCellEditable(int row, int col) {
                return col > 0 && region.propertiesAnimation[row][1] != null;
            }

            public void setValueAt(Object value, int row, int col) {
                if (!getValueAt(row, col).toString().equals(value.toString())) {
                    SketchletEditor.editorPanel.saveRegionUndo();
                }
                region.propertiesAnimation[row][col] = value.toString();
                if (col == 1) {
                    if (value == null || value.toString().isEmpty()) {
                        region.propertiesAnimation[row][2] = "";
                        region.propertiesAnimation[row][3] = "";
                        region.propertiesAnimation[row][4] = "";
                    } else {
                        String strProperty = region.propertiesAnimation[row][0];
                        String start = region.getMinValue(strProperty);
                        String end = region.getMaxValue(strProperty);

                        if (region.propertiesAnimation[row][2].isEmpty() && start != null) {
                            region.propertiesAnimation[row][2] = start;
                        }
                        if (region.propertiesAnimation[row][3].isEmpty() && end != null) {
                            region.propertiesAnimation[row][3] = end;
                        }
                        if (region.propertiesAnimation[row][4].isEmpty()) {
                            region.propertiesAnimation[row][4] = "1.0";
                        }
                    }

                    ActivityLog.log("setRegionPropertiesAnimation", row + " " + col + " " + value);
                    TutorialPanel.addLine("cmd", "Set the animation property " + region.propertiesAnimation[row][0], "details.gif", scrollAnimation);
                    this.fireTableRowsUpdated(row, row);
                }
            }

            public Class getColumnClass(int c) {
                return String.class;
            }
        };
        tableAnimation = new JTable(modelAnimation);
        TableColumn col = tableAnimation.getColumnModel().getColumn(2);

        tableAnimation.setDefaultRenderer(String.class, new PropertiesTableRenderer(region.propertiesAnimation));

        col = tableAnimation.getColumnModel().getColumn(1);
        JComboBox animationType = new JComboBox();
        animationType.setEditable(true);
        animationType.addItem("");
        animationType.addItem("Loop Forever");
        animationType.addItem("Loop Once");
        animationType.addItem("Puls Forever");
        animationType.addItem("Puls Once");
        col.setCellEditor(new DefaultCellEditor(animationType));

        JComboBox animationTime = new JComboBox();
        animationTime.setEditable(true);
        animationTime.addItem("");
        animationTime.addItem("1");
        animationTime.addItem("2");
        animationTime.addItem("3");
        animationTime.addItem("4");
        animationTime.addItem("5");
        animationTime.addItem("6");
        animationTime.addItem("7");
        animationTime.addItem("8");
        animationTime.addItem("9");
        animationTime.addItem("10");
        col = tableAnimation.getColumnModel().getColumn(4);
        col.setCellEditor(new DefaultCellEditor(animationTime));

        tableAnimation.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        tableAnimation.setFillsViewportHeight(true);

        tableUpdateTransformations = new JTable(modelUpdateTransformations);
        tableUpdateTransformations.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent ke) {
                if ((ke.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
                    if (ke.getKeyCode() == ke.VK_V) {
                        int n = pasteInTable(tableUpdateTransformations, modelUpdateTransformations, region.updateTransformations);
                        ActivityLog.log("pasteInTable", "regionContinousEvents " + n);
                    }
                }
            }
        });
        tableUpdateTransformations.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        modelLimits = new AbstractTableModel() {

            public String getColumnName(int col) {
                return columnLimits[col].toString();
            }

            public int getRowCount() {
                return region.limits.length;
            }

            public int getColumnCount() {
                return columnLimits.length;
            }

            public Object getValueAt(int row, int col) {
                if (row >= 0) {
                    return region.limits[row][col];
                } else {
                    return "";
                }
            }

            public boolean isCellEditable(int row, int col) {
                return col > 0;
            }

            public void setValueAt(Object value, int row, int col) {
                if (!getValueAt(row, col).toString().equals(value.toString())) {
                    SketchletEditor.editorPanel.saveRegionUndo();
                }
                region.limits[row][col] = value;
                fireTableCellUpdated(row, col);
                ActivityLog.log("setRegionLimits", row + " " + col + " " + region.limits[row][0] + " " + " " + region.limits[row][1] + " " + " " + region.limits[row][2]);
                TutorialPanel.addLine("cmd", "Set region position limits", "details.gif", scrollLimits);
            }

            public Class getColumnClass(int c) {
                return String.class;
            }
        };

        tableLimits = new JTable(modelLimits);
        tableLimits.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        TableColumn dimensionColumn = tableUpdateTransformations.getColumnModel().getColumn(0);

        comboBoxDim.setEditable(true);
        comboBoxDim.addItem("position x");
        comboBoxDim.addItem("position y");
        comboBoxDim.addItem("rotation");
        comboBoxDim.addItem("speed");
        comboBoxDim.addItem("trajectory position");
        comboBoxDim.addItem("trajectory position 2");
        dimensionColumn.setCellEditor(new DefaultCellEditor(comboBoxDim));

        JPanel mappingControls = new JPanel();
        UIUtils.makeSmall(new JComponent[]{deleteMapping, moveUpMapping, moveDownMapping, duplicateMapping, editMapping});
        mappingControls.add(deleteMapping);
        mappingControls.add(moveUpMapping);
        mappingControls.add(moveDownMapping);
        mappingControls.add(duplicateMapping);
        mappingControls.add(editMapping);


        tableUpdateTransformations.setFillsViewportHeight(true);
        tableUpdateTransformations.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent event) {
                if (event.getValueIsAdjusting()) {
                    return;
                }
                mappingRow = tableUpdateTransformations.getSelectedRow();
                enableControls();
            }
        });

        setDropHandlers();

        statusBar.setEditable(false);

        setFormatCombo();
        setVariablesCombo();
        setVariablesComboWithEquals();

        setLayout(new BorderLayout());

        JPanel showImagePanel = new JPanel();
        JPanel showImagePanelControl = new JPanel();
        showImagePanel.setLayout(new BorderLayout());


        JPanel movablePanel = new JPanel(new BorderLayout());

        JPanel showTextPanel = new JPanel();
        showTextPanel.setLayout(new BorderLayout());
        JPanel singleLineTextPanel = new JPanel();
        JPanel fileTextPanel = new JPanel();
        singleLineTextPanel.add(new JLabel(Language.translate("Font")));

        singleLineTextPanel.add(new FontChooserPanel(this));

        JToolBar tbAlign = new JToolBar();
        tbAlign.setFloatable(false);

        JButton alignLeft = new JButton(Workspace.createImageIcon("resources/left.gif"));
        alignLeft.setToolTipText(Language.translate("Align Left"));
        alignLeft.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setHorizontalAlignment("left");
                RefreshTime.update();
                SketchletEditor.editorPanel.repaint();
            }
        });
        JButton alignCenter = new JButton(Workspace.createImageIcon("resources/center.gif"));
        alignCenter.setToolTipText(Language.translate("Align Center"));
        alignCenter.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setHorizontalAlignment("center");
                RefreshTime.update();
                SketchletEditor.editorPanel.repaint();
            }
        });
        JButton alignRight = new JButton(Workspace.createImageIcon("resources/right.gif"));
        alignRight.setToolTipText(Language.translate("Align Right"));
        alignRight.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setHorizontalAlignment("right");
                RefreshTime.update();
                SketchletEditor.editorPanel.repaint();
            }
        });
        JButton alignTop = new JButton(Workspace.createImageIcon("resources/align-top.png"));
        alignTop.setToolTipText(Language.translate("Align Top"));
        alignTop.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setVerticalAlignment("top");
                RefreshTime.update();
                SketchletEditor.editorPanel.repaint();
            }
        });
        JButton alignMiddle = new JButton(Workspace.createImageIcon("resources/align-centered.png"));
        alignMiddle.setToolTipText(Language.translate("Align Middle"));
        alignMiddle.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setVerticalAlignment("center");
                RefreshTime.update();
                SketchletEditor.editorPanel.repaint();
            }
        });
        JButton alignBottom = new JButton(Workspace.createImageIcon("resources/align-bottom.png"));
        alignBottom.setToolTipText(Language.translate("Align Bottom"));
        alignBottom.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.setVerticalAlignment("bottom");
                RefreshTime.update();
                SketchletEditor.editorPanel.repaint();
            }
        });

        tbAlign.add(alignLeft);
        tbAlign.add(alignCenter);
        tbAlign.add(alignRight);
        tbAlign.add(alignTop);
        tbAlign.add(alignMiddle);
        tbAlign.add(alignBottom);

        singleLineTextPanel.add(tbAlign);

        fileTextPanel.add(new JLabel(Language.translate("Text file")));
        showTextPanel.add(singleLineTextPanel, BorderLayout.NORTH);

        TutorialPanel.prepare(this.textArea);
        showTextPanel.add(Notepad.getEditorPanel(textArea, false), BorderLayout.CENTER);

        this.textArea.setText(region.strText);
        this.textArea.addKeyListener(new KeyAdapter() {

            public void keyReleased(KeyEvent e) {
                if (!region.strText.equals(textArea.getText())) {
                    SketchletEditor.editorPanel.saveRegionUndo();
                    region.strText = textArea.getText();
                    RefreshTime.update();
                    SketchletEditor.editorPanel.repaint();
                }
            }
        });
        this.trajectory1.setText(region.strTrajectory1);
        this.trajectory1.addKeyListener(new KeyAdapter() {

            public void keyReleased(KeyEvent e) {
                if (!region.strTrajectory1.equals(trajectory1.getText())) {
                    SketchletEditor.editorPanel.saveRegionUndo();
                    region.strTrajectory1 = trajectory1.getText();
                    RefreshTime.update();
                    SketchletEditor.editorPanel.repaint();
                }
            }
        });
        this.trajectory2.setText(region.strTrajectory1);
        this.trajectory2.addKeyListener(new KeyAdapter() {

            public void keyReleased(KeyEvent e) {
                if (!region.strTrajectory2.equals(trajectory2.getText())) {
                    SketchletEditor.editorPanel.saveRegionUndo();
                    region.strTrajectory2 = trajectory2.getText();
                    RefreshTime.update();
                    SketchletEditor.editorPanel.repaint();
                }
            }
        });

        this.charactersPerLine.setText(region.strCharactersPerLine);
        this.charactersPerLine.addKeyListener(new KeyAdapter() {

            public void keyReleased(KeyEvent e) {
                if (!region.strCharactersPerLine.equals(charactersPerLine.getText())) {
                    SketchletEditor.editorPanel.saveRegionUndo();
                    region.strCharactersPerLine = charactersPerLine.getText();
                    RefreshTime.update();
                    SketchletEditor.editorPanel.repaint();
                }
            }
        });
        this.maxNumLines.setText(region.strMaxNumLines);
        this.maxNumLines.addKeyListener(new KeyAdapter() {

            public void keyReleased(KeyEvent e) {
                if (!region.strMaxNumLines.equals(maxNumLines.getText())) {
                    SketchletEditor.editorPanel.saveRegionUndo();
                    region.strMaxNumLines = maxNumLines.getText();
                    RefreshTime.update();
                    SketchletEditor.editorPanel.repaint();
                }
            }
        });

        JPanel southPanel = new JPanel();
        JPanel textSettings = new JPanel();
        textSettings.add(this.trimText);
        textSettings.add(this.wrapText);
        textSettings.add(this.charactersPerLine);

        TutorialPanel.prepare(this.charactersPerLine);

        textSettings.add(new JLabel(Language.translate(" characters per line ")));
        textSettings.add(new JLabel(Language.translate(" Show at most ")));
        textSettings.add(this.maxNumLines);
        TutorialPanel.prepare(this.maxNumLines);
        textSettings.add(new JLabel(Language.translate(" lines")));
        southPanel.setLayout(new BorderLayout());
        southPanel.add(textSettings, BorderLayout.SOUTH);
        showTextPanel.add(southPanel, BorderLayout.SOUTH);

        final JPanel urlPanel = new JPanel();
        urlPanel.add(new JLabel(Language.translate("URL/path:")));
        urlPanel.add(this.imageUrlField);
        JButton selectFile = new JButton("...");

        selectFile.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChoser.showOpenDialog(urlPanel);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    imageUrlField.setSelectedItem(fileChoser.getSelectedFile().getPath());
                    RefreshTime.update();
                    SketchletEditor.editorPanel.repaint();
                }
            }
        });

        urlPanel.add(selectFile);

        JPanel mappingLeftPane = new JPanel(new BorderLayout());

        JPanel checkPanel = new JPanel();
        checkPanel.setBorder(BorderFactory.createTitledBorder(Language.translate("Enable motion")));

        checkPanel.add(this.canMove);
        checkPanel.add(this.canRotate);
        scrollLimits = new JScrollPane(tableLimits);
        scrollUpdateTransformations = new JScrollPane(tableUpdateTransformations);
        scrollUpdateTransformations.setBorder(BorderFactory.createTitledBorder(Language.translate("Mapping motion to variable updates")));

        mappingLeftPane.add(checkPanel, BorderLayout.NORTH);

        JPanel panelVariablesMapping = new JPanel(new BorderLayout());
        panelVariablesMapping.add(mappingControls, BorderLayout.SOUTH);
        panelVariablesMapping.add(scrollUpdateTransformations, BorderLayout.CENTER);

        JButton clearImage = new JButton(Language.translate("clear"));
        JButton fromClipboard = new JButton(Language.translate("paste"));
        JButton fromURL = new JButton(Language.translate("from url"));
        JButton fromFile = new JButton(Language.translate("from file"));

        showImagePanelControl.add(clearImage);
        showImagePanelControl.add(fromClipboard);
        showImagePanelControl.add(fromURL);
        showImagePanelControl.add(fromFile);

        showImagePanel.add(getDrawingPanel(region), BorderLayout.CENTER);

        JButton buttonDefineCaptureArea = new JButton(Language.translate("Define capturing area"));
        buttonDefineCaptureArea.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Dimension dimScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
                Rectangle rectScreenSize = new Rectangle(dimScreenSize);
                BufferedImage captureedImage = AWTRobotUtil.robot.createScreenCapture(rectScreenSize);
                int x = 0;
                int y = 0;
                int w = 100;
                int h = 100;
                try {
                    x = Integer.parseInt(region.strCaptureScreenX);
                    y = Integer.parseInt(region.strCaptureScreenY);
                    w = Integer.parseInt(region.strCaptureScreenWidth);
                    h = Integer.parseInt(region.strCaptureScreenHeight);
                } catch (Throwable eNum) {
                }
                ImageAreaSelect.createAndShowGUI(ActiveRegionsFrame.reagionsAndActions, captureedImage, x, y, w, h, false);
                if (ImageAreaSelect.bSaved) {
                    captureScreen.setSelected(true);
                    String params[] = ImageAreaSelect.strArea.split(" ");
                    if (params.length >= 4) {
                        captureScreenX.setSelectedItem(params[0]);
                        captureScreenY.setSelectedItem(params[1]);
                        captureScreenWidth.setSelectedItem(params[2]);
                        captureScreenHeight.setSelectedItem(params[3]);
                    }
                }
                RefreshTime.update();
                SketchletEditor.editorPanel.repaint();
            }
        });

        canMove.setSelected(region.bCanMove);
        canRotate.setSelected(region.bCanRotate);
        canResize.setSelected(region.bCanResize);
        wrapText.setSelected(region.bWrapText);
        trimText.setSelected(region.bTrimText);
        walkThrough.setSelected(region.bWalkThrough);
        stickToTrajectory.setSelected(region.bStickToTrajectory);
        orientationTrajectory.setSelected(region.bOrientationTrajectory);
        captureScreen.setSelected(region.bCaptureScreen);
        captureScreenMouseMap.setSelected(region.bCaptureScreenMouseMap);

        canMove.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                region.bCanMove = canMove.isSelected();
            }
        });
        canRotate.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                SketchletEditor.editorPanel.saveRegionUndo();
                region.bCanRotate = canRotate.isSelected();
            }
        });
        canResize.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                SketchletEditor.editorPanel.saveRegionUndo();
                region.bCanResize = canResize.isSelected();
            }
        });
        wrapText.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                SketchletEditor.editorPanel.saveRegionUndo();
                region.bWrapText = wrapText.isSelected();
            }
        });
        trimText.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                SketchletEditor.editorPanel.saveRegionUndo();
                region.bTrimText = trimText.isSelected();
            }
        });
        walkThrough.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                SketchletEditor.editorPanel.saveRegionUndo();
                region.bWalkThrough = walkThrough.isSelected();
            }
        });
        stickToTrajectory.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                SketchletEditor.editorPanel.saveRegionUndo();
                region.bStickToTrajectory = stickToTrajectory.isSelected();
            }
        });
        orientationTrajectory.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                SketchletEditor.editorPanel.saveRegionUndo();
                region.bOrientationTrajectory = orientationTrajectory.isSelected();
            }
        });
        captureScreen.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                SketchletEditor.editorPanel.saveRegionUndo();
                region.bCaptureScreen = captureScreen.isSelected();
            }
        });
        captureScreenMouseMap.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                SketchletEditor.editorPanel.saveRegionUndo();
                region.bCaptureScreenMouseMap = captureScreenMouseMap.isSelected();
            }
        });


        this.imageUrlField.setSelectedItem(region.strImageUrlField);
        this.imageUrlField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (imageUrlField.getSelectedItem() != null) {
                    if (!region.strImageUrlField.equals(imageUrlField.getSelectedItem().toString())) {
                        SketchletEditor.editorPanel.saveRegionUndo();
                        region.strImageUrlField = (String) imageUrlField.getSelectedItem();
                        TutorialPanel.addLine("cmd", "Set the region image property: image url" + "=" + region.strImageUrlField, imageUrlField);
                        RefreshTime.update();
                        SketchletEditor.editorPanel.repaint();
                    }
                }
            }
        });
        UIUtils.populateVariablesCombo(this.imageUrlField, true);
        UIUtils.populateVariablesCombo(this.imageIndex, true);
        UIUtils.populateVariablesCombo(this.animationMs, true);
        UIUtils.populateVariablesCombo(this.captureScreenX, true);
        UIUtils.populateVariablesCombo(this.captureScreenY, true);
        UIUtils.populateVariablesCombo(this.captureScreenWidth, true);
        UIUtils.populateVariablesCombo(this.captureScreenHeight, true);

        UIUtils.removeActionListeners(this.imageIndex);
        this.imageIndex.setSelectedItem(region.strImageIndex);
        this.imageIndex.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (imageIndex.getSelectedItem() != null && !region.strImageIndex.equals(imageIndex.getSelectedItem().toString())) {
                    SketchletEditor.editorPanel.saveRegionUndo();
                    region.strImageIndex = (String) imageIndex.getSelectedItem();
                    TutorialPanel.addLine("cmd", "Set the region property: active frame" + "=" + region.strImageIndex, "details.gif", imageIndex);
                    RefreshTime.update();
                    SketchletEditor.editorPanel.repaint();
                }
            }
        });
        UIUtils.removeActionListeners(this.animationMs);
        this.animationMs.setSelectedItem(region.strAnimationMs);
        this.animationMs.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (animationMs.getSelectedItem() != null && !region.strAnimationMs.equals(animationMs.getSelectedItem().toString())) {
                    SketchletEditor.editorPanel.saveRegionUndo();
                    region.strAnimationMs = (String) animationMs.getSelectedItem();
                    TutorialPanel.addLine("cmd", "Set the region property: frame animation ms" + "=" + region.strImageIndex, "details.gif", animationMs);
                    RefreshTime.update();
                    SketchletEditor.editorPanel.repaint();
                }
            }
        });

        captureScreenX.setSelectedItem(region.strCaptureScreenX);
        this.captureScreenX.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (captureScreenX.getSelectedItem() != null && !region.strCaptureScreenX.equals(captureScreenX.getSelectedItem().toString())) {
                    SketchletEditor.editorPanel.saveRegionUndo();
                    region.strCaptureScreenX = (String) captureScreenX.getSelectedItem();
                    RefreshTime.update();
                    SketchletEditor.editorPanel.repaint();
                }
            }
        });

        captureScreenY.setSelectedItem(region.strCaptureScreenY);
        this.captureScreenY.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (captureScreenY.getSelectedItem() != null && !region.strCaptureScreenY.equals(captureScreenY.getSelectedItem().toString())) {
                    SketchletEditor.editorPanel.saveRegionUndo();
                    region.strCaptureScreenY = (String) captureScreenY.getSelectedItem();
                    RefreshTime.update();
                    SketchletEditor.editorPanel.repaint();
                }
            }
        });

        captureScreenWidth.setSelectedItem(region.strCaptureScreenWidth);
        this.captureScreenWidth.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (captureScreenWidth.getSelectedItem() != null && !region.strCaptureScreenWidth.equals(captureScreenWidth.getSelectedItem().toString())) {
                    SketchletEditor.editorPanel.saveRegionUndo();
                    region.strCaptureScreenWidth = (String) captureScreenWidth.getSelectedItem();
                    RefreshTime.update();
                    SketchletEditor.editorPanel.repaint();
                }
            }
        });

        captureScreenHeight.setSelectedItem(region.strCaptureScreenHeight);
        this.captureScreenHeight.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (captureScreenHeight.getSelectedItem() != null && !region.strCaptureScreenHeight.equals(captureScreenHeight.getSelectedItem().toString())) {
                    SketchletEditor.editorPanel.saveRegionUndo();
                    region.strCaptureScreenHeight = (String) captureScreenHeight.getSelectedItem();
                    RefreshTime.update();
                    SketchletEditor.editorPanel.repaint();
                }
            }
        });

        final JButton btnScreenWidthToRegion = new JButton(Language.translate("set region width = capture width"));
        final JButton btnRegionToScreenWidth = new JButton(Language.translate("set capture width = region width"));
        final JButton btnScreenHeightToRegion = new JButton(Language.translate("set region height = capture height"));
        final JButton btnRegionToScreenHeight = new JButton(Language.translate("set capture height = region height"));
        btnScreenWidthToRegion.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(btnScreenWidthToRegion);
        btnRegionToScreenWidth.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(btnRegionToScreenWidth);
        btnScreenHeightToRegion.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(btnScreenHeightToRegion);
        btnRegionToScreenHeight.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(btnRegionToScreenHeight);
        this.walkThrough.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(this.walkThrough);

        btnScreenWidthToRegion.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (region.strCaptureScreenWidth != null && !region.strCaptureScreenWidth.isEmpty()) {
                    try {
                        SketchletEditor.editorPanel.saveRegionUndo();
                        region.setWidth((int) Double.parseDouble(region.strCaptureScreenWidth));
                        RefreshTime.update();
                        SketchletEditor.editorPanel.repaint();
                    } catch (Throwable e) {
                    }
                }
            }
        });
        btnScreenHeightToRegion.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (region.strCaptureScreenHeight != null && !region.strCaptureScreenHeight.isEmpty()) {
                    try {
                        SketchletEditor.editorPanel.saveRegionUndo();
                        region.setHeight((int) Double.parseDouble(region.strCaptureScreenHeight));
                        RefreshTime.update();
                        SketchletEditor.editorPanel.repaint();
                    } catch (Throwable e) {
                    }
                }
            }
        });
        btnRegionToScreenWidth.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                captureScreenWidth.setSelectedItem("" + region.getWidth());
                RefreshTime.update();
                SketchletEditor.editorPanel.repaint();
            }
        });
        btnRegionToScreenHeight.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                captureScreenHeight.setSelectedItem("" + region.getHeight());
                RefreshTime.update();
                SketchletEditor.editorPanel.repaint();
            }
        });
        JPanel screenCapturePanel = new JPanel(new SpringLayout());
        screenCapturePanel.add(this.captureScreen);
        screenCapturePanel.add(buttonDefineCaptureArea);
        screenCapturePanel.add(new JLabel(""));
        screenCapturePanel.add(new JLabel(""));
        screenCapturePanel.add(new JLabel(""));
        screenCapturePanel.add(new JLabel(""));

        screenCapturePanel.add(new JLabel(Language.translate("X (left): "), JLabel.RIGHT));
        screenCapturePanel.add(this.captureScreenX);

        screenCapturePanel.add(new JLabel(Language.translate("Width: "), JLabel.RIGHT));
        screenCapturePanel.add(this.captureScreenWidth);
        screenCapturePanel.add(btnScreenWidthToRegion);
        screenCapturePanel.add(btnRegionToScreenWidth);

        screenCapturePanel.add(new JLabel(Language.translate("Y (top): "), JLabel.RIGHT));
        screenCapturePanel.add(this.captureScreenY);

        screenCapturePanel.add(new JLabel(Language.translate("Height: "), JLabel.RIGHT));
        screenCapturePanel.add(captureScreenHeight);
        screenCapturePanel.add(btnScreenHeightToRegion);
        screenCapturePanel.add(btnRegionToScreenHeight);

        screenCapturePanel.add(this.captureScreenMouseMap);
        screenCapturePanel.add(new JLabel(""));
        screenCapturePanel.add(new JLabel(""));
        screenCapturePanel.add(new JLabel(""));
        screenCapturePanel.add(new JLabel(""));
        screenCapturePanel.add(new JLabel(""));

        SpringUtilities.makeCompactGrid(screenCapturePanel,
                4, 6, //rows, cols
                5, 5, //initialX, initialY
                5, 5);//xPad, yPad    }

        deleteMapping.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                UIUtils.deleteTableRows(tableUpdateTransformations, modelUpdateTransformations, region.updateTransformations);
            }
        });

        editMapping.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = mappingRow;
                editUpdateTransformationsEvent(row);
            }
        });

        moveUpMapping.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (mappingRow > 0) {
                    SketchletEditor.editorPanel.saveRegionUndo();
                    Object[] rowData1 = region.updateTransformations[mappingRow];
                    Object[] rowData2 = region.updateTransformations[mappingRow - 1];

                    region.updateTransformations[mappingRow] = rowData2;
                    region.updateTransformations[mappingRow - 1] = rowData1;

                    int r = mappingRow - 1;

                    modelUpdateTransformations.fireTableDataChanged();

                    tableUpdateTransformations.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });
        moveDownMapping.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (mappingRow < region.updateTransformations.length - 1) {
                    SketchletEditor.editorPanel.saveRegionUndo();
                    Object[] rowData1 = region.updateTransformations[mappingRow];
                    Object[] rowData2 = region.updateTransformations[mappingRow + 1];

                    region.updateTransformations[mappingRow] = rowData2;
                    region.updateTransformations[mappingRow + 1] = rowData1;

                    int r = mappingRow + 1;

                    modelUpdateTransformations.fireTableDataChanged();

                    tableUpdateTransformations.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });
        duplicateMapping.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = mappingRow;
                if (row < region.updateTransformations.length - 1) {
                    SketchletEditor.editorPanel.saveRegionUndo();
                    Object[] rowData1 = region.updateTransformations[row];

                    for (int i = region.updateTransformations.length - 2; i >= row + 1; i--) {
                        region.updateTransformations[i + 1] = region.updateTransformations[i];
                    }

                    region.updateTransformations[row + 1] = new Object[]{
                            "" + region.updateTransformations[row][0],
                            "" + region.updateTransformations[row][1],
                            "" + region.updateTransformations[row][2],
                            "" + region.updateTransformations[row][3],
                            "" + region.updateTransformations[row][4]
                    };

                    int r = row + 1;

                    modelUpdateTransformations.fireTableDataChanged();
                    tableUpdateTransformations.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });

        enableControls();

        shapePanel = new ShapePanel(this.region);

        JPanel trajectPanel = new JPanel();
        trajectPanel.setLayout(new BorderLayout());
        JPanel ctrlTrPanel = new JPanel();
        ctrlTrPanel.setBorder(BorderFactory.createTitledBorder(Language.translate("Trajectory")));
        JButton clearTrajectory = new JButton(Language.translate("clear trajectory"));
        ctrlTrPanel.add(this.stickToTrajectory);
        ctrlTrPanel.add(this.orientationTrajectory);
        ctrlTrPanel.add(clearTrajectory);
        clearTrajectory.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.editorPanel.saveRegionUndo();
                trajectory1.setText("");
                trajectory2.setText("");
                RefreshTime.update();
                SketchletEditor.editorPanel.repaint();
            }
        });
        mappingLeftPane.add(ctrlTrPanel, BorderLayout.SOUTH);
        // trajectPanel.add(new JScrollPane(action.trajectory1));
        //mappingLeftPane.add(trajectPanel, BorderLayout.SOUTH);
        JPanel mappingGridPane = new JPanel(new BorderLayout());
        mappingGridPane.add(mappingLeftPane, BorderLayout.WEST);
        mappingGridPane.add(panelVariablesMapping);

        tabsMove.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(tabsMove);
        tabsMove.addTab(Language.translate("Settings"), mappingGridPane);
        JPanel panelTrajectoryPoints = new JPanel(new GridLayout(1, 2));
        TutorialPanel.prepare(trajectory1);
        TutorialPanel.prepare(trajectory2);

        JScrollPane tp1 = new JScrollPane(trajectory1);
        JScrollPane tp2 = new JScrollPane(trajectory2);
        tp1.setBorder(BorderFactory.createTitledBorder(Language.translate("Primary trajectory (x y time)")));
        tp2.setBorder(BorderFactory.createTitledBorder(Language.translate("Secondary trajectory (x y time)")));
        panelTrajectoryPoints.add(tp1);
        panelTrajectoryPoints.add(tp2);
        tabsMove.setTabPlacement(JTabbedPane.LEFT);
        tabsMove.add(Language.translate("Motion Limits"), scrollLimits);
        tabsMove.addTab(Language.translate("Trajectory Points"), panelTrajectoryPoints);
        movablePanel.add(tabsMove);

        final JButton defineClip = new JButton(Language.translate("Define Visible Area"), Workspace.createImageIcon("resources/clip.png"));
        defineClip.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.saveRegionUndo();
                SketchletEditor.editorPanel.defineClip();
                RefreshTime.update();
                SketchletEditor.editorPanel.repaint();
            }
        });
        JPanel panel = new JPanel();
        panel.add(defineClip);

        tabsImage.setFont(tabsImage.getFont().deriveFont(9.0f));
        tabsImage.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(tabsImage);

        tabsImage.add(showImagePanel, Language.translate("Image"));
        if (Profiles.isActive("active_region_shape")) {
            JPanel shapePanelWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
            shapePanelWrapper.add(shapePanel);
            tabsImage.add(shapePanelWrapper, Language.translate("Shape"));
        }
        if (Profiles.isActive("active_region_url")) {
            JPanel urlPanelWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
            urlPanelWrapper.add(urlPanel);
            tabsImage.add(urlPanel, Language.translate("URL/file path"));
        }
        if (Profiles.isActive("active_region_screen_capture")) {
            JPanel screenCapturePanelWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
            screenCapturePanelWrapper.add(screenCapturePanel);
            tabsImage.add(screenCapturePanelWrapper, Language.translate("Screen Capture"));
        }
        if (Profiles.isActive("active_region_text")) {
            tabsImage.add(showTextPanel, Language.translate("Text"));
        }

        tabs.setFont(tabsImage.getFont().deriveFont(9.0f));
        tabs.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(tabs);

        tabs.applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        tabs.setTabPlacement(JTabbedPane.LEFT);
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        tabs.add(tabsImage, Language.translate("Graphics "));
        indexGraphics = tabs.getTabCount() - 1;
        setPanel = new PropertiesSetPanel(region);
        setPanel.setSaveUndoAction(new Runnable() {

            public void run() {
                SketchletEditor.editorPanel.saveRegionUndo();
            }
        });

        JPanel panelAnimation = new JPanel(new BorderLayout());
        JPanel panelButtons = new JPanel(new BorderLayout());
        final JButton btnClear = new JButton(Language.translate("Clear"));
        btnClear.setEnabled(false);
        btnClear.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableAnimation.getSelectedRow();
                if (row >= 0 && region.propertiesAnimation[row][1] != null) {
                    region.propertiesAnimation[row][1] = "";
                    region.propertiesAnimation[row][2] = "";
                    region.propertiesAnimation[row][3] = "";
                    region.propertiesAnimation[row][4] = "";
                    region.propertiesAnimation[row][5] = "";
                    modelAnimation.fireTableDataChanged();
                }
            }
        });
        final JButton btnStart = new JButton(Language.translate("<- Start"));
        btnStart.setEnabled(false);
        btnStart.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableAnimation.getSelectedRow();
                if (row >= 0 && region.propertiesAnimation[row][1] != null) {
                    region.propertiesAnimation[row][2] = region.getProperty(region.propertiesAnimation[row][0]);
                    modelAnimation.fireTableRowsUpdated(row, row);
                }
            }
        });
        final JButton btnEnd = new JButton(Language.translate("<- End"));
        btnEnd.setEnabled(false);
        btnEnd.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableAnimation.getSelectedRow();
                if (row >= 0 && region.propertiesAnimation[row][1] != null) {
                    region.propertiesAnimation[row][3] = region.getProperty(region.propertiesAnimation[row][0]);
                    modelAnimation.fireTableRowsUpdated(row, row);
                }
            }
        });
        final JTextField startField = new JTextField(5);
        final JSlider slider = new JSlider(JSlider.VERTICAL, 0, 100, 0);
        final JTextField endField = new JTextField(5);
        startField.setHorizontalAlignment(JTextField.CENTER);
        endField.setHorizontalAlignment(JTextField.CENTER);

        TutorialPanel.prepare(startField);
        TutorialPanel.prepare(endField);

        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.add(endField, BorderLayout.NORTH);
        sliderPanel.add(slider, BorderLayout.CENTER);
        sliderPanel.add(startField, BorderLayout.SOUTH);
        sliderPanel.setBorder(BorderFactory.createTitledBorder("Explore"));

        endField.setEnabled(false);
        slider.setEnabled(false);
        startField.setEnabled(false);

        panelButtons.add(sliderPanel);

        JPanel panel3 = new JPanel(new GridLayout(3, 1));
        panel3.add(btnStart);
        panel3.add(btnEnd);
        panel3.add(btnClear);

        panelButtons.add(panel3, BorderLayout.SOUTH);

        TutorialPanel.prepare(panel3);

        JPanel panelButtonsWrapper = new JPanel(new BorderLayout());
        panelButtonsWrapper.add(panelButtons);

        panelAnimation.add(panelButtonsWrapper, BorderLayout.EAST);
        tableAnimation.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent event) {
                int row = tableAnimation.getSelectedRow();
                boolean bEnable = row >= 0 && region.propertiesAnimation[row][1] != null;
                btnClear.setEnabled(bEnable);
                btnStart.setEnabled(bEnable);
                btnEnd.setEnabled(bEnable);
                slider.setEnabled(bEnable);
                startField.setEnabled(bEnable);
                endField.setEnabled(bEnable);

                String start = null;
                String end = null;
                String init = "";
                if (bEnable) {
                    String strProperty = region.propertiesAnimation[row][0];
                    start = region.getMinValue(strProperty);
                    end = region.getMaxValue(strProperty);
                    init = region.getProperty(strProperty);
                    if (init == null || init.isEmpty()) {
                        String defaultValue = region.getDefaultValue(strProperty);
                        if (defaultValue != null && !defaultValue.isEmpty()) {
                            init = defaultValue;
                        }
                    }

                    if (start == null || end == null) {
                        bEnable = false;
                    }

                    try {
                        _start = Double.parseDouble(start);
                        _end = Double.parseDouble(end);

                        if (init.isEmpty()) {
                            _init = _start;
                        } else {
                            _init = Double.parseDouble(init);
                        }

                        bCanUpdate = false;
                        slider.setValue(slider.getMinimum() + (int) ((slider.getMaximum() - slider.getMinimum()) * (_init - Math.min(_start, _end)) / Math.abs(_end - _start)));
                        bCanUpdate = true;
                    } catch (Exception e) {
                        bEnable = false;
                    }
                }
                if (bEnable) {
                    startField.setText(start);
                    endField.setText(end);
                }
            }
        });
        tabsImage.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                int index = tabsImage.getSelectedIndex();
                if (index >= 0) {
                    ActivityLog.log("selectTabRegionImage", tabsImage.getTitleAt(index));
                }
            }
        });
        tabsRegionEvents.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                SketchletEditor.editorPanel.extraEditorPanel.openRelevantHelpPage(false);
            }
        });
        TutorialPanel.prepare(tabsImage);
        tabsTransform.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                int index = tabsTransform.getSelectedIndex();
                if (index >= 0) {
                    ActivityLog.log("selectTabRegionTransform", tabsTransform.getTitleAt(index));
                }
            }
        });
        TutorialPanel.prepare(tabsTransform);
        slider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (!bCanUpdate) {
                    return;
                }

                bCanUpdate = false;
                try {
                    int row = tableAnimation.getSelectedRow();
                    if (row >= 0) {
                        String start = startField.getText();
                        String end = endField.getText();
                        if (start != null && end != null) {
                            _start = Double.parseDouble(start);
                            _end = Double.parseDouble(end);
                            int fps = (int) slider.getValue();

                            region.setProperty(region.propertiesAnimation[row][0], "" + (Math.min(_start, _end) + Math.abs(_start - _end) * fps / (slider.getMaximum() - slider.getMinimum())));
                            SketchletEditor.editorPanel.repaint();
                        }
                    }
                } catch (Exception e2) {
                }

                bCanUpdate = true;
            }
        });

        TutorialPanel.prepare(slider);

        col = tableAnimation.getColumnModel().getColumn(5);
        col.setCellEditor(new DefaultCellEditor(Curves.globalCurves.getComboBox()));
        scrollAnimation = new JScrollPane(tableAnimation);
        panelAnimation.add(scrollAnimation);

        tabsTransform.add(Language.translate("Set Transformation"), setPanel);
        tabsTransform.add(Language.translate("Animate Transformation"), panelAnimation);
        tabsTransform.setFont(tabsImage.getFont().deriveFont(9.0f));
        tabsTransform.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(tabsTransform);


        if (Profiles.isActive("active_region_widget")) {
            widgetPanel = new WidgetPanel(this.region);
            tabs.add(widgetPanel, Language.translate("Widget "));
            indexWidget = tabs.getTabCount() - 1;
        }
        if (Profiles.isActive("active_region_transformations")) {
            tabs.add(tabsTransform, Language.translate("Transformations "));
            indexTransform = tabs.getTabCount() - 1;
        }

        tabs.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (!tabs.isShowing()) {
                    return;
                }
                int n = tabs.getSelectedIndex();
                if (n >= 0) {
                    String title = tabs.getTitleAt(n).trim().toLowerCase();

                    if (n == indexGraphics) {
                        SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("active_region_image");
                    } else if (n == indexTransform) {
                        SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("active_region_transform");
                    } else if (n == indexEvents) {
                        int n2 = tabsRegionEvents.getSelectedIndex();

                        if (n2 == indexMotion) {
                            SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("active_region_move");
                        } else if (n2 == indexMouseEvents) {
                            SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("active_region_mouse");
                        } else if (n2 == indexOverlap) {
                            SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("active_region_interaction");
                        }
                    } else if (n == indexWidget) {
                        SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("active_region_widget");
                    } else if (n == indexGeneral) {
                        SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("active_region_general");
                    }
                    ActivityLog.log("selectTabRegionSettings", title);
                }
            }
        });
        TutorialPanel.prepare(tabs);

        tabs.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        tabsRegionEvents.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(tabsRegionEvents);

        if (Profiles.isActiveAny("active_region_move,active_region_mouse,active_region_mouse,active_region_overlap")) {
            tabs.add(tabsRegionEvents, Language.translate("Region Events "));
            indexEvents = tabs.getTabCount() - 1;
        }
        if (Profiles.isActive("active_region_mouse")) {
            mouseEventPanel = new MouseEventsPanel(this.region.mouseProcessor);
            tabsRegionEvents.add(mouseEventPanel, Language.translate("Mouse Events "));
            indexMouseEvents = tabsRegionEvents.getTabCount() - 1;
        }
        if (Profiles.isActive("active_region_keyboard")) {
            keyboardEventsPanel = new KeyboardEventsPanel(this.region.keyboardProcessor);
            tabsRegionEvents.add(keyboardEventsPanel, Language.translate("Keyboard Events "));
            indexKeyboardEvents = tabsRegionEvents.getTabCount() - 1;
        }
        if (Profiles.isActive("active_region_move")) {
            tabsRegionEvents.add(movablePanel, Language.translate("Move & Rotate "));
            indexMotion = tabsRegionEvents.getTabCount() - 1;
        }
        if (Profiles.isActive("active_region_overlap")) {
            regionOverlapEventsPanel = new RegionOverlapEventsPanel(this.region);
            tabsRegionEvents.add(regionOverlapEventsPanel, Language.translate("Overlap & Touch "));
            indexOverlap = tabsRegionEvents.getTabCount() - 1;
        }

        if (Profiles.isActive("active_region_general")) {
            generalSettingsPanel = new GeneralSettingsPanel(region);
            tabs.add(generalSettingsPanel, Language.translate("General "));
            indexGeneral = tabs.getTabCount() - 1;
        }

        if (tabIndex >= 0 && tabIndex < tabs.getTabCount()) {
            tabs.setSelectedIndex(tabIndex);
        }

        this.add(tabs, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();

        controlPanel.add(statusBar);
        upwards.setToolTipText(Language.translate("Move region upwards"));
        backwards.setToolTipText(Language.translate("Move region backwards"));
        delete.setToolTipText(Language.translate("Delete region"));
        controlPanel.add(upwards);
        controlPanel.add(backwards);
        controlPanel.add(delete);

        upwards.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.editorPanel.moveCurrentActionUpwards();
                RefreshTime.update();
                SketchletEditor.editorPanel.repaint();
            }
        });

        backwards.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.editorPanel.moveCurrentActionBackwards();
                RefreshTime.update();
                SketchletEditor.editorPanel.repaint();
            }
        });

        delete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.editorPanel.deleteSelectedRegion();
                RefreshTime.update();
                SketchletEditor.editorPanel.repaint();
            }
        });

        widgetPanel.reloadWidgetEvents();
    }

    public void setDropHandlers() {
        new FileDrop(System.out, this.tableUpdateTransformations, new FileDrop.Listener() {

            public void filesDropped(Point p, java.io.File[] files) {
                if (files.length > 0) {
                }
            }

            public void dragOver(int x, int y) {
            }

            public void stringDropped(Point p, String strText) {
                DataRowFrame.emptyOnCancel = true;
                if (strText.startsWith("=")) {
                    ActiveRegionsFrame.showRegionsAndActions();
                    ActiveRegionPanel ap = ActiveRegionsFrame.refresh(region, ActiveRegionPanel.indexEvents, ActiveRegionPanel.indexMotion);
                    int row = ap.getFreeMappingRow();
                    region.updateTransformations[row][1] = strText.substring(1);
                    ap.editUpdateTransformationsEvent(row);
                }
                DataRowFrame.emptyOnCancel = false;
            }
        });
    }

    JComboBox comboBoxFormat = new JComboBox();

    public void setFormatCombo() {
        comboBoxFormat = new JComboBox();
        comboBoxFormat.setEditable(true);
        comboBoxFormat.addItem("");

        comboBoxFormat.addItem("0");
        comboBoxFormat.addItem("00");
        comboBoxFormat.addItem("000");
        comboBoxFormat.addItem("0.00");

        TableColumn formatColumn = this.tableUpdateTransformations.getColumnModel().getColumn(4);
        formatColumn.setCellEditor(new DefaultCellEditor(comboBoxFormat));
        formatColumn.setCellEditor(new DefaultCellEditor(comboBoxFormat));
    }

    JComboBox comboBoxVariables = new JComboBox();

    public void setVariablesCombo() {
        comboBoxVariables = new JComboBox();
        comboBoxVariables.setEditable(true);
        comboBoxVariables.addItem("");

        for (String strVar : DataServer.variablesServer.variablesVector) {
            comboBoxVariables.addItem(strVar);
        }

        tableUpdateTransformations.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(comboBoxVariables));
    }

    JComboBox comboBox = new JComboBox();

    public void setVariablesComboWithEquals() {
        comboBox.removeAllItems();
        comboBox.setEditable(true);
        comboBox.addItem("");

        for (String strVar : DataServer.variablesServer.variablesVector) {
            comboBox.addItem("=" + strVar);
        }

        tableUpdateTransformations.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(comboBox));
        tableUpdateTransformations.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(comboBox));
    }

    public static int pasteInTable(JTable table, AbstractTableModel model, Object data[][]) {
        SketchletEditor.editorPanel.saveRegionUndo();
        String strClipboard = TextTransfer.getClipboardContents();
        String lines[] = strClipboard.split("\n");

        int last = ActiveRegion.getLastNonEmptyRow(data);
        for (int i = last + 1, r = 0; i < data.length && r < lines.length; i++, r++) {
            String rowData[] = lines[r].split("\t");
            for (int j = 0; j < Math.min(rowData.length, data[i].length); j++) {
                model.setValueAt(rowData[j], i, j);
            }
        }
        model.fireTableDataChanged();

        return lines.length;
    }

    public JPanel getDrawingPanel(ActiveRegion a) {
        imageEditor = new ActiveRegionImageEditor(a, this);

        JPanel freeHandPanel = new JPanel();
        freeHandPanel.setLayout(new BorderLayout());

        JPanel colorPanelWrapper = new JPanel(new BorderLayout());

        JPanel frameGrid = new JPanel(new GridLayout(0, 2));
        frameGrid.setBorder(BorderFactory.createTitledBorder(Language.translate("flipbook")));
        JLabel label1 = new JLabel(Language.translate("  active frame: "));
        label1.setFont(label1.getFont().deriveFont(9.0f));
        frameGrid.add(label1);
        frameGrid.add(this.imageIndex);
        this.imageIndex.setPreferredSize(new Dimension(70, 25));
        JLabel label2 = new JLabel(Language.translate("  animation (ms): "));
        label2.setFont(label2.getFont().deriveFont(9.0f));
        frameGrid.add(label2);
        frameGrid.add(this.animationMs);

        colorPanelWrapper.add(frameGrid, BorderLayout.NORTH);
        colorPanelWrapper.add(imageEditor.getColorPanel(), BorderLayout.SOUTH);

        freeHandPanel.add(colorPanelWrapper, BorderLayout.EAST);

        loadDrawingTabs();

        freeHandPanel.add(imageEditor.drawingPanels, BorderLayout.CENTER);
        freeHandPanel.add(imageEditor.getControlPanel(), BorderLayout.WEST);

        imageEditor.enableControls();

        return freeHandPanel;
    }


    public void loadDrawingTabs() {
        imageEditor.scrollPane = null;
        imageEditor.drawingPanels.removeAll();
        JScrollPane scrollPane = new JScrollPane(imageEditor);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        imageEditor.drawingPanels.addTab("1", scrollPane);

        for (String strAdditionalFile : region.additionalImageFile) {
            int index = imageEditor.drawingPanels.getTabCount();
            imageEditor.drawingPanels.add("" + (index + 1), new JPanel());
        }

        imageEditor.scrollPane = scrollPane;
    }

    public void enableControls() {
        this.deleteMapping.setEnabled(mappingRow >= 0);
        editMapping.setEnabled(mappingRow >= 0);
        this.moveUpMapping.setEnabled(mappingRow > 0);
        this.moveDownMapping.setEnabled(mappingRow >= 0 && mappingRow < region.updateTransformations.length - 1);
        this.duplicateMapping.setEnabled(mappingRow >= 0 && eventRow < region.updateTransformations.length - 1);

        if (mouseEventPanel != null) {
            mouseEventPanel.enableControls();
        }

        if (regionOverlapEventsPanel != null) {
            regionOverlapEventsPanel.enableControls();
        }
    }

    public void refresh() {
        int index = region.parent.regions.indexOf(region);
        upwards.setEnabled(index > 0);
        backwards.setEnabled(index < region.parent.regions.size() - 1);

        statusBar.setText("position (left,up) : " + region.x1 + "," + region.y1 + "    size : " + (region.x2 - region.x1) + "," + (region.y2 - region.y1));

        RefreshTime.update();
        SketchletEditor.editorPanel.repaint();
        RefreshTime.update();
    }

    public static void refreshRegionComboBox(int n) {
        regionComboBox.setEditable(true);
        regionComboBox.removeAllItems();

        regionComboBox.addItem("");
        regionComboBox.addItem("Any region");

        for (int i = 0; i < n; i++) {
            regionComboBox.addItem("" + (i + 1));
        }
    }

    public int getFreeMappingRow() {
        int row = -1;
        for (int i = 0; i < this.modelUpdateTransformations.getRowCount(); i++) {
            String str = (String) this.modelUpdateTransformations.getValueAt(i, 1);
            if (str.isEmpty()) {
                row = i;
                break;
            }
        }

        return row;
    }

    public void editUpdateTransformationsEvent(int row) {
        if (row >= 0) {
            SketchletEditor.editorPanel.saveRegionUndo();
            Object editors[] = new Object[this.columnNamesUpdateTransformations.length];
            editors[0] = DataRowFrame.cloneComboBox(comboBoxDim);
            editors[1] = DataRowFrame.cloneComboBox(comboBoxVariables);
            editors[2] = DataRowFrame.cloneComboBox(comboBox);
            editors[3] = DataRowFrame.cloneComboBox(comboBox);
            editors[4] = DataRowFrame.cloneComboBox(comboBoxFormat);

            new DataRowFrame(SketchletEditor.editorFrame,
                    "Bidirectional Transformations",
                    row,
                    this.columnNamesUpdateTransformations,
                    editors, null, null,
                    tableUpdateTransformations,
                    modelUpdateTransformations);
        }
    }

    int rowVariables = -1;


    public void populateComboBoxes() {
        UIUtils.populateVariablesCombo(this.imageUrlField, true);
        UIUtils.populateVariablesCombo(this.imageIndex, true);
        UIUtils.populateVariablesCombo(this.animationMs, true);
        UIUtils.populateVariablesCombo(this.captureScreenX, true);
        UIUtils.populateVariablesCombo(this.captureScreenY, true);
        UIUtils.populateVariablesCombo(this.captureScreenWidth, true);
        UIUtils.populateVariablesCombo(this.captureScreenHeight, true);

        this.imageUrlField.setPreferredSize(new Dimension(280, 30));
        this.imageIndex.setPreferredSize(new Dimension(85, 18));
        this.animationMs.setPreferredSize(new Dimension(85, 18));

        if (generalSettingsPanel != null) {
            generalSettingsPanel.populateComboBoxes();
        }
    }

    public void refreshComponents() {
        UIUtils.refreshComboBox(this.imageUrlField, region.strImageUrlField);
        UIUtils.refreshComboBox(this.imageIndex, region.strImageIndex);
        UIUtils.refreshComboBox(this.animationMs, region.strAnimationMs);
        UIUtils.refreshComboBox(this.captureScreenX, region.strCaptureScreenX);
        UIUtils.refreshComboBox(this.captureScreenY, region.strCaptureScreenY);
        UIUtils.refreshComboBox(this.captureScreenWidth, region.strCaptureScreenWidth);
        UIUtils.refreshComboBox(this.captureScreenHeight, region.strCaptureScreenHeight);
        UIUtils.refreshComboBox(this.shapePanel.fillColor, region.strFillColor);
        UIUtils.refreshComboBox(this.shapePanel.lineColor, region.strLineColor);
        UIUtils.refreshComboBox(this.shapePanel.lineStyle, region.strLineStyle);
        UIUtils.refreshComboBox(this.shapePanel.lineThickness, region.strLineThickness);
        UIUtils.refreshComboBox(this.shapePanel.shapeList, region.shape);
        this.shapePanel.shapeArguments.setText(region.strShapeArgs);

        if (this.widgetPanel != null) {
            this.widgetPanel.refreshComponents();
        }
        if (this.mouseEventPanel != null) {
            this.mouseEventPanel.refresh();
        }
        if (this.regionOverlapEventsPanel != null) {
            this.regionOverlapEventsPanel.refresh();
        }
        if (generalSettingsPanel != null) {
            generalSettingsPanel.refreshComponents();
        }

        this.textArea.setText(region.strText);
        this.trajectory1.setText(region.strTrajectory1);
        this.trajectory2.setText(region.strTrajectory1);

        this.charactersPerLine.setText(region.strCharactersPerLine);
        this.maxNumLines.setText(region.strMaxNumLines);

        UIUtils.refreshCheckBox(canMove, region.bCanMove);
        UIUtils.refreshCheckBox(canRotate, region.bCanRotate);
        UIUtils.refreshCheckBox(canResize, region.bCanResize);
        UIUtils.refreshCheckBox(wrapText, region.bWrapText);
        UIUtils.refreshCheckBox(trimText, region.bTrimText);
        UIUtils.refreshCheckBox(walkThrough, region.bWalkThrough);
        UIUtils.refreshCheckBox(stickToTrajectory, region.bStickToTrajectory);
        UIUtils.refreshCheckBox(orientationTrajectory, region.bOrientationTrajectory);
        UIUtils.refreshCheckBox(captureScreen, region.bCaptureScreen);
        UIUtils.refreshCheckBox(captureScreenMouseMap, region.bCaptureScreenMouseMap);

        UIUtils.refreshTable(setPanel.table);
        UIUtils.refreshTable(tableAnimation);
        UIUtils.refreshTable(tableUpdateTransformations);
        UIUtils.refreshTable(tableLimits);
    }

    public void installPluginAutoCompletion() {
        if (this.widgetPanel != null) {
            this.widgetPanel.installPluginAutoCompletion();
        }
    }

    public void updateUIControls() {
        if (generalSettingsPanel != null) {
            generalSettingsPanel.updateUIControls();
        }
    }
}
