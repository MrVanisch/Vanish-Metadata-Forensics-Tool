package com.vanish.forensics.core;

import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Metadata editor for PNG files using standard Java ImageIO.
 * Reads the original image WITH its metadata, merges new tEXt chunks,
 * and writes everything back preserving existing data.
 */
public class PngMetadataEditor implements MetadataEditor {

    @Override
    public void updateMetadata(File source, File destination, Map<String, String> changes) throws Exception {
        // Step 1: Read original image with its metadata using ImageReader
        ImageReader reader = null;
        BufferedImage image;
        IIOMetadata originalMetadata;

        try (ImageInputStream iis = ImageIO.createImageInputStream(source)) {
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("png");
            if (!readers.hasNext()) {
                throw new IOException("No PNG reader found");
            }
            reader = readers.next();
            reader.setInput(iis);
            image = reader.read(0);
            originalMetadata = reader.getImageMetadata(0);
        } finally {
            if (reader != null) reader.dispose();
        }

        if (image == null) {
            throw new IOException("Could not read PNG image: " + source.getName());
        }

        // Step 2: Get a PNG writer and prepare metadata
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
        if (!writers.hasNext()) {
            throw new IOException("No PNG writer found");
        }
        ImageWriter writer = writers.next();
        ImageWriteParam writeParam = writer.getDefaultWriteParam();

        // Use original metadata as base (preserves existing chunks)
        IIOMetadata metadata = originalMetadata;

        // Step 3: Add/modify tEXt chunks for each change
        for (Map.Entry<String, String> entry : changes.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // Use clean display names for tEXt chunk keywords
            String pngKey = mapToPngKeyword(key);
            addTextChunk(metadata, pngKey, value);
        }

        // Step 4: Write image with merged metadata
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(destination)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, metadata), writeParam);
        } finally {
            writer.dispose();
        }
    }

    /**
     * Maps Tika internal key names to clean PNG tEXt keywords.
     * PNG spec defines standard keywords: Title, Author, Description, Copyright,
     * Creation Time, Software, Disclaimer, Warning, Source, Comment.
     */
    private String mapToPngKeyword(String tikaKey) {
        String lower = tikaKey.toLowerCase();

        // Standard PNG keywords
        if (lower.contains("artist") || lower.contains("author") || lower.contains("creator")) return "Author";
        if (lower.contains("title")) return "Title";
        if (lower.contains("description") || lower.contains("subject")) return "Description";
        if (lower.contains("copyright")) return "Copyright";
        if (lower.contains("software") || lower.contains("tool")) return "Software";
        if (lower.contains("date") || lower.contains("creation time")) return "Creation Time";
        if (lower.contains("comment")) return "Comment";
        if (lower.contains("source")) return "Source";
        if (lower.contains("disclaimer")) return "Disclaimer";
        if (lower.contains("warning")) return "Warning";

        // For non-standard keys, use the original name as-is
        // This preserves custom fields like "GPS Latitude" etc.
        return tikaKey;
    }

    private void addTextChunk(IIOMetadata metadata, String key, String value) throws IIOInvalidTreeException {
        IIOMetadataNode textEntry = new IIOMetadataNode("tEXtEntry");
        textEntry.setAttribute("keyword", key);
        textEntry.setAttribute("value", value);

        IIOMetadataNode text = new IIOMetadataNode("tEXt");
        text.appendChild(textEntry);

        IIOMetadataNode root = new IIOMetadataNode("javax_imageio_png_1.0");
        root.appendChild(text);

        metadata.mergeTree("javax_imageio_png_1.0", root);
    }

    @Override
    public boolean supports(String extension) {
        return extension != null && extension.equalsIgnoreCase("png");
    }
}
