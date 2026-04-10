package com.vanish.forensics.core;

import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;

/**
 * Factory that selects the appropriate MetadataExtractor based on file MIME type.
 * Uses Apache Tika for MIME type detection and delegates to specialized extractors.
 */
public class ExtractorFactory {

    private final Tika tika = new Tika();
    private final ImageMetadataExtractor imageExtractor = new ImageMetadataExtractor();
    private final DocumentMetadataExtractor documentExtractor = new DocumentMetadataExtractor();

    /**
     * Returns the appropriate MetadataExtractor for the given file.
     * Detects MIME type automatically using Apache Tika.
     *
     * @param file the file to find an extractor for
     * @return the appropriate MetadataExtractor
     * @throws IOException if MIME type cannot be detected
     */
    public MetadataExtractor getExtractor(File file) throws IOException {
        String mimeType = detectMimeType(file);

        if (imageExtractor.supports(mimeType)) {
            return imageExtractor;
        }

        if (documentExtractor.supports(mimeType)) {
            return documentExtractor;
        }

        // Fallback: use document extractor (Tika handles many formats)
        return documentExtractor;
    }

    /**
     * Detects the MIME type of the given file using Apache Tika.
     * Tika uses both file extension and content-based (magic bytes) detection.
     *
     * @param file the file to detect MIME type for
     * @return the detected MIME type string
     * @throws IOException if the file cannot be read
     */
    public String detectMimeType(File file) throws IOException {
        return tika.detect(file);
    }

    /**
     * Returns a human-readable description of the file type.
     */
    public String getFileTypeDescription(File file) throws IOException {
        String mimeType = detectMimeType(file);

        if (mimeType.startsWith("image/")) return "Image (" + mimeType + ")";
        if (mimeType.contains("pdf")) return "PDF Document";
        if (mimeType.contains("word") || mimeType.contains("document")) return "Word Document";
        if (mimeType.contains("excel") || mimeType.contains("spreadsheet")) return "Spreadsheet";
        if (mimeType.contains("powerpoint") || mimeType.contains("presentation")) return "Presentation";
        if (mimeType.contains("opendocument")) return "OpenDocument";
        if (mimeType.startsWith("text/")) return "Text File";

        return "File (" + mimeType + ")";
    }
}
