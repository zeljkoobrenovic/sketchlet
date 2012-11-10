package net.sf.sketchlet.designer.programming.macros;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;

/**
 * Created with IntelliJ IDEA.
 * User: zeljko
 * Date: 25-10-12
 * Time: 10:21
 * To change this template use File | Settings | File Templates.
 */
public class MacroSaxParserUtil {

    private Macro macro;
    private String pathPrefix;
    private int actionIndex = -1;

    public MacroSaxParserUtil(Macro macro, String pathPrefix) {
        this.macro = macro;
        if (!pathPrefix.endsWith("/")) {
            pathPrefix += "/";
        }
        this.pathPrefix = pathPrefix;
    }

    private String namedParameterName = "";

    public boolean startElement(String path, Attributes atts) {
        if (path.startsWith(pathPrefix)) {
            String relativePath = path.substring((pathPrefix).length());

            if (relativePath.equalsIgnoreCase("action")) {
                actionIndex++;
                return true;
            }
        }
        return false;
    }

    public boolean processCharacters(String path, String value) {
        if (path.startsWith(pathPrefix)) {
            String relativePath = path.substring((pathPrefix).length());

            if (relativePath.equalsIgnoreCase("name")) {
                macro.name = value;
                return true;
            } else if (relativePath.equalsIgnoreCase("repeat")) {
                try {
                    macro.repeat = (int) Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                return true;
            } else if (relativePath.equalsIgnoreCase("action/type")) {
                macro.actions[actionIndex][0] = value;
                return true;
            } else if (relativePath.equalsIgnoreCase("action/param1")) {
                macro.actions[actionIndex][1] = value;
                return true;
            } else if (relativePath.equalsIgnoreCase("action/param2")) {
                macro.actions[actionIndex][2] = value;
                return true;
            } else if (relativePath.equalsIgnoreCase("named-parameters/named-parameter/parameter-name")) {
                namedParameterName = value;
                return true;
            } else if (relativePath.equalsIgnoreCase("named-parameters/named-parameter/parameter-value")) {
                if (StringUtils.isNotBlank(namedParameterName)) {
                    macro.getParameters().put(namedParameterName, value);
                }
                return true;
            }
        }
        return false;
    }

    public boolean endElement(String path) {
        return path.startsWith(pathPrefix);
    }
}
