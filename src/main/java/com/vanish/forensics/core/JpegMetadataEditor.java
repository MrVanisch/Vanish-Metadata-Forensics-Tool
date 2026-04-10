package com.vanish.forensics.core;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

import java.io.*;
import java.util.Map;

/**
 * Editor for JPEG EXIF metadata using Apache Commons Imaging.
 */
public class JpegMetadataEditor implements MetadataEditor {

    @Override
    public void updateMetadata(File source, File destination, Map<String, String> changes) throws Exception {
        final ImageMetadata metadata = Imaging.getMetadata(source);
        final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
        
        TiffOutputSet outputSet = null;
        if (null != jpegMetadata) {
            final TiffImageMetadata exif = jpegMetadata.getExif();
            if (null != exif) {
                outputSet = exif.getOutputSet();
            }
        }

        if (null == outputSet) {
            outputSet = new TiffOutputSet();
        }

        final TiffOutputDirectory rootDir = outputSet.getOrCreateRootDirectory();
        final TiffOutputDirectory exifDir = outputSet.getOrCreateExifDirectory();

        for (Map.Entry<String, String> entry : changes.entrySet()) {
            String tag = entry.getKey().toLowerCase();
            String value = entry.getValue();

            switch (tag) {
                case "artist":
                    updateField(rootDir, TiffTagConstants.TIFF_TAG_ARTIST, value);
                    break;
                case "title":
                case "description":
                    updateField(rootDir, TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION, value);
                    break;
                case "copyright":
                    updateField(rootDir, TiffTagConstants.TIFF_TAG_COPYRIGHT, value);
                    break;
                case "software":
                    updateField(rootDir, TiffTagConstants.TIFF_TAG_SOFTWARE, value);
                    break;
                case "date":
                    updateField(exifDir, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, value);
                    break;
                case "gps":
                    // Format expected: "latitude, longitude" e.g. "52.2297, 21.0122"
                    updateGps(outputSet, value);
                    break;
            }
        }

        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(destination))) {
            new ExifRewriter().updateExifMetadataLossless(source, os, outputSet);
        }
    }

    private void updateField(TiffOutputDirectory dir, org.apache.commons.imaging.formats.tiff.taginfos.TagInfoAscii tagInfo, String value) throws Exception {
        dir.removeField(tagInfo);
        dir.add(tagInfo, value);
    }

    private void updateGps(TiffOutputSet outputSet, String value) throws Exception {
        String[] parts = value.split(",");
        if (parts.length != 2) return;
        
        double lat = Double.parseDouble(parts[0].trim());
        double lon = Double.parseDouble(parts[1].trim());
        
        outputSet.setGPSInDegrees(lon, lat); // Coordinates are (longitude, latitude) in setGPSInDegrees
    }

    @Override
    public boolean supports(String extension) {
        return extension != null && (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg"));
    }
}
