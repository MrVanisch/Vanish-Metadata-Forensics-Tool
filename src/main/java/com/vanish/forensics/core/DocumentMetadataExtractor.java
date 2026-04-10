package com.vanish.forensics.core;

import com.vanish.forensics.model.DocumentData;
import com.vanish.forensics.model.FileMetadata;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.metadata.Office;
import org.apache.tika.metadata.PagedText;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;

/**
 * Extracts metadata from document files using Apache Tika.
 * Supports: PDF, DOCX, XLSX, PPTX, ODT, ODS, ODP, RTF, TXT, and more.
 */
public class DocumentMetadataExtractor implements MetadataExtractor {

    private static final String[] SUPPORTED_TYPES = {
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.oasis.opendocument.text",
            "application/vnd.oasis.opendocument.spreadsheet",
            "application/vnd.oasis.opendocument.presentation",
            "application/rtf",
            "text/plain"
    };

    private final Tika tika = new Tika();

    @Override
    public FileMetadata extract(File file) throws IOException {
        FileMetadata fileMetadata = new FileMetadata();

        // Basic file info
        fileMetadata.setFileName(file.getName());
        fileMetadata.setFilePath(file.getAbsolutePath());
        fileMetadata.setFileSize(file.length());
        fileMetadata.setFileExtension(getExtension(file));

        try {
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            fileMetadata.setLastModified(sdf.format(attrs.lastModifiedTime().toMillis()));
        } catch (IOException e) {
            // Ignore
        }

        // Detect MIME type
        String mimeType = tika.detect(file);
        fileMetadata.setMimeType(mimeType);

        // Extract metadata using Tika
        Metadata tikaMetadata = new Metadata();
        try (InputStream is = new FileInputStream(file)) {
            // Parse the file — this fills the metadata object
            tika.parse(is, tikaMetadata);

            // Store all raw metadata
            for (String name : tikaMetadata.names()) {
                String value = tikaMetadata.get(name);
                if (value != null && !value.isEmpty()) {
                    fileMetadata.addRawMetadata(name, value);
                }
            }

            // Extract structured document data
            DocumentData docData = extractDocumentData(tikaMetadata);
            fileMetadata.setDocumentData(docData);

        } catch (Exception e) {
            fileMetadata.addRawMetadata("Error", "Tika parsing error: " + e.getMessage());
        }

        return fileMetadata;
    }

    /**
     * Extracts structured document data from Tika metadata.
     */
    private DocumentData extractDocumentData(Metadata tikaMetadata) {
        DocumentData docData = new DocumentData();

        // Core properties
        docData.setAuthor(getMetaValue(tikaMetadata, TikaCoreProperties.CREATOR.getName(), "meta:author", "Author"));
        docData.setTitle(getMetaValue(tikaMetadata, TikaCoreProperties.TITLE.getName(), "dc:title", "title"));
        docData.setSubject(getMetaValue(tikaMetadata, TikaCoreProperties.SUBJECT.getName(), "dc:subject", "subject"));
        docData.setDescription(getMetaValue(tikaMetadata, TikaCoreProperties.DESCRIPTION.getName(), "dc:description"));
        docData.setLanguage(getMetaValue(tikaMetadata, TikaCoreProperties.LANGUAGE.getName(), "dc:language"));
        docData.setKeywords(getMetaValue(tikaMetadata, "meta:keyword", "Keywords", "dc:subject"));

        // Creation / modification dates
        docData.setCreationDate(getMetaValue(tikaMetadata, TikaCoreProperties.CREATED.getName(), "meta:creation-date", "Creation-Date"));
        docData.setModificationDate(getMetaValue(tikaMetadata, TikaCoreProperties.MODIFIED.getName(), "meta:save-date", "Last-Modified"));

        // Application info
        docData.setCreator(getMetaValue(tikaMetadata, "Application-Name", "xmp:CreatorTool", "producer"));
        docData.setProducer(getMetaValue(tikaMetadata, "pdf:producer", "pdf:PDFVersion", "producer"));

        // Office-specific
        docData.setLastAuthor(getMetaValue(tikaMetadata, "meta:last-author", "Last-Author"));
        docData.setRevision(getMetaValue(tikaMetadata, "meta:editing-cycles", "Revision-Number", "cp:revision"));
        docData.setCompany(getMetaValue(tikaMetadata, "extended-properties:Company", "meta:company", "Company"));
        docData.setManager(getMetaValue(tikaMetadata, "extended-properties:Manager", "meta:manager"));
        docData.setCategory(getMetaValue(tikaMetadata, "cp:category", "meta:category"));

        // Page/word/character counts
        String pageCount = getMetaValue(tikaMetadata, "xmpTPg:NPages", "meta:page-count", "Page-Count");
        if (pageCount != null) {
            try { docData.setPageCount(Integer.parseInt(pageCount.trim())); } catch (NumberFormatException ignored) {}
        }

        String wordCount = getMetaValue(tikaMetadata, "meta:word-count", "Word-Count");
        if (wordCount != null) {
            try { docData.setWordCount(Integer.parseInt(wordCount.trim())); } catch (NumberFormatException ignored) {}
        }

        String charCount = getMetaValue(tikaMetadata, "meta:character-count", "Character Count");
        if (charCount != null) {
            try { docData.setCharacterCount(Integer.parseInt(charCount.trim())); } catch (NumberFormatException ignored) {}
        }

        return docData;
    }

    /**
     * Tries multiple metadata keys and returns the first non-null value.
     */
    private String getMetaValue(Metadata metadata, String... keys) {
        for (String key : keys) {
            String value = metadata.get(key);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return null;
    }

    @Override
    public boolean supports(String mimeType) {
        if (mimeType == null) return false;
        for (String type : SUPPORTED_TYPES) {
            if (mimeType.equalsIgnoreCase(type)) return true;
        }
        return mimeType.startsWith("application/") || mimeType.startsWith("text/");
    }

    @Override
    public String getName() {
        return "Document Metadata Extractor (Tika)";
    }

    private String getExtension(File file) {
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(dot + 1).toLowerCase() : "";
    }
}
