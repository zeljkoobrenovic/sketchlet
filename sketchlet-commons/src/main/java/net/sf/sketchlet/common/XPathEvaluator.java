/*
 * XPathEvaluator.java
 *
 * Created on October 7, 2006, 8:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.sketchlet.common;

import com.sun.org.apache.xml.internal.utils.PrefixResolver;
import com.sun.org.apache.xml.internal.utils.PrefixResolverDefault;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

/**
 *
 * @author obrenovi
 */
public class XPathEvaluator {

    private DocumentBuilderFactory factory;
    private DocumentBuilder builder;
    private XPath xpath;
    private static String encoding = "UTF-8";
    private Document document;
    private Node node;

    /** Creates a new instance of XPathEvaluator */
    public XPathEvaluator(Document document) {
        this();
        this.setDocument(document);
    }

    public XPathEvaluator(Node node) {
        this();
        this.node = node;
    }
    NamespaceContext ctx = new NamespaceContext() {

        public String getNamespaceURI(String prefix) {
            if (getDocument() != null) {
                try {
                    PrefixResolver resolver = new PrefixResolverDefault(getDocument().getDocumentElement());
                    return resolver.getNamespaceForPrefix(prefix);
                } catch (Exception e) {
                }
            }
            return "http://" + prefix;
        }

        public Iterator getPrefixes(String val) {
            return null;
        }

        public String getPrefix(String uri) {
            return null;
        }
    };

    /** Creates a new instance of XPathEvaluator */
    public XPathEvaluator() {
        try {
            factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setExpandEntityReferences(false);
            factory.setIgnoringComments(true);
            factory.setValidating(false);
            builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new ErrorHandler() {

                public void warning(SAXParseException exception) throws SAXException {
                }

                public void error(SAXParseException exception) throws SAXException {
                }

                public void fatalError(SAXParseException exception) throws SAXException {
                }
            });
            xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(ctx);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public static String getEncoding() {
        return encoding;
    }

    public static void setEncoding(String encoding) {
        XPathEvaluator.encoding = encoding;
    }

    public void createDocumentFromInputStream(InputStream input) {
        this.node = null;
        this.setDocument(null);

        try {
            this.setDocument(builder.parse(input));
        } catch (SAXException sxe) {
            sxe.printStackTrace(System.out);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public void createDocumentFromString(String content) {
        try {
            this.createDocumentFromInputStream(new ByteArrayInputStream(content.toString().getBytes(this.getEncoding())));
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public void createDocumentFromFile(File file) {
        this.node = null;
        this.setDocument(null);

        try {
            FileInputStream fis = new FileInputStream(file);
            this.setDocument(builder.parse(fis));
            fis.close();
        } catch (SAXException sxe) {
            System.out.println(file.getPath());
            sxe.printStackTrace(System.out);
        } catch (FileNotFoundException fnfe) {
            System.out.println("File '" + file.getName() + "' could not be found. Creating an empty document.");
            this.setDocument(builder.newDocument());
        } catch (Exception e) {
            System.out.println(file.getPath());
            e.printStackTrace(System.out);
        }
    }

    public void createDocumentFromFile(String fileUrl) {
        this.node = null;
        this.setDocument(null);

        try {
            this.setDocument(builder.parse(new URL(fileUrl).openStream()));
        } catch (SAXException sxe) {
            sxe.printStackTrace(System.out);
        } catch (FileNotFoundException fnfe) {
            System.out.println("File '" + fileUrl + "' could not be found. Creating an empty document.");
            this.setDocument(builder.newDocument());
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public String getString(String expression) {
        if (this.getDocument() == null && this.node == null) {
            return "";
        }
        String value = "";
        try {
            if (this.getDocument() != null) {
                value = (String) xpath.evaluate(expression, this.getDocument(), XPathConstants.STRING);
            } else {
                value = (String) xpath.evaluate(expression, this.node, XPathConstants.STRING);
            }
        } catch (XPathExpressionException xpee) {
            xpee.printStackTrace(System.out);
        }

        return value;
    }

    public double getDouble(String expression) {
        if (this.getDocument() == null && this.node == null) {
            return 0.0;
        }
        double value = 0.0;
        try {
            if (this.getDocument() != null) {
                value = ((Double) xpath.evaluate(expression, this.getDocument(), XPathConstants.NUMBER)).doubleValue();
            } else {
                value = ((Double) xpath.evaluate(expression, this.node, XPathConstants.NUMBER)).doubleValue();
            }
        } catch (XPathExpressionException xpee) {
            xpee.printStackTrace(System.out);
        }

        return value;
    }

    public int getInteger(String expression) {
        return (int) this.getDouble(expression);
    }

    public NodeList getNodes(String expression) {
        if (this.getDocument() == null && this.node == null) {
            return null;
        }
        NodeList value = null;
        try {
            if (this.getDocument() != null) {
                value = (NodeList) xpath.evaluate(expression, this.getDocument(), XPathConstants.NODESET);
            } else {
                value = (NodeList) xpath.evaluate(expression, this.node, XPathConstants.NODESET);
            }
        } catch (XPathExpressionException xpee) {
            xpee.printStackTrace(System.out);
        }

        return value;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}
