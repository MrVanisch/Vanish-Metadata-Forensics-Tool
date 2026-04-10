package com.vanish.forensics.core;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Interface for modifying metadata in existing files.
 * Provides a way to update specific tags safely.
 */
public interface MetadataEditor {

    /**
     * Updates specific metadata tags in the source file and saves to a new destination.
     * 
     * @param source the original file
     * @param destination the file where updated data will be saved
     * @param changes a map of tag names to new values
     * @throws IOException if the update fails
     */
    void updateMetadata(File source, File destination, Map<String, String> changes) throws Exception;

    /**
     * Checks if this editor supports the given file extension.
     */
    boolean supports(String extension);
}
