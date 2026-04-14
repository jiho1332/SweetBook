package com.sweetbook.service;

import com.sweetbook.constant.BookProjectStatus;
import com.sweetbook.vo.BookPagePreviewVO;
import com.sweetbook.vo.BookProjectVO;
import com.sweetbook.vo.BookRequestPreviewVO;
import com.sweetbook.vo.MemoryVO;
import com.sweetbook.vo.PetVO;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookTransformServiceImpl implements BookTransformService {

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

    @Override
    public Map<String, Object> applyTemplate(Long bookProjectId) {
        BookProjectVO project = bookProjectService.getBookProjectById(bookProjectId);
        if (project == null) {
            throw new IllegalArgumentException("존재하지 않는 book_project 입니다. bookProjectId=" + bookProjectId);
        }

        PetVO petVO = petService.getPetById(project.getPetId());
        if (petVO == null) {
            throw new IllegalArgumentException("존재하지 않는 반려견입니다. petId=" + project.getPetId());
        }

        List<MemoryVO> memoryList = memoryService.getMemoryListByPetId(project.getPetId());
        if (memoryList == null || memoryList.isEmpty()) {
            throw new IllegalArgumentException("템플릿에 넣을 memory 데이터가 없습니다. petId=" + project.getPetId());
        }

        if (project.getBookSpecUid() == null || project.getBookSpecUid().isBlank()) {
            throw new IllegalArgumentException("book_spec_uid가 없습니다.");
        }

        if (project.getContentTemplateUid() == null || project.getContentTemplateUid().isBlank()) {
            throw new IllegalArgumentException("content_template_uid가 없습니다.");
        }

        String bookUid = project.getBookUid();

        if (bookUid == null || bookUid.isBlank()) {
            bookUid = sweetBookApiService.createBook(project);

            boolean saved = bookProjectService.modifyBookCreated(
                    project.getBookProjectId(),
                    bookUid,
                    bookUid,
                    BookProjectStatus.DRAFT
            );

            if (!saved) {
                throw new IllegalStateException("book_uid 저장 실패");
            }

            project.setBookUid(bookUid);
            project.setSweetbookBookId(bookUid);
        }

        int appliedCount = 0;

        for (MemoryVO memoryVO : memoryList) {
            sweetBookApiService.addContent(bookUid, project, petVO, memoryVO);
            appliedCount++;
        }

        bookProjectService.modifyBookProjectStatus(project.getBookProjectId(), BookProjectStatus.DRAFT);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("bookProjectId", project.getBookProjectId());
        result.put("bookUid", bookUid);
        result.put("bookSpecUid", project.getBookSpecUid());
        result.put("contentTemplateUid", project.getContentTemplateUid());
        result.put("memoryCount", memoryList.size());
        result.put("appliedCount", appliedCount);
        result.put("status", BookProjectStatus.DRAFT);
        result.put("message", "책 생성 및 내지 적용이 완료되었습니다.");

        return result;
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