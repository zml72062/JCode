package shell;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * A {@code ShellRunner} runs a Unix shell and handles its I/O.
 * 
 * Notice that the functionality of this class is NOT supported on
 * machines running Windows.
 */
public class ShellRunner {
    private Process shell;
    private Scanner shellOutput;
    private OutputStreamWriter shellInput;
    private String shellPath;

    public ShellRunner(String shellPath) {
        this.shellPath = Objects.requireNonNull(shellPath);
    }

    /**
     * Spawn a process in which a Unix shell is running. 
     * @throws IOException if an I/O error occurs
     */
    public void spawnShell() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(shellPath)
            .directory(new File("."))
            .redirectErrorStream(true);

        // there should be no shell running previously
        assert shell == null;
        shell = pb.start();

        // connect shell input/output to JVM
        shellOutput = new Scanner(shell.getInputStream());
        shellInput = new OutputStreamWriter(shell.getOutputStream());

        // print current working directory
        synchronized (shellInput) {
            shellInput.write("__pwd__=$(pwd); echo $__pwd__' >'\n");
            shellInput.flush();
        }
    }

    /**
     * Run command in the shell process, with command string provided by
     * {@code commandSupplier}.
     * @param commandSupplier a {@code Supplier<String>} object that provides
     * the command
     * @throws IOException if an I/O error occurs
     */
    public void runCommand(Supplier<String> commandSupplier)
        throws IOException {
        String command = commandSupplier.get();
        synchronized (shellInput) {
            shellInput.write(command + "\n" + 
                            "__pwd__=$(pwd); echo $__pwd__' >'\n");
            shellInput.flush();
        }
    }

    /**
     * Fetch output from the shell process, and consume them via
     * {@code outputConsumer}.
     * @param outputConsumer a {@code Consumer<String>} object that consumes
     * a line of shell output
     */
    public void consumeOutput(Consumer<String> outputConsumer) {
        while (true) {
            if (!shellOutput.hasNextLine())
                break;
            outputConsumer.accept(shellOutput.nextLine());
        }
    }

    public Process getShell() {
        return shell;
    }
}

