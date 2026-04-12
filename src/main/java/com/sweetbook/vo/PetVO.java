package com.sweetbook.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PetVO {

    private Long petId;
    private Long memberId;
    private String name;
    private String color;
    private String breed;
    private String relationshipLabel;
    private LocalDate memorialDate;
    private LocalDateTime createdAt;

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getRelationshipLabel() {
        return relationshipLabel;
    }

    public void setRelationshipLabel(String relationshipLabel) {
        this.relationshipLabel = relationshipLabel;
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