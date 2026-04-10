package com.vanish.forensics.core;

import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Metadata editor for PNG files using standard Java ImageIO.
 * Maps common metadata fields to PNG tEXt chunks.
 */
public class PngMetadataEditor implements MetadataEditor {

    @Override
    public void updateMetadata(File source, File destination, Map<String, String> changes) throws Exception {
        BufferedImage image = ImageIO.read(source);
        if (image == null) {
            throw new IOException("Could not read PNG image: " + source.getName());
        }

        // Get a PNG writer
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
        if (!writers.hasNext()) {
            throw new IOException("No PNG writer found");
        }
        ImageWriter writer = writers.next();

        // Prepare metadata
        ImageWriteParam writeParam = writer.getDefaultWriteParam();
        ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromRenderedImage(image);
        IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);

        // Map Vanish keys to PNG standard keywords
        // Reference: https://www.w3.org/TR/PNG/#11textinfo
        for (Map.Entry<String, String> entry : changes.entrySet()) {
            String key = entry.getKey().toLowerCase();
            String value = entry.getValue();
            String pngKey = null;

            switch (key) {
                case "artist": pngKey = "Author"; break;
                case "title": pngKey = "Title"; break;
                case "description": pngKey = "Description"; break;
                case "software": pngKey = "Software"; break;
                case "copyright": pngKey = "Copyright"; break;
                case "date": pngKey = "Creation Time"; break;
                default: 
                    // Use the key as-is for custom fields
                    pngKey = entry.getKey();
            }

            if (pngKey != null) {
                addTextChunk(metadata, pngKey, value);
            }
        }

        // Write image with metadata
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(destination)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, metadata), writeParam);
        } finally {
            writer.dispose();
        }
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
