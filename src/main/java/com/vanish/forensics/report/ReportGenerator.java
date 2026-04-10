package com.vanish.forensics.report;

import com.vanish.forensics.analyzer.BatchAnalyzer;
import com.vanish.forensics.model.FileMetadata;

import java.io.IOException;
import java.util.List;

/**
 * Interface for generating reports from metadata analysis results.
 */
public interface ReportGenerator {

    /**
     * Generates a report for a single file's metadata.
     *
     * @param metadata the file metadata to report on
     */
    void generate(FileMetadata metadata) throws IOException;

    /**
     * Generates a report for multiple files (batch analysis).
     *
     * @param results list of metadata results
     * @param stats batch statistics
     */
    void generateBatch(List<FileMetadata> results, BatchAnalyzer.BatchStatistics stats) throws IOException;

    /**
     * Returns the name of this report generator.
     */
    String getName();
}
