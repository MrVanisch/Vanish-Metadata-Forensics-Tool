package com.vanish.forensics.report;

import com.vanish.forensics.analyzer.BatchAnalyzer;
import com.vanish.forensics.model.*;
import com.vanish.forensics.ui.ConsoleColors;

import java.util.List;
import java.util.Map;

/**
 * Generates clean, readable console reports showing ONLY important information.
 * Uses a dashboard-style layout with risk scoring and color-coded sections.
 */
public class ConsoleReportGenerator implements ReportGenerator {

    private static final int WIDTH = 58;
    private static final String LINE = ConsoleColors.CYAN + "─".repeat(WIDTH) + ConsoleColors.RESET;
    private static final String DOUBLE = ConsoleColors.CYAN + "═".repeat(WIDTH) + ConsoleColors.RESET;

    @Override
    public void generate(FileMetadata metadata) {
        System.out.println();

        // ===== HEADER =====
        printBox("🔍 VANISH — Metadata Analysis Report");

        // ===== RISK DASHBOARD =====
        if (metadata.hasForensicData()) {
            printRiskDashboard(metadata);
        }

        // ===== FILE INFO =====
        printSectionHeader("📁 FILE");
        printKV("Name", metadata.getFileName());
        printKV("Size", metadata.getFormattedFileSize());
        printKV("Type", metadata.getMimeType());
        printKV("Modified", metadata.getLastModified());

        // ===== FORENSIC DATA =====
        if (metadata.hasForensicData()) {
            ForensicData f = metadata.getForensicData();

            printSectionHeader("🔑 HASHES");
            printKV("MD5", f.getMd5());
            printKV("SHA-1", f.getSha1());
            printKV("SHA-256", f.getSha256());

            printSectionHeader("🧬 FORENSIC");
            printKV("Entropy", f.getEntropyBar());
            printKV("Category", f.getEntropyCategory());
            printKV("Signature", f.getMagicBytes() + " → " + f.getDetectedType());

            if (!f.isExtensionMatch()) {
                System.out.println(ConsoleColors.RED + ConsoleColors.BOLD +
                        "  ⚠ EXTENSION MISMATCH!" + ConsoleColors.RESET);
                System.out.println(ConsoleColors.RED +
                        "    " + f.getExtensionMismatchWarning() + ConsoleColors.RESET);
            } else {
                printKV("Extension", "✅ Matches file content");
            }

            if (f.hasThumbnail()) {
                printKV("Thumbnail", "⚠ Embedded (" + f.getThumbnailWidth() + "x" + f.getThumbnailHeight() + ")");
            }
        }

        // ===== EXIF — only important fields =====
        if (metadata.hasExifData()) {
            ExifData exif = metadata.getExifData();

            // Only print if we have meaningful data
            boolean hasCamera = exif.getCameraMake() != null || exif.getCameraModel() != null;
            boolean hasPhoto = exif.getExposureTime() != null || exif.getIso() != null;

            if (hasCamera || hasPhoto) {
                printSectionHeader("📷 CAMERA");
                if (exif.getCameraMake() != null && exif.getCameraModel() != null) {
                    printKV("Device", exif.getCameraMake() + " " + exif.getCameraModel());
                } else {
                    printKV("Make", exif.getCameraMake());
                    printKV("Model", exif.getCameraModel());
                }
                printKV("Lens", exif.getLensModel());
                printKV("Serial", highlightSensitive(exif.getSerialNumber()));
                printKV("Software", exif.getSoftware());
                if (exif.getResolution() != null) {
                    printKV("Resolution", exif.getResolution());
                }
            }

            if (hasPhoto) {
                printSectionHeader("📸 SHOOTING");
                printKV("Date", exif.getDateTimeOriginal());
                // Build compact settings line
                StringBuilder settings = new StringBuilder();
                if (exif.getExposureTime() != null) settings.append(exif.getExposureTime());
                if (exif.getFNumber() != null) settings.append("  ").append(exif.getFNumber());
                if (exif.getIso() != null) settings.append("  ISO ").append(exif.getIso());
                if (exif.getFocalLength() != null) settings.append("  ").append(exif.getFocalLength());
                if (settings.length() > 0) {
                    printKV("Settings", settings.toString());
                }
                printKV("Flash", exif.getFlash());
            }

            // GPS — CRITICAL — always very prominent
            if (exif.hasGps()) {
                GpsCoordinates gps = exif.getGpsCoordinates();
                System.out.println();
                System.out.println(ConsoleColors.RED + "  ┌──────────────────────────────────────────────────┐" + ConsoleColors.RESET);
                System.out.println(ConsoleColors.RED + "  │  🔴 GPS LOCATION DETECTED                       │" + ConsoleColors.RESET);
                System.out.println(ConsoleColors.RED + "  ├──────────────────────────────────────────────────┤" + ConsoleColors.RESET);
                System.out.println(ConsoleColors.RED + "  │  " + ConsoleColors.BOLD + ConsoleColors.WHITE +
                        String.format("%-48s", gps.toDMSString()) + ConsoleColors.RED + "  │" + ConsoleColors.RESET);
                System.out.println(ConsoleColors.RED + "  │  " + ConsoleColors.WHITE +
                        String.format("%-48s", gps.toString()) + ConsoleColors.RED + "  │" + ConsoleColors.RESET);
                System.out.println(ConsoleColors.RED + "  │  " + ConsoleColors.CYAN +
                        String.format("%-48s", gps.toGoogleMapsUrl()) + ConsoleColors.RED + "  │" + ConsoleColors.RESET);
                System.out.println(ConsoleColors.RED + "  └──────────────────────────────────────────────────┘" + ConsoleColors.RESET);
            }
        }

        // ===== DOCUMENT — only important fields =====
        if (metadata.hasDocumentData()) {
            DocumentData doc = metadata.getDocumentData();
            boolean hasContent = doc.getAuthor() != null || doc.getTitle() != null ||
                    doc.getCompany() != null || doc.getCreator() != null;

            if (hasContent) {
                printSectionHeader("📄 DOCUMENT");
                printKV("Title", doc.getTitle());
                printKV("Author", highlightSensitive(doc.getAuthor()));
                printKV("Last Author", highlightSensitive(doc.getLastAuthor()));
                printKV("Company", highlightSensitive(doc.getCompany()));
                printKV("Manager", highlightSensitive(doc.getManager()));
                printKV("Created", doc.getCreationDate());
                printKV("Modified", doc.getModificationDate());
                printKV("Creator App", doc.getCreator());
                printKV("Producer", doc.getProducer());

                // Compact stats line
                StringBuilder stats = new StringBuilder();
                if (doc.getPageCount() > 0) stats.append(doc.getPageCount()).append(" pages");
                if (doc.getWordCount() > 0) {
                    if (stats.length() > 0) stats.append(" • ");
                    stats.append(doc.getWordCount()).append(" words");
                }
                if (doc.getRevision() != null) {
                    if (stats.length() > 0) stats.append(" • ");
                    stats.append("Rev ").append(doc.getRevision());
                }
                if (stats.length() > 0) printKV("Stats", stats.toString());
            }
        }

        // ===== MEDIA (Audio/Video) =====
        if (metadata.hasMediaData()) {
            MediaData media = metadata.getMediaData();
            printSectionHeader("📊 MEDIA");
            printKV("Title", media.getTitle());
            printKV("Artist", highlightSensitive(media.getArtist()));
            printKV("Album", media.getAlbum());
            printKV("Date", media.getDate());
            printKV("Duration", media.getDuration());
            
            if (media.getWidth() > 0) {
                printKV("Resolution", media.getResolution());
                if (media.getFrameRate() != null) {
                    printKV("Frame Rate", String.format("%.2f fps", media.getFrameRate()));
                }
                printKV("Video Codec", media.getVideoCodec());
            }
            
            printKV("Audio Codec", media.getAudioCodec());
            printKV("Bitrate", media.getBitrate());
            printKV("Software", media.getSoftware());

            if (media.hasGps()) {
                GpsCoordinates gps = media.getGpsCoordinates();
                System.out.println();
                System.out.println(ConsoleColors.RED + "  ┌──────────────────────────────────────────────────┐" + ConsoleColors.RESET);
                System.out.println(ConsoleColors.RED + "  │  🔴 VIDEO GPS LOCATION DETECTED                  │" + ConsoleColors.RESET);
                System.out.println(ConsoleColors.RED + "  ├──────────────────────────────────────────────────┤" + ConsoleColors.RESET);
                System.out.println(ConsoleColors.RED + "  │  " + ConsoleColors.BOLD + ConsoleColors.WHITE +
                        String.format("%-48s", gps.toDMSString()) + ConsoleColors.RED + "  │" + ConsoleColors.RESET);
                System.out.println(ConsoleColors.RED + "  │  " + ConsoleColors.CYAN +
                        String.format("%-48s", gps.toGoogleMapsUrl()) + ConsoleColors.RED + "  │" + ConsoleColors.RESET);
                System.out.println(ConsoleColors.RED + "  └──────────────────────────────────────────────────┘" + ConsoleColors.RESET);
            }
        }

        // ===== ALERTS SUMMARY =====
        if (metadata.hasAlerts()) {
            printSectionHeader("⚠️  ALERTS (" + metadata.getAlerts().size() + ")");

            // Group alerts by severity, show most severe first
            for (SensitiveDataAlert.Severity severity : new SensitiveDataAlert.Severity[]{
                    SensitiveDataAlert.Severity.CRITICAL,
                    SensitiveDataAlert.Severity.HIGH,
                    SensitiveDataAlert.Severity.MEDIUM,
                    SensitiveDataAlert.Severity.LOW}) {

                for (SensitiveDataAlert alert : metadata.getAlerts()) {
                    if (alert.getSeverity() != severity) continue;

                    String color = getSeverityColor(severity);
                    System.out.printf("  %s%-8s%s %-16s %s%s%s%n",
                            color, severity.getIcon() + severity.getLabel(), ConsoleColors.RESET,
                            alert.getFieldName(),
                            ConsoleColors.DIM, truncate(alert.getFieldValue(), 30), ConsoleColors.RESET);
                }
            }

            // One-line summary bar
            System.out.println();
            Map<SensitiveDataAlert.Severity, Integer> counts = metadata.getAlertCountBySeverity();
            StringBuilder summary = new StringBuilder("  ");
            addBadge(summary, "🔴", counts.getOrDefault(SensitiveDataAlert.Severity.CRITICAL, 0), ConsoleColors.RED);
            addBadge(summary, "🔶", counts.getOrDefault(SensitiveDataAlert.Severity.HIGH, 0), ConsoleColors.YELLOW);
            addBadge(summary, "⚠️", counts.getOrDefault(SensitiveDataAlert.Severity.MEDIUM, 0), ConsoleColors.BLUE);
            addBadge(summary, "ℹ️", counts.getOrDefault(SensitiveDataAlert.Severity.LOW, 0), ConsoleColors.DIM);
            System.out.println(summary);
        } else {
            System.out.println();
            System.out.println(ConsoleColors.GREEN + "  ✅ No sensitive data detected" + ConsoleColors.RESET);
        }

        // ===== RAW METADATA TABLE =====
        if (!metadata.getRawMetadata().isEmpty()) {
            printRawMetadataTable(metadata);
        }

        // ===== FOOTER =====
        System.out.println();
        System.out.println(ConsoleColors.DIM + "  📊 Total metadata fields: " +
                metadata.getRawMetadata().size() + ConsoleColors.RESET);
        System.out.println(DOUBLE);
        System.out.println();
    }

    /**
     * Prints a detailed table of ALL raw metadata fields.
     */
    private void printRawMetadataTable(FileMetadata metadata) {
        printSectionHeader("📐 RAW METADATA TABLE");
        
        Map<String, String> raw = metadata.getRawMetadata();
        if (raw.isEmpty()) return;

        // Header
        System.out.println(ConsoleColors.CYAN + "  ┌" + "─".repeat(25) + "┬" + "─".repeat(28) + "┐" + ConsoleColors.RESET);
        System.out.printf("  %s│ %-23s │ %-26s │%s%n", 
                ConsoleColors.CYAN, ConsoleColors.BOLD + "TAG NAME" + ConsoleColors.RESET + ConsoleColors.CYAN, 
                ConsoleColors.BOLD + "VALUE" + ConsoleColors.RESET + ConsoleColors.CYAN, ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN + "  ├" + "─".repeat(25) + "┼" + "─".repeat(28) + "┤" + ConsoleColors.RESET);

        // Sort keys for better table readability
        java.util.TreeMap<String, String> sorted = new java.util.TreeMap<>(raw);
        
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            String tag = entry.getKey();
            String val = entry.getValue();

            // Truncate to fit table width
            String truncatedTag = truncate(tag, 23);
            String truncatedVal = truncate(val, 26);

            System.out.printf("  %s│%s %-23s %s│%s %-26s %s│%s%n",
                    ConsoleColors.CYAN, ConsoleColors.RESET, truncatedTag, ConsoleColors.CYAN,
                    ConsoleColors.WHITE, truncatedVal, ConsoleColors.CYAN, ConsoleColors.RESET);
        }

        System.out.println(ConsoleColors.CYAN + "  └" + "─".repeat(25) + "┴" + "─".repeat(28) + "┘" + ConsoleColors.RESET);
    }

    /**
     * Prints the risk dashboard — visual summary of privacy risk.
     */
    private void printRiskDashboard(FileMetadata metadata) {
        ForensicData f = metadata.getForensicData();
        String riskColor = getRiskColor(f.getRiskLevel());

        System.out.println();
        System.out.println("  ┌────────────────────────────────────────────────────┐");
        System.out.println("  │  " + riskColor + ConsoleColors.BOLD +
                "PRIVACY RISK: " + f.getRiskLevel() +
                ConsoleColors.RESET + "                                    │");
        System.out.println("  │  " + riskColor + f.getRiskBar() +
                ConsoleColors.RESET + "                 │");
        System.out.println("  └────────────────────────────────────────────────────┘");
    }

    @Override
    public void generateBatch(List<FileMetadata> results, BatchAnalyzer.BatchStatistics stats) {
        System.out.println();
        printBox("📊 VANISH — Batch Report");

        printSectionHeader("📈 OVERVIEW");
        printKV("Files", String.valueOf(stats.getTotalFiles()));
        printKV("Total Size", stats.getFormattedTotalSize());
        printKV("With Alerts", stats.getFilesWithAlerts() + "/" + stats.getTotalFiles());
        printKV("Total Alerts", String.valueOf(stats.getTotalAlerts()));

        if (stats.getTotalAlerts() > 0) {
            printSectionHeader("⚠️  SEVERITY BREAKDOWN");
            Map<SensitiveDataAlert.Severity, Integer> bySev = stats.getAlertsBySeverity();
            for (SensitiveDataAlert.Severity sev : SensitiveDataAlert.Severity.values()) {
                int count = bySev.getOrDefault(sev, 0);
                if (count > 0) {
                    String color = getSeverityColor(sev);
                    String bar = "█".repeat(Math.min(count, 30));
                    System.out.printf("  %s%-10s %s (%d)%s%n",
                            color, sev.getLabel(), bar, count, ConsoleColors.RESET);
                }
            }
        }

        printSectionHeader("📝 FILES");
        for (FileMetadata meta : results) {
            String riskInfo;
            if (meta.hasForensicData()) {
                String riskColor = getRiskColor(meta.getForensicData().getRiskLevel());
                riskInfo = riskColor + "[" + meta.getForensicData().getRiskLevel() +
                        " " + meta.getForensicData().getRiskScore() + "/100]" + ConsoleColors.RESET;
            } else if (meta.hasAlerts()) {
                riskInfo = ConsoleColors.YELLOW + "[" + meta.getAlerts().size() + " alerts]" + ConsoleColors.RESET;
            } else {
                riskInfo = ConsoleColors.GREEN + "[SAFE]" + ConsoleColors.RESET;
            }
            System.out.printf("  %-35s %s%n", truncate(meta.getFileName(), 35), riskInfo);
        }

        System.out.println(DOUBLE);
        System.out.println();
    }

    @Override
    public String getName() {
        return "Console Report";
    }

    // ───────────────────── HELPERS ─────────────────────

    private void printBox(String title) {
        System.out.println(ConsoleColors.CYAN + "  ╔" + "═".repeat(WIDTH - 4) + "╗" + ConsoleColors.RESET);
        int padding = WIDTH - 6 - title.length();
        System.out.println(ConsoleColors.CYAN + "  ║  " + ConsoleColors.BOLD + ConsoleColors.WHITE +
                title + " ".repeat(Math.max(0, padding)) + ConsoleColors.CYAN + "  ║" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN + "  ╚" + "═".repeat(WIDTH - 4) + "╝" + ConsoleColors.RESET);
    }

    private void printSectionHeader(String title) {
        System.out.println();
        System.out.println(ConsoleColors.CYAN + ConsoleColors.BOLD + "  " + title + ConsoleColors.RESET);
        System.out.println("  " + ConsoleColors.CYAN + "─".repeat(WIDTH - 4) + ConsoleColors.RESET);
    }

    private void printKV(String key, String value) {
        if (value == null || value.isEmpty()) return;
        System.out.printf("  %s%-15s%s %s%n",
                ConsoleColors.DIM, key, ConsoleColors.RESET, value);
    }

    private String highlightSensitive(String value) {
        if (value == null || value.isEmpty()) return null;
        return ConsoleColors.YELLOW + ConsoleColors.BOLD + value +
                ConsoleColors.RESET + ConsoleColors.RED + " ◄" + ConsoleColors.RESET;
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen - 3) + "..." : text;
    }

    private void addBadge(StringBuilder sb, String icon, int count, String color) {
        if (count > 0) {
            sb.append(color).append(icon).append(" ").append(count).append("  ").append(ConsoleColors.RESET);
        }
    }

    private String getSeverityColor(SensitiveDataAlert.Severity severity) {
        switch (severity) {
            case CRITICAL: return ConsoleColors.RED;
            case HIGH: return ConsoleColors.YELLOW;
            case MEDIUM: return ConsoleColors.BLUE;
            case LOW: return ConsoleColors.DIM;
            default: return ConsoleColors.RESET;
        }
    }

    private String getRiskColor(String riskLevel) {
        switch (riskLevel) {
            case "CRITICAL": return ConsoleColors.RED + ConsoleColors.BOLD;
            case "HIGH": return ConsoleColors.YELLOW + ConsoleColors.BOLD;
            case "MEDIUM": return ConsoleColors.BLUE;
            case "LOW": return ConsoleColors.GREEN;
            case "SAFE": return ConsoleColors.GREEN + ConsoleColors.BOLD;
            default: return ConsoleColors.RESET;
        }
    }
}
