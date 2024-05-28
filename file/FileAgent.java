package file;

import java.io.File;
import java.io.IOException;

/**
 * Each editing thread of the text editor holds a {@code FileAgent} object
 * which bookkeeps the data and metadata of a text file.
 */
public class FileAgent {
    private File file;
    private StringBuilder content;
    private boolean dirty; // Is the file edited but not saved?

    /**
     * Associate the {@code FileAgent} object with a file from the given 
     * {@code path}.
     * @param path the file path
     */
    public void open(String path) {
        file = new File(path);
    }

    /**
     * Read {@code file} into the {@code StringBuilder} {@code content};
     * @throws IOException
     */
    public void read() throws IOException {
        if (file == null)
            throw new EmptyFileException();
        if (!FileOperations.isTextFile(file))
            throw new NotTextFileException();
        // A new file should be handled by another {@code FileAgent} object
        assert content == null;
        
        content = new StringBuilder(
            new String(
                FileOperations.readFile(file), FileOperations.DEFAULT_CHARSET
            )
        );
        dirty = false;
    }

    /**
     * Write {@code content} back to {@code file}.
     * @throws IOException
     */
    public void write() throws IOException {
        if (file == null)
            throw new EmptyFileException();
        FileOperations.writeFile(file, 
            content.toString().getBytes(FileOperations.DEFAULT_CHARSET));
        dirty = false;
    }

    public void setDirty() {
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public String getContent() {
        return content.toString();
    }

    /**
     * Like {@code getContent()}, but allows caller to modify {@code content}. 
     * Thus we set {@code dirty} to {@code true}.
     * @return file content
     */
    public StringBuilder editContent() {
        dirty = true;
        return content;
    }

    /**
     * Reset internal state of the {@code FileAgent} object. If there are
     * unsaved changes, and {@code save} is set to {@code true}, save them
     * to file before closing.
     * @param save whether to save changes
     * @throws IOException
     */
    public void close(boolean save) throws IOException {
        if (dirty && save)
            write();
        
        file = null;
        content = null;
    }
}

/**
 * Thrown by a {@code FileAgent} object when trying to operate
 * a non-text file.
 */
class NotTextFileException extends IOException {
    /**
     * Constructs a {@code NotTextFileException} with
     * {@code null} as its error detail message.
     */
    public NotTextFileException() {
        super();
    }

    /**
     * Constructs a {@code NotTextFileException} with the
     * specified detail message. The string {@code s} can be
     * retrieved later by the
     * {@link java.lang.Throwable#getMessage}
     * method of class {@code java.lang.Throwable}.
     *
     * @param   s   the detail message.
     */
    public NotTextFileException(String s) {
        super(s);
    }
}

/**
 * Thrown by a {@code FileAgent} object when trying to operate
 * an empty file.
 */
class EmptyFileException extends IOException {
    /**
     * Constructs a {@code EmptyFileException} with
     * {@code null} as its error detail message.
     */
    public EmptyFileException() {
        super();
    }

    /**
     * Constructs a {@code EmptyFileException} with the
     * specified detail message. The string {@code s} can be
     * retrieved later by the
     * {@link java.lang.Throwable#getMessage}
     * method of class {@code java.lang.Throwable}.
     *
     * @param   s   the detail message.
     */
    public EmptyFileException(String s) {
        super(s);
    }
}