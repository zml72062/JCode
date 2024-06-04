package gui;

import java.awt.Color;
import java.awt.event.*;
import java.util.HashSet;
import java.util.concurrent.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.text.*;

public class Prettifier {
    private static final String[] keywords 
        = {"abstract", "assert", "boolean", "break", "byte", "case", "catch", 
           "char", "class", "const", "continue", "default", "do", "double", 
           "else", "enum", "extends", "false", "final", "finally", "float", "for", 
           "goto", "if", "implements", "import", "instanceof", "int", "interface", 
           "long", "native", "new", "null", "package", "private", "protected", 
           "public", "return", "short", "static", "strictfp", "super", "switch", 
           "synchronized", "this", "throw", "throws", "transient", "true", "try", 
           "var", "void", "volatile", "while"};
    private static final HashSet<String> keywordSet = new HashSet<>();
    static {
        for (String word: keywords)
            keywordSet.add(word);
    }
    private static final Object prettifyLock = new Object();
    private static final AttributeSet defaultAttributeSet
        = new SimpleAttributeSet() {
            {
                StyleConstants.setForeground(this, Color.BLACK);
            }
        };
    private static final ExecutorService prettifierThreads = 
        Executors.newCachedThreadPool();

    public static boolean isKeyword(String word) {
        return keywordSet.contains(word);        
    }

    public static void prettify(JTextPane textArea) {
        var document = textArea.getStyledDocument();
        document.setCharacterAttributes(0, document.getLength(), 
            defaultAttributeSet, true);

        String text = null;
        try {
            text = document.getText(0, document.getLength());
        } catch (BadLocationException ex) {

        }

        // Color keywords
        Matcher wordMatcher = Pattern.compile("(\\w+)").matcher(text);
        var keywordAttributeSet = new SimpleAttributeSet();
        StyleConstants.setForeground(keywordAttributeSet, Color.BLUE);
        while (wordMatcher.find()) {
            int wordBegin = wordMatcher.start();
            int wordEnd = wordMatcher.end();
            if (isKeyword(wordMatcher.group()))
                document.setCharacterAttributes(wordBegin, wordEnd - wordBegin, 
                    keywordAttributeSet, true);
        }
        
        // Color method calls
        Matcher methodMatcher = Pattern.compile("(\\w+)\\s*\\(").matcher(text);
        var methodAttributeSet = new SimpleAttributeSet();
        StyleConstants.setForeground(methodAttributeSet, Color.GREEN.darker().darker());
        while (methodMatcher.find()) {
            int methodBegin = methodMatcher.start(1);
            int methodEnd = methodMatcher.end(1);
            if (!isKeyword(methodMatcher.group(1)))
                document.setCharacterAttributes(methodBegin, methodEnd - methodBegin, 
                    methodAttributeSet, true);
        }
    }


    public static void setPrettifierAction(JTextPane textArea) {
        // When user pressing whitespace characters, prettify code
        var inputMap = textArea.getInputMap();
        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "prettify_and_enter"
        );
        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "prettify_and_tab"
        );
        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "prettify_and_space"
        );
        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "prettify_and_backspace"
        );
        var actionMap = textArea.getActionMap();
        actionMap.put(
            "prettify_and_enter", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    var doc = textArea.getStyledDocument();
                    try {
                        if (textArea.getSelectedText() != null)
                            doc.remove(textArea.getSelectionStart(),
                                       textArea.getSelectionEnd()
                                     - textArea.getSelectionStart());
                        doc.insertString(textArea.getCaret().getDot(), 
                                         "\n", null);
                    } catch (BadLocationException ex) {
                        return;
                    }
                    prettifierThreads.submit(() -> {
                        synchronized (prettifyLock) {
                            new PrettifyWorker(textArea).execute();
                            try {
                                prettifyLock.wait();
                            } catch (InterruptedException ex) {
    
                            }
                        }
                    });
                };
            }
        );
        actionMap.put(
            "prettify_and_tab", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    var doc = textArea.getStyledDocument();
                    try {
                        if (textArea.getSelectedText() != null)
                            doc.remove(textArea.getSelectionStart(),
                                       textArea.getSelectionEnd()
                                     - textArea.getSelectionStart());
                        doc.insertString(textArea.getCaret().getDot(), 
                                         "    ", null);
                    } catch (BadLocationException ex) {
                        return;
                    }
                    prettifierThreads.submit(() -> {
                        synchronized (prettifyLock) {
                            new PrettifyWorker(textArea).execute();
                            try {
                                prettifyLock.wait();
                            } catch (InterruptedException ex) {
    
                            }
                        }
                    });
                };
            }
        );
        actionMap.put(
            "prettify_and_space", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    prettifierThreads.submit(() -> {
                        synchronized (prettifyLock) {
                            new PrettifyWorker(textArea).execute();
                            try {
                                prettifyLock.wait();
                            } catch (InterruptedException ex) {

                            }
                        }
                    });
                };
            }
        );
        actionMap.put(
            "prettify_and_backspace", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    var doc = textArea.getStyledDocument();
                    try {
                        if (textArea.getSelectedText() != null)
                            doc.remove(textArea.getSelectionStart(),
                                       textArea.getSelectionEnd()
                                     - textArea.getSelectionStart());
                        else 
                            doc.remove(textArea.getCaret().getDot() - 1, 1);
                    } catch (BadLocationException ex) {
                        return;
                    }
                    prettifierThreads.submit(() -> {
                        synchronized (prettifyLock) {
                            new PrettifyWorker(textArea).execute();
                            try {
                                prettifyLock.wait();
                            } catch (InterruptedException ex) {
    
                            }
                        }
                    });
                };
            }
        );
    }

    private static class PrettifyWorker extends SwingWorker<Object, Object> {
        private JTextPane textArea;

        public PrettifyWorker(JTextPane textArea) {
            this.textArea = textArea;
        }

        public Object doInBackground() {
            synchronized (prettifyLock) {
                prettify(textArea);
                prettifyLock.notifyAll();
            }
            return null;
        }
    }
}
