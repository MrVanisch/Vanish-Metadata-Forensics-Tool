package com.vanish.forensics.ui;

import com.vanish.forensics.analyzer.BatchAnalyzer;
import com.vanish.forensics.analyzer.FileForensicAnalyzer;
import com.vanish.forensics.analyzer.SensitiveDataDetector;
import com.vanish.forensics.cleaner.MetadataCleaner;
import com.vanish.forensics.core.ExtractorFactory;
import com.vanish.forensics.core.MetadataExtractor;
import com.vanish.forensics.core.UniversalMetadataEditor;
import com.vanish.forensics.model.FileMetadata;
import com.vanish.forensics.model.GpsCoordinates;
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
                    editMetadata();
                    break;
                case "5":
                    showRawMetadata();
                    break;
                case "6":
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
        System.out.println(ConsoleColors.CYAN + "  │  " + ConsoleColors.GREEN + "[4]" + ConsoleColors.WHITE + " ✍️  Edit metadata (PRO)               " + ConsoleColors.CYAN + "│" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN + "  │  " + ConsoleColors.GREEN + "[5]" + ConsoleColors.WHITE + " 🗂️  View raw metadata                " + ConsoleColors.CYAN + "│" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN + "  │  " + ConsoleColors.GREEN + "[6]" + ConsoleColors.WHITE + " ⚙️  Settings                          " + ConsoleColors.CYAN + "│" + ConsoleColors.RESET);
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

    /**
     * Entry point for direct editing mode from CLI.
     */
    public void directEditInit(String path) {
        performEdit(path);
    }

    /**
     * Interactive metadata editor flow.
     */
    private void editMetadata() {
        System.out.println();
        System.out.println(ConsoleColors.BOLD + "  ✍️ EDIT METADATA (PRO)" + ConsoleColors.RESET);
        String path = readInput("Enter file path");
        performEdit(path);
    }

    private void performEdit(String path) {
        File file = new File(path.trim().replace("\"", ""));
        if (!file.exists() || !file.isFile()) {
            System.out.println(ConsoleColors.RED + "  Error: File not found: " + path + ConsoleColors.RESET);
            return;
        }

        UniversalMetadataEditor universalEditor = new UniversalMetadataEditor();

        // Step 1: Read ALL metadata from the file
        Map<String, String> existingMetadata;
        try {
            System.out.println(ConsoleColors.DIM + "  Reading metadata from: " + file.getName() + "..." + ConsoleColors.RESET);
            existingMetadata = universalEditor.readAllMetadata(file);
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "  Error reading metadata: " + e.getMessage() + ConsoleColors.RESET);
            return;
        }

        if (existingMetadata.isEmpty()) {
            System.out.println(ConsoleColors.YELLOW + "  No metadata found in this file." + ConsoleColors.RESET);
            System.out.println(ConsoleColors.DIM + "  You can still add new metadata fields." + ConsoleColors.RESET);
        }

        // Step 2: Display all metadata as a numbered list
        List<String> keys = new ArrayList<>(existingMetadata.keySet());
        Set<String> readOnlyKeys = getReadOnlyKeys(keys);
        displayMetadataTable(keys, existingMetadata, readOnlyKeys);

        // Step 3: Interactive editing loop
        Map<String, String> changes = new HashMap<>();
        boolean editing = true;
        boolean forceMode = false;

        while (editing) {
            System.out.println();
            System.out.println(ConsoleColors.CYAN + "  Commands:" + ConsoleColors.RESET);
            System.out.println("    " + ConsoleColors.GREEN + "[number]" + ConsoleColors.RESET + "  Edit field by number from the list above");
            System.out.println("    " + ConsoleColors.GREEN + "[A]" + ConsoleColors.RESET + "       Add a new custom metadata field");
            System.out.println("    " + ConsoleColors.GREEN + "[L]" + ConsoleColors.RESET + "       Show metadata list again");
            System.out.println("    " + ConsoleColors.YELLOW + "[F]" + ConsoleColors.RESET + "       Toggle Force Mode " + (forceMode ? "(ON)" : "(OFF)"));
            System.out.println("    " + ConsoleColors.GREEN + "[S]" + ConsoleColors.RESET + "       Save all changes & create edited file");
            System.out.println("    " + ConsoleColors.RED + "[C]" + ConsoleColors.RESET + "       Cancel");

            String choice = readInput("Action").trim().toUpperCase();

            switch (choice) {
                case "S":
                    editing = false;
                    break;
                case "C":
                    System.out.println(ConsoleColors.YELLOW + "  Cancelled. No changes saved." + ConsoleColors.RESET);
                    return;
                case "F":
                    forceMode = !forceMode;
                    System.out.println(ConsoleColors.YELLOW + "  Force Mode is now " + (forceMode ? "ON. You can edit structural data (WARNING: may corrupt file or be appended as a fake tag)." : "OFF.") + ConsoleColors.RESET);
                    break;
                case "A":
                    String newKey = readInput("Field name (e.g. Author, Title, Comment, Copyright)");
                    if (!newKey.isEmpty()) {
                        String newVal = readInput("Value for '" + newKey + "'");
                        changes.put(newKey, newVal);
                        System.out.println(ConsoleColors.GREEN + "  + Added: " + newKey + " = " + newVal + ConsoleColors.RESET);
                    }
                    break;
                case "L":
                    displayMetadataTable(keys, existingMetadata, readOnlyKeys);
                    if (!changes.isEmpty()) {
                        System.out.println();
                        System.out.println(ConsoleColors.YELLOW + "  Pending changes (" + changes.size() + "):" + ConsoleColors.RESET);
                        for (Map.Entry<String, String> c : changes.entrySet()) {
                            System.out.println("    " + ConsoleColors.GREEN + c.getKey() + ConsoleColors.RESET + " → " + c.getValue());
                        }
                    }
                    break;
                default:
                    // Try to parse as a number
                    try {
                        int idx = Integer.parseInt(choice);
                        if (idx >= 1 && idx <= keys.size()) {
                            String selectedKey = keys.get(idx - 1);
                            String oldValue = existingMetadata.get(selectedKey);

                            // Check if read-only
                            if (readOnlyKeys.contains(selectedKey) && !forceMode) {
                                System.out.println(ConsoleColors.RED + "  ⚠ Field '" + selectedKey + "' is READ-ONLY (structural data)." + ConsoleColors.RESET);
                                System.out.println(ConsoleColors.DIM + "  This value is encoded in the file's binary structure and cannot be overwritten." + ConsoleColors.RESET);
                                System.out.println(ConsoleColors.DIM + "  Use [F] to enable Force Mode if you really want to try bypassing this." + ConsoleColors.RESET);
                                break;
                            } else if (readOnlyKeys.contains(selectedKey) && forceMode) {
                                System.out.println(ConsoleColors.YELLOW + "  ⚠ Forcing edit on structural field '" + selectedKey + "'." + ConsoleColors.RESET);
                            }

                            System.out.println();
                            System.out.println("  Field:    " + ConsoleColors.CYAN + selectedKey + ConsoleColors.RESET);
                            System.out.println("  Current:  " + ConsoleColors.DIM + oldValue + ConsoleColors.RESET);
                            String newValue = readInput("New value (or Enter to skip)");
                            if (!newValue.isEmpty()) {
                                changes.put(selectedKey, newValue);
                                System.out.println(ConsoleColors.GREEN + "  ✓ Queued: " + selectedKey + " → " + newValue + ConsoleColors.RESET);
                            }
                        } else {
                            System.out.println(ConsoleColors.RED + "  Invalid number. Use 1-" + keys.size() + ConsoleColors.RESET);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(ConsoleColors.RED + "  Unknown command. Use a number, A, L, F, S, or C." + ConsoleColors.RESET);
                    }
            }
        }

        if (changes.isEmpty()) {
            System.out.println("  No changes to save.");
            return;
        }

        // Step 4: Apply changes
        try {
            String ext = "";
            int dotIdx = path.lastIndexOf(".");
            if (dotIdx > 0) ext = path.substring(dotIdx);
            String outPath = path.substring(0, dotIdx > 0 ? dotIdx : path.length()) + "_edited" + ext;
            File outFile = new File(outPath);

            System.out.println();
            System.out.println(ConsoleColors.DIM + "  Applying " + changes.size() + " change(s)..." + ConsoleColors.RESET);
            universalEditor.applyChanges(file, outFile, changes);

            System.out.println(ConsoleColors.GREEN + "  ✅ Metadata updated successfully!" + ConsoleColors.RESET);
            System.out.println("  Output file: " + ConsoleColors.CYAN + outPath + ConsoleColors.RESET);

            // Step 5: Verification — re-read edited file and confirm changes
            File sidecar = new File(outPath + ".metadata.json");
            if (sidecar.exists()) {
                System.out.println("  Sidecar:   " + ConsoleColors.CYAN + sidecar.getName() + ConsoleColors.RESET);
                System.out.println(ConsoleColors.DIM + "  (For this format, changes are stored in a sidecar JSON file)" + ConsoleColors.RESET);
            } else {
                System.out.println();
                System.out.println(ConsoleColors.DIM + "  Verifying changes in output file..." + ConsoleColors.RESET);
                try {
                    Map<String, String> newMetadata = universalEditor.readAllMetadata(outFile);
                    int verified = 0;
                    int notFound = 0;
                    for (Map.Entry<String, String> change : changes.entrySet()) {
                        // Search for the value in the new metadata
                        boolean found = false;
                        for (String val : newMetadata.values()) {
                            if (val.contains(change.getValue())) {
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            verified++;
                        } else {
                            notFound++;
                        }
                    }
                    if (verified > 0) {
                        System.out.println(ConsoleColors.GREEN + "  ✓ Verified: " + verified + " change(s) confirmed in the output file." + ConsoleColors.RESET);
                    }
                    if (notFound > 0) {
                        System.out.println(ConsoleColors.YELLOW + "  ⚠ " + notFound + " change(s) could not be verified (structural fields are read-only)." + ConsoleColors.RESET);
                    }
                } catch (Exception ve) {
                    System.out.println(ConsoleColors.DIM + "  (Verification skipped)" + ConsoleColors.RESET);
                }
            }
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "  Error during editing: " + e.getMessage() + ConsoleColors.RESET);
            e.printStackTrace();
        }
    }

    /**
     * Identifies metadata keys that are structural/read-only and cannot be edited.
     * These are properties encoded in binary chunks (dimensions, compression, color space, etc.)
     */
    private Set<String> getReadOnlyKeys(List<String> keys) {
        Set<String> readOnly = new HashSet<>();
        for (String key : keys) {
            String lower = key.toLowerCase();
            if (lower.startsWith("chroma ") || lower.startsWith("compression ") ||
                lower.startsWith("data ") || lower.startsWith("dimension ") ||
                lower.startsWith("transparency ") || lower.startsWith("x-tika:") ||
                lower.equals("content-type") || lower.equals("width") || lower.equals("height") ||
                lower.startsWith("tiff:") || lower.startsWith("ihdr") ||
                lower.startsWith("gama") || lower.startsWith("phys") || lower.startsWith("srgb") ||
                lower.startsWith("imagereader:") ||
                lower.startsWith("pdf:") || lower.contains("numimages") ||
                lower.equals("content-length")) {
                readOnly.add(key);
            }
        }
        return readOnly;
    }

    /**
     * Displays all metadata fields as a formatted, numbered table.
     * Read-only fields are marked with a lock icon.
     */
    private void displayMetadataTable(List<String> keys, Map<String, String> metadata, Set<String> readOnlyKeys) {
        System.out.println();
        System.out.println(ConsoleColors.BOLD + "  ┌─────┬──────────────────────────────────────┬──────────────────────────────────────┐" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.BOLD + "  │  #  │ Field                                │ Value                                │" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.BOLD + "  ├─────┼──────────────────────────────────────┼──────────────────────────────────────┤" + ConsoleColors.RESET);
        
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = metadata.get(key);
            boolean isReadOnly = readOnlyKeys.contains(key);
            
            // Truncate for display
            String displayKey = key.length() > 34 ? key.substring(0, 31) + "..." : key;
            String displayVal = value.length() > 36 ? value.substring(0, 33) + "..." : value;
            
            // Add lock icon for read-only fields
            String prefix = isReadOnly ? "🔒" : "  ";
            String keyColor = isReadOnly ? ConsoleColors.DIM : "";
            String keyReset = isReadOnly ? ConsoleColors.RESET : "";
            
            System.out.printf("  │ %s%-3d%s │ %s%s%-34s%s │ %-36s │%n",
                ConsoleColors.GREEN, (i + 1), ConsoleColors.RESET,
                prefix, keyColor, displayKey, keyReset,
                displayVal);
        }
        
        System.out.println(ConsoleColors.BOLD + "  └─────┴──────────────────────────────────────┴──────────────────────────────────────┘" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.DIM + "  Total: " + keys.size() + " fields (🔒 = read-only structural data, cannot be edited)" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.DIM + "  Tip: Use [A] to add new writable fields like Author, Title, Copyright, Comment" + ConsoleColors.RESET);
    }

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
