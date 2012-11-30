package net.sf.sketchlet.net;

import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;
import org.apache.log4j.Logger;

import java.net.URLDecoder;
import java.util.StringTokenizer;

/**
 * @author Omnibook
 */
public class DataReceiver {
    private static final Logger log = Logger.getLogger(DataReceiver.class);

    public DataReceiver() {
    }

    public void updateVariable(String updateCommand, boolean bEncode) {
        if (VariablesBlackboard.isPaused()) {
            return;
        }

        int n;
        while ((n = updateCommand.indexOf('\0')) >= 0) {
            updateCommand = updateCommand.substring(n + 1);
        }

        if (bEncode) {
            StringTokenizer tokenizer = new StringTokenizer(updateCommand, " \t\n");
            if (tokenizer.countTokens() >= 2) {
                String prefix = tokenizer.nextToken();
                String variableName = tokenizer.nextToken();
                String value = "";
                String group = "";
                String description = "";

                try {
                    value = URLDecoder.decode(tokenizer.nextToken(), "UTF-8");
                    group = URLDecoder.decode(tokenizer.nextToken(), "UTF-8");
                    description = URLDecoder.decode(tokenizer.nextToken(), "UTF-8");
                } catch (Exception e) {
                }
                if (prefix.equalsIgnoreCase("UPDATE-DIRECT")) {
                    VariablesBlackboard.getInstance().updateVariable(variableName, value, group, description);
                } else if (prefix.equalsIgnoreCase("UPDATE")) {
                    VariablesBlackboard.getInstance().updateVariable(variableName, value, group, description);
                } else if (prefix.equalsIgnoreCase("DELETE")) {
                    if (variableName.contains("*")) {
                        VariablesBlackboard.getInstance().removeVariables(variableName);
                    } else {
                        VariablesBlackboard.getInstance().removeVariable(variableName);
                    }
                } else {
                    log.info("Command '" + updateCommand + "' not recognized.");
                }
            } else {
                log.info("Wrong number of arguments in '" + updateCommand + "'");
            }
        } else {
            int nu = updateCommand.indexOf(" ");
            if (nu > 0) {
                String prefix = updateCommand.substring(0, nu);
                updateCommand = updateCommand.substring(nu + 1);
                nu = updateCommand.indexOf(" ");

                if (nu > 0) {
                    String variableName = updateCommand.substring(0, nu);
                    String value = updateCommand.substring(nu + 1);

                    if (prefix.equalsIgnoreCase("UPDATE-DIRECT")) {
                        VariablesBlackboard.getInstance().updateVariable(variableName, value);
                    } else if (prefix.equalsIgnoreCase("UPDATE")) {
                        VariablesBlackboard.getInstance().updateVariable(variableName, value);
                    } else if (prefix.equalsIgnoreCase("DELETE")) {
                        if (variableName.contains("*")) {
                            VariablesBlackboard.getInstance().removeVariables(variableName);
                        } else {
                            VariablesBlackboard.getInstance().removeVariable(variableName);
                        }
                    } else {
                        log.info("Command '" + updateCommand + "' not recognized.");
                    }
                }
            }
        }
    }

}


