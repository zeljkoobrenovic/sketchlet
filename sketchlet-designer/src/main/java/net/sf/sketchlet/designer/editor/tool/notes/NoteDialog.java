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

    JScrollPane scrollPane;
    public JTextArea noteTextArea = new JTextArea();
    public int original_w;
    public int original_h;
    public boolean isMinimized = false;
    public static Font noteFont = new Font("Verdana", Font.PLAIN, 11);

    public NoteDialog(int x, int y) {
        super("", true, true, false, true);
        setLayout(new BorderLayout());
        setTitle("");
        // setAlwaysOnTop(true);
        this.setFrameIcon(Workspace.createImageIcon("resources/cursor_postit.png"));

        scrollPane = new JScrollPane(noteTextArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        noteTextArea.setLineWrap(true);
        noteTextArea.setWrapStyleWord(true);
        // noteTextArea.setFont(new Font("Verdana", Font.PLAIN, 11));
        noteTextArea.setFont(noteFont);

        noteTextArea.setBackground(Color.YELLOW);
        add(scrollPane, BorderLayout.CENTER);

        this.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));

        setSize(120, 100);
        setLocation(x, y);

        this.addInternalFrameListener(new InternalFrameAdapter() {

            public void internalFrameClosed(InternalFrameEvent e) {
                SketchletEditor.editorPanel.currentPage.notes.remove(NoteDialog.this);
            }
        });
    }

    public static void main(String args[]) {
        new NoteDialog(20, 20);
    }
}
