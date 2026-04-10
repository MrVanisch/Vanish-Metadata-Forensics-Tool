# 🔍 VANISH — Metadata Forensics Tool

**Advanced OSINT & Forensic tool for metadata extraction, analysis, and cleaning.**

Vanish is a powerful Java-based security utility designed to uncover hidden information within files, detect privacy risks, and securely strip metadata.

## 🌟 Key Features

### 1. 🔍 Comprehensive Extraction
- **Images:** Full EXIF, IPTC, and XMP extraction (GPS, Camera Serial, Software, History).
- **Multimedia:** Forensic analysis of **Video (MP4, MOV, AVI)** and **Audio (MP3, WAV, FLAC)** — including duration, resolution, codecs, and GPS location in videos.
- **Documents:** Metadata from PDF, DOCX, XLSX, PPTX, and more using **Apache Tika**.
- **Forensics:** Cryptographic hashes (MD5, SHA-1, SHA-256) and Shannon entropy analysis.

### 2. 🛡️ Privacy Risk Scoring
- Automated **Risk Score (0-100)** based on detected sensitive data.
- **Sensitive Data Detection:** Identification of GPS, names, emails, serial numbers, and software trails.
- **Magic Byte Verification:** Detects if a file is disguised with a fake extension.

### 3. 🧹 Metadata Cleaning
- Securely remove metadata from images (JPEG, PNG, etc.) via re-encoding.
- Backup creation for original files.

### 4. 📊 Professional Reporting
- **Console:** Interactive, color-coded dashboard with risk alerts.
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

### Usage

```bash
# Start Interactive Menu
java -jar target/vanish-forensics-1.0.0.jar

# Analyze a single file with HTML report
java -jar target/vanish-forensics-1.0.0.jar --file photo.jpg --html

# Batch analyze a directory with all exports
java -jar target/vanish-forensics-1.0.0.jar --dir ./evidence --json --html

# Clean metadata
java -jar target/vanish-forensics-1.0.0.jar --clean secret.jpg
```

## 🛠️ Technology Stack
- **Core:** Java 17+
- **Image Extraction:** `metadata-extractor` (v2.19.0)
- **Document Analysis:** `Apache Tika` (v2.9.1)
- **JSON Engine:** `Gson` (v2.11.0)
- **Build System:** Maven

## 📐 Architecture
Modular structure following clean code principles:
- `com.vanish.forensics.core`: Extraction engines.
- `com.vanish.forensics.analyzer`: Forensic and risk analysis logic.
- `com.vanish.forensics.report`: Multi-format generators (HTML/JSON/Console).
- `com.vanish.forensics.model`: Unified data models.

---
*Developed for educational and forensic purposes. Stay safe and protect your metadata.*
