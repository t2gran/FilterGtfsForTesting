package no.tiger.gtfs.filter.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtils {
    private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);


    @SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
    public static void createOutputDirectory(File dir) {
        dir.mkdirs();
        LOG.info("Delete all files in output directory: " + dir.getAbsolutePath());
        for (File file : dir.listFiles()) {
            file.delete();
        }
    }

    public static void compress(String sourcePath, String targetZipFile) {
        Path sourceDir = Paths.get(sourcePath);
        try {
            LOG.info("Compress files in " + sourcePath + " => " + targetZipFile);
            ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(targetZipFile));
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    try {
                        Path targetFile = sourceDir.relativize(file);
                        outputStream.putNextEntry(new ZipEntry(targetFile.toString()));
                        byte[] bytes = Files.readAllBytes(file);
                        outputStream.write(bytes, 0, bytes.length);
                        outputStream.closeEntry();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
