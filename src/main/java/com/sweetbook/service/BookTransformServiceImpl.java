package com.sweetbook.service;

import com.sweetbook.vo.BookPagePreviewVO;
import com.sweetbook.vo.BookProjectVO;
import com.sweetbook.vo.BookRequestPreviewVO;
import com.sweetbook.vo.MemoryVO;
import com.sweetbook.vo.PetVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookTransformServiceImpl implements BookTransformService {

    private static final int FORCE_CONTENT_COUNT = 30;

    private final PetService petService;
    private final MemoryService memoryService;
    private final BookProjectService bookProjectService;
    private final SweetBookApiService sweetBookApiService;

    public BookTransformServiceImpl(PetService petService,
                                    MemoryService memoryService,
                                    BookProjectService bookProjectService,
                                    SweetBookApiService sweetBookApiService) {
        this.petService = petService;
        this.memoryService = memoryService;
        this.bookProjectService = bookProjectService;
        this.sweetBookApiService = sweetBookApiService;
    }

    @Override
    public BookRequestPreviewVO buildBookRequestPreview(Long petId, String templateCode, String bookSpecCode) {
        PetVO pet = petService.getPetById(petId);
        if (pet == null) {
            throw new IllegalArgumentException("반려견 정보를 찾을 수 없습니다. petId=" + petId);
        }

        List<MemoryVO> memoryList = memoryService.getMemoryListByPetId(petId);
        List<BookPagePreviewVO> pages = new ArrayList<>();

        int pageNumber = 1;

        BookPagePreviewVO coverPage = new BookPagePreviewVO();
        coverPage.setPageNumber(pageNumber++);
        coverPage.setChapterType("COVER");
        coverPage.setTitle(pet.getName());
        coverPage.setText("");
        coverPage.setImageUrl(pet.getProfileImageUrl());
        pages.add(coverPage);

        if (memoryList != null && !memoryList.isEmpty()) {
            for (int i = 0; i < FORCE_CONTENT_COUNT; i++) {
                MemoryVO source = memoryList.get(i % memoryList.size());

                BookPagePreviewVO page = new BookPagePreviewVO();
                page.setPageNumber(pageNumber++);
                page.setChapterType(source.getChapterType() != null ? source.getChapterType() : "DAILY");
                page.setTitle(source.getTitle() != null && !source.getTitle().isBlank() ? source.getTitle() : "추억");
                page.setText(source.getContent() != null && !source.getContent().isBlank() ? source.getContent() : "소중한 순간");
                page.setImageUrl(source.getImageUrl() != null && !source.getImageUrl().isBlank() ? source.getImageUrl() : pet.getProfileImageUrl());
                pages.add(page);
            }
        } else {
            for (int i = 0; i < FORCE_CONTENT_COUNT; i++) {
                BookPagePreviewVO page = new BookPagePreviewVO();
                page.setPageNumber(pageNumber++);
                page.setChapterType("DAILY");
                page.setTitle("추억");
                page.setText("소중한 순간");
                page.setImageUrl(pet.getProfileImageUrl());
                pages.add(page);
            }
        }

        BookRequestPreviewVO preview = new BookRequestPreviewVO();
        preview.setPetId(petId);
        preview.setPetName(pet.getName());
        preview.setTitle(pet.getName() + "의 추억책");
        preview.setCoverTitle(pet.getName());
        preview.setCoverSubtitle("");
        preview.setDedicationText("");
        preview.setTemplateCode(templateCode);
        preview.setBookSpecCode(bookSpecCode);
        preview.setPages(pages);

        return preview;
    }

    @Override
    public Map<String, Object> applyTemplate(Long bookProjectId) {
        BookProjectVO project = bookProjectService.getBookProjectById(bookProjectId);
        if (project == null) {
            throw new IllegalArgumentException("존재하지 않는 프로젝트입니다. bookProjectId=" + bookProjectId);
        }

        // 해피패스 고정값
        project.setBookSpecUid("PHOTOBOOK_A4_SC");
        project.setBookSpecCode("PHOTOBOOK_A4_SC");
        project.setContentTemplateUid("58edh76I0rYa");
        project.setTemplateCode("58edh76I0rYa");

        PetVO pet = petService.getPetById(project.getPetId());
        if (pet == null) {
            throw new IllegalArgumentException("반려견 정보가 없습니다.");
        }

        if (pet.getProfileImageUrl() == null || pet.getProfileImageUrl().isBlank()) {
            throw new IllegalArgumentException("대표 이미지가 없습니다. 대표 이미지를 먼저 등록해주세요.");
        }

        List<MemoryVO> memoryList = memoryService.getMemoryListByPetId(project.getPetId());

        int contentCount = 0;
        String bookUid = null;

        try {
            bookUid = sweetBookApiService.createBook(project);
            bookProjectService.modifyBookCreated(bookProjectId, bookUid, bookUid, "BOOK_CREATED");

            sweetBookApiService.addCover(bookUid, project, pet);

            for (int i = 0; i < FORCE_CONTENT_COUNT; i++) {
                MemoryVO requestMemory = new MemoryVO();

                if (memoryList != null && !memoryList.isEmpty()) {
                    MemoryVO source = memoryList.get(i % memoryList.size());

                    requestMemory.setMemoryId(source.getMemoryId());
                    requestMemory.setPetId(source.getPetId());
                    requestMemory.setChapterType(source.getChapterType() != null ? source.getChapterType() : "DAILY");
                    requestMemory.setDisplayOrder(i + 1);
                    requestMemory.setTitle(source.getTitle() != null && !source.getTitle().isBlank() ? source.getTitle() : "추억");
                    requestMemory.setContent(source.getContent() != null && !source.getContent().isBlank() ? source.getContent() : "소중한 순간");
                    requestMemory.setImageUrl(source.getImageUrl() != null && !source.getImageUrl().isBlank() ? source.getImageUrl() : pet.getProfileImageUrl());
                } else {
                    requestMemory.setPetId(project.getPetId());
                    requestMemory.setChapterType("DAILY");
                    requestMemory.setDisplayOrder(i + 1);
                    requestMemory.setTitle("추억");
                    requestMemory.setContent("소중한 순간");
                    requestMemory.setImageUrl(pet.getProfileImageUrl());
                }

                sweetBookApiService.addContent(bookUid, project, pet, requestMemory);
                contentCount++;
            }

            sweetBookApiService.finalizeBook(bookUid);
            bookProjectService.modifyBookFinalized(bookProjectId, "FINALIZED");

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("bookProjectId", bookProjectId);
            result.put("bookUid", bookUid);
            result.put("contentCount", contentCount);
            result.put("status", "FINALIZED");
            return result;

        } catch (Exception e) {
            bookProjectService.modifyBookProjectStatus(bookProjectId, "FAILED");
            throw new RuntimeException("책 생성 실패: " + e.getMessage(), e);
        }
    }
}