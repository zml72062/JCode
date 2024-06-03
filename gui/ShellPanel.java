package gui;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import shell.ShellRunner;

public abstract class ShellPanel extends JPanel {
    private JTabbedPane shellPane;

    public static final String SHELL_PATH = "/bin/sh";

    /**
     * Every time a command is executed in the shell, something may need
     * to be done with the GUI. (For example, if we create files by shell
     * commands, we need to repaint directory structure.)
     * 
     * Implement this abstract method to specify actions that should ensue
     * a command execution.
     */
    public abstract void actionOnCommandExecution();

    public ShellPanel() {
        setLayout(new BorderLayout());

        // Set margin
        add(new JPanel() {
            public Dimension getPreferredSize() {
                return new Dimension(Parameters.FRAME_MARGIN, 0);
            }
        }, BorderLayout.EAST);
        add(new JPanel() {
            public Dimension getPreferredSize() {
                return new Dimension(0, Parameters.FRAME_MARGIN);
            }
        }, BorderLayout.SOUTH);

        shellPane = new JTabbedPane();
        add(shellPane, BorderLayout.CENTER);

        spawnShell();
    }

    /**
     * Start a shell process in a new tab of the shell panel.
     */
    public void spawnShell() {
        // Create text area
        var textArea = new JTextArea(0, 0);
        textArea.setFont(
            new Font(Font.MONOSPACED, Font.PLAIN, Parameters.DEFAULT_FONT_SIZE));

        // Make previous lines uneditable in text area
        // ref: https://stackoverflow.com/questions/30671911/restricting-access-to-some-lines-in-jtextareas
        ((AbstractDocument) textArea.getDocument())
            .setDocumentFilter(new DocumentFilter() {
            
            private boolean allowChange(int offset) {
                try {
                    int offsetLastLine = textArea.getLineCount() == 0 ?
                        0 : textArea.getLineStartOffset(textArea.getLineCount() - 1);
                    return offset >= offsetLastLine;
                } catch (BadLocationException ex) {
                    throw new RuntimeException(ex);
                }
            }

            public void remove(FilterBypass fb, int offset, int length) 
                throws BadLocationException {
                if (allowChange(offset))
                    super.remove(fb, offset, length);
            }

            public void replace(FilterBypass fb, int offset, int length, 
                String text, AttributeSet attrs) throws BadLocationException {
                if (allowChange(offset))
                    super.replace(fb, offset, length, text, attrs);
            }

            public void insertString(FilterBypass fb, int offset, String string, 
                AttributeSet attr) throws BadLocationException {
                if (allowChange(offset))
                    super.insertString(fb, offset, string, attr);
            }
        });

        // Add text area to panel
        var scrollableTextArea = new JScrollPane(textArea) {
            public Dimension getPreferredSize() {
                return new Dimension(Parameters.FRAME_WIDTH - Parameters.DIRECTORY_PANEL_WIDTH, 
                                     Parameters.SHELL_PANEL_HEIGHT);
            }
        };

        // Add a shell runner
        var runner = new ShellRunner(SHELL_PATH);
        runner.setWorkingDirectory(".");

        // Create a shell daemon to supervise shell execution
        var shellDaemon = new ShellDaemon(runner, textArea, scrollableTextArea);

        // Keyboard shortcuts
        textArea.getInputMap().put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "cache input"
        );
        textArea.getActionMap().put(
            "cache input", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        shellDaemon.commands.offer(textArea.getText().substring(
                            textArea.getLineStartOffset(textArea.getLineCount() - 1)
                        ));
                        textArea.append("\n");
                    } catch (BadLocationException ex) {

                    }
                }
            }
        );
        textArea.getInputMap().put(
            KeyStroke.getKeyStroke("ctrl C"), "send ctrl-C"
        );
        textArea.getActionMap().put(
            "send ctrl-C", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        Runtime.getRuntime().exec(
                            new String[] {"kill", "-1", 
                                Long.valueOf(runner.getShell().pid()).toString()
                            }
                        );
                    } catch (IOException ex) {

                    }
                }
            }
        );
        
        shellPane.add(SHELL_PATH, scrollableTextArea);
        shellDaemon.execute();
    }

    private class ShellDaemon extends SwingWorker<Object, String> {
        private ShellRunner runner;
        private JTextArea textArea;
        private JScrollPane scrollableTextArea;

        public BlockingQueue<String> commands;

        public ShellDaemon(ShellRunner runner, JTextArea textArea,
                           JScrollPane scrollableTextArea) {
            this.runner = runner;
            this.textArea = textArea;
            this.scrollableTextArea = scrollableTextArea;
            commands = new LinkedBlockingQueue<>();
        }

        public Object doInBackground() throws IOException, InterruptedException {
            runner.spawnShell();

            Thread printOutput = new Thread(() -> {
                runner.consumeOutput(this::publish);
            });
            printOutput.setDaemon(true);
            printOutput.start();
    
            Thread getInput = new Thread(() -> {
                while (true) {
                    try {
                        runner.runCommand(() -> {
                            try {
                                return commands.take();
                            } catch (InterruptedException ex) {
                                return null;
                            }
                        });
                        actionOnCommandExecution();
                    } catch (IOException ex) {

                    }
                }
            });
            getInput.setDaemon(true);
            getInput.start();
            
            runner.getShell().waitFor();
            return null;
        }

        public void process(List<String> chunks) {
            for (String output: chunks)
                textArea.append(output + "\n");
        }

        public void done() {
            shellPane.remove(scrollableTextArea);
        }
    }
}
