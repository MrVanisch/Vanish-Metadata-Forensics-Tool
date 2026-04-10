package com.vanish.forensics.model;

/**
 * Holds EXIF metadata extracted from image files.
 * Includes camera info, shooting parameters, and GPS data.
 */
public class ExifData {

    private String cameraMake;
    private String cameraModel;
    private String lensModel;
    private String dateTimeOriginal;
    private String dateTimeDigitized;
    private String exposureTime;
    private String fNumber;
    private String iso;
    private String focalLength;
    private String flash;
    private String whiteBalance;
    private String orientation;
    private String software;
    private String colorSpace;
    private int imageWidth;
    private int imageHeight;
    private String serialNumber;
    private GpsCoordinates gpsCoordinates;

    // --- Getters and Setters ---

    public String getCameraMake() { return cameraMake; }
    public void setCameraMake(String cameraMake) { this.cameraMake = cameraMake; }

    public String getCameraModel() { return cameraModel; }
    public void setCameraModel(String cameraModel) { this.cameraModel = cameraModel; }

    public String getLensModel() { return lensModel; }
    public void setLensModel(String lensModel) { this.lensModel = lensModel; }

    public String getDateTimeOriginal() { return dateTimeOriginal; }
    public void setDateTimeOriginal(String dateTimeOriginal) { this.dateTimeOriginal = dateTimeOriginal; }

    public String getDateTimeDigitized() { return dateTimeDigitized; }
    public void setDateTimeDigitized(String dateTimeDigitized) { this.dateTimeDigitized = dateTimeDigitized; }

    public String getExposureTime() { return exposureTime; }
    public void setExposureTime(String exposureTime) { this.exposureTime = exposureTime; }

    public String getFNumber() { return fNumber; }
    public void setFNumber(String fNumber) { this.fNumber = fNumber; }

    public String getIso() { return iso; }
    public void setIso(String iso) { this.iso = iso; }

    public String getFocalLength() { return focalLength; }
    public void setFocalLength(String focalLength) { this.focalLength = focalLength; }

    public String getFlash() { return flash; }
    public void setFlash(String flash) { this.flash = flash; }

    public String getWhiteBalance() { return whiteBalance; }
    public void setWhiteBalance(String whiteBalance) { this.whiteBalance = whiteBalance; }

    public String getOrientation() { return orientation; }
    public void setOrientation(String orientation) { this.orientation = orientation; }

    public String getSoftware() { return software; }
    public void setSoftware(String software) { this.software = software; }

    public String getColorSpace() { return colorSpace; }
    public void setColorSpace(String colorSpace) { this.colorSpace = colorSpace; }

    public int getImageWidth() { return imageWidth; }
    public void setImageWidth(int imageWidth) { this.imageWidth = imageWidth; }

    public int getImageHeight() { return imageHeight; }
    public void setImageHeight(int imageHeight) { this.imageHeight = imageHeight; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public GpsCoordinates getGpsCoordinates() { return gpsCoordinates; }
    public void setGpsCoordinates(GpsCoordinates gpsCoordinates) { this.gpsCoordinates = gpsCoordinates; }

    public boolean hasGps() {
        return gpsCoordinates != null;
    }

    public String getResolution() {
        if (imageWidth > 0 && imageHeight > 0) {
            return imageWidth + " x " + imageHeight;
        }
        return null;
    }
}
