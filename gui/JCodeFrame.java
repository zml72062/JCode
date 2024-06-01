package gui;

import javax.swing.JFrame;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import file.FileAgent;

import javax.swing.*;

public class JCodeFrame extends JFrame {
    public JCodeFrame() {
        setMinimumSize(new Dimension(Parameters.MIN_FRAME_WIDTH, 
                                     Parameters.MIN_FRAME_HEIGHT));

                add(new DirectoryPanel() {
                    @Override
                    public void actionOnSelectingFile(File file) {
System.out.println(file);
                    }
                }, BorderLayout.WEST);
                // var pane = new JTabbedPane();
                // add(createBrowerPanel(), BorderLayout.WEST);
                // pane.add(createEditorPanel());
                
                // setDefaultMinimumSize();
                add(new JPanel() {
                    {
                        setLayout(new BorderLayout());
                        add(new ShellPanel(), BorderLayout.SOUTH);
                        add(new TextEditorPanel(), BorderLayout.CENTER);
                    }
                }, BorderLayout.CENTER);



        // add(pane, BorderLayout.CENTER);

        // Menu bar

        // currentChooser = new JFileChooser();
        var menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        var fileMenu = new JMenu("File");
        // var openAction = new AbstractAction("Open") {
        //     @Override
        //     public void actionPerformed(ActionEvent e) {
        //         var openStatus = currentChooser.showOpenDialog(EditorFrame.this);
        //         if (openStatus == JFileChooser.APPROVE_OPTION) {
        //             File openedFile = currentChooser.getSelectedFile();
        //             currentFileAgent = new FileAgent();
        //             currentFileAgent.open(openedFile.getPath());
        //             try {
        //                 currentFileAgent.read();
        //             } catch (IOException ex) {
        //                 System.out.println("IOException");
        //             }
        //             if (currentFileAgent.getContent() == null)
        //                 return;
                    
        //             textEditArea.setText(currentFileAgent.getContent());
        //         }
        //     }
        // };
        // fileMenu.add(openAction);
        menuBar.add(fileMenu);
        pack();
    }
}




// class EditorFrame extends JFrame {
//     private static final int MARGIN = 5;
//     private static final int DEFAULT_WIDTH = 800;
//     private static final int DEFAULT_HEIGHT = 600;
//     private static final int MIN_WIDTH = 240;
//     private static final int MIN_HEIGHT = 180;


//     private JTextArea textEditArea;
//     private JFileChooser currentChooser;
//     private FileAgent currentFileAgent;
//     // private FileConsumer currentOpenFileConsumer;

//     /**
//      * A {@code SwingWorker} that internally holds a file. Every instance of
//      * {@code FileConsumer} must implement {@code doInBackground()}, 
//      * {@code done()} and {@code process()}.
//      * 
//      * The method {@code doInBackground()} specifies what to do in a background
//      * thread. During its execution, it may optionally produce in-process data
//      * in the form of {@code Object} instances by calling {@code publish()} on
//      * them. These in-process data will then be passed to {@code process()}. 
//      * At the end of data processing, this method should return a 
//      * {@code StringBuilder} containing the content of the internally-held file, 
//      * which will be passed to {@code done()} for final processing.
//      */
//     public abstract class FileConsumer extends SwingWorker<StringBuilder, Object> {
//         private File file;

//         public FileConsumer(File file) {
//             this.file = file;
//         }

//         public File getFile() {
//             return file;
//         }

//         @Override
//         public abstract StringBuilder doInBackground() throws Exception;

//         @Override
//         public abstract void done();

//         @Override
//         public abstract void process(java.util.List<Object> objects);
//     }

//     public EditorFrame() {
//         var pane = new JTabbedPane();
//         add(createBrowerPanel(), BorderLayout.WEST);
//         pane.add(createEditorPanel());
        
//         setDefaultMinimumSize();
//         add(pane, BorderLayout.CENTER);

//         // Menu bar

//         currentChooser = new JFileChooser();
//         var menuBar = new JMenuBar();
//         setJMenuBar(menuBar);

//         var fileMenu = new JMenu("File");
//         var openAction = new AbstractAction("Open") {
//             @Override
//             public void actionPerformed(ActionEvent e) {
//                 var openStatus = currentChooser.showOpenDialog(EditorFrame.this);
//                 if (openStatus == JFileChooser.APPROVE_OPTION) {
//                     File openedFile = currentChooser.getSelectedFile();
//                     currentFileAgent = new FileAgent();
//                     currentFileAgent.open(openedFile.getPath());
//                     try {
//                         currentFileAgent.read();
//                     } catch (IOException ex) {
//                         System.out.println("IOException");
//                     }
//                     if (currentFileAgent.getContent() == null)
//                         return;
                    
//                     textEditArea.setText(currentFileAgent.getContent());
//                 }
//             }
//         };
//         fileMenu.add(openAction);
//         menuBar.add(fileMenu);
//         pack();
//     }

//     private void setDefaultMinimumSize() {
//         setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
//     }

//     private JPanel createShellPanel() {
//         return null;

//     }

//     private JPanel createBrowerPanel() {
//         var browserPanel = new JPanel() {
//             @Override
//             public Dimension getPreferredSize() {
//                 return new Dimension(160, DEFAULT_HEIGHT);
//             }
//         };

        
        
//         return browserPanel;
//     }

//     // private void addFiles(File root) {
//     //     printf
//     // }

//     private JPanel createEditorPanel() {
        
//     }
// }

