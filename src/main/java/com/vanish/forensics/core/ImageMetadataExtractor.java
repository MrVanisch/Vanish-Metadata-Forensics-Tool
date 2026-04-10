package com.vanish.forensics.core;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.Directory;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.lang.GeoLocation;

import com.vanish.forensics.model.ExifData;
import com.vanish.forensics.model.FileMetadata;
import com.vanish.forensics.model.GpsCoordinates;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;

/**
 * Extracts EXIF, IPTC, and XMP metadata from image files.
 * Uses Drew Noakes' metadata-extractor library.
 *
 * Supported formats: JPEG, PNG, TIFF, WebP, HEIF, GIF, BMP, RAW formats
 */
public class ImageMetadataExtractor implements MetadataExtractor {

    private static final String[] SUPPORTED_TYPES = {
            "image/jpeg", "image/png", "image/tiff", "image/webp",
            "image/heif", "image/heic", "image/gif", "image/bmp",
            "image/x-canon-cr2", "image/x-nikon-nef", "image/x-sony-arw"
    };

    @Override
    public FileMetadata extract(File file) throws IOException {
        FileMetadata metadata = new FileMetadata();

        // Basic file info
        metadata.setFileName(file.getName());
        metadata.setFilePath(file.getAbsolutePath());
        metadata.setFileSize(file.length());
        metadata.setFileExtension(getExtension(file));

        try {
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            metadata.setLastModified(sdf.format(attrs.lastModifiedTime().toMillis()));
        } catch (IOException e) {
            // Ignore if we can't read file attributes
        }

        try {
            // Read image metadata using metadata-extractor
            Metadata imageMetadata = ImageMetadataReader.readMetadata(file);

            // Store all raw metadata
            for (Directory directory : imageMetadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    String key = directory.getName() + " - " + tag.getTagName();
                    String value = tag.getDescription();
                    if (value != null && !value.isEmpty()) {
                        metadata.addRawMetadata(key, value);
                    }
                }
            }

            // Extract structured EXIF data
            ExifData exifData = extractExifData(imageMetadata);
            metadata.setExifData(exifData);

            // Detect MIME type from metadata
            String mimeType = detectMimeType(file);
            metadata.setMimeType(mimeType);

        } catch (ImageProcessingException e) {
            metadata.setMimeType("image/unknown");
            metadata.addRawMetadata("Error", "Could not process image metadata: " + e.getMessage());
        }

        return metadata;
    }

    /**
     * Extracts structured EXIF data from image metadata directories.
     */
    private ExifData extractExifData(Metadata imageMetadata) {
        ExifData exifData = new ExifData();

        // ExifIFD0 — camera make, model, software, orientation
        ExifIFD0Directory ifd0 = imageMetadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        if (ifd0 != null) {
            exifData.setCameraMake(ifd0.getDescription(ExifIFD0Directory.TAG_MAKE));
            exifData.setCameraModel(ifd0.getDescription(ExifIFD0Directory.TAG_MODEL));
            exifData.setSoftware(ifd0.getDescription(ExifIFD0Directory.TAG_SOFTWARE));
            exifData.setOrientation(ifd0.getDescription(ExifIFD0Directory.TAG_ORIENTATION));

            if (ifd0.containsTag(ExifIFD0Directory.TAG_IMAGE_WIDTH)) {
                exifData.setImageWidth(ifd0.getInteger(ExifIFD0Directory.TAG_IMAGE_WIDTH));
            }
            if (ifd0.containsTag(ExifIFD0Directory.TAG_IMAGE_HEIGHT)) {
                exifData.setImageHeight(ifd0.getInteger(ExifIFD0Directory.TAG_IMAGE_HEIGHT));
            }
        }

        // ExifSubIFD — shooting parameters
        ExifSubIFDDirectory subIfd = imageMetadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (subIfd != null) {
            exifData.setDateTimeOriginal(subIfd.getDescription(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL));
            exifData.setDateTimeDigitized(subIfd.getDescription(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED));
            exifData.setExposureTime(subIfd.getDescription(ExifSubIFDDirectory.TAG_EXPOSURE_TIME));
            exifData.setFNumber(subIfd.getDescription(ExifSubIFDDirectory.TAG_FNUMBER));
            exifData.setIso(subIfd.getDescription(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT));
            exifData.setFocalLength(subIfd.getDescription(ExifSubIFDDirectory.TAG_FOCAL_LENGTH));
            exifData.setFlash(subIfd.getDescription(ExifSubIFDDirectory.TAG_FLASH));
            exifData.setWhiteBalance(subIfd.getDescription(ExifSubIFDDirectory.TAG_WHITE_BALANCE_MODE));
            exifData.setColorSpace(subIfd.getDescription(ExifSubIFDDirectory.TAG_COLOR_SPACE));
            exifData.setLensModel(subIfd.getDescription(ExifSubIFDDirectory.TAG_LENS_MODEL));

            // Try to get image dimensions from SubIFD if not found in IFD0
            if (exifData.getImageWidth() == 0 && subIfd.containsTag(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH)) {
                exifData.setImageWidth(subIfd.getInteger(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH));
            }
            if (exifData.getImageHeight() == 0 && subIfd.containsTag(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT)) {
                exifData.setImageHeight(subIfd.getInteger(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT));
            }

            // Camera serial number (body serial)
            exifData.setSerialNumber(subIfd.getDescription(ExifSubIFDDirectory.TAG_BODY_SERIAL_NUMBER));
        }

        // GPS Directory
        GpsDirectory gpsDir = imageMetadata.getFirstDirectoryOfType(GpsDirectory.class);
        if (gpsDir != null) {
            GeoLocation geoLocation = gpsDir.getGeoLocation();
            if (geoLocation != null && !geoLocation.isZero()) {
                exifData.setGpsCoordinates(
                        new GpsCoordinates(geoLocation.getLatitude(), geoLocation.getLongitude())
                );
            }
        }

        return exifData;
    }

    @Override
    public boolean supports(String mimeType) {
        if (mimeType == null) return false;
        for (String type : SUPPORTED_TYPES) {
            if (mimeType.equalsIgnoreCase(type)) return true;
        }
        return mimeType.startsWith("image/");
    }

    @Override
    public String getName() {
        return "Image Metadata Extractor (EXIF/IPTC/XMP)";
    }

    private String getExtension(File file) {
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(dot + 1).toLowerCase() : "";
    }

    private String detectMimeType(File file) {
        String ext = getExtension(file);
        switch (ext) {
            case "jpg": case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "gif": return "image/gif";
            case "tiff": case "tif": return "image/tiff";
            case "webp": return "image/webp";
            case "heif": case "heic": return "image/heif";
            case "bmp": return "image/bmp";
            case "cr2": return "image/x-canon-cr2";
            case "nef": return "image/x-nikon-nef";
            case "arw": return "image/x-sony-arw";
            default: return "image/" + ext;
        }
    }
}
