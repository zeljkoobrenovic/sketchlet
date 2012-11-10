/*
 * QuotedStringTokenizer.java
 *
 * Created on March 26, 2008, 2:23 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.sketchlet.common;

/*
 * This breaks a string into tokens.
 * This is a little better than java.util.QuotedStringTokenizer because
 * We handle quoted strings.
 *
 * This is used to implement getargs in InText.
 *
 * The difference from the TextArea is that we capture the <nl>
 * character and transmit the char to the socket.
 */
import java.util.Vector;

/**
 * String tokenizer.
 * 
 * This tokenizer is a little more powerfule the java.util.QuotedStringTokenizer
 * because it can handle quoted tokens.
 * 
 * Interface:
 *    QuotedStringTokenizer(String s): read tokens from s
 *    boolean AtEol(): no more tokens left
 *    String NextToken(): get next token
 *    String[] parseTokens(): gett all the tokens
 *    static String[] parseArgs(String s): get all the tokens
 */
public class QuotedStringTokenizer {

    /**
     * Remember the string and the current position.
     */
    int index, length;
    String str;
    Character split;

    /**
     * Create a tokenizer from a string.
     * Start from the front.
     */
    public QuotedStringTokenizer(String s) {
        str = s;
        index = 0;
        length = s.length();
    }

    /**
     * String tokenizer from a specific char
     */
    public QuotedStringTokenizer(String s, char c) {
        this(s);
        split = new Character(c);
    }

    /**
     * Test if a char is a space or not
     */
    private boolean isWhitespace(char c) {
        boolean rval;

        if (split == null) {
            rval = Character.isWhitespace(c);
        } else {
            rval = c == split.charValue();
        }
        return rval;
    }

    /**
     * Return the index of the first nonwhite char.
     */
    private void skipWhite() {
        while (index != length) {
            if (isWhitespace(str.charAt(index)) == false) {
                break;
            }
            index++;
        }
    }

    /**
     * Read a quoted string.
     * Guaranteed the first char is a double quote.
     */
    private String readQuotedString() {
        StringBuffer buffer = new StringBuffer();

        // Skip initial quote
        index++;

        // Collect until next quote
        loop:
        while (index != length) {
            char c = str.charAt(index);
            switch (c) {
                case '"':
                    index++;
                    break loop;
                /*
                case '\\':
                if(index < length - 1)
                index++;
                c = str.charAt(index);
                switch(c) {
                case 't':
                c = '\t';
                break;
                case 'r':
                c = '\r';
                break;
                case 'n':
                c = '\n';
                break;
                }
                buffer.append(c);
                break;
                 */
                default:
                    buffer.append(c);
                    break;
            }
            index++;
        }

        // Create the string
        return buffer.toString();
    }

    /**
     * Read until the next white space.
     */
    private String readString() {
        int start = index;
        while (index != length) {
            if (isWhitespace(str.charAt(index))) {
                break;
            }
            index++;
        }
        return str.substring(start, index);
    }

    /**
     * Read until the end of line.
     */
    private String readRest() {
        int start = index;
        index = length;
        return str.substring(start, length);
    }

    /**
     * See if at eol.
     */
    public boolean atEol() {
        skipWhite();
        return index == length;
    }

    /**
     * Read the next token.
     */
    public String nextToken() {
        String arg;

        // Skip white, then read one of the token types
        skipWhite();
        if (index == length) {
            return "";
        }
        char c = str.charAt(index);
        if (c == '"') {
            arg = readQuotedString();
        } else {
            arg = readString();
        }
        return arg;
    }

    /**
     * parse the entire line.
     */
    public String[] parseTokens(int count) {
        Vector argv = new Vector(10);
        String arg;
        int i, argc;

        // Loop until all tockens are collected
        argc = 0;
        while (index != length) {
            skipWhite();
            if (index == length) {
                break;
            }
            if (argc == count) {
                // The last argument takes the rest of the line
                arg = readRest();
            } else {
                // Determine if a quoted argument
                char c = str.charAt(index);
                if (c == '"') {
                    arg = readQuotedString();
                } else {
                    arg = readString();
                }
            }
            argv.addElement(arg);
            argc++;
        }

        // Now convert to a string array
        String[] args = new String[argc];
        for (i = 0; i != argc; i++) {
            args[i] = (String) argv.elementAt(i);
        }
        return args;
    }

    /**
     * Turn the line into a list of words.
     */
    public static String[] parseArgs(String command) {
        QuotedStringTokenizer token = new QuotedStringTokenizer(command);
        return token.parseTokens(-1);
    }

    /**
     * Turn the line into a list of words,
     * split by the character in the argument.
     */
    public static String[] parseArgs(String command, char c) {
        QuotedStringTokenizer token = new QuotedStringTokenizer(command, c);
        return token.parseTokens(-1);
    }

    /**
     * Turn the line into a list of words.
     * Expect argc normal arguments, the collect the rest as a single arg.
     */
    public static String[] parseArgs(String command, int argc) {
        QuotedStringTokenizer token = new QuotedStringTokenizer(command);
        return token.parseTokens(argc);
    }

    public static void main(String args[]) {
        args = parseArgs("\"[a.position x]]\"");
        System.out.println(args.length);
        for (String s : args) {
            System.out.println(s);
        }
    }
}
