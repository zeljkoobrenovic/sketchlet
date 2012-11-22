/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.codegen;

import java.util.Vector;

/**
 *
 * @author zobrenovic
 */
public class CodeFragment {

    private CodeFile codeFile;
    private Vector<String> codeLines = new Vector<String>();
    private int indentLevel = 0;

    public CodeFragment(CodeFile codeFile) {
        this(codeFile, "");
    }

    public CodeFragment(CodeFile codeFile, String codeFragment) {
        this.setCodeFile(codeFile);
        this.getCodeLines().add(codeFragment);
    }

    public void setIndentLevel(int level) {
        this.indentLevel = level;
    }

    public void prepare() {
    }

    public void generate() {
    }

    public void appendLine(String code) {
        this.getCodeLines().add(code);
    }

    public void appendLines(Vector<String> lines, int level) {
        String prefix = CodeGenUtils.getTabSpaces(level);
        for (String line : lines) {
            this.getCodeLines().add(prefix + line);
        }
    }

    public void generate(int indentLevel) {
        this.setIndentLevel(indentLevel);
        this.generate();
    }

    public String toString() {
        StringBuffer str = new StringBuffer();

        for (String strFragment : this.getCodeLines()) {
            str.append(CodeGenUtils.getTabSpaces(this.getIndentLevel()) + strFragment);
            if (!strFragment.endsWith("\n")) {
                str.append("\n");
            }
        }

        return str.toString();
    }

    public CodeFile getCodeFile() {
        return codeFile;
    }

    public void setCodeFile(CodeFile codeFile) {
        this.codeFile = codeFile;
    }

    public Vector<String> getCodeLines() {
        return codeLines;
    }

    public void setCodeLines(Vector<String> codeLines) {
        this.codeLines = codeLines;
    }

    public int getIndentLevel() {
        return indentLevel;
    }
}
