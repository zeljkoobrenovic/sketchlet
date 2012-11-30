package net.sf.sketchlet.framework.model.programming.macros;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;

/**
 *
 * @author zeljko
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
                macro.setName(value);
                return true;
            } else if (relativePath.equalsIgnoreCase("repeat")) {
                try {
                    macro.setRepeat((int) Double.parseDouble(value));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                return true;
            } else if (relativePath.equalsIgnoreCase("action/type")) {
                macro.getActions()[actionIndex][0] = value;
                return true;
            } else if (relativePath.equalsIgnoreCase("action/param1")) {
                macro.getActions()[actionIndex][1] = value;
                return true;
            } else if (relativePath.equalsIgnoreCase("action/param2")) {
                macro.getActions()[actionIndex][2] = value;
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
