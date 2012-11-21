/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.tool.notes;

import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;

/**
 * @author zobrenovic
 */
public class NoteDialog extends JInternalFrame {

    private JScrollPane scrollPane;
    private JTextArea noteTextArea = new JTextArea();
    private int originalWidth;
    private int originalHeight;
    private boolean minimized = false;
    private static Font noteFont = new Font("Verdana", Font.PLAIN, 11);

    public NoteDialog(int x, int y) {
        super("", true, true, false, true);
        setLayout(new BorderLayout());
        setTitle("");
        this.setFrameIcon(Workspace.createImageIcon("resources/cursor_postit.png"));

        scrollPane = new JScrollPane(noteTextArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        noteTextArea.setLineWrap(true);
        noteTextArea.setWrapStyleWord(true);
        noteTextArea.setFont(noteFont);

        noteTextArea.setBackground(Color.YELLOW);
        add(scrollPane, BorderLayout.CENTER);

        this.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));

        setSize(120, 100);
        setLocation(x, y);

        this.addInternalFrameListener(new InternalFrameAdapter() {

            public void internalFrameClosed(InternalFrameEvent e) {
                SketchletEditor.getInstance().getCurrentPage().getNotes().remove(NoteDialog.this);
            }
        });
    }

    public boolean isMinimized() {
        return minimized;
    }

    public void setMinimized(boolean minimized) {
        this.minimized = minimized;
    }

    public int getOriginalWidth() {
        return originalWidth;
    }

    public void setOriginalWidth(int originalWidth) {
        this.originalWidth = originalWidth;
    }

    public int getOriginalHeight() {
        return originalHeight;
    }

    public void setOriginalHeight(int originalHeight) {
        this.originalHeight = originalHeight;
    }

    public String getNoteText() {
        return noteTextArea.getText();
    }

    public void setNoteText(String noteText) {
        this.noteTextArea.setText(noteText);
    }
}
