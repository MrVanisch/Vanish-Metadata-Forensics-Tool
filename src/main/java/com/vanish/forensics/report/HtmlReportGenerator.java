package com.vanish.forensics.report;

import com.vanish.forensics.analyzer.BatchAnalyzer;
import com.vanish.forensics.model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Generates professional HTML reports with a modern dark theme.
 * Reports include interactive elements: GPS map links, color-coded alerts, 
 * and a responsive layout ready for viewing in any browser.
 */
public class HtmlReportGenerator implements ReportGenerator {

    private final String outputDirectory;

    public HtmlReportGenerator(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public void generate(FileMetadata metadata) throws IOException {
        StringBuilder html = new StringBuilder();
        buildHtmlHeader(html, "Vanish Report — " + metadata.getFileName());

        // Hero section
        html.append("<div class='hero'>\n");
        html.append("  <h1>🔍 VANISH</h1>\n");
        html.append("  <p class='subtitle'>Metadata Forensics Report</p>\n");
        html.append("  <p class='timestamp'>Generated: ").append(getTimestamp()).append("</p>\n");
        html.append("</div>\n");

        // File info card
        html.append("<div class='card'>\n");
        html.append("  <h2>📁 File Information</h2>\n");
        html.append("  <table>\n");
        addRow(html, "File Name", metadata.getFileName());
        addRow(html, "File Path", metadata.getFilePath());
        addRow(html, "File Size", metadata.getFormattedFileSize());
        addRow(html, "MIME Type", metadata.getMimeType());
        addRow(html, "Extension", metadata.getFileExtension());
        addRow(html, "Last Modified", metadata.getLastModified());
        html.append("  </table>\n");
        html.append("</div>\n");

        // Forensic data card
        if (metadata.hasForensicData()) {
            ForensicData f = metadata.getForensicData();

            // Risk score card
            html.append("<div class='card risk-card risk-").append(f.getRiskLevel().toLowerCase()).append("'>\n");
            html.append("  <h2>🛡️ Privacy Risk Score</h2>\n");
            html.append("  <div class='risk-display'>\n");
            html.append("    <span class='risk-score'>").append(f.getRiskScore()).append("</span>\n");
            html.append("    <span class='risk-label'>/ 100 — ").append(f.getRiskLevel()).append("</span>\n");
            html.append("  </div>\n");
            html.append("  <div class='risk-bar-container'>\n");
            html.append("    <div class='risk-bar' style='width:").append(f.getRiskScore()).append("%;'></div>\n");
            html.append("  </div>\n");
            html.append("</div>\n");

            // Hashes card
            html.append("<div class='card'>\n");
            html.append("  <h2>🔑 Cryptographic Hashes</h2>\n");
            html.append("  <table>\n");
            addRow(html, "MD5", f.getMd5());
            addRow(html, "SHA-1", f.getSha1());
            addRow(html, "SHA-256", f.getSha256());
            html.append("  </table>\n");
            html.append("</div>\n");

            // Forensic analysis card
            html.append("<div class='card'>\n");
            html.append("  <h2>🧬 Forensic Analysis</h2>\n");
            html.append("  <table>\n");
            addRow(html, "Entropy", String.format("%.4f / 8.0 bits — %s", f.getEntropy(), f.getEntropyCategory()));
            addRow(html, "Magic Bytes", f.getMagicBytes());
            addRow(html, "Detected Type", f.getDetectedType());
            addRow(html, "Extension Match", f.isExtensionMatch() ? "✅ Matches" : "⚠️ MISMATCH");
            if (!f.isExtensionMatch()) {
                addRow(html, "Warning", f.getExtensionMismatchWarning());
            }
            html.append("  </table>\n");
            html.append("</div>\n");
        }

        // EXIF card
        if (metadata.hasExifData()) {
            ExifData exif = metadata.getExifData();
            html.append("<div class='card'>\n");
            html.append("  <h2>📷 Camera & EXIF Data</h2>\n");
            html.append("  <table>\n");
            addRow(html, "Camera Make", exif.getCameraMake());
            addRow(html, "Camera Model", exif.getCameraModel());
            addRow(html, "Lens", exif.getLensModel());
            addRow(html, "Serial Number", exif.getSerialNumber());
            addRow(html, "Software", exif.getSoftware());
            addRow(html, "Resolution", exif.getResolution());
            addRow(html, "Date Taken", exif.getDateTimeOriginal());
            addRow(html, "Exposure", exif.getExposureTime());
            addRow(html, "Aperture", exif.getFNumber());
            addRow(html, "ISO", exif.getIso());
            addRow(html, "Focal Length", exif.getFocalLength());
            addRow(html, "Flash", exif.getFlash());
            addRow(html, "White Balance", exif.getWhiteBalance());
            addRow(html, "Orientation", exif.getOrientation());
            html.append("  </table>\n");

            // GPS subsection
            if (exif.hasGps()) {
                GpsCoordinates gps = exif.getGpsCoordinates();
                html.append("  <div class='gps-alert'>\n");
                html.append("    <h3>🌍 GPS Location Found</h3>\n");
                html.append("    <p><strong>Coordinates:</strong> ").append(gps.toString()).append("</p>\n");
                html.append("    <p><strong>DMS:</strong> ").append(gps.toDMSString()).append("</p>\n");
                html.append("    <div class='map-links'>\n");
                html.append("      <a href='").append(gps.toGoogleMapsUrl()).append("' target='_blank' class='btn'>📍 Google Maps</a>\n");
                html.append("      <a href='").append(gps.toOpenStreetMapUrl()).append("' target='_blank' class='btn btn-secondary'>🗺️ OpenStreetMap</a>\n");
                html.append("    </div>\n");
                html.append("  </div>\n");
            }
            html.append("</div>\n");
        }

        // Document data card
        if (metadata.hasDocumentData()) {
            DocumentData doc = metadata.getDocumentData();
            html.append("<div class='card'>\n");
            html.append("  <h2>📄 Document Metadata</h2>\n");
            html.append("  <table>\n");
            addRow(html, "Title", doc.getTitle());
            addRow(html, "Author", doc.getAuthor());
            addRow(html, "Last Author", doc.getLastAuthor());
            addRow(html, "Company", doc.getCompany());
            addRow(html, "Manager", doc.getManager());
            addRow(html, "Subject", doc.getSubject());
            addRow(html, "Keywords", doc.getKeywords());
            addRow(html, "Category", doc.getCategory());
            addRow(html, "Creator App", doc.getCreator());
            addRow(html, "Producer", doc.getProducer());
            addRow(html, "Language", doc.getLanguage());
            addRow(html, "Created", doc.getCreationDate());
            addRow(html, "Modified", doc.getModificationDate());
            addRow(html, "Revisions", doc.getRevision());
            if (doc.getPageCount() > 0) addRow(html, "Pages", String.valueOf(doc.getPageCount()));
            if (doc.getWordCount() > 0) addRow(html, "Words", String.valueOf(doc.getWordCount()));
            if (doc.getCharacterCount() > 0) addRow(html, "Characters", String.valueOf(doc.getCharacterCount()));
            html.append("  </table>\n");
            html.append("</div>\n");
        }

        // Alerts card
        if (metadata.hasAlerts()) {
            html.append("<div class='card'>\n");
            html.append("  <h2>⚠️ Sensitive Data Alerts (").append(metadata.getAlerts().size()).append(")</h2>\n");

            // Summary badges
            Map<SensitiveDataAlert.Severity, Integer> counts = metadata.getAlertCountBySeverity();
            html.append("  <div class='alert-summary'>\n");
            for (SensitiveDataAlert.Severity sev : SensitiveDataAlert.Severity.values()) {
                int count = counts.getOrDefault(sev, 0);
                if (count > 0) {
                    html.append("    <span class='badge badge-").append(sev.name().toLowerCase())
                            .append("'>").append(sev.getIcon()).append(" ").append(sev.getLabel())
                            .append(": ").append(count).append("</span>\n");
                }
            }
            html.append("  </div>\n");

            // Individual alerts
            for (SensitiveDataAlert alert : metadata.getAlerts()) {
                html.append("  <div class='alert alert-").append(alert.getSeverity().name().toLowerCase()).append("'>\n");
                html.append("    <div class='alert-header'>").append(alert.getSeverity().getIcon())
                        .append(" [").append(alert.getSeverity().getLabel()).append("] ")
                        .append(escapeHtml(alert.getFieldName())).append("</div>\n");
                html.append("    <div class='alert-value'>Value: <code>")
                        .append(escapeHtml(alert.getFieldValue())).append("</code></div>\n");
                html.append("    <div class='alert-desc'>").append(escapeHtml(alert.getDescription())).append("</div>\n");
                html.append("    <div class='alert-fix'>💡 ").append(escapeHtml(alert.getRemediation())).append("</div>\n");
                html.append("  </div>\n");
            }
            html.append("</div>\n");
        }

        // Raw metadata card (collapsible)
        if (!metadata.getRawMetadata().isEmpty()) {
            html.append("<div class='card'>\n");
            html.append("  <details>\n");
            html.append("    <summary><h2 style='display:inline'>🗂️ All Raw Metadata (")
                    .append(metadata.getRawMetadata().size()).append(" fields)</h2></summary>\n");
            html.append("    <table>\n");
            for (Map.Entry<String, String> entry : metadata.getRawMetadata().entrySet()) {
                addRow(html, entry.getKey(), entry.getValue());
            }
            html.append("    </table>\n");
            html.append("  </details>\n");
            html.append("</div>\n");
        }

        buildHtmlFooter(html);

        String fileName = "vanish_report_" + sanitize(metadata.getFileName()) + ".html";
        writeFile(html.toString(), fileName);
    }

    @Override
    public void generateBatch(List<FileMetadata> results, BatchAnalyzer.BatchStatistics stats) throws IOException {
        StringBuilder html = new StringBuilder();
        buildHtmlHeader(html, "Vanish Batch Report");

        html.append("<div class='hero'>\n");
        html.append("  <h1>📊 VANISH</h1>\n");
        html.append("  <p class='subtitle'>Batch Analysis Report</p>\n");
        html.append("  <p class='timestamp'>Generated: ").append(getTimestamp()).append("</p>\n");
        html.append("</div>\n");

        // Stats overview
        html.append("<div class='card'>\n");
        html.append("  <h2>📈 Overview</h2>\n");
        html.append("  <div class='stats-grid'>\n");
        html.append("    <div class='stat'><span class='stat-num'>").append(stats.getTotalFiles()).append("</span><span class='stat-label'>Files</span></div>\n");
        html.append("    <div class='stat'><span class='stat-num'>").append(stats.getFormattedTotalSize()).append("</span><span class='stat-label'>Total Size</span></div>\n");
        html.append("    <div class='stat'><span class='stat-num'>").append(stats.getFilesWithAlerts()).append("</span><span class='stat-label'>With Alerts</span></div>\n");
        html.append("    <div class='stat'><span class='stat-num'>").append(stats.getTotalAlerts()).append("</span><span class='stat-label'>Total Alerts</span></div>\n");
        html.append("  </div>\n");
        html.append("</div>\n");

        // Per-file summary table
        html.append("<div class='card'>\n");
        html.append("  <h2>📝 File Results</h2>\n");
        html.append("  <table>\n");
        html.append("    <tr><th>File</th><th>Type</th><th>Size</th><th>Alerts</th><th>Highest</th></tr>\n");
        for (FileMetadata meta : results) {
            html.append("    <tr>");
            html.append("<td>").append(escapeHtml(meta.getFileName())).append("</td>");
            html.append("<td>").append(escapeHtml(meta.getMimeType())).append("</td>");
            html.append("<td>").append(meta.getFormattedFileSize()).append("</td>");
            html.append("<td>").append(meta.getAlerts().size()).append("</td>");
            SensitiveDataAlert.Severity highest = meta.getHighestSeverity();
            html.append("<td class='severity-").append(highest != null ? highest.name().toLowerCase() : "none")
                    .append("'>").append(highest != null ? highest.getIcon() + " " + highest.getLabel() : "✅ Clean")
                    .append("</td>");
            html.append("</tr>\n");
        }
        html.append("  </table>\n");
        html.append("</div>\n");

        buildHtmlFooter(html);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        writeFile(html.toString(), "vanish_batch_report_" + timestamp + ".html");
    }

    @Override
    public String getName() {
        return "HTML Report";
    }

    // --- HTML Building Helpers ---

    private void buildHtmlHeader(StringBuilder html, String title) {
        html.append("<!DOCTYPE html>\n<html lang='en'>\n<head>\n");
        html.append("  <meta charset='UTF-8'>\n");
        html.append("  <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        html.append("  <title>").append(escapeHtml(title)).append("</title>\n");
        html.append("  <style>\n");
        html.append(getCSS());
        html.append("  </style>\n");
        html.append("</head>\n<body>\n<div class='container'>\n");
    }

    private void buildHtmlFooter(StringBuilder html) {
        html.append("<div class='footer'>\n");
        html.append("  <p>Generated by <strong>Vanish Metadata Forensics Tool</strong> v1.0.0</p>\n");
        html.append("</div>\n");
        html.append("</div>\n</body>\n</html>");
    }

    private void addRow(StringBuilder html, String key, String value) {
        if (value == null || value.isEmpty()) return;
        html.append("    <tr><td class='key'>").append(escapeHtml(key))
                .append("</td><td>").append(escapeHtml(value)).append("</td></tr>\n");
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    private String getTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private void writeFile(String content, String fileName) throws IOException {
        File outputDir = new File(outputDirectory);
        if (!outputDir.exists()) outputDir.mkdirs();

        File file = new File(outputDir, fileName);
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.print(content);
        }
        System.out.println("  🌐 HTML report saved to: " + file.getAbsolutePath());
    }

    /**
     * Returns the CSS stylesheet for the HTML report.
     * Modern dark theme with glassmorphism, gradients, and responsive layout.
     */
    private String getCSS() {
        return """
            * { margin: 0; padding: 0; box-sizing: border-box; }
            body {
                font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
                background: linear-gradient(135deg, #0a0a1a 0%, #1a1a2e 50%, #16213e 100%);
                color: #e0e0e0;
                min-height: 100vh;
                line-height: 1.6;
            }
            .container { max-width: 900px; margin: 0 auto; padding: 20px; }
            .hero {
                text-align: center; padding: 40px 20px; margin-bottom: 30px;
                background: linear-gradient(135deg, rgba(0,212,255,0.1), rgba(138,43,226,0.1));
                border-radius: 16px; border: 1px solid rgba(255,255,255,0.05);
            }
            .hero h1 {
                font-size: 2.5em; margin-bottom: 8px;
                background: linear-gradient(135deg, #00d4ff, #8a2be2);
                -webkit-background-clip: text; -webkit-text-fill-color: transparent;
            }
            .subtitle { font-size: 1.2em; color: #888; }
            .timestamp { font-size: 0.85em; color: #666; margin-top: 8px; }
            .card {
                background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.06);
                border-radius: 12px; padding: 24px; margin-bottom: 20px;
                backdrop-filter: blur(10px);
            }
            .card h2 { color: #00d4ff; margin-bottom: 16px; font-size: 1.3em; }
            table { width: 100%; border-collapse: collapse; }
            td, th { padding: 8px 12px; text-align: left; border-bottom: 1px solid rgba(255,255,255,0.05); }
            th { color: #00d4ff; font-weight: 600; }
            .key { color: #888; font-weight: 500; width: 35%; }
            code { background: rgba(0,212,255,0.1); padding: 2px 6px; border-radius: 4px; font-size: 0.9em; }
            .gps-alert {
                margin-top: 16px; padding: 16px; border-radius: 8px;
                background: rgba(255,59,48,0.1); border: 1px solid rgba(255,59,48,0.3);
            }
            .gps-alert h3 { color: #ff3b30; margin-bottom: 8px; }
            .map-links { margin-top: 12px; display: flex; gap: 10px; }
            .btn {
                display: inline-block; padding: 8px 16px; border-radius: 6px;
                background: linear-gradient(135deg, #00d4ff, #0099cc); color: #fff;
                text-decoration: none; font-weight: 500; transition: transform 0.2s;
            }
            .btn:hover { transform: translateY(-2px); }
            .btn-secondary { background: linear-gradient(135deg, #8a2be2, #6a1fb2); }
            .alert {
                margin: 12px 0; padding: 14px; border-radius: 8px;
                border-left: 4px solid #666;
            }
            .alert-critical { border-color: #ff3b30; background: rgba(255,59,48,0.08); }
            .alert-high { border-color: #ff9500; background: rgba(255,149,0,0.08); }
            .alert-medium { border-color: #5ac8fa; background: rgba(90,200,250,0.08); }
            .alert-low { border-color: #636366; background: rgba(99,99,102,0.08); }
            .alert-header { font-weight: 600; margin-bottom: 6px; }
            .alert-value { font-size: 0.9em; margin-bottom: 4px; }
            .alert-desc { font-size: 0.85em; color: #aaa; margin-bottom: 4px; }
            .alert-fix { font-size: 0.85em; color: #30d158; }
            .alert-summary { display: flex; gap: 10px; margin-bottom: 16px; flex-wrap: wrap; }
            .badge {
                padding: 4px 12px; border-radius: 20px; font-size: 0.85em; font-weight: 500;
            }
            .badge-critical { background: rgba(255,59,48,0.2); color: #ff6961; }
            .badge-high { background: rgba(255,149,0,0.2); color: #ffb347; }
            .badge-medium { background: rgba(90,200,250,0.2); color: #5ac8fa; }
            .badge-low { background: rgba(99,99,102,0.2); color: #aaa; }
            .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 16px; }
            .stat {
                text-align: center; padding: 20px;
                background: rgba(0,212,255,0.05); border-radius: 8px;
                border: 1px solid rgba(0,212,255,0.1);
            }
            .stat-num { display: block; font-size: 1.8em; font-weight: 700; color: #00d4ff; }
            .stat-label { display: block; font-size: 0.85em; color: #888; margin-top: 4px; }
            .severity-critical { color: #ff3b30; font-weight: 600; }
            .severity-high { color: #ff9500; font-weight: 600; }
            .severity-medium { color: #5ac8fa; }
            .severity-low { color: #aaa; }
            .severity-none { color: #30d158; }
            details summary { cursor: pointer; }
            details summary:hover { color: #00d4ff; }
            .footer { text-align: center; padding: 30px; color: #666; font-size: 0.85em; }
            .risk-display { display: flex; align-items: baseline; gap: 12px; margin-bottom: 12px; }
            .risk-score { font-size: 3em; font-weight: 800; }
            .risk-label { font-size: 1.1em; color: #888; }
            .risk-bar-container {
                height: 8px; background: rgba(255,255,255,0.1);
                border-radius: 4px; overflow: hidden;
            }
            .risk-bar {
                height: 100%; border-radius: 4px;
                transition: width 0.5s ease;
            }
            .risk-safe .risk-score { color: #30d158; }
            .risk-safe .risk-bar { background: #30d158; }
            .risk-low .risk-score { color: #30d158; }
            .risk-low .risk-bar { background: #30d158; }
            .risk-medium .risk-score { color: #5ac8fa; }
            .risk-medium .risk-bar { background: #5ac8fa; }
            .risk-high .risk-score { color: #ff9500; }
            .risk-high .risk-bar { background: linear-gradient(90deg, #ff9500, #ff3b30); }
            .risk-critical .risk-score { color: #ff3b30; }
            .risk-critical .risk-bar { background: linear-gradient(90deg, #ff3b30, #ff006e); }
            .hash-text { font-family: 'Consolas', 'Courier New', monospace; font-size: 0.85em; word-break: break-all; }
            @media (max-width: 600px) {
                .container { padding: 10px; }
                .hero h1 { font-size: 1.8em; }
                .stats-grid { grid-template-columns: repeat(2, 1fr); }
            }
            """;
    }
}
