package com.sweetbook.vo;

import java.util.List;

public class BookRequestPreviewVO {

    private Long petId;
    private String petName;
    private String title;
    private String coverTitle;
    private String coverSubtitle;
    private String dedicationText;
    private String templateCode;
    private String bookSpecCode;
    private List<BookPagePreviewVO> pages;

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
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

    public List<BookPagePreviewVO> getPages() {
        return pages;
    }

    public void setPages(List<BookPagePreviewVO> pages) {
        this.pages = pages;
    }
}