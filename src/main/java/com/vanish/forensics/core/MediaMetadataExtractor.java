package com.vanish.forensics.core;

import com.vanish.forensics.model.FileMetadata;
import com.vanish.forensics.model.GpsCoordinates;
import com.vanish.forensics.model.MediaData;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;

/**
 * Extractor for Audio and Video metadata using Apache Tika.
 * Supported formats: MP4, MOV, AVI, MP3, WAV, FLAC, etc.
 */
public class MediaMetadataExtractor implements MetadataExtractor {

    @Override
    public FileMetadata extract(File file) throws IOException {
        FileMetadata fileMetadata = new FileMetadata();
        
        // Basic file info
        fileMetadata.setFileName(file.getName());
        fileMetadata.setFilePath(file.getAbsolutePath());
        fileMetadata.setFileSize(file.length());
        fileMetadata.setFileExtension(getExtension(file));

        try {
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            fileMetadata.setLastModified(sdf.format(attrs.lastModifiedTime().toMillis()));
        } catch (IOException ignored) {}

        MediaData mediaData = new MediaData();
        Metadata metadata = new Metadata();
        AutoDetectParser parser = new AutoDetectParser();

        try (InputStream stream = new FileInputStream(file)) {
            parser.parse(stream, new DefaultHandler(), metadata, new ParseContext());
        } catch (Exception e) {
            fileMetadata.addRawMetadata("Error", "Tika parsing failed: " + e.getMessage());
        }

        fileMetadata.setMimeType(metadata.get(Metadata.CONTENT_TYPE));

        // Map general metadata
        mediaData.setTitle(metadata.get(TikaCoreProperties.TITLE));
        mediaData.setArtist(getAny(metadata, "xmpDM:artist", "Author", "meta:author", "creator"));
        mediaData.setAlbum(metadata.get("xmpDM:album"));
        mediaData.setGenre(metadata.get("xmpDM:genre"));
        mediaData.setSoftware(getAny(metadata, "xmpDM:audioCompressor", "Software", "xmp:CreatorTool", "generator"));
        mediaData.setDate(getAny(metadata, "xmpDM:releaseDate", "dcterms:created", "Creation-Date", "meta:creation-date"));

        // Duration handling
        String durationStr = metadata.get("xmpDM:duration");
        if (durationStr != null) {
            try {
                double seconds = Double.parseDouble(durationStr);
                mediaData.setDuration(formatDuration(seconds));
            } catch (NumberFormatException e) {
                mediaData.setDuration(durationStr);
            }
        }

        // Video specific
        String width = getAny(metadata, "tiff:ImageWidth", "width");
        String height = getAny(metadata, "tiff:ImageLength", "height");
        if (width != null && height != null) {
            try {
                mediaData.setWidth(Integer.parseInt(width.split("\\.")[0]));
                mediaData.setHeight(Integer.parseInt(height.split("\\.")[0]));
            } catch (Exception ignored) {}
        }

        String frameRate = metadata.get("xmpDM:videoFrameRate");
        if (frameRate != null) {
            try { mediaData.setFrameRate(Double.parseDouble(frameRate)); } catch (Exception ignored) {}
        }
        mediaData.setVideoCodec(getAny(metadata, "xmpDM:videoCompressor", "xmpDM:videoCodec"));

        // Audio specific
        mediaData.setAudioCodec(getAny(metadata, "xmpDM:audioCompressor", "xmpDM:audioCodec"));
        mediaData.setSampleRate(metadata.get("xmpDM:audioSampleRate"));
        mediaData.setChannels(metadata.get("xmpDM:audioChannelType"));
        mediaData.setBitrate(getAny(metadata, "xmpDM:audioSampleRate", "xmpDM:fileDataRate"));

        // GPS
        String lat = getAny(metadata, "geo:lat", "latitude", "Geolocation:Latitude");
        String lon = getAny(metadata, "geo:long", "longitude", "Geolocation:Longitude");
        if (lat != null && lon != null) {
            try {
                mediaData.setGpsCoordinates(new GpsCoordinates(
                        Double.parseDouble(lat),
                        Double.parseDouble(lon)
                ));
            } catch (Exception ignored) {}
        }

        // Store all raw metadata
        for (String name : metadata.names()) {
            fileMetadata.addRawMetadata(name, metadata.get(name));
        }

        fileMetadata.setMediaData(mediaData);
        return fileMetadata;
    }

    @Override
    public boolean supports(String mimeType) {
        if (mimeType == null) return false;
        return mimeType.startsWith("video/") || mimeType.startsWith("audio/");
    }

    @Override
    public String getName() {
        return "Media Metadata Extractor (Tika Media Engine)";
    }

    private String getAny(Metadata metadata, String... keys) {
        for (String key : keys) {
            String val = metadata.get(key);
            if (val != null && !val.isEmpty()) return val;
        }
        return null;
    }

    private String getExtension(File file) {
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(dot + 1).toLowerCase() : "";
    }

    private String formatDuration(double seconds) {
        int h = (int) (seconds / 3600);
        int m = (int) ((seconds % 3600) / 60);
        int s = (int) (seconds % 60);
        if (h > 0) return String.format("%02d:%02d:%02d", h, m, s);
        return String.format("%02d:%02d", m, s);
    }
}
