package com.sweetbook.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PetVO {

    private Long petId;
    private String petToken;
    private String name;
    private String profileImageUrl;
    private LocalDate memorialDate;
    private LocalDateTime createdAt;

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public String getPetToken() {
        return petToken;
    }

    public void setPetToken(String petToken) {
        this.petToken = petToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public LocalDate getMemorialDate() {
        return memorialDate;
    }

    public void setMemorialDate(LocalDate memorialDate) {
        this.memorialDate = memorialDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}