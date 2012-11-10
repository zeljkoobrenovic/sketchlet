/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.uml;

import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.*;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.type.ClassOrInterfaceType;
import net.sf.sketchlet.common.EscapeChars;
import net.sf.sketchlet.common.file.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zeljko
 */
public class JavaUmlParser implements Runnable {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        String result = new JavaUmlParser().parseFileXml(new File("C:/tutorials/LDAP/final/src/main/java"), new File("C:/tutorials/LDAP/final/src/main/java"));
        result = "<?xml version='1.0' encoding='UTF-8'?>\n<java>\n" + result + "</java>";
        // System.out.println(result);
        FileUtils.saveFileText(new File("files/bbproject/xml-java/java.xml"), result);

        result = new JavaUmlParser().parseFileXml(new File("C:/svnroot/portal5/foundation/trunk/business/src/main"), new File("C:/svnroot/portal5/foundation/trunk/business/src/main"));
        result = "<?xml version='1.0' encoding='UTF-8'?>\n<java>\n" + result + "</java>";
        System.out.println(result);
        FileUtils.saveFileText(new File("files/bbproject/xml-java/java2.xml"), result);
    }

    public String parseFile(File javaFile) {
        String result = "";
        if (javaFile.isDirectory()) {
            for (File f : javaFile.listFiles()) {
                result += parseFile(f);
            }
        } else if (javaFile.getName().endsWith(".java")) {
            try {
                FileInputStream in = new FileInputStream(javaFile);

                CompilationUnit cu;
                try {
                    cu = JavaParser.parse(in);
                } finally {
                    in.close();
                }

                String packageMembers = "";

                String addAfter = "";
                for (TypeDeclaration type : cu.getTypes()) {
                    if (type instanceof ClassOrInterfaceDeclaration) {
                        result += "package " + cu.getPackage().getName() + "\n";
                        result += "   has: " + type.getName() + "\n";
                        ClassOrInterfaceDeclaration cid = (ClassOrInterfaceDeclaration) type;
                        result += cid.getName() + "\n";
                        result += "  color:lightblue\n";
                        if (cid.getImplements() != null) {
                            for (ClassOrInterfaceType ici : cid.getImplements()) {
                                result += "  implements:" + ici.getName() + "\n";
                                addAfter += ici.getName() + "\n";
                                if (cu.getImports() != null) {
                                    for (ImportDeclaration id : cu.getImports()) {
                                        if (id.toString().trim().endsWith("." + ici.getName() + ";")) {
                                            addAfter += "package " + getPackage(id.toString().trim()) + "\n";
                                            addAfter += "  has:" + ici.getName() + "\n";
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if (cid.getExtends() != null) {
                            for (ClassOrInterfaceType eci : cid.getExtends()) {
                                result += "  extends:" + eci.getName() + "\n";
                                addAfter += eci.getName() + "\n";
                                if (cu.getImports() != null) {
                                    for (ImportDeclaration id : cu.getImports()) {
                                        if (id.toString().trim().endsWith("." + eci.getName() + ";")) {
                                            addAfter += "package " + getPackage(id.toString().trim()) + "\n";
                                            addAfter += "  has:" + eci.getName() + "\n";
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        if (cid.getAnnotations() != null) {
                            for (AnnotationExpr ann : cid.getAnnotations()) {
                                result += "  stereotype:" + ann.getName() + "\n";
                            }
                        }
                        if (cid.getMembers() != null) {
                            for (BodyDeclaration bd : cid.getMembers()) {
                                if (bd instanceof MethodDeclaration) {
                                    MethodDeclaration m = (MethodDeclaration) bd;
                                    if (!ModifierSet.isPrivate(m.getModifiers())) {
                                        result += "  method:" + m.getName() + "\n";
                                    }
                                }
                                if (bd instanceof FieldDeclaration) {
                                    FieldDeclaration m = (FieldDeclaration) bd;
                                    if (!ModifierSet.isPrivate(m.getModifiers())) {
                                        for (VariableDeclarator v : m.getVariables()) {
                                            result += "  field:" + v.getId().getName() + "\n";
                                        }
                                    }
                                }
                            }
                        }

                        packageMembers += packageMembers.isEmpty() ? "" : ",";
                        packageMembers += cid.getName();
                    }
                }

                result += addAfter;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public String parseFileXml(File codeBase) {
        return this.parseFileXml(codeBase, codeBase);
    }

    public String parseFileXml(File codeBase, File javaFile) {
        String result = "";
        String strDir = codeBase.toURI().relativize(javaFile.getParentFile().toURI()).getPath().replace("\\", "/");
        if (javaFile.isDirectory()) {
            for (File f : javaFile.listFiles()) {
                result += parseFileXml(codeBase, f);
            }
        } else if (javaFile.getName().endsWith(".java")) {
            try {
                FileInputStream in = new FileInputStream(javaFile);

                CompilationUnit cu;
                try {
                    cu = JavaParser.parse(in);
                } finally {
                    in.close();
                }

                String packageMembers = "";

                List<String> addAfter = new ArrayList<String>();

                if (cu != null && cu.getTypes() != null) {
                    for (TypeDeclaration type : cu.getTypes()) {
                        if (type instanceof ClassOrInterfaceDeclaration) {
                            ClassOrInterfaceDeclaration cid = (ClassOrInterfaceDeclaration) type;
                            result += "<class name='" + cid.getName() + "' package='" + cu.getPackage().getName() + "' directory='" + "" + "' file='" + javaFile.getName() + "'>\n";
                            if (cid.getAnnotations() != null) {
                                result += "   <annotations>\n";
                                for (AnnotationExpr ann : cid.getAnnotations()) {
                                    result += "      <annotation name='" + ann.getName() + "'";
                                    if (ann.getData() != null) {
                                        result += ">";
                                        result += "<data>" + ann.getData() + "</data>";
                                        result += "</annotation>\n";
                                    } else {
                                        result += "/>\n";
                                    }
                                }
                                result += "   </annotations>\n";
                            }
                            if (cid.getImplements() != null) {
                                result += "   <implements>\n";
                                for (ClassOrInterfaceType ici : cid.getImplements()) {
                                    result += this.getDeclarationFromImport(cu, javaFile, "interface", ici.getName());
                                }
                                result += "   </implements>\n";
                            }
                            if (cid.getExtends() != null) {
                                result += "   <extends>\n";
                                for (ClassOrInterfaceType eci : cid.getExtends()) {
                                    result += this.getDeclarationFromImport(cu, javaFile, "class", eci.getName());
                                }
                                result += "   </extends>\n";
                            }

                            if (cid.getMembers() != null) {
                                String strMethods = "";
                                for (BodyDeclaration bd : cid.getMembers()) {
                                    if (bd instanceof MethodDeclaration) {
                                        MethodDeclaration m = (MethodDeclaration) bd;
                                        int mod = m.getModifiers();
                                        strMethods += "      <method name='" + m.getName() + "'"
                                                + " returns='" + EscapeChars.forHTMLTag(m.getType().toString()) + "'";
                                        if (ModifierSet.isFinal(mod)) {
                                            strMethods += " final='" + ModifierSet.isFinal(mod) + "'";
                                        }
                                        if (ModifierSet.isStatic(mod)) {
                                            strMethods += " static='" + ModifierSet.isStatic(mod) + "'";
                                        }
                                        if (ModifierSet.isAbstract(mod)) {
                                            strMethods += " abstract='" + ModifierSet.isAbstract(mod) + "'";
                                        }
                                        strMethods += " visibility='" + (ModifierSet.isPrivate(mod) ? "private" : (ModifierSet.isPublic(mod) ? "public" : (ModifierSet.isProtected(mod) ? "protected" : "package"))) + "'";
                                        if (m.getAnnotations() != null && m.getParameters() != null) {
                                            strMethods += ">\n";
                                            if (m.getAnnotations() != null) {
                                                for (AnnotationExpr ann : m.getAnnotations()) {
                                                    strMethods += "         <annotation name='" + StringEscapeUtils.escapeXml(ann.getName().toString()) + "'";
                                                    if (ann.getData() != null) {
                                                        strMethods += ">\n";
                                                        strMethods += "            <data>" + StringEscapeUtils.escapeXml(ann.getData().toString()) + "</data>";
                                                        strMethods += "         </annotation>\n";
                                                    } else {
                                                        strMethods += "/>\n";
                                                    }
                                                }
                                            }
                                            if (m.getParameters() != null) {
                                                for (Parameter param : m.getParameters()) {
                                                    strMethods += "         <param name='" + StringEscapeUtils.escapeXml(param.getId().toString()) + "'";
                                                    strMethods += " type='" + StringEscapeUtils.escapeXml(param.getType().toString()) + "'";
                                                    strMethods += "/>\n";
                                                }
                                            }
                                            strMethods += "      </method>\n";
                                        } else {
                                            strMethods += "/>\n";
                                        }
                                    }
                                }
                                if (!strMethods.isEmpty()) {
                                    result += "   <methods>\n";
                                    result += strMethods;
                                    result += "   </methods>\n";
                                }
                            }

                            if (cid.getMembers() != null) {
                                String strFields = "";
                                for (BodyDeclaration bd : cid.getMembers()) {
                                    if (bd instanceof FieldDeclaration) {
                                        FieldDeclaration f = (FieldDeclaration) bd;
                                        if (!ModifierSet.isPrivate(f.getModifiers())) {
                                            for (VariableDeclarator v : f.getVariables()) {
                                                String rawType = f.getType().toString();
                                                String types = rawType.replace("[]", "");
                                                if (rawType.contains("<")) {
                                                    int n1 = rawType.indexOf("<");
                                                    int n2 = rawType.indexOf(">");
                                                    if (n2 > n1) {
                                                        types = rawType.substring(n1 + 1, n2);
                                                    }
                                                }

                                                String tt[] = types.split(",");
                                                for (String st : tt) {
                                                    String strClass = this.getDeclarationFromImport(cu, javaFile, "class", st);
                                                    addAfter.remove(strClass);
                                                    addAfter.add(strClass);
                                                }

                                                int m = f.getModifiers();
                                                strFields += "      <field id='"
                                                        + v.getId()
                                                        + "' rawType='"
                                                        + net.sf.sketchlet.common.EscapeChars.forHTMLTag(rawType)
                                                        + "' types='"
                                                        + net.sf.sketchlet.common.EscapeChars.forHTMLTag(types)
                                                        + "' cardinality='"
                                                        + (rawType.contains("[]") || rawType.contains("List<") || rawType.contains("Map<") ? "*" : "") + "'";
                                                if (ModifierSet.isFinal(m)) {
                                                    strFields += " final='" + ModifierSet.isFinal(m) + "'";
                                                }
                                                if (ModifierSet.isFinal(m)) {
                                                    strFields += " static='" + ModifierSet.isStatic(m) + "'";
                                                }
                                                if (ModifierSet.isFinal(m)) {
                                                    strFields += " visibility='" + (ModifierSet.isPrivate(m) ? "private" : (ModifierSet.isPublic(m) ? "public" : (ModifierSet.isProtected(m) ? "protected" : "package"))) + "'";
                                                }
                                                strFields += "/>\n";
                                            }
                                        }
                                    }
                                }
                                if (!strFields.isEmpty()) {
                                    result += "   <fields>\n";
                                    result += strFields;
                                    result += "   </fields>\n";
                                }

                            }
                            packageMembers += packageMembers.isEmpty() ? "" : ",";
                            packageMembers += cid.getName();
                            result += "</class>\n\n";
                        }
                    }

                    for (String str : addAfter) {
                        result += str.trim() + "\n";
                    }
                }
            } catch (Exception e) {
                System.out.println(result);
                e.printStackTrace();
            }
        }

        return result;
    }

    private static String getPackage(String fullName) {
        if (fullName.startsWith("import ")) {
            fullName = fullName.substring(7).trim();
        }
        if (fullName.endsWith(";")) {
            fullName = fullName.substring(0, fullName.length() - 1).trim();
        }
        int n = fullName.lastIndexOf(".");
        if (n > 0) {
            return fullName.substring(0, n);
        }
        return fullName;
    }

    public void run() {
    }

    private String getDeclarationFromImport(CompilationUnit cu, File javaFile, String type, String name) {
        String result = "";
        if (name.equals("String") || name.substring(0, 1).equals(name.substring(0, 1).toLowerCase())) {
            return "";
        }
        result += "      <" + type + " name='" + name + "'";
        if (cu.getImports() != null) {
            for (ImportDeclaration id : cu.getImports()) {
                if (id.toString().trim().endsWith("." + name + ";")) {
                    String pack = getPackage(id.toString().trim());
                    if (pack.isEmpty() && new File(javaFile.getParentFile(), name + ".java").exists()) {
                        pack += cu.getPackage().getName();
                    }
                    result += " package='" + pack + "'";
                    break;
                }
            }
        }

        result += "/>\n";

        return result;
    }
}
