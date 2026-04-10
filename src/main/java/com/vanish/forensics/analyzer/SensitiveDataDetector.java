package com.vanish.forensics.analyzer;

import com.vanish.forensics.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Analyzes extracted metadata to detect sensitive or privacy-compromising information.
 * Assigns severity levels and provides remediation suggestions.
 */
public class SensitiveDataDetector {

    // Regex patterns for detecting sensitive data in free-text fields
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("(\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{2,4}\\)?[-.\\s]?\\d{3,4}[-.\\s]?\\d{3,4}");

    /**
     * Analyzes a FileMetadata object and returns a list of sensitive data alerts.
     * Also adds the alerts to the FileMetadata object itself.
     *
     * @param metadata the metadata to analyze
     * @return list of detected sensitive data alerts
     */
    public List<SensitiveDataAlert> analyze(FileMetadata metadata) {
        List<SensitiveDataAlert> alerts = new ArrayList<>();

        // Analyze EXIF data
        if (metadata.hasExifData()) {
            analyzeExifData(metadata.getExifData(), alerts);
        }

        // Analyze document data
        if (metadata.hasDocumentData()) {
            analyzeDocumentData(metadata.getDocumentData(), alerts);
        }

        // Analyze raw metadata for patterns
        analyzeRawMetadata(metadata, alerts);

        // Set alerts on metadata object
        metadata.setAlerts(alerts);

        return alerts;
    }

    /**
     * Checks EXIF data for sensitive information.
     */
    private void analyzeExifData(ExifData exif, List<SensitiveDataAlert> alerts) {

        // CRITICAL: GPS Location
        if (exif.hasGps()) {
            GpsCoordinates gps = exif.getGpsCoordinates();
            alerts.add(new SensitiveDataAlert(
                    SensitiveDataAlert.AlertType.GPS_LOCATION,
                    SensitiveDataAlert.Severity.CRITICAL,
                    "GPS Coordinates",
                    gps.toString() + " (" + gps.toDMSString() + ")",
                    "Image contains GPS coordinates that reveal the exact location where the photo was taken. " +
                    "This can expose home address, workplace, or other sensitive locations.",
                    "Remove EXIF GPS data before sharing. Use the 'Clean Metadata' feature."
            ));
        }

        // HIGH: Camera serial number (can identify unique device owner)
        if (exif.getSerialNumber() != null && !exif.getSerialNumber().isEmpty()) {
            alerts.add(new SensitiveDataAlert(
                    SensitiveDataAlert.AlertType.SERIAL_NUMBER,
                    SensitiveDataAlert.Severity.HIGH,
                    "Camera Serial Number",
                    exif.getSerialNumber(),
                    "Camera body serial number can uniquely identify the device and its owner across images.",
                    "Remove serial number metadata before sharing publicly."
            ));
        }

        // HIGH: Camera make and model (device fingerprinting)
        if (exif.getCameraMake() != null && exif.getCameraModel() != null) {
            alerts.add(new SensitiveDataAlert(
                    SensitiveDataAlert.AlertType.DEVICE_INFO,
                    SensitiveDataAlert.Severity.MEDIUM,
                    "Camera Device",
                    exif.getCameraMake() + " " + exif.getCameraModel(),
                    "Camera make and model can be used for device fingerprinting and narrowing down the photographer.",
                    "Consider removing device information for anonymous sharing."
            ));
        }

        // MEDIUM: Software used for editing
        if (exif.getSoftware() != null && !exif.getSoftware().isEmpty()) {
            alerts.add(new SensitiveDataAlert(
                    SensitiveDataAlert.AlertType.SOFTWARE_INFO,
                    SensitiveDataAlert.Severity.MEDIUM,
                    "Software",
                    exif.getSoftware(),
                    "Software information reveals tools used for photo editing/processing.",
                    "Remove software metadata if anonymity is required."
            ));
        }

        // LOW: Date/time of capture
        if (exif.getDateTimeOriginal() != null) {
            alerts.add(new SensitiveDataAlert(
                    SensitiveDataAlert.AlertType.CREATION_DATE,
                    SensitiveDataAlert.Severity.LOW,
                    "Date/Time Original",
                    exif.getDateTimeOriginal(),
                    "Original capture date/time can reveal when and potentially where you were.",
                    "Remove date metadata for enhanced privacy."
            ));
        }
    }

    /**
     * Checks document metadata for sensitive information.
     */
    private void analyzeDocumentData(DocumentData doc, List<SensitiveDataAlert> alerts) {

        // HIGH: Author name
        if (doc.getAuthor() != null && !doc.getAuthor().isEmpty()) {
            alerts.add(new SensitiveDataAlert(
                    SensitiveDataAlert.AlertType.PERSONAL_NAME,
                    SensitiveDataAlert.Severity.HIGH,
                    "Author",
                    doc.getAuthor(),
                    "Document author reveals personal identity of the creator.",
                    "Remove author metadata before sharing anonymously."
            ));
        }

        // HIGH: Last author
        if (doc.getLastAuthor() != null && !doc.getLastAuthor().isEmpty()
                && !doc.getLastAuthor().equals(doc.getAuthor())) {
            alerts.add(new SensitiveDataAlert(
                    SensitiveDataAlert.AlertType.PERSONAL_NAME,
                    SensitiveDataAlert.Severity.HIGH,
                    "Last Author",
                    doc.getLastAuthor(),
                    "Last editor's name is stored in the document metadata.",
                    "Remove last author metadata."
            ));
        }

        // HIGH: Company name
        if (doc.getCompany() != null && !doc.getCompany().isEmpty()) {
            alerts.add(new SensitiveDataAlert(
                    SensitiveDataAlert.AlertType.COMPANY_INFO,
                    SensitiveDataAlert.Severity.HIGH,
                    "Company",
                    doc.getCompany(),
                    "Company name reveals the organization that created this document.",
                    "Remove company metadata for external sharing."
            ));
        }

        // MEDIUM: Manager name
        if (doc.getManager() != null && !doc.getManager().isEmpty()) {
            alerts.add(new SensitiveDataAlert(
                    SensitiveDataAlert.AlertType.PERSONAL_NAME,
                    SensitiveDataAlert.Severity.MEDIUM,
                    "Manager",
                    doc.getManager(),
                    "Manager name reveals internal organizational structure.",
                    "Remove manager metadata."
            ));
        }

        // MEDIUM: Software / creator application
        if (doc.getCreator() != null && !doc.getCreator().isEmpty()) {
            alerts.add(new SensitiveDataAlert(
                    SensitiveDataAlert.AlertType.SOFTWARE_INFO,
                    SensitiveDataAlert.Severity.MEDIUM,
                    "Creator Application",
                    doc.getCreator(),
                    "Creator application reveals the software and version used to create this document.",
                    "Remove application metadata if needed."
            ));
        }

        // MEDIUM: Revision history
        if (doc.getRevision() != null && !doc.getRevision().isEmpty()) {
            try {
                int revisions = Integer.parseInt(doc.getRevision().trim());
                if (revisions > 1) {
                    alerts.add(new SensitiveDataAlert(
                            SensitiveDataAlert.AlertType.EDIT_HISTORY,
                            SensitiveDataAlert.Severity.MEDIUM,
                            "Revision Count",
                            doc.getRevision(),
                            "Document has been revised " + revisions + " times. Edit history may contain traces of previous versions.",
                            "Save as a new document to reset revision history."
                    ));
                }
            } catch (NumberFormatException ignored) {}
        }

        // LOW: Creation date
        if (doc.getCreationDate() != null) {
            alerts.add(new SensitiveDataAlert(
                    SensitiveDataAlert.AlertType.CREATION_DATE,
                    SensitiveDataAlert.Severity.LOW,
                    "Creation Date",
                    doc.getCreationDate(),
                    "Document creation date reveals when the document was first created.",
                    "Remove date metadata if needed."
            ));
        }
    }

    /**
     * Scans all raw metadata values for email addresses and phone numbers.
     */
    private void analyzeRawMetadata(FileMetadata metadata, List<SensitiveDataAlert> alerts) {
        for (var entry : metadata.getRawMetadata().entrySet()) {
            String value = entry.getValue();
            if (value == null) continue;

            // Check for email addresses
            Matcher emailMatcher = EMAIL_PATTERN.matcher(value);
            while (emailMatcher.find()) {
                alerts.add(new SensitiveDataAlert(
                        SensitiveDataAlert.AlertType.EMAIL_ADDRESS,
                        SensitiveDataAlert.Severity.HIGH,
                        entry.getKey(),
                        emailMatcher.group(),
                        "Email address found in metadata field '" + entry.getKey() + "'.",
                        "Remove email address from metadata."
                ));
            }

            // Check for phone numbers (only in specific fields that might contain them)
            if (entry.getKey().toLowerCase().contains("phone") ||
                entry.getKey().toLowerCase().contains("contact") ||
                entry.getKey().toLowerCase().contains("tel")) {
                Matcher phoneMatcher = PHONE_PATTERN.matcher(value);
                while (phoneMatcher.find()) {
                    alerts.add(new SensitiveDataAlert(
                            SensitiveDataAlert.AlertType.PHONE_NUMBER,
                            SensitiveDataAlert.Severity.HIGH,
                            entry.getKey(),
                            phoneMatcher.group(),
                            "Phone number found in metadata field '" + entry.getKey() + "'.",
                            "Remove phone number from metadata."
                    ));
                }
            }
        }
    }
}
