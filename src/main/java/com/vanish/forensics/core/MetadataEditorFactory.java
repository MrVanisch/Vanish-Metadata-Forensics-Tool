package com.vanish.forensics.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory for selecting the appropriate MetadataEditor based on file type.
 */
public class MetadataEditorFactory {

    private static final Map<String, MetadataEditor> editors = new HashMap<>();

    static {
        // Register supported editors
        registerEditor(new JpegMetadataEditor());
        registerEditor(new PngMetadataEditor());
    }

    public static void registerEditor(MetadataEditor editor) {
        // Temporary hack to get extensions from the editor's supports() method
        // In a real factory we might want a better registration system
        if (editor.supports("jpg")) editors.put("jpg", editor);
        if (editor.supports("jpeg")) editors.put("jpeg", editor);
        if (editor.supports("png")) editors.put("png", editor);
    }

    /**
     * Returns an editor for the given file, or null if not supported.
     */
    public static MetadataEditor getEditor(File file) {
        String name = file.getName().toLowerCase();
        int dot = name.lastIndexOf('.');
        if (dot == -1) return null;
        
        String ext = name.substring(dot + 1);
        return editors.get(ext);
    }

    /**
     * Returns a comma-separated string of supported extensions.
     */
    public static String getSupportedExtensions() {
        return String.join(", ", editors.keySet()).toUpperCase();
    }
}
