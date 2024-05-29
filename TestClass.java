import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Scanner;

public class TestClass {

    public static void main(final String[] args) {
        final TestClass ts = new TestClass();
    }
    
    private BufferedWriter w;
    private Scanner r;
    
    public TestClass() {
        // Start the process using process builder
        final String programLocation = "/bin/bash";
        final ProcessBuilder pb = new ProcessBuilder().redirectErrorStream(true);
        pb.command(programLocation);
        Process process;
        try {
            process = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
            process = null;
            // killProgram();
        }
    
        // Build your own wrappers for communicating with the program.
        w = new BufferedWriter(
                new OutputStreamWriter(process.getOutputStream()));
        r = new Scanner(process.getInputStream());
    
        // Print the five starting messages.
        // printFromBuffer();
        // printFromBuffer();
        // printFromBuffer();
        // printFromBuffer();
        // printFromBuffer();
    
        // Run the following three commands in Maxima
        runCommand("ls\n");
        runCommand("cd ..\n");
    }
    
    /**
     * Runs the given string and prints out the returned answer.
     */
    private void runCommand(final String s) {
        try {
            w.write(s);
            w.flush();
            while (r.hasNextLine()) {
                System.out.println("message");
                printFromBuffer();
            }
            System.out.println("end");

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
    
    private void printFromBuffer() {
        final String s = r.nextLine();
        System.out.println(s + " -blah");
    }
}