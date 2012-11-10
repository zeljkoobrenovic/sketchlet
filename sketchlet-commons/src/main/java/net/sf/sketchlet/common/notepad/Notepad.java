package net.sf.sketchlet.common.notepad;

import net.sf.sketchlet.common.file.FileUtils;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Segment;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Sample application using the simple text editor component that
 * supports only one font.
 *
 * @author Timothy Prinzing
 * @version 1.30 07/26/04
 */
public class Notepad extends JPanel {
    private static final Logger log = Logger.getLogger(Notepad.class);
    private SyntaxPanel editor;
    private Hashtable commands;
    private JMenuBar menubar;
    private JComponent status;
    private JFrame elementTreeFrame;
    private static ResourceBundle resources;
    private final static String EXIT_AFTER_PAINT = new String("-exit");
    private static boolean exitAfterFirstPaint;

    static {
        try {
            resources = ResourceBundle.getBundle("net.sf.sketchlet.workspace.resources.Notepad", Locale.getDefault());
        } catch (MissingResourceException mre) {
            log.error("resources/Notepad.properties not found", mre);
        }
    }

    public void paintChildren(Graphics g) {
        super.paintChildren(g);
    }

    File file = null;

    public void openFile(String strPath) {
        Frame frame = getFrame();
        this.file = new File(strPath);
        frame.setTitle(this.file.getName());
        editor.setText(FileUtils.getFileText(file.getPath()));

        if (strPath.endsWith("js")) {
            editor.jCmbLangs.setSelectedItem("text/javascript");
        } else if (strPath.endsWith("java")) {
            editor.jCmbLangs.setSelectedItem("text/java");
        } else if (strPath.endsWith("py")) {
            editor.jCmbLangs.setSelectedItem("text/python");
        } else if (strPath.endsWith(".xsl") || strPath.endsWith(".xslt")) {
            editor.jCmbLangs.setSelectedItem("text/xml");
        } else if (strPath.endsWith(".bsh")) {
            editor.jCmbLangs.setSelectedItem("text/java");
        } else if (strPath.endsWith(".groovy")) {
            editor.jCmbLangs.setSelectedItem("text/groovy");
        } else if (strPath.endsWith(".c")) {
            editor.jCmbLangs.setSelectedItem("text/c");
        } else if (strPath.endsWith(".cpp")) {
            editor.jCmbLangs.setSelectedItem("text/cpp");
        } else if (strPath.endsWith(".cs")) {
            editor.jCmbLangs.setSelectedItem("text/cpp");
        } else if (strPath.endsWith(".h")) {
            editor.jCmbLangs.setSelectedItem("text/cpp");
        } else if (strPath.endsWith(".tcl")) {
        } else if (strPath.endsWith(".rb")) {
            editor.jCmbLangs.setSelectedItem("text/ruby");
        } else if (strPath.endsWith(".sl")) {
        } else if (strPath.endsWith(".jsl")) {
        } else if (strPath.endsWith(".pl")) {
        } else if (strPath.endsWith(".awk")) {
        } else if (strPath.endsWith(".ams")) {
        } else if (strPath.endsWith(".html")) {
            editor.jCmbLangs.setSelectedItem("text/xhtml");
        } else if (strPath.endsWith(".htm")) {
            editor.jCmbLangs.setSelectedItem("text/xhtml");
        } else if (strPath.endsWith(".jsp")) {
            editor.jCmbLangs.setSelectedItem("text/xhtml");
        } else if (strPath.endsWith(".asp")) {
            editor.jCmbLangs.setSelectedItem("text/xhtml");
        } else {
            editor.jCmbLangs.setSelectedItem("text/xml");
        }
    }

    public static void openNotepad(File file, WindowListener listener) {
        openNotepad(file.getPath(), listener);
    }

    public static void openNotepad(String filename, WindowListener listener) {
        final JFrame frame = new JFrame();
        frame.setTitle("Configuration");
        frame.setBackground(Color.lightGray);
        frame.getContentPane().setLayout(new BorderLayout());
        final Notepad notepad = new Notepad();
        frame.getContentPane().add("Center", notepad);
        frame.setJMenuBar(notepad.createMenubar());
        frame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                saveDocument(notepad);
            }
        });

        if (listener != null) {
            frame.addWindowListener(listener);
        }

        frame.pack();
        frame.setSize(600, 600);
        notepad.openFile(filename);

        frame.setVisible(true);
    }

    public static void saveDocument(Notepad notepad) {
        File f = notepad.file;
        if (f == null) {
            JFileChooser chooser = new JFileChooser();
            int ret = chooser.showSaveDialog(notepad.getFrame());

            if (ret != JFileChooser.APPROVE_OPTION) {
                return;
            }

            f = chooser.getSelectedFile();
            notepad.getFrame().setTitle(f.getName());
        }

        FileUtils.saveFileText(f.getPath(), notepad.editor.getText());
    }

    Notepad() {
        super(true);

        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new BorderLayout());

        // create the embedded JTextComponent
        editor = new SyntaxPanel();

        // install the command table
        commands = new Hashtable();

        JScrollPane scroller = new JScrollPane();
        JViewport port = scroller.getViewport();
        port.add(editor);
        try {
            String vpFlag = resources == null ? "true" : resources.getString("ViewportBackingStore");
            Boolean bs = Boolean.valueOf(vpFlag);
        } catch (MissingResourceException mre) {
            // just use the viewport default
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add("Center", scroller);
        add("Center", panel);
        add("South", createStatusbar());
    }

    public static void main(String[] args) {
        try {
            String vers = System.getProperty("java.version");
            if (vers.compareTo("1.1.2") < 0) {
                log.warn("!!!WARNING: Swing must be run with a 1.1.2 or higher version VM!!!");
            }
            if (args.length > 0 && args[0].equals(EXIT_AFTER_PAINT)) {
                exitAfterFirstPaint = true;
            }
            JFrame frame = new JFrame();
            frame.setTitle(resources == null ? "Title" : resources.getString("Title"));
            frame.setBackground(Color.lightGray);
            frame.getContentPane().setLayout(new BorderLayout());
            Notepad notepad = new Notepad();
            frame.getContentPane().add("Center", notepad);
            frame.setJMenuBar(notepad.createMenubar());
            frame.pack();
            frame.setSize(500, 600);
            frame.setVisible(true);
        } catch (Throwable t) {
            log.error("uncaught exception", t);
        }
    }

    /**
     * Find the hosting frame, for the file-chooser dialog.
     */
    protected Frame getFrame() {
        for (Container p = getParent(); p != null; p = p.getParent()) {
            if (p instanceof Frame) {
                return (Frame) p;
            }
        }
        return null;
    }

    protected Action getAction(String cmd) {
        return (Action) commands.get(cmd);
    }

    protected String getResourceString(String nm) {
        String str;
        try {
            str = resources == null ? nm : resources.getString(nm);
        } catch (MissingResourceException mre) {
            str = null;
        }
        return str;
    }

    protected JMenuBar getMenubar() {
        return menubar;
    }

    /**
     * Create a status bar
     */
    protected Component createStatusbar() {
        // need to do something reasonable here
        status = new StatusBar();
        return status;
    }

    /**
     * Resets the undo manager.
     */
    protected void resetUndoManager() {
        undo.discardAllEdits();
        undoAction.update();
        redoAction.update();
    }

    /**
     * Create the menubar for the app.  By default this pulls the
     * definition of the menu from the associated resource file.
     */
    protected JMenuBar createMenubar() {
        JMenuItem mi;
        JMenuBar mb = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem save = new JMenuItem("Save");
        JMenuItem exit = new JMenuItem("Exit");

        save.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                FileUtils.saveFileText(file.getPath(), editor.getText());
            }
        });
        exit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                FileUtils.saveFileText(file.getPath(), editor.getText());
                getFrame().setVisible(false);
            }
        });

        fileMenu.add(save);
        fileMenu.addSeparator();
        fileMenu.add(exit);

        mb.add(fileMenu);

        this.menubar = mb;
        return mb;
    }

    // Yarked from JMenu, ideally this would be public.
    protected PropertyChangeListener createActionChangeListener(JMenuItem b) {
        return new ActionChangedListener(b);
    }

    // Yarked from JMenu, ideally this would be public.
    private class ActionChangedListener implements PropertyChangeListener {

        JMenuItem menuItem;

        ActionChangedListener(JMenuItem mi) {
            super();
            this.menuItem = mi;
        }

        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if (e.getPropertyName().equals(Action.NAME)) {
                String text = (String) e.getNewValue();
                menuItem.setText(text);
            } else if (propertyName.equals("enabled")) {
                Boolean enabledState = (Boolean) e.getNewValue();
                menuItem.setEnabled(enabledState.booleanValue());
            }
        }
    }

    /**
     * Listener for the edits on the current document.
     */
    protected UndoableEditListener undoHandler = new UndoHandler();
    /**
     * UndoManager that we add edits to.
     */
    protected UndoManager undo = new UndoManager();
    /**
     * Suffix applied to the key used in resource file
     * lookups for an image.
     */
    public static final String imageSuffix = "Image";
    /**
     * Suffix applied to the key used in resource file
     * lookups for a label.
     */
    public static final String labelSuffix = "Label";
    /**
     * Suffix applied to the key used in resource file
     * lookups for an action.
     */
    public static final String actionSuffix = "Action";
    /**
     * Suffix applied to the key used in resource file
     * lookups for tooltip text.
     */
    public static final String openAction = "open";
    public static final String newAction = "new";
    public static final String saveAction = "save";
    public static final String exitAction = "exit";
    public static final String showElementTreeAction = "showElementTree";

    class UndoHandler implements UndoableEditListener {

        /**
         * Messaged when the Document has created an edit, the edit is
         * added to <code>undo</code>, an instance of UndoManager.
         */
        public void undoableEditHappened(UndoableEditEvent e) {
            undo.addEdit(e.getEdit());
            undoAction.update();
            redoAction.update();
        }
    }

    /**
     * FIXME - I'm not very useful yet
     */
    class StatusBar extends JComponent {

        public StatusBar() {
            super();
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        }

        public void paint(Graphics g) {
            super.paint(g);
        }
    }

    // --- action implementations -----------------------------------
    private UndoAction undoAction = new UndoAction();
    private RedoAction redoAction = new RedoAction();
    /**
     * Actions defined by the Notepad class
     */
    private Action[] defaultActions = {
            new NewAction(),
            new OpenAction(),
            new SaveAction(),
            new ExitAction(),
            new ShowElementTreeAction(),
            undoAction,
            redoAction
    };

    class UndoAction extends AbstractAction {

        public UndoAction() {
            super("Undo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                undo.undo();
            } catch (CannotUndoException ex) {
                log.error("Unable to undo: " + ex);
                ex.printStackTrace();
            }
            update();
            redoAction.update();
        }

        protected void update() {
            if (undo.canUndo()) {
                setEnabled(true);
                putValue(Action.NAME, undo.getUndoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Undo");
            }
        }
    }

    class RedoAction extends AbstractAction {

        public RedoAction() {
            super("Redo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                undo.redo();
            } catch (CannotRedoException ex) {
                log.error("Unable to redo: " + ex);
                ex.printStackTrace();
            }
            update();
            undoAction.update();
        }

        protected void update() {
            if (undo.canRedo()) {
                setEnabled(true);
                putValue(Action.NAME, undo.getRedoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Redo");
            }
        }
    }

    class OpenAction extends NewAction {

        OpenAction() {
            super(openAction);
        }

        public void actionPerformed(ActionEvent e) {
            Frame frame = getFrame();
            JFileChooser chooser = new JFileChooser();
            int ret = chooser.showOpenDialog(frame);

            if (ret != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File f = chooser.getSelectedFile();
            if (f.isFile() && f.canRead()) {
                frame.setTitle(f.getName());
                editor.setText(FileUtils.getFileText(f.getPath()));
            } else {
                JOptionPane.showMessageDialog(getFrame(),
                        "Could not open file: " + f,
                        "Error opening file",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    class SaveAction extends AbstractAction {

        SaveAction() {
            super(saveAction);
        }

        public void actionPerformed(ActionEvent e) {
            File f = file;
            if (f == null) {
                Frame frame = getFrame();
                JFileChooser chooser = new JFileChooser();
                int ret = chooser.showSaveDialog(frame);

                if (ret != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                f = chooser.getSelectedFile();
                frame.setTitle(f.getName());
            }

            FileUtils.saveFileText(f.getPath(), editor.getText());
        }
    }

    class NewAction extends AbstractAction {

        NewAction() {
            super(newAction);
        }

        NewAction(String nm) {
            super(nm);
        }

        public void actionPerformed(ActionEvent e) {
            editor.setText("");
            resetUndoManager();
            getFrame().setTitle(resources.getString("Title"));
            revalidate();
        }
    }

    Notepad parent = this;

    /**
     * Really lame implementation of an exit command
     */
    class ExitAction extends AbstractAction {

        ExitAction() {
            super(exitAction);
        }

        public void actionPerformed(ActionEvent e) {
            saveDocument(parent);
            parent.getFrame().setVisible(false);
        }
    }

    /**
     * Action that brings up a JFrame with a JTree showing the structure
     * of the document.
     */
    class ShowElementTreeAction extends AbstractAction {

        ShowElementTreeAction() {
            super(showElementTreeAction);
        }

        ShowElementTreeAction(String nm) {
            super(nm);
        }

        public void actionPerformed(ActionEvent e) {
            if (elementTreeFrame == null) {
                // Create a frame containing an instance of
                // ElementTreePanel.
                try {
                    String title = resources.getString("ElementTreeFrameTitle");
                    elementTreeFrame = new JFrame(title);
                } catch (MissingResourceException mre) {
                    elementTreeFrame = new JFrame();
                }

                elementTreeFrame.addWindowListener(new WindowAdapter() {

                    public void windowClosing(WindowEvent weeee) {
                        elementTreeFrame.setVisible(false);
                    }
                });
                Container fContentPane = elementTreeFrame.getContentPane();

                fContentPane.setLayout(new BorderLayout());
            }
            elementTreeFrame.setVisible(true);
        }
    }

    /**
     * Thread to load a file into the text storage model
     */
    class FileLoader extends Thread {

        FileLoader(File f, Document doc) {
            setPriority(4);
            this.f = f;
            this.doc = doc;
        }

        public void run() {
            try {
                // initialize the statusbar
                status.removeAll();
                JProgressBar progress = new JProgressBar();
                progress.setMinimum(0);
                progress.setMaximum((int) f.length());
                status.add(progress);
                status.revalidate();

                // try to start reading
                Reader in = new FileReader(f);
                char[] buff = new char[4096];
                int nch;
                while ((nch = in.read(buff, 0, buff.length)) != -1) {
                    doc.insertString(doc.getLength(), new String(buff, 0, nch), null);
                    progress.setValue(progress.getValue() + nch);
                }
            } catch (IOException e) {
                final String msg = e.getMessage();
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        JOptionPane.showMessageDialog(getFrame(),
                                "Could not open file: " + msg,
                                "Error opening file",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (BadLocationException e) {
                System.err.println(e.getMessage());
            }
            doc.addUndoableEditListener(undoHandler);
            // we are done... get rid of progressbar
            status.removeAll();
            status.revalidate();

            resetUndoManager();
        }

        Document doc;
        File f;
    }

    /**
     * Thread to save a document to file
     */
    class FileSaver extends Thread {

        Document doc;
        File f;

        FileSaver(File f, Document doc) {
            setPriority(4);
            this.f = f;
            this.doc = doc;
        }

        public void run() {
            try {
                // initialize the statusbar
                status.removeAll();
                JProgressBar progress = new JProgressBar();
                progress.setMinimum(0);
                progress.setMaximum((int) doc.getLength());
                status.add(progress);
                status.revalidate();

                // start writing
                Writer out = new FileWriter(f);
                Segment text = new Segment();
                text.setPartialReturn(true);
                int charsLeft = doc.getLength();
                int offset = 0;
                while (charsLeft > 0) {
                    doc.getText(offset, Math.min(4096, charsLeft), text);
                    out.write(text.array, text.offset, text.count);
                    charsLeft -= text.count;
                    offset += text.count;
                    progress.setValue(offset);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                out.flush();
                out.close();
            } catch (IOException e) {
                final String msg = e.getMessage();
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        JOptionPane.showMessageDialog(getFrame(),
                                "Could not save file: " + msg,
                                "Error saving file",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (BadLocationException e) {
                System.err.println(e.getMessage());
            }
            // we are done... get rid of progressbar
            status.removeAll();
            status.revalidate();
        }
    }
}
