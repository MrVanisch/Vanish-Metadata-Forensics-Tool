# 🔍 VANISH — Metadata Forensics Tool

**Advanced OSINT & Forensic tool for metadata extraction, analysis, and cleaning.**

Vanish is a powerful Java-based security utility designed to uncover hidden information within files, detect privacy risks, and securely manage your digital footprint.

## 🌟 Key Features

### 1. 🔍 Comprehensive Extraction
- **Images:** Full EXIF, IPTC, and XMP extraction (GPS, Camera Serial, Software, History).
- **Multimedia:** Forensic analysis of **Video (MP4, MOV, AVI)** and **Audio (MP3, WAV, FLAC)** — including duration, resolution, codecs, and GPS location in videos.
- **Documents:** Metadata from PDF, DOCX, XLSX, PPTX, and more using **Apache Tika**.
- **PDF Forensics:** Detection of PDF version, encryption status, interactive forms, and embedded attachments.
- **Forensics:** Cryptographic hashes (MD5, SHA-1, SHA-256) and Shannon entropy analysis.

### 2. 🛡️ Privacy Risk Scoring
- Automated **Risk Score (0-100)** based on detected sensitive data.
- **Sensitive Data Detection:** Identification of GPS, names, emails, serial numbers, and software trails.
- **Magic Byte Verification:** Detects if a file is disguised with a fake extension.

### 3. 🧹 Metadata Cleaning & Editing
- **Metadata Cleaning:** Securely remove metadata from images (JPEG, PNG, etc.) via re-encoding.
- **Metadata Editing (PRO):** Interactively modify EXIF/Text tags including **Artist, Description, Copyright, Software, Date, and GPS coordinates** in both **JPEG and PNG** files.
- **Safety:** Always creates a new `_edited.jpg` or `_edited.png` file to preserve original evidence.
- **Backup creation** for original files during cleaning.

### 4. 📊 Professional Reporting
- **Console:** Interactive, color-coded dashboard with risk alerts and forensic mapping.
- **JSON:** Machine-readable export for automated workflows.
- **HTML:** Modern Dark-Mode reports with embedded GPS map links and glassmorphism design.

## 🚀 Quick Start

### Requirements
- **Java JDK 17+**

### Installation & Build
```bash
# Clone the repository
git clone https://github.com/MrVanisch/Vanish-Metadata-Forensics-Tool.git
cd Vanish-Metadata-Forensics-Tool

# Build the project (using integrated Maven)
./mvnw clean package -DskipTests
```

### Usage Examples

```bash
# Start Interactive Menu
java -jar target/vanish-forensics-1.0.0.jar

# Analyze a single file with HTML report
java -jar target/vanish-forensics-1.0.0.jar --file photo.jpg --html

# Forensic video/audio analysis
java -jar target/vanish-forensics-1.0.0.jar --file evidence.mp4 --html

# Clean metadata
java -jar target/vanish-forensics-1.0.0.jar --clean secret.jpg

# Edit metadata (interactively for a specific file)
java -jar target/vanish-forensics-1.0.0.jar --edit photo.jpg
```

## ✍️ How to Edit Metadata (Step-by-Step)

Vanish allows you to professionally modify metadata for JPEG images to protect your privacy or for testing purposes:

1.  **Launch the tool** and select option `[4] ✍️ Edit metadata (PRO)`.
2.  **Provide the path** to your JPEG file (e.g., `evidence/photo.jpg`).
3.  **Select fields to edit** by typing their ID (1-6):
    *   `[1]` Artist (Author)
    *   `[2]` Description (Image Title)
    *   `[3]` Copyright
    *   `[4]` Software (Encoder tool)
    *   `[5]` Date (Formatted as `YYYY:MM:DD HH:MM:SS`)
    *   `[6]` GPS Coordinates (Formatted as `Latitude, Longitude`)
4.  **Save Changes**: Press `[S]` to apply all edits. Vanish will generate a new file named `photo_edited.jpg`.
5.  **Analyze the new file**: Use Option `[1]` to verify that the new metadata is active.

## 🛠️ Technology Stack
- **Core:** Java 17+
- **Image Extraction:** `metadata-extractor` (v2.19.0)
- **Document Analysis:** `Apache Tika` (v2.9.1)
- **Metadata Editing:** `Apache Commons Imaging` (v1.0-alpha3)
- **JSON Engine:** `Gson` (v2.11.0)
- **Build System:** Maven

---
*Developed for educational and forensic purposes. Stay safe and protect your metadata.*
