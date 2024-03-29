package com.javarush.task.task31.task3110;

public class FileProperties {
    private String name;
    private long size;
    private long compressedSize;
    private int compressionMethod;

    public FileProperties(String name, long size, long compressedSize, int compressionMethod) {
        this.name = name;
        this.size = size;
        this.compressedSize = compressedSize;
        this.compressionMethod = compressionMethod;
    }

    @Override
    public String toString() {
        if (size > 0) {
            return String.format("%s %d Kb (%d Kb) сжатие: %d%%",
                    name, size / 1024, compressedSize / 1024, getCompressionRatio());
        } else {
            return name;
        }
    }

    public long getCompressionRatio() {
        return 100 - ((compressedSize * 100) / size);
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public long getCompressedSize() {
        return compressedSize;
    }

    public int getCompressionMethod() {
        return compressionMethod;
    }
}
