package com.vanish.forensics.model;

/**
 * Represents GPS coordinates extracted from image EXIF data.
 * Provides utility methods for formatting and generating map links.
 */
public class GpsCoordinates {

    private final double latitude;
    private final double longitude;

    public GpsCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    /**
     * Generates a Google Maps URL for these coordinates.
     */
    public String toGoogleMapsUrl() {
        return String.format("https://www.google.com/maps?q=%.6f,%.6f", latitude, longitude);
    }

    /**
     * Generates an OpenStreetMap URL for these coordinates.
     */
    public String toOpenStreetMapUrl() {
        return String.format("https://www.openstreetmap.org/?mlat=%.6f&mlon=%.6f&zoom=15", latitude, longitude);
    }

    /**
     * Converts decimal degrees to Degrees/Minutes/Seconds format.
     */
    public String toDMSString() {
        return String.format("%s, %s", decimalToDMS(latitude, true), decimalToDMS(longitude, false));
    }

    private String decimalToDMS(double decimal, boolean isLatitude) {
        String direction = isLatitude
                ? (decimal >= 0 ? "N" : "S")
                : (decimal >= 0 ? "E" : "W");

        decimal = Math.abs(decimal);
        int degrees = (int) decimal;
        double minutesDecimal = (decimal - degrees) * 60;
        int minutes = (int) minutesDecimal;
        double seconds = (minutesDecimal - minutes) * 60;

        return String.format("%d°%d'%.2f\"%s", degrees, minutes, seconds, direction);
    }

    @Override
    public String toString() {
        return String.format("%.6f, %.6f", latitude, longitude);
    }
}
