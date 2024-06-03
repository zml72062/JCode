import java.awt.*;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            var frame = new gui.JCodeFrame();
            frame.setTitle("JCode");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}

