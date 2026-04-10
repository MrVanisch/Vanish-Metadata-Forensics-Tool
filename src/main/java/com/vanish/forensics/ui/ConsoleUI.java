package com.vanish.forensics.ui;

import com.vanish.forensics.analyzer.BatchAnalyzer;
import com.vanish.forensics.analyzer.FileForensicAnalyzer;
import com.vanish.forensics.analyzer.SensitiveDataDetector;
import com.vanish.forensics.cleaner.MetadataCleaner;
import com.vanish.forensics.core.ExtractorFactory;
import com.vanish.forensics.core.MetadataExtractor;
import com.vanish.forensics.model.FileMetadata;
import com.vanish.forensics.report.ConsoleReportGenerator;
import com.vanish.forensics.report.HtmlReportGenerator;
import com.vanish.forensics.report.JsonReportGenerator;

import java.io.File;
import java.util.*;

/**
 * Interactive console user interface for the Vanish Metadata Forensics Tool.
 * Provides a menu-driven interface for all tool features.
 */
public class ConsoleUI {

    private final Scanner scanner;
    private final ExtractorFactory extractorFactory;
    private final SensitiveDataDetector sensitiveDataDetector;
    private final FileForensicAnalyzer forensicAnalyzer;
    private final BatchAnalyzer batchAnalyzer;
    private final MetadataCleaner metadataCleaner;
    private final ConsoleReportGenerator consoleReport;

    private String reportOutputDir = "./vanish_reports";

    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
        this.extractorFactory = new ExtractorFactory();
        this.sensitiveDataDetector = new SensitiveDataDetector();
        this.forensicAnalyzer = new FileForensicAnalyzer();
        this.batchAnalyzer = new BatchAnalyzer();
        this.metadataCleaner = new MetadataCleaner();
        this.consoleReport = new ConsoleReportGenerator();
    }

    /**
     * Starts the interactive menu loop.
     */
    public void start() {
        printBanner();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = readInput("Select option");

            switch (choice) {
                case "1":
                    analyzeSingleFile();
                    break;
                case "2":
                    batchAnalyzeDirectory();
                    break;
                case "3":
                    cleanMetadata();
                    break;
                case "4":
                    showRawMetadata();
                    break;
                case "5":
                    settings();
                    break;
                case "0":
                    running = false;
                    printExit();
                    break;
                default:
                    System.out.println(ConsoleColors.RED + "  Invalid option. Please try again." + ConsoleColors.RESET);
            }
        }
    }

    /**
     * Prints the application banner/logo.
     */
    private void printBanner() {
        System.out.println();
        System.out.println(ConsoleColors.CYAN + ConsoleColors.BOLD);
        System.out.println("  ╔═══════════════════════════════════════════════════════╗");
        System.out.println("  ║                                                       ║");
        System.out.println("  ║   ██╗   ██╗ █████╗ ███╗   ██╗██╗███████╗██╗  ██╗     ║");
        System.out.println("  ║   ██║   ██║██╔══██╗████╗  ██║██║██╔════╝██║  ██║     ║");
        System.out.println("  ║   ██║   ██║███████║██╔██╗ ██║██║███████╗███████║     ║");
        System.out.println("  ║   ╚██╗ ██╔╝██╔══██║██║╚██╗██║██║╚════██║██╔══██║     ║");
        System.out.println("  ║    ╚████╔╝ ██║  ██║██║ ╚████║██║███████║██║  ██║     ║");
        System.out.println("  ║     ╚═══╝  ╚═╝  ╚═╝╚═╝  ╚═══╝╚═╝╚══════╝╚═╝  ╚═╝     ║");
        System.out.println("  ║                                                       ║");
        System.out.println("  ║       " + ConsoleColors.WHITE + "Metadata Forensics Tool  v1.0.0" + ConsoleColors.CYAN + "              ║");
        System.out.println("  ║       " + ConsoleColors.DIM + "OSINT • Privacy • Security" + ConsoleColors.CYAN + ConsoleColors.BOLD + "                  ║");
        System.out.println("  ╚═══════════════════════════════════════════════════════╝" + ConsoleColors.RESET);
        System.out.println();
    }

    /**
     * Prints the main menu.
     */
    private void printMenu() {
        System.out.println();
        System.out.println(ConsoleColors.CYAN + "  ┌─────────────────────────────────────────┐" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN + "  │" + ConsoleColors.BOLD + "           MAIN MENU                      " + ConsoleColors.CYAN + "│" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN + "  ├─────────────────────────────────────────┤" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN + "  │  " + ConsoleColors.GREEN + "[1]" + ConsoleColors.WHITE + " 🔍 Analyze single file              " + ConsoleColors.CYAN + "│" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN + "  │  " + ConsoleColors.GREEN + "[2]" + ConsoleColors.WHITE + " 📂 Batch analyze directory           " + ConsoleColors.CYAN + "│" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN + "  │  " + ConsoleColors.GREEN + "[3]" + ConsoleColors.WHITE + " 🧹 Clean metadata from file          " + ConsoleColors.CYAN + "│" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN + "  │  " + ConsoleColors.GREEN + "[4]" + ConsoleColors.WHITE + " 🗂️  View raw metadata                " + ConsoleColors.CYAN + "│" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN + "  │  " + ConsoleColors.GREEN + "[5]" + ConsoleColors.WHITE + " ⚙️  Settings                          " + ConsoleColors.CYAN + "│" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN + "  │  " + ConsoleColors.RED + "[0]" + ConsoleColors.WHITE + " 🚪 Exit                              " + ConsoleColors.CYAN + "│" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN + "  └─────────────────────────────────────────┘" + ConsoleColors.RESET);
    }

    // ==================== OPTION 1: Analyze Single File ====================

    private void analyzeSingleFile() {
        System.out.println();
        System.out.println(ConsoleColors.BOLD + "  🔍 ANALYZE SINGLE FILE" + ConsoleColors.RESET);
        String path = readInput("Enter file path");

        File file = new File(path.trim().replace("\"", ""));
        if (!validateFile(file)) return;

        try {
            System.out.println(ConsoleColors.DIM + "  Detecting file type..." + ConsoleColors.RESET);
            String fileType = extractorFactory.getFileTypeDescription(file);
            System.out.println("  Type: " + ConsoleColors.CYAN + fileType + ConsoleColors.RESET);

            System.out.println(ConsoleColors.DIM + "  Extracting metadata..." + ConsoleColors.RESET);
            MetadataExtractor extractor = extractorFactory.getExtractor(file);
            FileMetadata metadata = extractor.extract(file);

            System.out.println(ConsoleColors.DIM + "  Analyzing for sensitive data..." + ConsoleColors.RESET);
            sensitiveDataDetector.analyze(metadata);

            System.out.println(ConsoleColors.DIM + "  Running forensic analysis..." + ConsoleColors.RESET);
            forensicAnalyzer.analyze(file, metadata);

            // Display console report
            consoleReport.generate(metadata);

            // Ask for additional report formats
            askForReportExport(metadata);

        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "  Error: " + e.getMessage() + ConsoleColors.RESET);
        }
    }

    // ==================== OPTION 2: Batch Analyze ====================

    private void batchAnalyzeDirectory() {
        System.out.println();
        System.out.println(ConsoleColors.BOLD + "  📂 BATCH ANALYZE DIRECTORY" + ConsoleColors.RESET);
        String path = readInput("Enter directory path");

        File dir = new File(path.trim().replace("\"", ""));
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println(ConsoleColors.RED + "  Error: Directory not found: " + dir.getAbsolutePath() + ConsoleColors.RESET);
            return;
        }

        // Ask for file type filter
        System.out.println();
        System.out.println("  Filter by extension? (e.g., 'jpg,png,pdf' or 'all' for all files)");
        String filterInput = readInput("Extensions");

        if (!filterInput.equalsIgnoreCase("all")) {
            Set<String> extensions = new HashSet<>();
            for (String ext : filterInput.split(",")) {
                extensions.add(ext.trim().toLowerCase());
            }
            batchAnalyzer.setAllowedExtensions(extensions);
        } else {
            batchAnalyzer.setAllowedExtensions(null);
        }

        // Set up progress listener
        batchAnalyzer.setProgressListener(new BatchAnalyzer.ProgressListener() {
            @Override
            public void onProgress(int current, int total, String currentFile) {
                int percent = (current * 100) / total;
                int bars = percent / 5;
                String progressBar = "█".repeat(bars) + "░".repeat(20 - bars);
                System.out.printf("\r  %s[%s]%s %d/%d (%d%%) %s",
                        ConsoleColors.CYAN, progressBar, ConsoleColors.RESET,
                        current, total, percent, currentFile);
            }

            @Override
            public void onFileComplete(FileMetadata result) {
                // Progress is updated in onProgress
            }

            @Override
            public void onError(String filePath, String error) {
                System.out.println();
                System.out.println(ConsoleColors.RED + "  ⚠ Error: " + filePath + " — " + error + ConsoleColors.RESET);
            }
        });

        try {
            System.out.println(ConsoleColors.DIM + "  Scanning directory..." + ConsoleColors.RESET);
            List<FileMetadata> results = batchAnalyzer.analyzeDirectory(dir);
            System.out.println(); // New line after progress bar

            if (results.isEmpty()) {
                System.out.println(ConsoleColors.YELLOW + "  No supported files found in directory." + ConsoleColors.RESET);
                return;
            }

            BatchAnalyzer.BatchStatistics stats = batchAnalyzer.getStatistics(results);
            consoleReport.generateBatch(results, stats);

            // Ask for export
            System.out.println("  Export reports?");
            System.out.println("    " + ConsoleColors.GREEN + "[1]" + ConsoleColors.RESET + " JSON report");
            System.out.println("    " + ConsoleColors.GREEN + "[2]" + ConsoleColors.RESET + " HTML report");
            System.out.println("    " + ConsoleColors.GREEN + "[3]" + ConsoleColors.RESET + " Both");
            System.out.println("    " + ConsoleColors.DIM + "[Enter]" + ConsoleColors.RESET + " Skip");
            String exportChoice = readInput("Export");

            if (exportChoice.contains("1") || exportChoice.equals("3")) {
                new JsonReportGenerator(reportOutputDir).generateBatch(results, stats);
            }
            if (exportChoice.contains("2") || exportChoice.equals("3")) {
                new HtmlReportGenerator(reportOutputDir).generateBatch(results, stats);
            }

        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "  Error: " + e.getMessage() + ConsoleColors.RESET);
        }
    }

    // ==================== OPTION 3: Clean Metadata ====================

    private void cleanMetadata() {
        System.out.println();
        System.out.println(ConsoleColors.BOLD + "  🧹 CLEAN METADATA" + ConsoleColors.RESET);
        String path = readInput("Enter file path to clean");

        File file = new File(path.trim().replace("\"", ""));
        if (!validateFile(file)) return;

        System.out.println();
        System.out.println(ConsoleColors.YELLOW + "  ⚠ WARNING: This will create a new copy without metadata." + ConsoleColors.RESET);
        System.out.println("  Create backup of original? " + ConsoleColors.GREEN + "[Y/n]" + ConsoleColors.RESET);
        String backupChoice = readInput("Backup");
        boolean createBackup = !backupChoice.equalsIgnoreCase("n");

        System.out.println(ConsoleColors.DIM + "  Cleaning metadata..." + ConsoleColors.RESET);
        MetadataCleaner.CleanResult result = metadataCleaner.cleanFile(file, createBackup);

        if (result.isSuccess()) {
            System.out.println();
            System.out.println(ConsoleColors.GREEN + "  ✅ Metadata cleaned successfully!" + ConsoleColors.RESET);
            System.out.println("  Cleaned file: " + ConsoleColors.CYAN + result.getCleanedFile() + ConsoleColors.RESET);
            if (result.getBackupFile() != null) {
                System.out.println("  Backup: " + ConsoleColors.DIM + result.getBackupFile() + ConsoleColors.RESET);
            }
            System.out.println();
            System.out.println("  Removed data:");
            for (String field : result.getRemovedFields()) {
                System.out.println("    " + ConsoleColors.RED + "✗" + ConsoleColors.RESET + " " + field);
            }
        } else {
            System.out.println(ConsoleColors.RED + "  ❌ Error: " + result.getErrorMessage() + ConsoleColors.RESET);
        }
    }

    // ==================== OPTION 4: Raw Metadata ====================

    private void showRawMetadata() {
        System.out.println();
        System.out.println(ConsoleColors.BOLD + "  🗂️ VIEW RAW METADATA" + ConsoleColors.RESET);
        String path = readInput("Enter file path");

        File file = new File(path.trim().replace("\"", ""));
        if (!validateFile(file)) return;

        try {
            MetadataExtractor extractor = extractorFactory.getExtractor(file);
            FileMetadata metadata = extractor.extract(file);

            System.out.println();
            System.out.println(ConsoleColors.BOLD + "  Raw metadata for: " + ConsoleColors.CYAN + file.getName() + ConsoleColors.RESET);
            System.out.println(ConsoleColors.CYAN + "  " + "─".repeat(56) + ConsoleColors.RESET);

            if (metadata.getRawMetadata().isEmpty()) {
                System.out.println(ConsoleColors.YELLOW + "  No metadata found in this file." + ConsoleColors.RESET);
            } else {
                // Sort keys for readability
                TreeMap<String, String> sorted = new TreeMap<>(metadata.getRawMetadata());
                for (Map.Entry<String, String> entry : sorted.entrySet()) {
                    System.out.printf("  %s%-40s%s %s%n",
                            ConsoleColors.DIM, entry.getKey(), ConsoleColors.RESET, entry.getValue());
                }
                System.out.println();
                System.out.println(ConsoleColors.DIM + "  Total: " + sorted.size() + " fields" + ConsoleColors.RESET);
            }

        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "  Error: " + e.getMessage() + ConsoleColors.RESET);
        }
    }

    // ==================== OPTION 5: Settings ====================

    private void settings() {
        System.out.println();
        System.out.println(ConsoleColors.BOLD + "  ⚙️ SETTINGS" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN + "  " + "─".repeat(40) + ConsoleColors.RESET);
        System.out.println("  Report output directory: " + ConsoleColors.CYAN + reportOutputDir + ConsoleColors.RESET);
        System.out.println();
        System.out.println("  " + ConsoleColors.GREEN + "[1]" + ConsoleColors.RESET + " Change report output directory");
        System.out.println("  " + ConsoleColors.DIM + "[Enter]" + ConsoleColors.RESET + " Back to main menu");

        String choice = readInput("Option");
        if (choice.equals("1")) {
            String newDir = readInput("New output directory");
            if (!newDir.isEmpty()) {
                reportOutputDir = newDir.trim();
                System.out.println(ConsoleColors.GREEN + "  ✅ Output directory updated to: " + reportOutputDir + ConsoleColors.RESET);
            }
        }
    }

    // ==================== Helper Methods ====================

    private void askForReportExport(FileMetadata metadata) {
        System.out.println("  Export to file?");
        System.out.println("    " + ConsoleColors.GREEN + "[1]" + ConsoleColors.RESET + " JSON report");
        System.out.println("    " + ConsoleColors.GREEN + "[2]" + ConsoleColors.RESET + " HTML report");
        System.out.println("    " + ConsoleColors.GREEN + "[3]" + ConsoleColors.RESET + " Both");
        System.out.println("    " + ConsoleColors.DIM + "[Enter]" + ConsoleColors.RESET + " Skip");
        String choice = readInput("Export");

        try {
            if (choice.contains("1") || choice.equals("3")) {
                new JsonReportGenerator(reportOutputDir).generate(metadata);
            }
            if (choice.contains("2") || choice.equals("3")) {
                new HtmlReportGenerator(reportOutputDir).generate(metadata);
            }
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "  Error exporting: " + e.getMessage() + ConsoleColors.RESET);
        }
    }

    private boolean validateFile(File file) {
        if (!file.exists()) {
            System.out.println(ConsoleColors.RED + "  Error: File not found: " + file.getAbsolutePath() + ConsoleColors.RESET);
            return false;
        }
        if (!file.isFile()) {
            System.out.println(ConsoleColors.RED + "  Error: Not a file: " + file.getAbsolutePath() + ConsoleColors.RESET);
            return false;
        }
        if (!file.canRead()) {
            System.out.println(ConsoleColors.RED + "  Error: Cannot read file: " + file.getAbsolutePath() + ConsoleColors.RESET);
            return false;
        }
        return true;
    }

    private String readInput(String prompt) {
        System.out.print(ConsoleColors.PURPLE + "  " + prompt + " > " + ConsoleColors.RESET);
        return scanner.nextLine().trim();
    }

    private void printExit() {
        System.out.println();
        System.out.println(ConsoleColors.CYAN + "  ┌─────────────────────────────────────────┐" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN + "  │  " + ConsoleColors.WHITE + "👋 Thank you for using VANISH!          " + ConsoleColors.CYAN + "│" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN + "  │  " + ConsoleColors.DIM + "   Stay safe. Protect your metadata.    " + ConsoleColors.CYAN + "│" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN + "  └─────────────────────────────────────────┘" + ConsoleColors.RESET);
        System.out.println();
    }
}
