/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.codegen;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.plugin.CodeGenPluginFile;

/**
 *
 * @author zobrenovic
 */
public abstract class CodeFile implements CodeGenPluginFile {

    public static final int SECTION_HEADER = 1;
    public static final int SECTION_SCRIPT_BEGIN = 2;
    public static final int SECTION_SCRIPT_IMPORT = 3;
    public static final int SECTION_SCRIPT_GLOBAL_VARIABLES = 4;
    public static final int SECTION_SCRIPT_MAIN = 5;
    public static final int SECTION_SCRIPT_END = 6;
    public static final int SECTION_DECLARATIONS_BEGIN = 7;
    public static final int SECTION_DECLARATIONS_MAIN = 8;
    public static final int SECTION_DECLARATIONS_END = 9;
    public static final int SECTION_BODY_BEGIN = 10;
    public static final int SECTION_BODY = 11;
    public static final int SECTION_BODY_END = 12;
    public static final int SECTION_FOOTER = 12;
    public Vector<Vector<CodeFragment>> sections = new Vector<Vector<CodeFragment>>();
    public Vector<ImageFile> images = new Vector<ImageFile>();

    public CodeFile() {
        this(25);
    }

    public CodeFile(int numOfSections) {
        init(numOfSections);
    }

    public String getPreviewText() {
        return this.toString();
    }

    public Vector<String> getImageFilePaths() {
        Vector<String> img = new Vector<String>();

        for (ImageFile imgf : this.images) {
            img.add((!imgf.subDir.isEmpty() ? (imgf.subDir + "/") : "") + imgf.fileName);
        }

        return img;
    }

    public abstract String getFileName();

    public String getFileMimeType() {
        return "text/xml";
    }

    public String getSubDirectory() {
        return "";
    }

    public void prepare() {
        for (Vector<CodeFragment> section : this.sections) {
            for (CodeFragment fragment : section) {
                fragment.prepare();
            }
        }
    }

    public void generate() {
        for (Vector<CodeFragment> section : this.sections) {
            for (CodeFragment fragment : section) {
                fragment.generate();
            }
        }
    }

    public boolean shouldExport() {
        return true;
    }

    public void init(int numOfSections) {
        for (int i = 0; i < numOfSections; i++) {
            sections.add(new Vector<CodeFragment>());
        }
    }

    public void clearAllCode() {
        int numOfSections = this.sections.size();
        this.sections = new Vector<Vector<CodeFragment>>();
        this.init(numOfSections);
    }

    public int getSectionsCount() {
        return this.sections.size();
    }

    public void exportFile(File dir) {
        FileUtils.saveFileText(new File(dir, this.getFileName()).getPath(), this.toString());
        this.exportImages(dir);
    }

    public void exportImages(File dir) {
        for (ImageFile imgfile : this.images) {
            imgfile.exportImage(dir);
        }
    }

    public CodeFragment addCodeFragment(String code) {
        return this.addCodeFragment(code, 0);
    }

    public void addEmptyLine(int sectionIndex) {
        Vector<CodeFragment> section;
        if (sectionIndex >= 0 && sectionIndex < sections.size()) {
            section = sections.elementAt(sectionIndex);
        } else {
            section = sections.elementAt(0);
        }
        section.add(new CodeFragment(this, ""));
    }

    public CodeFragment addCodeFragment(String code, int sectionIndex) {
        Vector<CodeFragment> section;
        if (sectionIndex >= 0 && sectionIndex < sections.size()) {
            section = sections.elementAt(sectionIndex);
        } else {
            section = sections.elementAt(0);
        }
        CodeFragment fragment = new CodeFragment(this, code);
        if (!section.contains(code) && !code.trim().isEmpty()) {
            section.add(fragment);
        }

        return fragment;
    }

    public void addUniqueCodeFragment(String code, int sectionIndex, int identLevel) {
        if (!this.codeExists(code, sectionIndex)) {
            this.addCodeFragment(code, sectionIndex, identLevel);
        }
    }

    public void addCodeFragment(String code, int sectionIndex, int identLevel) {
        CodeFragment fragment = this.addCodeFragment(code, sectionIndex);
        fragment.setIndentLevel(identLevel);
    }

    public boolean codeExists(String code, int section) {
        for (CodeFragment codeFragment : this.sections.elementAt(section)) {
            for (String line : codeFragment.codeLines) {
                if (line.equals(code)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void addCodeFragment(CodeFragment code, int sectionIndex) {
        Vector<CodeFragment> section;
        if (sectionIndex >= 0 && sectionIndex < sections.size()) {
            section = sections.elementAt(sectionIndex);
        } else {
            section = sections.elementAt(0);
        }
        section.add(code);
    }

    public void sort(int sectionIndex) {
        Vector<CodeFragment> section;
        if (sectionIndex >= 0 && sectionIndex < sections.size()) {
            section = sections.elementAt(sectionIndex);

            Collections.sort(section, new Comparator<CodeFragment>() {

                public int compare(CodeFragment o1, CodeFragment o2) {
                    return o1.toString().compareTo(o2.toString());
                }

                public boolean equals(Object obj) {
                    return obj == this;
                }
            });
        }
    }

    public String toString() {
        this.generate();
        StringBuffer str = new StringBuffer();

        for (Vector<CodeFragment> section : this.sections) {
            for (CodeFragment fragment : section) {
                String strFragment = fragment.toString();
                str.append(strFragment);
                if (!strFragment.isEmpty() && !strFragment.endsWith("\n")) {
                    str.append("\n");
                }
            }
        }

        return str.toString();
    }
}
