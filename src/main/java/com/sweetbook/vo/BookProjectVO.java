package com.sweetbook.vo;

import java.time.LocalDateTime;

public class BookProjectVO {

    private Long bookProjectId;
    private Long petId;
    private String title;
    private String coverTitle;
    private String coverSubtitle;
    private String dedicationText;
    private String templateCode;
    private String bookSpecCode;
    private String sweetbookBookId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime finalizedAt;

    public Long getBookProjectId() {
        return bookProjectId;
    }

    public void setBookProjectId(Long bookProjectId) {
        this.bookProjectId = bookProjectId;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCoverTitle() {
        return coverTitle;
    }

    public void setCoverTitle(String coverTitle) {
        this.coverTitle = coverTitle;
    }

    public String getCoverSubtitle() {
        return coverSubtitle;
    }

    public void setCoverSubtitle(String coverSubtitle) {
        this.coverSubtitle = coverSubtitle;
    }

    public String getDedicationText() {
        return dedicationText;
    }

    public void setDedicationText(String dedicationText) {
        this.dedicationText = dedicationText;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getBookSpecCode() {
        return bookSpecCode;
    }

    public void setBookSpecCode(String bookSpecCode) {
        this.bookSpecCode = bookSpecCode;
    }

    public String getSweetbookBookId() {
        return sweetbookBookId;
    }

    public void setSweetbookBookId(String sweetbookBookId) {
        this.sweetbookBookId = sweetbookBookId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getFinalizedAt() {
        return finalizedAt;
    }

    public void setFinalizedAt(LocalDateTime finalizedAt) {
        this.finalizedAt = finalizedAt;
    }
}