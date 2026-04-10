package com.vanish.forensics.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds forensic analysis data for a file:
 * - Cryptographic hashes (MD5, SHA-1, SHA-256)
 * - Entropy analysis (randomness detection)
 * - Magic bytes / file signature verification
 * - Risk scoring
 */
public class ForensicData {

    // Cryptographic hashes
    private String md5;
    private String sha1;
    private String sha256;

    // Entropy analysis
    private double entropy;          // 0.0 - 8.0 (bits per byte)
    private String entropyCategory;  // "Low", "Normal", "High", "Suspicious"

    // File signature
    private String magicBytes;       // Hex string of first bytes
    private String detectedType;     // Actual type based on magic bytes
    private boolean extensionMatch;  // Does extension match actual content?
    private String extensionMismatchWarning;

    // Risk score
    private int riskScore;           // 0-100
    private String riskLevel;        // "SAFE", "LOW", "MEDIUM", "HIGH", "CRITICAL"

    // Embedded objects
    private boolean hasThumbnail;
    private int thumbnailWidth;
    private int thumbnailHeight;

    // --- Getters and Setters ---

    public String getMd5() { return md5; }
    public void setMd5(String md5) { this.md5 = md5; }

    public String getSha1() { return sha1; }
    public void setSha1(String sha1) { this.sha1 = sha1; }

    public String getSha256() { return sha256; }
    public void setSha256(String sha256) { this.sha256 = sha256; }

    public double getEntropy() { return entropy; }
    public void setEntropy(double entropy) {
        this.entropy = entropy;
        this.entropyCategory = categorizeEntropy(entropy);
    }

    public String getEntropyCategory() { return entropyCategory; }

    public String getMagicBytes() { return magicBytes; }
    public void setMagicBytes(String magicBytes) { this.magicBytes = magicBytes; }

    public String getDetectedType() { return detectedType; }
    public void setDetectedType(String detectedType) { this.detectedType = detectedType; }

    public boolean isExtensionMatch() { return extensionMatch; }
    public void setExtensionMatch(boolean extensionMatch) { this.extensionMatch = extensionMatch; }

    public String getExtensionMismatchWarning() { return extensionMismatchWarning; }
    public void setExtensionMismatchWarning(String warning) { this.extensionMismatchWarning = warning; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) {
        this.riskScore = Math.max(0, Math.min(100, riskScore));
        this.riskLevel = categorizeRisk(this.riskScore);
    }

    public String getRiskLevel() { return riskLevel; }

    public boolean hasThumbnail() { return hasThumbnail; }
    public void setHasThumbnail(boolean hasThumbnail) { this.hasThumbnail = hasThumbnail; }

    public int getThumbnailWidth() { return thumbnailWidth; }
    public void setThumbnailWidth(int thumbnailWidth) { this.thumbnailWidth = thumbnailWidth; }

    public int getThumbnailHeight() { return thumbnailHeight; }
    public void setThumbnailHeight(int thumbnailHeight) { this.thumbnailHeight = thumbnailHeight; }

    /**
     * Returns all hashes as a map.
     */
    public Map<String, String> getHashes() {
        Map<String, String> hashes = new LinkedHashMap<>();
        if (md5 != null) hashes.put("MD5", md5);
        if (sha1 != null) hashes.put("SHA-1", sha1);
        if (sha256 != null) hashes.put("SHA-256", sha256);
        return hashes;
    }

    /**
     * Returns entropy bar visualization.
     */
    public String getEntropyBar() {
        int filled = (int) (entropy / 8.0 * 20);
        int empty = 20 - filled;
        return "█".repeat(filled) + "░".repeat(empty) + String.format(" %.2f/8.0 bits", entropy);
    }

    /**
     * Returns risk score bar visualization.
     */
    public String getRiskBar() {
        int filled = riskScore / 5;
        int empty = 20 - filled;
        return "█".repeat(filled) + "░".repeat(empty) + String.format(" %d/100", riskScore);
    }

    private String categorizeEntropy(double e) {
        if (e < 1.0) return "Very Low (structured data)";
        if (e < 3.5) return "Low (text/document)";
        if (e < 5.0) return "Normal (typical file)";
        if (e < 7.0) return "High (compressed data)";
        if (e < 7.5) return "Very High (encrypted/compressed)";
        return "Suspicious (possible encryption/steganography)";
    }

    private String categorizeRisk(int score) {
        if (score <= 10) return "SAFE";
        if (score <= 30) return "LOW";
        if (score <= 55) return "MEDIUM";
        if (score <= 80) return "HIGH";
        return "CRITICAL";
    }
}
