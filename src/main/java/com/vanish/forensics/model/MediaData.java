package com.vanish.forensics.model;

/**
 * Model for Audio and Video metadata.
 */
public class MediaData {
    // General
    private String title;
    private String artist;
    private String album;
    private String date;
    private String genre;
    private String duration; // e.g. "00:03:45"
    private String software; // Encoding software/tool

    // Video specific
    private int width;
    private int height;
    private Double frameRate;
    private String videoCodec;
    
    // Audio specific
    private String audioCodec;
    private String sampleRate;
    private String channels;
    private String bitrate;

    // Location (Video GPS)
    private GpsCoordinates gpsCoordinates;

    // --- Getters and Setters ---

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getSoftware() { return software; }
    public void setSoftware(String software) { this.software = software; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public Double getFrameRate() { return frameRate; }
    public void setFrameRate(Double frameRate) { this.frameRate = frameRate; }

    public String getVideoCodec() { return videoCodec; }
    public void setVideoCodec(String videoCodec) { this.videoCodec = videoCodec; }

    public String getAudioCodec() { return audioCodec; }
    public void setAudioCodec(String audioCodec) { this.audioCodec = audioCodec; }

    public String getSampleRate() { return sampleRate; }
    public void setSampleRate(String sampleRate) { this.sampleRate = sampleRate; }

    public String getChannels() { return channels; }
    public void setChannels(String channels) { this.channels = channels; }

    public String getBitrate() { return bitrate; }
    public void setBitrate(String bitrate) { this.bitrate = bitrate; }

    public GpsCoordinates getGpsCoordinates() { return gpsCoordinates; }
    public void setGpsCoordinates(GpsCoordinates gpsCoordinates) { this.gpsCoordinates = gpsCoordinates; }

    public boolean hasGps() { return gpsCoordinates != null; }
    
    public String getResolution() {
        if (width > 0 && height > 0) return width + "x" + height;
        return null;
    }
}
