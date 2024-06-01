package gui;

import java.awt.*;
import javax.swing.*;

public class ShellPanel extends JPanel {
    private JTextArea textArea;

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

        // Create text area
        textArea = new JTextArea(0, 0);
        textArea.setFont(
            new Font(Font.MONOSPACED, Font.PLAIN, Parameters.DEFAULT_FONT_SIZE));
        var scrollableTextArea = new JScrollPane(textArea) {
            public Dimension getPreferredSize() {
                return new Dimension(Parameters.FRAME_WIDTH - Parameters.DIRECTORY_PANEL_WIDTH, 
                                     Parameters.SHELL_PANEL_HEIGHT);
            }
        };
        add(scrollableTextArea, BorderLayout.CENTER);
    }
}
