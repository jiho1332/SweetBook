package com.sweetbook.controller;

import com.sweetbook.service.BookProjectService;
import com.sweetbook.service.BookTransformService;
import com.sweetbook.service.SweetBookApiService;
import com.sweetbook.vo.BookProjectVO;
import com.sweetbook.vo.BookRequestPreviewVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

        validatePreviewProject(project);

        BookRequestPreviewVO preview = bookTransformService.buildBookRequestPreview(
                project.getPetId(),
                project.getTemplateCode(),
                project.getBookSpecCode()
        );

        return ResponseEntity.ok(preview);
    }

    @PostMapping("/book-projects/{bookProjectId}/apply-template")
    public ResponseEntity<Map<String, Object>> applyTemplate(
            @PathVariable("bookProjectId") Long bookProjectId) {

        BookProjectVO project = bookProjectService.getBookProjectById(bookProjectId);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }

        validateTemplateProject(project);

        Map<String, Object> result = bookTransformService.applyTemplate(bookProjectId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/book-projects/{bookProjectId}/finalize")
    public ResponseEntity<Map<String, Object>> finalizeBook(
            @PathVariable("bookProjectId") Long bookProjectId) {

        BookProjectVO project = bookProjectService.getBookProjectById(bookProjectId);
        if (project == null) {
            return ResponseEntity.notFound().build();
        }

        if (project.getBookUid() == null || project.getBookUid().isBlank()) {
            throw new IllegalArgumentException("book_uid가 없습니다. 먼저 템플릿 적용을 완료해야 합니다.");
        }

        String response = sweetBookApiService.finalizeBook(project.getBookUid());
        bookProjectService.modifyBookFinalized(bookProjectId, "FINALIZED");

        return ResponseEntity.ok(Map.of(
                "success", true,
                "bookProjectId", bookProjectId,
                "bookUid", project.getBookUid(),
                "status", "FINALIZED",
                "response", response
        ));
    }

    private void validatePreviewProject(BookProjectVO project) {
        if (project.getTemplateCode() == null || project.getTemplateCode().isBlank()) {
            throw new IllegalArgumentException("template_code가 비어 있습니다.");
        }

        if (project.getBookSpecCode() == null || project.getBookSpecCode().isBlank()) {
            throw new IllegalArgumentException("book_spec_code가 비어 있습니다.");
        }

        if (project.getTitle() == null || project.getTitle().isBlank()) {
            throw new IllegalArgumentException("책 제목이 비어 있습니다.");
        }

        if (project.getPetId() == null) {
            throw new IllegalArgumentException("pet_id가 없습니다.");
        }
    }

    private void validateTemplateProject(BookProjectVO project) {
        if (project.getBookSpecUid() == null || project.getBookSpecUid().isBlank()) {
            throw new IllegalArgumentException("book_spec_uid가 비어 있습니다.");
        }

        if (project.getContentTemplateUid() == null || project.getContentTemplateUid().isBlank()) {
            throw new IllegalArgumentException("content_template_uid가 비어 있습니다.");
        }

        if (project.getTitle() == null || project.getTitle().isBlank()) {
            throw new IllegalArgumentException("책 제목이 비어 있습니다.");
        }

        if (project.getPetId() == null) {
            throw new IllegalArgumentException("pet_id가 없습니다.");
        }
    }
}