package com.vanish.forensics.ui;

/**
 * ANSI color codes and text formatting utilities for console output.
 * Provides methods for colorizing text in terminal applications.
 */
public class ConsoleColors {

    // Reset
    public static final String RESET = "\u001B[0m";

    // Styles
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";
    public static final String ITALIC = "\u001B[3m";
    public static final String UNDERLINE = "\u001B[4m";

    // Regular Colors
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    // Bright Colors
    public static final String BRIGHT_RED = "\u001B[91m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_BLUE = "\u001B[94m";
    public static final String BRIGHT_PURPLE = "\u001B[95m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_WHITE = "\u001B[97m";

    // Background Colors
    public static final String BG_RED = "\u001B[41m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String BG_YELLOW = "\u001B[43m";
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_CYAN = "\u001B[46m";

    // --- Utility methods ---

    public static String red(String text) { return RED + text + RESET; }
    public static String green(String text) { return GREEN + text + RESET; }
    public static String yellow(String text) { return YELLOW + text + RESET; }
    public static String blue(String text) { return BLUE + text + RESET; }
    public static String cyan(String text) { return CYAN + text + RESET; }
    public static String purple(String text) { return PURPLE + text + RESET; }
    public static String bold(String text) { return BOLD + text + RESET; }
    public static String dim(String text) { return DIM + text + RESET; }

    public static String colorBold(String color, String text) {
        return color + BOLD + text + RESET;
    }
}
