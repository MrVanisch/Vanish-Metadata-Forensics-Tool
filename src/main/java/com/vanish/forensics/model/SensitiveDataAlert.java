package com.vanish.forensics.model;

/**
 * Represents a sensitive data alert found during metadata analysis.
 * Each alert has a severity level, description, and remediation suggestion.
 */
public class SensitiveDataAlert {

    /**
     * Severity levels for sensitive data findings.
     */
    public enum Severity {
        LOW("LOW", "ℹ️"),
        MEDIUM("MEDIUM", "⚠️"),
        HIGH("HIGH", "🔶"),
        CRITICAL("CRITICAL", "🔴");

        private final String label;
        private final String icon;

        Severity(String label, String icon) {
            this.label = label;
            this.icon = icon;
        }

        public String getLabel() { return label; }
        public String getIcon() { return icon; }
    }

    /**
     * Types of sensitive data that can be detected.
     */
    public enum AlertType {
        GPS_LOCATION,
        PERSONAL_NAME,
        SERIAL_NUMBER,
        SOFTWARE_INFO,
        EDIT_HISTORY,
        COMPANY_INFO,
        CREATION_DATE,
        DEVICE_INFO,
        EMAIL_ADDRESS,
        PHONE_NUMBER,
        EMBEDDED_THUMBNAIL
    }

    private final AlertType type;
    private final Severity severity;
    private final String fieldName;
    private final String fieldValue;
    private final String description;
    private final String remediation;

    public SensitiveDataAlert(AlertType type, Severity severity, String fieldName,
                               String fieldValue, String description, String remediation) {
        this.type = type;
        this.severity = severity;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.description = description;
        this.remediation = remediation;
    }

    public AlertType getType() { return type; }
    public Severity getSeverity() { return severity; }
    public String getFieldName() { return fieldName; }
    public String getFieldValue() { return fieldValue; }
    public String getDescription() { return description; }
    public String getRemediation() { return remediation; }

    @Override
    public String toString() {
        return String.format("[%s] %s %s: %s = %s",
                severity.getLabel(), severity.getIcon(), type, fieldName, fieldValue);
    }
}
