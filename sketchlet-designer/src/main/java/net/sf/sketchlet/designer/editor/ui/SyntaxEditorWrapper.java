package net.sf.sketchlet.designer.editor.ui;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

/**
 * @author zeljko
 */
public class SyntaxEditorWrapper {
    private RSyntaxTextArea syntaxTextArea;
    private Runnable onChange;

    public SyntaxEditorWrapper(RSyntaxTextArea syntaxTextArea) {
        this.syntaxTextArea = syntaxTextArea;
    }

    public SyntaxEditorWrapper(RSyntaxTextArea syntaxTextArea, Runnable onChange) {
        this.syntaxTextArea = syntaxTextArea;
        this.onChange = onChange;
    }

    public RSyntaxTextArea getSyntaxTextArea() {
        return syntaxTextArea;
    }

    public void setSyntaxTextArea(RSyntaxTextArea syntaxTextArea) {
        this.syntaxTextArea = syntaxTextArea;
    }

    public Runnable getOnChange() {
        return onChange;
    }

    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }
}
