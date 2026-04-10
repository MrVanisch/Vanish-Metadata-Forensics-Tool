package com.vanish.forensics.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main model holding all extracted metadata for a single file.
 * This is the central data structure passed between components.
 */
public class FileMetadata {

    private String fileName;
    private String filePath;
    private long fileSize;
    private String mimeType;
    private String lastModified;
    private String fileExtension;

    // Type-specific metadata
    private ExifData exifData;
    private DocumentData documentData;

    // Forensic analysis data
    private ForensicData forensicData;

    // Raw metadata (all key-value pairs)
    private Map<String, String> rawMetadata = new HashMap<>();

    // Security alerts
    private List<SensitiveDataAlert> alerts = new ArrayList<>();

    // --- Getters and Setters ---

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getLastModified() { return lastModified; }
    public void setLastModified(String lastModified) { this.lastModified = lastModified; }

    public String getFileExtension() { return fileExtension; }
    public void setFileExtension(String fileExtension) { this.fileExtension = fileExtension; }

    public ExifData getExifData() { return exifData; }
    public void setExifData(ExifData exifData) { this.exifData = exifData; }

    public DocumentData getDocumentData() { return documentData; }
    public void setDocumentData(DocumentData documentData) { this.documentData = documentData; }

    public Map<String, String> getRawMetadata() { return rawMetadata; }
    public void setRawMetadata(Map<String, String> rawMetadata) { this.rawMetadata = rawMetadata; }

    public void addRawMetadata(String key, String value) {
        this.rawMetadata.put(key, value);
    }

    public List<SensitiveDataAlert> getAlerts() { return alerts; }
    public void setAlerts(List<SensitiveDataAlert> alerts) { this.alerts = alerts; }

    public void addAlert(SensitiveDataAlert alert) {
        this.alerts.add(alert);
    }

    public boolean hasExifData() { return exifData != null; }
    public boolean hasDocumentData() { return documentData != null; }
    public boolean hasAlerts() { return !alerts.isEmpty(); }
    public boolean hasForensicData() { return forensicData != null; }

    public ForensicData getForensicData() { return forensicData; }
    public void setForensicData(ForensicData forensicData) { this.forensicData = forensicData; }

    /**
     * Returns a human-readable file size string.
     */
    public String getFormattedFileSize() {
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        if (fileSize < 1024 * 1024 * 1024) return String.format("%.1f MB", fileSize / (1024.0 * 1024));
        return String.format("%.1f GB", fileSize / (1024.0 * 1024 * 1024));
    }

    /**
     * Counts alerts by severity level.
     */
    public Map<SensitiveDataAlert.Severity, Integer> getAlertCountBySeverity() {
        Map<SensitiveDataAlert.Severity, Integer> counts = new HashMap<>();
        for (SensitiveDataAlert.Severity sev : SensitiveDataAlert.Severity.values()) {
            counts.put(sev, 0);
        }
        for (SensitiveDataAlert alert : alerts) {
            counts.merge(alert.getSeverity(), 1, Integer::sum);
        }
        return counts;
    }

    /**
     * Returns the highest severity level among all alerts.
     */
    public SensitiveDataAlert.Severity getHighestSeverity() {
        SensitiveDataAlert.Severity highest = null;
        for (SensitiveDataAlert alert : alerts) {
            if (highest == null || alert.getSeverity().ordinal() > highest.ordinal()) {
                highest = alert.getSeverity();
            }
        }
        return highest;
    }
}
