package gui;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import file.FileEnumerator;
import file.FileEnumerator.DirectoryListData;

public abstract class DirectoryPanel extends JPanel {
    private File root;
    private DefaultMutableTreeNode rootNode;
    private JFileChooser chooser;
    private JTree tree;
    private JScrollPane treePane;
    private JLabel label;
    private JPanel innerPanel;
    private JButton openFolderButton;

    /**
     * Implement this abstract method to specify the action when a file is
     * selected from the directory structure view. 
     * @param file the file selected
     */
    public abstract void actionOnSelectingFile(File file);

    public DirectoryPanel() {
        setLayout(new BorderLayout());

        // Set margin
        add(new JPanel() {
            public Dimension getPreferredSize() {
                return new Dimension(Parameters.FRAME_MARGIN, 0);
            }
        }, BorderLayout.EAST);
        add(new JPanel() {
            public Dimension getPreferredSize() {
                return new Dimension(Parameters.FRAME_MARGIN, 0);
            }
        }, BorderLayout.WEST);
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

        // Add inner components
        initializeFileChooser();
        initializeLabel();
        initializeButtons();
        add(
            new JPanel() {
                {
                    setLayout(new BorderLayout());
                    add(openFolderButton, BorderLayout.SOUTH);
                    add(innerPanel, BorderLayout.CENTER);
                }
            }, BorderLayout.CENTER
        );
    }

    public Dimension getPreferredSize() {
        return new Dimension(Parameters.DIRECTORY_PANEL_WIDTH,
                             Parameters.FRAME_HEIGHT);
    }

    private void initializeFileChooser() {
        chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("."));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }

    private void initializeLabel() {
        // Add a message string when no folder is open
        label = new JLabel("No open folder.");
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 
                            Parameters.MESSAGE_FONT_SIZE));
        label.setHorizontalAlignment(JLabel.CENTER);

        // Add the message string in {@code innerPanel}
        innerPanel = new JPanel() {
            {
                setLayout(new GridLayout());
                add(label);
            }
        };
    }

    private void initializeButtons() {
        // Put button below {@code innerPanel}
        openFolderButton = new JButton("Open Folder...");
        openFolderButton.setFont(
            new Font(Font.SANS_SERIF, Font.PLAIN, Parameters.MESSAGE_FONT_SIZE)
        );

        // Set action listener
        openFolderButton.addActionListener(e -> {
            int result = chooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File directory = chooser.getSelectedFile();
                chooser.validate();

                try {
                    setRootPath(directory);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(
                        this, "Error: Fail to open folder!", 
                        "Open folder error", JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
    }

    /**
     * Set {@code rootPath} as the root of the directory structure, and 
     * update GUI.
     * @param rootPath a {@code File} object representing the root of
     * directory structure
     * @throws IOException
     */
    private void setRootPath(File rootPath) throws IOException {
        root = new File(rootPath.getCanonicalPath());
        rootNode = new DefaultMutableTreeNode(root);

        var oldTree = tree;

        tree = new JTree(rootNode, true) {
            public String convertValueToText(Object value, 
                                             boolean selected, 
                                             boolean expanded, 
                                             boolean leaf, 
                                             int row,
                                             boolean hasFocus) {
                if (value != null) {
                    String sValue = ((File)((DefaultMutableTreeNode)value)
                        .getUserObject()).getName();
                    if (sValue != null)
                        return sValue;
                }
                return "";
            }
        };
        tree.addTreeSelectionListener(e -> {
            TreePath path = tree.getSelectionPath();
            if (path == null)
                return;

            var selectedFile = (File)((DefaultMutableTreeNode) 
                path.getLastPathComponent()).getUserObject();
            
            if (selectedFile.isFile())
                actionOnSelectingFile(selectedFile);
        });
        tree.getSelectionModel()
            .setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        if (oldTree == null)
            innerPanel.remove(label);
        else
            innerPanel.remove(treePane);
        
        innerPanel.add(treePane = new JScrollPane(tree));
        innerPanel.validate();

        new DirectoryListWorker(root, rootNode, tree).execute();
    }

    private class DirectoryListWorker 
        extends SwingWorker<Object, DirectoryListData> {
        private HashMap<File, DefaultMutableTreeNode> nodeMap;
        private FileEnumerator enumerator;
        private JTree tree;

        public DirectoryListWorker(File root, DefaultMutableTreeNode rootNode,
                                   JTree tree) {
            this.nodeMap = new HashMap<>();
            nodeMap.put(root, rootNode);
            this.enumerator = new FileEnumerator(root);
            this.tree = tree;
        }

        public Object doInBackground() {
            DirectoryListData children;
            while ((children = enumerator.enumerate()) != null)
                publish(children);
            return null;
        }

        public void process(List<DirectoryListData> chunks) {
            int size = chunks.size();
            for (int i = 0; i < size; i++) {
                DirectoryListData result = chunks.get(i);
                DefaultMutableTreeNode rootNode = nodeMap.get(result.root);
    
                for (File child: result.children) {
                    DefaultMutableTreeNode childNode;
                    if (child.isDirectory())
                        childNode = new DefaultMutableTreeNode(child, true);
                    else
                        childNode = new DefaultMutableTreeNode(child, false);
    
                    rootNode.add(childNode);
                    nodeMap.put(child, childNode);
                }
            }
            
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            model.reload(rootNode);
        }
    }
}

