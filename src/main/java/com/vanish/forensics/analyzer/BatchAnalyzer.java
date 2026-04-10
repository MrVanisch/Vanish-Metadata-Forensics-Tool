package com.vanish.forensics.analyzer;

import com.vanish.forensics.core.ExtractorFactory;
import com.vanish.forensics.core.MetadataExtractor;
import com.vanish.forensics.model.FileMetadata;
import com.vanish.forensics.model.SensitiveDataAlert;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Performs batch analysis of multiple files or entire directories.
 * Provides progress tracking and aggregate statistics.
 */
public class BatchAnalyzer {

    /**
     * Listener interface for tracking batch analysis progress.
     */
    public interface ProgressListener {
        void onProgress(int current, int total, String currentFile);
        void onFileComplete(FileMetadata result);
        void onError(String filePath, String error);
    }

    private final ExtractorFactory extractorFactory;
    private final SensitiveDataDetector sensitiveDataDetector;
    private ProgressListener progressListener;

    // File extensions to scan (null = scan all)
    private Set<String> allowedExtensions = null;

    public BatchAnalyzer() {
        this.extractorFactory = new ExtractorFactory();
        this.sensitiveDataDetector = new SensitiveDataDetector();
    }

    public void setProgressListener(ProgressListener listener) {
        this.progressListener = listener;
    }

    /**
     * Sets file extensions to include in the scan.
     * Pass null to include all files.
     */
    public void setAllowedExtensions(Set<String> extensions) {
        this.allowedExtensions = extensions;
    }

    /**
     * Analyzes all supported files in the given directory (recursively).
     *
     * @param directory the directory to scan
     * @return list of FileMetadata results for all analyzed files
     * @throws IOException if the directory cannot be read
     */
    public List<FileMetadata> analyzeDirectory(File directory) throws IOException {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + directory.getAbsolutePath());
        }

        // First, collect all files
        List<File> files = collectFiles(directory);
        return analyzeFiles(files);
    }

    /**
     * Analyzes a list of files.
     *
     * @param files the files to analyze
     * @return list of FileMetadata results
     */
    public List<FileMetadata> analyzeFiles(List<File> files) {
        List<FileMetadata> results = new ArrayList<>();
        int total = files.size();

        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);

            if (progressListener != null) {
                progressListener.onProgress(i + 1, total, file.getName());
            }

            try {
                MetadataExtractor extractor = extractorFactory.getExtractor(file);
                FileMetadata metadata = extractor.extract(file);

                // Run sensitive data analysis
                sensitiveDataDetector.analyze(metadata);

                results.add(metadata);

                if (progressListener != null) {
                    progressListener.onFileComplete(metadata);
                }
            } catch (Exception e) {
                if (progressListener != null) {
                    progressListener.onError(file.getAbsolutePath(), e.getMessage());
                }
            }
        }

        return results;
    }

    /**
     * Recursively collects all files in a directory.
     */
    private List<File> collectFiles(File directory) throws IOException {
        List<File> files = new ArrayList<>();

        Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                File file = path.toFile();
                if (file.isFile() && !file.isHidden() && isAllowedExtension(file)) {
                    files.add(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                // Skip files we can't access
                return FileVisitResult.CONTINUE;
            }
        });

        return files;
    }

    /**
     * Checks if the file extension is in the allowed set.
     */
    private boolean isAllowedExtension(File file) {
        if (allowedExtensions == null) return true;

        String name = file.getName();
        int dot = name.lastIndexOf('.');
        if (dot <= 0) return false;

        String ext = name.substring(dot + 1).toLowerCase();
        return allowedExtensions.contains(ext);
    }

    /**
     * Generates aggregate statistics from batch analysis results.
     */
    public BatchStatistics getStatistics(List<FileMetadata> results) {
        return new BatchStatistics(results);
    }

    /**
     * Holds aggregate statistics from a batch analysis.
     */
    public static class BatchStatistics {
        private final int totalFiles;
        private final int filesWithAlerts;
        private final int totalAlerts;
        private final Map<SensitiveDataAlert.Severity, Integer> alertsBySeverity;
        private final Map<SensitiveDataAlert.AlertType, Integer> alertsByType;
        private final Map<String, Integer> filesByMimeType;
        private final long totalSize;

        public BatchStatistics(List<FileMetadata> results) {
            this.totalFiles = results.size();
            this.alertsBySeverity = new HashMap<>();
            this.alertsByType = new HashMap<>();
            this.filesByMimeType = new HashMap<>();

            int withAlerts = 0;
            int alerts = 0;
            long size = 0;

            for (FileMetadata meta : results) {
                size += meta.getFileSize();

                if (meta.hasAlerts()) {
                    withAlerts++;
                    alerts += meta.getAlerts().size();

                    for (SensitiveDataAlert alert : meta.getAlerts()) {
                        alertsBySeverity.merge(alert.getSeverity(), 1, Integer::sum);
                        alertsByType.merge(alert.getType(), 1, Integer::sum);
                    }
                }

                if (meta.getMimeType() != null) {
                    filesByMimeType.merge(meta.getMimeType(), 1, Integer::sum);
                }
            }

            this.filesWithAlerts = withAlerts;
            this.totalAlerts = alerts;
            this.totalSize = size;
        }

        public int getTotalFiles() { return totalFiles; }
        public int getFilesWithAlerts() { return filesWithAlerts; }
        public int getTotalAlerts() { return totalAlerts; }
        public Map<SensitiveDataAlert.Severity, Integer> getAlertsBySeverity() { return alertsBySeverity; }
        public Map<SensitiveDataAlert.AlertType, Integer> getAlertsByType() { return alertsByType; }
        public Map<String, Integer> getFilesByMimeType() { return filesByMimeType; }
        public long getTotalSize() { return totalSize; }

        /**
         * Returns a formatted total size string.
         */
        public String getFormattedTotalSize() {
            if (totalSize < 1024) return totalSize + " B";
            if (totalSize < 1024 * 1024) return String.format("%.1f KB", totalSize / 1024.0);
            if (totalSize < 1024L * 1024 * 1024) return String.format("%.1f MB", totalSize / (1024.0 * 1024));
            return String.format("%.1f GB", totalSize / (1024.0 * 1024 * 1024));
        }
    }
}
