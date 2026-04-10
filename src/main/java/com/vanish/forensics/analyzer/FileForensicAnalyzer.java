package com.vanish.forensics.analyzer;

import com.vanish.forensics.model.FileMetadata;
import com.vanish.forensics.model.ForensicData;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Performs forensic-level analysis on files:
 * - Calculates cryptographic hashes (MD5, SHA-1, SHA-256)
 * - Analyzes file entropy (randomness detection)
 * - Verifies file signature (magic bytes vs extension)
 * - Computes risk score
 */
public class FileForensicAnalyzer {

    private static final int BUFFER_SIZE = 8192;
    private static final int ENTROPY_SAMPLE_SIZE = 65536; // 64KB sample

    /**
     * Runs full forensic analysis on a file and attaches results to metadata.
     */
    public ForensicData analyze(File file, FileMetadata metadata) throws IOException {
        ForensicData forensic = new ForensicData();

        // Calculate hashes
        calculateHashes(file, forensic);

        // Analyze entropy
        analyzeEntropy(file, forensic);

        // Verify file signature
        verifySignature(file, forensic);

        // Calculate risk score based on all metadata
        calculateRiskScore(metadata, forensic);

        metadata.setForensicData(forensic);
        return forensic;
    }

    /**
     * Calculates MD5, SHA-1, and SHA-256 hashes simultaneously.
     */
    private void calculateHashes(File file, ForensicData forensic) throws IOException {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

            try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    md5.update(buffer, 0, read);
                    sha1.update(buffer, 0, read);
                    sha256.update(buffer, 0, read);
                }
            }

            forensic.setMd5(HexFormat.of().formatHex(md5.digest()));
            forensic.setSha1(HexFormat.of().formatHex(sha1.digest()));
            forensic.setSha256(HexFormat.of().formatHex(sha256.digest()));

        } catch (NoSuchAlgorithmException e) {
            // Should never happen — these algorithms are guaranteed by JCA
            throw new RuntimeException("Hash algorithm not available", e);
        }
    }

    /**
     * Analyzes file entropy (Shannon entropy).
     * Higher entropy = more randomness = possible encryption/compression/steganography.
     */
    private void analyzeEntropy(File file, ForensicData forensic) throws IOException {
        long[] frequency = new long[256];
        long totalBytes = 0;

        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = is.read(buffer)) != -1 && totalBytes < ENTROPY_SAMPLE_SIZE) {
                for (int i = 0; i < read && totalBytes < ENTROPY_SAMPLE_SIZE; i++) {
                    frequency[buffer[i] & 0xFF]++;
                    totalBytes++;
                }
            }
        }

        // Calculate Shannon entropy
        double entropy = 0.0;
        for (int i = 0; i < 256; i++) {
            if (frequency[i] > 0) {
                double p = (double) frequency[i] / totalBytes;
                entropy -= p * (Math.log(p) / Math.log(2));
            }
        }

        forensic.setEntropy(entropy);
    }

    /**
     * Reads file magic bytes and verifies they match the file extension.
     */
    private void verifySignature(File file, ForensicData forensic) throws IOException {
        byte[] header = new byte[16];
        int bytesRead;

        try (InputStream is = new FileInputStream(file)) {
            bytesRead = is.read(header);
        }

        if (bytesRead < 4) {
            forensic.setMagicBytes("(too small)");
            forensic.setDetectedType("Unknown");
            forensic.setExtensionMatch(true);
            return;
        }

        String hex = HexFormat.of().formatHex(header, 0, Math.min(bytesRead, 8));
        forensic.setMagicBytes(hex.toUpperCase());

        // Detect actual type from magic bytes
        String detectedType = detectFromMagicBytes(header);
        forensic.setDetectedType(detectedType);

        // Check extension match
        String extension = getExtension(file).toLowerCase();
        boolean matches = checkExtensionMatch(extension, detectedType);
        forensic.setExtensionMatch(matches);

        if (!matches && !detectedType.equals("Unknown")) {
            forensic.setExtensionMismatchWarning(
                    "File extension '." + extension + "' does NOT match detected type '" + detectedType + "'. " +
                    "This file may be disguised or corrupted!"
            );
        }
    }

    /**
     * Detects file type from magic bytes (file signatures).
     */
    private String detectFromMagicBytes(byte[] header) {
        // JPEG: FF D8 FF
        if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
            return "JPEG Image";
        }
        // PNG: 89 50 4E 47
        if (header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
            return "PNG Image";
        }
        // GIF: 47 49 46 38
        if (header[0] == 0x47 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x38) {
            return "GIF Image";
        }
        // PDF: 25 50 44 46
        if (header[0] == 0x25 && header[1] == 0x50 && header[2] == 0x44 && header[3] == 0x46) {
            return "PDF Document";
        }
        // ZIP/DOCX/XLSX/PPTX: 50 4B 03 04
        if (header[0] == 0x50 && header[1] == 0x4B && header[2] == 0x03 && header[3] == 0x04) {
            return "ZIP Archive / Office Document";
        }
        // RAR: 52 61 72 21
        if (header[0] == 0x52 && header[1] == 0x61 && header[2] == 0x72 && header[3] == 0x21) {
            return "RAR Archive";
        }
        // EXE/DLL: 4D 5A
        if (header[0] == 0x4D && header[1] == 0x5A) {
            return "Windows Executable (PE)";
        }
        // BMP: 42 4D
        if (header[0] == 0x42 && header[1] == 0x4D) {
            return "BMP Image";
        }
        // TIFF: 49 49 2A 00 or 4D 4D 00 2A
        if ((header[0] == 0x49 && header[1] == 0x49 && header[2] == 0x2A && header[3] == 0x00) ||
            (header[0] == 0x4D && header[1] == 0x4D && header[2] == 0x00 && header[3] == 0x2A)) {
            return "TIFF Image";
        }
        // 7z: 37 7A BC AF
        if (header[0] == 0x37 && header[1] == 0x7A && header[2] == (byte) 0xBC && header[3] == (byte) 0xAF) {
            return "7-Zip Archive";
        }
        // XML: 3C 3F 78 6D
        if (header[0] == 0x3C && header[1] == 0x3F && header[2] == 0x78 && header[3] == 0x6D) {
            return "XML Document";
        }
        // WebP: RIFF....WEBP
        if (header[0] == 0x52 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x46 &&
            header.length >= 12 && header[8] == 0x57 && header[9] == 0x45 && header[10] == 0x42 && header[11] == 0x50) {
            return "WebP Image";
        }
        // MP4/MOV: ftyp at offset 4
        if (header.length >= 8 && header[4] == 0x66 && header[5] == 0x74 && header[6] == 0x79 && header[7] == 0x70) {
            return "MP4/MOV Video";
        }

        return "Unknown";
    }

    /**
     * Checks if file extension matches detected type.
     */
    private boolean checkExtensionMatch(String ext, String detectedType) {
        if (detectedType.equals("Unknown")) return true; // Can't verify

        switch (ext) {
            case "jpg": case "jpeg":
                return detectedType.contains("JPEG");
            case "png":
                return detectedType.contains("PNG");
            case "gif":
                return detectedType.contains("GIF");
            case "pdf":
                return detectedType.contains("PDF");
            case "docx": case "xlsx": case "pptx": case "zip":
                return detectedType.contains("ZIP") || detectedType.contains("Office");
            case "bmp":
                return detectedType.contains("BMP");
            case "tiff": case "tif":
                return detectedType.contains("TIFF");
            case "exe": case "dll":
                return detectedType.contains("PE") || detectedType.contains("Executable");
            case "rar":
                return detectedType.contains("RAR");
            case "7z":
                return detectedType.contains("7-Zip");
            case "xml":
                return detectedType.contains("XML");
            case "webp":
                return detectedType.contains("WebP");
            case "mp4": case "mov":
                return detectedType.contains("MP4") || detectedType.contains("MOV");
            default:
                return true; // Unknown extension, can't verify
        }
    }

    /**
     * Calculates overall privacy risk score based on all detected sensitive data.
     */
    private void calculateRiskScore(FileMetadata metadata, ForensicData forensic) {
        int score = 0;

        // GPS location = major risk
        if (metadata.hasExifData() && metadata.getExifData().hasGps()) {
            score += 35;
        }

        // Author/personal info
        if (metadata.hasDocumentData()) {
            if (metadata.getDocumentData().getAuthor() != null) score += 15;
            if (metadata.getDocumentData().getCompany() != null) score += 10;
            if (metadata.getDocumentData().getLastAuthor() != null) score += 8;
            if (metadata.getDocumentData().getManager() != null) score += 5;
        }

        // Camera serial number
        if (metadata.hasExifData() && metadata.getExifData().getSerialNumber() != null) {
            score += 20;
        }

        // Camera device info
        if (metadata.hasExifData() && metadata.getExifData().getCameraModel() != null) {
            score += 5;
        }

        // Software info
        if (metadata.hasExifData() && metadata.getExifData().getSoftware() != null) {
            score += 3;
        }

        // High entropy (possible hidden data)
        if (forensic.getEntropy() > 7.5) {
            score += 10;
        }

        // Extension mismatch (suspicious)
        if (!forensic.isExtensionMatch()) {
            score += 15;
        }

        // Embedded thumbnail (can contain original uncropped image)
        if (forensic.hasThumbnail()) {
            score += 5;
        }

        // Alerts from sensitive data detector
        if (metadata.hasAlerts()) {
            score += Math.min(metadata.getAlerts().size() * 3, 20);
        }

        forensic.setRiskScore(score);
    }

    private String getExtension(File file) {
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(dot + 1) : "";
    }
}
