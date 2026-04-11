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
    private final MediaMetadataExtractor mediaExtractor = new MediaMetadataExtractor();

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
        MetadataExtractor baseExtractor;

        if (imageExtractor.supports(mimeType)) {
            baseExtractor = imageExtractor;
        } else if (documentExtractor.supports(mimeType)) {
            baseExtractor = documentExtractor;
        } else if (mimeType.startsWith("video/") || mimeType.startsWith("audio/")) {
            baseExtractor = mediaExtractor;
        } else {
            // Fallback: use document extractor (Tika handles many formats)
            baseExtractor = documentExtractor;
        }

        return new MetadataExtractor() {
            @Override
            public com.vanish.forensics.model.FileMetadata extract(File file) throws IOException {
                com.vanish.forensics.model.FileMetadata result = baseExtractor.extract(file);
                
                // Scan for Vanish Stealth Payload at the end of the file
                try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(file, "r")) {
                    long length = raf.length();
                    if (length > 20) {
                        long start = Math.max(0, length - 10240);
                        raf.seek(start);
                        byte[] bytes = new byte[(int) (length - start)];
                        raf.readFully(bytes);
                        String tail = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                        int markerIdx = tail.lastIndexOf("VANISH_META:");
                        if (markerIdx != -1) {
                            String json = tail.substring(markerIdx + 12).trim();
                            if (json.endsWith("}")) {
                                try {
                                    java.util.Map<String, String> stealthMap = new com.google.gson.Gson().fromJson(
                                        json, new com.google.gson.reflect.TypeToken<java.util.Map<String, String>>(){}.getType());
                                    if (stealthMap != null) {
                                        for (java.util.Map.Entry<String, String> entry : stealthMap.entrySet()) {
                                            String k = entry.getKey();
                                            String v = entry.getValue();
                                            result.addRawMetadata(k, v);
                                            
                                            // Ensure structural fields are mapped dynamically for UI consistency
                                            String kl = k.toLowerCase();
                                            if (result.hasMediaData()) {
                                                com.vanish.forensics.model.MediaData md = result.getMediaData();
                                                if (kl.contains("date") || kl.contains("created")) md.setDate(v);
                                                if (kl.contains("title")) md.setTitle(v);
                                                if (kl.contains("artist") || kl.contains("author")) md.setArtist(v);
                                            }
                                            if (result.hasDocumentData()) {
                                                com.vanish.forensics.model.DocumentData dd = result.getDocumentData();
                                                if (kl.contains("date") || kl.contains("created")) dd.setCreationDate(v);
                                                if (kl.contains("title")) dd.setTitle(v);
                                                if (kl.contains("author")) dd.setAuthor(v);
                                                if (kl.contains("company")) dd.setCompany(v);
                                            }
                                        }
                                    }
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                } catch (Exception ignored) {}
                
                return result;
            }

            @Override
            public boolean supports(String mimeType) {
                return baseExtractor.supports(mimeType);
            }

            @Override
            public String getName() {
                return baseExtractor.getName() + " (with Stealth Payload Support)";
            }
        };
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
        if (mimeType.startsWith("video/")) return "Video (" + mimeType + ")";
        if (mimeType.startsWith("audio/")) return "Audio (" + mimeType + ")";
        if (mimeType.contains("pdf")) return "PDF Document";
        if (mimeType.contains("word") || mimeType.contains("document")) return "Word Document";
        if (mimeType.contains("excel") || mimeType.contains("spreadsheet")) return "Spreadsheet";
        if (mimeType.contains("powerpoint") || mimeType.contains("presentation")) return "Presentation";
        if (mimeType.contains("opendocument")) return "OpenDocument";
        if (mimeType.startsWith("text/")) return "Text File";

        return "File (" + mimeType + ")";
    }
}
