package com.vanish.forensics.model;

/**
 * Holds metadata extracted from document files (PDF, DOCX, XLSX, PPTX, etc.).
 */
public class DocumentData {

    private String author;
    private String title;
    private String subject;
    private String description;
    private String creator;       // Application that created the document
    private String producer;      // PDF producer
    private String language;
    private String creationDate;
    private String modificationDate;
    private String lastAuthor;    // Last person who saved the document
    private String revision;
    private String company;
    private String manager;
    private String category;
    private String keywords;
    private int pageCount;
    private int wordCount;
    private int characterCount;

    // --- Getters and Setters ---

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }

    public String getProducer() { return producer; }
    public void setProducer(String producer) { this.producer = producer; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getCreationDate() { return creationDate; }
    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    public String getModificationDate() { return modificationDate; }
    public void setModificationDate(String modificationDate) { this.modificationDate = modificationDate; }

    public String getLastAuthor() { return lastAuthor; }
    public void setLastAuthor(String lastAuthor) { this.lastAuthor = lastAuthor; }

    public String getRevision() { return revision; }
    public void setRevision(String revision) { this.revision = revision; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getManager() { return manager; }
    public void setManager(String manager) { this.manager = manager; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }

    public int getPageCount() { return pageCount; }
    public void setPageCount(int pageCount) { this.pageCount = pageCount; }

    public int getWordCount() { return wordCount; }
    public void setWordCount(int wordCount) { this.wordCount = wordCount; }

    public int getCharacterCount() { return characterCount; }
    public void setCharacterCount(int characterCount) { this.characterCount = characterCount; }
}
