/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.uml;

import japa.parser.JavaParser;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zeljko
 */
public class CascadingUmlUtil {

    private String stereotype = "";

    public List<String> getAllNodes() {
        List<String> allNodes = new ArrayList<String>();
        for (String nodeName : classes.keySet()) {
            allNodes.add(nodeName);
        }

        return allNodes;
    }

    public List<String> getAllStereotypes() {
        List<String> allStereotypes = new ArrayList<String>();
        for (String nodeName : classes.keySet()) {
            for (String stereotype : classes.get(nodeName).stereotypes) {
                stereotype = "<" + stereotype + ">";
                if (!allStereotypes.contains(stereotype)) {
                    allStereotypes.add(stereotype);
                }
            }
        }

        return allStereotypes;
    }

    public List<String> getAllNamespaces() {
        List<String> allNamespaces = new ArrayList<String>();
        for (String nodeName : classes.keySet()) {
            String namespace = classes.get(nodeName).namespace;
            if (StringUtils.isNotBlank(namespace) && !allNamespaces.contains(namespace)) {
                allNamespaces.add(namespace);
            }
        }

        return allNamespaces;
    }

    public String getUmlGraphCode(String text) {
        text = text.replace("\\\n", "%n");
        String types[] = {"abstract class", "class", "node", "note", "component", "package", "collaboration", "usecase", "activeclass", "interface"};

        classes.clear();
        templates.clear();
        beginLines.clear();
        ClassInfo umlOptions = new ClassInfo("UMLOptions");
        umlOptions.incrementCount();
        umlOptions.addCommentLine("@hidden");
        String line;

        List<ClassInfo> currentClass = null;

        boolean bFields = false;
        boolean bMethods = false;
        boolean bTypes = false;
        boolean bVisibility = false;

        String multilinePrefix = "subclasses";

        int mode = 0;

        Map<String, String> stereotypeColors = new HashMap<String, String>();

        try {
            BufferedReader in = new BufferedReader(new StringReader(text));
            int lineCount = 0;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.endsWith("\\")) {
                    continue;
                }
                lineCount++;
                if (line.toLowerCase().trim().startsWith("stereotype-colors")) {
                    mode = 1;
                    continue;
                } else if (line.toLowerCase().startsWith("import ") || (lineCount <= 1 && line.toLowerCase().startsWith("package "))) {
                    this.beginLines.add(line.trim().replace(";", ""));
                    continue;
                } else if (!line.startsWith(" ")) {
                    mode = 0;
                } else if (mode >= 1) {
                    line = line.trim();
                    int n = line.indexOf(":");
                    if (n > 0) {
                        String as[] = line.substring(0, n).trim().split(",");
                        for (String s : as) {
                            if (mode == 1) {
                                stereotypeColors.put(s.trim(), line.substring(n + 1).trim());
                            }
                        }
                    }
                }

            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            BufferedReader in = new BufferedReader(new StringReader(text));
            int lineCount = 0;
            while ((line = in.readLine()) != null) {
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }
                lineCount++;
                line = line.replace("\t", "    ");
                if (line.toLowerCase().trim().equalsIgnoreCase("stereotype-colors")) {
                    mode = 1;
                    currentClass = null;
                    continue;
                } else if (line.toLowerCase().startsWith("import ") || (lineCount <= 1 && line.toLowerCase().startsWith("package "))) {
                    continue;
                } else if (!line.startsWith(" ")) {
                    {
                        line = line.replace("==>", "#####navassoc:");
                        line = line.replace("=>", "#####navassoc:");
                        line = line.replace("-->", "#####navassoc:");
                        line = line.replace("->", "#####navassoc:");
                        line = line.replace("<==", "#####revnavassoc:");
                        line = line.replace("<=", "#####revnavassoc:");
                        line = line.replace("<--", "#####revnavassoc:");
                        line = line.replace("<-", "#####revnavassoc:");
                        line = line.replace("--", "#####assoc:");
                        line = line.replace("==", "#####assoc:");

                        if (line.contains("#####")) {
                            int n1 = line.indexOf("#####");
                            if (n1 >= 0) {
                                int n2 = line.indexOf(":", n1 + 1);
                                if (n2 > 0) {
                                    String strClass1 = line.substring(0, n1).trim();
                                    String strClass2 = line.substring(n2 + 1).trim();

                                    int n3 = strClass2.indexOf("[");

                                    String paramParams = "";

                                    if (n3 > 0) {
                                        int n4 = strClass2.indexOf("]", n3 + 1);
                                        if (n4 > 0) {
                                            paramParams = strClass2.substring(n3 + 1, n4).trim();
                                            strClass2 = strClass2.substring(0, n3).trim();
                                        }
                                    }

                                    String strRel = line.substring(n1, n2).replace("#####", "").trim();
                                    ClassInfo ci1 = getClassInfo(strClass1);
                                    List<ClassInfo> cis = new ArrayList<ClassInfo>();
                                    cis.add(ci1);
                                    processRelation(cis, strRel, strClass2, paramParams);
                                    continue;
                                }
                            }
                        }
                    }
                    multilinePrefix = "subclasses";
                    mode = 0;
                    String type = "";
                    for (String s : types) {
                        if (line.startsWith(s + " ")) {
                            type = s;
                            line = line.substring(s.length() + 1).trim();
                            break;
                        }
                    }
                    stereotype = "";
                    String strParents = "";
                    int n = line.indexOf(":");
                    if (n > 0) {
                        strParents = line.substring(n + 1).trim();
                        line = line.substring(0, n).trim();
                    }

                    String name = line;
                    int n1 = line.indexOf("<");
                    int n2 = line.indexOf(">");
                    if (n1 >= 0 && n2 > n1) {
                        name = line.substring(0, n1).trim();
                        stereotype = line.substring(n1 + 1, n2).trim();
                    }

                    String names[];
                    if (name.isEmpty() && !stereotype.isEmpty()) {
                        names = new String[]{"*"};
                    } else {
                        names = name.split(",");
                    }
                    currentClass = new ArrayList<ClassInfo>();
                    for (String cn : names) {
                        cn = cn.trim();
                        ClassInfo cc;
                        if (cn.equalsIgnoreCase("UMLOptions") || cn.equalsIgnoreCase("Default") || cn.equalsIgnoreCase("Defaults")) {
                            cc = umlOptions;
                            currentClass.add(cc);
                        } else {
                            cc = getClassInfo(cn);
                            cc.incrementCount();
                            currentClass.add(cc);
                        }
                        stereotype = stereotype.trim();
                        if (!stereotype.isEmpty()) {
                            cc.setStereotype(stereotype);
                            String color = stereotypeColors.get(stereotype);
                            if (color != null) {
                                cc.addCommentLine("@opt nodefillcolor " + color);
                            }
                        }
                        if (type.equalsIgnoreCase("interface")) {
                            cc.setAsInterface(true);
                        } else if (!type.isEmpty()) {
                            // cc.addCommentLine("@opt shape " + type);
                            cc.setShape(type);
                        }
                        if (!strParents.isEmpty()) {
                            String parents[] = strParents.split(",");
                            for (String strClass : parents) {
                                strClass = strClass.trim();
                                ClassInfo info = getClassInfo(strClass);
                                cc.addExtendsOrImplement(info);
                            }
                        }
                    }
                    continue;
                } else if (mode == 0 && currentClass != null) {
                    line = line.replace("==>", "navassoc:");
                    line = line.replace("=>", "navassoc:");
                    line = line.replace("-->", "navassoc:");
                    line = line.replace("->", "navassoc:");
                    line = line.replace("<==", "revnavassoc:");
                    line = line.replace("<=", "revnavassoc:");
                    line = line.replace("<--", "revnavassoc:");
                    line = line.replace("<-", "revnavassoc:");
                    line = line.replace("--", "assoc:");
                    line = line.replace("==", "assoc:");
                    int n = line.indexOf(":");
                    if (n == -1) {
                        line = "    " + multilinePrefix + ":" + line.trim();
                        n = line.indexOf(":");
                    }
                    if (n > 0) {
                        String param = line.substring(0, n).trim();
                        String value = line.substring(n + 1).trim();
                        if (value.trim().isEmpty()) {
                            multilinePrefix = param;
                            continue;
                        }

                        String paramParams = "";
                        int np1 = param.indexOf("[");
                        int np2 = param.indexOf("]");
                        if (np1 > 0 && np2 > np1) {
                            paramParams = param.substring(np1 + 1, np2).trim();
                            param = param.substring(0, np1).trim();
                        }

                        if (param.equalsIgnoreCase("extends") || param.equalsIgnoreCase("specializes") || param.equalsIgnoreCase("inherits")) {
                            String array[] = value.split(",");
                            for (String strClass : array) {
                                strClass = strClass.trim();
                                ClassInfo info = getClassInfo(strClass);
                                for (ClassInfo cc : currentClass) {
                                    cc.addExtendsOrImplement(info);
                                }
                            }
                        } else if (param.equalsIgnoreCase("implements")) {
                            String array[] = value.split(",");
                            for (String interfaceName : array) {
                                interfaceName = interfaceName.trim();
                                ClassInfo info = getClassInfo(interfaceName);
                                info.setAsInterface(true);
                                for (ClassInfo cc : currentClass) {
                                    cc.addInterface(info.getClassId());
                                }
                            }
                        } else if (param.equalsIgnoreCase("assoc") || param.equalsIgnoreCase("association")
                                || param.equalsIgnoreCase("associations") || param.equalsIgnoreCase("navassoc")
                                || param.equalsIgnoreCase("has") || param.equalsIgnoreCase("composed")
                                || param.equalsIgnoreCase("depend") || param.equalsIgnoreCase("dependencies")) {
                            this.processRelation(currentClass, param, value, paramParams);
                        } else if (param.equalsIgnoreCase("revnavassoc")) {
                            this.processRelation(currentClass, param, value, paramParams);
                        } else if (param.equalsIgnoreCase("revdepend")) {
                            this.processRelation(currentClass, param, value, paramParams);
                        } else if (param.equalsIgnoreCase("tag") || param.equalsIgnoreCase("tags")) {
                            String array[] = value.split(",");
                            for (String strTag : array) {
                                for (ClassInfo cc : currentClass) {
                                    cc.addCommentLine("@tagvalue " + strTag.trim());
                                }
                            }
                        } else if (param.equalsIgnoreCase("opt")) {
                            value = value.trim();
                            if (value.equalsIgnoreCase("hidden")) {
                                for (ClassInfo cc : currentClass) {
                                    cc.addCommentLine("@hidden");
                                }
                            } else {
                                for (ClassInfo cc : currentClass) {
                                    cc.addCommentLine("@opt " + value.trim());
                                }
                            }
                        } else if (param.equalsIgnoreCase("type")) {
                            value = value.trim();
                            for (ClassInfo cc : currentClass) {
                                cc.setShape(value);
                            }
                        } else if (param.equalsIgnoreCase("stereotype")) {
                            value = value.trim();
                            for (ClassInfo cc : currentClass) {
                                cc.setStereotype(value);
                            }
                        } else if (param.equalsIgnoreCase("subclass") || param.equalsIgnoreCase("subclasses") || param.equalsIgnoreCase("inheritedby") || param.equalsIgnoreCase("specializedby") || param.equalsIgnoreCase("implementedby")) {
                            String array[] = value.split(",");
                            for (String strSubClass : array) {
                                ClassInfo info = getClassInfo(strSubClass.trim());
                                for (ClassInfo cc : currentClass) {
                                    info.addExtendsOrImplement(cc);
                                    if (!cc.getShape().isEmpty()) {
                                        info.setShape(cc.getShape());
                                    }
                                }
                            }
                        } else if (param.equalsIgnoreCase("field") || param.equalsIgnoreCase("fields") || param.equalsIgnoreCase("attribute") || param.equalsIgnoreCase("attributes")) {
                            String array[];
                            array = split(value, ',');
                            for (String strField : array) {
                                String field = strField.trim();
                                int fn1 = field.indexOf("<");
                                int fn2 = field.indexOf(">");
                                String fstereotype = "";
                                if (fn1 == 0 && fn2 > fn1) {
                                    fstereotype = field.substring(fn1 + 1, fn2).trim();
                                    field = field.substring(fn2 + 1).trim();
                                }
                                if (!field.contains(" ")) {
                                    field = "String " + field + "=\"\"";
                                } else {
                                    bTypes = true;
                                }
                                for (ClassInfo cc : currentClass) {
                                    if (!fstereotype.isEmpty()) {
                                        cc.addBodyLine("/** @stereotype \"" + fstereotype + "\" */");
                                    }
                                    if (cc.isInterface) {
                                        int ns = field.lastIndexOf(" ");
                                        if (ns > 0) {
                                            field = field.substring(ns + 1).trim();
                                        }
                                        field = "public static final String " + field + "=\"\"";
                                    }
                                    cc.addBodyLine(field + ";");
                                }
                            }
                            bFields = true;
                        } else if (param.equalsIgnoreCase("method") || param.equalsIgnoreCase("methods") || param.equalsIgnoreCase("operation") || param.equalsIgnoreCase("operations")) {
                            String array[];
                            array = split(value, ',');
                            for (String strMethod : array) {
                                String method = strMethod.trim();
                                int mn1 = method.indexOf("<");
                                int mn2 = method.indexOf(">");
                                String mstereotype = "";
                                if (mn1 == 0 && mn2 > mn1) {
                                    mstereotype = method.substring(mn1 + 1, mn2).trim();
                                    method = method.substring(mn2 + 1).trim();
                                }

                                method = CascadingUmlUtil.getSafeMethodSignature(method);

                                if (method != null) {
                                    for (ClassInfo cc : currentClass) {
                                        if (!mstereotype.isEmpty()) {
                                            cc.addBodyLine("/** @stereotype \"" + mstereotype + "\" */");
                                        }
                                        if (cc.isInterface) {
                                            cc.addBodyLine(method + ";");
                                        } else {
                                            if (method.contains("abstract ")) {
                                                cc.addBodyLine(method + ";");
                                            } else if (!method.contains("{")) {
                                                cc.addBodyLine(method + " {}");
                                            } else {
                                                cc.addBodyLine(method);
                                            }
                                        }
                                    }
                                }
                            }
                            bMethods = true;
                        } else if (param.equalsIgnoreCase("note")) {
                            for (ClassInfo cc : currentClass) {
                                cc.addCommentLine("@note " + value.trim());
                            }
                        } else if (param.equalsIgnoreCase("show")) {
                            String show[] = value.split(",");
                            for (ClassInfo cc : currentClass) {
                                for (String s : show) {
                                    cc.addCommentLine("@opt " + s.trim());
                                }
                            }
                        } else if (param.equalsIgnoreCase("color")) {
                            for (ClassInfo cc : currentClass) {
                                cc.addCommentLine("@opt nodefillcolor " + value.trim());
                            }
                        } else if (param.equalsIgnoreCase("namespace") || param.equalsIgnoreCase("package")) {
                            for (ClassInfo cc : currentClass) {
                                cc.addCommentLine("@opt namespace " + value.trim());
                            }
                        } else if (param.equalsIgnoreCase("text")) {
                            for (ClassInfo cc : currentClass) {
                                cc.setText(value.trim());
                            }
                        } else if (param.equalsIgnoreCase("count-filter")) {
                            for (ClassInfo cc : currentClass) {
                                cc.countFilter = value.trim();
                            }
                        } else if (param.equalsIgnoreCase("shape")) {
                            for (ClassInfo cc : currentClass) {
                                cc.addCommentLine("@opt shape " + value.trim());
                            }
                        } else {
                            for (ClassInfo cc : currentClass) {
                                cc.addCommentLine("@opt " + param + " " + value);
                            }
                        }
                    } else {
                        /*
                         * n = line.indexOf(":"); if (n > 0) { String param =
                         * line.substring(0, n).trim(); String value =
                         * line.substring(n + 1).trim(); for (ClassInfo cc :
                         * currentClass) { cc.addCommentLine("@opt " + param + "
                         * " + value); } } else { for (ClassInfo cc :
                         * currentClass) { cc.addCommentLine("@opt " +
                         * line.trim()); } }
                         */
                    }
                    if (bFields) {
                        for (ClassInfo cc : currentClass) {
                            cc.addCommentLine("@opt attributes");
                        }
                    }

                    if (bMethods) {
                        for (ClassInfo cc : currentClass) {
                            cc.addCommentLine("@opt operations");
                        }
                    }

                    if (bTypes) {
                        for (ClassInfo cc : currentClass) {
                            cc.addCommentLine("@opt types");
                        }
                    }

                    if (bVisibility) {
                        for (ClassInfo cc : currentClass) {
                            cc.addCommentLine("@opt visibility");
                        }
                    }
                }

            }

            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String code = "";
        for (String importLine : this.beginLines) {
            code += importLine + ";\n";
        }
        code += umlOptions + "\n\n";

        for (ClassInfo info : classes.values()) {
            for (ClassInfo tinfo : templates.values()) {
                if (tinfo.stereotypes.isEmpty() || info.compareStereoypes(tinfo)) {
                    if (!tinfo.countFilter.isEmpty()) {
                        String filter = tinfo.countFilter;
                        try {
                            if (filter.startsWith(">=")) {
                                filter = filter.substring(2).trim();
                                int count = (int) Double.parseDouble(filter);
                                if (info.getCount() < count) {
                                    continue;
                                }
                            } else if (filter.startsWith(">=")) {
                                filter = filter.substring(2).trim();
                                int count = (int) Double.parseDouble(filter);
                                if (info.getCount() > count) {
                                    continue;
                                }
                            } else if (filter.startsWith("==")) {
                                filter = filter.substring(2).trim();
                                int count = (int) Double.parseDouble(filter);
                                if (info.getCount() != count) {
                                    continue;
                                }
                            } else if (filter.startsWith("=")) {
                                filter = filter.substring(1).trim();
                                int count = (int) Double.parseDouble(filter);
                                if (info.getCount() != count) {
                                    continue;
                                }
                            } else if (filter.startsWith(">")) {
                                filter = filter.substring(1).trim();
                                int count = (int) Double.parseDouble(filter);
                                if (info.getCount() <= count) {
                                    continue;
                                }
                            } else if (filter.startsWith(">")) {
                                filter = filter.substring(1).trim();
                                int count = (int) Double.parseDouble(filter);
                                if (info.getCount() >= count) {
                                    continue;
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                    if (!(info.shape.isEmpty() && tinfo.shape.trim().equalsIgnoreCase("class"))) {
                        if (!tinfo.shape.isEmpty()) {
                            if (!(info.shape.equals(tinfo.shape))) {
                                continue;
                            }
                        }
                    }
                    if (tinfo.name.equals("*") || tinfo.name.isEmpty()) {
                        if (tinfo.getShape().isEmpty() || tinfo.getShape().equalsIgnoreCase(info.getShape()) || info.shape.isEmpty() && tinfo.shape.trim().equalsIgnoreCase("class")) {
                            info.merge(tinfo);
                        }
                    } else if (tinfo.name.startsWith("*") && tinfo.name.endsWith("*")) {
                        String str = tinfo.name.substring(1, tinfo.name.length() - 1);
                        if (info.name.contains(str)) {
                            info.merge(tinfo);
                        }
                    } else if (tinfo.name.startsWith("*")) {
                        String str = tinfo.name.substring(1);
                        if (info.name.endsWith(str)) {
                            info.merge(tinfo);
                        }
                    } else if (tinfo.name.endsWith("*")) {
                        String str = tinfo.name.substring(0, tinfo.name.length() - 1);
                        if (info.name.startsWith(str)) {
                            info.merge(tinfo);
                        }
                    }
                }
            }
        }

        for (ClassInfo info : classes.values()) {
            code += info + "\n\n";
        }

        return code;
    }

    public ClassInfo getClassInfo(String name) {
        ClassInfo info;
        if (name.contains("*")) {
            info = templates.get(name + ":" + stereotype);

            if (info == null) {
                info = addTemplateClass(name);
            }
        } else {
            info = classes.get(name);

            if (info == null) {
                info = addClass(name);
            }
        }

        return info;
    }

    public ClassInfo addClass(String name) {
        ClassInfo info = new ClassInfo(name);
        classes.put(name, info);

        return info;
    }

    public ClassInfo addTemplateClass(String name) {
        ClassInfo info = new ClassInfo(name);
        templates.put(name + ":" + stereotype, info);

        return info;
    }

    Map<String, ClassInfo> classes = new HashMap<String, ClassInfo>();
    Map<String, ClassInfo> templates = new HashMap<String, ClassInfo>();
    List<String> beginLines = new ArrayList<String>();

    static class ClassInfo {

        private String name = "";
        private List<String> bodyLines = new ArrayList<String>();
        private List<String> commentLines = new ArrayList<String>();
        private List<String> interfaces = new ArrayList<String>();
        boolean isInterface = false;
        private List<String> stereotypes = new ArrayList<String>();
        private String shape = "";
        private List<ClassInfo> extendOrImplements = new ArrayList<ClassInfo>();
        private int counter = 0;
        private String text = "";
        String countFilter = "";
        String namespace = "";

        public ClassInfo() {
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void merge(ClassInfo info) {
            List<String> list = new ArrayList<String>();
            list.addAll(info.bodyLines);
            list.addAll(bodyLines);
            bodyLines = list;

            list = new ArrayList<String>();
            list.addAll(info.commentLines);
            list.addAll(commentLines);
            commentLines = list;

            interfaces.addAll(info.interfaces);
            extendOrImplements.addAll(info.extendOrImplements);

            if (shape.isEmpty()) {
                shape = info.shape;
            }
            if (stereotypes.isEmpty()) {
                stereotypes = info.stereotypes;
            }

            if (!isInterface) {
                isInterface = isInterface;
            }
        }

        public void incrementCount() {
            counter++;
        }

        public int getCount() {
            return counter;
        }

        public void addExtendsOrImplement(ClassInfo info) {
            if (!this.extendOrImplements.contains(info)) {
                this.extendOrImplements.add(info);
            }
        }

        public ClassInfo(String name) {
            this.name = name;
        }

        public void addCommentLine(String line) {
            if (!this.commentLines.contains(line)) {
                this.commentLines.add(line);
            }
        }

        public void setStereotype(String stereotype) {
            this.stereotypes.remove(stereotype);
            this.stereotypes.add(stereotype);
        }

        public boolean compareStereoypes(ClassInfo info) {
            for (String str1 : info.stereotypes) {
                for (String str2 : this.stereotypes) {
                    if (str1.equals(str2)) {
                        return true;
                    }
                }
            }

            return false;
        }

        public List<String> getStereotype() {
            return this.stereotypes;
        }

        public void setShape(String shape) {
            this.shape = shape;
        }

        public String getShape() {
            return this.shape;
        }

        public void addUniqueCommentLine(String line) {
            if (!this.commentLines.contains(line)) {
                this.commentLines.add(line);
            }
        }

        public void addInterface(String interfaceName) {
            if (!this.interfaces.contains(interfaceName)) {
                this.interfaces.add(interfaceName);
            }
        }

        public void setAsInterface(boolean isInterface) {
            this.isInterface = isInterface;
        }

        public void addBodyLine(String line) {
            if (!this.bodyLines.contains(line)) {
                this.bodyLines.add(line);
            }
        }

        public String getClassId() {
            return (name.equals("UMLOptions")) ? name : getJavaIdentifier(this.name); //"Class_" + id;
        }

        public String toString() {
            String strClass = "";
            String _text = text.isEmpty() ? name : text;
            strClass += "/**" + "\n";
            strClass += " * " + _text.replace("%n", "\n *") + "\n";
            strClass += " * @opt commentname\n";
            strClass += " *\n";
            for (String line : this.commentLines) {
                strClass += " * " + line.replace("%n", "\n *") + "\n";
            }
            for (ClassInfo info : this.extendOrImplements) {
                if (info.isInterface) {
                    addInterface(info.getClassId());
                } else {
                    strClass += " * @extends " + info.getClassId() + "\n";
                }
            }
            for (String strtyp : this.stereotypes) {
                if (!strtyp.isEmpty()) {
                    strClass += " * @stereotype \"" + strtyp + "\"\n";
                }
            }
            if (!this.shape.trim().isEmpty()) {
                if (!shape.equalsIgnoreCase("abstract class")) {
                    strClass += " * @opt shape \"" + this.shape + "\"\n";
                }
            }
            strClass += " */" + "\n";
            if (isInterface) {
                strClass += "interface ";
            } else if (shape.equalsIgnoreCase("abstract class")) {
                strClass += "abstract class ";
            } else {
                strClass += "class ";
            }
            strClass += getClassId();
            if (this.interfaces.size() > 0) {
                strClass += isInterface ? " extends " : " implements ";
                String strInterfaces = "";
                for (String strInterface : interfaces) {
                    if (!strInterfaces.isEmpty()) {
                        strInterfaces += ", ";
                    }
                    strInterfaces += strInterface;
                }
                strClass += " " + strInterfaces + " ";
            }
            strClass += " {\n";
            for (String line : this.bodyLines) {
                strClass += "    " + line + "\n";
            }
            strClass += "}\n";

            return strClass;
        }
    }

    public static String[] split(String text, char sep) {
        char markers[][] = {{'<', '>'}, {'(', ')'}};
        char inMarker[] = null;

        List<String> list = new ArrayList<String>();

        String string = "";
        out:
        for (char c : text.toCharArray()) {
            if (inMarker != null && inMarker[1] == c) {
                inMarker = null;
            } else if (inMarker == null) {
                if (c == sep) {
                    if (!string.trim().isEmpty()) {
                        list.add(string.trim());
                    }
                    string = "";
                    continue;
                }
                for (char marker[] : markers) {
                    if (c == marker[0]) {
                        inMarker = marker;
                        break;
                    }
                }
            }
            string += c;
        }

        if (!string.trim().isEmpty()) {
            list.add(string.trim());
        }

        return list.toArray(new String[list.size()]);
    }

    public static String getJavaIdentifier(String str) {
        str = str.replace("-", "_");
        str = str.replace("/", "_");
        str = str.replace(".", "_");
        str = str.replace(" ", "_");
        str = str.replace("%", "_");

        return str;
    }

    private void processRelation(List<ClassInfo> currentClass, String param, String value, String paramParams) {
        if (param.equalsIgnoreCase("assoc") || param.equalsIgnoreCase("association")
                || param.equalsIgnoreCase("associations") || param.equalsIgnoreCase("navassoc")
                || param.equalsIgnoreCase("has") || param.equalsIgnoreCase("composed")
                || param.equalsIgnoreCase("depend") || param.equalsIgnoreCase("dependencies")) {
            if (param.equalsIgnoreCase("association")) {
                param = "assoc";
            } else if (param.equalsIgnoreCase("associations")) {
                param = "assoc";
            } else if (param.equalsIgnoreCase("dependency")) {
                param = "depend";
            } else if (param.equalsIgnoreCase("dependencies")) {
                param = "depend";
            }
            String array[] = value.split(",");
            final String defaultParams = paramParams.isEmpty() ? "- - -" : paramParams;
            for (String strClass : array) {
                strClass = strClass.trim();
                String params = defaultParams;
                int n1 = strClass.indexOf("[");
                int n2 = strClass.indexOf("]");
                if (n1 > 0 && n2 > n1) {
                    params = strClass.substring(n1 + 1, n2).trim();
                    strClass = strClass.substring(0, n1).trim();
                }
                if (params.trim().isEmpty()) {
                    params = defaultParams;
                }

                if (!params.contains(" ") || (params.trim().startsWith("\"") && params.endsWith("\""))) {
                    params = "- " + params + " -";
                }
                ClassInfo info = getClassInfo(strClass);
                for (ClassInfo cc : currentClass) {
                    cc.addCommentLine("@" + param + " " + params + " " + info.getClassId());
                }
            }
        } else if (param.equalsIgnoreCase("revnavassoc")) {
            param = "navassoc";
            String array[] = value.split(",");
            final String defaultParams = paramParams.isEmpty() ? "- - -" : paramParams;
            for (String strClass : array) {
                strClass = strClass.trim();
                String params = defaultParams;
                int n1 = strClass.indexOf("[");
                int n2 = strClass.indexOf("]");
                if (n1 > 0 && n2 > n1) {
                    params = strClass.substring(n1 + 1, n2).trim();
                    strClass = strClass.substring(0, n1).trim();
                }
                if (params.trim().isEmpty()) {
                    params = defaultParams;
                }

                if (!params.contains(" ") || (params.trim().startsWith("\"") && params.endsWith("\""))) {
                    params = "- " + params + " -";
                }
                ClassInfo info = getClassInfo(strClass);
                for (ClassInfo cc : currentClass) {
                    info.addCommentLine("@" + param + " " + params + " " + cc.getClassId());
                }
            }
        } else if (param.equalsIgnoreCase("revdepend")) {
            param = "depend";
            String array[] = value.split(",");
            final String defaultParams = paramParams.isEmpty() ? "- - -" : paramParams;
            for (String strClass : array) {
                strClass = strClass.trim();
                String params = defaultParams;
                int n1 = strClass.indexOf("[");
                int n2 = strClass.indexOf("]");
                if (n1 > 0 && n2 > n1) {
                    params = strClass.substring(n1 + 1, n2).trim();
                    strClass = strClass.substring(0, n1).trim();
                }
                if (params.trim().isEmpty()) {
                    params = defaultParams;
                }

                if (!params.contains(" ") || (params.trim().startsWith("\"") && params.endsWith("\""))) {
                    params = "- " + params + " -";
                }
                ClassInfo info = getClassInfo(strClass);
                for (ClassInfo cc : currentClass) {
                    info.addCommentLine("@" + param + " " + params + " " + cc.getClassId());
                }
            }
        }
    }

    private static String getSafeMethodSignature(String signature) {
        String s;
        int n = signature.indexOf("(");
        if (n == -1) {
            s = "public void " + signature + "()";
            if (testSignature(s)) {
                return s;
            }
        } else {
            s = "public void " + signature + "";
            if (testSignature(s)) {
                return s;
            }
            s = "public void " + signature.substring(0, n) + "()";
            if (testSignature(s)) {
                return s;
            }
            s = signature.substring(0, n) + "()";
            if (testSignature(s)) {
                return s;
            }
        }
        s = signature;
        if (testSignature(s)) {
            return s;
        }

        return null;
    }

    private static String getSafeFieldSignature(String signature) {
        String s;
        s = "public String " + signature;
        if (testField(s)) {
            return s;
        }
        s = signature;
        if (testField(s)) {
            return s;
        }

        return null;
    }

    private static boolean testSignature(String s) {
        try {
            String code = "class test_rand_6546546465465_sdalkjhf_564 { " + s + "{}}";
            byte[] bytes = code.getBytes("UTF-8");
            InputStream in = new ByteArrayInputStream(bytes);
            JavaParser.parse(in);
            return true;
        } catch (Exception e) {
        }

        return false;
    }

    private static boolean testField(String s) {
        try {
            String code = "class test_rand_6546546465465_sdalkjhf_564 { " + s + ";}";
            byte[] bytes = code.getBytes("UTF-8");
            InputStream in = new ByteArrayInputStream(bytes);
            JavaParser.parse(in);
            return true;
        } catch (Exception e) {
        }

        return false;
    }
}
