package gui;

import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.*;
import file.FileAgent;
import file.FileAgent.NotTextFileException;

public abstract class TextEditorPanel extends JPanel {
    private JTabbedPane editorPane;
    private final AtomicInteger untitledCount = new AtomicInteger(0);
    private final HashMap<EditorIdentifier, EditorContentManager> 
        managerMap = new HashMap<>();
    private NamedScrollPane currentCreatingPane;
    private JTextPane currentCreatingTextArea;
    private JFileChooser saveChooser;
    
    public JFileChooser openChooser;
    public final Object closeLock;
    public enum SaveOption {
        SAVE, CREATE, FORK, CLOSE
    }

    /**
     * Implement this abstract method to specify what to do when a file is
     * created.
     */
    public abstract void actionOnCreatingFile();

    /**
     * Implement this abstract method to specify what to do when tab count
     * becomes nonzero.
     */
    public abstract void actionOnNonzeroTabs();

    /**
     * Implement this abstract method to specify what to do when tab count
     * becomes zero.
     */
    public abstract void actionOnZeroTabs();

    /**
     * Implement this abstract method to provide {@code UndoableEditListener}
     * for document.
     */
    public abstract void undoableEditHappened(UndoableEditEvent e);

    public TextEditorPanel() {
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
        add(new JPanel() {
            public Dimension getPreferredSize() {
                return new Dimension(0, Parameters.FRAME_MARGIN);
            }
        }, BorderLayout.NORTH);

        editorPane = new JTabbedPane();
        add(editorPane, BorderLayout.CENTER);

        // Show confirm dialog when user trying to overwrite existing file
        // ref: https://stackoverflow.com/questions/3651494/jfilechooser-with-confirmation-dialog
        saveChooser = new JFileChooser() {
            public void approveSelection() {
                File f = getSelectedFile();
                if (f.exists() && getDialogType() == SAVE_DIALOG) {
                    int result = JOptionPane.showConfirmDialog(this, 
                        "The file exists, overwrite?",
                        "Existing file",
                        JOptionPane.YES_NO_OPTION);
                    switch (result) {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        default:
                            return;
                    }
                }
                super.approveSelection();
            }
        };
        saveChooser.setCurrentDirectory(new File("."));

        // Add a JFileChooser for opening file
        openChooser = new JFileChooser();
        openChooser.setCurrentDirectory(new File("."));

        // Global lock to ensure sequential execution of multiple close 
        // operations
        closeLock = new Object();
    }

    public void create() {
        int count = untitledCount.incrementAndGet();
        EditorContentManager manager;
        synchronized (this) {
            createEditorTab(count, null);
            manager = new EditorContentManager(currentCreatingTextArea, 
                                               currentCreatingPane);
            managerMap.put(new EditorIdentifier(count, null), manager);
            editorPane.setSelectedComponent(currentCreatingPane);
        }
        actionOnNonzeroTabs();
        manager.create();
        manager.execute();
    }

    public void open(File file, boolean readOnly) {
        EditorContentManager manager;
        synchronized (this) {
            if ((manager = managerMap.get(new EditorIdentifier(0, file)))
                        != null) {
                editorPane.setSelectedComponent(manager.textPane);
                return;
            }
            createEditorTab(0, file);
            manager = new EditorContentManager(currentCreatingTextArea, 
                                               currentCreatingPane);
            managerMap.put(new EditorIdentifier(0, file), manager);
            editorPane.setSelectedComponent(currentCreatingPane);
        }
        actionOnNonzeroTabs();
        manager.open(file, readOnly);
        manager.execute();
    }

    public void save(SaveOption option) {
        var selectedPane = editorPane.getSelectedComponent();
        if (selectedPane == null)
            return;
        
        var identifier = ((NamedScrollPane) selectedPane).identifier;
        EditorContentManager manager;
        synchronized (this) {
            manager = managerMap.get(identifier);
        }

        synchronized (manager) {
            manager.saveOption = option;
            manager.notifyAll();
        }
    }

    public JTabbedPane getEditorPane() {
        return editorPane;
    }

    private void createEditorTab(int untitledNumber, File file) {
        // Create text area with line wrap disabled
        // ref: https://www.coderanch.com/t/332983/java/Stop-text-wrapping
        currentCreatingTextArea = new JTextPane() {
            public boolean getScrollableTracksViewportWidth() {
                return getSize().width < getParent().getSize().width;
            }
  
            public void setSize(Dimension d) {
                if (d.width < getParent().getSize().width) {
                    d.width = getParent().getSize().width;
                }
                super.setSize(d);
            }
        };
        currentCreatingTextArea.setFont(
            new Font(Font.MONOSPACED, Font.PLAIN, Parameters.DEFAULT_FONT_SIZE));
        currentCreatingTextArea.getStyledDocument()
            .addUndoableEditListener(this::undoableEditHappened);
        currentCreatingPane = new NamedScrollPane(currentCreatingTextArea) {
            {
                identifier = new EditorIdentifier(untitledNumber, file);
            }
            public Dimension getPreferredSize() {
                return new Dimension(Parameters.FRAME_WIDTH - Parameters.DIRECTORY_PANEL_WIDTH, 
                                     Parameters.FRAME_HEIGHT - Parameters.SHELL_PANEL_HEIGHT);
            }
        };

        Prettifier.setPrettifierAction(currentCreatingTextArea);
        String title = currentCreatingPane.identifier.toString();
        editorPane.add(title, currentCreatingPane);
    }

    public class EditorIdentifier {
        public int untitledNumber;  // non-zero for untitled document
        public File file;           // non-null for titled document

        public EditorIdentifier(int untitledNumber, File file) {
            this.untitledNumber = untitledNumber;
            this.file = file;
        }

        public String toString() {
            if (untitledNumber > 0)
                return "Untitled-" + untitledNumber;
            try {
                return file.getCanonicalPath();
            } catch (IOException ex) {
                return file.getAbsolutePath();
            }
        }

        public boolean equals(Object obj) {
            if (this == obj) // same identity
                return true;
            if (obj == null)
                return false;
            if (obj.getClass() != getClass())
                return false;

            EditorIdentifier objIdentifier = (EditorIdentifier) obj;
            return untitledNumber == objIdentifier.untitledNumber &&
                (untitledNumber > 0 ||
                 Objects.equals(toString(), objIdentifier.toString()));
        }

        public int hashCode() {
            return toString().hashCode();
        }
    }

    public class NamedScrollPane extends JScrollPane {
        public EditorIdentifier identifier;
        public JTextPane component;

        public NamedScrollPane(JComponent c) {
            super(c);
            component = (JTextPane) c;
        }
    }

    private class EditorContentManager extends SwingWorker<Object, String> {
        private FileAgent agent;
        private boolean readOnly;
        private JTextPane textArea;
        private NamedScrollPane textPane;
        private SaveOption saveOption;

        public EditorContentManager(JTextPane textArea, NamedScrollPane textPane) {
            this.textArea = textArea;
            this.textPane = textPane;
            agent = new FileAgent();
        }

        public void create() {
            agent.create();
        }

        public void open(File file, boolean readOnly) {
            agent.open(file.getAbsolutePath());
            this.readOnly = readOnly;
            if (readOnly)
                textArea.setEditable(false);
        }

        public Object doInBackground() 
            throws IOException, InterruptedException {
            if (agent.getFile() != null) {
                // Read from file
                try {
                    agent.read();
                } catch (NotTextFileException ex) {
                    JOptionPane.showMessageDialog(
                        TextEditorPanel.this, 
                        "Error: Not text file!", 
                        "Open file error", JOptionPane.ERROR_MESSAGE
                    );
                    synchronized (TextEditorPanel.this) {
                        managerMap.remove(textPane.identifier);
                        editorPane.remove(textPane);
                    }
                    return null;
                }
                if (readOnly)
                    publish(agent.getContent());
                else
                    publish(agent.editContent().toString());
            }
            boolean closed = false;
            while (true) {
                SaveOption option;
                synchronized (this) {
                    wait();
                    // After awakened, check {@code saveOption}.
                    option = saveOption;
                }
                if (!readOnly) {
                    var content = agent.editContent();
                    if (content.length() == 0)
                        content.append(textArea.getText());
                    else
                        content.replace(0, content.length(), 
                                        textArea.getText());
                    switch (option) {
                        case SAVE:
                            agent.write();
                            break;
                        case CREATE:
                            saveNew();
                            actionOnCreatingFile();
                            break;
                        case FORK:
                            forkNew();
                            actionOnCreatingFile();
                            actionOnNonzeroTabs();
                            break;
                        case CLOSE:
                            synchronized (closeLock) {
                                int result = JOptionPane.showConfirmDialog(TextEditorPanel.this, 
                                    "Save file?",
                                    "Save",
                                    JOptionPane.YES_NO_CANCEL_OPTION);
                                switch (result) {
                                    case JOptionPane.CANCEL_OPTION:
                                        continue;
                                    case JOptionPane.YES_OPTION:
                                        if (agent.getFile() == null)
                                            saveNew();
                                        else
                                            agent.write();
                                        // fall through
                                    case JOptionPane.NO_OPTION:
                                        synchronized (TextEditorPanel.this) {
                                            managerMap.remove(textPane.identifier);
                                            editorPane.remove(textPane);
                                        }
                                        break;
                                }
                                closed = true;
                            }
                            break;
                    }
                } else {
                    switch (option) {
                        case FORK:
                            forkNew();
                            actionOnCreatingFile();
                            actionOnNonzeroTabs();
                            break;
                        case CLOSE:
                            synchronized (closeLock) {
                                synchronized (TextEditorPanel.this) {
                                    managerMap.remove(textPane.identifier);
                                    editorPane.remove(textPane);
                                }
                                closed = true;
                            }
                        default:
                            break;
                    }
                }
                if (closed) {
                    synchronized (closeLock) {
                        closeLock.notifyAll();
                    }
                    break;
                } 
            }
            return null;
        }

        private void saveNew() throws IOException {
            if (saveChooser.showSaveDialog(TextEditorPanel.this) != 
                    JFileChooser.APPROVE_OPTION)
                return;

            agent.saveAs(saveChooser.getSelectedFile()
                                    .getAbsolutePath());

            // Rename tab and modify {@code managerMap}
            synchronized (TextEditorPanel.this) {
                managerMap.remove(textPane.identifier);
                var oldName = textPane.identifier.toString();
                textPane.identifier.untitledNumber = 0;
                textPane.identifier.file = agent.getFile();
                EditorContentManager oldManager;
                if ((oldManager = managerMap.get(textPane.identifier))
                                != null) {
                    managerMap.remove(textPane.identifier);
                    editorPane.remove(oldManager.textPane);
                }
                managerMap.put(textPane.identifier, this);
                editorPane.setTitleAt(
                    editorPane.indexOfTab(oldName),
                    textPane.identifier.toString()
                );
            }
        }

        private void forkNew() throws IOException {
            if (saveChooser.showSaveDialog(TextEditorPanel.this) !=
                    JFileChooser.APPROVE_OPTION)
                return;

            var newAgent = agent.forkAs(saveChooser
                .getSelectedFile().getAbsolutePath());

            EditorContentManager oldManager, newManager;
            EditorIdentifier newIdentifier 
                = new EditorIdentifier(0, newAgent.getFile());
            synchronized (TextEditorPanel.this) {
                if ((oldManager = managerMap.get(newIdentifier)) 
                               != null) {
                    managerMap.remove(newIdentifier);
                    newManager = new EditorContentManager(oldManager.textArea, 
                                                          oldManager.textPane);
                } else {
                    createEditorTab(0, newAgent.getFile());
                    newManager = new EditorContentManager(currentCreatingTextArea, 
                                                          currentCreatingPane);
                }
                newManager.agent = newAgent;
                managerMap.put(newIdentifier, newManager);
                editorPane.setSelectedComponent(newManager.textPane);
            }
            newManager.execute();
        }

        public void process(List<String> chunks) {
            var document = textArea.getStyledDocument();
            try {
                document.remove(0, document.getLength());
                document.insertString(0, chunks.get(chunks.size() - 1), null);
            } catch (BadLocationException ex) {
                JOptionPane.showMessageDialog(
                    TextEditorPanel.this, 
                    "Error: Fail to open file!", 
                    "Open file error", JOptionPane.ERROR_MESSAGE
                );
                synchronized (TextEditorPanel.this) {
                    managerMap.remove(textPane.identifier);
                    editorPane.remove(textPane);
                }
                return;
            }
            Prettifier.prettify(currentCreatingTextArea);
        }

        public void done() {
            if (editorPane.getSelectedComponent() == null)
                actionOnZeroTabs();
        }
    }
}

