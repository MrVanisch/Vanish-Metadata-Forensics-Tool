package com.vanish.forensics;

import com.vanish.forensics.analyzer.BatchAnalyzer;
import com.vanish.forensics.analyzer.FileForensicAnalyzer;
import com.vanish.forensics.analyzer.SensitiveDataDetector;
import com.vanish.forensics.core.ExtractorFactory;
import com.vanish.forensics.core.MetadataExtractor;
import com.vanish.forensics.model.FileMetadata;
import com.vanish.forensics.report.ConsoleReportGenerator;
import com.vanish.forensics.report.HtmlReportGenerator;
import com.vanish.forensics.report.JsonReportGenerator;
import com.vanish.forensics.ui.ConsoleUI;

import java.io.File;
import java.util.List;

/**
 * Main entry point for the Vanish Metadata Forensics Tool.
 *
 * Usage:
 *   java -jar vanish.jar                          # Interactive mode
 *   java -jar vanish.jar --file photo.jpg          # Analyze single file
 *   java -jar vanish.jar --dir ./photos            # Batch analyze directory
 *   java -jar vanish.jar --file photo.jpg --json   # Analyze + export JSON
 *   java -jar vanish.jar --file photo.jpg --html   # Analyze + export HTML
 *   java -jar vanish.jar --clean photo.jpg         # Clean metadata
 *   java -jar vanish.jar --help                    # Show help
 */
public class VanishApp {

    private static final String VERSION = "1.0.0";
    private static final String REPORT_DIR = "./vanish_reports";

    public static void main(String[] args) {
        // Enable ANSI colors on Windows
        enableAnsiSupport();

        if (args.length == 0) {
            // Interactive mode
            ConsoleUI ui = new ConsoleUI();
            ui.start();
            return;
        }

        // CLI mode — parse arguments
        try {
            handleCLI(args);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Handles command-line arguments for non-interactive mode.
     */
    private static void handleCLI(String[] args) throws Exception {
        String filePath = null;
        String dirPath = null;
        boolean exportJson = false;
        boolean exportHtml = false;
        boolean cleanMode = false;
        boolean editMode = false;

        // Parse arguments
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--help":
                case "-h":
                    printHelp();
                    return;
                case "--version":
                case "-v":
                    System.out.println("Vanish Metadata Forensics Tool v" + VERSION);
                    return;
                case "--file":
                case "-f":
                    if (i + 1 < args.length) filePath = args[++i];
                    break;
                case "--dir":
                case "-d":
                    if (i + 1 < args.length) dirPath = args[++i];
                    break;
                case "--json":
                    exportJson = true;
                    break;
                case "--html":
                    exportHtml = true;
                    break;
                case "--clean":
                    cleanMode = true;
                    if (i + 1 < args.length) filePath = args[++i];
                    break;
                case "--edit":
                case "-e":
                    editMode = true;
                    if (i + 1 < args.length) filePath = args[++i];
                    break;
                default:
                    // Treat unknown args as file paths if no flag is set
                    if (filePath == null && !args[i].startsWith("-")) {
                        filePath = args[i];
                    }
            }
        }

        // Execute based on parsed arguments
        if (cleanMode && filePath != null) {
            cleanFile(filePath);
        } else if (editMode && filePath != null) {
            startEditingMode(filePath);
        } else if (filePath != null) {
            analyzeSingleFile(filePath, exportJson, exportHtml);
        } else if (dirPath != null) {
            analyzeDirectory(dirPath, exportJson, exportHtml);
        } else {
            System.out.println("Error: No file or directory specified.");
            System.out.println("Use --help for usage information.");
        }
    }

    /**
     * Analyzes a single file in CLI mode.
     */
    private static void analyzeSingleFile(String path, boolean json, boolean html) throws Exception {
        File file = new File(path);
        if (!file.exists()) {
            throw new Exception("File not found: " + path);
        }

        ExtractorFactory factory = new ExtractorFactory();
        MetadataExtractor extractor = factory.getExtractor(file);
        FileMetadata metadata = extractor.extract(file);

        // Sensitive data detection
        SensitiveDataDetector detector = new SensitiveDataDetector();
        detector.analyze(metadata);

        // Forensic analysis (hashes, entropy, risk score)
        FileForensicAnalyzer forensicAnalyzer = new FileForensicAnalyzer();
        forensicAnalyzer.analyze(file, metadata);

        // Always show console report
        new ConsoleReportGenerator().generate(metadata);

        // Export if requested
        if (json) {
            new JsonReportGenerator(REPORT_DIR).generate(metadata);
        }
        if (html) {
            new HtmlReportGenerator(REPORT_DIR).generate(metadata);
        }
    }

    /**
     * Analyzes all files in a directory in CLI mode.
     */
    private static void analyzeDirectory(String path, boolean json, boolean html) throws Exception {
        File dir = new File(path);
        if (!dir.isDirectory()) {
            throw new Exception("Not a directory: " + path);
        }

        BatchAnalyzer batchAnalyzer = new BatchAnalyzer();

        // Set up simple progress listener
        batchAnalyzer.setProgressListener(new BatchAnalyzer.ProgressListener() {
            @Override
            public void onProgress(int current, int total, String currentFile) {
                System.out.printf("\r  Analyzing %d/%d: %s", current, total, currentFile);
            }

            @Override
            public void onFileComplete(FileMetadata result) {}

            @Override
            public void onError(String filePath, String error) {
                System.err.println("\n  Error: " + filePath + " — " + error);
            }
        });

        List<FileMetadata> results = batchAnalyzer.analyzeDirectory(dir);
        System.out.println();

        BatchAnalyzer.BatchStatistics stats = batchAnalyzer.getStatistics(results);
        new ConsoleReportGenerator().generateBatch(results, stats);

        if (json) {
            new JsonReportGenerator(REPORT_DIR).generateBatch(results, stats);
        }
        if (html) {
            new HtmlReportGenerator(REPORT_DIR).generateBatch(results, stats);
        }
    }

    /**
     * Starts the interactive editor for a specific file in CLI mode.
     */
    private static void startEditingMode(String path) {
        ConsoleUI ui = new ConsoleUI();
        ui.directEditInit(path);
    }
    
    /**
     * Cleans metadata from a file in CLI mode.
     */
    private static void cleanFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            System.err.println("File not found: " + path);
            return;
        }

        var cleaner = new com.vanish.forensics.cleaner.MetadataCleaner();
        var result = cleaner.cleanFile(file, true);

        if (result.isSuccess()) {
            System.out.println("✅ Metadata cleaned: " + result.getCleanedFile());
            System.out.println("📦 Backup: " + result.getBackupFile());
        } else {
            System.err.println("❌ Error: " + result.getErrorMessage());
        }
    }

    /**
     * Prints help information.
     */
    private static void printHelp() {
        System.out.println();
        System.out.println("  VANISH — Metadata Forensics Tool v" + VERSION);
        System.out.println("  ════════════════════════════════════════");
        System.out.println();
        System.out.println("  USAGE:");
        System.out.println("    java -jar vanish.jar                          Interactive mode");
        System.out.println("    java -jar vanish.jar --file <path>            Analyze single file");
        System.out.println("    java -jar vanish.jar --dir <path>             Batch analyze directory");
        System.out.println("    java -jar vanish.jar --clean <path>           Clean metadata from file");
        System.out.println();
        System.out.println("  OPTIONS:");
        System.out.println("    -f, --file <path>    Path to file to analyze");
        System.out.println("    -d, --dir <path>     Path to directory for batch analysis");
        System.out.println("    --json               Export report as JSON");
        System.out.println("    --html               Export report as HTML");
        System.out.println("    --clean <path>       Clean metadata from file (creates backup)");
        System.out.println("    -e, --edit <path>    Edit metadata of a file (interactive)");
        System.out.println("    -v, --version        Show version");
        System.out.println("    -h, --help           Show this help");
        System.out.println();
        System.out.println("  EXAMPLES:");
        System.out.println("    java -jar vanish.jar --file photo.jpg");
        System.out.println("    java -jar vanish.jar --file document.pdf --json --html");
        System.out.println("    java -jar vanish.jar --dir ./photos --html");
        System.out.println("    java -jar vanish.jar --clean secret_photo.jpg");
        System.out.println("    java -jar vanish.jar --edit photo.jpg");
        System.out.println();
    }

    /**
     * Enables ANSI escape code support on Windows terminals.
     */
    private static void enableAnsiSupport() {
        // Windows 10+ supports ANSI codes natively in newer terminals
        // This is a best-effort workaround for older versions
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("windows")) {
            try {
                // Attempt to enable virtual terminal processing
                new ProcessBuilder("cmd", "/c", "chcp 65001 > nul").inheritIO().start().waitFor();
            } catch (Exception ignored) {
                // Silently fail — ANSI may still work in modern terminals
            }
        }
    }
}
