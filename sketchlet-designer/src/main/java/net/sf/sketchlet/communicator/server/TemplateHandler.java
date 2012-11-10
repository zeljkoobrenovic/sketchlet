/*
 * TemplateHandler.java
 *
 * Created on April 21, 2008, 2:23 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.sketchlet.communicator.server;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;


public abstract class TemplateHandler {

    public Hashtable<String, Vector<Template>> commandTemplates = new Hashtable<String, Vector<Template>>();
    public Hashtable<String, Vector<Template>> commandDiffTemplates = new Hashtable<String, Vector<Template>>();
    public Hashtable<String, Vector<Template>> commandDiffTemplatesFirstTime = new Hashtable<String, Vector<Template>>();

    public TemplateHandler() {
    }

    public void processTemplates(String triggerVariables[]) {
        if (triggerVariables == null) {
            return;
        }

        for (String triggerVariable : triggerVariables) {
            this.processTemplates(triggerVariable);
        }
    }

    public void processTemplates(String triggerVariable) {
        this.processTemplates(this.commandTemplates.get(triggerVariable));
    }

    /**
     * Diff templates are the templates that are used only when the value is changed
     * regarded of fact that it vas updated. For example, we can derive value adult based on
     * the age, with many values for age that will produce the same value for audult.
     */
    public void processDiffTemplates(String triggerVariables[]) {
        if (triggerVariables == null) {
            return;
        }

        for (String triggerVariable : triggerVariables) {
            this.processDiffTemplates(triggerVariable);
        }
    }

    public void processDiffTemplates(String triggerVariable) {
        this.processTemplates(this.commandDiffTemplates.get(triggerVariable));
    }

    /**
     * We also need to inform the clinets when the value is updated for the first time, as it is new
     * for the clients. Therefore, we will inform the clients first time when the value is updated,
     * even though it is not changed.
     * IMPORTANT: This method whould be called AFTER processDiffTemplates.
     */
    public void processDiffTemplatesFirstTime(String triggerVariables[]) {
        if (triggerVariables == null) {
            return;
        }
        for (String triggerVariable : triggerVariables) {
            this.processDiffTemplatesFirstTime(triggerVariable);
        }
    }

    public void processDiffTemplatesFirstTime(String triggerVariable) {
        Vector<Template> templates = this.commandDiffTemplatesFirstTime.get(triggerVariable);

        if (templates != null) {
            this.processTemplates(templates);

            this.commandDiffTemplatesFirstTime.remove(triggerVariable);
            this.commandDiffTemplates.put(triggerVariable, templates);
        }
    }

    public void processTemplates(Vector<Template> templates) {
        if (templates != null) {
            for (Template template : templates) {
                if (template.test()) {
                    this.sendTemplate(template);
                }
            }
        }
    }

    public abstract void sendTemplate(Template template);

    public String processAdditionalParameters(String commandTemplate) {
        return commandTemplate;
    }

    public Template createTemplate() {
        return new Template();
    }

    public void processTemplateCommand(String line) throws IOException {
        if (line.startsWith("ADD TEMPLATE ") || line.startsWith("REGISTER ")) {
            String commandTemplate;
            Hashtable<String, Vector<Template>> templateHashtable;

            if (line.startsWith("ADD TEMPLATE DIFF ")) {
                commandTemplate = line.substring(17).trim();
                templateHashtable = this.commandDiffTemplatesFirstTime;
            } else if (line.startsWith("ADD TEMPLATE ")) {
                commandTemplate = line.substring(13).trim();
                templateHashtable = this.commandTemplates;
            } else if (line.startsWith("REGISTER DIFF ")) {
                commandTemplate = line.substring(14).trim();
                templateHashtable = this.commandDiffTemplatesFirstTime;
            } else if (line.startsWith("REGISTER ")) {
                commandTemplate = line.substring(9).trim();
                templateHashtable = this.commandTemplates;
            } else {
                return;
            }

            commandTemplate = this.processAdditionalParameters(commandTemplate);

            int position = commandTemplate.indexOf(' ');

            if (position > 0) {
                String template = commandTemplate.substring(position).trim();
                String triggerVariables = commandTemplate.substring(0, position).trim();
                StringTokenizer tokenizer = new StringTokenizer(triggerVariables, ",");

                while (tokenizer.hasMoreTokens()) {
                    String triggerVariable = URLDecoder.decode(tokenizer.nextToken(), "UTF-8");
                    String test = null;

                    int n1 = triggerVariable.indexOf("[");
                    int n2 = triggerVariable.indexOf("]");

                    if (n1 > 0 && n2 > n1) {
                        test = triggerVariable.substring(n1 + 1, n2);
                        triggerVariable = triggerVariable.substring(0, n1);
                    }

                    Vector<Template> templates = templateHashtable.get(triggerVariable);
                    if (templates == null) {
                        templates = new Vector<Template>();
                    }

                    Template newTemplate = this.createTemplate();

                    newTemplate.template = template;
                    newTemplate.variable = triggerVariable;
                    newTemplate.test = test;

                    templates.add(newTemplate);

                    templateHashtable.put(triggerVariable, templates);
                }
            } else {
                String triggerVariables = commandTemplate;
                StringTokenizer tokenizer = new StringTokenizer(triggerVariables, ",");

                while (tokenizer.hasMoreTokens()) {
                    String triggerVariable = URLDecoder.decode(tokenizer.nextToken(), "UTF-8");
                    String template = "<%=" + triggerVariable + "%>";
                    String test = null;

                    int n1 = triggerVariable.indexOf("[");
                    int n2 = triggerVariable.indexOf("]");

                    if (n1 > 0 && n2 > n1) {
                        test = triggerVariable.substring(n1 + 1, n2);
                        triggerVariable = triggerVariable.substring(0, n1);
                    }

                    Vector<Template> templates = templateHashtable.get(triggerVariable);
                    if (templates == null) {
                        templates = new Vector<Template>();
                    }

                    Template newTemplate = this.createTemplate();

                    newTemplate.template = template;
                    newTemplate.variable = triggerVariable;
                    newTemplate.test = test;

                    templates.add(newTemplate);

                    templateHashtable.put(triggerVariable, templates);
                }
            }
        }
    }
}
