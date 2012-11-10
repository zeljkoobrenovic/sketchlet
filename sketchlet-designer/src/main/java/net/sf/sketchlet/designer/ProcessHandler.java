/*
 * ProcessHandler.java
 *
 * Created on November 11, 2006, 4:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer;

import net.sf.sketchlet.common.QuotedStringTokenizer;
import net.sf.sketchlet.common.system.PlatformManager;
import net.sf.sketchlet.designer.ui.desktop.ProcessConsolePanel;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * @author cuypers
 */
public class ProcessHandler implements Runnable, net.sf.sketchlet.context.VariableUpdateListener {
    private static final Logger log = Logger.getLogger(ProcessHandler.class);
    Thread t = new Thread(this);
    static Runtime runtime = Runtime.getRuntime();
    public Process theProcess;
    String command;
    String workingDirectory;
    int offset;
    public ProcessConsolePanel output;
    Vector processes;
    String id;
    boolean running = false;

    /**
     * Creates a new instance of Workspace
     */
    public ProcessHandler(String id, String command, String workingDirectory, int offset, Vector processes, ProcessConsolePanel output) {
        this.id = id;
        this.command = command;
        this.workingDirectory = workingDirectory;
        this.offset = offset;
        this.processes = processes;
        this.output = output;

        t.start();
    }

    public void setStatus(String status) {
        this.output.status = status;
        if (Workspace.mainPanel != null && !Workspace.programEnding) {
            Workspace.mainPanel.refreshData(true);
        }
    }

    PrintWriter outStream;

    @Override
    public void variableUpdated(String variableName, String value) {
        if (variableName.trim().equals("")) {
            return;
        }
        if (this.outStream == null) {
            return;
        }

        String inVariable = this.output.inVariableField.getText().trim();
        if (!inVariable.equals("") && variableName.equals(inVariable)) {
            value = value.replace("\\n", "\n");
            outStream.print(value);
            outStream.flush();
        }

        if (!this.running) {
            String startCondition = this.output.startOnField.getText().trim();
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
                    this.output.startProcess();
                }
            }
        } else {
            String stopCondition = this.output.stopOnField.getText().trim();
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
                    this.output.stopProcess();
                }
            }
        }
        // DataServer.unprotectVariable(triggerVariable);
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
                theProcess = Runtime.getRuntime().exec(command);
            } else {
                String args[] = QuotedStringTokenizer.parseArgs(command);

                ProcessBuilder processBuilder = new ProcessBuilder(args);
                if (this.workingDirectory != null && !this.workingDirectory.trim().equals("")) {
                    processBuilder = processBuilder.directory(new File(workingDirectory));
                }
                processBuilder.redirectErrorStream(true);
                theProcess = processBuilder.start();
            }

            synchronized (processes) {
                this.processes.add(theProcess);
            }
            BufferedReader inStream = new BufferedReader(new InputStreamReader(theProcess.getInputStream()));
            outStream = new PrintWriter(new OutputStreamWriter(theProcess.getOutputStream()));

            if (net.sf.sketchlet.communicator.server.DataServer.variablesServer != null) {
                net.sf.sketchlet.communicator.server.DataServer.variablesServer.addVariablesUpdateListener(this);
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

        if (theProcess != null) {
            synchronized (processes) {
                this.processes.remove(theProcess);
                this.theProcess.destroy();
                this.theProcess = null;
            }
        }

        this.output.stop.setEnabled(false);
        this.output.start.setEnabled(true);

        addLine("Process finished.");

        this.setStatus("finished");

        running = false;

        if (net.sf.sketchlet.communicator.server.DataServer.variablesServer != null) {
            net.sf.sketchlet.communicator.server.DataServer.variablesServer.removeVariablesUpdateListener(this);
        }
    }

    private synchronized void addCharacter(int c) {
        if (Workspace.showGUI) {
            this.output.textArea.append("" + (char) c);

            this.output.textArea.setSelectionStart(this.output.textArea.getText().length() - 1);
            this.output.textArea.setSelectionEnd(this.output.textArea.getText().length());
        } else {
            System.out.print(c);
        }
    }

    private synchronized void addLine(String line) {
        if (Workspace.showGUI) {
            this.output.textArea.append(line + "\n");
            this.output.textArea.setSelectionStart(this.output.textArea.getText().length() - line.length());
            this.output.textArea.setSelectionEnd(this.output.textArea.getText().length());

            String outVariable = this.output.outVariableField.getText().trim();
            if (!outVariable.equals("")) {
                net.sf.sketchlet.communicator.server.DataServer.variablesServer.updateVariable(outVariable, line, this.id, "Console output of process " + this.output.titleField.getText());
            }
        } else {
            log.info("[" + this.id + "]: " + line);
        }
    }
}
