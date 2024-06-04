package gui;

import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.UndoableEditEvent;

public class JCodeFrame extends JFrame {
    private DirectoryPanel directoryPanel;
    private TextEditorPanel textEditorPanel;
    private ShellPanel shellPanel;
    private JCodeMenuBar menuBar;

    public JCodeFrame() {
        setMinimumSize(new Dimension(Parameters.MIN_FRAME_WIDTH, 
                                     Parameters.MIN_FRAME_HEIGHT));
        
        add(directoryPanel = new DirectoryPanel() {
            public void actionOnSelectingFile(File file) {
                textEditorPanel.open(file, false);
            }
        }, BorderLayout.WEST);

        add(new JPanel() {
            {
                setLayout(new BorderLayout());
                add(shellPanel = new ShellPanel() {
                    public void actionOnCommandExecution() {
                        File rootPath;
                        if ((rootPath = directoryPanel.getRootPath()) != null) {
                            try {
                                directoryPanel.setRootPath(rootPath);
                            } catch (IOException ex) {

                            }
                        }
                    };
                }, BorderLayout.SOUTH);
                add(textEditorPanel = new TextEditorPanel() {
                    public void actionOnCreatingFile() {
                        File rootPath;
                        if ((rootPath = directoryPanel.getRootPath()) != null) {
                            try {
                                directoryPanel.setRootPath(rootPath);
                            } catch (IOException ex) {

                            }
                        }
                    }

                    public void actionOnNonzeroTabs() {
                        menuBar.saveItem.setEnabled(true);
                        menuBar.saveAsItem.setEnabled(true);
                        menuBar.closeItem.setEnabled(true);
                        menuBar.undoItem.setEnabled(true);
                        menuBar.redoItem.setEnabled(true);
                        menuBar.cutItem.setEnabled(true);
                        menuBar.copyItem.setEnabled(true);
                        menuBar.pasteItem.setEnabled(true);
                    }

                    public void actionOnZeroTabs() {
                        menuBar.saveItem.setEnabled(false);
                        menuBar.saveAsItem.setEnabled(false);
                        menuBar.closeItem.setEnabled(false);
                        menuBar.undoItem.setEnabled(false);
                        menuBar.redoItem.setEnabled(false);
                        menuBar.cutItem.setEnabled(false);
                        menuBar.copyItem.setEnabled(false);
                        menuBar.pasteItem.setEnabled(false);
                    }

                    public void undoableEditHappened(UndoableEditEvent e) {
                        menuBar.undo.addEdit(e.getEdit());
                        menuBar.undoAction.updateUndoState();
                        menuBar.redoAction.updateRedoState();
                    }
                }, BorderLayout.CENTER);
            }
        }, BorderLayout.CENTER);

        setJMenuBar(menuBar = new JCodeMenuBar(textEditorPanel, 
                                               directoryPanel,
                                               shellPanel));
        pack();
    }
}

