package net.sf.sketchlet.designer.editor.ui.desktop;

import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.context.VariableUpdateListener;
import net.sf.sketchlet.context.VariablesBlackboardContext;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.model.PageVariable;
import net.sf.sketchlet.framework.model.Page;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.TableCellEditor;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Vector;
import java.util.concurrent.Executors;

public class Notepad extends JPanel {
    private static final Logger log = Logger.getLogger(Notepad.class);

    private File file = null;

    public void paintChildren(Graphics g) {
        super.paintChildren(g);
    }

    public void openFile(String strPath) {
        Frame frame = getFrame();
        this.file = new File(strPath);
        frame.setTitle(this.file.getName());
        editor.setText(FileUtils.getFileText(file.getPath()));
        editor.setCaretPosition(0);
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

    public static Notepad openNotepadFromString(String strText, String caption, String type) {
        final JFrame frame = new JFrame();
        frame.setTitle(caption);
        frame.setBackground(Color.LIGHT_GRAY);
        frame.getContentPane().setLayout(new BorderLayout());
        final Notepad notepad = new Notepad();
        notepad.editor.setSyntaxEditingStyle(type);
        notepad.editor.setText(strText);
        notepad.editor.setCaretPosition(0);
        frame.getContentPane().add("Center", notepad);
        frame.setJMenuBar(notepad.createMenubar());
        JToolBar toolbar = new JToolBar();
        JButton save = new JButton("save");
        save.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (notepad.onSave != null) {
                    notepad.onSave.run();
                }
            }
        });
        toolbar.add(save);
        JButton saveClose = new JButton("save & close");
        saveClose.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (notepad.onSave != null) {
                    notepad.onSave.run();
                }
                frame.setVisible(false);
            }
        });
        toolbar.add(saveClose);
        JButton close = new JButton("exit without saving");
        close.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                frame.setVisible(false);
            }
        });
        toolbar.add(close);
        frame.pack();
        frame.setSize(600, 700);
        frame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent we) {
                if (notepad.onSave != null) {
                    notepad.onSave.run();
                }
            }
        });

        frame.getContentPane().add("North", toolbar);
        frame.setVisible(true);

        return notepad;
    }

    public static Notepad openNotepadFromFile(final File file, String caption, String type) {
        final JFrame frame = new JFrame();
        frame.setTitle(caption);
        frame.setBackground(Color.LIGHT_GRAY);
        frame.getContentPane().setLayout(new BorderLayout());
        final Notepad notepad = new Notepad();
        notepad.editor.setSyntaxEditingStyle(type);
        notepad.editor.setText(FileUtils.getFileText(file));
        notepad.editor.setCaretPosition(0);
        frame.getContentPane().add("Center", notepad);
        frame.setJMenuBar(notepad.createMenubar());
        JToolBar toolbar = new JToolBar();
        JButton save = new JButton("save");
        save.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                FileUtils.saveFileText(file, notepad.editor.getText());
            }
        });
        toolbar.add(save);
        JButton saveClose = new JButton("save & close");
        saveClose.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                FileUtils.saveFileText(file, notepad.editor.getText());
            }
        });
        toolbar.add(saveClose);
        JButton close = new JButton("exit without saving");
        close.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                frame.setVisible(false);
            }
        });
        toolbar.add(close);
        frame.pack();
        frame.setSize(600, 700);
        frame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent we) {
                FileUtils.saveFileText(file, notepad.editor.getText());
            }
        });

        frame.getContentPane().add("North", toolbar);
        frame.setVisible(true);

        return notepad;
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

    public static JPanel getEditorPanel(final RSyntaxTextArea editor, boolean showSearchToolbar) {
        return getEditorPanel(editor, showSearchToolbar, true);
    }

    public static JPanel getEditorPanel(final RSyntaxTextArea editor, boolean showSearchToolbar, boolean bLineNumbers) {
        final JPanel panel = new JPanel(new BorderLayout());
        final JToolBar toolBar = new JToolBar();
        final JTextField searchField = new JTextField(15);
        final RTextScrollPane scrollPane = new RTextScrollPane(editor);
        scrollPane.setLineNumbersEnabled(bLineNumbers);
        JButton hideButton = new JButton("X");
        toolBar.add(hideButton);
        toolBar.add(searchField);
        final JButton nextButton = new JButton("Find Next");
        nextButton.setActionCommand("FindNext");
        toolBar.add(nextButton);
        searchField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nextButton.doClick(0);
            }
        });
        JButton prevButton = new JButton("Find Previous");
        prevButton.setActionCommand("FindPrev");
        hideButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.remove(toolBar);
                panel.revalidate();
            }
        }

        );
        final JCheckBox regexCB = new JCheckBox("Regex");
        final JCheckBox matchCaseCB = new JCheckBox("Match Case");
        final ActionListener l = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String command = e.getActionCommand();
                boolean forward = "FindNext".equals(command);

                SearchContext context = new SearchContext();
                String text = searchField.getText();
                if (text.length() == 0) {
                    return;
                }
                context.setSearchFor(text);
                context.setMatchCase(matchCaseCB.isSelected());
                context.setRegularExpression(regexCB.isSelected());
                context.setSearchForward(forward);
                context.setWholeWord(false);

                boolean found = SearchEngine.find(editor, context);
                if (!found && forward) {
                    editor.setCaretPosition(0);
                    found = SearchEngine.find(editor, context);
                }
                if (!found) {
                    JOptionPane.showMessageDialog(scrollPane, "Text not found");
                }
            }
        };
        prevButton.addActionListener(l);
        toolBar.setFloatable(false);
        toolBar.add(prevButton);
        toolBar.add(regexCB);
        toolBar.add(matchCaseCB);
        nextButton.addActionListener(l);

        editor.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_F) {
                    panel.remove(toolBar);
                    panel.add(toolBar, BorderLayout.SOUTH);
                    panel.revalidate();
                    searchField.requestFocus();
                }
            }
        });

        if (showSearchToolbar) {
            panel.add(toolBar, BorderLayout.SOUTH);
        }

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    public static RSyntaxTextArea getInstance() {
        RSyntaxTextArea editor = new RSyntaxTextArea();
        Notepad.installVariablesAutoCompletion(editor);
        Notepad.installPagePropertiesAutoCompletion(editor);
        Notepad.installRegionPropertiesAutoCompletion(editor);
        editor.setSyntaxEditingStyle(RSyntaxTextArea.SYNTAX_STYLE_JAVASCRIPT);
        editor.setCodeFoldingEnabled(false);
        return editor;
    }

    public static RSyntaxTextArea getInstance(String editingStyle) {
        RSyntaxTextArea editor = new RSyntaxTextArea();
        Notepad.installVariablesAutoCompletion(editor);
        Notepad.installPagePropertiesAutoCompletion(editor);
        Notepad.installRegionPropertiesAutoCompletion(editor);
        editor.setSyntaxEditingStyle(editingStyle);
        editor.setCodeFoldingEnabled(false);
        return editor;
    }

    static DefaultCompletionProvider variablesCompletionProvider = new DefaultCompletionProvider() {
        public boolean isAutoActivateOkay(JTextComponent tc) {
            try {
                Document doc = tc.getDocument();
                String lastTwoCharactersTyped = doc.getText(tc.getCaretPosition() - 1, 2);
                return (lastTwoCharactersTyped.equals("${"));
            } catch (BadLocationException e) {
            }
            return false;
        }
    };
    static DefaultCompletionProvider variablesCompletionProviderEquals = new DefaultCompletionProvider() {
        public boolean isAutoActivateOkay(JTextComponent tc) {
            try {
                Document doc = tc.getDocument();
                String text = doc.getText(tc.getCaretPosition(), 1);
                return (tc.getCaretPosition() == 0 && text.equals("="));
            } catch (BadLocationException e) {
            }
            return false;
        }
    };

    static {
        variablesCompletionProvider.setAutoActivationRules(false, null);
        variablesCompletionProvider.setAutoActivationRules(true, "${");
        variablesCompletionProviderEquals.setAutoActivationRules(false, null);
        variablesCompletionProviderEquals.setAutoActivationRules(true, "=");

        Executors.newCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                while (VariablesBlackboard.getInstance() == null) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                }
                VariablesBlackboardContext.getInstance().addVariableUpdateListener(new VariableUpdateListener() {
                    private List<String> addedVariables = new Vector<String>();

                    @Override
                    public void variableUpdated(String variableName, String value) {
                        if (!addedVariables.contains(variableName)) {
                            addedVariables.add(variableName);
                            updateCariablesCompletionProvider();
                        }
                    }
                });
            }
        });
    }

    public static synchronized void updateCariablesCompletionProvider() {
        variablesCompletionProvider.clear();
        variablesCompletionProviderEquals.clear();
        for (String varName : VariablesBlackboardContext.getInstance().getVariableNames()) {
            String variableDescription = VariablesBlackboardContext.getInstance().getVariableDescription(varName);
            if (StringUtils.isNotBlank(variableDescription)) {
                variablesCompletionProvider.addCompletion(new ShorthandCompletion(variablesCompletionProvider, varName, varName + "}", variableDescription));
                variablesCompletionProviderEquals.addCompletion(new ShorthandCompletion(variablesCompletionProviderEquals, varName, varName, variableDescription));
            } else {
                variablesCompletionProvider.addCompletion(new ShorthandCompletion(variablesCompletionProvider, varName, varName + "}"));
                variablesCompletionProviderEquals.addCompletion(new ShorthandCompletion(variablesCompletionProviderEquals, varName, varName, variableDescription));
            }
        }
        if (SketchletEditor.getInstance() != null && SketchletEditor.getInstance().getCurrentPage() != null) {
            int rowPosition = 1;
            for (String row[] : SketchletEditor.getInstance().getCurrentPage().getSpreadsheetData()) {
                char columnChar = 'A' - 1;
                for (String cell : row) {
                    if (StringUtils.isNotBlank(cell)) {
                        String cellId = columnChar + "" + rowPosition;
                        variablesCompletionProvider.addCompletion(new ShorthandCompletion(variablesCompletionProvider, cellId, cellId + "}"));
                        variablesCompletionProviderEquals.addCompletion(new ShorthandCompletion(variablesCompletionProviderEquals, cellId, cellId));
                    }
                    columnChar++;
                }
                rowPosition++;
            }
            for (PageVariable variable : SketchletEditor.getInstance().getCurrentPage().getPageVariables()) {
                String name = variable.getName();
                variablesCompletionProvider.addCompletion(new ShorthandCompletion(variablesCompletionProvider, name, name + "}"));
                variablesCompletionProviderEquals.addCompletion(new ShorthandCompletion(variablesCompletionProviderEquals, name, name));
            }
        }
    }

    public static void installVariablesAutoCompletion(RSyntaxTextArea editor) {
        updateCariablesCompletionProvider();
        AutoCompletion ac = new AutoCompletion(variablesCompletionProvider);
        ac.setAutoActivationDelay(180);
        ac.setAutoActivationEnabled(true);
        ac.install(editor);
        AutoCompletion acEquals = new AutoCompletion(variablesCompletionProviderEquals);
        acEquals.setAutoActivationDelay(180);
        acEquals.setAutoActivationEnabled(true);
        acEquals.install(editor);
    }


    public static void installPagePropertiesAutoCompletion(RSyntaxTextArea editor) {
        DefaultCompletionProvider provider = new DefaultCompletionProvider() {
            public boolean isAutoActivateOkay(JTextComponent tc) {
                try {
                    Document doc = tc.getDocument();
                    String lastTwoCharactersTyped = doc.getText(tc.getCaretPosition() - 4, 5);
                    return (lastTwoCharactersTyped.equals("page["));
                } catch (BadLocationException e) {
                }
                return false;
            }
        };
        provider.setAutoActivationRules(false, null);
        provider.setAutoActivationRules(true, "page[");
        for (String property[] : Page.getAllProperties()) {
            provider.addCompletion(new ShorthandCompletion(provider, property[0], "'" + property[0] + "']", property[2]));
        }


        AutoCompletion ac = new AutoCompletion(provider);
        ac.setAutoActivationDelay(180);
        ac.setAutoActivationEnabled(true);
        ac.install(editor);
    }

    public static void installRegionPropertiesAutoCompletion(RSyntaxTextArea editor) {
        DefaultCompletionProvider provider = new DefaultCompletionProvider() {
            public boolean isAutoActivateOkay(JTextComponent tc) {
                try {
                    Document doc = tc.getDocument();
                    String lastTwoCharactersTyped = doc.getText(tc.getCaretPosition() - 4, 5);
                    return (lastTwoCharactersTyped.equals("this["));
                } catch (BadLocationException e) {
                }
                return false;
            }
        };
        provider.setAutoActivationRules(false, null);
        provider.setAutoActivationRules(true, "this[");
        for (String property[] : ActiveRegion.showProperties) {
            if (property[1] != null) {
                provider.addCompletion(new ShorthandCompletion(provider, property[0], "'" + property[0] + "']", property[1]));
            }
        }
        for (String property[] : ActiveRegion.showImageProperties) {
            if (property[1] != null) {
                provider.addCompletion(new ShorthandCompletion(provider, property[0], "'" + property[0] + "']", property[1]));
            }
        }

        AutoCompletion ac = new AutoCompletion(provider);
        ac.setAutoActivationDelay(180);
        ac.setAutoActivationEnabled(true);
        ac.install(editor);
    }


    public static RSyntaxTextArea getTextField() {
        final RSyntaxTextArea component = Notepad.getInstance();
        component.setSyntaxEditingStyle(RSyntaxTextArea.SYNTAX_STYLE_XML);
        component.setMargin(new Insets(0, 0, 0, 0));
        component.addKeyListener(new KeyAdapter() {
            private String contentBeforeEnter = null;

            @Override
            public void keyReleased(KeyEvent e) {
                String text = component.getText();
                if (e.getKeyCode() == KeyEvent.VK_ENTER && text.contains("\n")) {
                    component.setText(text.replace("\n", ""));
                }
            }
        });

        return component;
    }

    public static TableCellEditor getTableCellEditor() {
        return new RSyntaxTextAreaCellEditor();
    }


    Notepad() {
        super(true);

        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new BorderLayout());

        editor = getInstance();
        editor.setAntiAliasingEnabled(true);

        commands = new Hashtable();

        try {
            String vpFlag = "true";
            Boolean bs = Boolean.valueOf(vpFlag);
        } catch (MissingResourceException mre) {
        }

        menuItems = new Hashtable();
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add("Center", getEditorPanel(editor, true));
        add("Center", panel);
        add("South", createStatusbar());
    }

    protected Frame getFrame() {
        for (Container p = getParent(); p != null; p = p.getParent()) {
            if (p instanceof Frame) {
                return (Frame) p;
            }
        }
        return null;
    }

    protected JMenuItem createMenuItem(String cmd) {
        JMenuItem mi = new JMenuItem(getResourceString(cmd + labelSuffix));
        URL url = getResource(cmd + imageSuffix);
        if (url != null) {
            mi.setHorizontalTextPosition(JButton.RIGHT);
            mi.setIcon(new ImageIcon(url));
        }
        String astr = getResourceString(cmd + actionSuffix);
        if (astr == null) {
            astr = cmd;
        }

        final Component parent = this;

        if (astr.equals("insert-path")) {
            mi.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    final JFileChooser fc = new JFileChooser();
                    fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    fc.showOpenDialog(parent);

                    File file = fc.getSelectedFile();

                    if (file != null) {
                        editor.replaceSelection(file.getPath());
                    }
                }
            });

        } else {
            mi.setActionCommand(astr);
            Action a = getAction(astr);
            if (a != null) {
                mi.addActionListener(a);
                a.addPropertyChangeListener(createActionChangeListener(mi));
                mi.setEnabled(a.isEnabled());
            } else {
                mi.setEnabled(false);
            }
        }


        menuItems.put(cmd, mi);
        return mi;
    }

    protected Action getAction(String cmd) {
        return (Action) commands.get(cmd);
    }

    protected String getResourceString(String nm) {
        String str;
        try {
            str = nm;
        } catch (MissingResourceException mre) {
            str = null;
        }
        return str;
    }

    protected URL getResource(String key) {
        String name = getResourceString(key);
        if (name != null) {
            URL url = Workspace.class.getResource(name);
            return url;
        }
        return null;
    }

    protected Component createStatusbar() {
        status = new StatusBar();
        return status;
    }

    protected void resetUndoManager() {
        undo.discardAllEdits();
        undoAction.update();
        redoAction.update();
    }

    protected JButton createToolbarButton(String key) {
        URL url = getResource(key + imageSuffix);
        JButton b = new JButton(new ImageIcon(url)) {

            public float getAlignmentY() {
                return 0.5f;
            }
        };
        b.setRequestFocusEnabled(false);
        b.setMargin(new Insets(1, 1, 1, 1));

        String astr = getResourceString(key + actionSuffix);
        if (astr == null) {
            astr = key;
        }
        Action a = getAction(astr);
        if (a != null) {
            b.setActionCommand(astr);
            b.addActionListener(a);
        } else {
            b.setEnabled(false);
        }

        String tip = getResourceString(key + tipSuffix);
        if (tip != null) {
            b.setToolTipText(tip);
        }

        return b;
    }

    public Runnable onSave = null;

    protected JMenuBar createMenubar() {
        JMenuBar mb = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem save = new JMenuItem("Save", Workspace.createImageIcon("resources/save.gif"));
        JMenuItem exit = new JMenuItem("Exit", Workspace.createImageIcon("resources/system-log-out.png"));
        save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        save.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (onSave != null) {
                    onSave.run();
                } else {
                    FileUtils.saveFileText(file.getPath(), editor.getText());
                }
            }
        });
        exit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                getFrame().setVisible(false);
            }
        });

        fileMenu.add(save);
        fileMenu.addSeparator();
        fileMenu.add(exit);

        mb.add(fileMenu);

        return mb;
    }

    protected PropertyChangeListener createActionChangeListener(JMenuItem b) {
        return new ActionChangedListener(b);
    }

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

    public RSyntaxTextArea editor;

    private Hashtable commands;
    private Hashtable menuItems;
    private JComponent status;
    private JFrame elementTreeFrame;
    protected UndoableEditListener undoHandler = new UndoHandler();
    protected UndoManager undo = new UndoManager();
    public static final String imageSuffix = "Image";
    public static final String labelSuffix = "Label";
    public static final String actionSuffix = "Action";
    public static final String tipSuffix = "Tooltip";
    public static final String openAction = "open";
    public static final String newAction = "new";
    public static final String saveAction = "save";
    public static final String exitAction = "exit";
    public static final String showElementTreeAction = "showElementTree";

    class UndoHandler implements UndoableEditListener {

        public void undoableEditHappened(UndoableEditEvent e) {
            undo.addEdit(e.getEdit());
            undoAction.update();
            redoAction.update();
        }
    }

    class StatusBar extends JComponent {

        public StatusBar() {
            super();
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        }

        public void paint(Graphics g) {
            super.paint(g);
        }
    }

    private UndoAction undoAction = new UndoAction();
    private RedoAction redoAction = new RedoAction();
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
                log.error("Unable to undo", ex);
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
                log.error("Unable to redo", ex);
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
                editor.setCaretPosition(0);
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
            getFrame().setTitle("");
            revalidate();
        }
    }

    Notepad parent = this;

    class ExitAction extends AbstractAction {

        ExitAction() {
            super(exitAction);
        }

        public void actionPerformed(ActionEvent e) {
            saveDocument(parent);
            parent.getFrame().setVisible(false);
        }
    }

    class ShowElementTreeAction extends AbstractAction {

        ShowElementTreeAction() {
            super(showElementTreeAction);
        }

        public void actionPerformed(ActionEvent e) {
            if (elementTreeFrame == null) {
                try {
                    String title = "";
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
}

class RSyntaxTextAreaCellEditor extends AbstractCellEditor implements TableCellEditor {
    private RSyntaxTextArea component = Notepad.getInstance();

    public RSyntaxTextAreaCellEditor() {
        component.setSyntaxEditingStyle(RSyntaxTextArea.SYNTAX_STYLE_XML);
        component.setMargin(new Insets(0, 0, 0, 0));
        component.addKeyListener(new KeyAdapter() {
            private String contentBeforeEnter = null;

            @Override
            public void keyReleased(KeyEvent e) {
                String text = component.getText();
                if (e.getKeyCode() == KeyEvent.VK_ENTER && text.contains("\n")) {
                    component.setText(text.replace("\n", ""));
                    stopCellEditing();
                }
            }
        });
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int rowIndex, int vColIndex) {
        component.setText((String) value);
        return component;
    }

    public Object getCellEditorValue() {
        return component.getText();
    }
}
