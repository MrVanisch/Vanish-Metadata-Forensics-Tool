package com.vanish.forensics.core;

import com.vanish.forensics.model.FileMetadata;
import java.io.File;
import java.io.IOException;

/**
 * Interface for metadata extraction from different file types.
 * Each implementation handles a specific category of files.
 */
public interface MetadataExtractor {

    /**
     * Extracts metadata from the given file.
     *
     * @param file the file to extract metadata from
     * @return FileMetadata object containing all extracted data
     * @throws IOException if the file cannot be read
     */
    FileMetadata extract(File file) throws IOException;

    /**
     * Checks if this extractor supports the given MIME type.
     *
     * @param mimeType the MIME type to check
     * @return true if this extractor can handle the given MIME type
     */
    boolean supports(String mimeType);

    /**
     * Returns a human-readable name for this extractor.
     */
    String getName();
}
