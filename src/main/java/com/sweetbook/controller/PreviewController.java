package com.sweetbook.controller;

import com.sweetbook.mapper.BookProjectMapper;
import com.sweetbook.mapper.MemoryMapper;
import com.sweetbook.mapper.PetMapper;
import com.sweetbook.vo.BookProjectVO;
import com.sweetbook.vo.MemoryVO;
import com.sweetbook.vo.PetVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api")
public class PreviewController {

    private final BookProjectMapper bookProjectMapper;
    private final MemoryMapper memoryMapper;
    private final PetMapper petMapper;

    public PreviewController(BookProjectMapper bookProjectMapper,
                             MemoryMapper memoryMapper,
                             PetMapper petMapper) {
        this.bookProjectMapper = bookProjectMapper;
        this.memoryMapper = memoryMapper;
        this.petMapper = petMapper;
    }

    @GetMapping("/book-preview/{bookProjectId}")
    public Map<String, Object> preview(@PathVariable("bookProjectId") Long bookProjectId) {

        BookProjectVO project = bookProjectMapper.selectBookProjectById(bookProjectId);
        if (project == null) {
            throw new RuntimeException("book_project 없음");
        }

        Long petId = project.getPetId();
        if (petId == null) {
            throw new RuntimeException("petId 없음");
        }

        PetVO pet = petMapper.selectPetById(petId);
        List<MemoryVO> memories = memoryMapper.selectMemoryListByPetId(petId);

        Map<String, Object> result = new HashMap<>();
        result.put("bookProjectId", bookProjectId);
        result.put("title", nvl(project.getTitle()));
        result.put("coverTitle", nvl(project.getCoverTitle()));
        result.put("subtitle", nvl(project.getCoverSubtitle()));
        result.put("dedicationText", nvl(project.getDedicationText()));
        result.put("templateCode", nvl(project.getTemplateCode()));
        result.put("bookSpecCode", nvl(project.getBookSpecCode()));
        result.put("petName", pet != null ? nvl(pet.getName()) : "");
        result.put("coverImage", pet != null ? nvl(pet.getProfileImageUrl()) : "");
        result.put("memorialDate", pet != null && pet.getMemorialDate() != null ? pet.getMemorialDate().toString() : "");

        List<Map<String, Object>> pages = new ArrayList<>();

        Map<String, Object> coverPage = new HashMap<>();
        coverPage.put("pageType", "COVER");
        coverPage.put("title", nvl(project.getCoverTitle()).isEmpty() ? nvl(project.getTitle()) : nvl(project.getCoverTitle()));
        coverPage.put("subtitle", nvl(project.getCoverSubtitle()));
        coverPage.put("imageUrl", pet != null ? nvl(pet.getProfileImageUrl()) : "");
        coverPage.put("chapterType", "COVER");
        pages.add(coverPage);

        if (!nvl(project.getDedicationText()).isEmpty()) {
            Map<String, Object> dedicationPage = new HashMap<>();
            dedicationPage.put("pageType", "DEDICATION");
            dedicationPage.put("title", "우리의 이야기");
            dedicationPage.put("subtitle", "");
            dedicationPage.put("imageUrl", "");
            dedicationPage.put("text", nvl(project.getDedicationText()));
            dedicationPage.put("chapterType", "INTRO");
            pages.add(dedicationPage);
        }

        if (memories != null) {
            for (MemoryVO memory : memories) {
                Map<String, Object> page = new HashMap<>();
                page.put("pageType", "MEMORY");
                page.put("chapterType", nvl(memory.getChapterType()));
                page.put("title", nvl(memory.getTitle()));
                page.put("subtitle", "");
                page.put("imageUrl", nvl(memory.getImageUrl()));
                page.put("text", nvl(memory.getContent()));
                pages.add(page);
            }
        }

        result.put("pages", pages);

        return result;
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }
}