package net.sf.sketchlet.model.programming.screenscripts;

// Capture.java

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

/**
 * This class defines the application GUI and starts the application.
 */
public class CaptureFrame extends JFrame {

    private Dimension dimScreenSize;
    private ImageArea imageArea;
    private Rectangle rectScreenSize;
    private JScrollPane jsp;
    private ActionListTable actionTable;
    private ConditionTable conditionTable;
    private String path = SketchletContextUtils.getCurrentProjectDir() + SketchletContextUtils.sketchletDataDir() + File.separator + "temp" + File.separator;
    private File scriptFile = null;
    private static CaptureFrame captureFrame = null;
    private JTextField name = new JTextField(22);

    public static void openCaptureFrame(File scriptFile, String scriptName) {
        if (getCaptureFrame() == null) {
            setCaptureFrame(new CaptureFrame(scriptFile, scriptName));
            getCaptureFrame().setVisible(true);
        } else {
            if (getCaptureFrame().scriptFile.exists()) {
                getCaptureFrame().saveScreenScript();
            }
            getCaptureFrame().scriptFile = scriptFile;
            getCaptureFrame().setTitle(scriptName);
            getCaptureFrame().name.setText(scriptName);
            getCaptureFrame().loadScreenScript();
            if (!getCaptureFrame().isVisible()) {
                getCaptureFrame().setVisible(true);
            }
        }
    }

    static JFileChooser fcSave = new JFileChooser();

    /**
     * Construct a Capture GUI.
     *
     * @param title text appearing in the title bar of Capture's main window
     */
    private CaptureFrame(File scriptFile, String scriptName) {
        // Place title in the title bar of Capture's main window.
        super(scriptName);
        this.setIconImage(Workspace.createImageIcon("resources/mouse.png").getImage());
        this.scriptFile = scriptFile;

        // Exit the application if user selects Close from system menu.

        // setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Save screen dimensions for initially positioning main window and
        // performing screen captures.

        dimScreenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // Copy screen dimensions to Rectangle for use with Robot's
        // createScreenCapture() method.

        rectScreenSize = new Rectangle(dimScreenSize);

        // Construct a save file chooser. Initialize the starting directory to
        // the current directory, do not allow the user to select the "all files"
        // filter, and restrict the files that can be selected to those ending
        // with .png extensions.

        fcSave.setCurrentDirectory(new File(System.getProperty("user.dir")));
        fcSave.setAcceptAllFileFilterUsed(false);
        fcSave.setFileFilter(new ImageFileFilter());

        // Create the application's menus.

        JMenuBar mb = new JMenuBar();

        JMenu menu = new JMenu(Language.translate("File"));

        JTabbedPane tabs = new JTabbedPane();

        actionTable = new ActionListTable(this);
        conditionTable = new ConditionTable(this);

        tabs.add(actionTable, Language.translate("Actions"));
        // tabs.add(conditionTable, "Conditions");

        setImageArea(new ImageArea(this, actionTable));

        ActionListener al1, al2, al3;

        al1 = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                saveScreenScript();
                saveLastScreen();
                close();
                setVisible(false);
            }
        };

        al2 = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // Hide Capture's main window so that it does not appear in
                // the screen capture.

                setVisible(false);

                try {
                    Thread.sleep(1000);
                } catch (Exception et) {
                }

                // Perform the screen capture.

                BufferedImage biScreen;
                biScreen = AWTRobotUtil.getRobot().createScreenCapture(rectScreenSize);

                // Show Capture's main window for continued user interaction.

                setVisible(true);

                // Update ImageArea component with the new bahabahaImages, and adjust
                // the scrollbars.

                getImageArea().setImage(biScreen);

                jsp.getHorizontalScrollBar().setValue(0);
                jsp.getVerticalScrollBar().setValue(0);
            }
        };

        al3 = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                BufferedImage biScreen;

                Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable transferable = clip.getContents(null);
                RenderedImage img = null;

                if (transferable.isDataFlavorSupported(new DataFlavor("image/x-java-image; class=java.awt.Image", "Image"))) {
                    try {
                        img = (RenderedImage) transferable.getTransferData(new DataFlavor("image/x-java-image; class=java.awt.Image", "Image"));
                        biScreen = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
                        Graphics2D g2 = biScreen.createGraphics();
                        g2.setColor(Color.WHITE);
                        g2.fillRect(0, 0, img.getWidth(), img.getHeight());
                        g2.drawRenderedImage(img, null);
                        g2.dispose();

                        getImageArea().setImage(biScreen);

                        jsp.getHorizontalScrollBar().setValue(0);
                        jsp.getVerticalScrollBar().setValue(0);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(jsp = new JScrollPane(getImageArea()), BorderLayout.CENTER);

        getContentPane().add(panel);

        int width = (int) (dimScreenSize.width * 0.7);
        int height = (int) (dimScreenSize.height * 0.7);

        setSize(width, height);

        if (SketchletEditor.editorFrame == null) {
            setLocation((width - width / 2) / 2, (height - height / 2) / 2);
        } else {
            this.setLocationRelativeTo(SketchletEditor.editorFrame);
        }

        JPanel panelButtons = new JPanel();
        panelButtons.setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton btnSave = new JButton(Language.translate("Save"), Workspace.createImageIcon("resources/ok.png"));
        btnSave.addActionListener(al1);
        JButton btnCapture = new JButton(Language.translate("Capture Screen"), Workspace.createImageIcon("resources/screen.png"));
        btnCapture.addActionListener(al2);
        JButton btnPaste = new JButton(Language.translate("Paste Image"), Workspace.createImageIcon("resources/edit-paste.png"));
        btnPaste.addActionListener(al3);

        panelButtons.add(btnSave);
        panelButtons.add(btnCapture);
        panelButtons.add(btnPaste);
        panelButtons.add(new JLabel(Language.translate(" name ")));
        panelButtons.add(name);

        this.getContentPane().add(panelButtons, BorderLayout.NORTH);
        panel.add(tabs, BorderLayout.NORTH);

        loadScreenScript();

        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent weeee) {
                close();
            }
        });

    }

    public static CaptureFrame getCaptureFrame() {
        return captureFrame;
    }

    public static void setCaptureFrame(CaptureFrame captureFrame) {
        CaptureFrame.captureFrame = captureFrame;
    }

    public void close() {
        saveScreenScript();
        saveLastScreen();
        setCaptureFrame(null);
        if (ScreenScripts.getScreenScriptsPanel() != null) {
            ScreenScripts.getScreenScriptsPanel().loadScreenScripts();
        }
        ScreenScripts.setPublicScriptRunner(new ScreenScriptRunner(ScreenScripts.getScreenScriptsPanel().getScriptDir()));
        ScreenScripts.getPublicScriptRunner().loadScripts();
    }

    public void loadScreenScript() {
        ScreenScriptInfo s = new ScreenScriptInfo();
        ScreenScriptRunner.loadScreenScript(s, this.scriptFile, conditionTable.getConditions(), actionTable.getActions());
        conditionTable.getTableModel().fireTableDataChanged();
        actionTable.getTableModel().fireTableDataChanged();
        this.name.setText(scriptFile.getName().replace(".txt", ""));
        if (s.isWhenAllConditions()) {
            this.conditionTable.getAllConditionsRadioButton().setSelected(true);
        } else {
            this.conditionTable.getAnyConditionRadioButton().setSelected(true);
        }
    }

    boolean saving = false;

    public void saveScreenScript() {
        if (saving) {
            return;
        }

        saving = true;
        try {
            if (!scriptFile.getName().equals(name.getText() + ".txt")) {
                String oldName = "Screen:" + scriptFile.getName().replace(".txt", "");
                String newName = "Screen:" + name.getText();
                ScreenScripts.getScreenScriptsPanel().getScreenScripts().elementAt(ScreenScripts.getScreenScriptsPanel().getCurrentRow()).setName(name.getText());
                scriptFile.renameTo(new File(scriptFile.getParent() + File.separator + (String) name.getText() + ".txt"));
                ScreenScripts.getScreenScriptsPanel().getScreenScripts().elementAt(ScreenScripts.getScreenScriptsPanel().getCurrentRow()).setFile(scriptFile);
                ScreenScripts.getScreenScriptsPanel().loadScreenScripts(scriptFile);
                scriptFile = new File(scriptFile.getParent(), name.getText() + ".txt");
                SketchletEditor.getPages().replaceReferencesMacros(oldName, newName);
                // System.gc();
            }

            PrintWriter out = new PrintWriter(new FileWriter(scriptFile));

            out.println("WhenAllConditions " + this.conditionTable.getAllConditionsRadioButton().isSelected());

            for (Condition c : conditionTable.getConditions()) {
                out.println("AddCondition");
                out.println("Variable " + c.getVariable());
                out.println("Operator " + c.getOperator());
                out.println("Value " + c.getValue());
                out.println();
            }

            for (RobotAction a : actionTable.getActions()) {
                out.println("AddAction " + a.name);
                out.println("Parameters " + a.parameters);
                out.println();
            }

            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        saving = false;
    }

    public void saveLastScreen() {
        if (getImageArea().getImage() == null) {
            return;
        }

        new File(path).mkdirs();
        File file = new File(path + "last_screen.png");
        file.deleteOnExit();
        ImageWriter writer = null;
        ImageOutputStream ios = null;

        try {
            Iterator iter;
            iter = ImageIO.getImageWritersByFormatName("png");

            if (!iter.hasNext()) {
                showError("Unable to save image to png file type.");
                return;
            }

            writer = (ImageWriter) iter.next();

            ios = ImageIO.createImageOutputStream(file);
            writer.setOutput(ios);

            ImageWriteParam iwp = writer.getDefaultWriteParam();

            writer.write(null, new IIOImage((BufferedImage) getImageArea().getImage(), null, null), iwp);
        } catch (IOException e2) {
            showError(e2.getMessage());
        } finally {
            try {
                if (ios != null) {
                    ios.flush();
                    ios.close();
                }
                if (writer != null) {
                    writer.dispose();
                }
            } catch (IOException e2) {
            }
        }

    }

    /**
     * Present an error message via a dialog box.
     *
     * @param message the message to be presented
     */
    public static void showError(String message) {
        JOptionPane.showMessageDialog(null, message, Language.translate("Capture"), JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Application entry point.
     *
     * @param args array of command-line arguments
     */
    public static void main(String[] args) {
        new CaptureFrame(null, "Capture");
    }

    public ImageArea getImageArea() {
        return imageArea;
    }

    public void setImageArea(ImageArea imageArea) {
        this.imageArea = imageArea;
    }
}
