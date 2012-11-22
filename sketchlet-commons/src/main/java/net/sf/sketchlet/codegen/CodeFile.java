/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.codegen;

import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.plugin.CodeGenPluginFile;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

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

    private Vector<Vector<CodeFragment>> sections = new Vector<Vector<CodeFragment>>();
    private Vector<ImageFile> images = new Vector<ImageFile>();

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

        for (ImageFile imgf : this.getImages()) {
            img.add((!imgf.getSubDir().isEmpty() ? (imgf.getSubDir() + "/") : "") + imgf.getFileName());
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
        for (Vector<CodeFragment> section : this.getSections()) {
            for (CodeFragment fragment : section) {
                fragment.prepare();
            }
        }
    }

    public void generate() {
        for (Vector<CodeFragment> section : this.getSections()) {
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
            getSections().add(new Vector<CodeFragment>());
        }
    }

    public void clearAllCode() {
        int numOfSections = this.getSections().size();
        this.setSections(new Vector<Vector<CodeFragment>>());
        this.init(numOfSections);
    }

    public int getSectionsCount() {
        return this.getSections().size();
    }

    public void exportFile(File dir) {
        FileUtils.saveFileText(new File(dir, this.getFileName()).getPath(), this.toString());
        this.exportImages(dir);
    }

    public void exportImages(File dir) {
        for (ImageFile imgfile : this.getImages()) {
            imgfile.exportImage(dir);
        }
    }

    public CodeFragment addCodeFragment(String code) {
        return this.addCodeFragment(code, 0);
    }

    public void addEmptyLine(int sectionIndex) {
        Vector<CodeFragment> section;
        if (sectionIndex >= 0 && sectionIndex < getSections().size()) {
            section = getSections().elementAt(sectionIndex);
        } else {
            section = getSections().elementAt(0);
        }
        section.add(new CodeFragment(this, ""));
    }

    public CodeFragment addCodeFragment(String code, int sectionIndex) {
        Vector<CodeFragment> section;
        if (sectionIndex >= 0 && sectionIndex < getSections().size()) {
            section = getSections().elementAt(sectionIndex);
        } else {
            section = getSections().elementAt(0);
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
        for (CodeFragment codeFragment : this.getSections().elementAt(section)) {
            for (String line : codeFragment.getCodeLines()) {
                if (line.equals(code)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void addCodeFragment(CodeFragment code, int sectionIndex) {
        Vector<CodeFragment> section;
        if (sectionIndex >= 0 && sectionIndex < getSections().size()) {
            section = getSections().elementAt(sectionIndex);
        } else {
            section = getSections().elementAt(0);
        }
        section.add(code);
    }

    public void sort(int sectionIndex) {
        Vector<CodeFragment> section;
        if (sectionIndex >= 0 && sectionIndex < getSections().size()) {
            section = getSections().elementAt(sectionIndex);

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

        for (Vector<CodeFragment> section : this.getSections()) {
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

    public Vector<Vector<CodeFragment>> getSections() {
        return sections;
    }

    public void setSections(Vector<Vector<CodeFragment>> sections) {
        this.sections = sections;
    }

    public Vector<ImageFile> getImages() {
        return images;
    }

    public void setImages(Vector<ImageFile> images) {
        this.images = images;
    }
}
