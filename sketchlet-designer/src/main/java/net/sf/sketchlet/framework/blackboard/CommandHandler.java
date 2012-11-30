package net.sf.sketchlet.framework.blackboard;

/**
 * @author zobrenovic
 */
public class CommandHandler {

    // VAR variable | group | This is an new variable
    public static void processAddVarCommand(String line) {
        String strLine = line.substring(7).trim();
        String name = "";
        String group = "";
        String desc = "";
        String varDesc[] = strLine.split("\\|");
        if (varDesc.length >= 1) {
            name = varDesc[0].trim();
        }
        if (varDesc.length >= 2) {
            group = varDesc[1].trim();
        }
        if (varDesc.length >= 3) {
            desc = varDesc[2].trim();
        }

        if (VariablesBlackboard.getInstance() != null && !VariablesBlackboard.getInstance().isPaused()) {
            VariablesBlackboard.getInstance().addVariable(name, group, desc);
        }
    }

    public static void processSetCommand(String line, boolean newThread) {
        String strLine = line.substring(4).trim();
        if (strLine.isEmpty()) {
            return;
        }
        String name;
        String value = "";
        int n = strLine.indexOf(" ");

        if (n > 0) {
            name = strLine.substring(0, n);
            if (n < strLine.length()) {
                value = strLine.substring(n + 1);
                value = value.replace("\\r", "\r");
                value = value.replace("\\n", "\n");
            }
        } else {
            name = strLine;
        }

        if (VariablesBlackboard.getInstance() != null && !VariablesBlackboard.getInstance().isPaused()) {
            VariablesBlackboard.getInstance().updateVariable(name, value, newThread);
        }
    }

    public static void processSetArrayCommand(String line, boolean newThread) {
        String strLine = line.substring(5).trim();
        if (strLine.isEmpty()) {
            return;
        }
        String commands[] = strLine.split(";");
        for (int i = 0; i < commands.length; i++) {
            String strCommand = commands[i];
            String name = "";
            String value = "";
            int n = strCommand.indexOf(" ");

            if (n > 0) {
                name = strCommand.substring(0, n);
                if (n < strCommand.length()) {
                    value = strCommand.substring(n + 1);
                    value = value.replace("\\r", "\r");
                    value = value.replace("\\n", "\n");
                }
            } else {
                name = strCommand;
            }

            if (VariablesBlackboard.getInstance() != null && !VariablesBlackboard.getInstance().isPaused()) {
                VariablesBlackboard.getInstance().updateVariable(name, value, newThread);
            }
        }
    }
}
