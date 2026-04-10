package com.vanish.forensics.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vanish.forensics.analyzer.BatchAnalyzer;
import com.vanish.forensics.model.FileMetadata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates JSON reports from metadata analysis results.
 * Exports data to a structured, pretty-printed JSON file.
 */
public class JsonReportGenerator implements ReportGenerator {

    private final String outputDirectory;
    private final Gson gson;

    public JsonReportGenerator(String outputDirectory) {
        this.outputDirectory = outputDirectory;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();
    }

    @Override
    public void generate(FileMetadata metadata) throws IOException {
        Map<String, Object> report = new HashMap<>();
        report.put("tool", "Vanish Metadata Forensics Tool");
        report.put("version", "1.0.0");
        report.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        report.put("type", "single_file_analysis");
        report.put("result", metadata);

        String fileName = "vanish_report_" + sanitizeFileName(metadata.getFileName()) + ".json";
        writeJsonFile(report, fileName);
    }

    @Override
    public void generateBatch(List<FileMetadata> results, BatchAnalyzer.BatchStatistics stats) throws IOException {
        Map<String, Object> report = new HashMap<>();
        report.put("tool", "Vanish Metadata Forensics Tool");
        report.put("version", "1.0.0");
        report.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        report.put("type", "batch_analysis");

        // Statistics
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalFiles", stats.getTotalFiles());
        statistics.put("filesWithAlerts", stats.getFilesWithAlerts());
        statistics.put("totalAlerts", stats.getTotalAlerts());
        statistics.put("totalSize", stats.getFormattedTotalSize());
        statistics.put("alertsBySeverity", stats.getAlertsBySeverity());
        statistics.put("alertsByType", stats.getAlertsByType());
        statistics.put("filesByMimeType", stats.getFilesByMimeType());
        report.put("statistics", statistics);

        // Results
        report.put("results", results);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "vanish_batch_report_" + timestamp + ".json";
        writeJsonFile(report, fileName);
    }

    @Override
    public String getName() {
        return "JSON Report";
    }

    private void writeJsonFile(Object data, String fileName) throws IOException {
        File outputDir = new File(outputDirectory);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File outputFile = new File(outputDir, fileName);
        try (FileWriter writer = new FileWriter(outputFile)) {
            gson.toJson(data, writer);
        }

        System.out.println("  📄 JSON report saved to: " + outputFile.getAbsolutePath());
    }

    private String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
