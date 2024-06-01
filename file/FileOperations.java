package file;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;

/**
 * Utility class for file operations.
 */
public class FileOperations {
    private static final int MAX_BUFFER_LEN = 1024;
    private static final double MIN_TEXT_SIGN_PERCENTAGE = 0.95;
    
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * Detect whether {@code file} is a text file. The solution is due to
     * {@ref https://stackoverflow.com/questions/620993/determining-binary-text-file-type-in-java}.
     * @param file a {@code java.io.File} object
     * @return whether {@code file} is a text file
     * @throws IOException if {@code file} does not exist, or {@code file} is
     * a directory, or an {@code IOException} occurs elsewhere in the method
     */
    public static boolean isTextFile(File file) throws IOException {
        byte[] data;
        try (FileInputStream in = new FileInputStream(file)) {
            // Read at most 1,024 bytes from file
            int size = in.available();
            if (size > MAX_BUFFER_LEN) {
                data = new byte[MAX_BUFFER_LEN];
                in.read(data);
            } else {
                data = in.readAllBytes();
            }
        }

        String s = new String(data, DEFAULT_CHARSET);
        // Delete all text signs
        String s2 = s.replaceAll("[a-zA-Z0-9ßöäü\\.\\*!\"§\\$\\%&/()=\\?@~'#:,;\\"  +
                                 "+><\\|\\[\\]\\{\\}\\^°²³\\\\ \\n\\r\\t_\\-`´âêîô" +
                                 "ÂÊÔÎáéíóàèìòÁÉÍÓÀÈÌÒ©‰¢£¥€±¿»«¼½¾™ª]", "");

        // Find the percentage of text signs
        double d = (double)(s.length() - s2.length()) / (double)(s.length());
        
        return d > MIN_TEXT_SIGN_PERCENTAGE;
    }

    /**
     * Read all bytes in {@code file}.
     * @param file a {@code java.io.File} object
     * @return a {@code byte[]} array containing content of {@code file}
     * @throws IOException if {@code file.length()} is zero, or is larger
     * than {@code Integer.MAX_VALUE}, or an {@code IOException} occurs
     * elsewhere in the method
     */
    public static byte[] readFile(File file) throws IOException {
        long fileSize = file.length();
        if (fileSize == 0)
            throw new NoSuchFileException(file.getName());
        if (fileSize > Integer.MAX_VALUE)
            throw new IOException(
                String.format("File %s too long!", file.getName()));

        try (FileInputStream in = new FileInputStream(file)) {
            return in.readAllBytes();
        }
    }

    /**
     * Write byte array {@code content} into {@code file}.
     * @param file a {@code java.io.File} object
     * @param content a {@code byte[]} array containing the content to write
     * @throws IOException if an {@code IOException} occurs while writing
     */
    public static void writeFile(File file, byte[] content) throws IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(content);
        }
    }
}

