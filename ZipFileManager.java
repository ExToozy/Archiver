package com.javarush.task.task31.task3110;

import com.javarush.task.task31.task3110.exception.PathIsNotFoundException;
import com.javarush.task.task31.task3110.exception.WrongZipFileException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipFileManager {
    private final Path zipFile;

    public ZipFileManager(Path zipFile) {
        this.zipFile = zipFile;
    }

    public void createZip(Path source) throws Exception {

        Path zipDirectory = zipFile.getParent();
        if (Files.notExists(zipDirectory))
            Files.createDirectories(zipDirectory);


        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            if (Files.isDirectory(source)) {
                FileManager fileManager = new FileManager(source);
                List<Path> fileNames = fileManager.getFileList();
                for (Path fileName : fileNames)
                    addNewZipEntry(zipOutputStream, source, fileName);
            } else if (Files.isRegularFile(source)) {
                addNewZipEntry(zipOutputStream, source.getParent(), source.getFileName());
            } else {
                throw new PathIsNotFoundException();
            }
        }
    }

    private void addNewZipEntry(ZipOutputStream zipOutputStream, Path filePath, Path fileName) throws IOException {
        Path fullPath = filePath.resolve(fileName);
        try (InputStream in = Files.newInputStream(fullPath)) {
            ZipEntry zipEntry = new ZipEntry(fileName.toString());
            zipOutputStream.putNextEntry(zipEntry);
            copyData(in, zipOutputStream);
            zipOutputStream.closeEntry();
        }

    }

    private void copyData(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[8 * 1024];
        int len;
        while ((len = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, len);
        }
    }

    public List<FileProperties> getFilesList() throws Exception {
        if (Files.isRegularFile(zipFile)) {
            List<FileProperties> filesList = new ArrayList<>();
            try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFile))) {
                ZipEntry zipEntry = zipInputStream.getNextEntry();
                while (zipEntry != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    copyData(zipInputStream, baos);
                    FileProperties file = new FileProperties(zipEntry.getName(), zipEntry.getSize(), zipEntry.getCompressedSize(), zipEntry.getMethod());
                    filesList.add(file);
                    zipEntry = zipInputStream.getNextEntry();
                }
            }
            return filesList;
        } else {
            throw new WrongZipFileException();
        }
    }

    public void extractAll(Path outputFolder) throws Exception {
        if (!Files.isRegularFile(zipFile)) {
            throw new WrongZipFileException();
        }
        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFile))) {
            if (Files.notExists(outputFolder))
                Files.createDirectories(outputFolder);
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                String fileName = zipEntry.getName();
                Path fileFullName = outputFolder.resolve(fileName);
                Path parent = fileFullName.getParent();
                if (Files.notExists(parent))
                    Files.createDirectories(parent);
                try (OutputStream outputStream = Files.newOutputStream(fileFullName)) {
                    copyData(zipInputStream, outputStream);
                }
                zipEntry = zipInputStream.getNextEntry();
            }
        }
    }

    public void removeFile(Path path) throws Exception {
        List<Path> pathList = Collections.singletonList(path);
        removeFiles(pathList);
    }

    public void removeFiles(List<Path> pathList) throws Exception {
        if (!Files.isRegularFile(zipFile)) {
            throw new WrongZipFileException();
        }
        Path tempZipFile = Files.createTempFile(null, null);

        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFile));
             ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(tempZipFile))) {

            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                Path archivedFile = Paths.get(zipEntry.getName());

                if (!pathList.contains(archivedFile)) {
                    String fileName = zipEntry.getName();
                    zipOutputStream.putNextEntry(new ZipEntry(fileName));

                    copyData(zipInputStream, zipOutputStream);

                    zipOutputStream.closeEntry();
                    zipInputStream.closeEntry();
                } else {
                    ConsoleHelper.writeMessage(String.format("Файл '%s' удален из архива.", archivedFile.toString()));
                }
                zipEntry = zipInputStream.getNextEntry();
            }
        }
        Files.move(tempZipFile, zipFile, StandardCopyOption.REPLACE_EXISTING);
    }

    public void addFile(Path absolutePath) throws Exception {
        addFiles(Collections.singletonList(absolutePath));
    }

    public void addFiles(List<Path> absolutePathList) throws Exception {
        // Проверяем существует ли zip файл
        if (!Files.isRegularFile(zipFile)) {
            throw new WrongZipFileException();
        }

        // Создаем временный файл
        Path tempZipFile = Files.createTempFile(null, null);
        List<Path> archiveFiles = new ArrayList<>();

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(tempZipFile))) {
            try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFile))) {

                ZipEntry zipEntry = zipInputStream.getNextEntry();
                while (zipEntry != null) {
                    String fileName = zipEntry.getName();
                    archiveFiles.add(Paths.get(fileName));

                    zipOutputStream.putNextEntry(new ZipEntry(fileName));
                    copyData(zipInputStream, zipOutputStream);

                    zipInputStream.closeEntry();
                    zipOutputStream.closeEntry();

                    zipEntry = zipInputStream.getNextEntry();
                }
            }

            // Архивируем новые файлы
            for (Path file : absolutePathList) {
                if (Files.isRegularFile(file)) {
                    if (archiveFiles.contains(file.getFileName()))
                        ConsoleHelper.writeMessage(String.format("Файл '%s' уже существует в архиве.", file.toString()));
                    else {
                        addNewZipEntry(zipOutputStream, file.getParent(), file.getFileName());
                        ConsoleHelper.writeMessage(String.format("Файл '%s' добавлен в архиве.", file.toString()));
                    }
                } else
                    throw new PathIsNotFoundException();
            }
        }

        // Перемещаем временный файл на место оригинального
        Files.move(tempZipFile, zipFile, StandardCopyOption.REPLACE_EXISTING);
    }
}

