package file;

import java.io.File;
import java.util.*;

public class FileEnumerator {
    @SuppressWarnings("unused")
    private File root;
    private Queue<File> fileQueue;

    public FileEnumerator(File root) {
        this.root = root;
        fileQueue = new ArrayDeque<>();
        fileQueue.offer(root);
    }

    /**
     * Find next directory in {@code fileQueue}, and list its files.
     * @return a {@code DirectoryListData} object, containing a {@code File}
     * object (representing the next directory) and a {@code File[]} array
     * (representing its children)
     */
    public DirectoryListData enumerate() {
        while (!fileQueue.isEmpty()) {
            File nextFile = fileQueue.poll();
            if (nextFile.isDirectory()) {
                File[] children = nextFile.listFiles();
                if (children == null)
                    continue;

                // Put directories before normal files, then sort file names
                // in lexicographic order
                Arrays.sort(children, (f1, f2) -> {
                    if (f1.isDirectory() && f2.isFile())
                        return -1;
                    if (f1.isFile() && f2.isDirectory())
                        return 1;
                    return Comparator.comparing(File::getName).compare(f1, f2);
                });
                for (File child: children)
                    fileQueue.offer(child);
                
                return new DirectoryListData(nextFile, children);
            }
        }
        return null;
    }

    public class DirectoryListData {
        public File root;
        public File[] children;

        public DirectoryListData(File root, File[] children) {
            this.root = root;
            this.children = children;
        }
    }
}
