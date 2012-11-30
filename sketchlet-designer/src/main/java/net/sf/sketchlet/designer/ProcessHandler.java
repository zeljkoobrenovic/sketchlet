package net.sf.sketchlet.designer;

import net.sf.sketchlet.common.QuotedStringTokenizer;
import net.sf.sketchlet.common.system.PlatformManager;
import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;
import net.sf.sketchlet.designer.editor.ui.desktop.ProcessConsolePanel;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

/**
 * @author cuypers
 */
public class ProcessHandler implements Runnable, net.sf.sketchlet.context.VariableUpdateListener {
    private static final Logger log = Logger.getLogger(ProcessHandler.class);
    private Thread t = new Thread(this);
    private static Runtime runtime = Runtime.getRuntime();
    private Process process;
    private String command;
    private String workingDirectory;
    private int offset;
    private ProcessConsolePanel processConsolePanel;
    private List processes;
    private String id;
    private boolean running = false;

    private PrintWriter outStream;

    /**
     * Creates a new instance of Workspace
     */
    public ProcessHandler(String id, String command, String workingDirectory, int offset, List processes, ProcessConsolePanel processConsolePanel) {
        this.id = id;
        this.command = command;
        this.workingDirectory = workingDirectory;
        this.offset = offset;
        this.processes = processes;
        this.processConsolePanel = processConsolePanel;

        t.start();
    }

    public void setStatus(String status) {
        this.processConsolePanel.status = status;
        if (Workspace.getMainPanel() != null && !Workspace.getProcessRunner().getIoServicesHandler().isProgramEnding()) {
            Workspace.getMainPanel().refreshData(true);
        }
    }

    @Override
    public void variableUpdated(String variableName, String value) {
        if (variableName.trim().equals("")) {
            return;
        }
        if (this.outStream == null) {
            return;
        }

        String inVariable = this.processConsolePanel.inVariableField.getText().trim();
        if (!inVariable.equals("") && variableName.equals(inVariable)) {
            value = value.replace("\\n", "\n");
            outStream.print(value);
            outStream.flush();
        }

        if (!this.running) {
            String startCondition = this.processConsolePanel.startOnField.getText().trim();
            if (!startCondition.equals("")) {
                int n = startCondition.indexOf(" ");
                if (n == -1) {
                    n = startCondition.indexOf("=");
                }

                boolean startProcess = false;

                if (n == -1) {
                    startProcess = variableName.equalsIgnoreCase(startCondition);
                } else {
                    String variable = startCondition.substring(0, n).trim();
                    String condValue = startCondition.substring(n + 1).trim();

                    startProcess = variableName.equalsIgnoreCase(variable) && condValue.equalsIgnoreCase(value);
                }

                if (startProcess) {
                    this.processConsolePanel.startProcess();
                }
            }
        } else {
            String stopCondition = this.processConsolePanel.stopOnField.getText().trim();
            if (!stopCondition.equals("")) {
                int n = stopCondition.indexOf(" ");
                if (n == -1) {
                    n = stopCondition.indexOf("=");
                }

                boolean stopProcess = false;

                if (n == -1) {
                    stopProcess = variableName.equalsIgnoreCase(stopCondition);
                } else {
                    String variable = stopCondition.substring(0, n).trim();
                    String condValue = stopCondition.substring(n + 1).trim();

                    stopProcess = variableName.equalsIgnoreCase(variable) && condValue.equalsIgnoreCase(value);
                }

                if (stopProcess) {
                    this.processConsolePanel.stopProcess();
                }
            }
        }
        // VariablesBlackboard.unprotectVariable(triggerVariable);
    }

    public void run() {
        if (command == null || command.trim().equals("")) {
            addLine("Empty command.");
            running = false;
            return;
        }

        running = true;

        try {
            if (offset > 0) {
                this.setStatus("waiting");
                addLine("Waiting " + (this.offset / 1000.0) + " seconds...");
                Thread.sleep(this.offset);
            }

            boolean openFile = false;

            if (command.toLowerCase().startsWith("open ")) {
                command = new File(command.substring(5).trim()).getAbsolutePath();

                command = PlatformManager.getDefaultFileOpenerCommand().replace("$f", command);

                openFile = true;
            }

            this.setStatus("executing");
            addLine("Executing: " + command);
            addLine("Working directory: " + workingDirectory);

            if (openFile) {
                setProcess(Runtime.getRuntime().exec(command));
            } else {
                String args[] = QuotedStringTokenizer.parseArgs(command);

                ProcessBuilder processBuilder = new ProcessBuilder(args);
                if (this.workingDirectory != null && !this.workingDirectory.trim().equals("")) {
                    processBuilder = processBuilder.directory(new File(workingDirectory));
                }
                processBuilder.redirectErrorStream(true);
                setProcess(processBuilder.start());
            }

            synchronized (processes) {
                this.processes.add(getProcess());
            }
            BufferedReader inStream = new BufferedReader(new InputStreamReader(getProcess().getInputStream()));
            outStream = new PrintWriter(new OutputStreamWriter(getProcess().getOutputStream()));

            if (VariablesBlackboard.getInstance() != null) {
                VariablesBlackboard.getInstance().addVariablesUpdateListener(this);
            }

            this.setStatus("running");

            String line;
            try {
                while ((line = inStream.readLine()) != null) {
                    addLine(line);
                }
            } catch (Exception e) {
                log.error(e);
                addLine(e.getMessage());
            }
        } catch (Exception e) {
            log.error(e);
        }

        if (getProcess() != null) {
            synchronized (processes) {
                this.processes.remove(getProcess());
                this.getProcess().destroy();
                this.setProcess(null);
            }
        }

        this.processConsolePanel.stop.setEnabled(false);
        this.processConsolePanel.start.setEnabled(true);

        addLine("Process finished.");

        this.setStatus("finished");

        running = false;

        if (VariablesBlackboard.getInstance() != null) {
            VariablesBlackboard.getInstance().removeVariablesUpdateListener(this);
        }
    }

    private synchronized void addLine(String line) {
        if (Workspace.isShowGUI()) {
            this.processConsolePanel.textArea.append(line + "\n");
            this.processConsolePanel.textArea.setSelectionStart(this.processConsolePanel.textArea.getText().length() - line.length());
            this.processConsolePanel.textArea.setSelectionEnd(this.processConsolePanel.textArea.getText().length());

            String outVariable = this.processConsolePanel.outVariableField.getText().trim();
            if (!outVariable.equals("")) {
                VariablesBlackboard.getInstance().updateVariable(outVariable, line, this.id, "Console output of process " + this.processConsolePanel.titleField.getText());
            }
        } else {
            log.info("[" + this.id + "]: " + line);
        }
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }
}
