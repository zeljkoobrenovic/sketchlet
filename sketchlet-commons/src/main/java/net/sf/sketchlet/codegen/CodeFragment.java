/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.codegen;

import net.sf.sketchlet.codegen.CodeGenUtils;
import java.util.Vector;

/**
 *
 * @author zobrenovic
 */
public class CodeFragment {

    public CodeFile codeFile;
    public Vector<String> codeLines = new Vector<String>();
    int indentLevel = 0;

    public CodeFragment(CodeFile codeFile) {
        this(codeFile, "");
    }

    public CodeFragment(CodeFile codeFile, String codeFragment) {
        this.codeFile = codeFile;
        this.codeLines.add(codeFragment);
    }

    public void setIndentLevel(int level) {
        this.indentLevel = level;
    }

    public void prepare() {
    }

    public void generate() {
    }

    public void appendLine(String code) {
        this.codeLines.add(code);
    }

    public void appendLines(Vector<String> lines, int level) {
        String prefix = CodeGenUtils.getTabSpaces(level);
        for (String line : lines) {
            this.codeLines.add(prefix + line);
        }
    }

    public void generate(int indentLevel) {
        this.setIndentLevel(indentLevel);
        this.generate();
    }

    public String toString() {
        StringBuffer str = new StringBuffer();

        for (String strFragment : this.codeLines) {
            str.append(CodeGenUtils.getTabSpaces(this.indentLevel) + strFragment);
            if (!strFragment.endsWith("\n")) {
                str.append("\n");
            }
        }

        return str.toString();
    }
}
