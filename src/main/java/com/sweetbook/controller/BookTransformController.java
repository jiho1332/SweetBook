package com.sweetbook.controller;

import com.sweetbook.service.BookProjectService;
import com.sweetbook.service.BookTransformService;
import com.sweetbook.service.SweetBookApiService;
import com.sweetbook.vo.BookProjectVO;
import com.sweetbook.vo.BookRequestPreviewVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookTransformController {

    private final BookProjectService bookProjectService;
    private final BookTransformService bookTransformService;
    private final SweetBookApiService sweetBookApiService;

    public BookTransformController(BookProjectService bookProjectService,
                                   BookTransformService bookTransformService,
                                   SweetBookApiService sweetBookApiService) {
        this.bookProjectService = bookProjectService;
        this.bookTransformService = bookTransformService;
        this.sweetBookApiService = sweetBookApiService;
    }

    @GetMapping("/templates")
    public String getTemplates() {
        return sweetBookApiService.getTemplates();
    }

    @GetMapping("/book-specs")
    public String getBookSpecs() {
        return sweetBookApiService.getBookSpecs();
    }

    @GetMapping("/list")
    public String getBooks() {
        return sweetBookApiService.getBooks();
    }

    @GetMapping("/book-projects/{bookProjectId}/preview")
    public ResponseEntity<BookRequestPreviewVO> previewBookRequest(
            @PathVariable("bookProjectId") Long bookProjectId) {

        BookProjectVO project = bookProjectService.getBookProjectById(bookProjectId);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }

        validateProject(project);

        BookRequestPreviewVO preview = bookTransformService.buildBookRequestPreview(
                project.getPetId(),
                project.getTemplateCode(),
                project.getBookSpecCode()
        );

        return ResponseEntity.ok(preview);
    }

    @PostMapping("/book-projects/{bookProjectId}/create")
    @ResponseBody
    public String createBook(@PathVariable("bookProjectId") Long bookProjectId) {

        BookProjectVO project = bookProjectService.getBookProjectById(bookProjectId);
        if (project == null) {
            throw new IllegalArgumentException("존재하지 않는 책 프로젝트입니다. bookProjectId=" + bookProjectId);
        }

        validateProject(project);

        BookRequestPreviewVO preview = bookTransformService.buildBookRequestPreview(
                project.getPetId(),
                project.getTemplateCode(),
                project.getBookSpecCode()
        );

        if (preview == null) {
            throw new IllegalStateException("책 미리보기 데이터 생성에 실패했습니다.");
        }

        String bookUid = sweetBookApiService.createBook(preview);
        sweetBookApiService.addContents(bookUid, preview);
        sweetBookApiService.finalizeBook(bookUid);

        project.setSweetbookBookId(bookUid);
        project.setStatus("CREATED");
        bookProjectService.modifyBookProject(project);

        return "책 생성 완료: " + bookUid;
    }

    private void validateProject(BookProjectVO project) {
        if (project.getTemplateCode() == null || project.getTemplateCode().isBlank()) {
            throw new IllegalArgumentException("템플릿이 선택되지 않았습니다.");
        }

        if (project.getBookSpecCode() == null || project.getBookSpecCode().isBlank()) {
            throw new IllegalArgumentException("판형이 선택되지 않았습니다.");
        }

        if (project.getTitle() == null || project.getTitle().isBlank()) {
            throw new IllegalArgumentException("책 제목이 비어 있습니다.");
        }

        if (project.getPetId() == null) {
            throw new IllegalArgumentException("반려견 정보가 연결되지 않았습니다.");
        }
    }
}