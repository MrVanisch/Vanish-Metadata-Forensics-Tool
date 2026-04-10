package com.vanish.forensics.cleaner;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Removes metadata from files to protect privacy.
 * Creates backup copies before modifying files.
 */
public class MetadataCleaner {

    /**
     * Result of a metadata cleaning operation.
     */
    public static class CleanResult {
        private final String originalFile;
        private final String cleanedFile;
        private final String backupFile;
        private final boolean success;
        private final List<String> removedFields;
        private final String errorMessage;

        public CleanResult(String originalFile, String cleanedFile, String backupFile,
                           boolean success, List<String> removedFields, String errorMessage) {
            this.originalFile = originalFile;
            this.cleanedFile = cleanedFile;
            this.backupFile = backupFile;
            this.success = success;
            this.removedFields = removedFields;
            this.errorMessage = errorMessage;
        }

        public String getOriginalFile() { return originalFile; }
        public String getCleanedFile() { return cleanedFile; }
        public String getBackupFile() { return backupFile; }
        public boolean isSuccess() { return success; }
        public List<String> getRemovedFields() { return removedFields; }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * Cleans metadata from an image file by re-encoding it.
     * Re-encoding strips all EXIF, IPTC, and XMP data.
     *
     * @param inputFile the image file to clean
     * @param createBackup if true, creates a backup of the original file
     * @return CleanResult with details of the operation
     */
    public CleanResult cleanImage(File inputFile, boolean createBackup) {
        List<String> removedFields = new ArrayList<>();
        String backupPath = null;

        try {
            // Create backup if requested
            if (createBackup) {
                File backupFile = new File(inputFile.getParent(),
                        getNameWithoutExtension(inputFile) + "_backup." + getExtension(inputFile));
                Files.copy(inputFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                backupPath = backupFile.getAbsolutePath();
            }

            // Read image pixels (discarding metadata)
            BufferedImage image = ImageIO.read(inputFile);
            if (image == null) {
                return new CleanResult(inputFile.getAbsolutePath(), null, backupPath,
                        false, removedFields, "Could not read image file. Format may not be supported.");
            }

            // Determine output format
            String extension = getExtension(inputFile);
            String formatName = getImageFormatName(extension);

            // Create cleaned output file
            File cleanedFile = new File(inputFile.getParent(),
                    getNameWithoutExtension(inputFile) + "_clean." + extension);

            // Write image without metadata
            boolean written = ImageIO.write(image, formatName, cleanedFile);
            if (!written) {
                return new CleanResult(inputFile.getAbsolutePath(), null, backupPath,
                        false, removedFields, "Could not write cleaned image. Format '" + formatName + "' not supported for writing.");
            }

            // Track what was removed
            removedFields.add("All EXIF data (camera, settings, dates)");
            removedFields.add("GPS coordinates");
            removedFields.add("IPTC data");
            removedFields.add("XMP data");
            removedFields.add("Thumbnail images");
            removedFields.add("Camera serial numbers");

            return new CleanResult(
                    inputFile.getAbsolutePath(),
                    cleanedFile.getAbsolutePath(),
                    backupPath,
                    true,
                    removedFields,
                    null
            );

        } catch (IOException e) {
            return new CleanResult(inputFile.getAbsolutePath(), null, backupPath,
                    false, removedFields, "Error cleaning image: " + e.getMessage());
        }
    }

    /**
     * Cleans metadata from a file. Automatically detects the file type
     * and applies the appropriate cleaning method.
     *
     * @param file the file to clean
     * @param createBackup if true, creates a backup of the original
     * @return CleanResult with operation details
     */
    public CleanResult cleanFile(File file, boolean createBackup) {
        String extension = getExtension(file);

        // Image files — re-encode to strip metadata
        switch (extension) {
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
            case "tiff":
            case "tif":
                return cleanImage(file, createBackup);
            default:
                return new CleanResult(file.getAbsolutePath(), null, null,
                        false, new ArrayList<>(),
                        "Metadata cleaning for '" + extension + "' files is not yet supported. " +
                        "Supported formats: JPEG, PNG, GIF, BMP, TIFF.");
        }
    }

    private String getExtension(File file) {
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(dot + 1).toLowerCase() : "";
    }

    private String getNameWithoutExtension(File file) {
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    private String getImageFormatName(String extension) {
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "jpg";
            case "png":
                return "png";
            case "gif":
                return "gif";
            case "bmp":
                return "bmp";
            case "tiff":
            case "tif":
                return "tiff";
            default:
                return extension;
        }
    }
}
