package com.javarush.task.task31.task3110.command;

import com.javarush.task.task31.task3110.ConsoleHelper;
import com.javarush.task.task31.task3110.ZipFileManager;
import com.javarush.task.task31.task3110.exception.PathIsNotFoundException;
import com.javarush.task.task31.task3110.exception.WrongZipFileException;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ZipAddCommand extends ZipCommand {
    @Override
    public void execute() throws Exception {
        try {
            ConsoleHelper.writeMessage("Добавление файла в архив.");
            ZipFileManager zipFileManager = getZipFileManager();
            ConsoleHelper.writeMessage("Введите имя файла добавления в архив");
            String stringPath = ConsoleHelper.readString();
            Path path = Paths.get(stringPath);
            zipFileManager.addFile(path);
            ConsoleHelper.writeMessage("Файл в архив добавлен создан.");
        } catch (WrongZipFileException e) {
            ConsoleHelper.writeMessage("Вы неверно указали имя архива.");
        } catch (PathIsNotFoundException e) {
            ConsoleHelper.writeMessage("Вы неверно указали имя файла или директории.");
        }
    }
}
