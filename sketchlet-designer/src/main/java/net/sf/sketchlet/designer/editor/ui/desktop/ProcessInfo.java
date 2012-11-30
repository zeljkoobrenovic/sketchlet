package net.sf.sketchlet.designer.editor.ui.desktop;

import java.util.List;

public class ProcessInfo {
    private final String identifier;
    private final String titleText;
    private final String description;
    private final String commandOriginal;
    private final String workingDirectory;
    private final List processes;
    private final int offset;
    private final boolean autoStart;
    private final String startCondition;
    private final String stopCondition;
    private final String outVariable;
    private final String inVariable;

    public ProcessInfo(String identifier, String titleText, String description, String commandOriginal, String workingDirectory, List processes, int offset, boolean autoStart, String startCondition, String stopCondition, String outVariable, String inVariable) {
        this.identifier = identifier;
        this.titleText = titleText;
        this.description = description;
        this.commandOriginal = commandOriginal;
        this.workingDirectory = workingDirectory;
        this.processes = processes;
        this.offset = offset;
        this.autoStart = autoStart;
        this.startCondition = startCondition;
        this.stopCondition = stopCondition;
        this.outVariable = outVariable;
        this.inVariable = inVariable;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getTitleText() {
        return titleText;
    }

    public String getDescription() {
        return description;
    }

    public String getCommandOriginal() {
        return commandOriginal;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public List getProcesses() {
        return processes;
    }

    public int getOffset() {
        return offset;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public String getStartCondition() {
        return startCondition;
    }

    public String getStopCondition() {
        return stopCondition;
    }

    public String getOutVariable() {
        return outVariable;
    }

    public String getInVariable() {
        return inVariable;
    }
}
