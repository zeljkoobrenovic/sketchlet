package com.sun.tools.internal.xjc.runtime;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class JAXBContextFactory {
    private static final String DOT_OBJECT_FACTORY = ".ObjectFactory";
    private static final String IMPL_DOT_OBJECT_FACTORY = ".impl.ObjectFactory";

    /**
     * The JAXB API will invoke this method via reflection
     */
    public static JAXBContext createContext(Class[] classes, Map properties) throws JAXBException {
        Class[] r = new Class[classes.length];
        boolean modified = false;

        // find any reference to our 'public' ObjectFactory and
        // replace that to our 'private' ObjectFactory.
        for (int i = 0; i < r.length; i++) {
            Class c = classes[i];
            String name = c.getName();
            if (name.endsWith(DOT_OBJECT_FACTORY)
                    && !name.endsWith(IMPL_DOT_OBJECT_FACTORY)) {
                // we never generate into the root package, so no need to worry about FQCN "ObjectFactory"

                // if we find one, tell the real JAXB provider to
                // load foo.bar.impl.ObjectFactory
                name = name.substring(0, name.length() - DOT_OBJECT_FACTORY.length()) + IMPL_DOT_OBJECT_FACTORY;

                try {
                    c = c.getClassLoader().loadClass(name);
                } catch (ClassNotFoundException e) {
                    throw new JAXBException(e);
                }

                modified = true;
            }

            r[i] = c;
        }

        if (!modified) {
            // if the class list doesn't contain any of our classes,
            // this ContextFactory shouldn't have been called in the first place
            // if we simply continue, we'll just end up with the infinite recursion.

            // the only case that I can think of where this could happen is
            // when the user puts additional classes into the JAXB-generated
            // package and pass them to JAXBContext.newInstance().
            // Under normal use, this shouldn't happen.

            // anyway, bail out now.
            // if you hit this problem and wondering how to get around the problem,
            // subscribe and send a note to users@jaxb.dev.java.net (http://jaxb.dev.java.net/)
            throw new JAXBException("Unable to find a JAXB implementation to delegate");
        }

        // delegate to the JAXB provider in the system
        return JAXBContext.newInstance(r, properties);
    }


    /**
     * The JAXB API will invoke this method via reflection
     */
    public static JAXBContext createContext(String contextPath,
                                            ClassLoader classLoader, Map properties) throws JAXBException {

        List<Class> classes = new ArrayList<Class>();
        StringTokenizer tokens = new StringTokenizer(contextPath, ":");

        // each package should be pointing to a JAXB RI generated
        // content interface package.
        //
        // translate them into a list of private ObjectFactories.
        try {
            while (tokens.hasMoreTokens()) {
                String pkg = tokens.nextToken();
                classes.add(classLoader.loadClass(pkg + IMPL_DOT_OBJECT_FACTORY));
            }
        } catch (ClassNotFoundException e) {
            throw new JAXBException(e);
        }

        // delegate to the JAXB provider in the system
        return JAXBContext.newInstance(classes.toArray(new Class[classes.size()]), properties);
    }
}
