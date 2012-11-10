/*
 * EscapeChars.java
 *
 * Created on October 19, 2007, 4:12 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.sketchlet.common;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Convenience methods for altering special characters related to URLs,
 * regular expressions, and HTML tags.
 */
public final class EscapeChars {
    
    /**
     * Synonym for <tt>URLEncoder.encode(String, "UTF-8")</tt>.
     *
     * <P>Used to ensure that HTTP query strings are in proper form, by escaping
     * special characters such as spaces.
     *
     * <P>It is important to note that if a query string appears in an <tt>HREF</tt>
     * attribute, then there are two issues - ensuring the query string is valid HTTP
     * (it is URL-encoded), and ensuring it is valid HTML (ensuring the ampersand is escaped).
     */
    public static String forURL(String aURLFragment){
        String result = null;
        try {
            result = URLEncoder.encode(aURLFragment, "UTF-8");
        } catch (UnsupportedEncodingException ex){
            throw new RuntimeException("UTF-8 not supported", ex);
        }
        return result;
    }
    
    
    /**
     * Replace characters having special meaning <em>inside</em> HTML tags
     * with their escaped equivalents, using character entities such as <tt>'&amp;'</tt>.
     *
     * <P>The escaped characters are :
     * <ul>
     * <li> <
     * <li> >
     * <li> "
     * <li> '
     * <li> \
     * <li> &
     * </ul>
     *
     * <P>This method ensures that arbitrary text appearing inside a tag does not "confuse"
     * the tag. For example, <tt>HREF='Blah.do?Page=1&Sort=ASC'</tt>
     * does not comply with strict HTML because of the ampersand, and should be changed to
     * <tt>HREF='Blah.do?Page=1&amp;Sort=ASC'</tt>. This is commonly seen in building
     * query strings. (In JSTL, the c:url tag performs this task automatically.)
     */
    public static String forHTMLTag(String aTagFragment){
        final StringBuilder result = new StringBuilder();
        final StringCharacterIterator iterator = new StringCharacterIterator(aTagFragment);
        char character =  iterator.current();
        while (character != CharacterIterator.DONE ){
            if (character == '<') {
                result.append("&lt;");
            } else if (character == '>') {
                result.append("&gt;");
            } else if (character == '\"') {
                result.append("&quot;");
            } else if (character == '\'') {
                result.append("&#039;");
            } else if (character == '\\') {
                result.append("&#092;");
            } else if (character == '&') {
                result.append("&amp;");
            } else {
                //the char is not a special one
                //add it to the result as is
                result.append(character);
            }
            character = iterator.next();
        }
        return result.toString();
    }
    
    /**
     * Return <tt>aText</tt> with all start-of-tag and end-of-tag characters
     * replaced by their escaped equivalents.
     *
     * <P>If user input may contain tags which must be disabled, then call
     * this method, not {@link #forHTMLTag}. This method is used for text appearing
     * <em>outside</em> of a tag, while {@link #forHTMLTag} is used for text appearing
     * <em>inside</em> an HTML tag.
     *
     * <P>It is not uncommon to see text on a web page presented erroneously, because
     * <em>all</em> special characters are escaped (as in {@link #forHTMLTag}), instead of
     * just the start-of-tag and end-of-tag characters. In
     * particular, the ampersand character is often escaped not once but <em>twice</em> :
     * once when the original input occurs, and then a second time when the same item is
     * retrieved from the database. This occurs because the ampersand is the only escaped
     * character which appears in a character entity.
     */
    public static String toDisableTags(String aText){
        final StringBuilder result = new StringBuilder();
        final StringCharacterIterator iterator = new StringCharacterIterator(aText);
        char character =  iterator.current();
        while (character != CharacterIterator.DONE ){
            if (character == '<') {
                result.append("&lt;");
            } else if (character == '>') {
                result.append("&gt;");
            } else {
                //the char is not a special one
                //add it to the result as is
                result.append(character);
            }
            character = iterator.next();
        }
        return result.toString();
    }
    
    /**
     * Escape characters in the same way as the <c:out> tag in JSTL.
     *
     * <P>The following characters are replaced with character entities : < > " ' &
     */
    public static String forHTMLTextFlow(String aText){
        final StringBuilder result = new StringBuilder();
        final StringCharacterIterator iterator = new StringCharacterIterator(aText);
        char character =  iterator.current();
        while (character != CharacterIterator.DONE ){
            if (character == '<') {
                result.append("&lt;");
            } else if (character == '>') {
                result.append("&gt;");
            } else if (character == '\"') {
                result.append("&quot;");
            } else if (character == '\'') {
                result.append("&#039;");
            } else if (character == '&') {
                result.append("&amp;");
            } else {
                //the char is not a special one
                //add it to the result as is
                result.append(character);
            }
            character = iterator.next();
        }
        return result.toString();
    }
    
    /**
     * Replace characters having special meaning in regular expressions
     * with their escaped equivalents.
     *
     * <P>The escaped characters include :
     *<ul>
     *<li>.
     *<li>\
     *<li>?, * , and +
     *<li>&
     *<li>:
     *<li>{ and }
     *<li>[ and ]
     *<li>( and )
     *<li>^ and $
     *</ul>
     *
     */
    public static String forRegex(String aRegexFragment){
        final StringBuilder result = new StringBuilder();
        
        final StringCharacterIterator iterator = new StringCharacterIterator(aRegexFragment);
        char character =  iterator.current();
        while (character != CharacterIterator.DONE ){
      /*
       * All literals need to have backslashes doubled.
       */
            if (character == '.') {
                result.append("\\.");
            } else if (character == '\\') {
                result.append("\\\\");
            } else if (character == '?') {
                result.append("\\?");
            } else if (character == '*') {
                result.append("\\*");
            } else if (character == '+') {
                result.append("\\+");
            } else if (character == '&') {
                result.append("\\&");
            } else if (character == ':') {
                result.append("\\:");
            } else if (character == '{') {
                result.append("\\{");
            } else if (character == '}') {
                result.append("\\}");
            } else if (character == '[') {
                result.append("\\[");
            } else if (character == ']') {
                result.append("\\]");
            } else if (character == '(') {
                result.append("\\(");
            } else if (character == ')') {
                result.append("\\)");
            } else if (character == '^') {
                result.append("\\^");
            } else if (character == '$') {
                result.append("\\$");
            } else {
                //the char is not a special one
                //add it to the result as is
                result.append(character);
            }
            character = iterator.next();
        }
        return result.toString();
    }
    
    /**
     * Disable all <tt><SCRIPT></tt> tags in <tt>aText</tt>.
     *
     * <P>Insensitive to case.
     */
    public static String forScriptTagsOnly(String aText){
        String result = null;
        Matcher matcher = SCRIPT.matcher(aText);
        result = matcher.replaceAll("&lt;SCRIPT>");
        matcher = SCRIPT_END.matcher(result);
        result = matcher.replaceAll("&lt;/SCRIPT>");
        return result;
    }
    
    // PRIVATE //
    
    private EscapeChars(){
        //empty - prevent construction
    }
    
    private static final Pattern SCRIPT = Pattern.compile("<SCRIPT>", Pattern.CASE_INSENSITIVE);
    private static final Pattern SCRIPT_END = Pattern.compile("</SCRIPT>", Pattern.CASE_INSENSITIVE);
    public static String prepareForXML(
            String strText) {
        if (strText != null) {
            return getEsc(strText, false);
        } else {
            return "";
        }

    }

    private static String getEsc(String str, boolean isAttVal) {
        String result = "";
        for (int i = 0; i <
                str.length(); i++) {
            char ch = str.charAt(i);
            switch (ch) {
                case '&':
                    result += "&amp;";
                    break;

                case '\'':
                    result += "&#039;";
                    break;

                case '\\':
                    result += "&#092;";
                    break;

                case '<':
                    result += "&lt;";
                    break;

                case '>':
                    result += "&gt;";
                    break;

                case '\"':
                    if (isAttVal) {
                        result += "&quot;";
                    } else {
                        result += '\"';
                    }

                    break;
                case '\n':
                    result += "\\n";

                    break;
                case '\r':
                    result += "\\r";

                    break;
                default:
                    if (ch > '\u007f') {
                        result += "&#";
                        result +=
                                Integer.toString(ch);
                        result +=
                                ';';
                    } else {
                        result += ch;
                    }

            }
        }

        return result;
    }
}

