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
- **Universal Metadata Editor (PRO):** Edit metadata of **any file type**. The tool reads all existing metadata, displays them as a numbered list, and lets you change any value you want.
  - **JPEG/PNG:** Changes are written directly into the file using EXIF/tEXt chunks.
  - **Other formats (PDF, DOCX, MP4, etc.):** A sidecar `.metadata.json` file is generated alongside the target file.
- **In-Place Editing:** Modifies the file seamlessly without cluttering your system with `_edited` copies, using auto-buffering for safety.
- **Force Mode [F] Payload Injection:** Bypasses structural read-only restrictions to inject metadata via stealth text payload chunks (e.g. `tEXt`) — perfect for testing forensic tool blind spots.

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

# Edit metadata of ANY file (interactive)
java -jar target/vanish-forensics-1.0.0.jar --edit photo.jpg
java -jar target/vanish-forensics-1.0.0.jar --edit document.pdf
java -jar target/vanish-forensics-1.0.0.jar --edit screenshot.png
```

## ✍️ How to Edit Metadata (Step-by-Step)

Vanish can edit metadata of **any file type**. Here's how:

1.  **Launch the editor** — either via the menu (`[4] ✍️ Edit metadata`) or directly:
    ```bash
    java -jar target/vanish-forensics-1.0.0.jar --edit myfile.png
    ```
2.  **Vanish reads all metadata** from the file and displays a numbered table:
    ```
      ┌─────┬──────────────────────────────────────┬──────────────────────────────────────┐
      │  #  │ Field                                │ Value                                │
      ├─────┼──────────────────────────────────────┼──────────────────────────────────────┤
      │ 1   │ Author                               │ John Doe                             │
      │ 2   │ 🔒Chroma ColorSpaceType              │ RGB                                  │
      │ 3   │ GPS Latitude                         │ 52.2297                              │
      │ ...                                                                               │
      └─────┴──────────────────────────────────────┴──────────────────────────────────────┘
      Total: 28 fields (🔒 = read-only structural data, cannot be edited)
    ```
3.  **Pick any field by its number** to change its value. Example: type `1` → enter new author name.
    *   *Note: Fields with `🔒` are binary structural properties. To forcefully falsify them with a payload chunk, type `F` to enable **Force Mode**.*
4.  **Add new fields** with `[A]` — type any custom field name and value.
5.  **Review your changes** with `[L]` to see the full list + pending edits.
6.  **Save** with `[S]` — Vanish safely applies your changes overwriting the original file.

## 🛠️ Technology Stack
- **Core:** Java 17+
- **Image Extraction:** `metadata-extractor` (v2.19.0)
- **Document Analysis:** `Apache Tika` (v2.9.1)
- **Metadata Editing:** `Apache Commons Imaging` (v1.0-alpha3)
- **JSON Engine:** `Gson` (v2.11.0)
- **Build System:** Maven

---
*Developed for educational and forensic purposes. Stay safe and protect your metadata.*
