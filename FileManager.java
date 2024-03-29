package com.javarush.task.task31.task3110;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private Path rootPath;
    private List<Path> fileList = new ArrayList<>();

    public FileManager(Path rootPath) throws IOException {
        this.rootPath = rootPath;
        collectFileList(rootPath);
    }

    private void collectFileList(Path path) throws IOException {
        if (Files.isRegularFile(path)) {
            fileList.add(rootPath.relativize(path));
        }
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
                for (Path inDirectoryPath : directoryStream) {
                    collectFileList(inDirectoryPath);
                }
            }
        }
    }

    public List<Path> getFileList() {
        return fileList;
    }
}
