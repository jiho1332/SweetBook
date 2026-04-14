package com.sweetbook.service;

import com.sweetbook.vo.BookPagePreviewVO;
import com.sweetbook.vo.BookRequestPreviewVO;
import com.sweetbook.vo.MemoryVO;
import com.sweetbook.vo.PetVO;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookTransformServiceImpl implements BookTransformService {

    private final PetService petService;
    private final MemoryService memoryService;

    public BookTransformServiceImpl(PetService petService, MemoryService memoryService) {
        this.petService = petService;
        this.memoryService = memoryService;
    }

    @Override
    public BookRequestPreviewVO buildBookRequestPreview(Long petId, String templateCode, String bookSpecCode) {
        PetVO petVO = petService.getPetById(petId);
        if (petVO == null) {
            throw new IllegalArgumentException("존재하지 않는 반려견입니다. petId=" + petId);
        }

        List<MemoryVO> memoryList = memoryService.getMemoryListByPetId(petId);
        if (memoryList == null || memoryList.isEmpty()) {
            throw new IllegalArgumentException("책으로 변환할 추억 데이터가 없습니다. petId=" + petId);
        }

        List<BookPagePreviewVO> pages = new ArrayList<>();
        int pageNumber = 1;

        for (MemoryVO memoryVO : memoryList) {
            BookPagePreviewVO page = new BookPagePreviewVO();
            page.setPageNumber(pageNumber++);
            page.setChapterType(memoryVO.getChapterType());
            page.setTitle(memoryVO.getTitle());
            page.setText(memoryVO.getContent());
            page.setImageUrl(memoryVO.getImageUrl());
            pages.add(page);
        }

        String defaultTitle = petVO.getName() + "와 함께한 시간";

        BookRequestPreviewVO preview = new BookRequestPreviewVO();
        preview.setPetId(petVO.getPetId());
        preview.setPetName(petVO.getName());
        preview.setTitle(defaultTitle);
        preview.setCoverTitle(defaultTitle);
        preview.setCoverSubtitle(buildCoverSubtitle(petVO));
        preview.setDedicationText(buildDedicationText(petVO));
        preview.setTemplateCode(templateCode);
        preview.setBookSpecCode(bookSpecCode);
        preview.setPages(pages);

        return preview;
    }

    private String buildCoverSubtitle(PetVO petVO) {
        if (petVO.getMemorialDate() != null) {
            return petVO.getMemorialDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")) + "의 기억";
        }
        return "소중한 기억을 담아";
    }

    private String buildDedicationText(PetVO petVO) {
        return petVO.getName() + "와 함께한 모든 순간에 고마움을 담아";
    }
}