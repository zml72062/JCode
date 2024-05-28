import java.awt.*;
import javax.swing.*;

// A simple text editor
public class Main {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            var frame = new EditorFrame();
            frame.setTitle("JCode");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}

class EditorFrame extends JFrame {
    private static final int MARGIN = 5;
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;
    private static final int MIN_WIDTH = 80;
    private static final int MIN_HEIGHT = 60;

    public EditorFrame() {
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        var textEditPanel = new JPanel();
        textEditPanel.setLayout(new BorderLayout());

        // Set margin
        textEditPanel.add(new JPanel() {
            public Dimension getPreferredSize() {
                return new Dimension(MARGIN, 0);
            }
        }, BorderLayout.WEST);
        textEditPanel.add(new JPanel() {
            public Dimension getPreferredSize() {
                return new Dimension(MARGIN, 0);
            }
        }, BorderLayout.EAST);
        textEditPanel.add(new JPanel() {
            public Dimension getPreferredSize() {
                return new Dimension(0, MARGIN);
            }
        }, BorderLayout.SOUTH);

        var textEditArea = new JTextArea(0, 0);
        textEditArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 20));
        var scrollableTextEditArea = new JScrollPane(textEditArea) {
            public Dimension getPreferredSize() {
                return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            }
        };
        textEditPanel.add(scrollableTextEditArea, BorderLayout.CENTER);
        
        add(textEditPanel);

        // Menu bar

        var menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        
        pack();
    }
}
