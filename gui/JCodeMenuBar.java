package gui;

import java.io.*;
import java.awt.Toolkit;
import java.awt.event.*;
import javax.swing.*;
import gui.TextEditorPanel.*;

public class JCodeMenuBar extends JMenuBar {
    private static final int MASK = 
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    private TextEditorPanel textEditorPanel;
    private DirectoryPanel directoryPanel;
    private ShellPanel shellPanel;
    public JMenuItem newItem;
    public JMenuItem openItem;
    public JMenuItem openReadOnlyItem;
    public JMenuItem openFolderItem;
    public JMenuItem saveItem;
    public JMenuItem saveAsItem;
    public JMenuItem closeItem;
    public JMenuItem exitItem;
    public JMenuItem newTerminalItem;

    public JCodeMenuBar(TextEditorPanel textEditorPanel,
                        DirectoryPanel directoryPanel,
                        ShellPanel shellPanel) {
        this.textEditorPanel = textEditorPanel;
        this.directoryPanel = directoryPanel;
        this.shellPanel = shellPanel;
        initializeFileMenu();
        initializeTerminalMenu();
    }

    private void initializeFileMenu() {
        var fileMenu = new JMenu("File");
        
        newItem = fileMenu.add(new AbstractAction("New") {
            public void actionPerformed(ActionEvent e) {
                textEditorPanel.create();
            }
        });
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, MASK));

        fileMenu.addSeparator();

        openItem = fileMenu.add(new AbstractAction("Open...") {
            public void actionPerformed(ActionEvent e) {
                int choice = textEditorPanel.openChooser.showOpenDialog(textEditorPanel);
                if (choice == JFileChooser.APPROVE_OPTION)
                    textEditorPanel.open(
                        textEditorPanel.openChooser.getSelectedFile(), false);
            }
        });
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, MASK));

        openReadOnlyItem = fileMenu.add(new AbstractAction("Open Read-only...") {
            public void actionPerformed(ActionEvent e) {
                int choice = textEditorPanel.openChooser.showOpenDialog(textEditorPanel);
                if (choice == JFileChooser.APPROVE_OPTION)
                    textEditorPanel.open(
                        textEditorPanel.openChooser.getSelectedFile(), true);
            }
        });

        openFolderItem = fileMenu.add(new OpenFolderAction(
            "Open Folder...", directoryPanel));

        fileMenu.addSeparator();

        saveItem = fileMenu.add(new AbstractAction("Save") {
            public void actionPerformed(ActionEvent e) {
                var pane = textEditorPanel.getEditorPane();
                var currentPane = (NamedScrollPane) pane.getSelectedComponent();
                if (pane.getSelectedComponent() != null) {
                    if (currentPane.identifier.untitledNumber == 0)
                        textEditorPanel.save(SaveOption.SAVE);
                    else
                        textEditorPanel.save(SaveOption.CREATE);
                }
            }            
        });
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, MASK));
        saveItem.setEnabled(false);

        saveAsItem = fileMenu.add(new AbstractAction("Save As...") {
            public void actionPerformed(ActionEvent e) {
                var pane = textEditorPanel.getEditorPane();
                var currentPane = (NamedScrollPane) pane.getSelectedComponent();
                if (pane.getSelectedComponent() != null) {
                    if (currentPane.identifier.untitledNumber == 0)
                        textEditorPanel.save(SaveOption.FORK);
                    else
                        textEditorPanel.save(SaveOption.CREATE);
                }
            }
        });
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 
                                                         MASK | KeyEvent.SHIFT_DOWN_MASK));
        saveAsItem.setEnabled(false);

        fileMenu.addSeparator();

        closeItem = fileMenu.add(new AbstractAction("Close") {
            public void actionPerformed(ActionEvent e) {
                textEditorPanel.save(SaveOption.CLOSE);
            } 
        });
        closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, MASK));
        closeItem.setEnabled(false);

        fileMenu.addSeparator();

        exitItem = fileMenu.add(new AbstractAction("Exit") {
            public void actionPerformed(ActionEvent e) {
                /**
                 * Why do we need to run the following code in a new thread?
                 * 
                 * If we do not do so, the {@code actionPerformed()} method
                 * will execute in the Event Dispatch Thread.
                 * 
                 * Thus we would be calling {@code textEditorPanel.save()}
                 * from the Event Dispatch Thread, and then {@code wait()}.
                 * Since {@code textEditorPanel.save()} further invokes a
                 * {@code SwingWorker}, which eventually spawns a dialog
                 * once again in the Event Dispatch Thread, the dialog may
                 * be blocked by the {@code wait()} call in exactly the
                 * same thread. What we will see is incompletely painted
                 * dialog which would be not responsive.
                 * 
                 * As a remedy we run {@code textEditorPanel.save()} and
                 * {@code wait()} in a different thread from the Event
                 * Dispatch Thread, which would prevent the Event Dispatch
                 * Thread from blocking.
                 */
                
                new Thread(() -> {
                    var pane = textEditorPanel.getEditorPane();
                    synchronized (textEditorPanel.closeLock) {
                        while (pane.getSelectedComponent() != null) {
                            textEditorPanel.save(SaveOption.CLOSE);
                            try {
                                textEditorPanel.closeLock.wait();
                            } catch (InterruptedException ex) {

                            }
                        }
                    }
                    System.exit(0);
                }).start();
            }
        });

        add(fileMenu);
    }

    private void initializeTerminalMenu() {
        var terminalMenu = new JMenu("Terminal");
        newTerminalItem = terminalMenu.add(new AbstractAction("New Terminal") {
            public void actionPerformed(ActionEvent e) {
                shellPanel.spawnShell();
            }
        });
        newTerminalItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, MASK));

        add(terminalMenu);
    }

    public static class OpenFolderAction extends AbstractAction {
        private JFileChooser chooser;
        private DirectoryPanel directoryPanel;
    
        public OpenFolderAction(String name, DirectoryPanel directoryPanel) {
            super(name);
            this.chooser = directoryPanel.getFileChooser();
            this.directoryPanel = directoryPanel;
        }
    
        public void actionPerformed(ActionEvent e) {
            int result = chooser.showOpenDialog(directoryPanel);
    
            if (result == JFileChooser.APPROVE_OPTION) {
                File directory = chooser.getSelectedFile();
                chooser.validate();
    
                try {
                    directoryPanel.setRootPath(directory);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(
                        directoryPanel, "Error: Fail to open folder!", 
                        "Open folder error", JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }
    }
}
