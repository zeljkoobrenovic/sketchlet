package net.sf.sketchlet.designer.editor.ui.region;

import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;
import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.UIUtils;
import net.sf.sketchlet.designer.editor.ui.desktop.Notepad;
import net.sf.sketchlet.designer.editor.ui.eventpanels.KeyboardEventsPanel;
import net.sf.sketchlet.designer.editor.ui.eventpanels.MouseEventsPanel;
import net.sf.sketchlet.designer.editor.ui.macros.ImageAreaSelect;
import net.sf.sketchlet.designer.editor.ui.profiles.Profiles;
import net.sf.sketchlet.designer.editor.ui.properties.PropertiesSetPanel;
import net.sf.sketchlet.framework.model.log.ActivityLog;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.model.Pages;
import net.sf.sketchlet.framework.model.programming.screenscripts.AWTRobotUtil;
import net.sf.sketchlet.framework.model.programming.screenscripts.TextTransfer;
import net.sf.sketchlet.framework.model.programming.timers.curves.Curves;
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

    private String[] columnNamesAnimation = {Language.translate("Dimension"), Language.translate("Animation Type"), Language.translate("Start Value"), Language.translate("End Value"), Language.translate("Cycle Duration"), Language.translate("Curve")};
    private String[] columnNamesUpdateTransformations = {Language.translate("Dimension"), Language.translate("Variable"), Language.translate("Start value"), Language.translate("End value"), Language.translate("Format")};
    private String[] columnLimits = {Language.translate("Dimension"), Language.translate("Min"), Language.translate("Max")};
    private ShapePanel shapePanel;
    private PropertiesSetPanel setPanel;
    private  AbstractTableModel modelAnimation;
    private AbstractTableModel modelUpdateTransformations;
    private JTable tableAnimation;
    private JScrollPane scrollAnimation;
    private JTable tableLimits;
    private AbstractTableModel modelLimits;
    private JScrollPane scrollLimits;
    private JTable tableUpdateTransformations;
    private JScrollPane scrollUpdateTransformations;
    private ActiveRegion region;
    private Pages pages;
    private JTabbedPane tabs = new JTabbedPane();
    private JTabbedPane tabsImage = new JTabbedPane();
    private JTabbedPane tabsTransform = new JTabbedPane();
    private JTabbedPane tabsMove = new JTabbedPane();
    private JTabbedPane tabsRegionEvents = new JTabbedPane();
    private ActiveRegionImageEditor imageEditor;
    private WidgetPanel widgetPanel;
    private GeneralSettingsPanel generalSettingsPanel;
    private JButton backwards = new JButton(Workspace.createImageIcon("resources/go-down.png"));
    private JButton upwards = new JButton(Workspace.createImageIcon("resources/go-up.png"));
    private JButton delete = new JButton(Workspace.createImageIcon("resources/user-trash.png"));
    private JTextField statusBar = new JTextField(25);
    private static JFileChooser fileChoser;
    private int eventRow = -1;
    private int mappingRow = -1;
    private static JComboBox regionComboBox = new JComboBox();
    private JButton deleteMapping = new JButton(Language.translate("Delete"));
    private JButton moveUpMapping = new JButton(Language.translate("Move Up"));
    private JButton moveDownMapping = new JButton(Language.translate("Move Down"));
    private JButton duplicateMapping = new JButton(Language.translate("Duplicate"));
    private JButton editMapping = new JButton(Language.translate("Edit"));
    private JComboBox comboBoxDim = new JComboBox();
    private double _start = 0.0;
    private double _end = 0.0;
    private double _init = 0.0;
    private boolean canUpdate = true;
    private static ActiveRegionPanel currentActiveRegionPanel = null;
    private static int indexGraphics = 0;
    private static int indexWidget = 1;
    private static int indexTransform = 2;
    private static int indexEvents = 3;
    private static int indexMouseEvents = 0;
    private static int indexKeyboardEvents = 0;
    private static int indexMotion = 1;
    private static int indexOverlap = 2;
    private static int indexGeneral = 4;
    private JComboBox imageUrlField = new JComboBox();
    private JComboBox imageIndex = new JComboBox();
    private JComboBox animationMs = new JComboBox();
    private JComboBox captureScreenX = new JComboBox();
    private JComboBox captureScreenY = new JComboBox();
    private JComboBox captureScreenWidth = new JComboBox();
    private JComboBox captureScreenHeight = new JComboBox();
    private RSyntaxTextArea textArea = Notepad.getInstance(RSyntaxTextArea.SYNTAX_STYLE_NONE);
    private JTextArea trajectory1 = new JTextArea();
    private JTextArea trajectory2 = new JTextArea();
    private JTextField charactersPerLine = new JTextField(5);
    private JTextField maxNumLines = new JTextField(5);
    private JCheckBox canMove = new JCheckBox(Language.translate("Enable moving by mouse"), true);
    private JCheckBox canRotate = new JCheckBox(Language.translate("Enable rotating by mouse"), true);
    private JCheckBox canResize = new JCheckBox(Language.translate("Enable resizing"));
    private JCheckBox wrapText = new JCheckBox(Language.translate("Wrap text"), false);
    private JCheckBox trimText = new JCheckBox(Language.translate("Trim"), false);
    private JCheckBox walkThrough = new JCheckBox(Language.translate("Make region solid (disable walk through)"));
    private JCheckBox stickToTrajectory = new JCheckBox(Language.translate("Stick to Trajectory"), true);
    private JCheckBox orientationTrajectory = new JCheckBox(Language.translate("Control Orientation"), true);
    private JCheckBox captureScreen = new JCheckBox(Language.translate("Capture Part of the Screen"), false);
    private JCheckBox captureScreenMouseMap = new JCheckBox(Language.translate("Map Mouse Clicks to Screen"), false);

    private KeyboardEventsPanel keyboardEventsPanel;
    private MouseEventsPanel mouseEventPanel;
    private RegionOverlapEventsPanel regionOverlapEventsPanel;

    public ActiveRegionPanel() {
        setCurrentActiveRegionPanel(this);
    }

    public static JFileChooser getFileChooser() {
        if (ActiveRegionPanel.fileChoser == null) {
            ActiveRegionPanel.fileChoser = new JFileChooser();
            ActiveRegionPanel.fileChoser.setCurrentDirectory(new File(SketchletContextUtils.getSketchletDesignerTemplateFilesDir()));
        }
        return ActiveRegionPanel.fileChoser;
    }

    public ActiveRegionPanel(Pages pages, ActiveRegion _action, int tabIndex) {
        setCurrentActiveRegionPanel(this);
        this.pages = pages;
        this.setRegion(_action);
        this.populateComboBoxes();
        modelUpdateTransformations = new AbstractTableModel() {

            public String getColumnName(int col) {
                return columnNamesUpdateTransformations[col].toString();
            }

            public int getRowCount() {
                return getRegion().updateTransformations.length;
            }

            public int getColumnCount() {
                return columnNamesUpdateTransformations.length;
            }

            public Object getValueAt(int row, int col) {
                return getRegion().updateTransformations[row][col];
            }

            public boolean isCellEditable(int row, int col) {
                return true;
            }

            public void setValueAt(Object value, int row, int col) {
                if (!getValueAt(row, col).toString().equals(value.toString())) {
                    SketchletEditor.getInstance().saveRegionUndo();
                }
                getRegion().updateTransformations[row][col] = value;
                fireTableCellUpdated(row, col);
                ActivityLog.log("setRegionContinuousMouseEvent", row + " " + col + " " + getRegion().updateTransformations[row][0] + " " + " " + getRegion().updateTransformations[row][1] + " " + " " + getRegion().updateTransformations[row][2]);
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
                return getRegion().propertiesAnimation.length;
            }

            public int getColumnCount() {
                return columnNamesAnimation.length;
            }

            public Object getValueAt(int row, int col) {
                return getRegion().propertiesAnimation[row][col] == null ? "" : getRegion().propertiesAnimation[row][col];
            }

            public boolean isCellEditable(int row, int col) {
                return col > 0 && getRegion().propertiesAnimation[row][1] != null;
            }

            public void setValueAt(Object value, int row, int col) {
                if (!getValueAt(row, col).toString().equals(value.toString())) {
                    SketchletEditor.getInstance().saveRegionUndo();
                }
                getRegion().propertiesAnimation[row][col] = value.toString();
                if (col == 1) {
                    if (value == null || value.toString().isEmpty()) {
                        getRegion().propertiesAnimation[row][2] = "";
                        getRegion().propertiesAnimation[row][3] = "";
                        getRegion().propertiesAnimation[row][4] = "";
                    } else {
                        String strProperty = getRegion().propertiesAnimation[row][0];
                        String start = getRegion().getMinValue(strProperty);
                        String end = getRegion().getMaxValue(strProperty);

                        if (getRegion().propertiesAnimation[row][2].isEmpty() && start != null) {
                            getRegion().propertiesAnimation[row][2] = start;
                        }
                        if (getRegion().propertiesAnimation[row][3].isEmpty() && end != null) {
                            getRegion().propertiesAnimation[row][3] = end;
                        }
                        if (getRegion().propertiesAnimation[row][4].isEmpty()) {
                            getRegion().propertiesAnimation[row][4] = "1.0";
                        }
                    }

                    ActivityLog.log("setRegionPropertiesAnimation", row + " " + col + " " + value);
                    this.fireTableRowsUpdated(row, row);
                }
            }

            public Class getColumnClass(int c) {
                return String.class;
            }
        };
        tableAnimation = new JTable(modelAnimation);
        tableAnimation.setDefaultRenderer(String.class, new PropertiesTableRenderer(getRegion().propertiesAnimation));

        TableColumn col = tableAnimation.getColumnModel().getColumn(1);
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

        setTableUpdateTransformations(new JTable(modelUpdateTransformations));
        getTableUpdateTransformations().addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent ke) {
                if ((ke.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
                    if (ke.getKeyCode() == ke.VK_V) {
                        int n = pasteInTable(getTableUpdateTransformations(), modelUpdateTransformations, getRegion().updateTransformations);
                        ActivityLog.log("pasteInTable", "regionContinousEvents " + n);
                    }
                }
            }
        });
        getTableUpdateTransformations().putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        modelLimits = new AbstractTableModel() {

            public String getColumnName(int col) {
                return columnLimits[col].toString();
            }

            public int getRowCount() {
                return getRegion().limits.length;
            }

            public int getColumnCount() {
                return columnLimits.length;
            }

            public Object getValueAt(int row, int col) {
                if (row >= 0) {
                    return getRegion().limits[row][col];
                } else {
                    return "";
                }
            }

            public boolean isCellEditable(int row, int col) {
                return col > 0;
            }

            public void setValueAt(Object value, int row, int col) {
                if (!getValueAt(row, col).toString().equals(value.toString())) {
                    SketchletEditor.getInstance().saveRegionUndo();
                }
                getRegion().limits[row][col] = value;
                fireTableCellUpdated(row, col);
                ActivityLog.log("setRegionLimits", row + " " + col + " " + getRegion().limits[row][0] + " " + " " + getRegion().limits[row][1] + " " + " " + getRegion().limits[row][2]);
            }

            public Class getColumnClass(int c) {
                return String.class;
            }
        };

        setTableLimits(new JTable(modelLimits));
        getTableLimits().putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        TableColumn dimensionColumn = getTableUpdateTransformations().getColumnModel().getColumn(0);

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


        getTableUpdateTransformations().setFillsViewportHeight(true);
        getTableUpdateTransformations().getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent event) {
                if (event.getValueIsAdjusting()) {
                    return;
                }
                mappingRow = getTableUpdateTransformations().getSelectedRow();
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
                SketchletEditor.getInstance().setHorizontalAlignment("left");
                RefreshTime.update();
                SketchletEditor.getInstance().repaint();
            }
        });
        JButton alignCenter = new JButton(Workspace.createImageIcon("resources/center.gif"));
        alignCenter.setToolTipText(Language.translate("Align Center"));
        alignCenter.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().setHorizontalAlignment("center");
                RefreshTime.update();
                SketchletEditor.getInstance().repaint();
            }
        });
        JButton alignRight = new JButton(Workspace.createImageIcon("resources/right.gif"));
        alignRight.setToolTipText(Language.translate("Align Right"));
        alignRight.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().setHorizontalAlignment("right");
                RefreshTime.update();
                SketchletEditor.getInstance().repaint();
            }
        });
        JButton alignTop = new JButton(Workspace.createImageIcon("resources/align-top.png"));
        alignTop.setToolTipText(Language.translate("Align Top"));
        alignTop.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().setVerticalAlignment("top");
                RefreshTime.update();
                SketchletEditor.getInstance().repaint();
            }
        });
        JButton alignMiddle = new JButton(Workspace.createImageIcon("resources/align-centered.png"));
        alignMiddle.setToolTipText(Language.translate("Align Middle"));
        alignMiddle.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().setVerticalAlignment("center");
                RefreshTime.update();
                SketchletEditor.getInstance().repaint();
            }
        });
        JButton alignBottom = new JButton(Workspace.createImageIcon("resources/align-bottom.png"));
        alignBottom.setToolTipText(Language.translate("Align Bottom"));
        alignBottom.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().setVerticalAlignment("bottom");
                RefreshTime.update();
                SketchletEditor.getInstance().repaint();
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

        showTextPanel.add(Notepad.getEditorPanel(textArea, false), BorderLayout.CENTER);

        this.textArea.setText(getRegion().text);
        this.textArea.addKeyListener(new KeyAdapter() {

            public void keyReleased(KeyEvent e) {
                if (!getRegion().text.equals(textArea.getText())) {
                    SketchletEditor.getInstance().saveRegionUndo();
                    getRegion().text = textArea.getText();
                    RefreshTime.update();
                    SketchletEditor.getInstance().repaint();
                }
            }
        });
        this.trajectory1.setText(getRegion().trajectory1);
        this.trajectory1.addKeyListener(new KeyAdapter() {

            public void keyReleased(KeyEvent e) {
                if (!getRegion().trajectory1.equals(trajectory1.getText())) {
                    SketchletEditor.getInstance().saveRegionUndo();
                    getRegion().trajectory1 = trajectory1.getText();
                    RefreshTime.update();
                    SketchletEditor.getInstance().repaint();
                }
            }
        });
        this.trajectory2.setText(getRegion().trajectory1);
        this.trajectory2.addKeyListener(new KeyAdapter() {

            public void keyReleased(KeyEvent e) {
                if (!getRegion().trajectory2.equals(trajectory2.getText())) {
                    SketchletEditor.getInstance().saveRegionUndo();
                    getRegion().trajectory2 = trajectory2.getText();
                    RefreshTime.update();
                    SketchletEditor.getInstance().repaint();
                }
            }
        });

        this.charactersPerLine.setText(getRegion().charactersPerLine);
        this.charactersPerLine.addKeyListener(new KeyAdapter() {

            public void keyReleased(KeyEvent e) {
                if (!getRegion().charactersPerLine.equals(charactersPerLine.getText())) {
                    SketchletEditor.getInstance().saveRegionUndo();
                    getRegion().charactersPerLine = charactersPerLine.getText();
                    RefreshTime.update();
                    SketchletEditor.getInstance().repaint();
                }
            }
        });
        this.maxNumLines.setText(getRegion().maxNumLines);
        this.maxNumLines.addKeyListener(new KeyAdapter() {

            public void keyReleased(KeyEvent e) {
                if (!getRegion().maxNumLines.equals(maxNumLines.getText())) {
                    SketchletEditor.getInstance().saveRegionUndo();
                    getRegion().maxNumLines = maxNumLines.getText();
                    RefreshTime.update();
                    SketchletEditor.getInstance().repaint();
                }
            }
        });

        JPanel southPanel = new JPanel();
        JPanel textSettings = new JPanel();
        textSettings.add(this.trimText);
        textSettings.add(this.wrapText);
        textSettings.add(this.charactersPerLine);

        textSettings.add(new JLabel(Language.translate(" characters per line ")));
        textSettings.add(new JLabel(Language.translate(" Show at most ")));
        textSettings.add(this.maxNumLines);
        textSettings.add(new JLabel(Language.translate(" lines")));
        southPanel.setLayout(new BorderLayout());
        southPanel.add(textSettings, BorderLayout.SOUTH);
        showTextPanel.add(southPanel, BorderLayout.SOUTH);

        final JPanel urlPanel = new JPanel();
        urlPanel.add(new JLabel(Language.translate("URL/path:")));
        urlPanel.add(this.getImageUrlField());
        JButton selectFile = new JButton("...");

        selectFile.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChoser.showOpenDialog(urlPanel);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    getImageUrlField().setSelectedItem(fileChoser.getSelectedFile().getPath());
                    RefreshTime.update();
                    SketchletEditor.getInstance().repaint();
                }
            }
        });

        urlPanel.add(selectFile);

        JPanel mappingLeftPane = new JPanel(new BorderLayout());

        JPanel checkPanel = new JPanel();
        checkPanel.setBorder(BorderFactory.createTitledBorder(Language.translate("Enable motion")));

        checkPanel.add(this.canMove);
        checkPanel.add(this.canRotate);
        scrollLimits = new JScrollPane(getTableLimits());
        scrollUpdateTransformations = new JScrollPane(getTableUpdateTransformations());
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

        showImagePanel.add(getDrawingPanel(getRegion()), BorderLayout.CENTER);

        JButton buttonDefineCaptureArea = new JButton(Language.translate("Define capturing area"));
        buttonDefineCaptureArea.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Dimension dimScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
                Rectangle rectScreenSize = new Rectangle(dimScreenSize);
                BufferedImage captureedImage = AWTRobotUtil.getRobot().createScreenCapture(rectScreenSize);
                int x = 0;
                int y = 0;
                int w = 100;
                int h = 100;
                try {
                    x = Integer.parseInt(getRegion().captureScreenX);
                    y = Integer.parseInt(getRegion().captureScreenY);
                    w = Integer.parseInt(getRegion().captureScreenWidth);
                    h = Integer.parseInt(getRegion().captureScreenHeight);
                } catch (Throwable eNum) {
                }
                ImageAreaSelect.createAndShowGUI(ActiveRegionsFrame.reagionsAndActions, captureedImage, x, y, w, h, false);
                if (ImageAreaSelect.bSaved) {
                    captureScreen.setSelected(true);
                    String params[] = ImageAreaSelect.strArea.split(" ");
                    if (params.length >= 4) {
                        getCaptureScreenX().setSelectedItem(params[0]);
                        getCaptureScreenY().setSelectedItem(params[1]);
                        getCaptureScreenWidth().setSelectedItem(params[2]);
                        getCaptureScreenHeight().setSelectedItem(params[3]);
                    }
                }
                RefreshTime.update();
                SketchletEditor.getInstance().repaint();
            }
        });

        canMove.setSelected(getRegion().movable);
        canRotate.setSelected(getRegion().rotatable);
        canResize.setSelected(getRegion().resizable);
        wrapText.setSelected(getRegion().textWrapped);
        trimText.setSelected(getRegion().textTrimmed);
        walkThrough.setSelected(getRegion().walkThroughEnabled);
        stickToTrajectory.setSelected(getRegion().stickToTrajectoryEnabled);
        orientationTrajectory.setSelected(getRegion().changingOrientationOnTrajectoryEnabled);
        captureScreen.setSelected(getRegion().screenCapturingEnabled);
        captureScreenMouseMap.setSelected(getRegion().screenCapturingMouseMappingEnabled);

        canMove.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                getRegion().movable = canMove.isSelected();
            }
        });
        canRotate.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                SketchletEditor.getInstance().saveRegionUndo();
                getRegion().rotatable = canRotate.isSelected();
            }
        });
        canResize.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                SketchletEditor.getInstance().saveRegionUndo();
                getRegion().resizable = canResize.isSelected();
            }
        });
        wrapText.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                SketchletEditor.getInstance().saveRegionUndo();
                getRegion().textWrapped = wrapText.isSelected();
            }
        });
        trimText.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                SketchletEditor.getInstance().saveRegionUndo();
                getRegion().textTrimmed = trimText.isSelected();
            }
        });
        walkThrough.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                SketchletEditor.getInstance().saveRegionUndo();
                getRegion().walkThroughEnabled = walkThrough.isSelected();
            }
        });
        stickToTrajectory.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                SketchletEditor.getInstance().saveRegionUndo();
                getRegion().stickToTrajectoryEnabled = stickToTrajectory.isSelected();
            }
        });
        orientationTrajectory.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                SketchletEditor.getInstance().saveRegionUndo();
                getRegion().changingOrientationOnTrajectoryEnabled = orientationTrajectory.isSelected();
            }
        });
        captureScreen.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                SketchletEditor.getInstance().saveRegionUndo();
                getRegion().screenCapturingEnabled = captureScreen.isSelected();
            }
        });
        captureScreenMouseMap.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                SketchletEditor.getInstance().saveRegionUndo();
                getRegion().screenCapturingMouseMappingEnabled = captureScreenMouseMap.isSelected();
            }
        });


        this.getImageUrlField().setSelectedItem(getRegion().imageUrlField);
        this.getImageUrlField().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (getImageUrlField().getSelectedItem() != null) {
                    if (!getRegion().imageUrlField.equals(getImageUrlField().getSelectedItem().toString())) {
                        SketchletEditor.getInstance().saveRegionUndo();
                        getRegion().imageUrlField = (String) getImageUrlField().getSelectedItem();
                        RefreshTime.update();
                        SketchletEditor.getInstance().repaint();
                    }
                }
            }
        });
        UIUtils.populateVariablesCombo(this.getImageUrlField(), true);
        UIUtils.populateVariablesCombo(this.getImageIndex(), true);
        UIUtils.populateVariablesCombo(this.getAnimationMs(), true);
        UIUtils.populateVariablesCombo(this.getCaptureScreenX(), true);
        UIUtils.populateVariablesCombo(this.getCaptureScreenY(), true);
        UIUtils.populateVariablesCombo(this.getCaptureScreenWidth(), true);
        UIUtils.populateVariablesCombo(this.getCaptureScreenHeight(), true);

        UIUtils.removeActionListeners(this.getImageIndex());
        this.getImageIndex().setSelectedItem(getRegion().strImageIndex);
        this.getImageIndex().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (getImageIndex().getSelectedItem() != null && !getRegion().strImageIndex.equals(getImageIndex().getSelectedItem().toString())) {
                    SketchletEditor.getInstance().saveRegionUndo();
                    getRegion().strImageIndex = (String) getImageIndex().getSelectedItem();
                    RefreshTime.update();
                    SketchletEditor.getInstance().repaint();
                }
            }
        });
        UIUtils.removeActionListeners(this.getAnimationMs());
        this.getAnimationMs().setSelectedItem(getRegion().strAnimationMs);
        this.getAnimationMs().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (getAnimationMs().getSelectedItem() != null && !getRegion().strAnimationMs.equals(getAnimationMs().getSelectedItem().toString())) {
                    SketchletEditor.getInstance().saveRegionUndo();
                    getRegion().strAnimationMs = (String) getAnimationMs().getSelectedItem();
                    RefreshTime.update();
                    SketchletEditor.getInstance().repaint();
                }
            }
        });

        getCaptureScreenX().setSelectedItem(getRegion().captureScreenX);
        this.getCaptureScreenX().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (getCaptureScreenX().getSelectedItem() != null && !getRegion().captureScreenX.equals(getCaptureScreenX().getSelectedItem().toString())) {
                    SketchletEditor.getInstance().saveRegionUndo();
                    getRegion().captureScreenX = (String) getCaptureScreenX().getSelectedItem();
                    RefreshTime.update();
                    SketchletEditor.getInstance().repaint();
                }
            }
        });

        getCaptureScreenY().setSelectedItem(getRegion().captureScreenY);
        this.getCaptureScreenY().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (getCaptureScreenY().getSelectedItem() != null && !getRegion().captureScreenY.equals(getCaptureScreenY().getSelectedItem().toString())) {
                    SketchletEditor.getInstance().saveRegionUndo();
                    getRegion().captureScreenY = (String) getCaptureScreenY().getSelectedItem();
                    RefreshTime.update();
                    SketchletEditor.getInstance().repaint();
                }
            }
        });

        getCaptureScreenWidth().setSelectedItem(getRegion().captureScreenWidth);
        this.getCaptureScreenWidth().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (getCaptureScreenWidth().getSelectedItem() != null && !getRegion().captureScreenWidth.equals(getCaptureScreenWidth().getSelectedItem().toString())) {
                    SketchletEditor.getInstance().saveRegionUndo();
                    getRegion().captureScreenWidth = (String) getCaptureScreenWidth().getSelectedItem();
                    RefreshTime.update();
                    SketchletEditor.getInstance().repaint();
                }
            }
        });

        getCaptureScreenHeight().setSelectedItem(getRegion().captureScreenHeight);
        this.getCaptureScreenHeight().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (getCaptureScreenHeight().getSelectedItem() != null && !getRegion().captureScreenHeight.equals(getCaptureScreenHeight().getSelectedItem().toString())) {
                    SketchletEditor.getInstance().saveRegionUndo();
                    getRegion().captureScreenHeight = (String) getCaptureScreenHeight().getSelectedItem();
                    RefreshTime.update();
                    SketchletEditor.getInstance().repaint();
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
                if (getRegion().captureScreenWidth != null && !getRegion().captureScreenWidth.isEmpty()) {
                    try {
                        SketchletEditor.getInstance().saveRegionUndo();
                        getRegion().setWidth((int) Double.parseDouble(getRegion().captureScreenWidth));
                        RefreshTime.update();
                        SketchletEditor.getInstance().repaint();
                    } catch (Throwable e) {
                    }
                }
            }
        });
        btnScreenHeightToRegion.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (getRegion().captureScreenHeight != null && !getRegion().captureScreenHeight.isEmpty()) {
                    try {
                        SketchletEditor.getInstance().saveRegionUndo();
                        getRegion().setHeight((int) Double.parseDouble(getRegion().captureScreenHeight));
                        RefreshTime.update();
                        SketchletEditor.getInstance().repaint();
                    } catch (Throwable e) {
                    }
                }
            }
        });
        btnRegionToScreenWidth.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                getCaptureScreenWidth().setSelectedItem("" + getRegion().getWidth());
                RefreshTime.update();
                SketchletEditor.getInstance().repaint();
            }
        });
        btnRegionToScreenHeight.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                getCaptureScreenHeight().setSelectedItem("" + getRegion().getHeight());
                RefreshTime.update();
                SketchletEditor.getInstance().repaint();
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
        screenCapturePanel.add(this.getCaptureScreenX());

        screenCapturePanel.add(new JLabel(Language.translate("Width: "), JLabel.RIGHT));
        screenCapturePanel.add(this.getCaptureScreenWidth());
        screenCapturePanel.add(btnScreenWidthToRegion);
        screenCapturePanel.add(btnRegionToScreenWidth);

        screenCapturePanel.add(new JLabel(Language.translate("Y (top): "), JLabel.RIGHT));
        screenCapturePanel.add(this.getCaptureScreenY());

        screenCapturePanel.add(new JLabel(Language.translate("Height: "), JLabel.RIGHT));
        screenCapturePanel.add(getCaptureScreenHeight());
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
                UIUtils.deleteTableRows(getTableUpdateTransformations(), modelUpdateTransformations, getRegion().updateTransformations);
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
                    SketchletEditor.getInstance().saveRegionUndo();
                    Object[] rowData1 = getRegion().updateTransformations[mappingRow];
                    Object[] rowData2 = getRegion().updateTransformations[mappingRow - 1];

                    getRegion().updateTransformations[mappingRow] = rowData2;
                    getRegion().updateTransformations[mappingRow - 1] = rowData1;

                    int r = mappingRow - 1;

                    modelUpdateTransformations.fireTableDataChanged();

                    getTableUpdateTransformations().getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });
        moveDownMapping.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (mappingRow < getRegion().updateTransformations.length - 1) {
                    SketchletEditor.getInstance().saveRegionUndo();
                    Object[] rowData1 = getRegion().updateTransformations[mappingRow];
                    Object[] rowData2 = getRegion().updateTransformations[mappingRow + 1];

                    getRegion().updateTransformations[mappingRow] = rowData2;
                    getRegion().updateTransformations[mappingRow + 1] = rowData1;

                    int r = mappingRow + 1;

                    modelUpdateTransformations.fireTableDataChanged();

                    getTableUpdateTransformations().getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });
        duplicateMapping.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = mappingRow;
                if (row < getRegion().updateTransformations.length - 1) {
                    SketchletEditor.getInstance().saveRegionUndo();

                    for (int i = getRegion().updateTransformations.length - 2; i >= row + 1; i--) {
                        getRegion().updateTransformations[i + 1] = getRegion().updateTransformations[i];
                    }

                    getRegion().updateTransformations[row + 1] = new Object[]{
                            "" + getRegion().updateTransformations[row][0],
                            "" + getRegion().updateTransformations[row][1],
                            "" + getRegion().updateTransformations[row][2],
                            "" + getRegion().updateTransformations[row][3],
                            "" + getRegion().updateTransformations[row][4]
                    };

                    int r = row + 1;

                    modelUpdateTransformations.fireTableDataChanged();
                    getTableUpdateTransformations().getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });

        enableControls();

        shapePanel = new ShapePanel(this.getRegion());

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
                SketchletEditor.getInstance().saveRegionUndo();
                trajectory1.setText("");
                trajectory2.setText("");
                RefreshTime.update();
                SketchletEditor.getInstance().repaint();
            }
        });
        mappingLeftPane.add(ctrlTrPanel, BorderLayout.SOUTH);
        // trajectPanel.add(new JScrollPane(action.trajectory1));
        //mappingLeftPane.add(trajectPanel, BorderLayout.SOUTH);
        JPanel mappingGridPane = new JPanel(new BorderLayout());
        mappingGridPane.add(mappingLeftPane, BorderLayout.WEST);
        mappingGridPane.add(panelVariablesMapping);

        getTabsMove().putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(getTabsMove());
        getTabsMove().addTab(Language.translate("Settings"), mappingGridPane);
        JPanel panelTrajectoryPoints = new JPanel(new GridLayout(1, 2));

        JScrollPane tp1 = new JScrollPane(trajectory1);
        JScrollPane tp2 = new JScrollPane(trajectory2);
        tp1.setBorder(BorderFactory.createTitledBorder(Language.translate("Primary trajectory (x y time)")));
        tp2.setBorder(BorderFactory.createTitledBorder(Language.translate("Secondary trajectory (x y time)")));
        panelTrajectoryPoints.add(tp1);
        panelTrajectoryPoints.add(tp2);
        getTabsMove().setTabPlacement(JTabbedPane.LEFT);
        getTabsMove().add(Language.translate("Motion Limits"), scrollLimits);
        getTabsMove().addTab(Language.translate("Trajectory Points"), panelTrajectoryPoints);
        movablePanel.add(getTabsMove());

        final JButton defineClip = new JButton(Language.translate("Define Visible Area"), Workspace.createImageIcon("resources/clip.png"));
        defineClip.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().saveRegionUndo();
                SketchletEditor.getInstance().defineClip();
                RefreshTime.update();
                SketchletEditor.getInstance().repaint();
            }
        });
        JPanel panel = new JPanel();
        panel.add(defineClip);

        getTabsImage().setFont(getTabsImage().getFont().deriveFont(9.0f));
        getTabsImage().putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(getTabsImage());

        getTabsImage().add(showImagePanel, Language.translate("Image"));
        if (Profiles.isActive("active_region_shape")) {
            JPanel shapePanelWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
            shapePanelWrapper.add(shapePanel);
            getTabsImage().add(shapePanelWrapper, Language.translate("Shape"));
        }
        if (Profiles.isActive("active_region_url")) {
            JPanel urlPanelWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
            urlPanelWrapper.add(urlPanel);
            getTabsImage().add(urlPanel, Language.translate("URL/file path"));
        }
        if (Profiles.isActive("active_region_screen_capture")) {
            JPanel screenCapturePanelWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
            screenCapturePanelWrapper.add(screenCapturePanel);
            getTabsImage().add(screenCapturePanelWrapper, Language.translate("Screen Capture"));
        }
        if (Profiles.isActive("active_region_text")) {
            getTabsImage().add(showTextPanel, Language.translate("Text"));
        }

        getTabs().setFont(getTabsImage().getFont().deriveFont(9.0f));
        getTabs().putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(getTabs());

        getTabs().applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        getTabs().setTabPlacement(JTabbedPane.LEFT);
        getTabs().setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        getTabs().add(getTabsImage(), Language.translate("Graphics "));
        setIndexGraphics(getTabs().getTabCount() - 1);
        setPanel = new PropertiesSetPanel(getRegion());
        setPanel.setSaveUndoAction(new Runnable() {

            public void run() {
                SketchletEditor.getInstance().saveRegionUndo();
            }
        });

        JPanel panelAnimation = new JPanel(new BorderLayout());
        JPanel panelButtons = new JPanel(new BorderLayout());
        final JButton btnClear = new JButton(Language.translate("Clear"));
        btnClear.setEnabled(false);
        btnClear.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableAnimation.getSelectedRow();
                if (row >= 0 && getRegion().propertiesAnimation[row][1] != null) {
                    getRegion().propertiesAnimation[row][1] = "";
                    getRegion().propertiesAnimation[row][2] = "";
                    getRegion().propertiesAnimation[row][3] = "";
                    getRegion().propertiesAnimation[row][4] = "";
                    getRegion().propertiesAnimation[row][5] = "";
                    modelAnimation.fireTableDataChanged();
                }
            }
        });
        final JButton btnStart = new JButton(Language.translate("<- Start"));
        btnStart.setEnabled(false);
        btnStart.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableAnimation.getSelectedRow();
                if (row >= 0 && getRegion().propertiesAnimation[row][1] != null) {
                    getRegion().propertiesAnimation[row][2] = getRegion().getProperty(getRegion().propertiesAnimation[row][0]);
                    modelAnimation.fireTableRowsUpdated(row, row);
                }
            }
        });
        final JButton btnEnd = new JButton(Language.translate("<- End"));
        btnEnd.setEnabled(false);
        btnEnd.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableAnimation.getSelectedRow();
                if (row >= 0 && getRegion().propertiesAnimation[row][1] != null) {
                    getRegion().propertiesAnimation[row][3] = getRegion().getProperty(getRegion().propertiesAnimation[row][0]);
                    modelAnimation.fireTableRowsUpdated(row, row);
                }
            }
        });
        final JTextField startField = new JTextField(5);
        final JSlider slider = new JSlider(JSlider.VERTICAL, 0, 100, 0);
        final JTextField endField = new JTextField(5);
        startField.setHorizontalAlignment(JTextField.CENTER);
        endField.setHorizontalAlignment(JTextField.CENTER);

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

        JPanel panelButtonsWrapper = new JPanel(new BorderLayout());
        panelButtonsWrapper.add(panelButtons);

        panelAnimation.add(panelButtonsWrapper, BorderLayout.EAST);
        tableAnimation.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent event) {
                int row = tableAnimation.getSelectedRow();
                boolean bEnable = row >= 0 && getRegion().propertiesAnimation[row][1] != null;
                btnClear.setEnabled(bEnable);
                btnStart.setEnabled(bEnable);
                btnEnd.setEnabled(bEnable);
                slider.setEnabled(bEnable);
                startField.setEnabled(bEnable);
                endField.setEnabled(bEnable);

                String start = null;
                String end = null;
                String init;
                if (bEnable) {
                    String strProperty = getRegion().propertiesAnimation[row][0];
                    start = getRegion().getMinValue(strProperty);
                    end = getRegion().getMaxValue(strProperty);
                    init = getRegion().getProperty(strProperty);
                    if (init == null || init.isEmpty()) {
                        String defaultValue = getRegion().getDefaultValue(strProperty);
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

                        canUpdate = false;
                        slider.setValue(slider.getMinimum() + (int) ((slider.getMaximum() - slider.getMinimum()) * (_init - Math.min(_start, _end)) / Math.abs(_end - _start)));
                        canUpdate = true;
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
        getTabsImage().addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                int index = getTabsImage().getSelectedIndex();
                if (index >= 0) {
                    ActivityLog.log("selectTabRegionImage", getTabsImage().getTitleAt(index));
                }
            }
        });
        getTabsRegionEvents().addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                SketchletEditor.getInstance().getExtraEditorPanel().openRelevantHelpPage(false);
            }
        });
        getTabsTransform().addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                int index = getTabsTransform().getSelectedIndex();
                if (index >= 0) {
                    ActivityLog.log("selectTabRegionTransform", getTabsTransform().getTitleAt(index));
                }
            }
        });
        slider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (!canUpdate) {
                    return;
                }

                canUpdate = false;
                try {
                    int row = tableAnimation.getSelectedRow();
                    if (row >= 0) {
                        String start = startField.getText();
                        String end = endField.getText();
                        if (start != null && end != null) {
                            _start = Double.parseDouble(start);
                            _end = Double.parseDouble(end);
                            int fps = (int) slider.getValue();

                            getRegion().setProperty(getRegion().propertiesAnimation[row][0], "" + (Math.min(_start, _end) + Math.abs(_start - _end) * fps / (slider.getMaximum() - slider.getMinimum())));
                            SketchletEditor.getInstance().repaint();
                        }
                    }
                } catch (Exception e2) {
                }

                canUpdate = true;
            }
        });

        col = tableAnimation.getColumnModel().getColumn(5);
        col.setCellEditor(new DefaultCellEditor(Curves.getGlobalCurves().getComboBox()));
        scrollAnimation = new JScrollPane(tableAnimation);
        panelAnimation.add(scrollAnimation);

        getTabsTransform().add(Language.translate("Set Transformation"), setPanel);
        getTabsTransform().add(Language.translate("Animate Transformation"), panelAnimation);
        getTabsTransform().setFont(getTabsImage().getFont().deriveFont(9.0f));
        getTabsTransform().putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(getTabsTransform());


        if (Profiles.isActive("active_region_widget")) {
            widgetPanel = new WidgetPanel(this.getRegion());
            getTabs().add(widgetPanel, Language.translate("Widget "));
            setIndexWidget(getTabs().getTabCount() - 1);
        }
        if (Profiles.isActive("active_region_transformations")) {
            getTabs().add(getTabsTransform(), Language.translate("Transformations "));
            setIndexTransform(getTabs().getTabCount() - 1);
        }

        getTabs().addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (!getTabs().isShowing()) {
                    return;
                }
                int n = getTabs().getSelectedIndex();
                if (n >= 0) {
                    String title = getTabs().getTitleAt(n).trim().toLowerCase();

                    if (n == getIndexGraphics()) {
                        SketchletEditor.getInstance().getHelpViewer().showAutoHelpByID("active_region_image");
                    } else if (n == getIndexTransform()) {
                        SketchletEditor.getInstance().getHelpViewer().showAutoHelpByID("active_region_transform");
                    } else if (n == getIndexEvents()) {
                        int n2 = getTabsRegionEvents().getSelectedIndex();

                        if (n2 == getIndexMotion()) {
                            SketchletEditor.getInstance().getHelpViewer().showAutoHelpByID("active_region_move");
                        } else if (n2 == getIndexMouseEvents()) {
                            SketchletEditor.getInstance().getHelpViewer().showAutoHelpByID("active_region_mouse");
                        } else if (n2 == getIndexOverlap()) {
                            SketchletEditor.getInstance().getHelpViewer().showAutoHelpByID("active_region_interaction");
                        }
                    } else if (n == getIndexWidget()) {
                        SketchletEditor.getInstance().getHelpViewer().showAutoHelpByID("active_region_widget");
                    } else if (n == getIndexGeneral()) {
                        SketchletEditor.getInstance().getHelpViewer().showAutoHelpByID("active_region_general");
                    }
                    ActivityLog.log("selectTabRegionSettings", title);
                }
            }
        });

        getTabs().setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        getTabsRegionEvents().putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(getTabsRegionEvents());

        if (Profiles.isActiveAny("active_region_move,active_region_mouse,active_region_mouse,active_region_overlap")) {
            getTabs().add(getTabsRegionEvents(), Language.translate("Region Events "));
            setIndexEvents(getTabs().getTabCount() - 1);
        }
        if (Profiles.isActive("active_region_mouse")) {
            setMouseEventPanel(new MouseEventsPanel(this.getRegion().mouseProcessor));
            getTabsRegionEvents().add(getMouseEventPanel(), Language.translate("Mouse Events "));
            setIndexMouseEvents(getTabsRegionEvents().getTabCount() - 1);
        }
        if (Profiles.isActive("active_region_keyboard")) {
            setKeyboardEventsPanel(new KeyboardEventsPanel(this.getRegion().keyboardProcessor));
            getTabsRegionEvents().add(getKeyboardEventsPanel(), Language.translate("Keyboard Events "));
            setIndexKeyboardEvents(getTabsRegionEvents().getTabCount() - 1);
        }
        if (Profiles.isActive("active_region_move")) {
            getTabsRegionEvents().add(movablePanel, Language.translate("Move & Rotate "));
            setIndexMotion(getTabsRegionEvents().getTabCount() - 1);
        }
        if (Profiles.isActive("active_region_overlap")) {
            setRegionOverlapEventsPanel(new RegionOverlapEventsPanel(this.getRegion()));
            getTabsRegionEvents().add(getRegionOverlapEventsPanel(), Language.translate("Overlap & Touch "));
            setIndexOverlap(getTabsRegionEvents().getTabCount() - 1);
        }

        if (Profiles.isActive("active_region_general")) {
            generalSettingsPanel = new GeneralSettingsPanel(getRegion());
            getTabs().add(generalSettingsPanel, Language.translate("General "));
            setIndexGeneral(getTabs().getTabCount() - 1);
        }

        if (tabIndex >= 0 && tabIndex < getTabs().getTabCount()) {
            getTabs().setSelectedIndex(tabIndex);
        }

        this.add(getTabs(), BorderLayout.CENTER);

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
                SketchletEditor.getInstance().moveCurrentActionUpwards();
                RefreshTime.update();
                SketchletEditor.getInstance().repaint();
            }
        });

        backwards.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.getInstance().moveCurrentActionBackwards();
                RefreshTime.update();
                SketchletEditor.getInstance().repaint();
            }
        });

        delete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SketchletEditor.getInstance().deleteSelectedRegion();
                RefreshTime.update();
                SketchletEditor.getInstance().repaint();
            }
        });

        if (widgetPanel != null) {
            widgetPanel.reloadWidgetEvents();
        }
    }

    public static int getIndexGraphics() {
        return indexGraphics;
    }

    public static void setIndexGraphics(int indexGraphics) {
        ActiveRegionPanel.indexGraphics = indexGraphics;
    }

    public static int getIndexWidget() {
        return indexWidget;
    }

    public static void setIndexWidget(int indexWidget) {
        ActiveRegionPanel.indexWidget = indexWidget;
    }

    public static int getIndexTransform() {
        return indexTransform;
    }

    public static void setIndexTransform(int indexTransform) {
        ActiveRegionPanel.indexTransform = indexTransform;
    }

    public static int getIndexEvents() {
        return indexEvents;
    }

    public static void setIndexEvents(int indexEvents) {
        ActiveRegionPanel.indexEvents = indexEvents;
    }

    public static int getIndexMouseEvents() {
        return indexMouseEvents;
    }

    public static void setIndexMouseEvents(int indexMouseEvents) {
        ActiveRegionPanel.indexMouseEvents = indexMouseEvents;
    }

    public static int getIndexKeyboardEvents() {
        return indexKeyboardEvents;
    }

    public static void setIndexKeyboardEvents(int indexKeyboardEvents) {
        ActiveRegionPanel.indexKeyboardEvents = indexKeyboardEvents;
    }

    public static int getIndexMotion() {
        return indexMotion;
    }

    public static void setIndexMotion(int indexMotion) {
        ActiveRegionPanel.indexMotion = indexMotion;
    }

    public static int getIndexOverlap() {
        return indexOverlap;
    }

    public static void setIndexOverlap(int indexOverlap) {
        ActiveRegionPanel.indexOverlap = indexOverlap;
    }

    public static int getIndexGeneral() {
        return indexGeneral;
    }

    public static void setIndexGeneral(int indexGeneral) {
        ActiveRegionPanel.indexGeneral = indexGeneral;
    }

    public static ActiveRegionPanel getCurrentActiveRegionPanel() {
        return currentActiveRegionPanel;
    }

    public static void setCurrentActiveRegionPanel(ActiveRegionPanel currentActiveRegionPanel) {
        ActiveRegionPanel.currentActiveRegionPanel = currentActiveRegionPanel;
    }

    public void setDropHandlers() {
        new FileDrop(System.out, this.getTableUpdateTransformations(), new FileDrop.Listener() {

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
                    ActiveRegionPanel ap = ActiveRegionsFrame.refresh(getRegion(), ActiveRegionPanel.getIndexEvents(), ActiveRegionPanel.getIndexMotion());
                    int row = ap.getFreeMappingRow();
                    getRegion().updateTransformations[row][1] = strText.substring(1);
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

        TableColumn formatColumn = this.getTableUpdateTransformations().getColumnModel().getColumn(4);
        formatColumn.setCellEditor(new DefaultCellEditor(comboBoxFormat));
        formatColumn.setCellEditor(new DefaultCellEditor(comboBoxFormat));
    }

    JComboBox comboBoxVariables = new JComboBox();

    public void setVariablesCombo() {
        comboBoxVariables = new JComboBox();
        comboBoxVariables.setEditable(true);
        comboBoxVariables.addItem("");

        for (String strVar : VariablesBlackboard.getInstance().getVariablesList()) {
            comboBoxVariables.addItem(strVar);
        }

        getTableUpdateTransformations().getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(comboBoxVariables));
    }

    JComboBox comboBox = new JComboBox();

    public void setVariablesComboWithEquals() {
        comboBox.removeAllItems();
        comboBox.setEditable(true);
        comboBox.addItem("");

        for (String strVar : VariablesBlackboard.getInstance().getVariablesList()) {
            comboBox.addItem("=" + strVar);
        }

        getTableUpdateTransformations().getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(comboBox));
        getTableUpdateTransformations().getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(comboBox));
    }

    public static int pasteInTable(JTable table, AbstractTableModel model, Object data[][]) {
        SketchletEditor.getInstance().saveRegionUndo();
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
        setImageEditor(new ActiveRegionImageEditor(a, this));

        JPanel freeHandPanel = new JPanel();
        freeHandPanel.setLayout(new BorderLayout());

        JPanel colorPanelWrapper = new JPanel(new BorderLayout());

        JPanel frameGrid = new JPanel(new GridLayout(0, 2));
        frameGrid.setBorder(BorderFactory.createTitledBorder(Language.translate("flipbook")));
        JLabel label1 = new JLabel(Language.translate("  active frame: "));
        label1.setFont(label1.getFont().deriveFont(9.0f));
        frameGrid.add(label1);
        frameGrid.add(this.getImageIndex());
        this.getImageIndex().setPreferredSize(new Dimension(70, 25));
        JLabel label2 = new JLabel(Language.translate("  animation (ms): "));
        label2.setFont(label2.getFont().deriveFont(9.0f));
        frameGrid.add(label2);
        frameGrid.add(this.getAnimationMs());

        colorPanelWrapper.add(frameGrid, BorderLayout.NORTH);
        colorPanelWrapper.add(getImageEditor().getColorPanel(), BorderLayout.SOUTH);

        freeHandPanel.add(colorPanelWrapper, BorderLayout.EAST);

        loadDrawingTabs();

        freeHandPanel.add(getImageEditor().getDrawingPanels(), BorderLayout.CENTER);
        freeHandPanel.add(getImageEditor().getControlPanel(), BorderLayout.WEST);

        getImageEditor().enableControls();

        return freeHandPanel;
    }


    public void loadDrawingTabs() {
        getImageEditor().setScrollPane(null);
        getImageEditor().getDrawingPanels().removeAll();
        JScrollPane scrollPane = new JScrollPane(getImageEditor());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        getImageEditor().getDrawingPanels().addTab("1", scrollPane);

        for (String strAdditionalFile : getRegion().additionalImageFile) {
            int index = getImageEditor().getDrawingPanels().getTabCount();
            getImageEditor().getDrawingPanels().add("" + (index + 1), new JPanel());
        }

        getImageEditor().setScrollPane(scrollPane);
    }

    public void enableControls() {
        this.deleteMapping.setEnabled(mappingRow >= 0);
        editMapping.setEnabled(mappingRow >= 0);
        this.moveUpMapping.setEnabled(mappingRow > 0);
        this.moveDownMapping.setEnabled(mappingRow >= 0 && mappingRow < getRegion().updateTransformations.length - 1);
        this.duplicateMapping.setEnabled(mappingRow >= 0 && eventRow < getRegion().updateTransformations.length - 1);

        if (getMouseEventPanel() != null) {
            getMouseEventPanel().enableControls();
        }

        if (getRegionOverlapEventsPanel() != null) {
            getRegionOverlapEventsPanel().enableControls();
        }
    }

    public void refresh() {
        int index = getRegion().parent.getRegions().indexOf(getRegion());
        upwards.setEnabled(index > 0);
        backwards.setEnabled(index < getRegion().parent.getRegions().size() - 1);

        statusBar.setText("position (left,up) : " + getRegion().x1 + "," + getRegion().y1 + "    size : " + (getRegion().x2 - getRegion().x1) + "," + (getRegion().y2 - getRegion().y1));

        RefreshTime.update();
        SketchletEditor.getInstance().repaint();
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
            SketchletEditor.getInstance().saveRegionUndo();
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
                    getTableUpdateTransformations(),
                    modelUpdateTransformations);
        }
    }

    int rowVariables = -1;


    public void populateComboBoxes() {
        UIUtils.populateVariablesCombo(this.getImageUrlField(), true);
        UIUtils.populateVariablesCombo(this.getImageIndex(), true);
        UIUtils.populateVariablesCombo(this.getAnimationMs(), true);
        UIUtils.populateVariablesCombo(this.getCaptureScreenX(), true);
        UIUtils.populateVariablesCombo(this.getCaptureScreenY(), true);
        UIUtils.populateVariablesCombo(this.getCaptureScreenWidth(), true);
        UIUtils.populateVariablesCombo(this.getCaptureScreenHeight(), true);

        this.getImageUrlField().setPreferredSize(new Dimension(280, 30));
        this.getImageIndex().setPreferredSize(new Dimension(85, 18));
        this.getAnimationMs().setPreferredSize(new Dimension(85, 18));

        if (generalSettingsPanel != null) {
            generalSettingsPanel.populateComboBoxes();
        }
    }

    public void refreshComponents() {
        UIUtils.refreshComboBox(this.getImageUrlField(), getRegion().imageUrlField);
        UIUtils.refreshComboBox(this.getImageIndex(), getRegion().strImageIndex);
        UIUtils.refreshComboBox(this.getAnimationMs(), getRegion().strAnimationMs);
        UIUtils.refreshComboBox(this.getCaptureScreenX(), getRegion().captureScreenX);
        UIUtils.refreshComboBox(this.getCaptureScreenY(), getRegion().captureScreenY);
        UIUtils.refreshComboBox(this.getCaptureScreenWidth(), getRegion().captureScreenWidth);
        UIUtils.refreshComboBox(this.getCaptureScreenHeight(), getRegion().captureScreenHeight);
        UIUtils.refreshComboBox(this.shapePanel.fillColor, getRegion().strFillColor);
        UIUtils.refreshComboBox(this.shapePanel.lineColor, getRegion().lineColor);
        UIUtils.refreshComboBox(this.shapePanel.lineStyle, getRegion().lineStyle);
        UIUtils.refreshComboBox(this.shapePanel.lineThickness, getRegion().lineThickness);
        UIUtils.refreshComboBox(this.shapePanel.shapeList, getRegion().shape);
        this.shapePanel.shapeArguments.setText(getRegion().shapeArguments);

        if (this.widgetPanel != null) {
            this.widgetPanel.refreshComponents();
        }
        if (this.getMouseEventPanel() != null) {
            this.getMouseEventPanel().refresh();
        }
        if (this.getRegionOverlapEventsPanel() != null) {
            this.getRegionOverlapEventsPanel().refresh();
        }
        if (generalSettingsPanel != null) {
            generalSettingsPanel.refreshComponents();
        }

        this.textArea.setText(getRegion().text);
        this.trajectory1.setText(getRegion().trajectory1);
        this.trajectory2.setText(getRegion().trajectory1);

        this.charactersPerLine.setText(getRegion().charactersPerLine);
        this.maxNumLines.setText(getRegion().maxNumLines);

        UIUtils.refreshCheckBox(canMove, getRegion().movable);
        UIUtils.refreshCheckBox(canRotate, getRegion().rotatable);
        UIUtils.refreshCheckBox(canResize, getRegion().resizable);
        UIUtils.refreshCheckBox(wrapText, getRegion().textWrapped);
        UIUtils.refreshCheckBox(trimText, getRegion().textTrimmed);
        UIUtils.refreshCheckBox(walkThrough, getRegion().walkThroughEnabled);
        UIUtils.refreshCheckBox(stickToTrajectory, getRegion().stickToTrajectoryEnabled);
        UIUtils.refreshCheckBox(orientationTrajectory, getRegion().changingOrientationOnTrajectoryEnabled);
        UIUtils.refreshCheckBox(captureScreen, getRegion().screenCapturingEnabled);
        UIUtils.refreshCheckBox(captureScreenMouseMap, getRegion().screenCapturingMouseMappingEnabled);

        UIUtils.refreshTable(setPanel.table);
        UIUtils.refreshTable(tableAnimation);
        UIUtils.refreshTable(getTableUpdateTransformations());
        UIUtils.refreshTable(getTableLimits());
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

    public JTabbedPane getTabs() {
        return tabs;
    }

    public void setTabs(JTabbedPane tabs) {
        this.tabs = tabs;
    }

    public JTabbedPane getTabsRegionEvents() {
        return tabsRegionEvents;
    }

    public void setTabsRegionEvents(JTabbedPane tabsRegionEvents) {
        this.tabsRegionEvents = tabsRegionEvents;
    }

    public JTabbedPane getTabsImage() {
        return tabsImage;
    }

    public void setTabsImage(JTabbedPane tabsImage) {
        this.tabsImage = tabsImage;
    }

    public JTabbedPane getTabsTransform() {
        return tabsTransform;
    }

    public void setTabsTransform(JTabbedPane tabsTransform) {
        this.tabsTransform = tabsTransform;
    }

    public JTabbedPane getTabsMove() {
        return tabsMove;
    }

    public void setTabsMove(JTabbedPane tabsMove) {
        this.tabsMove = tabsMove;
    }

    public JTable getTableLimits() {
        return tableLimits;
    }

    public void setTableLimits(JTable tableLimits) {
        this.tableLimits = tableLimits;
    }

    public JTable getTableUpdateTransformations() {
        return tableUpdateTransformations;
    }

    public void setTableUpdateTransformations(JTable tableUpdateTransformations) {
        this.tableUpdateTransformations = tableUpdateTransformations;
    }

    public ActiveRegion getRegion() {
        return region;
    }

    public void setRegion(ActiveRegion region) {
        this.region = region;
    }

    public JComboBox getImageUrlField() {
        return imageUrlField;
    }

    public void setImageUrlField(JComboBox imageUrlField) {
        this.imageUrlField = imageUrlField;
    }

    public JComboBox getImageIndex() {
        return imageIndex;
    }

    public void setImageIndex(JComboBox imageIndex) {
        this.imageIndex = imageIndex;
    }

    public JComboBox getAnimationMs() {
        return animationMs;
    }

    public void setAnimationMs(JComboBox animationMs) {
        this.animationMs = animationMs;
    }

    public JComboBox getCaptureScreenX() {
        return captureScreenX;
    }

    public void setCaptureScreenX(JComboBox captureScreenX) {
        this.captureScreenX = captureScreenX;
    }

    public JComboBox getCaptureScreenY() {
        return captureScreenY;
    }

    public void setCaptureScreenY(JComboBox captureScreenY) {
        this.captureScreenY = captureScreenY;
    }

    public JComboBox getCaptureScreenWidth() {
        return captureScreenWidth;
    }

    public void setCaptureScreenWidth(JComboBox captureScreenWidth) {
        this.captureScreenWidth = captureScreenWidth;
    }

    public JComboBox getCaptureScreenHeight() {
        return captureScreenHeight;
    }

    public void setCaptureScreenHeight(JComboBox captureScreenHeight) {
        this.captureScreenHeight = captureScreenHeight;
    }

    public ActiveRegionImageEditor getImageEditor() {
        return imageEditor;
    }

    public void setImageEditor(ActiveRegionImageEditor imageEditor) {
        this.imageEditor = imageEditor;
    }

    public KeyboardEventsPanel getKeyboardEventsPanel() {
        return keyboardEventsPanel;
    }

    public void setKeyboardEventsPanel(KeyboardEventsPanel keyboardEventsPanel) {
        this.keyboardEventsPanel = keyboardEventsPanel;
    }

    public MouseEventsPanel getMouseEventPanel() {
        return mouseEventPanel;
    }

    public void setMouseEventPanel(MouseEventsPanel mouseEventPanel) {
        this.mouseEventPanel = mouseEventPanel;
    }

    public RegionOverlapEventsPanel getRegionOverlapEventsPanel() {
        return regionOverlapEventsPanel;
    }

    public void setRegionOverlapEventsPanel(RegionOverlapEventsPanel regionOverlapEventsPanel) {
        this.regionOverlapEventsPanel = regionOverlapEventsPanel;
    }
}
